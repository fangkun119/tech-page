---
title: AI编程方法(2) 10：Spec-Kit 和 OpenSpec 的原理与使用
author: fangkun119
date: 2026-07-19 10:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-v2-10-spec-kit-openspec-tutorial/cover.jpg
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
AI编程方法(2) 10：Spec-Kit 和 OpenSpec 的原理与使用
aicent-v2-10-spec-kit-openspec-tutorial
-->

## 1. 从 vibe coding 的痛点说起

### 1.1 工具一直在进化，根本问题却没解决

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/6fa5a61231513950109127dfc8179253_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

AI 编程这两年经历了一次范式跃迁：从 Copilot 的代码补全，到 Cursor、Claude Code 这类 agentic IDE，再到 Codex、Devin 这类自主编程 Agent。工具越来越强，但有一个根本问题始终没解决——**AI 编程的输出质量极不稳定，跟提示词质量强相关**。

同样的 AI，有人用它写出干净可维护的代码，有人用它写出一团乱麻。差距不在 AI，在你怎么跟它沟通。

### 1.2 这种工作方式有个名字：vibe coding

业界把"给 AI 一段模糊需求、让它直接生成代码"的方式叫做 **vibe coding**。这个词本身是中性描述，不是贬义：你凭"感觉"跟 AI 沟通，AI 凭"感觉"生成代码，你看着代码"感觉"对不对再调整。

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/ccd41e0e9336e61c5759080911f00124_MD5.webp" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-10-spec-kit-openspec-tutorial/ccd41e0e9336e61c5759080911f00124_MD5.webp
用途：可视化 vibe coding 的痛点与 AI 编程范式演进
内容：推测展示从代码补全到 agentic IDE 再到自主 Agent 的演进路径，或对比 vibe coding 与结构化开发的工作方式差异
-->

### 1.3 为什么中小任务没事，中大项目就崩

vibe coding 在写个小工具、改个 bug 这种中小任务上不明显。一旦放到中大规模项目，四个硬问题就会暴露出来。这四个问题你其实都不陌生，传统软件工程早就给过解药：

##### ① 需求漂移

项目跨多个会话推进时，AI 每次都要重新理解项目背景，前面的决策很容易被遗忘。这就像传统项目里没有需求基线，每个会话等于换了一个新人接手，上下文全丢。

##### ② 架构失控

AI 倾向生成"看起来能跑"的代码，缺乏整体架构约束，多次迭代后代码库变成一团乱麻。这就像传统团队没有 architecture review，每个人只对自己当下这块"能跑"负责，没有人对整体架构负责。

##### ③ 回退困难

vibe coding 没有正式的需求文档，做错了不知道该回到哪个状态。这就像传统项目里没有 git tag 对应需求版本，出了问题无法定位"当时需求到底是什么"。

##### ④ 无法协作

多人协作时每个人的提示词理解都不一样，产出风格不一致。这就像团队没有 coding convention，十个人写出十种风格。

### 1.4 为什么多背几条 prompt 也救不了

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/082154b62ad63dff4e8776374cf4c87d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

你可能会问：那我多写几条精细的 prompt、配一堆 Cursor Rules 不就行了？

答案是：不够。这四个问题的共同根源是**需求只活在 chat 历史里**。chat 是易失的——会话一关，需求就没了；下一个会话 AI 又从零开始猜你想要什么。

业界在 2025 年下半年形成共识：**仅靠 prompt engineering 不够了，需要在 prompt 之上加一层结构化的"需求层"**。这层需求不能活在 chat 里，必须活在项目仓库里，跟代码一起版本管理。

这就是 Spec-Driven Development（SDD）出场的背景。SDD 的核心主张一句话：**spec 是项目的事实来源，不是 chat 历史**。下一章我们拆开讲 SDD 到底是什么。

## 2. SDD 是什么：让 AI 读规约而不是读你的心思

### 2.1 先回答一个疑问：spec 不就是需求文档吗

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/4a5b8a72d098aeba8d5de0bdaced620b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

讲 SDD 之前，先破一个最容易卡住的疑问：**spec 不就是需求文档吗？我写瀑布那套不就行了？**

形似，神不同。

传统需求文档是写给人看的，写完归档，跟代码同步全靠人工。SDD 的 spec 是写给 AI 看的（同时也给人看）——更准确地说，**spec 就是 AI 的 system prompt**。更新 spec 等于更新 AI 的工作上下文。

这个视角转换是理解 SDD 的关键：spec 不再是一份交付物，而是 AI 的工作输入。

### 2.2 SDD 的核心主张

Spec-Driven Development 的核心主张是：**在 AI 生成代码之前，先生成 spec（规约）；spec 是项目的事实来源**。

SDD 工具链通常强制做四件事：

##### ① 把需求写成结构化文档

不是 chat 里说一嘴，而是写成 markdown 文件，落到项目仓库里。

##### ② 需求经过 AI 跟人的双向确认

AI 读懂需求后回写自己的理解，人确认后再生成代码。避免 AI 单方面"猜"歪了。

##### ③ 需求跟代码持续对齐

每次代码改动都要回到 spec 检查是否一致，发现漂移立刻修正。

##### ④ 保留完整的需求历史

所有决策都有 artifact 文件留底，可追溯、可审计。

### 2.3 跟传统瀑布的关键差异

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/0e2cd72700c117e25cbed270935b7bc8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

SDD 不是新概念。瀑布开发就是 spec-driven 的——先写需求文档再写代码。但传统瀑布的 spec 是"死的"，写完就僵化，无法跟代码同步演进。

**AI 时代的 SDD 是新形态：spec 是活的，AI 帮你维护它，让它跟代码持续保持同步**。

用一个对照讲透：传统瀑布的 spec 是一次性交付物，写完归档；SDD 的 spec 是持续维护的活文档，AI 每次改动都会回头检查它。这就是"spec 给 AI 看"这个视角带来的根本变化。

### 2.4 SDD 在 AI 编程谱系里的独特价值

把 SDD 放进整个 AI 编程辅助谱系，它的独特价值有五条：

##### ① 它是工作流层

套在 AI agent 之上，不替代 AI agent。你照常用 Claude Code、Cursor，SDD 只是给这套工具加上阶段化流程。

##### ② 它强制阶段化

从需求到代码有正式 artifact 流转，先 specify 再 plan 再 implement，不能跳。

##### ③ 它把"决策"持久化

spec 跟代码一起版本管理，不会因为 chat 会话切换丢失上下文。

##### ④ 它让 AI 跟人有"协议"

spec 是双方都遵守的契约，AI 不能擅自偏离，人也不能随口改需求。

##### ⑤ 它支持长期项目演进

需求是持久的，跨会话、跨周、跨月都能延续。

我的立场：**SDD 是 AI 编程从"作坊"到"工程"的关键一步**。没有 SDD 的 AI 编程，跟没有版本控制的传统编程是同一个问题——能跑，但不可持续。

## 3. SDD 在 AI 编程谱系里的位置

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/b002d8811193cde3e6a0a8dcdec63e42_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 3.1 一个常见疑问：配了 Cursor Rules 不就够了吗

很多读者会卡在这里：我已经给项目配了 Cursor Rules、写了 AGENTS.md、塞了一堆背景知识到 Claude Project docs，这不就是给 AI 需求了吗？为什么还要专门的 SDD 工具？

因为 Cursor Rules、Claude Project docs、AGENTS.md 本质是同一类东西——**静态 context**。它们给 AI 一份背景说明书，让 AI 工作时参考，但不约束 AI 怎么工作。

### 3.2 静态 context 跟 SDD 的关键差异

用一个类比讲透两者的差距：

静态 context 像给新员工一份员工手册——告诉他公司做什么、技术栈是什么、规范是什么。但员工拿到手册后，先做哪一步、后做哪一步、每步要产出什么交付物，手册不管。

SDD 像给新员工一套带门禁的流程——每个阶段必须产出正式 artifact（spec、plan、tasks）才能刷开门禁进入下一阶段。不只给背景，还强制工作流的阶段化。

关键差异就在这里：**Cursor Rules 加 Claude Project docs 不约束工作流**，AI 用什么顺序做、做什么阶段、产什么 artifact，都没规定。SDD 工具强制先 specify 再 plan 再 implement，不能跳。

### 3.3 业界主流 SDD 工具全景

2025 年到 2026 年初是 SDD 工具的爆发期，主流方案有四个：

##### ① Spec-Kit（GitHub 出品）

2026 年 5 月公开发布，90,000+ star。spec-as-source 最纯粹形式，6+3 个核心命令，覆盖 constitution 到 implement 的完整闭环。

##### ② OpenSpec（Fission-AI 出品）

社区项目，2026 年 1 月发布 1.0。delta-spec 范式，Propose→Apply→Archive 三态流程，brownfield-first。

##### ③ Kiro（AWS 出品）

跟 AWS 生态绑定，IDE 形态，限定 Claude 模型。

##### ④ Tessl

商业 SaaS，主打"living specs"，spec 跟代码双向同步。

### 3.4 为什么本文聚焦 Spec-Kit 和 OpenSpec

因为这俩是目前最活跃、最有代表性、社区采用最广的开源方案。更重要的是，**它们的设计哲学正好对立**：一个走 spec-as-source 的纯粹路线，一个走 delta-spec 的增量路线。理解这两个，就理解了 SDD 的两个主要流派。

### 3.5 作者看法

我的看法：**SDD 在 AI 编程时代不是可选项，是必选项**。区别只是用哪个工具、什么时候用。

对中大规模的 greenfield 项目，不上 SDD 就是给自己挖坑。对小增量改动，硬上 SDD 反而是 overkill。**工具选择匹配工作性质**——后面第 6 章会给一份完整的决策树。

## 4. Spec-Kit：spec-as-source 的重武器

### 4.1 Spec-Kit 是什么

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/0ea2a9b27631c3f26ee09cc73ba25ce6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Spec-Kit 是 GitHub 在 2026 年 5 月开源的 spec-driven development 工具链。它本质是一个 **CLI 工具加一套 slash command 集合**——不是 AI agent 本身，而是**套在 AI agent 之上的工作流层**。

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/8c40e1fdaf589c38f8b8b69f37fcbeaf_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-10-spec-kit-openspec-tutorial/8c40e1fdaf589c38f8b8b69f37fcbeaf_MD5.jpg
用途：展示 Spec-Kit 的整体架构与工作流
内容：推测展示 Spec-Kit 作为 CLI + slash command 层套在 20+ AI agent 之上的结构，以及 specify→plan→tasks→implement 的闭环流程
-->

它支持 20+ AI agent，包括 Claude Code、Copilot、Cursor、Gemini CLI、Codex 等。装一次 Spec-Kit，用什么 AI agent 都可以。

Spec-Kit 是 **spec-as-source 最纯粹形式**：spec 文件本身就是项目的源头，所有其他东西（plan、tasks、code）都从 spec 派生。spec 一变，下游一切都要重新生成。

### 4.2 设计哲学

Spec-Kit 的设计哲学有三个核心点。

#### (1) constitution 是非协商的项目宪法

每个 Spec-Kit 项目第一件事是写 `constitution.md`，把项目的非协商原则固化下来：用什么技术栈、什么编码规范、什么测试要求。

你可以把 constitution 理解为**项目级的架构约束文档**——类似传统项目里的 ADR（Architecture Decision Record），但更上层，定义的是整个项目不可协商的底线。所有后续命令都引用 constitution，AI agent 跑偏 constitution 会被人工纠正。

Spec-Kit 把"非协商原则"这件事工程化，让 AI agent 不再凭"感觉"决定项目方向。`constitution.md` 每个项目只写一次，整个项目生命周期内不轻易改。

#### (2) 4 阶段闭环 specify→plan→tasks→implement

每个 feature 都走完整 4 阶段：

##### ① specify

把需求写成 user story + acceptance criteria 的 spec。

##### ② plan

根据 spec + constitution 生成技术实施方案。

##### ③ tasks

把 plan 拆成可执行的有序任务列表。

##### ④ implement

AI agent 按 tasks 执行代码生成。

每个阶段都有正式 artifact 文件，前一阶段的输出是后一阶段的输入。**没有跨阶段的 shortcut，没有"凭感觉跳过 plan 直接写代码"**。这像传统瀑布的阶段门禁，但区别在于 artifact 是活的、AI 帮你维护。

#### (3) 3 个可选命令加强代码质量

clarify（澄清未明问题）、analyze（跨 artifact 一致性检查）、checklist（自定义质量校验清单）。这三个不是必须的，但在严格项目里强烈推荐，特别是 analyze——它是 Spec-Kit 防 spec 漂移的核心工具。

### 4.3 核心命令体系

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/948906fb109d4c198b5ca4baaca79fe2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Spec-Kit 总共 6 个核心命令 + 3 个可选命令，命令以 `/speckit.` 前缀触发。

#### (1) 6 个核心命令（必须走的闭环）

##### ① `/speckit.constitution`

写项目宪法，定义非协商原则。每个项目只写一次。

##### ② `/speckit.specify`

把需求转成 spec，输出 user story + acceptance criteria。这是 spec-as-source 的源头，所有后续 artifact 从这里派生。

##### ③ `/speckit.plan`

根据 spec + constitution 生成技术方案，包含技术栈选型、模块划分、数据流、关键技术决策。Plan 阶段 AI agent 可能根据 constitution 排除一些方案。

##### ④ `/speckit.tasks`

把 plan 拆成可执行的有序任务列表，任务按 user story 组织，标记并行可执行的任务和依赖关系。

##### ⑤ `/speckit.taskstoissues`

把任务列表转成 GitHub issues，方便项目管理。

##### ⑥ `/speckit.implement`

AI agent 按 tasks 执行代码生成。这是最后一步，前面 4 步都准备好了才能跑。

#### (2) 3 个可选命令（加强代码质量）

##### ① `/speckit.clarify`

在 specify 之后、plan 之前跑。AI agent 自动找出 spec 里的模糊点，反过来问问题，逼你回答，减少 plan 阶段返工。

##### ② `/speckit.analyze`

在 tasks 之后、implement 之前跑。跨 artifact 一致性检查：constitution 跟 spec 是否一致、spec 跟 plan 是否一致、plan 跟 tasks 是否一致。发现漂移立刻修正。

##### ③ `/speckit.checklist`

自定义质量校验清单，比如"测试覆盖率达标"、"所有公开 API 有文档"。项目定制需求时用。

### 4.4 典型工作流

跑一个 Spec-Kit 项目从开始到结束的标准流程：

##### ① 初始化项目

装 Specify CLI，跑 `specify init` 在项目目录下生成 `.specify/` 工作区，里面有 `memory/constitution.md`、`spec.md`、`plan.md`、`tasks.md` 等占位文件。选择主 AI agent（比如 Claude Code），Spec-Kit 会为这个 agent 生成对应的 slash command 配置文件。

##### ② 写 constitution

跑 `/speckit.constitution`，AI agent 引导你回答几个问题（项目类型、技术栈、非协商原则），生成 `constitution.md`。这一步写一次定下来，整个项目期间不再改。

##### ③ specify

跑 `/speckit.specify`，提供高层需求描述（口头说或者贴一份需求文档），AI agent 生成 `spec.md`，包含 user story、acceptance criteria、功能边界。这一步生成的 spec 通常 800 行左右，很厚但完整。

##### ④ clarify（可选但推荐）

跑 `/speckit.clarify`，AI agent 反过来问你几个问题（spec 里哪里没说清楚、边界在哪、特殊情况怎么处理）。你回答后 spec 被更新。

##### ⑤ plan

跑 `/speckit.plan`，AI agent 读 spec + constitution 生成 `plan.md`，包含技术栈选型、模块划分、关键技术决策。Plan 阶段会主动检查跟 constitution 的一致性，违反 constitution 的方案不会被采纳。

##### ⑥ tasks

跑 `/speckit.tasks`，AI agent 把 plan 拆成有序任务列表（典型一个项目几十到上百个 task），按 user story 分组，标记并行可执行的任务和依赖关系。

##### ⑦ analyze（可选但强烈推荐）

跑 `/speckit.analyze`，AI agent 检查 constitution、spec、plan、tasks 之间的一致性，发现漂移给出修正建议。这一步是只读的，AI agent 不会改文件，只会报告问题。

##### ⑧ implement

跑 `/speckit.implement`，AI agent 按 tasks 列表逐个执行代码生成。这是真正写代码的步骤。可以一次性跑完所有 tasks，也可以一个一个 task 跑（推荐后者，方便中途审查）。

##### ⑨ review + commit

每个 task 完成后人工 review 生成的代码，git commit。implement 阶段不是"AI 写完就完"，**人始终在 review 环节**。

### 4.5 适用场景和局限

Spec-Kit 适合的场景：

##### ① medium 到 large greenfield 项目

从零开发，工程量中到大，模块跨多个文件夹。

##### ② 需求清晰

上游有明确的需求文档或产品决策。

##### ③ AI agent 协作

用 Claude Code、Copilot、Cursor 等 AI agent 做主体开发。

##### ④ 方法论场景

团队学习 SDD 的工程方法论。

Spec-Kit 不适合的场景：

##### ① 小 feature、快速原型、单文件改动

流程开销大于收益。社区共识是 Spec-Kit 的 800 行 spec 对小增量过重。

##### ② 大型 brownfield 项目改造

legacy 代码上下文太复杂，LLM context limit 不够。即使 Spec-Kit 有 brownfield 扩展，效果不如 OpenSpec。

##### ③ 探索性研究项目

需求未定就跑 spec 会反复返工。

##### ④ 个人快速实验

6+3 个命令对一个人的小项目太重。

我的判断：**Spec-Kit 是"重武器"**。重在它强制完整流程、强在它产出结构化 artifact、缺点是流程开销大。用在合适的场景威力巨大，用在不合适的场景就是负担。这跟一个团队选择 Java Spring 还是选择 Node 是类似的判断——跟项目规模、团队成熟度、长期维护要求匹配。

## 5. OpenSpec：delta-spec 的轻武器

### 5.1 OpenSpec 是什么

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/377b10376bf501bac2fa310cb9228038_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

OpenSpec 是 Fission-AI 在 2026 年 1 月发布 1.0 的开源 SDD 工具。它的设计哲学跟 Spec-Kit 正好对立：**轻量、灵活、增量友好**。

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/e553cdee2191a5337ec1491a6fac65de_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-10-spec-kit-openspec-tutorial/e553cdee2191a5337ec1491a6fac65de_MD5.jpg
用途：展示 OpenSpec 的整体架构与工作流
内容：推测展示 OpenSpec 的 changes 流程、Propose→Apply→Archive 状态机，以及 delta-spec 合并到主 spec 的机制
-->

OpenSpec 用 Node.js 写的，要求 Node.js 20.19.0+。装好后用 `openspec init` 在项目里初始化 `openspec/` 工作区。

它支持 30+ AI 编程工具，包括 Claude Code、Codex、Cursor、Cline、Windsurf、Copilot、Kilo Code、Kiro、Pi 等。OpenSpec init 时会扫描项目里现有的 AI 工具配置目录（`.claude/`、`.cursor/` 等），自动预选检测到的工具。

### 5.2 设计哲学

OpenSpec 的设计哲学跟 Spec-Kit 对照着看最清晰，三个核心点。

#### (1) delta-spec 范式

这是 OpenSpec 最关键的设计。OpenSpec 不像 Spec-Kit 那样要求每次都写完整 spec，而是写 **delta（增量）spec**：只描述"这次改动相对于现有系统的差异"。

你可以把 delta 理解为 **spec 的 git diff**——不改写整份需求文档，只标记这次新增、修改、删除了哪些 requirement。Delta 用三个 marker 标记：

##### ① `ADDED Requirements`

这次新增的需求。

##### ② `MODIFIED Requirements`

这次修改的需求，要写明"之前是什么"。

##### ③ `REMOVED Requirements`

这次删除的需求。

这个设计带来一个关键能力：**两个并行的 change 可以同时修改同一份 spec 的不同 requirement，互不冲突**。archive 阶段 delta 被合并到主 spec。

#### (2) 3 态流程 Propose→Apply→Archive

OpenSpec 把每个 change 看成一个状态机：

##### ① Propose

写出 change 的 proposal、design、tasks、delta spec。change 还没被实施。

##### ② Apply

AI agent 按 tasks 执行代码生成。代码生成完成。

##### ③ Archive

把 change folder 移到 `openspec/changes/archive/`，把 delta 合并到 `openspec/specs/`（主 spec）。

每个 change 是独立 folder，多个 change 可以并行 propose 互不干扰，按需要的顺序 archive。

#### (3) brownfield-first

OpenSpec 官方文档明确写"fluid not rigid, iterative not waterfall, easy not complex, **brownfield-first**"。它假设项目已经有代码，需求是"在已有系统上加 X、改 Y、删 Z"，整个工作流围绕这个假设设计。

OpenSpec 1.0 的最新版本已经升级为 **action-based workflow**，意思是可以灵活地编辑任何 artifact，不强制走 Propose→Apply→Archive 的顺序。这是相对 Spec-Kit 4 阶段刚性流程的进一步轻量化。

### 5.3 核心命令体系

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/ff267ae7d40a68611d05edf98a23b6b1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

OpenSpec 命令以 `/opsx:` 前缀触发（OpenSpec 缩写）。

#### (1) core profile（默认启用）

##### ① `/opsx:propose <change-name>`

创建 change folder，包含 proposal.md、design.md、tasks.md、delta specs。

##### ② `/opsx:explore`

discuss with AI first，分析代码库、比较方案、画架构图。不产 artifact，单纯讨论。

##### ③ `/opsx:apply`

AI agent 按 tasks 执行代码生成。

##### ④ `/opsx:sync`

同步状态、刷新 artifact graph。

##### ⑤ `/opsx:archive`

merge delta 到 main spec，move change folder 到 archive。

#### (2) 扩展 profile（按需启用）

##### ① `/opsx:new`

propose 的另一种入口，对应 Spec-Kit 风格的多步骤流程。

##### ② `/opsx:continue`

一次产一个 artifact，方便 step by step 审查。

##### ③ `/opsx:ff`

fast-forward，一次产生所有 artifact。

##### ④ `/opsx:verify`

archive 前跑一致性检查。

##### ⑤ `/opsx:bulk-archive`

批量 archive 多个 change。

##### ⑥ `/opsx:onboard`

把现有项目接入 OpenSpec。

OpenSpec 还支持 **custom schemas**，可以定义自己的 artifact 类型（比如加一份 ADR 架构决策记录、加一份研究报告）。这是 OpenSpec 比 Spec-Kit 更灵活的一个体现。

### 5.4 典型工作流

跑一个 OpenSpec change 从开始到结束的标准流程：

##### ① 初始化项目

跑 `openspec init`，OpenSpec 扫描项目检测已有 AI 工具，让你选要启用哪些，生成 `openspec/specs/`、`openspec/changes/`、`openspec/AGENTS.md` 等。如果项目已有代码，可以跑 `/opsx:onboard` 让 OpenSpec 反向理解项目现状。

##### ② 写 project.md（如果是新项目）

这是 OpenSpec 项目的"世界观"，描述技术栈、整体架构、关键决策。AI 看这个文件理解项目背景。

##### ③ propose 一个 change

跑 `/opsx:propose add-dark-mode`，AI agent 在 `openspec/changes/add-dark-mode/` 下生成：

- `proposal.md`：为什么做这个 change、要改什么
- `design.md`：技术方案
- `tasks.md`：实现 checklist
- `specs/`：delta specs，包含 ADDED + MODIFIED + REMOVED 标记

##### ④ review proposal

人工读 proposal、design、tasks，跟 AI 讨论调整。OpenSpec 的 propose 阶段是设计协商，不是一次定终身。

##### ⑤ apply

跑 `/opsx:apply`，AI agent 按 tasks 逐个执行：

- `Add theme context provider` ✓
- `Create toggle component` ✓
- `Add CSS variables` ✓
- `Wire up localStorage` ✓

如果中途出问题，apply 可以从断点 resume，不用从头来。

##### ⑥ verify（可选）

跑 `/opsx:verify` 跨 artifact 一致性检查，给出 CRITICAL、WARNING、SUGGESTION 三档报告。不会阻塞 archive。

##### ⑦ archive

跑 `/opsx:archive`，把 change folder 移到 `openspec/changes/archive/<date>-<change-name>/`，把 delta 合并到 `openspec/specs/<domain>/spec.md`。从此这个 change 的需求成为主 spec 的一部分。

##### ⑧ 进入下一个 change

每个 change 是独立的，archive 后立即可以开始下一个。可以并行多个 change（不同 change folder 互不干扰）。

OpenSpec 的 spec 写法用 **RFC 2119 keywords**（MUST、SHALL、SHOULD、MAY）描述需求强度，用 **Given/When/Then** 描述场景。这跟 BDD（Behavior-Driven Development）的写法一致，方便每个 requirement 直接转测试用例。

### 5.5 适用场景和局限

OpenSpec 适合的场景：

##### ① brownfield 增量改造

在已有项目上加功能、改功能、删功能，delta-spec 范式天然适合。

##### ② 多 change 并行

团队多人同时做不同 feature，每个 change 独立 folder 不冲突。

##### ③ 快速迭代节奏

3 态流程比 Spec-Kit 4 阶段闭环更轻，迭代更快。

##### ④ 小 feature 也能上 SDD

因为流程轻，社区有人说"OpenSpec 让我连小 feature 也愿意走 SDD"。

##### ⑤ 跟现有 AI 工具集成深

30+ AI 工具支持，比 Spec-Kit 多一档。

OpenSpec 不适合的场景：

##### ① 0 到 1 大型新项目

缺乏 constitution 这种宪法级强约束，新项目容易跑偏。

##### ② 企业合规重的项目

3 态流程的灵活性变成了"没有强制门禁"，跨阶段的审计追溯不如 Spec-Kit。

##### ③ 需要完整方法论训练的团队

OpenSpec 的轻量化意味着团队成员可以"绕过"SDD 一些环节，对方法论学习不利。

我的判断：**OpenSpec 是"轻武器"**。轻在它流程灵活、轻在它 artifact 少、缺点是约束力不够。用在合适的场景体验流畅，用在不合适的场景容易失控。

OpenSpec 在 brownfield 上确实比 Spec-Kit 强很多。**delta-spec 范式是 Spec-Kit 不能模仿的差异化**。Spec-Kit 后来加了 brownfield 扩展，但 spec-as-source 的本质决定了它在 brownfield 上的工程开销永远大于 OpenSpec。

## 6. Spec-Kit vs OpenSpec：选型决策

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/0d8f2c71425702d91289e030736a04d0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 6.1 设计哲学正面对比

**Spec-Kit：spec-as-source**。spec 是源头，所有其他 artifact 从 spec 派生。spec 变了下游一切重新生成。哲学上接近"瀑布的现代化版本"，主张严格上游设计。

**OpenSpec：delta-spec**。spec 是会成长的，每次 change 只描述差异，archive 后合并到主 spec。哲学上接近"敏捷的工程化版本"，主张增量演进。

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/0eb87bd4505eea50fde16077ac035aff_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-10-spec-kit-openspec-tutorial/0eb87bd4505eea50fde16077ac035aff_MD5.jpg
用途：对比 spec-as-source 与 delta-spec 两种范式
内容：推测展示两种范式的工作方式对照——spec-as-source 从源头派生下游 artifact，delta-spec 用增量 diff 累积合并到主 spec
-->

两个哲学没有绝对对错，**匹配的项目类型不同**：greenfield 大项目用 spec-as-source 防跑偏，brownfield 增量项目用 delta-spec 防过度设计。

### 6.2 流程对比：刚性 vs 灵活

**Spec-Kit 4 阶段闭环**：specify→plan→tasks→implement，每个阶段都有正式 artifact，前一阶段输出是后一阶段输入。没有跨阶段 shortcut，没有跳过环节。

**OpenSpec 3 态流程**：Propose→Apply→Archive，每个 change 是独立状态机。Action-based 工作流（1.0 升级）允许灵活编辑任何 artifact，没有刚性顺序。

刚性 vs 灵活的取舍：**Spec-Kit 刚性带来一致性强、约束力强；OpenSpec 灵活带来迭代快、压力小**。

### 6.3 输出物对比：厚重 vs 轻量

Spec-Kit 的 constitution + spec + plan + tasks 完整下来，一个中等项目的 artifact 总量上千行。**完整但厚重**。

OpenSpec 的每个 change 是一份小 delta，archive 后合并到主 spec。主 spec 会成长，但单次 change 的负担小。**轻量但需要长期维护主 spec**。

约 3 倍的输出量差距。这是 Spec-Kit 重、OpenSpec 轻最直接的体现。

### 6.4 适用场景推荐

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/b8be2be8ec1e2dbb40f5d0c4a3791eb0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

不同项目类型推荐的工具：

##### ① greenfield 中大项目

用 **Spec-Kit**，spec-as-source 防跑偏。

##### ② brownfield 增量改造

用 **OpenSpec**，delta-spec 范式契合。

##### ③ 多人并行多 feature

用 **OpenSpec**，每个 change 独立 folder。

##### ④ 严格合规项目

用 **Spec-Kit**，强制门禁强制审计。

##### ⑤ 个人小工具

两个都不推荐，直接用提示词 + Claude Code 就够。

##### ⑥ 跨语言异构项目

用 **Spec-Kit**，constitution 适合复杂技术决策。

### 6.5 决策树

怎么选？给一份决策树：

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/7511baf67616d10fff455c3ad318ccfc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-v2-10-spec-kit-openspec-tutorial/7511baf67616d10fff455c3ad318ccfc_MD5.jpg
用途：Spec-Kit vs OpenSpec 选型决策树
内容：推测展示按 greenfield/brownfield、团队容忍度、任务颗粒度、项目持续时间等维度分支的工具选择流程图
-->

##### ① 项目是新的（greenfield）还是已有的（brownfield）？

- greenfield 且规模中大 → **Spec-Kit**
- greenfield 且规模小 → 不用 SDD，直接 Claude Code + 提示词
- brownfield 且增量改 → **OpenSpec**
- brownfield 且要大重构 → **Spec-Kit**（重构当作新项目处理）

##### ② 团队对方法论的容忍度？

- 团队需要强制方法论学习 → **Spec-Kit**
- 团队成熟、追求灵活 → **OpenSpec**

##### ③ 单次任务的颗粒度？

- 大颗粒度（多模块、多人协作） → **Spec-Kit**
- 小颗粒度（单 feature、个人改动） → **OpenSpec**

##### ④ 项目持续时间？

- 长期项目（半年+） → **Spec-Kit**（前期投入有长期回报）
- 短期项目（几周内） → **OpenSpec**（不上重型流程）

### 6.6 学习建议

**先选 Spec-Kit 学 SDD 范式，再切 OpenSpec 体验增量风格**。

Spec-Kit 的强制流程是最好的方法论训练场，掌握后再用 OpenSpec 就能理解什么时候该"轻"什么时候该"重"。反过来先学 OpenSpec 的风险是容易养成"轻流程"的习惯，回头再上 Spec-Kit 觉得繁琐。

## 7. 落地建议与总结

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/f304d1c15a3df8861b4292e06ff6b4f2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 7.1 一句话回顾

SDD 是 AI 编程从作坊到工程的关键一步。没有 SDD 的 AI 编程，跟没有版本控制的传统编程是同一个问题——能跑，但不可持续。

### 7.2 两个工具的画像

##### ① Spec-Kit

GitHub 出品，重流程，spec-as-source 纯粹形式，4 阶段闭环 + 6+3 个命令。适合 greenfield 中大项目。

##### ② OpenSpec

社区出品，轻流程，delta-spec 范式，3 态流程 + action-based 工作流。适合 brownfield 增量改造。

**两者不是替代关系，是互补关系**。理解两者设计哲学的差异、掌握适用场景的判断标准，是上手 SDD 的前提。

### 7.3 掌握范式比掌握工具更重要

工具会迭代，范式是持久的。

把 Spec-Kit 和 OpenSpec 这两个工具背后的设计哲学理解透了，将来再出新的 SDD 工具——Kiro、Tessl、Intent 这些都在快速演进——你能很快判断它在 SDD 谱系里的位置。
