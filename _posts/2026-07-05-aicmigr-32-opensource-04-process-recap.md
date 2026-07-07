---
title: 传统项目迁AI 32：挑战开源 - 流程回顾
author: fangkun119
date: 2026-07-05 12:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
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

**AI 编程实战 · 挑战开源流程回顾：把整套动作打包成可反复用的清单**

本篇是"挑战开源"系列的收尾篇。系列第 30、31 篇把一个第一个 PR、一个第二个 PR、一个 issue 全部跑通，本篇把那两篇的所有动作连起来打包成一份可以反复用的清单——以后碰到任何项目都按这份清单跑。

本篇既是实操回顾，也附上完整的提示词清单用于指导实践。跑完之后，工程师的项目里就有了完整的"挑战开源"工作流。

还有一件事必须开篇就说：系列第 30 篇那个"挑了排在最稳那一档的项目"的判断，后来才意识到背后有几条 AI 推荐之外的隐性信号。本篇经验分享章节会把那几条讲清楚——找项目这件事，AI 帮工程师筛 80%，剩下 20% 是工程师的判断。

本篇按两部分组织：第一部分（第 2 章到第 4 章）是方法论提炼，速查导向，不深入具体技术栈；第二部分（第 5 章到第 10 章）是实战演示，结合 RobustMQ 项目与 Rust、tokio、gh CLI 等技术栈复现全过程，解释 why。

**全文导读地图**

<img src="imgs/aicmigr-32-opensource-04-process-recap/d0f81bb26bbb38b23a0807d966c22877_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    START([接到一个开源项目]) --\> P0[第零阶段 准备]
    P0 --\> P1[第一阶段 找项目]
    P1 --\> P2[第二阶段 第一个 PR 跑通流程]
    P2 --\> P3[第三阶段 第二个 PR 真本事]
    P3 --\> P4[第四阶段 提高质量 issue]
    P4 --\> ONE[一键流程 串联四阶段]
    ONE --\> EXP[经验分享 隐性信号/PR 被 close/长期复利/AI 时代门槛]
    EXP --\> DONE([GitHub 个人页留下真实贡献痕迹])

    P1 -. 三道题筛项目 .-> M1[5 个候选 + good first issue 入口]
    P2 -. SignYourName 类入口 .-> M2[fork→branch→commit→push→PR→merge]
    P3 -. 四维度地图 + 三条标准 + 长提示词 .-> M3[AI 当助教 plan→代码→测试→lint→PR]
    P4 -. 算法层面性能问题 .-> M4[5 节 issue 模板 + 我可以跟 PR 信号]

    BEGINNER([初学 AI 编程工程师]) -. 从头读 .-> P0
    EXPERT([熟练 AI 编程工程师]) -. 跳 Check List .-> CL[第 4 章 全流程 Check List]
    CL -. 需要展开 .-> P1
-->

**两类读者怎么读这篇文档：**

- 初学 AI 编程工程师：通读第一部分建立方法论框架（四阶段工作流、关键技术决策、Check List），再按第二部分场景一到场景四的顺序，对照 RobustMQ 案例复现一遍，每个 review 重点都对照自查
- 熟练 AI 编程工程师：直接看第 4 章 Check List 速查；遇到想回顾"为什么这么做"时回到第 3 章对应的方法论节，或第二部分对应场景

## 1. 开篇与定位

<img src="imgs/aicmigr-32-opensource-04-process-recap/856a4ceda56ad0889a91ecfd868738de_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 1.1 本篇在系列中的位置

整套系列讲的是同一件事：AI 时代怎么做老项目改造。这件事有三种形式，背后是同一套方法论——读懂陌生代码、找到改造点、用 AI 高效产出、保住质量。

| 形式 | 代码归属 | bug 归属 | 改造方向归属 | 典型场景 |
|------|---------|---------|-------------|---------|
| 公司内的老项目 | 工程师自己的 | 工程师自己的 | 工程师自己定 | 给业务代码库加功能、修缺陷 |
| 基于开源做需求 | 不是自己的 | 不归自己管 | 工程师自己的用法 | 在开源骨架上长业务 |
| 贡献开源（第七部分） | 不是自己的 | 不是自己的 | 贡献回去 | 给社区项目提 PR |

第七部分把这套方法论用在"别人的老项目"上，走完最后一公里。第七部分三篇：系列第 30 篇跑通第一个 PR 的心理胜利，系列第 31 篇跑通第二个 PR + 第一个 issue 的真本事，本篇把动作打包成可反复用的清单 + 经验分享。

### 1.2 本篇要打包的资产

本篇把第 30、31 两篇里跑过的动作重新组织成一份"反复用"资产。这份资产有三类读者用得上的东西：

- 一份按阶段组织的 Check List，工程师上手任何开源项目时直接照表跑
- 一份覆盖四阶段的关键提示词清单，可直接复制到 Claude Code 实跑
- 一段一键流程提示词，让 Claude Code 在工程师选好项目和 issue 后串行自主跑完整流程
- 一组经验分享，补第 30、31 篇学不到、只能靠时间踩出来的隐性知识

## 2. 工作流总览

<img src="imgs/aicmigr-32-opensource-04-process-recap/7b1ef15f6254bddfb23aded8cf59a6b6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-32-opensource-04-process-recap/fea609be1892cf872ca3370bc26c88c4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 工作流的工程语境

这套工作流要解决的问题很具体：工程师想给开源项目贡献代码，但开源协作有它自己的姿势——不属于工程师的代码、不属于工程师的方向、维护者不归工程师管。工程师需要把第 30、31 两篇跑通过的所有动作标准化，形成一份可以反复用、可以迁移到任何项目的清单。

整个工作流横跨五个阶段：准备、找项目、第一个 PR 跑通流程、第二个 PR 真本事、提高质量 issue。前两个阶段属于"想清楚做什么"，后三个阶段属于"把它做出来并跑通"。最后还可以把后三个阶段串成一键流程。

### 2.2 五阶段概览

| 阶段 | 核心动作 | 关键产出 |
|------|----------|----------|
| 准备 | GitHub 账号、SSH key、Claude Code、gh CLI 配齐 | 真实开源工程师的工具栈 |
| 找项目 | 用三道题标准 + AI 跑出候选 + 找最简单的 PR 入口 | 一个匹配背景的载体项目 + 一个零门槛 PR 目标 |
| 第一个 PR 跑通流程 | fork + clone 之后，让 AI 跑剩余的 branch/commit/push/PR | 一个被合并的 SignYourName 类 PR |
| 第二个 PR 真本事 | 摸项目找方向 → 筛 issue → 让 AI 当助教实现 PR | 一个有真实工程价值的 PR |
| 提高质量 issue | 扫代码找算法层性能问题 → 写高质量 issue 模板 | 一个被维护者认真讨论的 issue |

### 2.3 阶段之间的依赖关系

准备阶段是所有后续阶段的前置条件，缺一项工具栈后面都跑不通。找项目阶段决定载体，载体决定第一个 PR 入口能找到什么。第一个 PR 跑通流程之后，工程师才掌握 fork → branch → commit → push → PR → review → merge 这条肌肉记忆，第二个 PR 才能聚焦技术内容。第二个 PR 实现过程中扫代码积累的认知，是高质量 issue 的输入。前三个阶段是"建立基础"，后两个阶段是"展示真本事"。

## 3. 关键方法论

### 3.1 找项目：三道题标准 + AI 半小时给 5 个候选

<img src="imgs/aicmigr-32-opensource-04-process-recap/de1f57fb002f97c810fb84956003e41b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

开源新人最常踩的坑是心里没标准。心里没标准没关系，AI 帮工程师建标准。工程师只要把三道题告诉 AI，加上自己的技术栈背景，AI 半小时就能给 5 个候选。

#### (1) 三道题是什么

三道题本质上是把"什么样的项目适合作为第一个 PR 目标"拆成可判断的硬条件。

##### ① 真实技术含量

不是 demo 项目、不是教程配套仓库、不是单纯 markdown 集合。项目里得有真实的工程代码、真实的测试、真实的 issue 在被讨论。

##### ② 正在快速成长但还没到顶级

几百到几千 star、有持续 commit、有几个核心维护者，但还没到几万 star、没成为行业标准。这种区间的项目最欢迎新人——既活、又有上升空间、维护者还有空 review。

##### ③ 跟技术栈或职业方向有关联

工程师后续要在这个项目上持续投入，关联性是必须的。完全不相关的项目，跑完第一个 PR 之后再提第二个就提不动了。

#### (2) review 重点：三道题必须每一条都打中

AI 给的 5 个候选里，每一个都要满足三道题。如果有候选 star 数超 5 万或者最近 3 个月没 commit，这个候选要砍掉。AI 推荐之后还要靠工程师自己判断——本篇经验分享章节会展开几条 AI 推荐之外的隐性信号。

### 3.2 找 PR 入口：项目方设计的"first PR"入口优先

挑好项目之后，下一步是找最简单的 PR 入口。第一个 PR 不证明工程师技术多牛，只证明能走通流程。所以入口要越简单越好。

#### (1) 入口优先级

##### ① 项目方专门设计的入口

最理想的是项目方为新人专门设计的"签名墙"、"贡献者列表加自己"那种入口。改一行 markdown 就行，跑完整 PR 流程。

##### ② good first issue 标签下的 typo 或文档补充

如果项目没有专门入口，退而求其次找 typo 修订或文档补充。这种改动小、不影响功能、维护者 review 起来也轻松。

##### ③ 别选超过 50 行的 good first issue

超过 50 行的"good first issue"不是真的 good for first——它对新人来说规模已经超出预期，跑起来很容易踩"50 行实际改完发现是 500 行"的坑。

#### (2) review 重点：让 AI 扫仓库找入口

让 AI 扫一遍仓库的 README、CONTRIBUTING.md、docs/，看有没有项目方为新人设计的 first PR 入口。提示词原文见第 6 章场景一。

### 3.3 第一个 PR：三个护栏防 AI 瞎搞

<img src="imgs/aicmigr-32-opensource-04-process-recap/fea609be1892cf872ca3370bc26c88c4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第一个 PR 的核心动作是让 AI 跑完 fork 之后的剩余流程：建 branch、改文件、跑 lint、commit、push、写 PR 描述、提 PR。但 AI 在这种长流程里容易瞎搞，工程师要在提示词里设三个护栏。

#### (1) 三个护栏

##### ① 每步执行前告知 + 报错立刻停

每一步执行前告诉工程师要做什么，执行后告诉结果。任何报错立刻停下来问，不要自己瞎修。这条防 AI 在报错时陷入"自己改自己测"的循环。

##### ② 别瞎跑 cargo build（针对 Rust 项目）

markdown 改动只跑相关检查就行，不要瞎跑 cargo build——在 RobustMQ 这种大型 Rust 项目，cargo build 一次几分钟，markdown 改动跑这个纯属浪费时间。

##### ③ gh CLI 优先 + fallback 人工

如果系统装了 gh CLI，直接 `gh pr create` 提 PR；没装就把 PR 描述输出给工程师，由工程师自己去 GitHub 点提交。这是真实工程师的工具链——能自动化的自动化，自动化不了的 fallback 人工。

#### (2) review 重点：完整流程跑通的标志

跑完场景二，第一个 PR 提了，等维护者 review。SignYourName 这种 PR 通常一天内被合并，耗时 30-60 分钟。

### 3.4 第二个 PR：让 AI 当助教，工程师做判断

<img src="imgs/aicmigr-32-opensource-04-process-recap/66829c567fc7af7f725dcd8944fe6a5b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二个 PR 要真有技术含量。在工程师不熟的项目（典型如 Rust 写的 RobustMQ）上，AI 当助教解释机制，工程师做判断。流程分三步：摸项目找方向、筛 issue、实现 PR。

#### (1) 第一步：带任务问的项目地图

##### ① 四维度框架

让 AI 按模块划分、测试组织、贡献热点、适合新人的入口这四个维度给一份项目地图。但仅限"针对找 issue 任务"的那部分，不要给一份干巴巴的完整架构文档。

##### ② 带任务问是关键

AI 不知道工程师要干嘛时，会给一份干巴巴的架构介绍。告诉 AI 目的，它会过滤出有用部分。这条姿势在系列第 24 篇已经建立，本篇再用一次。

#### (2) 第二步：三条标准筛 issue

筛 issue 必须用三条标准：改动量 50-200 行、描述清楚且改动局部、不需要深度业务理解（工程师能 review 得了 AI 写的代码）。

##### ① 风险点必须列

AI 推荐 issue 时容易忽略"看起来 50 行实际改完发现是 500 行"的隐藏复杂度。每个候选必须显式说"如果改起来比预期大要在哪一刻停下来"——这是新人最容易踩的坑。

#### (3) 第三步：长提示词实现 PR

挑好 issue 之后用一段长提示词让 AI 跑完整套实现 + 测试 + 提交。长提示词的四个关键设计在下面。

##### ① 先 plan 后改

不能拿到 issue 闷头写。AI 先读 issue 完整描述和相关代码，给一份"我要改什么"的简短 plan（3-5 条），工程师审核 plan 之后才能进下一步。

##### ② 关键改动逐段解释

AI 写完之后，关键改动要逐段告诉工程师"这里在做什么、为什么这么写"。任何用到栈特有机制的地方（如 Rust 的 lifetime / async trait / ownership / Send + Sync），AI 要简短解释。这是 AI 当助教的核心动作。

##### ③ 不为炫技用复杂 idiom

如果有多种合理写法，选项目现有代码里最常用的那种风格，不要为了炫技用复杂的 idiom。这条防 AI 写出工程师 review 不了的高手风格代码。

##### ④ 所有 warning 清掉不留 allow 绕过

跑 lint（如 cargo clippy），所有 warning 都要清掉，不要留 `#[allow(...)]` 绕过。活跃项目对 PR 质量有硬要求，allow 绕过是新人最容易在这里偷懒的地方。

### 3.5 高质量 issue：算法层有改进 + 我可以跟 PR 信号

<img src="imgs/aicmigr-32-opensource-04-process-recap/e5cb0d3c171c083c584a1eadb8df8126_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二个 PR 提了之后，接下来要做的不是马上提第三个 PR，而是提一个高质量 issue。原因有两个：第一，issue 比 PR 更难——PR 是修好了问题，issue 是发现了值得讨论的问题，后者需要工程师对项目有判断力。第二，issue 是跟维护者建立更好连接的入口，一个高质量 issue 会吸引维护者来讨论，这种讨论比 PR review 的对话深度高得多。

#### (1) 第一步：扫代码找算法层性能问题

让 AI 扫一遍代码找潜在的性能问题，重点看不必要的 clone / async 锁 / 循环内分配 / iterator 低效用法。但不要找 clippy 已经能查出来的（项目 CI 自己会跑）——要找那种需要看上下文才能发现的真问题。

##### ① 挑算法层面有改进的那一个

挑那种"算法层面有改进、修复需要讨论方向、需要测试验证"的问题，不是挑最简单的。"多余的 collect"、"多余的 clone"那种 1 行就能改的代码风格问题，维护者反应通常是"直接提 PR 删一行就行，提 issue 干嘛"。

#### (2) 第二步：写高质量 issue 模板

让 AI 按五节模板写 issue：Description / Reproduction / Impact / Suggested fix / Environment。

##### ① 显式带"我可以跟 PR"信号

最后一条"我可以跟 PR"是高质量 issue 的标志。它告诉维护者工程师不是只想刷 contribution 数字的伸手党，愿意接着把这件事做完。很多时候维护者看到这一句就会主动来 review 和讨论。

##### ② 语气是普通工程师

语气是普通工程师报告问题，不是 LinkedIn 帖子，不是问问题。这条与第一个 PR 描述的"普通工程师不是 LinkedIn 帖子"原则一致。

### 3.6 一键流程：让 Claude Code 自主跑完整流程

前面四个场景一个个跑，是为了让工程师看清每一步的产出和 review 点。真正上手之后，工程师会希望一次粘贴、Claude Code 自主跑完整流程、关键决策点停下来等工程师判断。

#### (1) 一键流程的边界

**不管 fork 和选项目这两件事。** 一键流程不管 fork 这一步（GitHub 账号操作 AI 做不了），也不管挑哪个项目和挑哪个 issue——这两个决策必须人来挑。它从已经选好项目、并 fork + clone 完这一步开始。

**第一个 PR 期间并行筛第二个 PR 的 issue。** 第一个 PR 提了之后，等 review 通常要几小时到一天。这段空窗期最适合找下一件事——筛第二个 PR 的 issue。AI 不会主动并行做事，工程师要在提示词里显式告诉它什么时候开始下一步。

**关键决策显式停下来等人工。** issue 的选择和修复方向这两件事 AI 替工程师拍是最危险的——挑了一个写完才发现规模超预期的 issue，或者修复方向跟项目方向不一致，几小时白白浪费。提示词里要显式写"停下来等工程师反馈"。

**超时护栏。** "超 1.5 倍预估时间停下来报告"是给 AI 装的护栏。AI 容易陷在某个细节上反复打磨，工程师要在提示词里告诉它"超时就停下来让我看"，而不是让它一直跑直到上下文耗尽。

## 4. 全流程 Check List

<img src="imgs/aicmigr-32-opensource-04-process-recap/ef263ec8a1f1506b69626017b573f5c1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

下表把五个阶段拆成可裁剪的速查表。熟练工程师上手新项目时可以直接拿这张表逐项核对，不需要回到正文。

### 4.1 五阶段 Check List

| 阶段 | 必做硬性条目 | 必停决策点 | 必 review 的产出 |
|------|--------------|-----------|-----------------|
| 准备 | GitHub 账号、SSH key、Claude Code、gh CLI 都配齐 | 无 | gh CLI 能不能 `gh auth status` 通过 |
| 找项目 | 三道题都打中、star 数 < 5 万、近 3 个月有 commit | 5 个候选里挑哪一个 | good first issue 入口是不是项目方专门设计的（签名墙最优） |
| 第一个 PR | SignYourName 类一行改动、跑了相关 lint（不是 cargo build）、Conventional Commits | 报错时立刻停下来 | PR 描述是普通工程师语气、不是 LinkedIn 帖子 |
| 第二个 PR | 改动量 50-200 行、plan 先于代码、关键改动逐段解释、所有 warning 清掉 | issue 选择、修复方向 plan 审核 | PR 描述含 trade-off / 后续可改进点 |
| 高质量 issue | 算法层有改进（非 1 行能改的代码风格）、5 节模板齐全 | 挑哪一个性能问题、修复思路 | 显式带"我可以跟 PR"信号 |

### 4.2 阶段时间预算

| 阶段 | 时间预算 |
|------|---------|
| 准备（一次性） | 30 分钟 |
| 找项目 | 30-60 分钟 |
| 第一个 PR | 30-60 分钟（含等 review） |
| 第二个 PR | 1-1.5 小时 |
| 高质量 issue | 30-45 分钟 |
| 一键流程（全跑通） | 3-4 小时（含几次 review） |

### 4.3 反模式速查（不要做这些）

| 反模式 | 后果 | 正确姿势 |
|--------|------|---------|
| 挑 star 数 > 5 万的顶级项目 | review 周期几个月，新人磨没信心 | 三道题筛 + AI 推荐 + 隐性信号过滤 |
| 挑 > 50 行的 good first issue | 实际改完发现是 500 行 | 找签名墙类入口或 typo / 文档补充 |
| 让 AI 在报错时自己改自己测 | AI 陷入循环，时间耗尽 | 提示词显式写"报错立刻停下来问" |
| markdown 改动跑 cargo build | 几分钟编译纯 markdown 改动 | 只跑相关 lint |
| 拿到 issue 闷头写代码 | 改完发现方向不对 | 先 plan 后改，工程师审核 |
| 留 `#[allow(...)]` 绕过 warning | 维护者直接 close PR | 所有 warning 都清掉 |
| issue 选 1 行能改的代码风格问题 | 维护者说"直接提 PR 删一行" | 选算法层有改进的问题 |
| issue 不带"我可以跟 PR"信号 | 维护者当作伸手党 | 显式说愿意跟 PR |

## 5. 实战演示总览

<img src="imgs/aicmigr-32-opensource-04-process-recap/568f255fc25232b6f782b5586f940971_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二部分把第一部分的方法论落到具体场景里。所有场景都以 RobustMQ 作为示范项目——整个工作流跟项目无关，提示词改个项目名，就能复刻到任何项目上。

### 5.1 四个场景的全局对照

| 场景 | 对应系列篇目 | 核心产出 | 主要方法论 |
|------|-------------|---------|-----------|
| 场景一 找项目 | 系列第 30 篇前半段 | 5 个候选项目 + good first issue 入口 | 三道题标准、找最简单入口 |
| 场景二 第一个 PR | 系列第 30 篇 | 第一个被合并的 PR（SignYourName） | 三个护栏、完整流程跑通 |
| 场景三 第二个 PR | 系列第 31 篇前半段 | 第二个被合并的 PR（有技术含量） | 四维度地图、三条标准、长提示词 |
| 场景四 高质量 issue | 系列第 31 篇后半段 | 第一个被认真讨论的 issue | 算法层问题、5 节模板、我可以跟 PR |

### 5.2 实战前的准备

GitHub 账号已注册、SSH key 配过、Claude Code 跑得起来、装了 gh CLI（真实开源工程师的工具栈）。

如果工程师跳过了前面直接看本篇，先回到系列第 30 篇跑一遍 SignYourName，确保已经走通过完整的 fork → branch → commit → push → PR → merge 流程。否则后面所有提示词的"基于第 30 篇那套姿势"都没有素材。

下面承接系列第 30、31 篇的内容，以 RobustMQ 作为具体示范项目。

## 6. 场景一 找到合适自己的项目

对应系列第 30 篇前半段，产出：一个匹配工程师背景的候选项目、一个最简单的第一个 PR 入口。

这个场景的灵魂是心里没标准没关系，AI 帮工程师建标准。工程师只要把三道题告诉 AI，加上技术栈背景，AI 半小时给 5 个候选。

### 6.1 第一步：让 AI 推荐项目

打开 Claude Code，丢这一段提示词。

#### (1) 提示词原文

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

#### (2) review 重点

5 个候选中的每一个都要满足三道题。如果有候选 star 数超 5 万或者最近 3 个月没 commit，这个候选要砍掉。AI 推荐之后还要靠工程师自己判断，后面经验分享章节有几条隐性信号要套。

### 6.2 第二步：让 AI 找最简单的 PR 入口

挑好项目，先 git clone 下来，在仓库目录下打开 Claude Code。

#### (1) 提示词原文

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

### 6.3 场景一收尾

跑完场景一，工程师手里有了一个匹配自己背景的项目、一个最简单的 PR 入口。下面场景二、三承接系列第 30 篇的内容，以 RobustMQ 为例，继续把整个流程跑完。

## 7. 场景二 以 RobustMQ 为例，跑通第一个 PR

<img src="imgs/aicmigr-32-opensource-04-process-recap/8ef3390205f842227735ae1a8d48dd51_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应系列第 30 篇，产出：第一个被合并到 RobustMQ 的 PR。

这个场景的灵魂是走通流程比技术难度重要。第一个 PR 不证明工程师技术多牛，只证明能把 fork → branch → commit → push → PR → review → merge 整条流程跑通。

### 7.1 为什么挑 RobustMQ

系列第 30 篇让 AI 用提示词 1 推荐项目，5 个候选里挑了 RobustMQ，因为它三道题都打中：基础软件赛道、活跃但还没到顶级、Rust 写的，跟学习 Rust 的方向对得上。RobustMQ 还在 docs 里给新人贡献者准备了一个 SIGNYOURNAME.md 入口，把自己的名字加一行进去就行。这正是这一步要的——最简单的入口，跑完整 PR 流程。

### 7.2 人工的两步

#### (1) 第一步：fork RobustMQ 仓库

打开 `https://github.com/robustmq/robustmq`，右上角点 Fork。

#### (2) 第二步：clone 到本地

```bash
git clone git@github.com:<your-username>/robustmq.git
cd robustmq
```

### 7.3 让 AI 跑剩余流程

剩下全部丢给 Claude Code。在仓库目录下打开 Claude Code，丢这一段提示词。

#### (1) 提示词原文

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

#### (2) review 重点：三个护栏

三个护栏必须在提示词里写清楚：每步执行前告知 + 报错立刻停（防 AI 瞎修）；别瞎跑 cargo build（防 AI 在 RobustMQ 这种大型 Rust 项目花几分钟编译 markdown 改动）；gh CLI 优先 + fallback 人工（真实工程师工具链）。

### 7.4 场景二收尾

跑完场景二，第一个 PR 提了，等 RobustMQ 维护者 review。SignYourName 这种 PR 通常一天内被合并。耗时 30-60 分钟。

## 8. 场景三 以 RobustMQ 为例，跑通第二个 PR

对应系列第 31 篇，产出：第二个被合并到 RobustMQ 的 PR。

这个场景的灵魂是让 AI 当 Rust 助教。RobustMQ 是 Rust 写的消息中间件，代码量大、用了大量 async 和 tokio 生态。工程师 Rust 不熟没关系，AI 解释机制，工程师做判断。

第一个 PR 跑通流程之后，第二个 PR 要真有技术含量。走通三步：摸项目找方向、筛 issue、实现 PR。

### 8.1 第一步：摸 RobustMQ 项目（针对找 issue 任务）

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

#### (2) review 重点：带任务问

AI 不知道工程师要干嘛，会给一份干巴巴架构介绍。告诉 AI 目的，它会过滤出有用部分。这是系列第 24 篇建立的姿势，在本篇再用一次。

### 8.2 第二步：让 AI 帮工程师筛 issue

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

#### (2) review 重点：风险点必须列

AI 推荐 issue 时容易忽略"看起来 50 行实际改完发现是 500 行"的隐藏复杂度。每个候选必须显式说"如果改起来比预期大要在哪一刻停下来"——这是新人最容易踩的坑。

### 8.3 第三步：实现 PR（长提示词）

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

#### (2) review 重点：四件事

四件事必须在提示词里写清楚：先 plan 后改（不能拿到 issue 闷头写）；关键改动逐段解释（AI 当 Rust 助教）；不为炫技用复杂 idiom（防 AI 写出工程师 review 不了的高手风格 Rust）；所有 warning 清掉不留 allow 绕过（RobustMQ 这种活跃项目对 PR 质量有硬要求）。

### 8.4 场景三收尾

跑完场景三，第二个 PR 提了。耗时 1-1.5 小时。

## 9. 场景四 以 RobustMQ 为例，提一个高质量 issue

<img src="imgs/aicmigr-32-opensource-04-process-recap/a57650f483342eb6499ef2cd2224ed55_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应系列第 31 篇后半段，产出：第一个被 RobustMQ 维护者认真讨论的 issue。

第二个 PR 提了之后，接下来要做的不是马上提第三个 PR，而是提一个高质量 issue。原因有两个：

第一，issue 是比 PR 更难的开源贡献。PR 是修好了这个问题，issue 是发现了一个值得讨论的问题。后者需要工程师对项目有判断力，知道什么是真问题、什么是噪音。

第二，issue 是跟 Maintainer 建立连接的更好入口。一个高质量 issue 会吸引 Maintainer 来讨论，这种讨论比 PR review 的对话深度高得多。系列第 30 篇讲过"跟核心维护者建立连接"是开源最被低估的收益，issue 就是建立这种连接的最好方式。

### 9.1 第一步：扫 RobustMQ 找性能问题

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

#### (2) review 重点：挑算法层面有改进的那一个

挑算法层面有改进的那一个，不是挑最简单的。"多余的 collect"、"多余的 clone"那种 1 行就能改的代码风格问题，Maintainer 反应通常是"直接提 PR 删一行就行，提 issue 干嘛"。算法层面有改进、修复需要讨论方向、需要测试验证的问题，才值得走 issue 流程。

### 9.2 第二步：让 AI 写一个高质量 RobustMQ issue

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

#### (2) review 重点：最后一条是高质量 issue 的标志

最后一条"我可以跟 PR"是高质量 issue 的标志。它告诉 Maintainer 工程师不是只想刷 contribution 数字，愿意接着把这件事做完。很多时候 Maintainer 看到这一句就会主动来 review 和讨论。

### 9.3 场景四收尾

跑完场景四，第一个高质量 issue 也提了。耗时 30-45 分钟。

到这里，第一个 PR + 第二个 PR + 第一个 issue 全部跑完。GitHub 个人页面上有了 RobustMQ 这个项目的真实贡献痕迹。

## 10. 一键跑完整流程：让 Claude Code 自主执行

前面四个场景一个个跑，是为了让工程师看清每一步的产出和 review 点。真正上手之后，工程师会希望一次粘贴、Claude Code 自主跑完整流程、关键决策点停下来等工程师判断。

### 10.1 一键流程的边界

注意：一键流程不管 fork 这一步（GitHub 账号操作 AI 做不了），也不管挑哪个项目和挑哪个 issue——这两个决策必须人来挑。它从已经选好项目，并 fork + clone 完这一步开始。

下面这段提示词的默认项目是 RobustMQ，也可以换成 AI 推荐出来的任何项目。

### 10.2 一键流程提示词

#### (1) 提示词原文

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

### 10.3 提示词设计的四个 why

#### (1) 顺序里第一个 PR 期间并行做第二步筛 issue

第一个 PR 提了之后，等 review 通常要几小时到一天，这段空窗期最适合找下一件事。AI 不会主动并行做事，工程师要在提示词里显式告诉它什么时候开始下一步。

#### (2) issue 的选择和修复方向显式让 AI 停下来等人

这两件事 AI 替工程师拍是最危险的——挑了一个写完才发现是个规模超预期的 issue，或者修复方向跟项目方向不一致，几小时白白浪费。

#### (3) lint warning 不留 allow 注解

这是开源协作的硬要求。新人最容易在这里偷懒，提示词里必须显式禁掉。

#### (4) "超 1.5 倍预估时间停下来报告"

这是给 AI 装的护栏。AI 容易陷在某个细节上反复打磨，工程师要在提示词里告诉它"超时就停下来让我看"，而不是让它一直跑直到上下文耗尽。

## 11. 经验分享：那些动作之外的事

到这里实操部分就结束了。但开源做久了，会发现真正决定能走多远的不是这些动作，是动作之外的几件事。这部分把这套系列里学不到的东西补给工程师。

### 11.1 选项目还有 AI 推荐之外的隐性信号

<img src="imgs/aicmigr-32-opensource-04-process-recap/732577fbfac353ce71248b43364c94d5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

系列第 30 篇三道题筛出来是候选，但要进一步看三个东西。

#### (1) 三条隐性信号

**PR 列表里 review 速度。** 超过一周的项目对新人不友好。

**维护者构成。** 只有 1-2 个核心维护者，工程师的 PR 完全靠他们的状态决定迭代速度。

**最近 6 个月新增 contributor 数。** 新人持续进来意味着 onboarding 路径已经被走通过，工程师不是第一个吃螃蟹。

#### (2) 三条信号合起来能筛掉僵尸项目

这三条信号合起来，能筛掉那种"星光闪闪但已经停滞"的僵尸项目。这些项目最坑新人，因为它们看起来活，实际上没人在 review。

### 11.2 PR 被 close 了别憋着

第一次 PR 被 close 时心里很不舒服，觉得 Maintainer 不识货。后来才明白，PR 被 close 是开源里最常见的事，跟工程师写得好不好没关系，跟项目当下状态有关系。

#### (1) 被 close 之后做三件事

**礼貌问原因。** Could you help me understand which part is out of scope?

**把代码留在自己 fork 里。** 可能哪天项目方向变了就用上了。

**换一个 issue 继续。** 别把 PR 被 close 当成对工程师能力的判决，开源是协作不是考试。

### 11.3 长期主义的复利

<img src="imgs/aicmigr-32-opensource-04-process-recap/70dee8a0d6b04341c59409438749bc19_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

系列第 30 篇讲过 Kafka 那段经历：提了两个 PR 就停了，跟同期开始的另一个人坚持了四五年成了 Kafka 的 PMC。那段差距想了很久，到底差在什么动作上。不差在技术，差在三件事。

#### (1) 差距在三件事

**持续在那个项目上跑。** 每周都有 commit，即使是文档级的小 commit，频次比单次质量更重要。

**参与 review 别人的 PR。** 即使还不是 Committer，review 别人是无声的能力证明，Maintainer 会逐渐把工程师当作自己人。

**在 mailing list 和 design doc 讨论里发声。** 即使有时只是问澄清性问题，这件事让工程师从写代码的工程师变成项目的 stakeholder。

#### (2) 时间线

把这三件事加起来，从 Contributor 到 Committer 1-2 年，从 Committer 到 PMC 3-5 年。差距不在某一个 PR 写得多牛，在工程师愿不愿意把开源当工作的一部分。

### 11.4 AI 时代的新红利和新门槛

AI 让"开始"这件事变容易了，这是系列第 30 篇讲的红利。但还有一个更隐性的变化——AI 让中等技术含量的 PR 数量爆炸，Maintainer 的注意力反而更稀缺。三年前一个写得清楚的 bug fix PR 提上去，Maintainer 多半会顺手 review。今天同样质量的 PR 排在一堆 AI 辅助生成的 PR 后面，review 速度反而慢了。

#### (1) 新人门槛反而高了

这意味着新人想脱颖而出，门槛反而高了。光会用 AI 提 PR 不够，工程师得展示 AI 替代不了的东西，包括：对项目方向的理解，对 trade-off 的判断，跟 Maintainer 沟通的体感。AI 让代码贬值，但对项目的判断力在升值。

#### (2) AI 时代的正确工作姿势

写代码这一段大量用 AI（系列第 30、31 篇那些工作流每天都在跑），但 review、讨论、长期跟踪 issue 这些动作工程师自己做。把省下来的写代码时间投到这些"人才能做的事"上。这才是 AI 时代工程师贡献开源的正确姿势：用 AI 加速产出，把时间投在 AI 替代不了的协作和判断上。

## 12. 小结

<img src="imgs/aicmigr-32-opensource-04-process-recap/eddb00ce4e7148cbeb8c35a80c955453_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第七部分到这里结束。系列第 30 篇第一个 PR 的心理胜利，第 31 篇第二个 PR + 第一个 issue 的真本事，本篇把动作打包成可反复用的清单 + 经验分享。整套挑战开源的方法论全部跑完。

### 12.1 整套系列的方法论回顾

一起回顾下前面的内容。第一部分到第七部分，讲的是同一件事：AI 时代怎么做老项目改造。包含公司内的老项目、基于开源做需求、挑战开源给别人的项目贡献代码，这三种形式背后是同一套方法论：读懂陌生代码、找到改造点、用 AI 高效产出、保住质量。

这套方法论的核心不是某个具体项目的技巧，是 AI 时代工程师该有的工作姿势。写代码这件事 AI 让它贬值，但读代码、判断方向、跟人协作，这些事 AI 让它升值。整套系列所有的动作都在训练后一类能力。

### 12.2 一句话总结

如果工程师只能从这部分记一句话，记这一句：开源不是技术比赛，是耐心比赛。AI 时代，这条更对。

### 12.3 三个月后的回看

挑一个感兴趣的项目，按本篇的清单跑通第一轮，然后每周回来一次，每次只做一个小改动。频次大于单次重量。3 个月后回头看，GitHub 个人页面会变成另一个样子——不是因为做了什么了不起的事，只是因为没停下来。

## 13. 思考

### 13.1 思考一

跑完整套流程大约花了多少时间？最卡工程师的是哪一步——筛项目、筛 issue、实现 PR、扫漏洞、还是写 issue？

### 13.2 思考二

如果让工程师给一个还没开始做开源的工程师推荐一件最值得做的事，会推荐什么？
