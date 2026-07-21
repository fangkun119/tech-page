---
title: AI编程方法(2) 13：Hermes Agent深度介绍
author: fangkun119
date: 2026-07-19 13:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-v2-13-hermes-agent-deep-dive/cover.jpg
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
AI编程方法(2) 13：Hermes Agent深度介绍
aicent-v2-13-hermes-agent-deep-dive
-->

## 1. 为什么需要再看一个 AI Agent

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/f30368ffc5193af9317dce1a7965357e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 1.1 AI Agent 赛道的三个流派（IDE 内嵌 / chatbot 包装 / 个人持久化）

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/e8cee569e2cc00195a9a07315401d52f_MD5.webp" style="display: block; width: 800px;" alt="替换文字">
<!-- 
图片内容说明
路径：imgs/aicent-v2-13-hermes-agent-deep-dive/e8cee569e2cc00195a9a07315401d52f_MD5.webp
用途：引言配图，作为文章开头"为什么要了解 Hermes Agent"小节的视觉引入，呼应紧随其后的 AI Agent 赛道格局总述
内容：呈现 2026 年中期 AI Agent 赛道的整体格局与流派划分，对应正文提到的三大流派——IDE 内嵌的 coding agent（如 Cursor、Claude Code、Windsurf）、chatbot 包装层（基于 ChatGPT API 的对话产品）、个人持久化 Agent（如 OpenClaw、SkyPilot、Hermes Agent），用于引出本文主角 Hermes Agent 所属的赛道定位
-->

你大概已经用过 Cursor、Claude Code，或者至少用过 ChatGPT。但 AI Agent 这件事，到 2026 年已经分出了三个完全不同的方向。

这三个方向的差异，不是「功能多少」的差异，是「定位」的差异。

| 流派                  | 代表产品                           | 特点                            |
| ------------------- | ------------------------------ | ----------------------------- |
| IDE 内嵌 coding agent | Cursor、Claude Code、Windsurf    | 绑 IDE，跟代码强相关，IDE 关掉 agent 就停了 |
| chatbot 包装层         | 各种基于 ChatGPT API 的产品           | 本质是把对话能力套个 UI                 |
| 个人持久化 Agent         | OpenClaw、SkyPilot、Hermes Agent | 长期跟个人一起成长，活在服务器上              |

用传统软件工程打比方：IDE 内嵌 agent 像 IDE 插件，能力挂在宿主进程上，宿主一退它就退；chatbot 包装层像 REST 客户端套壳，核心逻辑全在后端 LLM；个人持久化 Agent 像你雇的一个 24 小时在岗的私人助理服务，跑在自己的服务器上，跟你的设备解耦。

本文聚焦第三类，主角是这条路上的代表项目 Hermes Agent。

### 1.2 Hermes Agent 值得你花时间的四个理由

为什么要在已经拥挤的 Agent 赛道里单独花时间研究它？下面四个理由构成它的独特性。

#### (1) 设计哲学独特：闭环、自管记忆、自创技能

Hermes Agent 的核心设计哲学有三块拼图：闭合学习循环（closed learning loop）、Agent 自管记忆（agent-curated memory）、自主创建技能（autonomous skill creation）。这套组合在业界 Agent OS 里很有代表性。

这三个术语的机制留到第 4 章展开。这里只点一句：它们共同回答了一个普通 LLM-wrapper 回答不了的问题——「Agent 的能力天花板由什么决定」。

#### (2) 工程实现完整：v0.14.0 + 134k+ star，不是玩具

`v0.14.0` 已经发布，GitHub 上 134k+ star。它不是 demo，也不是论文配套代码，是一个有持续开发节奏的成熟项目。根据 release notes，一个版本通常包含 180+ commit，项目处于高速演进期。

这意味着它的设计哲学不只是 PPT，是已经在真实负载下跑起来的工程实现。

#### (3) 覆盖场景全：CLI + 20+ 平台 + IDE + cron + subagent

CLI 加 20+ 消息平台（Telegram、Discord、Slack、WhatsApp、Signal、飞书、企业微信等）加 IDE 集成（VS Code、Zed、JetBrains 通过 ACP 协议）加 cron 自动化加 subagent 并行。它把「Agent 能做的事」做到了一个开源项目能做到的极致。

这不是功能清单的堆叠，是「一个 Agent 能在多少地方陪你」的覆盖度问题。

#### (4) 作为对照，能让你看清 Agent OS 的边界

理解 Hermes 后，你能更清楚地评判其他 Agent 项目的设计取舍。它是一面镜子——照出哪些是「LLM 给的能力」，哪些是「Agent 自己积累的能力」，哪些是「平台策略」而不是「技术天花板」。

我的看法是 Hermes Agent 是 2025 到 2026 年开源 Agent 领域最值得研究的项目之一。它不是最完美的，但它在「个人持久化 Agent」这条路上做得最远。

那 Hermes Agent 到底是什么？下一节给出准确定义。

## 2. Hermes Agent 是什么

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/66077593d4bbd5a81d4880fb5df6b7e2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 一句话定位

<span style="color: red; font-weight: bold;">Hermes Agent 是 Nous Research 开发的开源、自我改进的个人 AI Agent，它跟人一起成长。</span>

官方 slogan 是 "The agent that grows with you"。这句话不是营销话术，是 Hermes 最核心的设计哲学：它不是一次性工具，是一个越用越懂你的长期伙伴（persistent personal agent）。

那么问题来了——「跟人一起成长」到底什么意思？模型权重又不会变。答案藏在本章后面几节的属性里，真正的机制展开在第 4 章。这里先埋一个钩子：Hermes 的「成长」不是改模型，而是 Agent 自己积累记忆、自己写技能、自己跑闭环。

### 2.2 关键属性

属性一眼看全，机制留到第 4 章展开。本章只列属性，不做痛点—机制映射（那在第 3.2 节）。

| 属性    | 说明                                                                                                        |
| ----- | --------------------------------------------------------------------------------------------------------- |
| 开源    | MIT 协议，完全开源，可以自部署                                                                                         |
| 自我改进  | 内置 closed learning loop，从交互中学习、创建技能、改进自己                                                                  |
| 持久化   | 跨重启保留记忆、技能、会话历史；不依赖笔记本，可以跑在 5 美元 VPS 上                                                                    |
| 多平台接入 | CLI + 20+ 消息平台（Telegram、Discord、Slack、WhatsApp、Signal、飞书、企业微信等）+ IDE 集成（VS Code、Zed、JetBrains via ACP 协议） |
| 多模型兼容 | 支持 Nous Portal、OpenRouter、OpenAI、Anthropic、Gemini 等几乎所有主流 LLM Provider，任意切换无 lock-in                      |
| 多后端执行 | 本地、Docker、SSH、Singularity、Modal、Daytona 六种后端，从笔记本到 serverless 都能跑                                         |
| 多人协作  | spawn subagent 并行做事，每个 subagent 有自己的对话和终端                                                                 |

「跨平台」「多模型」这两项本章一句话带过，痛点场景留到第 3.2 节，原理细节留到第 4 章架构图，真正展开在 5.3 节。

### 2.3 它明确不是什么

光说「是什么」还不够，Hermes 的定位要看它**明确不是什么**。

| 它不是            | 为什么不是                                     |
| -------------- | ----------------------------------------- |
| coding copilot | 不绑 IDE，不针对代码写作场景，不像 Copilot 那样实时建议代码      |
| chatbot 包装层    | 不只是 LLM API 的 UI 壳，它有自己的核心引擎、记忆系统、技能系统    |
| IDE 插件         | 虽然有 IDE 集成，但本质是独立运行的 agent，IDE 只是众多接入入口之一 |
| 云服务            | 默认本地部署，数据在你的机器或你的 VPS 上                   |
| 企业 Agent OS    | 定位个人使用，企业部署不是它的设计目标                       |

<span style="color: red; font-weight: bold;">Hermes 的定位很清晰：一个属于你个人的、跟你长期一起成长的 AI Agent。</span>

### 2.4 项目背景

Hermes Agent 是 Nous Research 开发的项目。Nous Research 在 AI 圈是个有趣的存在——他们既训练开源大模型（Hermes 系列模型、Nomos、Psyche），又做开源 Agent 工具（Hermes Agent 本身）。项目由模型训练者亲自下场做 Agent，这种组合在业界不多见。

为什么这点值得专门拎出来讲？因为这意味着一个隐藏的数据闭环：Agent 在真实使用中积累的交互数据，天然可以反哺模型训练。传统软件工程里有个类比——既造发动机又造整车的车厂，从发动机工况到整车调校的数据链路天然打通，每一环的反馈都能直接改进另一环。Nous Research 就处在这样的位置，第 5.5 节会展开这个闭环。

项目当前版本是 `v0.14.0`，发布日期 2026 年 5 月 16 日。根据 release notes，单个版本通常包含 180+ commit，项目处于持续高速演进期。版本号、日期、commit 数都是原文事实，这里一字未改——目的是让你对项目的活跃度有真实判断，而不是被我转述模糊掉。

## 3. 它解决什么问题：五个真实痛点

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/6d9cb9be47321b29459b282a276a516e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 3.1 痛点清单（记忆缺失 / 不会学习 / 跨平台脱节 / 不能持续 / 绑定设备）

如果你用过 Cursor 或 Claude Code，下面这五个坑你大概率踩过。

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/f5a47a870fcacc056ac49c27b67049d4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicent-v2-13-hermes-agent-deep-dive/f5a47a870fcacc056ac49c27b67049d4_MD5.jpg
用途：以图示方式直观呈现当前 AI Agent 用户面临的 5 大核心痛点，为后文引出 Hermes Agent 的针对性解决方案做铺垫
内容：归纳并可视化展示 AI Agent 用户的五大真实痛点——① Agent 没有记忆（无状态，每次会话都需重新交代背景）；② Agent 不会学习（能力来自 LLM 而非自身经验积累）；③ Agent 跟真实生活脱节（只在 IDE 或自有 UI 中出现，不进入 Telegram、Slack、WhatsApp 等日常活动场景）；④ Agent 不能持续做事（缺乏 cron 调度，需手动触发）；⑤ Agent 绑定本地设备（笔记本关机即停，不够稳定可靠）
-->

#### (1) 记忆缺失：每次会话都要重讲你是谁

大部分 Agent 是无状态的。每次新开一个会话，你都得重新告诉它你是谁、在做什么项目、有哪些偏好。这不是把 context window 调大就能解决的问题——你不可能每次都把所有历史粘贴进 prompt。

换个工程师熟悉的说法：无状态 Agent 像 REST API 的无状态服务，每次请求都要带全部上下文；而你真正想要的是一个有 session 的服务，能记住你是谁。

#### (2) 不会学习：教过的下次还不会

你花半小时教 Agent 怎么处理一类任务，下次它还是不会。原因在于：没人教过它「自己总结经验」。Agent 的能力是 LLM 自带的，不是它自己一点点攒出来的。LLM 不变，它就不变。

#### (3) 跨平台脱节：它不在你真实活动的地方

你的日常分散在 Telegram、Slack、WhatsApp 这些平台里。但 Agent 通常只能活在 IDE 或它自己的 UI 里，根本不出现在你真实活动的地方。你为了找它，要专门切换到一个独立的窗口。

#### (4) 不能持续：没有 cron，必须手动触发

你想要的是「每天早上 9 点给我一份新闻摘要」「每周一扫一遍代码库 PR 状态」。但绝大多数 Agent 没有 cron（定时调度）能力，必须你手动触发一次才动一次。它永远在「等你叫」。

#### (5) 绑定设备：笔记本关机它就停

Agent 跑在你的笔记本上，笔记本关机、重启、断电、断网，它就停了。一个真正能用的助手应该跟你的设备解耦，有自己的「家」。

### 3.2 Hermes 的逐个回应（痛点—机制映射，用表格）

五个痛点，Hermes 都有对应机制。这里只做映射，机制细节留到第 4 章展开。

| 痛点    | Hermes 的机制                                                                    | 传统软件工程类比                                          |
| ----- | ----------------------------------------------------------------------------- | ------------------------------------------------- |
| 记忆缺失  | agent-curated memory（`MEMORY.md` + `USER.md`），每次会话注入 system prompt            | 像用户 profile / CRM 客户档案，但由 Agent 自己读写，不是人填         |
| 不会学习  | autonomous skill creation（5+ tool calls 后自动写 `SKILL.md`，使用中 self-improvement） | 像团队的 Runbook / SOP，但 Agent 自己写、自己改、自己归档           |
| 跨平台脱节 | multi-platform messaging gateway（一个 gateway 接 20+ 平台，跨平台会话延续）                 | 像 API Gateway（Kong / Nginx），session 在 gateway 层共享 |
| 不能持续  | cron scheduler（自然语言定义任务，自动调度，可推送到任何平台）                                        | 完全对应 Linux cron / Airflow，只是用自然语言定义任务             |
| 绑定设备  | server-resident（5 美元 VPS；Daytona / Modal serverless，idle 时 hibernate）         | 像传统后端服务部署，服务跑服务器上 24h 在线，笔记本只是 client             |

### 3.3 一张对比表看清差异（ChatGPT / Claude Desktop / Cursor Agent / Hermes）

把 Hermes 和市面上常见的「AI 助手」放在同一张表里，差异就清楚了。

| 产品 | 记忆 | 平台 | 持续性 | 定位 |
|---|---|---|---|---|
| ChatGPT 网页版 | 有限 | 单一（网页） | 关网页即结束 | 网页对话工具 |
| Claude Desktop | 绑设备 | 单一（Mac 桌面） | 离设备就不在 | 桌面应用 |
| Cursor 内 Agent | 绑 IDE 会话 | 单一（IDE 内） | IDE 关 agent 停 | IDE 内 coding 助手 |
| Hermes Agent | 记得偏好（自管记忆） | 20+ 平台 + CLI + IDE | VPS 上 24h 在线 | 个人持久化伙伴 |

差异不是「功能多少」，是定位的差异。Hermes 想做的，是一个属于你的、独立于任何设备和平台的、长期成长的伙伴。

## 4. 技术原理：closed learning loop 是怎么转起来的

前面三章把「Hermes 是什么」和「为什么值得看」讲清楚了。这一章是全文重点章，篇幅最长，回答的问题是：**它到底是怎么做到越用越能干的？**

原理章的核心是 closed learning loop（闭合学习循环）。其余机制——记忆、技能、subagent、安全——都是挂在这根脊梁上的肌肉。下面从整体架构开始，逐层拆到核心循环。

### 4.1 整体架构：三层结构

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/32a5a1c5463dbfe17015a11c84de4d89_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Hermes Agent 的架构分三层：

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/a3abf927085cb4e3c65a83b40014870a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!-- 
图片内容说明
路径：imgs/aicent-v2-13-hermes-agent-deep-dive/a3abf927085cb4e3c65a83b40014870a_MD5.jpg
用途：直观展示 Hermes Agent 的三层分层架构，帮助读者理解从用户入口到 AI 大脑再到执行环境的完整数据流转链路
内容：自上而下分三层呈现系统架构。顶层为用户接入层，列出 CLI、TUI、Messaging Gateway（覆盖 20+ 平台）、IDE 集成（基于 ACP 协议）、Cron Scheduler、OpenAI 兼容 REST API 等多种交互入口；中间为核心引擎层，作为 AI Agent 的大脑，包含记忆系统、技能引擎、学习循环、定时调度、安全控制、MCP 集成、Model Router、Subagent 编排等模块；底层为执行后端层，展示实际运行 tool calls 的多种环境，包括本地终端、Docker、E2B、SSH、Singularity（面向 HPC）、Modal 与 Daytona（serverless）。三层之间通过自上而下的调用关系串联
-->

这跟传统 Web 应用的三层结构同构：**接入层（Nginx / API 网关）+ 应用层（业务服务）+ 数据/执行层（数据库 + worker）**。也对应微服务架构里的「API Gateway + 业务服务 + 存储与执行单元」。每层职责分明，下层不知道上层长什么样。

#### (1) 用户接入层：你从哪里找 Hermes

这一层是所有入口的集合。CLI 和 TUI 给命令行用户；Messaging Gateway（消息网关）覆盖 20+ 平台，跨平台接入在一句话里点到这里，具体展开留到 5.3 节；IDE 集成走 ACP 协议（Agent Client Protocol）；Cron Scheduler 用自然语言定义定时任务；OpenAI 兼容的 REST API 让 Hermes 能被任何 OpenAI SDK 直接调。

类比 API Gateway：**一个入口层，统一接住所有外部调用**，下游只看到统一的 Agent 接口。

#### (2) 核心引擎层：Hermes 的大脑

中间层是大脑。包括记忆系统、技能引擎、学习循环（closed learning loop）、定时调度、安全控制、MCP（Model Context Protocol）集成、Model Router（模型路由）、Subagent 编排。

这一层对应传统 Web 应用里的业务服务层。**所有「智能」相关的逻辑都在这里**：选哪个模型、读哪段记忆、调哪个 skill、是否要 spawn subagent，都由引擎层决策。

#### (3) 执行后端层：tool call 在哪跑

最底层负责实际执行 tool call。本地终端是默认；Docker 容器做隔离；E2B 是托管沙箱；SSH 连远程机器；Singularity 面向 HPC（高性能计算）；Modal 加 Daytona 提供 serverless（无服务器）执行。

这是「数据/执行层」的 Agent 版本。**tool call 不是跑在 Agent 主进程里，而是被派发到合适的执行后端**——就像传统后端把重活儿扔给 worker 或独立服务。某些 backend 还支持 hibernate（休眠），用完即睡、下次秒起。

### 4.2 核心引擎 AIAgent：像 orchestration 引擎一样的工作方式

核心引擎的最核心类是 `AIAgent`，定义在 `run_agent.py` 里。这是一个**同步执行的 orchestration engine**（编排引擎）。

类比传统后端的工作流引擎（Airflow、Temporal）：**它自己不干活，它负责调度一切**。一个 turn（回合）内，AIAgent 依次完成以下职责：

- 选 Provider（通过 Model Router 选模型）
- 构造 prompt（调 prompt_builder）
- 执行 tool call（派发到执行后端）
- 重试（失败后换策略再来）
- fallback（主模型不可用切备用）
- 回调（hook 机制，方便扩展）
- 压缩（context 太长自动摘要）
- 持久化（对话、记忆、技能落地到磁盘）

`AIAgent` 支持三种 API 模式，覆盖几乎所有主流 Provider：

| API 模式 | 适用 Provider |
|---|---|
| Anthropic 风格 | Claude 系列模型 |
| OpenAI Chat Completions 风格 | OpenAI + 大部分第三方 Provider |
| OpenAI Responses API 风格 | OpenAI 新模型 |

差异藏在 `ProviderTransport` ABC（抽象基类）背后。每个 Transport 负责自己的消息格式转换、tool 格式转换、kwargs 组装、响应规范化。**上层 AIAgent 代码不知道、也不需要知道当前在调哪种 API**。

这是教科书级的抽象隔离，类比 SLF4J（日志门面）或 JDBC（数据库驱动）：**底层差异藏到接口后面，上层代码不用改**。<span style="color: red; font-weight: bold;">换模型像换数据库驱动一样，业务逻辑一行不动。</span>

### 4.3 system prompt 是动态拼出来的（prompt_builder.py）

你可能会问：**为什么不直接写一个长 system prompt，把所有规则一次性塞给模型？**

答案在于「谁来维护」。写死的 prompt 是你写的，**它不会自己长大**。Hermes 反过来——prompt 由 `prompt_builder.py` 在运行时拼出来，每次调用都不同，内容随你的使用历史动态膨胀。

`prompt_builder.py` 把 6 个片段拼成最终 system prompt：

| 片段 | 来源文件 | 作用 |
|---|---|---|
| personality | SOUL.md | Agent 的人格设定 |
| memory | MEMORY.md + USER.md | 长期记忆 + 用户画像 |
| skills | 当前会话相关 skill documents | 技能库 |
| context files | AGENTS.md + .hermes.md | 项目上下文 |
| tool-use guidance | 内置 | 工具使用指南 |
| model-specific instructions | 内置 | 针对当前 LLM 的特定指引 |

类比传统模板引擎（Jinja2、Thymeleaf）：**模板是骨架，运行时把变量渲染进去**。Hermes 的模板就是这 6 个槽位，变量来自你日积月累的记忆和技能文件。

这是个性化的核心。<span style="color: red; font-weight: bold;">每个人的 Hermes 实例 system prompt 都不一样</span>，因为每个人的 MEMORY/USER/SKILLS 都不一样。两个用户用同一份代码、同一个模型，跑出来的 Agent 行为可以完全不同。

### 4.4 记忆系统：MEMORY.md + USER.md（agent-curated memory）

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/2a9074d5e1b1d9d90dc494550a36cc3b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

记忆系统看起来只有两三个 Markdown 文件，但设计哲学藏在「谁来写」这件事上。

#### (1) MEMORY.md：事实笔记

Agent 在对话中遇到值得长期记的事——用户偏好、项目背景、关键决策——**主动**写入 `MEMORY.md`。不是用户告诉它「请记住这个」，是 Agent 自己判断「这条值得留」。

Hermes 还会定期 nudge（轻推）Agent：**提醒它把当前对话的关键信息沉淀到 MEMORY.md**，避免「聊完就忘」。

#### (2) USER.md：结构化用户画像

`USER.md` 比 MEMORY 更结构化，记录用户的角色、技能、偏好、典型工作流。

它通过 Honcho 集成做 dialectic user modeling（辩证式用户建模）——一种通过持续对话不断深化对用户理解的技术。**画像不是一次性写死的，是 Agent 在每次交互中持续修正的**。

#### (3) FTS5 cross-session search：跨会话全文搜索

Hermes 用 SQLite 的 FTS5（全文搜索扩展）让自己的历史对话变得可检索。配合 LLM 总结，Agent 能回忆起几个月前的对话细节。

原文有个例子很到位：「我们上次聊那个 RocksDB 优化是怎么决定的？」——**Hermes 搜得到**。这是普通 chatbot 完全做不到的能力，因为它们的会话是断的，没有跨会话索引。

记忆系统的设计哲学是 **agent-curated memory**（Agent 自己管理的记忆）。不是「用户传 system prompt 给 Agent」，是「Agent 自己维护自己的 system prompt 输入」。像有个私人秘书，自己整理笔记、自己决定哪条归档、哪条丢掉。

类比传统软件里的用户 profile：CRM 里销售按固定 schema 填客户信息，**schema 是系统定的，内容是人填的**。Hermes 完全反过来——**没有固定 schema，内容是 Agent 自己写的**，Agent 自己决定记什么、忘什么、怎么重组。

### 4.5 技能系统：autonomous skill creation

如果说记忆系统是「Agent 记得」，技能系统就是「Agent 会做」。

每个 skill 是一份 SKILL.md 文件，结构固定：

| 段 | 内容 |
|---|---|
| What | 这个 skill 做什么，什么时候用，什么时候不用 |
| How | 具体怎么做，分步骤 |
| Pitfalls | 踩过的坑 |
| Verification | 怎么验证做对了 |

类比传统团队的 Runbook / SOP / playbook：**做完一个复杂任务，把步骤写下来，下次新人照做**。一份合格的 Runbook 必须回答四件事——做什么、怎么做、踩过什么坑、怎么验证做对了。SKILL.md 就是这四件事的 Agent 版本。

关键区别是：**skill 不是代码，是给 LLM 看的指南**。它告诉模型「遇到这种场景，按这些步骤做，注意这些坑」，而不是用程序逻辑硬编码。这让 skill 可读、可改、可迁移。

技能系统有三个特殊机制，是它区别于普通 Runbook 的地方：

#### (1) autonomous skill creation：复杂任务后自动写

当 Agent 完成一个复杂任务（**5+ tool calls**），它会自己判断：**这值得抽象成 skill 吗？** 如果值得，自己写出 SKILL.md，落到磁盘。整个过程不需要用户介入。

这是技能系统的源头活水。**用得越多，skill 越多**。

#### (2) skill self-improvement during use：用中自我改进

Agent 在用某个 skill 时，如果发现描述过时、步骤不全、有 bug，会**主动 patch（打补丁）**——不是等用户报错，是自己边用边修。

skill 越用越好，越用越准。

#### (3) autonomous curator：定期整理

skill 越积越多会变成垃圾。Hermes 提供 `hermes curator` 子命令（v0.13.0 引入），**定期 review 所有 skill，整合重叠的、归档过时的**。这是 Agent 版的「知识库治理」。

skill 兼容 agentskills.io 开放标准。**Hermes 写的 skill 能被其他兼容 Agent 用，反过来也行**。这让 skill 不锁在 Hermes 生态里——技能可移植，投资不沉没。

类比传统 Runbook：**Runbook 是团队手写的、人维护的**，跑偏了要靠新人反馈。Hermes 的 skill 是 Agent 自己写、自己改、自己归档的——<span style="color: red; font-weight: bold;">整个生命周期不需要人维护</span>。这是质变。

### 4.6 闭环：closed learning loop（点睛之笔，把 4.4 和 4.5 串起来）

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/066b4845fea3c472be55da5579cbc871_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面五节铺了这么多机制，这一节把它们串起来。**这是全文最重要的章节**。

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/5063ca648547c03dc69500fea8dcfa77_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!-- 
图片内容说明
路径：imgs/aicent-v2-13-hermes-agent-deep-dive/5063ca648547c03dc69500fea8dcfa77_MD5.jpg
用途：图示 Hermes 的核心创新 closed learning loop（闭合学习循环）的整体结构，说明 Agent 如何在处理任务过程中自我积累能力
内容：展示一个闭环结构，通常包含"任务执行 → 学习总结（autonomous skill creation / skill self-improvement during use）→ 写入 SKILL/MEMORY 库 → 通过 autonomous curator 整合归档 → 应用到下次任务"的循环过程，强调新能力反哺后续任务，形成自我增长的能力飞轮
-->

#### (1) 核心类比：传统 CI/CD 流水线

先把类比讲透。**传统软件团队的 CI/CD 流水线**是这样的：

代码 push → CI 自动跑测试 → 失败信息反馈 → 工程师改代码 → 再 push → CI 再跑 → 质量提升 → 进入下一轮。

**这是一个闭环**。每一轮，代码库都在变好。失败不是损失，是反馈；反馈不是噪声，是改进信号。跑得越久，仓库越稳。

#### (2) Hermes 的版本：closed learning loop

Hermes 的 closed learning loop（闭合学习循环）结构完全同构：

Agent 接任务 → 执行过程中积累经验 → 经验写成 skill（autonomous skill creation）+ 记忆（agent-curated memory）→ 下次同类任务直接用 skill → 执行更顺、出错更少 → 产生新的、更深层的经验 → 再沉淀成新 skill → 再优化……

**整个循环不需要人介入，Agent 自己闭环**。你只管用，它在背后自己长大。

把 4.4 和 4.5 串起来：

- **记忆系统让 Agent「记得」**——记得你的偏好、记得项目背景、记得几个月前的决策。
- **技能系统让 Agent「会做」**——会做上次做过的复杂任务，会避开踩过的坑。
- **执行反馈让 Agent「做得更好」**——每一次执行都是下一次的输入，skill self-improvement during use 把使用过程变成改进过程。

三者首尾相接，构成闭环。autonomous curator 在循环外做治理，保证 skill 库不腐烂；FTS5 cross-session search 让记忆可检索，保证循环不丢信号。

#### (3) 关键差异：Hermes vs 普通 LLM-wrapper

这是全文核心论点，必须点透。

<span style="color: red; font-weight: bold;">普通 LLM-wrapper 的能力是「LLM 给的」</span>。你封装一层 prompt、套一层工具，模型多强它就多强。**天花板就是模型本身**——你把 GPT-4 换成 GPT-5，它变强；不换，它永远停在原地。再怎么用、用多久，都不会长出新能力。

<span style="color: red; font-weight: bold;">Hermes 的能力是「LLM + 自己积累的 skill 库」</span>。模型本身只是地基，skill 库是盖在上面的楼。**地基不变，楼可以一直往上盖**——用得越久，skill 越多、越精、越贴合你的工作流。**天花板随使用时间上升**。

这才是 closed learning loop 的本质：<span style="color: red; font-weight: bold;">它把模型能力的静态边界，变成了随时间增长的能力飞轮</span>。

#### (4) 问答推进：Hermes 的「学习」到底学的是什么？

你可能会问：**Hermes 的「学习」到底学的是什么？模型权重又没动。**

答案是——<span style="color: red; font-weight: bold;">它学的是 skill，不是参数</span>。

模型权重（parameters）是 LLM 厂商训练时定下来的，Hermes 不动它，也动不了。但模型之上还有一层「使用经验」，这一层 Hermes 能写、能改、能检索。**学的是「在什么场景下、按什么步骤、避开什么坑」这种经验性知识**，载体是 SKILL.md 和 MEMORY.md。

类比 CI/CD：<span style="color: red; font-weight: bold;">工程师改的是代码，不是编译器</span>。编译器（模型）不动，但代码（skill）每跑一轮都在变好。CI/CD 让代码质量随时间上升，closed learning loop 让 Agent 能力随时间上升——**机制完全同构**。

#### (5) 这为什么是分水岭

我的看法是，<span style="color: red; font-weight: bold;">closed learning loop 是 Hermes 跟所有 LLM-wrapper 的分水岭</span>。前者越用越强，后者再怎么用都还是模型本身的能力。

把时间维度拉长看：一个用了半年的 Hermes，跟一个刚部署的 Hermes，**不是同一个 Agent**。前者带着你半年的工作流记忆、几百个针对性 skill、踩过并被 patch 过的坑库。后者是空白。

这是普通 chatbot 永远做不到的。它们每次对话都从零开始，再聊一万次也是 GPT/Claude 本身。<span style="color: red; font-weight: bold;">Hermes 的护城河不是模型，是时间</span>——你用得越久，它跟你的耦合越深，迁移成本越高，价值也越大。

这也是为什么 Nous Research 把 slogan 写成 "grows with you"（跟你一起长）。**不是营销话术，是 closed learning loop 的直接描述**。

### 4.7 subagent 委托：Agent 版的 worker pool

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/2539b66edb7c4ba56f47b1cc50096f28_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

闭环之外还有一个能力：subagent（子 Agent）委托。

主 Agent 可以 spawn isolated subagents（隔离的子 Agent）处理并行任务。**每个子 Agent 有自己的对话上下文、自己的终端、自己的工具集**——跟主 Agent 完全隔离。

subagent 的核心价值是 **zero-context-cost pipelines**（零上下文成本流水线）。

举例：主 Agent 接到「研究 A、B、C 三个候选方案」。如果自己做，要把三个方案的中间过程都塞进 context，主 Agent 的 context window 会被中间步骤挤爆，主线任务反而看不清。

派给三个 subagent 并行做，**主 Agent 只接收最终结果**——三个总结报告。中间的探索、试错、检索全部留在子 Agent 的 context 里，主 Agent 一行都不背。

类比后端的 worker pool / 线程池 / 微服务调用：**主线程派发任务给独立执行单元并行跑，自己只收结果，不被中间状态污染**。subagent 是 Agent 版的 worker——机制同构，只是执行的不是函数，是另一个 Agent。

subagent 还支持通过 Python RPC 脚本调工具，这让「多步骤任务串成单个 turn」成为可能：主 Agent 一次发指令，subagent 在内部跑完整个 pipeline，最后把成品交回来。**对主 Agent 来说，复杂任务变成了一次函数调用**。

### 4.8 安全机制：生产级 Agent 该有的样子

讲完所有「智能」机制，最后看安全。**这一节不长，但决定 Hermes 能不能上生产**。

5 项核心安全设计：

| 机制 | 说明 |
|---|---|
| 多种 execution backend | 默认本地，敏感操作建议跑在 Docker / E2B / SSH 隔离环境 |
| Dangerous command approval | Slack + Telegram 上危险命令需原生平台按钮确认（非输入 `/approve`） |
| MCP OAuth 2.1 PKCE | 完整 OAuth 标准支持 MCP server 认证 |
| OSV malware scanning | MCP extension 包自动通过 OSV 漏洞数据库扫描 |
| Webhook HMAC 签名验证 | 明确警告 webhook payload 可能含恶意指令 |

逐条拆：

- **多种 execution backend**：tool call 不一定要跑在你本机。敏感操作扔到 Docker 容器或 E2B 沙箱里，跑挂了也不伤主机。
- **Dangerous command approval**：危险命令要确认，但确认方式不是让你输入 `/approve`（容易被 prompt injection 伪造），**而是在 Slack / Telegram 原生界面上点按钮**——这把确认动作的来源固定到了平台 UI，攻击者很难绕。
- **MCP OAuth 2.1 PKCE**：Hermes 接 MCP server 时走完整 OAuth 2.1 PKCE 流程，**认证标准对齐主流身份体系**，不是自己发明一套。
- **OSV malware scanning**：装 MCP extension 时自动过 OSV（开源漏洞数据库）扫描，**拦截已知恶意包**。
- **Webhook HMAC 签名验证**：webhook 是 Agent 的外部入口，payload 可能被构造来注入恶意指令。Hermes 用 HMAC 签名验证来源，**明确警告 payload 内容不可信**。

这些设计反映一个事实：<span style="color: red; font-weight: bold;">Hermes 是真实生产用的 Agent，不是 demo 项目</span>。每一项都对应一个真实攻击面——容器逃逸、prompt injection、供应链投毒、webhook 伪造。**demo 不需要管这些，生产必须管**。

## 5. 能用它做什么：场景即原理的证据

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/f45be22f122ea5ae7935ef648f5a360c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

原理讲完了，但这些机制在真实场景里到底能落地成什么？下面五个方向，每个都是第 4 章某个原理的证据——场景不是清单，而是「原理能落地成什么」的证明。

### 5.1 个人助手类

四个场景的共同点是依赖「记忆」——这正是 4.4 agent-curated memory 的落地。

| 场景 | 用的原理 | 典型用法 |
|---|---|---|
| 消息聚合助手 | multi-platform messaging gateway + cron scheduler | 在 Telegram、Slack、Email 上接收各种通知，每天 9 点自动分类、总结、提醒，生成「昨日通知摘要」 |
| 日程助手 | agent-curated memory + cron scheduler | 自然语言定义日程，自动设置提醒、推送日历、定期 review 是否完成 |
| 学习助手 | agent-curated memory | 记住你的理解程度，下次主动检查你是否掌握，像一个持续跟踪学习进度的私教 |
| 写作助手 | agent-curated memory | 记得你的写作风格、已发内容、读者偏好，是个性化的写作伙伴 |

每个场景都把「Agent 自己管理记忆」这件事变成了具体能力。通用 LLM 给不出「昨天讨论到哪」的回答，因为它的记忆每次清零；Hermes 给得出，因为记忆是它自己 curated 的。

### 5.2 自动化工作流（PR review 作为重点案例）

GitHub PR Review Agent 是 Hermes 官方文档里展开讲的典型场景。这里只讲它为什么典型，完整实操在第 6.4 章展开。

为什么把 PR review 单拎出来？因为它一个场景就把 Hermes 的记忆、技能、cron、外部工具全用上了：

- **接入层**：webhook 监听 GitHub 仓库，新 PR 一来就触发
- **技能系统**：`code-review` 的 SKILL.md 告诉 Agent 怎么看 diff、查 bug、查安全、查 code quality、查测试覆盖
- **cron scheduler**：定时检查 PR 状态
- **外部工具**：通过 `gh` CLI 拉 diff、回写评论

几乎串联了第 4 章所有核心机制，所以它是 Hermes 的「能力集大成」场景。下一章我们用它做主线 demo。

其他自动化场景用表格呈现：

| 场景 | 用的原理 |
|---|---|
| 定时 dashboard | cron scheduler + subagent（覆盖多仓库，每周一生成团队工作总结：PR 状态、open issue 趋势、stale PR 提醒，推到 Slack）|
| 自动化测试 agent | cron scheduler + skill（测试套件，定期跑端到端测试，发现回归自动通知。Nous Research 内部在用）|
| 新闻聚合 | cron scheduler + 多源拉取 + 摘要（每天 9 点跑一遍关注的新闻源，过滤出与工作相关的内容，摘要推到 Telegram）|

### 5.3 跨平台协作

跨平台是 Hermes 的天然属性。第 2.2 章只点了一句，第 3 章只做了痛点映射，这里是真正的展开处。三个场景：

| 场景 | 用的原理 |
|---|---|
| 跨平台对话延续 | multi-platform messaging gateway（session 在 gateway 层共享，不管从哪个 client 进来）|
| 语音输入跨平台 | gateway + voice memo transcription（Telegram / Discord 发语音自动转文字）|
| 家庭群组助手 | gateway + agent-curated memory（记住家庭成员偏好，安排家庭日程、记账、查物流、答疑）|

「跨平台对话延续」用传统软件工程类比最好理解：类似微服务架构里 session 提到 API Gateway 层共享，客户端是无状态的——你从 Telegram 进来、换到 CLI、再换到 WhatsApp，Agent 端的会话上下文一直在。区别只在于，传统 Gateway 存的是用户登录态，Hermes 存的是完整对话上下文。

### 5.4 开发者工作流

四个场景用表格：

| 场景 | 用的原理 |
|---|---|
| 代码 review 助手 | skill（`requesting-code-review`）+ pre-commit 检查（commit 前自动 review diff，发现问题让你修）|
| 调试助手 | agent-curated memory（知道你的项目背景，来自 MEMORY.md + USER.md）|
| 多 Agent 编排 | subagent（主 Agent spawn 三个 subagent 并行研究候选方案，最后整合结果）|
| IDE 集成 | ACP 协议（VS Code / Zed / JetBrains，在 IDE 里也能用上你的 Hermes 个人 Agent）|

调试助手这条值得单独点一下：粘一段错误日志给 Hermes，它结合你之前的项目知识给具体调试建议，比通用 LLM 强在它知道你的项目背景——这是 4.4 记忆系统的价值证据。同样一段日志，通用 LLM 只能给通用建议；Hermes 能说「你上周改过这块的连接池配置，先查那边」，因为它记得。

### 5.5 Nous Research 自己怎么用（含「Agent 数据反哺模型训练」闭环）

最有说服力的不是用户怎么用，而是 Nous Research 自己怎么用 Hermes。从 GitHub 仓库的 release notes 和 skills 库能看出来：

| 用法 | 说明 |
|---|---|
| PR review 自动化 | 仓库 30+ skills，大部分都是开发流程相关 |
| 测试 agent | Rust message middleware project 的自动化测试，这是 Hermes 的典型用法 |
| 跨平台沟通 | 工程师在 Telegram / Discord / CLI 之间切换跟 Hermes 协作 |
| 持续训练数据 | Hermes 导出训练轨迹，用于 SFT（监督微调）数据生成 + RL 微调（强化学习微调），训练 Hermes 系列模型 |

前三条都不新鲜，无非是「自己做自己的 dogfooder」。最后一条才是本章高潮。

#### (1) Agent 数据反哺模型训练的闭环

Hermes Agent 不只是被动地用 LLM，它产生的数据反过来训练 LLM。这个闭环让 Hermes Agent 跟 Nous Research 的模型形成相互增强：

```
Agent 执行任务 → 产生训练轨迹 → SFT / RL 微调 → 新版 Hermes 模型 → 更强的 Agent
```

#### (2) 呼应 2.4 项目背景：车厂的数据闭环

第 2.4 章讲过 Nous Research 的双重身份——它既做模型（Hermes 系列大模型），又做 Agent（Hermes Agent）。用当时的车厂类比：它既造发动机（模型），又造整车（Agent），还自己开车跑赛道收集数据（Agent 轨迹），数据反过来改进发动机。

<span style="color: red; font-weight: bold;">这不是「两个业务」，是「一个闭环」</span>。市面上绝大多数 Agent 厂商只造整车（用别家的模型），市面上绝大多数模型厂商只造发动机（不知道下游怎么用）。Nous Research 两头都做，数据能在自己手里转起来。

#### (3) 呼应 4.6 closed learning loop：两个闭环嵌套

第 4.6 章讲的是 Agent 内部的闭环：执行 → skill → 再执行。那是 Agent 在「自己用」层面的学习。

5.5 这里讲的是更大的外部闭环：Agent → 训练数据 → 新模型 → 更强 Agent。这是模型层面的学习。

两个闭环嵌套在一起：

- 内层（4.6）：单次任务里 Agent 自己积累 skill
- 外层（5.5）：跨任务、跨用户，Agent 数据反哺模型权重

内层不改权重，只改 skill 库；外层改权重，让基座模型本身变强。

#### (4) 作者判断

我的判断是，这种「Agent 反哺模型」的闭环是 Nous Research 做 Hermes 的真正野心——<span style="color: red; font-weight: bold;">它不只是工具，是数据飞轮</span>。一个只做 Agent 的公司，天花板是上游模型；一个只做模型的公司，天花板是不懂下游场景。两边都做、数据能自循环的公司，天花板是飞轮转速本身。

## 6. 上手实操：从零跑通一个 PR review Agent

原理看完了，能不能上手？能。下面 8 节带你从装到跑，最后落地一个真实可用的自动化 PR review Agent。

### 6.1 安装

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/e1215c88bd33318b414ae522d5f45d7d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">


Hermes 支持五种安装方式，覆盖主流平台：

| 平台 | 命令 | 备注 |
|---|---|---|
| Linux / macOS | `curl -fsSL https://raw.githubusercontent.com/NousResearch/hermes-agent/main/scripts/install.sh \| bash` | 最简单 |
| Windows | WSL2 用 Linux 命令最稳；也支持 PowerShell 一键脚本 | 原生 Windows 是 early beta，有 rough edges |
| Android（Termux） | 同上的 curl 一行命令 | |
| PyPI（v0.13 后） | `pip install hermes-agent && hermes` | |
| 源码 | `git clone` 后跑 `setup-hermes.sh`（自动装 uv、创建 venv、装 dependency） | |

安装过程会自动装一组依赖：uv、Python 3.11、Node.js、ripgrep、ffmpeg。Hermes Home 默认在 `~/.hermes/`，所有配置和数据都存这里。

### 6.2 首次配置（hermes setup）

装完跑 `hermes setup`，setup wizard 会引导你完成四项配置：

- **选 LLM Provider**：Nous Portal、OpenRouter、OpenAI、Anthropic、Gemini 任选。Provider 之间随时切换，配置写在 `config.yaml` 里
- **配 fallback providers**：主 Provider 失败时自动切到备用
- **选 platforms**：决定接入哪些消息平台
- **配 OpenClaw 迁移**：如果你之前用 OpenClaw，setup 会自动检测 `~/.openclaw`，迁移你的记忆、技能、API key

setup 完跑 `hermes` 进入交互模式，开始跟你的 Agent 第一次对话。

### 6.3 三种运行模式（CLI / Gateway / API Server）

Hermes 有三种主要运行方式：

| 模式 | 启动命令 | 适用场景 |
|---|---|---|
| CLI / TUI | `hermes`（交互对话）或 `hermes --tui`（Ink-based TUI） | 开发者日常使用 |
| Messaging Gateway | `hermes gateway`（挂在 VPS 24h） | 个人持久化 agent 场景，Telegram + Discord + Slack 等都通过此 gateway |
| API Server | `hermes serve`（启动 OpenAI 兼容 `/v1/chat/completions`） | 集成到已有业务系统 |

三种模式可以同时跑，共享同一份记忆、技能、配置，互不冲突。

类比传统后端：这三种模式就是同一服务的不同接入方式。CLI 像本地 REPL，开发者随手调；Gateway 像消息队列的常驻消费者进程，挂着等事件来；API Server 像 REST 服务，对外暴露标准接口给别的系统调。底层是同一个 Agent，只是入口不同。

### 6.4 主线 demo：自动化 PR review

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/7b8d34d7a0654aeee1e744de91621583_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一节是本章核心。按 Hermes 官方入门示例，做一遍 GitHub PR review agent，把 Hermes 的记忆、技能、cron、外部工具调用四项能力一次跑通。

整条 demo 类比传统 CI 流水线：webhook 触发像 git push hook，skill 像 CI 配置文件，cron 像定时任务，外部工具调用像 CI 调测试脚本。区别在于：CI 跑的是固定脚本，Hermes 跑的是会学习进化的 Agent。

#### (1) 装 gh CLI 并做 authentication

Hermes 通过 gh CLI 操作 GitHub，先在本机装好 gh CLI 并完成 authentication。

#### (2) 在 Hermes 里教它 review 规范

启动 `hermes`，把团队规范告诉它：

```
> hermes

> Remember: In our backend repo, we use Python with FastAPI. 

  All endpoints must have type annotations and Pydantic models. 

  We don't allow raw SQL — only SQLAlchemy ORM. 

  Test files go in tests/ and must use pytest fixtures.
```

「Remember」开头这句让 Hermes 把规范永久记到 MEMORY，之后每次 review 都会带上。

#### (3) 手动 review 一个 PR 做测试

先拿一个真实 PR 验证它能不能干活：

```
> Review this pull request. Read the diff, check for bugs, 

  security issues, and code quality. Be specific about line numbers.

  Run: gh pr diff 3888 --repo NousResearch/hermes-agent
```

Hermes 会跑 `gh pr diff 3888 --repo NousResearch/hermes-agent`，分析 diff 后给出 review。满意就进下一步。

#### (4) 定义 skill

把这套 review 流程沉淀成 skill，路径写在 `~/.hermes/skills/code-review/SKILL.md`。这样以后每次 review 都自动套用同一份规范，不用再口头交代。

#### (5) 定义 cron job

让它定时自己跑：

```
> hermes cron create "0 */2 * * *" \

    "Check for new open PRs and review them. 

     Repos to monitor: myorg/backend-api, myorg/frontend-app"
```

每 2 小时 Hermes 自动跑一次，检查指定仓库的新 PR 并 review。

#### (6) webhook 模式（可选）

如果你有 public URL，可以切到 webhook 模式，让 GitHub 实时推送 PR 事件给 Hermes，比 cron 更及时。

这 6 步把 Hermes 的四项核心能力全部用上——MEMORY 记录你的 review 规范，skill 沉淀 review 流程，cron 让它自动跑，gh CLI 让它操作真实 GitHub。换个工作流，同样的模式可以套到任何自动化场景。

### 6.5 接入消息平台（Telegram 例子）

让 Hermes 出现在你的消息平台很简单。以 Telegram 为例：

1. 在 Telegram 找 `@BotFather` 创建一个 bot，拿到 token
2. 在 `~/.hermes/config.yaml` 里加 telegram adapter 配置，填上 token
3. 跑 `hermes gateway` 启动 gateway 进程
4. 在 Telegram 里跟你的 bot 对话，Hermes 出现

Discord、Slack、WhatsApp 等平台流程类似：每个平台拿到对应的 token 或 webhook，配到 `config.yaml` 里。接入新平台不用改 Hermes 代码。

### 6.6 给 Hermes 教新技能的两种方式

用得越久，你会越想给它教更多技能。两条路：

#### (1) 让 Hermes 自动创建

复杂任务完成后 Hermes 会自动写 skill。你可以在 `~/.hermes/skills/` 目录里看到它写的 SKILL.md，需要的话直接编辑。

#### (2) 自己写 skill

按 Hermes 的 SKILL.md 标准格式写一份 markdown 放进 skills 目录。Hermes 启动时自动加载。标准格式有 7 段：

- `# Skill`
- `## When to Use`
- `## Prerequisites`
- `## How to Run`
- `## Quick Reference`
- `## Procedure`
- `## Pitfalls`
- `## Verification`

注意：skill 不是代码，是给 LLM 看的指南。关键是清楚描述「什么时候用这个 skill」和「分步骤怎么做」，让 LLM 在合适的时机自然用上。

### 6.7 进阶：subagent 与 MCP

熟悉 Hermes 后，两个进阶能力值得花时间。

#### (1) Subagent

在对话里用 `delegate_task` 让主 Agent 把任务委托给子 Agent。子 Agent 跑在独立的对话上下文里，做完返回结果，不污染主 Agent 的 context。处理复杂并行任务的利器——类比后端的 worker pool，主线程只收最终结果。

#### (2) MCP 集成

Hermes 完整支持 MCP（Model Context Protocol）服务器，可以接入社区已有的 MCP server 扩展工具能力，比如 github-mcp、slack-mcp、filesystem-mcp。优势是生态兼容：别人写的 MCP server Hermes 直接能用，反过来 Hermes 也能作为 MCP host 接其他工具。

### 6.8 让 Hermes 越用越能干的 5 条建议

Hermes 的特点是「越用越能干」，但前提是你真的持续用。六条让它在日常发挥价值的建议：

1. **把 Hermes 装在 VPS 上**：让它 24 小时在线。5 美元的 DigitalOcean VPS 完全够用
2. **接到你最常用的消息平台**：Telegram 多就接 Telegram，Slack 多就接 Slack
3. **跟它聊重要决策**：项目背景、技术选型、个人偏好都告诉它，让 MEMORY 长起来
4. **复杂任务不要怕用**：复杂任务完成后 Hermes 自动写 skill，用得越多 skill 库越丰富
5. **定期 review skill 库**：跑 `hermes curator` 让 Hermes 自己整理 skill，避免越积越乱
6. **定 cron 自动化重复工作**：每天 / 每周重复的事都让 cron 接管

我的判断是：Hermes 的价值是复利型的，用得越久越能干，三个月跟一年的 Hermes 完全不一样。早一天开始你就早一天受益。

## 7. 选型判断与总结

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/e5dc8d9dc88fc5256fe0a1ecaf307388_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 7.1 核心定位回顾（一句话）

一句话回顾：Hermes Agent 是一个属于你个人的、跟你长期一起成长的开源 AI Agent——你装在 VPS 上，它 24 小时在线，记得你的偏好，自己学习成长。

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/8f1e02e015dc88c60c3dc5a01ae408e2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicent-v2-13-hermes-agent-deep-dive/8f1e02e015dc88c60c3dc5a01ae408e2_MD5.jpg
用途：总结章节配图，直观呈现 Hermes Agent 的核心定位与关键特征
内容：呼应正文定义，展示 Hermes Agent 作为“跑在 VPS 上、24 小时在线、跨 20+ 平台、记得用户偏好、能自主学习成长”的个人开源 AI agent 的核心定位，并与其对照（非 coding copilot、非 chatbot、非 IDE 插件）
-->

它的核心创新是 closed learning loop（闭合学习循环）——agent-curated memory、autonomous skill creation、skill self-improvement、autonomous curator 这套机制第 4.6 节已展开，这里不再重复。

那它到底适合谁？下一节给出客观判断。

### 7.2 适合谁 / 不适合谁

**表 1：适合谁**

| 适合人群 | 为什么适合 |
|---|---|
| 个人开发者 | 想要跨平台、持久化、能学习的 AI 助手 |
| 小团队 | 用 Hermes 做 PR review、code monitoring、知识管理等自动化 |
| AI 探索者 | 想深入理解「Agent 怎么持续学习」，Hermes 是最好的开源参考 |
| 多平台用户 | 用 Telegram、Discord、Slack、WhatsApp 等多个平台，想要统一的 agent 接入 |

**表 2：不太适合谁**

| 不太适合人群 | 为什么不适合 | 更推荐的替代 |
|---|---|---|
| 只想快速回答问题 | 不需要 Hermes 的复杂度 | ChatGPT 就够 |
| 只想要 IDE 内代码助手 | Hermes 不针对代码写作场景 | Cursor、Claude Code 更直接 |
| 企业级 Agent OS 需求 | Hermes 定位个人，企业部署非设计目标 | 企业级 Agent OS 平台 |

### 7.3 我的判断（含局限：Python 生态 / 个人定位 / 学习曲线）

我的判断是：Hermes Agent 在「个人持久化 Agent」这条路上做得最深。它对 Agent 的几个核心问题——记忆、学习、跨平台、持久化——都给出了完整的工程方案。理解 Hermes 能让你看清楚 Agent 这件事的可能性边界。

听到这里可能想立刻上生产。先别急，Hermes 有三个绕不开的局限。

#### (1) Python 生态

Python 生态意味着部署门槛比 Java、Go、Rust 高，依赖管理也更复杂。

这件事可以类比传统 Web 开发：选 Python 做 Agent 就像选 Ruby 做 Web——开发时很爽，但部署比 Java 重，依赖、虚拟环境、版本锁一个都不能少。团队要接受这个 trade-off，运维成本要提前算清楚。

#### (2) 设计哲学偏个人

Hermes 的设计哲学偏向个人。企业场景需要的多租户、权限、审计这些能力不是它的强项。

如果你的需求是「给整个组织跑一个统一 Agent 平台，要细粒度权限和完整审计日志」，Hermes 当前不是为这个场景设计的。强行扩展会违背它的设计初衷。

#### (3) 学习曲线

功能太多，新手容易眼花缭乱。

记忆、技能、闭环、subagent、cron、多平台 gateway——每一个机制单独看都不复杂，组合起来需要时间消化。第一次接触的人很容易被功能清单吓到，不知道从哪里开始。

但我的立场也很清楚：没有一个开源项目能做到完美，重要的是它解决的问题足够真实，方向足够清晰。Hermes 做到了这两点。
