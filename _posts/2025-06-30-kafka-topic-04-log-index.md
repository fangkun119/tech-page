---
title: Kafka参考04：日志数据索引
author: fangkun119
date: 2025-06-30 12:00:00 +0800
categories: [中间件, Kafka]
tags: [Kafka]
pin: false
math: true
mermaid: true
image:
  path: /imgs/cover/kafka.jpeg
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

## 1 介绍

### 1.1 背景
了解 Kafka 的集群运转机制、可以理解 Broker 如何协作来保证 Partition 内消息的一致性；但是具体分工到每个 Broker，这些消息是如何高效处理和保存的，对于理解 Kafka 的高吞吐、高性能、高可扩展非常有帮助。

这个过程的核心、以及关键着手点就是研究 Kafka 的消息数据（即Log 日志）是如何存储的。

### 1.2 数据存储机制
Kafka 的每个 Broker 都以相同逻辑在运行，无状态设计使它们便于水平扩展，可以用一个新的 Broker 替换一个旧的 Broker，唯一要做的是进行数据转移，但这并不是复制粘贴那么简单，数据是以二进制的格式来存储的，涉及到的底层细节也非常多。
### 1.3 数据迁移工具
Kafka也提供⼯具来协助进⾏数据迁移，例如 bin 目录下的kafka-reassign-partitions.sh，可以通过脚本的--help指令来了解。
## 2 Topic消息数据文件

### 2.1 数据文件介绍

存储目录：配置在 server.properties 的 log.dir 中，该目录下每个`{topic}-{partion_idx}`作为一个子目录，存储对应的数据

例子如下：

```shell
$ ls /app/kafka/kafka-logs/my-topic-1
0000000000000000000000000000.index
0000000000000000000000000000.log
0000000000000000000000000000.timeindex
leader-epoch-checkpoint
partition.metadata
```

Segment 文件： {first_message_offset}.log 

* 数据文件大小固定为 1G，由配置项 log.segment.bytes 指定
* 每次写满就新增一个数据文件，每个文件也叫一个 segment 文件
* 文件名命名规范为 {first_message_offset}.log ，用这个 segment 文件中第一条消息的偏移量命名

配套索引：用于在 segment 文件中查找消息

* {first_message_offset}.index：以消息偏移量为 key 的索引
* {first_message_offset}.timeindex：以消息时间戳为 key 的索引

元数据：

* partition.metada：当前 partition 所属的 cluster 和 topic
* leader-epoch-checkpoint：参考之前的 epoch 机制

### 2.2 内容读取

这些文件都是二进制的文件，无法使用文本工具直接查看。但是，Kafka提供了工具可以用来查看这些日志的内容文件。

```bash
#1、查看timeIndex文件
[root@192-168-65-112 bin]# ./kafka-dump-log.sh --files /app/kafka/logs/disTopic0/00000000000000000000.timeindex
Dumping /app/kafka/logs/disTopic-0/00000000000000000000.timeindex
timestamp: 1723519364827 offset: 50
timestamp: 1723519365630 offset: 99
timestamp: 1723519366162 offset: 148
timestamp: 1723519366562 offset: 197
timestamp: 1723519367013 offset: 246
timestamp: 1723519367364 offset: 295
timestamp: 1723519367766 offset: 344

#2、查看index文件
[root@192-168-65-112 bin]# ./kafka-dump-log.sh --files /app/kafka/logs/disTopic0/00000000000000000000.index
Dumping /app/kafka/logs/disTopic-0/00000000000000000000.index
offset: 50 position: 4098
offset: 99 position: 8214
offset: 148 position: 12330
offset: 197 position: 16446
offset: 246 position: 20562
offset: 295 position: 24678
offset: 344 position: 28794

#3、查看log文件
[root@192-168-65-112 bin]# ./kafka-dump-log.sh --files /app/kafka/logs/disTopic0/00000000000000000000.log
Dumping /app/kafka/kafka-logs/secondTopic-0/00000000000000000000.log
Starting offset: 0
.....
baseOffset: 350 lastOffset: 350 count: 1 baseSequence: 349 lastSequence: 349 producerId: 5002 producerEpoch: 0 partitionLeaderEpoch: 7 isTransactional: false isControl: false deleteHorizonMs: OptionalLong.empty position: 29298 CreateTime: 1723519367827 size: 84 magic: 2 compresscodec: none crc: 400306231 isvalid: true
baseOffset: 351 lastOffset: 351 count: 1 baseSequence: 350 lastSequence: 350 producerId: 5002 producerEpoch: 0 partitionLeaderEpoch: 7 isTransactional: false isControl: false deleteHorizonMs: OptionalLong.empty position: 29382 CreateTime: 1723519367829 size: 84 magic: 2 compresscodec: none crc: 2036034757 isvalid: true
.......
```

这些数据文件的记录方式，是理解Kafka本地存储的主线

### 2.3 追加写入

这些文件中的消息日志，只允许追加，不支持删除和修改。

只有文件名最大的一个log文件是当前写入消息的日志文件，其他文件都是不可修改的历史日志。

文件大小固定，用第一条消息偏移量命名，都是为了方便读取消息，提高读取效率。

### 2.4 数据索引：index 和 timeindex

Kafka 通过消息偏移量来找到消息日志。而用第一条消息偏移量命名的方式，使得 Kafka 能够快速找到所需的索引文件。

两个索引并不是对每一条消息都添加索引，而是Broker每写入40KB的数据，就建立一条index索引，具体由配置项 log.index.interval.bytes 指定。

```text
log.index.interval.bytes
# The interval with which we add an entry to the offset index
# Type: int
# Default: 4096 (4 kibibytes)
# Valid Values: [0,...]
# Importance: medium Update Mode: cluster-wide
```

index 文件用于加速消息查询

timeindex 文件用来加速时间相关的消息处理，例如文件清理

## 3 过期文件清理

Kafka 定期删除过期的数据文件，删除机制涉及到几组配置属性

### 3.1 过期文件检测间隔

配置项：`log.retention.check.interval.ms`

默认是 300000 毫秒，也就是五分钟

### 3.2 文件保留时长

配置项：`log.retention.hours` ，` log.retention.minutes`， `log.retention.ms` 

以时间精度最高的配置项为准，默认生效的是 log.retention.hours，默认值是 168 小时、也就是 7 天

### 3.3 判断文件是否超时

以每个.timeindex中最大的那一条记录为准。

### 3.4 文件清理策略

配置项：`log.cleanup.policy`

配置值：

* delete 表示删除，此时还会参考另一个配置项 `log.retention.bytes`表示所有文件大小超过这个阈值时会触发最早文件的删除（默认值 -1 表示无限大）

* compact 表示压缩，虽然不会直接删除日志文件，但是会造成消息丢失（会将key相同的消息进行压缩，只保留最后一条）

## 4 客户端消费进度管理

### 4.1 内置 Topic：__consumer_offsets

每个消费者组（Consumer Group）的消费进度，管理在 Kafka 的一个内置 Topic 中，名为`__consumer_offsets` 

这个 Topic 默认被划分为 50 个分区

和普通 Topic 一样

* 在各个 Broker 的本地磁盘中，可以看到与这个 Topic 相关的日志存储目录

* 这个内置 Topic 的状态信息也会记录在 Zookeeper 中，例如：controller_epoch, leader, version, leader_epoch, isr （见早先的章节内容）

### 4.2 进度管理数据

启动一个消费者订阅这个内置Topic

```shell
[root@192-168-65-112 kafka_2.13-3.8.0]# bin/kafka-console-consumer.sh --topic
__consumer_offsets --bootstrap-server worker1:9092 --consumer.config
config/consumer.properties --formatter
"kafka.coordinator.group.GroupMetadataManager\$OffsetsMessageFormatter" --from-beginning
```

可以看到结果

```shell
[test,disTopic,1]::OffsetAndMetadata(offset=3, leaderEpoch=Optional[1], metadata=,
commitTimestamp=1661351768150, expireTimestamp=None)
[test,disTopic,2]::OffsetAndMetadata(offset=0, leaderEpoch=Optional.empty, metadata=,
commitTimestamp=1661351768150, expireTimestamp=None)
[test,disTopic,0]::OffsetAndMetadata(offset=6, leaderEpoch=Optional[2], metadata=,
commitTimestamp=1661351768150, expireTimestamp=None)
[test,disTopic,3]::OffsetAndMetadata(offset=6, leaderEpoch=Optional[3], metadata=,
commitTimestamp=1661351768151, expireTimestamp=None)
[test,disTopic,1]::OffsetAndMetadata(offset=3, leaderEpoch=Optional[1], metadata=,
commitTimestamp=1661351768151, expireTimestamp=None)
[test,disTopic,2]::OffsetAndMetadata(offset=0, leaderEpoch=Optional.empty, metadata=,
commitTimestamp=1661351768151, expireTimestamp=None)
[test,disTopic,0]::OffsetAndMetadata(offset=6, leaderEpoch=Optional[2], metadata=,
commitTimestamp=1661351768151, expireTimestamp=None)
[test,disTopic,3]::OffsetAndMetadata(offset=6, leaderEpoch=Optional[3], metadata=,
commitTimestamp=1661351768153, expireTimestamp=None)
[test,disTopic,1]::OffsetAndMetadata(offset=3, leaderEpoch=Optional[1], metadata=,
commitTimestamp=1661351768153, expireTimestamp=None)
```

与普通 Topic 一样，这个 Topic 也是以 Key-  Value 的方式维护消费进度：

* key：groupid + topic + partition
* value：当前offset。

需要指出的是：这些Offset数据，其实是可以被消费者“修改”的

* 例如：指定消费者从指定位置开始消费时，消费者会主动调整Offset，触发 Kafka 更改这个 Topic 中的记录

### 4.3 内置 Topic 对比 Zookeeper 

早期 Kafka 版本中，Offset确实是存在Zookeeper中的，但是 Kafka 很早就选择了将 Offset 从 Zookeeper 转移到 Broker上。

这也体现了Kafka其实早就意识到，Zookeeper这样一个外部组件在面对三高问题时，是不太"靠谱"的，所以Kafka逐渐转移了Zookeeper上的数据，而后有的Kraft集群，其实也是这种思想的延伸。

### 4.4 阻止消费者订阅内置 Topic

另外，这个系统Topic里面的数据是非常重要的，因此Kafka在消费者端也设计了一个参数来控制这个Topic应该从订阅关系中剔除。

```java
public static final String EXCLUDE_INTERNAL_TOPICS_CONFIG = "exclude.internal.topics";
private static final String EXCLUDE_INTERNAL_TOPICS_DOC = "Whether internal topics matching a subscribed pattern should " +
            "be excluded from the subscription. It is always possible to explicitly subscribe to an internal topic.";
public static final boolean DEFAULT_EXCLUDE_INTERNAL_TOPICS = true;
```

## 5 Kafka的文件高效读写机制

这是Kafka非常重要的一个设计，分几个方向来理解。

### 5.1 文件结构设计

Kafka的数据文件结构设计可以加速日志文件的读取，例如：

* 同一个 Topic 下的多个 Partition 分开存储日志数据，加速并行读取
* index的稀疏索引结构，加快 Log 日志检索。

### 5.2 顺序写磁盘

Kafka 对磁盘是顺序读和顺序写，因此可以利用操作系统和硬盘结构的特性：

* 为每个Log文件提前规划固定的大小，占据一块连续的磁盘空间
* 避免磁盘碎片，避免随机读写的发生，提高读写效率

Kafka 官网有测试数据，同样的磁盘：顺序写速度能达到 600M/s，基本与写内存相当；而随机写的速度就只有100K/s，差距比加很大

### 5.3 零拷贝

Kafka 大量运行 Linux 提供的零拷贝 I/O 优化机制来加速文件读写。

#### (1) Linux 传统 I/O 

步骤 1：将用户态 JVM 内存中原始数据，拷贝到内核态的页缓存、或 Socket 缓冲区

步骤 2：Linux 使用 DMA Copy 将内核态缓存的数据，写入到磁盘或网络中

而零拷贝技术，主要是减少用户态与内核态之间的数据拷贝，有两种方式：mmap 文件映射，sendfile 文件传输

#### (2) mmap 文件映射机制

**原理：**

* 用户态不再缓存整个IO的内容，改为只持有文件映射信息，通过这些映射，"遥控"内核态的文件读写。
* 这样就减少了内核态与用户态之间的拷贝数据大小，提升了IO效率。
* 在 Linux 上使用 `man 2 mmap` 命令可以查看详细说明。

**适用场景：**

* 适合于操作不是很大的文件，通常映射的文件不建议超过2G。
* 也正是这个原因，Kafka 将日志文件设计成大小不超过 1G

#### (3) sendfile文件传输机制

**原理：**

* 可以理解为用户态（也就是应用程序）不再关注数据的内容，只是向内核态发一个 sendfile 指令，要他去复制文件就行。
* 这样数据就完全不用复制到用户态，从而实现了零拷贝。相比mmap，连索引都不读了，直接通知操作系统去拷贝就是了。
* 在 Linux 上使用 `man 2 sendfile`命令可以查看详细说明。

**优缺点：**

* 优点：效率更高了。
* 缺点：用户态对文件内容完全无感知，也就是说无法在用户态中对文件内容做解析。

**使用场景：**

* 当Consumer要从Broker上poll消息时，Broker需要读取自己本地的数据文件，然后通过网卡发送给Consumer。

* 这个过程中，Broker只负责传递消息，而不对消息进行任何的加工。以此它只需要将数据从磁盘读取出来，复制到网卡的Socket缓冲区，然后通过网络发送出即可。这个过程中，用户态只需要往内核态发一个sendfile指令，而不需要有任何的数据拷贝过程。

* Kafka大量的使用了sendfile机制，用来加速对本地数据文件的读取过程。

* JDK中8中java.nio.channels.FileChannel类提供了transferTo和transferFrom⽅法，底层就是使⽤了操作

  系统的sendfile机制。

### 5.4 合理配置刷盘频率

#### (1) 原理和利弊

缓存中的数据如果没有及时写入到硬盘，如果服务突然崩溃，就可能丢消息的可能。但是每写一条数据，就刷一次盘（为同步刷盘）又会带来额外的开销。

刷盘操作在Linux系统中对应了一个fsync的系统调用。

```text
FSYNC(2) 
Linux Programmer's Manual 
FSYNC(2)
    fsync, fdatasync - synchronize a file's in-core state with storage device.
```

这里的 in-core state 指的是操作系统内核态的缓存（pageCache），是应用程序接触不到的一部分缓存。

这也意味着应用程序并不能干预这些数据的刷盘操作，它唯一能做的就是尽量频繁的通知操作系统进行刷盘（但何时刷盘取决于操作系统），这样做：

* 并不能百分百保证数据安全，只能近似于“同步刷盘”
* 也会增加性能开销

在实际应用时，我们通常也只能根据自己的业务场景进行权衡。

#### (2) Kafka 提供的配置参数

Kafka在服务端设计了几个参数，来控制刷盘的频率：

`flush.ms` : 多长时间进行一次强制刷盘

```text
This setting allows specifying a time interval at which we will force an fsync of data written to the log. For example if this was set to 1000 we would fsync after 1000 ms had passed. In general we recommend you not set this and use replication for durability and allow the operating system's background flush capabilities as it is more efficient.
Type: long
Default: 9223372036854775807
Valid Values: [0,...]
Server Default Property: log.flush.interval.ms
Importance: medium
```

`log.flush.interval.messages`：表示当同一个Partiton的消息条数积累到这个数量时，就会申请一次刷盘操作。 默认是Long.MAX。

```text
The number of messages accumulated on a log partition before messages are flushed to disk.
Type: long
Default: 9223372036854775807
Valid Values: [1,...]
Importance: high
Update Mode: cluster-wide
```

log.flush.interval.ms：当一个消息在内存中保留的时间，达到这个数量时，就会申请一次刷盘操作。他的默认值是空。如果这个参数配置为空，则生效的是下下一个参数。

```text
The maximum time in ms that a message in any topic is kept in memory before flushed to disk. If not set, the value in log.flush.scheduler.interval.ms is used.
Type: long
Default: null
Valid Values:
Importance: high
Update Mode: cluster-wide
```

log.flush.scheduler.interval.ms：检查是否有日志文件需要进行刷盘的频率。默认也是Long.MAX。

```text
The frequency in ms that the log flusher checks whether any log needs to be flushed to disk.
Type: long
Default: 9223372036854775807
Valid Values:
Importance: high
Update Mode: read-only
```

#### (3) 总结

这里可以看到：

* Kafka 为了最大化性能，默认是将刷盘操作交由了操作系统进行统一管理。
* Kafka并没有实现写一个消息就进行一次刷盘的“同步刷盘”机制。也就是说，Kafka无法保证非正常断电情况下的消息安全。

这其实不光是Kafka面临的问题，而是所有应用程序都需要面临的问题。

在RabbitMQ中：

* 官网明确提出，服务端并不完全保证消息不丢失
* 如果需要提升消息安全性，就只能通过Publisher Confirms机制，让客户端参与验证。
* 虽然 RocketMQ 提供了“同步刷盘”的配置选项，但如果真的每一个消息就调用一次刷盘操作，那么任何服务器都是无法承受的。