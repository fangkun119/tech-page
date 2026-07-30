---
title: 传统项目迁AI 24：自动测试 - 拆解学习Hermes
author: fangkun119
date: 2026-07-05 04:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-24-autotest-01-hermes-decomp-learning/cover.jpg
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
aicmigr-24-autotest-01-hermes-decomp-learning
传统项目迁AI 24：自动测试 - 拆解学习Hermes
-->

## 1. 先建立「够用于拆需求」的顶层认知

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/8396f0105a79347f42de2df0c8e31567_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

你接到一个任务：基于某个开源项目实现一个功能。

举一个真实场景 —— <span style="color: red; font-weight: bold;">基于开源的 Hermes Agent</span>，做一个 7×24 小时不间断跑<span style="color: red; font-weight: bold;">混沌测试的 AI 系统，要求自动起集群、自动注入故障、自动调多语言 SDK 跑测试、自动收集日志、自动出报告</span>。

你的第一反应是什么？打开 IDE 开始写代码？

### 1.1 接手陌生项目，先建立认知再动手

传统软件工程里，接手一个老项目之前，你会先读它的架构文档、接口契约、部署拓扑，搞清楚「它在整体里是什么角色」再动手。基于开源项目做需求是同一个动作 —— 区别只在于，开源项目对你来说是陌生的，文档要你自己去建立。

### 1.2 「够用于拆需求」的工程定义

#### (1) 不是什么

接到任务，第一件事不是动手，是先把工具摸透。但「摸透」有明确的工程定义 

- ❌ 不是把项目源码读到能提 PR
- ❌ 不是把官方文档逐字读完
- ✅ 而是建立一份对项目「<span style="color: red; font-weight: bold;">边界</span>、<span style="color: red; font-weight: bold;">能力</span>、<span style="color: red; font-weight: bold;">扩展形状</span>、<span style="color: red; font-weight: bold;">运行时</span>」的最小完备认知

#### (2) 为什么是这个定义

下一步是拆需求。<span style="color: red; font-weight: bold;">拆需求只需要你知道：哪些能力项目已经有了，哪些能力需要自己写。这个判断不需要你读懂每一行源码。</span>

#### (3) 类比锚定

把它类比成传统项目里的「接手老项目」—— 你不会一上来就去读所有业务代码，你会先问四件事：

- <span style="color: red; font-weight: bold;">这个系统是干嘛的？</span>
- <span style="color: red; font-weight: bold;">它暴露了哪些接口？</span>
- <span style="color: red; font-weight: bold;">它依赖哪些外部服务？</span>
- <span style="color: red; font-weight: bold;">它在什么环境下跑？</span>

<span style="color: red; font-weight: bold; background-color: yellow;">搞清楚这四点，你就能开始评估「新需求要怎么落到这个系统上」</span>。读完后续章节，你应该能回答两个问题：

- 怎样在动手之前，把一个陌生开源项目摸到「够用」的程度？
- 这套打法什么时候能用、什么时候不能用？

## 2. 四维度认知框架

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/9fc766ad5b491ae5806076920636f058_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">这份最小完备认知由四层组成，少了任何一层，要么用不起来，要么用错。</span>

### 2.1 四层结构

| 层      | 含义                             | 决定了什么       |
| ------ | ------------------------------ | ----------- |
| <span style="color: red; font-weight: bold;">它是什么</span>   | 定位与边界，解决什么、不解决什么、与同类工具的关系      | <span style="color: red; font-weight: bold;">要不要继续往下看</span>    |
| <span style="color: red; font-weight: bold;">它能做什么</span>  | 原生能力清单，不写一行代码能白嫖多少             | <span style="color: red; font-weight: bold;">能省多少工作量</span>     |
| <span style="color: red; font-weight: bold;">它怎么扩展</span>  | 扩展机制（插件 / Skill / Hook 等）与开发范式 | <span style="color: red; font-weight: bold;">自己要写的部分长什么样</span> |
| <span style="color: red; font-weight: bold;">它内部怎么跑</span> | 进程模型、状态管理、对外接入面                | <span style="color: red; font-weight: bold;">能否预判生产环境表现</span>  |

### 2.2 框架图示

对应的四维度拆解框架图示如下：

<!-- 
图片内容说明
路径：imgs/aicmigr-24-autotest-01-hermes-decomp-learning/de98adb35d95b8a342666b4d2a836375_MD5.jpg
用途：展示对陌生开源项目建立顶层认知的四个维度拆解框架
内容：四个维度——它是什么（定位/边界）、它能做什么（原生能力清单）、它怎么扩展（扩展点形状）、它内部怎么跑（关键运行时）
-->

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/de98adb35d95b8a342666b4d2a836375_MD5.jpg" style="display: block; width: 800px;" alt="四维度认知框架">

### 2.3 关键洞察：不解决什么比能做什么更值钱

这四层里，「它能做什么」和「它不解决什么」是一对。<span style="color: red; font-weight: bold;">「不解决什么」往往比「能做什么」更值钱</span> —— 它直接告诉你接下来要写的核心代码落在哪。这一点在后面的 Hermes 案例里会看得非常清楚。

## 3. 用 AI 建立认知的方法

知道了要建立什么认知，下一个问题是：怎么建立得又快又准？

答案是让 AI 帮你。但 AI 不会主动给你正确的认知，前提条件不到位，你只会拿到一坨官方话术。

### 3.1 让 AI 帮你建立认知的两个前提

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/80586a6f54a1993fadf390b1066d5c9d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 前提一：自己要有拆解框架

工程师对<span style="color: red; font-weight: bold;">陌生开源项目</span>要建立的<span style="color: red; font-weight: bold;">顶层认知</span>，不是 AI 自己能猜出来的，而是过去踩坑沉淀出来的判断维度。<span style="color: red; font-weight: bold;">这个框架是工程师的能力，不是 AI 的能力</span>。

AI 的能力是：你给好框架之后，它能把框架填得又快又准。

反例很好理解。你问 Claude：「告诉我 Hermes 是什么」，拿到一坨「它是 AI Agent 平台、支持多种扩展、有完整工具链」。<span style="color: red; font-weight: bold;">读完依然不知道能拿它干什么。问题不在 AI，在于你没给 AI 一个拆解框架。</span>

这就像传统项目里，一个新手去问老员工「这个系统是干嘛的」，得到的多半是一段泛泛的项目简介；但如果你问「**这个系统的核心接口契约是什么、它依赖的中间件有哪些、它不负责哪些业务**」，老员工立刻知道你懂行，回答也会精准得多。

#### (2) 前提二：带着任务去问

同样是问「Hermes 怎么扩展」，不带任务问和带着任务问，得到的答案完全不一样：

| 问法    | AI 给出的内容                          | 工程师是否能直接用        |
| ----- | --------------------------------- | ---------------- |
| 不带任务问 | 扩展机制大全：所有扩展点、所有 API、所有玩法          | 不能，看完仍不知道哪个对自己有用 |
| 带任务问  | 过滤掉与任务无关部分，告诉「对你这个场景该用哪个扩展点，因为……」 | 能，这是工程师真正需要的认知   |

带任务问的标准动作：每个<span style="color: red; font-weight: bold;">提示词里都嵌入一个具体的业务任务</span>，比如「<span style="color: red; font-weight: bold;">我要做一个自动化测试 Agent</span>」，让 AI 拿着这个任务去过滤资料、筛选回答、给出建议。

### 3.2 三个提示词覆盖四个维度

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/af493c2e6d98177ced36510f1491f762_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">四个认知维度，用三个提示词就能全部拿到</span>：

| 提示词 | 覆盖维度 | 输出要回答的问题 |
|-------|--------|-----------|
| 提示词一 | <span style="color: red; font-weight: bold;">是什么 + 内部机制</span> | 是否适合做这个任务、哪些核心抽象会被直接用、运行时模型对部署方案的影响 |
| 提示词二 | <span style="color: red; font-weight: bold;">能做什么</span> | 任务需要的每项能力分别属于「直接支持 / 半支持 / 不支持」，自己要写的工程量 |
| 提示词三 | <span style="color: red; font-weight: bold;">怎么扩展</span> | 扩展点的形状（目录结构、Tool 定义、状态共享、最小骨架），评估开发量 |

<span style="color: red; font-weight: bold;">为什么「是什么」和「内部机制」合并？因为在多数项目里，这两块连着讲最自然——一个项目的定位和它的运行时模型是绑在一起的。</span>

这三个提示词不是抽象的方法论，后面第 4 章会给出完整的原文，你可以直接照着改业务任务去用。

### 3.3 提示词措辞的两条经验

提示词怎么写，直接决定输出质量。两条经验值得单独讲。

#### (1) 经验一：列清单让 AI 对照，而不是让 AI 罗列

提示词二的关键设计是：把任务需要的能力一条一条列出来，让 AI 拿着这份清单去对照目标项目的能力。<span style="color: red; font-weight: bold;">不带清单，得到的是「所有能力罗列」；带清单，得到的是「对自己有用的能力筛选」。</span>

这条经验可以类比传统软件工程里的接口契约评审 —— 你不会让评审人泛泛地看「这个接口设计得怎么样」，你会给他一份 check list：幂等性、异常处理、向后兼容、性能边界。<span style="color: red; font-weight: bold;">AI 也一样，给它锚点，它的回答才有方向。</span>

#### (2) 经验二：给 AI 留反问的余地

带任务问还不够，带任务问 + <span style="color: red; font-weight: bold;">给 AI 留反问的余地</span>（因为你对自己的观点并不确定，你不希望AI顺着你说、而是给它纠正你的机会），才是更高级的姿势。

措辞示例：

```text
继续。如果我决定用 Hermes 做这个系统，我自己要写的核心是 Skill 吗？
如果是，请告诉我……
```

注意第一句：「我自己要写的核心是 Skill 吗？」—— 特地写成疑问句而不是肯定句。

AI 拿到一个肯定句会顺着你说，<span style="color: red; font-weight: bold;">拿到一个疑问句才有空间纠正你的预设</span>。这条经验在第 4.4 节会看到一个极具说服力的例子：正是这个疑问句，让 AI 纠正了「核心是 Skill」的错误预设，省下了一大段走回头路的工程量。

## 4. 实战：Hermes 拆解全过程

讲完方法论，这一章用一个真实案例完整走一遍。案例的任务是：基于 Hermes Agent 实现 7×24 不间断跑混沌测试的 AI 系统。

### 4.1 场景与任务起点

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/0b75346fe3169e291df418d888896b9a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Hermes Agent 是一个开源的自托管 AI Agent 控制平面。

这类「<span style="color: red; font-weight: bold;">基于开源项目做需求</span>」的场景在工程师日常里非常常见：

- **运维要基于 Prometheus 做内部巡检平台**
- **后端要基于 Temporal 做工作流引擎**
- **算法要基于 LlamaIndex 做 RAG 应用**

都是同一个动作。

本案例的目标系统主要包括这些能力：

**✅ 自动起集群**
**✅ 自动注入故障**
**✅ 自动调多语言 SDK 跑测试**
**✅ 自动收集日志**
**✅ 自动出报告**

参考资料（项目方关于「基于 AI 的自动化测试」的两篇博客）：

* [https://robustmq.com/zh/Blogs/95](https://robustmq.com/zh/Blogs/95)
* [https://robustmq.com/zh/Blogs/84](https://robustmq.com/zh/Blogs/84)

任务起点的一个关键背景：Hermes 还很新。作者之前听过但没真正用过，公开渠道也搜不到太多企业落地案例。这恰好是这套打法的价值所在——<span style="color: red; font-weight: bold;">在没有现成先例的时候，基于 AI 快速摸出一条能走通的路</span>。

### 4.2 提示词一：它是什么 + 它的内部机制

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/d1173b6e6495509e6711398f214795a1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 提示词全文

```text
你现在在 Hermes 这个开源项目的目录下。我刚接到一个公司内的任务:

要基于 Hermes 做一个 7×24 跑混沌测试的 AI Agent，功能包括:
自动起集群、注入故障、调多语言 SDK 跑测试、收集日志、生成报告、通过飞书通知团队。

请你读 README、docs/architecture.md 和 src 顶层目录结构，告诉我:

1. Hermes 的核心定位是什么。一句话说清楚。
2. Hermes 的核心抽象有哪些。每个抽象用一句话解释它的作用。
3. Hermes 不解决什么问题。它的设计边界在哪。
4. Hermes 是怎么跑起来的。进程模型、配置加载、状态管理大致是什么样。
5. 结合我接到的任务，Hermes 的定位是不是和我的需求方向一致。有没有明显的不匹配。

不要复述官方话术。用你判断后的结论来回答。
```

读完这个提示词的输出，应该能回答这几个问题：

- Hermes 是不是适合做这个任务
- 它的核心抽象有哪些会被直接用到
- 它的运行时模型对部署方案有什么影响

注意最后一行「不要复述官方话术。用你判断后的结论来回答」—— <span style="color: red; font-weight: bold;">这是在主动压住 AI 的「官方话术倾向」，逼它给出判断而不是复述。</span>

#### (2) Claude Code 跑完输出的关键信息

Claude Code 给出的核心定位是：

- 有学习能力的通用 AI 工作台
- 把 LLM、工具调用、跨平台消息、定时任务封装成可以长期运行的 Agent 基础设施。

围绕这个定位，它拆出了三层关键信息。

##### ① 核心抽象

| 抽象 | 作用 |
|----|----|
| AI Agent | 对话循环 |
| SessionDB | SQLite + FTS5 会话存储 |
| Tools | 自动发现的工具注册表 |
| Skills | Markdown 写的过程记忆 |
| Delegate | 子 Agent 隔离调度 |
| Cron | 定时触发 |
| Gateway | 多平台消息路由 |
| Terminal Backends | 本地 / Docker / SSH / Modal 等执行环境 |

##### ② 运行时

```text
通常两个长期进程：hermes CLI 和 hermes gateway 网关
通过 SQLite WAL 共享状态
子 Agent 是同进程线程
配置走 ~/.hermes/config.yaml，API Key 单独放 .env
```

##### ③ 不解决什么问题（比「能做什么」更值钱）

- 不是测试框架（没有 assertion / fixture / test suite 概念）
- 不是可观测性平台（日志只到 agent.log，没有结构化指标和告警）
- 不是集群编排器（Terminal backend 只是一个 shell）
- 不是报告生成引擎（没有结构化报告模板）
- 没有任何故障注入能力（tools/ 里没有 chaos engineering 相关实现）
- 默认还是 human-in-the-loop 模式（7×24 无人值守需要大量 allowlist 配置）

#### (3) 输出的工程价值

「<span style="color: red; font-weight: bold;">不解决什么问题</span>」这一段，比「能做什么」更值钱。它直接告诉工程师<span style="color: red; font-weight: bold;">接下来要写的核心，正好填在这些「它不做」的位置上</span>：

- 故障注入
- 结构化报告
- 跨会话状态
- 无人值守的 allowlist 配置

<span style="color: red; font-weight: bold;">结论：方向是对的，但 Hermes 是骨架，不是答案。要做的是在它给的扩展点上，把它不做的那些事补齐。</span>

### 4.3 提示词二：它能做什么

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/4e309496915664cb2c6118ca0c4680ec_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 提示词全文

```text
继续。我的任务需要这些能力，请对照 Hermes 现有功能，
告诉我每一项能不能直接用、用哪个特性、还需要我自己实现什么:

1. 定时触发(每 2 小时跑一次基础场景、每天凌晨跑全量场景)
2. 调用工具函数(起集群、注入故障、跑客户端、收日志、出报告)
3. 在多轮调用之间共享状态(集群信息、测试进度)
4. 生成结构化报告(JSON + Markdown)
5. 通过飞书发通知(测试失败、报告生成完毕)
6. 手动触发(开发期间针对性验证某个场景)
7. LLM 调用循环(多轮 tool use、错误重试、token 管理)

输出格式:每一项分别说，Hermes 直接支持哪些、半支持哪些(需要简单配置)、
完全不支持哪些(需要我自己写)。

最后给我一个总结，我自己要写的工程量大概有多大。
```

<span style="color: red; font-weight: bold;">关键设计就是第 3.3 节讲过的那条经验：把任务需要的能力一条一条列出来，让 AI 拿着这份清单去对照 Hermes 的能力。</span>不带清单得到「能力罗列」，带清单得到「对自己有用的能力筛选」。

#### (2) 跑完后的判断

对照图如下：

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/52d558087c9b193feeaeb39d5c681b67_MD5.jpg" style="display: block; width: 800px;" alt="能力对照图">

<!-- 
图片内容说明
路径：imgs/aicmigr-24-autotest-01-hermes-decomp-learning/52d558087c9b193feeaeb39d5c681b67_MD5.jpg
用途：对照任务需要的 7 项能力与 Hermes 原生支持情况，给出工程量判断
内容：7 项能力按"直接支持 / 半支持（简单配置）/ 完全不支持（自己写）"三类划分的对照图
-->

详细对照表：

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/capability-comparison-table.svg" style="display: block; width: 800px;" alt="能力对照表">

<!-- 
图片内容说明
路径：imgs/aicmigr-24-autotest-01-hermes-decomp-learning/capability-comparison-table.svg
用途：对照任务需要的 7 项能力与 Hermes 原生支持情况，给出工程量判断
内容：7 项能力按"直接支持 / 半支持（简单配置）/ 完全不支持（自己写）"三类划分的对照表
-->

结论：自己要写的核心收敛到几个工具函数。<span style="color: red; font-weight: bold;">Hermes 的白嫖额度比预估的高，真正的工程量大概 600-800 行 Python，集中在故障注入和报告生成两件事</span>。

#### (3) 一条关键设计判断

跑完这一轮得到一条非常重要的判断：<span style="color: red; font-weight: bold;">结构化报告</span>这件事，不能交给 LLM 写。Claude Code 直接告诉作者，如果让 LLM 直接生成 <span style="color: red; font-weight: bold;">JSON</span>，字段名会飘、嵌套层级会乱，任何下游程序解析都会踩坑。

这条分工规则成为后续设计 Tool 时的一条硬规则：

| 产物类型 | 由谁生成 | 原因 |
|--------|--------|----|
| 结构化产物（JSON、配置、报告字段） | 代码生成 | 字段名 / 嵌套层级稳定，下游可解析 |
| 叙述性产物（人类可读说明） | LLM 生成 | 擅长自然语言组织 |

这个判断非常重要，接下来将由上面图片的3个Python生成结构化报告给Hermes框架即可 —— 它直接决定了后续拆需求时，**拆到的颗粒度只到工具函数级别就够了，不用从零规划一个 Agent 框架**。

### 4.4 提示词三：它怎么扩展

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/5b54382ad58cca39073b14bfaa1e555b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 提示词全文

```text
继续。如果我决定用 Hermes 做这个系统，我自己要写的核心是 Skill 吗？

如果是，请深入读 docs/skills.md 和 examples/ 下的几个示例 Skill，告诉我:

1. Skill 是什么形状。目录结构、关键文件、一个最小 Skill 包含哪几样。
2. Skill 里的 Tool 怎么定义。函数签名、参数类型、返回值约定。
3. Skill 之间怎么共享状态。Memory 系统的使用范式是什么。
4. SKILL.md 这个文件是给谁看的、写什么内容、有没有写法约定。
5. 给我一个最小可行的 Skill 骨架，假设这个 Skill 只做一件事：返回当前时间。让我能判断扩展一个 Skill 的工程量大概多大。

不要泛泛而谈，我要的是能直接用来评估开发量的具体认知。
```

核心是让 AI 给<span style="color: red; font-weight: bold;">「扩展点的形状」</span>，而不是讲「扩展机制有哪些」。注意第一句措辞「我自己要写的核心是 Skill 吗？如果是，请告诉我……」—— 这就是第 3.3 节那条经验的实战应用：特地把「核心是 Skill」写成疑问句而不是肯定句，给 AI 留出反问的余地。

#### (2) 反认知的纠正：核心是 Tool，不是 Skill

事实证明这个余地留对了。Claude Code 跑完后给出一个反认知的<span style="color: red; font-weight: bold;">纠正</span>：<span style="color: red; font-weight: bold;">以为要写的核心是 Skill，实际上要写的核心是 Tool</span>。

Skill 不能定义新的 Tool，<span style="color: red; font-weight: bold;">Skill 只是 Tool 的「使用说明书」</span>。

三者区别如下：

| 概念      | 位置                      | 语言       | 职责                                    |
| ------- | ----------------------- | -------- | ------------------------------------- |
| Tool    | `tools/*.py`            | Python   | 通过 `registry.register()` 注册，是实际执行逻辑   |
| Skill   | `skills/*/SKILL.md`     | Markdown | 告诉 LLM 什么时候调哪个 Tool、按什么顺序调            |
| Scripts | `skills/*/scripts/*.py` | Python   | 普通辅助脚本，不是 Tool，Agent 通过 Terminal 工具调用 |

#### (3) 工程影响

这个纠正直接改变了对工程量的判断：

- Skill 写一份 Markdown，几小时就够
- <span style="color: red; font-weight: bold;">真正的工作量在 Tool 的 Python 实现</span>

你可能会问：如果一开始就按「写一个大的 Skill」去拆需求，会发生什么？答案是—— 最后会发现 Skill 里装不下任何业务逻辑，绕一圈才回到 Tool 上。**这种走回头路在工程上是可以避免的，前提是问 AI 时留出反问的余地**。

这就是为什么第 3.3 节那条「疑问句而非肯定句」的经验不是玄学——它在这里实打实省下了一段弯路。

### 4.5 跑完三个提示词后的整体认知

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/cad8d08179d15911cdd3f43281691f86_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 四个角度的总览

| 角度         | 一句话概括               | 认知                                                                                                                                                                                                                                            |
| ---------- | ------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| Hermes定位   | Agent 工程脏活打包层       | 自托管的 AI Agent 控制平面。<br> - LLM 调用<br> - 多通道接入<br> - 定时触发<br> - 跨会话记忆<br> - Skill 扩展<br>这些通用能力都解决了，核心价值是把 Agent 工程里所有「重复的脏活」打包好，让你只关心业务逻辑                                                                                                       |
| Hermes能给什么 | 六项能力直接白嫖            | 下面六样直接拿来用，不写一行代码<br>- Cron 触发<br>- Gateway 多通道（飞书原生支持）<br>- Memory 跨会话状态<br>- CLI 手动触发<br>- Delegate 子 Agent 并发<br>- AI Agent 的工具调用循环与重试退避                                                                                                    |
| 要写什么       | Tool + Skill + 配置   | **① 几个 Tool（Python，注册到 `tools/`）**<br>- 起集群<br>- 注入故障<br>- 跑客户端<br>- 收日志<br>- 出报告<br><br>**② 一份 SKILL.md（Markdown，放在 `skills/chaos-testing/`）**<br>- 告诉 LLM 什么时候按什么顺序调这些 Tool；<br><br>**③ 一份 Cron 配置定义两条定时任务**<br><br>**④ 一份 Gateway 配置接入飞书** |
| 工程量        | 600-800 行 Python 核心 | **① 核心 600-800 行 Python**<br>- 集中在chaos_tool / cluster_tool / report_tool 三个工具上。<br>**② Skill 是 Markdown**<br>- 几小时就够<br>**③ Cron 和 Gateway 是配置**<br>- 纯参数                                                                                    |

#### (2) 边界图

把这份认知画成一张边界图，左右一目了然：

<!-- 
图片内容说明
路径：imgs/aicmigr-24-autotest-01-hermes-decomp-learning/966b50fdd0e839d16921a5bf63b17f8b_MD5.jpg
用途：作为拆需求的起点，把任务划分为白嫖区与开发区
内容：左侧白嫖区（Hermes 原生能力），右侧开发区（要自己写的 Tool / SKILL.md / 配置）
-->

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/966b50fdd0e839d16921a5bf63b17f8b_MD5.jpg" style="display: block; width: 800px;" alt="边界图">

#### (3) 这张图是拆需求的起点

边界图把任务分成两半：

- 白嫖区不用拆，只需要拆开发区
- 原本一句话需求里那些模糊的「自动跑」「自动通知」「自动收集」，全都被 Hermes 接走了
- 真正要拆的是：开发区里那几个 Tool 怎么写、SKILL.md 怎么把它们串起来

## 5. 把认知沉成体系：三轮叠加

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/29d9e1dfdb501c23b20dbe8a3d42bad0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 5.1 为什么不能停在 AI 的总结上

跑完三个提示词，手上已经有了一份对 Hermes 够用的认知。这时候容易犯一个错：觉得够了，不再往下走。

问题在于，AI 给出的都是经过总结后的内容。如果一直只看这种被总结过的内容，没法真正学会一个东西 —— **总结的过程会丢东西，而丢掉的往往就是建立体系化认知最需要的部分**。

<span style="color: red; font-weight: bold;">这就像你只看别人画的架构图、永远不读原始的设计文档，你对那个系统的理解就停在「大概长这样」，遇到边角问题就会露馅。</span>

### 5.2 三轮叠加学习法

正确的做法是回到官方文档，但不是从头到尾啃一遍，而是带着两个东西去过文档：

- 前面 AI 帮你建立的认知框架
- 「做一个 XX 系统」的具体任务

看的标准：**大概看懂、大概知道有什么东西就行**。不需要把每一篇都搞懂。

<span style="color: red; font-weight: bold;">为什么这个标准就够了？因为下一步拆完需求后还会带着拆出来的需求再回头看一次。</span>三轮叠加下来效果最好：

```text
第一轮：AI 建立顶层认知
第二轮：带着认知 + 任务回看官方文档
第三轮：拆完需求后，回看认知框架与官方文档
```

AI 与系统化资料各有擅长，组合使用最强：

| 学习方式 | 适合的阶段 | 局限 |
|--------|---------|------|
| Claude Code 辅助学习 | 快速建立认知、总结核心要点、过滤与任务无关的部分 | 给的认知片段化，不够成体系；从头学一个大项目效果不好 |
| 官方系统化资料 | 把 AI 建立的认知沉下去、补成体系 | 不带框架去读会迷失在细节里 |

## 6. 速查表与思考

<img src="imgs/aicmigr-24-autotest-01-hermes-decomp-learning/17f4113c1d9529d7f39befbee9ab4c96_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 6.1 接到「基于开源项目做需求」任务的 Check List

把前面讲过的方法论裁剪成一份速查表。每接到一个「用 XX 开源项目实现 YY 需求」的任务，照此清单逐条勾。

#### (1) 第 0 步：明确任务边界

- [ ] 用一句话写出需求（谁、要做什么、关键约束是什么）
- [ ] 列出任务需要的能力清单（每条独立，便于后续对照）

#### (2) 第 1 步：建立四维度认知

- [ ] 它是什么：定位、解决什么、不解决什么、与同类工具关系
- [ ] 它能做什么：原生能力清单、不写代码能白嫖多少
- [ ] 它怎么扩展：扩展机制、扩展边界、开发范式
- [ ] 它内部怎么跑：进程模型、状态管理、对外接入面
- [ ] 至少能回答「不解决什么」——这块比「能做什么」更值钱

#### (3) 第 2 步：让 AI 帮你建立认知

- [ ] 三个提示词分别覆盖「是什么+内部机制」「能做什么」「怎么扩展」
- [ ] 每个提示词都嵌入具体业务任务，不是裸问
- [ ] 提示词二把任务能力列成清单，让 AI 对照而不是罗列
- [ ] 提示词三措辞留反问余地（疑问句而非肯定句）

#### (4) 第 3 步：输出边界图与工程量判断

- [ ] 画一张边界图：白嫖区 vs 开发区一目了然
- [ ] 给出开发区工程量估算（行数级别 + 集中在哪几块）
- [ ] 区分「结构化产物用代码生成」与「叙述性产物交给 LLM」

#### (5) 第 4 步：回看官方文档沉成体系

- [ ] 带着 AI 建立的认知框架 + 任务，过一遍官方文档
- [ ] 标准是「大概看懂」即可，不必逐篇搞懂
- [ ] 拆完需求后，回看一次认知框架与文档，完成三轮叠加

#### (6) 反模式（避免）

- ❌ 第一件事就动手写代码
- ❌ 「Claude，告诉我 XX 是什么」裸问
- ❌ 把肯定句写死，不给 AI 留反问余地
- ❌ 停在 AI 总结层，不回看官方文档
- ❌ 把所有产物都交给 LLM 生成（包括结构化 JSON）

### 6.2 思考

回想最近一次基于开源项目做二次开发的经历，如果重新来一次：

- 你的「拆解框架」应该是哪几个维度？
- 接到任务时问 AI 的第一个问题，有没有带「任务视角」？

这套打法不是 Hermes 专用的——下次接到任何「用某某开源项目实现某某需求」的任务，四维度框架和带任务问的姿势，都能直接套上去。**框架决定问什么，任务决定怎么问**，两个都到位，认知建立的速度会比想象的快。
