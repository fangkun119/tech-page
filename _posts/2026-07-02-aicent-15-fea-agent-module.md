---
title: AI编程方法 15：核心功能 - Agent模块复杂逻辑拆解
author: fangkun119
date: 2026-07-02 08:00:00 +0800
categories: [AI编程, 方法论]
tags: [AI编程, AI编程方法论]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-15-fea-agent-module/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---

<!--
aicent-15-fea-agent-module
AI编程方法 15：Agent模块复杂逻辑拆解
-->

## 1. 全文导读

<img src="imgs/aicent-15-fea-agent-module/be6c310a7d4e9bca84b04cd37ccc257e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇以 Hify（一个面向 20-50 人内部使用的 AI Agent 平台）的 Agent 管理模块为载体，演示"用 Claude Code 把一个陌生业务概念从零理解到落地交付"的全过程。文章重排为两部分：第一部分是可速查的方法论手册，第二部分是可照做的实战教材。

### 1.1 阅读地图

<img src="imgs/aicent-15-fea-agent-module/7bc47dc56ca6b7a9aa5369c053abb146_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
flowchart TD
    A[本篇导读] --/> B[第一部分 方法论提炼]
    A --/> C[第二部分 实战演示]

    B --/> B1[第2章 核心方法论]
    B --/> B2[第3章 速查 Check List]

    B1 --/> B1a[2.1 领域四问]
    B1 --/> B1b[2.2 概念到数据结构映射]
    B1 --/> B1c[2.3 复杂CRUD拆解法]
    B1 --/> B1d[2.4 手动指令 vs Skill]

    C --/> C0[第4章 实战背景]
    C --/> C1[第5章 领域四问理解Agent]
    C --/> C2[第6章 数据建模]
    C --/> C3[第7章 智能客服落地]
    C --/> C4[第8章 CRUD拆解]
    C --/> C5[第9章 执行与验证]
    C --/> C6[第10章 前端对接]
    C --/> C7[第11章 验收与延伸]
-->


### 1.2 两种读者的推荐路径

| 读者画像   | 推荐路径                             |
| ------ | -------------------------------- |
| 新手系统学习 | 第 2 章 → 第 3 章 → 第 5-11 章（照做一遍）   |
| 熟练者速查  | 第 2 章 → 第 3 章（速查）→ 第 6/8 章（决策对比） |

## 2. 核心方法论：从陌生概念到可交付模块

<img src="imgs/aicent-15-fea-agent-module/01b8290a5a7555c42e882fe145f11c2b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇要带走的核心能力，不是"Agent 模块本身"，而是这套"<span style="color: red; font-weight: bold;">现学现卖</span>"的方法论：遇到完全陌生的业务概念（如审批引擎、支付系统、数据管道），也能用同样的路径快速进入并交付。

### 2.1 领域四问：把陌生概念速理解

#### (1) 触发条件

当开发者面对一个新概念（如 Agent、Workflow、RAG），<span style="color: red; font-weight: bold;">不要直接开始写代码。先用一段结构化提示词让 Claude Code 扮演领域专家做产品层面的梳理。</span>

#### (2) 提示词模板

```text
在 {应用场景，如 AI 应用平台 Dify} 里，{概念} 是什么概念？它和 {参照物，如普通的对话} 有什么区别？用户创建一个 {概念} 需要配置哪些东西？从产品层面帮我梳理。
```

#### (3) 期望产出

回答应至少包含三个层次：

* 核心定义（一句话能讲清）
* 与相邻概念的差异（用表格对比）
* 用户视角的配置项清单（分层组织）

若任何一层缺失，需要追问。

#### (4) 验证理解的标志

<span style="color: red; font-weight: bold;">能把**概念**映射为**存储结构（数据库表/对象模型）**，就说明真正理解了。</span>映射不出来就回到 2.1 继续问。

### 2.2 概念到数据结构的映射法

#### (1) 三步映射

##### ① 列出概念的所有属性

把领域四问得到的配置项逐条列出。

*例如 Agent 三层配置（身份定义/能力绑定/运行参数）共 8 个属性。

##### ② 区分"实体属性"与"关联关系"

| 类型   | 处理方式        | 示例                                 |
| ---- | ----------- | ---------------------------------- |
| 实体属性 | 属性直接成为字段    | Agent 的名称、temperature、max_tokens 等 |
| 关联关系 | 多对多关系单独建关联表 | Agent 与工具（agent_tool 关联表）          |

##### ③ 对每个存储决策做对比选型

不要把"AI 给的方案"当默认答案。<span style="color: red; font-weight: bold;">AI 擅长对比但不擅长选型——选型能力只能慢慢养成。</span>

#### (2) 关键存储决策速判表

| 决策点            | 候选方案                        | 选择倾向    | 判断依据                                 |
| -------------- | --------------------------- | ------- | ------------------------------------ |
| 固定参数存储         | 字段打散 / JSON 列 / 混合          | 打散      | 参数对所有实体相同、数量少（≤5）、需 SQL 过滤           |
| 异构参数存储（按供应商变化） | 字段打散 / JSON 列               | JSON    | 字段按类型完全不同，打散不可行                      |
| 多对多关联粒度        | 绑聚合（如 Server）/ 绑细粒度（如 Tool） | 绑聚合     | 内部使用场景，新条目自动生效，无需精细管控                |
| 浮点参数类型         | FLOAT / DECIMAL             | DECIMAL | 避免精度问题（如 temperature 用 DECIMAL(3,2)） |
| 关联表索引          | 单列 / 联合唯一                   | 联合唯一    | 防止重复绑定（如 UNIQUE(agent_id, tool_id)）  |

#### (3) 反模式警示

##### ① 同样的技术手段到处套

<span style="color: red; font-weight: bold;">JSON 存储在 auth_config 是必需的，但在 Agent 参数上就是过度设计。每个场景要单独判断。</span>

##### ② 过度设计未来扩展

"以后可能要加参数"不是用 JSON 的理由。

*例如第二部分例子中：当前参数就 3 个，ALTER TABLE 的成本远低于 JSON 解析的长期成本。

### 2.3 复杂 CRUD 的拆解法

#### (1) 拆解顺序

<span style="color: red; font-weight: bold;">不要按"创建/查询/更新/删除"平铺，按"创建路径为主线，其他路径对比差异"展开。因为创建路径最复杂，把它拆透，其他路径是简化版。</span>

#### (2) 创建路径的标准步骤

```text
前端请求 → Controller 参数校验（@Valid）
       → Service 业务校验（唯一性、跨模块存在性）
       → @Transactional 事务内写入（主表 + 关联表）
       → @CacheEvict 清缓存
       → 返回详情响应
```

#### (3) 三个易漏点

##### ① 跨模块校验走 Service 接口而非 Mapper

校验 modelConfigId 是否存在，应调 ProviderService 接口，不直接查 model_config 的 mapper。这是模块边界的硬规范。

##### ② 关联表更新优先全量替换

数据量小的关联表（如 agent_tool），DELETE 再 INSERT 比增量 diff 简单得多，性能问题可忽略。

##### ③ 语义有歧义的接口必须拆开

"toolIds 不传"是"清空"还是"不修改"？两种语义都合理但实现不同。拆成两个独立接口：

```text
PUT /api/v1/agents/{id}           # 更新基本信息（不含 toolIds）
PUT /api/v1/agents/{id}/tools     # 全量替换工具列表
```

#### (4) 删除策略的取舍

| 取舍点 | 选择 | 理由 |
|---|---|---|
| 主表删除方式 | 逻辑删除（deleted=1） | 历史可追溯 |
| 关联表删除方式 | 物理删除 | 关联表无逻辑删除意义 |
| 进行中的会话拦截 | 不拦截 | agent 删了，对话自然找不到配置返回错误，接受该行为 |
| 历史会话外键 | 不处理 | 历史会话保留 |

### 2.4 手动指令 vs Skill 的选型标准

#### (1) 两种驱动方式

##### ① 手动指令

每一步都自己写提示词，Claude Code 按步执行。过程慢但每一步开发者都在学习。

##### ② Skill 驱动

用一句话启动模块交付 Skill，Claude Code 自动按"梳理需求 → 设计数据模型 → 等确认 → 按层拆解执行"流程走。开发者只在关键决策点拍板。

#### (2) 选型标准

| 维度 | 手动指令优先 | Skill 优先 |
|---|---|---|
| 业务概念熟悉度 | 第一次接触 | 已熟悉同类模块 |
| 学习目标 | 过程本身就是学习 | 只要交付结果 |
| 流程可复用性 | 一次性 | 模式清晰可沉淀为 Skill |
| 关键决策密度 | 高（每步都要判断） | 低（只在选型点拍板） |

#### (3) Skill 启动提示词模板

```text
按模块交付 Skill 的流程，帮我做 {模块名}。{一句话概念定义}。关联 {上游模块} 和 {下游模块}（关系：一对多/多对多）。先从第一步开始。
```

## 3. Agent 模块速查 Check List

<img src="imgs/aicent-15-fea-agent-module/13979386d1c75fd9e0f09663314421a1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本 Check List 可裁剪。在项目启动时复制到工单或 PR 描述里，按项打勾。

### 3.1 领域理解阶段

- [ ] 用领域四问提示词让 Claude Code 做产品层面梳理
- [ ] 产出三层配置清单（身份定义 / 能力绑定 / 运行参数）
- [ ] 明确"做"与"不做"的边界（如 Agent 不做自主多步推理、不做 Agent 间调用）
- [ ] 用一个具体场景（如智能客服）验证理解，把配置项逐一填出实际值

### 3.2 数据建模阶段

- [ ] 列出所有属性，区分"实体属性"与"关联关系"
- [ ] 主表 DDL（含 deleted、enabled、created_at、updated_at、索引）
- [ ] 多对多关联表（含联合唯一索引）
- [ ] 复用已有外键（如 chat_session.agent_id 不新建表）
- [ ] 参数存储方案选型（固定参数打散 / 异构参数 JSON）
- [ ] 多对多粒度决策（绑聚合 / 绑细粒度）
- [ ] 浮点参数用 DECIMAL 不用 FLOAT

### 3.3 CRUD 拆解阶段

- [ ] 创建路径：参数校验 → 唯一性校验 → 跨模块校验（走 Service）→ 事务写入 → 清缓存
- [ ] 列表查询：避免 JOIN 与 N+1，用批量 IN + GROUP BY
- [ ] 详情查询：加 @Cacheable
- [ ] 更新工具列表：全量替换（DELETE + INSERT）
- [ ] 语义歧义接口拆分（基本信息与工具绑定分开）
- [ ] 删除策略：主表逻辑删、关联表物理删、历史会话不处理

### 3.4 执行与验证阶段

- [ ] Service 实现提示词写清每步前置条件与异常情况
- [ ] 用 curl 验证所有接口（创建/查询/更新/工具绑定/删除）
- [ ] 删除后查询验证错误码（如 3000 Agent 不存在）
- [ ] 边界用例验证（modelConfigId 不存在返回 2005、name 重复返回 3001）

### 3.5 前端对接阶段

- [ ] 列表页：名称、关联模型名、工具数量、temperature、enabled、创建时间
- [ ] 表单：System Prompt 至少 6 行 textarea
- [ ] temperature 用 slider（0-1，步长 0.1）
- [ ] 模型下拉联动 Provider（按供应商分组）
- [ ] 工具绑定单独 tab 或区域

### 3.6 经验沉淀阶段

- [ ] 把本次验证过的规范补进 CLAUDE.md
- [ ] 把可复用的提示词存为片段
- [ ] 评估是否可沉淀为 Skill

## 4. 实战背景：Hify Agent 模块定位

<img src="imgs/aicent-15-fea-agent-module/9d81e7edd38c6eae53e7b6030c911b15_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 4.1 为什么用 Agent 模块做"现学现卖"示范

开发者接到的大部分需求，一开始都是不懂的。以前的做法是搜资料、问同事、翻文档，耗时长。现在配合 Claude Code 与本系列教的方法论（领域理解 → 数据建模 → 拆解执行），陌生概念可以快速搞懂并落地。

本篇的核心不是 Agent 模块本身，而是这个"现学现卖"的全过程示范。Hify 的 Agent 模块恰好是一个合适的载体——开发者假设自己对 Agent 概念理解模糊（"知道是能用工具的 AI，但说不清数据怎么存、和模型什么关系"），用方法论把它从零做到交付。

### 4.2 技术栈

实战部分紧扣以下技术栈，避免空谈：

| 层 | 技术选型 |
|---|---|
| 后端框架 | Spring Boot |
| 持久层 | MyBatis + MySQL |
| 缓存 | Spring Cache（@Cacheable / @CacheEvict） |
| 前端框架 | Vue 3 |
| UI 组件库 | Element Plus |
| 业务封装 | HifyTable、HifyFormDialog（项目内封装） |
| AI 工具 | Claude Code（指令驱动 + Skill 驱动） |

## 5. 用领域四问理解 Agent

<img src="imgs/aicent-15-fea-agent-module/73227a8dfc850916c1a7030aa0437671_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 5.1 直接复用领域四问

第 14 篇刚学了领域快速理解四问，这里直接用。开发者给 Claude Code 的提示词如下：

```text
在 AI 应用平台（比如 Dify）里，Agent 是什么概念？它和普通的对话有什么区别？用户创建一个 Agent 需要配置哪些东西？从产品层面帮我梳理。
```

Claude Code 的输出如下：

<img src="imgs/aicent-15-fea-agent-module/c786de5bab4a7422e7576c8243155531_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/c786de5bab4a7422e7576c8243155531_MD5.jpg
用途：展示用领域四问提示词向 Claude Code 提问"Agent 是什么概念"后得到的终端输出，作为本节建立 Agent 认知的依据
内容：深色背景白色文字的结构化回答，包含四部分——①标题"Agent 是什么"+核心定义（Agent 可分为一次性智能体：用户需求+LLM 生成文本+检索/思考/记忆/调用工具/回答目标；也可分为长期智能体：根据目标持续执行任务、依据工具反馈决定下一步，核心在于 Tool-Use+多轮自主决策）；②"和普通对话的区别"对比表格（普通对话 vs Agent，从输入输出、工具调用、自主决策、记忆、身份设定、体验效果六维度对比）；③"用户创建 Agent 需要配置什么"分三层——第一层身份定义（角色、描述、System Prompt）、第二层能力设定（模型选择、工具配置、知识配置）、第三层运行参数（Temperature、最大上下文长度、最大输出 token）
-->

### 5.2 关键认知：普通对话 vs Agent

输出帮开发者建立了清晰的认知：

- **普通对话**是一次性问答——用户发消息、LLM 返回文本、结束。没有记忆，没有工具，没有目标感。
- **Agent** 是有目标、能行动的对话主体。它不只是回答问题，而是根据目标调用工具、根据结果决定下一步。核心差异在于有没有 Tool Use + 多轮自主决策。

<img src="imgs/aicent-15-fea-agent-module/4d777040517080ad60d830cadde47701_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/4d777040517080ad60d830cadde47701_MD5.jpg
用途：用对比表格直观呈现"普通对话"与"Agent"在四个核心维度上的本质差异，呼应正文"Tool Use + 多轮自主决策"的结论
内容：4 行 3 列对比表格（浅米色背景）。第 1 列为特征维度（加粗）：工具调用、自主决策、身份感、执行轮次；第 2 列"普通对话"依次为：无、无、无、一轮；第 3 列"Agent"（加粗）依次为：可调用外部工具、根据中间结果决定下一步、有名字/性格/职责定义、多轮直到目标完成。整体强调 Agent 相较普通对话在工具扩展、动态决策、个性化身份、多轮迭代四方面的智能代理属性
-->

### 5.3 Agent 的三层配置

创建一个 Agent 要配三层东西：

<img src="imgs/aicent-15-fea-agent-module/6f6b8ec4b23617ef63c14c9b86e42785_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/6f6b8ec4b23617ef63c14c9b86e42785_MD5.jpg
用途：用左右对照图说明"创建 Agent 要配三层东西"——左侧三层配置面板与右侧"智能客服 Agent"具体实例一一对应
内容：左侧为三层嵌套配置面板——身份定义（浅紫边框：名称、描述、System Prompt）、能力绑定（浅绿边框：绑定模型 model_config、绑定工具 mcp_server）、运行参数（浅红边框：temperature 0.3、max_tokens 1024）；右侧为"智能客服 Agent"实例（米色背景），列出 名称=Hify 智能客服、Prompt="你是客服助手..."、模型=GPT-4o、工具=(暂无)、temperature=0.3、max_tokens=1024、context_turns=8；中间用虚线把左侧三层与右侧对应字段连接起来。底部附比喻："模型是引擎，Agent 配置是方向盘"
-->

#### (1) 第一层：身份定义

名称、描述、System Prompt（角色指令，定义性格、职责、回答风格、边界约束——这是 Agent 的"灵魂"）。

#### (2) 第二层：能力绑定

绑定模型（选哪个 Provider 的哪个 model_config）、绑定工具（选哪些 MCP Server）、绑定知识库（选哪些 Knowledge Base，后面做 RAG 时再讲）。

#### (3) 第三层：运行参数

temperature（创意度，0= 严谨，1= 发散）、最大输出 token、最大上下文轮数（保留最近几轮对话，影响记忆深度和 token 成本）。

### 5.4 Hify 的 Agent 边界

Claude Code 还给了一个关键判断——Hify 的 Agent 边界：

<img src="imgs/aicent-15-fea-agent-module/41b543dffed9626bac14c82cc4d121f4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/41b543dffed9626bac14c82cc4d121f4_MD5.jpg
用途：Claude Code 给出的"Hify Agent 边界"判断结论图，用"做/不做"对比明确 Hify Agent 的设计范围
内容：深色背景说明图，分为三部分：标题"Hify Agent 的边界"；前置说明"结合你的 CLAUDE.md，Hify 的 Agent 设计范围"；"做"列出三项——Agent 绑模型/绑 MCP 工具/配 System Prompt、Agent 发起对话（chat_session 关联 agent_id）、对话时走 RAG 检索知识库；"不做"列出三项——不做 Agent 自主多步推理（ReAct/Function Calling 循环，是 Workflow 的事）、不做 Agent 之间互相调用、不做 Agent 记忆持久化（上下文靠 Redis session）；定位总结"Hify 的 Agent 更接近'有身份的对话配置模板'，而不是完整的 Autonomous Agent，对 20-50 人内部使用合适"
-->

| 维度 | 做 | 不做 |
|---|---|---|
| 工具 | Agent 绑模型、绑 MCP 工具、配 System Prompt | 不做 Agent 自主多步推理（ReAct / Function Calling 循环，那是 Workflow 的事） |
| 调用 | Agent 发起对话 | 不做 Agent 之间互相调用 |
| 记忆 | 上下文靠 Redis session | 不做 Agent 记忆持久化 |

也就是说 Hify 的 Agent 更接近**有身份的对话配置模板**，而不是完整的 Autonomous Agent。这个定位对 20-50 人内部使用是合适的，够用，不过度复杂。

开发者会发现：**Claude Code 不止是一个写代码的程序员，还是一个专家，一个导师**。

## 6. 数据建模实战：从概念映射到表结构

<img src="imgs/aicent-15-fea-agent-module/7a3ddf0a54de6d5ea648e8841f029ad0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 6.1 用提示词加速映射

理解了 Agent 是什么，下一步自然是：这些信息怎么存？在以往的流程中，需要再花大量时间去理解，才有可能把 Agent 映射为程序的语义。一般情况下，当一个概念能被映射为存储的结构表示，就说明已经理解它了。

开发者让 Claude Code 帮忙加速这个过程，提示词如下：

```text
基于刚才的分析，Agent 在数据库里应该怎么存？需要哪些表？表之间什么关系？特别是：System Prompt 用什么类型、模型参数怎么存、Agent 和工具的多对多关系怎么处理。
```

输出内容较多不贴出。要点是：Claude Code 给了完整的数据模型设计，还**主动对比了参数存储的三种方案**。AI 很擅长对比，但这里考验的是开发者的选型决策能力——这点只能慢慢养成。

### 6.2 表结构总览

3 张表就够：agent 主表、agent_tool 关联表。chat_session 已有 agent_id 外键不需要新表。知识库关联先不做，等 RAG 模块开发时再加 agent_knowledge 关联表。

### 6.3 参数存储的三方案对比

Claude Code 对比的三种方案：

| 方案 | 实现 | 优点 | 缺点 |
|---|---|---|---|
| 方案 A（字段打散存） | temperature、max_tokens、max_context_turns 各一列 | 查询直接、类型约束清晰 | 加参数要 ALTER TABLE |
| 方案 B（JSON 列存） | 全部塞一个 JSON 字段 | 灵活，加参数不改表 | 无法 SQL 直接过滤，多一层解析 |
| 方案 C（混合） | 固定参数打散，扩展参数放 JSON | 兼顾两者 | 实现复杂度上升 |

开发者的判断：**选方案 A**。Hify 当前参数就三个，不过度设计。和第 13 篇 auth_config 用 JSON 的决策不同——auth_config 的字段按供应商类型完全不同，JSON 是必须的；Agent 参数对所有 Agent 都一样，打散存更简单。<span style="color: red; font-weight: bold;">同样的技术手段不是到处套用，要看具体场景</span>。

### 6.4 绑 Server 还是绑 Tool

Claude Code 提了一个开发者没想到的问题：agent_tool 关联的是整个 MCP Server，还是 Server 下的某个具体工具？

| 选项 | 含义 | 代价 |
|---|---|---|
| 绑 Server | Agent 自动获得该服务的所有工具，新工具自动生效 | 无法精细管控单个工具 |
| 绑 Tool | 精细管控每个工具 | 配置繁琐，新增工具需手动绑定 |

开发者的判断：**绑 Server**。20-50 人内部使用，不需要精细管控到单个工具。简单优先。

### 6.5 最终表结构（DDL）

```sql
CREATE TABLE agent (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500) NOT NULL DEFAULT '',
    system_prompt TEXT COMMENT '角色指令，可以很长',
    model_config_id BIGINT NOT NULL COMMENT '绑定的模型配置',
    temperature DECIMAL(3,2) NOT NULL DEFAULT 0.70 COMMENT '0.00~1.00',
    max_tokens INT NOT NULL DEFAULT 2048,
    max_context_turns INT NOT NULL DEFAULT 10 COMMENT '保留最近几轮上下文',
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_agent_model_config_id (model_config_id)
) COMMENT 'Agent 配置';

CREATE TABLE agent_tool (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    agent_id BIGINT NOT NULL,
    tool_id BIGINT NOT NULL COMMENT '关联 mcp_server.id',
    created_at DATETIME NOT NULL,
    UNIQUE KEY uk_agent_tool (agent_id, tool_id),
    INDEX idx_agent_tool_agent_id (agent_id)
) COMMENT 'Agent 与工具关联';
```

### 6.6 DDL 设计细节

#### (1) temperature 用 DECIMAL(3,2) 不用 FLOAT

避免浮点精度问题。

#### (2) max_context_turns 直接存在 agent 表上

对话引擎读取时不需要额外查询。

#### (3) agent_tool 加联合唯一索引

`UNIQUE KEY uk_agent_tool (agent_id, tool_id)` 防止重复绑定。

#### (4) ER 关系总览

<img src="imgs/aicent-15-fea-agent-module/4d1f10a099bde25d506919909fb0706f_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/4d1f10a099bde25d506919909fb0706f_MD5.jpg
用途：展示 agent 与 agent_tool 两张核心表，及其与 model_config、chat_session、mcp_server 等已存在表的 ER 关系图，用于可视化上一段 DDL 的表结构与外键关联
内容：ER 图（实体-关系图）。核心表为 agent（紫色，含 id / name / system_prompt / model_config_id / temperature / max_tokens / max_context_turns 等字段）与 agent_tool（粉色，含 agent_id、tool_id 字段，agent_id 关联 agent.id，tool_id 关联 mcp_server.id）。关系连线均为 1:N：model_config(已存,1)→agent(N)、agent(1)→chat_session(N, 后面板)、agent(1)→agent_tool(N)、mcp_server(已存,1)→agent_tool(N)。图中不同表用不同颜色区分，已存在表标注"（已存）"，后台表标注"（后面板）"；底部附注"参数打散存（3个固定参数）：工具库 Server 不明卡 - 联合索引 - 引用防重复"。
-->

## 7. 场景落地：智能客服的配置思考

<img src="imgs/aicent-15-fea-agent-module/fc556ed88b091d305123740320415aa8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 7.1 为什么选智能客服做主线

Hify 是一个 AI Agent 平台，它的价值是：可以在上面创建各种 LLM 应用。智能客服、代码审查助手、数据分析顾问、会议纪要生成器，这些本质上都是不同配置的 Agent。一个平台，无限种可能。

本篇用智能客服来展开。这是最典型的企业 AI 落地场景，也是后面整个系列的主线——对话引擎做完后用它测试对话，RAG 做完后给它加产品知识库，MCP 做完后给它绑查订单工具。从本篇开始，智能客服会贯穿到系列结束。

<img src="imgs/aicent-15-fea-agent-module/a51ec3cdae262656620cf1e5f008f08d_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/a51ec3cdae262656620cf1e5f008f08d_MD5.jpg
用途：用一张图同时讲清"Hify 平台=一个模型驱动多种 LLM 应用"的定位，以及智能客服作为系列主线的成长路线
内容：上半部分（灰色圆角框）列出 Hify 平台的 4 个 LLM 应用——智能客服（t=0.3 稳定，系列主线）、代码审查（t=0.1 严谨，读代码仓库）、创意写作（t=0.9 发散，无工具）、数据分析（t=0.2 精确，查数据库），四者通过中间米色框汇聚到同一个模型 GPT-4o，下方标注"模型是引擎，Agent 配置是方向盘"；下半部分（淡紫色圆角框）展示智能客服的成长路线（系列主线）：第 15 篇配置/创建 Agent → 第 16-17 篇对话能聊天了 → 第 20-21 篇 RAG 有知识了 → 第 24-25 篇 MCP 能查订单了
-->

### 7.2 把智能客服对应到数据结构

如果开发者是产品经理，会怎么定义这个智能客服？智能客服顾名思义，就是能根据不同问题给出答案的虚拟人。那么它怎么对应到上面的数据结构？一一对比。

#### (1) 选模型：GPT-4o

为什么不选更便宜的 GPT-3.5-turbo？客服场景需要准确理解用户的问题，尤其是涉及产品功能的专业描述。3.5 容易理解偏差，4o 更稳。成本上，内部 20-50 人的使用量，4o 的费用完全可控。

#### (2) 写 System Prompt：Agent 的灵魂

不是随便写一句"你是客服"就行了，每一条指令都有用意：

```text
你是 Hify 平台的智能客服助手，负责解答用户关于产品功能、使用方法、常见问题的咨询。语气专业友好，回答简洁明了。如果用户的问题超出你的知识范围，诚实告知并引导联系人工客服。不编造不确定的信息。
```

逐句拆解：

| 指令片段 | 用意 |
|---|---|
| 语气专业友好 | 不要太机械也不要太随意 |
| 回答简洁明了 | 客服场景用户要的是答案不是长篇大论 |
| 超出知识范围诚实告知 | 最关键的一条，防止模型"幻觉"编造不存在的功能 |
| 引导联系人工客服 | 给用户一个兜底方案 |

#### (3) 调参数：temperature 设 0.3

为什么不是默认的 0.7？客服回答要稳定可靠，同一个问题问两次，答案应该基本一致。temperature 越高越有创意，但也越不可控，客服场景要的是可靠不是创意。如果是创意写作助手，可能会设 0.8 甚至 0.9。

#### (4) max_context_turns 设 8

为什么不是 20？每多保留一轮对话上下文，就多消耗一轮的 token 费用。客服场景大部分问题 3-5 轮就解决了，8 轮留够余量。设太大会浪费 token，而且太长的上下文反而会让模型"走神"。

#### (5) 工具暂时不绑

MCP 工具接入在后面的系列里讲。到时候可以给客服绑一个"查订单状态"的工具、一个"搜索产品知识库"的工具，客服就不只是靠模型的通用知识回答了，而是能查真实数据。

### 7.3 配置不是随便填的

到这里，智能客服和 Agent 的数据存储结构已经对应上了。读者会发现<span style="color: red; font-weight: bold;">配置不是随便填的，每个值背后都有产品思考</span>。同样的模型，换一套配置就是完全不同的应用。

<img src="imgs/aicent-15-fea-agent-module/526d74a99046e03994e5533561a8f2c6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/526d74a99046e03994e5533561a8f2c6_MD5.jpg
用途：用对比表格直观说明"同一模型搭配不同 Agent 配置就是不同应用"的核心观点
内容：一张 5 列 4 行的配置对比表，表头为「应用 / 模型 / temperature / Prompt 核心指令 / 工具」。四行分别为智能客服、代码审查、创意写作、数据分析四个应用场景——四行「模型」列均填写同一个 GPT-4o（即不变的"引擎"），而 temperature（0.3/0.1/0.9/0.2）、Prompt 核心指令（专业友好不编造 / 严格按规范逐行审查 / 发散思维大胆联想 / 基于数据说话给出结论）、工具（查订单搜知识库 / 读代码仓库 / 无 / 查数据库）三列则各不相同（即不同的"方向盘"）。通过模型列恒定、其余配置列逐行变化，视觉化呈现"模型是引擎，Agent 配置是方向盘"的比喻。
-->

<span style="color: red; font-weight: bold;">模型是引擎，Agent 配置是方向盘。</span>Hify 的价值就是让开发者可以自由组装这些方向盘。

## 8. CRUD 拆解实战

<img src="imgs/aicent-15-fea-agent-module/195f4930de9481baaab78431b634ac27_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 8.1 拆解的起点

智能客服的配置想清楚了，接下来回到技术实现，Agent 模块的 CRUD 怎么做。开发者给 Claude Code 的提示词如下：

```text
帮我拆解 Agent CRUD 的完整逻辑：从前端点保存到数据库落库，中间要经过哪些步骤？把创建、查询、更新、删除四个场景都拆解出来。
```

Claude Code 的拆解让开发者意识到 Agent CRUD 远不是简单的单表操作。

### 8.2 创建路径

```text
前端发 POST 请求
  → Controller 参数校验（name 非空、modelConfigId 非空、temperature 0~1）
  → Service 检查 name 唯一性
  → 跨模块校验 modelConfigId 存在且 enabled（调 ProviderService 接口，不直接查 mapper）
  → INSERT agent 主表
  → 如果 toolIds 非空，批量 INSERT agent_tool
  → 清除缓存
  → 返回详情
```

### 8.3 列表查询：避免 JOIN 与 N+1

先分页查 agent，再批量查各 agent 的工具数量：

```sql
SELECT agent_id, COUNT(*) FROM agent_tool WHERE agent_id IN (...) GROUP BY agent_id
```

不 JOIN，不 N+1——批量 IN 查询是最优平衡。

### 8.4 详情查询

查 agent + 查关联的 mcp_server 列表，组装完整响应。加 @Cacheable。

### 8.5 更新工具列表：全量替换 vs 增量 diff

Claude Code 对比了两种方案：

| 方案 | 实现 | 适用场景 |
|---|---|---|
| 方案 A（全量替换） | DELETE 再 INSERT | 关联表数据量小，性能可忽略，逻辑简单 |
| 方案 B（增量 diff） | 计算增删集合，分别操作 | 数据量大或需要审计 |

Claude Code 推荐方案 A，agent_tool 数据量小，全删重插没性能问题，逻辑简单。开发者同意：**不是所有场景都需要最优雅的方案，够用且简单就是最好的**。

### 8.6 接口语义拆分：基本信息与工具绑定分开

Claude Code 提了一个开发者没想到的设计问题：toolIds 不传的时候是"清空工具"还是"不修改工具"？两种语义都合理，但实现不同。Claude Code 建议拆成独立接口：

```text
PUT /api/v1/agents/{id}           # 更新基本信息（不含 toolIds）
PUT /api/v1/agents/{id}/tools     # 全量替换工具列表
```

语义更清晰，不存在歧义。开发者拍板：就这样。

### 8.7 删除策略

不做对话会话拦截——agent 删了，进行中的对话自然找不到 agent 配置返回错误，接受这个行为。级联删 agent_tool（物理删除，关联表没有逻辑删除的意义），agent 本身逻辑删除（deleted=1）。chat_session 里的 agent_id 不处理，历史会话保留。

### 8.8 流程总览

总结的流程如下：

<img src="imgs/aicent-15-fea-agent-module/e97c723879e7da17a9adea19012dc5fc_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/e97c723879e7da17a9adea19012dc5fc_MD5.jpg
用途：展示 Agent CRUD 的完整流程图（以 POST 创建路径为主线），用于本节"总结的流程如下"处可视化整套 CRUD 的分层执行链路
内容：以前端 POST /api/v1/agents 请求为起点，依次经过 Controller @Valid 参数校验（name/modelConfigId 非空）→ 检查 name 唯一性（SELECT COUNT by name）→ 跨模块校验 modelConfigId 存在且 enabled（调 ProviderService）→ @Transactional 事务内批量写入 agent 与 agent_tool 表（INSERT agent、INSERT agent_tool，含批量写入 toolIds 如有）→ @CacheEvict 清缓存 → 返回 AgentDetailResponse；整体为线性无分支的成功路径，串联参数校验、唯一性校验、跨模块校验、事务写入、缓存清理与响应等环节
-->

## 9. 逐步执行与验证

<img src="imgs/aicent-15-fea-agent-module/e41a078b2850b3cb3ee117da285f2fd4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 9.1 按层拆，每步可验证

需求拆解清楚了，按第 13 篇建立的标准流程——按层拆，每步可验证。

<img src="imgs/aicent-15-fea-agent-module/9ac290446659cf635c1f410c33f17ee8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/9ac290446659cf635c1f410c33f17ee8_MD5.jpg
用途：展示按 13 讲标准流程对 Agent CRUD 功能做"按层拆、每步可验证"的逐步执行任务清单
内容：将 Agent 创建/查询/更新/删除等业务拆解为分层的可执行任务列表，逐条标注每一步要做什么、产出什么，便于按顺序交付并逐项验证；图片起到本节"逐步执行"小节开篇的总览作用
-->

因为篇幅原因，这里只重点说任务 3。

### 9.2 Service 创建的提示词

```text
在 hify-agent 中实现创建 Agent 的 Service 方法。接收 name、description、systemPrompt、modelConfigId、temperature、maxTokens、maxContextTurns、toolIds。第一步检查 name 唯一性。第二步跨模块校验 modelConfigId——调 ProviderService 的接口，不直接查 model_config 的 mapper（跨模块走 Service 接口，CLAUDE.md 规范）。第三步在 @Transactional 事务中 INSERT agent 和批量 INSERT agent_tool。第四步 @CacheEvict 清除 agent 列表缓存。返回 AgentDetailResponse。
```

关键是把每一步的前置条件和异常情况都写清楚。如果只说"创建 Agent"，Claude Code 大概率漏掉 name 唯一校验或者忘了跨模块调 Service 而直接查 mapper。<span style="color: red; font-weight: bold;">需求描述的细致程度决定了代码的正确程度</span>。

### 9.3 实际输入指令（格式混乱也能理解）

此时会发现，好像没指令诶，怎么搞？开发者实际的输入指令如下：

<img src="imgs/aicent-15-fea-agent-module/e1e236576fc787b8a2dbbd597df08a7f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/e1e236576fc787b8a2dbbd597df08a7f_MD5.jpg
用途：展示开发者在 Claude Code 中输入的真实指令原文，用以说明实际开发中指令常常是格式混乱的复制文本
内容：暗色编辑器界面中粘贴了一段"完成 agent 的 crud 接口"的任务清单文本，按 1-7 编号列出各步骤（Entity/Mapper、DTO、Service 创建/查询/更新/删除、Controller 对接）及每步的验证方式（编译通过、curl 测试、浏览器全流程），并在末尾用大段文字重点说明任务3——创建 Agent Service 的四步实现：name 唯一校验、跨模块调 ProviderService 校验 modelConfigId、@Transactional 事务中 INSERT agent 与批量 INSERT agent_tool、@CacheEvict 清缓存。文本为复制粘贴格式，换行不统一、无额外缩进，但仍可被 Claude Code 正确理解。
-->

这段内容其实是 9.2 节提示词的复制，格式混乱，不是一个标准的指令。但是 Claude Code 也能理解，也能完成主体的工作。<span style="color: red; font-weight: bold;">在 Agent 看来，这种格式的指令和人类看起来规范的指令是一样的</span>。这点是很有意思的。

### 9.4 Claude Code 的输出

它的输出如下：

<img src="imgs/aicent-15-fea-agent-module/40c85dab17a90322fadefdeae510c95f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/40c85dab17a90322fadefdeae510c95f_MD5.jpg
用途：展示 Claude Code 根据前面给出的非标准格式需求描述（任务 3：创建 Agent 的 Service 方法）一次性产出的完整代码成果汇总
内容：一张「全部通过。产出物汇总:」的任务完成验证表，含三列（任务/文件/验证结果），共 7 行任务：
1. 实体+Mapper：Agent.java、AgentTool.java、AgentMapper.java、AgentToolMapper.java（编译通过）；
2. DTO：Create/Update/ToolBind Request、Detail/List/Item Response、QueryRequest（编译通过）；
3. Service 创建：name 唯一 + modelConfigId，跨模块校验 + 事务两张表（code:200, id:1）；
4. Service 查询：详情带 toolIds、列表带 toolCount 批量 GROUP BY（分页结构正确）；
5. Service 更新：基本信息更新、工具列表独立接口全量替换（name/temperature 已更新）；
6. Service 删除：级联删 agent_tool + 逻辑删 agent（删后查询返回 3000）；
7. Controller：6 个接口 + 错误码校验（modelConfigId 不存在>2005、name 重复>3001）。
体现了 Claude Code 仅凭一段格式混乱的需求描述，就按层（实体→Mapper→DTO→Service→Controller）一次性交付完整 CRUD 代码并通过逐层验证。
-->

### 9.5 curl 验证

此时开发者也不知道能不能跑，于是给了下面这个提示词：

```text
用 curl 验证 agent crud 的接口是否能运行，不能运行就修复
```

输出如下：

<img src="imgs/aicent-15-fea-agent-module/f3867392e8a1d19192580d3435e9ba6c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/f3867392e8a1d19192580d3435e9ba6c_MD5.jpg
用途：展示开发者用 curl 验证 Agent CRUD 全部接口后，Claude Code 生成的验证结果汇总，证明第一遍代码就能跑通无需修复
内容：终端中执行 curl 命令后的输出与接口测试汇总表，覆盖 6 个接口：POST /agents 返回 201、GET /agents 返回 200（分页列表含 toolCount）、GET /agents/{id} 返回 200（详情含 toolIds）、PUT /agents/{id} 返回 200（temperature 已更新为 0.7）、PUT /agents/{id}/tools/{id} 返回 200（工具列表绑定成功）、DELETE /agents/{id} 返回 200；删除后再查返回「3000 Agent 不存在」。表格标题「全部接口正常」，所有接口均通过测试
-->

惊喜发现，第一遍就没错，不用修改。

当然，读者可能见过那种全自动的，一跑跑几天，中间不用任何介入就把应用做出来的案例。这块可以在留言区讨论。

## 10. 前端对接

<img src="imgs/aicent-15-fea-agent-module/532b9222938e03355e5710010e90c53a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 10.1 Agent 管理页面提示词

接下来完成前端页面。Agent 的管理页面比 Provider 复杂一些，开发者给的提示词如下：

```text
用 HifyTable 和 HifyFormDialog 实现 Agent 管理页面。列表展示：名称、关联模型名、工具数量、temperature、enabled（tag）、创建时间。新增 / 编辑表单：名称（input）、描述（textarea）、模型选择（下拉，从 model_config 接口拉取可用模型，按供应商分组）、System Prompt（textarea，至少 6 行高度）、temperature（slider，0-1，步长 0.1）、max_tokens（number input）、max_context_turns（number input）。工具绑定单独一个 tab 或区域，多选 checkbox。
```

### 10.2 模型下拉联动 Provider

模型选择的下拉要联动 Provider——用 Element Plus 的 el-select + option-group，按供应商分组展示已启用的模型。

## 11. 验收与延伸

### 11.1 创建第一个 LLM 应用

Agent 模块做完了，后端接口跑通了，前端管理页面也对接了。现在打开浏览器，创建第一个 LLM 应用。

在 Agent 管理页面点"新增 Agent"，填入第 7 章设计好的智能客服配置：

| 字段 | 值 |
|---|---|
| 名称 | Hify 智能客服 |
| 描述 | 处理售前咨询和产品使用问题 |
| 模型 | GPT-4o |
| System Prompt | 贴入 7.2 节那段客服指令 |
| temperature | 0.3 |
| max_tokens | 1024 |
| max_context_turns | 8 |

<img src="imgs/aicent-15-fea-agent-module/023f4cc8877f189ef1e60eea0b2663b0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/023f4cc8877f189ef1e60eea0b2663b0_MD5.jpg
用途：展示 Agent 管理页面"新增 Agent"弹窗的实际填写效果，作为创建第一个 LLM 应用的操作验收截图
内容：弹窗标题为"新增 Agent"，含"基本配置"（当前激活）和"工具绑定"两个 Tab。基本配置表单已按要求预填智能客服配置：名称为"Hly 智能客服"（必填）、模型下拉选中"GPT-4o"（必填）、描述填"处理售前咨询和产品使用问题"、System Prompt 文本域贴入了完整的客服指令（专业友好、答不全则引导联系人工客服）。下方三个可调参数：Temperature 滑块拖至 0.3（范围 0-1）、最大输出输入框为 1024（带 +/- 按钮）、上下文轮数输入框为 8（提示 1-10 可调）。底部为灰色"取消"按钮和蓝色"确认"按钮。背景为置灰的 Agent 管理主页面，左侧可见深色侧边栏导航（Agent 管理、对话等），右上角为 Admin 用户信息和紫色"新增 Agent"入口按钮。
-->

点保存。列表里出现了"Hify 智能客服"，模型显示 GPT-4o，状态正常。

<img src="imgs/aicent-15-fea-agent-module/e71cb954b7c46888ee7ac5fdc72b777a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!-- 
图片内容说明
路径：imgs/aicent-15-fea-agent-module/e71cb954b7c46888ee7ac5fdc72b777a_MD5.jpg
用途：展示保存 Agent 后回到列表页的验收结果，证明第一个 LLM 应用"Hify 智能客服"已成功创建并生效
内容：Agent 管理列表页，表格列包含名称、关联模型、Temperature、工具数量、状态（enabled tag）和创建时间。首行数据为"Hify 智能客服"，模型为 GPT-4o，状态显示为正常启用，对应正文"列表里出现了 Hify 智能客服，模型显示 GPT-4o，状态正常"的验收描述。
-->

读者也可以试试自己再创建一个"代码审查助手"。同样选 GPT-4o，但 temperature 改 0.1，Prompt 完全不同。两个 Agent 并排显示在列表里，同一个模型，完全不同的用途。

至此，第一个 LLM 应用诞生了。

### 11.2 后续主线：智能客服会越来越强

现在它还不能对话——Agent 只是一份配置，还需要对话引擎来驱动它。下一篇做对话引擎，做完之后就能真正和智能客服聊天了。到讲 RAG 时，给它加上产品知识库，它就能回答具体的产品问题。到讲 MCP 时，给它绑上查订单工具，它就能帮用户查真实数据。

从配置到对话到知识到工具，智能客服会一步步变得越来越强。这就是后面系列的主线。

### 11.3 如果用 Skill 呢？

本篇开发者**全程用指令手动做**：从理解 Agent 概念、到设计数据模型、到拆解任务、到逐步执行。整个过程和第 13 篇做 Provider 几乎一模一样。

如果用第 14 篇写的模块交付 Skill，提示词如下：

```text
按模块交付 Skill 的流程，帮我做 Agent 管理模块。Agent 是模型 + 提示词 + 参数 + 工具的组合配置，关联 model_config 和 mcp_server（多对多）。先从第一步开始。
```

一句话启动，Claude Code 自动按 Skill 的流程走——梳理需求、设计数据模型、等确认、按层拆解执行。

两种方式的选择标准见第 2.4 节的对比表。本篇用手动是因为开发者第一次理解 Agent，过程本身就是学习。后面做对话引擎，如果模式和 Skill 匹配，直接用 Skill。

### 11.4 方法论回顾

本篇的核心重点是：假设开发者不知道 Agent 是什么，但可以用本系列教的方法论，实现从零理解到落地实现。

回顾整个过程：让 Claude Code 教 Agent 的概念（领域四问）→ 把概念映射成数据结构（3 张表、参数存法的方案对比、绑 Server 还是绑 Tool 的决策）→ 用智能客服场景让抽象落地 → 拆解复杂的 CRUD 逻辑（跨模块校验、事务、独立工具绑定接口）→ 逐步执行交付 → 发现流程可以用 Skill 一行搞定。

<span style="color: red; font-weight: bold;">这个"现学现卖"的能力才是读者真正要带走的</span>。以后遇到任何不懂的业务需求，如审批引擎、支付系统、数据管道等等，都可以这样做：让 Claude Code 教概念，把概念翻译成数据结构，用具体场景验证理解，然后拆解执行。<span style="color: red; font-weight: bold;">不是每个领域都要有三五年经验才能动手，有方法论就能快速进入。</span>

### 11.5 沉淀进 CLAUDE.md 的经验

几条值得补进 CLAUDE.md 的经验：

#### (1) 三条核心经验

##### ① 跨模块调用走 Service 接口不走 Mapper

这条已有，本篇验证了它的价值。

##### ② 关联表的更新优先考虑全量替换

数据量小时比 diff 简单得多。

##### ③ 语义有歧义的接口要拆开

基本信息和工具绑定分开更新。

### 11.6 思考

#### (1) 实践题

试一下：找一个工作中完全不懂的业务概念，用领域四问让 Claude Code 教，然后试着设计数据模型。整个过程花了多久？

#### (2) 多模型绑定

如果允许一个 Agent 同时绑定多个模型（主模型 + 备用模型，主模型不可用时自动切换），数据模型需要怎么调整？

#### (3) System Prompt 变量替换

Agent 的 System Prompt 如果需要支持变量替换（比如 `{{user_name}}`、`{{company_name}}`），实现方案是什么？
