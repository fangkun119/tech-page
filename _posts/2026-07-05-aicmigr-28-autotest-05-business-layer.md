---
title: 传统项目迁AI 28：自动测试 - 添加业务层、跑通系统
author: fangkun119
date: 2026-07-05 08:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
math: true
mermaid: true
image:
  path: imgs/aicmigr-28-autotest-05-business-layer/cover.jpg
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
aicmigr-28-autotest-05-business-layer
传统项目迁AI 28：自动测试 - 添加业务层、跑通系统
-->

## 1. 代码层做完，系统为什么还跑不起来——业务层的本质

<img src="imgs/aicmigr-28-autotest-05-business-layer/20eacb9aa7350aee8923315b4692c906_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

上一篇代码层写完了——5 个 Python Tool、SKILL.md 骨架，每个 Tool 都过了单点对话验证。你可能会问，代码层五件 Tool 都过了单点验证，为什么系统还跑不起来？

因为还差五件**业务层**的事没做：场景库是空的、报告模板没写、config.yml 没填、Deploy Key 没生成、cron.yml 没装。<span style="color: red; font-weight: bold;">这五件都不是代码层的事</span>。本篇先讲清楚业务层的本质，再挨个把这五件填进去，最后让系统真的跑起来。

### 1.1 代码层是能力，业务层是定义

<img src="imgs/aicmigr-28-autotest-05-business-layer/e0baefc824ae86d6fdb417356eefa222_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

先用传统软件工程里的一个常识打通认知。

传统软件项目里，<span style="color: red; font-weight: bold;">接口骨架先定下来，业务实现再往骨架里填——接口还在晃，往上糊业务会跟着晃</span>。这条常识平移到 AI 测试系统，对应关系很清晰：

- **接口骨架 / 抽象层** → 代码层（Tool 和 Skill 的 Python 代码）。
- **业务实现层（填进接口的业务）** → 业务层（场景库、配置、凭证、调度）。

代码层是 `cluster.py` 怎么 spawn 进程、`chaos.py` 怎么调 Chaosd HTTP API、`report.py` 怎么渲染 Jinja2 模板——这是 Hermes Tool 的通用写法，换个被测系统也能套，对应传统工程里的抽象层。

业务层是当前项目具体测什么的定义：测哪些场景、用什么 SDK 版本矩阵、报告归档到哪、什么频率跑。这些跟 RobustMQ 绑死，给另一个开源项目加自动化测试时，业务层要完全重写——对应传统工程里填进接口的具体业务。

你可能会问，为什么不一边写代码一边填业务？

两层工程理由：

第一层，代码层是抽象骨架，<span style="color: red; font-weight: bold;">骨架晃动时往上糊业务，业务会跟着晃</span>。先把 5 个 Tool 接口稳定下来，业务层填进去才有明确的对接点——场景库知道按什么字段写、报告模板知道 `report.py` 会传什么字段、`config.yml` 知道哪个 Tool 读哪个键。

第二层，业务层变更不该动摇代码层。加一个新协议场景、调一下 SDK 版本矩阵、改一下报告字段，都是常态。两层分离后，<span style="color: red; font-weight: bold;">这些变更只动业务层文件，代码层一行不用改</span>。

一句话收住：<span style="color: red; font-weight: bold;">代码层是能力，业务层是定义。能力可以复用，定义必须重写。先把能力做完，再把定义填进去，工程上更稳。</span>

### 1.2 业务层是四类边界清晰的资产

<img src="imgs/aicmigr-28-autotest-05-business-layer/38e52c4a6a39dbc0f8bc5373da4ad67a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">业务层不是一团模糊的"配置"，是四类边界清晰的资产，每一类都有自己的格式和读者</span>。填错格式或写错读者，业务层就接不上代码层。

| 资产类别 | 形态 | 给谁用 | 关键约束 |
|---|---|---|---|
| 场景库 | Markdown 自然语言 | AI Agent | 关键字段对齐 `chaos.py` 接口、`client.py` 返回 JSON |
| 配置 | YAML | 代码层（Tool 按 key 取值） | `chaos.py` 读 Chaosd 端点、`client.py` 读 SDK 矩阵、`report.py` 读 GitHub 仓库地址 |
| 凭证 | SSH 密钥 | `git push` | 权限最小化、不交互（Cron 跑起来没人输密码） |
| 调度 | YAML 时间表 | Hermes | P0/P1/P2 分级变成真实定时任务，频率与资源约束平衡、错峰避免冲突 |

### 1.3 五件具体事：把待办列表化

上一篇末尾留了五个待办，都不是代码层的事，下面挨个填：

```text
1. 场景库 scenarios/ 是空的,Skill 没拿到具体场景没法跑完整流程。
2. 报告模板 report.md.j2 没写,report.py 调 Jinja2 加载会报模板找不到。
3. 套件配置 config.yml 是空的,SDK 矩阵、Chaosd 端点、报告仓库地址都没值。
4. GitHub Deploy Key 和 test-reports 仓库没创建,git push 真跑会失败。
5. cron.yml 没装,系统还是手动 CLI 触发,不是 7×24。
```


## 2. 五件业务层资产挨个填

五件业务层资产，按"给 Agent 看 → 给代码读 → 给 git push → 给 Hermes 调度"的读者递进顺序，挨个填：场景库、报告模板、config.yml、Deploy Key、cron.yml。每件先上可复现的提示词/配置/命令原文，再补一段设计判断。

### 2.1 场景库 scenarios/

<img src="imgs/aicmigr-28-autotest-05-business-layer/4225f96311756ca4ff67dcebdebe27c9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 目录树与第一个场景的提示词

场景库按 25 篇方案的 P0/P1/P2 分级组织，目录树如下：

<img src="imgs/aicmigr-28-autotest-05-business-layer/5160a88d9ff8e9b0d5ff59d96987e1d6_MD5.jpg" style="display: block; width: 800px;" alt="场景库目录树：按协议和优先级分级组织">

<!--
图片内容说明
路径：imgs/aicmigr-28-autotest-05-business-layer/5160a88d9ff8e9b0d5ff59d96987e1d6_MD5.jpg
用途：展示按 P0/P1/P2 分级组织的 scenarios/ 场景库目录结构
内容：场景库目录树，按协议（mqtt/、mq9/等）和优先级（p0-/p1-/p2-）分级组织，每个场景独立一个 Markdown 文件，Agent 加载时只取本轮要跑的几个
-->

第一个场景 `p0-broker-kill-leader.md` 的提示词原文：

```
帮我在 ~/.hermes/skills/robustmq-chaos-test/scenarios/mqtt/ 下
写第一个场景 p0-broker-kill-leader.md。

格式:Markdown,自然语言描述,Agent 读完知道按 SKILL.md 的"单场景五步"怎么调 Tool。

具体内容:
- 场景名:p0-broker-kill-leader
- 协议:MQTT
- 优先级:P0(每次触发都跑)
- 集群:3 节点 RobustMQ,broker-1 是 Leader
- 故障:用 chaos.py inject broker-kill,target=broker-1,duration=30 秒
- SDK 矩阵:从 config.yml 读 P0 档的 SDK 列表
- 验证:故障期间只记录,Chaosd recover 后等 broker-1 健康检查 200,
       再等 60 秒,跑完整 SDK 矩阵的 basic-pubsub
- 通过标准:exit_code=0,lost=0,p99_ms<500
- 失败处理:记录到 report,继续下一场景

写完不要解释,我直接看文件。
```

#### (2) 关键设计：场景描述是给 Agent 看的可执行说明

你可能会问，场景描述写给谁看？答案不是人，是 Agent。

传统软件里的测试用例（如 JUnit/pytest）是给测试框架执行的结构化代码；这里的场景库换成了给 Agent 读的自然语言 Markdown，但承担同样的"告诉系统这一轮跑什么"的职责。<span style="color: red; font-weight: bold;">两者形态不同，定位一致：都是**可执行说明**，不是文档</span>。

Claude Code 跑完产出的 30 行左右 Markdown，主要核对两件事：故障类型和参数跟 `chaos.py` 接口对得上；通过标准的字段名（`exit_code` / `lost` / `p99_ms`）跟 `client.py` 返回的 JSON 对得上。字段对齐 chaos.py 接口、字段名对齐 client.py 返回 JSON——<span style="color: red; font-weight: bold;">场景描述是给 Agent 看的可执行说明，不是给人看的文档</span>。

第一个场景写完后，剩下几个场景套同样模板，只改协议、target、duration、SDK 矩阵几个参数。每个场景独立成文件，Agent 一次只加载本轮要跑的那几个。这是场景库不是 chaos.py，属于业务层。

### 2.2 报告模板 templates/report.md.j2

<img src="imgs/aicmigr-28-autotest-05-business-layer/300069489be324d3e317ea0eb8c00f90_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

`report.py` 在 git push 之前要把测试结果渲染成 Markdown，模板就是 `report.md.j2`。提示词原文：

```
帮我写 ~/.hermes/skills/robustmq-chaos-test/templates/report.md.j2,
Jinja2 模板。

输入是 report.py 传进来的 run_data,字段包括:run_id / started_at /
ended_at / cluster_info / scenarios(每个 scenario 含 fault_info /
sdk_results / passed)。

模板要点:
- 顶部一段 summary,Run ID + 起止时间 + 整体 pass/fail
- 每个 scenario 一个二级标题,展开 fault 信息和 SDK 矩阵结果
- SDK 结果用 Markdown 表格,列:sdk / version / scenario /
  exit_code / lost / p99_ms / passed
- 底部一段"协议兼容性归因",按 25 讲那张归因表的逻辑套
  (不同 SDK 不一致 → 协议实现问题;特定版本失败 → 版本兼容问题;
   全部失败 → broker 端问题)
- 别在模板里调 LLM,纯 Jinja2 语法
模板长度控制在 80 行以内。
```

#### (1) 模板的灵魂：协议兼容性归因

传统测试报告模板只列 pass/fail 和数据，归因靠人写。这里的 `report.md.j2` 在底部多了一段"协议兼容性归因"——按 25 篇那张归因表（不同 SDK 不一致 → 协议实现问题；特定版本失败 → 版本兼容问题；全部失败 → broker 端问题）自动套模板。问题暴露和定位归因合在一份报告里给人看，<span style="color: red; font-weight: bold;">协议兼容性归因</span>才是 25 篇那张归因表落地的真实落点，也是"测试报告对外公开建立社区信任"那条决策真正能落地的位置。

#### (2) 边界 case 的人工补刀

Claude Code 产出的模板有一处要手动改：`if all_failed` 那段加一句"如果三种归因都不命中，标记为待人工分析"。<span style="color: red; font-weight: bold;">AI 在边界 case 上会偷懒，工程师手动收一下</span>——这是上一篇"工程师把关的边际价值"在业务层填充中的延续。报告模板属于业务层定义（换了被测系统，归因逻辑要重写），不是代码层能力。

### 2.3 套件配置 config.yml

<img src="imgs/aicmigr-28-autotest-05-business-layer/f51920826921ee5b7e2595ce7057f1f8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

`chaos.py` 读 Chaosd 端点、`client.py` 读 SDK 矩阵、`report.py` 读 GitHub 仓库地址，都从这个文件读。完整 YAML：

```yaml
# ~/.hermes/skills/robustmq-chaos-test/config.yml
chaosd:
  endpoint: "http://127.0.0.1:31767"

sdk_matrix:
  p0:
    - sdk: python
      version: "3.11"
      scenarios: [basic-pubsub]
    - sdk: go
      version: "1.21"
      scenarios: [basic-pubsub]

  p1:
    - sdk: python
      versions: ["3.10", "3.11", "3.12"]
      scenarios: [basic-pubsub, failover]
    - sdk: rust
      version: "1.70"
      scenarios: [basic-pubsub, failover]

  p2:
    - sdk: python
      versions: ["3.10", "3.11", "3.12"]
    - sdk: go
      versions: ["1.20", "1.21"]
    - sdk: rust
      versions: ["1.70", "1.75"]
    - sdk: java
      versions: ["11", "17", "21"]

github:
  reports_repo: "git@github.com:<your-org>/test-reports.git"
  deploy_key_path: "~/.ssh/test-reports-deploy"
  branch: "main"
```

#### (1) 为什么 P0 只放 2 个 SDK

你可能会问，P0 为什么刻意只放 2 个 SDK？因为 <span style="color: red; font-weight: bold;">P0 是基础保障线，跑得越快越好，大矩阵留给 P1/P2</span>。

这是 25 篇场景分级直接落到 SDK 矩阵的体现：<span style="color: red; font-weight: bold;">分级不只是测哪些场景，是每档用多大 SDK 矩阵</span>。矩阵大小直接决定跑一轮的时长——P0 两小时跑一次，只放 python 和 go 各一个版本；P2 一周跑一次，才放开到四种语言的多版本矩阵。这是配置文件不是 Tool 代码，换了被测项目要重写 SDK 矩阵，属于业务层。

### 2.4 GitHub Deploy Key 和 test-reports 仓库

#### (1) 三步命令原文

**第一步：生成密钥对**

```bash
ssh-keygen -t ed25519 -f ~/.ssh/test-reports-deploy \
  -C "robustmq-chaos-test deploy key" -N ""
```

**第二步：在 GitHub 配置 Deploy Key**

创建一个空的 public 仓库 test-reports，进 Settings → Deploy keys → Add deploy key，把 `~/.ssh/test-reports-deploy.pub` 内容贴进去，勾选 Allow write access。

**第三步：初始化仓库本地状态**

```bash
GIT_SSH_COMMAND="ssh -i ~/.ssh/test-reports-deploy" \
  git clone git@github.com:<your-org>/test-reports.git /tmp/test-reports-init

cd /tmp/test-reports-init
echo "# RobustMQ Quality Reports" > README.md
git add README.md

GIT_SSH_COMMAND="ssh -i ~/.ssh/test-reports-deploy" \
  git commit -m "init" && git push
```

push 成功这一步，Deploy Key 链路就活了，后面 `report.py` 跑出来的报告会一直 push 到这个仓库。先把 key 链路跑通，再让代码层用。

#### (2) 命令背后的两个设计含义

| 设计点 | 含义 |
|---|---|
| `-N ""` 空 passphrase | 这把 key 给自动化系统用，无人值守时无法交互输入密码，所以必须空 passphrase。Check List 那条就是落地这里 |
| public 仓库 | test-reports 设为 public，对应 25 篇"完全公开建立社区信任"那条决策——测试报告对外可见，是物理体现 |
| 只授写权限到单一仓库 | Deploy Key 只能写 test-reports，权限最小化，不是放一把能写所有仓库的 PAT |

你可能会问，为什么用 Deploy Key 而不是 Personal Access Token？因为 Deploy Key 只授写权限到单一仓库，跟 PAT（一把 token 能动整个账号下的仓库）相比权限范围小一个数量级。这是凭证不是代码，换了托管平台或组织要重新生成，属于业务层。

### 2.5 cron.yml 三档调度

<img src="imgs/aicmigr-28-autotest-05-business-layer/29a4887b8c0e40822c4debf0afaab88e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

`cron.yml` 把 P0/P1/P2 三档变成 Hermes 真实的定时任务：

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

#### (1) 频率与资源平衡

| 任务 | schedule | 频率含义 |
|---|---|---|
| `p0-mqtt-basic` | `0 */2 * * *` | 每两小时整点 |
| `p0-mq9-basic` | `30 */2 * * *` | 每两小时半点，与 MQTT 错开 30 分钟避免资源冲突 |
| `p1-daily-fault` | `0 3 * * *` | 每天凌晨 3 点 |
| `p2-weekly-matrix` | `0 4 * * 0` | 每周日凌晨 4 点 |

频率配置直接来自 25 篇场景分级表。P0 跑得最勤，所以刻意错开 MQTT 和 mq9 错峰 30 分钟；P2 跑全量矩阵最重，所以放周日凌晨机器最闲的时候。这跟传统 CI/CD 流水线的定时触发器思路一致：频率越高，单轮负担越要压下来。

#### (2) Prompt 用自然语言对接 Procedure

你可能会问，cron 装上后系统怎么知道该跑什么？答案是每条 job 的 `prompt` 字段——自然语言写的同一句话。Hermes 把 prompt 喂给 AI Agent，Agent 加载 robustmq-chaos-test Skill 后按 Procedure 跑，跟 hermes CLI 手动触发走的是**同一条路**。

这条"同一条路"的设计含义，先埋一句伏笔，后面真跑完再展开。`cron.yml` 是调度时间表，跟代码层无关，换了调度策略或频率要重写，属于业务层。


## 3. 链路真跑：用一段对话跑完整个 P0 测试

<img src="imgs/aicmigr-28-autotest-05-business-layer/43d3bd192e1fad1d5d6c8e6e5e701e87_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

业务层五件填完，进系统真跑一遍。

### 3.1 加载确认：Tool 和 Skill 都被 Hermes 看到

代码层做完、业务层填完，第一件事不是直接跑——先确认 Hermes 真的看到了它们。怎么确认？两条命令。

```
$ hermes /skills
robustmq-chaos-test v1.0.0 chaos testing for RobustMQ

$ hermes /tools
cluster       集群启停
observability 观测数据收集
client        多语言 SDK 调度
chaos         故障注入和恢复
report        报告生成 + GitHub 提交
... (Hermes 自带 Tool 略)
```

5 个自研 Tool 加 1 个 Skill 全部被 Hermes 识别，加载链路没问题。如果这里 `/skills` 列不出 `robustmq-chaos-test`、或 `/tools` 缺了某个，就要回头查 SKILL.md 摆放路径和 Tool 注册，不能往下跑。

### 3.2 进交互模式手动触发 P0

业务层填完后，系统真跑的链路长这样。

<img src="imgs/aicmigr-28-autotest-05-business-layer/9e8731e33a9ed7d60b571e29bf010631_MD5.jpg" style="display: block; width: 800px;" alt="业务层填完后进入系统真跑验证阶段">

<!--
图片内容说明
路径：imgs/aicmigr-28-autotest-05-business-layer/9e8731e33a9ed7d60b571e29bf010631_MD5.jpg
用途：展示业务层填完后系统进入真实运行验证阶段的总览
内容：呈现业务层五件事（场景库、报告模板、config.yml、Deploy Key、cron.yml）就位后，进入 Hermes 真实运行阶段，从"代码就绪"过渡到"系统真跑"
-->

这一步先不装 Cron，手动触发一次 P0——验证整条链路真的能闭环。等手动跑通，cron.yml 才能放心装上自动跑。`hermes` 不带参数进交互模式：

```
$ hermes
> 按 P0 跑一轮 MQTT 基础场景
```

这段自然语言 prompt 是整条链路的入口。Hermes 把它喂给 AI Agent，Agent 加载 robustmq-chaos-test Skill，按 SKILL.md 的 Procedure 一轮一轮往下跑。

<img src="imgs/aicmigr-28-autotest-05-business-layer/0e1fe89bd38ca2a765b952aadc18b618_MD5.jpg" style="display: block; width: 800px;" alt="在 Hermes 交互模式中输入 P0 触发语句">

<!--
图片内容说明
路径：imgs/aicmigr-28-autotest-05-business-layer/0e1fe89bd38ca2a765b952aadc18b618_MD5.jpg
用途：展示进入 Hermes 交互模式手动跑 P0 场景的对话触发过程
内容：在 hermes 交互终端输入"按 P0 跑一轮 MQTT 基础场景"，验证整个系统在真实链路上的闭环能力，作为装 Cron 自动跑之前的手动验证步骤
-->

### 3.3 七轮对话把链路串起来

下面是 Agent 真实跑出来的七轮。每轮 Agent 调一个 Tool，把调用、返回和关键字段贴出来，让读者看到链路是怎么一轮轮串起来的。

<img src="imgs/aicmigr-28-autotest-05-business-layer/1f125d25c68e5539cdc2fbe640afa123_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-28-autotest-05-business-layer/b823b22df8a251f0a905fb98ba3e6dc1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 第 1 轮：加载 Skill，前置检查

```
Loaded skill: robustmq-chaos-test

Calling cluster(action="status")...
→ {"running": false}
```

Agent 先加载 Skill，再调 `cluster` 看集群状态。`running: false` 说明环境干净，可以起集群——这是 Procedure 规定的前置检查，避免在已有集群上叠加故障。

#### (2) 第 2 轮：起集群

```
Calling cluster(action="start")...
→ {"status": "running", "endpoint": "127.0.0.1:1883",
   "data_dirs": ["/tmp/rmq-xxx-1", "/tmp/rmq-xxx-2", "/tmp/rmq-xxx-3"]}
```

3 节点集群起来，`endpoint` 是客户端连接地址，三个 `data_dirs` 对应三个 broker 的数据目录，broker-1 是 leader。

#### (3) 第 3 轮：加载场景，打基线 snapshot

```
Loaded scenario: scenarios/mqtt/p0-broker-kill-leader.md

Calling observability(action="snapshot")...
→ {"timestamp": "...", "metrics": {...}, "logs": {...}}
```

Agent 按 prompt 里"P0"和"MQTT"定位到 `scenarios/mqtt/p0-broker-kill-leader.md`。`observability` 打基线 snapshot——**这一步是为故障后对比用的**，故障期观测数据减去基线，才知道偏差有多大。

#### (4) 第 4 轮：注入故障

```
Calling chaos(action="inject", fault_type="broker-kill",
              target="broker-1", duration_seconds=30)...
→ {"fault_id": "fault-abc123", "status": "active"}
```

`chaos` 把 broker-1 kill 掉，`fault_id` 是后续 recover 的句柄，`status: active` 表示故障窗口已开。

#### (5) 第 5 轮：故障期 SDK 测试，只记录不判断

```
Calling client(action="run", sdk="python", version="3.11",
               scenario="basic-pubsub", cluster_endpoint="127.0.0.1:2883")...
→ {"exit_code": 1, "lost": 47, "p99_ms": 2300, "errors": [...]}
```

```
Calling client(action="run", sdk="go", version="1.21",...)
→ {"exit_code": 1, "lost": 52, "p99_ms": 2100,...}
```

故障期 Python SDK 丢 47 条消息、p99 飙到 2300ms，Go SDK 丢 52 条——**这些数字都符合预期，但这一轮的关键设计是"只记录不判断"**：故障期 broker-1 已经挂了，跑出 `exit_code: 1` 是物理事实，不是被测系统的 bug。如果在这里就判定 fail，整个测试就是误报。故障期的结果先收着，等自愈窗口再统一归因。

#### (6) 第 6 轮：自愈验证

故障窗口 30 秒结束，Chaosd 自动 recover，等 broker-1 健康检查 200 再等 60 秒，进入自愈验证窗口：

```
Calling chaos(action="recover", fault_id="fault-abc123")...
→ {"status": "recovered"}

Calling cluster(action="status")...
→ {"healthy_brokers": 3}
```

`recover` 让 Chaosd 释放故障，`healthy_brokers: 3` 确认三个 broker 全部健康。再等 60 秒让集群稳定，然后跑完整 SDK 矩阵——**这才是 pass/fail 的唯一判据**：

```
... waiting 60s...

Calling client(action="run", sdk="python", version="3.11",...)...
→ {"exit_code": 0, "lost": 0, "p99_ms": 38}

Calling client(action="run", sdk="go", version="1.21",...)...
→ {"exit_code": 0, "lost": 0, "p99_ms": 41}

所有 SDK 自愈验证通过。
```

自愈窗口两个 SDK 都是 `exit_code: 0, lost: 0, p99_ms` 在 50ms 以内——这才是链路真的通过。

#### (7) 第 7 轮：生成报告并 push 到 GitHub，然后停集群

```
Calling report(action="generate_and_push", run_data={...})...
→ {"json_path": "...", "markdown_path": "...",
   "github_url": "https://github.com/<your-org>/test-reports/blob/main/...",
   "run_passed": true}
报告已 push: https://...
Calling cluster(action="stop")...
→ {"status": "stopped"}
本轮完成。
```

`report` 把整轮 run_data 渲染成 Markdown 报告，通过 Deploy Key push 到 test-reports 仓库，返回 `github_url` 可直接访问。`cluster` 最后把集群停掉，资源归位——**生成报告 + push GitHub + 停集群，这一轮把链路彻底闭环**。

七轮跑完了。整轮 7 个动作走下来，就是 25 篇方案文档那张时序图的真实运转——这里 cron.yml 没装，用 CLI 手动触发了一次，但<span style="color: red; font-weight: bold;">这条链路装上 Cron 之后一行代码不用改就变 7×24</span>。

七轮跑完了，这和上一篇的单点对话验证有什么不同？传统软件工程里有条常识：**单元测试通过 ≠ 集成测试通过**。<span style="color: red; font-weight: bold;">单点验证就是单元测试——每个 Tool 单独调一次，只证明"这个 Tool 活着"；链路验证就是集成测试——一次完整七步对话跑下来，证明 Procedure 真的把五个 Tool 串得起来、报告真的能 push、集群真的能停</span>。**单点验证 ≠ 链路验证，前者只证明 Tool 活着，后者证明链路串得起来。** Cron 上线前必须做链路验证——这是最后一道闸。


## 4. 统一接口：手动验证到 7×24 的零成本切换

<img src="imgs/aicmigr-28-autotest-05-business-layer/7a5e00677a4f3dd5ff1d86d631b58f44_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

cron.yml 一装，系统从手动验证升级到 7×24 自动化，代码一行不用改。这个结论听起来像是占了便宜，但它不是巧合——它是第 2.5 节埋下的那条"同一条路"设计，经过第 3 章链路真跑时的见证，到这里收束出来的工程价值。

### 4.1 统一接口：自然语言 prompt

第 3 章手动跑 P0 时，你在 Hermes 交互模式下敲进去的是一句话：`> 按 P0 跑一轮 MQTT 基础场景`。Cron 自动触发的也是同样一段自然语言 prompt，只是来源不同——一个来自人手敲，一个来自 cron.yml 在 P0/P1/P2 三档某一档的时间点触发。

你可能会问：Cron 触发和第 3 章的 CLI 手动触发，走的是同一条路还是两条路？

同一条路。不管是哪种来源，Hermes 都把这段 prompt 喂给 AI Agent，Agent 加载 robustmq-chaos-test Skill 后按 Procedure 跑。<span style="color: red; font-weight: bold;">入口不同，出口相同——对 Agent 来说它根本不在乎 prompt 是谁喂进来的，它只认 prompt 本身</span>。这就是"同一条路"的全部含义，也是后面一切工程价值的地基。

### 4.2 工程价值：接口稳定后实现可替换

为什么统一接口这么重要？如果不统一会怎样？把这两问拆成正面和反面看。

#### (1) 正面：接口稳定后实现可替换

落到传统软件工程类比上，这句话对应的是"接口稳定后实现可替换"。做过 Java 或 Go 的人都知道：你定义一个支付接口，微信支付和支付宝是它的两种实现，上层业务代码只依赖接口不依赖实现，所以换支付渠道时上层一行不改。

映射到本篇：

| 传统软件工程 | 本篇对应物 |
|---|---|
| 接口 | 自然语言 prompt |
| 接口的两种实现 | Cron 触发（自动）/ CLI 触发（手动） |
| 上层业务逻辑 | Hermes 喂 prompt 给 Agent、Agent 加载 Skill 按 Procedure 跑 |
| 接口契约 | SKILL.md 的 Procedure（Agent 执行步骤） |

接口稳定之后，切换实现不破坏上层逻辑——这就是为什么 <span style="color: red; font-weight: bold;">手动验证 → 自动 7×24 的切换零代码改动</span>。这条价值不是事后吹出来的，是 prompt 一开始就用自然语言对接 Procedure 这个设计选择直接带来的。

#### (2) 反面：两条链路意味着负担翻倍

反过来想，如果 Cron 和 CLI 走两条不同的链路——Cron 走一套独立的调度代码、CLI 走另一套交互代码，两套代码各自把 prompt 拼出来各自喂给 Agent——会发生什么？

每次改 Procedure，两边都得改、两边都得测。每加一个新触发来源（比如后面要接的飞书机器人），就得多维护一条链路。工程负担随触发来源数量线性翻倍，而且每条链路都有自己踩坑的概率。<span style="color: red; font-weight: bold;">统一接口把这件事压成一条路：所有触发来源都只负责"把同一段 prompt 喂进来"，剩下的全部复用</span>。这才是"先手动验证、再自动跑"能成为一个自然工程节奏的原因。

### 4.3 工程链路活起来的样子

<img src="imgs/aicmigr-28-autotest-05-business-layer/cc5f45eb9bca0c22f26f83a46f3cb84b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这条链路现在已经真正活起来，代码在 https://github.com/robustmq/robustmq/tree/main/chaos-test ，值得停下来多看一眼。

<img src="imgs/aicmigr-28-autotest-05-business-layer/af26f67bc52dd1ca05ce7705465a8093_MD5.jpg" style="display: block; width: 800px;" alt="从需求到业务层填充的工程链路追溯图">

<!--
图片内容说明
路径：imgs/aicmigr-28-autotest-05-business-layer/af26f67bc52dd1ca05ce7705465a8093_MD5.jpg
用途：展示从需求到业务层填充的完整工程链路追溯图
内容：呈现 24 篇一句话需求 → 25 篇两份评审级文档 → 26-27 篇长出代码 → 28 篇填业务层的全链路，每一步代码、配置、判断都可向上游决策追溯，体现"工程链路活起来"的样子
-->

这条链路上每一步都有迹可循：24 篇的一句话需求 → 25 篇的两份评审级文档 → 26-27 篇长出来的代码 → 28 篇填进去的业务层。任何一段代码、任何一个配置项、任何一句设计判断，都能向上追溯到某一条决策。<span style="color: red; font-weight: bold;">我的看法是，工程链路的价值不在于文档本身有多漂亮，而在于"每一个为什么都能找到答案"——这才是工程链路活起来的样子</span>。

### 4.4 系统升级到 7×24

这正是 4.2 节那条"接口稳定后实现可替换"的直接落地：cron.yml 装好之后，Hermes 启动时自动加载，把原本由人手敲的那段 prompt 换成由定时器喂进来，Hermes、Agent、Skill、Procedure 全部原样复用——<span style="color: red; font-weight: bold;">系统正式从"对话触发"变成 7×24</span>。

再往前看一步：第一期通道仅 CLI（25 篇拍过的决策），飞书第二期接入。<span style="color: red; font-weight: bold;">飞书这条新触发来源接进来时，Procedure 一字不改</span>——这不过是"统一接口"这条设计原则的第二次印证。只要新来源也喂的是同一段自然语言 prompt，它就自动复用同一条路。

## 5. 业务层落地 Check List 与小结

<img src="imgs/aicmigr-28-autotest-05-business-layer/71c52c5e69d318ed441462a65e87694c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

业务层填完、链路真跑、系统升级到 7×24，全篇收口。这一章给两样东西：一份可裁剪速查表，一句带走整篇认知主线的收口判断。

### 5.1 业务层落地 Check List

按资产类别给出 5 张表，供项目阶段快速查阅。

#### (1) 填场景库时

| 项 | 检查内容 |
|---|---|
| 字段对齐 | 故障类型和参数是否跟 chaos.py 接口对得上？ |
| 通过标准字段 | exit_code / lost / p99_ms 字段名是否跟 client.py 返回 JSON 对得上？ |
| 优先级分级 | 场景是否按 P0/P1/P2 标注，Cron 按档加载？ |
| 模板复用 | 同协议同故障类型的场景是否套同样模板，只改关键参数？ |

#### (2) 填配置时

| 项 | 检查内容 |
|---|---|
| Key 命名 | chaos.py / client.py / report.py 读的 key 是否在 config.yml 都有？ |
| P0 极简 | P0 档是否只放最少 SDK（基础保障线，跑得越快越好）？ |
| 大矩阵后置 | 多版本矩阵是否留给 P1/P2 档？ |
| 仓库地址 | GitHub 仓库地址、分支、Deploy Key 路径是否写实？ |

#### (3) 配凭证时

| 项 | 检查内容 |
|---|---|
| 空 passphrase | 自动化系统用的 key 是否 `-N ""` 空密码、不交互？ |
| 单仓权限 | Deploy Key 是否只授写权限到单一仓库（test-reports）？ |
| 私钥隔离 | 私钥是否放 ~/.ssh，不进 .env、不进版本库？ |
| 链路验证 | clone + push 是否真实跑通一次，确认 key 链路活？ |

#### (4) 填调度时

| 项 | 检查内容 |
|---|---|
| 频率对齐分级 | P0 高频、P1 中频、P2 低频是否对齐 25 篇场景分级表？ |
| 错峰避让 | 同优先级多协议任务是否错峰（如 MQTT 与 mq9 错开 30 分钟）？ |
| Prompt 自然语言 | schedule 任务的 prompt 是否用自然语言，让 Agent 按 Procedure 跑？ |
| 与 CLI 同路 | Cron 触发的 prompt 是否与 CLI 手动触发用同一段自然语言？ |

#### (5) 链路验证时

| 项 | 检查内容 |
|---|---|
| 单点 ≠ 链路 | 每个 Tool 单独调通不等于链路通，必须做一次完整 7 步对话验证 |
| 故障期只记录 | 故障期 SDK 测试是否只记录不判断（避免误报）？ |
| 自愈才判断 | pass/fail 唯一依据是自愈验证窗口的 exit_code/lost/p99_ms？ |
| 报告真 push | 报告是否真 push 到 GitHub 仓库、URL 是否可访问？ |

### 5.2 小结：代码层是能力，业务层是定义，场景才是业务

<img src="imgs/aicmigr-28-autotest-05-business-layer/a9ef80cdbf517c7b21772e2ef5b6b4cc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

五件业务层的事——写场景库、写报告模板、填 config.yml、生成 Deploy Key 和 test-reports 仓库、装 cron.yml 三档调度——单独看都不复杂，合在一起把代码层那几个 Tool 从"能调"变成"真跑"。

换一个项目，这套系统能搬多少过去？作者的判断是：

> <span style="color: red; font-weight: bold;">代码层是能力，业务层是定义，场景才是业务</span>——代码层是通用 Hermes 编程，换个项目能复用；业务层是当前这个具体项目的定义，跟 RobustMQ 绑死。先做代码层，再填业务层，工程上更稳。下次再做一个类似系统（给另一个开源项目加自动化测试），代码层 80% 能搬，业务层完全重写。

这是全篇认知主线的最终落点：第 1 章讲清楚"代码层 vs 业务层"是什么，第 2 章每件资产回扣一次"这是业务层不是代码层"，到这里收第三次——换项目怎么办。三次回扣角度不同，指向同一件事：**能力可以复用，定义必须重写**。

跑通系统不是终点。第二期飞书接入、更多场景库、Quality Dashboard 前端，都是在这条已经活起来的链路上往外长——业务层可以不断重写，代码层那条统一接口的路不用再动。

回想最近做过的一个项目，如果按这一篇的"代码层 vs 业务层"分一下，哪些代码是通用能力，哪些是业务定义？如果有一天要把这个项目搬到另一个公司、另一个客户、另一个相似场景，代码层能搬多少、业务层要重写多少？

