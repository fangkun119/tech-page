---
title: Spring Cloud Alibaba深入 07：Spring Cloud Gateway
author: fangkun119
date: 2026-03-24 12:00:00 +0800
categories: [微服务, Spring Cloud Alibaba]
tags: [微服务, Spring Cloud Alibaba, Spring Cloud Gateway]
pin: false
math: true
mermaid: true
image:
  path: /imgs/cover/spring_cloud_alibaba_in_depth.png
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

### 1.1 文章概要

本文讲解 **Spring Cloud Gateway** 的核心原理与实战应用，涵盖以下知识模块：

| 知识模块 | 说明 |
| ---- | ---- |
| **核心概念** | **三大组件**（Route、Predicate、Filter）、**工作原理**、**路由匹配**机制 |
| **基础配置** | **路由配置**、**Predicate 断言**、**Filter 过滤器**、**动态路由** |
| **高级特性** | **限流策略**（Redis+Lua、Sentinel）、**统一异常处理**、**跨域配置** |
| **实战应用** | **路由配置实践**、**自定义扩展** |

本文从**核心概念**、**基础配置**、**高级特性**、**实战应用**四个维度系统阐述 **Spring Cloud Gateway**，帮助读者构建完整的**微服务网关**认知框架。

**版本说明**：本文基于 [Spring Cloud Gateway 4.1.x](https://github.com/spring-cloud/spring-cloud-gateway/tree/4.1.x) 编写。

### 1.2 配套资源

#### (1) 示例代码

| 资源类型       | 链接                                                                                                                                                                                                                                                                                                                                                                                                                 |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| 示例代码       | 复用 [spring-cloud-alibaba-2023-nacos](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/) 的代码，用到其中两个模块：<br>- [tlmall-nacos-demo-order](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-nacos-demo-order)<br>- [tlmall-nacos-demo-gateway](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-nacos-demo-gateway) |
| Postman 集合 | [github.com/fangkun119/postman-workspace](https://github.com/fangkun119/postman-workspace)                                                                                                                                                                                                                                                                                                                         |

#### (2) 前置知识

- **环境配置**：[Spring Cloud Alibaba上手 03：中间件环境]({% post_url 2025-12-20-spring-cloud-alibaba-mvp-02-env %})
- **快速上手**：[Spring Cloud Alibaba上手 08：Spring Cloud Gateway]({% post_url 2025-12-26-spring-cloud-alibaba-mvp-08-gateway %})

## 2. 基础知识

### 2.1 主要价值与场景

**Spring Cloud Gateway** 是 **Spring Cloud** 官方推出的第二代微服务网关，用于替代早期的 **Zuul** 网关，在微服务架构中承担 **统一流量入口** 的核心职责。

#### (1) 主要价值

**网关** 作为微服务的 **统一入口**，集中处理所有外部请求，提供以下关键能力：

| 主要能力        | 说明                     | 业务价值              |
| ----------- | ---------------------- | ----------------- |
| 统一路由管理     | 前端只需知晓网关地址，无需关注后端微服务位置  | 降低客户端复杂度，解耦前后端   |
| 通用功能集中处理   | 权限校验、限流、黑白名单等跨服务功能统一实现 | 避免重复代码，提升可维护性    |
| 流量分流与负载均衡  | 部署多个网关实例，水平扩展系统吞吐量      | 多实例线性扩展，适应并发度要求 |

**职责定位**：

> 网关位于 **客户端与微服务之间**，是所有流量的必经之路，承担 **路由转发**、**安全防护**、**流量控制** 等职责。


```
客户端请求 → Gateway网关（统一入口）
                ├─ 权限校验
                ├─ 限流控制
                ├─ 路由转发
                └─ 负载均衡
                    ↓
        后端微服务（订单/商品/会员等）
```

#### (2) 技术对比

**主流网关对比：**

| 网关方案                     | 主要特性                                          | 适用场景                        | 官网                                                                     |
| ------------------------ | --------------------------------------------- | --------------------------- | ---------------------------------------------------------------------- |
| **Spring Cloud Gateway** | **Spring微服务栈原生**，与Nacos、Sentinel无缝集成，JVM统一技术栈 | **Spring Cloud微服务体系**，零集成成本 | [官网](https://spring.io/projects/spring-cloud-gateway)                  |
| **Higress**              | **AI网关能力**（LLM流量治理）、**Wasm插件扩展**、一体化设计        | **开源自建**，需统一管理传统业务和AI/LLM流量 | [官网](https://higress.io/)                                              |
| **MSE云原生网关**             | **免运维托管**、同城多活、企业级SLA、阿里云深度集成                 | **企业生产**（阿里云用户），要托管式高可用     | [官网](https://www.aliyun.com/product/mse)                               |
| **Nginx/Kong**           | **成熟老牌**、高性能反向代理、插件生态完善                       | **高并发通用网关**，已有成熟Nginx体系     | [Nginx](https://nginx.org) \| [Kong](https://konghq.com)               |
| **Kubernetes Ingress**   | **K8s原生声明式**、自动化部署，云原生集成                      | **K8s容器化**基础路由，原生YAML管理     | [文档](https://kubernetes.io/docs/concepts/services-networking/ingress/) |

**Higress vs MSE云原生网关**：

| 技术名称         | 介绍                                                            |
| ------------ | ------------------------------------------------------------- |
| **Higress**  | 阿里云推出的**开源网关**，适合自建Kubernetes、虚拟机或混合环境，适合有自运维能力、希望深度定制的团队     |
| **MSE云原生网关** | 基于Higress的**商业化托管服务**，在开源能力之上增强多活、高可用、企业级安全与可观测性，并提供控制台和SLA支持 |

### 2.2 微服务网关的必要性

#### (1) 挑战与问题

在微服务架构中，系统被拆分为多个微服务，客户端直接调用各服务会面临**四大挑战**：

| 挑战维度  | 具体问题                | 影响          |
| ----- | --------------------- | ----------- |
| 配置复杂  | 客户端需维护所有微服务地址和端口配置   | 维护成本高，变更影响大 |
| 认证困难  | 各服务认证方式不同，客户端需适配多种认证  | 开发复杂度增加     |
| 跨域问题  | 存在跨域请求、防火墙限制、浏览器不友好协议 | 调用链复杂，性能损耗  |
| 重构困难  | 服务重新划分时需同步更新所有调用方     | 系统迭代困难      |

> **主要问题**：缺少**统一流量入口**，导致客户端复杂、运维困难、无法统一管控。

#### (2) 主要价值

为解决上述问题，微服务架构引入 **API 网关** 作为 **统一流量入口**：

<img src="imgs/spring-cloud-alibaba-09-gateway/010c99e621f0519aa45d0d1a2fa1906e_MD5.jpg" style="display: block; width: 500px;" alt="微服务网关统一入口结构图">

**API 网关** 提供简单、有效且统一的 **API 路由管理**，作为系统 **统一入口** 实现与业务解耦的 **公用逻辑**。

**主要能力**：

| 能力维度    | 功能说明                | 业务价值          |
| ------- | --------------------- | ------------- |
| 路由转发     | 根据服务名动态路由到后端微服务      | 简化前端配置，降低耦合    |
| 通用功能集中   | 统一实现认证、鉴权、限流、黑白名单    | 避免重复代码，提升一致性  |
| 流量管理     | 在入口层进行流量控制、监控、防刷     | 统一管控，提升系统稳定性  |
| 动态服务发现   | 通过注册中心自动感知服务实例变化     | 无需手动配置，支持弹性伸缩 |

> **价值总结**：网关通过 **统一入口** 和 **集中治理**，解决了微服务架构中客户端复杂、运维困难、缺乏管控的 **三大痛点**。

### 2.3 技术特性

**Spring Cloud Gateway** 是 **Spring Cloud** 生态系统中的第二代微服务网关，旨在替代早期的 **Netflix Zuul**，为微服务架构提供统一、高效的 **API 路由管理** 和 **过滤器链扩展** 能力。

**官方文档**：[https://docs.spring.io/spring-cloud-gateway/reference/](https://docs.spring.io/spring-cloud-gateway/reference/)

**主要特性**

| 特性维度          | 技术实现                                                              | 业务价值            |
| ------------- | ----------------------------------------------------------------- | --------------- |
| 响应式编程模型       | 基于 **Spring WebFlux** + **Netty** + **Project Reactor**，采用非阻塞 I/O | 高并发处理、低资源消耗     |
| 过滤器扩展机制       | 通过过滤器链实现 **安全认证**、**权限校验**、**监控**、**限流** 等 **横切关注点**              | 灵活扩展、业务逻辑解耦     |
| 动态路由配置        | 同时支持 **配置文件** 和基于 **注册中心** 的 **动态路由规则**                           | 无需重启服务、动态调整流量策略 |
| Spring 生态无缝集成 | 与 **Nacos**（服务发现）、**Sentinel**（流控熔断）等 Spring Cloud 组件深度集成         | 降低集成成本、统一技术栈    |

**技术约束**

**Spring Cloud Gateway** 基于 **响应式编程模型** 构建，不支持传统的 **Servlet 容器**（如 Tomcat），也不能打包为 **WAR 部署**。其运行必须依赖 **Spring WebFlux** 和 **Netty** 异步框架。


### 2.4 快速接入

#### (1) 接入步骤

接入 **Gateway** 需要完成以下关键步骤：

| 步骤       | 操作                            | 关键产出        |
| -------- | ----------------------------- | ---------- |
| 创建网关服务   | 构建独立的 **Gateway** 模块              | 网关服务工程    |
| 添加必需依赖   | 引入 **Gateway**、**Nacos**、**Loadbalancer** | Maven 依赖配置 |
| 配置路由规则   | 定义路径到服务的映射关系                  | 路由配置文件    |

#### (2) 创建服务

构建独立的网关服务模块 `tlmall-nacos-demo-gateway`，作为独立微服务部署，承担统一流量入口职责。

<img src="imgs/spring-cloud-alibaba-09-gateway/72f82965b45f91a150899285e8c6026d_MD5.jpg" style="display: block; width: 100%;" alt="Gateway 网关服务模块结构">

**完整代码**：[microservices/tlmall-nacos-demo-gateway](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-nacos-demo-gateway)

#### (3) 依赖配置

**必需依赖**：在 **Spring Cloud Alibaba** 技术栈中，**Gateway** 需要以下三个必需依赖协同工作：

| 依赖                                            | 职责                  |
| --------------------------------------------- | ------------------- |
| `spring-cloud-starter-gateway`                | 路由转发和断言过滤           |
| `spring-cloud-starter-alibaba-nacos-discover` | 服务注册与动态发现           |
| `spring-cloud-starter-loadbalancer`           | 客户端负载均衡，根据服务名选择调用实例 |

**pom.xml 配置**：

```xml
<!-- Spring Cloud Gateway 网关 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<!-- Nacos 服务注册与发现 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
</dependency>

<!-- Loadbalancer 负载均衡器 -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-loadbalancer</artifactId>
</dependency>
```

> **⚠️ 版本兼容性说明**：
> - **Spring Cloud Gateway 4.1.x** 基于 **Spring Boot 3.x** 构建
> - 需要使用 **Jakarta EE 9+** 命名空间（`jakarta.*` 而非 `javax.*`）
> - 确保使用 **Spring Cloud 2022.x** 或更高版本
> - Loadbalancer 依赖名称从 `spring-cloud-loadbalancer` 更新为 `spring-cloud-starter-loadbalancer`

**完整代码**：[pom.xml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/pom.xml "pom.xml")

#### (4) 路由配置

##### ① 配置语法

**路由配置**与微服务的其他配置一样，通过 **YAML** 配置文件完成。配置可以存放在本地 `application.yml` 文件中，也可以托管在 **Nacos 配置中心**。

**配置示例**：

```yml
spring:
  cloud:
    gateway:
      routes:
        - id: order_route  # 路由 ID，全局唯一
          uri: lb://tlmall-order  # lb:// 表示使用负载均衡
          predicates:
            - Path=/orders/**   # 路径匹配规则
```

**配置要素**：

| 要素         | 配置键                 | 说明                                                  | 示例值                 |
| ---------- | ------------------- | --------------------------------------------------- | ------------------- |
| **路由 ID**  | `routes.id`         | 路由的唯一标识符                                          | `order_route`       |
| **目标 URI** | `routes.uri`        | 转发目标地址，支持 `lb://服务名` 负载均衡格式（lb = Load Balancer） | `lb://tlmall-order` |
| **断言规则**   | `routes.predicates` | 路由匹配条件集合，所有断言满足时请求才会被该路由处理                        | `Path=/orders/**`    |
| **过滤器链**   | `routes.filters`    | 请求和响应处理器集合（可选），在转发前后对请求和响应进行预处理和后处理                    | 详见第 5 章             |

##### ② 方法1：本地配置

**配置方式**：通过本地 `application.yml` 文件定义路由规则。

**多服务路由配置示例**：

```yml
server:
  port: 18888
spring:
  application:
    name: tlmall-gateway
  cloud:
    gateway:
      # 路由配置：路由 ID、目标 URI、断言
      routes:
        # 用户服务路由
        - id: user_route
          uri: lb://tlmall-user-openfeign
          predicates:
            - Path=/users/**
        # 订单服务路由
        # 测试地址：http://localhost:18888/order/getOrder?userId=fox
        - id: order_route         # 路由 ID，全局唯一
          uri: lb://tlmall-order  # lb 表示负载均衡，tlmall-order 为下游微服务名
          predicates:
            # 断言：路径匹配时进行路由
            - Path=/orders/**
            # Header 匹配（可选）：请求需包含 X-Request-Id 请求头，值匹配 \d+ 正则表达式
            #- Header=X-Request-Id, \d+
```

**完整代码**：[application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/resources/application.yml "application.yml")

##### ③ 方法 2：Nacos 远程配置

> **配置说明**：本文主要采用方法①（本地配置）进行演示。方法②（**Nacos** 远程配置）适用于生产环境，可实现配置的集中管理与动态刷新。详细实现步骤请参考：
>
> - [Spring Cloud Alibaba 上手 05：Nacos配置中心]({% post_url 2025-12-23-spring-cloud-alibaba-mvp-05-nacos-config-center %})
> - [Spring Cloud Alibaba 深入 04：Nacos配置中心]({% post_url 2026-02-11-spring-cloud-alibaba-in-depth-04-nacos-config-center %})

**方案优势**：将路由配置托管在 **Nacos** 配置中心，支持动态刷新与集中管理，无需重启服务。

**配置对比**：

| 配置方式    | 优势                  | 劣势              | 适用场景        |
| ------- | ------------------- | --------------- | ----------- |
| 本地配置    | 配置简单，依赖少            | 修改需重启服务，缺乏集中管理  | 开发测试环境      |
| **Nacos** 托管 | 集中管理，动态刷新，无需重启     | 需要额外的 **Nacos** 依赖     | 生产环境，多实例部署 |

**实施步骤**：

###### 步骤 1：引入依赖

```xml
<!-- Nacos 配置中心 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

###### 步骤 2：配置连接信息

在本地 `application.yml` 中配置 **Nacos** 配置中心连接参数：

```yml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848  # Nacos 配置中心地址
        # namespace: 留空使用默认命名空间，或填写命名空间ID（不是名称）
        group: DEFAULT_GROUP         # 配置分组
        file-extension: yml          # 配置文件格式
        refresh-enabled: true        # 开启配置自动刷新
```

> **动态刷新说明**：**Gateway** 路由配置通过 `refresh-enabled: true` 自动刷新，无需 `@RefreshScope` 注解（其他业务配置仍需此注解）

###### 步骤 3：创建远程配置

在 **Nacos** 配置中心控制台创建远程配置文件：

| 配置项     | 值                    | 说明              |
| ------- | -------------------- | --------------- |
| Data ID | `tlmall-gateway.yml` | 与步骤 4 的 import 配置一致 |
| Group   | `DEFAULT_GROUP`      | 与步骤 2 的 group 配置一致  |
| 配置格式    | `YAML`               | 与内容格式一致         |

**配置内容示例**：

```yml
spring:
  cloud:
    gateway:
      routes:
        # 用户服务路由
        - id: user_route
          uri: lb://tlmall-user-openfeign
          predicates:
            - Path=/users/**
        # 订单服务路由
        - id: order_route
          uri: lb://tlmall-order
          predicates:
            - Path=/orders/**
```

###### 步骤 4：导入远程配置

在本地 `application.yml` 中配置导入远程配置：

```yml
spring:
  config:
    import:
      - optional:nacos:tlmall-gateway.yml    # 导入网关路由配置
```

> **⚠️ 配置优先级**：远程配置优先级高于本地配置，冲突时远程配置会覆盖本地配置。

###### 远程配置说明与注意事项

**配置分离原则**：

| 配置类型                 | 配置位置                 | 说明                      |
| -------------------- | -------------------- | ----------------------- |
| **Gateway** 路由配置        | **Nacos** 配置中心           | 支持动态刷新，集中管理              |
| 服务发现配置             | 本地 `application.yml` | `spring.cloud.nacos.discovery` |
| 其他业务配置（需动态刷新）      | **Nacos** 配置中心           | 需在 Bean 上添加 `@RefreshScope` 注解 |

**动态刷新机制**：

| 配置类型         | 刷新方式                    | 前置条件                    |
| ------------ | ----------------------- | ----------------------- |
| **Gateway** 路由配置 | 自动刷新，无需注解（**Gateway** 内置支持） | `refresh-enabled: true` |
| 其他业务配置       | 需添加 `@RefreshScope` 注解  | `refresh-enabled: true` |

#### (5) 测试验证

**步骤 1：启动服务**

启动 **订单服务** 与 **Gateway** 网关服务：

<img src="imgs/spring-cloud-alibaba-09-gateway/459e2ad39febdbc4bb3bf942e44097ec_MD5.jpg" style="display: block; width: 420px;" alt="启动订单服务与网关服务">

**步骤 2：验证路由**

通过 **Postman** 向网关发送请求，验证请求是否正确路由到 **订单服务**：

<img src="imgs/spring-cloud-alibaba-09-gateway/a67d6431702d17d4dcfb1038262c49f1_MD5.jpg" style="display: block; width: 100%;" alt="Postman 测试网关路由">

## 3. 工作原理

### 3.1 关键概念

**Spring Cloud Gateway** 的工作原理围绕三个关键概念展开：**路由**、**断言** 和 **过滤器**。

#### (1) 路由（Route）

路由是网关的**基本配置单元**，定义了请求从入口到目标的完整映射关系。

**配置示例**：

以下配置将网关的 `/orders/**` 路径请求转发到 **订单服务** `tlmall-order`：

```yml
spring:
  cloud:
    gateway:
      # 路由配置
      routes:
        - id: order_route         # 路由 ID
          uri: lb://tlmall-order  # 目标 URI
          predicates:             # 断言集合
            - Path=/orders/**
```

**路由配置要素**：

| 配置要素       | 说明                                                     | 配置示例               |
| ---------- | -------------------------------------------------------- | ------------------ |
| **路由 ID**    | 路由的唯一标识符                                                  | `order_route`     |
| **目标 URI**   | 请求转发的目标地址，支持 `lb://微服务名` 负载均衡格式（`lb` = Load Balancer）          | `lb://tlmall-order` |
| **断言集合**     | 判断请求是否匹配该路由的条件集合，**所有条件必须同时满足**                               | `Path=/orders/**` |
| **过滤器集合** | 对请求和响应进行处理的过滤器链（可选配置）                                        | 详见第 5 章           |

#### (2) 断言（Predicate）

**断言**负责判断请求是否满足路由条件，只有满足**所有断言条件**的请求才会被该路由处理。

以上面的配置为例，只有请求路径前缀为`/orders`的请求才会匹配这条路由规则。

```yml
spring:
  cloud:
    gateway:
      routes:
        - id: order_route
          uri: lb://tlmall-order
          predicates:
            # 断言
            - Path=/orders/**
```

除了路径匹配，断言还允许开发者根据 **HTTP 请求**的任意信息进行匹配，例如请求路径、HTTP 方法、请求头、Cookie、请求参数、客户端 IP、域名、时间范围等。

#### (3) 过滤器（Filter）

过滤器负责在请求转发前后对**请求**和**响应**进行处理，是 **Gateway** 功能扩展的**工作机制**。

**配置示例**：

在订单服务路由中添加请求头过滤器：

```yml
spring:
  cloud:
    gateway:
      routes:
        - id: order_route
          uri: lb://tlmall-order
          predicates:
            - Path=/orders/**
          filters:
            # 为所有请求添加追踪ID
            - AddRequestHeader=X-Trace-Id, ${random.value}
            # 去除路径前缀 /api
            - StripPrefix=1
```

**过滤函数分类（[官方文档](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway-server-webmvc/gateway-handler-filter-functions.html)）：**

> **🆕 Gateway 4.1.x 新特性**：在传统的 Gateway Filter 基础上，4.1.x 版本引入了 **Filter Functions API**，提供了更灵活、更符合函数式编程风格的过滤方式。

| 分类                            | 处理对象                 | 执行时机          | 典型应用                    |
| ----------------------------- | -------------------- | ------------- | ----------------------- |
| **Before Filter Functions**   | 请求（`ServerRequest`）  | 路由转发**之前**    | 添加请求头、修改请求路径、追加请求参数     |
| **After Filter Functions**    | 响应（`ServerResponse`） | 下游服务返回**之后**  | 添加响应头、修改响应状态码、调整响应内容    |
| **Advanced Filter Functions** | 请求 **和** 响应          | 请求前 **和** 响应后 | 熔断、**限流**、**重试**、认证令牌转发 |

**过滤器技术演进**：

| 版本特性 | Gateway 2.x | Gateway 4.1.x |
|---------|-------------|---------------|
| **API风格** | GatewayFilter接口 | **Filter Functions**（函数式）+ GatewayFilter |
| **编程模型** | 面向对象 | 面向对象 + **函数式编程** |
| **灵活性** | 需要实现接口 | 支持 **Lambda表达式**，更简洁 |
| **类型安全** | 运行时检查 | **编译时类型检查** |

> **💡 最佳实践**：在 Gateway 4.1.x 中，推荐优先使用 **Filter Functions API** 进行简单过滤逻辑的实现，对于复杂场景仍可使用传统的 GatewayFilter 接口。

**过滤器类型：**

| 类型                 | 作用范围 | 配置方式      | 典型场景       |
| ------------------ | ---- | --------- | ---------- |
| **Gateway Filter** | 单个路由 | 需通过配置显式指定 | 特定路由的个性化处理 |
| **Global Filter**  | 所有路由 | 自动生效，无需配置 | 全局统一的处理逻辑  |

### 3.2 请求处理流程

#### (1) 请求处理完整流程

**处理流程说明**：当客户端请求到达网关时，会经过以下处理流程：

<img src="imgs/spring-cloud-alibaba-09-gateway/564bdbf2f6df599323903373ad45633a_MD5.jpg" style="display: block; width: 100%;" alt="Gateway 请求处理流程">

**处理阶段划分**：

| 处理阶段       | 说明                      | 关键组件                             |
| ---------- | ------------------------- | -------------------------------- |
| **路由映射**   | 请求到达后，根据**断言条件**匹配对应的路由规则 | [**RoutePredicateHandlerMapping**](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.2/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/handler/RoutePredicateHandlerMapping.java) |
| **过滤器链执行** | 匹配成功后，执行特定路由的**过滤器链**     | **Gateway Filter Chain**         |
| **代理请求**   | 通过代理层将请求转发到**目标微服务**      | **Netty Routing Filter**         |

> **过滤器执行机制**：图中过滤器之间用**虚线**分隔，表示过滤器可以在代理请求的**前后**均执行逻辑——**所有 Pre 过滤器**先执行，然后执行代理请求，代理请求完成后，再执行**Post 过滤器**。

**路由映射工作原理**：

其中的**路由映射**通过 [**RoutePredicateHandlerMapping**](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.2/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/handler/RoutePredicateHandlerMapping.java) 来实现，流程如下：

| 步骤 | 阶段 | 说明 |
| --- | --- | --- |
| ① | **配置解析** | 启动时加载并解析配置文件中的路由定义 |
| ② | **路由匹配** | 接收请求后，根据**断言条件**匹配对应路由 |
| ③ | **过滤器链构建** | 为匹配的路由构建**过滤器处理链** |
| ④ | **请求转发** | 通过代理层将请求转发到目标服务 |

#### (2) 与 Spring MVC 的对比

**Spring Cloud Gateway** 的请求处理流程与 **Spring MVC** 有相似之处，但在**编程模型**和**实现机制**上有本质区别：

| 对比维度 | Spring MVC | Spring Cloud Gateway | 主要差异 |
| --- | --- | --- | --- |
| **编程模型** | 阻塞 I/O | **响应式非阻塞 I/O** | 同步阻塞 vs 异步非阻塞 |
| **映射机制** | `HandlerMapping` | [`RoutePredicateHandlerMapping`](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.2/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/handler/RoutePredicateHandlerMapping.java) | 方法映射 vs 路由匹配 |
| **处理单元** | **Controller** 方法 | **Filter** 链路 | 方法调用 vs 过滤器链 |
| **底层框架** | Spring MVC（Servlet） | **Spring WebFlux**（Reactor） | Servlet 容器 vs 响应式框架 |
| **线程模型** | 每请求一线程（高并发下线程数多） | **事件循环**少量线程（资源利用率高） | 阻塞线程 vs 非阻塞事件循环 |

> **结论**：**Gateway** 采用**响应式编程模型**，通过**事件循环**和**非阻塞 I/O** 实现高并发处理，相比 **Spring MVC** 的阻塞模型具有更低的资源消耗和更高的吞吐量。

### 3.3 负载均衡机制

#### (1) 过滤器机制

[**ReactiveLoadBalancerClientFilter**](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/ReactiveLoadBalancerClientFilter.java) 是 **Spring Cloud Gateway** 实现 **客户端负载均衡** 的核心全局过滤器。该过滤器拦截 **URI** scheme 为 `lb` 的请求，通过 **Spring Cloud LoadBalancer** 从 **注册中心** 获取服务实例，并按 **负载均衡策略** 选择目标实例进行转发。

**路由 URI 配置方式对比**：

| 配置方式 | URI 格式示例 | 适用场景 | 负载均衡支持 |
|---------|-------------|---------|------------|
| **硬编码方式** | `http://localhost:8061` | 单实例部署或调试环境 | ❌ 不支持，直接转发到固定地址 |
| **服务发现方式** | `lb://tlmall-order01` | 微服务集群生产环境 | ✅ 支持，自动选择健康实例 |

> **URI 格式说明**：`lb://` 前缀标识 **负载均衡**，`tlmall-order01` 为服务在 **注册中心** 的 **Service ID**。

**负载均衡的主要价值**：

| 主要价值 | 实现机制 | 业务效益 |
|---------|---------|---------|
| **高可用性** | 实例故障时自动切换至健康实例 | 保障服务连续性，提升系统稳定性 |
| **水平扩展** | 动态增加服务实例数量 | 线性提升处理能力，应对流量增长 |
| **流量分发** | 按 **负载均衡策略** 分配请求 | 优化资源利用率，避免单点过载 |
#### (2) 工作原理

[**ReactiveLoadBalancerClientFilter**](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/ReactiveLoadBalancerClientFilter.java) 的工作流程：

| 处理步骤 | 工作机制 | 技术实现 |
|---------|---------|---------|
| **① URI 识别** | 检测 **URI** scheme 是否为 `lb` | 解析 `uri.getScheme()`，非 `lb` 则跳过 |
| **② 服务发现** | 从 **注册中心**（如 **Nacos**）获取服务实例列表 | 通过 `ReactorServiceInstanceLoadBalancer` 响应式获取实例 |
| **③ 实例选择** | 按 **负载均衡策略**（轮询/随机等）选择实例 | 调用 `choose(Request)` 方法，结合健康检查 |
| **④ URI 替换** | 将逻辑 **URI** 转换为物理 **URI** | 用 `http://ip:port` 替换 `lb://服务名` 并更新 **ServerWebExchange** |
| **⑤ 请求传递** | 将请求传递至 **过滤器链** 的下一环节 | 执行 `chain.filter(exchange)` 继续处理 |

**设计特点**：

| **特点**      | 技术实现                                             | 效果                    |
| ----------- | ------------------------------------------------ | --------------------- |
| **响应式处理**   | 基于 **Project Reactor** 的 **异步非阻塞模型**，通过 `Mono<Response<ServiceInstance>>` 响应式流处理 | **非阻塞 I/O** 提升并发性能，降低资源消耗 |
| **过滤器顺序控制** | 实现 `Ordered` 接口，确保在路由匹配后、实际请求转发前执行               | 保证 **负载均衡** 在正确时机执行，避免路由冲突  |
| **无损传递**    | 仅在 `ServerWebExchange` 中更新目标地址，不修改原始请求           | 保证 **过滤器链** 完整性，支持后续过滤器正常处理 |

## 4. 常用配置和扩展

### 4.1 断言工厂

#### (1) 介绍

**断言配置**的实质是 **断言工厂** 机制。Spring Cloud Gateway 内置了多种断言工厂以满足不同路由场景。

**Predicates（断言）**：路由匹配规则，判断请求是否满足转发条件，满足则转发至目标服务。

**工作机制**：

> `application.yml` 中的断言配置为字符串形式，由 `Predicate Factory` **读取并解析**为路由判断逻辑。

网关启动时 **自动加载** 所有断言工厂，启动日志中可见完整列表：

<img src="imgs/spring-cloud-alibaba-09-gateway/96ac3489096a579b2f99421929cf6c88_MD5.jpg" style="display: block; width: 100%;" alt="网关启动日志中的断言工厂列表">

**断言工厂主要能力**：

| 能力维度 | 说明 |
| --- | --- |
| **请求匹配** | 判断请求是否满足路由规则 |
| **条件转发** | 满足条件的请求转发至目标服务 |
| **拒绝访问** | 不满足条件的请求返回 404 |

**官方文档**：[Spring Cloud Gateway - Route Predicate Factories](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-request-predicates-factories)

#### (2) 内置工厂分类

Spring Cloud Gateway 官方提供了 **12 种路由断言工厂**，覆盖了常见的路由匹配场景（[官网文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gateway-request-predicates-factories)）。

| 类型分类 | 包含的断言工厂 | 主要用途 |
| --- | --- | --- |
| **时间相关** | After、Before、Between | 基于时间段的访问控制 |
| **请求内容** | Method、Path、Header、Cookie、Query | 根据请求特征路由 |
| **来源信息** | RemoteAddr、Host、XForwardedRemoteAddr | 基于客户端信息路由 |
| **权重控制** | Weight | 灰度发布和 A/B 测试 |

**各断言工厂详细说明**：

| 断言工厂                   | 匹配维度     | 示例配置                                                                                          | 应用场景        |
| ---------------------- | -------- | --------------------------------------------------------------------------------------------- | ----------- |
| `Path`                 | 请求路径     | `- Path=/orders/**`                                                                           | 路径路由        |
| `Method`               | HTTP 方法  | `- Method=GET,POST`                                                                           | HTTP 方法限制   |
| `Header`               | 请求头      | `- Header=X-Request-Id, \d+`                                                                  | 请求头验证       |
| `Cookie`               | Cookie 值 | `- Cookie=token,.*`                                                                           | Cookie 校验   |
| `Query`                | 请求参数     | `- Query=userId`                                                                              | 参数匹配        |
| `RemoteAddr`           | 远程地址     | `- RemoteAddr=192.168.1.0/24`                                                                 | IP 白名单/黑名单  |
| `Host`                 | 请求主机     | `- Host=**.somehost.org`                                                                      | 域名路由        |
| `XForwardedRemoteAddr` | 真实客户端 IP | `- XForwardedRemoteAddr=192.168.1.0/24`                                                       | 代理后的 IP 路由  |
| `After`                | 时间之后     | `- After=2023-01-01T00:00:00+08:00[Asia/Shanghai]`                                            | 定时发布        |
| `Before`               | 时间之前     | `- Before=2023-12-31T23:59:59+08:00[Asia/Shanghai]`                                           | 定时下线        |
| `Between`              | 时间区间     | `- Between=2023-01-01T00:00:00+08:00[Asia/Shanghai],2023-12-31T23:59:59+08:00[Asia/Shanghai]` | 限时访问        |
| `Weight`               | 权重分组     | `- Weight=group1, 80`                                                                         | 灰度发布/A/B 测试 |

#### (3) 配置实例

##### ① Path 断言工厂

**主要功能**：根据**请求路径**进行路由匹配

**配置方式**：参考[官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-path-route-predicate-factory)进行如下配置，完整代码见[application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/resources/application.yml "application.yml")

```yml
server:
  port: 18888
spring:
  application:
    name: tlmall-gateway
  cloud:
    gateway:
      # 设置路由：路由id、路由到微服务的uri、断言
      routes:
        # 订单服务路由
        - id: order_route
          uri: lb://tlmall-order
          predicates:
            # 断言，路径相匹配的进行路由
            - Path=/orders/**
```

**匹配逻辑**：

| 请求路径 | 匹配结果 | 处理方式 |
| --- | --- | --- |
| `http://localhost:18888/orders?userId=fox` | **匹配成功** | 转发到订单服务 |
| `http://localhost:18888/other` | **匹配失败** | 返回 404 |

**测试效果**：

✓ 成功场景：`/orders` 请求正常转发

<img src="imgs/spring-cloud-alibaba-09-gateway/22a4026a868a32f8434663e08728d290_MD5.jpg" style="display: block; width: 100%;" alt="Path 断言成功场景 - /orders 请求正常转发">

✗ 失败场景：`/other` 路径返回 404

<img src="imgs/spring-cloud-alibaba-09-gateway/de19cdbe9047685d59a3340a7204a254_MD5.jpg" style="display: block; width: 100%;" alt="Path 断言失败场景 - /other 路径返回 404">

##### ② Header 断言工厂

**主要功能**：根据**请求头信息**进行路由匹配

**配置方式**：参考[官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-header-route-predicate-factory)进行如下配置，将 `order_route` 的 `predicates` 替换为：

```yml
server:
  port: 18888
spring:
  application:
    name: tlmall-gateway
  cloud:
    gateway:
      # 设置路由：路由id、路由到微服务的uri、断言
      routes:
        # 订单服务路由
        - id: order_route         # 路由ID，全局唯一
          uri: lb://tlmall-order  # lb表示负载均衡器loadbalancer, tlmall-order是下游微服务名
          predicates:
            # Header匹配
            # 请求中带有请求头名为x-request-id，值与\d+正则表达式匹配
            - Header=X-Request-Id, \d+
```

**匹配规则**：请求必须携带名为 `X-Request-Id` 的请求头，且值符合 `\d+` 正则表达式

**测试效果**：

| 请求头配置 | 匹配结果 | 处理方式 |
| --- | --- | --- |
| 未携带请求头 | **匹配失败** | 返回 404 |
| `X-Request-Id: 123` | **匹配成功** | 正常转发 |

**测试场景验证**：

✗ 缺少请求头：请求被拦截

<img src="imgs/spring-cloud-alibaba-09-gateway/4413f994db750e255490258967b4e384_MD5.jpg" style="display: block; width: 100%;" alt="Header 断言失败场景 - 缺少请求头请求被拦截">

✓ 携带正确请求头：`X-Request-Id: 123`，成功转发

<img src="imgs/spring-cloud-alibaba-09-gateway/ba22b818e6476318ff8accf9304fe737_MD5.jpg" style="display: block; width: 100%;" alt="Header 断言成功场景 - 携带正确请求头成功转发">

##### ③ Method 断言工厂

**主要功能**：根据 **HTTP 请求方法**进行路由匹配

**配置方式**：

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: order_route
          uri: lb://tlmall-order
          predicates:
            # 仅GET和POST请求能够匹配该路由
            - Method=GET,POST
```


#### (4) 自定义断言工厂

##### ① 设计思路

当官方提供的断言工厂无法满足业务需求时，可以基于 **Spring Cloud Gateway** 的扩展机制实现自定义断言工厂。

典型应用场景：

| 场景类型       | 具体说明            |
| ---------- | --------------- |
| **复杂业务逻辑** | 根据用户 VIP 等级进行路由 |
| **动态条件判断** | 根据系统负载或业务状态动态路由 |
| **特殊格式验证** | 自定义的请求格式验证逻辑    |
##### ② 实现步骤

**步骤 1：继承抽象类**

继承 `AbstractRoutePredicateFactory` 抽象类：

```java
@Component
public class CustomRoutePredicateFactory
    extends AbstractRoutePredicateFactory<CustomRoutePredicateFactory.Config> {
    // 实现逻辑
}
```

**步骤 2：实现关键方法**

在 `apply` 方法中实现断言逻辑：

```java
@Override
public Predicate<ServerWebExchange> apply(Config config) {
    return exchange -> {
        // 实现自定义的匹配逻辑
        // 返回true表示匹配成功，false表示匹配失败
    };
}
```

**步骤 3：配置 Spring 管理**

使用 `@Component` 注解将自定义断言工厂交由 Spring 管理，网关启动时自动加载。

##### ③ 源码参考

**Path 断言工厂**：[PathRoutePredicateFactory.java](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/handler/predicate/PathRoutePredicateFactory.java)

**处理流程**：

| 处理阶段       | 实现逻辑                          |
| ---------- | ----------------------------- |
| **获取请求路径** | 从 `ServerWebExchange` 中提取请求路径 |
| **模式匹配**   | 将请求路径与配置的模式进行匹配               |
| **返回匹配结果** | 匹配成功返回 true，失败返回 false        |

> 官方源码的这种设计模式可作为参考，实现自定义断言工厂的实现逻辑。


### 4.2 过滤器工厂

#### (1) 关键概念

**GatewayFilter** 是网关提供的**过滤器机制**，用于对进入网关的**请求**和微服务返回的**响应**进行动态处理。

官网文档：[Gateway Filter Factories](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gatewayfilter-factories)

**主要能力**：

| 处理阶段 | 能力说明 | 典型应用 |
| --- | --- | --- |
| **请求预处理** | 在转发到后端服务前对请求进行处理 | 添加请求头、修改请求参数、重写路径 |
| **响应后处理** | 在收到后端服务响应后对响应进行处理 | 修改响应头、转换响应体、设置状态码 |
| **业务解耦** | 将通用功能从业务代码中分离到网关层 | 鉴权、限流、熔断、日志记录 |

**过滤器工厂 vs 断言工厂**：

| 对比维度 | 过滤器工厂 | 断言工厂 |
| --- | --- | --- |
| **执行时机** | 请求处理阶段 | 路由匹配阶段 |
| **作用** | 对请求和响应进行处理 | 判断请求是否匹配路由 |
| **处理结果** | 修改请求或响应内容 | 返回 true/false |
| **典型场景** | 添加头、修改参数、限流熔断 | 路径匹配、条件路由 |

#### (2) 内置工厂分类

Spring Cloud Gateway 提供 **38 种内置过滤器工厂**，覆盖**请求头**、**请求路径**、**请求参数**、**响应头**、**熔断**、**限流**等场景。

官网文档：[Gateway Filter Factories](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#gatewayfilter-factories)

**内置工厂清单**：

| 类别 | 工厂名称 | 主要用途 | 文档 | 源码 |
| --- | --- | --- | --- | --- |
| **请求头** | AddRequestHeaderGatewayFilterFactory | 给请求增加请求头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-addrequestheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/AddRequestHeaderGatewayFilterFactory.java) |
| **请求头** | SetRequestHeaderGatewayFilterFactory | 设定（覆盖）请求头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-setrequestheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/SetRequestHeaderGatewayFilterFactory.java) |
| **请求头** | RemoveRequestHeaderGatewayFilterFactory | 从请求中移除指定请求头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-removerequestheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RemoveRequestHeaderGatewayFilterFactory.java) |
| **请求头** | AddRequestHeadersIfNotPresent<br>GatewayFilterFactory | 仅当请求头不存在时才添加 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-addrequestheadersifnotpresent-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/AddRequestHeadersIfNotPresentGatewayFilterFactory.java) |
| **请求头** | MapRequestHeaderGatewayFilterFactory | 将已有请求头的值映射到新请求头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-maprequestheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/MapRequestHeaderGatewayFilterFactory.java) |
| **请求头** | PreserveHostHeaderGatewayFilterFactory | 保留请求的原始 Host 头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-preservehostheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/PreserveHostHeaderGatewayFilterFactory.java) |
| **请求头** | SetRequestHostHeaderGatewayFilterFactory | 设定请求的 Host 头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-setrequesthostheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/SetRequestHostHeaderGatewayFilterFactory.java) |
| **请求头** | RequestHeaderSizeGatewayFilterFactory | 限制请求头大小，超限则返回 431 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-requestheadersize-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RequestHeaderSizeGatewayFilterFactory.java) |
| **响应头** | AddResponseHeaderGatewayFilterFactory | 给响应增加响应头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-addresponseheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/AddResponseHeaderGatewayFilterFactory.java) |
| **响应头** | SecureHeadersGatewayFilterFactory | 添加安全相关的 HTTP 响应头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-secureheaders-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/SecureHeadersGatewayFilterFactory.java) |
| **响应头** | TokenRelayGatewayFilterFactory | 转发 OAuth2 认证令牌到下游服务 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-tokenrelay-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/TokenRelayGatewayFilterFactory.java) |
| **请求路径** | RewritePathGatewayFilterFactory | 使用正则表达式重写请求路径 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-rewritepath-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RewritePathGatewayFilterFactory.java) |
| **请求路径** | StripPrefixGatewayFilterFactory | 去除请求路径中的前缀 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-stripprefix-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/StripPrefixGatewayFilterFactory.java) |
| **请求路径** | PrefixPathGatewayFilterFactory | 给请求路径添加前缀 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-prefixpath-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/PrefixPathGatewayFilterFactory.java) |
| **请求路径** | SetPathGatewayFilterFactory | 通过路径模板设置请求路径 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-setpath-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/SetPathGatewayFilterFactory.java) |
| **熔断** | CircuitBreakerGatewayFilterFactory | 熔断下游服务，失败时走 fallback | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-circuitbreaker-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/CircuitBreakerGatewayFilterFactory.java) |
| **熔断** | FallbackHeadersGatewayFilterFactory | 向 fallback 请求添加执行异常详情的请求头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-fallbackheaders-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/FallbackHeadersGatewayFilterFactory.java) |
| **重试** | RetryGatewayFilterFactory | 下游调用失败时自动重试 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-retry-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RetryGatewayFilterFactory.java) |
| **限流** | RequestRateLimiterGatewayFilterFactory | 对请求做限流保护 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-requestratelimiter-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RequestRateLimiterGatewayFilterFactory.java) |
| **请求参数** | AddRequestParameterGatewayFilterFactory | 给请求增加查询参数 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-addrequestparameter-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/AddRequestParameterGatewayFilterFactory.java) |
| **请求参数** | RemoveRequestParameterGatewayFilterFactory | 从请求中移除指定查询参数 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-removerequestparameter-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RemoveRequestParameterGatewayFilterFactory.java) |
| **请求参数** | RewriteRequestParameterGatewayFilterFactory | 重写请求参数的值 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-rewriterequestparameter-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RewriteRequestParameterGatewayFilterFactory.java) |
| **响应头** | SetResponseHeaderGatewayFilterFactory | 设定（覆盖）响应头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-setresponseheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/SetResponseHeaderGatewayFilterFactory.java) |
| **响应头** | RemoveResponseHeaderGatewayFilterFactory | 从响应中移除指定响应头 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-removeresponseheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RemoveResponseHeaderGatewayFilterFactory.java) |
| **响应头** | RewriteResponseHeaderGatewayFilterFactory | 使用正则表达式重写响应头的值 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-rewriteresponseheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RewriteResponseHeaderGatewayFilterFactory.java) |
| **响应头** | DedupeResponseHeaderGatewayFilterFactory | 去除响应头中的重复值 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-deduperesponseheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/DedupeResponseHeaderGatewayFilterFactory.java) |
| **响应头** | RewriteLocationResponseHeader<br>GatewayFilterFactory | 重写响应 Location 头，通常去掉后端细节 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-rewritelocationresponseheader-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RewriteLocationResponseHeaderGatewayFilterFactory.java) |
| **重定向** | RedirectToGatewayFilterFactory | 重定向到指定 URL | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-redirectto-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RedirectToGatewayFilterFactory.java) |
| **请求体** | ModifyRequestBodyGatewayFilterFactory | 修改发送到下游的请求体 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-modifyrequestbody-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/ModifyRequestBodyGatewayFilterFactory.java) |
| **请求体** | CacheRequestBodyGatewayFilterFactory | 缓存请求体以便重复读取 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-cacherequestbody-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/CacheRequestBodyGatewayFilterFactory.java) |
| **响应体** | ModifyResponseBodyGatewayFilterFactory | 修改返回给客户端的响应体 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-modifyresponsebody-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/ModifyResponseBodyGatewayFilterFactory.java) |
| **响应体** | RemoveJsonAttributesResponseBody<br>GatewayFilterFactory | 从 JSON 响应体中移除指定属性 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-removejsonattributesresponsebody-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RemoveJsonAttributesResponseBodyGatewayFilterFactory.java) |
| **请求** | RequestSizeGatewayFilterFactory | 限制请求体大小，超限则拒绝 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-requestsize-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/RequestSizeGatewayFilterFactory.java) |
| **响应状态** | SetStatusGatewayFilterFactory | 设置返回给客户端的 HTTP 状态码 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-setstatus-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/SetStatusGatewayFilterFactory.java) |
| **会话** | SaveSessionGatewayFilterFactory | 在转发请求前强制保存会话 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-savesession-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/SaveSessionGatewayFilterFactory.java) |
| **响应缓存** | LocalResponseCacheGatewayFilterFactory | 在网关本地缓存 GET 请求的响应 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-localresponsecache-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/LocalResponseCacheGatewayFilterFactory.java) |
| **请求转换** | JsonToGrpcGatewayFilterFactory | 将 JSON 负载转换为 gRPC 请求 | [链接](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-jsontogrpc-gatewayfilter-factory) | [链接](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/JsonToGrpcGatewayFilterFactory.java) |

#### (3) 使用实例

##### ① 添加请求头

**应用场景**：为所有经过网关的请求添加统一的请求头，如**鉴权信息**、**追踪 ID**、**版本标识**等。

| 类别 | 工厂名称 | 主要用途 | 文档 | 源码 |
| --- | --- | --- | --- | --- |
| **请求头** | AddRequestHeaderGatewayFilterFactory | 给请求增加请求头 | [官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-addrequestheader-gatewayfilter-factory) | [GitHub 源码](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/AddRequestHeaderGatewayFilterFactory.java) |

**配置示例**：

```yml
spring:  
  cloud:
    gateway:  
      # 设置路由：路由id、路由到微服务的uri、断言  
      routes:  
        # 订单服务路由：测试使用 http://localhost:{gateway_port}/orders        
        - id: order_route
          uri: lb://tlmall-order
          # 断言工厂  
          predicates:  
            # 断言，路径相匹配的进行路由  
            - Path=/orders/**  
          # 过滤器工厂  
          filters:  
            # 请求转发到tlmall-order前，自动添加X-Request-color: red
            - AddRequestHeader=X-Request-color, red
```

完整代码：[application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/resources/application.yml)

**验证**：

在订单服务的 **Controller** 中添加测试接口：

```java
@RestController
@RequestMapping("/orders")
@Slf4j
@Validated
public class GatewayDemoController {
	// …… 其它代码 ……

	// 返回指定请求头，用于验证Gateway上游AddRequestHeader、自定义过滤器的功能
	@GetMapping("/request-headers/{header_name}")
	public Result getRequestHeader(
	        HttpServletRequest request,
	        @PathVariable("header_name") @NotNull @NotBlank String headerName) throws Exception {
	    String headerValue = request.getHeader(headerName);
	    log.info("Gateway请求头{}值为{}", headerName, headerValue);
	    return Result.success("返回Gateway请求头", headerName + ": " + headerValue);
	}
}
```

完整代码：[GatewayDemoController.java](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-order/src/main/java/org/nacosdemo/tlmallorder/controller/GatewayDemoController.java)

通过网关访问该接口，能够正确获取到网关添加的请求头。

<img src="imgs/spring-cloud-alibaba-09-gateway/0cdcc9714dd92dcd60d61e3795c59f8a_MD5.jpg" style="display: block; width: 100%;" alt="">

<img src="imgs/spring-cloud-alibaba-09-gateway/cdee3f753d58c9741b02b0db40580ba3_MD5.jpg" style="display: block; width: 100%;" alt="">

##### ② 添加请求参数

**应用场景**：为请求添加统一的查询参数，如 **API 版本**、**鉴权 Token**、**客户端标识**等。

| 类别 | 工厂名称 | 主要用途 | 文档 | 源码 |
| --- | --- | --- | --- | --- |
| **请求参数** | AddRequestParameterGatewayFilterFactory | 给请求增加查询参数 | [官方文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-addrequestparameter-gatewayfilter-factory) | [GitHub 源码](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/AddRequestParameterGatewayFilterFactory.java) |

**配置示例**：

```yml
spring:  
  cloud:
    gateway:  
      # 设置路由：路由id、路由到微服务的uri、断言  
      routes:  
        # 订单服务路由：测试使用 http://localhost:{gateway_port}/orders        
        - id: order_route
          uri: lb://tlmall-order
          # 断言工厂  
          predicates:  
            # 断言，路径相匹配的进行路由  
            - Path=/orders/**  
          # 过滤器工厂  
          filters:  
            # 请求转发到tlmall-order前，自动添加请求参数 color: blue
            - AddRequestParameter=color, blue
```

完整代码：[application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/resources/application.yml)

**验证**：

在订单服务的 **Controller** 中添加测试接口：

```java
@RestController
@RequestMapping("/orders")
@Slf4j
@Validated
public class GatewayDemoController {

	// …… 其它代码 ……

	// 返回指定请求参数，用于验证Gateway上游AddRequestParameter的功能
	@GetMapping("/request-params/{param_name}")
	public Result getRequestParam(
	        HttpServletRequest request,
	        @PathVariable("param_name") @NotNull @NotBlank String paramName) throws Exception {
	    String paramValue = request.getParameter(paramName);
	    log.info("Gateway请求参数{}值为{}", paramName, paramValue);
	    return Result.success("返回Gateway请求参数", paramName + ": " + paramValue);
	}
}
```

完整代码：[GatewayDemoController.java](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-order/src/main/java/org/nacosdemo/tlmallorder/controller/GatewayDemoController.java)

通过网关访问该接口，能够正确获取到网关添加的请求参数。

<img src="imgs/spring-cloud-alibaba-09-gateway/b8c920e96743c9ced50d0462feaf0659_MD5.jpg" style="display: block; width: 100%;" alt="">

<img src="imgs/spring-cloud-alibaba-09-gateway/a4a9b7e0e25fcc280a638dbcc48bbfcc_MD5.jpg" style="display: block; width: 100%;" alt="">

#### (4) 自定义过滤器工厂

##### ① 应用场景

当内置过滤器工厂无法满足需求时，可以实现自定义过滤器工厂，例如：

- **统一鉴权**：在网关层为所有请求添加鉴权token
- **请求日志**：记录所有经过网关的请求信息
- **数据转换**：对请求或响应数据进行格式转换

##### ② 实现步骤

**实现方式**：继承 `AbstractNameValueGatewayFilterFactory` 抽象类，类名必须以 `GatewayFilterFactory` 结尾，并通过 `@Component` 注解交给 Spring 管理。

> 可参考官方实现：[AddRequestHeaderGatewayFilterFactory.java](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/factory/AddRequestHeaderGatewayFilterFactory.java)

**示例目标**：通过自定义过滤器在请求头中添加 `BusinessDomain` 字段。

###### 步骤1：实现过滤器工厂

继承 `AbstractNameValueGatewayFilterFactory` 抽象类，实现 `apply` 方法，用 `@Component` 注解将其注册为 Spring Bean。


```java
@Component  
public class AddBizDomainGatewayFilterFactory extends AbstractNameValueGatewayFilterFactory {  
    private static final Logger log = LoggerFactory.getLogger(AddBizDomainGatewayFilterFactory.class);  
  
    @Override  
    public GatewayFilter apply(AbstractNameValueGatewayFilterFactory.NameValueConfig config) {  
        return (exchange, chain) -> {  
            log.info("AddBizDomainGatewayFilterFactory:" + config.getName() + ":" + config.getValue());  
            // 获取配置的Business Domain  
            String bizDomain = config.getValue();  
            // 把Business Domain添加到请求头中  
            ServerHttpRequest request =  exchange.getRequest();  
            if (StringUtils.isNoneBlank(bizDomain)) {  
                request = request  
                        .mutate()  
                        .header("BusinessDomain", bizDomain)  
                        .build();  
            }  
            // 继续执行chain上的其它过滤器  
            return chain.filter(exchange.mutate().request(request).build());  
        };  
    }  
}
```

###### 步骤2：配置过滤器工厂

在 `application.yml` 中配置使用自定义过滤器工厂。

**命名规则**：类名格式为 `XXXGatewayFilterFactory`，配置时去掉 `GatewayFilterFactory` 后缀即可使用。

例如：`AddBizDomainGatewayFilterFactory` → 配置中使用 `AddBizDomain`


```yml
spring:  
  cloud:  
    gateway:  
      routes:  
        - id: order_route           
          uri: lb://tlmall-order
          predicates:  
            - Path=/orders/**  
          filters:
            # 自定义过滤器工厂  
            - AddBizDomain=domain,order
```

完整代码：[application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/resources/application.yml "application.yml")

###### 步骤3：验证效果

<img src="imgs/spring-cloud-alibaba-09-gateway/f4b012f159bec327b7e26d93a8ce78bd_MD5.jpg" style="display: block; width: 100%;" alt="自定义过滤器验证 - 请求头添加 BusinessDomain 字段">

<img src="imgs/spring-cloud-alibaba-09-gateway/f9a97702bd32201467b6e280e83acb66_MD5.jpg" style="display: block; width: 100%;" alt="自定义过滤器验证 - 网关日志输出">

##### ③ 常见问题解决

###### 问题1：过滤器工厂无法识别

| 维度 | 说明 |
| --- | --- |
| **原因** | 未添加 `@Component` 注解，Spring 无法加载该过滤器工厂 |
| **机制** | Gateway 通过 Spring 容器扫描发现所有过滤器工厂，未注册为 Bean 则无法被发现 |
| **错误提示** | 启动时报错 `Unable to find GatewayFilterFactory with name XXX` |
| **解决方案** | 在自定义过滤器工厂类上添加 `@Component` 注解 |

###### 问题2：配置无法生效

| 维度 | 说明 |
| --- | --- |
| **原因** | 配置的名称与类名不匹配 |
| **命名规则** | 配置名称 = 类名去掉 `GatewayFilterFactory` 后缀 |

| 类名 | 配置名称 |
|------|----------|
| `AddBizDomainGatewayFilterFactory` | `AddBizDomain` |
| `CheckAuthGatewayFilterFactory` | `CheckAuth` |

| 正误对比 | 示例配置 | 说明 |
| --- | --- | --- |
| ✅ **正确** | `AddBizDomain=domain,order` | 类名前缀匹配 |
| ❌ **错误** | `AddBizDomainFilter=domain,order` | 多了后缀 `Filter` |
| ✅ **正确** | `addbizdomain=domain,order` | 不区分大小写 |

> **解决方案**：确保配置名称与类名前缀一致（不区分大小写）

### 4.3 全局过滤器

#### (1) 基本概念

##### ① 作用范围

**Global Filter** 会自动应用于所有路由的请求，**无需显式配置**。

```
请求进入网关
    ↓
[全局过滤器链] ← Global Filter（匹配到路由的请求会经过）
    ↓
[路由匹配] ← 根据断言条件匹配路由
    ↓
[路由过滤器链] ← Gateway Filter（仅该路由的请求经过）
    ↓
后端服务
```

官方文档：[https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#global-filters](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#global-filters)


对比Gateway Filter，**Global Filter**有如下差异

| 对比维度 | **Global Filter** | **Gateway Filter** |
| --- | --- | --- |
| **作用范围** | **所有路由** | **配置的路由** |
| **配置方式** | **自动生效**，无需配置 | 需要在配置文件中**显式配置** |
| **使用场景** | **全局统一的处理逻辑**（认证、日志、监控） | **特定路由的个性化处理** |
| **实现接口** | `GlobalFilter` | `GatewayFilter` |
| **适配机制** | 通过 `GatewayFilterAdapter` 适配 | 直接使用 |

##### ② 实现机制

`GatewayFilterAdapter` 适配器负责将 `GlobalFilter` 统一到过滤器链处理机制中，其**作用**有三点：

| 作用 | 具体说明 |
| --- | --- |
| **接口适配** | 将 `GlobalFilter` 包装为 `GatewayFilter`，使两者能在**同一链中执行** |
| **自动发现** | 开发者只需实现 `GlobalFilter` 接口并标注 `@Component`，Spring **自动扫描并注册**为 Bean |
| **统一调度** | 请求到达时，网关将路由专属的 Gateway Filter 与全局的 Global Filter **合并成完整过滤器链**，按 `order` 顺序依次执行 |

**主要差异**：Global Filter 基于 Spring Bean **自动发现**，零配置即可生效；Gateway Filter 必须在 `application.yml` 中**显式配置**路由才能加载。

**执行流程：**

```
请求到达 → 路由匹配 → 构建过滤器链 → 执行过滤器 → 转发后端服务
                      ↑
           Gateway Filter（路由专属）
           + Global Filter（全局，通过适配器包装）
```

**设计价值：** 这种设计使得全局逻辑（如**认证**、**日志**、**监控**）只需**定义一次**，就能对所有路由生效，而**无需在每个路由中重复配置**。

#### (2) 内置全局过滤器

##### ① 过滤器总览

**Spring Cloud Gateway** 内置了多个全局过滤器，按功能分类如下：

| **分类**          | **过滤器名称**                                  | **主要用途**                                                            | **官方文档**                                                                                                            |
| --------------- | ------------------------------------------ | ------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- |
| **监控指标**        | `GatewayMetricsFilter`                     | **统计性能数据**（响应时间、状态码、路由信息），用于**监控健康状态**和**性能分析**                     | [文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-gateway-metrics-filter)           |
| **缓存**          | `LocalResponseCache`                       | 缓存 **GET 请求响应**，相同请求直接返回缓存，减少后端压力，提升响应速度                            | [文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-local-response-cache-filter)      |
| **负载均衡**        | `ReactiveLoadBalancerClientFilter`         | 将服务名（如 `lb://user-service`）**解析为真实地址**（IP+端口），从多个实例中选择一个调用          | [文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-reactiveloadbalancerclientfilter) |
| **路由URL**       | `RouteToRequestUrl`                        | 根据路由配置生成目标 URL，转换为**实际微服务地址**                                       | [文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-routetorequesturl-filter)         |
| **HTTP客户端**     | `NettyRoutingFilter`                       | 使用 **Netty 客户端**向下游微服务发送 **HTTP/HTTPS 请求**，**暂存响应**供后续处理            | [文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-netty-routing-filter)             |
| **HTTP客户端**     | `WebClientHttpRoutingFilter` <br>*(实验性)*   | 使用 **WebClient** 发送请求（**不依赖 Netty**），是 NettyRoutingFilter 的**替代方案** | 见 NettyRoutingFilter                                                                                                |
| **响应处理**        | `NettyWriteResponseFilter`                 | 将下游服务的**响应数据返回客户端**（在**所有过滤器处理完成后执行**）                              | [文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-netty-write-response-filter)      |
| **响应处理**        | `WebClientWriteResponseFilter` <br>*(实验性)* | 将 WebClient 获取的响应返回客户端，是 NettyWriteResponseFilter 的**替代方案**         | 见 NettyWriteResponseFilter                                                                                          |
| **路径转发**        | `ForwardRoutingFilter`                     | 将请求**转发到网关内部本地处理器**（**不经过网络**），适用于调用本地 Controller 或 Service         | [文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#forward-routing-filter)               |
| **路径转发**        | `ForwardPathFilter`                        | 配合 ForwardRoutingFilter 使用，**修正本地转发的请求路径**                          | [文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#forward-routing-filter)               |
| **WebSocket相关** | `WebsocketRoutingFilter`                   | 处理 **WebSocket 长连接**（`ws://` 和 `wss://` 协议），支持**通过服务名负载均衡**         | [文档](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-websocket-routing-filter)         |

> **官方文档总览**：[https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#global-filters](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#global-filters)

##### ② ReactiveLoadBalancerClientFilter

它将**逻辑服务名**解析为**实际服务器地址**。

| **URI 类型**  | **格式示例**                | **支持实例数** | **说明**        |
| ----------- | ----------------------- | --------- | ------------- |
| **逻辑服务名**   | `lb://tlmall-order`     | **多实例**   | 通过服务名**动态发现** |
| **实际服务器地址** | `http://127.0.0.1:8061` | **单实例**   | 直接指定服务地址      |

它集成 **Spring Cloud LoadBalancer**，从注册中心（如 **Nacos**）获取可用实例列表并选择调用。


```
原始请求：http://gateway:18888/orders?userId=fox
    ↓
路由 URI：lb://tlmall-order
    ↓
调用 LoadBalancer 进行查询：
    - tlmall-order: 192.168.1.10:8061 (健康)
    - tlmall-order: 192.168.1.11:8062 (健康)
    ↓
选择实例：192.168.1.10:8061
    ↓
替换后 URI：http://192.168.1.10:8061/orders?userId=fox
    ↓
发起真实调用
```

源码链接：[ReactiveLoadBalancerClientFilter.java](https://github.com/spring-cloud/spring-cloud-gateway/blob/4.1.x/spring-cloud-gateway-server/src/main/java/org/springframework/cloud/gateway/filter/ReactiveLoadBalancerClientFilter.java)

#### (3) 自定义全局过滤器

以 **Token 校验**为例，演示自定义全局过滤器的实现。

##### 应用场景

在网关层实现统一的 **Token 校验**，可避免在每个微服务中重复实现鉴权逻辑。

**主要价值**：将鉴权逻辑抽象为独立层，在网关统一处理。

| 功能 | 说明 |
| --- | --- |
| **JWT 本地校验** | 使用公钥验证 JWT 签名 |
| **远程鉴权中心** | 调用统一的授权中心验证 Token |
| **黑名单机制** | 检查 Token 是否在黑名单中 |
| **白名单机制** | 对特定接口或路径放行，无需鉴权 |
| **适配多鉴权方式** | 灵活配置多种鉴权策略 |
| **记录鉴权失败日志** | 便于安全审计和问题排查 |

##### 实现步骤

###### 步骤 1：编写全局过滤器

通过实现 `GlobalFilter` 接口编写全局过滤器：

```java
@Component // 取消注释，可以让这个全局过滤器生效
public class CheckAuthFilter implements GlobalFilter, Ordered {  
    private static final Logger log = LoggerFactory.getLogger(CheckAuthFilter.class);  
  
	@Override  
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {  
	    // 获取Access Token  
	    String token = exchange.getRequest().getHeaders().getFirst("token");  
	    if (null == token) {  
	        log.info("token is null");  
	        return getResponseMono(exchange, HttpStatus.UNAUTHORIZED, "Token is missing");  
	    }  
	    // 检查Access Token  
	    log.info("校验token");  
	    if (!isValid(token)) {  
	        log.info("token is invalid");  
	        return getResponseMono(exchange, HttpStatus.FORBIDDEN, "Token is invalid");  
	    }  
	    // 调用Filter Chain上面的其它过滤器  
	    return chain.filter(exchange);  
	}
  
    @Override  
    public int getOrder() {  
        return 10;  
    }  
  
    boolean isValid(String token) {  
        // 模拟Token验证逻辑
        return true;  
    }  

	// …… 其它代码 ……
}
```

完整代码：[CheckAuthFilter.java](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/java/org/nacosdemo/tlmallgateway/filter/CheckAuthFilter.java)

###### 步骤 2：指定执行顺序

通过实现 `Ordered` 接口控制过滤器执行顺序，数值越小优先级越高。

示例代码：

```java
@Override
public int getOrder() {
    return 10;
}
```

###### 步骤 3：测试验证

**测试场景 1**：不带 Token 请求 → 返回 401 Unauthorized

<img src="imgs/spring-cloud-alibaba-09-gateway/9c872de58417404a7ec87afd41eedfb1_MD5.jpg" style="display: block; width: 100%;" alt="Token 校验测试 - 不带 Token 请求返回 401">

**测试场景 2**：带有效 Token 请求 → 正常转发到后端服务

<img src="imgs/spring-cloud-alibaba-09-gateway/493dd9170e78beb3e9cdac4a881501e9_MD5.jpg" style="display: block; width: 100%;" alt="Token 校验测试 - 带有效 Token 请求正常转发">

### 4.4 跨域配置

#### (1) 同源策略与跨域问题

**微服务架构**中，前端与后端通过 **Gateway** 网关交互时，常面临 **跨域访问**限制。

##### ① 同源策略（Same-Origin Policy）

**同源定义**：如果两个页面的 **协议**、**主机名**、**端口** 完全相同，则它们同源。

| URL 对比                                                           | 判定结果    | 原因         |
| ---------------------------------------------------------------- | ------- | ---------- |
| `http://example.com:8080/app1` vs `http://example.com:8080/app2` | **同源**  | 协议、主机、端口相同 |
| `http://example.com:8080` vs `https://example.com:8080`          | **不同源** | 协议不同       |
| `http://example.com:8080` vs `http://example.com:8081`           | **不同源** | 端口不同       |
| `http://example.com` vs `http://other.com`                       | **不同源** | 主机不同       |

##### ② 跨域访问限制

浏览器的 **同源策略** 限制 **Ajax** 请求只能访问同源资源。

**具体执行过程**

> - **请求阶段**：浏览器**会发送**跨域请求到服务器（请求可达）
> - **响应阶段**：服务器**会处理**请求并返回响应
> - **限制生效**：浏览器**阻止 JavaScript 读取响应**（缺少正确的 **CORS** 响应头时）

**限制目的**：防止恶意网站读取另一个网站的敏感数据（如用户信息、会话数据等），保护用户隐私和安全。

**典型表现**：浏览器控制台出现 **CORS** 错误：

```
Access to XMLHttpRequest at 'http://gateway:18888/order/get'
from origin 'http://localhost:3000' has been blocked by CORS policy:
No 'Access-Control-Allow-Origin' header is present on the requested resource.
```

##### ③ CORS 解决方案

**跨源资源共享（CORS）** 是 **W3C 标准**，通过在 **HTTP** 响应头中添加特定字段，允许服务器明确告知浏览器哪些跨域请求是被允许的。

**官方文档**：[MDN - CORS](https://developer.mozilla.org/zh-CN/docs/Web/HTTP/CORS)

**关键响应头**：

| 响应头                                | 作用               | 示例值                           |
| ---------------------------------- | ---------------- | ----------------------------- |
| `Access-Control-Allow-Origin`      | 允许的源             | `*` 或 `http://example.com`    |
| `Access-Control-Allow-Methods`     | 允许的 **HTTP** 方法  | `GET, POST, PUT, DELETE`      |
| `Access-Control-Allow-Headers`     | 允许的请求头           | `Content-Type, Authorization` |
| `Access-Control-Allow-Credentials` | 是否允许携带凭证         | `true`                        |
| `Access-Control-Max-Age`           | **预检请求**的缓存时间（秒） | `3600`                        |
##### ④ 问题复现

**前置准备**：启动 `ui/tlmall-frontend` 模块，该模块会启动一个 **Web 服务**（端口 **8080**）

<img src="imgs/spring-cloud-alibaba-09-gateway/3781eccaf5d89f9dddc4ab42f2d33944_MD5.jpg" style="display: block; width: 100%;" alt="前端模块启动 Web 服务">

**复现步骤**：

> **步骤1**：关闭 **Gateway** 跨域配置（将下图 **两种配置方法** 都注释掉）

<img src="imgs/spring-cloud-alibaba-09-gateway/6d80a725a2e70c1d8b6ef3b73b7700c8_MD5.jpg" style="display: block; width: 100%;" alt="Gateway 跨域配置方法一：配置类">

<img src="imgs/spring-cloud-alibaba-09-gateway/09a684f931ec2a4064bc1c860c483529_MD5.jpg" style="display: block; width: 100%;" alt="Gateway 跨域配置方法二：配置文件">

> **步骤2**：通过前端页面（端口 **8080**）访问后端 **Gateway** 接口（端口**18888**）：
>
> ```
> http://localhost:18888/orders?userId=fox
> ```

**错误表现**：因为端口不同导致不同源，浏览器控制台出现 **CORS** 跨域错误

<img src="imgs/spring-cloud-alibaba-09-gateway/af3a76f11972dfe11440e293113580af_MD5.jpg" style="display: block; width: 100%;" alt="浏览器控制台 CORS 跨域错误提示">

#### (2) Gateway跨域配置

##### ① 配置方案

**推荐策略**：在 **Gateway** 网关层统一配置 **CORS**，而非在每个微服务中单独配置。

**主要优势**：

| 优势维度 | 说明 | 价值 |
| --- | --- | --- |
| **集中管理** | 统一的跨域策略，便于维护 | 降低运维成本 |
| **配置简化** | 各个微服务无需关注跨域问题 | 提升开发效率 |
| **性能优化** | 跨域预检请求在网关层处理 | 减少后端服务压力 |

> **官方文档**：[CORS Configuration](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#cors-configuration)

##### ② 方式1：Java配置类（推荐）

**生产环境注意**：下面代码仅用于 **API 演示**，实际生产环境需要更严格的安全限制。

```java
/**  
 * 两种方法（使用任意一种即可，不要同时启用)  
 * 方法1：通过application.yml配置  
 * 方法2：通过下面的@Configuration类来配置  
 */  
@Configuration // 取消注释，使此配置类生效  
public class CorsConfig {  
    @Bean  
    public CorsWebFilter corsFilter() {  
	    // 注意！！
	    // 下面代码只是简单演示API如何使用，生产环境上需要更严格的安全限制
	    
        // 创建CORS配置对象  
        CorsConfiguration config = new CorsConfiguration();  
        config.addAllowedMethod("*");   // 允许所有HTTP方法  
        config.addAllowedOrigin("*");   // 允许所有来源域（生产环境建议指定具体域名）  
        config.addAllowedHeader("*");   // 允许所有请求头  
        // 创建基于URL的CORS配置源，用于响应式编程  
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());  
        source.registerCorsConfiguration("/**", config);    // 对所有路径（/**）应用上述CORS配置  
        // 返回CORS过滤器Bean，Spring会自动将其注册到过滤器链  
        return new CorsWebFilter(source);  
    }  
}
```

**代码链接**：[CorsConfig.java](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/java/org/nacosdemo/tlmallgateway/config/CorsConfig.java)

##### ③ 方式2：YAML配置文件

```yaml
spring:
  cloud:
    gateway:
		# 跨域配置的两种方法（使用任意一种即可，不要同时启用)  
		# 方法1：通过application.yml配置  
		# 方法2：通过下面的@Configuration类来配置 （见CorsConfig）  
		# 注意！！  
		# 下面代码只是简单演示API如何使用，生产环境上需要更严格的安全限制  
		globalcors:  
		  cors-configurations:  
		    '[/**]':  
			    allowedOrigins: "*"  
			    allowedMethods:  
			      - GET  
			      - POST  
			      - DELETE  
			      - PUT  
			      - PATCH  
			      - OPTION
```

**代码链接**：[application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/resources/application.yml "application.yml")

##### ④ 验证效果

<img src="imgs/spring-cloud-alibaba-09-gateway/b147df2cef3d23356961b05ca54ebf27_MD5.jpg" style="display: block; width: 100%;" alt="跨域配置生效后成功访问接口">

### 4.5 网关限流

#### (1) 基于 Redis + Lua 的令牌桶限流

##### ① 介绍

> **官方文档**：[RequestRateLimiter GatewayFilter Factory](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#the-requestratelimiter-gatewayfilter-factory)

**实现方式**：Spring Cloud Gateway 通过 **RequestRateLimiter** 过滤器工厂，基于 **Redis + Lua** 实现**令牌桶算法**限流。

**限流响应**：请求被限流时返回 **HTTP 429 - Too Many Requests**

##### ② 令牌桶算法

> **令牌桶算法**以固定速率向桶中添加令牌，请求处理时消耗令牌。桶有最大容量，满桶后新生成的令牌会被丢弃。

<img src="imgs/spring-cloud-alibaba-09-gateway/c7e5131c6ec370792755c0d62c9306ee_MD5.jpg" style="display: block; width: 500px;" alt="令牌桶算法示意图">

| 核心要素 | 说明 |
| --- | --- |
| **令牌生成** | 系统以固定速率向桶中添加令牌，控制长期请求速率 |
| **令牌容量** | 桶有最大容量限制，应对突发流量 |
| **请求处理** | 请求到达时从桶中获取令牌，获取成功处理，失败则拒绝 |

**算法特点**：可应对突发流量，通过调整令牌生成速率和桶容量实现不同限流策略。

##### ③ 实现步骤

**步骤1**：添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

**步骤2**：配置 Redis 与流控策略

```
spring:
  application:
    name: tlmall-gateway
  data:
    # 配置redis地址
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 5000
      lettuce:
        pool:
          max-active: 200
          max-wait: 10000
          max-idle: 100
          min-idle: 10
  # 配置nacos注册中心地址
  cloud:
    nacos:
      discovery:
        server-addr: 127.0.0.1:8848
    gateway:
      # 设置路由：路由id、路由到微服务的uri、断言
      routes:
        - id: order_route  
          uri: lb://tlmall-order  
          predicates:
            - Path=/order/**
          # 配置过滤器工厂
          filters:
            - name: RequestRateLimiter  # 限流过滤器
              args:
                redis-rate-limiter.replenishRate: 1 # 令牌桶每秒填充速率
                redis-rate-limiter.burstCapacity: 2 # 令牌桶的总容量
                key-resolver: "#{@keyResolver}"     # 使用SpEL表达式，从Spring容器中获取Bean对象
```

**步骤3**：配置 KeyResolver

```java
@Configuration  
public class RateLimiterConfig {
	// Bean的名称要和key-resolver中的SpEL表达式保持一致
    @Bean  
    KeyResolver keyResolver() {  
        // 参数限流
        return exchange -> Mono.just(
		        // 按照userId请求参数，对请求过于频繁的user限流
		        exchange.getRequest().getQueryParams().getFirst("userId"));  
    }  
}
```


##### ④ 配置与效果

**配置说明**：

| 配置参数 | 配置值 | 说明 |
| --- | --- | --- |
| **replenishRate** | 1 | 每秒生成1个令牌 |
| **burstCapacity** | 2 | 桶的最大容量为2个令牌 |

**限流原理**：

> **核心约束**：**桶中令牌数永远不会超过桶容量**。

| 时间点 | 桶中令牌数 | 请求 | 结果 | 说明 |
| --- | --- | --- | --- | --- |
| t=0 | 2 | 请求1 | ✅ 成功 | 消耗1个，桶剩1个 |
| t=0 | 1 | 请求2 | ✅ 成功 | 消耗1个，桶剩0个 |
| t=0 | 0 | 请求3 | ❌ 失败 | **桶已空，无令牌可用** |
| t=0 | 0 | 请求4 | ❌ 失败 | 桶已空 |
| t=1s | 1 | 请求3 | ✅ 成功 | 等待1秒后，新生成1个令牌 |

**测试结果**：

快速发送多个请求，第 1、2 个请求成功（消耗初始的 2 个令牌），第 3 个请求失败（桶已空）。等待 1 秒后，第 3 个请求可成功（新令牌生成），**后续每秒**仅能通过 1 个请求。

如果请求超过限制，就会触发`429: Too Many Requests`

```
HTTP/1.1 429 Too Many Requests
```

#### (2) 方法2：整合Sentinel流控

##### ① 介绍

**Sentinel** 可与 **Spring Cloud Gateway** 整合，对**网关入口流量**进行统一限流。

Sentinel 支持**两种网关流控维度**：

| 限流维度 | 说明 | 配置方式 |
| --- | --- | --- |
| **Route ID 限流** | 以配置文件中的 `Route ID` 为单位进行流控 | 直接为路由ID配置规则 |
| **API 分组限流** | 先在 Sentinel 控制台定义 API 分组，再为分组配置流控规则 | 需要先创建 API 分组 |

如下图所示：
<img src="imgs/spring-cloud-alibaba-09-gateway/28826e05c082949ecf60dcec2cdbc117_MD5.jpg" style="display: block; width: 100%;" alt="Sentinel网关流控两种维度">

**与 Redis+Lua 方案对比**：

| 对比项 | 说明 |
| --- | --- |
| **优势** | 提供 **Web 控制台可视化配置**、**多种流控策略**（直接拒绝/Warm Up/匀速排队）及**内置实时监控面板**，大幅提升运维效率 |
| **劣势** | 仅支持预置的限流维度，灵活性不及 Redis+Lua 方案（不支持自定义 `keyResolver`） |

**方案选择建议**：

> **适用 Sentinel**：需要 **可视化配置**、**多种流控策略**、**实时监控**的场景
>
> **适用 Redis+Lua**：需要 **高度自定义限流 Key** 的场景（例如按用户 ID、IP 等多维度组合限流）

**官方文档**：[Sentinel 网关流控](https://sentinelguard.io/zh-cn/docs/api-gateway-flow-control.html)

##### ② 整合步骤

**1. 添加依赖**

```
<!-- gateway接入sentinel  -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-alibaba-sentinel-gateway</artifactId>
</dependency>

<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-sentinel</artifactId>
</dependency>
```

完整代码：[pom.xml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/pom.xml)

**2. 配置Sentinel控制台**

```yml
spring:
  cloud:
    sentinel:
      transport:
        # 添加sentinel的控制台地址
        dashboard: tlmall-sentinel-dashboard:8888
```

完整代码：[application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/resources/application.yml)

**3. 启动Sentinel控制台**

```bash
port="8888"

mkdir -p ./log

echo "java -Dserver.port=${port} -Dcsp.sentinel.dashboard.server=tlmall-sentinel-dashboard:8888 -Dproject.name=sentinel-dashboard -jar ./bin/sentinel-dashboard-1.8.6.jar 2>&1 > log/log.txt &"

nohup java -Dserver.port=${port} -Dcsp.sentinel.dashboard.server=tlmall-sentinel-dashboard:8888 -Dproject.name=sentinel-dashboard -jar ./bin/sentinel-dashboard-1.8.6.jar 2>&1 > log/log.txt &
```

完整脚本：[start_all.sh](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/local/start_all.sh)、[sentinel/start.sh](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/local/sentinel/start.sh)

**参考文档**：[环境搭建]({% post_url  2025-12-20-spring-cloud-alibaba-mvp-02-env %})、[Sentinel上手]({% post_url 2025-12-25-spring-cloud-alibaba-mvp-07-sentinel %})、[Sentinel详解]({% post_url 2026-03-07-spring-cloud-alibaba-in-depth-05-sentinel %})

##### ③ 基于 Route ID 的流控规则

**1. 触发服务注册**

启动网关服务并访问任意接口，触发服务注册到 Sentinel。

**2. 查看注册的路由 ID**

<img src="imgs/spring-cloud-alibaba-09-gateway/5adb09ab46b4d918e7acff2ccf010a6f_MD5.jpg" style="display: block; width: 100%;" alt="Sentinel控制台">

`/orders` 路由对应的 `order_route` 已注册到 Sentinel（配置见 [application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-gateway/src/main/resources/application.yml)）

<img src="imgs/spring-cloud-alibaba-09-gateway/3489f7aee18114135b6493a07815eea4_MD5.jpg" style="display: block; width: 100%;" alt="路由ID注册到Sentinel控制台">

**3. 配置流控规则**

点击 `[+流控]` 添加流控规则：

<img src="imgs/spring-cloud-alibaba-09-gateway/7df447337526803dd15bf9b6f45f2433_MD5.jpg" style="display: block; width: 100%;" alt="添加流控规则">

**4. 验证流控效果**

频繁发送请求，触发 `429: Too Many Requests`：

<img src="imgs/spring-cloud-alibaba-09-gateway/c8316734bacfb10a366f889fc020895d_MD5.jpg" style="display: block; width: 100%;" alt="频繁请求触发429错误">

##### ④ 基于 API 分组的流控规则

**1. 创建 API 分组**

在 Sentinel 控制台 `API 管理` 面板创建 API 分组：

<img src="imgs/spring-cloud-alibaba-09-gateway/eb65e417a79ddaba86599125cb38fefc_MD5.jpg" style="display: block; width: 100%;" alt="API管理面板创建API分组">

**2. 配置流控规则**

创建流控规则时选择该 API 分组：

<img src="imgs/spring-cloud-alibaba-09-gateway/7d9d695ae39601743fd999c6c6e40e05_MD5.jpg" style="display: block; width: 100%;" alt="选择API分组创建流控规则">

**3. 验证流控效果**

频繁发送请求，触发 `429: Too Many Requests`：

<img src="imgs/spring-cloud-alibaba-09-gateway/7e49b3bf45e8186fe3fd15fbcfea9061_MD5.jpg" style="display: block; width: 100%;" alt="API分组流控触发429错误">

##### ⑤ Spring Boot 3 兼容性问题

###### 问题表现

在 **Spring Boot 3** 环境下，**Gateway** 与 **Sentinel** 整合存在兼容性问题：流控后返回的 HTTP 状态码异常，默认的 `DefaultBlockRequestHandler` 会抛出异常。

<img src="imgs/spring-cloud-alibaba-09-gateway/b1001cb06275f675662ebbf3fdc97535_MD5.jpg" style="display: block; width: 100%;" alt="默认的DefaultBlockRequestHandler异常">

###### 解决方法

**问题原因**：默认的 `DefaultBlockRequestHandler` 在处理响应状态码时调用了不存在的方法。

**解决方法**：自定义 `BlockRequestHandler` 实现异常处理逻辑。

**1. 实现 BlockRequestHandler**

```java
public class MyBlockRequestHandler implements BlockRequestHandler {

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable t) {
        //返回json数据;
        return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .body(fromObject(buildErrorResult(t)));
    }

    private Result buildErrorResult(Throwable ex) {
        if (ex instanceof ParamFlowException) {
            return Result.failed("请求被限流了");
        }
        return Result.failed("系统繁忙");
    }
}
```


**2. 注册自定义处理器**

Spring 的自动装配机制无法识别自定义的 `BlockRequestHandler`，需要实现一个 `ApplicationRunner` Bean，在服务启动时通过 `GatewayCallbackManager` 手动注册：

```java
@Component
public class GatewayBlockHandlerRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        GatewayCallbackManager.setBlockHandler(new MyBlockRequestHandler());
    }
}
```

##### ⑥ Sentinel 网关流控原理

###### 执行流程

<img src="imgs/spring-cloud-alibaba-09-gateway/ff2abcbf1c35b397f9da92de335eea3a_MD5.jpg" style="display: block; width: 100%;" alt="Sentinel网关流控原理">

###### 规则管理

`GatewayRuleManager` 作为 API 网关规则管理器，负责加载和管理两类规则：

| 规则类型 | 规则名称 | 说明 |
|---------|---------|------|
| 网关流控规则 | `GatewayFlowRule` | 定义针对 API 分组、Route ID 等维度的流控策略（如 QPS 限制、熔断阈值） |
| 热点参数规则 | `ParamFlowRule` | 针对请求参数的精细化流控（如限制某个参数的访问频率） |

当 `GatewayFlowRule` 包含热点参数配置时，`GatewayRuleManager` 会将其转换为 `ParamFlowRule`：

```
GatewayFlowRule（网关流控规则）
    ↓
内部转换
    ↓
ParamFlowRule（热点参数规则）
```


###### 规则执行

外部请求到达 **Spring Cloud Gateway** 后，通过 `SentinelGatewayFilter` 全局过滤器（属于 `sentinel-spring-cloud-gateway-adapter` 包）拦截请求并执行流控：

| 步骤 | 说明 |
| ---- | ---- |
| **1. 规则解析** | 解析配置的网关流控规则 |
| **2. 规则转换** | 将网关流控规则转换为热点参数规则 |
| **3. 流量检测** | 对请求进行流量检测 |
| **4. 异常处理** | 触发限流时执行自定义的异常处理逻辑 |

###### 相关类

| 类名 | 作用 |
| --- | --- |
| [GatewayRuleManager](https://github.com/alibaba/Sentinel/blob/1.8/sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/rule/GatewayRuleManager.java) | Gateway 流控规则管理器，负责加载和管理流控规则 |
| [GatewayFlowRule](https://github.com/alibaba/Sentinel/blob/1.8/sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/rule/GatewayFlowRule.java) | Gateway 流控规则定义类 |
| [ParamFlowRule](https://github.com/alibaba/Sentinel/blob/1.8/sentinel-extension/sentinel-parameter-flow-control/src/main/java/com/alibaba/csp/sentinel/slots/block/flow/param/ParamFlowRule.java) | 热点参数流控规则，支持对请求参数进行细粒度限流 |
| [SentinelGatewayFilter](https://github.com/alibaba/Sentinel/blob/1.8/sentinel-adapter/sentinel-spring-cloud-gateway-adapter/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/sc/SentinelGatewayFilter.java) | Sentinel Gateway 过滤器，拦截请求并执行流控检查 |
| [GatewayFlowSlot](https://github.com/alibaba/Sentinel/blob/1.8/sentinel-adapter/sentinel-api-gateway-adapter-common/src/main/java/com/alibaba/csp/sentinel/adapter/gateway/common/slot/GatewayFlowSlot.java) | Gateway 流控槽位，执行流控规则检查 |

## 5. 总结

本文系统介绍 **Spring Cloud Gateway** 的原理与实战，通过**核心概念理解** + **工作原理剖析** + **配置扩展实践**的完整闭环，帮助读者：

| 学习层次        | 核心收获                                                               |
| ----------- | ------------------------------------------------------------------ |
| **建立体系化认知** | 理解 **Gateway** 三大组件（Route、Predicate、Filter）、**路由匹配机制**、**过滤器执行流程** |
| **掌握实战能力**  | 熟练运用 **路由配置**、**断言工厂**、**过滤器**、**限流策略**（Redis+Lua、Sentinel）构建生产级网关 |
| **解决实际问题**  | 具备 **统一异常处理**、**跨域配置**、**自定义扩展**的能力，独立维护生产环境网关 |

