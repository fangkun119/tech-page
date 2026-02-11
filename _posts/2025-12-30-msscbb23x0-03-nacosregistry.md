---
title: Spring Cloud Alibaba 03：Nacos注册中心详解
author: fangkun119
date: 2025-12-30 12:00:00 +0800
categories: [微服务, Spring Cloud Alibaba]
tags: [微服务, Spring Cloud Alibaba]
pin: false
math: true
mermaid: true
image:
  path: /imgs/cover/spring_cloud_alibaba.png
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
Nacos有两个功能：注册中心，配置托管。

这篇文档详解其中的注册中心，配套代码见 [spring-cloud-alibaba-2023-nacos](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/)

用到其中两个模块：

- [microservices/tlmall-nacos-demo-user](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-nacos-demo-user)
- [microservices/tlmall-nacos-demo-order](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-nacos-demo-order)

## 1. 注册中心设计思路

### 1.1 微服务为什么需要注册中心

微服务架构的系统中，业务被拆解到各个微服务中。这些微服务需要相互调用，如何进行服务发现和健康检查成为一大问题。Nacos注册中心就是解决此问题核心组件。

<img src="/imgs/spring-cloud-alibaba-03-nacos/f2b655516fc740f02794af51ad43ac06_MD5.jpg" width="100%" align="left" alt="微服务架构中服务发现和健康检查的示意图" />

具体来说注册中心需要提供如下功能：

(1) 服务发现：在调用下游微服务时，需要一种机制能够找到下游服务、选择合适机制进行调用：

| 功能         | 说明                              |
| ---------- | ------------------------------- |
| **实例动态管理** | 服务实例可以动态启动和停止，注册中心需要实时维护实例的可用状态 |
| **负载均衡支持** | 从多个可用实例中选择合适的调用目标，实现请求的合理分发     |
| **配置集中化**  | 避免在代码中硬编码服务地址，提供统一的配置管理         |

(2) 服务状态实时感知：下游服务有实例宕机时，能够及时感知并剔除，防止为调用以确保稳定性和用户体验：

| 功能         | 说明                          |
| ---------- | --------------------------- |
| **健康状态检测** | 定期检查服务实例的健康状态，及时发现故障节点      |
| **故障快速隔离** | 将故障实例从可用服务列表中移除，防止请求路由到失效节点 |
| **自动恢复机制** | 当故障实例恢复后，能够自动重新加入服务列表       |

在这个过程中，需要解决如下挑战：

| 挑战             | 说明                                           |
| ---------------- | ---------------------------------------------- |
| **服务发现效率** | 在高并发场景下，确保服务发现的低延迟和高可用性 |
| **数据一致性**   | 在集群环境中，保证各节点服务数据的一致性       |
| **网络分区处理** | 在出现网络分区时，确保服务发现功能的正确性     |
| **扩展性要求**   | 支持大规模微服务实例的注册和发现               |

### 1.2 注册中心演进历程

理解注册中心的挑战，要回顾它的演进过程。本节以“会员服务”调用“订单服务”为例，来演示注册中心的演进历程，说明传统中间件的不足。

#### (1) 阶段1：硬编码地址

<img src="/imgs/spring-cloud-alibaba-03-nacos/da2715e0dcd55e0f03292cfd9454a65a_MD5.jpg" width="100%" align="left" alt="硬编码地址的服务调用方式示意图" />

从上图可以看出，这是一种运维成本非常高、维护困难的方法。把上下游关系这样易变又重要的信息，散落在四处分散的代码中，更是编程的禁忌。首先想到的改进点就是把分散的配置集中起来。

#### (2) 阶段2：用注册表避免分散配置

<img src="/imgs/spring-cloud-alibaba-03-nacos/ff4b879f1a4fc386020e7aae3ba24494_MD5.jpg" width="100%" align="left" alt="使用注册表集中配置服务地址的示意图" />

这样做仍然有问题，最明显的就是：效性差、无自动感知能力。这使得人工介入频繁，影响了服务的稳定性，提高了运维成本，也让运维变得复杂容易出错。

问题之一：把实例(instance)这样细粒度的配置、暴露给业务系统，并不是我们期望的
问题之二：不能自动发现宕机的实例、是影响服务稳定性和频繁人工介入的关键原因

#### (3) 阶段3：用Nginx实现自动健康检查

<img src="/imgs/spring-cloud-alibaba-03-nacos/953faac8c68517ee433844fb3fd582f0_MD5.jpg" width="100%" align="left" alt="使用Nginx实现自动健康检查的架构图" />

Nginx提供了宕机实例自动感知的功能，并且只暴露给业务系统整个服务的域名，使业务系统不再需要关注每个服务具体有哪些实例。

但上图中新瓶颈的本质原因是，依然需要维护庞大的实例列表，它只是把这样的列表维护，从业务端移到了Nginx而已。

### 1.3 注册中心的设计思路

#### (1) 初步思路

传统中间件的种种不足、催生了专用微服务中间件的需求。本节以MySQL为数据库，用Demo设计的方式演示注册中心的设计思路。

根据上一小节的种种痛点，期望注册中心能满足如下需求：

<img src="/imgs/spring-cloud-alibaba-03-nacos/437148dc6b6ee06b82fddc3d3422d159_MD5.jpg" width="360" align="left" alt="注册中心需求设计示意图" />

和前面的最大差别在于：服务实例数据不再由人工配置，而是由各个服务实例主动上报（实例注册，实例注销），从而避免人工运维，同时也可以结合心跳机制等，实现自动感知（实例注销）。

这也意味着要对实例数据进行增删查改，因此自然涉及到API和数据库，来实现对应的操作。

<img src="/imgs/spring-cloud-alibaba-03-nacos/390e326f415adb82c3a90ccf867ff3f0_MD5.jpg" width="100%" align="left" alt="注册中心API和数据库架构图" />

数据库表如下

<img src="/imgs/spring-cloud-alibaba-03-nacos/e98a0adf03597068952bddda909ef8f3_MD5.jpg" width="100%" align="left" alt="注册中心数据库表结构设计" />

核心操作API演示如下

<img src="/imgs/spring-cloud-alibaba-03-nacos/e157ea2e86c811bef6b68b6c5f398a3b_MD5.jpg" width="100%" align="left" alt="注册中心核心操作API演示" />

暂时不考虑实例宕机和心跳机制，整体实现如下

<img src="/imgs/spring-cloud-alibaba-03-nacos/9e5347778b041900ac7859ef264eabbb_MD5.jpg" width="100%" align="left" alt="注册中心基本实现架构图" />

上游调用注册中心的`/discover`来找到要调用实例列表并随机选择，而下游主动调用`/register`或`/cancel`API来注册或注销自己。

#### (2) 问题和完善

##### 解决性能单点问题

第一个问题，注册中心和MySQL成了整个微服务系统的性能瓶颈。每次微服务间的相互调用，都要先访问注册中心，成为调用最密集的API，也是整个系统的风险点。首先想到的必然是缓存。

添加远程缓存并不能彻底解决单点问题，实例`IP:port`也不是频繁更新的数据，首选使用本地缓存。

<img src="/imgs/spring-cloud-alibaba-03-nacos/a0c0067147dd620fa9c579ca8de37730_MD5.jpg" width="100%" align="left" alt="注册中心添加本地缓存解决性能问题" />

##### 解决实例状态更新问题

使用缓存就必然要考虑数据同步问题，先尝试上游定时从注册中心主动拉取数据，例如每隔10秒拉取下游服务的实例列表，然后在本地做一个负载均衡。

<img src="/imgs/spring-cloud-alibaba-03-nacos/bd486874514fb43bc90cb9df0436ba65_MD5.jpg" width="100%" align="left" alt="客户端定时拉取服务实例列表" />

但是这样还有一个问题，就是如果实例宕机、没能主动向注册中心注销，就会导致注册中心中存放有错误的数据、让微服务调用已经宕机的实例，对可靠性造成影响。这带来一个新问题，如何主动发现宕机的实例、而不是被动等待注销。

<img src="/imgs/spring-cloud-alibaba-03-nacos/264a3c33cbcc4b0628205a4d109a12c4_MD5.jpg" width="100%" align="left" alt="实例宕机未主动注销的问题示意图" />

解决办法是给注册中心增加心跳检查机制，自动发现和剔除宕机的服务实例。具体来说，就是给数据表添加`last_heartbeat_time`字段，然后注册中心每五秒钟访问一次各个实例，更新这个字段。下一小节完善这个机制。

<img src="/imgs/spring-cloud-alibaba-03-nacos/f032ed3af6fab36689698c98f022a4df_MD5.jpg" width="100%" align="left" alt="注册中心心跳检查机制" />

##### 健康扫描和自动剔除机制

接下来进一步细化和完善这个机制，首先采用一个分级机制：

- 心跳检测：5秒一次
- 健康警示：15秒 (3次心跳检测) 仍然不能访问
- 实例移除：30秒 (6次心跳检测) 仍然不能访问

每一轮健康扫描，注册中心（服务端）都会向各个服务实例发送一遍健康检测请求，然后实例是否返回相应，依据分级规则标记实例状态，并决定是否移除。

<img src="/imgs/spring-cloud-alibaba-03-nacos/1498642f0a8e7b734d31d8efe74cfac4_MD5.jpg" width="100%" align="left" alt="健康扫描和自动剔除机制流程图" />

#### (3) Nacos的服务发现

综合上面的所有内容，就是完整的服务注册和发现系统：

- 客户端（服务消费者）：定时从注册中心拉取实例列表，缓存在本地，使用本地负载均衡选择实例
- 服务实例：主动把自己登记在注册中心，正常关机时主动注销
- 注册中心：定时扫描所有实例，向实例发送心跳，更新实例状态

如果因为网络中断、导致实例被判为宕机并剔除。实例内部会有连接中断检测机制，会用指数退避策略检测网络是否恢复，一旦恢复，就会发出注册请求，把自己重新登记到注册中心。

<img src="/imgs/spring-cloud-alibaba-03-nacos/5ab0de1214123382d9696a785b1a310c_MD5.jpg" width="100%" align="left" alt="Nacos服务发现完整架构图" />

## 2. Nacos注册中心核心概念

### 2.1 功能回顾

<img src="/imgs/spring-cloud-alibaba-03-nacos/897aa64db1f3675b53e5c2ab45b92294_MD5.jpg" width="100%" align="left" alt="Nacos两大核心功能示意图" />

Nacos同时承担着注册中心（服务发现）和配置托管两个任务。上一篇文档演示了如何快速搭建整套微服务，包含了Nacos注册中心的基本使用，这篇对其深入介绍。

### 2.2 数据模型

使用Nacos注册中心要理清楚下面几个概念

<img src="/imgs/spring-cloud-alibaba-03-nacos/0bcca30772fe89cb7eb3679e9a1b017b_MD5.jpg" width="100%" align="left" alt="Nacos数据模型层次结构图" />

首先是服务（Service）和实例（Instance）：服务是具体的应用（例如库存服务），实例则是该应用的正在运行的进程。服务通常采用多实例的方式实现高可用和负载均衡。

<img src="/imgs/spring-cloud-alibaba-03-nacos/ce3629e4d9f061b94807c7ff9a3aea35_MD5.jpg" width="100%" align="left" alt="Nacos服务与实例关系示意图" />

然后是命名空间（Namespace）和分组（Group）：

命名空间是租户粒度的环境隔离，不同命名空间下的服务互不可见，不能相互调用。典型应用场景是：1️⃣区分开发、测试、生产环境；2️⃣ 多租户隔离

<img src="/imgs/spring-cloud-alibaba-03-nacos/f228d0ffbd2056c093372d28ae37a991_MD5.jpg" width="100%" align="left" alt="Nacos命名空间隔离示意图" />

分组是命名空间下的次级划分，同一个命名空间、不同分组下的服务可以相互调用（需指定分组）。典型应用场景是对不同业务线、不同项目的服务进行逻辑划分。

<img src="/imgs/spring-cloud-alibaba-03-nacos/2860592b021649201365bafe606a0871_MD5.jpg" width="100%" align="left" alt="Nacos服务分组示意图" />

除了服务（Service）和实例（Instance），其实在他俩之间，还包含了一个中间层 —— 集群（Cluster）。集群用来对服务所属的集群按照地域、机房等地域属性进行分组。

Nacos还容许为服务、集群、机房配置元数据（Metadata），

<img src="/imgs/spring-cloud-alibaba-03-nacos/1e74e18c500700883d7bdfaa564cc73d_MD5.jpg" width="100%" align="left" alt="Nacos元数据层次结构图" />

这三层元数据的典型用途、特点、设置方式对比如下：

| 层级       | **服务元数据**                                                      | **集群元数据**                                                                   | **实例元数据**                                                       |
| -------- | -------------------------------------------------------------- | --------------------------------------------------------------------------- | --------------------------------------------------------------- |
| **核心作用** | 定义**全局服务策略**                                                   | 定义**集群特性**                                                                  | 描述**实例个性**                                                      |
| **数据特点** | 静态、共享                                                          | 区域共享、较静态                                                                    | 动态、独立                                                           |
| **典型用途** | 协议类型、路由规则、服务描述                                                 | 机房信息、流量类型、容灾等级                                                              | 版本、权重、灰度、性能                                                     |
| **基础示例** | `protocol: "http"`<br>`description: "订单服务"`                    | `region: "beijing"`<br>`traffic: "main"`                                    | `version: "2.0"`<br>`weight: "80"`                              |
|          | `route-policy: "same-zone-priority"`<br>`selector: "env=prod"` | `region: "cn-beijing"`<br>`traffic-type: "main"`<br>`capacity: "10000-qps"` | `gray-release: "true"`<br>`weight: "100"`<br>`version: "2.0.1"` |
| **设置方式** | OpenAPI/控制台                                                    | OpenAPI/控制台                                                                 | 客户端注册/控制台                                                       |

设置好元数据之后，就可以在代码中读取这些元数据，实现具体的策略。例如同机房优先路由、灰度策略发布等。

```java
@Component
public class OrderServiceRouter implements RequestContextListener {

    @Autowired
    private DiscoveryClient discoveryClient;
    
    // 当前应用所在区域（通过环境变量注入）
    @Value("${deploy.region}")
    private String currentRegion;

    /**
     * 智能路由：优先同机房 + 灰度版本
     */
    public ServiceInstance chooseOrderService() {
        List<ServiceInstance> allInstances = discoveryClient.getInstances("order-service");
        
        // 1. 先筛选同机房实例（利用集群元数据）
        List<ServiceInstance> sameZoneInstances = allInstances.stream()
            .filter(instance -> {
                String instanceRegion = instance.getMetadata().get("region");
                return currentRegion.equals(instanceRegion);
            })
            .collect(Collectors.toList());
        
        // 如果同机房无可用实例，降级到跨机房
        List<ServiceInstance> candidates = sameZoneInstances.isEmpty() ? 
            allInstances : sameZoneInstances;
        
        // 2. 再筛选灰度版本实例（利用实例元数据）
        boolean enableGray = shouldEnableGray(); // 根据用户ID或流量比例判断
        
        List<ServiceInstance> finalCandidates = candidates.stream()
            .filter(instance -> {
                boolean isGray = "true".equals(instance.getMetadata().get("gray-release"));
                return enableGray ? isGray : !isGray;
            })
            .collect(Collectors.toList());
        
        // 3. 根据权重随机选择（权重在实例元数据中）
        return selectByWeight(finalCandidates);
    }

    private ServiceInstance selectByWeight(List<ServiceInstance> instances) {
        Map<ServiceInstance, Integer> weightMap = instances.stream()
            .collect(Collectors.toMap(
                instance -> instance,
                instance -> Integer.parseInt(
                    instance.getMetadata().getOrDefault("weight", "100")
                )
            ));
        return WeightedRandomSelector.choose(weightMap);
    }
}

```

关于Nacos注册中心的数据模型，具体可以参考官方文档<https://nacos.io/docs/ebook/knk2h0/>

### 2.3 核心功能

理解Nacos注册中心的核心功能，主要是理解一个服务实例（Instance）从上线到下线的完整生命周期。上线这部分可以用下图来概括。下线则比较简单，主要是主动下线，或者健康检查没有通过被注册中心从实例列表剔除。

<img src="/imgs/spring-cloud-alibaba-03-nacos/333d29614787a641168297747ade6597_MD5.jpg" width="100%" align="left" alt="Nacos服务实例上线流程图" />

服务注册：由服务实例主动调用Nacos API来触发。时机有两种：实例启动时注册；实例发现收不到心跳时尝试重新注册（间隔指数递增）。

<img src="/imgs/spring-cloud-alibaba-03-nacos/a2fb61d8857eeab49e37874127824cb8_MD5.jpg" width="100%" align="left" alt="服务注册流程示意图" />

注册后进入心跳维持阶段，由Nacos主动发起心跳检测，每5秒一次

<img src="/imgs/spring-cloud-alibaba-03-nacos/fb7e54e0c27714063ef3fb605bd6f4ee_MD5.jpg" width="100%" align="left" alt="心跳维持机制示意图" />

随后，进入健康检查阶段。15秒收不到心跳回复则标记为不健康。对于默认的临时实例，30秒收不到心跳回复则将其剔除；而对于特殊的持久实例，则不进行剔除，需要用户通过控制台或API来下线实例。

<img src="/imgs/spring-cloud-alibaba-03-nacos/5130d95ec8b9e4d88636e90a5fcb9c0f_MD5.jpg" width="100%" align="left" alt="健康检查和实例剔除流程" />

基于上述机制，微服务就可以定期从注册中心拉取下游服务的实例列表，列表中只包含处于健康状态的实例。然后结合本地负载均衡组装（例如Round Robin）选择一个实例发起调用。

<img src="/imgs/spring-cloud-alibaba-03-nacos/d4efebe62ff7e7360fe12359406874f2_MD5.jpg" width="380" align="left" alt="服务发现和负载均衡示意图" />

总结上面的所有内容，就得到了一个服务实例的完整生命周期

<img src="/imgs/spring-cloud-alibaba-03-nacos/b2be93feed6863f8e8b0b3b13657e401_MD5.jpg" width="580" align="left" alt="服务实例完整生命周期流程图" />

### 2.4 总结

Nocos注册中心的强大之处，在于：

- 清晰的架构蓝图：命名空间、分组、服务、集群、实例
- 高效的运行引擎：注册、心跳、发现

这种设计实现了服务的逻辑隔离和动态生命周期的完美结合。

不仅如此，Nacos自身也是一个分布式系统，各个Nacos实例之间通过分布式协议同步数据，最终做到高可用，再后续章节也会演示。

<img src="/imgs/spring-cloud-alibaba-03-nacos/9e9f2581108dbb067292a10e7aef547f_MD5.jpg" width="100%" align="left" alt="Nacos集群分布式架构示意图" />

## 3. Nacos常用配置详解

本节通过实验环境搭建，来深入理解Nacos核心概念在实战中的应用。

### 3.1 实验环境

实验场景是会员服务([`tlmall-user`](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-nacos-demo-user))调用订单服务([`tlmall-order`](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-nacos-demo-order))来查询一个用户的订单。本节小先用先把这两个服务搭建起来。

<img src="/imgs/spring-cloud-alibaba-03-nacos/33c0844b35082fc7af0355843a760551_MD5.jpg" width="500" align="left" alt="会员服务调用订单服务实验场景" />

通过配置Nacos注册中心，来让两个服务都注册到默认的public命名空间![[imgs/spring-cloud-alibaba-03-nacos/a06fb4ff6fc57fea184cdbd85b9d6ab9_MD5.jpg]]
具体代码包括

|          | 会员服务                                                                                                                                                                                                                                                                                       | 订单服务                                                                                                                                                                                                                                                                                  |
| -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Maven依赖  | [microservices/tlmall-nacos-demo-user/pom.xml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-user/pom.xml)<br><br>`spring-cloud-starter-alibaba-nacos-discovery`                                                                 | [microservices/tlmall-nacos-demo-order/pom.xml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-order/pom.xml)<br><br>`spring-cloud-starter-alibaba-nacos-discovery`                                                          |
| 服务发现配置   | [microservices/tlmall-nacos-demo-user/src/main/resources/application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-user/src/main/resources/application.yml)<br><br>`spring.cloud.nacos.descovery.*`                         | [microservices/tlmall-nacos-demo-order/src/main/resources/application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-order/src/main/resources/application.yml)<br><br>`spring.cloud.nacos.descovery.*`                  |
| 服务发现Beam | [microservices/tlmall-nacos-demo-order/src/.../TlmallOrderApplication.java](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-order/src/main/java/org/nacosdemo/tlmallorder/TlmallOrderApplication.java)<br>`@EnableDiscoveryClient` | [microservices/tlmall-nacos-demo-user/src/.../TlmallUserApplication.java](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-user/src/main/java/org/nacosdemo/tlmalluser/TlmallUserApplication.java)<br>`@EnableDiscoveryClient` |

依次启动订单服务和用户服务

<img src="/imgs/spring-cloud-alibaba-03-nacos/963850b292e9e722d956067266c7f021_MD5.jpg" width="280" align="left" alt="服务启动日志截图" />

可以看到它们都能够注册到Nacos

<img src="/imgs/spring-cloud-alibaba-03-nacos/15ae8ffaa864130b991dcef70a58db01_MD5.jpg" width="100%" align="left" alt="服务注册到Nacos控制台截图" />

向会员服务发送请求，它能够从订单服务获得订单详情、并返回

<img src="/imgs/spring-cloud-alibaba-03-nacos/59ee339238407de9a04391368fca5858_MD5.jpg" width="100%" align="left" alt="会员服务调用订单服务成功截图" />

接下来将在这个环境基础上完成接下来的实验

### 3.2 服务隔离

#### (1) 命名空间

对一个微服务来说，Namespace, Group, Service Name三元组才是它的唯一标识。

<img src="/imgs/spring-cloud-alibaba-03-nacos/94d8a987d162b156cc83d74399798120_MD5.jpg" width="560" align="left" alt="Nacos服务唯一标识三元组示意图" />

命名空间是Nacos的环境隔离方案，它可以用来把不相关的微服务隔离开，相互干扰。从Nacos Discovery获取下游服务的实例列表时，只会返回相同命名空间下的实例，起到服务隔离的作用。

<img src="/imgs/spring-cloud-alibaba-03-nacos/b779c2f5907e398a891f7997016a425b_MD5.jpg" width="100%" align="left" alt="命名空间服务隔离效果示意图" />

但是注意，这中隔离框架层面的约束，**并非不不可以绕开**。例如用继承父类的方法来替换`NacosServiceDiscovery Bean`来返回其它命名的实例，嵌入自定义的隔离策略，虽然并不推荐。

#### (2) 服务分组

在默认配置下，从Nacos Discovery获取下游服务的实例列表时，同样只会返回相同服务分组下的实例，这一点与命名空间相同。但是框架也提供了便捷方法，来访问其它分组的实例。

在应用方面

- 默认情况：将调用链上的服务规范在同一个Group中，例如部门内部众多微服务相互调用。
- 跨组访问：指定分组名称来访问其它分组的微服务，例如各部门访问公司的公用微服务。

跨组访问推荐在服务网关（例如上篇文档中的tlmall-gateway）中配置跨组路由

```yml
spring:
  cloud:
    gateway:
      routes:
        - id: cross-group-route
          # 在URI中指定分组名称为GroupB
          uri: lb://user-service.GroupB
          predicates:
            - Path=/cross-group/**
```

#### (3) 隔离演示

本节演示如何使用Namespace进行服务隔离。首先在Nacos创建ID为dev的命名空间（注意是ID，因为Nacos配置中使用的也是ID，**实践中应当避免让Nacos自动生成命名空间ID**）

<img src="/imgs/spring-cloud-alibaba-03-nacos/5fde21b91f0cac91a1562704f21b99ec_MD5.jpg" width="100%" align="left" alt="创建dev命名空间截图" />

首先把订单服务的配置（[microservices/tlmall-nacos-demo-order/.../application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-order/src/main/resources/application.yml)）中`namespace: dev`前面的"# "去掉，让它生效，然后重启。

```yml
spring:  
  application:  
    name: tlmall-order  
  cloud:  
    nacos:  
      discovery:  
        # 基础实验环境配置  
        server-addr: tlmall-nacos-server:8848  
        username: nacos  
        password: nacos  
        # 服务逻辑隔离实验  
        namespace: dev
# ...
```

重启后，它就把自己注册到了`ID`为`dev`的命名空间下

<img src="/imgs/spring-cloud-alibaba-03-nacos/8d6c29398f31a832ca5640120e726ed1_MD5.jpg" width="100%" align="left" alt="订单服务注册到dev命名空间截图" />

而用户服务仍然留在默认的`public`命名空间下

<img src="/imgs/spring-cloud-alibaba-03-nacos/142d1b4b234e6980e1e99fee4c6b7e0d_MD5.jpg" width="100%" align="left" alt="用户服务留在public命名空间截图" />

此时再次用Postman发送请求，用户服务已经无法访问订单服务了

<img src="/imgs/spring-cloud-alibaba-03-nacos/c547b73d3c24334a80744eac52a274b6_MD5.jpg" width="100%" align="left" alt="跨命名空间调用失败截图" />

日志显示，用户服务在访问订单服务是，订单服务没有实例

```text
ERROR 8936 --- [tlmall-user] [nio-8050-exec-1] o.a.c.c.C.[.[.[/].[dispatcherServlet]    : Servlet.service() for servlet [dispatcherServlet] in context with path [] threw exception [Request processing failed: java.lang.IllegalStateException: No instances available for tlmall-order] with root cause

java.lang.IllegalStateException: No instances available for tlmall-order
	at ......
```

查看用户服务访问订单服务的代码，看到它使用的订单服务在Nacos中的服务名`tlmall-order`，并没有任何表示Namespace的字段。这代表着，用户服务访问的是，与它相同Namespace下的订单服务。

```java
String url = "http://tlmall-order/order/getOrder?userId="+userId;  
Result result = restTemplate.getForObject(url,Result.class);  
return result;
```

### 3.3 集群和元数据

#### (1) 指定集群

回顾之前的内容，一个微服务下，可以用`集群`来对它的实例进行逻辑划分。

<img src="/imgs/spring-cloud-alibaba-03-nacos/0747a18451f9e580e7d9983d08f05f25_MD5.jpg" width="100%" align="left" alt="集群概念示意图" />

首先要指定一个实例属于哪个集群，通过在实例配置中指定`cluster-name`来实现，例如[microservices/tlmall-nacos-demo-user/src/main/resources/application.ym](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-user/src/main/resources/application.yml)

```yml
spring:  
  application:  
    name: tlmall-user  
  cloud:  
    nacos:  
      discovery:  
        # 基础实验环境配置  
        server-addr: tlmall-nacos-server:8848  
        username: nacos  
        password: nacos  
        # 服务逻辑隔离实验  
        # namespace: dev  
        # 指定所属的集群  
        cluster-name: sh-cluster
```

重启后，就可以看到实例被注册在指定的配置下

<img src="/imgs/spring-cloud-alibaba-03-nacos/8ad5f5645bccf2d2ae10bf0a82212f83_MD5.jpg" width="100%" align="left" alt="实例注册到指定集群截图" />

#### (2) 集群元数据

进一步的，在Nacos中为这个集群添加元数据

<img src="/imgs/spring-cloud-alibaba-03-nacos/f2a15c2e08ce18065043604b0167ce52_MD5.jpg" width="500" align="left" alt="集群元数据配置截图" />

#### (2) 实例元数据

实例的元数据可以通过`application.yml`填写

```yml
spring:  
  application:  
    name: tlmall-user  
  cloud:  
    nacos:  
      discovery:  
        # 基础实验环境配置  
        server-addr: tlmall-nacos-server:8848  
        username: nacos  
        password: nacos  
        # 服务逻辑隔离实验  
        # namespace: dev
        # 所属集群  
        cluster-name: sh-cluster  
        # 自定义元数据  
        metadata:
          version: "v2.1.0"  
          region: "cn-shanghai"  
          usage: "demo"  
          weight: "100"
```

也能以编程的方式动态注入（2.3.0版本开始）

```java
@Configuration
public class NacosMetadataConfig {
    @Bean
    public NacosDiscoveryProperties nacosProperties(Environment env) {
        NacosDiscoveryProperties properties = new NacosDiscoveryProperties();
        Map<String, String> metadata = properties.getMetadata();
        metadata.put("startup.time", 
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        metadata.put("java.version", env.getProperty("java.version"));
        metadata.put("build.number", env.getProperty("build.number", "unknown"));
        return properties;
    }
}
```

服务重启后，在Nacos控制台可以看到这些元数据

<img src="/imgs/spring-cloud-alibaba-03-nacos/33ad47a12db7ac661c5f2e85bbe2097c_MD5.jpg" width="100%" align="left" alt="实例元数据显示截图" />

使用集群名称或者元数据，结合负载均衡器，就能实现特定的流量路由功能，例如<https://blog.csdn.net/fei476662546/article/details/144205394>

<img src="/imgs/spring-cloud-alibaba-03-nacos/3245e667cc2ac26f662dfd355cfafcdd_MD5.jpg" width="100%" align="left" alt="元数据实现流量路由示例图" />

### 3.4 临时实例和持久实例（2.0之后改为永久服务）

#### (1) 用途

在Nacos中，实例处理默认的临时实例（Ephemeral），还有一种是持久实例（Persistent）。它们的差别如下图。

<img src="/imgs/spring-cloud-alibaba-03-nacos/994b19b4251d97c0feedc737f6b85b0d_MD5.jpg" width="100%" align="left" alt="临时实例和持久实例对比图" />

持久实例在心跳实例失效时，不会被Nacos注册中心剔除。

这主要用于基础服务，有独立于Nacos的服务保障机制，例如：

- 数据库服务
- 缓存服务
- 消息队列

它们不希望被Nacos的心跳摘除机制干预。

更详细的对比如下表

| 特性              | 临时实例       | 持久实例      |
| --------------- | ---------- | --------- |
| 健康检查机制          | 客户端主动上报心跳  | 服务端主动探测   |
| 数据存储            | 内存存储       | 磁盘持久化     |
| 服务下线            | 自动剔除(心跳超时) | 手动删除      |
| 使用场景            | 业务微服务      | 基础设施服务    |
| 一致性保证           | AP(可用性优先)  | CP(一致性优先) |

#### (2) 开启持久实例

微服务可以通过`spring.cloud.nacos.discovery.enphemeral=false`来开启持久实例

<img src="/imgs/spring-cloud-alibaba-03-nacos/7232bf666a7d6ff799e92c726d1ef6b6_MD5.jpg" width="100%" align="left" alt="开启持久实例配置截图" />

两个注意事项

- 2.x版本将持久化提升到服务级别，同一个服务不能同时存在持久实例和临时实例
- 需要Nacos集群模式，单机模式不支持持久实例，需要通过Raft协议保证一致性

## 4. Nacos安全配置详解

### 4.1 安全认证原理

Nacos是微服务的核心，暴露给攻击者会给整个微服务都带来危险，例如：服务信息暴露，核心配置暴露，恶意注册和删除实例，恶意修改服务元数据和配置。

<img src="/imgs/spring-cloud-alibaba-03-nacos/c34ef8b04b44a8944442f20c80a6acae_MD5.jpg" width="100%" align="left" alt="Nacos安全威胁示意图" />

但同时Nacos是内部组件，并不需要暴露到外网。可以结合内外部措施来试试，既保障安全，又兼顾高效。通常的实践如下。

- Nacos部署在内网,通过防火墙限制访问
- 启用Nacos安全认证
- 定期审计访问日志
- 使用HTTPS加密传输

安全认证方面，Nacos提供了两套认证体系，分别适用于：（1）用户和微服务访问；（2）Nacos内部通信

<img src="/imgs/spring-cloud-alibaba-03-nacos/a3e11318f1d792f25884c982c8a95f04_MD5.jpg" width="100%" align="left" alt="Nacos两套认证体系架构图" />

自定义程度更高的鉴权策略，则需要靠编写[Nacos Plugin](https://github.com/nacos-group/nacos-plugin)来实现l。

### 4.2 安全认证配置

官方文档见<https://nacos.io/docs/next/manual/user/auth/>。

配置过程分为三部分：Nacos服务端，Nacos控制台，微服务。

注意：这些步骤重在演示和理解Nacos的使用，实际生产环境中需要考虑的更加缜密，确保开启过程中不会影响服务的可用性。

#### (1) 修改Nacos配置开启认证

在Nacos的`conf/application.properties`开启下面的配置，开启后立刻生效，不需要重启。

首先开启认证功能

<img src="/imgs/spring-cloud-alibaba-03-nacos/52a4a773125b36fe856c25b9ca2d9b2a_MD5.jpg" width="100%" align="left" alt="开启认证功能配置截图" />

然后添加Nacos内部通信用的AK/SK （Access Key / Secret Key）配置

<img src="/imgs/spring-cloud-alibaba-03-nacos/b2aa2bbb3a8e9c32e5bada05b3117eb6_MD5.jpg" width="100%" align="left" alt="Nacos内部通信AK/SK配置截图" />

最后添加供微服务/用户访问所需要的密钥，用来生成Access Token

<img src="/imgs/spring-cloud-alibaba-03-nacos/3049f8d4176ab73e7407945c093d5f2f_MD5.jpg" width="100%" align="left" alt="微服务访问密钥配置截图" />

按照上面的方法，修改如下配置

```properties
# 开启认证
nacos.core.auth.enabled=true

# Nacos内部通信用的AK/SK配置，可理解为“白名单”，建议用复杂无规律的字符串
# 注意：(1)不用于微服务或者用户访问；(2)实现见com.alibaba.nacos.core.auth.AuthFilter
nacos.core.auth.server.identity.key=authKey
nacos.core.auth.server.identity.value=nacosSecurty

# Nacos给微服务和用户访问的密钥配置，用于为它们生成Access Token，
# 注意：
#（1）使用默认值有安全风险(2.2.0.1后无默认值)；
#（2）使用Base64编码，编码前原始密钥长度少于32字符；
#（3）推荐随机串工具例如 http://tool.pfan.cn/random?chknumber=1&chklower=1&chkupper=1
nacos.core.auth.plugin.nacos.token.secret.key=tXSVzvbEWi8oB4KtTVGvF6nNXFRTSJygtP3xfo50cSVk
```

开启认证后，微服务将无法把自己注册到Nacos

<img src="/imgs/spring-cloud-alibaba-03-nacos/fc83df331312435e173fe77463035f27_MD5.jpg" width="100%" align="left" alt="开启认证后服务注册失败截图" />

#### (2) 在Nacos控制台配置账户

的接下来创建账户并授权，创建的账户将被微服务用来访问Nacos。

Nacos的权限管理使用的是常用的 `用户 ↔️ 角色 ↔️ 权限` 绑定体系，就是：

- 用户绑定到具体的角色
- 给每个角色绑定具体的权限
- 权限为对命名空间的读写操作

下面是操作步骤：首先创建`用户`和`密码`，这也是后面要添加在微服务配置中的

<img src="/imgs/spring-cloud-alibaba-03-nacos/df16899f3ae3727e8d655b3f898c6728_MD5.jpg" width="100%" align="left" alt="创建用户和密码截图" />

然后创建`角色`并绑定上一步创建的`用户`，最后给角色绑定`权限`即对特定命名空间的读写权限

<img src="/imgs/spring-cloud-alibaba-03-nacos/d6880468c46b2ab1938ed9152cef7051_MD5.jpg" width="100%" align="left" alt="创建角色并绑定权限截图" />

通过上述操作，微服务使用用户名`fox`和密码`123456`可以获得对命名空间`dev`的`读权限、写权限`

#### (3) 为微服务配置用户名和密码

把微服务的`application.yml`中，把Nacos Discovery的`username`和`password`改成刚才创建的用户名和密码。这个微服务就具备了对`dev`命名空间的读写权限。

<img src="/imgs/spring-cloud-alibaba-03-nacos/36b5a3bf3e96311d6013bf571b45de40_MD5.jpg" width="100%" align="left" alt="微服务配置用户名密码截图" />

注意上面的namespace配置，它所填的namespace，必须是`username`在Nacos控制台中配置了权限的命名空间。

配置修改之后，重新微服务，就能够注册到Nacos了。要在日志和Nacos控制台双重确认。

<img src="/imgs/spring-cloud-alibaba-03-nacos/217cdb588028791493056a758b434a07_MD5.jpg" width="100%" align="left" alt="配置认证后服务注册成功截图" />

## 5. Nacos集群部署

官方文档：<https://nacos.io/zh-cn/docs/v2/guide/admin/cluster-mode-quick-start.html>

### 5.1 需求和部署方案

之前小节使用单机模式Nacos + 内置基于内存的Derby数据库，这个组合仅限于演示，既没有可用性保障、又没有数据持久化。

<img src="/imgs/spring-cloud-alibaba-03-nacos/deb46a498cbb6b0122040986524470e6_MD5.jpg" width="100%" align="left" alt="单机Nacos架构示意图" />

期望的方式是：Nginx + Nacos集群 + 主备模式且有可用性保障的数据库。为了方便搭建实验环境和演示，Demo使用的是MySQL。

<img src="/imgs/spring-cloud-alibaba-03-nacos/c7a557657f72361916e7bab272155f77_MD5.jpg" width="100%" align="left" alt="Nacos集群架构示意图" />

采用Nacos集群的优势是：

- 高可用:任意节点故障不影响整体服务
- 负载均衡:请求均匀分发到各节点
- 数据一致性:MySQL保证数据不丢失
- 易扩展:可根据需求增加节点

### 5.2 虚拟机环境准备

#### (1) 端口偏移规则

搭建Nacos集群，一共要使用4个端口。除了主端口以外，还有三个端口，通过主端口叠加偏移量计算得出。

| 端口   | 与主端口关系     | 作用                   | 协议    | 对外开放    |
| ---- | ---------- | -------------------- | ----- | ------- |
| 8848 |            | 主端口：HTTP API / 控制台端口 | HTTP  | 是       |
| 9848 | 主端口 + 1000 | 客户端请求端口              | gRPC  | 是       |
| 9849 | 主端口 + 1001 | 服务间数据同步端口            | gRPC  | 否（内部通信） |
| 7848 | 主端口 - 1000 | Raft协议端口             | JRAFT | 否（内部通信） |

例如：

```text
如果主端口修改为8849,则:
- gRPC客户端端口: 9849 (8849+1000)
- gRPC服务端端口: 9850 (8849+1001)
- Raft协议端口: 7849 (8849-1000)
```

重要的事情说三遍：

```text
在Nacos集群模式下，微服务注册失败的常见原因之一，就是
- 防火墙只开通了主端口
- 没有开通另外三个所需的端口
```

#### (2) 虚拟机配置要求

<img src="/imgs/spring-cloud-alibaba-03-nacos/0d24e42f0b982cb7c7881eabe617a680_MD5.jpg" width="100%" align="left" alt="虚拟机环境配置截图" />

硬件配置建议

| 资源  | 最低配置    | 推荐配置     |
| --- | ------- | -------- |
| CPU | 2核      | 4核       |
| 内存  | 4GB     | 8GB      |
| 磁盘  | 20GB    | 50GB SSD |
| 网络  | 100Mbps | 1Gbps    |

软件环境要求

| 软件    | 版本要求 | 说明          |
| ----- | ---- | ----------- |
| JDK   | 8+   | 不要使用OpenJDK |
| MySQL | 5.7+ | 建议8.0+      |
| Nacos | 2.x  | 本文档基于2.3.2  |

重要提示:Nacos 2.3.2不支持OpenJDK，问题如下

```text
现象:无论输入什么用户名密码,都提示"用户名密码错误"  
原因:Nacos 2.3.2在OpenJDK环境下存在鉴权bug  
解决:卸载OpenJDK,安装Oracle JDK或其他兼容JDK
```

官方Issue链接：<https://github.com/alibaba/nacos/issues/12097>

卸载Centos7自带的OpenJDK并安装JDK1.8的图文教程：<https://www.jb51.net/program/3244507tc.htm>
JDK版本检查:

```text
java -version  
# 正确输出示例  
java version "1.8.0_281"  
Java(TM) SE Runtime Environment (build 1.8.0_281-b09)  
​  
# 错误输出示例  
openjdk version "11.0.11"  ← 不支持
```

#### (3) 虚拟机创建

创建三台虚拟机，使用Centos7，为它们在虚拟子网中设置如下IP

```bash
# 准备三台centos7服务器
192.168.65.207
192.168.65.208
192.168.65.213
```

### 5.3 配置虚拟机中的Nacos节点（cluster.conf）

步骤如下，注意不要使用127.0.0.1或Localhost

<img src="/imgs/spring-cloud-alibaba-03-nacos/50587973775e5e39be2f57f8cc584398_MD5.jpg" width="100%" align="left" alt="配置cluster.conf步骤示意图" />

默认情况下，完整命令如下

```bash
$ cd nacos/conf/
$ cp cluster.conf.example cluster.conf
$ vim cluster.conf # 设置IP配置
$ cat cluster.conf # 查看配置
#
# Copyright 1999-2021 Alibaba Group Holding Ltd.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# nacos instance ip
192.168.65.207:8848
192.168.16.208:8848
192.168.16.213:8848
```

假如是多网卡环境，Nacos支持指定网卡或IP，可以使用的配置项如下

```properties
#多网卡选择
#ip-address参数可以直接设置nacos的ip
#该参数设置后，将会使用这个IP去cluster.conf里进行匹配，请确保这个IP的值在cluster.conf里是存在的
nacos.inetutils.ip-address=10.11.105.155

#use-only-site-local-interfaces参数可以让nacos使用局域网ip，这个在nacos部署的机器有多网卡时很有用，可以让nacos选择局域网网卡
nacos.inetutils.use-only-site-local-interfaces=true

#ignored-interfaces支持网卡数组，可以让nacos忽略多个网卡
nacos.inetutils.ignored-interfaces[0]=eth0
nacos.inetutils.ignored-interfaces[1]=eth1

#preferred-networks参数可以让nacos优先选择匹配的ip，支持正则匹配和前缀匹配
nacos.inetutils.preferred-networks[0]=30.5.124.
nacos.inetutils.preferred-networks[0]=30.5.124.(25[0-5]|2[0-4]\\d|((1d{2})|([1-9]?\\d))),30.5.124.(25[0-5]|2[0-4]\\d|((1d{2})|([1-9]?\\d)))
```

### 5.4 配置Nacos节点认证

Nacos节点之间相互通信需要安全认证，微服务和UI访问Nacos节点的API也需要安全认证。因此需要添加认证配置，方法就是`4.2.(1)`小节所介绍的，修改`conf/application.properties`设置如下配置.

```properties
# 开启认证
nacos.core.auth.enabled=true

# Nacos内部通信用的AK/SK配置，可理解为“白名单”，建议用复杂无规律的字符串
# 注意：(1)不用于微服务或者用户访问；(2)实现见com.alibaba.nacos.core.auth.AuthFilter
nacos.core.auth.server.identity.key=authKey
nacos.core.auth.server.identity.value=nacosSecurty

# Nacos给微服务和用户访问的密钥配置，用于为它们生成Access Token，
# 注意：
#（1）使用默认值有安全风险(2.2.0.1后无默认值)；
#（2）使用Base64编码，编码前原始密钥长度少于32字符；
#（3）推荐随机串工具例如 http://tool.pfan.cn/random?chknumber=1&chklower=1&chkupper=1
nacos.core.auth.plugin.nacos.token.secret.key=tXSVzvbEWi8oB4KtTVGvF6nNXFRTSJygtP3xfo50cSVk
### 集群配置详解
```

需要注意的是，所有Nacos节点都要添加完全相同的配置。

### 5.5 配置Nacos使用的数据库

现前都是使用Nacos内置的Derby内存数据库，现在要准备一个真正的数据库来持久化Nacos数据，生产环境中建议至少使用主备模式。

<img src="/imgs/spring-cloud-alibaba-03-nacos/56958656f203009b447bd9f008580cc8_MD5.jpg" width="100%" align="left" alt="配置MySQL数据库示意图" />

我们使用MySQL，从官方（例如[Nacos 2.3.2](https://github.com/alibaba/nacos/blob/2.3.2/distribution/conf/mysql-schema.sql)）获取建库脚本（[/nacos/init_mysql_database.sql](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/nacos/init_mysql_database.sql)）创建数据库，然后修改Nacos的application.properties配置，添加数据库连接：

```properties
spring.sql.init.platform=mysql
db.num=1
db.url.0=jdbc:mysql://192.168.65.211:3306/nacos?characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useUnicode=true&useSSL=false&serverTimezone=UTC
db.user.0=root
db.password.0=123456
```

常见错误：“Nacos Server did not start because dumpservice bean construction failure: No DataSource set"，此时要检查Nacos的MySQL连接、数据库IP、端口、用户名、密码是否正确。

### 5.6 启动Nacos集群

<img src="/imgs/spring-cloud-alibaba-03-nacos/c13398ac8721503ae46fbffff33d52cb_MD5.jpg" width="100%" align="left" alt="启动Nacos集群步骤示意图" />

启动所有节点，对每个节点，进入 `nacos`目录，执行`bash bin/startup.sh`即可

全部启动之后，登录其中一个节点的控制台：`http://192.168.65.207:8848/nacos`，用户名密码都是nacos。就可以看到3个Nacos节点，并且其中一个被选举为Leader。

### 5.7 微服务访问Nacos

#### (1) 不推荐硬编码配置

<img src="/imgs/spring-cloud-alibaba-03-nacos/dd285cf695cf393e50b0bf00e8048b8f_MD5.jpg" width="100%" align="left" alt="微服务连接Nacos集群配置方式对比图" />

可以像下面那样，直接填入三个Nacos节点的地址，让微服务连接Nacos集群。

```yml
spring:  
  application:  
    name: tlmall-order  
  cloud:  
    nacos:  
      discovery:  
        # (1) 基础实验环境配置  
        # server-addr: tlmall-nacos-server:8848  
        username: nacos  
        password: nacos  
        # (6) 指定Nacos集群地址（Nacos集群实验）  
        # 方案1：直连多节点
        # 优势：无额外单点，客户端自动故障转移
        # 劣势：硬编码IP地址不便于运维
        server-addr: 192.168.65.207:8848,192.168.65.208:8848,192.168.65.213:8848  
        # 方案2：Nginx反向代理
        # 优势：配置集中管理，简化运维
        # 劣势：Nginx成为单点
        # server-addr: nacos.tlmall.com:8848
```

但是配置IP并不便于运维，因此同时也介绍第二种方法，为Nacos节点部署一个Nginx，让微服务连接Nginx以实现配置解耦

#### (2) 安装Nginx

<img src="/imgs/spring-cloud-alibaba-03-nacos/aa363b008c3cfd8a7852cc0805dcf803_MD5.jpg" width="100%" align="left" alt="Nginx反向代理架构示意图" />

需要注意的是（[链接](https://www.nacos.io/zh-cn/docs/next/v2/upgrading/2.0.0-compatibility/))：

- Nginx请求时，需要配置成TCP转发，不能配置http2转发，否则连接会被nginx断开。
- 9849和7848端口为服务端之间的通信端口，请勿暴露到外部网络环境和客户端测。

安装的Nginx必须带有stream模块，检查命令时

```
nginx -V  #输出中药包含 --with-stream

# 输出示例(包含stream模块):
nginx version: nginx/1.18.0
built by gcc 4.8.5 20150623 (Red Hat 4.8.5-44) (GCC)
built with OpenSSL 1.0.2k-fips
TLS SNI support enabled
configure arguments: --with-stream ...
```

在Linux安装Nginx的命令如下

```bash
#安装依赖包
yum -y install gcc gcc-c++ autoconf automake
yum -y install zlib zlib-devel openssl openssl-devel pcre-devel

# 下载nginx
wget https://nginx.org/download/nginx-1.18.0.tar.gz
tar -zxvf nginx-1.18.0.tar.gz
cd nginx-1.18.0

#编译nginx  如果使用 nginx 的 stream 功能，在编译时一定要加上 “--with-stream”
./configure --with-stream
make && make install
#安装后nginx默认路径/usr/local/nginx
```

如果安装过程中依赖包下载失败，需要更换yum源，例如

```text
Error downloading packages:  
libcom_err-devel-1.42.9-19.el7.x86_64: [Errno 256] No more mirrors to try.  
pcre-devel-8.32-17.el7.x86_64: [Errno 256] No more mirrors to try.  
libverto-devel-0.2.5-4.el7.x86_64: [Errno 256] No more mirrors to try.  
keyutils-libs-devel-1.5.8-3.el7.x86_64: [Errno 256] No more mirrors to try.  
libsepol-devel-2.5-10.el7.x86_64: [Errno 256] No more mirrors to try.  
libselinux-devel-2.5-15.el7.x86_64: [Errno 256] No more mirrors to try.
```

则需要更换镜像源，使用阿里镜像，方法是编辑`/etc/yum.repos.d/CentOS-Base.repo`文件修改`bashurl`，并且确保`mirrorlist`行被注释掉。

```
baseurl=http://mirrors.aliyun.com/centos/7/os/x86_64/
```

#### (3) 配置Nginx的HTTP负载均衡

首先让Niginx能够转发HTTP请求到它所代理的Nacos节点

进入`/usr/local/nginx`目录，编辑配置文件

```bash
vim conf/nginx.conf
```

填入如下配置

```text
http {
    # nacos服务器http相关地址和端口
    upstream nacos-server {
        server 192.168.65.207:8848;
        server 192.168.65.208:8848;
        server 192.168.65.213:8848;
    }
    server {
        listen 8848;
        location / {
            proxy_pass http://nacos-server/;
        }
    }
}
```

#### (4) 配置`Nginx`的`TCP/gRPC`负载均衡

为了支持`Nacos`的`gRPC`通信和TCP长连接，还需要添`Nginx`的`stream`模块配置

<img src="/imgs/spring-cloud-alibaba-03-nacos/03a0ec097c3d4c4a91a281d83c06e6bc_MD5.jpg" width="100%" align="left" alt="Nginx的TCP/gRPC负载均衡配置示意图" />

同样是修改`nginx.conf`，为stream设置如下配置

```text
# nacos服务器grpc相关地址和端口，需要nginx已经有stream模块
# stream块用于做TCP转发
stream {
    upstream nacos-server-grpc {
        server 192.168.65.207:9848;
        server 192.168.65.208:9848;
        server 192.168.65.213:9848;
    }
    server {
        listen 9848;
        proxy_pass nacos-server-grpc;
    }
}
```

注意前提条件：`Nginx`安装时需要把stream模块安装上，使用`nginx -V | grep stream`命令可以检查

#### (5) 微服务连接`Nacos`的`Nginx`代理

<img src="/imgs/spring-cloud-alibaba-03-nacos/40d7389ff7941c923756a82bb85b1a19_MD5.jpg" width="100%" align="left" alt="微服务连接Nginx代理示意图" />

在微服务的`application.yml`中修改Nacos地址为`Nginx`的地址，以`tlmall-order`为例，配置如下

```yml
spring:  
  application:  
    name: tlmall-order  
  cloud:  
    nacos:  
      discovery:  
        # (1) 基础实验环境配置  
        # server-addr: tlmall-nacos-server:8848  
        username: nacos  
        password: nacos  
        # (6) 指定Nacos集群地址（Nacos集群实验）  
        # 方案1：直连多节点
        # 优势：无额外单点，客户端自动故障转移
        # 劣势：硬编码IP地址不便于运维
        server-addr: 192.168.65.207:8848,192.168.65.208:8848,192.168.65.213:8848  
        # 方案2：Nginx反向代理
        # 优势：配置集中管理，简化运维
        # 劣势：Nginx成为单点
        server-addr: nacos.tlmall.com:8848
```

完整代码：

- <https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-order/src/main/resources/application.yml>
- <https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-nacos-demo-user/src/main/resources/application.yml>

修改之后重启微服务，登录被Nginx反向代理的Nacos控制台，就可以找到已经注册成功的微服务了。

## 6. 总结

### 6.1 核心知识架构

本文档系统介绍了 Nacos 注册中心的实战应用，涵盖从设计原理到生产部署的完整路径：

```
设计思路 → 核心概念 → 常用配置 → 安全配置 → 集群部署
```

### 6.2 注册中心设计演进

**微服务核心挑战**：

- 服务发现：动态维护服务实例网络位置，支持负载均衡和配置集中化
- 状态感知：健康检查、故障隔离、自动恢复

**演进历程**（以会员服务调用订单服务为例）：

1. **硬编码地址** → 运维成本高、配置分散
2. **注册表集中配置** → 无自动感知能力、人工介入频繁
3. **Nginx 健康检查** → 仍需维护庞大实例列表
4. **专用注册中心** → 实例主动上报 + 心跳机制 + 自动剔除

## **注册中心核心机制**：

- 客户端：定时拉取实例列表，本地缓存 + 负载均衡
- 服务实例：主动注册/注销，网络恢复后指数退避重试
- 注册中心：分级健康扫描（5秒心跳、15秒警示、30秒剔除）

### 6.3 Nacos 核心概念

**数据模型层次**（从粗到细）：

```
Namespace（命名空间） → Group（分组） → Service（服务） → Cluster（集群） → Instance（实例）
```

**关键概念**：

- **命名空间**：租户粒度环境隔离（开发/测试/生产），跨命名空间服务互不可见
- **分组**：命名空间下次级划分，不同分组服务可跨组访问（需指定分组名）
- **集群**：按地域/机房对实例分组，支持同机房优先路由
- **实例**：服务的具体运行进程，包含丰富的元数据信息

**元数据应用**：

- 服务元数据：协议类型、路由规则（静态全局）
- 集群元数据：机房信息、流量类型（区域共享）
- 实例元数据：版本、权重、灰度标识（动态独立）

**实例生命周期**：

1. 启动注册 → 心跳维持（5秒/次） → 健康检查（15秒不健康、30秒剔除临时实例） → 定期发现

**实例类型对比**：

| 特性   | 临时实例               | 持久实例            |
| ---- | ------------------ | --------------- |
| 健康检查 | 客户端上报心跳            | 服务端主动探测         |
| 数据存储 | 内存                 | 磁盘持久化           |
| 下线方式 | 自动剔除               | 手动删除            |
| 一致性  | AP（Distro协议，最终一致性） | CP（Raft协议，强一致性） |
| 使用场景 | 业务微服务              | 基础设施服务          |

### 6.4 实战配置要点

**服务隔离实验**：

- 订单服务注册到 `dev` 命名空间后，用户服务（在 `public` 命名空间）无法发现并调用订单服务
- 证明 Namespace 是框架层面的隔离约束

**集群配置**：

```yaml
spring.cloud.nacos.discovery.cluster-name: sh-cluster
```

**元数据配置**：

```yaml
spring.cloud.nacos.discovery.metadata:
  version: "v2.1.0"
  region: "cn-shanghai"
  weight: "100"
```

**持久实例开启**：

```yaml
spring.cloud.nacos.discovery.ephemeral: false  # 2.x版本提升到服务级别
```

### 6.5 安全配置体系

**安全威胁**：服务信息暴露、配置泄露、恶意注册、元数据篡改

**两套认证机制**：

1. **微服务/用户访问**：用户名 + 密码 → Access Token
2. **Nacos 节点间通信**：AK/SK（server.identity.key/value）

**权限管理模型**：`用户 ↔️ 角色 ↔️ 权限`（命名空间级读写权限）

**核心配置**：

```properties
# 开启认证
nacos.core.auth.enabled=true
# 内部通信白名单
nacos.core.auth.server.identity.key=authKey
nacos.core.auth.server.identity.value=nacosSecurty
# 微服务访问密钥（Base64编码，原始密钥≥32字符）
nacos.core.auth.plugin.nacos.token.secret.key=<Base64密钥>
```

### 6.6 生产集群部署

**推荐架构**：`Nginx + Nacos集群(3节点) + MySQL主备`

**端口规划**（以主端口 8848 为例）：

| 端口   | 计算    | 作用                    | 对外开放  |
| ---- | ----- | --------------------- | ----- |
| 8848 | -     | HTTP API/控制台          | 是     |
| 9848 | +1000 | gRPC客户端请求             | 是     |
| 9849 | +1001 | gRPC服务端同步             | 否（内部） |
| 7848 | -1000 | Raft协议（集群协调/Leader选举） | 否（内部） |

**关键配置**：

1. **cluster.conf**：配置所有节点 IP:端口（勿使用 127.0.0.1）
2. **application.properties**：
   - 认证配置（所有节点完全相同）
   - MySQL 数据源连接
3. **Nginx 反向代理**：
   - HTTP 负载均衡（8848端口）
   - stream 模块 TCP/gRPC 负载均衡（9848端口）

**环境要求**：

- JDK 8+（推荐使用 Oracle JDK，OpenJDK 在 2.3.2 版本开启鉴权时存在兼容性问题）
- MySQL 5.7+（建议 8.0+）
- 防火墙开通全部 4 个端口

**微服务连接方式**：

```yaml
# 方案一：直连多节点
# 优势：无额外单点，客户端自动故障转移
# 劣势：不便于管理配置和运维，不推荐配置IP
server-addr: 192.168.65.207:8848,192.168.65.208:8848,192.168.65.213:8848

# 方案二：通过 Nginx 解耦
# 优势：配置集中管理，便于运维
# 劣势：引入新的单点
server-addr: nacos.tlmall.com:8848
```

### 6.7 核心价值

Nacos 注册中心的强大之处在于：

- **清晰的架构蓝图**：命名空间、分组、服务、集群、实例五层模型
- **高效的运行引擎**：注册、心跳、发现的自动化机制
- **完善的隔离策略**：逻辑隔离与动态生命周期的完美结合
- **生产级可靠性**：集群模式 + 安全认证 + 数据持久化

这种设计既保障了微服务架构的灵活性，又提供了企业级所需的稳定性和安全性。

