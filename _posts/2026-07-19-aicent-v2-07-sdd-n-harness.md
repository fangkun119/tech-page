---
title: AI编程方法(2) 07：SDD 和 Harness - AI 编程的两大支柱
author: fangkun119
date: 2026-07-19 07:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-v2-07-sdd-n-harness/cover.jpg
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
AI编程方法(2) 07：SDD 和 Harness - AI 编程的两大支柱
aicent-v2-07-sdd-n-harness
-->

<img src="imgs/aicent-v2-07-sdd-n-harness/b25aa3805e507d79432e3a0a90b9c0c2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

AI 编程这两年冒出来不少方法论和工具：SDD、Harness、Vibe Coding、Context Engineering、ReAct、Test-Driven AI、Plan-then-Execute、AI-First Architecture。我的判断很直接：<span style="color: red; font-weight: bold;">当下真正成熟可教的方法论，只有两个，SDD 和 Harness Engineering</span>。这篇文章就把这两根支柱讲透。

这两个方法论不是同一回事，各自解决 AI 编程里一个不同的核心问题：

1. <span style="color: red; font-weight: bold;">SDD 解决"想"的问题</span>，把"想清楚自己要什么"这件事工程化，通过结构化的 spec 让 AI 拿到清晰的执行依据。
2. <span style="color: red; font-weight: bold;">Harness 解决"做"的问题</span>，把"AI 怎么做"这件事工程化，通过一套配置和约束让 AI 在合理边界内工作。

两个合起来是闭环，只学一个都不够。SDD 落到具体工具：OpenSpec、Spec-Kit，以及它们对应的不同使用模式。Harness 落到具体的约束工具：CLAUDE.md、Skills、Slash Commands、Subagents、Plan Mode、Permission、Hooks。

## 1. 为什么传统工程师需要 SDD 和 Harness

<img src="imgs/aicent-v2-07-sdd-n-harness/77fa1b0e21444f2251b16c158dfd715d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

你写了十年需求文档、做过无数次 code review、搭过 CI/CD 流水线，这些经验在 AI 编程时代还值钱吗？值钱，但需要一次重新映射。这一章先把两大支柱的定位讲清楚，让你看到传统经验和 AI 编程之间不是断层，是一座桥。

### 1.1 AI 编程的根本变化：想和做被分开了

传统开发里，你想一点做一点，需求和实现紧密耦合在你的脑子里。AI 编程把这个耦合撕开了：<span style="color: red; font-weight: bold;">"想"全部留给你，"做"被外包给 AI</span>。

打个比方。传统开发像你自己开车，方向盘、油门、刹车都在你手里，你想怎么走就怎么走。AI 编程像你坐在副驾，给一个能力极强但缺判断边界的新司机指路。你说一句模糊的需求，他凭概率猜你的意图：猜对了是运气，猜错了你纠正，他再猜一次，你再纠正。这种"凭直觉聊天"的协作方式，效率低，质量不稳。

而且根本的失败源头不在 AI，在你没把需求想清楚。AI 不知道你要什么、不知道边界在哪、不知道验收标准。这种状态下，再强的模型也救不了你。

### 1.2 SDD 解决"想"的问题

SDD（Spec-Driven Development，规格驱动开发）的作用，是把"想"这件事工程化。

通过结构化的 spec（规格），把模糊需求变成 AI 能消化的清晰输入。spec 不是简单的需求文档，<span style="color: red; font-weight: bold;">它更像你熟悉的需求规格说明书加上接口契约</span>：有结构、有验收标准、有边界约束。AI 读完它，知道要做什么、做到什么程度、什么不该做、做完怎么验证。

SDD 的灵魂是把"想"从藏在你脑子里的隐性知识，变成可以被审查、可以被共享、可以被 AI 消化的显性产物。这一步你以前也做（写需求文档、定义接口），只是 AI 编程把它的重要性放大了——因为 AI 没有你脑子里的默认约束，你必须显式写出来。

### 1.3 Harness 解决"做"的问题

Harness 的字面意思是"挽具"。AI 是匹烈马，能力极强但缺判断边界，放任它跑会出事：它会跑偏（偏离任务焦点）、会失控（改了不该改的代码）、会被上下文污染（读太多无关信息后做出错误判断）、会做不可逆操作（没经过你确认就动手）。

Harness 的作用是给 AI 装上挽具，让它在合理边界内工作。<span style="color: red; font-weight: bold;">你可以把 Harness 理解成 CI/CD 流水线 + code review 制度 + 权限管理的合体</span>——这些都是你传统开发里已经在用的工程化机制，只是 AI 编程需要一套新的实现。

具体怎么装？通过一整套配置和约束：

- 让 AI 一上来就知道项目规则（System Prompt 类工具）
- 给 AI 可复用的能力封装（Tools 类工具）
- 让 AI 在合适的上下文里工作（Context 类工具）
- 在 AI 动手前拦住危险操作（Permission）
- 在 AI 做完后自动验证（Hooks）
- 让 AI 重要操作先规划后执行（Plan Mode）

这些机制合起来，AI 从一个"放飞的执行者"变成"可控的协作者"。

### 1.4 两者合起来才是闭环

这两个方法论必须合起来用，少一个，另一个的价值会被严重稀释。

| 只用 SDD | 只用 Harness | 两者合一 |
|---------|-------------|---------|
| spec 写得很好，但 AI 执行时跑偏没办法管住 | Hooks 配得很全，Permission 划得很细，但 AI 不知道要做什么 | SDD 给清晰目标 + Harness 给严格边界 |
| AI 可能看着 spec 也写出不符合 spec 的代码 | Hooks 拦得住 AI 写错代码，拦不住 AI 写对了你没真正要的东西 | 出错有 Hooks 拦截，危险操作有 Permission 拒绝，关键决策有 Plan Mode 复核 |
| 有清晰目标但没有控制力 | 有强控制力但没有清晰目标 | 完整闭环 |

一句话收束：<span style="color: red; font-weight: bold;">SDD 告诉 AI 做什么，Harness 保证 AI 做对</span>。前者从需求侧约束，后者从执行侧约束。

<img src="imgs/aicent-v2-07-sdd-n-harness/dc75382707ba35e91ef9e6fd4ffc5756_MD5.webp" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-07-sdd-n-harness/dc75382707ba35e91ef9e6fd4ffc5756_MD5.webp
用途：展示 SDD 和 Harness 两大支柱的定位关系，作为本章的视觉收束
内容：SDD 解决"想"的问题（需求侧约束），Harness 解决"做"的问题（执行侧约束），两者合起来构成 AI 编程的完整闭环
-->

## 2. 凭什么是这两个

<img src="imgs/aicent-v2-07-sdd-n-harness/9640685bd9f21dd2691fe9ecfa45f488_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

你可能会问：AI 编程方法论这么多，为什么单单这两个值得作为支柱？

### 2.1 选支柱的三条标准

我选支柱有三条标准：

1. <span style="color: red; font-weight: bold;">成熟</span>：有完整概念体系和经过验证的实践经验。
2. <span style="color: red; font-weight: bold;">可教</span>：有具体步骤、可用工具、可练动作。
3. <span style="color: red; font-weight: bold;">覆盖完整工程</span>：能贯穿从需求到上线的整条链路。

三条标准合起来过，只有 SDD 和 Harness 同时满足。

### 2.2 SDD 和 Harness 为什么同时满足

SDD 有 Spec-Kit 和 OpenSpec 等成熟工具支撑，有 `/specify → /plan → /tasks → /implement` 这样的清晰流程，有 spec.md / plan.md / tasks.md 这样的具体产出物，从需求到测试贯穿全程。

Harness 有 Anthropic 官方持续迭代，有 CLAUDE.md、Skills、Hooks、Permission 等具体落地机制，每一项都能配出来、看到效果，从开发到部署贯穿全程。

### 2.3 其他方法论差在哪里

按这三条标准过其他方法论，都差一些火候：

| 方法论 | 现状 | 为什么不够格作为主轴 |
|--------|------|---------------------|
| Vibe Coding | 更像姿态不是方法论 | 没有具体步骤可学，只能作为底色不能作为主轴 |
| Context Engineering | 方向对 | 没有标准工具、没有成熟工作流，半年后可能完全不一样 |
| ReAct / TAO 等 Agent 框架 | 适用范围窄 | 只适合做自主决策型 Agent 应用，不通用 |
| Test-Driven AI | 是好实践 | 只是具体动作，更适合作为 SDD 落地的一步 |
| Plan-then-Execute | 已落地 | 在 Harness 的 Plan Mode 里已经直接落地 |
| AI-First Architecture | 概念太新 | 还没有可操作实践 |

### 2.4 一个判断

这不是说其他方法论没价值，是说它们当下还不到"作为主轴"的成熟度。等它们成熟了，自然会进入主轴。但现在，把精力投在 SDD 和 Harness 上，是最稳的选择。

我的立场很明确：<span style="color: red; font-weight: bold;">学方法论不在多，在精</span>。先把这两个支柱在真实项目里跑熟，你有能力判断其他方法论的位置；而不是在十种半生不熟的方法论里来回跳。

## 3. SDD 的三种落地模式

SDD 的核心动作是**写 spec、拆任务、执行任务**。在工具层面，当下最值得知道的是 Spec-Kit 和 OpenSpec，它们各自代表了一种模式。但还有第三种模式，不依赖任何 SDD 专用工具，在实践中被很多有经验的工程师采用。三种模式一起讲，SDD 的图景才完整。

### 3.1 Spec-Kit：自动化模式

<img src="imgs/aicent-v2-07-sdd-n-harness/c538347930c10ca54c455a775f5448e1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Spec-Kit 是 GitHub 在 2025 年推出的 SDD 工具，目前是这个领域最成熟的工具之一。它的核心工作流是四个命令：

1. `/specify`：定义需求。你给一个粗略的功能描述，Spec-Kit 帮你把它扩展成一份结构化的 spec.md，包含背景、目标、用户故事、验收标准、非目标（明确说不做什么）等部分。这一步把"我有个想法"变成"我有一份完整需求"。
2. `/plan`：规划架构。基于已经写好的 spec，Spec-Kit 帮你产出 plan.md，内容包括技术选型、架构决策、模块划分、关键约束、风险点。这一步把"我知道要做什么"变成"我知道大致怎么做"。
3. `/tasks`：拆任务。基于 spec 和 plan，Spec-Kit 让 AI 自动拆出一份 tasks.md，通常 40 到 50 个具体可执行的任务，每个任务有明确的边界和验收。这一步把"大致方案"变成"可执行清单"。
4. `/implement`：执行实施。AI 按 tasks.md 一个个任务执行，你最后 review 整体代码。这一步把"清单"变成"代码"。

四个命令跑下来，你的工作是**写第一份 spec、检查 plan、最后 review 代码。中间的拆任务和执行任务，AI 自动跑**。这是 Spec-Kit 最大的特色，也是它被叫做"自动化模式"的原因。你可以把它理解成一条"全自动流水线"：原料（spec）进去，成品（代码）出来，中间少人工干预。

Spec-Kit 适合的场景：中等复杂度任务、探索性开发（快速试错）、新手工程师（还在学 SDD）、团队协作（让 AI 拆出来的 tasks.md 作为大家对齐的契约）、时间紧不追求极致质量。

它的代价也清晰：**AI 拆任务的质量，天花板是你 spec 的质量，spec 写得再好，也无法包含你脑子里所有的工程直觉**。

两轮推断叠加是个真实问题：AI 拆任务一轮推断，AI 执行任务再一轮推断，误差源头多。任务粒度通常偏大，AI 在大任务里更容易凑合、跳过、幻觉。最致命的是，AI 在不理解时不会停下来问，会编造一个看似合理的实现。你最后 review 整体代码时，已经发现不了"AI 拆错了任务"，错误源头在前，代价在后。

### 3.2 OpenSpec：手动控制 + delta spec

OpenSpec 是社区驱动的开源 SDD 工具，核心定位和 Spec-Kit 有所不同。它强调两件事：**手动控制和 delta spec**。

手动控制模式的工作流是这样的：你自己写 spec.md 和 plan.md，这一步和 Spec-Kit 类似。但接下来不同：**你自己拆任务，基于自己的工程判断，把整个工作切分成你觉得合适的粒度。然后逐个把任务交给 AI 执行，每个任务执行完你 review，不完美就改，改完进入下一个**。

人和 AI 分工清晰：**人拆任务，AI 只执行单个任务**。你可以把它理解成"你当包工头，AI 是你手下的工人"——活怎么分、分多大、先做什么后做什么，都是你定，AI 只负责把你给的活干完。

任务粒度没有硬规定，凭工程感觉，一般按功能切。一个完整功能，或者功能的一部分，作为一个任务给出去。你觉得合适就给，这是个判断动作，不是死规则。

OpenSpec 适合的场景：0 到 1 新项目（关键架构决策不能交给 AI）、系统级项目（进程、隔离、安全等高风险代码）、关键路径代码（出错代价大）、有经验的工程师（有自己的工程直觉）、复杂度高追求质量的场景。

**delta spec 是 OpenSpec 的另一个关键特性**，Spec-Kit 当下还没有这个机制。delta spec 是"增量规格"，你不需要写完整的 spec，只需要写"这次要改什么、改成什么样、为什么改、不破坏什么"。

delta spec 在改造场景下的价值非常大。改造一个老项目时，你不需要写"完整 spec"，因为老项目已经在那里了。你需要的是一份能让 AI 理解"这次的边界"的文档。delta spec 把这件事工程化了：给 AI 一份 delta spec，它知道在哪里改、不在哪里改、改成什么样、改完怎么验证。

delta spec 也适用于其他增量场景：

- 做开源贡献时写 PR 描述（PR 本质上就是一个 delta spec）
- 给现有项目加 feature（不是从零写，是基于现有结构增量）
- 修复一类系统性 bug（明确边界范围和影响）

这些场景下用 OpenSpec 的 delta spec 模式，比强行套 Spec-Kit 的完整 spec 流程更顺。

### 3.3 轻量手工模式：只用 CLAUDE.md 和 Skill

<img src="imgs/aicent-v2-07-sdd-n-harness/3434acefca5468e6b079348f7b217e78_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

还有第三种模式，在实践中被很多有经验的工程师采用，但很少被作为一种正式模式讲。它的核心是：**不用任何 SDD 专用工具，人在脑子里完成方案设计和任务拆分，AI 一段一段执行，人一段一段 review**。

工作流是这样的：你拿到一个需求，完整地想透——技术方案、模块划分、关键决策、边界处理，这些事不写成 spec.md，在脑子里跑通，可能在草稿纸上画几笔架构图，但不形式化。然后你把整个工作拆成一系列小任务，每个任务粒度小到 AI 一次能写完、你一次能 review 完，一般是一个函数、一个类、一段几十行的逻辑。然后你把任务一个个交给 AI 执行，每段写完你立刻 review，不满意就改，改完进入下一段。

工具上只用 Harness 最基础的两件：**CLAUDE.md 提供项目上下文和编码规范，Skill 封装重复模式**。不用 Subagents，不用 Plan Mode，不用 Hook 自动校验，不用 Headless。整个协作过程极轻量。这种姿态最像"我口述，你打字"——所有思考、决策、判断都在你手里，AI 只是执行你的方案。

这种模式有五个明显的优势：

| 优势 | 说明 |
|------|------|
| 质量可控 | AI 不做任何创造性决策，所有方案、架构、接口、命名都是你定的。AI 只做执行，执行性任务失败率低、容易复现、容易验证。AI 最容易出问题的"凑合做决策"在这种模式下根本不会发生。 |
| 代码可 review | 每段 AI 产出小到能完整看清楚。不会出现 Subagents 并行跑出来几千行代码、你扫一眼觉得差不多对、合并后真出问题找不到根因这种情况。代码增量始终保持在你认知带宽之内。 |
| 代码可维护 | 产出的代码风格、抽象、边界和你自己写出来的几乎一样，因为方案是你设计的，任务是你拆的。半年后回来看，认得出来是你的代码。相比之下，让 AI 自由发挥的代码，半年后你自己都看不懂为什么这样写。 |
| 速度其实不慢 | 真实经验里，AI 做错决策的成本远高于人做对决策再让 AI 执行的成本。这种模式前期人多花 30 分钟把方案想透，后面 3 小时稳稳推进，总时长可能比放手让 AI 跑还短，产出质量是确定的。 |
| 工具学习成本极低 | 只要会用 CLAUDE.md 和 Skill，学习成本可能就半小时。真正花精力的地方在"想清楚方案、拆好任务"，而这恰恰是工程师本来就该练的能力。 |

这种模式的适用边界也要说清楚：

- **它最适合**：你已经熟悉的技术栈、你完全想得清楚的业务逻辑、增量开发、关键路径代码、对可维护性要求高的项目。简单说，你脑子里能跑通整个方案的场景。
- **它不太适合**：完全陌生的领域（你脑子里都没方案，怎么拆任务）、需要大量探索的项目（还没到执行阶段）、规模过大的项目（任务数到一定量，纯靠脑子拆会乱）、纯产品验证型项目（质量要求低，速度优先）。

这种模式可以看作 OpenSpec 手动控制模式的进一步轻量化。OpenSpec 至少还有工具承载 spec.md 和 plan.md 这些产出物，这种模式连产出物都不形式化，直接在脑子里跑通。两者本质上是同一种姿态在不同重量级下的展开：**人主导思考，AI 主导执行，任务粒度始终保持小，人始终在 review 循环里**。

讲到这里值得拎出一个判断：<span style="color: red; font-weight: bold;">SDD 的核心是思考方法，不是工具本身</span>。Spec-Kit 和 OpenSpec 给了 SDD 落地的工具，但不是所有场景都需要这些工具。当你对项目足够熟悉、规模可控、任务粒度天然小，SDD 的"想清楚再做"这个核心动作，在你脑子里就能完成，产出物不需要外化。这不是 SDD 失效，是 SDD 已经内化成你的工作习惯，不需要外部脚手架了。

### 3.4 三种模式的本质对比

把 Spec-Kit 的自动化模式、OpenSpec 的手动控制模式、轻量手工模式放在一起对比，有几个本质区别值得讲清楚。

<img src="imgs/aicent-v2-07-sdd-n-harness/08d422beaab846a5dfdf8ee4df625207_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-07-sdd-n-harness/08d422beaab846a5dfdf8ee4df625207_MD5.jpg
用途：在进入三种模式本质对比前，给出三种 SDD 模式的可视化对比示意图
内容：Spec-Kit 自动化模式、OpenSpec 手动控制模式、轻量手工模式三种 SDD 落地模式的工作流对比，展示人 AI 分工、任务粒度、控制力度的差异
-->

| 维度 | Spec-Kit 自动化 | OpenSpec 手动控制 | 轻量手工模式 |
|------|----------------|------------------|-------------|
| 全局视角 | AI 试图同时拥有（从 spec 推断，失真严重） | 永远在人手里 | 永远在人手里 |
| AI 误差放大 | 两轮推断叠加（误差相乘，80%×80%=64%） | 一轮（只执行，无前置误差） | 一轮（只执行，无前置误差） |
| 任务粒度 | AI 决定，通常偏大（一个功能甚至模块） | 人决定，按功能切，适中 | 最小（函数/类/几十行逻辑） |
| 发现问题时机 | 最后 review 整体代码（回溯成本大） | 每个任务执行完立刻 review | 每段几十行完成立刻 review（最早） |
| 工程师角色 | 像产品经理（前期 spec + 后期 review） | 像团队 leader（全程工程决策） | 工程师本人（只外包敲键盘） |
| 能力要求 | 相对低（主要会写 spec） | 高（spec + 拆任务 + review + 改） | 最高（不能藏拙，所有能力暴露在外） |

展开讲两个最关键的维度。

<span style="color: red; font-weight: bold;">全局视角在谁手里</span>：自动化模式里，AI 试图同时拥有全局视角（拆任务）和局部视角（执行任务）。但 AI 的全局视角是从 spec 推断出来的，失真严重——你 spec 里没说的东西、你脑子里默认的约束、你团队的真实优先级，AI 都不知道。AI 用一个不完整的全局视角拆出来的任务，经常错过关键路径。手动控制模式和轻量手工模式里，全局视角永远在你手里，AI 只在局部工作。这种分工让 AI 待在它擅长的位置（执行），让你待在你擅长的位置（判断）。

<span style="color: red; font-weight: bold;">AI 误差的放大倍数</span>：自动化模式有两轮推断叠加，误差是相乘而不是相加。拆任务对了 80%，执行任务对了 80%，最终产出只有 64% 接近你真正想要的。这是自动化模式最难解决的根本问题。手动控制模式和轻量手工模式都只有一轮推断——拆任务由你做（没有 AI 误差），AI 只在执行时推断一次，最终产出质量上限就是 AI 单次执行的质量，不会被前置环节稀释。

<span style="color: red; font-weight: bold;">三种模式服务不同水平的工程师，这件事不能藏着说</span>：新手用自动化模式更顺，因为 AI 帮你补了你还没建立起来的判断；中级工程师用手动控制模式更稳，工具能承载你不能全部装在脑子里的内容；资深工程师在熟悉的项目上用轻量手工模式效率最高，因为你的判断 AI 替代不了，工具反而会拖你后腿。

### 3.5 你应该选哪个

按几个具体情况说。

<img src="imgs/aicent-v2-07-sdd-n-harness/c51d6b9df440a21ea220b557a27a845d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-07-sdd-n-harness/c51d6b9df440a21ea220b557a27a845d_MD5.jpg
用途：在选型决策前，给出按工程师水平和项目类型选择 SDD 模式的决策图
内容：根据工程师能力阶段（新手/中级/资深）和项目类型（新项目/改造/系统级/熟悉项目增量）推荐对应的 SDD 模式（Spec-Kit/OpenSpec 手动/delta spec/轻量手工）
-->

| 你的情况 | 推荐模式 | 理由 |
|---------|---------|------|
| 新手或中级，新项目从零开始，中等复杂度 | Spec-Kit 自动化 | 工作流帮你建立 SDD 基本流程，即使产出不完美也能在过程里学会 SDD 思维。这个阶段追求"先把流程跑通"。 |
| 资深，新项目从零开始，系统级 | OpenSpec 手动控制 | 系统级项目的关键架构决策不能交给 AI，你必须全程在场。产出可控，工程判断能完整传递到代码里。 |
| 资深，熟悉的项目，增量开发或关键路径 | 轻量手工模式 | 你脑子里能跑通整个方案，任务拆分是几分钟的事，工具反而拖慢节奏。CLAUDE.md + Skill 在质量、可维护性、速度之间达到最佳权衡。 |
| 老项目改造 | OpenSpec delta spec | 改造场景下你不是在写未来，是在改过去。delta spec 把"这次要改什么、改成什么样、不破坏什么"工程化，正好对应改造需求。若对老项目非常熟悉且改造规模小，直接用轻量手工模式也行。 |
| 做开源贡献（写 PR） | OpenSpec delta spec | 一个 PR 本质上就是一个 delta spec，maintainer 一眼能看到你的改动边界和验收逻辑，合并速度会快很多。 |
| 团队协作 | 混用 | Spec-Kit 拆出来的 tasks.md 作为团队对齐契约，具体执行时关键任务用手动控制（资深的人盯），次要任务用自动化（让 AI 跑批量）。 |

最后一条判断：<span style="color: red; font-weight: bold;">三种模式都该会，场景不同切换用</span>。它们不是替代关系，是互补关系。Spec-Kit 给你完整流程的脚手架，适合不熟的场景；OpenSpec 给你 delta spec 和手动控制的灵活性，适合改造和系统级；轻量手工模式给你最高的产出可控性，适合熟悉的场景。一个成熟的 AI 编程工程师工具箱里，三种模式都该有，根据场景选最合适的。

## 4. Harness 的约束落地工具

讲完 SDD，讲 Harness。Harness 的原理前面已经讲过——给 AI 装挽具，让它在合理边界内工作。这一章不再讨论原理，直接落到工具上：Harness 通过什么具体工具实现？每个工具是什么、怎么配、什么时候用？

### 4.1 整体图景：三道防线 + 杠杆 + 隔离 + 项目规则

<img src="imgs/aicent-v2-07-sdd-n-harness/8a4e4310305b7e6b2be22dbb24307dc0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Harness 的六个工具看起来散，但它们有清晰的结构。在进入细节前，先给你一张心智地图：

<img src="imgs/aicent-v2-07-sdd-n-harness/d54d789ac1d5289b727711b94e17c033_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-07-sdd-n-harness/d54d789ac1d5289b727711b94e17c033_MD5.jpg
用途：在 Harness 工具细节展开前，给出 Harness 六大工具体系的总览图，作为本章的导航地图
内容：Harness 的六个工具（CLAUDE.md、Skills/Slash Commands、Subagents、Plan Mode、Permission、Hooks）的分类与定位，按项目规则、能力杠杆、上下文隔离、三道防线（前中后）的结构组织
-->

- **项目规则（基础）**：CLAUDE.md —— AI 一上来就懂项目
- **能力杠杆**：Skills 和 Slash Commands —— AI 调用你封装的能力
- **上下文隔离**：Subagents —— AI 在隔离上下文里工作
- **三道防线**：
  - 前置：Plan Mode —— 重要操作先规划
  - 中置：Permission —— 在你定义的边界内自主
  - 后置：Hooks —— 做完后自动验证

记住这个结构，下面每个工具都会归位。如果你传统开发里搭过 CI/CD 流水线、定过 code review 流程、管过生产权限，这个结构对你不陌生——只是换了一套针对 AI 的实现。

### 4.2 CLAUDE.md 和 AGENTS.md：项目说明（基础）

**是什么**：放在项目根目录的 Markdown 文件，作为 AI 的"项目说明书"。Claude Code 启动时自动读取 CLAUDE.md，作为整个对话的 system prompt 前置。OpenAI Codex CLI 读取 AGENTS.md。Cursor 读取 .cursorrules。这几个文件命名不同，做的事情一样：让 AI 一上来就知道项目的上下文和规则。

**为什么需要**：没有它，AI 对你的项目一无所知，只能用通用知识凑。结果就是 AI 写出来的代码不符合你项目风格、用了不该用的库、改了不该改的文件、跑了不该跑的命令。CLAUDE.md 的角色，接近你传统开发里的项目架构约束文档加上新人 onboarding 文档——它把"新人进项目第一天该知道的事"一次性写清楚。

**怎么配**：一个最小可用的 CLAUDE.md，包含这四块内容：

1. **项目概况**：这个项目是什么、解决什么问题、技术栈是什么、目录结构怎么组织。三五句话讲清楚。
2. **运行命令**：怎么启动开发环境、怎么跑测试、怎么 build、怎么 lint。AI 经常需要执行这些命令，把命令写清楚省得 AI 自己猜。
3. **代码约定**：命名规则、错误处理风格、注释规范、测试要求。这些是项目特定的约束，AI 必须知道。
4. **禁区**：哪些目录不能改、哪些操作要先问、哪些命令绝对不能跑。这一块在改造大型项目时尤其重要。

最小版可以只有几十行，用着用着再加细节。不要一开始就追求完美。**CLAUDE.md 是用出来的，不是想出来的**——你在和 AI 协作过程中，每次发现"哦，这条规则 AI 应该知道"，就加一行到 CLAUDE.md 里。半年下来，你的 CLAUDE.md 会变成项目最有价值的文档之一。

**什么时候用**：任何持续使用 Claude Code 的项目，第一天就该写。这是 AI 编程工程化的基本功，投入小回报大。如果你用的是 SDD 的轻量手工模式，CLAUDE.md 是你和 AI 协作的核心承载，几乎所有项目规则都靠它传递，值得多花点心力维护。

### 4.3 Skills 和 Slash Commands：能力封装（杠杆）

**Skills 是什么**：把一个可复用的能力封装成 Claude 可调用的 skill。写在 `~/.claude/skills/`（个人 skill，跨项目共享）或项目的 `.claude/skills/`（项目 skill，团队共享）。每个 skill 是一个目录，里面有 SKILL.md 描述这个 skill 是什么、什么时候触发、怎么使用。

**Slash Commands 是什么**：斜杠命令，比如 `/review`、`/refactor`、`/test`，触发预定义的工作流。和 Skills 接近但更轻量。区别在于触发方式：Slash Commands 是手动触发的，Skills 是 AI 在合适场景下自动选择的。

**为什么需要**：团队和个人都有"反复做同一件事"的需求。代码审查、PR 描述、bug 排查、新模块脚手架、接入支付的标准流程，这些事每次重新讲给 AI 听是浪费。封装成 Skill 或 Slash Command，下次直接调用，AI 沿着你预定义的工作流跑，质量稳定。

**怎么配 Skills**：写一个 SKILL.md，核心结构是这样的：

1. 第一部分说这个 skill 是什么，一两句话讲清楚它解决什么问题。
2. 第二部分说什么时候用，给具体的触发场景，让 Claude 能自动识别该不该调用。比如"用户提到代码审查、PR review、看代码质量时，使用这个 skill"。
3. 第三部分说怎么用，详细的执行流程。可以包含具体的 prompt 模板、需要读的文件、需要跑的命令、产出物的格式要求。

写好的 skill 放到对应目录，Claude 自动加载。下次遇到相关场景，Claude 主动调用，你不需要每次提醒它。

**怎么配 Slash Commands**：在 `.claude/commands/` 目录下放一个 markdown 文件，文件名就是命令名（比如 review.md 对应 `/review`），内容是这个命令的执行 prompt。输入 `/review` 时，Claude 按这个 prompt 执行。

**什么时候用**：同一个流程在团队或个人这里被反复执行；需要新人快速对齐的项目约定；个人想沉淀的工作流模板；复杂的多步流程想固化成单一动作。

我的看法：Skills 和 Slash Commands 是个人和团队能力沉淀到 AI 上的核心机制。一个工程师工作三年，沉淀的不只是代码，是这一套属于自己的 Skills 库。下次启动新项目，这些 Skills 直接用，起点比别人高一截。

### 4.4 Subagents：任务隔离（上下文管理）

<img src="imgs/aicent-v2-07-sdd-n-harness/301b8f2a396e8e3b8ade64d4981abe33_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

**是什么**：Claude Code 的协作机制。从主对话里启动一个子代理，让它去做一件独立的事，比如读一大段代码、跑一个测试、调研一个问题。子代理在自己的上下文里工作，主对话不会被它读的几万行代码污染。

**为什么需要**：AI 的上下文窗口是有限的。你让主对话直接读 5 万行代码，后面几句话它就开始遗忘前面讨论的内容。这是 AI 编程里最常见的失败模式之一——你和 AI 聊得好好的，聊着聊着它就忘了开头的关键约束。Subagents 解决这个问题：让"读大量内容"这件事在隔离的上下文里发生，主对话只接收 Subagent 的总结结论，上下文保持干净。

**怎么用**：在 Claude Code 里，你可以显式让它"开一个 subagent 去做某件事"，或者通过 Task 工具调用预定义的 subagent。每个 subagent 有自己的 system prompt 和工具权限，你可以在 `.claude/agents/` 里预定义专用的 subagent，比如一个 code-reviewer subagent 只做代码审查，一个 test-runner subagent 只跑测试。

**什么时候用**：三种典型场景：

1. **需要读大量代码时**。改造大型项目时是必选。你让 5 个 subagent 分别读项目的不同模块，每个都返回一份总结，主对话基于这些总结做架构判断，而不是自己直接读 10 万行代码。
2. **需要并行做几件事时**。同时跑测试、查日志、读文档，三件事让三个 subagent 并行，主对话等结果汇总。比串行做快得多。
3. **想保护主对话上下文不被脏数据污染时**。比如你和 AI 在讨论一个架构决策，中间需要查一段不熟悉的代码，直接读会污染上下文，让 subagent 去读返回总结，主对话上下文干净。

注意一点：**如果你在用 SDD 的轻量手工模式，Subagents 反而是个负担**。任务粒度本来就小，上下文压力不大，主对话能装得下，引入 Subagents 反而打散了你和 AI 的协作节奏。Subagents 是上下文压力大的时候才用的工具，不是越多越好。

### 4.5 Plan Mode：先规划后执行（安全网前置）

**是什么**：Claude Code 的规划机制。开启后，Claude 在执行任何写操作之前，先输出一个完整的 plan——我准备做什么、按什么顺序做、改哪些文件、有什么风险。你确认后再开始动手。

**为什么需要**：解决"AI 乱动手"问题。AI 经常在没完全理解任务的情况下就开始改代码，等你发现的时候已经改了一堆错的东西。Plan Mode 强制它先停下来想清楚，把 plan 给你看，你判断 plan 对不对，对了再让它执行。

看 plan 比看代码容易得多。plan 是结构化的、几百字的，代码是几百行散落在多个文件的。用少量信息快速判断 AI 的理解是否正确，这就是 Plan Mode 的杠杆。

**怎么用**：在 Claude Code 里，Plan Mode 是一个可以切换的模式（快捷键或菜单切换）。开启之后，Claude 默认进入"先规划后执行"的模式。每个写操作前，先输出 plan，等你确认。

**什么时候用**：三类场景必开：

1. **重要操作时**。改生产配置、改核心模块、做大规模重构、改数据库 schema。这些操作出错代价大，plan 比代码便宜。
2. **不熟悉的代码区域时**。你自己也没把握 AI 会怎么动这块代码。让它先告诉你"我打算这样改"，你看 plan 就能判断它理解对没对。
3. **和 AI 第一次协作的新场景时**。比如刚开始用某个新工具、刚接手一个新项目、和 AI 一起做一类没做过的事。这种场景 Plan Mode 是安全网，防止 AI 一上来就跑偏。

日常熟悉的小任务可以不开 Plan Mode，效率优先。但任何"这次出错代价大"的场景，Plan Mode 默认开启，这是一条好规则。

### 4.6 Permission：事前权限（安全网中置）

<img src="imgs/aicent-v2-07-sdd-n-harness/5e6829eb9cee191fc45eb849b605a077_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

**是什么**：Claude Code 的事前约束机制。告诉 Claude "这些命令不需要确认就能跑"、"这些命令必须问我"、"这些命令绝对不能跑"。Permission 配置在 `~/.claude/settings.json` 或项目级的 `.claude/settings.json`，粒度可以非常细。

**为什么需要**：让你敢把控制权交给 AI。没有 Permission，你只能在两种极端模式里二选一：要么每个操作都手动确认（累），要么放任 AI 自己跑（怕）。中间没有空间。Permission 给你中间空间，精细划定 AI 的自主边界，安全和效率兼得。

**怎么配**：Permission 的配置是 JSON，核心是三类规则：

| 规则类型 | 含义 | 典型例子 |
|---------|------|---------|
| allow | 这些操作 AI 不需要问就能做 | 读文件、读目录、查 git 状态、跑测试这类纯读操作或安全的写操作 |
| ask | 这些操作必须问你才能做 | 写文件到敏感目录、跑可能改环境的命令、安装新依赖这类需要谨慎的操作 |
| deny | 这些操作绝对不能做 | 删除文件、修改 .env、跑 production 部署命令、改数据库 migration 这类红线 |

举个具体例子。一个 Web 项目的 Permission 配置可能是：allow 读所有源码、写 src/ 和 tests/ 目录、跑 npm test 和 npm run lint；ask 写 package.json、跑 npm install、改 .gitignore；deny 删除文件、改 .env、跑生产部署命令。这样配下来，AI 在 src/ 里能自由工作，涉及依赖和配置时会问你，生产相关操作直接被拒。

**什么时候用**：任何让 AI 自主跑命令的场景都该配。改造场景下尤其重要（限制 AI 只能改某些目录）。CI/CD 场景下是必选，严格限制 AI 能做什么。日常项目里，至少配几条 deny 规则保护红线，这是底线。

### 4.7 Hooks：事后检查（安全网后置）

**是什么**：Claude Code 的事后检查机制。Claude 在调用工具之前（PreToolUse）或之后（PostToolUse），自动触发你定义的脚本。脚本可以是任意命令：跑测试、跑 lint、检查 git status、发通知，任何能在命令行执行的事都行。

**为什么需要**：把"AI 做完之后人来验证"这件事变成"AI 做完之后机器自动验证"。AI 写错了代码，Hooks 立刻拦住或报告；AI 改坏了配置，Hooks 立刻发现；AI 引入了 lint 错误，Hooks 立刻让它改回来。你不需要再当"代码的最后一道防线"，Hooks 在前面替你拦住。这就是反馈循环的工程化。

**怎么配**：Hooks 配置在 `.claude/settings.json` 里，核心是两类：

- **PreToolUse hook**：在 AI 调用某个工具之前触发。常见用法是"AI 准备改某个敏感文件之前，先跑一个检查，如果检查不通过就拦住"。
- **PostToolUse hook**：在 AI 调用某个工具之后触发。常见用法是"AI 写完代码之后，自动跑测试，如果测试失败把结果反馈给 AI 让它修"。

最低限度的 Hooks 配置，建议两条：

1. 第一条：Edit 工具的 PostToolUse hook 跑 lint。AI 每次改完代码，自动跑 linter，有错误立刻反馈。这条让 AI 写出来的代码至少符合代码风格规范。
2. 第二条：Edit 工具的 PostToolUse hook 跑相关测试。AI 改完某个文件，自动跑对应的测试。这条让 AI 改坏功能的概率大幅下降。

更进一步可以加：Bash 工具的 PreToolUse hook 检查命令安全性（不让跑危险命令）、Write 工具的 PostToolUse hook 自动 format（AI 写完代码自动格式化）、Edit 工具的 PostToolUse hook 自动更新相关文档（AI 改完接口自动同步文档）。

**什么时候用**：任何严肃的项目都该配。Hooks 是把"工程化"从概念变成具体动作的关键机制。<span style="color: red; font-weight: bold;">没有 Hooks 的 AI 编程，本质上还是手工作坊；有了 Hooks，AI 编程才进入工业化阶段</span>。

注意一点：如果你在用 SDD 的轻量手工模式，Hooks 是可选项。任务粒度小、每段都立刻 review，你这个"人 review"本身就是反馈循环。Hooks 是给"你不在场"的场景准备的，比如批量自动化任务、CI/CD 集成、Subagents 并行执行。轻量手工模式下你始终在场，Hooks 的价值会被稀释。

### 4.8 工具整体协作与克制原则

这六个工具不是孤立的，它们组合起来才是完整 Harness。

<img src="imgs/aicent-v2-07-sdd-n-harness/814a29bfbb5e95836da6903ce672347b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-07-sdd-n-harness/814a29bfbb5e95836da6903ce672347b_MD5.jpg
用途：在讲完六个工具后，展示它们如何协作构成完整 Harness，强化整体协作图景
内容：CLAUDE.md（项目规则基础）、Skills/Slash Commands（能力杠杆）、Subagents（上下文隔离）、Plan Mode（前置安全网）、Permission（中置安全网）、Hooks（后置安全网）六个工具的协作关系，构成前中后三道防线 + 杠杆 + 隔离 + 规则的完整工程化机制
-->

六个工具串起来，你和 AI 的协作就有了**前中后三道防线 + 上下文隔离 + 能力杠杆 + 项目规则的完整工程化机制**：

1. CLAUDE.md 让 AI 一上来就懂项目（基础）
2. Skills 和 Slash Commands 让 AI 调用你封装的能力（杠杆）
3. Subagents 让 AI 在隔离上下文里工作（上下文管理）
4. Plan Mode 让 AI 重要操作先规划（安全网前置）
5. Permission 让 AI 在你定义的边界内自主工作（安全网中置）
6. Hooks 让 AI 做完后自动验证（安全网后置）

这是 Harness 真正的样子，不是某个单点工具，是一整套配置和约束的合奏。

但要补充一句：<span style="color: red; font-weight: bold;">不是所有场景都需要六个工具全上</span>。Harness 的工具是按场景取用的，不是堆得越多越好：

- **轻量手工模式**：只用 CLAUDE.md 和 Skill 就够了，其他四个可以不用。
- **批量自动化场景**：Hooks 和 Permission 是核心，Plan Mode 反而拖慢。
- **改造大型项目**：Subagents 和 Permission 是必选，Hooks 提供安全网。

我的判断：<span style="color: red; font-weight: bold;">工具该用才用，不用就不用，这种"克制"才是真正的工程师姿态</span>。工具堆得多不等于工程化做得好，工程化做得好是知道什么时候用什么工具。

## 5. 从今天开始的三步

<img src="imgs/aicent-v2-07-sdd-n-harness/34dfc14e4ac8b24b75423cdb9ff11987_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

讲完原理和工具，最后讲怎么从今天开始落地。三步，按顺序走。

### 5.1 第一步：从写 CLAUDE.md 开始

这是 Harness 最基础的工具，投入最小，回报立竿见影。

**今天动手**：挑你最常工作的那个项目，写一份 CLAUDE.md。不追求完美，先写最小版——几句话讲项目是什么、技术栈、运行命令、最重要的几条代码约定。半小时之内能写完。

**预期反馈**：写完之后，下次和 AI 在这个项目里协作，你会立刻感觉到差别——AI 不再问基础问题，不再用错误的命令，不再写不符合你项目风格的代码。这个体验之后，你就理解了 Harness 的价值：**它不是抽象概念，是直接节省时间的工程动作**。

### 5.2 第二步：选一种 SDD 模式开始

CLAUDE.md 写完后，开始练 SDD。怎么选？

- 如果你对当前项目不太熟，或者刚开始练 SDD，用 Spec-Kit 的自动化模式，工具会带着你走完整流程。
- 如果你对项目熟、有完整工程感，直接用轻量手工模式，人想透方案、拆好任务、AI 一段一段执行。
- 如果你在做老项目改造，用 OpenSpec 的 delta spec 模式。

三种模式选一种，选哪种取决于你的项目类型和你自己的能力阶段。

**别扭感提醒**：第一次用任何一种 SDD 模式都会觉得别扭——"我直接写代码不就行了，为什么要先写 spec、为什么要先想清楚"。这种别扭感很正常，所有从手撸代码切到 SDD 的工程师都经历过。

撑过第一次，你会发现 SDD 让你和 AI 的协作质量上了一个台阶：AI 不再凭直觉猜，你也不再反复纠正。这种正反馈一旦建立，SDD 就内化成你的默认工作方式了。

### 5.3 第三步：逐步加 Harness 的其他工具

CLAUDE.md 和 SDD 模式都稳定后，按下面这个顺序逐步加其他 Harness 工具，每一步等上一步稳定后再加：

1. **加 Hooks**：CLAUDE.md 稳定后，先配最简单的两条（写完代码自动跑 lint + 自动跑测试），一周之内显著减少你的 review 负担。
2. **加 Permission**：Hooks 顺了，给项目划几条 deny 红线（不能改 .env、不能跑生产部署、不能删文件），让你敢更放心地让 AI 自主跑。
3. **开始用 Subagents**：Permission 顺了，需要读一大段代码时开一个 subagent 去读，主对话保持干净。
4. **Plan Mode 默认开着**：重要操作前先看 plan，小任务可以关掉。
5. **Skills 和 Slash Commands 是最后进阶**：发现某个流程被反复跑（代码审查、PR 描述、新模块脚手架），就封装成 Skill 或 Slash Command。Skills 库的积累是长期过程，不急。

### 5.4 克制：不是所有工具都必须上

要补一句：<span style="color: red; font-weight: bold;">不是所有工具都必须上</span>。

如果你主要用 SDD 的轻量手工模式，CLAUDE.md + Skill 就够覆盖你 80% 的场景。其他工具按需引入，该用才用。工具堆得多不等于工程化做得好，工程化做得好是知道什么时候用什么工具。

三步走完，你已经在用 SDD + Harness 跑完整工作流了。这套姿态一旦建立，你和不用这套方法的工程师之间，会以惊人速度拉开差距。不是因为你更努力，是因为你的协作方式工程化了——前期投入是一次性的，后期回报是持续复利的。

## 6. 结语：AI 不是被信任的，是被工程化使用的

<img src="imgs/aicent-v2-07-sdd-n-harness/d8926e21cd69cf5211cce66a6f521f82_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

讲完 SDD 和 Harness 的原理、工具、落地，最后要讲一个贯穿全文的底层假设。这个假设是一切的前提，理解了它，你才能理解为什么 SDD 和 Harness 长这样。

### 6.1 一个底层假设

两个方法论共享一个底层假设：<span style="color: red; font-weight: bold;">AI 不是被信任的，是被工程化使用的</span>。

不是"AI 像同事一样自己会做事"，是"<span style="color: red; font-weight: bold;">AI 是一个能力极强但缺判断边界的执行者，需要被精确指挥、被严格约束、被持续校验</span>"。

SDD 是精确指挥（把需求想清楚交给 AI），Harness 是严格约束和持续校验（边界 + 验证）。这两个方法论，都是这个底层假设的自然产物。

### 6.2 接受或不接受这个假设，决定了你能否用上 SDD 和 Harness

接受这个假设，SDD 和 Harness 都是自然的产物。

不接受这个假设（觉得 AI 应该更自主、更聪明、更懂我），你会觉得 SDD 和 Harness 都太繁琐——"我都要写这么多 spec、配这么多 Hook，AI 还不如我自己写"。

我的判断：**很多工程师抗拒 SDD 和 Harness，本质上是抗拒这个底层假设**。他们心里期待的是"AI 像同事一样靠谱"，一旦发现 AI 需要被工程化约束，就感到失望和抵触。但这份期待本身就不现实。

### 6.3 真实的 AI 与 ROI 论证

真实的 AI 不是"理想协作者"，它今天不是，明年很可能也不是。模型升级不能补上"商业判断、团队约束、产品方向、关键拍板"这些根本不属于模型能力范畴的东西。模型再聪明，也不知道你们团队这个季度优先做什么、不知道哪个客户不能得罪、不知道哪段代码是祖传不能动。

在这种真实情况下，工程化使用是唯一稳定的协作方式。

前期投入是真实的：写 CLAUDE.md、写 spec、配 Hooks、调 Permission，每一件都要花时间。但这些投入是一次性的，回报是持续的：

- CLAUDE.md 写好一份，用一年。
- Skills 沉淀一个，用十次。
- Hooks 配好一遍，每天都在替你拦错。

<span style="color: red; font-weight: bold;">SDD + Harness 是 AI 编程里 ROI 最高的两件事</span>。你找不到比这投入产出比更高的工程化动作了。

### 6.4 收束：把这两根支柱练到肌肉记忆

回到最开头那个判断。AI 编程方法论很多，<span style="color: red; font-weight: bold;">但当下真正成熟可教的就两个：SDD 和 Harness。一个解决"想"的问题，一个解决"做"的问题，合起来是完整闭环</span>。

学方法论不在多，在精。把这两根支柱在真实项目里反复跑、反复打磨，练到肌肉记忆，你就拿到了 AI 编程时代真正稀缺的能力。

剩下的方法论，作为延伸阅读知道就行。等你把 SDD + Harness 用熟，你自己有能力判断它们的位置。
