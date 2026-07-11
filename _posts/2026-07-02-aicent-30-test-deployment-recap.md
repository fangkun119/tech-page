---
title: AI编程方法 30：测试部署 - 流程回顾
author: fangkun119
date: 2026-07-02 22:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-30-test-deployment-recap/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---

<!--
aicent-30-test-deployment-recap
AI编程方法 30：测试部署 - 流程回顾
-->

## 1. 本文导读地图

<img src="imgs/aicent-30-test-deployment-recap/c67340b75f633573233fc8b3a6b52bea_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是测试部署篇的流程回顾，复盘第 27~29 篇落地 Hify（智能客服 AI 应用平台）「质量体系 + 可交付形态 + 可观测性」全过程。内容按「方法论提炼」与「实战演示」两部分组织：方法论部分不绑定技术栈、可迁移到任何 AI 编程子系统；实战部分紧扣 Hify、Spring Boot Test、Docker、K8s、OpenTelemetry、Grafana、CLAUDE.md、SKILL 等技术栈，复现项目过程。

<img src="imgs/aicent-30-test-deployment-recap/eba1f067694aec07114daf585c6938e0_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
\`\`\`mermaid
flowchart TD
    Start([本文导读]) --\> Q{阅读目标?}
    Q --\>|掌握方法论 / 快速复习| A1[第 2 章 背景与目标]
    Q --\>|完整复现项目 / 深入理解| B1[第 5 章 场景一 核心链路]

    subgraph 第一部分[第一部分 方法论提炼 - 参考手册风]
        A1
        A2[第 3 章 四条核心动作]
        A3[第 4 章 Check List]
        A1 -.-> A2
        A2 -.-> A3
    end

    subgraph 第二部分[第二部分 实战演示 - 实战教材风]
        B1
        B2[第 6 章 场景二 单测]
        B3[第 7 章 场景三 集成测试]
        B4[第 8 章 场景四 部署]
        B5[第 9 章 场景五 可观测性]
        B1 -.-> B2
        B2 -.-> B3
        B3 -.-> B4
        B4 -.-> B5
    end

    A3 --\> D[第 10 章 总结]
    B5 --\> D
\`\`\`
-->

| 读者类型 | 推荐阅读路径 | 预期收益 |
|----------|--------------|----------|
| 初学 AI 编程工程师 | 第 1 章 → 第 2 章 → 第 5~9 章（实战）→ 第 3~4 章（方法论沉淀） | 系统掌握从质量体系到部署可观测的完整流程，理解每一步 why |
| 熟练 AI 编程工程师 | 第 1 章 → 第 3~4 章（方法论 + Check List 速查）→ 按需查阅第 5~9 章任一场景 | 快速回顾方法论、获取可裁剪 Check List |


## 2. 背景与目标：测试部署篇要交付什么

第 27~29 篇分别解决了质量保障、可交付、可观测三件事，把 Hify 从「能跑」推到「可上线」。本篇不再引入新方法，而是把这三件事放进真实操作里跑一遍，看在实战中咨询先行、执行分段、自检兜底这套流程到底交付了什么。建议先看完前三篇再看本篇，本文重在过程回顾与体验。

### 2.1 要解决什么问题

Hify 在进入测试部署篇之前，代码已经能跑，但距离「能交付」还差三步。<span style="color: red; font-weight: bold;">这三步是层层递进的：没有质量保障，交付出去的产物不可信；没有可交付形态，质量再好也只能在开发机上跑；没有可观测性，上线后系统是黑盒，出问题无法定位。</span>

#### (1) 三件事的递进关系

##### ① 第一件事：建质量体系

第 27 篇先解决「怎么知道代码是对的」。思路是先识别核心链路和风险点，写成 CLAUDE.md 的风险地图，再基于这份地图生成单测规范、划定集成测试的 P0 优先级。质量体系的产出不是测试代码本身，而是一份「测什么、不测什么」的地图，让后续每一篇的测试都有依据。

##### ② 第二件事：打包成可交付形态

第 28 篇解决「怎么把能跑的代码变成能部署的产物」。从本地 tar 包到 Docker 镜像再到 K8s 清单，三种形态对应三种交付场景。这一步的前提是质量已经过验证——如果没有第 27 篇的风险地图，部署时根本不知道哪些链路必须验证。

##### ③ 第三件事：加可观测性

第 29 篇解决「上线后怎么知道它在正常跑」。通过 Metrics、日志、Grafana Dashboard 把系统的内部状态暴露出来，让黑盒变透明。可观测性必须放在部署之后谈，因为指标和面板的形态取决于部署形态：K8s 上是 Prometheus 抓取，本地是日志文件，形态不同面板设计也不同。

#### (2) 本篇的定位

三件事各自成篇之后，本篇做的是把它们串起来：在真实项目里，这三件事不是线性完成的，而是交织在咨询、执行、自检的循环里。本篇的五个场景，每个都对应前三篇某一环的实战复盘，重点不在结论而在过程。

### 2.2 Hify 项目背景

<img src="imgs/aicent-30-test-deployment-recap/9302e034c31131697a214983d4a4d1ad_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Hify 是贯穿第 27~29 篇及本篇的载体项目。它的技术栈和约束决定了测试部署篇的每一个选择——为什么质量地图要圈出 LLM 调用链路、为什么部署要支持三种形态、为什么可观测性要监控 DistributionSummary。先理清 Hify 的全貌，后面场景的取舍才有依据。

#### (1) 技术栈与依赖

Hify 是一个 Spring Boot + Vue 的 AI 应用，采用模块化单体架构，部署在 K8s 上。核心技术栈与外部依赖如下：

| 类别 | 技术/组件 | 用途 |
|------|-----------|------|
| 后端框架 | Spring Boot | 业务逻辑与 API |
| 前端框架 | Vue | 单页应用 |
| 外部 API | LLM API | 核心对话能力 |
| 数据库 | MySQL | 业务数据 |
| 缓存 | Redis | 会话与热点 |
| 向量库 | pgvector | 知识库检索 |
| 部署平台 | K8s | 生产环境 |

#### (2) 业务场景与规模约束

Hify 的核心功能是 LLM 对话，落地场景是企业内部的智能客服。目标用户是企业内部团队，并发规模从几人到几十人不等，使用时间集中在工作时间，不需要 7×24 小时高可用。这套约束直接影响部署决策——几十人并发意味着单副本即可承载，不必为高可用做复杂的多副本与自动伸缩设计，但 LLM 调用的慢响应和流式输出（SSE）必须在网关层特殊处理。

### 2.3 真实开发体验

前三篇的方法论，作者总结是「先想清楚，再动手」。但想清楚这件事本身，Claude Code 能帮作者做大半。围绕咨询的价值，可以从三个视角提炼出第 3 章方法论的引子。

#### (1) 咨询让 Claude Code 主动发现风险

##### ① 风险识别比作者自己想的更全

第 27 篇做核心链路分析时，Claude Code 识别出的风险点比作者自己预想的更全。有一个典型例子：`KnowledgeNodeExecutor` 里的 O(n) 线性搜索，是 Claude Code 在做清单分析时主动发现的，作者并没有要求它找这类问题。这种「你没问它也说」的时刻，是用对了咨询的信号。

##### ② 核心链路分析是典型样板

第 27 篇的核心链路分析本身就是咨询模式的典型样板：让 Claude Code 读完整个项目，输出核心链路清单和风险地图，作者来确认。这种做法把「发现风险」从作者一个人的脑力活，变成 AI 主动扫描 + 作者业务判断的双向校验。

#### (2) 咨询模式与执行模式是两种节奏

##### ① 咨询环节主动提醒遗漏

第 28 篇部署环节，健康检查接口的遗漏是 Claude Code 在咨询环节提醒作者的。作者没想到的事，它问了。健康检查接口是 tar 包、Docker、K8s 三种部署形态都需要的公共前置，漏了它后面三种形态都要返工。

##### ② 执行模式不会主动暴露遗漏

如果跳过咨询，直接让 Claude Code 写 Dockerfile，它永远不会主动说「你这个接口还没有」。咨询模式和执行模式是两种节奏：<span style="color: red; font-weight: bold;">咨询阶段 Claude Code 会追问、会提醒；执行阶段它只完成指令。两种模式分开用，效果差很多。</span>

#### (3) 执行后自检的惊喜

##### ① 主动指出面板会空白

第 29 篇的 Grafana Dashboard 生成完后，Claude Code 主动指出 `DistributionSummary` 缺 `_bucket`，面板会空白。这个问题作者自己不一定能发现——面板渲染成功不代表数据正确，只有懂指标语义的人或 AI 才会注意到字段缺失。

##### ② 自检不是每次都做但值得单独说

执行后自检这件事，Claude Code 不是每次都做，但在第 27~29 这几篇里它做了，作者觉得值得单独说一下。它和咨询阶段的主动追问不同——咨询是执行前的风险扫描，自检是执行后的结果校验，两者一前一后把咨询模式的价值闭合起来，也为第 3 章的方法论提供了完整素材。

## 3. 方法论提炼：测试部署的四条核心动作

<img src="imgs/aicent-30-test-deployment-recap/a75b1e3f8154ec803d688f034b242541_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本部分不绑定具体技术栈，是可迁移到任何「测试 + 部署 + 可观测」AI 编程场景的方法论。每条动作按「怎么做 / 为什么 / 常见误区」三段式展开。

### 3.1 动作一：先咨询，再执行

<img src="imgs/aicent-30-test-deployment-recap/daa66e3409b9d6c6c044830215876360_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 怎么做

<span style="color: red; font-weight: bold;">把高风险任务先用咨询模式过一遍，确认后再进入执行模式。</span>高风险任务包括：测试计划、架构方案、接口设计、部署形态、可观测性策略。咨询模式的提示词统一以「不要写代码，先给分析 / 先给清单」结尾，让 Claude Code 只输出判断和建议，不产出代码。拿到分析后人工确认，再切到执行模式让它落地。

#### (2) 为什么

<span style="color: red; font-weight: bold;">咨询模式下 Claude Code 会主动说出「你没问它也说」的信息，这些信息在直接执行时永远不会出现。</span>本篇三个场景都是证据：部署形态讨论里，Claude Code 主动问「当前有没有健康检查接口」，tar.gz、Docker、K8s 三种形态都需要它，没有就先补；可观测性讨论里，它给出三个高代价架构决策（Actuator 独立端口、traceId 进 MDC、日志字段规范），并明确说明改晚了要动 K8s Service、Nginx 和所有日志打点代码；核心链路分析里，它识别出 7 个风险点，其中 `doStreamChat()` 抛 RuntimeException 时 assistant 消息缺失、`selectRecentBySessionId` 取最旧消息这两个高危点是它在做清单分析时主动发现的，作者没要求它找。

#### (3) 常见误区

##### ① 高风险任务直接让 Claude Code 写代码

跳过咨询直接进入执行，Claude Code 不会主动报告健康检查遗漏、架构决策代价、风险地图这些前置信息，产出的代码「能跑但有缺口」，缺口往往在部署或上线后才暴露。

##### ② 咨询模式与执行模式混用

两种节奏分开用效果差很多。<span style="color: red; font-weight: bold;">咨询模式让 Claude Code 只输出判断，执行模式让它落地代码。</span>在一条提示词里既要求分析又要求写代码，Claude Code 会急着进入执行，跳过本该在咨询阶段暴露的信息。

### 3.2 动作二：Claude Code 先读代码再动手

#### (1) 怎么做

<span style="color: red; font-weight: bold;">每个执行步骤前，让 Claude Code 先扫描项目再动手。</span>执行类提示词统一以「先读现有代码 / 先扫描项目」开头，要求它对照真实代码再生成，而不是凭假设生成。扫描后如果发现与提示词假设不一致的地方，先报告再决定怎么处理。

#### (2) 为什么

<span style="color: red; font-weight: bold;">凭假设生成的代码会漏掉项目里真实存在的问题，直接执行会产生「能跑但有缺陷」的代码。</span>本篇五个场景里 Claude Code 通过扫描主动发现了五类问题：本地部署时读出现有 `start.sh` 是开发模式（跑 `mvn build` + `npm dev server`），告诉你需要完全重写而不是在上面改；可观测性配置时发现 MDC 在新线程 `llmExecutor` 里会丢失，新建 `MdcTaskWrapper` 解决跨线程问题；发现熔断器不走 Resilience4j Registry；发现 `HealthController.java` 已经存在但格式不一致，主动给出两个选项而不是自作主张改全局格式；发现 `logback-spring.xml` 是手拼 JSON 字符串、有转义风险。这五个问题提示词里都没写，是 Claude Code 自己读代码发现的。

#### (3) 常见误区

##### ① 凭假设生成代码

不先扫描项目，让 Claude Code 直接按提示词生成。结果是 `start.sh` 在开发模式脚本上叠加改动、MDC 跨线程丢失没被处理、已有接口被重复创建——代码能跑，但缺陷埋在细节里。

##### ② 看到扫描发现报告不细看

Claude Code 扫描后给出的发现报告（例如「`HealthController.java` 已存在但格式不一致」「熔断器不走 Registry」）是关键决策点。不细看就让它继续执行，等于放弃了用真实代码修正提示词假设的机会。

### 3.3 动作三：执行后自检

<img src="imgs/aicent-30-test-deployment-recap/399e18cff35ad384a317908aa60a2772_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 怎么做

<span style="color: red; font-weight: bold;">关键产出生成后，让 Claude Code 对自己的产出做一次自检。</span>关键产出包括 Grafana Dashboard、测试计划、配置文件这类一旦出错影响面大的产物。自检不是例行问一句「有没有问题」，而是明确要求它对照需求逐项检查，并报告发现的缺口。

#### (2) 为什么

<span style="color: red; font-weight: bold;">自检能发现提示词没覆盖的问题。</span>本篇三个证据：Grafana Dashboard 生成完后，Claude Code 主动发现 `DistributionSummary` 缺 `_bucket`，询问是否补加 `.publishPercentileHistogram(true)`——不补 P95/P99 面板全是空白；单测场景里，作者发了一份故意有三处错误的测试计划，Claude Code 执行前先指出三个问题（「验证数据库有记录」在单测里做不到、「apiKey 格式校验」在代码里不存在、`@MockBean` 与「只写单元测试」冲突），没有照着错误计划直接写代码；本地部署里端口写错时，Claude Code 按实际配置生成而不是按提示词盲目执行。

#### (3) 常见误区

##### ① 接受产物就结束

Dashboard JSON 导入、测试计划确认后就直接进入下一步，不做自检。结果是面板空白不知道为什么、测试计划里的错误被照单全收写进测试代码。

##### ② 自检当例行公事走形式

只问一句「检查一下」而不要求逐项对照。Claude Code 会默认产出没问题，自检变成空跑。正确的做法是要求它对照需求清单逐项核对并报告缺口。

### 3.4 动作四：先写失败的测试，再修 bug

#### (1) 怎么做

<span style="color: red; font-weight: bold;">顺序固定为「红 → 修 → 绿」。</span>先写一条针对 bug 的测试，跑它让它变红，证明测试确实命中了错误行为；然后修代码，跑测试让它变绿，才证明 bug 真的修好了。顺序不能反。

#### (2) 为什么

本篇对应 IT-P0-03 集成测试（对话上下文多轮正确性）。这条测试对应一个已知 bug：`selectRecentBySessionId` 的 SQL 是 `ORDER BY created_at ASC LIMIT`，取的是最旧消息而非最新。操作顺序是：先写测试验证第 3 次请求收到的是「第一条」「第二条」而非「第三条」之前的最新两条，测试变红，证明 SQL 取的确实是最旧消息；然后把 ASC 改成 DESC，测试从红变绿，bug 才算真正被修复。如果顺序反了——先修 SQL 再补测试——测试一上来就是绿的，无法证明它真的在验证这个 bug，也无法区分「bug 修好了」和「测试根本没覆盖到」。

#### (3) 常见误区

##### ① 修完 bug 再补测试

先改 SQL 再写测试，测试直接通过。这种测试无法证明它命中了错误行为，后续 bug 复发时测试也不会变红。

##### ② 跳过「红」的阶段直接验证通过

写了测试但不先确认它是红的，直接改代码后跑通过。缺少「红」这一步，就缺少了「测试找对了问题」的证据。

### 3.5 补充动作：提示词约束要写到位

除了四条核心动作，还有两类补充工作决定产出质量。

| 补充类型 | 具体内容 | 不写约束的后果 |
|----------|----------|----------------|
| 提示词约束 | SSE 流式响应需要 Nginx `proxy_buffering off`；LLM 调用超时时间要够长；前端 Dockerfile 要把 `/api` 反向代理到后端 | Nginx 把 SSE 事件攒批后一次性发出，用户看不到打字机效果；LLM 慢调用被超时切断；前端请求打不到后端 |
| 已有模式复用 | 把验证过的流程固化为 SKILL，例如 `unit-test.md`、`integration-test.md` | 每次重写提示词、流程不一致、新场景下漏掉关键步骤 |

提示词约束那一类，作者在本篇里直接写进了提示词（「Hify 有流式响应（SSE），Nginx 需要关闭缓冲」「LLM 调用可能很慢，超时时间要够长」）。不说这些约束，Claude Code 生成的 Nginx 配置会把事件攒批后一次性发出。已有模式复用那一类，单测流程固化进 `.claude/skills/unit-test.md` 后，后续 `/单测 AgentService.createAgent` Claude Code 自动走完整流程；集成测试流程固化进 `integration-test.md` 后，`/集成测试 Agent模块` 自动复用。

### 3.6 方法论总览

| 动作 | 核心要点 | 常见误区 |
|------|----------|----------|
| 先咨询，再执行 | 高风险任务先用咨询模式过一遍，确认后再执行 | 高风险任务直接写代码、咨询与执行混用 |
| 先读代码再动手 | 每个执行步骤前先扫描项目，凭真实代码修正假设 | 凭假设生成代码、扫描发现报告不细看 |
| 执行后自检 | 关键产出生成后对照需求逐项自检 | 接受产物就结束、自检走形式 |
| 先写失败的测试，再修 bug | 红 → 修 → 绿，顺序固定 | 修完再补测试、跳过「红」直接验证通过 |

## 4. 测试部署 Check List

<img src="imgs/aicent-30-test-deployment-recap/48fb97b381ebbe1691980d0a119f2b87_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本清单按开发顺序分六组，可裁剪到项目阶段快速查阅。

### 4.1 质量体系规划阶段

- ☐ 是否让 Claude Code 输出核心链路清单（3-5 条）+ 风险地图（每条链路含名称、涉及模块和类、为何核心）
- ☐ 两个高危点是否识别：`doStreamChat()` 工作流路径抛 RuntimeException 时 user 消息落库但 assistant 消息缺失
- ☐ `selectRecentBySessionId` 的 SQL `ORDER BY created_at ASC LIMIT` 是否被识别为取最旧而非最新消息
- ☐ 测试是否按 P0/P1/P2 分档（P0 核心链路必须覆盖、P1 应该覆盖、P2 有余力再做）
- ☐ 核心链路地图是否对照业务理解人工确认后再写进 `CLAUDE.md`
- ☐ 单测规范是否基于核心链路地图生成（非泛泛通用规范）

### 4.2 单元测试阶段

- ☐ mock 方式是否用 `@Mock` + `@InjectMocks` + `@ExtendWith(MockitoExtension.class)`，而非 `@MockBean`（后者需启动 Spring Context，与单测冲突）
- ☐ 断言是否用 AssertJ 且有意义（验证 `insert()` 被调用一次，而非"数据库有记录"这种 mock 后做不到的断言）
- ☐ 是否禁止在测试里写业务逻辑、重复计算期望值（直接写字面量，避免把被测逻辑在测试里再实现一遍）
- ☐ 测试命名是否 `should_[期望结果]_when_[输入条件]`
- ☐ 测试结构是否 Given-When-Then
- ☐ 测试计划是否先于代码产出（执行路径树 + 场景清单 + P0/P1/P2 分档），高风险任务先咨询再执行
- ☐ 流程是否固化为 `.claude/skills/unit-test.md` SKILL（后续 `/单测AgentService.createAgent` 自动走完整流程）

### 4.3 集成测试阶段

- ☐ 是否用 Spring Boot Test + MockMvc 完整启动（非切片测试）
- ☐ 是否 mock 掉 LLM API 和 MCP Server 而非真实调用（如 `MockProviderAdapter`）
- ☐ 是否打真实 HTTP 请求走完从 Controller 到数据库的完整链路
- ☐ 是否确认技术基础就位（如 H2 内存库、`application-mock.yml`、Mock 适配器），不重复搭建
- ☐ 已知 bug 是否先写红测试再修（红 → 修 SQL `ASC` 改 `DESC` → 绿），不修完再补测试
- ☐ 流程是否固化为 `.claude/skills/integration-test.md` SKILL（后续 `/集成测试Agent模块` 自动复用）

### 4.4 部署交付阶段

- ☐ 是否有健康检查接口 `GET /api/v1/health`，返回 HTTP 200 和 `{"code":0,"data":"ok"}`（接口本身只返回 ok，不检查数据库连接）
- ☐ 健康检查接口是否被三处复用：本地 `curl` 确认、Docker `healthcheck` 指令、K8s liveness/readiness 探针
- ☐ 三种部署形态（本地 tar / Docker / K8s）是否都依赖健康检查接口
- ☐ `start.sh` 是否轮询 `/api/v1/health`，就绪打印成功，失败打印最后 30 行日志
- ☐ `start.sh` 是否为生产模式完全重写（而非沿用跑 `mvn build` + `npm dev server` 的开发脚本）
- ☐ SSE 流式响应是否在 Nginx 配置 `proxy_buffering off`（否则事件攒批后一次性发出，看不到打字机效果）
- ☐ LLM 调用超时时间是否足够长（避免慢响应被 Nginx 截断）
- ☐ Nginx 是否把 `/api` 请求反向代理到后端
- ☐ Dockerfile 是否分三步产出：后端 Dockerfile、前端 Dockerfile、`docker-compose.yml`

### 4.5 可观测性阶段

- ☐ Actuator 是否独立端口隔离（改晚了要动 K8s Service 和 Nginx）
- ☐ traceId 是否趁早进 MDC（改晚了要动所有日志打点代码）
- ☐ 日志字段规范是否趁早定（改晚了所有告警规则都要跟着改）
- ☐ 跨线程（如 `llmExecutor`）是否用 `MdcTaskWrapper` 包装任务，避免 traceId 丢失
- ☐ 日志是否 JSON 格式，含 `timestamp`/`level`/`traceId`/`thread`/`logger`/`message`
- ☐ `logback-spring.xml` 是否避免手拼 JSON 字符串（用结构化编码器，规避转义风险）
- ☐ `DistributionSummary` 是否配置 `publishPercentileHistogram(true)`（否则 `_bucket` 缺失，P95/P99 面板空白）
- ☐ Grafana Dashboard 指标名是否与代码暴露的指标逐一对齐（如 `hify_chat_requests_total` 等）
- ☐ 是否区分"需告警"与"不建议告警"（如熔断器 HALF_OPEN 是正常恢复流程、非工作时间 P1/P2 无人使用不需 on-call）

### 4.6 回归与验收阶段

- ☐ 改动后是否先跑旧用例确认旧功能没坏（接入新功能先验证旧功能）
- ☐ 三种部署形态（本地 tar / Docker / K8s）是否都能跑通
- ☐ Grafana Dashboard 是否无空白面板（`_bucket` 配置正确，P95/P99 有数据）
- ☐ 核心链路（P0）是否都有测试覆盖


## 5. 实战演示一：识别核心链路，写入 CLAUDE.md（场景一）

<img src="imgs/aicent-30-test-deployment-recap/a3c41a82c6838afba865200c3b53a0cd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本章复盘第 27 篇质量体系的第一步——所有测试的起点是知道测什么。先让 Claude Code 读完整个项目，输出核心链路清单和风险地图，由作者确认后写进 `CLAUDE.md`，后续每一步测试规划都基于这份地图展开。

### 5.1 让 Claude Code 输出核心链路清单和风险地图

所有测试的起点是知道测什么。与其凭直觉判断哪里重要，不如先让 Claude Code 通读代码，把核心链路和风险集中区域列出来，作者再对照业务理解做确认。

#### (1) 指令

```text
帮我分析这个系统，输出三个清单：

1. 核心链路清单（3-5条）
每条链路：名称、涉及的模块和类、为什么是核心链路

2. 风险集中区域
哪些模块/方法最容易出问题、出了问题影响最大
每个风险点：风险类型（安全/并发/性能/数据一致性）、可能的失败场景

3. 测试重心建议
基于前两条，测试精力应该往哪放
哪些地方必须有测试覆盖，哪些可以先跳过

输出格式：结构化的 CLAUDE.md 片段，我直接复制进去。
```

#### (2) 要点

##### ① Claude Code 给出 5 条核心链路和 7 个风险点（其中 2 个高危）

Claude Code 读完代码后，给出 5 条核心链路与 7 个风险点，其中 2 个属于高危。高危点之所以关键，是因为它们的失败场景会直接造成数据不一致或业务错误，必须被测试覆盖。两个高危风险点如下：

| 风险点 | 风险类型 | 失败场景 |
|------|----------|----------|
| `doStreamChat()` 工作流路径抛 RuntimeException | 数据一致性 | user 消息已落库，但 assistant 消息缺失，对话历史出现单边记录 |
| `selectRecentBySessionId` 的 SQL | 数据一致性 | `ORDER BY created_at ASC LIMIT` 取的是最旧消息而非最新，多轮对话上下文错乱 |

##### ② 拿到结果对照业务理解人工检查

AI 的分析基于代码结构，作者的判断来自业务理解，两者对比才能确认这份地图是准的。Claude Code 主动识别出的风险点未必和作者预想的重合——比如 `KnowledgeNodeExecutor` 里的 O(n) 线性搜索，作者没要求它找，它也列了出来。差异和补全是这一步最有价值的产出。

##### ③ 确认后写进 CLAUDE.md，影响后续每一步

确认无误后写进 `CLAUDE.md`。这份地图会影响后面每一步：单测规范基于它生成，集成测试的 P0 优先级基于它划定。地图一旦固化，后续 Claude Code 在任何测试环节都能直接引用，不必重新分析。

### 5.2 基于地图生成单测规范

核心链路地图写进 `CLAUDE.md` 后，下一步是基于它生成单元测试规范。规范不是泛泛的通用要求，而是紧扣地图里识别出的核心链路与风险点。

#### (1) 指令

```text
基于 CLAUDE.md 中的核心链路和风险地图，帮我生成 Hify 项目的单元测试规范。

规范需要覆盖：
1. 哪些代码必须写单测（结合核心链路判断）
2. 哪些代码不写单测、用集成测试替代（结合 Hify 的外部依赖特点）
3. 测试命名规范：should_[期望结果]_when_[输入条件]
4. 测试结构：Given-When-Then
5. mock 使用规范：什么时候 mock，什么时候不 mock
6. 断言规范：用 AssertJ，断言要有意义
7. 禁止事项：哪些写法不允许出现

输出格式：直接输出 CLAUDE.md 片段，我复制进去就能用。
```

#### (2) 要点

##### ① 禁止事项特别值得关注

规范里的禁止事项一条最值得关注：禁止在测试里写业务逻辑、不要重复计算期望值、直接写字面量。这是 AI 写测试时最常见的坏味道——它会把被测的计算逻辑在测试里再实现一遍，测试通过只能说明两段逻辑一样错。被测代码有 bug 时，测试里的同款 bug 会让红测试变绿，缺陷被双双掩盖。

##### ② 规范明确写出来，后续测试代码自动遵守

规范一旦写进 `CLAUDE.md`，后续 Claude Code 写任何测试代码都会自动遵守，不必每次重复强调。这就是「固化」的价值：把一次性约定变成默认行为，规范从口头提醒升级为项目级约束。


## 6. 实战演示二：单测执行——Claude Code 纠正错误计划（场景二）

<img src="imgs/aicent-30-test-deployment-recap/7bffb88cd633f5317e30db1a84fd27e6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

有了单测规范，先手动走一遍 ProviderService 的单测，跑通后固化为 SKILL。

### 6.1 先规划，不直接写代码

#### (1) 指令

深度分析 ProviderService 的 createProvider 方法。

```text
告诉我：
1. 这个方法有哪些执行路径（正常路径 + 异常路径）
2. 每条路径的关键变量是什么
3. 哪些边界条件最容易出错

基于分析，给我一份测试计划：测哪些场景、每个场景验证什么断言。

先给计划，不要写代码。
```

#### (2) 要点

##### ① Claude Code 给出完整执行路径树与分档测试场景

Claude Code 给出了完整的执行路径树，并列出 11 个测试场景，按 P0/P1/P2 三档排定优先级。先有路径分析，再有测试场景，测试计划建在被测代码的真实行为之上，而不是凭直觉拍脑袋。

##### ② 「先给计划不要写代码」是高风险任务先咨询的体现

指令末尾明确要求「先给计划，不要写代码」，这是高风险任务（测试计划、架构方案、接口设计）的标准做法：先让 Claude Code 把方案过一遍，人确认无误后再落地，避免一上来就拿到一堆需要返工的代码。

### 6.2 故意错误的测试计划与 Claude Code 的纠正

#### (1) 指令

测试计划确认如下：

```text
- 正常创建：provider name 唯一，验证返回 id 且数据库有记录
- 重复名称：抛出 BizException，错误码 PROVIDER_NAME_DUPLICATE
- apiKey 格式校验：不符合格式，抛出 BizException
```

按这个计划写单元测试。

约束：用 @MockBean mock 掉，只写单元测试。

#### (2) 要点

##### ① Claude Code 没照错误计划直接写代码，而是先指出三处问题

这份计划被故意埋了三个错误。Claude Code 没有照单全收直接写代码，而是先把三处问题逐条点出来，再等人确认。这正是上一节「高风险任务先咨询」的逆向印证：AI 自己也会在动手前先核对计划是否站得住脚。

##### ② 错误计划与 Claude Code 纠正的对比

| 错误计划项 | Claude Code 的纠正 | 为什么 |
|------------|--------------------|--------|
| 验证数据库有记录 | 单测做不到，只能验证 insert() 被调用了一次 | insert() 被 mock 后数据不真正写库 |
| apiKey 格式校验 | 代码里根本不存在，没有被测对象 | 这条测试无的放矢 |
| 用 @MockBean | 与「只写单元测试」冲突，应用 @Mock + @InjectMocks + @ExtendWith(MockitoExtension.class) | @MockBean 需启动 Spring Context |

##### ③ 三类错误的代表性

这三处错误分别代表了 AI 写测试时最典型的三种坏味道：断言越界（验证 mock 替身背后的真实副作用）、无的放矢（测了根本不存在的逻辑）、定位错乱（用集成测试的工具去做单元测试）。任何一条如果被照单全收，写出来的测试要么跑不过，要么过了也没意义。

### 6.3 固化为 SKILL

#### (1) 要点

##### ① 确认后 Claude Code 生成两个文件

三处错误纠正、计划确认后，Claude Code 生成两个文件：测试类本体与（若需要的）基类/工厂。流程到此跑通，从「分析路径」到「纠正计划」再到「生成代码」一气呵成。

##### ② 流程固化为 .claude/skills/unit-test.md

把这套流程固化为 `.claude/skills/unit-test.md`，以后只要一句 `/单测 AgentService.createAgent`，Claude Code 就会自动走完整流程：先分析执行路径、给出分档测试计划、与人确认后再落地代码。一次手动复盘，换来之后无数次自动复用。


## 7. 实战演示三：集成测试——测试驱动修复（场景三）

<img src="imgs/aicent-30-test-deployment-recap/af87c97e67094767a76adff8d71587c1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

集成测试是 Hify 的主力——Spring Boot Test 完整启动，mock 掉外部 API，打真实 HTTP 请求，走完从 Controller 到数据库的完整链路。

### 7.1 规划集成测试清单

#### (1) 指令

```text
测试范围：Spring Boot Test + MockMvc，mock 掉 LLM API 和 MCP Server，
测试 Hify 自身从 Controller 到数据库的完整链路。

按优先级分三档：
P0：核心链路，必须覆盖
P1：主要功能，应该覆盖
P2：边缘场景，有余力再做

每条给出：测什么、验证什么、为什么这个优先级。

不要写代码，先给清单。
```

#### (2) 要点

##### ① Claude Code 先检查 application-mock.yml

Claude Code 先检查 `application-mock.yml`，发现 H2 内存库和 `MockProviderAdapter` 已经就位，不需要额外搭建技术基础，直接进入清单规划。

##### ② 清单按 P0/P1/P2 三档划分

清单按 P0/P1/P2 三档划分优先级：P0 是核心链路必须覆盖，P1 是主要功能应该覆盖，P2 是边缘场景有余力再做。每条都明确测什么、验证什么、为什么这个优先级，先给清单再写代码。

### 7.2 IT-P0-03 先写红测试

#### (1) 指令

```text
测试步骤：
1. 同一 session 依次发 3 条消息（"第一条"、"第二条"、"第三条"）
2. 在第 3 条发送时，拦截 MockProviderAdapter 收到的 ChatRequest

验证 MockProviderAdapter 第 3 次收到的 messages 数组：
① 包含"第一条"和"第二条"的历史消息
② 历史消息按时间升序排列（先旧后新，符合 LLM 期望）
③ 最后一条是"第三条"
```

#### (2) 要点

##### ① 这条测试对应一个已知 bug

这条 IT-P0-03（对话上下文多轮正确性）对应一个已知 bug，先写测试，测试应该是红的——测试先红才能定位到真正的故障点。

##### ② 根因是 SQL 取了最旧消息

根因是 `selectRecentBySessionId` 的 SQL 排序方向反了：

```sql
ORDER BY created_at ASC LIMIT
```

ASC 升序取到的最旧消息而非最新，历史消息拼装顺序因此错误，第 3 次请求拿到的上下文不对。

##### ③ 测试变红后才能去修 SQL

测试变红后才能去修 SQL——红是修复的前提，没有红就没有「bug 被真正修复」的证据。

### 7.3 修 SQL，测试从红变绿

#### (1) 红绿循环流程

```mermaid
graph LR
    A[写红测试<br/>IT-P0-03] --> B[验证测试红<br/>复现 bug]
    B --> C[修 SQL<br/>ASC 改 DESC]
    C --> D[测试变绿<br/>bug 真正修复]
```

#### (2) 要点

##### ① 先写测试让它红

先写测试让它红，验证 SQL 取的确实是最旧消息——测试红才能证明 bug 被复现，而不是凭感觉判断 SQL 有问题。

##### ② 修 SQL，测试从红变绿

修 SQL（ASC 改 DESC），测试从红变绿，bug 才算真正被修复——绿是修复的最终证据，测试不绿不算修完。

##### ③ 测试驱动修复，顺序不能反

这是测试驱动修复，不是修完再补测试——顺序不能反。先红后绿，修复才被「测试通过」这个客观信号约束，而不是靠人眼 review SQL。

### 7.4 固化为 SKILL

#### (1) 要点

##### ① 流程固化为 integration-test.md

流程固化为 `.claude/skills/integration-test.md`，把「规划清单 → 写红测试 → 修代码 → 测试变绿」的测试驱动修复节奏沉淀成可复用的 Skill。

##### ② 后续 /集成测试 Agent模块 自动复用

后续执行 `/集成测试 Agent模块`，流程自动复用，Claude Code 自动走完整链路，不需要每次重复交代测试范围与红绿循环的顺序。


## 8. 实战演示四：部署——咨询先行（场景四）

<img src="imgs/aicent-30-test-deployment-recap/49716c4994f87d5c754eb1b4bff3c1c9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本地部署、Docker 部署、K8s 部署，三种形态的顺序是从简单到复杂。但先做任何一个之前，咨询环节不能跳过。

Hify 是一个 Spring Boot + Vue 的 AI 应用，后端调用外部 LLM API，依赖 MySQL、Redis、pgvector，目标用户是企业内部团队，规模从几人到几十人不等。基于这个背景，作者没有直接让 Claude Code 写 Dockerfile，而是先用一条咨询指令，让它分析应该支持哪些部署形态。

### 8.1 咨询先行：分析部署形态

#### (1) 指令

```text
我想把它做成可交付的形态，方便部署到不同环境。
帮我分析应该支持哪些部署形态，以及每种形态需要提前准备什么。

不要写任何代码，先给我分析。
```

#### (2) 要点

##### ① Claude Code 给出三种形态对比和建议优先级

Claude Code 没有直接动手，而是先给出 tar.gz、Docker、K8s 三种形态的对比，并给出建议优先级，主动问了四个确认问题，把不确定的约束先问清楚。

##### ② 第四个问题是作者没想到的

第四个问题是：当前有没有健康检查接口？tar.gz、Docker、K8s 三种形态都需要它——没有就先补。这说明咨询的价值在于暴露前置依赖，而不是直接堆代码。

##### ③ 三种部署形态对比

| 形态 | 适用场景 | 健康检查用法 |
|------|----------|--------------|
| 本地 tar | 企业内部简单环境 | 启动后 `curl /api/v1/health` 确认服务起来了 |
| Docker | 标准化交付 | `HEALTHCHECK` 指令轮询健康接口 |
| K8s | 集群编排 | `liveness` 和 `readiness` 探针 |

三种形态共用同一个健康检查接口，所以它必须先补。

### 8.2 补健康检查接口

#### (1) 指令

```bash
帮 Hify 加一个健康检查接口 GET /api/v1/health，
返回 HTTP 200 和 {"code":0,"data":"ok"}。

这个接口会被三个地方用到：
- 本地部署：启动后 curl 确认服务起来了
- Docker：healthcheck 指令
- K8s：liveness 和 readiness 探针

接口本身只需要返回 ok，不需要检查数据库连接。
```

#### (2) 要点

##### ① Claude Code 先扫描项目再动手

Claude Code 先扫描项目，发现 `HealthController.java` 已经存在，但格式与现有项目风格不一致。

##### ② 主动给出两个选项，而不是自作主张

Claude Code 没有自作主张直接改全局格式，而是主动给出两个选项让作者选择：是按现有文件风格适配，还是统一为新格式。这是咨询模式和执行模式结合的正确节奏。

### 8.3 本地部署打包脚本

#### (1) 指令

```text
帮 Hify 生成本地部署的打包脚本。

要求：
- 产物是一个 tar 包，包含：后端 jar、前端 dist 目录、start.sh、stop.sh、配置模板 application.yml
- 目标机器已有 Java 环境，不需要打包 JDK
- start.sh 支持通过环境变量或配置文件注入 MySQL、Redis、pgvector 的连接信息
- stop.sh 优雅停止，等待进程退出
- Makefile 加 package 命令，一键打包

不需要包含 MySQL、Redis、pgvector，它们是外部服务。
```

#### (2) 要点

##### ① 先读现有 start.sh，发现需要完全重写

Claude Code 先读了现有的 `start.sh`，发现它是开发模式（跑 `mvn build` + npm dev server），与部署脚本的目标完全不同。它明确告诉作者需要完全重写，而不是在上面改——这是诚实评估现状的表现。

##### ② 新的 start.sh 启动后轮询健康接口

新的 `start.sh` 启动后会轮询 `/api/v1/health`：就绪了打印成功，失败了打印最后 30 行日志告诉作者哪里出了问题。这种「启动后自检 + 失败时直接给现场」的写法，把健康检查接口的价值落到了打包脚本里。

### 8.4 Docker 部署（前后端 Dockerfile + compose）

#### (1) 要点

##### ① Docker 部署分三步走

Docker 部署不是一条指令搞定，而是分三步走：先后端 Dockerfile，再前端 Dockerfile，最后 `docker-compose.yml`。每一步独立生成、独立验证，符合「分步实现，每步验证」的原则。

#### (2) 指令

前端 Dockerfile 有两个关键约束不能省：

```text
情况说明：
- Vue 3 项目，npm run build 打包
- 用 Nginx 托管静态文件
- 前端需要把 /api 请求反向代理到后端

特别注意：
- Hify 有流式响应（SSE），Nginx 需要关闭缓冲
- LLM 调用可能很慢，超时时间要够长
```

#### (3) 要点

##### ① proxy_buffering off 是 SSE 的关键

<span style="color: red; font-weight: bold;">`proxy_buffering off` 是 SSE 能正常工作的关键。</span>Nginx 默认会缓冲后端响应，等攒够一批再一次性发给客户端。对普通 HTTP 响应无伤大雅，但 SSE 是逐字推送——缓冲会把事件攒批后一次性发出，用户看不到打字机效果，体验完全退化成「等很久然后一次性蹦出来」。

##### ② 不说约束，Claude Code 不会主动猜

不说这个约束，Claude Code 生成的 Nginx 配置会沿用默认行为，把缓冲开着。这类「领域知识」必须由作者在指令里显式给出，不能指望工具自己推断——这印证了场景四的核心：咨询先行，约束要讲清 why。

### 8.5 K8s 部署

#### (1) 要点

##### ① K8s 的核心提示词和验收不在本文展开

K8s 的核心提示词和验收不在本文展开，源码里都有，感兴趣的读者可以直接看仓库。

##### ② 部署顺序看第 29 篇的执行命令就够了

K8s 的部署顺序看第 29 篇的执行命令就够了，按顺序跑即可。

##### ③ 部署顺序从简单到复杂

三种形态的部署顺序从简单到复杂：本地 tar → Docker → K8s。每一步都用同一个健康检查接口兜底，前一步验证通过再进入下一步，不要跳级。


## 9. 实战演示五：可观测性——从黑盒到透明（场景五）

可观测性这个领域知识面很广，先咨询不是因为你不懂，是因为有些架构决策改晚了代价极高。LLM 应用天然是个黑盒——一次对话进去，到底走了哪条链路、LLM 耗时多少、有没有触发熔断、MCP 工具调用成没成，外面全看不见。要把这个黑盒打开，不是装个日志框架那么简单，它牵涉到端口隔离、traceId 贯穿、字段规范、指标命名、告警取舍一连串决策。这一章复盘第 30 篇可观测性落地全过程，分咨询、结构化日志、Grafana Dashboard、告警策略四步走。

### 9.1 咨询先行：三个架构决策

<img src="imgs/aicent-30-test-deployment-recap/946941218ebdf7cb622631a9653c2ff2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Hify 是一个 Spring Boot + Vue 的 AI 应用，模块化单体架构，部署在 K8s 上。核心功能是 LLM 对话，依赖 MySQL、Redis、pgvector，调用外部 LLM API 和 MCP Server。基于这个背景，作者没有直接让 Claude Code 装日志框架、配 Prometheus，而是先用一条咨询指令，让它分析应该做哪些、不做哪些、有没有需要提前考虑的架构决策。

#### (1) 指令

```text
我想加可观测性，帮我分析：
1.应该做哪些，为什么
2.哪些一期不需要做，理由是什么
3.有没有什么需要提前考虑的架构决策。
不要写代码，先给分析。
```

#### (2) 要点

##### ① Claude Code 给出三个架构决策，改晚了代价极高

咨询指令里第三个问题「有没有需要提前考虑的架构决策」是这一步最值钱的地方。<span style="color: red; font-weight: bold;">Claude Code 没有泛泛而谈，而是给出了三个具体的、改晚了代价极高的决策。</span>这三个决策你不一定自己想得到——它们不是写代码层面的技巧，而是架构层面的方向选择，决定了后续扩展的成本。一旦代码铺开、日志打点写满、告警规则配齐，再回头改就是伤筋动骨。

##### ② 三个架构决策对比

| 架构决策 | 为什么趁早 | 改晚了的代价 |
|---------|-----------|-------------|
| Actuator 独立端口隔离 | 业务端口和监控端口分开，K8s Service、Nginx 只暴露业务端口，监控端口只在集群内可达 | 改晚了要动 K8s Service 配置、Nginx 反代规则，牵涉面广，还可能误把监控端点暴露到公网 |
| traceId 趁早进 MDC | 一次请求所有日志共享 traceId，后续所有打点天然带链路上下文 | 改晚了要把所有已写好的日志打点代码逐行加上 traceId，工作量随代码量线性增长 |
| 日志字段规范趁早定 | 字段名、类型、取值统一后，告警规则、Dashboard 查询都能稳定引用 | 改晚了所有告警规则、Grafana 面板的字段引用都要跟着改，一处漏改就是监控盲区 |

##### ③ 这三个决策决定了后续扩展成本

<span style="color: red; font-weight: bold;">这三个决策的共同点是：越早定越省事，越晚改越痛苦。</span>Actuator 端口晚改要动基础设施配置，traceId 晚改要动所有日志代码，字段规范晚改要动所有告警规则。咨询环节的价值就在于把这些「现在不痛、以后痛到无法回头」的决策提前拎出来，而不是等到代码铺满才发现动不了。

### 9.2 结构化日志 + traceId + MDC 跨线程

架构决策定了之后，第一步落地是结构化日志。这一步的提示词看似平常——配 JSON 格式、引 OpenTelemetry 生成 traceId、关键节点打日志——但 Claude Code 读项目后自己发现了三个提示词里没说的问题。

#### (1) 指令

```text
帮 Hify 配置结构化日志，同时引入 OpenTelemetry 生成 traceId。

要求：

1. 日志格式是 JSON，包含 timestamp、level、traceId、thread、logger、message
2. 同一次请求的所有日志共享同一个 traceId，可以用 traceId 过滤出完整链路
3. traceId 通过 OpenTelemetry 生成，为后续链路追踪预留扩展点
4. 对话链路的关键节点要有日志：请求进入、LLM 调用开始/结束、工具调用、异常
5. 日志输出到 stdout，由 K8s 采集

不需要接 Jaeger 或 Zipkin，一期只用 traceId 串联日志。
```

#### (2) 要点

##### ① Claude Code 读项目后发现三个提示词没说的问题

提示词只提了五个要求，Claude Code 读完项目代码后，主动报出了三个它发现的问题。这三个问题都是提示词里没说、但会直接让 traceId 链路断裂或日志质量不达标的隐患：

- **MDC 在新线程（llmExecutor）里会丢失**：LLM 调用是耗时操作，Hify 把它丢到独立的 `llmExecutor` 线程池执行。MDC 是 ThreadLocal 实现，跨线程默认不带过去，一旦任务切到 llmExecutor，traceId 就断了，链路日志拼不起来。
- **logback-spring.xml 是手拼 JSON 字符串，有转义风险**：现有的日志配置是用字符串拼接拼出 JSON，遇到 message 里带引号、换行、反斜杠就会破坏 JSON 结构，下游日志采集器解析失败直接丢日志。
- **ChatServiceImpl 日志稀疏**：核心对话链路的日志打点太少，关键节点（请求进入、LLM 调用开始/结束、工具调用、异常）缺位，traceId 串起来了也看不出链路里发生了什么。

这三个问题没有一个是提示词点名的，全是 Claude Code 自己读代码读出来的。这就是「先读代码再动手」的价值——提示词描述的是目标，代码现状才是起点。

##### ② 解决方案：新建 MdcTaskWrapper 解决跨线程丢失

针对 MDC 跨线程丢失，Claude Code 新建了 `MdcTaskWrapper`。原理很直接：在任务提交到 llmExecutor 之前，把当前线程的 MDC 上下文拷一份带过去，任务执行时先把这份上下文 set 进新线程的 MDC，执行完再 clear。

为什么要包一层而不是改线程池配置：llmExecutor 是 Spring 容器管理的 bean，改它的配置会影响所有用到它的地方；而所有提交到 llmExecutor 的任务统一用 `MdcTaskWrapper` 包一层，是一种侵入性最小、且能保证「凡是用 llmExecutor 的任务都带 traceId」的写法。这个 why 讲清楚，后续团队成员新写 llmExecutor 任务时就知道必须套这层包装，否则 traceId 又会断。

### 9.3 Grafana Dashboard

<img src="imgs/aicent-30-test-deployment-recap/71d4c0998be13c105d741c6f89ffc443_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

结构化日志铺好后，第二步是指标可视化。这一步的提示词把指标名和面板需求列得很细，但 Claude Code 在生成 Dashboard JSON 的前后各做了一次自检，这两次自检决定了 Dashboard 导进去之后是能用还是只能看。

#### (1) 指令

```text
帮我生成 Hify 的 Grafana Dashboard JSON。

Hify 暴露了以下指标：

- hify_chat_requests_total（label: agent_id）
- hify_chat_duration_ms（DistributionSummary，label: agent_id）
- hify_llm_calls_total（label: provider, model, success）
- hify_llm_duration_ms（DistributionSummary，label: provider, model）
- hify_circuit_breaker_state（label: provider，0=CLOSED 1=OPEN 2=HALF_OPEN）
- hify_mcp_tool_calls_total（label: tool, success）

Dashboard 需要包含以下面板：

1. 对话量（QPS 曲线，按 agent_id 分组）
2. 对话延迟（P50/P95/P99）
3. LLM 调用成功率（按 provider 分组）
4. LLM 调用延迟（P95，按 provider 分组）
5. 熔断器状态（各 Provider 当前状态）
6. MCP 工具调用成功率

输出完整的 Grafana Dashboard JSON，我直接导入使用。
```

#### (2) 要点

##### ① Claude Code 两步自检，缺一不可

Claude Code 在生成 Dashboard JSON 的过程中做了两次主动自检，一次在生成前，一次在生成后：

- **生成前主动发现指标名有误**：Claude Code 先去读了项目里 Micrometer 实际注册的指标，发现提示词里列的指标名和实际暴露的对不上。如果没这步，按提示词里的名字写 PromQL，Dashboard 导进去后面板全是「No data」，指标对不上你都不知道是名字错了还是没数据。
- **生成后主动发现 DistributionSummary 缺 _bucket**：Claude Code 生成完 JSON 后，主动检查了 P95/P99 面板用到的 histogram bucket 指标，发现项目里的 `hify_chat_duration_ms` 和 `hify_llm_duration_ms` 是 DistributionSummary 但没配 `publishPercentileHistogram(true)`，Prometheus 端拿不到 `_bucket` 序列。它没有默默吞掉，而是主动询问作者是否补加这个配置。如果没这步，P95/P99 面板导入后全是空白，而且你根本不知道为什么——指标名对、PromQL 对、就是没数据，这种空白最让人无从排查。

这两步自检缺一不可。前一步保证指标名对得上，后一步保证分位数指标有数据。Claude Code 不是把 JSON 生成完就交差，而是前后各做一次校验，这正是执行模式的正确节奏。

##### ② 指标到面板的映射

| 面板 | 用到的指标 | 计算方式 |
|------|-----------|---------|
| 对话量 | hify_chat_requests_total | rate() 求 QPS，按 agent_id 分组 |
| 对话延迟 P50/P95/P99 | hify_chat_duration_ms | histogram_quantile，依赖 _bucket 序列 |
| LLM 调用成功率 | hify_llm_calls_total | success=true 的量除以总量，按 provider 分组 |
| LLM 调用延迟 P95 | hify_llm_duration_ms | histogram_quantile，按 provider 分组 |
| 熔断器状态 | hify_circuit_breaker_state | 当前值（0/1/2），按 provider 分组 |
| MCP 工具调用成功率 | hify_mcp_tool_calls_total | success=true 的量除以总量 |

其中两个延迟面板都依赖 DistributionSummary 的 `_bucket` 序列，这正是 Claude Code 生成后自检发现的问题点。

### 9.4 告警策略（含不建议告警）

Dashboard 是给人看的，告警是给 on-call 的人发的。这一步的提示词把业务背景讲得很清楚——企业内部智能客服、工作时间使用、几十人并发——这些背景直接决定了告警阈值和要不要 on-call。Claude Code 给出的清单里最有价值的不是正面清单，而是「不建议告警」部分。

#### (1) 指令

```text
基于 Hify 当前暴露的指标，帮我梳理告警策略。

Hify 的业务背景：企业内部智能客服，工作时间使用，非 24 小时高可用场景。

用户规模：几十人并发。

帮我整理：

1. 哪些指标需要告警

2. 每条告警的阈值建议和触发条件

3. 告警级别（P0 立即响应 / P1 30 分钟内 / P2 次日处理）

4. 理由

不要写 Grafana 配置，先给策略清单。
```

#### (2) 要点

##### ① Claude Code 清单里有专门的「不建议告警」部分

Claude Code 给出的策略清单不是一味地往上堆告警，而是分成了「建议告警」和「不建议告警」两部分。「不建议告警」这部分和正面清单同等重要——它告诉作者哪些指标看起来该告、实际上告了只会添乱。

##### ② 不建议告警项及理由

| 不建议告警项 | 为什么不建议 |
|-------------|-------------|
| 熔断器 HALF_OPEN 状态 | HALF_OPEN 是熔断恢复流程的正常阶段（试探性地放一部分请求过去看是否恢复），它介于 OPEN 和 CLOSED 之间是设计如此。对它告警只会产生误判，真正该关注的是持续 OPEN，而不是 HALF_OPEN |
| 非工作时间的 P1/P2 | Hify 是企业内部工具，夜间无人使用，不需要 on-call。非工作时间触发 P1/P2 没有人响应，除了制造噪音和疲劳，没有实际价值 |

##### ③ 告警太多会产生疲劳

告警系统的致命问题不是漏告，而是误告和噪音。每一条不该告的告警，都在消耗 on-call 的注意力——当告警频繁误触发，人会本能地忽略它，等到真正严重的事件发生时反而错过了。所以「不建议告警」这部分和正面清单同样重要：知道不告什么，才能让告出来的每一条都值得响应。这一步的关键不是把所有指标都配上告警，而是结合业务背景（内部工具、工作时间、几十人并发）做取舍，把告警留给真正需要人介入的时刻。


## 10. 总结：可迁移到其他项目的经验

测试部署篇结束，Hify 从能跑的代码，变成了有质量保障、可交付、可观测的系统。这三篇做完，作者手上多了四样可以迁移到其他项目的东西。

### 10.1 四样交付物

#### (1) 交付物清单

##### ① 核心链路地图

Hify 的核心链路地图写进了 `CLAUDE.md`，Claude Code 每次动手前先读这份地图，知道哪些代码动不得、哪些节点是关键路径。这份地图本身可以复制到任何项目：<span style="color: red; font-weight: bold;">把项目里「不能改、改了就出事」的链路梳理出来落到 `CLAUDE.md` 即可。</span>

##### ② 两个测试 SKILL

单测 SKILL 和集成测试 SKILL 各就位一个。<span style="color: red; font-weight: bold;">SKILL 的本质是「把一次性的测试动作沉淀成可复用的流程」</span>，迁移到其他项目时，只需要替换项目特有的断言和上下文，测试节奏（先写失败用例、再修 bug、执行后自检）可以直接复用。

##### ③ 三种部署形态

开发、测试、生产三种部署形态都能跑。三种形态的差异配置沉淀成 `application-{env}.yml`，迁移时按目标项目的环境变量和基础设施调整对应配置项即可，<span style="color: red; font-weight: bold;">三态分离的思路本身可复用。</span>

##### ④ Grafana 大盘与告警策略

Grafana 大盘和告警策略都配好了。<span style="color: red; font-weight: bold;">大盘模板（JVM、HTTP、业务指标三层）和告警阈值规则可以导出为 JSON</span>，在新项目里导入后替换数据源和指标名即可复用。

#### (2) 一句话归纳

| 交付物 | 内容 |
|--------|------|
| 核心链路地图 | 写进 `CLAUDE.md`，标明不能动的关键路径 |
| 单测 SKILL | 单元测试的可复用流程 |
| 集成测试 SKILL | 集成测试的可复用流程 |
| 三种部署形态 | 开发/测试/生产三态分离、都能跑 |
| Grafana 大盘 + 告警策略 | JVM/HTTP/业务三层大盘 + 阈值规则 |

### 10.2 后续使用建议

#### (1) 用法由读者自己定

后面怎么用这套东西，由读者根据自己的项目情况决定，没有标准答案。

#### (2) 一个具体的起手式

如果读者工作里有一个老项目，可以用第 5 章场景一的提示词跑一遍：让 Claude Code 识别这个老项目的核心链路，再对比读者自己的判断，看两者有没有差异。

差异的地方，往往就是接下来该把精力放进去的地方——要么是 Claude Code 看漏了关键约束（说明地图还没写进 `CLAUDE.md`），要么是读者自己忽略了一些隐性依赖（说明这里值得补一次梳理）。

### 10.3 系列过渡

测试部署篇到此结束。本系列后续内容会进入新的主题，继续沿着「从能跑的代码到可交付的系统」这条主线往前走。


