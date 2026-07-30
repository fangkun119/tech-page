---
title: 传统项目迁AI 23：阶段复盘 - 流程复用实战
author: fangkun119
date: 2026-07-04 23:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/cover.jpg
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
aicmigr-23-mid-recap-02-workflow-reuse-practice
传统项目迁AI 23：阶段复盘 - 流程复用实战
-->

## 1. 反直觉现象：为什么第二次反而轻松？

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/4898af69b02144b78b58e1fcecddbdd6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是"传统项目迁 AI"系列第 23 篇，回答一个具体的问题：**同一套 AI 编程工作流，换一个功能还能跑通吗？跑通的过程长什么样？提示词会变多还是变少？**

先把答案亮出来：**积累在前、轻松在后**。第二次跑同样的工作流，<span style="color: red; font-weight: bold;">提示词从 9 条压缩到 1 条，但功能反而更复杂——29 个改造点对第一次的 12 个。</span>前若干篇攒下的三类资产，在这一篇被一次性兑现。

你可能会问：<span style="color: red; font-weight: bold;">功能更复杂、工作量翻倍，提示词反而少了 8 条，这不矛盾吗？</span>把两次改造放在一起对照，反差就一目了然：

| 维度 | 第一次（第 17–21 篇 Prompt 版本对比） | 第二次（本篇 Overview） |
|------|--------------------------------------|----------------------|
| 功能范围 | 单模块 | 7 个模块，跨 Admin + AgentScope 两个数据库 |
| 改造点数量 | 12 个 | 29 个（17 后端 + 10 前端 + 2 文档） |
| 后端技术难点 | null 处理、复用判断 | 跨库 Mapper 注入、Redis 缓存、SQL 聚合性能 |
| 前端改造类型 | 改现有组件 | 新建页面 |
| 提示词数量 | 9 条（拆需求 + 拆方案） | 1 条 |
| 资产交互 | 单向读取 `docs/` 与 `CLAUDE.md` | 读取 + 回灌新约束到 `CLAUDE.md` |

<span style="color: red; font-weight: bold;">最后一行才是关键对照：功能更复杂、改造点翻倍、工作量翻倍，提示词反而少了 8 条。</span>

<span style="color: red; font-weight: bold;">**慢就是快**——第一次跑是在搭脚手架，第二次跑是在用脚手架。</span>

这个现象在传统软件工程里并不陌生。打个类比：新人入职第一周，要手把手带他读架构文档、过代码规范、走一遍历史模块；到第二周，只需要丢一句"把这个接口加个分页"，他就能独立完成。AI 在项目里也是一样——它"读"过的东西、你立过的规矩、你带它跑过的流程，都会沉淀成项目的资产，让下一次任务越跑越轻。

这些"脚手架"具体是什么？下一章揭晓。

## 2. 资产飞轮：让 AI 越用越顺的三类资产

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/1116f153ebb0442016fff6f6a0ff9f83_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二次跑同样的工作流，AI 凭一句话就能产出三份高质量文档（需求 / 改造影响 / 技术方案）。凭什么？

<span style="color: red; font-weight: bold;">凭的不是模型变聪明了，而是项目里"攒了东西"。</span>接续上一章的新人类比：第二周的新人为什么能独立干活？因为他读完了架构文档、团队规范、历史代码，你给一句话任务，他能自己定位到改哪里、遵守什么规矩。AI 也一样——它"读"过的东西，会沉淀成项目的资产。

具体是三类资产，对应传统工程师最熟悉的三类文档：

| 资产 | 文件位置 | 作用 | 何时攒下 | 传统工程类比 |
|------|---------|------|---------|------------|
| 项目脑图 | `docs/`（架构图 / 模块依赖 / API 清单 / 数据模型） | AI 拆需求时直接读，不需要重新扫代码 | 第 8–9 篇（绘图 + 接口与数据模型） | 架构说明书 + API 字典 + 数据模型文档 |
| 项目级约束 | `CLAUDE.md`（禁区 / 历史包袱 / 代码风格） | AI 写方案时自动遵守，不需要每次重申 | 第 10 篇（提炼 CLAUDE.md） | 团队编码规范 + 项目约束清单 |
| 改造模板 | 第一次改造的提示词序列 | 同工序直接复用，不需要重新设计提示词 | 第 17–21 篇（第一次完整改造） | 标准 SOP（标准作业程序） |

- **项目脑图**让 AI 知道项目长什么样——架构怎么分层、模块怎么依赖、接口和数据模型是什么。相当于新人手边那本架构说明书。
- **项目级约束**让 AI 知道什么不能碰——哪些是历史包袱禁区、代码风格要求什么、哪些坑踩过不能再来。相当于贴在团队 Wiki 上的编码规范。
- **改造模板**让 AI 知道这类活儿怎么干——拆需求、拆方案、写代码、补测试、回灌文档的完整提示词序列。相当于车间里的标准作业程序（SOP），同样的工序换个产品也能照着跑。

<span style="color: red; font-weight: bold;">三类资产叠加之后，AI 对项目的熟悉度接近一个新入职两周的工程师。</span>再让它做一次类似的功能，自然顺。

这里有一个值得停下来的判断：**<span style="color: red; font-weight: bold;">当一套提示词序列在一个项目里被反复复用、且只在结尾做参数化差异时，它就已经具备被沉淀为 Skill 的形态</span>**。本系列后续会专门讲到如何把这些模板固化成可复用的 Skill。

原理讲完了。但光讲原理不够扎实——下面用一次完整的实战，看资产飞轮在六个关键节点上怎么兑现。

## 3. 实战：给 SAA Admin 加 Overview 页面

### 3.1 案例背景

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/e627949bc88b9c341eb5c4f03f13a23d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

SAA Admin 已经具备 Application 管理、Prompt 工程、评测、可观测、MCP 等模块，但没有一个全局首页。用户登录后默认跳进 Application Management，要看其他模块的状态得逐个点过去。

补一个 Overview 页面，是几乎所有 Admin 系统都该有的标准功能，但改造复杂度并不低：

| 维度 | 数值 |
|------|------|
| 涉及模块数 | 7 个 |
| 跨库数量 | 2 个（Admin + AgentScope） |
| 缓存依赖 | Redis |
| 改造点总数 | 29 个（17 后端 + 10 前端 + 2 文档） |

让 Claude Code 画 PRD，可以用如下提示词：

```text
顶部 6 张指标卡：Prompt / Version / 实验 / 数据集 / 知识库 / 模型
中部左：实验状态饼图（DRAFT / RUNNING / COMPLETED / FAILED / STOPPED）
中部右：Top 10 Prompt 版本柱图（pre / release 对比）
底部左：最近活动 Timeline（UNION 三张表，最近 20 条）
底部右：文档索引状态（按知识库分组，进度条 + 数量）
```

Overview 页面长这样：

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/60dfa03265c736c1c00975562de9e393_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/60dfa03265c736c1c00975562de9e393_MD5.jpg
用途：展示 Overview 仪表盘页面的整体布局
内容：SAA Admin Overview 页面设计稿或实现稿，包含顶部 6 张指标卡、中部实验状态饼图与 Top 10 Prompt 版本柱图、底部 Timeline 与文档索引状态四个主要区域
-->

### 3.2 六大场景总览

Overview 改造完整跑下来分六个场景，对应工作流的六个关键节点。所有场景都按"提示词 / 产出 / 点评"三段式展开。

```mermaid
flowchart LR
    S1["场景一<br/>现状确认"] --> S2["场景二<br/>一句话出三份文档"]
    S2 --> S3["场景三<br/>人工校验"]
    S3 --> S4["场景四<br/>后端改造"]
    S4 --> S5["场景五<br/>前端改造"]
    S5 --> S6["场景六<br/>文档回灌"]
    S6 -.资产飞轮.-> S1
```

### 3.3 场景一：现状确认

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/81748dd6380dc57af78f418d9f2e6ff8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

为不踩坑、不做重复功能，第一步先打开浏览器、登录账号 `saa/123456`，点一遍左侧菜单：Agent Builder（Application / MCP / Component / Knowledge）、Prompt 工程、评测、可观测。确认所有模块都没有"全局概览"入口，默认登录跳 Application Management。

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/613ecd5b237762f9514d49b4eb31e1a0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/613ecd5b237762f9514d49b4eb31e1a0_MD5.jpg
用途：展示改造前对 SAA Admin 现状的逐菜单确认结果
内容：SAA Admin 左侧菜单展开后的浏览器截图，覆盖 Agent Builder / Prompt 工程 / 评测 / 可观测等模块，用以确认现有功能没有"全局概览"入口
-->

这一步的提示词省了——确认就是确认，不需要 AI。这正是第 20 篇翻车后留下的硬约束：**改造前必须自己点一遍产品**。

为什么不省掉这一步？因为 AI 拆需求时不会主动点你的产品，它只看代码。代码里可能早就有一个半成品入口、或一个名字相近的功能模块，AI 看了就以为"这功能已经有了"，直接给一份重复造轮子的方案。<span style="color: red; font-weight: bold;">人花十分钟点一遍菜单，比 AI 花两小时写出一份废方案划算得多。</span>

### 3.4 场景二：一个提示词出三份文档

原本第 17 篇拆需求 + 第 18 篇拆方案要跑九个提示词，这次合并成一句简短的提示词：

```text
我加一个 overview 页面。你规划一下 overview 放什么、有什么、
数据怎么统计、接口怎么定义、前端页面怎么展示。全流程。
然后根据之前的流程 docs 里面出需求文档、技术方案、改造点文档。能做到吗？
```

就这一个提示词，AI 跑出来三份完整文档：

- `docs/requirements/overview-dashboard.md`（需求）
- `docs/requirements/overview-dashboard-impact.md`（改造影响分析）
- `docs/requirements/overview-dashboard-solution.md`（技术方案）

三份文档加起来超过 800 行，覆盖：6 维需求拆解、29 个改造点列表、跨库查询风险分析、5 个关键决策、详细 SQL、目录结构、实施顺序、mock 方案。

5 个关键决策列在下表，便于快速回顾：

| 决策项 | 最终选择 |
|--------|---------|
| 是否引入图表库 | 不引（用 antd 现有组件） |
| 缓存粒度 | 整体缓存 5 分钟 |
| 跨库查询是否引 ES | 不引（MySQL UNION） |
| 是否做实时刷新 | 不做 |
| 是否做时间筛选 | 不做 |

为什么这么短一句提示词就能产出三份文档？因为前面已经把上下文跑了一遍、跑了一遍完整的改造流程、并在最后做了文档自动更新。<span style="color: red; font-weight: bold;">**慢就是快**——项目改造的齿轮一旦跑起来，就会越来越快。</span>

链路图：

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/19e8945fffd7ab6f5ed27abb6d06e1ee_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/19e8945fffd7ab6f5ed27abb6d06e1ee_MD5.jpg
用途：展示一个提示词产出三份文档（需求 / 改造影响 / 技术方案）的链路图
内容：从单条提示词出发，AI 同时产出 docs/requirements/overview-dashboard.md、overview-dashboard-impact.md、overview-dashboard-solution.md 三份文档的流程链路图
-->

**点评**：<span style="color: red; font-weight: bold;">跟上次改造比，这次拆需求、拆方案的提示词从 9 条压缩到 1 条，这就是积累的力量。</span>AI 凭一句话产出三份文档，是因为它已经从 `docs/`、`CLAUDE.md`、第一次改造模板里拿到了所有需要的上下文。如果是新项目第一次跑，这一步还是要拆成 9 个提示词。**积累在前、轻松在后**。

### 3.5 场景三：人工校验三份文档

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/c4445d3d8015c9d0ffe4b34c6e74ec19_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

到这一步必须停下来。

AI 一次出三份文档很爽，但不能直接进下一步动代码。即便 Claude Code 的改造效果会很好甚至可能完美、不需要做任何更改，<span style="color: red; font-weight: bold;">人工校验三份文档的关键内容这一步绝对不能省</span>：

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/0bfb918805a6b0181dbd8fd1e90501d5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/0bfb918805a6b0181dbd8fd1e90501d5_MD5.jpg
用途：展示人工校验三份文档（需求 / 改造影响 / 技术方案）的关键检查点
内容：人工审核三份文档的清单或检查流程示意，强调审核环节不可省略
-->

校验完发现需要调整的，反馈给 AI 让它更新文档，格式如下：

```text
我审核了三份文档，以下需要调整：
- [问题 1]
- [问题 2]
- 5 个决策全部拍板：[逐条最终决策]

按上面更新文档。所有"待审核"标识改成最终决策。
```

**点评**：这是全篇最重要的一处提醒。第 22 篇讲过的"工作量下来 ≠ 思考量下来"在这里有最直接的落地。AI 压缩了 90% 的工作量，省下来的时间必须用在校验上。<span style="color: red; font-weight: bold;">如果偷懒、直接用 AI 出的文档进入开发，就把"快"变成了"快错"。</span>**<span style="color: red; font-weight: bold;">这一步的校验质量决定了后面三天的代码方向。</span>**

为什么这一步即便 AI 越来越强也不能省？因为 AI 拿到的上下文是你喂给它的，它对业务的判断不会超过你喂进去的那部分。它可能漏掉一个只有你知道的边界条件、可能选了一个技术上正确但产品上不合适的方案。<span style="color: red; font-weight: bold;">人花半小时过一遍决策点，是把"方向对不对"的责任牢牢攥在自己手里。</span>

### 3.6 场景四：执行后端改造

提示词也是一句：

```text
按 docs/requirements/overview-dashboard-solution.md 完成后端改造。

按 19 讲的小步执行原则跑：分批做（DTO 一批、Service 一批、Controller
一批、Mapper 新增 COUNT 方法一批）、每批跑通 mvn test 才进下一批、
不重构现有 Mapper、跨库 Mapper 注入参考 ModelConfigBridgeServiceImpl。
跑通后告诉我总测试数 + 失败数。
```

为什么这么短？因为所有具体约束（字段对齐、Redis 缓存、跨库注入、SQL 写法）都已经在 `solution.md` 里了，AI 读这份文档就能拿到所有信息，提示词只需指向它。

后端代码长什么样、SQL 怎么拼，跟上次的工作流一致，本篇不展开（第 19 篇已讲透）。

**点评**：跟上次比，这次后端改造的核心难点变了——上次是 null 处理、复用判断；这次是跨库 Mapper 注入、Redis 缓存、SQL 聚合性能。<span style="color: red; font-weight: bold;">同样的工作流，落到具体代码上的关注点完全不同——这就是</span>**工作流可迁移**的真实含义。如果今天在自己项目上做类似的聚合首页，技术细节会和这次不一样（可能用 ehcache 不是 Redisson、可能不需要跨库），<span style="color: red; font-weight: bold;">但工作流的关键节点完全一样：先确定决策点、再小步实现、再补测试、最后回灌资产。</span>

### 3.7 场景五：执行前端改造

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/09b65204247b1fbcb0b4b91f68136929_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

```text
按 docs/requirements/overview-dashboard-solution.md 完成前端改造。

不引入 echarts，全用 antd 现有组件。改完跑前端构建确认无报错。
```

**点评**：跟上次比，这次是新建页面、不是改现有组件，所以前端改造的整体节奏更顺——没有 props 破坏性变更、没有现有调用方需要同步更新，直接按设计稿建组件即可。

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/200d4e5e12c4f8c6a5cc9ad2694bf991_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/200d4e5e12c4f8c6a5cc9ad2694bf991_MD5.jpg
用途：展示前端改造完成后的 Overview 页面效果
内容：前端 Overview 页面的运行截图，包含指标卡、饼图、柱图、Timeline、文档索引状态等区域，体现 antd 组件实现效果
-->

但有一处需要特别注意：**菜单配置文件位置不是凭直觉能猜对的**，必须自己点一遍产品 + 让 AI 扫代码确认。Overview 入口加在哪、用什么 icon，这些是第 17 篇老项目约束的具体形态。AI 默认会"建一个新菜单组件"，但项目里可能用配置数组管菜单，那就改配置数组即可。

这条提醒值得画重点。老项目里"菜单怎么管"这种细节，往往没有文档、只有代码里的一个配置数组。AI 第一次扫代码可能扫不到，你得主动告诉它"菜单是配置驱动的，改配置数组"。这正是 CLAUDE.md 该写入的项目级约束——下次再做类似功能，AI 就会主动问"菜单是配置驱动还是组件驱动"。

### 3.8 场景六：文档自动更新

Overview 改造跑完了。把这一轮新发现回灌到 `docs/`：

```text
1. api-list.md：加 GET /api/dashboard/overview
2. data-model.md：加 DashboardOverviewResult 顶层 + 10 个子 DTO
3. overview-dashboard.md：补审核中新发现的边界
4. CLAUDE.md：加项目级新约束。候选：
   - "聚合多模块的接口默认整体缓存（Redis），按卡片细粒度缓存需要明确理由"
   - "跨 admin / agentscope 库查询的 Service 必须放在 server-start 模块，参考 ModelConfigBridgeServiceImpl 先例"

判断哪些是项目级 → 写进 CLAUDE.md，哪些是这一次特殊处理 → 留在 solution.md。
输出每份文件的改动 diff。
```

**点评**：跟上次比，这次回灌到 `CLAUDE.md` 的新约束更通用。"跨库 Service 放 server-start"这条是项目级的：下次再有类似 Overview 的功能（运营 Dashboard、成本概览、健康度报表），AI 会主动提醒"跨库查询走 server-start，参考 `ModelConfigBridgeServiceImpl`"。<span style="color: red; font-weight: bold;">`docs/` 资产越改造越厚，方法论才会真正长在项目里。</span>

## 4. 方法论提炼：让流程复用可执行

### 4.1 流程复用的四个标志

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/524120380f9ed058c01afd9b4ad8f3d1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把第二次改造和第一次改造放在一起对照，可以提炼出"流程是否进入复用态"的四个标志。<span style="color: red; font-weight: bold;">当四个标志同时出现，说明工作流已经从"每次从头搭"变成"每次套用模板"。</span>

| 标志 | 第一次改造 | 第二次改造 | 含义 |
|------|-----------|-----------|------|
| 现状确认从"必做"变成"快做" | 必做（AI 不熟项目，人要详细点一遍） | 快做（提示词为空，人点菜单确认即可） | AI 不介入的环节，确认本身不变重，变轻 |
| 提示词从"九条"变成"一条" | 9 条（拆需求 + 拆方案） | 1 条 | 资产齐全后，AI 能从一句话推导出完整方案 |
| 人工校验从"可省"变成"绝不省" | 可省（容易偷懒） | 绝不省（AI 出得越快越要校验） | <span style="color: red; font-weight: bold;">AI 压缩的是工作量，不是思考量</span> |
| 资产从"只读"变成"回灌" | 单向读取 `docs/` 和 `CLAUDE.md` | 读取 + 回灌新约束 | 每跑一次都让下一次更省事 |

### 4.2 项目阶段 Check List

下表是流程复用态下的可裁剪 Check List。在自己项目上对照勾选，攒下资产后逐步精简。

| 阶段 | 检查项 | 通过标准 |
|------|--------|---------|
| 现状确认 | 自己点一遍产品，确认要做的是新功能而不是重复功能 | 菜单逐项点过，无重复入口 |
| 拆需求 + 拆方案 | 一个提示词产出三份文档（需求 / 影响分析 / 技术方案） | 三份文档齐备，决策点拍板 |
| 人工校验 | 反馈问题清单 + 5 个决策最终拍板 | 所有"待审核"标识清零 |
| 后端改造 | 按 solution.md 小步分批（DTO / Service / Controller / Mapper） | 每批 `mvn test` 通过 |
| 前端改造 | 不引入新依赖（如 echarts），用现有组件库实现 | 前端构建无报错 |
| 文档回灌 | `api-list.md` / `data-model.md` / `CLAUDE.md` 同步更新 | 项目级约束写入 CLAUDE.md |

### 4.3 三句话压缩

<img src="imgs/aicmigr-23-mid-recap-02-workflow-reuse-practice/3ae43a10213b027f5f4a1e87fd3fd097_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 工作流的本质不变

变的是这次改造的具体技术细节（跨库查询、Redis 缓存、5 个 Section 组件、29 个改造点），不变的是"理解 + 开发"的工作流本质。这些新复杂度没有一个让工作流偏离原本的节奏——<span style="color: red; font-weight: bold;">这套流程不依赖于具体功能、只依赖于改造类型，而且</span>**越用越省事**。

#### (2) 资产飞轮第一次显形

前面攒下的资产在这一篇真正发挥了作用：`docs/` 让 AI 不用重新扫代码、`CLAUDE.md` 让 AI 自动遵守项目约束、第一次改造的模板让这次的提示词从繁到简。<span style="color: red; font-weight: bold;">这些不是花哨的功能，是踏踏实实的工程积累。</span>

#### (3) 跨技术栈迁移预告

系列第六部分开始，会换一个语言（Python / Hermes Agent）跑同样的工作流，验证一次跨技术栈的可迁移性。Java 项目能跑通的工作流，换到 Python 项目还能不能稳？等到第六部分跑完，心里会有答案。

### 4.4 两个自检问题

#### (1) 最有感的一点是什么

是"一个提示词出三份文档"的轻快，还是"必须人工校验三份文档"的提醒？

#### (2) 能否在自己的项目上复现

假设今天在自己的项目上做一个类似 Overview 的功能，能否像这一篇一样用一个提示词搞定拆需求 + 拆方案？如果不能，差在哪？是 `docs/` 资产不够全，还是 `CLAUDE.md` 没攒够，还是没做过类似的改造、没留下模板？
