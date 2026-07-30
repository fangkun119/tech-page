---
title: 传统项目迁AI 07：了解项目 - 老项目改造画什么图、怎么画
author: fangkun119
date: 2026-07-04 07:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-07-proj-rd-02-drawing-tool/cover.jpg
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
aicmigr-07-proj-rd-02-drawing-tool
传统项目迁AI 07：了解项目 - 老项目改造画什么图、怎么画
-->

## 1. 开篇：为什么 AI 编程工具需要画图能力

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/7f1e573dc215abb5dee01cc45b259d12_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 1.1 一张图的价值：AI 出厂只给源码

做传统软件的工程师都懂一张图的价值：接手老项目第一周，你画的不是代码，是架构图、ER 图、时序图。<span style="color: red; font-weight: bold;">这些图是团队沟通的硬通货，比文字描述高效十倍。</span>

可你打开 Claude Code 让它"画一张项目架构图"，它默认只回你一段 Mermaid 源码——你得自己粘到 mermaid.live 里渲染。

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/bf265a032ed40b2ba2959d3e2f2db8dc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-07-proj-rd-02-drawing-tool/bf265a032ed40b2ba2959d3e2f2db8dc_MD5.jpg
用途：展示 Claude Code 默认行为——只输出 Mermaid 源码、不渲染图片
内容：Claude Code 对话截图，AI 返回一段 mermaid 代码块，没有给出渲染后的图片
-->

<span style="color: red; font-weight: bold;">这不是工具的缺陷，是能力没启用。</span>装上对应的 SKILL 之后，它能直接输出 PNG / SVG / PDF，还能在浏览器里实时预览。本篇解决两件事：

| 问题 | 答案 |
|------|------|
| 怎么让 Claude Code 直接出图？ | 装 claude-mermaid 这个 SKILL，两步命令搞定 |
| 老项目改造该画什么图？ | 四类必备图（架构/模块依赖/时序/ER），每类都给提示词模板和坑 |

读完这一篇，你能给手上的 AI 编程工具装好画图能力，并知道什么场景该画什么图。

### 1.2 SKILL：Claude Code 的能力扩展点

你可能会问：为什么不直接让 Claude Code 内置画图？答案和传统 IDE 一样——核心保持精简，能力通过插件扩展。Claude Code 的"插件机制"叫 **SKILL**。

可以把它类比成 VS Code 的扩展、IntelliJ 的 Plugin、或者你熟悉的 SDD 工具链里的 Harness——<span style="color: red; font-weight: bold;">核心 runtime 不变，外挂能力按需加载。</span>

落到目录上，Claude Code 启动时会扫描两个位置：

```text
~/.claude/skills/         # 用户级，所有项目共享
<项目根>/.claude/skills/  # 项目级，仅当前项目生效
```

<span style="color: red; font-weight: bold;">扫到的每个 SKILL 都会被加载进上下文，成为 Claude Code 的"知识库"。</span>一个 SKILL 就是一个文件夹，核心是 `SKILL.md`——它告诉 Claude Code 这个 SKILL 干什么、什么时候用、调用什么工具。

### 1.3 安装 SKILL 的两种方式与获取渠道

SKILL 怎么装、去哪找，一张表说清。

**安装方式：**

| 方式 | 命令 | 适用场景 |
|------|------|----------|
| **Plugin 方式** | `/plugin marketplace add <github-repo>` 然后 `/plugin install` | 自动处理依赖、配置 MCP server，最省事 |
| **手动 clone** | `git clone` 到 `~/.claude/skills/` 或项目 `.claude/skills/`，重启 Claude Code | 透明、可控、便于阅读和修改 SKILL 内部 |

**获取渠道：**

| 渠道 | 入口 |
|------|------|
| 官方 marketplace | Claude Code 里执行 `/plugin marketplace list` 查看官方收录 |
| GitHub 社区 | 搜索 `claude-code skills` 或 `claude skill`，社区项目众多 |
| 第三方聚合站 | `skillsdirectory.com`、`fastmcp.me` 等，收录可用 SKILL 及安装命令 |

<span style="color: red; font-weight: bold;">本篇演示的是"消费"别人写好的 SKILL，先把画图能力用起来。</span>

### 1.4 画图工具的选型策略

Claude Code 配合不同工具画图，常见三种，各有定位：

| 工具 | 适用场景 | AI 友好度 | 推荐频率 |
|------|----------|------------|----------|
| **Mermaid** | 90% 场景：架构图、流程图、时序图、ER 图 | 高（Claude Code / Cursor / GitHub / Notion / VS Code 原生支持） | 主力 |
| **PlantUML** | 复杂 UML（详细类图、带 frame 结构的时序图） | 中（语法稍重，AI 易出错） | 偶尔兜底 |
| **draw.io（diagrams.net）** | 最终交付的精修架构图（PPT、正式文档） | 低（不适合 AI 快速生成） | 用于精修交付 |

推荐的日常流程：

```text
1. 用 Mermaid 快速迭代 → 反复改 → 留在项目 docs/ 目录
2. 最终要交付到外部（PPT、正式文档）的一两张图 → 用 draw.io 精修
3. PlantUML 仅在 Mermaid 表达不够时偶尔用一次
```

本篇只演示 Mermaid 能力的安装（claude-mermaid 方案）。PlantUML 与 draw.io 需要时再装，装法类似。

## 2. 给 Claude Code 装上画图能力

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/70a28be176f1cf19654a959e2beb4338_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 两步装好 claude-mermaid

claude-mermaid（作者 veelenga）是一个 MCP Server + Plugin 一体的方案。<span style="color: red; font-weight: bold;">它必须装两次，少一步都用不了。</span>

为什么要装两次？用传统软件的类比讲：渲染引擎是真正干活的工人，Plugin 定义是告诉 Claude Code 怎么调用这个工人。<span style="color: red; font-weight: bold;">少了渲染引擎，Plugin 只是一张说明书，没人执行；少了 Plugin，Claude Code 根本不知道这个能力存在。</span>两段缺一不可，就像后端服务和前端配置必须同时存在。

#### (1) 第一步：装渲染引擎

```bash
npm install -g claude-mermaid
```

这一步装的是真正干渲染活的 Node 程序。没有它，后面的 Plugin 只是空壳。Node 版本需要 **20 或更高**，用 `node -v` 确认。

#### (2) 第二步：装 Plugin 定义

在 Claude Code 里依次执行：

```text
/plugin marketplace add veelenga/claude-mermaid

/plugin install claude-mermaid@claude-mermaid
```

这一步装的是 SKILL 定义和 MCP 配置。

> 重要：装完后**完全退出** Claude Code 再重新启动（不是 reload），让 MCP Server 起来。如果安装遇到问题，可以直接让 Claude Code 帮忙排查。

### 2.2 一句话验证安装

装完用一句话验证：

```text
画个最简单的用户登录流程图，保存到当前目录。
```

预期效果：Claude Code 直接生成图片文件，而不是只返回 Mermaid 源码。

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/c9212c34a1216e4cd056057b363f4238_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-07-proj-rd-02-drawing-tool/c9212c34a1216e4cd056057b363f4238_MD5.jpg
用途：展示 claude-mermaid 安装后的验证效果图
内容：用户登录流程图渲染结果，证明 Claude Code 已具备直接输出图片的能力
-->

## 3. 画什么图：四类必备图的选择指南

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/d9af8da7e38656843889484ee89b3087_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

装好工具只是第一步。<span style="color: red; font-weight: bold;">老项目改造过程里反复需要的图，其实就那么四类——它们对应你做需求分析时常画的四类图，只是颗粒度更粗、聚焦"系统骨架"而非"业务流程"。</span>

### 3.1 四类图各回答什么问题

| 图类型 | 回答的问题 | 典型应用节点 |
|--------|------------|--------------|
| 架构图 | 系统的骨架是什么样？前端/后端/DB/外部服务怎么分层？ | 画项目全景、写 CLAUDE.md 的"项目架构"、理清编译时依赖、判断改造影响范围 |
| 模块依赖图 | 哪个模块是底层？哪个是上层？有没有循环依赖？改一个模块会拖动谁？ | 画模块依赖、评估改动影响面 |
| 时序图 | 一次请求是怎么从入口走到 DB 的？每个参与者按什么顺序交互？ | 梳理接口生命周期、复现 bug 路径、对比改造前后调用链 |
| ER 图（schema 图） | 表与表之间是什么关系？主键、外键、关联方向？ | 梳理数据模型、DB 相关改造、给接手者讲数据关系 |

### 3.2 其他图的取舍原则

状态图、类图、部署图等用到概率低。等真碰到再说——不要为了"图齐全"而画用不上的图，<span style="color: red; font-weight: bold;">图的价值密度比数量更重要</span>。

## 4. 四类图实战：提示词、效果与坑

下面四节以 **Spring AI Alibaba Admin** 项目为真实场景，给出每一类图的提示词模板、实际效果与常见坑。所有提示词都可以直接复制到 Claude Code 中使用。

每节都按同一个套路走：先给提示词模板（你直接抄），再看实际效果图（看到结果），最后讲坑或关键点（为什么这么做）。四节走完，你就有了画这四类图的肌肉记忆。

### 4.1 架构图

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/29eb8a515da4b93bc323a9bfaada98b2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 提示词模板

```text
帮我画一张这个项目的架构图。前端、后端、数据库、外部服务分层画出来。

每个模块写名字加一句话职责。别画实现细节，服务级就够了。

保存成 ./docs/architecture.svg，dark 主题。
```

#### (2) 实际效果

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/704d5dfc6ad8aec24bdd78ef1ad65820_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-07-proj-rd-02-drawing-tool/704d5dfc6ad8aec24bdd78ef1ad65820_MD5.jpg
用途：展示上面提示词在 Spring AI Alibaba Admin 项目上跑出来的架构图
内容：分层的架构图，前端/后端/数据库/外部服务按层分组，每个模块标注名字和一句话职责
-->

#### (3) 三个常见坑与对策

| 坑 | 表现 | 对策 |
|----|------|------|
| 模块堆一团乱麻 | AI 把所有模块挤在一起，看不出层次 | 明确说"分层"，让 Mermaid 的 `subgraph` 帮忙分组 |
| 周边基础设施淹没主干 | 日志、监控、配置中心都画进去，主干被淹没 | 补一句"周边基础设施用一个方框概括就行，别展开" |
| 画完一张就停 | AI 不主动迭代 | 第一张出来直接说"把 XX 模块展开再画一张"。<span style="color: red; font-weight: bold;">迭代是常态，一次画成的很少</span> |

### 4.2 模块依赖图

#### (1) 提示词模板

```text
看一下我的 pom.xml，画一张项目内部模块之间的依赖图。

外部库不画。有循环依赖用红色标出来。

保存成 ./docs/module-deps.svg。
```

#### (2) 实际效果

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/8ff889c4b6d84f46c7d986d61561dcd4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-07-proj-rd-02-drawing-tool/8ff889c4b6d84f46c7d986d61561dcd4_MD5.jpg
用途：展示上面提示词跑出来的模块依赖图
内容：项目内部模块的有向图，外部库被排除，循环依赖用红色高亮
-->

#### (3) 关键点

**强制读真实文件**。明确要求"看 pom.xml"，让 AI 读真实文件。<span style="color: red; font-weight: bold;">不说清楚它就根据项目名脑补依赖关系。</span>

### 4.3 时序图

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/b38453d522f22d3cd22b46d4926a2375_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 提示词模板

```text
帮我画 POST /api/prompts/create 这个接口的调用链时序图。

先去 grep 真实代码，从 Controller 一路追到 DB。

标清楚每一步是哪个类哪个方法，保存成 ./docs/sequence-create-prompt.svg。
```

#### (2) 实际效果

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/c132457119cc602a5f4dc6ad1703c065_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-07-proj-rd-02-drawing-tool/c132457119cc602a5f4dc6ad1703c065_MD5.jpg
用途：展示上面提示词跑出来的时序图
内容：Mermaid sequenceDiagram 渲染图，从 Controller 一路到 DB，每一步标注类名和方法名
-->

#### (3) 关键点

**一句"先 grep 真实代码"是时序图可信度的命门**。<span style="color: red; font-weight: bold;">不让 AI 读代码，它就会根据接口名瞎猜调用链。</span>

### 4.4 数据库 Schema 图（ER 图）

#### (1) 提示词模板

```text
看项目里的建表 SQL（db/migration 或 resources 里），画一张 ER 图。

主键、外键、表之间的关系标清楚。保存成 ./docs/schema.svg。
```

#### (2) 实际效果

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/d26dfc816ac721cb38ba5351b13cb3c7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-07-proj-rd-02-drawing-tool/d26dfc816ac721cb38ba5351b13cb3c7_MD5.jpg
用途：展示上面提示词跑出来的 ER 图
内容：Mermaid erDiagram 渲染图，标清主键、外键、表间关联方向
-->

#### (3) 关键点

**让 AI 读真实 DDL 或 JPA Entity**。<span style="color: red; font-weight: bold;">不要让 AI 根据表名猜字段。</span>指向 `db/migration` 或 `resources/` 下的真实 SQL，或 JPA Entity 类。

### 4.5 让图好看：三个细节

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/b8f600dc0e62692aed51992a27623a08_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

画出来和画得好看是两回事。<span style="color: red; font-weight: bold;">好看的图是能一直留下来的资产，丑的图画完一次就丢。</span>三个最重要的细节：

| 细节 | 做什么 | 怎么做 |
|------|--------|--------|
| 加颜色 | 同类模块一个色系 | 核心模块冷色（蓝、紫）、周边模块暖色（灰、琥珀）、外部系统中性色；Mermaid 里用 `classDef` 定义样式后批量应用 |
| 留白 | 不要把每个节点都塞满字 | 每个方框一行标题 + 一句话描述足够；其他放到配套文档里讲。图是索引，不是百科 |
| 固定方向 | 架构图方向不要变来变去 | 整个项目统一 TD（自上而下）或 LR（自左向右）；扫一眼就能对照 |

## 5. AI 画的图一定有错：Review 与存档

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/c79eb389c850c86e3d94d82f569486f3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 5.1 为什么 AI 画的图必然有错

装好工具、背熟提示词之后，仍要守住一件事：<span style="color: red; font-weight: bold;">AI 画的图一定有错</span>。

这不是吓唬人，而是 AI 的工作方式决定的。AI 基于它**读到**的代码画图，但它读到的不等于全貌。

可以类比一个常见场景：一个新人入职，你只把代码仓库丢给他，不交代任何背景。他会读得很认真，注释、命名、调用关系都能捋出来——但他画的架构图几乎一定是错的。因为代码之外还有一堆东西他看不见：哪个模块早废弃了只是没删、哪条调用链是早期方案现在走的是另一个通道、哪个对外接口是为了应付某个对接方的特殊需求临时加的。这些信息不在代码里，在团队的脑子里、在历史的决策里。

<span style="color: red; font-weight: bold;">AI 面对的，正是这个"只看代码、不看历史决策"的新人处境，而且它连问都没法问。</span>所以下面这些错误几乎是必然出现的：

| 错误类型 | 为什么会错 |
|----------|------------|
| **把废弃模块画成核心** | 旧代码还在仓库里，AI 不知道它已经停用 |
| **漏掉重要的异步通道** | MQ、定时任务、回调等不在主调用链上的通道容易被忽略 |
| **把 3 个表的关系画反** | 外键方向、关联基数（1:N、N:M）容易画错 |
| **把重载方法当成两个独立接口** | 方法签名差异小，AI 容易拆错 |

老项目里那些代码之外的东西——**隐性约定、历史包袱、对接方的特殊需求**——AI 通通看不见。它能把代码读到的部分画得像模像样，但那些"代码没写、但人人都知道"的部分，<span style="color: red; font-weight: bold;">恰恰是判断一张图对不对的关键。</span>

<span style="color: red; font-weight: bold;">所以 AI 画完图，工作才完成一半，剩下一半是你来 review。</span>

### 5.2 Review 与存档工作流

```text
AI 画初稿  →  你 review  →  修正  →  存档到项目 docs/ 目录
```

#### (1) 存档的产物

修正过的图，就是 `ARCHITECTURE.md` 里的那张架构图，或项目 `docs/` 里的那张数据流图。它是团队理解这个项目的**存档**——下次打开项目、或新同事接手时，这张图就是入门的第一份资料。

#### (2) 为什么存档比记忆可靠

图会过时，但**存档过的图比脑子里的理解可靠得多**。下一次回头看，至少有一个明确的、可对照的版本，而不是模糊的记忆。

这一点值得多说一句。你刚 review 完一张图的时候，脑子里对这张图的理解是最清晰的，每个节点为什么这么画、哪条边修正过，你都记得。但三个月后你再打开这个项目，记忆已经模糊——你只隐约记得"那个模块好像是核心""表关系大概是这样"。这时候如果没有存档，你只能重新让 AI 画一遍、重新 review 一遍，等于把活干了两次。存档的价值，就是给你留一个明确的、可对照的基线：再回来时对照着看哪里变了，而不是从零再推导一次。

### 5.3 Review 与存档清单

- [ ] AI 画的图一定有错——**逐节点核对**
- [ ] 核对：是否把废弃模块画成核心？
- [ ] 核对：是否漏掉重要的异步通道？
- [ ] 核对：表关系、调用方向是否反了？
- [ ] 核对：是否把重载方法当成了两个独立接口？
- [ ] 修正后保存到项目 `docs/` 目录或 `ARCHITECTURE.md`

## 6. 总结与作者立场

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/d555255757ec1f91d25542015f87a966_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

全文五条主线收束成下面一张回顾表，带走的就这五条核心结论。

### 6.1 五件事回顾

| # | 主题 | 核心结论 |
|---|------|----------|
| 1 | SKILL 生态 | Claude Code 不是出厂定型的工具，`~/.claude/skills/` 和项目 `.claude/skills/` 是扩展点 |
| 2 | 画图能力 | 用 claude-mermaid 方案，Claude Code 能实时出 PNG / SVG / PDF，支持多种主题、浏览器实时预览。装法两步命令 |
| 3 | 四类图的场景对应 | 架构图讲骨架、模块依赖图讲模块关系、时序图讲调用链、ER 图讲数据结构。老项目改造反复需要这四类 |
| 4 | 工具策略 | Mermaid 为主（90% 场景），PlantUML 与 draw.io 兜底（复杂 UML 或精修交付图） |
| 5 | 硬守的一条线 | AI 画的图一定有错。画图不是"AI 画完就完"，是"AI 起稿 + 你把关 + 留下资产" |
