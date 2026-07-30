---
title: 传统项目迁AI 30：挑战开源 - 第一个PR
author: fangkun119
date: 2026-07-05 10:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-30-opensource-01-first-pr/cover.jpg
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
aicmigr-30-opensource-01-first-pr
传统项目迁AI 30：挑战开源 - 第一个PR
-->

**AI 编程实战 · 挑战开源第一篇：跑通你的第一个 PR**

<img src="imgs/aicmigr-30-opensource-01-first-pr/2355cce17798bc98e492fa65c0d08083_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

AI 都能写代码了，开源还值得做吗？值得，而且比以前更值得。本篇回答两件事：为什么值得，以及怎么跑通第一个 PR。

这套系列从头到尾在讲一件事：AI 时代怎么做老项目改造。前面讲过两种形式。一种是公司内的老项目——代码是工程师自己的、bug 是工程师自己的、方向是工程师自己定的，工程师要在 AI 帮助下读懂、护住、改造一个有几年历史的老代码库。另一种是基于开源做需求——代码不是工程师自己的，但用法是工程师自己的，工程师要在 AI 帮助下快速摸清它、拆需求、把它当骨架长出自己的业务。

这里进入第三种形式：挑战开源。代码不是工程师自己的，bug 也不是工程师自己的，工程师把它贡献回去。这同样是老项目改造，只是改造的对象是别人的老项目。

| 形式 | 代码归属 | bug 归属 | 改造方向归属 | 典型场景 |
|------|---------|---------|-------------|---------|
| 公司内的老项目 | 工程师自己的 | 工程师自己的 | 工程师自己定 | 给业务代码库加功能、修缺陷 |
| 基于开源做需求 | 不是自己的 | 不归自己管 | 工程师自己的用法 | 在开源骨架上长业务 |
| 贡献开源（第七部分） | 不是自己的 | 不是自己的 | 贡献回去 | 给社区项目提 PR |

三种形式背后是同一套方法论：读懂陌生代码、找到改造点、用 AI 高效产出、保住质量。本篇把这套方法论用在"别人的老项目"上，走完最后一公里。

## 1. 为什么 AI 时代还要做开源

<img src="imgs/aicmigr-30-opensource-01-first-pr/0c5b3c81c57bfae93ab8767da8a16763_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 1.1 开源 commit 反而比以前更值钱

你可能会问：AI 都能写代码了，开源经历还重要吗？

<span style="color: red; font-weight: bold;">答案是比以前更重要。</span>

用传统工程师熟悉的场景做类比：AI 把整条行业的"基准线"抬高了。以前一个工程师能不能写出 80 分的代码，是面试官判断他是否胜任的核心指标；现在任何工程师在 AI 辅助下都能稳定产出 80 分的代码，"熟练使用 Java / Python / Rust"在简历上越来越没分量——这条线之下的人都被 AI 拉平了，光说熟练度已经区分不出谁强谁弱。

<span style="color: red; font-weight: bold;">那什么才不可贬值？开源 commit。</span>

一个人 GitHub 上有 50 个被合并到 Tokio 或 Apache 顶级项目的 commit，面试官能直接点进去看代码、看 review 过程、看跟维护者的讨论。<span style="color: red; font-weight: bold;">这些东西不可伪造、不可代笔、AI 写不出来。这是 AI 时代留给工程师证明自己的少数几条路之一。</span>

开源 commit 的现实收益具体有三件，按重要性递减。

| 收益 | 说明 | 为什么重要 |
|------|------|-----------|
| 简历分量 | "贡献过 Apache Kafka"、"Tokio 第 N 名贡献者"在简历上可能只占一行，但权重远高于工作经历那几页 | 它是可点击验证的 |
| 个人影响力 | 工程师的代码进了一个有几千 star 的项目，每天不知多少工程师在用这段代码跑生产 | 这种影响力靠真实代码使用量支撑，不靠社交媒体粉丝数 |
| 跟核心维护者建立连接 | review 工程师的 PR 的，可能就是项目核心 committer 或大公司资深工程师 | 这种连接不靠饭局、不靠投简历，靠代码本身。这是开源最被低估的收益 |

### 1.2 "开第一个口子"为什么这么难

你可能已经被上面说服了，想立刻动手。<span style="color: red; font-weight: bold;">但开源最难的不是技术，是开第一个口子。</span>

在 AI 出现之前，这件事真的很难，难到让一大批想做开源的工程师在第一步就放弃。常见的放弃路径有三种，每一种都足以劝退。

| 误区 | 表现 | 后果 |
|------|------|------|
| 挑了一个太顶级的项目 | 想给 Linux Kernel、Kubernetes、Spring Framework 提 PR | PR review 周期长、维护者顾不上新人，等三个月没人看，信心被磨没 |
| 挑了一个太简单的项目 | 盯着 good first issue 标签挑改 typo 的活 | 流程跑通了，但 PR 在简历上没有说服力，面试官一眼看穿在刷 contribution |
| 找不到合适的项目 | 天天刷 GitHub Trending，心里没标准 | 看完一圈也没上手，"找不到项目"这件事本身就是劝退 |

### 1.3 AI 把开第一个口子的成本降了一个数量级

好在 AI 出现之后，这三种误区都有了解法。把"误区"和"AI 的解法"对照着看，差别就清楚了。

| 误区 | AI 的解法 |
|------|----------|
| 挑了太顶级的项目 | AI 几分钟跑出一份匹配背景的候选清单，避免凭感觉撞上顶级项目 |
| 挑了太简单的项目 | 候选清单本身按"有真实技术含量"过滤，过滤掉纯刷量项目 |
| 找不到项目 | 一段提示词就能拿到候选，从"心里没标准"变成"有清单可选" |

挑完项目之后还有一道坎：看不懂陌生代码、不会写 PR 描述、不会回复 review。这些以前要花大量时间打磨的协作活，现在 AI 都能给一份合格初稿，工程师审核调整就行。

<span style="color: red; font-weight: bold;">把这几件事加起来，AI 时代的开源本质上是把"开第一个口子"的成本降低了一个数量级。</span>以前要花几年才能开始的事，现在几小时就能起步。<span style="color: red; font-weight: bold;">门槛低了，但开源 commit 在简历上的分量没降。这中间的差就是工程师的红利。</span>

## 2. 三道题筛项目：把"开第一个口子"量化

<img src="imgs/aicmigr-30-opensource-01-first-pr/d58d13384ce1c4700178833faef5078f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 为什么需要三道题

AI 能帮工程师扫仓库、列候选，但前提是工程师得先告诉它"什么样的项目才合适"。"合适"听起来主观，凭感觉挑往往撞上顶级项目或刷量项目。

传统软件工程里做技术选型，不会拍脑袋定方案，而是走几道评审关卡——技术可行性、团队匹配度、长期维护成本——逐条筛下来，主观感受就被量化成了可比较的清单。挑开源项目同理：把"合适"拆成三道可量化的题，主观判断就变成了打分表。

三道题的核心思想一句话：

<span style="color: red; font-weight: bold;">跟着一个有未来的项目一起成长，工程师也能跟着成长。</span><span style="color: red; font-weight: bold;">等这个项目从"快速成长"变成"行业标准"，工程师的早期 commit 记录会变成最值钱的资产。</span>

三道题就是为了精准命中"有未来"的项目：有真实技术含量、快速成长但还没到顶级、关联自己的技术栈或职业方向。

### 2.2 第一道题：有真实技术含量

不是 demo 项目，是真的在解决工程问题的项目。这样提 PR 的过程中能学到东西，简历上写出来也有分量。

判断标准一句话能讲清：这个项目是干嘛的，工程师能说清楚它在解决哪个工程问题吗？说不清的基本就是 demo。

### 2.3 第二道题：快速成长但还没到顶级

| 维度 | 状态 |
|------|------|
| star 数 | 几百到几千（不是几十，也不是几万） |
| commit 活跃度 | 近一年持续有提交 |
| 维护者 | 有几个核心维护者 |
| 发展阶段 | 没到几万 star、没成为行业标准 |

<span style="color: red; font-weight: bold;">这个阶段的项目正在从"作者驱动"过渡到"社区驱动"，特别欢迎新人 PR，每一个外部贡献者对他们都是宝贵的。</span>

顶级项目（Linux Kernel、Kubernetes 这种）恰恰相反——贡献门槛高、review 周期长、维护者忙到顾不上新人。这也是为什么三道题里专门留了"还没到顶级"这一条。

### 2.4 第三道题：关联技术栈或职业方向

做后端就找消息队列、数据库、缓存这类项目；做 AI 就找模型框架、Agent 框架、推理引擎；做前端就找 UI 库、构建工具。

让开源经历跟工作互相增益，而不是再开一个赛道。

## 3. AI 时代的 PR 工作流：人工两步，AI 跑剩余流程

<img src="imgs/aicmigr-30-opensource-01-first-pr/8e5939f1edabb46c014cbfea38dc177f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

项目选定之后，下一步是挑第一个 PR，然后把整条流程跑通。这一章把流程讲清。

对有传统开发经验的工程师来说，可以把整条 PR 流程类比成熟悉的 MR/PR + Code Review + CI：fork ≈ 拿到代码副本，branch + commit + push ≈ 在自己分支上提交改动，PR ≈ 发起 MR，review ≈ Code Review，合并 ≈ MR 进主干。区别只在于——这条流程里绝大多数步骤由 AI 执行，工程师只做两次必须人工的操作。

### 3.1 第一个 PR 的唯一标准：走通流程比技术难度重要

<span style="color: red; font-weight: bold;">第一个 PR 的目的不是证明工程师技术多牛，是证明能把 fork → branch → commit → push → PR → review → merge 整条流程跑通。</span><span style="color: red; font-weight: bold;">这条流程一旦跑通，后面再大的 PR 走的都是同一条路。</span>

> 走通流程比技术难度重要。

### 3.2 全流程六阶段

第一个 PR 的完整流程分六个阶段，前两个阶段属于"想清楚做什么"，后四个阶段属于"把它做出来并跑通"。

| 阶段 | 核心动作 | 关键产出 | 时间预算 |
|------|----------|----------|---------|
| 筛项目 | 用三道题标准 + AI 跑出候选清单 | 选定一个载体项目 | 10-15 分钟 |
| 找入口 | 让 AI 扫仓库找最简单的 PR 入口 | 一个零门槛 PR 目标 | 10-15 分钟 |
| fork + clone | 人工在 GitHub fork、本地 clone | 工程师账号下的仓库副本 | — |
| AI 跑剩余流程 | 建分支、改文件、跑 lint、写 commit、push、写 PR 描述 | 准备好被合并的 PR | 30-60 分钟 |
| 等 review | 几小时到一天，耐心等 | 维护者反馈或直接合并 | 几小时到一天 |
| 合并 | PR 进入主线 | GitHub 个人页一条 contribution | — |

阶段之间有严格依赖：筛项目决定载体，载体决定找入口能找到什么；fork + clone 是人工两步，必须先于 AI 跑流程；AI 跑流程产出的 PR 提交后进入等 review，review 通过后才进入合并。

### 3.3 三原则：人工两步、AI 跑流程、护栏到位

AI 时代的 PR 工作流不是工程师手敲一堆 git 命令。三条原则决定整个工作流的姿势。

| 原则 | 内容 | 设计动机 |
|------|------|----------|
| 人工两步 | fork + clone 必须人工做 | 涉及 GitHub 账号操作和本地仓库，AI 不替工程师操作账号 |
| AI 跑剩余流程 | 建 branch、改文件、跑 lint、写 commit、push、写 PR 描述全部丢给 AI | 一段提示词全部搞定，工程师角色是审核与拍板 |
| 护栏到位 | 每步执行前告知 + 报错立刻停 + lint 不瞎跑 | 避免 AI 瞎修把仓库搞乱 |

<span style="color: red; font-weight: bold;">后两条看似矛盾——一边全交给 AI，一边又设护栏。其实不矛盾：全交给 AI 是为了省时间，设护栏是为了让省下的时间不白费。</span><span style="color: red; font-weight: bold;">一个瞎修的 AI 比工程师手敲更费时间。</span>

### 3.4 速查表：第一个 PR 全流程

<img src="imgs/aicmigr-30-opensource-01-first-pr/8630abc0e226cb7a69f999e4455715e7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

熟练之后上手新项目可以直接拿下面两张表逐项核对，不需要回到正文。前一张覆盖"想清楚做什么"的两个阶段，后一张覆盖"把它做出来并跑通"的四个阶段。

**筛项目 + 找入口**

| 维度 | 必做硬性条目 | 必停决策点 | 必 review 的产出 |
|------|--------------|-----------|-----------------|
| 第一道题：真实技术含量 | 确认不是 demo 项目、真在解决工程问题 | — | 项目定位一句话能讲清 |
| 第二道题：快速成长但未顶级 | star 几百到几千、有持续 commit、有几个核心维护者 | 项目是否已经"作者驱动 → 社区驱动"过渡 | star 数、近一年 commit 活跃度、维护者背景 |
| 第三道题：关联技术栈 | 跟工程师职业方向一致（后端 / AI / 前端） | 是否再开一个赛道 | 技术栈匹配度 |
| 扫仓库入口 | 让 AI 扫 README、CONTRIBUTING.md、docs/ | — | 是否有 first PR 入口（签名墙、贡献者列表、good first issue、typo） |
| 入口类型选择 | 优先项目方设计的零门槛入口，次选 typo 修订或文档补充 | 入口是否走完整 PR 流程 | 入口路径 + 怎么改 + 怎么提 PR |

**跑通 PR**

| 维度 | 必做硬性条目 | 必停决策点 | 必 review 的产出 |
|------|--------------|-----------|-----------------|
| 人工两步 | GitHub Fork + 本地 clone | — | 仓库在自己账号下、能本地打开 |
| AI 跑流程 | 提示词包含 7 步（建 branch、改文件、跑 lint、commit、push、写 PR 描述、提 PR） | AI 报错时停下来问工程师 | 每个 commit 都符合 Conventional Commits |
| 护栏 | 每步执行前告知 + 报错立刻停 | lint 类型是否匹配改动 | lint 跑通且不瞎跑 cargo build |
| 提交方式 | gh CLI 优先，没装则 AI 准备材料、人工去 GitHub 点提交 | 是否装了 gh CLI | PR 链接产出 |
| 等 review | 几小时到一天 | 不在等待期间反复刷新页面 | PR 状态变更通知 |
| 合并 | PR 进入主线 | — | GitHub 个人页多一条 contribution |

## 4. 实战：从挑项目到跑通第一个 PR

<img src="imgs/aicmigr-30-opensource-01-first-pr/19f17f38e4a54a360aa6e0ee037dec7d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-30-opensource-01-first-pr/ed3d24730d685a25be1e3c5474159951_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一章把第 2 章的三道题和第 3 章的 PR 工作流串起来，从零候选一直跑到 PR 提交，在一个载体项目上演示完整路径。合计耗时 50-90 分钟（不含等 review）。

### 4.1 实战前的准备：把三道题写成提示词

挑项目之前先确认两件事：

- GitHub 账号已注册、能正常 fork 仓库
- 本地装好 git，能 clone 仓库
- 装好 Claude Code 或同类 AI 编程工具
- 可选：装好 `gh` CLI（用于 AI 一气呵成提 PR）

工具齐了，把三道题标准加上自己的技术栈背景，丢给 Claude Code。AI 不知道工程师的方向，但工程师告诉它之后，它能在 GitHub 上跑出来一份匹配的清单。

```text
我想开始给开源项目贡献代码,作为我的第一个开源 PR 的目标项目。

我的筛选标准是三道题:
1. 有真实技术含量,不是 demo 项目
2. 正在快速成长但还没到顶级(几百到几千 star、有持续 commit、
   有几个核心维护者,但还没到几万 star、没成为行业标准)
3. 跟我的技术栈或职业方向有关联

我的背景:做基础软件方向,熟悉 Java、Go、Python,Rust 在学。
对消息队列、数据库、Agent 框架这些方向感兴趣。

按这三道题给我推荐 5 个候选项目。每个项目说清楚:
- 项目定位
- star 数和最近一年的 commit 活跃度
- 维护者背景(独立团队还是大公司主导)
- 为什么适合我作为第一个 PR 目标
- 它的 good first issue 入口在哪
```

### 4.2 review 重点：如何避免候选清单糊掉

这段提示词看似简单，但每一条都是护栏，漏一条候选清单就糊：

- 三道题是否显式喂给 AI（不喂 AI 不知道标准）
- 工程师自己的技术栈背景是否写清楚（背景模糊则候选清单也模糊）
- 5 个候选是否每个都覆盖五项要素（定位 / star 和 commit / 维护者 / 为什么适合 / good first issue 入口）

### 4.3 选定 RobustMQ：三道题打分实例

Claude Code 跑完推荐了 5 个候选。看完那 5 个候选，挑了排在最稳那一档的项目，RobustMQ。三道题打分如下：

| 三道题 | RobustMQ 表现 |
|-------|---------------|
| 真实技术含量 | 基础软件赛道（消息队列） |
| 快速成长但未顶级 | 活跃但还没到顶级 |
| 关联技术栈 | Rust 写的，跟工程师学习 Rust 的方向对上 |

三道题都打中，所以选定。本篇后面所有动作都在 RobustMQ 这个项目上跑，但这套流程跟具体项目无关，载体可以换成 Tokio、Tauri、Async-NATS 或者工程师自己 AI 推荐出来的任何项目，提示词改个项目名就能复刻。

### 4.4 用 AI 扫仓库找 first PR 入口

项目定了之后，fork + clone 这两步必须人工做——它们涉及 GitHub 账号操作和本地仓库路径，AI 不替工程师操作账号，这也是三原则里"人工两步"那条的来源。在项目 GitHub 页面右上角点 Fork，把仓库复制到自己账号下，然后 clone 到本地：

```bash
git clone git@github.com:<your-username>/robustmq.git
cd robustmq
```

clone 完之后，让 Claude Code 在仓库里找最简单的 PR 入口。这一步的目的是从"载体项目"走到"具体改哪一行"。

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

Claude Code 跑完会告诉工程师：这个项目在 docs 里有一个 SignYourName 入口，专为新人贡献者设计，把自己的名字加到 SIGNYOURNAME.md 里就行。表面上看就是改一行 Markdown，但它走的是完整的 fork → branch → commit → push → PR → review → merge 流程。<span style="color: red; font-weight: bold;">项目方知道开源新人最怕的是开第一个口子，所以专门留了一个零技术门槛的入口给新人练手。</span>

一些活跃的开源项目都有类似的设计。<span style="color: red; font-weight: bold;">工程师筛中的项目里有这种入口就用，没有就找一个 typo 修订或文档补充作为替代，替代品同样要求走完整 PR 流程，不能跳步。</span>

入口找到了，还要回头 review 这一段的输出：

- AI 是否真扫了 README / CONTRIBUTING.md / docs/ 三类文件（不是凭空猜）
- 输出的入口是否包含三项（具体路径 / 怎么改 / 怎么提 PR）
- 入口是否走完整 PR 流程（fork → branch → commit → push → PR → review → merge），而非某种"半流程"快捷方式

### 4.5 一段提示词跑完七步 PR 流程

入口找到了，剩下的全部丢给 Claude Code。在仓库目录下打开 Claude Code，丢下面这一段提示词。

```text
我刚 fork 并 clone 了一个开源项目的仓库,要给 SIGNYOURNAME.md 加一行
我的信息,作为我的第一个开源 PR。

请你帮我把整套流程跑完。每一步执行前告诉我你要做什么,执行后告诉
我结果。任何报错立刻停下来问我,不要自己瞎修。

具体步骤:
1. 建一个新 branch,命名规范跟项目已有 branch 风格对齐
   (比如 feature/sign-your-name-<我的名字>)
2. 找到 SIGNYOURNAME.md 文件,先看一眼现有列表的格式,
   然后在合适的位置严格按同样格式加一行:
   "Robert (LoboXu) - <my-email> - 2026-05-03"
3. 跑项目自带的代码格式检查或 lint。看 CONTRIBUTING.md /
   pre-commit hook / Makefile / cargo 配置,有什么用什么。
   别瞎跑 cargo build,这是个 markdown 改动,只跑相关检查就行。
   有报错就修,跑通为止。
4. 写一条 commit message,Conventional Commits 规范,subject
   不超过 50 字符,简洁有效。然后 git add 相关文件并 commit。
5. push 到我的 fork(remote 名是 origin)。
6. 给我写一个 PR 描述,3-5 行,说明这是我的第一个 PR、跟着
   SignYourName 入口跑通流程、表达对项目的好感、语气是普通
   工程师不是 LinkedIn 帖子。
7. 如果系统装了 gh CLI,直接 gh pr create 把 PR 提了。
   没装就把 PR 描述输出给我,我自己去 GitHub 点提交。
```

<span style="color: red; font-weight: bold;">这段提示词看着像流水账，但每一步都是给 AI 装的护栏。护栏没装好，AI 一瞎跑仓库就废了。</span>

| 护栏 | 内容 | 防止什么 |
|------|------|---------|
| 每步执行前告知 + 报错立刻停 | AI 每一步动作前先告知工程师要做什么，做完告知结果，任何报错立刻停下来问，不自己瞎修 | AI 把仓库搞乱后工程师还得花时间收拾 |
| lint 不要瞎跑 cargo build | 显式告诉 AI 这是 Markdown 改动，看 CONTRIBUTING.md / pre-commit hook / Makefile / cargo 配置，有什么用什么，只跑相关检查 | <span style="color: red; font-weight: bold;">一个 Markdown 改动跑 `cargo build` 等五分钟，AI 还以为自己很勤奋。</span> |
| gh CLI 优先、fallback 人工 | 装了 `gh` 就一气呵成，没装就让 AI 准备好材料、人工去 GitHub 点提交，AI 自己判断系统是否装了 `gh` CLI | 没装 `gh` 时 AI 卡在提 PR 一步或瞎自创命令 |

Claude Code 跑完这一段，大概会发生这些事：

| 步骤 | AI 的动作 | 工程师的角色 |
|------|----------|-------------|
| 1 | `git status` 确认仓库干净，`git checkout -b` 建新 branch | 看 AI 告知 |
| 2 | 打开 SIGNYOURNAME.md 看格式，按现有列表风格加一行 | 看 AI 加的内容 |
| 3 | 看 CONTRIBUTING.md 知道项目用 markdownlint，跑一遍发现没问题 | 看 lint 结果 |
| 4 | `git add` + `commit`，message 是 `docs: sign my name in SIGNYOURNAME.md`（符合 Conventional Commits） | 看 commit message |
| 5 | `git push` 到工程师的 fork | 看 push 结果 |
| 6 | 看到装了 `gh` CLI，直接 `gh pr create` 提 PR | 拿到 PR 链接 |

整个过程工程师做的事：只有 fork + clone + 看 AI 跑完的结果 + 在它问的时候回答几次。手没敲过一行 git 命令，也没打开过 SIGNYOURNAME.md。

跑完别急着合上窗口，按这六条自查一遍：

- AI 是否在每一步前都告知（而非直接动手）
- AI 报错时是否停下来问，而非瞎修
- lint 是否匹配改动类型（Markdown 改动不跑 cargo build）
- commit message 是否符合 Conventional Commits（type + 简短 subject 不超过 50 字符）
- PR 描述是否 3-5 行、语气是普通工程师而非 LinkedIn 帖子
- gh CLI 装了时是否一气呵成，没装时是否输出 PR 描述给工程师手动提交

# 5. 合并那一刻：你拿到的是什么

<img src="imgs/aicmigr-30-opensource-01-first-pr/c93ab0c69bf16181e6346f83a4b23aec_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 5.1 提交之后等什么

提交 PR 后，通常几小时到一天内会有 review。SignYourName 这种 PR 一般很快就会被合并。等待期间不要反复刷新页面——耐心是开源的基本功，反复刷新只会消耗工程师自己的注意力。

### 5.2 合并的意义：心理资产而非技术成就

<span style="color: red; font-weight: bold;">PR 合并那一刻，工程师拿到的不是技术成就，是心理资产。</span>

第一个 PR 是个里程碑。<span style="color: red; font-weight: bold;">它证明了一件事：工程师能在不属于他的代码上做交付。</span><span style="color: red; font-weight: bold;">这件事听起来简单，但在开源里它是分水岭。这个动作做过一次，后面所有 PR 都是基于这个心理基础。</span>

GitHub 个人页面上那条 contribution 不会消失，它会一直在那里。下次有面试官点进工程师的 GitHub，会看到工程师给一个真实开源项目提过 PR。<span style="color: red; font-weight: bold;">这条记录从此就是工程师简历的一部分，甚至不需要写在简历正文里。</span>

<span style="color: red; font-weight: bold;">更重要的是，工程师证明了开源没那么难，跨过了开源新人的最大心理门槛。</span>

### 5.3 本篇核心三句话

| 顺序 | 核心结论 |
|------|---------|
| 第一句 | 开源 commit 在 AI 时代反而更值钱——它不可伪造、不可代笔、AI 写不出来，是工程师证明自己的少数几条公开可验证的路之一 |
| 第二句 | 开源最难的不是技术，是开第一个口子——心理门槛比技术门槛高得多，AI 把这个成本降低了一个数量级 |
| 第三句 | 第一个 PR 走通流程优先——SignYourName 这种入口看着简单，但它跑的是完整 PR 流程，跑通了后面再大的 PR 都是同一条路；合并那一刻拿到的是心理资产，比 PR 本身值钱得多 |
