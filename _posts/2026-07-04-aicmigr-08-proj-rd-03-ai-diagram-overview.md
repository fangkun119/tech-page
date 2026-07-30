---
title: 传统项目迁AI 08：了解项目 - AI绘图俯视项目全景
author: fangkun119
date: 2026-07-04 08:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
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

## 1. 为什么改造老项目要先画三张图

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/2a4d980b23cd2e1f36c33fe545e54d6b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

做新项目时，第一步永远是评审需求文档和架构设计文档——所有人对着同一份文档建立共识，才知道要做什么、怎么做。AI 改造老项目第一步同样是建立共识，只不过对象换成了 AI：让 AI 把项目读出来，画成三张图。

为什么是这一步？**画三张图，本质就是给 AI 写一份"项目需求文档"**。没有它，后面每次让 AI 改造，它都要重新猜一次项目长什么样——猜一次偏一次。

本篇对应老项目改造八步心法的步 4：画项目全景。<span style="color: red; font-weight: bold;">全景不是一张图，是三张——架构图、模块图、依赖图，分别从系统级、代码级、生态级三个高度俯视同一个项目。</span>

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/c636692819261d8afb8cb78625ea5527_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/c636692819261d8afb8cb78625ea5527_MD5.jpg
用途：呈现八步心法整体流程，本篇对应步 4「画项目全景」
内容：八步心法示意图，第 4 步被突出标注，从 README 到核心链路的全流程
-->

### 1.1 一张图不够：系统级、代码级、生态级三种俯视

你可能会问：为什么不直接让 AI 看一眼代码就画？<span style="color: red; font-weight: bold;">为什么是三张图不是一张？因为同一个项目，从不同高度看，回答的是不同的问题，一张图扛不下三种视角。</span>

三张图各自回答的问题、主要信息源、对应的传统软件工程类比，对照如下：

| 图   | 俯视高度 | 回答的问题                 | 主要信息源                              | 传统软件工程类比                       |
| --- | ---- | --------------------- | ---------------------------------- | ------------------------------ |
| <span style="color: red; font-weight: bold;">架构图</span> | 系统级  | <span style="color: red; font-weight: bold;">项目长什么样、跑起来各部分怎么协作</span>     | README、顶层目录                        | 系统设计文档里的部署视图                   |
| <span style="color: red; font-weight: bold;">模块图</span> | 代码级  | <span style="color: red; font-weight: bold;">仓库内部怎么组织、谁依赖谁、改一个会拖动谁</span> | `pom.xml`                          | UML 组件图 / Maven 依赖树可视化         |
| <span style="color: red; font-weight: bold;">依赖图</span> | 生态级  | <span style="color: red; font-weight: bold;">项目靠什么外部能力活着、哪些外部依赖不能丢</span> | `pom.xml`、`application.yml`、README | 第三方依赖清单 + 中间件配置表 + 外部 API 对接清单 |

<span style="color: red; font-weight: bold;">三张图不重复，它们从三个高度俯视同一个项目。</span>把三张图放在一起，工程师和 AI 就有了完整的项目认知：从最粗的系统轮廓，到代码模块的依赖骨架，再到外部生态的命门。

### 1.2 不画的代价：四类迟早会遇到的问题

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/415bbd614f434dff887c6cd47f3b242a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

工程师可能觉得，画一张架构图也就罢了，另外两张真有必要吗？<span style="color: red; font-weight: bold;">每一张图解决一个改造过程中迟早会遇到的具体问题，少一张都会在某个节点卡住。</span>

| 缺哪张图 | 迟早会遇到的问题 | 后果 |
|---------|----------------|------|
| 架构图 | 人与 AI 基线认知不同步 | 工程师心里一个版本，AI 猜另一个版本，改着改着就跑偏 |
| 模块图 | 改动的辐射范围不清 | 改 `server-core` 会不会波及 `server-runtime`、改 `server-openapi` 会不会动到 `server-start`，只能翻 `pom.xml` 一行一行查 |
| 依赖图 | 项目的命门不明 | 升级 Spring AI 会不会炸、Nacos 连不上能否启动、MySQL 换 PostgreSQL 要改几处，全靠踩坑才知道 |

除了上面三类具体问题，还有一层更关键的理由：<span style="color: red; font-weight: bold;">这三张图本身就是后续 CLAUDE.md 的骨架资产</span>。写 CLAUDE.md 时要给 AI 一份项目概貌，三张图就是概貌的骨架——今天画好，后续每一次改造都能直接复用，不用再让 AI 重新猜一遍。

## 2. 提示词怎么写：三条原则 + 一张差异化对照表

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/197ac20102eb49d60bffa5821665485d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

为什么不直接对 AI 说一句「画一张架构图」就完事？为什么要费劲设计提示词？

把 AI 想象成入职第一天的新人，被丢到一个陌生仓库前面，只听到一句「画出这个项目的架构」——它能做的只有打开 `pom.xml` 猜。猜错方向、漏关键依赖，都是常态。提示词本质上就是给这位新人写的「入职引导文档」：告诉它先看什么、不看什么、每一步要交什么。

这条「入职引导文档」可以归纳成三条原则：<span style="color: red; font-weight: bold;">指定信息源、给约束、提升信息密度</span>。

### 2.1 三条通用原则：指定信息源 / 给约束 / 提升信息密度

#### (1) 指定信息源：告诉 AI 去读哪些文件

传统软件工程里，新人入职第一天的引导文档总会写清楚「先看 README，再看 `pom.xml`，最后翻 `application.yml`」。提示词里的「指定信息源」就是这件事——<span style="color: red; font-weight: bold;">明确告诉 AI 该读哪些文件，而不是让它根据模块名瞎猜。</span>

| 图 | 必须指定的信息源 |
|----|----------------|
| 架构图 | README、顶层目录 |
| 模块图 | `pom.xml`（parent pom + 各子模块 pom） |
| 依赖图 | `pom.xml`、`application.yml`、README |

不指定会怎样？<span style="color: red; font-weight: bold;">不让 AI 读 `pom.xml`，它可能根据模块名瞎猜依赖方向；不让读 `application.yml`，它就不知道项目连了哪些中间件。</span>

#### (2) 给约束：告诉 AI 不画什么、怎么分层

UML 评审会上有一条默契——「只画接口不画实现」，否则一张类图会被私有方法塞满。提示词里的「给约束」扮演同样的角色：<span style="color: red; font-weight: bold;">约定 AI 不画什么、怎么分层、怎么分类，免得产出被无关信息淹没。</span>

三张图各自的约束要点：

| 图   | 核心约束                                 |
| --- | ------------------------------------ |
| 架构图 | <span style="color: red; font-weight: bold;">① 前端、后端、数据库、中间件分层画<br>② 基础设施用一个方框概括</span>  |
| 模块图 | <span style="color: red; font-weight: bold;">① 只画项目自己的模块，外部库不画<br>② 循环依赖用红色标出来</span>    |
| 依赖图 | <span style="color: red; font-weight: bold;">① 分三类（关键 Java 依赖、中间件、外部 API），每类用不同颜色</span> |

为什么必须给约束？<span style="color: red; font-weight: bold;">Spring Boot 一个项目 transitive 依赖能到几百个，全画进来图就废了。</span>

#### (3) 提升信息密度：让每个方框写一句话职责

Java 工程师习惯给每个类写一句 Javadoc、给数据库表的字段写注释——这是协作效率的底层资产。提示词里的「提升信息密度」就是这件事：<span style="color: red; font-weight: bold;">让 AI 在每个方框里写「一句话职责」，而不是只写一个模块名。</span>

| 维度 | 只写模块名 | 加一句话职责 |
|------|----------|------------|
| 信息量 | 一个词 | <span style="color: red; font-weight: bold;">一个词 + 这个模块干嘛</span> |
| 阅读体验 | 还得回去翻代码 | <span style="color: red; font-weight: bold;">看图就懂</span> |
| 协作效率 | AI 和人仍不同步 | <span style="color: red; font-weight: bold;">锚定共识</span> |

### 2.2 三张图的关键指令对照表

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/e3dbe1692e25f87c71e49ca85a8a29d1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">三条原则是通用的，但落到三张图上，具体指令各不相同。</span>这张四维对照表是后续实战三段提示词的「设计图」——先看清设计，再看代码。

| 维度 | 架构图 | 模块图 | 依赖图 |
|------|-------|-------|-------|
| 信息源 | README + 顶层目录 | `pom.xml` | `pom.xml` + `application.yml` + README |
| 核心约束 | 分层画 | 只画项目模块、外部库不画 | 分三类、不同颜色 |
| 信息密度指令 | 核心模块写一句话职责 | 有循环依赖用红色标 | 关键依赖、不要 transitive |
| 输出 | `docs/architecture.svg` | `docs/module-deps.svg` | `docs/external-deps.svg` |
## 3. 实战：让 AI 读 Spring AI Alibaba Admin 画三张图

原理讲清楚了，接下来拿一个真实企业级项目验证。Spring AI Alibaba Admin 是前后端分离 + 多 Maven 子模块 + 对接多个模型 API 的典型架构——基本上是你日常能见到的 Spring Boot 项目样板，足够代表性。只需 `git clone`，本篇不跑项目，只让 AI 读代码画图。

### 3.1 准备：clone 项目与 docs/ 目录约定

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/ac228270f4e12ff6bb75d202efdf909a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

```bash
git clone https://github.com/alibaba/spring-ai-alibaba.git

cd spring-ai-alibaba/spring-ai-alibaba-admin
```

本篇只让 AI 读代码画图，不跑项目。

一个固定约定：所有产出的图、文档统一归档到 `docs/` 目录下。本篇画的三张图就是第一批住进 `docs/` 的资产。

### 3.2 第一张：架构图

```
读一下这个项目的 README 和顶层目录，给我画一张架构图。

前端、后端、数据库、中间件分层画，核心模块写一句话职责。

周边基础设施（日志、监控、配置）用一个方框概括就行，

不用展开。保存到 docs/architecture.svg。
```

#### (1) 关键点

- **分层说清楚**：Spring AI Alibaba Admin 是前后端分离——前端 React 一层、后端 Java 一层、下面挂 MySQL 和 Nacos、再下面对接外部模型 API。没有分层提示，AI 会把所有东西堆到一起。

- **核心模块写一句话职责**：AI 默认只写模块名，每个方框只有一个词，别人看了不知道 `server-core` 是干嘛的。加一句职责，图的信息密度立刻上来。

效果：

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/ea2164fb1b21747197a477f5da049e5e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/2b544af758c5fa8fa584b27960d68347_MD5.jpg
用途：展示架构图的最终效果，作为读者产出对照
内容：Spring AI Alibaba Admin 架构图：前端 React / 后端 Java / MySQL + Nacos / 外部模型 API（DashScope、OpenAI 等），分层呈现，含 OpenTelemetry trace 链路
-->

#### (2) 三个常见坑

| 坑                | 表现                                                                   | 怎么改                                          |
| ---------------- | -------------------------------------------------------------------- | -------------------------------------------- |
| frontend 被画小     | AI 把 frontend 画成与后端并列的小方框，实际它是一整个独立的前端工程                             | review 检查前端是否合理展开，UI 路由与后端 API 调用关系是否画出      |
| 漏掉 OpenTelemetry | observability 通过 OTel 集成实现，第一版常漏「服务发出 trace → OTel Collector → 存储」链路 | 直接说「把 OpenTelemetry trace 链路补上」，AI 基于上下文迭代一版 |
| 一次画不完美就是失败       | 第一版不够细，误以为方法没用                                                       | 迭代三五轮是常态，如「放大 Server 层」「数据库层加上表名」            |

### 3.3 第二张：模块图

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/26e13046d52eb1052d768a486e95e3ff_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

```
看一下项目的 pom.xml，画一张内部模块依赖图。

只画项目自己的模块，外部库不画。有循环依赖用红色标出来。

保存到 docs/module-deps.svg。
```

#### (1) 关键点

- **强调读 `pom.xml`**：Spring AI Alibaba Admin 下有四个 server 子模块加一个 frontend，AI 只要读 parent pom 和各子模块的 pom，关系就清楚了。不让读就根据模块名瞎猜依赖方向。
- **外部库不画是必须的**：Spring Boot 一个项目 transitive 依赖能到几百个，全画进来图就废了。

效果：

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/8ff889c4b6d84f46c7d986d61561dcd4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/8ff889c4b6d84f46c7d986d61561dcd4_MD5.jpg
用途：展示模块图的最终效果，作为读者产出对照
内容：Spring AI Alibaba Admin 模块依赖图：server-start、server-runtime、server-openapi、server-core 四个 Maven 子模块之间的依赖关系
-->

#### (2) 三个常见坑

| 坑 | 表现 | 怎么改 |
|----|------|-------|
| `server-start` 被漏掉 | start 是 entry point（main 方法在里面），AI 易误认为运行时入口而非代码模块 | review 看 start 是否在图里、是否正确依赖其他三个 server 模块 |
| 依赖方向画反 | 正确方向是 `start` 依赖 `runtime` 和 `openapi`，`openapi` 和 `runtime` 都依赖 `core` | 若 `core` 反依赖 `runtime` 就是错的，让 AI 重画 |
| frontend 出现在模块图 | 模块图只画 Java 模块依赖，frontend 是独立 React 工程通过 HTTP 调用，不走 Maven | AI 塞进去就让其去掉 |

### 3.4 第三张：依赖图

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/4e39a9dd712b42d6bfea1da52d38b7cf_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

```
综合看 pom.xml、application.yml 和 README，帮我梳理这个项目。

对外依赖了什么，分成三类：关键 Java 依赖、中间件、外部 API。

画出来，每类用不同颜色。保存到 docs/external-deps.svg。
```

#### (1) 关键点

- **分三类是这张图的灵魂**：Java 依赖看 pom（Spring AI、Spring Boot Actuator、Micrometer 等），中间件看 `application.yml` 和 `docker-compose`（MySQL、Nacos、OTel Collector），外部 API 看 README 和配置样例（DashScope、OpenAI、DeepSeek 等模型提供商）。
- **三类分开画才能一眼分辨**：哪些是代码层面的依赖、哪些是运行时要连的中间件、哪些是外部第三方服务。

效果：

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/81127e334bdc92671264da73f9fac88c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/81127e334bdc92671264da73f9fac88c_MD5.jpg
用途：展示依赖图的最终效果，作为读者产出对照
内容：Spring AI Alibaba Admin 外部依赖图，三类分色：关键 Java 依赖（Spring AI、Spring Boot Actuator、Micrometer）、中间件（MySQL、Nacos、OTel Collector）、外部模型 API（DashScope、OpenAI、DeepSeek）
-->

#### (2) 三个常见坑

| 坑                          | 表现                                      | 怎么改                                                    |
| -------------------------- | --------------------------------------- | ------------------------------------------------------ |
| transitive dependency 全列出来 | Spring Boot 一个 starter 拉几十个间接依赖，全画进来没法看 | 提示词强调「关键」，review 挑出主干依赖，砍掉无关的                          |
| 不知道读 `application.yml`     | 第一版只有 Java 依赖、没中间件，AI 漏掉了配置文件           | 直接说「去读 application.yml 和 application-*.yml，看项目连了什么中间件」 |
| 外部模型 API 容易漏               | 它们通过配置注入、不在 `pom.xml` 里                 | 让 AI 读 README 的 Configure Your API Keys 一节             |
## 4. 画完之后：review、迭代与归档

AI 画完第一版就是终点吗？不是。传统软件工程的架构图评审会从不会一次过稿，总要在评审意见上来回改几轮——<span style="color: red; font-weight: bold;">AI 画的图同理，必须有 review 和迭代环节。</span>

### 4.1 接受「一次画不完美」：迭代三五轮是常态

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/66cfa044572a62051f2a39848e58fe6e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

AI 画图一定有错。<span style="color: red; font-weight: bold;">一次画不完美是常态，迭代三五轮才能拿到一张真正能用的图。</span><span style="color: red; font-weight: bold;">迭代方式是给具体指令，而不是泛泛要求「画好一点」：</span>

```
放大 Server 层，把四个子模块之间的调用关系画细一点
数据库层加上表名
把 OpenTelemetry trace 链路补上
```

### 4.2 review 三维度：信息齐全 / 方向正确 / 粒度合适

<span style="color: red; font-weight: bold;">无论哪张图，review 都从信息齐全、方向正确、粒度合适三个维度入手。</span>三个维度在三张图上各有具体必检项，合并成一张表，扫表即用：

| review 维度 | 含义 | 架构图必检项 | 模块图必检项 | 依赖图必检项 |
|------------|------|------------|------------|------------|
| 信息齐全 | 该有的有没有 | 核心模块都在？observability 没漏？废弃的东西没画成核心？ | `server-start` 在图里？循环依赖出现了？ | 三类齐全？中间件没遗漏？外部 API 反映了 README 的模型提供商？ |
| 方向正确 | 依赖、调用、数据流方向对不对 | 前后端边界对？数据流向真实？ | 依赖方向 `start` → `runtime`/`openapi` → `core`？循环依赖是真的还是画错？ | — |
| 粒度合适 | 不是太粗也不是太细 | 分层清晰，周边基础设施一个方框概括？ | 外部库砍掉了？frontend 没塞进来？ | Java 依赖列主干？transitive 砍掉？三类分色清楚？ |

### 4.3 画不出来是项目信号灯

<img src="imgs/aicmigr-08-proj-rd-03-ai-diagram-overview/64fca0a7057d25e3dac3fe5964d6cb9d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

如果画到一半发现某张图特别难画，<span style="color: red; font-weight: bold;">那通常不是工程师的问题，是项目本身就有问题——循环依赖是架构问题，模块职责不清也是架构问题，外部依赖一团乱麻更是架构问题。</span><span style="color: red; font-weight: bold;">画不出整齐的图，本身就是项目需要整理的信号灯。</span>

| 难画的表现    | 可能的项目问题     |
| -------- | ----------- |
| <span style="color: red; font-weight: bold;">模块依赖纠缠不清</span> | <span style="color: red; font-weight: bold;">循环依赖、模块职责不清</span> |
| <span style="color: red; font-weight: bold;">外部依赖一团乱麻</span> | <span style="color: red; font-weight: bold;">第三方依赖缺乏治理</span>   |
| <span style="color: red; font-weight: bold;">架构层次混乱</span>   | <span style="color: red; font-weight: bold;">历史叠加、缺少分层规范</span> |

### 4.4 归档到 docs/：成为人和 AI 的共同记忆

<span style="color: red; font-weight: bold;">review 到满意为止，定稿放进 `docs/`。定稿之后，这三张图就是工程师和 AI 的共同记忆。</span>

**这三张图是后续 CLAUDE.md 的骨架资产**——写 CLAUDE.md 时要给 AI 一份项目概貌，三张图就是概貌的骨架，后续每一次改造都能直接复用，不用再让 AI 重新猜一遍。
## 5. 小结

### 5.1 小结

把 Spring AI Alibaba Admin clone 下来，所有产出统一归档到 `docs/`。用三段提示词让 AI 画出架构图、模块图、依赖图——三张图分别锚定系统级、代码级、生态级共识，不重复。review 三维度（信息齐全、方向正确、粒度合适），迭代三五轮后定档，这三张图就是后续 CLAUDE.md 的骨架资产。
