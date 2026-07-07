---
title: 传统项目迁AI 26：自动测试 - 跑通 Hermes Agent
author: fangkun119
date: 2026-07-05 06:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-26-autotest-03-hermes-agent-setup/cover.jpg
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
aicmigr-26-autotest-03-hermes-agent-setup
传统项目迁AI 26：自动测试 - 跑通 Hermes Agent
-->

## 1. 开篇导读

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/2fd8ac50fc4e24ebf9d3dc994318303d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是"自动测试"系列的第三篇。前一篇把一句话需求翻译成了完整设计文档和完整方案文档，手上拿着两份评审级文档。但纸面上的方案再清晰，在工具上跑不通就只是 PPT。本篇要做的事是把 Hermes 真正跑起来，验证那份方案里的每个扩展点都活着。

本篇的真正主角不是 Hermes 的功能盘点，而是**接管一个新工具第一天该怎么走**的方法论。整篇文档分为两部分：第一部分是方法论提炼（参考手册风，不深入技术栈），第二部分是结合 Hermes/RobustMQ 的实战演示（讲清 why）。下面这张导读地图帮助两类读者快速定位。

### 1.1 全文导读地图

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/f2d0b4163f46f47e2685ac55f194bfb8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart LR
    ROOT["本篇<br/>接管新工具第一天"] --\> P1["第一部分 · 方法论提炼"]
    ROOT --\> P2["第二部分 · 实战演示"]
    ROOT --\> P3["小结与思考"]

    P1 --\> P1A["2.1 核心命题：先跑通再读文档<br/>[两类]"]
    P1 --\> P1B["2.2 四步法总览<br/>[初学]"]
    P1 --\> P1C["2.3 第一步：装上体验<br/>[初学]"]
    P1 --\> P1D["2.4 第二步：写最简 Hello World<br/>[两类]"]
    P1 --\> P1E["2.5 第三步：把卡过的坑记下来<br/>[两类]"]
    P1 --\> P1F["2.6 第四步：对照真实项目<br/>[两类]"]
    P1 --\> P1G["2.7 项目阶段 Check List<br/>[熟练]"]

    P2 --\> P2A["3.1 装上 Hermes 跟它聊一聊<br/>[初学]"]
    P2 --\> P2B["3.2 写两个最简 Hello World<br/>[两类]"]
    P2 --\> P2C["3.3 把方案落到 Hermes 目录<br/>[两类]"]
    P2 --\> P2D["3.4 触发链路怎么走<br/>[熟练]"]
    P2 --\> P2E["3.5 第一行代码往哪里下<br/>[熟练]"]

    P3 --\> P3A["4.1 四步法口诀<br/>[两类]"]
    P3 --\> P3B["4.2 慢路与快路<br/>[两类]"]
    P3 --\> P3C["4.3 思考<br/>[两类]"]
-->

### 1.2 两类读者的建议阅读路径

| 读者类型 | 建议路径 |
|---|---|
| 初学 AI 编程工程师 | 1 → 2.1 → 2.2 → 2.3 → 2.4 → 3.1 → 3.2 → 3.3 → 4（系统学习全过程） |
| 熟练 AI 编程工程师 | 1 → 2.7（Check List）→ 3.3 → 3.5（直接看落点）→ 按需查阅 2.4 / 3.2 细节 |

## 2. 第一部分 · 方法论提炼：接管新工具的四步法

### 2.1 核心命题：先跑通，再读文档

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/f4e5c0c9bcc9bfb5a510fe895f20d36e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接到一个新工具，大多数人的第一反应是把官方文档从头读一遍。这是**慢路**。

#### (1) 慢路慢在哪

读完一整套文档，脑子里全是抽象概念，没一个动作落到键盘上。等真要动手，又得回头翻文档——读的时候没有具体问题，记不住。

#### (2) 快路的姿势

把顺序反过来：**先跑通最小闭环，然后带着具体问题去读对应章节**。跑通的过程会自然把人卡在一些点上，这些点就是最该读的地方，读起来效率比裸读高十倍。

#### (3) 这条路为什么有效

跑通最小闭环会产生两类高价值副产品：

##### ① 建立对工具的"手感"
知道工具怎么决定要不要调能力、调能力时会不会先确认、返回格式长什么样、慢不慢。这种手感是后续做任何扩展的基础。

##### ② 精准定位阅读点
卡过的每个点都是后续读文档的指路灯。撞坑的过程会精准告诉人下一步该读哪里，这种深度看文档读不出来。

### 2.2 四步法总览

接管一个新工具的第一天，按四步走：

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/e303eaf34fcc1ce574cc912e5742a2d5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart LR
    S1["第一步<br/>装上体验"] --\> S2["第二步<br/>写 Hello World"]
    S2 --\> S3["第三步<br/>跑通"]
    S3 --\> S4["第四步<br/>对照真实项目"]
    S4 --\> DONE["真正接管工具"]
-->

前三步是**工具掌握**，工程师在任何工具上都该这么做。第四步是**项目落地**，把通用的工具能力对接到具体的工程任务——这一步走完，才真正接管了这个工具。

### 2.3 第一步：装上体验，建立手感

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/b3168c21ad514d9ad45c1c747a1a12cb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 安装本身不是重点

装完能跑就行。如果中间踩坑（运行时版本、网络、依赖），花一两个小时解决就够了，不要为了把环境搞到完美而耗一整天。装到能跑的程度立刻进下一步，后面遇到问题再回来调。

#### (2) 装完后最重要的一步是先聊一聊

进入交互界面，扔几个真实指令试试。挑指令的原则是覆盖工具的几类典型能力：

##### ① 让工具解释自己
了解工具对外宣称的能力边界。

##### ② 让工具跑一个 shell 或外部动作
验证工具对真实环境的操作能力。

##### ③ 看工具怎么处理简单查询
观察工具的返回格式和决策方式。

#### (3) 第一次对话的真正目的

第一次对话的目的不是验证 LLM 能用——这件事 ChatGPT 早就证明过了。目的是验证**接下来要扩展的这个东西真的活着**，知道它的能力调用循环是怎么跑的。建立这个感觉之后，后面写扩展时才知道扩展点在工具那一侧是怎么被看到的。

#### (4) 顺手玩玩内置命令

花十分钟玩玩斜杠命令也值得：`/help` 看所有命令、`/tools` 看当前可用工具、`/model` 切模型。这些用一遍，大概知道工具给了哪些"开关"。

### 2.4 第二步：写最简 Hello World 验证最复杂机制

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/1be377a328279ec29b3d41751e67f2c5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 目标设定

这一步的目标不是做个有用的东西，是**用最简任务验证最复杂机制**。

#### (2) 双层扩展机制

成熟 AI Agent 工具的扩展机制通常有两层，两层都得验证：

| 层 | 形态 | 验证载体 |
|---|---|---|
| 能力层（Tool） | 代码写的能力 | 最简 Tool |
| 指令层（Skill） | 文本写的指令 | 最简 Skill |

两件事都跑通，就掌握了工具的全部扩展能力。剩下的差异只是：Tool 的逻辑更复杂、Skill 的指令更精细，**机制是一样的**。

#### (3) 文件放哪要先说清

扩展文件默认从用户目录加载（工具自动创建，不存在就 `mkdir -p` 自己建一下）。这里有一条重要的边界原则：

##### ① 用户目录是用户的地盘
所有自己写的扩展都放这里。

##### ② 工具仓库源码目录是官方自带的
不要把自己的代码放那里。原因有二：

- 会跟着工具升级被覆盖。
- 污染源码目录将来很难清理。

### 2.5 第三步：跑通之后，把卡过的坑记下来

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/025d852ecf715e3c95719dd37bad0b0c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

完美跑通是一种幸运，**卡几次才是常态**。把坑记下来比读十遍文档更有价值。

#### (1) 三类典型坑

##### ① 扩展没注册上
工具启动后看不到新加的能力，大概率是文件没被扫到。

##### ② 扩展没加载到
工具收到指令后没按扩展里的流程走，而是自己临场发挥。

##### ③ 调用扩展但参数错了
大概率是扩展的描述写得不清楚，或者参数 schema 里没把约束写明白。

#### (2) 通用排错三步

| 步骤 | 检查项 |
|---|---|
| 1. 文件位置 | 文件名拼写是否对、文件是否真的在用户目录下、工具是否需要重启加载新扩展 |
| 2. 目录结构 | 目录层级是否符合工具规定（如套件是否有专用子目录） |
| 3. 描述清晰度 | frontmatter 字段是否写对、description 是否清晰到工具知道何时加载 |

#### (3) 关键认知：扩展的描述是给工具看的

Tool 的 description 是给 LLM 看的，不是给人看的。写得越清楚，LLM 调得越准。这一条认知撞过一次坑才能内化。

### 2.6 第四步：对照真实项目，想清楚怎么放

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/2192c773bb7403300c2ac64538a3cdd2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

练 Hello World 的目的不是好玩。目的是**跑完之后立刻能回头看真实项目方案，知道每个文件该往工具用户目录的哪个位置放、第一行代码该往哪里下**。

#### (1) 这一步走完才算真正接管

前三步是通用工具掌握，第四步是项目落地。把通用的工具能力对接到具体的工程任务，这一步走完，才真正接管了这个工具。

#### (2) 对照动作的核心

把项目方案里的每条决策，逐条对应到工具用户目录的某个位置：

- 决策"几个能力函数放在一个套件下" → 用户目录下的某个套件目录。
- 决策"调度规则分档" → 用户目录下的全局配置文件。
- 决策"凭据怎么管" → 用户目录外的独立位置（如 SSH 目录）。

#### (3) 对照之后会有的踏实感

跑完 Hello World 之后再回头看项目方案文档，会有一种"这件事能干"的踏实感。不是因为方案变简单了，是因为**对工具的理解更进一步了**。

### 2.7 项目阶段 Check List（可裁剪）

下表把"接管新工具第一天"的整套动作做成可裁剪的 Check List，供项目阶段快速查阅。每行带一个反问点，落到具体可判断的标准上。

| 项目阶段 | 关键动作 | 产物 | 反问点 |
|---|---|---|---|
| 接到新工具第一天 | 不裸读官方文档；先跑通最小闭环 | 最小可运行实例 | 是否已经动手敲出了第一个能跑的命令？ |
| 装上工具 | 装到能跑就停手，不为完美环境耗一天 | 可启动的工具进程 | 装到能跑的程度是否立刻进下一步？ |
| 第一次对话 | 扔三类典型指令（解释自己 / 跑动作 / 简单查询） | 对工具手感的初印象 | 是否建立了"工具怎么决定调能力"的感觉？ |
| 体验斜杠命令 | `/help`、`/tools`、`/model` 各过一遍 | 工具开关清单 | 知道工具给了哪些"开关"吗？ |
| 写最简扩展（能力层） | 最简 Tool，20 行内 | 一个能被工具调用的扩展 | 工具是否真的扫描到并加载了？ |
| 写最简扩展（指令层） | 最简 Skill，20 行内 | 一个能被工具按流程执行的指令 | 工具是否按 Skill 里的流程走？ |
| 跑通 | 两层扩展都通过验证 | 验证记录 | 双层机制都跑通了吗？ |
| 卡坑记录 | 把卡过的坑逐条记下来 | 排错清单 | 三类典型坑都内化了吗？ |
| 对照真实项目 | 把项目方案逐条对应到工具用户目录 | 文件落点对照表 | 每条方案决策都有物理落点吗？ |
| 第一行代码定位 | 找到依赖最少的那个扩展先写 | 代码起点 | 哪个扩展是后续所有扩展的前提？ |

## 3. 第二部分 · 实战演示：把 Hermes Agent 跑通

### 3.1 装上 Hermes，跟它聊一聊

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/39ec439ae280859187644e8e74b6c1a3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 一键安装

Hermes 中文社区把命令行版的一键安装脚本写得很清楚：

```bash
curl -fsSL https://res1.hermesagent.org.cn/install.sh | bash
```

完整的安装、模型配置、网络问题处理，直接看 hermesagent.org.cn 的快速入门，文档比本篇能讲得详细。

#### (2) 进入交互界面

```text
$ hermes
```

进入交互界面后，扔几个真实指令试试：

```text
> 你能帮我做什么?
> 帮我看看磁盘空间占用，列出最大的 5 个目录
> 现在几点了
```

第一条让 Hermes 解释自己，第二条让它跑 shell，第三条看简单查询它怎么处理。这三个指令五分钟就能跑完，但会迅速建立对这个工具的"手感"：

- 它怎么决定要不要调工具
- 调工具的时候它会不会先确认
- 返回的格式长什么样
- 慢不慢

#### (3) 第一次对话的目的

第一次对话的目的不是验证 LLM 能用，而是验证接下来要扩展的这个东西真的活着——知道它的工具调用循环是怎么跑的。建立这个感觉之后，后面写 Tool 时才知道 Tool 在 Agent 那一侧是怎么被看到的。

#### (4) 体验斜杠命令

花十分钟玩玩斜杠命令也值得：`/help` 看所有命令、`/tools` 看当前可用工具、`/model` 切模型。这些用一遍，大概知道 Hermes 给了哪些"开关"。

### 3.2 写两个最简 Hello World：Tool 与 Skill

体验完该动手了。Hermes 的扩展机制有两层：Tool 是 Python 写的能力，Skill 是 Markdown 写的指令。两层都得验证，所以 Hello World 也分两件事。

#### (1) 文件放哪

Hermes 默认从两个地方加载扩展：

| 类型 | 加载目录 |
|---|---|
| Tool | `~/.hermes/tools/` |
| Skill | `~/.hermes/skills/` |

装完 Hermes 自动创建，不存在就 `mkdir -p` 自己建一下。Hermes 仓库源码里也有 `tools/` 和 `skills/` 目录，那是官方自带的工具和技能，**不要把自己的代码放那里**——会跟着 `hermes update` 被覆盖，而且污染源码目录将来很难清理。用户目录是用户的地盘，放自己的扩展。

#### (2) 一个最简 Tool：get_current_time

在 `~/.hermes/tools/` 下新建 `time_tool.py`（这是 Tool 平铺的最简形态，正式项目里多个相关 Tool 会放在 Skill 套件下，第三部分第 3.3 节讲清楚）：

```python
import json
from datetime import datetime, timezone
from tools.registry import registry

def get_current_time(args: dict, **_) -> str:
    now = datetime.now(timezone.utc)
    return json.dumps({
        "utc": now.isoformat(),
        "unix": int(now.timestamp())
    })

registry.register(
    name="get_current_time",
    description="Return the current UTC time.",
    handler=get_current_time,
    parameters={
        "type": "object",
        "properties": {},
    },
)
```

20 行 Python，三件事：定义函数、写注册参数、调 `registry.register`。Hermes 启动时会扫描 `~/.hermes/tools/` 目录，自动把这个 Tool 加进 Agent 可调用列表。

写完跑一次：

```text
$ hermes
> 现在几点?
```

如果一切正常，会看到 Agent 调用了 `get_current_time`，返回 UTC 时间。Tool 注册机制就这么简单。

#### (3) 一个最简 Skill：hello-world

在 `~/.hermes/skills/hello-world/` 目录下新建 `SKILL.md`，内容如下：

```
name: hello-world
description: "Say hello and tell me the current time."
version: 1.0.0

# Hello World
## When to Use
当用户说"用 hello-world skill 跟我打招呼"或类似指令时加载。

## Procedure
1. 调用 `get_current_time` Tool 获取当前时间。
2. 用一句话回复用户:"你好!现在是 {utc 时间}，祝你开发顺利。"
```

三段加起来不到 20 行 Markdown，这就是一个最简 Skill 的全部内容。

跑一次：

```text
$ hermes
> 用 hello-world skill 跟我打招呼
```

Agent 加载 Skill，按 Procedure 调 `get_current_time`，然后用 Skill 里规定的格式回复。

#### (4) 两件事都跑通意味着什么

两件事都跑通，就已经掌握了 Hermes 的全部扩展能力。剩下的差异只是：Tool 的逻辑更复杂、Skill 的指令更精细。机制是一样的。

#### (5) 跑通之后把卡过的坑也记下来

完美跑通是一种幸运，卡几次才是常态。下面三类坑卡过一次，对 Hermes 的理解就深一层。

##### ① Tool 没注册上
Hermes 启动后看不到 `get_current_time`，大概率是文件没被扫到。检查文件名拼写、检查文件是否真的在 `~/.hermes/tools/` 目录下、检查 Hermes 是否需要重启加载新 Tool。

##### ② Skill 没加载到
Agent 收到指令后没按 Skill 里的 Procedure 走，而是自己临场发挥。检查目录结构是不是 `~/.hermes/skills/hello-world/SKILL.md`（不是 `~/.hermes/skills/hello-world.md`）、检查 frontmatter 的 `name` 字段是不是写对、检查 `description` 是否清晰到 Agent 知道何时加载。

##### ③ Agent 调 Tool 但参数错了
大概率是 Tool 的 `description` 写得不清楚，或者 `parameters` schema 里没把参数约束写明白。Tool 的 `description` 是给 LLM 看的，不是给人看的，写得越清楚 LLM 调得越准。

这种深度看文档读不出来，只能自己撞出来。这就是"先跑通再读文档"的价值：撞坑的过程会精准地告诉人下一步该读哪里。

### 3.3 把 25 篇的方案落到 Hermes 用户目录

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/126cece82da5c15793f9cffccc90507b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这是本篇真正的终点。回顾 25 篇第二次翻译拍过的几个关键决策：

- 7 个 Tool 函数全部放在一个 Skill 套件下（共享上下文）。
- 集群直接 spawn 进程不引入 Docker，SDK 隔离用本地版本管理。
- 报告 push 到 GitHub 公开仓库（Deploy Key）。
- 第一期通道仅 CLI。

这些决策直接决定了要往 Hermes 用户目录的哪些位置放哪些文件。

#### (1) 文件分布全景

把 25 篇的方案落到 Hermes 上，文件分布是这样：

<!-- 
图片内容说明
路径：imgs/26_自动测试_03：跑通 Hermes Agent/9638e36c8ea63a0418f0d61ce4cce8b6_MD5.jpg
用途：展示 Hermes 用户目录下各文件的物理分布
内容：把项目方案里的三类内容（Skill 套件本体 / Hermes 全局配置 / 外部凭据和仓库）映射到 ~/.hermes 下的具体位置，对应 25 篇拍过的几条关键决策
-->

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/9638e36c8ea63a0418f0d61ce4cce8b6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

整张表里有三类内容。

#### (2) Skill 套件本体

`~/.hermes/skills/robustmq-chaos-test/` 下面的全部：核心代码、场景描述、报告模板全在这里。

这是 25 篇拍过的"1 个套件 + 多个 Tool"决策的物理体现——所有相关 Tool 共享同一个上下文，Agent 串联调用最自然。Skill 套件目录跟 Hello World 那个 `~/.hermes/skills/hello-world/` 是同级关系，只是套件内部多了 `tools/`、`scenarios/`、`templates/` 几个子目录。

#### (3) Hermes 全局配置

`~/.hermes/cron.yml` 和 `~/.hermes/config.yaml`：

| 文件 | 内容 |
|---|---|
| `cron.yml` | P0/P1/P2 三档调度规则 |
| `config.yaml` | approvals 模式 + command_allowlist |

这两份是 7×24 无人值守的安全护栏，跟 Skill 套件是松耦合的：Skill 不知道自己被谁触发的，只管按 Procedure 走。

#### (4) 外部凭据和仓库

`~/.ssh/test-reports-deploy` 和 GitHub `test-reports` 仓库：

- Deploy Key 私钥放在 `~/.ssh`，只授写权限到单一仓库。
- Skill 里通过 `GIT_SSH_COMMAND` 环境变量指定使用，不进 Hermes 的 `.env`，也不进版本库。

这是 25 篇反问那一轮收紧的硬规则。

#### (5) 看清楚两件事：机制是一致的

##### ① 5 个 .py 文件的 Tool 注册机制跟 get_current_time 是同一个机制
注册方式一样、扫描机制一样、Agent 调用方式一样。区别只是 Tool 内部要做的事更复杂：

| Tool | 要做的事 |
|---|---|
| `cluster.py` | spawn 多个 RobustMQ 进程并管理生命周期 |
| `chaos.py` | 调 Chaosd HTTP API 注入故障 |
| `report.py` | 程序化渲染 Markdown 加 git push |

注册到 Hermes 的方式是同一个 `registry.register` 调用。

##### ② robustmq-chaos-test Skill 跟 hello-world Skill 也是同一个机制
`SKILL.md` frontmatter 一样、Procedure 写法一样、Hermes 加载机制一样。区别只是 Procedure 写得更长——前置检查、单场景执行五步、通过失败判断、熔断逻辑，以及套件下多了 `tools/` 这层子目录。Hermes 加载 Skill 时会把 `tools/` 下的 Python 文件作为 Skill 私有的 Tool 注册进来。

#### (6) 跑完之后的踏实感

跑完 Hello World 之后再看 25 篇的方案文档，会有一种"这件事能干"的踏实感。不是因为方案变简单了，是因为对工具的理解更进一步了。

### 3.4 触发链路在项目里怎么走

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/deb6c3c96de45b23425d35b4ad099c95_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把触发链路再走一遍，会发现这条路在 Hello World 时已经走过了。

#### (1) Cron 触发

`cron.yml` 写 P0/P1/P2 三档调度，每档触发的 prompt 是自然语言（比如 P0 是"按 P0 跑一轮 MQTT 基础场景"）。Agent 收到 prompt 加载 `robustmq-chaos-test` Skill 后，按 Procedure 调 Tool。

这套流程跟刚才用 `hermes "用 hello-world skill 跟我打招呼"` 没本质区别，只是触发源从手动命令换成了 cron。

#### (2) 手动触发

第一期就 CLI，工程师在终端跑：

```text
hermes "按 P1 跑一轮 mq9 故障场景"
```

这条路刚才已经走过一次了——Hello World 那段就是手动触发。25 篇拍过决策"第一期通道仅 CLI"，飞书等到第二期再做，先把核心闭环跑稳。

#### (3) 报告归档

`report.py` 跑完用 Deploy Key 把 JSON + Markdown 双格式报告 `git push` 到 GitHub `test-reports` 仓库，Quality Dashboard 是静态页面，直接从 GitHub 仓库读取展示。

这一步是要新写的，但 `git push` 是系统命令，不需要 Hermes 给特殊支持，只要在 Skill 的 allowlist 里加上 `git push` 即可。

#### (4) 整个项目要做的全部新事情

整个项目里要做的全部新事情，都建立在 Hello World 验证过的两个机制上：**Tool 注册 + Skill 加载**。其余的 Cron、AI Agent 调用循环、command_allowlist 安全沙箱，Hermes 全部白嫖。

| 能力 | 来源 |
|---|---|
| Tool 注册 / Skill 加载 | Hello World 已验证 |
| Cron 调度 | Hermes 自带 |
| AI Agent 调用循环 | Hermes 自带 |
| command_allowlist 安全沙箱 | Hermes 自带 |

### 3.5 第一行代码该往哪里下

#### (1) Tool 依赖顺序

回到 25 篇实施步骤的 Tool 依赖顺序：

```text
cluster → observability → run_client → inject_fault → push_report
```

#### (2) 第一个要写的是 cluster.py

理由是后面所有 Tool 的动作都假设集群在跑：

- `chaos.py` 要在集群上注入故障
- `client.py` 要连集群跑 SDK 测试
- `observability.py` 要从集群收集日志和 metrics

集群起停先做完，后面才有东西可操作。

#### (3) Hello World 已经把"工具准备好了"确认过

下一篇就开始写 `cluster.py`。但在写之前，本篇的 Hello World 已经把"工具准备好了"这件事确认过了。下一篇一上来不会有任何关于"Hermes 能不能扩展"的疑问，所有精力都用在集群启停的业务逻辑怎么实现上。

这就是练 Hello World 的真正价值。

## 4. 小结与思考

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/45929cb4bbd40df50933c8899d2e9b38_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 4.1 四步法口诀

接管一个新工具的第一天，四步走：

```text
装上体验 → 写 Hello World → 跑通 → 对照真实项目想清楚怎么放
```

前三步是工具掌握，工程师在任何工具上都该这么做。第四步是项目落地，把通用的工具能力对接到具体的工程任务，这一步走完，才真正接管了这个工具。

### 4.2 慢路与快路

很多人第一天卡在前两步——把环境装得完美、文档读得透彻，但没动手写过任何扩展，这是慢路。

先跑通最小闭环，再带着具体问题读对应章节——这条路上每一步都有反馈，每一次卡壳都是下一步的指路灯。

下一篇，开始写下 `cluster.py` 的第一行代码。

### 4.3 思考

回想最近接手的一个开源框架或库，如果用本篇的"四步法"重做一遍接管过程，会在哪一步发现自己当时其实是跳过的？这一步跳过让后面付出了什么代价？
