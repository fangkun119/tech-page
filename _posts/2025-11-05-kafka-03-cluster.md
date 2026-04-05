---
title: Kafka深入03：集群工作机制
author: fangkun119
date: 2025-11-05 12:00:00 +0800
categories: [中间件, Kafka]
tags: [Kafka]
pin: false
math: true
mermaid: true
image:
  path: /imgs/cover/kafka_in_depth.jpeg
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

## 1. 文章概要

本文系统讲解 Kafka 集群工作机制，内容涵盖以下六大知识模块：

| 知识模块                      | 说明                                                        |
| ------------------------- | --------------------------------------------------------- |
| **状态与业务数据分离**             | 集群设计的起点：**状态信息**存 **Zookeeper**，**业务数据**存本地日志                       |
| **Zookeeper 数据管理**        | **Broker**、**Topic**、**Partition** 等元数据在 Zookeeper 中的存储结构          |
| **Controller 选举**         | 通过抢占 **/controller** 临时节点选举集群管理者                                  |
| **Leader Partition 选举** | **AR** 顺序优先 + **ISR** 存活过滤的选举规则，以及 **Leader** 自动平衡                 |
| **Partition 故障恢复**        | **Follower**/**Leader** 故障的不同处理流程，以及 **LEO**、**HW** 的作用              |
| **HW 一致性保障**              | **Epoch** 机制如何解决 Leader 切换时的 HW 不一致问题                     |

基于上述知识框架，本文将从集群设计的核心原理出发，按照由浅入深的路径展开：首先介绍 Zookeeper 如何管理集群元数据，然后详细解析 Controller 选举和 Leader Partition 选举机制，最后深入探讨故障恢复场景下的数据一致性保障。通过这一完整的学习路径，帮助读者全面理解 Kafka 集群如何协调多个 Broker 实现消息的可靠同步与故障恢复。

核心内容提炼（[`Youtube`](https://youtu.be/quSV-f4yWew) `|` [`B站`](https://www.bilibili.com/video/BV1yGD5BMEAu/)）：

<iframe width="560" height="315" src="https://www.youtube.com/embed/quSV-f4yWew?si=kJ-u0UbRjXD-Bwtl" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share" referrerpolicy="strict-origin-when-cross-origin" allowfullscreen></iframe>

## 2. 集群工作概览

Kafka 集群设计围绕两个目标：**高吞吐**和**可扩展**。理解这些设计的关键在于区分**两类数据**，它们各有明确的存储位置和处理方式：

| 数据类型       | 存储位置           | 作用                                                         | 各 Broker 的处理方式             |
| ------------ | -------------- | ---------------------------------------------------------- | -------------------------- |
| **状态信息**     | **Zookeeper** | 记录集群元数据（**Broker** 列表、**Topic**/**Partition** 分配、**Leader** 选举等） | 不同 Broker 读取不同状态，执行差异化逻辑 |
| **业务数据**     | **Kafka** 本地日志 | 记录消息内容                                                     | 所有 Broker 以相同逻辑读写      |

基于上述分类，Kafka 采用了**状态与业务数据分离**的设计。这一设计成为集群扩展的基础——新增 **Broker** 只需启动并注册到 **Zookeeper**，无需在节点间同步额外状态。

> **版本说明**：自 **Kafka 4.0（2025 年）**起，**KRaft 模式**彻底取代 **Zookeeper**，成为唯一的元数据管理方式。本文以 **Zookeeper** 为例讲解集群工作机制，因其设计原理直观清晰。关于 **KRaft** 的相关内容，将在后续文章[Kafka深入05：功能扩展]({% post_url 2025-11-09-kafka-05-extension %})中补充。

## 3. Zookeeper 中的数据管理

### 3.1 Zookeeper 的特性

前文提到 Kafka 将状态信息交给 **Zookeeper** 管理。那么，**Zookeeper** 凭借什么能力来胜任这个角色呢？它提供了两项关键能力来支撑这一设计：

| 机制             | 作用                                               |
| -------------- | ------------------------------------------------ |
| **强一致性保证**     | 确保所有 **Broker** 对集群状态达成共识，分工清晰                       |
| **Watcher 机制** | **Broker** 注册监听后由 **Zookeeper** 主动推送变更通知，避免反复轮询，降低网络开销 |

有了这两项基础能力，Kafka 集群中的多个 **Broker** 才能协调一致地工作。接下来，让我们看看这些 **Broker** 在集群中具体扮演什么角色。

### 3.2 Kafka 集群中的**两类角色**

在 Kafka 集群中，**Broker** 节点并非同质化运行，而是根据职责分工担任两类不同角色：

| 角色类型                 | 说明                                                  |
| -------------------- | --------------------------------------------------- |
| **Controller**       | 从集群中选举出的一个 **Broker**，负责管理分区和副本状态                   |
| **Leader Partition** | 每个 **Partition** 的多个副本中选举出的 **Leader**，负责与客户端进行数据交互 |

理解这两类角色的区别至关重要。**Controller** 是集群级别的管理者，整个集群只有一个；而 **Leader Partition** 是分区级别的数据服务者，每个 Partition 都有自己的 Leader。下图展示了 Kafka 的整体集群结构（红色标识关键状态信息）：

<img src="/imgs/kafka-03-cluster/f8bf663d11821f7327bb30fe41ded829_MD5.jpg" style="display: block; width: 100%;" alt="Kafka 集群结构">

可以看到，这两类角色的选举信息都存储在 **Zookeeper** 中。接下来，让我们深入分析 **Zookeeper** 中具体的数据结构。

### 3.3 **Zookeeper** 中的数据结构

了解了 **Zookeeper** 的特性和集群中的角色后，我们来看看具体的数据结构。上述两类角色的选举信息都存储在 **Zookeeper** 中，其组织结构如下：

<img src="/imgs/kafka-03-cluster/884b4f1343203b53e1cb7f13ab779036_MD5.jpg" style="display: block; width: 100%;" alt="Zookeeper 数据存储结构">

> 工具推荐：实际工作中可使用 IDEA 中的 **Zookeeper Manager 插件**查看 Zookeeper 数据

从上图可以看到，**Zookeeper** 中维护了多个关键节点，其中最重要的是以下三个：

| Zookeeper节点              | 说明                           |
| ------------------ | ---------------------------- |
| **/brokers/ids**   | 记录集群中所有 **BrokerId**        |
| **/brokers/topics** | 记录 **Topic** 的 **Partition** 分区信息 |
| **/controller**    | 标记当前 **Controller Broker**     |

除了存储静态的元数据信息外，**Zookeeper** 还负责跟踪 **Broker** 的动态注册过程：

| 场景              | 说明                                                          |
| --------------- | ----------------------------------------------------------- |
| **Broker** 启动 | 在 **Zookeeper** 中创建临时节点 **/brokers/ids/{BrokerId}**         |
| **Broker** 停止 | 对应的临时节点自动注销                                                |
| 集群感知      | 其他 **Broker** 通过监听这些节点的变化感知集群状态                              |

## 4. **Controller** 选举机制

**Kafka** 集群启动时，需要从多个 **Broker** 中选出一个担任 **Controller**，负责管理分区和副本状态。选举通过抢占 **Zookeeper** 的 **/controller 节点**实现。

### 4.1 选举过程

**Controller** 的选举过程分为两个关键步骤：先通过抢占方式确定初始 **Controller**，然后通过监听机制处理后续的故障切换。

**步骤 1：抢占注册**

集群中的 **Broker** 启动时，会尝试在 **Zookeeper** 创建临时节点 **/controller**，并将自己的 **brokerId** 写入该节点。例如，当 **BrokerId=2** 的节点成功创建该节点时，节点内容如下：

```json
{"version":2,"brokerId":2,"timestamp":"1723447688383","kraftControllerEpoch":-1}
```

这一 JSON 结构记录了成功注册的 **Broker** 标识及其版本信息，其中 **brokerId** 字段标识了当前 **Controller** 的身份。

**步骤 2：监听与切换**

由于 **/controller** 是临时节点，一旦当前 **Controller** 故障，节点会自动删除，从而触发重新选举。具体机制如下：

| 场景          | 说明                                                                  |
| ----------- | ------------------------------------------------------------------- |
| **创建失败**     | **Zookeeper** 保证集群中只有一个 **Broker** 能成功创建该节点                          |
| **注册监听**     | 未成功的 **Broker** 会注册监听，等待 **/controller** 节点被删除                          |
| **重新选举** | 节点删除后，所有监听的 **Broker** 重新尝试注册，争取成为新的 **Controller** |

### 4.2 心跳与故障转移

**Controller** 选举完成后，需要确保其持续在线并能够响应集群管理请求。为此，**Kafka** 设计了心跳机制来实时监控 **Controller** 的健康状态，并在故障时触发自动切换。

心跳机制的工作原理如下：

| 机制       | 说明                                                            |
| -------- | ------------------------------------------------------------- |
| **心跳维护** | **Controller** 通过心跳连接与 **Zookeeper** 保持会话                     |
| **故障检测** | **Zookeeper** 长时间未收到心跳时，删除 **/controller** 临时节点                  |
| **自动切换** | 下一个 **Broker** 成功注册成为新 **Controller**，version 自动更新 |

### 4.3 **Controller** 的职责

选举成功后，**Controller** 作为集群的核心管理者，需要实时掌握集群状态变化。它通过监听 **Zookeeper** 中的关键节点来感知集群动态，并据此协调各项事务：

| 监听节点                   | 作用                                                  |
| ---------------------- | --------------------------------------------------- |
| **/brokers/ids**       | 感知 **Broker** 的增减变化                                  |
| **/brokers/topics**    | 感知 **Topic** 及 **Partition** 的增减变化                       |
| **/admin/delete_topic** | 处理删除 **Topic** 的请求                                   |

除了监听集群状态外，**Controller** 还承担着以下关键职责：

- **元数据同步**：将集群元数据推送给其他 **Broker**，确保所有节点对集群状态达成一致认知
- **分区管理**：负责创建新分区、重新分配分区副本等管理操作
- **Leader 选举协调**：当 **Leader Partition** 故障时，协调并触发新的 Leader 选举
- **副本状态维护**：监控 ISR 列表的变化，动态调整副本同步状态

## 5. **Leader Partition** 选举机制

### 5.1 副本状态记录

每个 **Partition** 有多个副本，其中 **Leader Partition** 负责与客户端交互，其余 **Follower Partition** 从 **Leader** 同步数据。

为了有效管理这些副本的状态，**Kafka** 设计了三组核心集合，它们之间的关系可以表示为：**AR = ISR + OSR**（即：所有分配副本 = 保持同步的副本 + 有问题的副本）。

<img src="/imgs/kafka-03-cluster/fe6953521376e743f74fd615ee2a3d28_MD5.jpg" style="display: block; width: 100%;" alt="AR ISR OSR 副本状态集合">

这三组集合的具体含义如下：

| 概念      | 全称                       | 说明                                  |
| ------- | ------------------------ | ----------------------------------- |
| **AR**  | **Assigned Replicas**    | **Partition** 的所有副本集合（包括存活和未存活的）    |
| **ISR** | **In-Sync Replicas**     | **AR** 中服务正常、与 **Leader** 保持同步的副本集合 |
| **OSR** | **Out-of-Sync Replicas** | 从 **ISR** 中移除的副本（服务异常或同步延迟过高）       |

在实际运行中，副本会在这些集合间动态调整。**ISR** 的成员变化遵循以下规则：

| 规则类型       | 说明                                                         |
| ---------- | ---------------------------------------------------------- |
| 移出 **ISR** | **Follower** 超过 **`replica.lag.time.max.ms`**（默认 **30s**）未向 **Leader** 发送拉取请求，则从 **ISR** 中移除 |
| 移除旧参数  | 早期版本还有 `replica.lag.max.messages` 参数（按消息差值判断），新版本已废弃              |

我们可以通过命令行工具查看当前副本的实际状态。使用 `kafka-topics.sh --describe` 查看各 **Partition** 的 **AR**、**ISR** 和 **Leader**：

```shell
[root@192-168-65-112 kafka_2.13-3.8.0]# bin/kafka-topics.sh --bootstrap-server worker1:9092 --describe --topic disTopic
[2024-08-12 15:42:57,462] WARN [AdminClient clientId=adminclient-1] The DescribeTopicPartitions API is not supported, using Metadata API to describe topics. (org.apache.kafka.clients.admin.KafkaAdminClient)
Topic: disTopic      TopicId: CNrWfmEgSBqc9gLClemrXw    PartitionCount: 3       ReplicationFactor: 2    Configs:
        Topic: disTopic      Partition: 0    Leader: 1       Replicas: 1,0   Isr: 0,1        Elr: N/A        LastKnownElr: N/A
        Topic: disTopic      Partition: 1    Leader: 2       Replicas: 0,2   Isr: 2,0        Elr: N/A        LastKnownElr: N/A
        Topic: disTopic      Partition: 2    Leader: 2       Replicas: 2,1   Isr: 2,1        Elr: N/A        LastKnownElr: N/A
```

从上面的输出结果中，我们可以看到几个关键字段：

| 字段 | 含义 |
| --- | --- |
| **Leader** | 当前 **Leader** 所在的 **Broker ID** |
| **Replicas** | **AR**（所有副本的 **Broker ID** 列表） |
| **Isr** | **ISR**（当前同步中的副本 **Broker ID** 列表） |

这些副本状态信息最终都会持久化到 **Zookeeper** 中，其存储结构如下图所示：

<img src="/imgs/kafka-03-cluster/a19dcbb33b12d4cd72436f1d70bbb97c_MD5.jpg" style="display: block; width: 100%;" alt="Zookeeper 中的 Partition 信息存储结构">

具体的分区信息存储在上图中的 state 字段里，其数据结构示例如下：

<img src="/imgs/kafka-03-cluster/1e961a4730cbfaa040feb4163523429f_MD5.jpg" style="display: block; width: 100%;" alt="State 存储数据结构">

### 5.2 **Leader** 选举规则

在了解了副本状态的记录方式后，我们来看看 **Kafka** 如何进行 **Leader** 选举。其核心规则可以概括为八个字：**AR 顺序优先，ISR 存活条件过滤**。

<img src="/imgs/kafka-03-cluster/02c1987097bb6c19bfe971a3747d0306_MD5.jpg" style="display: block; width: 100%;" alt="Leader 选举规则">

| 规则 | 说明 |
| --- | --- |
| 排序依据 | 按 **AR** 列表中的顺序，排在越前的 **Broker** 优先级越高 |
| 存活过滤 | **候选 Broker 必须在 ISR 列表中** |
| 选举结果 | **第一个同时满足"排在 AR 前面"且"在 ISR 中"的 Broker 成为 Leader** |

为了更好地理解这条规则，下面我们通过一个实验来验证它。

实验：创建 **Topic** → 模拟 **Broker** 故障 → 观察 **Leader** 变化

```shell
# 1、创建一个备份因子为 3 的 Topic。每个 Partition 有 3 个备份
[root@192-168-65-112 kafka_2.13-3.8.0]# bin/kafka-topics.sh --bootstrap-server worker1:9092 --create --replication-factor 3 --partitions 4 --topic secondTopic
Created topic secondTopic.

# 2、查看 Topic 的 Partition 情况。可以注意到，默认的 Leader 就是 Replicas 中的第一个。
[root@192-168-65-112 kafka_2.13-3.8.0]# bin/kafka-topics.sh --bootstrap-server worker1:9092 --describe --topic secondTopic
[2024-08-12 16:50:33,594] WARN [AdminClient clientId=adminclient-1] The DescribeTopicPartitions API is not supported, using Metadata API to describe topics. (org.apache.kafka.clients.admin.KafkaAdminClient)
Topic: secondTopic      TopicId: DNNw-hXqQCOW61shM7zZ2Q PartitionCount: 4       ReplicationFactor: 3    Configs:
        Topic: secondTopic      Partition: 0    Leader: 1       Replicas: 1,0,2 Isr: 1,0,2      Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 1    Leader: 0       Replicas: 0,2,1 Isr: 0,2,1      Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 2    Leader: 2       Replicas: 2,1,0 Isr: 2,1,0      Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 3    Leader: 1       Replicas: 1,2,0 Isr: 1,2,0      Elr: N/A        LastKnownElr: N/A

# 3、在 worker3 上停掉 kafka 服务
[root@192-168-65-193 kafka_2.13-3.8.0]# bin/kafka-server-stop.sh

# 4、再次查看 SecondTopic 上的 Partition 分区情况，Leader 依然是 Replicas 中的第一个存活的 Broker。
[root@192-168-65-112 kafka_2.13-3.8.0]# bin/kafka-topics.sh --bootstrap-server worker1:9092 --describe --topic secondTopic
[2024-08-12 16:52:51,510] WARN [AdminClient clientId=adminclient-1] The DescribeTopicPartitions API is not supported, using Metadata API to describe topics. (org.apache.kafka.clients.admin.KafkaAdminClient)
Topic: secondTopic      TopicId: DNNw-hXqQCOW61shM7zZ2Q PartitionCount: 4       ReplicationFactor: 3    Configs:
        Topic: secondTopic      Partition: 0    Leader: 1       Replicas: 1,0,2 Isr: 1,2        Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 1    Leader: 2       Replicas: 0,2,1 Isr: 2,1        Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 2    Leader: 2       Replicas: 2,1,0 Isr: 2,1        Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 3    Leader: 1       Replicas: 1,2,0 Isr: 1,2        Elr: N/A        LastKnownElr: N/A
```

实验步骤概括：

| 步骤                | 操作                                                                                    | 目的                                  |
| ----------------- | ------------------------------------------------------------------------------------- | ----------------------------------- |
| ① 创建 **Topic**    | **3** 个 **Broker**，**4** 个 **Partition**，这 **4** 个 **Partition** 的 **AR** 顺序依次为 [1,0,2]、[0,2,1]、[2,1,0]、[1,2,0] | 观察 **Leader** 初始分配是否遵循 **AR** 顺序优先  |
| ② 停止 **Broker 0** | 在 **Broker 0** 上停掉 **Kafka** 服务                                                       | **模拟 Broker 故障，触发 Leader 重新选举** |
| ③ 对比状态        | 查看故障前后各 **Partition** 的 **Leader** 和 **ISR** 变化                                       | **验证选举规则：AR 顺序优先 + ISR 存活过滤**   |

根据实验结果，我们可以整理出以下观察数据：

| **Partition**   | 初始 **Leader** | 初始 **AR**   | **Broker 0** 故障后 **Leader** | 说明                                |
| ----------- | --------- | ------- | ------------------- | --------------------------------- |
| **Partition 0** | **Broker 1**  | [1,0,2] | **Broker 1**            | **Broker 0** 故障，**Broker 1** 在 **ISR** 中，保持不变 |
| **Partition 1** | **Broker 0**  | [0,2,1] | **Broker 2**            | **Broker 0** 故障，按 **AR** 顺序选 **Broker 2**     |
| **Partition 2** | **Broker 2**  | [2,1,0] | **Broker 2**            | **Broker 0** 故障，**Broker 2** 在 **ISR** 中，保持不变 |
| **Partition 3** | **Broker 1**  | [1,2,0] | **Broker 1**            | **Broker 0** 故障，**Broker 1** 在 **ISR** 中，保持不变 |

从上述结果可以得出以下观察结论：

- **Broker 0** 停止后，从所有 **Partition** 的 **ISR** 中移除
- **Partition 1** 的原 **Leader**（**Broker 0**）故障后，按 **AR** 顺序选择了 **Broker 2** 作为新 **Leader**
- 验证了选举规则：**AR 顺序优先 + ISR 存活过滤**

选举完成后，**Zookeeper** 中的元数据会立即更新。下面让我们看一下选举结果在 **Zookeeper** 中的存储情况：

```shell
# Zookeeper 上的 /brokers/topics/secondTopic
{
  "partitions": {
    "0": [1, 0, 2],
    "1": [0, 2, 1],
    "2": [2, 1, 0],
    "3": [1, 2, 0]
  },
  "topic_id": "DNNw-hXqQCOW61shM7zZ2Q",
  "adding_replicas": {},
  "removing_replicas": {},
  "version": 3
}
```

从上面的存储结构中可以看出：该节点只存储各 **Partition** 的 **AR**（副本分配顺序），不存储当前的 **Leader** 和 **ISR**。**Leader** 和 **ISR** 是运行时动态计算并维护的。

### 5.3 **Leader** 自动平衡

#### (1) 问题产生

了解了 **Leader** 选举规则后，我们还需要关注一个实际运行中的问题：**Leader** 负载不均。

由于 **Leader** 负责客户端读写和数据同步，其负载明显高于 **Follower**。**Kafka** 默认将 **Leader** 均匀分配到不同 **Broker**，但这个理想状态会被 **Broker** 故障打破。当故障发生时，**Leader** 重新选举可能导致负载集中到少数 **Broker** 上，产生负载不均的问题。

#### (2) 自动平衡

针对上述负载不均问题，**Kafka** 提供了 **Leader 自动平衡**机制来维持集群的负载均衡：

<img src="/imgs/kafka-03-cluster/a1fae39b87c9da80322ed1ad8be86efc_MD5.jpg" style="display: block; width: 100%;" alt="Leader 自动平衡机制">

| 工作机制   | 说明                                                    |
| ------ | ----------------------------------------------------- |
| 理想状态 | **AR** 中的第一个节点即为 **Leader**，称为 **Preferred Leader**         |
| 检测方式 | **Controller** 定期检查所有 **Broker** 的 **Leader** 分布情况            |
| 触发条件 | **某个 Broker** 上的不均衡 **Leader** 比例超过阈值                      |

官网截图如下：

<img src="/imgs/kafka-03-cluster/27e0a66eb4fd5e51fe0bc3e2acf42620_MD5.jpg" style="display: block; width: 100%;" alt="Leader 自动平衡官网配置说明">

#### (3) 自动平衡配置

要让自动平衡机制正常工作，需要在 `server.properties` 中配置相关参数。这些参数控制着平衡的触发时机和检测频率：

| 参数                                            | 默认值    | 说明         |
| --------------------------------------------- | ------ | ---------- |
| `auto.leader.rebalance.enable`            | `true` | 自动平衡开关     |
| `leader.imbalance.check.interval.seconds` | `300`  | 检测间隔（秒）    |
| `leader.imbalance.per.broker.percentage`  | `10`   | 不均衡比例阈值（%） |

参数详情（来自 Kafka 官方文档）：

```text
# 1、自平衡开关，默认 true
auto.leader.rebalance.enable
Enables auto leader balancing. A background thread checks the distribution of partition leaders at regular intervals, configurable by `leader.imbalance.check.interval.seconds`. If the leader imbalance exceeds `leader.imbalance.per.broker.percentage`, leader rebalance to the preferred leader for partitions is triggered.
Type:	boolean
Default:	true
Valid Values:
Importance:	high
Update Mode:	read-only

# 2、自平衡扫描间隔
leader.imbalance.check.interval.seconds
The frequency with which the partition rebalance check is triggered by the controller
Type:	long
Default:	300
Valid Values:	[1,...]
Importance:	high
Update Mode:	read-only

# 3、自平衡触发比例
leader.imbalance.per.broker.percentage
The ratio of leader imbalance allowed per broker. The controller would trigger a leader balance if it goes above this value per broker. The value is specified in percentage.
Type:	int
Default:	10
Valid Values:
Importance:	high
Update Mode:	read-only
```

注意：配置更改需同步到**所有** Broker 并**重启** 才能生效。

#### (4) 手动触发平衡

除了依赖自动平衡机制外，管理员也可以根据实际需要，通过脚本手动触发指定 **Partition** 的 **Leader** 平衡。这种方式更加灵活，可以在特定场景下精确控制平衡时机：

```shell
# 启动 worker3 上的 Kafka 服务，Broker 0 上线
# secondTopic 的 partition 1 不是理想状态，理想的 Leader 应该是 Replicas 中的 0
[root@192-168-65-112 kafka_2.13-3.8.0]# bin/kafka-topics.sh --bootstrap-server worker1:9092 --describe --topic secondTopic
[2024-08-12 17:16:48,966] WARN [AdminClient clientId=adminclient-1] The DescribeTopicPartitions API is not supported, using Metadata API to describe topics. (org.apache.kafka.clients.admin.KafkaAdminClient)
Topic: secondTopic      TopicId: DNNw-hXqQCOW61shM7zZ2Q PartitionCount: 4       ReplicationFactor: 3    Configs:
        Topic: secondTopic      Partition: 0    Leader: 1       Replicas: 1,0,2 Isr: 1,2,0      Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 1    Leader: 2       Replicas: 0,2,1 Isr: 2,1,0      Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 2    Leader: 2       Replicas: 2,1,0 Isr: 2,1,0      Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 3    Leader: 1       Replicas: 1,2,0 Isr: 1,2,0      Elr: N/A        LastKnownElr: N/A

# 手动触发 partition 1 的 Leader 平衡
[root@192-168-65-112 kafka_2.13-3.8.0]# bin/kafka-leader-election.sh --bootstrap-server worker1:9092 --election-type preferred --topic secondTopic --partition 1
Valid replica already elected for partitions secondTopic-1

# 平衡后再次查看，partition 1 的 Leader 变为 0（Preferred Leader）
[root@192-168-65-112 kafka_2.13-3.8.0]# bin/kafka-topics.sh --bootstrap-server worker1:9092 --describe --topic secondTopic
[2024-08-12 17:18:50,015] WARN [AdminClient clientId=adminclient-1] The DescribeTopicPartitions API is not supported, using Metadata API to describe topics. (org.apache.kafka.clients.admin.KafkaAdminClient)
Topic: secondTopic      TopicId: DNNw-hXqQCOW61shM7zZ2Q PartitionCount: 4       ReplicationFactor: 3    Configs:
        Topic: secondTopic      Partition: 0    Leader: 1       Replicas: 1,0,2 Isr: 1,2,0      Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 1    Leader: 0       Replicas: 0,2,1 Isr: 2,1,0      Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 2    Leader: 2       Replicas: 2,1,0 Isr: 2,1,0      Elr: N/A        LastKnownElr: N/A
        Topic: secondTopic      Partition: 3    Leader: 1       Replicas: 1,2,0 Isr: 1,2,0      Elr: N/A        LastKnownElr: N/A
```

#### (5) 生产环境建议

虽然 **Leader** 平衡机制能够维持集群负载均衡，但需要注意这个操作涉及大量数据复制，属于重量级操作。对于对性能敏感的生产环境，为了避免平衡过程影响业务性能，建议采用以下策略：

| 策略          | 做法                                                  |
| ----------- | --------------------------------------------------- |
| **关闭自动平衡**  | 将 **`auto.leader.rebalance.enable`** 设为 `false` |
| **低峰期手动触发** | 在业务低峰期通过 `kafka-leader-election.sh` 手动执行            |
| **控制影响范围**  | **避免平衡过程对在线业务造成性能抖动**                                   |

## 6. **Partition** 故障恢复机制

### 6.1 问题描述

**Kafka** 运行在分布式环境中，**Broker** 故障是常态。每个 **Partition** 通过多个副本同步数据，**Leader** 负责接收 **Producer** 消息并同步给 **Follower**。

在这种架构下，一个关键问题随之而来：当 **Leader** 所在 **Broker** 宕机时，**Kafka** 会从 **ISR** 中重新选举 **Leader**——但此时已写入但尚未完全同步的消息该如何处理？这就引出了 **LEO** 和 **HW** 两个关键值的作用。

### 6.2 **LEO** 与 **HW**

#### (1) 概念

**Kafka** 通过 **LEO** 和 **HW** 两个值来跟踪副本间的数据同步进度：

| 概念     | 全称              | 含义                                           |
| ------ | --------------- | -------------------------------------------- |
| **LEO** | **Log End Offset**  | 每个 **Partition** 自己保存的最后一个消息的 offset         |
| **HW**  | **High Watermark** | 同一个 **Partition** 的 **ISR** 中**最小的 LEO**，代表所有副本都已同步的消息边界 |

这两个概念的具体含义，可以通过下图直观理解：

<img src="/imgs/kafka-03-cluster/774f77ead5936343225e764fb785d60e_MD5.jpg" style="display: block; width: 100%;" alt="LEO 和 HW 概念示意图">

#### (2) 作用

##### ① **LEO** 的作用

每个副本独立维护自己的 **LEO**。**Leader** 写入消息、**Follower** 拉取消息时，各自的 **LEO** 自增。**Leader** 通过 **Follower** 上报的 **LEO** 掌握各副本的同步进度。这一机制确保了 **Leader** 能够实时了解整个副本集群的同步状态，为后续的 **HW** 计算提供基础数据。

##### ② **HW** 的作用

在理解 **LEO** 如何记录各副本同步进度的基础上，**HW** 进一步定义了数据的安全边界。**HW** 由 **Leader** 统一计算并同步给所有 **Follower**，计算规则很简单：取 **ISR** 中所有副本 **LEO** 的最小值。

**HW 标记了数据安全边界**：

| 区域 | 同步状态 | 消费者可见性 |
| --- | --- | --- |
| **HW 之前** | 消息已在所有副本间完成同步 | **可见** |
| **HW 之后** | 消息尚未完全同步 | **不可见** |

结合上图可以看到，**4** 号及之后的消息虽已写入 **Leader**，但 **HW** 尚未推进到该位置，因此消费者暂时无法拉取这些消息。这种设计确保了消费者只能读取到已在所有副本间完成同步的数据，避免了因 **Leader** 故障导致的数据不一致问题。

> 说明：**HW** 是服务端的副本同步机制，与 **Producer** 的 **acks** 参数（控制写入确认级别）属于不同维度，两者分别保障副本间一致性和写入可靠性，不要混淆。

### 6.2 **Follower** 故障处理

了解了 **LEO** 和 **HW** 的概念后，我们来看 **Follower** 发生故障时的处理流程。**Follower** 故障不会影响消息写入，因为 **Leader** 仍然正常工作，只是同步副本数量减少，**HW** 推进速度可能变慢。整个处理流程分为三个步骤：

| 步骤            | 动作                       | 说明                                                                          |
| ------------- | ------------------------ | --------------------------------------------------------------------------- |
| ① 移出 **ISR** | **Leader** 更新 **ISR**      | **故障 Follower** 超时后，**Leader** 将其临时移出 **ISR**，其余副本继续正常同步                   |
| ② 日志截断与恢复     | **Follower** 本地恢复         | **Follower** 恢复后，截断本地日志中高于故障前记录的 **HW** 的部分，再从 **HW** 位置向 **Leader** 拉取并重新同步 |
| ③ 重新加入 **ISR** | **Leader** 更新 **ISR**      | 该 **Follower** 的 **LEO** 追上 **Leader** 的 **HW** 后，**Leader** 将其重新加入 **ISR** |

具体流程如下图所示：

<img src="/imgs/kafka-03-cluster/dae95b12b514b647412f7676e10fd0bf_MD5.jpg" style="display: block; width: 100%;" alt="Follower 故障恢复流程">

### 6.3 **Leader** 故障处理

**Leader** 故障的影响范围更广，除了需要重新选举新 Leader 外，还涉及未同步消息的处理，整个恢复流程相对复杂：

| 步骤              | 动作                        | 说明                                                                                                    |
| --------------- | ------------------------- | ----------------------------------------------------------------------------------------------------- |
| ① 重新选举        | **Controller** 选举新 **Leader**   | **从 ISR 中选出一个 Follower 提升为新 Leader。由于部分消息可能尚未同步完成，新 Leader 的 LEO 可能低于原 Leader，需要后续截断来保证一致性** |
| ② 其余 Follower 截断日志 | 其余 **Follower** 截断日志       | **Kafka 以新 Leader 的数据为准，其余 Follower 将本地日志中高于切换前 HW 的部分全部截断，再从新 Leader 拉取并重新同步**                   |
| ③ 旧 Leader 恢复    | 原 **Leader** 截断并同步         | **旧 Leader 恢复后降级为普通 Follower，执行同样的日志截断，再从新 Leader 同步数据**                                                  |

流程如下图所示：

<img src="/imgs/kafka-03-cluster/86c7f83779a5179f255101520a4d42c8_MD5.jpg" style="display: block; width: 100%;" alt="Leader 故障恢复流程">

### 6.4 恢复过程中的消息丢失

#### (1) 问题及原因

在 **Leader** 故障恢复过程中，所有副本都会截断到 **HW** 位置。这就意味着 **HW 之后未完全同步的消息会被丢弃**。如上图所示，**Partition 0** 的 **4、5、6、7** 号消息因未同步到新 **Leader** 而丢失。

**这是 Kafka 在一致性与可用性之间的取舍——优先保证副本间数据一致性，代价是未同步消息可能丢失**。

> 技术对比：**Kafka** 为保证高性能，在不稳定场景下会牺牲部分数据安全性。**RocketMQ** 则优先保证数据安全，学习时可对比理解。

#### (2) 补救方法

针对上述消息丢失问题，可以通过调整 **Producer** 配置来降低风险：

> 将 **Producer** 的 **acks** 参数设置为 **`all`**（或 **`-1`**），确保消息写入所有 **ISR** 副本后再返回确认，**Producer** 根据返回值自行判断是否需要重发。这样即使 **Leader** 故障，未确认的消息也不会丢失。

#### (3) **HW** 一致性问题

上述截断机制的有效性依赖于一个关键前提——各 **Broker** 记录的 **HW** 必须保持一致。然而在实际的分布式环境中，**HW** 和 **LEO** 本身也是需要在多个 **Broker** 间同步的值，那么如何保证 **HW** 在多个 **Broker** 间的一致性呢？如果 **HW** 本身就不一致，基于 **HW** 的截断机制是否还会有效？

## 7. **HW** 一致性保障：**Epoch** 机制

**HW** 机制保证各 **Partition** 的数据基本同步，但 **HW** 值在不同副本之间并不总是保持一致。本节分析 **HW 不一致** 的产生原因，下一节介绍 **Epoch** 机制如何解决。

### 7.1 **HW** 不一致的产生

#### (1) 问题根源

要理解 **HW** 不一致问题，首先需要了解 **HW** 的更新机制。**HW** 的更新依赖 **Follower** 上报 **LEO**，这三个动作之间存在时间窗口。更新过程按以下顺序循环执行：

| 步骤 | 动作 | 说明 |
| --- | --- | --- |
| ① **LEO** 上报 | **Follower** → **Leader** | **Follower** 发起拉取请求时携带自己的 **LEO** |
| ② **HW** 计算 | **Leader** 本地 | **Leader** 收到请求后，基于所有 **Follower** 的 **LEO** 计算 **HW** |
| ③ **HW** 返回 | **Leader** → **Follower** | **Leader** 在拉取响应中携带 **HW** 返回 |

**关键问题**：**Leader** 在第 ② 步计算出新的 **HW** 后，**Follower** 必须等到下一次拉取请求（第 ① 步）才能获知这个新值。这个延迟窗口导致 **HW 在不同副本间出现暂时性不一致**。

| 场景 | **HW** 状态 | 结果 |
| --- | --- | --- |
| 正常运行 | 经过若干轮循环后趋于一致 | ✓ 数据同步完成 |
| **Leader** 切换 | 各副本按照各自记录的 **HW** 进行截断 | ✗ **已同步数据可能被错误删除** |

#### (2) 例子演示

为了更直观地理解这个问题，下面通过一个具体例子演示 **HW** 不一致的产生过程。

##### ① 故障背景

下图展示了 **HW** 不一致的典型场景：

<img src="/imgs/kafka-03-cluster/1191e76c37d590f5227d4dc63695e8e9_MD5.jpg" style="display: block; width: 100%;" alt="HW 不一致问题场景">

场景假设：**ISR** 中有 **Leader**（**Broker0**）和 **Follower**（**Broker1**）两个副本。**Leader** 写入两条新消息（**offset 3** 和 **4**），并同步到 **Follower**。此时发生了一次完整的 **HW** 更新流程：

| 步骤 | 动作 | 说明 |
| --- | --- | --- |
| ① **Follower** 拉取 | **Broker1** 发起拉取请求，携带 **LEO=3** | 表示 **offset 0-2** 已同步 |
| ② **Leader** 计算 **HW** | **Broker0** 收到请求，计算 **min(LEO=5, LEO=3) = 3** | 将 **HW** 更新为 **3** |
| ③ **Follower** 再次拉取 | **Broker1** 继续拉取，携带 **LEO=5** | 表示 **offset 0-4** 都已拉取 |
| ④ **Leader** 再次计算 **HW** | **Broker0** 计算 **min(LEO=5, LEO=5) = 5** | 将 **HW** 更新为 **5**，并在响应中返回 |

**关键问题**：在第 ④ 步，**Leader** 已将 **HW** 更新为 **5**，但这个新值要等到下一次拉取请求才能传递给 **Follower**。**若此时 Leader 突然故障，Follower 的 HW 仍然是旧值 3**，这为后续的数据丢失埋下了隐患。

此时各副本状态：**Follower** 的 **LEO** 已是 **5**，但 **HW** 仍为 **3**。**Leader** 已将 **HW** 推进到 **5**，但这个新值还未传递给 **Follower**。两者 **HW** 相差 **2** 个 **offset**。

| **Broker** | 角色 | **ISR** 状态 | **LEO** | **HW** |
| --- | --- | --- | --- | --- |
| **Broker0** | **Leader** | ✓ 在 **ISR** 中 | **5** | **5** |
| **Broker1** | **Follower** | ✓ 在 **ISR** 中 | **5** | **3** |

总结这个过程，如下图

<img src="/imgs/kafka-03-cluster/c70c0529fff24b6c335212cad27ffd9f_MD5.jpg" style="display: block; width: 100%;" alt="HW 更新延迟过程">


这个问题与之前讨论的 **6.4** 节有本质区别，需要特别加以区分：

| 场景 | 同步状态 | 丢失原因 | 是否预期 |
| --- | --- | --- | --- |
| **6.4** 节（正常丢失） | **offset 3-4** 确实未同步到新 **Leader** | 未同步的消息被截断 | ✓ 预期行为 |
| 本节（异常丢失） | **offset 3-4** 已在所有 **ISR** 副本间同步 | **因 HW 不一致被错误截断** | ✗ 非预期行为 |

##### ② 故障触发

当上述 **HW** 不一致的状态出现时，如果恰好发生 **Leader** 故障，就会触发错误的数据截断。

假设此时 **Leader**（**Broker0**）突然宕机，**Controller** 从 **ISR** 中选举 **Broker1** 为新 **Leader**。恢复流程如下：

| 步骤 | 对象 | 动作 | 结果 |
| --- | --- | --- | --- |
| ① 新 **Leader** 截断日志 | **Broker1**（新 **Leader**） | 依据本地 **HW=3** 判断数据边界 | **将日志截断到 offset 3** |
| ② 其他副本对齐 | 其他副本（如 **Broker2**） | 向新 **Leader** 拉取，收到 **HW=3** | 也根据 **HW=3** 将日志截断到 **offset 3** |

截断后的状态：

| **Broker** | 角色 | **LEO** | **HW** |
| --- | --- | --- | --- |
| ~~**Broker0**~~ | ~~旧 **Leader**~~ | ~~宕机~~ | ~~5~~ |
| **Broker1** | 新 **Leader** | **3** | **3** |
| **Broker2** | **Follower**（如果存在） | **3** | **3** |

##### ③ 故障后果

经过上述错误截断后，原本已同步的数据被永久删除，导致数据丢失。

<img src="/imgs/kafka-03-cluster/05f75067c8b3160884872c8a0401c0c3_MD5.jpg" style="display: block; width: 100%;" alt="HW 不一致导致数据丢失">

⚠️ **offset 3** 和 **4** 的消息被永久丢失——这两条消息：

| 副本 | **HW** 值 | 消息状态 | 结果 |
| --- | --- | --- | --- |
| 旧 **Leader** | **HW=5** | "已提交" | 客户端可能已消费 |
| 新 **Leader** | **HW=3** | "未提交" | **在截断时被删除** |

**问题本质**：综合上述分析，**HW** 不一致问题的根源在于：

| 问题方面 | 详细机制 | 导致结果 |
| --- | --- | --- |
| **HW** 更新的延迟性 | **Leader** 更新 **HW** 后，**Follower** 要等到下一次拉取请求才能获知 | 这个窗口内 **HW** 不一致 |
| **截断依据的缺陷** | **新 Leader 截断日志时依赖的是本地滞后的 HW，而非旧 Leader 上最新的 HW** | **导致已同步数据被错误删除** |

> 说明：这个问题在 **Kafka 0.11** 版本之前比较严重。引入 **Leader Epoch 机制**后，通过版本号追踪数据边界，避免了基于滞后的 **HW** 进行截断。关于 **Epoch** 机制的具体实现，将在下一节详细介绍。

### 7.2 **Epoch** 机制

为解决上述问题，**Kafka** 引入了 **Leader Epoch 机制**。其核心思路是：不再单纯依赖 **HW** 这一可能滞后的值来判断截断位置，而是用 **Leader** 版本号精确标记每段数据的归属 **Leader**，从而准确识别合法的数据边界。

<img src="/imgs/kafka-03-cluster/93966498803a581f39d5855b5e3a1552_MD5.jpg" style="display: block; width: 100%;" alt="Leader Epoch 机制">

#### (1) 基本概念

**Leader Epoch** 是一个单调递增的版本号，每次 **Leader** 发生变更时自动加 **1**。每个 **Epoch** 记录包含一条关键信息：**(epoch 版本号, 该 Leader 任期内写入的第一条消息 offset)**。

> 可以将其理解为 **Leader** 的交接日志——每次换 **Leader** 都记一笔，写明"第 **N** 任 **Leader** 从 **offset X** 开始负责"。

为便于理解，下面通过一个具体示例说明：假设某 **Partition** 经历了三次 **Leader** 切换，其 **Epoch** 记录如下：

| **Epoch** | 起始 **Offset** | 含义 |
| ----- | ----------- | ---- |
| **0** | **0** | 第 **0** 任 **Leader**，从 **offset 0** 开始写入 |
| **1** | **100** | 第 **1** 任 **Leader**，从 **offset 100** 开始写入（说明 **0** 任在 **offset 100** 时被替换） |
| **2** | **250** | 第 **2** 任 **Leader**，从 **offset 250** 开始写入（说明 **1** 任在 **offset 250** 时被替换） |

根据这一机制，拥有最新 **Epoch** 的副本即为当前有效 **Leader**，其余副本则为过期 **Leader**。

#### (2) 工作流程

<img src="/imgs/kafka-03-cluster/b3e74a05cd930529a3647bd1078e909a_MD5.jpg" style="display: block; width: 100%;" alt="Epoch 机制工作流程">

了解基本概念后，我们来看 **Epoch** 机制的完整工作流程：

| 步骤 | 动作 | 说明 |
| ---- | ---- | ---- |
| ① 记录 | **Leader** 上任时新增一条 **Epoch** 记录 | 记录 **(epoch, startOffset)**，例如 **(2, 250)** 表示第 **2** 任 **Leader** 从 **offset 250** 开始负责 |
| ② 持久化 | **Epoch 记录写入 leader-epoch-checkpoint 文件** | 同时保存在内存和本地文件中，防止 **Broker** 重启后丢失 |
| ③ 同步 | **Follower** 从 **Leader** 拉取 **Epoch** 信息 | **Leader** 变更时，新 **Leader** 读取已有 **Epoch** 记录并追加自己的记录，**Follower** 通过 **Fetch** 请求同步 |
| ④ 截断 | **Follower 恢复时依据 Epoch 判断截断位置** | **不再依赖本地 HW，而是根据 Leader 返回的最新 Epoch 对应的 offset 来确定截断点** |

#### (3) 回到 **7.1** 的例子：**Epoch** 如何避免数据丢失

现在让我们回到 **7.1** 节的场景，看看 **Epoch** 机制如何避免数据丢失。

回顾之前的问题场景：当 **Leader**（**Broker0**）宕机后，**Follower**（**Broker1**）因本地 **HW=3** 而错误截断了 **offset 3-4** 的已同步数据。如果引入 **Epoch** 机制，处理流程会发生根本性变化：

| 步骤 | 对象 | 动作 | 结果 |
| ---- | ---- | ---- | ---- |
| ① 旧 **Leader** 记录 | **Broker0**（旧 **Leader**） | 写入消息 **offset 3-4** 时，**Epoch** 记录为 **(0, 0)** | 表明 **offset 0** 开始的数据都由第 **0** 任 **Leader** 写入 |
| ② 新 **Leader** 上任 | **Broker1**（新 **Leader**） | 上任时新增 **Epoch (1, 5)**，表示自己从 **offset 5** 开始写入 | 如果后续有新消息，从 **offset 5** 开始编号 |
| ③ **Follower** 截断 | **Broker1**（新 **Leader**） | **不再按 HW=3 截断，而是向旧 Leader（或从本地 Epoch 记录）查询最新 Epoch 对应的 offset** | 旧 **Leader** 返回 **Epoch=0** 的截止 **offset** 为 **5** |
| ④ 保留数据 | **Broker1**（新 **Leader**） | **根据 Epoch 信息判断 offset 0-4 都是合法数据，不做截断** | ✅ **offset 3-4** 的消息被保留，数据不再丢失 |

**关键区别**：旧方案用 **HW**（一个可能滞后的同步进度值）来判断截断位置，而新方案用 **Epoch**（一个精确的 **Leader** 版本记录）来判断数据归属，从而从根本上避免了因 **HW** 不一致导致的数据丢失。

#### (4) **leader-epoch-checkpoint** 文件

在实际应用中，**Epoch** 信息通过 **leader-epoch-checkpoint** 文件进行持久化存储。下面查看一个文件样例：

该文件保存在每个 **Partition** 对应的本地日志目录中，是一个可直接查看的文本文件：

```shell
[root@192-168-65-193 secondTopic-1]# pwd
/app/kafka/logs/secondTopic-1
[root@192-168-65-193 secondTopic-1]# cat leader-epoch-checkpoint
0
1
2 0
3 197
```

文件格式说明如下：

| 行号      | 内容             | 说明 |
| ------- | -------------- | -- |
| 第 1 行   | `0`            | 文件格式版本号 |
| 第 2 行   | `1`            | 后续记录的条数 |
| 第 3 行起  | `epoch offset` | 每行一条 **Epoch** 记录 |

每个记录包含两个字段：

| 字段      | 说明 |
| ------- | ---------------------------------------- |
| **epoch**  | **Leader** 的 **Epoch** 版本号，从 **0** 开始，每次 **Leader** 变更时 **+1** |
| **offset** | 该 **Epoch** 版本的 **Leader** 写入的第一条消息的 **offset** |

以上示例中，`secondTopic` 的 **partition 1** 经历了两次 **Leader** 切换，因此 **Epoch** 更新为 **2**；由于切换时尚未写入消息，所以 **offset** 为 **0**。

## 8. 总结

通过前面的学习，我们已经了解了 **Kafka** 集群工作的各个环节。现在让我们从整体视角回顾一下：本文以 **Zookeeper** 元数据为入口，依次讲解了 **Controller** 选举 → **Leader Partition** 选举 → 故障恢复 → 数据一致性保障这条完整的 **Kafka** 集群工作链路。

在这个过程中，读者可以在以下三个层次上获得系统性的认知提升：

| 认知层次 | 收获 |
| --- | --- |
| 理解集群设计基础 | 掌握状态与业务数据分离的设计思路，理解 **Zookeeper** 在集群中扮演的角色 |
| 掌握选举与恢复机制 | 理解 **Controller** 选举、**Leader Partition** 选举规则（**AR** 顺序 + **ISR** 存活）、**Leader** 自动平衡，以及 **Follower**/**Leader** 故障的不同处理流程 |
| **理解数据一致性保障** | **理解 LEO、HW 在副本同步中的作用，以及 Epoch 机制如何弥补 HW 不一致问题** |

回看整篇文章，**Kafka** 的集群机制始终围绕一个目标：在 **Broker** 故障、网络不稳定等复杂环境下，保持 **Partition** 内多个副本间的**数据一致性**。正是这些一致性保证，支撑了 **Kafka** 在生产环境中持续提供**高吞吐**、**高可用**的消息服务。
