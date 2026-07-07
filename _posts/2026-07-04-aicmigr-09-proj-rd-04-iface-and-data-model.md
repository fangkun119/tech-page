---
title: 传统项目迁AI 09：了解项目 - 生成接口清单和数据模型
author: fangkun119
date: 2026-07-04 09:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
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
传统项目迁AI 09：了解项目 - 生成接口清单和数据模型
-->

## 1. 导读地图：本篇怎么读

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/1a23b41be7629b605713916122ada2ac_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是「了解项目」阶段的第四篇，对应八步心法的 **步 5：梳理接口和数据模型**。上一篇让 AI 画出了三张俯视图（架构图、模块图、依赖图），它们只回答了"项目长什么样"。本篇接着往里走一层，回答两个更关键的问题：**这个项目对外承诺了哪些接口、内部在处理什么数据**。

读完本篇，读者会拿到两份新的资产——一份 REST 接口清单和一份核心数据模型说明，并同时拿到一套"让 AI 一次性梳理出这两份资产"的可复用方法论（提示词模板 + 关键点 + 常见坑清单 + 互相校对动作）。这两份资产是后续每一篇改造工作的导航地图。

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/9b783b9df2264f41a957b0d0a4f75e51_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/09_了解项目_04：生成接口清单和数据模型/9b783b9df2264f41a957b0d0a4f75e51_MD5.jpg
用途：呈现八步心法整体流程，本篇对应步 5「梳理接口和数据模型」
内容：八步心法示意图，第 5 步被突出标注，承接上一讲的三张俯视图
-->

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/c88cfb5e5b5263a7258459803ea74bca_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    Start([本篇导读：接口清单 + 数据模型])
    Start --\> P1[第一部分 方法论提炼]
    Start --\> P2[第二部分 实战演示]

    P1 --\> P1A[2、为什么接口和数据模型<br/>要一起梳理]
    P1 --\> P1B[3、两份资产的方法论<br/>提示词/关键点/坑]
    P1 --\> P1C[4、项目 Check List<br/>可裁剪速查]

    P2 --\> P2A[5、主线项目延续<br/>Spring AI Alibaba Admin]
    P2 --\> P2B[6、资产 1：REST 接口清单]
    P2 --\> P2C[7、资产 2：核心数据模型]
    P2 --\> P2D[8、两份资产的互相校对]
    P2 --\> P2E[9、五份资产怎么用<br/>与后续衔接]
    P2 --\> P2F[10、小结与思考]

    P1A -.速查.-> Senior([熟练工程师<br/>快速回顾])
    P2A -.代入.-> Beginner([初学工程师<br/>系统掌握])
-->

两类读者的推荐读法：

- **初学 AI 编程工程师**：建议通读全篇。第一部分建立"接口+数据模型双资产"的认知骨架；第二部分用 Spring AI Alibaba Admin 把骨架落到真实企业级微服务上，把两份资产的提示词、产出效果、常见坑完整走一遍，最后落到"两份资产互相校对"这一步。
- **熟练 AI 编程工程师**：可只看第一部分的 Check List 速查；接到一个老项目时，按清单填提示词、按 review 表走一遍、再用本篇给的校对动作收尾即可。

## 2. 为什么接口和数据模型要一起梳理

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/4dea24f9fe13e530cadd9229be5c3505_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接口和数据模型看起来是两件事，但本篇坚持合在一起做。原因有四条，理解了这四条，就理解了为什么这两份资产是改造的导航地图。

### 2.1 接口是项目的门面

外部世界通过接口和这个项目打交道。前端调什么 API、三方集成请求什么端点、运维连什么管理接口，全是接口。**接口清单决定了"这个项目对外承诺了什么"**。

### 2.2 数据模型是项目的根基

接口处理的每一个请求、返回的每一份响应，背后都对应某些数据结构的流动。Prompt 对象长什么样、Dataset 的字段有哪些、Evaluator 的结果存成什么格式，这些是**项目的内部骨骼**。

### 2.3 两者绑定：接口的入参和返回 90% 是数据模型的映射

接口的参数和返回，90% 的情况下是数据模型的某种映射或变形。接口 `POST /api/prompts/create` 的请求体基本就是 Prompt 数据模型的子集，响应基本就是 Prompt 的一个视图。**接口和数据模型画不清楚，就看不出项目的真实形状**。

### 2.4 改造视角：每个新功能几乎都要同时动这三处

#### (1) 加一个新功能的三处改动

从改造的角度看，加一个新功能大概率要同时改三处：

| 改动点 | 内容 |
|-------|-----|
| 加接口 | 暴露一个新的端点供前端或外部调用 |
| 改数据模型 | 调整若干字段、加表或加列 |
| 改业务逻辑 | 把接口和数据模型串起来 |

#### (2) 没有两份资产的代价

接口清单和数据模型是改动的导航地图。**没有这两份资产，每次改造都要从零摸索**——每次都要回去翻 Controller、翻 entity、翻 SQL，AI 也无法基于一份共同记忆帮你做事。

所以本篇要让 AI 一次性把这两份都梳理出来。

## 3. 两份资产的方法论：提示词、关键点与坑

第二部分实战之前，先把方法论提炼出来。本节回答四个问题：两份资产各看哪些信息源？提示词怎么写？关键约束是什么？两份资产做完之后还要不要互相校对？

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/21def9e6af562f27a416f81f757b47f4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/042a01bd980949acf79ac41152c15911_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/7a8c0e43c4eefece15792f9392cbd2ff_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 3.1 两份资产各自的信息源

不让 AI 读真实代码就会脑补。两份资产必须分别指定明确的信息源：

| 资产 | 信息源 | 不让 AI 读会怎样 |
|------|-------|----------------|
| 接口清单 | 项目所有 Controller（多模块时每个 server 子模块都要扫） | AI 根据模块名瞎猜端点，遗漏整片接口 |
| 数据模型 | entity 类、DTO、数据库建表 SQL（三边对照） | AI 只看 entity，漏掉 DB 实际字段或 DTO 实际契约 |

### 3.2 接口清单的三条提示词约束

接口清单的提示词除了"扫所有 Controller"之外，还有三条关键约束决定了清单质量。

#### (1) 约束一：按模块分组

让清单有组织。Spring AI Alibaba Admin 有 Prompt、Dataset、Evaluator、Experiment、Trace 几大模块，每个模块下有若干接口。**分组的清单可读性远高于一张一百行的大表**。

#### (2) 约束二：每个接口给"一句话说明"

强制 AI 给出人类能看懂的意图，而不是只抄 `@Operation` 注解或方法名。接口叫 `createPromptTemplate`，一句话说明应该是"创建一个新的 Prompt 模板"，**让非开发同事也能看懂**。

#### (3) 约束三：列出主要入参和返回结构，但不展开所有字段

要求 AI 不只列端点，还要列参数类型和返回类型，但**不要展开所有字段，只要主要的**。字段细节交给数据模型那份文档，两份资产各司其职。

### 3.3 数据模型的三条提示词约束

数据模型的提示词要求 AI 同时看三个数据源，并产出 Markdown 说明加一张 ER 图。

#### (1) 约束一：三个数据源一起看

| 数据源 | 层次 | 职责 |
|-------|------|-----|
| entity 类 | Java 持久层 | entity ↔ DB 表的映射 |
| DTO | 传输层 | 接口请求/响应的契约 |
| 建表 SQL | DB 层 | 数据库实际表结构 |

三者不完全一致是常态：entity 有的字段 DTO 里不暴露，DTO 有的字段是两个 entity 的组合。**让 AI 三边对照**。

#### (2) 约束二：标出主键、外键、枚举值

这三个是数据模型的硬信息：

| 信息 | 作用 |
|-----|-----|
| 主键 | 每个表怎么定位一条记录 |
| 外键 | 表之间怎么关联 |
| 枚举值 | 字段取值范围（如 Prompt 状态、Experiment 运行状态） |

**这三个是改造时最容易踩坑的地方**。

#### (3) 约束三：同时产出 Markdown 说明 + ER 图

让 AI 同时产出两份产物：

| 产物 | 适合什么场景 |
|------|------------|
| Markdown 说明 | 精确查找（PromptTemplate 表有哪些字段） |
| ER 图 | 整体把握（这几个表是怎么关联的） |

### 3.4 两份资产之间必须互相校对

两份资产做完一定要互相对一下。**接口清单和数据模型之间应该是自洽的**。

#### (1) 不自洽的典型表现

| 不自洽的表现 | 可能的原因 |
|------------|----------|
| 接口清单里某个 API 返回 `PromptTemplate`，但数据模型里找不到这个实体 | AI 在接口清单里保留了老的类名，数据模型用了 refactor 之后的新名字 |
| 数据模型里有 `PromptTemplate`，但接口清单里没有任何 API 引用它 | 接口清单漏扫，或这个实体已经被废弃 |

#### (2) 校对是 SKILL 的雏形

这个"互相校对"的动作，在系列第 11 篇 SKILL.md 里会固化成一个可复用的模板——**每次更新任何一份资产都触发一次校对，防止资产之间慢慢漂移**。

### 3.5 三类常见坑的清单速览

把第二部分实战中出现的坑提炼出来，作为方法论速查。第二部分会逐个展开。

| 资产 | 常见坑 | 应对 |
|------|-------|-----|
| 接口清单 | 漏扫多模块 Controller | 提示词里点明"项目是多模块的，每个 server 子模块下都可能有 Controller" |
| 接口清单 | 把内部 RPC 接口和 REST 接口混在一起 | 让 AI 在清单里区分"对外"和"内部"两类 |
| 接口清单 | 返回结构写得太粗（只写"返回 Prompt 对象"） | 至少要写"是单个还是列表、有没有 `Result<>` 包装" |
| 数据模型 | 只看 entity，忽略建表 SQL | 让 AI 以 DB 层为准，entity 和 DTO 作为参照 |
| 数据模型 | DTO 和 entity 混成一个说明 | 让 AI 分开，entity 一份、DTO 一份 |
| 数据模型 | ER 图漏关系（只看 DDL） | 让 AI 也扫 `findBy*` 等查询方法，把隐式关系补上 |

## 4. 第一部分 Check List：接口清单 + 数据模型速查

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/7e562f56095e75ea076729f63a493989_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节提供一份可裁剪、可勾选的 Check List，供工程师在梳理这两份资产的不同阶段快速查阅。读者可以按项目实际情况裁剪后贴到自己的 `CLAUDE.md` 或工作笔记里。

### 4.1 启动前的 Check List

启动 AI 梳理之前，逐条自检：

- [ ] 是否已经画完三张俯视图（架构图、模块图、依赖图），让 AI 对项目形状有基础认知？
- [ ] 是否确认了项目是多模块结构，知道每个 server 子模块里都可能藏着 Controller？
- [ ] 是否约定好产物存放在 `docs/` 目录下（`docs/api-list.md`、`docs/data-model.md`、`docs/data-model-er.svg`）？
- [ ] 是否想清楚接口清单和数据模型要一起做（而不是分两次）？

### 4.2 接口清单的 Check List

提示词和产出环节逐条自检：

- [ ] 提示词里是否明确"扫所有 Controller"且点明了多模块？
- [ ] 是否要求"按模块分组"组织清单？
- [ ] 是否要求每个接口给"一句话说明"（人类能看懂意图）？
- [ ] 是否要求列主要入参和返回结构，但不要展开所有字段？
- [ ] 第一版出来后，是否对照"Controller 数量少得可疑"做了一次 sanity check？
- [ ] 是否让 AI 区分"对外 REST"和"内部 RPC"两类接口？
- [ ] 返回结构是否细化到"单个/列表/有无 `Result<>` 包装"？

### 4.3 数据模型的 Check List

提示词和产出环节逐条自检：

- [ ] 提示词里是否同时指定 entity 类、DTO、建表 SQL 三个数据源？
- [ ] 是否要求"标出主键、外键、枚举值"？
- [ ] 是否要求 entity 和 DTO 分开说明，不混在一起？
- [ ] 是否要求以 DB 层为准（DB 里有但 entity 里没有的字段不能漏）？
- [ ] 是否要求同时产出 Markdown 说明 + ER 图？
- [ ] ER 图是否扫了 `findBy*` 之类的查询方法，把隐式关系补上？

### 4.4 两份资产互相校对的 Check List

两份资产都做完后，逐条自检：

- [ ] 是否让 AI 做了一次"接口清单 ↔ 数据模型"的不一致扫描？
- [ ] 接口里提到的每个实体，是否都能在数据模型里找到定义？
- [ ] 不一致点是否验证后修复（而不是直接采信 AI 的一面之词）？
- [ ] 校对动作是否记下来，准备后续在 SKILL.md 里固化成模板？

## 5. 实战主线延续：Spring AI Alibaba Admin

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/4f624a7edc2baa3675386dbc9be4c68e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

说完方法论，进入第二部分实战。本篇延续上一篇的主线项目——**Spring AI Alibaba Admin**，所有出现的提示词、产出的清单、沉淀的文档都围绕它展开。读者跟着一起操作会收获最大。

### 5.1 主线延续：从画图到梳理接口和数据模型

上一篇画完了三张俯视图，系统级有了架构图、代码级有了模块图、生态级有了依赖图。三种粒度的俯视摆在 `docs/` 里，工程师和 AI 对这个项目有了第一层共识——**俯视告诉你"项目长什么样"，但它没告诉你"项目怎么被调用"和"项目在处理什么数据"**，而这就是本篇要讲的内容。

### 5.2 本篇产出物：住进 docs/ 的两份新资产

梳理完本篇会拿到两份新的资产：**一份 REST 接口清单**和**一份核心数据模型说明**（外加一张 ER 图），都住进 `docs/` 里。加上上一篇的三张图，`docs/` 目录就攒齐了五份资产，构成了项目的"脑图骨架"。

### 5.3 本篇只读代码，不跑项目

延续上一篇的约定：本篇仍然只让 AI 读代码、产出文档。把"让项目跑起来"这件事留到系列第 13 篇（构建护栏阶段）一次性讲透。

## 6. 资产 1：REST 接口清单实战

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/964874447674e3bcd1990d62c7e030e2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把方法论的提示词套到 Spring AI Alibaba Admin 上，让 AI 一次性产出接口清单。本节给出提示词（code block 原文不改写）、关键点说明、产出效果（节选）、三类常见坑。读者可以直接复制提示词到自己的项目运行。

### 6.1 提示词

提示词：

```
扫一下这个项目里所有的 Controller，给我整理一份 REST 接口清单。
每个接口列出方法（GET/POST 等）、路径、一句话说明、主要入参、返回结构。
按模块分组。保存到 docs/api-list.md。
```

### 6.2 三个关键点

#### (1) 关键点一：按模块分组是清单可读性的前提

Spring AI Alibaba Admin 有 Prompt、Dataset、Evaluator、Experiment、Trace 几大模块，每个模块下有若干接口。**分组的清单可读性远高于一张一百行的大表**。

#### (2) 关键点二："一句话说明"强制人类意图

强制 AI 给出人类能看懂的意图，而不是只抄 `@Operation` 注解或方法名。接口叫 `createPromptTemplate`，一句话说明应该是"创建一个新的 Prompt 模板"，**让非开发同事也能看懂**。

#### (3) 关键点三：主要入参和返回结构，不展开所有字段

要求 AI 不只列端点，还要列参数类型和返回类型，但**不要展开所有字段，只要主要的**。字段细节交给数据模型那份文档，两份资产各司其职。

### 6.3 产出效果（节选）

下面是 AI 跑出来的清单内容（节选，完整内容约 500 行，覆盖 32 个 Controller、22 大模块）。读者可以用它对照自己跑出来的版本是否对齐。

```
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

### 6.4 三个常见坑

#### (1) 第一个坑：AI 漏扫多模块 Controller

Spring AI Alibaba Admin 的 Controller 分散在 `server-core`、`server-openapi`、`server-runtime` 几个模块里。**你要在提示词里顺手提一句"项目是多模块的，每个 server 子模块下都可能有 Controller"**。第一版出来一旦发现数量少得可疑，直接问一句"你扫了几个模块？有没有漏？"，AI 会补回来。

#### (2) 第二个坑：AI 把内部 RPC 接口和 REST 接口混在一起

Spring AI Alibaba Admin 里有些是对外 REST，有些是给 SDK 或 Agent 用的内部接口。**让 AI 在清单里区分"对外"和"内部"两类**，避免读者把它当成对外承诺去调用。

#### (3) 第三个坑：返回结构写得太粗

AI 有时候写"返回 Prompt 对象"就完事了。这不够，**至少要告诉你"返回一个 Prompt 列表还是单个 Prompt，有没有包装成 `Result<>` 这种统一响应结构"**。不够的话让它再细化一层。

## 7. 资产 2：核心数据模型实战

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/6eb1e88809c25228e7bbd6c9c50072a8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接着让 AI 综合看 entity、DTO、建表 SQL，产出数据模型说明和一张 ER 图。本节给出提示词、三个关键点、ER 图产出效果、三类常见坑。

### 7.1 提示词

提示词：

```
看项目的 entity 类、DTO、数据库建表 SQL，给我梳理核心数据模型。
每个模型列出字段、类型、一句话说明。标出主键、外键、枚举值。
关键模型之间的关系画一张简单的 ER 图。保存到 docs/data-model.md 和 docs/data-model-er.svg。
```

### 7.2 三个关键点

#### (1) 关键点一：三个数据源一起看

entity 类（Java 层的 model）、DTO（传输层的 model）、建表 SQL（DB 层的 model）三者不完全一致是常态。比如 entity 有的字段 DTO 里不暴露，DTO 有的字段是两个 entity 的组合。**让 AI 三边对照**。

#### (2) 关键点二：标出主键、外键、枚举值

这三个是数据模型的硬信息。主键告诉你每个表怎么定位一条记录，外键告诉你表之间怎么关联，枚举告诉你某些字段的取值范围（比如 Prompt 的状态、Experiment 的运行状态）。**这三个是改造时最容易踩坑的地方**。

#### (3) 关键点三：同时产出 Markdown 说明和 ER 图

让 AI 同时产出两份产物。前者适合精确查找（PromptTemplate 表有哪些字段），后者适合整体把握（这几个表是怎么关联的）。两份产物各司其职，缺一不可。

### 7.3 产出效果：ER 图

生成的 ER 图如下：

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/5fe34b6475502d896a32eb9384efb492_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/09_了解项目_04：生成接口清单和数据模型/5fe34b6475502d896a32eb9384efb492_MD5.jpg
用途：展示数据模型 ER 图的最终效果，作为读者产出对照
内容：Spring AI Alibaba Admin 核心数据模型 ER 图，展示 Prompt、Dataset、Evaluator、Experiment、Trace 等核心实体之间的外键关联与枚举字段
-->

### 7.4 三个常见坑

#### (1) 第一个坑：AI 只看 entity，忽略建表 SQL

这样拿到的字段可能和数据库实际表不一致。比如 JPA 的 `@Transient` 字段在 entity 里有、DB 里没有，反过来 DB 有一些字段没映射到 entity 里。**让 AI 以 DB 层为准，entity 和 DTO 作为参照**。

#### (2) 第二个坑：AI 把 DTO 和 entity 混成一个说明

这两种 model 的职责完全不同：entity 是持久层的映射，DTO 是传输层的契约。放一起说你会晕。**让 AI 分开，entity 一份、DTO 一份，分别说清楚**。

#### (3) 第三个坑：ER 图里容易漏关系

有些关系不通过外键表达，是通过业务代码维护的"逻辑关联"（比如 `prompt_id` 是某个字段，但 DB 里没建外键约束）。**让 AI 除了看 DDL，也扫一下代码里 `findBy*` 之类的查询方法，把隐式关系补上**。

## 8. 两份资产的互相校对

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/4e89aec3b693da50e505f28d4399bb94_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

两份资产都做完，还差最后一步：**互相校对**。这一步是资产可信的前提，也是后续 SKILL 模板的雏形。

### 8.1 为什么要校对

接口清单和数据模型这两份资产之间应该是自洽的。如果发现接口清单里某个 API 返回 `PromptTemplate`，但数据模型里根本找不到 `PromptTemplate` 这个实体，**说明两份资产有一份是错的**。

### 8.2 不自洽的根源：AI 各版本梳理时的漂移

这种不自洽在 AI 梳理的时候经常出现：

| 漂移方向 | 表现 |
|---------|-----|
| 接口清单滞后 | 接口清单里保留了老的类名，但数据模型里用了 refactor 之后的新名字 |
| 数据模型滞后 | 数据模型里漏掉了某个实体，但接口清单里仍有 API 在返回它 |

### 8.3 校对的提示词

两份资产做完一定要互相对一下。让 AI 做一次校对：

```
对照 docs/api-list.md 和 docs/data-model.md，看接口里提到的每个实体在数据模型里是不是都有定义。
有不一致的地方列出来。
然后验证不一致的地方并修复。
```

### 8.4 校对的产出：一份修正后可信的资产

AI 会扫一遍，列出不一致点。**你再让它修正，修正完的两份资产才是可信的**。这个"互相校对"的动作在系列第 11 篇 SKILL.md 会固化成一个可复用的模板——**到时候每次更新任何一份资产都触发一次校对，防止资产之间慢慢漂移**。

## 9. 五份资产怎么用

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/4f5dc2495bb6855bda808df23775121d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

画完上一篇的三张图、做完本篇的两份清单，`docs/` 目录现在有五份东西。

### 9.1 五份资产的目录结构

```
docs/
├── architecture.svg          ← 架构图
├── module-deps.svg           ← 模块图
├── external-deps.svg         ← 依赖图
├── api-list.md               ← 接口清单
└── data-model.md + data-model-er.svg  ← 数据模型
```

### 9.2 五份资产是后续每一篇的查询入口

这五份资产不是摆在目录里吃灰，**是后面每一讲的查询入口**：

| 后续篇目 | 会用到哪份资产 | 用法 |
|---------|-------------|-----|
| 系列第 10 篇（CLAUDE.md） | 五份全部 | 把这五份资产引用进 CLAUDE.md，让 AI 每次启动都能快速定位项目的门面和根基 |
| 系列第 13 篇（构建环境） | 依赖图 | 对照"依赖图"确认中间件是不是都启动了 |
| 系列第 14 篇（建护栏） | 接口清单 + 数据模型 | 对照"接口清单"决定哪些接口要加集成测试；对照"数据模型"决定哪些表要加 characterization test |
| 第四部分（做需求改造） | 接口清单 + 数据模型 | 选一个接口改时，第一件事就是翻"接口清单"看当前长什么样、翻"数据模型"看字段关系，再动手 |

### 9.3 摸清一个项目的核心产出就是这五份资产

它们不需要多漂亮，**只需要够工程师和 AI 共同作为后续工作的输入就行**。

## 10. 小结与思考

<img src="imgs/aicmigr-09-proj-rd-04-iface-and-data-model/636e7b142eb555b402cadec9981aa053_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 10.1 小结

本篇做了三件事。

#### (1) 第一件：产出了两份新资产

REST 接口清单和核心数据模型说明。两份资产要一起做，因为接口是项目的门面、数据是项目的根基，**它们互相绑定**。接口的参数返回是数据模型的映射，数据模型的变化会倒逼接口变化。做改造的时候这两份资产是导航地图。

#### (2) 第二件：给了一套可复用的提示词

关键是让 AI 读真实文件。**接口清单让它扫所有 Controller，数据模型让它综合看 entity、DTO、建表 SQL。不让它读代码就会脑补**。提示词不堆格式化要求，只把信息源、约束、输出位置说清楚。

#### (3) 第三件：给了一个"互相校对"的收尾动作

做完互相校对。接口里提到的实体在数据模型里要能找到，反过来也一样。**这个自洽性是资产可信的前提**。这个动作会后续在 SKILL.md 里固化为可复用模板。

#### (4) 五份资产到齐，项目脑图成型

到这里 `docs/` 里已经有五份资产了。加上下一篇要写的 CLAUDE.md，**项目的"脑图"就基本成型**。

### 10.2 思考

#### (1) 关于手上项目的复盘

读者手上项目的 Controller 如果让 AI 扫一遍，估计能扫出多少个接口？**自己能说清楚的有几个？差距在哪里？**这个差距就是团队当前接口治理水平的真实写照。

#### (2) 关于团队协作的检验

团队现在有没有一份类似的"接口清单"文档？如果有，多久更新一次？如果没有，团队是怎么回答"我们有哪些对外接口"这个问题的？
