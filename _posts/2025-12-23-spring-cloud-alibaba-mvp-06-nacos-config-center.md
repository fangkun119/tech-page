---
title: Spring Cloud Alibaba上手 06：Nacos配置中心
author: fangkun119
date: 2025-12-23 12:00:00 +0800
categories: [微服务, Spring Cloud Alibaba]
tags: [微服务, Spring Cloud Alibaba, Nacos]
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

## 1. 介绍

### 1.1 文档概要

本文系统讲解 **Nacos 配置中心**，涵盖以下知识模块：

| 知识模块 | 说明 |
| ---- | ---- |
| **配置中心作用** | 配置分散问题、集中管理价值、动态刷新能力 |
| **配置托管步骤** | 本地配置导入、配置分组策略、文件上传操作 |
| **分组命名空间** | Group 分组机制、Namespace 隔离策略 |
| **配置文件汇总** | 微服务配置分类、中间件配置管理 |

本文从**作用分析**、**托管步骤**、**分组策略**到**配置汇总**四个维度系统阐述 **Nacos 配置中心**，帮助读者掌握微服务**配置集中管理**的标准实践。

### 1.2 配套资源

代码

| 资源类型           | 说明                               | 链接                                                                                                                   |
| -------------- | -------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| **项目源码**       | Spring Cloud Alibaba 2023 完整示例代码 | [github.com/fangkun119/spring-cloud-alibaba-2023-demo](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo) |
| **Postman 集合** | API 测试用例集合，便于快速验证功能              | [github.com/fangkun119/postman-workspace](https://github.com/fangkun119/postman-workspace)                           |

环境搭建：《[Spring Cloud Alibaba上手 03：中间件环境]({% post_url 2025-12-20-spring-cloud-alibaba-mvp-03-env %})》

## 2. 配置中心的作用

单体应用拆分为微服务后，配置分散且难以维护。配置中心实现配置集中管理和规范化，并支持动态刷新。

**官方文档**：[Nacos 配置中心](https://sca.aliyun.com/docs/2023/user-guide/nacos/advanced-guide/)
## 3. 配置托管步骤

配置托管通过**本地配置**、**分组策略**、**上传配置**三个步骤实现微服务配置的集中管理。

### 3.1 本地配置修改

#### (1) 配置操作

在本地配置文件中指定要从 Nacos 导入的远程配置。

**配置方式：**

**Spring Boot 2.4+** 使用 `spring.config.import`（云原生多配置管理支持），**老版本**使用 `bootstrap.yml`。

```yml
spring:
  application:
    name: tlmall-order
  config:
    import:
      # 微服务业务配置
      - optional:nacos:${spring.application.name}.yml
      # 数据库公用配置
      - optional:nacos:db-common.yml?group=DEFAULT_GROUP&refreshEnabled=true
```

**配置说明：**

| 配置项                          | 说明                                          |
| ---------------------------- | ------------------------------------------- |
| `${spring.application.name}` | Spring Boot 占位符，自动替换为应用名（如 `tlmall-order`）  |
| `optional:nacos:...yml`      | 从 Nacos 加载指定 Data ID 的配置，`optional` 表示配置非强制 |
| `group=DEFAULT_GROUP`        | 指定**配置分组**，推荐显式配置                           |
| `refreshEnabled=true`        | 开启动态刷新，配置变更时自动生效                            |

#### (2) 理解Nacos配置分组

配置分组（Group）是 Nacos 对配置集的逻辑分组机制。

**分组价值：**

| 价值 | 说明 | 典型场景 |
| ---- | ---- | ---- |
| **区分相同 Data ID** | 通过 Group 区分相同 Data ID 的不同配置 | AB 测试的实验组与对照组 |
| **场景隔离** | 不同应用使用相同配置项时通过 Group 隔离 | 多个服务的 `database_url`、`MQ_Topic` |
| **模块化管理** | 按功能模块分组，便于管理和维护 | `DATABASE` 组、`MIDDLEWARE` 组 |
| **灰度发布** | 动态切换 Group 实现配置快速切换 | 灰度发布、环境切换 |

**分组建议：**

> 虽然 Nacos 支持不指定分组（全部使用 `DEFAULT_GROUP`），但**不推荐**。即使在同一 Namespace 内，也应通过 Group 区分不同业务域，提升配置可维护性。

### 3.2 远程配置上传

#### (1) 创建或选择 Namespace

点击"创建配置"或选择已有 Namespace。

<img src="imgs/spring-cloud-alibaba-mvp-06-nacos-config-center/ee7fb608a3d4a84e4348083a8fd19558_MD5.jpg" style="display: block; width: 380px;" alt="Nacos配置中心创建或选择Namespace">

#### (2) 创建配置项

| 步骤 | 操作 |
| ---- | ---- |
| **填写元数据** | 填入"配置分组"（Group）和"配置名称"（Data ID） |
| **选择格式** | 勾选"配置格式"（YAML、Properties 等） |
| **填入内容** | 填入配置内容（与本地配置格式一致） |

以 `db-common.yml` 为例，内容为微服务连接 MySQL 的配置：

<img src="imgs/spring-cloud-alibaba-mvp-06-nacos-config-center/08693e6f0655d79f3cef0de3a8f56b25_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置中心创建db-common.yml配置">

其他微服务配置和公用配置上传方法相同。

## 4. 配置文件汇总

### 4.1 微服务配置

各微服务（`tlmall-account`、`tlmall-storage`、`tlmall-order`、`tlmall-gateway`）的配置托管在 `public` 命名空间的 `DEFAULT_GROUP` 下，包括**业务专用配置**和**公用配置**。

<img src="imgs/spring-cloud-alibaba-mvp-06-nacos-config-center/3ef26960aebdb4ba98a12fb77731d952_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置中心public命名空间配置列表">

配置文件列表如下：

| Data ID | 类型 | 用途 | GitHub链接 |
| ---- | ---- | ---- | ---- |
| `db-common.yml` | 公用 | 微服务数据库连接 | [GitHub链接](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/db-common.yml) |
| `nacos-discovery.yml` | 公用 | 微服务注册到 Nacos | [GitHub链接](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/nacos-discovery.yml) |
| `seata-client.yml` | 公用 | 微服务访问 Seata | [GitHub链接](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/seata-client.yml) |
| `sentinel-dashboard.yml` | 公用 | 微服务访问 Sentinel | [GitHub链接](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/sentinel-dashboard.yml) |
| `tlmall-account.yml` | 专用 | 账户服务配置 | [GitHub链接](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/tlmall-account.yml) |
| `tlmall-storage.yml` | 专用 | 库存服务配置 | [GitHub链接](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/tlmall-storage.yml) |
| `tlmall-order.yml` | 专用 | 订单服务配置 | [GitHub链接](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/tlmall-order.yml) |
| `tlmall-gateway.yml` | 专用 | 网关服务配置 | [GitHub链接](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/tlmall-gateway.yml) |

**中间件配置**

中间件Seata也支持Nacos远程配置，把它托管在独立命名空间，避免与微服务配置混淆。

<img src="imgs/spring-cloud-alibaba-mvp-06-nacos-config-center/e95f37828ca519ab5a47563c6440d6b5_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置中心seata命名空间配置">

配置文件列表如下：

| Data ID                  | 类型  | 用途            | GitHub链接                                                                                                                                              |
| ------------------------ | --- | ------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| `seataServer.properties` | 中间件 | Seata 事务协调器配置 | [GitHub链接](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/seata/SEATA_GROUP/seataServer.properties) |
## 5. 总结

本文从**作用分析**、**托管步骤**、**分组策略**到**配置汇总**，系统讲解 **Nacos 配置中心**，帮助读者：

| 学习层次 | 核心收获 |
| ---- | ---- |
| **理解核心价值** | 掌握配置中心解决微服务配置分散问题的原理、动态刷新机制、集中管理优势 |
| **具备实战能力** | 熟练运用 **spring.config.import**、**配置分组**、**命名空间**实现配置托管，掌握微服务配置与中间件配置分类管理 |
| **优化配置方案** | 理解 **Group** 与 **Namespace** 隔离策略，掌握 **Spring Boot 3.x** 配置导入最佳实践 |

