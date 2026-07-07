---
title: 传统项目迁AI 07：了解项目 - 绘图工具
author: fangkun119
date: 2026-07-04 07:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
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
传统项目迁AI 07：了解项目 - 绘图工具
-->

## 1. 导读地图

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/90d5e3527f609a2868aec6cf6bbe9bad_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是系列的第 7 篇，主题是**给 AI 编程工具装上画图能力**，并建立"什么场景画什么图"的方法论。改写自原稿《07_了解项目_02：绘图工具》。

两类读者的推荐阅读路径如下：

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/10b5d8b7b4bf11b619584e9a2cec38cd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    START([进入本篇]) --\> Q{读者类型?}
    Q --\>|初学 AI 编程工程师| P1[第一部分 方法论提炼]
    P1 --\> P2[第二部分 实战演示]
    P2 --\> END([下一篇：画项目全景])
    Q --\>|熟练 AI 编程工程师| C1[第 2 章 概览速读]
    C1 --\> C2[第 4 章 Check List 收藏]
    C2 --\> C3[按需查阅第 6 章提示词]
    C3 --\> END

    subgraph 第一部分 方法论提炼
      P1
    end
    subgraph 第二部分 实战演示
      P2
    end
-->

```text
本篇结构：
  第一部分 方法论提炼（参考手册风，速查用）
    2. 绘图能力概览：SKILL、工具选型
    3. 四类必备图选择指南：场景决策
    4. 项目画图 Check List：可裁剪清单
  第二部分 实战演示（教材风，复现用）
    5. 给 Claude Code 装上画图能力
    6. 四类图实战：提示词 + 效果 + 坑
    7. 让图好看：3 个细节
    8. AI 画的图一定有错：Review 与存档
```

## 2. 绘图能力概览

### 2.1 为什么要给 AI 编程工具"装"画图能力

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/2a7bda52e2ff9d5ba6f526820a77010f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Claude Code 出厂时不会直接渲染图片——当用户让它"画一张项目架构图"，它默认只返回一段 Mermaid 源码，需要用户自己去 mermaid.live 或类似工具里渲染。

<!-- 
图片内容说明
路径：imgs/07_了解项目_02：绘图工具/bf265a032ed40b2ba2959d3e2f2db8dc_MD5.jpg
用途：展示 Claude Code 默认行为——只输出 Mermaid 源码、不渲染图片
内容：Claude Code 对话截图，AI 返回一段 mermaid 代码块，没有给出渲染后的图片
-->

这不是工具的缺陷，而是能力未启用。装上后，它能直接输出 PNG / SVG / PDF，并支持浏览器实时预览。

### 2.2 SKILL：Claude Code 的能力扩展点

#### (1) SKILL 是什么

SKILL 是 Claude Code 的能力扩展机制。Claude Code 启动时会扫描两个目录：

```text
~/.claude/skills/         # 用户级，所有项目共享
<项目根>/.claude/skills/  # 项目级，仅当前项目生效
```

扫到的每个 SKILL 会被加载进上下文，成为 Claude Code 的"知识库"。一个 SKILL 就是一个文件夹，核心是 `SKILL.md`——它告诉 Claude Code 这个 SKILL 干什么、什么时候用、用什么工具。

#### (2) 安装 SKILL 的两种方式

| 方式 | 命令 | 适用场景 |
|------|------|----------|
| **Plugin 方式** | `/plugin marketplace add <github-repo>` 然后 `/plugin install` | 自动处理依赖、配置 MCP server，最省事 |
| **手动 clone** | `git clone` 到 `~/.claude/skills/` 或项目 `.claude/skills/`，重启 Claude Code | 透明、可控、便于阅读和修改 SKILL 内部 |

#### (3) 去哪里找 SKILL

##### ① 官方渠道

Anthropic 官方 plugin marketplace：在 Claude Code 里执行 `/plugin marketplace list` 即可看到官方收录的 SKILL。

##### ② GitHub 社区

GitHub 搜索 `claude-code skills` 或 `claude skill`，社区项目众多。

##### ③ 第三方聚合站

如 `skillsdirectory.com`、`fastmcp.me` 等，收录了大量可用 SKILL 及其安装命令。

> 提示：本篇演示的是"消费"别人写好的 SKILL。本系列第 11 篇会讲如何自己编写 `SKILL.md`，做 SKILL 的"生产者"。

### 2.3 画图工具选型策略

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/0cbdce900160deb3de4e4b80a422db62_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Claude Code 配合不同工具画图，常见三种，各有定位：

| 工具 | 适用场景 | AI 友好度 | 推荐频率 |
|------|----------|------------|----------|
| **Mermaid** | 90% 场景：架构图、流程图、时序图、ER 图 | 高（Claude Code / Cursor / GitHub / Notion / VS Code 原生支持） | 主力 |
| **PlantUML** | 复杂 UML（详细类图、带 frame 结构的时序图） | 中（语法稍重，AI 易出错） | 偶尔兜底 |
| **draw.io（diagrams.net）** | 最终交付的精修架构图（PPT、正式文档） | 低（不适合 AI 快速生成） | 用于精修交付 |

#### (1) 推荐的日常流程

```text
1. 用 Mermaid 快速迭代 → 反复改 → 留在项目 docs/ 目录
2. 最终要交付到外部（PPT、正式文档）的一两张图 → 用 draw.io 精修
3. PlantUML 仅在 Mermaid 表达不够时偶尔用一次
```

#### (2) 本篇的安装范围

本篇只演示 Mermaid 能力的安装（claude-mermaid 方案）。PlantUML 与 draw.io 需要时再装，装法类似。

## 3. 四类必备图选择指南

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/8a5789d2092e53e719334319c7e00713_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

老项目改造过程里反复需要的图就那么四类，下表给出每一类的核心用途与典型应用节点：

| 图类型 | 回答的问题 | 典型应用节点 |
|--------|------------|--------------|
| **架构图** | 系统的骨架是什么样？前端/后端/DB/外部服务怎么分层？ | 画项目全景、写 CLAUDE.md 的"项目架构"、理清编译时依赖、判断改造影响范围 |
| **模块依赖图** | 哪个模块是底层？哪个是上层？有没有循环依赖？改一个模块会拖动谁？ | 画模块依赖、评估改动影响面 |
| **时序图** | 一次请求是怎么从入口走到 DB 的？每个参与者按什么顺序交互？ | 梳理接口生命周期、复现 bug 路径、对比改造前后调用链 |
| **ER 图（schema 图）** | 表与表之间是什么关系？主键、外键、关联方向？ | 梳理数据模型、DB 相关改造、给接手者讲数据关系 |

### 3.1 各类图在本系列中的使用位置

#### (1) 架构图

用方框和箭头展示系统的骨架。颗粒度粗，一张图说清整个系统的样子。本系列中至少出现在四处：

```text
① 第 8 篇画项目全景
② 第 10 篇 CLAUDE.md 的"项目架构"小节
③ 第 13 篇编译运行时理清依赖
④ 第四部分改造时判断影响范围
```

#### (2) 模块依赖图

有向图，节点是模块或包，边是依赖关系。一眼看出底层 vs 上层、循环依赖。本系列中：

```text
① 第 8 篇画模块依赖
② 任何时候想知道"改这个模块会拖动谁"
```

#### (3) 时序图

时间轴展示多参与者交互，如 `用户 → 前端 → Controller → Service → DB`。本系列中：

```text
① 第 9 篇梳理接口：画"一次 API 调用的完整生命周期"
② 复现 bug：画"问题路径"
③ 第四部分改造：对比"改造前 vs 改造后的调用链"
```

#### (4) ER 图

表与表之间的外键关系，主键、外键、关联方向一目了然。本系列中：

```text
① 第 9 篇梳理数据模型
② 改造涉及 DB 的任何时候
③ 给接手同事讲数据关系
```

### 3.2 其他图的取舍原则

状态图、类图、部署图等用到概率低。**等真碰到再说**——不要为了"图齐全"而画用不上的图，图的价值密度比数量更重要。

## 4. 项目画图 Check List

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/d2f3b4b2da06444f18902af255b68fc8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

下面这份清单覆盖"装工具 → 画图 → Review → 存档"全流程，可裁剪后贴到项目 Wiki 或个人笔记中。

### 4.1 装工具阶段

- [ ] Node.js ≥ 20（`node -v` 确认）
- [ ] 已执行 `npm install -g claude-mermaid`
- [ ] 已在 Claude Code 中 `/plugin marketplace add veelenga/claude-mermaid`
- [ ] 已 `/plugin install claude-mermaid@claude-mermaid`
- [ ] **完全退出**并重启 Claude Code（不是 reload），让 MCP Server 起来

### 4.2 选图阶段

- [ ] 想清楚要回答的问题（系统骨架？模块依赖？调用链？数据结构？）
- [ ] 对照第 3 章决策表选择图类型
- [ ] 选定方向（架构图统一 TD 或 LR，整个项目保持一致）

### 4.3 画图阶段

- [ ] 让 AI 读**真实代码**（pom.xml / Controller / 建表 SQL），不要让它脑补
- [ ] 分层 / 分组（用 subgraph）
- [ ] 周边基础设施（日志、监控、配置中心）用一个方框概括
- [ ] 加颜色：核心模块冷色、周边暖色、外部中性
- [ ] 留白：每个方框一行标题 + 一句话描述
- [ ] 迭代：第一张出来后说"把 XX 模块展开再画一张"

### 4.4 Review 与存档阶段

- [ ] AI 画的图一定有错——**逐节点核对**
- [ ] 核对：是否把废弃模块画成核心？
- [ ] 核对：是否漏掉重要的异步通道？
- [ ] 核对：表关系、调用方向是否反了？
- [ ] 核对：是否把重载方法当成了两个独立接口？
- [ ] 修正后保存到项目 `docs/` 目录或 `ARCHITECTURE.md`

## 5. 给 Claude Code 装上画图能力

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/2b1f3199193d821fbeb6b3ab4b86073e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节以 **claude-mermaid**（作者 veelenga）为例，演示如何让 Claude Code 直接输出可渲染的图。它是一个 MCP Server + Plugin 一体的方案。

### 5.1 安装两步走

claude-mermaid 必须装两次——少一步都用不了。

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

### 5.2 验证安装

装完用一句话验证：

```text
画个最简单的用户登录流程图，保存到当前目录。
```

预期效果：Claude Code 直接生成图片文件，而不是只返回 Mermaid 源码。

<!-- 
图片内容说明
路径：imgs/07_了解项目_02：绘图工具/c9212c34a1216e4cd056057b363f4238_MD5.jpg
用途：展示 claude-mermaid 安装后的验证效果图
内容：用户登录流程图渲染结果，证明 Claude Code 已具备直接输出图片的能力
-->

## 6. 四类图实战：提示词 + 效果 + 坑

下面四小节以 **Spring AI Alibaba Admin** 项目为真实场景，给出每一类图的提示词模板、实际效果与常见坑。所有提示词都可以直接复制到 Claude Code 中使用。

### 6.1 架构图

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/dc7aebc0855dfef44301eb19625f528a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 提示词模板

```text
帮我画一张这个项目的架构图。前端、后端、数据库、外部服务分层画出来。

每个模块写名字加一句话职责。别画实现细节，服务级就够了。

保存成 ./docs/architecture.svg，dark 主题。
```

#### (2) 实际效果

<!-- 
图片内容说明
路径：imgs/07_了解项目_02：绘图工具/704d5dfc6ad8aec24bdd78ef1ad65820_MD5.jpg
用途：展示上面提示词在 Spring AI Alibaba Admin 项目上跑出来的架构图
内容：分层的架构图，前端/后端/数据库/外部服务按层分组，每个模块标注名字和一句话职责
-->

#### (3) 三个常见坑与对策

##### ① 坑一：模块堆一团乱麻

AI 容易把所有模块挤在一起。对策：明确说"分层"，让 Mermaid 的 `subgraph` 帮忙分组。

##### ② 坑二：周边基础设施淹没主干

AI 倾向于把日志、监控、配置中心都画进去，主干被淹没。对策：补一句"周边基础设施用一个方框概括就行，别展开"。

##### ③ 坑三：画完一张就停

AI 不主动迭代。对策：第一张出来直接说"把 XX 模块展开再画一张"。**迭代是常态，一次画成的很少**。

### 6.2 模块依赖图

#### (1) 提示词模板

```text
看一下我的 pom.xml，画一张项目内部模块之间的依赖图。

外部库不画。有循环依赖用红色标出来。

保存成 ./docs/module-deps.svg。
```

#### (2) 实际效果

<!-- 
图片内容说明
路径：imgs/07_了解项目_02：绘图工具/8ff889c4b6d84f46c7d986d61561dcd4_MD5.jpg
用途：展示上面提示词跑出来的模块依赖图
内容：项目内部模块的有向图，外部库被排除，循环依赖用红色高亮
-->

#### (3) 关键点

##### ① 强制读真实文件

明确要求"看 pom.xml"，让 AI 读真实文件。不说清楚它就根据项目名脑补依赖关系。

### 6.3 时序图

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/e04023ce4d7ae5d048fdf7d664407e01_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 提示词模板

```text
帮我画 POST /api/prompts/create 这个接口的调用链时序图。

先去 grep 真实代码，从 Controller 一路追到 DB。

标清楚每一步是哪个类哪个方法，保存成 ./docs/sequence-create-prompt.svg。
```

#### (2) 实际效果

<!-- 
图片内容说明
路径：imgs/07_了解项目_02：绘图工具/c132457119cc602a5f4dc6ad1703c065_MD5.jpg
用途：展示上面提示词跑出来的时序图
内容：Mermaid sequenceDiagram 渲染图，从 Controller 一路到 DB，每一步标注类名和方法名
-->

#### (3) 关键点

##### ① 一句"先 grep 真实代码"

不让 AI 读代码，它就会根据接口名瞎猜调用链。这一句是时序图可信度的命门。

> 说明：脚本对 Level 4 仅识别 ①-⑳。下面的实战小节按 Level 3 编号继续递进，受 Level 4 容量限制，关键点改用项目符号而非 Level 4 标题呈现。

### 6.4 数据库 Schema 图（ER 图）

#### (1) 提示词模板

```text
看项目里的建表 SQL（db/migration 或 resources 里），画一张 ER 图。

主键、外键、表之间的关系标清楚。保存成 ./docs/schema.svg。
```

#### (2) 实际效果

<!-- 
图片内容说明
路径：imgs/07_了解项目_02：绘图工具/d26dfc816ac721cb38ba5351b13cb3c7_MD5.jpg
用途：展示上面提示词跑出来的 ER 图
内容：Mermaid erDiagram 渲染图，标清主键、外键、表间关联方向
-->

#### (3) 关键点

- **让 AI 读真实 DDL 或 JPA Entity**：不要让 AI 根据表名猜字段。指向 `db/migration` 或 `resources/` 下的真实 SQL，或 JPA Entity 类。

## 7. 让图好看：3 个细节

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/92b654d50bb5044319a80e1b81136c8d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

"画出来"和"画得好看"是两回事。好看的图是能一直留下来的资产，丑的图画完一次就丢。下表是三个最重要的细节：

| 细节 | 做什么 | 怎么做 |
|------|--------|--------|
| **加颜色** | 同类模块一个色系 | 核心模块冷色（蓝、紫）、周边模块暖色（灰、琥珀）、外部系统中性色；Mermaid 里用 `classDef` 定义样式后批量应用 |
| **留白** | 不要把每个节点都塞满字 | 每个方框一行标题 + 一句话描述足够；其他放到配套文档里讲。**图是索引，不是百科** |
| **固定方向** | 架构图方向不要变来变去 | 整个项目统一 TD（自上而下）或 LR（自左向右）；扫一眼就能对照 |

## 8. AI 画的图一定有错

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/bf700f96052ef424a1c20571ce4ec23b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 8.1 为什么 AI 画的图必然有错

装好工具、背熟提示词之后，仍要守住一件事：**AI 画的图一定有错**。可能的错误形态：

- **把废弃模块画成核心**：旧代码还在仓库里，AI 不知道它已经停用
- **漏掉重要的异步通道**：MQ、定时任务、回调等不在主调用链上的通道容易被忽略
- **把 3 个表的关系画反**：外键方向、关联基数（1:N、N:M）容易画错
- **把 Controller 的一个重载方法当成两个独立接口**：方法签名差异小，AI 容易拆错

AI 基于它**读到**的代码画图，但它读到的不等于全貌。老项目里那些代码之外的东西——**隐性约定、历史包袱、对接方的特殊需求**——它看不见。

### 8.2 Review 与存档的工作流

承接本系列第 6 篇八步心法的第 8 步，画图阶段仍要守住一条线：

```text
AI 画初稿  →  你 review  →  修正  →  存档到项目 docs/ 目录
```

#### (1) 存档的产物

修正过的图，就是 `ARCHITECTURE.md` 里的那张架构图，或项目 `docs/` 里的那张数据流图。它是团队理解这个项目的**存档**——下次打开项目、或新同事接手时，这张图就是入门的第一份资料。

#### (2) 为什么存档比记忆可靠

图会过时，但**存档过的图比脑子里的理解可靠得多**。下一次回头看，至少有一个明确的、可对照的版本，而不是模糊的记忆。

## 9. 本篇总结

<img src="imgs/aicmigr-07-proj-rd-02-drawing-tool/e0aac4fc847576b5e15268f77ec44539_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇讲了五件事：

| # | 主题 | 核心结论 |
|---|------|----------|
| 1 | **SKILL 生态** | Claude Code 不是出厂定型的工具，`~/.claude/skills/` 和项目 `.claude/skills/` 是扩展点。本篇做消费者，第 11 篇做生产者 |
| 2 | **画图能力** | 用 claude-mermaid 方案，Claude Code 能实时出 PNG / SVG / PDF，支持多种主题、浏览器实时预览。装法两步命令 |
| 3 | **四类图的场景对应** | 架构图讲骨架、模块依赖图讲模块关系、时序图讲调用链、ER 图讲数据结构。老项目改造反复需要这四类 |
| 4 | **工具策略** | Mermaid 为主（90% 场景），PlantUML 与 draw.io 兜底（复杂 UML 或精修交付图） |
| 5 | **硬守的一条线** | AI 画的图一定有错。画图不是"AI 画完就完"，是"AI 起稿 + 你把关 + 留下资产" |

装好画图能力后，下一篇将真正动手，用这几类图给 Spring AI Alibaba Admin 画出完整的项目全景。

## 10. 思考

### 10.1 关于 SKILL 装机量

回想一下，你电脑上的 Claude Code（或其他 AI 编程工具）装过几个 SKILL / Plugin？

- **如果装过**：它们解决了你什么具体问题？
- **如果一个也没装**：是没需求，还是不知道可以装？

### 10.2 关于缺图的项目

回想你最近接手或维护的项目，哪一类图（架构、依赖、时序、ER）你最缺？

- **缺的原因**：是没画过，还是画过但丢了？
- **如果让你从零补一张图**：你会从哪一类开始？为什么？
