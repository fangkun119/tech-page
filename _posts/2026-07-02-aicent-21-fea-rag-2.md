---
title: AI编程方法(1) 21：高级功能 - RAG（下）客服手册和对话集成
author: fangkun119
date: 2026-07-02 13:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-21-fea-rag-2/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---

<!--
aicent-21-fea-rag-2
AI编程方法 21：高级功能 - RAG（下）客服手册和对话集成
-->
## 1. 全文导读

<img src="imgs/aicent-21-fea-rag-2/4bb18732cf44dba2c7eb0baff8d2cec4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是「RAG 知识库」主题的下半部分。上一篇（第 20 篇）完成了探路：pgvector 装好了，Embedding API 调通了，两个能力分别验证通过。本篇把这两个能力变成 Hify 的真实功能——既要把文档组织成可检索的知识库，又要把检索结果接入已经跑通的对话引擎。

本篇的核心价值不在于 RAG 技术本身，而在于一个通用的工程命题：**在一个已经跑通、已有线上用户的系统里，怎样加一个新能力，同时保证旧功能一分都不能少。** 这个命题适用于 RAG，也适用于未来给任何系统加任何新能力。

全文分两部分：

- **第一部分（第 2 章）：方法论速查。** 把"在跑通的系统里加新能力"拆成可操作的方法论条目，配一张项目阶段 Check List。这一部分不深入具体技术栈，可快速复习、当参考手册。
- **第二部分（第 3–8 章）：实战演示。** 结合 Hify 项目、pgvector、MySQL + PostgreSQL 双数据源、Spring、Vue3 + Element Plus，复现"接入 RAG 知识库"的完整项目过程，解释每一步为什么这么做。

<img src="imgs/aicent-21-fea-rag-2/d2b4e636e3dafb3db3db3a71663b3a15_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicent-21-fea-rag-2.md/d2b4e636e3dafb3db3db3a71663b3a15_MD5.jpg
用途：展示章节内容划分和两部分之间的内容映射关系
内容：由以下mermaid生成
flowchart TD
    A["本篇主题：在跑通的对话系统里接入 RAG 知识库"] --\> P1["第一部分 方法论提炼"]
    A --\> P2["第二部分 实战演示"]
    P1 --\> M["第 2 章 在跑通的系统里加新能力<br/>方法论速查 + Check List"]
    P2 --\> S3["第 3 章 实战背景"]
    P2 --\> S4["第 4 章 第一步<br/>圈定改动范围"]
    P2 --\> S5["第 5 章 第二步<br/>设计数据模型"]
    P2 --\> S6["第 6 章 第三步<br/>实现数据管线"]
    P2 --\> S7["第 7 章 第四步<br/>接入对话引擎"]
    P2 --\> S8["第 8 章 第五步<br/>完整验收"]

    S4 -.->|"方法论落地"| M
    S6 -.->|"独立验收"| M
    S7 -.->|"增量三步走"| M
    S8 -.->|"三维度验证"| M
-->

读完本篇，可以带走三样东西：一套"加新能力不破坏旧功能"的方法论、一份可裁剪的项目 Check List、一个生产可用的知识库系统实现路径。

## 2. 在跑通的系统里加新能力——方法论速查

这一章把本篇（以及上一篇）沉淀的方法论提炼成速查条目。每条都给出：操作要领、思考关注点、适用场景。本章不展开具体技术栈，技术细节在第二部分。

### 2.1 最小侵入点：先圈死改动范围

<img src="imgs/aicent-21-fea-rag-2/a99718b4de8b174191bbce02735d660c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

**操作要领**：动手写代码之前，先回答两个问题——新能力应该插在已有链路的哪一步？插入之后上下文结构怎么变？把答案写进给 AI 编程助手的指令。

**思考关注点**：

- 不是所有"在已有系统里加功能"都需要大改，关键是找到最小侵入点。
- 要定位到具体的方法、具体的行：改的是哪个方法、在哪一行之前插入。
- 改动范围一旦圈死，就把它显式写进指令，作为防止改动扩散的第一道防线。

**适用场景**：任何在已有跑通系统上加功能的需求。

### 2.2 增量开发三步走：圈范围 → 保旧 → 验新

**操作要领**：<span style="color: red; font-weight: bold;">遵循固定节奏，顺序不能乱。</span>

```text
第一步：圈定改动范围（最小侵入点）
第二步：先验证旧功能没坏（跑原有用例）
第三步：再验证新功能生效（跑新用例）
```

**思考关注点**：

- <span style="color: red; font-weight: bold;">三步的顺序不能反。</span>如果先测新功能再测旧功能，旧功能出了问题，无法判断是这次改动导致的，还是原本就有的。
- 每一步都是对前一步的护栏。如果旧功能已经坏了，先回滚再排查，不要在旧功能坏掉的状态下继续往下走。
- 这套节奏适用于在任何跑通的系统里加任何新能力。

**适用场景**：改动涉及已有线上用户使用的链路时，尤其重要。

### 2.3 新能力先独立做完并验收，再接入主链路

**操作要领**：把新能力拆成独立模块，先单独做完并验收，不混在主链路的改动里。独立模块跑通后，再接入主链路。

**思考关注点**：

- 独立模块出了问题，只影响新功能；主链路出了问题，是线上故障。
- 分层独立后，出问题能精确定位是哪一层，不用在多个改动里互相干扰。
- 本篇的"数据管线"就是这样：新建的、不碰已有代码、相对独立，先单独做完验收，再接入 sendMessage。

**适用场景**：新能力本身自成体系、可与主链路解耦时。

### 2.4 用精确指令锁死扩散边界

**操作要领**：给 AI 编程助手下指令时，把"不要改什么"显式写出来，把扩散边界提前锁死。

**思考关注点**：

- AI 编程助手默认会举一反三。在新项目里这是优点，在有线上用户的系统里是风险——它可能判断"既然改了这个方法，顺便把上层参数也调整一下更合理"。
- 指令里要显式写约束，例如"不要修改流式调用、SseEmitter 转发、消息存储的逻辑。不要改 Controller 层。"
- 把约束写进指令，是防止改动扩散的第一道防线。

**适用场景**：所有在已有系统上的 AI 编程改造。

### 2.5 多步骤任务的代码组织：环节独立，管线串联

<img src="imgs/aicent-21-fea-rag-2/403501c0f2159794fcf8903069a48edf_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

**操作要领**：<span style="color: red; font-weight: bold;">多步骤串联任务，把每个环节拆成独立方法（或独立步骤），管线方法只负责串联和状态管理。</span>

**思考关注点**：

- 不只是为了代码整洁，更重要的是：某个环节出了问题，能精确定位是哪一步，单独测试、单独修复，而不是在一个几百行的大方法里翻来翻去。
- 这个原则适用于任何多步骤数据处理管线，不只是 RAG。

**适用场景**：数据管线、ETL、任何"上一步输出喂给下一步"的串联任务。

### 2.6 接口契约对齐与异步边界

**操作要领**：

- **接口契约对齐**：多步骤串联任务，先问清楚每个环节的输入是什么、输出是什么，且输出格式要能直接喂给下一个环节。对齐之后再写代码。
- **异步边界**：耗时操作（如文档解析、向量化）必须异步执行。接口只负责接收请求、建记录、提交异步任务，不占住请求线程。

**思考关注点**：

- 多步骤串联任务最容易出的错，就是环节之间数据格式对不上：上一步输出 A，下一步期望收到 B。把每个环节的输入输出先对齐，省去大量调试时间。
- 不能假设外部 API 返回顺序和输入顺序一致，要按返回数据里的 index 字段排序后再对应。
- 同步等待耗时操作会超时、会占住线程池，必须异步化。

**适用场景**：调用外部 API 的串联流程、耗时处理任务。

### 2.7 守边界的 Prompt 设计

**操作要领**：检索增强（RAG）注入 system prompt 时，要加一句明确的边界约束——"<span style="color: red; font-weight: bold;">如果资料中没有相关信息，直接说没有找到，不要编造</span>"。

**思考关注点**：

- 没有这个约束，LLM 在检索结果不充分时会用训练知识补充，而且不会告诉用户它在补。
- 加了这句约束，LLM 知道自己的边界。<span style="color: red; font-weight: bold;">RAG 不只让 LLM 能引用文档，也让它知道自己不知道什么。</span>
- 注入方式要保留原始 Prompt，参考资料附在后面（拼接而非替换），语义层次分明。

**适用场景**：所有基于检索增强的 LLM 应用。

### 2.8 项目阶段 Check List

下面这张可裁剪的 Check List，把上述方法论落到"在跑通的系统里加新能力"的各个阶段，供项目实战时快速勾选。

```text
【需求分析阶段】
□ 新能力能否与主链路解耦？能 → 独立模块先做完
□ 找到最小侵入点：改哪个方法、哪一行之前插入
□ 确定新能力在已有链路中的插入位置（第几步）

【指令编写阶段】
□ 显式写出"只改什么"（改动范围）
□ 显式写出"不要改什么"（扩散边界）
□ 多步骤串联任务：先对齐每环节输入输出格式
□ 耗时操作：确认异步执行、独立线程池

【实现阶段】
□ 新能力独立做完并单独验收，不混入主链路改动
□ 多步骤管线：每个环节独立方法，管线只串联+状态管理
□ 异常隔离：单条失败不影响其他条目、不影响线程池
□ 零侵入开关：新能力默认关闭，开关在最小粒度（如 Agent 维度）

【验收阶段】
□ 圈定改动范围（确认实际改动与指令一致）
□ 先跑旧用例：旧功能没坏，才往下走
□ 再跑新用例：新能力生效
□ 边界场景：检索结果不足时 LLM 不编造
□ 未启用新能力的路径：行为与之前完全一致
```

## 3. 实战背景：Hify 接入 RAG 知识库

<img src="imgs/aicent-21-fea-rag-2/8af89dff49e30ea7ac1b450c3f8a328e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

从本章起进入实战演示部分，结合 Hify 项目复现完整过程。

Hify 要支持 RAG 知识库：管理员上传文档，系统自动分块、向量化存入 pgvector；对话时检索相关内容，注入上下文。本篇要完成两件事：

1. **数据管线**：组织、生成知识库的全流程。这是新模块，需要从零开始建，不碰已有代码，相对独立。
2. **检索接入对话引擎**：把 RAG 检索接入已经跑通的 sendMessage 链路。这是本篇真正难的地方——17 篇实现的 sendMessage 已经可以运行、用户在用，要在这条链路里插入新环节，同时保证原有功能一分不能少。

这不只是 RAG 的问题。以后给任何一个跑通的系统加新能力，都会遇到同样的挑战：怎么加新的，不破坏旧的。

> 提示：在老项目中使用 AI 的能力（用 AI 对老项目进行改造和开发），核心原则是——**从小到大，从快到慢**。AI 理解和拆解项目也需要过程，不要一开始就甩一个大目标。

## 4. 第一步：圈定改动范围

动手之前，先把改动范围圈清楚。这是方法论 2.1 的实战落地。

### 4.1 sendMessage 的九步链路

<img src="imgs/aicent-21-fea-rag-2/46dcba095241b2dcb6445352ceec14dd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

17 篇的 sendMessage 链路（实现时是六步，后来细化成了九步），完整梳理如下：

```text
1. 加载 Session → 拿到 agentId
2. 加载 Agent → 拿到 systemPrompt、modelConfigId、temperature、maxContextTurns
3. 加载 ModelConfig → 拿到 modelId、providerId
4. 加载 Provider → 拿到 baseUrl、authConfig
5. 写入用户消息到 MySQL
6. 从 Redis 加载上下文历史
7. 拼 messages 数组：[system] + 历史 + 当前消息
8. 调 LLM streamChat
9. 写入 assistant 消息，更新 Redis
```

### 4.2 RAG 插在第 6.5 步

RAG 检索应该插在哪一步？答案是第 6 步之后、第 7 步之前，标记为第 6.5 步：

```text
6.5 ★ RAG 检索：用户问题 → Embedding → pgvector Top-K → 相关 chunk
```

为什么是这个位置？逻辑很清晰：

- **不能更早**：第 1–4 步还在加载配置，根本不知道用户问了什么；第 5 步之后用户消息才确定，才能拿它去检索。
- **不能更晚**：第 7 步是拼装 messages，检索结果要注入 system prompt，必须在第 7 步之前拿到。

### 4.3 插入后的 messages 结构

<img src="imgs/aicent-21-fea-rag-2/cd237e7094c585d4c17c16b35ff30f7a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

插入之后，messages 结构从：

```text
[system: Agent 原始 Prompt]

[user: 上一轮]

[assistant: 上一轮]

[user: 当前消息]
```

变成：

```text
[system: Agent 原始 Prompt + 参考资料]

[user: 上一轮] ← 历史消息不变

[assistant: 上一轮] ← 历史消息不变

[user: 当前消息] ← 当前消息不变
```

只有 system 变了，其余不动。检索结果注入 system prompt，对历史消息和当前消息完全无侵入。这是最小侵入的接入方式——改动面小，风险低，旧功能最不容易被破坏。

<img src="imgs/aicent-21-fea-rag-2/c444745823bd23734999a5801864d1d0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicent-21-fea-rag-2.md/c444745823bd23734999a5801864d1d0_MD5.jpg
用途：可视化展示 sendMessage 对话引擎的完整九步流程，并用高亮明确标出本次 RAG 改造的最小侵入点，是「圈定改动范围」这一工程原则的具体体现
内容：垂直线性流程图，自上而下共 9 个步骤：1.Load session → 2.Load agent config → 3.Load model config → 4.Load provider → 5.Save user message → 6.Load context history → 6.5.RAG retrieval（紫色高亮，标注 Embed query → pgvector top-K → chunks）→ 7.Build messages array（浅绿色高亮，标注 [system+RAG]+history+current）→ 8.Stream LLM call → 9.Save assistant message。右侧标注「Only method changed: buildMessages()」，强调除 buildMessages 方法外其余八步完全不碰，直观呈现改动范围被严格圈定在 RAG 检索与消息拼装两处。
-->

### 4.4 圈定改动范围

最终圈定的改动范围：**<span style="color: red; font-weight: bold;">只改 ChatService 的 buildMessages 方法，其他八步不动。</span>** 流式调用、SseEmitter 转发、消息存储、Redis 上下文管理，全部不碰。把这个约束写进给 Claude Code 的指令，是防止改动扩散的第一道防线。

## 5. 第二步：设计数据模型

<img src="imgs/aicent-21-fea-rag-2/951175c287523cd0dfbb315b44c09a0e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

在实现数据管线之前，先把数据模型定好。数据模型是后续所有改动的蓝图。

Hify 要支持 RAG 知识库，管理员上传文档，系统自动分块、向量化存入 pgvector，对话时检索相关内容注入上下文。基于这个需求设计数据模型时，有一个细节容易被忽略：这是两个数据库，不是一个。

### 5.1 三张表，两个数据库

| 数据库 | 表名 | 用途 |
| --- | --- | --- |
| MySQL | knowledge_base | 知识库容器 |
| MySQL | document | 文档元信息 + 处理状态 |
| PostgreSQL（pgvector） | document_chunk | 分块文本 + embedding 向量 |

为什么分两个数据库？

- **knowledge_base 和 document**<span style="color: red; font-weight: bold;">是业务数据，有完整的增删改查，走 MyBatis-Plus，放 MySQL。</span>
- **document_chunk**<span style="color: red; font-weight: bold;">是向量数据，只有批量写入和相似度查询两种操作，需要 pgvector 的向量检索能力，放 PostgreSQL。</span>用 JdbcTemplate 直接写 SQL 比配 MyBatis Mapper 更简单。

两个数据库的分工：MySQL 管"文档是什么、处理状态怎样"，pgvector 管"向量存在哪、怎么检索"。Spring 里要配双数据源：

```yaml
spring:
  datasource:
    mysql:
      url: jdbc:mysql://localhost:3306/hify
    pgvector:
      url: jdbc:postgresql://localhost:5432/hify
```

### 5.2 DDL

#### (1) MySQL：knowledge_base

知识库容器，轻量，就是一个分组——名称、描述、状态。真正的内容在 document 和 chunk 里。

```sql
CREATE TABLE knowledge_base (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500) DEFAULT '',
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL
);
```

#### (2) MySQL：document

关联 knowledge_base_id，记录文件元信息和处理状态。

```sql
CREATE TABLE document (
  id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  knowledge_base_id BIGINT NOT NULL,
  name VARCHAR(200) NOT NULL,
  file_type VARCHAR(20) NOT NULL,
  file_size BIGINT NOT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  error_message VARCHAR(500) DEFAULT '',
  chunk_count INT NOT NULL DEFAULT 0,
  deleted TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,
  KEY idx_document_kb_id (knowledge_base_id)
);
```

重点是 status 字段——`PENDING → PROCESSING → DONE / FAILED` 四个状态。文档处理是异步的，前端轮询这个字段显示进度。error_message 记录失败原因，chunk_count 记录分块数量。

#### (3) PostgreSQL：document_chunk

向量数据，存分块文本和 embedding。

```sql
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE document_chunk (
  id BIGSERIAL PRIMARY KEY,
  knowledge_base_id BIGINT NOT NULL,
  document_id BIGINT NOT NULL,
  chunk_index INT NOT NULL,
  content TEXT NOT NULL,
  embedding vector(1536) NOT NULL,
  token_count INT NOT NULL DEFAULT 0,
  deleted SMALLINT NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chunk_kb ON document_chunk (knowledge_base_id) WHERE deleted = 0;
```

knowledge_base_id 是冗余字段，检索时直接按知识库过滤，不用 JOIN document 表。embedding 是 1536 维向量，对应 OpenAI text-embedding-3-small 模型的输出维度。

#### (4) agent 表加一列

```sql
ALTER TABLE agent
ADD COLUMN knowledge_base_id BIGINT DEFAULT NULL;
```

NULL 表示不启用 RAG，有值表示启用。对话链路里判断这个字段决定要不要走检索。加一列，对已有功能零侵入。

### 5.3 关系图

<img src="imgs/aicent-21-fea-rag-2/aa944fa0bdaf7a81e990b3326923f491_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicent-21-fea-rag-2.md/aa944fa0bdaf7a81e990b3326923f491_MD5.jpg
用途：展示 RAG 知识库改造涉及的三张核心表（agent、knowledge_base、document_chunk）及它们之间的关联关系，是后续所有改动的数据模型蓝图
内容：数据库表关系图。agent 表通过新增的 knowledge_base_id 列关联到 knowledge_base 表（控制 Agent 是否启用 RAG 检索，NULL 表示不启用）；knowledge_base 表是文档的父表，存储名称、描述、启用状态；document_chunk 表属于 PostgreSQL(pgvector)，通过 knowledge_base_id 冗余字段直接关联知识库（无需 JOIN document 表），存储 content 文本和 1536 维 embedding 向量。图中清晰标注了 MySQL 与 pgvector 的跨库边界，以及冗余字段的设计意图。
-->

### 5.4 mock profile 处理

H2 不支持 vector 类型。schema-h2.sql 里把 document_chunk 的 embedding 列改成 TEXT，相似度查询在 mock profile 下返回空列表，不影响其他功能的验证。

## 6. 第三步：实现数据管线

数据管线是独立的新能力，先单独做完并验收，不混在对话引擎的改动里（方法论 2.3）。这样如果出了问题，能精确定位是哪一层。

### 6.1 数据管线拆解

<img src="imgs/aicent-21-fea-rag-2/449ecf070335f5c4c430c2405b6268e9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

动手之前，先让 Claude Code 把数据管线拆清楚。围绕三条提问展开：

```text
1. 这条数据管线由哪几个环节组成？每个环节做什么？
2. 管线之外，还需要哪些配套功能才能让这个特性完整可用？
3. 哪些部分需要前端页面？
```

拆解之后，数据管线这个特性分三块：

| 块 | 内容 | 说明 |
| --- | --- | --- |
| 第一块 | 知识库与文档的 CRUD | 骨架：创建知识库、上传文档、查看状态、查看分块、删除。没有这些接口，管线没有输入，前端没有操作入口 |
| 第二块 | 管线处理逻辑 | 文档上传后异步触发五步处理：状态更新 → 解析文本 → 分块 → 向量化 → 存入 pgvector。这是核心能力 |
| 第三块 | 前端页面 | 管理员需要界面来操作。不做前端只能 curl 调接口，不是一个完整的功能 |

开发顺序：`1 → 2 → 3 → 4 验收`。先搭后端骨架，再填管线处理逻辑，再做前端，最后前后端一起验收。每一步都可以独立测试，出了问题能精确定位是哪一层。

### 6.2 知识库与文档 CRUD——后端

先把增删改查的骨架搭好，管线处理逻辑后面再加。

#### (1) 知识库 CRUD 指令

```text
Hify 的知识库管理模块，后端部分。参考数据模型章节的 knowledge_base 表。

实现以下接口：

POST   /api/v1/knowledge-bases          — 创建知识库
       参数：name（必填）、description（可选）

GET    /api/v1/knowledge-bases          — 分页查询知识库列表
       参数：page、size、name（模糊搜索）

GET    /api/v1/knowledge-bases/{id}      — 查询单个知识库详情

PUT    /api/v1/knowledge-bases/{id}      — 更新知识库
       参数：name、description、enabled

DELETE /api/v1/knowledge-bases/{id}      — 逻辑删除知识库

GET    /api/v1/knowledge-bases/{kbId}/documents
       分页查询知识库下的文档列表

约束：

- 代码放在 hify-knowledge 模块
- 分层结构：Controller → Service → Mapper，遵循 CLAUDE.md 的代码组织规范
- Controller 只做参数校验和响应包装，业务逻辑在 Service
- 删除知识库时，关联的 document 和 document_chunk 一起逻辑删除
- 返回格式统一用 Result<T> 包装
```

#### (2) 文档管理 CRUD 指令

```text
Hify 的文档管理模块，后端部分。参考数据模型章节的 document 表和 document_chunk 表。

实现以下接口：

POST   /api/v1/knowledge-bases/{kbId}/documents
       上传文档
       接收 multipart/form-data
       校验文件类型（只接受 txt/md/pdf）和大小（不超过 10MB）
       文件落盘到 upload 目录，MySQL 写入 document 记录（status=PENDING）
       立即返回 documentId，提交异步任务到线程池

GET    /api/v1/knowledge-bases/{kbId}/documents
       分页查询知识库下的文档列表

GET    /api/v1/documents/{id}
       查询单个文档详情
       参数：status、chunk_count、error_message

GET    /api/v1/documents/{id}/chunks
       查询文档的分块列表
       调 pgvector 的 JdbcTemplate

DELETE /api/v1/documents/{id}
       逻辑删除文档
       同时删除 pgvector 里的 chunk

约束：

- 上传接口必须异步。文档处理要几秒到几十秒，同步等待会超时，也会占住 Tomcat 线程。
  上传接口只负责：接收文件、创建记录、提交异步任务。处理逻辑全在异步线程里跑。
- asyncExecutor 用独立线程池，核心线程 2，最大线程 4，队列 100
- document 的 status 字段驱动前端轮询：PENDING → PROCESSING → DONE / FAILED
- 查询 chunks 走 pgvector 数据源的 JdbcTemplate，不走 MyBatis
- 删除文档时，pgvector 里的 chunk 也要逻辑删除（UPDATE deleted=1）
```

### 6.3 管线处理逻辑——后端

上传接口提交异步任务后，管线开始工作。五个环节串联处理。

#### (1) 先对齐每个环节的输入输出

先让 Claude Code 把每个环节的输入输出格式梳理清楚（方法论 2.6）：

```text
知识库文档处理管线，每个环节的输入是什么、输出是什么？
输出的格式要能直接喂给下一个环节。

为什么要专门问这个？
多步骤串联任务最容易出的错，就是环节之间数据格式对不上。
上一步输出了 A，下一步期望收到 B，接口一对就出问题。
把每个环节的输入输出先对齐，再写代码，省去很多调试时间。
```

#### (2) 管线实现指令

```text
Hify 文档处理管线，在异步线程池中执行。接续上传接口提交的异步任务。

管线有五个环节，每个环节拆成独立的 private 方法，管线方法只负责串联和状态管理：

1. 状态更新
   document.status = PROCESSING

2. 解析 — extractText(filePath, fileType) → String
   TXT/MD：直接读文件内容，UTF-8
   PDF：用 Apache PDFBox 提取文字层。扫描版 PDF（提取文字为空）一期不支持，返回错误
   解析失败（加密 PDF、损坏文件）→ status=FAILED，写 error_message，后续环节不执行

3. 分块 — splitChunks(text) → List<ChunkDTO>
   递归分割：chunk_size=512 token，overlap=64 token
   切割优先级：段落边界（\n\n）> 句子边界（句号、问号）> 字符数截断
   每个 ChunkDTO 包含：chunkIndex、content、tokenCount

4. 向量化 — embedChunks(List<ChunkDTO>) → List<ChunkDTO>（补上 embedding 字段）
   调用 Embedding API，input 支持数组，一次请求处理多个块
   分批逻辑：每批最多 100 条，超过就分多批
   注意：API 返回的 data[] 数组按 index 字段排序后再和原始 chunk 列表对应
         不能假设返回顺序和输入顺序一致

5. 存储 — saveChunks(documentId, knowledgeBaseId, List<ChunkDTO>)
   JdbcTemplate.batchUpdate() 批量写入 pgvector 的 document_chunk 表
   写完后更新 document.status=DONE，chunk_count=N

异常处理：
- 任何一个环节失败，都要 catch 住，更新 document.status=FAILED，写清楚 error_message
- 不能因为一个文档处理失败影响线程池里其他文档的处理

约束：
- 不要把所有逻辑写在一个大方法里
- 管线方法只负责串联五步 + 状态管理，每个环节的具体逻辑在独立方法里
- Embedding API 的配置复用 Provider 模块的配置，不要硬编码 URL 和 API Key
```

#### (3) 管线代码组织原则

<span style="color: red; font-weight: bold;">每个环节独立，管线只负责串联。</span>这不只是为了代码整洁，更重要的是：某个环节出了问题时，能精确定位是哪一步，单独测试单独修复，而不是在一个几百行的大方法里翻来翻去。这个原则适用于任何多步骤数据处理管线（方法论 2.5）。

### 6.4 前端页面

<img src="imgs/aicent-21-fea-rag-2/beb5d1bd32aa0e81beb7455056dcf913_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

后端接口跑通后，做前端。分两个页面：知识库管理和文档管理。

#### (1) 知识库管理页指令

```text
Hify 前端，知识库管理页面。Vue 3 + Element Plus。

页面路径：/knowledge-bases

功能：
1. 列表页
   - 表格展示：名称、描述、状态（启用/禁用）、文档数量、创建时间
   - 顶部搜索框：按名称模糊搜索
   - 操作列：编辑、删除（二次确认）
   - 右上角"新建知识库"按钮

2. 新建/编辑弹窗
   - 表单字段：名称（必填）、描述（可选）
   - 编辑时回填已有数据
   - 提交后刷新列表

3. 点击知识库名称，跳转到文档管理页：/knowledge-bases/{id}/documents

调用后端接口：
- GET    /api/v1/knowledge-bases           列表
- POST   /api/v1/knowledge-bases           新建
- PUT    /api/v1/knowledge-bases/{id}      编辑
- DELETE /api/v1/knowledge-bases/{id}      删除

约束：
- 遵循 CLAUDE.md 的前端代码规范
- 表格用 el-table，弹窗用 el-dialog，表单校验用 el-form 的 rules
- 空状态给提示文案，不要空白页面
```

#### (2) 文档管理页指令

```text
Hify 前端，文档管理页面。Vue 3 + Element Plus。

页面路径：/knowledge-bases/{kbId}/documents

功能：
1. 页面顶部显示当前知识库名称，有返回按钮回到知识库列表

2. 文档列表
   - 表格展示：文件名、文件类型、文件大小、分块数量、处理状态、创建时间
   - 状态列用不同颜色标签：
     PENDING（灰色）、PROCESSING（蓝色，带 loading 动画）、DONE（绿色）、FAILED（红色）
   - FAILED 状态鼠标悬浮显示 error_message
   - 操作列：查看分块、删除（二次确认）

3. 上传功能
   - 右上角"上传文档"按钮，点击打开上传弹窗
   - 支持拖拽上传，限制文件类型 txt/md/pdf，限制大小 10MB
   - 上传后立即在列表中出现，状态为 PENDING
   - 自动轮询：每 3 秒调一次 GET /api/v1/documents/{id}
     状态变为 DONE 或 FAILED 时停止轮询，刷新列表

4. 查看分块弹窗
   - 点击"查看分块"打开弹窗
   - 列表展示每个 chunk 的序号和内容（content 字段，截断显示前 200 字，点击展开全文）
   - 不展示 embedding 向量（太长没有可读性）

调用后端接口：
- GET    /api/v1/knowledge-bases/{kbId}/documents   文档列表
- POST   /api/v1/knowledge-bases/{kbId}/documents   上传文档
- GET    /api/v1/documents/{id}                     轮询状态
- GET    /api/v1/documents/{id}/chunks              查看分块
- DELETE /api/v1/documents/{id}                     删除文档

约束：
- 轮询逻辑用 setInterval，组件销毁时 clearInterval，避免内存泄漏
- 上传组件用 el-upload，设置 accept 和 before-upload 校验
- PROCESSING 状态的行禁止删除操作（按钮置灰）
```

### 6.5 数据管线验收

前后端都完成后，走一遍完整流程验收。

#### (1) 后端验收

```bash
curl -X POST http://localhost:8080/api/v1/knowledge-bases \
-H "Content-Type: application/json" \
-d '{"name": "产品手册", "description": "公司产品手册和售后政策"}'

curl -X POST http://localhost:8080/api/v1/knowledge-bases/1/documents \
-F "file=@product_manual.txt"

curl http://localhost:8080/api/v1/documents/1

curl http://localhost:8080/api/v1/documents/1/chunks
```

查 pgvector 的 document_chunk 表，确认每条记录都有 1536 维的 embedding 向量。

#### (2) 前端验收

知识库管理：

<img src="imgs/aicent-21-fea-rag-2/b50dff8171d5e8f6207abc21e3c3f12f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicent-21-fea-rag-2.md/b50dff8171d5e8f6207abc21e3c3f12f_MD5.jpg
用途：展示知识库列表页的前端验收效果，验证知识库的增删改查管理功能正常运行
内容：Hify 系统知识库列表页截图。左侧导航栏含模型管理、Agent、知识库、对话四个模块，「知识库」高亮选中并展开「知识库管理」「文档管理」两个子项。主区域表格列出已有知识库记录，包含名称（如「产品手册」「客服手册」）、描述、文档数量、状态（启用/禁用）、创建时间、操作列。顶部有「创建知识库」按钮，证明列表查询、状态切换、入口跳转等管理类操作在前端可用。
-->

<img src="imgs/aicent-21-fea-rag-2/14174652e87a32192343676d6485300d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicent-21-fea-rag-2.md/14174652e87a32192343676d6485300d_MD5.jpg
用途：展示单个知识库详情页的前端验收效果，验证知识库元信息与文档概览的展示正常
内容：Hify 系统知识库详情页截图。面包屑显示「首页 / 知识库 / 知识库管理」并定位到某个具体知识库，页面顶部展示该知识库的基本信息（名称、描述、状态、创建时间等元数据），下方区域列出该知识库下已关联的文档及其分块数、处理状态。证明从列表进入详情、查看知识库构成、以及文档与知识库的归属关系在前端均渲染正确。
-->

上传文档，拆解为向量存储：

<img src="imgs/aicent-21-fea-rag-2/99786a427a38227406a31e0fc6cc6006_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicent-21-fea-rag-2.md/99786a427a38227406a31e0fc6cc6006_MD5.jpg
用途：展示文档管理页面验收效果，验证上传文档后异步拆解、向量化存储的完整管线在前端正常运行
内容：Hify 系统文档管理界面截图。面包屑显示「首页 / 知识库 / 文档管理」，当前知识库为「产品手册」，右上角有「返回」按钮。表格列出已上传文档，包含文件名、文件类型（txt/md/pdf）、文件大小、分块数量（chunk_count）、处理状态（PENDING/PROCESSING/DONE/FAILED 用不同颜色标签区分）、创建时间等列。可见文档已处理为 DONE 状态，chunk_count 显示分块数，证明上传→解析→分块→向量化→存储到 pgvector 的数据管线全流程跑通。
-->

## 7. 第四步：接入对话引擎

数据管线跑通了，现在把检索接入 sendMessage。这是本篇风险最高的一步（方法论 2.2、2.3 的实战）。

原因很简单：数据管线是新建的，坏了只影响新功能；而 sendMessage 是已有用户在用的链路，改坏了是线上故障。

这种情况下，遵循增量开发三步走：**<span style="color: red; font-weight: bold;">圈定改动范围 → 验证旧功能没坏 → 验证新功能生效</span>**。三步顺序不能乱，每一步都是对前一步的护栏。

### 7.1 写精确指令，把改动范围圈死

<img src="imgs/aicent-21-fea-rag-2/9a3f370c739f31ef2eb6a15c07ebaf9d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

先想清楚要改什么。sendMessage 九步链路里，RAG 插在第 6.5 步——用户消息向量化、检索相关 chunk、注入 system prompt。对应到代码，就是 ChatService 里的 buildMessages 方法。

改动范围确定了，指令就要把这个约束写进去（方法论 2.4）：

```text
修改 ChatService 的 buildMessages 方法，在 System Prompt 后插入 RAG 检索结果。

改动范围：只改 buildMessages 这一个方法。

具体逻辑：
- 检查 Agent 是否有 knowledgeBaseId；没有就跳过，直接返回原始 system prompt
- 有的话：把用户消息向量化，调 document_chunk 相似度查询，topK=3，过滤相似度低于 0.75 的结果

把检索到的 chunk 拼进 system prompt，格式如下：

{Agent 原始 Prompt}

请基于以下参考资料回答用户问题。
如果资料中没有相关信息，直接说"我没有找到相关资料"，不要编造。

【参考资料】
[1] {chunk1内容}
[2] {chunk2内容}

不要修改流式调用、SseEmitter 转发、消息存储的逻辑。不要改 Controller 层。
```

这条指令有几个细节值得说：

#### (1) 改动范围要显式写出来

Claude Code 默认会举一反三，它可能判断"既然改了 buildMessages，顺便把 Controller 的参数也调整一下更合理"。在新项目里这是优点，在有线上用户的系统里是风险。写"不要改 Controller 层"，是把扩散边界提前锁死。

#### (2) 原始 Prompt 保留，参考资料拼接在后面

Agent 原始 Prompt 要保留，参考资料附在后面。不是替换，是拼接。两者之间有空行，语义层次分明，LLM 读起来清楚哪部分是角色设定、哪部分是检索资料。

#### (3) 守边界的约束是关键

"没有相关信息就直接说"这句非常关键（方法论 2.7）。没有这个约束，LLM 在检索结果不充分时会用训练知识补充，还不告诉用户它在补。加了这句，LLM 知道自己的边界——RAG 不只让 LLM 能引用文档，也让它知道自己不知道什么。

#### (4) 没绑知识库的 Agent 零影响

没有绑知识库的 Agent，行为和之前完全一致。knowledgeBaseId 为空时，buildMessages 直接走原来的路径，零影响。开关在 Agent 维度，不影响全局。

### 7.2 改完先跑旧用例

<img src="imgs/aicent-21-fea-rag-2/7a2d3dd838b1f98c309399497d9ed196_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

代码改完，第一件事不是测新功能，是确认旧的还没坏：

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/1/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "Hify 支持哪些模型供应商？", "stream": true}'
```

用一个没绑知识库的 session，正常问一个问题。流式返回正常、内容正确，旧功能完好，再继续。

如果这里出了问题，先回滚再排查，不要在旧功能已经坏掉的状态下继续往下走。两件事同时出错，定位会乱。

### 7.3 跑新用例，看检索是否命中

旧功能确认没坏，再给 Agent 绑上知识库，测新功能：

```bash
curl -X PUT http://localhost:8080/api/v1/agents/1 \
-H "Content-Type: application/json" \
-d '{"knowledgeBaseId": 1}'

curl -N -X POST http://localhost:8080/api/v1/chat/sessions/2/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "你们支持七天无理由退货吗？已拆封的怎么办？", "stream": true}'
```

看后端日志，应该能看到 RAG 检索命中 2 条，相似度 0.94、0.81。回答里出现具体条款引用，说明检索链路通了。

三步走的本质是：每一步只验证一件事。第一步缩小改动面，第二步确认没有破坏，第三步确认新能力生效，顺序不能反。这套节奏不只适用于 RAG 接入，适用于在任何跑通的系统里加任何新能力。

## 8. 第五步：完整验收

数据管线和对话引擎都改完了，现在做整体验收。

验收不只是"跑一遍看看有没有报错"。<span style="color: red; font-weight: bold;">RAG 的验收要覆盖三个维度：检索链路是否通、回答质量是否变了、边界场景是否守住了。</span>三个维度都过，这个功能才算真正完成。

### 8.1 准备测试文档

<img src="imgs/aicent-21-fea-rag-2/a5ab69d03db3cec54b437870a33691f7_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

先准备一份内容确定的测试文档，上传到知识库。用真实业务内容，不要用"测试测试"这种占位文字——内容越接近真实场景，验收结论越可信。

```text
退换货政策：
自签收之日起七天内，未拆封商品支持无理由退货。
已拆封但存在质量问题的商品，三十天内可申请换货。
退货运费由买家承担，换货运费由公司承担。
生鲜食品、定制商品不支持退换货。

产品保修：
所有电子产品享受一年免费保修。
保修期内非人为损坏，提供免费维修或更换。

会员权益：
银卡会员：年消费满 2000 元自动升级，享受 9.5 折优惠。
金卡会员：年消费满 5000 元自动升级，享受 9 折优惠 + 专属客服。
```

文档上传后，等状态变为 DONE，确认 chunk_count 有值，再进行后续验收。管线没跑完就测对话，检索会命中空结果，容易误判。

### 8.2 验收一：对比绑知识库前后的回答

这是最核心的验收点。同一个问题，绑知识库前后的回答应该有明显差异。

用同一个 Agent，先不绑知识库，问：

```text
你们支持七天无理由退货吗？已拆封的怎么办？

预期回答类似：
根据一般的电商行业惯例，大多数平台支持七天无理由退货……

靠猜的，用的是训练知识，说的是一般情况。
```

再给这个 Agent 绑上知识库，问同样的问题：

```bash
curl -X PUT http://localhost:8080/api/v1/agents/1 \
-H "Content-Type: application/json" \
-d '{"knowledgeBaseId": 1}'
```

预期回答变成：

```text
根据退换货政策，自签收之日起七天内，未拆封商品支持无理由退货。
如果商品已拆封但存在质量问题，三十天内可申请换货。

从"靠猜的通用回答"变成"引用具体条款的准确回答"，
这个差异说明检索链路通了，内容注入生效了。

同时看后端日志，应该能看到：
RAG 检索命中 2 条，相似度 0.94、0.81
```

日志没有这一行，说明检索没有触发，要往 buildMessages 里查。

### 8.3 验收二：边界场景——文档里没有的问题

<img src="imgs/aicent-21-fea-rag-2/716dd4ca84cc6382a3ae6e68b8069e08_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

问一个测试文档里完全没有覆盖的问题：

```text
你们有没有学生优惠？

预期回答：
根据现有的产品手册，我没有找到关于学生优惠的相关信息。
如需了解，建议联系人工客服确认。
```

没有编造，诚实告知边界。这是 Prompt 里那句"如果资料中没有相关信息，直接说我没有找到相关资料"发挥的作用（方法论 2.7）。

如果这里 LLM 回答了一个听起来合理的学生优惠方案，说明约束没有生效，要回去检查 system prompt 的拼接逻辑。

### 8.4 验收三：没有绑知识库的 Agent 不受影响

换一个没有绑知识库的 Agent，正常对话，确认行为和之前完全一致。

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/3/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "你好，介绍一下你自己", "stream": true}'
```

流式返回正常，日志里没有任何 RAG 相关的输出。这一步确认的是：RAG 的开关在 Agent 维度，没有绑知识库的 Agent 完全不受影响，最小侵入的承诺兑现了。

<img src="imgs/aicent-21-fea-rag-2/b81d1c0a2c25154a5f703c7bcb05ab9c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicent-21-fea-rag-2.md/4a0f2e0136c5f5b69274206f6230ff91_MD5.jpg
用途：展示未绑定知识库的 Agent 进行普通对话的验收效果，验证 RAG 改动对已有功能零影响
内容：AI 客服助手系统的对话界面截图。用户提问"你好，介绍一下你自己"类的常规问题，AI 正常流式返回自我介绍，界面侧边栏显示模型管理、Agent、知识库、对话四个导航模块，对话列表中有一个"客服助手"会话。该截图证明未绑知识库的 Agent 行为完全不变，没有任何 RAG 检索相关的日志输出。
-->

三个验收维度都过，这个功能才算完整交付：链路通、质量变好、边界守住、旧功能没坏。

## 9. 总结与思考

### 9.1 两篇做了什么

<img src="imgs/aicent-21-fea-rag-2/883c9c087395b56e99a27db5bcba9c2b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本系列这两篇和前面的内容有点不一样：内容变多了，细节变少了，很多地方直接给提示词，没有一步步拆解。

原因很简单：RAG 本身是一个可以单独开一门课的话题。把它压进两篇，取舍是必然的。这里的选择是——把思考过程和提示词完整呈现，细节留给读者自己去琢磨。

如果深度读完这两篇，按照主体框架自己展开，让 Claude Code 配合把每个环节做透，完全可以做出一个生产可用的知识库系统，学到的东西会远超两篇的篇幅。

这两篇一直想教的不是某个技术点，是思考方式，值得多花时间。两篇做了一件完整的事：**在一个跑通的系统里，加入一个从未接触过的技术能力，从探路到集成到验收。**

### 9.2 方法论回顾

| 篇目 | 方法论 | 核心动作 |
| --- | --- | --- |
| 第 20 篇 | 探路 | 从业务痛点出发建立认知 → 拆解技术组件缩小陌生范围 → 约束驱动选型 → 最小 Demo 建立手感 |
| 第 21 篇 | 集成 | 找到最小侵入点 → 独立验收新能力 → 增量开发三步走 |

第 21 篇的集成方法论：数据管线单独做完再接入，接入后先跑旧用例再跑新用例。这套流程不只适用于 RAG，适用于在任何跑通的系统里加任何新能力。

### 9.3 代码组织原则

还有一个值得带走的代码组织原则（方法论 2.5）：每个环节独立方法，管线只负责串联。数据管线是这样，任何多步骤串联任务都适用。某个环节出了问题，能精确定位，单独修复，不用在一个几百行的大方法里翻来翻去。

### 9.4 思考

<img src="imgs/aicent-21-fea-rag-2/c060aaf43a5caef7cb044348c69035d3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 分块策略：固定切割 vs 按结构切割

当前用的是固定 512 token + 64 overlap 的递归分割。但如果文档本身有明确的章节结构，比如"退换货政策""产品保修""会员权益"这样的标题，按固定大小切可能会把一个完整章节切断，检索时拿到的是半截内容。那么按标题分块会不会更好？两种策略各有什么适用场景？让 Claude Code 帮忙实现一个基于标题识别的分块策略，和当前的递归分割对比检索效果。

#### (2) 动态参数：topK 和相似度阈值怎么调？

现在 topK=3、阈值 0.75 是写死的。问题来了：用户的问题涉及多个主题时，3 条不够；问题很简单时，3 条又太多，白占 token。

固定参数是一种妥协，不是最优解。让 Claude Code 帮忙设计一个根据场景动态调整这两个参数的方案——想清楚"场景"怎么判断，参数怎么映射。

#### (3) 表格内容的检索难题

当前 RAG 只处理纯文本。如果文档里有表格，比如会员等级对比表、银卡金卡的权益并排放，切块之后表格结构就碎了，检索效果会很差，LLM 拿到的是一堆乱序的单元格内容。

这是 RAG 的一个经典难题，没有银弹。让 Claude Code 分析有哪些处理方案，各自的代价和适用边界在哪里。
