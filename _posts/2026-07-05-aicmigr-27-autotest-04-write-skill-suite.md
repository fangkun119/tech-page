---
title: 传统项目迁AI 27：自动测试 - 编写Skill套件
author: fangkun119
date: 2026-07-05 07:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
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


## 1. 开篇：从方案文档到可运行代码

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/f7ba5f036264f70b2b53ac20e7989d5b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

上一篇装好了 Hermes、跑通了 Hello World，也在 Hermes 用户目录里定位了第 25 篇方案文档提到的每个文件位置。本篇往前走一步——把方案文档翻译成真正能跑的代码。

做法是用提示词驱动 Claude Code 写完 robustmq-chaos-test Skill 套件（5 个 Python Tool + 1 个 SKILL.md），再退一步通读 AI 写出来的代码做把关。

### 1.1 第三次翻译：从方案文档到可运行代码

这件事在系列里是第三次翻译。前两次的产物是文档，这一次的产物是代码。

| 翻译次数 | 输入 | 产物 |
|---|---|---|
| 第一次（第 25 篇） | 一句话需求 | 完整设计文档 |
| 第二次（第 25 篇） | 完整设计文档 | 完整方案文档 |
| 第三次（本篇） | 完整方案文档 | 可运行代码 |

前两次是脑力劳动 —— 把模糊的需求一点点逼清楚，落在文档里。<span style="color: red; font-weight: bold;">第三次不同：体力活被 AI 接走了，只要提示词到位，AI 写代码比人快、比人准。</span>

代码仓库地址：[https://github.com/robustmq/robustmq](https://github.com/robustmq/robustmq)

### 1.2 工程师的新角色：写提示词 + 通读把关

既然 AI 把敲键盘的活干了，工程师做什么？

**<span style="color: red; font-weight: bold;">答案是两件事 —— 写好提示词、通读 AI 写出来的代码堵住漏洞。</span>** 这两件事 AI 自己做不好，是工程师在 AI 时代的边际价值所在。

写提示词对应"定义"：把方案文档里的判断翻译成 AI 能听懂的约束 
- 接口签名、硬约束、已有依赖，让 AI 在你画好的框里写。

通读把关对应"验收"。这里有个反直觉的判断要先抛出来：**<span style="color: red; font-weight: bold;">AI 写完代码跑通了一次 Hello World，不代表代码过关。</span>**

类比传统软件开发：一个 Controller 接口能返回 200，不代表这个接口在生产环境 7×24 跑半年不出问题。AI 写的代码同样 —— 单次跑通只证明逻辑通顺，不证明跨文件语义一致、运营时不出问题、半年后还跑得动。

这两类漏洞（跨文件的语义一致性、运营时的时间函数）会在第 6 章点评 AI 代码时具体展开。本篇接下来的任务，是把"第三次翻译"这套打法在 robustmq-chaos-test 套件的真实开发里复现一遍 —— 贴出每个 Tool 的提示词原文，点评 Claude Code 写出来的代码，让方法论不仅知其然，也知其所以然。

## 2. 开发提示词三件套：让 AI 写出能用的代码

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/a5494e821c590593b120dc043c436f74_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第 1 章抛出了"单点跑通不等于写得好"这个判断，接下来的问题是：怎么让 AI 写的代码一次到位、跑起来就能对接上下游？

答案在工具上 —— 这次翻译的工具是提示词。好的开发提示词包含三件事：<span style="color: red; font-weight: bold; background-color: yellow;">接口签名、硬约束、已有依赖</span>。三件都到位，AI 给的代码才能直接对接上下游、跑起来就可用。

### 2.1 接口签名：模块与外界的契约

接口签名告诉 AI 这个模块支持哪些动作（action）、输入参数长什么样、返回什么结构的 JSON、handler 怎么注册。

传统软件工程里有"接口契约文档"——上下游模块按契约对接，契约一旦定死，各方只能在契约之内填实现。接口签名在 AI 时代的角色完全一样，只是契约的接收方从"另一个工程师"变成了"AI"。这部分写得越具体，AI 给出的代码越能直接对接上下游，越少需要二次缝合。

为什么这一件最重要？因为接口是模块与外界的契约。<span style="color: red; font-weight: bold;">接口模糊，AI 就要"猜"上下游怎么调它，猜出来的代码常常跟真实调用方对不上。</span>

| 对比项 | 传统开发 | AI 时代开发 |
|---|---|---|
| 契约载体 | 接口契约文档 / Swagger | 提示词里的接口签名 |
| 契约接收方 | 另一个工程师 | Claude Code |
| 落地动作 | 各方在契约内填实现 | AI 在签名内生成代码 |

我的看法是：先把 action 取值、参数字典、返回 JSON 字段逐条列出，比写一段功能描述要值钱得多。

- 功能描述说"启动集群"，AI 不知道返回什么；
- 签名写清"返回 `{status, endpoint, data_dirs}`"，AI 就知道下游该读哪几个字段。

### 2.2 硬约束：哪些事绝对不能做

<span style="color: red; font-weight: bold;">硬约束</span>是哪些事<span style="color: red; font-weight: bold;">绝对不能做的负面清单</span>。

- ⚠️ 不能引入 Docker
- ⚠️ 不能给环境变量配默认值
- ⚠️ 失败时不能抛异常
- ⚠️ 临时目录必须清理。

这些约束通常来自前几轮的方法论沉淀（本系列里 24、25 篇跑出来的判断）。它们是工程师踩过坑后的判断，AI 不知道、也不会主动想到。

你可能会问：为什么不直接把功能写明白，要费劲列负面清单？因为硬约束比功能描述更有效。

原因在于 AI 的默认实现倾向"通用、保守、不出错" —— 它会选最常见、最教科书的写法。但生产场景常常需要反默认：

| AI 的默认倾向              | 生产场景的反默认                    | **不写进约束会怎样**            |
| --------------------- | --------------------------- | ----------------------- |
| 给环境变量配一个合理默认值         | fail-fast 比偷偷用默认值强一万倍       | ❌ **用户路径没配对，跑起来才发现不存在** |
| 失败时抛异常让上层处理           | Agent 循环见到异常会中断，必须返回 error  | ❌ **整个测试流程被异常打断，无法定位**  |
| 用通用的 JSON 生成方式（含 LLM） | 结构化数据用代码生成，LLM 写 JSON 字段名会飘 | ❌ **下游解析报错，污染整份报告**     |
| 临时目录用完不清理             | 7×24 跑半年后磁盘被撑爆              | ❌ **系统静默挂掉，定位困难**       |

这些反默认的判断，必须靠硬约束显式压给 AI。<span style="color: red; font-weight: bold;">AI 不读你的项目历史，只读你这段提示词。</span>

### 2.3 已有依赖：把环境明明白白告诉 AI

已有依赖是这个模块运行时会用到的具体配置：

- 端口分配规则
- 健康检查 URL
- 二进制路径环境变量
- 外部端点地址。

这部分让 AI 不用猜 —— 直接照着真实环境写，跑起来就能用。

为什么已有依赖要单独拎出来？因为它跟"功能逻辑"是两件事。AI 写 spawn 进程的代码不需要知道端口是 1883 还是 21883；但<span style="color: red; font-weight: bold;">端口分配规则不是 AI 能猜的，必须工程师告诉它。</span>

| 内容归属 | 具体事项 | 写在哪 | 谁的强项 |
|---|---|---|---|
| 实现怎么写 | spawn / 超时 / edge case | 留给 AI | AI 的强项 |
| 环境长什么样 | 端口 / URL / 路径 / 端点 | 写进提示词 | 工程师的强项 |

把"实现怎么写"留给 AI，把"环境长什么样"明明白白写进提示词，分工才清晰。

### 2.4 写法的关键：预设加余地

三件套讲清了"写什么"，但提示词的"怎么写"还有一条原则：**预设 + 余地**。

预设 —— 关键约束、不能踩的坑、上下游契约，这些预设进提示词，让 AI 在你画好的框里写。传统代码评审里有个常识：给开发者清晰的边界（接口、约束、规范），但不教他每一行怎么写。

余地 —— 怎么实现 spawn、怎么处理超时、edge case 怎么写，这些留给 AI 发挥，不要把提示词写成二十条编号清单。

| 写法       | 结果                         |
| -------- | -------------------------- |
| 几段话讲清三件套 | AI 在框里自由发挥，能给出更强写法         |
| 二十条编号清单  | AI 被锁死在工程师预设的实现路径上，发挥空间被堵死 |

我的立场是：<span style="color: red; font-weight: bold;">提示词几段话讲清楚，比模板化的二十条清单靠谱。</span>编号清单的本质是"替 AI 写代码"，但 AI 写实现比人快、比人稳，工程师替它写的实现往往是次优解。

### 2.5 工程师把关的两件事

三件套把"写"这件事讲完了，"读"还有最后一步 —— 通读 AI 写出来的代码把关。AI 时代的代码评审，重点不再是"这段代码写得好不好看"，而是 <span style="color: red; font-weight: bold;">AI 看不见的两类漏洞</span>。

| 漏洞类型                                                         | 含义                          | 典型例子                                                     |
| ------------------------------------------------------------ | --------------------------- | -------------------------------------------------------- |
| <span style="color: red; font-weight: bold;">跨文件语义一致性</span>                                                     | AI 写每个文件时看不见全局，各自自洽，合在一起有裂缝 | ❌ chaos.py 的 `recover` 语义 vs SKILL.md 的等待逻辑              |
| <span style="color: red; font-weight: bold;">运营时的时间函数</span> | AI 写的是"当前能跑"，缺"跑半年后还能跑"的判断  | ❌ cluster.py 写死 `time.sleep(5)`、report.py 每次重新 git clone |

第一类漏洞的典型例子：

- chaos.py 里 chaosd 的 `recover` 接口是撤销攻击配置，不是重启进程。
- 但 SKILL.md 里"等故障恢复"的逻辑如果没说清"是等 chaosd recover 成功还是等 broker 进程重新健康"。
- Agent 调完 recover 立刻跑 client 验证，会把"broker 还没来得及重启"误判为测试失败。

第二类漏洞的典型例子：

- cluster.py 写死 `time.sleep(5)` 等健康检查 —— 慢机器 8 秒才 ready、快机器 2 秒就好，写死 5 秒在两种机器上都有问题。
- 又如 report.py 每次都重新 git clone 一个浅副本，仓库积累几千个 report 之后，克隆会越来越慢。这些不是 blocker，但系统跑 7×24 半年后这里会成为明显瓶颈。

一句话总结：<span style="color: red; font-weight: bold;">AI 写的是"当前能跑"，人要补的是"跑半年后还能跑"。</span>

这两类漏洞会在第 6 章结合每个 Tool 的点评具体展开。在动手写之前，先定清楚多个 Tool 和 Skill 的开发顺序——先写谁、后写谁，直接决定能不能跑通。

## 3. 开发顺序：依赖在先、Tool 在先

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/bdfd7868db862b6bc5a825272cbf41da_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

写多个 Tool 加一个 Skill 时，顺序遵循两条原则。

### 3.1 两条原则

| 原则 | 含义 | 反直觉之处 |
|---|---|---|
| 依赖在先、被依赖在后 | 按依赖关系排，每个新 Tool 都能直接调前面已写好的 Tool | <span style="color: red; font-weight: bold;">表面看像流水线，实则避免了"写到一半发现下游接口还没定"的卡顿</span> |
| Tool 在先、Skill 在后 | Tool 接口稳定后再写 Skill | 很多人觉得 Skill 是"大脑"该先写，但 <span style="color: red; font-weight: bold;">Skill 是 Tool 的使用说明书</span> |

下面分别展开。

### 3.2 依赖在先：以 robustmq-chaos-test 套件为例

robustmq-chaos-test 套件五个 Tool 的依赖顺序是这样的：

| 顺序  | Tool             | 功能说明              | 依赖关系                                                 |
| --- | ---------------- | ----------------- | ---------------------------------------------------- |
| 1   | cluster.py       | 起停测试集群            | 所有其他 Tool 的动作都假设集群在跑                                 |
| 2   | observability.py | 从集群进程拿日志和 metrics | 依赖集群活着，跟其他 Tool 没强依赖                                 |
| 3   | client.py        | 连集群跑 SDK 测试       | 依赖 cluster.py 给的 endpoint                            |
| 4   | chaos.py         | 在集群上注入故障          | 依赖 cluster.py；故障期间要跟 observability.py 和 client.py 配合 |
| 5   | report.py        | 整合前四个 Tool 的产出    | 依赖前面四个的全部结果                                          |

依赖关系画成图就是这样：

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/tool-dependency.svg" style="display: block; width: 800px;" alt="robustmq-chaos-test 五个 Tool 的依赖关系图">

这个顺序对应传统软件工程里"接口先行"的常识：

- 先定下游模块的对外接口
- 上游模块才有东西可以调

obustmq-chaos-test 套件按这个顺序写下来，每个新 Tool 提示词里都可以直接贴前面已经写好的 Tool 接口给 AI 参考，从没出现过"下游接口还得回头改"的情况。

### 3.3 Tool 在先：Skill 最后写

Skill 最后写。这一步反直觉——很多人觉得 Skill 是"大脑"，应该最先写。

但 Skill 是 Tool 的使用说明书，说明书要先有被说明的东西才能写。Tool 接口都定下来之后，Skill 才知道按什么顺序调谁、传什么参数。

为什么这条反直觉但正确？因为 Skill 的本质是工作流编排，编排的对象是 Tool。<span style="color: red; font-weight: bold;">对象不存在，编排就是空中楼阁 —— AI 会编出根本不存在的 Tool 调用，Skill 加载之后一跑就报错。</span>先写 Tool 再写 Skill，等价于"先有产品再写说明书"，符合人类工程师做事的真实顺序。

### 3.4 代码层与业务层分离

<span style="color: red; font-weight: bold;">代码层（Tool 和 Skill 的 Python 代码）跟业务层（具体测哪些场景、SDK 版本矩阵、报告字段、Cron 频率）要分开做。</span>先把代码层做完，再填业务层，工程上更稳。

为什么这么切？代码层是通用 Hermes 编程，跟"测什么场景、SDK 用什么版本、报告什么字段"无关

- cluster.py 怎么 spawn 进程
- chaos.py 怎么调 Chaosd HTTP API

这些是 Hermes Tool 的通用写法，换个被测系统也能套。
业务层是混沌测试这个具体业务的定义。

先把通用层做完、稳定下来，再填业务层，业务变更不会动摇代码层。
本篇只做代码层。业务层（场景库、Jinja2 模板、config.yml、Deploy Key、cron.yml）留给下一篇。

## 4. 实战全景：系统跑起来长什么样

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/41fa8d14a8264d1085b787c8ed45da8b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

动手写代码之前，先把整套系统真正跑起来时长什么样想清楚。脑子里有这幅图，写到一半才不会忘了为什么要写这个文件。

类比传统软件工程：写一个微服务之前，架构师会先画一张部署图，标清楚谁调谁、数据往哪流。AI 时代写 Skill 套件也是同样，先画出"系统跑起来时长什么样"，再倒推每个文件该承担什么职责。

### 4.1 一次完整混沌测试的九个步骤

一次完整的混沌测试有 9 个步骤：

```text
1. Cron 在 P0/P1/P2 三档某一档的时间点触发,触发动作是给 AI Agent
   发一段自然语言:"按 P0 跑一轮 MQTT 基础场景"。
2. Agent 收到后加载 robustmq-chaos-test Skill,按 Skill 的 Procedure
   一步步调 Tool。
3. 先调 cluster.py 起一个测试集群, 等 RobustMQ 进程健康。
4. 集群跑起来了就调 chaos.py 调 chaosd 注入故障。
5. 注入到位之后调 client.py, 在故障期间用预装的多语言版本管理工具
   切换到目标 SDK 版本跑一轮 basic-pubsub 测试, 这段只记录不判断。
6. 等故障窗口结束 chaos.py 自动恢复, 再调 client.py 验证自愈。
7. 整轮过程中 observability.py 收集 RobustMQ 进程的日志和 metrics。
8. 所有数据交给 report.py, 程序化生成 JSON ＋ Markdown 双格式报告,
   用 Deploy Key git push 到 GitHub 公开仓库。
9. Quality Dashboard 是静态页面,从 GitHub 仓库读取,展示这轮的结果。
```

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/chaos-test-9-steps.svg" style="display: block; width: 800px;" alt="一次完整混沌测试的 9 步链路图">
<!--
图片内容说明
路径：imgs/aicmigr-27-autotest-04-write-skill-suite/chaos-test-9-steps.svg
用途：展示一次完整混沌测试的 9 步链路图
内容：从 Cron 触发开始，到 AI Agent 加载 robustmq-chaos-test Skill，依次调用 cluster.py（起测试集群）、chaos.py（注入故障）、client.py（故障期间切换 SDK 版本跑测试，只记录不判断）、chaos.py 自动恢复、client.py 自愈验证、observability.py（收集日志和 metrics）、report.py（生成 JSON+Markdown 报告并 git push），最终 Quality Dashboard 从 GitHub 仓库读取并展示结果
-->

### 4.2 链路分层

9 个步骤串起来是一条完整链路，可以分成四层：

| 层 | 构成 | 职责 |
|---|---|---|
| 触发源 | Cron | 按 P0/P1/P2 三档频率发自然语言指令 |
| 指挥层 | AI Agent ＋ Skill | 解析指令、按 Procedure 编排 5 个 Tool |
| 执行层 | 5 个 Tool（cluster / observability / client / chaos / report） | 真正干活：起集群、注入故障、跑测试、收数据、出报告 |
| 数据层 | 集群 ＋ Chaosd ＋ GitHub 仓库 | 被测对象、故障源、报告归档 |
| Dashboard | Quality Dashboard 静态页面 | 从 GitHub 读取并对外展示 |

整条链路最后归档到一个公开可读的 Dashboard。

每一段都有人负责，本篇的开发任务，就是把"有人负责"这件事一件一件落到具体文件上。

### 4.3 从链路反推开发清单

链路上每个环节都需要落到具体文件。按链路反推开发任务，清单是这样：

| 实施环节 | 要写的东西 | 类别 |
| --- | --- | --- |
| 接收触发并加载 Skill | SKILL.md | Skill |
| 起停集群 | cluster.py | Tool |
| 收集日志 / metrics | observability.py | Tool |
| 多语言 SDK 调度 | client.py | Tool |
| 故障注入和恢复 | chaos.py | Tool |
| 报告生成 + GitHub 提交 | report.py | Tool |
| 触发 (Cron 调度) | Hermes 自带 Cron 工具 | 白嫖 |
| Agent 循环 / Skill 加载 | Hermes 自带 AI Agent | 白嫖 |

把清单收一收，总共两类东西要写：**5 个 Python Tool + 1 个 SKILL.md**。

其他链路环节 Hermes 全部自带，本篇不用碰：

| 环节 | 归属 |
|---|---|
| Cron 触发 | Hermes 自带 |
| Agent 循环 | Hermes 自带 |
| Skill 加载机制 | Hermes 自带 |

还有一些业务层的东西没列：具体测哪些场景（场景库）、报告长什么样（Jinja2 模板）、SDK 矩阵和 Chaosd 端点参数（config.yml）、GitHub Deploy Key 和仓库怎么配、Cron 三档怎么填——本篇只做代码层，业务层留给下一篇。

这正是第 3.4 节"代码层与业务层分离"在实战中的落地：代码层是通用 Hermes 编程，先把通用层做完、稳定下来，再填业务层，业务变更不会动摇代码层。

## 5. 一气呵成：5 个 Tool + 1 个 SKILL.md

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/b275e52e87ef80ee17278095df1805ad_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

按第 3 章的开发顺序，5 个 Tool 加 1 个 Skill 一气呵成写完。文章只贴提示词，Claude Code 跑出来的真实代码放 GitHub 仓库，读者想看哪个 Tool 的代码直接去对应文件看。

这样安排有两层考虑：一是叙事干净，篇幅留给提示词和点评这两件真正值得读的事；二是<span style="color: red; font-weight: bold;">提示词才是工程师在 AI 时代的产出，代码是 AI 的产出</span>，把 AI 的产出贴满全文反而掩盖了工程师真正的边际价值。

仓库地址：[https://github.com/robustmq/robustmq](https://github.com/robustmq/robustmq)

### 5.1 cluster.py：第一个 Tool 的完整提示词

#### (1) 提示词 

第一个 Tool 把完整提示词展开讲，后面四个 Tool 同构，就不重复拆模板了。

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/cluster.py。

这是混沌测试 Skill 套件的第一个 Tool, 负责在本地起停 RobustMQ 测试集群。

按方案的决策: 不引入 Docker, 直接 spawn 多个 RobustMQ 进程。

action 支持 start / stop / status 三种。

start: 用 subprocess 拉起 3 个 RobustMQ broker 进程, 每个用独立端口 (broker-1: 1883,broker-2: 2883,broker-3: 3883) 和独立数据目录。
数据目录用 tempfile.mkdtemp 创建, 记下来 stop 时清理。
启动后等 5 秒, curl http://127.0.0.1:1883/health 确认健康, 失败立即 kill 全部并返回 {"status": "failed"}。
成功返回 {"status": "running", "endpoint": "127.0.0.1:1883", "data_dirs": [...]}。 

stop: kill 所有 broker 进程,清理 data_dirs。

status: 返回当前运行的进程数和 endpoint。

RobustMQ 二进制路径通过环境变量 ROBUSTMQ_HOME 取, 没设置就 fail-fast
返回 error, 别用默认值兜底, 默认值会让用户误以为路径配对了, 跑起来才发现不存在。

handler 函数签名 (args: dict, **_) -> str, 失败返回 {"error": "..."} 别抛异常, Agent 循环见到异常会中断。
```

#### (2) 三件套如何落进提示词

第 2 章讲过开发提示词三件套 —— 接口签名、硬约束、已有依赖。提示词写得好不好，就看这三件有没有讲清。下表把三件套在这段提示词里的具体落点列出来：

| 三件套  | 提示词里的对应内容                                                                                                                                     |
| ---- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| 接口签名 | - action 支持 start / stop / status；<br>- handler 签名 `(args: dict, **_) -> str`；<br>- 返回 JSON 字段（`status` / `endpoint` / `data_dirs` / `error`） |
| 硬约束  | - 不引入 Docker（第 25 篇决策）；<br>- `ROBUSTMQ_HOME` 不给默认值（fail-fast）；<br>- 失败返回 error 不抛异常（Agent 循环约束）；<br>- 临时目录 stop 时清理（资源泄漏防线）                   |
| 已有依赖 | - 端口分配规则（broker-1/2/3 各自端口）；<br>- 健康检查 URL（`http://127.0.0.1:1883/health`）；<br>- 二进制路径环境变量（`ROBUSTMQ_HOME`）                                   |

这几条硬约束没有一条是凭空想的。

- 不引入 Docker 是第 25 篇方案文档定的；
- `ROBUSTMQ_HOME` fail-fast 来自"fail-fast 比偷偷用默认强一万倍"的反默认判断；
- 失败不抛异常来自 Agent 循环的工作机制；
- 临时目录清理是资源泄漏防线。

每一条都能往回追溯到第 24、25 篇的某条决策。

类比传统软件工程，这等价于接口契约文档的写法：

- 先定 action 取值、参数字典、返回 JSON 字段
- 再附上"不能做什么"和"环境长什么样"。

差别只在于这份契约的接收方从"另一个工程师"变成了"AI"。

### 5.2 observability.py

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/observability.py。

负责从 RobustMQ 集群收集观测数据,后面 chaos 故障注入和 client SDK
测试都会调它打快照。

action 支持 collect_logs / collect_metrics / snapshot 三种。

collect_logs: 从 cluster.py 起的 broker 进程的 log 文件抓最近 N 行 默认 100),按节点返回 dict[node_name, list[str]]。log 路径在 data_dirs/<node>/logs/ 下。

collect_metrics: 调 RobustMQ 内置的 /metrics 端点(每节点的 HTTP 端口), 拉 Prometheus 格式数据,返回关键指标(connections / messages_in / messages_out / errors)。

snapshot: 一次性收 logs + metrics,加时间戳,用于故障注入前后对比。
```

这个 Tool 第一期不复杂，就三件事：读日志文件、调 metrics 端点、snapshot 打快照。它是第 25 篇方案里"协议兼容性归因"的数据基础——故障期间和故障后各打一次 snapshot，放进报告供 dashboard 展示，问题定位才有依据。

### 5.3 client.py

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/client.py。

负责调度多语言 SDK 跑测试。按之前方案的决策: 不用 Docker 隔离, 用本地版本管理工具切换。服务器上预装了 pyenv / gvm / rustup / sdkman / nvm。

action 是 run,参数 sdk(python/go/rust/java)、version(具体版本)、scenario(basic-pubsub / failover / latency 等)、cluster_endpoint。

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

不传 sdk 时, 用 ThreadPoolExecutor 并发跑全部语言。并发逻辑放 Python 里
就行,别用 Hermes 的 delegate 机制,Tool 内部并发更可控。
```

版本管理切换、stdout 协议、format error 分级这几条，都是工程师真实跑过混沌测试才会想到的细节。少一条，系统跑半年准翻车。

### 5.4 chaos.py

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/chaos.py。

负责故障注入和恢复。按之前方案的决策: chaosd 主 + tc/kill 补。 

判断标准是"故障是否需要被精确测量和回放", 需要就用 chaosd (进程级 kill 信号、网络精确延迟分布、磁盘 I/O 限速、时钟穿越), 只是验证可达性就用 tc/kill 系统命令。

action 支持 inject / recover。

inject: 接收 fault_type(broker-kill / network-delay / network-partition / disk-fill 等)、target(集群里的节点名)、duration_seconds 和具体参数,调 Chaosd HTTP API 注入。 返回 {"fault_id": "...", "status": "active"}。

recover: 按 fault_id 撤销故障。

第一版只实现 broker-kill 和 network-delay 两种, 其他先返回 not_implemented,后面迭代加。

chaosd 端点配置在 ~/.hermes/skills/robustmq-chaos-test/config.yml 里
读配置文件, don't hardcode。
```

提示词里两条关键约束都体现了第 2.4 节"预设加余地"的写法。一是让 AI 别把所有故障类型一次写完——先把核心两种跑通，扩展场景留给后续迭代；二是 chaosd 端点放 config.yml（下一篇填），让代码层 Tool 不依赖具体环境配置。

### 5.5 report.py

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/tools/report.py。

负责把整轮测试结果整合成报告,提交到 GitHub。

action 是 generate_and_push,接收一个 run_data dict(包含 cluster
信息、注入的故障列表、各 SDK 跑的结果、observability 抓的数据)。

内部:
1. 用 Jinja2 模板渲染 Markdown 报告。模板路径
   ~/.hermes/skills/robustmq-chaos-test/templates/report.md.j2。
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

提示词里三条硬规则都是前两篇跑出来的判断，对照如下：

| 硬规则 | 出处 |
|---|---|
| 结构化的东西用代码生成，叙述性的交给 AI（`json.dumps` 写 JSON 不走 LLM） | 第 24 篇跑出来的硬规则 |
| `git push` 用 Deploy Key | 第 25 篇反问那一轮收紧的硬规则 |
| 写新报告时清理 30 天前本地临时文件 | 工程细节，漏了 30 天后磁盘真会炸 |

### 5.6 SKILL.md 骨架：把 Tool 接口完整喂给 AI

5 个 Tool 都写完了，该写 Skill 把它们串起来。Skill 的提示词跟 Tool 不一样  —— Tool 的提示词主要传"接口约束"；Skill 的提示词主要喂"已经写好的 Tool 接口"。

```text
帮我写 ~/.hermes/skills/robustmq-chaos-test/SKILL.md,告诉 Agent 在 cron
触发或手动触发时, 按什么顺序调我已经写好的 5 个 Tool。

5 个 Tool 是 cluster / observability / client / chaos / report。每个
Tool 的 handler 签名、action 取值、参数定义、返回 JSON 字段,见下面贴的
接口表。直接照着调,别编不存在的字段。

骨架按这几节来:
- frontmatter,name=robustmq-chaos-test,requires_tools 列全 5 个
- When to Use, 讲清 cron 触发("按 P0 跑一轮..."这种 prompt)和手动 CLI
  触发(hermes "按 P1 跑一轮 mq9 故障场景") 各自怎么识别
- 测试前置检查,确认无残留进程
- 单场景执行五步: 基线 snapshot → 注入故障 → 故障期间跑 SDK 测试
  (只记录不判断)→ 等故障窗口结束自动恢复 → 自愈验证(pass/fail
  唯一依据)
- 通过失败判断,exit_code=0 且 lost=0 且 p99_ms<500
- 报告归档,直接调 report.py 的 generate_and_push,它会处理 git push
- Pitfalls

具体场景(broker-kill 用什么 target、network-delay 多少毫秒之类)

[贴 5 个 Tool parameters schema]
```

这一步先留白，下一篇再用一个新提示词把场景填进去。

#### (1) Skill 的灵魂：把 Tool 接口完整喂给 AI

提示词的核心动作是把 Tool 接口完整喂给 AI。AI 不需要猜 chaos 接受什么参数、client 返回什么字段，<span style="color: red; font-weight: bold;">已经写好的真实接口直接贴进去，AI 写出来的 Skill 调用方式就跟代码一一对应。</span>

这一步是 Skill 的灵魂。Skill 是工作流编排，编排的对象是 Tool，对象不清楚就编排不出来。<span style="color: red; font-weight: bold;">如果不把 Tool 接口贴给 AI，AI 会编出根本不存在的 Tool 调用，Skill 加载之后跑就报错</span>——这正是第 3.2 节"Tool 在先、Skill 在后"原则的实操落地。

类比传统软件工程，这一步等价于"先有产品再写说明书"。产品（Tool）的接口契约一旦定死，说明书（SKILL.md）按真实契约编排工作流，才不会出现说明书里写了一个根本不存在的按钮。

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/tool-skill-dependency.svg" style="display: block; width: 800px;" alt="5 个 Tool + 1 个 SKILL.md 的依赖顺序（SVG 版）">
<!--
图片内容说明
路径：imgs/aicmigr-27-autotest-04-write-skill-suite/tool-skill-dependency.svg
用途：展示 5 个 Tool + 1 个 SKILL.md 的依赖顺序
内容：按依赖关系排列的开发顺序图，cluster.py 在最前（被所有 Tool 依赖），其后依次是 observability.py、client.py、chaos.py、report.py，最后是 SKILL.md（说明 Tool 接口后才写）
-->

## 6. 逐个点评：AI 写出来的代码到底怎么样

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/bad81089a8ccb71e23f68f394455d731_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

5 个 Tool + 1 个 Skill，六段代码全部写完，单点验证跑通。但跑通不等于写得好。

真实工程师拿到 AI 给的代码，第一件事是退一步整体通读一遍，逐段判断好坏，该改的立刻改——这一步是 AI 替不了的。代码在这里：[https://github.com/robustmq/robustmq/tree/main/chaos-test](https://github.com/robustmq/robustmq/tree/main/chaos-test) 

提示：社区代码会持续更新，去仓库看到的可能已经不是第一版（可以用最新代码跑功能）。第一版本原始代码打包在这里：[https://pan.baidu.com/s/1DrzOocv4GClFK3Z7ITY_-g?pwd=6666](https://pan.baidu.com/s/1DrzOocv4GClFK3Z7ITY_-g?pwd=6666)

### 6.1 两类漏洞：点评的尺子

你可能会问：点评 AI 写的代码，凭什么评判好坏？

尺子就是第 2 章抛过的"工程师把关两件事"。每个 Tool 的点评都会落在这两类漏洞上，先具体化一次：

| 漏洞类型 | 含义 | 本篇典型例子 |
|---|---|---|
| 跨文件语义一致性 | AI 写每个文件时看不见全局，合在一起有裂缝 | chaos.py 的 recover 语义 vs SKILL.md 的等待逻辑 |
| 运营时的时间函数 | AI 写的是"当前能跑"，缺"跑半年后还能跑"的判断 | cluster.py 的健康检查超时、report.py 的 clone 策略 |

<span style="color: red; font-weight: bold;">单点跑通只证明逻辑通顺，不证明这两类漏洞不存在。</span>下面逐个点评。

### 6.2 cluster.py：写得扎实，健康检查要改

写得扎实的地方有两条。

第一，`ROBUSTMQ_HOME` fail-fast 落实彻底——env 没设置就立刻返回 error 并给出 export 示例，工程师看到不用猜。

第二，失败回滚组合出现——`_kill_all` 和 `_cleanup_data_dirs` 一并调用，start 失败时不会在 `/tmp` 留下记录。

要挑刺的就是运营时漏洞：健康检查写死了 `time.sleep(5)`。RobustMQ 在慢机器上可能 8 秒才 ready，在快机器上 2 秒就好了，写死 5 秒在两种机器上都有问题。

还有一处不是 blocker 但会弄脏 downstream 诊断信息：`status` 里 endpoint 永远硬编 `127.0.0.1:1883`。如果 broker-1 挂了但 2/3 还活着，这条 endpoint 对外说"可用"其实对不上。

改法：把 `time.sleep(_HEALTH_TIMEOUT)` 换成带重试的 `_wait_healthy` 循环——每 1 秒探一次、最多等 30 秒、超时可配。其他暂时够用，等真正跑出问题再迭代。

### 6.3 observability.py：代码克制，error 字段需分级

代码非常克制，该做的三件事都做了，没有多余逻辑。

`_tail_lines` 用读全文件再切片的做法，注释里也坦白"适合短日志"。`_parse_prometheus` 只认 4 个关键指标，是有意缩减范围的设计，没问题。

需要关注的是运营时漏洞的变种。`_scrape_metrics` 对 5 秒超时没区分两种情况：broker 根本没开 HTTP 端口 vs broker 开了但慢响应，两种都落到 `{"error": "scrape failed:..."}` 里。

为什么这是个问题？chaos 注入之后 broker 可能已经被 kill，这个 error 是预期行为、不是告警，但 Agent 看到 error 字段可能误判为异常中止。

改法：在返回里加一个 `reachable: false` 字段而不是只有 error，让 Agent 能区分"数据收集的探针失败"和"Tool 自身出错"。

### 6.4 client.py：最有价值的工程判断

`script_format_error` 单独列 status 这个决定很对，是这个文件最有价值的工程判断——脚本格式错跟测试失败混在一起算，会让你查问题查到怀疑人生。

并发用 `ThreadPoolExecutor` 而不是 Hermes delegate 也合理，Tool 内部并发比跨 Agent 调度可控得多。

需要留意两个隐患。

第一，版本切换用 shell prefix 字符串拼接是务实选择（避开了跨进程环境传递的麻烦），代价是：`_VERSION_SETUP` 里的路径（`$HOME/.gvm`、`$HOME/.sdkman`）如果机器上不存在，错误信息会被 subprocess 的 stderr 淹没，`_run_one` 只知道 `exit_code != 0`，不知道是 gvm 没装还是测试真挂了。

第二，`version="default"` 时完全跳过版本切换这个行为是对的，但没有任何日志说明"跳过了版本切换"，排查时会浪费时间。加一行 `logger.debug` 就够了。

### 6.5 chaos.py：执行彻底，但 recover 语义需要说明

几个关键约束执行得很彻底。

Chaosd 端点从 config.yml 读、绝不 hardcode。用手写 YAML 解析而不是 `import yaml` 是对的，避免了 yaml 文件不在环境里时的 ImportError——这是 Tool 在别人环境里被复用时最容易踩的坑。fault 记录落盘 JSON 也是关键设计，恢复 fault_id 对应的 chaosd_uid 不依赖内存状态，session 重启后也能 recover。

但这里出现了本篇最典型的跨文件语义漏洞。

broker-kill 在 Chaosd 语义里是"发信号"——SIGKILL 打出去进程立刻消失；但 Chaosd 的 recover 接口是撤销攻击配置，不是重启进程。**recover 成功不等于 broker 恢复，只等于 Chaosd 停止攻击。**

这个语义差异必须在 Tool description 里说明白。不然 Agent 调完 recover 立刻跑 client 验证，会把"broker 还没来得及重启"误判为测试失败。

<span style="color: red; font-weight: bold;">这正是跨文件语义一致性在实战中的典型例子，也是 6.7 节 SKILL.md Step 5 措辞需要收紧的原因。</span>

### 6.6 report.py：三个设计都对，clone 策略要改

三个设计决定都做对了。

Jinja2 渲染 Markdown、`json.dumps` 写 JSON（不走 LLM）、git push 用 Deploy Key。Fallback 机制（Jinja2 缺失或模板不存在时退化到纯 Python 渲染）保证了 report 在业务层还没就位时也能跑，是让代码层和业务层解耦的关键。

两个值得记 TODO 的点。

第一个是运营时漏洞：`_push_to_github` 每次都 git clone 一个完整（浅）副本，仓库积累几千个 report 之后克隆会越来越慢。更稳的做法是维护一个持久化的本地 clone（在 `_CLONE_BASE` 下），每次 push 前 `git pull` 而不是重新 clone。现阶段还不是问题，但系统跑 7×24 半年后这里会成为明显瓶颈，记一条 TODO 比以后定位慢 push 要省时间。

第二个是边界条件：`_compute_run_passed` 里同名场景出现多次时，后面的会覆盖前面的（`by_name[name] = s`）。实践中不应该有重复名，但如果 Agent 在某轮出 bug 跑了两次同名场景，这里会静默丢弃一条——pass/fail 算错但没有任何提示。改法是在赋值前检查 `if name in by_name: logger.warning(...)`，一行搞定。

### 6.7 SKILL.md 骨架：结构对，Step 5 措辞要收紧

骨架结构是对的：触发条件、前置检查、单场景五步、pass/fail 判据、报告归档、pitfalls——缺一节都会让 Agent 在某个分支上发呆。"故障期间只记录不判断"这条在 Step 3 里说得很明确，是整个 Skill 最重要的业务规则，放对了位置。

但 Step 5 的措辞需要收紧——这里就是 6.5 节 chaos.py recover 语义差异显现的地方。

原文写的是 `wait 60 seconds after recovery`，但 recovery 指 Chaosd recover 成功还是 broker 进程重新健康？建议改成"等 broker-1 健康检查返回 200 后再等 60 秒"，让 Agent 知道要先验活才开始计时。否则 60 秒可能有一半在等 broker 重启，留给自愈验证的时间不够，结果偏悲观。

还有一处留白：Circuit Breaker 那节说"pause 调度直到人工确认"，具体怎么 pause？是 Hermes 有 pause cron 的 API 还是要人去改 cron.yml？这里留白，下一篇配 Cron 时要补上。

### 6.8 把关小结：边际价值就两件事

把六个文件的点评收一收。

整体水平基本达到了"工程可用"——AI 执行了提示词里所有明确说过的约束，没有幻觉出不存在的接口，边界情况（config 缺失、二进制不存在、Jinja2 没装）也都有降级处理。这是好提示词的直接结果：<span style="color: red; font-weight: bold;">接口签名、硬约束、已有依赖三件事交代清楚，AI 才能写出跟方案对得上的代码。</span>

工程师把关的边际价值就体现在两件事上，每件事都对应着两类漏洞：

| 漏洞类型 | 点评里出现的例子 |
|---|---|
| 跨文件语义一致性 | chaos.py 的 recover 语义 vs SKILL.md Step 5 的等待逻辑 |
| 运营时的时间函数 | cluster.py 的 `time.sleep(5)`、report.py 的 git clone 策略 |

<span style="color: red; font-weight: bold;">把关不是重写，是用最小改动堵住这两类漏洞。</span>重写既不经济也不必要——AI 执行了提示词里所有明确说过的约束，没有幻觉出不存在的接口，边界情况也都有降级处理。把关要做的是 AI 看不见的那部分。

## 7. 收束：Check List、单点验证与思考

<img src="imgs/aicmigr-27-autotest-04-write-skill-suite/5b3281d1f88a770ff0d72c80b0e5d896_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

方法论在前面六章已经展开。这一章把它压成可裁剪的 Check List，再把单点验证、业务层缺口、思考题收一收。

### 7.1 开发 Check List

<span style="color: red; font-weight: bold;">三张表对应开发的三个阶段：写提示词前、排开发顺序时、把关 AI 代码时。每个阶段只问几条关键问题，够用就行。</span>

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

### 7.2 单点验证都通过

每个 Tool 都通过单点对话验证过——hermes 进交互，告诉 Agent 调具体 Tool，返回结果回来，这个 Tool 就活着。Skill 骨架加载到 Hermes 后，通过 `hermes /skills` 能看到 `robustmq-chaos-test` 出现在列表里，说明 Hermes 识别了它。

### 7.3 还差业务层

代码层齐了，但还差业务层——下面五件事留到下一篇：

| 业务层缺口 | 影响 |
|---|---|
| 场景描述（broker-kill 用什么 target、各档跑哪些场景） | Skill 没拿到具体场景，没法跑完整流程 |
| 报告 Markdown 模板 `report.md.j2` | report.py 现在调 Jinja2 加载会报模板找不到 |
| config.yml | SDK 矩阵、Chaosd 端点、报告仓库地址还都是空的 |
| GitHub Deploy Key | test-reports 仓库没创建，git push 真跑会失败 |
| cron.yml | 系统现在只能手动 CLI 触发，还不是 7×24 |

下一篇把这五件事做完，然后用一段对话把整个系统串起来跑一遍，让读者真的看到报告 push 进 GitHub 仓库。

### 7.4 小结

写代码是从方案文档到可运行代码的第三次翻译。<span style="color: red; font-weight: bold;">AI 时代，这一步从体力劳动变成质量把关，工程师的角色不是敲键盘，是写好提示词。</span>

好的开发提示词包含三件事：接口签名 ＋ 硬约束 ＋ 已有依赖。Tool 的提示词重在传约束，Skill 的提示词重在喂 Tool 接口。哪一件没传到位，AI 给的代码就会偏离方案文档，跑起来出问题。

开发顺序也有讲究：依赖在先、Tool 在先。cluster.py 第一个写，因为后面所有 Tool 依赖它给的 endpoint；Skill 最后写，因为它是 Tool 的使用说明书，说明书要等东西做出来再写。

跟着这个顺序把六个文件写完，代码层就齐了。下一篇填业务层，然后让 Hermes 真的跑起来，看见报告 push 进 GitHub 仓库。

### 7.5 思考

回想最近一次让 AI 帮忙写代码的经历。如果用这一篇的"接口签名 ＋ 硬约束 ＋ 已有依赖"三件事重新组织提示词，最容易漏掉的是哪一件？为什么？
