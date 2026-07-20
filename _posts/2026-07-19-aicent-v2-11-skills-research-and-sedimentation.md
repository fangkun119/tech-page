---
title: AI编程方法(2) 11：AI编程实用Skills调研与沉淀
author: fangkun119
date: 2026-07-19 11:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-v2-11-skills-research-and-sedimentation/cover.jpg
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
AI编程方法(2) 11：AI编程实用Skills调研与沉淀
aicent-v2-11-skills-research-and-sedimentation
-->

## 1\. 为什么单独写这篇

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/46e764587567942a022dd6e79470907c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Skills 在 Anthropic 2025 年 10 月正式发布后，成了 AI 编程里被讨论最多的话题之一。社区里冒出几十个 Skills 仓库，一年时间从零积累到 1000+ 个 Skill，Cursor、Codex、Gemini CLI、Windsurf 都跟进支持。这个速度本身说明 Skills 解决了一个真实问题。

但你看完 Anthropic 官方文档，或者刷完几篇“Top 10 Must-Have Skills”的清单文章，会有几个困惑没解决。

1. **Skills 到底是什么** 。官方定义是“可重用的指令包”，听起来抽象。和 MCP、Subagents、Plugins、Commands 是什么关系？都是给 AI 加能力的，什么时候用哪个？这个问题没讲清楚，后面所有讨论都是空中楼阁。
2. **业界都在用什么 Skills** 。每个介绍文章推荐几个 Skill，但都是各推各的，没有全景视角。哪些 Skill 是工程师日常开发用得上的？哪些是行政办公型的？哪些是花活、装样子用的？你需要一个有评判力的整理，而不是堆砌清单。
3. **怎么沉淀自己的 Skills** 。看完别人的 Skill 知道大概长什么样，但回到自己的项目，不知道从哪些日常开发动作开始沉淀。什么样的操作值得变成 Skill？什么样的不值得？Skill 的描述怎么写才能被 AI 准确触发？这些是方法论层面的事，几乎没有现成资料讲透。

这份文档想把这三个困惑一起解决。前半部分讲业界全景和实用 Skills 清单，让你知道当下能直接用什么。后半部分讲怎么沉淀自己的 Skills，让你回到自己的项目能持续受益。

## 2\. Skills 是什么：从程序员视角理解

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/02ee479f1482e68fefc3ecacc6ad8e1a_MD5.webp" style="display: block; width: 800px;" alt="替换文字">

### 2.1 一句话定义

Skill 的本质，用程序员熟悉的话说，是 **流程固化加上工具调用** 。

流程固化的意思是：某个操作有固定的步骤、固定的输出、固定的质量要求，这些标准化的部分写下来，以后 AI 看到类似任务，自动按这套标准走，不用每次重新讨论步骤。

工具调用的意思是：某些操作需要调用外部工具（运行命令、读文件、查 API、生成图像），把这些工具的用法封装在 Skill 里，AI 触发 Skill 时自动用对的工具，不用每次解释。

两件事合起来： **Skill = SOP + 工具箱** 。SOP 是 Standard Operating Procedure（标准作业程序），告诉 AI 这件事按什么流程做。工具箱告诉 AI 这件事需要什么外部能力。

技术实现上， **一个 Skill 就是一个目录** ，核心文件叫 `SKILL.md` 。这个 markdown 文件由两部分组成：

- 开头一段 YAML frontmatter（name 加 description）
- 后面是 markdown 写的指令。

Anthropic 把这个格式开放成了标准，Claude、Cursor、Codex、Gemini CLI、Windsurf 都按这个标准走，Skill 跨工具通用。

### 2.2 Skills 和 MCP、Subagents、Plugins、Commands 的关系

很多人在 Skills 出来之后，看到一堆相邻概念混在一起搞不清，我们一个一个理顺。

1. **MCP（Model Context Protocol）** ：是工具调用的协议。把外部系统（数据库、API、服务）封装成 AI 可以调用的工具。MCP 服务器是独立运行的服务，AI 通过 MCP 协议和它通信。MCP 解决的是 **“AI 能调用什么外部能力”** 的问题。
2. **Skill** ：是一个指令包。告诉 AI 在特定场景下按什么流程做事。Skill 本身不提供外部能力，但可以指导 AI 怎么用现有的工具（包括 MCP）。Skill 解决的是 **“AI 在某类任务上按什么标准做”** 的问题。
3. **Subagent** ：是一个独立的 AI 实例。有自己的系统提示词、自己的工具权限、自己的对话历史。主 Claude 把任务分发给 Subagent 完成，然后收回结果。Subagent 解决的是 **“复杂任务怎么并行拆分”** 的问题。
4. **Plugin** ：是一个组合包。可以包含 Skills、命令、Subagents、MCP 配置，作为一个整体安装。Plugin 解决的是 **“怎么把一组相关能力打包分发”** 的问题。
5. **Command（斜杠命令）** ：是用户主动触发的指令。用户输入 `/deploy` 或 `/review` ，Claude 执行对应的指令。Skill 也可以做成命令形式，但 Skill 的核心特点是 **AI 自动触发** （根据 description 判断当前任务匹配哪个 Skill）。

这五个概念的关系大致是这样： **MCP 是工具供给侧，Skills 是行为标准，Subagents 是任务分发，Plugins 是打包分发，Commands 是显式入口** 。日常开发中最常用的是 Skills 和 MCP，Subagents 在复杂任务里偶尔用，Plugins 主要是分发用，Commands 是 Skills 的一种特殊触发方式。

需要记住的判断： **当你想固化一个流程或一种风格，用 Skill。当你想接入一个外部系统，用 MCP。当你想并行处理几件独立的事，用 Subagent。当你想把一套工具分发给别人，用 Plugin** 。

### 2.3 渐进式加载

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/b501c0a905272a6559c352e275addf50_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Skills 有一个工程上很巧妙的设计，叫 **渐进式加载** （progressive disclosure）。这个设计是 Skill 能在生态里规模化的关键。渐进式加载分三层。

- 第一层是 **元数据** ，只有 name 和 description，大约 100 个 token。AI 在会话开始时扫描所有可用 Skill 的元数据，知道有哪些 Skill 存在，但不加载详细内容。这一步几乎不花上下文。
- 第二层是 **SKILL.md 主体** ，大约几千 token。AI 判断当前任务匹配某个 Skill 后，加载这个 Skill 的完整 SKILL.md，按里面的指令执行。只有用到的 Skill 才占上下文。
- 第三层是 **辅助资源（scripts、references、examples），按需加载** 。SKILL.md 里如果引用了 `references/api.md` ，只有当 AI 真的需要看这个文件时才加载，平时不占上下文。

为什么这个设计重要？因为它让 **一个 AI 实例能同时挂载几十甚至几百个 Skill 而不撑爆上下文** 。如果每个 Skill 都把所有内容塞进系统提示词，挂十个 Skill 就把上下文吃完了，挂一百个根本不可能。渐进式加载让 Skill 的数量可以无限扩展，真正用的时候才占资源。

实操上，你需要记住的设计原则是： **SKILL.md 要尽量短** （几千 token 以内），把详细的参考、示例、长文档放到辅助文件里，通过引用按需加载。这是写好 Skill 的关键约束。

### 2.4 跨工具兼容

Skills 是 Anthropic 推动的，但格式是开放标准，生态里其他工具都跟进了。截止到 2026 年初，支持 Skills 标准的工具至少包括 Claude Code、Claude.ai、Claude API、OpenAI Codex、Cursor、Gemini CLI、Windsurf、Antigravity 这八个。

这个跨工具兼容对开发者意味着两件事。

第一，你沉淀的 Skill 是 **跨工具资产** 。你今天在 Claude Code 里写的 Skill，明天换成 Cursor、Codex 还能用，后天去新公司用别的 AI 工具大概率也能用。这是程序员能持续累积的个人技术资产，不是绑死在某个工具上的临时投入。

第二，业界共享的 Skill 库 **不需要为每个工具重做一份** 。GitHub 上的几个大 Skills 仓库（awesome-claude-skills、claude-skills-library 等）都是直接给所有支持工具用，不分版本。这降低了 Skills 生态的碎片化。

你理解了这一点后，沉淀 Skill 的动力会强很多。沉淀 Skill 不是给某个工具打工，是给自己积累带得走的工程资产。

## 3\. Skills 的两类核心价值

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/35afa708912f065310f9ea76f1eca590_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Skills 在日常开发中的价值，可以归到两个明确的类别。理解这两类价值，选 Skill 和写 Skill 的时候判断会更准。

### 3.1 Capability Uplift：让 AI 学会做没做过的事

第一类是 **能力提升** ，让 AI 学会做一些它本来做不了或做不好的事。

举几个例子。

- AI 默认不知道怎么处理 PDF 表单，装一个 pdf 处理 Skill 之后，它能读取 PDF、提取表格、填充字段，这是新增的能力。
- AI 默认不知道怎么连接到企业的 Workspace API，装一个 gws Skill 之后，它能读 Gmail、写 Sheets、建日程，这是新增的能力。
- AI 默认不知道怎么爬取网页内容，装一个 Firecrawl Skill 之后，它能抓取网页、提取结构化数据、做研究，这是新增的能力。

能力提升类 Skill 的特点是 **配套了外部工具或专门知识** 。Skill 内容里有具体的 API 调用方法、命令行用法、库的导入方式，这些是 AI 训练数据里可能没有或者过时的内容。

这一类 Skill 的判断标准是： **装上之后，AI 能做之前做不了的事** 。如果只是让 AI 风格变了，不算这一类。

### 3.2 Encoded Preference：让 AI 按你的方式做事

第二类是 **偏好编码** ，把你或你团队的特定做事方式编码进 Skill，让 AI 按这套方式执行，而不是按 AI 默认的“最常见做法”。

举几个例子。

- AI 默认写前端会用 Inter、Roboto 这种最常见的字体，出来的设计都“AI 味十足”。装一个 frontend-design Skill 之后，AI 在写代码前先确定美学方向（极简、复古、Brutalist 等），用有辨识度的字体搭配，输出明显不一样。
- AI 默认写 React 不一定考虑性能，装一个 React Best Practices Skill 之后，AI 主动关注 memoization、列表 key、状态提升等性能点。AI 默认写 Ruby 是通用风格，装一个 DHH 风格 Skill 之后，AI 按 37signals 的风格写（REST 纯粹性、瘦控制器、Hotwire 模式），代码风格一致。

偏好编码类 Skill 的特点是 **没有外部工具，纯文字指令** 。它不给 AI 加新能力，只是约束 AI 的选择偏好。

这一类 Skill 的判断标准是： **装上之后，AI 做事的方式变了，但能做的事没变** 。

### 3.3 两类价值在日常开发中怎么混搭

实际工作中，这两类 Skill 经常一起用。

举一个真实场景。你做 React 项目，装了三个 Skill：

- Frontend Design（偏好编码，管美学方向）
- React Best Practices（偏好编码，管性能习惯）
- Webapp Testing（能力提升，用 Playwright 做 E2E 测试）。

Claude 写组件时按设计 Skill 选美学方向，按 React Skill 写性能友好的代码，写完之后用测试 Skill 跑端到端验证。三个 Skill 不冲突，叠加效果远大于单个用。

判断装多少 Skill 的原则是 **按场景配齐，不按热门堆砌** 。如果你今天的工作场景是 React 前端开发，装好这三个就够了。如果换到 Java 后端开发，前面三个用不上，换装 Java 风格、JUnit、Spring Boot 之类的 Skill。装多了没用的 Skill 不会让 AI 变笨，但会让你心智混乱，不知道当前哪些在生效。

## 4\. 业界 Skills 全景：调研结果

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/12bfc67570714da5c9979160ac9b4841_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

到 2026 年初，Skills 生态已经形成几个清晰的层级。你了解这个全景，知道去哪里找好的 Skill，以及哪些是噪音哪些是信号。  
<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/b299f5dac9b80bd311573471a7958f73_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 4.1 Anthropic 官方 Skills

Anthropic 自己维护一个官方仓库 [anthropics/skills](https://github.com/anthropics/skills) （截至 2026 年初已经积累 121k star、14k fork）。这是 Skills 生态的起点，质量稳定、维护持续、文档完整。

官方 Skills 覆盖几个核心场景。

- 文档处理类（docx、pdf、pptx、xlsx）是最早发布的几个，Claude 默认就能用，用来生成 Word、PDF、PowerPoint、Excel。
- 开发类有 mcp-builder（指导构建 MCP 服务器）、webapp-testing（用 Playwright 测 Web 应用）、skill-creator（用来创建新 Skill 本身）。
- 前端类有 frontend-design（避免 AI slop 美学）、brand-guidelines（套用 Anthropic 品牌色）。
- 生成类有 algorithmic-art（p5.js 算法艺术）、canvas-design（代码生成视觉设计）。

给你的实操建议： **官方 Skills 是入门首选，装上看一遍，理解 SKILL.md 的标准写法** 。skill-creator 这个尤其重要，它能引导你创建自己的 Skill，是写新 Skill 时的脚手架。

### 4.2 社区高 star 的 Skills 库

社区里几个仓库非常活跃，值得关注。

1. [**obra/superpowers**](https://github.com/obra/superpowers) （作者 Jesse Vincent）是 Claude Code 早期最有影响力的 Skill 库之一，核心是 20 多个经过实战检验的 Skill，覆盖 TDD、调试、协作模式、规划方法。这个库的特点是每个 Skill 都有实战背景，不是为了凑数。
2. [**alirezarezvani/claude-skills**](https://github.com/alirezarezvani/claude-skills) 在 2026 年初已经累积 268 个 Skill、5200+ star，可能是当下最全的开源 Skills 库。覆盖工程、DevOps、营销、合规、C-level 角色，12 个 AI 工具通用。规模大，质量参差不齐，需要挑着用。
3. [**ComposioHQ/awesome-claude-skills**](https://github.com/ComposioHQ/awesome-claude-skills) 是 Composio 维护的精选清单，1000+ Skill 资源，带评分和分类。质量整体偏高，但有些条目是 Composio 自家产品的引流。
4. [**travisvn/awesome-claude-skills**](https://github.com/travisvn/awesome-claude-skills) 是另一个 awesome 系列清单，偏轻量，适合快速浏览。

**Garry Tan 的** [**GStack**](https://github.com/garrytan/gstack) (截至 2026 年初 20k+ star) 是 YC 总裁 Garry Tan 个人维护的 Skill 库，把 Claude Code 变成完整的 AI 工程团队，覆盖 office hours、设计、code review、QA、浏览器测试。这个库因为 Garry Tan 的影响力被广泛讨论，实际质量不错，但偏 startup 场景。

给你的实操建议： **先看 obra/superpowers，因为是实战导向。再看 awesome 系列清单，挑分类感兴趣的看。不要试图把这些库全装上，挑你日常工作场景需要的就行** 。

### 4.3 单个明星 Skill 的赏析

有几个单独的 Skill 在生态里特别知名，值得专门研究。

1. [**frontend-design**](https://github.com/anthropics/skills/tree/main/skills/frontend-design) （Anthropic 官方）是 Skill 生态里安装量最大的，到 2026 年 3 月超过 277000 次安装。它解决的问题特别明确：让 AI 不再写“AI slop”风格的前端。具体做法是要求 AI 在写代码前先确定美学方向（极简主义、复古未来风、Brutalist、有机自然风等），禁用 Inter、Roboto、Arial 等高频字体，强制有辨识度的字体搭配。你可以装上感受一下“AI 写出来的设计能多大程度上不像 AI”。
2. **Superpowers** （obra）是一组核心 Skill 的集合，包含 TDD、调试、规划等几个基础工程技能。它的设计哲学是“让 AI 像一个有经验的工程师那样工作”，不是只完成任务，而是按工程纪律完成任务。
3. [**agent-browser**](https://github.com/vercel-labs/agent-browser) （由 Vercel Labs 维护）是浏览器自动化的代表作，GitHub 14000 star。它让 AI 能控制浏览器，带 ref-based 元素定位、网络拦截、隔离 session、视频流回放等能力。日常做 E2E 测试、网页爬取、UI 自动化非常好用。
4. [**Composio**](https://github.com/ComposioHQ/composio) （ComposioHQ）严格说是平台不是 Skill，但通过 Skill 接口提供 850+ SaaS 应用的集成，覆盖 OAuth、凭证、动作模式。一个 Skill 调用 Composio 等于连上几百个外部应用。
5. [**skill-creator**](https://github.com/anthropics/skills/tree/main/skills/skill-creator) （Anthropic 官方）是元 Skill，用来创建新 Skill。它会引导你定义 schema、写指令、设置测试。你第一次写 Skill 时强烈建议用它，效率比从零写高得多。

给你的实操建议： **这五个 Skill 选 2 到 3 个，装上日常用一段时间，观察 AI 的行为变化。这是理解 Skill 价值最直接的方式，比读 100 篇文章都管用** 。

## 5\. 实用 Skills 清单：按开发场景分类

这一章是文档的核心之一。我们按工程师日常开发会遇到的场景，把业界值得参考的 Skill 整理出来，每个分类挑 2 到 3 个代表性的，给你一个可以直接对照查找的清单。

### 5.1 代码生成与重构类

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/f367b6721a37e720f265fdc79b7f95c7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这类 Skill 在 AI 写代码的主战场，目标是让 AI 写出符合特定风格、特定架构的代码。

- **software-architecture Skill** 实现 Clean Architecture、SOLID 原则、综合软件设计最佳实践。AI 在写代码时主动按这些原则组织代码，而不是堆砌方法。适合做长期项目，代码质量要求高的团队。
- **DHH 风格 Ruby Skill** 是个特别的例子，把 DHH（Rails 创始人）的 37signals 风格编码进 Skill。REST 纯粹性、胖模型、瘦控制器、Hotwire 模式、Current attributes、清晰优先的哲学。Ruby 团队装上之后，AI 写出来的代码风格特别一致。
- **subagent-driven-development Skill** 通过 Subagent 调度独立任务，在每个迭代之间设置 code review checkpoint，做快速可控的开发。适合复杂任务的拆分执行。

代码生成类 Skill 的核心价值是 **风格一致性和架构纪律** 。AI 默认会写出“通用风格”的代码，这个风格不一定符合你的项目。装上风格类 Skill，AI 写出来的代码统一，review 负担降低。

实战建议： **这类 Skill 不要装太多，挑一到两个匹配当前项目技术栈的就够** 。

1. Java 项目装 software-architecture（架构原则）加一个针对项目的代码风格 Skill。
2. Ruby 项目装 DHH 风格 Skill。
3. 如果团队有自己的架构风格（比如 DDD、六边形架构、整洁架构），把团队的风格沉淀成项目级 Skill，效果远超装一堆社区 Skill。

装上之后注意观察 AI 在哪些场景下“按风格写”了、哪些没有，把没有的场景在 SKILL.md 里补例子。

### 5.2 测试类

测试类 Skill 是工程师用得最多的几类之一。

- **Build （TDD） Skill** 是用 TDD 流程写代码的标准模板。AI 先写测试看着失败，再写代码让测试通过，再重构。这个流程逼着 AI 思考清楚再写代码，避免“看似对其实错”的代码。obra/superpowers 里有一个高质量的 TDD Skill，实战已经验证。
- [**webapp-testing**](https://github.com/anthropics/skills/tree/main/skills/webapp-testing) **Skill** （Anthropic 官方）用 Playwright 做 Web 应用的 UI 测试。装上之后 AI 能写 E2E 测试代码，跑测试看结果，根据失败修代码。
- **JUnit Test Generator Skill** 是 Java 生态的代表，针对 Spring Boot 项目自动生成 JUnit 测试。模板化的部分（setup、teardown、mock 数据）由 Skill 标准化，AI 只需要补业务断言。

测试类 Skill 的价值在 **测试质量稳定** 。AI 没有 Skill 时写的测试经常凑覆盖率，有 Skill 之后写的测试结构正规、关注关键路径。

实战建议： **测试类 Skill 是 AI 编程里 ROI 最高的几类之一，几乎所有项目都该装** 。

1. TDD Skill 适合习惯 TDD 的团队，逼着 AI 先写测试。
2. 如果项目用 BDD，装 cucumber 或 gherkin 相关的 Skill。
3. 前端项目装 webapp-testing
4. 后端项目装语言对应的测试模板 Skill。

测试 Skill 装上后，要定期检查 AI 写出来的测试是不是真有保护作用，凑覆盖率的测试要及时让 AI 重写。

### 5.3 代码 Review 类

代码 Review 是日常开发里 AI 越来越能干的事，有几个出色的 Skill。

- **code-review Skill** （obra/superpowers 中的）：按工程师的 review 思路审视代码，关注边界条件、错误处理、命名一致、抽象合理性。比 lint 工具看得深，比人手 review 快。
- **Garry Tan 的 GStack code review Skill** ：偏 startup 视角，重点关注代码能不能上线、有没有明显风险、是不是过度工程。适合早期产品的快速迭代节奏。
- **security-audit Skill** 专注安全维度，扫描代码里的常见安全问题（SQL 注入、XSS、密钥泄露、不安全的反序列化等）。这一类应该在所有 PR 上自动跑，作为安全门禁。

Code Review 类 Skill 的价值在 **review 不再是瓶颈** 。每个 PR 自动跑一遍 review Skill，人再做最终判断，review 速度快质量高。

实战建议：把 code-review Skill 和 security-audit Skill 组合用，在 PR 流程里两个都跑。

1. code-review 关注代码质量
2. security-audit 关注安全风险，两个维度互补。
3. CI 流水线里可以做成自动跑：PR 创建后自动调用 Claude 配 review Skill，产出 review comment 贴到 PR 上，真人 reviewer 看到 AI 的初审结果再做最终判断。

这套流程下来，真人花在 review 上的时间能降一半以上，质量还更高（AI 不会漏掉边界条件、不会忘记检查安全）。

### 5.4 文档与注释类

文档类 Skill 不直接产出代码，但产出对项目长期价值很大。

- **Document Collaboration Skill** 是结构化的文档协同撰写流程。分三个阶段推进：上下文收集、迭代打磨、读者测试。维护文档结构、交叉引用、术语一致性。适合 PRD、设计文档、RFC 这种严谨长文档。
- **API Docs Generator Skill** 自动生成 OpenAPI、Swagger、README 里的 API 章节。从代码注释、接口定义提取信息，生成完整 API 文档，保证文档和代码同步。
- **Javadoc Generator Skill** 是 Java 生态的标配，根据代码自动生成 Javadoc 注释，符合标准 Javadoc 规范。AI 默认写的 Javadoc 经常不规范，用 Skill 标准化之后质量稳定。
- **Changelog Generator Skill** 从 git commits 自动生成用户视角的 changelog。把“技术 commit”翻译成“用户能理解的发布说明”。适合 SaaS 产品发版时用。

文档类 Skill 的价值在 **文档不再是项目的负担** 。AI 帮你写第一版，人 review 调整，文档跟着代码一起更新。

实战建议： **文档 Skill 是被低估的一类。很多团队的文档质量差，本质是“没人愿意写”** 。

1. 用 AI 加文档 Skill，AI 写第一版，人花十分钟改一改，文档质量立刻起来。
2. Changelog Generator Skill 配合 git tag，每次发版自动出 changelog，运营或 PM 看到的发布说明立刻可用。
3. API Docs Generator Skill 配合 CI，代码合并后自动更新 API 文档，文档和代码永远同步，不再有“文档过期”的问题。

### 5.5 Git 与提交类

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/f6020af3fe9243fe9c6ad996c91d6190_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Git 类 Skill 是日常开发的高频场景。

- **commit-message-generator Skill** 根据 staged diff 自动生成 Conventional Commits 格式的提交信息。fix、feat、chore、docs 等类型自动判断，scope 自动从改动文件推断。AI 写 commit message 默认是流水账，Skill 标准化之后规范统一，适合配合自动 changelog 用。
- **branch-manager Skill** 标准化分支命名（feat/、fix/、chore/ 前缀加任务 ID），管理分支生命周期（开发分支、合并到 main、清理已合并分支）。团队协作时分支规范很重要，Skill 让规范自动执行。
- **git-workflow Skill** 综合了几个常见 git 操作流程：rebase 整理 commit、cherry-pick 选择性合并、revert 安全回滚、resolve conflict 标准流程。AI 在做 git 操作时经常出意外（强推、丢 commit），Skill 把这些操作的安全姿势编码进去。

Git 类 Skill 的价值在 **避免 AI 做 git 操作时翻车** 。git 操作出错代价高（丢代码、改历史、强推覆盖别人工作），Skill 把这些操作的安全栏杆固化下来。

实战建议： **commit-message-generator Skill 是几乎每个项目都该装的，把 git commit 规范化的成本几乎为零（装上 Skill，以后让 AI 提交时自动按 Conventional Commits 写）** 。规范的 commit message 配合 changelog generator 形成自动化链路：每次提交自动规范、每次发版自动出 changelog，运维成本极低。Git 类 Skill 在 SKILL.md 里要明确禁止某些危险操作（force push、删除分支、改历史），这条防护是 git Skill 的核心价值。

### 5.6 调试与排错类

调试是 AI 编程里特别能体现 Skill 价值的场景。

- **Debug Skill** （obra/superpowers 中的）按工程师的调试套路工作：复现问题、定位范围、提出假设、验证假设、循环逼近。AI 没有 Skill 时调试经常乱猜，有 Skill 之后调试动作有章法。
- **stack-trace-analyzer Skill** 专门解析报错堆栈，从堆栈里找出真正的根因（往往不是堆栈顶部的那一行），关联到代码、给出修复建议。
- **log-analyzer Skill** 处理大段日志（几千到几万行），提取关键错误、时间序列、异常模式，辅助排查问题。

调试类 Skill 的价值在 **调试不再靠运气** 。AI 拿着标准化的调试套路，大概率比人快（因为 AI 不会忘记某个步骤），小概率比人慢（因为可能在某个步骤反复）。

实战建议： **Debug Skill 在新人入职时特别有用，新人不熟悉项目的调试套路，装上 Skill 后 AI 引导新人按套路走** 。log-analyzer Skill 配合云上日志服务（ELK、Loki、Datadog）用，AI 直接拉日志、分析、定位，几分钟做完工程师手动要几十分钟的事。stack-trace-analyzer Skill 配合错误监控（Sentry、Bugsnag）用，新报错出现后 AI 自动分析根因，运维负担降下来。

### 5.7 部署与 CI/CD 类

部署相关的 Skill 是工程化的关键部分。

- **Dockerfile Skill** 写多阶段 Dockerfile 的标准模板。基础镜像选择、依赖安装、缓存层优化、安全 user、HEALTHCHECK 配置。AI 默认写的 Dockerfile 经常不优化镜像大小，Skill 标准化之后镜像小、构建快。
- **Kubernetes yaml Skill** 写 Deployment、Service、ConfigMap、Secret、HPA、PDB 等资源的标准模板。资源限制、健康检查、滚动升级策略、PDB 配置等容易出错的细节都标准化。
- **GitHub Actions Skill** 写 CI/CD 流水线的标准模板。build 阶段、test 阶段、deploy 阶段、artifact 上传、secret 注入等环节标准化。Github Actions 的 yaml 格式很容易写错，Skill 把常见模式固化下来。
- **aws-skills Skill** 集合了 AWS 开发的最佳实践：CDK 用法、成本优化、Serverless、事件驱动架构。AWS 生态太大，有这种 Skill 兜底，AI 不容易在某个服务上跑偏。

部署类 Skill 的价值在 **基础设施代码标准化** 。Dockerfile 和 K8s yaml 这些“看起来简单实际坑多”的配置文件，Skill 兜住底，出问题概率低。

实战建议： **部署 Skill 是 DevOps 转型的关键工具。传统上 DevOps 是稀缺角色，一个团队就一两个人懂部署** 。装上 Skill 之后，普通开发也能用 AI 写出生产级的 Dockerfile 和 K8s yaml，DevOps 角色更聚焦在架构设计和故障处理，不用一直被“帮我写个 Dockerfile”打断。GitHub Actions Skill 配合项目模板，新项目几分钟内有完整的 CI/CD 流水线。

### 5.8 数据库与迁移类

数据库类 Skill 是另一个 AI 经常出错的领域。

- **db-migrator Skill** 为 PostgreSQL、MySQL、SQLite 创建可逆迁移。分析当前 schema、确定改动、生成迁移文件、验证安全性、生成回滚方案。AI 默认写的迁移容易丢数据（比如直接 drop column），Skill 强制可逆和安全检查。
- **schema-design Skill** 帮你设计数据库 schema，考虑范式、索引、外键、性能。生成 ER 图和 schema DDL。
- **database-designer Skill** 偏综合，从业务需求到 schema 设计全程指导，适合做新项目的数据库设计。

数据库类 Skill 的价值在 **避免数据丢失** 。数据库改动一旦出错代价极高（数据丢失不可逆），Skill 把“先验证再执行”、“先生成回滚再执行”这些纪律固化下来。

实战建议： **db-migrator Skill 是数据库项目的标配，装上之后 AI 写迁移自动可逆** 。schema-design Skill 在新项目初期用，AI 配合做 schema 设计，产出 ER 图和 DDL，人 review。生产数据库的操作建议在 Skill 里加入“必须先在测试环境验证”的硬约束，AI 不会绕过。这类 Skill 特别适合配合 Plan Mode 用：重要的数据库改动先出 plan，人确认后再执行，降低出错概率。

### 5.9 前端设计类

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/81dc23c4fe7b604242c3b083f153ac3c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前端类 Skill 是 Skill 生态里最热闹的一块，可能是因为 AI 默认写的前端实在太“AI 味”。

- **frontend-design Skill** （Anthropic 官方）前面讲过，装机量最大。让 AI 在写代码前先选美学方向，用有辨识度的字体，做有性格的设计。
- **React Best Practices Skill** 让 AI 写 React 时主动关注 memoization、列表 key、状态提升、useEffect 依赖、Context vs prop drilling 等性能和可维护性问题。
- **Tailwind Component Skill** 标准化 Tailwind 类的使用模式，避免 AI 写出“50 个 class 拼起来的丑陋元素”。
- **brand-guidelines Skill** （Anthropic 官方）套用品牌色和字体，适合做品牌一致的网站、文档、艺术作品。

前端设计类 Skill 的价值在 **告别 AI slop** 。AI 默认前端长得都一样，Skill 装上能跳出这个模板。

实战建议： **frontend-design Skill 是前端开发者必装的，装上立刻能感受到 AI 输出的视觉差别** 。配合 React Best Practices Skill 一起用，既有美感又有性能。如果团队有自己的设计系统（Material UI、Ant Design、自研组件库），沉淀一个项目级 Skill 编码设计系统的用法，AI 写组件时自动按设计系统走，不会自由发挥。 [brand-guidelines](https://github.com/anthropics/skills/tree/main/skills/brand-guidelines) Skill 适合做品牌一致性强的项目（企业官网、SaaS 产品），装上之后所有界面自动符合品牌。

### 5.10 API 集成与 MCP 构建类

把 AI 接入外部系统是日常需求，这类 Skill 帮你做接入。

- [**mcp-builder**](https://github.com/anthropics/skills/tree/main/skills/mcp-builder) **Skill** （Anthropic 官方）指导你构建 MCP 服务器。覆盖 API 深度研究、Schema 设计、工具实现、评估测试的完整四阶段流程。TypeScript + Streamable HTTP 技术栈，带 Python 和 TypeScript 参考实现。装上之后 AI 写 MCP server 不会跑偏。
- **API Builder Skill** 帮你设计 RESTful API：资源建模、URL 命名、HTTP 方法选择、错误处理、版本管理。生成 OpenAPI 文档和 mock server。
- **GraphQL Schema Designer Skill** GraphQL 的对应版本，负责 GraphQL schema 设计。
- **Composio Skill** 通过 Composio 接入 850+ SaaS 应用。装上之后 AI 能直接读 Gmail、写 Sheets、建 Linear ticket、发 Slack 消息等。

API 集成类 Skill 的价值在 **AI 真正能“操作”系统，不只是写代码** 。一旦 AI 能调外部 API、操作 SaaS，工作流的自动化空间打开。

实战建议： **mcp-builder Skill 是 MCP 生态的关键 Skill，如果你打算给自己的产品做 MCP 集成，装上这个 Skill 让 AI 引导你做** 。Composio 在中国大陆访问可能不方便，作为替代可以用阿里、腾讯云的 API 网关产品配合自建 Skill。GraphQL Schema Designer 在团队选型 GraphQL 时特别有用，新人不熟悉 GraphQL，Skill 兜底让产出符合最佳实践。API Builder Skill 在做新 RESTful API 时作为起点，生成的接口规范度高。

### 5.11 项目管理与规划类

这类 Skill 在 SDD 实践里很重要。

- **hierarchical-planner Skill** 创建分层的项目计划，适合单个开发者用 AI 做完整项目。产出 Claude 可执行的计划（带验证标准），不是企业文档式的计划。
- **brainstorm Skill** 多角色头脑风暴。AI 扮演产品、技术、设计、运营几个角色，从不同视角讨论一个问题。适合做初期方案选型。
- **spec-driven-development Skill** 把 SDD 流程编码成 Skill，引导 AI 走完 specify、plan、tasks、implement 四个阶段。和 Spec-Kit 工具是替代关系，适合不想引入新工具的项目。

项目管理类 Skill 的价值在 **思考流程结构化** 。新项目最难的是“想清楚”，这类 Skill 强迫 AI 按结构化流程思考，避免直接跳到写代码。

实战建议： **hierarchical-planner Skill 在做单人项目时价值最大，它产出的不是企业文档，是“Claude 能直接执行的计划”** 。brainstorm Skill 在方案选型阶段用，几个角色一起讨论比单个角色想得周全。spec-driven-development Skill 适合作为 Spec-Kit 的轻量替代：如果你不想引入 Spec-Kit 这种重型工具，装这个 Skill 就够用了（这也是 When 项目“轻量手工模式”的思路）。

### 5.12 安全审查类

安全相关的 Skill 是企业级项目的必备。

- **security-audit Skill** 静态分析代码，扫描常见漏洞：SQL 注入、XSS、CSRF、不安全反序列化、密钥泄露、依赖漏洞。装上之后每个 PR 自动跑一遍。
- **OWASP Skill** 按 OWASP Top 10 的视角审视代码，覆盖最常见的 Web 安全问题。
- **Secret Scanner Skill** 专门扫密钥和敏感信息泄露。检测 commit 历史里的 API key、密码、token，生成清单。
- **Penetration Testing Skill** 偏 red team 视角，引导 AI 做渗透测试，使用 ffuf、Burp、Metasploit 等工具。

安全类 Skill 的价值在 **安全门禁自动化** 。安全审查传统上是稀缺资源（需要懂安全的工程师），Skill 让一线开发都能跑基础安全检查。

实战建议： **security-audit Skill 应该作为 PR 必跑项，和 code-review Skill 并列** 。配合 git pre-commit hook 用，提交前自动扫描密钥泄露，降低误提交风险。Secret Scanner Skill 单独用一次扫历史 commit 检查老问题（很多项目历史 commit 里有遗留密钥）。Penetration Testing Skill 在做安全测试时用，AI 引导你按渗透测试流程走，产出测试报告。

## 6\. 怎么选 Skills：判断装不装的心法

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/1d8bae33597dcdde810974e82df5a6d7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

业界 Skill 这么多，装哪些？这一章给你一个判断框架，避免被各种“Top 10 Must-Have”清单牵着走。

### 6.1 几个判断标准

判断要不要装某个 Skill，我推荐用几个标准。

第一个标准是 **匹配当前工作场景** 。你这周做 React 前端，装前端 Skill 套件。下周做 Java 后端，前端 Skill 卸掉，装 Java Skill 套件。Skill 不是装得越多越好，装的是“当下用得上”的。和当前工作场景不相关的 Skill，即使再热门，装上也是噪音。

第二个标准是 **解决一个真实痛点** 。这个 Skill 解决的问题，是不是你最近反复遇到的？如果是，装。如果只是“看起来好玩”，先收藏，真正遇到这个问题再装。装一堆“以后可能用得上”的 Skill，大多数永远不会真用，只是占心智。

第三个标准是 **价值类型清楚** 。回到第 3 章的两类价值：这个 Skill 是 Capability Uplift（让 AI 能做新事）还是 Encoded Preference（让 AI 按你的方式做事）？两类都有用，但选择逻辑不同。Capability Uplift 类的 Skill 装上立刻能感受到效果（AI 能做之前做不到的事），Encoded Preference 类的需要时间体会（AI 输出的风格慢慢变了）。

第四个标准是 **官方或高 star 优先** 。Anthropic 官方维护的 Skill 质量稳定、文档完整、跨工具兼容性好。社区 Skill 优先选 obra/superpowers、Garry Tan GStack、官方推荐这种有信誉的来源。新建的、star 少的 Skill 风险高，可以观望一阵再决定。

第五个标准是 **试用再决定** 。装上用一周，观察 AI 行为是不是真的按这个 Skill 在工作。有些 Skill 写得花哨但 description 触发不准，装了也不生效。试用一周不生效或者没价值，果断卸掉。

### 6.2 三种常见反模式

反过来，有几种装 Skill 的姿势是错的，你要避开。

第一种反模式是 **跟风装明星 Skill** 。看到 GitHub trending 上某个 Skill star 涨得快，就装上。装完发现自己根本不做那个场景，纯占心智。Skill 不是工具集邮，是工作配套。

第二种反模式是 **装一堆类似 Skill 试图对比** 。装了三个不同的 code review Skill，期望“自动选最好的”。实际上 AI 不会自动选，反而可能混乱（三个 Skill 的 description 都匹配，AI 不知道用哪个）。同类 Skill 选一个用就行，挑那个最匹配你工作流的。

第三种反模式是 **不读 SKILL.md 就用** 。Skill 装上后不打开看 SKILL.md 里写了什么，出问题了不知道怎么改。Skill 不是黑盒，SKILL.md 是文本文件，装上花 5 分钟读一遍，知道它在约束 AI 做什么、不做什么，用起来才有信心。

避开这三种反模式，你的 Skill 装机表会保持精简、聚焦、有效。

## 7\. 自己沉淀 Skills 的方法论

这是文档的另一个核心。装别人的 Skill 是起点，真正的能力是 **从自己日常开发中沉淀 Skill** 。  
<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/37f3db6ef6c7145f480224b348b61465_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 7.1 什么时候该沉淀 Skill

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/eee3250ec1a02ad1380cd42785b79e31_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

不是所有操作都值得沉淀成 Skill。判断要不要做 Skill 的几个信号。

1. **同样的操作做到第三次** 。第一次做不知道有没有标准化空间，第二次发现“上次也做过这件事”，第三次确认是稳定模式，这时候沉淀成 Skill 最划算。过早做 Skill 容易僵化（还没摸清模式就固化），过晚做 Skill 已经被各种走样的实现污染（每次做都不一样）。第三次是合适的判断点。
2. **流程明确、步骤可重复** 。你能用文字写出“做这件事的标准步骤是什么”，而且这些步骤每次都一样，那这件事适合做 Skill。如果每次步骤都不一样，需要根据情况大幅调整，做 Skill 价值不大。
3. **AI 默认做不好或做不对** 。你发现 AI 在某类任务上反复做错（比如总用错某个客户端、总忽略某个边界条件、总写一种你不喜欢的风格），这是个强信号：把约束写成 Skill，AI 以后自动按 Skill 走。
4. **跨项目复用** 。你在项目 A 沉淀的这套做法，在项目 B、项目 C 也用得上，那做成 Skill 跨项目复用很有价值。如果只在某个特定项目用，放在项目级 CLAUDE.md 就行，不用单独做 Skill。

第五个信号是 **团队多人能受益** 。如果你团队有几个人都在做类似工作，Skill 能让团队风格统一，这是做 Skill 的强动机。

### 7.2 SKILL.md 的标准结构

SKILL.md 的格式很简单，但每个部分都有讲究。标准结构如下。

- 开头是 YAML frontmatter，两个字段： **name 和 description** 。name 是 Skill 的标识，通常用 kebab-case，比如 `commit-message-generator` 、 `react-best-practices` 。description 是关键中的关键，后面专门讲。
- frontmatter 之后是 markdown 写的指令。指令通常包括几个部分：Skill 的目的（为什么存在）、什么时候触发（场景描述）、具体步骤（SOP）、约束和原则（禁区）、示例（典型输入输出）。

最后可选地附上参考资源： `references/` 目录放详细参考文档、 `scripts/` 目录放辅助脚本、 `examples/` 目录放完整例子。这些资源不会一开始就加载，AI 真用到的时候才读。

一个最小可用的 SKILL.md 大概是这样的骨架，我用伪代码表达，不是真实代码：

复制代码

```
---
name: 我的 -skill-name
description: 一句话讲清楚这个 Skill 干什么, 什么时候应该触发
---

# 这个 Skill 的目的

一段话讲清楚 Skill 解决什么问题。

## 触发场景

什么样的用户输入会触发这个 Skill。列几个典型例子。

## 工作流程

1. 第一步做什么
2. 第二步做什么
3. 第三步做什么

...

## 约束和原则

- 必须做的事
- 不允许做的事
- 边界条件的处理方式

## 示例

输入: 某个具体例子
输出: 期望的产出
```

整个 SKILL.md 控制在几千 token 以内，核心指令明确，详细参考放到引用文件。

### 7.3 写 description 的关键技巧

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/284f508e7493438e903e272baf5fc855_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

description 是 Skill 里最重要的字段，决定 AI 能不能准确触发这个 Skill。写好 description 是 Skill 设计的核心技能。

description 写得好的关键有几条。

第一， **直接讲场景和触发条件** 。AI 通过比对当前任务和 description 决定是否加载这个 Skill，description 越具体，匹配越准。“用来处理代码”这种泛泛的描述基本不触发，“用 TDD 流程写代码，先写测试看着失败，再写实现让测试通过”这种具体描述触发准。

第二， **列出关键词和典型动作** 。AI 的匹配是语义级的，但关键词命中能提高准确率。把这个 Skill 相关的动词（生成、审查、修复、迁移）、名词（API、数据库、提交信息）写进 description，触发概率高。

第三， **写明什么时候用、什么时候不用** 。“用于 Python 代码生成，不适用于 JavaScript”这种边界声明很重要，避免 Skill 在错误场景被触发。AI 看到这种声明会主动判断当前场景是否合适。

第四， **长度控制在 50 到 200 字** 。太短信息不够，触发不准。太长占元数据空间，AI 扫描所有 Skill 时累。50 到 200 字是合理区间。

举一个 description 写得好的例子（用 commit-message-generator Skill 的 description 改写）：

> “根据 staged git diff 生成符合 Conventional Commits 规范的提交信息。在用户要求写 commit message、生成提交说明、或者准备提交代码时触发。识别改动类型（feat、fix、chore、docs 等），从改动文件推断 scope，生成英文 commit message。不适用于：用户已经手写好 commit message 只需要 review 的场景。”

这段 description 做对了几件事：

- 讲清楚做什么（根据 diff 生成 commit message）
- 什么时候触发（用户的几种典型表达）
- 关键动作（识别类型、推断 scope、生成消息）
- 边界（不适用什么场景）。

AI 看到这段 description，触发准确率明显提高。

### 7.4 Skill 内容的渐进式分层

写 Skill 内容的时候，要利用 Skill 系统的渐进式加载特性，把内容分层。

- 最上层（SKILL.md 主体）放 **核心指令和最常用的工作流** 。一两个 A4 大小的篇幅，几千 token 以内。AI 触发这个 Skill 时一定会读这部分，所以放最关键的内容。
- 中层（ `references/` 目录）放 **详细参考和延展知识** 。比如 SKILL.md 提到“用 pdfplumber 提取文本”，具体的 pdfplumber API 用法、参数细节、常见坑放在 `references/pdfplumber.md` 。AI 在 SKILL.md 里引用这个文件，真用到的时候才加载。
- 下层（ `scripts/` 、 `examples/` 目录）放 **可执行脚本和完整例子** 。比如 SKILL.md 提到“按这个模板写测试”，完整的测试模板代码放在 `scripts/test-template.py` 。AI 需要的时候直接调用脚本。

这套分层的好处是 **SKILL.md 主体保持简洁，详细内容按需加载** 。你写 Skill 时不需要把所有细节都塞到 SKILL.md 里，可以放心地把延展内容放到引用文件，反正 AI 用到的时候会自己读。

### 7.5 一个真实例子：从需求到 SKILL.md 的全流程

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/81985c7f5edb2060675594eeaf1c566a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

我们用一个真实场景走一遍，看 Skill 怎么从需求变成 SKILL.md。  
<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/8333976754e02540922e180f2e60e66b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

**起点：发现一个反复出现的问题**

你做 Spring Boot 项目，发现 AI 写 REST controller 时反复出问题：

第一，参数校验用 @Valid 但不写自定义错误消息，前端拿不到友好提示。第二，异常处理散落各处，每个 controller 自己 try-catch。第三，响应格式不统一，有的返回原始对象，有的包了一层 Result 类。

这是个典型的“AI 默认做法不符合项目要求”场景，适合做 Skill。

**第二步：整理你期望的标准做法**

你和团队约定的标准做法是：

1. 所有参数用 @Valid 校验，自定义错误消息中文化
2. 异常统一用 @ControllerAdvice 全局处理，不在 controller 里 try-catch
3. 所有响应统一用 ApiResponse 类包装，带 code、message、data 三个字段
4. 错误情况返回标准错误码，在 ErrorCode 枚举里维护

这四条标准把你的期望讲清楚了。

**第三步：写 SKILL.md**

把这套标准翻译成 SKILL.md。骨架大致是：

复制代码

```
---
name: spring-rest-controller
description: 生成符合项目规范的 Spring Boot REST controller。在用户要求新增 API 端点、写 controller、或修改 RESTful 接口时触发。强制使用 @Valid 校验、@ControllerAdvice 异常处理、ApiResponse 响应包装、标准 ErrorCode 错误码。
---

# Spring Boot REST Controller 标准写法

## 触发场景

用户提出以下任意需求时触发:

- 新增 API 端点
- 写 REST controller
- 修改 RESTful 接口
- 加 HTTP 错误处理

## 标准流程

1. 接口路径用 RESTful 命名 (资源名复数、HTTP 动词、嵌套不超过两层)
2. 参数加 @Valid 校验,@NotNull/@NotBlank/@Size 的 message 用中文
3. 返回类型用 ApiResponse<T>, 内置三个字段:code、message、data
4. 异常不在 controller 里 try-catch, 抛出自定义 BizException 由全局处理器处理
5. 错误码从 ErrorCode 枚举取, 不允许硬编码字符串

## 约束和禁区

- 不要在 controller 里写业务逻辑, 只做参数校验和调用 service
- 不要用 ResponseEntity, 统一用 ApiResponse
- 不要写 @ExceptionHandler 在单个 controller, 统一在 GlobalExceptionHandler
- 不要返回 null, 无数据时返回空对象或空列表

## 参考资源

详细的 ApiResponse 类定义见 references/api-response.md
详细的 ErrorCode 枚举见 references/error-codes.md
完整的 controller 示例见 examples/sample-controller.java
```

**第四步：补充参考资源**

`references/api-response.md` 里放 ApiResponse 类的完整定义、用法示例、和 ResponseEntity 的区别。 `references/error-codes.md` 里放当前项目所有 ErrorCode 枚举值的列表和含义。 `examples/sample-controller.java` 里放一个完整的标准 controller 例子。

**第五步：测试这个 Skill**

装上 Skill，让 AI 写一个新的 controller。看 AI 是不是按这套规范写的：有没有用 @Valid，错误消息有没有中文化，返回有没有用 ApiResponse，异常有没有走全局处理。

如果 AI 行为符合期望，Skill 写好了。如果某个点 AI 没做对，回头改 SKILL.md 里对应的指令，让它更明确。这是一个迭代的过程，不要指望第一版就完美。

**第六步：沉淀和分发**

这个 Skill 写好后，放在项目的 `.claude/skills/spring-rest-controller/` 目录，所有用 Claude Code 的同事自动能用。或者放在个人 `~/.claude/skills/` 目录，跨项目可用。如果觉得通用，提到团队仓库或 GitHub 让社区受益。

整个流程下来，你完成了一次从“日常问题”到“工程资产”的转换。下次再做类似项目，这个 Skill 直接可用，不需要每次重新教 AI。

### 7.6 个人、项目、团队三种 Skills 的差别

Skill 按存放位置可以分三种，各自的用法不同。

**个人 Skill** 放在 `~/.claude/skills/` 目录，跨所有项目可用，只对你自己生效。适合编码自己的工作习惯：你喜欢的命名风格、你常用的工具组合、你的 TDD 流程。个人 Skill 是你最早开始沉淀的部分，从你日常工作里抽象出来。

**项目 Skill** 放在项目仓库的 `.claude/skills/` 目录，只在这个项目里生效，所有 clone 这个项目的人都能用。适合编码项目特有的规范：API 风格、数据库表命名、错误处理约定。项目 Skill 跟着项目一起版本化，新人 clone 项目后立刻享有完整的 Skill 支持。

**团队 Skill** 放在团队共享仓库，通过软链或 plugin 机制让团队成员都装上。适合编码团队层面的标准：代码 review 流程、提交规范、安全审查清单。团队 Skill 让团队的工程文化“可执行”，而不只是口头约定。

三种 Skill 不冲突，按优先级合并：项目 Skill 覆盖个人 Skill 同名的，团队 Skill 覆盖项目 Skill 同名的（如果配置了团队优先）。实操上你会同时有这三种 Skill 在生效，各管一摊。

你需要养成的习惯是 **新发现一个模式就思考放哪个层级** 。这个模式是个人的（只我喜欢这么干），放 `~/.claude/skills/` 。这个模式是项目的（项目要求），放 `.claude/skills/` 。这个模式是团队的（团队规范），提到团队仓库。三种层级各自演进，长期下来你的 Skill 资产会非常丰富。

## 8\. Skills 和团队协作

Skills 在团队层面的协作有几个值得讲清楚的实践。

### 8.1 团队 Skills 仓库怎么管

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/4499cf19b015c8505aab240eb8b72a66_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

团队 Skills 通常有几种组织方式。

第一种是 **单仓库集中管理** 。建一个 `team-skills` 仓库，所有团队 Skill 放里面，每个 Skill 一个目录。团队成员 clone 这个仓库到 `~/.claude/team-skills/` ，通过软链或者 `--add-dir` 参数加载。这种方式简单直接，适合小团队（几个人到几十个人）。

第二种是 **按职能拆仓库** 。前端 Skills 一个仓库、后端 Skills 一个仓库、运维 Skills 一个仓库。团队成员按自己的职能 clone 对应仓库。适合规模大、职能分明的团队。

第三种是 **每个项目自带 Skills** 。Skills 跟着项目走，放在项目仓库的 `.claude/skills/` 。新人 clone 项目就有 Skills。适合多项目并行、项目间差异大的场景。

实操建议： **小团队用第一种、大团队用第二种、多项目用第三种** 。不一定二选一，可以混搭（团队共有的放第一类仓库，项目特有的放项目里）。

### 8.2 Skills 怎么版本化

Skills 是文本资产，用 git 版本化最自然。但有几个特殊点。

**SKILL.md 的修改要走 PR** 。同事改 description 或者改流程的时候，review 是必要的，因为 Skill 的改动影响所有使用者的 AI 行为。一个 description 改错可能导致 Skill 在不该触发的场景触发。

**Skills 的版本要和它依赖的工具同步** 。比如某个 Skill 依赖 pdfplumber 0.10+，pdfplumber 升级到 1.0 后 API 变了，Skill 也要跟着更新。这种依赖关系建议在 SKILL.md 开头写清楚（“本 Skill 依赖 pdfplumber 0.10+”）。

**Skills 的废弃要有明确流程** 。某个 Skill 不再使用了（比如被新的 Skill 替代），不要直接删，在 SKILL.md 里加上 “DEPRECATED： 用 new-skill-name 替代” 标记，过一段时间（一个版本周期）再删，让使用者有时间迁移。

**重大变更要写 changelog** 。Skill 的 SKILL.md 内容变化（尤其是 description 改了）要记录在 CHANGELOG.md 里，使用者能看到这个 Skill 的演进。

### 8.3 Skills 的评审和迭代

新 Skill 加入团队前，值得过一道评审。评审看几个点。

第一， **这个 Skill 解决的问题是不是真实的** 。avoid 为了“显得专业”而做 Skill，只做真实解决问题的 Skill。

第二， **description 写得准不准** 。让团队几个人读 description，猜这个 Skill 是干什么的。如果大家猜得不一样，description 不够准，要改。

第三， **和已有 Skill 有没有重叠** 。如果团队已经有一个做类似事情的 Skill，新 Skill 要么改进它，要么明确边界，不要做重复的。

第四， **SKILL.md 长度合不合理** 。太短（几百字）信息不够，太长（几万字）占上下文。理想范围在几千字符。超出范围考虑拆分（详细内容放到引用文件）。

通过评审后，Skill 进入团队仓库，所有人能用。

迭代上，定期回顾（比如每月一次）看哪些 Skill 真的被频繁触发，哪些几乎不被用。不被用的要么是 description 写不准，要么是真的没价值。改 description 或者删除，保持 Skill 库精简有效。

### 8.4 实际团队的 Skills 起步路径

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/de6ab8bb7ad38ecfc92ec21f141d20aa_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

团队从零开始引入 Skills 不需要一步到位，有个合理的起步路径。

**第一阶段（第一个月）选个 1 到 2 个人尝鲜** 。这一到两个人在自己项目里用 Skills，装几个官方 Skill，沉淀几个项目级 Skill，把使用感受、效果、坑都摸一遍。这个阶段的目标不是规模化，是建立“团队里有人懂 Skills”的种子。

**第二阶段（第二、三个月）在一个项目里全员推** 。挑一个团队都参与的项目，所有人在这个项目里用 Skills。Skill 仓库放项目根目录，所有改动走 PR。这个阶段会出现很多教训：Skill 写得不准、SKILL.md 太长、description 触发错误。这些教训本身是宝贵的，记录下来作为后续团队规范。

**第三阶段（第四个月起）沉淀团队级 Skills 仓库** 。把项目里验证过的 Skill 抽出来，放到团队共享仓库。新项目都默认用这套 Skills。这个阶段是规模化的起点，团队的工程文化开始“通过 Skills 自动执行”。

**第四阶段（半年到一年）向社区贡献** 。团队沉淀的优质 Skill 可以贡献给开源社区，既是回馈，也是品牌建设。社区贡献者有动力维护 Skill 质量，团队也能从社区反馈中学习。

这套路径不强求完整跑完，根据团队实际情况调整。但有一个原则： **不要试图一次性全员推全套 Skills** 。让 1 到 2 个种子先跑通，他们的经验是后续推广的基础。

### 8.5 Skills 的失败模式

讲一些团队推 Skills 时常见的失败模式，避坑用。

**第一种失败是 Skill 库过度膨胀** 。团队为了“显得专业”，每个动作都做成 Skill，结果几十上百个 Skill，description 互相重叠，AI 不知道触发哪个，效果反而比没 Skill 还差。解决办法：每月评审一次，不被用的 Skill 删掉，description 重叠的合并。

**第二种失败是 Skill 写成“操作手册”** 。SKILL.md 写得像新人入职文档，讲很多背景、很多原则、很多“为什么”。AI 读完上下文都满了，真正的指令反而没空间。解决办法：SKILL.md 只写“AI 做事的指令”，背景知识放 references/。

**第三种失败是 Skill 和 CLAUDE.md 重复** 。同一条约束既在 CLAUDE.md 又在某个 Skill 里。AI 听谁的？这种重复会让 AI 行为不一致。解决办法： **项目通用约束放 CLAUDE.md，特定场景的标准流程放 Skill** 。两者职责清晰不冲突。

**第四种失败是 Skill 不维护** 。Skill 上线后没人管，依赖的工具升级了、API 变了，Skill 还在按老方法走。结果 AI 按 Skill 写出来的代码跑不通，大家不信任 Skill 了。解决办法：每个 Skill 指定 owner（就像代码 owner），owner 负责跟进相关依赖的变化。

**第五种失败是 Skill 取代了思考** 。团队太依赖 Skill，新问题来了第一反应是“找个 Skill 用”，而不是“想想问题本身”。Skill 是固化已知模式，新问题需要先思考再固化。解决办法： **记住 Skill 是工具不是替代品** ，创造性的工作永远需要思考。

## 9\. Skills 生态的演进趋势

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/b18d1da6b11bcdc0d475ccb8c3cec2eb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

写到这一节，补充一些关于 Skills 生态未来几年走向的判断。这些判断不是预言，是基于当下趋势的合理外推，供你做长期投入决策参考。

### 9.1 标准化进一步深化

Skills 的开放标准在 2025 年底由 Anthropic 推动，2026 年初已经被主流 AI 编程工具（Claude Code、Cursor、Codex、Gemini CLI、Windsurf、Antigravity）全部支持。这个标准化趋势会进一步深化，可能演化方向有几个。

**Skills 的语义匹配会变得更精准** 。当前 Skills 触发依赖 description 的语义匹配，有时候不够准。未来可能引入更精细的元数据（任务类型、技术栈、依赖工具），让匹配更准确。

**Skills 之间的依赖管理会标准化** 。当前 Skill 之间可以引用，但管理粗糙。未来可能出现类似 npm 的依赖管理，一个 Skill 声明依赖哪些其他 Skill，自动加载，避免重复造轮子。

**Skills 的版本化和兼容性管理会跟进** 。当前 Skill 的版本管理靠 git tag，粗糙。未来可能有专门的版本协议，标记 Skill 与底层 AI 模型版本、依赖库版本的兼容性。

你投入 Skills 的长期判断： **标准会更稳定，投资 Skills 的回报会更长期** 。今天写的 Skill，五年后大概率还在工作，而且经过迭代更成熟。

### 9.2 Skills 市场会出现

类似 npm、PyPI、Maven Central 这种，Skills 也会出现专门的市场和分发机制。当前几个尝试： [bestclaudecodeskills.com](https://bestclaudecodeskills.com/) 是个目录站， [agentskills.io](https://agentskills.io/) 在做更结构化的市场，Anthropic 官方的 plugin marketplace 在快速扩张。

Skills 市场出现后，会带来几个变化。

1. **优质 Skills 浮现** 。市场有评分、下载量、星级，优质 Skill 自然被推到前面，你选 Skill 的决策成本降低。
2. **Skill 商业化探索** 。免费 Skill 之外可能出现付费 Skill（企业级 Skill、专业领域 Skill），类似当前 IDE 插件市场的商业模式。
3. **Skills 安全审计变得重要** 。Skill 可以执行代码、访问文件、调用 API，安全风险存在。Skills 市场会引入审计机制，标记安全 Skill 和有风险的 Skill。

你实操建议： **不要急着上传自己的 Skill 到任何市场** 。先在自己项目和团队里沉淀 Skill，等格式稳定、自己用顺手了，再考虑分享。早期上传容易踩坑（格式被废弃、被改名、被合并），浪费精力。

### 9.3 Skills 和工程组织变化

更长远看，Skills 这种“工程模式可文本化”的能力，会改变工程组织的形态。

**初级工程师的边界会变化** 。当前初级工程师做的很多事（写 boilerplate、按规范实现、跑常规测试），装 Skill 后 AI 直接能做。初级工程师的核心价值会从“会做这些事”转移到“会沉淀这些事成 Skill”，定位上升。

**资深工程师的影响力会放大** 。一个资深工程师把自己的经验沉淀成 Skill，团队里其他人通过 Skill 享有这份经验，资深工程师的影响力直接放大 N 倍（N 是团队人数）。这种影响力是真实的、可度量的，Skill 的下载量、触发次数都是指标。

**团队的工程文化会变得“可执行”** 。当前团队的工程文化靠口头约定、文档约束，执行力差。Skill 让工程文化变成“AI 自动执行的约束”，新人入职就享有团队完整的工程文化，不需要漫长的“摸索过程”。

**外包和外协会重新洗牌** 。外包公司过去靠“我们有大量初级工程师”赚钱，这种模式在 Skills 时代逐渐失效。少数资深工程师配合 AI 加 Skills，产出能超过大量初级工程师配合落后工具。外包行业会向“高端工程团队”和“AI 工作流服务”两极分化。

这些判断不需要你立刻认同，作为长期投入 Skills 的参考。最后一句： **Skills 的投入不是临时的工具学习，是工程师对自己长期资产的投资** 。看清这一点，投入的姿态就不一样。

## 10\. 结语：Skills 作为程序员个人资产

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/3260d206c5c778339551156509d201d1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

写到最后想强调一件事：Skills 不只是一个工具特性，是程序员在 AI 编程时代积累 **个人工程资产** 的重要载体。  
<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/a6dec09a30b35055ea35108ef5ffc543_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

传统意义上，程序员的资产是代码。代码是项目的产物，跟着项目走，换公司、换项目可能就用不上。文档是公司的资产，你写的文档留在内网，出公司带不走。经验是个人的资产，但只在你脑子里，转移给别人成本高。

Skills 不一样。Skills 是 **可文本化、可移植、跨工具、可复用** 的工程资产。你在 A 公司沉淀的 commit-message-generator Skill，跳槽到 B 公司继续用。你在 Claude Code 写的 Skill，换成 Cursor、Codex 还能用。你五年前学会的某个工程模式，如果当时沉淀成了 Skill，五年后还在为你工作，而且经过这些年的迭代它变得更成熟。

这是个新时代特有的资产形式。前 AI 时代，程序员积累经验靠“做项目 + 读书 + 和同事讨论”，经验大多停留在脑子里，很难规模化复用。 **AI 时代，经验可以编码成 Skill，直接让 AI 按你的经验执行，你的工作半径被 AI 放大几倍** 。

你需要认真对待这件事。从训练营 3 周开始，把每个值得沉淀的工程模式都做成 Skill。第一年可能就沉淀 5 到 10 个，看起来不多，但这 5 到 10 个 Skill 是你真正的、带得走的、长期复利的工程资产。坚持几年，你的 Skill 库会变成你最有价值的工作工具，远超过你拿过的任何工资条。

这份文档不能让你立刻拥有 Skill 库，但希望它给了你三件事：

1. 看清楚业界 Skill 全景的地图
2. 判断装不装某个 Skill 的标准
3. 沉淀自己 Skill 的方法论。

把这三件事用起来，Skill 这个工具就在你手上变成可用的工程实践，而不只是某个文档里的概念。
