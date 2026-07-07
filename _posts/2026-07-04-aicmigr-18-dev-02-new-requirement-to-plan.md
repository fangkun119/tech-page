---
title: 传统项目迁AI 18：项目开发 - 从新需求到改造方案
author: fangkun119
date: 2026-07-04 18:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
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

## 1. 全文导读

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/5ddb496c86726d584ac503708704f389_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇把"从新需求到改造方案"拆成两部分组织：第一部分提炼方法论（参考手册风，可裁剪 Check List），适合熟练工程师快速回顾；第二部分结合 prompt-version-diff 案例逐步复现七步法（教材风，深入解释 why），适合初学工程师系统掌握。读者可按下图定位章节。

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/3d821b8554683335d41f5ce5f95816f5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
\`\`\`mermaid
graph TD
    A[本篇：从新需求到改造方案] --/> B[第一部分 方法论提炼]
    A --/> C[第二部分 实战演示]

    B --/> B1[1、全文导读]
    B --/> B2[2、总览：需求清楚 ≠ 可以动手]
    B --/> B3[3、方法论核心：七步法]
    B --/> B4[4、可裁剪 Check List]

    B3 --/> B3a[3.1 七步法总览图]
    B3 --/> B3b[3.2 AI 当调研员]
    B3 --/> B3c[3.3 七步法详解表格]

    C --/> C1[5、案例背景：Prompt 版本 Diff]
    C --/> C2[6、七步法实战复现]
    C --/> C3[7、文档维护更新：沉淀回 docs/]
    C --/> C4[8、最终产出与小结]
    C --/> C5[9、思考]

    C2 --/> C2a[6.1-6.3 Step 1-3 链路/改造点/流程图]
    C2 --/> C2b[6.4-6.7 Step 4-7 影响/步骤/整合/审核]
\`\`\`
-->

- 初学工程师：建议通读第一部分建立框架，再按第二部分逐 Step 复现，遇到方法论细节回查第一部分对应表格。
- 熟练工程师：可直接看第 3 章七步法详解表格和第 4 章可裁剪 Check List，第二部分按需查阅。

## 2. 总览：需求清楚 ≠ 可以动手

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/4a9b4567c4bf5b8ae2abdb155c1b8a2b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 老项目改造最常见的翻车点

需求文档拿到手，开发者常做的一件事是：扫一眼、心里大概有数，然后直接上手写代码。这个思路本身没错，但很看开发者的水平和经验——这也是老项目改造里最常见的翻车原因。

老项目不是新项目，"心里大概有数"远远不够。比如开发者以为只改一个 Controller，实际可能牵动整条链路（Service / DAO / 配置 / 异常处理 / 测试）；以为这次只动后端，实际前端要加按钮、加组件、加调用。这些坑不在动手前想清楚，就在动手中、动手后才发现，代价是返工。

### 2.2 让 AI 把"怎么改"一次性展开

正确的思路是：让 AI 帮开发者把"怎么改"一次性展开。AI 读现有代码、画链路、列改造点、说影响、说改造步骤，最后整合成一份方案文档让人审。开发者看着方案文档审核、补漏、调整，才动手写代码。

这里有两个特别要注意的方法论要点：

#### (1) 让 AI 显式考虑前端

AI 默认只看接口层，会把改造缩成"加一个后端接口"，但有些老项目改造需要涉及前端。提示词必须明确要求 AI 考虑前端调整，不然会漏掉前端的工作量。

#### (2) 让 AI 整合信息聚焦给人审

前面几步 AI 会输出一堆零散内容（链路、改造点、流程图、影响、步骤），最后必须让 AI 把这些整合成一份结构清晰、易于审核的方案文档。AI 默认会平铺直叙，开发者要明确要求"按改造方案标准格式整合"。

### 2.3 "做什么" vs "怎么改"——方案文档的价值

需求文档讲"做什么"，改造方案讲"怎么改"，二者之间存在一段必须跨越的距离。一份合格的改造方案要回答：动哪条链路、每个节点改什么、改动会牵动哪些既有功能、按什么顺序改、哪些决策需要人拍板。

需求清楚并不等于可以立即动手开发，跳过方案设计直接写代码，是老项目改造最常见的翻车场景之一。中间隔着的这份方案文档，就是七步法要产出的核心成果。

## 3. 方法论核心：七步法

### 3.1 七步法总览图

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/1d7568e3f862a439c0b76fc3b76eef89_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/ec1b1c6656407cbc4c0193aee469f9a5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/18_项目开发_02：从新需求到改造方案/ec1b1c6656407cbc4c0193aee469f9a5_MD5.jpg
用途：七步法总览图，展示从需求到改造方案的完整流程
内容：Step 1 摸链路（含前端）→ Step 2 列改造点 → Step 3 画流程图 → Step 4 说影响 → Step 5 说改造步骤 → Step 6 整合给人审 → Step 7 人审核定稿，外加文档维护更新回灌 docs/。图分三段：Step1-6 AI 自动产出（浅蓝）、Step 7 人工审核（粉色）、方案文档产出（浅紫）。
-->

### 3.2 七步法的核心思想：AI 当调研员

七步法把"怎么改"这件事整体交给 AI 当调研员：让 AI 通读现有代码、画出改造牵动的链路、逐节点列出改造点、说明影响范围、给出步骤与顺序，最后把零散产出整合成一份结构清晰的方案文档交给团队审核。AI 在这个环节的价值在于"广度扫描"——能快速读完一个改动可能牵动的 Controller/Service/DAO/配置/异常处理/测试/前端调用，把开发者凭脑补容易漏掉的节点一次性铺开，并对"是否要改前端"这类隐性假设做显式验证。

但 AI 的能力边界同样清晰：它基于代码反推事实，叠加通用最佳实践给出建议，却看不到团队隐性约束、老接口的历史副作用、配置的特殊处理、产品的历史决策。这些信息只存在于团队脑子里。因此七步法的另一面是"人主导审核拍板"——AI 把调研做全，人把方向定准。Step 1-6 让 AI 把"怎么改"展开到可审阅的程度，Step 7 把决策权交回团队，三层审核后定稿，并把结论沉淀回 docs/，形成闭环。

### 3.3 七步法详解

#### (1) 七步法逐 Step 表格

##### ① Step 1：摸改造涉及的链路（含前端）

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/5d7519ec6894d09a920d3d116e87ce65_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 维度 | 内容 |
|---|---|
| 目标 | 在动手之前，先把一个改动从 HTTP 入口一路摸到 DB，确认整条链路上的节点，避免"只改 Controller"式的盲改。 |
| 做什么 | 让 AI 读相关代码，列出改动牵动的全部节点：前端调用（页面/组件/API 封装）、网关/拦截器、Controller、Service、DAO/Mapper、DB 表/字段、配置、异常处理、日志、测试。按 HTTP 入口 → 业务逻辑 → 数据落库的顺序串成一条链路。 |
| AI 如何思考 | 先从需求中提取关键动名词定位入口，再沿调用关系向下追溯；遇到接口/抽象类枚举所有实现；遇到配置项反查注入点；遇到异常体系反查全局处理。显式追问"这个改动是否需要前端配合"。 |
| 关注点 | 前端节点必须显式标注两种状态——"现有组件需修改"还是"组件不存在需新建"；链路中间环节别漏，尤其是拦截器、AOP、全局异常处理、事务边界、消息异步链路。 |
| review 重点 | 前端节点状态标注是否准确（区分"改"与"新建"）；链路是否完整，中间层有没有被跳过；跨服务/跨模块的调用是否画全。 |
| 技巧 | 给 AI 一份"链路节点清单模板"，要求它逐项回答"涉及/不涉及 + 理由"，强制覆盖而非自由发挥；对老项目要求 AI 显式列出"可能存在但我没找到的环节"。 |

链路图是后续所有步骤的地基。链路上漏掉一个节点，后面的改造点列表、影响范围、步骤排序就会整体偏差。因此这一步宁可多列、不可少列，把不确定的节点也标出来让人确认。

##### ② Step 2：列所有改造点

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/01187bdc76e8a291de2afa1b2f3df01c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 维度 | 内容 |
|---|---|
| 目标 | 在链路的每个节点上，把"具体要改什么"逐条列清，形成可估工、可分配的改造点清单。 |
| 做什么 | 对链路上的每个节点列出改造点，每条至少包含：编号、所属节点、改造类型（新增/修改/删除/迁移）、涉及文件、改什么（一句话描述）、预估工作量。前端、配置、测试、文档各自单列，不混在业务代码里。 |
| AI 如何思考 | 沿 Step 1 的链路逐节点展开，对每个节点回答"为了满足需求，这里要动什么"；对每个改造点反问"改了之后，上下游会不会需要联动调整"，把联动项也补进清单。 |
| 关注点 | 前端改造点是否齐（按钮、表单、组件、路由、状态、接口封装、错误提示）；测试改造点是否齐（新增用例、修改既有用例、回归范围）；配置类改动是否单独可追踪。 |
| review 重点 | 前端改造点与后端接口是否一一对应；测试有没有被遗忘；"迁移/删除"类高风险改造点是否标了出来。 |
| 技巧 | 要求 AI 按统一模板输出表格，禁止散文式描述；要求每条改造点都能独立指派给某个人、能独立估工。 |

改造点清单是步骤排序（Step 5）和影响范围（Step 4）的输入。这一步列得越具体，后面越不容易返工。

##### ③ Step 3：画改造流程图

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/6b40133d02db9f00b8a969595c7134ad_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 维度 | 内容 |
|---|---|
| 目标 | 用一张时序图把"改造后系统怎么跑"可视化，让人一眼看出节点间的调用依赖与数据流向。 |
| 做什么 | 输出一张时序图（sequence diagram），覆盖前端 → 网关 → Controller → Service → DAO → DB 的完整调用链，标出新增/修改的调用、异步消息、跨服务调用、异常返回路径。 |
| AI 如何思考 | 以一个完整请求为线索，从用户操作出发，逐步推导每一跳的调用方与被调方；遇到分支（成功/失败/异常）显式画出；把 Step 2 中标记为"新增"的节点用不同颜色或注释突出。 |
| 关注点 | 前端到后端的调用链是否完整（不能只画后端内部）；数据流方向清楚（谁是读、谁是写）；异常路径不能省略。 |
| review 重点 | 调用链是否覆盖所有改造点；新增节点与既有节点的区分是否清晰；跨服务边界是否标明。 |
| 技巧 | 指定使用 Mermaid sequenceDiagram 语法，方便直接嵌入文档；要求 AI 在图下方配一段文字说明，解释关键调用与新增逻辑。 |

流程图是给人审的"全局视图"。审核者通常先看图再看文字，图错了，后面的细节都没人愿意看。

##### ④ Step 4：说明影响范围

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/035d3c24aeda08154ea263da1682b966_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 维度 | 内容 |
|---|---|
| 目标 | 把改造对现有系统的影响讲清楚，并标出风险等级，让团队在动手前就知道哪里最容易出事。 |
| 做什么 | 从以下维度逐一评估影响：对外接口签名/兼容性、内部调用链路、既有测试用例、技术文档、前端兼容（旧版本客户端是否受影响）、性能（DB 查询、缓存、并发）、数据迁移、运维（配置、监控、告警）。每项标注高/中/低风险。 |
| AI 如何思考 | 对每个影响维度问三个问题：会不会受影响、受影响的表现是什么、严重程度如何；对高风险项反问"有没有更稳的替代方案"；对涉及数据或外网接口的项默认提高一档风险。 |
| 关注点 | 风险等级要给理由，不能只贴标签；不要漏掉"看似无关但实际耦合"的模块（共用的枚举、共用的工具类、共用的中间表）。 |
| review 重点 | 风险等级是否合理（AI 倾向于低估老系统的隐性耦合）；有没有漏掉的影响项；外网接口和数据迁移是否被重点提示。 |
| 技巧 | 要求 AI 对每项影响给出"判断依据"——它是看了哪段代码、哪个配置得出这个结论的，便于审核者快速复核。 |

影响范围是 Step 7 决策点的重要输入。高风险项往往就是需要在审核会上重点讨论的决策点。

##### ⑤ Step 5：说明改造步骤与顺序

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/55b2683feddd6eecc2300c74d369ff6c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 维度        | 内容                                                                                                                                         |
| --------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| 目标        | 给出按依赖排序的改造步骤，让团队知道先做什么、后做什么、每步依赖谁、关键决策在哪。                                                                                                  |
| 做什么       | 输出有序的步骤列表，每步包含：步骤编号、做什么、依赖哪些前置步骤、预估工作量、产出物。关键决策点显式列出；团队有分歧的决策给出两个方案 + 推荐 + 理由；没有分歧的决策直接给方案，不做过度调研。                                         |
| AI 如何思考   | 按改造点之间的依赖关系拓扑排序（DB → DAO → Service → Controller → 前端，数据/契约先行）；识别可以并行的步骤；识别必须串行的步骤（如数据迁移要先于代码上线）；对前端步骤单独标注——前端往往可以在后端接口契约确定后并行推进，不必等后端全部完成。 |
| 关注点       | 前端依赖是否正确（前端等的是"接口契约"，不是"后端全部完成"）；数据迁移、灰度、回滚步骤是否单列；关键决策点不能埋在步骤描述里，要抽出来。                                                                     |
| review 重点 | 关键决策点列得全不全；前端能否提前并行；有没有"看似无依赖实则强依赖"的步骤被错排。                                                                                                 |
| 技巧        | 要求 AI 对每个决策点标注"分歧程度"（已共识/有分歧），只对"有分歧"的项展开两方案对比，避免把简单决策也写成调研报告。                                                                             |

步骤排序的价值在于缩短整体周期。把能并行的步骤识别出来（尤其是前后端并行），往往比压单点工作量更有意义。

##### ⑥ Step 6：整合信息聚焦给人审

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/27b61d86d8c56b6246a1fd86e3c52cf2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 维度 | 内容 |
|---|---|
| 目标 | 把前五步零散的产出（链路、改造点、流程图、影响、步骤）整合成一份结构清晰、可直接上会的方案文档。 |
| 做什么 | 按改造方案的标准结构组织文档：一句话概要、改造链路、改造点清单、流程图、影响范围、改造步骤、待审核决策点。每节用统一模板，避免散文。 |
| AI 如何思考 | 默认倾向是平铺直叙地复述前五步内容，因此要明确要求"按改造方案标准格式整合、聚焦审核"；把散落在各步中的决策点集中抽到第 7 节"待审核决策点"，每条给出背景、选项、AI 建议、需要人决策的原因。 |
| 关注点 | 第 7 节"待审核决策点"是文档的灵魂——它是 Step 7 三层审核的第一层输入；决策点不能散落在各节里，必须集中；无关细节要砍掉，审核者要的是结论与依据，不是过程流水账。 |
| review 重点 | 决策点提取得齐不齐（对照前五步逐一核对）；结构清不清晰（审核者能否在 5 分钟内抓住全部决策点）；有没有把 AI 的猜测当成既定事实写进文档。 |
| 技巧 | 明确给 AI 一份"方案文档骨架"作为模板，要求严格按骨架填充；要求 AI 区分"事实（来自代码）"与"建议（来自最佳实践）"，两者在文档中用不同标记呈现。 |

整合的目的是"聚焦"。一份好的方案文档不是把所有信息都堆上去，而是让审核者用最短时间抓住"要拍什么板、为什么这么拍"。

##### ⑦ Step 7：人审核 + 调整 + 定稿

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/06a2f90ea7ce55eaeee8f73029e2ec3b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 维度 | 内容 |
|---|---|
| 目标 | 由团队对方案文档进行三层审核，拍板决策点、校验链路与影响、反馈 AI 调整，最终定稿。 |
| 做什么 | 三层审核：(1) 决策点拍板——逐条确认 Step 6 抽出的待审核决策点，给出选择与理由；(2) 链路/改造点/影响范围审核——结合团队对老系统的了解，补 AI 看不到的耦合、副作用、隐性约束；(3) 反馈 AI 调整——把审核结论回传给 AI，让其修订方案文档，必要时回到 Step 1-6 局部重跑。 |
| AI 如何思考 | 在审核环节 AI 是"被修订方"，不是决策方；它根据团队反馈更新文档，并对团队提出的新约束显式标注"已纳入/与原方案冲突，需重排步骤"。 |
| 关注点 | 必须由人主导，因为以下信息 AI 看不到：团队隐性约定（命名/分层/错误码规范）、老接口的历史副作用（被未知下游依赖）、配置的特殊处理（环境差异/灰度策略）、产品的历史决策（某些"看起来该改"的东西其实不能动）。 |
| review 重点 | 决策点是否全部拍板（不能遗留"待定"进入开发）；AI 看不到的隐性约束是否被补充进去；修订后的文档是否真正反映了审核结论。 |
| 技巧 | 审核会前先把 Step 6 的"待审核决策点"单独抽出作为会议议程，避免在细节里迷失；审核结论要有书面记录，作为后续回灌 docs/ 的依据。 |

七步法的闭环在 Step 7 完成——决策权始终在人手里。AI 把调研做全，团队把方向定准，再把定稿沉淀回 docs/，下一次改造就有了更准的起点。

## 4. 可裁剪 Check List

### 4.1 改造前 Check List（Step 1-6 产出是否齐全）


<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/6371af27c7808c29457984498a4a97a5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">


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

### 4.2 审核定稿 Check List（Step 7 三层审核）

- [ ] 第一层：所有待审核决策点已逐条拍板，无"待定"遗留
- [ ] 第一层：每个决策点的选择都有书面理由
- [ ] 第二层：链路完整性已结合团队对老系统的了解复核
- [ ] 第二层：AI 看不到的隐性耦合、副作用、特殊配置已补充
- [ ] 第二层：产品历史决策导致的"不能动"区域已标注
- [ ] 第二层：外网接口、数据迁移、灰度策略已重点确认
- [ ] 第三层：审核结论已反馈给 AI，方案文档已修订
- [ ] 第三层：AI 对新约束的"已纳入/需重排"标注已核对
- [ ] 第三层：修订后的文档与审核结论一致，可作为开发依据

### 4.3 文档维护更新 Check List（沉淀回 docs/）

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/fe7c6f0d82df34bd5f835cf486ec811c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

- [ ] api-list：新增/修改/废弃的接口已更新到接口清单
- [ ] data-model：表结构、字段、索引、枚举变更已更新到数据模型文档
- [ ] 需求文档：原始需求与最终方案决策已关联归档
- [ ] CLAUDE.md：影响 AI 后续理解的架构约束、约定、特殊处理已写入项目说明
- [ ] impact.md：本次改造的影响范围、风险等级、决策结论已沉淀
- [ ] 回滚预案：数据迁移与灰度的回滚步骤已记录在案
- [ ] 关联项：被本次改动牵动的其他模块文档已同步标注

## 5. 案例背景

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/7223b6d0bc03af51c6eca83031ef6682_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 5.1 案例需求：Prompt 版本 Diff

本系列选取 prompt-version-diff 这一真实需求作为贯穿性案例，用以演示七步法在前后端联动改造中的完整应用。

该需求的核心问题是当前 Prompt 版本对比的实现路径过于冗长。现有方案采取的是"两次单版本请求 + 前端拼装"的模式：前端 `version-history.jsx` 在用户勾选两个版本后，先后发起两次单版本查询请求拿到各自内容，再在 `VersionCompareModal.jsx` 中执行内存级的 diff 计算。这种做法虽然功能上能跑通，却存在三个明显的工程债——网络往返次数翻倍、对比逻辑散落在前端（与后端领域逻辑割裂）、以及后续若要做权限收敛或审计难以在统一入口收口。

改造目标是把对比的"计算职责"从前端下沉到后端：新增 `GET /api/prompt/version/diff` 接口，由后端 `PromptVersionService` 统一拉取两个版本内容、计算字段级差异，并以结构化的 `PromptVersionDiffResult` 一次性返回。前端 `VersionCompareModal.jsx` 则保留其已有的行级 diff 渲染逻辑（`renderDiffLines`），仅把数据来源从"本地拼装"切换为"接口直取"。

需要注意的是，这次改造刻意不动数据库 schema——`prompt_version` 表保持原样，所有逻辑都在应用层完成。这一点决定了后续链路扫描中 Mapper 与 MyBatis XML 都会被标记为"复用不动"，也决定了改造点清单不会出现任何 DB 迁移相关的工作量。

### 5.2 为什么选这个案例

选择案例的标准不是"看起来多复杂"，而是"能否覆盖方法论的关键决策点"。prompt-version-diff 恰好在一个中等体量的范围内，同时命中了三个具有教学价值的维度。

#### (1) 前后端联动的完整性

很多教程案例只讲后端或只讲前端，但真实的业务改造几乎都是跨层联动：一个接口的新增会牵动前端的 API 函数、类型声明、组件状态，也会牵动后端的 Controller、Service、ServiceImpl、Mapper。本案例刚好覆盖了从 React 组件到 MyBatis 查询的完整纵切面，能够训练开发者在链路扫描时不漏掉任何一端。如果在小型案例里习惯了"只看后端"，到真实项目里极易出现"后端改完了才发现前端要联调"的返工；本案例正是用来纠正这种单边视角的好教材。

#### (2) "复用现有组件"的决策价值

本案例最精彩的部分不在于"新增了什么"，而在于"避免了新增什么"。`VersionCompareModal.jsx` 已经实现了 `renderDiffLines` 的行级渲染，`PromptVersionMapper.selectByPromptKeyAndVersion` 也已经能按版本号精确取数。如果方法论执行不到位，AI 很容易给出"引入 react-diff-viewer"或"新建 diff 组件"这类看似合理实则重复造轮子的建议。本案例能让读者直观体会到——好的链路扫描会把"现有可复用资产"显式地列出来，从而把改造成本从"新建一套"压低到"换一个数据源"。

#### (3) 存在真实的工程决策分歧

即便是看似简单的 diff 接口，也会在落地时冒出一系列非平凡的选择：当某个版本的字段为 null 时，`DiffItem` 的 `valueA`/`valueB` 应当是空字符串、null 还是 undefined？`changed` 字段的判定规则是按字节比较、按行比较还是按语义比较？前端 `VersionCompareModal` 的 props 结构是否需要从"两个独立 content"改为"一个 diffResult 对象"？这些决策点不会出现在需求文档里，却会决定改造的质量上限。本案例会在 Step 4 的影响范围分析中集中暴露这些分歧，让读者看到方法论如何"逼出"隐藏的设计问题。

综合这三个维度，prompt-version-diff 是一个"麻雀虽小、决策点俱全"的样本，适合作为七步法的入门复现案例。

## 6. 七步法实战复现

本篇以 prompt-version-diff 为样本，逐步复现七步法。每一节都会引用原始提示词与 AI 的产出片段（链路表格、改造点清单、mermaid 时序图），并着重解释"为什么这样提问""为什么产出长这样""review 时该盯哪些点"，而非简单罗列步骤。

### 6.1 Step 1：摸出改造涉及的链路（含前端）

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/a5e3f060a0290931362d83899065e3cf_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一步要解决的核心问题是"消除视野盲区"。开发者拿到需求后最常见的失误不是技术判断错误，而是根本没意识到某个节点存在于链路中——比如忘了拦截器、忘了前端某个被复用的展示组件、忘了 typing 声明文件。一旦节点漏列，后面的改造点清单就会跟着漏，最终在编码或联调阶段返工。因此 Step 1 的设计目标是让 AI 在固定模板下做一次"全链路扫描"，把 HTTP 入口到 DB 查询之间的所有节点一次性铺开，并且明确区分"现有可复用"与"需要新增/修改"两类，为后续判断"该不该新建组件"提供事实依据。

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
路径：imgs/18_项目开发_02：从新需求到改造方案/872c430e5301bd3afcc9166721775118_MD5.jpg
用途：Step 1 让 AI 摸改造链路的产出片段示例（链路节点表格）
内容：AI 扫代码后输出的链路表格，列出前端入口 version-history.jsx（现有需修改）、前端对比弹窗 VersionCompareModal.jsx（现有需修改）、前端 API 函数 services/prompt/index.ts（需新增 getDiffVersion）、前端类型声明 typing.ts（需新增 diff types）、后端 Controller PromptController.java（需新增接口）、后端 Service PromptVersionService.java（需新增 diffVersions 方法）、后端 ServiceImpl（需新增实现，两次 Mapper 查询+内存比较）、后端 Mapper PromptVersionMapper.java（不动，复用 selectByPromptKeyAndVersion）、MyBatis XML（不动）、DB prompt_version 表（不动，纯查询无 schema 变更）。
-->

为什么这份产出值得细看？关键在于 AI 把两个前端节点标成了"现有需修改"——`version-history.jsx` 已有勾选交互和 `showCompare` 状态，`VersionCompareModal.jsx` 已实现 `renderDiffLines` 的行级 diff 渲染。这正是提示词中"不要漏前端节点"这条显式约束起作用的结果。如果不加这条约束，AI 会默认沿用"前端是黑盒、只看后端"的隐性偏好，把前端折叠成一句"前端适配"，开发者就无从知晓已有组件可以复用。链路扫描出"现有组件可用"，直接把改造的复杂度从"新建 diff 组件 + 新建接口"压低到"换数据源 + 新建接口"，这是本案例最大的成本节省点。

另一方面，这份产出之所以清晰，是因为提示词本身的约束足够具体：要求"文件、类、方法、关键逻辑"四列固定，要求"标出现有/新增/修改"三态，要求"表格 + 链路图"双形态。模糊的提示词（"帮我分析一下影响范围"）会换来模糊的散文式回答；结构化约束换来结构化产出。这条经验贯穿整个七步法——把期望的输出形态写进提示词，比寄希望于 AI 自由发挥更可靠。

review 时的重点有两个：

#### (1) 前端节点的分类是否准确

开发者要逐条核对"现有需修改"和"不存在需新建"是否标错。AI 有时会因为读到了某个文件就误判为"现有"，但那个文件可能只是名字相近、实际逻辑要重写；反过来也可能把"实际不存在、需要新建"的文件误归到"现有"。本案例中 `services/prompt/index.ts` 标为"需新增 getDiffVersion 函数"是正确的——文件存在，但目标函数不存在，属于"在现有文件里新增"，与"现有需修改"语义不同。

#### (2) 链路完整性

从 HTTP 入口到 DB 出口之间，是否漏掉了横切关注点。AI 最容易跳过的是拦截器、AOP 切面、统一异常处理、参数校验 filter。这些节点虽然不一定要改，但必须在链路图里出现，否则后续做影响范围分析时会出现"看似只动了 Service，实则新接口绕过了某个全局校验"的隐性事故。本案例由于是 GET 查询且无权限收敛需求，横切节点确实可以不动，但 review 时仍要确认它们被列出来了。

### 6.2 Step 2：列出所有改造点

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/11971ffbcab91a3796b6bd9d82a2131d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Step 1 输出的是"链路图"，是面向理解的；Step 2 要把它转换成"改造点清单"，是面向执行与排期的。这一步要解决的问题是"把模糊的改造范围拆成可估时、可分配、可验收的最小单元"。每个改造点都应当能独立回答"改哪个文件、改哪一段、改成什么样"，并且能在编码完成后单独提交、单独 review。这一步如果做得粗糙——比如只写"前端适配 diff 接口"一条——就会在排期阶段发现无法估时，在编码阶段发现边界不清，最终演变成"边写边发现新工作"的低效循环。

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
路径：imgs/18_项目开发_02：从新需求到改造方案/dddd1962ef6c2bbe1648129c0e8725da_MD5.jpg
用途：Step 2 列改造点的产出片段示例（改造点清单表格）
内容：AI 输出 12 条改造点 P01-P12。后端 P01-P06（新增 PromptVersionDiffResult/VersionMeta/DiffItem 三个 DTO、Service diffVersions 方法签名、Impl 实现、Controller GET diff 接口）；前端 P07-P10（新增 services 函数和 types、修改 VersionCompareModal.jsx 和 version-history.jsx）；测试&文档 P11-P12（api-list.md 和 data-model.md）。无新前端依赖、无 i18n 变更。
-->

值得深入解读的第一个 why：为什么 AI 没有给出"引入 react-diff-viewer"或"新建 diff 渲染组件"这种看似标准的改造点？答案藏在 Step 1 的链路扫描里。正因为 Step 1 明确写出了"`VersionCompareModal.jsx` 已实现 `renderDiffLines`、属现有需修改"，AI 在 Step 2 就拥有了"已有可复用渲染层"的事实前提，自然不会去建议引入第三方库。这就是七步法把"链路扫描"放在"改造点拆解"之前的根本原因——前置的事实采集会约束后续的建议范围，防止 AI 在缺乏上下文时凭训练数据里的"标准做法"凑出多余工作量。如果读者在自己的项目里看到 AI 给出了重复造轮子的改造点，几乎都可以追溯到 Step 1 没把现有资产摸清。

第二个 why：为什么这份清单没有单独的"测试"类型条目？这并不是 AI 的疏漏，而是项目当前测试覆盖有限的现实反映。在测试基础设施尚未完善的项目里，强行列出"为 diffVersions 写单元测试"这类条目，往往会在排期时被无声地推迟或跳过，反而让清单失真。本案例选择把测试工作收敛到 Step 5（落地步骤）里作为执行动作，而不是单独编号为 P。这是一种务实的取舍——清单的真实性优先于清单的"完整性"。读者在自己的项目里应当根据测试成熟度灵活判断：测试覆盖良好的项目，应当为每个新增 Service 方法单列一条测试改造点；测试缺失的项目，则更适合在落地阶段集中补一个冒烟测试。

review 时的重点：

#### (1) 前端改造点的齐全度

P07-P10 四条前端是否把"现有组件改造"和"新建文件"区分清楚了。本案例中 `services/prompt/index.ts` 是"在现有文件里新增函数"，`typing.ts` 是"在现有文件里新增类型"，而 `VersionCompareModal.jsx` 与 `version-history.jsx` 是"修改现有逻辑"——四种语义都被准确表达，没有把"新增函数"误标为"修改"。读者 review 时要特别留意 AI 是否会把"现有文件里加一个函数"笼统标成"修改"，这种笼统会让估时偏小。

#### (2) 测试是否被合理处理

要么单列为 P，要么在落地步骤里显式提及，不能两头都缺。本案例属于后者，开发者在 Step 5 阶段需要确认测试动作真的被排进了执行流，而不是被清单的"省略"默默吞掉。

### 6.3 Step 3：画改造流程图

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/928442267896a7282d829f39b238599c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前两步产出的是"文字 + 表格"，适合逐条阅读但不适合一眼把握全局。Step 3 要解决的问题是"把改造后的目标态可视化"，让所有参与者——后端、前端、review 人——在同一张图上对齐对调用链与数据流的理解。流程图的价值在于它能暴露文字描述里容易被忽略的细节：调用顺序是否正确、数据形态在哪里发生变化、哪些节点是复用、哪些是新增。一张图画完，如果三个角色对"两次 Mapper 调用发生在哪里""DiffItem 在哪里组装"达成一致，后续编码就不会出现"我以为你那边算好了"的接口错位。

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
路径：imgs/18_项目开发_02：从新需求到改造方案/dd1840f0deeaa36361a8150c6cec9db0_MD5.jpg
用途：Step 3 画改造流程图的产出示例（mermaid sequence diagram）
内容：完整调用链时序图。用户→version-history.jsx（勾选两版本点对比）→services/prompt/index.ts（getDiffVersion）→PromptController（GET /api/prompt/version/diff）→PromptVersionServiceImpl（diffVersions）→DB（selectByPromptKeyAndVersion 调用两次取两个版本）→内存比较三字段组装 DiffItem→返回 PromptVersionDiffResult→前端 VersionCompareModal 渲染行级 diff。图明确：无"新建 diff 组件"节点（复用现有），不改表。
-->

第一个 why：为什么图里没有"新建 diff 组件"节点？因为 `VersionCompareModal` 是被复用的，它在前端拿到 `PromptVersionDiffResult` 后直接走已有的 `renderDiffLines` 路径渲染。这一点的教学价值在于——流程图天然会暴露"多余的节点"。如果 AI 在 Step 2 凑出了不必要的改造点，到了 Step 3 画图时就会发现"这个节点画不进调用链"或"画进去了但和现有节点功能重叠"。换言之，流程图是 Step 2 改造点清单的"反向校验"：能顺畅画进图的改造点是合理的，画不进去或导致图变得别扭的，往往是过度设计。读者可以把"画图顺畅度"当作 Step 2 成果的试金石。

第二个 why：为什么 `selectByPromptKeyAndVersion` 在图里被明确画出调用了两次？这不是装饰性细节，而是 Step 4 影响范围分析的关键依据。两次单版本查询意味着——后端 diff 接口本身没有事务性约束（两次查询之间数据若被改写，理论上会出现"版本 A 是旧值、版本 B 是新值"的不一致快照）。在低频改写的 Prompt 配置场景下这种不一致可以接受，但在高频写入的场景下就需要重新设计（例如合并为一次 SQL、加版本快照表、或加读写锁）。流程图把"调用两次"显式画出来，等于把这个潜在风险点前置到了设计阶段，而不是留到 Code Review 时才被某个资深工程师发现。这就是"画清楚"比"写清楚"更强的地方——图不会撒谎，也不会省略。

第三个 why：画图 Skill 的价值在哪里。本步骤明确提到"用 mermaid"，并且产出可以保存为 `.svg` 或代码块。安装专门的画图 Skill 后，AI 不再需要把 mermaid 语法记在脑子里，而是能把注意力集中在"调用链对不对、数据流清不清楚"这种语义层面的问题上。手工画图的成本越高，开发者越倾向于跳过这一步直接编码；Skill 把画图成本压到极低，开发者就更愿意在编码前画一张图对齐认知。工具的可达性直接决定了方法论是否会被执行——再好的步骤，如果执行成本过高，都会在实践中被悄悄绕过。

review 时的重点：

#### (1) 前端到后端的调用链是否完整往返

图里必须画出"前端拿到响应、渲染 diff 组件"这一段，而不能停在"后端返回就结束"。本案例的时序图画到了 `VersionCompareModal` 渲染行级 diff，是完整的；如果只画到 Controller 返回 JSON，就等于把前端当成"黑盒接收者"，后续做联调时容易出问题。开发者 review 时要确认图的终点是"用户看到对比结果"，而不是"接口返回 200"。

#### (2) 数据流是否清晰

两个版本的内容是如何变成 `DiffItem` 的 `valueA`/`valueB`/`changed` 字段的？比较规则是按字段、按行还是按字符？`changed` 字段的判定逻辑画进去了吗？这些细节决定了 AI 是否真的理解了 diff 的语义。本案例图里画出了"内存比较三字段组装 DiffItem"，语义层面是清楚的；如果图里只写"做 diff 然后返回"，就说明 AI 对 diff 算法的理解还停留在黑盒，Step 4 就需要重点追问比较规则与边界条件（null、空串、超长内容）的处理。

### 6.4 Step 4：说明影响范围

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/9e8fc2567d3dfb01c1a84b17b23e1d69_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

改造点列了、流程图画了，下一步要明确这次改造对现有系统的影响。这一步要解决的问题是"把改造的副作用讲清楚，并标出风险等级"。这一步最大的价值不只是列出影响项，而是让 AI 帮开发者验证脑子里的假设——很多"你以为的坑"，AI 扫一遍代码后会告诉你"这个坑不存在"。

提示词如下：

```
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
路径：imgs/18_项目开发_02：从新需求到改造方案/1df9237c8fda6ef032e5709e8dc51712_MD5.jpg
用途：Step 4 说明影响范围的产出片段示例（影响范围与风险等级表格）
内容：AI 输出的影响范围表格，含 6 项影响项：现有 GET /api/prompt/version 接口（低风险，新接口新路径原接口不动）、PromptVersionServiceImpl 现有方法（低风险，只有 log.info 无 metrics 副作用，直接调 Mapper 无需抽取内部方法）、VersionCompareModal.jsx 改造（中风险，props 结构变更是破坏性改动需同步更新调用方，renderDiffLines 保留不动）、现有测试（低风险，当前无 PromptVersion 测试）、前端依赖（低风险，无需新增依赖复用 renderDiffLines）、LONGTEXT 大内容性能（中风险，两版本 template 同时返回，本期不做大小限制上线后监控 latency）。
-->

需要特别注意第 2 条：AI 扫到了 `PromptVersionServiceImpl.getByPromptKeyAndVersion` 只有 `log.info`，没有 metrics 副作用。这和开发者脑子里想的"复用 service 方法会双倍打点"不一样，AI 帮开发者验证了这个假设是错的，这次不需要抽取 `getVersionInternal`。这正是 AI 当调研员、帮开发者验证假设的价值：你以为的坑，AI 扫一遍告诉你"这个坑不存在"。

为什么这一步要用 AI 来做而不是开发者自己想？因为开发者凭经验做的影响判断，往往是基于"通用最佳实践"而非"这个项目的真实代码"。比如"复用 service 方法会不会双倍打点"这个假设，在通用层面是对的（很多项目 service 层确实有 metrics），但在这个项目的真实代码里是错的（这个 service 只有 log）。只有 AI 实际去读 `PromptVersionServiceImpl` 的实现，才能给出准确判断。Step 4 的方法论意义就在于此——把"基于经验的假设"换成"基于代码的事实"。

review 时的重点：

#### (1) 风险等级是否合理

AI 容易把所有项标"低"或"中"。如果开发者觉得某条实际是"高"（比如某个改造确实有破坏性），要追问让 AI 重新评估。本案例中 `VersionCompareModal.jsx` 改造被标为"中"风险是合理的——props 结构变更是破坏性改动，需要同步更新调用方。

#### (2) 有没有漏的影响项

AI 列的 6 项是常见的，但具体项目可能有特殊影响（比如分布式事务、多租户隔离、灰度发布机制），这些要主动让 AI 检查。本案例 Step 7 审核时就发现漏了一条：Spring Security 配置中需要把新接口加白名单。

### 6.5 Step 5：说明准备怎么改

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/259afb789d3a9bdb77a32afb7b956c23_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

知道改什么、影响范围清楚了，下一步 AI 给出改造步骤和顺序。这一步要解决的问题是"把改造点按依赖关系排成可执行的步骤，并把关键决策点显式抽出来"。

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

注意提示词最后两行：**有分歧给方案、没分歧直接给**。这避免了 AI 为每个改造点都凑 2-3 个方案的"过度调研"。老项目改造里大多数改造点没什么方案分歧（比如加一个 DTO），不需要硬列对比。这条约束的 why 在于——AI 有"显得调研全面"的倾向，会为每个点都列对比方案，反而稀释真正需要决策的项。明确要求"没分歧就一句话带过"，能让关键决策点（如本案例的 props 结构变更）从噪音中凸显出来。

AI 的产出片段如下（改造步骤与顺序表格）：

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/ff949f8b38aece13fc76bc17b7511000_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/18_项目开发_02：从新需求到改造方案/ff949f8b38aece13fc76bc17b7511000_MD5.jpg
用途：Step 5 说明改造步骤的产出片段示例（改造步骤与顺序表格）
内容：AI 输出的改造步骤表格，7 步合计约 7 小时。步骤1 建 DTO（P01-P03，1h，createTime 用 epoch ms）；步骤2 Service（P04-P05，1.5h，null 视同空字符串用 nullToEmpty）；步骤3 Controller（P06，0.5h，异常复用 StudioException）；步骤4 前端 API（P07-P08，0.5h，可与步骤3并行）；步骤5 VersionCompareModal 改造（P09，2h，关键决策：props 从 version1/version2 改为 PromptVersionDiffResult）；步骤6 version-history 接入（P10，1h，加 loading）；步骤7 文档（P11-P12，0.5h）。
-->

合计约 7 小时。注意步骤 5 列了关键决策：`VersionCompareModal` 的 props 结构变更。这是真正有破坏性的改动（Step 4 第 3 条标的"中"风险），需要同步更新调用方。其他步骤没有分歧就一句话带过。

review 时的重点：

#### (1) 关键决策点对不对

AI 列出的决策项是不是真的需要决策。如果 AI 把"用 try-catch 还是 if-null"这种小事也列成决策点，要让它精简。本案例 AI 把决策收敛到"props 结构变更"一项是合理的——这正是真正有破坏性、需要团队拍板的点。

#### (2) 前端步骤的"前置依赖"对不对

步骤 4 写"可与步骤 3 并行（有接口 mock 就能开始）"，这个细节很关键。前端不需要等后端跑通再开始，只要接口契约定型 + mock 数据，前端就能并行。AI 容易把前端依赖写成"后端必须全部完成"，让前端工作开始得太晚。这一条直接关系到整体周期——识别出前后端可并行，往往比压单点工作量更能缩短交付时间。

### 6.6 Step 6：整合信息聚焦给人审核

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/0c580f404fc69c7f9f78ba55aded7c3d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前 5 步产出了一堆零散内容：链路图、改造点表、流程图、影响范围、改造步骤。最后让 AI 把这些整合成一份结构清晰的改造方案。这一步要解决的核心问题是"聚焦"——不是把所有信息堆上去，而是让审核者用最短时间抓住"要拍什么板、为什么这么拍"。

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

注意第 7 节"待审核的关键决策点"，这是 Step 6 的灵魂。AI 默认会平铺直叙，开发者必须明确要求它把决策点单独抽出来，不然前 5 步散落的决策点要翻几页才能看全。集中在一处，人审核效率最高。

产出片段如下（第 7 节"待审核的关键决策点"示例）：

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/d37aa07659fb759d57a45882a71f616b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/18_项目开发_02：从新需求到改造方案/d37aa07659fb759d57a45882a71f616b_MD5.jpg
用途：Step 6 整合信息的产出片段示例（第 7 节"待审核的关键决策点"表格）
内容：一张表格截图，列出整合方案中待人工拍板的 4 个关键决策点（D1-D4）。D1 null 字段处理方式（推荐 null 视同空字符串用 nullToEmpty）；D2 VersionCompareModal props 结构变更方式（推荐直接改 props 同步更新调用方）；D3 前端是否加 loading 状态（推荐加）；D4 是否监控 diff 接口 latency（建议加）。每条含推荐方案与备选。
-->

看完这一节就知道 4 个决策要拍板，不用翻整篇文档找决策点。这次的 D1-D4 和开发者预想的可能不一样：没有"是否抽取 getVersionInternal"（Step 4 已经确认不需要），没有"split 还是 unified 模式"（现有组件已经解决了这个问题）。AI 扫出真实代码后，方案里的决策点就是真实的决策点，而不是基于假设的假决策点。这也是七步法的隐性价值——通过前置的代码扫描，把"伪决策"过滤掉，让审核精力集中在真正需要人判断的点上。

review 时的重点：

#### (1) 决策点提取得齐不齐

前 5 步里所有 AI 标注"需要决策"或"建议"或给了多方案的，都要在第 7 节出现。

#### (2) 结构清不清晰

整合后的文档应该一眼能看出"链路 → 改造点 → 流程 → 影响 → 步骤 → 决策"的逻辑递进。如果读起来像散文，让 AI 重整一版。

整合完成的定稿方案文档如下（Code Block 原样保留，含完整七节）：

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

### 6.7 Step 7：人审核 + 调整 + 定稿

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/37bc941a905677e1d4845a24d2b2fad4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面六步都是 AI 跑的，Step 7 是人主导：开发者审核、调整、定稿。审核要走三个层次。

#### (1) 第一层：决策点拍板

打开第 6 步整合的方案文档，先看第 7 节"待审核的关键决策点"。每条决策给出开发者的判断：

- D1（null 字段处理）：null 视同空字符串。理由：与历史数据兼容，不想在 UI 上区分"字段缺失"和"字段为空"。
- D2（props 变更方式）：直接改 props，同步更新调用方。理由：兼容层增加长期维护负担，改造点不多直接改干净。
- D3（前端 loading）：是。理由：diff 接口在 LONGTEXT 时可能慢，没 loading 体验差。
- D4（监控 latency）：是。理由：观测一段时间再决定要不要加大小限制。

#### (2) 第二层：链路、改造点、影响范围审核

需要逐节看：

- 链路有没有漏（特别是前端节点）。
- 改造点有没有漏（特别是文档、i18n 这种容易漏的）。
- 影响范围有没有漏（特别是分布式事务、多租户、灰度等项目特定的影响）。

#### (3) 第三层：把发现的问题反馈给 AI 调整

review 中开发者会发现这样的问题：

- P10 修改 `version-history.jsx` 漏了一个细节：用户选中两条版本后要禁用其他版本的勾选（防止选超过 2 个）。
- 影响范围漏了：Spring Security 配置中需要把新接口加白名单。
- 工作量合计 7 小时太乐观了，`VersionCompareModal` 改造涉及状态联动，实际可能就得 3 小时。

把所有发现汇总反馈给 AI 让它调整：

```
我审核了方案文档，以下几点需要调整：

1. P10 补充细节：用户在版本列表选中两条后，要禁用其他版本的勾选
2. 影响范围漏了：Spring Security 配置中需要把 GET diff 接口加白名单（新增 P13 改造点）
3. 第 7 节决策全部拍板：D1 null 视同空字符串，D2 直接改 props，D3 加 loading，D4 加监控

更新 prompt-version-diff-solution.md，把这些反馈整合进去。

特别注意第 7 节的决策点要全部更新成最终决策（不再是"待审核"）。
```

AI 调整完，开发者再 review 一遍。多数情况下两轮就够。

#### (4) 为什么 Step 7 这么重要

因为前 6 步 AI 给的是"基于代码反推 + 通用最佳实践"的输出，有些东西 AI 看不到：团队的隐性约定、某个老接口的副作用、某个配置文件的特殊处理、产品历史决策的延续。这些必须人 review 补进去。

省下 Step 7 直接进开发，到时候要补回来。而且是以"代码改完发现要返工"的方式补，代价大十倍。Step 7 的本质是把"团队脑子里的隐性知识"显式地注入到方案文档里，让 AI 的调研和团队的经验在文档层面合流。

## 7. 文档维护更新：把改造方案沉淀回 docs/

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/d551be12709d5d68f0be397009165153_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 7.1 为什么要回灌 docs/

七步跑完，开发者手上有一份审核过的 `prompt-version-diff-solution.md`，但这一轮的产出不只这份文档。审核过程中开发者和 AI 反复交流，产生了一堆新信息：新发现的边界、新的老项目约束、新的项目级判断。

这些新信息要回灌到 docs/，否则下次类似改造又要从头摸索。docs/ 资产不是一次性产出，是每次改造都被验证、被丰富的活资产，这一篇是"活资产闭环"的落地。

### 7.2 回灌提示词

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

4. CLAUDE.md：

   有没有新发现的"老项目约束"应该补进去。具体判断：

   - 这条约束是项目级的（影响所有未来类似改造）→ 写进 CLAUDE.md
   - 这条约束只是这一次的特殊处理 → 留在 solution.md 就行

   比如"复用现有 Service 方法前先确认有无 metrics 副作用"是项目级约束，

   应写进 CLAUDE.md。"VersionCompareModal props 改造方式"是这次的

   特殊处理，留在 solution.md 即可。

输出每份文件的改动 diff 给我 review。
```

### 7.3 CLAUDE.md 判断标准

回灌时最需要判断力的是 CLAUDE.md——不是所有发现都该写进去。判断标准是"作用域"：

#### (1) 项目级约束 → 写进 CLAUDE.md

如果这条约束影响所有未来类似改造（比如"复用现有 Service 方法前先确认有无 metrics 副作用"），它是项目级的，应该写进 CLAUDE.md，让 AI 在后续每次改造都自觉遵守。

#### (2) 一次性特殊处理 → 留在 solution.md

如果这条约束只是这一次的特殊处理（比如"VersionCompareModal props 改造方式"），留在 solution.md 即可，不要污染 CLAUDE.md 的全局视野。

这个判断标准本身就是一种方法论——区分"通用约定"和"个案决策"，让 CLAUDE.md 保持精炼，只承载真正影响全局的约束。

### 7.4 与 docs-auto-sync Skill 的呼应

这一步呼应本系列前面挖的 docs-auto-sync Skill——可以直接调用 SKILL 跑这一步，效果一样。跑完这一步，开发者手里所有 docs/ 资产被这一轮深度思考反向丰富了一轮。"docs/ 是项目脑图"在这一刻被验证 + 升级：脑图不是一次性产出，是每次改造都被丰富的活资产。

## 8. 最终产出与小结

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/13b42b451af3a07020a4cd3adf5f5245_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 8.1 最终产出清单

整轮跑完开发者手上有：

#### (1) 主产出

`docs/requirements/prompt-version-diff-solution.md`（审核定稿 + 决策落定）

#### (2) 资产同步产出

- a. `docs/api-list.md`：加了新接口（标"开发中"）
- b. `docs/data-model.md`：加了三个新 DTO
- c. `docs/requirements/prompt-version-diff.md`：补了审核新发现的边界
- d. `CLAUDE.md`：加了一条项目级约束（如适用）
- e. `docs/requirements/prompt-version-diff-impact.md`：链路 + 改造点 + 影响 + 步骤的详细文档（作为 solution.md 的引用源）

### 8.2 时间成本对比

整个七步 + 文档维护更新跑下来 60-90 分钟，比手写方案文档省一天，且文档质量比手写高得多——有 AI 调研员的扫描 + 开发者审核的深度。本系列下一篇拿这份方案文档动手改后端，每个改造点对应明确的代码改动，不用再决策、不用再调研、不用再纠结。

### 8.3 核心要点回顾

这一篇的核心：让 AI 把改造想透，开发者审核拍板，60 分钟出一份方案文档。七步走：

1. 摸涉及的链路（含前端）
2. 列改造点（后端 + 前端 + 测试 + 文档）
3. 画改造流程图
4. 说影响范围与风险
5. 说改造步骤与顺序
6. 整合信息聚焦给人审核
7. 开发者审核 + 反馈调整 + 定稿

加一步文档维护更新：把这一轮的新信息回灌到所有 docs/。

### 8.4 docs/ 是活资产

docs/ 资产不是一次性产出，是每次改造都被验证、被丰富的活资产，这一篇是"活资产闭环"的第一次落地。后面两篇做完代码改造后，资产还会再被更新一轮。

本系列下一篇带着这份方案文档动手写代码。先做后端：建 DTO、加 Service 方法、加 Controller 接口、补 Characterization Test、跑通集成测试。

## 9. 思考

<img src="imgs/aicmigr-18-dev-02-new-requirement-to-plan/9e2b35bd4b66defb7c213fcd051c81f4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 9.1 两个反思问题

#### (1) 动手前的分析习惯

开发者最近一次做改造，动手前有没有画过改造流程图、列过改造点、评估过影响范围？如果只是在脑子里大概有数就直接动手了，那次改造踩了几个本来可以提前发现的坑？

#### (2) 七步法中哪一步价值最大

在这一篇的七步法里，哪一步对团队价值最大？是 Step 1（摸链路）、Step 4（说影响）、还是 Step 6（整合给人审核）？为什么？读者可以结合自己团队的实际场景，思考每一步的边际价值——对于测试覆盖完善的项目，Step 4 的影响分析可能价值最高；对于前后端分离明确的项目，Step 1 的链路扫描可能价值最高；对于多人协作的大型改造，Step 6 的决策点整合可能价值最高。
