---
title: 传统项目迁AI 18：项目开发 - 从新需求到改造方案
author: fangkun119
date: 2026-07-04 18:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-18-dev-02-new-requirement-to-plan/cover.jpg
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
aicmigr-18-dev-02-new-requirement-to-plan
传统项目迁AI 18：项目开发 - 从新需求到改造方案
-->

## 1. 为什么需求清楚还不能动手

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/59184c6c0a88b9a164df6ffdbdf92d9e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 1.1 老项目改造最常见的翻车点

拿到需求文档，你的第一反应是什么？扫一眼、心里有数、上手写代码？

这套流程放到老项目改造上往往不够用。以为只改一个 Controller，实际可能牵动整条链路——Service、DAO、配置、异常处理、测试；以为这次只动后端，实际前端要加按钮、加组件、加调用。这些坑不在动手前想清楚，就在动手中、动手后才发现，代价是返工。

<span style="color: red; font-weight: bold;">老项目不是新项目，"心里大概有数"是最容易踩坑的地方。</span>开发者水平越高、经验越足，越容易高估自己的脑补覆盖面，也就越容易在改造范围上估错。

### 1.2 "做什么" vs "怎么改"——方案文档的价值

<span style="color: red; font-weight: bold;">需求文档回答"做什么"，改造方案回答"怎么改"，二者之间存在一段必须跨越的距离。</span>

类比传统软件工程，需求文档对应需求分析阶段的产出，改造方案对应概要设计阶段的产出——就像接口契约之于实现，方案文档是动手编码前的"契约层"。一份合格的改造方案要回答：**动哪条链路、每个节点改什么、改动会牵动哪些既有功能、按什么顺序改、哪些决策需要人拍板**。

<span style="color: red; font-weight: bold;">需求清楚并不等于可以立即动手开发，跳过方案设计直接写代码，是老项目改造最常见的翻车场景之一。</span>这份方案文档，就是下文七步法要产出的核心成果。

## 2. 让 AI 当调研员——七步法全景

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/8d02ff83fb2ea4ef99760bebc0c77e42_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 七步法总览

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/ec1b1c6656407cbc4c0193aee469f9a5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-18-dev-02-new-requirement-to-plan/ec1b1c6656407cbc4c0193aee469f9a5_MD5.jpg
用途：七步法总览图，展示从需求到改造方案的完整流程
内容：Step 1 摸链路（含前端）→ Step 2 列改造点 → Step 3 画流程图 → Step 4 说影响 → Step 5 说改造步骤 → Step 6 整合给人审 → Step 7 人审核定稿，外加文档维护更新回灌 docs/。图分三段：Step1-6 AI 自动产出（浅蓝）、Step 7 人工审核（粉色）、方案文档产出（浅紫）。
-->

七步的骨架可以用一张表先记住：

| Step   | 步骤名           | 一句话目标                                                                                                                          |
| ------ | ------------- | ------------------------------------------------------------------------------------------------------------------------------ |
| Step 1 | 摸改造涉及的链路（含前端） | <span style="color: red; font-weight: bold;">从 HTTP 入口一路摸到 DB，把改动牵动的全部节点铺开</span>                                              |
| Step 2 | 列所有改造点        | 每个节点上"具体改什么"逐条列清，形成可估工的清单                                                                                                      |
| Step 3 | 画改造流程图        | 用一张<span style="color: red; font-weight: bold;">时序图</span>把"<span style="color: red; font-weight: bold;">改造后系统怎么跑</span>"可视化   |
| Step 4 | 说明影响范围        | 把改造对现有系统的<span style="color: red; font-weight: bold;">副作用</span>讲清楚，标出<span style="color: red; font-weight: bold;">风险等级</span> |
| Step 5 | 说明改造步骤与顺序     | 按依赖关系排成可执行<span style="color: red; font-weight: bold;">步骤</span>，抽出关键<span style="color: red; font-weight: bold;">决策点</span>                                                                                                           |
| Step 6 | 整合信息聚焦给人审     | 把零散产出整合成结构清晰的<span style="color: red; font-weight: bold;">方案文档</span>                                                                                                              |
| Step 7 | 人审核 + 调整 + 定稿 | 团队三层审核、拍板决策、反馈 AI 定稿                                                                                                           |

<span style="color: red; font-weight: bold;">Step 1-6 由 AI 自动产出（广度扫描）</span>，Step 7 由人主导（决策拍板），方案文档是整条链路的产出物，定稿后再沉淀回 docs/。

### 2.2 为什么让 AI 当调研员

<span style="color: red; font-weight: bold;">七步法的核心思想是把"怎么改"整体交给 AI 当调研员</span>：让 AI 通读代码、画出链路、逐节点列改造点、说影响、给步骤、最后整合成一份可审核的方案文档。

这个角色类比传统软件工程，就是项目里的需求分析师或初级工程师——做广度扫描，把开发者凭脑补容易漏掉的节点一次性铺开，并对"是否要改前端"这类隐性假设做显式验证。

落到方法论上，它对应 SDD（Spec-Driven Development，规约驱动开发）的思路 —— 先用一份规约驱动后续开发，类似传统瀑布流程里"设计文档驱动开发"的延伸，只是这份规约由 AI 起草、人审核定稿。

你可能会问：既然 AI 这么能干，为什么不直接让 AI 写代码？

因为AI看不到团队脑子里的隐性约束 —— 老接口的历史副作用、配置的特殊处理、产品的历史决策，这些信息只存在于团队脑子里，代码里查不到。AI 基于代码反推事实、叠加通用最佳实践给出建议，却无法知道"某个看起来该改的东西其实不能动"。

<span style="color: red; font-weight: bold;">因此七步法的另一面是"人主导审核拍板"——AI 把调研做全，人把方向定准。</span>

### 2.3 贯穿案例的引入

下面用一个真实需求 `prompt-version-diff`（Prompt 版本对比）来演示。

这是一个前后端联动、体量中等、决策点俱全的改造样本，既能覆盖链路扫描的纵切面（从 React 组件到 MyBatis 查询），又能逼出隐藏的设计分歧。后续章节会按七步法逐步推进，每一步的原理讲解紧跟同一个案例的实操片段。

## 3. 贯穿案例——prompt-version-diff

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/3d30f318bb3a16999baf680da12929c2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 3.1 案例需求：Prompt 版本 Diff

先说清楚这个案例在改什么。

把 `Prompt 版本 Diff` **类比**成两个传统场景：

1. Git Diff，两个 commit 之间的代码差异
2. API 文档的版本对比，同一接口新旧两版的字段差异。

这个需求要做的是同一件事，只不过对比对象是 Prompt 的两个版本：用户在历史列表里勾选两个版本，系统给出字段级的差异展示。

历史遗留的实现路径有点绕。前端 `version-history.jsx` 在用户勾选两个版本后，先后发起两次单版本查询请求拿到各自内容，再在 `VersionCompareModal.jsx` 里执行内存级的 diff 计算。

功能能跑通，但背着三个工程债：

- 网络往返次数翻倍 —— 一次对比要打两次接口；
- 对比逻辑散落在前端，与后端领域逻辑割裂；
- 后续若要做权限收敛或审计，没有一个统一入口能收口。

改造目标是把"计算职责"从前端下沉到后端：新增 `GET /api/prompt/version/diff` 接口，由后端 `PromptVersionService` 统一拉取两个版本内容、计算字段级差异，以结构化的 `PromptVersionDiffResult` 一次性返回。前端 `VersionCompareModal.jsx` 保留已有的行级 diff 渲染逻辑（`renderDiffLines`），仅把数据来源从"本地拼装"切换为"接口直取"。

这里有一个<span style="color: red; font-weight: bold;">刻意留下的约束</span>：<span style="color: red; font-weight: bold;">这次改造不动数据库 schema</span>。`prompt_version` 表保持原样，所有逻辑都在应用层完成。

这个约束不是顺手写的 —— 它直接决定了后续链路扫描里 Mapper 与 MyBatis XML 都会被标记为"复用不动"，也决定了改造点清单不会出现任何 DB 迁移相关的工作量。先记住这个约束，等看到 Step 4 的影响分析时会明白它的分量。

### 3.2 为什么选这个案例

你可能会问：一个 diff 接口，看起来不大，为什么要拿它贯穿全文演示七步法？

判断标准不是"看起来多复杂"，而是"能否覆盖方法论的关键决策点"。prompt-version-diff 在一个中等体量内同时命中了三个维度 —— 麻雀虽小、决策点俱全。

| 维度                | 说明                                                                                                                                                             | 在本案例里的具体体现                                                                                                                                                  |
| ----------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| <span style="color: red; font-weight: bold;">前后端联动的完整性</span>     | 真实改造几乎都是跨层联动，单边视角会导致"后端改完才发现前端要联调"的返工                                                                                                                          | 从 React 组件 `version-history.jsx`、`VersionCompareModal.jsx` 一路到 `PromptVersionService`、`PromptVersionMapper`、MyBatis 查询，完整覆盖一次纵切面                            |
| <span style="color: red; font-weight: bold;">"复用现有组件"的决策价值</span> | 最精彩的不是"新增了什么"，而是"<span style="color: red; font-weight: bold;">避免了新增什么</span>"——执行不到位时，<span style="color: red;">AI 容易建议"引入 react-diff-viewer"这类重复造轮子的方案</span> | `VersionCompareModal.jsx` 已实现 `renderDiffLines` 行级渲染，`PromptVersionMapper.selectByPromptKeyAndVersion` 已能按版本号精确取数；好的链路扫描会把这些可复用资产显式列出，把成本从"新建一套"压低到"换一个数据源" |
| <span style="color: red; font-weight: bold;">存在真实的工程决策分歧</span>   | 看似简单的 diff 接口，落地时会冒出<span style="color: red; font-weight: bold;">一堆需求文档里不会写的非平凡选择</span>                                                                                                                           | 字段为 null 时 `DiffItem.valueA`/`valueB` 取空字符串、null 还是 undefined？`changed` 判定按字节、按行还是按语义？前端 props 是否要从"两个独立 content"改为"一个 diffResult 对象"？这些分歧会在 Step 4 集中暴露    |

三个维度凑齐，prompt-version-diff 就成了一个适合入门的复现样本：体量不吓人，但七步法该踩的决策点一个不缺。

## 4. 七步法逐步深入（Step 1-6）

下面逐步展开 Step 1-6，每步原理在前、案例实操紧跟。Step 7 因性质不同（人主导而非 AI 跑），单独成章。

### 4.1 Step 1：摸出改造涉及的链路（含前端）

<span style="color: red; font-weight: bold; background-color: yellow;">笔记备注：这个流程过渡依赖让 AI 读代码，对于前端这类依靠视觉和浏览器交互的部分，容易遗漏，在第19篇有增强后的完整版</span>

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/4248f827957d2e15bb6b296ec32268db_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一步类比需求评审会上白板画调用链：传统做法是一个资深工程师边问边在白板上把请求从入口画到 DB，每经过一层都口头确认"这里要不要动"。AI 接过这支白板笔后，能在固定模板下做一次全链路扫描，把 HTTP 入口到 DB 查询之间的所有节点一次性铺开，并明确区分"现有可复用"与"需要新增/修改"两类。

它要解决的核心问题是消除视野盲区。开发者拿到需求后最常见的失误不是技术判断错误，而是根本没意识到<span style="color: red;">某个节点存在于链路中</span>——忘了拦截器、忘了前端某个被复用的展示组件、忘了 typing 声明文件。节点一旦漏列，后面的改造点清单就会跟着漏，最终在编码或联调阶段返工。<span style="color: red; font-weight: bold;">链路图是后续所有步骤的地基，链路上漏掉一个节点，改造点列表、影响范围、步骤排序就会整体偏差——因此这一步宁可多列、不可少列。</span>

提示词如下：

```
基于 docs/requirements/prompt-version-diff.md 的需求，扫一下代码：

- 找出这次改造涉及的完整链路（从 HTTP 入口到 DB 查询）

- 每个节点说明：文件、类、方法、关键逻辑（只看相关的）

- 标出"现有节点"和"需要新增/修改的节点"

- 不要漏前端节点（前端入口、调用、组件）

输出用表格 + 链路图（可以是 mermaid）。

保存到 docs/requirements/prompt-version-diff-impact.md。
```

AI 的产出片段如下（链路节点表格）：

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/872c430e5301bd3afcc9166721775118_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-18-dev-02-new-requirement-to-plan/872c430e5301bd3afcc9166721775118_MD5.jpg
用途：Step 1 让 AI 摸改造链路的产出片段示例（链路节点表格）
内容：AI 扫代码后输出的链路表格，列出前端入口 version-history.jsx（现有需修改）、前端对比弹窗 VersionCompareModal.jsx（现有需修改）、前端 API 函数 services/prompt/index.ts（需新增 getDiffVersion）、前端类型声明 typing.ts（需新增 diff types）、后端 Controller PromptController.java（需新增接口）、后端 Service PromptVersionService.java（需新增 diffVersions 方法）、后端 ServiceImpl（需新增实现，两次 Mapper 查询+内存比较）、后端 Mapper PromptVersionMapper.java（不动，复用 selectByPromptKeyAndVersion）、MyBatis XML（不动）、DB prompt_version 表（不动，纯查询无 schema 变更）。
-->

这份产出值得细看的关键，是 AI 把两个前端节点标成了"现有需修改"——`version-history.jsx` 已有勾选交互和 `showCompare` 状态，`VersionCompareModal.jsx` 已实现 `renderDiffLines` 的行级 diff 渲染。这正是提示词里"**不要漏前端节点**"这条显式约束起作用的结果。

<span style="color: red; font-weight: bold;">如果不加这条约束，AI 会默认沿用"前端是黑盒、只看后端"的隐性偏好，把前端折叠成一句"前端适配"，开发者就无从知晓已有组件可以复用。</span>链路扫描出"现有组件可用"，直接把改造复杂度从"新建 diff 组件 + 新建接口"压低到"换数据源 + 新建接口"——好的链路扫描会把"现有可复用资产"显式列出来，这是本案例最大的成本节省点。

另一方面，这份产出之所以清晰，是因为提示词本身的约束足够具体：要求"文件、类、方法、关键逻辑"四列固定，要求"标出现有/新增/修改"三态，要求"表格 + 链路图"双形态。<span style="color: red; font-weight: bold;">模糊的提示词（"帮我分析一下影响范围"）会换来模糊的散文式回答；结构化约束换来结构化产出。</span>这条经验贯穿整个七步法——把期望的输出形态写进提示词，比寄希望于 AI 自由发挥更可靠。

review 时的重点有两个。

#### (1) 前端节点的分类是否准确

逐条核对"<span style="color: red; font-weight: bold;">现有需修改</span>"和"<span style="color: red; font-weight: bold;">不存在需新建</span>"是否标错。

- AI 有时因为读到了某个文件就误判为"现有"，但那个文件可能只是名字相近、实际逻辑要重写；
- 反过来也可能把"实际不存在、需要新建"的文件误归到"现有"。

本案例中 `services/prompt/index.ts` 标为"需新增 getDiffVersion 函数"是正确的 —— 文件存在，但目标函数不存在，属于"在现有文件里新增"，与"现有需修改"语义不同。

#### (2) 链路完整性

从 HTTP 入口到 DB 出口之间，是否漏掉了<span style="color: red; font-weight: bold;">横切关注点</span>。AI 最容易跳过的是拦截器、AOP 切面、统一异常处理、参数校验 filter。

这些节点虽然不一定要改，但必须在链路图里出现，否则后续做影响范围分析时会出现"**看似只动了 Service，实则新接口绕过了某个全局校验**"的**隐性事故**。本案例由于是 GET 查询且无权限收敛需求，横切节点确实可以不动，但 review 时仍要确认它们被列出来了。

### 4.2 Step 2：列出所有改造点

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/1569cd8837bcea811907a2a3336c8a55_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 从链路图到改造点清单

Step 1 输出的链路图是面向理解的；Step 2 要把它转换成改造点清单，面向执行与排期。这一步类比 WBS 工作分解结构——每个改造点对应一个可指派的任务，能独立估时、独立验收、独立提交。

它要解决的问题是"把模糊的改造范围拆成**可估时、可分配、可验收**的最小单元"。<span style="color: red; font-weight: bold;">每个改造点都应当能独立回答"改哪个文件、改哪一段、改成什么样"，并且能在编码完成后单独提交、单独 review。</span>

你可能会问：为什么不直接写"前端适配 diff 接口"一条？因为这种笼统会让估时偏小、边界不清。<span style="color: red; font-weight: bold;">一条模糊的改造点到了排期阶段无法估时，到了编码阶段发现边界不清，最终演变成"边写边发现新工作"的低效循环。</span>提示词因此强制按"**编号/类型/涉及文件/改什么**"四列固定输出，禁止散文式描述。

提示词如下：

```
基于上一步的链路分析，把整个改造拆成具体的改造点列表。

每条改造点写：

- 编号（P01, P02,...）
- 类型：新增 / 修改 / 测试 / 文档
- 涉及文件（路径 + 大概行号）
- 改什么（一句话说清）

后端、前端、测试、文档都列出来，不要漏前端工作量。

输出用表格，追加到 prompt-version-diff-impact.md。
```

AI 的产出片段如下（改造点清单表格）：

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/dddd1962ef6c2bbe1648129c0e8725da_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-18-dev-02-new-requirement-to-plan/dddd1962ef6c2bbe1648129c0e8725da_MD5.jpg
用途：Step 2 列改造点的产出片段示例（改造点清单表格）
内容：AI 输出 12 条改造点 P01-P12。后端 P01-P06（新增 PromptVersionDiffResult/VersionMeta/DiffItem 三个 DTO、Service diffVersions 方法签名、Impl 实现、Controller GET diff 接口）；前端 P07-P10（新增 services 函数和 types、修改 VersionCompareModal.jsx 和 version-history.jsx）；测试&文档 P11-P12（api-list.md 和 data-model.md）。无新前端依赖、无 i18n 变更。
-->

两个 why 值得追问。

**第一个 why**：为什么 AI 没有给出"引入 react-diff-viewer"或"新建 diff 渲染组件"这种看似标准的改造点？

- 答案藏在 Step 1 的链路扫描里——正因为 Step 1 明确写出了"`VersionCompareModal.jsx` 已实现 `renderDiffLines`、属现有需修改"，AI 在 Step 2 就拥有了"已有可复用渲染层"的事实前提，自然不会去建议引入第三方库。
- 这就是七步法把"链路扫描"放在"改造点拆解"之前的根本原因——<span style="color: red; font-weight: bold;">前置的事实采集会约束后续的建议范围，防止 AI 在缺乏上下文时凭训练数据里的"标准做法"凑出多余工作量。</span><span style="color: red; font-weight: bold;">如果在你自己的项目里看到 AI 给出了重复造轮子的改造点，几乎都可以追溯到 Step 1 没把现有资产摸清。</span>

**第二个 why**：为什么这份清单没有单独的"测试"类型条目？这不是 AI 的疏漏，而是项目当前测试覆盖有限的现实反映。

- 在**测试基础设施尚未完善**的项目里，强行列出"为 diffVersions 写单元测试"这类条目，往往会在排期时被无声地推迟或跳过，反而让清单失真。
- 本案例选择把测试工作 **收敛到 Step 5（落地步骤）** 里作为执行动作，而不是单独编号为 P —— 清单的真实性优先于清单的"完整性"。
- 测试成熟度不同的项目要灵活判断：<span style="color: red; font-weight: bold;">测试覆盖良好的项目，应当为每个新增 Service 方法单列一条测试改造点</span>；<span style="color: red; font-weight: bold;">测试缺失的项目，则更适合在落地阶段集中补一个冒烟测试</span>。

#### (2) Review 重点

##### ① 前端改造点的齐全度

P07-P10 四条前端是否把"现有组件改造"和"新建文件"区分清楚了。

本案例中：

| 文件 | 操作类型 |
|---|---|
| `services/prompt/index.ts` | 在现有文件里新增函数 |
| `typing.ts` | 在现有文件里新增类型 |
| `VersionCompareModal.jsx` | 修改现有逻辑 |
| `version-history.jsx` | 修改现有逻辑 |

四种语义都被准确表达，没有把"新增函数"误标为"修改"。review 时要特别留意 AI 是否会<span style="color: red; font-weight: bold;">把"现有文件里加一个函数"笼统标成"修改"</span>，这种笼统会让<span style="color: red; font-weight: bold;">估时偏小</span>。

##### ② 测试是否被合理处理

要么单列为 P，要么在落地步骤里显式提及，<span style="color: red; font-weight: bold;">不能两头都缺</span>。

本案例属于后者，开发者在 Step 5 阶段需要确认测试动作真的被排进了执行流，而不是被清单的"省略"默默吞掉。

### 4.3 Step 3：画改造流程图

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/985abc12d23593ecedad99834158dd15_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 反向校验改造点清单

前两步产出的是"文字 + 表格"，适合逐条阅读但不适合一眼把握全局。Step 3 类比接口设计文档里的时序图或 PlantUML —— 用一张图把"改造后系统怎么跑"可视化，<span style="color: red; font-weight: bold;">让所有参与者（后端、前端、review 人）在同一张图上对齐对调用链与数据流的理解</span>。

流程图的价值在于它能**暴露文字描述里容易被忽略的细节**：

- <span style="color: red; font-weight: bold;">调用顺序是否正确</span>
- <span style="color: red; font-weight: bold;">数据形态在哪里发生变化</span>
- <span style="color: red; font-weight: bold;">哪些节点是复用、哪些是新增。</span>

一张图画完，如果三个角色对"两次 Mapper 调用发生在哪里"、"DiffItem 在哪里组装"达成一致，后续编码就不会出现"我以为你那边算好了"的接口错位。

提示词如下：

```
基于已经列出的改造点，画一张改造流程图（用 mermaid）。

图里要展示：

- 用户从前端发起对比请求的完整调用链
- 后端处理流程（Controller → Service → DAO → 返回）
- 数据流（入参怎么变成 DiffItem 返回）
- 如果有表结构变更，单独画 schema 变更图（本需求不改表，明确说"不改表"）

保存到 docs/requirements/prompt-version-diff-flow.svg（或 mermaid 代码块）。
```

AI 的产出片段如下（一张完整的 sequence diagram，安装的画图 Skill 在这里特别好用）：
<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/dd1840f0deeaa36361a8150c6cec9db0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-18-dev-02-new-requirement-to-plan/dd1840f0deeaa36361a8150c6cec9db0_MD5.jpg
用途：Step 3 画改造流程图的产出示例（mermaid sequence diagram）
内容：完整调用链时序图。用户→version-history.jsx（勾选两版本点对比）→services/prompt/index.ts（getDiffVersion）→PromptController（GET /api/prompt/version/diff）→PromptVersionServiceImpl（diffVersions）→DB（selectByPromptKeyAndVersion 调用两次取两个版本）→内存比较三字段组装 DiffItem→返回 PromptVersionDiffResult→前端 VersionCompareModal 渲染行级 diff。图明确：无"新建 diff 组件"节点（复用现有），不改表。
-->

三个 why 值得追问。

**① 第一个 why：为什么图里没有"新建 diff 组件"节点？**

因为 `VersionCompareModal` 是被复用的，它在前端拿到 `PromptVersionDiffResult` 后直接走已有的 `renderDiffLines` 路径渲染。

这一点的作用在于——流程图天然会暴露"多余的节点"。如果 AI 在 Step 2 凑出了不必要的改造点，到了 Step 3 画图时就会发现"这个节点画不进调用链"或"画进去了但和现有节点功能重叠"。

换言之，<span style="color: red; font-weight: bold;">流程图是 Step 2 改造点清单的"反向校验"：能顺畅画进图的改造点是合理的，画不进去或导致图变得别扭的，往往是过度设计。</span>

<span style="color: red; font-weight: bold;">"画图顺畅度"可以作为 Step 2 成果的试金石</span>。

**② 第二个 why：为什么 `selectByPromptKeyAndVersion` 在图里被明确画出调用了两次？**

这不是装饰性细节，而是 Step 4 影响范围分析的关键依据。

两次单版本查询意味着：后端 diff 接口本身没有事务性约束（两次查询之间数据若被改写，理论上会出现"版本 A 是旧值、版本 B 是新值"的不一致快照）。

- 在低频改写的 Prompt 配置场景下这种不一致可以接受
- 在高频写入的场景下就需要重新设计（例如合并为一次 SQL、加版本快照表、或加读写锁）。

<span style="color: red; font-weight: bold;">流程图把"调用两次"显式画出来，等于把这个潜在风险点前置到了设计阶段</span>，而不是留到 Code Review 时才被某个资深工程师发现。这就是"画清楚"比"写清楚"更强的地方——图不会撒谎，也不会省略。

**③ 第三个 why：画图 Skill 的价值在哪里**

本步骤明确提到"用 mermaid"，并且产出可以保存为 `.svg` 或代码块。<span style="color: red; font-weight: bold;">安装专门的画图 Skill 后</span>，AI 不再需要把 mermaid 语法记在脑子里，而是能把注<span style="color: red; font-weight: bold;">意力集中在"调用链对不对、数据流清不清楚"这种语义层面的问题上</span>。

手工画图的成本越高，开发者越倾向于跳过这一步直接编码；Skill 把画图成本压到极低，开发者就更愿意在编码前画一张图对齐认知 —— 工具的可达性直接决定了方法论是否会被执行。

#### (2) review 重点

##### ① 调用链是否完整

图里必须画出"前端拿到响应、渲染 diff 组件"这一段，而不能停在"后端返回就结束"。

本案例的时序图画到了 `VersionCompareModal` 渲染行级 diff，是完整的；如果只画到 Controller 返回 JSON，就等于把前端当成"黑盒接收者"，后续做联调时容易出问题。

review 时要确认图的终点是"用户看到对比结果"，而不是"接口返回 200"。

##### ② 数据流是否清晰

两个版本的内容是：

- 如何变成 `DiffItem` 的 `valueA`/`valueB`/`changed` 字段的？
- 比较规则是按字段、按行还是按字符？
- `changed` 字段的判定逻辑画进去了吗？

这些细节决定了 AI 是否真的理解了 diff 的语义。本案例图里画出了"<span style="color: red; font-weight: bold;">内存比较三字段</span>组装 DiffItem"，语义层面是清楚的；<span style="color: red; font-weight: bold;">如果图里只写"做 diff 然后返回"，就说明 AI 对 diff 算法的理解还停留在黑盒</span>，Step 4 就需要重点追问比较规则与边界条件（null、空串、超长内容）的处理。

### 4.4 Step 4：说明影响范围

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/edf4934f086d30efa39176a409c4ea27_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

改造点列清了、流程图画完了，下一步要回答："这次改造会<span style="color: red; font-weight: bold;">动到现有系统的哪些地方，哪里最容易出事</span>？" <span style="color: darkgray;">这一节的角色类似 Code Review 之前的影响评估 —— 动手前先把副作用摆出来，并给每项标个风险等级，让团队在写第一行代码前就知道压力点在哪。</span>

但这一步真正的价值不在"列影响项"，而在<span style="color: red; font-weight: bold;">让 AI 帮你验证脑子里那些基于经验的假设</span>。承接 Step 3 的一个关键观察 —— 流程图里 `PromptVersionServiceImpl` 被调了两次 Mapper，这个细节正是 Step 4 影响分析的依据。"图不会撒谎，也不会省略"，所以 Step 4 才能精准评估这个双次调用对性能、并发、副作用的真实影响。

提示词如下：

```text
基于改造点和流程图，说明这次改造的影响范围：

1. 现有接口受不受影响（列出可能受影响的接口和判断依据）
2. 现有调用链路受不受影响（改动会不会影响其他用 PromptVersionService 的地方）
3. 测试影响（现有测试需不需要改）
4. 文档影响（api-list.md、data-model.md、CLAUDE.md 哪些要更新）
5. 前端兼容性（引入新依赖会不会和现有依赖冲突）
6. 性能影响（新接口在 LONGTEXT 字段大时的网络传输和序列化）

每条标"高/中/低"风险等级。输出用表格。
```

AI 的产出片段如下（影响范围与风险表格）：

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/1df9237c8fda6ef032e5709e8dc51712_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-18-dev-02-new-requirement-to-plan/1df9237c8fda6ef032e5709e8dc51712_MD5.jpg
用途：Step 4 说明影响范围的产出片段示例（影响范围与风险等级表格）
内容：AI 输出的影响范围表格，含 6 项影响项：现有 GET /api/prompt/version 接口（低风险，新接口新路径原接口不动）、PromptVersionServiceImpl 现有方法（低风险，只有 log.info 无 metrics 副作用，直接调 Mapper 无需抽取内部方法）、VersionCompareModal.jsx 改造（中风险，props 结构变更是破坏性改动需同步更新调用方，renderDiffLines 保留不动）、现有测试（低风险，当前无 PromptVersion 测试）、前端依赖（低风险，无需新增依赖复用 renderDiffLines）、LONGTEXT 大内容性能（中风险，两版本 template 同时返回，本期不做大小限制上线后监控 latency）。
-->

重点看第 2 条：AI 扫到 `PromptVersionServiceImpl.getByPromptKeyAndVersion` 只有 `log.info`，没有 metrics 副作用。这和开发者脑子里"复用 service 方法会双倍打点"的假设完全相反。AI 当调研员、帮开发者验证假设的价值就在这里——<span style="color: red; font-weight: bold;">你以为的坑，AI 扫一遍告诉你"这个坑不存在"</span>，于是这次不需要抽取 `getVersionInternal`。

为什么这一步要用 AI 而不是开发者自己想？因为<span style="color: red; font-weight: bold;">开发者凭经验做的影响判断，往往是基于"通用最佳实践"而非"这个项目的真实代码"</span>。比如"复用 service 方法会不会双倍打点"这个假设，在通用层面是对的（很多项目 service 层确实有 metrics），但在这个项目的真实代码里是错的（这个 service 只有 `log`）。只有 AI 实际去读 `PromptVersionServiceImpl` 的实现，才能给出准确判断。<span style="color: red; font-weight: bold;">Step 4 的方法论意义就在于此——把"基于经验的假设"换成"基于代码的事实"。</span>这条方法论可以浓缩成一份检查清单：每项影响都问三遍——会不会受影响、受影响的表现是什么、严重程度如何；涉数据和外网接口的项默认提一档风险；共用的枚举、工具类、中间表这类"看似无关实则耦合"的模块最容易漏，要单独盯。

#### (2) review 重点

##### ① 风险等级是否合理

<span style="color: red; font-weight: bold;">AI 容易把所有项标"低"或"中"，倾向低估老系统的隐性耦合</span>。如果某条实际是"高"（比如某个改造确实有破坏性），要追问让 AI 重新评估。本案例 `VersionCompareModal.jsx` 改造被标为"中"风险是合理的——props 结构变更是破坏性改动，需要同步更新调用方。<span style="color: red; font-weight: bold;">另外要让 AI 对每项给出"判断依据"</span>：它看了哪段代码、哪个配置得出这个结论，方便审核者快速复核。

##### ② 有没有漏掉的影响项

AI 列的 6 项是常见的，但<span style="color: red; font-weight: bold;">具体项目可能有特殊影响</span> —— <span style="color: red; font-weight: bold;">分布式事务</span>、<span style="color: red; font-weight: bold;">多租户隔离</span>、<span style="color: red; font-weight: bold;">灰度发布机制</span>、<span style="color: red; font-weight: bold;">安全配置</span> —— 这些要主动让 AI 检查。本案例 Step 7 审核时就发现漏了一条：Spring Security 配置中需要把新接口加白名单。外网接口和数据迁移必须被重点提示。

### 4.5 Step 5：说明改造步骤与顺序

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/6ca9b505b4036abc1ed63aec4af3d0dd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 改造步骤排序：依赖先行，没分歧别硬凑方案

知道改什么、影响范围也清楚了，下一步要把改造点<span style="color: red; font-weight: bold;">按依赖关系</span>排成可执行的步骤 —— 这一节的角色<span style="color: red; font-weight: bold;">类似项目排期表</span>或甘特图，重点不是"每件事多少工时"，而是"哪些事必须串行、<span style="color: red; font-weight: bold;">哪些事可以并行</span>、关键决策卡在哪一环"。

提示词如下：

```
基于改造点和影响范围，给出 AI 准备的改造步骤和顺序。

要求：

- 按依赖关系排序（后端任务在前、前端任务跟上、测试穿插）
- 每步说明：做什么、依赖哪些前置步骤、预估工作量
- 关键决策点显式列出（比如"null 字段怎么处理"）
- 如果某个改造点有多个实现方式且优劣明显，给 2 个方案 + 推荐
- 如果改造点没有方案分歧，直接给一个方案就行（不要为了"显得调研全"硬凑方案）

输出用表格，追加到 prompt-version-diff-impact.md。
```

注意提示词最后两行的克制：<span style="color: red; font-weight: bold;">有分歧给方案、没分歧直接给</span>。这避免了 AI 为每个改造点都凑 2-3 个方案的"过度调研"。老项目改造里大多数改造点没什么方案分歧（比如加一个 DTO），不需要硬列对比。这条约束的 why 在于——<span style="text-decoration: underline;">AI 有"显得调研全面"的倾向，会为每个点都列对比方案，反而稀释真正需要决策的项</span>。<span style="color: red; font-weight: bold;">明确要求"没分歧就一句话带过"，能让关键决策点（如本案例的 props 结构变更）从噪音中凸显出来。</span>方法论上呼应 Step 5 的排序原则：按 DB → DAO → Service → Controller → 前端的依赖做拓扑排序，数据/契约先行；前端等的是"接口契约"不是"后端全部完成"。

AI 的产出片段如下（改造步骤与顺序表格）：

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/ff949f8b38aece13fc76bc17b7511000_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-18-dev-02-new-requirement-to-plan/ff949f8b38aece13fc76bc17b7511000_MD5.jpg
用途：Step 5 说明改造步骤的产出片段示例（改造步骤与顺序表格）
内容：AI 输出的改造步骤表格，7 步合计约 7 小时。步骤1 建 DTO（P01-P03，1h，createTime 用 epoch ms）；步骤2 Service（P04-P05，1.5h，null 视同空字符串用 nullToEmpty）；步骤3 Controller（P06，0.5h，异常复用 StudioException）；步骤4 前端 API（P07-P08，0.5h，可与步骤3并行）；步骤5 VersionCompareModal 改造（P09，2h，关键决策：props 从 version1/version2 改为 PromptVersionDiffResult）；步骤6 version-history 接入（P10，1h，加 loading）；步骤7 文档（P11-P12，0.5h）。
-->

合计约 7 小时。注意步骤 5 列了关键决策：`VersionCompareModal` 的 props 结构变更——这正是真正有破坏性、需要团队拍板的点（对应 Step 4 第 3 条标的"中"风险）。其他步骤没有分歧就一句话带过，没硬凑方案。

#### (2) review 重点

##### ① 关键决策点对不对

<span style="color: red; font-weight: bold;">AI 列出的决策项</span>是不是<span style="color: red; font-weight: bold;">真的需要决策</span>。如果 AI 把"用 try-catch 还是 if-null"这种小事也列成决策点，要让它精简。本案例 AI 把决策收敛到"props 结构变更"一项是合理的。技巧是要求 AI 对每个决策点<span style="color: red; font-weight: bold;">标注"分歧程度"（已共识/有分歧）</span>，只对"有分歧"的项展开两方案对比。

##### ② 前端步骤的前置依赖对不对

步骤 4 写"可与步骤 3 并行（有接口 mock 就能开始）"，这个细节很关键。前端不需要等后端跑通再开始，<span style="color: red; font-weight: bold;">只要接口契约定型 + mock 数据，前端就能并行</span>。AI 容易把前端依赖写成"后端必须全部完成"，让前端工作开始得太晚。<span style="color: red; font-weight: bold;">识别出前后端可并行，往往比压单点工作量更能缩短交付时间。</span>

### 4.6 Step 6：整合信息聚焦给人审核

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/c36beec16df848a471a55573d5b17006_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 散料整合成上会材料——过滤伪决策，聚焦真拍板

前 5 步产出了一堆零散内容：<span style="color: red; font-weight: bold;">链路图</span>、<span style="color: red; font-weight: bold;">改造点表</span>、<span style="color: red; font-weight: bold;">流程图</span>、<span style="color: red; font-weight: bold;">影响范围</span>、<span style="color: red; font-weight: bold;">改造步骤</span>。最后这一步像把项目资料<span style="color: red; font-weight: bold;">整理成一份上会材料</span>或 RFC 文档。

给决策者看的不是过程流水账，而是<span style="color: red; font-weight: bold;">结论与依据</span>。核心问题就两个字：<span style="color: red; font-weight: bold;">聚焦</span>。一份好的方案文档不是把所有信息都堆上去，而是让审核者用最短时间抓住"<span style="color: red; font-weight: bold;">要拍什么板、为什么这么拍</span>"。

提示词如下：

```
把前面五步的产出整合成一份完整的改造方案文档，结构如下：

1. 一句话概要
2. 涉及链路（链路图 + 节点表格）
3. 改造点清单（后端 / 前端 / 测试 / 文档分类列出）
4. 改造流程图
5. 影响范围与风险（表格）
6. 改造步骤与顺序（表格 + 关键决策标注）
7. 待审核的关键决策点（单独提取出来，方便人 review）

第 7 节是关键：把前面散落在各步骤的"需要人决策"的点集中列出来，

让我能在一个地方一次性审核所有决策。

保存到 docs/requirements/prompt-version-diff-solution.md。
```

注意第 7 节"待审核的关键决策点"，这是这一步（第6步）提示词的灵魂。

AI 默认会平铺直叙地复述之前5步内容，方法论上有两点：

1. 要求 AI 区分"事实（来自代码）"和"建议（来自最佳实践）"，两者在文档中用不同标记呈现
2. 给 AI 一份方案文档骨架作为模板，要求严格按骨架填充，避免散文。

AI 的产出片段如下（第 7 节"待审核的关键决策点"示例）：

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/d37aa07659fb759d57a45882a71f616b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/aicmigr-18-dev-02-new-requirement-to-plan/d37aa07659fb759d57a45882a71f616b_MD5.jpg
用途：Step 6 整合信息的产出片段示例（第 7 节"待审核的关键决策点"表格）
内容：一张表格截图，列出整合方案中待人工拍板的 4 个关键决策点（D1-D4）。D1 null 字段处理方式（推荐 null 视同空字符串用 nullToEmpty）；D2 VersionCompareModal props 结构变更方式（推荐直接改 props 同步更新调用方）；D3 前端是否加 loading 状态（推荐加）；D4 是否监控 diff 接口 latency（建议加）。每条含推荐方案与备选。
-->

看完这一节就知道 4 个决策要拍板，不用翻整篇文档找决策点。

图中这次的 D1-D4 和开发者预想的可能不一样：

- 没有"是否抽取 `getVersionInternal`"（Step 4 已经确认不需要）
- 没有"split 还是 unified 模式"（现有组件已经解决了这个问题）。

AI 扫出真实代码后，方案里的决策点就是真实的决策点，而不是基于假设的假决策点。

这也是七步法的隐性价值——<span style="color: red; font-weight: bold;">通过前置的代码扫描，把"伪决策"过滤掉，让审核精力集中在真正需要人判断的点上。</span>

#### (2) review 重点

##### ① 决策点齐全

<span style="color: red; font-weight: bold;">前 5 步里所有</span> AI 标注"需要决策"或"建议"或给了多方案的，<span style="color: red; font-weight: bold;">都要在第 7 节出现</span>。逐一对照前五步核对，漏一条都可能让审核会现场卡壳。

##### (2) 结构清晰

整合后的文档应该一眼能看出"<span style="color: red; font-weight: bold;">链路</span> → <span style="color: red; font-weight: bold;">改造点</span> → <span style="color: red; font-weight: bold;">流程</span> → <span style="color: red; font-weight: bold;">影响</span> → <span style="color: red; font-weight: bold;">步骤</span> → <span style="color: red; font-weight: bold;">决策</span>"的逻辑递进。审核者能不能在 5 分钟内抓住全部决策点是底线指标。如果读起来像散文，让 AI 重整一版；同时警惕 AI 把自己的猜测当成既定事实写进文档。

AI 整合后的产出是一份完整的定稿方案文档，结构如下：

```text
# Prompt 版本 Diff — 改造方案（定稿）

> 状态：方案定稿，待实施
> 来源：prompt-version-diff-impact.md 整合
> 关联需求：docs/requirements/prompt-version-diff.md

---

## 1. 一句话概要

在 `version-history.jsx` 的现有"勾选对比"交互基础上，新增后端 `GET /api/prompt/version/diff` 接口，将当前"两次单版本请求 + 前端拼装"的对比方式改为"后端统一返回 diff 结果"，前端 `VersionCompareModal.jsx` 保留现有行级 diff 渲染逻辑，仅改造数据来源。

---

## 2. 涉及链路

### 节点表

| 节点 | 文件 | 状态 | 说明 |
| --- | --- | --- | --- |
| 前端入口 | `frontend/.../pages/prompts/version-history/version-history.jsx` | 现有，需修改 | 已有勾选和 showCompare 状态，接入 getDiffVersion + loading |
| 前端对比弹窗 | `frontend/.../components/VersionCompareModal.jsx` | 现有，需修改 | 已有行级 diff 渲染，改造 props 结构和数据来源 |
| 前端 API 函数 | `frontend/.../services/prompt/index.ts` | 需新增 | 新增 `getDiffVersion` 函数 |
| 前端类型声明 | `frontend/.../services/prompt/typing.ts` | 需新增 | 新增 diff 相关 types |
| 后端 Controller | `...admin/controller/PromptController.java` | 需新增接口 | 新增 `GET /api/prompt/version/diff` |
| 后端 Service 接口 | `...admin/service/PromptVersionService.java` | 需新增方法 | 新增 `diffVersions` 签名 |
| 后端 Service 实现 | `...admin/service/impl/PromptVersionServiceImpl.java` | 需新增实现 | 两次 Mapper 查询 + 内存比较 |
| 后端 Mapper | `...admin/mapper/PromptVersionMapper.java` | **不动** | 复用 `selectByPromptKeyAndVersion` |
| MyBatis XML | `PromptVersionMapper.xml` | **不动** | 复用现有 SQL |
| DB | `prompt_version` 表 | **不动** | 纯查询，无 schema 变更 |

### 调用链路图

\```mermaid
sequenceDiagram
participant U as 用户
participant VH as version-history.jsx
participant API as services/prompt/index.ts
participant BE as PromptController
participant SVC as PromptVersionServiceImpl
participant DB as prompt_version 表
U->>VH: 勾选两个版本，点击"对比"
VH->>VH: 显示 loading
VH->>API: getDiffVersion({ promptKey, versionA, versionB })
API->>BE: GET /api/prompt/version/diff
BE->>SVC: diffVersions(promptKey, versionA, versionB)
SVC->>DB: selectByPromptKeyAndVersion × 2
DB-->>SVC: PromptVersionDO A + B
SVC->>SVC: 内存比较三字段，组装 DiffItem
SVC-->>BE: PromptVersionDiffResult
BE-->>API: Result<PromptVersionDiffResult>
API-->>VH: diff 数据
VH->>U: VersionCompareModal 渲染行级 diff
\```

## 3. 改造点清单

### 后端

| 编号 | 类型 | 文件 | 改什么 |
| --- | --- | --- | --- |
| P01 | 新增 | `dto/PromptVersionDiffResult.java` | 顶层 DTO |
| P02 | 新增 | `dto/VersionMeta.java` | 版本元信息 DTO |
| P03 | 新增 | `dto/DiffItem.java` | Diff 单元 DTO |
| P04 | 新增 | `PromptVersionService.java` | 新增 `diffVersions` 方法签名 |
| P05 | 新增 | `PromptVersionServiceImpl.java` | 实现 `diffVersions` |
| P06 | 新增 | `PromptController.java` | 新增 GET diff 接口 |

### 前端

| 编号 | 类型 | 文件 | 改什么 |
| --- | --- | --- | --- |
| P07 | 新增 | `services/prompt/index.ts` | 新增 `getDiffVersion` 函数 |
| P08 | 新增 | `services/prompt/typing.ts` | 新增 diff 相关 types |
| P09 | 修改 | `components/VersionCompareModal.jsx` | 改 props 结构，数据来源从前端拼装改为后端 diff 结果 |
| P10 | 修改 | `pages/prompts/version-history/version-history.jsx` | 接入 `getDiffVersion`，加 loading 状态 |

### 测试 & 文档

| 编号 | 类型 | 文件 | 改什么 |
| --- | --- | --- | --- |
| P11 | 文档 | `docs/api-list.md` | 新增接口记录，标"开发中" |
| P12 | 文档 | `docs/data-model.md` | 新增三个 DTO 说明 |

---

## 4. 改造流程图

\```mermaid
flowchart LR
subgraph 后端新增
D01[P01-P03\n建三个 DTO] --> D02[P04-P05\nService diffVersions]
D02 --> D03[P06\nController GET diff 接口]
end
subgraph 前端改造
F01[P07-P08\nAPI 函数 + 类型] --> F02[P09\nVersionCompareModal\n改 props 结构]
F02 --> F03[P10\nversion-history\n接入 + loading]
end
D03 -->|接口定型后| F01
D01 -->|可并行| F01
\```

---

## 5. 影响范围与风险

| # | 影响项 | 风险 | 说明 |
| --- | --- | --- | --- |
| 1 | 现有 `GET /api/prompt/version` 接口 | **低** | 新接口新路径，原接口不动 |
| 2 | `PromptVersionServiceImpl` 现有方法 | **低** | 只有 `log.info` 日志，无 metrics 副作用，直接调 Mapper 无需抽取内部方法 |
| 3 | `VersionCompareModal.jsx` 改造 | **中** | props 结构变更是破坏性改动，需同步更新 `version-history.jsx` 的调用处；行级 diff 逻辑（`renderDiffLines`）保留不动 |
| 4 | 现有测试 | **低** | 当前无 PromptVersion 测试，新增不影响现有 |
| 5 | 前端依赖 | **低** | 无需新增依赖，复用现有 `renderDiffLines` |
| 6 | LONGTEXT 大内容性能 | **中** | 两个版本 template 同时返回，本期不做大小限制，上线后监控接口 latency |

---

## 6. 改造步骤与顺序

| 步骤 | 改造点 | 依赖 | 工作量 | 关键决策 |
| --- | --- | --- | --- | --- |
| 1 | P01 + P02 + P03（建 DTO） | / | 1h | `createTime` 用 epoch ms，与现有 `PromptVersionDetail` 一致 |
| 2 | P04 + P05（Service） | 步骤 1 | 1.5h | null 视同空字符串，用 `Objects.equals(nullToEmpty(a), nullToEmpty(b))` |
| 3 | P06（Controller） | 步骤 2 | 0.5h | 异常复用 `StudioException` 体系 |
| 4 | P07 + P08（前端 API） | 步骤 3 接口定型 | 0.5h | 可与步骤 3 并行 |
| 5 | P09（VersionCompareModal 改造） | 步骤 4 | 2h | **关键**：props 从 `version1/version2` 对象改为 `PromptVersionDiffResult`；行级渲染继续在前端 |
| 6 | P10（version-history 接入） | 步骤 5 | 1h | 在现有 `showCompare` 基础上加 loading 状态 |
| 7 | P11 + P12（文档） | 步骤 3 接口定型 | 0.5h | — |

**合计约 7 小时。**

---

## 7. 待审核的关键决策点

| # | 决策点 | 推荐方案 | 备选 |
| --- | --- | --- | --- |
| D1 | null 字段处理方式 | null 视同空字符串（`nullToEmpty`），`changed` 基于空字符串比较 | null 单独标记为"字段缺失"状态 |
| D2 | `VersionCompareModal` props 结构变更方式 | 直接改 props，同步更新调用方 `version-history.jsx` | 做兼容层同时支持旧 props 和新 props，旧逻辑渐进迁移 |
| D3 | 前端是否加 loading 状态 | 加（diff 接口在 LONGTEXT 时可能较慢，没有 loading 体验差） | 不加（依赖现有弹窗打开的默认状态） |
| D4 | 是否监控 diff 接口 latency | 建议加（为后续是否要做大小限制提供数据依据） | 本期不做 |
```

## 5. Step 7——人主导的审核与定稿

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/7032f9a434ae5bec48ea062af9651152_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

你可能会问：前 6 步 AI 都跑完了，链路、改造点、流程图、影响范围、决策点都有了，为什么还要单独留一步给人？

因为前 6 步 AI 给的是"基于代码反推 + 通用最佳实践"的输出。它能从代码里看到结构，但<span style="color: red; font-weight: bold;">看不到结构背后那些**只在团队脑子里的东西**</span>——命名约定、分层规范、某个老接口被未知下游依赖的副作用、某个配置在不同环境下的灰度策略、产品历史上"看起来该改但偏偏不能动"的决策延续。

所以 Step 7 在性质上跟前 6 步不一样：前 6 步 AI 是主跑，<span style="color: red; font-weight: bold;">Step 7 是人主导</span>。可以把 Step 7 类比成传统研发流程里的<span style="color: red; font-weight: bold;">需求评审会决策环节</span> / <span style="color: red; font-weight: bold;">架构评审委员会</span>——AI 是把材料准备齐全、把选项摆出来的分析师，团队是拿着材料拍板的决策者，Step 6 产出的方案文档就是上会材料。审核要走三个层次。

### 5.1 三层审核（步骤7）分别审什么

#### (1) 第一层：决策点拍板

打开 Step 6 整合好的方案文档，先看第 7 节 "待审核的关键决策点"。这一层只做一件事：把<span style="color: red; font-weight: bold;">每条决策点上 AI 给的"推荐方案 / 备选"逐条过一遍</span>，给出最终选择和理由。输入是文档里现成的 D1-D4，输出是每条都不再带"待审核"字样。

以本章案例为例，四个决策点要这样拍板：

| 决策点 | 最终选择 | 理由 |
| --- | --- | --- |
| D1（null 字段处理） | null 视同空字符串 | 与历史数据兼容，不想在 UI 上区分"字段缺失"和"字段为空" |
| D2（props 变更方式） | 直接改 props，同步更新调用方 `version-history.jsx` | 兼容层增加长期维护负担，改造点不多直接改干净 |
| D3（前端 loading） | 加 | diff 接口在 LONGTEXT 时可能慢，没 loading 体验差 |
| D4（监控 latency） | 加 | 观测一段时间再决定要不要加大小限制 |

决策点拍板的硬性要求是全部拍掉，<span style="color: red; font-weight: bold;">不能遗留"待定"进入开发</span>。任何一条留"待定"的决策点，进了开发都会变成"代码写到一半发现方向没定"。

#### (2) 第二层：链路、改造点、影响范围审核

第一层只盯决策点，第二层要<span style="color: red; font-weight: bold;">逐节</span>看方案文档的主体内容，重点是补 <span style="color: red; font-weight: bold;">AI 看不到的耦合</span>、<span style="color: red; font-weight: bold;">副作用</span>、<span style="color: red; font-weight: bold;">隐性约束</span>。三类容易漏的东西要逐一核对：

- <span style="color: red; font-weight: bold;">链路有没有漏</span> —— 特别是前端节点。AI 画链路时容易把后端链路画得很全，但前端从组件到 API 函数的环节可能漏。
- <span style="color: red; font-weight: bold;">改造点有没有漏</span>——特别是文档、i18n 这类非代码产出。这类东西散落在各处，AI 不一定都扫到。
- <span style="color: red; font-weight: bold;">影响范围有没有漏</span>——特别是分布式事务、多租户、灰度这种<span style="color: red; font-weight: bold;">项目特定</span>的影响。这类影响不是通用最佳实践能覆盖的，必须结合团队对老系统的了解才能判断。

这一层是把前面强调的"AI 看不到的隐性约束" —— <span style="color: red; font-weight: bold;">团队隐性约定</span>（<span style="color: red; font-weight: bold;">命名</span> / <span style="color: red; font-weight: bold;">分层</span> / <span style="color: red; font-weight: bold;">错误码规范</span>）<span style="color: red; font-weight: bold;">、老接口的历史副作用</span><span style="color: red; font-weight: bold;">、配置的特殊处理</span><span style="color: red; font-weight: bold;">、产品的历史决策</span> —— 具体落到当前方案上的一次过滤。

#### (3) 第三层：把发现的问题反馈给 AI 调整

先看看上一层审核，开发者会发现了哪些问题（以本章案例为例）：

- **P10 漏了一个细节**：用户选中两条版本后要禁用其他版本的勾选，<span style="text-decoration: underline;">防止选超过 2 个</span>。
- **影响范围漏了**：<span style="text-decoration: underline;">Spring Security 配置</span>需要把新接口加白名单。
- **工作量估计太乐观**：`VersionCompareModal` 改造涉及<span style="text-decoration: underline;">状态联动</span>，实际可能要 3 小时而不是估的 2 小时。

把所有发现汇总反馈给 AI，让它调整方案文档。反馈提示词长这样：

```
我审核了方案文档，以下几点需要调整：

1. P10 补充细节：用户在版本列表选中两条后，要禁用其他版本的勾选
2. 影响范围漏了：Spring Security 配置中需要把 GET diff 接口加白名单（新增 P13 改造点）
3. 第 7 节决策全部拍板：D1 null 视同空字符串，D2 直接改 props，D3 加 loading，D4 加监控

更新 prompt-version-diff-solution.md，把这些反馈整合进去。

特别注意第 7 节的决策点要全部更新成最终决策（不再是"待审核"）。
```

AI 调整完，开发者再 review 一遍。多数情况下两轮就够：

- 第一轮补**主体遗漏**
- 第二轮**收尾对齐**

如果**反馈触发了较大的链路或步骤重排，可能需要回到 Step 1-6 局部重跑**，这种情况少，但要接受。

在审核环节，AI 的角色从"主跑"切到"被修订方"，它：

① 根据团队反馈更新文档

② 对团队提出的新约束<span style="color: red; font-weight: bold;">显式标注</span> —— 是"<span style="color: red; font-weight: bold;">已纳入原方案</span>"<span style="color: red; font-weight: bold;">还是</span>"<span style="color: red; font-weight: bold;">与原方案冲突</span>，<span style="color: red; font-weight: bold;">需要重排步骤</span>"。

- 这个标注不能省
- 否则团队给的反馈被悄悄吸收进文档，下一次改造就丢了"为什么当初这么改"的依据。

### 5.2 为什么 Step 7 不能省

七步法的闭环在 Step 7 完成——**决策权始终在人手里**。AI 把调研做全，团队把方向定准，再把定稿沉淀回 docs/，下一次改造就有了更准的起点。

<span style="color: red; font-weight: bold;">省下 Step 7 直接进开发，到时候要补回来。而且是以"代码改完发现要返工"的方式补，代价大十倍。</span>返工的不只是代码，还有围绕代码的测试、文档、灰度计划——这些一旦基于错误方向铺开，回退成本是指数级的。

Step 7 的本质，是把<span style="color: red; font-weight: bold;">团队脑子里的隐性知识</span>显式地注入到方案文档里，让 AI 的调研和团队的经验在文档层面合流。

- 前 6 步 AI 产出的是"基于代码的客观还原 + 通用最佳实践"
- <span style="color: red; font-weight: bold;">Step 7 把它升级成"基于代码 + 项目语境的可执行方案"。</span>少了这一步，方案文档就只是 AI 的单向输出，不是团队真正认可、可以直接照着写代码的东西。

## 6. 把改造方案沉淀回 docs/

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/698fb2ac74ee53ec503f938f84469237_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 6.1 为什么要回灌 docs/

七步跑完、Step 7 审核定稿，开发者手上有一份拍过板的 `docs/requirements/prompt-version-diff-solution.md`。但这一轮的产出不止这份文档。

审核过程中，开发者和 AI 反复交锋，沉淀了一批新信息：

- 新发现的边界（如 Spring Security 白名单这类隐性约束被显式化）
- 新识别的老项目约定
- 新的项目级判断。

这些信息如果只留在 `solution.md` 里，下次类似改造又要从头摸索一遍。

类比传统软件工程：`docs/` 就是项目的架构知识库 —— 但和静态 Wiki 不同，它要随着每次改造被验证、被丰富。<span style="color: red; font-weight: bold;">docs/ 资产不是一次性产出，是每次改造都被验证、被丰富的活资产</span>。这一步就是"活资产闭环"的落地：把这一轮深度思考的副产物，反向喂回知识库，越用越准。

### 6.2 回灌提示词

回灌用一段提示词驱动 AI 完成，逐份 docs/ 文件列出改动要求，并要求输出 diff 供人复核：

```
基于已定稿的 prompt-version-diff-solution.md（已含审核调整），

更新所有相关 docs/ 资产：

1. docs/api-list.md：
   预先把 GET /api/prompt/version/diff 接口加进去，标"开发中"，
   入参和返回结构按方案最终版本

2. docs/data-model.md：
   加新增的 PromptVersionDiffResult / VersionMeta / DiffItem 三个 DTO

3. docs/requirements/prompt-version-diff.md：
   把审核中新发现的边界（Spring Security 白名单等）补进对应小节

4. CLAUDE.md：有没有新发现的"老项目约束"应该补进去。具体判断：

   - 这条约束是项目级的（影响所有未来类似改造）→ 写进 CLAUDE.md
   - 这条约束只是这一次的特殊处理 → 留在 solution.md 就行

   比如"复用现有 Service 方法前先确认有无 metrics 副作用"是项目级约束，应写进 CLAUDE.md。

   "VersionCompareModal props 改造方式"是这次的特殊处理，留在 solution.md 即可。

输出每份文件的改动 diff 给我 review。
```

### 6.3 CLAUDE.md 判断标准

回灌时最需要判断力的是 `CLAUDE.md` —— 不是所有发现都该写进去。

`CLAUDE.md` 可以类比成传统项目里的"架构约束文档 + 编程规范 + 团队 Wiki"，但有一个关键区别：它是写给 AI 看的，AI 在每次改造前都会读它，把它当作项目背景。所以它的内容选择标准就是"作用域"——只收"通用约定"，不收"个案决策"。

判断标准如下：

| 判断标准              | 处理方式                                   | 示例                                  |
| ----------------- | -------------------------------------- | ----------------------------------- |
| <span style="color: red; font-weight: bold;">项目级约束</span>（影响所有未来类似改造） | 写进 `CLAUDE.md`，让 AI 在后续每次改造都自觉遵守       | "复用现有 Service 方法前先确认有无 metrics 副作用" |
| <span style="color: red; font-weight: bold;">一次性</span>特殊处理（仅这一次相关）   | 留在 `solution.md`，不污染 `CLAUDE.md` 的全局视野 | "VersionCompareModal props 改造方式"    |

这个判断标准本身就是一种方法论 —— <span style="color: red; font-weight: bold;">区分"通用约定"和"个案决策"，让 CLAUDE.md 保持精炼，只承载真正影响全局的约束</span>。`CLAUDE.md` 一旦塞满个案细节，AI 读它时反而抓不住项目级主线，效果适得其反。

### 6.4 与 docs-auto-sync Skill 的关系

这一步也可以用 docs-auto-sync Skill 跑，效果一样 —— Skill 把"逐份 docs/ 文件比对、输出 diff、人工复核"这套流程固化成了可复用工具。

## 7. 产出、Check List 与边际价值思考

跑完整套七步加文档维护更新，开发者手上到底多了什么？这一章把产出清单、操作 Check List 和"哪一步最值"的开放思考放在一起，作为全文收尾。

### 7.1 最终产出清单

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/ade3b2e355e45f19cc654bac1bb3fc15_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

整轮跑完，开发者手上的产出汇总如下：

| 产出物 | 类型 | 说明 |
|---|---|---|
| `docs/requirements/prompt-version-diff-solution.md` | 主产出 | 审核定稿 + 决策落定的改造方案文档 |
| `docs/api-list.md` | 资产同步 | 加了新接口（标"开发中"） |
| `docs/data-model.md` | 资产同步 | 加了 `PromptVersionDiffResult` / `VersionMeta` / `DiffItem` 三个 DTO |
| `docs/requirements/prompt-version-diff.md` | 资产同步 | 补了审核中新发现的边界（如 Spring Security 白名单） |
| `CLAUDE.md` | 资产同步 | 加了一条项目级约束（如适用） |
| `docs/requirements/prompt-version-diff-impact.md` | 资产同步 | 链路 + 改造点 + 影响 + 步骤的详细文档，作为 `solution.md` 的引用源 |

主产出只有一份：审核定稿的方案文档。其余五份是资产同步产出，价值不在这一轮，而在下一轮改造时让 AI 起步点更高。

### 7.2 时间成本

整个七步加文档维护更新跑下来 60-90 分钟，比手写方案文档省一天，且质量更高——有 AI 的代码级扫描和开发者的经验审核两层叠加。下一阶段进入编码实施：每个改造点对应明确的代码改动，不用再决策、不用再调研、不用再纠结。

### 7.3 改造前 Check List

用于自检 Step 1-6 产出是否齐全：

- [ ] Step 1：改造链路已画出，覆盖前端到 DB 的全链路
- [ ] Step 1：前端节点已标注"现有需修改"或"不存在需新建"
- [ ] Step 1：链路中间环节（拦截器/AOP/异常处理/事务/异步）已逐一确认
- [ ] Step 2：每个节点的改造点已编号，含类型、涉及文件、改什么、工作量
- [ ] Step 2：前端改造点单独列出，与后端接口一一对应
- [ ] Step 2：测试改造点已列（新增用例、修改用例、回归范围）
- [ ] Step 2：配置类改动单独可追踪
- [ ] Step 3：时序图覆盖前端 → 后端 → DB 完整调用链
- [ ] Step 3：新增节点与既有节点在图上可区分
- [ ] Step 3：异常/失败路径已画出
- [ ] Step 4：影响范围按接口/调用链/测试/文档/前端兼容/性能/数据/运维逐项评估
- [ ] Step 4：每项影响标注高/中/低风险，并给出判断依据
- [ ] Step 5：改造步骤按依赖排序，每步标明依赖与工作量
- [ ] Step 5：可并行步骤（尤其前后端并行）已识别
- [ ] Step 5：数据迁移/灰度/回滚步骤已单列
- [ ] Step 5：关键决策点已显式抽出
- [ ] Step 6：方案文档按标准结构组织（概要/链路/改造点/流程图/影响/步骤/决策点）
- [ ] Step 6：待审核决策点已集中到独立章节，每条含背景、选项、AI 建议
- [ ] Step 6：文档中"事实"与"AI 建议"已区分标注

### 7.4 审核定稿 Check List

用于自检 Step 7 三层审核是否到位：

- [ ] 第一层：所有待审核决策点已逐条拍板，无"待定"遗留
- [ ] 第一层：每个决策点的选择都有书面理由
- [ ] 第二层：链路完整性已结合团队对老系统的了解复核
- [ ] 第二层：AI 看不到的隐性耦合、副作用、特殊配置已补充
- [ ] 第二层：产品历史决策导致的"不能动"区域已标注
- [ ] 第二层：外网接口、数据迁移、灰度策略已重点确认
- [ ] 第三层：审核结论已反馈给 AI，方案文档已修订
- [ ] 第三层：AI 对新约束的"已纳入/需重排"标注已核对
- [ ] 第三层：修订后的文档与审核结论一致，可作为开发依据

### 7.5 文档维护更新 Check List

用于自检审核中新发现是否已沉淀回 docs/：

- [ ] api-list：新增/修改/废弃的接口已更新到接口清单
- [ ] data-model：表结构、字段、索引、枚举变更已更新到数据模型文档
- [ ] 需求文档：原始需求与最终方案决策已关联归档
- [ ] CLAUDE.md：影响 AI 后续理解的架构约束、约定、特殊处理已写入项目说明
- [ ] impact.md：本次改造的影响范围、风险等级、决策结论已沉淀
- [ ] 回滚预案：数据迁移与灰度的回滚步骤已记录在案
- [ ] 关联项：被本次改动牵动的其他模块文档已同步标注

### 7.6 边际价值——哪一步对你的项目最值

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/9a80601701daaa9e12bafa49732aab9c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

七步法里哪一步对团队价值最大？是 Step 1（摸链路）、Step 4（说影响），还是 Step 6（整合给人审核）？答案不唯一——它取决于项目本身的形态。换一个项目，价值最高的那一步就会换。

关键是看每一步的**边际价值**：在团队现有能力栈之上，这一步额外补齐的那块认知缺口值多少。下表列出三类典型场景：

| 项目场景 | 价值最高的 Step | 理由 |
|---|---|---|
| 测试覆盖完善的项目 | Step 4（说影响） | 自动化测试已经覆盖了大部分回归路径，链路不靠人画也能跑出来；真正稀缺的是对性能、数据兼容、运维副作用这些测试覆盖不到的影响维度的提前评估 |
| 前后端分离明确的项目 | Step 1（摸链路） | 接口契约稳定、职责边界清晰，影响范围相对可控；真正的难点是老链路里隐藏的中间环节（拦截器/AOP/异步任务）被 AI 系统性扫出来 |
| 多人协作的大型改造 | Step 6（整合给人审核） | 参与方多、决策点散、隐性约束多；最有价值的是把分散的"待定"收敛成一份可逐条拍板的决策清单，让审核会不再在细节里迷失 |

所以七步法不是一套平均用力的流程，而是一组可以按项目形态调权重的工具。识别出当前项目最稀缺的那块认知，把对应的 Step 做厚，其余 Step 维持基准产出，六十分钟的投资回报就最高。

### 7.7 核心要点回顾

这一篇的核心一句话：**让 AI 把改造想透，开发者审核拍板，60 分钟出一份高质量改造方案文档**。

七步骨架：

1. 摸涉及的链路（含前端）
2. 列改造点（后端 + 前端 + 测试 + 文档）
3. 画改造流程图
4. 说影响范围与风险
5. 说改造步骤与顺序
6. 整合信息聚焦给人审核
7. 开发者审核 + 反馈调整 + 定稿

再加一步文档维护更新：把这一轮的新信息回灌到所有 docs/，完成活资产闭环。

呼应第 6 章的观点：docs/ 不是一次性产出，是每次改造都被验证、被丰富的活资产——这一篇是活资产闭环的第一次落地，编码实施完成后，资产还会再被更新一轮。
