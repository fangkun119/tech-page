---
title: AI编程方法 25：高级功能 - MCP接入（下）实现服务
author: fangkun119
date: 2026-07-02 17:00:00 +0800
categories: [AI编程, 方法论]
tags: [AI编程, AI编程方法论]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-25-fea-mcp-2/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---

<!--
aicent-25-fea-mcp-2
AI编程方法 25：高级功能 - MCP接入（下）实现服务
-->

## 1. 导读

<img src="imgs/aicent-25-fea-mcp-2/7d5aa692ffd8f23a7e5e1a51f79036c5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一篇是系列的第 25 篇，承接前一篇 MCP Client 接入，专题讲解**如何自建一个真实的 MCP Server**，让 Agent 从"只能说"升级为"真正能做"。围绕退款场景，全文用 Java + Spring Boot + Java MCP SDK 完整复现一个退款 MCP Server 的设计、实现、调试与接入过程。

本文按"方法论提炼 + 实战演示"两部分组织，便于不同读者按需取用：

<img src="imgs/aicent-25-fea-mcp-2/311807a8d04cc2ec844ca2220106e5d1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    Start([读者入口]) --\> Q{阅读目标?}
    Q --\>|掌握方法论 / 快速复习| P1[第一部分 方法论提炼]
    Q --\>|完整复现项目 / 深入理解 why| P2[第二部分 实战演示]

    subgraph 第一部分[第一部分 方法论提炼 - 参考手册风]
        A1[1、何时自建 MCP Server]
        A2[2、MCP Server 是什么]
        A3[3、从场景推导工具]
        A4[4、工具设计原则]
        A5[5、开发标准模板与工作流]
        A6[6、项目 Check List]
    end

    subgraph 第二部分[第二部分 实战演示 - 实战教材风]
        B1[7、场景与项目背景]
        B2[8、退款工具推导实战]
        B3[9、拆解实现步骤]
        B4[10、逐步实现关键细节]
        B5[11、MCP 调试工具实现]
        B6[12、接入 Hify 与端到端验收]
        B7[13、总结与延伸思考]
    end

    P1 --\> 第一部分
    P2 --\> 第二部分
    第一部分 -.方法论印证.-> 第二部分
-->

**两类读者建议路径**：

- **熟练工程师 / 项目快速回顾**：直接读第一部分（章 2-7），尤其章 7 的 Check List 可裁剪到项目阶段使用。
- **初学工程师 / 系统掌握**：先快速浏览第一部分建立方法论框架，再精读第二部分看方法论如何落地，"不仅知其然、也知其所以然"。

**第一部分 方法论提炼**

## 2. 何时自建 MCP Server：决策方法

<img src="imgs/aicent-25-fea-mcp-2/3e240faf2fc9caf8b7c8ede04f0d3cd5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接入外部能力前，第一个决策是：**用现成的 MCP Server，还是自建？** <span style="color: red; font-weight: bold;">判断标准只有一句话——看工具提供方是否已经为你做好了可直接使用的 Server。</span>

### 2.1 决策两类情况

**① 用现成的 MCP Server**

工具提供方已发布官方 MCP Server，直接接入即可。典型如支付场景：Stripe 官方提供了支付查询、退款发起等 Server，企业无需自己实现。

**② 必须自建**

两种情形会落到"必须自建"，且常常同时出现：

- **内部系统无公开 Server**：公司财务系统、ERP、内部工单系统等不对外，没人替你做 Server。
- **公开 Server 但需定制业务逻辑**：例如退款要走公司自己的审批流，金额超过 500 元需主管审批——这种业务规则只有自己最清楚。

### 2.2 决策检查表

| 判断维度 | 用现成 | 必须自建 |
| --- | --- | --- |
| 是否有官方 Server | 有 | 无 |
| 系统是否对外 | 是（公网服务） | 否（内部系统） |
| 业务规则是否通用 | 通用、标准 | 含公司专属审批/规则 |
| 改造成本 | 接入即可 | 定制 > 接入 |

退款场景两条都占：财务系统是内部的，且退款要走公司审批流。这就是本篇选它作为自建案例的理由。

## 3. MCP Server 是什么：认知模型

<img src="imgs/aicent-25-fea-mcp-2/a07d950456dd3271d6fab20a8a9f21d6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

动手前先把"交付物是什么"想清楚。MCP Server 在认知层面有四个关键性质，理解了它们，后面的实现、调试、接入才不会跑偏。

### 3.1 本质：一个独立的 HTTP Server

MCP Server 是一个独立部署的应用（本篇用 Spring Boot），与调用方（Hify）完全分开部署，只通过 HTTP 通信。可以把它理解成一个"专门按 MCP 协议说话的 HTTP 服务"。

### 3.2 黑盒性质：调用方不关心实现细节

调用方只存一个 endpoint 地址，发标准 MCP 协议请求。Server 内部用 Java 还是 Python、查的是 MySQL 还是 Oracle，调用方一概不关心。<span style="color: red; font-weight: bold;">这就是标准化协议的核心价值——工具提供方换了，调用方一行代码不用改。</span>

### 3.3 开发者只需关心两件事

剥去框架细节，MCP Server 的代码对开发者而言只有两件核心工作：

**① 声明工具 schema**

声明这个 MCP 工具的名字、描述、需要的参数。

**② 实现业务逻辑**

收到调用请求后，查数据库或调接口，返回结果。

### 3.4 协议自动暴露的端点

使用 MCP SDK（如 Java MCP SDK 的 `WebMvcSseServerTransport`）后，以下端点由 SDK 自动处理，开发者不需要手写：

- `GET /sse`：供调用方发现并监听 Server。
- `POST /messages`：供调用方发起工具调用请求。

> 提示：第一部分保持概念层面，具体 Java 配置与目录结构在第二部分章 11 展开。

## 4. 从业务场景推导工具：核心方法论

<img src="imgs/aicent-25-fea-mcp-2/a90a04a3431d989c192ab2649825c81b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

**这是全篇最重要的方法论**：<span style="color: red; font-weight: bold;">工具不是拍脑袋定的，而是从业务场景反推出来的。错误的顺序（先想"怎么实现"）会导致工具粒度失当、能力缺失或冗余。</span>

### 4.1 三步推导法

<span style="color: red; font-weight: bold;">正确顺序永远是"场景 → 能力 → 实现"</span>：

**① 第一步：问"用户会说什么"**

从真实业务场景出发，穷举用户在该领域可能说的话。以退款为例，用户会说四类话："我要退款"、"我的退款审批了没"、"不退了"、"为什么退款被拒了"。

**② 第二步：问"每类话需要什么能力"**

为每类用户话术匹配工具能力。这里的关键是判断**是否需要单独工具、是否需要拆分**：

- "我要退款" → 不能一步直接提交（用户没有确认机会），必须**拆成两步**：先查资格，再提交申请。
- "我的退款审批了没" → 查退款进度，返回状态与预计到账时间。
- "不退了" → 撤销申请，但只有 PENDING 状态才能撤。
- "为什么退款被拒了" → **不需要单独工具**，查状态时把拒绝原因一并返回即可。

**③ 第三步：问"怎么实现"**

前两步完成后，才有资格谈实现。上例最终推导出 4 个工具（具体 schema 见第二部分章 9）。

### 4.2 工具粒度的判断与权衡

推导过程中有三类常被忽略的判断题，需要工程师主动思考：

**① 拆还是合**

涉及"用户确认"的敏感操作要拆分（如查资格 + 提交申请分两步），给用户确认机会；纯查询可合并到现有工具的返回值里（如拒绝原因并入查状态）。

**② 是否全部做成 Server**

推导出的 4 个工具是否都要做成 MCP Server，还是可以只做一个满足全部诉求？这考验判断与鉴别能力，没有标准答案，要结合调用频率、权限边界、复用性综合权衡。

**③ 参数接受多形式以减少用户负担**

`get_refund_status` 同时接受 `orderId` 或 `refundId`：用户记得订单号不记得退款单号时，由 Server 做转换，比让用户多说一句话更合理。

### 4.3 方法论的可迁移性

这套"用户话术 → 能力 → 实现"的方法不仅适用于退款，库存查询、工单创建、订单状态等任何工具型 Agent 场景都通用。掌握方法后，可以让 AI 编程助手（如 Claude Code）按这个范式帮你批量推导。

## 5. 工具设计原则：面向 LLM 的返回值与 description

<img src="imgs/aicent-25-fea-mcp-2/56190c142085baf8f60be4f4b8bc73b7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">工具的消费者是 LLM，不是人，也不是纯程序。</span>这一认知决定了工具设计有两类必须遵守的原则。

### 5.1 返回值要"一鱼两吃"：程序字段 + LLM 字段分开

以退款状态为例，同一份数据要服务两类读者：

| 字段 | 面向 | 作用 | 示例 |
| --- | --- | --- | --- |
| `status` | 程序判断 | 英文枚举，供代码做分支控制 | `PROCESSING` |
| `statusLabel` | LLM 直读 | 中文标签，LLM 直接说给用户，无需自己翻译 | "处理中" |

**反模式**：<span style="color: red; font-weight: bold;">只返回 `status=PROCESSING`，让 LLM 自己翻译——LLM 可能翻错或表述不一致。</span>`statusLabel` 是 LLM 专门写给用户看的字段，必须与程序字段分开设计。

### 5.2 description 要说清"什么情况下调"

<span style="color: red; font-weight: bold;">工具的 `description` 不是功能说明，而是触发条件说明。</span>LLM 据此决定是否调用、调用顺序。

| 工具 | 优秀 description 写法要点 |
| --- | --- |
| `check_refund_eligibility` | "用户说'我要退款'时，先调此工具确认是否符合条件，再决定是否提交申请。**不要跳过此步直接提交。**" |
| `submit_refund` | "仅在用户确认退款意愿、且 `check_refund_eligibility` 返回 `eligible=true` 后调用。" |

要点：写清前置条件、调用时机、与其它工具的协作关系，必要时用"不要跳过""仅在…后"这类约束语。

### 5.3 错误处理：友好提示，绝不抛技术异常

调用失败时返回结构化错误，而不是让 SDK 抛异常中断整个对话：

- 返回 `{"error": "错误原因"}`，并标记 `isError=true`。
- 错误原因必须是 LLM 能理解并转述给用户的自然语言（如"该订单已有进行中的退款申请，编号：xxx"），不能是堆栈或技术错误码。

### 5.4 设计检查清单

- [ ] 返回值是否同时覆盖程序判断与 LLM 表述
- [ ] description 是否说明了调用时机与前置条件
- [ ] 失败路径是否返回友好错误而非抛异常
- [ ] 是否考虑了 LLM 会如何把返回值"说"给用户

## 6. MCP Server 开发标准模板与接入工作流

<img src="imgs/aicent-25-fea-mcp-2/8a45ec9f16fd47a74566e77cf6f251f7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">MCP Server 的开发高度模板化——声明 schema、实现逻辑、注册协议，每个 Server 都是同一套路。</span>做明白第一个，后续（库存 Server、工单 Server）只需换 schema 与业务逻辑。这种模板化任务正是 AI 编程助手提效最大的场景。

### 6.1 标准开发六步模板

每个 MCP Server 都可以套用以下六步，<span style="color: red; font-weight: bold;">每步必须能独立验证——出了问题立刻知道在哪一层找。</span>

| 步骤 | 任务 | 验证方式 |
| --- | --- | --- |
| ① 建项目 | 创建独立工程，引入 MCP SDK 依赖，启动应用 | 编译通过、能启动 |
| ② 注册 Server Bean | 配置 MCP Server，暴露 SSE 端点 | `curl /sse` 返回 SSE 流 |
| ③ 数据层 | 建业务表，实现数据访问层 | 表自动创建、单测读写正常 |
| ④ 业务逻辑 | 实现工具对应的 Service 方法 | 单元测试覆盖每个方法 |
| ⑤ 注册工具 | 把业务方法注册为 MCP 工具 | `tools/list` 返回所有工具 |
| ⑥ 接入调用方 | 在 Agent 平台注册并绑定 | 端到端对话、日志出现 tool_calls |

### 6.2 通用接入工作流

工具自身跑通后，进入接入阶段，遵循固定的四段式流程：

**① 工具自身跑通**

Server 单独运行，能用 curl 验证 schema 与业务逻辑。

**② 调试工具验证**

在 Agent 平台的 MCP Server 管理页面用调试工具验证每个工具行为，不必手动拼 curl。

**③ 接入 Agent**

注册 Server、测试连通、把工具绑定到具体 Agent。

**④ 端到端测试**

通过真实对话触发工具调用，验证 LLM 的调用时机与返回值表述是否符合预期。

> 提示：第二部分章 12 会演示调试工具的实现，章 13 演示接入与端到端验收。

## 7. 项目 Check List（可裁剪）

<img src="imgs/aicent-25-fea-mcp-2/70810583786c3ce49d9f5d4909ec8ff7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本 Check List 覆盖 MCP Server 项目全生命周期，可裁剪到具体项目阶段使用。建议每个项目开工时复制一份，逐项打勾。

### 7.1 决策阶段

- [ ] 已确认目标能力是否有官方 MCP Server 可用
- [ ] 已判断是否因内部系统或定制业务规则而必须自建
- [ ] 已明确 Server 的业务边界（哪些工具归这个 Server）

### 7.2 工具设计阶段

- [ ] 已穷举用户在该场景下的话术
- [ ] 已为每类话术匹配工具能力，并判断拆/合
- [ ] 已为每个工具定义入参、出参字段
- [ ] 返回值已同时包含程序字段（如 `status`）与 LLM 字段（如 `statusLabel`）
- [ ] 每个工具的 `description` 写清了调用时机与前置条件
- [ ] 已设计友好错误返回（`{"error": ...}` + `isError=true`）

### 7.3 实现阶段

- [ ] 独立工程创建、MCP SDK 依赖引入、应用可启动
- [ ] MCP Server Bean 已注册、SSE 端点已暴露（`curl /sse` 通过）
- [ ] 业务表与数据访问层已就绪
- [ ] 业务 Service 方法已实现且单测覆盖
- [ ] 工具已注册到 MCP 协议（`tools/list` 返回完整列表）
- [ ] 调用失败路径返回结构化错误而非抛异常

### 7.4 接入阶段

- [ ] Server 已在 Agent 平台注册
- [ ] 连通性测试通过
- [ ] 工具已绑定到目标 Agent

### 7.5 验收阶段

- [ ] 已用调试工具逐一验证每个工具行为
- [ ] 已用真实对话触发端到端流程
- [ ] LLM 调用时机符合 description 约束
- [ ] 敏感操作（如撤销、提交）的状态约束生效（如仅 PENDING 可撤销）
- [ ] 日志可见 tool_calls、记录正确入库

**第二部分 实战演示**

## 8. 场景与项目背景：智能客服退款痛点

<img src="imgs/aicent-25-fea-mcp-2/dcf5344d487403ff5548fff51c820b9b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二部分用 Java + Spring Boot + Java MCP SDK 完整复现一个退款 MCP Server。先看为什么选这个场景。

### 8.1 智能客服的"只能说，不能做"痛点

前一篇跑通了 MCP Client，智能客服能调外部工具了。但那个 Server 是模拟的，硬编码返回固定结果，不是真的查数据库。更深的痛点是：用户说"我要退款"，客服只能回答"好的，请您拨打客服热线"；用户说"我的退款什么时候到账"，客服只能说"请您耐心等待"。这个体验非常差。

### 8.2 为什么选退款场景自建

退款是智能客服最高频的诉求之一，但它涉及公司内部财务系统，没有公开的 MCP Server 可以用，必须自建。做这个真实的退款 MCP Server，连接内部财务系统，才能让 Agent 真正能"做事"，不只是"说事"。

退款场景恰好命中第一部分章 2 决策表的两条"必须自建"：财务系统是内部的（无公开 Server），退款又要走公司自己的审批流（需定制业务逻辑）。

## 9. 退款 Server 工具推导实战

<img src="imgs/aicent-25-fea-mcp-2/722cb4d33e0914d75353ace0eaaf37f3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节按第一部分章 4 的三步推导法，落地退款场景。

### 9.1 让 AI 助手按方法论推导

把第一部分的方法论写成提示词交给 Claude Code，让它代为推导：

```text
我要开发一个退款 MCP Server，供智能客服 Agent 使用。

从智能客服的真实场景出发——用户通常会说哪些关于退款的话？

每类诉求需要什么样的工具能力？

帮我推导出需要哪些工具，每个工具的输入输出是什么。
```

### 9.2 推导结果：四类用户话术 → 四个工具

按"用户会说什么 → 需要什么能力"的顺序推导：

| 用户话术 | 需要的能力 | 工具决策 |
| --- | --- | --- |
| "我要退款" | 先查资格，再提交申请 | **拆成两个工具**（一步直接提交用户无确认机会） |
| "我的退款审批了没" | 查退款进度，返回状态与预计到账时间 | 单独工具 |
| "不退了" | 撤销申请，仅 PENDING 可撤 | 单独工具 |
| "为什么退款被拒了" | 查拒绝原因 | **不单独建工具**，查状态时一并返回 |

最终推导出 4 个工具及其输入输出：

```text
check_refund_eligibility：查退款资格。入参：orderId。出参：eligible（能不能退）、reason（原因）、deadline（最晚可退日期）、amount（可退金额）。

submit_refund：提交退款申请。入参：orderId、reason。出参：refundId、status、estimatedDays。

get_refund_status：查退款状态。入参：orderId 或 refundId（两者都接受，用户记得订单号不记得退款单号，Server 里做转换比让用户多说一句话合理）。出参：status、statusLabel、estimatedArrival、rejectReason。

cancel_refund：撤销申请。入参：refundId。出参：success、message。
```

### 9.3 两个值得记住的设计细节

**① statusLabel 字段专门给 LLM 用**

`get_refund_status` 的 `statusLabel` 字段专门给 LLM 用，LLM 直接把 `statusLabel` 说给用户，不需要自己翻译 PROCESSING 是什么意思。工具的返回值设计要考虑 LLM 的使用方式，不是只考虑数据完整性。（详见第一部分章 5。）

**② 是否四个工具都做成 MCP Server**

这非常考验判断与鉴别能力——是不是这四个工作都要做成 MCP Server，还是可以只做一个满足这四点需求。这个问题留给读者结合自身项目思考。

### 9.4 一次完整退款对话的工具编排

Claude Code 还能帮忙拆解一次完整退款对话会用哪些工具。下图展示了 6 步退款对话中 2 个工具（`check_refund_eligibility` 与 `submit_refund`）的调用顺序，**调用顺序与时机由 LLM 自主决定**：

<img src="imgs/aicent-25-fea-mcp-2/5c0beb40076e3a88a0222cd303165e28_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-25-fea-mcp-2.md/0a234fe25eb6843300509f0da3aa8cc1_MD5.jpg
用途：展示一次完整退款对话中两个 MCP 工具（check_refund_eligibility、submit_refund）的调用顺序与编排，说明 LLM 自行决定工具调用时机。
内容：流程图呈现 6 步退款对话：1) 用户发起退款（订单号 ORD-001）；2) 调用 check_refund_eligibility 校验资格，返回 eligible=true、amount=299、deadline=2026-04-08；3) Agent 通知用户符合条件并询问是否确认；4) 用户确认；5) 调用 submit_refund 提交退款（reason=质量问题），返回 refundid=REF-001、status=PENDING、estimatedDays=3；6) Agent 告知用户退款已提交、预计 3 个工作日到账。图注强调本次对话调用了两个工具，调用顺序与时机均由 LLM 自主决定。
-->

## 10. 拆解实现步骤

<img src="imgs/aicent-25-fea-mcp-2/eba71eae74a71c171ad7837747457a54_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

工具确定后，让 Claude Code 按"每步可独立验证"的要求拆解实现步骤。这一步的价值在于：让模板化开发变得可控、可定位。

### 10.1 拆解提示词

```text
我要开发一个退款 MCP Server，独立的 Spring Boot 应用，用 Java MCP SDK。

提供四个工具：check_refund_eligibility、submit_refund、get_refund_status、cancel_refund，操作 refund_application 表。

帮我拆解实现步骤，从建项目到能被 Hify 调用，每步给出验证方式。
```

### 10.2 拆解结果

Claude Code 给出的 6 步拆解与第一部分章 6 的标准模板一致：

<img src="imgs/aicent-25-fea-mcp-2/48ef09aaf8462ddd837532800bb8e71c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-25-fea-mcp-2.md/16464ae72ee04505a23192a636cbc005_MD5.jpg
用途：展示 Claude Code 对退款 MCP Server 实现步骤的拆解结果（任务清单）
内容：一张三列表格（步骤 / 任务 / 验证方式），共 6 个步骤：①建 Spring Boot 项目并引入 MCP SDK（验证：编译通过、能启动）；②注册 MCP Server Bean、暴露 SSE 端点（验证：curl /sse 返回 SSE 流）；③建 refund_application 表、写数据访问层（验证：表自动创建、单测读写正常）；④实现四个工具的业务逻辑 Service（验证：单元测试覆盖每个方法）；⑤把业务逻辑注册到 MCP 工具（验证：tools/list 返回四个工具）；⑥在 Hify 中注册并端到端对话（验证：日志出现 tool_calls、记录入库）。
-->

每步都能独立验证，出了问题立刻知道在哪层找。接下来按这 6 步逐步实现。

## 11. 逐步实现关键细节

本节展开退款 Server 的实现细节。所有提示词、依赖、字段、curl 均可直接复用。

### 11.1 MCP Server 在代码里的样子

<img src="imgs/aicent-25-fea-mcp-2/31f2b4578de2b5455c4c7d0df233ee2b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

动手前先看清交付物。MCP Server 是一个独立的 Spring Boot 应用，和 Hify 完全分开部署，只通过 HTTP 通信。三层结构如下图所示：

<img src="imgs/aicent-25-fea-mcp-2/34850b13c362450224931d3aa3460f80_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-25-fea-mcp-2.md/f018bf0110a7a297cc289d2b7c6511d1_MD5.jpg
用途：以三层架构示意图直观展示 MCP Server 与 Hify 主应用的部署关系——MCP Server 作为独立的 Spring Boot 应用单独部署，两者之间仅通过 HTTP 进行通信。
内容：展示 Hify 主应用与 MCP Server 的分层关系，突出"独立部署 + HTTP 通信"两点，呼应前文对 MCP Server 定位的解释，作为引出后续目录结构说明的过渡图示。
-->

典型目录结构如下：

```text
hify-mcp-refund/                          ← 独立 Maven 项目
├── pom.xml
└── src/main/java/
    ├── RefundMcpApplication.java         ← Spring Boot 启动类
    ├── config/
    │   └── McpConfig.java                ← 注册工具到 MCP SDK
    └── service/
        └── RefundService.java            ← 真正的业务逻辑
```

直观地理解，MCP Server 就是一个 HTTP Server。对 Hify 而言它是黑盒——Hify 只存一个 endpoint 地址，发标准协议请求，不关心里面是 Java 还是 Python，不关心查的是 MySQL 还是 Oracle。这就是标准化协议的价值：

<img src="imgs/aicent-25-fea-mcp-2/34850b13c362450224931d3aa3460f80_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-25-fea-mcp-2.md/55687b610a32371a896bc7842f3609e7_MD5.jpg
用途：展示 MCP Server（hify-mcp-refund）的三层内部结构与 Hify MCP Client、数据库之间的关系，说明 MCP Server 对客户端而言是黑盒、通过标准 MCP 协议通信的架构价值。
内容：左侧是 Hify MCP Client（标注 MCP 协议），通过 MCP 协议箭头指向中间的 hify-mcp-refund 独立进程（监听 9002 端口）。该进程内部由上至下分三层：①工具定义层（apply_refund / query_refund_status 的 schema，浅紫色）；②工具实现层（Service 方法，浅绿色）；③MCP 协议层（tools/list 返回 schema、tools/call 分发调用，浅灰色）。右侧是数据库（refund_application 表，浅橙色），工具实现层有箭头指向该表，表示读写退款申请数据。整体说明客户端只通过标准协议与 Server 交互，不关心其内部实现与底层数据库。
-->

### 11.2 第 1-2 步：建项目，注册 MCP Server Bean

创建独立 Spring Boot 工程 `hify-mcp-refund`。引入依赖：

```xml
io.modelcontextprotocol.sdk:mcp-spring-webmvc:1.1.1
spring-boot-starter-web
```

配置类 `McpConfig.java`：

```text
注册 WebMvcSseServerTransport，路径 /messages
注册 McpSyncServer Bean，serverInfo 填 "refund-mcp-server" "1.0.0"
工具列表先空着，后面再注册
监听 9001 端口。
```

SDK 会自动暴露两个端点（开发者无需手写）：`GET /sse` 供 Hify 发现和监听，`POST /messages` 供 Hify 发工具调用请求，这两个端点由 SDK 处理。

验证：

```bash
curl -N http://localhost:9001/sse
```

### 11.3 第 3 步：建表和数据访问层

<img src="imgs/aicent-25-fea-mcp-2/b2c51084973184539ffdc1ea9835cffc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

建 `refund_application` 表：

```text
orderId、userId、amount、reason

status（PENDING/APPROVED/PROCESSING/COMPLETED/REJECTED）

rejectReason、createdAt、updatedAt
```

用 Spring Data JPA 实现 `RefundRepository`。加查询方法 `findTopByOrderIdOrderByCreatedAtDesc`——按订单号查最新的退款申请。

### 11.4 第 4 步：实现业务逻辑

实现 `RefundService`，四个方法对应四个工具。

`checkEligibility(orderId)`：

```text
查订单是否在退款期内（一期简化：7天内且已签收）
返回 Map：eligible、reason、deadline、amount
```

`submitRefund(orderId, userId, amount, reason)`：

```text
检查同一订单是否有 PENDING/APPROVED/PROCESSING 状态的申请
有则返回错误：该订单已有进行中的退款申请，编号：xxx
无则写入 refund_application，status=PENDING
返回 Map：refundId、status、statusLabel、estimatedDays=3
```

`getStatus(orderId)`：

```text
查最新的退款申请记录
status 用英文枚举，statusLabel 用中文给 LLM 直接说给用户
返回 Map：refundId、orderId、amount、status、statusLabel、
        submittedAt、rejectReason
```

`cancelRefund(refundId)`：

```text
只有 PENDING 状态可以撤销
PENDING 以外返回：退款已在处理中，无法撤销
```

业务层约束（呼应第一部分章 5 的设计原则）：

```text
- 异常情况返回友好提示，不要抛出技术错误信息给 LLM
- 返回值 LLM 会直接读，设计时考虑 LLM 怎么把它说给用户
```

### 11.5 第 5 步：注册工具到 MCP 协议

在 `McpConfig` 里，把四个工具注册到 `McpSyncServer`。每个工具需要：

```text
工具名称（name）

description——重点：说清楚"什么情况下调这个工具"

inputSchema（JSON Schema 格式）

handler（调用 RefundService 对应方法，结果序列化为 JSON 返回）
```

`description` 示例写法（详见第一部分章 5）：

`check_refund_eligibility`：

```text
"查询订单退款资格。用户说'我要退款'时，先调此工具确认是否符合条件，
再决定是否提交申请。不要跳过此步直接提交。"
```

`submit_refund`：

```text
"提交退款申请。仅在用户确认退款意愿、且 check_refund_eligibility
返回 eligible=true 后调用。"
```

调用失败时返回 `{"error": "错误原因"}`，`isError=true`——不要让 SDK 抛出异常中断整个对话。

验证（先验 SSE，再验工具列表）：

```bash
curl -N http://localhost:9001/sse
```

```bash
curl -X POST "http://localhost:9001/messages?sessionId=xxx" \
  -H "Content-Type: application/json" \
  -d '{"jsonrpc":"2.0","id":1,"method":"tools/list","params":{}}'
```

## 12. MCP 调试工具实现

<img src="imgs/aicent-25-fea-mcp-2/20f07a875825617395d7749213d8f57c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Server 自身跑通后，下一步是接入 Hify。但在接入之前，Hify 需要一个调试工具——开发 MCP Server 时能直接在 Hify 里验证工具行为，不用手动拼 curl。

### 12.1 为什么放在 Hify 侧边栏

这个调试工具放在 Hify 侧边栏的 MCP Server 管理页面里。理由是：MCP Server 接入是 Hify 的通用基础能力，调试工具也应该在这里，以后每接一个新 Server 都在同一处调试。

### 12.2 前端：调试 Tab 的功能设计

在 Hify 前端的 MCP Server 详情页中，新增"调试"Tab。功能如下：

```text
1. 左侧：工具列表（从 mcp_tool 表读取），点击选中工具

2. 右侧调试面板：

- 顶部显示工具 description（让开发者确认描述是否合理）

- 根据 inputSchema 自动渲染参数表单

  string → 文本输入框，number → 数字输入框，必填标红星

- 调用按钮 + 结果展示区

  结果显示返回内容 + 耗时

  保留最近 5 次调用记录
```

### 12.3 后端：调试接口

```text
POST /api/v1/mcp-servers/{id}/debug

入参：toolName、arguments（Map）

逻辑：复用 McpClientService.callTool()

返回：result（String）、elapsedMs（Int）
```

### 12.4 实现约束

```text
- 参数表单根据 inputSchema 动态渲染，不要写死字段
- 调用中 loading，防止重复点击
```

### 12.5 实现效果

按上述提示词让 Claude Code 生成页面，细节需要不断与它对话打磨。最终效果如下图（左：整体布局；右：调试面板）：

<img src="imgs/aicent-25-fea-mcp-2/73bb16f4a2738ab635c071ca2fa3ca7b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-25-fea-mcp-2.md/76275b3eb16a1abdf035ea9406316cd8_MD5.jpg
用途：展示 Hify 前端 MCP Server 详情页"调试"Tab 的整体页面布局效果图，印证 Claude Code 根据提示词生成的调试页面成果
内容：MCP Server 详情页调试 Tab 的整体视图。页面顶部为 MCP Server 详情的 Tab 导航（基本信息/工具/调试等），选中"调试"Tab 后呈现左右分栏布局：左侧为该 MCP Server 暴露的工具列表，每项展示工具名称及简要说明，可点击选中；整体用于在页面上直接选择工具并发起调试调用，无需借助外部客户端
-->

<img src="imgs/aicent-25-fea-mcp-2/b7d3d47aa7d3d039bfbbf8c6777516df_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-25-fea-mcp-2.md/ae1a710658b471b05cf186722bda69f2_MD5.jpg
用途：展示 Hify 前端 MCP Server 详情页"调试"Tab 的最终效果图，作为本节"用 Claude Code 生成调试页面"的成果印证
内容：MCP Server 详情页调试面板效果图。右侧为工具调试面板：顶部展示选中工具的 description 说明文字；中部根据 inputSchema 动态渲染参数输入表单（含参数名、类型、是否必填等字段，支持填写 JSON/字符串等参数值）；下方提供"调用"按钮发起工具调用，并展示返回结果（result 内容）与耗时（elapsedMs）；面板保留最近 5 次调用记录便于对比排查，调用中显示 loading 防止重复点击
-->

## 13. 接入 Hify 与端到端验收

<img src="imgs/aicent-25-fea-mcp-2/d5d42960875554b7dac0743cf3a9f424_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

调试工具就绪后，把退款 Server 接入 Hify 并做端到端验收。

### 13.1 三步接入：注册、测试、绑定

注册退款 Server：

```bash
curl -X POST http://localhost:8080/api/v1/mcp-servers \
  -H "Content-Type: application/json" \
  -d '{"name": "退款服务", "endpoint": "http://localhost:9001"}'
```

测试连通：

```bash
curl -X POST http://localhost:8080/api/v1/mcp-servers/1/test
```

把工具绑定到 Agent：

```bash
curl -X PUT http://localhost:8080/api/v1/agents/1/tools \
  -H "Content-Type: application/json" \
  -d '{"toolIds": [1, 2, 3, 4]}'
```

Server 从模拟换成真实的，Hify 的 MCP Client 一行代码没有改。这就是标准化协议的价值——工具提供方换了，调用方不用动。完整端到端流转见下图：

<img src="imgs/aicent-25-fea-mcp-2/b0867db1e3679ad1e28399686337a7e0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-25-fea-mcp-2.md/e38c179453e2ddf9a13ae74579408627_MD5.jpg
用途：展示接入真实退款 MCP Server 后，用户退款请求在 Hify 与 LLM 之间端到端流转的完整架构与数据流向
内容：架构流程图，自上而下展示退款申请的处理链路：顶部 refund_application 表（写入审批流记录）；中间从用户发起"我要退款"开始，经 Hify（第一次 LLM 调用，携带 tools schema）→ LLM（决定调用 apply_refund）→ 退款 MCP Server（写入审批流并返回申请单号）→ Hify（第二次 LLM 生成回答）；底部状态消息显示最终回复："已为您提交退款申请，申请单号 6，预计 1-3 个工作日"。各组件用不同浅色块区分角色，箭头标明数据流向。
-->

### 13.2 端到端验收：四类对话

先用调试工具验证工具本身，再测端到端对话。工具验证通过后，用以下四类对话覆盖完整退款流程。

退款申请：

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/2/messages \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"content": "我的订单ORD-001收到的商品是破损的，我要退款"}'
```

查退款进度：

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/2/messages \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"content": "我的退款审批了吗"}'
```

撤销退款：

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/2/messages \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"content": "算了不退了"}'
```

退款政策咨询：

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/2/messages \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"content": "你们的退款政策是什么"}'
```

### 13.3 前端集成效果

下图是 MCP Server 集成到对话引擎后，在前端对话界面的实际运行效果：

<img src="imgs/aicent-25-fea-mcp-2/4bff1c40748183f97decb24337486686_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-25-fea-mcp-2.md/4bff1c40748183f97decb24337486686_MD5.jpg
用途：展示 MCP Server 工具集成到智能客服对话引擎后，在前端对话界面的实际运行效果，作为验收章节的端到端效果截图。
内容：智谱AI 客服系统的对话前端界面。左侧深色导航栏包含模型管理、Agent、知识库、工作流、MCP 工具、对话等模块；中间为"对话列表"面板，当前选中"智能客服"会话（预览显示"退款申请已成功提交！- 退款单..."）；右侧为对话主界面，展示一段退款场景的真实对话——用户询问订单 20240501001 是否可退款，AI 回复该无线蓝牙耳机（¥299.00）在 7 天无理由退货期内符合退款条件，用户确认后 AI 通过调用 MCP 工具成功提交退款申请，返回退款单号 RF20260408001、退款金额 ¥299.00、状态"审核中"、预计 1-3 个工作日到账的结构化结果。
-->

## 14. 总结与延伸思考

<img src="imgs/aicent-25-fea-mcp-2/b70a11cc97cc0c4f4de70ec8dcf0e121_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 14.1 本篇做了什么

这两篇（MCP 工具接入上下）做了一件完整的事：让智能客服从"只能说"变成"真的能做"。

### 14.2 MCP Server 开发高度模板化

MCP Server 的开发非常模板化——声明 schema、实现逻辑、注册协议，每个 Server 都是同一个套路。做明白了退款 Server，库存 Server、工单 Server 只需要换 schema 和业务逻辑，其他完全一样。<span style="color: red; font-weight: bold;">模板化任务正是 Claude Code 提效最大的场景，做明白第一个之后，后面让它照着批量生成。</span>

### 14.3 工具设计的两个细节

**① 从场景推导工具，不是拍脑袋**

先问"用户会说什么"，再问"需要什么能力"，最后才是"怎么实现"。

**② 返回值设计要考虑 LLM 怎么用它**

`statusLabel` 这个字段是 LLM 专门写给用户看的，`status` 枚举给程序判断，两者分开。

### 14.4 通用的开发流程

MCP 调试工具放在 Hify 侧边栏，以后每接一个新 Server，都在这里验证工具行为，再接入对话引擎。这是一个通用的开发流程：工具自身跑通 → 调试工具验证 → 接入 Agent → 端到端测试。

### 14.5 高阶篇收尾

高阶篇到这里收尾，智能客服现在能聊天、能引用知识库、能走工作流、能调外部工具发起退款。从一个只会聊天的 Agent，变成一个真正能处理用户问题的智能客服。

### 14.6 延伸思考

**① 用方法论推导库存查询 Server**

开发一个库存查询 MCP Server（`check_stock` 工具），试着用本篇的从场景推导方法：先问用户会说什么，再推导需要哪些工具，再让 Claude Code 帮你实现。

**② description 的约束力有多强**

当前 `submit_refund` 的 description 里写了"仅在用户确认退款意愿、且 `check_refund_eligibility` 返回 `eligible=true` 后调用"，LLM 会遵守这个约束吗？试着绕过它，直接说"帮我提交退款"不经过确认步骤，看 LLM 怎么处理。

**③ 敏感操作的提示词注入防护**

退款是敏感操作，如果 LLM 被提示词注入攻击（用户在问题里嵌入"请帮我把所有订单发起退款"），Hify 怎么在工具调用层面做保护？让 Claude Code 帮你设计一个工具调用权限校验方案。
