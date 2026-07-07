---
title: 传统项目迁AI 17：项目开发 - 拆解新需求
author: fangkun119
date: 2026-07-04 17:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-17-dev-01-break-down-new-requirements/cover.jpg
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
aicmigr-17-dev-01-break-down-new-requirements
传统项目迁AI 17：项目开发 - 拆解新需求
-->

## 1. 全文导读地图

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/ed8ade3781b52d2930dcde0677ec47a2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是"项目开发"部分的开篇，主题是"如何在老项目场景下用 AI 把一个一句话需求拆成一份能让后端直接写代码、测试直接补用例、产品直接 review 的需求文档"。改写后分为两部分：第一部分是方法论提炼（需求文档六维体系 + AI 能力地图 + 三步走 + Check List），第二部分是实战演示（以 Spring AI Alibaba Admin 的 Prompt 版本对比需求为线索，复现三步走全程，给出可复用提示词与完整需求文档定稿）。

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/eb5814d66c797b6e6de9e8b5bf780cac_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    START["一句话需求砸过来"] --\> PART1["第一部分 方法论提炼<br>速查手册 + Check List"]
    START --\> PART2["第二部分 实战演示<br>三步走 + 提示词 + 定稿"]

    PART1 --\> M1["2.1 为什么需求文档质量不稳定"]
    PART1 --\> M2["2.2 需求文档六维体系"]
    PART1 --\> M3["2.3 AI 在六维上的能力地图"]
    PART1 --\> M4["2.4 三步走：AI 草稿 → 人判断 → AI 定稿"]
    PART1 --\> M5["2.5 最常见翻车点：把草稿当定稿"]
    PART1 --\> M6["2.6 拆需求阶段 Check List"]

    PART2 --\> S1["3.1 实战准备：Prompt 版本对比需求缘起"]
    PART2 --\> S2["3.2 Step 1：让 AI 出六维草稿"]
    PART2 --\> S3["3.3 Step 2：人补三个关键判断"]
    PART2 --\> S4["3.4 Step 3：让 AI 出定稿"]
    PART2 --\> S5["3.5 review 重点与产出物回顾"]

    M6 -. 项目阶段查阅 .-> S1
    S4 --\> END["一份能直接写代码的需求文档"]
    END --\> SUMMARY["4. 小结与思考"]
    SUMMARY --\> APPENDIX["5. 附录：完整需求文档定稿"]
-->

读者路径建议：

| 读者类型 | 推荐路径 |
|---------|---------|
| 初学 AI 编程工程师 | 顺读全文，重点看第二部分三步走的提示词、决策和 review 重点 |
| 熟练 AI 编程工程师 | 先读第一部分 2.2 六维体系 + 2.6 Check List，按需跳到第二部分查阅定稿文档 |

## 2. 第一部分 方法论提炼

### 2.1 为什么需求文档质量不稳定

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/1a3b20cfcb26b40555ca02699d0ab7b5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

leader 丢过来一句话——"给 Prompt 加个版本对比功能"，这种话本身写不了代码，必须拆。同样是拆需求，不同工程师交出来的文档质量差距巨大：有人交出接口契约清晰、边界场景齐全、老项目约束标到位的版本；有人交出"加个版本对比 + 三行接口签名"就完了。这种差异不是技术能力问题，而是大家都在凭经验做事，没有可重复的体系——状态好就写得全，状态差就漏一半。

#### (1) 三种最常见的翻车

| 翻车类型 | 典型表现 | 后果 |
|--\>------|---------|------|
| 翻车一：写得太抽象 | "加个版本对比功能"，接口签名不清，错误码、返回结构全靠开发猜 | 开发反复找产品确认，每次半天 |
| 翻车二：漏边界场景 | Happy path 写得完整，"版本不存在、两版本相同、内容超长"全没列 | 开发写完测试才发现要补，文档反复改 |
| 翻车三：忘了老项目约束 | 工程师按新需求拆，忘了"promptKey 已被 SDK 客户依赖、接口路径不能改" | 改造做到一半踩了禁区，整个返工 |

#### (2) 共同根因

三种翻车背后是同一个问题：**没有可重复的体系，每次靠经验拍脑袋**。下面给出的六维体系就是用来解决这个问题的。

### 2.2 需求文档六维体系

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/99f44f7e79ec85b4ad03c9f56d53076d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把需求文档拆成六个维度，每次拆需求都按这六个维度跑一遍。这不是发明新方法论，而是把工程师本来就在做的事结构化——好处是每次产出格式一致、不漏维度、团队 review 起来快，而这恰好是 AI 最擅长的事情。

> 提示：可以考虑把下面的六个维度写成 Skill 落地下来，作为团队统一的需求拆解工具。

#### (1) 六个维度对照表

| # | 维度 | 含义 | 是否老项目独有 | 示例（Prompt 版本对比） |
|---|------|------|--------------|----------------------|
| ① | 业务目标 | 一句话说清楚解决什么问题、给谁用 | 否 | 让用户快速看出 Prompt 两个版本之间的差异，定位是哪次改动引入了问题 |
| ② | 用户场景 | 典型使用场景 + 当前痛点 + 期望体验 | 否 | 工程师调试 Prompt 时切换版本看效果，目前只能各打开一次肉眼对比，期望一键对比 |
| ③ | 接口契约 | 方法、路径、入参、返回、错误码 | 否 | 开发拿到这一节就能写代码 |
| ④ | 边界场景清单 | 异常情况和它们的预期处理 | 否 | 空值、超长、不存在、并发、跨范围……每条都要有"预期行为" |
| ⑤ | 老项目约束 | CLAUDE.md 禁区和历史包袱里和这个需求相关的条目 | **是（老项目独有）** | 新增接口要避开禁区、新功能要兼容历史包袱 |
| ⑥ | 不在这次范围里的事 | 明确这次不做什么 | 否 | "对比两个版本"扩展开可能变成"对比 N 版本""对比附评分""导出 diff"，不写"不做这些"AI 就会顺手做 |

#### (2) 六维合起来的效果

六个维度合起来就是一份完整的文档：开发拿了能直接写代码、测试拿了能直接补用例、产品拿了能直接 review。下面看看 AI 在这六维上分别能干到什么程度。

### 2.3 AI 在六维上的能力地图与分工心法

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/bc10dfd339a67a7c62b7630ca20e2ba0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这是本篇的核心。**AI 不是替你拆需求，而是和你分工**——看清 AI 在每个维度上的真实能力，分工才合理。

#### (1) AI 能力三档地图

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/88b7e272ece1f07bbde24a31b5e7e2bb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/17_项目开发_01：拆解新需求/88b7e272ece1f07bbde24a31b5e7e2bb_MD5.jpg
用途：可视化展示 AI 在六个维度上的能力评分（强/中/弱三档），帮助读者快速理解人机分工边界
内容：雷达图或分档柱状图。强档（85+ 分）：接口契约、边界场景的技术性 edge case、老项目约束；中档（60 分左右）：业务目标、用户场景；弱档（30 分以下）：边界场景的产品决策、不在这次范围里的事
-->

##### ① AI 强的维度（85+ 分）

| 维度 | AI 强在哪里 | 人手写的差距 |
|------|-----------|------------|
| 接口契约 | 基于 docs/api-list.md 和现有代码，能写出和现有接口风格一致的契约；错误码沿用项目惯例、统一返回结构对、参数命名风格一致 | 人手写要查现有接口模仿，AI 30 秒搞定 |
| 边界场景的技术性 edge case | 404、空值、超长、并发、不存在……这些技术 edge case AI 列得很全 | AI 比人想得全，因为 AI 不会疲劳 |
| 老项目约束 | 基于 CLAUDE.md 和现有代码反推 | 人凭脑子记不住 CLAUDE.md 全文，AI 每次启动都加载全部内容 |

##### ② AI 中等的维度（60 分左右）

| 维度 | AI 表现 | 为什么不能直接定稿 |
|------|--------|-----------------|
| 业务目标 | 能从代码反推一个版本，但容易写偏方向 | 例如 AI 写"帮工程师调试不同版本"，但真实目标是"给运营看 Prompt 演进"，方向就错了。草稿可做起点，方向必须人定 |
| 用户场景 | 能根据接口推测使用场景，但真实痛点 AI 不知道 | 草稿可做起点，具体痛点要人补 |

##### ③ AI 弱的维度（30 分以下）

| 维度 | 为什么 AI 弱 |
|------|------------|
| 边界场景的产品决策 | AI 能列"内容超长怎么办"这种场景，但这种场景怎么处理是产品决策：截断？返回 400？提示用户？AI 不知道产品判断 |
| 不在这次范围里的事 | 完全是产品判断。AI 默认会写"做对比"含糊一片，不知道优先级和资源 |

#### (2) 分工心法

**一句话**：AI 帮你做 AI 强的（70%），人补 AI 弱的（30%），整体出来 90+。下面三步走，教你怎么跑这个分工。

### 2.4 三步走：AI 出草稿 → 人补判断 → AI 出定稿

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/a61ef35745e48daa476a79c2b1636eba_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 三步走流程

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/5e0f229ac71f33bbd60794cafe89691a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    S1["Step 1<br>AI 出六维草稿<br>读 docs/ + CLAUDE.md + 现有代码"]
    S1 --\>|草稿约 5 分钟| S2["Step 2<br>人补三个关键判断<br>业务方向 + 产品决策 + 改造范围"]
    S2 --\>|判断约 10 分钟| S3["Step 3<br>AI 整理定稿<br>格式化为正式技术文档"]
    S3 --\>|整理约 5 分钟| OUT["20 分钟产出一份 80+ 分需求文档"]
-->

#### (2) 时间预算表

| 步骤 | 用时 | 主体产出 |
|------|------|---------|
| Step 1 AI 出草稿 | 5 分钟 | 六维结构齐全的草稿（覆盖 70%） |
| Step 2 人补三个判断 | 10 分钟 | 业务方向修正 + 边界产品决策 + 范围决策 |
| Step 3 AI 整理定稿 | 5 分钟 | 正式技术文档（表格化、结构化） |
| 合计 | 20 分钟 | 比手写 PRD 省两个小时 |

#### (3) Step 2 只补三个判断的原因

其他维度（接口契约、技术边界、老项目约束）AI 写的直接用。**只补这三个判断**的原因如前面能力地图所示：AI 在数据和代码层面强，在业务判断层面弱。这三个判断都是业务判断，AI 永远做不了。

### 2.5 最常见翻车点：把 AI 草稿当定稿

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/5f299fdaaaca7a5efd600f3425531862_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

老项目拆需求最大的翻车点是：AI 草稿覆盖 70% 看起来挺全，工程师跳过 Step 2 直接用。为什么这是致命错误？

#### (1) 翻车链路

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/3444a390e463e7850a742043b4625280_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart LR
    A["AI 草稿覆盖 70%<br>集中在 AI 强的维度"] --\> B["跳过 Step 2"]
    B --\> C["AI 弱的维度凭默认理解填"]
    C --\> D["业务方向偏 / 范围模糊 / 产品决策错位"]
    D --\> E["改造做完<br>业务方说：不是我要的"]
-->

#### (2) 关键数字

记住一个数字：**AI 帮你做到 70 分，人 review 补的 30 分是这份文档从能用到好用的关键**。

#### (3) 与系列第 15 篇的心法呼应

系列第 15 篇讲过"AI 写测试的隐性偏差"——AI 用业务直觉补测试断言，错；本篇讲 AI 用代码默认填业务目标和产品决策，错。两篇背后是同一个心法：**AI 在数据和代码层面强，在业务判断层面弱**。和 AI 分工，不要把 AI 当定稿——这是本篇最硬的一句话。

### 2.6 拆需求阶段 Check List

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/cc868d62d3f4aaefd5ac030f76592555_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

下面这份 Check List 供项目拆需求阶段快速查阅，可直接复制到项目内部使用。

#### (1) 启动前准备

- [ ] 已拿到一句话需求（来源：leader / PM / 业务方）
- [ ] 项目已有 docs/ 资产（api-list.md、data-model.md、CLAUDE.md）— 没有则先回到第 6-12 篇补齐
- [ ] 已识别需求的三个特点：场景真实、改造路径清晰、可落地（可选：可提 PR）

#### (2) Step 1 提示词检查

- [ ] 强制 AI 读 docs/ 下所有资产（特别是 api-list.md、data-model.md、CLAUDE.md）
- [ ] 强制 AI 扫现有相关代码
- [ ] 单独列出"老项目约束"维度
- [ ] 边界场景要求至少 8 条 + 每条标"预期行为"或"待产品决策"
- [ ] "不在这次范围里"AI 先列候选，人后定
- [ ] 指定输出路径：docs/requirements/{feature-name}.md

#### (3) Step 2 三判断检查

- [ ] 业务目标方向是否需要修正（AI 草稿默认偏哪个方向，真实目标是哪个）
- [ ] 每条"待产品决策"的边界场景是否给出决策
- [ ] "不在这次范围里"候选清单是否完成砍掉/留下/推迟决策

#### (4) Step 3 定稿检查

- [ ] 一句话总结放最前
- [ ] 接口契约用表格（方法 + 路径 + 入参 + 返回 + 错误码）
- [ ] 边界场景用表格（场景 + 预期行为 + 依据）
- [ ] 老项目约束单独一节，每条标 CLAUDE.md 来源
- [ ] 不在这次范围里的事单独一节

#### (5) 出文档前最后自检

- [ ] AI 强的维度（接口契约、技术边界、老项目约束）是否完整且对齐项目风格
- [ ] AI 弱的维度（业务目标、产品决策、改造范围）是否经过人补判断
- [ ] 文档是否可作为下一阶段（拆改造方案）的输入

## 3. 第二部分 实战演示

### 3.1 实战准备：Prompt 版本对比需求缘起

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/55b0e1ec4cd56abac5307fe532ee0ff0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 为什么直接给需求，不做铺垫

真实业务场景就是这样：leader 或 PM 找到你，开口就是一句话——"给 Prompt 加个版本对比功能"，然后人就走了。第一反应通常是懵的：什么是版本对比？对比什么？要做到什么程度？给谁用？这种"被一句话砸过来、自己心里没底"的状态，正是本篇要解决的真实场景。

#### (2) 需求说明

Spring AI Alibaba Admin 里已经有 Prompt 版本管理：

| 现有接口 | 用途 |
|---------|------|
| `POST /api/prompt/version` | 创建版本 |
| `GET /api/prompt/version` | 取单个版本 |

**缺口**：两个版本之间没有 diff 功能。用户想知道 v3 和 v5 改了什么，只能各打开一次自己肉眼对比。

#### (3) 这个需求的三个特点

| 特点 | 说明 |
|------|------|
| 场景真实 | 版本对比是版本管理系统的常见缺口，几乎每个有版本概念的系统都会遇到这种需求 |
| 改造路径清晰 | 一个新接口（`GET /api/prompt/version/diff`）+ 一段 diff 逻辑 + 前端一个对比按钮和展示组件，涉及后端、前端、测试、文档，是一个完整闭环 |
| 真的可以提 PR | 对应仓库目前确实没有这个功能，做完是真能提给 spring-ai-alibaba 项目 |

#### (4) 本系列五篇的连贯安排

| 篇号 | 主题 |
|------|------|
| 第 17 篇（本篇） | 拆需求 |
| 第 18 篇 | 拆方案 |
| 第 19 篇 | 改后端 |
| 第 20 篇 | 改前端 |
| 第 21 篇 | 实操课，串起来 |

### 3.2 Step 1：让 AI 出六维草稿

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/e18e2923f0e9c7ea5fd2d3d84f73218f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第一步把 AI 强的维度先压满。提示词的关键是把"读 docs/、读 CLAUDE.md、读现有代码"这三件事强制让 AI 做。

#### (1) 提示词

```text
我有一个新需求：给 Prompt 管理加版本对比功能。

GET /api/prompt/version/diff?promptKey=xxx&versionA=v3&versionB=v5

返回两个版本的 diff 结果。

读 docs/ 下的所有资产（特别是 api-list.md、data-model.md、CLAUDE.md），
再扫一下代码里现有 Prompt 版本相关的实现（PromptVersionController、
PromptService、PromptVersionEntity），按以下六个维度给我写需求文档草稿：

1. 业务目标（一句话）
2. 用户场景（典型使用场景 + 当前痛点）
3. 接口契约（方法、路径、入参、返回、错误码，对齐项目现有风格）
4. 边界场景清单（至少列 8 条 edge case，包括从现有代码反推的，
   每条标"待产品决策"或基于现有代码的预期行为）
5. 老项目约束（CLAUDE.md 禁区和历史包袱里和这个需求相关的，
   每条标 CLAUDE.md 来源）
6. 不在这次范围里的事（先列你能想到的候选，最终我来定）

输出 markdown，保存到 docs/requirements/prompt-version-diff.md。
```

#### (2) 关键约束

| 约束 | 作用 |
|------|------|
| 强制读 docs/ + CLAUDE.md + 现有代码 | 把老项目隐性知识显性化 |
| 单独列"老项目约束"维度 | 强迫 AI 显式输出禁区和历史包袱 |
| 边界场景至少 8 条 + 每条标"预期行为"或"待产品决策" | 防止 AI 偷懒只列 happy path |
| "不在这次范围里"AI 先列候选，人后定 | AI 提供候选，决策权在人 |

#### (3) 真实产出节选

产出是一份六维结构齐全的草稿，覆盖 70% 内容。在 Spring AI Alibaba Admin 上跑了一遍，AI 真实跑出来的草稿关键两段大概是这样：

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/af2f4c2ce042cd2fc21ae3b08729688d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字"> 

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/911f81fe5aa5a973127bce2df224f2f0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/17_项目开发_01：拆解新需求/af2f4c2ce042cd2fc21ae3b08729688d_MD5.jpg
用途：展示 AI 真实产出的草稿片段（老项目约束 + 边界场景表格）
内容：表格形式的草稿截图。第一段是"老项目约束"维度，每条约束都标注了 CLAUDE.md 来源（如"ORM 混用""业务 ID vs 自增主键""逻辑删除""统一返回结构"），并说明对本需求的影响；第二段是"边界场景"维度，列出了从代码反推的技术 edge case（如 JPA @Table 字段为 null、LONGTEXT 字段超大、MySQL collation 大小写敏感性），每条标"基于代码推断"或"待产品决策"
-->

<!--
图片内容说明
路径：imgs/17_项目开发_01：拆解新需求/911f81fe5aa5a973127bce2df224f2f0_MD5.jpg
用途：展示 AI 真实产出的草稿片段（接口契约或返回结构部分）
内容：草稿截图，展示接口契约表格或返回结构的 JSON 示例（PromptVersionDiffResult 的字段定义，包括 promptKey、versionA、versionB、diffs 等字段）
-->

#### (4) 几个值得注意的细节

##### ① 老项目约束节都标了 CLAUDE.md 来源

AI 不是泛泛说要遵守约束，而是把每一条具体到哪一节、影响什么、怎么对齐。这种细节，工程师手写时 90% 会忘。

##### ② 边界场景实际列了 12 条（截图只截了 5 条）

AI 不光列了基础场景（不存在、相同），还列出了只有读了代码才知道的场景：

| 编号 | 场景 | 来源 |
|------|------|------|
| E04 | JPA `@Table` 的字段为 null | 从 entity 类反推 |
| E10 | LONGTEXT 字段超大 | 从 SQL 建表语句反推 |
| E11 | MySQL collation 大小写敏感性 | 从 DB 配置反推 |

这些场景 AI 是从 entity 类、SQL 建表语句、DB 配置反推的，人凭脑子想想不到这么细。

##### ③ 每条边界都标了"基于代码推断"或"待产品决策"

- **基于代码推断**的，AI 给出明确预期行为
- **待产品决策**的，AI 老老实实标出来等人定

这种透明度比 AI 自作主张拍板要好得多。

### 3.3 Step 2：人补三个关键判断

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/d92ecf2f0cb777861f351bdeee9283d9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

草稿覆盖了 70%，剩下 30% 必须人补。**只 review 三个判断**：

| 判断 | 为什么必须人补 |
|------|--------------|
| 业务目标的方向 | AI 在业务判断层面弱，方向必须人定 |
| 边界场景的产品决策 | 这些是产品决策，AI 不知道产品判断 |
| 不在这次范围里的事 | 完全是产品判断，AI 不知道优先级和资源 |

其他维度（接口契约、技术边界、老项目约束）AI 写的直接用。

#### (1) 判断一：业务目标修正

AI 草稿默认偏"工程师 review 自己的修改"。但对照三个场景里有两个是团队场景（线上效果回溯、多人协作合并），真实目标更偏**团队多人协作下的 Prompt 演进追溯**。

| 维度 | AI 草稿默认 | 修正后 |
|------|-----------|-------|
| 业务目标 | 工程师 review 自己的修改 | 团队多人协作下的 Prompt 演进追溯 |
| 对 diff 结果的影响 | 只返回内容 | 返回元信息（创建时间、状态）+ 内容 |

#### (2) 判断二：5 条待决策边界场景

| 编号 | 场景 | 最终决策 |
|------|------|---------|
| E04 | template 为 null | null 视同空字符串 |
| E07 | 软删除的 Prompt | 允许查 diff |
| E10 | LONGTEXT 超大 | 本期不做大小限制 |
| E11 | 版本号大小写 | 不在应用层 toLowerCase（DB 默认不敏感） |
| E12 | 高并发 | 不加缓存 |

#### (3) 判断三：8 条候选范围决策

| 决策类型 | 候选项 |
|---------|-------|
| 砍掉（7 条） | 后端生成 unified diff、跨 promptKey 对比、N 版本对比、diff 缓存、versionDescription diff、权限控制等 |
| 留到下期（2 条） | diff 导出、一键比对上一版 |

判断都做完后，把决策反馈给 AI 让它整理出定稿。

### 3.4 Step 3：让 AI 出定稿

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/e413e08331cf3f663cba404406f588dd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把 Step 2 的三个判断打包反馈给 AI，让它一次性更新到文档里。

#### (1) 提示词

```text
我对 docs/requirements/prompt-version-diff.md 做了三个判断，
按下面的内容更新文档：

业务目标修正：从"工程师 review 自己的修改"改成"团队多人协作下的
Prompt 演进追溯"。diff 结果要返回 versionA/versionB 的元信息
（创建时间、状态）不只是内容。

边界场景的产品决策（每条更新到表格里替换"待产品决策"）：
- E04（template 为 null）：null 视同空字符串
- E07（已软删除）：允许查 diff
- E10（LONGTEXT 超大）：本期不做大小限制
- E11（版本号大小写）：不在应用层 toLowerCase
- E12（高并发）：本期不加缓存

不在这次范围里的事最终决策：
- 砍掉：后端生成 unified diff、跨 promptKey 对比、N 版本对比、
  diff 缓存、versionDescription diff、权限控制
- 留到下期：diff 导出、一键比对上一版

整理为正式技术文档：
- 一句话总结放最前
- 接口契约用表格（方法 + 路径 + 入参 + 返回 + 错误码）
- 边界场景用表格（场景 + 预期行为 + 依据）
- 老项目约束单独一节，每条标 CLAUDE.md 来源
- 不在这次范围里的事单独一节

保存到原文件。
```

这个提示词有点长，但内容是模板化的，可以在自己的项目中复用。整个三步跑下来 20 分钟：Step 1 让 AI 跑五分钟、Step 2 做三个判断十分钟、Step 3 让 AI 整理五分钟，比手写 PRD 省两个小时。

#### (2) 定稿产出预览

跑完 Step 3，会得到一份结构标准的需求文档 `docs/requirements/prompt-version-diff.md`，包含一句话总结、业务目标、用户场景、接口契约（含基本信息/入参/返回结构/错误码）、边界场景、老项目约束、不在这次范围里等七节。完整内容见后文 `5. 附录：完整需求文档定稿`。

> 提示：可以把附录的文档复制到本地 markdown 文件查看，应该可以看到不少内容。

### 3.5 review 重点与产出物回顾

#### (1) review 三问

| 问题 | 通过标准 |
|------|---------|
| 接口契约能直接给后端用吗？ | 方法/路径/入参/返回/错误码齐全，风格与现有接口一致 |
| 边界场景每条都有明确的预期行为吗？ | 每条都有"预期行为"，且依据是"代码推断"或"产品决策"标清 |
| 老项目约束每条都有 CLAUDE.md 来源标注吗？ | 每条都映射到 CLAUDE.md 的具体条目 |

#### (2) 关键产出物

| 产出物 | 路径 | 作用 |
|-------|------|------|
| 需求文档定稿 | `docs/requirements/prompt-version-diff.md`（完整内容见 `5. 附录`） | 第 18 篇拆改造方案的输入 |
| 三段提示词模板 | 见 3.2、3.4 节 Code Block | 可在团队内复用，下次拆需求直接套 |
| 三判断决策记录 | 见 3.3 节三张表 | 留痕，后续追溯业务决策依据 |

这份文档接下来就是第 18 篇的输入——下一篇拿着这份需求文档，开始拆改造方案：涉及哪些代码、改哪些地方、用什么 diff 库、改造任务怎么排，把"做什么"翻译成"怎么做"。

## 4. 小结与思考

<img src="imgs/aicmigr-17-dev-01-break-down-new-requirements/79c203858e1c8d3ec4c9a2d62697a1c0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 4.1 核心要点回顾

本篇内容比较多，也比较繁琐，其实就想强调一个观点：到了改造的深水区，需要进一步深入介入，这一步千万不能偷懒——这一步做的好坏与否，是老项目改造效果好与不好的最重要原因。

| 维度 | 核心要点 |
|------|---------|
| 六维体系 | 业务目标、用户场景、接口契约、边界场景、老项目约束、不在这次范围里的事 |
| AI 能力地图 | 接口契约、技术边界、老项目约束 AI 强（85+）；业务目标、用户场景 AI 中（60）；产品决策、改造范围 AI 弱（30 以下） |
| 三步走 | AI 出 70 分草稿（基于 docs/ + CLAUDE.md + 现有代码）→ 人补三个关键判断（业务方向 + 产品决策 + 范围）→ AI 出定稿。全程 20 分钟 |
| 最易翻车点 | 把 AI 草稿当定稿。AI 强的部分覆盖 70%，但漏的 30% 才是文档从能用到好用的关键，必须人补 |

### 4.2 思考

#### (1) 最让你头疼的维度

你拿过的需求文档里最让你头疼的是哪个维度？业务目标、边界场景、还是不做的事？这个维度让你头疼的原因是写的人没写清楚，还是你没追问清楚？

#### (2) 省下来的 8 小时怎么花

想象你接下来一个月要写 5 份需求文档，AI 帮你把每份从 2 小时压到 20 分钟，省下 8 个多小时。这 8 小时你会拿来做什么——多做一个需求、多做 review、还是其他？

## 5. 附录：完整需求文档定稿

本附录是 Step 3 跑完产出的 `docs/requirements/prompt-version-diff.md` 完整内容，作为参考模板与第 18 篇的输入。

````text
# Prompt 版本对比（Diff）

> 状态：正式
> 创建时间：2026-04-27
> 关联接口：`GET /api/prompt/version/diff`

**一句话总结：** 在团队多人协作下，支持任意两个 Prompt 版本的内容与元信息对比，用于演进追溯和发布前 review。

---

## 1. 业务目标

支持团队多人协作场景下的 Prompt 演进追溯：任意两个版本之间的 `template`、`variables`、`modelConfig` 内容差异，以及版本元信息（创建时间、状态、创建者），均可通过一次接口调用获取，无需手动切换版本页面对比。

---

## 2. 用户场景

**场景 A — 发布前 review**

工程师修改了 `customer-service` v4 → v5，准备发布到生产。PM 需要核对改了哪些地方，目前只能在两个版本详情页之间反复切换，手动比对 `template` 内容。

**场景 B — 问题回溯**

线上 Prompt `order-summary` 在 v7 之后效果下滑，工程师需要逐版本比对，找到是哪一次修改引入了问题。当前没有 diff 视图，只能在本地手动 diff。

**场景 C — 多人协作冲突解决**

两名工程师各自基于 v3 创建了 v4-alice 和 v4-bob，需要对比差异后决定合并策略。返回的版本元信息（创建时间、状态）帮助团队判断哪个版本更新、哪个已发布。

**当前痛点：**

- `GET /api/prompt/version` 每次只返回单个版本，没有跨版本比较视图
- `PromptVersionDO.previousVersion` 字段已存在（设计时预留 diff 场景），但从未被任何接口消费
- 版本列表接口不返回 `template` 内容，用户须逐个请求详情再手动比对

---

## 3. 接口契约

### 基本信息

| 项 | 值 |
| --- | --- |
| 方法 | `GET` |
| 路径 | `/api/prompt/version/diff` |
| 鉴权 | `Authorization: Bearer <token>`（与其他 `/api/prompt/*` 接口一致） |
| 返回格式 | `Result<PromptVersionDiffResult>` |

### 入参

| 参数名 | 类型 | 必填 | 约束 | 说明 |
| --- | --- | --- | --- | --- |
| `promptKey` | `String` | ✅ | `@NotBlank`；`^[a-zA-Z0-9_-]+$`；长度 1–255 | 与 `PromptCreateRequest.promptKey` 校验规则一致 |
| `versionA` | `String` | ✅ | `@NotBlank`；`^[a-zA-Z0-9._-]+$`；长度 1–32 | 与 `PromptVersionCreateRequest.version` 校验规则一致 |
| `versionB` | `String` | ✅ | `@NotBlank`；`^[a-zA-Z0-9._-]+$`；长度 1–32 | 同上 |

**示例：**

```http
GET /api/prompt/version/diff?promptKey=customer-service&versionA=v3&versionB=v5
```

### 返回结构

**顶层：** `Result<PromptVersionDiffResult>`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | `Integer` | 200 = 成功 |
| `message` | `String` | `"success"` 或错误描述 |
| `data` | `PromptVersionDiffResult` | diff 结果 |

**`PromptVersionDiffResult`：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `promptKey` | `String` | 被比较的 Prompt Key |
| `versionA` | `VersionMeta` | 版本 A 元信息 |
| `versionB` | `VersionMeta` | 版本 B 元信息 |
| `diffs` | `DiffFields` | 各字段的对比结果 |

**`VersionMeta`（版本元信息）：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `version` | `String` | 版本号 |
| `status` | `String` | `pre` / `release` |
| `createTime` | `Long` | 创建时间，epoch 毫秒，与现有 `PromptVersionDetail.createTime` 格式一致 |

**`DiffFields`：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `template` | `DiffItem` | Prompt 模板内容对比 |
| `variables` | `DiffItem` | 变量列表对比 |
| `modelConfig` | `DiffItem` | 模型参数对比 |

**`DiffItem`：**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `changed` | `Boolean` | 两版本该字段是否有差异（null 视同空字符串参与比较） |
| `valueA` | `String` | 版本 A 的原始字符串值；字段为 null 时返回 `""` |
| `valueB` | `String` | 版本 B 的原始字符串值；字段为 null 时返回 `""` |

> **设计说明：** 接口返回原始字段值，不在后端生成行级 diff 标注。行级高亮由前端渲染层实现，后端不持有 UI 表达逻辑。

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "promptKey": "customer-service",
    "versionA": {
      "version": "v3",
      "status": "release",
      "createTime": 1745000000000
    },
    "versionB": {
      "version": "v5",
      "status": "pre",
      "createTime": 1745100000000
    },
    "diffs": {
      "template": {
        "changed": true,
        "valueA": "你是一名客服，请回答用户问题。\n\n用户问题：{{question}}",
        "valueB": "你是一名专业客服，请简洁准确地回答用户问题。\n\n用户问题：{{question}}\n\n要求：回答不超过200字。"
      },
      "variables": {
        "changed": false,
        "valueA": "[\"question\"]",
        "valueB": "[\"question\"]"
      },
      "modelConfig": {
        "changed": true,
        "valueA": "{\"temperature\": 0.7, \"maxTokens\": 1024}",
        "valueB": "{\"temperature\": 0.3, \"maxTokens\": 512}"
      }
    }
  }
}
```

### 错误码

| HTTP 状态码 | `code` | `message` | 触发条件 |
| --- | --- | --- | --- |
| 400 | 400 | `"参数错误：{字段} 不能为空"` | `promptKey`/`versionA`/`versionB` 为空或格式不符 |
| 400 | 400 | `"versionA 和 versionB 不能相同"` | 两个版本号完全相同 |
| 404 | 404 | `"Prompt 不存在"` | `promptKey` 在 DB 中不存在 |
| 404 | 404 | `"版本 {versionA} 不存在"` | 版本 A 在该 `promptKey` 下找不到 |
| 404 | 404 | `"版本 {versionB} 不存在"` | 版本 B 在该 `promptKey` 下找不到 |
| 500 | 500 | `"An internal error has occurred..."` | 未预期异常 |

> 错误码与 `StudioException` 常量对齐：`INVALID_PARAM=400`、`NOT_FOUND=404`、`SERVER_ERROR=500`。

---

## 4. 边界场景

| # | 场景 | 预期行为 | 依据 |
| --- | --- | --- | --- |
| E01 | `versionA == versionB`（传入相同版本号） | 返回 400，`"versionA 和 versionB 不能相同"` | 所有字段 `changed=false` 无业务意义，且大概率是调用方 bug；代码推断 |
| E02 | `promptKey` 存在，但某个版本不存在 | 返回 404，错误信息明确区分是 versionA 还是 versionB 不存在，如 `"版本 v9 不存在"` | 方便前端精确提示；代码推断 |
| E03 | `promptKey` 本身不存在 | 先查 `prompt` 表，返回 404 `"Prompt 不存在"`，不继续查版本表 | 快速失败，减少无效 DB 查询；代码推断 |
| E04 | 某版本的 `template` / `variables` / `modelConfig` 为 null（历史数据缺失） | null 视同空字符串：`valueA`/`valueB` 返回 `""`，`changed` 基于空字符串参与比较 | **产品决策** |
| E05 | `versionA` 和 `versionB` 属于不同 `promptKey` | 接口设计只有一个 `promptKey`，两版本必须属于同一 Prompt；传入版本号在当前 `promptKey` 下查不到时走 E02 的 404 逻辑 | 跨 Prompt 比较不在本期范围；代码推断 |
| E06 | 两个版本都是 `pre` 状态，或一个 `release` 一个 `pre` | 正常返回，不限制版本状态组合 | `pre` 版本之间的 diff 是合理的 review 场景；代码推断 |
| E07 | `promptKey` 对应的 Prompt 已被软删除 | 允许查 diff，正常返回版本内容 | 历史追溯需要；**产品决策** |
| E08 | `template` 内容极大（LONGTEXT，可达数 MB） | 直接返回完整内容，本期不做大小限制；网络传输性能由调用方承担 | **产品决策** |
| E09 | 版本号大小写，如 `versionA=V3` vs 存储值 `v3` | 不在应用层做 toLowerCase；查询结果依赖 MySQL collation（`admin` 库 `utf8mb4_general_ci`，大小写不敏感），行为与现有 `GET /api/prompt/version` 一致 | **产品决策** |
| E10 | 高并发对同一对版本频繁请求 diff | 纯只读接口，DB 读并发安全；本期不加缓存 | **产品决策** |

---

## 5. 老项目约束

| 约束 | CLAUDE.md 来源 | 对本需求的影响 |
| --- | --- | --- |
| `prompt_version` 表使用 **JPA `@Table`**，实体在 `server-start/entity` 包 | "ORM 混用" | 新查询逻辑必须走 JPA Repository 或 JPQL，不能混用 MyBatis-Plus `BaseMapper`；新 DTO `PromptVersionDiffResult` 不加 `@TableName` |
| 接口传参和关联外键一律用**业务 ID**，不暴露自增主键 | "业务 ID vs 自增主键" | 入参使用 `promptKey + versionA + versionB`，禁止用数据库 `id` 字段作为查询条件 |
| 大多数表用 `status = 0` 表示软删除，直接写 `DELETE` 会破坏数据完整性 | "逻辑删除" | `prompt_version` 当前无 `deleted` 字段；E07 决策允许查已软删除 Prompt 的版本，查询时不过滤 `prompt.status`，仅按 `prompt_key` 查版本表 |
| 统一返回结构：`Result<T> { code, message, data: T }` | "统一返回结构" | `PromptVersionDiffResult` 必须包在 `Result<>` 里返回；错误时用 `Result.error(code, message)`，不裸抛 HTTP 异常 |
| `admin` 库用 JPA，`agentscope` 库用 MyBatis-Plus，注意 DataSource 路由 | "ORM 混用" | `prompt_version` 在 `admin` 库，diff 接口只读 `admin` 库，无跨库操作风险 |

---

## 6. 不在这次范围里

### 本期砍掉（不做）

| 候选项 | 原因 |
| --- | --- |
| 后端生成 unified diff / Myers diff 行级标注 | diff 格式与前端 UI 框架强耦合，前端自行计算性能更好；后端不持有渲染逻辑 |
| 跨 `promptKey` 的版本对比 | 业务语义不清晰，`promptKey` 是版本的命名空间，跨 key 比较无明确含义 |
| 超过 2 个版本的多向对比（v3 vs v4 vs v5） | 界面复杂度指数级增加，当前场景不需要 |
| Diff 结果缓存（Redis） | 当前数据量小；缓存带来的失效一致性问题得不偿失，量上来再做 |
| `versionDescription` 字段的 diff | 非核心内容，改动频率低，意义不大 |
| 细粒度权限控制（仅创建者可查 diff） | 当前无细粒度资源权限体系，等权限体系完善后再加 |

### 留到下期

| 候选项 | 说明 |
| --- | --- |
| Diff 结果导出（PDF / 文本文件下载） | 有审计存档场景，非 MVP 必须 |
| 基于 `previousVersion` 的"一键比对上一版"快捷入口 | `PromptVersionDO.previousVersion` 字段已存在，后端接口已通用（调用方传 `versionA=previousVersion` 即可）；前端加快捷按钮即可，不需要新增后端接口 |
````
