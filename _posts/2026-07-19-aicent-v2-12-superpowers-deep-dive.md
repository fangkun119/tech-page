---
title: AI编程方法(2) 12：Superpowers深度介绍
author: fangkun119
date: 2026-07-19 12:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-v2-12-superpowers-deep-dive/cover.jpg
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
AI编程方法(2) 12：Superpowers深度介绍
aicent-v2-12-superpowers-deep-dive
-->

## 1. 为什么你需要关注 Superpowers

<img src="imgs/aicent-v2-12-superpowers-deep-dive/637820ed39025a38bf6cd48ce8a18d1b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 1.1 一个你熟悉的场景：AI 写代码很快，但很散

用过 Claude Code、Cursor、Codex 的人，多半遇到过同一种体验：AI 写代码很快，但很散。

用一个你熟悉的场景类比——这就像团队里招了一个聪明但没有工程纪律的新人。脑子快，上手快，但你不敢把重要模块交给他。

散的具体表现，你多半见过：

- 让它做一个功能，它直接写实现，不写测试。
- 一个函数塞十件事，职责不清。
- 不问就开写，搞错了再大改。
- 报错了用 try-except 包起来，不追根因。
- 改一处，悄悄弄坏另外两处。
- 这个窗口忘了上个窗口定下的约定。

<span style="color: red; font-weight: bold;">这些不是模型不够聪明的问题。</span>Claude、GPT、Gemini 在 benchmark 上都很强，但把它们放进真实工程项目，工程纪律还是新手水平。

这里有一句需要诚实说的让步：当然，随着模型越来越强，这些问题会越来越少。在 Superpowers 设计的那个时间点，AI 写代码就是存在这些问题，它是为了解决这些问题出现的。但到了今天，这些问题还是存在，而 Superpowers 的价值也不止这一层。

### 1.2 这不是模型问题，是工作流问题

有经验的工程师都知道一件事： **软件质量不来自写代码快，来自工程纪律** 。

写 spec 之前不动键盘。任务拆到几分钟一个。写测试之前不写实现。改完跑全套测试。没 review 不合并。这些纪律是几十年工程实践沉淀下来的，但 AI agent 默认不知道。

那怎么让 agent 也拥有这些纪律？有两条路。

- 一条是 **把纪律训进模型** ，改底座模型的训练目标。这件事只有 Anthropic、OpenAI 这种规模的团队做得了——相当于改招聘标准，只招天生守纪律的天才。
- 另一条是 **把纪律装进工作流** ，用一套机制约束 agent 的行为。这件事普通工程师就能做——相当于建流程制度，普通人按流程走也能产出合格品。

Superpowers 走的是第二条，而且在这条路上做得比谁都远。

### 1.3 20 万 stars 说明什么

Superpowers 2025 年 7 月建仓，上线后五个月破 10 万 stars，到 2026 年中超过 20 万，单月还能涨三万多。这个增长速度，在没有大公司市场预算撑着的开源项目里很罕见。

更能说明问题的是另一件事：2026 年 1 月，它被收进 Anthropic 官方的 Claude Code 插件市场。一个第三方个人项目能进官方市场，等于官方替它的质量背了书。

数字和官方认可加在一起，指向同一个判断： <span style="color: red; font-weight: bold;">AI agent 缺工程纪律，是真实痛点，不是伪需求。</span>

既然是工作流问题，那 Superpowers 到底提供了一套什么样的工作流？

## 2. Superpowers 到底是什么

<img src="imgs/aicent-v2-12-superpowers-deep-dive/0eea08dec56cc5e3d7146fab296e93ee_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 一句话定义

先给定义，再拆解。

<span style="color: red; font-weight: bold;">Superpowers 是一个 agentic skills 框架，同时是一套软件开发方法论。它把"严肃软件工程师该做的事"打包成 Skills，强制嵌进 AI coding agent 的工作流。</span>

<span style="color: red; font-weight: bold;">说人话：它就是给 AI agent 装上一套工程纪律。不是给你一本规范手册让你自己去读，是直接装进 agent，让它必须照着做。</span>

<img src="imgs/aicent-v2-12-superpowers-deep-dive/a23f98e897ead62862f4098a35a42808_MD5.webp" style="display: block; width: 800px;" alt="替换文字">

<!-- 图片注释
路径：imgs/aicent-v2-12-superpowers-deep-dive/a23f98e897ead62862f4098a35a42808_MD5.webp
用途：用一句话定义 + 5 组件视角双栏展示 Superpowers 的本质
内容描述：信息图，标题"Superpowers——AI agent 的工程纪律框架"。左栏"一句话定义"：agentic skills 框架 + 软件开发方法论，三个关键组件——skills 框架（可复用可扩展工作流单元）、方法论（spec 优先/TDD/YAGNI/subagent 协作/code review）、强制嵌入（不是"给你用"是"管住 agent"的约束）。底部标注 20 万+ GitHub Stars、支持 20+ 种 agent、永远跟最新模型一起进化。右栏"5 组件视角下，Superpowers 只动两个"：LLM（复用，不带自己的模型）、Tools（复用，约束使用方式不新增工具）、Memory（★ 改造，大型 Skills 库沉淀几十年工程经验）、Loop（★ 改造，强制结构化执行流程 brainstorm→spec→plan→execute→review）、Environment（复用，跨 agent 工作）。底部总结："只改 Memory 和 Loop，其他三个全复用——这是它能跨 20+ agent 安装、永远跟最新模型一起进化的原因"。
-->

它官方仓库（github.com/obra/superpowers）的自我描述就一句话：an agentic skills framework and software development methodology that works——一个真正能用的 agentic skills 框架兼软件开发方法论。

### 2.2 三个关键词逐个拆

这句话里三个词都要紧，逐个拆开看。

#### (1) agentic skills 框架

传统团队靠 SOP 手册、code review checklist、编码规范文档来约束人。Superpowers 靠 Skills 约束 agent。Skills 是可复用、可扩展的工作流单元——每一个 Skill 就是一段被结构化的工程经验，agent 遇到对应场景就自动激活。

#### (2) 软件开发方法论

它不只是工具，是一套完整的工程哲学。spec 优先、TDD、YAGNI、subagent 协作、code review，全在里面。这些不是它发明的，是几十年工程实践沉淀下来的精华，它只是把这些精华强制嵌进了 agent 的工作流。

#### (3) 强制嵌进 agent 的工作流

这是最关键的一个词。<span style="color: red; font-weight: bold;">它不是"给你用"的工具，是"管住 agent"的约束。</span>agent 想跳过某一步（比如不写测试直接实现），Skill 会拦住它。传统做法是靠人自觉遵守规范，Superpowers 是靠机制强制 agent 遵守。

### 2.3 它不是 SDD 工具，是 agent 的工程纪律框架

你可能会问：这跟 SDD 工具（Spec-Kit、OpenSpec）有什么区别？这个问题重要到要在这里先给一个判断。

<span style="color: red; font-weight: bold;">Superpowers 不是 SDD 工具，是 AI agent 的工程纪律框架。</span>

SDD 工具（Spec-Kit、OpenSpec）的定位是提供 spec 到 plan 到 tasks 的工作流——聚焦在"把需求变成规格"这一段。

Superpowers 的定位是让 agent 做任何事都遵守工程纪律，这里面包含 SDD，但远不止 SDD。它还套着 TDD、review、debugging、根因分析等一圈别的 Skill。

记住这个区分，它是理解 Superpowers 在工具栈里位置的钥匙。后面有一整章讲它跟 Spec-Kit、OpenSpec 的边界。

## 3. 核心机制：Skills 框架

<img src="imgs/aicent-v2-12-superpowers-deep-dive/b759a12e4ebe2a2fc9bc03d295efd910_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 3.1 Skill 是什么：给 LLM 看的工程经验

理解 Superpowers 的核心，是理解它的 Skills 系统。

先用大白话建立直觉：<span style="color: red; font-weight: bold;">Skill 就是把老司机的经验写成 agent 能读懂的规则</span>。传统团队里，老员工脑子里的经验靠口口相传、靠 code review 时指出问题来传递。Superpowers 把这些经验写成 SKILL.md 文件，agent 读到就能遵守。

这里要跟 Claude Code 自带的 Skills 区分一下：机制是同一套（都是 SKILL.md），但 Superpowers **提供的是一整套围绕工程纪律精心设计的 Skill 库，不是零散的几个** 。这就是差距所在。

<img src="imgs/aicent-v2-12-superpowers-deep-dive/71ac134ee7af13240cc5295c2adcbd4f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 图片注释
路径：imgs/aicent-v2-12-superpowers-deep-dive/71ac134ee7af13240cc5295c2adcbd4f_MD5.jpg
用途：可视化展示 Superpowers 核心 Skills 体系的构成和每个 Skill 的触发场景/动作/价值
内容描述：结构化网格图，标题"Superpowers 核心 Skills 体系"，副标题"每个 Skill 是 markdown 文件——遇到对应场景自动激活，不靠用户手动调用"。八个彩色 Skill 方框，每个包含触发场景/做/价值三部分：① brainstorming（蓝色，用户说"我想做 X"时触发，先问目标和约束不问技术实现）② writing-specs（橙色，头脑风暴结束后触发，把对话整理成 spec 分块让用户签字）③ writing-implementation-plans（蓝色，spec 签完后触发，拆成几分钟一个任务精确到文件路径）④ subagent-driven-development（红色，plan 签完后触发，每任务独立 subagent + worktree 执行）⑤ tdd（绿色，任何写实现时刻触发，先写测试看红再写实现看绿）⑥ debugging-systematically（橙色，遇到 bug 时触发，系统化定位根因不瞎猜）⑦ root-cause-analysis（紫色，修根因不修症状）⑧ writing-skills（深蓝色元 Skill，教 agent 自己造新 Skill，把书/博客/最佳实践提炼成 SKILL.md）。底部注释："writing-skills 是精妙之处——agent 读一本工程书，自动提炼出新 Skill，把知识工程自动化"。
-->

### 3.2 一个 Skill 长什么样

Superpowers 的每个 Skill 是一个 markdown 文件，结构大致是这样：

```markdown
---
name: brainstorming
description: 在写任何代码之前激活。当用户表达想做一个新功能、
  新项目、新改动时，先把需求和目标搞清楚，而不是直接动手。
---

# Brainstorming

## 什么时候用
用户说"我想做 X""帮我加个 Y 功能""我们需要一个 Z"——
任何还没想清楚就想动手的时刻。

## 流程
1. 不要立刻问技术细节（用什么数据库、什么框架）
2. 先问目标：这是给谁用的？要解决什么问题？成功长什么样？
3. 问约束：有没有合规要求？性能要求？时间限制？
4. 问边界：哪些明确不做（YAGNI）？
5. 把理解复述给用户确认，再进入写 spec 阶段

## 检查清单
- [ ] 我理解了用户真正要解决的问题，不只是表面需求
- [ ] 我问清楚了关键约束
- [ ] 我跟用户确认过理解一致

## 常见坑
- 跳过这一步直接问"用什么技术栈"——这是在帮用户实现一个
  他可能还没想清楚的方案
- 头脑风暴变成需求审讯，连珠炮问问题——要对话，不要质询
```

这是简化示意，但结构是真实的。重点： **Skill 不是代码，是给 LLM 看的指令** 。它告诉 LLM 遇到某类任务时走什么流程、做什么检查、避什么坑。一个 SKILL.md 就是一段被结构化过的工程经验。

### 3.3 内置的核心 Skills

Superpowers 内置 20 多个 Skill，最关键的几个：

1. **brainstorming** ，启动新功能时强制先头脑风暴，理解用户真正要做什么。
2. **writing-specs** ，把头脑风暴的结果落成 spec，分块给用户确认。
3. **writing-implementation-plans** ，把 spec 拆成清晰到新手都能照做的 plan，每个任务几分钟一块。
4. **subagent-driven-development** ，用 subagent 实现每个任务，完成后做两阶段 review。
5. **tdd** ，真正的红绿 TDD，测试先写，看到失败再写实现。
6. **debugging-systematically** ，有方法地 debug，不瞎试。
7. **root-cause-analysis** ，修根因不修症状。
8. 还有一个 **writing-skills** ，是教 agent 怎么写新 Skill 的元 Skill，后面单独讲。

### 3.4 Skill 怎么自动激活

这是 Superpowers 跟普通 prompt 模板的根本区别：<span style="color: red; font-weight: bold;">Skill 不靠用户手动调用，是 agent 自己识别场景后激活</span>。

机制是这样：每个 Skill 在 SKILL.md 的 description 里写了"什么时候用"。agent 启动时，把所有 Skill 的这段描述读进 context。用户说"我想加一个新功能"，agent 看到 brainstorming 的触发条件命中，自动启动这个流程。

打个传统类比：这就像 CI/CD 管道——你配好规则，代码一提交就自动触发构建和测试，不用手动跑脚本。Skill 也是配好触发条件，场景一出现就自动激活，用户不用记有哪些 Skill。

### 3.5 writing-skills：能自己长出新能力

<span style="color: red; font-weight: bold;">writing-skills 是 Superpowers 真正精妙的地方。</span>它 **教 agent 怎么自己造新 Skill** 。

流程大概是：你给 agent 一本编程书、一篇博客、一段团队最佳实践，它用 writing-skills 的流程读完，提炼里面可复用的模式，落成 SKILL.md 格式，然后验证用 subagent 跑一遍，看这个新 Skill 是不是真的能让 agent 遵守。

这等于用 TDD 测试 Skill 本身。作者 Jesse 描述过他怎么干这件事： **让 Claude 读一本经典工程书，提炼出一组 Skills，再用这些 Skills 反过来改进 Claude 自己的工作方式** 。这是把知识工程自动化的雏形。

## 4. 完整工作流：从一句话到合并

<img src="imgs/aicent-v2-12-superpowers-deep-dive/b4e09ec0a8d50b45f980887d82a1cf84_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把 Superpowers 串起来看一遍。

<img src="imgs/aicent-v2-12-superpowers-deep-dive/1203388ab308f715cf4eb59dbcd1ca51_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 图片注释
路径：imgs/aicent-v2-12-superpowers-deep-dive/1203388ab308f715cf4eb59dbcd1ca51_MD5.jpg
用途：可视化展示 Superpowers 的六阶段完整工作流，并对比原生 Claude Code 的差异
内容描述：工作流流程图，标题"Superpowers 完整工作流——从一句话到合并"。顶部六个彩色阶段方框横向排列：① Brainstorming（头脑风暴，问为什么做/给谁用/成功长什么样）② 写 Spec（分块展示需求，逐块用户签字）③ 写 Plan（拆到几分钟一个任务，精确到文件路径）④ Subagent 执行（独立 worktree 隔离，TDD 先红后绿）⑤ 两阶段 Review（spec 合规检查 + 代码质量检查）⑥ 收尾（全套测试绿，合并/PR/丢弃）。下半部分"VS"对比框：左侧原生 Claude Code（直接写 200 行，没 spec 没测试没 review，来回 10 轮，半小时出"看起来能用"，再花 1-2 小时修 bug）；右侧装了 Superpowers（头脑风暴问清目标，spec 逐块签字，plan 拆十几个任务，subagent TDD + 两阶段 review，1-2 小时出"真能用"代码）。底部总结："流程慢一点，产出真能用——总耗时差不多，产出质量和可维护性差很远"。
-->

### 4.1 从一句话开始

你打开 Claude Code（或 Cursor、Codex、Gemini CLI 任一），说：

> "我想给应用加一个 newsletter 订阅功能。"

没装 Superpowers 的 Claude Code 会立刻问几个技术问题：用什么数据库、前端什么框架、要不要双重确认，然后开写。

装了 Superpowers 的不一样。brainstorming 自动激活，它先问：这个 newsletter 是给现有用户还是开放注册？预期多少用户？发送频率？要不要支持退订？有没有合规要求（GDPR、CAN-SPAM）？

这一步不问"怎么实现"，问"为什么做"和"目标是什么"。

### 4.2 写 spec

头脑风暴几分钟后，writing-specs 激活。它把对话整理成 spec，分块展示："这是第一块，用户故事和验收标准，你看一下，没问题我们看下一块。"

你逐块确认。整份 spec 通常 5 到 20 块，每块短到一眼能看完。关键设计： **spec 必须你签字，agent 才能往下走** 。这强制了动手前先想清楚。

### 4.3 写 plan

spec 签完，writing-implementation-plans 激活。把 spec 拆成几分钟一个的任务（举例说明）：

- 任务一，在 src/models/subscriber.py 建 Subscriber model，字段 email、created\_at、confirmed\_at，加 pytest 测试覆盖验证逻辑。
- 任务二，在 src/api/newsletter.py 建 POST /subscribe，接收 email，返回 confirmation token。
- 任务三，在 tests 里写 happy path 和 invalid email 两个测试。

**每个任务带精确的文件路径、命令、预期代码** 。Jesse 形容这种 plan 要清晰到"一个有热情但品味差、判断力弱、没项目背景、还讨厌写测试的新手都能照着做"。这种颗粒度是 Superpowers 的核心创新之一。

### 4.4 subagent 执行加两阶段 review

plan 签完，subagent-driven-development 激活。每个任务交给一个独立 subagent，在独立 worktree 里干活，context 干净，只看到当前任务相关的东西。

你可以把 subagent 理解成你团队里的初级开发：你给清晰的任务描述，他独立干完，你严格 review。

subagent 读任务，写测试，看到测试失败，写实现，看到测试通过，提交。主 agent 接到完成信号，启动两阶段 review。

- 第一阶段 **查 spec 合规** ：这个提交真的实现了任务要求吗？有没有偷工减料？有没有动不该动的地方？
- 第二阶段 **查代码质量** ：风格、命名、复杂度、测试覆盖、潜在 bug。

两阶段都过才接受，否则打回让 subagent 改。这是借鉴传统大公司 code review 实践的精华——先验"做对了"（spec 合规），再验"做好了"（代码质量），顺序不能颠倒。

<span style="color: red; font-weight: bold;">这就是 agent as contractor 哲学的落地——把 subagent 当承包商，严格验收。</span>

### 4.5 收尾

所有任务做完，agent 跑全套测试确认绿，给你四个选项： **合并分支、建 GitHub PR、保留分支稍后处理、丢弃所有改动** 。然后清理 worktree。

整个流程从你说"我想加 newsletter"到代码合并，可能半小时到两小时。但产出的代码是 spec 驱动、TDD 写就、双重 review 过的。

### 4.6 跟"直接让 AI 写"的对比

原生 Claude Code 的流程：你说加个 newsletter 功能，agent 说好我开始写，写了两百行，说加完了你看看。你发现没验证、没确认、缺测试，又来回十轮才能用。

Superpowers 的流程：你说加个 newsletter，agent 头脑风暴问清楚目标，写 spec 逐块给你签字，写 plan 拆成十几个任务，subagent 逐个执行加两阶段 review，最后跑通全套测试问你要不要合并。

| | 原生 Claude Code | 装了 Superpowers |
|---|---|---|
| 写代码 | 半小时出"看起来能用" | 一两小时出"真的能用" |
| 修 bug | 再花一两小时修 | 基本不用修 |
| 总耗时 | 差不多 | 差不多 |
| 产出质量 | 工程纪律差 | spec 驱动、TDD、双重 review |

总耗时差不多，但产出质量和工程纪律差很远。

## 5. 背后的工程哲学

<img src="imgs/aicent-v2-12-superpowers-deep-dive/02f717ab890c3ae3341a6b8231aa5d3f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

工作流背后是工程哲学。哲学是理解"为什么这么设计"的钥匙，值得单独看。

<img src="imgs/aicent-v2-12-superpowers-deep-dive/df72c91d691a491f14de91ac98e7965e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 图片注释
路径：imgs/aicent-v2-12-superpowers-deep-dive/df72c91d691a491f14de91ac98e7965e_MD5.jpg
用途：可视化展示 Superpowers 背后的六大工程哲学原则
内容描述：3×2 网格信息图，标题"Superpowers 背后的工程哲学"。六个彩色色块分别展示：① Agent as Contractor（不把 agent 当同事，当承包商）② 两阶段 Review（spec 合规 + 代码质量，先验正确性再验质量）③ 真红绿 TDD（先写测试看到红，再写实现看到绿）④ YAGNI + DRY 写进 Skill（agent 想违反时被拦住）⑤ Worktree 隔离（用 git 管理人 + agent 协作）⑥ 慢就是快（总耗时差不多，半年后可维护性天壤之别）。底部总结："工具会过时，工程方法论不会"。
-->

### 5.1 Agent as contractor：把 agent 当承包商

第一条哲学是 **把 agent 当承包商，而不是同事** 。

这两种模式的区别，做过外包管理的人秒懂：

- **同事模式** ：你给个大方向，他自己想清楚怎么做，你信任他的判断。
- **承包商模式** ：你给清晰的 spec，他按 spec 做，你严格验收。

Superpowers 用承包商模式：不信任 agent 的判断力，给清晰的 spec，让它按 spec 做，然后严格 review。

我的看法是，很多人用 AI 编程不顺，根源就是把 agent 当同事，期待它有判断力。Superpowers 强制你把它当承包商，期待降下来，产出反而稳了。

### 5.2 两阶段 review

只查 spec 合规，代码合规但质量可能差。只查代码质量，代码质量好但可能跑偏。两阶段一起，既守住"做对了"又守住"做好了"。

这是借鉴大公司 code review 实践的精华，只是从"人审人"变成了"agent 审 agent + 人监督"。

### 5.3 真红绿 TDD

业界很多团队号称 TDD，实际是代码写完补测试。Superpowers 的 tdd Skill 强制先写测试、跑、看到失败（红）、写实现、跑、看到通过（绿）。

"看到失败"这一步是关键。不先看到失败，你不知道测试是真在测东西还是写废了。

### 5.4 YAGNI 和 DRY 写进 Skill

agent 有两个坏习惯：爱"贴心地"加一堆将来可能用得到的功能（违反 YAGNI），也爱在多处复制相似代码（违反 DRY）。

Superpowers 把这两条原则写进 Skills。agent 想这么干的时候，Skill 会拦住它。传统做法是靠人自觉遵守 YAGNI 和 DRY，Superpowers 是从设计层就强制 agent 遵守。

### 5.5 Worktree 隔离

subagent 都在独立 worktree 里干活，主分支干净，多个 subagent 并行不冲突，实验失败容易丢，review 过了才合并。

传统做法是用 git 管理人的协作。Superpowers 是用 git 管理人加 agent 的协作。

### 5.6 慢就是快

最后一条最反直觉： **流程看起来慢，总耗时不长** 。

传统流程半小时出代码加一两小时修。Superpowers 一两小时出代码基本不用修。总耗时差不多，但产出真能用，而且半年后回头看可维护性强得多。

<span style="color: red; font-weight: bold;">慢就是快。</span>

## 6. 用 5 组件抽象看它的设计

<img src="imgs/aicent-v2-12-superpowers-deep-dive/6ab3f103561d627198c1bed429ac8511_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 6.1 只改两个组件，其他全复用

专栏前面介绍过 5 组件抽象：一个 AI agent 由 LLM、Tools、Memory、Loop、Environment 五部分组成。用这个框架拆 Superpowers，会有一个关键发现。

#### (1) LLM（复用）

它不带自己的模型，装在你已有的 agent 上，用 agent 自带的 LLM。这是聪明之处：不绑定特定模型，新模型出来就跟着用，永远跟最新模型一起进化。

#### (2) Tools（复用）

它不增加新工具，依赖 agent 自带的读写文件、跑命令、调 API，但约束这些工具的使用方式——没 spec 签字不写实现，没测试不写实现，没 review 不合并。

它不给 agent 新能力，给 agent 新纪律。这跟 Hermes、OpenClaw 那类"加能力"的项目方向完全相反。两个方向都有价值，但定位不同。

#### (3) Memory（★ 改造，核心）

这是它最大的创新维度。它的 Memory 不是"记住跟用户的对话"，是"记住怎么做好软件工程"——一个大型 Skills 库，每个 Skill 是 markdown 格式的工程方法论。

传统类比：老员工脑子里的经验，走人就丢了；写成文档的团队知识库，谁都能查。Superpowers 的 Memory 是后者——结构化、可复用、可扩展。你能写自己的 Skill，通过 writing-skills 让 agent 自动提炼，越用越丰富。

#### (4) Loop（★ 改造，核心）

另一个核心创新。它的 Loop 不是普通 ReAct loop，是强制结构化流程：brainstorm 到 spec 到 plan 到 subagent execute 到 review 到收尾，每个阶段有明确的通过条件，没通过不进下一阶段。

传统 ReAct 是每一步 LLM 自由决定下一步。Superpowers 是大流程固定、LLM 在流程内填细节。打个比方，传统 ReAct 像自由发挥的演员，Superpowers 像有剧本和导演的演员——剧本给框架，演员填细节。

#### (5) Environment（复用）

跟 LLM 一样复用 agent 自带的。Claude Code 上就是本地仓库加终端，Cursor 上就是 IDE 加项目，它跨 Environment 工作。

拆完的关键发现是：<span style="color: red; font-weight: bold;">Superpowers 只改两个组件（Memory 和 Loop），其他三个全复用</span>。

### 6.2 为什么这个设计让它能跨 20+ agent

"只改两个"这个设计带来了三个深远影响：

- **可移植性极强** ：任何支持 Skills 的 agent 都能装。
- **跟 agent 演化解耦** ：模型、工具、环境升级都不影响它。
- **核心价值聚焦** ：只做一件事，给 agent 装工程纪律。

少做但做透，这是它能涨到 20 万 stars 的根本原因。

## 7. 跟 Spec-Kit、OpenSpec 的边界

<img src="imgs/aicent-v2-12-superpowers-deep-dive/d45bf32d2241b947581928c58799afca_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这是全文最关键的选型章。你心里真正的疑问是：已经有 Spec-Kit、OpenSpec 了，为什么还要 Superpowers。

<img src="imgs/aicent-v2-12-superpowers-deep-dive/16aa3b607da30bb088ebc0aaaa67ced3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 图片注释
路径：imgs/aicent-v2-12-superpowers-deep-dive/16aa3b607da30bb088ebc0aaaa67ced3_MD5.jpg
用途：对比 Spec-Kit、OpenSpec、Superpowers 三个工具的定位、范围、形态、核心价值，理清边界
内容描述：三栏对比信息图，标题"Superpowers vs Spec-Kit vs OpenSpec——边界在哪"。三个彩色面板分别展示：Spec-Kit（蓝色，新项目 greenfield SDD 工具，specify→plan→tasks→implement 四阶段闭环，CLI 形态，9 万+ stars）；OpenSpec（橙色，已有项目 brownfield 增量改造，propose→apply→archive 三态流程带 delta 追踪，CLI 形态）；Superpowers（深蓝，AI agent 工程纪律框架全覆盖，brainstorm+spec+plan+TDD+subagent+review+debug，Plugin 形态，20 万+ stars）。底部包含关系示意：OpenSpec + Spec-Kit ⊂ Superpowers（SDD 只是 Superpowers 内部的一个 Skill）。
-->

### 7.1 三个工具的本质差异

#### (1) Spec-Kit

GitHub 出的 SDD 工具，到 2026 年中 9 万多 stars，最新 v0.8.7，支持 30 多个 agent。 **定位是新项目 0 到 1 的 SDD 工具** 。范围是 specify 到 plan 到 tasks 到 implement 四阶段闭环，形态是 CLI，核心价值是让"从一句话需求到工业级规格"有标准流程。每个 Spec-Kit 工作流底下有一个 constitution，一份写着不可变工程原则的 markdown，对每次改动、每个会话都生效。

#### (2) OpenSpec

社区做的棕地 SDD 工具，装法是 npm install -g @fission-ai/openspec 然后 openspec init。 **定位是已有项目的增量改造** 。范围是 propose 到 apply 到 archive 三态流程，用 ADDED、MODIFIED、REMOVED 这种 delta 标记追踪相对已有功能改了什么。它比 Spec-Kit 轻——同样一个功能 Spec-Kit 可能产出八百行规格，OpenSpec 大约两百五十行，review 负担小很多。核心价值是让老项目改造也有规格化方法，而不是裸跑。

#### (3) Superpowers

Jesse 做的 agentic skills 框架，20 万 stars。 **定位是 AI agent 的工程纪律框架** 。范围比 SDD 宽得多，包含 brainstorm、spec、plan、TDD、subagent、review、debugging、根因分析等完整工程实践。形态是 plugin，装在已有 agent 上提供 Skills 系统。核心价值是让 agent 做任何任务都遵守严肃工程师的纪律。

### 7.2 一句话理清三者关系

**Spec-Kit 和 OpenSpec 是"做 SDD 的工具"，聚焦 spec 到 plan 到 tasks 的工作流** 。 **Superpowers 是"管 agent 纪律的框架"，它把 SDD 当成内部的一个 Skill，外面还套着 TDD、review、debugging 等一圈别的 Skill** 。

换个说法：如果把工具按覆盖范围排，OpenSpec 管 SDD 的增量改造，Spec-Kit 管 SDD 的完整闭环，Superpowers 管的是整个工程纪律，SDD 只是它的一部分。

<span style="color: red; font-weight: bold;">包含关系是这样的：OpenSpec（增量 SDD）+ Spec-Kit（完整 SDD）⊂ Superpowers（完整工程纪律）。</span>

### 7.3 什么场景用哪个

#### (1) 用 Spec-Kit

你做新项目 0 到 1，需求清晰，想要清晰的四阶段闭环。

#### (2) 用 OpenSpec

你做棕地改造，在已有项目上加功能，改动涉及多个文件多个模块，需要 delta 管理，又不想要 Spec-Kit 那么重的四阶段。

#### (3) 用 Superpowers

你想要全面的工程纪律，不只是 SDD，想让 agent 自动走 brainstorm、TDD、review、subagent 协作的完整流程，愿意接受慢一点换"产出质量高、不用反复修"，并且想跨多个 agent 用同一套工作流。

### 7.4 能不能一起用

能，但要小心边界。

Superpowers 内置的 writing-specs 跟 Spec-Kit 是重复的，选一个用。选 Superpowers 就用它的 spec 加 plan 流程；选 Spec-Kit 就绕开 Superpowers 的 writing-specs，但保留它的 TDD、review、debugging 这些别的 Skill。

理想组合是 Spec-Kit 或 OpenSpec 做 spec 加 plan，Superpowers 做 TDD 加 subagent execute 加 review。但这要手工调整 Skill 的激活规则，不是开箱即用的。

## 8. 跨 agent 支持

<img src="imgs/aicent-v2-12-superpowers-deep-dive/d54f03123d458b82a7c44ca02c8e60bb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 8.1 不绑定单一 agent 的设计哲学

**Superpowers 一个重要设计是不绑定单一 agent** 。到 2026 年中，它官方支持 Claude Code、Codex CLI 和 App、Cursor、Gemini CLI、OpenCode、GitHub Copilot CLI、Factory Droid，几乎覆盖了所有主流 coding agent。

Jesse 的设计哲学很清楚：押的是工程方法论本身，不是某个工具。理由有三个：

1. 工具半年一换，方法论几十年不变，绑死工具的话工具一死它就死。
2. 不同 agent 各有优势（Claude Code 上下文好、Cursor IDE 集成好、Codex 快），让它跨 agent 跑，用户能在不同场景用不同 agent 而工作流不变。
3. Skills 标准跨 agent 互通，生态才能做大。

### 8.2 各 agent 上的实际效果差异

但跨 agent 不是完美的，实际效果有差异：

- **Claude Code** 上支持最完整，所有 Skill 都能用。
- **Cursor、Codex** 上大部分能用，少数依赖 Claude 特性的效果打折。
- **Gemini CLI** 上基础 Skill 能用，subagent-driven-development 这类高级的还在适配。
- **OpenCode** 作为开源 agent 集成度反而不错，适配比某些官方工具更激进。

实际建议： **Superpowers 的最佳搭档仍然是 Claude Code，其他 agent 上能用但要接受效果打折** 。

## 9. 怎么开始用

看完原理，这里是可以立刻动手的部分。

### 9.1 安装

Claude Code 上，在会话里跑：

```
/plugin install superpowers@claude-plugins-official
```

注意后面的 @claude-plugins-official 不能省，这是它进了官方市场之后的标准安装方式。

其他 agent 各有各的装法。Codex 在 plugin marketplace 里搜 superpowers 安装。OpenCode 用它自己的 plugin 系统，即使你已经在 Claude Code 装了，在 OpenCode 上也要单独装一次。具体去官方仓库 github.com/obra/superpowers 看对应平台的说明。

### 9.2 第一个 demo

装完别急着试复杂场景。开个新会话，说"我想给这个项目加一个简单功能，一个显示当前时间的 CLI 命令"，然后看 Superpowers 自动激活哪些 Skill。

预期会看到：brainstorming 激活，问你为什么做、用什么格式、要不要支持时区；你回答后 writing-specs 激活展示 spec；签字后 writing-implementation-plans 给出两三个小任务；签字后 subagent 执行每个任务；完成后两阶段 review；跑测试通过，问你合并还是提 PR。

这个流程跑通，你就理解 Superpowers 了。

### 9.3 写自己的 Skill

熟悉之后你会想加自己的 Skill。最简单的方式是让 Superpowers 帮你写。跑"我想写一个 Skill，记录我团队的 code review 标准"，writing-skills 自动激活，问你什么时候用、流程是什么、检查清单包含什么、怎么验证。回答完它给你写一份 SKILL.md，放到对应目录就生效。

### 9.4 跟现有工作流对接

如果你已经在用 Spec-Kit、OpenSpec，几个建议：

- 不要立刻全切到 Superpowers，先在某个小项目里跑通完整流程体验差异。
- 学习它的 Skills 写法，即使不全用它的工作流，SKILL.md 模板也能借鉴。
- 重点关注 writing-skills 这个元能力，这是它最有启发性的设计。

## 10. 作者判断：有用但需要筛选

<img src="imgs/aicent-v2-12-superpowers-deep-dive/a21476019bd79e150e5aae572f448c68_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

讲完事实，给判断。

最核心的判断：<span style="color: red; font-weight: bold;">有用但需要筛选</span>。我的个人看法是，它大概率是过渡产物的工具——不是因为它不好，而是因为 AI 编程演化太快，今天的最优解明天可能就被新机制取代。但它代表的"给 agent 装工程纪律"这个方向，不会过时。

### 10.1 它的局限

诚实说它不完美的地方。

**慢** 。修一个 typo 这种小事，走 brainstorm 到 spec 到 plan 到 subagent 到 review 整套，明显 overkill。它适合中等以上复杂度的任务，小任务直接让 agent 改就行。而且它现在还不能自动判断哪个任务该走全流程、哪个该跳过，这个判断得靠你。

**重** 。对小项目过度工程化。个人小项目、prototype、一次性脚本，它太重了。它默认你做的是严肃软件工程，但有时你只是想快速验证一个想法，这种场景它反而碍事。

**加新 Skill 不容易进主仓库** 。Jesse 对加 Skill 很谨慎，社区 PR 接受率低。每个 Skill 要在所有支持的 agent 上验证、符合方法论、经过 TDD 验证，加一个不够好的 Skill 比不加更糟。实际意味着你主要用官方的 Skill，自己加的多半只在自己的 fork 里用。

**学习曲线** 。"自动激活"听起来简单，但你要适应不立刻写代码、接受 brainstorming 的"啰嗦"、接受多次 review 的"反复"、接受 subagent 工作的"看不见"。很多人第一次用觉得太慢不爽，坚持两三周才发现产出质量的差异。

### 10.2 它在 AI 编程演化里的位置

把 AI 编程工具放时间线上看：

- **2022-2023 Copilot 时代** ：AI 是代码补全工具，在你写的时候帮一把。
- **2024 Cursor、Claude Code 时代** ：AI 是编程助手，能完成完整任务，但要你不断引导。
- **2025 Superpowers、Hermes、SDD 工具时代** ：AI 开始像一个有结构化流程、有持久记忆、会按工程纪律工作的承包商。
- **2026** ：Multi-Agent 协作、自主软件工程师，还在长出来。

Superpowers 是这个演化的一个关键节点，它代表 AI 编程从"快但散"走向"慢但稳"的转折。

### 10.3 谁该用谁不该用

**该用的** ：严肃做软件工程、关心代码长期可维护性、团队较大需要统一工作流、项目复杂容错率低、愿意接受流程慢一点换质量。

**不该用的** ：个人小项目、prototype、一次性脚本、需要快速迭代的探索性工作、已经有非常成熟的自定义工作流不想被框架约束。

<span style="color: red; font-weight: bold;">最后一句话：工具会过时，工程方法论不会。</span>Spec-Kit 可能被替代，Superpowers 也可能被替代， **但它们代表的"AI 编程需要工程纪律"这件事不会变** 。
