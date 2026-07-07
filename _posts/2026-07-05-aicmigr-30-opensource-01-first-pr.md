---
title: 传统项目迁AI 30：挑战开源 - 第一个PR
author: fangkun119
date: 2026-07-05 10:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
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

本篇是"挑战开源"系列的开篇。从这一篇开始，系列进入第七部分。整套系列从第一篇开始就在讲同一件事：AI 时代怎么做老项目改造。前面几个部分讲了这件事的两种形式。

一种是公司内的老项目。代码是工程师自己的、bug 是工程师自己的、方向是工程师自己定的。工程师需要在 AI 帮助下读懂、护住、改造一个有几年历史的老代码库。

另一种是基于开源做需求。代码不是工程师自己的，但用法是工程师自己的。开源项目本质上也是一个老项目，工程师需要在 AI 帮助下快速摸清它、拆需求、把它当骨架长出自己的业务。

第七部分进入第三种形式：挑战开源。代码不是工程师自己的，bug 也不是工程师自己的，工程师把它贡献回去。这同样是老项目改造，只是改造的对象是别人的老项目。

公司内的老项目、基于开源借用别人的成果、贡献开源让工程师的名字进入社区项目，三种形式背后是同一套方法论：读懂陌生代码、找到改造点、用 AI 高效产出、保住质量。整套系列的方法论在这里走完最后一公里。

第七部分三篇。本篇先把"为什么"和"怎么开始"讲清楚，再跑通工程师的第一个 PR。系列第 31 篇带工程师做一个真有难度的 PR，让 AI 当 Rust 助教。系列第 32 篇把整套动作模板化，给工程师一份可以反复用的实操脚本。

**全文导读地图**

<img src="imgs/aicmigr-30-opensource-01-first-pr/2af663b8eb043e15918fc6238d815f63_MD5.jpg" alt="">

<!--
flowchart TD
    A["第七部分定位<br>三种形式背后的统一方法论"] --\> B["为什么 AI 时代开源仍值钱<br>公开可验证的能力证明"]
    B --\> C["开第一个口子最难<br>三种误区 + AI 三种解法"]
    C --\> D["三道题筛项目<br>方法论提炼"]
    D --\> E["场景一<br>让 AI 推荐项目（RobustMQ）"]
    E --\> F["场景二<br>让 AI 找最简单的 PR 入口"]
    F --\> G["场景三<br>跑通 SignYourName PR（完整流程）"]
    G --\> H["合并那一刻<br>心理资产而非技术成就"]
-->

**两类读者怎么读这篇文档：**

- 初学 AI 编程工程师：通读第一部分第 1、2 章建立方法论框架（三道题、第一个 PR 选型标准、AI 时代 PR 工作流三原则），再按第二部分场景一到场景三的顺序复现一遍。每个提示词原文都要复制到 Claude Code 里实跑一次，对照 review 重点自查
- 熟练 AI 编程工程师：直接看第 3 章 Check List 速查；遇到想回顾"为什么这么做"时回到第 2 章对应的方法论节，或第二部分对应场景

## 1. 工作流总览

### 1.1 三种形式背后的统一方法论

<img src="imgs/aicmigr-30-opensource-01-first-pr/dfff315265c929a7c4c8f599d49ec6d6_MD5.jpg" alt="">

整套系列讲的是同一件事：AI 时代怎么做老项目改造。这件事有三种形式，背后是同一套方法论。

| 形式 | 代码归属 | bug 归属 | 改造方向归属 | 典型场景 |
|------|---------|---------|-------------|---------|
| 公司内的老项目 | 工程师自己的 | 工程师自己的 | 工程师自己定 | 给业务代码库加功能、修缺陷 |
| 基于开源做需求 | 不是自己的 | 不归自己管 | 工程师自己的用法 | 在开源骨架上长业务 |
| 贡献开源（第七部分） | 不是自己的 | 不是自己的 | 贡献回去 | 给社区项目提 PR |

三种形式背后统一的方法论是：读懂陌生代码、找到改造点、用 AI 高效产出、保住质量。第七部分把这套方法论用在"别人的老项目"上，走完最后一公里。

### 1.2 第一个 PR 全流程六阶段概览

<img src="imgs/aicmigr-30-opensource-01-first-pr/649fd231b5a02fac8eaaaefa4f1c1f58_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇聚焦"第一个 PR"的完整流程。流程分六个阶段，每个阶段有明确的动作和产出。

| 阶段 | 核心动作 | 关键产出 |
|------|----------|----------|
| 筛项目 | 用三道题标准 + AI 跑出候选清单 | 选定一个载体项目 |
| 找入口 | 让 AI 扫仓库找最简单的 PR 入口 | 一个零门槛 PR 目标（如 SignYourName） |
| fork + clone | 人工在 GitHub fork、本地 clone | 工程师自己账号下的仓库副本 |
| AI 跑剩余流程 | 建分支、改文件、跑 lint、写 commit、push、写 PR 描述 | 准备好被合并的 PR |
| 等 review | 几小时到一天，耐心等 | 维护者反馈或直接合并 |
| 合并 | PR 进入主线 | GitHub 个人页一条 contribution |

### 1.3 阶段之间的依赖关系

筛项目决定载体，载体决定找入口能找到什么。fork + clone 是人工两步，必须先于 AI 跑流程。AI 跑流程产出的 PR 提交后进入等 review，review 通过后才进入合并。前两个阶段属于"想清楚做什么"，后四个阶段属于"把它做出来并跑通"。

## 2. 关键方法论

### 2.1 为什么 AI 时代开源 commit 仍是少数公开可验证的能力证明

<img src="imgs/aicmigr-30-opensource-01-first-pr/0b46bcbf5614bbe715984805e07a43f7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

很多人会问：AI 都能写代码了，开源经历还重要吗？答案是比以前更重要。

#### (1) AI 让"会写代码"这件事大幅贬值

任何工程师在 AI 辅助下都能产出 80 分的代码。这意味着光说"我熟练使用 Java / Python / Rust"在简历上越来越没分量，因为 AI 让这条线的下限抬高了，工程师的熟练和别人的熟练，在面试官眼里没区别。

#### (2) 开源 commit 是公开可验证的工程能力证明

一个人 GitHub 上有 50 个被合并到 Tokio 或 Apache 顶级项目的 commit，面试官能直接点进去看代码、看 review 过程、看跟维护者的讨论。这些东西不可伪造、不可代笔、AI 写不出来。这是 AI 时代留给工程师证明自己的少数几条路之一。

#### (3) 三种现实收益

开源 commit 的现实收益具体有三件。

##### ① 简历分量

"贡献过 Apache Kafka"、"Tokio 第 N 名贡献者"这些 title 在简历上占的篇幅可能就一行，但它的权重远高于工作经历那几页。因为它是可点击验证的。

##### ② 个人影响力

工程师的代码进了一个有几千 star 的项目，意味着每天不知道有多少工程师都在用这段代码跑生产。这种影响力不靠社交媒体粉丝数撑，靠真实的代码使用量支撑。

##### ③ 跟核心维护者建立连接

开源圈是个网络，工程师提一个有质量的 PR，review 它的可能就是这个项目的核心 committer 或者大公司的资深工程师。这种连接不靠饭局、不靠投简历，靠代码本身。这是开源最被低估的收益。

### 2.2 开第一个口子最难：三种误区加 AI 三种解法

<img src="imgs/aicmigr-30-opensource-01-first-pr/4ba8967e185387d0b551c1f52710ea16_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

讲到这里，工程师可能已经想试试。但开源最难的不是技术，是开第一个口子。在 AI 出现之前，这件事真的很难，难到让一大批想做开源的工程师在第一步就放弃。

#### (1) 三种常见的开第一个口子误区

开源新人最常踩的三种坑。

##### ① 挑了一个太顶级的项目

Linux Kernel、Kubernetes、Spring Framework 这种项目，贡献门槛高、PR review 周期长、维护者忙到顾不上新人。一个新人想给 Kubernetes 提 PR，issue 选半天选不到合适的，提了 PR 等三个月都没人 review，信心直接被磨没。

##### ② 挑了一个太简单的项目

看着 good first issue 标签，挑了个改 typo 的活，提一个改文档错别字的 PR。流程跑通了，但这种 PR 在简历上没有任何说服力，面试官扫一眼就知道只是想刷个 contribution。

##### ③ 找不到合适的项目

天天去 GitHub Trending 看推荐，因为心里没标准，看完一圈也没相中上手哪个。这个状态可以持续好几年，"找不到项目"这件事本身就是劝退。

#### (2) AI 时代的三种解法

AI 出现之后，这三种误区都有解法了。

##### ① AI 帮工程师筛项目

工程师心里没标准没关系，只要把"什么样的项目适合我"的判断维度告诉 AI，几分钟就能跑出一份匹配背景的候选清单。这件事以前要靠几年的开源圈子影响，现在一段提示词就能拿到。

##### ② AI 帮工程师读懂陌生代码

挑了有技术含量的项目不再是死局，因为以前"看不懂代码就放弃"的工程师，现在可以让 AI 当导师，在不熟的技术栈、不熟的代码库上也能找到改造点。

##### ③ AI 帮工程师跨过协作门槛

写 PR 描述、写 commit message、回复 review 意见，这些以前要花大量时间打磨的东西，现在 AI 能给一份合格初稿，工程师审核调整就行。

把这三件事加起来，AI 时代的开源本质上是把"开第一个口子"这件事的成本降低了一个数量级。以前要花几年才能开始的事，现在几小时就能起步。门槛低了，但开源 commit 在简历上的分量没降。这中间的差就是工程师的红利。

### 2.3 三道题筛项目标准

<img src="imgs/aicmigr-30-opensource-01-first-pr/3594e08b98a15d21608c5ff6f28ad5f8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

AI 帮工程师筛项目，但工程师得先告诉 AI 什么样的项目才合适。下面这三道题就是这个标准。

#### (1) 第一道题：有真实技术含量

不是 demo 项目，是真的在解决工程问题的项目。这样提 PR 的过程中能学到东西，简历上写出来也有分量。

#### (2) 第二道题：正在快速成长但还没到顶级

已经验证了价值方向（有几百到几千 star、有持续的 commit 活动、有几个核心维护者），但还在早期阶段（没到几万 star、没成为行业标准）。这个阶段的项目特别欢迎新人 PR，因为他们正在从"作者驱动"过渡到"社区驱动"，每一个外部贡献者对他们都是宝贵的。

#### (3) 第三道题：跟工程师的技术栈或职业方向有关联

工程师做后端就找消息队列、数据库、缓存这类项目；做 AI 就找模型框架、Agent 框架、推理引擎；做前端就找 UI 库、构建工具。让开源经历跟工作互相增益，而不是再开一个赛道。

#### (4) 三道题的核心思想

跟着一个有未来的项目一起成长，工程师也能跟着成长。等这个项目从"快速成长"变成"行业标准"，工程师的早期 commit 记录会变成最值钱的资产。

### 2.4 第一个 PR 选型唯一标准：走通流程比技术难度重要

项目定了，接下来是选第一个 PR。这一步的标准只有一条。

> 走通流程比技术难度重要。

第一个 PR 的目的不是证明工程师技术多牛，是证明能把 fork → branch → commit → push → PR → review → merge 整条流程跑通。这条流程跑通了，后面再大的 PR 都是同一条路。

### 2.5 AI 时代 PR 工作流三原则

<img src="imgs/aicmigr-30-opensource-01-first-pr/17a9e3c8465cb102d393429ec14e86b1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

AI 时代的 PR 工作流，不是工程师手敲一堆 git 命令。三条原则决定整个工作流的姿势。

| 原则 | 内容 | 设计动机 |
|------|------|----------|
| 人工两步 | fork + clone 必须人工做 | 涉及 GitHub 账号操作和本地仓库，AI 不替工程师操作账号 |
| AI 跑剩余流程 | 建 branch、改文件、跑 lint、写 commit、push、写 PR 描述全部丢给 AI | 一段提示词全部搞定，工程师角色是审核与拍板 |
| 护栏到位 | 每步执行前告知 + 报错立刻停 + lint 不瞎跑 | 避免 AI 瞎修把仓库搞乱 |

## 3. 项目阶段 Check List

下表把第一个 PR 的流程拆成可裁剪的速查表。熟练工程师上手新项目时可以直接拿这张表逐项核对，不需要回到正文。

### 3.1 三道题筛项目 Check List

<img src="imgs/aicmigr-30-opensource-01-first-pr/75099d008119d9ba589076da216c2c12_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 维度 | 必做硬性条目 | 必停决策点 | 必 review 的产出 |
|------|--------------|-----------|-----------------|
| 第一道题：真实技术含量 | 确认不是 demo 项目、真在解决工程问题 | — | 项目定位一句话能讲清 |
| 第二道题：快速成长但未顶级 | star 几百到几千、有持续 commit、有几个核心维护者 | 项目是否已经"作者驱动 → 社区驱动"过渡 | star 数、近一年 commit 活跃度、维护者背景 |
| 第三道题：关联技术栈 | 跟工程师职业方向一致（后端 / AI / 前端） | 是否再开一个赛道 | 技术栈匹配度 |

### 3.2 找入口 Check List

| 维度 | 必做硬性条目 | 必停决策点 | 必 review 的产出 |
|------|--------------|-----------|-----------------|
| 扫仓库入口 | 让 AI 扫 README、CONTRIBUTING.md、docs/ | — | 是否有 first PR 入口（签名墙、贡献者列表、good first issue typo） |
| 入口类型选择 | 优先项目方设计的零门槛入口，次选 typo 修订或文档补充 | 入口是否走完整 PR 流程 | 入口路径 + 怎么改 + 怎么提 PR |

### 3.3 跑通 PR Check List

<img src="imgs/aicmigr-30-opensource-01-first-pr/ebe9e43fdbbe6ad7e7e19061dfc74208_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 维度 | 必做硬性条目 | 必停决策点 | 必 review 的产出 |
|------|--------------|-----------|-----------------|
| 人工两步 | GitHub Fork + 本地 clone | — | 仓库在自己账号下、能本地打开 |
| AI 跑流程 | 提示词包含 7 步（建 branch、改文件、跑 lint、commit、push、写 PR 描述、提 PR） | AI 报错时停下来问工程师 | 每个 commit 都符合 Conventional Commits |
| 护栏 | 每步执行前告知 + 报错立刻停 | lint 类型是否匹配改动 | lint 跑通且不瞎跑 cargo build |
| 提交方式 | gh CLI 优先，没装则 AI 准备材料、人工去 GitHub 点提交 | 是否装了 gh CLI | PR 链接产出 |
| 等 review | 几小时到一天 | 不在等待期间反复刷新页面 | PR 状态变更通知 |
| 合并 | PR 进入主线 | — | GitHub 个人页多一条 contribution |

### 3.4 阶段时间预算

| 阶段 | 时间预算 |
|------|---------|
| 筛项目（场景一） | 10-15 分钟 |
| 找入口（场景二） | 10-15 分钟 |
| 跑通 PR（场景三） | 30-60 分钟（不含等 review 的几小时到一天） |
| 全流程一键跑通 | 1-2 小时（不含等 review） |

## 4. 实战演示总览

<img src="imgs/aicmigr-30-opensource-01-first-pr/f2673cbb9328f07887718a11d9bfda69_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二部分把第一部分的方法论落到具体场景里。每个场景都对应源文档里的一段实操，产出明确、review 点清晰。

### 4.1 三个场景的全局对照

| 场景 | 对应方法论节 | 核心产出 | 主要提示词 |
|------|-------------|---------|-----------|
| 场景一 让 AI 推荐项目 | 第 2.3 节三道题筛项目 | 选定 RobustMQ 作为载体 | 提示词 1（三道题 + 背景喂入） |
| 场景二 让 AI 找最简单的 PR 入口 | 第 2.4 节走通流程优先 | SignYourName 入口定位 | 提示词 2（扫仓库找 first PR 入口） |
| 场景三 跑通 SignYourName PR | 第 2.5 节 AI 时代 PR 工作流三原则 | 第一个被合并的 PR | 提示词 3（七步完整 PR 流程） |

### 4.2 实战前的准备

实战前需要两件事。

#### (1) 工程师手上的工具

- GitHub 账号已注册、能正常 fork 仓库
- 本地装好 git，能 clone 仓库
- 装好 Claude Code 或同类 AI 编程工具
- 可选：装好 `gh` CLI（用于 AI 一气呵成提 PR）

#### (2) 载体项目可替换说明

本篇后面所有动作都在 RobustMQ 这个项目上跑。但请注意，这套流程跟具体项目无关，载体可以换成 Tokio、Tauri、Async-NATS 或者工程师自己 AI 推荐出来的任何项目，提示词改个项目名就能复刻。

## 5. 场景一 让 AI 推荐项目

<img src="imgs/aicmigr-30-opensource-01-first-pr/356c88b9812906885d1ff0317c32ade7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应第 2.3 节三道题筛项目标准。产出选定一个载体项目，整套流程接下来都在这个项目上跑。

### 5.1 第一步：把三道题写成提示词

把三道题标准加上自己的技术栈背景，丢给 Claude Code。AI 不知道工程师的方向，但工程师告诉它之后，它能在 GitHub 上跑出来一份匹配的清单。

#### (1) 提示词原文

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

### 5.2 review 重点

#### (1) review 重点

- 三道题是否显式喂给 AI（不喂 AI 不知道标准）
- 工程师自己的技术栈背景是否写清楚（背景模糊则候选清单也模糊）
- 5 个候选是否每个都覆盖五项要素（定位 / star 和 commit / 维护者 / 为什么适合 / good first issue 入口）

### 5.3 选定 RobustMQ 的判断

Claude Code 跑完推荐了 5 个候选。看完那 5 个候选，挑了排在最稳那一档的项目，RobustMQ。

#### (1) 三道题打分

| 三道题 | RobustMQ 表现 |
|-------|---------------|
| 真实技术含量 | 基础软件赛道（消息队列） |
| 快速成长但未顶级 | 活跃但还没到顶级 |
| 关联技术栈 | Rust 写的，跟工程师学习 Rust 的方向对得上 |

三道题都打中，所以选定。

### 5.4 场景一收尾

跑完场景一，工程师手上选定了一个载体项目。耗时 10-15 分钟。

## 6. 场景二 让 AI 找最简单的 PR 入口

<img src="imgs/aicmigr-30-opensource-01-first-pr/2c90a2d3a3cd4619f7c4c45e04483cb7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应第 2.4 节第一个 PR 选型标准：走通流程比技术难度重要。产出定位到一个零门槛 PR 入口。

### 6.1 第一步：让 AI 扫仓库找 first PR 入口

让 Claude Code 在仓库里找最简单的 PR 入口。

提示词原文

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

### 6.2 review 重点

- AI 是否真扫了 README / CONTRIBUTING.md / docs/ 三类文件（不是凭空猜）
- 输出的入口是否包含三项（具体路径 / 怎么改 / 怎么提 PR）
- 入口是否走完整 PR 流程（fork → branch → commit → push → PR → review → merge），而非某种"半流程"快捷方式

### 6.3 SignYourName 入口的解读

#### (1) 解读

Claude Code 跑完告诉工程师：这个项目在 docs 里有一个 SignYourName 入口，专门为新人贡献者设计，把自己的名字加到 SIGNYOURNAME.md 里就行。

#### (2) 入口背后的设计

表面上看就是改一行 Markdown，但它走的是完整的 fork → branch → commit → push → PR → review → merge 流程。项目方知道开源新人最怕的是开第一个口子，所以专门留了一个零技术门槛的入口给新人练手。

#### (3) 没有签名墙入口时的替代

一些活跃的开源项目都有类似的设计。工程师筛中的项目里有这种入口就用，没有就找一个 typo 修订或文档补充作为替代。替代品同样要求走完整 PR 流程，不能跳步。

### 6.4 场景二收尾

跑完场景二，工程师手上定位到一个零门槛 PR 入口。耗时 10-15 分钟。

## 7. 场景三 跑通 SignYourName PR

对应第 2.5 节 AI 时代 PR 工作流三原则。这一遍的姿势很关键：fork 和 clone 这两步必须人工做（GitHub 账号操作 + 本地 clone），剩下所有动作都交给 Claude Code 跑。建 branch、改文件、跑 lint、写 commit、push、写 PR 描述，一段提示词丢进去全部搞定。

### 7.1 人工的两步

<img src="imgs/aicmigr-30-opensource-01-first-pr/13c7830787de7ee4e497c256a09f5297_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 第一步：fork 仓库

在项目的 GitHub 页面右上角点 Fork，把仓库复制到自己账号下。

#### (2) 第二步：clone 到本地

```bash
git clone git@github.com:<your-username>/robustmq.git
cd robustmq
```

### 7.2 AI 跑剩余流程的提示词

剩下的全部丢给 Claude Code。在仓库目录下打开 Claude Code，丢下面这一段提示词。

提示词原文

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

### 7.3 提示词关键设计

这段提示词的关键设计有三点，每一点都是给 AI 装的护栏。

#### (1) 每一步执行前告知 + 报错立刻停

这是给 AI 装的护栏，避免它瞎修把仓库搞乱。AI 在每一步动作前先告知工程师要做什么，做完告知结果，任何报错立刻停下来问工程师，不自己瞎修。

#### (2) lint 不要瞎跑 cargo build

显式告诉 AI 这是个 Markdown 改动，别在大型 Rust 项目上等几分钟编译。AI 应该看 CONTRIBUTING.md / pre-commit hook / Makefile / cargo 配置，有什么用什么，只跑相关检查就行。

#### (3) gh CLI 优先、fallback 人工

真实工程师有 `gh` 就一气呵成，没 `gh` 就用 AI 准备好材料、人工去 GitHub 点提交。提示词显式覆盖了这两种情况，AI 自己判断系统是否装了 `gh` CLI。

### 7.4 跑完会发生什么

<img src="imgs/aicmigr-30-opensource-01-first-pr/edec992310997d9c8b3482c7872682ae_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Claude Code 跑完这一段，大概会发生这些事。

**AI 的预期动作序列**

| 步骤 | AI 的动作 | 工程师的角色 |
|------|----------|-------------|
| 1 | `git status` 确认仓库干净，`git checkout -b` 建新 branch | 看 AI 告知 |
| 2 | 打开 SIGNYOURNAME.md 看格式，按现有列表风格加一行 | 看 AI 加的内容 |
| 3 | 看 CONTRIBUTING.md 知道项目用 markdownlint，跑一遍发现没问题 | 看 lint 结果 |
| 4 | `git add` + `commit`，message 是 `docs: sign my name in SIGNYOURNAME.md`（符合 Conventional Commits） | 看 commit message |
| 5 | `git push` 到工程师的 fork | 看 push 结果 |
| 6 | 看到装了 `gh` CLI，直接 `gh pr create` 提 PR | 拿到 PR 链接 |

整个过程工程师做的事：只有 fork + clone + 看 AI 跑完的结果 + 在它问的时候回答几次。手没敲过一行 git 命令，也没打开过 SIGNYOURNAME.md。

### 7.5 review 重点

- AI 是否在每一步前都告知（而非直接动手）
- AI 报错时是否停下来问，而非瞎修
- lint 是否匹配改动类型（Markdown 改动不跑 cargo build）
- commit message 是否符合 Conventional Commits（type + 简短 subject 不超过 50 字符）
- PR 描述是否 3-5 行、语气是普通工程师而非 LinkedIn 帖子
- gh CLI 装了时是否一气呵成，没装时是否输出 PR 描述给工程师手动提交

### 7.6 合并那一刻：心理资产而非技术成就

<img src="imgs/aicmigr-30-opensource-01-first-pr/d1df37c55fa18474e19d852766f419f6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 提交PR

提交 PR 后，通常几小时到一天内会有 review。SignYourName 这种 PR 一般很快就会被合并，不要在等待期间反复刷新页面，耐心是开源的基本功。

#### (2) 合并的意义

PR 合并那一刻，工程师拿到的不是技术成就，是心理资产。

第一个 PR 是个里程碑。它证明了一件事：工程师能在不属于他的代码上做交付。这件事听起来简单，但在开源里它是分水岭。这个动作做过一次，后面所有 PR 都是基于这个心理基础。

GitHub 个人页面上那条 contribution 不会消失，它会一直在那里。下次有面试官点进工程师的 GitHub，会看到工程师给一个真实开源项目提过 PR。这条记录从此就是工程师简历的一部分，甚至不需要写在简历正文里。

更重要的是，工程师证明了开源没那么难，跨过了开源新人的最大心理门槛。

### 7.7 场景三收尾

跑完场景三，工程师手上有了一个被合并的 PR，GitHub 个人页多了一条 contribution。耗时 30-60 分钟（不含等 review 的几小时到一天）。

## 8. 篇章收尾

<img src="imgs/aicmigr-30-opensource-01-first-pr/51edd5bb4e8430420ea4fa2a95314cdb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 8.1 小结

本篇核心三句话。

#### (1) 第一句：开源 commit 在 AI 时代反而更值钱

AI 时代，开源 commit 是工程师证明自己的少数几条公开可验证的路之一。它不可伪造、不可代笔、AI 写不出来，所以它在 AI 时代反而更值钱。

#### (2) 第二句：开源最难的不是技术，是开第一个口子

但开源最难的不是技术，是开第一个口子。心理门槛比技术门槛高得多。AI 时代帮工程师把"开第一个口子"的成本降低了一个数量级。

#### (3) 第三句：第一个 PR 走通流程优先，合并那一刻拿到的是心理资产

第一个 PR 选什么？标准只有一条：走通流程比技术难度重要。SignYourName 这种入口看着简单，但它跑的是完整 PR 流程，跑通了你后面再大的 PR 都是同一条路。

合并那一刻，工程师拿到的不是技术胜利，是心理资产。这一条比 PR 本身值钱得多。

### 8.2 思考

#### (1) 思考一：复盘自己卡在哪一步

回想之前有没有想过给开源项目提 PR，但最后没成？最卡的是哪一步：找不到合适项目、看不懂代码、不知道怎么走 PR 流程。还是别的？

#### (2) 思考二：用本篇方法论重新走一遍

如果用本篇的"三道题筛项目 + AI 找入口"再走一遍，工程师心里现在有候选项目吗？先写下想提 PR 的那个项目，挑一个走起来。

### 8.3 下一篇预告

系列第 31 篇在第一个 PR 建立的心理基础上，做一个真有难度的 PR，给一个 Rust 项目贡献一段真实业务代码，让 AI 当工程师的 Rust 助教。第 32 篇把整套动作模板化，给一份可以反复用的实操脚本。
