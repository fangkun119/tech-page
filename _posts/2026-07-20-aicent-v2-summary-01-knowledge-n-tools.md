---
title: AI编程方法(2)：核心摘要 01 - 知识和工具
author: fangkun119
date: 2026-07-20 00:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-v2-summary-01-knowledge-n-tools/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---
<!--
aicent-v2-summary-01-knowledge-n-tools
AI编程方法（V2）：核心摘要（1）- 知识更新
-->

## 1. 知识更新

### 1.1 AI 编程市场观察和工具全景

<img src="imgs/aicent-v2-05-ai-dev-tool-trend-2026/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 05：AI 编程市场观察和工具全景]({% post_url 2026-07-19-aicent-v2-05-ai-dev-tool-trend-2026 %})**

#### (1) 一句话主旨

焦虑的根源不是工具太多，是**没有认知锚点**。常见误区——把"用得好 AI"等同于"知道更多技巧"：

**用得好 = 知道目的 + 用对工具 ≠ 知道更多工具**

#### (2) 认知三阶段：多数人卡在 ②→③

| 阶段       | 心态                               | 天花板      |
| -------- | -------------------------------- | -------- |
| ① 当代码工具  | Copilot 副手，只做代码生成器               | ~1.5x 提速 |
| ② 盲目追工具  | prompt/agent/hook/MCP/Skills 全都学 | 越学越焦虑    |
| ③ 当工作流的事 | AI 介入需求→设计→开发→测试→上线→监控→迭代        | 数倍跃迁     |

业界共识在 ③，但**"有共识、缺方法"**，多数人卡在 ②→③ 的过渡。

#### (3) 七层架构：一张工具地图

| 层 | 类比传统工程 | 代表 | 核心认知 |
|---|---|---|---|
| ① 模型层 | 语言/运行时 | Claude/GPT/Gemini | 原料，不是工具 |
| ② 工具层 | IDE | Cursor/CC/Copilot/Agent SDK | IDE vs CLI 是场景差异，不是强弱 |
| ③ 配置约束层 | 规范+pre-commit+CI 门禁 | CLAUDE.md/Skills/Hooks/Permission | 决定你是用户还是工程师 |
| ④ 协作机制层 | 团队分工+并行+评审 | Subagents/Worktrees/Plan Mode | 单兵 → AI 团队 |
| ⑤ 协议生态层 | USB-C 统一接口 | MCP/gh CLI | 代码助手 → 工程助手 |
| ⑥ 工作流层 | DevOps 流水线 | `claude -p`/Actions/事件触发 | 人不在循环里 |
| ⑦ 方法论层 | 设计模式+架构原则 | SDD/Harness/Vibe Coding | 工具会迭代，方法论持久 |

#### (4) 七层协作：三个场景看哪层主导

| 场景 | 主导层 | 关键工具 |
|---|---|---|
| 写新模块 | 方法论+工具 | SDD/Spec-Kit/Claude Code/Hooks |
| 大型项目改造 | 配置+协作 | Subagents/Permission/Hooks/MCP |
| 接入 CI/CD | 工作流 | Actions/`claude -p`/gh CLI |

**反直觉**：改造场景下，约束机制（Permission/Hooks）比工具本身更重要——多数人把重心放在"用什么工具写代码"是用错了。

#### (5) 新工具判断：三问 + 一特例

| 问题 | 判断什么 |
|---|---|
| ① 它属于哪一层？ | 定位 |
| ② 在这一层比现有工具好在哪？ | 差异 |
| ③ 当下场景需要它吗？ | 相关性 |

三问过滤 95% 噪音。

**特例：结构性变化立刻学**（跳过三问）——改变某一层基本玩法的：
- MCP 重写协议层（外部工具的连接方式被统一）
- SDD 重写方法论层（协作起点从代码退回 spec）

#### (6) 复盘

- 错把"追工具"当"用工具"，是焦虑循环的源头
- 七层架构是**过滤锚点**，不是学习清单
- 真正的资产 = 判断力 + 工作流，**不是工具数量**
- **用基础工具把工作流跑通并反复打磨** > 不断追新工具
- "会用 AI 玩的人很多，会用 AI 把事情做成的人很少"

### 1.2 AI自动化的边界与人加AI协作的新工作流

<img src="imgs/aicent-v2-06-automation-boundary-human-ai-workflow/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 06：AI自动化的边界与人加AI协作的新工作流]({% post_url 2026-07-19-aicent-v2-06-automation-boundary-human-ai-workflow %})**

#### (1) 一句话主旨

工作流确实变了，但变的方式不是 AI 全自动接管，是**人加 AI 协作的新工作流**。Bun 6 天 96 万行不是"AI 自主"的神话，是"协作做到极致"的范本——工程铺垫深 + 决策全靠人 + 测试做 verification gate。

#### (2) 工作流变了：三个维度

| 维度 | 传统 | AI 时代 |
|---|---|---|
| 产出物 | 给人看（PRD / 设计文档 / 代码 / 测试报告）| 给 AI 看（Spec / CLAUDE.md / Skills / Hooks）|
| 协作方式 | 人对人顺序交接，像流水线一节扣一节 | 人加 AI 共同体，每个阶段同时在场 |
| 反馈循环 | 靠人发现（小时 / 天 / 周级）| 工程化（Permission / Hooks / Headless，秒 / 分钟级）|

每个产出物都有**两个读者：人和 AI**——这是工作姿态的根本性调整。

#### (3) 自动化的真实边界：30% 提速是顶级水平

| 维度 | 真实情况 |
|---|---|
| 做得到 | 边界清晰 + 可验证 + 有参考实现的迁移（Bun Zig→Rust、Nubank 单体→微服务）、修测试、issue 处理 |
| 做不到 | 从零建大型软件、写 PRD、架构评审拍板、模糊需求消歧 |
| 真实提效 | **30%**（IBM+AWS 数据），10x / 20x 是愿景不是现状 |
| 当前瓶颈 | **可靠性，不是能力**——demo 一次容易，持续 100 次难 |

咨询公司卖愿景（麦肯锡 Level 4）、AI 厂商卖 token（Anthropic/OpenAI）、产品公司卖工具（Devin/Cursor），**都不在卖现实**。

#### (4) Bun 案例真相：五件事拆穿神话

| # | 真相 | 反直觉点 |
|---|---|---|
| ① | **结构化迁移**，不是从零建造 | Zig 原码是完整参考实现，翻译 ≠ 创作 |
| ② | **工程铺垫极深** | 300 条翻译规则、智能指针抽象、`bun_collections` crate 预先就位 |
| ③ | **人始终在循环** | Sumner 全程决策：何时启动/停、合什么、Phase A/B 重点 |
| ④ | **99.8% 测试通过 ≠ 生产可用** | 测试只能证明 bug 存在，不能证明不存在；真实评分要等 6-12 个月 |
| ⑤ | **Anthropic 自家 dogfooding** | Bun 团队的隐性知识是普通团队没有的，数字是"上限"不是"基线" |

#### (5) AI 时代真正的能力：工作流设计

| 能力 | 命运 |
|---|---|
| 工具熟练度（Cursor / CC / Cline）| 工具半年一迭代，永远追新疲惫 |
| Prompt 技巧（少样本 / 思维链 / 角色扮演）| 被模型进化吞掉 |
| **工作流设计**（产出物 + 协作 + 反馈循环）| **跨工具、跨模型，随 AI 进化升值** |

Bun 的关键是 Sumner 设计的协作工作流，不是"会用 Claude Code"。把同一个 CC 给没有这套设计能力的工程师，做不出 Bun 效果。

#### (6) AI 时代的红利：驾驭完整工作流的门槛被拉低

| 时代 | 独立交付完整项目的门槛 |
|---|---|
| AI 前 | 每个维度都精通到"自己能写"——Carmack / Linus / Notch 级别，百里挑一 |
| AI 后 | 每个维度都精通到"**能判断 AI 写得对不对**"——能力曲线平缓得多 |

**AI 降低的是技术门槛，没降低判断门槛**——判断力只能在真实项目里反复跑、反复踩坑、反复复盘才能建立。

#### (7) 复盘

- Bun 不是 AI 自主的证据，是协作做到极致的范本——**后者今天就能学**
- 工作流变了三件事：产出物给 AI 看、协作是人+AI 共同体、反馈被工程化
- 自动化边界清晰：**30% 是顶级水平，10x 是营销话术**
- 工作流设计能力 = AI 时代的真正壁垒——工具熟练度和 Prompt 技巧都会被吞掉
- **门槛降低的是技术，不是判断**——判断力只能在真实项目里被虐出来

### 1.3 SDD 和 Harness - AI 编程的两大支柱

<img src="imgs/aicent-v2-07-sdd-n-harness/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 07：SDD 和 Harness - AI 编程的两大支柱]({% post_url 2026-07-19-aicent-v2-07-sdd-n-harness %})**

#### (1) 一句话主旨

SDD 解决"想"的问题，Harness 解决"做"的问题——**AI 不是被信任的，是被工程化使用的**。

#### (2) 两大支柱的定位

| 维度 | SDD（规格驱动） | Harness（挽具） |
|---|---|---|
| 解决什么 | 想（需求侧） | 做（执行侧） |
| 把什么工程化 | "想清楚自己要什么" | "AI 怎么做" |
| 类比传统 | 需求规格说明书 + 接口契约 | CI/CD + code review + 权限管理 |
| 落地工具 | Spec-Kit / OpenSpec | CLAUDE.md / Skills / Permission / Hooks |

少一个价值会被稀释：只用 SDD 拦不住 AI 跑偏，只用 Harness 拦不住 AI 做对了你没真正要的东西。**SDD 告诉 AI 做什么，Harness 保证 AI 做对**。

#### (3) 凭什么是这两个

选支柱三条标准：**成熟、可教、覆盖完整工程**。其他方法论都差一些火候：

| 方法论 | 为什么不够格作为主轴 |
|---|---|
| Vibe Coding | 姿态不是方法论 |
| Context Engineering | 没有标准工具和成熟工作流 |
| ReAct / TAO | 适用范围窄，只适合自主决策型 Agent |
| Test-Driven AI | 只是 SDD 落地的一步 |
| Plan-then-Execute | 已在 Harness 的 Plan Mode 里落地 |
| AI-First Architecture | 概念太新，没有可操作实践 |

学方法论不在多，在精——先把这两根支柱跑熟，再判断其他方法论的位置。

#### (4) SDD 三种落地模式

| 维度 | Spec-Kit 自动化 | OpenSpec 手动控制 | 轻量手工模式 |
|---|---|---|---|
| 全局视角 | AI 推断（失真严重） | 永远在人手里 | 永远在人手里 |
| AI 误差 | 两轮叠加（80%×80%=64%） | 一轮 | 一轮 |
| 任务粒度 | AI 决定，偏大 | 人决定，适中 | 最小（函数/几十行） |
| 发现问题时机 | 最后 review（回溯成本大） | 每任务后立即 review | 每段后立即 review（最早） |
| 工程师角色 | 像产品经理 | 像团队 leader | 工程师本人（只外包敲键盘） |
| 能力要求 | 相对低 | 高 | 最高（不能藏拙） |

最关键两个维度：**全局视角在谁手里 + AI 误差放大倍数**。自动化模式两轮推断相乘是根本问题。

#### (5) SDD 选型

| 场景 | 推荐模式 |
|---|---|
| 新手+新项目+中等复杂度 | Spec-Kit 自动化 |
| 资深+系统级新项目 | OpenSpec 手动控制 |
| 资深+熟悉项目+增量/关键路径 | 轻量手工模式 |
| 老项目改造 | OpenSpec delta spec |
| 写 PR / 开源贡献 | OpenSpec delta spec |
| 团队协作 | 混用（tasks.md 对齐 + 关键任务手动） |

三种模式是**互补关系不是替代关系**——场景不同切换用。

#### (6) Harness 六大工具

| 类别 | 工具 | 作用 |
|---|---|---|
| 项目规则（基础） | CLAUDE.md | AI 一上来就懂项目 |
| 能力杠杆 | Skills / Slash Commands | 调用封装的可复用能力 |
| 上下文隔离 | Subagents | 在隔离上下文里工作，不污染主对话 |
| 前置安全网 | Plan Mode | 重要操作先规划，确认后再动手 |
| 中置安全网 | Permission | 在边界内自主（allow / ask / deny） |
| 后置安全网 | Hooks | 做完后自动验证（PreToolUse / PostToolUse） |

**没有 Hooks 还是手工作坊，有了 Hooks 才进入工业化**。

#### (7) 克制原则 + 落地三步

不是所有工具都必须上：

| 场景 | 必选工具 |
|---|---|
| 轻量手工模式 | CLAUDE.md + Skill |
| 批量自动化 | Hooks + Permission |
| 改造大型项目 | Subagents + Permission + Hooks |

落地三步（按顺序走）：

1. **写 CLAUDE.md**——最小投入（半小时），立竿见影
2. **选一种 SDD 模式开始**——撑过第一次别扭感，正反馈一旦建立就内化成默认工作方式
3. **逐步加 Harness 工具**——Hooks → Permission → Subagents → Plan Mode → Skills/Slash Commands

#### (8) 复盘

- SDD 是**思考方法不是工具**——内化后不需要外部脚手架（资深工程师在熟悉项目上脑子里跑通即可）
- Harness 不在堆工具，**"该用才用"的克制才是真正的工程师姿态**
- 接受"AI 不被信任"这个假设 → SDD/Harness 是自然产物；不接受 → 觉得繁琐
- **SDD + Harness 是 AI 编程里 ROI 最高的两件事**——投入一次性，回报持续复利

### 1.4 从看清楚到做出来

<img src="imgs/aicent-v2-08-cognitive-summary-take-action-2026/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 08：从看清楚到做出来]({% post_url 2026-07-19-aicent-v2-08-cognitive-summary-take-action-2026 %})**

#### (1) 一句话主旨

教程传递的是**工具知识**，你缺的是**驾驭能力**。AI 编程不是降低门槛，是**转移了门槛**——从"会不会写代码"挪到"会不会驾驭 AI 产出的代码"。前者教程能教，后者教程教不了。

#### (2) 三层能力：你卡在哪一格

| 层 | 是什么 | 怎么获得 |
|---|---|---|
| ① 工具层 | CC/Cursor/Spec-Kit/Hooks 怎么用 | 免费教程遍地，门槛已被互联网拉平 |
| ② 驾驭层 | 让 AI 理解需求、定边界、跑工作流、纠偏、兜底、沉淀 | 真实项目里反复跑，**教程教不了** |
| ③ 价值层 | 把驾驭用回自己的场景产生价值 | 没人能替你回到你的场景 |

驾驭是**肌肉记忆，不是知识**——读《重构》不等于会在十万行遗留代码上做重构。

#### (3) 四类真实场景：驾驭长在哪里

| 场景 | 代表项目 | 逼你练出的能力 |
|---|---|---|
| 复杂系统改造 | OryxOS | 架构决策的判断力（挡住"看着对、其实埋雷"的方案） |
| 新项目从 0 到 1 | When | 工程链路完整性（模糊需求→spec→SDD→Harness→部署） |
| 老项目改造 | DifyPro | 增量改造的克制（读懂十万行陌生代码、不破坏契约） |
| 外部协作 | mq9 | 留下可审计印记（过 maintainer review、积累公开资产） |

**自学能给你知识，给不了你失败**——没有失败，就没有肌肉记忆。

#### (4) 经验导向产品，不是凭空许愿

| 路径 | 起步 | 成事概率 |
|---|---|---|
| 没驾驭经验直接做产品 | 从零摸索，试错成本高 | 大概率半途而废 |
| 有驾驭经验再做产品 | 把已经会的用一遍 | 高得多 |

经验是通用的，应用是个人的。**先有经验，再谈应用**。不做产品也合理——在公司业务上把驾驭用回去，同样产生巨大价值。

#### (5) 七条判断：站在地图上看清自己位置

| # | 判断 | 实战含义 |
|---|---|---|
| 1 | AI 编程是程序员旧角色的升级版 | 十年工程经验是驾驭的本钱，不是包袱 |
| 2 | 工具会过时，判断会留下 | 学工具短期、学方法论长期、练驾驭终身 |
| 3 | AI 自动化有清晰边界 | 不被 10x 话术忽悠，不错过 30% 真实提速 |
| 4 | 工作流是人加 AI 协作，不是全自动接管 | 设计协作工作流才是真能力 |
| 5 | 开源是 AI 时代被低估的复利资产 | 门槛降低、含金量提升、复利加快 |
| 6 | SDD 和 Harness 是当下成熟可教的两个方法论 | 不在多，在精，练到肌肉记忆 |
| 7 | 真实经验最终导向产品 | 经验是产品的前置 |

#### (6) 复盘

- 门槛转移：从"会不会写代码"挪到"会不会驾驭 AI 产出的代码"
- 驾驭是肌肉记忆不是知识：**只能在四类真实场景里被虐出来**
- 做产品不是凭空许愿：是驾驭经验自然导向的结果
- 站在认知地图上才知道自己在哪、要去哪、为什么去那里
- **工具会过时，判断会留下，驾驭会陪你一辈子**

## 2. 工具详解

### 2.1 Agent从技术上看到底是什么

<img src="imgs/aicent-v2-09-what-is-agent-technically/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 09：Agent从技术上看到底是什么]({% post_url 2026-07-19-aicent-v2-09-what-is-agent-technically %})**


> 把这篇讲给完全不懂的人听：**Agent 就是"你给目标，它自己搞定"的程序**。所有抽象都是这句话的展开。

#### (1) 一句话和公式

| 项 | 内容 |
|---|---|
| 一句话 | 能感知环境、自主决策、执行动作、基于反馈循环迭代的程序 |
| 公式 | Agent = LLM + Tools + Memory + Loop + Environment |
| 灵魂 | "自主决策的循环"——少了就不是 Agent |
| 类比 | 传统脚本 = 按 SOP 干活的操作员；Agent = 给目标就能搞定、还会换办法的员工 |

#### (2) 五组件速查

| 组件 | 说人话 | 传统类比 | 关键判断 |
|---|---|---|---|
| LLM | 大脑（决定下一步） | CPU | 没它"自主"立不住 |
| Tools | 手（伸出去干活） | 系统调用 | 决定 Agent 能力的边界 |
| Memory | 记性（短期必需，长期高阶） | 内存+硬盘 | 短期必需，长期才会成长 |
| Loop | 心跳（ReAct：决策→行动→观察→再决策） | 主循环 | 80% 工程复杂度都在这 |
| Environment | 场地（文件/网络/数据库/接口） | 运行时 | 决定能力上限和安全底线 |

#### (3) 三对最易混

| 对比 | 差别（一句话） |
|---|---|
| Agent vs LLM | LLM 是 CPU，Agent 是装好系统加应用的整台电脑 |
| Agent vs Chatbot | Chatbot 缺 Tools 和 Loop，加回来就是 Agent |
| Agent vs Workflow | Workflow 步骤写死（确定性），Agent 自己决定怎么走（探索式） |

#### (4) 用尺子量六个产品

| 产品 | 凑齐五组件？ | 一句话定位 |
|---|---|---|
| ChatGPT 经典版 | 只 LLM+Memory | chatbot，不是 Agent |
| ChatGPT + 插件 | 凑齐 | 加上 Tools/Loop/Environment 才跨线 |
| Claude Code | 五格全满 | 工业级完整实现 |
| Cursor | 全满（贴 IDE） | 跟 IDE 深度集成的 Coding Agent |
| LangChain | 不适用 | 是"做 Agent 的工具盒"，不是 Agent |
| Hermes | 全满 | 核心创新在跨会话长期记忆 |
| AutoGPT | 全满但 Loop 弱 | 历史价值 > 技术价值 |

#### (5) 从单 Agent 到 Agent OS

| Agent OS 要管 | 对应单 Agent 的组件 |
|---|---|
| 进程管理（起停重启） | Loop |
| 工作空间隔离 + 沙盒 | Environment |
| 多模型路由 | LLM |
| 可观测性 | 全局 |

一句话：**Agent 是应用程序，Agent OS 是操作系统**。

#### (6) 三层技术栈

| 层 | 类比 | 2026 判断 |
|---|---|---|
| L1 模型层 | 硬件/CPU | 顶尖实验室在卷，普通工程师进不去 |
| L2 Agent OS 层 | 操作系统 | **最值得投入**（2026–2028 定义窗口） |
| L3 应用层 | 应用程序 | 门槛最低，L2 成熟后大爆发 |

#### (7) 这把尺子的四个价值

| 价值  | 说明                   |
| --- | -------------------- |
| 判断  | 看到号称 Agent 的产品能立刻辨真假 |
| 设计  | 做系统知道从哪五组件入手         |
| 比较  | 能讲清两个产品的本质差异         |
| 沟通  | 跟同事讨论时知道大家在说同一件事     |

### 2.2 Spec-Kit 和 OpenSpec 的原理与使用

<img src="imgs/aicent-v2-10-spec-kit-openspec-tutorial/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 10：Spec-Kit 和 OpenSpec 的原理与使用]({% post_url 2026-07-19-aicent-v2-10-spec-kit-openspec-tutorial %})**

#### (1) 用一句话讲清楚 SDD

**SDD = 把需求从 chat 历史搬进项目仓库，让 AI 读规约而不是猜你的心思**。

spec 不是写给人归档的需求文档，而是**写给 AI 的 system prompt**——更新 spec 等于更新 AI 的工作上下文。

#### (2) 为什么 prompt engineering 救不了 vibe coding

vibe coding 四个痛点，本质都是"需求只活在 chat 里"：

| 痛点 | 表现 | 传统软件工程的解药 |
|------|------|---------------------|
| 需求漂移 | 跨会话决策被遗忘 | 需求基线 |
| 架构失控 | 多次迭代代码变乱麻 | Architecture Review |
| 回退困难 | 做错不知回到哪 | git tag 对应需求版本 |
| 无法协作 | 十人十种风格 | Coding Convention |

**结论**：仅靠 prompt 不够，需要在 prompt 之上加一层结构化、版本化的"需求层"。

#### (3) SDD 与静态 context 的本质差异

| 维度 | Cursor Rules / AGENTS.md / Project docs | SDD 工具 |
|------|------------------------------------------|----------|
| 本质 | 静态 context（员工手册） | 工作流（带门禁的流程） |
| 给 AI 什么 | 背景知识 | 背景 + 阶段化 artifact |
| 约束工作流 | ❌ 不约束 | ✅ 强制 specify→plan→implement |
| 持久化 | 部分持久 | spec 跟代码一起版本管理 |

#### (4) Spec-Kit vs OpenSpec：一表看清两个流派

| 维度 | Spec-Kit（重武器） | OpenSpec（轻武器） |
|------|--------------------|---------------------|
| 出品方 | GitHub | Fission-AI |
| 核心范式 | **spec-as-source**（spec 是源头） | **delta-spec**（只写增量 diff） |
| 流程 | 4 阶段闭环 specify→plan→tasks→implement | 3 态流程 Propose→Apply→Archive |
| 刚性 | 强制门禁，不可跳 | action-based，灵活编辑 |
| 核心命令 | 6 必须 + 3 可选（`/speckit.` 前缀） | core + 扩展 profile（`/opsx:` 前缀） |
| 宪法约束 | `constitution.md` 非协商原则 | 无，靠 `project.md` |
| 增量描述 | 每次重写整份 spec | ADDED / MODIFIED / REMOVED 三标记 |
| 并行能力 | 弱（线性流程） | 强（多 change folder 互不干扰） |
| 最适合 | greenfield 中大项目 | brownfield 增量改造 |
| 输出量 | 厚重（单项目上千行） | 轻量（单 change 负担小） |

#### (5) 选型决策：四个问题定位工具

| 问题 | 选 Spec-Kit | 选 OpenSpec | 都不选 |
|------|-------------|------------|--------|
| 项目阶段 | greenfield 中大项目 / 大重构 | brownfield 增量 | — |
| 团队容忍度 | 需要学方法论 | 成熟、追求灵活 | — |
| 任务颗粒度 | 多模块多人 | 单 feature / 个人改动 | 小工具、单文件 |
| 项目周期 | 长期（半年+） | 短期（几周） | 快速原型 |

**学习路径建议**：先 Spec-Kit 学范式，再 OpenSpec 体验增量风格。

#### (6) 关键术语速查

| 术语 | 一句话解释 |
|------|------------|
| vibe coding | 凭"感觉"跟 AI 沟通、生成、调整的工作方式 |
| SDD | Spec-Driven Development，spec 是事实来源 |
| spec-as-source | spec 是源头，下游一切从 spec 派生 |
| delta-spec | 只写这次改动相对于现有系统的差异（spec 的 git diff） |
| constitution | Spec-Kit 的项目宪法，非协商原则，写一次不改 |
| artifact | 每个阶段产出的正式文件（spec/plan/tasks 等） |
| greenfield | 从零开始的新项目 |
| brownfield | 已有代码基础的增量改造项目 |

#### (7) 一句话记忆锚点

> **SDD 是 AI 编程从"作坊"到"工程"的关键一步，就像版本控制之于传统编程——能跑，但不可持续。**

工具会迭代，范式持久：**spec-as-source** 和 **delta-spec** 是 SDD 的两个主要流派，掌握它们就能快速判断任何新 SDD 工具的位置。

### 2.3 AI编程实用Skills调研与沉淀

<img src="imgs/aicent-v2-11-skills-research-and-sedimentation/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 11：AI编程实用Skills调研与沉淀]({% post_url 2026-07-19-aicent-v2-11-skills-research-and-sedimentation %})**

#### (1) 一句话理解 Skill

**Skill = SOP + 工具箱**：把"按什么流程做"和"用什么工具做"打包进一个目录加一份 `SKILL.md`。

类比：给新员工发一本《入职手册》加一张工具卡——手册告诉他步骤（先校验再执行），工具卡告诉他工具（PDF 用 pdfplumber）。以后遇到同类任务，他自动按这套走，不用每次重新教。

#### (2) Skill 与四个相邻概念的边界

| 概念 | 解决的问题 | 一句话定位 |
|------|-----------|-----------|
| MCP | AI 能调用什么外部能力 | 工具供给侧 |
| Skill | AI 按什么标准做事 | 行为标准 |
| Subagent | 复杂任务怎么并行拆分 | 任务分发 |
| Plugin | 一组能力怎么打包分发 | 整体安装 |
| Command | 用户怎么显式触发 | 斜杠入口 |

**判断口诀**：固化流程用 Skill；接入外部系统用 MCP；并行处理用 Subagent；打包分发用 Plugin。

#### (3) 渐进式加载——Skill 能规模化的关键

| 层级 | 内容 | Token 占用 |
|------|------|-----------|
| 元数据 | name + description | ~100 token |
| SKILL.md | 核心指令 | 几千 token |
| 辅助资源 | references / scripts / examples | 按需加载 |

价值：一个 AI 实例能挂上百个 Skill 而不撑爆上下文。

**写 Skill 的核心约束**：SKILL.md 尽量短，详细内容放引用文件按需加载。

#### (4) 两类核心价值

| 类别 | 作用 | 判断标准 |
|------|------|---------|
| Capability Uplift | 配套外部工具，让 AI 学会新能力 | 能做之前做不了的事 |
| Encoded Preference | 纯文字指令，约束 AI 偏好 | 能做的事没变，做事方式变了 |

实战中两类常叠加：React 项目同时装 Frontend Design（偏好）+ React Best Practices（偏好）+ Webapp Testing（能力），美感、性能、测试三者叠加。

#### (5) 业界生态三层

| 层级 | 代表 | 特点 |
|------|------|------|
| 官方 | anthropics/skills（121k star） | 质量稳定、文档完整、跨工具兼容 |
| 社区库 | obra/superpowers、Garry Tan GStack、awesome-claude-skills | 实战导向、规模大 |
| 明星单 Skill | frontend-design（277k 装机）、agent-browser、skill-creator | 解决具体问题 |

**入门首选**：skill-creator（写新 Skill 的脚手架）、frontend-design（装机量最大，告别 AI slop）。

#### (6) 选 Skills 的判断框架

**五个判断标准：**

| 标准 | 关键问题 |
|------|---------|
| 场景匹配 | 这周的工作场景用得上吗？ |
| 真实痛点 | 解决的问题最近反复遇到？ |
| 价值类型 | Capability Uplift 还是 Encoded Preference？ |
| 来源信誉 | 官方或高 star 优先 |
| 试用验证 | 装上用一周看 AI 行为是否真变化 |

**三种反模式：**

| 反模式 | 症状 |
|--------|------|
| 跟风装明星 Skill | 装完发现根本不做那个场景 |
| 装一堆同类对比 | description 互相冲突，AI 不知用哪个 |
| 不读 SKILL.md | 出问题不知怎么改 |

#### (7) 沉淀自己的 Skills

**五个信号（何时该做 Skill）：**

| 信号 | 说明 |
|------|------|
| 同样操作做到第三次 | 模式已稳定，是沉淀的最佳时机 |
| 流程明确可重复 | 步骤每次都一样 |
| AI 默认做不好 | 反复出错的强信号 |
| 跨项目复用 | 放 `~/.claude/skills/` |
| 团队多人受益 | 提到团队共享仓库 |

**SKILL.md 标准结构：**

```text
YAML frontmatter（name + description）
├─ 目的
├─ 触发场景
├─ 工作流程（SOP）
├─ 约束和原则（禁区）
├─ 示例（输入 → 输出）
└─ 参考资源（references / scripts / examples，按需加载）
```

**description 写作四要点：**

| 要点 | 例子 |
|------|------|
| 直接讲场景 | "用 TDD 流程写代码" 而非 "处理代码" |
| 列关键词 | 动词+名词（生成、审查、API、数据库） |
| 写明用/不用 | "适用 Python，不适用 JavaScript" |
| 50-200 字 | 太短触发不准，太长占元数据 |

**三种 Skills 的存放位置：**

| 层级 | 位置 | 作用域 |
|------|------|--------|
| 个人 | `~/.claude/skills/` | 跨项目，只对自己 |
| 项目 | 项目仓库 `.claude/skills/` | 项目内，clone 即用 |
| 团队 | 团队共享仓库 | 团队范围，工程文化可执行 |

#### (8) 团队协作的关键实践

**起步四阶段：**

| 阶段 | 时长 | 动作 |
|------|------|------|
| 一 | 第 1 月 | 1-2 人尝鲜，建立种子 |
| 二 | 第 2-3 月 | 单项目全员推，沉淀教训 |
| 三 | 第 4 月起 | 抽到团队仓库，新项目默认用 |
| 四 | 半年-1 年 | 向社区贡献 |

**五种失败模式：**

| 失败模式 | 解决办法 |
|---------|---------|
| Skill 库过度膨胀 | 每月评审，删/合并 |
| 写成操作手册 | 背景知识放 `references/` |
| 和 CLAUDE.md 重复 | 通用约束放 CLAUDE.md，场景流程放 Skill |
| Skill 不维护 | 每个 Skill 指定 owner |
| Skill 取代思考 | 新问题先想清楚再固化 |

#### (9) 长期判断：Skill 是个人工程资产

| 资产类型 | 是否可移植 | 是否可规模化复用 |
|---------|-----------|----------------|
| 代码 | 跟项目走 | 否 |
| 文档 | 留在公司内网 | 否 |
| 经验（脑中） | 可带走 | 难，转移成本高 |
| **Skill** | **跨工具、跨公司** | **AI 自动按经验执行** |

**核心结论**：投入 Skill 不是临时工具学习，是工程师对自己长期资产的投资。从现在开始，每发现一个值得沉淀的工程模式就做成 Skill，五年后这些 Skill 会变成你最有价值的工作工具。

### 2.4 Superpowers深度介绍

<img src="imgs/aicent-v2-12-superpowers-deep-dive/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 12：Superpowers深度介绍]({% post_url 2026-07-19-aicent-v2-12-superpowers-deep-dive %})**

#### (1) 一句话定位

Superpowers **不是 SDD 工具，是 AI agent 的工程纪律框架**——把严肃工程师该做的事打包成 Skills，强制嵌进 agent 工作流。不是"给你用"，是"管住 agent"。

#### (2) 它要解决的真问题

| 表面现象 | 根因判断 |
|---|---|
| AI 写代码很快但很散 | 不是模型不够聪明，是 agent 缺工程纪律 |
| benchmark 强但实战差 | 把纪律训进模型只有大厂做得了 |
| 改一处悄悄坏两处 | 缺 spec、测试、review 的强制流程 |

两条路：训进模型（大厂专属）vs 装进工作流（人人能做）。Superpowers 走第二条，且走得最远。

#### (3) 三个关键词

| 关键词 | 含义 |
|---|---|
| agentic skills 框架 | Skills 是可复用、可扩展的工作流单元 |
| 软件开发方法论 | spec 优先、TDD、YAGNI、subagent、review |
| 强制嵌进工作流 | 拦住 agent 的"跳步"冲动 |

#### (4) Skill 是什么

**大白话**：把老司机经验写成 agent 能读懂的规则——一个 `SKILL.md` 文件（frontmatter 写"什么时候用" + 流程 + 检查清单 + 常见坑）。

**自动激活**：description 命中即激活，不靠用户手动调用（类比 CI/CD 管道）。

| 核心 Skill | 触发时机 |
|---|---|
| brainstorming | 用户说"我想做 X" |
| writing-specs | 头脑风暴结束 |
| writing-implementation-plans | spec 签字后 |
| subagent-driven-development | plan 签字后 |
| tdd | 写实现时刻 |
| debugging-systematically | 遇到 bug |
| root-cause-analysis | 修根因 |
| **writing-skills** | **元 Skill：让 agent 自己造新 Skill（最精妙）** |

#### (5) 完整工作流：六阶段

| 阶段 | 动作 | 通过条件 |
|---|---|---|
| ① Brainstorming | 问为什么做、给谁用 | 目标约束清楚 |
| ② 写 Spec | 分块展示，逐块签字 | 用户全部签字 |
| ③ 写 Plan | 拆到几分钟一个任务 | 清晰到新手能照做 |
| ④ Subagent 执行 | 独立 worktree，TDD 先红后绿 | 测试通过 |
| ⑤ 两阶段 Review | spec 合规 + 代码质量 | 两阶段都过 |
| ⑥ 收尾 | 跑全套测试 | 合并 / PR / 保留 / 丢弃 |

**对比原生 Claude Code：**

| 维度 | 原生 | 装 Superpowers |
|---|---|---|
| 写代码 | 半小时"看起来能用" | 一两小时"真的能用" |
| 修 bug | 再花一两小时修 | 基本不用修 |
| 总耗时 | 差不多 | 差不多 |
| 产出质量 | 工程纪律差 | spec + TDD + 双 review |

#### (6) 六条工程哲学

| 哲学 | 核心 |
|---|---|
| Agent as Contractor | 把 agent 当承包商，给 spec 严格验收，不当同事 |
| 两阶段 Review | 先验"做对了"再验"做好了"，顺序不能颠倒 |
| 真红绿 TDD | 先写测试看到红，再写实现看到绿 |
| YAGNI + DRY 写进 Skill | 从设计层强制，不靠 agent 自觉 |
| Worktree 隔离 | 用 git 管理人 + agent 协作 |
| 慢就是快 | 总耗时差不多，半年后可维护性天壤之别 |

#### (7) 用 5 组件抽象看设计

| 组件 | 改造? | 说明 |
|---|---|---|
| LLM | 复用 | 不带模型，跟最新模型一起进化 |
| Tools | 复用 | 不增工具，只约束使用方式 |
| **Memory** | **★ 改造** | **Skills 库 = 工程方法论沉淀** |
| **Loop** | **★ 改造** | **强制结构化流程（剧本 + 导演）** |
| Environment | 复用 | 跨 agent 工作 |

**关键发现**：只改 Memory 和 Loop，其他全复用 → 可移植性极强、跟 agent 演化解耦、核心价值聚焦 → 跨 20+ agent、涨到 20 万 stars 的根本原因。

#### (8) 与 Spec-Kit / OpenSpec 的边界

| 工具 | 定位 | 范围 | 形态 | Stars |
|---|---|---|---|---|
| Spec-Kit | 新项目 greenfield SDD | specify→plan→tasks→implement | CLI | 9 万+ |
| OpenSpec | 棕地 brownfield 增量改造 | propose→apply→archive（带 delta） | CLI | - |
| **Superpowers** | **AI agent 工程纪律框架** | **SDD + TDD + review + debug** | **Plugin** | **20 万+** |

**包含关系**：OpenSpec + Spec-Kit ⊂ Superpowers（SDD 只是 Superpowers 内部的一个 Skill）。

**组合建议**：理想组合是 Spec-Kit/OpenSpec 做 spec + plan，Superpowers 做 TDD + subagent + review，但要手工调整 Skill 激活规则，非开箱即用。

#### (9) 局限与长期判断

| 局限 | 具体表现 |
|---|---|
| 慢 | 小任务 overkill，不能自动判断何时跳过 |
| 重 | 小项目 / prototype / 一次性脚本过度工程化 |
| 加新 Skill 难 | 主仓库 PR 接受率低，自建多停留在 fork |
| 学习曲线 | 两三周才能感受产出质量差异 |

**演化时间线：**

| 时代 | AI 角色 |
|---|---|
| 2022-2023 Copilot | 代码补全 |
| 2024 Cursor / Claude Code | 编程助手，需不断引导 |
| 2025 Superpowers / SDD | 有纪律的承包商 |
| 2026 Multi-Agent | 自主软件工程师 |

**核心结论**：工具会过时，工程方法论不会。Superpowers 大概率是过渡产物，但 **"给 agent 装工程纪律"这个方向不会变**——严肃工程、大团队、复杂项目值得投入；小项目、prototype 绕开。

### 2.5 Hermes Agent深度介绍

<img src="imgs/aicent-v2-13-hermes-agent-deep-dive/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 13：Hermes Agent深度介绍]({% post_url 2026-07-19-aicent-v2-13-hermes-agent-deep-dive %})**

> 讲给不懂的人听：**Hermes 是跑在 VPS 上、24h 在线、越用越懂你的私人 AI 助手**。模型不变，但它自己长本事——核心是"closed learning loop"。

#### (1) 一句话主旨

Hermes Agent 的护城河不是模型，是**时间**：用得越久，skill 库越厚、记忆越深、迁移成本越高。**它把"模型能力的静态边界"变成了"随时间增长的能力飞轮"**。

#### (2) AI Agent 赛道三个流派

| 流派 | 代表 | 类比 | 关键判语 |
|---|---|---|---|
| IDE 内嵌 coding agent | Cursor / Claude Code / Windsurf | IDE 插件 | IDE 关掉 agent 就停 |
| chatbot 包装层 | ChatGPT API 套壳产品 | REST 客户端套壳 | 核心 logic 全在后端 LLM |
| **个人持久化 Agent** | **Hermes** / OpenClaw / SkyPilot | **24h 在岗的私人助理** | **跟设备解耦，跟人一起成长** |

差异不是"功能多少"，是**定位**的差异。

#### (3) Hermes 是什么 / 不是什么

| 是 | 不是 |
|---|---|
| 开源（MIT）、自部署 | 云服务 |
| 持久化（5$ VPS 即可跑） | 绑笔记本（关机即停） |
| 多平台（CLI + 20+ 消息平台 + IDE） | 只活在 IDE 或单一 UI |
| 多模型兼容（任意切换无 lock-in） | 绑某家 LLM |
| 多后端执行（本地/Docker/SSH/Modal/Daytona） | 只在本机跑 |

**关键属性 7 项**：开源 / 自我改进 / 持久化 / 多平台接入 / 多模型兼容 / 多后端执行 / 多人协作（spawn subagent）。

#### (4) 五个痛点 → Hermes 的机制

| 痛点 | Hermes 机制 | 传统工程类比 |
|---|---|---|
| 记忆缺失（每次重讲） | `MEMORY.md` + `USER.md` 注入 prompt | 用户 profile / CRM 档案，**但 Agent 自己写** |
| 不会学习（教过还不会） | 5+ tool calls 后自动写 `SKILL.md` | Runbook / SOP，**但 Agent 自己维护** |
| 跨平台脱节 | messaging gateway 接 20+ 平台 | API Gateway，session 在 gateway 层共享 |
| 不能持续（手动触发） | cron scheduler + 自然语言定义任务 | Linux cron / Airflow |
| 绑定设备（关机即停） | server-resident，5$ VPS / serverless | 后端服务，24h 在线 |

#### (5) 核心创新：closed learning loop

**一句话**：模型权重不动，但 Agent 自己积累 skill + 记忆，**用得越久越强**。

| 对照 | 能力天花板 |
|---|---|
| 普通 LLM-wrapper | 模型本身（不换模型就原地踏步） |
| Hermes | 模型 + skill 库（**随使用时间上升**） |

**核心类比——CI/CD 流水线**：

| CI/CD | Hermes |
|---|---|
| 代码 push | Agent 接任务 |
| CI 跑测试失败 → 反馈 | 执行中积累经验 |
| 工程师改代码 | 写 skill + 记忆 |
| 仓库质量提升 | 下次同类任务更顺 |
| **工程师改的是代码，不是编译器** | **Agent 学的是 skill，不是参数** |

**三个串起来的机制**：
- 记忆系统让 Agent **记得**（agent-curated memory）
- 技能系统让 Agent **会做**（autonomous skill creation）
- 执行反馈让 Agent **做得更好**（skill self-improvement during use）

#### (6) 三层架构（同构传统 Web 三层）

| 层 | 组件 | 传统类比 |
|---|---|---|
| ① 用户接入层 | CLI / TUI / Messaging Gateway / IDE(ACP) / cron / OpenAI 兼容 API | Nginx / API Gateway |
| ② 核心引擎层 | `AIAgent`（orchestration engine）：记忆 / 技能 / 学习循环 / Model Router / Subagent / 安全 | 业务服务层 |
| ③ 执行后端层 | 本地 / Docker / E2B / SSH / Singularity(HPC) / Modal / Daytona(serverless) | 数据库 + worker |

**两个关键工程抽象**：
- `ProviderTransport` ABC（LLM API 差异藏接口后）→ 类比 SLF4J / JDBC
- `prompt_builder.py` 运行时拼 6 片段（personality / memory / skills / context / tool-use / model-specific）→ 类比 Jinja2 模板引擎，**每个人的 system prompt 都不一样**

#### (7) 记忆 + 技能 + Subagent + 安全速查

| 机制 | 一句话 | 关键点 |
|---|---|---|
| `MEMORY.md` | 事实笔记，Agent 主动写入 | 定期 nudge 提醒沉淀 |
| `USER.md` | 结构化用户画像 | Honcho 辩证式建模，持续修正 |
| FTS5 跨会话搜索 | SQLite 全文搜索 + LLM 总结 | 搜得到几个月前的对话 |
| `SKILL.md` 四段 | What / How / Pitfalls / Verification | 给 LLM 看的指南，**不是代码** |
| autonomous curator | `hermes curator` 定期整理 skill 库 | 防止腐烂，兼容 agentskills.io 标准 |
| subagent | 隔离的子 Agent，并行处理 | zero-context-cost pipelines，主 Agent 只收结果，类比 worker pool |
| 安全 5 项 | 多 backend / 危险命令按钮确认 / OAuth 2.1 PKCE / OSV 扫描 / HMAC webhook | **生产级 Agent 不是 demo** |

#### (8) 场景：PR review 把所有机制一次用上

| 能力 | 在 PR review 里做什么 |
|---|---|
| 记忆 | "Remember: 我们用 FastAPI + SQLAlchemy，不允许 raw SQL" |
| 技能 | `code-review` SKILL.md 告诉 Agent 怎么看 diff / bug / security |
| cron | `hermes cron create "0 */2 * * *"` 每 2h 扫新 PR |
| 外部工具 | `gh pr diff` 拉 diff，回写评论 |
| webhook（可选） | GitHub 实时推送 PR 事件 |

**6 步上手**：装 gh CLI → 教规范 → 手动测一次 → 写 SKILL.md → 加 cron →（可选）webhook。

#### (9) Nous Research 的双重身份：两个闭环嵌套

| 闭环 | 层面 | 改什么 |
|---|---|---|
| 内层（4.6） | Agent 自己用 | skill 库（不改权重） |
| 外层（5.5） | Agent → 训练数据 → 新模型 → 更强 Agent | 模型权重 |

**车厂类比**：既造发动机（模型），又造整车（Agent），还自己跑赛道收集数据——**数据链路天然打通**。市面上绝大多数厂商只做一边。

#### (10) 适合 / 不适合

| 适合 | 不适合 |
|---|---|
| 个人开发者（跨平台 + 持久化 + 学习） | 只想快速问答（ChatGPT 够） |
| 小团队（PR review / 监控 / 知识管理自动化） | 只想要 IDE 代码助手（Cursor / CC 更直接） |
| AI 探索者（理解"Agent 怎么持续学习"） | 企业级 Agent OS（缺多租户/权限/审计） |
| 多平台重度用户 | |

**三个局限**：① Python 生态部署门槛高；② 设计哲学偏个人；③ 功能太多学习曲线陡。

#### (11) 费曼复盘

- Hermes 的"成长"不是改模型，是**自己长 skill + 自己记记忆**
- closed learning loop 是分水岭：**LLM-wrapper 用多久都还是模型本身，Hermes 越用越强**
- 三层架构同构传统 Web 三层——接入/引擎/执行各司其职
- 护城河不是模型，**是时间**：用得越久耦合越深，迁移成本越高
- 安全设计反映它是**生产级 Agent**，不是 demo——每一项都对应真实攻击面
- Nous Research 的双重身份形成**数据飞轮**：Agent 数据反哺模型训练，模型变强再让 Agent 变强
- 工程师视角的最大启发：**把"模型给的能力"和"Agent 自己积累的能力"分开看**，是判断任何 Agent 项目天花板的尺子

### 2.6 AI时代的软件工程

<img src="imgs/aicent-v2-14-software-engineering-in-ai-era/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 14：AI时代的软件工程]({% post_url 2026-07-19-aicent-v2-14-software-engineering-in-ai-era %})**

#### (1) 一句话主旨

软件工程没被 AI 终结，是被**压缩**了——"写代码"这层压薄，"理解、判断、验证、负责"那几层压厚。**重音从"软件"落到"工程"**。

#### (2) 地基松动：三条假设 + 一条曲线

| 维度 | 传统 | AI 时代 |
|---|---|---|
| 代码即资产 | 稀缺、精心打造 | 廉价、用完即弃的产出 |
| 生成是瓶颈 | 人力时间决定产出 | 验证才是瓶颈 |
| 工程师角色 | 动手的建造者 | 委托者 + 监督者 |

SWE-bench Verified：2023.10 的 1.96% → 2026.04 的 78.4%，工业老库迁移省 12 倍工时——"行不行"已不是问题。

两篇论文定性"中心对象"换了：

| 论文 | 中心对象转移 |
|---|---|
| Bhati 2026（A-SDLC 综述） | 代码生成 → 人类监督下的委托执行 |
| Kohl & Carro 2026（ICSE FoSE） | 代码构建 → 意图表达、架构控制、系统化验证 |

#### (3) 第一个范式转移：代码从资产变产出

**脊柱类比**：代码之于规约，就像**编译产物之于源代码**——你不会手改二进制，改源码然后重编译。

| 维度   | 旧范式                | 新范式                |
| ---- | ------------------ | ------------------ |
| 核心产物 | 代码                 | 规约（spec）           |
| 代码地位 | 稀缺资产               | 可再生产出              |
| 工程焦点 | 版本控制 / review / 重构 | 规约编写 / 演进 / AI 重生成 |
| 思想根源 | TDD、接口契约、需求文档（碎片化） | 规约中心化、机器可读（SDD）    |

泼冷水：Thoughtworks 技术雷达把 SDD 放在"评估"环；业界连"规约是什么"都没共识。**范式在移动是真的，远未成熟——谁说 SDD 是确定答案，他在卖东西。**

#### (4) 瓶颈转移：验证鸿沟根在意图鸿沟

**验证鸿沟数据**：

| 数据                        | 含义              |
| ------------------------- | --------------- |
| Sonar：96% 不信 vs 48% 一致验证  | 信任和验证之间一道深沟     |
| Apiiro：AI 快 4 倍，风险放大 10 倍 | 速度和安全朝反方向跑      |
| Aikido：AI 代码要为约 1/5 漏洞负责  | 漏洞里相当一部分直接来自 AI |

**根因——意图鸿沟**（微软研究院 Lahiri）：

| plausible by construction | correct by construction |
|---|---|
| 构造上合理 / 能编译通过 | 构造上正确 / 真做了你想的事 |
| AI 默认给这个 | 需要意图形式化，只能人来做 |

Meyer 一针见血：**prompt engineering 本质就是需求工程**——软件工程里最难的学科之一。AI 没让它变简单，只是换了个地方继续难。

#### (5) 认知债：被乐观叙事盖住的最深代价

| 维度   | 技术债          | 认知债           |
| ---- | ------------ | ------------- |
| 在哪里  | 写在代码里        | "本该理解却没理解"的空缺 |
| 能否量化 | 能（重复率、churn） | 不能，是"缺席"      |
| 出问题时 | 修起来麻烦        | 根本不知道从哪下手     |
| 是否复利 | 是            | 是             |

Deer Valley 共识（50 位技术人物含 Kent Beck、Martin Fowler）：**没有理解的速度，是不可持续的**。

征兆：PR review 时间不降反升近一倍——过去是"检查代码"，现在是"**重建作者跳过的那部分理解**"。

crossing point（意大利面点）：vibe coding 第 1 周飞快、第 3 个月撞墙掉到零；打好地基的项目稳定反超。

#### (6) 一线大佬：怎么干 + 问责崩塌

| 人物 | 核心论断 |
|---|---|
| Kent Beck | AI 是"不可预测的精灵"；做**增强编码**不做氛围编码；"精灵吃种子粮"（悄悄吃掉未来设计余地）；AI 缺乏品味 |
| Martin Fowler | 这是**汇编到高级语言**级别的变革；未来属于**专家通才**（故障不在单点深处，在技术缝隙里） |
| Boris Cherny | 100% AI 写代码可能，但"**最终是人在做**" |

**问责崩塌**（Kohl & Carro）：AI 写 → AI 验 → 没理解的人闭眼 merge → 出事谁负责？链条上每个人都能甩锅给 AI。

监督 vs 脱岗的差别：不在你有没有点 merge 按钮，而在**能不能回答"这段代码为什么是对的、错了你能不能扛"**。

#### (7) 什么升值了：判断（三层）

| 层           | 是什么             | 为什么 AI 替不了             |
| ----------- | --------------- | ---------------------- |
| ① 精确表达意图    | 描述对不对、全不全、边界清不清 | 描述质量归你，产量放大后意图不准的代价也放大 |
| ② 系统层面看全局   | 看到落在技术缝隙里的故障    | AI 在孤立上下文里工作，看不到缝隙     |
| ③ 定义"什么是对的" | 从而验证 AI 产出、为它负责 | 标准来自业务、约束、用户理解，AI 不在场  |

共同点：**都需要"在场"**。不亲手敲的是代码，没让渡出去的是判断。

#### (8) 费曼复盘

- 代码从"资产"变"产出"——围绕"保护每行代码"的旧工程纪律不再自动成立
- 瓶颈从"生成"移到"验证"——AI 拆掉了前一个，后一个长在人的判断上拆不掉
- 认知债比技术债可怕：**一个让你慢，一个让你盲**
- "工程师升维成监督者"是有条件的——没理解就是脱岗，不是升维
- 真正升值的只有一个词：**判断**（意图 + 全局 + 标准）
- 软件工程没被终结，是被压缩了——**重音从"软件"落到"工程"**

### 2.7 AI 时代，程序员的能力如何养成

<img src="imgs/aicent-v2-15-dev-career/cover.jpg" style="display: block; width: 800px;" alt="替换文字">

**完整内容: [AI编程方法(2) 15：AI 时代，程序员的能力如何养成]({% post_url 2026-07-19-aicent-v2-15-dev-career %})**

#### (1) 一句话主旨

AI 抹平了「挣扎」，而挣扎是能力的原材料。能力养成的机制没消失——**从「自动发生」变成「必须刻意」**。

#### (2) 三个类比先建手感：AI 到底抹掉了什么

| 类比 | 直觉 |
|---|---|
| copy SO 答案 vs 读完再改 | "能跑" ≠ "长本事"，差别在脑子 |
| SQL 全丢 ORM，三年后不懂 join | **认知卸载**：脑力转移给工具 |
| 技术债 → 认知债 | 代价**滞后、隐性、复利** |

便利有代价，挣扎是能力的原材料。

#### (3) 最硬实证：Anthropic 2026.01 随机对照实验

52 名工程师学 Trio 异步库，AI 组 vs 手写组：

| 维度 | 结果 |
|---|---|
| 测验平均分 | 50% vs 67%（d=0.738, p=0.01） |
| 完成速度 | 仅快 ~2 分钟，未达统计显著 |
| 差距最大题型 | **debugging**（监督 AI 最需要的能力） |

为没证实的快，赔掉实打实的懂。不会 debug 的监督者 = 橡皮图章。

#### (4) 六种 AI 交互模式：怎么用决定结果

| 模式 | 平均分 | 共同点 |
|---|---|---|
| **毁学习** | | |
| AI 代劳 / 渐进依赖 / 迭代式 AI 调试 | <40% | 让 AI 把活干完就走 |
| **保学习** | | |
| 先生成后理解 | 65%+ | 生成代码后回头追问、检查理解 |
| 混合代码-解释 | 65%+ | 要 AI 同时给代码和解释 |
| 概念性提问 | 65%+ | 只问概念，独立完成任务 |

**最锋利对照**：「先生成后理解」vs「AI 代劳」操作几乎一样——差别只在**用完之后是否回头理解**。

#### (5) 理论根基 + 宏观图景

| 维度 | 核心判断 |
|---|---|
| 合意困难（Bjork） | 学习更费力 → 长期记忆更强 |
| 认知债（Kosmyna 2025） | 思考丢给 AI → 脑回路退化 |
| 刻意练习（Ericsson） | 专长需要**痛苦**挣扎 |
| 学徒制崩塌 | 5-7 年后全行业资深断层；初级岗较 2022 降 40% |

AI 的全部卖点 = 消除痛苦 = 顶在能力养成的命门上。

#### (6) 三类人的行动手册

| 角色 | 核心原则 | 三个动作 |
|---|---|---|
| 年轻程序员 | 把挣扎从「被强加」变「主动设计」 | ① AI 当老师不当代笔 ② 先卡 10-30 分钟再问 AI ③ 往深水区扎根（合规/分布式/系统设计） |
| 老程序员（Kent Beck 范本） | 把「理解」从「写」里解耦出来 | ① 不让渡理解（盯中间结果）② 主动减速（让 AI 慢下来学）③ 守住设计嗅觉（AI 缺乏品味） |
| 组织 | 结构问题用结构解 | ① "必须解释才能合并"焊进流程 ② 每周 2h 导师时间锁进日历 ③ AI 增强导师制而非替代 |

#### (7) 复盘

- 焦点不是「用不用 AI」，而是**用完之后是否回头理解**
- **卡壳本身就是学习发生的地方**——不是路上的障碍
- 「写着写着就会了」的老路断了——新路要主动求理解、主动留挣扎、主动往深水区走
- 反方声音存在（纵向证据缺失），认真对待风险但别当铁律
- 慢，在这件事上，是真的快