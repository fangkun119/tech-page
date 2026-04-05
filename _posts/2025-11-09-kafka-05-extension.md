---
title: Kafka深入05：功能扩展
author: fangkun119
date: 2025-11-09 12:00:00 +0800
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


## 1. 文档概要

本文系统讲解 **Kafka 企业级扩展能力**，聚焦以下内容：

| 知识模块           | 说明                                                             |
| -------------- | -------------------------------------------------------------- |
| **性能压测方法**     | **量化评估**集群吞吐与延迟，掌握**官方压测工具**使用方法与**结果解读**                      |
| **监控平台部署**     | **EFAK** 监控系统安装配置，实现**集群运行状态可视化**与**告警管理**                     |
| **KRaft 集群模式** | **去 Zookeeper 化**架构演进，理解 **Raft 协议**原理与**集群自主管理**              |
| **流式计算实践**     | **Kafka Streams** 快速入门，掌握 **KStream**、**KTable** 等流式计算**基础概念** |

基于上述知识模块，本文从**性能验证**、**运维监控**、**架构演进**、**实时计算**四个维度系统阐述 **Kafka 企业级扩展能力**，帮助读者建立完整的 **Kafka 生态认知**，为实际生产环境部署与应用提供参考。

为了直观展示各部分内容，下图提供了整体技术预览：

<img src="/imgs/kafka-05-extension/80ab9b4d99a4aa22a0e177f591efc80d_MD5.jpg" style="display: block; width: 100%;" alt="Kafka 企业级扩展能力概览">

如图所示，各技术模块的核心要点如下：

<!--
图片内容提取:
- 性能压测与基准测试
- EFAK (Kafka-Eagle) 监控平台
- 核心性能指标
- 建立集群性能基准
- 什么是EFAK
- KRaft集群：摆脱Zookeeper?
- 流式计算 (Kafka Streams)
- 为什么需要Zookeeper?
- KRaft核心优势
- 流式计算
- KRaft节点角色
- 监控统计
- 流式计算
- KRaft关键配置项
- 拓扑结构
- 核心功能
- 开发配置选择

图片概括:
这张图片展示了Kafka扩展技术的三大核心方向：性能优化方面包含压测与基准测试；运维监控方面介绍EFAK监控平台及核心指标；架构演进方面阐述KRaft模式如何替代Zookeeper，以及流式计算(Kafka Streams)的应用场景，涵盖了从集群管理到开发配置的完整技术栈。
-->

## 2. Kafka 性能压测

Kafka 提供了官方性能压测工具，用于量化评估集群的**吞吐能力**与**延迟特性**，是验证集群配置有效性的基准测试手段。

<img src="/imgs/kafka-05-extension/9b2768f34fbcdaf6cdbd2328ac070df5_MD5.jpg" style="display: block; width: 100%;" alt="Kafka 性能压测工具">

<!--
图片内容提取:
**测试命令参数:**
- `bin/kafka-producer-perf-test.sh`
- `--topic test`
- `--num-record 1000000`
- `--record-size 1024`
- `--throughput -1`
- `--producer-props`
- `bootstrap.servers=worker1:9092`
- `acks=1`

**性能指标:**
- 吞吐量：`80,560 records/sec`、`78.67 MB/sec`
- 平均延迟：`237.82 ms`
- 最大延迟：`1145.00 ms`
- 延迟分位值：P50: `165 ms`、P95: `699 ms`、P99: `998 ms`、P99.9: `1113 ms`

**说明备注:**
- 以此基准测试测量当前服务端配置是否充足，定位集群吞吐上限。

图片概括:
这张图片展示了Kafka生产者性能测试的结果，通过向`worker1:9092`服务器的`test`主题发送100万条1024字节的消息进行压力测试。测试结果显示系统吞吐量为每秒8万余条记录(约78.67MB)，平均延迟237.82毫秒，最大延迟1145毫秒。通过吞吐量仪表盘、延迟对比条形图和长尾延迟分位图，全面评估了Kafka集群在高负载下的处理能力和响应性能。
-->

### 2.1 工具概述

为了系统性地评估 Kafka 集群的性能表现，官方提供了专门的压力测试工具。该工具通过模拟真实生产环境的负载场景，能够精确测量集群的**吞吐能力**与**延迟特性**，成为验证集群配置有效性的重要基准测试手段。

**核心能力一览：**

| 工具特性 | 具体说明 | 技术价值 |
| --- | --- | --- |
| **脚本位置** | `bin/kafka-producer-perf-test.sh` | **开箱即用**，无需额外安装 |
| **性能指标** | **吞吐量**、**平均延迟**、**最大延迟**、**分位数延迟** | 多维度性能评估体系 |
| **典型场景** | **新集群上线验证**、**配置调优对比**、**容量规划评估**、**性能故障排查** | 覆盖**生产环境全生命周期** |

基于上述能力特性，该工具在实际应用中体现出显著价值：通过真实负载模拟获取可量化的性能数据，为**服务端参数调优**、**容量规划决策**、**性能瓶颈定位**提供数据支撑，确保生产环境的**高性能**与**高稳定性**。

### 2.2 命令详解

为了更好地使用压测工具，我们首先需要了解其命令结构。该工具的命令由三个核心部分组成：脚本路径、测试参数和生产者配置。

**命令结构说明：**

| 命令组成 | 核心要素 | 配置要点 |
| --- | --- | --- |
| 脚本路径 | `bin/kafka-producer-perf-test.sh` | 位于 Kafka 安装目录下 |
| 测试参数 | `--topic`、`--num-record`、`--record-size`、`--throughput` | 控制测试规模与数据特征 |
| 生产者配置 | `--producer-props` | 包含 `bootstrap.servers`、`acks` 等关键参数 |

理解了命令结构后，下面通过一个完整的示例来看实际执行效果：

**完整命令示例：**

```shell
[root@192-168-65-112 kafka_2.13-3.8.0]# bin/kafka-producer-perf-test.sh --topic test --num-record 1000000 --record-size 1024 --throughput -1 --producer-props bootstrap.servers=worker1:9092 acks=1
212281 records sent, 42456.2 records/sec (41.46 MB/sec), 559.9 ms avg latency, 1145.0 ms max latency.
463345 records sent, 92669.0 records/sec (90.50 MB/sec), 229.6 ms avg latency, 946.0 ms max latency.
1000000 records sent, 80560.702489 records/sec (78.67 MB/sec), 237.82 ms avg latency, 1145.00 ms max latency, 145 ms 50th, 699 ms 95th, 959 ms 99th, 1123 ms 99.9th.
```

从上述示例可以看到，压测过程中会实时输出吞吐进度，最终汇总所有性能指标。为了灵活应对不同的测试场景，我们需要掌握各个参数的具体含义和配置方法。

**关键参数配置：**

| 参数 | 参数作用 | 建议值 | 配置说明 |
| --- | --- | --- | --- |
| `--topic` | 指定测试 Topic | 生产环境目标 Topic | 必须提前创建，建议与实际业务 Topic 配置一致 |
| `--num-record` | 发送记录总数 | 1000000（100 万条） | 数值越大，测试结果越准确，但耗时更长 |
| `--record-size` | 单条记录大小（字节） | 1024（1KB） | 模拟实际消息大小，影响吞吐量测试结果 |
| `--throughput` | 目标吞吐量（条/秒） | -1（不限流） | 负值表示不限制，测试集群最大吞吐能力 |
| `--producer-props` | 生产者配置参数 | 根据实际环境配置 | 核心配置 `bootstrap.servers`、`acks` 等 |

> **执行提示**：压测过程中会实时输出吞吐进度，最终输出汇总性能指标，包括平均延迟、最大延迟及各分位数延迟。

### 2.3 结果解读

执行压测命令后，工具会输出包含多个性能指标的测试结果。这些指标从不同维度反映了集群的运行状态，理解它们的含义对于准确评估集群性能至关重要。

**输出指标说明：**

| 指标维度 | 具体指标 | 指标含义 | 评估标准 | 关注要点 |
| --- | --- | --- | --- | --- |
| **吞吐能力** | **records/sec** | 每秒发送记录数 | 数值越高越好 | **> 5万条/秒** 为达标基准 |
| | **MB/sec** | 每秒传输数据量 | 带宽利用率 | 反映实际网络吞吐情况 |
| **延迟特性** | **avg latency** | 平均响应延迟 | 越低越好 | **< 300ms** 为正常范围 |
| | **max latency** | 最大延迟峰值 | 关注极端情况 | **应 < 2000ms**，避免性能抖动 |
| | **50th/95th/99th** | 分位数延迟 | 稳定性评估 | **99th < 1000ms** 为佳 |

基于上述指标，生产环境建议目标是 **吞吐量 > 5万条/秒**，**99分位延迟 < 1000ms**。如测试结果未达到基准，需检查服务端配置（如 **num.network.threads**、**num.io.threads** 等）。

> **性能基准参考**：生产环境建议目标是 **吞吐量 > 5万条/秒**，**99分位延迟 < 1000ms**。如未达到基准，需检查服务端配置（如 **num.network.threads**、**num.io.threads** 等）。

### 2.4 应用场景

掌握了压测工具的使用方法和结果解读后，我们来看看它在实际生产环境中的具体应用。Kafka 压测工具在不同生产阶段发挥着关键作用，能够帮助我们做出数据驱动的决策。

| 应用场景 | 测试目的 | 核心关注点 | 验证标准 |
| --- | --- | --- | --- |
| **新集群上线** | 验证基础配置合理性 | 吞吐量是否达标 | 吞吐量 ≥ **5万条/秒** |
| **配置调优** | 评估参数调整效果 | 延迟是否改善 | 99分位延迟 < **1000ms** |
| **容量规划** | 评估集群承载能力 | 最大可持续吞吐 | 峰值负载下的稳定性 |
| **故障排查** | 定位性能瓶颈点 | 延迟突增原因 | 识别资源瓶颈或配置问题 |

在实际执行压测时，为确保测试结果的准确性与可靠性，建议遵循以下最佳实践：

> **压测执行建议**：
> - **模拟生产环境真实负载**：消息大小、并发度、Producer 配置应与生产环境一致
> - **多次测试取平均值**：避免单次测试的偶然性影响
> - **逐步增加负载**：从低负载逐步提升至目标负载，观察集群响应特征
> - **记录详细日志**：便于后续分析性能拐点和异常原因

## 3. Kafka 监控平台

生产环境通常会对Kafka搭建监控平台。**EFAK**（Eagle For Apache Kafka，原名 Kafka Eagle）是 Kafka 生态中**成熟的开源监控平台**，用于监控 Kafka 集群整体运行情况，在生产环境中得到广泛应用。

> **产品定位**：EFAK 是一款**可视化的 Kafka 集群管理系统**，提供从监控到运维的全栈管理能力，官网地址：[https://www.kafka-eagle.org/](https://www.kafka-eagle.org/)

<img src="/imgs/kafka-05-extension/f812f92022de3ea6bcf3e0d275030f9f_MD5.jpg" style="display: block; width: 100%;" alt="EFAK 监控平台界面">

<!--
图片内容提取:
EFAK
HOME
ABOUT
DOWNLOAD
DOCS
BLOGS
CONTACT
KAFKA EAGLE
A DISTRIBUTED AND HIGH-PERFORMANCE KAFKA MONITORING SYSTEM
DOWNLOAD (v3.0.1)

图片概括:
这是一张Kafka Eagle (现更名为EFAK)监控系统的官网首页截图。页面展示了一个分布式、高性能的Kafka监控系统,提供了导航菜单(首页、关于、下载、文档、博客、联系方式),并显示了当前的最新版本v3.0.1下载入口。EFAK是用于管理和监控Kafka集群的开源工具。
-->

### 3.1 EFAK 监控平台介绍

在 Kafka 生产环境中，搭建监控平台是保障集群稳定运行的关键环节。**EFAK**（Eagle For Apache Kafka，原名 Kafka Eagle）作为 Kafka 生态中成熟的开源监控平台，专门用于监控和管理 Kafka 集群的运行状态。

> **产品定位**：EFAK 是一款可视化的 Kafka 集群管理系统，提供从监控到运维的全方位管理能力，在生产环境中得到广泛应用。官网地址：[https://www.kafka-eagle.org/](https://www.kafka-eagle.org/)

<img src="/imgs/kafka-05-extension/a4437013dceea78637e854d2acca35da_MD5.jpg" style="display: block; width: 100%;" alt="EFAK 集群监控仪表板">

<!--
图片内容提取:
被监控端
Kafka Cluster
Zookeeper
Cluster
EFAK Web Server (ke.sh)
EFAK
底层数据 (推荐 MySQL)
客户端访问
访问端口 8048 | 默认凭证: admin / 123456

图片概括:
这张图展示了EFAK（Kafka监控平台）的系统架构。包括被监控的Kafka集群和Zookeeper集群，EFAK Web服务器通过ke.sh脚本启动，底层使用MySQL存储数据。用户通过Web Dashboard在8048端口访问，默认使用admin/123456凭证登录进行Kafka集群监控管理。
-->

为了全面了解 EFAK 的能力，下面通过核心功能矩阵展示其提供的全方位监控管理能力：

**核心功能矩阵：**

| 功能模块 | 具体能力 | 业务价值 |
| --- | --- | --- |
| **集群监控** | Broker、Topic、Partition 的实时状态监控 | 掌握集群整体运行健康状况 |
| **性能指标** | 生产/消费速率、延迟、吞吐量等关键指标 | 量化评估集群性能表现 |
| **消息管理** | 消息查询、Topic 创建与配置、Partition 管理 | 简化日常运维操作流程 |
| **消费组管理** | 消费组状态、Offset 管理、Lag 监控 | 快速定位消费异常问题 |
| **告警机制** | 自定义告警规则、多渠道通知 | 及时响应集群异常状况 |
| **可视化界面** | 友好的 Web Dashboard、图表化展示 | 降低运维门槛，提升管理效率 |

> **生产实践建议**：EFAK 特别适合中小规模 Kafka 集群的监控管理，其轻量级部署方式和丰富可视化能力，能够满足绝大多数生产环境的监控需求。

### 3.2 环境准备

在开始部署 EFAK 之前，需要先完成基础环境的准备工作。EFAK 的正常运行依赖于 **Java 运行环境**和**数据库服务**的支持。

#### (1) 获取安装包

首先，从 [EFAK 官网](https://www.kafka-eagle.org/) 下载最新稳定版本的安装包。

**资源清单**：

| 资源类别 | 文件名称 | 用途说明 |
| --- | --- | --- |
| **EFAK 安装包** | `efak-web-3.0.2-bin.tar.gz` | EFAK 运行环境，包含 Web 管理平台全部功能 |

> **版本说明**：本文以 **v3.0.2** 为例演示，生产环境建议使用最新稳定版本，以获得功能更新和安全补丁。

#### (2) 环境依赖

获取安装包后，接下来检查所需的环境依赖。EFAK 需要以下软件组件的支持：

| 依赖软件 | 作用 | 版本要求 | 部署要求 |
| --- | --- | --- | --- |
| **Java** | EFAK 运行基础环境 | **JDK 8+** | 必须预装并配置 JAVA_HOME |
| **数据库** | 存储监控数据和元数据 | **MySQL**（生产）或 **SQLite**（测试） | 需提前创建空白数据库 |
| **Zookeeper** | Kafka 集群协调服务 | 与 Kafka 集群适配版本 | 需提前启动并正常运行 |
| **Kafka** | 被监控的目标服务 | 2.x+ 或 3.x+ 系列 | 需提前启动并正常运行 |

> **生产建议**：优先选择 **MySQL** 作为数据存储，以获得更好的数据一致性和查询性能。

#### (3) 数据库准备

完成环境依赖检查后，还需要准备数据库服务。数据库用于存储 EFAK 的监控数据和元信息，具体准备事项如下：

| 准备事项 | 具体要求 | 注意事项 |
| --- | --- | --- |
| **数据库创建** | 需提前创建空白数据库（如 `ke`） | EFAK 首次启动时会自动初始化表结构，无需手动建表 |
| **权限配置** | 确保 EFAK 连接用户具有该数据库的完整操作权限 | 建议使用独立数据库用户，避免权限冲突 |
| **连接参数** | 准备好 MySQL 服务的连接地址、端口及用户凭据 | 配置格式：`jdbc:mysql://host:port/dbname` |

> **核心提示**：数据库无需手动初始化表结构，EFAK 首次启动时会自动创建所需的数据表。

关于 MySQL 服务的具体搭建过程，本文不再赘述，请参考相关数据库部署文档。

### 3.3 安装配置

本节介绍 EFAK 的完整安装流程，主要包含三个步骤：**解压安装包**、**修改配置文件**和**配置环境变量**。整个安装过程在 Linux 服务器上完成。

#### (1) 解压安装包

首先需要将 EFAK 安装包解压到指定目录。**解压操作说明**：

| 操作项 | 说明 |
| --- | --- |
| **源文件** | `efak-web-3.0.2-bin.tar.gz`（从官网下载的安装包） |
| **目标目录** | `/app/kafka/eagle`（建议的安装路径） |
| **解压目的** | 将 EFAK 安装包释放到指定目录，为后续配置做准备 |

**执行命令**：

```shell
tar -zxvf efak-web-3.0.2-bin.tar.gz -C /app/kafka/eagle
```

该命令将安装包解压到指定目录。**命令参数说明**：

| 参数 | 作用 |
| --- | --- |
| `-z` | 处理 **gzip** 压缩格式 |
| `-x` | **解压** 压缩包 |
| `-v` | 显示**详细过程**（可选，便于确认解压进度） |
| `-f` | 指定**文件名** |
| `-C` | 切换到**目标目录**后再解压 |

> **目录创建提示**：若目标目录 `/app/kafka/eagle` 不存在，需先使用 `mkdir -p /app/kafka/eagle` 创建，否则解压操作会失败。

#### (2) 配置文件修改

完成解压后，接下来需要修改配置文件以适配实际环境。EFAK 的核心配置文件为 `conf/system-config.properties`，该文件提供了完整的配置模板。

<img src="/imgs/kafka-05-extension/879872d95c0fffaf6d13b24136ad40c8_MD5.jpg" style="display: block; width: 100%;" alt="EFAK 配置文件系统配置">

<!--
图片内容提取:
- **集群寻址**: 定位目标 ZK 节点 (worker1:2181...)
- **Offset 存储**: 将消费位点强制存储于 Kafka 内部
- **conf/system-config.properties** (文件标题):
  - cluster1.zk.list
  - cluster1.zk.acl.enable
  - cluster1.efak.offset.storage
  - efak.url / efak.username
- **权限控制**: 决定是否开启 ZK ACL 校验
- **持久化库**: 指向已创建的 MySQL 服务
- **流程步骤**: 配置 KE_HOME 环境变量 → 启动 ZK/Kafka → 执行 /ke.sh start

图片概括:
这张图片展示了 Kafka 监控管理工具(EFAK/KE)的系统配置流程图。图中详细说明了配置文件`system-config.properties`的关键参数设置,包括 ZooKeeper 集群地址、ACL 权限控制、消费位点存储方式等核心配置项,并提供了从环境变量配置到服务启动的完整部署步骤。该图使用颜色编码的方框和箭头清晰地展示了各组件间的依赖关系和数据流向。
-->

**关键配置项说明**：

| 配置类别 | 核心配置项 | 配置说明 |
| --- | --- | --- |
| **集群配置** | **efak.zk.cluster.alias** | 集群别名标识，用于区分多个 Kafka 集群 |
| | **cluster1.zk.list** | **Zookeeper 集群地址列表**，格式：`host1:port,host2:port,host3:port` |
| **Offset 存储** | **cluster1.efak.offset.storage** | **Offset 存储方式**，可选值：`kafka`（推荐）或 `zk` |
| **数据库连接** | **efak.driver** | **MySQL JDBC 驱动类**，固定值：`com.mysql.cj.jdbc.Driver` |
| | **efak.url** | **MySQL 数据库连接地址**，包含 IP、端口、数据库名及编码参数 |
| | **efak.username** | **数据库连接用户名**，需具有完整操作权限 |
| | **efak.password** | **数据库连接密码**，与用户名对应 |

> **重要提示**：MySQL 数据库需**提前创建空白数据库**（如示例中的 `ke`），EFAK 首次启动时会**自动完成表结构初始化**，无需手动创建数据表。

**配置文件示例**：

```properties
######################################
# multi zookeeper & kafka cluster list
# Settings prefixed with 'kafka.eagle.' will be deprecated, use 'efak.' instead
######################################
# 指向Zookeeper地址
efak.zk.cluster.alias=cluster1
cluster1.zk.list=worker1:2181,worker2:2181,worker3:2181

######################################
# zookeeper enable acl
######################################
# Zookeeper权限控制
cluster1.zk.acl.enable=false
cluster1.zk.acl.schema=digest
#cluster1.zk.acl.username=test
#cluster1.zk.acl.password=test123

######################################
# kafka offset storage
######################################
# offset选择存在kafka中。
cluster1.efak.offset.storage=kafka
#cluster2.efak.offset.storage=zk

######################################
# kafka mysql jdbc driver address
######################################
#指向自己的MySQL服务。库需要提前创建
efak.driver=com.mysql.cj.jdbc.Driver
efak.url=jdbc:mysql://192.168.65.212:3306/ke?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull
efak.username=root
efak.password=root
```

配置文件修改完成后，最后一步是配置环境变量。

#### (3) 配置环境变量

配置 **EFAK 运行环境变量**，使 `ke.sh` 命令全局可用，方便后续管理和操作。

```shell
# 编辑环境变量配置文件
vi ~/.bash_profile

# 配置KE_HOME环境变量，并添加到PATH中
export KE_HOME=/app/kafka/eagle/efak-web-3.0.2
PATH=$PATH:$KE_HOME/bin:$HOME/.local/bin:$HOME/bin

# 让环境变量生效
source ~/.bash_profile
```

**环境变量配置说明**：

| 变量 | 配置值 | 核心作用 |
| --- | --- | --- |
| **KE_HOME** | `/app/kafka/eagle/efak-web-3.0.2` | 指向 **EFAK 安装根目录** |
| **PATH** | `$PATH:$KE_HOME/bin:...` | 将 `$KE_HOME/bin` 加入系统路径，支持**全局调用** `ke.sh` 命令 |

### 3.4 启动与访问

完成上述配置后，接下来需要启动 EFAK 服务并通过 Web 界面进行访问管理。

#### (1) 启动 EFAK 服务

在启动服务前，请确保 Zookeeper 和 Kafka 服务已经正常运行。

| 操作步骤 | 说明 |
| --- | --- |
| **前置条件** | 确保 **Zookeeper** 和 **Kafka** 服务已先启动 |
| **启动命令** | `ke.sh start` |
| **启动成功标志** | 看到 `EFAK Service has started success` 及访问地址提示 |

**启动执行示例：**

```shell
ke.sh start
```

**启动成功标志：**

```shell
-- 日志较长，看到以下内容表示服务启动成功
[2023-06-28 16:09:43] INFO: [Job done!]
Welcome to
   ______    ______    ___     __ __
  / ____/   / ____/   /   |   / //_/
 / __/     / /_      / /| |  / ,<
 / /___   / __/     / ___ | / /| |
/_____/   /_/       /_/  |_|/_/ |_|
( Eagle For Apache Kafka® )

Version v3.0.2 -- Copyright 2016-2022
*******************************************************************
* EFAK Service has started success.
* Welcome, Now you can visit 'http://192.168.232.128:8048'
* Account:admin ,Password:123456
*******************************************************************
* <Usage> ke.sh [start|status|stop|restart|stats] </Usage>
* <Usage> https://www.kafka-eagle.org/ </Usage>
*******************************************************************
```

**常用管理命令：**

| 命令 | 说明 |
| --- | --- |
| `ke.sh start` | 启动 EFAK 服务 |
| `ke.sh stop` | 停止 EFAK 服务 |
| `ke.sh restart` | 重启 EFAK 服务 |
| `ke.sh status` | 查看服务运行状态 |

#### (2) 访问 Web 管理界面

服务启动成功后，即可通过浏览器访问 EFAK 的 Web 管理界面。

**访问信息：**

| 访问项 | 配置值 |
| --- | --- |
| 访问地址 | `http://192.168.232.128:8048` |
| 服务端口 | 8048 |
| 默认用户名 | `admin` |
| 默认密码 | `123456` |

使用上述默认凭据登录后，即可进入 EFAK 的主控制台。

**登录界面预览：**

<img src="/imgs/kafka-05-extension/db47f1ea6c789f38e0c34e6ec35f27a5_MD5.jpg" style="display: block; width: 100%;" alt="EFAK 登录界面">

<!--
图片内容提取:
**界面标题与版本:**
- Kafdrop v3.0.2

**左侧导航菜单:**
- Dashboard (展开)
  - Overview (选中)
  - TV Dashboard
- MESSAGE
  - Topics
- APPLICATION
  - Consumers
- PERFORMANCE
  - Node
  - Monitor
- PLUGINS
  - Connector
- NOTIFICATION

**顶部指标栏:**
- BROKERS: 3
- TOPICS: 8
- ZOOKEEPERS: 3
- CONSUMERS: 2

**主要监控面板:**
- Broker MessageIn (折线图)
- Kafka OS
  - Memory: 0.0%
  - CPU: 0.00%
- Active Topics
- Byte In: 0.00 (B/sec)
- Topic LogSize: 150

图片概括:
这是一张Kafdrop v3.0.2 Kafka集群监控平台的Dashboard界面截图。界面采用深蓝色主题,左侧为导航菜单,主区域展示集群关键指标:3个Broker、8个Topic、3个Zookeeper节点和2个消费者。监控面板包含消息吞吐量折线图、系统资源使用率(CPU和内存均为0%),以及活跃Topic、字节流入速率(0.00 B/sec)和Topic日志大小(150)等实时性能数据。
-->

> **安全建议**：生产环境部署后，**务必修改默认密码**，避免安全风险。
>
> **扩展学习**：关于 EFAK 的更多用法（如集群部署、高可用配置），可参考 [官方文档](https://www.kafka-eagle.org/)。

## 4. KRaft 集群模式

### 4.1 KRaft 模式概述

#### (1) 介绍

##### ① KRaft 是什么

**Kafka KRaft**（Kafka Raft）是 Kafka 从 2.8.0 版本引入的**去 Zookeeper 化**集群架构。为了理解其价值，我们首先需要了解其发展历程。

> **核心本质**：KRaft 将原本由 Zookeeper 管理的集群元数据和控制权交还给 Kafka 集群自身管理，基于 **Raft 共识算法**实现分布式一致性。

**版本演进路径**：

| Kafka版本   | 时间       | 核心事件                                 | 状态定位          |
| --------- | -------- | ------------------------------------ | ------------- |
| **2.8.0** | 2021年    | **首次引入 KRaft 模式**                    | **早期预览版**（EA） |
| **3.3.1** | 2022年10月 | **KIP-833** 标记为 **Production Ready** | **生产可用**      |
| **3.x+**  | 持续迭代     | **官方规划完全替代 Zookeeper**               | **默认模式**      |

> **生产现状**：尽管 **3.3.1** 版本已标记为生产可用，但基于**分布式算法复杂性**的稳定性仍需时间验证，目前**大部分企业**仍使用 Zookeeper 模式。
##### ② Zookeeper 模式的痛点

了解了 KRaft 的发展历程后，让我们深入分析传统 Zookeeper 模式面临的核心问题：

| 痛点维度 | 核心问题 | 直接影响 |
| --- | --- | --- |
| **架构复杂度** | 需独立部署和维护 **Zookeeper** 集群 | **运维成本**激增，**故障点**增加 |
| **性能依赖** | Kafka 性能受 **Zookeeper** 波动影响 | 集群**稳定性不可控** |
| **云原生障碍** | **Zookeeper** 架构不适配云原生环境 | **限制** Kafka 容器化部署 |
| **扩展瓶颈** | **Zookeeper** 不适合海量元数据存储 | **限制 Partition** 规模扩展 |

> **演进本质**：**KRaft 模式**通过**去 Zookeeper 化**实现 Kafka 集群的**自主管理**，从根本上消除外部依赖带来的架构制约。

##### ③ KRaft 模式的改进

面对上述痛点，KRaft 模式提供了全面的解决方案。**KRaft** 让 Kafka **消除外部依赖**，全面拥抱云原生架构：

<img src="/imgs/kafka-05-extension/d73bc2d1607c69453494d0899c8ca715_MD5.jpg" style="display: block; width: 100%;" alt="KRaft 模式架构改进">

<!--
图片内容提取:
传统模式 (依赖 Zookeeper)
云原生 Kafka 架构 (生产就绪)
Kafka Brokers
Zookeeper
Cluster
云原生 Kafka 架构
Kafka Cluster with Kraft
Brokers
Controller
Brokers

• Broker 依赖外部 ZK 存储状态
• Controller 动态选举产生
• 缺点：外部依赖导致连接故障，限制 Partition 规模上限
• Kafka 集群内部集成固定 Controller 节点
• 基于定制化raft 协议自主管理元数据
• 优点：消除外部依赖，原生高可用，无需额外能力层补充

图片概括:
这张图片对比了Kafka的两种架构模式：传统依赖Zookeeper的模式和云原生的Kraft架构。传统模式依赖外部ZK存储状态，存在连接故障和分区规模限制；而云原生架构在集群内部集成固定Controller节点，基于Raft协议自主管理元数据，消除了外部依赖，提供原生高可用性，更适合生产环境部署。
-->

**具体对比如下：**

| 对比维度 | 传统 Zookeeper 模式 | KRaft 模式 | 核心差异 |
| --- | --- | --- | --- |
| **Controller 产生方式** | Zookeeper 动态选举 | 配置文件固定指定 | 从**动态选举**转向**静态配置** |
| **元数据存储** | Zookeeper 集中存储 | Controller 节点分布式存储 | 从**外部集中**转向**内部分布** |
| **外部依赖** | 强依赖 Zookeeper | **完全独立运行** | 消除外部依赖，自主管理 |
| **运维复杂度** | 需维护两套集群 | 仅维护 Kafka 集群 | 运维成本显著降低 |
| **云原生适配** | 适配困难 | **天然适配云原生** | 容器化部署更便捷 |

> **架构演进本质**：KRaft 模式通过**去 Zookeeper 化**实现 Kafka 集群的**自主管理**，基于 **Raft 共识算法**构建分布式一致性架构，从**依赖外部协调**转向**内部自治**。

**架构演进路径：**

<img src="/imgs/kafka-05-extension/ca3b4da943a0c84b405d2f92d12214cb_MD5.jpg" style="display: block; width: 100%;" alt="Kafka 架构演进路径">

<!--
图片内容提取:
**主要标题**: Kafka TICKET

**架构图组件标签**:
- Topic (中心云朵形状)
- Data Source (左侧方框)
- Data Bridge (中间方框)
- Data Sink (右侧方框)

**子标签**:
- Topic: Application Logs (位于Data Source下)
- Topic: Application Logs (位于Data Bridge下)
- Topic: Application Logs (位于Data Sink下)

**工具类别标签**:
- Tool (左侧工具框)
- Tool (中间工具框)
- Tool (右侧工具框)

**Kafka命令行工具列表**:
- kafka-topics.sh
- kafka-console-producer.sh
- kafka-console-consumer.sh
- kafka-run-class.sh
- kafka-server-start.sh
- kafka-server-stop.sh
- kafka-configs.sh
- kafka-log-dirs.sh
- kafka-mirror-maker.sh
- kafka-reassign-partitions.sh
- kafka-preferred-replica-election.sh
- kafka-delete-records.sh
- kafka-leader-election.sh
- kafka-acls.sh
- kafka-broker-api-versions.sh
- kafka-delegation-tokens.sh
- kafka-get-offsets.sh
- kafka-consumer-groups.sh
- kafka-dump-log.sh
- kafka-verifiable-consumer.sh
- kafka-verifiable-producer.sh
- kafka-streams-application-reset.sh
- kafka-metadata-quorum.sh
- kafka-metadata-quorum-controller.sh
- kafka-metadata-quorum-broker.sh
- kafka-metadata-quorum-topic.sh
- kafka-metadata-quorum-observer.sh
- kafka-metadata-quorum-features.sh

图片概括:
这张图片展示了Kafka TICKET架构,包含三个核心组件:数据源(Data Source)、数据桥接(Data Bridge)和数据接收(Data Sink),它们通过"应用日志"主题进行数据交互。底部展示了Kafka操作工具,右侧列出了25+个Kafka命令行工具,涵盖主题管理、生产消费、集群运维等功能。这是一个典型的Kafka数据流处理架构图。
-->

#### (2) Raft 协议原理

理解了 KRaft 模式的改进后，我们接下来探讨其技术核心——**Raft 协议**。

**Raft 协议**是一种基于**多数同意原则**的**分布式共识算法**，与早期的 **Paxos 协议**功能类似，但设计更简洁、易于理解和工程实现。

| 协议维度 | 核心说明 | 技术定位 |
| --- | --- | --- |
| **核心思想** | 通过**多数节点达成一致**来实现集群共识 | 确保分布式系统的一致性和可用性 |
| **KRaft 定位** | Kafka 基于 **Raft 协议**的**定制化实现** | 将 **Raft** 应用于 Kafka 元数据管理场景 |
| **类似方案** | **RocketMQ** 的 **DLedger** 集群同样基于 **Raft 定制** | 主流 **MQ** 产品均采用 **Raft** 实现高可用 |

基于 Raft 协议，KRaft 带来了以下核心优势：

**KRaft 带来的核心优势：**

| 优势维度 | 技术改进 | 业务价值 |
| --- | --- | --- |
| **独立运行** | **消除 Zookeeper 外部依赖**，Kafka 集群自主管理元数据 | 避免 Zookeeper 性能波动影响，版本迭代更自由 |
| **Controller 固定** | **Controller 节点配置文件固定**，无需动态选举 | 便于配合 **Kubernetes** 等高可用工具，提升集群稳定性 |
| **扩展能力增强** | **摆脱 Zookeeper 数据存储限制**，元数据分布式管理 | **Partition 规模显著提升**，支撑更大规模的业务场景 |
| **云原生友好** | **简化部署架构**，仅需维护 Kafka 集群 | 天然适配**容器化环境**，降低云原生部署复杂度 |

> **生产现状**：尽管 **KRaft 模式**已标记为生产可用，但基于**分布式算法复杂性**的稳定性仍需时间验证，目前**大部分企业**仍使用 Zookeeper 模式，**KRaft 大规模应用**尚需时间沉淀。

#### (3) 命令行工具参数变化

KRaft 模式的引入不仅体现在架构层面，也反映在日常使用的命令行工具中。Kafka 在**去 Zookeeper 化**过程中，`bin` 目录下的脚本参数也在**持续演进**，体现了从**依赖外部协调**向**自主管理**的转型路径。具体变化如下表所示：

| 对比维度 | **Zookeeper 模式** | **KRaft 模式** | 核心差异 |
| --- | --- | --- | --- |
| **连接参数** | `--zookeeper` | `--bootstrap-server` | **协调方式**从外部依赖转向自主管理 |
| **地址指定** | 指定 **Zookeeper 集群地址**<br>例：`localhost:2181` | 指定 **Kafka 服务地址**<br>例：`localhost:9092` | **服务定位**从协调服务转向数据服务 |
| **典型脚本** | `kafka-topics.sh --zookeeper` | `kafka-topics.sh --bootstrap-server` | **命令语法**发生根本性变化 |
| **迁移进度** | **旧版本**使用此模式 | **所有脚本均已迁移** | **完全替代**是必然趋势 |

> **演进本质**：这一脚本层面的参数演进，本质上是 Kafka 从**"依赖 Zookeeper"**向**"自主管理"**转型的**技术体现**，反映了架构层面的**根本性变革**。

面对这一变革，建议采取以下实践策略：新项目应**直接使用 KRaft 模式**参数；对于旧项目，建议**制定迁移计划**，逐步将脚本参数从 `--zookeeper` 迁移至 `--bootstrap-server`。

### 4.2 KRaft 集群配置

理解了 KRaft 模式的核心原理后，接下来学习如何实际部署一个 KRaft 集群。Kafka 在 `config/kraft` 目录下提供了 KRaft 协议的参考配置文件，包含三种节点角色的配置模板。

**配置文件说明：**

| 配置文件 | 节点角色 | 说明 |
| --- | --- | --- |
| **broker.properties** | Broker 节点 | 纯数据节点，仅提供消息转发服务 |
| **controller.properties** | Controller 节点 | 纯控制节点，负责集群管理和元数据维护 |
| **server.properties** | 混合节点 | 同时具备 Broker 和 Controller 功能 |

> **角色定位**：Controller 承担 Zookeeper 的职责，负责集群管理；Broker 提供消息转发服务。生产环境通常使用 **server.properties** 混合模式以简化部署。

#### (1) 关键配置项

配置 KRaft 集群需要理解几个关键参数。以下是 KRaft 集群的核心配置项，根据实际环境进行定制：

```properties
# 节点角色：broker（消息转发）+ controller（集群管理）
process.roles=broker,controller

# 节点唯一标识：集群内全局唯一，不可重复
node.id=1

# 投票节点列表：格式为"节点ID@主机:端口"，端口9093用于Controller间通信（区别于客户端9092端口）
controller.quorum.voters=1@worker1:9093,2@worker2:9093,3@worker3:9093

# Broker对外暴露地址：客户端访问地址
advertised.listeners=PLAINTEXT://worker1:9092

# Controller协议别名：默认值CONTROLLER
controller.listener.names=CONTROLLER

# 监听服务绑定：PLAINTEXT（客户端）、CONTROLLER（集群通信）
listeners=PLAINTEXT://:9092,CONTROLLER://:9093

# 数据存储目录：生产环境建议配置在非/tmp目录
log.dirs=/app/kafka/kraft-log

# Topic默认分区数
num.partitions=2
```

**关键配置说明：**

| 配置项 | 主要作用 | 配置要点 |
| --- | --- | --- |
| **process.roles** | 定义节点角色组合 | 可选值：`broker`、`controller` 或 `broker,controller` |
| **node.id** | 节点唯一标识 | 集群内**全局唯一**，与投票节点列表对应 |
| **controller.quorum.voters** | 投票节点列表 | 格式：`节点ID@主机:端口`，端口 **9093** 区别于客户端端口 |
| **advertised.listeners** | Broker 对外地址 | 客户端访问地址，**不能使用 localhost** |
| **listeners** | 监听服务绑定 | `PLAINTEXT`（客户端）、`CONTROLLER`（集群通信） |
| **log.dirs** | 数据存储目录 | 生产环境建议配置在**非 /tmp 目录** |

> **配置提示**：`controller.quorum.voters` 配置需严格遵循格式要求：
> - **`@` 符号前**：节点 ID，必须与各节点的 `node.id` 一致
> - **`@` 符号后**：Controller 通信地址（**端口 9093** 不同于客户端端口 9092）

#### (2) 配置文件部署

配置文件准备完成后，需要将其分发到集群中的各个服务器。每个节点都需要根据自身特性调整特定的配置参数，以确保集群能够正确识别和通信。

将配置文件分发到各服务器，根据节点特性修改以下属性：

| 修改项 | 配置说明 | 配置示例 | 注意事项 |
| --- | --- | --- | --- |
| **node.id** | 每个节点的**唯一标识符**，集群内不能重复 | `node.id=1`、`node.id=2`、`node.id=3` | **必须全局唯一**，与 `controller.quorum.voters` 中的节点 ID 对应 |
| **advertised.listeners** | 当前节点对客户端暴露的服务访问地址 | `PLAINTEXT://worker1:9092` | 需配置为**客户端可访问的实际 IP 或域名**，不能使用 localhost |

> **角色配置说明**：节点角色的配置直接影响集群功能：
> - **混合节点**（broker + controller）：`node.id` 必须包含在 `controller.quorum.voters` 列表中，参与集群投票
> - **纯 Broker 节点**：`node.id` **不能**包含在 `controller.quorum.voters` 列表中，仅提供消息转发服务
> - **纯 Controller 节点**：`node.id` 必须包含在 `controller.quorum.voters` 列表中，仅负责集群管理

#### (3) 日志目录格式化

与 Zookeeper 模式不同，KRaft 集群对数据存储格式有特殊要求，启动前必须先对日志目录进行格式化操作。这一步骤会初始化集群元数据，是 KRaft 集群启动的必要前提。

KRaft 集群对数据格式有特殊要求，启动前需对日志目录进行格式化。

**初始化流程说明：**

| 操作步骤 | 命令 | 核心参数 | 作用说明 |
| --- | --- | --- | --- |
| **生成集群 ID** | `bin/kafka-storage.sh random-uuid` | `random-uuid` | 生成**集群唯一标识符**，用于元数据一致性校验 |
| **格式化目录** | `bin/kafka-storage.sh format` | `-t <集群ID>`<br>`-c <配置文件>` | 使用指定集群 ID 格式化日志目录 |

```shell
# 生成集群唯一ID
[root@192-168-65-112 kafka_2.13-3.8.0]$ bin/kafka-storage.sh random-uuid
j8XGPOrcR_yX4F7ospFkTA

# 格式化日志目录（-t 指定集群ID，-c 指定配置文件）
[root@192-168-65-112 kafka_2.13-3.8.0]$ bin/kafka-storage.sh format -t j8XGPOrcR_yX4F7ospFkTA -c config/kraft/server.properties
Formatting /app/kafka/kraft-log with metadata.version 3.4-IV0.
```

> **集群 ID 统一性**：集群内所有服务器必须使用**同一个集群 ID**，确保元数据一致性，否则节点无法正常通信。这个 ID 是集群的唯一标识符，一旦设定就不能更改。

#### (4) 服务启动与验证

完成上述准备工作后，就可以启动 Kafka 服务了。以下以 Worker1 节点为例演示 Broker 和 Controller 服务的启动与验证流程，帮助读者掌握完整的集群启动过程。

格式化完成后，即可启动 Kafka 服务。以下以 Worker1 节点为例演示 Broker 和 Controller 服务的启动与验证流程。

**启动操作步骤：**

| 操作步骤 | 命令 | 说明 |
| --- | --- | --- |
| **1. 启动服务** | `bin/kafka-server-start.sh -daemon config/kraft/server.properties` | **后台启动** Kafka 服务，`-daemon` 参数实现守护进程 |
| **2. 验证进程** | `jps` | 查看 **Java 进程**，确认 **Kafka 进程**已启动 |

**启动执行示例：**

```shell
# 后台启动 Kafka 服务
[root@192-168-65-112 kafka_2.13-3.8.0]$ bin/kafka-server-start.sh -daemon config/kraft/server.properties

# 验证服务启动状态
[root@192-168-65-112 kafka_2.13-3.8.0]$ jps
10993 Jps
10973 Kafka
```

**验证要点：**

| 验证项 | 检查方法 | 成功标志 |
| --- | --- | --- |
| **进程状态** | `jps` 命令查看 | 显示 **Kafka 进程**及进程号 |
| **集群状态** | 启动所有节点后检查 | 所有 **Controller 节点**完成选举，集群正常通信 |

> **重要提示**：启动时需确保所有节点已完成**日志目录格式化**，且使用**相同的集群 ID**。启动顺序建议为**先启动 Controller 节点**，再启动 Broker 节点。

集群启动完成后，即可像普通集群一样创建 Topic 并维护 Topic 信息。至此，KRaft 集群的部署配置工作已全部完成，您可以开始享受去 Zookeeper 化带来的架构优势了。

## 5. Kafka 流式计算

在大数据技术生态中，**Kafka** 扮演着流式计算的**核心数据源**角色，是连接数据产生层与数据处理层的关键桥梁。

| 定位维度 | 具体说明 |
| --- | --- |
| **数据角色** | 作为流式计算框架的**统一数据入口**，持续接收和分发实时数据流 |
| **生态支撑** | 与 **Kafka Streams**、**Spark Streaming**、**Flink** 等主流流式计算框架深度集成 |
| **技术价值** | 凭借高吞吐、低延迟特性，支撑海量数据的实时处理需求 |

> **核心本质**：Kafka 的**消息队列**能力与流式计算的**实时处理**需求高度契合，使其成为构建实时数据管道的**基础设施首选**。

### 5.1 批量计算与流式计算对比

在大数据计算领域，批量计算与流式计算代表了两种不同的数据处理理念。前者关注历史数据的全量分析，后者聚焦实时数据的动态处理。两者的根本差异体现在数据时效性与处理模式上。

#### (1) 核心对比

<img src="/imgs/kafka-05-extension/4e23cb3a147d6fc6d4d161a5a17ffda0_MD5.jpg" style="display: block; width: 100%;" alt="批量计算与流式计算对比">

<!--
图片内容提取:
### 上半部：批量计算
DATA_BLOCK_1
DATA_BLOCK_1  DATA_BLOCK_2
处理全量、离线的静态数据。表现为静态水池与定时水桶。例如：传统的 SQL 批处理或标准的 MQ 批量 poll() 消费。

### 下半部：流式计算
stream_event_001  stream_event_002  stream_event_003
处理实时、动态产生的数据流。表现为流动水管。来一条处理一条，极高实时性。例如：实时 PV/UV 累加统计。

### 核心论点：Kafka 凭借其极高吞吐量，成为了流式计算的天然首选数据源，并推出了原生的 Kafka Streams API。

图片概括:
这张图片对比了批量计算与流式计算两种数据处理模式。批量计算处理静态离线数据（如SQL批处理），流式计算处理实时动态数据（如PV/UV统计），具有极高实时性。Kafka凭借其高吞吐量成为流式计算的理想数据源，并提供了原生的Kafka Streams API支持。
-->

为了更清晰地理解两者的差异，下表从多个维度进行了详细对比：

| 对比维度     | 批量计算               | 流式计算                                | 核心差异                   |
| -------- | ------------------ | ----------------------------------- | ---------------------- |
| **数据特征** | 静态数据，数据集完整     | 动态数据，持续产生                       | 数据时效性的根本差异         |
| **处理模式** | 按批次拉取完整数据      | 逐条处理实时到达的数据                     | 批量处理 vs 流式处理   |
| **时效性**  | 高延迟，适合离线计算     | 低延迟，适合实时计算                      | 离线分析 vs 实时响应   |
| **典型场景** | 数据库SQL查询、Kafka批量消费 | 实时PV/UV统计、实时风控                      | 历史分析 vs 实时决策   |
| **代表技术** | Hadoop MapReduce   | Spark Streaming、Flink、Kafka Streams | 批处理框架 vs 流计算框架 |

> **核心本质**：批量计算关注历史数据的全量分析，适合离线场景；流式计算关注实时数据的动态变化，适合在线场景。

#### (2) 应用场景对比

理论对比之后，让我们看看它们在实际业务中的应用场景：

| 计算范式 | 典型场景 | 核心特征 |
| --- | --- | --- |
| **批量计算** | **数据库全量统计分析**<br>**Kafka 批量消费处理**<br>**离线数仓 ETL 作业** | 静态数据集、批量拉取、离线处理 |
| **流式计算** | **网站实时 PV/UV 统计**<br>**实时风控系统**<br>**实时推荐引擎** | 动态数据流、逐条处理、实时计算 |

从上述对比可以看出，批量计算和流式计算在业务场景上形成了互补关系：批量计算承担历史数据的深度分析任务，而流式计算则负责实时数据的快速响应。

#### (3) Kafka Streams 生态优势分析

在流式计算领域，虽然 RocketMQ、RabbitMQ 等主流消息队列产品也都推出了各自的流式计算 API，但 Kafka 凭借独特的优势，仍然稳居流式计算核心数据源的地位。这主要得益于以下三个方面的核心竞争力：

| 优势维度 | 核心特性 | 技术价值 |
| --- | --- | --- |
| **高吞吐能力** | 单机每秒几十万 TPS 级别的消息处理能力 | 支撑海量数据的实时处理需求 |
| **生态完整** | 围绕 Kafka 构建的大数据流式计算生态最为成熟完整 | 提供从数据接入到计算分析的全链路解决方案 |
| **原生集成** | Kafka Streams 提供原生的流式计算 API，与 Kafka 无缝集成 | 降低开发复杂度，实现开箱即用的流式处理能力 |

为了更直观地展现 Kafka 在流式计算领域的领先地位，下表将其与 RocketMQ、RabbitMQ 的流式计算方案进行了横向对比：

| 对比维度 | **Kafka Streams** | **RocketMQ Streams** | **RabbitMQ Streams** |
| --- | --- | --- | --- |
| **生态成熟度** | 最成熟，与 Spark、Flink 深度集成 | 较新，生态仍在发展 | 较新，应用范围有限 |
| **部署复杂度** | 轻量级，无需额外集群 | 需部署 RocketMQ 集群 | 需部署 RabbitMQ 集群 |
| **社区活跃度** | 极高，业界应用广泛 | 中等，阿里生态为主 | 中等，传统企业应用 |

综合来看，Kafka Streams 为 Kafka 提供了轻量级的原生流式计算能力，配合 Spark Streaming、Flink 等大型框架，共同构成了业界最完整的大数据流式计算生态体系。

### 5.2 Kafka Streams 快速实践

**Word Count**（词频统计）是流式计算的经典入门案例，类似于分布式计算领域的"Hello World"。本节通过实现一个基于 Kafka 的实时词频统计程序，演示 Kafka Streams 的核心使用方式。

**核心功能**：实时统计 `INPUT_TOPIC` 中每个单词出现的次数，将结果输出到 `OUTPUT_TOPIC`。

#### (1) Maven 依赖配置

开发 Kafka Streams 应用首先需要引入相应的依赖包。在 `pom.xml` 中添加 Kafka Streams 依赖：

| 依赖配置 | 说明 |
| ---- | ---- |
| **Group ID** | `org.apache.kafka` |
| **Artifact ID** | `kafka-streams` |
| **版本** | `3.8.0`（需与 Kafka 集群版本适配） |

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-streams</artifactId>
    <version>3.8.0</version>
</dependency>
```

> **版本适配说明**：Kafka Streams 客户端版本应与 Kafka 集群版本保持一致，以确保功能兼容性和稳定性。

#### (2) 流式计算程序开发

完成依赖配置后，接下来编写流式计算程序。Kafka Streams 提供了 High Level DSL API，通过声明式的方式构建流式计算拓扑，大大简化了开发复杂度。

| 实现方式 | 核心特点 | 适用场景 |
| --- | --- | --- |
| **High Level DSL** | 声明式 API，提供丰富的流式操作算子 | 常见流式计算场景，开发效率高 |

以下是一个完整的词频统计实现示例，代码中包含了必要的配置和核心处理逻辑：

```java
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Author： roy
 * Description：使用HighLevel构建Topology
 **/
public class WordCountStream {
    private static final String INPUT_TOPIC = "inputTopic";
    private static final String OUTPUT_TOPIC = "outputTopic";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "streams-wordcount");
        props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.65.112:9092");
        props.putIfAbsent(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
        props.putIfAbsent(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.putIfAbsent(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaStreams streams = new KafkaStreams(buildTopology(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // 优雅关闭。streams需要调用close才会清除本地缓存
        Runtime.getRuntime().addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private static Topology buildTopology() {
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        KStream<Object, String> source = streamsBuilder.stream(WordCountStream.INPUT_TOPIC);

        //flatMapValues：对每个值(如果Value是Collection，也会解析出每个值)执行一个函数，返回一个或多个值
        // 将字符串转换为小写，并使用空格分隔符分割字符串
        source.flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
                // 将每个单词作为key，进行分组
                .groupBy((key, value) -> value)
                // 对每个分组进行计数，结果为一个KTable，可以理解为一个中间结果集
                .count()
                // 转换成为KStream数据流
                .toStream()
                // 输出到指定Topic
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), Serdes.String()));
        return streamsBuilder.build();
    }
}
```

上述代码实现了完整的词频统计功能。为了更清晰地理解数据如何在处理链路中流转，下表详细说明了每个处理阶段的具体操作和作用。

#### (3) 数据流处理流程

流式计算处理链路：

| 处理阶段 | 核心操作 | 技术实现 | 业务价值 |
| --- | --- | --- | --- |
| **1. 数据源接入** | 创建数据流 | `stream(INPUT_TOPIC)` | 从输入 Topic 创建 **KStream** 数据流，启动处理链路 |
| **2. 数据分词** | 文本转换 | `flatMapValues` | 按**非单词字符**分割，转换为**小写**，标准化数据格式 |
| **3. 数据分组** | Key 分组 | `groupBy` | 以**单词作为 Key** 进行分组，为聚合做准备 |
| **4. 数据聚合** | 频次统计 | `count()` | 统计每个单词出现次数，生成 **KTable** 中间结果集 |
| **5. 数据转换** | 流式转换 | `toStream()` | 将 **KTable** 转换为 **KStream**，适配下游处理 |
| **6. 结果输出** | 数据写入 | `to(OUTPUT_TOPIC)` | 将统计结果写入输出 Topic，完成数据流转 |

> **流式计算本质**：采用**逐条处理**模式，消息到达后**立即进入处理链路**，**无需等待批量聚合**，实时性显著高于传统批量计算。

#### (4) 应用核心参数配置

理解了数据流转过程后，还需要关注代码中涉及的核心配置项。这些配置直接影响应用的运行行为和性能表现。下表总结了 Kafka Streams 应用的关键配置参数及其作用：

| 配置项 | 核心职责 | 配置说明 |
| --- | --- | --- |
| **APPLICATION_ID_CONFIG** | **应用唯一标识** | 用于协调消费组，同一应用的不同实例需使用相同的 ID |
| **BOOTSTRAP_SERVERS_CONFIG** | **Kafka 集群地址** | 指定 Kafka 服务器地址，格式：`host:port`，支持多个地址逗号分隔 |
| **DEFAULT_KEY_SERDE_CLASS_CONFIG** | **Key 序列化方式** | 指定消息键的序列化器类型（如 `StringSerde`、`IntegerSerde`） |
| **DEFAULT_VALUE_SERDE_CLASS_CONFIG** | **Value 序列化方式** | 指定消息值的序列化器类型，需与实际数据类型匹配 |
| **AUTO_OFFSET_RESET_CONFIG** | **Offset 重置策略** | 消费者启动时的位置策略：`latest`（最新）、`earliest`（最早） |

> **配置要点**：这些参数是构建 Kafka Streams 应用的**基础配置**，直接影响应用的正常运行和数据处理的准确性。建议根据实际业务场景选择合适的序列化器和 Offset 重置策略。

至此，我们已经完成了一个 Kafka Streams 应用的开发全过程。从依赖配置、程序开发、流程理解到参数配置，掌握了流式计算应用的核心开发要素。
### 5.3 Kafka Streams 核心概念

深入理解 Kafka Streams 的核心概念，是构建复杂流式应用的基础。本节将重点介绍两个核心概念：KStream 与 KTable 的数据抽象差异，以及 Processor 底层 API 的工作原理。

#### (1) KStream 与 KTable 核心概念

在开始具体概念之前，先理解 Kafka Streams 的整体工作方式。**Kafka Streams** 基于 **Topology（拓扑结构）** 构建流式计算链路。当 **Source 端**有数据流入时，数据会沿着 **Topology** 定义的处理链路逐条处理，其工作模式类似于工厂流水线。

在此基础上，Kafka Streams 提供了两种核心的数据抽象：**KStream** 和 **KTable**。理解二者的区别对于选择合适的处理方式至关重要。

**核心抽象概念对比：**

| 对比维度 | **KStream** | **KTable** |
| --- | --- | --- |
| **本质定义** | **数据流抽象** | **状态表抽象** |
| **数据特性** | 持续流动的**数据序列** | 数据的**最新状态视图** |
| **处理模式** | 每条记录**独立处理** | 基于 **Key** 的**状态更新** |
| **关注焦点** | 数据的**流动过程** | 数据的**当前状态** |
| **典型操作** | **转换**、**过滤**、**聚合** | **查询**、**更新**、**连接** |
| **状态管理** | 通常**无状态** | 必须维护**状态存储** |
| **数据更新** | **追加模式**，记录只增不减 | **更新模式**，相同 Key 会覆盖 |
| **应用场景** | 实时数据处理、事件流分析 | 数据聚合、状态查询、表连接 |

> **核心思想**：**KStream** 关注数据流动过程，**KTable** 关注数据当前状态。两者可以**相互转换**，共同构建完整的流式计算链路。

为了更好地理解如何在实际应用中选择使用 KStream 或 KTable，下面从架构定位和应用场景角度进一步说明。

**架构定位与应用场景：**

| 概念 | 架构定位 | 典型应用场景 | 转换关系 |
| --- | --- | --- | --- |
| **KStream** | **流式处理层**的核心抽象 | 实时日志分析、事件流处理、实时过滤转换 | 可通过 `groupBy` + `count` 转换为 KTable |
| **KTable** | **状态管理层**的核心抽象 | 实时统计、状态查询、数据表连接、缓存更新 | 可通过 `toStream` 转换为 KStream |

<img src="/imgs/kafka-05-extension/118c1fe1bc7e3bd87b7aa29e2328c89e_MD5.jpg" style="display: block; width: 100%;" alt="KStream 与 KTable 转换关系">

<!--
图片内容提取:
输入起点
.stream(INPUT_TOPIC)
数据流操作 (KStream)
.flatMapValues(拆分单词)
输出终点
.to(OUTPUT_TOPIC)
source
KStream
KStream
KStream
KStream
target
KTable
RocksDB
本地状态聚合 (KTable + RocksDB)
.groupBy().count()
备注说明：使用 StreamsBuilder，将底层复杂逻辑抽象为连贯的数据流主线 API。

图片概括:
这张图展示了Kafka Streams的数据流处理流程。从输入主题(INPUT_TOPIC)开始，通过KStream进行数据转换操作(如拆分单词)，最终输出到目标主题(OUTPUT_TOPIC)。图中还演示了使用KTable结合RocksDB进行本地状态聚合的计数操作。整体架构采用StreamsBuilder将复杂的流处理逻辑抽象为简洁的API调用链，实现了声明式的数据处理管道设计。
-->

#### (2) Processor 底层 API 详解

除了前面介绍的高级 DSL API，Kafka Streams 还提供了底层的 Processor API。这两种 API 各有适用场景，开发者需要根据实际需求进行选择。

**Kafka Streams** 提供**两层 API**：

| API层级 | API类型 | 核心特点 | 适用场景 |
| --- | --- | --- | --- |
| **High Level API** | KStream/KTable | **声明式 API**，提供丰富的流式操作算子 | 常见流式计算场景，**开发效率高** |
| **Low Level API** | Processor | **过程式 API**，提供底层处理节点控制 | 复杂自定义逻辑，**灵活度更高** |

当 **High Level API** 无法满足复杂业务需求时，**Low Level API** 提供了更底层的 **Processor 模型**，支持灵活构建复杂的 Topology。

```java
package com.roy.kfk.stream;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.api.Processor;
import org.apache.kafka.streams.processor.api.ProcessorContext;
import org.apache.kafka.streams.processor.api.ProcessorSupplier;
import org.apache.kafka.streams.processor.api.Record;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * Author： roy
 * Description：将INPUT_TOPIC中每个单词出现的次数。
 * 使用LowLevel API构建Topology。自由度更高。
 *
 */
public class WordCountProcessorDemo {

    private static final String INPUT_TOPIC = "inputTopic";
    private static final String OUTPUT_TOPIC = "outputTopic";

    public static void main(String[] args) {
        Properties props = new Properties();
        props.putIfAbsent(StreamsConfig.APPLICATION_ID_CONFIG, "word");
        props.putIfAbsent(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.65.112:9092,192.168.65.170:9092,192.168.65.193:9092");
        props.putIfAbsent(StreamsConfig.STATESTORE_CACHE_MAX_BYTES_CONFIG, 0);
        props.putIfAbsent(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.putIfAbsent(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class);
        props.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        KafkaStreams streams = new KafkaStreams(buildTopology(), props);
        final CountDownLatch latch = new CountDownLatch(1);

        // 优雅关闭。streams需要调用close才会清除本地缓存
        Runtime.getRuntime().addShutdownHook(new Thread("streams-wordcount-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (final Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    private static Topology buildTopology() {
        Topology topology = new Topology();
        topology.addSource("source", WordCountProcessorDemo.INPUT_TOPIC);
        topology.addProcessor("process", new MyWCProcessor(), "source");
        topology.addSink("sink", OUTPUT_TOPIC, new StringSerializer(), new LongSerializer(), "process");
        return topology;
    }

    //输入和输出的key与value都必须是相同类型，否则无法序列化。--只能设置一个KEY_SERDE_CLASS 和一个 VALUE_SERDE_CLASS
    // 用highlevel API就可以转。但是用lowlevel API的话，暂不知道怎么处理。
    static class MyWCProcessor implements ProcessorSupplier<String, String, String, Long> {
        @Override
        public Processor<String, String, String, Long> get() {
            return new Processor<String, String, String, Long>() {
                private KeyValueStore<String, Long> kvstore;

                @Override
                public void init(ProcessorContext<String, Long> context) {
                    context.schedule(Duration.ofSeconds(1), PunctuationType.STREAM_TIME, timestamp -> {
                        try (KeyValueIterator<String, Long> iter = kvstore.all()) {
                            System.out.println("=======" + timestamp + "======");
                            while (iter.hasNext()) {
                                KeyValue<String, Long> entry = iter.next();
                                System.out.println("[" + entry.key + "," + entry.value + "]");
                                context.forward(new Record<>(entry.key, entry.value, timestamp));
                            }
                        }
                    });
                    this.kvstore = context.getStateStore("counts");
                }

                @Override
                public void process(Record<String, String> record) {
                    System.out.println(">>>>>" + record.value());
                    String[] words = record.value().toLowerCase().split("\\W+");
                    for (String word : words) {
                        Long count = this.kvstore.get(word);
                        if (null == count) {
                            this.kvstore.put(word, 1);
                        } else {
                            this.kvstore.put(word, count + 1);
                        }
                    }
                }
            };
        }

        @Override
        public Set<StoreBuilder<?>> stores() {
            return Collections.singleton(Stores.keyValueStoreBuilder(
                    Stores.inMemoryKeyValueStore("counts"),
                    Serdes.String(),
                    Serdes.Long()));
        }
    }
}
```

下面通过一个完整的代码示例来理解 Processor API 的使用方式，然后深入分析其核心组件。

**Processor 模型核心组件：**

<img src="/imgs/kafka-05-extension/8328c52c42ef8f7997da5044039560a5_MD5.jpg" style="display: block; width: 100%;" alt="Processor 模型架构">

<!--
图片内容提取:
Source Processor (起始节点)
数据起点，直接读取 Topic 数据传入下游。
Stream Processor (处理核心)
中间处理节点。支持自定义 schedule 定时触
发与手动操控 KeyValueStore 状态机。
Sink Processor (终点节点)
数据终点，将计算结果输出回 Topic。
对比结论:相比 High-Level API,Low-Level 提供了绝对的底层控制权,像构建工厂流水线
一样,适合极其复杂的定制化状态计算链路。

图片概括:
这张图片介绍了 Kafka Streams Low-Level API 的三种核心处理器节点:Source Processor 作为数据起点读取 Topic 数据,Stream Processor 作为处理核心支持自定义定时触发和状态机操作,Sink Processor 将结果输出回 Topic。相比 High-Level API,Low-Level API 提供更精细的底层控制权,适合构建复杂的定制化状态计算流程。
-->

**Kafka Streams** 通过**三种 Processor 节点**构建数据处理链条：

| 节点类型 | 职责定位 | 数据流向 |
| --- | --- | --- |
| **Source Processor** | 数据起点 | 从 **Topic** 读取数据，向下游传递 |
| **Processor** | 数据处理 | 从上游接收数据，处理后向下游传递 |
| **Sink Processor** | 数据终点 | 从上游接收数据，输出到 **Topic** |

> **架构本质**：**Processor 模型**是流式计算的**标准处理范式**，通过串联多个处理节点构建完整的数据处理链路，每个节点专注于**单一职责**的数据转换。

最后，从宏观生态视角来看，Kafka Streams 在整个流式计算生态中处于什么位置？它与 Spark Streaming、Flink 等框架有何区别？下面的对比将帮助读者建立更完整的技术认知。

**Kafka Streams 生态定位：**

| 框架 | 核心定位 | 适用场景 | 规模特点 |
| --- | --- | --- | --- |
| **Kafka Streams** | **轻量级流式计算库** | Kafka 生态内的流式处理 | **中小规模**实时计算 |
| **Spark Streaming** | **大规模批流一体框架** | 大数据集群化计算 | 大规模集群化计算 |
| **Flink** | **企业级流式计算引擎** | 大规模实时流式计算 | 超大规模实时计算 |

> **核心结论**：**Kafka Streams** 围绕 **Kafka** 构建**轻量级流式处理能力**，适合**中小规模**实时计算；**Spark Streaming**、**Flink** 等大型框架支持**更大规模**的集群化计算，但基础的流式计算思想与 **Kafka Streams** 一脉相承。

## 6. 总结

本文从**性能验证**、**运维监控**、**架构演进**、**实时计算**四个维度系统阐述了 **Kafka 企业级扩展能力**，帮助读者建立完整的 **Kafka 生态认知**。

通过学习本文，读者将获得以下核心能力：

| 学习层次 | 核心收获 |
| ---- | ---- |
| **建立性能评估体系** | 掌握 **Kafka 官方压测工具**使用方法，理解**吞吐量**、**延迟**、**分位数**等性能指标含义，具备**集群性能量化评估**与**调优验证**能力 |
| **完善运维监控能力** | 熟练部署 **EFAK 监控平台**，实现集群运行状态**可视化**与**告警管理**，建立生产环境**可观测性**基础 |
| **理解架构演进路径** | 理解 **KRaft 模式**的**去 Zookeeper 化**演进本质，掌握 **Raft 协议**基本原理，了解 **Kafka 集群自主管理**的技术趋势 |
| **拓展流式计算视野** | 掌握 **Kafka Streams** 快速入门方法，理解 **KStream**、**KTable** 等流式计算基础概念，为深入学习 **Spark Streaming**、**Flink** 等大数据技术打下基础 |

这四个维度的能力建设，为实际生产环境部署与应用提供了全面的技术支撑。


