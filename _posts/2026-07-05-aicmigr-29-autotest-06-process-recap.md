---
title: 传统项目迁AI 29：自动测试 - 流程回顾
author: fangkun119
date: 2026-07-05 09:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
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


**AI 编程实战 · 自动测试流程回顾：从一句话需求到 7×24 跑起来**

本篇是"自动测试"系列的收尾篇。系列前几篇把"接到一句话需求"到"7×24 系统跑起来"的所有动作逐段拆解过，本篇把它们连起来跑一遍——从 leader 给出"用 Hermes Agent 实现 7×24 混沌测试"这一句话，到完整设计文档、完整方案文档、5 个 Tool、Skill、业务层、真实运转。

本篇既是实操回顾，也附上完整的提示词清单用于指导实践。跑完之后，工程师的项目里就有了一次完整的"基于开源项目二次开发"的闭环。

还有一件事必须开篇就说：系列第 25 篇第二次翻译时做过一次反问，把"自研 Claude API tool use"路径砍掉，改成基于 Hermes 重写。这次反问留下了一条新的硬约束——任何方案文档跑出来之前，先反问"路径选对了吗"。本篇工作流的第二步就是这件事。

**全文导读地图**

<img src="imgs/aicmigr-29-autotest-06-process-recap/6e6fba37372fbb6f9298cc19f97e5ece_MD5.jpg" alt="全文导读地图">

<!--
flowchart TD
    A["一句话需求<br>用 Hermes Agent 实现 7×24 混沌测试"] --\> B["第一次翻译<br>从一句话到设计文档"]
    B --\> C["路径反问（硬约束）<br>自研 vs 用 Hermes"]
    C --\> D["第二次翻译<br>基于 Hermes 重写方案"]
    D --\> E["代码层<br>5 个 Tool + Skill 骨架"]
    E --\> F["业务层<br>场景库 + 报告 + 配置 + 调度"]
    F --\> G["跑通验证<br>/skills 加载 + P0 对话跑通 + 报告 push"]
-->

**两类读者怎么读这篇文档：**

- 初学 AI 编程工程师：通读第一部分建立方法论框架，再按第二部分场景一到场景四的顺序复现一遍，每个 review 重点都对照自查
- 熟练 AI 编程工程师：直接看第 3 章 Check List 速查；遇到想回顾"为什么这么做"时回到第 2 章对应的方法论节，或第二部分对应场景

## 1. 工作流总览

<img src="imgs/aicmigr-29-autotest-06-process-recap/554819f1e92c932d465c86e16880ec67_MD5.jpg" alt="工作流总览">

### 1.1 工作流的工程语境

这套工作流要解决的问题很具体：工程师拿到 leader 的一句话需求（典型如"用 Hermes Agent 给 RobustMQ 实现 7×24 不间断跑混沌测试的系统"），需要把它落成一套能在生产环境跑起来的 AI Agent 系统。

整个工作流横跨六个阶段：第一次翻译、路径反问、第二次翻译、代码层、业务层、跑通验证。前三个阶段是"想清楚做什么"，后三个阶段是"把它做出来并跑起来"。

### 1.2 六阶段概览

| 阶段 | 核心动作 | 关键产出 |
|------|----------|----------|
| 第一次翻译 | 把一句话需求展开成设计文档 | `docs/design.md` |
| 路径反问（硬约束） | 拿刚摸过的开源项目对比"自研"路径 | 路径决策 |
| 第二次翻译 | 基于选定路径重写方案文档 | `docs/solution.md` |
| 代码层 | 5 个 Tool + Skill 骨架 | `~/.hermes/skills/robustmq-chaos-test/` |
| 业务层 | 场景库、报告模板、配置、调度 | scenarios + templates + config.yml + cron.yml |
| 跑通验证 | 加载验证 + 对话跑通 + 报告 push | GitHub 上能看到报告 |

### 1.3 阶段之间的依赖关系

第一次翻译产出的设计文档与具体技术栈无关，是后续所有工作的"宪法"。路径反问决定第二次翻译的方向，第二次翻译产出的方案文档定义了代码层每个 Tool 的接口签名和硬约束。代码层是骨架，业务层把骨架填满，跑通验证确认整条链路活着。

## 2. 关键方法论

### 2.1 第一次翻译三步法：展开、反问补漏、整合成 PRD

<img src="imgs/aicmigr-29-autotest-06-process-recap/d456804e2fc6765d3e53f9eda3c6d8d1_MD5.jpg" alt="第一次翻译三步法">

第一次翻译不是一次性写完设计文档，而是分三步走。

#### (1) 展开阶段

把一句话需求展开成完整的技术设计文档，这一步先别谈具体技术框架，把"为什么要做、做什么、不做什么、用什么思路、覆盖什么场景、产出什么"讲清楚。展开时让 AI 列出关键选择给工程师看，每个选择给两三个选项和影响，由工程师来拍。

#### (2) 反问补漏阶段

跑完展开阶段，设计文档总有几处空洞。工程师需要拿着"完整设计文档该长什么样"的预期去比对 AI 给的版本，差什么就反问什么。反问是"你这里漏了，补上"，不是追问——AI 不会主动告诉工程师它漏了什么。

#### (3) 整合阶段

把前两轮讨论的所有产出整合成一份完整的技术设计文档，至少覆盖七个维度：背景和目标、系统设计思路、工具集清单、场景库分级、SDK 矩阵、协议兼容性归因表、测试记录公开度决策。

### 2.2 第二次翻译前的路径反问硬约束

这条硬约束来自一次真实的工程翻车：系列第 25 篇第一次跑方案文档时，AI 顺着工程师的提问给了一条"自研 Claude API tool use"的路径，但工程师刚摸过的 Hermes 开源项目已经把这条路径里 80% 的工程都做了。

#### (1) 反问的时机

第一次翻译跑完之后、第二次翻译之前，停下来反问"路径选对了吗"。

##### ① 反问的内容

拿出第一次摸开源项目时得到的认知，对比"自研"和"用开源项目"两条路径，列出每条路径要做什么、工程量多大。

##### ② 反问的产物

这条教训要写进方案文档，让后续类似工作流的 AI 主动提醒工程师做反问。

### 2.3 提示词三件事：接口签名、硬约束、已有依赖

<img src="imgs/aicmigr-29-autotest-06-process-recap/aa8fa651236cffb2d9cc0fbea8c0d1fb_MD5.jpg" alt="提示词三件事">

写代码层的提示词时，三件事缺一不可。

| 维度 | 内容 | 缺失的后果 |
|------|------|-----------|
| 接口签名 | 函数签名、参数、返回值格式 | AI 编出不一致的调用约定 |
| 硬约束 | fail-fast 规则、不能抛异常、不引入 Docker 等禁令 | AI 用默认值兜底，跑起来不对 |
| 已有依赖 | 环境变量、配置文件、上下游 Tool | AI 自己重新发明已有的东西 |

### 2.4 Tool 提示词重传约束、Skill 提示词重喂接口

代码层有两类提示词，各有重传规则。

#### (1) Tool 提示词的重传约束

每个 Tool 的提示词要交代接口签名、硬约束、已有依赖三件事。如果某个 Tool 写出来跑不通，十有八九是这三件事里某一件没说清，回提示词补两句重跑就行。

#### (2) Skill 提示词的重喂接口

Skill 的任务是告诉 Agent 在 cron 触发或手动触发时按什么顺序调 Tool。这一步必须把 5 个 Tool 的接口签名全部贴给 AI——如果不贴，AI 会编出根本不存在的 Tool 调用，Skill 加载之后跑就报错。

### 2.5 结构化产物用代码生成，JSON 不交给 LLM

<img src="imgs/aicmigr-29-autotest-06-process-recap/383f2f9ece97260ace1fb2fc19f13b10_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

凡是结构化的东西都用代码生成，不要交给 LLM。

#### (1) JSON 报告

JSON 报告用 `json.dumps` 写，绝对不调 LLM 生成 JSON。LLM 生成 JSON 时格式漂移、字段缺失的概率远高于代码生成。

#### (2) Markdown 报告

Markdown 报告用 Jinja2 模板渲染，模板里不调 LLM，纯 Jinja2 语法。模板负责把结构固定下来，数据从 Python 传进来。

### 2.6 关键决策点显式停下来等人工

AI 不能替工程师拍四类决策：

- 第一次翻译的关键选择（场景分级、报告公开度、SDK 矩阵）
- 路径选择（自研 vs 用开源项目）
- 方案文档的决策点 review
- 业务层场景库的范围决策

提示词里要显式写"停下来等工程师反馈"，不要让 AI 自作主张。

## 3. 项目阶段 Check List

<img src="imgs/aicmigr-29-autotest-06-process-recap/2faf2b2d51edfd84425f3204ae659371_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

下表把六个阶段拆成可裁剪的速查表。熟练工程师上手新项目时可以直接拿这张表逐项核对，不需要回到正文。

| 阶段 | 必做硬性条目 | 必停决策点 | 必 review 的产出 |
|------|--------------|-----------|-----------------|
| 第一次翻译 | 设计文档六块全有（为什么／做什么／不做什么／思路／场景／产出） | 待拍板选择（≥3 个，每个带选项和影响） | 协议兼容性归因表是否写进文档 |
| 路径反问 | 拿出开源项目认知对比自研路径 | 路径选择 | 工程量对比是否清晰 |
| 第二次翻译 | 架构、Skill 设计、Tool 实现要点、场景库、触发、报告系统、决策记录 | 第 7 节决策记录 review | Skill 边界、故障注入选型、GitHub 凭据 |
| 代码层 | 接口签名 + 硬约束 + 已有依赖 | Tool 跑不通时停下来报错 | 每个 Tool 单点验证（`hermes "调用 X"`） |
| 业务层 | 第一个场景文件 + 模板 + config + Deploy Key + cron | 场景库接下来要写哪些 | 故障参数与 chaos.py 接口对齐、通过标准字段与 client.py 返回 JSON 对齐 |
| 跑通验证 | `/skills` 和 `/tools` 加载、P0 对话跑通、报告 push | 报告未 push 成功时人工介入 | GitHub 仓库能看到本次 run 的报告 |

### 3.1 阶段时间预算

| 阶段 | 时间预算 |
|------|---------|
| 场景一（第一次翻译） | 20-30 分钟 |
| 场景二（Hermes 验证） | 20-30 分钟 |
| 场景三（代码层） | 1.5-2 小时 |
| 场景四（业务层） | 1 小时 |
| 一键流程（全跑通） | 4-5 小时（含几次 review） |

## 4. 实战演示总览

<img src="imgs/aicmigr-29-autotest-06-process-recap/11bf2fa1a508f7f13904bcbae9cf6b2d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第二部分把第一部分的方法论落到具体场景里。每个场景对应系列中的一篇，产出明确、review 点清晰。

### 4.1 四个场景的全局对照

| 场景 | 对应系列篇目 | 核心产出 | 主要方法论 |
|------|-------------|---------|-----------|
| 场景一 第一次翻译 | 系列第 25 篇 | `docs/design.md` | 展开-反问-整合三步法 |
| 场景二 Hermes 验证 | 系列第 26 篇 | Hello World Skill | 动手命令、验证加载机制 |
| 场景三 代码层 | 系列第 27 篇 | 5 个 Tool + Skill 骨架 | 提示词三件事、Skill 重喂接口 |
| 场景四 业务层 | 系列第 28 篇 | 场景库 + 报告 + 配置 + 调度 | 结构化用代码生成、P0/P1/P2 分级 |

### 4.2 实战前的准备

系列第 24-28 篇的准备工作都已经做完：第 26 篇把 Hermes 装好了、Claude Code 跑起来；第 24 篇建立了对 Hermes 的顶层认知；第 25 篇完成了方案文档（本篇会重新跑一份）。

如果工程师跳过了前面直接看本篇，先回到系列第 26 篇跑一遍 Hello World，确保 Hermes 装好且能扩展。否则后面所有提示词的"基于 Hermes"都没有素材。

```bash
cd hermes
```

## 5. 场景一 第一次翻译：从一句话到完整设计文档

<img src="imgs/aicmigr-29-autotest-06-process-recap/1710f0eef13b1c990aed16631628f407_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应系列第 25 篇，产出 `docs/design.md`（完整设计文档，与具体技术栈无关）。这个场景完整演示了第 2.1 节的"展开-反问-整合"三步法。

### 5.1 第一步：展开一句话需求

#### (1) 提示词原文

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

#### (2) review 重点

- 设计文档六块都有内容（为什么／做什么／不做什么／思路／场景／产出）
- AI 列出来的"待拍板的选择"至少 3 个
- 每个选择带选项和影响，不是空泛的"由你决定"

### 5.2 第二步：反问补漏

跑完第一步，AI 给的设计文档总有几处空洞，把空洞挑出来反问。

#### (1) 提示词原文

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

#### (2) review 重点

反问是"你这里漏了，补上"，不是追问。AI 不会主动告诉工程师它漏了什么，工程师需要自己有一份"完整设计文档该长什么样"的预期，拿这份预期去比对 AI 给的版本，差什么就反问什么。预期从哪来？从工作经验里来。

### 5.3 第三步：整合成正式 PRD

#### (1) 提示词原文

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

#### (2) review 重点

协议兼容性归因表（不同 SDK 不一致 ／ 特定版本失败 ／ 全部失败 → 三种归因）有没有写进文档。这张表系列第 28 篇的报告模板会反复用。

### 5.4 场景一收尾

跑完场景一，工程师手上有完整设计文档。这份文档跟具体技术栈无关。耗时 20-30 分钟。

## 6. 场景二 跑通 Hermes 验证机制

<img src="imgs/aicmigr-29-autotest-06-process-recap/edbcee8893be7a31d836fc64fb8ce5df_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应系列第 26 篇。这一步是动手命令，不是 AI 提示词。目标是验证 Hermes 的加载机制真的活着，后面正式项目才能照同样的机制写。

### 6.1 安装 Hermes 并感受手感

#### (1) 安装命令

```bash
# 安装 Hermes
curl -fsSL https://res1.hermesagent.org.cn/install.sh | bash

# 跟它聊一聊,感受手感
hermes
```

#### (2) 对话示例

```
> 你能帮我做什么?
> 帮我看看磁盘空间占用,列出最大的 5 个目录
```

### 6.2 写最小 Skill 验证机制

在 `~/.hermes/skills/hello-world/` 下新建 `SKILL.md`，写一个返回当前时间的 Skill。跑通就行，这一步只是验证 Hermes 真的能扩展，不是本篇的重点。

### 6.3 review 重点与收尾

`hermes /skills` 能看到 hello-world 出现在列表里。能看到说明 Hermes 加载机制活着，后面正式项目就照同样的机制写。

跑完场景二，Hermes 验证完毕。耗时 20-30 分钟。

## 7. 场景三 让代码长出来：5 个 Tool + Skill 骨架

对应系列第 27 篇。产出 `~/.hermes/skills/robustmq-chaos-test/` 下的 5 个 Tool 和 1 个 `SKILL.md`。

### 7.1 依赖顺序与共同原则

<img src="imgs/aicmigr-29-autotest-06-process-recap/6a62aae5a89b352fa67a5b7767396a6b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

按系列第 25 篇方案的依赖顺序：cluster → observability → client → chaos → report → SKILL。

每个 Tool 的提示词关键设计都是三件事：**接口签名、硬约束、已有依赖**（详见第 2.3 节）。下面每个 Tool 的提示词原文都会重传这三件事。

### 7.2 cluster.py：起停测试集群

#### (1) 职责

混沌测试 Skill 套件的第一个 Tool，负责在本地起停 RobustMQ 测试集群。

#### (2) 提示词原文（提示词 4）

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

#### (3) 三件事体现

- 接口签名：`action 支持 start / stop / status`、`handler 函数签名 (args: dict, **_) -> str`
- 硬约束：不引入 Docker、`ROBUSTMQ_HOME` 没默认值是 fail-fast、失败返回 error 不抛异常
- 已有依赖：`tempfile.mkdtemp`、`http://127.0.0.1:1883/health`、`ROBUSTMQ_HOME` 环境变量

#### (4) review 重点

不引入 Docker、`ROBUSTMQ_HOME` 没默认值是 fail-fast、临时目录 stop 时清理、失败返回 error 不抛异常。这四条任何一条没做到，回提示词补一句重跑。

### 7.3 observability.py：收集观测数据

#### (1) 职责

从 RobustMQ 集群收集观测数据，后面 chaos 故障注入和 client SDK 测试都会调它打快照。

#### (2) 提示词原文（提示词 5）

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

#### (3) review 重点

snapshot 必须带时间戳、要能用于"故障前后对比"——这是后面判断自愈是否成功的依据。

### 7.4 client.py：调度多语言 SDK

<img src="imgs/aicmigr-29-autotest-06-process-recap/e62e32989fe74a04ddd5cc32544c7211_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 职责

调度多语言 SDK 跑测试。按 `solution.md` 决策不用 Docker 隔离，用本地版本管理工具切换。

#### (2) 提示词原文（提示词 6）

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

#### (3) 三件事体现

- 接口签名：`action 是 run`、参数清单、约定 JSON 字段
- 硬约束：JSON 解析失败单独记 `script_format_error`、并发逻辑放 Python 不用 delegate
- 已有依赖：`pyenv / gvm / rustup / sdkman / nvm`、`CLUSTER_ENDPOINT` 环境变量

#### (4) review 重点

`script_format_error` 不能跟测试失败混算；并发用 Python `ThreadPoolExecutor` 不用 Hermes delegate。

### 7.5 chaos.py：故障注入与恢复

#### (1) 职责

故障注入和恢复。按 `solution.md` 决策 Chaosd 主 + tc/kill 补。

#### (2) 提示词原文（提示词 7）

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

#### (3) 三件事体现

- 接口签名：`action 支持 inject / recover`、返回字段
- 硬约束：第一版只实现两种故障、其他返回 `not_implemented`、Chaosd 端点不 hardcode
- 已有依赖：Chaosd HTTP API、`config.yml`

### 7.6 report.py：生成报告并提交 GitHub

#### (1) 职责

整合整轮测试结果，生成报告并提交到 GitHub。

#### (2) 提示词原文（提示词 8）

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

#### (3) 三件事体现

- 接口签名：`action 是 generate_and_push`、返回字段、`run_passed` 判定逻辑
- 硬约束：JSON 用 `json.dumps` 不调 LLM、Deploy Key 通过 `GIT_SSH_COMMAND` 指定、清理 30 天前临时文件
- 已有依赖：Jinja2 模板、`config.yml`、`~/.ssh/test-reports-deploy`

#### (4) review 重点

`run_passed` 字段的判定逻辑（核心场景全过 + 非核心通过率 ≥ 75%）不能丢，这是 cron 跑完后看报告的第一眼指标。

### 7.7 SKILL.md 骨架：串起 5 个 Tool

#### (1) 职责

5 个 Tool 写好后，Skill 的任务是告诉 Agent 在 cron 触发或手动触发时，按什么顺序调这些 Tool。

#### (2) 提示词原文（提示词 9）

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

#### (3) review 重点（Skill 的灵魂）

Tool 接口完整喂给 AI。如果不把 Tool 接口贴给 AI，AI 会编出根本不存在的 Tool 调用，Skill 加载之后跑就报错。

这一步的代码没有写到本篇里，Claude Code 跑出来的真实代码都放在 GitHub 仓库里。读到哪个 Tool 想看代码，直接去仓库对应文件看。

### 7.8 单点验证与场景三收尾

每个 Tool 写完后跑一次单点验证：

```
$ hermes
> 调用 cluster 起一个测试集群
```

Agent 真的去调了、返回结果回来，这个 Tool 就活着。某个 Tool 调不通也别慌，九成是 description 没写清楚或 parameters schema 不合法，回提示词改两句重跑就行。

跑完场景三，代码层就齐了。耗时 1.5-2 小时。

## 8. 场景四 填业务层

对应系列第 28 篇。产出场景库、报告模板、`config.yml`、Deploy Key + GitHub 仓库、`cron.yml`。

### 8.1 第一个场景库文件

<img src="imgs/aicmigr-29-autotest-06-process-recap/02ff6631f50d9c75c0212859c56cdb1f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 产出说明

在 `~/.hermes/skills/robustmq-chaos-test/scenarios/mqtt/` 下写第一个场景 `p0-broker-kill-leader.md`。格式是 Markdown，自然语言描述，Agent 读完知道按 SKILL.md 的"单场景五步"怎么调 Tool。

#### (2) 提示词原文（提示词 10）

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

#### (3) review 重点

- 故障类型和参数跟 `chaos.py` 接口对得上
- 通过标准的字段名（`exit_code` ／ `lost` ／ `p99_ms`）跟 `client.py` 返回的 JSON 对得上

第一个场景写完，套同样模板写其他场景。

### 8.2 报告 Jinja2 模板

#### (1) 产出说明

写 `~/.hermes/skills/robustmq-chaos-test/templates/report.md.j2`，Jinja2 模板。模板负责把结构固定下来，数据从 `report.py` 传进来，模板里不调 LLM。

#### (2) 提示词原文（提示词 11）

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

#### (3) review 重点

底部那段"协议兼容性归因"是本篇设计文档对应到报告的真实落点。如果三种归因都不命中，模板要标"待人工分析"——AI 在边界 case 上容易偷懒，这里要手动收一下。

### 8.3 套件配置 config.yml

#### (1) 产出说明

直接抄（按 `solution.md` 决策填值）：

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

#### (2) review 重点

P0 档刻意只放 2 个 SDK——P0 是基础保障线，跑得越快越好，大矩阵留给 P1／P2。

### 8.4 GitHub Deploy Key + test-reports 仓库

<img src="imgs/aicmigr-29-autotest-06-process-recap/e8c4d7c929e34fae71e5c8a5994037b3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 产出说明

这一步是动手命令，不是 AI 提示词。三步搞定。

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

#### (2) review 重点

勾 Allow write access 不能漏（默认是只读）。第三步 push 成功这一刻 Deploy Key 链路就活了。

### 8.5 cron.yml 三档调度

#### (1) 产出说明

直接抄：

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

#### (2) review 重点

P0 MQTT 和 mq9 错开 30 分钟避免资源冲突。频率配置直接来自 `design.md` 的 P0／P1／P2 分级。

### 8.6 场景四收尾

跑完场景四，业务层就位。耗时 1 小时。

## 9. 一键跑完整流程：让 Claude Code 自主执行

<img src="imgs/aicmigr-29-autotest-06-process-recap/b9f945df6c9be07e974f6e66eea54cb8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面四个场景一个个跑，是为了让工程师看清每一步的产出和 review 点。真正上手之后，工程师会希望一次粘贴、Claude Code 自主跑完整流程，关键决策点停下来等判断。

### 9.1 一键流程的设计意图与范围

#### (1) 设计意图

把整套流程压成一段提示词，整段粘贴到 Claude Code，关键决策点会停下来等工程师输入。

#### (2) 范围说明

一键流程不管装 Hermes 和点 GitHub UI（这两件需要人动手）。它从"已经摸过 Hermes、Hermes 已装好"这一步开始，跑到"5 个 Tool ＋ Skill ＋ 业务层全部就位"结束，最后停下来等工程师跑验证。

### 9.2 一键流程提示词原文

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

粘贴完等 Claude Code 跑。整个流程 4-5 小时（含几次 review）。工程师不在的时间它在跑，工程师回来的时间它停在那里等判断。

### 9.3 这段提示词的设计动机

#### (1) 第零步"路径反问"摆在第零位强制等待

这是这套工作流相比系列第 21 篇护栏的最关键差异。系列第 21 篇是"先点产品看现状"，本篇是"第一次翻译跑完先反问路径"。两个第零步都是一类硬约束，都来自一次真实翻车留下的工程教训。

#### (2) 关键决策点显式让 AI 停下来

第一次翻译的关键选择（场景分级、报告公开度、SDK 矩阵）、路径选择（自研 vs Hermes）、第 7 节决策点 review、业务层场景库决策，这四个决策点 AI 不能替工程师拍。

#### (3) 所有硬约束都明确写进提示词

接口签名 ＋ 硬约束 ＋ 已有依赖、Tool 提示词重传约束、Skill 提示词重喂接口、结构化的东西用代码生成不交给 LLM。这些约束散落在系列第 24-28 篇，一键流程里要全部明确写出来。

#### (4) summary.md 集中暴露 review 点

AI 不能完全替工程师思考，但可以把"我不确定的地方"集中到 summary 让工程师重点看。

## 10. 小结与思考

<img src="imgs/aicmigr-29-autotest-06-process-recap/810fccca6e8180896852677dc5aad09a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 10.1 第六部分小结

第六部分到这里结束。从系列第 24 篇摸 Hermes、第 25 篇两次翻译、第 26 篇 Hello World、第 27 篇让代码长出来、第 28 篇填业务层让系统活起来，整套"基于开源项目二次开发"的方法论全部跑完。

### 10.2 最值钱的教训：第二次翻译前先反问"路径选对了吗"

#### (1) 教训为什么值钱

AI 顺着工程师的提问给的方案不一定是最优的，工程师刚摸过的那个开源项目，可能已经把方案里 80% 的工程都做了，不用自己撸。

#### (2) 教训的产物

这条教训已经写进系列第 25 篇方案文档，下次类似工作流 AI 会主动提醒工程师做反问。本篇的提示词清单和一键工作流里"第零步反问"也是这条教训的产物。

### 10.3 工程师在 AI 时代能交付的最值钱的东西

整门系列到这里也接近尾声。一个工程师在 AI 时代能交付的最值钱的东西，不是写代码的能力，是把方向定准、把约束讲清楚、把模糊变具体的能力。

AI 给工程师一个 80 分的初稿，工程师要做的不是改成 95 分，是先判断初稿是不是建立在对的方向上。方向不对，改 100 遍也是错的。

### 10.4 思考

#### (1) 时间消耗自评

跑完整套流程大约花了多少时间？

#### (2) 最卡的是哪一步

最卡工程师的是哪一步：

- 摸 Hermes
- 第一次翻译
- 第二次翻译反问
- 代码层 Tool 编写
- 业务层场景库
- 跑通验证

把答案记下来，下一次跑类似工作流时优先复盘这一步。
