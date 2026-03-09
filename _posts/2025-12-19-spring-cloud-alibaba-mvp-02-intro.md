---
title: Spring Cloud Alibaba上手 02：MVP项目介绍
author: fangkun119
date: 2025-12-19 12:00:00 +0800
categories: [微服务, Spring Cloud Alibaba]
tags: [微服务, Spring Cloud Alibaba]
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

## 1. 文档概要

本文讲解 **Spring Cloud Alibaba MVP 实战项目**，涵盖以下知识模块：

| 知识模块 | 说明 |
| ---- | ---- |
| **学习困境与解决方案** | **三大挑战**（碎片化、缺实践、效率低）及 **MVP 解决方案** |
| **技术栈架构** | **六大中间件**：Nacos、Sentinel、Gateway、Seata、Feign、Skywalking |
| **业务场景** | **电商下单**：服务调用、数据修改、**分布式事务**协调 |
| **技术选型** | **Alibaba vs Netflix**、跨技术栈对比（Dubbo、Istio） |
| **版本组合** | **Spring Boot 3.x**、**Cloud 2023.x** 版本适配 |

本文从**项目背景**、**技术选型**、**架构设计**、**版本规划**四个维度系统阐述 **Spring Cloud Alibaba MVP 项目**，帮助读者构建完整的**微服务**认知框架。

## 2. 项目背景与目标

### 2.1 学习困境分析

**Spring Cloud Alibaba** 学习曲线陡峭，面临**三大挑战**：

| 挑战维度 | 具体问题 | 影响 |
| --- | --- | --- |
| **知识碎片化** | 组件众多、配置复杂，缺乏整体框架 | 难以建立系统性理解 |
| **实践缺失** | 传统学习偏重理论，缺乏动手验证 | 知识无法转化为技能 |
| **效率低下** | 零散学习耗时长，无法快速应用 | 学习成本高、见效慢 |

### 2.2 解决方案：MVP 实战项目

本文通过一个 **MVP（最小可行产品）** 项目解决上述问题，其**核心思路**是：

> 通过一个**完整可运行**的项目，串联 **Spring Cloud Alibaba** 的核心组件，实现从理论到实践的快速跨越。

**项目特点：**

| 特点 | 说明 | 价值 |
| --- | --- | --- |
| **完整精炼** | 项目结构完整、代码精炼，覆盖核心组件 | 避免知识碎片化，建立全局认知 |
| **即学即用** | 基于 **Spring Cloud Alibaba 2023.x** 版本 | 快速部署、立即验证，缩短学习周期 |
| **灵活可扩展** | 架构设计清晰，模块边界明确 | 可根据实际业务需求快速适配扩展 |

### 2.3 项目技术栈

**核心中间件及职责：**

| 中间件 | 核心职责 | 典型场景 |
| --- | --- | --- |
| **Nacos** | 服务注册与发现、配置管理 | 微服务注册中心、动态配置 |
| **Feign** | 声明式 HTTP 调用、客户端负载均衡 | 微服务间通信 |
| **Sentinel** | 流量控制、熔断降级 | 服务保护、防止雪崩 |
| **Gateway** | 微服务 API 网关 | 统一入口、路由转发 |
| **Seata** | **分布式事务**一致性 | 跨服务数据一致性保障 |
| **Skywalking** | 分布式链路追踪与监控 | 服务调用链路可视化 |

**架构定位：**

> **六大中间件**协同工作，形成完整的**微服务治理体系**。其中 **Nacos** 是基础设施，**Gateway** 是流量入口，**Feign** 和 **Sentinel** 负责服务调用和保护，**Seata** 保障数据一致性，**Skywalking** 提供可观测性。

### 2.4 业务场景设计

**场景选择：经典电商下单**

选择**电商下单**场景作为演示，原因是：

| 优势 | 说明 |
| --- | --- |
| **易于理解** | 符合直觉，业务逻辑清晰 |
| **覆盖全面** | 涉及服务调用、数据修改、事务协调 |
| **贴近实战** | 真实业务场景，可迁移复用 |

**调用关系：**

**订单服务**接收用户下单请求后，通过微服务中间件协调：

- **库存服务**：扣减商品库存
- **账户服务**：扣减用户余额

调用关系如下图所示：

<img src="/imgs/spring-cloud-alibaba-mvp-02-intro/c52cfaf43815d38b7004df4238de60c0_MD5.jpg" style="display: block; width: 100%;" alt="/imgs/spring-cloud-alibaba-mvp-02-intro/c52cfaf43815d38b7004df4238de60c0_MD5.jpg">

## 3. 技术栈对比

**微服务技术栈**众多，在深入 MVP 架构之前，先阐明**技术栈选型依据**。

### 3.1 选型范围

选型对比涵盖**两个维度**：

| 对比维度 | 说明 |
| ---- | ---- |
| **Spring Cloud 内部** | **Alibaba 技术栈** vs **Netflix 技术栈** |
| **跨技术栈** | **Spring Cloud** vs **Dubbo** vs **Kubernetes Service Mesh** |

### 3.2 Spring Cloud 技术栈对比

**Spring Cloud 微服务体系**包含**两套主流技术栈**：

| 技术栈 | 架构组成 |
| ---- | ---- |
| **Alibaba 技术栈** | Spring Boot + Spring Cloud Alibaba + Gateway |
| **Netflix 技术栈** | Spring Boot + Spring Cloud Netflix + Gateway |

**核心差异**：中间层的组件选择不同——**Alibaba** 还是 **Netflix**。

#### (1) 为什么选择 Spring Cloud Alibaba

基于**维护状态**、**功能性能**、**社区支持**三个维度分析：

| 分析维度 | 说明 |
| ---- | ---- |
| **维护状态** | **Netflix 技术栈**已于 2020 年停止维护，**Alibaba** 持续活跃迭代 |
| **功能性能** | **Alibaba** 在性能、组件精简度、集群规模、分布式事务方面更具优势 |
| **社区支持** | **Alibaba** 国内生态完善，社区活跃度高 |

#### (2) 详细对比分析

| 对比项 | Spring Cloud Alibaba | Spring Cloud Netflix | 核心优势 |
| ---- | ---- | ---- | ---- |
| **维护状态** | 各组件持续快速迭代 | 2020 年停止维护 | 避免技术债务风险 |
| **服务治理** | **Nacos** 统一服务发现与配置 | Eureka + Config + Bus 三套分离 | 降低架构复杂度 |
| **流量防护** | **Sentinel**（损耗 1ms，支持热点参数、集群限流） | Hystrix（损耗 5ms，功能单一） | 性能与功能提升 |
| **分布式事务** | 内置 **Seata** | 无此能力 | 解决分布式事务难题 |
| **通信协议** | 原生整合 **Dubbo** 高性能 RPC | 仅 HTTP（Feign） | 提升服务调用性能 |
| **生产验证** | **Nacos** 经双 11 百万级实例考验 | 大规模集群稳定性不足 | 生产环境支撑更强 |
| **配置推送** | **Nacos** 配置变更毫秒级生效 | Git Webhook 延迟较高 | 实时响应配置变更 |
| **组件数量** | **Nacos** 替代三套组件 | 组件繁多 | 提升运维效率 |
| **本土化** | 中文文档完善，整合阿里云生态 | 英文文档为主 | 降低学习运维成本 |
| **人才储备** | 国内主流技术栈，招聘维护成本低 | 技术团队转向 | 长期维护有保障 |

**延伸阅读**：Spring Cloud Netflix 技术栈详细学习笔记：<https://github.com/fangkun119/manning-smia/tree/master/note>

### 3.3 跨技术栈对比分析

除 **Spring Cloud** 外，**Java 生态**还有多种微服务技术栈，各有适用场景。

| 维度          | Spring Cloud | Quarkus/Micronaut   | Vert.x      | Dubbo         | Istio + Spring Boot |
| ----------- | ------------ | ------------------- | ----------- | ------------- | ------------------- |
| **启动速度**    | 慢（秒级）        | **极快（毫秒级）**         | 快           | 中等            | 慢                   |
| **内存占用**    | 高            | **极低**              | 低           | 中等            | 高                   |
| **学习曲线**    | 平缓           | 陡峭                  | 陡峭          | 中等            | **极陡峭**             |
| **社区生态**    | **最丰富**      | 较小                  | 中等          | 国内强           | 云原生生态               |
| **云原生适配**   | 良好           | **优秀**              | 良好          | 一般            | **卓越**              |
| **多编程语言支持** | 差            | 一般                  | **优秀**      | 差             | **卓越**              |
| **运维复杂度**   | 中等           | 低                   | 中等          | 中等            | **极高**              |
| **适用场景**    | **传统企业级应用**  | **Serverless/边缘计算** | **高并发异步场景** | **内部RPC密集系统** | **大型多语言混合架构**       |

## 4. 中间件版本组合

微服务包含数量众多的中间件，中间件之间的**兼容性**很重要。[官网](https://sca.aliyun.com/docs/2023/overview/version-explain/) 提供组件版本建议，我们根据官方建议来选择中间件版本作为 **MVP 项目**。

### 4.1 新版组合

截至文档编写时，**Spring Cloud Alibaba** 官网给出的最近建议是 **2023.0.x**

<img src="/imgs/spring-cloud-alibaba-mvp-02-intro/156a0b9db55d033c2003982b6ef62072_MD5.jpg" style="display: block; width: 380px;" alt="/imgs/spring-cloud-alibaba-mvp-02-intro/156a0b9db55d033c2003982b6ef62072_MD5.jpg">

包含如下的中间件组合

| 框架                   | 版本         |
| -------------------- | ---------- |
| **Spring Boot**          | **3.2.4**      |
| **Spring Cloud**         | **2023.0.1**   |
| **Spring Cloud Alibaba** | **2023.0.1.0** |

项目使用 **Spring Cloud Alibaba 2023.0.1.0** 会自动导入如下版本的依赖

| **Spring Cloud Alibaba Version** | **Sentinel Version** | **Nacos Version** | **RocketMQ Version** | **Seata Version** |
| ---------------------------- | ---------------- | ------------- | ---------------- | ------------- |
| 2023.0.1.0                   | 1.8.6            | 2.3.2         | 5.1.4            | 2.0.0         |

具体到 **mvn.pom** 中，可以使用如下配置

```xml
<spring.boot.version>3.2.4</spring.boot.version>
<spring-cloud.version>2023.0.1</spring-cloud.version>
<spring-cloud-alibaba.version>2023.0.1.0</spring-cloud-alibaba.version>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>${spring.boot.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
<!-- Spring Cloud依赖 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-dependencies</artifactId>
    <version>${spring-cloud.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>

<!-- Spring Cloud Alibaba依赖 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-dependencies</artifactId>
    <version>${spring-cloud-alibaba.version}</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>
```

需要说明，上面使用 **2023 年版本**的，只是微服务项目中 **Maven 依赖**。它们负责与中间件交互，而中间件本身仍在持续的版本更新升级。

### 4.2 旧版组合

旧版通过[官网](https://sca.aliyun.com/docs/2023/overview/version-explain/)顶部的**下拉列表**进行选择，使用方法相同

<img src="/imgs/spring-cloud-alibaba-mvp-02-intro/72e575d87e9cfc43f589f1e1d159e009_MD5.jpg" style="display: block; width: 380px;" alt="/imgs/spring-cloud-alibaba-mvp-02-intro/72e575d87e9cfc43f589f1e1d159e009_MD5.jpg">

## 5. 总结

本文系统介绍 **Spring Cloud Alibaba MVP 实战项目**，通过**项目背景分析** + **技术栈选型对比** + **架构设计说明** + **版本规划**的完整闭环，帮助读者：

| 学习层次 | 核心收获 |
| ---- | ---- |
| **建立体系化认知** | 理解 **Spring Cloud Alibaba** 三大学习困境、**Alibaba vs Netflix** 技术栈差异、六大中间件协同工作架构 |
| **掌握实战能力** | 熟练运用 **Nacos**、**Sentinel**、**Gateway**、**Seata** 构建**微服务体系**，掌握**电商下单**场景设计与服务协调 |
| **优化技术决策** | 理解跨技术栈（**Dubbo**、**Istio**）选型依据，掌握 **Spring Boot 3.x**、**Cloud 2023.x** 版本适配方案，具备生产环境技术选型能力 |

