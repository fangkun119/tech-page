---
title: 传统项目迁AI 27：自动测试 - 编写Skill套件
author: fangkun119
date: 2026-07-05 07:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-27-autotest-04-write-skill-suite/cover.jpg
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
aicmigr-27-autotest-04-write-skill-suite
传统项目迁AI 27：自动测试 - 编写Skill套件
-->

## 1. 全文导读地图

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/f0b11c8608ed1b6d95949d09d5dd3af1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是"自动测试"系列的第四篇，承接上一篇"跑通 Hermes Agent"的进度。上一篇装好了 Hermes、跑通了 Hello World，还把第 25 篇方案里的每个文件在 Hermes 用户目录里找到了位置。本篇把方案文档里的代码长出来——用提示词驱动 Claude Code 写完 robustmq-chaos-test Skill 套件（5 个 Python Tool + 1 个 SKILL.md）。

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/f3ec2ef067f765ab1d546cbd51a2a8dd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    A[两类读者] --\> B[初学 AI 编程工程师<br/>想系统掌握 AI 编程方法论]
    A --\> C[熟练 AI 编程工程师<br/>想快速回顾方法论、查 Check List]

    B --\> D[第一部分：方法论提炼<br/>第 2-4 章]
    B --\> E[第二部分：实战演示<br/>第 5-9 章]
    C --\> F[第 2 章 开发提示词三件套<br/>核心方法论]
    C --\> G[第 4.4 节 开发 Check List<br/>项目阶段速查]
    C --\> H[第 9 章 整体点评<br/>工程师把关清单]

    D --\> D1[2. 开发提示词三件套]
    D --\> D2[3. 开发顺序：依赖在先、Tool 在先]
    D --\> D3[4. 工程师把关的边际价值]

    E --\> E1[5. 实战背景：系统跑起来长这样]
    E --\> E2[6. 实战任务：从链路反推开发清单]
    E --\> E3[7. 一气呵成：5 个 Tool + 1 个 SKILL.md]
    E --\> E4[8. 逐个点评：AI 写出来的代码到底怎么样]
    E --\> E5[9. 整体看一眼：从单点验证到工程可用]
-->

第一部分讲方法论（开发提示词三件套、开发顺序、工程师把关），不深入具体技术栈，可裁剪做项目 Check List；第二部分把方法论在 robustmq-chaos-test Skill 套件的真实开发中复现一遍，逐个 Tool 贴出提示词原文、再点评 Claude Code 写出来的代码，让方法论"不仅知其然、也知其所以然"。

## 2. 开发提示词三件套

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/99bbc6b7afcc0923f14fb976b5492b85_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

写代码是从方案文档到可运行代码的第三次翻译（前两次是方法拆解、方案落地）。AI 时代，这一步从体力劳动变成质量把关：工程师的角色不再是敲键盘，而是写好提示词。好的开发提示词包含三件事——接口签名、硬约束、已有依赖。

### 2.1 接口签名

接口签名告诉 AI：这个模块支持哪些动作（action）、输入参数长什么样、返回什么结构的 JSON、handler 怎么注册。这部分越具体，AI 给出的代码越能直接对接上下游模块，越少需要二次缝合。

为什么这一件最重要？因为接口是模块与外界的契约。接口一旦定死，AI 实现时只能在契约之内填代码；接口模糊，AI 就要"猜"上下游怎么调它，猜出来的代码常常跟真实调用方对不上。先把 action 取值、参数字典、返回 JSON 字段逐条列出，比写一段功能描述要值钱得多。

### 2.2 硬约束

硬约束是哪些事绝对不能做的负面清单：不能引入 Docker、不能给环境变量配默认值、失败时不能抛异常、临时目录必须清理。这些约束通常来自前几轮的方法论沉淀（本系列里 24/25 篇跑出来的判断），它们是工程师踩过坑后的判断，AI 不知道、也不会主动想到。

为什么硬约束比功能描述更有效？因为 AI 的默认实现倾向"通用、保守、不出错"——它会选最常见、最教科书写法。但生产场景常常需要反默认：fail-fast 比偷偷用默认值强一万倍、Agent 循环见到异常会中断所以必须返回 error。这些反默认的判断，必须靠硬约束显式压给 AI。

### 2.3 已有依赖

已有依赖是这个模块运行时会用到的具体配置：端口分配规则、健康检查 URL、二进制路径环境变量、外部端点地址。这部分让 AI 不用猜——直接照着真实环境写，跑起来就能用。

为什么已有依赖要单独拎出来？因为它跟"功能逻辑"是两件事。AI 写 spawn 进程的代码不需要知道端口是 1883 还是 21883；但端口分配规则不是 AI 能猜的，必须工程师告诉它。把"实现怎么写"留给 AI（这是它的强项），把"环境长什么样"明明白白写进提示词（这是工程师的强项），分工才清晰。

### 2.4 提示词写法的关键：预设加余地

#### (1) 预设你已经想到的

关键约束、不能踩的坑、上下游契约——这些预设进提示词，让 AI 在你画好的框里写。

#### (2) 把余地留给 AI 去补

怎么实现 spawn、怎么处理超时、edge case 怎么写——这些留给 AI 发挥，不要把提示词写成二十条编号清单。提示词几段话讲清楚，比模板化的二十条清单靠谱。

## 3. 开发顺序：依赖在先、Tool 在先

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/f22eccd9232c9498f82c743c59cf2c4d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

写多个 Tool + 一个 Skill 时，顺序遵循两条原则：依赖在先、被依赖在后；Tool 在先、Skill 在后。

### 3.1 第一条原则：依赖在先、被依赖在后

按依赖关系排开发顺序，每一个新 Tool 都能直接调前面已经写好的 Tool，不会出现"写到一半发现下游接口还没定"的卡顿。

以 robustmq-chaos-test 套件为例，五个 Tool 的依赖顺序是：

| 顺序 | Tool | 依赖关系 |
|------|------|----------|
| 1 | cluster.py | 起停测试集群。所有其他 Tool 的动作都假设集群在跑 |
| 2 | observability.py | 从集群进程拿日志和 metrics。依赖集群活着，跟其他 Tool 没强依赖 |
| 3 | client.py | 连集群跑 SDK 测试。依赖 cluster.py 给的 endpoint |
| 4 | chaos.py | 在集群上注入故障。依赖 cluster.py；故障期间要跟 observability.py 和 client.py 配合 |
| 5 | report.py | 整合前四个 Tool 的产出。依赖前面四个的全部结果 |

### 3.2 第二条原则：Tool 在先、Skill 在后

Skill 最后写。这一步反直觉——很多人觉得 Skill 是"大脑"，应该最先写。但 Skill 是 Tool 的使用说明书，说明书要先有被说明的东西才能写。Tool 接口都定下来之后，Skill 才知道按什么顺序调谁、传什么参数。

为什么这条反直觉但正确？因为 Skill 的本质是工作流编排，编排的对象是 Tool。对象不存在，编排就是空中楼阁——AI 会编出根本不存在的 Tool 调用，Skill 加载之后一跑就报错。先写 Tool 再写 Skill，等价于"先有产品再写说明书"，符合人类工程师做事的真实顺序。

### 3.3 代码层与业务层分离

代码层（Tool 和 Skill 的 Python 代码）跟业务层（具体测哪些场景、SDK 版本矩阵、报告字段、Cron 频率）要分开做。先把代码层做完，再填业务层，工程上更稳。

为什么这么切？代码层是通用 Hermes 编程，跟"测什么场景、SDK 用什么版本、报告什么字段"无关——cluster.py 怎么 spawn 进程、chaos.py 怎么调 Chaosd HTTP API，这些是 Hermes Tool 的通用写法，换个被测系统也能套。业务层是混沌测试这个具体业务的定义，先把通用层做完、稳定下来，再填业务层，业务变更不会动摇代码层。

## 4. 工程师把关的边际价值

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/363768b2f1adfd6c40556689f6772e85_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

跑通单点验证不等于写得好。真实工程师拿到 AI 给的代码，第一件事是退一步整体看一遍，评判每一段是好是坏、该改的赶紧改。AI 时代的开发，工程师的边际价值不在写代码本身，而在两件 AI 做不好的事情上。

### 4.1 把关不是重写

工程师的把关不是把 AI 写的代码推翻重写，是用最小改动堵住两类漏洞。重写既不经济也不必要——AI 执行了提示词里所有明确说过的约束，没有幻觉出不存在的接口，边界情况也都有降级处理，这是好提示词的直接结果。把关要做的是 AI 看不见的那部分。

### 4.2 漏洞一：跨文件的语义一致性

AI 在写每个文件时看不见全局，只有人通读一遍才能发现跨文件的语义裂缝。

#### (1) 典型场景：recover 语义与等待逻辑的错位

例如 chaos.py 里 Chaosd 的 `recover` 接口是撤销攻击配置、不是重启进程；但 SKILL.md 里"等故障恢复"的逻辑如果没说清"是等 Chaosd recover 成功还是等 broker 进程重新健康"，Agent 调完 recover 立刻跑 client 验证，会把"broker 还没来得及重启"误判为测试失败。

#### (2) AI 看不见全局

AI 写 chaos.py 时只盯着这一个文件，写 SKILL.md 时也只盯着这一个文件。两边各自自洽，合在一起有裂缝——只有工程师通读全套代码才能发现。

### 4.3 漏洞二：运营时的时间函数

AI 写的是"当前能跑"，人要补的是"跑半年后还能跑"。

#### (1) 典型场景：硬编码的超时和无限增长的副本

例如 cluster.py 写死 `time.sleep(5)` 等健康检查——慢机器 8 秒才 ready、快机器 2 秒就好，写死 5 秒在两种机器上都有问题。又如 report.py 每次都重新 git clone 一个浅副本，仓库积累几千个 report 之后，克隆会越来越慢，系统跑 7×24 半年后这里会成为明显瓶颈。

#### (2) 改法：轮询加可配超时、维护持久化副本

健康检查改成轮询（每 1 秒探一次、最多等 30 秒、超时可配）；report.py 维护一个持久化的本地 clone，每次 push 前 `git pull` 而不是重新 clone。这些不是 blocker，但记一条 TODO 比以后定位慢 push 要省时间。

### 4.4 开发 Check List

把方法论压成一份可裁剪的 Check List，供项目阶段快速查阅。

#### (1) 写提示词前

| 项 | 检查内容 |
|---|---|
| 接口签名 | action 取值、参数字典、返回 JSON 字段是否逐条列出？ |
| 硬约束 | 哪些事绝对不能做（负面清单）是否写明？ |
| 已有依赖 | 端口、URL、路径、端点配置是否写实？ |
| 预设与余地 | 关键约束预设进提示词；实现细节留给 AI |

#### (2) 排开发顺序时

| 项 | 检查内容 |
|---|---|
| 依赖在先 | 后写的 Tool 是否都能直接调前面已写好的 Tool？ |
| Tool 在先 | Skill 是否在所有 Tool 接口稳定后再写？ |
| 代码层 vs 业务层 | 通用 Tool 代码是否与具体业务配置分离？ |

#### (3) 把关 AI 代码时

| 项 | 检查内容 |
|---|---|
| 跨文件语义一致性 | 通读全套代码，找跨文件的语义裂缝（如 recover 语义与等待逻辑） |
| 运营时时间函数 | 硬编码的超时、无限增长的副本、写死的路径——这些跑半年后还会不会出问题？ |
| 单点验证 | 每个 Tool 在交互模式下调一次，确认返回结果正常 |
| Skill 加载 | Skill 是否能被 Hermes 识别（出现在 `/skills` 列表） |

## 5. 实战背景：系统跑起来长这样

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/e2bf19b2406cff01a7e3737de01b53a2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

从本章开始进入第二部分实战演示。先看本篇要写的代码全部跑起来之后，整套系统是什么样子——脑子里有这幅图，代码才不只是孤立的文件，写到一半才不会忘了为什么要写这个。

### 5.1 一次完整混沌测试的九个步骤

动手写代码之前，先把这套系统真的跑起来时是什么样想清楚。

一次完整的混沌测试有 9 个步骤：

```text
1. Cron 在 P0/P1/P2 三档某一档的时间点触发,触发动作是给 AI Agent
   发一段自然语言:"按 P0 跑一轮 MQTT 基础场景"。
2. Agent 收到后加载 robustmq-chaos-test Skill,按 Skill 的 Procedure
   一步步调 Tool。
3. 先调 cluster.py 起一个测试集群,等 RobustMQ 进程健康。
4. 集群跑起来了就调 chaos.py 调 Chaosd 注入故障。
5. 注入到位之后调 client.py,在故障期间用预装的多语言版本管理工具
   切换到目标 SDK 版本跑一轮 basic-pubsub 测试,这段只记录不判断。
6. 等故障窗口结束 chaos.py 自动恢复,再调 client.py 验证自愈。
7. 整轮过程中 observability.py 收集 RobustMQ 进程的日志和 metrics。
8. 所有数据交给 report.py,程序化生成 JSON ＋ Markdown 双格式报告,
   用 Deploy Key git push 到 GitHub 公开仓库。
9. Quality Dashboard 是静态页面,从 GitHub 仓库读取,展示这轮的结果。
```

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/be3d74eed61956c938fcce1f621d75b3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/27_自动测试_04：编写Skill套件/be3d74eed61956c938fcce1f621d75b3_MD5.jpg
用途：展示一次完整混沌测试的 9 步链路图
内容：从 Cron 触发开始，到 AI Agent 加载 robustmq-chaos-test Skill，依次调用 cluster.py（起测试集群）、chaos.py（注入故障）、client.py（故障期间切换 SDK 版本跑测试，只记录不判断）、chaos.py 自动恢复、client.py 自愈验证、observability.py（收集日志和 metrics）、report.py（生成 JSON+Markdown 报告并 git push），最终 Quality Dashboard 从 GitHub 仓库读取并展示结果
-->

### 5.2 链路分层

链路里有触发源、有指挥层（AI Agent ＋ Skill）、有执行层（5 个 Tool）、有数据层（集群 ＋ Chaosd ＋ GitHub 仓库），整条链路最后归档到一个公开可读的 Dashboard。每一段都有人负责，本篇的开发任务，就是把"有人负责"这件事一件一件落到具体文件上。

## 6. 实战任务：从链路反推开发清单

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/51697a7a35a0fae25d9c751c682d0c33_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 6.1 代码层：两类东西要写

按链路上每个环节反推开发任务，清单是这样：

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/78843674d49e32b34721d0f71f2a4bdb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/27_自动测试_04：编写Skill套件/78843674d49e32b34721d0f71f2a4bdb_MD5.jpg
用途：展示"为了让系统跑起来要做几件事"的开发任务清单
内容：从混沌测试链路反推出的代码层任务清单：需要编写 5 个 Python Tool（cluster.py / observability.py / client.py / chaos.py / report.py）与 1 个 SKILL.md；Cron 触发、Agent 循环、Skill 加载机制由 Hermes 自带无需开发；业务层（场景库、Jinja2 模板、config.yml、Deploy Key、cron.yml）留到下一讲
-->

总共两类东西要写：5 个 Python Tool + 1 个 SKILL.md。其他链路环节（Cron 触发、Agent 循环、Skill 加载机制）Hermes 全部自带，本篇不用碰。

### 6.2 业务层留给下一讲

但这两类还不够让系统真的跑起来。还有一些业务层的东西没列：具体测哪些场景（场景库）、报告长什么样（Jinja2 模板）、SDK 矩阵和 Chaosd 端点参数（config.yml）、GitHub Deploy Key 和仓库怎么配、Cron 三档怎么填。本篇只做代码层，业务层留给下一篇。

### 6.3 为什么先做代码层

代码层是通用 Hermes 编程，跟"测什么场景、SDK 用什么版本、报告什么字段"无关。cluster.py 怎么 spawn 进程、chaos.py 怎么调 Chaosd HTTP API，这些是 Hermes Tool 的通用写法，换个被测系统也能套。业务层是混沌测试这个具体业务的定义：测哪些场景、用什么 SDK 版本矩阵、报告归档到哪、什么频率跑。先把通用层做完，再填业务层，工程上更稳——这正是第 3.3 节方法论在实战中的落地。

## 7. 一气呵成：5 个 Tool + 1 个 SKILL.md

按第 3 章的开发顺序，5 个 Tool + 1 个 Skill 一气呵成写完。文章里只贴提示词，Claude Code 跑出来的真实代码放在 GitHub 仓库里，读者读到哪个 Tool 想看代码，直接去仓库对应文件看就行。文章不贴代码是为了让叙事干净，把篇幅留给提示词和点评这两件真正值得读的事。

仓库地址：https://github.com/robustmq/robustmq

### 7.1 cluster.py：第一个 Tool 的完整提示词

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/b28816d56ce6dc1bc484d8514206d619_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第一个 Tool 把完整提示词展开，后面四个就不重复这个模板了。

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/cluster.py。

这是混沌测试 Skill 套件的第一个 Tool,负责在本地起停 RobustMQ 测试集群。
按 25 讲方案的决策:不引入 Docker,直接 spawn 多个 RobustMQ 进程。

action 支持 start / stop / status 三种。

start: 用 subprocess 拉起 3 个 RobustMQ broker 进程,每个用独立端口
(broker-1: 1883,broker-2: 2883,broker-3: 3883)和独立数据目录。
数据目录用 tempfile.mkdtemp 创建,记下来 stop 时清理。
启动后等 5 秒,curl http://127.0.0.1:1883/health 确认健康,失败立即
kill 全部并返回 {"status": "failed"}。
成功返回 {"status": "running", "endpoint": "127.0.0.1:1883",
"data_dirs": [...]}。

stop: kill 所有 broker 进程,清理 data_dirs。

status: 返回当前运行的进程数和 endpoint。

RobustMQ 二进制路径通过环境变量 ROBUSTMQ_HOME 取,没设置就 fail-fast
返回 error,别用默认值兜底,默认值会让用户误以为路径配对了,
跑起来才发现不存在。

handler 函数签名 (args: dict, **_) -> str,失败返回
{"error": "..."} 别抛异常,Agent 循环见到异常会中断。
```

#### (1) 三件套如何落进提示词

提示词的关键设计包含三件事——接口签名、硬约束、已有依赖，正是第 2 章方法论在实战中的落地。

#### (2) 接口签名

action 支持哪几种、返回什么 JSON、handler 怎么注册——这部分越具体，AI 给的代码越能直接对接其他 Tool。

#### (3) 硬约束

第 25 篇两次翻译跑出来的判断，落到这个 Tool 上有这些：不引入 Docker（博客 95 决策）、ROBUSTMQ_HOME 不给默认值（fail-fast 比偷偷用默认强一万倍）、失败返回 error 不抛异常（Agent 循环约束）、临时目录 stop 时清理（资源泄漏防线）。

#### (4) 已有依赖

端口分配规则（broker-1/2/3 各自端口）、健康检查 URL、二进制路径环境变量。AI 不用猜，直接照着写。

### 7.2 observability.py

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/observability.py。

负责从 RobustMQ 集群收集观测数据,后面 chaos 故障注入和 client SDK
测试都会调它打快照。

action 支持 collect_logs / collect_metrics / snapshot 三种。

collect_logs: 从 cluster.py 起的 broker 进程的 log 文件抓最近 N 行
(默认 100),按节点返回 dict[node_name, list[str]]。log 路径在
data_dirs/<node>/logs/ 下。

collect_metrics: 调 RobustMQ 内置的 /metrics 端点(每节点的 HTTP 端口),
拉 Prometheus 格式数据,返回关键指标(connections / messages_in /
messages_out / errors)。

snapshot: 一次性收 logs + metrics,加时间戳,用于故障注入前后对比。
```

这个 Tool 第一期不复杂，就三件事：读日志文件、调 metrics 端点、snapshot 打快照。这是第 25 篇方案里"协议兼容性归因"的数据基础：故障期间和故障后各打一次 snapshot，放进报告里给 dashboard 看，问题定位才有依据。

### 7.3 client.py

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/client.py。

负责调度多语言 SDK 跑测试。按 25 讲方案的决策:不用 Docker 隔离,
用本地版本管理工具切换。服务器上预装了 pyenv / gvm / rustup /
sdkman / nvm。

action 是 run,参数 sdk(python/go/rust/java)、version(具体版本)、
scenario(basic-pubsub / failover / latency 等)、cluster_endpoint。

内部:
1. 用版本管理工具切换到指定版本(如 pyenv shell 3.11)
2. 进入 ~/.hermes/skills/robustmq-chaos-test/sdk_clients/<sdk>/ 目录
   跑对应 scenario 的入口脚本
3. 入口脚本通过 CLUSTER_ENDPOINT 环境变量接收集群地址,stdout 最后
   一行必须输出约定 JSON({sent, received, lost, p99_ms, errors})
4. 解析最后一行 JSON,exit code 表示通过失败

注意:解析最后一行 JSON 失败时,单独记 status=script_format_error,
别跟测试失败混在一起算,脚本格式错跟测试失败混一起,会让你查问题
查到怀疑人生。

不传 sdk 时,用 ThreadPoolExecutor 并发跑全部语言。并发逻辑放 Python 里
就行,别用 Hermes 的 delegate 机制,Tool 内部并发更可控。
```

版本管理切换、stdout 协议、format error 分级这几条，都是工程师真实跑过混沌测试才会想到的细节。少一条，系统跑半年准翻车。

### 7.4 chaos.py

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/87d4f08b7a3cdfeafa3a5bc97e80133d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/chaos.py。

负责故障注入和恢复。按 25 讲决策:Chaosd 主 + tc/kill 补。
判断标准是"故障是否需要被精确测量和回放",需要就用 Chaosd
(进程级 kill 信号、网络精确延迟分布、磁盘 I/O 限速、时钟穿越),
只是验证可达性就用 tc/kill 系统命令。

action 支持 inject / recover。

inject: 接收 fault_type(broker-kill / network-delay /
network-partition / disk-fill 等)、target(集群里的节点名)、
duration_seconds 和具体参数,调 Chaosd HTTP API 注入。
返回 {"fault_id": "...", "status": "active"}。

recover: 按 fault_id 撤销故障。

第一版只实现 broker-kill 和 network-delay 两种,其他先返回
not_implemented,后面迭代加。

Chaosd 端点配置在 ~/.hermes/skills/robustmq-chaos-test/config.yml 里
读,don't hardcode。
```

让 AI 别把所有故障类型一次写完是关键。先把核心两种跑通，扩展场景留给后续迭代。Chaosd 端点放 config.yml（下一篇填）是为了让代码层 Tool 不依赖具体环境配置。

### 7.5 report.py

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/report.py。

负责把整轮测试结果整合成报告,提交到 GitHub。

action 是 generate_and_push,接收一个 run_data dict(包含 cluster
信息、注入的故障列表、各 SDK 跑的结果、observability 抓的数据)。

内部:
1. 用 Jinja2 模板渲染 Markdown 报告。模板路径
   ~/.hermes/skills/robustmq-chaos-test/templates/report.md.j2,
   下一讲再写,这一步先调 jinja2.Template 加载即可。
2. 用 json.dumps 写 JSON 报告。绝对别调 LLM 生成 JSON,LLM 写 JSON
   字段名会飘,污染下游所有解析。
3. git push 到 GitHub 公开仓库 test-reports。仓库地址从 config.yml 读。
   git push 用 Deploy Key:私钥放在 ~/.ssh/test-reports-deploy,
   通过 GIT_SSH_COMMAND="ssh -i ~/.ssh/test-reports-deploy" 指定。
4. 返回 {"json_path": "...", "markdown_path": "...",
   "github_url": "...", "run_passed": bool}。

run_passed 字段的逻辑:核心场景全过 + 非核心场景通过率 ≥ 75%。

写新报告时顺手清理 30 天前的本地临时文件,别让磁盘炸。
```

第一条是第 24 篇跑出来的硬规则：结构化的东西用代码生成，叙述性的东西交给 AI。第二条把 git push ＋ Deploy Key 落到代码，这是第 25 篇反问那一轮收紧的硬规则。第三条是细节，但漏了 30 天后磁盘真会炸。

### 7.6 SKILL.md 骨架

#### (1) 提示词

5 个 Tool 都写完了，该写 Skill 把它们串起来。Skill 的提示词跟 Tool 不一样——Tool 的提示词主要传"接口约束"；Skill 的提示词主要喂"已经写好的 Tool 接口"。

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/SKILL.md,告诉 Agent 在 cron
触发或手动触发时,按什么顺序调我已经写好的 5 个 Tool。

5 个 Tool 是 cluster / observability / client / chaos / report。每个
Tool 的 handler 签名、action 取值、参数定义、返回 JSON 字段,见下面贴的
接口表。直接照着调,别编不存在的字段。

骨架按这几节来:
- frontmatter,name=robustmq-chaos-test,requires_tools 列全 5 个
- When to Use,讲清 cron 触发("按 P0 跑一轮..."这种 prompt)和手动 CLI
  触发(hermes "按 P1 跑一轮 mq9 故障场景")各自怎么识别
- 测试前置检查,确认无残留进程
- 单场景执行五步:基线 snapshot → 注入故障 → 故障期间跑 SDK 测试
  (只记录不判断)→ 等故障窗口结束自动恢复 → 自愈验证(pass/fail
  唯一依据)
- 通过失败判断,exit_code=0 且 lost=0 且 p99_ms<500
- 报告归档,直接调 report.py 的 generate_and_push,它会处理 git push
- Pitfalls

具体场景(broker-kill 用什么 target、network-delay 多少毫秒之类)

[贴 5 个 Tool parameters schema]
```

这步先留白，下一篇再用一个新提示词把场景填进去。

#### (2) Skill 的灵魂：把 Tool 接口完整喂给 AI

提示词的核心动作是把 Tool 接口完整喂给 AI。AI 不需要猜 chaos 接受什么参数、client 返回什么字段，已经写好的真实接口直接贴进去，AI 写出来的 Skill 调用方式就跟代码一一对应。

这一步是 Skill 的灵魂。Skill 是工作流编排，编排的对象是 Tool，对象不清楚就编排不出来。如果不把 Tool 接口贴给 AI，AI 会编出根本不存在的 Tool 调用，Skill 加载之后跑就报错——这正是第 3.2 节"Tool 在先、Skill 在后"原则的实操落地。

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/abf0a1ce8a34e871140e634b437d679c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/27_自动测试_04：编写Skill套件/abf0a1ce8a34e871140e634b437d679c_MD5.jpg
用途：展示 5 个 Tool + 1 个 SKILL.md 的依赖顺序
内容：按依赖关系排列的开发顺序图，cluster.py 在最前（被所有 Tool 依赖），其后依次是 observability.py、client.py、chaos.py、report.py，最后是 SKILL.md（说明 Tool 接口后才写）
-->

## 8. 逐个点评：AI 写出来的代码到底怎么样

5 个 Tool + 1 个 Skill，六段代码全部写完，跑通了单点验证。但跑通不等于写得好。真实工程师拿到 AI 给的代码，第一件事是退一步整体看一遍，评判每一段是好是坏，该改的赶紧改。代码在这里：https://github.com/robustmq/robustmq/tree/main/chaos-test

提示：当前系列的代码是第一版本的代码，因为这个代码仓库是社区的代码，会持续更新，有可能去代码仓库看的时候代码就已经更新了（可以用最新的代码来跑功能），所以留了第一版本原始代码的打包：https://pan.baidu.com/s/16wp7j6qqfFrBD7BaDSvUOQ?pwd=6666

下面挨个点评。

### 8.1 cluster.py 的点评

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/e760d01681b75e82b24ae923238e99fe_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 写得扎实的地方

##### ① ROBUSTMQ_HOME fail-fast 落实彻底

env 没有设置就立刻返回 error 并给出 export 示例，工程师看到这条错误不用猜，临时目录管理也干净。

##### ② 失败回滚组合出现

`_kill_all` 和 `_cleanup_data_dirs` 组合出现，start 失败时一并回滚，不会在 `/tmp` 留下记录。

#### (2) 要挑刺的地方

##### ⑤ 健康检查太脆

等 5 秒是拍脑袋定的，RobustMQ 在慢机器上可能 8 秒才 ready，在快机器上 2 秒就好了，写死 5 秒会导致在两种机器上都有问题。正确做法是轮询：每 1 秒探一次，最多等 30 秒，探到就继续。

##### ② status 里 endpoint 永远硬编

`status` 里 endpoint 永远硬编 `127.0.0.1:1883`，如果 broker-1 挂了但 2/3 还活着，这条 endpoint 对外说"可用"其实对不上。不是 blocker，但会让 downstream 诊断信息变脏。

#### (3) 修改

把 `time.sleep(_HEALTH_TIMEOUT)` 换成带重试的 `_wait_healthy` 循环，超时参数可配。其他暂时够用，等真正跑出问题再迭代。

### 8.2 observability.py 的点评

#### (1) 代码克制

代码非常克制，做到了该做的三件事，没有多余逻辑。`_tail_lines` 用读全文件再切片的做法，注释里也坦白"适合短日志"。`_parse_prometheus` 只认 4 个关键指标，是有意缩减范围的设计，没问题。

#### (2) 需要关注的一处

`_scrape_metrics` 对 5 秒超时没有区分"broker 根本没开 HTTP 端口"和"broker 开了但慢响应"，两种情况都落到 `{"error": "scrape failed:..."}` 里。chaos 注入之后 broker 可能已经被 kill，这个 error 是预期行为不是告警，但 Agent 看到 error 字段可能会误判为异常中止。

#### (3) 修改

改法是在返回里加一个 `reachable: false` 字段而不是只有 error，让 Agent 能区分"数据收集的探针失败"和"Tool 自身出错"。

### 8.3 client.py 的点评

#### (1) script_format_error 是最有价值的工程判断

`script_format_error` 单独列 status 这个决定很对，是这个文件最有价值的工程判断。并发用 `ThreadPoolExecutor` 而不是 Hermes delegate 也合理，Tool 内部并发比跨 Agent 调度可控得多。

#### (2) 版本切换用 shell prefix 是务实选择但有代价

版本切换用 shell prefix 字符串拼接是务实选择，避免了跨进程环境传递的麻烦，代价是 `_VERSION_SETUP` 里的路径（`$HOME/.gvm`、`$HOME/.sdkman`）如果机器上不存在，错误信息会被 subprocess 的 stderr 淹没，`_run_one` 只知道 `exit_code != 0`，不知道是 gvm 没装还是测试真挂了。

#### (3) version=default 时缺一行日志

一个小隐患：`version="default"` 时版本前缀是 `"".format(version="default")`，最终 cmd 是 `bash "<script>"`，等于完全跳过版本切换，这个行为是对的，但没有任何日志说明"跳过了版本切换"，排查时会浪费时间，加一行 `logger.debug` 就够了。

### 8.4 chaos.py 的点评

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/8c48d6b1abed75ca0c482715a3002709_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 几个关键约束执行得很彻底

Chaosd 端点从 config.yml 读、绝不 hardcode，这个约束执行得很彻底。用手写 YAML 解析而不是 `import yaml` 是对的，避免了 yaml 文件不在环境里时的 ImportError，这是 Tool 在别人环境里被复用时最容易踩的坑。fault 记录落盘 JSON 也是关键设计，恢复 fault_id 对应的 chaosd_uid 不用依赖内存状态，session 重启后也能 recover。

#### (2) broker-kill 的 recover 语义差异需要说明

需要补的是：broker-kill 在 Chaosd 语义里是"发信号"，SIGKILL 打出去进程立刻消失，但 Chaosd 的 recover 接口是撤销攻击配置，不是重启进程。recover 成功不等于 broker 恢复，只等于 Chaosd 停止攻击。这个语义差异应该在 Tool description 里说明白，不然 Agent 调完 recover 立刻跑 client 验证，会把"broker 还没来得及重启"误判为测试失败——这正是第 4.2 节"跨文件语义一致性"在实战中的典型例子。

### 8.5 report.py 的点评

#### (1) 三个设计决定都做对了

三个设计决定都做对了：Jinja2 渲染 Markdown、`json.dumps` 写 JSON（不走 LLM）、git push 用 Deploy Key。Fallback 机制（Jinja2 缺失或模板不存在时退化到纯 Python 渲染）保证了 report 在业务层还没就位时也能跑，是让代码层和业务层解耦的关键。

#### (2) git clone 的真实风险

有一个真实风险值得注意：`_push_to_github` 每次都 git clone 一个完整（浅）副本，如果仓库积累了几千个 report，克隆会越来越慢。更稳的做法是维护一个持久化的本地 clone（在 `_CLONE_BASE` 下），每次 push 前 `git pull`，而不是重新 clone。现阶段还不是问题，但系统跑 7×24 半年后这里会成为明显瓶颈，记一条 TODO 比以后定位慢 push 要省时间——这正是第 4.3 节"运营时的时间函数"在实战中的典型例子。

#### (3) 同名场景的边界条件

`_compute_run_passed` 里有一个边界条件：同名场景出现多次时，后面的会覆盖前面的（`by_name[name] = s`）。实践中不应该有重复名，但如果 Agent 在某轮出 bug 跑了两次同名场景，这里会静默丢弃一条，pass/fail 算错但没有任何提示。改法是在赋值前检查 `if name in by_name: logger.warning(...)`，一行搞定。

### 8.6 SKILL.md 骨架的点评

#### (1) 骨架结构是对的

骨架结构是对的：触发条件、前置检查、单场景五步、pass/fail 判据、报告归档、pitfalls，缺一节都会让 Agent 在某个分支上发呆。"故障期间只记录不判断"这条在 Step 3 里说得很明确，是整个 Skill 最重要的业务规则，放对了位置。

#### (2) Step 5 的措辞需要收紧

有一处措辞需要收紧：Step 5 写的是 `wait 60 seconds after recovery`，但 recovery 指 Chaosd recover 成功还是 broker 进程重新健康？上面第 8.4 节点评提到的语义差异，这里就显现了。建议改成"等 broker-1 健康检查返回 200 后再等 60 秒"，让 Agent 知道要先验活才开始计时，否则 60 秒可能有一半在等 broker 重启，留给自愈验证的时间不够，结果偏悲观。

#### (3) Circuit Breaker 留白要补

Circuit Breaker 那节说"pause 调度直到人工确认"，具体怎么 pause，是 Hermes 有 pause cron 的 API 还是要人去改 cron.yml，这里留白，下一篇配 Cron 时要补上。

## 9. 整体看一眼：从单点验证到工程可用

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/1f04d0f97565dc1fca1236569f4379ad_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 9.1 整体水平：工程可用

六个文件的代码整体水平基本达到了"工程可用"这个标准。AI 执行了提示词里所有明确说过的约束，没有幻觉出不存在的接口，边界情况（config 缺失、二进制不存在、Jinja2 没装）也都有降级处理。这是好提示词的直接结果，接口签名、硬约束、已有依赖三件事交代清楚，AI 才能写出跟方案对得上的代码。

### 9.2 工程师把关的边际价值在两处

工程师把关的边际价值体现在两个地方，正是第 4 章方法论的实战落地：

#### (1) 跨文件的语义一致性

比如 chaos.py 的 recover 语义和 SKILL.md 的等待逻辑之间的裂缝，AI 在写每个文件时看不见全局，只有人通读一遍才能发现。

#### (2) 运营时的时间函数

比如 cluster.py 的健康检查超时和 report.py 的 clone 策略，AI 写的是"当前能跑"，人要补的是"跑半年后还能跑"。

把关不是重写，是用最小改动堵住这两类漏洞。

### 9.3 单点验证都通过

每个 Tool 都通过单点对话验证过——hermes 进交互，告诉 Agent 调具体 Tool，返回结果回来，这个 Tool 就活着。Skill 骨架加载到 Hermes 后，通过 `hermes /skills` 能看到 `robustmq-chaos-test` 出现在列表里，说明 Hermes 识别了它。

### 9.4 还差业务层

但还差业务层，下面五件事留到下一篇：

#### (1) 场景描述

具体的场景描述（broker-kill 用什么 target、各档跑哪些场景）还是留白的，Skill 没拿到具体场景没法跑完整流程。

#### (2) 报告 Markdown 模板

报告 Markdown 模板 `report.md.j2` 没写，report.py 现在调 Jinja2 加载会报模板找不到。

#### (3) config.yml

config.yml 没填，SDK 矩阵、Chaosd 端点、报告仓库地址还都是空的。

#### (4) GitHub Deploy Key

GitHub Deploy Key 没生成，test-reports 仓库没创建，git push 真跑会失败。

#### (5) cron.yml

cron.yml 还没装，系统现在只能手动 CLI 触发，还不是 7×24。

下一篇把这五件事做完，然后用一段对话把整个系统串起来跑一遍，让读者真的看到报告 push 进 GitHub 仓库。

## 10. 小结与思考

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/84d35215cddd2e7fc273389b52dc2e7b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 10.1 小结

写代码是从方案文档到可运行代码的第三次翻译。AI 时代，这一步从体力劳动变成质量把关，工程师的角色不是敲键盘，是写好提示词。

好的开发提示词包含三件事：接口签名 ＋ 硬约束 ＋ 已有依赖。Tool 的提示词重在传约束，Skill 的提示词重在喂 Tool 接口。哪一件没传到位，AI 给的代码就会偏离方案文档，跑起来出问题。

开发顺序也有讲究：依赖在先、Tool 在先。cluster.py 第一个写，因为后面所有 Tool 依赖它给的 endpoint；Skill 最后写，因为它是 Tool 的使用说明书，说明书要等东西做出来再写。

跟着这个顺序把六个文件写完，代码层就齐了。下一篇填业务层，然后让 Hermes 真的跑起来，看见报告 push 进 GitHub 仓库。

### 10.2 思考

回想最近一次让 AI 帮忙写代码的经历。如果用这一篇的"接口签名 ＋ 硬约束 ＋ 已有依赖"三件事重新组织提示词，最容易漏掉的是哪一件？为什么？
