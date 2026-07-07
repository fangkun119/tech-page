---
title: 传统项目迁AI 11：了解项目 - 提炼Skill
author: fangkun119
date: 2026-07-04 11:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
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
传统项目迁AI 11：了解项目 - 提炼Skill
-->

## 1. 导读地图：本篇怎么读

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/e32c7a5ecd432dcb847ba0ad3df28565_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是「了解项目」阶段的第六篇，紧接系列第 10 篇（提炼 CLAUDE.md）。系列第 10 篇结尾留了一句话：**还有一类东西没落地，就是反复要做的操作流程**。这类东西放不进 CLAUDE.md——它不是常识，是操作。改造前的体检、PR 前的检查、文档跟着代码走，这些每次改造都要走一遍的流程，应该住到另一个地方，这个地方就是 **SKILL**。

系列第 7 篇装过一个别人写的 SKILL（画图），那是消费者视角。本篇换角度，从自己的老项目里挖出重复流程，写成第一个可用的 SKILL——这是生产者视角。

学完本篇，项目的 `.claude/skills/` 目录里会多一个实际能用的 SKILL。**项目的 AI 协作基础设施从"别人给的"变成"自己养的"**。

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/32871bbfe0f4c6590fd5f1efa2a99862_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    Start([本篇导读：提炼 SKILL])
    Start --\> P1[第一部分 方法论提炼]
    Start --\> P2[第二部分 实战演示]

    P1 --\> P1A[2、为什么老项目特别需要 SKILL]
    P1 --\> P1B[3、方法论：挖什么<br/>+ 三特征判断法]
    P1 --\> P1C[4、第一部分 Check List<br/>可裁剪速查]

    P2 --\> P2A[5、主线项目延续<br/>Spring AI Alibaba Admin]
    P2 --\> P2B[6、让 AI 帮工程师挖 SKILL<br/>五步实操]
    P2 --\> P2C[7、docs-auto-sync SKILL<br/>完整产出]
    P2 --\> P2D[8、SKILL 上线的三个测试]
    P2 --\> P2E[9、CLAUDE.md 与 SKILL 的分工]
    P2 --\> P2F[10、与后续篇目的衔接]
    P2 --\> P2G[11、小结与思考]

    P1C -.速查.-> Senior([熟练工程师<br/>快速回顾])
    P2A -.代入.-> Beginner([初学工程师<br/>系统掌握])
-->

两类读者的推荐读法：

- **初学 AI 编程工程师**：建议通读全篇。第一部分建立"SKILL = 把反复流程固化成 AI 能自动执行的资产"的认知骨架；第二部分用 Spring AI Alibaba Admin 把骨架落到真实企业级项目上，把挖 SKILL 的五步提示词、产出效果、上线测试完整走一遍。
- **熟练 AI 编程工程师**：可只看第一部分的 Check List 速查；接到一个老项目时，按三特征判断法扫一遍候选清单、让 AI 出 Top 3 推荐、挑一个生成完整 SKILL.md、再过三个上线测试即可。

## 2. 为什么老项目特别需要 SKILL

写 SKILL 这件事，新项目和老项目完全是两回事。理解了这一点，就理解了本篇为什么要把"提炼 SKILL"单独拿出来讲。

### 2.1 老项目是 SKILL 的富矿

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/7d9af9af0ba2c1bc707df96a3cc0f611_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

新项目自己起的，每件事第一次做印象都深，写不写 SKILL 差别不大。

老项目不一样，老项目的特征是"很多事情工程师反复做了很多次，但每次都凭记忆"。改造前体检、PR 前检查、文档同步、新增接口前对齐，这些事每周都做，但因为没沉淀，**每次都得重新想一遍流程**。漏一步、错一步是常态。

更扎心的是：这些流程不只是一个人在做，团队里每个人都在做，每个人做的细节还不一样。A 提 PR 前会跑测试和格式化，B 会顺手 review changelog，Robert 总是先做一次资产校对。三个人的 PR 过 review 的速度差三倍，**原因不是能力差，是流程没标准化**。

SKILL 的价值就在这里：**把这些反复做但没沉淀的流程，固化成一个 AI 能自动执行的资产**。A 装上之后，提 PR 前 AI 自动跑一遍 Robert 的全套流程；B 装上之后，AI 自动同步文档不会忘。整个团队的下限被拉到上限。

老项目恰恰是 SKILL 的富矿，因为这种"反复做但没沉淀"的流程多到挖不完。等挖完，后面的开发就非常顺畅了。

### 2.2 SKILL 的起点是"挖"，不是"设计"

#### (1) 写 SKILL 的起点

讲挖之前先说清一件事：很多人开始写 SKILL 时第一反应是研究 YAML 格式、description 怎么写、allowed-tools 有哪些。这些都是有用的知识，但**它们应该在挖到具体场景之后才学**。

SKILL 的起点是识别"我反复在做某件事"。没有重复流程就硬写 SKILL，产出的是一堆没人用的代码。Claude Code 扫 `~/.claude/skills/` 时每个 SKILL 都会被索引——不是性能问题，是 SKILL 写多了 AI 在多个 SKILL 之间会判断混乱、互相冲突。**所以写之前先想清楚：这件事是不是真的在反复做**。

#### (2) 三特征判断法：值不值得写成 SKILL

怎么判断一件事值不值得写成 SKILL？三个特征判断法：

| 特征 | 含义 | 反例（不满足） |
|------|------|---------------|
| 可复制 | 同样的动作序列会被反复执行 | 偶尔做一次的事 |
| 可参数化 | 只有几个变量在变，骨架是同一个 | 流程每次都不一样 |
| 可自动化 | 动作序列有明确的起点和终点 | 改着改着凭感觉做完了 |

以"新增一个接口"的流程为例：具体接口名不一样、入参出参不一样，**但流程是一样的**（先看 `docs/api-list.md` 里现有接口的路径规范、再看 `data-model.md` 里相关实体、再写实现、再补测试、再回头更新接口清单）。这就是典型的可参数化。

三个特征同时满足，才值得写成 SKILL。差一个都别硬写：偶尔做的事写进文档就好，流程太发散的事留在脑子里就好，做成 SKILL 反而会把 AI 卡在错误的框里。

### 2.3 老项目里哪些流程值得挖

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/7ee173291236576ff0593da25133e814_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

老项目里有四类流程几乎家家都有，挖出来就能用：

| 候选流程 | 触发时机 | 解决的痛点 |
|---------|---------|-----------|
| 技术文档自动更新 | 代码改动后 | 文档腐烂（接口清单、数据模型和真实代码对不上） |
| 改造前体检 | 动手改代码前 | 测试是否绿、编译是否通过、依赖的中间件是否连得上 |
| PR 前检查 | 提 PR 前 | 测试跑过、格式化过、changelog 更新、相关文档改了 |
| 新增接口前对齐 | 加新接口前 | 看现有接口路径风格、统一响应格式、错误码规则 |

#### (1) 文档腐烂：老项目最普遍的痛点

`docs/` 里的接口清单、数据模型、架构图，代码每次改动都让其中某一份漂移。如果不主动同步，三份资产慢慢就和真实代码对不上了——半年后整个 `docs/` 没人敢相信，最后变成"代码即文档"。

这是老项目最常见的痛点：**文档腐烂**。一个 SKILL 把这件事自动化掉，团队再也不用花人力维护文档。

#### (2) 为什么本篇挑"技术文档自动更新"做示范

四个候选都符合三特征：可复制、可参数化、可自动化。本篇挑第一个——**技术文档自动更新**——作为示范动手写。理由很简单：**它解决了老项目最普遍、最让人头疼的问题**。这种 SKILL 的价值，最容易被人直接感受到。

### 2.4 一个老项目到底需要多少个 SKILL

挖之前先说克制。读者可能一下子兴奋起来，想把上面四个场景全写成 SKILL，再加几个自己想到的。**不建议**。

| 数量区间 | 评价 |
|---------|------|
| 5 个以内（推荐） | 大多数老项目够用 |
| 5-10 个 | 复杂系统适用 |
| > 10 个 | 容易出现"一句话匹配多个 SKILL"，AI 判断哪个该触发会迷茫 |

SKILL 数量不代表 AI 协作能力，**写得准、用得勤才是**。

#### (1) 推荐节奏：先挖 3 个，用一个月再扩展

节奏建议：**先挖 3 个最高频的流程写成 SKILL，用一个月，觉得真的有用再扩展**。第一个就按本篇的"技术文档自动更新"写，后面两个留给自己挖。

挖的时候回到三特征判断法：最近一个月在反复做什么？这件事可复制吗？可参数化吗？可自动化吗？**三个都满足，就写**。

不要追求"一开始就写一套完美的 SKILL 体系"。**SKILL 是养出来的，不是设计出来的**。第一版只要能跑、能解决一个具体痛点就够了。

## 3. 方法论：挖什么、怎么挖、怎么判断

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/241a7760ea7a7ef12dea18eab92411b7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二部分实战之前，先把方法论提炼出来。本节回答三个问题：怎么识别值得挖的流程？怎么让 AI 帮忙扫候选清单？挖出来之后怎么挑优先级？

### 3.1 识别：三特征判断法

已在第 2.2 节展开。核心一句话：**可复制 + 可参数化 + 可自动化，三特征同时满足才值得写成 SKILL**。

### 3.2 挖掘：让 AI 扫项目出候选清单

挖 SKILL 这件事本身不是手动一条条想——**让 AI 帮忙扫一遍项目，给一份候选清单，然后工程师来选哪个先做**。

提示词（本篇第二部分第 6.1 节会逐字给出）的核心要求：

- 让 AI 扫描 `git log`、`CLAUDE.md`、`docs/`、`README`、`CONTRIBUTING`、`.github/`
- 用三特征做筛选标准
- 输出表格：流程名、为什么是反复的、能参数化的部分、起点和终点

### 3.3 排序：Top 3 推荐的标准

候选清单可能 5-10 项。接下来让 AI 按三个维度排序挑出 Top 3：

| 维度 | 含义 |
|------|------|
| 频率高 | 这件事多久做一次（每天/每周/每月） |
| 痛点深 | 不做这件事会带来多大麻烦 |
| 自动化收益大 | 一个 SKILL 能省下多少人力 |

三维度同时高的，就是第一个该挖的 SKILL。本篇挑的"技术文档自动更新"就是三维度都满分的典型。

## 4. 第一部分 Check List：提炼 SKILL 速查

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/01d38b3af3916aa5514f29e4f3677794_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节提供一份可裁剪、可勾选的 Check List，供工程师在提炼 SKILL 的不同阶段快速查阅。读者可以按项目实际情况裁剪后贴到自己的工作笔记里。

### 4.1 识别候选流程的 Check List

启动挖 SKILL 之前，逐条自检：

- [ ] 是否回到三特征判断法，问自己"最近一个月在反复做什么"？
- [ ] 这件事是否满足**可复制**（同样动作序列反复执行，不是偶尔一次）？
- [ ] 这件事是否满足**可参数化**（只有几个变量在变，骨架是同一个）？
- [ ] 这件事是否满足**可自动化**（有明确的起点触发信号和终点产出物）？
- [ ] 三个特征是否**同时满足**（差一个就别硬写）？

### 4.2 让 AI 扫候选清单的 Check List

让 AI 扫项目出候选清单环节，逐条自检：

- [ ] 提示词是否让 AI 扫描了 `git log`、`CLAUDE.md`、`docs/`、`README`、`CONTRIBUTING`、`.github/`？
- [ ] 是否明确以三特征作为筛选标准（可复制、可参数化、可自动化）？
- [ ] 是否要求 AI 每个候选写明"流程名、为什么是反复的、能参数化的部分、起点和终点"？
- [ ] 是否要求 AI 用表格总结？

### 4.3 排序挑 Top 3 的 Check List

让 AI 排序环节，逐条自检：

- [ ] 是否明确三个优先级维度：频率高、痛点深、自动化收益大？
- [ ] 是否要求 AI 给每个候选写明 `name`（英文）、`description`、预期 `steps`、`allowed-tools`？
- [ ] Top 3 中是否大概率包含"技术文档自动更新"这一类（三维度都高）？
- [ ] 是否用表格总结（含类型和理由）？

### 4.4 生成完整 SKILL.md 的 Check List

让 AI 生成完整 SKILL 环节，逐条自检：

- [ ] 提示词是否指定了 SKILL 的 `name`？
- [ ] 是否要求 `description` 写清楚什么场景触发、产出是什么？
- [ ] 是否要求 `steps` 清晰可执行？
- [ ] 是否要求 `allowed-tools` 限制到最小？
- [ ] 是否明确"只汇报不一致，不要自动改文件，让人决定怎么处理"（对文档同步类 SKILL）？
- [ ] 是否要求保存到 `.claude/skills/{name}/SKILL.md` 标准目录？

### 4.5 上线前测试的 Check List

SKILL 写完，三个测试动作必做，逐条自检：

- [ ] **测试一**：说一句应该匹配的话，验证 SKILL 是否被自动加载？
- [ ] **测试二**：说一句故意不匹配的话，验证 SKILL 是否不被错误加载？
- [ ] **测试三**：真跑一次，检查是否按 `steps` 走、产出是否符预期、有没有自作主张改文件？
- [ ] 三个测试都过了，SKILL 才算上线。

### 4.6 数量与节奏的 Check List

挖 SKILL 这件事的节奏自检：

- [ ] 是否克制了"一次性把所有候选都写成 SKILL"的冲动？
- [ ] 第一个月是否控制在 3 个最高频的 SKILL 以内？
- [ ] 是否用了一个月之后再决定要不要扩展？
- [ ] 总数是否控制在 5 个以内（视系统复杂度）？

## 5. 实战主线延续：Spring AI Alibaba Admin

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/051e9a6029faf60f1ecb3027870020af_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

说完方法论，进入第二部分实战。本篇延续系列前几篇的主线项目——**Spring AI Alibaba Admin**，所有出现的提示词、产出的 SKILL、上线测试都围绕它展开。

### 5.1 主线延续：从五份资产 + CLAUDE.md 到 SKILL

系列前几篇走完后，Spring AI Alibaba Admin 的项目根目录已经住了：

```
项目根/
├── CLAUDE.md                  ← 系列第 10 篇产出（启动时被 Claude Code 自动读取）
└── docs/
    ├── architecture.svg       ← 架构图（系列第 8 篇产出）
    ├── module-deps.svg        ← 模块图（系列第 8 篇产出）
    ├── external-deps.svg      ← 依赖图（系列第 8 篇产出）
    ├── api-list.md            ← 接口清单（系列第 9 篇产出）
    └── data-model.md + data-model-er.svg  ← 数据模型（系列第 9 篇产出）
```

**CLAUDE.md 告诉 AI 这是什么项目，但还没告诉 AI "怎么做特定的事"**——这就是本篇要补上的另一类资产。

### 5.2 本篇产出物：.claude/skills/ 下的第一个 SKILL

提炼完本篇会拿到一份新的资产：**`.claude/skills/docs-auto-sync/SKILL.md`**。这份 SKILL 让 AI 在代码改动后自动比对接口清单和数据模型，产出不一致清单（不自动改文件，由人决定如何处理）。

### 5.3 本篇只读资产，不跑项目

延续前几篇的约定：本篇仍然只让 AI 读项目里的代码和文档、产出 SKILL。把"让项目跑起来"这件事留到系列第 13 篇（构建护栏阶段）一次性讲透。

## 6. 让 AI 帮工程师挖 SKILL：五步实操

理论讲完，开始挖。这一节给出五步实操，每一步都附完整提示词（code block 原文不改写）、产出效果、关键点说明。读者可以直接复制提示词到自己的项目运行。

### 6.1 第一步：让 AI 分析项目重复流程


<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/ed5bc044cfb2c68523b3774f0cdb0261_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

提示词：

```
扫一下当前项目（包括 git log、CLAUDE.md、docs/、README、CONTRIBUTING、
.github/），找出团队反复在做的操作流程。

判断标准是三特征：可复制、可参数化、可自动化。三个都满足才算值得做 SKILL 的候选。

把找到的候选列出来，每个写明：流程名、为什么是反复的、能参数化的部分是什么、
起点和终点是什么。最后给我用一个表格总结。
```

#### (1) 跑完会拿到一份候选清单

跑完会拿到一份清单，可能 5-10 项，这是这个项目里所有候选 SKILL 的集合。运行后 Spring AI Alibaba Admin 这个项目生成了四个点。第一眼就能看到 CRUD 脚手架，这也是工程师最经常遇到的。

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/74d6c26cad187b5550e3717c3c382ad1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/11_了解项目_06：提炼Skill/74d6c26cad187b5550e3717c3c382ad1_MD5.jpg
用途：展示 AI 扫描 Spring AI Alibaba Admin 项目后产出的候选 SKILL 清单
内容：表格形式列出 4 个候选流程（含 CRUD 脚手架、技术文档自动更新等），每个写明流程名、为什么是反复的、能参数化的部分、起点和终点
-->

### 6.2 第二步：让 AI 出 Top 3 推荐

提示词：

```
从上面的清单里挑 3 个最高优先级的，给我做成候选 SKILL。
每个候选写：name（英文）、description、预期 steps、allowed-tools。
优先级判断标准：频率高、痛点深、自动化收益大。用表格总结，包含类型和理由。
```

#### (1) Top 3 大概率包含"技术文档自动更新"

跑完会拿到三个候选。预期：**Top 3 大概率会包含"技术文档自动更新"这一类**——因为它的频率最高、痛点最深（文档腐烂）、自动化收益最大（一个 SKILL 替代每周几小时人力维护）。下图中第二个就是文档。

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/7ece2da34ab6fb5d680ef703456df466_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/11_了解项目_06：提炼Skill/7ece2da34ab6fb5d680ef703456df466_MD5.jpg
用途：展示 AI 从候选清单中挑选的 Top 3 SKILL 推荐结果（含 CRUD 脚手架、技术文档自动更新等）
内容：表格形式展示 Top 3 候选 SKILL，每个包含 name（英文）、description、预期 steps、allowed-tools，以及类型和优先级理由；第二个为"技术文档自动更新"
-->

### 6.3 第三步：让 AI 生成完整的 CRUD SKILL

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/961760914fcec8a5abb2b66954b61826_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

提示词：

```
生成代码中 CRUD 的 SKILL。
注意按照标准格式和放在标准目录。
结果放到 .claude/skills/ 目录中。
```

这句话的提示词基本就是通用的，读者复制即可。

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/cd5feccda93d40eeaf940855be59b769_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/11_了解项目_06：提炼Skill/cd5feccda93d40eeaf940855be59b769_MD5.jpg
用途：展示 AI 在 .claude/skills/ 目录中生成 CRUD SKILL 的实际产出效果
内容：截图展示 AI 基于提示词生成的 CRUD SKILL 文件，包括 name、description、steps、allowed-tools 等字段的标准格式
-->

### 6.4 第四步：生成技术文档自动更新的 SKILL

提示词：

```
基于上面的候选，给我生成完整的 SKILL.md。要求：
- 名字 docs-auto-sync
- description 写清楚什么场景触发、产出是什么
- steps 清晰可执行
- allowed-tools 限制到最小
- 重要：只汇报不一致的地方，不要自动改文件，让人决定怎么处理

保存到 .claude/skills/docs-auto-sync/SKILL.md。
```

#### (1) AI 给出的完整产出

AI 给出的内容大概长这样（这是一个跑出来的版本，做参考）——这部分作为 SKILL 的核心知识，下一节（第 7 章）完整展开。

## 7. docs-auto-sync SKILL：完整产出

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/9233efc7866b69e442faff10be1f307b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节给出第四步 AI 生成的完整 `docs-auto-sync` SKILL 内容。读者可以对照这份产出，在自己的项目里检查 AI 生成的是否符合规范。code block 原文不改写。

### 7.1 SKILL 整体描述

SKILL 的开头描述部分：

`````markdown
对照代码（Controller、Entity、SQL）与文档（`docs/api-list.md`、`docs/data-model.md`）做交叉比对，输出不一致清单，**不自动修改任何文件**，由人决定如何处理。

- 新增或修改了 Controller（接口变更、路径变更、参数变更）
- 新增或修改了 Entity 类或 SQL 表定义（字段变更、新表、删表）
- 怀疑文档与代码已经偏移，想做一次全量对齐检查
- PR review 前确认文档是否跟上了代码变更

一份结构化差异报告，分两节：

1\. **接口差异**（代码 vs `docs/api-list.md`）：新增接口、删除接口、路径/方法变更、入参/返回类型变更

2\. **数据模型差异**（Entity/SQL vs `docs/data-model.md`）：新增表/实体、删除表/实体、字段增删、类型变更、枚举值变更

每条差异标注：来源文件 + 行号、当前代码实际值、文档记录值、建议动作（更新文档 / 核实代码 / 忽略）。
`````

### 7.2 调用方式与参数

SKILL 的调用方式和参数表：

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

### 7.3 工具权限

SKILL 允许使用的工具列表：

`````markdown
`Read`、`Bash`（仅用于 `find` / `grep` 定位文件）

**不使用** `Write`、`Edit`、`Agent`。
`````

**关键考量：** allowed-tools 限制到最小

注意 `allowed-tools` 里**只有 `Read` 和 `Bash`（限定 find/grep）**——没有 `Write`、`Edit`、`Agent`。这就是提示词中"只汇报不一致的地方，不要自动改文件"在工具权限层面的兜底。**即使 AI 想改文件也改不了，因为工具权限根本没给它**。

### 7.4 执行步骤：参数解析

SKILL 执行步骤的第一段——参数解析：

`````markdown
When the user runs `/docs-auto-sync [targetModule] [docTarget]`:

1\. `targetModule` 默认 `all`；`docTarget` 默认 `both`
2\. 若 `targetModule` 不是 `all`，后续所有 find/grep 限定到包含该模块名的文件
`````

### 7.5 执行步骤：扫描 Controller 提取 endpoint

SKILL 执行步骤的第二段——扫描 Controller：

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

### 7.6 执行步骤：扫描 Entity 和 SQL

SKILL 执行步骤的第三段——扫描 Entity 和 SQL：

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

### 7.7 执行步骤：解析现有文档

SKILL 执行步骤的第四段——解析 `docs/` 下的两份文档：

`````markdown
```bash
```
用 `Read` 工具读取两个文档，解析出：

- api-list.md：每个模块的接口列表（方法 + 路径 + 入参说明 + 返回说明）
- data-model.md：每张表的字段列表（字段名 + 类型 + 说明）
`````

### 7.8 执行步骤：接口比对规则

SKILL 执行步骤的第五段——接口比对：

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

### 7.9 执行步骤：数据模型比对规则

SKILL 执行步骤的第六段——数据模型比对：

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

### 7.10 执行步骤：报告格式

SKILL 执行步骤的第七段——产出报告的格式要求：

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

### 7.11 执行步骤：报告后处理与注意事项

SKILL 执行步骤的最后两段——报告后处理 + 边界注意事项：

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

**关键考量**：差异数为 0 时也要明确反馈

注意"若差异数为 0，输出'文档与代码完全一致，无需更新'"这一条。**SKILL 不是只在出问题时才有用——它跑完没问题，也要明确告诉用户没问题**，否则用户不知道是没跑还是真没问题。

## 8. SKILL 上线的三个测试

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/b37d3a633df57810a7a67c1bae1bda07_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

写完 SKILL 最容易犯的错是放那儿不验证。**三个测试动作必做**，对应第一部分 Check List 4.5 节的速查项。

### 8.1 测试一：说一句应该匹配的话

说一句应该匹配的话：

```
我刚改完一批 Controller，帮我看看文档还对不对得上
```

Claude Code 应该**自动加载 docs-auto-sync 并按步骤跑**。如果没被加载，说明 `description` 写得太窄或者关键词没覆盖到这句话。

### 8.2 测试二：说一句故意不匹配的话

说一句故意不匹配的话：

```
帮我检查一下这段代码
```

这句话和文档同步无关，**SKILL 不应该被加载**。如果 AI 错误加载了，说明 `description` 太宽泛——需要收紧到只在"文档/代码对齐"这个场景触发。

### 8.3 测试三：真跑一次看产出

真跑一次，看输出符不符合预期。检查清单：

| 检查项 | 通过标准 |
|-------|---------|
| 步骤完整性 | 是不是按 SKILL 里写的那些步骤走了 |
| 差异具体性 | 是不是列了具体的不一致点（含代码位置、文档位置、建议动作） |
| 工具越权检查 | 有没有自作主张改文件（应该没有，因为 `allowed-tools` 没给 Write/Edit） |

三个测试过了，SKILL 才算上线。

## 9. CLAUDE.md 与 SKILL 的分工

到这里第二部分的核心都讲完了。回头看看系列第 10 篇和本篇（系列第 11 篇）的关系。

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/cb9ca823deac8b7eb579173e12211c33_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/11_了解项目_06：提炼Skill/cb9ca823deac8b7eb579173e12211c33_MD5.jpg
用途：对比 CLAUDE.md（系列第 10 篇）和 SKILL（系列第 11 篇）的分工与对称关系
内容：左右对称结构图——左侧 CLAUDE.md（怎么写：静态知识、项目是什么、AI 启动共识），右侧 SKILL（怎么挖：动态能力、怎么做特定的事、操作流程固化）
-->

### 9.1 两篇对称

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/96ff3a89033df03dfbcfcd43af215d5f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

两篇对称：**系列第 10 篇讲"怎么写"（CLAUDE.md 的常识），本篇讲"怎么挖"（SKILL 的流程）**。

| 资产 | 回答的问题 | 性质 |
|------|----------|------|
| CLAUDE.md | 这是什么项目？AI 启动要知道什么？ | 静态知识 |
| SKILL | 怎么做特定的事？某个反复流程怎么走？ | 动态能力 |

#### (1) 一起看才完整

一起看才完整：**CLAUDE.md 告诉 AI 这是什么项目，SKILL 告诉 AI 怎么做特定的事**。前者是静态知识，后者是动态能力。

### 9.2 60 分起步，养到 90 分

SKILL 不是一次写完美的。**AI 帮工程师生成的第一版大概只能到 60 分**。不要追求第一版就完美，追求"能跑、能解决一个具体痛点"就够了。

| 阶段 | 分数 | 怎么做 |
|------|------|-------|
| AI 初版 | 60 分 | 让 AI 基于提示词生成 |
| 手动微调 | 70-80 分 | 调 `description` 让触发更精准、改 `steps` 按团队真实流程走、收紧 `allowed-tools` 避免越权（约一两个小时） |
| 实战打磨 | 80+ 分 | 用一个月，每次实际触发中迭代——发现误触发就收紧 `description`、发现漏触发就扩展场景、发现步骤不对就调 `steps` |
| 持续迭代 | 90+ 分 | 半年后 SKILL 真正成熟，成为团队标配资产 |

**SKILL 是养出来的**

SKILL 是养出来的，不是写出来的。**新人入职装上就能用，老人改造时不用每次想流程**——这才是 SKILL 成熟的标志。

### 9.3 不要堆工具，要在场景里用工具

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/eb6247ae0d698dc8b590780a8a72b673_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本模块（「了解项目」阶段）快结束了，再强调下本系列的"道"。

#### (1) 回想系列第 4 篇的工具全景图

回想系列第 4 篇给的那张工具全景图，那篇说过：**工具箱要全，但工程师不需要每天深度研究每个工具**。只要知道地图、知道最好用的几件工具的用法、其他大概有什么、用在哪里。本篇就是这个思想的具体演绎。

#### (2) 场景驱动，不是工具驱动

团队的"文档腐烂"问题——这是**场景**、是**事**。需要"自动同步文档"的方案——这是**工具**、是 SKILL。打开工具箱发现 SKILL 这个东西，刚好能用。

| 思维方式 | 表现 | 结果 |
|---------|------|------|
| 工具驱动 | "我要学 SKILL"才学 SKILL | 学完用不上 |
| 场景驱动 | "我有这个问题"才用 SKILL | 工具发挥价值 |

整门系列从系列第 4 篇铺武器库地图、到系列第 6 篇八步心法、到本篇挖 SKILL，这条线就是想告诉读者：**学 AI 编程不是堆工具的过程，是建立"场景驱动用工具"的思维方式**。

#### (3) 王兴的那句话

美团创始人王兴有句经典的话：**和高人聊，从书上学，在事上练**。一定要在"事上练"——工具是死的，场景是活的。**带着场景去用工具，工具才有价值；盯着工具不看场景，学多少都用不上**。

## 10. 与后续篇目的衔接

SKILL 上线之后，项目的 AI 协作基础设施基本成型。但还有一件事要交代清楚——本篇在整个系列中的位置。

### 10.1 本篇结束时的资产清单

写完本篇，`docs/` 里有五份资产、项目根目录有一份 CLAUDE.md、**`.claude/skills/` 里有一个工程师自己挖出来的 SKILL**。

```
项目根/
├── CLAUDE.md                  ← 系列第 10 篇产出（启动时被 Claude Code 自动读取）
├── docs/
│   ├── architecture.svg       ← 架构图（系列第 8 篇产出）
│   ├── module-deps.svg        ← 模块图（系列第 8 篇产出）
│   ├── external-deps.svg      ← 依赖图（系列第 8 篇产出）
│   ├── api-list.md            ← 接口清单（系列第 9 篇产出）
│   └── data-model.md + data-model-er.svg  ← 数据模型（系列第 9 篇产出）
└── .claude/skills/
    └── docs-auto-sync/SKILL.md  ← 本篇产出（被 Claude Code 自动索引）
```

这就是老项目的 AI 协作基础设施——**从"工程师理解了项目"到"AI 也能理解项目"，再到"AI 能按团队真实流程做事"**。

### 10.2 SKILL 在后续每一篇的作用

SKILL 不会摆在 `.claude/skills/` 里吃灰，**它是后面每一篇改造工作的 AI 入口**：

| 后续篇目 | SKILL 的作用 |
|---------|-------------|
| 系列第 12 篇（第二部分实操课） | 把系列第 8-11 篇的提示词全部串起来，在 Spring AI Alibaba Admin 上跑一遍完整流程，SKILL 是其中"操作流程固化"的环节 |
| 系列第 13 篇（构建护栏） | 改造前体检类的 SKILL 会自动跑，确认测试是否绿、编译是否通过 |
| 系列第 14 篇（建护栏） | docs-auto-sync 类 SKILL 会在 PR review 时自动跑，确认文档是否跟上了代码 |
| 第四部分（做需求改造） | 各类 SKILL 在改造流程的对应环节自动触发 |

### 10.3 第二部分到此完结

本篇是「了解项目」阶段的最后一篇。从系列第 6 篇到本篇（系列第 11 篇），八步心法的前六步全部讲完：

| 篇目 | 主题 | 产出 |
|------|------|------|
| 系列第 6 篇 | 八步心法 + 从 README 到核心链路 | 心法骨架 + 项目初印象 |
| 系列第 7 篇 | 绘图工具 | 画图能力 |
| 系列第 8 篇 | AI 绘图俯视项目全景 | 三张图（架构图、模块图、依赖图） |
| 系列第 9 篇 | 生成接口清单和数据模型 | 两份清单 |
| 系列第 10 篇 | 提炼 CLAUDE.md | 项目根目录的 CLAUDE.md |
| 本篇（系列第 11 篇） | 提炼 SKILL | `.claude/skills/` 下的第一个 SKILL |

下一篇是第二部分的实操课，会把系列第 8-11 篇的提示词全部串起来，在 Spring AI Alibaba Admin 上跑一遍完整流程。第三部分就进入编译运行和护栏了。

## 11. 小结与思考

<img src="imgs/aicmigr-11-proj-rd-06-distill-skill/ecba411e684b1aaa8e93f5f52286cd57_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 11.1 小结

本篇核心就一件事：**从老项目里挖出一个重复流程，写成第一个 SKILL**。

#### (1) 老项目和 SKILL 天然契合

老项目和 SKILL 天然契合：老项目里"反复做但没沉淀"的流程多得挖不完，**SKILL 让团队的下限被拉到上限**。

#### (2) 挖的方法：三步走

挖的方法：**让 AI 扫项目找候选清单 → 让 AI 出 Top 3 推荐 → 选一个让 AI 生成完整 SKILL.md**。三步走，比手动想快十倍。

#### (3) 挑的示范：技术文档自动更新

本篇挑的是"技术文档自动更新"，**解决文档腐烂这个老项目通病**。这个 SKILL 的价值一旦用上就回不去了。

#### (4) 不要贪多：5-10 个够用

挖完不要贪多。**5-10 个够用，先挖 3 个用一个月**。

#### (5) 不要追求完美：60 分起步养到 90 分

写完不要追求完美。**60 分起步，养到 90 分。SKILL 是养出来的，不是设计出来的**。

### 11.2 思考

#### (1) 关于手上项目的自检

读者手上项目最让人头疼的"反复做但没沉淀"的流程是什么？这件事符合三特征（可复制、可参数化、可自动化）吗？**如果让 AI 帮工程师出 Top 3 候选 SKILL，会包括它吗？**

#### (2) 关于团队现状的检验

团队现在有没有"文档腐烂"的问题？`docs/` 里的内容多久没更新了？**如果一个 SKILL 能自动同步代码和文档，它能给团队省多少时间？**
