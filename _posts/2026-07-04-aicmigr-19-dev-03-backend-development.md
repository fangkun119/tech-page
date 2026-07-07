---
title: 传统项目迁AI 19：项目开发 - 后端开发
author: fangkun119
date: 2026-07-04 19:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-19-dev-03-backend-development/cover.jpg
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
aicmigr-19-dev-03-backend-development
传统项目迁AI 19：项目开发 - 后端开发
-->

## 1. 全文导读

<img src="imgs/aicmigr-19-dev-03-backend-development/b477404f5a59a01f6c9564f80736e488_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇把"后端开发"拆成两部分组织：第一部分提炼方法论（参考手册风，可裁剪 Check List），让熟练工程师快速回顾改造执行的四个原则与七个步骤；第二部分以 prompt-version-diff 案例逐步复现七步落地（教材风，深入解释 why），让初学工程师系统掌握"AI 小步改 + 人严格 review + Characterization Test 兜底"这套老项目改造打法。读者可按下图定位章节。

<img src="imgs/aicmigr-19-dev-03-backend-development/f24e9bcf94014aa8369711004014384f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/19_项目开发_03：后端开发/f24e9bcf94014aa8369711004014384f_MD5.jpg
用途：后端开发七步走总览图，展示从锁住行为到提交文档的完整执行链路
内容：Step 1 锁住改造前的行为（Characterization Test）→ Step 2 建 DTO（P01-P03）→ Step 3 实现 Service（P04-P05）→ Step 4 加 Controller（P06）→ Step 5 补单元测试 + curl 验证返回结构 → Step 6 跑通 mvn test 全套 → Step 7 提交 + 文档自动更新。整条链路贯穿四个原则：小步执行、自主修复 + 3 次兜底、复用现有结构、补测试不补到位不算完成。
-->

- 初学工程师：建议先读第一部分建立"四个原则 + 七步走"的框架，再按第二部分 Step 1-7 逐步复现，每步对照提示词与 review 重点理解 why。
- 熟练工程师：可直接看第 3 章方法论详解表格和第 4 章可裁剪 Check List，第二部分按需查阅。

## 2. 总览：方案到手 ≠ 可以让 AI 一把梭

<img src="imgs/aicmigr-19-dev-03-backend-development/b588449ece4cb2ddd1fbf6d95b7c4554_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 后端改造的真实风险面

第 18 篇跑完，开发者手上有一份审核过的改造方案，终于到了动手写代码这一步。这一篇做后端，对应方案里的 P01-P06：建 DTO、实现 Service、加 Controller、补集成测试、跑通 mvn test。

这一篇的提示词会写得比较细，约束比较多。原因是：本篇就是让 AI 完成后端接口的开发，越详细的提示词和约束效果越好。建议读者多琢磨提示词的内容和思路。

在执行阶段，最大的风险来自 AI 的两个默认行为：

#### (1) AI 爱"顺手"优化老代码

开发者让 AI 加一个方法，AI 顺手把现有方法重构了。这种改动单看都合理，但只要现有行为变了，所有调用方都受影响——这句话其实很细节，也是 AI 经常会犯的问题。在真实的老项目改造中，改造质量的差异或者改出问题，往往都来自于这种很细的细节。这就是为什么前面花了那么多时间在整理文档和上下文，并且让开发者停下来花时间去确认、调整。

#### (2) AI 爱凭"应该"写测试断言

AI 写测试时断言全过，但根本没跑过现有代码。AI 经常会用业务直觉补断言——比如看代码 `if (result == null) return Collections.emptyList()`，凭直觉写 `assertNotNull(result)`，但实际跑代码可能因为业务数据导致返回 null，于是测试反而失败。这是隐性偏差在第 15 篇讲过的内容，这一篇是它在真实改造里的具体表现。

两类坑不在提示词里硬约束，就会一路埋到生产。这一篇的核心思路是：AI 小步执行 + 开发者严格 review + Characterization Test 兜底。七步跑下来，后端代码可运行、有测试覆盖、不破坏现有行为，commit 后等前端联调（第 20 篇）。

### 2.2 为什么不能"一个提示词搞定"

读者可能会有疑问：如果是新项目，这个需求一个提示词就搞定了，Claude Code 能跑得很好。为什么要分这么多步？

这就是新项目开发和老项目开发的区别。老项目开发需要步步为营，一步一步来，越细致越好。读者可能进一步追问：这么细不是很浪费时间吗？这其实要回到本系列最开始的命题——出 bug 处理的时间、返工的时间，比做的时候因为细致花的时间多得多。

所以老项目改造，慢就是快。别急，细致点，按照流程步骤来，反而会觉得真的很快，比想象中快。本篇的提示词很详细，不是为了让读者觉得繁琐，而是为了让读者体验到一个细致的提示词带来的效果——提示词宁多勿少，读者在实际项目中可以精简。

## 3. 方法论核心：四个原则 + 七步走

### 3.1 改造前必须锁住现有行为

第 15 篇讲过 Characterization Test：不是测代码"应该做什么"，是锁住代码"现在实际做什么"。这一篇是它在真实改造里第一次落地。

为什么改造前必须锁现有行为？以这一篇的案例为例：第 18 篇的方案里 P05（实现 diffVersions）要复用 `getByPromptKeyAndVersion`。复用就意味着 AI 可能"顺手"动这个老方法——比如改个返回类型、加个参数、调整空值处理。这些改动单看都合理，但只要现有行为变了，所有调用方都受影响。

锁现有行为的做法很简单：改造前先跑一次现有代码，把它的实际输入输出记下来，作为测试断言。改造后再跑一遍，断言通过就说明现有行为没变。这一步是必选项——没有 Characterization Test 兜底，AI 改坏了可能得两周后才发现。

### 3.2 改造执行的四个原则

<img src="imgs/aicmigr-19-dev-03-backend-development/79436246804e2f80d500e7a6f0376648_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

四个原则贯穿后面的七步，本质是一句话：让 AI 走小步、走对方向、能验证。

| 原则 | 含义 | 反例（不遵守会怎样） |
|---|---|---|
| 小步执行 | AI 默认会一口气把 P01-P06 全改完。明确要求 AI 按改造点分批：P01-P03 一批、P04-P05 一批、P06 一批，每批跑通了 review + commit 再下一批。 | 改完出错不知道是哪一步出的，回退也不好回。 |
| 自主修复 + 3 次兜底 | 改造过程中编译报错、测试失败、依赖冲突都是常态。AI 要能自己修、自己重试，但连续 3 次同一错误必须停下来问人（第 13 篇讲过这个机制，这里直接复用）。 | AI 在同一坑里反复试错，烧 token 还修不对，越改越偏。 |
| 复用现有结构 | Spring AI Alibaba Admin 有自己的代码风格：统一返回结构 `Result<T>`、JPA `@Table` 风格、`StudioException` 异常处理体系。明确要求"对齐项目现有风格"。 | AI 按业界最佳实践写一套和项目不一致的代码，风格割裂，后期维护成本高。 |
| 补测试不补到位不算完成 | 每个改造点跑完都要有对应的测试，没有测试的改造点不算 Done。 | 改造点跑通但无测试，下次改坏无人知晓，回归只能靠人工。 |

### 3.3 七步走详解

<img src="imgs/aicmigr-19-dev-03-backend-development/384e2d1f6c54dc1cdec49dc1c39d1d05_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) Step 1：锁住改造前的行为（Characterization Test）

| 维度 | 内容 |
|---|---|
| 目标 | 在动手改之前，先用 Characterization Test 锁住待复用方法（如 `getByPromptKeyAndVersion`）的现有行为，建立改造前后行为不变的硬指标。 |
| 做什么 | 先读待复用方法的实现，记录它实际做的事；用 Mockito mock 依赖，按"实际跑出来是什么"写断言；覆盖正常返回 + 异常路径两类场景。 |
| AI 如何思考 | AI 默认会凭"应该是什么"补断言（业务直觉），而不是凭"实际是什么"写。提示词必须明确禁止凭直觉写断言。 |
| 关注点 | 测试覆盖场景要与待复用方法的真实分支对齐，不要凭假设加场景（比如方法不做状态过滤，就不要加"状态过滤"场景）。 |
| review 重点 | 打开测试文件看 `assertEquals(...)` 里的预期值——看到 `assertEquals(100, ...)` 这种值，追问"100 这个值是从哪来的？是跑现有代码跑出来的，还是猜的？"。 |
| 技巧 | 测试加在能访问到待测类的模块下（如 server-start），原因要写进提示词让 AI 不困惑；明确给出跑测试的 mvn 命令。 |

这一步是第 15 篇心法在真实改造里的第一次落地，也是后面所有步骤的兜底机制。如果 Characterization Test 失败，立刻知道行为变了。

#### (2) Step 2：建 DTO（P01-P03）

| 维度 | 内容 |
|---|---|
| 目标 | 锁住现有行为后，第一批改造是把方案里的 DTO 类建出来，为 Service/Controller 提供类型基础。 |
| 做什么 | 按方案文档（如 solution.md 第 7 节最终决策）建 DTO；字段名、类型、注释与方案对齐；加 lombok 注解（`@Data @Builder @NoArgsConstructor @AllArgsConstructor`）对齐项目现有 DTO 风格。 |
| AI 如何思考 | AI 会默认按业界最佳实践写字段命名和 null 处理，可能与项目既有 DTO 风格（如 createTime 用 epoch ms）不一致。 |
| 关注点 | 严格按方案的最终决策（如 D1 null 视同空字符串等）；不要顺手改其他文件；只做这一批 DTO，不要继续做下一批。 |
| review 重点 | 字段是不是和方案文档对得上（打开方案改造点表格 + 决策点，逐字段对照）；git status 看是否只动了应该动的文件。 |
| 技巧 | 提示词最后一句必须明确"只做 P01-P03，不要继续 P04-P05"——这是关键约束，挡住 AI 一口气改完的倾向。 |

#### (3) Step 3：实现 Service（P04-P05）

| 维度 | 内容 |
|---|---|
| 目标 | 第二批改造是 Service 接口和实现，复用待复用方法（如 `getByPromptKeyAndVersion`），不能重构它。 |
| 做什么 | 按方案设计：调 Mapper 两次 → 校验 → 内存比较关键字段 → 组装返回；null 处理用方案约定的语义（如 `nullToEmpty`）；异常用项目的异常体系（如 `StudioException` + INVALID_PARAM/NOT_FOUND 错误码）。 |
| AI 如何思考 | AI 看到"复用老方法"会想顺手把它重构得更优雅。提示词必须明确"不要重构 X 任何细节，只调用它"。 |
| 关注点 | 实现完跑一遍 Step 1 的 Characterization Test，确认现有行为没偏移；有失败立刻 stop 让人介入判断。 |
| review 重点 | git diff 看实现类，应该只有新增方法，原方法一行不动；null 处理对不对（`a != null ? a : ""` 后再 `Objects.equals` 比较，而不是 `a == null && b == null`）；Characterization Test 全过。 |
| 技巧 | 提示词让 AI 跑 Step 1 的 Characterization Test 作为兜底，有失败就 stop——这是把"行为不变"做成硬指标的关键设计。 |

#### (4) Step 4：加 Controller（P06）

| 维度 | 内容 |
|---|---|
| 目标 | 第三批改造是 Controller 接口，对外暴露新功能。 |
| 做什么 | 加 GET 接口；入参全部 `@RequestParam` + `@NotBlank` 校验；正常路径走项目统一的返回结构（如 `Result.success(data)`）；异常处理走全局 `@RestControllerAdvice`，不在 Controller 里 try-catch。 |
| AI 如何思考 | AI 倾向在 Controller 里自己 try-catch + 包装错误响应。提示词必须明确禁止，让它走全局异常处理。 |
| 关注点 | 接口路径不与现有路径冲突；不要重构 Controller 现有的其他接口；跑一遍 mvn test 确认全部通过；用 curl 跑新接口看返回结构对不对。 |
| review 重点 | 接口签名和方案描述完全一致；git diff 看应该只有新增方法 + 一行 import；curl 返回结构与方案接口契约对得上（这一步人来跑最稳）。 |
| 技巧 | 提示词明确"只做 P06，不要继续做集成测试，那是下一步"——分批约束在每一步都要重复。 |

#### (5) Step 5：补单元测试 + curl 验证返回结构

| 维度 | 内容 |
|---|---|
| 目标 | 新接口跑通后，分两部分验证：Service 层单元测试验证逻辑正确，curl 验证真实 HTTP 响应结构对得上接口契约。 |
| 做什么 | 单元测试用 `@ExtendWith(MockitoExtension.class)` + Mockito mock Mapper（注意 mock 范围对——mock Mapper，不要 mock 被测的 Service 自身）；覆盖方案文档里的关键边界（如 E01/E02/E04 + happy path）；curl 用真实数据库里已有的两个版本手动跑。 |
| AI 如何思考 | AI 容易直接 `when(...).thenReturn(mock对象)`，然后凭直觉写断言，而不是先想"这个 mock 会让被测方法实际算出什么"。 |
| 关注点 | 单元测试和 curl 不能互相替代——单元测试发现不了 JSON 字段名拼错、类型序列化异常这类问题；断言凭"实际跑出来是什么"写。 |
| review 重点 | 看到断言追问"这个值是跑出来的还是猜的"；边界场景齐全；mock 范围对；curl 这步不要让 AI 代劳——AI 报告"接口跑通了"不可信，自己眼睛看到 JSON 结构才算验证完。 |
| 技巧 | 项目没有 Testcontainers 基础设施时，不要用 `@SpringBootTest` + 真实 DB，用 Mockito；测试模块要选对（能访问到 Service 和异常类的那一个）。 |

curl 验证要重点盯三件事：data 字段存在（不是 null）；diffs 下三个字段都有（template / variables / modelConfig）；changed 是 boolean，valueA / valueB 是字符串（不是 null）。

#### (6) Step 6：跑通 mvn test 全套

| 维度 | 内容 |
|---|---|
| 目标 | 所有后端改造点跑完后，最后跑一遍完整 mvn test，确认整体没问题。 |
| 做什么 | 跑完整测试（含新增的测试模块）；输出全部测试结果（通过 / 失败 / 跳过各多少）；失败的列出来，但不要让 AI 试图修，只汇报。 |
| AI 如何思考 | AI 看到 failing test 会自己上手修，可能把测试改成"全过"而不是修代码。提示词必须明确"不要修，只汇报"。 |
| 关注点 | 失败数为 0（任何失败都不能进下一步）；Step 1 的 Characterization Test 全过（证明现有行为没被破坏）；总测试数 = 改造前基线 + 新增。 |
| review 重点 | 总数不对（比如基线 14 + 新增 6 = 20），说明有测试被意外删了或跳过了；Step 1 测试和改造前完全一致。 |
| 技巧 | 让 AI 用 `-fae`（fail at end）跑全套，把所有模块的失败都暴露出来，不要一个失败就停。 |

#### (7) Step 7：提交 + 文档自动更新

| 维度 | 内容 |
|---|---|
| 目标 | 把改造方案落地的事实回灌到 docs/，让活资产闭环。 |
| 做什么 | 更新 api-list（把"开发中"改为"已上线（后端）"，入参返回结构按实际实现校对）；更新 data-model（新 DTO 的字段如有 review 中调整过的，同步更新，注意嵌套关系）；更新 solution（在每条改造点后面标注实际 commit hash 和文件路径，方便回溯）。 |
| AI 如何思考 | AI 容易照抄方案里的预期描述（比如"新增 dto/DiffItem.java"），但实际实现可能把 DiffItem 做成顶层 DTO 的静态内部类——文档要反映实际结构，不是照抄方案。 |
| 关注点 | 实际执行的细节要核对（比如 DiffFields 是 PromptVersionDiffResult 的内部类，不是独立顶层类）；docs/data-model 要把嵌套关系写清楚，不然会以为要新建四个文件（实际只有三个）。 |
| review 重点 | 输出每份文件的改动 diff；标注的实际 commit hash 和文件路径要可回溯。 |
| 技巧 | 文档标注要反映实际结构，不要照抄 solution.md 里的预期描述——这一步呼应本系列前面挖的 docs-auto-sync Skill，可以直接调用 Skill 跑这一步。 |

## 4. 可裁剪 Check List

<img src="imgs/aicmigr-19-dev-03-backend-development/e9d6c002236ae1724215bdf24c20b3f1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 4.1 改造前准备 Check List（动手前）

- [ ] 改造方案已审核定稿，每条改造点对应明确的代码改动
- [ ] 已识别需要复用的现有方法（如 `getByPromptKeyAndVersion`）
- [ ] 待复用方法已写 Characterization Test，断言基于"实际跑出来"
- [ ] Characterization Test 全过，建立了"行为不变"的硬指标基线
- [ ] 改造点已按依赖关系分批（如 P01-P03 / P04-P05 / P06 三批）
- [ ] 测试模块路径已确认（能访问到待测类和异常类的那一个）
- [ ] 项目测试基础设施已确认（有无 Testcontainers 决定能否用真实 DB）

### 4.2 改造执行 Check List（每批 commit 前必查）

- [ ] AI 是否只改了这一批的改造点（git status / git diff 看文件清单）
- [ ] 是否顺手重构了现有方法（让 AI 撤销）
- [ ] 新增代码是否对齐项目现有风格（统一返回结构、异常体系、命名约定）
- [ ] null 处理是否符合方案最终决策（如 `nullToEmpty` 语义）
- [ ] 新增方法是否有对应测试覆盖（没测试不算 Done）
- [ ] 断言是否凭"实际跑出来"写（review 时盯着 assertEquals 追问来源）
- [ ] Step 1 的 Characterization Test 是否全过（行为没偏移）
- [ ] mock 范围是否对（mock Mapper，不 mock 被测 Service 自身）
- [ ] 边界场景是否齐全（关键边界 E01/E02/E04 + happy path 都有）
- [ ] curl 验证是否由人来做（不轻信 AI 报告"接口跑通了"）
- [ ] mvn test 全套失败数为 0
- [ ] 总测试数 = 改造前基线 + 新增（无测试被意外删除或跳过）

### 4.3 文档回灌 Check List（提交后）

- [ ] api-list.md：新增接口已更新状态（"开发中" → "已上线（后端）"）
- [ ] api-list.md：入参和返回结构按实际实现校对
- [ ] data-model.md：新 DTO 字段如有 review 调整，已同步
- [ ] data-model.md：嵌套关系（如静态内部类）已写清楚
- [ ] solution.md：每条改造点已标注实际 commit hash 和文件路径
- [ ] solution.md：文档反映实际结构，未照抄方案预期描述
- [ ] CLAUDE.md：项目级约束已补入（个案决策留在 solution.md）

### 4.4 防翻车 Check List（贯穿全程）

- [ ] 每个提示词都明确写"不要重构现有方法"
- [ ] 每步 commit 前 git diff 看一遍，超出范围的改动一律撤销
- [ ] Characterization Test 作为最后兜底
- [ ] 提示词里写硬话："不要凭'应该是什么'写断言，凭'实际跑出来是什么'写"
- [ ] review 时看到 `assertEquals(...)` / `assertTrue(...)` 就追问"这个值/判断从哪来的"
- [ ] 测试失败时先怀疑测试，不要先怀疑代码（基于"实际"的断言失败是信号）

## 5. 案例背景：Prompt 版本 Diff 的后端落地

<img src="imgs/aicmigr-19-dev-03-backend-development/85a3ad4a055af38fc0daa6a7fa60b0f9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 5.1 本篇案例的起点

本篇接续第 18 篇的方案文档 `prompt-version-diff-solution.md`，把 P01-P06 这六个后端改造点在 Spring AI Alibaba Admin 项目里逐步落地。这一篇对应方案里的"动手写代码"环节，跑通后等待第 20 篇做前端联调。

改造目标回顾：把 Prompt 版本对比的"计算职责"从前端下沉到后端，新增 `GET /api/prompt/version/diff` 接口，由后端 `PromptVersionService.diffVersions` 统一拉取两个版本内容、计算字段级差异，并以结构化的 `PromptVersionDiffResult` 一次性返回。这次改造刻意不动数据库 schema——`prompt_version` 表保持原样，所有逻辑都在应用层完成。

### 5.2 本篇案例的两个关键约束点

#### (1) P05 要复用 `getByPromptKeyAndVersion`

第 18 篇方案的影响范围分析已经确认：`getByPromptKeyAndVersion` 只有 `log.info`，没有 metrics 副作用，不需要抽取 `getVersionInternal`。这意味着 P05 实现时直接调用这个老方法即可——但也意味着 AI 有"顺手优化"这个老方法的风险，必须用 Characterization Test 锁住。

#### (2) 测试基础设施的限制

项目当前没有 Testcontainers 基础设施，不能用 `@SpringBootTest` + 真实 DB。所有测试都得用 `@ExtendWith(MockitoExtension.class)` + Mockito mock Mapper。这个约束决定了 Step 1 和 Step 5 的测试写法，也决定了测试加在哪个模块——`PromptVersionServiceImpl` 和 `StudioException` 都在 server-start，server-core 没有依赖 server-start，无法访问这些类，所以测试必须加在 server-start 模块下。

## 6. 七步走实战复现

本节以 prompt-version-diff 为样本，逐步复现后端改造的七步落地。每一节都会引用原始提示词与 AI 的真实产出片段（测试输出、接口签名、测试报告），并着重解释"为什么这样提问""为什么产出长这样""review 时该盯哪些点"，而非简单罗列步骤。

### 6.1 Step 1：锁住改造前的行为（Characterization Test）

<img src="imgs/aicmigr-19-dev-03-backend-development/5360d708238fd4b6437b1f0292a207f5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

动手前先给 `getByPromptKeyAndVersion` 加 Characterization Test。这一步是第 15 篇心法在真实改造里的第一次落地，也是后面所有步骤的兜底机制。

提示词：

```
我要改造 PromptVersionServiceImpl，在改之前需要先用 Characterization Test
锁住 getByPromptKeyAndVersion 方法的现有行为。

要求：
- 不要凭"应该是什么"写断言，凭"实际跑出来是什么"写
- 先读 getByPromptKeyAndVersion 的实现，记录它实际做的事，再照实际行为写断言
- 测试覆盖两种场景：正常返回（版本存在）、版本不存在抛 StudioException
  （注意：该方法不做状态过滤，不要凭假设加"状态过滤"场景）
- 用 Mockito mock PromptVersionMapper，不依赖真实数据库
- 测试加在 spring-ai-alibaba-admin-server-start 模块下新建（原因：
  PromptVersionServiceImpl 和 StudioException 都在 server-start，server-core
  没有依赖 server-start，无法访问这些类）
  路径：src/test/java/.../admin/service/impl/PromptVersionServiceImplTest.java
- 跑命令：mvn test -pl spring-ai-alibaba-admin-server-start -am \
  -Dtest=PromptVersionServiceImplTest -Dsurefire.failIfNoSpecifiedTests=false

跑完汇报：测试覆盖了哪些场景、断言基于的实际值是什么、跑通的状态。
```

这个提示词很细——基本只有理解上一节课获得的改造点，才能理解这个提示词的意思。建议读者详细琢磨。老项目改造需要的是细心、细节和经验，而这些就是在琢磨中训练出来的。

为什么这个提示词要写得这么细？关键约束有三条，每一条都对应 AI 的一个隐性偏差：

#### (1) "不要凭'应该是什么'写断言"对应 AI 的业务直觉偏差

AI 看代码会自动脑补"应该"的行为，凭业务直觉补断言。这条硬话每次写测试相关提示词都要加，是挡住 AI 用"应该"代替"实际"的第一道关。

#### (2) "不要凭假设加'状态过滤'场景"对应 AI 的过度覆盖倾向

AI 看到方法名带 Version 就想加状态过滤、分页、排序等"常见场景"，但这个方法实际不做状态过滤。提示词显式禁止，避免 AI 凑出多余的、跑不通的测试。

#### (3) "测试加在 server-start 模块下"对应 AI 的路径迷茫

如果不给具体路径和原因，AI 可能把测试加到 server-core，结果编译不过——因为 server-core 不依赖 server-start。提示词把"为什么加在这里"写清楚，AI 一次就能加对位置。

产出：2 个 Characterization Test，全部通过。实际跑出来的两个场景和断言依据：

```
场景 1（版本存在）：mock Mapper 返回一个 PromptVersionDO，调用后拿到 PromptVersionDetail。断言依据 fromDO 的实际转换逻辑：createTime 由 LocalDateTime 经系统时区转 epoch ms，previousVersion 为 null 时返回 null（不是空字符串）。

场景 2（版本不存在）：mock Mapper 返回 null，断言抛出 StudioException，errCode == 404，errMsg == "Prompt版本不存在: no-key@v99"（消息格式从源码读出，不是猜的）。

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.573 s
BUILD SUCCESS
```

为什么这两个断言值得细看？因为它们都不是 AI 凭直觉写的：

- `createTime` 由 `LocalDateTime` 经系统时区转 epoch ms——这个转换逻辑是 `fromDO` 里实际写的，AI 是读了源码才知道的，不是猜的。
- `previousVersion` 为 null 时返回 null（不是空字符串）——这是 AI 容易凭直觉写成空字符串的地方，但实际行为是返回 null，断言必须反映实际。
- `errMsg == "Prompt版本不存在: no-key@v99"`——消息格式是从源码读出来的，包含具体的 key 和 version，不是 AI 凭模板猜的。

review 重点（最关键）：

#### (4) 断言是不是凭"实际"写的

打开测试文件看 `assertEquals(...)` 里的预期值。如果看到 AI 写 `assertEquals(100, result.getXxx())` 这种，追问"100 这个值是从哪来的？是跑现有代码跑出来的，还是你猜的？"。AI 经常会用业务直觉补断言，这是第 15 篇讲过的最大隐性偏差。

#### (5) 测试能跑通

如果有测试失败，先不要修测试，先确认是不是测试逻辑写错了。如果测试逻辑没问题但跑不过，那是开发者对现有行为的认知错了。这反而是 Characterization Test 的价值——让开发者看到代码"实际做什么"和开发者"以为它做什么"的差距。

### 6.2 Step 2：建 DTO（P01-P03）

<img src="imgs/aicmigr-19-dev-03-backend-development/2847131ee9b7a1530f4d88f38bf7edad_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

锁住现有行为后，正式开始改造。第一批是建三个 DTO：`PromptVersionDiffResult`、`VersionMeta`、`DiffItem`。

提示词：

```
基于 docs/requirements/prompt-version-diff-solution.md 的 P01-P03，
建三个 DTO 类：PromptVersionDiffResult、VersionMeta、DiffItem。

要求：
- 严格按 solution.md 第 7 节的最终决策（D1 null 视同空字符串等）
- 字段名、类型、注释和 solution.md 对齐
- 对齐项目现有 DTO 风格（lombok 注解、字段命名、null 处理）
- createTime 用 epoch ms（与现有 PromptVersionDetail.createTime 一致）
- 不要顺手改其他文件

只做 P01-P03 这三个 DTO，做完汇报，不要继续做 P04-P05。
```

最后一句"只做 P01-P03，不要继续 P04-P05"是关键约束。AI 默认会一口气改完，明确告诉它停在这一步。产出是 3 个新建的 DTO 文件（具体代码可查看代码仓库，这里就不展开细讲了），全部加 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`，字段对齐 solution.md 文件的内容。

为什么这条"只做这一批"的约束每一步都要写？因为 AI 有"显得能干"的倾向——看到方案文档列了 P01-P06 六个改造点，会默认全部做完汇报，让开发者觉得它效率高。但分批执行的真正价值是"出错可定位、回退可回滚"。如果 AI 一把梭改完六个改造点，某一处出错，开发者不知道是哪一步引入的；git diff 也很难精细回退。每批 review + commit 后再下一批，每一步都是干净的提交，回退和定位都简单。

review 重点：

#### (1) 字段是不是和 solution.md 对得上

打开 solution.md 第 3 节改造点表格 + 第 6 节决策点，逐字段对照 AI 写的 DTO。重点是 `createTime` 是否用 epoch ms（与现有 `PromptVersionDetail.createTime` 一致），null 处理是否符合 D1 决策。

#### (2) 有没有顺手改其他文件

git status 看一下，应该只有三个新建的 java 文件。如果发现 AI 还动了别的文件，让它解释为什么改，然后让它撤销不必要的改动。AI 的"顺手优化"哪怕看起来真的更好，也不要在这次改造里做——优化是另一个改造任务，单独走流程。

### 6.3 Step 3：实现 Service（P04-P05）

第二批是 Service 接口和实现。

提示词：

```
基于 solution.md 的 P04-P05，给 PromptVersionService 加 diffVersions 方法
+ 在 PromptVersionServiceImpl 里实现。

要求：
- 严格按 solution.md 步骤 2 的设计：调 Mapper 两次 → 校验 → 内存比较三字段 → 组装返回
- null 处理用 Objects.equals(nullToEmpty(a), nullToEmpty(b))，对应 D1 决策
- 复用 getByPromptKeyAndVersion 现有方法（18 讲 solution.md 影响范围第 2 条
  已确认该方法只有 log.info 无 metrics 副作用，不需要抽 getVersionInternal）
- 不要重构 getByPromptKeyAndVersion 任何细节，只调用它
- 异常用 StudioException + INVALID_PARAM/NOT_FOUND 错误码

实现完跑一遍 mvn test，确认 Step 1 的 Characterization Test 全部通过
（行为没偏移）。如果有测试失败，stop，告诉我具体是哪个测试、什么原因。

只做 P04-P05，不要做 P06。
```

注意提示词最后一段：让 AI 跑 Step 1 的 Characterization Test，有失败就 stop，这是兜底机制。Characterization Test 失败，说明改造意外破坏了现有行为，必须人介入判断。

为什么把"跑 Characterization Test"写进提示词而不是只靠开发者事后检查？因为这是把"行为不变"做成 AI 的硬约束。如果只靠开发者事后 git diff 看，AI 偷偷改了 `getByPromptKeyAndVersion` 的某个细节（比如调整空值处理），开发者可能看不出来——但 Characterization Test 会立刻失败。把测试跑通作为"完成 P04-P05"的前置条件，等于让 AI 自己卡住自己，不会带着失败的测试往下走。

这里实际有 3 个实现要点（执行后记录）：

```
diffVersions 先校验 versionA == versionB（抛 INVALID_PARAM），再查 promptMapper 确认 promptKey 存在（抛 NOT_FOUND），再两次调 Mapper 查版本（各自抛 NOT_FOUND），最后内存比较组装返回。

null 处理用局部变量 String sa = a!= null? a: ""，再 !Objects.equals(sa, sb) 判断 changed，等价于 nullToEmpty 语义，用标准库不引入额外依赖。

getByPromptKeyAndVersion 原方法零改动，Characterization Test 验证通过：
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

review 重点：

#### (1) 有没有动 `getByPromptKeyAndVersion`

git diff 看 `PromptVersionServiceImpl`，应该只有新增 `diffVersions` 方法以及两个私有辅助方法（`toVersionMeta`、`diffItem`），原方法一行不动。如果 AI 动了原方法（哪怕只是格式化），让它撤销。

#### (2) null 处理对不对

打开 `diffItem` 实现，确认空值处理是 `a != null ? a : ""` 后再 `Objects.equals` 比较，而不是 AI 凭直觉用的 `a == null && b == null`（后者语义完全不同）。这是 AI 最容易写错的地方——它会凭直觉认为"两个 null 就是没变"，但方案的 D1 决策是"null 视同空字符串"，两者在 valueA/valueB 字段的填充值上完全不同。

#### (3) Characterization Test 全过

这是硬指标。Step 1 的两个测试和改造前完全一致，证明 `getByPromptKeyAndVersion` 现有行为没被破坏。

### 6.4 Step 4：加 Controller（P06）

<img src="imgs/aicmigr-19-dev-03-backend-development/0241f362e4b5c5a83abc00b413022110_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

提示词：

```
基于 solution.md 的 P06，给 PromptController 加 GET /api/prompt/version/diff 接口。

要求：
- 三个入参：promptKey、versionA、versionB，全部 @RequestParam，加 @NotBlank
- 正常路径返回 Result.success(data)，对齐 PromptController 现有接口写法
- 异常处理走全局 GlobalExceptionHandler（@RestControllerAdvice），
  不要在 Controller 里 try-catch，不要自己包装错误响应
- 接口路径 /api/prompt/version/diff，注意路径不冲突（已确认现有
  /api/prompt/version 是单版本查询）
- 不要重构 PromptController 现有的其他接口

跑一遍 mvn test 确认全部通过（含 Step 1 的 Characterization Test）。
然后用 curl 跑一下新接口，看返回结构对不对。
只做 P06，不要继续做集成测试，那是下一步。
```

产出：`PromptController` 新增一个方法，import 新增 `PromptVersionDiffResult`，其余接口零改动。Characterization Test 继续全过。

实际加进去的接口签名：

```java
@GetMapping("/prompt/version/diff")
public Result<PromptVersionDiffResult> diffPromptVersions(
        @RequestParam @NotBlank String promptKey,
        @RequestParam @NotBlank String versionA,
        @RequestParam @NotBlank String versionB) throws StudioException {
    log.info("对比Prompt版本差异请求: promptKey={}, versionA={}, versionB={}", promptKey, versionA, versionB);
    return Result.success(promptVersionService.diffVersions(promptKey, versionA, versionB));
}
```

```
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

为什么提示词要明确"不要在 Controller 里 try-catch"？因为项目有全局 `@RestControllerAdvice`（`GlobalExceptionHandler`），统一处理 `StudioException` 并包装成标准的错误响应结构。如果 AI 在 Controller 里自己 try-catch + 包装错误响应，会出现两套错误响应结构——一套是全局的，一套是这个 Controller 自己的，前端联调时不知道按哪套解析。提示词把这一条写死，强制 AI 走全局异常处理，保持错误响应结构统一。

review 重点：

#### (1) 接口签名对

和 solution.md 第 3 节里 P06 的描述完全一致：三个 `@RequestParam` + `@NotBlank`，返回 `Result<PromptVersionDiffResult>`。

#### (2) 没有重构其他接口

git diff `PromptController.java`，应该只有新增方法 + 一行 import，其余内容一字不动。

#### (3) curl 返回结构对

手动 curl 一下，看返回 JSON 结构和 solution.md 第 3 节的接口契约对得上。这一步人来做：AI 报告"接口跑通了"不一定可信，自己跑一次最稳。

### 6.5 Step 5：补单元测试 + curl 验证返回结构

新接口跑通了，分两部分：先补 Service 层单元测试，再 curl 验证真实 HTTP 响应结构。

两者不能互相替代：单元测试验证 Service 逻辑正确，curl 验证序列化到 JSON 的结构对得上接口契约。JSON 字段名拼错、类型序列化异常这类问题单元测试发现不了。

提示词（单元测试）：

```
给 diffVersions 补单元测试，测试加在 server-start 模块下（原因同 Step 1：
PromptVersionServiceImpl 和 StudioException 在 server-start，server-core 无法访问）
PromptVersionServiceDiffTest.java（如果不存在就新建）。

注意：项目当前没有 Testcontainers 基础设施，不能用 SpringBootTest + 真实 DB。
用 @ExtendWith(MockitoExtension.class) + Mockito mock PromptVersionMapper 和 PromptMapper
（diffVersions 内部调了两个 Mapper，两个都要 mock）。

覆盖需求文档 prompt-version-diff.md 第 4 节的关键边界：
* E01 versionA == versionB → 抛 StudioException(INVALID_PARAM)
* E02 versionA 不存在 → 抛 StudioException(NOT_FOUND)，errMsg 含版本号
* E04 template 为 null → valueA/valueB 返回 ""，changed=false（两个都是空字符串）
* 正常 happy path（两版本 template 不同 → changed=true，variables 相同 → changed=false）

测试断言凭"实际跑出来是什么"写，不凭"应该是什么"。

跑命令：mvn test -pl spring-ai-alibaba-admin-server-start -am \
  -Dtest=PromptVersionServiceDiffTest -Dsurefire.failIfNoSpecifiedTests=false

汇报每个测试覆盖的场景和实际跑通的状态。
```

产出：4 个单元测试，全部通过。

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.601 s
BUILD SUCCESS
```

四个场景的实际验证结果：

```
E01：diffVersions("key","v1","v1") → 抛 StudioException，errCode=400，无需查 DB
E02：mock Mapper 返回 null for versionA → 抛 StudioException，errCode=404，errMsg 包含版本号 "v1"
E04：两版本 template 均为 null → valueA=""、valueB=""、changed=false（空字符串相等）
happy path：template 不同 → changed=true；variables 相同 → changed=false；promptKey/version/status字段值与 mock 数据一致
```

为什么这四个边界值得覆盖？因为它们对应了 `diffVersions` 的三个核心决策点：

- E01 对应"参数校验顺序"——先校验 `versionA == versionB`，避免无意义的 DB 查询。
- E02 对应"版本不存在的错误码"——`NOT_FOUND` 而不是 `INVALID_PARAM`，错误码语义要准确。
- E04 对应 D1 决策"null 视同空字符串"——两个版本 template 都为 null 时，valueA/valueB 都是 `""`（不是 null），changed 是 false（两个空字符串相等）。这条最容易写错，是 D1 决策的真实落地。
- happy path 对应"内存比较逻辑"——template 不同 changed=true，variables 相同 changed=false，证明三字段分别比较、互不影响。

review 重点：

#### (1) 断言基于实际行为

看到 `assertEquals(...)` 追问 AI"这个值是跑出来的还是猜的"。Mockito 测试里 AI 容易直接 `when(...).thenReturn(mock对象)`，然后凭直觉写断言，而不是先想"这个 mock 会让 diffVersions 实际算出什么"。

#### (2) 边界场景齐全

E01/E02/E04 + happy path 四条都有，少一条让 AI 补。

#### (3) Mockito mock 范围对

mock `PromptVersionMapper` 和 `PromptMapper`，不要 mock `PromptVersionService` 自身，测的是真实 `diffVersions` 实现，不是 mock 出来的壳。

curl 验证（人来做）：

启动应用后，用真实数据库里已有的两个版本手动 curl，看实际返回的 JSON 结构和接口契约对不对：

```bash
TOKEN=$(curl -s \
  -H "Content-Type: application/json" \
  -d '{"username":"saa","password":"123456"}' \
  http://localhost:8080/api/auth/login \
  | jq -r '.data.access_token')

curl -s \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/prompt/version/diff?promptKey=xxx&versionA=v1&versionB=v2" \
  | jq
```

预期返回结构（对照 solution.md 第 2 节接口契约）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "promptKey": "xxx",
    "versionA": { "version": "v1", "status": "release", "createTime": 1745000000000 },
    "versionB": { "version": "v2", "status": "pre", "createTime": 1745100000000 },
    "diffs": {
      "template": { "changed": true, "valueA": "...", "valueB": "..." },
      "variables": { "changed": false, "valueA": "...", "valueB": "..." },
      "modelConfig": { "changed": false, "valueA": "...", "valueB": "..." }
    }
  }
}
```

curl 这步不要让 AI 代劳：AI 报告"接口跑通了"不可信，自己眼睛看到 JSON 结构才算验证完。重点盯三件事：

##### ① data 字段存在（不是 null）

如果 data 是 null，说明 Controller 返回结构有问题，或者序列化失败。

##### ② diffs 下三个字段都有（template / variables / modelConfig）

少一个字段说明 DTO 结构和接口契约不一致，可能是漏了字段或者字段名拼错。

##### ③ changed 是 boolean，valueA / valueB 是字符串（不是 null）

如果 changed 是字符串 `"true"` 或者 valueA 是 null，说明类型序列化有问题，或者 D1 决策没落地（valueA 应该是空字符串不是 null）。

### 6.6 Step 6：跑通 mvn test 全套

<img src="imgs/aicmigr-19-dev-03-backend-development/48f5dfecf68937dbe97d6a450afcdf3a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

到这一步所有后端改造点（P01-P06）跑完了。最后跑一遍完整 mvn test，确认整体没问题。

提示词：

```
跑一遍完整测试，含新增的 server-start 测试：

mvn test -pl spring-ai-alibaba-admin-server-runtime,spring-ai-alibaba-admin-server-core,spring-ai-alibaba-admin-server-start \
  -am -Dsurefire.failIfNoSpecifiedTests=false -fae

输出全部测试结果（通过 / 失败 / 跳过 各多少）。
失败的列出来，但不要试图修，只汇报。
```

为什么提示词明确"不要试图修，只汇报"？因为 AI 看到 failing test 会自己上手改，最常见的是把测试改成"全过"而不是修代码——比如把 `assertEquals(expected, actual)` 改成 `assertEquals(actual, actual)`，测试永远过但根本没验证任何东西。提示词明确"只汇报"，让开发者拿到失败的清单后自己判断——是测试错了还是代码错了。

产出：完整测试报告，0 失败。

```
server-core:
Tests run: 14, Failures: 0, Errors: 0, Skipped: 0

server-start（新增）:
Tests run: 4, Failures: 0, Errors: 0
Tests run: 2, Failures: 0, Errors: 0
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

总计: 20 个测试，0 失败，BUILD SUCCESS
```

<img src="imgs/aicmigr-19-dev-03-backend-development/c4c17554c7b920b48d8509d359498bb4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/19_项目开发_03：后端开发/c4c17554c7b920b48d8509d359498bb4_MD5.jpg
用途：Step 6 跑通 mvn test 全套的最终测试报告截图，展示 0 失败的完整结果
内容：完整测试报告显示 server-core 14 个测试全过（改造前基线），server-start 新增 6 个测试全过（2 个 Characterization Test + 4 个 diffVersions 单元测试），合计 20 个测试 0 失败，BUILD SUCCESS。证明后端改造完成且未破坏现有行为。
-->

review 重点：

#### (1) 失败数为 0

任何失败都不能进下一步。

#### (2) Step 1 的 Characterization Test 全过

`PromptVersionServiceImplTest` 2 个测试和改造前完全一致，证明 `getByPromptKeyAndVersion` 现有行为没被破坏。

#### (3) 总测试数 = 改造前 + 新增

改造前基线 14 个（server-core），新增 6 个（server-start：2 个 Characterization Test + 4 个 `diffVersions` 单元测试），合计 20 个。如果总数不对，说明有测试被意外删了或跳过了——这是 AI 偷偷删测试的常见信号，必须追查。

### 6.7 Step 7：提交 + 文档自动更新

最后一步：把改造方案落地的事实回灌到 docs/。

提示词：

```
后端改造跑通了（P01-P06 + 测试）。更新相关 docs/ 资产：

1. docs/api-list.md：
   把之前标"开发中"的 GET /api/prompt/version/diff 改为"已上线（后端）"
   入参和返回结构按实际实现校对一遍

2. docs/data-model.md：
   三个新 DTO 的字段如有任何 review 中调整过的，同步更新

3. docs/requirements/prompt-version-diff-solution.md：
   在每条改造点（P01-P06）后面标注实际 commit hash 和文件路径，方便回溯

输出每份文件的改动 diff。
```

产出：三份文档同步更新。

实际执行时有一个细节要核对：`PromptVersionDiffResult` 里用了静态内部类 `DiffFields`，而不是独立顶层类。docs/data-model.md 里要把这个嵌套关系写清楚，不然会以为要新建四个文件（实际只有三个）。

另外 solution.md 里第 3 节写的是"P03 新增 dto/DiffItem.java"，但实际实现里没有独立的 `DiffFields.java`，`DiffFields` 是 `PromptVersionDiffResult` 的内部类。文档标注时要反映这个实际结构，不要照抄 solution.md 里的预期描述。

为什么这一步要强调"反映实际结构，不要照抄方案"？因为方案是改造前的预期，文档是改造后的现实，两者会有差异。最常见的差异是：

#### (1) 嵌套关系变了

方案写"独立顶层类"，实际实现里发现"静态内部类"更紧凑（比如 `DiffFields` 只服务于 `PromptVersionDiffResult`，没必要独立成顶层类）。文档要反映这个实际选择。

#### (2) 文件数变了

方案列了 4 个文件（3 个 DTO + 1 个 DiffFields），实际只有 3 个文件（`DiffFields` 是其中一个 DTO 的内部类）。文档要把嵌套关系写清楚，不然下次读文档的人会以为漏建了一个文件。

#### (3) 字段名/类型微调

review 中调整过的字段（比如把 `Long` 改成 `long`、把 `String` 改成更具体的类型）要同步到 data-model.md，否则下次改造会基于过时的字段定义做决策。

这一步呼应本系列前面挖的 docs-auto-sync Skill——可以直接调用 Skill 跑这一步，效果一样。跑完这一步，开发者手里所有 docs/ 资产被这一轮深度思考反向丰富了一轮。

## 7. 怎么避免最常见的翻车

<img src="imgs/aicmigr-19-dev-03-backend-development/7f5b8ea6b7bb65009b4412fdacad8dcf_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

后端改造里两个翻车点贯穿全程，必须特别警惕。两个翻车点背后是同一个心法：AI 在数据 / 代码层面强，在判断 / 直觉层面弱。这一篇（执行改造）和第 15 篇（补测试）、第 17 篇（拆需求）、第 18 篇（拆方案）背后都是这一条。

### 7.1 翻车一：AI 顺手优化老代码

最典型的场景：开发者让 AI 加 `diffVersions` 方法复用 `getByPromptKeyAndVersion`，AI 改完一看代码"这个方法可以重构得更优雅"，顺手就改了。

防止办法有三层：

#### (1) 每个提示词里明确写"不要重构现有方法"

这一篇所有提示词都加了这一条。这是第一道关，挡住 AI 的"顺手优化"倾向。

#### (2) 每步 commit 前 git diff 看一遍，超出范围的改动一律撤销

AI 的优化哪怕看起来真的更好，也不要在这次改造里做。优化是另一个改造任务，单独走流程。这一层是开发者主动把关，不依赖 AI 的自觉。

#### (3) Characterization Test 是最后兜底

哪怕 AI 偷偷改了什么，只要现有行为没变，测试会过。如果测试失败，立刻知道行为变了。这一层是最硬的保障，不依赖提示词也不依赖开发者的注意力。

### 7.2 翻车二：AI 用"应该"而不是"实际"写测试断言

第 15 篇讲过的隐性偏差，这一篇是它在真实改造里的具体表现。

最典型的场景：AI 写 Characterization Test 时，看代码 `if (result == null) return Collections.emptyList()`，凭直觉写 `assertNotNull(result)`，但实际跑代码可能因为业务数据导致返回 null，于是测试反而失败。

防止办法有三层：

#### (1) 提示词里写硬话

不要凭"应该是什么"写断言，凭"实际跑出来是什么"写——这句话每次写测试相关提示词都要加。

#### (2) review 时盯着断言看

看到 `assertEquals(...)`、`assertTrue(...)` 就追问"这个值 / 这个判断是从哪来的"。

#### (3) 测试失败时先怀疑测试，不要先怀疑代码

如果测试断言是基于"实际"写的，那它失败就说明代码确实变了，这是有用的信号。如果断言是基于"应该"写的，那测试失败可能只是 AI 猜错了，真正破坏的代码反而没被检测到。

## 8. 最终产出与小结

<img src="imgs/aicmigr-19-dev-03-backend-development/7d42cc321a0c31679ef64e5707996567_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 8.1 最终产出清单

整轮跑完，开发者手上的产出：

- 三个新 DTO 文件（`PromptVersionDiffResult` 含静态内部类 `DiffFields`、`VersionMeta`、`DiffItem`）。
- Service 接口新增 `diffVersions` 方法签名 + 实现类新增实现（含两个私有辅助方法 `toVersionMeta`、`diffItem`），原方法 `getByPromptKeyAndVersion` 零改动。
- Controller 新增 `GET /api/prompt/version/diff` 接口，异常处理走全局 `GlobalExceptionHandler`。
- 测试资产：2 个 Characterization Test（锁住 `getByPromptKeyAndVersion` 现有行为）+ 4 个 `diffVersions` 单元测试（覆盖 E01/E02/E04 + happy path）。
- mvn test 全套 0 失败，总测试数 20（改造前 14 + 新增 6）。
- 三份文档同步更新（api-list.md / data-model.md / solution.md），反映实际实现结构。

### 8.2 核心要点回顾

这一篇的核心就一句话：让 AI 小步改后端、开发者严格 review、Characterization Test 兜底。

本篇的内容整体分为七步走：

1. 锁住改造前的行为（Characterization Test）
2. 建 DTO（P01-P03）
3. 实现 Service（P04-P05）
4. 加 Controller（P06）
5. 补单元测试 + curl 验证返回结构
6. 跑通 mvn test 全套
7. 提交 + 文档自动更新

四个原则贯穿七步：小步执行、自主修复 + 3 次兜底、复用现有结构、补测试不补到位不算完成。

最容易翻车的两点：AI "顺手"优化老代码（每步 git diff 兜底）、AI 用"应该"写断言（提示词硬约束 + review 时盯着断言）。

### 8.3 慢就是快

跑完这一篇，后端可运行（20 个测试全过）、有测试覆盖（Characterization Test + `diffVersions` 单元测试）、不破坏现有行为（Characterization Test 改造前后结果一致），commit 后等前端联调。

读者可能会有疑问：如果是我，我一步、一个提示词就搞定了，为什么要分这么多步？

这就是新项目开发和老项目开发的区别。老项目开发需要步步为营，一步一步来，越细致越好。这么细不是浪费时间——出 bug 处理的时间、返工的时间，比做的时候因为细致花的时间多得多。所以老项目改造，慢就是快。别急，细致点，按照流程步骤来，反而会觉得真的很快，比想象中快。

本系列下一篇做前端：接入新接口、改造 `VersionCompareModal`、加 loading 状态、和后端联调。

## 9. 思考

### 9.1 两个反思问题

<img src="imgs/aicmigr-19-dev-03-backend-development/63385906ec270c084fd654379457161a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) AI 顺手优化的真实经历

开发者最近一次让 AI 改老代码，有没有遇到 AI 顺手优化的情况？如果当时有 Characterization Test 兜底，会不会发现得更早？

#### (2) 七步走里哪一步最反直觉

在这一篇的七步走里，读者觉得哪一步最反直觉？是 Step 1（改之前先写测试）、Step 3（不要重构 `getByPromptKeyAndVersion`）、还是 Step 5（断言凭实际不凭应该）？为什么？

读者可以结合自己的实际场景思考每一步的反直觉点：对于习惯 TDD 的开发者，Step 1 最自然；对于习惯"顺手优化"的开发者，Step 3 最反直觉；对于习惯凭业务直觉写断言的开发者，Step 5 最反直觉。识别自己最反直觉的那一步，就是识别自己最容易翻车的那一步。
