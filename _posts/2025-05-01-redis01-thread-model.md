---
title: Redis参考01：线程模型
author: fangkun119
date: 2025-05-01 12:00:00 +0800
categories: [中间件, Redis]
tags: [Redis]
pin: false
math: true
mermaid: true
image:
  path: /imgs/cover/redis.jpeg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: Responsive rendering of Chirpy theme on multiple devices.
---

{: .no_toc }

<details close markdown="block">
  <summary>
    目录
  </summary>
  {: .text-delta }
- TOC
{:toc}
</details>
本文介绍 Redis 的线程模型，背后的设计考虑。以及与之相关的操作原子性是如何保证的，包括键值读写、复合指令、事务、管道、LUA 脚本、Fucntion。最后简单介绍 Redis Big Key 问题。

## 1 Redis介绍

### 1.1 Redis是什么？

全称 REmote DIctionary Server ，远程字典服务，全开源的⾼性能Key-Value数据库。 

官⽹地址： https://redis.io/ 。 

### 1.2 Redis的特点

Redis 具有如下特点

#### 1.2.1 数据结构复杂。

Redis相⽐于传统的K-V型数据库，能够⽀撑更更复杂的数据类型。

这意味着Redis 已经远远超出了缓存的范围，可以实现很多复杂的业务场景。并且还在不断发展更 多的业务场景。

#### 1.2.2 数据保存在内存，但是持久化到硬盘。

数据全部保存在内存，意味着Redis进⾏数据读和写的性能⾮常⾼ 。是集中式缓存的不⼆之选。

数据持久化到硬盘，意味着Redis上保存的数据是有安全保证的的，可以当做⼀个数据库来⽤。

所以， 官⽅对Redis的作⽤ ，也已经定位成了三个⽅⾯： Cache(缓存)， Database(数据库) ，Vector Search(向量搜索)

### 1.3 2024年的Redis是什么样的？

在2023年之前， Redis是⼀个纯粹的开源数据库。但是最近两年， Redis在进⾏华丽的蜕变，从缓存产品变成⼀整套⽣态服务。它包括：

| 名称          | 描述                                                         |
| ------------- | ------------------------------------------------------------ |
| Redis Cloud   | 基于AWS ，Azure等公有云的云服务。提供完整的企业服务，包括Redis Enterprise ，企业级的收费产品服务。 |
| Redis Insight | Redis服务的安装及管理套件，图形化客户端，不需要再寻找第三方客户端，t也可以在Redis Cloud上直接使⽤ 。 |
| Redis OSS     | 就是常⽤的开源的服务体系                                     |
| Redis Stack   | 基于Redis OSS打造的⼀套更完整的技术栈。它基于Redis Cloud提供服务，并且提供了很多高级扩展功能。 |

### 1.4 搭建实验环境所需要的配置

搭建单机Redis进行实验，建议调整下面的配置

```conf
daemonize yes    	# 允许后台启动
protected-mode no 	# 关闭保护模式，开启的话，只有本机才可以访问redis
# bind 127.0.0.1（ bind绑定的是⾃⼰机器⽹卡的ip，如果有多块⽹卡可以配多个 ip，代表允许客户端通过机器的哪些⽹卡ip去访问， 内⽹⼀般可以不配置bind，注释掉即可）
# 访问密码，建议开启
requirepass 123qweasd
```

## 2 Redis是单线程还是多线程？

### 2.1 整体概括

简单解释：客户端连接多线程，指令执行是单线程

* 客户端连接：为了能够与更多的客户端进⾏连接，还是使⽤的多线程来维护与客户端的Socket连接。

* 指令执行：则是由⼀个单独的主线程完成的。 

Redis 基于epoll实现了IO多路复⽤ ，这就可以⽤⼀个主线程同时响应多个客户端Socket连接的请求。

在这种线程模型下， Redis将客户端多个并发的请求转成了串⾏的执⾏⽅式。 因此， 在Redis中，完全不⽤考虑诸如MySQL的脏读、幻读、不可重复读之类的并发问题。并且，这种串⾏化的线程模型，加上Redis基于内存⼯作的极⾼性能，也让Redis成为很多并发问题的解决⼯具。

在redis.conf中就有⼀个参数maxclients维护了最⼤的客户端连接数

```conf
# Redis is mostly single threaded, however there are certain threaded
# operations such as UNLINK, slow I/O accesses and other things that are
# performed on side threads.
#
# Now it is also possible to handle Redis clients socket reads and writes
# in different I/O threads. Since especially writing is so slow, normally
# Redis users use pipelining in order to speed up the Redis performances per
# core, and spawn multiple instances in order to scale more. Using I/O
# threads it is possible to easily speedup two times Redis without resorting
# to pipelining nor sharding of the instance.
#
# By default threading is disabled, we suggest enabling it only in machines
# that have at least 4 or more cores, leaving at least one spare core.
# Using more than 8 threads is unlikely to help much. We also recommend using
# threaded I/O only if you actually have performance problems, with Redis
# instances being able to use a quite big percentage of CPU time, otherwise
# there is no point in using this feature.
#
# So for instance if you have a four cores boxes, try to use 2 or 3 I/O
# threads, if you have a 8 cores, try to use 6 threads. In order to enable I/O threads use the following configuration directive:
#
# io-threads 4

# Set the max number of connected clients at the same time. By default
# this limit is set to 10000 clients, however if the Redis server is not
# able to configure the process file limit to allow for the specified limit
# the max number of allowed clients is set to the current file limit
# minus 32 (as Redis reserves a few file descriptors for internal uses).
#
# Once the limit is reached Redis will close all the new connections sending
# an error 'max number of clients reached '.
#
# IMPORTANT: When Redis Cluster is used, the max number of connections is also
# shared with the cluster bus: every node in the cluster will use two
# connections, one incoming and another outgoing. It is important to size the
# limit accordingly in case of very large clusters.
#
# maxclients 10000
```

### 2.2 版本差异

然后，严格来说，Redis后端的线程模型跟Redis的版本是有关系的。

| 版本                 | 介绍                                                         |
| -------------------- | ------------------------------------------------------------ |
| Redis4.X及以前的版本 | 采⽤的纯单线程                                               |
| Redis5.x             | 进⾏了⼀次⼤的核⼼代码重构                                   |
| Redis6.x和7.x版本    | 开始⽤⼀种全新的多线程机制来提升后台⼯作。<br />尤其在现在的Redis7.x版本中， Redis后端的很多⽐较费时的操作， ⽐如持久化RDB ，AOF⽂件、 unlink异步删除、集群数据同步等，都是由额外的线程执⾏的。<br />例如，对于 FLUSHALL操作，就已经提供了异步的⽅式<br />`127.0.6.1:6379> FLUSHAL` |

### 2.3 客户端连接多线程的原因

现代CPU早就是多核架构了， Redis如果⼀直使⽤单线程，就不能发挥多核CPU的性能优势，迟早是跟不上时代的。

并且那些⾮常耗时的操作，也必然会对主线程产⽣影响。

所以， 多线程是⼀个必然结果。

只不过，对于Redis来说，为了保持快速， 多线程会⾛得⾮常谨慎。

### 2.4 指令执行保持单线程的原因

**原因1：瓶颈不在CPU，需求不迫切**

对于现代的Redis来说，CPU通常不会成为Redis的性能瓶颈。影响Redis的性能瓶颈⼤部分是内存和⽹络。 因此，核⼼线程改为多线程的要求并不急切。

**原因 2：单线程可以减少上下文切换的性能消耗**

**原因 3：避免增加复杂性**

如果Redis将核⼼线程改为多线程并发执⾏ ，那么就必然带来资源竞争，反⽽会极⼤增加Redis的业务复杂性，影响Redis的业务执⾏效率。

## 3 Redis如何保证指令原⼦性

### 3.1 键值读写：天然保证原子性

对于核⼼的读写键值的操作， Redis是单线程处理的。如果多个客户端同时进⾏读写请求， Redis只会排队串⾏ 。也就是说，针对单个客户端， Redis并没有类似MySQL 的事务那样保证同⼀个客户端的操作原⼦性。像下⾯这种情况，返回的k1的值，就很难确定。

如何控制Redis指令的原⼦性呢？这是⼀系列的问题，在不同的业务场景下， Redis 也提供了不同的思路。我们需要在项⽬中能够灵活选择。

### 3.2 复合指令：为多个操作提供原子性

Redis内部提供了很多复合指令，他们是⼀个指令，可是明显⼲着多个指令的活。

⽐如 MSET(HMSET) 、GETSET 、SETNX 、SETEX 。

这些复合指令都能很好的保持原⼦性。

### 3.3 Redis事务：保证指令不被加塞，但不能回滚

#### 3.3.1 Redis事务使用方法

像MySQL—样， Redis也提供了事务机制。

```text
127.0.0.1:6379> help @transactions

MULTI (null)  -- 开启事务
summary: Starts a transaction.
since: 1.2.0

DISCARD (null)  -- 放弃事务
summary: Discards a transaction.
since: 2.0.0

EXEC (null)  -- 执⾏事务
summary: Executes all commands in a transaction.
since: 1.2.0

WATCH key [key ...]   --监听某—个key的变化。 key有变化后，就执⾏当前事务
summary: Monitors changes to keys to determine the execution of a transaction.
since: 2.2.0

UNWATCH (null)  --去掉监听
summary: Forgets about watched keys of a transaction.
since: 2.2.0
```

使⽤⽅式也很典型，开启事务后，接⼊—系列操作，然后根据执⾏情况选择是执⾏事务还是回滚事务。

下面是一个例子：

```shell
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379(TX)> set k2 2
QUEUED
127.0.0.1:6379(TX)> incr k2
QUEUED
127.0.0.1:6379(TX)> get k2
QUEUED
127.0.0.1:6379(TX)> EXEC  --执⾏事务
1) OK
2) (integer) 3
3) "3"
127.0.0.1:6379> DISCARD  -- 放弃事务
```

#### 3.3.2 Redis事务不能回滚

Redis 事务和数据库中的事务，是不是同⼀回事呢？

看下⾯这个例⼦。

```shell
127.0.0.1:6379> MULTI
OK
127.0.0.1:6379(TX)> set k2 2 # 类型是 string
QUEUED
127.0.0.1:6379(TX)> incr k2
QUEUED
127.0.0.1:6379(TX)> get k2
QUEUED
127.0.0.1:6379(TX)> lpop k2 # 把 list 操作用在 string 上，引发报错
QUEUED
127.0.0.1:6379(TX)> incr k2
QUEUED
127.0.0.1:6379(TX)> get k2
QUEUED
127.0.0.1:6379(TX)> exec # 执行事务，事务只是中午失败停止，并没有回滚
1) OK
2) (integer) 3
3) "3"
4) (error) WRONGTYPE Operation against a key holding the wrong kind of value
5) (integer) 4
6) "4"
```

从这⾥可以看到。 Redis的事务并不是像数据库的事务那样，保证事务中的指令⼀起成功或者⼀起失败。 

Redis的事务作⽤ ：**仅仅只是保证事务中的原⼦操作是⼀起执⾏ ，⽽不会在执⾏过程中被其他指令加塞。

实际上，在事务指令敲击的过程中可以看到， 开启事务后，所有操作的返回结果都是QUEUED ，表示这些操作只是排好了队，等到EXEC后⼀起执⾏。

对于Redis事务的更多说明，参⻅官⽅文档：https://redis.io/docs/latest/develop/interact/transactions/

#### 3.3.3 Redis事务总结

(1) Redis事务可以通过Watch机制进⼀步保证在某个事务执⾏前，某⼀个key不被修改（Key修改触发事务执行）

(2) Redis事务失败回滚：指令敲错事务放弃，指令执行失败不回滚
- 如果事务是在EXEC前失败（⽐如事务中的指令敲错了，或者指令的参数不对），那么整个事务的操作都不会执⾏。
- 如果事务是在EXEC执⾏之后失败（⽐如指令操作的key类型不对），那么事务中的其他操作都会正常执⾏，不受影响。

(3) 事务执⾏过程中出现失败了怎么办
- 只要客户端执⾏了EXEC指令，那么就算之后客户端的连接断开了，事务就会⼀直进⾏下去。
- 事务有可能引发数据不⼀致，导致 Redis 不能启动，需要修复 AOF 文件
  当EXEC指令执⾏后， Redis会先将事务中的所有操作都先记录到AOF⽂件中，然后再执⾏具体的操作。这时有⼀种可能， Redis保存了AOF记录后，事务的操作在执⾏过程中，服务就出现了⾮正常宕机（服务崩溃了，或者执⾏进程被kill -9了）。这就会造成AOF中记录的操作，与数据不符合。如果Redis发现这种情况，那么在下次服务启动时，就会出现错误，⽆法正常启动。这时，需要使⽤ `redis-check-aof` ⼯具修复AOF⽂件，将这些不完整的事务操作记录移除掉。这样下次服务就可以正常启动了。
- 事务机制优缺点，什么时候⽤事务？没有标准答案，⾃⾏总结。

### 3.4 Pipeline：降低 RTT，但不具备原子性

#### 3.4.1 管道使用演示

使⽤ `redis-cli --help` 可以看到这两个 pipe 指令

```
--pipe             Transfer raw Redis protocol from stdin to server.
--pipe-timeout <n> In --pipe mode, abort with error if after sending all data. no reply is received within <n> seconds. Default timeout: 30. Use 0 to wait forever.
```

在 Linux 上编辑⼀个⽂件 command.txt 。⽂件中可以包含⼀系列的指令

```
set count 1
incr count
incr count
incr count
```

然后在客户端执⾏ redis-cli 指令时，就可以直接执⾏这个⽂件中的指令。

```
[root@192-168-65-214 ~]# cat command.txt | redis-cli -a 123qweasd --pipe
Warning: Using a password with '-a ' or '-u ' option on the command line interface may not be safe. All data transferred. Waiting for the last reply... Last reply received from server.
errors: 0, replies: 4

[root@192-168-65-214 ~]# redis-cli -a 123qweasd
Warning: Using a password with '-a ' or '-u ' option on the command line interface may not be safe.
127.0.0.1:6379> get count
"4"
```

#### 3.4.2 管道的用途

结论：如果你有⼤批量的数据需要快速写⼊到Redis中，这种⽅式可以⼀定程度提⾼执⾏效率

具体参考官⽅文档：https://redis.io/docs/latest/develop/use/pipelining/

核⼼作⽤：优化RTT（round-trip time）

当客户端执⾏⼀个指令，数据包需要通过⽹络从Client传到Server，然后再从Server返回到Client。这个中间的时间消耗，就称为RTT（Rount Trip Time）。

Redis提供了pipeline机制。其思路也⽐较简单明了，就是将客户端的多个指令打包，一起往服务端推送。

例如官⽅就给出了⼀个案例：

```
[root@192-168-65-214 ~]# printf "AUTH\n123qweasd\r\nPING\r\nPING\r\nPING\r\n" | nc localhost 6379
+OK
+PONG
+PONG
+PONG
```

#### 3.4.3 注意事项

如下

1. redis的原⽣复合指令和事务，都是原⼦性的。但是pipeline不具备原⼦性。
2. pipeline只是将多条命令发送到服务端，最终还是可能会被其他客户端的指令加塞的，虽然这种概率通常⽐较⼩。
3. pipeline的执⾏需要客户端和服务端同时完成，pipeline在执⾏过程中，会阻塞当前客户端。在pipeline中不建议拼装过多的指令。
4. 总体来说，pipeline机制适合做⼀些在⾮热点时段进⾏的数据调整任务。

### 3.5 LUA脚本：具有原子性

管道不保证原子性，主要用于

Redis的事务和Pipeline机制，对于Redis的指令原⼦性问题，都有⼀定的帮助，但是这两个机制对于指令原⼦性问题都有⽔⼟不服的地⽅。并且，他们都只是对Redis现有指令进⾏拼凑，⽆法添加更多⾃定义的复杂逻辑。因此，企业中⽤到更多的是LUA脚本。同时也是Redis7版本着重调整的⼀个功能。

#### 3.5.1 什么是LUA？为什么Redis⽀持 LUA？

LUA是⼀种⼩巧的脚本语⾔，拥有很多⾼级语⾔的特性，⽐如参数类型、作⽤域、函数等。语法简单，熟悉Java后基本上可以零⻔槛上⼿LUA。

LUA语⾔最⼤的特点是单线程模式，这使得 LUA天⽣就⾮常适合⼀些单线程模型的中间件，⽐如Redis、Nginx等。

在Redis中执⾏⼀段 LUA 脚本，天然就是原⼦性的。

#### 3.5.2 Redis中如何执⾏LUA

Redis中对lua语⾔的API介绍参考官⽅文档：https://redis.io/docs/latest/develop/interact/programmability/lua-api/

具体参考指令可以使⽤ `help eval` 指令查看：

```
127.0.0.1:6379> help eval

EVAL script numkeys [key [key ...]] [arg [arg ...]]
summary: Executes a server-side Lua script.
since: 2.6.0
group: scripting
```

示例：

```
127.0.0.1:6379> eval "return {KEYS[1],KEYS[2],ARGV[1],ARGV[2]}" 2 key1 key2 first second
1) "key1"
2) "key2"
3) "first"
4) "second"
```

在lua脚本中，可以使⽤ `redis.call` 函数来调⽤Redis的命令：

```
127.0.0.1:6379> set stock_1 1
OK
127.0.0.1:6379> eval "local initcount = redis.call('get', KEYS[1]) local a = tonumber(initcount) local b = tonumber(ARGV[1]) if a >= b then redis.call('set', KEYS[1], a) return 1 end redis.call('set', KEYS[1], b) return 0" 1 "stock_1" 10
(integer) 0
127.0.0.1:6379> get stock_1
"10"
```

#### 3.5.3 使⽤LUA注意点

##### (1) 避免死循环和耗时运算

否则redis会阻塞，将不接受其他的命令。Redis中有⼀个配置参数来控制Lua脚本的最⻓控制时间，默认5秒钟。

```conf
lua-time-limit 5000
busy-reply-threshold 5000
```

##### (2) 尽量使⽤只读脚本

只读脚本是Redis7中新增的⼀种脚本执⾏⽅法，表示那些不修改Redis数据集的只读脚本。

需要在脚本上加上⼀个只读的标志，并通过指令 `EVAL_RO` 触发。

### 3.6 Redis Function：保证原子性

#### 3.6.1 什么是Function

Redis Function允许将⼀些功能声明成⼀个统⼀的函数，提前加载到Redis服务端。

客户端可以直接调⽤这些函数，⽽不需要再去开发函数的具体实现。

Redis Function更⼤的好处在于在Function中可以嵌套调⽤其他Function ，从⽽更 有利于代码复⽤ 。相⽐之下， lua脚本就⽆法进⾏复⽤ 。

Redis Function同样保证内部的指令是在一个原子操作中执行的：“Functions provide the same core functionality as scripts but are first-class software artifacts of the database”（ https://redis.io/docs/latest/develop/interact/programmability/functions-intro/ )

#### 3.6.2 如何使用 Function

在服务器上新增⼀个 `mylib.lua` 文件，内容如下：

```lua
#!lua name=mylib

local function my_hset(keys, args)
  local hash = keys[1]
  local time = redis.call('TIME')[1]
  return redis.call('HSET', hash, '_last_modified_', time, unpack(args))
end

redis.register_function('my_hset', my_hset)
```

注意脚本第⼀⾏是指定函数的命名空间，不是注释，不能少

加载函数：

```bash
[root@192-168-65-214 myredis]# cat mylib.lua | redis-cli -a 123qweasd -x FUNCTION LOAD REPLACE
```

调用函数：

```
127.0.0.1:6379> FCALL my_hset 1 myhash myfield "some value" another_field "another value"
(integer) 3
127.0.0.1:6379> HGETALL myhash
1) "_last_modified_"
2) "1717748001"
3) "myfield"
4) "some value"
5) "another_field"
6) "another value"
```

#### 3.6.3 Function注意事项

如下

1. Function同样也可以进⾏只读调⽤。
2. **如果在集群中使⽤Function，⽬前版本需要在各个节点都⼿动加载⼀次**。
3. **Function是要在服务端缓存的，所以不建议使⽤太多太⼤的Function**。
4. Function和Script⼀样，也有⼀系列的管理指令。使⽤指令 `help @scripting` ⾃⾏了解。

### 3.7 Redis原子性总结

以上介绍的各种机制，其实都是Redis改变指令执⾏顺序的⽅式。在这⼏种⼯具中，Lua脚本通常会是项⽬中⽤得最多的⽅式。在很多追求极致性能的⾼并发场景，Lua脚本都会担任很重要的⻆⾊。但是其他的各种⽅式也需要了解，这样⾯临真实业务场景，才有更多的⽅案可以选择。

## 4 Redis中的Big key问题

Big key指那些占⽤空间⾮常⼤的key。⽐如⼀个list中包含200W个元素，或者⼀个string⾥放⼀篇⽂章。基于Redis的单线程为主的核心工作机制，这些Big key⾮常容易造成Redis的服务阻塞。因此在实际项⽬中，⼀定需要特殊关照。

在Redis客户端指令中，提供了两个扩展参数，可以帮助快速发现这些 Big Key

```bash
[root@192-168-65-214 myredis]# redis-cli --help
...
--bigkeys          Sample Redis keys looking for keys with many elements (complexity).
--memkeys          Sample Redis keys looking for keys consuming a lot of memory.
```

关于BigKey的处理，后⾯继续深入介绍。

## 5 Redis线程模型总结

Redis的线程模型整体还是多线程的，只是后台执⾏指令的核⼼线程是单线程的。整个线程模型可以理解为还是以单线程为主。基于这种单线程为主的线程模型，不同客户端的各种指令都需要依次排队执⾏。

Redis这种以单线程为主的线程模型，相⽐其他中间件，还是⾮常简单的。这使得Redis处理线程并发问题，要简单⾼效很多。甚⾄在很多复杂业务场景下，Redis都是⽤来进⾏线程并发控制的很好的⼯具。但是，这并不意味着Redis就没有线程并发的问题。这时候选择合理的指令执⾏⽅式，就⾮常重要了。

另外，Redis这种⽐较简单的线程模型其实本身是不利于发挥多线程的并发优势的。⽽且Redis的应⽤场景⼜通常与⾼性能深度绑定在⼀起，所以在使⽤Redis的时候，还是要时刻思考Redis的这些指令执⾏⽅式，这样才能最⼤限度发挥Redis⾼性能的优势。
