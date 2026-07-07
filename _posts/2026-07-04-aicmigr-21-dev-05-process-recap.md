---
title: 传统项目迁AI 21：项目开发 - 流程回顾
author: fangkun119
date: 2026-07-04 21:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-21-dev-05-process-recap/cover.jpg
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
aicmigr-21-dev-05-process-recap
传统项目迁AI 21：项目开发 - 流程回顾
-->

## 1. 改造闭环总览

<img src="imgs/aicmigr-21-dev-05-process-recap/59a50967b08bae6da545a69049473e7a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是"项目开发"部分的收尾篇。前面几篇把"接到一句话需求"到"前端跑通、可提 PR"的完整链路拆成了五个阶段一篇一篇讲，本篇把这五个阶段连起来跑一遍，让读者拿到一份既能整体把握、又能逐阶段落地的老项目改造闭环。

### 1.1 为什么要做流程回顾

每一篇单独看都能跑通一个环节，但真正落到企业项目里，工程师拿到的是一句话需求，交付的是一份完整可提 PR 的改造。从一句话需求到 PR，中间至少要经过：需求拆解、方案设计、后端改造、前端改造、文档同步五个阶段。任何一环掉链子，整个改造就会卡住或者返工。

本篇的价值在于把分散在前几篇里的方法论、提示词、Check List、踩坑教训串成一条线，让读者一次看完整个闭环。

### 1.2 全文导读地图

下面这张地图给出了整个改造闭环的全景。两类读者可以按图定位：

<img src="imgs/aicmigr-21-dev-05-process-recap/b249ba61057d2324abc714028a339fa6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    A["一句话需求"] --\> B{"阶段一<br>现状确认<br>必做硬约束"}
    B --\>|功能不存在| C["阶段二<br>拆需求<br>六维 + 人审三判断"]
    B --\>|功能已存在| Z["重新评估<br>新增/重构/优化"]
    C --\> D["阶段三<br>拆方案<br>七步法 + 人审决策点"]
    D --\> E["阶段四<br>后端改造<br>Characterization Test + P 编号小步"]
    E --\> F["阶段五<br>前端改造 + 文档同步<br>双截图 + 回灌 docs/"]
    F --\> G["可提 PR"]

    B -.人工决策门.-> H1["人工点产品确认"]
    C -.人工决策门.-> H2["人审三个判断"]
    D -.人工决策门.-> H3["人审第 7 节决策点"]
    F -.人工决策门.-> H4["浏览器验证"]
-->

#### (1) 两类读者的阅读路径

##### ① 方法论速查型读者

熟练 AI 编程工程师可以只看第一部分（第 1-8 章），快速回顾整套方法论、查 Check List。第 8 章是可裁剪的全流程 Check List，可以单独摘出来贴到项目仓库里。

##### ② 系统学习型读者

初学 AI 编程工程师建议先读第一部分建立框架，再读第二部分（第 9-15 章）跟着实战案例复现。第二部分保留了所有提示词原文，可以直接复制到自己项目里跑。

### 1.3 前置准备

整套流程跑通的前提是前面几篇的准备工作已经完成：

| 准备项 | 来源 | 作用 |
|--------|------|------|
| 项目跑通 | 系列前几篇 | 所有提示词都能落到真实代码上 |
| 护栏到位 | 系列前几篇 | Characterization Test 才有兜底 |
| CLAUDE.md 写好 | 系列前几篇 | 提示词里的"读 docs/ + CLAUDE.md"才有素材 |
| docs/ 资产齐全 | 系列前几篇 | 拆需求、拆方案才能基于现状推断 |

如果跳过前面直接读本篇，请先回到护栏部分跑一遍实操，确保有一个跑通且测试覆盖到位的项目，否则后面所有提示词的"基于 docs/..."都没有素材。

启动方式：

```bash
cd spring-ai-alibaba/spring-ai-alibaba-admin
```

在项目根目录启动 Claude Code，所有提示词都在这里跑。

## 2. 阶段一 现状确认（必做硬约束）

<img src="imgs/aicmigr-21-dev-05-process-recap/7e08aa916c094be7dae77685cfd78851_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一步是上一篇复盘留下的硬约束，比所有提示词都重要。任何改造任务开始之前，先打开生产环境点几下，确认你以为要做的功能是不是真的不存在。

### 2.1 为什么这一步不能跳

工程师最常见的踩坑场景：接到"给某某功能加版本对比"的需求，直接让 AI 扫代码、设计接口、写实现，跑了四个小时之后才发现产品里已经有版本对比按钮，只是性能慢了一点。整个改造白跑。

这条硬约束来自一次真实翻车——本系列前几篇里，工程师和 AI 一起做了一个本来不需要做的功能（项目里其实已经有版本对比了）。复盘后把它固化成"改造前先点产品看现状"的硬规则。

AI 扫代码无法发现"功能已存在"。原因有二：

#### (1) 代码层和产品层视角不同

代码里有实现不代表产品层有入口。前端按钮被注释掉、权限配置没开、入口被隐藏，这些在代码层看起来都像"功能不存在"，但产品层一开浏览器就能看到。

#### (2) AI 的"功能存在"判断标准太粗

AI 通常通过是否存在同名 Controller、同名 Service 方法判断功能是否存在。但企业项目里同一个能力可能有多个实现路径（旧版 REST + 新版 GraphQL、后台 API + 前端 SDK），AI 容易漏判。

### 2.2 操作步骤

启动依赖服务：

```bash
./scripts/deps-start.sh
```

打开浏览器，带着 leader 的一句话需求去点产品。判断标准很简单：

| 现状 | 立项方向 |
|------|----------|
| 功能完全不存在 | 新增 |
| 功能存在但实现不符合预期 | 重构 |
| 功能存在但性能/体验差 | 优化 |

举个例子：leader 说"给 Prompt 加版本对比"，工程师打开 Prompt 管理 → 版本历史，点一下就知道旧的对比按钮是不是已经在工作。如果在工作，需求就不是"加新功能"，而是"性能优化"或"重构"，立项方向完全不同。

### 2.3 现状确认 Check List

#### (1) 完整走一遍用户路径

不止看入口页面，要按用户真实操作路径点完整套流程，确认每一步都到位。

#### (2) 截图存证

把现状截图存到改造记录里，作为后续对照基线。

#### (3) 现状判断写进文档

把"功能已存在/不存在/部分存在"的判断写进需求文档第一节，避免后续协作者重复确认。

## 3. 阶段二 拆需求

<img src="imgs/aicmigr-21-dev-05-process-recap/fdbcf399c1019d8522b50f46e0724bc2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

需求文档的产出路径是：AI 写六维草稿 → 人审三个判断 → AI 整理成 PRD 定稿。这一节讲方法论，实战提示词见第 11 章。

### 3.1 六维拆解框架

把一句话需求拆成可执行的 PRD，需要六个维度全覆盖：

| 维度 | 关键产出 | 谁来定 |
|------|----------|--------|
| 业务目标 | 一句话定位本次改造要解决什么 | 人审 |
| 用户场景 | 典型场景 + 痛点 | AI 起草、人审微调 |
| 接口契约 | 方法、路径、入参、返回、错误码 | AI 写（对齐项目风格） |
| 边界场景 | 至少 8 条 edge case | AI 列、人审做产品决策 |
| 老项目约束 | CLAUDE.md 禁区和历史包袱 | AI 写（标 CLAUDE.md 来源） |
| 不在这次范围 | 候选清单 | 人审 |

#### (1) AI 负责什么

AI 读 docs/ 下所有资产 + CLAUDE.md，扫一遍现有代码实现，按这六个维度输出 markdown 草稿。这一步 AI 能覆盖大约 70% 的内容。

#### (2) 人负责什么

人只审三个判断：业务目标方向、边界场景的产品决策、不在这次范围里的事。其他三个维度（接口契约、老项目约束、技术边界）AI 写的直接用。

##### ① 为什么人只审三个

接口契约、老项目约束、技术边界这些维度，AI 基于代码和 docs/ 推断的准确率很高，人来审反而容易引入主观偏差。而业务方向、产品决策、范围划定涉及业务判断和优先级权衡，AI 没有上下文，必须人来定。

### 3.2 拆需求 Check List

#### (1) 六维都填上了

任何一个维度空着，下一步拆方案就会卡。

#### (2) 边界场景至少 8 条

少于 8 条通常意味着 AI 没把代码扫透。

#### (3) 老项目约束都有 CLAUDE.md 来源

每条约束要标明来自 CLAUDE.md 的哪一节，方便后续追溯。

#### (4) "不在这次范围"有候选清单

候选清单是后续优化的输入，不能省。

## 4. 阶段三 拆方案

<img src="imgs/aicmigr-21-dev-05-process-recap/926924cc498206a0fed05011fd4d2e71_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

需求文档定稿后，下一步是把需求拆成可执行的改造方案。这一节讲方法论，实战提示词见第 12 章。

### 4.1 七步法框架

拆方案需要七个步骤，每步都有明确的产出和 review 重点：

| 步骤 | 产出 | review 重点 |
|------|------|-------------|
| 1 摸链路 | 完整链路表格 + 链路图 | 前端节点列得对不对、链路完整性 |
| 2 列改造点 | P01、P02、... 改造点列表 | 前端列得齐不齐、测试有没有列 |
| 3 画流程图 | Mermaid sequence diagram | 前端到后端调用链完整、数据流清楚 |
| 4 说影响范围 | 风险等级表格（高/中/低） | 风险等级合理、有没有漏项 |
| 5 说改造步骤 | 步骤 + 依赖 + 工作量 + 决策点 | 前端前置依赖对（有 mock 就能并行） |
| 6 整合方案 | solution.md 七节 | 第 7 节决策点提取得齐 |
| 7 人审反馈 | 决策落定 | 第 7 节决策全部拍板 |

#### (1) 第 7 节为什么是关键

solution.md 的第 7 节是"待审核的关键决策点"。这一节把前面散落在各步骤里所有"需要人决策"的点集中列出来，方便人一次性审完。如果没有这一节，决策点散在六节里，人审起来会漏。

#### (2) 拆方案的时间预算

整套七步做完大约 60 分钟。其中前五步 AI 主导约 40 分钟，人审第 7 节加反馈调整约 20 分钟。

### 4.2 拆方案 Check List

#### (1) 链路完整性

HTTP 入口到 DB 的每个节点都列出来了，前端节点（页面、组件、API、类型声明）一个都不能漏。

#### (2) 改造点编号化

每条改造点有唯一 P 编号，后续后端改造时按 P 编号分批执行。

#### (3) 影响范围风险等级合理

风险等级直接决定后端改造时是否需要先跑 Characterization Test，定错等级会埋雷。

#### (4) 关键决策点全部抽出来

第 7 节的决策点是人审的唯一入口，漏抽等于把决策权交给 AI。

## 5. 阶段四 后端改造

<img src="imgs/aicmigr-21-dev-05-process-recap/682975fe3336e693d654dc882ce9493f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

后端改造是整个流程里最容易翻车的环节。这一节讲方法论，实战提示词见第 13 章。

### 5.1 Characterization Test 先行

后端改造的第一步不是写新代码，而是锁住现有行为。任何要复用或修改的现有方法，改造前先用 Characterization Test 把当前行为钉死。

#### (1) 什么是 Characterization Test

Characterization Test 不是测试"代码应该是什么行为"，而是测试"代码现在实际是什么行为"。断言的来源不是设计文档，而是当前实际跑出来的值。

#### (2) 为什么必须先跑

后端改造往往要复用现有方法。复用前如果不锁住现有行为，后续改动很容易无意中破坏老功能——而且这种破坏要等到上线后才暴露。先跑 Characterization Test 等于给现有行为上了一份保险。

### 5.2 P 编号小步推进

solution.md 里每个改造点都有一个 P 编号。后端改造严格按 P 编号分批执行，每批跑通后才进下一批。

| 批次 | 典型内容 | review 重点 |
|------|----------|-------------|
| 第一批 | DTO 类（P01-P03） | 字段和 solution.md 对得上、`git status` 只有新建文件 |
| 第二批 | Service 接口 + 实现（P04-P05） | 没动现有方法、null 处理对、Characterization Test 全过 |
| 第三批 | Controller（P06） | 接口签名对、没重构其他接口 |

#### (1) 为什么必须小步

小步推进的好处是出问题能立刻定位到是哪一批。一次性把 P01-P06 全做完，跑挂了根本不知道是哪一步引入的 bug。

#### (2) "只调用，不重构"原则

复用现有方法时只调用，不重构。即使是看起来不顺眼的代码也别动。重构现有方法会破坏 Characterization Test 钉死的行为基线，等于把保险拆了。

### 5.3 断言凭实际不凭应该

写测试断言时，凭"实际跑出来是什么"写，不凭"应该是什么"写。这两者的区别：

#### (1) 凭应该写

工程师觉得"这里应该返回非 null"，于是断言 `assertNotNull(result)`。如果实际行为是返回 null（可能是个 bug，也可能是历史包袱），测试立刻挂掉，但挂掉的原因不是改造引入的，而是历史包袱。

#### (2) 凭实际写

工程师先跑一遍看实际返回什么，再写断言。这样锁住的是真实行为，后续改造若改变了这个行为，测试才会挂——这才是改造引入的回归。

### 5.4 后端改造 Check List

#### (1) Characterization Test 全过

任何一步完成都要确认 Characterization Test 没挂。

#### (2) 每批跑通 mvn test

每批改造完成跑一次完整测试，失败立刻 stop。

#### (3) 失败不修

测试失败时 AI 不要试图修，列出来让人看。AI 修测试容易把断言改成"应该是什么"，破坏凭实际的原则。

#### (4) 人来 curl 验证

接口跑通的最后一步必须人来 curl 验证 JSON 结构，AI 报告"接口跑通了"不可信。

## 6. 阶段五 前端改造 + 文档同步

<img src="imgs/aicmigr-21-dev-05-process-recap/53c6329f2fa73b2870016d992a5b76e7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

后端跑通后进入前端改造和文档同步。这一节讲方法论，实战提示词见第 14 章。

### 6.1 双截图对照法

前端改造必须留改造前和改造后两张截图。原因：

#### (1) 改造前截图作为基线

让 AI 先告诉工程师前端入口在哪里（菜单路径 + UI 位置），工程师照着点一遍，把现状截屏存下来。这张图是后续对照的基线。

#### (2) 改造后截图作为验收证据

改完之后浏览器重新加载，按改造目标操作一遍，截改造后的图。两张图对照能立刻看出改动是否符合预期。

### 6.2 文档同步的判断标准

前端跑通后，最后一步是把这一轮的所有新发现回灌到 docs/。判断标准很关键：

| 发现类型 | 写到哪里 |
|----------|----------|
| 影响所有未来类似改造 | CLAUDE.md |
| 只这一次的特殊处理 | solution.md |
| 新接口契约 | docs/api-list.md |
| 新增 DTO | docs/data-model.md |
--\>发现的边界 | docs/requirements/`<feature>`.md |

#### (1) 为什么区分 CLAUDE.md 和 solution.md

CLAUDE.md 是项目级约束，AI 在后续所有改造里都会读到。如果把一次性特殊处理也写进去，CLAUDE.md 会越来越臃肿，AI 读起来分不清哪些约束是普适的、哪些是某次改造的遗留。

#### (2) 文档同步的时间预算

文档同步大约 10 分钟，但价值很大——这一步等于把这一轮深度思考反向丰富了一圈 docs/ 资产。

## 7. 一键工作流方法论

<img src="imgs/aicmigr-21-dev-05-process-recap/7be75e8581f465d3feb71afa54396bc1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面五个阶段一个一个跑是为了让工程师看清每一步的产出和 review 点。真正上手之后，更高效的做法是一次粘贴、Claude Code 自主跑完整流程、关键决策点停下来等工程师判断。

### 7.1 关键决策点必须停

整套一键流程里有五个关键决策点，AI 不能替工程师拍板，必须停下来等：

| # | 决策点 | 等工程师做什么 |
|---|--------|----------------|
| 1 | 现状确认 | 打开浏览器点产品，确认"功能不存在"才进下一步 |
| 2 | 业务目标修正 | 需求文档草稿出来后，反馈业务目标的修正 |
| 3 | 边界场景决策 | 边界场景的产品决策必须人来定，AI 不能替工程师拍板 |
| 4 | 方案 review | solution.md 第 7 节决策点必须人来审 |
| 5 | 前端截图 | 前端改造前后双截图必须人来截、来对照 |

### 7.2 自主原则

其他时间 AI 自主跑，遵循以下原则：

| # | 自主原则 | 具体做法 |
|---|----------|----------|
| 1 | 每步自审输出质量 | 不合格自己重跑，不等工程师发现 |
| 2 | 失败自己 debug | 失败自己 debug 自己修，除非连续 3 次同一错误才停下来 |
| 3 | 测试断言凭实际 | 不凭应该 |
| 4 | 不重构现有方法 | 只调用 |

### 7.3 summary.md 集中暴露 review 点

AI 不可能完全替工程师思考，但可以把"我不确定的地方"集中到 summary.md 让工程师重点看。这个文件列每个产出文件 + 工程师应该重点 review 的地方。

## 8. 全流程 Check List 汇总

<img src="imgs/aicmigr-21-dev-05-process-recap/21944e40aca8e5d7b2617a049cabcf62_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一章是可裁剪的全流程 Check List，按阶段组织。建议直接摘出来贴到项目仓库的 `.claude/checklists/` 下，每次改造开始前过一遍。

### 8.1 阶段一 现状确认

- [ ] 启动依赖服务并打开浏览器
- [ ] 按用户路径完整点一遍产品
- [ ] **现状截图存证**（用于改造后对比）
- [ ] 现状判断（新增/重构/优化）写进文档第一节

### 8.2 阶段二 拆需求

- [ ] 六维草稿（业务目标、用户场景、接口契约、边界场景、老项目约束、不在范围）
- [ ] 边界场景至少 8 条
- [ ] 老项目约束都标 CLAUDE.md 来源
- [ ] "不在范围"有候选清单
- [ ] **人审三个判断**（业务目标、边界决策、范围）
- [ ] PRD 定稿

### 8.3 阶段三 拆方案

- [ ] 链路表 + 链路图（含前端节点）
- [ ] 改造点 P 编号清单
- [ ] Mermaid 流程图
- [ ] 影响范围风险等级表
- [ ] 改造步骤 + 依赖 + 决策点
- [ ] solution.md 第 7 节决策点抽齐
- [ ] **人审第 7 节、决策落定**

### 8.4 阶段四 后端改造

- [ ] Characterization Test 钉死现有行为
- [ ] 按 P 编号分批（DTO → Service → Controller）
- [ ] 每批跑 mvn test
- [ ] **失败立刻 stop，不修**
- [ ] 单元测试覆盖关键边界
- [ ] 人来 curl 验证 JSON 结构
- [ ] 总测试数 = 改造前 + 新增

### 8.5 阶段五 前端改造 + 文档同步

- [ ] 改造前截图
- [ ] 改造后截图
- [ ] **浏览器手动验证**
- [ ] docs/api-list.md 同步
- [ ] docs/data-model.md 同步
- [ ] docs/requirements/`<feature>`.md 补边界
- [ ] CLAUDE.md 项目级约束 vs solution.md 一次性处理区分清楚

### 8.6 一键工作流

- [ ] 五个关键决策点显式停下来
- [ ] 自主原则（自审、自 debug、凭实际、不重构）写进提示词
- [ ] summary.md 集中暴露 review 点

## 9. 实战背景与准备

<img src="imgs/aicmigr-21-dev-05-process-recap/f3cda773a943133e3fcfb3b217dac64b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二部分以"给 Prompt 管理加版本对比功能"为案例，复现整套改造闭环。这个案例来自本系列前几篇的真实改造记录。

### 9.1 为什么选这个案例

#### (1) 业务场景真实

Prompt 版本对比是企业 AI 平台的常见需求，工程师拿到这个需求时几乎一定会先想到"加个 diff 接口"。

#### (2) 踩坑教训典型

这个案例在系列前几篇里翻过车——做之前没点产品，做了一半才发现已有版本对比。这条教训直接催生了"阶段一现状确认"这个硬约束。

#### (3) 覆盖环节全

这个案例从后端到前端到文档同步全链路都涉及，能完整演示五个阶段。

### 9.2 案例的项目背景

本案例基于 spring-ai-alibaba 项目。涉及的关键类：

```
- PromptVersionController
- PromptService
- PromptVersionServiceImpl
- PromptVersionEntity
- PromptVersionMapper
- PromptMapper
```

预期的接口示例：

```
GET /api/prompt/version/diff?promptKey=xxx&versionA=v3&versionB=v5
```

返回两个版本的 diff 结果。

### 9.3 改造产出物清单

整套改造完成后，docs/ 下会新增/更新以下文件：

```
docs/requirements/prompt-version-diff.md            # 需求文档
docs/requirements/prompt-version-diff-impact.md     # 链路与改造点
docs/requirements/prompt-version-diff-flow          # 改造流程图
docs/requirements/prompt-version-diff-solution.md   # 改造方案
docs/api-list.md                                    # 新接口标"已上线"
docs/data-model.md                                  # 新增 DTO
CLAUDE.md                                           # 项目级新约束（如有）
```

## 10. 实战·阶段一 现状确认

<img src="imgs/aicmigr-21-dev-05-process-recap/cbeb954b9864d5dc0e697cff2883361a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 10.1 翻车复盘

这个案例在系列前几篇里翻过一次车。当时的场景：leader 说"给 Prompt 加版本对比"，工程师直接让 AI 设计接口、写实现，跑了几个小时之后才点开产品发现 Prompt 管理 → 版本历史页面已经有"对比"按钮，点击后能弹出 modal 显示两个版本的 diff，功能完整。

复盘时发现：AI 扫代码扫不到这个功能，因为前端入口被注释掉过、又恢复过，代码层看起来像"不存在"，但产品层一开浏览器就能看到。

### 10.2 复盘后的硬约束

这次翻车留下了一条新的硬约束：任何改造开始之前，先打开产品点几下确认现状。这条约束写在 CLAUDE.md 里，下次类似改造 AI 会主动提醒。

### 10.3 实战提示词 0：现状确认

#### (1) 提示词

下面这段提示词是改造开始前的第一段，必跑：

```
我打算做一个新需求："给 Prompt 管理加一个版本对比功能"。

我已经在产品上点了一遍，确认这个功能 [已存在 / 不存在 / 部分存在]。

现状描述：[一两句话写清楚，比如：版本历史页面已经有"对比"按钮，
点击后能弹出 modal 显示两个版本的 diff，功能完整]。

基于这个现状，告诉我下面的改造路径应该怎么定：是新增、重构、还是优化？
给一个判断 + 理由。
```

#### (2) review 重点

- **① 人来点产品，不是 AI 替你确认**：AI 扫代码无法发现"功能已存在"，必须人手验证。
- **② 现状描述要具体**：不要写"功能好像存在"，要写清楚入口、操作路径、当前行为。

跑完这一步，工程师已经避开了老项目改造里最常见的坑。

## 11. 实战·阶段二 拆需求

<img src="imgs/aicmigr-21-dev-05-process-recap/1eab6a89aa70e0dd6793c245a5141e18_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 11.1 实战提示词 1：六维拆解

#### (1) 提示词

```
我有一个新需求：给 Prompt 管理加版本对比功能。

接口示例：

GET /api/prompt/version/diff?promptKey=xxx&versionA=v3&versionB=v5

返回两个版本的 diff 结果。

读 docs/ 下所有资产 + CLAUDE.md，再扫一下代码里现有 Prompt 版本
相关的实现(`PromptVersionController`、`PromptService`、`PromptVersionEntity`)，
按以下六维写需求文档草稿：

1. 业务目标(一句话)
2. 用户场景(典型场景 + 痛点)
3. 接口契约(方法、路径、入参、返回、错误码，对齐项目现有风格)
4. 边界场景清单(至少 8 条 edge case，每条标"基于代码推断"或"待产品决策")
5. 老项目约束(CLAUDE.md 禁区和历史包袱里相关的，每条标 CLAUDE.md 来源)
6. 不在这次范围里的事(先列候选)

输出 markdown，保存到 docs/requirements/prompt-version-diff.md。
```

产出：六维草稿，覆盖约 70% 内容。

#### (2) review 重点

- **① 六维都填上了；**
- **② 边界场景至少 8 条；
- **③ 老项目约束都有 CLAUDE.md 来源；**
- **④ "不在这次范围"有候选清单。**

### 11.2 实战提示词 2：人审三个判断 + 让 AI 出定稿

#### (1) 提示词

review 三件事：业务目标方向、边界场景的产品决策、不在这次范围里的事。其他维度（接口契约、老项目约束、技术边界）AI 写的直接用。

review 完把三个判断打包反馈：

```
我对 docs/requirements/prompt-version-diff.md 做了三个判断：

业务目标修正：从"工程师 review 自己的修改"改成"团队多人协作下的
Prompt 演进追溯"。diff 结果要返回 versionA/versionB 的元信息
(创建时间、状态)不只是内容。

边界场景的产品决策：

- E04(template 为 null)：null 视同空字符串
- E07(已软删除)：允许查 diff
- E08(LONGTEXT 超大)：本期不做大小限制
- E09(版本号大小写)：不在应用层 toLowerCase
- E10(高并发)：本期不加缓存

不在这次范围里的事最终决策：

- 砍掉：后端生成 unified diff、跨 promptKey 对比、N 版本对比、
  diff 缓存、versionDescription diff、权限控制
- 留到下期：diff 导出、一键比对上一版

按以上判断更新文档，整理为正式 PRD 格式，保存到原文件。
```

产出：定稿的需求文档。

#### (2) 时间预算

跑完阶段二，需求文档定稿，大约 20 分钟。

## 12. 实战·阶段三 拆方案

<img src="imgs/aicmigr-21-dev-05-process-recap/d82a4b1e5ad68f5fbfb1ceef5f3885a6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-21-dev-05-process-recap/fd805da3b1c9f19581afca5bcd5024ed_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
### 12.1 实战提示词 3：摸链路（含前端）

#### (1) 提示词

```
基于 docs/requirements/prompt-version-diff.md 的需求，扫一下代码：

- 找出这次改造涉及的完整链路(HTTP 入口到 DB)
- 每个节点说明：文件、状态(现有/新增/修改)、关键逻辑
- 不要漏前端节点(页面、组件、API、类型声明)

输出表格 + 链路图，保存到 docs/requirements/prompt-version-diff-impact.md。
```

#### (2) review 重点

- **① 前端节点列得对不对；**
- **② 链路完整性。**

### 12.2 实战提示词 4：列改造点

#### (1) 提示词

```
基于上一步的链路，把改造拆成具体改造点列表(`P01`、`P02`、...)。
后端 / 前端 / 测试 / 文档都列出来，每条标类型 + 文件路径 + 一句话改什么。
追加到 prompt-version-diff-impact.md。
```

#### (2) review 重点

- **① 前端列得齐不齐；**
- **② 测试有没有列。**

### 12.3 实战提示词 5：画改造流程图

#### (1) 提示词

```
基于改造点，画一张改造流程图(mermaid sequence diagram)。
要展示：用户从前端发起请求的完整调用链 + 数据流。
保存到 docs/requirements/prompt-version-diff-flow(mermaid 代码块)。
```

#### (2) review 重点

- **① 前端到后端的调用链完整；**
- **② 数据流画清楚。**

### 12.4 实战提示词 6：说影响范围

#### (1) 提示词

```
基于改造点和流程图，说明影响范围(每条标"高/中/低"风险)：

1. 现有接口受不受影响
2. 现有调用链路受不受影响
3. 测试影响
4. 文档影响
5. 前端兼容性
6. 性能影响

输出表格。
```

#### (2) review 重点

- **① 风险等级合理吗；**
- **② 有没有漏的影响项。**

### 12.5 实战提示词 7：说改造步骤和顺序

#### (1) 提示词

```
基于改造点和影响范围，给出改造步骤和顺序：

- 按依赖关系排序，后端在前、前端跟上、测试穿插
- 每步说明：做什么、依赖、工作量、关键决策点
- 没有方案分歧的步骤直接给一个方案，不要硬凑多方案

输出表格。
```

#### (2) review 重点

- **① 关键决策点准**
- **② 前端步骤的"前置依赖"对**：前端不需要等后端全部完成，有 mock 就能并行。

### 12.6 实战提示词 8：整合成方案文档（关键决策单独抽出来）

#### (1) 提示词

```
把前面五步的产出整合成完整方案文档：

1. 一句话概要
2. 涉及链路
3. 改造点清单
4. 改造流程图
5. 影响范围与风险
6. 改造步骤与顺序
7. 待审核的关键决策点(单独抽出来，方便人 review)

第 7 节是关键：把前面散落的所有"需要人决策"的点集中列出来。

保存到 docs/requirements/prompt-version-diff-solution.md。
```

#### (2) review 重点

- **① 第 7 节决策点提取得齐；**
- **② 结构清晰。**

### 12.7 实战提示词 9：人审核反馈调整

#### (1) 提示词

打开方案文档，先看第 7 节"待审核的关键决策点"，每条给出判断。把所有 review 发现汇总反馈：

```
我审核了方案文档，以下需要调整：

- P10 补充细节：用户在版本列表选中两条后，要禁用其他版本的勾选
- 影响范围漏了：Spring Security 配置中需要把 GET diff 接口加白名单
- 第 7 节决策全部拍板：
  - D1 null 视同空字符串
  - D2 直接改 props，同步更新调用方
  - D3 加 loading 状态
  - D4 加 latency 监控

更新 prompt-version-diff-solution.md。
第 7 节决策全部从"待审核"改成最终决策。
```

#### (2) 时间预算

跑完阶段三，改造方案定稿，大约 60 分钟。

## 13. 实战·阶段四 后端改造

<img src="imgs/aicmigr-21-dev-05-process-recap/dcdee7365611668b3c47721178b0c060_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-21-dev-05-process-recap/0ceae3b95c54af1c00be8909b6c2aed7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 13.1 实战提示词 10：锁住改造前的行为

#### (1) 提示词

```
我要改造 PromptVersionServiceImpl 复用 getByPromptKeyAndVersion，
改之前先用 Characterization Test 锁住该方法现有行为。

要求：

- 不要凭"应该是什么"写断言，凭"实际跑出来是什么"写
- 用 Mockito mock PromptVersionMapper，覆盖正常返回 + 版本不存在
  抛 StudioException 两种场景
- 测试加在 server-start 模块下(实现类在这个模块)
  路径：src/test/java/.../admin/service/impl/PromptVersionServiceImplTest.java
- 跑通汇报每个测试断言基于的实际值
```

#### (2) review 重点（最关键）

- **① 断言凭"实际"写；**
- **② 测试能跑通。**

### 13.2 实战提示词 11：建 DTO（按 P 编号）

#### (1) 提示词

```
基于 prompt-version-diff-solution.md 的 P01-P03，建对应的 DTO 类
(`PromptVersionDiffResult`、`VersionMeta`、`DiffItem`)。
严格按 solution.md 决策，对齐项目现有风格(lombok 注解、字段命名、null 处理)，
不要顺手改其他文件。

只做这三个 DTO，不要继续做下一批。
```

#### (2) review 重点

- **① 字段和 solution.md 对得上；**
- **② `git status` 只有新建文件。**

### 13.3 实战提示词 12：实现 Service

#### (1) 提示词

```
基于 prompt-version-diff-solution.md 的 P04-P05，给 PromptVersionService
加 `diffVersions` 方法 + 在 PromptVersionServiceImpl 里实现。

- 复用 getByPromptKeyAndVersion(影响范围已确认无 metrics 副作用)
- 不要重构 getByPromptKeyAndVersion 任何细节，只调用它
- null 处理用 a!= null ? a : "" 后再 Objects.equals 比较(对应 D1 决策)
- 异常用 StudioException + INVALID_PARAM / NOT_FOUND 错误码

跑 `mvn test` 确认 Step 10 的 Characterization Test 全过(行为不偏移)。
失败就 stop，告诉我具体哪个测试。

只做 P04-P05，不要做 P06。
```

#### (2) review 重点

- **① 没动现有方法；**
- **② null 处理对；**
- **③ Characterization Test 全过。**

### 13.4 实战提示词 13：加 Controller

#### (1) 提示词

```
基于 prompt-version-diff-solution.md 的 P06，给 PromptController
加 `GET /api/prompt/version/diff` 接口。

异常走全局 GlobalExceptionHandler，不要在 Controller 里 try-catch。
不要重构 PromptController 现有的其他接口。

跑通 `mvn test` 确认全过。
```

#### (2) review 重点

- **① 接口签名对；**
- **② 没重构其他接口。**

### 13.5 实战提示词 14：补单元测试

#### (1) 提示词

```
给 diffVersions 方法补单元测试。测试加在 server-start 模块下：
`PromptVersionServiceDiffTest.java`(如果不存在就新建)。

用 `@ExtendWith(MockitoExtension.class)` + Mockito mock PromptVersionMapper
和 PromptMapper(diffVersions 内部调了两个 Mapper，两个都要 mock)。

覆盖需求文档 prompt-version-diff.md 第 4 节的关键边界：

- E01 versionA == versionB → 抛 StudioException(INVALID_PARAM)
- E02 versionA 不存在 → 抛 StudioException(NOT_FOUND)
- E04 template 为 null → valueA/valueB 返回 ""、changed=false
- happy path：两版本 template 不同 → changed=true

断言凭"实际跑出来是什么"写，不凭"应该是什么"。
```

#### (2) review 重点

- **① 断言基于实际行为；**
- **② 边界场景齐全。**

### 13.6 实战提示词 15：人来 curl 验证 JSON 结构

#### (1) 命令

启动应用，手动 curl 一下新接口看实际返回的 JSON 结构和 solution.md 接口契约对得上：

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"saa","password":"123456"}' | jq -r '.data.access_token')

curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/prompt/version/diff?promptKey=xxx&versionA=v1&versionB=v2" \
  | jq
```

#### (2) review 重点

- **① data 不是 null；**
- **② 嵌套字段都有；**
- **③ 字段类型正确；**
- **④ 这一步人来做，AI 报告"接口跑通了"不可信。**

### 13.7 实战提示词 16：跑通 mvn test 全套

```
跑完整测试，输出测试结果(通过 / 失败 / 跳过 各多少)。
失败的列出来不要试图修。
```

#### (1) review 重点

- **① 失败数为 0；**
- **② Characterization Test 全过；**
- **③ 总测试数 = 改造前 + 新增。**

#### (2) 时间预算

跑完阶段四，后端改造收尾，大约 1-2 小时。

## 14. 实战·阶段五 前端改造 + 资产同步

<img src="imgs/aicmigr-21-dev-05-process-recap/72179c955f7db2fbc08c414446d459d8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 14.1 实战提示词 17：让 AI 告诉你前端改造在哪里

```
基于 prompt-version-diff-solution.md，告诉我这次改造的前端入口在哪里
(菜单路径 + UI 位置)，方便我截图看现状。
```

照着点一遍，把现状截屏存下来。这是改造前的截图，后面对照用。

### 14.2 实战提示词 18：让 AI 概述要改什么 + 改完前端

#### (1) 提示词

```
基于 prompt-version-diff-solution.md，简单说一下前端要改哪些点、
改完应该是什么效果。

确认效果符合预期，让 AI 直接改完：
按上面说的改完前端，对齐项目风格，改完跑前端构建确认无报错。
```

#### (2) review 重点

- **① 跑通后 `git diff` 扫一眼改动范围；**
- **② 构建无报错；**
- **③ 人来预览：浏览器重新加载，按改造目标操作一遍；**
- **④ 把改造后的状态截屏，对照前面的截图。**

### 14.3 实战提示词 19：回灌 docs/

#### (1) 提示词

前端跑通后，改造闭环完成。最后一步把这一轮的所有新发现回灌到 docs/：

```
改造跑完了。把这一轮的所有新发现回灌到 docs/：

1. docs/api-list.md：把新接口标"已上线"，入参返回校对一遍
2. docs/data-model.md：加新增的 DTO(注意嵌套关系)
3. docs/requirements/prompt-version-diff.md：补审核新发现的边界
4. CLAUDE.md：加项目级新约束(如有)
```

#### (2) 判断标准

影响所有未来类似改造的写进 CLAUDE.md，只这一次的特殊处理留在 solution.md。

#### (2) 产出和实践预算

产出：每份文件改动 diff。

时间预算：前端改造 30-40 分钟，文档同步 10 分钟。

## 15. 实战·一键工作流原文

<img src="imgs/aicmigr-21-dev-05-process-recap/08712511490240397971eb2a0f65126f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面四个阶段一个一个跑是为了让工程师看清每一步的产出和 review 点。真正上手之后，更高效的做法是一次粘贴、Claude Code 自主跑完整流程、关键决策点停下来等工程师判断。

下面这段提示词就是干这个的。整段粘贴到 Claude Code，关键决策点会停下来等工程师输入。

### 15.1 一键工作流提示词原文

```
我刚拿到一个新需求：[把 leader 的一句话需求填这里]

完整跑通改造流程，全程自主推进，遇到关键决策点停下来等我，
不要每一步都问我。请按以下顺序执行：

第零步：现状确认(必做，不能跳)

- 提醒我打开浏览器、按需求路径点一下产品看现状
- 等我确认"功能不存在"才能进下一步
- 如果功能已存在，停下来让我重新评估改造方向(新增 vs 重构 vs 优化)

第一步：拆需求

- 读 docs/ + CLAUDE.md + 现有代码
- 按六维写需求文档草稿到 docs/requirements/`<feature>`.md
- 列出所有"待产品决策"的边界场景和"不在这次范围里"的候选
- 停下来等我反馈业务目标修正、边界场景决策、范围决策

我反馈完后：

- 整理为正式 PRD 文档定稿

第二步：拆方案

- 摸链路(含前端)+ 列改造点 + 画流程图 + 说影响 + 说步骤
- 整合成 solution.md，第 7 节"待审核的关键决策点"单独抽出来
- 停下来等我审核 solution.md 第 7 节 + 反馈调整

我反馈完后：

- 更新 solution.md 把决策落定

第三步：后端改造

- 锁现有行为 Characterization Test 先跑
- 按 P01-P03、P04-P05、P06 三批小步执行，每批跑通后才下一批
- 任何 Characterization Test 失败立刻 stop
- 补单元测试覆盖关键边界
- 跑 mvn test 全套，告诉我总测试数 + 失败数

第四步：前端改造(先停下来让我截图改造前)

- 告诉我前端入口位置(菜单路径)，等我截图改造前
- 改完前端，跑构建确认无报错
- 等我浏览器验证

第五步：文档自动更新

api-list.md / data-model.md / requirements/`<feature>`.md / CLAUDE.md
全部同步更新

自主原则：

- 每步跑完自己 review 输出质量，不合格自己重跑
- 失败自己 debug 自己修(除非连续 3 次同一错误)
- 测试断言凭实际不凭应该
- 不要重构现有方法，只调用
- 关键决策点停下来等我，不要替我拍板

跑完输出 `summary.md`，列每个产出文件 + 我应该重点 review 的地方。
```

### 15.2 这段提示词为什么这么写

#### (1) 第零步强制等待人确认

"现状确认"摆在第一位且强制等待人确认。上一篇翻车的教训：跳过这一步可能会让整个改造白跑 4 小时。这一条是这次工作流相比系列前几篇护栏脚本最关键的新增。

#### (2) 关键决策点显式让 AI 停下来

需求方向、边界决策、范围决策、方案 review、前端截图，这五个决策点 AI 不能替工程师做，必须停。其他时间它自主跑。

#### (3) 所有硬约束都明确写进去

Characterization Test、断言凭实际、不重构现有方法、3 次同错才停。这些约束散落在系列前几篇，一键流程里要全部明确写出来。

#### (4) summary.md 集中暴露 review 点

AI 不可能完全替工程师思考，但可以把"我不确定的地方"集中到 summary 让工程师重点看。

### 15.3 时间预算

粘贴完等 Claude Code 跑。整个流程 3-4 小时（含工程师的几次 review）。工程师不在的时候它在跑，回来的时候它停在那里等工程师判断。

## 16. 小结与思考

<img src="imgs/aicmigr-21-dev-05-process-recap/14c52d454087c166e9dca6a0b6ded03e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 16.1 方法论闭环回顾

本篇是"项目开发"部分的收尾。从拆需求、到拆方案、到后端改造、到前端改造和复盘，整个"老项目改造"的方法论全部跑完。

把整个系列的方法论串起来一句话：理解了项目（脑图）、跑通了项目（环境）、护住了项目（测试 + CI）、改造了项目（一次完整闭环）。这四件事做完，工程师已经具备在任何老项目上独立做改造的能力。

### 16.2 最值钱的教训

这次改造还留下了一条最值钱的教训：改造前先点产品看现状，AI 扫代码不能替代。

这条教训已经写进 CLAUDE.md，下次类似改造 AI 会主动提醒工程师做。本篇的"阶段一现状确认"和一键工作流里"第零步现状确认"都是这次教训的产物。

### 16.3 后续展望

下一篇会做整个改造的大复盘，把方法论再深一层。

### 16.4 思考

#### (1) 时间分布

跑完整套流程大约花了多少时间？最卡的是哪一步：现状确认、拆需求、拆方案、后端改造、还是前端改造？

#### (2) 踩坑规则化

本篇的 19 个提示词 + 一键工作流里，"现状确认"是上一篇翻车后新加的硬约束。回想工程师自己的工作里，有没有类似"踩了一次坑、留下一条规则"的经历？这条规则后来真的避免了再次踩坑吗？
