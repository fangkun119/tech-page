---
title: 传统项目迁AI 11：了解项目 - 提炼SKILL
author: fangkun119
date: 2026-07-04 11:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-11-proj-rd-06-distill-skill/cover.jpg
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
aicmigr-11-proj-rd-06-distill-skill
传统项目迁AI 11：了解项目 - 提炼SKILL
-->

## 1. SKILL 是什么：从 SOP 到 AI 自动执行

CLAUDE.md 已经告诉 AI 这是什么项目。可一改代码，AI 还是不知道按某个流程走。原因很简单：CLAUDE.md 是静态知识，它说明项目是什么，不说明某件事怎么做。你需要的是另一类东西——SKILL。

### 1.1 一句话先讲透：SKILL 就是 AI 版的 SOP

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/b622dc87ca33a7dc5894e91583bdabbe_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

传统软件工程里，新员工入职会拿到一本《提 PR 检查清单》：跑测试、格式化、更新 changelog、看相关文档。照着走一遍，不漏步、不跑偏。

<span style="color: red; font-weight: bold;">SKILL 就是给 AI 的这本清单——写明在什么场景下触发、按什么步骤操作、能用哪些工具。</span>

换成专业说法：SKILL 是 Claude Code 中以 `SKILL.md` 形式存在的可复用能力封装，存放在 `.claude/skills/{name}/` 目录下，由 Claude Code 自动索引，按 `description` 字段匹配触发。AI 在合适的场景读到合适的 SKILL，照着走，不需要人工提醒。

一句话概括它的价值：<span style="color: red; font-weight: bold;">SKILL 把这些反复做但没沉淀的流程，固化成一个 AI 能自动执行的资产</span>。

### 1.2 CLAUDE.md vs SKILL：静态知识 vs 动态能力

你可能会问，CLAUDE.md 不已经告诉 AI 这是什么项目了吗？为什么还需要 SKILL？

两者性质不同。<span style="color: red; font-weight: bold;">CLAUDE.md 像项目的 README，告诉新人这是什么项目、技术栈是什么、目录结构怎么分；SKILL 像项目的 SOP 手册，告诉新人提 PR 怎么提、改接口怎么同步文档、动代码前怎么体检。</span>前者回答"是什么"，后者回答"怎么做"。两者缺一不可。

| 资产        | 回答的问题              | 性质   | 类比        |
| --------- | ------------------ | ---- | --------- |
| CLAUDE.md | 这是什么项目？AI 启动要知道什么？ | 静态知识 | 项目 README |
| SKILL     | 怎么做特定的事？某个反复流程怎么走？ | 动态能力 | 项目 SOP 手册 |

关键一句话：<span style="color: red; font-weight: bold;">CLAUDE.md 告诉 AI 这是什么项目，SKILL 告诉 AI 怎么做特定的事</span>。

### 1.3 为什么老项目是 SKILL 的富矿

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/48822ed56dac9437f44b724c1417845e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

新项目像刚装修的新房，每件东西你亲手放进去，记得清清楚楚。老项目像住了十年的老房子，很多东西天天用，却说不出到底放在哪——流程也一样，团队反复做，却没沉淀，靠记忆接力。

举个具体例子。A、B、Robert 三个人都提 PR：A 会跑测试和格式化，B 会顺手 review changelog，Robert 总是先做一次资产校对。三个人过 review 的速度差三倍。原因不是能力差，是流程没标准化——每个人凭记忆各做一套，漏一步、错一步是常态。

SKILL 的价值就在这里。把这些反复做但没沉淀的流程固化下来：A 装上之后，提 PR 前 AI 自动跑一遍 Robert 的全套流程；B 装上之后，AI 自动同步文档不会忘。<span style="color: red; font-weight: bold;">整个团队的下限被拉到上限</span>。

<span style="color: red; font-weight: bold;">老项目恰恰是 SKILL 的富矿，因为这种"反复做但没沉淀"的流程多到挖不完。</span>那什么样的流程值得挖？这件事怎么判断？具体方法在第 2 章展开。

### 1.4 一个老项目到底需要几个 SKILL

知道了 SKILL 是什么，第一个冲动可能是把所有重复流程都写成 SKILL。先别急。

挖之前先说克制。看一个数量区间表，心里有个谱：

| 数量区间      | 评价                                                                                    |
| --------- | ------------------------------------------------------------------------------------- |
| 5 个以内（推荐） | 大多数老项目够用                                                                              |
| 5-10 个    | 复杂系统适用                                                                                |
| <span style="color: red; font-weight: bold;">> 10 个</span>    | <span style="color: red; font-weight: bold;">容易出现"一句话匹配多个 SKILL"，AI 判断哪个该触发会迷茫</span> |

<span style="color: red; font-weight: bold;">SKILL 数量不代表 AI 协作能力，写得准、用得勤才是。</span>数量一多，AI 在多个 SKILL 之间会判断混乱、互相冲突，反而是负担。

推荐节奏：<span style="color: red; font-weight: bold;">先挖 3 个最高频的流程写成 SKILL，用一个月，觉得真的有用再扩展</span>。第一个月控制在 3 个最高频 SKILL 以内，用满一个月再决定要不要扩展，总数控制在 5 个以内（视系统复杂度而定）。挖之前先用第 2 章的三特征判断法问自己一遍——最近一个月在反复做什么？三个特征都满足，才写。

<span style="color: red; font-weight: bold;">不要追求一开始就写一套完美的 SKILL 体系。SKILL 是养出来的，不是设计出来的</span>。第一版只要能跑、能解决一个具体痛点就够了。


## 2. 怎么判断一件事值不值得写成 SKILL

SKILL 是富矿，但不是所有重复的事都该写。这一章给出一套筛选标尺。

### 2.1 三特征判断法：可复制、可参数化、可自动化

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/9835f67a7886c5d66d6304dbf55da74a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

传统软件工程里，怎么判断一件事值不值得花时间写脚本？问三遍：

- 会不会反复做？
- 骨架是不是固定的（只有变量在变）？
- 有没有清晰的起止？

三个都答"是"才值得自动化。<span style="color: red; font-weight: bold;">SKILL 一字不差地复用这套标尺。</span>

三个特征翻译成 SKILL 的判断标准：

| 特征                                                       | 含义              | 反例（不满足）    |
| -------------------------------------------------------- | --------------- | ---------- |
| <span style="color: red; font-weight: bold;">可复制</span>                     | 同样的动作序列会被反复执行   | 偶尔做一次的事    |
| <span style="color: red; font-weight: bold;">可参数化</span> | 只有几个变量在变，骨架是同一个 | 流程每次都不一样   |
| <span style="color: red; font-weight: bold;">可自动化</span> | 动作序列有明确的起点和终点   | 改着改着凭感觉做完了 |

<span style="color: red; font-weight: bold;">以"新增一个接口"的流程为例</span>：具体接口名不一样、入参出参不一样，但流程是一样的（先看 `docs/api-list.md` 里现有接口的路径规范、再看 `data-model.md` 里相关实体、再写实现、再补测试、再回头更新接口清单）。这就是典型的可参数化——骨架不变，只有变量在变。

先问自己最近一个月在反复做什么，再用三特征逐一筛选。

关键判断：<span style="color: red; font-weight: bold;">三个特征必须同时满足，才值得写成 SKILL。差一个都别硬写</span>。<span style="color: red; font-weight: bold;">偶尔做的事写进文档就好，流程太发散的事留在脑子里就好，做成 SKILL 反而会把 AI 卡在错误的框里。</span>

这里要提醒一个常见误区：很多人开始写 SKILL 时第一反应是研究 YAML 格式、description 怎么写、allowed-tools 有哪些。这些都是有用的知识，但应该在挖到具体场景之后才学。<span style="color: red; font-weight: bold;">SKILL 的起点是识别"我反复在做某件事"，而不是研究 YAML 格式</span>。没有重复流程就硬写 SKILL，产出的是一堆没人用的代码——Claude Code 扫 `.claude/skills/` 时每个 SKILL 都会被索引，SKILL 写多了 AI 在多个 SKILL 之间会判断混乱、互相冲突。所以写之前先想清楚：这件事是不是真的在反复做。

### 2.2 老项目里最值得挖的四类流程

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/13cbfcd5d33e95a1bc8fb6e326830a95_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

判断标准有了，具体哪些流程值得挖？<span style="color: red; font-weight: bold;">老项目里有四类流程几乎家家都有，挖出来就能用：</span>

| 候选流程                                                         | 触发时机   | 解决的痛点                         |
| ------------------------------------------------------------ | ------ | ----------------------------- |
| <span style="color: red; font-weight: bold;">技术文档自动更新</span> | 代码改动后  | 文档腐烂（接口清单、数据模型和真实代码对不上）       |
| <span style="color: red; font-weight: bold;">改造前体检</span>    | 动手改代码前 | 测试是否绿、编译是否通过、依赖的中间件是否连得上      |
| <span style="color: red; font-weight: bold;">PR 前检查</span>                                                       | 提 PR 前 | 测试跑过、格式化过、changelog 更新、相关文档改了 |
| <span style="color: red; font-weight: bold;">新增接口前对齐</span>                                                      | 加新接口前  | 看现有接口路径风格、统一响应格式、错误码规则        |

对照这张表，在团队里找一找对应动作，候选清单就出来了。

### 2.3 为什么挑"技术文档自动更新"做示范

上面四个候选都符合三特征。接下来挑第一个——技术文档自动更新——作为示范动手写。

理由要从老项目最普遍的痛点说起：文档腐烂。`docs/` 里的接口清单、数据模型、架构图，代码每次改动都让其中某一份漂移。不主动同步，三份资产慢慢就和真实代码对不上了——半年后整个 <span style="color: red; font-weight: bold;">`docs/` 没人敢相信，最后变成"代码即文档"。</span>

<span style="color: red; font-weight: bold;">挑技术文档自动更新做示范：它解决了老项目最普遍、最让人头疼的问题</span>，这种 SKILL 的价值最容易被人直接感受到。

挑好了示范场景，下一步是怎么挖。下一章给出五步实操，把这套方法落到 Spring AI Alibaba Admin 这个真实项目上。


## 3. 实操：让 AI 帮你挖出第一个 SKILL

方法论讲完，进入实战。本章所有提示词、产出的 SKILL、运行结果都围绕 **Spring AI Alibaba Admin** 这个项目展开，你可以照着复现一遍。

### 3.1 主线项目：Spring AI Alibaba Admin 的资产现状

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/9b077698be5f5b8ab1866e9d68058d91_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

动手之前，先看清地形——这就像传统项目启动前的需求调研和资产盘点，你得知道手头已有哪些家底。系列前几篇走完后，Spring AI Alibaba Admin 的项目根目录已经攒下这些资产：

```
项目根/
├── CLAUDE.md                  ← 启动时被 Claude Code 自动读取
└── docs/
    ├── architecture.svg       ← 架构图
    ├── module-deps.svg        ← 模块图
    ├── external-deps.svg      ← 依赖图
    ├── api-list.md            ← 接口清单
    └── data-model.md + data-model-er.svg  ← 数据模型
```

CLAUDE.md 告诉 AI"这是什么项目"，但还没告诉它"怎么做某件特定的事"——这件空白，就是本章要补上的另一类资产。

你将拿到一份新资产：**`.claude/skills/docs-auto-sync/SKILL.md`**。这份 SKILL <span style="color: red; font-weight: bold;">让 AI 在代码改动后自动比对接口清单和数据模型，产出不一致清单（只汇报，不自动改文件，由人决定如何处理）</span>。

### 3.2 第一步：让 AI 扫项目出候选清单

#### (1) 提示词：让 AI 扫描项目档案

挖 SKILL 不靠人手一条条想，靠"勘探队"。<span style="color: red; font-weight: bold;">让 AI 扫一遍项目档案，给你一份候选矿点清单，工程师只管挑哪个先挖</span>——这等价于传统软件的需求采集：先把所有潜在需求捞上来，再排优先级。

让 AI 扫 `git log`、`CLAUDE.md`、`docs/`、`README`、`CONTRIBUTING`、`.github/` 这些项目档案，提示词：

```
扫一下当前项目（包括 git log、CLAUDE.md、docs/、README、CONTRIBUTING、
.github/），找出团队反复在做的操作流程。

判断标准是三特征：可复制、可参数化、可自动化。三个都满足才算值得做 SKILL 的候选。

把找到的候选列出来，每个写明：流程名、为什么是反复的、能参数化的部分是什么、
起点和终点是什么。最后给我用一个表格总结。
```

#### (2) 跑完会拿到一份候选清单

跑完你会拿到一份清单，通常 5-10 项，是这个项目里所有候选 SKILL 的集合。Spring AI Alibaba Admin 这个项目运行后生成了四个候选，第一眼就能看到 CRUD 脚手架——这也是工程师最常遇到的场景。

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/74d6c26cad187b5550e3717c3c382ad1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/11_了解项目_06：提炼Skill/74d6c26cad187b5550e3717c3c382ad1_MD5.jpg
用途：展示 AI 扫描 Spring AI Alibaba Admin 项目后产出的候选 SKILL 清单
内容：表格形式列出 4 个候选流程（含 CRUD 脚手架、技术文档自动更新等），每个写明流程名、为什么是反复的、能参数化的部分、起点和终点
-->

这一步的核查清单（四条都到位才算跑扎实）：

| 核查项  | 要点                                                                    |
| ---- | --------------------------------------------------------------------- |
| <span style="color: red; font-weight: bold;">扫描范围</span> | 是否覆盖 `git log`、`CLAUDE.md`、`docs/`、`README`、`CONTRIBUTING`、`.github/` |
| <span style="color: red; font-weight: bold;">筛选标准</span> | 是否明确以三特征（可复制、可参数化、可自动化）为门槛                                            |
| <span style="color: red; font-weight: bold;">字段完整</span> | 每个候选是否写明流程名、为什么是反复的、能参数化的部分、起点和终点                                     |
| <span style="color: red; font-weight: bold;">输出形式</span> | 是否要求用表格总结                                                             |

### 3.3 第二步：让 AI 出 Top 3 推荐

#### (1) 提示词：让 AI 按三个维度排优先级

候选矿点太多，挑哪个先挖？让 AI 按三个维度排序。这等价于传统项目的优先级矩阵或 ROI 评估——把"该不该做"拆成可量化的维度：

| 维度 | 含义 |
|------|------|
| <span style="color: red; font-weight: bold;">频率高</span> | 这件事多久做一次（每天/每周/每月） |
| <span style="color: red; font-weight: bold;">痛点深</span> | 不做这件事会带来多大麻烦 |
| <span style="color: red; font-weight: bold;">自动化收益大</span> | 一个 SKILL 能省下多少人力 |

三维度同时高的，就是第一个该挖的 SKILL。提示词：

```
从上面的清单里挑 3 个最高优先级的，给我做成候选 SKILL。
每个候选写：name（英文）、description、预期 steps、allowed-tools。
优先级判断标准：频率高、痛点深、自动化收益大。用表格总结，包含类型和理由。
```

#### (2) Top 3 大概率包含"技术文档自动更新"

跑完会拿到三个候选。我的预判是：**Top 3 大概率会包含"技术文档自动更新"这一类**——因为它的频率最高、痛点最深（文档腐烂是老项目的慢性病）、自动化收益最大（一个 SKILL 替代每周几小时的人力维护）。下图中第二个就是它。

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/7ece2da34ab6fb5d680ef703456df466_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/11_了解项目_06：提炼Skill/7ece2da34ab6fb5d680ef703456df466_MD5.jpg
用途：展示 AI 从候选清单中挑选的 Top 3 SKILL 推荐结果（含 CRUD 脚手架、技术文档自动更新等）
内容：表格形式展示 Top 3 候选 SKILL，每个包含 name（英文）、description、预期 steps、allowed-tools，以及类型和优先级理由；第二个为"技术文档自动更新"
-->

这一步的核查清单（四条都到位，排序结果才经得起追问）：

| 核查项  | 要点                                                           |
| ---- | ------------------------------------------------------------ |
| <span style="color: red; font-weight: bold;">排序维度</span> | 是否明确频率高、痛点深、自动化收益大三个维度                                       |
| <span style="color: red; font-weight: bold;">字段完整</span> | 每个候选是否写明 `name`（英文）、`description`、预期 `steps`、`allowed-tools` |
| <span style="color: red; font-weight: bold;">预期命中</span> | Top 3 中是否包含"技术文档自动更新"（三维度都高）                                 |
| <span style="color: red; font-weight: bold;">输出形式</span> | 是否用表格总结（含类型和理由）                                              |

### 3.4 第三步：生成 CRUD SKILL（可选热身）

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/14b653ae38799b6f7cf7d3abe4274818_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

在挖主目标之前，先用一个最简单的提示词热身——让 AI 生成 CRUD 脚手架的 SKILL。这一步等价于正式开打前先放一枪校准准星：验证一遍工具链能跑通、产出格式符合规范，再放心去挖主矿。

提示词：

```
生成代码中 CRUD 的 SKILL。
注意按照标准格式和放在标准目录。
结果放到 .claude/skills/ 目录中。
```

这句话基本是通用的，你直接复制即可。

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/cd5feccda93d40eeaf940855be59b769_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/11_了解项目_06：提炼Skill/cd5feccda93d40eeaf940855be59b769_MD5.jpg
用途：展示 AI 在 .claude/skills/ 目录中生成 CRUD SKILL 的实际产出效果
内容：截图展示 AI 基于提示词生成的 CRUD SKILL 文件，包括 name、description、steps、allowed-tools 等字段的标准格式
-->

### 3.5 第四步：生成 docs-auto-sync SKILL
#### (1) 提示词：让 AI 生成 SKILL

热身完，进入主目标。这是本章的核心产出。强调这个 Skill <span style="color: red; font-weight: bold;">只汇报不一致的地方，不要自动改文件，让人决定怎么处理</span>。 提示词如下：

```markdown
基于上面的候选，给我生成完整的 SKILL.md。要求：

- 名字 docs-auto-sync
- description 写清楚什么场景触发、产出是什么
- steps 清晰可执行
- allowed-tools 限制到最小
- 重要：只汇报不一致的地方，不要自动改文件，让人决定怎么处理

保存到 .claude/skills/docs-auto-sync/SKILL.md。
```

#### (2) AI 给出的完整产出

AI 给出的完整内容会在下一章按段拆解、完整展开，这里先不贴全文 —— 那是第 4 章的主场。你这一步要做的，是把提示词跑通，拿到一份结构完整的 SKILL.md。

这一步的核查清单（六条都到位，生成的 SKILL 才算规范）：

| 核查项  | 要点                                          |
| --- | ------------------------------------------- |
| <span style="color: red; font-weight: bold;">name</span> | 是否指定 SKILL 名字                               |
| <span style="color: red; font-weight: bold;">description</span> | 是否写清触发场景与产出                                 |
| <span style="color: red; font-weight: bold;">steps</span> | 是否清晰可执行                                     |
| <span style="color: red; font-weight: bold;">allowed-tools</span> | 是否限制到最小权限                                   |
| <span style="color: red; font-weight: bold;">安全边界</span> | 是否明确"只汇报不一致，不自动改文件，让人决定"（文档同步类 SKILL 的关键约束） |
| <span style="color: red; font-weight: bold;">存放路径</span> | 是否保存到 `.claude/skills/{name}/SKILL.md` 标准目录 |

下一章按段拆解、完整展开 docs-auto-sync SKILL 的内容。

## 4. 完整产出：docs-auto-sync SKILL 逐段拆解

第四步 AI 生成的 docs-auto-sync SKILL 分 11 段呈现。每段开头一句设计意图点评，code block 逐字保留——读者可对照自查自己项目里 AI 生成的是否符合规范。

### 4.1 整体描述

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/a4dda0aefd5a1153d635ec8aeb134099_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

描述段写清三件事：<span style="color: red; font-weight: bold;">做什么、何时触发、产出什么</span>。这是 Claude Code 匹配触发的核心，相当于 API 的文档字符串——描述模糊，Skill 就不会被正确调用。

`````markdown
对照代码（Controller、Entity、SQL）与文档（`docs/api-list.md`、`docs/data-model.md`）做交叉比对，输出不一致清单，**不自动修改任何文件**，由人决定如何处理。

- 新增或修改了 Controller（接口变更、路径变更、参数变更）
- 新增或修改了 Entity 类或 SQL 表定义（字段变更、新表、删表）
- 怀疑文档与代码已经偏移，想做一次全量对齐检查
- PR review 前确认文档是否跟上了代码变更

一份结构化差异报告，分两节：

1. **接口差异**（代码 vs `docs/api-list.md`）：新增接口、删除接口、路径/方法变更、入参/返回类型变更

2. **数据模型差异**（Entity/SQL vs `docs/data-model.md`）：新增表/实体、删除表/实体、字段增删、类型变更、枚举值变更

每条差异标注：来源文件 + 行号、当前代码实际值、文档记录值、建议动作（更新文档 / 核实代码 / 忽略）。
`````

### 4.2 调用方式与参数

两个参数都设了默认值（`all` / `both`），裸调用即全量扫描——降低上手门槛。这正是 API 设计里的"合理默认值"原则：常用路径零配置。

`````markdown
```
/docs-auto-sync [targetModule] [docTarget]
```

| 参数 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `targetModule` | 否 | `all` | 模块名（如 `MCP`、`AgentSchema`）或 `all` 全量扫描 |
| `docTarget` | 否 | `both` | `api`（只查接口）、`model`（只查数据模型）、`both` |

**示例**

```
/docs-auto-sync
/docs-auto-sync McpServer api
/docs-auto-sync all model
```
`````

### 4.3 工具权限：allowed-tools 限制到最小的设计考量

`allowed-tools` 里**只有 `Read` 和 `Bash`（限定 find/grep）**，没有 `Write`、`Edit`、`Agent`。这是提示词里"只汇报不一致，不要自动改文件"在工具权限层的硬兜底——<span style="color: red; font-weight: bold;">即便 AI 想改文件也改不了，因为根本没给它那个工具</span>。

类比传统软件工程里的最小权限原则：给新员工开权限要按需给，不需要他动数据库就别授 DBA。docs-auto-sync 只需"读"代码和文档、用 `find`/`grep` 定位，那就只给 `Read` 和 `Bash`。<span style="color: red; font-weight: bold;">口头约束靠不住，权限兜底才靠谱。</span>

`````markdown
`Read`、`Bash`（仅用于 `find` / `grep` 定位文件）

**不使用** `Write`、`Edit`、`Agent`。
`````

### 4.4 执行步骤：参数解析

第一步先把"全量扫描"和"指定模块"两条路径在源头分开，后续 find/grep 才能精准收敛范围。相当于路由层前置分流。

`````markdown
When the user runs `/docs-auto-sync [targetModule] [docTarget]`:

1. `targetModule` 默认 `all`；`docTarget` 默认 `both`
2. 若 `targetModule` 不是 `all`，后续所有 find/grep 限定到包含该模块名的文件
`````

### 4.5 执行步骤：扫描 Controller 提取 endpoint

`find` 定位 Controller 文件，`grep` 提取所有 HTTP 方法注解——这是后续比对的数据来源，相当于 ETL 的 extract 阶段。提取的每一项（方法、路径、入参、返回、行号）都是比对时的字段。

`````markdown
用 Bash 扫描所有 Controller 文件：

```bash
find. -name '*Controller.java' -not -path '*/test/*'
```

对每个目标 Controller（`targetModule=all` 则全部），提取：

```bash
grep -n '@RequestMapping\|@GetMapping\|@PostMapping\|@PutMapping\|@DeleteMapping\|@PatchMapping' <file>
```

从代码中记录每个 endpoint 的：

- HTTP 方法（GET/POST/PUT/DELETE/PATCH）
- 完整路径（`@RequestMapping` 前缀 + 方法注解路径拼接）
- 方法签名中的入参类型（`@RequestBody`、`@RequestParam`、`@PathVariable`）
- 返回类型（`Result<T>`、`Flux<T>`、`SseEmitter` 等）
- 所在文件 + 行号
`````

### 4.6 执行步骤：扫描 Entity 和 SQL

分两路扫描：Entity 类拿字段元数据，SQL 文件拿原始表结构，交叉验证。这就是双数据源对账——任何一侧缺失或矛盾都逃不掉。

`````markdown
**2a. 扫描 Entity 类**

```bash
find. -name '*Entity.java' -not -path '*/test/*'
find. -name '*DO.java' -not -path '*/test/*'
```

对每个目标 Entity 文件，提取：

- `@TableName` 或 `@Table(name=...)` → 表名
- 所有字段名（驼峰）+ Java 类型
- `@TableId` / `@Id` 标注的主键字段
- `@TableField("snake_name")` 映射的列名

**2b. 扫描 SQL 文件**

```bash
grep -n 'CREATE TABLE\|^\s*`\|^\s*[a-z]' docker/middleware/init/mysql/admin-schema.sql
grep -n 'CREATE TABLE\|^\s*`\|^\s*[a-z]' docker/middleware/init/mysql/agentscope-schema.sql
```

从 SQL 中记录每张表的：表名、列名、列类型、是否有 NOT NULL / DEFAULT、注释
`````

### 4.7 执行步骤：解析现有文档

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/4fb228f04a882f0fb97fdf354dfa219d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把 `docs/` 下两份文档读出来、解析成结构化数据，准备与代码侧对账。加载对账的另一端。

`````markdown
```bash
```
用 `Read` 工具读取两个文档，解析出：

- api-list.md：每个模块的接口列表（方法 + 路径 + 入参说明 + 返回说明）
- data-model.md：每张表的字段列表（字段名 + 类型 + 说明）
`````

### 4.8 执行步骤：接口比对规则

判定同一接口用"HTTP 方法 + 路径完全相同"——清晰、可复现、无歧义。这等价于接口契约比对：签名对得上才算同一个接口，对不上就是差异。

`````markdown
对每个从代码提取的 endpoint，在 api-list.md 中查找对应记录：

**匹配规则**：HTTP 方法 + 路径完全相同为同一接口。

对每条 endpoint 判断：

| 情况 | 标记 |
|------|------|
| 代码有，文档无 | `[新增接口]` — 文档缺失 |
| 文档有，代码无 | `[已删接口]` — 文档过期 |
| 路径相同但方法不同 | `[方法变更]` |
| 入参类型与文档描述不符 | `[入参变更]` |
| 返回类型与文档描述不符 | `[返回变更]` |
| 完全一致 | 不输出，只统计通过数 |
`````

### 4.9 执行步骤：数据模型比对规则

与接口比对同构，以表为单元、按字段粒度判断新增/删除/类型变更/枚举变更。逻辑和 DB schema diff 工具一致。

`````markdown
对每张从 Entity/SQL 提取的表，在 data-model.md 中查找对应 `### {tableName}` 章节：

| 情况 | 标记 |
|------|------|
| 代码/SQL 有表，文档无章节 | `[新增表]` — 文档缺失 |
| 文档有章节，代码/SQL 无表 | `[已删表]` — 文档过期 |
| 表存在，但字段在代码中有、文档无 | `[新增字段]` |
| 表存在，但字段在文档中有、代码无 | `[已删字段]` |
| 字段存在，但类型不符 | `[类型变更]` |
| 字段存在，但枚举值说明不符 | `[枚举变更]` |
| 完全一致 | 不输出，只统计通过数 |
`````

### 4.10 执行步骤：报告格式

报告分三层：汇总数字、逐条差异详情、已知设计决策白名单。注意 code block **内部**的 `---` 是 SKILL.md 自己定义的报告分节分隔符，属于技术内容，**必须原样保留**，不是文章正文水平线。

`````markdown
**格式要求**：

```
扫描范围：{targetModule} / {docTarget}
扫描时间：{当前日期}

- 接口：{通过数} 条一致，{差异数} 条不一致
- 数据模型：{通过数} 条一致，{差异数} 条不一致

---

- 代码位置：`XxxController.java:42`
- 文档现状：docs/api-list.md 中无此接口
- 建议动作：在 docs/api-list.md 对应章节追加该接口说明

- 文档位置：`docs/api-list.md:105`
- 代码现状：未找到对应 Controller 方法
- 建议动作：确认是否已废弃，若是则从 api-list.md 中删除

- 代码位置：`AccountController.java:67`
- 代码实际：入参 `AccountQuery { page, size, keyword, type }`
- 文档记录：入参 `BaseQuery { page, size, keyword }`
- 差异：文档缺少 `type` 字段
- 建议动作：更新 docs/api-list.md 对应入参说明

---

- 代码位置：`AccountEntity.java:45` / `agentscope-schema.sql:28`
- 文档现状：docs/data-model.md
- 建议动作：在 data-model.md account 表中补充该字段

- 代码位置：`ExperimentResultDO.java:33`
- 代码实际：`BigDecimal`（SQL: `DECIMAL(3,2)`）
- 文档记录：`Float`
- 建议动作：修正 data-model.md 中 score 字段的类型说明

---

以下差异是已知的设计决策，不代表文档错误：

- `ChatSession`：无 MySQL 表，存 Redis，文档中已有"非 MySQL 实体"节说明
- `DocumentChunk`：无 MySQL 表，存 Elasticsearch，同上
- `GlobalConfig`：运行时 DTO，非持久化，同上
```
`````

### 4.11 报告后处理：差异数为 0 也要明确反馈

注意"若差异数为 0，输出'文档与代码完全一致，无需更新'"这一条。<span style="color: red; font-weight: bold;">SKILL 不是只在出问题时才有用——它跑完没问题，也要明确告诉用户没问题</span>。否则用户分不清"没跑"和"真没问题"。这就像健康检查必须返回 200，不能静默——<span style="color: red; font-weight: bold;">静默等于不可观测</span>。

`````markdown
报告输出后：

- **不修改任何文件**
- 告知用户：如需逐条修复，可用 `/add-crud-module` 补充新模块，或手动 Edit 对应章节
- 若差异数为 0，输出"文档与代码完全一致，无需更新"

---

- 比对时忽略注释风格、空白行、措辞差异，只关注结构性不一致（路径、方法、字段名、类型）
- Entity 字段用驼峰，文档字段用 snake_case，比对时统一转换后再匹配
- `targetModule` 模糊匹配：输入 `MCP` 可匹配 `McpServerController`、`mcp_server` 表
- 若同一路径在多个 Controller 中出现（如继承/覆盖），以最终注册到 Spring 的为准，扫描时注意 `@RequestMapping` 前缀叠加
- `docs/data-model.md` 中"非 MySQL 实体"节（ChatSession / DocumentChunk / GlobalConfig）不参与 SQL 比对，跳过
`````


## 5. SKILL 上线：三个测试必做

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/dd3f5172e656f48626dccfbd097a9890_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

代码写完要跑单元测试，接口做完要联调。SKILL 也一样——写完往那一放不验证，等到 AI 真用起来才发现 description 写偏了、该触发时不触发、或者自作主张改文件，已经晚了。

<span style="color: red; font-weight: bold;">SKILL 的 description 本质上是个"自动路由器"：它决定 AI 在什么场景下加载这个 SKILL。</span>路由规则对不对，不能靠猜，得靠三个明确的验证动作去测。

### 5.1 测试一：说一句应该匹配的话

**测试目的**：验证 SKILL 是否被自动加载。

这是 <span style="color: red; font-weight: bold;">happy path</span>——相当于单元测试里给一个典型输入，看函数是否被正确调用。对着 Claude Code 说一句应该触发的话：

```
我刚改完一批 Controller，帮我看看文档还对不对得上
```

**期望行为**：Claude Code 自动加载 docs-auto-sync，并按 SKILL 里写的步骤往下跑。

**不符合时怎么调**：没被加载，说明 `description` 写得太窄，或者关键词没覆盖到这句话。回去扩展 description 的场景描述和触发关键词。

### 5.2 测试二：说一句故意不匹配的话

**测试目的**：验证 SKILL 不被错误加载，防止过度触发。

只测 happy path 不够——就像单元测试不能只写正向用例。还得来一个 <span style="color: red; font-weight: bold;">negative case</span>：换一句和文档同步无关的话，看 AI 会不会误伤。

```
帮我检查一下这段代码
```

**期望行为**：这句话和文档对齐无关，SKILL 不应该被加载。

**不符合时怎么调**：<span style="color: red; font-weight: bold;">AI 错误加载了，说明 `description` 太宽泛，什么请求都往上凑</span>。收紧到只在"文档/代码对齐"这个场景触发。

### 5.3 测试三：真跑一次看产出

**测试目的**：验证产出符合预期，且没有越权行为。

前两个测试只验证"要不要加载"，这个测试<span style="color: red; font-weight: bold;">验证"加载之后干得对不对"</span>——相当于端到端验收。真跑一次，按下面这份清单逐项检查：

| 检查项    | 通过标准                                              |
| ------ | ------------------------------------------------- |
| <span style="color: red; font-weight: bold;">步骤完整性</span>  | 是不是按 SKILL 里写的那些步骤走了                              |
| <span style="color: red; font-weight: bold;">差异具体性</span>  | 是不是列了具体的不一致点（含代码位置、文档位置、建议动作）                     |
| <span style="color: red; font-weight: bold;">工具越权检查</span> | 有没有自作主张改文件（应该没有，因为 `allowed-tools` 没给 Write/Edit） |

<span style="color: red; font-weight: bold;">三个测试都过，SKILL 才算上线。</span>


## 6. SKILL 是养出来的，不是设计出来的

### 6.1 CLAUDE.md 与 SKILL 的对称分工

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/f29e0f82b5504a543003becd2fab875c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

走到这一步，CLAUDE.md 写完了，SKILL 也上线、过了三个测试。两份资产一前一后，正好把全篇的主线收束成一张图。

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/cb9ca823deac8b7eb579173e12211c33_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/11_了解项目_06：提炼Skill/cb9ca823deac8b7eb579173e12211c33_MD5.jpg
用途：对比 CLAUDE.md（系列第 10 篇）和 SKILL（系列第 11 篇）的分工与对称关系
内容：左右对称结构图——左侧 CLAUDE.md（怎么写：静态知识、项目是什么、AI 启动共识），右侧 SKILL（怎么挖：动态能力、怎么做特定的事、操作流程固化）
-->

一句话点透这对关系：**CLAUDE.md 告诉 AI 这是什么项目，SKILL 告诉 AI 怎么做特定的事**。前者是静态知识，后者是动态能力——一动一静，一知识一能力，一起看才完整。

### 6.2 60 分起步，养到 90 分

你可能会问：第一版 SKILL 要写到什么程度才算合格？

我的看法很直接——**AI 帮工程师生成的第一版大概只能到 60 分**。不要追求第一版完美，追求"能跑、能解决一个具体痛点"就够了。<span style="color: red; font-weight: bold;">剩下的分数，是养出来的。</span>

| 阶段    | 分数      | 怎么做                                                                       |
| ----- | ------- | ------------------------------------------------------------------------- |
| AI 初版 | 60 分    | 让 AI 基于提示词生成                                                              |
| 手动微调  | 70-80 分 | 调 `description` 让触发更精准、改 `steps` 按团队真实流程走、收紧 `allowed-tools` 避免越权（约一两个小时） |
| 实战打磨  | 80+ 分   | 用一个月，每次实际触发中迭代——发现误触发就收紧 `description`、发现漏触发就扩展场景、发现步骤不对就调 `steps`        |
| 持续迭代  | 90+ 分   | 半年后 SKILL 真正成熟，成为团队标配资产                                                   |

**SKILL 是养出来的，不是设计出来的**。新人入职装上就能用，老人改造时不用每次想流程——这才是 SKILL 成熟的标志。设计是起点，养是路径，<span style="color: red; font-weight: bold;">团队的反复触发才是让它从 60 分走到 90 分的真正力量。</span>

### 6.3 场景驱动，不是工具驱动

学到这里，你可能会想：那我得把所有 SKILL、所有 MCP、所有 Agent 都学一遍才行吧？

恰恰相反。

团队的"文档腐烂"问题——这是**场景**、是**事**。需要"自动同步文档"的方案——这是**工具**、是 SKILL。打开工具箱发现 SKILL 这个东西，刚好能用。

| 思维方式 | 表现 | 结果 |
|---------|------|------|
| 工具驱动 | "我要学 SKILL"才学 SKILL | 学完用不上 |
| 场景驱动 | "我有这个问题"才用 SKILL | 工具发挥价值 |

美团创始人王兴有句经典的话：**和高人聊，从书上学，在事上练**。<span style="color: red; font-weight: bold;">一定要在"事上练"——工具是死的，场景是活的。</span><span style="color: red; font-weight: bold;">带着场景去用工具，工具才有价值；盯着工具不看场景，学多少都用不上</span>。

### 6.4 写完这篇文章之后项目长什么样

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/38f4c25cbe94ee0367401125aabafb03_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

回头看，写完整套方法论和实操，项目长什么样？

```
项目根/
├── CLAUDE.md                  ← 启动时被 Claude Code 自动读取
├── docs/
│   ├── architecture.svg       ← 架构图
│   ├── module-deps.svg        ← 模块图
│   ├── external-deps.svg      ← 依赖图
│   ├── api-list.md            ← 接口清单
│   └── data-model.md + data-model-er.svg  ← 数据模型
└── .claude/skills/
    └── docs-auto-sync/SKILL.md  ← 被 Claude Code 自动索引
```

`docs/` 里有五份资产，项目根有一份 CLAUDE.md，`.claude/skills/` 里有一个工程师自己挖出来的 SKILL。这就是老项目的 AI 协作基础设施——**<span style="color: red; font-weight: bold;">从"工程师理解了项目"，到"AI 也能理解项目"，再到"AI 能按团队真实流程做事"</span>**。

### 6.5 几个值得自检的问题

读完这一篇，不妨拿两个问题问自己。

**问题一**：你手上项目最让人头疼的"反复做但没沉淀"的流程是什么？这件事符合三特征（可复制、可参数化、可自动化）吗？如果让 AI 帮你出 Top 3 候选 SKILL，会包括它吗？

**问题二**：团队现在有没有"文档腐烂"的问题？`docs/` 里的内容多久没更新了？如果一个 SKILL 能自动同步代码和文档，它能给团队省多少时间？

把这两个问题想清楚，再回到文章里挑一个流程，从三特征判断开始走一遍——这才是 SKILL 真正落地的方式。
