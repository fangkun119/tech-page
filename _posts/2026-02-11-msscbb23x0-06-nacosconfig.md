---
title: Spring Cloud Alibaba 06：Nacos配置中心
author: fangkun119
date: 2026-02-11 12:00:00 +0800
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


## 1. 配置中心介绍

### 1.1 内容概括

前三篇文章分别介绍了 Spring Cloud Alibaba 的三个核心组件：服务注册与发现（Nacos 注册中心）、服务调用组件（OpenFeign）以及负载均衡器（Spring Cloud LoadBalancer）。这三个组件共同构成了微服务开发的技术基石。

在此基础上，本文将介绍其他常用组件。这些组件可根据项目实际需求选择性学习和应用，为微服务架构提供更完善的技术支撑。

本文重点介绍 Nacos 配置中心的实战应用，主要内容包括：

- **快速入门**：配置中心搭建及基本使用方法
- **配置隔离**：通过 Profile 实现多环境配置管理
- **动态刷新**：配置热加载机制及常见问题处理
- **插件扩展**：配置加密及自定义插件实现方法

**前置知识**：建议先阅读 **《Spring Cloud Alibaba 02：完整 Demo 搭建》**，建立对 Spring Cloud Alibaba 的整体认知。本文聚焦 Nacos 配置中心的实战应用，对基础知识将不做过多展开。

**配套代码**：[spring-cloud-alibaba-2023-nacos](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos)

**本文涉及模块**：[microservices/tlmall-config-demo-order](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-config-demo-order)

### 1.2 作用和价值

在微服务架构中，随着服务拆分粒度的不断细化，服务数量持续增长，配置管理的复杂度呈指数级上升。如果配置文件仍然由各个服务独立维护，整个系统将变得难以管理。

例如，当公共配置（如数据库连接、注册中心地址）需要修改时：

- 所有使用它的微服务都必须同步修改。
- 当微服务数量达到数百个时，这种维护方式显然不可行。

配置中心正是为解决这一问题而生的统一配置管理服务，它通过以下核心能力让分布式系统更加稳定可靠：

| 核心能力       | 说明                                                                           |
| ---------- | ---------------------------------------------------------------------------- |
| **集中化管理**  | 将分散在各个微服务中的配置统一纳管，特别是公共配置（如注册中心地址、数据库连接信息等），实现一处修改、全局生效，极大降低了运维成本            |
| **动态更新能力** | 支持配置的实时推送机制。当配置发生变更时，主动通知所有订阅该配置的微服务，客户端通过监听机制自动获取最新配置，整个过程无需重启服务，实现真正的配置热更新 |

配置中心的运行机制如下图所示，由Nacos控制台和集成在微服务中的Nacos Lib两部分组成：

<img src="imgs/spring-cloud-alibaba-06-config/8420e7fa747ccc4fd36fe879138cf7ae_MD5.jpg" style="display: block; width: 600px;" alt="配置中心运行机制示意图">

Nacos控制台提供三类核心端口，各司其职：

| 端口类型       | 用途说明                              |
| ---------- | --------------------------------- |
| **控制台端口**  | 供浏览器UI界面访问，用于配置的展示、修改和发布          |
| **监听端口**   | 供微服务中的Nacos Client监听配置变更事件，实现实时推送 |
| **配置拉取端口** | 供微服务中的Nacos Client主动拉取配置数据        |

### 1.3 新旧配置方式

Spring Boot 2.4引入了全新的配置方式，支持云原生，显著简化了配置管理，官方强烈推荐使用。

| 配置方式                  | 配置文件                                  | 特点                       | 复杂度 |
| --------------------- | ------------------------------------- | ------------------------ | --- |
| **Spring Boot 2.4之前** | `application.yaml` + `bootstrap.yaml` | 需要维护两个配置文件              | 较繁琐 |
| **Spring Boot 2.4+**  | 仅需 `application.yaml`                 | 使用`spring.config.import`导入 | 简化  |

**配置示例：**

```yaml
spring:
  application:
    name: tlmall-config-demo-order
  config:
    import:
      - optional:nacos:${spring.application.name}.yml  # 微服务应用配置
      - optional:nacos:db-common.yml                    # 数据库公共配置
      - nacos:nacos-discovery.yml                       # Nacos注册中心配置
```

**配置说明：**

- `nacos`：从Nacos配置中心载入配置
- `optional`：配置文件不存在时不阻止应用启动
- `${spring.application.name}`：应用名称占位符，此处为`tlmall-config-demo-order`

**配置优先级：**

当多个配置文件中存在相同配置项时，后加载的配置会覆盖先前的配置。相比bootstrap方式的复杂优先级规则，import方式的配置覆盖逻辑简单直观。

## 2. 快速入门

### 2.1 环境介绍

本文使用[spring-cloud-alibaba-2023-nacos](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos)中的[microservices/tlmall-config-demo-order](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-config-demo-order)模块进行演示，环境由三部分组成：

- 基础环境及中间件：域名配置，MySQL，Nacos
- 微服务应用：用IDEA启动[microservices/tlmall-config-demo-order](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-config-demo-order)即可
- PostMan：[postman-workspace](https://github.com/fangkun119/postman-workspace)

### 2.2 环境搭建

#### (1) 基础环境及中间件

复用先前的环境即可。搭建过程参考《Spring Cloud Alibaba 02：完整Demo搭建》中`3.3`到`3.5`这三个小节。

```text
3.3 域名配置
3.4 MySQL
3.5 Nacos
```

演示代码Repo如下，`git clone`下来分别导入到IDEA和Postman

- <https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos>
- <https://github.com/fangkun119/postman-workspace>

如果不想启动一整套数个中间件，可以用下面的命令只启动 MySQL 和 Nacos

```bash
_______________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/Code/mid-wares/nacos/
$ brew services start mysql
==> Successfully started `mysql` (label: homebrew.mxcl.mysql)
_______________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/Code/mid-wares/nacos/
$ bash bin/startup.sh
/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java   -Xms512m -Xmx512m -Xmn256m -Dnacos.standalone=true -Dnacos.member.list= -Xlog:gc*:file=/Users/ken/Code/mid-wares/nacos/logs/nacos_gc.log:time,tags:filecount=10,filesize=100m -Dloader.path=/Users/ken/Code/mid-wares/nacos/plugins,/Users/ken/Code/mid-wares/nacos/plugins/health,/Users/ken/Code/mid-wares/nacos/plugins/cmdb,/Users/ken/Code/mid-wares/nacos/plugins/selector -Dnacos.home=/Users/ken/Code/mid-wares/nacos -jar /Users/ken/Code/mid-wares/nacos/target/nacos-server.jar  --spring.config.additional-location=file:/Users/ken/Code/mid-wares/nacos/conf/ --logging.config=/Users/ken/Code/mid-wares/nacos/conf/nacos-logback.xml --server.max-http-header-size=524288
nacos is starting with standalone
nacos is starting. you can check the /Users/ken/Code/mid-wares/nacos/logs/start.out
```

关闭Nacos和MySQL的命令如下

```bash
_______________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/Code/mid-wares/nacos/
$ bash bin/shutdown.sh
The nacosServer(41623
41892
42897) is running...
Send shutdown request to nacosServer(41623
41892
42898) OK
_______________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/Code/mid-wares/nacos/
$ brew services stop mysql
Stopping `mysql`... (might take a while)
==> Successfully stopped `mysql` (label: homebrew.mxcl.mysql)
_______________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/Code/mid-wares/nacos/
$  brew services list | grep mysql
mysql   none
_______________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/Code/mid-wares/nacos/
$ jps | grep -i nacos
```

#### (2) 向Nacos添加配置

阅读微服务的本地配置 [/microservices/tlmall-config-demo-order/.../application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/src/main/resources/application.yml)， 看到下面三行`import`配置项，代表着这些配置要从Nacos远程加载。

```yml
spring
  config:
    import:
      - nacos:tlmall-config-demo-order.yml   # 订单服务配置
      - nacos:db-common.yml                  # 数据库公共配置
      - nacos:nacos-discovery.yml            # Nacos注册中心配置
```

因此检查Nacos配置中心，补充缺失的配置（注意勾选YAML）

这些配置内容分别如下

(1) `tlmall-config-demo-order.yml`

```yml
# 数据库连接  
spring:  
  datasource:  
    url: jdbc:mysql://tlmall-mysql:3306/tlmall_order?useSSL=false&characterEncoding=utf8&allowPublicKeyRetrieval=true  
# 热加载配置实验  
order:  
  count: 10
```

(2) `db-common.yml`

```
spring:  
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: root
  main:
    allow-bean-definition-overriding: true
mybatis:
  configuration:
    map-underscore-to-camel-case: true
```

(3) `nacos-discovery.yml` 

```
spring:
  cloud:
    nacos:
      discovery:
        server-addr: tlmall-nacos-server:8848
        username: nacos
        password: nacos
```

具体配置文件见

* [db-common.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/db-common.yml "db-common.yml")
* [nacos-discovery.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/nacos-discovery.yml "nacos-discovery.yml")
* [tlmall-config-demo-order.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/tlmall-config-demo-order.yml "tlmall-config-demo-order.yml")

上传好之后可以在Nacos配置中心找到这三份文件（对应三个同名的Data Id）

<img src="imgs/spring-cloud-alibaba-06-config/8cf49e54acfa4c3df5315073f9dfb780_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置中心配置文件列表">

#### (3) 启动并验证微服务

启动微服务，因为在`application.yml`中配了`logging.level.com.alibaba.cloud.nacos=debug`，启动日志中可以看到从Nacos中加载的远程配置。

<img src="imgs/spring-cloud-alibaba-06-config/233442d517a7eb1a75ed9d964d74bc30_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置加载日志">
用`spring_cloud_alibaba_nacos_config`这个Collection中的请求进行测试，成功界面如下

<img src="imgs/spring-cloud-alibaba-06-config/79e40922b21305788ea845dba8990e3a_MD5.jpg" style="display: block; width: 100%;" alt="测试成功界面">

### 2.3 微服务如何集成到Nacos配置中心

上一小节中的“[microservices/tlmall-config-demo-order](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/tree/main/microservices/tlmall-config-demo-order)”是已经集成到Nacos配置中心的微服务，但是并须要介绍它是如何集成的配置中心的。

本节介绍如何把一个微服务集成到Nacos配置中，具体步骤如下：

#### (1) 引入依赖

在微服务的`pom.xml`文件中引入Nacos配置中心依赖。

```xml
<!-- nacos-config 配置中心依赖 -->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

代码：[microservices/tlmall-config-demo-order/pom.xml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/pom.xml)

#### (2) 配置剥离

将微服务的配置按功能模块拆分，每个拆分出的配置文件对应Nacos中的一个Data ID。以订单服务为例，我们将其配置拆分为三部分：

| Data ID                                                                                                                                                                                                                        | 用途说明                      |
| ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | ------------------------- |
| [db-common.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/db-common.yml "db-common.yml")                                              | 数据库公共配置：用户名、密码、MyBatis配置等 |
| [nacos-discovery.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/nacos-discovery.yml "nacos-discovery.yml")                            | Nacos注册中心公共配置             |
| [tlmall-config-demo-order.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/tlmall-config-demo-order.yml "tlmall-config-demo-order.yml") | 订单服务专属配置                  |

完成配置拆分后，[application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/src/main/resources/application.yml "application.yml")中仅需保留Nacos配置中心的连接信息和配置导入声明：

```yml
server:  
  port: 8060  
spring:  
  application:  
    name: tlmall-config-demo-order  
  cloud:  
    nacos:  
      config:  
        server-addr: tlmall-nacos-server:8848
        username: nacos
        password: nacos
        namespace: public # Nacos命名空间
  config:  
    import:  
      - optional:nacos:tlmall-config-demo-order.yml&group=DEFAULT_GROUP
      - nacos:db-common.yml       # 数据库公共配置  
      - nacos:nacos-discovery.yml # Nacos注册中心配置
```

重要说明：

| 配置要点                 | 说明                                                             |
| -------------------- | -------------------------------------------------------------- |
| **Spring Boot 2.4+** | 使用`spring.config.import`方式导入配置，无需再使用bootstrap配置文件              |
| **配置依赖性**            | 示例中未使用`optional`前缀，意味着如果Nacos中不存在对应配置，应用将启动失败，这样可确保配置完整性       |

如果想在日志中看到微服务从Nacos加载的配置内容，可以使用如下配置，适合开发环境调试。

```yml
# 打印从Nacos载入的配置，适合开发环境调试和验证程序
logging:
  level:
    com.alibaba.nacos.config: debug
```

#### (3) 上传配置到Nacos

将拆分出的三份配置文件上传到Nacos配置中心。上传时需要注意 Namespace、Group、Data Id必须本地`application.yml`中一致。

<img src="imgs/spring-cloud-alibaba-06-config/8cf49e54acfa4c3df5315073f9dfb780_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置中心配置文件列表">

由于示例配置中未指定`Group`，系统将使用默认值：Group为`DEFAULT_GROUP`，Namespace为`public`。配置映射关系如下：

| 配置文件                                | Data ID（必须完全一致）                                                                                                                                                                                                               | Group         | Namespace |
| ------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- | --------- |
| `nacos:db-common.yml`                | [db-common.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/db-common.yml "db-common.yml")                                              | DEFAULT_GROUP | public    |
| `nacos:nacos-discovery.yml`          | [nacos-discovery.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/nacos-discovery.yml "nacos-discovery.yml")                            | DEFAULT_GROUP | public    |
| `nacos:tlmall-config-demo-order.yml` | [tlmall-config-demo-order.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/tlmall-config-demo-order.yml "tlmall-config-demo-order.yml") | DEFAULT_GROUP | public    |

完成配置上传后，即可启动微服务进行验证。

如果需要显式指定`Group`，可以在`import`配置中通过URL参数的方式指定：

```yaml
spring:
  config:
    import:
      # 使用URL参数显式指定Group和Namespace
      - optional:nacos:tlmall-config-demo-order.yml?group=DEFAULT_GROUP
      - nacos:db-common.yml
      - nacos:nacos-discovery.yml
```

参数说明：

| 参数          | 说明                                      | 默认值             |
| ----------- | --------------------------------------- | --------------- |
| `group`     | 指定配置所属的分组                               | `DEFAULT_GROUP` |
| `optional`  | 前缀表示配置文件不存在时不阻止应用启动（不使用此前缀时配置缺失会导致启动失败） | 无（必须存在）         |

## 3. 配置分组级别

### 3.1 Namespace级别

#### (1) 介绍

Namespace（命名空间）是Nacos实现租户级别配置隔离的核心机制。

通过Namespace，可以在同一Nacos实例中为不同团队、项目或环境创建完全隔离的配置区域，实现更高维度的配置管理。

**核心特性**：Namespace提供**完全隔离能力**，这与Group和Profile形成本质区别。不同Namespace下的微服务具有以下隔离特性：

- **配置隔离**：各Namespace的配置完全独立，互不影响
- **服务隔离**：服务之间**默认**无法通过Nacos服务发现相互访问（但可通过直接URL进行跨namespace调用）
- **独立视图**：每个Namespace拥有独立的配置视图和服务注册表

这种完全隔离的特性，使Namespace非常适合需要严格隔离的多租户场景。

#### (2) 应用场景

| 场景类型 | 使用场景 | 说明示例 |
|---------|---------|---------|
| **多租户隔离** | 不同团队或项目使用独立的命名空间 | 避免配置冲突，实现租户级别的资源隔离 |
| **环境隔离** | 开发、测试、生产环境使用不同的命名空间 | 确保生产环境配置不被测试环境误操作影响 |
| **多版本部署** | 同一服务的不同版本或部署实例隔离 | 支持灰度发布、A/B测试等场景 |

#### (3) 配置方法

在`application.yml`中通过`namespace`参数指定命名空间ID：

```yaml
spring:
  application:
    name: tlmall-config-demo-order
  config:
    import:
      - optional:nacos:tlmall-config-demo-order.yml
  cloud:
    nacos:
      config:
        # namespace: "" # 使用默认命名空间public
        # namespace: a1b2c3d4-e5f6-7890-abcd-ef1234567890  # 创建时自动生成的ID
        namespace: my_namespace_id # 创建命名空间时手动填入的ID
        server-addr: localhost:8848
```


注意事项

| 配置要点         | 说明                                                      |
| ------------ | ------------------------------------------------------- |
| **ID vs 名称** | `namespace`参数需填写**命名空间ID**而非名称。其中的public命名空间ID为空字符串`""` |
| **ID获取方式**   | 在Nacos控制台命名空间列表中复制                                      |
| **默认行为**     | 未明确指定`namespace`时，默认使用public命名空间                        |

#### (4) ID创建：自动 v.s 手动

在创建命名空间时，可以选择**自动生成**或**手动指定**命名空间ID。

<img src="imgs/spring-cloud-alibaba-06-config/cf9d5e0e4b7507b8a1bdeb8cc61d80a0_MD5.jpg" style="display: block; width: 100%;" alt="Nacos命名空间创建界面">

| 创建方式      | 操作方法                      | ID示例                                   |
| --------- | ------------------------- | -------------------------------------- |
| 自动生成UUID  | 不填写"命名空间ID"字段，系统自动生成      | `a1b2c3d4-e5f6-7890-abcd-ef1234567890` |
| 手动指定自定义ID | 手动填写"命名空间ID"字段，推荐使用有意义的标识 | `dev`、`test`、`prod`                    |

**为何推荐自动生成UUID**

命名空间ID创建后不可修改，而使用自动生成UUID具有以下优缺点，通常在生产环境，更加推荐使用自动生成UUID

| 特点   | 说明                       |
| ---- | ------------------------ |
| 唯一性  | ✅ UUID算法保证全球唯一性，无需担心ID重复 |
| 管理成本 | ✅ 系统自动生成，省去维护ID唯一性的开销    |
| 可读性  | ❌ 可读性较差，难以记忆             |


**何时选择手动指定命名空间ID**

在小型项目、多环境快速切换、或配置文件可读性优先的场景下，手动指定语义化ID（如`dev`、`prod`）会更加便捷。但这意味着需要建立统一的命名规范、并严格遵循。

| 特点   | 说明              |
| ---- | --------------- |
| 唯一性  | ⚠️ 需手动保证，存在冲突风险 |
| 可读性  | ✅ 语义化ID，配置文件易读  |
| 管理成本 | ❌ 需维护ID唯一性      |

#### (5) 配置克隆

Nacos提供了跨命名空间的配置克隆功能，可快速将配置从一个命名空间复制到另一个命名空间，简化多环境配置迁移工作。

**应用场景**：该功能特别适合创建新环境配置。例如，从测试环境克隆配置到自己的专属开发环境，建立配置基准后再按需调整，避免了从头创建的繁琐过程。

<img src="imgs/spring-cloud-alibaba-06-config/c61b482249589f7ab1b217e2ef36bcec_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置克隆功能界面">

### 3.2 Group级别

#### (1) 介绍

Group（分组）是Nacos配置中心的逻辑隔离机制，提供比Namespace更细粒度的配置分组能力。与Namespace的**完全隔离**不同，Group实现**逻辑分组**，适合在同一命名空间内进行配置的组织管理。

**核心特性**：配置通过Data ID + Group共同确定配置唯一性，实现配置的逻辑隔离和灵活管理。

#### (2) 应用场景

| 场景类型 | 使用场景                      | 说明示例                                    |
| ---- | ------------------------- | --------------------------------------- |
| 组件隔离 | 同一微服务内的不同中间件配置分组管理        | Redis配置使用`redis-group`，MQ配置使用`mq-group` |
| 项目隔离 | 不同项目或业务线使用不同的Group，避免配置冲突 | 电商平台使用`e-commerce`组，物流系统使用`logistics`组  |

#### (3) 配置方法

在`application.yml`中通过`group`参数指定分组名称：

```yaml
spring:
  cloud:
    nacos:
      config:
        group: e-commerce  # 指定分组名称
        server-addr: localhost:8848
```

配置要点：

| 配置要点     | 说明                              |
| -------- | ------------------------------- |
| 默认值  | 未指定`group`时，默认使用`DEFAULT_GROUP` |
| 唯一性  | Group必须在同一Namespace内唯一          |
| 配置标识 | Data ID和Group共同决定配置的唯一性         |

### 3.3 Profile级别

#### (1) 介绍

Profile（环境配置）是Spring Boot提供的多环境配置管理机制，实现同一套代码适配不同运行环境。

**核心机制**：通过`spring.profiles.active`激活指定环境，Spring Boot自动加载对应的Profile配置文件。Profile实现**环境级别配置隔离**，与Namespace（租户级）和Group（逻辑组级）形成互补。

配置示例：

```yaml
spring:
  application:
    name: tlmall-config-demo-order
  profiles:
    active: dev  # 激活开发环境
  config:
    import:
      - optional:nacos:${spring.application.name}-${spring.profiles.active}.yml
```

**多种激活方式**：除配置文件外，还可通过JVM参数、环境变量等方式动态指定Profile，无需修改代码即可灵活切换环境。

#### (2) 应用场景

Profile 尤其适用于**同一环境内的不同运行模式切换**，下面是一个典型的例子。

| 模式类型 | 使用场景 | 典型配置差异 |
| --- | --- | --- |
| **本地开发** (local) | 开发人员本地调试 | H2内存库、详细日志、热加载 |
| **单元测试** (unittest) | 自动化测试运行 | Mock依赖、快速启动、专用配置 |
| **演示模式** (demo) | 产品演示展示 | 预置数据、功能限制、精简UI |
| **调试模式** (debug) | 问题排查分析 | 调试端点、SQL日志、性能追踪 |

#### (3) 配置方法

首先上传一个profile配置文件，例如`tlmall-config-demo-order-mvp.yml`，使用`mvp`作为profile标识。

内容和之前一样，见[microservices/tlmall-config-demo-order/nacos_remote_config/tlmall-config-demo-order-mvp.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos/blob/main/microservices/tlmall-config-demo-order/nacos_remote_config/tlmall-config-demo-order-mvp.yml)

把它上传到Nacos

<img src="imgs/spring-cloud-alibaba-06-config/84fc1724ec66c208bdb5800b6439d884_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置上传">

接下来激活指定环境，按如下方式在`application.yaml`中配置：

```yaml
spring:  
  profiles:  
    active: mvp  
  config:  
    import:  
      # 根据profile来选择远程配置
      - optional:nacos:${spring.application.name}-${spring.profiles.active}.yml
      # 公用配置不变
      - nacos:db-common.yml 
      - nacos:nacos-discovery.yml
```


除了例子中的方法，还有两种其它方法激活profile，汇总如下

| 激活方式      | 配置位置              | 示例                              | 适用场景         |
| --------- | ----------------- | ------------------------------- | ------------ |
| **配置文件**  | `application.yml` | `spring.profiles.active: dev`   | 默认环境配置       |
| **JVM参数** | 启动命令              | `-Dspring.profiles.active=prod` | 生产部署脚本       |
| **环境变量**  | 操作系统环境变量          | `SPRING_PROFILES_ACTIVE=prod`   | Docker/K8s部署 |

### 3.4 配置优先级机制应用

#### (1) 介绍

配置优先级机制是`spring.config.import`方式的核心特性。**规则非常明确**：按照配置加载的先后顺序，后加载的配置会覆盖先前的配置。

这种机制相比bootstrap方式的复杂优先级规则，配置覆盖逻辑简单直观，易于理解和维护。

#### (2) 应用场景

| 场景类型 | 使用场景 | 典型配置顺序 |
|---------|---------|-------------|
| **分层配置** | 基础配置 → 环境配置 → 服务配置 | base < dev < service |
| **环境覆盖** | 开发环境覆盖测试环境，测试环境覆盖生产环境 | prod < test < dev |
| **临时覆盖** | Demo演示、调试等临时场景，只需覆盖部分配置 | base < dev < demo |

**场景示例：Demo演示配置**

在开发环境中进行产品演示时，需要使用特定的配置（如预置数据、功能限制等），但又不希望重新创建完整的配置文件。

通过配置优先级机制，只需创建一个包含差异配置的`demo-config.yml`，将其设置为最高优先级，即可覆盖基础配置和开发环境配置中的同名参数。

#### (3) 配置方法

在`application.yml`中，通过`import`列表的顺序控制优先级，并可结合Group实现配置的清晰管理：

```yaml
spring:
  config:
    import:
      # 低优先级：基础公共配置
      - nacos:base-config.yml?group=common
      # 中优先级：开发环境配置
      - nacos:dev-config.yml?group=common
      # 高优先级：Demo演示配置
      - nacos:demo-config.yml?group=xxx_mvp
```

配置要点：

| 配置要点 | 说明 |
|---------|------|
| **顺序决定优先级** | 列表中越靠后的配置，优先级越高 |
| **支持跨Group** | 可以从不同Group加载配置，实现灵活组合 |
| **简化规则** | 相比bootstrap方式，无需记忆复杂优先级规则 |

## 4. 配置动态刷新

### 4.1 默认刷新的流程和问题

#### (1) 刷新流程

Nacos配置中心原生支持配置动态刷新，配置变更时会自动推送到订阅的微服务。

```mermaid
flowchart LR
    A[Nacos托管</br>配置修改] --> B[Nacos发送</br>变更通知]
    B --> C[微服务</br>收到通知]
    C --> D[拉取最新</br>配置数据]
    D --> E[更新到</br>Environment]
    E -.-> F[Bean仍用</br>旧配置]
```

#### (2) 核心问题

配置虽然更新到了Environment，但**已经创建的Bean仍使用旧值**。

原因：Bean在创建时完成属性注入，单例Bean实例化后配置值就固定了，不会自动响应Environment的变化。

#### (3) 问题复现

将OrderController中的`@RefreshScope`注释掉，重启服务后修改Nacos配置，观察Bean是否同步更新。

**测试代码**：

```java
@RestController
@RequestMapping("/config-demo")
@Slf4j
// @RefreshScope // 注释掉这个注解，动态刷新将被关闭，Bean将无法读取更新后的配置
public class OrderController {
    @Value("${order.count}")
    @Getter
    private int count;

    @PostMapping("/orders")
    public Result<?> addOrder(@RequestBody OrderDTO orderDTO) {
        // 对比Environment和Bean中的配置值
        log.info("count from env: {}", StaticContextHolder.getEnvironmentProperty("order.count"));
        log.info("count from bean: {}", this.getCount());
        return Result.success(orderDTO);
    }
}
```

**验证步骤**：

步骤1：注释掉`@RefreshScope`，重启服务

步骤2：在Nacos修改`order.count`从10改为11

<img src="imgs/spring-cloud-alibaba-06-config/6673b3da9b96ee7ef9e41503ffa4df87_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置修改">

步骤3：发送POST请求触发配置读取

<img src="imgs/spring-cloud-alibaba-06-config/c394ffe2d7060341ddde7b4d18586207_MD5.jpg" style="display: block; width: 100%;" alt="Postman请求">

步骤4：查看日志输出

<img src="imgs/spring-cloud-alibaba-06-config/1d06e53aedee98b75b56a48a949a8263_MD5.jpg" style="display: block; width: 100%;" alt="日志输出">

验证结果：

- Environment中已更新：`order.count = 11`
- Bean中仍为旧值：`count = 10`

说明默认的Bean不能热加载配置，需要用`@RefreshScope`进行标注

### 4.2 用@RefreshScope实现Bean刷新

#### (1) 原理特性

`@RefreshScope`是Spring Cloud提供的配置动态刷新方案。被标注的Bean在配置变更时自动重新创建，从而获取最新值。

**核心特性**：

| 特性 | 说明 |
|-----|------|
| **独立缓存** | 被标注的Bean放入独立的缓存区域，与普通单例Bean分离 |
| **懒加载** | Bean创建延迟到实际调用时 |
| **自动刷新** | 配置变更时自动清除缓存，下次访问时重新创建Bean |
| **最新注入** | 新创建的Bean从Environment获取最新配置值 |

**应用场景**：

| 场景类型 | 使用场景 | 示例 |
|---------|---------|------|
| **业务配置** | 需要动态调整的业务参数、阈值 | 订单数量限制、超时时间 |
| **开关控制** | 功能开关、灰度发布 | 新功能开关、AB测试配置 |
| **连接参数** | 数据库连接、第三方接口参数 | 超时时间、重试次数 |

#### (2) 使用演示

**步骤一：添加@RefreshScope注解**

给OrderController添加`@RefreshScope`注解：

```java
@RestController
@RequestMapping("/config-demo")
@Slf4j
@RefreshScope // 开启动态刷新
public class OrderController {
    @Value("${order.count}")
    @Getter
    private int count;

    @PostMapping("/orders")
    public Result<?> addOrder(@RequestBody OrderDTO orderDTO) {
        // 对比Environment和Bean中的配置值
        log.info("count from env: {}", StaticContextHolder.getEnvironmentProperty("order.count"));
        log.info("count from bean: {}", this.getCount());
        return Result.success(orderDTO);
    }
}
```

在`TlmallOrderConfigDemoApplication`中添加监控代码，用于观测远程配置同步到Environment的过程：

```java
@SpringBootApplication  
@EnableDiscoveryClient  
@Slf4j  
@EnableScheduling   // 开启定时任务功能  
public class TlmallOrderConfigDemoApplication {  
    public static void main(String[] args) {  
        ApplicationContext applicationContext = SpringApplication.run(TlmallOrderConfigDemoApplication.class, args);  
  
        while (true) {  
            // 配置动态刷新实验，打印容器环境中的配置值，观察Nacos远程配置是否同步到容器环境
            // printConfigPropertyFromContextEnv("order.count");  
  
            // 每隔10秒执行一次  
            sleepSeconds(10);  
        }  
    }  
  
    private static void printConfigPropertyFromContextEnv(String property) {  
        String orderCount = StaticContextHolder.getEnvironmentProperty("order.count");  
        log.info("order count from environment: {}", orderCount);  
    }
    
    // …… 其它代码   
}
```

**步骤二：配置动态刷新**

重启服务后，在Nacos修改`order.count`从11改为12：

<img src="imgs/spring-cloud-alibaba-06-config/422663ed4282be7f01a27782e69d09c7_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置修改">

从日志可以看到配置推送和加载过程：

```text
order count from environment: 11
order count from environment: 11
order count from environment: 11
INFO 4501 --- [tlmall-config-demo-order] [acos-server-106] com.alibaba.nacos.common.remote.client   : [bde97932-74f0-4c68-8d34-95b732a44f5d_config-0] Receive server push request, request = ConfigChangeNotifyRequest, requestId = 191
……
INFO 4501 --- [tlmall-config-demo-order] [listener.task-0] c.a.c.n.c.NacosConfigDataLoader          : [Nacos Config] Load config[dataId=tlmall-config-demo-order.yml, group=DEFAULT_GROUP] success
DEBUG 4501 --- [tlmall-config-demo-order] [listener.task-0] c.a.c.n.c.NacosConfigDataLoader          : [Nacos Config] config[dataId=tlmall-config-demo-order.yml, group=DEFAULT_GROUP] content:
# 数据库连接
spring:
  datasource:
    url: jdbc:mysql://tlmall-mysql:3306/tlmall_order?useSSL=false&characterEncoding=utf8&allowPublicKeyRetrieval=true
# 热加载配置实验
order:
  count: 12
order count from environment: 12
order count from environment: 12
order count from environment: 12
```

**步骤三：发送POST请求验证**

发送POST请求触发Bean访问：

<img src="imgs/spring-cloud-alibaba-06-config/b37dd94a90cdd51ea71a317631315fe8_MD5.jpg" style="display: block; width: 100%;" alt="Postman请求">

从日志可以看到，Environment和Bean中的值都已更新为12：

<img src="imgs/spring-cloud-alibaba-06-config/bc400862014d97de15a4be4059dbd9a8_MD5.jpg" style="display: block; width: 100%;" alt="日志输出">

**验证结果**：配置动态刷新生效。

#### (3) 遗留问题

虽然`@RefreshScope`实现了Bean的动态刷新，但会导致**定时任务失效**。

下一节介绍解决方案。

### 4.3 @RefreshScope导致定时任务失效的解决

#### (1) 问题描述

使用`@RefreshScope`会导致定时任务失效。

**问题现象**：

1. 启动后定时任务正常执行
2. 修改配置中心配置
3. 定时任务停止执行
4. 调用接口后定时任务恢复

**原因分析**：

| 阶段     | 说明                            |
| ------ | ----------------------------- |
| Bean创建 | Bean创建时，定时任务注册到调度器           |
| 配置变更   | 配置变更时，Bean从RefreshScope缓存清除 |
| Bean特性 | 这类Bean采用懒加载机制                   |
| 后果     | Bean不会自动重建，导致定时任务失效               |

需要主动触发Bean重建，定时任务才会重新注册。
#### (2) 问题演示

**步骤一：创建定时任务组件**

创建带有`@RefreshScope`的定时任务组件：

```java
@Component
@RefreshScope // 用于加载配置更新
@Slf4j
// public class OrderConfigScheduler {
    // 值来自于远程配置order.count
    // 触发@RefreshScope执行逻辑会导致@Scheduled定时任务失效
    @Value("${order.count}")
    String count;

    // 定时任务每隔5s执行一次
    @Scheduled(cron = "*/5 * * * * ?")
    public void execute() {
        log.info("定时任务正常执行：order.count = {}", count);
    }
}
```

**步骤二：开启定时任务功能**

在启动类添加`@EnableScheduling`注解：

```java
@SpringBootApplication
@EnableDiscoveryClient
@Slf4j
@EnableScheduling   // 开启定时任务功能
public class TlmallOrderConfigDemoApplication {
    public static void main(String[] args) {
	    // …… 其它代码
	}

	// …… 其它代码
}
```

**步骤三：观察正常执行状态**

启动微服务，定时任务正常执行：

<img src="imgs/spring-cloud-alibaba-06-config/6b040e8ab2256c373b99a5a9608de35b_MD5.jpg" style="display: block; width: 100%;" alt="定时任务正常执行">

**步骤四：触发配置变更**

在Nacos修改`order.count`配置，触发Bean重建：

<img src="imgs/spring-cloud-alibaba-06-config/030fedcefd6670adce8af4c0bc32c0c2_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置修改">

**验证结果**：

配置更新后，定时任务停止执行。日志显示配置已成功推送和加载，但不再有定时任务执行记录：

```java
INFO 9395 --- [tlmall-config-demo-order] [listener.task-0] c.a.c.n.c.NacosConfigDataLoader          : [Nacos Config] Load config[dataId=tlmall-config-demo-order.yml, group=DEFAULT_GROUP] success
DEBUG 9395 --- [tlmall-config-demo-order] [listener.task-0] c.a.c.n.c.NacosConfigDataLoader          : [Nacos Config] config[dataId=tlmall-config-demo-order.yml, group=DEFAULT_GROUP] content:
# 数据库连接
spring:
  datasource:
    url: jdbc:mysql://tlmall-mysql:3306/tlmall_order?useSSL=false&characterEncoding=utf8&allowPublicKeyRetrieval=true
# 热加载配置实验
order:
  count: 13
2026-02-11T15:12:00.660+08:00  INFO 9395 --- [tlmall-config-demo-order] [listener.task-0] o.s.c.e.event.RefreshEventListener       : Refresh keys changed: [order.count]
2026-02-11T15:12:00.660+08:00 DEBUG 9395 --- [tlmall-config-demo-order] [listener.task-0] c.a.c.n.refresh.NacosContextRefresher    : Refresh Nacos config group=DEFAULT_GROUP,dataId=tlmall-config-demo-order.yml,configInfo=# 数据库连接
spring:
  datasource:
    url: jdbc:mysql://tlmall-mysql:3306/tlmall_order?useSSL=false&characterEncoding=utf8&allowPublicKeyRetrieval=true
# 热加载配置实验
order:
  count: 13
```

**结论**：使用`@RefreshScope`的定时任务组件，在配置变更后Bean被清除但未自动重建，导致定时任务失效。

#### (3) 解决方案

**解决思路**：

根据源码分析，定时任务失效的根本原因是Bean被清除后未自动重建。由于`@RefreshScope`标注的Bean采用懒加载机制，只有在被调用时才会触发重建。

因此，可以通过监听`RefreshScopeRefreshedEvent`事件，在配置变更时主动触发Bean访问，从而完成Bean重建和定时任务重新注册。

**实现演示**：

**步骤一：修改定时任务组件**

让`OrderConfigScheduler`实现`ApplicationListener`接口，监听刷新事件：

```java
@Controller
@RefreshScope // 用于加载配置更新
@Slf4j
public class OrderConfigScheduler implements ApplicationListener<RefreshScopeRefreshedEvent> {
    // 值来自于远程配置order.count
    @Value(PropertyPlaceholders.ORDER_COUNT)
    String count;

    // 触发@RefreshScope执行逻辑会导致@Scheduled定时任务失效
    // 定时任务每隔5s执行一次
    @Scheduled(cron = "*/5 * * * * ?")
    public void execute() {
        log.info("定时任务正常执行：order.count = {}", count);
    }

    @Override
    public void onApplicationEvent(RefreshScopeRefreshedEvent event) {
        // 监听到RefreshScopeRefreshedEvent时，执行一个空的方法，触发Bean重建，恢复失效的定时任务
        log.info("监听到RefreshScopeRefreshedEvent，执行该方法触发Bean重建");
    }
}
```

**步骤二：启动微服务验证**

启动微服务，定时任务正常执行：

<img src="imgs/spring-cloud-alibaba-06-config/1b85213bc821c5d390d216527219f508_MD5.jpg" style="display: block; width: 100%;" alt="定时任务正常执行">

**步骤三：触发配置变更**

修改Nacos远程配置，触发`@RefreshScope`执行：

<img src="imgs/spring-cloud-alibaba-06-config/6733bc7a69cdbb3aabe82fbc7044c175_MD5.jpg" style="display: block; width: 100%;" alt="Nacos配置修改">

**验证结果**：

从日志可以看到，通过事件监听触发Bean重建，定时任务恢复正常执行：

```text
INFO 11003 --- [tlmall-config-demo-order] [listener.task-0] o.n.t.scheduler.OrderConfigScheduler     : 监听到RefreshScopeRefreshedEvent，执行该方法触发Bean重建
INFO 11003 --- [tlmall-config-demo-order] [listener.task-0] o.s.c.e.event.RefreshEventListener       : Refresh keys changed: [order.count]
DEBUG 11003 --- [tlmall-config-demo-order] [listener.task-0] c.a.c.n.refresh.NacosContextRefresher    : Refresh Nacos config group=DEFAULT_GROUP,dataId=tlmall-config-demo-order.yml,configInfo=# 数据库连接
spring:
  datasource:
    url: jdbc:mysql://tlmall-mysql:3306/tlmall_order?useSSL=false&characterEncoding=utf8&allowPublicKeyRetrieval=true
# 热加载配置实验
order:
  count: 14
INFO 11003 --- [tlmall-config-demo-order] [listener.task-0] c.a.nacos.client.config.impl.CacheData   : [fixed-tlmall-nacos-server_8848] [notify-ok] dataId=tlmall-config-demo-order.yml, group=DEFAULT_GROUP,tenant=, md5=e17e7e9829afd1e86d4a6b9e13b7561a, listener=com.alibaba.cloud.nacos.refresh.NacosContextRefresher$1@4ceadfdf ,job run cost=303 millis.
INFO 11003 --- [tlmall-config-demo-order] [   scheduling-1] o.n.t.scheduler.OrderConfigScheduler     : 定时任务正常执行：order.count = 14
INFO 11003 --- [tlmall-config-demo-order] [   scheduling-1] o.n.t.scheduler.OrderConfigScheduler     : 定时任务正常执行：order.count = 14
```

通过事件监听机制，成功解决了定时任务失效的问题。

### 4.4 OpenFeign配置动态刷新

OpenFeign官方提供了配置动态刷新支持，无需使用`@RefreshScope`注解。

**配置方法**：

**步骤一：本地启用刷新功能**

在本地`application.yml`中启用OpenFeign的动态刷新：

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default: # 默认开启动态刷新
            refresh-enabled: true 
```

**步骤二：Nacos托管超时配置**

将超时配置托管到Nacos配置中心：

```yaml
spring:
  cloud:
    openfeign:
      client:
        config:
          default: # 默认超时时间
            connectTimeout: 5000
            readTimeout: 5000
```

**刷新效果**：

修改Nacos中的超时配置后，OpenFeign会自动检测并应用新配置，无需重启服务。

**说明**：

对于OpenFeign这类第三方中间件，优先使用官方提供的动态刷新机制，而非`@RefreshScope`注解。官方实现通常更稳定可靠。

## 5. Nacos插件扩展


Nacos提供了丰富的插件扩展机制，基于SPI（Service Provider Interface）实现。

官方已实现多种插件类型，详情可查阅[Nacos官方文档](https://nacos.io/docs)。

| 插件类型       | 功能说明        |
| ---------- | ----------- |
| **鉴权插件**   | 实现用户认证和授权功能 |
| **配置加密插件** | 实现敏感配置的加解密  |
| **多数据源插件** | 支持多种数据存储后端  |
| **追踪插件**   | 实现配置变更审计追踪  |

**插件开发流程**：

1. 实现Nacos定义的插件接口
2. 通过SPI机制注册插件
3. 打包部署到Nacos服务器和客户端

**参考资源**：

| 类型     | 链接                                                                                 |
| ------ | ---------------------------------------------------------------------------------- |
| **代码** | [github.com/nacos-group/nacos-plugin](https://github.com/nacos-group/nacos-plugin) |
| **文档** | [Nacos官方文档](https://nacos.io/docs/latest/plugin/auth-plugin/)                      |
## 6. 总结

本文系统介绍 Spring Cloud Alibaba Nacos 配置中心实战应用，通过基础使用 + 配置隔离 + 动态刷新 + 扩展增强的完整闭环，帮助读者：

| 学习层次 | 核心收获 |
|---------|---------|
| **建立体系化认知** | 从快速上手到深入原理，完整理解 Nacos 配置中心的三个隔离层次（Profile、Namespace、Group）和配置优先级机制 |
| **掌握实践能力** | 熟练使用 `spring.config.import` 管理多环境配置，实现配置动态刷新，掌握 `@RefreshScope` 注解的应用及注意事项 |
| **优化生产应用** | 定时任务失效解决方案，以及生产环境配置管理的最佳实践等 |

配套代码：[spring-cloud-alibaba-2023-nacos](https://github.com/fangkun119/spring-cloud-alibaba-2023-nacos)
