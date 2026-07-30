---
title: 传统项目迁AI 16：构建护栏 - 流程回顾
author: fangkun119
date: 2026-07-04 16:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-16-safeguard-04-process-recap/cover.jpg
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
aicmigr-16-safeguard-04-process-recap
传统项目迁AI 16：构建护栏 - 流程回顾
-->

## 1. 为什么要构建改造前护栏

### 1.1 改造前护栏是什么

<img src="imgs/aicmigr-16-safeguard-04-process-recap/72274bd54d8a9111490e04c5dcc928bb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

传统软件改造前你会做什么？搭好本地环境、确认测试能跑通、把 CI 配上。这套动作你做过无数遍，目的只有一个：**在动手改之前，先有一张安全网**。这张网由三件事构成——<span style="color: red; font-weight: bold;">环境</span>、<span style="color: red; font-weight: bold;">测试</span>、<span style="color: red; font-weight: bold;">CI</span>。环境让你能复现问题，测试让你能发现回归，CI 让每次提交都被自动检查。三者缺一，改动就可能在线上才暴露。

进入 AI 协作场景后，这张网的意义被放大了，原因有两个：

- AI 写代码很快，但快不等于对。一个错误的改动如果没有测试拦住，几分钟内就能毁掉一个核心链路。
- <span style="color: red; font-weight: bold;">AI 不会主动告诉你"我刚才那个改动把登录流程改坏了"，它只会继续往下做。</span>你必须有一套自动化的机制替你盯着。

所以"改造前护栏"不是新概念。它的本质还是你熟悉的那套"环境 + 测试 + CI"，只不过在 AI 协作下，它的角色发生了升级：

| 场景      | 护栏角色    | 类比                     |
| ------- | ------- | ---------------------- |
| <span style="color: red; font-weight: bold;">传统改造</span>    | <span style="color: red; font-weight: bold;">质量保证</span>    | <span style="color: red; font-weight: bold;">人工质检：人来确认改动是否合格</span>        |
| **AI 协作改造** | **AI 行为约束** | **给 AI 装熔断器：AI 一旦越界，自动断电** |

传统 CI 像人工质检——你提交代码，CI 帮你跑一遍验证；<span style="color: red; font-weight: bold;">AI 协作下的 CI 更像熔断器——AI 自主跑流程时，CI 是它无法绕过的硬性闸门，行为越界立刻被拦下来。</span>

### 1.2 四个场景是一条依赖链

<img src="imgs/aicmigr-16-safeguard-04-process-recap/9e2b81e4b2b44226b9333b2ff32842fb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这张安全网不是一次性建成的，它由四个场景串成一条依赖链。下面是它的全景：

<img src="imgs/aicmigr-16-safeguard-04-process-recap/6c657fd54b009879e38abbd56abaea87_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicmigr-16-safeguard-04-process-recap/6c657fd54b009879e38abbd56abaea87_MD5.jpg
用途：展示构建改造前护栏的四个场景串联关系
内容：场景一环境工程师 → 场景二测试摸底 → 场景三补兜底测试 → 场景四 CI 护栏 → 护栏到位，每一步都用箭头标注触发条件（项目活了 / 知道补什么 / P0 全绿 / push 自动跑）
-->

<span style="color: red; font-weight: bold;">四个场景串成一条线：<p><br/> 1. 先把项目跑起来（场景一）；<br/> 2. 再摸清现在有哪些测试（场景二）；<br/> 3. 然后补上改造前必须有的兜底测试（场景三）；<br/> 4. 最后让 CI 在每次 push 时自动跑这些测试（场景四）。</p><br/> 缺一不可，顺序也不能乱。</span>

这四个场景之间的关系不是平行的，而是严格的依赖链：

```mermaid
flowchart LR
    A["场景一环境工程师"] -->|项目活了| B["场景二测试摸底"]
    B -->|知道补什么| C["场景三补兜底测试"]
    C -->|P0全绿| D["场景四CI护栏"]
    D -->|push自动跑| E["护栏到位"]
```

为什么是这个顺序？前一个场景的产出，是后一个场景的输入：

- 场景二要跑测试，前提是项目能启动 → 必须先做场景一。
- 场景三要补缺口，前提是知道缺口在哪 → 必须先做场景二的摸底。
- <span style="color: red; font-weight: bold;">场景四要让 CI 跑测试，前提是有值得跑的测试 → 必须先补完场景三的 P0。</span>

## 2. 贯穿全程的两条铁律

在进入具体场景前，有两条原则必须先讲清楚。它们贯穿所有四个场景，是让 AI 自主推进而不失控的关键：一条管"AI 出错时怎么办"，一条管"补测试时断言怎么写"。

### 2.1 自主修复原则与 3 次兜底

<img src="imgs/aicmigr-16-safeguard-04-process-recap/f15ba08962c1e717ce88ce50ec14ee5a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

让 AI 自主跑安装、启动、CI 这类容易踩坑的步骤时，必须把"自主修复"的边界写死，否则 AI 要么每步都问、要么陷入死循环。

#### (1) 核心原则

| 原则 | 含义 |
|------|------|
| 先看报错自己判断 | 任何一步失败，AI 先读报错、自己分析原因、自己修、修完重试 |
| 不每个错误都问 | 不要把每个错误都抛给用户 |
| 3 次兜底停 | 同一个错误连续修 3 次还不行，停下来汇报具体卡在哪 |

这个原则适用于整个改造前护栏建立流程中的所有执行性步骤：安装脚本（scripts/install-deps.sh）的执行、应用启动（mvn clean package + 启动）、CI 第一次跑通（push 后 debug）。

#### (2) 为什么要硬编码"3 次"

你可能会问：让 AI 自己判断什么时候停不行吗？不行。AI 的默认行为是"无限重试"——它会不断换方法试，烧 token、卡住整个流程。<span style="color: red; font-weight: bold;">把"连续 3 次同一错误才停"写进提示词，等于给 AI 装了一个熔断器。</span>

反模式与正确做法对照：

| 反模式 | 后果 | 正确做法 |
|--------|------|---------|
| AI 遇到错误立刻停下问用户 | 用户沦为人工错误处理器 | AI 自己 debug，3 次同错才停 |
| AI 不设兜底无限重试 | 烧 token、卡住整个流程 | 连续 3 次同一错误必须停 |
| 用户没把"3 次"写进提示词 | AI 默认行为不可控 | 一键流程提示词里硬编码"连续 3 次同一错误" |

### 2.2 测试断言凭"实际行为"不凭"应该"

<img src="imgs/aicmigr-16-safeguard-04-process-recap/b32d9374bc9d277f40162bfe0b6a7fc6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">这是补测试环节最值钱的洞察，AI 一不留神就会踩坑导致测试无效。</span>

#### (1) 两种测试的本质区别

先用一个类比把概念讲清楚，再上专业术语。

想象你在一片遗址上做考古：你不知道这件陶器当年"应该"长什么样，你只知道它**现在**是这个形状、这个颜色。你能做的，是把这个现状如实记录下来、冻住，等将来有人动它时立刻能发现差异。这就是 **Characterization Test（特征化测试）** 的核心思想——它由 Michael Feathers 在《修改代码的艺术》里提出，和普通测试的思路完全相反。

普通测试则像写"需求验收单"：需求说"输入 X 应该返回 Y"，你就断言 Y。

| 类型 | 断言来源 | 适用场景 |
|------|---------|---------|
| Characterization Test（特征化测试） | 凭"现在实际做什么"写断言 | 改造前的存量代码、行为还不确定 |
| 普通测试 | 凭"应该做什么"写断言 | 全新需求、行为已明确 |

#### (2) 为什么改造前必须凭"实际"

<span style="color: red; font-weight: bold;">改造前的老代码，业务直觉和实际行为经常对不上。</span>如果按"应该是什么"补断言，会发生这种事：

- AI 按业务文档或常识补断言
- 实际行为可能与业务文档不一致
- 测试看起来绿了，实际没在保护任何东西
- 改造时一旦触动，测试反而成为阻力

<span style="color: red; font-weight: bold;">绿测试是危险的——它让你以为有保护，实际没有。红测试虽然烦人，但至少它没骗你。</span>

#### (3) 正确写法

| 步骤 | 动作 |
|------|------|
| Step 1 | 先跑一次现有代码，记录实际行为 |
| Step 2 | 把实际行为转成断言 |
| Step 3 | 不凭"应该是什么"写断言，凭"实际是什么"写 |

## 3. 场景一：让 AI 当你的环境工程师

<img src="imgs/aicmigr-16-safeguard-04-process-recap/86b954d056cd313d58ef145d19aa104c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

传统项目里，谁负责把项目从"代码躺在仓库"变成"能在本地起来、能被调用"？通常是运维或 DevOps 工程师——装中间件、写启动脚本、配好端口、处理依赖冲突，最后交付一个"能跑"的开发环境。AI 协作下，这个角色交给了 AI：它读依赖图、装中间件、写启停脚本、跑通应用、做接口冒烟，产出整套环境搭建产物。你的身份从执行者变成 review 者——AI 干活，你把关质量。

### 3.1 场景目标与关键产出

目标：把项目从"只能读代码"跑成"能起来能调用"。

关键产出文件：

| 产出文件 | 作用 |
|---------|------|
| docs/env-checklist.md | 依赖清单 |
| scripts/install-deps.sh + scripts/install-log.md | 本地安装脚本 + 安装日志 |
| scripts/deps-start.sh / deps-stop.sh / deps-status.sh | 依赖启停脚本 |
| docker-compose.dev.yml | Docker 备选方案 |
| docs/startup-log.md | 应用启动日志 |
| docs/smoke-test-result.md | 接口冒烟报告 |

前提：docs/ 下已经有项目了解阶段产出的资产（architecture.svg、api-list.md、data-model.md 等）。如果是新机器或重置过，先确认进了项目目录：

```bash
cd spring-ai-alibaba/spring-ai-alibaba-admin
```

在项目根目录启动 Claude Code，下面所有提示词都在这里跑。

### 3.2 提示词与产出物

#### (1) 依赖盘点

目标：让 AI 把项目需要的外部依赖梳理成一份清单。

```
综合看 docs/external-deps.svg、application*.yml、pom.xml、README，
给我列一份这个项目运行需要的完整外部依赖清单。
每个依赖列出：名字、版本要求（精确到主版本）、默认端口、
连接信息、初始化要求（建库、配 Nacos 命名空间等）。
输出用表格总结。保存到 docs/env-checklist.md。
```

产出：docs/env-checklist.md

review 重点：每个依赖的名字、版本要求、默认端口、连接信息、初始化要求是否齐全；整份清单与 docs/external-deps.svg 对得上，没有漏项或多列。

#### (2) 本地安装方案

目标：把清单变成可执行脚本，并让 AI 自己跑一遍。这里第一次用到自主修复原则。

```
读 docs/env-checklist.md，给我生成一份本地安装脚本，
保存到 scripts/install-deps.sh。
- 用 brew（macOS）或 apt（Linux）装中间件
- 包含每个中间件的初始化（建库 SQL、Nacos 配置等）

生成完直接执行这个脚本。执行过程遵循自主修复原则：
任何一步失败先看报错、自己判断原因、自己修、修完重试。
不要每个错误都问我。同一个错误连续修 3 次还不行，停下来汇报具体卡在哪。

最终输出 scripts/install-log.md，
记录每个中间件最终用了什么命令装上、过程中遇到什么问题、怎么修的。
```

产出：scripts/install-deps.sh + scripts/install-log.md

review 重点：脚本对每个中间件是否都包含"装 + 初始化 + 验证"三步；3 次失败兜底是否真的把死循环掐住了；install-log.md 是否如实记录了实际命令与修复过程。

#### (3) 依赖启停脚本

目标：把日常用的启停操作固化成三个脚本，避免每次手敲一长串命令。

```
基于 Step 2A 装好的中间件，生成三个脚本到 scripts/ 下：
- deps-start.sh：一键启动所有依赖中间件
- deps-stop.sh：一键停止所有依赖中间件
- deps-status.sh：查看每个中间件的运行状态

考虑混合场景：有的用 brew services 管，有的是手动 jar，
有的是 systemd。脚本要能处理这几种。
启动后等服务就绪再返回，不要"启动了但还没 ready"。
```

产出：scripts/deps-start.sh / deps-stop.sh / deps-status.sh

review 重点：启动顺序对不对（Nacos 在 OTel Collector 前、MySQL 在应用前）；启动后是否真的等服务就绪才返回，而不是"起了但还没 ready"；status 输出是否一眼能看清每个中间件状态。

#### (4) Docker 备选方案

目标：给偏好 Docker 的同学一个备选，不强求。

```
顺手给一份 docker-compose.dev.yml，把所有依赖打包成 docker。
偏好 Docker 的同学可以用这个替代 Step 2A 和 2B。
版本号、端口、初始化脚本要齐全。保存到项目根目录。
```

产出：docker-compose.dev.yml（备选，不强制）

review 重点：版本号、端口、初始化脚本是否齐全——能用 docker-compose up 一把拉起才算合格。

#### (5) 编译启动

目标：应用真正跑起来。再次用自主修复原则。

```
中间件已经起来了（用 ./scripts/deps-status.sh 确认）。
现在帮我跑 mvn clean package + 启动应用。

启动过程遵循自主修复原则（参照 install 脚本的兜底机制：
连续 3 次同一错误才停下来汇报）。

启动成功后告诉我应用监听的端口、管理界面地址。
失败和修复的过程记到 docs/startup-log.md。
```

产出：项目跑起来 + docs/startup-log.md

review 重点：mvn clean package 是否真的跑通；日志里有没有 ERROR；应用监听端口是否正常、管理界面是否能打开。

#### (6) 接口冒烟

目标：确认核心接口真能用，不只是应用起来了。

```
读 docs/api-list.md，挑 5 个最核心的接口（覆盖登录、Prompt、
Dataset、Evaluator、Trace 几大模块），用 curl 跑一遍。
返回 200 算通过，返回错误的列出来。
输出用表格总结，保存到 docs/smoke-test-result.md。
```

产出：docs/smoke-test-result.md

review 重点：选的接口是不是真核心（要覆盖登录、Prompt、Dataset、Evaluator、Trace 几大模块，不是随便挑的）；返回结构是否与 api-list.md 一致；错误是否诚实列出，而不是假装全绿。

到这里项目能跑了，下一步进入场景二：测试摸底。

## 4. 场景二：摸清测试现状

<img src="imgs/aicmigr-16-safeguard-04-process-recap/9dd69173af5eeffe80886eb357e37cdc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

传统项目里，"摸测试现状"是 QA 的活儿，开发只管写代码、提测，测试用例覆盖率够不够、哪些链路没人管，那是测试经理的事。但在 AI 协作改造老项目这个场景下，这个角色必须由你来扮演——更准确地说，由你指挥 AI 扮演"测试架构师"：它替你扫代码、跑测试、算缺口，你替它判断什么是核心、什么数字靠谱、什么该砍掉。

为什么必须摸测试现状？因为老项目改造最大的风险不是改错代码，而是改完没人发现。你动一个 Service，编译能过、接口能起，但如果核心链路没有兜底测试，改坏了你也看不出来——直到上线炸了才知道。所以动手改之前，先搞清楚三件事：核心链路是什么、现在测了什么、缺口在哪。这三件事摸清了，后面补测试、做改造才有护栏。

### 4.1 场景目标与关键产出

这一场景的目标就一句话：摸清核心链路是什么、现有测试覆盖到哪、缺口在哪。AI 扮演测试架构师角色，你扮演把关人。

关键产出文件有三个：

| 产出文件 | 回答什么问题 |
|---------|------------|
| docs/critical-paths.md | 这个项目什么值得测 |
| docs/test-status.md | 现在测了什么、跑起来什么状态 |
| docs/test-gaps.md | 还差什么、先补哪些 |

三个文件是串行依赖关系：先定核心链路，再照着链路查现状，最后把两者做差集得出缺口。顺序不能乱，尤其是第一步——<span style="color: red; font-weight: bold;">链路定错了，后面全跟着错</span>。

### 4.2 提示词与产出物

下面四个提示词对应四步动作。每一步的 review 重点里，我都把对应的"通过标准"列了出来——这些标准不是装饰，是验收线，不达标就让 AI 重做。

#### (1) 摸核心链路

目标：先确定"什么值得测"。这一步定调，后面所有动作都围绕它。

```
基于 docs/api-list.md、docs/data-model.md、CLAUDE.md，给我列出
这个项目最值得测的核心链路。要求：
- 总数不超过 8 条，宁少勿多
- 必须是"改造时容易出问题"的链路，不是所有链路
- 每条写：链路名、起点（哪个接口）、关键节点、终点（什么状态）

输出用表格总结。保存到 docs/critical-paths.md。
```

产出：docs/critical-paths.md

review 重点（对照通过标准）：

- 总数不超过 8 条，宁少勿多——AI 默认贪多，必须卡死
- 每条要写全链路名、起点接口、关键节点、终点状态，四要素缺一不可
- 是真核心。登录、Prompt CRUD、Dataset CRUD、Evaluator 跑批、实验执行、Trace 写入这种是核心；账号详情查询、分页列表这类 CRUD 不是。判断标准：改造时如果这条链路坏了，业务会不会直接趴窝

#### (2) 摸现有测试

目标：知道现状是什么。关键提醒——按链路验证，不是按文件验证。<span style="color: red; font-weight: bold;">文件存在不代表链路被覆盖</span>，一个 `UserControllerTest.java` 在那里，可能只测了 `getUserById`，创建、删除、权限校验一个没碰。

```
扫一下项目里所有的测试目录（src/test、tests/、e2e 等），
统计现有测试情况。要求：

- 单元测试 / 集成测试 / E2E 各多少个文件
- 哪些 Controller 有对应的测试，哪些没有
- 哪些核心 Service 有测试，哪些没有
- 不要给覆盖率百分比，那是 JaCoCo 干的事
- 不要列出每个测试方法，只关注"哪些核心链路被覆盖"

对照 docs/critical-paths.md，标出每条核心链路当前的测试覆盖
情况（有 / 部分 / 没有）。输出用表格总结。
保存到 docs/test-status.md。
```

产出：docs/test-status.md

review 重点（对照通过标准）：

- 文件数已统计（单元 / 集成 / E2E 各多少），Controller、Service 的覆盖已标注
- 对照 critical-paths.md，每条核心链路标了覆盖度（有 / 部分 / 没有）——这是本步的灵魂，没标就是白做
- 不要给覆盖率百分比。JaCoCo 那种行覆盖率数字会骗人，90% 行覆盖率可能完全不碰关键分支
- 不要列每个测试方法。我们关心的是链路层面的覆盖，不是方法级的清单

#### (3) 跑一遍看实际状态

目标：拿到真实数字，不只看代码。测试文件写在那里，和测试真的能跑、能通过，是两回事——老项目里"测试存在但跑就红"的情况太常见了。

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

产出：追加到 docs/test-status.md

review 重点（对照通过标准）：

- 通过 / 失败 / 跳过三类数字齐全
- 失败分类要靠谱：代码 bug（真问题）、测试本身坏了（断言写错或 mock 失效）、环境问题（数据库连不上、依赖没起）——三类要分清，笼统说"测试失败"等于没说
- 健康度算上跳过的。跳过的测试不是"没事"，是"不敢跑"，风险等同于失败，必须算进健康度
- 只汇报，不修复。这一步是体检，不是治病；动手修就会打乱摸底的节奏

#### (4) 算出缺口清单

目标：把"应该测的"和"现在测了的"做差集，得出一张可执行的补测试清单。这是整个场景的收口动作，直接决定下一场景补什么。

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

产出：docs/test-gaps.md

review 重点（对照通过标准）：

- 总数不超过 20 项——AI 会想列全，必须卡死
- 只列在核心链路上的缺口。不在主链路上的（哪怕确实缺）不要进来凑数
- 每项标 P0（改造前必须有）或 P1（有了更好）
- 每项写全：场景描述、为什么必须、建议测试类型（集成 / 单元 / Characterization Test）
- <span style="color: red; font-weight: bold;">P0 数量控制在 5-10 个。多了改造前补不完，少了护不住关键链路</span>；每个 P0 都要对应到明确的核心链路
- P1 不超过 10 个，不能挤占 P0 的注意力

### 4.3 关键约束：数量与分级

<img src="imgs/aicmigr-16-safeguard-04-process-recap/35ddb6751c2b1525e3fc53f8e1695e89_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这个场景的约束最密集，下面这些数字看着像细节，实际上每一个都是踩过坑才得出的经验。AI 默认会"贪多"——给它自由度，它会把 20 条链路、50 个缺口、全量 CRUD 都列出来，导致后面补测试时陷入泥潭。所以<span style="color: red; font-weight: bold;">数量约束不是建议，是护栏。</span>

| 约束 | 数值 | 为什么 |
|------|------|--------|
| 核心链路总数 | 不超过 8 条，宁少勿多 | 链路多了摸不准，后面每一步都被稀释 |
| 测试缺口总数 | 不超过 20 项 | 缺口太多补不完，等于没护栏 |
| P0 缺口数量 | 5-10 个 | P0 是改造前必须补的，多了补不完、少了护不住 |
| P1 缺口数量 | 不超过 10 个 | P1 是锦上添花，不能挤占 P0 注意力 |
| 冒烟接口数 | 5 个最核心接口 | 冒烟只验"活着"，不是完整回归 |

除了数字约束，还有一组原则性约束，同样是硬性的。它们针对的不是"列多少"，而是"怎么列"——AI 经常犯的反模式，对照如下：

| 约束 | 反模式 |
|------|--------|
| 只列在核心链路上的缺口 | 把不在主链路上的也列进来凑数 |
| 不追求覆盖率指标 | 追求 JaCoCo 那种百分比 |
| 追求"关键路径有兜底" | 追求"覆盖率数字好看" |
| 测试健康度算上跳过的 | 只算失败的 |
| 失败分类要靠谱（代码 bug / 测试本身坏 / 环境） | 笼统说"测试失败" |

<span style="color: red; font-weight: bold;">这两张表是本场景最值钱的经验。</span>前面四个提示词的 review 重点里，约束是散着出现的；这里集中起来，是为了让你在每一步结束时拿它对一遍——哪条没做到，回去重做。

测试现状摸清了：核心链路定了、覆盖现状有了、缺口清单出来了、P0 和 P1 分好了。到这里，改造前"必须补哪些测试"这个问题就有了明确答案——下一场景就是按这份缺口清单，一批一批把兜底测试补上。


## 5. 场景三：补出兜底测试

<img src="imgs/aicmigr-16-safeguard-04-process-recap/f6f0222feb09379246a15f4771e27c94_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 5.1 场景目标与关键产出

目标：把 P0 测试缺口全部补上并跑通。

这一步为什么是整个改造前护栏里最耗时、也最关键的一步？用费曼的方式说清楚：前面两章（摸现有测试、算缺口清单）做的是**诊断**——告诉你项目哪里病了、病的程度。但诊断本身不治病，知道缺口在哪并不等于缺口被堵上了。<span style="color: red; font-weight: bold;">**真正建立保护的是这一步**</span>：你把测试一行行写出来、跑通、钉死，改造时才会有一张网兜在下面，一旦 AI 把代码改坏了立刻报警。没有这一步，前面所有的诊断报告都只是纸上谈兵。

关键产出文件：`docs/test-plan.md`、`src/test/` 下新增的 P0 测试。

### 5.2 提示词与产出物

#### (1) 让 AI 补出测试计划

目标：把 P0 缺口拆成可控的小批次，避免一次性补一堆测试无法 review。

```
基于 docs/test-gaps.md，把 P0 缺口拆成多批，每批 1-3 个
（最好 1 个），给我一份补测试计划。每批写：批次号、测试类型
（Characterization Test / 集成测试 / 单元测试）、覆盖的核心
链路、预期工作量。

按"改造路径上的 Characterization > 核心链路集成 > 复杂逻辑
单元"的顺序排批次。简单 CRUD 不进计划。

输出用表格总结。保存到 docs/test-plan.md。
```

产出：`docs/test-plan.md`。

review 重点（吸收 §2.3 表(3) 的通过标准）：

| 检查点 | 通过标准 |
|--------|---------|
| 每批数量 | 严格 1-3 个，最好 1 个，不能更多 |
| 批次顺序 | 按价值优先级排（见 5.3） |
| 简单 CRUD | 真的没进计划 |
| 每批字段 | 批次号、测试类型、覆盖核心链路、预期工作量齐全 |

为什么要拆成这么小的批次？因为补测试是要你**逐行 review 的**——后面会强调，测试到底测的是不是"实际行为"，这事儿 AI 自己说不清，必须你盯着看。<span style="color: red; font-weight: bold;">一批塞 10 个测试，你根本 review 不过来，质量就崩了。</span>

#### (2) 让 AI 一批一批补

目标：按计划补出测试。这里最关键的是第 2 章讲的"凭实际行为写断言"——原理在那一章已经详述过，这里只引用，不重复展开。

```
按 docs/test-plan.md 的第 1 批，给项目补出对应的测试。

对 Characterization Test 类型：先跑一次现有代码记录实际行为，
再把行为转成断言。不要凭"应该是什么"写断言，凭"实际是什么"写。

对集成测试类型：需要真实启动应用 + 数据库。
用 SpringBootTest 的方式起完整 context 跑。

补完跑一遍 mvn test 确保都通过。
输出用表格总结每个测试覆盖的场景、预期结果、实际跑出来的状态。
```

产出：第 1 批 1-3 个测试 + 跑通的结果。

review 重点（最关键，吸收 §2.3 表(3)）：

| 检查点 | 通过标准 |
|--------|---------|
| 断言来源 | 测的是"现在实际做什么"，不是"AI 觉得应该做什么" |
| 场景覆盖 | 场景对不对，edge case 有没有漏 |
| 能跑通 | 每批补完跑 `mvn test` 确保通过 |
| Characterization Test | 凭"实际行为"写断言 |
| 集成测试 | 用 SpringBootTest 起完整 context + 真实数据库 |
| 节奏 | review 通过后才进下一批 |

这几项里最关键的是断言来源这一项——如果测试写成了"AI 认为代码应该做什么"，<span style="color: red; font-weight: bold;">那它不是兜底网，而是一厢情愿的愿望清单，改造时根本不会报警。</span>其余各项是工程性的，断言来源是认知性的，最容易出问题。

review 通过后开第 2 批（迭代提示词，原样保留）：

```
按 docs/test-plan.md 的第 2 批补测试，
参考第 1 批已经跑通的测试风格，保持一致。
其他要求同前。
```

按这个节奏直到所有 P0 批次跑完、`mvn test` 全绿。

### 5.3 补测试的批次顺序

<img src="imgs/aicmigr-16-safeguard-04-process-recap/b580b86302822a10dd3dd515ae456138_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

批次顺序不是随意的，按价值优先级排：

```mermaid
flowchart LR
    A["改造路径Characterization测试"] --> B["核心链路集成测试"]
    B --> C["复杂逻辑单元测试"]
```

为什么是这个顺序？

- **改造路径上的 Characterization Test 优先**：这些测试直接保护你即将要改的代码，改造时一旦行为变化立刻报警，价值最高。
- **核心链路集成测试其次**：保证整条业务流程不断，是兜底的中坚力量。
- **复杂逻辑单元测试最后**：单元测试粒度小，改造时容易因为小改动而需要重写，优先级最低。

<span style="color: red; font-weight: bold;">简单 CRUD 永远不进计划——CRUD 代码 AI 几分钟就能重写，给它写测试性价比极低。</span>

到这里，P0 测试缺口全部补上。

## 6. 场景四：让 CI 当你的兜底护栏

<img src="imgs/aicmigr-16-safeguard-04-process-recap/7ef6fcf5d8044d28a8d2afd9cc64c184_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

传统项目里 CI 是"提交前的质量门禁"——人写完代码，push 上去，CI 跑测试，挂了就不让合。这套机制你已经很熟。

到了 AI 协作的场景，CI 的角色变了：它不再只是挡人，而是"<span>每次 AI 改完代码自动验真</span>"的那道闸。AI 改代码快、改得多，你不可能每次都手动 `mvn test`。让 CI 在每次 push 时自动跑全套测试、失败就 block merge，AI 才有一个不依赖你盯着的反馈回路。这一章就是把这条回路接上——它是改造前所有护栏的最后一道闸。

这里有个容易踩的坑：很多老项目其实已经有 CI（Jenkinsfile、GitLab CI、甚至裸的 shell 脚本），只是没跑测试或者触发条件不对。如果你让 AI 上来就直接写一份新的 `.github/workflows/test.yml`，很可能把已有的构建逻辑、部署步骤、通知机制全覆盖掉。所以这一章的顺序是固定的：<span>先让 AI 摸清现状，再让它写新配置</span>。

### 6.1 场景目标与关键产出

目标很单一：让 push 自动触发测试、失败 block merge。

关键产出就一个文件——`.github/workflows/test.yml`（或你项目对应平台的 CI 配置）。拿到这个文件、commit 进仓库、第一次绿色构建跑出来，场景四就结束了，改造前的全部护栏也就到位了。

### 6.2 提示词与产出物

三个提示词按顺序走：分析现状 → 写完整 workflow → 跑通。每一步的 review 标准都直接来自场景四的检查清单。

#### (1) 让 AI 分析项目当前 CI 状态

目标：先搞清楚现状，别上来就写新配置。

```
扫一下项目里有没有现成的 CI 配置（看 .github/workflows/、.gitlab-ci.yml、Jenkinsfile 之类）。
如果有，告诉我现在跑了什么、什么时候触发、有没有跑测试。
如果没有，告诉我项目代码托管在哪个平台，建议用哪种 CI。
输出用表格总结。
```

产出：CI 现状分析（不落独立文件，在对话里汇报即可）。

review 重点（吸收检查清单）：

| 检查项 | 通过标准 |
|--------|---------|
| 各平台 CI 配置已扫 | `.github/workflows/`、`.gitlab-ci.yml`、Jenkinsfile 都看过了 |
| 现有 CI 行为说清 | 跑了什么、什么时候触发、有没有跑测试已知 |
| 无 CI 时给建议 | 代码托管平台已识别，建议用哪种 CI 已给出 |

#### (2) 让 AI 写完整 CI workflow

目标：产出一份能直接 commit 进仓库就跑的配置，不用你再手改。

```
基于上一步的分析，给我写一份完整的 CI workflow。要求：

- 触发条件：push 到任何分支 + 提 PR 时
- 运行环境：用项目对应的 JDK 版本（看 pom.xml 里 java.version）
- 启动需要的中间件（参考 docker-compose.dev.yml）
- 跑 mvn clean test，失败就 block merge
- 输出测试报告到 CI artifact 区方便 review
- 加合理的 cache（Maven 依赖缓存）让跑得快一点

输出完整的 .github/workflows/test.yml（或对应平台的配置文件），
我直接 commit 进仓库就能跑。
```

产出：`.github/workflows/test.yml`（或 `.gitlab-ci.yml` / `Jenkinsfile`）。

review 重点（吸收检查清单）：

| 检查项 | 通过标准 |
|--------|---------|
| 触发条件 | push 到任何分支 + 提 PR 时都触发 |
| 中间件配置 | 参考 `docker-compose.dev.yml` 起全，不缺数据库/缓存 |
| JDK 版本 | 对齐 `pom.xml` 里的 `java.version`，别凭感觉填 |
| 测试报告 | 输出到 CI artifact 区，失败也能下载看 |
| Maven cache | 依赖缓存已加，别每次都全量拉 |
| block merge | `mvn clean test` 失败要真的挡住合并 |

#### (3) 跑通 CI

目标：第一次真跑，跑通为止。这里又一次用到自主修复原则——和 install 脚本那条铁律一样：<span>连续 3 次报同一个错才停下来汇报</span>，中间的小波折让 AI 自己 debug 自己修。

```
push 一次代码触发 CI，看能不能跑通。失败就自己 debug 自己修，
跟 install 脚本一样的自主修复原则（连续 3 次同错才停下来汇报）。
最终跑通后告诉我 CI 跑一次需要多久。
```

产出：CI 第一次绿色构建。

review 重点（吸收检查清单）：

| 检查项 | 通过标准 |
|--------|---------|
| 触发成功 | push 一次能触发 CI |
| 自主修复 | 失败自己 debug 自己修，3 次同错才停 |
| 测试报告 | 能从 artifact 区下载 |
| 耗时合理 | 单次跑完在 10 分钟内（超了通常是 cache 没生效或中间件起太慢，回去查提示词 12） |

到这里，改造前的所有护栏——可运行的环境、行为基线测试、兜底测试、CI 兜底——全部到位。从下一章开始，就是真正动业务代码的改造了。

## 7. 一键自主执行模式

<img src="imgs/aicmigr-16-safeguard-04-process-recap/385e23e1108793d789860d63e3558c8c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面四个场景之所以让你一个个手工跑，是为了让你看清每一步的产出和 review 点——这是一条学习路径。

真正上手之后，手工跑就成了负担：你会希望一次粘贴、Claude Code 自主跑完所有步骤、遇到问题自己修、跑完自己验收。这是一条生产路径。两条路径背后是同一套流程，差别只在于谁来推进。

### 7.1 为什么需要一键模式

把四个场景串起来跑，中间要切 13 次提示词、做 13 次 review，任何一个环节卡住都要你亲自介入。这套流程重复跑的边际价值很低：你已经在场景一到场景四里看清楚了每一步在做什么、为什么这么做，剩下的就是把约束写死、让 AI 自己跑。

一键模式要解决的不是"流程变短"，而是"让 AI 在没有你盯着的情况下也守得住边界"。所以这段提示词不能只是把前面 13 个提示词拼起来——每个关键约束都必须显式写进去，否则 AI 默认会"贪快"，把硬约束悄悄放松。

下面这段提示词就是干这个的。<span style="color: red; font-weight: bold;">整段粘贴到 Claude Code，去吃个午饭，回来就齐了。</span>

### 7.2 一键流程提示词

```
我刚跑完第二部分，docs/ 里有架构图、模块图、依赖图、接口清单、数据模型五份资产，
根目录有 CLAUDE.md，.claude/skills/ 下有 docs-auto-sync skill。

现在帮我完整跑通改造前的护栏建立流程，
全程自主推进，遇到问题自己修、自己 review、自己决定下一步，
不要每一步都问我。

请按以下顺序执行：

第一步：环境搭建
- 基于 docs/external-deps.svg + application*.yml + pom.xml 生成 docs/env-checklist.md
- 生成本地安装脚本 scripts/install-deps.sh 并执行（遵循自主修复原则：连续 3 次同错才停）
- 生成依赖启停脚本 deps-start.sh / deps-stop.sh / deps-status.sh
- 顺手给一份 docker-compose.dev.yml 备选
- 跑 mvn package + 启动应用，记录 docs/startup-log.md
- 用 curl 跑 5 个核心接口冒烟，记录 docs/smoke-test-result.md

第二步：测试摸底
- 基于已有资产列 8 条核心链路，保存到 docs/critical-paths.md
- 扫现有测试状态，对照核心链路标覆盖度，保存到 docs/test-status.md
- 跑一遍 mvn test 看真实结果，追加到 test-status.md
- 算出测试缺口清单 docs/test-gaps.md，P0 不超过 10 个，P1 不超过 10 个，每项标场景描述和建议类型

第三步：补 P0 测试
- 拆补测试计划 docs/test-plan.md，每批 1-3 个最好 1 个
- 按计划一批一批补，每批跑通了才进下一批
- Characterization Test 必须凭"实际行为"写断言，不凭"应该"
- 所有 P0 批次跑完确认 mvn test 全绿

第四步：CI 集成
- 分析项目当前 CI 状态
- 写一份完整 .github/workflows/test.yml（或对应平台）
- push 触发一次 CI 跑通

自主原则：
- 每一步跑完自己 review 输出质量，不合格自己重跑
- 遇到失败自己 debug 自己修（除非连续 3 次同一错误）
- 测试别贪多，每批严格 1-3 个最好 1 个
- 测试断言凭实际不凭应该
- 所有步骤跑完后，生成一份 summary.md，列出每个产出文件、
  每份资产的主要内容概括、你认为还需要人工确认的地方
  （特别是补的测试是否都凭"实际行为"写的）

不要打断来问我。有判断不清的地方先做一个合理选择，
在 summary 里标记。跑完再汇报。
```

粘贴完等 Claude Code 自己跑。<span style="color: red; font-weight: bold;">时间大概 1-2 小时（环境搭建快、补测试慢，主要时间花在补测试和等 mvn 编译）。</span>

### 7.3 这段提示词的设计动机

这段提示词不是把前面 13 个提示词拼起来那么简单。每个设计点都对应一个具体的坑：

| 设计点 | 动机 |
|--------|------|
| 所有 1-3 个、自主修复、3 次兜底等关键约束都明确写进去 | 这些约束在前面反复强调，跑一键流程时 AI 默认会"贪快"批量补一堆测试，必须把约束写得很硬 |
| "测试断言凭实际不凭应该"单独列出来 | 这是最值钱的洞察，AI 一不留神就会按"业务直觉"补断言导致测试无效 |
| 人工确认点在 summary 里集中暴露 | 让 AI 把"我不确定的地方"都攒到最后，特别是测试断言这种容易踩坑的，标出来让用户重点 review |

### 7.4 跑完护栏后的目录结构

<img src="imgs/aicmigr-16-safeguard-04-process-recap/ce4876a05a9a7230f78b7ba2905f333a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

跑完四个场景，项目里会多出这些资产。每一份都对应一个明确的共识或机制：

```
spring-ai-alibaba-admin/
├── CLAUDE.md
├── .claude/skills/
│   ├── docs-auto-sync/
│   │   └── SKILL.md
│   └── env-bootstrap/  ← 场景一挖到的新 skill
│       └── SKILL.md
├── .github/workflows/
│   └── test.yml  ← CI 护栏
├── scripts/
│   ├── install-deps.sh
│   ├── install-log.md
│   ├── deps-start.sh
│   ├── deps-stop.sh
│   └── deps-status.sh
├── docker-compose.dev.yml  ← Docker 备选
├── docs/
│   ├── architecture.svg
│   ├── module-deps.svg
│   ├── external-deps.svg
│   ├── api-list.md
│   ├── data-model.md
│   ├── data-model-er.svg
│   ├── env-checklist.md
│   ├── startup-log.md
│   ├── smoke-test-result.md
│   ├── setup-guide.md
│   ├── critical-paths.md
│   ├── test-status.md
│   ├── test-gaps.md
│   └── test-plan.md
└── src/test/  ← P0 测试已补
```

这就是一个老项目的完整 AI 协作基础设施 + 改造前护栏。每次 push 触发 CI，每天上班 deps-start，docs/ 里每份资产都对应一类共识，CLAUDE.md 是 AI 的常识门面，两个 SKILL 守着重复流程的自动化。

## 8. 结语：改造前准备的完整闭环

<img src="imgs/aicmigr-16-safeguard-04-process-recap/ad3e5b7d3b840a8c7b463d06116f6bb0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

走到这里，三件事已经全部就绪：<span style="color: red; font-weight: bold;">理解项目（脑图）→ 跑通项目（环境）→ 护住项目（测试 + CI）</span>。脑图让你看清代码全貌、知道改动会落在哪一层；可复现的运行环境让 AI 和你站在同一起点；Characterization 测试加 CI 则把回归风险兜在每一次提交上。三件事缺一不可：缺脑图，AI 不知道往哪改；缺环境，改了没法验证；缺测试，改完不知道对不对。改造前的准备做完了，可以动手做真实需求改造。
