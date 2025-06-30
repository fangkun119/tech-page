---
dtitle: Redis参考05：开发规范和性能优化
author: fangkun119
date: 2025-05-11 12:00:00 +0800
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
## 1 键值设计

### 1.1 key 名设计

#### (1) 可读性和可管理性

业务名（或数据库名）为前缀（防止 key 冲突），用冒号分隔，比如业务名:表名:id

例如：`trade:order:1`

#### (2) 简洁性

保证语义前提下控制 key 的长度，key 较多时内存占用也不容忽视

例如：`user:{uid}:friends:messages:{mid}` 简化为 `u:{uid}:fr:m:{mid}`

#### (3) 不要包含特殊字符【强制】

反例：包含空格、换行、单双引号以及其他转义字符（出现特殊字符会出问题）

### 1.2 value 设计：拒绝 bigkey

#### (1) Bigkey 的界定

经验上：字符串超过 10KB，或者二级数据结构元素超过 5000

|             | 要求 | 描述                                                         |
| ----------- | ---- | ------------------------------------------------------------ |
| 拒绝 bigkey | 强制 | 虽然Redis 中，字符串最大 512MB，二级数据结构可以存储大约 40 亿个（2^32-1）个元素。<br />实际中如果下面两种情况，就会认为它是 bigkey：<br />1. 字符串类型：超过 10KB<br />2. 二级数据结构（hash、list、set、zset）：元素超过 5000 |

#### (2) 危害及产生原因

危害：导致 redis 阻塞、网络拥塞、慢查询。

产生原因：数据/流量倾斜，value schema 设计不当

- 社交类（流量倾斜）：例如大 V 明星的粉丝列表，需要精心设计
- 统计类（数据倾斜）：例如按天存储的用户集合
- 缓存类（使用不当）：例如整库数据放在 Redis 中，某个 hash 塞了所有 user 的数据。要思考两点：（1）是否所有字段都需要缓存；（2）会不会带进来更多关联数据

#### (3) 删除策略

* 被动（过期）删除：使用 Redis 4.0 的 lazyfree-lazy-expire yes
* 主动删除：hscan、sscan、zscan 渐进删除

#### (4) 优化

##### 方法1：拆分（列表拆分、Hash 分段、分批读写）

* 列表拆分（big list）：list1、list2、…listN。
* Hash 分段（big hash）：例如一个 Big  Key 存了 1 百万用户数据，可以拆分成 200 个 key，每个 key 下面存放 5000 个用户数据。
* 如果 bigkey 不可避免，也要思考一下要不要每次把所有元素都取出来（例如有时候仅仅需要 hmget，而不是 hgetall），删除也是一样，尽量用优雅的方式来处理。

##### 方法2：选择适合的数据类型

对于 Java 中的 Entity，要在平衡内存节省和性能

反例：一个 user 被拆成多个 key，每个属性一个 key，过于冗余：
- `set user:1:name tom`
- `set user:1:age 19`
- `set user:1:favor football`

正例：用一个 hash 存储一个 user：
- `hash 存储`，比直接用 key 存储，占用的空间和处理时间都小：
- `hmset user:1 name tom age 19 favor football`

##### 方法3：控制 key 的生命周期

- 建议用 expire 设置过期时间
- 条件允许可以打散过期时间，防止集中过期

## 2 命令使用注意事项

### 2.1 O(N) 命令关注 N 的数量

有遍历需求时：

* 首选使用 hscan、sscan、zscan 代替。
* 非首选 hgetall、lrange、smembers、zrange、sinter ，不是不可以使用，但使用时要明确 N 的值。

### 2.2 禁用命令 keys / flushall / flushdb / …

禁止线上使用 keys、flushall、flushdb 等

建议通过 redis 的 rename 机制禁止这些命令、或使用 scan 的方式渐进式处理。

### 2.3 多部门使用各自专属集群、而不是单集群多数据库

Redis 支持多数据库（使用数字进行区分，0 ～ 15，最多 16 个数据库），但不推荐使用这个功能：

* 多数据库较弱，很多客户端支持也较差
* 不同业务使用不同数据库，但是 Redis 底层仍然是单线程，会产生性能干扰

如果多个部门，建议各自使用专属的 Redis（而不是共用一个 Redis 然后每个部门用其中一个数据库）。

### 2.4 使用批量操作提高效率

使用方法

- 原生命令：例如 mget、mset（原子操作）
- 非原生命令：可以使用 pipeline 提高效率（非原子操作，需要客户端服务端同时支持该功能）

注意控制一次批量操作的元素个数（例如 500 以内，也和元素字节数有关）。

### 2.5 用 lua 脚本实现 Redis 事务

Redis 事务功能较弱，不建议过多使用，可以用 lua 替代

用 lua 实现事务的操作。

## 3 客户端使用注意事项

### 3.1 避免多个应用使用一个 Redis 实例

作用：避免应用之间相互影响。

正例：不相干的业务拆分（使用各自专用的 Redis），公共数据做服务化。

### 3.2 使用带有连接池的数据库

#### (1) 用途

用连接池可以有效控制连接，同时提高效率，标准使用方式。

Redis 优势在于性能，所以创建连接的开销对于性能优化也非常重要。

#### (2) 代码示例

```java
JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
jedisPoolConfig.setMaxTotal(5);
jedisPoolConfig.setMaxIdle(2);
jedisPoolConfig.setTestOnBorrow(true);

JedisPool jedisPool = new JedisPool(jedisPoolConfig, "192.168.0.60", 6379, 3000, null);

Jedis jedis = null;
try {
    jedis = jedisPool.getResource();
    // 具体的命令
    jedis.executeCommand()
} catch (Exception e) {
    logger.error("op key {} error: " + e.getMessage(), key, e);
} finally {
    // 注意这里不是关闭连接，在 JedisPool 模式下，Jedis 会被归还给资源池。
    if (jedis != null)
        jedis.close();
}
```

#### (3) 连接池参数含义

| 序号 | 参数名             | 含义                                                         | 默认值 | 使用建议                                               |
| ---- | ------------------ | ------------------------------------------------------------ | ------ | ------------------------------------------------------ |
| 1    | maxTotal           | 资源池中最大连接数                                           | 8      | 设置建议见下面                                         |
| 2    | maxIdle            | 资源池允许最大空闲的连接数                                   | 8      | 设置建议见下面                                         |
| 3    | minIdle            | 资源池确保最少空闲的连接数                                   | 0      | 设置建议见下面                                         |
| 4    | blockWhenExhausted | 当资源池用尽后，调用者是否要等待。只有当为 true 时，下面的 maxWaitMillis 才会生效 | true   | 建议使用默认值                                         |
| 5    | maxWaitMillis      | 当资源池连接用尽后，调用者的最大等待时间（单位为毫秒）       | -1     | 不建议使用默认值                                       |
| 6    | testOnBorrow       | 向资源池借用连接时是否做连接有效性检测（ping），无效连接会被移除 | false  | 业务量很大时候建议设置为 false（多一次 ping 的开销）。 |
| 7    | testOnReturn       | 向资源池归还连接时是否做连接有效性检测（ping），无效连接会被移除 | false  | 业务量很大时候建议设置为 false（多一次 ping 的开销）。 |
| 8    | jmxEnabled         | 是否开启 jmx 监控，可用于监控                                | true   | 建议开启，但应用本身也要开启                           |

#### (4) 优化建议 

##### 建议 1：估算 maxTotal（客户端最大连接数）配置

maxTotal 是客户端最大连接数，早期的版本叫 maxActive。 配置该参数考虑如下因素：(1) 业务希望 Redis 并发量；（2）客户端执行命令时间；（3）Redis 资源

Redis 资源由服务端参数 maxClient来控制，表示最多容许多少个请求正在被处理或排队，多出来的会被拒绝。为例避免客户端请求被拒绝，maxTotal 参数需要满足如下资源约束：`maxTotal * clientCount <= maxClient`（即`客户端最大连接数 * 客户端数量 <= 服务端 Redis 资源数`）。

参数的优化目标是：虽然希望控制空闲连接（连接池此刻可马上使用的连接），但是不希望因为连接池的频繁释放创建连接造成不必要开销。

以一个例子说明，假设：

- 一次命令时间（含网络传输）的平均耗时约为 1ms，代表一个 Redis 连接每秒能执行 1000 个命令（QPS 是 1000）。
- 业务期望的 QPS 是 50000。
- 那么理论上需要的资源池大小是 50000 / 1000 = 50 个 Redis 连接同时存在。
- 但事实上这是个理论值，还要考虑到要比理论值预留一些资源，通常来讲 maxTotal 可以比理论值大一些。

maxTotal 不是越大越好：

- 一方面连接太多占用客户端和服务端资源。
- 另一方面对于 Redis 这种高 QPS 的服务器，一个大命令的阻塞即使设置再大资源池仍然会无济于事。

##### 建议 2：理解 maxIdle 和 minIdle，以及与 maxTotal 配置项的关系

Redis Client 连接数变化过程：

* 假设一直有请求，Redis 连接池会 lazy 初始化链接，先达到 minIdle，然后达到 maxIdle，然后继续增长，但不能超过 maxTotal。
* 假设随后没有请求：默认连接池连接数会降到 maxIdle，但通过某些其它配置也可以降到 minIdle。

`[minIdle, maxIdle]` 理解为最小 idle 数可能出现的区间，具体最小 idle 是多少取决于其它配置。

* minIdle（最小空闲连接数），与其说是最小空闲连接数，不如说是“至少需要保持的空闲连接数”
* 在使用连接的过程中，如果连接数超过了 minIdle，还会继续建立连接，如果超过了 maxIdle，当超过的连接执行完业务后会慢慢被移出连接池释放掉
* maxIdle 实际上才是业务需要的最大连接数，而maxTotal 是为了给出余量，所以 maxIdle 不要设置过小，否则会有 new Jedis（新连接）开销。

Redis 为何要提供 minIdle，maxIdle、maxTotal 三个级别的设置？是因为 Redis 应用场景往往都非常注重性能

* maxTotal：用来兜底，避免有请求被拒绝，同时有不会浪费连接数
* maxIdle：代表业务需要的最大连接数
* minIdle：提供弹性伸缩空间，根据业务需要，容许释放一部分连接

如何配置：

- 最强性能配置：maxTotal = maxIdle（不保留“弹性伸缩空间”，lazy 创建满之后，就一直有这么多连接），避免连接池伸缩带来的性能干扰。但是如果并发量不大或者 maxTotal 设置过高，会导致不必要的连接资源浪费。
- 一般推荐配置：maxIdle 可以设置为按上面的业务期望 QPS 计算出来的理论连接数，maxTotal 可以再放大一倍预留 buffer 避免。

##### 建议 3：缓存预热

Redis 连接池连接是 lazy 创建的，如果系统启动完马上就会有很多的请求过来，那么可以给 redis 连接池做预热，比如快速的创建一些 redis 连接，执行简单命令，类似 ping()，快速的将连接池里的空闲连接提升到 minIdle 的数量。

连接池预热示例代码：

```java
List<Jedis> minIdleJedisList 
  = new ArrayList<Jedis>(jedisPoolConfig.getMinIdle());

for (int i = 0; i < jedisPoolConfig.getMinIdle(); i++) {
    Jedis jedis = null;
    try {
        jedis = pool.getResource();
        minIdleJedisList.add(jedis);
        jedis.ping(); // 连接池预热
    } catch (Exception e) {
        logger.error(e.getMessage(), e);
    } finally {
        // 注意！
      	// 如果这时候 close 将连接还回连接池，下一次 jedis.ping() 就不会创建新连接，而是复用连接池中的旧连接。
      	// 这样就会导致连接池连接数始终为 1，起不到预热的效果
        // jedis.close(); 
    }
}


// 统一将预热的连接还回连接池
for (int i = 0; i < jedisPoolConfig.getMinIdle(); i++) {
    Jedis jedis = null;
    try {
        jedis = minIdleJedisList.get(i);
        // 现在 minIdle 个连接都已经创建完毕了，可以将这些连接归还连接池
        jedis.close();
    } catch (Exception e) {
        logger.error(e.getMessage(), e);
    } finally {
    }
}
```

总之，要根据实际系统的 QPS 和调用 redis 客户端的规模整体评估每个节点所使用的连接池大小。

### 3.3 高并发下熔断

高并发下建议客户端添加熔断功能（例如 sentinel、hystrix）。

### 3.4 密码设置及开启 SSL

设置合理的密码，如有必要可以使用 SSL 加密访问。

### 3.5  Key 清除配置

Redis 过期键的三种清除策略：

#### (1) 被动删除（Lazy Deletion：读写已过期 Key 时触发）

当读 / 写一个已经过期的 key 时会触发惰性删除策略，直接删除掉这个过期 key。

#### (2) 主动删除（Regular Deletion：定期删除一批过期的 Key）

Redis 定期（默认每 100ms）主动删除一批过期的 Key（注意只是一部分，不能释放所有内存），已用内存超过 maxMemory 限定时触发该策略

#### (3) 内存淘汰（Eviction Policies：挑选没有过期的 Key 释放内存）

##### (a) 触发时机

当前已用内存超过maxMemory限定时，触发内存淘汰

##### (b) 8 种内存淘汰策略

在Redis 4.0 之前一共实现了 6 种内存淘汰策略，在 4.0 之后，又增加了 2 种策略，总共8种

| 类别                   | 策略名          | 介绍                                                         |
| ---------------------- | --------------- | ------------------------------------------------------------ |
| 针对设置过期时间的 key | volatile-ttl    | 根据过期时间的先后进行删除，越早过期的越先被删除             |
|                        | volatile-random | 随机删除                                                     |
|                        | volatile-lru    | 使用 LRU 算法挑选被删除的 Key                                |
|                        | volatile-lfu    | 使用 LFU 算法挑选被删除的 Key                                |
| 针对所有 Key           | allkeys-random  | 随机选择并删除数据                                           |
|                        | allkeys-lru     | 使用 LRU 算法选择并删除                                      |
|                        | allkeys-lfu     | 使用 LFU 算法选择并删除                                      |
| 不处理（默认值）       | noeviction      | 不会剔除任何数据，拒绝所有写入操作并返回客户端错误信息<br /> "(error) OOM command not allowed when used memory"，此时 Redis 只响应读操作 |

##### (c) LRU 和 LFU

LRU 算法（Least Recently Used）：以最近一次访问时间作为参考，灵敏度更高，没有 Frequency 统计期。

LFU 算法（Least Frequently Used）：淘汰最近一段时间被访问次数最少的数据，以次数作为参考有 Frequency 统计期，但是灵敏度略低。

算法选择：有热点数据时，会希望这些数据不被淘汰，此时 LFU 更合适（ LRU 没有 Frequency 统计期，容易被偶发或周期性批量操作污染缓存，导致命中率极具下降）

##### (d) 内存淘汰策略选择

根据自身业务类型，配置好 maxmemory-policy（默认是 noeviction），推荐使用 volatile-lru。

##### (e) 注意事项

如果不设置最大内存，当 Redis 内存超出物理内存限制时，内存的数据会开始和磁盘产生频繁的交换（swap），会让 Redis 的性能急剧下降。

Redis 运行在主从模式时，只有主结点才会执行过期删除策略，然后把删除操作 "del key" 同步到从结点删除数据。

## 4 系统内核参数优化

### 4.1 vm.swapiness

物理内存不足时，可以将一部分内存页进行 swap 到硬盘上。但是swap 空间由硬盘提供，对高并发高吞吐的应用来说，磁盘 IO 通常会成为系统瓶颈。

在 Linux 中，不是物理内存耗尽才会使用到 swap，swappiness 会决定操作系统使用 swap 的倾向程度。swappiness 的取值范围是 0 ~ 100.

* swappiness 的值越大，说明操作系统可能使用 swap 的概率越高
* swappiness 值越低，表示操作系统更加倾向于使用物理内存。

如果想让OS 宁愿 swap 也不会 OOM Killer

* 如果 linux 内核版本 >= 3.5，那么 swappiness 设置为 1，这样系统宁愿 swap 也不会 oom killer。
* 如果 linux 内核版本 < 3.5，那么 swappiness 设置为 0，这样系统宁愿 swap 也不会 oom killer（杀掉进程）

一般需要保证 redis 不会被 kill 掉

```bash
# 查看 linux 内核版本
cat /proc/version  
# >= 3.5 则将 swappniess 设置为 1，< 3.5 则将它设置为 0
echo 1 > /proc/sys/vm/swappiness
echo vm.swapiness=1 >> /etc/sysctl.conf
```

PS：OOM killer 机制是指 Linux 操作系统发现可用内存不足时，强制杀死一些用户进程（非内核进程），来保证系统有足够的可用内存进行分配。

### 4.2 vm.overcommit_memory（默认 0）

参数值

* 0：表示内核将检查是否有足够的可用物理内存（实际不一定用满）供应用进程使用；如果有足够的可用物理内存，内存申请允许；否则，内存申请失败，并把错误返回给应用进程。
* 1：表示内核允许分配所有的物理内存，而不管当前的内存状态如何。

如果是 0 的话，可能导致类似 fork 等操作执行失败，申请不到足够的内存空间。

Redis 建议把这个值设置为 1，就是为了让 fork 操作能够在低内存下也执行成功。

```bash
cat /proc/sys/vm/overcommit_memory
echo "vm.overcommit_memory=1" >> /etc/sysctl.conf
sysctl vm.overcommit_memory=1
```

### 4.3 合理设置文件句柄数

操作系统进程试图打开一个文件（或者叫句柄），但是现在进程打开的句柄数已经达到了上限，继续打开会报错："Too many open files"。

```bash
# 查看系统文件句柄数，看 open files 那项
ulimit -a  
# 设置系统文件句柄数
ulimit -n 65535  
```

## 5 慢查询日志查看工具：slowlog

Redis 的 `SLOWLOG` 是一个非常有用的工具，用于记录执行时间超过指定阈值的命令。通过查看慢查询日志，可以识别系统瓶颈、分析和优化 Redis 的性能问题。

慢查询参数可以配置在 Redis 配置文件中，也可以通过以下 Redis 命令动态修改和操作，而无需重启Redis 服务

```bash
# 查询有关慢日志的配置信息
config get slow* 
# 设置慢日志时间阈值，单位微秒，此处为 20 毫秒，即超过 20 毫秒的操作都会记录下来，生产环境建议设置 1000，也就是 1ms，这样理论上 redis 并发至少达到 1000，如果要求单机并发达到 1 万以上，这个值可以设置为 100
config set slowlog-log-slower-than 20000  
# 设置慢日志记录保存数量，如果保存数量已满，会删除最早的记录，最新的记录追加进来。记录慢查询日志时 Redis 会对长命令做截断操作，并不会占用大量内存，建议设置稍大些，防止丢失日志
config set slowlog-max-len 1024  
# 将服务器当前所使用的配置保存到 redis.conf
config rewrite 
# 获取慢查询日志列表的当前长度
slowlog len 
# 获取最新的 5 条慢查询日志。慢查询日志由四个属性组成：标识 ID，发生时间戳，命令耗时，执行命令和参数
slowlog get 5 
# 重置慢查询日志
slowlog reset 
```

注意事项：

- **性能影响**：慢查询日志会占用一定的内存，尤其是在记录大量日志时。建议合理设置 `slowlog-max-len` 参数。
- **生产环境**：在生产环境中，建议将 `slowlog-log-slower-than` 设置为一个合理的值，避免记录过多不必要的日志。



