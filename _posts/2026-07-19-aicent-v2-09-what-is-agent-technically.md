---
title: AI编程方法(2) 09：Agent从技术上看到底是什么
author: fangkun119
date: 2026-07-19 09:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-v2-09-what-is-agent-technically/cover.jpg
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
AI编程方法(2) 09：Agent从技术上看到底是什么
aicent-v2-09-what-is-agent-technically
-->

现在什么都叫 Agent。一个会调 API 的脚本叫 Agent，一个套壳的 ChatGPT 叫 Agent，一个按固定流程跑的自动化也叫 Agent。词被用滥了，代价是你没法判断一个东西到底是不是 Agent，也就没法判断它能干什么、不能干什么。

这篇文章给你一把尺子，用 5 个技术组件拆解 Agent。看到任何号称 Agent 的东西，都能立刻判断它是真 Agent 还是挂羊头。

## 1. 先建立直觉：Agent 到底是个什么东西

<img src="imgs/aicent-v2-09-what-is-agent-technically/8b689af43d505f92b11bc7a41dc22b5f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

先不上定义。用一个传统工程师秒懂的类比把画面立起来。

传统脚本像一个只能按 SOP 执行的操作员：每一步都写死在流程里，输入 A 就调 B、再调 C、输出 D。遇到 SOP 没写的情况，它直接卡住或报错。你给它的"自主权"等于零，它只是把人写好的步骤按顺序跑一遍。

Agent 像另一种员工：你只给它一个目标（比如"把这个需求做完"），它会自己看当前情况，自己决定下一步做什么，自己动手执行，做完还能回头看结果对不对，不对就换个办法再试。中间遇到 SOP 没写的情况，它能临场判断。

这个类比要先种下两件事：

- **自主决策**：它自己决定下一步，不是按死规则跑。
- **循环**：它不是一次性出结果，而是"决策—行动—观察—再决策"一圈圈转，直到任务完成。

<span style="color: red; font-weight: bold;">这两件事是 Agent 的核。传统脚本两条都没有，Agent 两条都有。剩下的区别都是这两条衍生出来的。</span>

下面把这直觉翻译成技术语言。

## 2. 一句话定义和五组件公式

<img src="imgs/aicent-v2-09-what-is-agent-technically/ebe03c6a1503870d9d1b5b86a1f5cfda_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">Agent 是一个能感知环境、自主决策、执行动作、并基于反馈循环迭代的程序。</span>

每个加粗词都要立住：

- **感知环境**：能看到外部世界的状态（用户消息、文件、API 响应、网页内容）。
- **自主决策**：能根据当前状态自己判断下一步做什么，不需要每一步都让人指挥。
- **执行动作**：能对外部世界做出真实改变（写文件、调 API、发消息、改数据库）。
- **反馈循环**：动作产生的结果重新进入感知，形成持续循环。
- **迭代**：随着循环推进，越来越接近目标。

注意这个定义里没有"LLM"三个字。Agent 这个抽象在 LLM 时代之前就存在，强化学习里的 Agent、游戏 AI 里的 NPC 都符合这个定义。LLM 让 Agent 的工程实现门槛大幅下降，但 **Agent 不等于"用 LLM 做的东西"**。这一点先记住，后面分辨真假 Agent 时用得上。

把定义具象化，2026 年我们说的 Agent 在技术上是：

<span style="color: red; font-weight: bold;">Agent = LLM（决策引擎）+ Tools（行动能力）+ Memory（状态承载）+ Loop（循环执行）+ Environment（交互对象）</span>

5 个组件 + 1 个循环结构 = Agent 的完整技术骨架。任何 Agent 系统，不管多复杂，都能用这 5 个组件解释。下一章把这五个词逐一展开。

## 3. 五个组件逐一拆解

逐一展开 5 个组件。

<img src="imgs/aicent-v2-09-what-is-agent-technically/bccb4c084d1d4bad3ece4ac551bb8e4d_MD5.webp" style="display: block; width: 800px;" alt="替换文字">
<!-- 
图片内容说明
路径：imgs/aicent-v2-09-what-is-agent-technically/bccb4c084d1d4bad3ece4ac551bb8e4d_MD5.webp
用途：第二章开头，用一张架构图把"五组件 + ReAct 循环"的 Agent 技术骨架一次性可视化，作为接下来 2.1–2.5 各组件展开的总览
内容：中心为 ReAct 循环流程图，标注三阶段——Reasoning(思考/决策)、Acting(行动/调工具)、Observation(观察/拿结果)，并循环回到 Reasoning；围绕循环的五个组件框：LLM(决策引擎/大脑，驱动 Reasoning)、Tools(行动能力，对应 Acting)、Memory(状态承载，装对话历史/任务进度/工具结果)、Loop(循环执行本身，发动机)、Environment(交互对象，文件系统/网络/数据库/用户接口)。整体表达"Agent = LLM + Tools + Memory + Loop + Environment，五组件在一个 ReAct 循环里协同运转"。
-->

### 3.1 逐个组件展开

每个小节按同一套节奏走：**类比 → 定义 → 工程实现 → 关键判断**。读起来像 5 遍同一种鼓点，不累。

#### (1) LLM：决策引擎


<img src="imgs/aicent-v2-09-what-is-agent-technically/3cb4286682c914664652eb966959e552_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

- **类比**：Agent 的大脑，对应传统系统里的 CPU。
- **定义**：理解任务和上下文，规划下一步，决定调哪个工具、用什么参数，解读工具返回的结果，判断任务是否完成。
- **工程实现**：通过 API 调用大模型（Claude、GPT、Gemini、Qwen、Nous Hermes 等）。可以单模型（所有决策同一个模型），也可以多模型路由——planning 这种重活用强模型、synthesis 这种轻活用快模型、主模型不可用时切到备用模型。OryxOS 项目的"多模型路由"，本质就是 LLM 决策引擎的精细化设计。
- **关键判断**：<span style="color: red; font-weight: bold;">LLM 不是 Agent 的全部，但是 Agent 的核心</span>。没有 LLM 的"决策"，"自主"两个字立不住，剩下的只是一个按死规则跑的脚本。

#### (2) Tools：行动能力

<img src="imgs/aicent-v2-09-what-is-agent-technically/b1f25839b72f37cc5d875eb236b122ed_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

- **类比**：Agent 伸出去的手，对应传统系统里的系统调用。
- **定义**：没有 Tools，LLM 只会输出文字。有了 Tools，Agent 才能写文件、调 API、查数据库、发邮件、点网页，才有了"行动"。
- **工程实现**：工具按形态分几类——本地工具（read_file / write_file / run_command）、网络工具（web_search / web_fetch）、数据库工具（query_db / update_db）、系统工具（list_processes），还有通过 MCP 协议调用的远程工具（GitHub MCP / Slack MCP / 数据库 MCP 等）。调用标准模式不复杂：LLM 输出结构化的"工具调用请求"（调哪个工具、传什么参数），框架接到请求后真正执行，再把结果回传给 LLM。LLM 自己不碰真实世界，只下指令；干活的是框架。这个分工意味着，Agent 能做什么、不能做什么，完全由你给它配了哪些工具决定。
- **关键判断**：<span style="color: red; font-weight: bold;">Tools 是 Agent 跟外部世界连接的接口。Agent 的"自主性"边界，本质上就是 Tools 的边界</span>。Tools 没给的能力，Agent 就做不了。这一点在后面讲 Agent 安全时是关键——管住一个 Agent，很大程度就是管住它能调哪些工具。

顺带一个趋势：MCP（Model Context Protocol）是 2025–2026 年 Tools 标准化的重要进展。Anthropic 推动的 MCP 协议让"工具"变成可插拔的标准件，一个 MCP Server 写好后，任何 MCP 兼容的 Agent 都能用。这件事正在改变 Agent 生态。

#### (3) Memory：状态承载

<img src="imgs/aicent-v2-09-what-is-agent-technically/d4cc4d6a47b1a6683fe504739ed1e064_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

- **类比**：Agent 的工作内存和硬盘。
- **定义**：Agent 在循环里得"记住"东西，不然每一步都像失忆重来。
- **工程实现**：短期记忆装当前对话历史、任务进度、工具调用结果、状态变量；长期记忆装跨会话的事实（用户偏好、项目背景）、技能、用户画像。两者对比如下。

| 维度 | 短期记忆（Context） | 长期记忆 |
|---|---|---|
| 内容 | 对话历史 / 任务进度 / 工具调用结果 / 状态变量 | 跨会话事实（用户偏好、项目背景）、技能、用户画像 |
| 实现 | LLM 的 context window | MEMORY.md / SQLite / 向量库 / 混合存储 |
| 是否必需 | 必需 | 高阶 |

短期记忆通常直接放在 LLM 的 context window 里，每次循环把过去的 reasoning、action、observation 拼进 prompt。长期记忆超出 context window 容量，要外部存储，常见实现有结构化文件（MEMORY.md、SQLite）、向量存储（embedding + 向量数据库按相似度检索）、混合存储（结构化文件 + 全文搜索 + 向量召回）。

- **关键判断**：<span style="color: red; font-weight: bold;">短期记忆是必需，长期记忆是高阶</span>。没有长期记忆的 Agent 也是 Agent，只是每次会话都是新的，不会成长。有长期记忆的 Agent 才会"成长"——Hermes Agent 主打的 persistent memory 就是在这一层做文章。

#### (4) Loop：循环执行

<img src="imgs/aicent-v2-09-what-is-agent-technically/37dfd6cea86006ff473572d7775e3b90_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

- **类比**：Agent 的心跳，对应传统系统里的主循环（main loop）。
- **定义**：Agent 的灵魂，也是它和 chatbot 最根本的区别。单次 LLM 调用是 chatbot，你问一句它答一句，完事；LLM 在循环里反复"决策—行动—观察—再决策"，才是 Agent。
- **工程实现**：最经典的是 **ReAct 范式（Reasoning + Acting）**，2022 年 Shunyu Yao 等人在论文里提出，现在是事实标准，Claude Code、Cursor、LangChain、AutoGPT 内部都跑这个循环。伪代码就这么几行：

```
循环开始：
    Reasoning（思考）：基于当前状态，LLM 决定下一步做什么
    Acting（行动）：调用一个 Tool 执行决策
    Observation（观察）：拿到 Tool 的返回结果
    更新状态：把 observation 加入 context
    判断是否完成：完成就跳出循环，否则继续转
```

就这个圈，转到任务完成或者达到最大步数为止。但循环不是无脑跑，一个好的 Agent 会在循环里处理 4 件事：

| 序号 | 循环里的事 | 说明 |
|---|---|---|
| 1 | 判断停止条件 | 任务完成 / 达到最大步数 / 用户中断 |
| 2 | 错误恢复 | 工具调用失败时是重试、降级还是报错给用户 |
| 3 | 上下文压缩 | context 越来越长时怎么压缩历史 |
| 4 | 多模型切换 | 不同步骤用不同模型 |

- **关键判断**：<span style="color: red; font-weight: bold;">Loop 的设计质量决定 Agent 的工程质量</span>。一个 Agent 框架 80% 的复杂度都在 Loop 里。早期那些容易陷入死循环、越跑越懵的 Agent，问题基本都出在 Loop 没设计好。

#### (5) Environment：交互对象

<img src="imgs/aicent-v2-09-what-is-agent-technically/bdff28666f0c20b2e024378152c88ac5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

- **类比**：Agent 的"世界"或运行场地，对应传统系统里进程跑在哪个运行时环境。
- **定义**：Agent 在什么环境里做事——文件系统、网络、数据库、用户接口（CLI / Web / IM / API）。在 Multi-Agent 系统里，其他 Agent 也是环境的一部分。
- **工程实现**：不同 Agent 活在不同环境。Coding Agent 的 Environment 通常是代码仓库 + 本地终端 + 网络（搜文档）+ 其他文件系统位置；Customer Service Agent 的 Environment 通常是用户 IM 消息流 + 知识库 + CRM 系统 + 工单系统。环境不是免费的，一个 Agent 能不能在某个环境里跑，取决于三件事：
  1. 这个环境能不能被工具化（有 API / 有 SDK / 能 scrape）；
  2. 这个环境的权限怎么管（哪些操作允许，哪些禁止）；
  3. 这个环境的副作用怎么控制（破坏性操作怎么回滚）。
- **关键判断**：<span style="color: red; font-weight: bold;">沙盒（Sandbox）就是 Environment 的安全设计</span>。OryxOS 项目的"工作空间隔离 + 沙盒"，本质就是给每个 Agent 提供受控的、跑坏了也不殃及别人的 Environment。Environment 决定 Agent 能力的上限，也决定它的安全底线。

光拆开看不够，下一章看它们怎么转起来。

## 4. 把五组件装回去：跑一个最小 Agent

<img src="imgs/aicent-v2-09-what-is-agent-technically/9cc45226d385df4ada1b11c839c8a07e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

举一个具体到能照着做的例子。你让 Agent：

> 读 README.md，翻译成中文，写入 README.zh.md。

你的这句话是任务，进入 Environment（这里是本地文件系统），先存进 Memory（短期记忆，记住"要干什么"）。Loop 开始转，每一圈里五个组件的角色如下：

| 圈次 | LLM 决策 | Tools 行动 | Observation → Memory | Loop 状态 |
|---|---|---|---|---|
| 第 1 圈 | 看到任务，决定先读文件 | `read_file("README.md")` | 文件内容回 Memory | 继续转 |
| 第 2 圈 | 看内容、决定翻译 | （不调工具，自己生成中文） | 翻译结果回 Memory | 继续转 |
| 第 3 圈 | 决定写入 | `write_file("README.zh.md", 翻译内容)` | "写成功"回 Memory | 继续转 |
| 第 4 圈 | 判断任务完成 | 无 | 无 | 跳出 Loop |

Environment 全程是本地文件系统。Memory 全程在累积：任务 → 文件内容 → 翻译结果 → 写成功结果，每一步都叠加上去。

注意第 2 圈，LLM 不调任何工具，自己生成翻译——这一圈告诉你，LLM 不一定每圈都调工具，"决策"也可以是"我自己来"。

整个过程五个组件一个不少：LLM 在每一圈做决策，Tools 是它伸出去的手，Memory 让它记得前面发生了什么，Loop 是让它一圈圈转下去的发动机，Environment 是它干活的场地。**这就是 Agent 在技术上的本质**。所有复杂 Agent 系统都是这个最小结构的放大版——工具更多、记忆更复杂、循环里处理的情况更细，但骨架还是这五样加一个圈。

骨架明白了，下一章用这把尺子去量现实世界的产品和概念。

## 5. 用这把尺子去量现实世界

掌握了五组件抽象，开始用它做三件事：拆产品、分易混概念、做自测题。三件事本质都是同一件——**用尺子做判断**。你能量准，说明抽象真正进到了脑子里。

### 5.1 拆产品、分易混、做自测
#### (1) 拆六个现实产品

<img src="imgs/aicent-v2-09-what-is-agent-technically/db776140e5a49c535e63369746b3c403_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">



<img src="imgs/aicent-v2-09-what-is-agent-technically/4407dfb1002a1fad791f7a3e0b637327_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!-- 
图片内容说明
路径：imgs/aicent-v2-09-what-is-agent-technically/4407dfb1002a1fad791f7a3e0b637327_MD5.jpg
用途：第四章开头，用一张"产品 × 五组件"对照表，把主流 Agent 产品按 LLM/Tools/Memory/Loop/Environment 拆开，印证正文 4.1–4.7"抽象的力量"——千差万别的产品本质都是五组件的不同实现
内容：矩阵式对比图，列为产品(ChatGPT、Claude Code、Cursor、LangChain、Hermes、AutoGPT)，行为五组件(LLM、Tools、Memory、Loop、Environment)。每个单元格填入该产品在该组件上的具体实现，例如 ChatGPT 经典版只占 LLM+Memory 两格(故为 chatbot)，加 Code Interpreter/插件后才凑齐五组件；Claude Code 五格全满(Opus/Sonnet、Read/Write/Edit/Bash/MCP、CLAUDE.md+Skills、ReAct、本地仓库+终端)；Cursor 贴 IDE、LangChain 是框架、Hermes 主打跨会话长期记忆、AutoGPT 五组件齐全但 Loop 设计欠成熟。通过"是否凑齐五组件"一眼分辨真 Agent 与类 Agent。
-->

六个产品（ChatGPT、Claude Code、Cursor、LangChain、Hermes、AutoGPT）用一张表横向对比：

| 产品 | LLM | Tools | Memory | Loop | Environment | 一句话定位 |
|---|---|---|---|---|---|---|
| ChatGPT（经典） | GPT | 无 | 对话历史（短期） | 无 | 无 | chatbot，不是 Agent |
| ChatGPT（+Code Interpreter+联网+插件） | GPT | Code Interpreter / 插件 / 浏览器 | Custom Instructions（长期）+ 对话历史（短期） | 内部 ReAct | 用户文件 + 互联网 | 凑齐五组件才跨过线 |
| Claude Code | Claude（Opus / Sonnet） | Read / Write / Edit / Bash / Grep / Web Search / Skills / MCP Tools | CLAUDE.md + AGENTS.md + Skills + 对话历史 | ReAct + Plan Mode + Headless Mode | 代码仓库 + 终端 + 文件系统 + 网络 + MCP Servers | 工业级完整实现 |
| Cursor | Claude / GPT / Gemini | IDE 内文件操作 + 代码搜索 + 运行终端 + Web Search | rules 文件（类似 CLAUDE.md）+ 对话历史 | ReAct | 当前打开的代码仓库 | 跟 IDE 深度集成的 Coding Agent |
| LangChain | 任意 Provider | 抽象 + 开发者自己实现 | 多种后端 | 提供 ReAct 实现 | 开发者自己定义 | 框架，不是 Agent 本身，是"做 Agent 的工具盒" |
| Hermes | 多 Provider | CLI / IM / MCP | 跨会话长期记忆（MEMORY.md + 用户画像 + 全文搜索召回） | ReAct + Subagent 委托 + cron 调度 | CLI + 多个消息平台 + IDE 集成 | 主打"会成长的个人 Agent"，核心创新在 Memory |
| AutoGPT | GPT-4 | 搜索 / 文件 / 浏览器 | 本地向量数据库 | ReAct（当时设计还不成熟） | 本地文件系统 + 浏览器 | 历史价值大于技术价值 |

几个关键判断：

- <span style="color: red; font-weight: bold;">ChatGPT 凑齐五组件才跨过线</span>。经典版只有 LLM 和 Memory，是 chatbot；加了 Code Interpreter、联网、插件之后，Tools / Loop / Environment 补齐，才变成 Agent。同一个产品，跨不跨线就看你给它配了什么。
- <span style="color: red; font-weight: bold;">Claude Code 是工业级完整实现</span>：Tools 系统极其丰富（Skills + Slash Commands + Subagents + MCP），Memory 系统设计精细（CLAUDE.md + AGENTS.md + Skills 三层），Permission System 给 Tools 加事前权限，Hooks 给 Loop 加事后约束。
- <span style="color: red; font-weight: bold;">LangChain 不是 Agent 本身，是"做 Agent 的工具盒"</span>。它提供抽象和工具集，你用它写出来的东西才是 Agent。
- <span style="color: red; font-weight: bold;">Hermes 的核心创新在 Memory</span>：跨会话长期记忆 + 能自动创建新技能、自我改进，让 Agent 越用越能干。Claude Code 的 Memory 是项目级 + 会话级，Hermes 是跨会话长期记忆，这个差异决定两者定位不同——一个是 IDE 内的 coding 工具，一个是个人长期助手。
- <span style="color: red; font-weight: bold;">AutoGPT 历史价值大于技术价值</span>。它让"自主 Agent"在 2023 年破圈，变成业界共识；但设计现在看有不少问题（容易陷入无意义循环、context 管理粗糙、错误恢复弱）。

六个产品千差万别，五组件拆开是同一个东西的不同实现。下次看新 Agent 产品，第一反应问五个问题：**它的 LLM 是什么、Tools 有哪些、Memory 怎么设计、Loop 长什么样、Environment 是什么**。回答完这五个，就读懂了它，也就能判断它到底是真 Agent，还是缺了组件的"类 Agent"。

#### (2) 厘清三个易混概念

<img src="imgs/aicent-v2-09-what-is-agent-technically/807f6d9ffeb743f029284498d4c7ad51_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicent-v2-09-what-is-agent-technically/02aa1a0341b0c610f6d3602b6ca9f622_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!-- 
图片内容说明
路径：imgs/aicent-v2-09-what-is-agent-technically/02aa1a0341b0c610f6d3602b6ca9f622_MD5.jpg
用途：第五章开头，用概念澄清图把 Agent 与 LLM、Chatbot、Workflow、Copilot 等易混概念一次性区分清楚，对应正文 5.1–5.4 几节"界限划清"的核心主张
内容：以网格/矩阵+关系网络的方式列出 Agent 与易混概念的边界——LLM 仅是文字输入输出的无状态模型(CPU/大脑)、Chatbot 是退化了 Tools 和 Loop 只能聊天的形态、Workflow 是预定义步骤的确定性流程(缺 LLM 自主决策)、Copilot 是人主导的辅助式协作；Agent 则是凑齐 LLM+Tools+Memory+Loop+Environment、具备自主决策循环的完整形态。配以图标(大脑/聊天气泡/齿轮等)强化各概念定位，强调"少了自主决策循环就不是 Agent"。
-->

**Agent vs LLM**：LLM 是 Agent 的一个组件，不是 Agent 本身。LLM 输入文字、输出文字，无状态、无工具、无循环；Agent 用 LLM 做决策，但还有 Tools、Memory、Loop、Environment。类比：<span style="color: red; font-weight: bold;">LLM 是 CPU（大脑）</span>，Agent 是装好操作系统加应用程序的整台电脑。没有 CPU 电脑跑不起来，但 CPU 不等于电脑。

**Agent vs Chatbot**：Chatbot 是 Agent 退化掉 Tools 和 Loop 之后的形态，只能聊，不能动手。<span style="color: red; font-weight: bold;">Chatbot 加上 Tools 加 Loop，就变成 Agent</span>——这就是 ChatGPT 进化的路径。

**Agent vs Workflow**（最容易混的一对）：Workflow 是开发者预先定义每一步、按顺序执行的确定性流程，比如"先调 LLM 分类，再调 API 拉数据，再调 LLM 总结"；Agent 是开发者只定义目标和工具、怎么达成由 Agent 自己决定的探索式循环。两者经常混用——Dify 的 Workflow 节点是预定义流程，Dify 的 Agent 节点是 Agent，同一个产品里两种范式都有。

关键判断：<span style="color: red; font-weight: bold;">Workflow 是确定性的，Agent 是探索式的</span>。需要可预测的场景用 Workflow，需要灵活探索的场景用 Agent。前面提到的"每天定时拉天气 API 发邮件"脚本就是 Workflow，因为每一步都写死，没有 LLM 自主决策。

界限划清之后：不是什么能调 API、能聊天、能自动跑的东西都叫 Agent。<span style="color: red; font-weight: bold;">少了"自主决策的循环"这个核，就不是 Agent</span>。这把尺子最大的用处，是让你不再被滥用的"Agent"这个词忽悠。

#### (3) 四道自测题

几道判断题，看看这把尺子有没有真正进到脑子里。

**判断题 1**：以下哪些是 Agent？
- A 没开插件的 ChatGPT 网页版
- B 开了 Agent 模式的 Cursor
- C 你写的每天定时拉天气 API 发到邮箱的脚本
- D AutoGPT
- E Dify 里搭的一个按预定义节点执行的"工作流"

**答案**：B 和 D 是 Agent。A 是 chatbot（缺 Tools、Loop）。C 是 Workflow（缺 LLM 自主决策，每步写死）。E 是 Workflow（预定义流程，不是自主决策）。

**判断题 2**：Claude Code 的 Skills 在五组件抽象里属于哪个组件？

**答案**：Tools 和 Memory 的混合。Skill 本身是"做某件事的方法"（Memory 属性），同时它扩展 Agent 的能力（Tools 属性）。这种混合是 Claude Code 的设计精妙处。

**判断题 3**：Hermes Agent 跟 Claude Code 在五组件上的最大差异是什么？

**答案**：Memory。Claude Code 的 Memory 是项目级（CLAUDE.md）和会话级，Hermes 的 Memory 是跨会话长期记忆 + 自动技能进化。这个差异决定它们的定位——Claude Code 是 IDE 内的 coding 工具，Hermes 是个人长期助手。

**判断题 4**：你公司想做一个"内部代码 review Agent"，五组件你会怎么设计？

**参考答案**：LLM 用强弱双模型（复杂代码用强模型、快速 lint 用快模型）；Tools 配 read_file、查规范、调工单系统、发 review 评论；Memory 用项目级代码风格（CLAUDE.md）+ 历史 review 案例（数据库）；Loop 用标准 ReAct，先 Plan Mode 列 review 大纲；Environment 是代码仓库 + 工单系统 + 内部知识库。

如果你能流畅做出类似设计，五组件抽象就真正在你头脑里了。单个 Agent 讲明白了，但企业真正的问题是稳定跑很多个 Agent。下一章讲 Agent OS 和三层技术栈。

## 6. 从单个 Agent 到 Agent OS 与三层技术栈

<img src="imgs/aicent-v2-09-what-is-agent-technically/2f9d85cac51b949d4cdf405a0e3971a8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!-- 
图片内容说明
路径：imgs/aicent-v2-09-what-is-agent-technically/2f9d85cac51b949d4cdf405a0e3971a8_MD5.jpg
用途：第六章开头，用对比图说清"单个 Agent"和"Agent OS"的差别，以及 Single-Agent 与 Multi-Agent 的差别，铺垫正文 OryxOS 作为 L2 基础设施要解决的问题
内容：横向三栏对比——左栏"Agent OS 做什么"：列出进程管理、工作空间隔离、模型路由、安全沙盒、可观测性等支撑多 Agent 稳定运行的基础设施职责，类比操作系统；中栏"Single-Agent vs Multi-Agent"：Single-Agent 为单一实例、上下文集中、好控制，Multi-Agent 为多 Agent 协作、各有角色(Planner/Researcher/Writer/Reviewer)、独立 context、互相通信，代价是协调成本与一致性问题；右栏"协作模式"：呈现 Agent 间委托、调度、通信等协作形态。整体强调从"跑一个 Agent"升级到"稳定跑很多个 Agent"需要 Agent OS 这层基础设施。
-->

单个 Agent 讲明白后，上升一层。**企业里真正的问题，从来不是"跑一个 Agent"，而是"稳定地跑很多个 Agent"**。本章把横向（Agent OS 能力）和纵向（三层技术栈定位）一起讲清楚。

### 6.1 Agent OS 在解决什么 + Single vs Multi

<img src="imgs/aicent-v2-09-what-is-agent-technically/7d017357e8def87cdb7a3068e290a6f1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Agent 是单个智能体实例——一个 LLM + Tools + Memory + Loop + Environment。Agent OS 是 **支撑很多个 Agent 一起稳定运行的基础设施**，管的是进程管理、工作空间隔离、模型路由、安全沙盒、可观测性。

这个区别跟"一个应用程序"和"操作系统"的区别一模一样。你在电脑上跑一个程序很容易，但要让几十个程序在同一台机器上同时跑、互不干扰、出问题不互相拖垮、还能统一管资源，就需要操作系统。Agent OS 干的是同一类事，只是对象从程序换成了 Agent。

具体来说，Agent OS 要回答前面拆 Agent 时反复点到的那几个组件的"放大版"问题：

| Agent OS 要回答的问题 | 对应单个 Agent 的哪个组件 |
|---|---|
| 进程管理：多个 Agent 同时跑，谁管它们的生命周期（起、停、重启） | Loop（执行） |
| 工作空间隔离 + 沙盒：一个 Agent 跑飞了把环境搞坏，怎么不连累别的 | Environment（安全） |
| 多模型路由：几十个 Agent 都要调模型，怎么统一路由、控成本、切备用 | LLM（决策引擎） |
| 可观测性：怎么知道每个 Agent 此刻在干什么、花了多少钱、出没出错 | 全局（横跨五组件） |

一句话点题：<span style="color: red; font-weight: bold;">Agent 是应用程序，Agent OS 是操作系统</span>。

顺带把 Single vs Multi 也分清。Single-Agent 是一个 Agent 独立完成任务，简单、好控制、上下文集中。Multi-Agent 是多个 Agent 协作完成任务，每个 Agent 有自己的角色（Planner、Researcher、Writer、Reviewer）、自己的 context、互相通信。Multi 的价值是并行（多个 Agent 同时干不同的事）、专业化（每个用不同 prompt 和工具）、上下文隔离（避免单个 context 过长）；代价是协调成本、一致性问题、调试困难。

关键判断：**先做好 Single，再做 Multi**。Multi 不是 Single 的升级，是复杂度高一个数量级的不同问题。而支撑 Multi 稳定运行，恰恰需要 Agent OS 这层基础设施。

### 6.2 三层技术栈

<img src="imgs/aicent-v2-09-what-is-agent-technically/50f9efcc6bfe317335ac3fb65eabd6c0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicent-v2-09-what-is-agent-technically/e6b318de55c1cd6fa4d4b709e812c993_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!-- 
图片内容说明
路径：imgs/aicent-v2-09-what-is-agent-technically/e6b318de55c1cd6fa4d4b709e812c993_MD5.jpg
用途：第七章开头，用分层信息图直观展示 Agent 技术栈的三层结构及对应职业判断，呼应正文 L1/L2/L3 分层与"现在投入 L2、几年后跨到 L3"的职业路径
内容：左侧三层结构——L1 基础模型层（深蓝，Claude/GPT/Gemini/Qwen/Nous Hermes，顶尖 AI 实验室主导、工程师难参与）；L2 Agent OS/运行时层（浅蓝，Hermes Agent/OryxOS/OpenClaw/Claude Code/Cursor/LangChain，标注"2026 最缺人、正在被定义"）；L3 Agent 应用层（浅绿，Coding Agent/客服 Agent/研究 Agent/交易 Agent，门槛最低、需求最广、依赖 L2 成熟）。右侧"职业判断"三块——① L1 卡位极高；② L2 最值得投入（2026–2028 定义窗口期，OryxOS 为关键 L2 产品）；③ L3 大爆发在路上（L2 成熟后爆发）。底部深蓝"职业路径"条带：现在 → L2 OryxOS →（几年后）→ L3 主角。
-->

把 Agent 相关的技术放进一个分层结构，看清楚整体生态：

- **L1 基础模型层**：LLM 本身。Anthropic、OpenAI、Google、Meta、Mistral、阿里、Nous Research 等。
- **L2 Agent 运行时层**：把 LLM 包装成 Agent 的运行时，提供 Tools、Memory、Loop、Environment 的工程实现。
- **L3 Agent 应用层**：基于 Agent 运行时构建的具体应用。一个 Coding Agent 是 App，一个 Customer Service Agent 也是 App。

这个分层跟传统软件栈非常像：**L1 像硬件（CPU、芯片），L2 像操作系统，L3 像应用程序**。这个类比让 Agent OS 的存在意义瞬间清晰：没有操作系统，每个应用程序都要自己处理硬件细节，不可能；没有 Agent OS，每个 Agent App 都要自己处理 LLM 路由、工具管理、Memory 持久化、沙盒安全，不现实。Agent OS 让 Agent App 的开发门槛大幅下降，就像操作系统让应用程序的开发门槛下降一样。这也是 OryxOS 项目重要的原因——它做的是 L2 层基础设施。

三层都缺什么人：

- **L1**：少数顶尖 AI 实验室在卷，普通工程师参与不了。
- **L2**：2026 年最缺工程师的层。大部分企业都在自己造轮子，业界还没形成统一标准。
- **L3**：参与门槛最低，需求最广，几乎每个企业都要 Coding Agent、客服 Agent、研究 Agent。

职业判断：能进 L1 当然好，但 L1 卡位极高。<span style="color: red; font-weight: bold;">L2 是 2026–2028 年最值得进入的层</span>。L3 应用层会随着 L2 成熟而大爆发，现在打 L2 基础，几年后跨到 L3 当主角。这一层正在被定义，现在投入的人，几年后回头看是早期建设者。

讲完抽象和分层，下一章看一眼业界实际在哪，再用一句话收束全文。

## 7. 2026 年业界现状与一句话总结

<img src="imgs/aicent-v2-09-what-is-agent-technically/6f17921826cdc437b25c44198ecc8252_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 7.1 业界现状与收束

#### (1) 2026 年 Agent 技术的真实状态

讲了抽象和分层，看一眼业界实际在哪。

**已经形成的共识**：

- ReAct 是事实标准，所有主流框架的 Loop 都基于它或它的变种。
- MCP 协议在统一 Tools 生态，越来越多框架接入。
- 五组件抽象是默认架构，从 Hermes 到 Claude Code 到 OryxOS，都是 LLM + Tools + Memory + Loop + Environment。

**还在争的**：

1. Agent OS 的形态没统一——Hermes 的"个人持久化"路线、OpenClaw 的"反应式工具使用"路线、OryxOS 希望做的"企业级运行时"路线，三个完全不同方向。
2. Memory 怎么做最优——结构化文件、向量数据库、混合方案，谁都没赢。
3. Multi-Agent 怎么协作——很多论文很多框架，没有事实标准。
4. Agent 安全怎么保证——Permission、Hooks、Sandbox 都是当前方案，但缺统一标准。

**在快速演进的**：

1. 从 Single-Agent 到 Multi-Agent 是 2026–2027 年的明显方向。
2. MCP Server 生态在快速增长，越来越多服务有官方 MCP Server。
3. Agent 的"loop 深度"在增加，能完成的任务从单步到几步到几十步。
4. Permission、Hooks、可观测、成本追踪这些工程能力在每个 Agent OS 里成为标配。

**一个判断**：2026 年是 Agent 技术的"基础设施大爆发期"。L1 模型层已经相对稳定（卡位完成），L3 应用层还没大规模起来（需求被低估），**最缺人、最值得投入的是 L2 Agent OS 层**。

#### (2) 一句话总结

回到开头那句定义：<span style="color: red; font-weight: bold;">Agent 是一个能感知环境、自主决策、执行动作、并基于反馈循环迭代的程序。</span>

具象到 2026 年的工程实现：**Agent = LLM（决策引擎）+ Tools（行动能力）+ Memory（状态承载）+ Loop（循环执行）+ Environment（交互对象），五个组件加一个循环结构**。

这把抽象的尺子有四个价值：

- **判断**：看到新 Agent 产品能立刻拆开看，分辨真假。
- **设计**：要做 Agent 系统知道从哪五个组件入手。
- **比较**：能讲清两个 Agent 产品的本质差异。
- **沟通**：跟同事讨论时知道大家在说同一件事。

而当你把单个 Agent 拆明白，Agent OS 的意义就自然浮出来——它是让很多个 Agent 稳定共存的操作系统，OryxOS 做的就是这一层。
