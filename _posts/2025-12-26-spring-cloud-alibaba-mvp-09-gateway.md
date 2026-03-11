---
title: Spring Cloud Alibaba上手 09：Spring Cloud Gateway
author: fangkun119
date: 2025-12-26 12:00:00 +0800
categories: [微服务, Spring Cloud Alibaba]
tags: [微服务, Spring Cloud Alibaba, Spring Cloud Gateway]
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

本文讲解 **Spring Cloud Gateway 微服务网关**，涵盖以下知识模块：

| 知识模块           | 说明                                                     |
| -------------- | ------------------------------------------------------ |
| **网关价值**       | 直接调用痛点 + 网关四大价值（路由、安全、流量、可观测性）                 |
| **Gateway 架构** | 核心能力（路由管理、Filter 机制）、技术实现（WebFlux + Netty 响应式） |
| **网关创建**       | 依赖配置、Nacos 集成、路由规则（路径断言、负载均衡）              |
| **完整代码**       | 项目模块 + Nacos 远程配置                              |

本文从**网关价值**、**Gateway 架构**、**网关创建**三个维度系统阐述 **Spring Cloud Gateway**，帮助读者构建完整的**微服务网关**认知框架。

### 1.2 配套资源

#### (1) 项目源码

| 资源类型           | 说明                               | 链接                                                                                                                   |
| -------------- | -------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| **项目源码**       | Spring Cloud Alibaba 2023 完整示例代码 | [github.com/fangkun119/spring-cloud-alibaba-2023-demo](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo) |
| **Postman 集合** | API 测试用例集合，便于快速验证功能              | [github.com/fangkun119/postman-workspace](https://github.com/fangkun119/postman-workspace)                           |

#### (2) 环境搭建

见文档：[Spring Cloud Alibaba上手 03：中间件环境]({% post_url 2025-12-20-spring-cloud-alibaba-mvp-03-env %})

## 2. 微服务网关的作用

微服务架构需引入 **API 网关**，解决外部客户端直接调用微服务带来的复杂性问题。

### 2.1 直接调用微服务的痛点

**外部客户端**（浏览器、移动 App、第三方系统）直接与多个微服务交互时，面临以下挑战：

| 问题维度 | 具体痛点 | 影响 |
| --- | --- | --- |
| **调用复杂** | 需处理多服务请求，增加代码与配置复杂度 | 维护成本高、扩展困难 |
| **认证分散** | 各服务认证方式不同，客户端需分别适配 | 认证逻辑重复、安全风险高 |
| **环境约束** | 跨域限制、防火墙策略、浏览器不支持 RPC 协议 | 调用链路复杂、兼容性差 |
| **重构困难** | 微服务迭代时客户端需同步修改 | 耦合度高、响应迟缓 |

> **注意**：API 网关主要解决**外部客户端**访问微服务的问题。微服务之间的**内部调用**（如订单服务调用库存服务）通常**不经过网关**，而是直接通过 OpenFeign 等方式调用。

### 2.2 API 网关的核心价值

**API 网关**作为**外部流量**进入微服务架构的**统一入口**，对外提供单一访问接口，对内路由到具体微服务，将横切关注点（认证、鉴权、限流、监控等）从业务代码中剥离，实现业务逻辑与基础设施的分离。

**主要功能**：

| 功能类别 | 具体能力 | 业务价值 |
| --- | --- | --- |
| **路由管理** | 统一 API 路由、请求转发、协议转换 | 简化客户端调用、隐藏后端拓扑 |
| **安全防护** | 身份认证、权限鉴权、防刷、请求/响应增强 | 保障系统安全、统一权限管控 |
| **流量控制** | 限流、熔断、降级、灰度发布 | 保护服务稳定性、平滑上线 |
| **可观测性** | 日志记录、监控统计、请求追踪 | 问题排查、性能分析 |

## 3. Spring Cloud Gateway 介绍

**Spring Cloud Gateway** 是 **Spring Cloud 官方**推出的第二代网关框架，旨在替代 **Netflix Zuul**，为微服务架构提供统一的 **API 路由管理**，基于 **响应式架构** 构建。

**核心能力**

| 能力维度            | 实现方式                   | 典型应用            |
| --------------- | ---------------------- | --------------- |
| **API 路由管理**    | 动态路由配置、请求转发            | 统一服务入口、隐藏后端拓扑   |
| **Filter 扩展机制** | 预置 Filter + 自定义 Filter | 安全认证、监控、限流、熔断降级 |

技术实现

| 技术栈 | 说明 |
| --- | --- |
| **WebFlux + Netty + Reactor** | 构建 **响应式 API 网关** |
| **非 Servlet 架构** | **不支持**传统 Servlet 容器（如 Tomcat），**无法构建为 WAR 包** |

**官方文档**：[https://docs.spring.io/spring-cloud-gateway/reference/](https://docs.spring.io/spring-cloud-gateway/reference/)

## 4. 微服务网关创建步骤

### 4.1 构建网关应用

**模块创建**：在 IDEA 项目中添加名为 `tlmall-gateway` 的模块

<img src="/imgs/spring-cloud-alibaba-mvp-09-gateway/a79a536de4bc4cbd2ef89b26efe919f5_MD5.jpg" style="display: block; width: 100%;" alt="添加tlmall-gateway模块">

**依赖配置**：添加 **Spring Cloud Gateway** Maven 依赖，并引入 Nacos 注册中心、配置中心、负载均衡器依赖

```xml
<!-- gateway网关 -->  
<dependency>  
    <groupId>org.springframework.cloud</groupId>  
    <artifactId>spring-cloud-starter-gateway</artifactId>  
</dependency>  
  
<!--nacos-discovery  注册中心依赖-->  
<dependency>  
    <groupId>com.alibaba.cloud</groupId>  
    <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>  
</dependency>  
  
<!-- loadbalancer 负载均衡器依赖-->  
<dependency>  
    <groupId>org.springframework.cloud</groupId>  
    <artifactId>spring-cloud-loadbalancer</artifactId>  
</dependency>  
  
<!-- nacos-config 配置中心依赖 -->  
<dependency>  
    <groupId>com.alibaba.cloud</groupId>  
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>  
</dependency>
```

**本地配置**：在 `application.yml` 中配置端口等信息，并通过 `config.import` 从 **Nacos** 导入远程配置（包括网关配置 `tlmall-gateway.yml`）

```yml
server:  
  port: 18888  
spring:  
  application:  
    name: tlmall-gateway  
  cloud:  
    nacos:  
      config:  
        server-addr: tlmall-nacos-server:8848  
        file-extension: yml  
  config:  
    import:  
      - optional:nacos:${spring.application.name}.yml  
      - nacos:nacos-discovery.yml
```

### 4.2 配置网关路由规则

**配置上传**：编写 `tlmall-gateway.yml` 并上传至 **Nacos 配置中心**

<img src="/imgs/spring-cloud-alibaba-mvp-09-gateway/a1358cf9be483e82bf8e8aff86d175c0_MD5.jpg" style="display: block; width: 100%;" alt="上传tlmall-gateway.yml到Nacos">

**配置策略**：**Nacos 远程配置**仅包含 **Gateway 路由规则**，其他配置保留在本地

**路由规则说明**：

| 路由ID | 目标服务 | 路径断言 | 说明 |
| --- | --- | --- | --- |
| `order_route` | `tlmall-order` | `/order/**` | 路由到订单服务 |
| `storage_route` | `tlmall-storage` | `/storage/**` | 路由到库存服务 |
| `account_route` | `tlmall-account` | `/account/**` | 路由到账户服务 |

```yml
spring:  
  cloud:  
    gateway:  
      #设置路由：路由id、路由到微服务的uri、断言  
      routes:  
        - id: order_route  #路由ID，全局唯一，建议配置服务名  
          uri: lb://tlmall-order  #lb 整合负载均衡器loadbalancer  
          predicates:  
            - Path=/order/**   # 断言，路径相匹配的进行路由  
  
        - id: storage_route   #路由ID，全局唯一，建议配置服务名  
          uri: lb://tlmall-storage  #lb 整合负载均衡器loadbalancer  
          predicates:  
            - Path=/storage/**   # 断言，路径相匹配的进行路由  
  
        - id: account_route   #路由ID，全局唯一，建议配置服务名  
          uri: lb://tlmall-account  #lb 整合负载均衡器loadbalancer  
          predicates:  
            - Path=/account/**   # 断言，路径相匹配的进行路由
```

### 4.3 完整代码参考

| 内容类型           | 链接地址                                                                                                                                                                                                          |
| -------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **项目模块代码**     | [microservices/tlmall-gateway](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/tree/main/microservices/tlmall-gateway)                                                                           |
| **Nacos 远程配置** | [midwares/dev/remote/nacos/public/DEFAULT_GROUP/tlmall-gateway.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/tlmall-gateway.yml) |

## 5. 总结

本文系统介绍 **Spring Cloud Gateway 微服务网关**，通过**网关价值分析** + **Gateway 架构讲解** + **网关创建实战**的完整闭环，帮助读者：

| 学习层次 | 核心收获 |
| ---- | ---- |
| **建立网关认知** | 理解微服务直接调用的**四大痛点**（调用复杂、认证分散、环境约束、重构困难）、**网关核心价值**（路由管理、安全防护、流量控制、可观测性） |
| **掌握 Gateway 架构** | 理解 **Spring Cloud Gateway** 响应式架构（WebFlux + Netty + Reactor）、**Filter 扩展机制**、与传统 Servlet 网关的本质差异 |
| **具备实战能力** | 熟练构建网关应用（依赖配置、Nacos 集成）、配置动态路由规则（路径断言、负载均衡）、实现网关与微服务集群的完整集成 |



