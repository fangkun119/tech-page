---
title: 传统项目迁AI 19：项目开发 - 后端开发
author: fangkun119
date: 2026-07-04 19:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
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

## 1. 方案到手，为什么不能让 AI 一把梭

方案审核通过，到了动手写代码这一步。开发者手里拿着一份定稿的改造方案，第一反应往往是：把方案丢给 Claude Code，让它一口气改完，跑通就行。

这一篇先劝住你。<span style="color: red; font-weight: bold;">同样的提示词，在新项目里跑得很好；在老项目里，几乎一定会翻车。原因不在 AI 能力，而在老项目的特殊性</span> —— 每一行被 AI 顺手改掉的代码背后，都可能挂着线上调用方。本章先把两个翻车风险讲透，再用两个贯穿全文的认知锚点，回答"为什么必须慢"。

### 1.1 老项目改造最大的两个风险点

执行阶段最大的风险来自 AI 的两个默认行为。单看都很合理，进了老项目就埋雷到生产。这两类坑不在提示词里硬约束，就会一路埋到线上。下面就是一个例子。

<img src="imgs/aicmigr-19-dev-03-backend-development/c2976f933bdf5afb28366bd50545c185_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) AI 爱"顺手"优化老代码

场景很典型：你让 AI 在 Service 里加一个 `diffVersions` 方法，复用现有的 `getByPromptKeyAndVersion`。<span style="text-decoration: underline;">AI 改完一看，觉得老方法"可以重构得更优雅" —— 顺手调整了返回类型，或改了空值处理</span>。

AI 为什么会这样做？因为它的训练目标是"写得更好"，不是"写得一样"。<span style="color: red; font-weight: bold;">没有硬约束时，它默认把每一处能改的地方都改成业界最佳实践。这种倾向在新项目里是优点，在老项目里就是事故源。</span>

后果是链式的：<span style="color: red; font-weight: bold;">老方法的行为一变，所有调用方全部受影响</span>。调用方不在你这次改造的文件清单里，可能跨模块、跨服务，甚至是你根本不知道的下游脚本。<span style="color: red; font-weight: bold;">改造质量的差异，往往就藏在这种很细的细节里。</span>这也是为什么必须在每一步停下来，确认 AI 的每一处调整。

#### (2) AI 爱"应该"写测试断言

场景同样典型：AI 写 Characterization Test 时，盯着代码 `if (result == null) return Collections.emptyList()`，凭业务直觉补上 `assertNotNull(result)`。

"应该"和"实际"在这里分道扬镳。<span style="color: red; font-weight: bold;">AI 看到方法签名带 Version、带返回值，脑补出"应该返回非空集合"；但真实业务数据完全可能让方法走到 null 分支</span> —— 代码写得清清楚楚，AI 却没跑过，只是"觉得应该是这样"。<span style="color: red; font-weight: bold;">于是断言全过只是假象，真实数据一跑，测试反而失败。</span>

后果比第一种更隐蔽：<span style="color: red; font-weight: bold;">测试通过给了开发者虚假的安全感，以为现有行为被锁住了，其实锁的是 AI 的脑补。</span><span style="color: red; font-weight: bold;">这种偏差一旦流到后续步骤，AI 改坏了代码两星期后才被发现，定位成本极高</span>。

### 1.2 新项目 vs 老项目：慢就是快

<img src="imgs/aicmigr-19-dev-03-backend-development/2288f2e0110879eb7eac30779a1815bc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

你可能会问：如果是新项目，同样的需求一个提示词 Claude Code 就能搞定，为什么要分这么多步？

这是新项目和老项目的本质区别。把两者放在一张表里对比，差异立刻清晰：

| 维度      | 新项目            | 老项目                                         |
| ------- | -------------- | ------------------------------------------- |
| 现有行为    | 没有，一切从零写       | 已在线上跑，每个方法背后都挂着调用方                          |
| AI 翻车后果 | 改坏了重写一遍，影响范围可控 | 改坏了就是事故，调用方可能在你看不到的地方                       |
| 安全网     | 不需要兜底，迭代试错成本低  | 必须步步为营，越细致越好                                |
| 正确节奏    | 一个提示词一把梭       | <span style="color: red; font-weight: bold;">小步改</span> + <span style="color: red; font-weight: bold;">人严格 review</span> + <span style="color: red; font-weight: bold;">Characterization Test 兜底</span> |

<span style="color: red; font-weight: bold;">老项目改造里，细致不是浪费时间。</span><span style="color: red; font-weight: bold;">出 bug 的处理时间、返工的回滚时间，比做的时候因为细致花掉的时间多得多</span>。<span style="color: red; font-weight: bold;">慢一点、按步骤来，反而比想象中更快——这是贯穿全文的基本盘。</span>

既然不能一把梭，那该用什么打法？这里给出两个贯穿后续所有章节的认知锚点，先把它们和传统软件工程里熟悉的概念对齐：

**① spec ≈ 接口契约**

<span style="color: red; font-weight: bold;">AI 改代码必须严格对齐方案 spec，就像传统开发里实现必须对齐接口契约一样 —— 脱离 spec 自由发挥，哪怕代码更优雅，也是违约。</span>方案文档里每一条改造点、每一个字段定义，都是约束 AI 的"契约"。

**② Characterization Test ≈ 回归测试基线 / 金标准**。

改造前先跑一遍现有代码，把真实输入输出记下来作为断言；改造后再跑一遍，断言通过就证明现有行为没变。<span style="color: red; font-weight: bold;">这套测试不测代码"应该做什么"，而是锁住代码"现在实际做什么"</span>——<span style="color: red; font-weight: bold;">它就是老项目改造里的回归测试基线，是发现 AI 偷偷改坏行为的金标准</span>。

两个锚点合在一起，回答了本章标题的问题：方案到手不能让 AI 一把梭，是因为 AI 既可能脱离"接口契约"自由发挥，也可能用脑补替换"回归测试基线"。后续四个原则、七步走框架，本质上就是围绕这两个锚点建立的防护网。

方法论见下一章——四个原则 + 七步走框架。

## 2. 让 AI 对齐验证锚点：四个原则与七步走

第 1 章落下了两个锚点：spec 充当接口契约，Characterization Test 充当回归测试基线。锚点解决的是"拿什么验证"，但 <span style="color: red; font-weight: bold;">AI 在改造过程中不会自动对齐锚点 —— 它可能一次改一大片、可能在新结构上自创风格、可能改完不补测试。</span>要让它始终贴着锚点走，还需要一套执行方法论。

本章是这套方法论的全景索引：四个原则定方向，七步走框架定动作。每一步的 why 留给实战章，这里只做速查。

### 2.1 四个原则一览

<img src="imgs/aicmigr-19-dev-03-backend-development/0e9f9b97220a2dd06b89fe5f8db86824_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

四个原则本质就一句话：<span style="color: red; font-weight: bold;">让 AI 走小步、走对方向、能验证</span>。它们贯穿后面每一步改造，是给两个锚点装上的"防护网"。

| **原则**           | 一句话说明                                                                                                                    | 防什么坑                                                                               |
| ------------ | ------------------------------------------------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------- |
| **小步执行**         | 按改造点分批：P01-P03 一批、P04-P05 一批、P06 一批，<span style="color: red; font-weight: bold;">每批跑通 review + commit 再下一批</span>        | 改完出错<span style="color: red; font-weight: bold;">不知是哪一步出错，回退</span>也不好回            |
| **自主修复 + 3 次兜底** | 编译报错/测试失败/依赖冲突 AI 自己修自己重试，<span style="color: red; font-weight: bold;">连续 3 次同一错误必须停下来问人</span>                          | AI 在同一坑里反复试错，烧 token 还修不对，<span style="color: red; font-weight: bold;">越改越偏</span> |
| **复用现有结构**       | <span style="color: red; font-weight: bold;">对齐项目既有代码风格</span>（`Result<T>`、JPA `@Table`、`StudioException`），不另起一套"业界最佳实践" | AI 写出<span style="color: red; font-weight: bold;">风格割裂的代码</span>，后期维护成本高           |
| **补测试不补到位不算完成**  | 每个改造点跑完都要有<span style="color: red; font-weight: bold;">对应测试</span>，没测试的改造点不算 Done                                        | 改造点跑通但无测试，下次改坏无人知晓，<span style="color: red; font-weight: bold;">回归</span>靠人工                                                           |

<span style="color: red; font-weight: bold;">四条原则不是平行清单，而是相互咬合</span>：小步执行让"出错可定位、回退可回滚"，给自主修复圈出安全边界；自主修复把人的精力留到 review，而不是替 AI 收拾试错；复用现有结构降低 review 难度，人能一眼看出"是否超出本批范围"；补测试不补到位不算完成则把每个小步的"完成"做成可验证的硬指标。

四者合起来，正好对应到第 1 章的两个锚点上 —— 复用结构守住 spec（接口契约），补测试守住 Characterization Test（回归测试基线），小步与兜底则负责把这两条防线分摊到每一步。

### 2.2 七步走框架与原则的对应

<img src="imgs/aicmigr-19-dev-03-backend-development/3f364dc310280d956f2c105ba6d4a61f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

四个原则如何落地？通过七步走。

七步走是把四个原则拆成具体动作的执行链路：从锁住行为开始，到提交文档结束，每一步都明确调用哪条原则。

| 步骤     | 动作                                                                                                                          | 主要应用的原则                     | 这一步在做什么                                 |
| ------ | --------------------------------------------------------------------------------------------------------------------------- | --------------------------- | --------------------------------------- |
| Step 1 | <span style="color: red; font-weight: bold;">锁住改造前的行为</span>（Characterization Test）                                         | 补测试不补到位不算完成                 | 改造前先记录待复用方法的实际行为，建立"行为不变"的硬指标基线         |
| Step 2 | 建 <span style="color: red; font-weight: bold;">DTO</span>（P01-P03）                                                          | 小步执行 / 复用现有结构               | 按方案第一批改造点建 DTO，字段、注释、lombok 风格与方案和项目对齐  |
| Step 3 | 实现 <span style="color: red; font-weight: bold;">Service</span>（P04-P05）                                                     | 小步执行 / 复用现有结构 / 补测试不补到位不算完成 | 实现 Service，复用老方法不重构，跑 Step 1 测试作为兜底     |
| Step 4 | 加 <span style="color: red; font-weight: bold;">Controller</span>（P06）                                                       | 小步执行 / 复用现有结构               | 对外暴露接口，走全局异常处理，不自行 try-catch            |
| Step 5 | 补<span style="color: red; font-weight: bold;">单元测试</span> + <span style="color: red; font-weight: bold;">curl 验证</span>返回结构 | 补测试不补到位不算完成                 | Mockito 单元测试验证逻辑 + 真实 curl 验证 HTTP 返回结构 |
| Step 6 | 跑通 <span style="color: red; font-weight: bold;">mvn test 全套</span>                                                          | 自主修复 + 3 次兜底 / 补测试不补到位不算完成  | 完整跑一次 `-fae`，3次修复失败后AI由人接入，最终保证 0 失败    |
| Step 7 | <span style="color: red; font-weight: bold;">提交</span> + 文档自动更新                                                                                                                 | 复用现有结构                      | 把落地事实回灌 docs/，反映实际结构而非照抄方案预期            |

下面这张总览图就是这条链路的可视化导航——Step 1 到 Step 7 的动作、对应的原则，一眼可见。七步的具体提示词、AI 产出与 review 重点，在实战章逐一展开。

<img src="imgs/aicmigr-19-dev-03-backend-development/f24e9bcf94014aa8369711004014384f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
<!--
图片内容说明
路径：imgs/19_项目开发_03：后端开发/f24e9bcf94014aa8369711004014384f_MD5.jpg
用途：后端开发七步走总览图，展示从锁住行为到提交文档的完整执行链路
内容：Step 1 锁住改造前的行为（Characterization Test）→ Step 2 建 DTO（P01-P03）→ Step 3 实现 Service（P04-P05）→ Step 4 加 Controller（P06）→ Step 5 补单元测试 + curl 验证返回结构 → Step 6 跑通 mvn test 全套 → Step 7 提交 + 文档自动更新。整条链路贯穿四个原则：小步执行、自主修复 + 3 次兜底、复用现有结构、补测试不补到位不算完成。
-->

## 3. 实战：Prompt 版本 Diff 的后端落地

方法论讲了四个原则和七步走，但读者真正卡住的地方不是记不住原则，而是不知道每一步具体长什么样：提示词该怎么写？AI 产出是什么形态？review 要盯哪里？这一章用一条真实案例把七步完整跑一遍，每一步的提示词原文、AI 产出、终端输出、review 重点全部展开，照着就能复现。

案例选自方案文档 `prompt-version-diff-solution.md` 里的 P01-P06 六个后端改造点。目标是把 Prompt 版本对比的"计算职责"从前端下沉到后端：

- 新增 `GET /api/prompt/version/diff` 接口
- 由后端 `PromptVersionService.diffVersions` 统一拉取两个版本、算字段级差异、一次性返回结构化的 `PromptVersionDiffResult`。

用传统软件工程的话说，相当于把一段散落在前端的行为收拢成一个有契约保证的后端服务。这次改造刻意不动数据库 schema，`prompt_version` 表保持原样，所有逻辑都在应用层。

### 3.1 测试代码位置与两个关键约束

<img src="imgs/aicmigr-19-dev-03-backend-development/52aa10404e9a8d717b24539cbb6300af_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">
为了防止阅读后文时、对一些决策不理解，本节先补充两块背景信息。

#### (1) 测试代码位置

**为什么测试要加在 server-start 而不是 server-core？**

因为 `PromptVersionServiceImpl` 和 `StudioException` 都在 server-start 模块下，而 server-core 并不依赖 server-start，根本访问不到这两个类。测试加到 server-core，编译都过不了。这是老项目带来的现实约束。看起来是路径选择问题，实际上决定了后面 Step 1 和 Step 5 所有测试的写法和落点。

#### (2) 关键约束

正式动手前还有第二个约束同样关键，直接决定改造能不能安全落地。

##### ① P05 要复用 `getByPromptKeyAndVersion`

方案文档的影响范围分析已经确认：`getByPromptKeyAndVersion` 只有 `log.info`，没有 metrics 副作用，不需要抽取 `getVersionInternal`。P05 实现时直接调用这个老方法即可。

但"复用"二字背后藏着一个坑 —— AI 看到"复用老方法"会本能地想顺手把它重构得更优雅：改个返回类型、加个参数、调整空值处理。这些改动单看都合理，一旦现有行为变了，所有调用方都会被波及。

这就是 <span style="color: red; font-weight: bold;">Characterization Test（特征化测试）必须在动手前先写的原因</span>。它相当于给老方法拍一张"行为快照"，用一套断言把 `getByPromptKeyAndVersion` 的现有行为锁死，后面任何一步只要这套测试红了，立刻知道行为被动了。与传统回归测试的差别在于：回归测试断言"应该是什么"，特征化测试断言"现在实际是什么"——哪怕现在实现里有 bug，也先锁住，改造只保证行为不变，不顺手修 bug。

##### ② 项目没有 Testcontainers，测试只能 Mockito + 加在 server-start

项目当前没有 Testcontainers 基础设施（数据库Docker容器等），不能用 `@SpringBootTest` 配真实 DB。所有测试都得走 `@ExtendWith(MockitoExtension.class)` + Mockito mock Mapper 这条路。

这条约束也是老项目带来的现实约束，它贯穿 Step 1 和 Step 5 的提示词，也是前面强调测试必须落在 server-start 的根因。

两个约束记下来，下面进入 Step 1。

### 3.2 Step 1：锁住改造前的行为（Characterization Test）

这一步的作用：动手改之前，先给 `getByPromptKeyAndVersion` 加 Characterization Test，锁住它的现有行为，<span style="color: red; font-weight: bold;">建立改造前后"行为不变"的硬指标</span>。这是特征化测试技术在真实改造里的第一次落地，也是后面所有步骤的兜底机制。

#### (1) 提示词

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

这条提示词细到几乎只有<span style="color: red; font-weight: bold;">读过方案文档的人才能完整理解</span>。<span style="color: red; font-weight: bold;">这种密度不是啰嗦，是老项目改造的必需品</span>。可以把提示词类比成给新人的需求说明单 —— 边界写得越死，新人（AI）越没有脑补空间。关键约束有三条，每一条都对应 AI 的一个隐性偏差：

| 提示词硬约束                   | 挡住的 AI 隐性偏差                                                                                                                       | 不写会怎样                       |
| ------------------------ | --------------------------------------------------------------------------------------------------------------------------------- | --------------------------- |
| <span style="color: red; font-weight: bold;">"不要凭'应该'写断言，凭'实际'写"</span>      | AI 看代码会自动脑补<span style="color: red; font-weight: bold;">应该</span>的行为，<span style="color: red; font-weight: bold;">凭业务直觉补断言</span> | 测试全过但根本没验证现有代码，改造后行为变了也测不出来 |
| <span style="color: red; font-weight: bold;">"不要凭假设加'状态过滤'场景"</span>         | AI 看到方法名带 Version 就想加<span style="color: red; font-weight: bold;">分页、排序、状态过滤等凭空脑补的"常见场景"</span>                                   | 凑出一堆跑不通的多余测试，反而模糊真正的行为基线    |
| <span style="color: red; font-weight: bold;">"测试加在 server-start，路径给出"</span> | AI 不知道模块依赖方向，会<span style="color: red; font-weight: bold;">乱放位置</span>                                                            | 测试被加到 server-core，编译都过不了    |

#### (2) 产出（断言依据，全部通过）

```
场景 1（版本存在）：mock Mapper 返回一个 PromptVersionDO，调用后拿到 PromptVersionDetail。断言依据 fromDO 的实际转换逻辑：createTime 由 LocalDateTime 经系统时区转 epoch ms，previousVersion 为 null 时返回 null（不是空字符串）。

场景 2（版本不存在）：mock Mapper 返回 null，断言抛出 StudioException，errCode == 404，errMsg == "Prompt版本不存在: no-key@v99"（消息格式从源码读出，不是猜的）。

Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.573 s
BUILD SUCCESS
```

有了之前的铺垫后，效果明显。这三条断言都值得细看，因为它们都不是 AI 凭直觉写的：

- `createTime` 由 `LocalDateTime` 经系统时区转 epoch ms 是 `fromDO` 里实际写的转换逻辑
- `previousVersion` 为 null 时返回 null（而不是 AI 容易脑补的空字符串）
- `errMsg` 包含具体的 key 和 version，是从源码里读出来的消息格式。

这三个细节恰好是 AI 最容易"凭应该"写错的地方，提示词的硬约束把它们挡住了。

#### (3) review 重点

| 盯什么                                                       | 出错说明什么                                                                                                     |
| --------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| <span style="color: red; font-weight: bold;">关注 `assertEquals(...)` 里的预期值（比如 `assertEquals(100, ...)`）</span> | 追问"100 这个值从哪来？跑出来的还是猜的？" —— <span style="color: red; font-weight: bold;">AI 常用业务直觉补断言，是特征化测试的最大隐性偏差</span> |
| <span style="color: red; font-weight: bold;">跑一遍，测试是否真的能跑通</span>                                             | 测试逻辑没问题但<span style="color: red; font-weight: bold;">跑不过，说明开发者对现有行为的认知错了</span> —— 这反而是 Characterization Test 的价值，暴露"以为"和"实际"的差距                               |
| <span style="color: red; font-weight: bold;">测试场景是否和待测方法真实分支对齐</span>                                         | <span style="color: red; font-weight: bold;">AI 可能凭假设加场景</span>（比如方法不做状态过滤却加了"状态过滤"场景），多余场景要删                                                                  |

**commit 前必查**

- 改造方案已审核定稿，P01-P06 改造点对应明确代码改动
- `getByPromptKeyAndVersion` 已识别为待复用方法
- Characterization Test 全过，建立了"行为不变"基线
- 测试模块路径在 server-start（能访问 `PromptVersionServiceImpl` 和 `StudioException`）
- 项目确认无 Testcontainers，测试走 Mockito 路线

这一节的几条硬约束（断言凭实际、只做这一批、不重构现有方法）后面会反复出现。**首次讲透后，后续步骤只引用，不再展开论证**。

### 3.3 Step 2：建 DTO（P01-P03）

<img src="imgs/aicmigr-19-dev-03-backend-development/51aa044b972b7dd9e09a6bfdc94ff059_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

锁住现有行为后，正式开始改造。第一批把方案里的三个 DTO 建出来：`PromptVersionDiffResult`、`VersionMeta`、`DiffItem`，给后面的 Service 和 Controller 提供类型基础。

**提示词**

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

**最后一句**"<span style="color: red; font-weight: bold;">只做 P01-P03，不要继续 P04-P05</span>"是这一步的关键约束。

**为什么"只做这一批"必须每步都写？** AI 有"显得能干"的倾向 —— 看到方案列了 P01-P06 六个改造点，会默认一口气全做完来展示效率。但<span style="color: red; font-weight: bold;">分批执行的真正价值是"出错可定位、回退可回滚"</span>。

这和传统版本控制里的小步提交是同一个道理：

- 每批 review + commit 之后再下一批，每一步都是干净提交
- 出问题就退回上一步
-
<span style="color: red; font-weight: bold;">如果 AI 一把梭改完六个改造点，某一处出错时开发者根本不知道是哪一步引入的，`git diff` 也无法精细回退。</span>这条原则后面 Step 3/4/5 都会以"只做这一批"的形式重复，不再展开论证。

**产出**：三个新建的 DTO 文件，全部加 `@Data @Builder @NoArgsConstructor @AllArgsConstructor`，字段对齐 solution.md。

**review 重点**

| 盯什么                                         | 出错说明什么                                           |
| ------------------------------------------- | ------------------------------------------------ |
| 字段是不是和 solution.md 第 3 节改造点表格 + 第 6 节决策点对得上 | 重点核对 `createTime` 是否用 epoch ms、null 处理<span style="color: red; font-weight: bold;">是否符合 D1 决策</span> |
| `git status` 是否只有三个新建 Java 文件               | <span style="color: red; font-weight: bold;">顺手改了别的文件</span>就要让 AI 撤销——优化是另一个改造任务，单独走流程              |

**commit 前必查**

- AI 只改了 P01-P03 这一批改造点（`git status` / `git diff` 看文件清单）
- 没有顺手重构现有方法
- 新增 DTO 对齐项目现有风格（lombok、命名、null 处理）
- `createTime` 用 epoch ms（与现有 `PromptVersionDetail.createTime` 一致）

### 3.4 Step 3：实现 Service（P04-P05）

DTO 建好后，第二批是 Service 接口和实现，复用 `getByPromptKeyAndVersion`，但绝不能重构它。

**提示词**

```
基于 solution.md 的 P04-P05，给 PromptVersionService 加 diffVersions 方法
+ 在 PromptVersionServiceImpl 里实现。

要求：

- 严格按 solution.md 步骤 2 的设计：调 Mapper 两次 → 校验 → 内存比较三字段 → 组装返回
- null 处理用 Objects.equals(nullToEmpty(a), nullToEmpty(b))，对应 D1 决策
- 复用 getByPromptKeyAndVersion 现有方法（solution.md 影响范围第 2 条
  已确认该方法只有 log.info 无 metrics 副作用，不需要抽 getVersionInternal）
- 不要重构 getByPromptKeyAndVersion 任何细节，只调用它
- 异常用 StudioException + INVALID_PARAM/NOT_FOUND 错误码

实现完跑一遍 mvn test，确认 Step 1 的 Characterization Test 全部通过
（行为没偏移）。如果有测试失败，stop，告诉我具体是哪个测试、什么原因。

只做 P04-P05，不要做 P06。
```

提示词**最后一段**把 "<span style="color: red; font-weight: bold;">跑 Step 1 的 Characterization Test</span>" 写成了硬约束——有失败就 stop。

为什么把跑测试写进提示词，而不是只靠开发者事后 `git diff` 检查？因为 AI 如果偷偷改了 `getByPromptKeyAndVersion` 的某个细节（比如调整空值处理），`git diff` 里很难一眼看出来，但 Characterization Test 会立刻失败。把测试跑通作为"完成 P04-P05"的前置条件，等于让 AI 自己卡住自己，不会带着失败的测试往下走。<span style="color: red; font-weight: bold;">这就是把"行为不变"做成 AI 硬约束的关键设计</span>。

**产出**（实现要点，执行后记录）

```
diffVersions 先校验 versionA == versionB（抛 INVALID_PARAM），再查 promptMapper 确认 promptKey 存在（抛 NOT_FOUND），再两次调 Mapper 查版本（各自抛 NOT_FOUND），最后内存比较组装返回。

null 处理用局部变量 String sa = a!= null? a: ""，再 !Objects.equals(sa, sb) 判断 changed，等价于 nullToEmpty 语义，用标准库不引入额外依赖。

getByPromptKeyAndVersion 原方法零改动，Characterization Test 验证通过：
Tests run: 2, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

**review 重点**

| 盯什么                                                                                                          | 出错说明什么                                                                                      |
| ------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------- |
| `git diff` 看 `PromptVersionServiceImpl` 是否只有新增 `diffVersions` + 两个私有辅助方法（`toVersionMeta`、`diffItem`），原方法一行不动 | AI 哪怕只动了格式化也要让它撤销——<span style="color: red; font-weight: bold;">"顺手优化"的口子不能开</span>         |
| `diffItem` 里 null 处理是不是 `a != null ? a : ""` 后再 `Objects.equals` 比较                                          | AI 容易凭直觉写成 `a == null && b == null`，语义完全不同：<span style="color: red; font-weight: bold;">D1 决策</span>是"null 视同空字符串"，两者在 valueA/valueB 字段填充值上完全不同 |
| Step 1 的 Characterization Test 全过                                                                            | 这是硬指标，证明 `getByPromptKeyAndVersion` <span style="color: red; font-weight: bold;">现有行为没被破坏</span>                                                |

**commit 前必查**

- AI 只改了 P04-P05 这一批（`git status` 核对文件清单）
- null 处理符合 D1 决策（`nullToEmpty` 语义）
- 异常用 `StudioException` + `INVALID_PARAM`/`NOT_FOUND` 错误码
- Step 1 的 Characterization Test 全过（行为没偏移）

### 3.5 Step 4：加 Controller（P06）

<img src="imgs/aicmigr-19-dev-03-backend-development/54bce4f61cec35c688ae1fe344b4e049_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Service 跑通后，第三批是 Controller 接口，对外暴露新功能。

**提示词**

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

**为什么不能在 Controller 里 try-catch（要求三）？** 

因为老项目已经有了全局异常处理切面（`@RestControllerAdvice`）相当于一个统一错误响应拦截器，把项目里所有 `StudioException` 接住并统一包装成标准错误响应结构，前端只需要按一套结构解析。

如果让 AI 在 Controller 里自己 try-catch + 包装错误响应，就会出现两套并存的错误响应结构——一套全局的、一套这个 Controller 自己的，前端联调时不知道该按哪套解析。

提示词把这一条写死，强制 AI 走全局异常处理，保持错误响应结构统一。

**产出**（接口签名）

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

**review 重点**

| 盯什么                                                                                      | 出错说明什么                                                                                  |
| ---------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| <span style="color: red; font-weight: bold;">接口签名和</span> solution.md 第 3 节 P06 描述是否完全一致 | 三个 `@RequestParam` + `@NotBlank`，返回 `Result<PromptVersionDiffResult>`                   |
| `git diff` 看 `PromptController.java` 是否只有新增方法 + 一行 import                                | <span style="color: red; font-weight: bold;">其余接口一字不动</span>（同 3.3 节"不重构现有方法"原则）                                                            |
| Controller 里是否出现 try-catch                                                               | 出现就让 AI 撤销 —— 异常必须用老代码的全局切面，<span style="color: red; font-weight: bold;">AI不擅自添加</span> |
| 手动 curl 返回的<span style="color: red; font-weight: bold;"> JSON 结构是否和接口契约对得上</span>        | AI 报告"接口跑通了"不可信，这一步人来跑最稳                                                                |

**commit 前必查**

- AI 只改了 P06（`git status` 核对）
- 异常走全局 `@RestControllerAdvice`，不在 Controller 里 try-catch
- 接口路径 `/api/prompt/version/diff` 不与现有 `/api/prompt/version` 冲突
- mvn test 全部通过（含 Step 1 的 Characterization Test）

### 3.6 Step 5：补单元测试 + curl 验证返回结构

新接口跑通后，验证分两部分：

- Service 层单元测试验证逻辑正确
- Curl 验证真实 HTTP 响应结构对得上接口契约
 
两者不能互相替代——单元测试发现不了 JSON 字段名拼错、类型序列化异常这类问题。

**提示词**（单元测试），其中 prompt-version-diff.md 是之前建立 safe guard 时挖掘的出关键边界

```
给 diffVersions 补单元测试，测试加在 server-start 模块下（原因同 Step 1：
PromptVersionServiceImpl 和 StudioException 在 server-start，server-core 无法访问）
PromptVersionServiceDiffTest.java（如果不存在就新建）。

注意：项目当前没有 Testcontainers 基础设施，不能用 SpringBootTest + 真实 DB。
用 @ExtendWith(MockitoExtension.class) + Mockito mock PromptVersionMapper 和 PromptMapper
（diffVersions 内部调了两个 Mapper，两个都要 mock）。

覆盖需求文档 prompt-version-diff.md 

* E01 versionA == versionB → 抛 StudioException(INVALID_PARAM)
* E02 versionA 不存在 → 抛 StudioException(NOT_FOUND)，errMsg 含版本号
* E04 template 为 null → valueA/valueB 返回 ""，changed=false（两个都是空字符串）
* 正常 happy path（两版本 template 不同 → changed=true，variables 相同 → changed=false）

测试断言凭"实际跑出来是什么"写，不凭"应该是什么"。

跑命令：mvn test -pl spring-ai-alibaba-admin-server-start -am \
  -Dtest=PromptVersionServiceDiffTest -Dsurefire.failIfNoSpecifiedTests=false

汇报每个测试覆盖的场景和实际跑通的状态。
```

断言约束和模块路径约束同 3.2 节，不再展开。

**产出**（单测输出）

```
Tests run: 4, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.601 s
BUILD SUCCESS
```

这四个边界不是随便选的，每一条都对应 `diffVersions` 的一个核心决策点：

| 边界场景 | 实际验证结果 | 考查的核心决策点 |
|---|---|---|
| E01：`diffVersions("key","v1","v1")` | 抛 `StudioException`，errCode=400，无需查 DB | 参数校验顺序——先校验 `versionA == versionB`，避免无意义的 DB 查询 |
| E02：mock Mapper 返回 null for versionA | 抛 `StudioException`，errCode=404，errMsg 包含版本号 "v1" | 版本不存在的错误码——`NOT_FOUND` 而不是 `INVALID_PARAM`，错误码语义要准确 |
| E04：两版本 template 均为 null | valueA=""、valueB=""、changed=false（空字符串相等） | D1 决策"null 视同空字符串"——两个版本 template 都为 null 时 valueA/valueB 都是 `""`（不是 null），最容易写错 |
| happy path：template 不同 | template changed=true；variables 相同 changed=false；promptKey/version/status 字段值与 mock 数据一致 | 内存比较逻辑——三字段分别比较、互不影响 |

**review 重点**

| 盯什么                                     | 出错说明什么                                                                                                                                                   |
| --------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `assertEquals(...)` 追问"这个值是跑出来的还是猜的"    | AI 容易直接 `when(...).thenReturn(mock对象)` 后<span style="color: red; font-weight: bold;">凭直觉写断言</span>，而不是先想"这个 mock 会让 diffVersions 实际算出什么"                 |
| 边界场景是否齐全（E01/E02/E04 + happy path 四条都有） | <span style="color: red; font-weight: bold;">边界场景缺失</span>，少一条就让 AI 补                                                                                    |
| Mockito mock 范围对不对                      | mock `PromptVersionMapper` 和 `PromptMapper`，不要 mock `PromptVersionService` 自身 —— 测的是真实 `diffVersions` 实现，不是 mock 出来的壳。<span style="color: red; font-weight: bold;">AI 可能会偷懒、造一个空壳来替代测试对象、然后对着这个空壳跑测试</span>。 |

**curl 验证（人来做）**

启动应用后，用真实数据库里已有的两个版本手动 curl，看返回 JSON 结构和接口契约对不对：

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

**预期返回结构**（对照 solution.md 第 2 节接口契约）

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

<span style="color: red; font-weight: bold;">curl 这步不要让 AI 代劳：AI 报告"接口跑通了"不可信，自己眼睛看到 JSON 结构才算验证完。</span>重点盯三件事：

| 盯什么                                                 | 出错说明什么                                                                                |
| --------------------------------------------------- | ------------------------------------------------------------------------------------- |
| `data` 字段存在（不是 null）                                | data 是 null 说明 Controller 返回结构有问题，或者序列化失败                                             |
| `diffs` 下三个字段都有（template / variables / modelConfig） | 少一个字段说明 DTO 结构和接口契约不一致——可能漏字段或字段名拼错                                                   |
| `changed` 是 boolean，valueA / valueB 是字符串（不是 null）   | `changed` 是字符串 `"true"` 或 valueA 是 null，说明类型序列化有问题，或者 D1 决策没落地（valueA 应该是空字符串不是 null） |

**commit 前必查**

- 单元测试断言凭"实际跑出来"写（见 3.2 节硬约束）
- mock 范围对（mock Mapper，不 mock 被测 Service 自身）
- 边界场景齐全（E01/E02/E04 + happy path）
- curl 由人来做，眼睛看到 JSON 结构才算验证完

### 3.7 Step 6：跑通 mvn test 全套

<img src="imgs/aicmigr-19-dev-03-backend-development/56b49711ac1aefbafbdf36fce05e769f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

到这一步所有后端改造点（P01-P06）跑完了，最后跑一遍完整 mvn test，确认整体没问题。

**提示词**

```
跑一遍完整测试，含新增的 server-start 测试：

mvn test -pl spring-ai-alibaba-admin-server-runtime,spring-ai-alibaba-admin-server-core,spring-ai-alibaba-admin-server-start \
  -am -Dsurefire.failIfNoSpecifiedTests=false -fae

输出全部测试结果（通过 / 失败 / 跳过 各多少）。
失败的列出来，但不要试图修，只汇报。
```

**为什么要明确"不要试图修，只汇报"？** <span style="color: red; font-weight: bold;">AI 看到 failing test 会本能上手改，最常见的手法是把测试改成"全过"而不是修代码</span> —— 比如把 `assertEquals(expected, actual)` 改成 `assertEquals(actual, actual)`，测试永远过但根本没验证任何东西。

提示词明确"只汇报"，让开发者拿到失败清单后自己判断：是测试错了还是代码错了。`-fae`（<span style="color: red; font-weight: bold;">fail at end</span>）参数让 mvn 跑完全套再汇总，避免一个失败就停，把所有模块的问题一次性暴露出来。

**产出**（完整测试报告）

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

**review 重点**

| 盯什么                                          | 出错说明什么                                       |
| -------------------------------------------- | -------------------------------------------- |
| 失败数为 0                                       | 任何失败都不能进下一步                                  |
| `PromptVersionServiceImplTest` 2 个测试和改造前完全一致 | 证明 `getByPromptKeyAndVersion` 现有行为没被破坏（兜底验证） |
| 总测试数 = 改造前基线 + 新增（14 + 6 = 20）               | 总数不对说明有测试被意外删了或跳过了——这是 AI 偷偷删测试的常见信号，必须追查    |

**commit 前必查**

- mvn test 全套失败数为 0
- 总测试数 = 改造前基线（server-core 14）+ 新增（server-start 6）= 20
- Step 1 的 Characterization Test 改造前后结果一致

### 3.8 Step 7：提交 + 文档自动更新

最后一步把改造方案落地的事实回灌到 docs/，让活资产闭环。这一步的本质是：<span style="color: red; font-weight: bold;">方案是改造前的预期，文档是改造后的现实</span>，两者一定会有差异，文档必须反映现实而不是照抄方案。

**提示词**

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

**为什么强调"反映实际结构，不要照抄方案"？** 因为<span style="color: red; font-weight: bold;">方案是改造前的预期，文档是改造后的现实</span>，两者一定会有差异。实际执行时就遇到了一个：solution.md 第 3 节写的是"P03 新增 dto/DiffItem.java"，但实际实现里 `DiffFields` 没有独立成顶层类，而是 `PromptVersionDiffResult` 的静态内部类（`DiffFields` 只服务于 `PromptVersionDiffResult`，没必要独立）。文档必须反映这个实际选择，不然下次读文档的人会以为漏建了一个文件。

这一步最常见的差异可以归成三类：

| 差异类型 | 典型表现 | 文档处理 |
|---|---|---|
| 嵌套关系变了 | 方案写"独立顶层类"，实际实现成"静态内部类"（如 `DiffFields` 是 `PromptVersionDiffResult` 的内部类） | 文档反映这个实际选择，说明嵌套关系 |
| 文件数变了 | 方案列了 4 个文件（3 个 DTO + 1 个 DiffFields），实际只有 3 个（`DiffFields` 是其中一个 DTO 的内部类） | data-model.md 把嵌套关系写清楚，避免误以为漏建文件 |
| 字段名/类型微调 | review 中把 `Long` 改成 `long`、把 `String` 改成更具体的类型 | 同步到 data-model.md，否则下次改造会基于过时的字段定义做决策 |

这一步呼应 docs-auto-sync 这类自动化回灌工具——可以直接调用 Skill 跑这一步，效果一样。跑完后开发者手里所有 docs/ 资产被这一轮深度思考反向丰富了一轮。

**文档回灌 Check List**

- `api-list.md`：新增接口状态从"开发中"改为"已上线（后端）"
- `api-list.md`：入参和返回结构按实际实现校对
- `data-model.md`：新 DTO 字段如有 review 调整，已同步
- `data-model.md`：嵌套关系（如静态内部类 `DiffFields`）已写清楚
- `solution.md`：每条改造点（P01-P06）已标注实际 commit hash 和文件路径
- `solution.md`：文档反映实际结构，未照抄方案预期描述

整轮跑完，开发者手上的产出：三个新 DTO（`PromptVersionDiffResult` 含静态内部类 `DiffFields`、`VersionMeta`、`DiffItem`）、Service 新增 `diffVersions` + 两个私有辅助方法（原方法零改动）、Controller 新增 `GET /api/prompt/version/diff`、6 个测试（2 个 Characterization Test + 4 个 `diffVersions` 单元测试）、mvn test 全套 20 个测试 0 失败、三份文档同步更新。commit 后等前端联调。

贯穿这七步有两个翻车点：AI 顺手优化老代码、AI 用"应该"而不是"实际"写测试断言。这两个翻车点贯穿全程，下一章独立讲。

## 4. 两个贯穿全程的翻车点

后端改造里有两个翻车点贯穿全程：AI 顺手优化老代码、AI 用"应该"而不是"实际"写测试断言。把它们独立成章，是为了让你在每一步都能回来对照——它们不在某一步出现，而是每一步都可能复发。

### 4.1 翻车一：AI 顺手优化老代码

<img src="imgs/aicmigr-19-dev-03-backend-development/8722563ed280f0db683e6abcf4f720ac_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

典型场景：你让 AI 加一个 `diffVersions` 方法复用 `getByPromptKeyAndVersion`，AI 改完一扫代码，"这个方法可以重构得更优雅"，顺手就把老方法也改了。<span style="color: red; font-weight: bold;">改造任务瞬间混入优化任务，review 成本飙升。</span>

三层防御（细节做法见第 3 章各 Step）：

| 层级 | 做法 | 为什么有效 | 失效场景 |
|---|---|---|---|
| 提示词层 | 每个提示词明确写"不要重构现有方法" | 把约束前置到 AI 的注意力里，挡住"顺手优化"的倾向 | AI 在长上下文里仍可能"忘"，不能只靠它 |
| review 层 | 每步 commit 前 `git diff` 看一遍，超出范围的改动一律撤销 | 不依赖 AI 自觉，靠开发者主动把关 | 开发者疲劳、diff 太长扫漏 |
| 兜底层 | Characterization Test 锁住现有行为 | 只要行为没变测试就过，行为一变立刻报警 | 没补测试的那部分代码失守 |

一句话点评：<span style="color: red; font-weight: bold;">提示词是请求，review 是验证，测试是兜底——三层任意一层失守，另外两层还能接住，单层防御必翻车。</span>

### 4.2 翻车二：AI 用"应该"而不是"实际"写断言

典型场景：AI 写 Characterization Test，看代码 `if (result == null) return Collections.emptyList()`，凭直觉写 `assertNotNull(result)`，但实际跑下来业务数据让结果返回了 null，测试反而失败。更隐蔽的是反向情况：断言基于"应该"写（比如把 `assertEquals(expected, actual)` 写成 `assertEquals(actual, actual)`），代码真被改坏了，测试还绿。

你可能会问：翻车二和翻车一有什么关系？根子是同一个——<span style="color: red; font-weight: bold;">AI 在"判断"这件事上靠不住</span>。

三层防御（细节做法见第 3 章各 Step）：

| 层级 | 做法 | 为什么有效 | 失效场景 |
|---|---|---|---|
| 提示词层 | 写硬话——"不要凭'应该是什么'写断言，凭'实际跑出来是什么'写" | 把判断标准从"直觉"换成"运行结果"，掐掉偏差源头 | AI 仍可能凭代码长相猜，需 review 补 |
| review 层 | 看到 `assertEquals(...)`、`assertTrue(...)` 就追问"这个值 / 这个判断从哪来的" | 把 AI 的隐性假设逼到显性，凭空猜的断言一眼露馅 | 断言太多 review 不过来 |
| 兜底层 | 测试失败时先怀疑测试，不要先怀疑代码 | 基于"实际"的断言失败说明代码确实变了，是有效信号 | 开发者本能地先去改测试，把信号当噪音 |

一句话点评：翻车二的根子和翻车一是同一个——<span style="color: red; font-weight: bold;">AI 在"判断"这件事上靠不住</span>，三层防御换成"把判断逼成数据"。

### 4.3 一个心法：AI 在数据层强、在判断层弱

<img src="imgs/aicmigr-19-dev-03-backend-development/f5d904ede6428393cd97ca51a53dd8ef_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

两个翻车点背后是同一条心法：**AI 在数据 / 代码层面强，在判断 / 直觉层面弱**。

打个类比：<span style="color: red; font-weight: bold;">AI 像一个手速极快、但判断力不稳定的实习生。</span>让它照着 spec 生成代码、照着方法签名补测试、照着字段定义补 DTO——这些"数据层"活它又快又准。但让它判断"这段老代码该不该顺手重构""这个断言该写 null 还是非 null"——这些"判断层"活它会自作主张，而且错得理直气壮。

这条心法贯穿全系列：本篇（执行改造）的两个翻车点是它的具体表现；后续补测试、拆需求、拆方案几篇，本质都是在把"判断"从 AI 手里收回到开发者手里——用 spec 锁需求、用 Characterization Test 锁行为、用 review 锁范围。

回看第 3 章七步，每一步都在印证这条心法：Step 1 让 AI 生成 data-model.md（数据层，放手），Step 3-7 每步 commit 前 `git diff` 人工 review（判断层，收紧），补 Characterization Test 更是把"行为是否变化"这件最需要判断的事，从 AI 的直觉换成测试的客观输出。<span style="color: red; font-weight: bold;">该放手时放手，该收手时收手——这是用 AI 改老项目的核心节奏。</span>

跑完整个流程，你手上会有哪些资产？下一章盘点。

## 5. 跑完一遍流程，你手上有什么

<img src="imgs/aicmigr-19-dev-03-backend-development/96cc01fdece55029b8c9ff8f446efc6a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

跑完整个流程，代码库里多出来的东西归为四类：代码、测试、测试结果、文档。下面按类盘点。

### 5.1 代码资产

| 层 | 改造点 | 产出 |
|---|---|---|
| DTO | P01-P03 | 三个新文件；`PromptVersionDiffResult` 含静态内部类 `DiffFields`、`VersionMeta`、`DiffItem` |
| Service | P04-P05 | 接口新增 `diffVersions` 方法签名；实现类补上实现（含私有辅助方法 `toVersionMeta`、`diffItem`）；原方法 `getByPromptKeyAndVersion` 零改动 |
| Controller | P06 | 新增 `GET /api/prompt/version/diff` 接口；异常走全局 `GlobalExceptionHandler` |

### 5.2 测试资产

| 测试名 | 类型 | 测试数 |
|---|---|---|
| `getByPromptKeyAndVersion` 现有行为 | Characterization Test | 2 |
| `diffVersions` 单元测试（E01/E02/E04 + happy path） | 单元测试 | 4 |

合计 6 个新增测试，全部落在 server-start 模块：前 2 个锁住改造前行为，后 4 个覆盖新方法的三个异常分支与一条正常路径。

### 5.3 测试结果

| 模块 | 测试数 | 说明 |
|---|---|---|
| server-core | 14 | 改造前基线 |
| server-start（新增） | 6 | 2 个 Characterization Test + 4 个 `diffVersions` 单元测试 |
| 合计 | 20 | 0 失败，BUILD SUCCESS |

`mvn test` 全套 0 失败，总测试数 20 = 改造前基线 14 + 新增 6；Characterization Test 改造前后结果一致，现有行为没被破坏。

### 5.4 文档资产

三份同步更新，反映实际实现结构（细节见 3.8 节）：

| 文档 | 更新要点 |
|---|---|
| `api-list.md` | 接口状态从"开发中"改为"已上线（后端）"；入参和返回结构按实际实现校对 |
| `data-model.md` | 新 DTO 字段同步更新；嵌套关系（`DiffFields` 作为 `PromptVersionDiffResult` 的静态内部类）写清楚 |
| `solution.md` | 每条改造点（P01-P06）后标注实际 commit hash 和文件路径，方便回溯 |

这些资产背后是一个判断：慢就是快——下一章展开。

## 6. 慢就是快

<img src="imgs/aicmigr-19-dev-03-backend-development/9d451783797c9797ddf3ab9363dea32b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

慢就是快——改造时多花的半天，远比上线后翻车的三天便宜。

这句话是我在每个团队都会反复强调的，也是前面把改造拆成 Step 1 锁行为、Step 2 写 spec、Step 3 跑兜底测试、一步步走的根本原因。

你可能会问：一个提示词就能搞定的事，为什么非要分这么多步？答案藏在新项目和老项目的差别里。

### 6.1 为什么老项目不能一步到位

新项目没包袱，一步到位，错了就错了，重构成本可控。老项目不一样，它有用户、有数据、有暗角——那些藏在历史代码里的边界条件，往往是多年前踩坑换来的。

区别就在这：<span style="color: red; font-weight: bold;">你贪快这一步，行为没锁，接口换完，回头线上一个隐性 bug，定位起来不是十分钟，是三天。</span>

> 新项目：一步到位，错了重构成本低。
> 老项目：有用户、有数据、有暗角，错了就是线上事故。

Characterization Test 是这个差别的时间放大器。改造前后结果不一致那一刻，你能当场发现；和上线后从用户工单倒回来排查，是两种完全不同的时间量级。

### 6.2 把账算清：慢是刻意的慢

所以核心账必须算清楚：

**改造时因细致多花的时间 < 出 bug 处理 + 返工的时间**

这里的"慢"不是磨蹭，是刻意的慢，是把判断层用流程兜住。<span style="color: red; font-weight: bold;">多花的半天买的是确定性，省下的是上线后翻车的三天和连带返工。</span>

### 6.3 心法落地：护栏不是多余的动作

这正是第 4 章心法的落地。

AI 在数据层强——它能扫代码、能生成 spec、能补测试；但在判断层弱——它不知道哪个行为是业务命脉，哪个边界是历史踩坑换来的。

判断层的兜底靠什么？三道护栏：

- **Characterization Test** 锁住旧行为，让任何偏差当场暴露
- **spec** 写清每一步，让 AI 在明确的契约下干活
- **人工 review** 把关，让人的判断补上 AI 的盲区

这些不是多余的动作，是把 AI 的数据层能力安全释放出来的护栏。<span style="color: red; font-weight: bold;">没有它们，AI 越能干，翻车越狠。</span>

我的看法很直接：老项目改造，不要追求一次到位。**按流程走、细致点、Step by step，反而会觉得真的很快——比想象中快。**
