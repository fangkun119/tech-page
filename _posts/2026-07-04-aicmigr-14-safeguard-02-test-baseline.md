---
title: 传统项目迁AI 14：构建护栏 - 测试摸底
author: fangkun119
date: 2026-07-04 14:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-14-safeguard-02-test-baseline/cover.jpg
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
aicmigr-14-safeguard-02-test-baseline
传统项目迁AI 14：构建护栏 - 测试摸底
-->

## 1. 为什么动手改造前要先摸测试

### 1.1 改造事故最常见的根源：验证债

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/9500732d1acfb0c3321e176b25607e5c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

老项目改造里最常见的事故长这样：<span style="color: red; font-weight: bold;">工程师让 AI 改一个 service 方法，AI 改完一行，悄悄改坏了五行</span>。如果没有测试，这五行被改坏的事实往往两周后才暴露——可能是某个对接方报 bug 找上门，可能是上线后客诉，也可能是某个边角接口跑不出预期值。<span style="color: red; font-weight: bold;">改一行坏五行、两周后才被发现，这就是改造事故最典型的样子。</span>

给这种事故起个名字，叫**验证债**——技术债的一种变体，指"理论上应该验证、实际没人验证"欠下的账。这个名字来自一个公开数据：Sonar 的统计显示，96% 的开发者不完全信任 AI 输出，但只有 48% 每次都 review。<span style="color: red; font-weight: bold;">中间这 48% 的差距，就是验证债。</span>

老项目里这个债更深 —— <span style="color: red; font-weight: bold;">老项目通常没多少测试，AI 改完根本没人能验证那五行有没有被改坏</span>。

### 1.2 没摸清就补，多数会补错地方

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/390e0a6fa6453343214489d67a362836_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

到这里你可能会问：缺测试就直接补呗，为什么要先花一篇来摸底？

<span style="color: red; font-weight: bold;">因为没摸清就开始补，多数会补错地方。</span>

AI 默认会按通用代码质量标准列出一堆"应该补的测试"：每个 controller 加单元测试、每个 service 加 mock、每个 util 加边界测试。这些列出来就是 200 条，看起来都合理，但根本补不完。<span style="color: red; font-weight: bold;">问题不在清单不够全，在清单的目标错了——它按"通用代码质量"列，而不是按"老项目改造"列。</span>

这两种目标差别巨大：

| 目标类型         | 特征                                                        | 结果      |
| ------------ | --------------------------------------------------------- | ------- |
| ❎ 通用目标（覆盖率）  | 补到死、补不完                                                   | 改造迟迟动不了 |
| ✅ 改造目标（<span style="color: red; font-weight: bold;">关键路径</span>） | <span style="color: red; font-weight: bold;">聚焦、可控</span> | 能让改造启动  |

<span style="color: red; font-weight: bold;">老项目改造的目标不是"达到 80% 覆盖率"，是"改造路径上的关键节点都有兜底"。</span>这两件事听起来都像"补测试"，落点完全不同：前者追求数字好看，后者追求改得安心。

所以摸底要先解决一个核心问题——**什么是关键路径**。它对应传统软件工程里的"业务关键路径"，用于圈定回归测试范围；在 AI 改造场景下用途更直接：<span style="color: red; font-weight: bold;">关键路径就是改造时最怕被改坏的那几条主流程。</span><span style="color: red; font-weight: bold;">回答了它，缺口清单自然就收敛到一个可执行的范围（5-10 项 P0，而不是 200 项 P0）。</span>把"什么是关键路径"和"现状离它差多远"这两件事讲清楚，就是本文要做的事。

### 1.3 本文的边界

先把边界划清楚，避免期待落空：<span style="color: red; font-weight: bold;">本篇做的事不是补测试，是摸清现状</span>——这个项目应该测什么、现在测了什么、缺口在哪里。

读完本文，你会知道动手改造之前至少要补哪几条测试，AI 才能放心地改。真正补测试是下一阶段的事，本文只把"摸"这一步做透。

## 2. 摸底四步法总览

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/6d6fb419275287e4791a5f03e2a50769_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 四步走与最终产出

摸底四步可以一句话讲清：摸核心链路 → 摸现有测试 → 跑一遍看实际状态 → 算出缺口清单。

类比传统软件工程，这一整套相当于一次"测试策略制定"——但<span style="color: red; font-weight: bold;">目标不是出一份覆盖率报告挂墙上好看，而是拿到改造护栏的位置坐标。</span>

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/59bca08ae8851bcac9a100219b0bd350_MD5.jpg" style="display: block; width: 800px;" alt="摸底四步法流程图：摸核心链路 → 摸现有测试 → 跑一遍看实际状态 → 算出缺口清单，每步对应一份 docs 资产">

<!-- 
图片内容说明
路径：imgs/aicmigr-14-safeguard-02-test-baseline/59bca08ae8851bcac9a100219b0bd350_MD5.jpg
用途：展示摸底四步法流程与产出资产
内容：四步走流程图（摸核心链路→摸现有测试→跑一遍看实际状态→算出缺口清单），每步对应 docs 资产
-->

<!--
flowchart LR
    S1["Step 1<br>摸核心链路<br>（应该测什么）"] --\> S2["Step 2<br>摸现有测试<br>（现在测了什么）"]
    S2 --\> S3["Step 3<br>跑一遍看实际状态<br>（真能跑吗）"]
    S3 --\> S4["Step 4<br>算出缺口清单<br>（差什么、必补什么）"]

    S1 -.资产.-> A1["docs/critical-paths.md"]
    S2 -.资产.-> A2["docs/test-status.md"]
    S3 -.资产.-> A2
    S4 -.资产.-> A3["docs/test-gaps.md"]
-->

四步走完，`docs/` 目录里多出三份资料：

| 产出资产                     | 回答的问题                     |
| ------------------------ | ------------------------- |
| `docs/critical-paths.md` | <span style="color: red; font-weight: bold;">应该</span>测什么                 |
| `docs/test-status.md`    | 现在<span style="color: red; font-weight: bold;">测了</span>什么（静态盘点 + 动态运行结果） |
| `docs/test-gaps.md`      | <span style="color: red; font-weight: bold;">差什么</span>、必补什么              |

三份资料不是孤立的文档，而是后续改造的<span style="color: red; font-weight: bold;">决策底座——没有它们，补测试就是盲补</span>。

### 2.2 四步各自要回答的问题

四步法不是把摸底拆成四个孤立动作，而是四个层层递进的问题：先回答"应该测什么"，再回答"现在测了什么"，然后验证"这些测试真能跑吗"，最后算出"差什么、必补什么"。<span style="color: red; font-weight: bold;">前一步的产出直接喂给下一步，缺一环后面就接不上。</span>

层层递进对应如下：

| 步骤 | 回答的问题 | 产出资产 |
|------|------------|----------|
| Step 1 | 应该测什么 | `docs/critical-paths.md` |
| Step 2 | 现在测了什么 | `docs/test-status.md`（静态部分） |
| Step 3 | 测试真能跑吗 | `docs/test-status.md`（动态部分追加） |
| Step 4 | 差什么、必补什么 | `docs/test-gaps.md` |

每一步的具体约束、提示词、产出示例、review 要点见后续 Step 1-4 四章。

## 3. Step 1 摸核心链路

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/fec2a6e5b4dc1f7a370642328564cf1c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">摸测试的第一步要先回答"应该测什么"，而不是"现在有什么测试"。顺序不能反——先确定要测的核心链路，后面三步才有对照基准。</span>

传统软件工程里这叫识别"业务关键路径"——就是前面说过的、改造时最怕被改坏的那几条主流程，也是后续给 AI 设护栏的位置。

### 3.1 目标与约束

让 AI 基于前面已经备好的资产 —— 接口清单 `docs/api-list.md`、数据模型 `docs/data-model.md`、`CLAUDE.md` —— 找出这个项目里最值得测的核心链路，写入 `docs/critical-paths.md`。

你可能会问：为什么不直接让 AI 列所有链路？因为**不给约束，AI 会列出一两百条**，把"账号详情查询""Trace 列表分页"这类边角功能也算进去。清单一长，后面的缺口分析就**没法收敛**。所以这一步必须上三条硬约束：

| 约束                   | 说明                                                                |
| -------------------- | ----------------------------------------------------------------- |
| <span style="color: red; font-weight: bold;">总数不超过 8 条</span>        | 宁少勿多，超过 8 条说明 AI 没在挑，而是在罗列                                        |
| <span style="color: red; font-weight: bold;">必须是"改造时容易出问题"的链路</span> | 不是所有链路。判定标准：这条链路被改坏，会直接影响核心功能可用                                   |
| 每条写<span style="color: red; font-weight: bold;">四要素</span>           | <span style="color: red; font-weight: bold;">链路名</span>、<span style="color: red; font-weight: bold;">起点</span>（哪个接口）、<span style="color: red; font-weight: bold;">关键节点</span>（哪些 service / DB 操作）、<span style="color: red; font-weight: bold;">终点</span>（什么状态算成功） |

四要素的目的是让链路可追溯——起点对应入口接口，关键节点对应改造时最容易引入 bug 的位置，终点定义"成功"便于后续判断测试是否覆盖。缺任何一项，后面 Step 4 算缺口时都会卡壳。

### 3.2 提示词

约束想清楚后，提示词就是把约束原样翻译给 AI。可直接拷贝到 Claude Code 运行，改下文件名就能用在自己项目上：

```
基于 docs/api-list.md、docs/data-model.md、CLAUDE.md，给我列出
这个项目最值得测的核心链路。要求：
- 总数不超过 8 条，宁少勿多
- 必须是"改造时容易出问题"的链路，不是所有链路
- 每条写：链路名、起点（哪个接口）、关键节点（哪些 service / DB 操作）、
  终点（什么状态算成功）

输出用表格总结。保存到 docs/critical-paths.md。
```

### 3.3 产出示例

AI 输出的就是 `docs/critical-paths.md`，共 8 条。如果对 Spring AI Alibaba Admin 这个项目熟悉，会发现这 8 条正好覆盖了它的核心功能：

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/03be9a450832505bd63df312f9dd3cc8_MD5.jpg" style="display: block; width: 800px;" alt="critical-paths.md 核心链路清单截图">

<!--
图片内容说明
路径：imgs/aicmigr-14-safeguard-02-test-baseline/03be9a450832505bd63df312f9dd3cc8_MD5.jpg
用途：展示 AI 生成的 critical-paths.md 核心链路清单
内容：编辑器截图，docs/critical-paths.md 中以表格形式列出 8 条核心链路，每条标了链路名、起点接口、关键节点、终点成功状态，覆盖登录、Prompt 创建和运行、Dataset 创建和导入、Evaluator 跑批、实验执行、Trace 写入等关键流程
-->

### 3.4 review 要点

这一步 review 只盯一件事：<span style="color: red; font-weight: bold;">是不是真的核心</span>。

AI 容易把"有接口、有数据流"的链路都当成核心塞进清单。以 Spring AI Alibaba Admin 为例，核心与非核心可以这样对照：

| 类别 | 链路举例 | 判定理由 |
|------|----------|----------|
| 核心 | 登录、Prompt 创建和运行、Dataset 创建和导入、Evaluator 跑批、实验执行、Trace 写入 | 一旦被改坏，整个平台就不能用 |
| 非核心 | 账号详情查询、Trace 列表分页 | 改坏了用户顶多抱怨两句，不致命 |

如果 AI 把"账号信息修改"列为核心链路，要让它重新选。

一个简单的判定标准：问自己一句"如果这条链路被改坏了，是不是必须立刻发现并回滚？"答得上"是"的，才是核心。

## 4. Step 2 摸现有测试

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/caea37ac51b64b8e6c8734151cb0d85c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Step 1 回答了「应该测什么」，这一步回答「现在测了什么」。两者对照，才能算出缺口。

这一步类似传统软件工程里的测试覆盖率盘点，但目的不同——不是要出一个覆盖率百分比挂墙上好看，而是要拿到一份诚实的清单：<span style="color: red; font-weight: bold;">哪些核心链路现在有测试兜着，哪些没有</span>。

### 4.1 目标与约束

让 AI 扫描项目里所有的测试目录（`src/test`、`tests/`、`e2e` 等），统计现有测试情况，写入 `docs/test-status.md`。

你可能会问：直接看覆盖率报告不就行了？不行。覆盖率报告告诉你「代码行被跑了多少」，但不告诉你「业务链路被兜住了多少」——这是两件事。覆盖率是 JaCoCo 干的事，这一步要的是按链路视角的覆盖判断。所以提示词必须钉死五条约束：

| 约束                                  | 说明                                   |
| ----------------------------------- | ------------------------------------ |
| <span style="color: red; font-weight: bold;">分层统计文件数</span>                         | <span style="color: red; font-weight: bold;">单元测试</span> / <span style="color: red; font-weight: bold;">集成测试</span> / <span style="color: red; font-weight: bold;">E2E</span> 各多少个文件 |
| 标出 Controller 与核心 Service 的<span style="color: red; font-weight: bold;">测试缺口</span> | 哪些有测试、哪些没有                           |
| 不要给覆盖率百分比                           | 那是 JaCoCo 干的事，这一步<span style="color: red; font-weight: bold;">不看数字</span>            |
| 不要列每个测试方法                           | <span style="color: red; font-weight: bold;">只关注「哪些核心链路被覆盖」</span>，方法级清单会淹没主线        |
| 对照 `docs/critical-paths.md` 标覆盖情况   | 每条核心链路标注：<span style="color: red; font-weight: bold;">有 / 部分 / 没有</span>             |

最后一条是关键。前四条都是手段，最后一条才是这一步真正要交付的结论——把 Step 1 拿到的核心链路，逐条标上现在的测试覆盖状态。有了这张对照表，Step 4 算缺口才有依据。

### 4.2 提示词

约束讲清楚，提示词就是把约束原样交给 AI：

```
扫一下项目里所有的测试目录（src/test、tests/、e2e 等），
统计现有测试情况。要求：
- 单元测试 / 集成测试 / E2E 各多少个文件
- 哪些 Controller 有对应的测试，哪些没有
- 哪些核心 Service 有测试，哪些没有
- 不要给覆盖率百分比，那是 JaCoCo 干的事
- 不要列出每个测试方法，只关注"哪些核心链路被覆盖"

对照 docs/critical-paths.md，标出每条核心链路当前的测试覆盖情况（有 / 部分 / 没有）。
输出用表格总结。
保存到 docs/test-status.md。
```

### 4.3 产出示例

AI 输出的就是 `docs/test-status.md`，结果如下：

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/93a5317f523c1ae185322a112c0c79b2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-14-safeguard-02-test-baseline/93a5317f523c1ae185322a112c0c79b2_MD5.jpg
用途：展示 AI 生成的 test-status.md 现有测试盘点结果
内容：编辑器截图，docs/test-status.md 中以表格形式列出单元/集成/E2E 测试文件数，并对照 docs/critical-paths.md 标出每条核心链路的覆盖情况（有 / 部分 / 没有），可见大量链路标为"没有"或"部分"
-->

这个项目其实也非常缺测试，这是非常典型的情况。大部分的项目基本是没有测试的，质量基本靠人，而靠人靠不住 —— 这正是 AI 作为工程师战友最该顶上的地方。

### 4.4 review 要点

这一步 review 只盯一件事：<span style="color: red; font-weight: bold;">AI 是不是把「有测试文件」当成了「链路被覆盖」</span>。

这是最容易踩的坑。AI 看到目录里躺着一个 `PromptControllerTest.java`，就判定「Prompt 创建链路已覆盖」——但打开一看，可能这个测试只点了一下健康检查接口，跟 Prompt 创建的真正链路一点关系没有。<span style="color: red; font-weight: bold;">要让 AI 按链路验证，不是按文件验证：一条核心链路算被覆盖，必须是有测试真的从头跑到尾，覆盖了起点接口、关键节点和成功终点。</span>

<span style="color: red; font-weight: bold;">老项目最常见的现象：测试文件不少，但真正覆盖核心链路的不到一半。</span>这一步要诚实暴露这个事实——缺口摆出来不可怕，怕的是被一个文件名糊弄过去，以为有兜底，等真改造时才发现根本没网住。

## 5. Step 3 跑一遍看实际状态

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/a5057c31db60e0e064d1d094724b7af3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前两步是静态分析代码——看哪些测试文件存在、它们覆盖了哪些链路。但测试文件存在，就等于能跑吗？未必。<span style="color: red; font-weight: bold;">十年没维护的测试，跑起来一半失败、一半跳过是常态。</span>这一步只做一件事：让 `mvn test` 诚实地告诉我们实际状态。

### 5.1 目标与约束

类比传统 CI/CD 流水线里的"测试报告"：CI 跑完总会有一份通过 / 失败 / 跳过的汇总。这里要的也是同样的东西，只不过它是<span style="color: red; font-weight: bold;">摸底阶段的基线快照</span>，而<span style="color: red; font-weight: bold;">不是修复后的产物</span>。

#### (1) 目标

让 AI 跑一遍项目的标准测试命令，统计真实结果，输出一份可对照的健康度报告。**只汇报，不修复**。

#### (2) 约束要点

提示词必须卡住六件事，少一项 AI 都会偷懒：

| 序号  | 约束                               | 说明                 |
| --- | -------------------------------- | ------------------ |
| ①   | **跑**一遍**测试**（`mvn test`或项目标准命令） | **统计真实结果**，不能凭代码推断 |
| ②   | **通过 / 失败 / 跳过**各多少              | 三个数字都要，不能只给通过数     |
| ③   | **失败分类**：代码 bug / 测试本身坏了 / 环境问题  | 分类要**具体到每一条**失败    |
| ④   | 总**耗时**多少                        | 反映测试套件规模           |
| ⑤   | **不要试图修复**失败的测试，只汇报状态            | 摸底阶段不开工，开工就跑偏      |
| ⑥   | 给一个**"测试健康度"的判断**                | 颜色阈值见下             |

#### (3) 测试健康度颜色判定

| 颜色  | 通过率       | 含义                 |
| --- | --------- | ------------------ |
| 绿   | <span style="color: red; font-weight: bold;">≥ 90%</span> | 测试<span style="color: red; font-weight: bold;">基本可信，可作为改造护栏</span> |
| 黄   | 60% - 90% | 半死不活，能用但要警惕        |
| 红   | < 60%     | 形同虚设，改造基本没有兜底      |

注意：这里的"通过率"必须把跳过的也算作未通过——否则"90% 通过 + 30% 跳过"会被误判成绿，这是后面 review 要重点盯的坑。

### 5.2 提示词

```
跑一遍 mvn test（或项目的标准测试命令），统计真实结果：
- 通过 / 失败 / 跳过 各多少
- 失败的分类：代码 bug / 测试本身坏了 / 环境问题
- 跑总耗时多少
- 不要试图修复失败的测试，只汇报状态

最后给一个"测试健康度"的判断：绿（90% 通过）/ 黄（60-90%）/红（< 60%）。
输出用表格总结。
追加到 docs/test-status.md 的"实际运行结果"小节。
```

### 5.3 产出示例

结果追加到 `docs/test-status.md` 的"实际运行结果"小节。这次跑出来的结论很直接：测试用例太少，需要补。

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/6e43fb5080cbb8d7dab045585a73b320_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-14-safeguard-02-test-baseline/6e43fb5080cbb8d7dab045585a73b320_MD5.jpg
用途：展示 mvn test 实际运行结果与健康度判断
内容：终端输出截图，AI 跑完 mvn test 后汇总通过 / 失败 / 跳过的数字、失败分类（代码 bug / 测试本身坏了 / 环境问题）和总耗时，并给出测试健康度判断（绿 / 黄 / 红）
-->

### 5.4 review 要点

AI 交回来的报告，有两处最容易掺水，必须重点盯。

#### (1) 失败分类要靠谱

<span style="color: red; font-weight: bold;">AI 偷懒的典型套路：把所有失败都归类成"环境问题"</span>。环境问题听起来无害——"哦，是环境没配好，不是代码的事"，于是大家心安理得地翻篇。

要追问一句：<span style="color: red; font-weight: bold;">代码 bug 类型的失败有几个？具体是哪些？</span>

一个真正的代码 bug 类失败，就值得马上停下来确认。<span style="color: red; font-weight: bold;">老项目的测试里如果真有一两个能跑出来且暴露了代码 bug，那是金子——它说明这段代码现在就有问题，只是被埋着。</span>

#### (2) 不要被乐观结论骗了

"<span style="color: red; font-weight: bold;">绝大多数测试通过"这句话极具欺骗性</span>。听起来很好，但如果跳过的有 30%、失败的有 10%，那真实健康度是黄、甚至红，不是绿。

这就像传统 CI 里那种"100 passed, 50 skipped, 10 failed"的绿色构建——<span style="color: red; font-weight: bold;">绿灯不代表健康，跳过的测试等同于"我们不知道它跑不跑得通"。</span>

要让 AI 把跳过的和失败的合在一起看健康度。摸底阶段宁可把情况估计得糟一点，也不要被一个虚高的通过率骗过去。

## 6. Step 4 算出缺口清单

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/6d004b9b6001f882ad80e4b27add7b77_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 6.1 目标与约束

前三步走完，手里已经有两份对照资料：Step 1 的 `docs/critical-paths.md`（应该测什么）和 Step 2-3 的 `docs/test-status.md`（实际测了什么）。Step 4 就是把这两份资料对齐，<span style="color: red; font-weight: bold;">算出缺口清单</span>。这是四步法里<span style="color: red; font-weight: bold;">最关键的一步</span>——前三步都是输入，这一步直接决定动手改造前要补什么。

这一步对提示词的约束尤其严格。为什么？因为 AI 默认会输出 200 项"建议补"的测试缺口。<span style="color: red; font-weight: bold;">一份完美但不可执行的清单，对工程师来说和没有清单一样</span>：要么硬补到怀疑人生，要么干脆放弃。所以必须在提示词里把范围死死卡住。

约束要点：

| #   | 约束              | 说明                                                     |
| --- | --------------- | ------------------------------------------------------ |
| 1   | **总数不超过 20 项**  | 宁少勿多。超过 20 项基本就是 AI 在堆数量                               |
| 2   | **只列核心链路上的**缺口  | 不在主链路上的，再多也不进清单                                        |
| 3   | 每项**标 P0 / P1** | **P0** = 改造前**必须**有；**P1** = **有了更好**                  |
| 4   | **不追覆盖率指标**     | 追的是"关键路径有兜底"，覆盖率是副产品                                   |
| 5   | 每项写**三要素**      | **场景描述、为什么必须、建议测试类型**（集成 / 单元 / Characterization Test） |

这五条约束的本质是一句话：<span style="color: red; font-weight: bold;">把"理论上该补的"压到"动手前必须补的"</span>。AI 不加约束时会给你一份理想态清单，工程师要的是改造这一阶段能兜住底的最小集合。

### 6.2 提示词

直接复用，约束已经写死在提示词里：

```
对照 docs/critical-paths.md（应该测什么）和 docs/test-status.md
（现在测了什么），算出测试缺口。

严格遵守以下原则：
- 总数不超过 20 项，宁少勿多
- 只列在核心链路上的缺口，不在主链路上的不要列
- 每项标 P0（改造前必须有）/ P1（有了更好）
- 不要追求覆盖率指标，追求"关键路径有兜底"
- 每项写：场景描述、为什么必须、建议测试类型（集成 / 单元 / Characterization Test）

输出用表格总结。保存到 docs/test-gaps.md。
```

### 6.3 产出示例

最终产出是 `docs/test-gaps.md`。AI 按约束给出的缺口清单如下：

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/a83979e5f9443fac2dc40196ebd6f374_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-14-safeguard-02-test-baseline/a83979e5f9443fac2dc40196ebd6f374_MD5.jpg
用途：展示 AI 生成的 test-gaps.md 测试缺口清单（第一部分）
内容：编辑器截图，docs/test-gaps.md 中以表格形式列出 P0 / P1 测试缺口，每项标了场景描述、为什么必须、建议测试类型（集成 / 单元 / Characterization Test）
-->

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/22e44482496f191acead78b2d91bc69e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-14-safeguard-02-test-baseline/22e44482496f191acead78b2d91bc69e_MD5.jpg
用途：展示 AI 生成的 test-gaps.md 测试缺口清单（第二部分，接续上一张）
内容：编辑器截图，test-gaps.md 表格的下半部分，继续列出剩余 P0 / P1 缺口项和备注，覆盖登录、Prompt 运行、Dataset 导入、Evaluator 跑批等核心链路
-->

走到这一步，测试摸底已经做得差不多了。如果足够用心，还会发现对 Spring AI Alibaba Admin 这个项目的功能也会有充分的了解——因为测试是跟着功能走的。

### 6.4 review 要点

提示词把约束写死了，但 AI 不一定老实执行。review 时盯三条：

| 审查项     | 判定标准                                    | 处理动作                                              |
| ------- | --------------------------------------- | ------------------------------------------------- |
| P0 数量   | 控制在 5-10 个                              | 如果 AI 给了 15 个 P0，追问"哪几个必须改造前有，哪几个改造中补也来得及"，让它再砍一刀 |
| P0 对应链路 | **每个 P0 都要能直接回答"如果不补这个，AI 改了什么我会发现不了"** | 回答不了的降级为 P1 或剔除                                   |
| P1 数量   | **P1不超过 10 个**                          | **剩下所有想得到的测试缺口写在备注里**，不进缺口清单                      |

三条背后的同一个判断标准：**P0 是兜底，不是理想态**。<span style="color: red; font-weight: bold;">一个 P0 如果回答不了"AI 改坏了什么我能靠它发现"，那它就不是改造前的必选项。</span>

## 7. 反「大而全」大坑与测试摸底速查

四步法讲完，单独强调一个贯穿全程的大坑：老项目摸测试，最容易翻车的不是工具不对、流程不对，而是被 AI 的「大而全」反噬。它贯穿四步法——Step 1 摸链路、Step 4 列缺口都可能中招，所以放在最后单独讲，比夹在某一节里更有冲击力。

### 7.1 最大的坑：不让 AI 大而全

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/a00c68d4dd834b9221337cae814d8acf_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 「完美但不可执行」的清单陷阱

第一次让 AI 出测试缺口清单，它几乎一定会列 100+ 项。每一项看起来都合理：这个边界值该测、那个异常分支该测、util 函数也该有单测。

<span style="color: red; font-weight: bold;">但工程师看完这份清单，通常只有一个反应：补不动，干脆不补了。</span>

这是老项目改造里最常见的「被 AI 推着走偏」的场景。**AI 给出的是一份「完美但不可执行」的清单**，工程师面对它只有两条路：

- 硬补，补到怀疑人生，进度原地踏步；
- 直接放弃整个补测试这件事，回到「改一行坏五行」的老路。

<span style="color: red; font-weight: bold;">两个结果都不对。问题不在 AI 列错，而在你没给它框出「可执行」的边界。</span>

#### (2) 约束 AI 的三条原则

为什么 AI 这么喜欢列大而全的清单？因为它默认按通用代码质量标准行事 —— 只要理论上「该有测试」的，它都会列上。没有约束，它就倾向于把能想到的全给你。

解法是用三条原则把它框在可执行范围内：

| 原则          | 操作                                                                         | 为什么有效                                 |
| ----------- | -------------------------------------------------------------------------- | ------------------------------------- |
| 数量上限        | 每个产出文件都明确写「**不超过 X 项**」（Step 1 不超过 8 条核心链路，Step 4 不超过 20 项缺口清单，P0 上限 10 个） | **AI 看到具体数字，真的会去筛选**                  |
| 关联核心路径      | 「**不在主链路上的不要列**」必须写进提示词                                                    | 没这一句，AI 会按通用代码质量标准列出每个 util 函数都该有边界测试 |
| **优先级强制分层** | **P0 / P1 必须分开列**                                                          | 混在一起 AI 会把「建议补的」和「必须补的」搅成一锅粥，让人无从下手   |

这三条本质是在替 AI 做取舍：数量上限逼它从「全列」变成「挑重要的列」，关联核心路径告诉它「重要」的判断标准是主链路而非代码质量，优先级分层让它把「必须补」和「建议补」分清。<span style="color: red; font-weight: bold;">三条合在一起，AI 的输出就从「教科书」变成「能开工的任务清单」</span>。

#### (3) 第一版仍过多时的处理

加了三条约束，第一版还是嫌多怎么办？直接回它一句：

「P0 砍到 5 个、P1 砍到 10 个，砍掉多余的。」

<span style="color: red; font-weight: bold;">AI 会按这个新上限重新筛选，不是简单地删后一半，而是基于「更核心的链路优先」重新排序。砍完之后，清单会显著更聚焦，每一项都是改造路上绕不开的。</span>

这一招不只用在测试摸底。老项目改造里只要让 AI 列清单——无论是改造范围、风险点、回归用例——记得加这三条约束，避免被大而全反噬。

### 7.2 测试摸底速查表

<img src="imgs/aicmigr-14-safeguard-02-test-baseline/02aa2f43751973435ba47f99a3f252a1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这张速查表把四步的产出与 review 压成可对照执行的清单，在项目阶段直接用。正文 Step 1-4 已详述的约束与提示词，这里只留最关键的指针，不展开。

#### (1) 前置产物检查

动手前确认上游资产齐全，否则 AI 在 Step 1 就会因为没有接口清单而瞎编：

- [ ] `docs/api-list.md` 是否存在（接口清单）
- [ ] `docs/data-model.md` 是否存在（数据模型）
- [ ] `CLAUDE.md` 是否存在（项目根上下文）
- [ ] 环境已跑通，能执行 `mvn test`

#### (2) 四步速查表

每行一步，只留最该盯的产出与 1-2 条 review 要点。完整约束与提示词见正文 §3-§6。

| 步骤 | 关键产出 | 核心 review 盯点 |
|------|----------|------------------|
| Step 1 摸核心链路 | `docs/critical-paths.md`，不超过 8 条 | 是不是真核心（登录、Prompt 创建和运行、Dataset 创建和导入、Evaluator 跑批、实验执行、Trace 写入等链路齐全）；账号详情、列表分页这类非核心没混进来 |
| Step 2 摸现有测试 | `docs/test-status.md`（静态部分） | 按链路验证，不按文件验证——AI 没把「有测试文件」等同「链路被覆盖」；老项目常见现象被诚实暴露：测试文件不少但覆盖核心链路不到一半 |
| Step 3 跑一遍看实际状态 | `docs/test-status.md` 追加「实际运行结果」+ 健康度（绿 / 黄 / 红） | 失败分类靠谱，AI 没把所有失败归为「环境问题」；健康度把跳过的也计入（绿的标准是 90% 通过，不是「绝大多数通过」）；只汇报状态，不修复失败测试 |
| Step 4 算出缺口清单 | `docs/test-gaps.md`，不超过 20 项，P0 / P1 分层 | P0 数量 5-10 个，每个都能直接回答「不补这个，AI 改了什么会发现不了」；P1 不超过 10 个，其他想得到的写进备注 |

#### (3) 反「大而全」三条约束 checklist

这三条与 §7.1 的原则一一对应，写进每一步的提示词里才算数：

- [ ] 数量上限写进提示词（8 条链路 / 20 项缺口 / P0 ≤ 10）
- [ ] 「不在主链路上的不要列」写进提示词
- [ ] P0 / P1 分开列，不混在一起

### 7.3 小结

本文只讲一件事：摸清项目的测试现状，算出动手改造前必须补的测试缺口。

四步法走下来：摸核心链路 → 摸现有测试 → 跑一遍看实际状态 → 算出缺口清单。每一步都让 AI 在严格约束下产出，避免大而全。

跑完之后，`docs/` 目录里多出三份资产：

| 资产 | 内容 |
|------|------|
| `docs/critical-paths.md` | 应该测什么 |
| `docs/test-status.md` | 现在测了什么（静态 + 动态） |
| `docs/test-gaps.md` | 缺什么、必须补什么 |

有了 `test-gaps.md` 这份清单，下一阶段就能真正补出测试——尤其是 Characterization Test 这种锁住老项目「现实行为」的方法。<span style="color: red; font-weight: bold;">补完之后，改造前的护栏就齐了。</span>

<span style="color: red; font-weight: bold;">老项目改造的护栏，不是测试覆盖率指标，是改造路径上的关键节点都有兜底。摸清比补全重要十倍。</span>
