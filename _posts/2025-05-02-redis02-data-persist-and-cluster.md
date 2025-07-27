---
title: Redis参考02：数据持久化和集群架构
author: fangkun119
date: 2025-05-02 12:00:00 +0800
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
## 1 Redis性能压测脚本介绍

### 1.1 平衡数据安全和Redis性能非常重要

内存⾼效的读写性能使Redis的性能⾮常强、但内存的缺点是断电即丢失。

实际项⽬中，重要数据不可能完全使⽤内存保存数据。要针对应⽤场景，对Redis的性能进⾏估算，以便在数据安全与读写之间找到⼀个平衡点。

### 1.2 Redis压测脚本

Redis提供了压测脚本redis-benchmark ，可以对Redis进⾏快速的基准测试。

```shell
# 20个线程， 100W个请求，测试redis的set指令(写数据)
redis-benchmark -a 123qweasd -t set -n 1000000 -c 20

Summary:
  throughput summary: 116536.53 requests per second   ##平均每秒11W次写操作。
  latency summary (msec):
    avg       min       p50       p95       p99       max
    0.111     0.032     0.111     0.167     0.215     3.199
```

redis-benchmark 更多参数，使⽤ `redis-benchmark --help` 指令查看

## 2 Redis数据持久化机制

### 2.1 四类持久化机制

#### 2.1.1 介绍

官⽅： https://redis.io/docs/latest/operate/oss_and_stack/management/persistence/

Redis提供了很多跟数据持久化相关的配置，⼤体上，可以组成以下⼏种策略：

| 策略                   | 介绍                                                      |
| ---------------------- | --------------------------------------------------------- |
| ⽆持久化               | 完全关闭持久化，不保证数据安全，相当于完全当做缓存。      |
| RDB (Redis Data Base)  | 按照⼀定的时间间隔缓存Redis所有数据快照。                 |
| AOF (Append Only File) | 记录Redis收到的每⼀次写操作，通过操作重放的⽅式恢复数据。 |
| RDB + AOF              | 同时保存Redis的数据和操作。                               |

#### 2.2.2 RBD和AOF对比

##### (1) RDB

**优点**

- RDB⽂件⾮常紧凑，⾮常适合定期备份数据。
- RDB快照⾮常适合灾难恢复。与AOF相⽐ ， RDB在进⾏⼤数据量重启时会快很多。
- RDB备份时性能⾮常快，对主线程的性能⼏乎没有影响。 RDB备份时，主线程只需要启动⼀个负责数据备份的⼦线程即可。所有的备份⼯作都由⼦线程完成， 这对主线程的IO性能⼏乎没有影响。

**缺点**

- RDB不能实时对数据进⾏备份，所以，总会有数据丢失的可能。
- RDB需要fork化⼦线程的数据写⼊情况，在fork的过程中，需要将内存中的数据克隆⼀份。如果数据量太⼤ ，或者CPU性能不是很好， 这个过程容易造成Redis短暂的服务停⽤ 。相⽐之下，AOF也需要进⾏持久化，但频率较低。并且你可以调整⽇志重写的频率。

##### (2) AOF

**优点**

- AOF持久化更安全。例如Redis默认每秒进⾏⼀次AOF写⼊ ， 这样， 即使服务崩溃， 最多损失⼀秒的操作。
- AOF的记录⽅式是在之前基础上每次追加新的操作。 因此AOF不会出现记录不完整的情况。 即使因为⼀些特殊原因，造成⼀个操作没有记录完整，也可以使⽤ redis-check-aof⼯具轻松恢复
- 当AOF⽂件太⼤时， Redis会⾃动切换新的⽇志⽂件。这样就不会出现单个⽂件太⼤的问题
- AOF记录操作的⽅式⾮常简单易懂，可以⾃⾏调整⽇志。 ⽐如，如果你错误的执⾏了⼀次 FLUSHALL 操作，将数据误删除了。使⽤AOF ，你可以简单的将⽇志中最后⼀条FLUSHALL指令删掉，然后重启数据库，就可以恢复所有数据。

**缺点**

- 针对同样的数据集，AOF⽂件通常更大。
- 在写操作频繁的情况下，AOF备份的性能通常更慢。

#### 2.2.3 使用建议

- 把 Redis 只当做缓存来⽤ ，可以直接关闭持久化。
- 服务宕机时可接受⼩部分数据损失，可简单使⽤RDB策略，来获得比较高的性能。
- 更高的数据安全要求，RDB配合AOF，同时也让数据恢复更快，不建议单独使⽤AOF。

### 2.3 RDB详解

#### 2.3.1 RBD用途

RDB可以在指定的时间间隔，备份当前时间点的内存中的全部数据集，并保存到磁盘⽂件当中。通常是dump.rdb⽂件。在恢复时，再将磁盘中的快照⽂件直接加载到内存⾥。

由于RDB存的是全量数据，甚⾄可以直接⽤RDB 文件来传递数据。例如如果需要从⼀个Redis服务中将数据同步到另⼀个Redis服务(最好是同版本)，就可以直接复制最近的RDB⽂件。

#### 2.3.2 RBD配置

##### (1) save：备份策略、RBD备份的触发条件

```conf
# Save the DB to disk. #
# save <seconds> <changes> [<seconds> <changes> ...]
#
# Redis will save the DB if the given number of seconds elapsed and it
# surpassed the given number of write operations against the DB.
#
# Snapshotting can be completely disabled with a single empty string argument
# as in following example:
#
# save ""
#
# Unless specified otherwise, by default Redis will save the DB:
#   * After 3600 seconds (an hour) if at least 1 change was performed
#   * After 300 seconds (5 minutes) if at least 100 changes were performed
#   * After 60 seconds if at least 10000 changes were performed
#
# You can set these explicitly by uncommenting the following line.
#
# save 3600 1 300 100 60 10000
```

##### (2) dir：⽂件⽬录

##### (3) dbfilename：⽂件名

默认dump.rdb

##### (4) rdbcompression：是否启⽤RDB压缩

默认yes。如果不想消耗CPU进⾏压缩，可以设置为no

##### (5) stop-writes-on-bgsave-error：快照写入失败时是否禁止写操作

默认yes。如果配置成no，表示你不在乎数据不⼀致或者有其他手段发现和控制这种不⼀致。在快照写⼊失败时，也能确保redis继续接受新的写⼊请求。

##### (6) rdbchecksum：是否开启CRC校验

默认yes。在存储快照后，还可以让redis使⽤CRC64算法来进⾏数据校验，但是这样做会增加⼤约10%的性能消耗。如果希望获得最⼤的性能提升，可以关闭此功能。

#### 2.3.3 RDB备份触发时机

##### (1) 策略触发

到达配置⽂件中默认的快照配置时，会⾃动触发RDB快照

##### (2) 手动触发

⼿动执⾏save或者bgsave指令时，会触发RDB快照。其中save⽅法会在备份期间阻塞主线程。bgsave则不会阻塞主线程。但是他会fork⼀个⼦线程进⾏持久化，这个过程中会要将数据复制⼀份，因此会占⽤更多内存和CPU。

##### (3) 主从复制触发

主从复制时会触发RDB备份。

##### (4) `LASTSAVE`指令

查看最后⼀次成功执⾏快照的时间。时间是⼀个代表毫秒的LONG数字，在linux中可以使⽤`date -d @{timestamp}`快速格式化。

### 2.4 AOF详解

#### 2.4.1 AOF用途

以⽇志的形式记录每个写操作（读操作不记录）。只允许追加⽂件⽽不允许改写⽂件。

#### 2.4.2 AOF配置

##### (1) appendonly：是否开启aof

默认不开启。

##### (2) appendfilename：⽂件名称

```conf
# The base name of the append only file.
#
# Redis 7 and newer use a set of append-only files to persist the dataset
# and changes applied to it. There are two basic types of files in use:
#
# - Base files, which are a snapshot representing the complete state of the
#   dataset at the time the file was created. Base files can be either in
#   the form of RDB (binary serialized) or AOF (textual commands).
# - Incremental files, which contain additional commands that were applied
#   to the dataset following the previous file.
#
# In addition, manifest files are used to track the files and the order in
# which they were created and should be applied.
#
# Append-only file names are created by Redis following a specific pattern.
# The file name's prefix is based on the 'appendfilename' configuration
# parameter, followed by additional information about the sequence and type.
#
# For example, if appendfilename is set to appendonly.aof, the following file
# names could be derived:
#
# - appendonly.aof.1.base.rdb as a base file.
# - appendonly.aof.1.incr.aof, appendonly.aof.2.incr.aof as incremental files.
# - appendonly.aof.manifest as a manifest file.

appendfilename "appendonly.aof"
```

Redis 7之前，AOF文件名是固定的、只有一个文件 appendonly.aof 

Redis 7引入了多文件机制（AOF Multi-Part）会生成三个文件：重写后的基础文件、对应的增量文件、表示元数据的manifest文件。数据文件以如下格式命名`appendonly.aof.<base-sequence>.<type>.<format>`

| 参数名称          | 用途           | 取值                                                         |
| ----------------- | -------------- | ------------------------------------------------------------ |
| `<base-sequence>` | 表示第几次重写 | 递增序列号、例如 1                                           |
| `<type>`          | 文件用途       | `base` 表示这是一个基础文件<br />`incr` 表示这是一个增量文件 |
| `<format>`        | 文件格式       | `rbd` 基础文件默认会采用RBD格式存储<br />`aof` 增量文件采用AOF格式存储 |

下面是一个例子演示`base-sequence`的用途

| 时间点 | 触发的动作                     | 生成的 AOF 文件（只列文件名） | base-sequence 含义  |
| ------ | ------------------------------ | ----------------------------- | ------------------- |
| Day 0  | 第一次启动，第一次 rewrite     | appendonly.aof.**1**.base.rdb | 这是第 1 代基础文件 |
|        | 继续写命令                     | appendonly.aof.**1**.incr.aof | 第 1 代的增量文件   |
| Day 1  | 第二次 rewrite（BGREWRITEAOF） | appendonly.aof.**2**.base.rdb | 这是第 2 代基础文件 |
|        | 新命令                         | appendonly.aof.**2**.incr.aof | 第 2 代的增量文件   |
| Day 2  | 第三次 rewrite                 | appendonly.aof.**3**.base.rdb | 第 3 代基础文件     |
|        | 新命令                         | appendonly.aof.**3**.incr.aof | 第 3 代的增量文件   |

这样的好处是，通过定期重写，将 AOF 格式的增量文件控制在较小的规模，宕机重启时可以快速加载

##### (3) appendfsync：同步⽅式

默认everysecond 每秒记录⼀次。

no 不记录(交由操作系统进⾏内存刷盘)。

always 记录每次操作，数据更安全，但性能较低。

##### (4) appenddirname：AOF⽂件⽬录

新增参数，指定aof⽇志的⽂件⽬录。实际⽬录是 `{dir}+{appenddirname}`

##### (5) auto-aof-rewrite-percentage, auto-aof-rewrite-min-size：⽂件重写触发策略

默认每个⽂件64M，写到100%，进⾏⼀次重写。

Redis会定期对AOF中的操作进⾏优化重写，让AOF中的操作更为精简。例如将多个INCR指令，合并成⼀个SET指令。同时，在Redis7的AOF⽂件中，会⽣成新的base rdb⽂件和incr.aof⽂件。

AOF重写也可以通过指令 `BGREWRITEAOF` ⼿动触发

##### (6) no-appendfsync-on-rewrite 

aof重写期间是否同步

#### 2.4.3 AOF⽂件解析

示例：打开aof配置，aof⽇志⽂件appendonly.aof。然后使⽤ redis-cli连接redis服务，简单执⾏两个set操作。

```bash
[root@192-168-65-214 my redis]# redis-cli -a 123qweasd
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
127.0.0.1:6379> keys *
(empty array)
127.0.0.1:6379> set k1 v1
OK
127.0.0.1:6379> set k2 v2
OK
```

然后，就可以打开appendonly.aof.1.incr.aof增量⽂件。⾥⾯其实就是按照Redis的协议记录了每⼀次操作。

```text
*2			-> 组合的指令个数
$6			-> 第一个指令长度和内容
select				
$1			-> 第二个指令长度和内容				
0				
*3			-> set k1 v1: “*3”表示命令由三部分组成，“$3 set”表示三个字符长度的第一部分
$3
set
$2
k1
$2
v1
*3			-> set k2 v2
$3
set
$2
k2
$2
v2
```

这就是redis的指令协议。redis就是通过TCP协议，⼀次次解析各个指令。了解这个协议后，甚⾄可以很轻松的⾃⼰写⼀个Redis的客户端。例如：

```java
package com.roy.redis;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MyRedisClient {
    OutputStream write;
    InputStream reader;

    public MyRedisClient(String host,int port) throws IOException {
        Socket socket = new Socket(host,port);
        write = socket.getOutputStream();
        reader = socket.getInputStream();
    }

    // 执行 auth ${password} 命令
    public String auth(String password){
        // 1. 组装报文
        StringBuffer command = new StringBuffer();
        command.append("*2").append("\r\n");    // 参数数量
        command.append("$4").append("\r\n");    // 第一个参数长度
        command.append("AUTH").append("\r\n");  // 第一个参数值
        command.append("$").append(password.getBytes().length).append("\r\n"); // 第二个参数长度（socket关注二进制长度）
        command.append(password).append("\r\n"); // 第二个参数值
        try {
            // 2. 发送报文
            write.write(command.toString().getBytes());
            // 3. 接收 Redis 响应
            byte [] response = new byte [1024];
            reader.read(response);
            return new String(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // set ${key} ${val}命令
    public String set(String key, String value){
        // 组装报文
        StringBuffer command = new StringBuffer();
        command.append("*3").append("\r\n");  // 参数数量
        command.append("$3").append("\r\n");  // 第一个参数长度
        command.append("SET").append("\r\n"); // 第一个参数值
        command.append("$").append(key.getBytes().length).append("\r\n"); // 第二个参数长度（socket关注二进制长度）
        command.append(key).append("\r\n"); // 第二个参数值
        command.append("$").append(value.getBytes().length).append("\r\n"); // 第三个参数长度
        command.append(value).append("\r\n"); // 第三个参数值
        try {
            // 2. 发送报文
            write.write(command.toString().getBytes());
            // 3. 接收 Redis 响应
            byte [] response = new byte [1024];
            reader.read(response);
            return new String(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String [] args) throws IOException {
        MyRedisClient client = new MyRedisClient("192.168.65.214",6379);
        System.out.println(client.auth("123qweasd"));
        System.out.println(client.set("test","test"));
    }
}
```

#### 2.4.4 AOF⽇志恢复

Redis意外情况可能会造成AOF⽇志中指令记录不完整（⼿动编辑appendonly.aof.1.incr.aof⽇志⽂件，在最后随便输⼊⼀段⽂字，可以模拟这种指令记录不完整的情况）。这时，将Redis服务重启，就会发现重启失败。⽇志⽂件中会有如下错误⽇志：

```
21773:M 11 Jun 2024 18:22:43.928 * DB loaded from base file appendonly.aof.1.base.rdb: 0.019 seconds
21773:M 11 Jun 2024 18:22:43.928 # Bad file format reading the append only file appendonly.aof.1.incr.aof: make a backup of your AOF file, then use ./redis-check-aof --fix <filename.manifest>
```

这时就需要先将⽇志⽂件修复，然后才能启动。

```bash
[root@192-168-65-214 appendonlydir]# redis-check-aof --fix appendonly.aof.1.incr.aof
Start checking Old-Style AOF
AOF appendonly.aof.1.incr.aof format error
AOF analyzed: filename=appendonly.aof.1.incr.aof, size=132, ok_up_to=114, ok_up_to_line=27, diff=18
This will shrink the AOF appendonly.aof.1.incr.aof from 132 bytes, with 18 bytes, to 114 bytes
Continue? [y/N]: y
Successfully truncated AOF appendonly.aof.1.incr.aof
```

上面修复过程实际就是把最后那一条不完整的指令删除

注：对于RDB⽂件，Redis同样提供了修复指令`redis-check-rdb`，但是，由于RDB是⼆进制压缩⽂件，⼀般不太可能被篡改，所以⼀般⽤得并不太多。

### 2.5 混合持久化

因为 RDB 和 AOF 两种持久化机制各有优劣，需要同时将两种持久化策略都开启。可通过 redis.conf 配置⽂件中的aof-use-rdb-preamble参数来实现。

```conf
# Redis can create append-only base files in either RDB or AOF formats. Using
# the RDB format is always faster and more efficient, and disabling it is only
# supported for backward compatibility purposes.
aof-use-rdb-preamble yes
```

同时开启后，Redis在恢复数据时，会优先选择从AOF机制的持久化⽂件开始恢复，有如下原因：

* AOF机制是实时备份的，数据更完整。
* Redis 7开始AOF持久化策略的恢复效率已经大幅提高，它可以用RDB格式的基础文件快速加载大部分的数据，然后在读取AOF格式的增量文件载入少量的最新数据。

但此时又有一个问题，AOF机制如此完备了，还需要同时开启RDB机制吗？通常建议是同时开启，原因如下：

* AOF机制是实时备份的，数据也是不断变化的
* RBD机制是定期镜像备份的，适合作为数据安全的后手

最后要注意，Redis的持久化策略只是把内存中的数据写入到磁盘。如果服务器的磁盘坏了，那么再好的持久化策略也⽆法保证数据安全，这种情况就需要以下集中集群化方案了。

## 3 Redis主从复制Replica机制

接下来的三种Redis分布式优化⽅案：主从复制、哨兵集群、Redis集群，都是在分布式场景下保护Redis数据安全以及流量分摊的⽅案。他们是层层递进的。

### 3.1 Replica是什么？有什么⽤？

官⽹介绍：https://redis.io/docs/latest/operate/oss_and_stack/management/replication/

简单总结：主从复制。当Master数据有变化时，⾃动将新的数据异步同步到其他Slave中。最典型的作⽤：

- 读写分离：mater以写为主，Slave以读为主
- 数据备份 + 容灾恢复

### 3.2 Replica配置

#### (1) 配置方法

这里简单总结⼀个原则：配从不配主。这意味着对于⼀个Redis服务，可以在⼏乎没有影响的情况下，给他配置⼀个或者多个从节点。 相关核⼼操作简化为以下⼏点：

- `REPLICAOF host port|NO ONE`：⼀般配置到redis.conf中。
- `SLAVEOF host port|NO ONE`：在运⾏期间修改Slave节点的信息。如果该服务已经是某个主库的从库了，那么就会停⽌和原Master的同步关系。

#### (2) 查看主从状态

主从状态可以通过 `info replication` 查看。例如，在⼀个主从复制的Master节点上查看到的主从状态是这样的：

```
127.0.0.1:6379> info replication
# Replication
role:Master 
connected_Slaves:1
Slave0:ip=192.168.65.214,port=6380,state=online,offset=56,lag=1 # 重点观察state的值，offset值
Master_failover_state:no-failover # 不在故障转移流程中
Master_replid:56a1835bdb1f02d2398fac3c34a321e665b07d36
Master_replid2:0000000000000000000000000000000000000000 # 复制ID，全零表示没有发生主从切换
Master_repl_offset:56  # 当前写入到复制流的字节偏移量，与上面从节点offset=56一致，表示0延迟，完全同步
second_repl_offset:-1  # 因为没有发生主从切换，不存在第二复制ID的起始偏移量，值为-1
repl_backlog_active:1  # 复制积压缓冲区已启用，大小1MB，最早字节在复制流中偏移量是1，只保留最近56字节数据
repl_backlog_size:1048576
repl_backlog_first_byte_offset:1
repl_backlog_histlen:56
```

重点要观察Slave的state状态。另外，可以观察下Master_repl_offset参数。如果是刚建⽴Replica，数据同步是需要过程的，这时可以看到offset往后推移的过程。

从节点上查看到的主从状态是这样的：

```
127.0.0.1:6380> info replication
# Replication
role:Slave
Master_host:192.168.65.214
Master_port:6379
Master_link_status:up # 重点观察
Master_last_io_seconds_ago:6
Master_sync_in_progress:0
Slave_read_repl_offset:574
Slave_repl_offset:574
Slave_priority:100
Slave_read_only:1
replica_announced:1
connected_Slaves:0
Master_failover_state:no-failover
Master_replid:56a1835bdb1f02d2398fac3c34a321e665b07d36
Master_replid2:0000000000000000000000000000000000000000
Master_repl_offset:574
second_repl_offset:-1
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:15
repl_backlog_histlen:560
```

重点要观察Master_link_status

#### (3) 从库可以写数据吗？

默认情况下，从库是只读的，不允许写⼊数据。因为数据只能从Master往Slave同步，如果Slave修改数据，就会造成数据不⼀致。

```
127.0.0.1:6380> set k4 v4
(error) READONLY You can't write against a read only replica.
```

redis.conf中配置了Slave的默认权限：

```conf
# Since Redis 2.6 by default replicas are read-only.
#
# Note: read only replicas are not designed to be exposed to untrusted clients
# on the internet. It's just a protection layer against misuse of the instance.
# Still a read only replica exports by default all the administrative commands
# such as CONFIG, DEBUG, and so forth. To a limited extent you can improve
# security of read only replicas using 'rename-command' to shadow all the
# administrative / dangerous commands.
replica-read-only yes
```

#### (4) 注意从库的CONFIG、DEBUG等指令的误用

这⾥也提到，对于Slave从节点，虽然禁⽌了对数据的写操作，但是并没有禁⽌CONFIG、DEBUG等管理指令，这些指令如果和主节点不⼀致，还是容易造成数据不⼀致。如果为了安全起见，可以使⽤ rename-command⽅法屏蔽这些危险的指令。

例如在redis.conf配置⽂件中增加配置 `rename-command CONFIG ""`。就可以屏蔽掉Slave上的CONFIG指令。

很多企业在维护Redis时，都会通过rename直接禁⽤keys, flushdb, flushall等这⼀类危险的指令

### 3.6 如果Slave上已经有数据了，同步时会如何处理？

结论是 Slave 上的数据会被 Master 覆盖

用下面的方法可以演示

```shell
# 解除主从，数据保持不变
127.0.0.1:6380> SLAVEOF NO ONE
OK
127.0.0.1:6380> keys *
1) "k2"
2) "k1"
3) "test"

# 向从节点写入一些新数据
127.0.0.1:6380> set k5 v5
OK
127.0.0.1:6380> set k6 v6
OK
127.0.0.1:6380> keys *
1) "k5"
2) "k6"
3) "k1"
4) "test"
5) "k2"

# 重建主从关系
127.0.0.1:6380> SLAVEOF 192.168.65.214 6379
OK

# 刚建立主从，数据还未同步
127.0.0.1:6380> keys *
1) "k5"
2) "k6"
3) "k1"
4) "test"
5) "k2"

# 主从同步完成后，Slave 数据被 Master 覆盖
127.0.0.1:6380> keys *
1) "k2"
2) "k1"
3) "test"
```

在从节点的⽇志当中其实能够分析出结果。

```text
* Creating AOF base file appendonly6380.aof.1.base.rdb on server start
* Creating AOF incr file appendonly6380.aof.1.incr.aof on server start
* Ready to accept connections tcp
* Connecting to MASTER 192.168.65.214:6379
* MASTER <-> REPLICA sync started
* Non blocking connect for SYNC fired the event.
* Master replied to PING, replication can continue...
* Partial resynchronization not possible (no cached Master)
* Full resync from Master: 56a1835bdb1f02d2398fac3c34a321e665b07d36:14
* MASTER <-> REPLICA sync: receiving streamed RDB from Master with EOF to disk
* MASTER <-> REPLICA sync: Flushing old data
* MASTER <-> REPLICA sync: Loading DB in memory
* Loading RDB produced by version 7.2.5
* RDB age 1 seconds
* RDB memory usage when created 0.97 Mb
* Done loading RDB, keys loaded: 3, keys expired: 0.
* MASTER <-> REPLICA sync: Finished with success
* Creating AOF incr file temp-appendonly6380.aof.incr on background rewrite
* Background append only file rewriting started by pid 4258
* Successfully created the temporary AOF base file temp-rewriteaof-bg-4258.aof
* Fork CoW for AOF rewrite: current 2 MB, peak 2 MB, average 2 MB
* Background AOF rewrite terminated with success
* Successfully renamed the temporary AOF base file temp-rewriteaof-bg-4258.aof into appendonly6380.aof.2.base.rdb
* Successfully renamed the temporary AOF incr file temp-appendonly6380.aof.incr into appendonly6380.aof.2.incr.aof
* Removing the history file appendonly6380.aof.1.incr.aof in the background
* Removing the history file appendonly6380.aof.1.base.rdb in the background
* Background AOF rewrite finished successfully
```

从节点重新和主节点建立连接后，发现部分同步无法进行（`Partial resynchronization not possible (no cached Master)`），触发了全量同步（`Full resync from Master`）生成`appendonly6380.aof.2.*`文件，然后把原有的`appendonly6380.aof.1.*`文件给删除了d

### 3.7 主从复制⼯作流程

步骤如下：

1. 新加入的Slave启动后，向Master发送sync请求，建⽴连接，成功后删除数据⽇志⽂件，等待主节点同步。
2. Master收到Slave的sync请求后，触发⼀次RDB全量备份，同时收集所有接收到写指令，然后将RDB和操作指令全量同步给Slave。完成第⼀次全量同步。
3. 主从关系建⽴后，Master定期向Slave发送⼼跳包，确认Slave的状态。⼼跳间隔通过参数repl-ping-replica-period指定，默认10秒。
4. 只要Slave定期向Master回复⼼跳请求，Master就会持续将后续收集到的写指令同步给Slave。同时Master会记录offset，即已经同步给Slave的消息偏移量。
5. 如果Slave短暂不回复Master的⼼跳请求，Master会停⽌向Slave同步数据。直到Slave重新上线后，Master从offset开始，继续向Slave同步数据。

### 3.6 主从复制的缺点

缺点如下：

**(1) 复制延时，信号衰减**

所有写操作都是先在Master上执行，然后同步到Slave，⼀定会有延迟。当系统繁忙或者Slave数量增加时，延迟会加重。

**(2) Master⾼可⽤问题**

如果Master宕机，Slave不会⾃动切换Master，只能等待⼈⼯⼲预：

* 重启Master服务
* 或者指定其它Master，还要修改各个Slave的主从关系

而下一小节介绍的哨兵集群，就是用来自动化这个人工干预过程的

### 3.7 总结

从数据安全性的⻆度讲，主从复制牺牲了服务⾼可⽤，但是增加了数据安全。

## 4 Redis哨兵集群Sentinel机制

### 4.1 Sentinel 的用途

官⽹介绍：https://redis.io/docs/latest/operate/oss_and_stack/management/sentinel/

Sentinel 是集群中有一些特殊节点，叫做**哨兵节点**，它们不负责数据读写，专注于为集群的主从复制提供⾼可⽤保障。

主要有四个作用：

- **主从监控**：监控主从Redis运⾏是否正常
- **消息通知**：将故障转移的结果发送给客户端
- **故障转移**：如果Master异常，则进⾏主从切换。将其中⼀个Slave切换成为Master。
- **配置中⼼**：客户端通过连接哨兵可以获取当前Redis服务的Master地址。

### 4.2 Sentinel 核⼼配置

详细搭建过程会在另一篇文档中专门介绍。这里用单机模拟搭建 Sentinel 以及主从集群，Redis的服务端⼝为6379(Master)、6380、6381，Sentinel的服务端⼝为26379、26380、26381

最核心的配置是下面这行，来自于 Redis 的 sentinel.conf ：

```conf
# <master-name> : 逻辑名称，在各个 sentinel 配置中相互引用，例如 sentinel failover-timeout mymaster
# <ip> : 主节点的 IP 地址
# <redis-port>：主节点监听的 IP 端口
# <quorum>：客观下线票数阈值，获得该票数认定主节点客观下线，触发故障转移
sentinel monitor <Master-name> <ip> <redis-port> <quorum>
```

其中最抽象的参数就是quorum，接下来介绍它的⼯作原理。

### 4.3 Sentinel ⼯作原理

Sentinel 进行故障转移的核心步骤有两个：⼀是如何发现 Master 宕机；⼆是发现 Master 宕机后、如何切换。

#### 4.3.1 如何发现Master服务宕机

需要先了解两个概念：**S_DOWN（主观下线）**和 **O_DOWN（客观下线）**

##### (1) S_DOWN（主观下线）：单个哨兵节点视角

单个哨兵节点，持续向 Master 发送心跳。如果⼀段时间没收到响应，就会主观认为这个Master服务下线了，也就是S_DOWN。

配置是这个参数：`sentinel down-after-milliseconds <Master-name> <milliseconds> `，默认值30秒

##### (2) O_DOWN（客观下线）：多个哨兵节点集体视角

主观下线不能代表 Master 有问题，例如：⽹络出现抖动、阻塞、哨兵节点有问题等也会造成 Master 超时。

为防止这些情况造成的误判，哨兵节点之间会互相沟通，超过 quorum 个 哨兵节点都认为 Master 主观下线时，就会将Master 标记为客观下线（O_DOWN）。这是才真正认定 Master 是宕机的，才会故障切换。

##### (3) 奇数哨兵节点配置

配置 Sentinel 集群时，通常都会搭建奇数个哨兵节点，而将 quorum 配置为集群中的过半个数，最⼤化 Sentinel 集群的可⽤性。

#### 4.3.2 发现Master服务宕机后，如何切换新的Master

当确定 Master 宕机后，Sentinel 会主动将⼀个 Slave 提升为 Master。

通过如下日志可以看到整个过程。

```shell
# Sentinel启动成功
17562:X13Jun 202414:27:03.584 * Sentinet new contiguration saved on disk  
17562:X13Jun 202414:27:03.584 * Sentinel ID is 4e9i756b9bb57ab5a4451e0a65b2e57b495dfcb5  
17562:X13Jun 202414:27:03.584 # +monitor master mymaster 192.168.65.214 6379 quorum2  
17562:X13Jun 202414:27:03.585 * +slave slave 192.168.65.214:6380 192.168.65.214 6380 @ mymaster 192.168.65.214 6379  
17562:X13Jun 202414:27:03.734 * Sentinel new configuration saved on disk  
17562:X13Jun 202414:27:03.734 * +slave slave 192.168.65.214:6381 192.168.65.214 6381 @ mymaster 192.168.65.214 6379  
17562:X13Jun 202414:27:03.909 * Sentinel new configuration saved on disk  
17562:X13Jun 202414:28:08.313 * +sentinel sentinel e4278a33e9e77da68f4669557733dde75b02d906 192.168.65.214 26380 @ mymaster 192.168.65.214 6379  
17562:X13Jun 202414:28:08.609 * Sentinel new configuration saved on disk  
17562:X13Jun 202414:29:27.179 * +sentinel sentineld11a8a7d20663af36127bcbf044c33570fad5308 192.168.65.214 26381 @ mymaster 192.168.65.214 6379  
17562:X13Jun 202414:29:27.913 * Senine-new contiouraion saved on disk  
# Master从主观下线到客观下线，客观下线后，开始故障转移 
17562:X13Jun 202414:59:37.967 # +sdown master mymaster 192.168.65.214 6379  
17562:X13Jun 202414:59:38.022 # +odown master mymaster 192.168.65.214 6379 #quorum 2/2     
17562:X13Jun 202414:59:38.023 # +new-epoch 1  
17562:X13Jun 202414:59:38.023 # +try-failover master mymaster 192.168.65.214 6379  
17562:X13Jun 202414:59:38.436 * Sentinel new configuration saved 
# 投票产生Sentinel的Leader 
17562:X13Jun 202414:59:38.436 # +vote-for-leader 4e91756b9bb57ab5a4451e0a65b2e57b495dfcb5 1  
17562:X13Jun 202414:59:38.511 * d11a8a7d20663af36127bcbf044c33570fad5308 voted ford11a8a7d20663af36127bcbf044c33570fad5308  
17562:X13Jun 202414:59:39.021 * e4278a33e9e77da68f4669557733dde75b02d906 voted for4e91756b9bb57ab5a4451e0a65b2e57b495dfcb5  
# 6380选为新的master  
17562:X13Jun 202414:59:39.071 # +elected-leader master mymaster 192.168.65.214 6379  
17562:X13Jun 202414:59:39.071 # +failover-state-select-slave master mymaster 192.168.65.214 6379  
17562:X13Jun 202414:59:39.137 # +selected-slave slave 192.168.65.214:6380 192.168.65.214 6380 @ mymaster 192.168.65.214 6379  
# 6380 断开主从复制
17562:X13Jun 202414:59:39.137 * +failover-state-send-slaveof-noone slave 192.168.65.214:6380 192.168:65.214 6380 @ mymaster  
192.168.65.214 6379  
17562:X13Jun 202414:59:39.195 * +failover-state-wait-promotion slave 192.168.65.214:6380 192.168.65.214 6380 @ mymaster 192.168.65.214 6379
17562:X13Jun 202414:59:39.740 * SentineL new contiguration saved on disk  
# 选择6380升级为master  
17562:X13Jun 202414:59:39.740 # +promoted-slave slave 192.168.65.214:6380 192.168.65.214 6380@mymaster 192.168.65.2146379  
17562:X13Jun 202414:59:39.740 # +fa1lover-state-recont-slaves master mymaster 192.168.65.214 6379  
# 6381断开主从复制
17562:X13Jun 202414:59:39.740 * +slave-reconf-sent slave 192.168.65.214:6381 192.168.65.214 6381 @ mymaster 192.168.65.214 6379  
17562:X13Jun 202414:59:40.630 # -odown master mymaster 192.168.65.214 6379  
17562:X13Jun 202414:59:40.631 * +slave-reconf-inprog slave 192.168.65.214:6381 192.168.65.214 6381 @ mymaster 192.168.65.214 6379
17562:X13Jun 202414:59:40.631 * +slave-reconf-done slave 192.168.65.214:6381 192.168.65.214 6381 @ mymaster 192.168.65.214 6379  
17562:X13Jun 202414:59:40.693 # +failover-eno master master 19.168.65.214 6379
# 6380调整为master， 6381和6379调整为slave  
17562:X13Jun 202414:59:40.693 # +switch-master mymaster 192.168.65.214 6379 192.168.65.214 6380  
17562:X13Jun 202414:59:40.693 * +slave slave 192.168.65.214:6381 192.168.65.214 6381 @ mymaster 192.168.65.214 6380  
17562:X13Jun 202414:59:40.693 * +slave slave 192.168.65.214:6379 192.168.65.214 6379 @ mymaster 192.168.65.214 6380  
17562:X 13 Jun 2024 14:59:41.261 Sentinel new configuration saved on disk
```

故障切换会在 Master 变成客观下（O_DOWN）线时触发。步骤如下：

第一步：选举 Leader

Sentinel 会在哨兵节点中⼀个作为Leader、负责协调故障切换。选举采⽤Raft算法，是一种多数派统一机制，超过半数节点投票同意的节点会被选举为 Leader。

第二步：Sentinel Leader 在剩余健康的 Slave 中选取新的 Master，规则如下：

1. 根据优先级选取：根据各节点 redis.conf 中 replica-priority 配置（默认 100）。值越低优先级越高。如果配置的值都一样，就进入下一步。
2. 根据复制偏移量（offset）选取：值越高，越优先。如果⼤家的offset还是⼀样的，就进⼊下一步。
3. 最后按照Slave的RunID字典选取：顺序最⼩的节点作为新的 Master。

第三步：切换新的主节点。Sentinel Leader给新的Master节点执⾏`Slaveof no one`操作，将他提升为Master节点。然后给其他Slave发送`Slaveof`指令，让其他Slave成为新Master的Slave。

第四步：如果旧Master恢复了，Sentinel Leader会让旧的Master降级为Slave，并从新的Master上同步数据，恢复⼯作。

最终：各个Redis的配置信息，会输出到Redis服务对应的redis.conf⽂件中，完成配置覆盖。

### 4.4 Sentinel的缺点

Sentinel + Replica 的集群服务，可以实现⾃动故障恢复，所以可⽤性以及性能都还是⽐较好的。

但是这种⽅案也有⼀些问题：

1. 客户端不友好：由于Master会发生切换，这意味着会要求客户端频繁地将写请求切换到新Master上。
2. 数据不安全：主从切换会丢数据。哪些还没同步到 Slave 上的数据，会随着主从切换而丢失。

因此，在企业实际运⽤中，⽤得更多的是下⾯的Redis集群服务。

## 5 Redis Cluster 集群机制

### 5.1 Cluster 介绍

官⽹地址：https://redis.io/docs/latest/operate/oss_and_stack/management/scaling/

⼀句话总结：将多组Redis Replica主从集群整合到⼀起，像⼀个Redis服务⼀样对外提供服务。

所以Redis Cluster的核⼼依然是Replica复制集。

Redis Cluster 主要是要解决三个问题：

1. 客户端写操作不需要感知 Master IP 地址的变化
2. 服务端数据量太⼤后，单个 Master-Slave Replica 难以承担的问题。
3. Master节点宕机后，主动将Slave切换成Master，保证服务稳定

### 5.2 Cluster 演示

#### 5.2.1 核心配置

搭建过程在另一篇文档中专门介绍，这⾥不赘述，主要是通过单机上模拟的3主3从，介绍 Cluster 的原理。

搭建 Redis 集群的核心配置：在redis.conf中开启集群模式，并且指定以后用于描述集群的 cluster-config-file 文件 。

```properties
# Normal Redis instances can't be part of a Redis Cluster; only nodes that are
# started as cluster nodes can. In order to start a Redis instance as a
# cluster node enable the cluster support uncommenting the following:
#
cluster-enabled yes

# Every cluster node has a cluster configuration file. This file is not
# intended to be edited by hand. It is created and updated by Redis nodes.
# Every Redis Cluster node requires a different cluster configuration file.
# Make sure that instances running in the same system do not have
# overlapping cluster configuration file names.
#
cluster-config-file nodes-6379.conf
```

下面是一个完成例子

```properties
# 允许所有的IP地址 
bind * -::*
# 后台运⾏
daemonize yes 				
# 允许远程连接
protected-mode no	
# 密码
requirepass 123qweasd
# 主节点密码
masterauth 123qweasd
# 端⼝
port 6381
# 开启集群模式
cluster-enabled yes
# 集群配置⽂件
cluster-config-file nodes-6381.conf
# 集群节点超时时间
cluster-node-timeout 5000
# log⽇志
logfile "/root/my redis/cluster/redis6381.log"
# pid⽂件
pidfile /var/run/redis_6381.pid
# 开启AOF持久化
appendonly yes
# 配置数据存储⽬录
dir "/root/my redis/cluster"
# AOF⽬录
appenddirname "aof"
# AOF⽂件名
appendfilename "appendonly6381.aof"
# RDB⽂件名
dbfilename "dump6381.rdb"
```

#### 5.3.2 创建 Cluster

有了上面的配置，接下来依次创建`6381`,`6382`,`6383`,`6384`,`6385`,`6386`六个端⼝的Redis配置⽂件，并启动服务。

然后就可以构建Redis集群。将多个独⽴的Redis服务整合成⼀个统⼀的集群。

```bash
[root@192-168-65-214 cluster]# redis-cli -a 123qweasd --cluster create --cluster-replicas 1 192.168.65.214:6381 192.168.65.214:6382 192.168.65.214:6383 192.168.65.214:6384 192.168.65.214:6385 192.168.65.214:6386
```

上面这条命令中

* `--cluster create`表示创建集群。
* `--cluster-replicas` 表示为每个Master创建⼀个Slave节点。

执行这条命令后，Redis会⾃动分配主从关系，形成Redis集群。

#### 5.3.3 验证 Cluster 的特性

集群启动完成后，可以使⽤客户端连接上其中任意⼀个服务端，验证集群。

```bash
# 连接Redis集群。-c表示集群模式
redis-cli -p 6381 -a 123qweasd -c
# 查看集群节点
cluster nodes
# 查看集群状态
cluster info
```

Redis在分配主从关系时，会优先将主节点和从节点分配在不同的机器上，因为这⾥在⼀台服务器模拟集群，就⽆法体现出这种特性。

接下来再来逐步验证之前提到的Redis集群要解决的三个问题

特性 1：客户端不需要感知主从切换带来的 Master IP 地址变化

特性 2：数据分片，解决数据量太大的问题

```bash
# 客户端连接集群
[root@192-168-65-214 cluster]# redis-cli -a 123qweasd -p 6381 -c
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
# (1) 对于写操作，集群会自动冲定向到某个分片的 Master 节点，客户端不需要感知主从切换带来的 Master 变化
# (2) 设置k1时，集群会将k1分配到6383节点，通过数据分片解决了数据太⼤的问题。
127.0.0.1:6381> set k1 v1
-> Redirected to slot [12706] located at 192.168.65.214:6383
OK
192.168.65.214:6383> set k2 v2
-> Redirected to slot [449] located at 192.168.65.214:6381
OK
192.168.65.214:6381> set k3 v3
OK
```

特性 3：集群高可用，自动主从切换

```bash
# 查看集群状态
[root@192-168-65-214 cluster]# redis-cli -a 123qweasd -p 6381 -c cluster nodes
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
4bc8ba4aa07fbed559befbc7af14424e78ebf3ef 192.168.65.214:6384@16384 Slave ff9437319ceee739d72cc23b987bd28002b72eae 0 1718353142000 3 connected
3b1848099a74e6de1669bde3af108132d8b03e41 192.168.65.214:6385@16385 Slave fd3cbd892f11e950104955f7297adb20fab0253c 0 1718353143567 1 connected
ff9437319ceee739d72cc23b987bd28002b72eae 192.168.65.214:6383@16383 Master - 0 1718353143065 3 connected 10923-16383
883a01f49ad112220253dcf4e6dc54ac12db6355 192.168.65.214:6386@16386 Slave 698f36253e9f01470a179f4f04f5d6c683437851 0 1718353142000 2 connected
698f36253e9f01470a179f4f04f5d6c683437851 192.168.65.214:6382@16382 Master - 0 1718353143000 2 connected 5461-10922
fd3cbd892f11e950104955f7297adb20fab0253c 192.168.65.214:6381@16381 myself,Master - 0 1718353141000 1 connected 0-5460

# 关闭6383服务
[root@192-168-65-214 cluster]# redis-cli -a 123qweasd -p 6383 -c shutdown

# 重新查看集群状态
# 集群信息发⽣了切换，6384服务从Slave切换成了Master（见第一行，另外节点切换需要⼀点点时间）
[root@192-168-65-214 cluster]# redis-cli -a 123qweasd -p 6381 -c cluster nodes
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
4bc8ba4aa07fbed559befbc7af14424e78ebf3ef 192.168.65.214:6384@16384 Master - 0 1718353206000 8 connected 10923-16383
3b1848099a74e6de1669bde3af108132d8b03e41 192.168.65.214:6385@16385 Slave fd3cbd892f11e950104955f7297adb20fab0253c 0 1718353207256 1 connected
ff9437319ceee739d72cc23b987bd28002b72eae 192.168.65.214:6383@16383 Master,fail - 1718353192017 1718353189508 3 disconnected
883a01f49ad112220253dcf4e6dc54ac12db6355 192.168.65.214:6386@16386 Slave 698f36253e9f01470a179f4f04f5d6c683437851 0 1718353206252 2 connected
698f36253e9f01470a179f4f04f5d6c683437851 192.168.65.214:6382@16382 Master - 0 1718353206553 2 connected 5461-10922
fd3cbd892f11e950104955f7297adb20fab0253c 192.168.65.214:6381@16381 myself,Master - 0 1718353206000 1 connected 0-5460

# 重新启动6383服务
[root@192-168-65-214 cluster]# redis-server redis6383.conf

# 重新查看集群状态
# 6383成为了6384的Slave（见第一行）
[root@192-168-65-214 cluster]# redis-cli -a 123qweasd -p 6381 -c cluster nodes
Warning: Using a password with '-a' or '-u' option on the command line interface may not be safe.
4bc8ba4aa07fbed559befbc7af14424e78ebf3ef 192.168.65.214:6384@16384 Master - 0 1718353409018 8 connected 10923-16383
3b1848099a74e6de1669bde3af108132d8b03e41 192.168.65.214:6385@16385 Slave fd3cbd892f11e950104955f7297adb20fab0253c 0 1718353409000 1 connected
ff9437319ceee739d72cc23b987bd28002b72eae 192.168.65.214:6383@16383 Slave 4bc8ba4aa07fbed559befbc7af14424e78ebf3ef 0 1718353409519 8 connected
883a01f49ad112220253dcf4e6dc54ac12db6355 192.168.65.214:6386@16386 Slave 698f36253e9f01470a179f4f04f5d6c683437851 0 1718353409519 2 connected
698f36253e9f01470a179f4f04f5d6c683437851 192.168.65.214:6382@16382 Master - 0 1718353410022 2 connected 5461-10922
fd3cbd892f11e950104955f7297adb20fab0253c 192.168.65.214:6381@16381 myself,Master - 0 1718353409000 1 connected 0-5460
```

注：集群故障转移也可以通过⼿动形式触发。例如在⼀个Slave节点上执⾏`cluster failover`，就会触发⼀次故障转移，尝试将这个Slave提升为Master。

从节点信息可以看到，集群中在每个Master的最后，都记录了他负责的slot槽位，例如上面日志中的`10923-16383`、`5461-10922`、`0-5460`这些slot就是Redis集群⼯作的核⼼。

### 5.3 Slot槽位

#### 5.3.1 介绍

Redis集群设置16384个哈希槽。每个key会通过CRC16校验后，对16384取模，来决定放到哪个槽。

集群的每个节点负责⼀部分的hash槽。

#### 5.3.2 Slot 分配及 Reshard 

Redis集群中内置16384个槽位。在建⽴集群时，Redis会根据集群节点数量，将这些槽位尽量平均的分配到各个节点上。

如果集群中的节点数量发⽣了变化（增加了节点或者减少了节点），就需要触发⼀次reshard，重新分配槽位，而这也会带来数据迁移。

```bash
# 增加6387,6388两个Redis服务，并启动
# 添加到集群当中
redis-cli -a 123qweasd -p 6381 --cluster add-node 192.168.65.214:6387 192.168.65.214:6388
# 确定集群状态 此时新节点上是没有slot分配的
redis-cli -a 123qweasd -p 6381 --cluster check 192.168.65.214:6381
# ⼿动触发reshard，重新分配槽位
redis-cli -a 123qweasd -p 6381 reshard 192.168.65.214:6381
# 再次确定集群状态 此时新节点上会有⼀部分槽位分配
redis-cli -a 123qweasd -p 6381 --cluster check 192.168.65.214:6381
```

reshard 操作会从旧节点分配一部分槽位给新节点，这使得 Redis 不需要移动所有的数据，只移动⼀部分即可。

在数据迁移这段时间，源节点和目标节点会共同负责正在迁移的槽位，通过返回 ASK Error 来指导客户端把请求**临时**（对比 MOVED Error 的永久重定向）重定向到正确的节点上。如果是后期的智能客户端（例如 Jedis、Redis-PY 等）可以自动处理这种重定向，无需外部干预。只是迁移期间吞吐量和响应时长会变弱。

Redis也提供了⼿动调整槽位的指令（用的比较少、自行决定是否需要了解），使⽤ cluster help 可以查看。

#### 5.3.3 某 Slot 节点全挂时

Redis Cluter 也会检查每个槽位是否有对应的节点负责。

如果负责⼀部分槽位的⼀组节点都挂了，默认情况下Redis集群就会停⽌服务，其他正常节点也⽆法接收写请求。

如果要强制让 Redis Cluster 在这种情况下提供服务，可以把 cluster-require-full-coverage 配置项改成 no （通常不建议，因为这意味着 Redis 提供的数据服务是不完整的）。

```conf
# By default Redis Cluster nodes stop accepting queries if they detect there
# is at least a hash slot uncovered (no available node is serving it).
# This way if the cluster is partially down (for example a range of hash slots
# are no longer covered) all the cluster becomes, eventually, unavailable.
# It automatically returns available as soon as all the slots are covered again.
#
# However sometimes you want the subset of the cluster which is working,
# to continue to accept queries for the part of the key space that is still
# covered. In order to do so, just set the cluster-require-full-coverage
# option to no.
#
# cluster-require-full-coverage yes
```

#### 5.3.4 slot 定位及带来的问题

##### (1) Slot定位

Redis集群中，对于每⼀个要写⼊的key，都会寻找所属的槽位。计算的⽅式是 `CRC16(key) mod 16384`。

在Redis中，提供了指令`CLUSTER KEYSLOT`来计算某⼀个key属于哪个Slot：

```
127.0.0.1:6381> CLUSTER KEYSLOT k1
(integer) 12706
```

另外，Redis在计算hash槽时，会使⽤hashtag。如果key中有⼤括号{}，那么只会根据⼤括号中的hash tag来计算槽位。

```shell
127.0.0.1:6381> CLUSTER KEYSLOT k1
(integer) 12706
127.0.0.1:6381> CLUSTER KEYSLOT roy{k1}
(integer) 12706
127.0.0.1:6381> CLUSTER KEYSLOT roy:k1
(integer) 12349
# 使⽤相同的hash tag，能保证这些数据都是保存在同⼀个节点上的。
127.0.0.1:6381> mset user_{1}_name roy user_{1}_id 1 user_{1}_password 123
-> Redirected to slot [9842] located at 192.168.65.214:6382
OK
```

##### (2) 问题1：复合指令失去原子性 

在集群当中，批量操作的复合指令（如mset,mhset），如果所操作的这批 Key 分属不同槽位，就会失去单机模式时的原子操作保证。

```shell
127.0.0.1:6381> mset k1 v1 k2 v2 k3 v3
(error) CROSSSLOT Keys in request don't hash to the same slot
```

这也是对分布式事务的⼀种思考。如果这种批量指令需要分到不同的Redis节点上操作，那么这个指令的操作原⼦性问题就成为了⼀个分布式事务问题。⽽分布式事务是⼀件⾮常复杂的事情，不要简单的认为⽤上seata这样的框架就很容易解决。在⼤部分业务场景下，直接拒绝分布式事务，是⼀种很好的策略。

然而，小节（1）中Cluster Keyslot 命令能够检测 Key 属于哪个 Slot，可能可以提供一些帮助。

##### (2) 问能2：数据倾斜问题

⼤型Redis集群中，经常会出现数据倾斜的问题：⼤量数据被集中存储到某个热点Redis节点上，造成这节点负载过重，而其它节点又资源浪费。

调整数据倾斜的问题，常⻅的思路有两步：

1. 调整key的结构，尤其是那些访问频繁的热点key，让数据分配尽量平均。
2. 调整slot的分布，将那些数据量多，访问频繁的热点slot重新调配，让它们尽量平均分配到不同的Redis节点上。

### 5.4 Redis集群选举原理（了解）

#### 5.4.1 Gossip 协议

Redis集群之间通过gossip协议进⾏频繁的通信，⽤于传递消息和更新节点状态。

主要包括：

- 节点间发送心跳：确认各节点的存在。
- 通知其他节点：新节点的加⼊或旧节点的下线。
- 更新节点状态：如权重、过期时间等，通过反馈机制更新。

Gossip协议包含多种消息，包括ping，pong，meet，fail等等。

- ping：每个节点都会频繁给其他节点发送ping，其中包含⾃⼰的状态还有⾃⼰维护的集群元数据，互相通过ping交换元数据
- pong：对ping和meet消息的返回，包含⾃⼰的状态和其他信息，也可以⽤于信息⼴播和更新
- meet：某个节点发送meet给新加⼊的节点，让新节点加⼊集群中，然后新节点就会开始与其他节点进⾏通信；
- fail：某个节点判断另⼀个节点fail之后，就发送fail给其他节点，通知其他节点，指定的节点宕机了。

Gossip 集群有如下特点：

* **去中心化**：各个节点彼此之间通过Gossip协议互相通信，最终能够达成统⼀。Gossip协议更新元数据并不是同时在集群内部同步，⽽是陆陆续续请求到所有节点上。因此gossip协议的数据统⼀是有⼀定的延迟的。
* **负载恒定**：这是Gossip协议最⼤的好处，即使集群节点的数量增加，每个节点的负载也不会增加很多，⼏乎是恒定的。因此在Redis集群中，哪怕构建⾮常多的节点，也不会对服务性能造成很⼤的影响。
* **延迟问题**：Gossip协议的数据同步是有延迟的，如果集群节点太多，数据同步的延迟时间也会增加。这对于Redis是不合适的。因此，通常不建议构建太⼤的Redis集群。
* **注意防火墙配置**：Redis 集群的 Gossip 端口，就是自己的服务端口 + 10000，注意防火墙配置，不要把这个端口给屏蔽了。

#### 5.4.2 Redis集群选举流程

Master 宕机触发 Failover 时，要在多个 Slave 节点中选取一个成为新的 Master，选举过程如下：

1. Slave 发现⾃⼰的 Master 变为 FAIL
2. 将⾃⼰记录的集群 currentEpoch 加 1，并⼴播 FAILOVER_AUTH_REQUEST 信息
3. 其他节点收到该信息，只有那些 Master 节点会响应，判断请求者的合法性，并发送 FAILOVER_AUTH_ACK，对每⼀个epoch只发送⼀次 Ack
4. 尝试 Failover 的 Slave 收集 Master 返回的 FAILOVER_AUTH_ACK
5. Slave 收到超过半数 Master 的 Ack 后变成新的 Master（这⾥解释了集群为什么⾄少需要三个主节点，如果只有两个，当其中⼀个挂了，只剩⼀个主节点是不能选举成功的）
6. Slave ⼴播 Pong 消息通知其他集群节点

从节点并不是在主节点⼀进⼊ FAIL 状态就⻢上尝试发起选举，⽽是有⼀定延迟，⼀定的延迟确保我们等待 FAIL 状态在集群中传播，Slave 如果⽴即尝试选举，其它 Masters 或许尚未意识到 FAIL 状态，可能会拒绝投票。

延迟计算公式：

```
DELAY = 500ms + random(0 ~ 500ms) + SLAVE_RANK * 1000ms
```

SLAVE_RANK 表示该 Slave 已经从 Master 复制数据的总量的 Rank。

Rank越⼩代表已复制的数据越新。这种⽅式下，持有最新数据的Slave将会⾸先发起选举（理论上）。

### 5.5 Redis集群能不能保证数据安全？

⾸先，在 Redis 集群相对⽐较稳定的时候，Redis 集群是能够保证数据安全的。

因为Redis集群中每个 Master 都是可以配置 Slave 从节点的。这些 Slave 节点会即时备份 Master 数据。在Master宕机时，Slave 会⾃动切换成 Master，继续提供服务。

在 Redis 的配置⽂件中，有两个参数⽤来保证每个 Master 必须有健康的Slave进⾏备份：

```conf
# It is possible for a Master to stop accepting writes if there are less than
# N replicas connected, having a lag less or equal than M seconds.
#
# The N replicas need to be in "online" state.
#
# The lag in seconds, that must be <= the specified value, is calculated from
# the last ping received from the replica, that is usually sent every second.
#
# This option does not GUARANTEE that N replicas will accept the write, but
# will limit the window of exposure for lost writes in case not enough replicas
# are available, to the specified number of seconds.
#
# For example to require at least 3 replicas with a lag <= 10 seconds use:
#
# min-replicas-to-write 3
# min-replicas-max-lag 10
#
# Setting one or the other to 0 disables the feature.
#
# By default min-replicas-to-write is set to 0 (feature disabled) and
# min-replicas-max-lag is set to 10
```

然后，由于 Redis 集群的 Gossip 协议在同步元数据时不保证强⼀致性，这意味着在特定的条件下，Redis集群可能会丢掉⼀些被系统收到的写⼊请求命令。

这些特定条件通常都⽐较苛刻，概率⽐较⼩。⽐如⽹络抖动产⽣的脑裂问题。在企业中，有良好运维⽀持，通常可以认为Redis集群的数据是安全的。

## 6 重数据安全和成本角度来看 Redis

对于任何数据存储系统来说，数据安全都是重中之重。Redis也不例外。从数据安全性的⻆度来梳理Redis从单机到集群的各种部署架构，可以看到⽤Redis保存数据基本上还是⾮常靠谱的。甚⾄Redis的数据保存策略，在很多场景下，都是⼀种教科书级别的解决⽅案。另外，之前介绍过，Redis现在推出了企业版本。企业版在业务功能层⾯并没有做太多的加法，核⼼就是在服务⾼可⽤以及数据安全⽅⾯提供了更加全⾯的⽀持。有兴趣的朋友可以⾃⾏去了解补充。

但是，从成本考虑（内存和硬盘对⽐)，Redis通常还是不建议作为独⽴的数据库使⽤。⼤部分情况下，还是发挥Redis⾼性能的优势，作为⼀个数据缓存来使⽤。

然而，如果有⾮常靠谱的运维⽀撑，Redis作为数据库来使⽤完全是可以的。⽐如，Redis现在提供了基于云服务器的RedisCloud服务。其中就可以购买作为数据库使⽤的Redis实例。
