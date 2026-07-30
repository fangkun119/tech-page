---
title: 传统项目迁AI 21：项目开发 - 流程回顾
author: fangkun119
date: 2026-07-04 21:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
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

<img src="imgs/aicmigr-21-dev-05-process-recap/9f2193b8d9ab7ad4ec314a697090fd86_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

## 1. 为什么老项目改造需要一套专门流程

<img src="imgs/aicmigr-21-dev-05-process-recap/0e52745ab112650e0f1311161a68b6e6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

用过 Cursor 或 Claude Code 写新项目，大概率体验过"一句话生成一个模块"的顺畅。把同样的工作流搬到企业老项目上，往往会卡得很惨——AI 写出来的代码编译不过、改一个接口崩掉一片功能、跑了半天才发现功能根本不需要做。

落差根源只有一句话：**老项目改造和从零开发，面对的是两种完全不同的工作对象**。

从零开发是"在白纸上画画"——没有历史包袱，AI 生成什么就是什么。<span style="color: red; font-weight: bold;">老项目改造是"在别人的画上修改，还要保证不弄脏原来的部分"——任何改动都可能破坏既有功能，而 AI 对"既有功能"的理解永远是片面的。</span>

### 1.1 一个真实翻车：跑了 4 小时发现功能已存在

团队遇到过这样一个案例。leader 甩来一句话需求："给 Prompt 管理加一个版本对比功能"。

工程师拿到需求，习惯性地让 AI 扫代码、设计接口、写实现。AI 很配合地输出了完整的 diff 接口设计，从 Controller 到 Service 到 DTO 一气呵成。工程师盯着 AI 跑了整整 4 个小时，所有代码都快写完了，才想起来打开浏览器看一眼产品。

结果发现：**Prompt 管理 → 版本历史页面，已经有一个"对比"按钮**，点击后弹出 modal 显示两个版本的 diff，功能完整，只是性能慢了一点。

<span style="color: red; font-weight: bold;">整整 4 小时的改造，白跑了。</span>

### 1.2 AI 为什么发现不了"功能已存在"

复盘这次翻车时，我们问了一个问题：AI 不是扫过代码了吗，为什么没告诉我们功能已经存在？

原因有两个，都和"代码层 vs 产品层"的视角差异有关。

#### (1) 代码层和产品层的视角不同

代码里有实现，不代表产品层有入口。前端按钮被注释掉过、又恢复过、权限配置改过、入口被隐藏过——这些在代码层看起来都像"功能不存在"，但产品层一开浏览器就能看到。

#### (2) AI 的"功能存在"判断标准太粗

AI 通常通过"是否存在同名 Controller、同名 Service 方法"来判断功能是否存在。但企业项目里同一个能力往往有多条实现路径（旧版 REST + 新版 GraphQL、后台 API + 前端 SDK），AI 容易漏判。

### 1.3 这套流程在解决什么问题

翻车后我们把整个改造链路复盘了一遍，提炼出一套五阶段闭环。它不是把 AI 能做的事重新做一遍，而是解决一个核心问题：**在 AI 自主跑流程的同时，把"不确定性"一步步消除掉**。

传统软件工程里，从需求到上线也分阶段——需求评审、技术方案评审、编码、测试、上线。这套流程对应得上，但每一阶段都针对"AI 替代人跑"这件事做了改写：把"需要人来判断"的点显式钉住，让 AI 自主跑其余部分。

下一章会把整张全景图展开，并说明跑这套流程前需要备齐什么。

## 2. 五阶段全景与前置准备

<img src="imgs/aicmigr-21-dev-05-process-recap/c6ef8ada709b64b8f415e69f5f835ec4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 整个闭环在做什么

<span style="color: red; font-weight: bold;">把五阶段串起来看，每一阶段都在消除一类不确定性。产出物喂给下一个阶段，关键决策点（图中虚线）必须停下来等人审。</span>

| 阶段 | 消除的不确定性 | 产出物 |
|------|---------------|--------|
| 阶段一 现状确认 | 功能到底存不存在 | 立项方向（新增/重构/优化） |
| 阶段二 拆需求 | 一句话需求到底要做什么 | PRD 定稿 |
| 阶段三 拆方案 | PRD 怎么落到代码上 | solution.md |
| 阶段四 后端改造 | 改动会不会破坏现有行为 | Characterization Test + 新代码 |
| 阶段五 前端+文档 | 改完真的能用吗 | 双截图 + docs/ 回灌 |

下面这张图给出了整个闭环的全景。每个阶段的产出物喂给下一个阶段，关键决策点（图中虚线）必须停下来等人审。

<!--
flowchart TD
    A["一句话需求"] --\> B{"阶段一<br>现状确认<br>必做硬约束"}
    B --\>|功能不存在| C["阶段二<br>拆需求<br>六维+人审三判断"]
    B --\>|功能已存在| Z["重新评估<br>新增/重构/优化"]
    C --\> D["阶段三<br>拆方案<br>七步法+人审决策点"]
    D --\> E["阶段四<br>后端改造<br>CharacterizationTest+P编号小步"]
    E --\> F["阶段五<br>前端改造+文档同步<br>双截图+回灌docs/"]
    F --\> G["可提PR"]

    B -.人工决策门.-> H1["人工点产品确认"]
    C -.人工决策门.-> H2["人审三个判断"]
    D -.人工决策门.-> H3["人审第7节决策点"]
    F -.人工决策门.-> H4["浏览器验证"]
-->

### 2.2 跑流程前的前置准备

整套流程跑通的前提是项目本身已经具备三个条件。

| 准备项 | 作用 |
|--------|------|
| 项目能跑通 | 所有提示词都能落到真实代码上 |
| 护栏到位 | Characterization Test 才有兜底 |
| CLAUDE.md 写好 | 提示词里"读 docs/ + CLAUDE.md"才有素材 |
| docs/ 资产齐全 | 拆需求、拆方案才能基于现状推断 |

如果项目还没跑通、测试没覆盖、CLAUDE.md 是空的，先回去把这几件事做完。否则下面所有提示词里的"基于 docs/..."都没有素材。

启动方式：

```bash
cd spring-ai-alibaba/spring-ai-alibaba-admin
```

在项目根目录启动 Claude Code，所有提示词都在这里跑。

下面从阶段一开始，逐阶段展开。

## 3. 阶段一 现状确认：最容易被跳过的一步

<img src="imgs/aicmigr-21-dev-05-process-recap/f1b2a4e89d76970919f9baefe0d0fe91_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一步是整个流程里最反直觉的——明明 AI 扫一下代码就能判断功能存不存在，为什么非要人去点产品？

因为 AI 真的判断不了。原因在第 1 章已经讲过：代码层和产品层视角不同，AI 的"功能存在"判断标准太粗。

**结论很简单：现状确认必须人来做，AI 替代不了。**

### 3.1 操作步骤

启动依赖服务：

```bash
./scripts/deps-start.sh
```

打开浏览器，带着一句话需求去点产品。判断标准如下。

| 现状 | 立项方向 |
|------|----------|
| 功能完全不存在 | 新增 |
| 功能存在但实现不符合预期 | 重构 |
| 功能存在但性能/体验差 | 优化 |

举个例子：leader 说"给 Prompt 加版本对比"，工程师打开 Prompt 管理 → 版本历史，点一下就知道旧的对比按钮是不是已经在工作。如果在工作，需求就不是"加新功能"，而是"性能优化"或"重构"，立项方向完全不同。

### 3.2 现状确认 Check List

| 检查项 | 说明 |
|--------|------|
| 完整走一遍用户路径 | 不止看入口页面，要按用户真实操作路径点完整套流程 |
| 截图存证 | 把现状截图存到改造记录里，作为后续对照基线 |
| 现状判断写进文档 | 把"功能已存在/不存在/部分存在"写进需求文档第一节，避免后续协作者重复确认 |

### 3.3 实战提示词 0：现状确认

下面这段提示词是改造开始前的第一段，必跑。

```
我打算做一个新需求："给 Prompt 管理加一个版本对比功能"。

我已经在产品上点了一遍，确认这个功能 [已存在 / 不存在 / 部分存在]。

现状描述：[一两句话写清楚，比如：版本历史页面已经有"对比"按钮，
点击后能弹出 modal 显示两个版本的 diff，功能完整]。

基于这个现状，告诉我下面的改造路径应该怎么定：是新增、重构、还是优化？
给一个判断 + 理由。
```

| # | review 重点 |
|---|------|
| 1 | 人来点产品，不是 AI 替你确认——AI 扫代码无法发现"功能已存在"，必须人手验证 |
| 2 | 现状描述要具体——不要写"功能好像存在"，要写清楚入口、操作路径、当前行为 |

跑完这一步，已经避开了老项目改造里最常见的坑。

## 4. 阶段二 拆需求：把一句话变成 PRD

<img src="imgs/aicmigr-21-dev-05-process-recap/88344d3c11b38b4797b193cf3e501886_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

现状确认完后，下一步是把一句话需求变成可执行的 PRD。产出路径是：**AI 写六维草稿 → 人审三个判断 → AI 整理成 PRD 定稿**。

传统软件工程里做需求分析，通常关心业务目标、接口契约、边界场景这三件套。六维拆解是这三件套的扩展版——补上了"老项目约束"和"不在这次范围"两个维度，这两个维度是 AI 时代老项目改造特有的。

### 4.1 六维拆解框架

| 维度 | 关键产出 | 谁来定 |
|------|----------|--------|
| 业务目标 | 一句话定位本次改造要解决什么 | 人审 |
| 用户场景 | 典型场景 + 痛点 | AI 起草、人审微调 |
| 接口契约 | 方法、路径、入参、返回、错误码 | AI 写（对齐项目风格） |
| 边界场景 | 至少 8 条 edge case | AI 列、人审做产品决策 |
| 老项目约束 | CLAUDE.md 禁区和历史包袱 | AI 写（标 CLAUDE.md 来源） |
| 不在这次范围 | 候选清单 | 人审 |

#### (1) AI 负责什么

AI 读 docs/ 下所有资产 + CLAUDE.md，扫一遍现有代码实现，按这六个维度输出 markdown 草稿。<span style="color: red; font-weight: bold;">这一步 AI 能覆盖大约 70% 的内容。</span>

#### (2) 人负责什么

<span style="color: red; font-weight: bold;">人只审三个判断：业务目标方向、边界场景的产品决策、不在这次范围里的事。其他三个维度（接口契约、老项目约束、技术边界）AI 写的直接用。</span>

你可能会问：为什么人只审三个，其他维度不审？

因为接口契约、老项目约束、技术边界这些维度，AI 基于代码和 docs/ 推断的准确率很高，人来审反而容易引入主观偏差。而业务方向、产品决策、范围划定涉及业务判断和优先级权衡，AI 没有上下文，必须人来定。

### 4.2 拆需求 Check List

| 检查项 | 说明 |
|--------|------|
| 六维都填上了 | 任何一个维度空着，下一步拆方案就会卡 |
| 边界场景至少 8 条 | 少于 8 条通常意味着 AI 没把代码扫透 |
| 老项目约束都有 CLAUDE.md 来源 | 每条约束要标明来自 CLAUDE.md 的哪一节，方便后续追溯 |
| "不在这次范围"有候选清单 | 候选清单是后续优化的输入，不能省 |

### 4.3 实战提示词 1：六维拆解

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

| # | review 重点 |
|---|------|
| 1 | 六维都填上了 |
| 2 | 边界场景至少 8 条 |
| 3 | 老项目约束都有 CLAUDE.md 来源 |
| 4 | "不在这次范围"有候选清单 |

### 4.4 实战提示词 2：人审三个判断 + 让 AI 出定稿

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

产出：定稿的需求文档。跑完阶段二，大约 20 分钟。

## 5. 阶段三 拆方案：把 PRD 变成可执行清单

<img src="imgs/aicmigr-21-dev-05-process-recap/1772dd35dc0bb9bcadcf9262bbb16aef_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

需求文档定稿后，下一步是把需求拆成可执行的改造方案。产出路径是：**AI 跑七步法 → solution.md 第 7 节集中暴露决策点 → 人审第 7 节 → 决策落定**。

传统软件工程里这一步对应 design review，产出的是技术方案文档。这里的 solution.md 是它的 AI 时代版本——<span style="color: red; font-weight: bold;">区别在于第 7 节"待审核的关键决策点"被单独抽出来，这是整套流程的灵魂设计。</span>

### 5.1 七步法框架

拆方案需要七个步骤，每步都有明确的产出和 review 重点。

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

solution.md 的第 7 节是"待审核的关键决策点"。这一节把前面散落在各步骤里所有"需要人决策"的点集中列出来，方便人一次性审完。

<span style="color: red; font-weight: bold;">打个比方：它像一个"决策收件箱"。如果没有这个收件箱，决策点会散落在前面六节里，人审起来必然漏。漏一个决策点，等于把那个决策权默认交给 AI——而 AI 在产品决策上是没有上下文的。</span>

#### (2) 拆方案的时间预算

整套七步做完大约 60 分钟。其中前五步 AI 主导约 40 分钟，人审第 7 节加反馈调整约 20 分钟。

### 5.2 拆方案 Check List

| 检查项 | 说明 |
|--------|------|
| 链路完整性 | HTTP 入口到 DB 的每个节点都列出来了，前端节点（页面、组件、API、类型声明）一个都不能漏 |
| 改造点编号化 | 每条改造点有唯一 P 编号，后续后端改造时按 P 编号分批执行 |
| 影响范围风险等级合理 | 风险等级直接决定后端改造时是否需要先跑 Characterization Test，定错等级会埋雷 |
| 关键决策点全部抽出来 | 第 7 节的决策点是人审的唯一入口，漏抽等于把决策权交给 AI |

### 5.3 实战提示词 3：摸链路（含前端）

```
基于 docs/requirements/prompt-version-diff.md 的需求，扫一下代码：

- 找出这次改造涉及的完整链路(HTTP 入口到 DB)
- 每个节点说明：文件、状态(现有/新增/修改)、关键逻辑
- 不要漏前端节点(页面、组件、API、类型声明)

输出表格 + 链路图，保存到 docs/requirements/prompt-version-diff-impact.md。
```

review 重点：前端节点列得对不对、链路完整性。

### 5.4 实战提示词 4：列改造点

```
基于上一步的链路，把改造拆成具体改造点列表(`P01`、`P02`、...)。
后端 / 前端 / 测试 / 文档都列出来，每条标类型 + 文件路径 + 一句话改什么。
追加到 prompt-version-diff-impact.md。
```

review 重点：前端列得齐不齐、测试有没有列。

### 5.5 实战提示词 5：画改造流程图

```
基于改造点，画一张改造流程图(mermaid sequence diagram)。
要展示：用户从前端发起请求的完整调用链 + 数据流。
保存到 docs/requirements/prompt-version-diff-flow(mermaid 代码块)。
```

review 重点：前端到后端的调用链完整、数据流画清楚。

### 5.6 实战提示词 6：说影响范围

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

review 重点：风险等级合理吗、有没有漏的影响项。

### 5.7 实战提示词 7：说改造步骤和顺序

```
基于改造点和影响范围，给出改造步骤和顺序：

- 按依赖关系排序，后端在前、前端跟上、测试穿插
- 每步说明：做什么、依赖、工作量、关键决策点
- 没有方案分歧的步骤直接给一个方案，不要硬凑多方案

输出表格。
```

review 重点：关键决策点准、前端步骤的"前置依赖"对（前端不需要等后端全部完成，有 mock 就能并行）。

### 5.8 实战提示词 8：整合成方案文档（关键决策单独抽出来）

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

review 重点：第 7 节决策点提取得齐、结构清晰。

### 5.9 实战提示词 9：人审核反馈调整

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

跑完阶段三，改造方案定稿，大约 60 分钟。

## 6. 阶段四 后端改造：给现有行为上保险

后端改造是整个流程里最容易翻车的环节。原因很简单：你要复用或修改现有方法，而任何对现有方法的改动都可能破坏老功能——这种破坏往往要等到上线后才暴露。

这一阶段的全部设计，都在解决一个问题：**怎么在改动现有代码的同时，保证老功能不偏移**。

<span style="color: red; font-weight: bold;">支撑这件事的是三大支柱：Characterization Test（锁住现有行为）、P 编号小步（控制改动范围）、断言凭实际（保证基线有效）。</span>

### 6.1 支柱一：Characterization Test 先行


<img src="imgs/aicmigr-21-dev-05-process-recap/d4d04fbd43f77a85c67ef384e93a9ecd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">


后端改造的第一步不是写新代码，而是锁住现有行为。任何要复用或修改的现有方法，改造前先用 Characterization Test 把当前行为钉死。

#### (1) 什么是 Characterization Test

Characterization Test 不是测试"代码应该是什么行为"，而是测试"代码现在实际是什么行为"。断言的来源不是设计文档，而是当前实际跑出来的值。

你可以把它类比成传统开发中的"回归测试基线"——但传统回归测试是基于需求文档写断言，Characterization Test 是基于实际行为写断言。这个区别很关键，下面会展开。

#### (2) 为什么必须先跑

后端改造往往要复用现有方法。复用前如果不锁住现有行为，后续改动很容易无意中破坏老功能——而且这种破坏要等到上线后才暴露。<span style="color: red; font-weight: bold;">先跑 Characterization Test 等于给现有行为上了一份保险。</span>

### 6.2 支柱二：P 编号小步推进

solution.md 里每个改造点都有一个 P 编号。后端改造严格按 P 编号分批执行，每批跑通后才进下一批。

这有点像传统开发的"灰度发布"——不一次性上线所有改动，而是分批验证，出问题能立刻定位到是哪一批。

| 批次 | 典型内容 | review 重点 |
|------|----------|-------------|
| 第一批 | DTO 类（P01-P03） | 字段和 solution.md 对得上、`git status` 只有新建文件 |
| 第二批 | Service 接口 + 实现（P04-P05） | 没动现有方法、null 处理对、Characterization Test 全过 |
| 第三批 | Controller（P06） | 接口签名对、没重构其他接口 |

#### (1) 为什么必须小步

小步推进的好处是出问题能立刻定位到是哪一批。一次性把 P01-P06 全做完，跑挂了根本不知道是哪一步引入的 bug。

#### (2) "只调用，不重构"原则

复用现有方法时只调用，不重构。即使是看起来不顺眼的代码也别动。

为什么这么严格？因为重构现有方法会破坏 Characterization Test 钉死的行为基线，等于把保险拆了。Characterization Test 锁的是"现有行为"，你一重构，行为变了，基线就失效了——<span style="color: red; font-weight: bold;">而失效的基线比没有基线更危险，因为它给你一种"还有保险"的错觉。</span>

### 6.3 支柱三：断言凭实际不凭应该

<span style="color: red; font-weight: bold;">写测试断言时，凭"实际跑出来是什么"写，不凭"应该是什么"写。这两者的区别是 Characterization Test 能不能起作用的关键。</span>

#### (1) 凭"应该"写会怎样

工程师觉得"这里应该返回非 null"，于是断言 `assertNotNull(result)`。如果实际行为是返回 null（可能是个 bug，也可能是历史包袱），测试立刻挂掉——但挂掉的原因不是改造引入的，而是历史包袱。你分不清这是回归还是历史问题。

#### (2) 凭"实际"写会怎样

<span style="color: red; font-weight: bold;">工程师先跑一遍看实际返回什么，再写断言。这样锁住的是真实行为，后续改造若改变了这个行为，测试才会挂——这才是改造引入的回归，才是你应该关心的信号。</span>

### 6.4 后端改造 Check List

| 检查项 | 说明 |
|--------|------|
| Characterization Test 全过 | 任何一步完成都要确认 Characterization Test 没挂 |
| 每批跑通 mvn test | 每批改造完成跑一次完整测试，失败立刻 stop |
| 失败不修 | 测试失败时 AI 不要试图修，列出来让人看——AI 修测试容易把断言改成"应该是什么"，破坏凭实际的原则 |
| 人来 curl 验证 | 接口跑通的最后一步必须人来 curl 验证 JSON 结构，AI 报告"接口跑通了"不可信 |

### 6.5 实战提示词 10：锁住改造前的行为

<img src="imgs/aicmigr-21-dev-05-process-recap/5da19512939aca68bf28f64b64b58768_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

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

review 重点（最关键）：断言凭"实际"写、测试能跑通。

### 6.6 实战提示词 11：建 DTO（按 P 编号）

```
基于 prompt-version-diff-solution.md 的 P01-P03，建对应的 DTO 类
(`PromptVersionDiffResult`、`VersionMeta`、`DiffItem`)。
严格按 solution.md 决策，对齐项目现有风格(lombok 注解、字段命名、null 处理)，
不要顺手改其他文件。

只做这三个 DTO，不要继续做下一批。
```

review 重点：字段和 solution.md 对得上、`git status` 只有新建文件。

### 6.7 实战提示词 12：实现 Service

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

review 重点：没动现有方法、null 处理对、Characterization Test 全过。

### 6.8 实战提示词 13：加 Controller

```
基于 prompt-version-diff-solution.md 的 P06，给 PromptController
加 `GET /api/prompt/version/diff` 接口。

异常走全局 GlobalExceptionHandler，不要在 Controller 里 try-catch。
不要重构 PromptController 现有的其他接口。

跑通 `mvn test` 确认全过。
```

review 重点：接口签名对、没重构其他接口。

### 6.9 实战提示词 14：补单元测试

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

review 重点：断言基于实际行为、边界场景齐全。

### 6.10 实战提示词 15：人来 curl 验证 JSON 结构

启动应用，手动 curl 一下新接口看实际返回的 JSON 结构和 solution.md 接口契约对得上：

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"saa","password":"123456"}' | jq -r '.data.access_token')

curl -s -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/prompt/version/diff?promptKey=xxx&versionA=v1&versionB=v2" \
  | jq
```

| # | review 重点 |
|---|------|
| 1 | data 不是 null |
| 2 | 嵌套字段都有 |
| 3 | 字段类型正确 |
| 4 | 这一步人来做，AI 报告"接口跑通了"不可信 |

### 6.11 实战提示词 16：跑通 mvn test 全套

```
跑完整测试，输出测试结果(通过 / 失败 / 跳过 各多少)。
失败的列出来不要试图修。
```

review 重点：失败数为 0、Characterization Test 全过、总测试数 = 改造前 + 新增。

跑完阶段四，后端改造收尾，大约 1-2 小时。

## 7. 阶段五 前端改造 + 文档同步

<img src="imgs/aicmigr-21-dev-05-process-recap/b158c1e7bd9b5e05bea369bdd16bcc44_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

后端跑通后进入前端改造和文档同步。这一阶段的核心是两件事：**用双截图证明改造真的生效、把新发现回灌到 docs/**。

### 7.1 双截图对照法

<span style="color: red; font-weight: bold;">前端改造必须留改造前和改造后两张截图。原因不是流程仪式感，而是 AI 改前端代码后，"构建无报错"不等于"页面真的对了"——只有人眼对照才能确认。</span>

这相当于传统开发里 QA 的"前后对比验收"，只不过在这里基线是改造前那张截图。

#### (1) 改造前截图作为基线

让 AI 先告诉工程师前端入口在哪里（菜单路径 + UI 位置），工程师照着点一遍，把现状截屏存下来。这张图是后续对照的基线。

#### (2) 改造后截图作为验收证据

改完之后浏览器重新加载，按改造目标操作一遍，截改造后的图。两张图对照能立刻看出改动是否符合预期。

### 7.2 文档同步的判断标准

前端跑通后，最后一步是把这一轮的所有新发现回灌到 docs/。这一步的关键不是"写不写"，而是"写到哪里"——写错地方会让后续改造被误导。

| 发现类型 | 写到哪里 |
|----------|----------|
| 影响所有未来类似改造 | CLAUDE.md |
| 只这一次的特殊处理 | solution.md |
| 新接口契约 | docs/api-list.md |
| 新增 DTO | docs/data-model.md |
| 新发现的边界 | docs/requirements/`<feature>`.md |

#### (1) 为什么区分 CLAUDE.md 和 solution.md

CLAUDE.md 是项目级约束，AI 在后续所有改造里都会读到。如果把一次性特殊处理也写进去，CLAUDE.md 会越来越臃肿，AI 读起来分不清哪些约束是普适的、哪些是某次改造的遗留——结果就是该守的约束没守住。

这个区分有点像传统架构里的"架构约束文档" vs "某次迭代的 design doc"：前者是长期生效的红线，后者是这一次的上下文记录。

#### (2) 文档同步的时间预算

文档同步大约 10 分钟，但价值很大——这一步等于把这一轮深度思考反向丰富了一圈 docs/ 资产。

### 7.3 实战提示词 17：让 AI 告诉你前端改造在哪里

```
基于 prompt-version-diff-solution.md，告诉我这次改造的前端入口在哪里
(菜单路径 + UI 位置)，方便我截图看现状。
```

照着点一遍，把现状截屏存下来。这是改造前的截图，后面对照用。

### 7.4 实战提示词 18：让 AI 概述要改什么 + 改完前端

```
基于 prompt-version-diff-solution.md，简单说一下前端要改哪些点、
改完应该是什么效果。

确认效果符合预期，让 AI 直接改完：
按上面说的改完前端，对齐项目风格，改完跑前端构建确认无报错。
```

| # | review 重点 |
|---|------|
| 1 | 跑通后 `git diff` 扫一眼改动范围 |
| 2 | 构建无报错 |
| 3 | 人来预览：浏览器重新加载，按改造目标操作一遍 |
| 4 | 把改造后的状态截屏，对照前面的截图 |

### 7.5 实战提示词 19：回灌 docs/

前端跑通后，改造闭环完成。最后一步把这一轮的所有新发现回灌到 docs/：

```
改造跑完了。把这一轮的所有新发现回灌到 docs/：

1. docs/api-list.md：把新接口标"已上线"，入参返回校对一遍
2. docs/data-model.md：加新增的 DTO(注意嵌套关系)
3. docs/requirements/prompt-version-diff.md：补审核新发现的边界
4. CLAUDE.md：加项目级新约束(如有)
```

判断标准：影响所有未来类似改造的写进 CLAUDE.md，只这一次的特殊处理留在 solution.md。

产出：每份文件改动 diff。时间预算：前端改造 30-40 分钟，文档同步 10 分钟。

## 8. 一键工作流：熟练后的高效打法

<img src="imgs/aicmigr-21-dev-05-process-recap/229efa22b18844310576205786545046_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面五个阶段一个一个跑，是为了让工程师看清每一步的产出和 review 点。真正上手之后，更高效的做法是：**一次粘贴、Claude Code 自主跑完整流程、关键决策点停下来等工程师判断**。

<span style="color: red; font-weight: bold;">这套打法的关键不是"让 AI 全自动"，而是"让 AI 自主跑流程的同时，把人审决策点显式地钉住"。</span><span style="color: red; font-weight: bold;">这是对"全自动化幻觉"的纠正——AI 能自主的部分让它自主，不能替你拍板的部分必须停。</span>

### 8.1 关键决策点必须停

<span style="color: red; font-weight: bold;">整套一键流程里有五个关键决策点，AI 不能替工程师拍板，必须停下来等。</span>

| # | 决策点 | 等工程师做什么 |
|---|--------|----------------|
| 1 | 现状确认 | 打开浏览器点产品，确认"功能不存在"才进下一步 |
| 2 | 业务目标修正 | 需求文档草稿出来后，反馈业务目标的修正 |
| 3 | 边界场景决策 | 边界场景的产品决策必须人来定，AI 不能替工程师拍板 |
| 4 | 方案 review | solution.md 第 7 节决策点必须人来审 |
| 5 | 前端截图 | 前端改造前后双截图必须人来截、来对照 |

### 8.2 自主原则

其他时间 AI 自主跑，遵循以下原则。

| # | 自主原则 | 具体做法 |
|---|----------|----------|
| 1 | 每步自审输出质量 | 不合格自己重跑，不等工程师发现 |
| 2 | 失败自己 debug | 失败自己 debug 自己修，除非连续 3 次同一错误才停下来 |
| 3 | 测试断言凭实际 | 不凭应该 |
| 4 | 不重构现有方法 | 只调用 |

### 8.3 summary.md 集中暴露 review 点

<span style="color: red; font-weight: bold;">AI 不可能完全替工程师思考，但可以把"我不确定的地方"集中到 summary.md 让工程师重点看。</span>这个文件列每个产出文件 + 工程师应该重点 review 的地方。

<span style="color: red; font-weight: bold;">可以把它理解成 AI 主动写的一份"不确定清单"——它替你跑了大部分活，但坦诚地把"我拿不准的地方"摆到台面上等你过目。</span>

### 8.4 一键工作流提示词原文

下面这段提示词就是干这个的。整段粘贴到 Claude Code，关键决策点会停下来等工程师输入。

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

### 8.5 这段提示词为什么这么写

#### (1) 第零步强制等待人确认

"现状确认"摆在第一位且强制等待人确认。翻车的教训是：跳过这一步可能让整个改造白跑 4 小时。这一条是这套工作流最关键的新增。

#### (2) 关键决策点显式让 AI 停下来

需求方向、边界决策、范围决策、方案 review、前端截图，这五个决策点 AI 不能替工程师做，必须停。其他时间它自主跑。

#### (3) 所有硬约束都明确写进去

Characterization Test、断言凭实际、不重构现有方法、3 次同错才停。这些约束散落在各个阶段，一键流程里要全部明确写出来，否则 AI 不会自觉遵守。

#### (4) summary.md 集中暴露 review 点

AI 不可能完全替工程师思考，但可以把"我不确定的地方"集中到 summary 让工程师重点看。

### 8.6 时间预算

粘贴完等 Claude Code 跑。<span style="color: red; font-weight: bold;">整个流程 3-4 小时（含工程师的几次 review）。工程师不在的时候它在跑，回来的时候它停在那里等工程师判断。</span>

## 9. 全流程 Check List

<img src="imgs/aicmigr-21-dev-05-process-recap/973b1ce1d990b4d62417cc277d055fef_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一章是可裁剪的全流程 Check List，按阶段组织。建议直接摘出来贴到项目仓库的 `.claude/checklists/` 下，每次改造开始前过一遍。

### 9.1 阶段一 现状确认

- [ ] 启动依赖服务并打开浏览器
- [ ] 按用户路径完整点一遍产品
- [ ] **现状截图存证**（用于改造后对比）
- [ ] 现状判断（新增/重构/优化）写进文档第一节

### 9.2 阶段二 拆需求

- [ ] 六维草稿（业务目标、用户场景、接口契约、边界场景、老项目约束、不在范围）
- [ ] 边界场景至少 8 条
- [ ] 老项目约束都标 CLAUDE.md 来源
- [ ] "不在范围"有候选清单
- [ ] **人审三个判断**（业务目标、边界决策、范围）
- [ ] PRD 定稿

### 9.3 阶段三 拆方案

- [ ] 链路表 + 链路图（含前端节点）
- [ ] 改造点 P 编号清单
- [ ] Mermaid 流程图
- [ ] 影响范围风险等级表
- [ ] 改造步骤 + 依赖 + 决策点
- [ ] solution.md 第 7 节决策点抽齐
- [ ] **人审第 7 节、决策落定**

### 9.4 阶段四 后端改造

- [ ] Characterization Test 钉死现有行为
- [ ] 按 P 编号分批（DTO → Service → Controller）
- [ ] 每批跑 mvn test
- [ ] **失败立刻 stop，不修**
- [ ] 单元测试覆盖关键边界
- [ ] 人来 curl 验证 JSON 结构
- [ ] 总测试数 = 改造前 + 新增

### 9.5 阶段五 前端改造 + 文档同步

- [ ] 改造前截图
- [ ] 改造后截图
- [ ] **浏览器手动验证**
- [ ] docs/api-list.md 同步
- [ ] docs/data-model.md 同步
- [ ] docs/requirements/`<feature>`.md 补边界
- [ ] CLAUDE.md 项目级约束 vs solution.md 一次性处理区分清楚

### 9.6 一键工作流

- [ ] 五个关键决策点显式停下来
- [ ] 自主原则（自审、自 debug、凭实际、不重构）写进提示词
- [ ] summary.md 集中暴露 review 点

## 10. 小结与值钱教训

<img src="imgs/aicmigr-21-dev-05-process-recap/be3df27f05e210274cfa29b8badb6fcd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 10.1 方法论闭环回顾

<span style="color: red; font-weight: bold;">本篇是"项目开发"部分的收尾。从拆需求、到拆方案、到后端改造、到前端改造和复盘，整个"老项目改造"的方法论全部跑完。</span>

把整个系列的方法论串起来一句话：**理解了项目（脑图）、跑通了项目（环境）、护住了项目（测试 + CI）、改造了项目（一次完整闭环）**。这四件事做完，已经具备在任何老项目上独立做改造的能力。

### 10.2 最值钱的教训

这次改造还留下了一条最值钱的教训：**改造前先点产品看现状，AI 扫代码不能替代**。

这条教训已经写进 CLAUDE.md，下次类似改造 AI 会主动提醒工程师做。本篇的"阶段一现状确认"和一键工作流里"第零步现状确认"都是这次教训的产物。

### 10.3 后续实践

这套流程跑完，下一步可以在自己的项目上反复实践、持续沉淀，把更多踩坑换成规则。

### 10.4 思考

#### (1) 时间分布

跑完整套流程大约花了多少时间？最卡的是哪一步：现状确认、拆需求、拆方案、后端改造、还是前端改造？

#### (2) 踩坑规则化

本篇的 19 个提示词 + 一键工作流里，"现状确认"是翻车后新加的硬约束。回想自己的工作里，有没有类似"踩了一次坑、留下一条规则"的经历？这道规则后来真的避免了再次踩坑吗？

