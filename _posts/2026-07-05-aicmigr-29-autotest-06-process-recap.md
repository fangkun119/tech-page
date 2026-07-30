---
title: 传统项目迁AI 29：自动测试 - 流程回顾
author: fangkun119
date: 2026-07-05 09:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-29-autotest-06-process-recap/cover.jpg
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
aicmigr-29-autotest-06-process-recap
传统项目迁AI 29：自动测试 - 流程回顾
-->

## 1. 一句话需求落地全景：从模糊到 7×24 跑起来

### 1.1 传统流程为何失灵

<img src="imgs/aicmigr-29-autotest-06-process-recap/70a0c4bf1e6280e526ed1ba8d31e05ad_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

设想这样一个场景：leader 丢过来一句话——"用 Hermes Agent 给 RobustMQ 实现 7×24 不间断跑混沌测试的系统"，让你落地。

传统工程师拿到这种需求会卡在哪？瀑布流程里写需求文档、画架构图、排开发计划那一套，碰到"AI Agent 7×24 自主跑混沌测试"就开始失灵。三个原因：

- 需求模糊到只有一句话，没有可拆解的输入
- 交付物不是一个 Web 服务，而是一个能自己起集群、注入故障、跑 SDK、出报告的 AI 系统
- 怎么让 AI 按预期干活，不像调 REST API 那么直接

<span style="color: red; font-weight: bold;">这篇文章解决的就是这个问题：从一句话需求出发，一步步落地到一套能在生产环境 7×24 跑的 AI Agent 系统。</span>

### 1.2 最终产物全景图

跑完整套流程，手里会有这些东西：

| 产出 | 说明 |
|------|------|
| `docs/design.md` | 与技术栈无关的设计文档，相当于系统"宪法" |
| `docs/solution.md` | 基于选定技术栈的方案文档，定义每个 Tool 的接口和约束 |
| 5 个 Tool | `cluster.py`、`observability.py`、`client.py`、`chaos.py`、`report.py` |
| 1 个 `SKILL.md` | 串起 5 个 Tool，告诉 Agent 按什么顺序调 |
| 业务层 | 场景库 + 报告模板 + `config.yml` + Deploy Key + `cron.yml` |
| GitHub 报告 | 每次 run 的测试报告自动 push 到公开仓库 |

类比一下：这就像基于 Spring Boot 二次开发一个业务系统。Spring Boot 是框架（这里是 Hermes），业务代码是 Tool，启动配置是 Skill，测试用例是场景库，CI 流水线是 cron 调度。你不用从零造轮子，但要清楚在哪里接、怎么接。

整套落地走六个阶段。<span style="color: red; font-weight: bold;">前三阶段"想清楚做什么"，后三阶段"把它做出来并跑起来"。</span>

| 阶段 | 核心动作 | 关键产出 |
|------|----------|----------|
| 第一次翻译 | 把一句话需求展开成设计文档 | `docs/design.md` |
| 路径反问（硬约束） | 拿刚摸过的开源项目对比"自研"路径 | 路径决策 |
| 第二次翻译 | 基于选定路径重写方案文档 | `docs/solution.md` |
| 代码层 | 5 个 Tool + Skill 骨架 | `~/.hermes/skills/robustmq-chaos-test/` |
| 业务层 | 场景库、报告模板、配置、调度 | scenarios + templates + config.yml + cron.yml |
| 跑通验证 | 加载验证 + 对话跑通 + 报告 push | GitHub 上能看到报告 |

### 1.3 阶段与时间预算

<img src="imgs/aicmigr-29-autotest-06-process-recap/4d5f92d216386c11429d6aab14bc4e64_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前三阶段对应传统软件的需求评审 + 架构设计。区别在于：传统流程里评审对象是人和文档，这里评审对象是 AI 给出的展开结果。

你可能会问：为什么不直接让 AI 一次写完设计文档？<span style="color: red; font-weight: bold;">因为 AI 不会主动告诉你它漏了什么。</span>一次写完的版本总有空洞，需要工程师拿着"完整设计文档该长什么样"的预期，一处处反问、补漏、整合。这就是后续要讲的"三步法"。

中间还插一个硬约束——**路径反问**。这是整套工作流最值钱的工程教训：第一次翻译跑完后，先停下来反问"路径选对了吗"，别让 AI 顺着提问给一条"自研"路径，而忽略了你刚摸过的开源项目已经做了 80%。

后三阶段对应编码、集成、上线：

- **代码层**：5 个 Tool + Skill 骨架。AI 写代码最容易翻车的地方在提示词——接口签名、硬约束、已有依赖三件事说不清，写出来的 Tool 跑不通。
- **业务层**：场景库、报告模板、配置、cron 调度。代码骨架长出来后，还要填上"测什么、怎么报、多久跑一次"，系统才能真正 7×24 跑。
- **跑通验证**：加载、对话、push 三件事。后文还会给出一段"一键流程"提示词，让 Claude Code 自主跑完前面所有阶段，只在关键决策点停下来等你。

| 阶段 | 时间预算 |
|------|---------|
| 第一次翻译（含路径反问） | 20-30 分钟 |
| Hermes 验证机制 | 20-30 分钟 |
| 代码层（5 个 Tool + Skill） | 1.5-2 小时 |
| 业务层 | 1 小时 |

这套预算的前提是 Hermes 已装好、对它有顶层认知。如果跳过准备直接上手，每一步都会卡。

下一章先讲第一次翻译：怎么把这一句话展开成完整设计。

## 2. 第一次翻译：把一句话展开成完整设计

<img src="imgs/aicmigr-29-autotest-06-process-recap/150c1652754f280c5c6019611ea038ea_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-29-autotest-06-process-recap/0db2ecc85459c2a00caa1ce247ecc49b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第一次翻译的目标，是把一句话需求展开成一份与技术栈无关的完整设计文档——讲清楚"为什么做、做什么、不做什么、用什么思路、覆盖什么场景、产出什么"。这一步决定了后续所有提示词的方向，方向错了，后面写得再细都是白费。

### 2.1 为什么不能让 AI 一次写完

你可能会问：直接让 AI 一次把设计文档写完不行吗？

答案是：它会给你一个看起来完整的版本，但总有几处空洞——场景没分级、报告对内对外没说清、SDK 矩阵不系统。问题不在写得不够多，而在 AI 不会主动承认漏了什么。它只会顺着你的提问往下走，你不问，它就不补。

所以这件事必须分三步：先展开、再反问补漏、最后整合成 PRD。

类比一下，这就像需求评审做三轮——第一轮把骨架搭出来，第二轮挑空洞，第三轮定稿。一轮定稿的评审没人敢签字，一轮写完的设计文档同理。

### 2.2 三步法：展开、反问、整合

#### (1) 第一步：展开需求

展开阶段先别谈具体技术框架，把"为什么做、做什么、不做什么、用什么思路、覆盖什么场景、产出什么"讲清楚。让 AI 列出关键选择给你看，每个选择给两三个选项和影响，由你来拍。

提示词原文：

```text
帮我把这个一句话需求展开成完整的技术设计文档:用 AI Agent 给 RobustMQ
实现 7×24 不间断跑混沌测试的系统,自动起集群、注入故障、跑多语言 SDK、
收日志、出报告。

这一步先别谈具体技术框架,把＂为什么要做、做什么、不做什么、用什么思路、
覆盖什么场景、产出什么＂讲清楚。

有几个我自己一时拍不准的关键选择,你列出来给我看,每个给两三个选项和
影响,我来拍。

不要复述,要展开。
```

review 重点：

- 设计文档六块都有内容（为什么／做什么／不做什么／思路／场景／产出）
- AI 列出来的"待拍板的选择"至少 3 个
- 每个选择带选项和影响，不是空泛的"由你决定"

#### (2) 第二步：反问补漏

跑完第一步，AI 给的设计文档总有几处空洞，把空洞挑出来反问。

反问是"你这里漏了，补上"，不是追问。AI 不会主动告诉你它漏了什么，你需要自己有一份"完整设计文档该长什么样"的预期，拿这份预期去比对 AI 给的版本，差什么就反问什么。预期从哪来？从工作经验里来。

提示词原文：

```text
你这版有几处空洞:

1\. 场景优先级没分。混沌测试持续跑,得有 P0/P1/P2 这种分级,
   按触发频率匹配。补一下。

2\. 测试报告对内还是对外?这件事影响存储、Dashboard、脱敏一系列设计。
   给我两三个选项,我来拍。

3\. SDK 矩阵不够系统。具体覆盖哪些语言、每种语言哪几个版本、
   怎么切换版本,展开。

别又冒新选择,把这版补完整。
```

#### (3) 第三步：整合成 PRD

把前两轮讨论的所有产出整合成一份完整的技术设计文档，能拿出去给社区评审。

提示词原文：

```text
基于前面两轮讨论,把所有产出整合成一份完整的技术设计文档,
能拿出去给社区评审的那种。

至少包括:
1\. 背景和目标(为什么做)
2\. 系统设计思路(全流程 AI 自动化)
3\. 工具集清单(7 个工具函数,每个的签名和职责)
4\. 场景库分级(P0/P1/P2)
5\. SDK 矩阵(完整两张表:MQTT 和 mq9)
6\. 协议兼容性归因表(测试发现问题→具体在哪里改)
7\. 测试记录公开度决策

保存到 docs/design.md。
```

review 重点：协议兼容性归因表（不同 SDK 不一致／特定版本失败／全部失败 → 三种归因）有没有写进文档。这张表后面报告模板会反复用。

### 2.3 AI 不能替你拍的四类决策

三步法走完，设计文档成型了，但有几类决策 AI 不能替工程师拍。<span style="color: red; font-weight: bold;">提示词里要显式写"停下来等工程师反馈"，让 AI 知道这些点不能自作主张：</span>

| 决策点 | 说明 |
|--------|------|
| 第一次翻译的关键选择 | 场景分级、报告公开度、SDK 矩阵 |
| 路径选择 | 自研 vs 用开源项目 |
| 方案文档决策点 review | Skill 边界、故障注入选型、GitHub 凭据 |
| 业务层场景库范围 | 接下来要写哪些场景 |

<span style="color: red; font-weight: bold;">不要让 AI 自作主张。AI 给的是 80 分初稿，工程师要做的是先判断方向对不对，不是埋头改成 95 分。方向错了，改得越细偏差越大。</span>

光是补漏还不够，<span style="color: red; font-weight: bold;">第一次翻译跑完还要先反问一件事——路径选对了吗。</span>这是下一章的主题。

## 3. 路径反问：最值钱的硬约束

<img src="imgs/aicmigr-29-autotest-06-process-recap/771f2e0c17bc0bc11d4daa17c00c2dfb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-29-autotest-06-process-recap/80c480889e8bcd1791e20db01150e980_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

整套工作流跑下来，最值钱的教训一句话：**第二次翻译前先反问"路径选对了吗"**。

类比一下：跑长途前先看地图，而不是闷头踩油门。<span style="color: red; font-weight: bold;">油门踩得再狠，方向反了也是越开越远。</span>AI 时代的工程师最容易犯的就是这种错——代码层面 AI 帮你踩得很欢，但路径层面没人替你看地图。

### 3.1 教训是什么：一次真实的工程翻车

这条硬约束来自一次真实的工程翻车，整章只讲这一次。

第一次跑方案文档时，AI 顺着提问给了一条"自研 Claude API tool use"的路径。但刚摸过的 Hermes 开源项目，已经把这条路径里 80% 的工程都做了——剩下的 20% 才是需要自己写的。<span style="color: red; font-weight: bold;">AI 不会主动告诉你这件事，它只会顺着你的提问方向走。</span>

所以第一次翻译跑完之后、第二次翻译之前，必须停下来反问："路径选对了吗？"

### 3.2 先验证 Hermes 能用

第一次翻译跑完，手里有完整设计文档。下一步要做"路径反问"——对比自研 vs 用 Hermes。但你得先确认 Hermes 真能用、真能扩展，否则反问就成了空谈。

这一步是动手命令，不是 AI 提示词。

#### (1) 装 Hermes 并感受手感

```bash
# 安装 Hermes
curl -fsSL https://res1.hermesagent.org.cn/install.sh | bash

# 跟它聊一聊,感受手感
hermes
```

对话示例：

```
> 你能帮我做什么?
> 帮我看看磁盘空间占用,列出最大的 5 个目录
```

#### (2) 写最小 Skill 验证机制

在 `~/.hermes/skills/hello-world/` 下新建 `SKILL.md`，写一个返回当前时间的 Skill。跑通就行，这一步只是验证 Hermes 真的能扩展。

`hermes /skills` 能看到 hello-world 出现在列表里，说明 Hermes 加载机制活着，后面正式项目就照同样的机制写。

### 3.3 反问动作与产物

<span style="color: red; font-weight: bold;">AI 给的方案不一定是最优的。AI 顺着工程师的提问给方案，但工程师刚摸过的那个开源项目，可能已经把方案里大部分工程都做了，不用自己撸。</span>

停下来之后做什么？三件事，把"反问"从一个口号变成可执行的动作：

- 拿出第一次摸开源项目时得到的认知
- 对比"自研"和"用开源项目"两条路径
- 列出每条路径要做什么、工程量多大

反问的产物要落到三处，让经验沉淀成流程，而不是停在某个人的脑子里：

| 产物 | 落点 |
|------|------|
| 写进方案文档 | 让后续类似工作流的 AI 主动提醒工程师做反问 |
| 一键流程的"第零步反问" | 把反问从经验沉淀成流程 |
| 工作流的硬约束阶段 | 路径反问作为六阶段之一强制执行 |

我的看法是：这件事之所以最值钱，<span style="color: red; font-weight: bold;">因为它不是"AI 写错了代码"，而是"AI 选错了方向"。代码错了可以 debug，方向错了改 100 遍也是错的。</span>

### 3.4 AI 时代工程师最值钱的交付

路径反问这件事，背后是 AI 时代工程师角色的根本转变。

<span style="color: red; font-weight: bold;">一个工程师在 AI 时代能交付的最值钱的东西，不是写代码的能力，是把方向定准、把约束讲清楚、把模糊变具体的能力。</span>

<span style="color: red; font-weight: bold;">AI 给工程师一个 80 分的初稿，工程师要做的不是改成 95 分，是先判断初稿是不是建立在对的方向上。方向不对，改 100 遍也是错的。</span>

这套工作流的每一步都在印证这件事：

| 工作流环节 | 工程师守的方向 |
|------------|----------------|
| 第一次翻译的反问 | AI 展开的方向对不对 |
| 路径反问 | AI 选的技术路径对不对 |
| 每个 Tool 的 review | AI 写的接口和约束对不对 |
| 跑通验证 | 整条链路的方向对不对 |

<span style="color: red; font-weight: bold;">工程师守住方向感，AI 才能真正发挥威力。这是整套工作流想传递的核心。</span>

方向定准之后，下一步就是让 Agent 长出 5 个 Tool——这是代码层的事。

## 4. 代码层：让 Agent 长出 5 个 Tool

<img src="imgs/aicmigr-29-autotest-06-process-recap/9c400a8089d4110fd79be381f04868c1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-29-autotest-06-process-recap/a287618b9bf981567dbe46f987798905_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

代码层的目标，是按 `docs/solution.md` 的设计，让 AI 写出 5 个 Tool 和 1 个 Skill 骨架。这是整个工作流中篇幅最大的一章——提示词写得清不清，直接决定 Tool 跑不跑得通。

### 4.1 提示词三件事：接口签名、硬约束、已有依赖

写 Tool 的提示词时，三件事缺一不可。类比一下：这就像写函数前先定接口契约——函数签名、错误处理约定、依赖说明。把这三件事在提示词里说死，AI 就不会自己发明调用约定、用默认值兜底、或重造已有的轮子。

| 维度 | 内容 | 缺失的后果 |
|------|------|-----------|
| 接口签名 | 函数签名、参数、返回值格式 | AI 编出不一致的调用约定 |
| 硬约束 | fail-fast 规则、不能抛异常、不引入 Docker 等禁令 | AI 用默认值兜底，跑起来不对 |
| 已有依赖 | 环境变量、配置文件、上下游 Tool | AI 自己重新发明已有的东西 |

<span style="color: red; font-weight: bold;">如果某个 Tool 写出来跑不通，十有八九是这三件事里某一件没说清，回提示词补两句重跑就行。</span>

写 Skill 的提示词时还有一条特殊约束——必须把 5 个 Tool 的接口签名全部贴给 AI。Skill 的任务是告诉 Agent 在 cron 触发或手动触发时按什么顺序调 Tool。如果不贴接口，AI 会编出根本不存在的 Tool 调用，Skill 加载之后跑就报错。

### 4.2 依赖顺序

按 `docs/solution.md` 的依赖顺序写：cluster → observability → client → chaos → report → SKILL。

前一个是后一个的依赖：cluster 起集群，observability 在集群上打快照，client 在集群上跑 SDK，chaos 给集群注故障，report 整合所有结果。SKILL 最后写，串起 5 个 Tool。

### 4.3 五个 Tool 的职责与提示词

五个 Tool 同构，统一用"职责 + 提示词原文 + 三件事体现&review 重点"三段式呈现。提示词原文一字不改，硬约束一条不删。

#### (1) cluster.py：起停测试集群

职责：混沌测试 Skill 套件的第一个 Tool，负责在本地起停 RobustMQ 测试集群。

提示词原文：

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/cluster.py。

这是混沌测试 Skill 套件的第一个 Tool,负责在本地起停 RobustMQ 测试集群。

按 docs/solution.md 的决策:不引入 Docker,直接 spawn 多个 RobustMQ 进程。

action 支持 start / stop / status 三种。

start: 用 subprocess 拉起 3 个 RobustMQ broker 进程,每个用独立端口
       (broker-1: 1883, broker-2: 2883, broker-3: 3883)和独立数据目录。
       数据目录用 tempfile.mkdtemp 创建,记下来 stop 时清理。
       启动后 curl http://127.0.0.1:1883/health 确认健康,失败立即 kill 全部
       并返回 {"status": "failed"}。
       成功返回 {"status": "running", "endpoint": "127.0.0.1:1883",
                  "data\_dirs": \[...\]}。

stop: kill 所有 broker 进程,清理 data\_dirs。

status: 返回当前运行的进程数和 endpoint。

RobustMQ 二进制路径通过 ROBUSTMQ\_HOME 环境变量取,没设置就 fail-fast
返回 error,别用默认值兜底。

handler 函数签名 (args: dict, \*\*\_) -> str,失败返回 {"error": "..."}
别抛异常,Agent 循环见到异常会中断。
```

三件事体现 & review 重点：

| 维度 | 体现 |
|------|------|
| 接口签名 | action 支持 start / stop / status、handler 函数签名 (args: dict, **_) -> str |
| 硬约束 | 不引入 Docker、ROBUSTMQ_HOME 没默认值是 fail-fast、失败返回 error 不抛异常 |
| 已有依赖 | tempfile.mkdtemp、http://127.0.0.1:1883/health、ROBUSTMQ_HOME 环境变量 |

review 重点：不引入 Docker、`ROBUSTMQ_HOME` 没默认值是 fail-fast、临时目录 stop 时清理、失败返回 error 不抛异常。这四条任何一条没做到，回提示词补一句重跑。

#### (2) observability.py：收集观测数据

职责：从 RobustMQ 集群收集观测数据，后面 chaos 故障注入和 client SDK 测试都会调它打快照。

提示词原文：

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/observability.py。

负责从 RobustMQ 集群收集观测数据,后面 chaos 故障注入和 client SDK
测试都会调它打快照。

action 支持 collect\_logs / collect\_metrics / snapshot 三种。

collect\_logs 从 broker 进程的 log 文件抓最近 100 行,按节点返回。

collect\_metrics 调 RobustMQ 的 /metrics 端点,返回 connections /
messages\_in / messages\_out / errors。

snapshot 一次性收 logs + metrics 加时间戳,用于故障前后对比。
```

review 重点：snapshot 必须带时间戳、要能用于"故障前后对比"——这是后面判断自愈是否成功的依据。

#### (3) client.py：调度多语言 SDK

职责：调度多语言 SDK 跑测试。按 `solution.md` 决策不用 Docker 隔离，用本地版本管理工具切换。

提示词原文：

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/client.py。

负责调度多语言 SDK 跑测试。按 solution.md 决策:不用 Docker 隔离,
用本地版本管理工具切换。服务器上预装了 pyenv / gvm / rustup /
sdkman / nvm。

action 是 run,参数 sdk(python/go/rust/java)、version、scenario、
cluster\_endpoint。

内部:
1\. 用版本管理工具切换到指定版本
2\. 进入 sdk\_clients/\<sdk>/ 目录跑入口脚本
3\. 入口脚本通过 CLUSTER\_ENDPOINT 环境变量接收集群地址,stdout 最后
   一行必须输出约定 JSON({sent, received, lost, p99\_ms, errors})
4\. 解析最后一行 JSON,exit code 表示通过失败

注意:解析最后一行 JSON 失败,单独记 status=script\_format\_error,
别跟测试失败混在一起算。

不传 sdk 时,用 ThreadPoolExecutor 并发跑全部语言。并发逻辑放 Python 里,
别用 Hermes 的 delegate 机制。
```

三件事体现 & review 重点：

| 维度 | 体现 |
|------|------|
| 接口签名 | action 是 run、参数清单、约定 JSON 字段 |
| 硬约束 | JSON 解析失败单独记 `script_format_error`、并发逻辑放 Python 不用 delegate |
| 已有依赖 | pyenv / gvm / rustup / sdkman / nvm、CLUSTER_ENDPOINT 环境变量 |

review 重点：`script_format_error` 不能跟测试失败混算；并发用 Python `ThreadPoolExecutor` 不用 Hermes delegate。

#### (4) chaos.py：故障注入与恢复

职责：故障注入和恢复。按 `solution.md` 决策 Chaosd 主 + tc/kill 补。

提示词原文：

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/chaos.py。

负责故障注入和恢复。按 solution.md 决策:Chaosd 主 + tc/kill 补。

action 支持 inject / recover。

inject: 接收 fault\_type、target、duration\_seconds 和具体参数,调
        Chaosd HTTP API 注入。返回 {"fault\_id": "...", "status": "active"}。

recover: 按 fault\_id 撤销故障。

第一版只实现 broker-kill 和 network-delay 两种,其他先返回
not\_implemented。

Chaosd 端点配置在 ~/.hermes/skills/robustmq-chaos-test/config.yml 里
读,don't hardcode。
```

三件事体现：

| 维度 | 体现 |
|------|------|
| 接口签名 | action 支持 inject / recover、返回字段 |
| 硬约束 | 第一版只实现两种故障、其他返回 `not_implemented`、Chaosd 端点不 hardcode |
| 已有依赖 | Chaosd HTTP API、config.yml |

#### (5) report.py：生成报告并 push GitHub

职责：整合整轮测试结果，生成报告并提交到 GitHub。

提示词原文：

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/report.py。

负责整合整轮测试结果,生成报告并提交到 GitHub。

action 是 generate\_and\_push,接收 run\_data dict。

内部:
1\. 用 Jinja2 模板渲染 Markdown 报告。模板路径 templates/report.md.j2,
   下一步会写。
2\. 用 json.dumps 写 JSON 报告。绝对别调 LLM 生成 JSON。
3\. git push 到 GitHub 公开仓库 test-reports。仓库地址从 config.yml 读。
   git push 用 Deploy Key,通过 GIT\_SSH\_COMMAND 指定 ~/.ssh/
   test-reports-deploy。
4\. 返回 {"json\_path", "markdown\_path", "github\_url", "run\_passed"}。
   run\_passed 字段的逻辑:核心场景全过 + 非核心场景通过率 ≥ 75%。

写新报告时清理 30 天前的本地临时文件。
```

三件事体现 & review 重点：

| 维度 | 体现 |
|------|------|
| 接口签名 | action 是 generate_and_push、返回字段、run_passed 判定逻辑 |
| 硬约束 | JSON 用 `json.dumps` 不调 LLM、Deploy Key 通过 `GIT_SSH_COMMAND` 指定、清理 30 天前临时文件 |
| 已有依赖 | Jinja2 模板、config.yml、~/.ssh/test-reports-deploy |

review 重点：`run_passed` 字段的判定逻辑（核心场景全过 + 非核心通过率 ≥ 75%）不能丢，这是 cron 跑完后看报告的第一眼指标。

### 4.4 SKILL.md 骨架：串起 5 个 Tool

5 个 Tool 写好后，Skill 的任务是告诉 Agent 在 cron 触发或手动触发时，按什么顺序调这些 Tool。

提示词原文：

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/SKILL.md。

5 个 Tool 已经写好了,Skill 的任务是告诉 Agent 在 cron 触发或手动触发
时,按什么顺序调这些 Tool。

5 个 Tool 是 cluster / observability / client / chaos / report。每个
Tool 的接口签名见下面贴的接口表。直接照着调,别编不存在的字段。

骨架按这几节来:
\- frontmatter,name=robustmq-chaos-test,requires\_tools 列全 5 个
\- When to Use,讲清 cron 触发("按 P0 跑一轮..." 这种)和手动 CLI 触发
  (hermes "按 P1 跑一轮 mq9 故障场景")各自怎么识别
\- 测试前置检查,确认无残留进程
\- 单场景执行五步:基线 snapshot → 注入故障 → 故障期间跑 SDK 测试
  (只记录不判断)→ 等故障窗口结束自动恢复 → 自愈验证(pass/fail
  唯一依据)
\- 通过失败判断,exit\_code=0 且 lost=0 且 p99\_ms<500
\- 报告归档,直接调 report 的 generate\_and\_push,它会处理 git push
\- Pitfalls

具体场景(broker-kill 用什么 target、network-delay 多少毫秒之类)
这步先留白,下一步再用一个新提示词把场景填进去。

\[贴 5 个 Tool parameters schema\]
```

review 重点——Skill 的灵魂：Tool 接口完整喂给 AI。<span style="color: red; font-weight: bold;">如果不把 Tool 接口贴给 AI，AI 会编出根本不存在的 Tool 调用，Skill 加载之后跑就报错。</span>这一步的代码没有写到本篇里，Claude Code 跑出来的真实代码都放在 GitHub 仓库里，想看代码直接去仓库对应文件看。

### 4.5 单点验证：怎么知道每个 Tool 活着

每个 Tool 写完后跑一次单点验证：

```
$ hermes
> 调用 cluster 起一个测试集群
```

Agent 真的去调了、返回结果回来，这个 Tool 就活着。某个 Tool 调不通也别慌，九成是 description 没写清楚或 parameters schema 不合法，回提示词改两句重跑就行。

代码层骨架长出来后，还要填上测什么、怎么报、多久跑一次——业务层负责这件事。

## 5. 业务层：填上场景与报告

<img src="imgs/aicmigr-29-autotest-06-process-recap/32418dc99af4e01bddc32cd663013ced_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-29-autotest-06-process-recap/f4bbafe1468605e7c49c6c67a7ce226d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

5 个 Tool 写好了，骨架立起来了，但还跑不起来。缺的是业务层：测哪些场景、报告长什么样、多久跑一轮、报告 push 到哪。这一章把四块填完。

### 5.1 铁律：结构化产物用代码生成，别交给 LLM

这一章开讲前，先立一条贯穿到底的铁律：**凡是结构化的东西都用代码生成，不要交给 LLM**。

你可能会问：让 LLM 顺手生成 JSON 不是更省事吗？不行。<span style="color: red; font-weight: bold;">LLM 生成 JSON 时格式漂移、字段缺失的概率远高于代码生成。</span>

解决方案分两条路，按产物类型选：

- JSON 报告用 `json.dumps` 写
- Markdown 报告用 Jinja2 模板渲染，模板里不调 LLM，纯 Jinja2 语法

模板负责把结构固定下来，数据从 Python 传进来。

类比一下：<span style="color: red; font-weight: bold;">这就像传统项目里用 FreeMarker/Thymeleaf 渲染报表——模板定结构，数据走代码，不会让 LLM 去拼 HTML。</span>转型读者把这条映射套进来就懂了：Jinja2 模板 ≈ FreeMarker 模板，`report.py` 传的 `run_data` ≈ Controller 塞进 Model 的数据。

### 5.2 第一个场景库文件

场景库是一堆 Markdown 文件，每个文件描述一个故障场景。Agent 读完知道按 `SKILL.md` 的"单场景五步"怎么调 Tool。

在 `~/.hermes/skills/robustmq-chaos-test/scenarios/mqtt/` 下写第一个场景 `p0-broker-kill-leader.md`。格式是 Markdown，自然语言描述。

提示词原文：

```text
帮我在 ~/.hermes/skills/robustmq-chaos-test/scenarios/mqtt/ 下
写第一个场景 p0-broker-kill-leader.md。

格式:Markdown,自然语言描述,Agent 读完知道按 SKILL.md 的＂单场景五步＂
怎么调 Tool。

具体内容:
\- 场景名:p0-broker-kill-leader
\- 协议:MQTT
\- 优先级:P0(每次触发都跑)
\- 集群:3 节点 RobustMQ,broker-1 是 Leader
\- 故障:用 chaos.py inject broker-kill,target=broker-1,duration=30 秒
\- SDK 矩阵:从 config.yml 读 P0 档的 SDK 列表
\- 验证:故障期间只记录,Chaosd recover 后等 broker-1 健康检查 200,
       再等 60 秒,跑完整 SDK 矩阵的 basic-pubsub
\- 通过标准:exit\_code=0,lost=0,p99\_ms<500
\- 失败处理:记录到 report,继续下一场景

写完不要解释,我直接看文件。
```

review 重点：

- 故障类型和参数跟 `chaos.py` 接口对得上
- 通过标准的字段名（`exit_code`／`lost`／`p99_ms`）跟 `client.py` 返回的 JSON 对得上

第一个场景写完，套同样模板写其他场景。场景库就是这样滚出来的——一份模板，N 份填空。

### 5.3 报告 Jinja2 模板

报告模板负责把结构固定下来，数据从 `report.py` 传进来，模板里不调 LLM。这呼应了 5.1 的铁律：模板纯 Jinja2 语法。

写 `~/.hermes/skills/robustmq-chaos-test/templates/report.md.j2`。

提示词原文：

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/templates/report.md.j2,
Jinja2 模板。

输入是 report.py 传进来的 run\_data,字段包括:run\_id / started\_at /
ended\_at / cluster\_info / scenarios(每个 scenario 含 fault\_info /
sdk\_results / passed)。

模板要点:
\- 顶部一段 summary,Run ID + 起止时间 + 整体 pass/fail
\- 每个 scenario 一个二级标题,展开 fault 信息和 SDK 矩阵结果
\- SDK 结果用 Markdown 表格
\- 底部一段＂协议兼容性归因＂,按 docs/design.md 那张归因表的逻辑套
  (不同 SDK 不一致 → 协议实现问题;特定版本失败 → 版本兼容问题;
   全部失败 → broker 端问题)
\- 别在模板里调 LLM,纯 Jinja2 语法

模板长度控制在 80 行以内。
```

review 重点：底部那段"协议兼容性归因"是设计文档对应到报告的真实落点。如果三种归因都不命中，模板要标"待人工分析"——AI 在边界 case 上容易偷懒，这里要手动收一下。

### 5.4 套件配置 config.yml

`config.yml` 定义 SDK 矩阵的三档分级和 GitHub 仓库信息。直接抄（按 `solution.md` 决策填值）：

```yaml
# ~/.hermes/skills/robustmq-chaos-test/config.yml
chaosd:
  endpoint: "http://127.0.0.1:31767"
sdk_matrix:
  p0:
  - {sdk: python, version: "3.11", scenarios: [basic-pubsub]}
  - {sdk: go, version: "1.21", scenarios: [basic-pubsub]}
  p1:
  - {sdk: python, versions: ["3.10", "3.11", "3.12"],
     scenarios: [basic-pubsub, failover]}
  - {sdk: rust, version: "1.70", scenarios: [basic-pubsub, failover]}
  p2:
  - {sdk: python, versions: ["3.10", "3.11", "3.12"]}
  - {sdk: go, versions: ["1.20", "1.21"]}
  - {sdk: rust, versions: ["1.70", "1.75"]}
  - {sdk: java, versions: ["11", "17", "21"]}
github:
  reports_repo: "git@github.com:<your-org>/test-reports.git"
  deploy_key_path: "~/.ssh/test-reports-deploy"
  branch: "main"
```

review 重点：P0 档刻意只放 2 个 SDK——P0 是基础保障线，跑得越快越好，大矩阵留给 P1／P2。

### 5.5 GitHub Deploy Key + cron 调度

最后两件事：让报告能 push 到 GitHub 仓库，让套件能按 P0／P1／P2 三档自动跑。

#### (1) Deploy Key 三步

这一步是动手命令，不是 AI 提示词。三步搞定：

```bash
ssh-keygen -t ed25519 -f ~/.ssh/test-reports-deploy \
  -C "robustmq-chaos-test deploy key" -N ""

GIT_SSH_COMMAND="ssh -i ~/.ssh/test-reports-deploy" \
  git clone git@github.com:<your-org>/test-reports.git /tmp/test-reports-init

cd /tmp/test-reports-init
echo "# RobustMQ Quality Reports" > README.md
git add README.md
GIT_SSH_COMMAND="ssh -i ~/.ssh/test-reports-deploy" \
  git commit -m "init" && git push
```

review 重点：勾 Allow write access 不能漏（默认是只读）。第三步 push 成功这一刻 Deploy Key 链路就活了。

#### (2) cron.yml 三档调度

调度配置直接抄：

```yaml
jobs:
- name: p0-mqtt-basic
  schedule: "0 */2 * * *"
  prompt: "按 P0 跑一轮 MQTT 基础场景"
- name: p0-mq9-basic
  schedule: "30 */2 * * *"
  prompt: "按 P0 跑一轮 mq9 基础场景"
- name: p1-daily-fault
  schedule: "0 3 * * *"
  prompt: "按 P1 跑一轮故障场景,MQTT 和 mq9 都跑"
- name: p2-weekly-matrix
  schedule: "0 4 * * 0"
  prompt: "按 P2 跑一轮完整 SDK 矩阵"
```

review 重点：P0 MQTT 和 mq9 错开 30 分钟避免资源冲突。频率配置直接来自 `design.md` 的 P0／P1／P2 分级。

业务层填完，最后一件事就是验证整条链路真的活着——这是下一章的主题。

## 6. 跑通验证与一键流程

<img src="imgs/aicmigr-29-autotest-06-process-recap/1073261ffcb985f63b709bfddf87b98c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面五章把流程拆成一步步，是为了让你看清每一步的产出和 review 点。本章先讲怎么确认整条链路真的活了，再把整套流程压成一段可粘贴的提示词，让 Claude Code 自主跑完。

### 6.1 跑通验证：怎么知道整条链路活了

业务层填完，最后一件事：验证整套系统真的活着。三件事做完就算通：

| 验证项 | 怎么做 | 通过标志 |
|--------|--------|---------|
| 加载验证 | `hermes /skills` 和 `/tools` | 5 个 Tool 和 Skill 都在列表里 |
| 对话跑通 | 用对话跑一次 P0 场景，7 轮调用全部展示出来 | Agent 真的按 Skill 指定的顺序调 Tool，没报错 |
| 报告 push | 看 GitHub 仓库 | 本次 run 的报告出现在仓库里 |

报告没 push 成功时，人工介入。这一步是 cron 跑完后看报告的第一眼，失败必须停下来。

### 6.2 为什么需要一键流程

前五章一个个跑，是为了让读者看清每一步的产出和 review 点。<span style="color: red; font-weight: bold;">真正上手之后，你会希望一次粘贴、让 Claude Code 自主跑完整流程，关键决策点停下来等你输入。</span>

一键流程的设计意图：把整套流程压成一段提示词，整段粘贴到 Claude Code。它不管装 Hermes 和点 GitHub UI（这两件需要人动手），从"已经摸过 Hermes、Hermes 已装好"这一步开始，跑到"5 个 Tool + Skill + 业务层全部就位"结束，最后停下来等你跑验证。

一个类比：一键流程相当于传统项目里的 CI/CD 流水线——人定义好关键 gate，机器自己跑。差别在于，这里的 gate 不是 lint 和测试，而是路径选择、方案 review、场景库范围这类需要人脑拍板的决策点。

### 6.3 一键流程提示词原文

```text
我刚拿到一个新需求:[把 leader 的一句话需求填这里,比如"用 Hermes Agent
实现一个 7×24 跑混沌测试的 AI 系统"]

完整跑通改造流程,全程自主推进,遇到关键决策点停下来等我,
不要每一步都问我。请按以下顺序执行:

第零步:第二次翻译反问(必做,不能跳)
- 这一步是这套工作流最关键的硬约束
- 等第一次翻译跑出方案文档之后,先停下来反问"路径选对了吗"
- 拿出我跑场景一时摸出来的 Hermes 认知,对比"自研"和"用 Hermes"
  两条路径,告诉我每条要做什么、工程量多大
- 等我反馈"按 Hermes 路径走"之后才能进第二次翻译

第一步:第一次翻译,从一句话到设计文档
- 把一句话需求展开成完整设计文档
- 列出待我拍板的关键选择(场景分级、报告公开度、SDK 矩阵)
- 停下来等我反馈

我反馈完后:
- 按反馈补完整,整合成正式 docs/design.md

第二步:执行第零步的反问
- 见第零步说明

我反馈"用 Hermes 路径"之后:

第三步:第二次翻译,基于 Hermes 重写方案
- 整体架构 + Skill 设计 + 7 个 Tool 实现要点 + 场景库 + 触发 +
  报告系统 + 关键决策记录 + 实施步骤
- 跑完后反问收紧:Skill 边界 / 故障注入选型 / GitHub 凭据
- 停下来等我审核 docs/solution.md 第 7 节决策记录

我反馈完后:
- 把决策落定到 solution.md

第四步:代码层,5 个 Tool + Skill 骨架
- 按依赖顺序:cluster → observability → client → chaos → report →
  SKILL.md
- 每个 Tool 写完跑一次单点验证 (hermes "调用 X")
- 任何 Tool 调不通,停下来报错给我

第五步:业务层
- 写第一个场景库文件,然后告诉我接下来要写哪些场景,等我反馈
- 写报告 Jinja2 模板
- 填 config.yml(SDK 矩阵根据需求决定档位)
- 提醒我手动生成 Deploy Key + 创建 GitHub 仓库(我自己做)
- 装 cron.yml 三档调度

第六步:跑通验证
- hermes /skills 和 /tools 看加载是否成功
- 用对话跑一次 P0 场景,7 轮调用全部展示给我
- 看 GitHub 仓库,确认报告 push 成功

自主原则:
- 每步跑完自己 review 输出质量,不合格自己重跑
- 失败自己 debug 自己修(除非连续 3 次同一错误)
- 接口签名 + 硬约束 + 已有依赖,提示词里都要交代
- Tool 提示词重传约束,Skill 提示词重喂 Tool 接口
- 结构化的东西用代码生成,JSON 别交给 LLM 写
- 关键决策点停下来等我,不要替我拍板

跑完输出 summary.md,列每个产出文件 + 我应该重点 review 的地方。
```

粘贴完等 Claude Code 跑。整个流程 4-5 小时（含几次 review）。<span style="color: red; font-weight: bold;">工程师不在的时间它在跑，工程师回来的时间它停在那里等判断。</span>

### 6.4 这段提示词的设计动机

这段提示词不是随便堆出来的，每一个设计点背后都有教训：

| 设计点 | 动机 |
|--------|------|
| 第零步"路径反问"摆在第零位强制等待 | 这是整套工作流最关键的差异。第一次翻译跑完先反问路径，而不是闷头写代码 |
| 关键决策点显式让 AI 停下来 | 四类决策（场景分级、路径选择、方案 review、场景库范围）AI 不能替工程师拍 |
| 所有硬约束都明确写进提示词 | 接口签名 + 硬约束 + 已有依赖、结构化产物用代码生成不交给 LLM，这些约束散落在前面几章，一键流程里必须全部明确写出来 |
| summary.md 集中暴露 review 点 | AI 不能完全替工程师思考，但可以把"我不确定的地方"集中到 summary 让工程师重点看 |

<span style="color: red; font-weight: bold;">回头看整套工作流，核心只有一件事：在 AI 时代守住方向感。</span>第一次翻译、路径反问、Tool review、跑通验证，每一步都是在问"方向对不对"。方向对了，AI 才能真正发挥威力。
