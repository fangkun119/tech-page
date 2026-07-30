---
title: 传统项目迁AI 05：学习方法 - 2026业界进展
author: fangkun119
date: 2026-07-04 05:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-05-approach-05-industry-progress/cover.jpg
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
aicmigr-05-approach-05-industry-progress
传统项目迁AI 05：学习方法 - 2026业界进展
-->

## 1. 老项目 + AI：为什么越用越踩坑

<img src="imgs/aicmigr-05-approach-05-industry-progress/431fe3a1727f2b91b17f58be2af5573a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

从通用大模型到各种 Agent 框架，每次更新都号称"编程能力再上一个台阶"。可反直觉的事发生了：在老项目改造这个场景里，**模型越强，踩坑越频繁**——AI 自信地给出方案，合进去架构就乱；让它读十年前的代码，解释跑偏得离谱；改完编译通过，线上出了"沉默的 bug"。

问题不在 AI，在用法。老项目这个场景本身对 AI 不友好，<span style="color: red; font-weight: bold;">模型越强，产出越快，问题反而越尖锐。</span>这是一个结构性问题，不是"等模型再强一代"能解决的。

### 1.1 一把结构性的剪刀差

先用传统软件工程的经验做个类比。过去工程师自己写代码、自己 review、自己调试，"写的速度"和"理解的速度"是同一个速度——你写 100 行，自然理解 100 行。AI 介入后，这两件事被劈开了：<span style="color: red; font-weight: bold;">AI 负责写，人负责理解和验证</span>。

在"绿地项目"（从零开始的新项目）上，这个劈开还能接受：代码是新写的，架构清晰，AI 出错容易看出来。一旦放到"棕地项目"（有十年历史包袱的老项目）上，问题立刻被放大——AI 写得越快，人理解/验证越跟不上；老代码本来就难读，AI 还在不断往里堆新代码。

这就是开篇说的结构性问题：<span style="color: red; font-weight: bold;">AI 产出速度和人的理解/验证速度，形成了一把剪刀差。老项目放大了这把剪刀差。模型越强，剪刀张得越大。</span>

你可能会问：模型再强一代不就好了？答案是不会——剪刀差的两条腿不等长，模型越强，产出那条腿迈得越快，剪刀只会张得越大。

业界把这把剪刀差拆成了三个具体的"债"，每一笔都对应着 AI 在老项目改造里的一种典型踩坑。三个债不是孤立的三个名词，而是同一把剪刀差的三个切面。

### 1.2 三个债：剪刀差的三个切面

<img src="imgs/aicmigr-05-approach-05-industry-progress/7b68eb54bf402c7d938a9829329c41ea_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

学术界、大厂、咨询公司、安全厂商从不同角度都观察到了同一个现象。综合下来，老项目改造里 AI 踩坑的原因可以归为三类债：<span style="color: red; font-weight: bold;">理解债、棕地税、验证债</span>。下面逐一拆解。

#### (1) 理解债 Comprehension Debt

你让 AI 写了 1000 行代码，却只有时间真正理解其中 100 行——剩下 900 行在代码库里，却不在你的脑子里。短期看不出问题，等要 debug、要改造、要对接新需求时，你回头读自己的代码库就像在读别人写的代码。

这就是**理解债**（Comprehension Debt），由 Google 的 Addy Osmani 提出。他的原话大意是：AI 帮工程师写代码的速度，和工程师真正理解这些代码的速度，正在快速拉开差距。工程师过去自己写 100 行，对这 100 行是熟的；现在 AI 帮工程师生成 1000 行，工程师还是只有时间理解那 100 行。剩下的 900 行欠着。

Anthropic 自己做过一个 **52 人的随机对照实验**验证这件事：用 AI 辅助的那组，在代码理解测试上比对照组**低 17%**，其中 debugging 维度差距最大。<span style="color: red; font-weight: bold;">越是用 AI 的人，越不擅长理解和调试自己"写"出来的代码</span>。

老项目本来就欠了十年的理解债，AI 一边帮忙改、一边继续往里堆新债。如果没有系统的方法把 AI 的产出"理解进来"，这笔债永远还不清。对抗理解债需要把 AI 产出持久化、被后续 session 和工程师复用，后续章节会展开的工具如 CLAUDE.md、SKILL.md 就是为此而生。

#### (2) 棕地税 Brownfield Tax

把一个十年老项目丢给 AI，它表现不好不是因为笨，而是因为这个项目本身在向它征一笔"税"。这笔税有个正式名字，叫<span style="color: red;">棕地税</span>（Brownfield Tax），来自佛罗里达国际大学（Florida International University，FIU）的研究。研究者专门观察 AI 在有历史包袱项目里的表现，总结出五种反复出现的现象——每种都是一笔要交的税：

| 现象                        | 一句话含义                                                                                    | 对应打法                                                            |
| ------------------------- | ---------------------------------------------------------------------------------------- | --------------------------------------------------------------- |
| Dumb Zone                 | <span style="color: red; font-weight: bold;">context 使用率</span>超过 40%，AI 输出质量开始下降        | 上下文管理技巧、压缩和蒸馏                                                   |
| Cross-session Forgetting  | 每开一个新对话，前面辛苦教的东西全忘了                                                                      | CLAUDE.md 持久化                                                   |
| Context-blind suggestions | AI 给出<span style="color: red; font-weight: bold;">"更现代"但和现有架构完全不兼容</span>的方案             | MCP 接入历史数据                                                      |
| Translation Tax           | <span style="color: red; font-weight: bold;">senior 开发者花大量时间纠正 AI 的 naive 建议，反而变慢</span> | SKILL.md 固化流程                                                   |
| Context Overflow          | 代码分布在几十个文件，全喂爆 context，不喂又看不见全貌                                                          | <span style="color: red; font-weight: bold;">Context Map</span> |

拆开看：Dumb Zone 讲容量——老项目随便塞点代码和文档，context 很容易突破 40%，AI 进入"降智区"；Cross-session Forgetting 讲记忆——AI 每开一个新对话就重置，跨不过 session 这道坎；Context-blind suggestions 讲上下文失明——AI 不知道代码为什么写成现在这样，方案再"现代"也用不了；Translation Tax 讲反向负担——经验越丰富的开发者，被 AI 的 naive 建议浪费的时间越多；Context Overflow 讲规模——老项目的依赖分布在几十个文件里，喂多了爆，喂少了瞎。

五种现象合起来，就是 FIU 命名的 Brownfield Tax——老项目向 AI 征的"税"。注意右边那列打法不是凑出来的，是业界看到问题之后被逼出来的答案，每一个都对应一种具体的工具或方法论，这是后续章节会展开的内容。

#### (3) 验证债 Verification Debt

<img src="imgs/aicmigr-05-approach-05-industry-progress/443cd331556940890bb8d7ec97a6f0be_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">你有一半代码是 AI 写的，但其中只有不到一半被认真 review 过——中间这道"产出但没验证"的缺口，就是验证债</span>。Sonar 的 State of Code Developer Survey 给出一组扎眼的数据：

```
42% 的代码是 AI 辅助生成的
96% 的开发者不完全信任 AI 的输出
只有 48% 每次都 review AI 生成的代码
```

一半的代码是 AI 写的，一半没被认真 review。中间的 gap 就是**验证债**（Verification Debt）。

Veracode 2025 年的报告更狠：**45% 的 AI 生成代码引入了安全漏洞**。几乎一半。Ox Security 给这个现象起了一个特别形象的名字——**Army of Juniors**（实习生大军）：AI 产出"功能性极高，但系统性地缺乏架构判断力"。就像突然招了一千个实习生，每一个都能干活、能写代码，但没有一个能对整体架构负责。单点都没错，合起来却可能错。

这意味着工程师不能靠人肉 review 兜底——人肉 review 根本追不上 AI 产出的速度，必须有<span style="color: red; font-weight: bold;">工具兜底验证</span>。<span style="color: red; font-weight: bold;">Characterization Tests、跨模型 review、CI 门禁这些做法</span>，不是因为"最佳实践"才推荐，而是验证债逼出来的刚需。

#### (4) 三个债背后是同一件事

<span style="color: red; font-weight: bold;">三个债看起来是三件事，其实是同一件事的三个侧面。</span>

<img src="imgs/aicmigr-05-approach-05-industry-progress/5352a56fc81fd0a863b93e00f9d1b169_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-05-approach-05-industry-progress/5352a56fc81fd0a863b93e00f9d1b169_MD5.jpg
用途：直观展示 AI 代码产出速度与人类理解/验证能力之间日益扩大的差距，老项目放大了这一结构性矛盾
内容：两条曲线/两条腿对比——AI 产出速度曲线快速上升在前，人类理解与验证能力曲线缓慢爬升在后，三个债（理解债/棕地税/验证债）正是这一差距被老项目放大后的三种表现
-->

如图所示：AI 的写代码速度跑在前面，人的理解和验证能力在后面追，老项目放大了这个差距。理解债讲的是"追不上的理解"，棕地税讲的是"老项目让追得更吃力"，验证债讲的是"追不上的验证"——三者都是同一把剪刀差的不同切面。

这是一个结构性问题，不是"模型再变强"就能解决的。恰恰相反：**模型越强，产出越快，差距越大。**<span style="color: red; font-weight: bold;">业界的解法不是让模型变弱，而是给人配上一套能追上 AI 产出速度的方法论——这才是这一系列文章真正要讲的东西。</span><span style="color: red; font-weight: bold;">AI 不会变弱，但工程师可以补上方法论这条腿。</span>

### 1.3 三个反模式：动手前的红灯

讲完三个债，还有一个外部视角的补充。Cleveraud 是一家做了 15 年的咨询公司，他们在 **2026 年 3 月**的报告里明确指出，老项目 AI 改造有三个反复出现的失败模式，工程师动手前必须先自检：

```
三个失败模式反复出现：
- 试图一次性改造整个系统
- 在翻译过程中丢失嵌入的业务逻辑
- 技能鸿沟没有团队能独立跨越
```

把这三个反模式和前面讲的三个债、五种税对照看，会发现一件有意思的事：它们互为镜像。

第一个反模式——"试图一次性改造整个系统"，正好对应棕地税里的 Context Overflow 和 Context-blind suggestions。一次性丢给 AI 整个系统，既会爆 context，又会得到一堆和架构不兼容的方案。**反向规避**：<span style="color: red; font-weight: bold;">渐进式迁移，把改造拆成可回滚的小步</span>。

第二个反模式——"在翻译过程中丢失嵌入的业务逻辑"，对应 Context-blind suggestions。AI 读不懂"这条计费规则是 2009 年某次监管审计之后加上去的"这种代码之外的 context，翻译过程中自然就丢了。**反向规避**：<span style="color: red; font-weight: bold;">通过 MCP 把历史决策、监管背景、业务规则接入到 AI 的可见范围里，让 AI 看见"为什么写成现在这样"</span>。

第三个反模式——"技能鸿沟没有团队能独立跨越"，对应 Translation Tax。senior 开发者被 AI 的 naive 建议浪费时间，本质是团队没有把"怎么正确地用 AI 改老项目"固化下来。**反向规避**：<span style="color: red; font-weight: bold;">把流程沉淀成 SKILL.md（Custom Project Commands），让每个模块被处理的方式一致，让经验不在人身上</span>。

<span style="color: red; font-weight: bold;">三个反模式不是孤立的坑，它们是三个债的"放大版"——一旦踩进去，对应的债会指数级恶化。</span>后续章节给出的 Check List，每一条都在反向规避这三个反模式。

到这里应该能感受到：用 AI 改老项目踩的坑，不是 AI 不行，而是老项目这个场景对 AI 不友好，加上用法不对。三个债是诊断，三个反模式是红灯。接下来要回答的问题是：业界怎么解？

## 2. 二十年前的方法论为何成了 AI 时代的刚需

<img src="imgs/aicmigr-05-approach-05-industry-progress/7a6afcfbf3ecf86aa27dcc0d3e681cfd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 一本 2004 年的"圣经"重新火起来

#### (1) 书籍基本信息

<span style="color: red; font-weight: bold;">Working Effectively with Legacy Code</span>，作者 Michael Feathers，2004 年出版。它是遗留代码改造领域的"圣经"。2020 年之前，主要读者还只是那些不得不维护老系统的工程师。

#### (2) 在 2024-2026 年复兴的原因

从 2024 年起，这本书的讨论量、引用量开始显著上升。几乎所有严肃讨论"AI + legacy code"的文章都会引用它。原因很直接：Feathers 20 年前提出的两个核心概念——Characterization Tests 和 Seam——在 AI 时代重新成了刚需。20 年前解决"人改老系统"的方法，正好精准对症"AI 改老系统"的新困境。

为什么这本老书的方法能解 AI 时代的困境？因为 AI 改代码暴露的问题，本质和 20 年前遗留代码暴露的问题一样，而 Feathers 的两把武器正是传统工程师熟悉的"回归基线"和"解耦点"。下面两节先建立锚点。

### 2.2 Characterization Tests：锁住"现在的行为"

#### (1) 用回归基线做类比讲定义

传统工程师熟悉的"回归基线"（Regression Baseline），目的是锁住既有行为，让后续改动一旦碰坏现有逻辑就能被测试抓住。Characterization Tests 就是同一类东西——给老系统写一份"行为契约"，只有一处关键差别：

- <span style="color: red; font-weight: bold;">回归基线锁</span>的是**预期行为**——断言系统"应该"做什么。
- <span style="color: red; font-weight: bold;">Characterization Tests</span> 锁的是**现有行为**——记录系统"现在实际"在做什么，不预设对错。

对一份没有测试覆盖的遗留代码，你常常连它现在到底在干嘛都说不清，更别说对错。所以第一步不是写"正确性测试"，而是机械地把当前行为固化下来。

<span style="color: red; font-weight: bold;">术语定义：测试代码"现在实际"做什么，不是"应该"做什么。</span>

#### (2) 5 步土法流程

```
1. 把代码放进测试框架
2. 写一条你知道会失败的断言
3. 让失败告诉你真实行为
4. 再把断言改成和真实行为一致
5. 测试通过，这就是你的"行为基线"
```

<span style="color: red; font-weight: bold;">不预设、不猜，让代码自己告诉你它现在在干嘛</span>。跑通的那条断言，就是一份机械的、可回归的行为契约。

#### (3) 为什么 AI 时代变成刚需

为什么不直接写正常测试？因为 AI 改代码的速度远远超过人 review 的速度，靠人肉 review 确认 AI 没改坏，根本来不及。工程师必须有一个外部的、机械的、可回归的契约来兜底——Characterization Tests 就是这份契约。

更深一层：正常测试锁的是"应该"，改造前往往根本不存在；<span style="color: red; font-weight: bold;">Characterization Tests 锁的是"现状"，改造前永远存在，且正好是 AI 改造要保护的对象</span>。

#### (4) 沉默的行为偏移

CodeGeeks Solutions 2026 年初的报告里有段话特别扎心：

> 这是 AI refactoring 领域最被低估的实践之一，因为<span style="color: red; font-weight: bold;">它降低了 AI 的最大风险：沉默的行为偏移。</span>

"沉默的行为偏移"（silent behavioral drift）指的是：AI 改完代码，测试跑通了、diff 看起来干净，但在<span style="color: red; font-weight: bold;">某个没测到的路径上行为已经悄悄变了</span>，工程师发现不了。等到问题暴露，已经叠了好几层改动，回溯成本极高。Characterization Tests 就是对抗它的方法——先锁住"现在的行为"，AI 再改，一跑就知道有没有改坏。

### 2.3 Seam：让改造可被隔离

#### (1) 用依赖注入"抽接口"做类比讲定义

传统工程师做依赖注入时，有一步叫"抽接口"：你不在原地改实现，而是先抽出一个接口，让真实依赖可被替换。Seam 就是这种缝隙——一个让代码改造可以被隔离的接缝，你不在这里编辑代码，却能在运行时替换它的行为。

<span style="color: red; font-weight: bold;">术语定义：程序里一个能改变行为、但不需要在那个位置编辑代码的地方。</span>

#### (2) 制造 Seam 的典型操作

下面这些动作，每一件都是在制造 Seam：

- <span style="color: red;">把直接 new 的依赖抽成一个可覆写方法</span>
- <span style="color: red;">把硬编码的配置抽成注入</span>
- <span style="color: red;">把静态调用换成接口</span>

手法都很传统，但<span style="color: red; font-weight: bold;">每制造一个 Seam，就多一个"爆炸半径可控"的改造入口</span>。

#### (3) 为什么对 AI 改造重要

AI 改一段没有 Seam 的代码，风险远远高于有 Seam 的代码。Seam 让影响范围可预测——<span style="color: red; font-weight: bold;">改造被关在一个可替换的缝里</span>，AI 出错时爆炸半径小、<span style="color: red; font-weight: bold;">可回滚的边界清晰</span>。没有 Seam 的代码，AI 一动就是牵一发动全身，改完谁也不敢说没出事。

#### (4) Augment Code 报告的三步

Augment Code 2026 年的报告把这套打法讲得很完整：

```
先 Characterization Test 锁行为
再 Seam 做隔离
再 Refactor 做改造
```

三步是一条流水线：<span style="color: red; font-weight: bold;">先用测试锁住现在的行为，再造一个 Seam 把改造隔离起来，最后在隔离区里安全地重构。</span>两个概念配合起来用，才是 Feathers 方法论的完整形态。

### 2.4 为什么"慢改"武器在"快改"时代成了唯一护栏

2026 反而更需要这本老书，原因很简单——AI 改代码暴露的问题，和 20 年前遗留代码暴露的问题，本质上是同一个：

都是<span style="color: red; font-weight: bold;">"怎么改一个你不完全理解的系统"</span>。

方法没变，只是执行者从人变成了 AI + 人。AI 把改代码的速度拉爆了，但没有把"理解系统"和"安全验证"这两件事的速度拉爆。于是 Feathers 20 年前为"慢改"设计的两把武器——<span style="color: red; font-weight: bold;">先锁行为、再隔离改造</span>——在"快改"时代反而成了唯一的护栏。

这两个概念是后续方法论的两根支柱，Feathers 的原书是权威来源。

## 3. 业界的共同骨架：理解 → 改造 → 验证

<img src="imgs/aicmigr-05-approach-05-industry-progress/bc3e6b9e98cc6935aa7249a5a2049560_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把 2025-2026 年的学术、大厂、咨询、开源四路证据并排放在一起，会发现一件事：所有认真做这件事的人，都在收敛到同一个骨架——**理解 → 改造 → 验证**。<span style="color: red; font-weight: bold;">这不是某家公司的市场话术，是被现实逼出来的答案。</span>

### 3.1 三段式骨架速查

三段各自在做什么：

- **理解**，搞清楚项目现在到底在干嘛——不是"应该"干嘛，而是<span style="color: red; font-weight: bold;">理解"实际"在干嘛</span>。
- **改造**，在不弄坏现有行为的前提下演进代码，让<span style="color: red; font-weight: bold;">爆炸半径可控、随时能回滚</span>。
- **验证**，改完之后<span style="color: red; font-weight: bold;">用机械化的方式确认行为没变</span>，不靠人肉 review 兜底。

三个段的关注点、目标、典型动作如下：

| 段   | 关注点            | 目标              | 典型动作                                       |
| --- | -------------- | --------------- | ------------------------------------------ |
| 理解  | 项目当下实际在做什么     | 建立准确的行为基线       | Context Map、Seam 识别、Characterization Tests |
| 改造  | 在不破坏基线的前提下演进代码 | 控制爆炸半径、保留可回滚    | 渐进式迁移、SKILL.md 固化流程、MCP 接入历史数据             |
| 验证  | 改完之后行为是否仍然等价   | 不依赖人肉 review 兜底 | 编译门禁、跨模型 review、CI 强制回归                    |

### 3.2 四路证据如何殊途同归

学术、大厂、咨询、开源四个独立来源，分别从不同角度走到了同一个骨架。先用一张总表锚定：

| 来源 | 代表动作 | 对应骨架段 |
|------|---------|-----------|
| 学术 | Chain of Understanding 论文（ICPC 2026）、代码知识图谱 | 理解 |
| 大厂 | Anthropic Code Modernization Starter Kit（2026年3月） | 理解 + 改造 + 验证 |
| 咨询 | Thoughtworks CodeConcise + Multi-pass Enrichment、Cleveraud 报告 | 理解 + 改造 |
| 开源 | Aider（git 可回滚）、Cline（透明执行）、Continue（多模型）、Goose（toolkit） | 改造 + 验证 |

#### (1) 学术：让 AI 像专家一样读代码

<img src="imgs/aicmigr-05-approach-05-industry-progress/0db8a451d5e7dde23c75656b30298ae2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

学术界最值得关注的一篇论文是 **Chain of Understanding**，2025 年 4 月发在 arXiv（编号 arXiv:2504.04553），2026 年 4 月在 ICPC 2026（程序理解领域的顶级会议）正式发表。作者找了 **8 位代码审计专家**，问他们怎么读陌生代码库，结论高度一致——专家都按同一条链走：

```
全局理解：先把代码库当系统看（做什么、几个模块、数据怎么流）
局部理解：再挑具体模块看内部逻辑
关系理解：再回到系统层看这个模块和其他部分的关系
```

这条链是**螺旋上升**的，反复几次理解才算建立，和传统软件工程里"先看架构图、再读核心模块、再回头看依赖关系"的老经验完全一致。

作者基于这条链做了工具 <span style="color: red; font-weight: bold;">CodeMap</span>，用户实验结果是：用 CodeMap 的人对 LLM 回答的依赖降低了 **79%**。把"理解链"显式做出来，AI 的回答就不再需要工程师反复追问和纠偏。另一个学术方向是<span style="color: red; font-weight: bold;">代码知识图谱</span>：把代码解析成图（节点是类、函数、模块，边是调用、继承、依赖），AI 基于图查询而不是搜文本，精准命中、不超载。**Thoughtworks Technology Radar 2026** 把这个方向推荐为值得采纳的实践。

#### (2) 大厂：Anthropic 把老项目改造标准化

**2026年3月**，Anthropic 在 **$100M Claude Partner Network** 下推出了 **Code Modernization Starter Kit**。它不是一个工具，而是一套工作流模板——把和企业客户合作改造老项目的经验打包成标准化资产，客户拿到手可以直接用。核心结构是三阶段：

```
代码库分析 → 渐进式迁移 → 等价性验证
```

把这三个词和理解、改造、验证三段对照看：

| Anthropic Starter Kit | 本文对应层 |
|----------------------|-------------|
| 代码库分析 | 理解层 |
| 渐进式迁移 | 约束层 |
| 等价性验证 | 验证层 |

高度一致——一边是 Claude 官方从企业项目里总结的打法，一边是从学术、经典书、实战经验里归纳的方法论，结论指向同一个方向。

Anthropic 在官方文档里明确说：**CLAUDE.md** 承担"持久化项目记忆"的角色，把业务规则、边界情况、架构决策写进去，让 context 能跨 session、跨工程师传递。用传统软件工程类比：CLAUDE.md ≈ 架构决策记录（ADR）+ 项目交接文档。新人接手项目时，过去靠口头交接和散落的 wiki，现在靠一份机器和人都能读的项目记忆。**Custom Project Commands**（即 **SKILL.md**）则是把改造方法论编码成可复用脚本，保证<span style="color: red; font-weight: bold;">每个模块被处理的方式一致</span>。类比一下：它 ≈ 可复用的 <span style="color: red; font-weight: bold;">SOP</span> 脚本，把"<span style="color: red; font-weight: bold;">这块代码要先跑 Characterization Tests、再抽 Seam、再迁移</span>"这种流程固化下来。

#### (3) 咨询：Thoughtworks 与 Cleveraud 的判断

<img src="imgs/aicmigr-05-approach-05-industry-progress/3b71df361e3bf03544c5662645c7f4b0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Thoughtworks 的内部工具 <span style="color: red; font-weight: bold;">CodeConcise</span> 用知识图谱做 COBOL 等老系统的逆向工程，他们 2025 年底公布的数据：用 AI + 知识图谱做 COBOL 老系统逆向，时间比传统方法减少 **66%**。核心方法论叫 **Multi-pass Enrichment**（多轮富化）——一轮一轮给代码图加料：

```
先拿 AST 抽结构
再让 LLM 补语义
再注入业务知识
再交叉验证
```

直觉上 AI 读懂代码像是"一次性看明白"，但咨询公司经验告诉我们：<span style="color: red; font-weight: bold;">理解是长出来的，不是一次性读出来的。</span>每一轮把上一轮没看到的补回来，结构先抽出来、语义再补上、业务知识再注入、最后交叉验证防止编造。这和学术界的螺旋链本质是一回事。

**Cleveraud** 是一家做了 **15年** 的咨询公司，他们 2026 年 3 月发的报告里有几段判断讲得很透，值得原样引用：

```
AI 把数周的代码库分析压缩到几天。
但这种压缩不是捷径，而是让人类判断能从实际知识出发而不是假设。
```

这是第一层判断：**AI 做的是"压缩分析"，不是"替代判断"**。压缩让工程师从真实代码出发做决策，而不是凭经验拍脑袋。

```
架构决策、业务规则背后的监管解读，这些 AI 无法从代码里推断。
没有 AI 能读一条计费规则就知道这是 2009 年某地区监管审计之后加上去的。
这种 context 只存在于你的领域专家脑子里。
```

这是第二层判断：**代码之外的 context 永远靠人**。代码里能看到"是什么"，看不到"为什么"——为什么这段计费逻辑这么写、为什么这个字段叫这个名字、为什么这里有个看似冗余的判断。这些答案藏在领域专家脑子里、藏在审计报告里、藏在 Jira 工单里。三句话把老项目改造的核心判断讲清楚了：不要一次性改造、AI 做的是压缩分析不是替代判断、代码之外的 context 永远要靠人。

#### (4) 开源：社区在验证不同可能性

2024-2026 年冒出了一批优秀的开源 AI 编程工具，和闭源产品走不一样的路线。四个代表：

| 工具           | 特点                                    | 对老项目改造的价值             |
| ------------ | ------------------------------------- | --------------------- |
| **Aider**    | 完全基于 git 工作流，每次改动自动 commit，失败随时 reset | 永远有保险，可回滚             |
| **Cline**    | VSCode 插件，每步 plan/action/result 都透明显示 | 可看着它思考再决定执行           |
| **Continue** | 支持多模型 backend，可混用 Claude 和 GPT        | 不被单一模型锁死              |
| **Goose**    | Block 开源的 Agent 框架，核心是 toolkit 机制     | 可自定义 toolkit 完成特定领域任务 |

这几个工具不是让读者都去用，而是让读者知道：当闭源产品在定义方向时，<span style="color: red; font-weight: bold;">开源社区在验证方向的多种可能性。</span>**Aider** 验证了"永远可回滚"的价值——对老项目改造尤其重要，改坏了能立刻撤回；**Cline** 验证了"透明执行"的价值——AI 每一步在干什么看得清清楚楚，工程师能基于完整信息做判断；**Continue** 验证了"多模型混用"的价值——不被单一供应商锁死，不同模型在不同任务上各有优势；**Goose** 验证了"可定制 toolkit"的价值——特定领域任务可以让 Agent 带上专用工具集。这些设计哲学会反向影响闭源产品的演进，今天开源验证有效的东西，明天很可能就成了闭源产品的默认特性。

### 3.3 殊途同归

<img src="imgs/aicmigr-05-approach-05-industry-progress/66dcece0ea7d28079fc6c8adb08bb7d4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

扫完四路，会发现一个规律：所有主流实践都在收敛到一个共同骨架——**理解 → 改造 → 验证**，三段式。学术界研究专家怎么读代码，结论是 Chain of Understanding 三层螺旋；大厂把企业改造经验打包成 Starter Kit，结构是代码库分析 → 渐进式迁移 → 等价性验证；咨询公司用知识图谱和领域专家经验做逆向，方法论是 Multi-pass Enrichment；开源社区用 git 可回滚、透明执行、多模型、toolkit 在改造和验证上各展所长。四路名字不一样，骨架一致。

<img src="imgs/aicmigr-05-approach-05-industry-progress/ec486530571abdf7fe7a90734eb98c3d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-05-approach-05-industry-progress/ec486530571abdf7fe7a90734eb98c3d_MD5.jpg
用途：展示学术、大厂、咨询、开源四路独立实践如何殊途同归收敛到"理解→改造→验证"三段式骨架
内容：四路实践（学术 Chain of Understanding/知识图谱、Anthropic Starter Kit、Thoughtworks/Cleveraud 咨询方法论、Aider/Cline/Continue/Goose 等开源工具）汇聚到下方共同的"理解→改造→验证"流程图
-->

被现实逼出来的事，所有严肃从业者都会得出同样的结论。<span style="color: red; font-weight: bold;">这是一个领域走向成熟的标志。</span>

### 3.4 方法论地图与四点核心提炼（收束）

读到这里，读者会积累很多具体方法、工具、流程。<span style="color: red; font-weight: bold;">方法一多，难免会冒出一个疑问：这些到底是业界共识，还是作者的个人套路？</span>把前面四路证据再压缩一遍，四源头清清楚楚：

| 源头 | 落地提示 |
|------|---------|
| ICPC 2026 的 Chain of Understanding 论文 | 全局→局部→关系的理解链，对应 Context Map 与 Seam 识别 |
| Anthropic 官方 Starter Kit | 代码库分析→渐进式迁移→等价性验证，对应理解层、约束层、验证层 |
| Thoughtworks 15 年咨询经验 | CodeConcise + Multi-pass Enrichment，对应"理解是长出来的" |
| Feathers 20 年前的经典 | Characterization Tests 与 Seam，对应锁行为基线与隔离改造 |

<span style="color: red; font-weight: bold;">这四个源头都会以某种形式见到落地。</span>

可以把这张地图当作索引：每接触一个新方法，回头对照它落在哪一块。地图标的是"业界已经走过的路"，不是某个人凭空想出来的套路。

最后用四点收束全文。第一，业界看到的三个债——Comprehension Debt（理解债）、Brownfield Tax（棕地税）、Verification Debt（验证债）。三个债本质是同一件事：AI 产出速度远超人的理解和验证能力，老项目放大了这个差距。模型越强，产出越快，差距越大，这不是模型能自己解决的问题。第二，业界收敛的共同骨架——理解 → 改造 → 验证。学术（Chain of Understanding、知识图谱）、大厂（Anthropic Starter Kit）、咨询（Thoughtworks、Cleveraud）、开源（Aider、Cline）四路从不同方向走到同一个骨架。第三，20 年前经典的复兴——Working Effectively with Legacy Code（Michael Feathers，2004），Characterization Tests 和 Seam 这两个概念在 AI 时代重新成为刚需。AI 改代码暴露的问题，和 20 年前遗留代码暴露的问题本质是同一个：怎么改一个你不完全理解的系统。方法没变，执行者从人变成了 AI + 人。第四，定位——<span style="color: red; font-weight: bold;">这套方法论的底层与业界共识完全对齐。</span>带着这张地图往下走，每一步都能看到更深的来源——不是凭空造概念，是把已经被业界验证过的骨架落到具体动作上。


## 4. 一份能照做的 Check List

第 3 章把骨架讲清楚了，本章把它落成可勾选的清单，供读者按"理解 / 改造 / 验证"三个阶段对照项目逐项检查。清单每一条都在反向规避第 1 章讲的三个反模式——一次性改造、丢失嵌入业务逻辑、技能鸿沟。

### 4.1 理解段

<img src="imgs/aicmigr-05-approach-05-industry-progress/5ddef1d7861222b016c6f66db8cdcdcb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

按三段式骨架分三组，每组五项。项尽量具体可执行，避免空泛口号。

| 项   | 检查点                                          | 是否具备 |
| --- | -------------------------------------------- | ---- |
| ①   | 是否有一张 Context Map 标注系统模块、数据流、关键依赖            | ☐    |
| ②   | 是否识别出代码中的 Seam（可隔离的改造缝隙）                     | ☐    |
| ③   | 是否为关键路径补充了 Characterization Tests 锁住"当前实际行为" | ☐    |
| ④   | 是否把理解结果沉淀到 CLAUDE.md（持久化项目记忆）                | ☐    |
| ⑤   | context 使用率是否控制在 40% 以下，避免进入 Dumb Zone       | ☐    |

### 4.2 改造段

<img src="imgs/aicmigr-05-approach-05-industry-progress/a048907af9ca2414fc36a2064f3344f0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 项 | 检查点 | 是否具备 |
|----|--------|---------|
| ① | 是否拆成小步渐进迁移，而不是一次性重写整个系统 | ☐ |
| ② | 是否把重复流程固化成 SKILL.md（Custom Project Commands） | ☐ |
| ③ | 是否在 AI 介入前先制造 Seam，让爆炸半径可预测 | ☐ |
| ④ | 是否对历史数据通过 MCP 接入，让 AI 看见"为什么写成现在这样" | ☐ |
| ⑤ | 是否启用 git 工作流（如 Aider 的每步自动 commit），保证随时可回滚 | ☐ |

### 4.3 验证段
<img src="imgs/aicmigr-05-approach-05-industry-progress/bbd6038a905e14ae13b55ae176b428ea_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 项   | 检查点                                       | 是否具备 |
| --- | ----------------------------------------- | ---- |
| ①   | 是否有编译或类型检查作为 CI 门禁                        | ☐    |
| ②   | 是否在 PR 流程中加入跨模型 review                    | ☐    |
| ③   | 是否对 AI 改动强制跑 Characterization Tests 回归    | ☐    |
| ④   | 是否对 AI 生成代码引入安全扫描（参考 Veracode 报告 45% 漏洞率） | ☐    |
| ⑤   | 是否有等价性验证机制（Anthropic Starter Kit 第三阶段）    | ☐    |

### 4.4 棕地税速查：现象 → 打法

<img src="imgs/aicmigr-05-approach-05-industry-progress/72171976f5861f788dfe37a8b1161167_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Brownfield Tax 的五种现象含义已在第 1 章详解，本节只做"现象 → 打法"速查映射，供改造过程中遇到具体问题时快速查阅对应动作。

| 现象                        | 打法            |
| ------------------------- | ------------- |
| Dumb Zone                 | 上下文压缩和蒸馏      |
| Cross-session Forgetting  | CLAUDE.md     |
| Context-blind suggestions | MCP 接入历史数据    |
| Translation Tax           | SKILL.md 固化流程 |
| Context Overflow          | Context Map   |

五种现象合起来叫 Brownfield Tax——老项目向 AI 征的"税"。上面这张表里的每一条打法，都对应到 §4.1-4.3 清单里的某一项检查点：现象被识别出来，打法就被清单吸收为可执行的步骤。

### 4.5 参考资料

<img src="imgs/aicmigr-05-approach-05-industry-progress/53e23da2125dd64c4adb249e3585a4e3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

**核心论文**

- Chain of Understanding：Supporting End-user Developers' Code Understanding with Large Language Models，Jie Gao 等，ICPC 2026，arXiv：2504.04553

**企业实践报告**

- AI-Assisted Legacy Code Modernization Guide 2026，Cleveraud
- Using GenAI to understand legacy codebases，Thoughtworks Technology Radar
- How to Refactor Legacy Code，Augment Code，2026
- Best Practices for AI Refactoring Legacy Code，CodeGeeks Solutions，2026

**风险数据源**

- Addy Osmani 关于 Comprehension Debt 的系列文章
- Florida International University 关于 Brownfield Tax 的联合研究
- Sonar State of Code Developer Survey (2026)
- Veracode 2025 年度安全报告
- Ox Security 关于 Army of Juniors 的分析报告
- Anthropic 内部 52 人随机对照实验

**经典书籍**

- Working Effectively with Legacy Code，Michael Feathers，2004

**开源工具**

- Continue、Aider、Cline、Goose：各自 GitHub 仓库

