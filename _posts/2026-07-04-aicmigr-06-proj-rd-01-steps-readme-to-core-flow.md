---
title: 传统项目迁AI 06：了解项目 - 从README到核心链路
author: fangkun119
date: 2026-07-04 06:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/cover.jpg
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
aicmigr-06-proj-rd-01-steps-readme-to-core-flow
传统项目迁AI 06：了解项目 - 从README到核心链路
-->

## 1. 接手老项目的两种典型失败

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/a7c4f845b605dcee4d1293777706f804_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接手一个跑了几年的老项目，绝大多数工程师第一周都会陷入同一种焦虑：<span style="color: red; font-weight: bold;">代码几十万行，从哪儿看起？</span>

<span style="color: red; font-weight: bold;">传统软件时代这件事靠经验和韧性慢慢磨。AI 时代多了个变量——以为有了 Claude Code 就不用自己看了。结果两种人都没摸到项目骨架。</span>

### 1.1 失败模式一：从头到尾读代码

项目拿到手，打开 IDE，从 controller 开始，一个方法一个方法读下去。读到第三天还没读完 service 层，进度条只往前推了 10%。老板在群里问"项目看得怎么样了"，回答是"还在看"。

<span style="color: red; font-weight: bold;">问题不是慢，是读过的东西一会就忘。没有锚点，没有目标。</span>一周之后再回来，代码好像又变陌生了。

### 1.2 失败模式二：直接让 AI 全盘干

有人信奉"AI 时代不需要自己读代码"。项目拿到手直接打开 Claude Code：

```
帮我总结一下这个项目是做什么的
```

AI 给一段漂亮的总结。

```
帮我找出改造点
```

AI 给一个 todo list。

```
帮我写改造方案
```

AI 给一个方案。照着改，上线，出事。

<span style="color: red; font-weight: bold;">这种方式的问题更隐蔽：看起来很快，其实什么都不懂。</span>AI 的总结基于它看到的代码，那些它看不到的历史、对接方、隐性约定，工程师也没去补。以为上手了，其实还在地面。

### 1.3 共同病根：缺一套最小完备的锚点清单

<span style="color: red; font-weight: bold;">两种失败看起来是两个极端，根子却是同一个：没有一套最小完备的锚点清单。</span>

<span style="color: red; font-weight: bold;">什么是锚点清单？就像地图上的把手——抓住了就能定位，没有它就只能在一大片代码里乱逛。</span>详细展开留到第 2 章，这里先看它缺失时的后果：

| 失败模式    | 表象       | 根因                     |
| ------- | -------- | ---------------------- |
| 从头到尾读   | 无重点、容易忘  | 没有锚点清单指引"读什么不读什么"      |
| 全盘交给 AI | 看着快、其实不懂 | 没有锚点清单区分"哪些 AI 做、哪些人做" |

<span style="color: red; font-weight: bold;">从头到尾读是无重点，让 AI 全盘托管是无参与。</span>工程师需要一个清晰的骨架，知道哪些事要搞清楚、哪些不用，哪些交给 AI、哪些必须自己做——这套骨架就是后面要讲的改造流程。

## 2. 八步流程：把脑子里的理解变成 AI 能读的资产

第 1 章结尾给出"最小完备的锚点清单"这个词，那这套清单到底是什么？把"了解老项目"这件事拆开，可以归纳出八步。这八步不是某个人的发明，是任何一个接过老项目的工程师或多或少都在走的路径，只是没写下来。

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/6fdd51945db17e791212381d03a96d49_MD5.jpg" style="display: block; width: 800px;" alt="八步流程示意图：从 README 到 AI 能读的文档">

<!--
图片内容说明
路径：imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/6fdd51945db17e791212381d03a96d49_MD5.jpg
用途：用一张图直观呈现"了解老项目"八步流程的整体流程与各步骤之间的衔接关系
内容：八步流程示意图，从读 README 起，依次经过扫项目结构、找核心入口、画项目全景、梳理接口和数据模型、搭环境跑起来、带需求深挖代码，最终沉淀为 AI 能读的文档（CLAUDE.md / SKILL.md）
-->

### 2.1 八步清单总览

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/6472dff6dc41a576bcf74b9bae890ec3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 阶段   | 动作                    | 目标产出                              |
| ---- | --------------------- | --------------------------------- |
| 建立轮廓 | ① **读 README 和根目录文档** | 项目轮廓：做什么、跑在哪、核心概念                 |
|      | ② **扫项目结构**           | 模块地图：分几块、谁核心谁边缘                   |
| 确立坐标 | ③ **找核心入口**           | 坐标原点：controller / main / consumer |
|      | ④ **画项目全景**           | 架构图、模块依赖图、核心数据流图                  |
| 提炼契约 | ⑤ **梳理接口和数据模型**       | 对外契约（REST 清单）+ 内部骨骼（核心数据结构）       |
| 实弹运行 | ⑥ **搭起环境跑起来**         | 能断点、能看日志、能复现问题                    |
| 深度挖掘 | ⑦ **带着需求深挖代码**        | 有目标地读懂某条调用链                       |
| 终极转化 | ⑧ **沉淀成 AI 能读的文档**    | CLAUDE.md + SKILL.md              |

<span style="color: red; font-weight: bold;">八步合起来干一件事：把工程师脑子里的理解，变成 AI 也能看见的资产。</span>

传统软件工程师对这件事并不陌生——<span style="color: red;  font-weight: bold;">第 1 到 7 步</span>对应的产出（README 笔记、模块图、入口列表、架构图、接口契约、可运行环境、调用链路理解），每一份都是接手老项目时该沉淀的资产，本来就该做。<span style="color: red; font-weight: bold;">第 8 步</span>只是把这些资产改写成 AI 能读的格式，让 Claude Code 也能像刚入职的新同事一样快速进入状态。<span style="color: red; font-weight: bold;">从"传统软件资产"到"AI 能读的资产"，中间只差一个格式转换，这是理解整条流程的关键抓手。</span>

### 2.2 清单怎么用：三条运行原则

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/f661e32fc6df0d2eef950c04809ecc5a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

清单给了就能用好？不够。<span style="color: red; font-weight: bold;">八步是骨架，能不能落地还看三条运行原则。</span>这三条原则不是和八步并列的另一套骨架，而是这套清单的使用说明——拿到八步的同时就要知道它怎么转。

#### (1) 螺旋不是线性

八步是清单，不是顺序流水。真实工作里，读 README 的时候顺手扫了项目结构，搭环境的时候发现了一个接口要回去查代码，画全景的时候意识到核心入口漏了一个。<span style="color: red;">这些来回是正常的，甚至是必要的。</span>八步是一个锚点集合，告诉工程师"这几件事都要做"，不是必须按 1 到 8 的顺序做。

| 真实场景 | 跨步骤回跳 |
|---------|-----------|
| 画全景画到一半 | 回第二步重看某个模块结构 |
| 跑环境时遇到陌生接口 | 回第五步补接口清单 |
| 深挖代码发现入口漏了 | 回第三步重新核对入口 |

<span style="color: red; font-weight: bold;">重点是八步最后都要走到，不是次序。</span>

#### (2) 沉淀文档，不是留在脑子里

这件事值得反复强调。人的脑子有极限，今天搞懂的东西下周可能就忘一半。更关键的是，<span style="color: red; font-weight: bold;">留在脑子里的东西 AI 读不到。</span>每走完一步，问自己一个问题：这一步的产出写到哪里了？

- 第一步读完 README，有没有把关键点抄到一份笔记里？
- 第三步找到核心入口，有没有记在项目 map 的某一行？
- 第五步梳理出接口清单，有没有存成一份可查的 markdown？

<span style="color: red; font-weight: bold;">没沉淀等于没做。</span><span style="color: red; font-weight: bold;">前七步是理解，第八步是资产。两件不同的事。</span>这也是为什么第八步要专门拎出来讲"沉淀成 AI 能读的文档"——前七步在脑子里建立理解，第八步把它们固化成 AI 能读的格式，资产才真正落地。

#### (3) 建立锚点，不是读懂

最后一条最反直觉。<span style="color: red; font-weight: bold;">很多人接老项目第一反应是"要把这个项目读懂"，然后陷进代码里出不来。</span>

<span style="color: red; font-weight: bold;">我的判断是：目标不是读懂，是建立锚点。</span>

什么叫锚点？可以把它想成地图上的把手——工程师要改这个项目时能抓住的地方。<span style="color: red; font-weight: bold;">抓住这四个把手就足以安全推进改造：</span>

| 锚点    | 说明                               |
| ----- | -------------------------------- |
| <span style="color: red; font-weight: bold; background-color: yellow;">核心链路</span>  | 主流程从哪个入口进、经过哪些模块、最后落到哪里          |
| <span style="color: red; font-weight: bold; background-color: yellow;">高风险模块</span> | 改了最容易出事的那几块代码在哪里                 |
| <span style="color: red; font-weight: bold; background-color: yellow;">对外契约</span>  | REST / RPC 接口、消息格式这些对外承诺不能随便动的东西 |
| <span style="color: red; font-weight: bold; background-color: yellow;">历史坑</span>   | 历史上踩过、写在了注释或文档里的坑                |

<span style="color: red; font-weight: bold;">有了这些锚点，就算还没读懂整个项目，也能安全地改造；真要改某个具体模块，到时候再深挖那个模块就行。</span>

老项目改造里有一个残酷的真相：永远不会真的读懂一个老项目。代码几万几十万行，写它的人换过好几拨，历史细节没人记得全。<span style="color: red; font-weight: bold;">要做的是建立"足以推进改造"的理解，不是"完整无缺的理解"。</span>这套流程就是在帮工程师建立这份"足以推进改造"的最小理解。

### 2.3 八步逐步展开

#### (1) 第一步：读 README 和根目录文档

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/c0ee4a6d78f42a1a2abfb88431a2a36c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

打开项目先看 <span style="color: red; font-weight: bold;">README</span>，再看 <span style="color: red; font-weight: bold;">docs</span> 目录。

别嫌弃 README 写得简陋，能看多少算多少。这一步不是为了"懂"，是为了建立一个<span style="color: red; font-weight: bold;">最粗的轮廓：这个项目做什么的、跑在什么环境、有哪些核心概念。</span>

#### (2) 第二步：扫项目结构

把仓库 clone 下来，看<span style="color: red; font-weight: bold;">目录和包组织</span>。

| 项目类型          | 查看位置                    |
| ------------- | ----------------------- |
| Java 项目       | `pom.xml` 的 `module` 划分 |
| Go 项目         | `cmd` 和 `internal`      |
| TypeScript 项目 | `packages` 或 `src` 一级目录 |

这一步的核心是建立一张模块地图：项目分几块、<span style="color: red; font-weight: bold;">每个模块大概干什么、哪些是核心、哪些是边缘</span>。不看代码细节，只看结构。

#### (3) 第三步：找核心入口

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/49b989e67ae47d86886a91b437b4ccf2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

知道模块地图之后，找<span style="color: red; font-weight: bold;">具体的入口</span>：

| 项目类型 | 入口位置 |
|---------|---------|
| HTTP 服务 | controller、main 函数 |
| 定时任务 | 调度注册处 |
| 消息消费 | consumer 或 listener |

入口很重要。它是从用户到代码的第一个接触点，代码所有的主流程都从入口出发。找到入口就找到了地图上的坐标原点。

#### (4) 第四步：画项目全景

到这一步对项目有了基础认知，但散碎。第四步要<span style="color: red; font-weight: bold;">把散碎的认知拼成一张图</span>：<span style="color: red; font-weight: bold;">架构图、模块依赖图、核心数据流图</span>。

这张图不需要精细，<span style="color: red; font-weight: bold;">粗糙的手绘都行</span>。关键是画出来，不要只停在脑子里。一画就会发现自己哪里想不清楚、哪里的关系没理顺。

#### (5) 第五步：梳理接口和数据模型

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/c15f40557d9730c69ac29a2db30dd885_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

有了全景之后深入一层：项目对外暴露什么接口？内部核心数据结构是什么？

<span style="color: red; font-weight: bold;">接口是项目的"对外契约"，数据模型是项目的"内部骨骼"。</span>把这两样梳理清楚，就掌握了这个项目的输入输出和核心抽象。

#### (6) 第六步：搭起环境跑起来

理论上已经对项目有了相当的认知，但真正的检验是跑起来。

搭环境经常比想象的难。依赖的数据库版本、中间件、内部服务、环境变量，每一个都可能卡半天。<span style="color: red; font-weight: bold;">跑不起来是正常的，跑起来的那一刻对项目会有质的认知飞跃：</span>能打断点、能看日志、能复现问题，项目从"纸面上认识"变成"手里能操作"。

#### (7) 第七步：带着需求深挖代码

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/9488a55db11f264ecdf6eaaae3220994_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

跑起来之后，不要继续通读。开始带着具体需求深挖：<span style="color: red; font-weight: bold;">要改哪个模块？要加什么功能？带着这个问题回到代码里，沿着调用链路深挖。</span>

这一步读的代码是有目标的。每一处都带着问题：这里为什么这么写？这段逻辑会被谁调用？改了会影响哪里？<span style="color: red; font-weight: bold;">有目标的读胜过无目标的读一百倍。</span>

#### (8) 第八步：沉淀成 AI 能读的文档

前面七步做完，脑子里清楚了，但这不够——理解留在脑子里 AI 读不到（这一点 2.2 第 2 条已经讲透，这里只回指）。得把理解固化到文档里，让 AI 也能读到。

具体就是两份东西：先说它们是什么。

**① `CLAUDE.md`** 

相当于传统软件里给新同事写的"项目交接备忘录"——<span style="color: red; font-weight: bold;">项目背景、入口位置、团队约定、踩过的坑</span>，本来就该有这么一份；现在只是改成 AI 能读的格式，让 Claude Code 一进来就拿到这份交接。

**② `SKILL.md`** 

相当于<span style="color: red; font-weight: bold;">团队沉淀的 SOP 操作手册——某类任务怎么一步步做</span>，本来也是团队该有的资产；现在改成 AI 能读的格式，让 Claude Code 照着 SOP 干活。前七步的产出，一股脑写进这两份文档里。

### 2.4 八步速查 Check List

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/1bb0970f858f6aefc4a71efdd7c07d4c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

下面这份清单按这八步分组，可按项目实际情况裁剪后贴到自己的 `CLAUDE.md` 或工作笔记里。接到一个新老项目时按清单走一遍，能避免漏步。

#### (1) 步 1-2：建立轮廓

- [ ] README 是否已读完？关键概念是否摘到笔记里？
- [ ] docs / wiki / 内部知识库是否扫了一遍？
- [ ] 项目对外的一句话定位能不能写出来（做什么的、跑在哪）？
- [ ] 模块地图是否画出（分几块、谁核心谁边缘）？
- [ ] Maven / Go / TS 的包结构是否过了一遍？

#### (2) 步 3-4：定位入口与全景

- [ ] HTTP 入口（controller / main）是否列出？
- [ ] 定时任务、消息消费者入口是否列出？
- [ ] 是否能从某个入口追一条主流程到落库？
- [ ] 架构图是否画出（哪怕手绘）？
- [ ] 模块依赖图、核心数据流图是否各画一张？

#### (3) 步 5-6：契约与可运行

- [ ] 对外 REST / RPC 接口清单是否整理成 markdown？
- [ ] 核心数据结构（实体表 / DTO）是否列出关键字段？
- [ ] 中间件依赖清单是否齐（DB / Cache / MQ / 配置中心）？
- [ ] 本地能否启动？能否打断点、看日志、复现一个请求？

#### (4) 步 7-8：带需求深挖与沉淀

- [ ] 是否带着一个具体需求沿调用链深挖过一段代码？
- [ ] `CLAUDE.md` 是否已沉淀（项目背景、入口、约定、坑）？
- [ ] `SKILL.md` 是否已沉淀（可复用的操作模板）？
- [ ] 沉淀的内容是否 AI 也能读懂（结构化、有上下文）？

## 3. 实战主线：Spring AI Alibaba Admin

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/faf8a21526aae71de8b27c07feb6f7ea_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这套改造流程不是空理论，要在真实项目上验证才站得住。<span style="color: red; font-weight: bold;">本专栏的实战主线是 Spring AI Alibaba Admin</span>，后续会在这个项目上一步步走完八步。

### 3.1 项目背景与定位

Spring AI Alibaba Admin 是阿里巴巴官方开源的 Agent Studio 管理平台，挂在 spring-ai-alibaba 主仓库下作为子项目。定位是 AI Agent 的一站式开发平台，支持 Prompt 管理、Dataset 管理、Evaluator、实验、Observability、多模型配置。启动后是一个跑在 8080 端口的 Web 服务，有前端管理界面、一套 REST API，依赖 MySQL 和 Nacos。Apache 2.0 协议，Java + React 前后端分离。

### 3.2 为什么选它当实战主线

| 理由 | 说明 |
|------|------|
| 长得像每天打交道的老项目 | 标准 HTTP 服务 + Spring Boot + MySQL + REST 接口 + 独立前端 + Nacos/OpenTelemetry，一个真实形态的企业级微服务，不是简化 demo |
| AI 方向贴合 | Prompt 管理 / Evaluator / Trace 观测是 AI 工程师日常在用的功能，改起来有代入感、能快速进入状态 |
| 开源活跃、背书强 | 阿里巴巴官方维护、Apache 2.0、中文资料多、可真实提 issue/PR，不会出现"项目失联"或"文档过时" |

<span style="color: red; font-weight: bold;">它不是什么简化的 demo 项目，而是一个真实形态的、完整的企业级微服务：</span>

```
多模块 Maven（4 个 server 子模块 + frontend）
前后端分离
接数据库、接中间件
有 Actuator
```

<span style="color: red; font-weight: bold;">在这个项目上练出的方法论，能一比一迁移到公司那个跑了几年的老系统上。这是拿它当主线最根本的理由。</span>

## 4. 老项目改造的残酷真相与复利

<img src="imgs/aicmigr-06-proj-rd-01-steps-readme-to-core-flow/ee0a4d41bf4e004ffa45008f56a50eee_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 4.1 摸清比改更难

<span style="color: red; font-weight: bold;">老项目改造最难的第一步从来不是改，是摸清。</span>

为什么摸清这么难？因为代码几万几十万行，写它的人换过好几拨，当时的取舍没人记得全，文档还常常对不上代码。任何一个接过老项目的人都体验过那种挫败感：看了三天，觉得自己懂了，第四天遇到一个边界条件，发现之前的理解全推翻。

<span style="color: red; font-weight: bold;">老项目改造里有一个残酷的真相：永远不会真的读懂一个老项目。这个真相在第 2 章只是点了一句，这里要讲透。</span>读懂是一个不可能完成的目标，盯着它只会把人耗死在代码里。

<span style="color: red; font-weight: bold;">既然读不懂，那要的是什么？就像前面第 2 章说的，目标是建锚点，不是读懂。</span><span style="color: red; font-weight: bold;">要做的是建立"足以推进改造"的理解，不是"完整无缺的理解"。</span>这套改造流程就是在帮工程师建立这份最小理解：知道核心链路从哪到哪、哪些模块是高风险、哪些接口是对外契约、历史上踩过哪些坑。抓住这几个把手，就算没读懂整个项目，也能安全地动刀。真要改某个具体模块，到时候再深挖那一段就行。

摸清这件事，过去靠工程师的经验和韧性，一年接一两个老项目，磨出感觉。现在有 AI，方法论不变，只是执行更快——<span style="color: red; font-weight: bold;">同样的扫结构、梳接口清单、生成全景图，AI 把过去靠体力磨的活儿大幅提速。但"知道要摸清什么、摸到什么程度算够"这个判断，依然在人手里。</span>

### 4.2 走出自己的节奏

本篇给的是骨架，不动手。把多年接老项目的一套动作诚实地交给读者，读者拿去在自己的项目上走一走、改一改，走出属于自己的节奏。

走完八步不等于万事大吉。每个项目的坑长得不一样，每个团队的节奏也不一样。<span style="color: red; font-weight: bold;">这套流程是地图，不是导航——地图告诉你哪里有山、哪里有河，但具体怎么走，得自己迈腿。</span>

冷启动是最难熬的一段。前面几步看似都在做无用功：读 README、扫目录、画粗糙的全景图，每一步都离"能改代码"还远得很。但锚点就是在这些看似无用的动作里一点点长出来的。等到第七步带着需求深挖时回头一看，会发现自己已经站在了起步时想都不敢想的位置。

<span style="color: red; font-weight: bold;">熬过冷启动，后面全是复利。</span>

### 4.3 思考

#### (1) 关于过去的复盘

回想最近接手的一个老项目，这套改造流程里哪几步没走到或者走得不扎实？如果让你重来一次，会补哪几步？

#### (2) 关于沉淀的检验

手上现在正在维护的项目，如果让你给下一个接手的同事留一份 `CLAUDE.md`，能写多少？写不出来的部分，是因为自己也没搞清楚，还是因为没来得及沉淀？
