---
title: Spring Cloud Alibaba上手 09：SkyWalking
author: fangkun119
date: 2025-12-27 12:00:00 +0800
categories: [微服务, Spring Cloud Alibaba]
tags: [微服务, Spring Cloud Alibaba, SkyWalking]
pin: false
math: true
mermaid: true
image:
  path: /imgs/cover/spring_cloud_alibaba_hands_on.png
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: spring cloud alibaba
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

### 1.1 内容概要

本文讲解 **SkyWalking 分布式链路追踪** 的接入与应用，涵盖以下知识模块：

| 知识模块                | 说明                                             |
| ------------------- | ---------------------------------------------- |
| **SkyWalking 核心能力** | **可观测性平台**：分布式追踪、服务网格遥测、度量聚合、可视化展示             |
| **架构设计与定位**         | **分层架构**：数据采集层（Agent）+ 数据处理层（OAP、存储、Web 控制台）   |
| **微服务接入方案**         | **三步走**：准备 Agent JAR 包 → 配置 JVM 参数 → 验证接入效果    |
| **实战操作演示** | **字节码增强**：Agent 探针接入、Gateway Bug 修复、链路追踪可视化 |

本文从**工具介绍**、**架构设计**、**接入实战**三个维度系统阐述 **SkyWalking** 的应用，帮助读者快速实现微服务调用链路的**可视化监控**，提升分布式系统的**可观测性**与**问题定位效率**。

### 1.2 配套资源

#### (1) 项目源码

| 资源类型           | 说明                               | 链接                                                                                                                   |
| -------------- | -------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| **项目源码**       | Spring Cloud Alibaba 2023 完整示例代码 | [github.com/fangkun119/spring-cloud-alibaba-2023-demo](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo) |
| **Postman 集合** | API 测试用例集合，便于快速验证功能              | [github.com/fangkun119/postman-workspace](https://github.com/fangkun119/postman-workspace)                           |

#### (2) 环境搭建

见文档：[Spring Cloud Alibaba上手 03：中间件环境]({% post_url 2025-12-20-spring-cloud-alibaba-mvp-02-env %})


## 2. SkyWalking 介绍

### 2.1 核心能力

**SkyWalking** 是专为 **微服务**、**云原生架构** 和 **容器化环境**（Docker、K8s、Mesos）设计的 **可观测性分析平台** 与 **应用性能管理系统**。

**核心功能模块：**

| 功能模块 | 能力说明 |
| --- | --- |
| **分布式追踪** | 从请求入口到底层服务的 **全链路监控**，可视化调用关系 |
| **服务网格遥测** | Service Mesh 架构的 **遥测数据采集与分析** |
| **度量聚合** | 实时聚合 **性能指标**（响应时间、吞吐量、错误率） |
| **可视化展示** | 提供直观的 **仪表盘** 与 **拓扑图**，快速定位问题 |

**架构定位：**

> SkyWalking 通过 **非侵入式** 的数据采集，为分布式系统提供 **端到端** 的可观测性，帮助开发者快速定位性能瓶颈和故障点。

### 2.2 架构设计

**SkyWalking** 采用 **分层架构**，由 **数据采集层** 和 **数据处理层** 组成：

| 架构层次      | 核心组件               | 状态                                                                                                 |
| --------- | ------------------ | -------------------------------------------------------------------------------------------------- |
| **数据处理层** | OAP 服务器、数据存储、可视化看板 | 已在 [Spring Cloud Alibaba上手 03：中间件环境]({% post_url 2025-12-20-spring-cloud-alibaba-mvp-02-env %}) 搭建完成 |
| **数据采集层** | Agent 探针、字节码注入     | 本章将实现接入                                                                                            |

**本章重点：**

通过 **JVM 参数** 配置，让微服务接入 **SkyWalking Agent**，采集调用链路数据并发送至 **OAP 服务器**，最终在 **Web 控制台** 实现微服务调用链路的可视化监控。

<img src="/imgs/spring-cloud-alibaba-mvp-10-skywalking/ef7bb7c175e0ca1ebc4b7924e20c640d_MD5.jpg" style="display: block; width: 100%;" alt="SkyWalking架构">

## 3. 微服务整合

**接入原理**：通过 **JVM 参数** 让微服务加载 **SkyWalking Agent JAR 包**，Agent 通过 **字节码增强** 技术采集调用链路数据，并发送至 **SkyWalking OAP 服务器** 进行存储和分析。

**接入流程**（**三步走**）：

| 步骤 | 核心操作 | 关键产出 |
| --- | --- | --- |
| **① 准备 Agent** | 下载并解压 SkyWalking Agent，修复 Gateway 监控 Bug | `skywalking-agent.jar` |
| **② 配置 JVM 参数** | 为每个微服务添加 JVM 启动参数 | 指定 Agent 路径、服务名称、OAP 地址 |
| **③ 验证接入** | 启动微服务并访问 SkyWalking 控制台 | 链路追踪可视化 |

### 3.1 准备 SkyWalking Agent JAR 包

**① 下载并解压 Agent**

**SkyWalking Agent JAR 包** 来自官网，本项目使用 **版本 9.3.0**（下载方式见 3.7 节）。解压后目录结构如下：

```bash
_______________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/Code/mid-wares/
$ tar xvfz apache-skywalking-java-agent-9.3.0.tgz
x skywalking-agent/
x skywalking-agent/activations/
x skywalking-agent/activations/apm-toolkit-kafka-activation-9.3.0.jar
……
x skywalking-agent/licenses/LICENSE-asm.txt
```

**② 修复 Spring Cloud Gateway 监控 Bug**

**SkyWalking 9.3.0** 存在一个已知 Bug（[Issue #10509](https://github.com/apache/skywalking/issues/10509)）：**Gateway 插件默认未激活**，需手动将插件 JAR 包从 `optional-plugins` 目录拷贝至 `plugins` 目录。

**修复步骤：**

```bash
_______________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/Code/mid-wares/skywalking-agent/
$ cp optional-plugins/apm-spring-cloud-gateway-4.x-plugin-9.3.0.jar plugins/
```

### 3.2 配置 JVM 参数

**① JVM 参数说明**

为微服务添加 **JVM 启动参数**，需配置以下 **三项核心参数**：

| 参数名称 | 参数说明 | 示例值 |
| --- | --- | --- |
| **`-javaagent`** | 指定 `skywalking-agent.jar` 的**绝对路径** | `/path/to/skywalking-agent/skywalking-agent.jar` |
| **`-DSW_AGENT_NAME`** | 指定**微服务名称**（在 SkyWalking 控制台显示） | `tlmall-order` |
| **`-DSW_AGENT_COLLECTOR_BACKEND_SERVICES`** | 指定 **SkyWalking OAP 服务器地址** | `tlmall-skywalking-server:11800` |

**② 参数配置示例**（以**订单服务**为例）

```
-javaagent:/Users/ken/Code/mid-wares/skywalking-agent/skywalking-agent.jar
-DSW_AGENT_NAME=tlmall-order
-DSW_AGENT_COLLECTOR_BACKEND_SERVICES=tlmall-skywalking-server:11800
```

**③ IDEA 配置 JVM 参数**

在 **IntelliJ IDEA** 中为每个微服务添加 JVM 参数（**Run → Edit Configurations → Add VM options**）：

**四个微服务的 JVM 参数配置：**

| 微服务 | 服务名称参数 | 配置截图 |
| --- | --- | --- |
| **网关服务** | `-DSW_AGENT_NAME=tlmall-gateway` | <img src="/imgs/spring-cloud-alibaba-mvp-10-skywalking/f75b8916da0efe8461115892a649f139_MD5.jpg" style="display: block; width: 100%;" alt="tlmall-gateway的JVM参数配置"> |
| **订单服务** | `-DSW_AGENT_NAME=tlmall-order` | <img src="/imgs/spring-cloud-alibaba-mvp-10-skywalking/b06d1ea22a8b57f2ce69501ee1beaf92_MD5.jpg" style="display: block; width: 100%;" alt="tlmall-order的JVM参数配置"> |
| **库存服务** | `-DSW_AGENT_NAME=tlmall-storage` | <img src="/imgs/spring-cloud-alibaba-mvp-10-skywalking/34237f861a04e5b3610c883f5d1334cb_MD5.jpg" style="display: block; width: 100%;" alt="tlmall-storage的JVM参数配置"> |
| **账户服务** | `-DSW_AGENT_NAME=tlmall-account` | <img src="/imgs/spring-cloud-alibaba-mvp-10-skywalking/510e8846e51533212beff90b19cf63ad_MD5.jpg" style="display: block; width: 100%;" alt="tlmall-account的JVM参数配置"> |

**配置要点：**

> 所有微服务的 **`-javaagent`** 和 **`-DSW_AGENT_COLLECTOR_BACKEND_SERVICES`** 参数保持一致，仅 **`-DSW_AGENT_NAME`** 根据服务名称不同而变化。

### 3.3 验证接入效果

**① 启动日志验证**

启动微服务后，在控制台日志中可观察到 **SkyWalking Agent 加载成功** 的日志信息：

<img src="/imgs/spring-cloud-alibaba-mvp-10-skywalking/706d17d81fc79c4902cd55145d579be8_MD5.jpg" style="display: block; width: 100%;" alt="微服务启动日志">

**② SkyWalking 控制台验证**

访问 **SkyWalking Web 控制台**，在 **服务列表** 页面可观察到所有微服务已成功接入：

<img src="/imgs/spring-cloud-alibaba-mvp-10-skywalking/26da127a145682ee659485271679e880_MD5.jpg" style="display: block; width: 100%;" alt="SkyWalking控制台服务列表">

**③ 链路追踪验证**

使用 **Postman** 向**订单服务**发送下单请求，在 **Trace（追踪）** 页面可查看完整的**调用链路**：

<img src="/imgs/spring-cloud-alibaba-mvp-10-skywalking/084938ae7f67fc69af5cc2976860ee27_MD5.jpg" style="display: block; width: 100%;" alt="SkyWalking链路追踪">

**验证成功标志：**

> ✅ **启动日志**中出现 SkyWalking Agent 加载信息
> ✅ **控制台服务列表**显示四个微服务
> ✅ **Trace 页面**显示完整的调用链路（Gateway → Order → Storage → Account）

## 4. 总结

本文讲解 **SkyWalking 分布式链路追踪** 的接入方法与应用场景，通过**工具介绍** + **架构设计** + **接入实战**三部分内容，帮助读者：

| 学习层次 | 核心收获 |
| ---- | ---- |
| **理解核心概念** | 了解 **SkyWalking** 的**核心能力**（分布式追踪、度量聚合、可视化）和**分层架构**（数据采集层 + 数据处理层） |
| **掌握接入方法** | 熟练配置 **JVM 参数** 接入 **SkyWalking Agent**，实现微服务调用链路的**可视化监控** |
| **提升排查效率** | 通过**全链路追踪**快速定位性能瓶颈和故障点，缩短问题排查时间 |



