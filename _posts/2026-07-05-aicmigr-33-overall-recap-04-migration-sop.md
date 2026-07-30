---
title: 传统项目迁AI 33：总结复盘 - 老项目改造SOP
author: fangkun119
date: 2026-07-05 13:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-33-overall-recap-04-migration-sop/cover.jpg
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
aicmigr-33-overall-recap-04-migration-sop
传统项目迁AI 33：总结复盘 - 老项目改造SOP
-->

> <span style="color: red; font-weight: bold;">知识可以忘，工作姿势忘不了</span>。这篇文章把企业级老项目改造中跑过的方法论、提示词、踩过的坑重新组织一遍，变成一份能拎走的方法论资产。

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/e1246e1cb9cae25d4c58e86fcd1227af_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

## 1. 老项目改造方法论：三个项目跑出来的同一套打法

为什么要专门给"老项目改造"写一份 SOP？

AI 编程工具（Cursor、Claude Code 等）的官方教程，大多演示"从零起步"的新项目：干净的代码库、明确的需求、流畅的 demo。工程师的真实工作恰恰相反——百万行级的存量代码、模糊的业务需求、脆弱的历史测试。把一个你不熟、文档不全、跑起来就崩的旧系统，用 AI 协作的方式改造成符合新需求的版本，这就是"老项目改造"。

这份 SOP 来自三个真实项目的实战提炼：一个公司内部的 Java 后端项目、一个基于开源 AI Agent 控制平面的二次开发、一个 Rust 开源项目的 PR 贡献。三个项目形态完全不同，跑下来发现背后是同一套方法论。这套方法论的核心不是某个工具的快捷键，而是一种工作姿势——<span style="color: red; font-weight: bold;">面对任何陌生老项目，都能用同一套步骤走下来</span>。

### 1.1 老项目改造沿三种形式展开


<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/63175b24645fc18b46f0500a187ef6e0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">


老项目改造沿三种形式展开。这三种形式不是平行的三个练兵场，而是同一件事的三种渐进难度——后一种比前一种，工程师对代码和 bug 的掌控力逐渐变弱，但对社区的可见度和影响力逐渐变大。

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/6bc0285360e98d3b66acc4dacf6fb31c_MD5.jpg" style="display: block; width: 800px;" alt="三种改造形式与渐进难度全景图">

<!--
图片内容说明
路径：imgs/aicmigr-33-overall-recap-04-migration-sop/6bc0285360e98d3b66acc4dacf6fb31c_MD5.jpg
用途：全景式呈现老项目改造沿三种形式展开的渐进难度结构
内容：三种形式（公司内老项目改造、基于开源做需求、挑战开源）按渐进难度排列，背后是同一套方法论，展示三种形式不是平行的三个练兵场而是同一件事的三种渐进难度
-->

| 形式 | 代码归属 | bug 归属 | 典型场景 | 关键反问或观点 |
|------|----------|----------|----------|----------------|
| 公司内老项目改造 | 公司 | 公司 | 给 Java 后端老系统加新功能、改业务规则 | 跑通"摸项目 → 建护栏 → 拆需求 → 改造"全流程 |
| 基于开源做需求 | 不是你的 | 不是你的 | 在开源 AI Agent 控制平面上做二次开发 | 第一次翻译跑完，先反问"路径选对了吗" |
| 挑战开源 | 不是你的 | 不是你的 | 给 Rust 开源项目提 PR 贡献回去 | AI 时代开源 commit 比以前更值钱 |

为什么这三种形式背后是同一套方法论？因为无论代码归属如何、bug 谁背，工程师面对陌生代码库要做的事情是一样的：<span style="color: red; font-weight: bold;">读懂它、找到改造点、用 AI 高效产出、保住质量</span>。把它放在公司代码、开源代码、待贡献的开源代码上跑三遍，方法论自然收敛。

### 1.2 一套打法的四个动作


<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/0685aa3bc5362a2c24152528a2bab43b_MD5.jpg" style="display: block; width: 800px;" alt="一套打法的四个动作全景图">

<!--
图片内容说明
路径：imgs/aicmigr-33-overall-recap-04-migration-sop/0685aa3bc5362a2c24152528a2bab43b_MD5.jpg
用途：全景式呈现贯穿全系列的一套打法：四个动作及其 AI 参与度边界
内容：四个动作依次为读懂陌生代码（AI 大量参与）、建好改造前护栏（AI 大量参与）、用提示词驱动产出（AI 大量参与）、显式拍板关键决策点（工程师自己做），前三个动作 AI 大量参与产出，第四个动作只能由工程师自己完成
-->

四个动作合起来构成完整的方法论。先看速查表，再展开 why。

| 动作 | 核心动作 | AI 参与度 |
|------|----------|-----------|
| ① 读懂陌生代码 | 给拆解框架（架构/模块/依赖/接口/数据模型），让 AI 展开，认知写成 `CLAUDE.md` 留住 | 大量参与 |
| ② 建好改造前护栏 | Characterization Test 凝固行为，跑得起来才有底气改 | 大量参与 |
| ③ 用提示词驱动产出 | 普通场景驱动写代码/跑测试/提 PR；不熟语言场景让 AI 当助教边写边解释机制 | 大量参与 |
| ④ 显式拍板关键决策点 | 判断 80 分初稿是否建立在对的方向上，方向不对改 100 遍也错 | 工程师自己做 |

<span style="color: red; font-weight: bold;">前三个动作 AI 大量参与产出，第四个动作只能是工程师自己。这条边界是整套方法论最重要的交付。</span>

#### (1) 动作一：读懂陌生代码

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/7d7fba8089bdbd67fad54498722f048b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

为什么不能让 AI 自由发挥去读项目？

<span style="color: red; font-weight: bold;">不给框架，AI 会在它熟悉的维度展开——比如只看 README 和入口函数，漏掉工程师真正关心的依赖关系、数据模型、接口契约</span>。结果 AI 给你一份"看起来很全"的项目概览，实际只覆盖了表层。正确的做法是给一份明确的拆解框架（架构、模块、依赖、接口、数据模型），让 AI 在框架下展开，认知覆盖才完整。

为什么要把认知写成 `CLAUDE.md`？

类比一下：传统软件工程里，每个项目都有一份架构约束文档记录设计决策；AI 协作场景下，`CLAUDE.md` 就是这份"认知锚点"。因为<span style="color: red; font-weight: bold;">"理解债"的本质是代码产出速度远超工程师真正理解代码的速度</span>——AI 帮你展开一遍，下次再问还得展开一遍，除非把这次跑出来的认知写下来。`CLAUDE.md` 就是这份认知的沉淀，后续任何 AI 协作都基于它，认知不必每次重跑。

#### (2) 动作二：建好改造前护栏

为什么 Characterization Test 是生死线？

<span style="color: red; font-weight: bold;">"验证债"的本质是代码看起来对、不代表它真的对</span>。业界调研显示，96% 的开发者不完全信任 AI 产出，但只有 48% 每次都 review——中间这 48% 的缺口就是 bug 漏过去的地方。<span style="color: red; font-weight: bold;">Anthropic 的 52 人对照实验进一步显示，AI 辅助开发者的代码理解能力比对照组低 17%</span>。

<span style="color: red; font-weight: bold;">Characterization Test 不是单纯写测试，而是把"这一刻系统的行为是什么"凝固成可执行代码</span>。类比传统软件工程里的回归测试，但它不验证"系统是否正确"，只验证"系统行为是否发生了非预期的偏移"。任何后续改动跑这套测试就知道有没有偏移。

这一步在大多数 AI 编程教程里被忽略，但在企业级老项目改造里是生死线：<span style="color: red; font-weight: bold;">没有护栏的改造，AI 写得越快、埋的雷越多</span>。

#### (3) 动作三：用提示词驱动产出

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/8a9cd4f9bf910804d20c62ed8ffd300b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

为什么普通场景和不熟语言场景要分两层处理？

因为工程师的判断能力在不同场景下不同：

| 场景 | 工程师能力 | 提示词用法 | AI 角色 |
|------|------------|------------|---------|
| 普通工作场景（熟语言、熟栈） | 能直接判断代码细节对错 | 驱动写代码、跑测试、提 PR，工程师 review 把关 | 代笔 |
| 不熟语言场景（如 Rust） | 没有能力直接判断代码细节对错 | 让 AI 边写边解释关键机制（所有权、async、特定库的 idiom），工程师基于解释做判断 | 助教 |

这是这套打法在 AI 时代最值钱的部分：<span style="color: red; font-weight: bold;">把 AI 从"代笔"升级成"助教"</span>，让工程师的能力边界跟着 AI 解释能力一起扩展，而不是被 AI 替代。工程师不熟没关系，AI 解释机制，工程师做判断。

#### (4) 动作四：显式拍板关键决策点

为什么关键决策点不能让 AI 替你拍？

因为 AI 给的是基于提问方向的 80 分初稿。如果提问方向错了，AI 把这 80 分改成 95 分，只是把错误方向做得更精致。<span style="color: red; font-weight: bold;">方向不对，改 100 遍也是错的。</span>

三个贯穿全系列的拍板动作，本质上是同一件事：

| 出处 | 拍板动作 | 防的风险 |
|------|----------|----------|
| 反问路径 | 第一次翻译跑完，反问"路径选对了吗" | 改造路径债 |
| 先看现状 | 先点产品看现状，再决定怎么改 | 方向错的风险 |
| 挑 issue | 挑算法层面有改进的 issue，而不是一行能改的代码风格 | 投入产出比错位 |

这一动作在工具层面看不见，但跑完整段路径，工程师会感受到自己跟 AI 协作的姿势变了：从"让 AI 写"变成"让 AI 给初稿、自己拍方向"。

### 1.3 三种债与三种风险

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/e34993f443763b8be5843573a353383d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

业界已经在认真讨论 AI 时代工程师要警觉的事：Addy Osmani 提了 Comprehension Debt（理解债），Sonar 调研了 Verification Debt（验证债），Anthropic 的 52 人对照实验显示 AI 辅助开发者的代码理解能力比对照组低 17%。

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/bd952aa2e2a560e8b9af2c15bfc6e6d9_MD5.jpg" style="display: block; width: 800px;" alt="三种债与三种风险全景图">

<!--
图片内容说明
路径：imgs/aicmigr-33-overall-recap-04-migration-sop/bd952aa2e2a560e8b9af2c15bfc6e6d9_MD5.jpg
用途：全景式呈现 AI 时代工程师需要警觉的三种债与三种风险及其对应解药
内容：左侧三种债（理解债、验证债、改造路径债）及其解药主线，右侧三种风险（方向错、AI 偷懒、工程师跟不上）及其解药主线，揭示工程师在 AI 时代的真实角色是对 AI 的产出负责
-->

这些债和风险，在每个项目里都有具体的样子。把业界说法、项目里的真实样子和解药落地对齐，方便对照自己手头的项目：

| 名称 | 业界说法 | 项目里的真实样子 | 解药落地 |
|------|----------|------------------|----------|
| 理解债 | Addy Osmani 提出的 Comprehension Debt：AI 写得快，工程师脑子里没跟上 | 摸 Java 后端项目时，AI 一晚上能展开五个模块的依赖关系，但工程师第二天醒来未必能讲清楚这五个模块的调用链 | 把 AI 展开的认知写成 `CLAUDE.md`，作为团队的认知锚点，后续 AI 协作都基于它 |
| 验证债 | Sonar 调研的 Verification Debt：96% 开发者不完全信任 AI 产出，但只有 48% 每次都 review；Anthropic 52 人对照实验显示 AI 辅助开发者代码理解能力比对照组低 17% | AI 改完一段 Java 代码、跑通现有测试，看起来一切正常。但现有测试覆盖不到的边界 case，可能已经被 AI 的"看起来对"糊弄过去 | Characterization Test 把改造前的系统行为凝固成可执行代码，任何后续改动跑这套测试就知道有没有偏移 |
| 改造路径债 | <span style="color: red; font-weight: bold;">第一次跑出来的方案不一定是最优路径，如果不停下来反问，后面所有动作都建立在错的地基上</span> | 基于开源 AI Agent 控制平面做二次开发时，AI 给的第一版翻译方案看起来面面俱到。但如果直接动手实现，可能要写大量基础设施代码——而开源项目本身已经把 80% 都做了 | 第一次翻译跑完之后，先停下来反问"路径选对了吗"，再决定走哪条路 |
| 方向错的风险 | <span style="color: red; font-weight: bold;">如果工程师问"怎么实现 X"，AI 会想尽办法实现 X；但真正的需求可能是 Y</span> | 工程师把"加个缓存"作为提问方向，AI 给出 5 种缓存方案；但真正的需求可能是"降低接口延迟"，缓存只是其中一种解 | 在拆方案、挑 issue、定路径这几个关键决策点，工程师自己拍板，不接受 AI 的默认值 |
| AI 偷懒的风险 | 在边界 case、warning 处理、不熟的栈上，AI 会偷偷用 `#[allow]` 或泛化兜底糊弄过去 | 改 Rust 代码时，AI 可能用一行 `#[allow(dead_code)]` 把"懒得处理的警告"盖过去 | review 时专门扫一遍 `#[allow]`、泛化兜底、warning 处理，确认没有偷懒糊弄；allow 注解能不留就不留 |
| 工程师跟不上的风险 | <span style="color: red; font-weight: bold;">AI 越用越快，工程师对代码的真实把握越来越浅。某天 AI 出了 bug，工程师已经不知道怎么修了——这是最危险的风险，因为它会悄悄累积，到爆雷时已经晚了</span> | 工程师连续几周用 AI 改老代码，速度很快、产出很多；但当 AI 写出的某段代码出了线上 bug，工程师发现自己读不懂那段代码，不知道从哪下手 | 让 AI 当助教，而不是写完就走的合伙人——不熟的语言/库，让 AI 解释机制（所有权、async、idiom），由工程师做判断 |

**AI 偷懒的风险**有具体代码信号，单独看一下。改 Rust 代码时典型信号长这样：

```rust
#[allow(dead_code)]   // 警惕：AI 偷懒的典型信号
fn legacy_handler() { ... }
```

review 时扫一遍 `#[allow]`、泛化兜底、warning 处理，allow 注解能不留就不留。

把这三种债和三种风险加起来，工程师在 AI 时代真正的角色是：<span style="color: red; font-weight: bold;">不是用 AI 的人，是对 AI 的产出负责的人</span>。

### 1.4 关键决策点 Check List

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/d89129ecb5281d2ae4b46af9f859469e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节是可裁剪速查表，供项目阶段评审时勾选。每条都是一个必须由工程师自己拍板的决策点，不能让 AI 替你拍。

#### (1) 摸项目阶段

- [ ] 是否给了 AI 一份明确的拆解框架（架构/模块/依赖/接口/数据模型），而不是让 AI 自由发挥？
- [ ] 跑出来的认知是否写成了 `CLAUDE.md`，留住对项目的理解？
- [ ] 是否点过产品、看过现状，而不是只看代码？

#### (2) 建护栏阶段

- [ ] 是否有能跑通的兜底测试，而不只是新增测试？
- [ ] Characterization Test 是否把"这一刻系统的行为"凝固成可执行代码？
- [ ] 任何后续改动跑这套测试，能否立刻发现行为偏移？

#### (3) 拆方案阶段

- [ ] 第一次翻译跑完后，是否停下来反问过"路径选对了吗"？
- [ ] 是否检查过刚摸过的开源项目/现有工程，方案里 80% 是不是已经有人做了？
- [ ] AI 给的方案是基于对的方向，还是基于你提问的方向？

#### (4) 写代码与 review 阶段

- [ ] AI 给的 80 分初稿，是否先判断方向对不对，再决定要不要继续改？
- [ ] 是否扫过 AI 写的 `#[allow]`、泛化兜底、warning 处理，确认没有偷懒糊弄？
- [ ] 不熟的语言/库，是否让 AI 解释机制（所有权、async、idiom），由工程师做判断？
- [ ] 关键决策点（选哪个 issue、走哪条路径、保哪个数据结构）是否由工程师拍板？

#### (5) 挑战开源阶段

- [ ] 挑的 issue 是否在算法层面有改进，而不是一行能改的代码风格？
- [ ] commit 是否公开可验证、可点击回溯？
- [ ] 贡献是否真的回去了，而不是停在 fork 分支？

## 2. 把方法论带回自己项目的 3 个月落地路径

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/1f491192b6362d7e79bbb03f2d5b6a92_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

读完方法论，真正的问题来了：怎么把它带回自己手头的项目？这里给出一份具体可执行的 3 个月路径——这不是建议，是一份可以照着跑的练习计划。

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/8c8f47a6022de8343281af3454ee8398_MD5.jpg" style="display: block; width: 800px;" alt="3 个月落地路径">

<!--
图片内容说明
路径：imgs/aicmigr-33-overall-recap-04-migration-sop/8c8f47a6022de8343281af3454ee8398_MD5.jpg
用途：展示把方法论带回自己项目的 3 个月落地路径全景
内容：以时间轴方式呈现第 1 个月（摸项目 + 建护栏）、第 2 个月（做一次真实改造）、第 3 个月上半（基于开源做二次开发）、第 3 个月下半（挑战开源）四个阶段的目标、对应篇目和关键动作
-->

### 2.1 第 1 个月：摸项目 + 建护栏

| 维度 | 内容 |
|------|------|
| 目标 | 在公司老项目上跑通"摸项目 → 建护栏"的前半段 |
| 对应篇目 | 第 06-16 篇 |
| 关键动作 | 用四维度框架让 AI 给项目地图；跑通能跑的护栏；写 `CLAUDE.md` 留认知 |
| 验收信号 | 一周内拿到一份能跑的护栏测试套件，AI 协作基于 `CLAUDE.md` |
| 时间预算 | 一周够，不需要熬夜 |

### 2.2 第 2 个月：做一次真实改造

| 维度 | 内容 |
|------|------|
| 目标 | 在那个老项目上挑一个真实需求，跑完整改造流程 |
| 对应篇目 | 第 17-21 篇 |
| 关键动作 | 按"拆需求 → 拆方案 → 后端 → 前端"全流程跑；用第 21 篇的一键流程提示词；关键决策点停下来等工程师拍板 |
| 验收信号 | 三周内做完一次需求改造，代码合到主干 |
| 交付物 | 给本系列交的第一份作业 |

### 2.3 第 3 个月（上半）：基于开源做二次开发

| 维度 | 内容 |
|------|------|
| 目标 | 挑工作里能用上的开源项目，做点真实的东西 |
| 对应篇目 | 第 24-29 篇 |
| 候选项目 | 消息队列、Agent 框架、AI 工具链等 |
| 关键动作 | 按"两次翻译"流程做；第二次翻译前先反问"路径选对了吗" |
| 验收信号 | 月底产出一份方案文档 + 一份能跑的实现（不一定上线，认知和方法论也值得） |

### 2.4 第 3 个月（下半）：挑战开源

| 维度 | 内容 |
|------|------|
| 目标 | 给一个真实活跃的开源项目提第一个 PR |
| 对应篇目 | 第 30-32 篇 |
| 关键动作 | 按本系列流程筛项目；挑算法层面有改进的 issue；提 PR |
| 验收信号 | 月底 GitHub 个人页面多一条 contribution |
| 兑现观点 | 第 30 篇：在 AI 时代开源 commit 依然是简历最值钱的资产 |

走完这个流程，工程师的工作姿势会变成另一个样子。不是因为你做了什么了不起的事，是因为你没停下来。<span style="color: red; font-weight: bold;">频次大于单次重量</span>——这是项目改造、开源贡献、甚至整个工程师生涯里最值钱的一条。

## 3. 三句话带走整套方法论

<img src="imgs/aicmigr-33-overall-recap-04-migration-sop/f9ca021b96e22ea31a0c5529e560b19e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

整套方法论浓缩成三句话，每句都对应前面的具体动作。

### 3.1 把方向定准比把代码写好更值钱

<span style="color: red; font-weight: bold;">AI 时代，工程师能交付的最值钱的东西不是写代码，而是把方向定准、把约束讲清楚、把模糊变具体的能力</span>。AI 给一个 80 分初稿，工程师要做的不是改成 95 分，而是先判断初稿建立在对的方向上。<span style="color: red; font-weight: bold;">方向不对，改 100 遍也是错的。</span>

这句话的实战落点：动作四"显式拍板关键决策点"贯穿全程——反问路径、先看产品现状、挑算法级 issue，本质上都是这一句的具体化。

### 3.2 把时间投在 AI 替代不了的协作和判断上

<span style="color: red; font-weight: bold;">让 AI 加速产出，把时间投在它替代不了的协作和判断上。</span>写代码这一段大量交给 AI，但 review、讨论、长期跟踪、跟人协作、判断方向，这些自己做。这是 AI 时代工程师的正确姿势。

这句话的实战落点：动作三的"两层处理"——普通场景让 AI 代笔、不熟语言场景让 AI 当助教——本质上是把工程师的时间从"写"挪到"判"。

### 3.3 开源是耐心比赛，不是技术比赛

<span style="color: red; font-weight: bold;">开源不是技术比赛，是耐心比赛。</span>AI 时代，这条更对。挑战开源的本质是教"把工程师生涯当长期事情来看"。3 个月、6 个月、1 年后回头看，会感谢现在按这个节奏走的自己。

这句话的实战落点：挑战开源的完整路径，以及 3 个月落地路径，都是这一句的可执行版本。

到这里，三个项目、三种形式、一套打法、一份 SOP，全部交付。
