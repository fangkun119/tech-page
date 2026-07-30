---
title: 传统项目迁AI 12：了解项目 - 流程回顾
author: fangkun119
date: 2026-07-04 12:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
math: true
mermaid: true
image:
  path: imgs/aicmigr-12-proj-rd-07-process-recap/cover.jpg
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
aicmigr-12-proj-rd-07-process-recap
传统项目迁AI 12：了解项目 - 流程回顾
-->

## 1. 为什么老项目改造前要先搭"AI 协作基础设施"

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/75e4c1ff51695761e21205c682382e90_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

你接手一个十年老项目，准备让 Claude Code 帮你改造。第一句提示词发出去之前，先停一下：项目里有没有架构图？接口清单？数据模型？约束文档？

传统软件改造前要齐的"设计文档基线" —— <span style="color: red; font-weight: bold;">架构图 + 接口契约 + ER 图 + README + CI 脚本</span>—— 是工程师和团队协作的共同语言。AI 协作时也是一样： **没有这套基线，AI 就是个不熟悉项目的新人**，每次都要从头摸、每个判断都要人来兜底。

这套为 AI 准备的基线，叫"<span style="color: red; font-weight: bold;">AI 协作基础设施</span>"。它不是新概念，只是把传统软件里早就有的几份文档重新组织一下，让 AI 能读懂、能回查、能基于它干活。

### 1.1 单步教 ≠ 一气呵成跑

之前几篇，把动作拆成一步一步：先画架构图，再梳理接口，再写 CLAUDE.md。每一步单独跑都能跑通，看上去很简单。但实际上，步骤之间有依赖，"<span style="color: red; font-weight: bold;">前一步产出是后一步的输入</span>"。当卡在某一步时，问题完全有可能是出在了上一步。

### 1.2 七份资产之间有硬依赖

更关键的是，AI 协作基础设施里的七份资产**不是平级的**，它们之间有明确的上下游：

- CLAUDE.md 依赖 `docs/` 下的五份资产生成
- SKILL 又依赖 CLAUDE.md 和 `docs/` 才能判断项目里哪些流程值得自动化

你可能会问：那能不能七份资产并行跑？不能。**就像设计评审→接口评审→数据评审→编码→CI 是有上下游的，下游基于上游**——接口评审没过就开始编码，返工成本极高。<span style="color: red; font-weight: bold;">AI 协作基础设施也一样：先有架构图和接口清单，CLAUDE.md 才有依据；先有 CLAUDE.md，SKILL 才挖得出来。</span>

一份一份手敲能跑通，但跑不通时你往往不知道是哪一份出了问题。<span style="color: red; font-weight: bold;">这套基础设施的核心难点不在任何单份资产，而在"资产之间的依赖关系和执行顺序"。</span>这也是为什么很多团队试了 AI 改造，结果"AI 跑出来的东西不对"——根源往往不在提示词，而在资产没按顺序准备齐。

## 2. AI 协作基础设施长什么样：七份资产

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/956e9bb81c69a4fa6c5a5144e6652b6c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/d51adc757ad3efa5bd60b98ba3f9baba_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把"AI 协作基础设施"这个抽象概念，映射回你熟悉的传统软件工程心智模型：<span style="color: red; font-weight: bold;">它就是项目交付前必须齐的那套设计文档基线，只是组织方式按"AI 怎么读"重新分了类。</span>

### 2.1 七份资产速查表

| 类别 | 资产 | 路径 | 解决的问题 |
|------|------|------|-----------|
| 项目全景 | 架构图 | `docs/architecture.svg` | 让 AI 一眼看到分层和核心模块 |
| 项目全景 | 模块依赖图 | `docs/module-deps.svg` | 让 AI 看清内部模块怎么连 |
| 项目全景 | 外部依赖图 | `docs/external-deps.svg` | 让 AI 知道项目靠什么活着 |
| 接口与数据 | 接口清单 | `docs/api-list.md` | 让 AI 知道对外契约 |
| 接口与数据 | 数据模型 | `docs/data-model.md` + `data-model-er.svg` | 让 AI 知道数据底座 |
| 项目常识 | CLAUDE.md | 项目根目录 | 让 AI 知道项目怎么跑、禁区在哪 |
| 操作资产 | SKILL.md | `.claude/skills/{name}/SKILL.md` | 让 AI 自动跑团队反复做的流程 |

用熟悉的概念再过一遍：

- `docs/` 下的五份资产 ≈ <span style="color: red; font-weight: bold;">设计文档库</span>。架构图对应分层架构文档、模块依赖图对应组件依赖图、外部依赖图对应部署拓扑、接口清单对应 OpenAPI、数据模型对应 ER 图。它们是基线版本，AI 改代码时随时回查。
- `CLAUDE.md` ≈ <span style="color: red; font-weight: bold;">架构约束文档 + README + 团队踩坑笔记</span>的合体。一份文件同时回答"这个项目是什么、怎么跑、哪里不能动、踩过哪些坑"。
- `SKILL.md` ≈ <span style="color: red; font-weight: bold;">团队反复跑的 CI 脚本或 Makefile target</span>，但由 AI 触发执行。比如"代码改完后自动同步文档"这种重复操作，写成 SKILL 让 AI 来跑。

验收时按这张表点一遍——<span style="color: red; font-weight: bold;">七份资产齐了，AI 协作的基础就到位了。</span>

### 2.2 资产之间的依赖拓扑

七份资产不是平级的，上下游关系如下：

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/ebe445b1ff703e6a98e732c1f0e308c1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明

路径：imgs/aicmigr-12-proj-rd-07-process-recap/ebe445b1ff703e6a98e732c1f0e308c1_MD5.jpg

用途：七份资产依赖拓扑图（mermaid 流程图的渲染版）

内容：展示 AI 协作基础设施七份资产的上下游依赖：三张全景图（architecture/module-deps/external-deps）产出后喂给接口清单+数据模型（两者互相校对），再生成 CLAUDE.md 前五节，最后挖出第一个 SKILL；禁区/历史包袱为手写补充，AI 不该填

图片对应的mermaid：
flowchart LR
    SubA[三张全景图<br/>architecture / module-deps / external-deps] --\> SubB[接口清单 + 数据模型]
    SubB --\>|互相校对| SubB
    SubB --\> CMD[CLAUDE.md<br/>前五节基于 docs 生成]
    CMD --\> Skill[第一个 SKILL<br/>基于项目重复流程挖出]
    CMD -.禁区/历史包袱.-> Manual[手写补充<br/>AI 不该填]

    classDef docs fill:#eef,stroke:#336
    classDef root fill:#efe,stroke:#363
    classDef claude fill:#ffe,stroke:#663
    class SubA,SubB docs
    class Skill root
    class CMD,Manual claude
-->

从图中能看出三个要点：

#### (1) docs/ 是所有下游资产的原料

`docs/` 下的五份资产是基础。CLAUDE.md 的前五节、SKILL 的候选挖掘，都依赖这五份资产生成。<span style="color: red; font-weight: bold;">画图和梳理接口/数据模型必须先做</span>，做完之后 CLAUDE.md 和 SKILL 才有依据。<span style="color: red; font-weight: bold;">这跟传统流程"设计文档先评审，编码和 CI 才有据可依"是一个道理。</span>

#### (2) 接口清单和数据模型必须互相校对

<span style="color: red; font-weight: bold;">接口清单</span>和<span style="color: red; font-weight: bold;">数据模型</span>各自扫一遍 Controller 和 entity 还不够——<span style="color: red; font-weight: bold;">两份资产必须互相校对</span>：接口里提到的每个实体，数据模型里是否都有定义？数据模型里的关键字段，接口入参/出参里是否都被用到？

类比传统流程，这就像设计评审时让<span style="font-weight: bold; text-decoration: underline;">接口设计</span>和<span style="font-weight: bold; text-decoration: underline;">数据库设计</span><span style="text-decoration: underline;">互相对一遍</span>：接口里用到的实体，表结构里都得有；表里的关键字段，接口里也得用到。两头对不上，硬做下去到联调时全是返工。<span style="color: red; font-weight: bold;">为什么这一步最容易被跳过、又为什么最有价值，后面实战章节会展开。</span>这里先记住一个结论：<span style="color: red; font-weight: bold;">两份资产对不上，CLAUDE.md 写出来就是错的</span>。

#### (3) CLAUDE.md 的禁区和历史包袱必须手写

CLAUDE.md 的前五节（<span style="color: red; font-weight: bold;">项目定位</span>、<span style="color: red; font-weight: bold;">核心架构</span>、<span style="color: red; font-weight: bold;">关键模块</span>、<span style="color: red; font-weight: bold;">关键约定</span>、<span style="color: red; font-weight: bold;">怎么跑</span>）可以基于 `docs/` 自动生成，**但<span style="color: red; font-weight: bold;">禁区</span>和<span style="color: red; font-weight: bold;">历史包袱</span>这两节依赖的是只有团队成员才知道的隐性知识**：哪段代码动不得、哪些设计是当年为了赶工期留下的债。

| 模块 | 写什么 | 解决什么 |
|------|--------|----------|
| 项目定位 | 一句话说清项目是什么、提供什么能力 | AI 启动的第一句常识，避免“这是个 Spring Boot 项目”式的空洞认知 |
| 核心架构 | 一段话点出分层 + 指向 `docs/architecture.svg` 的链接 | 只当索引，绝不把图里的内容文字化重抄 |
| 关键模块 | 一个小表，每个模块一句话职责 | 模块间的详细依赖关系交给 `docs/module-deps.svg` 承担 |
| 关键约定 | 项目特有的硬规则（命名风格、响应包装、方法签名约束等），只写结论不展开理由 | 从代码反推，而非抄通用开发手册 |
| 怎么跑 | 一句话点出启动方式 + 指向运行文档的链接 | 让 AI 知道跑起来需要哪些中间件，防止瞎改配置 |
| 禁区 | 哪些字段/配置/接口动不得及原因 | 给 AI 划红线，红线之内再合理的重构也不许做 |
| 历史包袱 | 解释“看起来奇怪但有原因”的旧代码（死功能脏表、技术栈遗留、兼容层） | 解释清楚，AI 才不会“好心”去统一它 |

<span style="color: red; font-weight: bold;">让 AI 写这两节，结果一定是空话或编造。</span>**正确做法是让 AI 留占位（"待补充"），由工程师手写**。没思路就先列一两条占位，改造过程中踩到坑了再补。

### 2.3 资产规模与质量的硬指标

资产齐全不等于资产合格。验收时除了"有没有"，还要看"够不够精"和"够不够准"：

| 资产        | 硬指标                                                                                                                                                 | 不达标的常见原因                       |
| --------- | --------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------ |
| 三张全景图     | ① <span style="color: red; font-weight: bold;">关键依赖无遗漏</span>（MySQL、Nacos、OTel Collector、外部模型 API 都在）                                               | AI 扫描范围不够，漏看 `application.yml` |
| 接口清单      | ① 所有模块（core/openapi/runtime）的 Controller <span style="color: red; font-weight: bold;">全扫到</span><br>② 对外/内部接口分开标注                                   | AI 只扫了入口模块的 Controller         |
| 数据模型      | ① 以 <span style="color: red; font-weight: bold;">DB 层为准</span>、entity 和 DTO 分开说<br>② <span style="color: red; font-weight: bold;">隐式外键</span>关系反查出来 | AI 把 entity 和 DTO 混在一起         |
| CLAUDE.md | ① 总长不超过 300 行<br>② 不重复 `docs/` 内容（用链接指向）<br>③ 禁区/历史包袱有真实内容                                                                                          | AI 把 `docs/` 内容复制进 CLAUDE.md   |
| SKILL.md  | ① allowed-tools 限制到最小<br>② 不自动改文件只报告                                                                                                                | AI 给了过宽的工具权限                   |

#### (1) docs/ 资产的硬指标

`docs/` 下五份资产的硬指标核心就两条：**覆盖全、口径准**。

<span style="color: red; font-weight: bold;">覆盖全</span>：

- 三张全景图要把所有关键依赖画齐（MySQL、Nacos、OTel Collector、外部模型 API 缺一不可）；
- 接口清单要把三个模块的 Controller 全扫到（`server-core`、`server-openapi`、`server-runtime`）
- 对外接口和内部接口分开标注；
- 数据模型要把 entity 和 DTO 分开说明、隐式外键反查出来。

<span style="color: red; font-weight: bold;">口径准</span>：

- 数据模型必须以 DB 层为准。项目里数据模型表述有三层——DB <span style="color: red; font-weight: bold;">建表 SQL、entity 类、DTO，三层经常不一致</span>。以 D<span style="color: red; font-weight: bold;">B 为锚点，因为 DB 是合约的最终落地，改不动</span>；反过来以 entity 为锚点，DB 和 DTO 的偏差就讲不清了。

不达标的常见原因不是 AI 懒，而是 AI 扫描范围不够：

- 漏看 `application.yml` 导致外部依赖缺项
- 把 entity 和 DTO 混在一起导致数据模型混乱。

**这些不达标都能在 review 时被抓出来，前提是你知道验收标准**。

#### (2) CLAUDE.md 与 SKILL 的硬指标

这两份资产最容易出问题，因为它们涉及"什么该写、什么不该写"的判断。

**CLAUDE.md 的 300 行是硬上限**。为什么是 300 行？**README 不是写小说的地方**。CLAUDE.md 的作用是让 AI 快速建立项目心智模型，<span style="color: red; font-weight: bold;">超过 300 行说明它在重复 `docs/` 的内容——AI 读 CLAUDE.md 时上下文会被无效内容占满，反而降低判断质量。</span><span style="color: red; font-weight: bold;">正确做法是详细内容放 `docs/`，CLAUDE.md 只写摘要和链接。</span>

**SKILL.md 的 allowed-tools 必须限制到最小**。<span style="color: red; font-weight: bold;">SKILL 是让 AI 自动跑的流程脚本，给的工具权限越宽，风险越大。</span>特别是文档同步类的 SKILL，原则上<span style="color: red; font-weight: bold;">只读不写</span>——`allowed-tools` 限制到 `Read, Grep`，不给 `Edit, Write`。背后的逻辑后面实战章节会展开。

## 3. 怎么让 AI 自主产这七份资产：四要素方法论

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/4b6d90b26290b619380c66099d4060bc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

了解项目阶段的另一个关键产物，是<span style="color: red; font-weight: bold;">一段能让 Claude Code 自主跑完全流程的提示词</span>。这段提示词不是"把四个场景的提示词拼起来"那么简单——它<span style="color: red; font-weight: bold;">要求 AI 自己 review、自己修、跑完一次性汇报</span>。

你可能会问：直接把每一步的提示词粘进去跑不就行了？不行。一气呵成跑和分步跑有个本质区别：

- <span style="color: red; font-weight: bold;">分步跑时每步都会 review，AI 出错能立即资金进行纠正；</span>
- **一气呵成跑时中间不管管控质量，错误会累积放大**。

写好这段一键提示词，有四个要素缺一不可。这四个要素是一键流程提示词背后的方法论——本节先讲原理，怎么落到提示词文本里，后面实战章节会逐字拆解。

### 3.1 要素一：明确授权自主

老项目改造里 AI 默认会频繁确认——这是探索阶段的合理行为，但在一键流程里会打断节奏。**提示词必须明确写"<span style="color: red; font-weight: bold;">不要每一步都问我</span>"**，AI 才会真的一口气跑完。

类比传统协作：**就像授权下属独立干完一个任务，只在最后 review**，而不是事事请示。给下属派活时不说清楚"这点小事你自己定"，下属就会每个细节都来问，效率比你自己干还低。AI 也一样。

更进一步，要写明"<span style="color: red; font-weight: bold;">遇到判断不清的地方先做一个合理选择，在最后的 summary 里标记出来</span>"。这一句把 AI 的犹豫转化成决策：**有不确定的地方先决断、再标记，而不是停下来问**。

### 3.2 要素二：把 review 责任交给 AI

一键流程里，AI 不能产完就丢给工程师。提示词必须明确"<span style="color: red; font-weight: bold;">每一步跑完自己 review 输出质量，不合格自己重跑</span>"。这一句让 AI 对产出负责：

- 图里有漏、有错、有不清晰的地方，主动补充或重画
- 接口清单扫漏了模块，自己再扫一遍
- 数据模型和接口对不上，自己校对修正

类比传统工程实践：**就像让代码评审制度约束产出方，而不是只靠下游人工兜底**。把 review 责任完全推给测试工程师或线上事故，开发就会写出垃圾代码；把 review 责任压在写代码的人身上，代码质量就上去了。AI 也是同一个机制 —— <span style="color: red; font-weight: bold;">review责任</span> **不交给 AI，AI 就只是个执行器**；<span style="color: red; font-weight: bold;">交出去之后，AI 才是协作伙伴。</span>

### 3.3 要素三：用 summary 替代中途打断

中途打断是效率杀手。一段 15-30 分钟的自主流程，如果中间被打断十次问"这个细节怎么处理"，工程师实际花的时间远超流程本身。

正确做法是**让 AI 把所有不确定的地方都攒到最后，一次性写在 summary 里**。<span style="color: red; font-weight: bold;">工程师花 5 分钟读 summary、做决策，比中间被打断十次效率高得多。</span>

类比一下：**就像把零散会议攒成一次周会**，而不是随时拉人。零散打断看起来每次只占 2 分钟，但加上上下文切换成本，一天累计损失惊人。

summary 里要包含三类信息：

| summary 内容 | 例子 |
|-------------|------|
| 每个产出文件的路径 | `docs/architecture.svg` |
| 每份资产的主要内容概括 | "架构图体现了前后端分离和 OTel trace 链路" |
| 还需要人工确认的地方 | "module-deps 图里 start → runtime 的方向我标了，请你确认" |

### 3.4 要素四：占位 AI 不该填的内容

CLAUDE.md 的禁区、历史包袱，AI 不该填。**提示词要明确让 AI 写"待补充"占位**，避免 AI 瞎编。

类比代码实践：**占位就像代码里的 TODO，比瞎编实现安全得多**。`// TODO: 这里逻辑没搞清楚` 至少诚实地告诉后来者"这里需要补"，而一段看起来合理但实际错误的实现，会让后来者直接踩坑。

这个原则可以推广：**任何依赖团队隐性知识的内容，都应该让 AI 占位而非编造**。AI 编出来的禁区看起来合理，但一旦工程师信了，后面改造会踩大坑。<span style="color: red; font-weight: bold;">占位是诚实的表现——工程师看到"待补充"就知道这里要自己来，看到一段流畅的文字反而会放松警惕。</span>

## 4. 实战准备：主线项目与一次 clone

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/9540aa4c8cf99e28644b2df4e8e61e7a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

方法论讲完，进入实战。选一个真实开源项目当样本，把方法论在它身上完整跑一遍，这套动作就能直接复用到自己的老项目。

### 4.1 主线项目：Spring AI Alibaba Admin

样本项目选 **Spring AI Alibaba Admin**（`spring-ai-alibaba` 仓库下的 `spring-ai-alibaba-admin` 模块）。选它有三个理由：

- **真实开源**：不是 toy demo，有多模块、外部依赖、真实业务逻辑
- **规模适中**：既不至于一眼看完，也不会大到 AI 扫不动
- **结构典型**：前后端分离、多模块 Maven、有中间件和外部模型 API 调用，与公司里那些"典型老项目"形状相似

类比一下：它就像驾校的教练车 —— 和真车上路开的车结构一样，但没有商业包袱。在它身上把动作练熟，回到公司项目就能直接开。

### 4.2 从八步心法到七份资产

传统手工**了解一个老项目**的动作序列是"八步心法"：<span style="color: red; font-weight: bold;">读 README</span> → <span style="color: red; font-weight: bold;">看目录</span> → <span style="color: red; font-weight: bold;">找入口</span> → <span style="color: red; font-weight: bold;">跟请求</span> → <span style="color: red; font-weight: bold;">找配置</span> → <span style="color: red; font-weight: bold;">看依赖</span> → <span style="color: red; font-weight: bold;">划边界</span> → <span style="color: red; font-weight: bold;">画图</span>。

八步心法是过程，七份资产是结果。过程落到产物上，就是下面这张对照表：

| 八步心法 | 对应资产 |
|---------|---------|
| 读 README + 看依赖 | 外部依赖图（`external-deps.svg`） |
| 看目录 + 划边界 | 模块依赖图（`module-deps.svg`） |
| 找入口 + 跟请求 | 接口清单（`api-list.md`） |
| 找配置 | 数据模型（`data-model.md`） |
| 画图 | 架构图（`architecture.svg`） |
| 综合 | CLAUDE.md、SKILL |

**过程可以拆篇讲，结果必须一起验收**。传统软件工程也是如此：架构评审、接口评审、数据评审分着开，但交付时一整套设计文档基线必须一起齐。

### 4.3 准备：clone 项目并启动 Claude Code

先准备项目环境：

```bash
git clone https://github.com/alibaba/spring-ai-alibaba.git
cd spring-ai-alibaba/spring-ai-alibaba-admin
mkdir -p docs
mkdir -p .claude/skills
```

在这个目录下启动 Claude Code，后面所有提示词都在这里跑。

### 4.4 跑完之后项目目录长什么样

执行任何一条提示词之前，先看一遍跑完后的目录长什么样 —— 动手之前看一眼全貌，心里有终点，每一步才有的放失：

```
spring-ai-alibaba-admin/
├── CLAUDE.md ← 项目常识 + 禁区 + 历史包袱
├── .claude/skills/
│   └── docs-auto-sync/
│       └── SKILL.md ← 第一个自己挖的 skill
└── docs/
    ├── architecture.svg ← 架构图
    ├── module-deps.svg ← 模块依赖图
    ├── external-deps.svg ← 外部依赖图
    ├── api-list.md ← REST 接口清单
    ├── data-model.md ← 数据模型说明
    └── data-model-er.svg ← ER 图
```

这就是一个老项目的 AI 协作基础设施最终形态。后面 §5-§9 会一步步把它填出来。

## 5. 实战第一步：画三张全景图

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/bbd601f18a2695a9404bcda6eedc32c3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

`docs/` 下的三张 SVG 是所有下游资产的原料（见 §2.2 依赖拓扑）。先画图，再梳理接口和数据模型，这个顺序不能乱。

### 5.1 先画架构图

#### (1) 提示词与产出

```
读一下这个项目的 README 和顶层目录，给我画一张架构图。
前端、后端、数据库、中间件分层画，核心模块写一句话职责。
周边基础设施用一个方框概括就行，不用展开。
保存到 docs/architecture.svg。
```

产出：`docs/architecture.svg`

review 重点：有没有体现前后端分离、OpenTelemetry trace 链路有没有画上、server-start 有没有漏。

#### (2) 为什么先画架构图

类比"先看地图再开车"。架构图是入口，给 AI 一个项目的整体心智模型——**有了这张图，后面所有对话都基于同一张地图**：聊模块依赖、聊接口、聊数据模型时，AI 脑子里都有一张分层图作参考。

跳过架构图直接画模块依赖，AI 会陷入"只见树木不见森林"——能看清每个模块，但不知道这些模块组合起来是干什么的。

### 5.2 再画模块依赖图

#### (1) 提示词与产出

```
看一下项目的 pom.xml，画一张内部模块依赖图。
只画项目自己的模块，外部库不画。有循环依赖用红色标出来。
保存到 docs/module-deps.svg。
```

产出：`docs/module-deps.svg`

review 重点：start 依赖 runtime 和 openapi、两者都依赖 core，方向不能倒。frontend 不应该出现在这张图里。

#### (2) 为什么单独画一张模块依赖图

架构图画"项目对外长什么样"，模块依赖图画"项目内部怎么拼"。**这两张图服务的决策完全不同**：

| 图     | 服务的决策        |
| ----- | ------------ |
| 架构图   | <span style="color: red; font-weight: bold;">改造时动哪一层</span>      |
| 模块依赖图 | <span style="color: red; font-weight: bold;">改这个模块会影响哪些下游</span> |

把它们合并成一张图，信息密度太高、读不动；分开画，每张图都聚焦一个决策。传统软件工程里，系统上下文图和组件依赖图也是分开画的，同样的道理。

### 5.3 最后画外部依赖图

#### (1) 提示词与产出

```
综合看 pom.xml、application.yml 和 README，帮我梳理这个项目。
对外依赖了什么。分成三类：关键 Java 依赖、中间件、外部 API。
画出来，每类用不同颜色。保存到 docs/external-deps.svg。
```

产出：`docs/external-deps.svg`

review 重点：MySQL、Nacos、OTel Collector 都要在，外部模型 API（DashScope、OpenAI、DeepSeek）不能漏。

#### (2) 外部依赖图最容易漏的是外部 API

Java 依赖和中间件比较好扫——`pom.xml` 和 `application.yml` 里写得清清楚楚。<span style="color: red; font-weight: bold;">最容易漏的是外部模型 API</span>：DashScope、OpenAI、DeepSeek 这些 API 的 key 通常放在配置里（甚至是环境变量里）、调用代码散在 service 层。

AI 扫描时如果不主动提"外部 API"这一类，往往会漏掉。**提示词里写明**"<span style="color: red; font-weight: bold;">分成三类：关键 Java 依赖、中间件、外部 API</span>"，**就是在引导 AI 不漏第三类**。

## 6. 实战第二步：梳理接口和数据模型

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/b8d4247eaa9d9183a74f1b6989494a5f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

三张图画完，`docs/` 下有了项目的骨架。这一步把"血肉"补上：对外契约（接口清单）和数据底座（数据模型）。两份资产各自扫一遍还不够，必须互相校对。

### 6.1 REST 接口清单

```
扫一下这个项目里所有的 Controller，给我整理一份 REST 接口清单。
每个接口列出方法、路径、一句话说明、主要入参、返回结构。
按模块分组。保存到 docs/api-list.md。
```

产出：`docs/api-list.md`

review 重点：server-core、server-openapi、server-runtime 三个模块的 Controller 都要扫到。对外接口和内部接口分开标注。

### 6.2 数据模型（以 DB 层为准）

#### (1) 提示词与产出

```
看项目的 entity 类、DTO、数据库建表 SQL，给我梳理核心数据模型。
每个模型列出字段、类型、一句话说明。标出主键、外键、枚举值。
关键模型之间的关系画一张简单的 ER 图。
保存到 docs/data-model.md 和 docs/data-model-er.svg。
```

产出：`docs/data-model.md` + `docs/data-model-er.svg`

<span style="color: red; font-weight: bold;">review 重点：以 DB 层为准</span>、entity 和 DTO 分开说、<span style="color: red; font-weight: bold;">通过 findBy 反查出隐式外键关系</span>。

#### (2) 为什么要"以 DB 层为准"

项目里数据模型的表述有三层：DB 建表 SQL、entity 类、DTO。**三层经常会不一致**——DB 里字段叫 `user_id`，entity 里改成了 `userId`，DTO 里又变成了 `userIdStr`。

以 DB 层为准的原因：**DB 是合约的最终落地**。类比"以最终签字版合同为准"——<span style="color: red; font-weight: bold;">无论代码层怎么改名，DB 改不了</span>。**以 DB 为锚点，entity 和 DTO 的偏差都能讲清楚是谁错了**；反过来以 entity 为锚点，DB 和 DTO 的偏差就讲不清了。

### 6.3 两份资产互相校对（最易跳过、最有价值的一步）

#### (1) 提示词与产出

```
对照 docs/api-list.md 和 docs/data-model.md，
看接口里提到的每个实体在数据模型里是不是都有定义。
有不一致的地方列出来。
然后验证不一致的地方并修复。
```

产出：一份不一致点清单，AI 自动修正两份资产，直到自洽。

#### (2) 为什么不能跳过互相校对

跑完前两步，`docs/` 里已经有五份资产：`architecture.svg`、`module-deps.svg`、`external-deps.svg`、`api-list.md`、`data-model.md` + `data-model-er.svg`。看上去齐全了，**但接口清单和数据模型可能对不上**——接口里返回的某个字段，数据模型里压根没定义；数据模型里的某个枚举，接口入参里压根没用到。

<span style="color: red; font-weight: bold;">跳过这一步，CLAUDE.md 复述的就是两份对不上的资产。</span>**互相校对是质量保证的关键一步**——类似传统软件工程里 OpenAPI 契约和实体表的反向对账，AI 自己迭代修正，直到两份资产自洽。

这是「了解项目」阶段最容易跳过的、也是最有价值的一步。**两份资产对不上，后面所有基于它们的产出都是错的**。

## 7. 实战第三步：生成 CLAUDE.md

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/0c5d15ae269657e4b6098d98ea6082b6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接口和数据模型校对自洽后，`docs/` 下的五份资产就是稳定的原料。CLAUDE.md 的前五节可以基于它们生成；禁区和历史包袱两节，AI 填不出来，必须手写。

### 7.1 基于 docs/ 生成 CLAUDE.md 初稿

```
读 docs/ 下的所有资产，给我生成一份 CLAUDE.md 初稿。

精简：项目定位、核心架构、关键模块、关键约定、怎么跑，
外加两节空着的：禁区、历史包袱。

架构图、接口清单、数据模型的详细内容不要复制进来，
用链接指向 docs/ 就好。保存到项目根目录的 CLAUDE.md。
```

产出：项目根目录 CLAUDE.md（前五节 AI 生成、禁区和历史包袱留空）。

类比传统项目里的 README——CLAUDE.md 就是给 AI 看的 README。区别在于：

- README 给人看，可以铺陈；
- CLAUDE.md 给 AI 看，要短、要准、要指向详细资产而不是复制内容。

### 7.2 手写禁区和历史包袱

#### (1) 为什么这两节 AI 填不出来

这两节依赖的是只有团队成员才知道的隐性知识：

- **禁区**：哪段代码动不得——可能是某个核心算法被性能调优过、改一行性能崩一半；可能是某个表结构被多个老系统共享、改字段全公司炸。
- **历史包袱**：哪些设计是当年为了赶工期留下的债——可能是<span style="text-decoration: underline dashed;">某个 service 类承担了五个职责、想拆但拆不动</span>；可能是<span style="text-decoration: underline dashed;">某个接口返回结构混乱、客户端已经依赖这个混乱</span>。

这些知识<span style="color: red; font-weight: bold;">不在 `docs/` 里、不在 README 里、不在 git log 里</span>。AI 扫遍所有文件都挖不出来。<span style="color: red; font-weight: bold;">硬让 AI 写，结果一定是空话或编造</span>——比留空危害更大。

#### (2) 没思路就先占位

刚接手项目时，禁区可能一条都列不出来。**没关系，先列「待补充」占位**。类比代码里的 `TODO`——比瞎编一段实现安全得多。改造过程中踩到坑了——发现某个类一动就崩、某个表一改就连锁——再回头补到禁区一节。

CLAUDE.md 是活的文档，**不需要一开始就完美**。但**禁区这一节的标题必须在**，提醒工程师改造时多想一步。

review 重点：总长度不超过 300 行、没有重复 `docs/` 的内容（都用链接指向）、禁区和历史包袱两节有真实内容。

## 8. 实战第四步：挖出第一个 SKILL

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/888ea8c36bb5a148462fa4fc7d296b2c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

CLAUDE.md 写完后，最后一份资产是 SKILL。SKILL 不是拍脑袋想出来的，用「三步挖掘法」：让 AI 分析项目重复流程、出 Top 3 候选、选一个生成完整 SKILL.md。

### 8.1 让 AI 分析项目重复流程

```
扫一下当前项目（包括 git log、CLAUDE.md、docs/、README、CONTRIBUTING、.github/），
找出团队反复在做的操作流程。

判断标准是三特征：可复制、可参数化、可自动化。
三个都满足才算值得做 SKILL 的候选。

把找到的候选列出来，每个写明：流程名、为什么是反复的、能参数化的部分、
是什么、起点和终点是什么。最后给我用一个表格总结。
```

产出：5-10 项候选清单。

### 8.2 让 AI 出 Top 3 推荐

```
从上面的清单里挑 3 个最高优先级的，给我做成候选 SKILL。
每个候选写：name（英文）、description、预期 steps、allowed-tools。

优先级判断标准：频率高、痛点深、自动化收益大。用表格总结，包含类型和理由。
```

产出：三个候选 SKILL。

#### (1) Top 3 大概率包含「技术文档自动更新」

Top 3 大概率包含**技术文档自动更新**（docs-auto-sync），因为它的频率最高、痛点最深、自动化收益最大。

代码每次改动，`docs/` 里的接口清单、数据模型、架构图都可能漂移。如果不主动同步，半年后整个 `docs/` 没人敢相信，最后退化成「代码即文档」。一个 SKILL 把这件事自动化掉，团队再也不用花人力维护文档——**收益立竿见影**。

### 8.3 生成完整 SKILL.md

基于上面的候选，生成 docs-auto-sync 的完整 SKILL.md。要求：

```
基于上面的候选，生成 docs-auto-sync 的完整 SKILL.md。要求：

- 名字 docs-auto-sync
- description 写清楚什么场景触发、产出是什么
- steps 清晰可执行
- allowed-tools 限制到最小
- 重要：只汇报不一致的地方，不要自动改文件，让人决定怎么处理

保存到 .claude/skills/docs-auto-sync/SKILL.md。
```

产出：`.claude/skills/docs-auto-sync/SKILL.md`。

#### (1) 为什么「只汇报不自动改」

SKILL 设计有个原则：**只读不写**。`allowed-tools` 限制到 `Read, Grep`，不给 `Edit, Write`。

类比监控告警系统——**只通知不自动改代码**。原因是文档同步的判断需要人：接口字段从 `userId` 改成 `userIdStr`，**可能是文档没跟上、也可能是代码改错了**。SKILL 自动改文件，会把代码改错的情况误判成文档落后、把文档「修正」成错误的样子。<span style="color: red; font-weight: bold;">只报告不一致，让人决定怎么处理</span>，是安全的设计。

### 8.4 让 SKILL 生效并测试

完全退出 Claude Code 再重新启动，让 SKILL 生效。

测试 SKILL 是否可用：

```
我刚改完一批 Controller，帮我看看文档还对不对得上。
```

Claude Code 应该自动加载 docs-auto-sync 这个 Skill 并按步骤跑。

## 9. 一键跑完全流程：让 Claude Code 自主执行

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/da0de22eb743aa4c77bae5f2f41c9f60_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

§5-§8 四个场景一个个跑，是为了让读者看清每一步的产出和 review 点。**真正上手之后，工程师会希望一次粘贴、Claude Code 自主跑完所有步骤、遇到问题自己修、跑完自己验收**。

下面这段提示词就是干这个的。整段粘贴到 Claude Code，去喝杯咖啡，回来就齐了。

### 9.1 一键提示词全文

```
我刚 clone 了 Spring AI Alibaba Admin。现在帮我完整摸清这个项目，
产出一整套 AI 协作基础设施。整个过程你自主推进，遇到问题自己修、
自己 review、自己决定下一步，不要每一步都问我。

请按以下顺序执行：

第一步：画三张全景图，保存到 docs/

- architecture.svg（分层架构图，核心模块写一句话职责）
- module-deps.svg（内部模块依赖，循环依赖红色标出）
- external-deps.svg（Java 依赖 + 中间件 + 外部 API 三类）

第二步：梳理接口和数据模型

- docs/api-list.md（REST 接口清单,按模块分组,对外/内部区分）
- docs/data-model.md 和 docs/data-model-er.svg（以 DB 层为准）

第三步：对照以上两份，列出不一致的地方并修正，直到自洽

第四步：基于 docs/ 下的所有产出，生成项目根目录的 CLAUDE.md

- 前五节（项目定位、核心架构、关键模块、关键约定、怎么跑）你自己基于 docs/ 生成
- 禁区和历史包袱两节留空，写"待 Robert 补充"占位
- 整体控制在 300 行以内，不要把 docs/ 的内容复制进来

第五步：基于这个项目挖出最高优先级的一个 SKILL，生成完整的 SKILL.md

- 优先选"技术文档自动更新"（docs-auto-sync），解决代码改了但文档没跟上的问题
- 保存到 .claude/skills/docs-auto-sync/SKILL.md
- 只读不写（allowed-tools: Read, Grep）
- 步骤清晰，不自动修正，只报告

自主原则：

- 每一步跑完自己 review 输出质量，不合格自己重跑
- 图里有漏、有错、有不清晰的地方，主动补充或重画
- 遇到项目特有的细节（比如多模块、前后端分离），自己处理
- 所有步骤跑完后，生成一份 summary，列出每个产出文件、
  每份资产的主要内容概括、你认为还需要人工确认的地方

不要打断来问我。有判断不清的地方先做一个合理选择，
在最后的 summary 里标记出来。跑完再汇报。
```

粘贴完等 Claude Code 自己跑。**时间大概 15-30 分钟**，取决于模型速度和项目大小。

### 9.2 这段提示词的四要素拆解

这段提示词本质上是 §3 讲过的「四要素方法论」的一次完整落地。类比 Makefile / GitLab Pipeline——它定义了完整流程 + 自检 + 失败兜底。这里不再重复四要素的定义，而是聚焦**这段提示词如何把方法论落地成可执行的文本**：

| 要素               | 提示词里的关键句                      | 落地逻辑                                                                                                           |
| ---------------- | ----------------------------- | -------------------------------------------------------------------------------------------------------------- |
| 明确授权自主           | "不要每一步都问我"                    | 老项目改造里 AI 默认频繁确认，探索阶段合理，一键流程里会打断节奏。明确授权，AI 才会一口气跑完                                                             |
| 把 review 责任交给 AI | "每一步跑完自己 review 输出质量，不合格自己重跑" | 让 AI 对产出负责，不是产出完就丢给工程师。图漏了依赖自己补，接口清单扫漏了模块自己再扫一遍                                                                |
| 用 summary 替代中途打断 | "所有步骤跑完后，生成一份 summary"        | 把不确定的地方攒到最后一次性给工程师。**花 5 分钟读 summary 做决策，比中间被打断十次效率高得多**                                                       |
| 占位禁区和历史包袱        | "写'待补充'占位"                    | 这两节 AI 不该填，占位避免瞎编。<span style="color: red; font-weight: bold;">占位是诚实，编造是污染</span>——编出来的禁区看似合理，工程师一旦信了，后面改造会踩大坑 |

四要素不是抽象的方法论口号，而是这段提示词里每一句具体指令的底层逻辑。写自己的提示词时，照着这四条检查一遍，就不会漏掉关键约束。

## 10. 跑完之后的样子：验收与演化

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/02ec1f1647731f6997c6dee8cf63fa24_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

跑完"了解项目"的全套动作，回头按清单验收产出，再想清楚这套基础设施后面怎么演化。

跑完后项目目录就是 §4.4 那个样子：`docs/` 下五份资产、根目录 `CLAUDE.md`、`.claude/skills/` 下第一个 SKILL。

不再贴目录树，直接看每份资产的分工和验收标准。

### 10.1 七份资产的分工

七份资产像一个开发团队的七个角色——架构师、接口负责人、DBA、README 维护人、CI 维护人。缺一个角色就有一块工作没人做。每份资产对应 AI 协作时的一个心智维度：

| 资产 | AI 读它的目的 |
|------|--------------|
| 三张全景图 | 理解项目的整体形态和依赖关系 |
| 接口清单 | 知道对外契约，改接口时不破坏 |
| 数据模型 | 知道数据底座，改字段时不踩坑 |
| CLAUDE.md | 知道项目常识、禁区、历史包袱 |
| SKILL | 自动跑团队反复做的流程 |

任何一份缺失，AI 在某个维度上就是"瞎子"——没有数据模型，AI 改接口时不知道字段对应哪张表；没有禁区，AI 改代码时不知道哪段动不得。

### 10.2 验收 Check List

按四张清单收口：资产齐全度、一键提示词四要素、流程顺序、预期时间。每张表对应前面讲过的内容，这里只做验收。

#### (1) 资产齐全度 Check List

| 检查项 | 通过标准 |
|--------|---------|
| `docs/architecture.svg` 存在 | 体现前后端分离、OTel trace 链路、server-start 模块 |
| `docs/module-deps.svg` 存在 | start 依赖 runtime 和 openapi、两者都依赖 core，方向无倒置 |
| `docs/external-deps.svg` 存在 | MySQL、Nacos、OTel Collector、外部模型 API（DashScope/OpenAI/DeepSeek）齐全 |
| `docs/api-list.md` 存在 | 三个模块的 Controller 全扫到、对外/内部接口分开标注 |
| `docs/data-model.md` 和 ER 图存在 | 以 DB 层为准、entity/DTO 分开、隐式外键反查出来 |
| 接口清单 vs 数据模型已互相校对 | 不一致点已修正，两份资产自洽 |
| 项目根 CLAUDE.md 存在 | 不超过 300 行、不重复 `docs/`、禁区/历史包袱有真实内容 |
| `.claude/skills/` 下至少一个 SKILL | SKILL 的 allowed-tools 最小化、不自动改文件只报告 |

一项没过，对应资产就要回 §5-§8 重跑或手补。

#### (2) 一键提示词四要素 Check List

自己写一段自主流程提示词（不直接用 §9.1 那段），对照这四条查一遍：

| 要素 | 通过标准 |
|------|---------|
| 明确授权自主 | 出现"不要每一步都问我"、"判断不清先决断再标记" |
| review 责任交给 AI | 出现"每一步跑完自己 review 输出质量，不合格自己重跑" |
| 用 summary 替代中途打断 | 出现"所有不确定的地方攒到最后一次性汇报" |
| 占位 AI 不该填的内容 | 出现"禁区/历史包袱写'待补充'占位" |

四条缺一条，一键流程就会跑得不顺：缺"授权自主"AI 会一直打断；缺"review 责任"AI 会产出垃圾；缺"summary"会被中途打断十次；缺"占位"AI 会编造禁区。

#### (3) 流程顺序 Check List

资产之间的依赖决定了执行顺序，不能乱：

1. 先画三张全景图（架构、模块依赖、外部依赖）
2. 再梳理接口清单和数据模型
3. 接口清单和数据模型互相校对，直到自洽
4. 基于 `docs/` 生成 CLAUDE.md 前五节
5. 手写 CLAUDE.md 的禁区和历史包袱
6. 基于项目和 CLAUDE.md 挖出第一个 SKILL

跳步会导致下游资产没有原料。<span style="color: red; font-weight: bold;">最常见的错误是跳过第 3 步直接写 CLAUDE.md——结果 CLAUDE.md 里复述了一份对不上的接口清单。</span>

#### (4) 验收时间 Check List

| 阶段 | 预期耗时（Claude Code 自主跑） |
|------|------------------------------|
| 单个场景（如画三张图） | 3-5 分钟 |
| 四个场景分别跑 | 15-25 分钟 |
| 一键流程（含自主 review） | 15-30 分钟 |

一键流程跑超 30 分钟还卡着，多半是 AI 在某个场景反复重跑——这是 review 责任生效的表现，但需要看 summary 判断是否真有解不开的卡点。

### 10.3 基础设施是活的，会跟项目一起长

跑完一次不等于一劳永逸。类比传统软件——代码库会演化、CI 会迭代、README 会更新，AI 协作基础设施也一样，<span style="color: red; font-weight: bold;">它是活的，会跟项目一起长。</span>

三个演化点要心里有数：

- **`docs/` 会被 SKILL 持续校对**：代码改完后，docs-auto-sync 这类 SKILL 会主动提示哪些资产漂移了，让工程师决定是否更新。`docs/` 不再是写完就烂的死文档。
- **CLAUDE.md 的禁区会越写越多**：刚接手项目时可能只有一两条，改造过程中踩一个坑补一条——这正是"没思路就先占位"的后续动作。
- **`.claude/skills/` 里会多出第二三个 SKILL**：docs-auto-sync 只是起点，后续可能挖出"接口契约校验""迁移脚本生成"等流程。<span style="color: red; font-weight: bold;">我的看法是控制在 5 个以内——SKILL 太多反而让 AI 选择困难，违反"最小化"原则。</span>

<span style="color: red; font-weight: bold;">基础设施不是终点。</span>后面的改造、新增需求、Bug 修复，都会基于这套基础设施进行。

## 11. 下一步：从能理解到能兜底

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/4e47b2439168a415157681b2105106ad_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

AI 协作基础设施齐了，但只是起点——理解了项目还不够，还要能跑起来、能验证、能兜底：

| 要求 | 含义 |
|------|------|
| 能跑起来 | 编译通过、能在本地启动、能复现一个真实请求 |
| 能验证 | 有测试可跑、改完代码能快速确认没破坏 |
| 能兜底 | 有护栏机制，AI 改坏了能兜住 |

这三层是改造真正开始前必须建好的安全网，类比传统软件的自动化测试 + 灰度发布 + 回滚机制。**没有护栏直接做改造，等于在没系安全带的高速上开车**——AI 一旦改坏，回退成本极高。

AI 协作基础设施是护栏建设的前置条件：编译/测试/护栏的 SKILL 都会基于 CLAUDE.md 写、改动校验都依赖 `docs/` 里的资产。<span style="color: red; font-weight: bold;">先有地图才能修路，先有基础设施才能建护栏。</span>

## 12. 小结与思考

### 12.1 小结

"了解项目"的全套动作串成一条主线：七份资产（三张全景图 + 接口清单 + 数据模型 + CLAUDE.md + 第一个 SKILL）按依赖顺序产出，配上一段遵循四要素方法论的一键提示词，让 Claude Code 自主跑完。

**七份资产齐全 + 一键提示词可用 = AI 协作基础设施就位**。后面所有改造都基于这套基础设施进行。

### 12.2 思考

两个问题留给读者：

1. **跑完整套流程大约花了多少时间？** 在自己公司的项目上跑一遍，估计会花多久？公司项目通常比开源项目更复杂——模块更多、依赖更乱、隐性知识更深，预期时间应该乘以 2-3 倍。

2. **七份资产里（5 份 `docs/` + 1 份 CLAUDE.md + 1 份 SKILL），哪一份对团队价值最大？为什么？**

第二个问题没有标准答案，看团队最痛的是哪一块。我的判断按团队类型分：

| 团队类型 | 价值最大的资产 | 原因 |
|---------|--------------|------|
| 文档长期腐烂 | 第一个 SKILL（docs-auto-sync） | 把腐烂问题自动化——代码改完 SKILL 主动提示漂移，文档不再烂在角落 |
| 新人入职频繁 | CLAUDE.md | 把隐性知识显性化——新人不用靠"老员工口口相传"才能上手 |
| 跨团队协作多 | 接口清单 | 把对外契约固化——跨团队联调时不用再靠"对方向你解释半天" |

不同团队答案不同。但有一点是确定的：<span style="color: red; font-weight: bold;">七份资产是一个整体，缺任何一份，AI 协作的某个维度都会瘸腿</span>。

