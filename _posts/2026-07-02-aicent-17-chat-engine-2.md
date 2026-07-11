---
title: AI编程方法 17：核心功能 - 对话引擎（下）智能客服对话
author: fangkun119
date: 2026-07-02 10:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-17-chat-engine-2/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---

<!--
aicent-17-chat-engine-2
AI编程方法 17：核心功能 - 对话引擎（下）智能客服对话
-->
## 1. 本篇导读与上下文管理阶段方法论

### 1.1 阶段定位与产出物（结论先行）

<img src="imgs/aicent-17-chat-engine-2/b742a5bd7174a4095da947a9f3ab8102_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把系列第 16 篇定好的对话引擎设计决策全部落地——六步链路、数据模型、SSE 流式方案、适配层扩展、接口格式——让智能客服真正开口说话。本篇的产出物是「上下文管理 + 三层存储 + sendMessage CRUD + 多轮对话跑通」。

但在写 CRUD 之前，有一个核心问题要先解决：**上下文**。上下文管不好，多轮对话就是一句空话。

本篇是系列第 17 篇，落在「核心功能」主线的对话引擎子阶段下半场。读完本篇，读者应能回答四个问题：上下文到底是什么、对话历史存哪、sendMessage 这条最复杂的链路怎么落地、多轮对话怎么验收。前置依赖是系列第 13 篇（模型供应商适配）、第 15 篇（智能体配置）、第 16 篇（对话链路与流式选型）；下一篇（系列第 18 篇）做前端对话页面，把 curl 体验升级到浏览器。

### 1.2 全文导读地图（Mermaid）

下图把全篇骨架一次性摊开。第一部分只谈方法论动作，只字不提具体技术栈，可以照抄进任意项目的工程手册；第二部分把方法论逐条落到 Hify 的技术选型，回答 why。两部分用不同填色区分。

<img src="imgs/aicent-17-chat-engine-2/30bbd24671e1574c40c53e64d8fdf9c5_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">


<!--
flowchart LR
    Root(["对话引擎（下）<br/>上下文管理与智能客服对话"])

    Root --> A["第一部分 · 方法论"]
    Root --> B["第二部分 · 实战"]

    A --> A1["1、导读 + 阶段方法论"]
    A --> A2["2、本阶段工程 Check List"]

    A1 --> A1a["阶段目标 / 产出物"]
    A1 --> A1b["上下文与存储六步骨架"]

    A2 --> A2a["上下文本质认知"]
    A2 --> A2b["策略 / 存储 / 缓存"]
    A2 --> A2c["复杂方法落地 / 自验证"]

    B --> B3["2、背景：为什么先解决上下文"]
    B --> B4["3、上下文管理策略选型"]
    B --> B5["4、对话存储三层分工"]
    B --> B6["5、CRUD 与 sendMessage 落地"]
    B --> B7["6、验收：多轮对话"]
    B --> B8["7、收束与延伸思考"]

    B4 --> B4a["四种策略对比"]
    B4 --> B4b["为什么选滑动窗口"]
    B4 --> B4c["两个 token 实操问题"]

    B5 --> B5a["MySQL / Redis / pgvector 角色"]
    B5 --> B5b["Cache-Aside 协作"]
    B5 --> B5c["记忆 vs 检索"]

    B6 --> B6a["任务拆解"]
    B6 --> B6b["sendMessage 九步"]
    B6 --> B6c["让 AI 解释代码"]

    classDef method fill:#e8f0fe,stroke:#1a73e8,stroke-width:1.5px,color:#0b3d91
    classDef practice fill:#fef7e0,stroke:#f9ab00,stroke-width:1.5px,color:#7a5a00
    classDef root fill:#e6f4ea,stroke:#188038,stroke-width:2px,color:#0d652d

    class Root root
    class A,A1,A2,A1a,A1b,A2a,A2b,A2c method
    class B,B3,B4,B5,B6,B7,B8,B4a,B4b,B4c,B5a,B5b,B5c,B6a,B6b,B6c practice
-->


阅读建议：第一部分适合直接抄进团队 Wiki，作为对话引擎阶段的通用 Check List；第二部分按章顺序读，每章都会回头指认它对应第一部分哪条方法论动作，方便对照。

### 1.3 上下文与对话存储阶段方法论六步骨架

<img src="imgs/aicent-17-chat-engine-2/6e5d4fa9a1986f0f904e75b589519684_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

下面六步是本篇的纲领，也是「上下文管理 + 对话存储」阶段在任何技术栈里都成立的通用动作序列。每步用「方法论动作 + 可观测产物 + 必须挂住的事实」表述，动词开头，能落到具体产物上才算闭环。

#### (1) 上下文本质：先破除"LLM 有记忆"的误解

##### ① 动作

在动手写上下文管理之前，先把一个根上的认知掰正：<span style="color: red; font-weight: bold;">LLM 本身没有记忆，每次调用都是无状态的</span>。所谓"多轮对话的记忆"，是对话引擎通过把历史消息重新拼进每次请求实现的工程假象，不是模型自带的能力。

##### ② 可观测产物

一份能对团队讲清的"上下文 = 每次请求里 messages 数组的全部内容"的定义，外加一个反面提醒：去掉任何一条历史消息，后续语义就会断。

##### ③ 必须挂住的事实

上下文不是"对话历史"这一个名词就解释清楚的概念。它牵出三个绕不开的约束——窗口有限（context window，单位是 token）、超限即报错或截断、token 要花钱。这三条约束是后续所有策略选型的出发点。

#### (2) 上下文管理：在有限 token 预算内取舍历史

##### ① 动作

列出上下文管理的常见策略（滑动窗口、Token 预算裁剪、摘要压缩、向量召回），逐条评估优缺点与实现复杂度，再结合项目规模选定一种作为默认实现，其余作为后续优化路径保留。

##### ② 可观测产物

一份策略对比表，外加一份"为什么选它"的决策记录，记录里要写清项目规模、可配置参数、实现成本与延迟问题。

##### ③ 必须挂住的事实

<span style="color: red; font-weight: bold;">没有“最好的策略”，只有“最适合当前规模的策略”</span>。规模小（如内部 20-50 人）选最简单的；只有当"消息长度差异大导致体验差"这类真实问题出现，才升级到更复杂的策略。过度设计是这个阶段最常见的坑。

#### (3) 存储分层：真相来源 + 热缓存 + 知识库三件各司其职

##### ① 动作

把对话历史的存储需求拆成三类用途，分别交给三类存储：持久化真相来源（存全量历史，供查询与回溯）、热缓存（存最近 N 轮，供上下文组装）、知识检索库（语义检索文档片段，供 RAG）。三者职责不混淆。

##### ② 可观测产物

一张三存储职责对照表，每行写清：存什么、谁来读、读写性能、过期策略。特别标注"知识检索库不存对话历史"这一条，因为这是初学者最容易踩的认知混淆。

##### ③ 必须挂住的事实

<span style="color: red; font-weight: bold;">对话历史是结构化数据（一条条消息、有时间顺序），用前两类存储足够</span>；<span style="color: red; font-weight: bold;">向量库服务的是“语义检索”，是另一个维度的事</span>。<span style="color: red; font-weight: bold;">把两件事混在一起，会让架构在数据增长后难以拆分</span>。

#### (4) 缓存协作：写双写、读旁路、过期回填

##### ① 动作

为"真相来源 + 热缓存"这一对存储设计标准的协作模式：写时双写（消息落真相来源的同时推进热缓存），读时只读热缓存，热缓存过期后从真相来源重新加载回填。这是经典的 Cache-Aside 模式。

##### ② 可观测产物

一段缓存读写伪代码或流程图，标清双写时机、热缓存 TTL、冷启动回填逻辑。

##### ③ 必须挂住的事实

<span style="color: red; font-weight: bold;">热缓存只是加速，真相来源才是权威</span>。<span style="color: red; font-weight: bold;">任何“只在缓存里、真相来源没有”的数据都是隐患——缓存一过期，数据就丢了</span>。<span style="color: red; font-weight: bold;">双写必须在同一条消息的生命周期里完成</span>。

#### (5) 复杂方法落地：把指令写到每一步细节

##### ① 动作

对于串联整条链路的核心方法（如 sendMessage），给 AI 编程助手的指令必须细到每一步：每一步读写哪张表、在哪个线程、异常怎么处理、事务边界画在哪。任何一步省略都会埋下线上问题。

##### ② 可观测产物

一份分步指令清单，每步标清产物、依赖与注意事项（线程归属、事务边界、超时与中断处理）。

##### ③ 必须挂住的事实

<span style="color: red; font-weight: bold;">复杂方法的“复杂”不在某一行代码，而在多线程、多存储、多回调的交织</span>。指令写不到位，AI 生成的代码往往在边界条件下出问题——<span style="color: red; font-weight: bold;">而这些边界条件（超时、断连、调用失败）恰恰是线上必现的</span>。

#### (6) 自验证：让 AI 解释自己的代码

##### ① 动作

让 AI 生成代码后，反过来让它逐行解释执行流程（哪个线程做什么、读写时机、失败时已写入的数据怎么办），再人工核对其解释与代码是否一致。解释里出现的"代码中并不存在的逻辑"就是 bug 线索。

##### ② 可观测产物

一组自验证提示词，外加一份"代码事实 vs AI 自述"的对照判断，重点核对失败路径下已写入数据是否需要回滚。

##### ③ 必须挂住的事实

自验证不是"让 AI 再夸一遍自己的代码"，而是用解释逼出隐藏假设。<span style="color: red; font-weight: bold;">关键判断标准是：AI 说的逻辑，代码里到底有没有</span>。

### 1.4 本阶段工程 Check List（可裁剪速查表）

<img src="imgs/aicent-17-chat-engine-2/5fe0bd1d1fbaba786db53d7b9b3dd4cd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把本阶段容易遗漏的关键动作压成一张速查表，照着勾即可。表格只列动作与检查点，具体技术实现见第二部分。

| 阶段动作 | 关键检查点 | 勾 |
|----------|-----------|----|
| 上下文本质认知 | 团队是否清楚"LLM 无记忆、上下文 = messages 数组" | ☐ |
| 上下文本质认知 | 是否明确 context window 上限、超限后果、token 计费 | ☐ |
| 策略选型 | 是否列出 ≥3 种策略并记录优缺点 | ☐ |
| 策略选型 | 默认策略是否匹配当前项目规模（避免过度设计） | ☐ |
| 策略选型 | 是否预留"消息长度差异大"时的升级路径 | ☐ |
| 存储分层 | 全量历史是否有持久化真相来源 | ☐ |
| 存储分层 | 最近 N 轮是否有热缓存供上下文组装 | ☐ |
| 存储分层 | 是否把"知识检索库"与"对话历史"分开存放 | ☐ |
| 缓存协作 | 写消息时是否双写（真相来源 + 热缓存） | ☐ |
| 缓存协作 | 热缓存是否设置 TTL 与冷启动回填 | ☐ |
| 复杂方法落地 | 核心方法指令是否细到每一步的线程 / 存储 / 异常 | ☐ |
| 复杂方法落地 | 返回流式响应的方法上是否避开了大事务 | ☐ |
| 自验证 | 是否让 AI 解释执行流程并人工核对失败路径 | ☐ |
| 验收 | 是否验证流式回复 / 上下文带入 / 历史裁剪三项 | ☐ |

## 2. 背景与上下文本质（实战）

<img src="imgs/aicent-17-chat-engine-2/f6df1dfdaed9f5dbd18229090b09f905_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 从 16 篇的设计决策到落地

系列第 16 篇把对话引擎的所有设计决策都定好了——六步链路、数据模型、SSE 流式方案、适配层扩展、接口格式。本篇的工作就是把这些决策全部落地，让智能客服真正开口说话。

但落地之前要先解决一个根上的问题：**上下文**。

### 2.2 LLM 为什么"没有记忆"

"上下文"听起来是个显而易见的概念，不就是对话历史嘛。但它在技术实现上没那么简单——AI 对话里的"上下文"到底是什么？LLM 本身有记忆吗？多轮对话是怎么实现的？

一个常见误解需要先纠正：<span style="color: red; font-weight: bold;">LLM 本身没有记忆</span>。每次调用都是无状态的，上一轮告诉它"我的订单号是 12345"，下一轮它完全不知道。它不像数据库会持久化状态，每次调用都是一张白纸。

<img src="imgs/aicent-17-chat-engine-2/f6d493daf75d1d12d3ccb8604d707602_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">
<!--
图片内容说明
路径：imgs/aicent-17-chat-engine-2/f6d493daf75d1d12d3ccb8604d707602_MD5.jpg
用途：图示 LLM 无状态、每次调用都是白纸的本质
内容：示意图，说明每次 LLM 调用都是独立无状态的——上一轮告诉模型的信息下一轮它完全不知道，需要靠每次请求重新传入完整 messages 数组才能让模型"看到"历史，从而形成多轮对话的记忆假象
-->

那为什么用起来感觉模型"记得"说过什么？答案是每次调用都把历史消息重新塞进请求里，让模型在同一次推理中"看到"历史，造成记得的假象。

所以上下文的准确定义是：<span style="color: red; font-weight: bold;">你传给模型的 messages 数组的全部内容</span>。

```json
{
  "messages": [
    { "role": "system",    "content": "你是专业客服" },
    { "role": "user",      "content": "我的订单在哪里" },
    { "role": "assistant", "content": "请提供订单号" },
    { "role": "user",      "content": "订单号是 12345" },
    { "role": "assistant", "content": "您的订单正在配送中" },
    { "role": "user",      "content": "大概几天到" }
  ]
}
```

模型看到这整个数组，才能理解"几天到"指的是哪个订单。<span style="color: red; font-weight: bold;">去掉任何一条，语义就断了</span>。

这就带来了一个核心问题：**上下文窗口有限**。模型能处理的 messages 总长度有上限，叫 context window，单位是 token。GPT-4o 是 128K token（约 10 万汉字），Claude 3.5 Sonnet 是 200K，本地 Llama 3 可能只有 8K。超出窗口直接报错或截断。而且 token 是要花钱的，历史越长，每次调用越贵。

所以上下文管理的核心任务可以一句话概括：<span style="color: red; font-weight: bold;">在有限的 token 预算内，保留尽可能多的有用历史</span>。

## 3. 上下文管理策略选型（实战）

### 3.1 四种常见策略对比

<img src="imgs/aicent-17-chat-engine-2/6f5e260e1c71422aa37dcf2925aeb830_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

策略选型这一步，对应方法论第 (2) 步——在动手前先把策略空间摊开。给 AI 编程助手的提示词是：

```text
对话上下文管理有哪几种常见策略？各自的优缺点是什么？
```

<img src="imgs/aicent-17-chat-engine-2/8de6c0dd939246cdbd55f79e078a45dd_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">
<!--
图片内容说明
路径：imgs/aicent-17-chat-engine-2/8de6c0dd939246cdbd55f79e078a45dd_MD5.jpg
用途：展示对话上下文管理常见策略的对比分析
内容：对比几种上下文管理策略（滑动窗口、Token 预算裁剪、摘要压缩、向量检索召回等）的优缺点、适用场景与实现复杂度，为 Hify 最终选择滑动窗口策略提供决策依据
-->

常见策略大致四类：滑动窗口（只保留最近 N 轮）、Token 预算裁剪（按 token 数而非轮数裁剪）、摘要压缩（历史过长先让 LLM 总结再拼装）、向量召回（用相关性而非时序挑选历史）。这个决策过程主要来自 AI 编程助手的建议——这里其实可以展开，和 AI 深度交流每种方案的异同与原理，深入理解决策过程。

### 3.2 Hify 为什么选滑动窗口

<span style="color: red; font-weight: bold;">最终 Hify 选滑动窗口</span>。决策理由紧扣项目规模：

#### (1) 规模匹配

20-50 人内部使用，对话不会特别长，滑动窗口的"丢早期历史"在这一规模下不会造成体验问题。

#### (2) 可配置

`maxContextTurns` 已经在 Agent 表上配置好了，每个 Agent 可以独立调整窗口大小，不必硬编码。

#### (3) 实现成本极低

Redis List 天然支持 RPUSH 新消息、超出时 LPOP 旧的，实现十行以内就能写完。

#### (4) 保留升级路径

Token 预算裁剪留作后续优化路径，等真的出现"消息长度差异大导致体验差"的问题再做——<span style="color: red; font-weight: bold;">不过度设计</span>。

### 3.3 两个容易被忽略的 token 实操问题

<img src="imgs/aicent-17-chat-engine-2/de3c2f8d5bd8da3e0ee8b95040710c77_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

策略定了之后，还有两个实际问题需要提醒，它们都和 token 有关：

#### (1) System Prompt 占 token

每次请求都要带 system prompt，如果 prompt 很长（几千字），会持续占用 token 配额，变相压缩可用的对话历史。这正是系列第 15 篇强调要控制 prompt 长度的原因。

#### (2) Token 怎么算

粗略估算：1 个汉字 ≈ 1.5 token，1 个英文单词 ≈ 1 token。精确计算要用各家模型的 tokenizer。Hify 不自己算，而是直接用 LLM 返回的 `usage.completion_tokens` 记录，存入 `chat_message.tokens` 字段。

## 4. 对话存储三层分工（实战）

### 4.1 三种存储在对话引擎中的角色

<img src="imgs/aicent-17-chat-engine-2/8e57e55b45e48706e5eeff08372cec60_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

上下文策略定了，下一个问题是：对话历史存在哪？给 AI 编程助手的提示词是：

```text
对话历史应该存在哪？Redis、MySQL、向量数据库在对话引擎里分别扮演什么角色？
```

AI 的分析理清了三者的分工，对应方法论第 (3) 步——存储分层。

<img src="imgs/aicent-17-chat-engine-2/b39c6a9debcf6c01efa8b015386e3606_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">
<!--
图片内容说明
路径：imgs/aicent-17-chat-engine-2/b39c6a9debcf6c01efa8b015386e3606_MD5.jpg
用途：展示对话存储选型的分析结论
内容：对比表格或分析图，列出 Redis、MySQL、向量数据库（pgvector）在对话引擎中的不同定位——MySQL 作为持久化 source of truth 存全量历史、Redis 作为热缓存存最近 N 轮供上下文组装、pgvector 作为 RAG 知识库用于语义检索
-->

三者职责如下：

#### (1) MySQL：持久化真相来源

MySQL 是持久化存储，是数据的 source of truth。存全量对话记录——用户查看历史聊天、管理员做数据分析、Redis 缓存失效后的数据来源，都从 MySQL 查。它不用于组装 LLM 请求，因为每次都 SQL 查询 + 排序太慢。

#### (2) Redis：组装请求的工作内存

Redis 是工作内存，专门用于组装 LLM 请求。存最近 `maxContextTurns` 轮消息，ChatService 每次收到消息，直接从 Redis 取最近 N 轮拼入 messages 数组发给 LLM。读写 O(1) 毫秒级，TTL 2 小时到期自动清理。

```text
key: session:{sessionId}
val: [ {role,content}, {role,content}, ... ]   ← 最近 N 轮
TTL: 2h，每次对话刷新
```

### 4.2 Redis 与 MySQL 的 Cache-Aside 协作

<img src="imgs/aicent-17-chat-engine-2/eb44a014d26719f8cb3cff70805845ed_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

两者的关系是：MySQL 是真相来源（全量），Redis 是热缓存（最近 N 轮）。这一对存储采用经典的 Cache-Aside 模式——对应方法论第 (4) 步：

- **写时双写**：消息存 MySQL 的同时 RPUSH 到 Redis
- **读时只读 Redis**：组装上下文只读 Redis
- **过期回填**：Redis 过期后从 MySQL 重新加载

```typescript
List<ChatMessage> history = redis.get(sessionKey);
if (history == null) {
    // 冷启动：从 MySQL 加载最近 N 条，回写 Redis
    history = chatMessageMapper.selectRecent(sessionId, maxContextTurns * 2);
    redis.set(sessionKey, history, Duration.ofHours(2));
}
```

### 4.3 pgvector 不存对话历史：别混淆"记忆"与"检索"

那向量数据库（pgvector）呢？这里要特别说清楚——<span style="color: red; font-weight: bold;">pgvector 不是用来存对话历史的</span>。

很多初学者容易混淆"对话记忆"和"知识检索"：

- **对话历史**是结构化的（一条条消息，有时间顺序），用 MySQL + Redis 就够了。
- **pgvector 的角色是 RAG 知识库**：把产品文档切成小段，每段生成一个向量表示（1536 维 float 数组）存进 pgvector，用户提问时做语义检索找到最相关的文档段落，拼进上下文给 LLM 参考。这是系列第 20-21 篇的内容。

到时候智能客服就能回答具体的产品问题了——不是靠模型的通用知识瞎猜，而是基于产品文档给出准确回答。

三者协作的完整流程：

<img src="imgs/aicent-17-chat-engine-2/45db2bc5414a180b2c14440fa0ea1ff9_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">
<!--
图片内容说明
路径：imgs/aicent-17-chat-engine-2/45db2bc5414a180b2c14440fa0ea1ff9_MD5.jpg
用途：展示 Redis、MySQL、pgvector 三种存储在对话引擎中的协作关系
内容：一张数据流/架构图，呈现用户消息发送后，MySQL 持久化全量历史、Redis 缓存最近 N 轮用于拼装 LLM 请求、pgvector 作为 RAG 知识库供语义检索的分工与读写时序关系
-->

## 5. CRUD 实现与 sendMessage 落地（实战）

### 5.1 任务拆解

<img src="imgs/aicent-17-chat-engine-2/3bcefd1f875cb7bf7ad1302a01437c9c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

上下文和存储方案都定了，开始写代码——系列第 16 篇的设计决策全部落地。

按标准流程拆解，系列第 13 篇和第 15 篇已经教过方法论，这里直接执行：

<img src="imgs/aicent-17-chat-engine-2/d39f9a273174805607ad24b739ec5944_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">
<!--
图片内容说明
路径：imgs/aicent-17-chat-engine-2/d39f9a273174805607ad24b739ec5944_MD5.jpg
用途：展示对话引擎 CRUD 实现的任务拆解清单
内容：将 CRUD 实现拆分为若干任务项（Controller/Service/Mapper 接口与实现、Session 创建、ChatService.sendMessage 核心方法等）的清单或表格，说明每个任务的实现范围，重点标注任务 3 sendMessage 的核心地位
-->

### 5.2 sendMessage：项目最复杂的方法

重点是任务 3——`ChatService.sendMessage`，它串联了系列第 16 篇的整条六步链路。<span style="color: red; font-weight: bold;">这是整个项目最复杂的一个 Service 方法</span>：九个步骤，涉及多表查询、Redis 读写、Token 计算、线程切换、流式回调、异常处理。

给 AI 编程助手的指令必须写到这个细致程度，任何一步漏了都会出问题——对应方法论第 (5) 步。提示词如下：

```text
实现 ChatService.sendMessage 方法。接收 sessionId（可选）和 content。流程：异常处理：LLM 超时走 onTimeout 回调、客户端断开 catch IOException 停止 LLM 调用、send 失败调 completeWithError。事务注意：写消息操作拆成独立方法，不要在返回 SseEmitter 的方法上加 @Transactional。
```

### 5.3 自验证：让 AI 解释执行流程

<img src="imgs/aicent-17-chat-engine-2/e5ea5b6e8128a2b3f02b4b3229b3a9ed_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

AI 编程助手生成代码后，用系列第 16 篇学过的方法验证——对应方法论第 (6) 步。提示词如下：

```text
逐步解释 sendMessage 方法的执行流程。特别说明：哪些操作在 Tomcat 线程、哪些在 llmExecutor 线程、Redis 和 MySQL 的写入时机、如果第 7 步 LLM 调用失败第 5 步写入的用户消息怎么办。
```

让 AI 解释，人来听逻辑。<span style="color: red; font-weight: bold;">关键判断标准是：AI 说的逻辑，代码里到底有没有</span>。

比如 AI 如果说"LLM 失败会回滚用户消息"，但代码里没有回滚逻辑，就说明有问题。实际上用户消息不需要回滚——用户确实发了这条消息，只是 AI 没有成功回复，下次重试时用户消息已经在历史里了。这类"自述里多出来的逻辑"恰恰是最容易藏 bug 的地方。

## 6. 验收：和智能客服多轮对话（实战）

<img src="imgs/aicent-17-chat-engine-2/6e65d5570327ade75ae0d336054864c5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

所有代码写完了。这是最有仪式感的时刻——从系列第 15 篇创建 Agent 到现在，智能客服终于能说话了。验收分三轮：流式回复、上下文带入、历史裁剪。

### 6.1 第一轮：流式回复能否通

```bash
# 创建会话，发第一条消息
curl -N -X POST http://localhost:8080/api/v1/chat/sessions \
  -H "Content-Type: application/json" \
  -d '{"agentId": 1}'
# 返回 sessionId

# 和智能客服对话（流式）
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/{sessionId}/messages \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"content": "Hify 怎么创建 Agent？", "stream": true}'

# 应该看到流式输出：
# data: {"type":"delta","content":"在"}
# data: {"type":"delta","content":"Hify"}
# data: {"type":"delta","content":"中，您可以..."}
# data: {"type":"done","finishReason":"stop","latencyMs":3200}
```

第一轮通了，说明六步链路 + SSE 流式落地成功。

### 6.2 第二轮：上下文是否真的带进去了

第一轮通了还不够，上下文才是关键验证点。追问一句，故意不重复背景：

```bash
# 追问（不重复说明背景）
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/{sessionId}/messages \
  -H "Content-Type: application/json" \
  -H "Accept: text/event-stream" \
  -d '{"content": "那怎么配置模型？", "stream": true}'
```

判断方法看回答内容：

- 如果智能客服回答"您需要先进入模型管理页面…"，说明它**记住了上下文**，知道你在问 Hify 的操作——上下文拼装正确。
- 如果它回答"请问您想配置什么模型？能否提供更多背景？"，说明**上下文拼装有问题**，历史没有带进去。

### 6.3 第三轮：历史裁剪是否生效

再追问几轮，测试上下文裁剪：

```bash
# 再追问几轮，测试上下文裁剪
curl -N -X POST ... -d '{"content": "temperature 一般设多少？"}'
curl -N -X POST ... -d '{"content": "如果我想做一个代码审查的 Agent 呢？"}'
curl -N -X POST ... -d '{"content": "它的 Prompt 应该怎么写？"}'
```

检查后端日志，应该能看到上下文拼装的信息：历史消息 6 条，token 预算 3000，保留最近 3 轮。

检查数据，确认双写与缓存都正确：

```text
# MySQL：全量历史
SELECT role, LEFT(content, 50), tokens FROM chat_message
WHERE session_id = {sessionId} ORDER BY created_at;

# Redis：最近 N 轮
redis-cli LLEN session:{sessionId}
# 应该等于 maxContextTurns * 2（一问一答算两条）
```

全部通过。智能客服能流式回复，能记住上下文，历史太长会自动裁剪。

不过现在和它对话还得用 curl，下一篇（系列第 18 篇）做前端对话页面——消息气泡、打字机效果、自动滚动，让对话体验从命令行升级到浏览器。

## 7. 本篇收束与延伸思考

<img src="imgs/aicent-17-chat-engine-2/9fb3d0bae75cc2c742f068f80ee7b2a8_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 7.1 三件事回顾

本篇做了三件事：搞清楚上下文的本质和管理策略，理清对话存储的三层分工，实现完整的对话 CRUD 并跑通多轮对话。

#### (1) 上下文的本质

<span style="color: red; font-weight: bold;">LLM 没有记忆，每次调用都是白纸</span>。"记忆"是对话引擎通过拼装历史消息实现的。核心挑战是在有限 token 预算内保留尽可能多的有用历史。

#### (2) 存储三层分工

Redis 存最近 N 轮（上下文组装用，高频读写），MySQL 存全量历史（记录查询用），pgvector 不存对话历史（那是 RAG 知识库的事，后面讲）。

#### (3) 增量开发

sendMessage 是整个系列最复杂的方法——九步、多表、多缓存、多线程。给 AI 编程助手的指令要写到每一步的细节，然后让它解释自己的代码验证逻辑。

### 7.2 三个待深入的工程问题

#### (1) 关键消息置顶

当前的裁剪策略是"从最近往前保留"，但如果用户第一轮提供了关键背景信息（比如"我是 Hify 的管理员"），被裁掉后后面的回答就不准了。怎么实现"关键消息置顶"？

#### (2) 并发会话隔离

两个用户同时和同一个 Agent 对话，上下文会不会串？当前的 session 隔离是否足够？

#### (3) 摘要压缩如何落地

如果要实现"摘要压缩"——历史超过阈值时先让 LLM 总结再拼装——怎么改 sendMessage？改动范围有多大？

这三个问题没有在本篇给出答案，因为它们正是后续可以与 AI 深度交流、用作练手的真实工程课题。
