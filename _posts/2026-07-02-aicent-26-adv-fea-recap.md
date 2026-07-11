---
title: AI编程方法 26：高级功能 - 流程回顾
author: fangkun119
date: 2026-07-02 18:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-26-adv-fea-recap/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---

<!--
aicent-26-adv-fea-recap
AI编程方法 26：高级功能 - 流程回顾
-->

## 1. 本文导读地图

<img src="imgs/aicent-26-adv-fea-recap/2dc0c5f8d3c6d01ee4bc5c108029f2d4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本文是核心功能高阶篇的实操回顾，复盘 Hify 项目（智能客服 AI 应用平台）落地「工作流编排引擎」的全过程。内容按「方法论提炼」与「实战演示」两部分组织：方法论部分不绑定具体技术栈、可迁移到任何 AI 编程子系统；实战部分紧扣 Hify 项目、Spring、sealed interface/record、NodeExecutor、ExecutionContext、workflow_node_run 等技术栈，复现从概念建立到引擎跑通的完整链路。

<img src="imgs/aicent-26-adv-fea-recap/326a27bdb5ca2b8711e7b73d65bc7e30_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
\`\`\`mermaid
flowchart TD
    Start([本文导读]) --\> Q{阅读目标?}
    Q --\>|掌握方法论 / 快速复习| A1[2、背景与目标]
    Q --\>|完整复现项目 / 深入理解| B1[5、场景一 概念探路]

    subgraph 第一部分[第一部分 方法论提炼 - 参考手册风]
        A1
        A2[3、五条核心动作]
        A3[4、Check List]
        A1 --\> A2
        A2 --\> A3
    end

    subgraph 第二部分[第二部分 实战演示 - 实战教材风]
        B1
        B2[6、场景二 元数据 CRUD]
        B3[7、场景三 执行引擎]
        B4[8、场景四 接入对话引擎]
        B1 --\> B2
        B2 --\> B3
        B3 --\> B4
    end

    A3 -.-> D[9、总结]
    B4 -.-> D
\`\`\`
-->

| 读者类型 | 推荐阅读路径 | 预期收益 |
|----------|--------------|----------|
| 初学 AI 编程工程师 | 第 1 章 → 第 2 章 → 第 5~8 章（实战）→ 第 3~4 章（方法论沉淀） | 系统掌握从概念到落地的完整流程，理解每一步 why |
| 熟练 AI 编程工程师 | 第 1 章 → 第 3~4 章（方法论 + Check List速查）→ 按需查阅第 5~8 章任一场景 | 快速回顾方法论、获取可裁剪 Check List |

## 2. 背景与目标：Hify 工作流引擎要解决什么

<img src="imgs/aicent-26-adv-fea-recap/3aa9aa5933a66a9f4b41501fe997308e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 要解决什么问题

Hify 是一个 AI 应用平台，其智能客服在引入工作流引擎之前采用「一个 Prompt 通吃所有问题」的模式。这种模式在面对需要访问业务数据、需要分路径处理的场景时会失灵：

#### (1) 单 Prompt 与工作流引擎的对比

##### ① 单 Prompt 的局限

单 Prompt 让 LLM 凭自身知识「编答案」，适合通用知识类问题（如「退货政策是什么」），但面对「我的订单到哪了」「耳机坏了怎么保修」这类需要查数据库、需要走不同业务路径的问题，无法精准回答。

##### ② 工作流引擎的诉求

需要把任务拆成有序步骤：先判断用户意图，再走不同路径精准回答。每一步做一件具体的事，节点间可传递上下文，执行过程可观测。

### 2.2 Hify 做了什么

在第 22~23 篇，作者给 Hify 加上了工作流引擎，让智能客服从「一个 Prompt 通吃」变成「先判意图，再走不同路径」。本文聚焦工作流编排，从理解概念到引擎跑通，完整复盘全过程。

### 2.3 真实开发体验

工作流是本系列里作者花时间最多的模块，原因不在于代码难写，而在于概念需要消化。围绕「快、慢、惊喜」三个视角，可以提炼出后续第 3 章的方法论。

#### (1) 快在哪里

##### ① 元数据 CRUD

三张表（`workflow`、`workflow_node`、`workflow_edge`）结构清晰，创建时拆分写入、查询时组装还原，节奏与之前做知识库管理一致。Claude Code 读完现有代码风格后，生成的代码与项目一致性很高，基本不用大改。

##### ② NodeExecutor 体系

与第 14 篇 Provider 适配层是同一个模式：统一接口 + 按 type 分发 + Spring 自动注册。Claude Code 在「已有模式的复用」上表现很好，甚至主动提出用 `sealed interface + record` 做类型安全解析。

#### (2) 慢在哪里

##### ① 概念建立阶段

工作流是作者此前未做过的子系统，一开始脑子里没有完整的图：节点是什么、连线是什么、执行引擎怎么驱动、上下文怎么在节点间传递，这些概念需要一个一个搞清楚。作者的做法是先不写代码，让 Claude Code 用具体场景解释，再看代码层面怎么表示，再做存储设计。这个顺序不能乱，跳过概念直接写代码，方向会错。

##### ② 执行引擎的异常分支

正常路径 Claude Code 基本能写对，但边界情况大概率遗漏：条件分支所有条件都不匹配怎么办？`targetNodeKey` 指向不存在的节点会不会空指针？工作流里有环会不会死循环？这些问题需要逐个手动检查和追问 Claude Code 才能补全。正常路径不需要花太多精力 review，异常分支才是重点。

#### (3) 最让作者惊喜的

执行引擎跑通的那一刻，同一个智能客服 Agent 面对不同问题会自动走不同路径：

| 用户问题 | 走的路径 | 回答风格 |
|----------|----------|----------|
| 耳机坏了怎么保修 | 售后路径 | 售后客服语气 |
| 最新蓝牙耳机有什么功能 | 售前路径 | 产品顾问语气 |
| 耳机连不上蓝牙 | 技术支持路径 | 工程师语气 |

三条路径、三种回答风格，都由同一个工作流配置驱动。查 `workflow_node_run` 表，每个节点的输入输出、执行耗时、成功失败状态都清清楚楚：`classify` 节点 1200ms，`router` 节点 2ms，`aftersale` 节点 3400ms。<span style="color: red; font-weight: bold;">出了问题能精确定位到哪个节点，这种可观测性是单 Prompt Agent 做不到的。</span>

## 3. 方法论提炼：工作流模块的五条核心动作

本部分不绑定具体技术栈，是可迁移到任何「陌生子系统」AI 编程场景的方法论。每条动作按「怎么做 / 为什么 / 常见误区」三段式展开。

### 3.1 动作一：概念先行，再写代码

<img src="imgs/aicent-26-adv-fea-recap/16f57de2603f34f55e332f103b90997f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 怎么做

面对没做过的子系统，先不写代码。让 Claude Code 用业务场景解释概念（例如「用户问订单到哪了，单 Prompt 怎么处理 vs 工作流怎么处理，差别在哪」），再看代码层面怎么表示（数据结构），最后做存储设计。顺序固定：**场景 → 代码表示 → 存储设计**。

#### (2) 为什么

没做过的子系统，脑子里一开始没有完整的图。节点是什么、连线是什么、执行引擎怎么驱动、上下文怎么传递，这些概念没搞清楚就写代码，方向会错，后续返工成本远高于先理解概念。

#### (3) 常见误区

##### ① 从学术概念入手

<span style="color: red; font-weight: bold;">不要从 DAG、图论这些学术概念入手，会让人绕进去。</span>需要的是能直接对应到代码的解释。

##### ② 跳过概念直接写代码

跳过概念直接动手，大概率写错方向。

### 3.2 动作二：不熟悉的系统先做技术调研

#### (1) 怎么做

让 Claude Code 调研业界主流方案。例如工作流执行引擎调研了 Dify、n8n、Coze、Temporal 四个平台，从「线程模型、节点执行隔离、上下文传递、错误处理、执行记录」五个维度对比，最后给出适合本项目体量的建议。

#### (2) 为什么

不是所有方案都适合你的场景。<span style="color: red; font-weight: bold;">调研的价值在于知道有哪些选项、各自的代价是什么，然后做出有理由的选择。</span>例如 n8n 的 Redis Queue 是为多 Worker 水平扩展设计的；Temporal 的事件溯源是为小时/天级工作流设计的；Dify 的代码沙箱是为用户自定义代码节点设计的——这些 Hify 都不需要。

#### (3) 常见误区

##### ① 照搬大厂方案

看到 Dify、Temporal 就直接抄，忽略了它们的方案是为特定场景设计的（水平扩展、长周期工作流、用户自定义代码），照搬会引入不必要的复杂度。

##### ② 凭直觉选方案

不做对比，凭感觉选一个，后续遇到瓶颈才发现选错了。

### 3.3 动作三：复杂系统分步实现，每步验证

<img src="imgs/aicent-26-adv-fea-recap/47ae9bb096705b4ed73c507fb2716f37_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 怎么做

把复杂系统拆成多块，按顺序实现，每实现一块就用单元测试或可运行用例验证。例如执行引擎拆成四块按顺序实现：

##### ① Agent 绑定 `workflow_id` + ExecutionContext（单元测试验证模板替换）

##### ② NodeExecutor 体系（四种 Executor + Registry）

##### ③ 核心循环 WorkflowEngine（先线性执行 → 加条件分支 → 加错误处理）

##### ④ 执行记录表 `workflow_run` + `workflow_node_run`

#### (2) 为什么

<span style="color: red; font-weight: bold;">盲目追求一次写完，出了问题不知道是哪一步错的。分步实现让每一步的错误都能被快速定位和修复。</span>

#### (3) 常见误区

##### ① 一次性全写完

把所有代码一次生成、一次跑，错了不知道从哪查起。

### 3.4 动作四：review 重点放在异常分支

#### (1) 怎么做

正常路径交给 Claude Code 写，基本能写对，不用花太多精力。<span style="color: red; font-weight: bold;">review 的精力集中放在「如果这一步的输入不是预期的会怎样」上，逐个追问边界情况。</span>例如工作流执行引擎要检查四类异常分支：

| 异常场景 | 期望保护 |
|----------|----------|
| 条件分支所有条件都不匹配 | <span style="color: red; font-weight: bold;">有 defaultTarget 或合理报错</span> |
| `targetNodeKey` 指向不存在的节点 | <span style="color: red; font-weight: bold;">`nodeMap.get` 返回 null 时抛异常，不空指针</span> |
| 工作流里有环（A→B→A） | <span style="color: red; font-weight: bold;">步数限制（如 50 步）生效，不死循环</span> |
| LLM 节点调用失败 | <span style="color: red; font-weight: bold;">`workflow_node_run` 记录 FAILED，`workflow_run` 也标记 FAILED</span> |

#### (2) 为什么

<span style="color: red; font-weight: bold;">Claude Code 写正常路径没问题，但边界情况大概率遗漏。这些问题只有在 review 时逐个追问才能补全。</span>

#### (3) 常见误区

##### ① 只测正常路径

只验证 happy path，上线后被边界情况打穿（空指针、死循环、数据不一致）。

### 3.5 动作五：改完先跑旧用例

<img src="imgs/aicent-26-adv-fea-recap/03f8e80080d46677549ed360971d8096_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 怎么做

接入新功能后，第一件事不是测新功能，而是确认旧功能没坏。例如接入对话引擎后，先跑一个不绑工作流的 Agent，确认流式返回正常、日志里没有任何 workflow 相关输出。

#### (2) 为什么

新代码可能意外影响已有链路。先跑旧用例能立刻发现回归问题，避免「改了 A 坏了 B」的隐患潜伏到生产。

#### (3) 常见误区

##### ① 只测新功能

只验证新加的工作流路径，忽略原有链路，上线后才发现旧功能被破坏。

### 3.6 补充动作：已有模式优先复用

| 复用场景 | 复用的模式 | 来源 |
|----------|------------|------|
| 元数据 CRUD | 拆分写入 + 组装还原 + 同事务 | 之前做知识库管理的节奏 |
| NodeExecutor 体系 | 统一接口 + 按 type 分发 + Spring 自动注册 | 第 14 篇 Provider 适配层 |
| 节点配置 JSON 字段 | JSON 字段 + 类型安全解析 | 第 12 篇 `auth_config` 处理方式 |

Claude Code 在「已有模式的复用」上表现很好，会主动识别项目里已有的代码风格与模式，生成的代码与项目一致性很高。

### 3.7 方法论总览

| 动作 | 核心要点 | 常见误区 |
|------|----------|----------|
| 概念先行 | 场景 → 代码表示 → 存储设计 | 从学术概念入手、跳过概念直接写 |
| 技术调研 | 对比选项 + 做有理由的选择 | 照搬大厂方案、凭直觉选 |
| 分步实现 | 每步用单元测试/用例验证 | 一次性全写完 |
| 异常 review | 追问「输入不是预期会怎样」 | 只测正常路径 |
| 旧用例回归 | 接入新功能先验证旧功能没坏 | 只测新功能 |

## 4. 工作流开发 Check List

<img src="imgs/aicent-26-adv-fea-recap/941e01f1c0c77290a360b7441226a671_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本清单按开发顺序分七组，可裁剪到项目阶段快速查阅。

### 4.1 概念建立阶段

- ☐ 是否让 Claude Code 用业务场景解释概念，而不是从 DAG/图论入手
- ☐ 是否搞懂了两个核心概念：节点是「做什么」、连线是「做完去哪」
- ☐ 是否在写代码前先建立了完整的概念图（场景 → 代码表示 → 存储设计）

### 4.2 技术调研阶段

- ☐ 是否让 Claude Code 调研了业界主流方案（如 Dify、n8n、Coze、Temporal）
- ☐ 是否从多个维度（线程模型、上下文传递、错误处理等）做了对比
- ☐ 是否明确了每个方案的适用场景与代价
- ☐ 是否给出了适合本项目体量的选择理由

### 4.3 设计阶段

- ☐ 数据模型是否拆分合理（如 workflow / workflow_node / workflow_edge 三张表）
- ☐ 连线是否引用业务 key（如 `node_key` 字符串）而非数据库自增 id
- ☐ 不同类型节点的配置是否用 JSON 字段而非强行拆列
- ☐ 是否用 `sealed interface + record` 做类型安全解析（switch 模式匹配漏 case 编译报错）

### 4.4 实现阶段

- ☐ 是否分步实现（Agent 绑定 → NodeExecutor → 核心循环 → 执行记录表）
- ☐ 每一步是否都有单元测试或可运行用例验证
- ☐ 是否复用了项目已有的模式（如 Provider 适配层、知识库 CRUD）

### 4.5 异常分支 review 阶段

- ☐ 条件分支所有条件都不匹配时是否有 defaultTarget 或合理报错
- ☐ `targetNodeKey` 指向不存在的节点时是否抛异常而非空指针
- ☐ 工作流里有环时步数限制（如 50 步）是否生效
- ☐ LLM 节点调用失败时 `workflow_node_run` 是否记 FAILED、`workflow_run` 是否也标记 FAILED
- ☐ 发现的问题是否当场让 Claude Code 修复并复测

### 4.6 接入已有系统阶段

- ☐ 改动是否最小侵入（如只在一个判断分支加几行，原有逻辑一行不动）
- ☐ 是否不修改流式调用、SseEmitter 转发、Redis 上下文管理等已有逻辑
- ☐ 工作流执行失败时是否通过 SseEmitter 推错误提示给用户

### 4.7 回归与验收阶段

- ☐ 接入后是否先跑不绑工作流的 Agent，确认旧功能没坏
- ☐ 绑工作流后是否跑了多种意图，验证不同路径都被正确触发
- ☐ 是否查了执行记录表（`workflow_run` / `workflow_node_run`）验证可观测性
- ☐ 三个验收维度是否都过：链路通、不同意图走不同路径、旧功能没坏

## 5. 实战演示一：概念探路（场景一）

本章复盘第 22 篇的概念建立过程：从「没做过工作流」到「知道它在代码里是什么数据结构」。这一阶段不写业务代码，只建立概念与数据模型。

### 5.1 用业务场景建立概念

<img src="imgs/aicent-26-adv-fea-recap/e36c6facd8350b5c876958f736fb4de0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 指令

```
在 AI 应用平台中，工作流是什么概念？
和直接让 Agent 用一个 Prompt 回答有什么区别？
用智能客服的场景帮我解释，不要讲理论，给我具体的例子。
```

#### (2) 要点

##### ① Claude Code 用具体场景切入

Claude Code 用「我昨天下的订单还没到」这个具体场景切入，而不是从 DAG 定义开始。

##### ② 单 Prompt 与工作流的核心差异

| 模式 | 做法 | 适用场景 |
|------|------|----------|
| 单 Prompt | 让 LLM 凭知识编答案 | 通用知识类（如「退货政策是什么」） |
| 工作流 | 把任务拆成有序步骤，每步做一件具体的事 | 需要查数据/分路径（如「我的订单到哪了」） |

##### ③ 关键判断标准

<span style="color: red; font-weight: bold;">问「退货政策是什么」单 Prompt 够用；问「我的订单到哪了」必须走工作流——因为答案在数据库里，不在 LLM 脑子里。</span>

### 5.2 看它在代码里是什么形式

#### (1) 指令

```
工作流在代码层面怎么表示？
帮我用最直白的方式解释——不要说 DAG、不要说图论，
就告诉我它在代码里是什么数据结构。
```

指令里特意加「不要说 DAG」的原因是：学术概念先行会让人绕进去，需要的是能直接对应到代码的解释。

#### (2) 要点

##### ① 两个核心概念

| 概念 | 含义 | 代码表示 |
|------|------|----------|
| 节点 | 「做什么」 | 每个节点一条数据库记录，有 `type`（LLM / CONDITION / API_CALL）和 `config`（JSON 配置） |
| 连线 | 「做完去哪」 | 记录 `sourceNodeKey` → `targetNodeKey` + `condition` |

##### ② 存储与执行的形态差异

- **存库时**：平铺的两张表（`workflow_node` 存节点、`workflow_edge` 存连线）。
- **执行时**：加载进内存变成 `Map + while` 循环驱动。

<span style="color: red; font-weight: bold;">搞懂这两个东西就够了，工作流没有想象中那么神秘。</span>

### 5.3 设计完整的数据模型

<img src="imgs/aicent-26-adv-fea-recap/e6c29c20d6e3211ef037df22bcea869d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 指令

```
基于上面的工作流结构，帮我设计数据模型。要考虑：
1. 如何存储工作流基本信息
2. 如何存储节点，不同类型节点配置格式不同怎么处理
3. 如何存储节点之间的连接关系
4. Java 代码里如何做类型安全的解析
```

#### (2) 要点

##### ① Claude Code 先读现有代码风格再设计

Claude Code 不是凭空设计，而是先读了项目现有代码风格再给出方案，保证与项目一致性。

##### ② 三张表的分工

| 表名 | 职责 |
|------|------|
| `workflow` | 存储工作流基本信息 |
| `workflow_node` | 存储节点 |
| `workflow_edge` | 存储连线 |

##### ③ 两个关键设计决策

| 决策 | 选择 | 原因 |
|------|------|------|
| 连线如何引用节点 | 用 `node_key` 字符串而非数据库 id | <span style="color: red; font-weight: bold;">前端拖拽时不依赖自增顺序，避免 id 还没生成就要引用的问题</span> |
| 节点配置存储方式 | 用 JSON 字段而非拆列 | <span style="color: red; font-weight: bold;">不同类型节点配置格式差异大，强行拆列会让表结构难以维护，每加一种节点类型就要改表结构</span> |

##### ④ 类型安全解析方案

<span style="color: red; font-weight: bold;">采用 `sealed interface + record` 做类型安全解析。`switch` 模式匹配漏写任何一个 case 编译就报错，从编译期保证节点类型解析的完整性。</span>这个原则与第 12 篇 `auth_config` 的处理方式一致。

## 6. 实战演示二：元数据 CRUD（场景二）

<img src="imgs/aicent-26-adv-fea-recap/c6cef55d81e8ba0a28a92bc059ba62c9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本章复盘第 22 篇的 CRUD 实现与验证，目标是确认数据模型能正确存储和还原工作流配置。

### 6.1 实现 CRUD

#### (1) 指令

```
在 hify-workflow 模块中实现工作流的 CRUD。
```

#### (2) 接口列表

```
POST /api/v1/workflows — 创建工作流
GET /api/v1/workflows — 分页查询工作流列表
GET /api/v1/workflows/{id} — 查询工作流详情（含完整节点和边）
PUT /api/v1/workflows/{id} — 更新工作流
DELETE /api/v1/workflows/{id} — 逻辑删除工作流
```

#### (3) 约束

```
- 创建接口接收完整请求体（包含 nodes 和 edges），拆分写入三张表
workflow 写一条，nodes 批量插入 workflow_node，edges 批量插入 workflow_edge
三张表在同一个事务里，任何一张写失败全部回滚
- 查询详情接口从三张表组装回完整结构返回
- 更新工作流时，先逻辑删除原有的 nodes 和 edges，再批量插入新的，不做 diff
- 节点配置用 NodeConfigParser 解析，NodeConfig sealed interface + record 体系
```

#### (4) 要点

##### ① 关注 Claude Code 是否遵循项目代码风格

重点看 Claude Code 生成代码的过程，是否读完了现有代码风格再生成，与项目一致性如何。

##### ② 更新时直接替换不做 diff 的原因

工作流改动涉及多个节点和边，diff 逻辑复杂且容易出错。先逻辑删除原有的 nodes 和 edges、再批量插入新的，能保持三张表一致，代价更低、更不容易出错。

### 6.2 用真实配置验证

#### (1) 创建工作流

```bash
curl -X POST http://localhost:8080/api/v1/workflows \
-H "Content-Type: application/json" \
-d '{
  "name": "智能客服分类工作流",
  "nodes": [
    {"nodeKey": "start", "type": "START", "name": "开始", "config": {}},
    {"nodeKey": "classify", "type": "LLM", "name": "问题分类",
     "config": {"prompt": "判断问题类型，返回：售前/售后/技术支持", "outputVariable": "intent"}},
    {"nodeKey": "router", "type": "CONDITION", "name": "路由分发",
     "config": {"expression": "{{classify.intent}}", "outputVariable": "route"}},
    {"nodeKey": "presale", "type": "LLM", "name": "售前咨询",
     "config": {"prompt": "你是产品顾问，介绍产品功能和优势", "outputVariable": "answer"}},
    {"nodeKey": "aftersale", "type": "LLM", "name": "售后服务",
     "config": {"prompt": "你是售后客服，回答退换货和保修问题", "outputVariable": "answer"}},
    {"nodeKey": "techsupport", "type": "LLM", "name": "技术支持",
     "config": {"prompt": "你是技术工程师，帮用户排查使用问题", "outputVariable": "answer"}},
    {"nodeKey": "end", "type": "END", "name": "结束", "config": {"outputVariable": "answer"}}
  ],
  "edges": [
    {"sourceNodeKey": "start", "targetNodeKey": "classify", "condition": null},
    {"sourceNodeKey": "classify", "targetNodeKey": "router", "condition": null},
    {"sourceNodeKey": "router", "targetNodeKey": "presale", "condition": "售前"},
    {"sourceNodeKey": "router", "targetNodeKey": "aftersale", "condition": "售后"},
    {"sourceNodeKey": "router", "targetNodeKey": "techsupport", "condition": "技术支持"},
    {"sourceNodeKey": "presale", "targetNodeKey": "end", "condition": null},
    {"sourceNodeKey": "aftersale", "targetNodeKey": "end", "condition": null},
    {"sourceNodeKey": "techsupport", "targetNodeKey": "end", "condition": null}
  ]
}'
```

#### (2) 查询详情

```bash
curl http://localhost:8080/api/v1/workflows/1 | python3 -m json.tool
```

#### (3) 要点

##### ① 创建结果

创建成功返回 `workflowId`。

##### ② 查询结果与创建一一对应

查询详情返回的完整结构与创建时一一对应：七个节点都在，八条边都在，config JSON 字段没有丢失。

##### ③ 验收标准

验收标准只有一个：创建进去的配置，查询出来能完整还原，不多不少。

### 6.3 前端验收

#### (1) 操作顺序

```
打开 http://localhost:5173，进入“工作流管理”
点击“新建工作流”，看到预填的示例 JSON
修改其中一个节点的 Prompt，点“格式化”按钮，确认 JSON 美化正常
提交，确认创建成功，跳回列表页，列表中出现新建的工作流
输入非法 JSON，确认前端拦截，不提交
```

#### (2) 前端界面截图

<img src="imgs/aicent-26-adv-fea-recap/204e3c12db3aa3628ffe227e3ef67427_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
 
<img src="imgs/aicent-26-adv-fea-recap/204e3c12db3aa3628ffe227e3ef67427_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
  
<img src="imgs/aicent-26-adv-fea-recap/c0ac604fb118b8fbd9b01ed8663d3d0d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
   
<img src="imgs/aicent-26-adv-fea-recap/3b114ef3548b07dcffdb8f68f0d21484_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicent-26-adv-fea-recap/d12e6a53c3b303d0bc11689155851535_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
    
<!--
图片内容说明
路径：imgs/aicent-26-adv-fea-recap/121a506faf2ff6be3b1965c94f21a23b_MD5.jpg
用途：场景二「前端验收」步骤的截图，展示工作流管理页面的 JSON 编辑器界面，验证数据模型能通过前端正确提交与展示
内容：Hify 工作流管理前端页面的截图。页面为「工作流管理」列表/编辑界面，左侧/中部展示了一个 JSON 编辑器区域，内含预填的工作流配置示例（可见 nodes 数组中 start、classify、router、presale、aftersale、techsupport、end 等节点，以及 edges 数组中各节点间的连接关系和 condition 字段）；界面上提供「格式化」按钮用于美化 JSON、「提交」按钮用于创建工作流；右上角为新建/编辑操作区。整体是工作流配置的最简版前端形态（JSON 文本编辑，非可视化拖拽画布）。
-->

#### (3) 要点

##### ① JSON 编辑器的基本体验

重点展示三项基本体验：预填示例降低上手门槛、格式化按钮方便阅读、合法性校验防止提交坏数据。

##### ② 最简版验证数据模型

完整形态应该是可视化拖拽编排（Dify、Coze 那种画布），这里只做最简版（JSON 编辑器）用于验证数据模型。

## 7. 实战演示三：执行引擎（场景三）

本章复盘第 23 篇的执行引擎，从设计到实现的完整过程，是整个工作流模块的核心。

### 7.1 技术调研，选方案

<img src="imgs/aicent-26-adv-fea-recap/589882ba388a860bca01c1ac3c2e6d16_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 指令

```
工作流执行引擎在业界有哪些主流的实现方案？
重点看 Dify、Coze、n8n 这类 AI 应用平台是怎么设计的。
我想了解：线程模型怎么选、节点执行怎么隔离、上下文数据怎么在节点间传递、
错误处理和执行记录怎么做。
最后给我一个建议：Hify 这种体量的项目应该选哪种方案，为什么。
```

#### (2) 要点

##### ① 四平台对比

Claude Code 调研了 Dify、n8n、Coze、Temporal 四个平台：

| 平台 | 适用场景 | Hify 是否需要 |
|------|----------|---------------|
| n8n | 多 Worker 水平扩展（Redis Queue） | 不需要（Hify 不做水平扩展） |
| Temporal | 小时/天级长周期工作流（事件溯源） | 不需要（Hify 工作流是单次对话响应，最长几十秒） |
| Dify | 代码沙箱 + VariablePool 模式 | 部分借鉴（VariablePool 模式适合，代码沙箱不需要） |
| Coze | AI 应用平台工作流 | 仅作参考 |

##### ② 最终建议

<span style="color: red; font-weight: bold;">Hify 选轻量同步引擎，参考 Dify 的 `VariablePool` 模式，不引入消息队列。</span>原因：Hify 的节点类型是固定的几种，不需要代码沙箱；工作流是一次对话响应的一部分，不需要长周期编排；不做多 Worker 水平扩展，不需要消息队列。

##### ③ 调研的价值

不是所有方案都适合你的场景，先知道有哪些选项，再做有理由的选择。

### 7.2 看执行引擎的代码结构

#### (1) 指令

```
基于上面的调研结论，帮我把 Hify 执行引擎的代码结构梳理清楚。
四个部分：线程池、ExecutionContext、NodeExecutor 体系、核心循环。
每个部分是什么，相互之间怎么协作，用代码示例说明。
先把现有的代码都读清楚，不基于假设设计。
```

#### (2) 要点

##### ① 先读现有代码再设计

Claude Code 先读完现有代码再给出设计，不是凭空臆造。

##### ② 四个部分的协作

| 部分 | 设计 | 关键点 |
|------|------|--------|
| 线程池 | 复用 `llmExecutor`，不新建 | 避免重复造轮子 |
| ExecutionContext | 对标 Dify 的 `VariablePool` | 变量池机制 |
| NodeExecutor 体系 | 统一接口 + 按 type 分发 | 与第 14 篇 Provider 模式一致 |
| 核心循环 | 就是一个 `while` | 驱动节点依次执行 |

##### ③ ExecutionContext 变量池机制（重点）

- **key 格式**：`nodeKey.varName`（如 `classify.intent`）。
- **写入语义**：只增不改，避免覆盖历史值。
- **模板变量**：`{{classify.intent}}` 在运行时被替换为变量池中的实际值。

### 7.3 分步实现，每步验证

<img src="imgs/aicent-26-adv-fea-recap/7881ff3e47171d3f4f674a89eae73bd4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 指令

```
帮忙实现执行引擎
```

#### (2) 四块代码按顺序实现

##### ① Agent 绑定 `workflow_id` + ExecutionContext

单元测试验证模板替换，确保 `{{classify.intent}}` 能被正确解析为变量池中的值。

##### ② NodeExecutor 体系

实现四种 Executor + Registry（统一接口 + 按 type 分发 + Spring 自动注册）。

##### ③ 核心循环 WorkflowEngine

按递进顺序实现：先线性执行 → 加条件分支 → 加错误处理。每加一层能力都验证一次。

##### ④ 执行记录表 `workflow_run` + `workflow_node_run`

记录每次工作流执行与每个节点的执行细节，用于可观测性与故障定位。

#### (3) 分步实现的原因

盲目追求一次写完，出了问题不知道哪步错的。分步实现让每一步的错误都能被快速定位。

### 7.4 异常分支 review

<img src="imgs/aicent-26-adv-fea-recap/661c9cc88073705fba03897e4e9e2d2b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 指令

```
验证执行引擎是否有逻辑错误，是否正常
```

#### (2) 逐一检查四个边界情况

| 异常场景 | 期望保护 | review 重点 |
|----------|----------|-------------|
| a. 条件分支所有条件都不匹配 | <span style="color: red; font-weight: bold;">有 `defaultTarget` 或合理报错</span> | Claude Code 是否给出了处理逻辑 |
| b. `targetNodeKey` 指向不存在的节点 | <span style="color: red; font-weight: bold;">不空指针，`nodeMap.get` 返回 null 时抛异常</span> | 保护逻辑是否到位 |
| c. 工作流里有环（A→B→A） | <span style="color: red; font-weight: bold;">50 步限制生效，不死循环</span> | 步数上限是否生效 |
| d. LLM 节点调用失败 | <span style="color: red; font-weight: bold;">`workflow_node_run` 记 FAILED，`workflow_run` 也标记 FAILED</span> | 失败状态是否正确传播 |

#### (3) review 原则

正常路径 Claude Code 基本能写对，review 精力要花在异常分支上。如果发现问题，当场让 Claude Code 修复，展示修复过程。

## 8. 实战演示四：接入对话引擎（场景四）

本章把工作流引擎接入 `ChatServiceImpl`，验证绑工作流前后的行为差异，是完整链路的最后一环。

### 8.1 修改 ChatServiceImpl

<img src="imgs/aicent-26-adv-fea-recap/a1eea6a4650c9b2a058fb35009b02756_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 指令

```
修改 ChatServiceImpl 的 doStreamChat() 方法，支持工作流触发。
在加载完 Agent 之后插入判断：
- agent.getWorkflowId()!= null → 调 workflowEngine.execute(workflowId, userContent)
拿到返回的 String 结果，存入 MySQL 作为 assistant 消息，发 SSE done 事件，return
- workflowId 为 null → 走原有逻辑，一行不改
```

#### (2) 约束

```
- 不修改流式调用、SseEmitter 转发、Redis 上下文管理的逻辑
- 工作流执行失败时，catch BizException，通过 SseEmitter 推错误提示给用户
```

#### (3) 要点

##### ① 改动 diff

只在一个判断分支里加了几行代码，原有逻辑一行没动。

##### ② 最小侵入原则

有 `workflowId` 走工作流，没有走原有链路，两条路互不影响。这是接入已有系统时应该遵循的核心原则。

### 8.2 先跑不绑工作流的 Agent，确认旧功能没坏

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/1/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "你好，介绍一下你自己"}'
```

#### (1) 验收要点

##### ① 流式返回正常，内容正确

##### ② 日志里没有任何 workflow 相关输出

##### ③ 这步不能省

改完先跑旧用例，确认旧功能没坏，这步不能省。

### 8.3 给 Agent 绑定工作流，跑三种意图

<img src="imgs/aicent-26-adv-fea-recap/23b34713731427573e2ad5b7e794d915_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 绑定工作流

```bash
curl -X PUT http://localhost:8080/api/v1/agents/1 \
-H "Content-Type: application/json" \
-d '{"workflowId": 1}'
```

#### (2) 跑三种意图

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/2/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "我买的耳机坏了，怎么申请保修"}'
```

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/3/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "你们最新的蓝牙耳机有什么功能"}'
```

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/4/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "耳机连不上手机蓝牙怎么办"}'
```

#### (3) 验收要点

三个问题分别走三条不同的路径，回答风格明显不同：

| 用户问题 | 走的节点 | 回答风格 |
|----------|----------|----------|
| 我买的耳机坏了，怎么申请保修 | `aftersale` 节点 | 售后客服语气，回答保修政策 |
| 你们最新的蓝牙耳机有什么功能 | `presale` 节点 | 产品顾问语气，介绍功能 |
| 耳机连不上手机蓝牙怎么办 | `techsupport` 节点 | 工程师语气，做故障排查 |

展示后端日志，能看到每次请求经过了哪些节点。

### 8.4 查执行记录，验证可观测性

```bash
curl http://localhost:8080/api/v1/workflows/1/runs/latest | python3 -m json.tool
```

#### (1) `workflow_run` 记录

| 字段 | 内容 |
|------|------|
| `status` | `SUCCESS` |
| `input` | 用户消息 |
| `output` | 最终回答 |
| `elapsed_ms` | 总耗时 |

#### (2) `workflow_node_run` 记录

每个节点都有独立记录，以售后问题为例：

| 节点 | status | 耗时 |
|------|--------|------|
| `classify` | `SUCCESS` | ~1200ms |
| `router` | `SUCCESS` | ~2ms |
| `aftersale` | `SUCCESS` | ~3400ms |

#### (3) 可观测性的价值

出了问题能精确定位到哪个节点慢了或者错了，这是单 Prompt Agent 做不到的。

#### (4) 条件分支验证

对比三次请求的 `node_run` 记录：`classify` 和 `router` 每次都走，但第三个节点不同（`presale` / `aftersale` / `techsupport`），验证条件分支确实在工作。

### 8.5 验证不绑工作流的 Agent 不受影响

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/5/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "帮我写一首关于春天的诗"}'
```

#### (1) 验收要点

##### ① 直接调 LLM，流式返回正常

##### ② 日志里没有任何 workflow 相关输出

#### (2) 三个验收维度都过才算交付完整

| 验收维度 | 状态 |
|----------|------|
| 工作流链路通 | ✓ |
| 不同意图走不同路径 | ✓ |
| 旧功能没坏 | ✓ |

## 9. 总结：可迁移到其他子系统的经验

<img src="imgs/aicent-26-adv-fea-recap/acfaed02f1df0f7103c27d36f1ee324f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

四个场景走完，完整覆盖了工作流从概念探路到接入对话引擎的全过程。本系列从下一篇开始进入测试与部署篇，并会系统提炼整个系列的思考方式与工程经验。

### 9.1 五条值得记住的可迁移动作

#### (1) 五条可迁移动作清单

##### ① 不懂就先问清楚再动手

工作流是没做过的子系统，上来就写代码大概率方向错。先让 Claude Code 用具体场景建立概念，再看代码形式，再设计，顺序不能乱。

##### ② 遇到不熟悉的系统设计先做技术调研

Dify 的 `VariablePool`、n8n 的 `WorkerQueue`、Temporal 的事件溯源，不是所有方案都适合你的场景。调研的价值在于知道有哪些选项，然后做出有理由的选择。

##### ③ 复杂系统分步实现

执行引擎没有一次性全写出来：先线性执行验证 Context 数据传递 → 加条件分支验证路由 → 加错误处理做生产级保护 → 最后接入已有系统。每一步验证通过再往下走。

##### ④ review 重点在异常分支

正常路径 Claude Code 基本能写对，但「目标节点不存在」「条件都不匹配」「有环」这类边界大概率遗漏。review 精力不要花在正常路径上，花在「如果这一步的输入不是预期的会怎样」。

##### ⑤ 改完先跑旧用例

接入对话引擎之后，第一件事不是测新功能，是确认旧功能没坏。
