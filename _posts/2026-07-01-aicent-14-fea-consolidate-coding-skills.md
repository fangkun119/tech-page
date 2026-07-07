---
title: AI编程方法 14：核心功能 - 用 Skill 沉淀编码流程
author: fangkun119
date: 2026-07-01 23:00:00 +0800
categories: [AI编程, 方法论]
tags: [AI编程, AI编程方法论]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-14-fea-consolidate-coding-skills/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---

<!--
aicent-14-fea-consolidate-coding-skills
AI编程方法 14：用 Skill 沉淀编码流程
-->

## 1. 开篇与方法论总览：让 Claude Code 自动遵守流程

本篇是系列第 14 篇。系列第 13 篇完成了 Provider 模块的完整交付——从供应商选型、数据模型设计，到后端 8 个 MVC 任务、前端对接、完整验收，半天交付。在继续做下一个模块（Agent）之前，本篇刻意停下来做两件"沉淀"的事。

第一件事：回头看清楚第 13 篇里真正决定模块质量的是什么。不是代码本身，而是代码之前的那些判断——支持哪些供应商、鉴权信息怎么存、健康状态要不要独立成表。这些判断背后是**领域理解**。

第二件事：第 13 篇的交付流程是固定的——咨询 → 设计 → 拆解 → 执行 → 前端对接 → 验收。后面做 Agent、对话引擎、MCP 接入，每个模块都是这套流程。既然流程固定，就应该把它告诉 Claude Code，让它以后**自动按流程走**——这就是 Skill 机制。

本篇围绕这两件事展开，最后再用 Skill 思维回头重构第 13 篇遗留的 if-else，把方法论和工程实践完整闭环。

### 1.1 结论先行：本篇交付的三件事

<img src="imgs/aicent-14-fea-consolidate-coding-skills/bbfb5d693f7eb85fde4f414c1be8d02f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 介绍

一个完整的 AI 编程模块交付，除了"写代码"这一步之外，还有大量"可沉淀的经验"。本篇用三句话讲清要做的事：

> **领域理解是 AI 编程时代被低估的核心瓶颈，执行成本趋零之后，判断的权重反而大幅提升；Skill 机制把固定流程沉淀成 Claude Code 的操作手册，让 AI 以后自动按经验走；Skill 思维还可以反向推动代码重构，把"加一个供应商"这种重复场景从 if-else 演进为策略结构。**

这三句话拆开是三条主线，每条主线都对应一份方法论：

#### (2) 三条主线拆解

##### ① 领域理解：被低估的瓶颈

Claude Code 写代码的速度极快，第 13 篇 8 个 MVC 任务两三个小时全部交付。但真正决定模块做成什么样的，不是代码，而是代码之前的判断——支持哪些供应商、要不要引入 LangChain4j、鉴权信息怎么存、健康检查怎么做。这些判断没有一个是 Claude Code 替开发者做的：它给选项，开发者做取舍。Claude Code 让执行成本趋近于零之后，领域理解的权重不是降低了，而是大幅提升了。

##### ② Skill 机制：把固定流程变成操作手册

第 13 篇的交付流程是固定的：咨询 → 设计 → 拆解 → 执行 → 前端对接 → 验收。后面做 Agent、对话引擎、MCP，每个模块都是这套流程。Claude Code 提供 Skill 机制，专门解决"把固定流程告诉 AI"这件事。Skill 是写在 Markdown 里的操作手册，触发时让 Claude Code 按流程走。开发者只需要把经验描述清楚，让 AI 帮自己把经验写成 Skill 文件。

##### ③ Skill 思维重构 if-else：经验反过来推动代码

第 13 篇连通性测试里不同供应商的 API 差异用了 if-else 处理，当时刻意保留为"先跑通再优化"。本篇用策略模式重构这段 if-else，并把"加一个新供应商"这个固定流程沉淀成一个小的 provider-adapter Skill。重构后的代码 + 沉淀下来的 Skill 合在一起，构成一份越来越丰富的经验库。

### 1.2 全文导读地图

<img src="imgs/aicent-14-fea-consolidate-coding-skills/f3deac2b018fc3502c0dd3328b789c07_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 章节划分

本篇按"方法论手册 + 实战教材"两部分组织，共 7 章。第一部分（第 1–3 章）提炼方法论，不绑定具体技术栈，可独立速查；第二部分（第 4–7 章）结合 Hify 项目、Spring Boot + MyBatis-Plus + Vue 技术栈、Provider/Agent 模块复现实战过程，解释每个方法论的 why。

<img src="imgs/aicent-14-fea-consolidate-coding-skills/111a576fdc1dd6a5742dc8e259c8b92c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
\`\`\`mermaid
flowchart TD
    Start([系列第 14 篇 通过 Skill 让 Claude 自动遵守流程])

    subgraph P1[第一部分 方法论手册 第 1-3 章 速查风]
        C1[第 1 章 开篇与方法论总览]
        C2[第 2 章 领域快速理解方法论]
        C3[第 3 章 Skill 机制方法论]
    end

    subgraph P2[第二部分 实战教材 第 4-7 章 问题驱动]
        C4[第 4 章 实战开篇 领域理解落地]
        C5[第 5 章 实战 Skill 驱动开发落地]
        C6[第 6 章 实战 用 Skill 思维重构 if-else]
        C7[第 7 章 总结与思考]
    end

    Start --\> P1
    C1 --\> C2
    C2 --\> C3
    Start --\> P2
    C4 --\> C5
    C5 --\> C6
    C6 -\> C7
\`\`\`
-->

#### (2) 各章定位与读法

##### ① 第 1 章 全文导读与方法论总览（本章）

开篇定位、三条主线拆解、全文导读地图、可裁剪的领域四问 + Skill 沉淀综合 Check List。读完本章即可对本篇建立全局认知。

##### ② 第 2 章 领域快速理解方法论

抽象提炼"领域快速理解四问"方法论：为什么领域理解是 AI 编程时代的核心瓶颈；从外到内的四个提问（是什么 / 用在哪里 / 由什么组成 / 技术架构怎样）；提问框架表 + 速查 Check List。这一章不绑定具体技术栈，任意陌生领域都适用。

##### ③ 第 3 章 Skill 机制方法论

抽象提炼 Skill 三步法：Skill 是什么、与 CLAUDE.md 的区别、三步让 AI 帮你掌握并沉淀 Skill（让 Claude Code 教你 / 告诉你业界最佳实践 / 帮你写）；Skill 是活文档、大 Skill 与小 Skill 配合；本章 Check List。

##### ④ 第 4 章 实战开篇——领域理解落地

实战部分开篇。把领域四问落到 Dify/Hify 领域实战：每一问如何支撑产品定位、模块优先级、功能取舍、架构选型这四类设计决策。复用第 13 篇的实战结论作为佐证。

##### ⑤ 第 5 章 实战·Skill 驱动开发落地

把 Skill 三步法落到 Hify 项目：让 Claude Code 写 module-delivery Skill（含 review 三要点 + Skill 完整内容）；让 Claude Code 写 provider-adapter Skill（含 Skill 完整内容）；用 Skill 启动 Agent 模块的实际效果对比；Skill 迭代补充跨模块依赖、SSE 流式响应等场景。

##### ⑥ 第 6 章 实战·用 Skill 思维重构 if-else

把第 13 篇连通性测试遗留的 if-else 用策略模式重构（含提示词）；重构后加新供应商只需两步；把"加新供应商"沉淀成 provider-adapter 小 Skill；大小 Skill 配合形成经验库。

##### ⑦ 第 7 章 总结与思考

双方法论回顾：领域四问 + Skill 沉淀；"脏活累活适合 AI"的方法论升华；思考；承接系列下一篇 Agent 模块的具体实现。

### 1.3 领域四问 + Skill 沉淀综合 Check List（可裁剪速查）

下面这份 Check List 提炼自本篇两条主线，供项目进入新模块阶段快速查阅。按两组组织，条目精炼、可裁剪：做简单模块时跳过不适用的条目，做复杂模块时补全团队规范条目。

#### (3) 综合两组 Check List

##### ① 组一：领域快速理解（进入新模块前）

- [ ] 已用四问对目标领域做过一次系统性盘点：是什么 / 用在哪里 / 由什么组成 / 技术架构
- [ ] 每一问都翻译成了"会支撑哪个设计决策"
- [ ] 已区分"业界主流做法"与"自己项目的约束"，没有照抄业界方案
- [ ] 已用一两小时建立 70% 的领域认知，剩下 30% 靠亲手用产品 + 翻文档补
- [ ] 还没有让 Claude Code 写一行业务代码——这一步留给 Skill 沉淀之后

##### ② 组二：Skill 沉淀（每个模块交付后）

- [ ] 已识别本模块交付过程中"每次都要做、每次都差不多"的固定流程
- [ ] 已让 Claude Code 教过自己 Skill 是什么、与 CLAUDE.md 的区别
- [ ] 已让 Claude Code 列过业界 Skill 用法，知道 Skill 不止"开发流程"一种
- [ ] 已让 Claude Code 把自己的实际流程写成 Skill 文件，放在 `.claude/skills/` 目录
- [ ] 已对生成的 Skill 做过三要点 review：产出物明确 / 决策点标注 / 坑写进去
- [ ] 已在实际跑一遍 Skill 后，把发现的新场景补进 Skill（活文档迭代）
- [ ] 已区分大 Skill（覆盖通用模块开发）与小 Skill（覆盖特定场景操作），两者配合

这份 Check List 不是教条，而是"最小可沉淀"的提醒清单。把它贴在每个模块的复盘会议上方，对照执行即可避免最常见的遗漏：领域没理解清楚就写代码、流程每次手动描述一遍、踩过的坑不沉淀下次重复踩。

## 2. 领域快速理解方法论

**本章目标**：把"动手做新模块前先做一次领域梳理"这件事从一句口号，变成一套可复用的提问方法论。读完本章，读者面对任意陌生业务领域，都能用四个固定问题在一两小时内建立 70% 的认知，支撑后续的产品定义与架构决策。本章不绑定具体技术栈，订单系统、监控平台、数据管道都适用。

### 2.1 为什么领域理解是 AI 编程时代的核心瓶颈

<img src="imgs/aicent-14-fea-consolidate-coding-skills/aa21d07b7f3d7e5015daf5a2fdefc810_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 执行成本趋零之后，判断的权重上升

##### ① 代码不再是瓶颈，判断才是

回头看第 13 篇，Claude Code 写代码极快——8 个任务两三个小时全部交付。但真正决定这个模块做成什么样的，不是代码，而是代码之前的那些判断：

- 一期支持哪些供应商
- 要不要引入 LangChain4j
- 鉴权信息怎么存
- 健康检查怎么做

这些判断没有一个是 Claude Code 替开发者做的。它给选项，开发者做取舍。Claude Code 让执行成本趋近于零之后，**领域理解的权重不是降低了，而是大幅提升了**。

##### ② 判断做反的代价会被代码放大

如果这些判断做反了会怎样？

| 反向判断 | 代价 |
|---|---|
| 引入 LangChain4j | 多了一层重依赖和学习成本 |
| 鉴权信息用固定列存 | 后面加新供应商就得改表 |
| 健康状态放在 Provider 表里 | 高频探测和业务读竞争锁 |

每个选择单独看都有道理，合在一起就是一个越来越难维护的系统。**取舍的依据是什么？是对这个领域的理解**。领域理解不到位，再快的代码交付也只是更快地交付了一个更难维护的系统。

##### ③ 好消息：领域知识可以靠提问快速补齐

领域知识不是只能靠经验慢慢积累。Claude Code 本身就是一个极好的领域学习工具，关键是**要问对问题**。下一节给出的"领域快速理解四问"，就是把"问对问题"这件事变成一套可复用的方法论。

#### (2) 领域理解在标准交付流程中的位置

领域理解不是可有可无的前置步骤，而是整个交付流程的第一环。它的产出物——一份带着判断的领域认知——直接决定了后面咨询模式提问的维度、数据模型设计的取舍、技术选型的依据。

```text
领域快速理解（是什么 / 用在哪里 / 由什么组成 / 技术架构）
    ↓
咨询模式想清楚（选型 / 数据模型 / 设计决策）
    ↓
按层拆解任务（Entity → DTO → Service → Controller）
    ↓
逐步实现验证
    ↓
前端对接
    ↓
完整验收
```

第一环没做好，后面每一环都会被前面的认知盲区拖累。这也是为什么本系列把"领域快速理解"单独列为一章方法论的缘故。

### 2.2 领域快速理解四问

进入一个新领域时，最朴素的提问是"XX 是什么"。这种问题 AI 也能答，但答案的信息密度很低——一长段名词解释，看完之后提问者依然不知道该怎么设计架构。

真正高信息密度的做法，是按一个固定结构、**从外到内**地问四个问题。每一问都对应一种特定的认知目的，也对应一类后续的设计决策。

#### (1) 核心动作：从外到内的四问

下面这张图概括了四问的整体结构：

<img src="imgs/aicent-14-fea-consolidate-coding-skills/5a44640393e19b8f67d88dd4f5fb4d4e_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-14-fea-consolidate-coding-skills/5a44640393e19b8f67d88dd4f5fb4d4e_MD5.jpg
用途：领域快速理解四问的结构示意图
内容：从外到内展示四个提问层次——① 它是什么、解决什么问题；② 用在哪里、什么场景；③ 由什么组成、哪些是必要的；④ 技术架构是怎样的。每一问对应一种认知目的，层层递进。
-->

##### ① 第一问：它是什么，解决什么问题

**认知目的**：建立认知框架。

回答"它是什么"，本质上是在做一个产品定位。例如 Dify 是 AI 应用开发平台，让不会写代码的人也能搭建 AI 应用——这决定了它是平台型产品，不是开发者工具。这一问的产出，是后面所有产品定义的起点。

##### ② 第二问：用在哪里，什么场景

**认知目的**：理解优先级。

回答"用在哪里"，本质上是在做需求排序。例如企业用 Dify 主要做智能客服、内部知识问答、文档处理——这决定了对话能力和工具接入是刚需，工作流编排是进阶需求。第 13 篇之所以从 Provider 开始做，就是因为理解了场景之后知道：没有模型管理，后面所有功能都没有基础。

##### ③ 第三问：由什么组成，哪些是必要的

**认知目的**：支撑功能取舍。

回答"由什么组成"，本质上是在做功能裁剪。例如 Claude Code 列出 Dify 的模型管理、Agent、工作流、RAG、对话、工具接入等模块。追问"哪些是必须有的"，它就能帮开发者区分核心和外围。功能取舍直接基于这一步。

##### ④ 第四问：技术架构是怎样的

**认知目的**：支撑架构决策。

回答"技术架构"，**不是为了照抄**，而是为了理解它为什么这么选，然后根据自己的约束做不同选择。例如了解到 Dify 后端是 Python + Flask，用 Celery 做异步任务——理解它为什么这么选之后，再结合本项目团队是 Java 背景、模块化单体更匹配当前规模，于是选 Spring Boot 模块化单体。架构选型是在理解了业界方案复杂度之后的判断。

#### (2) 四问的执行要点

##### ① 一两个小时建立 70% 的认知

四个问题从外到内走一遍，一两个小时就能建立 70% 的领域认知。剩下 30% 靠亲手用一下产品、翻一下文档来补。**不要试图一次性把领域知识全部吃透**，70% 足以支撑产品定义和架构决策，剩下的认知会在实战中逐步补齐。

##### ② 四问不只适用于 Dify

这套方法不只适用于 Dify。要做订单系统、监控平台、数据管道，都是同样的路径——先用四问建立全局认知，再进入产品定义和架构设计。拿到任何一个陌生项目，先跑一轮四问，就知道该做什么、不做什么、先做什么。

##### ③ 提问时显式要求"带依据"

四问每一问都可以补一句"为什么"的追问。例如"技术架构是怎样的，它为什么这么选"。加这一句，AI 的回答会从"事实陈述"变成"因果链"，而因果链才是后续做架构决策的直接依据。

### 2.3 领域提问框架表

<img src="imgs/aicent-14-fea-consolidate-coding-skills/89c60934a5d4487dcecaa5194198473d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把本章方法论浓缩成一张速查表，做新业务模块时可以直接对照执行：

| 提问 | 认知目的 | 直接产出 | 支撑的设计决策 |
|---|---|---|---|
| 它是什么，解决什么问题 | 建立认知框架 | 产品定位（平台型 / 工具型 / 服务型） | 产品定义、用户画像、商业模式 |
| 用在哪里，什么场景 | 理解优先级 | 场景清单 + 刚需 vs 进阶分类 | 模块优先级、MVP 范围 |
| 由什么组成，哪些是必要的 | 支撑功能取舍 | 模块清单 + 核心 vs 外围分类 | 功能裁剪、迭代节奏 |
| 技术架构是怎样的 | 支撑架构决策 | 业界方案 + 选型理由 | 技术选型、模块化方式、异步策略 |

四问合起来覆盖了从产品定义到架构决策的完整链路。配套的可复用提问模板如下：

```text
进入一个新领域 XX，请按以下四个问题帮我做一次系统性梳理：
1. 它是什么，解决什么问题？（建立认知框架，给出产品定位）
2. 用在哪里，什么场景？（理解优先级，区分刚需和进阶）
3. 由什么组成，哪些是必要的？（支撑功能取舍，区分核心和外围）
4. 技术架构是怎样的？（支撑架构决策，说明业界主流方案和选型理由）

每个问题请同时给出"为什么这样设计"的因果链，便于我做后续判断。
```

### 2.4 本章 Check List

进入一个新领域前，对照以下清单逐项确认。任何一项没做完，都不要急着进入咨询模式：

#### (3) 本章 Check List

##### ① 认知层

- [ ] 已用四问对目标领域做过一次系统性盘点（不是漫无目的地问"XX 是什么"）
- [ ] 每一问都已翻译成"支撑哪个设计决策"，不是停留在事实陈述
- [ ] 已对业界方案追问过"为什么这样设计"，拿到了因果链
- [ ] 已区分"业界主流做法"与"自己项目的约束"，没有照抄业界方案

##### ② 节奏层

- [ ] 已用一到两小时完成 70% 的领域认知建立
- [ ] 已识别剩下 30% 的认知盲区，计划在实战中补齐
- [ ] 还没有让 Claude Code 写一行业务代码——这一步留给咨询模式

##### ③ 复用层

- [ ] 四问的方法论已迁移到至少一个非 Dify 的领域（订单系统 / 监控平台 / 数据管道等）做过验证
- [ ] 已形成自己的"领域提问模板"，可以套用到下一个陌生项目

清单全部打勾之后，再进入第 3 章的 Skill 机制方法论。领域理解的产出（一份带着判断的认知），会成为 Skill 沉淀的输入——因为 Skill 写的就是"在某个领域里，这类任务该怎么做"。

## 3. Skill 机制方法论

**本章目标**：把"Skill 机制"从一句功能介绍，变成一套可复用的方法论。读完本章，读者能清晰回答三个问题：Skill 是什么、它和 CLAUDE.md 有什么区别、如何让 Claude Code 帮自己把经验沉淀成 Skill。本章不绑定具体技术栈，适用于任何重复性的开发流程。

### 3.1 Skill 是什么

<img src="imgs/aicent-14-fea-consolidate-coding-skills/806423ce906f38e5403b8b19567250d6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

回到第 13 篇的交付过程。整个流程是固定的：咨询 → 设计 → 拆解 → 执行 → 前端对接 → 验收。后面做 Agent、对话引擎、MCP，每个模块都是这五步。既然流程固定，每次都手动给 Claude Code 描述一遍就是浪费。

Claude Code 提供 Skill 机制，专门解决这个问题。Skill 是 `.claude/skills/` 目录下的 Markdown 文件，**定义特定任务的标准操作流程**。可以把它理解为给 Claude Code 的"操作手册"。

```text
Skill = .claude/skills/{name}.md
        └── Markdown 文件，描述"这类任务怎么做"
        └── 引用时生效，按文件中定义的流程执行
```

一句话概括：**Skill 就是按格式定规范、在 Markdown 里写好规范、让 Claude Code 去识别执行**。

### 3.2 Skill 与 CLAUDE.md 的区别

Skill 和 CLAUDE.md 都是把规范告诉 Claude Code 的方式，但作用范围完全不同。下面这张图把两者的区别讲清楚：

<img src="imgs/aicent-14-fea-consolidate-coding-skills/f1386caf3a5d940485e9dd6c1b32fd4e_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-14-fea-consolidate-coding-skills/f1386caf3a5d940485e9dd6c1b32fd4e_MD5.jpg
用途：Skill 与 CLAUDE.md 的对比示意图
内容：从作用范围、加载时机、内容粒度三个维度对比两者——CLAUDE.md 是全局规范（什么规矩要遵守）、每次对话自动加载、定义 Controller 只做参数校验这类通用规矩；Skill 是具体任务的操作手册（这类任务怎么做）、引用时才生效、定义"做一个新模块应该先梳理需求再拆解再执行"这类具体流程。
-->

把两者的差异浓缩成对比表：

| 维度 | CLAUDE.md | Skill |
|---|---|---|
| 作用范围 | 全局规范 | 具体任务流程 |
| 加载时机 | 每次对话自动加载 | 引用时才生效 |
| 内容粒度 | 通用规矩（"Controller 只做参数校验"） | 具体流程（"做新模块先梳理需求再拆解再执行"） |
| 触发方式 | 始终生效 | 提到 Skill 名字时生效 |
| 文件位置 | 项目根目录 `CLAUDE.md` | `.claude/skills/{name}.md` |

CLAUDE.md 定义的是"什么规矩要遵守"，Skill 定义的是"这类任务怎么做"。两者配合使用：CLAUDE.md 保证所有代码都符合通用规范，Skill 保证特定任务按流程走。

### 3.3 Skill 三步法：让 Claude Code 教你、告诉你、帮你写

<img src="imgs/aicent-14-fea-consolidate-coding-skills/bf8590cb8d6703dde35cd80dbf57a402_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

与其人工去翻文档学 Skill 的用法，不如让 Claude Code 自己教。掌握 Skill 不需要走"读官方文档"这条路径，按下面三步走，一两小时就能从陌生到会用。

#### (1) 第一步：让 Claude Code 教你 Skill 是什么

提示词是：

```text
Claude Code 的 Skill 机制是什么？怎么创建 Skill、怎么使用、Skill 文件放在哪里？和 CLAUDE.md 有什么区别？请详细解释，给我举个例子。
```

Claude Code 会告诉你：Skill 是 `.claude/skills/` 目录下的 Markdown 文件，定义特定任务的标准操作流程，可以理解为给 Claude Code 的"操作手册"。使用方式是给 Claude Code 指令时提到 Skill 的名字，它就会按 Skill 定义的流程执行。

##### ① 不需要去翻文档学 Skill 的用法

看，**不需要去翻文档学 Skill 的用法，问 Claude Code 它就教你了**。这和系列第 10 篇的咨询模式一模一样——不懂就问，它见过的项目比开发者多。所以，把 AI 当作工具，也把 AI 当作老师。

#### (2) 第二步：让 Claude Code 告诉你别人怎么用 Skill

提示词是：

```text
业界用 Claude Code Skill 的最佳实践有哪些？大家一般用 Skill 解决什么问题？给我列举一些常见的 Skill 类型和使用场景。
```

Claude Code 给的回答会打开视野——原来 Skill 不只能写开发流程。每做一个模块、踩一个坑，Skill 库就丰富一点。业界常见的 Skill 类型可以归纳为五大类：

<img src="imgs/aicent-14-fea-consolidate-coding-skills/d8bff91708268b205ce96aa29300d15d_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-14-fea-consolidate-coding-skills/d8bff91708268b205ce96aa29300d15d_MD5.jpg
用途：Skill 业界最佳实践分类示意图
内容：把业界常见的 Skill 用法归纳为五大类——开发流程类（新模块交付 / API 接口开发 / 数据库变更）、质量保障类（代码审查 checklist / 单测规范 / 安全检查）、运维部署类（发布上线 / 环境搭建 / 故障排查）、文档生成类（API 文档 / 变更日志 / 技术方案模板）、Git 工作流类（commit message 规范 / 分支管理 / PR 审查标准）。
-->

##### ① 五大类 Skill 速览

| 类别 | 典型 Skill | 解决的问题 |
|---|---|---|
| 开发流程类 | 新模块交付流程、API 接口开发流程、数据库变更流程 | 把开发节奏标准化，就是第 13 篇干的事 |
| 质量保障类 | 代码审查 checklist、单元测试编写规范、安全检查清单 | 每次写 Service 方法都检查：入参校验 / 异常处理 / 缓存失效 / 日志 |
| 运维部署类 | 发布上线流程、环境搭建流程、故障排查流程 | 把运维经验固化，新人也能按流程操作 |
| 文档生成类 | API 文档生成、变更日志生成、技术方案模板 | 每次写文档不用从零开始，Skill 定义了结构和必填项 |
| Git 工作流类 | commit message 规范、分支管理流程、PR 审查标准 | 把团队协作规范固化 |

这些不需要现在全做，但知道别人怎么用，**对 Skill 的理解会从"一个功能"变成"一种工作方式"**。

##### ② Skill 的本质：把经验编码化

Skill 的本质是把经验编码化——开发者踩过的坑、总结的流程、做过的判断，全部固化成文字，让 Claude Code 以后自动按经验走。

#### (3) 第三步：让 Claude Code 帮你写 Skill

知道了 Skill 是什么、别人怎么用，现在让 Claude Code 帮你写。提示词如下（以把第 13 篇 Provider 模块的交付流程沉淀成 module-delivery Skill 为例）：

```text
我刚完成了 Hify 项目 Provider 模块的开发，流程是这样的：

1. 先用咨询模式梳理了供应商选型、数据模型设计、边界问题
2. 数据模型确定后更新了 schema.sql
3. 后端按 MVC 分层拆解：Entity+Mapper → DTO → Service（CRUD+ 连通性测试 + 模型同步 + 健康检查）→ Controller
4. 每步编译或 curl 验证通过再进下一步
5. 前端对接：创建 API 文件，把 mock 数据源换成真实 API
6. 完整验收：后端 curl + 浏览器全流程

帮我把这个流程沉淀成一个 Skill 文件，放在.claude/skills/module-delivery.md。要求：每一步有明确的产出物和验证方式，关键决策点标注"等待用户确认"，把我踩过的坑写成注意事项。
```

##### ① 写 Skill 提示词的三个要点

| 要点 | 说明 | 反例 → 正例 |
|---|---|---|
| 描述实际流程 | 把自己真实走过的步骤原样描述，不要提炼过度 | "做了模块开发" → "先咨询 → 更新 schema → 按 MVC 拆解 → 前端对接 → 验收" |
| 指定产出物与验证方式 | 每一步要明确"做完是什么样、怎么算做完" | "做需求分析" → "产出需求分析文档，包含功能范围、数据模型 DDL、设计决策及理由" |
| 标注踩过的坑 | 实战中遇到过的坑要原样写进去 | 通用模板 → 加上"Entity 的 JSON 字段必须用 TypeHandler、schema.sql 要同步更新" |

第三步生成的 Skill 内容是本篇实战部分的核心资产，完整内容留在第 5 章展开。

### 3.4 Skill 是活文档

第一版 Skill 不会完美。

用模块交付 Skill 做了 Agent 模块，可能发现 Skill 里没提"跨模块依赖怎么处理"——Agent 依赖 Provider 和 MCP，这在第 13 篇做 Provider 时没遇到。把这个补进 Skill。

做了对话引擎，可能发现 Skill 里的后端拆解不适用于流式响应场景，需要加一条"如果涉及 SSE 流式响应，Service 层用 SseEmitter + llmExecutor"。补进去。

Skill 和 CLAUDE.md 一样是**活文档**。系列第 2 篇说的 SDD 闭环——定规范 → AI 执行 → 发现问题 → 迭代规范——在 Skill 上同样适用。每做一个模块，Skill 就更完善一点。做到第四五个模块的时候，Skill 已经覆盖了绝大多数场景，Claude Code 几乎不需要额外指导就能按标准交付。

### 3.5 大 Skill 与小 Skill 配合

Skill 不只是大流程，小流程也可以沉淀。这两类 Skill 配合使用，构成一份越来越丰富的经验库：

| Skill 类型 | 覆盖范围 | 典型例子 |
|---|---|
| 大 Skill | 通用的模块开发流程 | `module-delivery.md`（咨询 → 拆解 → 对接 → 验收） |
| 小 Skill | 特定场景的操作步骤 | `provider-adapter.md`（加新供应商的两步流程） |

大 Skill 保证整个团队的模块开发节奏一致，小 Skill 保证特定场景下的操作不漏步。两者配合，`.claude/skills/` 目录就是一个团队的经验库——新人来了引用 Skill 就能按团队标准交付，不需要口口相传。

### 3.6 本章 Check List

每个模块交付后做一次 Skill 沉淀，对照以下清单逐项确认：

#### (4) 本章 Check List

##### ① 概念层

- [ ] 已让 Claude Code 教过自己 Skill 是什么、与 CLAUDE.md 的区别
- [ ] 已让 Claude Code 列过业界 Skill 用法，知道五大类 Skill 各自解决的问题
- [ ] 已理解 Skill 的本质是"把经验编码化"，不是"一个 AI 功能"

##### ② 沉淀层

- [ ] 已识别本模块交付过程中"每次都要做、每次都差不多"的固定流程
- [ ] 已让 Claude Code 把自己的实际流程写成 Skill 文件，放在 `.claude/skills/`
- [ ] 已对生成的 Skill 做过三要点 review：产出物明确 / 决策点标注 / 坑写进去
- [ ] 已在实际跑一遍 Skill 后，把发现的新场景补进 Skill（活文档迭代）

##### ③ 配合层

- [ ] 已区分大 Skill（覆盖通用模块开发）与小 Skill（覆盖特定场景操作）
- [ ] 已把团队规范（CLAUDE.md）与具体流程（Skill）配合使用，不混淆
- [ ] `.claude/skills/` 目录已有至少一个 Skill 文件，团队经验开始沉淀

清单全部打勾之后，Skill 机制就从"概念"变成了"团队工作方式"。本篇第二部分实战章节会演示这条路径在 Hify 项目上完整走一遍的过程。

## 4. 实战开篇：领域理解落地

**本章定位**：第二部分（实战教材）的开篇。第一部分用三章把方法论讲透了，从本章起进入 Hify 项目实战，把方法论落到具体场景上。本章把第 2 章的"领域快速理解四问"在 Dify/Hify 领域上完整跑一遍，让读者看到四问如何在一两个小时内产出一份带着判断的领域认知，直接支撑后续的产品定义和架构决策。

### 4.1 结论先行：四问在 Hify 上的产出

<img src="imgs/aicent-14-fea-consolidate-coding-skills/f52a8dff1226c44470a962cf694a4e59_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

四问合起来回答了开发者动手做 Hify 之前的全部认知问题。可以浓缩成一句结论：

> **四问走一遍，开发者从"对 Dify 一无所知"变成"知道 Hify 该做什么、不该做什么、先做什么、用什么技术栈做"，这份认知直接成为第 13 篇 Provider 模块和后续 Agent、对话引擎、MCP 模块的全部输入。**

下面四节按四问逐个展开，每一问都说明：问了什么、得到什么、支撑了哪个设计决策。

### 4.2 第一问落地：Hify 是什么

#### (1) 咨询过程

开发者动手做 Hify 之前，用 Claude Code 跑了一轮系统性的领域梳理。不是漫无目的地问"Dify 是什么"，而是按固定结构、从外到内去问。第一问就是建立认知框架：

```text
Dify 是什么？它解决什么问题？请给出产品定位、目标用户、典型用法。
```

#### (2) 得到的产出

Claude Code 的回答是：**Dify 是 AI 应用开发平台，让不会写代码的人也能搭建 AI 应用**。

#### (3) 支撑的设计决策

这个回答直接决定了 Hify 的产品定位——它是**平台型产品**，不是开发者工具。这一决策影响了后面三件事：

##### ① 产品形态

平台型产品意味着 Hify 要面向更广泛的用户群（不止工程师），UI 要做得更友好，配置项要尽量少。

##### ② 功能边界

平台型产品意味着 Hify 不会自己训练模型，而是接入第三方 LLM；不会自己实现 Agent 框架，而是封装业界主流方案。

##### ③ 商业模式

平台型产品意味着 Hify 的核心价值是"降低 AI 应用门槛"，不是"提供最强模型能力"——后者是 LLM 厂商的事。

### 4.3 第二问落地：Hify 用在哪里

#### (1) 咨询过程

```text
企业用 Dify 主要做什么场景？哪些是刚需，哪些是进阶？请按使用频率排序。
```

#### (2) 得到的产出

Claude Code 列出的主流场景是：智能客服、内部知识问答、文档处理。这三类是刚需；工作流编排是进阶需求。

#### (3) 支撑的设计决策

这一问直接决定了模块优先级：

| 场景类别 | 决策 | 理由 |
|---|---|---|
| 对话能力（智能客服 / 知识问答） | 一期必做 | 刚需，没有对话能力整个平台就失去意义 |
| 工具接入（文档处理 / 外部 API） | 一期必做 | 刚需，Agent 调外部能力靠它 |
| 模型管理（Provider） | 一期第一优先 | 支撑对话和工具调用的底层基础 |
| 工作流编排 | 二期再做 | 进阶需求，先打基础再上复杂功能 |

第 13 篇之所以从 Provider 开始做，就是因为这一问得出来的结论：**没有模型管理，后面所有功能都没有基础**。这是场景理解直接决定模块优先级的典型例子。

### 4.4 第三问落地：Hify 由什么组成

<img src="imgs/aicent-14-fea-consolidate-coding-skills/e5a0995658abd6cd1d7a755cb1a52a0e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 咨询过程

```text
Dify 由哪些核心模块组成？哪些是必须有的，哪些是可选的？请区分核心和外围。
```

#### (2) 得到的产出

Claude Code 列出 Dify 的核心模块：模型管理、Agent、工作流、RAG、对话、工具接入。其中模型管理、Agent、对话、工具接入是核心；工作流、RAG 是进阶。

#### (3) 支撑的设计决策

这一问支撑了功能取舍——决定了 Hify 一期做什么、二期做什么：

##### ① 一期核心模块（基于"必要"判断）

- Provider（模型管理）：所有上层能力的依赖
- Agent：调用模型 + 工具完成任务
- 对话引擎：发起流式 LLM 调用
- MCP 接入：标准化工具调用协议

##### ② 二期进阶模块（基于"可选"判断）

- 工作流编排：复杂多步任务
- RAG：知识库增强

功能取舍直接基于这一步——**没有"哪些是必要的"这个判断，功能取舍就成了拍脑袋**。

### 4.5 第四问落地：Hify 的技术架构

#### (1) 咨询过程

```text
Dify 的技术架构是怎样的？后端用什么、前端用什么、异步任务怎么处理？它为什么这么选？
```

#### (2) 得到的产出

Claude Code 给出 Dify 的技术栈：后端 Python + Flask，用 Celery 做异步任务；前端 React。

#### (3) 支撑的设计决策

这一问**不是为了照抄 Dify 的技术栈**，而是为了理解它为什么这么选，然后根据自己的约束做不同选择：

| 维度 | Dify 的选择 | Hify 的选择 | 决策依据 |
|---|---|---|---|
| 后端语言 | Python + Flask | Java + Spring Boot | 团队是 Java 背景，复用现有基础设施 |
| 架构形态 | 微服务 | 模块化单体 | 当前规模不需要微服务的复杂度 |
| 异步任务 | Celery | `@Scheduled` + 线程池 | Spring Boot 原生方案够用 |
| 前端 | React | Vue 3 + Element Plus | 团队 Vue 技术栈，Element Plus 生态成熟 |

Hify 选 Spring Boot 模块化单体，就是在理解了 Dify 架构复杂度之后的判断——**不是业界怎么做就照抄，而是回到自己的约束做选择**。

### 4.6 四问的方法论升华

四问合起来，一两个小时建立 70% 的领域认知，剩下 30% 靠亲手用一下产品、翻一下文档来补。这套方法的价值不在结论本身，而在**它把"领域学习"从"靠经验慢慢积累"变成了"靠提问快速建立认知"**。

#### (1) 四问适用于任意陌生项目

这套方法不只适用于 Dify。要做订单系统、监控平台、数据管道，都是同样的路径——先用四问建立全局认知，再进入产品定义和架构设计。拿到任何一个陌生项目，先跑一轮四问，就知道该做什么、不做什么、先做什么。

#### (2) 四问的产出是 Skill 沉淀的输入

四问产出的"带着判断的领域认知"，会成为下一章 Skill 沉淀的输入——因为 Skill 写的就是"在某个领域里，这类任务该怎么做"。领域理解不到位，写出来的 Skill 就是空中楼阁；领域理解到位，Skill 才能精准描述真实流程。

第 13 篇的 Provider 模块就是在四问产出的领域认知之上完成的：选 Spring Boot 单体、用 JSON 存鉴权、从 Provider 开始做、一期支持四种类型——每一个决策都能追溯到四问中的某一问。下一篇进入 Skill 沉淀，把第 13 篇走过的固定流程固化下来。

## 5. 实战·Skill 驱动开发落地

**本章定位**：把第 3 章的 Skill 三步法方法论落到 Hify 项目实战。本章按"写 Skill → 跑 Skill → 迭代 Skill"的因果链展开，让读者看到 Skill 机制如何把第 13 篇走过的固定流程固化成 Claude Code 的操作手册，并立刻在 Agent 模块上兑现价值。

本章包含本篇最重要的两份资产——`module-delivery` Skill 和 `provider-adapter` Skill 的完整内容。两份 Skill 的内容作为 code block 原样保留，开发者可以直接复制到自己的 `.claude/skills/` 目录使用。

### 5.1 让 Claude Code 写 module-delivery Skill

<img src="imgs/aicent-14-fea-consolidate-coding-skills/a9820a83e42a1c9825d3d2fb4e1f7cf6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第 3 章第三步提到，让 Claude Code 帮开发者把第 13 篇的模块交付流程沉淀成 Skill。提示词如下（code block 原样保留）：

```text
我刚完成了 Hify 项目 Provider 模块的开发，流程是这样的：

1. 先用咨询模式梳理了供应商选型、数据模型设计、边界问题
2. 数据模型确定后更新了 schema.sql
3. 后端按 MVC 分层拆解：Entity+Mapper → DTO → Service（CRUD+ 连通性测试 + 模型同步 + 健康检查）→ Controller
4. 每步编译或 curl 验证通过再进下一步
5. 前端对接：创建 API 文件，把 mock 数据源换成真实 API
6. 完整验收：后端 curl + 浏览器全流程

帮我把这个流程沉淀成一个 Skill 文件，放在.claude/skills/module-delivery.md。要求：每一步有明确的产出物和验证方式，关键决策点标注"等待用户确认"，把我踩过的坑写成注意事项。
```

#### (1) Claude Code 生成第一版，开发者 review

Claude Code 会生成第一版 Skill。但**第一版通常不完美**，开发者需要 review。review 的重点有三个：

##### ① 产出物是否明确

不是"做需求分析"就完了，而是"产出需求分析文档，包含功能范围、数据模型 DDL、设计决策及理由"。Claude Code 需要知道做到什么程度算完——产出物不明确，AI 就会在中途自己判断"差不多了"，结果产出质量不稳定。

##### ② 决策点是否标注

数据模型设计完、后端做完准备做前端之前——这些是开发者要拍板的地方。Skill 里要写"等待用户确认后再进入下一步"。决策点没标注，AI 会一路狂奔做完整个模块，等到开发者发现方向错了已经晚了。

##### ③ 踩过的坑有没有写进去

第 13 篇实际踩过的坑（Entity 的 JSON 字段必须用 TypeHandler、schema.sql 要同步更新、前端对接时要更新路由配置）要原样写进 Skill。这些坑写进去，下次就不会重复踩；不写进去，每个新模块都要重新踩一遍。

#### (2) module-delivery Skill 的完整内容

review 完让 Claude Code 改，改完就是第一个正式 Skill。完整的 Skill 内容如下（code block 原样保留，开发者可以拿着这份内容去问 AI 是什么意思）：

```text
# Skill: 新增 Provider Adapter

触发方式：当用户说"接入新供应商"、"新增 XX 提供商支持"、"加一个 Adapter" 时按此流程推进。

---

## 背景

Provider 的连通性测试、模型同步、调用逻辑按供应商类型有差异。
最初用 switch-case 实现，后来重构为策略模式：

\```
ProviderAdapterFactory
  └── Map<ProviderType, ProviderAdapter>
        ├── OpenAiAdapter        (OPENAI / OPENAI_COMPATIBLE / DEEPSEEK)
        ├── AnthropicAdapter     (ANTHROPIC)
        ├── AzureOpenAiAdapter   (AZURE_OPENAI)
        └── OllamaAdapter        (OLLAMA)
\```

每个 Adapter 实现统一接口，Factory 按类型路由，新增供应商只需加一个 Adapter 类 + 注册，不改任何已有代码。

---

## Step 1 — 分析目标供应商 API

**目标**：搞清楚接入该供应商需要哪些差异化实现。

需要调研的问题（逐一回答）：

| 问题 | 说明 |
|------|------|
| 认证方式 | Bearer Token / API Key Header / 双 Header / 无认证？ |
| 列模型接口 | URL 路径？返回结构（`data[]` / `models[]` / 其他）？ |
| 必填 authConfig 字段 | 如 `apiKey`、`apiVersion`、`anthropicVersion` |
| baseUrl 默认值 | 官方默认是什么？用户可否自定义？ |
| 特殊请求头 | 如 Anthropic 的 `anthropic-version` |
| Chat 调用路径 | `/v1/chat/completions` 还是其他？ |
| 流式响应格式 | SSE `data: {...}` 标准格式，还是自定义格式？ |

**产出物**：一份简短的 API 特征说明（口头或注释均可）

> ⚠️ **等待用户确认**：API 特征分析结果确认后再写代码

---

## Step 2 — 实现 Adapter

**目标**：新建一个实现 `ProviderAdapter` 接口的类。

**接口定义**（位于 `hify-provider/.../adapter/ProviderAdapter.java`）：

```java
public interface ProviderAdapter {
    /** 该Adapter支持的供应商类型（可多个） */
    List<String> supportedTypes();

    /** 连通性测试，返回延迟和模型数 */
    ConnectionTestResult test(Provider provider, OkHttpClient testClient);

    /** 拉取并返回模型列表（用于同步） */
    List<String> listModels(Provider provider, OkHttpClient client);

    /** 构造chat请求体（流式） */
    RequestBody buildChatRequest(Provider provider, List<ChatMessage> messages);

    /** 解析流式响应的一行delta文本，无内容返回null */
    String parseDelta(String line);
}
\```

**文件位置**：`hify-provider/src/main/java/com/hify/provider/adapter/impl/XxxAdapter.java`

**实现模板**：

```java
@Component
public class XxxAdapter implements ProviderAdapter {

    private final ObjectMapper objectMapper;

    @Override
    public List<String> supportedTypes() {
        return List.of("XXX");
    }

    @Override
    public ConnectionTestResult test(Provider provider, OkHttpClient testClient) {
        long start = System.currentTimeMillis();
        try {
            String apiKey = getAuth(provider, "apiKey");
            String url = provider.getBaseUrl().stripTrailing() + "/v1/models";
            Map<String, String> headers = Map.of("Authorization", "Bearer " + apiKey);

            String body = llmHttpClient.get(url, headers, testClient);
            int latency = (int) (System.currentTimeMillis() - start);
            int modelCount = parseDataArraySize(body);
            return ConnectionTestResult.ok(latency, modelCount);
        } catch (LlmApiException e) {
            return ConnectionTestResult.fail(e.getMessage());
        } catch (Exception e) {
            return ConnectionTestResult.fail("测试异常：" + e.getMessage());
        }
    }

    // ... 其他方法
}
\```

**注意事项**：
- `getAuth(provider, key)` 找不到字段时抛 `IllegalArgumentException("authConfig 缺少字段：" + key)`，会被上层统一捕获，不要吞掉
- 解析模型列表时不同供应商返回字段不同：OpenAI 是 `data[].id`，Ollama 是 `models[].name`，Anthropic 是 `data[].id`
- 流式解析：OpenAI 格式每行是 `data: {...}`，遇到 `data: [DONE]` 停止；Anthropic 是 `data: {"type":"content_block_delta",...}`
- 有特殊 Header 的（如 Anthropic `anthropic-version`）放在 authConfig 里，不要硬编码版本号

**验证**：

\```bash
mvn clean install -DskipTests -pl hify-provider -am
\```

---

## Step 3 — 注册到 Factory

**目标**：让 Factory 能路由到新 Adapter。

**Factory 实现**（位于 `hify-provider/.../adapter/ProviderAdapterFactory.java`）：

```java
@Component
public class ProviderAdapterFactory {

    private final Map<String, ProviderAdapter> adapterMap;

    // Spring自动注入所有ProviderAdapter实现
    public ProviderAdapterFactory(List<ProviderAdapter> adapters) {
        this.adapterMap = new HashMap<>();
        for (ProviderAdapter adapter : adapters) {
            for (String type : adapter.supportedTypes()) {
                adapterMap.put(type.toUpperCase(), adapter);
            }
        }
    }

    public ProviderAdapter get(String type) {
        ProviderAdapter adapter = adapterMap.get(type.toUpperCase());
        if (adapter == null) {
            throw new BizException(ErrorCode.PROVIDER_TYPE_NOT_SUPPORTED);
        }
        return adapter;
    }
}
\```

**注册方式**：新 Adapter 加 `@Component` 注解，`supportedTypes()` 返回对应的类型字符串，Factory 在启动时自动扫描注册，**无需手动修改 Factory 代码**。

**验证**：启动后在日志里确认 adapterMap 包含新类型（可在 Factory 构造方法加一行 log）。

---

## Step 4 — 更新 ProviderType 枚举（如需要）

如果新供应商需要在前端下拉菜单里出现，同步更新：

- 后端：`hify-provider/.../constant/ProviderType.java`（如果有枚举）
- 前端：`hify-web/src/views/provider/ProviderList.vue` 的 `providerTypes` 数组
- 数据库：`provider.type` 是 varchar，无需迁移，直接用新字符串值

---

## Step 5 — 验证

### 后端 curl 验证
```bash
# 1. 创建新供应商
curl -s -X POST http://localhost:8080/api/v1/providers \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "测试-XXX",
    "type": "XXX",
    "baseUrl": "https://api.xxx.com",
    "authConfig": { "apiKey": "sk-test-xxx" }
  }' | jq .

# 2. 连通性测试（id替换为上一步返回的id）
curl -s -X POST http://localhost:8080/api/v1/providers/1/test-connection | jq .
\```

**预期**：
- 真实 key：`success: true`，有 `latencyMs` 和 `modelCount`
- 假 key：`success: false`，`errorMessage` 包含"无效"或"认证失败"，**不能是 500**

### 浏览器验证
1. 前端下拉能选到新类型
2. 创建后列表显示正常
3. 点"测试"按钮有结果提示

---

## 常见坑

| 现象                    | 原因                                             | 修复                                          |
| --------------------- | ---------------------------------------------- | ------------------------------------------- |
| Factory 找不到新 Adapter  | 忘加 `@Component` 或 `supportedTypes()` 返回值大小写不一致 | Factory 用 `toUpperCase()` 统一，Adapter 返回值也大写 |
| authConfig 字段缺失导致 500 | 前端创建时没传必填的 auth 字段                             | `getAuth()` 的异常信息要明确说缺哪个字段                  |
| 连通性测试超时               | 用了默认 OkHttpClient（无超时限制）                       | 必须用注入的 `testClient`（10s 超时），不要 new          |
| 模型数量永远是 0             | 响应结构解析错误（字段名不是 `data`）                         | 用 `objectMapper.readTree(body)` 打印原始结构再解析   |
| 流式响应乱码/截断             | Anthropic 等有自己的 SSE 事件类型，直接用 OpenAI 解析逻辑会漏掉    | `parseDelta()` 按各供应商格式单独实现                  |
|                       |                                                |                                             |
```

### 5.2 让 Claude Code 写 provider-adapter Skill

module-delivery 是覆盖通用模块开发流程的大 Skill。除了大 Skill，还可以沉淀覆盖特定场景操作步骤的小 Skill。第 13 篇连通性测试里不同供应商的 API 差异用了 if-else，后来用策略模式重构——"加一个新供应商"就成了一套固定流程。把这套流程也写成 Skill，提示词如下：

```text
第13篇的连通性测试里，不同供应商的 API 差异用了 if-else，后来我用策略模式重构了。以后加新供应商的流程是固定的：分析 API → 实现 Adapter → 注册到 Factory → 验证。帮我把这个流程也写成 Skill，放在.claude/skills/provider-adapter.md。
```

#### (1) provider-adapter Skill 的完整内容

`provider-adapter.md` 的完整内容如下（code block 原样保留）：

```text
# Skill: 新增 Provider Adapter

触发方式：当用户说"接入新供应商"、"新增 XX 提供商支持"、"加一个 Adapter" 时按此流程推进。

---

## 背景

Provider 的连通性测试、模型同步、调用逻辑按供应商类型有差异。
最初用 switch-case 实现，后来重构为策略模式：

\```
ProviderAdapterFactory
  └── Map<ProviderType, ProviderAdapter>
        ├── OpenAiAdapter        (OPENAI / OPENAI_COMPATIBLE / DEEPSEEK)
        ├── AnthropicAdapter     (ANTHROPIC)
        ├── AzureOpenAiAdapter   (AZURE_OPENAI)
        └── OllamaAdapter        (OLLAMA)
\```  

每个 Adapter 实现统一接口，Factory 按类型路由，新增供应商只需加一个 Adapter 类 + 注册，不改任何已有代码。

---

## Step 1 — 分析目标供应商 API

**目标**：搞清楚接入该供应商需要哪些差异化实现。

需要调研的问题（逐一回答）：

| 问题 | 说明 |
|------|------|
| 认证方式 | Bearer Token / API Key Header / 双 Header / 无认证？ |
| 列模型接口 | URL 路径？返回结构（`data[]` / `models[]` / 其他）？ |
| 必填 authConfig 字段 | 如 `apiKey`、`apiVersion`、`anthropicVersion` |
| baseUrl 默认值 | 官方默认是什么？用户可否自定义？ |
| 特殊请求头 | 如 Anthropic 的 `anthropic-version` |
| Chat 调用路径 | `/v1/chat/completions` 还是其他？ |
| 流式响应格式 | SSE `data: {...}` 标准格式，还是自定义格式？ |

**产出物**：一份简短的 API 特征说明（口头或注释均可）

> ⚠️ **等待用户确认**：API 特征分析结果确认后再写代码

---

## Step 2 — 实现 Adapter

**目标**：新建一个实现 `ProviderAdapter` 接口的类。

**接口定义**（位于 `hify-provider/.../adapter/ProviderAdapter.java`）：

\```java
public interface ProviderAdapter {
    /** 该Adapter支持的供应商类型（可多个） */
    List<String> supportedTypes();

    /** 连通性测试，返回延迟和模型数 */
    ConnectionTestResult test(Provider provider, OkHttpClient testClient);

    /** 拉取并返回模型列表（用于同步） */
    List<String> listModels(Provider provider, OkHttpClient client);

    /** 构造chat请求体（流式） */
    RequestBody buildChatRequest(Provider provider, List<ChatMessage> messages);

    /** 解析流式响应的一行delta文本，无内容返回null */
    String parseDelta(String line);
}
\```

**文件位置**：`hify-provider/src/main/java/com/hify/provider/adapter/impl/XxxAdapter.java`

**实现模板**：

\```java
@Component
public class XxxAdapter implements ProviderAdapter {

    private final ObjectMapper objectMapper;

    @Override
    public List<String> supportedTypes() {
        return List.of("XXX");
    }

    @Override
    public ConnectionTestResult test(Provider provider, OkHttpClient testClient) {
        long start = System.currentTimeMillis();
        try {
            String apiKey = getAuth(provider, "apiKey");
            String url = provider.getBaseUrl().stripTrailing() + "/v1/models";
            Map<String, String> headers = Map.of("Authorization", "Bearer " + apiKey);

            String body = llmHttpClient.get(url, headers, testClient);
            int latency = (int) (System.currentTimeMillis() - start);
            int modelCount = parseDataArraySize(body);
            return ConnectionTestResult.ok(latency, modelCount);
        } catch (LlmApiException e) {
            return ConnectionTestResult.fail(e.getMessage());
        } catch (Exception e) {
            return ConnectionTestResult.fail("测试异常：" + e.getMessage());
        }
    }

    // ... 其他方法
}
\```

**注意事项**：
- `getAuth(provider, key)` 找不到字段时抛 `IllegalArgumentException("authConfig 缺少字段：" + key)`，会被上层统一捕获，不要吞掉
- 解析模型列表时不同供应商返回字段不同：OpenAI 是 `data[].id`，Ollama 是 `models[].name`，Anthropic 是 `data[].id`
- 流式解析：OpenAI 格式每行是 `data: {...}`，遇到 `data: [DONE]` 停止；Anthropic 是 `data: {"type":"content_block_delta",...}`
- 有特殊 Header 的（如 Anthropic `anthropic-version`）放在 authConfig 里，不要硬编码版本号

**验证**：
\```bash
mvn clean install -DskipTests -pl hify-provider -am
\```

---

## Step 3 — 注册到 Factory

**目标**：让 Factory 能路由到新 Adapter。

**Factory 实现**（位于 `hify-provider/.../adapter/ProviderAdapterFactory.java`）：

```java
@Component
public class ProviderAdapterFactory {

    private final Map<String, ProviderAdapter> adapterMap;

    // Spring自动注入所有ProviderAdapter实现
    public ProviderAdapterFactory(List<ProviderAdapter> adapters) {
        this.adapterMap = new HashMap<>();
        for (ProviderAdapter adapter : adapters) {
            for (String type : adapter.supportedTypes()) {
                adapterMap.put(type.toUpperCase(), adapter);
            }
        }
    }

    public ProviderAdapter get(String type) {
        ProviderAdapter adapter = adapterMap.get(type.toUpperCase());
        if (adapter == null) {
            throw new BizException(ErrorCode.PROVIDER_TYPE_NOT_SUPPORTED);
        }
        return adapter;
    }
}
\```

**注册方式**：新 Adapter 加 `@Component` 注解，`supportedTypes()` 返回对应的类型字符串，Factory 在启动时自动扫描注册，**无需手动修改 Factory 代码**。

**验证**：启动后在日志里确认 adapterMap 包含新类型（可在 Factory 构造方法加一行 log）。

---

## Step 4 — 更新 ProviderType 枚举（如需要）

如果新供应商需要在前端下拉菜单里出现，同步更新：

- 后端：`hify-provider/.../constant/ProviderType.java`（如果有枚举）
- 前端：`hify-web/src/views/provider/ProviderList.vue` 的 `providerTypes` 数组
- 数据库：`provider.type` 是 varchar，无需迁移，直接用新字符串值

---

## Step 5 — 验证

### 后端 curl 验证
```bash
# 1. 创建新供应商
curl -s -X POST http://localhost:8080/api/v1/providers \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "测试-XXX",
    "type": "XXX",
    "baseUrl": "https://api.xxx.com",
    "authConfig": { "apiKey": "sk-test-xxx" }
  }' | jq .

# 2. 连通性测试（id替换为上一步返回的id）
curl -s -X POST http://localhost:8080/api/v1/providers/1/test-connection | jq .
\```

**预期**：
- 真实 key：`success: true`，有 `latencyMs` 和 `modelCount`
- 假 key：`success: false`，`errorMessage` 包含"无效"或"认证失败"，**不能是 500**

### 浏览器验证
1. 前端下拉能选到新类型
2. 创建后列表显示正常
3. 点"测试"按钮有结果提示

---

## 常见坑

| 现象 | 原因 | 修复 |
|------|------|------|
| Factory 找不到新 Adapter | 忘加 `@Component` 或 `supportedTypes()` 返回值大小写不一致 | Factory 用 `toUpperCase()` 统一，Adapter 返回值也大写 |
| authConfig 字段缺失导致 500 | 前端创建时没传必填的 auth 字段 | `getAuth()` 的异常信息要明确说缺哪个字段 |
| 连通性测试超时 | 用了默认 OkHttpClient（无超时限制） | 必须用注入的 `testClient`（10s 超时），不要 new |
| 模型数量永远是 0 | 响应结构解析错误（字段名不是 `data`） | 用 `objectMapper.readTree(body)` 打印原始结构再解析 |
| 流式响应乱码/截断 | Anthropic 等有自己的 SSE 事件类型，直接用 OpenAI 解析逻辑会漏掉 | `parseDelta()` 按各供应商格式单独实现 |
```

#### (2) 两个 Skill 写完之后的核心洞察

写完两个 Skill 之后，开发者会发现一个核心事实：**Skill 就是写 Markdown 文档**。Skill 就是按格式定规范，在 Markdown 里写好规范，让 Claude Code 去识别执行。

没有什么神秘的——把流程写清楚、把产出物写明确、把坑写进去，就是一份合格的 Skill。这个认知一旦建立，团队沉淀 Skill 的速度就会显著加快。

### 5.3 实际跑一遍：用 Skill 启动 Agent 模块

<img src="imgs/aicent-14-fea-consolidate-coding-skills/cfe3a8ec57607de4c83810d8b52e70bc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Skill 写好了，当场验证。给 Claude Code 的提示词如下：

```text
按模块交付 Skill 的流程，帮我做 Agent 管理模块。先从第一步开始，梳理 Agent 模块的需求和数据模型。
```

#### (1) Claude Code 读到 Skill 后的行为

Claude Code 读到 Skill 后，自动按流程走。它会问开发者：

- Agent 模块的核心功能是什么
- 涉及哪些关联（绑定模型、绑定工具）
- 数据模型怎么设计

然后给出需求分析文档，标注"等待用户确认"。

#### (2) 有 Skill vs 无 Skill 的效果对比

| 维度 | 无 Skill 时 | 有 Skill 时 |
|---|---|---|
| 提示词长度 | 手写一大段指令描述整个流程 | 一句话引用 Skill 名字 |
| 流程一致性 | 每次描述可能漏步或顺序不同 | 引用同一份 Skill，产出结构稳定 |
| 团队协作 | 不同人产出质量差异大 | 同一份 Skill 保证团队标准统一 |
| 经验沉淀 | 经验只在个人脑子里 | 经验固化在 Skill 文件里 |

##### ① Skill 保证了流程一致性

不管开发者自己做还是团队里其他人做，引用同一个 Skill，产出的代码结构和质量标准是一样的。这是 Skill 机制对团队协作的根本价值。

##### ② Skill 让开发者只需要在关键决策点拍板

开发者的经验在 Skill 里积累，Claude Code 按经验走，开发者只需要在关键决策点拍板——需求确认、数据模型确认、前后端切换确认。其他执行细节都由 Skill 驱动 Claude Code 自动完成。

本篇不展开 Agent 模块的具体实现（那是系列下一篇的内容），但读者已经看到了 Skill 驱动开发的效果：**一句话启动一个新模块**。

### 5.4 Skill 也需要迭代

最后提一点：第一版 Skill 不会完美。Skill 和 CLAUDE.md 一样是**活文档**，需要在使用中持续迭代。

#### (1) 两个典型的迭代场景

##### ① 跨模块依赖场景

用模块交付 Skill 做了 Agent 模块，可能发现 Skill 里没提"跨模块依赖怎么处理"——Agent 依赖 Provider 和 MCP，这在第 13 篇做 Provider 时没遇到。把这个补进 Skill：

```text
如果新模块依赖其他模块（如 Agent 依赖 Provider、MCP），先在 Step 0 做一次依赖梳理：
- 列出所有依赖的模块及其当前可用的接口
- 确认依赖模块的数据模型与本模块的关联字段
- 标注哪些依赖接口已经存在、哪些需要本模块推动依赖模块先补齐
```

##### ② SSE 流式响应场景

做了对话引擎，可能发现 Skill 里的后端拆解不适用于流式响应场景。补一条进 Skill：

```text
如果涉及 SSE 流式响应，Service 层用 SseEmitter + llmExecutor：
- Controller 返回 SseEmitter 而非 Result
- Service 层用 llmExecutor 异步发起流式调用，通过 emitter.send() 推送每个 delta
- 流式响应不进 @Cacheable 缓存（流不能被缓存）
```

#### (2) 迭代的节奏

每做一个模块，Skill 就更完善一点。做到第四五个模块的时候，Skill 已经覆盖了绝大多数场景，Claude Code 几乎不需要额外指导就能按标准交付。这就是 Skill 作为活文档的真实价值——**它和团队的经验一起成长**。

## 6. 实战·用 Skill 思维重构 if-else

**本章定位**：把第 13 篇连通性测试里刻意保留的 if-else 用策略模式重构。这一章是第 13 篇第 8 章末尾承诺的兑现——"下一篇用设计模式重构 if-else"。本章按"遗留问题 → 重构 → 沉淀"的问题驱动结构展开，最后把"加一个新供应商"这个固定流程沉淀成小的 provider-adapter Skill，与第 5 章的大 Skill（module-delivery）配合，构成一份完整的经验库。

### 6.1 第 13 篇遗留的 if-else

<img src="imgs/aicent-14-fea-consolidate-coding-skills/da3489ceab9401c9e327047db388bf11_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第 13 篇连通性测试里，不同供应商的 API 差异用了 if-else 处理。当时的决策是"先跑通，下一篇重构"——这是刻意保留的：

```text
先跑通再优化
    ↓
if-else 处理供应商差异（第 13 篇）
    ↓
跑通之后用策略模式重构（本篇）
```

为什么不一开始就用策略模式？因为**在基础设施尚未稳定的阶段过早抽象，往往会让设计模式绑定到尚未成型的业务流程上**。先跑通一遍真实场景，等业务流程稳定之后再重构，抽象出来的设计模式才经得起后续扩展。

### 6.2 用策略模式重构

重构本身不复杂——用策略模式替代 if-else。提示词如下（code block 原样保留）：

```text
重构 Provider 模块的连通性测试。当前是 if-else 按 type 分发，改成策略模式：

定义 ProviderAdapter 接口：testConnection(provider)、listModels(provider)

实现四个适配器：OpenAiAdapter、AnthropicAdapter、OllamaAdapter、OpenAiCompatibleAdapter（和 OpenAiAdapter 共用逻辑）

创建 ProviderAdapterFactory：根据 provider.type 返回对应的 Adapter 实例

ProviderService 里的 if-else 替换为 factory.getAdapter(provider.getType()).testConnection(provider)

OpenAiCompatibleAdapter 直接继承 OpenAiAdapter，不需要额外代码
```

这个指令不展开分析输出——因为重要的是**这个问题和指令本身**。这条提示词示范了"重构一段代码"的标准提问方式：先描述现状（if-else 按 type 分发），再描述目标（策略模式 + Factory），再描述每个组件的职责（接口定义、四个适配器、Factory 路由），最后描述接入点（ProviderService 里的 if-else 替换）。

### 6.3 重构后加新供应商的两步流程

重构完之后，思考一个问题：**以后加新供应商（比如 Gemini），流程是什么？**

答案是两步：

#### (1) 第一步：写一个 GeminiAdapter

```text
@Component
public class GeminiAdapter implements ProviderAdapter {
    @Override
    public List<String> supportedTypes() {
        return List.of("GEMINI");
    }

    @Override
    public ConnectionTestResult test(Provider provider, OkHttpClient testClient) {
        // Gemini 特有的认证方式（URL 参数 ?key=xxx）
        // Gemini 特有的接口路径（/v1/models）
        // Gemini 特有的响应结构（models[].name）
    }

    // ... 其他方法
}
```

#### (2) 第二步：在 Factory 里注册

```text
Factory 在启动时自动扫描所有 @Component 的 ProviderAdapter 实现
新 Adapter 加上 @Component 注解、supportedTypes() 返回 "GEMINI"
Factory 在构造方法里把它注册到 adapterMap
不需要修改任何已有代码
```

就这两步。**不需要改任何已有代码**——这就是策略模式重构的回报。对比重构前的 if-else 方案：每加一家供应商，都要在 ProviderService 的连通性测试方法里加一个 else if 分支，方法越来越长、分支越来越多、测试覆盖越来越难。

### 6.4 把"加新供应商"沉淀成 provider-adapter 小 Skill

<img src="imgs/aicent-14-fea-consolidate-coding-skills/a9d918a5afb1095ee28dcfb2e056d3bd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这个"加新供应商"的流程也可以沉淀成 Skill。第 5 章已经展示了 `provider-adapter.md` 的完整内容——它就是这份小 Skill 的全文。

下面这张图展示了 provider-adapter Skill 沉淀下来的完整结构：

<img src="imgs/aicent-14-fea-consolidate-coding-skills/395131eab33c789c06b72683e62bf2f7_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-14-fea-consolidate-coding-skills/395131eab33c789c06b72683e62bf2f7_MD5.jpg
用途：provider-adapter Skill 沉淀后的文件结构图
内容：展示 provider-adapter.md 作为小 Skill 的完整结构——触发方式、背景（策略模式架构）、Step 1 分析目标供应商 API、Step 2 实现 Adapter、Step 3 注册到 Factory、Step 4 更新 ProviderType 枚举、Step 5 验证、常见坑表格。开发者不需要写一行代码，Skill 就沉淀下来了。
-->

如上图所示，开发者不用去写一行代码，这个 Skill 也沉淀下来了。**Skill 不只是大流程，小流程也可以沉淀**。

### 6.5 大 Skill + 小 Skill 配合形成经验库

大的 `module-delivery` Skill 覆盖通用的模块开发流程，小的 `provider-adapter` Skill 覆盖特定场景的操作步骤。两者配合，`.claude/skills/` 目录就是一个越来越丰富的经验库：

```text
.claude/skills/
  ├── module-delivery.md     大 Skill：通用模块开发流程
  │   └── 咨询 → 拆解 → 对接 → 验收
  ├── provider-adapter.md    小 Skill：加新供应商
  │   └── 分析 API → 实现 Adapter → 注册 Factory → 验证
  ├── (未来) agent-delivery.md    小 Skill：交付 Agent 模块
  ├── (未来) sse-stream.md        小 Skill：SSE 流式响应
  └── (未来) mcp-tool.md          小 Skill：接入 MCP 工具
```

#### (1) 大小 Skill 的分工

| Skill 类型 | 覆盖范围 | 典型例子 | 触发场景 |
|---|---|---|---|
| 大 Skill | 通用的模块开发流程 | `module-delivery.md` | 启动任意新模块 |
| 小 Skill | 特定场景的操作步骤 | `provider-adapter.md` | 加新供应商、加新 MCP 工具、加新 Agent 类型 |

#### (2) 经验库的长期价值

积累下来，团队的经验库会越来越丰富。新人来了引用 Skill 就能按团队标准交付，不需要口口相传；老人离开了，经验留在 Skill 里不会流失。**这才是 Skill 机制对团队的根本价值——把个人经验变成团队资产**。

## 7. 总结与思考

<img src="imgs/aicent-14-fea-consolidate-coding-skills/221a574bdd2da0ffc21c9e568f18d701_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇做了三件事：讲清楚领域理解的重要性、让 Claude Code 教开发者 Skill 并帮开发者写 Skill、用策略模式重构 if-else 并沉淀适配 Skill。后半段看似工程量大，真正的价值却集中在前半段的方法论沉淀——领域四问和 Skill 沉淀，构成了本篇最值得带走的两条主线。

### 7.1 双方法论回顾：领域四问 + Skill 沉淀

#### (1) 方法论一：领域快速理解四问

进入一个陌生领域时用——是什么、用在哪里、由什么组成、技术架构怎样。一两个小时建立 70% 的认知，支撑产品定义和架构决策。

##### ① 四问的产出

| 提问 | 产出 | 支撑的决策 |
|---|---|---|
| 是什么 | 产品定位 | 产品形态、功能边界、商业模式 |
| 用在哪里 | 场景清单 + 刚需分类 | 模块优先级、MVP 范围 |
| 由什么组成 | 模块清单 + 核心分类 | 功能取舍、迭代节奏 |
| 技术架构 | 业界方案 + 选型理由 | 技术选型、模块化方式 |

四问合起来覆盖了从产品定义到架构决策的完整链路。这套方法不只适用于 Dify——订单系统、监控平台、数据管道都适用。

#### (2) 方法论二：Skill 沉淀

不需要开发者自己研究 Skill 怎么写——让 Claude Code 教开发者概念、告诉开发者别人怎么用、帮开发者把经验写成 Skill 文件。开发者只需要做两件事：

##### ① 把实际流程描述给 Claude Code

把真实走过的步骤原样描述，不要提炼过度。Skill 写的是"在某个领域里这类任务怎么做"，描述越具体，Skill 越精准。

##### ② Review 生成的 Skill 是否准确

review 的三要点：产出物是否明确 / 决策点是否标注 / 踩过的坑有没有写进去。三要点全部确认之后，Skill 才能进 `.claude/skills/` 目录。

Skill 也是活文档——每做一个模块就迭代一次，越来越完善。做到第四五个模块时，Skill 已经覆盖绝大多数场景，Claude Code 几乎不需要额外指导就能按标准交付。

### 7.2 "脏活累活适合 AI"的方法论升华

从方法论上总结，本篇的核心思路是：**让开发者的思考模板化，把模板化的事情交给 AI**。

#### (1) AI 带来的最大效率提升

AI 给开发者带来的最大效率提升，不是它能写多复杂的代码，而是**它能帮开发者做很多模板化的事情**。有一个经验值得刻意记住：

> **脏活累活，开发者嫌烦、觉得没有技术含量的，都是适合 AI 做的。**

这条经验可以拆成三层：

##### ① 重复的流程

写一个新模块、加一个新供应商、做一轮代码审查——这些流程每次都差不多，交给 Skill 驱动的 Claude Code 自动走。

##### ② 模板化的代码

Entity / Mapper / DTO / Controller 这类标准模板代码，开发者只关心业务字段，其他由基础组件和 Claude Code 自动生成。

##### ③ 繁琐的验证

curl 联调、报错修复、跑测试——这些验证步骤繁琐但必要，交给 Claude Code 自动跑、自动修。

#### (2) 开发者该做什么

脏活累活交给 AI 之后，开发者的精力集中在三件事上：

- **领域理解**：通过四问建立认知，判断 AI 给的选项里哪个匹配当前场景
- **关键决策**：在 Skill 标注的决策点拍板，对架构方向负责
- **经验沉淀**：把每次踩的坑、做的判断、走过的流程写成 Skill，让下次更轻松

这才是 AI 编程时代开发者该做的事——**不是写更多代码，而是写更少代码、做更多判断、沉淀更多经验**。

### 7.3 思考：选一个重复流程写成 Skill

回顾开发者过去做项目的经验，有没有哪些"每次都要做、每次都差不多"的流程？比如：

- 接入一个新的第三方 SDK
- 搭建一个新的微服务
- 做一轮性能测试
- 上线一个新版本
- 排查一类生产故障

#### (1) 练习步骤

##### ① 选一个流程

从上面的清单或自己的经验里选一个最熟悉的重复流程。

##### ② 写成 Skill

Skill 应该覆盖：

- 第一步做什么、第二步做什么
- 每一步的输出是什么
- 怎么验证这一步做完了
- 踩过的坑写进注意事项
- 关键决策点标注"等待确认"

##### ③ 实际跑一遍

写完之后让 Claude Code 按这个 Skill 执行一次，看看效果如何。如果 Skill 有遗漏或错误，迭代修正——Skill 是活文档，越用越准。

这个练习的价值不在最终产出的 Skill 本身，而在**练习把隐性经验显性化的过程**。这个过程熟练之后，团队的经验沉淀速度会显著加快。

### 7.4 承接系列下一篇

本篇建立的"领域四问 → Skill 沉淀 → Skill 驱动开发"的方法论，会在下一篇继续复用，并叠加"完整模块实现"这一新的环节。

下一篇正式进入 Agent 模块的具体实现——用第 5 章沉淀的 module-delivery Skill 启动 Agent 模块，从需求分析到数据模型设计，从后端拆解到前端对接，从单测到完整验收。读者会明显感觉到节奏变快了——不是因为 Claude Code 变聪明了，而是开发者的经验在 Skill 里持续积累，Claude Code 按开发者的经验走，开发者只需要在关键决策点拍板。

从下一篇开始，每个模块都用 Skill 驱动开发。第 13 篇建立的"想清楚 → 拆解 → 对接 → 验收"流程，叠加本篇的"Skill 沉淀"环节，构成完整的 AI 编程工作流——**这套工作流会贯穿系列后续所有模块的交付**。

