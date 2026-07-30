---
title: 传统项目迁AI 09：了解项目 - 用AI产出接口清单与数据模型
author: fangkun119
date: 2026-07-04 09:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-09-proj-rd-04-iface-and-data-model/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
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

<!--
aicmigr-09-proj-rd-04-iface-and-data-model
传统项目迁AI 09：了解项目 - 用AI产出接口清单与数据模型
-->

## 1. 从「项目长什么样」到「项目对外承诺什么」

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/3692bc1178e2ecd801d47eb1f378552a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

项目摸底的三张俯视图——架构图、模块图、依赖图——回答的是「这个项目长什么样」。

但只看到样子还不够。真要动手改造，还要往里走一层，回答两个更关键的问题：

这个项目<span style="color: red; font-weight: bold;">对外承诺了哪些接口</span>？<span style="color: red; font-weight: bold;">内部在处理什么数据</span>？

本篇就来回答这两个问题。读完之后，你会拿到两份新资产：

- 一份 <span style="color: red; font-weight: bold;">REST 接口清单</span>——项目对外的契约
- 一份 <span style="color: red; font-weight: bold;">核心数据模型说明</span>——项目内部的骨骼

外加一套可复用的方法论：让 AI <span style="color: red; font-weight: bold;">一次性</span>把这两份资产梳理出来的提示词模板、关键约束、常见坑清单、互相校对动作。这两份资产，是后续所有改造工作的导航地图。

### 1.1 两份新资产的定位

把两份资产的定位说清楚：

| 资产                                                           | 类比                             | 回答的问题        |
| ------------------------------------------------------------ | ------------------------------ | ------------ |
| <span style="color: red; font-weight: bold;">REST 接口清单</span>                                                    | Controller 的索引视图               | 项目对外承诺了哪些能力？ |
| <span style="color: red; font-weight: bold;">核心数据模型说明</span> | <span style="color: red; font-weight: bold;">entity + DTO + DB schema 的合并视图</span> | 内部在流动什么数据？   |

加上之前的三张图，手里就有了五份资产：<span style="color: red; font-weight: bold;">架构图、模块图、依赖图、接口清单、数据模型</span>。后面所有改造动作，都从这五份资产出发。

这五份资产并不是一次产出的，而是分布在老项目改造八步流程的不同步骤里。下面这张图展示整套流程的全貌，本篇落在第 5 步「梳理接口和数据模型」——前一篇已经把前 4 步的三张俯视图做完，从这一步开始往项目的门面和根基深入。

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/9b783b9df2264f41a957b0d0a4f75e51_MD5.jpg" style="display: block; width: 800px;" alt="老项目改造八步流程图">

<!--
图片内容说明
路径：imgs/aicmigr-09-proj-rd-04-iface-and-data-model/9b783b9df2264f41a957b0d0a4f75e51_MD5.jpg
用途：呈现老项目改造八步流程整体流程，本篇对应步 5「梳理接口和数据模型」
内容：老项目改造八步流程图，第 5 步被突出标注，承接上一篇的三张俯视图
-->

### 1.2 只读代码、不跑项目

这一步和画三张图一样，**只读代码，不跑项目**。

你可能会问：跑起来看接口不是更直观吗？

我的看法是：跑项目要配环境、要起依赖、要造数据，成本高、噪声大。而接口清单和数据模型，**所有信息都在代码里**——Controller 注解、entity 类、建表 SQL，这些静态信息读出来就够了。AI 读完代码就能产出资产，不需要项目真的跑起来。

## 2. 为什么接口和数据模型必须一起梳理

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/748b8f43e8fd9007f44d43655d15992f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">接口和数据模型看起来是两件事：一个是"对外"，一个是"对内"。</span>但本篇坚持**合在一起一次性梳理**。

原因有三条。理解了这三条，就理解了为什么这两份资产是改造的导航地图。

### 2.1 接口是项目的门面、数据模型是项目的根基

外部世界怎么和这个项目打交道？前端调什么 API、三方集成请求什么端点、运维连什么管理接口——全是接口。<span style="color: red; font-weight: bold;">接口清单决定了"项目对外承诺了什么"</span>。类比到传统软件工程，接口清单就是项目的"契约入口"。

那接口处理的每一个请求、返回的每一份响应，背后对应的是什么？是某些<span style="color: red; font-weight: bold;">数据结构</span>的流动。Prompt 对象长什么样、Dataset 有哪些字段、Evaluator 的结果存成什么格式——这些是<span style="color: red; font-weight: bold;">项目的内部骨骼</span>。entity 对应 DB schema 的映射，数据模型就是这副骨骼的 X 光片。

### 2.2 改造视角：一次新功能几乎都要同时改三处

从改造视角看，加一个新功能大概率要同时改三处：

| 改动点   | 内容               |
| ----- | ---------------- |
| 加接口   | 暴露一个新的端点供前端或外部调用 |
| 改数据模型 | 调整若干字段、加表或加列     |
| 改业务逻辑 | 把接口和数据模型串起来      |

你可能会问：为什么不直接写 Controller？

因为只改 Controller 不动数据模型，接口的入参返回就无处落地；只动数据模型不暴露接口，外部世界感知不到这次改造。**三处是绑定的**。<span style="color: red; font-weight: bold;">所以接口清单和数据模型这两份资产，本来就是同一次改造的两面。</span>

### 2.3 没有两份资产的代价

#### (1) 每次改造从零摸索

接口清单和数据模型是改动的导航地图。<span style="color: red; font-weight: bold;">没有这两份资产，每次改造都要从零摸索</span>——每次都要回去翻 Controller、翻 entity、翻 SQL，反复确认"这个字段在哪个表""这个接口叫什么"。

#### (2) AI 没有共同记忆

更关键的是，AI 也没有共同记忆。

每次对话、每次改造，AI 都是从零开始读代码。如果有一份写下来的资产，AI 可以把它当上下文，直接基于这份共同记忆帮你做事；如果没有，AI 每次都要重新摸索，而且<span style="color: red; font-weight: bold;">每次摸出来的结果都不一样</span>。

<span style="color: red; font-weight: bold;">这两份资产，本质上是给 AI 和你都准备一份"共同记忆"。</span>

## 3. 让 AI 一次性产出两份资产的方法论

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/5228e00a106ddae99f9c3e9f0a9e1478_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

实战之前，先把方法论提炼出来。本节回答四个问题：

- 两份资产各看哪些信息源？
- 接口清单的提示词怎么写？
- 数据模型的提示词怎么写？
- 两份资产做完之后还要不要互相校对？（校对的完整动作放到第 6 章展开）

### 3.1 不让 AI 读真实代码，它就会脑补

这是最重要的一条经验。

你不告诉 AI 看哪里，它就会**根据模块名、类名猜**——猜出来的端点不存在，猜出来的字段拼错位置。<span style="color: red; font-weight: bold;">AI 的脑补能力很强，强到你不细看都会信。</span>

<span style="color: red; font-weight: bold;">所以两份资产必须分别指定明确的信息源。</span>

#### (1) 两份资产各自的信息源

| 资产   | 信息源                                                                                       | 不让 AI 读会怎样                        |
| ---- | ----------------------------------------------------------------------------------------- | --------------------------------- |
| 接口清单 | <span style="color: red; font-weight: bold;">项目所有 Controller（多模块时每个 server 子模块都要扫）</span> | AI 根据模块名瞎猜端点，遗漏整片接口               |
| 数据模型 | <span style="color: red; font-weight: bold;">entity 类、DTO、数据库建表 SQL（三边对照）</span>                                                              | AI 只看 entity，漏掉 DB 实际字段或 DTO 实际契约 |

### 3.2 接口清单的提示词约束

接口清单的提示词除了"扫所有 Controller"之外，还有三条关键约束决定了清单质量。

#### (1) 按模块分组

让清单有组织。

Spring AI Alibaba Admin 有 Prompt、Dataset、Evaluator、Experiment、Trace 几大模块，每个模块下有若干接口。<span style="color: red; font-weight: bold;">分组的清单可读性远高于一张一百行的大表</span>。类比到传统工程，这就是 Controller 按业务域分包的习惯，搬到资产文档里。

#### (2) 强制一句话人类看懂

强制 AI 给出人类能看懂的意图，而不是只抄 `@Operation` 注解或方法名。

接口叫 `createPromptTemplate`，一句话说明应该是"创建一个新的 Prompt 模板"。<span style="color: red; font-weight: bold;">让非开发同事也能看懂</span>——<span style="color: red; font-weight: bold;">产品、测试、运维都可能翻这份清单</span>。

#### (3) 列主要入参返回、不展开字段

要求 AI 不只列端点，还要列<span style="color: red; font-weight: bold;">参数类型和返回类型</span>，但<span style="color: red; font-weight: bold;">不要展开所有字段，只要主要的</span>。

为什么不全展开？因为字段细节交给数据模型那份文档，两份资产各司其职。接口清单只<span style="color: red; font-weight: bold;">回答"这个接口要什么、给什么"</span>，不回答"这个对象里每个字段长什么样"。

### 3.3 数据模型的提示词约束

数据模型的提示词要求 AI 同时看三个数据源，并产出 Markdown 说明加一张 ER 图。

#### (1) entity / DTO / 建表 SQL 三边对照

| 数据源      | 层次       | 职责               |
| -------- | -------- | ---------------- |
| entity 类 | Java 持久层 | entity ↔ DB 表的映射 |
| DTO      | 传输层      | 接口请求/响应的契约       |
| 建表 SQL   | DB 层     | 数据库实际表结构         |

三者不完全一致是常态：entity 有的字段 DTO 里不暴露，DTO 有的字段是两个 entity 的组合。**让 AI 三边对照**，而不是只看一个源。

<span style="color: red; font-weight: bold;">类比到传统工程，这就像接口契约、ORM 映射、DB schema 这三层，永远在做 diff——任何一层单独看都会漏信息。</span>

#### (2) 标出主键、外键、枚举值

这三个是数据模型的硬信息：

| 信息  | 作用                                  |
| --- | ----------------------------------- |
| <span style="color: red; font-weight: bold;">主键</span>  | 每个表怎么定位一条记录                         |
| <span style="color: red; font-weight: bold;">外键</span>  | 表之间怎么关联                             |
| <span style="color: red; font-weight: bold;">枚举值</span> | 字段取值范围（如 Prompt 状态、Experiment 运行状态） |

<span style="color: red; font-weight: bold;">这三个是改造时最容易踩坑的地方</span>。我的立场是：数据模型可以缺关系图，但绝对不能缺这三项。

#### (3) Markdown 说明 + ER 图双产物

让 AI 同时产出两份产物：

| 产物          | 适合什么场景                                        |
| ----------- | ------------------------------------------------- |
| Markdown 说明 | <span style="color: red; font-weight: bold;">精确查找（PromptTemplate 表有哪些字段）</span> |
| ER 图        | <span style="color: red; font-weight: bold;">整体把握（这几个表是怎么关联的）</span>            |

Markdown 说明像字典，ER 图像地图。**两者缺一不可**——字典解决"查一个"，地图解决"看全局"。

最后补一句：两份资产做完一定要<span style="color: red; font-weight: bold;">互相校对</span>，接口清单里的类型和数据模型里的实体应该对得上。这个校对动作后面第 6 章会完整展开，这里先点到为止。

## 4. 实战一：Spring AI Alibaba Admin 接口清单

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/44b049ff689ba9c3fd23d1965b402940_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把方法论套到主线项目 **Spring AI Alibaba Admin** 上跑一遍，一次性产出一份 REST 接口清单住进 `docs/`。这份接口清单是项目对外契约的索引视图——俯视图告诉你「项目长什么样」，接口清单告诉你「项目怎么被调用」。本篇仍然只让 AI 读代码、产出文档，把「让项目跑起来」留到构建护栏阶段一次性讲透。

### 4.1 提示词原文

把方法论的提示词直接套到 Spring AI Alibaba Admin 上：

```
扫一下这个项目里所有的 Controller，给我整理一份 REST 接口清单。
每个接口列出方法（GET/POST 等）、路径、一句话说明、主要入参、返回结构。
按模块分组。保存到 docs/api-list.md。
```

### 4.2 三个关键点回顾

这三个关键点本质上是第 3 章方法论在主线项目上的落地，这里快速对照。

#### (1) 关键点一：按模块分组

Spring AI Alibaba Admin 有 Prompt、Dataset、Evaluator、Experiment、Trace 等几大模块，每个模块下都有若干接口。<span style="color: red; font-weight: bold;">分组的清单可读性远高于一张一百行的大表</span>——传统软件工程里写接口文档也会按业务域分目录，AI 生成的清单同样要遵循这个习惯。

#### (2) 关键点二：「一句话说明」强制人类意图

<span style="color: red; font-weight: bold;">强制 AI 给出人类能看懂的意图</span>，而不是只抄 `@Operation` 注解或方法名。类比一下 Controller 扫描≈Swagger 自动文档：Swagger 默认抓的是方法签名和注解，但接口签名不等于业务意图。接口叫 `createPromptTemplate`，一句话说明应该是「创建一个新的 Prompt 模板」，让非开发同事也能看懂。

#### (3) 关键点三：主要入参和返回结构，不展开所有字段

要求 AI 不只列端点，<span style="color: red; font-weight: bold;">还要列参数类型和返回类型</span>，但<span style="color: red; font-weight: bold;">不要展开所有字段，只要主要的</span>。字段细节交给数据模型那份文档，两份资产各司其职——统一返回结构 `Result<T>` 这种包装就是典型的「主要结构」，需要写清楚，但 `Result<T>` 内部的 `code`、`message`、`data` 三个字段则不必每次重复列。

### 4.3 产出效果（节选）

下面是 AI 跑出来的清单内容（节选，完整内容约 500 行，覆盖 32 个 Controller、22 大模块）。读者可以用它对照自己跑出来的版本是否对齐。

```markdown
> 来源：扫描所有 Controller 源码自动整理，共 32 个 Controller。
> 统一返回结构：`Result<T>` `{ code, message, data: T }`，分页为 `PageResult<T>` / `PagingList<T>` `{ total, list }`。

---

- [1. 认证 / 账号](#1-认证--账号)
- [2. Prompt 管理](#2-prompt-管理)
- [3. 数据集管理](#3-数据集管理)
- [4. 评估器管理](#4-评估器管理)
- [5. 实验管理](#5-实验管理)
- [6. 模型配置（Studio）](#6-模型配置studio)
- [7. 可观测性](#7-可观测性)
- [8. 应用管理](#8-应用管理)
- [9. 工作流调试](#9-工作流调试)
- [10. 知识库 / 文档 / 分块](#10-知识库--文档--分块)
- [11. 模型 / Provider 管理](#11-模型--provider-管理)
- [12. 工具 / 插件](#12-工具--插件)
- [13. MCP Server](#13-mcp-server)
- [14. Agent Schema](#14-agent-schema)
- [15. 文件上传](#15-文件上传)
- [16. API Key](#16-api-key)
- [17. 工作空间](#17-工作空间)
- [18. 组件服务](#18-组件服务)
- [19. Chat 对话（OpenAPI）](#19-chat-对话openapi)
- [20. OAuth2](#20-oauth2)
- [21. 系统](#21-系统)
- [22. 代码生成器（Graph Studio）](#22-代码生成器graph-studio)

---

**Base path：** `/console/v1/auth`、`/console/v1/accounts`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/auth/refresh-token` | 刷新 Token |
| POST | `/console/v1/auth/logout` | 退出登录，使 Token 失效 |

**POST `/console/v1/auth/login`**
- 入参：`LoginRequest { username, password }`
- 返回：`Result<TokenResponse>` — `{ accessToken, refreshToken, expiresIn }`

**POST `/console/v1/auth/refresh-token`**
- 入参：`RefreshTokenRequest { refreshToken }`
- 返回：`Result<TokenResponse>`

**POST `/console/v1/auth/logout`**
- 返回：`Result<Void>`

---

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/accounts` | 创建账号 |
| GET | `/console/v1/accounts` | 分页查询账号列表 |
| GET | `/console/v1/accounts/{accountId}` | 获取账号详情 |
| PUT | `/console/v1/accounts/{accountId}` | 更新账号信息 |
| DELETE | `/console/v1/accounts/{accountId}` | 删除账号 |
| PUT | `/console/v1/accounts/change-password` | 修改密码 |
| GET | `/console/v1/accounts/profile` | 获取当前登录用户信息 |

**POST `/console/v1/accounts`**

- 入参：`Account { username, email, role,... }`
- 返回：`Result<String>` — 新建账号 ID

**GET `/console/v1/accounts`**

- 入参：`BaseQuery { page, size, keyword }` (query string)
- 返回：`Result<PagingList<Account>>`

**PUT `/console/v1/accounts/change-password`**

- 入参：`ChangePasswordRequest { oldPassword, newPassword }`
- 返回：`Result<String>`

---

**Base path：** `/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/prompt` | 创建 Prompt |
| GET | `/api/prompt` | 按 promptKey 获取 Prompt |
| GET | `/api/prompts` | 分页列表 |
| PUT | `/api/prompt` | 更新 Prompt |
| DELETE | `/api/prompt` | 删除 Prompt |
| POST | `/api/prompt/version` | 创建 Prompt 版本 |
| GET | `/api/prompt/version` | 获取指定版本详情 |
| GET | `/api/prompt/versions` | 版本分页列表 |
| GET | `/api/prompt/template` | 获取 Prompt 模板详情 |
| GET | `/api/prompt/templates` | 模板分页列表 |
| POST | `/api/prompt/run` | 执行 Prompt（流式） |
| GET | `/api/prompt/session` | 获取对话 Session |
| DELETE | `/api/prompt/session` | 删除对话 Session |

**POST `/api/prompt`**

- 入参：`PromptCreateRequest { promptKey, name, description, content,... }`
- 返回：`Result<Prompt>`

**GET `/api/prompt`**

- 入参：`?promptKey=xxx`
- 返回：`Result<Prompt>`

**GET `/api/prompts`**

- 入参：`PromptListRequest { page, size, keyword }` (query string)
- 返回：`Result<PageResult<Prompt>>`

**POST `/api/prompt/version`**

- 入参：`PromptVersionCreateRequest { promptKey, content, remark,... }`
- 返回：`Result<PromptVersion>`

**GET `/api/prompt/version`**

- 入参：`?promptKey=xxx&version=xxx`
- 返回：`Result<PromptVersionDetail>`

**POST `/api/prompt/run`**

- 入参：`PromptRunRequest { promptKey, version, variables, sessionId, stream }`
- 返回：`Flux<PromptRunResponse>` — SSE 流式响应

**GET `/api/prompt/session`**

- 入参：`?sessionId=xxx`
- 返回：`Result<ChatSession>`

---

**Base path：** `/api/dataset`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/dataset/dataset` | 创建数据集 |
| GET | `/api/dataset/datasets` | 数据集分页列表 |
| GET | `/api/dataset/dataset` | 获取数据集详情 |
| PUT | `/api/dataset/dataset` | 更新数据集 |
| DELETE | `/api/dataset/dataset` | 删除数据集 |
| POST | `/api/dataset/datasetVersion` | 创建数据集版本 |
| GET | `/api/dataset/datasetVersions` | 版本分页列表 |
| PUT | `/api/dataset/datasetVersion` | 更新版本信息 |
| POST | `/api/dataset/dataItem` | 创建数据项 |
| GET | `/api/dataset/dataItems` | 数据项分页列表 |
| GET | `/api/dataset/dataItem` | 获取单条数据项 |
| PUT | `/api/dataset/dataItem` | 更新数据项 |
| DELETE | `/api/dataset/dataItem` | 删除数据项 |
| GET | `/api/dataset/experiments` | 关联实验列表 |
| POST | `/api/dataset/dataItemFromTrace` | 从链路追踪创建数据项 |

**POST `/api/dataset/dataset`**

- 入参：`DatasetCreateRequest { name, description,... }`
- 返回：`Result<Dataset>`

**GET `/api/dataset/datasets`**

- 入参：`DatasetListRequest { page, size, keyword }` (query string)
- 返回：`Result<PageResult<Dataset>>`

**GET `/api/dataset/dataset`**

- 入参：`?datasetId=123`
- 返回：`Result<Dataset>`

**POST `/api/dataset/dataItem`**

- 入参：`DatasetItemCreateRequest { datasetId, items: [{ input, expectedOutput,... }] }`
- 返回：`Result<List<DatasetItem>>`

**POST `/api/dataset/dataItemFromTrace`**

- 入参：`DataItemCreateFromTraceRequest { traceId, datasetId,... }`
- 返回：`Result<List<DatasetItem>>`

---

**Base path：** `/api/evaluator`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/evaluator/evaluator` | 创建评估器 |
| GET | `/api/evaluator/evaluators` | 评估器分页列表 |
| GET | `/api/evaluator/evaluator` | 获取评估器详情 |
| PUT | `/api/evaluator/evaluator` | 更新评估器 |
| DELETE | `/api/evaluator/evaluator` | 删除评估器 |
| POST | `/api/evaluator/evaluatorVersion` | 创建评估器版本 |
| GET | `/api/evaluator/evaluatorVersions` | 版本分页列表 |
| POST | `/api/evaluator/debug` | 调试评估器 |
| GET | `/api/evaluator/templates` | 评估器模板列表 |
| GET | `/api/evaluator/template` | 获取模板详情 |
| GET | `/api/evaluator/experiments` | 关联实验列表 |

**POST `/api/evaluator/evaluator`**

- 入参：`EvaluatorCreateRequest { name, type, config, templateId,... }`
- 返回：`Result<Evaluator>`

**POST `/api/evaluator/debug`**

- 入参：`EvaluatorTestRequest { evaluatorId, input, expectedOutput }`
- 返回：`Result<EvaluatorDebugResult>` — `{ score, passed, detail }`

**GET `/api/evaluator/templates`**

- 入参：`EvaluatorTemplateListRequest { page, size }` (query string)
- 返回：`Result<PageResult<EvaluatorTemplate>>`

---

**Base path：** `/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/experiment` | 创建实验 |
| GET | `/api/experiments` | 实验分页列表 |
| GET | `/api/experiment` | 获取实验详情 |
| GET | `/api/experiment/results` | 获取实验整体评估结果 |
| GET | `/api/experiment/result` | 获取单个评估结果明细（分页） |
| PUT | `/api/experiment/stop` | 停止实验 |
| PUT | `/api/experiment/restart` | 重启实验 |
| DELETE | `/api/experiment` | 删除实验 |

**POST `/api/experiment`**

- 入参：`ExperimentCreateRequest { name, datasetId, evaluatorIds[], promptKey, promptVersion,... }`
- 返回：`Result<Experiment>`

**GET `/api/experiment/results`**

- 入参：`?experimentId=123`
- 返回：`Result<List<ExperimentEvaluatorResult>>` — 每个评估器的汇总分

**GET `/api/experiment/result`**

- 入参：`ExperimentEvaluatorResultDetailListRequest { experimentId, evaluatorId, page, size }`
- 返回：`Result<PageResult<ExperimentEvaluatorResultDetail>>`

---

**Base path：** `/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/model/supported` | 查询支持的模型提供商列表 |
| GET | `/api/models` | 模型配置分页列表 |
| GET | `/api/model` | 按 ID 获取单条模型配置 |
| GET | `/api/models/enabled` | 获取所有已启用的模型配置 |

**GET `/api/model/supported`**

- 入参：无
- 返回：`Result<List<String>>` — 提供商名称列表，如 `["openai","dashscope","deepseek"]`

**GET `/api/models`**

- 入参：`ModelConfigQueryRequest { page, size, provider }` (query string)
- 返回：`Result<PageResult<ModelConfigResponse>>`

**GET `/api/models/enabled`**

- 入参：无
- 返回：`Result<List<ModelConfigResponse>>`

---

**Base path：** `/api/observability`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/observability/traces` | 链路列表（分页） |
| GET | `/api/observability/traces/{traceId}` | 获取 Trace 详情及 Span 树 |
| GET | `/api/observability/services` | 服务列表及统计 |
| GET | `/api/observability/overview` | 全局概览统计 |

**GET `/api/observability/traces`**

- 入参：`TracesQueryRequest { page, size, serviceName, startTime, endTime, status }` (query string)
- 返回：`Result<PageResult<TraceSpanDTO>>`

**GET `/api/observability/traces/{traceId}`**

- 入参：`traceId` (path)
- 返回：`Result<TraceDetailDTO>` — 含完整 Span 树

**GET `/api/observability/services`**

- 入参：`ServicesQueryRequest { startTime, endTime }` (query string)
- 返回：`Result<ServicesResponseDTO>` — `{ services: [{ name, requestCount, errorRate, avgDuration }] }`

**GET `/api/observability/overview`**

- 入参：`OverviewQueryRequest { startTime, endTime }` (query string)
- 返回：`Result<OverviewStatsDTO>` — `{ totalTraces, errorCount, avgDuration,... }`

---

**Base path：** `/console/v1/apps`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/apps` | 创建应用 |
| GET | `/console/v1/apps` | 应用分页列表 |
| GET | `/console/v1/apps/{appId}` | 获取应用详情 |
| PUT | `/console/v1/apps/{appId}` | 更新应用 |
| DELETE | `/console/v1/apps/{appId}` | 删除应用 |
| POST | `/console/v1/apps/{appId}/publish` | 发布应用 |
| POST | `/console/v1/apps/{appId}/copy` | 复制应用 |
| GET | `/console/v1/apps/{appId}/versions` | 应用版本列表 |
| GET | `/console/v1/apps/{appId}/versions/{version}` | 获取指定版本详情 |
| POST | `/console/v1/apps/chat/completions` | 应用对话（内部调试用） |

**POST `/console/v1/apps`**

- 入参：`Application { name, type, description, config,... }`
- 返回：`Result<String>` — 新建 appId

**POST `/console/v1/apps/{appId}/publish`**

- 入参：`appId` (path)
- 返回：`Result<Void>`

**POST `/console/v1/apps/chat/completions`**

- 入参：`AgentRequest { appId, messages[], stream,... }`，`HttpServletResponse`
- 返回：SSE 流 / JSON（取决于 stream 参数）

---

**Base path：** `/console/v1/apps`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/apps/workflow/debug/init` | 初始化工作流调试，返回入参定义 |
| POST | `/console/v1/apps/workflow/debug/run-task` | 执行调试任务 |
| POST | `/console/v1/apps/workflow/debug/get-task-process` | 查询任务执行进度 |
| POST | `/console/v1/apps/workflow/debug/resume-task` | 恢复暂停的任务 |
| POST | `/console/v1/apps/workflow/debug/part-graph/run-task` | 执行子图任务 |
| POST | `/console/v1/apps/workflow/debug/part-graph/stop-task` | 停止子图任务 |
| POST | `/console/v1/apps/workflow/{appId}/run_stream` | 正式运行工作流（SSE 流） |

**POST `/console/v1/apps/workflow/debug/init`**

- 入参：`InitRequest { appId, version }`
- 返回：`Result<List<TaskRunParam>>` — 入参字段定义列表

**POST `/console/v1/apps/workflow/debug/run-task`**

- 入参：`TaskRunRequest { appId, inputs, nodeId }`
- 返回：`Result<TaskRunResponse>` — `{ taskId, status }`

**POST `/console/v1/apps/workflow/{appId}/run_stream`**

- 入参：`appId` (path)，`ApiTaskRunRequest { inputs,... }`
- 返回：`SseEmitter` — 实时事件流

---

**Base path：** `/console/v1/knowledge-bases`、`/console/v1/documents`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/knowledge-bases` | 创建知识库 |
| GET | `/console/v1/knowledge-bases` | 知识库分页列表 |
| GET | `/console/v1/knowledge-bases/{kbId}` | 获取知识库详情 |
| PUT | `/console/v1/knowledge-bases/{kbId}` | 更新知识库 |
| DELETE | `/console/v1/knowledge-bases/{kbId}` | 删除知识库 |
| POST | `/console/v1/knowledge-bases/query-by-codes` | 按 code 批量查询 |
| POST | `/console/v1/knowledge-bases/retrieve` | 向量检索（RAG 召回） |

**POST `/console/v1/knowledge-bases/retrieve`**

- 入参：`DocumentRetrieverQuery { kbCode, query, topK, minScore }`
- 返回：`Result<List<DocumentChunk>>`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/knowledge-bases/{kbId}/documents` | 批量创建文档 |
| GET | `/console/v1/knowledge-bases/{kbId}/documents` | 文档分页列表 |
| GET | `/console/v1/knowledge-bases/{kbId}/documents/{docId}` | 获取文档详情 |
| PUT | `/console/v1/knowledge-bases/{kbId}/documents/{docId}` | 更新文档 |
| DELETE | `/console/v1/knowledge-bases/{kbId}/documents/{docId}` | 删除文档 |
| DELETE | `/console/v1/knowledge-bases/{kbId}/documents/batch-delete` | 批量删除文档 |
| PUT | `/console/v1/knowledge-bases/{kbId}/documents/{docId}/re-index` | 重新索引文档 |

**POST `/console/v1/knowledge-bases/{kbId}/documents`**

- 入参：`CreateDocumentRequest { filePaths[], parseConfig, indexConfig }`
- 返回：`Result<List<String>>` — 文档 ID 列表

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/documents/{docId}/chunks` | 创建分块 |
| GET | `/console/v1/documents/{docId}/chunks` | 分块分页列表 |
| PUT | `/console/v1/documents/{docId}/chunks/{chunkId}` | 更新分块 |
| DELETE | `/console/v1/documents/{docId}/chunks/{chunkId}` | 删除分块 |
| DELETE | `/console/v1/documents/{docId}/chunks/batch-delete` | 批量删除分块 |
| POST | `/console/v1/documents/{docId}/chunks/preview` | 预览分块效果（不入库） |
| PUT | `/console/v1/documents/{docId}/chunks/update-status` | 批量更新分块状态 |

---

**Base path：** `/console/v1/models`、`/console/v1/providers`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/console/v1/models/{modelType}/selector` | 按类型获取可用模型分组列表 |
| GET | `/console/v1/models/enabled` | 获取已启用模型列表 |

**GET `/console/v1/models/{modelType}/selector`**

- 入参：`modelType` (path) — 如 `chat`、`embedding`
- 返回：`Result<List<ModelProviderGroup>>`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/providers` | 添加 Provider |
| GET | `/console/v1/providers` | Provider 列表 |
| GET | `/console/v1/providers/{provider}` | 获取 Provider 详情 |
| PUT | `/console/v1/providers/{provider}` | 更新 Provider |
| DELETE | `/console/v1/providers/{provider}` | 删除 Provider |
| GET | `/console/v1/providers/protocols` | 查询支持的协议列表 |
| POST | `/console/v1/providers/{provider}/models` | 为 Provider 添加模型 |
| GET | `/console/v1/providers/{provider}/models` | 查询 Provider 下的模型 |
| GET | `/console/v1/providers/{provider}/models/{modelId}` | 获取模型详情 |
| PUT | `/console/v1/providers/{provider}/models/{modelId}` | 更新模型配置 |
| DELETE | `/console/v1/providers/{provider}/models/{modelId}` | 删除模型 |
| GET | `/console/v1/providers/{provider}/models/{modelId}/parameter_rules` | 获取模型参数规则 |

---

**Base path：** `/console/v1/tools`、`/console/v1`（plugins）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/tools` | 创建工具 |
| GET | `/console/v1/tools` | 全量工具列表 |
| GET | `/console/v1/tools/page` | 工具分页列表 |
| GET | `/console/v1/tools/{id}` | 获取工具详情 |
| PUT | `/console/v1/tools/{id}` | 更新工具 |
| DELETE | `/console/v1/tools/{id}` | 删除工具 |
| GET | `/console/v1/tools/search` | 按名称搜索工具 |
| GET | `/console/v1/tools/plugin/{pluginId}` | 按插件 ID 查询工具 |
| PATCH | `/console/v1/tools/{id}/enabled` | 启用 / 禁用工具 |

**PATCH `/console/v1/tools/{id}/enabled`**

- 入参：`id` (path)，`?enabled=true/false`
- 返回：`Result<Void>`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/plugins` | 创建插件 |
| GET | `/console/v1/plugins` | 插件分页列表 |
| GET | `/console/v1/plugins/{pluginId}` | 获取插件详情 |
| PUT | `/console/v1/plugins/{pluginId}` | 更新插件 |
| DELETE | `/console/v1/plugins/{pluginId}` | 删除插件 |
| POST | `/console/v1/plugins/{pluginId}/tools` | 为插件添加工具 |
| GET | `/console/v1/plugins/{pluginId}/tools` | 插件工具列表 |
| GET | `/console/v1/plugins/{pluginId}/tools/{toolId}` | 获取插件工具详情 |
| PUT | `/console/v1/plugins/{pluginId}/tools/{toolId}` | 更新插件工具 |
| DELETE | `/console/v1/plugins/{pluginId}/tools/{toolId}` | 删除插件工具 |
| POST | `/console/v1/plugins/{pluginId}/tools/{toolId}/test` | 测试插件工具 |
| POST | `/console/v1/plugins/{pluginId}/tools/{toolId}/publish` | 发布插件工具 |
| POST | `/console/v1/tools/{toolId}/enable` | 启用工具 |
| POST | `/console/v1/tools/{toolId}/disable` | 禁用工具 |
| POST | `/console/v1/tools/query-by-ids` | 按 ID 批量查询工具 |

---

**Base path：** `/console/v1/mcp-servers`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/mcp-servers` | 注册 MCP Server |
| PUT | `/console/v1/mcp-servers` | 更新 MCP Server |
| GET | `/console/v1/mcp-servers` | MCP Server 分页列表 |
| GET | `/console/v1/mcp-servers/{serverCode}` | 获取 MCP Server 详情（含工具列表） |
| DELETE | `/console/v1/mcp-servers/{serverCode}` | 删除 MCP Server |
| POST | `/console/v1/mcp-servers/query-by-codes` | 按 code 批量查询 |
| POST | `/console/v1/mcp-servers/debug-tools` | 调试 MCP 工具调用 |

**POST `/console/v1/mcp-servers`**

- 入参：`McpServerDetail { code, name, url, transport, tools[],... }`
- 返回：`Result<String>` — serverCode

**POST `/console/v1/mcp-servers/debug-tools`**

- 入参：`McpServerCallToolRequest { serverCode, toolName, arguments }`
- 返回：`Result<McpServerCallToolResponse>` — `{ result, error }`

---

**Base path：** `/console/v1/agent-schemas`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/agent-schemas` | 创建 Agent Schema |
| GET | `/console/v1/agent-schemas` | 全量列表 |
| GET | `/console/v1/agent-schemas/page` | 分页列表 |
| GET | `/console/v1/agent-schemas/{id}` | 获取详情 |
| PUT | `/console/v1/agent-schemas/{id}` | 更新 |
| DELETE | `/console/v1/agent-schemas/{id}` | 删除 |
| GET | `/console/v1/agent-schemas/search` | 按名称搜索 |
| PATCH | `/console/v1/agent-schemas/{id}/enabled` | 启用 / 禁用 |

---

**Base path：** `/console/v1/files`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/files/upload` | 上传文件（服务端转存） |
| GET | `/console/v1/files/download` | 下载 / 预览文件 |
| POST | `/console/v1/files/upload-policies` | 获取前端直传 OSS 策略 |
| GET | `/console/v1/files/get-preview-url` | 获取文件预览链接 |

**POST `/console/v1/files/upload`**

- 入参：`multipart/form-data`，`files[]`（多文件），`category`（分类）
- 返回：`Result<List<UploadPolicy>>` — `{ url, key,... }`

**POST `/console/v1/files/upload-policies`**

- 入参：`WebUploadRequest { fileNames[], category }`
- 返回：`Result<List<WebUploadPolicy>>` — 前端直传 OSS 所需签名信息

**GET `/console/v1/files/download`**

- 入参：`?filePath=xxx&preview=true/false`
- 返回：文件字节流（`void`，直接写入 response）

---

**Base path：** `/console/v1/api-keys`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/api-keys` | 创建 API Key |
| GET | `/console/v1/api-keys` | 分页列表 |
| GET | `/console/v1/api-keys/{id}` | 获取详情 |
| PUT | `/console/v1/api-keys/{id}` | 更新 |
| DELETE | `/console/v1/api-keys/{id}` | 删除 |

**POST `/console/v1/api-keys`**

- 入参：`ApiKey { name, expireAt,... }`
- 返回：`Result<String>` — 生成的 key 值（仅此次可见）

---

**Base path：** `/console/v1/workspaces`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/console/v1/workspaces` | 创建工作空间 |
| GET | `/console/v1/workspaces` | 分页列表 |
| GET | `/console/v1/workspaces/{workspaceId}` | 获取详情 |
| PUT | `/console/v1/workspaces/{workspaceId}` | 更新 |
| DELETE | `/console/v1/workspaces/{workspaceId}` | 删除 |

---

**Base path：** `/console/v1/component-servers`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/console/v1/component-servers` | 组件分页列表 |
| GET | `/console/v1/component-servers/app-publishable` | 可发布应用分页列表 |
| POST | `/console/v1/component-servers` | 发布应用为组件 |
| PUT | `/console/v1/component-servers/{code}` | 更新组件 |
| DELETE | `/console/v1/component-servers/{code}` | 删除组件 |
| GET | `/console/v1/component-servers/{code}/detail-by-code` | 按 code 获取组件详情 |
| GET | `/console/v1/component-servers/{appId}/detail-by-appid` | 按 appId 获取组件详情 |
| GET | `/console/v1/component-servers/{code}/query-refer` | 查询引用关系 |
| GET | `/console/v1/component-servers/{appId}/query-config` | 查询组件配置 |
| POST | `/console/v1/component-servers/query-by-codes` | 按 code 批量查询 |
| GET | `/console/v1/component-servers/{code}/query-schema` | 获取组件 Schema |
| POST | `/console/v1/component-servers/schema-by-codes` | 按 code 批量获取 Schema |

---

**Base path：** `/api/v1/apps`

> 供外部 Agent 应用调用的标准对话接口。

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/apps/chat/completions` | Agent 对话（流式 / 非流式） |
| POST | `/api/v1/apps/workflow/completions` | 工作流同步执行 |
| POST | `/api/v1/apps/workflow/async-completions` | 工作流异步执行 |
| POST | `/api/v1/apps/workflow/stop-completions` | 停止异步任务 |
| POST | `/api/v1/apps/workflow/async-results` | 查询异步执行结果 |

**POST `/api/v1/apps/chat/completions`**

- 入参：`AgentRequest { appId, messages[], stream, model,... }`，`HttpServletResponse`
- 返回：SSE 流（`stream=true`）或 JSON

**POST `/api/v1/apps/workflow/async-completions`**

- 入参：`WorkflowRequest { appId, inputs,... }`
- 返回：`Result<TaskRunResponse>` — `{ taskId }`

**POST `/api/v1/apps/workflow/async-results`**

- 入参：`AsyncResultRequest { taskId }`
- 返回：`Result<AsyncResultResponse>` — `{ status, outputs, error }`

---

**Base path：** `/oauth2`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/oauth2/callback/github` | GitHub OAuth 回调，完成登录 |

**GET `/oauth2/callback/github`**

- 入参：`?code=xxx`（GitHub 回调 code）

---

**Base path：** `/console/v1/system`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/console/v1/system/global-config` | 获取系统全局配置 |
| GET | `/console/v1/system/health` | 健康检查 |

**GET `/console/v1/system/global-config`**

- 入参：无
- 返回：`Result<GlobalConfig>` — 前端所需全局配置项

**GET `/console/v1/system/health`**

- 入参：无
- 返回：`"ok"`（纯字符串）

---

**Base path：** `/graph-studio/api`

| 方法 | 路径 | 说明 |
|------|------|------|
| — | `/graph-studio/api/app/**` | Graph 应用管理（实现 AppAPI 接口） |
| — | `/graph-studio/api/dsl/**` | DSL 导入导出（实现 DSLAPI 接口） |
| — | `/graph-studio/api/run/**` | 运行 Graph（实现 RunnerAPI 接口） |
| POST | `/starter.zip` 等 | 代码工程下载（继承 Spring Initializr） |

> 此模块基于 Spring Initializr 框架扩展，具体路由由框架约定，接收 `GraphProjectRequest` 生成 Spring AI Alibaba 工程骨架。
```

### 4.4 三个常见坑

下面三个坑是我在多个项目上反复踩过的，提前知道能少跑几轮返工。

#### (1) 第一个坑：AI 漏扫多模块 Controller

Spring AI Alibaba Admin 的 Controller 分散在 `server-core`、`server-openapi`、`server-runtime` 几个模块里。<span style="color: red; font-weight: bold;">你要在提示词里顺手提一句「项目是多模块的，每个 server 子模块下都可能有 Controller」</span>。第一版出来一旦发现数量少得可疑，直接问一句「你扫了几个模块？有没有漏？」，AI 通常会乖乖补回来。

传统单体项目一个 `src/main/java` 一扫到底，多模块项目不一样——Maven 多模块的每个 `module` 都有自己的 `@RestController`，AI 默认只看主模块时会漏一大半。这是「我的看法」：扫完第一件事不是看清单细节，而是先看 Controller 总数对不对得上。

#### (2) 第二个坑：AI 把内部 RPC 接口和 REST 接口混在一起

Spring AI Alibaba Admin 里有些是对外 REST，有些是给 SDK 或 Agent 用的内部接口。<span style="color: red; font-weight: bold;">让 AI 在清单里区分「对外」和「内部」两类</span>，避免读者把它当成对外承诺去调用。

你可以问 AI：「这份清单里哪些是给外部用户调用的，哪些是 SDK / Agent 内部用的？」让它单独标出来。否则后面团队协作时，有人照着清单把内部接口当成对外契约用，重构一改就出事。

#### (3) 第三个坑：返回结构写得太粗

AI 有时候写「返回 Prompt 对象」就完事了。这不够——<span style="color: red; font-weight: bold;">至少要告诉你「返回一个 Prompt 列表还是单个 Prompt，有没有包装成 </span>**`Result<>`** <span style="color: red; font-weight: bold;">这种统一响应结构」</span>。不够的话让它再细化一层。

这里的关键是「主要结构」和「字段细节」的边界。统一返回结构 `Result<T>` 这种包装是契约的一部分，必须写清；但 `Result` 内部三个字段、`Prompt` 对象的十几个字段，则交给下一篇的数据模型文档去展开。两份资产各司其职，接口清单只回答「调谁、传什么、返回什么形状」，不回答「字段长什么样」。

## 5. 实战二：Spring AI Alibaba Admin 数据模型

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/214ef4999eb9eaa0c3781a11a6f1fbdb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接口清单住进 `docs/` 之后，把第二份资产也搬进去——核心数据模型。做法很简单：让 AI 综合看 entity 类、DTO、建表 SQL 三份材料，产出一份 Markdown 说明加一张 ER 图。前者像传统 DBA 写的数据字典，后者像老项目里 DBA 画的表关系图，两份一起才完整。

### 5.1 提示词原文

```
看项目的 entity 类、DTO、数据库建表 SQL，给我梳理核心数据模型。
每个模型列出字段、类型、一句话说明。标出主键、外键、枚举值。
关键模型之间的关系画一张简单的 ER 图。保存到 docs/data-model.md 和 docs/data-model-er.svg。
```

### 5.2 三个关键点回顾

这三个点其实就是第 3 章方法论在主线项目上的落地，这里只做提醒，不展开。

#### (1) 关键点一：三个数据源一起看

<span style="color: red; font-weight: bold;">entity（Java 层 model）、DTO（传输层 model）、建表 SQL（DB 层 model）三者从来不会完全一致</span>。类比传统项目，这就像 ORM 实体类、对外接口契约、DBA 建表脚本三套东西各管一段。entity 有的字段 DTO 不一定暴露，DTO 有的字段可能是两个 entity 的拼装。<span style="color: red; font-weight: bold;">所以要让 AI 三边对照，而不是只盯一处。</span>

#### (2) 关键点二：标出主键、外键、枚举值

这三个是数据模型里最硬的信息：

* 主键告诉你一条记录怎么定位
* 外键告诉你表之间怎么关联
* 枚举告诉你字段取值范围（比如 Prompt 的状态、Experiment 的运行状态）。

我的看法是，<span style="color: red; font-weight: bold;">这三个字段在后续改造里最容易踩坑</span>，<span style="color: red; font-weight: bold;">AI 必须显式标出来，不能含糊</span>。

#### (3) 关键点三：Markdown 说明和 ER 图同时产出

| 产物          | 适合什么场景                                        |
| ----------- | ------------------------------------------------- |
| Markdown 说明 | <span style="color: red; font-weight: bold;">精确查找（PromptTemplate 表有哪些字段）</span> |
| ER 图        | <span style="color: red; font-weight: bold;">整体把握（几个表怎么串起来）</span>                       |

两份产物各司其职，缺一不可。这跟传统项目里数据字典和 ER 图是同一套路。

### 5.3 产出效果：ER 图

下面这张图就是 AI 跑完提示词后产出的 ER 图，覆盖了 Prompt、Dataset、Evaluator、Experiment、Trace 等核心实体，可以拿来对照自己的产出。

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/5fe34b6475502d896a32eb9384efb492_MD5.jpg" style="display: block; width: 800px;" alt="ER 图">

<!--
图片内容说明
路径：imgs/aicmigr-09-proj-rd-04-iface-and-data-model/5fe34b6475502d896a32eb9384efb492_MD5.jpg
用途：展示数据模型 ER 图的最终产出形态，供读者对照自己的产出
内容：Spring AI Alibaba Admin 核心数据模型 ER 图，覆盖 Prompt、Dataset、Evaluator、Experiment、Trace 等核心实体，展示主键、外键关联与枚举字段
-->

### 5.4 三个常见坑

#### (1) 第一个坑：AI 只看 entity，忽略建表 SQL

| 症状               | 原因                                                             | 对策                                                                                  |
| ---------------- | -------------------------------------------------------------- | ----------------------------------------------------------------------------------- |
| 拿到的字段和 DB 实际表对不上 | JPA 的 `@Transient` 字段在 entity 里有、DB 里没有；反过来 DB 也有字段没映射进 entity | <span style="color: red; font-weight: bold;">让 AI 以 DB 层为准，entity 和 DTO 只作参照</span> |

你可能会问，为什么不让 AI 直接看 entity 就行？因为 entity 是给 Java 代码用的，DB 才是数据真相。<span style="color: red; font-weight: bold;">改造时如果按 entity 建索引、改字段，很可能撞上 DB 里压根没有的那一列。</span>

#### (2) 第二个坑：AI 把 DTO 和 entity 混成一个说明

这两种 model 的职责完全不同：

| 模型     | 职责         |
| -------- | ------------ |
| entity   | 持久层的映射 |
| DTO      | 传输层的契约 |

放一起说读者会晕 —— 这就好比把数据库表结构和 HTTP 接口返回字段写进同一张表。我的对策很直接：<span style="color: red; font-weight: bold;">让 AI 分开，entity 一份、DTO 一份，各自说清楚职责。</span>

#### (3) 第三个坑：ER 图里容易漏关系

有些关系<span style="color: red; font-weight: bold;">不走外键，而是靠业务代码维护的"逻辑关联"</span>。比如某个表里有 `prompt_id` 字段，但 DB 没建外键约束，AI 只看 DDL 就会漏掉。<span style="color: red; font-weight: bold;">我的做法是让 AI 除了扫 DDL，也扫一下代码里 `findBy*` 之类的查询方法，把隐式关系补进 ER 图。</span>这一步不加，后面做改造分析的时候关系就会断链。

## 6. 两份资产互相校对

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/6260df336864afe4c8bb2c8f5e74597a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

两份资产都做完，还差最后一步：**互相校对**。这一步是资产可信的前提，也是后续 SKILL 模板的雏形。

### 6.1 为什么要校对

接口清单和数据模型这两份资产之间应该是自洽的。你可以把它们想成传统软件工程里的**接口契约与实现**：接口清单是契约，数据模型是实现，<span style="color: red; font-weight: bold;">两边对不上，就是契约破裂。</span>

#### (1) 自洽是资产可信的前提

举个最常见的场景：接口清单里某个 API 返回 `PromptTemplate`，但你翻遍数据模型，根本找不到 `PromptTemplate` 这个实体。这时只有两种可能——要么接口清单记错了（这个 API 根本不返回它），要么数据模型漏了（实体确实存在但没被梳理进来）。

**两份资产只要有一份错，整份资产的可信度就坍塌**。你后续拿它们做改造决策，等于在错地图上找路。

#### (2) 漂移的两种典型表现

这种不自洽在 AI 梳理阶段经常出现，根源是两份资产是分两次生成的，AI 各跑一遍，版本容易错位。

| 漂移方向   | 典型表现                                |
| ------ | ----------------------------------- |
| <span style="color: red; font-weight: bold;">接口清单滞后</span> | 清单里保留了 refactor 之前的旧类名，数据模型里已经换成新名字 |
| <span style="color: red; font-weight: bold;">数据模型滞后</span> | 数据模型漏掉了某个实体，但接口清单里仍有 API 在返回它       |

### 6.2 校对提示词原文

两份资产做完一定要互相对一下。让 AI 跑一次校对，提示词原文如下：

```
对照 docs/api-list.md 和 docs/data-model.md，看接口里提到的每个实体在数据模型里是不是都有定义。
有不一致的地方列出来。
然后验证不一致的地方并修复。
```

AI 会扫一遍两份资产，把不一致点列出来。**你再让它逐条验证并修复，修正完的两份资产才是可信的**。

### 6.3 这一步是 SKILL 模板的雏形

为什么要单独强调"互相校对"这个动作？因为资产之间的漂移不是一次性的——**每次任何一份资产更新，都可能重新引入不一致**。

类比传统工程：CI 流水线上通常会挂一个"契约校验任务"——只要接口或模型变了，就自动跑一次 diff，发现不匹配就 fail。<span style="color: red; font-weight: bold;">互相校对这个动作，本质上就是给项目资产挂了一条类似的流水线。</span>

后续讲 SKILL 模板时，这个"互相校对"动作会被固化成一个可复用的触发器：<span style="color: red; font-weight: bold;">每更新一份资产，就自动触发一次校对，防止资产之间慢慢漂移</span>。

## 7. 五份资产怎么用

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/8a81da748cf184e6c5d5daeacff1bc4d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

画出三张俯视图、做完两份清单，`docs/` 目录现在有五份东西。

### 7.1 docs 目录的五份资产

```
docs/
├── architecture.svg          ← 架构图
├── module-deps.svg           ← 模块图
├── external-deps.svg         ← 依赖图
├── api-list.md               ← 接口清单
└── data-model.md + data-model-er.svg  ← 数据模型
```

### 7.2 后续每个阶段都把它们当查询入口

这五份资产不是摆在目录里吃灰，**是后续每一个阶段的查询入口**：

| 阶段              | 会用到哪份资产     | 用法                                                     |
| --------------- | ----------- | ------------------------------------------------------ |
| <span style="color: red; font-weight: bold;">构建 CLAUDE.md 阶段</span> | 五份全部        | 把这五份资产引用进 CLAUDE.md，让 AI 每次启动都能快速定位项目的门面和根基            |
| <span style="color: red; font-weight: bold;">构建环境阶段</span>          | 依赖图         | 对照依赖图确认中间件是不是都启动了                                      |
| <span style="color: red; font-weight: bold;">建护栏阶段</span>           | 接口清单 + 数据模型 | 对照接口清单决定哪些接口要加集成测试；对照数据模型决定哪些表要加 characterization test |
| <span style="color: red; font-weight: bold;">做需求改造阶段</span>         | 接口清单 + 数据模型 | 选一个接口改时，第一件事就是翻接口清单看当前长什么样、翻数据模型看字段关系，再动手              |

### 7.3 摸清一个项目的核心产出

这五份资产就是摸清一个项目的核心产出。它们不需要多漂亮，**只需要够工程师和 AI 共同作为后续工作的输入就行**。

回头看本篇做的三件事：两份资产一起做（接口是门面、数据是根基，互相绑定）；让 AI 读真实文件（扫 Controller、读 entity/DTO/建表 SQL，不让它脑补）；做完互相校对（接口里提到的实体在数据模型里要能找到，反过来也一样）。<span style="color: red; font-weight: bold;">这三件事合起来，就是项目改造的导航地图</span>。配合后续要写的 CLAUDE.md，项目的"脑图"就基本成型。
