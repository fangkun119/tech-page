---
title: Spring Cloud Alibaba上手 07：Seata
author: fangkun119
date: 2025-12-24 12:00:00 +0800
categories: [微服务, Spring Cloud Alibaba]
tags: [微服务, Spring Cloud Alibaba, Seata]
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

本文系统讲解基于 **Seata AT 模式**实现**分布式事务**的完整方案，涵盖以下知识模块：

| 知识模块      | 说明                                                                                    |
| --------- | ------------------------------------------------------------------------------------- |
| **业务场景**  | **电商下单**：订单、库存、账户三服务的分布式事务协调                                                          |
| **核心原理**  | **Seata AT 模式**：三大角色（TM/TC/RM）、两阶段提交流程、四大关键机制（UndoLog/本地锁/全局锁/事务分组）                                                |
| **微服务整合** | **TM（订单服务）**：`@GlobalTransactional` 全局事务注解<br>**RM（库存/账户服务）**：`@Transactional` 本地事务注解 |
| **测试验证**  | **正常提交**与**异常回滚**场景测试、事务一致性验证                                                         |

本文从**原理回顾**、**微服务整合**、**测试验证**三个维度阐述 **Seata 分布式事务**实战，帮助读者掌握基于 **Seata 2.0.0** 构建**分布式事务**体系的完整流程。

### 1.2 配套资源

#### (1) 项目源码

| 资源类型           | 说明                               | 链接                                                                                                                   |
| -------------- | -------------------------------- | -------------------------------------------------------------------------------------------------------------------- |
| **项目源码**       | Spring Cloud Alibaba 2023 完整示例代码 | [github.com/fangkun119/spring-cloud-alibaba-2023-demo](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo) |
| **Postman 集合** | API 测试用例集合，便于快速验证功能              | [github.com/fangkun119/postman-workspace](https://github.com/fangkun119/postman-workspace)                           |

#### (2) 环境搭建

见文档：[Spring Cloud Alibaba上手 03：中间件环境]({% post_url 2025-12-20-spring-cloud-alibaba-mvp-03-env %})

本文使用Seata 2.0.0

## 2. 背景信息

### 2.1 本文场景

**业务场景：电商下单分布式事务**

本文以**电商下单**场景为例，演示 **Seata** 分布式事务协调：

| 调用链路 | 操作说明 |
| ---- | ---- |
| **订单服务** → **库存服务** | 扣减商品库存 |
| **订单服务** → **账户服务** | 扣减用户余额 |

**事务协调规则：**

| 执行结果 | 事务动作 |
| ---- | ---- |
| **所有调用成功** | 全局提交：分布式事务完成 |
| **任意调用失败** | 全局回滚：恢复所有数据 |

**模块角色分配：**

| 模块                        | 分布式事务角色         | 职责说明                         |
| ------------------------- | --------------- | ---------------------------- |
| **订单服务** (tlmall-order)   | **TM** + **RM** | 发起全局事务（TM），管理本地资源（RM），执行分支事务 |
| **库存服务** (tlmall-storage) | **RM**          | 管理库存资源，执行分支事务                |
| **账户服务** (tlmall-account) | **RM**          | 管理账户资源，执行分支事务                |

### 2.2 前置章节

**知识衔接：**

[**Spring Cloud Alibaba上手 03：中间件环境 - 5. Seata 分布式事务**]({% post_url 2025-12-20-spring-cloud-alibaba-mvp-03-env %}#5-seata-分布式事务) 已涵盖：

| 知识模块 | 内容说明 |
| ---- | ---- |
| **理论原理** | Seata **AT 模式**分布式事务机制 |
| **环境搭建** | Seata **服务端**部署与配置 |

**本节目标：**

将 **MVP 项目**中的三个微服务（订单、库存、账户）接入 Seata，实现**分布式事务**协调。

### 2.3 原理回顾

[**前置文档**]({% post_url 2025-12-20-spring-cloud-alibaba-mvp-03-env %}#51-seata-at事务原理) 已详细讲解 **Seata AT 模式**，此处快速回顾核心要点。

#### (1) 核心概念

**为什么需要 Seata**

传统 **Spring** 的 `@Transactional` 只能保证**单机事务**原子性，无法解决**跨服务分布式事务**的一致性问题。**Seata** 正是为解决这一难题而生。

**AT 模式三大角色**

| 角色 | 全称 | Demo 模块 | 核心职责 |
|:---:|:---|:---|:---|
| **TC** | Transaction Coordinator | Seata 服务器 | 协调全局事务：维护 XID、驱动两阶段提交、管理全局锁 |
| **TM** | Transaction Manager | 订单服务 | 发起全局事务：begin/commit/rollback、决策最终状态 |
| **RM** | Resource Manager | 订单/库存/账户服务 | 管理本地资源：注册分支、记录 UndoLog、执行二阶段指令 |

#### (2) 两阶段提交流程

**一阶段：业务执行 + 记录回滚日志**

```
TM 发起全局事务 → TC 生成 XID
                    ↓
         XID 在微服务链路中透传
                    ↓
    ┌───────────────┼───────────────┐
    ↓               ↓               ↓
订单 RM          库存 RM          账户 RM
生成 UndoLog     生成 UndoLog     生成 UndoLog
注册分支事务      注册分支事务       注册分支事务
提交本地事务      提交本地事务       提交本地事务
```

> 💡 **关键**：数据已真实落库，但通过 UndoLog 保留了回滚能力

**二阶段：根据一阶段结果决定提交或回滚**

| 场景 | TC 指令 | RM 操作 | 性能特点 |
|:---|:---|:---|:---|
| **全局提交** | `branchCommit` | 异步删除 UndoLog | 极轻量，纯清理 |
| **全局回滚** | `branchRollback` | 执行 UndoLog 补偿 | 逆向还原数据 |

#### (3) 四大关键机制

除了上述流程展示的 **Seata AT 模式**的两阶段提交机制（**TM、TC、RM** 协作流程），**Seata** 还依赖以下四个核心机制保障分布式事务的**一致性**与**隔离性**：


|    核心机制     | 一阶段作用         | 二阶段作用             | 核心价值           |
| :---------: | :------------ | :---------------- | :------------- |
| **UndoLog** | 记录SQL执行前后镜像   | 提交时异步删除 / 回滚时执行补偿 | 回滚基础，保证数据可还原   |
|   **本地锁**   | 本地事务执行期间持有    | 事务提交后释放           | 防止本地并发写冲突      |
|   **全局锁**   | 向TC申请，修改前必须获取 | TC统一管理，分支提交后释放    | 防止分布式并发写冲突     |
|  **事务分组**   | 应用配置逻辑分组名     | TC通过配置映射到物理集群     | 逻辑与物理解耦，支持灵活切换 |

## 3. 微服务整合

### 3.1 事务发起者（TM）- 订单服务

**角色定位**：**订单服务**（`tlmall-order`）在本场景中承担**双重角色**：

| 角色     | 全称                  | 职责说明           |
| :----- | :------------------ | :------------- |
| **TM** | Transaction Manager | 发起全局事务，协调提交或回滚 |
| **RM** | Resource Manager    | 管理本地资源，执行分支事务  |

#### (1) 整合步骤

**步骤一：引入 Maven 依赖**

```xml
<!-- seata 依赖-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

完整代码：[microservices/tlmall-order/pom.xml](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/tlmall-order/pom.xml)


**步骤二：创建 AT 模式 Undo Log 表**

<img src="imgs/spring-cloud-alibaba-mvp-07-seata/a71dd9ce58d46caff57575ff11f1fd03_MD5.jpg" style="display: block; width: 100%;" alt="创建Undo Log表">

完整SQL：[microservices/sql/init.sql](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/sql/init.sql)


**步骤三：添加 Seata Client 配置**

<img src="imgs/spring-cloud-alibaba-mvp-07-seata/74a21259df86eeb2708e5a2e28c2f4aa_MD5.jpg" style="display: block; width: 100%;" alt="添加Seata Client配置">

配置已上传至 **Nacos**，通过以下本地配置导入：

```yml
spring:
  config:
    import:
      - optional:nacos:seata-client.yml
```

**配置文件位置：**

| 配置内容     | 配置类型 | 文件路径                                                                                                                                                                                                      |
| :------- | :--- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Seata客户端 | 远程配置 | [midwares/dev/remote/nacos/public/DEFAULT_GROUP/seata-client.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/seata-client.yml) |
| 订单服务     | 本地配置 | [microservices/tlmall-order/src/main/resources/application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/tlmall-order/src/main/resources/application.yml)     |

**步骤四：添加全局分布式事务注解**

```java
package org.springcloudmvp.tlmallorder.service.impl;

import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
// ...

@Service
public class OrderServiceImpl implements OrderService {

	@Override
    @GlobalTransactional(name = "createOrder", rollbackFor = Exception.class)
    public Result<?> createOrder(String userId, String commodityCode, Integer count) {
	    ...
	}
}
```

#### (2) 理解 @GlobalTransactional 注解

**工作机制：**

当调用 `createOrder` 方法时，`@GlobalTransactional` 的 **AOP 拦截器**生效，将每个 **SQL 执行**和**远程调用**视为一个**分支事务**。

以"先执行 SQL，再发起远程调用"为例，执行流程如下：

##### AT 一阶段操作

**TM（事务发起者）执行流程：**

| 步骤 | 执行主体 | 操作说明 |
| :--- | :--- | :--- |
| **1** | **TM** → **TC** | 开启全局事务，TC 生成 **XID** 并返回 |
| **2** | **TM** | 将 **XID** 绑定到当前线程上下文，准备执行业务逻辑 |

**RM（本地 SQL 执行）执行流程：**

| 步骤    | 执行主体            | 操作说明                                                                    |
| :---- | :-------------- | :---------------------------------------------------------------------- |
| **3** | **RM**          | 解析 SQL，生成**执行前镜像**                                                      |
| **4** | **RM**          | **执行业务 SQL**（数据库自动获取**本地锁**）                                            |
| **5** | **RM**          | 生成**执行后镜像**，插入 **Undo Log**                                             |
| **6** | **RM** → **TC** | 注册**分支事务**，获取 BranchId                                                  |
| **7** | **RM** → **TC** | 使用 BranchId 申请**全局锁**                                                   |
| **8** | **RM**          | **判断全局锁申请结果：**<br>• 成功：提交**本地事务**（数据真实落库），释放**本地锁**<br>• 失败：回滚本地事务，防止脏写 |

**远程调用与二阶段准备：**

| 步骤 | 执行主体 | 操作说明 |
| :--- | :--- | :--- |
| **9** | **TM** → **下游服务** | 通过 **XID** 透传，调用库存服务、账户服务 |
| **10** | **下游 RM** | 重复步骤 3-8，各自注册分支事务 |
| **11** | **TM** | 等待所有远程调用执行完毕，进入二阶段 |

**是否同时使用 `@Transactional` 注解？**

| 使用方式                                          | 效果说明                                   | 典型场景                             | 建议                            |
| :-------------------------------------------- | :------------------------------------- | :------------------------------- | :---------------------------- |
| 方式①：**仅** `@GlobalTransactional`              | 每个 SQL 注册为**独立的分支事务**，二阶段统一提交/回滚       | ✅ 订单插入后调用远程服务<br>✅ 多个独立的数据库操作    | ⭐ **推荐（默认）**<br>适合绝大多数分布式事务场景 |
| 方式②：`@GlobalTransactional` + `@Transactional` | 同一方法内的多条 SQL 合并为**一个分支事务**，减少与 TC 交互次数 | ⚡ 本地多条SQL高耦合，需要事务原子性<br>⚡ 性能敏感场景 | ⚠️ **谨慎**<br>会增加复杂度，谨慎评估成本收益  |

**本文选择方式①**

| 服务模块     | Seata 角色 | 使用的注解                   | 说明                   |
| :------- | :------- | :---------------------- | :------------------- |
| **订单服务** | TM + RM  | 仅`@GlobalTransactional` | 发起全局事务，每个 SQL 作为独立分支 |
| **库存服务** | RM       | `@Transactional`        | 保证本地事务原子性            |
| **账户服务** | RM       | `@Transactional`        | 保证本地事务原子性            |

##### AT 二阶段操作

**场景一：全局事务成功**

| 执行主体 | 操作说明 |
| :--- | :--- |
| **TM** → **TC** | 发起**全局提交**请求 |
| **TC** → **RM** | 异步指导各 **RM** 删除 **Undo Log**（纯清理，极轻量） |

**场景二：全局事务失败**

| 执行主体 | 操作说明 |
| :--- | :--- |
| **TM** → **TC** | 发起**全局回滚**请求 |
| **TC** → **RM** | 指导各 **RM** 执行补偿（回滚）操作，为确保数据一致性，回滚操作具有**最高优先级** |

### 3.2 事务参与者（RM）- 库存、账户服务

**角色定位：**

| 模块 | Seata 角色 | 核心职责 |
| :--- | :--- | :--- |
| **库存服务**（`tlmall-storage`） | **RM** | 管理库存资源，执行分支事务 |
| **账户服务**（`tlmall-account`） | **RM** | 管理账户资源，执行分支事务 |

#### (1) 整合步骤

**步骤一：引入 Maven 依赖**

```xml
<!-- seata 依赖-->
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
</dependency>
```

| 模块 | 仓库路径 |
| :--- | :--- |
| **库存服务** | [microservices/tlmall-storage/pom.xml](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/tlmall-storage/pom.xml) |
| **账户服务** | [microservices/tlmall-account/pom.xml](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/tlmall-account/pom.xml) |

**步骤二：创建 Undo Log 表（仅 AT 模式）**

库存服务和账户服务**各自维护一张 Undo Log 表**，在 **3.3 小节**与业务表一起创建，SQL 见：[microservices/sql/init.sql](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/sql/init.sql)

<img src="imgs/spring-cloud-alibaba-mvp-07-seata/4e55485e206c84d2aab275075680f6b3_MD5.jpg" style="display: block; width: 100%;" alt="库存和账户服务的UndoLog表">

**步骤三：添加 Seata Client 配置**

与订单服务相同，也让**库存服务**、**账户服务**导入Seata客户端远程配置（Nacos上Data ID为“seata-client.yml”的配置）。

| 配置内容     | 配置类型 | 仓库路径                                                                                                                                                                                                      |
| :------- | :--- | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Seata客户端 | 远程配置 | [midwares/dev/remote/nacos/public/DEFAULT_GROUP/seata-client.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/midwares/dev/remote/nacos/public/DEFAULT_GROUP/seata-client.yml) |
| 库存服务     | 本地配置 | [microservices/tlmall-storage/src/main/resources/application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/tlmall-storage/src/main/resources/application.yml) |
| 账户服务     | 本地配置 | [microservices/tlmall-account/src/main/resources/application.yml](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/tlmall-account/src/main/resources/application.yml) |

**步骤四：添加本地事务注解 `@Transactional`**

**库存服务：**

```java
@Override
@Transactional
public void reduceStock(String commodityCode, Integer count) throws BusinessException {
	// ...
}
```

**账户服务：**

```java
@Override
@Transactional
public void reduceBalance(String userId, Integer price) throws BusinessException {
	// ...
}
```

**完整代码：**

| 模块       | 完整代码                                                                                                                                                                                                                                                    |
| :------- | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| **库存服务** | [microservices/tlmall-storage/src/.../StorageServiceImpl.java](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/tlmall-storage/src/main/java/org/springcloudmvp/tlmallstorage/service/impl/StorageServiceImpl.java) |
| **账户服务** | [microservices/tlmall-account/src/.../AccountServiceImpl.java](https://github.com/fangkun119/spring-cloud-alibaba-2023-demo/blob/main/microservices/tlmall-account/src/main/java/org/spcloudmvp/tlmallaccount/service/impl/AccountServiceImpl.java)     |

#### (2) 理解 @Transactional 注解

**工作机制：**

`@Transactional` 是 **Spring** 的单机事务注解，负责本地事务回滚。**Seata** 通过 **AOP 代理**拦截其执行的 **SQL**，生成 **Undo Log**，使得全局事务回滚时，本地事务操作也能被回滚。

**AT 一阶段执行流程：**

| 步骤 | 操作说明 |
| :--- | :--- |
| **1** | 关闭本地事务自动提交，解析 SQL，获取**执行前镜像** |
| **2** | **执行业务 SQL**（数据库自动获取**本地锁**） |
| **3** | 查询数据库，获取**执行后镜像** |
| **4** | 构建 **Undo Log** 并插入数据库（**尚未提交**） |
| **5** | 注册**分支事务**并申请**全局锁**（需在本地事务提交前完成）<br>⚠️ **如果全局锁获取失败，回滚本地事务** |
| **6** | 提交**本地事务**（业务数据 + Undo Log 真实落库），释放**本地锁** |
| **7** | 如有远程调用，传递 **XID** 给下游服务并等待返回 |

> `@Transactional` **不参与 AT 二阶段**，AT 二阶段由 **TM** 的 `@GlobalTransactional` 统一处理。

### 3.3 测试验证

**测试场景：**

| 测试步骤 | 操作说明 | 预期结果 |
| :--- | :--- | :--- |
| **场景一** | 在 `http://tlmall-frontend:8080/order` 正常下单 | 下单成功 ✅ |
| **场景二** | 将用户余额修改为 `1`（不足购买单价为 `2` 的商品）<br>`UPDATE tlmall_account.account SET money = 1 WHERE user_id = 'fox'`<br>再次下单 | 下单失败，事务回滚 ✅ |

**场景一：正常下单**

<img src="imgs/spring-cloud-alibaba-mvp-07-seata/5cc909c1382681d807290cdf2dce951f_MD5.jpg" style="display: block; width: 300px;" alt="正常下单成功">

**场景二：事务回滚**

<img src="imgs/spring-cloud-alibaba-mvp-07-seata/66ae9dc73338a47969cbef355c90532b_MD5.jpg" style="display: block; width: 300px;" alt="下单失败事务回滚">

**Seata 回滚日志：**

<img src="imgs/spring-cloud-alibaba-mvp-07-seata/11b4d95cd84fcbbddd89fe3802eb5f48_MD5.jpg" style="display: block; width: 100%;" alt="Seata事务回滚日志">

### 3.4 Seata 2.0.0 的问题

**问题描述：**

当事务失败触发回滚时，**Seata 2.0.0** 抛出的是 `RuntimeException`，而非能够精准表达事务回滚的 `BusinessException`。

**当前应对方案：**

在代码中使用 `rollbackFor = Exception.class` 捕获所有异常：

```java
@Override
@GlobalTransactional(name = "createOrder", rollbackFor = Exception.class)
public Result<?> createOrder(String userId, String commodityCode, Integer count) {

}
```

**生产环境建议：**

应当将**事务异常**（需要回滚）与其他异常**区分开**，以便精准判断何时需要回滚。

> ⚠️ 这是 **Seata 2.0.0** 的已知问题，建议升级到**更新版本**以获得更精确的异常处理机制。

## 4. 总结

本文系统讲解 **Seata AT 模式**实现分布式事务的完整方案，通过**原理回顾**、**微服务整合**、**测试验证**的实战闭环，帮助读者：

| 学习层次        | 核心收获                                                                                                                       |
| :---------- | :------------------------------------------------------------------------------------------------------------------------- |
| **建立体系化认知** | 理解 **Seata AT 模式**三大角色（**TM/TC/RM**）协作机制、两阶段提交流程、四大关键机制（**UndoLog/本地锁/全局锁/事务分组**），掌握分布式事务一致性保障原理                           |
| **掌握实战能力**  | 熟练运用 **@GlobalTransactional**、**@Transactional** 注解实现微服务分布式事务协调，掌握**电商下单**场景的**订单/库存/账户**三服务整合，具备**正常提交**与**异常回滚**场景测试验证能力 |





