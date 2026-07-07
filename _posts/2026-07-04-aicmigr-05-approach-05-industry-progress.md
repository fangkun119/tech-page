---
title: 传统项目迁AI 05：学习方法 - 2026业界进展
author: fangkun119
date: 2026-07-04 05:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
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

## 1. 导读地图：本篇怎么读

<img src="imgs/aicmigr-05-approach-05-industry-progress/243443b36ffdc33918e1f11badc83dfe_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是「了解方法」阶段的收官综述。前几篇讲的是方法论本身——九步链路、人机分工、三层控制、武器库，属于"技"。本篇要给读者的是"道"：让读者看到，本系列讲的方法论不是作者个人套路，而是 2025-2026 年业界从学术到工程、不同角度同时收敛出来的方向。

读完本篇，读者会带着一张"方法论地图"进入系列后续的实战部分——每学一篇回头看本篇，能对照业界位置，看清所学方法的来源。

<img src="imgs/aicmigr-05-approach-05-industry-progress/880eb95446c023dca2cc2e084ff0fa32_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    Start([本篇导读：2026 业界进展])
    Start --\> P1[第一部分 方法论提炼]
    Start --\> P2[第二部分 实战演示]

    P1 --\> P1A[1. 业界进展速读<br/>三段式骨架速查]
    P1 --\> P1B[2. 项目 Check List<br/>可裁剪速查]

    P2 --\> P2A[3. 三个真实问题<br/>三个债]
    P2 --\> P2B[4. 共同方向<br/>学术/大厂/咨询/开源]
    P2 --\> P2C[5. 20 年经典复兴<br/>Feathers]
    P2 --\> P2D[6-8. 系列定位/小结/思考]

    P1A -.速读.-> Beginner([初学工程师<br/>建立认知骨架])
    P1B -.速查.-> Senior([熟练工程师<br/>快速回顾])
-->

两类读者的推荐读法：

- **初学 AI 编程工程师**：建议通读全篇。第一部分建立"理解 → 改造 → 验证"的认知骨架，第二部分用学术、大厂、咨询、开源四路案例与 Feathers 经典书深化 why。
- **熟练 AI 编程工程师**：可只看第一部分的 Check List 速查；遇到方法论被质疑时，再回到第二部分找对应的业界证据。

## 2. 业界进展速读：方法论不是个人套路

业界现在教 AI 编程的内容很多，讲工具的更多。读者可能已经学了 Claude Code 怎么用、CLAUDE.md 怎么写，但本篇想让读者知道：所学的方法论不是个人套路，而是业界从学术到工程、不同角度同时收敛出来的方向。看完会有底气：本系列讲的方法论，是有扎实学术和工程基础的。

### 2.1 业界收敛的同一个骨架

#### (1) 三段式骨架速查

把 2025-2026 年业界在 AI + 老项目改造领域的主流实践并排放在一起，会发现它们都收敛到同一个三段式骨架：

```
理解 → 改造 → 验证
```

这是一个领域走向成熟的标志——所有被现实逼出来的方法，严肃从业者都会得出同样的结论。三段式骨架的内涵如下：

| 段 | 关注点 | 目标 | 典型动作 |
|----|--------|------|---------|
| 理解 | 项目当下实际在做什么 | 建立准确的行为基线 | Context Map、Seam 识别、Characterization Tests |
| 改造 | 在不破坏基线的前提下演进代码 | 控制爆炸半径、保留可回滚 | 渐进式迁移、SKILL.md 固化流程、MCP 接入历史数据 |
| 验证 | 改完之后行为是否仍然等价 | 不依赖人肉 review 兜底 | 编译门禁、跨模型 review、CI 强制回归 |

#### (2) 为什么这不是巧合

业界用大量数据和研究给出了一个共识：问题不在 AI 的能力，在老项目这个场景本身对 AI 不友好。当真实问题摆出来后，所有认真做这件事的人都会自然得出"理解 → 改造 → 验证"的骨架——这是问题倒逼出来的答案，不是某一家公司的市场话术。

### 2.2 四路证据一图速览

学术、大厂、咨询、开源四个独立来源，分别从不同角度走到了同一个骨架：

| 来源 | 代表动作 | 对应骨架段 |
|------|---------|-----------|
| 学术 | Chain of Understanding 论文（ICPC 2026）、代码知识图谱 | 理解 |
| 大厂 | Anthropic Code Modernization Starter Kit（2026 年 3 月） | 理解 + 改造 + 验证 |
| 咨询 | Thoughtworks CodeConcise + Multi-pass Enrichment、Cleveraud 报告 | 理解 + 改造 |
| 开源 | Aider（git 可回滚）、Cline（透明执行）、Continue（多模型）、Goose（toolkit） | 改造 + 验证 |

后续章节会逐路展开讲，本部分先建立骨架速查的印象。

## 3. 第一部分 Check List：用业界共识检验自己的项目

<img src="imgs/aicmigr-05-approach-05-industry-progress/b41bdf70389bb387bef387d9c48b16ee_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节提供一份可裁剪、可勾选的 Check List，供工程师在项目对应阶段快速查阅。清单按"理解 / 改造 / 验证"三段式分组，每项尽量具体可执行，避免空泛口号。

### 3.1 三段式骨架速查 Check List

#### (1) 理解段 Check List

| 项 | 检查点 | 是否具备 |
|----|--------|---------|
| ① | 是否有一张 Context Map 标注系统模块、数据流、关键依赖 | ☐ |
| ② | 是否识别出代码中的 Seam（可隔离的改造缝隙） | ☐ |
| ③ | 是否为关键路径补充了 Characterization Tests 锁住"当前实际行为" | ☐ |
| ④ | 是否把理解结果沉淀到 CLAUDE.md（持久化项目记忆） | ☐ |
| ⑤ | context 使用率是否控制在 40% 以下，避免进入 Dumb Zone | ☐ |

#### (2) 改造段 Check List

| 项 | 检查点 | 是否具备 |
|----|--------|---------|
| ① | 是否拆成小步渐进迁移，而不是一次性重写整个系统 | ☐ |
| ② | 是否把重复流程固化成 SKILL.md（Custom Project Commands） | ☐ |
| ③ | 是否在 AI 介入前先制造 Seam，让爆炸半径可预测 | ☐ |
| ④ | 是否对历史数据通过 MCP 接入，让 AI 看见"为什么写成现在这样" | ☐ |
| ⑤ | 是否启用 git 工作流（如 Aider 的每步自动 commit），保证随时可回滚 | ☐ |

#### (3) 验证段 Check List

| 项 | 检查点 | 是否具备 |
|----|--------|---------|
| ① | 是否有编译或类型检查作为 CI 门禁 | ☐ |
| ② | 是否在 PR 流程中加入跨模型 review | ☐ |
| ③ | 是否对 AI 改动强制跑 Characterization Tests 回归 | ☐ |
| ④ | 是否对 AI 生成代码引入安全扫描（参考 Veracode 报告 45% 漏洞率） | ☐ |
| ⑤ | 是否有等价性验证机制（Anthropic Starter Kit 第三阶段） | ☐ |

### 3.2 Brownfield Tax 五种现象对应打法（子清单）

来自佛罗里达国际大学（FIU）的研究。当 AI 在有历史包袱的项目里改代码，五种典型现象会反复出现，每种都有对应打法：

```
Dumb Zone                → 上下文压缩和蒸馏
Cross-session Forgetting → CLAUDE.md
Context-blind suggestions → MCP 接入历史数据
Translation Tax          → SKILL.md 固化流程
Context Overflow         → Context Map
```

五种现象的含义：

- **Dumb Zone**：AI 的 context 使用率一旦超过 40%，输出质量就开始下降。老项目随便塞点代码和文档，context 很容易就到 40%。
- **Cross-session Forgetting**：AI 每开一个新对话，前面辛苦教它的东西全忘了。
- **Context-blind suggestions**：AI 不知道代码为什么写成现在这样，会给出更现代的方案，但和现有架构完全不兼容。
- **Translation Tax**：senior 开发者用 AI 反而变慢，因为要花时间纠正 AI 的 naive 建议。经验越丰富，被 AI 浪费的时间越多。
- **Context Overflow**：老项目相关代码和依赖分布在几十个文件里。全喂给 AI 会爆掉，不喂全又看不见全貌。

五种现象合起来叫 Brownfield Tax——老项目向 AI 征的"税"。

### 3.3 三个高风险反模式（来自 Cleveraud 2026 报告）

Cleveraud 在其 2026 年 3 月的报告中明确指出，老项目 AI 改造有三个反复出现的失败模式，工程师在动手前必须自检是否在踩坑：

```
三个失败模式反复出现：
- 试图一次性改造整个系统
- 在翻译过程中丢失嵌入的业务逻辑
- 技能鸿沟没有团队能独立跨越
```

把这三个反模式与上面的 Check List 对照看，会发现 Check List 的每一条都在反向规避它们——例如"渐进式迁移"规避"一次性改造"，"MCP 接入历史数据"规避"丢失业务逻辑"，"固化 SKILL.md"规避"技能鸿沟"。

## 4. 业界看到的三个真实问题

<img src="imgs/aicmigr-05-approach-05-industry-progress/24be8e3a5fedae3f94a6881300cab3d9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第一部分给出了骨架与 Check List，第二部分开始解释 why。先看业界看到了什么问题——核心是三个"债"。理解这三个债，就理解了老项目改造里 AI 踩坑的全部原因。

### 4.1 理解债（Comprehension Debt）

#### (1) 概念与来源

"理解债"（Comprehension Debt）这个词由 Google 的 Addy Osmani 提出。他的观察是：AI 帮工程师写代码的速度，和工程师真正理解这些代码的速度，正在快速拉开差距。

工程师过去写 100 行代码，自己写、自己 review，对这 100 行是熟的。现在 AI 帮工程师生成 1000 行，工程师还是只有时间理解那 100 行，剩下 900 行在代码库里，不在脑子里。短期没事，真要出 bug、要改造、要对接新需求，工程师回头读自己的代码库就像读别人的代码。

#### (2) Anthropic 内部实验数据

Anthropic 自己做过一个 52 人的随机对照实验验证这件事：用 AI 辅助的那组，在代码理解测试上比对照组低 17%，debugging 维度差距最大。

#### (3) 对项目的影响与对应方法论

老项目本来就欠了十年的理解债，AI 一边帮忙改、一边加新债。如果没有系统的方法把 AI 的产出"理解进来"，这个债永远还不清。CLAUDE.md、SKILL.md 这些不是简单的笔记工具，而是对抗理解债的方法论——它们让 AI 的产出能被持久化、被后续 session 和工程师复用。

### 4.2 棕地税（Brownfield Tax）

#### (1) 来源

"棕地税"（Brownfield Tax）这个词来自佛罗里达国际大学（Florida International University，FIU）的研究。研究者专门研究 AI 在有历史包袱项目里的表现，总结出五个典型现象。

#### (2) 五种典型现象

```
Dumb Zone：AI 的 context 使用率一旦超过 40%，输出质量就开始下降。老项目随便塞点代码和文档，context 很容易就到 40%。

Cross-session Forgetting：AI 每开一个新对话，前面你辛苦教它的东西全忘了。

Context-blind suggestions：AI 不知道代码为什么写成现在这样，会给你更现代的方案，但和你的架构完全不兼容。

Translation Tax：senior 开发者用 AI 反而变慢，因为要花时间纠正 AI 的 naive 建议。经验越丰富，被 AI 浪费的时间越多。

Context Overflow：老项目相关代码和依赖分布在几十个文件里。全喂给 AI 爆掉，不喂全又看不见全貌。
```

#### (3) 五种现象对应的打法

五种税每一种都有对应的打法。不是凑出来的方法，是业界看到了问题后被逼出来的：

```
Dumb Zone              → 上下文压缩和蒸馏
Cross-session Forgetting → CLAUDE.md
Context-blind suggestions → MCP 接入历史数据
Translation Tax        → SKILL.md 固化流程
Context Overflow       → Context Map
```

### 4.3 验证债（Verification Debt）

#### (1) 数据

Sonar 的 State of Code Developer Survey 给出一组很扎眼的数据：

```
42% 的代码是 AI 辅助生成的
96% 的开发者不完全信任 AI 的输出
只有 48% 每次都 review AI 生成的代码
```

一半的代码是 AI 写的，一半没被认真 review。中间的 gap 就是验证债。

Veracode 2025 年的报告更狠：45% 的 AI 生成代码引入了安全漏洞。几乎一半。

#### (2) Army of Juniors 概念

Ox Security 给这个现象起了一个名字——Army of Juniors（实习生大军）：AI 产出"功能性极高，但系统性地缺乏架构判断力"。就像招了一千个实习生，每个都能写代码，但没一个能对架构负责。

#### (3) 对应方法论

工程师必须有工具兜底验证，不能靠人肉 review。这正是系列后续"运行和测试：编译、测试、建立护栏"要解决的核心问题。Characterization Tests、跨模型 review、CI 门禁，不是最佳实践，而是验证债逼出来的刚需。

### 4.4 三个债背后是同一件事

这三个债看起来是三件事，实际上是一件事的三个侧面。

<img src="imgs/aicmigr-05-approach-05-industry-progress/5352a56fc81fd0a863b93e00f9d1b169_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/05_了解方法_05：2026业界进展/5352a56fc81fd0a863b93e00f9d1b169_MD5.jpg
用途：直观展示 AI 代码产出速度与人类理解/验证能力之间日益扩大的差距，老项目放大了这一结构性矛盾
内容：两条曲线/两条腿对比——AI 产出速度曲线快速上升在前，人类理解与验证能力曲线缓慢爬升在后，三个债（理解债/棕地税/验证债）正是这一差距被老项目放大后的三种表现
-->

AI 的写代码速度跑在前面，人的理解和验证能力在后面追，老项目放大了这个差距。这是一个结构性问题，不是模型再变强就能解决的。模型越强，产出越快，差距越大。

业界的解法不是让模型变弱，而是给人配上一套能追上模型产出速度的方法论。这也是开篇词里所说的——差的不是 AI，差的是用法。AI 不会变弱，但工程师可以补上方法论这条腿。

## 5. 业界在收敛的共同方向

看完问题，看业界的解法。把学术、大厂、咨询、开源社区的动作扫一遍，会发现所有认真做这件事的人都在收敛到同一个骨架。

### 5.1 学术界：让 AI 像专家一样读代码

<img src="imgs/aicmigr-05-approach-05-industry-progress/55897d6c5c40cc79efec9c722cef3cfc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) Chain of Understanding 论文

学术界最值得关注的一篇论文叫 Chain of Understanding，2025 年 4 月发在 arXiv，2026 年 4 月在 ICPC（程序理解领域的顶级会议）正式发表。

作者做了件很朴素的事：找了 8 位代码审计专家，问他们怎么读一个陌生代码库。结果非常一致——所有专家都按同一条链走：

```
全局理解：先把代码库当系统看（做什么的、几个模块、数据怎么流）
局部理解：再挑具体模块看内部逻辑
关系理解：再回到系统层看这个模块和其他部分的关系
```

这条链不是单向的，而是螺旋上升的：下到局部再回到全局，反复几次，理解才算建立。

作者基于这个做了工具叫 CodeMap，用户实验的结果是：用 CodeMap 的人对 LLM 回答的依赖降低了 79%。

#### (2) 代码知识图谱方向

另一个学术方向是代码知识图谱。思路是把代码先解析成一张图（节点是类、函数、模块，边是调用、继承、依赖），AI 基于图查询而不是搜文本。Thoughtworks Technology Radar 2026 年把这个方向推荐为值得采纳的实践。

#### (3) 对本系列读者的意义

本系列第二部分（了解项目）就是 Chain of Understanding 的落地版。画 Context Map 对应全局理解，识别 Seam 对应局部 + 关系理解。读者不必读原论文，但可以知道自己在走的路有 ICPC 2026 的学术背书。

### 5.2 大厂：Anthropic 把老项目改造标准化

#### (1) Code Modernization Starter Kit 推出

2026 年 3 月，Anthropic 在 $100M Claude Partner Network 下推出了 Code Modernization Starter Kit。

这不是一个工具，而是一套工作流模板。Anthropic 把他们和企业客户合作改造老项目的经验打包成了标准化资产，企业客户能直接用。

#### (2) 三阶段结构

核心结构是三阶段：

```
代码库分析 → 渐进式迁移 → 等价性验证
```

把这三个词和本系列的理解层、约束层、验证层对照看，会发现它们高度一致。

#### (3) CLAUDE.md 与 Custom Project Commands 的官方定位

这不是巧合。Anthropic 在官方文档里明确说：CLAUDE.md 承担"持久化项目记忆"的角色，把业务规则、边界情况、架构决策写进去，让 context 能跨 session、跨工程师传递。Custom Project Commands（本系列中称为 SKILL.md）把改造方法论编码成可复用的脚本，保证每个模块被处理的方式一致。

#### (4) 对本系列读者的意义

本系列的底层方法论和 Anthropic 官方背书的方向完全一致。差别在于 Anthropic 的 Starter Kit 面向 COBOL 到 Java 这种跨语言翻译，本系列更贴近日常工作里的同语言改造。

### 5.3 咨询：Thoughtworks 与 Cleveraud 的判断

<img src="imgs/aicmigr-05-approach-05-industry-progress/fec80f9b347696e4a76fc2bbee25a535_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

企业咨询公司是真正在帮客户改真实老系统的，他们的经验更接地气。

#### (1) Thoughtworks CodeConcise 与 Multi-pass Enrichment

Thoughtworks 有一个内部工具叫 CodeConcise，用知识图谱做 COBOL 等老系统的逆向工程。他们 2025 年底公布的数据：用 AI + 知识图谱做 COBOL 老系统逆向，时间比传统方法减少 66%。

他们的核心方法论叫 Multi-pass Enrichment（多轮富化）——一轮一轮地给代码图加料：

```
先拿 AST 抽结构
再让 LLM 补语义
再注入业务知识
再交叉验证
```

这和本系列讲的"理解是长出来的"本质是一回事。

#### (2) Cleveraud 的三个失败模式

Cleveraud 是一家做了 15 年的咨询公司。他们 2026 年 3 月发的报告里有几段判断讲得很透：

```
三个失败模式反复出现：
- 试图一次性改造整个系统
- 在翻译过程中丢失嵌入的业务逻辑
- 技能鸿沟没有团队能独立跨越
```

#### (3) Cleveraud 的三段核心判断

```
AI 把数周的代码库分析压缩到几天。
但这种压缩不是捷径，而是让人类判断能从实际知识出发而不是假设。
```

```
架构决策、业务规则背后的监管解读，这些 AI 无法从代码里推断。
没有 AI 能读一条计费规则就知道这是 2009 年某地区监管审计之后加上去的。
这种 context 只存在于你的领域专家脑子里。
```

三句话把老项目改造的三个核心判断讲清楚了：不要一次性改造、AI 做的是压缩分析不是替代判断、代码之外的 context 永远要靠人。

#### (4) 对本系列读者的意义

咨询公司的判断和本系列方法论一致：渐进式迁移、保留业务 context、把 AI 当成"压缩分析工具"而不是"替代判断工具"。工程师在做项目决策时，可以拿这些业界共识作为说服团队和利益相关方的依据。

### 5.4 开源：社区在验证不同可能性

最后看开源社区。2024-2026 年冒出了一批优秀的开源 AI 编程工具，和闭源产品走不一样的路线。

#### (1) 四个代表工具

- **Aider**：完全基于 git 工作流，每次改动自动 commit，失败了随时 reset。"永远有保险"的设计对老项目改造特别友好。
- **Cline**：VSCode 插件，最大特点是透明，每一步的 plan、action、result 都显示出来，可以看着它思考，再决定是否执行。
- **Continue**：支持多模型 backend，可以混用 Claude 和 GPT。
- **Goose**：Block 开源的 Agent 框架，核心是 toolkit 机制，支持自定义 toolkit 让 Agent 完成特定领域任务。

#### (2) 开源验证的价值

这四个工具不是让读者都去用，而是让读者知道：闭源产品定义方向的时候，开源社区在验证这个方向的多种可能性。Aider 验证了"永远可回滚"的价值，Cline 验证了"透明执行"的价值。这些设计哲学也会反向影响闭源产品的演进。

### 5.5 殊途同归

<img src="imgs/aicmigr-05-approach-05-industry-progress/4412215df73f42137cfd89be5cabc29f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

扫完这四路，会发现一个规律：所有主流实践都在收敛到一个共同骨架——理解 → 改造 → 验证，三段式。

```
理解 → 改造 → 验证
```

<img src="imgs/aicmigr-05-approach-05-industry-progress/ec486530571abdf7fe7a90734eb98c3d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/05_了解方法_05：2026业界进展/ec486530571abdf7fe7a90734eb98c3d_MD5.jpg
用途：展示学术、大厂、咨询、开源四路独立实践如何殊途同归收敛到"理解→改造→验证"三段式骨架
内容：四路实践（学术 Chain of Understanding/知识图谱、Anthropic Starter Kit、Thoughtworks/Cleveroad 咨询方法论、Aider/Cline/Continue/Goose 等开源工具）汇聚到下方共同的"理解→改造→验证"流程图
-->

名字不一样，骨架一致。被现实逼出来的事，所有严肃从业者都会得出同样的结论。这是一个领域走向成熟的标志。

## 6. 20 年前经典书的当代复兴

<img src="imgs/aicmigr-05-approach-05-industry-progress/3b3abd67d39d5dd63505a9f18db0ab2f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

最后讲一件特别有意思的事——一本 20 年前的书在 2024-2026 年重新火了起来。这本书不是新书，但它提出的两个核心概念在 AI 时代重新成了刚需。

### 6.1 一本 2004 年的"圣经"为何重新火起来

#### (1) 书籍基本信息

这本书叫 Working Effectively with Legacy Code，作者是 Michael Feathers，2004 年出版。工作了几年的工程师可能都听过，它是遗留代码改造领域的"圣经"。但 2020 年之前，它的主要读者只是那些不得不维护老系统的工程师。

#### (2) 在 2024-2026 年复兴的原因

从 2024 年开始，这本书的讨论量、引用量开始显著上升。今天几乎每一篇讨论"AI + legacy code"的文章都会引用它。原因是 Feathers 20 年前提出的两个核心概念——Characterization Tests 和 Seam——在 AI 时代重新成了刚需。

### 6.2 Characterization Tests：锁住行为基线

#### (1) 定义

Characterization Tests 的定义很简单：测试代码"现在实际"做什么，不是"应该"做什么。

#### (2) 5 步土法流程

步骤特别土：

```
1. 把代码放进测试框架
2. 写一条你知道会失败的断言
3. 让失败告诉你真实行为
4. 再把断言改成和真实行为一致
5. 测试通过，这就是你的"行为基线"
```

#### (3) 为什么 AI 时代变成刚需

听起来像废话？但它在 AI 时代变成刚需。因为 AI 改代码速度远超人 review 速度，不可能靠 review 确保 AI 没改坏。工程师必须有一个外部的、机械的、可回归的契约。

#### (4) 沉默的行为偏移

CodeGeeks Solutions 2026 年初的报告里有段话特别扎心：

> 这是 AI refactoring 领域最被低估的实践之一，因为它降低了 AI 的最大风险：沉默的行为偏移。

"沉默的行为偏移"（silent behavioral drift）指的是：AI 改完代码，测试跑通了、diff 看起来干净，但在某个没测到的路径上行为已经变了，工程师发现不了。Characterization Tests 就是对抗它的方法。

### 6.3 Seam：让改造可被隔离

#### (1) 定义

Seam 是程序里一个能改变行为、但不需要在那个位置编辑代码的地方。直白说：一个让代码改造可以被隔离的缝隙。

#### (2) 制造 Seam 的典型操作

把直接 new 的依赖抽成一个可覆写方法、把硬编码的配置抽成注入、把静态调用换成接口——这些都是在制造 Seam。

#### (3) 为什么对 AI 改造重要

AI 改一段没有 Seam 的代码，风险远高于有 Seam 的代码。因为 Seam 让影响范围可预测，AI 出错时爆炸半径小。

#### (4) Augment Code 报告引用

Augment Code 2026 年的报告里把这套讲得很完整：

```
先 Characterization Test 锁行为
再 Seam 做隔离
再 Refactor 做改造
```

### 6.4 一本 20 年前的书为何在 2026 反而更必要

一本 2004 年的书，在 2026 年反而更必要。原因很简单：AI 改代码暴露的问题，和 20 年前遗留代码暴露的问题，本质上是同一个——都是"怎么改一个你不完全理解的系统"。方法没变，只是执行者从人变成了 AI + 人。

如果读者还没读过 Feathers 的这本书，现在是最好的时机。它不长，20 年前的例子不影响理解，核心概念至今完全有效。读完会发现，本系列很多"新"方法，其实是 Feathers 20 年前就讲过的东西。

## 7. 这一系列站在哪里

<img src="imgs/aicmigr-05-approach-05-industry-progress/e31d18723b0cc0b07be06753ee6b3f98_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

扫完业界，最后讲一件事——本系列在业界版图里站在哪里。

### 7.1 为什么花一整篇讲业界

#### (1) 一个顾虑

业界综述类内容常被读者觉得重，像读报告。本系列也曾犹豫要不要单独开一篇讲业界，但最终还是决定讲一下。

#### (2) 目的

原因是：学到后面读者会积累很多具体方法、工具、流程，这些多了以后，很容易怀疑"这些真的是业界共识吗？还是作者的个人套路？"——本篇就是回答这个疑问。

#### (3) 本篇是方法论地图

从 ICPC 2026 的学术论文，到 Anthropic 官方 Starter Kit，到 Thoughtworks 15 年咨询经验，到 Feathers 20 年前的经典——所有这些源头，读者在接下来的系列里都会以某种形式见到它们的落地。本系列讲的方法论不是一家之言。

可以把本篇当成一张地图：每学完一篇，回头翻一翻这张地图，看看刚学的东西对应哪一块。

### 7.2 系列边界与读者责任

#### (1) 系列无法解决 100% 问题

不管本系列讲得多好，也解决不了读者 100% 的问题。每个人的项目、技术栈、团队习惯都不一样，不可能面面俱到。

#### (2) 系列提供的价值

本系列能做的，是把业界现在真实的样子让读者看到，把作者自己怎么判断、怎么做的讲给读者听。

#### (3) 读者责任

剩下的路要读者自己走。从本系列学到东西，然后自我演进——这才是真正属于读者自己的能力。

## 8. 小结

本篇扫完了 2026 年 AI + 老项目改造的业界全景。

### 8.1 四点核心提炼

#### (1) 业界看到的三个债

Comprehension Debt（理解债）、Brownfield Tax（棕地税）、Verification Debt（验证债）。三个债本质是同一件事——AI 产出速度远超人的理解和验证能力，老项目放大了这个差距。

#### (2) 业界收敛的共同骨架

理解 → 改造 → 验证。学术（Chain of Understanding、知识图谱）、大厂（Anthropic Starter Kit）、咨询（Thoughtworks、Cleveraud）、开源（Aider、Cline）四路殊途同归。

#### (3) 20 年前经典的复兴

Working Effectively with Legacy Code（Michael Feathers，2004）。Characterization Tests 和 Seam 在 AI 时代重新成为刚需。

#### (4) 系列定位

本系列方法论的底层与业界共识完全对齐。带着这张地图往下学，对每一步都能看到更深的来源。

### 8.2 承上启下

下一篇起进入系列第二部分，真正上手，拿一个项目用这套方法论走一遍。

## 9. 思考与参考资料

<img src="imgs/aicmigr-05-approach-05-industry-progress/99a319ebc3581ad4242f4eadcccb152e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 9.1 思考

#### (1) 三个债的自检

三个债（理解债、棕地税、验证债）里，读者在自己项目上感受最强烈的是哪一个？能不能具体描述一下？

#### (2) Feathers 的两个概念

读者以前接触过 Feathers 的 Characterization Tests 或 Seam 这两个概念吗？如果有，觉得它们在 AI 时代和 AI 之前有什么不同？如果没有，本篇之后会不会去读一读那本书？

### 9.2 参考资料

#### (1) 按类别整理的参考资料清单
##### ① 核心论文

- Chain of Understanding：Supporting End-user Developers' Code Understanding with Large Language Models，Jie Gao 等，ICPC 2026，arXiv：2504.04553

##### ② 企业实践报告

- AI-Assisted Legacy Code Modernization Guide 2026，Cleveraud
- Using GenAI to understand legacy codebases，Thoughtworks Technology Radar
- How to Refactor Legacy Code，Augment Code，2026
- Best Practices for AI Refactoring Legacy Code，CodeGeeks Solutions，2026

##### ③ 风险数据源

- Addy Osmani 关于 Comprehension Debt 的系列文章
- Florida International University 关于 Brownfield Tax 的联合研究
- Sonar State of Code Developer Survey (2026)
- Veracode 2025 年度安全报告
- Ox Security 关于 Army of Juniors 的分析报告
- Anthropic 内部 52 人随机对照实验

##### ④ 经典书籍

- Working Effectively with Legacy Code，Michael Feathers，2004

##### ⑤ 开源工具

- Continue、Aider、Cline、Goose：各自 GitHub 仓库
