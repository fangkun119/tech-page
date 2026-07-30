---
title: 传统项目迁AI 26：自动测试 - 跑通 Hermes Agent
author: fangkun119
date: 2026-07-05 06:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
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

## 1. 开篇：拿到新工具的第一天该怎么办

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/c8bca12ae326179c9edd59f144fd4fed_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

工程师拿到一个新 AI Agent 工具 —— 不管是 Hermes、Claude Code 还是 Cursor —— 第一反应往往是把官方文档从头读一遍。<span style="color: red; font-weight: bold;">这条路看似稳妥，却是效率最低的入门方式</span>。

本篇给出一条更快的路径：先跑通最小闭环，再带着具体问题回头读文档。

### 1.1 一个常见误区：先啃文档

为什么"先读文档"是慢路？<span style="color: red; font-weight: bold;">读完一整套文档，脑子里全是抽象概念，没一个动作落到键盘上</span>；等真要动手，又得回头翻文档，因为读的时候没有具体问题，记不住。

这条慢路传统工程师并不陌生 —— 很少有人会先把 Spring 全套源码读完，再写第一个 Controller。概念脱离了动手场景，注定留不下印记。

### 1.2 破解法：先跑通最小闭环，再带着问题读文档

把顺序反过来：<span style="color: red; font-weight: bold;">先跑通最小闭环，然后带着具体问题去读对应章节</span>。跑通的过程会自然把人卡在一些点上，这些点就是最该读的地方，读起来效率比裸读高十倍。

这其实就是传统软件工程师早已熟知的路径 —— <span style="color: red; font-weight: bold;">先写一个 hello world 跑起来，再回头补业务逻辑</span>。把这条路径平移到 AI Agent 工具上即可。

本篇用 Hermes 实战演示这条快路：从一键安装开始，写两个最简 Hello World（一个 Tool、一个 Skill；因为要给 Hermes 添加 Tool 和 Skill 所以就选择这两个），跑通之后立刻对照真实项目方案，把每条决策落到 Hermes 用户目录的物理位置上。走完这一遍，"接管一个新 AI Agent 工具"就不再是一个抽象命题。

## 2. 第一步：装上 Hermes 并建立手感

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/c91237dd4524f5a758994e5cb83fa227_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一章把 Hermes 装上、扔几条指令、记下坑、再对照真实项目的落点。<span style="color: red; font-weight: bold;">四步：装上体验、写 Hello World、把坑记下来、对照真实项目落点。</span>

### 2.1 一键安装

Hermes 中文社区提供了命令行版的一键安装脚本：

```bash
curl -fsSL https://res1.hermesagent.org.cn/install.sh | bash
```

完整的安装、模型配置、网络问题处理，直接看 hermesagent.org.cn 的快速入门文档，比本篇讲得细。

安装本身不是重点 —— <span style="color: red; font-weight: bold;">装到能跑就停手</span>。中间踩坑（运行时版本、网络、依赖）花一两个小时解决就够了，别为把环境搞到完美耗上一整天，后面遇到问题再回来调。

### 2.2 第一次对话：扔三类典型指令

```text
$ hermes
```

进入交互界面后，扔几个真实指令试试：

```text
> 你能帮我做什么?
> 帮我看看磁盘空间占用，列出最大的 5 个目录
> 现在几点了
```

挑指令的原则是覆盖工具的几类典型能力：

- <span style="color: red; font-weight: bold;">第一条让 Hermes 解释自己（了解能力边界）</span>
- <span style="color: red; font-weight: bold;">第二条让它跑 shell（验证对真实环境的操作能力）</span>
- <span style="color: red; font-weight: bold;">第三条看简单查询它怎么处理（观察返回格式和决策方式）</span>

这三条指令五分钟就能跑完，但能迅速建立对这个工具的"手感"：

| 观察维度 | 要建立的感觉            |
| ---- | ----------------- |
| 能力决策 | 它**怎么决定要不要调工具**   |
| 确认机制 | 调工具的时候它**会不会先确认** |
| 返回格式 | **返回结果长什么样**      |
| 响应速度 | 慢不慢               |

第一次对话的目的不是验证 LLM 能用 —— 这件事 ChatGPT 早就证明过了。目的是验证**接下来要扩展的这个东西真的活着**，看清它的工具调用循环是怎么跑的。这个感觉建立起来，后面写扩展时才知道扩展点在工具那一侧是怎么被看到的。

### 2.3 顺手玩玩斜杠命令

花十分钟玩玩斜杠命令也值得：

- `/help` 看所有命令
- `/tools` 看当前可用工具
- `/model` 切模型

用一遍，大概就知道 Hermes 给了哪些"开关"。

## 3. 第二步：写两个 Hello World 验证双层扩展

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/37f8b46a06c3bc3bd4e0f87a9a505383_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

体验完该动手了。Hermes 的扩展机制有两层，这一章就做两件事：分别写一个最简 Tool 和一个最简 Skill，把两层机制都跑通。

### 3.1 双层扩展机制：Tool 与 Skill

成熟 AI Agent 工具的扩展机制通常有两层，这两层构成了工具的全部扩展能力：<span style="color: red; font-weight: bold;">Hermes 的扩展机制有两层：Tool 是 Python 写的能力，Skill 是 Markdown 写的指令。</span>两层都得验证，所以 Hello World 也分两件事。

| 层          | 形态     | 验证载体     | 传统工程类比                     |
| ---------- | ------ | -------- | -------------------------- |
| 能力层（Tool）  | 代码写的能力 | 最简 Tool  | Controller / Service（代码逻辑） |
| 指令层（Skill） | 文本写的指令 | 最简 Skill | 配置文件 / 流程编排                |

类比传统 Web 工程：<span style="color: red; font-weight: bold;">Tool 相当于 Controller/Service，是真正的代码逻辑，决定"能做什么"；Skill 相当于配置文件和流程编排，是一段文本指令，决定"什么时候做、怎么做"</span>。理解了这个类比，后面写扩展时就知道该往哪层落。

### 3.2 文件放哪：用户目录是用户的地盘

Hermes 默认从两个地方加载扩展：

| 类型 | 加载目录 |
|---|---|
| Tool | `~/.hermes/tools/` |
| Skill | `~/.hermes/skills/` |

装完 Hermes 自动创建，不存在就 `mkdir -p` 自己建一下。

这里有一条重要的边界原则：

| 目录 | 归属 | 为什么 |
|---|---|---|
| `~/.hermes/` 用户目录 | 用户的地盘，放自己写的扩展 | 自己的扩展自己管 |
| Hermes 仓库源码里的 `tools/`、`skills/` | 官方自带的工具和技能 | 会跟着 `hermes update` 被覆盖；污染源码目录将来很难清理 |

<span style="color: red; font-weight: bold;">自己的扩展只往用户目录放，不要碰仓库源码</span> —— 这条边界守住了，后面升级和清理都不会有坑。

### 3.3 最简 Tool：get_current_time

在 `~/.hermes/tools/` 下新建 `time_tool.py`（这是 Tool 平铺的最简形态，正式项目里多个相关 Tool 会放在 Skill 套件下，后面的章节会讲清楚）：

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

### 3.4 最简 Skill：hello-world

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

### 3.5 跑通意味着什么

<span style="color: red; font-weight: bold;">两件事都跑通，就已经掌握了 Hermes 的全部扩展能力</span>。剩下的差异只是：Tool 的逻辑更复杂、Skill 的指令更精细。**机制是一样的** —— 后面把真实项目的 7 个 Tool 落到 Hermes 上时，用的就是这两个机制。

Hello World 的价值不在那 20 行代码本身，而在于它一次性验证了全部扩展机制。跑完之后再看真实项目的方案，剩下的就只是业务逻辑怎么写，不会再有"Hermes 能不能扩展"这种工具层面的疑问。

## 4. 第三步：把卡过的坑记下来

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/fa98ba04dcbd79a62e7c6720f92c6320_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

完美跑通是一种幸运，<span style="color: red; font-weight: bold;">卡几次才是常态</span>。把坑记下来比读十遍文档更有价值 —— 下面三类坑每卡过一次，对 Hermes 的理解就深一层。

### 4.1 三类典型坑

| 坑 | 现象 | 大概率原因 |
|---|---|---|
| Tool 没注册上 | Hermes 启动后看不到 `get_current_time` | 文件没被扫到 |
| Skill 没加载到 | Agent 收到指令后没按 Procedure 走，而是自己临场发挥 | 目录结构或 frontmatter 写错 |
| Agent 调 Tool 但参数错了 | Tool 被调了但参数不对 | description 写得不清楚，或 parameters schema 没把约束写明白 |

### 4.2 通用排错三步

| 步骤 | 检查项 |
|---|---|
| 1. 文件位置 | 文件名拼写是否对、文件是否真的在用户目录下、Hermes 是否需要重启加载新扩展 |
| 2. 目录结构 | 目录层级是否符合规定（如 Skill 是否有专用子目录 `~/.hermes/skills/hello-world/SKILL.md`，不是 `~/.hermes/skills/hello-world.md`） |
| 3. 描述清晰度 | frontmatter 字段是否写对、description 是否清晰到 Agent 知道何时加载 |

### 4.3 关键认知：description 是给 LLM 看的

上面第三类坑背后藏着一条反直觉认知：<span style="color: red; font-weight: bold;">Tool 的 description 是给 LLM 看的，不是给人看的</span>。

写文档时，人习惯用"够用就行"的简短注释；写 Tool 的 description 时这套习惯会直接埋坑 —— 。description 越清晰，LLM 越知道何时调、参数怎么传，调用就越准。

这一条认知撞过一次坑才能内化。<span style="color: red; font-weight: bold;">这就是"先跑通再读文档"的真正价值：撞坑的过程会精准地告诉人下一步该读哪里，比通读文档高效得多</span>。

## 5. 第四步：把项目方案落到 Hermes 用户目录

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/5ff1be746680a7ef7cd171d09c370a11_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/9229a5c607f4ccf4091e54d0cd88dd00_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面三步走的是"工具掌握"——装上、写 Hello World、跑通。第四步是**项目落地**：把真实项目方案拍过的决策，对应到 Hermes 用户目录的哪些位置放哪些文件。这是本篇真正的终点。

项目方案拍过的几个关键决策：

- 7 个 Tool 函数全部放在一个 Skill 套件下（共享上下文）
- 集群直接 spawn 进程不引入 Docker，SDK 隔离用本地版本管理
- 报告 push 到 GitHub 公开仓库（Deploy Key）
- 第一期通道仅 CLI

这些决策直接决定了文件怎么放。

### 5.1 文件分布全景

把项目方案落到 Hermes 上，文件分布是这样：
<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/file-structure-table.svg" style="display: block; width: 800px;" alt="替换文字">
下面逐类拆解。

### 5.2 Skill 套件本体

`~/.hermes/skills/robustmq-chaos-test/` 下面的全部内容：核心代码、场景描述、报告模板都在这里。

这是"1 个套件 + 多个 Tool"决策的物理体现 —— 所有相关 Tool 共享同一个上下文，Agent 串联调用最自然。这个目录跟 Hello World 那个 `~/.hermes/skills/hello-world/` 是**同级关系**，只是套件内部多了 `tools/`、`scenarios/`、`templates/` 几个子目录。

### 5.3 Hermes 全局配置

`~/.hermes/cron.yml` 和 `~/.hermes/config.yaml`：

| 文件 | 内容 |
|---|---|
| `cron.yml` | P0/P1/P2 三档调度规则 |
| `config.yaml` | approvals 模式 + command_allowlist |

这两份是 7×24 无人值守的**安全护栏**，跟 Skill 套件是松耦合的：Skill 不知道自己被谁触发，只管按 Procedure 走。

### 5.4 外部凭据和仓库

`~/.ssh/test-reports-deploy` 和 GitHub `test-reports` 仓库：

- Deploy Key 私钥放在 `~/.ssh`，只授写权限到单一仓库
- Skill 里通过 `GIT_SSH_COMMAND` 环境变量指定使用，不进 Hermes 的 `.env`，也不进版本库

这是反问那一轮收紧的硬规则——**凭据不进版本库，是底线**。

### 5.5 机制一致性：Hello World 已经走过这条路

看清楚两件事，真实项目的落点就不神秘了。

第一件事：<span style="color: red; font-weight: bold;">5 个 `.py` 文件的 Tool 注册机制跟 `get_current_time` 是同一个机制——注册方式一样、扫描机制一样、Agent 调用方式一样</span>。区别只是 Tool 内部要做的事更复杂：

| Tool | 要做的事 |
|---|---|
| `cluster.py` | spawn 多个 RobustMQ 进程并管理生命周期 |
| `chaos.py` | 调 Chaosd HTTP API 注入故障 |
| `report.py` | 程序化渲染 Markdown 加 git push |

注册到 Hermes 的方式都是<span style="color: red; font-weight: bold;">同一个 `registry.register` 调用</span>。

第二件事：<span style="color: red; font-weight: bold;">`robustmq-chaos-test` Skill 跟 `hello-world` Skill 也是同一个机制</span>。`SKILL.md` frontmatter 一样、Procedure 写法一样、Hermes 加载机制一样。区别只是 Procedure 写得更长——前置检查、单场景执行五步、通过失败判断、熔断逻辑，以及套件下多了 `tools/` 这层子目录。Hermes 加载 Skill 时会把 `tools/` 下的 Python 文件作为 Skill 私有的 Tool 注册进来。

### 5.6 触发链路在项目里怎么走

把项目的触发链路再走一遍，会发现这条路在 Hello World 时已经走过了。

#### (1) Cron 触发

`cron.yml` 写 P0/P1/P2 三档调度，每档触发的 prompt 是自然语言（比如 P0 是"按 P0 跑一轮 MQTT 基础场景"）。Agent 收到 prompt 后加载 `robustmq-chaos-test` Skill，按 Procedure 调 Tool。

<span style="color: red; font-weight: bold;">这套流程跟刚才用 `hermes "用 hello-world skill 跟我打招呼"` 没本质区别，只是触发源从手动命令换成了 cron。</span>

#### (2) 手动触发

第一期就 CLI，工程师在终端跑：

```text
hermes "按 P1 跑一轮 mq9 故障场景"
```

这条路刚才已经走过一次了——Hello World 那段就是手动触发。项目决策"第一期通道仅 CLI"，飞书等到第二期再做，先把核心闭环跑稳。

#### (3) 报告归档

`report.py` 跑完用 Deploy Key 把 JSON + Markdown 双格式报告 `git push` 到 GitHub `test-reports` 仓库，Quality Dashboard 是静态页面，直接从 GitHub 仓库读取展示。

这一步是要新写的，但 `git push` 是系统命令，不需要 Hermes 给特殊支持，只要在 Skill 的 allowlist 里加上 `git push` 即可。

#### (4) 项目要做的"新事情"其实很少

整个项目里要做的全部新事情，<span style="color: red; font-weight: bold;">都建立在 Hello World 验证过的两个机制上：Tool 注册 + Skill 加载</span>。<span style="color: red; font-weight: bold;">其余的 Cron、AI Agent 调用循环、command_allowlist 安全沙箱，Hermes 全部白嫖</span>。

| 能力 | 来源 |
|---|---|
| Tool 注册 / Skill 加载 | Hello World 已验证 |
| Cron 调度 | Hermes 自带 |
| AI Agent 调用循环 | Hermes 自带 |
| command_allowlist 安全沙箱 | Hermes 自带 |

### 5.7 第一行代码该往哪里下

落地路径清楚了，下一个问题自然就来了：第一行代码写哪个文件？

#### (1) Tool 依赖顺序

项目方案的 Tool 依赖顺序是这样的：

```text
cluster → observability → run_client → inject_fault → push_report
```

#### (2) 第一个要写的是 cluster.py

理由很简单：后面所有 Tool 的动作都假设集群在跑。

- `chaos.py` 要在集群上注入故障
- `client.py` 要连集群跑 SDK 测试
- `observability.py` 要从集群收集日志和 metrics

集群起停先做完，后面才有东西可操作。

#### (3) Hello World 已经把"工具准备好了"确认过

写 `cluster.py` 之前，本篇的 Hello World 已经把"工具准备好了"这件事确认过了。开始写业务逻辑时，不会有任何"Hermes 能不能扩展"的疑问，所有精力都用在集群启停的业务逻辑怎么实现上。

跑完 Hello World 再看项目方案文档，会有一种"这件事能干"的踏实感。不是因为方案变简单了，是因为**对工具的理解更进一步了**——这就是练 Hello World 的真正价值。

## 6. 方法论沉淀：接管新工具的四步法

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/55e52b0ac108c0cc83b5606d1312ad20_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第 2~5 章用 Hermes 真刀真枪走完了完整流程：装上工具、写 Hello World、踩坑、把项目方案落到用户目录。把这一遍里反复出现的动作抽出来，就是一套可以迁移到任何 AI Agent 工具的方法论。

### 6.1 为什么"先跑通"比"先读文档"高效

第 1 章已经讲过"先跑通 vs 先读文档"的取舍，这里不再重复。这一节回答的是更深一层的问题：<span style="color: red; font-weight: bold;">为什么"先跑通最小闭环"这条路会这么高效？</span>

答案藏在跑通过程中自然产生的两类副产品里。这两类副产品，靠裸读文档读不出来：

| 副产品 | 内容 |
|---|---|
| 对工具的"手感" | 知道工具怎么决定要不要调能力、调能力时会不会先确认、返回格式长什么样、慢不慢 |
| 精准定位阅读点 | 卡过的每个点都是后续读文档的指路灯，这种深度看文档读不出来 |

第一类副产品是"手感"。手感是肌肉记忆，必须动手才能建立。读十遍"Hermes 会先确认再调 Tool"，不如自己撞一次"它居然先问我要不要执行"——这一撞，就记住了。

第二类副产品更值钱：**精准定位阅读点**。文档是按作者思路组织的，但读者带着的具体问题往往和作者思路对不上。跑通最小闭环会把人卡在一些点上，这些点就是最该读的地方。带着这些点回头读对应章节，效率比从头裸读高十倍。

### 6.2 四步法总览

接管一个新工具的第一天，按四步走：

<img src="imgs/aicmigr-26-autotest-03-hermes-agent-setup/four-steps-flowchart.svg" style="display: block; width: 800px;" alt="四步法流程图">
<!-- 
图片内容说明
路径：imgs/aicmigr-26-autotest-03-hermes-agent-setup/four-steps-flowchart.svg
用途：展示接管新工具四步法的整体流程
内容：横向流程图，从左到右四个步骤依次为"装上体验/写Hello World/跑通/对照真实项目"，四步走完后到达终点"真正接管工具"；前三步属于工具掌握，第四步属于项目落地。
对应的mermaid如下：
flowchart LR
    S1["第一步<br/>装上体验"] --\> S2["第二步<br/>写HelloWorld"]
    S2 --\> S3["第三步<br/>跑通"]
    S3 --\> S4["第四步<br/>对照真实项目"]
    S4 --\> DONE["真正接管工具"]
-->

前三步是**工具掌握**——装上、写 Hello World、跑通。这三步对任何 AI Agent 工具都通用，工程师换一个工具也得这样做。第四步是**项目落地**——把通用的工具能力对接到具体的工程任务（本篇里就是把 RobustMQ 的测试方案落到 Hermes 用户目录）。

<span style="color: red; font-weight: bold;">这一步走完，才真正接管了这个工具。否则只是"用过"，不是"会用了"</span>。

### 6.3 关键认知：扩展描述是给 LLM 看的

还有一条认知贯穿全文，值得在这里再强调一次：<span style="color: red; font-weight: bold;">Tool 的 description 是给 LLM 看的，不是给人看的</span>。

第 4 章已经展开讲过为什么这一条只能撞坑才能内化。这里只点一句：这是贯穿本篇的核心认知——写得越清楚，LLM 调得越准；很多"Agent 调 Tool 但参数错了"的问题，根因都是描述没写明白。

## 7. 小结与思考题

### 7.1 四步法口诀

接管一个新工具的第一天，四步走：

```text
装上体验 → 写 Hello World → 跑通 → 对照真实项目想清楚怎么放
```

前三步是工具掌握，工程师在任何工具上都该这么做；第四步是项目落地，把通用的工具能力对接到具体的工程任务——这一步走完，才真正接管了这个工具。

### 7.2 慢路与快路

很多人第一天卡在前两步——把环境装得完美、文档读得透彻，但没动手写过任何扩展，这是慢路。

先跑通最小闭环，再带着具体问题读对应章节——这条路上每一步都有反馈，每一次卡壳都是下一步的指路灯。

### 7.3 项目阶段 Check List（可裁剪）

把"接管新工具第一天"的动作做成可裁剪的 Check List，供项目阶段快速查阅。每行带一个反问点，落到具体可判断的标准上。

| 项目阶段 | 关键动作 | 反问点 |
|---|---|---|
| 装上工具 | 装到能跑就停手，立刻进下一步 | 是否已经敲出了第一个能跑的命令？ |
| 建立手感 | 扔几类典型指令 + 把 `/help`、`/tools`、`/model` 各过一遍 | 知道工具给了哪些"开关"吗？ |
| 写最简扩展 | 一个最简 Tool + 一个最简 Skill，各 20 行内 | 双层机制都跑通了吗？ |
| 卡坑记录 | 把卡过的坑逐条记下来 | 三类典型坑都内化了吗？ |
| 对照真实项目 | 把项目方案逐条对应到工具用户目录 | 每条方案决策都有物理落点吗？ |
| 定第一行代码 | 找到依赖最少的那个扩展先写 | 哪个扩展是后续所有扩展的前提？ |

### 7.4 思考题

回想最近接手的一个开源框架或库，如果用本篇的四步法重做一遍接管过程，会在哪一步发现自己当时其实是跳过的？这一步跳过让后面付出了什么代价？
