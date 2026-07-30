---
title: 传统项目迁AI 32：挑战开源 - 流程回顾
author: fangkun119
date: 2026-07-05 12:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-32-opensource-04-process-recap/cover.jpg
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
aicmigr-32-opensource-04-process-recap
传统项目迁AI 32：挑战开源 - 流程回顾
-->

## 1. 开篇：为什么要打包这套工作流

<img src="imgs/aicmigr-32-opensource-04-process-recap/a892da78de4e2d72877b6abe08da27d5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 1.1 传统工程师卡在哪

想象一个场景：leader 丢过来一句话——"去给某个开源项目提个 PR，建立团队在社区的存在感"，让你落地。

传统工程师拿到这种任务会卡在三件事上。第一，代码不是你的：开源项目的代码归属社区，你不能像改自家业务代码那样随手改。第二，方向不是你的：改什么得看项目 roadmap 和维护者的胃口，不是你想加什么功能就加什么。第三，维护者不归你管：PR 提上去之后合不合、什么时候合、要不要你改，全看维护者的节奏。

这三件事凑在一起，让"给开源提 PR"变成一件传统工程经验直接失灵的事——<span style="color: red; font-weight: bold;">写代码本身不难，难的是走通整套协作姿势。</span>

本篇要做的事，就是把"走通整套姿势"标准化，<span style="color: red; font-weight: bold;">打包成一份碰到任何开源项目都能反复用的清单</span>。

还有一件事必须开篇就说：<span style="color: red; font-weight: bold;">找项目这件事，AI 帮你筛 80%，剩下 20% 是工程师的判断</span>。后面经验分享章节会把那 20% 的隐性信号讲清楚。

### 1.2 本篇打包的资产

本篇把跑通整套姿势的动作重新组织成四类读者用得上的东西：

| 资产 | 作用 |
|------|------|
| 五阶段 Check List | 上手任何开源项目时直接照表跑 |
| 关键提示词清单 | 覆盖四阶段，可直接复制到 Claude Code 实跑 |
| 一键流程提示词 | 选好项目和 issue 后，让 Claude Code 串行自主跑完整流程 |
| 经验分享 | 补清单学不到、只能靠时间踩出来的隐性知识 |


## 2. 工作流全貌：五阶段一张图

<img src="imgs/aicmigr-32-opensource-04-process-recap/e7bfb2fe9e22ee34c8efb5a71810d8e6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 五阶段速查表

整套工作流横跨五个阶段，<span style="color: red; font-weight: bold;">前两个阶段"想清楚做什么"，后三个阶段"把它做出来并跑通"。</span>

| 阶段 | 核心动作 | 关键产出 |
|------|----------|----------|
| 准备 | GitHub 账号、SSH key、Claude Code、gh CLI 配齐 | 真实开源工程师的工具栈 |
| 找项目 | 三道题标准 + AI 跑出候选 + 找最简单的 PR 入口 | 一个匹配背景的载体项目 + 一个零门槛 PR 目标 |
| 第一个 PR 跑通流程 | fork + clone 之后，让 AI 跑剩余的 branch/commit/push/PR | 一个被合并的 SignYourName 类 PR |
| 第二个 PR 真本事 | 摸项目找方向 → 筛 issue → 让 AI 当助教实现 PR | 一个有真实工程价值的 PR |
| 提高质量 issue | 扫代码找算法层性能问题 → 写高质量 issue 模板 | 一个被维护者认真讨论的 issue |

类比一下，这五个阶段可以映射到传统软件工程的流程：准备阶段对应开发环境搭建，找项目对应需求评审（决定做什么），第一个 PR 对应跑通冒烟流程（建立信心），第二个 PR 对应核心编码（真本事），高质量 issue 对应线上问题排查（发现值得讨论的问题）。映射关系不严格对应，但能帮转型读者快速建立认知坐标。

### 2.2 阶段之间的依赖关系

五个阶段的顺序不能乱，背后有硬依赖。

准备阶段是所有后续阶段的前置条件，缺一项工具栈后面都跑不通。找项目阶段决定载体，载体决定第一个 PR 入口能找到什么。第一个 PR 跑通流程之后，你才掌握 fork → branch → commit → push → PR → review → merge 这条肌肉记忆，第二个 PR 才能聚焦技术内容。第二个 PR 实现过程中扫代码积累的认知，是高质量 issue 的输入。

换句话说，<span style="color: red; font-weight: bold;">前三个阶段是"建立基础"，后两个阶段是"展示真本事"。跳过前面直接做后面，每一个都会卡。</span>

### 2.3 阶段时间预算

| 阶段 | 时间预算 |
|------|---------|
| 准备（一次性） | 30 分钟 |
| 找项目 | 30-60 分钟 |
| 第一个 PR | 30-60 分钟（含等 review） |
| 第二个 PR | 1-1.5 小时 |
| 高质量 issue | 30-45 分钟 |
| 一键流程（全跑通） | 3-4 小时（含几次 review） |

## 3. 找到匹配自己的项目

<img src="imgs/aicmigr-32-opensource-04-process-recap/c8e10eda054b2de766c74e55db566fde_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这个阶段要产出两样东西：一个匹配你背景的载体项目，一个最简单的第一个 PR 入口。核心思路是——<span style="color: red; font-weight: bold;">心里没标准没关系，AI 帮你建标准</span>。

### 3.1 三道题标准：把"什么样的项目适合入手"拆成硬条件

开源新人最常踩的坑是心里没标准。选项目像选赛道，选错了后面几个月都白费。<span style="color: red; font-weight: bold;">三道题本质上就是把"什么项目适合作为第一个 PR 目标"拆成三条可判断的硬条件。</span>

| 题号 | 标准 | 反面例子 |
|------|------|----------|
| ① 真实技术含量 | 不是 demo、不是教程配套仓库、不是单纯 markdown 集合；有真实工程代码、真实测试、真实 issue 在被讨论 | 一个只放笔记的 awesome-xxx 仓库 |
| ② 正在快速成长但还没到顶级 | 几百到几千 star、有持续 commit、有几个核心维护者，但还没到几万 star、没成为行业标准。这种区间的项目最欢迎新人——既活、又有上升空间、维护者还有空 review | Kafka、Redis 这种几万 star 的顶级项目，review 周期几个月 |
| ③ 跟技术栈或职业方向有关联 | 后续要在这个项目上持续投入，完全不相关的项目跑完第一个 PR 之后再提第二个就提不动了 | 做 Java 后端却选了个前端组件库 |

为什么要卡"还没到顶级"这一条？因为顶级项目维护者每天收到几十个 PR，新人 PR 排在队尾几个月轮不到 review，信心先被磨没。<span style="color: red; font-weight: bold;">几百到几千 star 的项目维护者还有空手把手带你，这才是新人该去的地方。</span>

#### (1) review 重点

AI 给的 5 个候选里，每一个都要满足三道题。如果有候选 star 数超 5 万或者最近 3 个月没 commit，这个候选要砍掉。AI 推荐之后还要靠自己判断——第 9 章经验分享会展开几条 AI 推荐之外的隐性信号。

### 3.2 找最简单的 PR 入口：项目方设计的入口优先

挑好项目之后，下一步是找最简单的 PR 入口。第一个 PR 不证明你技术多牛，只证明能走通流程。所以入口越简单越好，改一行 markdown 跑完整 PR 流程是最理想的。

入口有三档优先级：

| 优先级 | 入口类型 | 为什么 |
|--------|----------|--------|
| 最优 | 项目方为新人专门设计的入口（签名墙、贡献者列表加自己） | 改一行 markdown，维护者设计出来就是给新人走流程用的 |
| 次选 | good first issue 标签下的 typo 或文档补充 | 改动小、不影响功能、维护者 review 轻松 |
| 别选 | 超过 50 行的 good first issue | "50 行实际改完发现是 500 行"是新人最常见的坑，它不是真的 good for first |

#### (1) 让 AI 扫仓库找入口

挑好项目，先 git clone 下来，在仓库目录下打开 Claude Code，丢这一段提示词。

提示词原文：

```text
我刚把这个项目的仓库 clone 下来了。我要做我的第一个开源 PR,
目标是走通完整流程,不追求技术难度。

请你扫一遍仓库的 README、CONTRIBUTING.md、docs/,看有没有专门
为新人设计的"first PR"入口。比如:
- 项目方贴心准备的简单贡献入口(签名墙、贡献者列表之类)
- good first issue 标签下的 typo 修订或文档补充
- 任何只需要改一行就能体验完整 PR 流程的入口

如果有,告诉我具体路径 + 怎么改 + 怎么提 PR。
```

#### (2) review 重点

理想入口是项目方为新人专门设计的"签名"或"列表加自己"那种，改一行 markdown 就行。如果没有，退而求其次找 typo 修订或文档补充。别选超过 50 行的 good first issue，那不是真的 good for first。

### 3.3 让 AI 推荐项目

如果还没有候选项目，第一步是让 AI 按三道题给 5 个候选。打开 Claude Code，丢这一段提示词。

提示词原文：

```text
我想开始给开源项目贡献代码,作为我的第一个开源 PR 的目标项目。

我的筛选标准是三道题:
1. 有真实技术含量,不是 demo 项目
2. 正在快速成长但还没到顶级(几百到几千 star、有持续 commit、
   有几个核心维护者,但还没到几万 star、没成为行业标准)
3. 跟我的技术栈或职业方向有关联

我的背景:[填你自己的方向,比如:做基础软件方向,熟悉 Java、Go、
Python,Rust 在学,对消息队列、数据库、Agent 框架这些方向感兴趣]

按这三道题给我推荐 5 个候选项目。每个项目说清楚:
- 项目定位
- star 数和最近一年的 commit 活跃度
- 维护者背景(独立团队还是大公司主导)
- 为什么适合我作为第一个 PR 目标
- 它的 good first issue 入口在哪
```

跑完这一步，手里就有了 5 个候选 + 每个候选的 good first issue 入口。挑一个三道题全打中的进入第 4 章。

## 4. 跑通第一个 PR：三个护栏

<img src="imgs/aicmigr-32-opensource-04-process-recap/c8b852ca32761158463ed4a14bd23047_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第一个 PR 的核心动作是让 AI 跑完 fork 之后的剩余流程：建 branch、改文件、跑 lint、commit、push、写 PR 描述、提 PR。这个场景的灵魂是——<span style="color: red; font-weight: bold;">走通流程比技术难度重要</span>。

但 AI 在这种长流程里容易瞎搞：报错了自己改自己测陷入循环、对 markdown 改动瞎跑几分钟的编译、该停下来问你的时候自己拍板。要在提示词里设三个护栏防住它。<span style="color: red; font-weight: bold;">护栏这个概念可以类比成给新手司机配的三道保险——不是不信任 AI，是长流程里它确实会走神。</span>

### 4.1 三个护栏防 AI 瞎搞

| 护栏 | 防什么 | 怎么写进提示词 |
|------|--------|---------------|
| 每步执行前告知 + 报错立刻停 | 防 AI 在报错时陷入"自己改自己测"的循环 | "每一步执行前告诉我你要做什么，执行后告诉我结果。任何报错立刻停下来问我，不要自己瞎修" |
| 别瞎跑 cargo build（针对 Rust 项目） | 防 AI 在大型 Rust 项目花几分钟编译 markdown 改动 | "这是个 markdown 改动，只跑相关检查就行。别瞎跑 cargo build" |
| gh CLI 优先 + fallback 人工 | 真实工程师工具链：能自动化的自动化，自动化不了的 fallback 人工 | "如果系统装了 gh CLI，直接 gh pr create 把 PR 提了。没装就把 PR 描述输出给我，我自己去 GitHub 点提交" |

第三个护栏值得多说一句。gh CLI 是 GitHub 官方命令行工具，装了它 AI 就能直接提 PR；没装就退回人工，由你去 GitHub 网页点提交。这是真实工程师的工具链——<span style="color: red; font-weight: bold;">能自动化的自动化，自动化不了的 fallback 人工</span>，不要硬撑。

#### (1) 完整流程跑通的标志

跑完本章后面这套提示词，第一个 PR 提了，等维护者 review。SignYourName 这种 PR 通常一天内被合并，耗时 30-60 分钟。

### 4.2 以 RobustMQ 为例实跑

下面以 RobustMQ 作为示范项目跑一遍。选 RobustMQ 是因为它三道题都打中：基础软件赛道、活跃但还没到顶级、Rust 写的跟学 Rust 的方向对得上。它还在 docs 里给新人贡献者准备了一个 SIGNYOURNAME.md 入口，把自己的名字加一行进去就行——正是第 3 章说的最理想入口。

整套工作流跟项目无关，提示词改个项目名，就能复刻到任何项目上。

#### (1) 人工的两步：fork + clone

fork 和 clone 这两步 AI 做不了（涉及 GitHub 账号操作），只能人工。

第一步，打开 `https://github.com/robustmq/robustmq`，右上角点 Fork。

第二步，clone 到本地：

```bash
git clone git@github.com:<your-username>/robustmq.git
cd robustmq
```

#### (2) 让 AI 跑剩余流程

剩下全部丢给 Claude Code。在仓库目录下打开 Claude Code，丢这一段提示词。

提示词原文：

```text
我刚 fork 并 clone 了 RobustMQ 仓库,要给 docs/SIGNYOURNAME.md
加一行我的信息,作为我的第一个开源 PR。

请你帮我把整套流程跑完。每一步执行前告诉我你要做什么,执行后告诉我结果。
任何报错立刻停下来问我,不要自己瞎修。

具体步骤:

1. 建一个新 branch,命名规范跟 RobustMQ 已有 branch 风格对齐
   (比如 feature/sign-your-name-<我的名字>)
2. 找到 docs/SIGNYOURNAME.md,先看一眼现有列表的格式,
   然后在合适的位置严格按同样格式加一行我的信息
3. 跑项目自带的代码格式检查或 lint。看 RobustMQ 的 CONTRIBUTING.md /
   pre-commit hook / Makefile,有什么用什么。别瞎跑 cargo build,
   这是个 markdown 改动,只跑相关检查就行。有报错就修,跑通为止。
4. 写一条 commit message,Conventional Commits 规范,subject
   不超过 50 字符。然后 git add 相关文件并 commit。
5. push 到我的 fork(remote 名是 origin)。
6. 给我写一个 PR 描述,3-5 行,说明这是我的第一个 PR、跟着
   SignYourName 入口跑通流程、表达对 RobustMQ 的好感、语气是
   普通工程师不是 LinkedIn 帖子。
7. 如果系统装了 gh CLI,直接 gh pr create 把 PR 提了。
   没装就把 PR 描述输出给我,我自己去 GitHub 点提交。
```

这段提示词把第 4.1 节的三个护栏全部写进去了。"语气是普通工程师不是 LinkedIn 帖子"这一条值得注意——它防 AI 写出充满"excited to contribute"、"thrilled to join"那种 LinkedIn 风格的 PR 描述，维护者最烦这种。

#### (3) review 重点

跑完场景二，第一个 PR 提了，等 RobustMQ 维护者 review。SignYourName 这种 PR 通常一天内被合并，耗时 30-60 分钟。三个护栏的写法见 4.1 节，这里不再重述。


## 5. 跑通第二个 PR：让 AI 当助教

<img src="imgs/aicmigr-32-opensource-04-process-recap/2182b23a99bfdb398fe88298c6678f59_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第一个 PR 跑通流程之后，第二个 PR 要真有技术含量。这个场景的灵魂是——<span style="color: red; font-weight: bold;">让 AI 当助教，工程师做判断</span>。

为什么强调"助教"这个角色？以 RobustMQ 为例，它是 Rust 写的消息中间件，代码量大、用了大量 async 和 tokio 生态。如果你 Rust 不熟，让 AI 直接写代码你 review 不了；反过来你自己写又写不动。正确的姿势是 AI 解释机制，你做方向和取舍的判断。AI 是助教不是替身，决策权始终在你手里。

### 5.1 三步法与"带任务问"

第二个 PR 分三步：摸项目找方向、筛 issue、实现 PR。

| 步骤 | 做什么 | 产出 |
|------|--------|------|
| 第一步 摸项目 | 按四维度框架扫一遍仓库，针对"找 issue"任务出一份项目地图 | 项目地图 |
| 第二步 筛 issue | 按三条标准从 issue 列表筛 5 个候选 | 排好优先级的 5 个候选 |
| 第三步 实现 PR | 用长提示词让 AI 跑完实现 + 测试 + 提交 | 一个有技术含量的 PR |

这里有个关键姿势叫"带任务问"。你让 AI 给项目地图，如果不告诉它你要干嘛，它会给你一份干巴巴的完整架构介绍——模块分层、类继承、依赖图全画一遍，跟你想找 issue 毫无关系。类比一下，这就像让顾问写报告却不告诉他决策目的，他只能把所有知道的全倒给你。告诉 AI 目的，它才会过滤出有用的那部分。

这条姿势在第 5.2 节的提示词里体现得很明显——明确告诉 AI"只要对找 issue 有帮助的那部分，不要完整架构文档"。

### 5.2 第一步：摸 RobustMQ 项目（针对找 issue 任务）

#### (1) 提示词原文

```text
我在 RobustMQ 项目仓库目录下。我已经成功提了第一个 SignYourName PR,
现在要做第二个 PR,目标是给 RobustMQ 提交一个有真实工程价值的小贡献。

请你按这四个维度给我一份针对"找 issue"任务的 RobustMQ 项目地图:

1. 模块划分。RobustMQ 的代码大致分成几块?每块负责什么?
   (重点关注 MQTT broker、存储引擎、journal server、common 工具等)
2. 测试组织。单测放在哪、集成测试放在哪、怎么跑?
3. 贡献热点。最近三个月哪几个模块改动最多?活跃 contributor 主要在
   哪些模块?(扫 git log 看)
4. 适合新人的入口。看 RobustMQ issues 里 good first issue /
   help wanted 标签下,有没有可以上手的方向?CONTRIBUTING.md
   有什么对新人的硬约束?

不要给我一份完整架构文档,只给我对找 issue 有帮助的那部分。
```

四个维度（模块划分、测试组织、贡献热点、新人入口）不是随便选的——前两个帮你定位代码在哪，后两个帮你定位哪里好下手。贡献热点看 git log 是因为活跃模块维护者关注度高、PR review 快；冷门模块可能几个月没人看。

### 5.3 第二步：让 AI 帮你筛 issue

#### (1) 提示词原文

```text
基于刚才那份 RobustMQ 项目地图,帮我从 RobustMQ 的 issue 列表里筛 5 个
候选,作为我的第二个 PR 目标。

筛选标准:

1. 改动量不大,代码 + 测试加起来 50-200 行
2. 描述清楚,改动是局部的(单文件或单模块内)
3. 不需要深度业务理解,我能 review 得了 AI 写的代码

每个候选告诉我:
- issue 编号和标题
- 它在 RobustMQ 哪个模块(broker / journal / storage / common 等)
- 我做这个 PR 大概要干什么(一两句话)
- 为什么它符合上面三条标准
- 风险点在哪(比如有没有可能改完发现规模超出预期)

5 个候选给我排个优先级,从最稳到最有挑战。
```

三条标准对应三种风险：改动量 50-200 行防规模失控；改动局部防牵一发动全身；不需要深度业务理解防你 review 不了 AI 写的代码。第三条尤其重要——<span style="color: red; font-weight: bold;">你 review 不了的代码，合进去就是埋雷。</span>

#### (2) review 重点：风险点必须列

AI 推荐 issue 时容易忽略"看起来 50 行实际改完发现是 500 行"的隐藏复杂度。每个候选必须显式说"如果改起来比预期大要在哪一刻停下来"——这是新人最容易踩的坑。提示词里"风险点在哪"这一行就是逼 AI 把这件事讲清楚。

### 5.4 第三步：实现 PR（长提示词）

挑好 issue，在 fork 后的 RobustMQ 仓库目录打开 Claude Code。

#### (1) 提示词原文

```text
我要给 RobustMQ 提一个 PR,实现 issue #XXX:[一句话需求描述]。

请你帮我把整套流程跑完。每一步执行前告诉我你要做什么,执行后告诉我结果。
任何报错或不确定的地方立刻停下来问我,不要自己瞎修。

具体步骤:

1. 先读 issue #XXX 的完整描述,然后读 RobustMQ 相关代码:这个 issue
   涉及的模块当前实现长什么样、它依赖哪些 API、上下游有哪些关键调用。
   读完给我一份"我要改什么"的简短 plan(3-5 条)。我审核 plan
   之后才能进下一步。

2. plan 我同意之后,建一个新 branch,命名跟 RobustMQ 风格对齐。

3. 按 plan 写代码。我 Rust 不熟,所以你写完之后:
   - 关键改动逐段告诉我"这里在做什么、为什么这么写"
   - 任何用到 Rust 特有机制的地方(lifetime / async trait /
     ownership / Send + Sync 等),简短解释
   - 不要为了炫技用复杂的 idiom,选最直白的写法
   - 如果有多种合理写法,选 RobustMQ 现有代码里最常用的那种风格

4. 看测试影响。这次改动相关的现有测试在哪?改完后会不会失败?
   失败的话怎么更新?需要新增测试覆盖什么场景?

5. 跑测试。看 RobustMQ 的 Makefile / CI 配置确认怎么跑这个 crate 的
   测试,不要用默认 cargo test 假设。跑通为止。

6. 跑 cargo clippy。所有 warning 都要清掉,不要留 #[allow(...)]
   绕过。

7. 写一条 commit message,Conventional Commits 规范。
   然后 git add + commit。

8. push 到我的 fork。

9. 写一个 PR 描述,包括:
   - 这个 PR 解决的 issue
   - 改动的高层描述(2-3 句)
   - 实现要点(3-5 条 bullet)
   - 测试覆盖了哪些场景
   - 有任何 trade-off 或后续可改进的点,显式说出来

10. 如果装了 gh CLI,直接 gh pr create 提交。没装就把 PR 描述
    输出给我,我自己去 GitHub 提。
```

#### (2) 长提示词的四个关键设计

这段提示词看着长，其实只有四个关键设计点。这四点是从实战里踩出来的，每一点都防一种典型翻车。

| 设计点 | 防什么 | 对应步骤 |
|--------|--------|----------|
| 先 plan 后改 | 防 AI 拿到 issue 闷头写，改完发现方向不对 | 步骤 1：先出 plan 等你审核 |
| 关键改动逐段解释 | 防 AI 写出你 review 不了的代码，AI 当 Rust 助教 | 步骤 3：lifetime/async trait 等机制简短解释 |
| 不为炫技用复杂 idiom | 防 AI 写出高手风格代码，选项目现有最常用写法 | 步骤 3：选最直白写法 |
| 所有 warning 清掉不留 allow 绕过 | 活跃项目对 PR 质量有硬要求，allow 绕过是新人最容易偷懒的地方 | 步骤 6：跑 cargo clippy 清 warning |

第一点"先 plan 后改"值得多说一句。传统工程师拿到需求习惯直接写代码，但在 AI 协作里这是最危险的——<span style="color: red; font-weight: bold;">AI 写完几百行你才发现它理解错了 issue。先让它出 3-5 条 plan，你花两分钟审核，比写完再返工省几个小时。</span>这跟传统软件工程里"设计评审先于编码"是一个道理。

跑完这一步，第二个 PR 提了。耗时 1-1.5 小时。


## 6. 提一个高质量 issue

<img src="imgs/aicmigr-32-opensource-04-process-recap/975adbc8a477fcbe15c311e647dcb696_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二个 PR 提了之后，接下来要做的不是马上提第三个 PR，而是提一个高质量 issue。

### 6.1 为什么 issue 比 PR 更值钱

这件事直觉上反常识——PR 不是比 issue 更"实"吗？放在开源协作的语境里，恰好相反，有两个原因。

第一，issue 比 PR 更难。用费曼的方式讲：PR 是交卷，你已经把题做出来了；issue 是出题，你要发现一个值得讨论的问题。<span style="color: red; font-weight: bold;">前者只要会写代码，后者需要对项目有判断力</span>，知道什么是真问题、什么是噪音。

第二，issue 是跟维护者建立深度连接的入口。一个高质量 issue 会吸引维护者来讨论，这种讨论比 PR review 的对话深度高得多——<span style="color: red; font-weight: bold;">PR review 大多是"这里改一下"，issue 讨论往往是"这个问题要不要修、怎么修、影响什么"</span>，能真正让维护者记住你。第 9 章会讲到，跟核心维护者建立连接是开源最被低估的收益，issue 就是建立这种连接的最好方式。

### 6.2 扫代码找算法层问题

#### (1) 提示词原文

```text
帮我扫一遍 RobustMQ 的代码,找潜在的性能问题。重点看:

1. 不必要的.clone() 或.to_owned(),特别是热路径上(MQTT 消息处理、
   存储读写、broker 转发)。
2. async 函数里持有同步 Mutex(应该用 tokio::sync::Mutex 或者
   重构掉)。
3. 锁的粒度太粗,把不该锁住的代码包进去了。
4. 在循环里反复分配 Vec / String / HashMap(可以预分配的场景)。
5. Stream / iterator 用法低效,比如 collect 之后又遍历的可以
   直接 chain。

不要找 clippy 已经能查出来的(RobustMQ CI 自己会跑)。
找那种需要看上下文才能发现的真问题。

给我 5 个候选,每个说清楚:
- 文件路径和行号
- 问题描述(具体哪段代码 + 为什么是问题)
- 影响范围(只有 corner case 才会触发,还是日常路径上)
- 修复思路(不要写代码,讲清楚改动方向就行)
```

为什么要强调"不要找 clippy 已经能查出来的"？因为项目 CI 自己会跑 clippy，那种问题维护者早就修完了。你要找的是需要看上下文才能发现的真问题——这才是 issue 的价值所在。

#### (2) review 重点：挑算法层面有改进的那一个

5 个候选里挑哪一个？原则是挑算法层面有改进的，不是挑最简单的。

"多余的 collect"、"多余的 clone"那种 1 行就能改的代码风格问题，维护者反应通常是"直接提 PR 删一行就行，提 issue 干嘛"。这种问题走 issue 流程是浪费。算法层面有改进、修复需要讨论方向、需要测试验证的问题，才值得走 issue——因为它能引出真正的讨论。

### 6.3 写高质量 issue 模板

#### (1) 提示词原文

```text
基于上面那个 [具体问题描述,比如 broker 转发热路径上的 O(n²) 过滤]
的性能问题,帮我写一个给 RobustMQ 提的 GitHub issue。

要求:

1. 标题简洁,一句话概括问题(<80 字符)

2. 正文按这几节:
- Description:问题是什么,3-5 句话,讲清楚 [核心复杂度或机制
  问题]
- Reproduction:怎么复现(具体条件 + 触发路径)
- Impact:影响什么场景、严重程度,生产环境量化一下
- Suggested fix:可能的修复方向(不要写代码,讲思路)
- Environment:RobustMQ 版本、Rust 版本、OS

3. 语气是普通工程师,不是 LinkedIn 帖子,不是问问题,是报告问题

4. 显式说"我可以在确认方向后跟一个 PR",给 RobustMQ 维护者一个
信号:我不只是路过的伸手党
```

五节模板（Description / Reproduction / Impact / Suggested fix / Environment）对应维护者收到 issue 时脑子里会问的五个问题：这是什么、怎么复现、影响多大、怎么修、你的环境是什么。一节不漏，维护者才不用来回追问。

#### (2) review 重点：最后一条是高质量 issue 的标志

最后一条"我可以跟 PR"是高质量 issue 的标志。它告诉维护者你不是只想刷 contribution 数字的伸手党，愿意接着把这件事做完。很多时候维护者看到这一句就会主动来 review 和讨论。

"语气是普通工程师，不是 LinkedIn 帖子"这一条和第 4 章第一个 PR 描述的要求一脉相承——报告问题就好好报告，不要抒情。

跑完这一步，第一个高质量 issue 也提了。耗时 30-45 分钟。

到这里，第一个 PR + 第二个 PR + 第一个 issue 全部跑完。GitHub 个人页面上有了 RobustMQ 这个项目的真实贡献痕迹。


## 7. 一键流程：让 Claude Code 自主跑完整链路

<img src="imgs/aicmigr-32-opensource-04-process-recap/f0e7ba76e04733904531d1b455dd0202_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面四个场景一个个跑，是为了让你看清每一步的产出和 review 点。真正上手之后，你会希望一次粘贴、Claude Code 自主跑完整流程、关键决策点停下来等你判断。这一章就是那个"一次粘贴"的提示词。

### 7.1 一键流程的边界

在给提示词之前，必须讲清楚它的边界——一键流程不管哪些事。

不管 fork 这一步。GitHub 账号操作 AI 做不了，fork 必须人工。也不管挑哪个项目和挑哪个 issue——<span style="color: red; font-weight: bold;">这两个决策必须人来挑，AI 替你拍是最危险的</span>。

它从已经选好项目，并 fork + clone 完这一步开始。下面这段提示词的默认项目是 RobustMQ，也可以换成 AI 推荐出来的任何项目。

### 7.2 一键流程提示词

提示词原文：

```text
我要给开源项目持续贡献代码。当前已经 fork+clone 了 RobustMQ
(或者你换成自己的项目),本地仓库在当前目录。请按以下顺序执行,
关键决策点停下来等我。

第零步:摸项目(必做)
- 用四维度框架扫一遍仓库:模块划分 / 测试组织 / 贡献热点 / 新人入口
- 输出"针对找 issue 任务的项目地图"
- 不要给完整架构文档

第一步:第一个 PR(SignYourName 类入口)
- 找项目方为新人设计的"first PR"入口(签名墙 / 贡献者列表)
- 没有就找一个 typo 修订或文档补充
- 走通完整流程:branch / 改文件 / lint / commit / push / PR 描述 / 提 PR
- 跑完告诉我 PR 链接,然后停下来等 review

第一个 PR review 期间(等待时间),并行做:
第二步:筛第二个 PR 的 issue
- 按三条标准筛:改动量 50-200 行 / 局部改动 / 我能 review
- 给我 5 个候选 + 优先级排序
- 停下来等我挑

我挑完之后:
第三步:实现第二个 PR
- 先读 issue + 相关代码,给我一份 plan,等我审核
- plan 同意之后写代码,关键改动逐段解释
- 补测试,跑测试到通过
- 跑 lint(cargo clippy 之类),所有 warning 都清,不留 allow 注解
- commit / push / PR 描述 / 用 gh CLI 提 PR

第四步:扫漏洞,提高质量 issue
- 扫静态代码找性能问题(重点看不必要的 clone / async 锁 / 循环内分配 / iterator 低效用法)
- 排除 lint 能查出来的
- 给我 5 个候选,优先选算法层面有改进的(不是 1 行能改的代码风格)
- 我挑完之后,帮我写一个高质量 issue,显式带"我可以跟 PR"信号

自主原则:
- 每步跑完自己 review 输出质量,不合格自己重跑
- 失败自己 debug 自己修(除非连续 3 次同一错误)
- 任何"我能在 X 之内做完"的预估,如果实际超 1.5 倍,停下来报告
- 不要替我拍板 issue 的选择和修复方向

跑完输出 contributions.md,列每个产出 + 每个的 PR / issue 链接 +
我应该重点 review 的地方。
```

### 7.3 提示词设计的四个 why

这段提示词看着像把前面四章拼起来，其实有四个设计点是从实战里磨出来的，每一点都解决一个 AI 协作的具体问题。

| 设计点 | 解决什么问题 |
|--------|-------------|
| 第一个 PR 期间并行做第二步筛 issue | 第一个 PR 提完等 review 通常要几小时到一天，这段空窗期最适合找下一件事。AI 不会主动并行做事，必须显式告诉它什么时候开始下一步 |
| issue 选择和修复方向显式让 AI 停下来等人 | 这两件事 AI 替你拍是最危险的——挑了一个写完才发现规模超预期的 issue，或修复方向跟项目方向不一致，几小时白白浪费 |
| lint warning 不留 allow 注解 | 开源协作的硬要求。新人最容易在这里偷懒，必须显式禁掉 |
| 超 1.5 倍预估时间停下来报告 | AI 容易陷在某个细节上反复打磨，给它装的护栏——超时就停下来让你看，而不是一直跑直到上下文耗尽 |

第四个设计点"超 1.5 倍预估时间停下来报告"值得多说一句。AI 协作里有一种典型翻车：你让它修一个 bug，它修完之后开始"顺手优化"周边代码，优化完又发现新的可改进点，几轮下来上下文耗尽、原来的 bug 没修完。<span style="color: red; font-weight: bold;">超时护栏就是防这种"打磨成瘾"。</span>


## 8. 全流程 Check List 与反模式

<img src="imgs/aicmigr-32-opensource-04-process-recap/741588ae621febff5e5673d8cbe576c2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一章把前面四章的动作浓缩成两张表，上手新项目时直接照表跑，不需要回到正文。时间预算见第 2.3 节。

### 8.1 五阶段 Check List

| 阶段 | 必做硬性条目 | 必停决策点 | 必 review 的产出 |
|------|--------------|-----------|-----------------|
| 准备 | GitHub 账号、SSH key、Claude Code、gh CLI 都配齐 | 无 | gh CLI 能不能 `gh auth status` 通过 |
| 找项目 | 三道题都打中、star 数 < 5 万、近 3 个月有 commit | 5 个候选里挑哪一个 | good first issue 入口是不是项目方专门设计的（签名墙最优） |
| 第一个 PR | SignYourName 类一行改动、跑了相关 lint（不是 cargo build）、Conventional Commits | 报错时立刻停下来 | PR 描述是普通工程师语气、不是 LinkedIn 帖子 |
| 第二个 PR | 改动量 50-200 行、plan 先于代码、关键改动逐段解释、所有 warning 清掉 | issue 选择、修复方向 plan 审核 | PR 描述含 trade-off / 后续可改进点 |
| 高质量 issue | 算法层有改进（非 1 行能改的代码风格）、5 节模板齐全 | 挑哪一个性能问题、修复思路 | 显式带"我可以跟 PR"信号 |

### 8.2 反模式速查（不要做这些）

反模式比正确姿势更值得记——因为它每个都对应一种典型翻车。

| 反模式 | 后果 | 正确姿势 |
|--------|------|---------|
| 挑 star 数 > 5 万的顶级项目 | review 周期几个月，新人磨没信心 | 三道题筛 + AI 推荐 + 隐性信号过滤（见第 9 章） |
| 挑 > 50 行的 good first issue | 实际改完发现是 500 行 | 找签名墙类入口或 typo / 文档补充 |
| 让 AI 在报错时自己改自己测 | AI 陷入循环，时间耗尽 | 提示词设护栏：报错立刻停下来问（见第 4 章三个护栏） |
| markdown 改动跑 cargo build | 几分钟编译纯 markdown 改动 | 只跑相关 lint |
| 拿到 issue 闷头写代码 | 改完发现方向不对 | 先 plan 后改，工程师审核（见第 5 章长提示词） |
| 留 `#[allow(...)]` 绕过 warning | 维护者直接 close PR | 所有 warning 都清掉 |
| issue 选 1 行能改的代码风格问题 | 维护者说"直接提 PR 删一行" | 选算法层有改进的问题（见第 6 章） |
| issue 不带"我可以跟 PR"信号 | 维护者当作伸手党 | 显式说愿意跟 PR |

## 9. 动作之外的事：经验分享

<img src="imgs/aicmigr-32-opensource-04-process-recap/d39fc4a5c7cfb1d542de495cfdc4a5a2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

实操部分到第 8 章结束。但开源做久了会发现，真正决定能走多远的不是这些动作，是动作之外的几件事。这一章把前面学不到的东西补给读者。

### 9.1 选项目还有 AI 推荐之外的隐性信号

三道题筛出来的是候选，但要进一步看三个东西。这三个东西 AI 不会主动告诉你，得自己查。

| 隐性信号 | 怎么看 | 不达标的后果 |
|----------|--------|-------------|
| PR 列表里 review 速度 | 扫最近 20 个 PR，看从提出来到 merge 平均多久 | 超过一周的项目对新人不友好，PR 提上去几个月没人看 |
| 维护者构成 | 看有几个核心维护者，是否分散 | 只有 1-2 个核心维护者，你的 PR 完全靠他们的状态决定迭代速度 |
| 最近 6 个月新增 contributor 数 | 扫 git log 看新 author | <span style="color: red; font-weight: bold;">新人持续进来意味着 onboarding 路径已被走通，你不是第一个吃螃蟹</span> |

三条信号合起来，<span style="color: red; font-weight: bold;">能筛掉那种"星光闪闪但已经停滞"的僵尸项目</span>。<span style="color: red; font-weight: bold;">这些项目最坑新人——看起来活，star 数还在涨，实际上没人在 review。</span>

我的判断是：AI 帮你筛出三道题全打中的 5 个候选之后，这三条隐性信号才是你最终挑哪一个的依据。<span style="color: red; font-weight: bold;">star 数和 commit 数会骗人，review 速度和 contributor 流动性不会</span>。

### 9.2 PR 被 close 了别憋着

第一次 PR 被 close 时心里很不舒服，觉得维护者不识货。后来才明白，PR 被 close 是开源里最常见的事，跟你写得好不好没关系，跟项目当下状态有关系——可能这个方向他们刚否决过、可能功能已经在另一条 branch 上实现了、可能就是没空看。

被 close 之后做三件事：

第一，礼貌问原因。一句话就够：Could you help me understand which part is out of scope?（能帮我理解哪部分超出范围了吗）。这不是抬杠，是请教——知道为什么被 close，下一个 PR 才不会犯同样的错。

第二，把代码留在自己 fork 里。可能哪天项目方向变了就用上了。我自己就有被 close 的 PR 半年后被维护者主动找回来"你当时那个实现还在吗"的经历。

第三，换一个 issue 继续。别把 PR 被 close 当成对你能力的判决——开源是协作不是考试，维护者 close 你的 PR 不是在否定你，是在管理他们的项目。

### 9.3 长期主义的复利

讲一段亲身经历。我曾经给 Kafka 提了两个 PR 就停了，跟同期开始的另一个人坚持了四五年成了 Kafka 的 PMC。那段差距想了很久，到底差在什么动作上。

不差在技术，差在三件事。

第一，持续在那个项目上跑。每周都有 commit，即使是文档级的小 commit。频次比单次质量更重要——这跟传统软件工程里"持续集成胜过大爆炸式交付"是一个道理。

第二，参与 review 别人的 PR。即使还不是 Committer，review 别人是无声的能力证明，维护者会逐渐把你当自己人。

第三，在 mailing list 和 design doc 讨论里发声。即使有时只是问澄清性问题，这件事让你从写代码的工程师变成项目的 stakeholder。

把这三件事加起来，时间线大致是：从 Contributor 到 Committer 1-2 年，从 Committer 到 PMC 3-5 年。<span style="color: red; font-weight: bold;">差距不在某一个 PR 写得多牛，在你愿不愿意把开源当工作的一部分。</span>

我的看法是：开源的复利效应比任何技术红利都强。<span style="color: red; font-weight: bold;">每周一个小 commit，三年后就是一个量级的差距——这是用时间换壁垒，没有任何捷径。</span>

### 9.4 AI 时代的新红利与新门槛

AI 让"开始"这件事变容易了——这是红利。但还有一个更隐性的变化：AI 让中等技术含量的 PR 数量爆炸，维护者的注意力反而更稀缺了。

三年前一个写得清楚的 bug fix PR 提上去，维护者多半会顺手 review。今天同样质量的 PR 排在一堆 AI 辅助生成的 PR 后面，review 速度反而慢了。AI 拉高了 PR 的平均产量，但维护者的 review 带宽没有相应增加。

这意味着新人想脱颖而出，门槛反而高了。光会用 AI 提 PR 不够，你得展示 AI 替代不了的东西：

- 对项目方向的理解
- 对 trade-off 的判断
- 跟维护者沟通的体感

<span style="color: red; font-weight: bold;">AI 让代码贬值，但对项目的判断力在升值</span>。

我的立场很明确：写代码这一段大量用 AI（前面那些工作流每天都在跑），但 review、讨论、长期跟踪 issue 这些动作自己做。把省下来的写代码时间投到这些"人才能做的事"上。这才是 AI 时代工程师贡献开源的正确姿势——<span style="color: red; font-weight: bold;">用 AI 加速产出，把时间投在 AI 替代不了的协作和判断上。</span>

## 10. 小结与思考

<img src="imgs/aicmigr-32-opensource-04-process-recap/7851f1288e83acf66b9defe18c177097_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

整套"挑战开源"的工作流到这里全部跑完。第一个 PR 的心理胜利，第二个 PR + 第一个 issue 的真本事，加上本篇的清单和经验分享——五阶段从准备到一键流程，该讲的都讲完了。

### 10.1 一句话总结

如果只能从这篇记一句话，记这一句：<span style="color: red; font-weight: bold;">开源不是技术比赛，是耐心比赛</span>。AI 时代，这条更对。

### 10.2 三个月后的回看

挑一个感兴趣的项目，按本篇的清单跑通第一轮，然后每周回来一次，每次只做一个小改动。<span style="color: red; font-weight: bold;">频次大于单次重量</span>。

3 个月后回头看，GitHub 个人界面会变成另一个样子——<span style="color: red; font-weight: bold;">不是因为做了什么了不起的事，只是因为没停下来。</span>

### 10.3 两道思考题

#### (1) 思考一

跑完整套流程大约花了多少时间？最卡你的是哪一步——筛项目、筛 issue、实现 PR、扫漏洞、还是写 issue？

#### (2) 思考二

如果让你给一个还没开始做开源的工程师推荐一件最值得做的事，会推荐什么？
