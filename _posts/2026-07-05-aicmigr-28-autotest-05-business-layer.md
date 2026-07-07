---
title: 传统项目迁AI 28：自动测试 - 添加业务层、跑通系统
author: fangkun119
date: 2026-07-05 08:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
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

## 1. 全文导读地图

<img src="imgs/aicmigr-28-autotest-05-business-layer/56a9b220488cff5221df18a317836ddd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是"自动测试"系列的第五篇，承接上一篇"编写 Skill 套件"的进度。上一篇把 5 个 Python Tool 和 SKILL.md 骨架写完，每个 Tool 都通过单点对话验证过——但系统还跑不起来：场景库是空的、报告模板没写、config.yml 没填、Deploy Key 没生成、cron.yml 没装。本篇把这五件业务层的事填完，然后用一段真实对话把整套系统跑一遍，看到报告 push 进 GitHub 仓库。

<img src="imgs/aicmigr-28-autotest-05-business-layer/32fe4a8c51ab7e70abb3cdc61f72e7a8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    A[两类读者] --\> B[初学 AI 编程工程师<br/>想系统掌握 AI 编程方法论]
    A --\> C[熟练 AI 编程工程师<br/>想快速回顾方法论、查 Check List]

    B --\> D[第一部分：方法论提炼<br/>第 2-5 章]
    B --\> E[第二部分：实战演示<br/>第 6-9 章]
    C --\> F[第 2 章 代码层 vs 业务层<br/>核心方法论]
    C --\> G[第 5 章 业务层落地 Check List<br/>项目阶段速查]
    C --\> H[第 9 章 链路真跑<br/>七步闭环验证]

    D --\> D1[2. 代码层 vs 业务层：二分法]
    D --\> D2[3. 业务层资产盘点：四类东西]
    D --\> D3[4. 触发链路统一：Cron 与 CLI 同路]
    D --\> D4[5. 业务层落地 Check List]

    E --\> E1[6. 实战背景：五件业务层待办]
    E --\> E2[7. 五件业务层资产挨个填]
    E --\> E3[8. 链路真跑：七步闭环]
    E --\> E4[9. 从对话触发到 7×24]
-->

第一部分讲方法论（代码层 vs 业务层二分法、业务层四类资产、触发链路统一），不深入具体技术栈，可裁剪做项目 Check List；第二部分把方法论在 robustmq-chaos-test Skill 套件的真实业务层填充和链路验证中复现一遍，逐个资产贴出提示词或配置原文，再用一段真实对话跑完整个 P0 测试，让方法论"不仅知其然、也知其所以然"。

## 2. 代码层 vs 业务层：AI 时代工程交付的二分法

<img src="imgs/aicmigr-28-autotest-05-business-layer/b53c4fd80086cf32d2306d8ca13dfa20_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

上一篇把代码层写完，本篇填业务层。这两层不是偶然的切分，是 AI 时代工程交付的一个核心方法论——把"通用能力"和"业务定义"分开做，先做通用层、再填业务层。

### 2.1 两层的本质差异

代码层是 Tool 和 Skill 的 Python 代码：cluster.py 怎么 spawn 进程、chaos.py 怎么调 Chaosd HTTP API、report.py 怎么渲染 Jinja2 模板。这些是 Hermes Tool 的通用写法，换个被测系统也能套。

业务层是当前具体项目的定义：测哪些场景、用什么 SDK 版本矩阵、报告归档到哪、什么频率跑。这些跟 RobustMQ 绑死——给另一个开源项目加自动化测试时，业务层要完全重写。

### 2.2 为什么先代码层后业务层

代码层在前、业务层在后，工程上更稳。原因有两层。

#### (1) 代码层稳定下来后业务层才有所依附

代码层是抽象骨架，骨架晃动时往上糊业务，业务会跟着晃。先把 5 个 Tool 接口稳定下来，业务层填进去才有明确的对接点——场景库知道按什么字段写、报告模板知道 report.py 会传什么字段、config.yml 知道哪个 Tool 读哪个键。

#### (2) 业务层变更不该动摇代码层

业务变更是常态——加一个新协议的场景、调一下 SDK 版本矩阵、改一下报告字段。代码层和业务层分离后，这些变更只动业务层文件，代码层一行不用改。这正是上一篇"代码层 vs 业务层分离"那条原则在实战中的延续。

### 2.3 一句话记住

代码层是能力，业务层是定义。能力可以复用，定义必须重写。先把能力做完，再把定义填进去，工程上更稳。

## 3. 业务层资产盘点：四类东西要填

<img src="imgs/aicmigr-28-autotest-05-business-layer/dbe4671baf3e68a9f969db3f0c34f0f2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

业务层不是一团模糊的"配置"，是四类边界清晰的资产。每一类都有自己的格式和读者，填错格式或写错读者，业务层就接不上代码层。

### 3.1 场景库：给 Agent 看的自然语言

#### (1) 场景库的用途

场景库是 Agent 拿到触发后真正知道该跑什么的地方。每个场景一个 Markdown 文件，自然语言描述，告诉 Agent：这个场景是哪个协议、用哪个故障类型注入到哪个节点、跑哪些 SDK 验证、通过失败标准是什么。

#### (2) 读者是 Agent 不是人

场景描述不是给人看的文档，是给 Agent 看的可执行说明。写场景库时关键不是文笔，是字段——故障类型和参数要跟 chaos.py 接口对得上，通过标准的字段名（exit_code / lost / p99_ms）要跟 client.py 返回的 JSON 对得上。字段对不上，Agent 调 Tool 就报参数错误。

### 3.2 配置：给代码读的机器格式

config.yml 是给代码读的：chaos.py 从里面读 Chaosd 端点、client.py 读 SDK 矩阵、report.py 读 GitHub 仓库地址。这部分用 YAML 不用自然语言，因为代码层会按 key 取值，自然语言反而读不出来。

### 3.3 凭证：自动化系统的身份

GitHub Deploy Key 是自动化系统的 push 身份。Cron 跑起来的 report.py 没有人在终端输密码，必须有一把不交互的 key 才能 push。这一类资产的关键是权限最小化——只授写权限到单一仓库，私钥不进版本库不进 .env。

### 3.4 调度：把分级变成时间表

cron.yml 把方法论里的 P0/P1/P2 分级变成 Hermes 真实的定时任务。这一类资产的关键是频率与资源约束的平衡——P0 跑得快、P2 跑得全，两者错峰避免资源冲突。

### 3.5 四类资产对照表

| 资产 | 格式 | 读者 | 关键点 |
|------|------|------|--------|
| 场景库 | Markdown 自然语言 | AI Agent | 字段对齐 Tool 接口 |
| 配置 | YAML | 代码层（Tool） | 按 key 取值，注释清晰 |
| 凭证 | SSH 密钥 | Git push | 权限最小化、不交互 |
| 调度 | YAML 时间表 | Hermes | 频率与资源平衡 |

## 4. 触发链路统一：Cron 与 CLI 走同一条路

<img src="imgs/aicmigr-28-autotest-05-business-layer/1e57c734050b696c88bc2187fd26f5da_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

业务层填完后，系统能跑了。但"跑"有两种触发方式：Cron 自动触发、CLI 手动触发。一条关键设计让这两种触发零成本切换——它们对 Agent 来说是同一件事。

### 4.1 统一接口：自然语言 prompt

Cron 触发的动作是给 AI Agent 发一段自然语言："按 P0 跑一轮 MQTT 基础场景"。CLI 手动触发也是同样一段自然语言，只是来源不同。

#### (1) Cron 触发的内部机制

Cron 在 P0/P1/P2 三档某一档的时间点触发，触发动作是把那段 prompt 喂给 AI Agent。Hermes 把 prompt 喂给 AI Agent，Agent 加载 robustmq-chaos-test Skill 后按 Procedure 跑——跟手动 CLI 触发走的是同一条路。

### 4.2 为什么统一接口重要

统一接口带来一个直接的工程价值：手动验证 → 自动 7×24 的切换零代码改动。

#### (1) 链路装上 Cron 一行代码不用改

cron.yml 装好之后，Hermes 启动时自动加载。系统从"对话触发"变成 7×24，代码一行不用改，系统就升级了。第一期通道仅 CLI（25 篇拍过的决策），飞书第二期接入，Procedure 一字不改。

如果 Cron 和 CLI 走两条不同的链路，每次改 Procedure 都要两边改、两边测，工程负担翻倍。统一接口让"先手动验证、再自动跑"成为一个自然的工程节奏。

## 5. 业务层落地 Check List

<img src="imgs/aicmigr-28-autotest-05-business-layer/935e903e706491f540a96d02d1b9647a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把方法论压成一份可裁剪的 Check List，供项目阶段快速查阅。

### 5.1 填场景库时

| 项 | 检查内容 |
|---|---|
| 字段对齐 | 故障类型和参数是否跟 chaos.py 接口对得上？ |
| 通过标准字段 | exit_code / lost / p99_ms 字段名是否跟 client.py 返回 JSON 对得上？ |
| 优先级分级 | 场景是否按 P0/P1/P2 标注，Cron 按档加载？ |
| 模板复用 | 同协议同故障类型的场景是否套同样模板，只改关键参数？ |

### 5.2 填配置时

| 项 | 检查内容 |
|---|---|
| Key 命名 | chaos.py / client.py / report.py 读的 key 是否在 config.yml 都有？ |
| P0 极简 | P0 档是否只放最少 SDK（基础保障线，跑得越快越好）？ |
| 大矩阵后置 | 多版本矩阵是否留给 P1/P2 档？ |
| 仓库地址 | GitHub 仓库地址、分支、Deploy Key 路径是否写实？ |

### 5.3 配凭证时

| 项 | 检查内容 |
|---|---|
| 空 passphrase | 自动化系统用的 key 是否 `-N ""` 空密码、不交互？ |
| 单仓权限 | Deploy Key 是否只授写权限到单一仓库（test-reports）？ |
| 私钥隔离 | 私钥是否放 ~/.ssh，不进 .env、不进版本库？ |
| 链路验证 | clone + push 是否真实跑通一次，确认 key 链路活？ |

### 5.4 填调度时

| 项 | 检查内容 |
|---|---|
| 频率对齐分级 | P0 高频、P1 中频、P2 低频是否对齐 25 篇场景分级表？ |
| 错峰避让 | 同优先级多协议任务是否错峰（如 MQTT 与 mq9 错开 30 分钟）？ |
| Prompt 自然语言 | schedule 任务的 prompt 是否用自然语言，让 Agent 按 Procedure 跑？ |
| 与 CLI 同路 | Cron 触发的 prompt 是否与 CLI 手动触发用同一段自然语言？ |

### 5.5 链路验证时

| 项 | 检查内容 |
|---|---|
| 单点 ≠ 链路 | 每个 Tool 单独调通不等于链路通，必须做一次完整 7 步对话验证 |
| 故障期只记录 | 故障期 SDK 测试是否只记录不判断（避免误报）？ |
| 自愈才判断 | pass/fail 唯一依据是自愈验证窗口的 exit_code/lost/p99_ms？ |
| 报告真 push | 报告是否真 push 到 GitHub 仓库、URL 是否可访问？ |

## 6. 实战背景：27 篇留下的五件业务层待办

<img src="imgs/aicmigr-28-autotest-05-business-layer/b489f56a3b01b29f859413a367100c98_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

从本章开始进入第二部分实战演示。先回到上一篇末尾留下的五个待办，挨个看清楚为什么这五件事都是业务层、不是代码层。

### 6.1 五个待办回顾

回头看 27 篇末尾留的五个待办：

```text
1. 场景库 scenarios/ 是空的,Skill 没拿到具体场景没法跑完整流程。
2. 报告模板 report.md.j2 没写,report.py 调 Jinja2 加载会报模板找不到。
3. 套件配置 config.yml 是空的,SDK 矩阵、Chaosd 端点、报告仓库地址都没值。
4. GitHub Deploy Key 和 test-reports 仓库没创建,git push 真跑会失败。
5. cron.yml 没装,系统还是手动 CLI 触发,不是 7×24。
```

### 6.2 为什么都是业务层

这五件事都不是写代码层的事，是业务层的事。Tool 是能力，Skill 是说明书，这五件填进去才长出业务：测哪些场景、SDK 用什么版本、报告归档到哪、什么频率跑。

代码层通用，业务层定义具体测什么——这是 27 篇那个"先做代码层再做业务层"决策的另一边。下面挨个填。

## 7. 五件业务层资产挨个填

### 7.1 场景库 scenarios/

场景库是 Agent 拿到 Cron 触发或 CLI 手动触发后，真正知道该跑什么的地方。每个场景一个 Markdown 文件，自然语言描述，告诉 Agent：这个场景是哪个协议、用哪个故障类型注入到哪个节点、跑哪些 SDK 验证、通过失败标准是什么。

按 25 篇方案的 P0/P1/P2 分级，场景库目录长这样：

<img src="imgs/aicmigr-28-autotest-05-business-layer/5160a88d9ff8e9b0d5ff59d96987e1d6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/28_自动测试_05：添加业务层、跑通系统/5160a88d9ff8e9b0d5ff59d96987e1d6_MD5.jpg
用途：展示按 P0/P1/P2 分级组织的 scenarios/ 场景库目录结构
内容：场景库目录树，按协议（mqtt/、mq9/等）和优先级（p0-/p1-/p2-）分级组织，每个场景独立一个 Markdown 文件，Agent 加载时只取本轮要跑的几个
-->

#### (1) 写第一个场景的提示词

写第一个场景给 Claude Code 提示词：

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

#### (2) 提示词的关键是"给 Agent 看的可执行说明"

提示词的关键是"Agent 读完知道怎么调 Tool"，场景描述不是给人看的文档，是给 Agent 看的可执行说明。Claude Code 跑完产出 30 行左右的 Markdown，主要确认两件：故障类型和参数跟 chaos.py 接口对得上；通过标准的字段名（exit_code / lost / p99_ms）跟 client.py 返回的 JSON 对得上。

第一个场景写完之后，剩下几个场景套同样模板，只改协议、target、duration、SDK 矩阵这几个参数。每个场景独立成文件，Agent 一次只加载本轮要跑的那几个。

### 7.2 报告模板 templates/report.md.j2

report.py 第三步 git push 之前要把测试结果渲染成 Markdown，模板就是这个文件：

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

#### (1) 模板的灵魂是"协议兼容性归因"段

模板的灵魂是那段"协议兼容性归因"，25 篇那张归因表落到报告里，问题暴露和定位归因合在一份报告里给人看，这才是 25 篇方案"测试报告对外公开建立社区信任"那条决策的真实落点。

#### (2) 边界 case 的人工补刀

Claude Code 产出的模板有一处要手动改：`if all_failed` 那段加一句"如果三种归因都不命中，标记为待人工分析"。AI 在边界 case 上会偷懒，工程师手动收一下——这正是第 27 篇"工程师把关的边际价值"在业务层填充中的延续。

### 7.3 套件配置 config.yml

<img src="imgs/aicmigr-28-autotest-05-business-layer/024eda63723cec4028f98cf9b41ade42_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

chaos.py 读 Chaosd 端点、client.py 读 SDK 矩阵、report.py 读 GitHub 仓库地址，都从这个文件读：

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

#### (1) P0 刻意只放 2 个 SDK 的设计

P0 档刻意只放 2 个 SDK，P0 是基础保障线，跑得越快越好。大矩阵留给 P1/P2。这是 25 篇场景分级直接落到 SDK 矩阵的体现——分级不只是"测哪些场景"，是"每档用多大的 SDK 矩阵"，矩阵大小直接决定跑一轮的时长。

### 7.4 GitHub Deploy Key 和 test-reports 仓库

Deploy Key 这件事 25 篇拍过决策：只授写权限到 test-reports 单一仓库，私钥放 ~/.ssh，不进 .env 不进版本库。三步搞定。

#### (1) 第一步：生成密钥对

```bash
ssh-keygen -t ed25519 -f ~/.ssh/test-reports-deploy \
  -C "robustmq-chaos-test deploy key" -N ""
```

`-N ""` 是空 passphrase，因为这把 key 是给自动化系统用的，不能交互输入密码——这正是 Check List 第 5.3 节"空 passphrase"那条的实操落地。

#### (2) 第二步：在 GitHub 配置 Deploy Key

在 GitHub 上创建一个新的空仓库 test-reports（public，这是 25 篇方案"完全公开建立社区信任"那条决策的物理体现）。然后进 Settings → Deploy keys → Add deploy key，把 ~/.ssh/test-reports-deploy.pub 内容贴进去，勾选 Allow write access。

#### (3) 第三步：初始化仓库本地状态

确保 report.py 第一次 push 能成功：

```bash
GIT_SSH_COMMAND="ssh -i ~/.ssh/test-reports-deploy" \
  git clone git@github.com:<your-org>/test-reports.git /tmp/test-reports-init

cd /tmp/test-reports-init
echo "# RobustMQ Quality Reports" > README.md
git add README.md

GIT_SSH_COMMAND="ssh -i ~/.ssh/test-reports-deploy" \
  git commit -m "init" && git push
```

push 成功这一步，Deploy Key 链路就活了，后面 report.py 跑出来的报告会一直 push 到这个仓库。这一步是 Check List 第 5.3 节"链路验证"的实操——先把 key 链路跑通，再让代码层用。

### 7.5 cron.yml 三档调度

cron.yml 把 P0/P1/P2 三档变成 Hermes 真实的定时任务。

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

#### (1) 频率与资源约束的平衡

P0 每两小时一次，MQTT 和 mq9 错开 30 分钟避免资源冲突。P1 每天凌晨 3 点跑一次故障场景。P2 每周日凌晨 4 点跑全量 SDK 矩阵。频率配置直接来自 25 篇场景分级表。

#### (2) Prompt 用自然语言对接 Procedure

prompt 用自然语言写，Hermes 把 prompt 喂给 AI Agent，Agent 加载 robustmq-chaos-test Skill 后按 Procedure 跑，跟 27 篇 hermes CLI 手动触发走的是同一条路——这正是第 4 章"触发链路统一"那条方法论的物理体现。

## 8. 链路真跑：用一段对话跑完整个 P0 测试

<img src="imgs/aicmigr-28-autotest-05-business-layer/20da27c0e8a59340021d611886b78317_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-28-autotest-05-business-layer/d509b2089df400da364de74727194705_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

业务层填完，该看真东西了。

<img src="imgs/aicmigr-28-autotest-05-business-layer/9e8731e33a9ed7d60b571e29bf010631_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/28_自动测试_05：添加业务层、跑通系统/9e8731e33a9ed7d60b571e29bf010631_MD5.jpg
用途：展示业务层填完后系统进入真实运行验证阶段的总览
内容：呈现业务层五件事（场景库、报告模板、config.yml、Deploy Key、cron.yml）就位后，进入 Hermes 真实运行阶段，从"代码就绪"过渡到"系统真跑"
-->

### 8.1 先看一眼 Tool 和 Skill 都加载了

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

5 个 Tool 加 1 个 Skill 全部识别，Hermes 看到了它们。

### 8.2 用对话跑一次完整测试

<img src="imgs/aicmigr-28-autotest-05-business-layer/0e1fe89bd38ca2a765b952aadc18b618_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/28_自动测试_05：添加业务层、跑通系统/0e1fe89bd38ca2a765b952aadc18b618_MD5.jpg
用途：展示进入 Hermes 交互模式手动跑 P0 场景的对话触发过程
内容：在 hermes 交互终端输入"按 P0 跑一轮 MQTT 基础场景"，验证整个系统在真实链路上的闭环能力，作为装 Cron 自动跑之前的手动验证步骤
-->

进交互模式，手动跑一遍 P0 场景，这一步是验证整个系统真的能闭环，等手动跑通，Cron 就能放心装上自动跑。

```
$ hermes
> 按 P0 跑一轮 MQTT 基础场景
```

下面是 Agent 真实跑出来的对话。每一轮 Agent 调一个 Tool，把 Agent 的判断和 Tool 返回的关键字段贴一下，让读者看到链路真的串起来。

#### (1) 第 1 轮：加载 Skill，前置检查

```
Loaded skill: robustmq-chaos-test

Calling cluster(action="status")...
→ {"running": false}
```

环境干净，可以起集群。

#### (2) 第 2 轮：起集群

```
Calling cluster(action="start")...
→ {"status": "running", "endpoint": "127.0.0.1:1883",
   "data_dirs": ["/tmp/rmq-xxx-1", "/tmp/rmq-xxx-2", "/tmp/rmq-xxx-3"]}
```

3 节点集群健康，broker-1 是 leader。

#### (3) 第 3 轮：加载场景，打基线 snapshot

```
Loaded scenario: scenarios/mqtt/p0-broker-kill-leader.md

Calling observability(action="snapshot")...
→ {"timestamp": "...", "metrics": {...}, "logs": {...}}
```

基线已记录。

#### (4) 第 4 轮：注入故障

```
Calling chaos(action="inject", fault_type="broker-kill",
              target="broker-1", duration_seconds=30)...
→ {"fault_id": "fault-abc123", "status": "active"}
```

broker-1 已被 kill，开始故障期 SDK 测试（只记录，不判断）。

#### (5) 第 5 轮：故障期 SDK 测试

```
Calling client(action="run", sdk="python", version="3.11",
               scenario="basic-pubsub", cluster_endpoint="127.0.0.1:2883")...
→ {"exit_code": 1, "lost": 47, "p99_ms": 2300, "errors": [...]}
```

故障期 Python SDK 有丢消息，符合预期（只记录）。

```
Calling client(action="run", sdk="go", version="1.21",...)
→ {"exit_code": 1, "lost": 52, "p99_ms": 2100,...}
```

#### (6) 第 6 轮：自愈验证

故障窗口结束，Chaosd 自动 recover，等 broker-1 健康再等 60 秒，跑自愈验证：

```
Calling chaos(action="recover", fault_id="fault-abc123")...
→ {"status": "recovered"}

Calling cluster(action="status")...
→ {"healthy_brokers": 3}
```

broker-1 已恢复健康，等 60 秒进入自愈验证窗口。

```
... waiting 60s...

Calling client(action="run", sdk="python", version="3.11",...)...
→ {"exit_code": 0, "lost": 0, "p99_ms": 38}

Calling client(action="run", sdk="go", version="1.21",...)...
→ {"exit_code": 0, "lost": 0, "p99_ms": 41}

所有 SDK 自愈验证通过。
```

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

本轮完成。

#### (8) 七个动作把链路串起来

整轮 7 个动作走完。这就是 25 篇方案文档里那张时序图的真实运转，cron.yml 没装，用 CLI 手动触发了一次，但这条链路装上 Cron 之后一行代码不用改就变 7×24。

单点验证（每个 Tool 单独调）和链路验证（一次完整 7 步对话跑完）是两件事——前者只能证明每个 Tool 活着，后者证明整条链路真的串得起来。Cron 上线前必须做链路验证，这是 Check List 第 5.5 节的硬规则。

## 9. 从对话触发到 7×24：零代码切换

<img src="imgs/aicmigr-28-autotest-05-business-layer/d588e8f975e3d73cf2ca643ff3328b3a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

到这里整体开发就完成了，代码在：https://github.com/robustmq/robustmq/tree/main/chaos-test 。值得停下来多看一眼。

<img src="imgs/aicmigr-28-autotest-05-business-layer/af26f67bc52dd1ca05ce7705465a8093_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/28_自动测试_05：添加业务层、跑通系统/af26f67bc52dd1ca05ce7705465a8093_MD5.jpg
用途：展示从需求到业务层填充的完整工程链路追溯图
内容：呈现 24 篇一句话需求 → 25 篇两份评审级文档 → 26-27 篇长出代码 → 28 篇填业务层的全链路，每一步代码、配置、判断都可向上游决策追溯，体现"工程链路活起来"的样子
-->

### 9.1 工程链路活起来的样子

这条链路上每一步都有迹可循。从 24 篇一句话需求，到 25 篇两份评审级文档，到 26-27 篇长出来的代码，到刚才填进去的业务层。任何代码、配置、判断，都能追溯到上游某条决策。这不是文档本身有多漂亮，这是工程链路活起来的样子。

### 9.2 Cron 装好，系统升级到 7×24

cron.yml 已经装好，Hermes 启动时会自动加载。系统现在正式从"对话触发"变成 7×24，代码一行不用改，系统就升级了。第一期通道仅 CLI（25 篇拍过的决策），飞书第二期接入，Procedure 一字不改。

### 9.3 整套打法跑完一轮闭环

到这一篇为止，从 24 篇理解 Hermes、25 篇两次翻译拆需求和方案、26 篇跑通 Hello World、27 篇让代码长出来、28 篇填业务层让系统活起来。整套打法在 RobustMQ 上跑完了一轮闭环。

## 10. 小结与思考

### 10.1 小结

这一篇做了五件业务层的事：写场景库、写报告模板、填 config.yml、生成 Deploy Key 和 test-reports 仓库、装 cron.yml 三档调度。每件事单独看都不复杂，合在一起把 27 篇那六段代码从"能调"变成"真跑"。

代码层是能力，业务层是定义，场景才是业务。代码层是通用 Hermes 编程，换个项目能复用；业务层是当前这个具体项目的定义，跟 RobustMQ 绑死。先做代码层，再填业务层，工程上更稳。下次再做一个类似系统（给另一个开源项目加自动化测试），代码层 80% 能搬，业务层完全重写。

跑通系统不是终点。第二期飞书接入、更多场景库、Quality Dashboard 前端，都是在这条已经活起来的链路上往外长。但今天看着报告 push 进 GitHub 这一刻，值得停下来。

### 10.2 思考

回想最近做过的一个项目，如果按这一篇的"代码层 vs 业务层"分一下，哪些代码是通用能力，哪些是业务定义？如果有一天要把这个项目搬到另一个公司、另一个客户、另一个相似场景，代码层能搬多少、业务层要重写多少？
