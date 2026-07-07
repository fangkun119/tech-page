---
title: 传统项目迁AI 12：了解项目 - 流程回顾
author: fangkun119
date: 2026-07-04 12:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
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

## 1. 导读地图：本篇怎么读

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/c5d091b6bbe27aa5aa7987a447f82054_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是「了解项目」阶段的收尾篇，紧接系列第 11 篇（提炼 SKILL）。系列第 6 篇给过八步心法，第 7-11 篇把每一步在 Spring AI Alibaba Admin 上分别落了地——画图工具、三张全景图、接口清单与数据模型、CLAUDE.md、第一个 SKILL。但分篇讲清每一步，不等于工程师真的能一口气跑完。

本篇把分散在五篇里的动作串成一条线：**从 `git clone` 到 `docs/` 五份资产齐全、CLAUDE.md 写好、第一个 SKILL 装进去**，并在最后给一段可一键粘贴的自主执行提示词。跑完这一篇，老项目的 AI 协作基础设施就齐全了，下一部分（编译、测试、护栏）可以安心开始。

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/dc7ab4d685b9809259cdf410e6b70708_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    Start([本篇导读：流程回顾])
    Start --\> P1[第一部分 方法论提炼]
    Start --\> P2[第二部分 实战演示]

    P1 --\> P1A[2、为什么要回头串一遍]
    P1 --\> P1B[3、方法论：AI 协作基础设施的七份资产]
    P1 --\> P1C[4、方法论：让 AI 自主跑完全流程的四要素]
    P1 --\> P1D[5、第一部分 Check List<br/>可裁剪速查]

    P2 --\> P2A[6、主线项目延续<br/>Spring AI Alibaba Admin]
    P2 --\> P2B[7、场景一：画三张全景图]
    P2 --\> P2C[8、场景二：梳理接口和数据模型]
    P2 --\> P2D[9、场景三：生成 CLAUDE.md]
    P2 --\> P2E[10、场景四：挖出第一个 SKILL]
    P2 --\> P2F[11、一键跑完全流程]
    P2 --\> P2G[12、跑完之后的样子]
    P2 --\> P2H[13、与后续篇目的衔接]
    P2 --\> P2I[14、小结与思考]

    P1D -.速查.-> Senior([熟练工程师<br/>快速回顾])
    P2A -.代入.-> Beginner([初学工程师<br/>系统掌握])
-->

两类读者的推荐读法：

- **初学 AI 编程工程师**：建议通读全篇。第一部分建立"七份资产 + 一段自主提示词 = AI 协作基础设施"的整体认知；第二部分按四个场景加一段一键流程，把系列第 7-11 篇的动作完整复现一遍，看完就能在自己的老项目上照着跑。
- **熟练 AI 编程工程师**：可只看第一部分的 Check List 速查；接到一个老项目时，直接拿第 11 节那段一键提示词粘进 Claude Code，让 AI 自主跑完全流程，再回头按 Check List 验收七份资产即可。

## 2. 为什么要回头串一遍

「了解项目」阶段一共七篇，前六篇每篇只做一件事。这种分篇讲法适合学，但不适合用。真正上手时，工程师不会一篇一篇地切——而是 `git clone` 完，希望 Claude Code 一口气把所有资产都产出来。

更重要的是，**这七份资产不是独立的，它们之间有依赖关系**：CLAUDE.md 依赖 `docs/` 下的五份资产生成；SKILL 又依赖 CLAUDE.md 和 `docs/` 才能判断项目里哪些流程值得自动化。一份一份手敲能跑通，但跑不通时工程师往往不知道是哪一份出了问题。

本篇的价值就在这里：**把分散的动作串成一条主线，标清前置依赖和 review 点**，最后再给一段一键提示词。看完这一篇，工程师不仅知道每一步怎么跑，还知道为什么这个顺序、哪一步出问题该怎么回退。

## 3. 方法论：AI 协作基础设施的七份资产

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/e1e89baa41ab553798c285654126b3c0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/38b78b8d859c3802400e0076c480092f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

「了解项目」阶段的产物可以归纳为**七份资产**，分三类。把它们记牢，验收时按这张表点一遍就行。

| 类别 | 资产 | 路径 | 解决的问题 |
|------|------|------|-----------|
| 项目全景 | 架构图 | `docs/architecture.svg` | 让 AI 一眼看到分层和核心模块 |
| 项目全景 | 模块依赖图 | `docs/module-deps.svg` | 让 AI 看清内部模块怎么连 |
| 项目全景 | 外部依赖图 | `docs/external-deps.svg` | 让 AI 知道项目靠什么活着 |
| 接口与数据 | 接口清单 | `docs/api-list.md` | 让 AI 知道对外契约 |
| 接口与数据 | 数据模型 | `docs/data-model.md` + `data-model-er.svg` | 让 AI 知道数据底座 |
| 项目常识 | CLAUDE.md | 项目根目录 | 让 AI 知道项目怎么跑、禁区在哪 |
| 操作资产 | SKILL.md | `.claude/skills/{name}/SKILL.md` | 让 AI 自动跑团队反复做的流程 |

### 3.1 七份资产之间的依赖关系

七份资产不是平级的，**它们之间有明确的上下游**：

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/ebe445b1ff703e6a98e732c1f0e308c1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
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

从图中能看出三件事：

#### (1) docs/ 是所有下游资产的原料

`docs/` 下的五份资产是基础。CLAUDE.md 的前五节、SKILL 的候选挖掘，都依赖这五份资产生成。**所以画图和梳理接口/数据模型必须先做**，做完之后 CLAUDE.md 和 SKILL 才有依据。

#### (2) 接口清单和数据模型必须互相校对

接口清单和数据模型各自扫一遍 Controller 和 entity 还不够——**两份资产必须互相校对**：接口里提到的每个实体，在数据模型里是否都有定义？数据模型里的关键字段，在接口入参/出参里是否都被用到？校对出的不一致点，让 AI 自动修正两份资产，直到自洽。

这一步是「了解项目」阶段最容易跳过的，但也是最有价值的。**两份资产对不上，CLAUDE.md 写出来就是错的**。

#### (3) CLAUDE.md 的禁区和历史包袱必须手写

CLAUDE.md 的前五节（项目定位、核心架构、关键模块、关键约定、怎么跑）可以基于 `docs/` 自动生成，但**禁区和历史包袱两节 AI 填不出来**——这两节依赖的是只有团队成员才知道的隐性知识：哪段代码动不得、哪些设计是当年为了赶工期留下的债。

让 AI 写这两节，结果一定是空话或编造。**正确做法是让 AI 留占位（"待补充"），由工程师手写**。没思路就先列一两条占位，改造过程中踩到坑了再补。

### 3.2 资产规模与质量的硬指标

资产齐全不等于资产合格。验收时除了"有没有"，还要看"够不够精"和"够不够准"：

| 资产 | 硬指标 | 不达标的常见原因 |
|------|--------|-----------------|
| 三张全景图 | 关键依赖无遗漏（MySQL、Nacos、OTel Collector、外部模型 API 都在） | AI 扫描范围不够，漏看 `application.yml` |
| 接口清单 | 三个模块（core/openapi/runtime）的 Controller 全扫到、对外/内部接口分开标注 | AI 只扫了入口模块的 Controller |
| 数据模型 | 以 DB 层为准、entity 和 DTO 分开说、隐式外键关系反查出来 | AI 把 entity 和 DTO 混在一起 |
| CLAUDE.md | 总长不超过 300 行、不重复 `docs/` 内容（用链接指向）、禁区/历史包袱有真实内容 | AI 把 `docs/` 内容复制进 CLAUDE.md |
| SKILL.md | allowed-tools 限制到最小、不自动改文件只报告 | AI 给了过宽的工具权限 |

**总长 300 行是 CLAUDE.md 的硬上限**。超过 300 行说明 CLAUDE.md 在重复 `docs/` 的内容，AI 读 CLAUDE.md 时上下文会被无效内容占满，反而降低判断质量。

## 4. 方法论：让 AI 自主跑完全流程的四要素

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/e9a732a6145409f6ce2ead3027a14057_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

「了解项目」阶段的另一个关键产物，是**一段能让 Claude Code 自主跑完全流程的提示词**。这段提示词不是"把四个场景的提示词拼起来"那么简单——它要求 AI 自己 review、自己修、跑完一次性汇报。

写好这段提示词，有四个要素缺一不可。

### 4.1 要素一：明确授权自主

老项目改造里 AI 默认会频繁确认——这是探索阶段的合理行为，但在一键流程里会打断节奏。**提示词必须明确写"不要每一步都问我"**，AI 才会真的一口气跑完。

更进一步，要写明"遇到判断不清的地方先做一个合理选择，在最后的 summary 里标记出来"。这一句把 AI 的犹豫转化成决策：**有不确定的地方先决断、再标记，而不是停下来问**。

### 4.2 要素二：把 review 责任交给 AI

一键流程里，AI 不能产完就丢给工程师。提示词必须明确"每一步跑完自己 review 输出质量，不合格自己重跑"。这一句让 AI 对产出负责：

- 图里有漏、有错、有不清晰的地方，主动补充或重画
- 接口清单扫漏了模块，自己再扫一遍
- 数据模型和接口对不上，自己校对修正

**review 责任不交给 AI，AI 就只是个执行器**；交出去之后，AI 才是协作伙伴。

### 4.3 要素三：用 summary 替代中途打断

中途打断是效率杀手。一段 15-30 分钟的自主流程，如果中间被打断十次问"这个细节怎么处理"，工程师实际花的时间远超流程本身。

正确做法是**让 AI 把所有不确定的地方都攒到最后，一次性写在 summary 里**。工程师花 5 分钟读 summary、做决策，比中间被打断十次效率高得多。summary 里要包含三类信息：

| summary 内容 | 例子 |
|-------------|------|
| 每个产出文件的路径 | `docs/architecture.svg` |
| 每份资产的主要内容概括 | "架构图体现了前后端分离和 OTel trace 链路" |
| 还需要人工确认的地方 | "module-deps 图里 start → runtime 的方向我标了，请你确认" |

### 4.4 要素四：占位 AI 不该填的内容

CLAUDE.md 的禁区、历史包袱，AI 不该填。**提示词要明确让 AI 写"待补充"占位**，避免 AI 瞎编。

这个原则可以推广：**任何依赖团队隐性知识的内容，都应该让 AI 占位而非编造**。AI 编出来的禁区看起来合理，但一旦工程师信了，后面改造会踩大坑。占位是诚实的表现，工程师看到"待补充"就知道这里要自己来。

## 5. 第一部分 Check List：流程回顾速查

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/23b8b88ad71a1965b1fd24bcdcaeff12_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接到一个老项目，要快速搭起 AI 协作基础设施时，按下面的 Check List 走。每一条都对应前面讲过的方法论，可以裁剪后贴到项目自己的 CLAUDE.md 或 wiki 里。

### 5.1 资产齐全度 Check List

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

### 5.2 一键自主流程提示词的四要素 Check List

写一段自主流程提示词时，对照这四条查一遍：

| 要素 | 通过标准 |
|------|---------|
| 明确授权自主 | 出现"不要每一步都问我"、"判断不清先决断再标记" |
| review 责任交给 AI | 出现"每一步跑完自己 review 输出质量，不合格自己重跑" |
| 用 summary 替代中途打断 | 出现"所有不确定的地方攒到最后一次性汇报" |
| 占位 AI 不该填的内容 | 出现"禁区/历史包袱写'待补充'占位" |

### 5.3 流程顺序 Check List

资产之间的依赖决定了执行顺序，**不能乱**：

1. 先画三张全景图（架构、模块依赖、外部依赖）
2. 再梳理接口清单和数据模型
3. 接口清单和数据模型互相校对，直到自洽
4. 基于 `docs/` 生成 CLAUDE.md 前五节
5. 手写 CLAUDE.md 的禁区和历史包袱
6. 基于项目和 CLAUDE.md 挖出第一个 SKILL

跳步会导致下游资产没有原料。**最常见的错误是跳过互相校对直接写 CLAUDE.md**——结果 CLAUDE.md 里复述了一份对不上的接口清单。

### 5.4 验收时间 Check List

| 阶段 | 预期耗时（Claude Code 自主跑） |
|------|------------------------------|
| 单个场景（如画三张图） | 3-5 分钟 |
| 四个场景分别跑 | 15-25 分钟 |
| 一键流程（含自主 review） | 15-30 分钟 |

如果一键流程跑超 30 分钟还卡着，多半是 AI 在某个场景反复重跑——这是 review 责任生效的表现，但需要看 summary 判断是否真有解不开的卡点。

## 6. 实战主线延续：Spring AI Alibaba Admin

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/ad2484f784e0eb18ce063e16741c4df9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二部分把上面方法论在 Spring AI Alibaba Admin 上跑一遍。**主线项目延续自系列第 6-11 篇**——同一个项目，同一套八步心法，本篇只是把动作串起来。

### 6.1 主线延续：从八步心法到七份资产

系列第 6 篇讲过八步心法：读 README → 看目录 → 找入口 → 跟请求 → 找配置 → 看依赖 → 划边界 → 画图。这八步对应到产物上，就是本篇的七份资产：

| 八步心法 | 对应资产 |
|---------|---------|
| 读 README + 看依赖 | 外部依赖图（`external-deps.svg`） |
| 看目录 + 划边界 | 模块依赖图（`module-deps.svg`） |
| 找入口 + 跟请求 | 接口清单（`api-list.md`） |
| 找配置 | 数据模型（`data-model.md`） |
| 画图 | 架构图（`architecture.svg`） |
| 综合 | CLAUDE.md、SKILL |

八步心法是过程，七份资产是结果。**过程可以拆篇讲，结果必须一起验收**。

### 6.2 本篇产出物

跑完本篇，项目目录里会多出以下文件：

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

这就是一个老项目的 AI 协作基础设施。

### 6.3 准备：clone 项目并启动 Claude Code

进入主线实操前，先准备项目环境：

```bash
git clone https://github.com/alibaba/spring-ai-alibaba.git
cd spring-ai-alibaba/spring-ai-alibaba-admin
mkdir -p docs
mkdir -p .claude/skills
```

在这个目录下启动 Claude Code，后面所有提示词都在这里跑。

## 7. 场景一：画三张全景图

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/38cef3daec9bb83242a53fe4a99d5545_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应系列第 8 篇。产出 `docs/` 下的三张 SVG。

### 7.1 第一步：架构图

#### (1) 画图

提示词：

```
读一下这个项目的 README 和顶层目录，给我画一张架构图。
前端、后端、数据库、中间件分层画，核心模块写一句话职责。
周边基础设施用一个方框概括就行，不用展开。
保存到 docs/architecture.svg。
```

产出：`docs/architecture.svg`

review 重点：有没有体现前后端分离、OpenTelemetry trace 链路有没有画上、server-start 有没有漏。

#### (2) 为什么先画架构图

架构图是入口，它给 AI 一个项目的整体心智模型。**有了这张图，后面所有对话都基于同一张地图**——聊模块依赖、聊接口、聊数据模型时，AI 脑子里都有一张分层图作参考。

跳过架构图直接画模块依赖，AI 会陷入"只见树木不见森林"的状态——能看清每个模块，但不知道这些模块组合起来是干什么的。

### 7.2 第二步：模块依赖图

#### (1) 画图

提示词：

```
看一下项目的 pom.xml，画一张内部模块依赖图。
只画项目自己的模块，外部库不画。有循环依赖用红色标出来。
保存到 docs/module-deps.svg。
```

产出：`docs/module-deps.svg`

review 重点：start 依赖 runtime 和 openapi、两者都依赖 core，方向不能倒。frontend 不应该出现在这张图里。

#### (2) 为什么模块依赖图要单独画

架构图画的是"项目对外长什么样"，模块依赖图画的是"项目内部怎么拼"。**这两张图服务的决策完全不同**：

- 看架构图决定"改造时动哪一层"
- 看模块依赖图决定"改这个模块会影响哪些下游"

把它们合并成一张图，信息密度太高、读不动；分开画，每张图都聚焦一个决策。

### 7.3 第三步：外部依赖图

#### (1) 画图

提示词：

```
综合看 pom.xml、application.yml 和 README，帮我梳理这个项目。
对外依赖了什么。分成三类：关键 Java 依赖、中间件、外部 API。
画出来，每类用不同颜色。保存到 docs/external-deps.svg。
```

产出：`docs/external-deps.svg`

review 重点：MySQL、Nacos、OTel Collector 都要在，外部模型 API（DashScope、OpenAI、DeepSeek）不能漏。

#### (2) 外部依赖图最容易漏的是外部 API

Java 依赖和中间件比较好扫——`pom.xml` 和 `application.yml` 里写得清清楚楚。**最容易漏的是外部模型 API**：DashScope、OpenAI、DeepSeek 这些 API 的 key 通常放在配置里、调用代码散在 service 层。

AI 扫描时如果不主动提"外部 API"这一类，往往会漏掉。**提示词里写明"分成三类：关键 Java 依赖、中间件、外部 API"**，就是在引导 AI 不漏第三类。

## 8. 场景二：梳理接口和数据模型

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/1748abb8c234326bd66c1f020cacbd51_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应系列第 9 篇。产出接口清单和数据模型文档。

### 8.1 第一步：REST 接口清单

提示词：

```
扫一下这个项目里所有的 Controller，给我整理一份 REST 接口清单。

每个接口列出方法、路径、一句话说明、主要入参、返回结构。

按模块分组。保存到 docs/api-list.md。
```

产出：`docs/api-list.md`

review 重点：server-core、server-openapi、server-runtime 三个模块的 Controller 都要扫到。对外接口和内部接口分开标注。

### 8.2 第二步：数据模型

#### (1) 画图

提示词：

```
看项目的 entity 类、DTO、数据库建表 SQL，给我梳理核心数据模型。

每个模型列出字段、类型、一句话说明。标出主键、外键、枚举值。

关键模型之间的关系画一张简单的 ER 图。

保存到 docs/data-model.md 和 docs/data-model-er.svg。
```

产出：`docs/data-model.md` + `docs/data-model-er.svg`

review 重点：以 DB 层为准、entity 和 DTO 分开说、通过 findBy 反查出隐式外键关系。

#### (2) 为什么要"以 DB 层为准"

项目里数据模型的表述有三层：DB 建表 SQL、entity 类、DTO。**三层经常会不一致**——DB 里字段叫 `user_id`，entity 里改成了 `userId`，DTO 里又变成了 `userIdStr`。

以 DB 层为准的原因：**DB 是合约的最终落地**。无论代码层怎么改名，DB 改不了。以 DB 为锚点，entity 和 DTO 的偏差都能讲清楚；反过来以 entity 为锚点，DB 和 DTO 的偏差就讲不清了。

### 8.3 第三步：两份资产互相校对

#### (1) 画图

提示词：

```
对照 docs/api-list.md 和 docs/data-model.md，
看接口里提到的每个实体在数据模型里是不是都有定义。
有不一致的地方列出来。
然后验证不一致的地方并修复。
```

产出：一份不一致点清单，AI 自动修正两份资产，直到自洽。

#### (2) 这一步是最容易跳过的，也是最有价值的

跑完前两步，`docs/` 里已经有五份资产：`architecture.svg`、`module-deps.svg`、`external-deps.svg`、`api-list.md`、`data-model.md` + `data-model-er.svg`。看上去齐全了，**但接口清单和数据模型可能对不上**——接口里返回的某个字段，数据模型里压根没定义；数据模型里的某个枚举，接口入参里压根没用到。

跳过这一步，CLAUDE.md 复述的就是两份对不上的资产。**互相校对是质量保证的关键一步**，AI 自己迭代修正，直到两份资产自洽。

## 9. 场景三：生成 CLAUDE.md

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/4f189dff0b480680a6fb02562dbafc8f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应系列第 10 篇。产出项目根目录的 CLAUDE.md。

### 9.1 基于 docs/ 生成 CLAUDE.md 初稿

提示词：

```
读 docs/ 下的所有资产，给我生成一份 CLAUDE.md 初稿。

精简：项目定位、核心架构、关键模块、关键约定、怎么跑，
外加两节空着的：禁区、历史包袱。

架构图、接口清单、数据模型的详细内容不要复制进来，
用链接指向 docs/ 就好。保存到项目根目录的 CLAUDE.md。
```

产出：项目根目录 CLAUDE.md（前五节 AI 生成、禁区和历史包袱留空）

### 9.2 手写禁区和历史包袱

#### (1) 为什么这两节 AI 填不出来

这两节依赖的是只有团队成员才知道的隐性知识：

- **禁区**：哪段代码动不得——可能是某个核心算法被性能调优过、改一行性能崩一半；可能是某个表结构被多个老系统共享、改字段全公司炸。
- **历史包袱**：哪些设计是当年为了赶工期留下的债——可能是某个 service 类承担了五个职责、想拆但拆不动；可能是某个接口返回结构混乱、客户端已经依赖这个混乱。

这些知识**不在 `docs/` 里、不在 README 里、不在 git log 里**。AI 扫遍所有文件都挖不出来。**硬让 AI 写，结果一定是空话或编造**——比留空危害更大。

#### (2) 没思路就占位

刚接手项目时，禁区可能一条都列不出来。**没关系，先列"待补充"占位**。改造过程中踩到坑了——发现某个类一动就崩、某个表一改就连锁——再回头补到禁区一节。

CLAUDE.md 是活的文档，**不需要一开始就完美**。但**禁区这一节的标题必须在**，提醒工程师改造时多想一步。

review 重点：总长度不超过 300 行、没有重复 `docs/` 的内容（都用链接指向）、禁区和历史包袱两节有真实内容。

## 10. 场景四：挖出第一个 SKILL

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/afdf7cda2fc99dd8d39b57840461926c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应系列第 11 篇。三步挖掘法：让 AI 分析项目、出 Top 3 候选、选一个生成完整 SKILL。

### 10.1 第一步：让 AI 分析项目重复流程

提示词：

```
扫一下当前项目（包括 git log、CLAUDE.md、docs/、README、CONTRIBUTING、.github/），
找出团队反复在做的操作流程。

判断标准是三特征：可复制、可参数化、可自动化。
三个都满足才算值得做 SKILL 的候选。

把找到的候选列出来，每个写明：流程名、为什么是反复的、能参数化的部分、
是什么、起点和终点是什么。最后给我用一个表格总结。
```

产出：5-10 项候选清单。

### 10.2 第二步：让 AI 出 Top 3 推荐

提示词：

```
从上面的清单里挑 3 个最高优先级的，给我做成候选 SKILL。
每个候选写：name（英文）、description、预期 steps、allowed-tools。

优先级判断标准：频率高、痛点深、自动化收益大。用表格总结，包含类型和理由。
```

产出：三个候选 SKILL。

#### (1) Top 3 大概率包含"技术文档自动更新"

Top 3 大概率包含**技术文档自动更新**（docs-auto-sync），因为它的频率最高、痛点最深、自动化收益最大。

代码每次改动，`docs/` 里的接口清单、数据模型、架构图都可能漂移。如果不主动同步，半年后整个 `docs/` 没人敢相信，最后退化成"代码即文档"。一个 SKILL 把这件事自动化掉，团队再也不用花人力维护文档——**收益立竿见影**。

### 10.3 第三步：生成完整 SKILL.md

基于上面的候选，生成 docs-auto-sync 的完整 SKILL.md。要求：

```
- 名字 docs-auto-sync
- description 写清楚什么场景触发、产出是什么
- steps 清晰可执行
- allowed-tools 限制到最小
- 重要：只汇报不一致的地方，不要自动改文件，让人决定怎么处理
```

保存到 `.claude/skills/docs-auto-sync/SKILL.md`。

产出：`.claude/skills/docs-auto-sync/SKILL.md`

#### (1) 为什么"只汇报不自动改"

SKILL 设计有个原则：**只读不写**。`allowed-tools` 限制到 `Read, Grep`，不给 `Edit, Write`。

原因是文档同步的判断需要人：接口字段从 `userId` 改成 `userIdStr`，**可能是文档没跟上、也可能是代码改错了**。SKILL 自动改文件，会把代码改错的情况误判成文档落后、把文档"修正"成错误的样子。**只报告不一致，让人决定怎么处理**，是安全的设计。

### 10.4 让 SKILL 生效并测试

完全退出 Claude Code 再重新启动，让 SKILL 生效。

测试 SKILL 是否可用：

```
我刚改完一批 Controller，帮我看看文档还对不对得上。
```

Claude Code 应该自动加载 docs-auto-sync 这个 Skill 并按步骤跑。

## 11. 一键跑完全流程：让 Claude Code 自主执行

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/cb9ba007939f2955671edf230ee983a0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面四个场景一个个跑，是为了让工程师看清每一步的产出和 review 点。**真正上手之后，工程师会希望一次粘贴、Claude Code 自主跑完所有步骤、遇到问题自己修、跑完自己验收**。

下面这段提示词就是干这个的。整段粘贴到 Claude Code，去喝杯咖啡，回来就齐了。

### 11.1 一键提示词全文

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

### 11.2 这段提示词的四要素拆解

这段提示词为什么这么写？对照第 4 节的四要素拆开看：

#### (1) 要素一：明确告诉 AI 自主

"不要每一步都问我"是关键一句。老项目改造里 AI 默认会频繁确认，这在探索阶段合理，但一键流程里会打断节奏。**明确授权自主决策，AI 才会真的一口气跑完**。

#### (2) 要素二：把 review 责任交给 AI

"每一步跑完自己 review 输出质量，不合格自己重跑"——这一句让 AI 对产出负责，不是产出完就丢给工程师。图里漏了依赖，AI 自己补；接口清单扫漏了模块，AI 自己再扫一遍。

#### (3) 要素三：用 summary 替代中途打断

让 AI 把"我不确定的地方"都攒到最后，一次性给工程师。**工程师花 5 分钟读 summary 做决策，比中间被打断十次效率高得多**。

#### (4) 要素四：占位禁区和历史包袱

这两节 AI 不该填，所以让它写"待补充"占位，避免 AI 瞎编。**占位是诚实，编造是污染**——编出来的禁区看起来合理，但一旦工程师信了，后面改造会踩大坑。

## 12. 跑完之后的样子

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/497540fc68df38cf09c74ef39e43254e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

一键流程跑完，项目目录长这样：

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

**这就是一个老项目的 AI 协作基础设施**。

### 12.1 七份资产的分工

七份资产看起来多，但每份都有清晰的分工：

| 资产 | AI 读它的目的 |
|------|--------------|
| 三张全景图 | 理解项目的整体形态和依赖关系 |
| 接口清单 | 知道对外契约，改接口时不破坏 |
| 数据模型 | 知道数据底座，改字段时不踩坑 |
| CLAUDE.md | 知道项目常识、禁区、历史包袱 |
| SKILL | 自动跑团队反复做的流程 |

任何一份缺失，AI 在某个维度上就是"瞎子"——比如没有数据模型，AI 改接口时不知道字段对应哪张表；没有禁区，AI 改代码时不知道哪段动不得。

### 12.2 这是基础设施，不是终点

跑完本篇，AI 协作基础设施就齐了。**但基础设施只是起点**——后面的改造、新增需求、Bug 修复，都会基于这套基础设施进行。

随着改造推进，七份资产会演化：

- `docs/` 里的资产会被 SKILL（如 docs-auto-sync）持续校对和提示更新
- CLAUDE.md 的禁区会越写越多——踩一个坑补一条
- `.claude/skills/` 里会多出第二个、第三个 SKILL（但建议控制在 5 个以内）

基础设施是活的，会跟着项目一起长。

## 13. 与后续篇目的衔接

<img src="imgs/aicmigr-12-proj-rd-07-process-recap/9ab5aba3ad7ee23c3053f1ac8bc536b4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

「了解项目」阶段到此结束。**从系列第 13 篇开始进入第三部分：编译 + 测试 + 建立护栏**。

为什么需要第三部分？理解了项目还不够，**要能跑起来、能验证、能兜底**：

- **能跑起来**：编译通过、能在本地启动、能复现一个真实请求
- **能验证**：有测试可跑、改完代码能快速确认没破坏
- **能兜底**：有护栏机制，AI 改坏了能兜住

有了这三层，到第四部分才能安心动手做真实的需求改造。**没有护栏直接做改造，等于在没系安全带的高速上开车**——AI 一旦改坏，回退成本极高。

本篇产出的 AI 协作基础设施，是第三部分护栏建设的前置条件：编译/测试/护栏的 SKILL 都会基于 CLAUDE.md 写、改动校验都依赖 `docs/` 里的资产。**先有地图才能修路，先有基础设施才能建护栏**。

## 14. 小结与思考

### 14.1 小结

「了解项目」阶段从系列第 6 篇八步心法到第 11 篇第一个 SKILL，整个方法论在 Spring AI Alibaba Admin 上跑完一遍。本篇把分散在五篇里的动作串成一条主线，最后给了一段可一键粘贴的自主执行提示词。

跑完这一篇，老项目的 AI 协作基础设施——**三张全景图 + 接口清单 + 数据模型 + CLAUDE.md + 第一个 SKILL**——就齐全了。

接下来从系列第 13 篇开始第三部分：编译 + 测试 + 建立护栏。理解了项目还不够，要能跑起来、能验证、能兜底。有了这些，到第四部分才能安心动手做真实的需求改造。

### 14.2 思考

跑完整套流程大约花了多少时间？在自己公司的项目上跑一遍，估计会花多久？**公司项目通常比开源项目更复杂——模块更多、依赖更乱、隐性知识更深**，预期时间应该乘以 2-3 倍。

本篇产出的 7 份资产里（5 份 `docs/` + 1 份 CLAUDE.md + 1 份 SKILL），哪一份对团队价值最大？为什么？

不同团队的答案不一样：

- **文档长期腐烂的团队**：第一个 SKILL（docs-auto-sync）价值最大，因为它把腐烂问题自动化了
- **新人入职频繁的团队**：CLAUDE.md 价值最大，因为它把隐性知识显性化了
- **跨团队协作多的团队**：接口清单价值最大，因为它把对外契约固化了

**没有标准答案，看团队最痛的是哪一块**。但有一点是确定的：七份资产是一个整体，缺任何一份，AI 协作的某个维度都会瘸腿。
