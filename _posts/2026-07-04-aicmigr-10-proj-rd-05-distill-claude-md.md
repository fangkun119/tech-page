---
title: 传统项目迁AI 10：了解项目 - 提炼CLAUDE.md
author: fangkun119
date: 2026-07-04 10:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-10-proj-rd-05-distill-claude-md/cover.jpg
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
aicmigr-10-proj-rd-05-distill-claude-md
传统项目迁AI 10：了解项目 - 提炼CLAUDE.md
-->

## 1. 问题：AI 一进老项目就乱动

<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/529420f7db6b713df3af6430a6e51029_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接手一个老项目，第一次把 AI 拉进来改造，它往往连"哪些地方不能碰"都不知道，就冲过去改代码、重命名、顺手"优化"一通。它不知道某个字段被外部 SDK 依赖，不知道某段看着乱的历史代码正兜着三家老 API，更不知道改一行配置会让灰度发布直接报错。改造还没开始，AI 已经替你埋了一圈雷。

<span style="color: red; font-weight: bold;">问题不在于 AI 不聪明，而在于它对这个项目一无所知——它每次启动，都是空着脑子进来的。</span>

换个角度想：传统软件里，新员工入职第一天会拿到什么？不是一整套架构文档全集，不是所有接口清单，不是公司代码规范手册，而是一份**项目入职手册**——几十页纸，告诉新人"<span style="color: red; font-weight: bold;">这是什么项目、核心架构长什么样、关键模块在哪、哪些地方绝对不能动、哪些奇怪代码有历史原因</span>"。`CLAUDE.md` 对 AI 起的就是这个作用：它放在项目根目录，Claude Code 启动时自动读取，<span style="color: red; font-weight: bold;">让 AI 每次都带着项目共识进来，而不是凭空猜。</span>

但老项目的入职手册不能凭空写。新人版的项目入职手册是团队多年沉淀出来的；老项目的 `CLAUDE.md` 也一样——它必须<span style="color: red; font-weight: bold;">从前期的项目资产里提炼：架构图、模块图、依赖图、接口清单、数据模型</span>这些已经画好的俯视图，是 `CLAUDE.md` 的前置条件，没有它们，写出来的就是空中楼阁。

本篇讲怎么从这些资产里提炼出一份合格的 `CLAUDE.md`，特别是其中两节 AI 永远写不出、必须工程师自己手写的硬核内容。

## 2. 心智模型：CLAUDE.md 是 AI 的项目入职手册

<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/eb44b9cdb24ce1b85926318955204038_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

一句话先把结论立住：<span style="color: red; font-weight: bold;">CLAUDE.md 是 AI 的项目入职手册</span>。后面所有写法、所有检查、所有红线，都从这个心智模型推导出来。

### 2.1 类比：CLAUDE.md ≈ 项目入职手册

回到传统软件里那个熟悉的场景。新员工入职第一天，桌上摆的是什么？不是一整套架构文档全集，不是所有接口清单，不是公司代码规范手册，而是一本薄薄的**项目入职手册**——这是什么项目、核心架构长什么样、关键模块在哪、哪些地方绝对不能动。新人读完就能开始干活，剩下的细节边做边查。

CLAUDE.md 对 AI 起的就是这个作用。规范全集、架构文档全集、接口清单全集，这些都属于 `docs/` 下的资产，不是 CLAUDE.md 该背的内容。CLAUDE.md 只承担两个角色：<span style="color: red; font-weight: bold;">索引</span>和<span style="color: red; font-weight: bold;">常识</span>。

### 2.2 老项目最常见的错误：写得太多

老项目改造里最常见的写法错误，是写得太多。

你可能会问：写多点不更全吗？恰恰相反。三种典型表现：

- 把架构图的内容用文字重抄一遍
- 把接口清单全塞进去
- 把数据模型每张表每个字段都列出来

结果 CLAUDE.md 几千行，AI 每次启动都加载一大堆 context，context 太满，AI 反而抓不住重点——这种状态有个术语叫 <span style="color: red; font-weight: bold;">Dumb Zone（context 过载区）</span>。掉进 Dumb Zone 的 AI，重点全糊在一起，等于没读。更糟的是，<span style="color: red; font-weight: bold;">手写内容一旦和原项目规范出现出入，基本是百分百会和原规范形成冲突</span>，AI 不知道该信哪个。

### 2.3 正确定位：索引 + 常识

回到老项目里 CLAUDE.md 的正确定位：**索引 + 常识，而不是大量的约束**。

| 角色  | 职责                         |
| --- | -------------------------- |
| 索引  | <span style="color: red; font-weight: bold;">指向已提炼出来的详细内容（`docs/` 下的资产）</span> |
| 常识  | <span style="color: red; font-weight: bold;">让 AI 一启动就知道关键信息</span>            |

索引指向 `docs/` 下已经提炼好的资产，常识让 AI 一启动就带着项目共识进来。<span style="color: red; font-weight: bold;">两条职责以外的内容，下放到 `docs/`，不要塞进 CLAUDE.md。</span>

量化红线只有一条：<span style="color: red; font-weight: bold;">CLAUDE.md 超过 300 行就是写多了</span>。这条红线后续 review 时会再次用到，但论证只在这里讲一次。

## 3. 为什么老项目的 CLAUDE.md 不能凭空写

<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/4e807c7cac32ab17372f301441075040_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

写 CLAUDE.md 这件事，新项目和老项目完全是两回事。理解了这一点，就理解了为什么老项目的 CLAUDE.md 必须单独当成一件事来办。承接第 2 章的入职手册类比：新项目你给自己写入职手册，老项目你是在替别人补写入职手册。

### 3.1 新项目：经验就够了

新项目写 CLAUDE.md 不难。有个常用技巧：参考阿里巴巴 Java 规范这样的成熟规范生成约束条款，再按自己的项目风格调一调就能用。

<span style="color: red; font-weight: bold;">因为工程师就是作者，代码是自己写的，规矩是自己定的</span>，每一处约束脑子里都清楚，CLAUDE.md 凭经验就能写出来。

### 3.2 老项目：经验会骗人

那老项目凭什么写？

老项目不一样。<span style="color: red; font-weight: bold;">工程师是接手者，项目不是自己写的，很多设计决策背后的原因自己都没搞清楚</span>。这种情况下凭什么写 CLAUDE.md？凭五天前刚读的代码？凭零散的印象？写出来的东西通常会落入三种陷阱：

| 陷阱 | 表现 | 后果 |
|-----|-----|-----|
| <span style="color: red; font-weight: bold;">空洞</span> | "这是一个 Spring Boot 项目" | AI 等于没读 |
| <span style="color: red; font-weight: bold;">错误</span> | 把猜的当成事实 | AI 被误导，给出错误建议 |
| <span style="color: red; font-weight: bold;">遗漏</span> | 没写出最关键的那几条禁区 | 改造时踩雷 |

<span style="color: red; font-weight: bold;">空洞让 CLAUDE.md 等于没写，错误比不写更糟，遗漏则是改造时最危险的一种——AI 不知道禁区在哪，就敢一头扎进去。</span>

### 3.3 正确姿势：从资产里提炼，不是从零写

老项目写 CLAUDE.md 的正确姿势不是从零写，<span style="color: red; font-weight: bold;">是从已有资产里提炼</span>。前期先把项目摸清——画出架构图、模块图、依赖图，做出接口清单和数据模型。<span style="color: red; font-weight: bold;">这五份资产不只是给自己看的笔记，它们是 CLAUDE.md 的前置条件</span>。有了它们，CLAUDE.md 才有得提炼；<span style="color: red; font-weight: bold;">没有它们，CLAUDE.md 就是空中楼阁</span>。

本篇要做的，就是把前期摸清项目的积累，凝成一份 AI 每次启动都能看到的项目常识。

## 4. 放什么、不放什么

<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/d17871b4928b8174da3d2d2ed93cf320_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第 2 章把 CLAUDE.md 定位成"索引 + 常识"。落到实操，就是两张清单：放七类、不放五类。读者可以直接对着自己的项目填——这两张表本质上是给 AI 的入职手册目录页。

### 4.1 放什么：七类

放进来的内容，按实战经验归为这七类：

| 类别   | 写法                                   | 示例                                                                                       |
| ---- | ------------------------------------ | ---------------------------------------------------------------------------------------- |
| <span style="color: red; font-weight: bold;">项目定位</span> | 一句话说清楚                               | "Spring AI Alibaba Admin 是阿里巴巴开源的 Agent 管理平台，提供 Prompt 管理、Dataset、Evaluator、Trace 观测等能力" |
| <span style="color: red; font-weight: bold;">核心架构</span> | 一段话 + 指向 `docs/architecture.svg` 的链接 | 不要把图里的内容文字化重写                                                                            |
| <span style="color: red; font-weight: bold;">关键模块</span> | 一个小表，每个模块一句话职责                       | 详细依赖关系在 `module-deps.svg` 里                                                              |
| <span style="color: red; font-weight: bold;">关键约定</span> | 硬规则，不展开理由                            | "所有 REST 接口响应统一包装 Result"、"数据库字段一律用 snake_case，Java 字段用 camelCase"                       |
| <span style="color: red; font-weight: bold;">怎么跑</span>  | 一句话 + 指向 `docs/` 运行文档的链接             | 单独做运行文档                                                                                  |
| <span style="color: red; font-weight: bold;">禁区</span>   | 老项目的灵魂一节                             | 第 6 章单独讲                                                                                 |
| <span style="color: red; font-weight: bold;">历史包袱</span> | 老项目的灵魂另一节                            | 第 6 章单独讲                                                                                 |

后两类（禁区、历史包袱）是老项目 CLAUDE.md 的灵魂，第 6 章单独讲。

### 4.2 不放什么：五类

不放的内容同样按经验归为五类：

| 不放     | 原因                                         |
| ------ | ------------------------------------------ |
| <span style="color: red; font-weight: bold;">完整架构细节</span> | 那是 `architecture.svg` 的事                   |
| <span style="color: red; font-weight: bold;">完整接口清单</span> | `api-list.md` 就在 `docs/` 里，不要重复            |
| <span style="color: red; font-weight: bold;">完整数据模型</span> | `data-model.md` 就在 `docs/` 里，不要重复          |
| <span style="color: red; font-weight: bold;">通用代码规范</span> | 阿里 Java 开发手册这种东西不是项目特有的，进 CLAUDE.md 只会稀释重点 |
| <span style="color: red; font-weight: bold;">背景故事</span>   | AI 不需要读一篇项目诞生史才能工作                         |

### 4.3 一句话核心原则

把上面两张清单压成一句话：

<span style="color: red; font-weight: bold;">CLAUDE.md 里的每一条，要么是"AI 启动就必须知道的常识"，要么是"指向 docs/ 的入口"。不满足这两条的，删掉。</span>

这是全文最硬的判断标准。写完初稿后，对着这条原则逐条审：<span style="color: red; font-weight: bold;">既不是常识、也不是入口的，直接删</span>。这张对比图把"放"和"不放"的边界画得更直观：

<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/92a065281f6fa7adf594ff9ffb100311_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/10_了解项目_05：提炼CLAUDE.md/92a065281f6fa7adf594ff9ffb100311_MD5.jpg
用途：直观对比"放进 CLAUDE.md 的内容"与"不放进 CLAUDE.md 的内容"，强化索引+常识的定位
内容：左右对比图——左侧列出应放进 CLAUDE.md 的七类内容（项目定位、核心架构、关键模块、关键约定、怎么跑、禁区、历史包袱），右侧列出不应放进的内容（完整架构、完整接口清单、完整数据模型、通用代码规范、背景故事）
-->

## 5. 让 AI 生成前五节初稿

<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/ef0701c605493cb91d0fc2a1d7953f63_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

定位（第 2 章）和清单（第 4 章）都讲清楚了，接下来把方法落到实操——套用一段提示词，让 AI 一次性产出 CLAUDE.md 的前五节初稿。主线项目 Spring AI Alibaba Admin 在本节引入，并贯穿后续章节。

### 5.1 主线项目：Spring AI Alibaba Admin

主线项目沿用至今：**Spring AI Alibaba Admin**——阿里巴巴开源的 Agent 管理平台，提供 Prompt 管理、Dataset、Evaluator、Trace 观测等能力。本文后续所有提示词、产出的 CLAUDE.md、写出的禁区/历史包袱都围绕它展开。

积累完成后，它的 `docs/` 目录下已经住进五份资产：

```
docs/
├── architecture.svg          ← 架构图
├── module-deps.svg           ← 模块图
├── external-deps.svg         ← 依赖图
├── api-list.md               ← 接口清单
└── data-model.md + data-model-er.svg  ← 数据模型
```

<span style="color: red; font-weight: bold;">这五份资产只能告诉工程师和 AI"项目长什么样"，但还没告诉 AI"每次启动都要带着这些共识进来"</span>——这正是本篇接下来要解决的问题。

本篇只读资产、不跑项目，"让项目跑起来"留到后续构建护栏阶段一次性讲透。

### 5.2 提示词

<span style="color: red; font-weight: bold;">提示词</span> = 给 AI 的入职手册生成指令。把这段直接复制到 Claude Code 里运行：

```
读 docs/ 下的所有资产，给我生成一份 CLAUDE.md 初稿。

精简：项目定位、核心架构、关键模块、关键约定、怎么跑，

外加两节空着的：禁区、历史包袱。

架构图、接口清单、数据模型的详细内容不要复制进来，

用链接指向 docs/ 就好。保存到项目根目录的 CLAUDE.md。
```

### 5.3 三个关键点

这段提示词看着朴素，每一句都对应一个关键点：

| 关键点         | 作用                       | 决定了什么               |
| ----------- | ------------------------ | ------------------- |
| <span style="color: red; font-weight: bold;">让 AI 基于资产提炼</span> | "读 docs/ 下所有资产"让 AI 不凭空写 | CLAUDE.md 的可信度      |
| <span style="color: red; font-weight: bold;">用链接而不是复制</span>    | "用链接指向 docs/ 就好"防止文字化    | CLAUDE.md 的精简度      |
| <span style="color: red; font-weight: bold;">禁区/历史包袱留空</span>   | 让 AI 留位置但不填，瞎编或写泛        | 老项目 CLAUDE.md 的根本区别 |

三句提示词和三个关键点一一对应：

- 第一句："读 docs/ 下的所有资产"对应"让 AI 基于资产提炼"
- 第二句："用链接指向 docs/ 就好"对应"用链接而不是复制"
- 第三句："禁区、历史包袱留空"对应"留位置但不填"。

第三条最关键：**让 AI 留出这两节的位置，但不要让它填**。因为 AI 填不出这两节的真实内容，它会瞎编或者写得很泛。留空让工程师自己填——这是老项目 CLAUDE.md 区别于新项目的根本所在。

### 5.4 产出效果

把提示词喂给 Claude Code，AI 基于 `docs/` 下的五份资产生成的 CLAUDE.md 初稿如下：

<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/b4a63d2e8d4c1b241449b3a5bcdd28d1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/10_了解项目_05：提炼CLAUDE.md/b4a63d2e8d4c1b241449b3a5bcdd28d1_MD5.jpg
用途：展示 AI 基于五份资产生成的 CLAUDE.md 初稿效果，作为读者产出对照
内容：生成的 CLAUDE.md 示例，包含项目定位、核心架构（含指向 docs/architecture.svg 的链接）、关键模块表、关键约定、怎么跑五节，以及留空的禁区、历史包袱两节
-->

前五节成型：项目定位、核心架构（含指向 `docs/architecture.svg` 的链接）、关键模块、关键约定、怎么跑都齐了；禁区、历史包袱两节按提示词要求留空。

### 5.5 三个常见坑

初稿出来通常踩这三个坑，按表格对照处理：

| 坑               | 表现              | 处理                |
| --------------- | --------------- | ----------------- |
| <span style="color: red; font-weight: bold;">AI 把架构图文字化</span>      | 核心架构节出现长段模块描述   | 让 AI 压成一句话 + 链接   |
| <span style="color: red; font-weight: bold;">关键约定写得太通用</span>       | 出现"代码要有注释"这种通用话 | 让 AI 从代码风格反推项目硬规则 |
| <span style="color: red; font-weight: bold;">AI 自作主张填禁区/历史包袱</span> | 两节被填上空洞内容       | 直接让 AI 恢复空白       |

第一个坑：AI 第一次生成会忍不住把 `architecture.svg` 的内容用文字描述一遍，塞进"核心架构"那节。初稿出来检查一下，如果看到长段的模块描述，直接说"这一段太长了，压成一句话 + 链接"。

第二个坑：AI 容易写通用的（比如"代码要有注释"），不写项目特有的。如果出现这种情况，<span style="color: red; font-weight: bold;">让它从代码风格里反推项目真实的硬规则，而不是抄通用开发手册</span>。

第三个坑：AI 可能贴心地把两节空着的也填上。这时候直接让它恢复空白："禁区和历史包袱这两节留给我自己写，别帮我猜。"

## 6. 灵魂两节：禁区与历史包袱必须手写

<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/3fcddfbf00c91dc702a9a010b85b6e3b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

初稿成型，前五节有了，最后两节还空着——第 5 章里说"让 AI 留位置但不填"。现在回答全文最硬的一个问题：**为什么这两节必须手写，AI 一字都填不出？** 这一章也是老项目 CLAUDE.md 区别于新项目的根本所在。

<span style="color: red; font-weight: bold;">新项目没有禁区，也没有历史包袱；老项目的灵魂，全压在这两节上。</span>

### 6.1 为什么这两节 AI 写不出

答案一句话：<span style="color: red; font-weight: bold;">这两节的信息不在代码里、不在 `docs/` 里、只在工程师脑子里</span>。

前五节的信息有据可查——项目定位、核心架构、关键模块、关键约定、怎么跑，AI 读完 `docs/` 下的五份资产、扫一遍代码就能提炼出来。但禁区和历史包袱这两节完全不同，它们的信息从来不落地。

举个例子。`PromptEntity.external_key` 为什么动不得？代码里只有字段定义，没有"**某 SDK 客户依赖它做缓存键**"这层依赖关系——这层依赖关系在工程师脑子里，在某个客户的接入文档里，唯独不在代码里。再看 `PromptTemplate.vue` 为什么用 Vue 而不是 React：代码里只有 Vue 语法，没有"**整个 admin 其他地方都用 React，这里是个例外**"这个背景。这些信息靠口口相传、靠踩坑记录、靠 code review 时一句"别动这个，会出事"——从没被文字化过。

AI 没有这些信息源，就只能瞎编或者写得很泛。让 AI 填禁区，它大概率写"注意数据安全""遵守接口契约"这种放之四海皆准的废话；让它填历史包袱，它会输出"注意代码可维护性"这种空话。**因此这两节必须工程师手写，一个字都不能交给 AI**。

### 6.2 禁区写什么

禁区写什么：<span style="color: red; font-weight: bold;">哪些代码动不得、哪些字段有对接方依赖、哪些配置改了会出事</span>。换个说法，禁区就是给 AI 划红线——红线之内，再合理的重构也不许做。

示例（不是真实的内容，每个项目的禁区不一样）：

```
## 禁区

- `server-core/PromptEntity` 的 `external_key` 字段：某 SDK 客户依赖
  此字段做缓存键，删掉或改名会导致该 SDK 直接报错。不要重构。

- `application.yml` 里 `nacos.server-addr` 的默认值：部分企业用户依赖
  这个默认值做灰度发布，改动需要发公告。

- `POST /api/prompts/search` 接口路径：曾经公开给过社区，更改路径
  会造成外部调用失败。新增同义接口可以，删除原接口不行。
```

<span style="color: red; font-weight: bold;">三条示例，分别对应三种典型禁区：字段有外部依赖、配置被用户当约定、接口对外公开过。</span>照着这个分类去自己项目里挖。

### 6.3 历史包袱写什么

历史包袱写什么：<span style="color: red; font-weight: bold;">项目里看起来奇怪但有原因的东西</span>。换个说法，历史包袱就是给 AI 解释奇怪的旧代码——解释清楚了，AI 才不会"好心"去统一它。

示例：

```
## 历史包袱

- `server-core/Dataset` 和 `DatasetItem` 的表结构看起来冗余，是 2024 年某个
  实验性功能留下的。功能已下线但数据保留中，勿删。

- 前端 `PromptTemplate.vue` 用的是 Vue 不是 React，是早期遗留。
  整个 admin 其他地方都用 React，这里例外。不要"顺手重构"统一。

- `LegacyEvaluatorAdapter` 这个类是 v0.x 时代的兼容层，看起来很乱，
  是因为要同时支持三种老 API。v1.0 之后新代码一律走 EvaluatorV1。
```

<span style="color: red; font-weight: bold;">三条示例，分别对应三种典型包袱：死功能留着的脏表结构、技术栈不统一的遗留文件、新老并存时的兼容层。</span>照着这个分类去自己项目里挖。

### 6.4 为什么这两节价值百倍

<span style="color: red; font-weight: bold;">这两节每一条都是 AI 永远猜不出来的——只有接手者和项目原作者聊过、或者自己踩过坑之后才知道。但写出来之后，AI 改造时就会自动避开。</span>两条示例列出的六条信息，每一条对应一个 AI 行为转变：

| 价值点 | 说明 |
|-------|-----|
| AI 改造自动避开禁区 | 看到禁区条目就不会动 |
| AI 不会"好心"重构历史包袱 | 不会把 Vue 改成 React |
| 列不出 = 理解不够深 | 是项目理解深度的信号 |

<span style="color: red; font-weight: bold;">老项目 CLAUDE.md 写得好不好，就看这两节写得深不深。</span>其他内容 AI 都能帮工程师生成，<span style="color: red; font-weight: bold;">这两节必须自己投入时间</span>。如果工程师现在还列不出几条禁区、几条历史包袱，那是一个信号：<span style="color: red; font-weight: bold;">对这个项目的理解还不够深</span>——再回去挖一挖，和老同事聊一聊，翻翻 git log 里那些奇怪的 commit。

<span style="color: red; font-weight: bold;">花时间填这两节，是给工程师自己、给团队、给 AI 一起做的最值得的一件事</span>。

## 7. 定稿前过三个检查点

<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/1f4c553efc4610a3b10962cb8a7af181_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

CLAUDE.md 写完，定稿前过一遍这三个检查点。就像上线前过 code review checklist，一条一条对，过了才算定稿。

| 检查点 | 通过标准 |
|-------|---------|
| <span style="color: red; font-weight: bold;">有没有禁区和历史包袱</span> | 至少列一两条占位，后面遇到再补 |
| <span style="color: red; font-weight: bold;">是不是太长</span> | 不超过 300 行（第 2 章那条 300 行红线），超了下放到 `docs/` |
| <span style="color: red; font-weight: bold;">有没有重复 docs/</span> | <span style="color: red; font-weight: bold;">核心架构应是引导而非替代</span> |

三点逐一过：第一点，老项目一定有禁区、一定有历史包袱，没有就是漏了，至少先列一两条占位，后面踩到再补；第二点，超过 300 行就是塞了太多详细内容，扫一遍哪些段落可以下放到 `docs/`，CLAUDE.md 里只保留一句话 + 链接；第三点最容易踩，单独展开。

### 7.1 引导 vs 替代

第三点的合格标准一句话：核心架构这一节应引导读者去看 `docs/`，而不是把 `docs/` 里的内容重新抄一遍。下面这张表把两种写法摆在一起，一看就懂。

| 写法                                         | 类型  | 是否合格 |
| ------------------------------------------ | --- | ---- |
| 架构分为前端、后端、数据库三层，详见 `docs/architecture.svg` | 引导  | 合格   |
| 把架构图的每个模块都文字化描述一遍                          | <span style="color: red; font-weight: bold;">替代</span>  | <span style="color: red; font-weight: bold;">不合格</span>  |

左边一行合格：给的是结论 + 入口，AI 既知道分层、也知道去哪看全图。右边一行不合格：把 `architecture.svg` 的内容用文字重写了一遍，那是在复制，不是在索引。<span style="color: red; font-weight: bold;">三个检查点全过，CLAUDE.md 才算定稿。</span>

## 8. 小结、思考与后续入口
<img src="imgs/aicmigr-10-proj-rd-05-distill-claude-md/df53f2864083fe1caf1c0f8fdb13f4a1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 8.1 小结

本篇做了一件事：**把 `docs/` 里的五份资产凝成一份 CLAUDE.md，放进项目根目录**。核心要点压成四行，像一张可以带走的脑图：

| 要点 | 内容 |
|-----|-----|
| 核心原则 | 索引 + 常识，不是 docs/ 的复制 |
| 生成路径 | AI 写前五节，人工写禁区/历史包袱 |
| 区别根本 | 老项目 CLAUDE.md 的灵魂在禁区 + 历史包袱 |
| 脑图成型 | <span style="color: red; font-weight: bold;">五份资产 + 一份 CLAUDE.md = 项目脑图成型</span> |

写禁区和历史包袱这两节，是给工程师自己、给团队、给 AI 一起做的最值得的一件事——**写得深不深，直接决定这份 CLAUDE.md 有没有用**。

### 8.2 思考

读完先别急着写，回到自己项目，对着这两道题自检一遍。这是检验有没有真懂的最快办法：

| 思考题 | 提示 |
|-------|-----|
| 自己项目能列出几条禁区/历史包袱？ | 列不出是真的没有，还是还没搞清楚？ |
| 团队老项目有类似 CLAUDE.md 的东西吗？ | 里面是索引+常识，还是大杂烩？ |

思考1：手上的项目，现在让写禁区、历史包袱两节，能列出几条？列不出是因为真的没有，还是自己还没挖到？如果是后者，这些信息目前存在哪里——同事脑子里？Jira 的某个老 ticket 里？还是 git log 里某个奇怪的 commit？翻一翻，多半能挖出几条。

思考2：团队正在运行的老项目，有没有一个类似 CLAUDE.md 的东西（README、内部 wiki 都算）？如果有，里面是索引 + 常识，还是什么都往里塞的大杂烩？如果是后者，问题出在哪——是定位错了，还是该下放的内容没下放？

### 8.3 CLAUDE.md 的后续作用

CLAUDE.md 不会在根目录吃灰，<span style="color: red; font-weight: bold;">它是后续每次 AI 工作的入口</span>。Claude Code 每次启动都会读这份文件，改造中的硬约束、禁区、历史包袱都在它里面，AI 是带着这些共识进来的：

| 后续场景   | CLAUDE.md 的作用         |
| ------ | --------------------- |
| 构建测试护栏 | AI 知道哪些地方是禁区，建测试时自动避开 |
| 加集成测试  | AI 知道哪些接口要测、哪些字段不能改   |
| 做需求改造  | AI 知道改造硬约束，避免踩雷       |

但还有一类东西没落地：**反复要做的操作流程**——改造前的体检、PR 前的 checklist、资产更新后的校对。这些不是常识，是操作。塞进 CLAUDE.md 会让它臃肿，每次启动都加载一遍没必要。它们属于另一个地方——**SKILL**。<span style="color: red; font-weight: bold;">如果说 CLAUDE.md 是 AI 的入职手册，那 SKILL 就是入职手册之后的 SOP，把重复流程固化成可复用的步骤。</span>

这是下一篇的主题：怎么从老项目里挖出这些重复流程，写成工程师自己的第一个 SKILL。


