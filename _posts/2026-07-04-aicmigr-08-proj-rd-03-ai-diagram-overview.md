---
title: 传统项目迁AI 08：了解项目 - AI绘图俯视项目全景
author: fangkun119
date: 2026-07-04 08:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/cover.jpg
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
aicmigr-08-proj-rd-03-ai-diagram-overview
传统项目迁AI 08：了解项目 - AI绘图俯视项目全景
-->

## 1. 导读地图：本篇怎么读

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/7d947ab0566b4174b6a3d4f1d4802832_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是「了解项目」阶段的第三篇，对应八步心法的 **步 4：画项目全景**。上一篇把画图能力装进了 Claude Code，本篇正式拿 Spring AI Alibaba Admin 这个真实企业级项目开刀，让 AI 读代码、画出三张俯视全局的图：架构图、模块图、依赖图。

读完本篇，读者会拿到一套"画三张图"的可复用方法论（提示词模板 + review 要点 + 常见坑清单），并能在自己的老项目上一比一复现。这三张图也是系列第 10 篇 CLAUDE.md 的骨架资产，今天画好，明天就能用上。

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/30dfda29c3c21ab9f26f6e30009f2f11_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    Start([本篇导读：画三张图])
    Start --\> P1[第一部分 方法论提炼]
    Start --\> P2[第二部分 实战演示]

    P1 --\> P1A[2、为什么先画三张图<br/>三种俯视的价值]
    P1 --\> P1B[3、画三张图的方法论<br/>提示词/迭代/review]
    P1 --\> P1C[4、项目 Check List<br/>可裁剪速查]

    P2 --\> P2A[5、主线项目登场<br/>Spring AI Alibaba Admin]
    P2 --\> P2B[6、三张图实战<br/>提示词/效果/坑]
    P2 --\> P2C[7、review 与定档归档]
    P2 --\> P2D[8、小结与思考]

    P1C -.速查.-> Senior([熟练工程师<br/>快速回顾])
    P2A -.代入.-> Beginner([初学工程师<br/>系统掌握])
-->

两类读者的推荐读法：

- **初学 AI 编程工程师**：建议通读全篇。第一部分建立"三张图方法论"的认知骨架，第二部分用 Spring AI Alibaba Admin 把骨架落到一个真实企业级微服务上，把每张图的提示词、效果、坑完整走一遍。
- **熟练 AI 编程工程师**：可只看第一部分的 Check List 速查；接到一个新老项目时，按清单填提示词、按 review 表走一遍即可。

## 2. 为什么先画三张图

老项目改造八步心法的第 4 步是"画项目全景"。全景不是一张图，是三张。三张图分别从系统级、代码级、生态级三个高度俯视同一个项目，回答三个不同的问题。

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/c636692819261d8afb8cb78625ea5527_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/08_了解项目_03：AI绘图俯视项目全景/c636692819261d8afb8cb78625ea5527_MD5.jpg
用途：呈现八步心法整体流程，本篇对应步 4「画项目全景」
内容：八步心法示意图，第 4 步被突出标注，从 README 到核心链路的全流程
-->

### 2.1 三张图回答三个不同的问题

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/23506d2dab36a09c7026dfdff5da5c69_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 架构图：系统级俯视

回答"这个项目长什么样"。前端、后端、数据库、中间件的系统边界和调用关系。一张图告诉读者这是一个什么样的系统、跑起来的时候各部分怎么协作。

#### (2) 模块图：代码级俯视

回答"项目内部怎么组织"。具体到代码仓库里的模块划分和模块之间的依赖关系。一张图告诉读者这个仓库里有几个模块、谁依赖谁、改一个会拖动谁。

#### (3) 依赖图：生态级俯视

回答"项目靠什么外部能力活着"。这个项目用了哪些第三方库、连了哪些中间件、对接了哪些外部 API。一张图告诉读者这个系统的生存环境长什么样、哪些外部依赖不能丢。

### 2.2 三张图不重复：三种高度俯视同一个项目

三张图不重复，它们从三个不同的高度俯视同一个项目。下表把它们对照清楚：

| 图 | 俯视高度 | 回答的问题 | 主要信息源 |
|----|---------|----------|-----------|
| 架构图 | 系统级 | 项目长什么样、跑起来怎么协作 | README、顶层目录 |
| 模块图 | 代码级 | 内部模块怎么组织、谁依赖谁 | `pom.xml` |
| 依赖图 | 生态级 | 靠什么外部能力活着 | `pom.xml`、`application.yml`、README |

### 2.3 不画的代价：四类迟早会遇到的问题

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/cfb20cdebdd5a63c01721882936d75fd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

工程师可能觉得，画一张架构图也就罢了，另外两张真有必要吗？真有必要。每一张图解决一个工程师迟早会遇到的具体问题。

#### (1) 没有架构图：人与 AI 基线认知不同步

后面每次让 AI 改造，它都要重新猜一次项目长什么样。工程师猜一个版本，AI 猜另一个版本，改着改着就跑偏了。架构图的作用是锚定共同认知，改造之前先对表。

#### (2) 没有模块图：不知道改动的辐射范围

改 `server-core` 会不会波及 `server-runtime`？改 `server-openapi` 会不会动到 `server-start`？这些问题，没有模块图只能看 `pom.xml` 一行一行翻。有了图，影响范围一眼就看清。

#### (3) 没有依赖图：不知道项目的命门在哪

升级 Spring AI 的版本会不会炸？Nacos 连不上整个应用还能不能启动？MySQL 换成 PostgreSQL 要改几处？这些都是依赖图一眼看得到的事情。

#### (4) 三张图是后续 CLAUDE.md 的骨架资产

还有一个更关键的理由：这三张图是系列第 10 篇 CLAUDE.md 的前置资产。写 CLAUDE.md 时需要给 AI 一份项目概貌，三张图就是概貌的骨架。今天画好，明天就能用上。

## 3. 画三张图的方法论

第二部分实战之前，先把方法论提炼出来。本节回答三个问题：提示词怎么写？三张图各自的关键指令有什么不同？画完之后怎么 review 和迭代？

### 3.1 提示词设计的三条通用原则

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/a5f539c15304c5728915893612c844ba_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

无论画哪张图，提示词都要遵守三条原则。这三条原则是后续 Check List 的源头。

#### (1) 原则一：指定信息源

告诉 AI 去读哪些文件，而不是让它根据模块名瞎猜。

| 图 | 必须指定的信息源 |
|----|----------------|
| 架构图 | README、顶层目录 |
| 模块图 | `pom.xml`（parent pom + 各子模块 pom） |
| 依赖图 | `pom.xml`、`application.yml`、README |

不让 AI 读 `pom.xml`，它可能根据模块名瞎猜依赖方向；不让 AI 读 `application.yml`，它就不知道项目连了哪些中间件。

#### (2) 原则二：给约束

告诉 AI 不画什么、怎么分层、怎么分类，否则画出来的图会被无关信息淹没。

- 架构图：前端、后端、数据库、中间件分层画，基础设施用一个方框概括
- 模块图：只画项目自己的模块，外部库不画，循环依赖用红色标出来
- 依赖图：分三类（关键 Java 依赖、中间件、外部 API），每类用不同颜色

Spring Boot 一个项目 transitive 依赖能到几百个，全画进来图就废了。

#### (3) 原则三：提升信息密度

让 AI 在每个方框里写"一句话职责"，而不是只写一个模块名。

| 维度 | 只写模块名 | 加一句话职责 |
|------|----------|------------|
| 信息量 | 一个词 | 一个词 + 这个模块干嘛 |
| 阅读体验 | 还得回去翻代码 | 看图就懂 |
| 协作效率 | AI 和人仍不同步 | 锚定共识 |

加一句职责，图的信息密度立刻上来。

### 3.2 三张图的差异化提示策略

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/906552a9d44b2a600d9b56635097fcca_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

三张图的提示词结构类似（信息源 + 约束 + 输出位置），但关键指令各不相同。下表把它们对照清楚，方便读者套用到自己的项目。

| 维度 | 架构图 | 模块图 | 依赖图 |
|------|-------|-------|-------|
| 信息源 | README + 顶层目录 | `pom.xml` | `pom.xml` + `application.yml` + README |
| 核心约束 | 分层画 | 只画项目模块、外部库不画 | 分三类、不同颜色 |
| 信息密度指令 | 核心模块写一句话职责 | 有循环依赖用红色标 | 关键依赖、不要 transitive |
| 输出 | `docs/architecture.svg` | `docs/module-deps.svg` | `docs/external-deps.svg` |

### 3.3 三轮迭代与 review 心法

提示词设计好了，不代表一次就能画好。画图是一个迭代过程，需要 review。

#### (1) 接受"一次画不完美"的常态

AI 画图一定有错（系列上一篇留的硬线）。一次画不完美是常态，迭代三五轮才能拿到一张真正能用的图。迭代方式是给具体指令：

```
放大 Server 层，把四个子模块之间的调用关系画细一点
数据库层加上表名
把 OpenTelemetry trace 链路补上
```

#### (2) review 三件事：信息齐全、方向正确、粒度合适

无论哪张图，review 都从这三个维度入手：

| review 维度 | 含义 | 检查问题 |
|------------|------|---------|
| 信息齐全 | 该有的有没有 | 核心模块都在？外部 API 没漏？ |
| 方向正确 | 依赖、调用、数据流方向对不对 | `start` 是不是依赖 `runtime`？循环依赖是真的还是画错？ |
| 粒度合适 | 不是太粗也不是太细 | 外部库砍掉了吗？transitive 砍掉了吗？ |

#### (3) 画不出整齐的图通常是项目本身有问题

如果画到一半发现某张图特别难画，那通常不是工程师的问题，是项目本身就有问题。

| 难画的表现 | 可能的项目问题 |
|----------|-------------|
| 模块依赖纠缠不清 | 循环依赖、模块职责不清 |
| 外部依赖一团乱麻 | 第三方依赖缺乏治理 |
| 架构层次混乱 | 历史叠加、缺少分层规范 |

画不出整齐的图，本身就是项目需要整理的信号灯。

## 4. 第一部分 Check List：画三张图速查

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/820141fcbcdce4d5ae80528e7fa253f0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节提供一份可裁剪、可勾选的 Check List，供工程师在画三张图的不同阶段快速查阅。读者可以按项目实际情况裁剪后贴到自己的 `CLAUDE.md` 或工作笔记里。

### 4.1 提示词设计 Check List

设计画图提示词时逐条自检：

- [ ] 是否指定了信息源（README / `pom.xml` / `application.yml`）？
- [ ] 是否给出了"不画什么"的约束（外部库不画、transitive 不画）？
- [ ] 是否给出了结构约束（分层画 / 分三类 / 不同颜色）？
- [ ] 是否要求"核心模块写一句话职责"提升信息密度？
- [ ] 是否指定了输出路径（`docs/architecture.svg` 等）？
- [ ] 是否要求标注循环依赖（模块图）？

### 4.2 三张图各自的 review Check List

每张图画完，按下表逐项 review：

| 图 | 必检项 |
|----|-------|
| 架构图 | 前端是否合理展开（不是被画成与后端并列的小方框）？前后端边界对不对？数据流向真实吗？OpenTelemetry trace 链路在不在？有没有把废弃的东西画成核心？ |
| 模块图 | `start` 模块在不在图里？依赖方向对不对（`start` → `runtime`/`openapi` → `core`）？循环依赖是真的还是画错？`frontend` 是不是被错误地塞进来了？ |
| 依赖图 | Java 依赖是不是只列主干？中间件有没有遗漏（要读 `application.yml`）？外部模型 API 有没有漏（要读 README 的 Configure Your API Keys）？三类分色清楚吗？ |

### 4.3 通用迭代 Check List

画完第一版进入迭代阶段，逐条自检：

- [ ] 是否接受"一次画不完美"的常态，准备好迭代三五轮？
- [ ] 迭代指令是否具体（"放大 Server 层""补上 OTel trace""加上表名"），而不是泛泛要求"画好一点"？
- [ ] review 是否覆盖了三个维度（信息齐全 / 方向正确 / 粒度合适）？
- [ ] 卡住时是否考虑过"是不是项目本身有问题"，把它当信号灯？
- [ ] 是否做到了"大致对即可"，没有为追求完美而阻塞后续工作？
- [ ] 定稿是否归档到 `docs/` 目录，准备给后续 CLAUDE.md 引用？

## 5. 实战主线登场：Spring AI Alibaba Admin

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/6636c216a8c5f6896d58729dc2cfb0d9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

说完方法论，进入第二部分实战。本篇及之后连续五篇（08 到 12）都在 Spring AI Alibaba Admin 这个项目上做事，所有出现的提示词、产出的图、沉淀的文档都围绕它展开。读者跟着一起操作会收获最大。

### 5.1 项目登场：连续五篇都在这个项目上

从本篇开始，第二部分剩下的五篇每一篇都在这个项目上做事。所有出现的提示词、产出的图、沉淀的文档，都围绕它展开。

### 5.2 准备工作：把项目 clone 下来

花两分钟准备一下：

```bash
git clone https://github.com/alibaba/spring-ai-alibaba.git

cd spring-ai-alibaba/spring-ai-alibaba-admin
```

### 5.3 本篇只读代码，不跑项目

读者可能手痒想 `mvn spring-boot:run` 跑起来看看，先按住这个冲动。系列第 13 篇专门讲怎么让 AI 帮忙把编译运行搞定，那时候一次性讲透。本篇只让 AI 读代码、画图。

### 5.4 docs/ 目录约定

一个约定：后面系列里所有产出的图、文档、学习笔记，都统一放在 `docs/` 目录下。这是一个固定的规范，不会变。本篇画的三张图就是第一批住进 `docs/` 的资产。

## 6. 三张图实战：提示词、效果与坑

把方法论的提示词套到 Spring AI Alibaba Admin 上，逐张画。每张图给出提示词（code block 原文不改写）、关键点说明、效果图、常见坑。读者可以直接复制提示词到自己的项目运行。

提示：这些图在上一篇已经出现过，但上一篇是演示画图效果，本篇才是真正开始了解项目。

### 6.1 第一张：架构图

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/92281391cbd0aa137355acdae5911e47_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

提示词：

```
读一下这个项目的 README 和顶层目录，给我画一张架构图。

前端、后端、数据库、中间件分层画，核心模块写一句话职责。

周边基础设施（日志、监控、配置）用一个方框概括就行，

不用展开。保存到 docs/architecture.svg。
```

#### (1) 关键点

##### ① 分层说清楚

Spring AI Alibaba Admin 是前后端分离的项目：前端 React 一层、后端 Java 一层、下面挂 MySQL 和 Nacos、再下面对接外部模型 API。没有分层的提示，AI 会把所有东西堆到一起。

##### ② 核心模块写一句话职责

这一条很关键。AI 默认只写模块名，画出来的图每个方框里只有一个词，别人看了还是不知道 `server-core` 到底是干嘛的。加一句职责，图的信息密度立刻上来。

画出来的效果如下：

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/2b544af758c5fa8fa584b27960d68347_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/2b544af758c5fa8fa584b27960d68347_MD5
用途：展示架构图的最终效果，作为读者产出对照
内容：Spring AI Alibaba Admin 架构图：前端 React / 后端 Java / MySQL + Nacos / 外部模型 API（DashScope、OpenAI 等），分层呈现，含 OpenTelemetry trace 链路
-->

#### (2) 三个常见坑

##### ① 第一个坑：frontend 被画成与后端并列的小方框

AI 容易把 frontend 画成一个跟后端并列的小方框，实际上 frontend 是一整个独立的前端工程。review 的时候看一下，前端是不是被合理展开、UI 路由和后端 API 的调用关系是不是画出来了。

##### ② 第二个坑：漏掉 OpenTelemetry

AI 容易漏掉 OpenTelemetry。Spring AI Alibaba Admin 的 observability 能力是通过 OTel 集成实现的，架构图里应该体现"服务发出 trace → OTel Collector → 存储"这条链路。第一张出来如果没画，直接说"把 OpenTelemetry trace 链路补上"，AI 会基于上下文迭代一版。

##### ③ 第三个坑：以为一次画不完美是失败

一次画不完美是常态。画完说"放大 Server 层，把四个子模块之间的调用关系画细一点"、"数据库层加上表名"。迭代三五轮才能拿到一张真正能用的架构图。

### 6.2 第二张：模块图

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/debde2d05d558db95f566494e8d72f1a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

提示词：

```
看一下项目的 pom.xml，画一张内部模块依赖图。

只画项目自己的模块，外部库不画。有循环依赖用红色标出来。

保存到 docs/module-deps.svg。
```

#### (1) 关键点

##### ① 强调"看 pom.xml"

Spring AI Alibaba Admin 下面有四个 server 子模块加一个 frontend，AI 只要读 parent pom 和各子模块的 pom，关系就清楚了。不让它读，它可能根据模块名瞎猜依赖方向。

##### ② "外部库不画"是必须的

Spring Boot 一个项目 transitive 依赖能到几百个，全画进来图就废了。

画出来的效果如下：

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/8ff889c4b6d84f46c7d986d61561dcd4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/08_了解项目_03：AI绘图俯视项目全景/8ff889c4b6d84f46c7d986d61561dcd4_MD5.jpg
用途：展示模块图的最终效果，作为读者产出对照
内容：Spring AI Alibaba Admin 模块依赖图：server-start、server-runtime、server-openapi、server-core 四个 Maven 子模块之间的依赖关系
-->

#### (2) 三个常见坑

##### ① 第一个坑：server-start 模块被漏掉

AI 可能把 `server-start` 模块漏掉。start 模块通常是 entry point（main 方法在里面），AI 容易认为它是"运行时入口"而不是"代码模块"。review 的时候看 start 模块有没有在图里，以及它有没有正确地依赖其他三个 server 模块。

##### ② 第二个坑：依赖方向画反

正确的方向是 `start` 依赖 `runtime` 和 `openapi`，`openapi` 和 `runtime` 都依赖 `core`。如果图里出现 `core` 反过来依赖 `runtime` 这种，就是错的，要让 AI 重画。

##### ③ 第三个坑：frontend 不应该出现在这张图里

模块图只画 Java 模块之间的依赖关系，frontend 是独立的 React 工程，通过 HTTP 调用后端，不通过 Maven 依赖。AI 有时候会把 frontend 也塞进去，要让它去掉。

### 6.3 第三张：依赖图

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/ea19ebe9ce9cae21b9413bd62ec900b0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

提示词：

```
综合看 pom.xml、application.yml 和 README，帮我梳理这个项目。

对外依赖了什么，分成三类：关键 Java 依赖、中间件、外部 API。

画出来，每类用不同颜色。保存到 docs/external-deps.svg。
```

#### (1) 关键点

##### ① 分三类是这张图的灵魂

Java 依赖看 pom（Spring AI、Spring Boot Actuator、Micrometer 等），中间件看 `application.yml` 和 `docker-compose`（MySQL、Nacos、OTel Collector），外部 API 看 README 和配置样例（DashScope、OpenAI、DeepSeek 等模型提供商）。

##### ② 三类分开画才能一眼分辨

三类分开画，工程师才能一眼分辨，哪些是代码层面的依赖、哪些是运行时要连的中间件、哪些是外部第三方服务。

画出来的效果如下：

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/81127e334bdc92671264da73f9fac88c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/08_了解项目_03：AI绘图俯视项目全景/81127e334bdc92671264da73f9fac88c_MD5.jpg
用途：展示依赖图的最终效果，作为读者产出对照
内容：Spring AI Alibaba Admin 外部依赖图，三类分色：关键 Java 依赖（Spring AI、Spring Boot Actuator、Micrometer）、中间件（MySQL、Nacos、OTel Collector）、外部模型 API（DashScope、OpenAI、DeepSeek）
-->

#### (2) 三个常见坑

##### ① 第一个坑：transitive dependency 全列出来

AI 容易把 transitive dependency 全列出来。Spring Boot 一个 starter 就能拉几十个间接依赖，全画进来就没法看了。提示词里强调"关键"，review 的时候挑出真正的项目主干依赖，砍掉无关的。

##### ② 第二个坑：不知道"应该看 application.yml 才知道中间件"

AI 可能不知道"应该看 application.yml 才知道中间件"。如果第一版画出来只有 Java 依赖没有中间件，直接告诉它"去读 application.yml 和 application-*.yml，看项目连了什么中间件"。

##### ③ 第三个坑：外部模型 API 容易漏

外部模型 API 这一类容易漏。因为它们在代码里是通过配置注入的，不在 `pom.xml` 里。README 的 Configure Your API Keys 一节会说清楚项目支持哪些 API。让 AI 读 README 的这一节。

## 7. 画完之后：review 与定档归档

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/f5503ad2bfb06ef21d277342b4ef10b3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

画完不是终点，review 和存档才是。系列上一篇留了一条硬线：AI 画的图一定有错。本篇把这条线具体化。

### 7.1 review 的停顿点：AI 完成主要工作，人来校对

AI 完成了主要工作，需要工程师自己来做校对。这一步是需要停下来的——但也不一定全对，只要大致对即可。

### 7.2 三张图各自的 review 要点

每张图的 review 重点不同，下表对照清楚：

| 图 | review 重点 |
|----|-----------|
| 架构图 | 核心模块是不是都在？前端后端的边界是不是画对了？数据流向是不是真实？有没有把 observability 漏掉？有没有把已经废弃的东西画成核心？ |
| 模块图 | 依赖方向对不对（`start` → `runtime`/`openapi` → `core`）？循环依赖出现了不要忽略，这是真实存在的架构问题 |
| 依赖图 | 三类是不是齐全？Java 依赖有没有列主干？中间件有没有遗漏？外部 API 是不是反映了当前 README 写的那几个模型提供商？ |

### 7.3 定档到 docs/：成为人和 AI 的共同记忆

review 到满意为止，然后定稿放进 `docs/`。定稿之后，这三张图就是工程师和 AI 的共同记忆。系列第 10 篇写 CLAUDE.md 会引用它们，后面改造的每一篇都可能翻出来对照。图画得扎实，后面所有篇都轻松。

### 7.4 画图卡住是项目信号灯

如果画到一半发现某张图特别难画，那通常不是工程师的问题，是项目本身就有问题。循环依赖是架构问题，模块职责不清也是架构问题，外部依赖一团乱麻更是架构问题。画不出整齐的图，说明这个项目本身需要整理。这本身就是一个信号。

## 8. 小结与思考

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/287b5606c8b730316e18565337b50df6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 8.1 小结

本篇做了三件事。

第一件，把 Spring AI Alibaba Admin clone 下来，确认从本篇开始实操都在这个项目上做，所有产出都放在 `docs/` 目录下。

第二件，讲清楚了为什么要画三张图：架构图锚定系统级共识、模块图暴露代码级依赖、依赖图展示生态级牵挂。三张图是三种不同粒度的俯视，不重复。

第三件，给了三套提示词，分别画出这三张图。提示词像人说话的风格，不堆格式化要求，只把关键指令和关键约束说清楚。每一张图都附上常见坑和 review 要点，让读者不至于画到一半一头雾水。

### 8.2 思考

#### (1) 关于手上项目的复盘

读者手上正在维护的那个老项目，如果让画这三张图，哪一张最难画？为什么？是因为项目本身就乱，还是因为自己没想清楚？

#### (2) 关于团队协作的检验

架构图、模块图、依赖图这三张图里，团队里哪一张最容易被反复翻出来看？这张图更新频率高不高？如果不高，为什么？是因为项目很稳定，还是因为没人维护？
