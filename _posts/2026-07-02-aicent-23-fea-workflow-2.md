---
title: AI编程方法 23：高级功能 - 工作流（下）执行引擎
author: fangkun119
date: 2026-07-02 15:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-23-fea-workflow-2/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---

<!--
aicent-23-fea-workflow-2
AI编程方法 23：高级功能 - 工作流（下）执行引擎
-->

<img src="imgs/aicent-23-fea-workflow-2/e5aea51e364f9a913813f77d8861be1d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第 22 篇把工作流的元数据存进了三张表，节点和连线都能完整保存。但存进去的只是一份静态配置，读得出来却"不会动"。本篇要做的事就是让它动起来——实现执行引擎。

全文按"方法论提炼 + 实战复现"两部分组织。第一部分是 AI 编程方法论速查手册，三句话能说清每条方法论的"问题信号 / 关键动作 / 预期产出"，不深入技术栈；第二部分把 Hify 智能客服工作流引擎从调研、选型、设计、实现到验收的完整过程复现一遍，紧扣 Java/Spring 技术栈。

## 全文导读地图

<img src="imgs/aicent-23-fea-workflow-2/fde52b770ba0c97d3b702888f59b9203_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
flowchart LR
    A[本篇：让工作流配置跑起来] --> B[第一部分 方法论提炼]
    A --> C[第二部分 实战复现]

    B --> B1[1\.1 技术调研先行]
    B --> B2[1\.2 复杂系统分步实现]
    B --> B3[1\.3 异常分支 review]
    B --> B4[1\.4 三大方法论速查表]
    B --> B5[1\.5 项目落地 Check List]

    C --> C1[2\. 实战背景与触发链路]
    C --> C2[3\. 第一步 技术调研与选型]
    C --> C3[4\. 第二步 代码结构设计]
    C --> C4[5\. 第三步 后端分块实现与接入]
    C --> C5[6\. 第四步 前端最简版与验收]
    C --> C6[7\. 延伸思考]

    B5 -.快速复习.-> C1
    B1 -.方法指导.-> C2
    B2 -.方法指导.-> C4
    B3 -.方法指导.-> C5
-->

| 阅读路径 | 适用读者 | 建议读法 |
| --- | --- | --- |
| 速查复习 | 已经做过工作流项目，只是回查方法论 | 只读第一部分（## 1） |
| 从零复现 | 第一次接触工作流引擎 | 按章节顺序读第二部分（## 2 到 ## 6） |
| 团队评审 | 技术负责人要指导他人或做架构评审 | 第一部分 + ## 6.4 异常分支 review |

## 1. AI 编程方法论提炼

本篇在原文的"总结"里浓缩了三条 AI 编程方法论。本节把它们抽象成"问题信号 / 关键动作 / 预期产出"三段式，作为可裁剪的速查手册。方法论本身与具体技术栈无关，可复用到任何复杂系统的 AI 协作开发中。

### 1.1 方法论一：遇到不熟悉的系统设计，先做技术调研再决策

<img src="imgs/aicent-23-fea-workflow-2/086e98dc8178e746aa052cee0181ba3d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 问题信号

出现下面任意一种情况，就说明该停下来做调研了：

##### ① 凭感觉拍方案

团队内部凭经验拍"用消息队列""用状态机"，但没有数据支撑。

##### ② 自己翻文档耗时过长

一个不熟悉的领域（比如工作流引擎），自己去翻 Dify、n8n、Coze、Temporal 的官方文档和源码，耗时半天起步。

##### ③ 不知道"有哪些选项"

只听说过一种方案，不知道业界还有什么做法，更不知道每种做法的代价。

#### (2) 关键动作

##### ① 把"调研需求"写成一段提示词

明确四件事：调研对象、关注维度、对比标杆、产出建议。以本篇为例：

```text
工作流执行引擎在业界有哪些主流的实现方案？
重点看 Dify、Coze、n8n 这类 AI 应用平台是怎么设计的。
我想了解：线程模型怎么选、节点执行怎么隔离、上下文数据怎么在节点间传递、
错误处理和执行记录怎么做。
最后给我一个建议：Hify 这种体量的项目应该选哪种方案，为什么。
```

##### ② 让 AI 做调研，而不是替你做决策

调研产出的是"选项清单 + 各自代价"，决策仍然由人做。调研的价值在于知道有哪些选项，然后做出有理由的选择。

##### ③ 调研结论必须落到"为什么不选"

光说"选 Dify 模式"不够，必须说清楚"为什么不选 n8n / Temporal"。排除理由往往比入选理由更能体现权衡。

#### (3) 预期产出

##### ① 一张多维度对比表

横向是候选方案，纵向是关注维度（线程模型、上下文传递、执行记录、适用场景等）。

##### ② 一份选型推理

针对自身项目约束（规模、运行时长、并发量、已有基础设施），逐条排除不适用的方案，给出最终选择和理由。

### 1.2 方法论二：复杂系统分步实现，每步单独验证

#### (1) 问题信号

##### ① 想一次性把整个系统写完

"工作流引擎一次写完"——这种冲动出现时就要警惕。

##### ② 验收只能整体跑

写完所有代码才能跑一次验收，中间任何一步出错都无法定位。

#### (2) 关键动作

##### ① 把系统拆成可独立验证的块

以 Hify 工作流引擎为例，拆成四块，每块单独验证：

| 块 | 内容 | 单独验证点 |
| --- | --- | --- |
| 第一块 | 线程池 + Agent 绑定 | agent 表能绑定 workflow_id |
| 第二块 | ExecutionContext | 单元测试 resolve 模板变量 |
| 第三块 | NodeExecutor 体系 | 四种节点能独立执行 |
| 第四块 | 核心循环 + 执行记录 | 端到端跑通 + 节点记录落库 |

##### ② 在块内部再分步

第四块 WorkflowEngine 内部又分三步：线性执行（验证 Context 传递）→ 加条件分支（验证路由）→ 加错误处理（生产级保护）。每步验证通过再往下走。

##### ③ 接入已有系统也是独立一步

把工作流接入 ChatServiceImpl 是单独一步，且必须先验证原有逻辑没坏，再验证新功能正常。

#### (3) 预期产出

每一步都能单独跑通、单独验收。出问题时能立刻定位到是哪一步引入的，而不是在几千行代码里大海捞针。

### 1.3 方法论三：有状态流转的代码，review 重点在异常分支

#### (1) 问题信号

代码涉及状态流转、节点跳转、循环、条件分支——这类代码的 review 重点和普通 CRUD 完全不同。

#### (2) 关键动作

##### ① 不在正常路径上花精力

正常路径 AI 基本能写对。review 精力要花在"如果这一步的输入不是预期的会怎样"。

##### ② 列出所有异常分支清单

以 Hify 工作流引擎为例，必查的四个边界：

| 异常分支 | 具体场景 | 期望行为 |
| --- | --- | --- |
| 条件全不匹配 | CONDITION 节点所有条件都不命中 | <span style="color: red; font-weight: bold;">明确走 defaultTarget 还是直接结束</span> |
| 目标节点不存在 | 边的 targetNodeKey 指向不存在的节点 | <span style="color: red; font-weight: bold;">抛异常，不空指针</span> |
| 工作流有环 | A→B→A 形成死循环 | <span style="color: red; font-weight: bold;">50 步上限兜底终止</span> |
| 节点调用失败 | LLM 节点调用失败 | <span style="color: red; font-weight: bold;">node_run 和 run 都 FAILED，SseEmitter 推错误提示</span> |

##### ③ 逐条问 AI，逐条验证

每个边界单独问 AI"这里怎么处理的"，看处理逻辑是否正确，发现问题立刻修复再往下走。

#### (3) 预期产出

一份异常分支处理清单，每条都经过验证，生产环境不会因为边界 case 崩溃。

### 1.4 三大方法论速查表

<img src="imgs/aicent-23-fea-workflow-2/ff992da28ee7a0b94a630a6575dad404_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

| 方法论 | 问题信号 | 关键动作 | 预期产出 |
| --- | --- | --- | --- |
| <span style="color: red; font-weight: bold;">技术调研先行</span> | 凭感觉拍方案 / 不知道有哪些选项 | 写清调研需求提示词；让 AI 调研不替你决策；落到"为什么不选" | 多维对比表 + 选型推理 |
| <span style="color: red; font-weight: bold;">分步实现</span> | 想一次写完 / 只能整体验收 | 拆成可独立验证的块；块内再分步；接入已有系统也是独立一步 | 每步都能单独跑通、单独验收 |
| <span style="color: red; font-weight: bold;">异常分支 review</span> | 涉及状态流转/跳转/循环/分支 | 正常路径不花精力；列全异常分支清单；逐条问逐条验证 | 异常分支处理清单，条条经过验证 |

### 1.5 项目落地 Check List

下面这份 Check List 可裁剪到具体项目里，按阶段勾选。

#### (1) 调研期

**需求是否写清**

- [ ] 调研对象明确（如"工作流执行引擎"）
- [ ] 关注维度明确（线程模型 / 上下文 / 错误处理 / 执行记录）
- [ ] 对比标杆明确（如"Dify / Coze / n8n / Temporal"）
- [ ] 产出要求明确（对比表 + 选型建议 + 排除理由）

**调研产出是否完整**

- [ ] 至少覆盖 3 个以上业界方案
- [ ] 每个方案都列了关键维度的实现方式
- [ ] 每个不选的方案都有明确的排除理由（结合自身项目约束）
- [ ] 最终选型有"为什么是它"的正面理由

#### (2) 实现期

**是否拆成了可独立验证的块**

- [ ] 每块有明确的输入输出
- [ ] 每块能单独跑通、单独验收
- [ ] 块内复杂逻辑再分步（如 WorkflowEngine 拆线性→条件→错误三步）

**是否复用了已有基础设施**

- [ ] 线程池是否复用（不新建）
- [ ] HTTP 客户端是否复用
- [ ] 模型适配层是否复用

**扩展点是否符合开闭原则**

- [ ] 新增类型是否只加文件不改老代码（注册表模式）
- [ ] 新增节点类型是否只加 record + @Component

#### (3) 验收期

**正常路径验收**

- [ ] 主流程端到端跑通
- [ ] curl 验收脚本全部通过
- [ ] 前端关键路径截图核对

**异常分支 review**

- [ ] 条件全不匹配的行为符合预期
- [ ] 目标节点不存在时不空指针
- [ ] 有环时 50 步上限生效
- [ ] 节点调用失败时状态正确传播、SseEmitter 正确关闭

**增量开发回归**

- [ ] 不绑工作流的 Agent 原有逻辑没坏
- [ ] 绑了工作流的 Agent 正确触发
- [ ] 执行记录每节点都落库

## 2. 实战背景：Hify 工作流引擎要解决什么问题

从本节开始进入第二部分实战复现。技术栈：Java + Spring Boot + MySQL + Redis + Vue 3 + Element Plus，项目代号 Hify（智能客服系统）。

### 2.1 工作流执行引擎是什么

<img src="imgs/aicent-23-fea-workflow-2/63568e09caa40ca5da9e44370900d7df_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 不懂先问：先把问题问清楚

进入实战前，先把三个问题问清楚：

| 问题分类 | 具体问题 |
| --- | --- |
| 概念问题 | 工作流执行引擎是什么概念？ |
| 关系问题 | 它和第 22 篇存的工作流配置是什么关系？ |
| 链路问题 | 怎么被触发？从用户发消息到工作流执行完毕返回结果，完整链路是什么样的？ |

#### (2) 让 AI 读现有代码，再回答

Claude Code 不会凭空解释，它先把现有代码读了一遍：`ChatServiceImpl.java`、`WorkflowServiceImpl.java`，然后基于真实代码说清楚关系。

<img src="imgs/aicent-23-fea-workflow-2/c5f88dff09ba7a09c18a5afa0b55b0ef_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/c5f88dff09ba7a09c18a5afa0b55b0ef_MD5.jpg
用途：说明 WorkflowServiceImpl（管配置的"食谱作者"）和 WorkflowEngine（管执行的"厨师"）的职责分离，中间通过数据库解耦。
内容：架构图，左侧灰色框 WorkflowServiceImpl（存取 workflow_node / workflow_edge / CRUD 接口，"只管存，不管跑"，比喻为"食谱作者"），右侧紫色框 WorkflowEngine（运行时加载节点和边、构建 nodeMap + edgeMap、循环执行节点、管理 ExecutionContext，"只管跑，不管存"，比喻为"厨师"），中间橙色框数据库（静态配置），箭头单向流动：WorkflowServiceImpl → 数据库 → WorkflowEngine。
-->

#### (3) Claude Code 的结论

`WorkflowServiceImpl` 是第 22 篇实现的工作流元数据存储，只管配置的 CRUD；执行引擎是另一个东西，是下一步要建的。

### 2.2 元数据与执行引擎的关系：食谱与厨师

#### (1) 食谱与厨师的比喻

元数据与引擎的关系可以用比喻说清楚：<span style="color: red; font-weight: bold;">食谱放在那里什么都不会发生，厨师拿到食谱才能把菜做出来</span>。工作流配置就是食谱，本篇要实现的执行引擎就是厨师。

<img src="imgs/aicent-23-fea-workflow-2/f4c5bf624a5a8d34e8a07fa3407202ec_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/f4c5bf624a5a8d34e8a07fa3407202ec_MD5.jpg
用途：展示从用户发消息到结果返回的完整触发链路，说明 ChatService 如何按 workflowId 是否存在分流到 WorkflowEngine 或直接调 LLM。
内容：流程图，顶部"用户发消息"→ Controller → ChatService（紫色，判断是否有 workflowId），分两个分支：有 workflowId → WorkflowEngine（绿色，workflowExecutor 线程池，逐节点执行 + while 循环 + Context）；无 workflowId → 直接调 LLM（绿色，原逻辑含 RAG）。两分支汇聚到 SseEmitter → 用户（橙色）。
-->

#### (2) 执行引擎的工作过程

执行引擎从数据库加载第 22 篇存的那份配置，构建 nodeMap + edgeMap，然后从起始节点开始一个节点一个节点地执行，最终结果通过 SseEmitter 推给用户。用户侧感知不到区别，收到的还是一样的 SSE 事件流。

#### (3) 项目代码现状

Claude Code 还顺手列出了项目库中代码的实现状态。

<img src="imgs/aicent-23-fea-workflow-2/578f6d5fbd6d2d84b15b91976ee9dcfd_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/578f6d5fbd6d2d84b15b91976ee9dcfd_MD5.jpg
用途：列出 Hify 项目中工作流相关代码文件的当前实现状态，明确哪些已存在、哪些待建。
内容：代码文件实现状态清单，按模块（hify-workflow / hify-chat 等）列出文件名（WorkflowServiceImpl、WorkflowNode、WorkflowEdge、NodeConfigDef、NodeConfigParser、ChatServiceImpl 等），每个文件标注状态（已实现 / 待实现 / 部分实现），区分出第 22 篇已完成的 CRUD 部分与本篇待建的执行引擎部分。
-->

### 2.3 完整触发链路：从用户消息到 SSE 返回

<img src="imgs/aicent-23-fea-workflow-2/73035b58dbf2b03f5b952169bc62295c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

把执行引擎放进完整链路看一遍：

<img src="imgs/aicent-23-fea-workflow-2/4853f325549052b9aed73efa5c1d60a5_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">


<!--
sequenceDiagram
    participant U as 用户
    participant C as Controller
    participant S as ChatServiceImpl
    participant E as WorkflowEngine
    participant DB as 数据库

    U->>C: 发消息
    C->>S: doStreamChat()
    S->>DB: 加载 Agent
    alt agent.workflowId != null
        S->>E: workflowEngine.execute(workflowId, userContent)
        E->>DB: 加载 nodes + edges
        E->>E: while 循环逐节点执行
        E->>DB: 写 workflow_run / workflow_node_run
        E--\>>S: 返回最终输出 String
        S->>DB: 存 assistant 消息
        S--\>>U: SSE done 事件
    else agent.workflowId == null
        S->>S: 走原逻辑（RAG 检索 + 直接调 LLM）
        S--\>>U: SSE 流式返回
    end
-->

关键设计点：执行引擎对外暴露的就是一个同步方法 `execute(workflowId, userMessage)`，返回 String。对 `ChatServiceImpl` 来说，<span style="color: red; font-weight: bold;">调工作流和调 LLM 的接口形态一致</span>——都是同步拿一个 String 回来，再走 SSE。这样原有流式逻辑、SseEmitter 转发、Redis 上下文管理<span style="color: red; font-weight: bold;">一行都不用改</span>。

## 3. 第一步：技术调研与方案选型

<img src="imgs/aicent-23-fea-workflow-2/6136e72d83e4a8155450701fefff3f22_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

知道执行引擎是什么了，但怎么实现？按第一部分的方法论一：先别急着写代码，让 Claude Code 做一轮技术调研。

### 3.1 业界四大方案对比：Dify / n8n / Coze / Temporal

#### (1) 调研提示词

调研需求按方法论一的要求写清楚：

```text
工作流执行引擎在业界有哪些主流的实现方案？
重点看 Dify、Coze、n8n 这类 AI 应用平台是怎么设计的。
我想了解：线程模型怎么选、节点执行怎么隔离、上下文数据怎么在节点间传递、
错误处理和执行记录怎么做。
最后给我一个建议：Hify 这种体量的项目应该选哪种方案，为什么。
```

#### (2) 四个维度的调研结论

##### ① 线程模型

- **Dify**：线程池 + 就绪队列，节点依赖满足后才入队
- **n8n**：多 Worker 进程 + Redis 消息队列，Worker 之间通过 Redis 协调
- **Coze**：Go goroutine，天生轻量并发
- **Temporal**：事件溯源状态机，崩了从检查点恢复

##### ② 上下文传递

Dify 的 VariablePool 模式最值得参考：节点之间不直接传对象，通过命名变量引用。每个节点把输出写进变量池，下一个节点从池里按引用读。变量池只增不覆盖，历史输出不会被改掉。

##### ③ 执行记录

Dify 持久化两层：`workflow_run`（整次执行）和 `workflow_node_execution`（每个节点的执行记录）。每次执行的每个节点都有完整记录，可以回放、调试、统计耗时。

#### (3) 四方案对比表

<img src="imgs/aicent-23-fea-workflow-2/ed6d76a556563aa09270b16f258b0e97_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/ed6d76a556563aa09270b16f258b0e97_MD5.jpg
用途：用一张表对照 Dify / n8n / Coze / Temporal 四大工作流引擎方案，作为选型决策的依据。
内容：四列（Dify / n8n / Coze / Temporal）× 多行（线程模型、节点隔离、上下文传递、错误处理与执行记录、适用场景）的对比表。Dify：线程池+就绪队列、代码沙箱 seccomp、VariablePool、workflow_run/workflow_node_execution、用户自定义代码节点；n8n：多 Worker + Redis 队列、进程级隔离、JSON 数据传递、执行日志、多 Worker 水平扩展；Coze：Go goroutine、轻量并发；Temporal：事件溯源状态机、事件日志可回放、长时间运行与崩溃恢复。
-->

### 3.2 Hify 选型结论：轻量同步引擎 + VariablePool 模式

Claude Code 的建议：<span style="color: red; font-weight: bold;">Hify 选轻量同步引擎，参考 Dify 的 VariablePool 模式，不引入消息队列</span>。

#### (1) 排除理由：为什么不选另外三个

##### ① n8n 的 Redis Queue：过度工程化

n8n 的 Redis Queue 是为多 Worker 水平扩展设计的。Hify 面向小规模团队，CLAUDE.md 里 Redis 只做缓存，<span style="color: red; font-weight: bold;">引入消息队列属于过度工程化</span>。

##### ② Temporal 的事件溯源：解决的不是 Hify 的问题

Temporal 解决的是"工作流中途崩溃后从断点恢复"，适合运行时间以小时 / 天计的工作流。Hify 的工作流是一次对话响应的一部分，最长几十秒，<span style="color: red; font-weight: bold;">崩了直接报错重试就够了</span>。

##### ③ Dify 的代码沙箱：Hify 没有任意代码执行

Dify 的代码沙箱（seccomp）是为隔离用户自定义代码节点。Hify 的节点类型是 LLM / CONDITION / API_CALL / KNOWLEDGE，<span style="color: red; font-weight: bold;">没有任意代码执行，不需要沙箱</span>。

#### (2) 入选理由：为什么参考 Dify

- **VariablePool 模式**：节点间通过命名变量解耦，可回放、可调试
- **两层执行记录**：每次执行 + 每个节点都有完整记录，便于排查
- **线程池模式**：和 Hify 已有的 `llmExecutor` 线程池基础设施天然契合

到这里对执行引擎的理解应该很完整了。这就是让 AI 做业界调研的好处——够快，信息够多，几秒内就能拿到一份有质量的行业对比。

## 4. 第二步：执行引擎代码结构设计

调研完了，让 Claude Code 把设计具体化。

### 4.1 四大组成部分与协作关系

<img src="imgs/aicent-23-fea-workflow-2/5265d8d94b9001d15631e90c730aee05_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 设计提示词

```text
基于上面的调研结论，帮我把 Hify 执行引擎的代码结构梳理清楚。
四个部分：线程池、ExecutionContext、NodeExecutor 体系、核心循环。
每个部分是什么，相互之间怎么协作，用代码示例说明。
先把现有的代码都读清楚，不基于假设设计。
```

#### (2) 先读现有代码

Claude Code 先把现有代码读完：`ThreadPoolConfig.java`、`NodeConfigDef.java`、`NodeConfigParser.java`、`WorkflowNode.java`、`LlmHttpClient.java` 等，然后基于真实代码给出设计。

<img src="imgs/aicent-23-fea-workflow-2/27f54034d007d7921d5d7986eab3a7a6_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/27f54034d007d7921d5d7986eab3a7a6_MD5.jpg
用途：展示 Hify 执行引擎四大组件（WorkflowEngine / 线程池 / ExecutionContext / NodeExecutor 体系）的类架构与协作关系。
内容：类架构图。顶部中央 WorkflowEngine（核心循环，while 驱动节点执行）；左侧线程池（复用项目最初的 llmExecutor，core=10 max=50，CallerRunsPolicy）；底部中央 ExecutionContext（变量池，key 格式 nodeKey.varName，只增不改）；右侧 NodeExecutor 体系（NodeExecutor 接口 + 四种实现 LlmNodeExecutor/ConditionNodeExecutor/ApiCallNodeExecutor/KnowledgeNodeExecutor + NodeExecutorRegistry 按 type 分发）。箭头表示依赖：WorkflowEngine 调度 NodeExecutor、读写 ExecutionContext；NodeExecutor 写 ExecutionContext。
-->

#### (3) 四大组成部分概览

| 部分 | 职责 | 设计要点 |
| --- | --- | --- |
| 线程池 | 承载工作流执行 | 复用 `llmExecutor`，不新建 |
| ExecutionContext | 贯穿工作流的变量池 | 对标 Dify VariablePool，只增不改 |
| NodeExecutor 体系 | 每种节点一个执行器 | 统一接口 + 注册表分发 |
| 核心循环 WorkflowEngine | 把前三件事串起来 | while 循环 + 执行记录 |

### 4.2 ExecutionContext：贯穿工作流的变量池

<img src="imgs/aicent-23-fea-workflow-2/687bd349f4a1ccdf666c7ec06e5e83d0_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 设计要点

ExecutionContext 对标 Dify 的 VariablePool。一次执行创建一个，从 START 节点活到 END 节点。

##### ① key 格式

<span style="color: red; font-weight: bold;">`nodeKey.varName`，比如 `classify.intent`</span>。

##### ② 读写规则

<span style="color: red; font-weight: bold;">节点只能写自己 nodeKey 下的变量，读可以读任意历史节点的输出</span>。

##### ③ 只增不改

<span style="color: red; font-weight: bold;">写入只增不改，历史输出不会被覆盖掉</span>。

#### (2) Java 骨架

```java
public class ExecutionContext {
private final Map<String, Object> variables = new LinkedHashMap<>();

public ExecutionContext(Long workflowRunId, String userMessage) {
variables.put("start.userMessage", userMessage);
}

public void set(String nodeKey, String varName, Object value) {
variables.put(nodeKey + "." + varName, value);
}

public String resolve(String template) {
String result = template;
for (Map.Entry<String, Object> entry: variables.entrySet()) {
result = result.replace("{{" + entry.getKey() + "}}",
entry.getValue()!= null? entry.getValue().toString(): "");
}
return result;
}
}
```

#### (3) Context 随节点执行逐步累积变量

<img src="imgs/aicent-23-fea-workflow-2/7d8230c68472601a474e808f18905d91_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/7d8230c68472601a474e808f18905d91_MD5.jpg
用途：用具体示例说明 ExecutionContext 变量池如何随节点执行逐步累积，帮助理解"只增不改"的写入语义。
内容：节点执行序列与变量池快照对照。START 执行后：{ start.userMessage }；classify 执行后：{ start.userMessage, classify.intent }（intent 取值如 售后/售前）；kb 执行后：{ start.userMessage, classify.intent, kb.docs }（检索到的知识库文档拼接串）；answer 执行后：{ start.userMessage, classify.intent, kb.docs, answer.output }（最终回答）。END 节点结束循环。
-->

### 4.3 NodeExecutor 体系：统一接口 + 注册表模式

<img src="imgs/aicent-23-fea-workflow-2/15c51806b169e88a20146d55292f6ec2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 与第 14 篇 Provider 适配层同模式

NodeExecutor 体系采用统一接口、每种节点一个实现，和第 14 篇的 Provider 适配层是同一个模式。四种 Executor 由 Spring 自动注册到 Registry，按 type 分发。

#### (2) 接口定义

```java
public interface NodeExecutor {
	void execute(WorkflowNode node, NodeConfigDef config, ExecutionContext ctx);
	String nodeType();
}
```

#### (3) 四种执行器职责

##### ① LlmNodeExecutor

解析 config 里的 promptTemplate，用 `ctx.resolve()` 替换模板变量，复用已有的 ProviderAdapter 调 LLM，把返回内容写入 ctx。

##### ② ConditionNodeExecutor

解析 expression，`ctx.resolve()` 替换变量后做字符串匹配，把 true/false 写入 ctx。

##### ③ ApiCallNodeExecutor

解析 url，替换变量后用 `LlmHttpClient` 调外部接口，把响应写入 ctx。

##### ④ KnowledgeNodeExecutor

调 `KnowledgeService.searchChunks()`，把检索结果拼成字符串写入 ctx，后续 LLM 节点用 `{{kb.docs}}` 引用。

<img src="imgs/aicent-23-fea-workflow-2/ed671563ed7abea27816ca3930ce291f_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/ed671563ed7abea27816ca3930ce291f_MD5.jpg
用途：说明 NodeExecutor 体系的开闭原则设计——统一接口 + 注册表分发，新增节点类型零改动老代码。
内容：类图。顶部 NodeExecutor 接口（方法：execute(WorkflowNode, NodeConfigDef, ExecutionContext)、String nodeType()）。下方四个实现类（均标注 @Component）：LlmNodeExecutor（nodeType=LLM）、ConditionNodeExecutor（nodeType=CONDITION）、ApiCallNodeExecutor（nodeType=API_CALL）、KnowledgeNodeExecutor（nodeType=KNOWLEDGE）。右侧 NodeExecutorRegistry（Spring 自动注入所有 NodeExecutor 实现，get(type) 按 nodeType() 分发，未知类型抛 BizException）。新增节点类型只需加一个 record + 一个 @Component 实现类。
-->

#### (4) 扩展点：新增节点类型零改动

新增节点类型只需要：在 `NodeConfigDef` 加一个 record，加一个 `@Component` 实现类，不改其他任何代码。

### 4.4 核心循环 WorkflowEngine：骨架就是几行 while

<img src="imgs/aicent-23-fea-workflow-2/db5caea5cfeff6b28af81ffae7134291_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 把前三件事串起来

WorkflowEngine 把线程池、ExecutionContext、NodeExecutor 体系串起来，再加上执行记录。<span style="color: red; font-weight: bold;">骨架就是一个 while 循环</span>：

```java
public String execute(Long workflowId, String userMessage) {
    Map<String, WorkflowNode> nodeMap = loadNodeMap(workflowId);
    Map<String, List<WorkflowEdge>> edgeMap = loadEdgeMap(workflowId);
    WorkflowRun run = createRun(workflowId, userMessage);
    ExecutionContext ctx = new ExecutionContext(run.getId(), userMessage);
    String currentKey = findStartKey(nodeMap);

    while (currentKey != null) {
        WorkflowNode node = nodeMap.get(currentKey);
        if ("END".equals(node.getType())) break;
        WorkflowNodeRun nodeRun = createNodeRun(run.getId(), node);

        try {
            NodeConfigDef config = configParser.parse(node.getType(), node.getConfig());
            executorRegistry.get(node.getType()).execute(node, config, ctx);
            finishNodeRun(nodeRun, "SUCCESS", ctx.snapshot(), null, elapsed);
        } catch (Exception e) {
            finishNodeRun(nodeRun, "FAILED", ctx.snapshot(), e.getMessage(), elapsed);
            throw e;
        }

        currentKey = pickNext(edgeMap, currentKey, node.getType(), ctx);
    }

    String output = resolveEndOutput(nodeMap, ctx, currentKey);
    finishRun(run, "SUCCESS", output, null);
    return output;
}
```

#### (2) 复杂在哪里

<span style="color: red; font-weight: bold;">这就是执行引擎的全部本质</span>。复杂的是每个 Executor 怎么实现、Context 怎么传、错误怎么处理，<span style="color: red; font-weight: bold;">骨架就是这几行</span>。把骨架看懂，细节就是各组件的具体实现。

## 5. 第三步：后端分块实现与接入

架构清楚了，开始实现。按方法论二：四块按顺序来，每块单独验证再往下走。

<img src="imgs/aicent-23-fea-workflow-2/86067731846c7a2d42b6b1fba5c48357_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/86067731846c7a2d42b6b1fba5c48357_MD5.jpg
用途：展示后端实现的四块拆分与"每块单独验证"的分步策略，是方法论二的具体落地。
内容：流程图，四个顺序块：① 线程池 + Agent 绑定（验证：agent 表能绑定 workflow_id）→ ② ExecutionContext（验证：单元测试 resolve 模板变量）→ ③ NodeExecutor 体系（验证：四种节点能独立执行）→ ④ 核心循环 + 执行记录（验证：端到端跑通 + 节点记录落库）。每块之间有验证检查点。
-->

本节技术细节较多，代码和提示词是最好的讲解。关键在于理解提示词为什么这么写、要达到什么目的。

### 5.1 线程池 + Agent 绑定

<img src="imgs/aicent-23-fea-workflow-2/1fccdda91e3c8303c8def9a67c103922_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 给 agent 表加 workflow_id 字段

```sql
ALTER TABLE agent ADD COLUMN workflow_id BIGINT DEFAULT NULL;
```

#### (2) 更新 Java 实体与服务

- 更新 `Agent.java` entity，加 `workflowId` 字段
- 更新 `AgentService` 的更新接口，支持绑定/解绑工作流

#### (3) 线程池：复用而非新建

不要新建线程池。工作流执行复用已有的 `llmExecutor`。`ChatServiceImpl` 在提交任务时就已经在线程池里了。

### 5.2 ExecutionContext 实现

实现 `ExecutionContext` 类，放在 `hify-workflow` 模块的 `engine` 包下。

#### (1) 实现要求

```yaml
- 内部用 LinkedHashMap<String, Object> 存变量，保持写入顺序
- 构造时传入 workflowRunId 和 userMessage
```

`userMessage` 预写入为 "start.userMessage"，所有节点默认能读到。

```yaml
- set(nodeKey, varName, value)：写入变量，key 格式 = nodeKey + "." + varName
- get(nodeKey, varName)：读取变量
- resolve(template)：替换模板变量
```

遍历所有变量，把 "{{nodeKey.varName}}" 替换为对应值。变量不存在时保留原始占位符，不报错。

```yaml
- snapshot()：返回所有变量的只读视图，用于执行记录落库
```

#### (2) 单元测试验证

写单元测试：`set("classify", "intent", "售后")`，然后 `resolve("你好，{{classify.intent}}客服为您服务")`，输出应该是"你好，售后客服为您服务"。

### 5.3 NodeExecutor 四种节点执行器

实现 `NodeExecutor` 接口和四种节点执行器，放在 `hify-workflow` 的 `engine/executor` 包下。

#### (1) 接口

```java
void execute(WorkflowNode node, NodeConfigDef config, ExecutionContext ctx)
String nodeType()
```

#### (2) LlmNodeExecutor（nodeType = "LLM"）

```yaml
- 解析 LlmNodeConfig（modelConfigId、prompt、outputVariable）
- ctx.resolve(prompt) 替换模板变量
- 复用 ModelConfigMapper、ProviderMapper、ProviderAdapterFactory 加载模型配置
- 同步调 LLM（非流式），把返回内容写入 ctx.set(nodeKey, outputVariable, content)
```

#### (3) ConditionNodeExecutor（nodeType = "CONDITION"）

```yaml
- 解析 ConditionNodeConfig（expression、outputVariable）
- ctx.resolve(expression) 替换变量后做字符串匹配
- 支持 ==、!=、字面量 true/false
- 结果写入 ctx.set(nodeKey, outputVariable, boolResult)
```

#### (4) ApiCallNodeExecutor（nodeType = "API_CALL"）

```yaml
- 解析 ApiCallConfig（url、method、headers、outputVariable）
- ctx.resolve() 替换 url 和 headers 里的模板变量
- 复用 LlmHttpClient 发起 HTTP 请求
- 响应体写入 ctx
```

#### (5) KnowledgeNodeExecutor（nodeType = "KNOWLEDGE"）

```yaml
- 解析 KnowledgeConfig（knowledgeBaseId、query、topK、outputVariable）
- ctx.resolve(query) 替换查询模板
- 调 KnowledgeService.searchChunks()
- 结果拼成字符串写入 ctx
```

#### (6) NodeExecutorRegistry

```yaml
- Spring 自动注入所有 NodeExecutor 实现类
- get(type) 按 nodeType() 分发，未知类型抛 BizException
```

所有 Executor 执行失败时 catch 住异常，让外层 WorkflowEngine 统一处理状态。

### 5.4 核心循环 + 执行记录建表

#### (1) 先建执行记录表

```sql
CREATE TABLE workflow_run (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workflow_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    input TEXT,
    output TEXT,
    error VARCHAR(500),
    elapsed_ms INT,
    created_at DATETIME NOT NULL,
    finished_at DATETIME
);

CREATE TABLE workflow_node_run (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    workflow_run_id BIGINT NOT NULL,
    node_key VARCHAR(64) NOT NULL,
    node_type VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    outputs JSON,
    error VARCHAR(500),
    elapsed_ms INT,
    created_at DATETIME NOT NULL,
    finished_at DATETIME,
    KEY idx_node_run_run_id (workflow_run_id)
);
```

#### (2) 实现WorkflowEngine

实现 `WorkflowEngine` 类，放在 `hify-workflow` 的 `engine` 包下。

### 5.5 WorkflowEngine 三步迭代：线性 → 条件分支 → 错误处理

<img src="imgs/aicent-23-fea-workflow-2/dfca7dddca88483d31aaa48f4fdd8eb9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

按方法论二：分三步实现，每步验证通过再往下。

#### (1) 第一步：线性执行（只支持无条件边）

```yaml
- execute(workflowId, userMessage) 方法
- 从数据库加载节点和边，构建 nodeMap + edgeMap
- 创建 WorkflowRun 记录，status=RUNNING
- 从 START 节点开始 while 循环
- 每个节点执行前创建 WorkflowNodeRun 记录
- 调 NodeExecutorRegistry.get(type).execute()
- 节点执行完更新 WorkflowNodeRun status=SUCCESS，写 outputs=ctx.snapshot()
- findNext：找 conditionExpr 为 null 的出边
- END 节点：结束循环，取 outputVariable 对应的 ctx 值作为最终输出
- 更新 WorkflowRun status=SUCCESS
```

#### (2) 第二步：加条件分支

```yaml
- 修改 findNext 方法：
  CONDITION 节点：从 ctx 取布尔结果，匹配 conditionExpr = "true"/"false" 的边
  其他节点：先找无条件边，没有就取第一条
- 改完先跑线性工作流验证没坏，再跑条件分支工作流
```

#### (3) 第三步：错误处理

```yaml
- 节点执行失败：更新 WorkflowNodeRun status=FAILED 写 error
  更新 WorkflowRun status=FAILED，终止循环，抛 BizException
- 执行记录落库失败：只打 log，不影响主流程
- 保护：nodeMap.get(currentKey) 返回 null 时抛异常（目标节点不存在）
- 保护：找不到 START 节点时抛异常
- 保护：执行步数超过 50 步时终止（防止配置错误导致死循环）
```

#### (4) 约束

```yaml
- 依赖 WorkflowNodeMapper、WorkflowEdgeMapper、NodeConfigParser、
  NodeExecutorRegistry、WorkflowRunMapper、WorkflowNodeRunMapper
- 不引入新的异步机制，WorkflowEngine 是同步执行的
```

### 5.6 接入对话引擎：第四次增量开发

<span style="color: red; font-weight: bold;">这是对话引擎的第四次增量开发</span>：第 16 篇基础链路、第 17 篇上下文、第 21 篇 RAG、本篇工作流。

#### (1) 修改点

修改 `ChatServiceImpl` 的 `doStreamChat()` 方法，支持工作流触发。在加载完 Agent 之后（现有代码第 144 行附近）插入判断：

```yaml
- agent.getWorkflowId()!= null → 调 workflowEngine.execute(workflowId, userContent)
  拿到返回的 String 结果，存入 MySQL 作为 assistant 消息，发 SSE done 事件，return

- workflowId 为 null → 走原有逻辑（RAG 检索 + 直接调 LLM），一行不改
```

#### (2) 约束

```yaml
- 不修改流式调用、SseEmitter 转发、Redis 上下文管理的逻辑
- 不改 Controller 层
- 工作流执行失败时，catch BizException，通过 SseEmitter 推错误提示给用户，
  不能让 SseEmitter 处于未完成状态
```

#### (3) 接入流程图

<img src="imgs/aicent-23-fea-workflow-2/d49349b2eec646c4fa7ca986c5cd6a3b_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/d49349b2eec646c4fa7ca986c5cd6a3b_MD5.jpg
用途：展示 ChatServiceImpl.doStreamChat() 接入工作流后的分流逻辑，明确原有逻辑零改动的设计目标。
内容：流程图。入口 doStreamChat() → 加载 Agent → 决策点 agent.getWorkflowId() != null ？分支 A（是）：调 workflowEngine.execute(workflowId, userContent) → 拿到 String 结果 → 存 MySQL assistant 消息 → 发 SSE done 事件 → return；分支 B（否）：走原逻辑（RAG 检索 + 直接调 LLM）→ SSE 流式返回。两条分支都最终通过 SseEmitter 推给用户。catch BizException 时通过 SseEmitter 推错误提示，保证 SseEmitter 不处于未完成状态。
-->

#### (4) 增量开发三步走

```yaml
- 先跑不绑工作流的 Agent，确认原有逻辑没坏
- 再跑绑了工作流的 Agent，确认工作流正确触发
- 查 workflow_node_run 表，确认每个节点的输入输出都落库了
```

## 6. 第四步：前端最简版与验收

### 6.1 完整形态 vs 最简版：刻意取舍

<img src="imgs/aicent-23-fea-workflow-2/b44478f49fa397394e16775816c3ef18_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 工作流前端的完整形态

先说说工作流前端真正应该是什么样子。

<img src="imgs/aicent-23-fea-workflow-2/bee605da0d0c967cd60fb6ba877c14e4_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/bee605da0d0c967cd60fb6ba877c14e4_MD5.jpg
用途：展示工作流前端的"完整形态"——可视化拖拽编排界面，作为刻意取舍的对照基准。
内容：UI 线框图，三栏布局。左栏：节点类型面板（START / LLM / CONDITION / API_CALL / KNOWLEDGE / END 等可拖拽节点）；中栏：画布（已拖入的节点之间连线，形成工作流图）；右栏：配置面板（点击节点后显示，配置 Prompt、模型、outputVariable 等参数）。形态类似 Dify / Coze / n8n。
-->

完整的工作流前端是可视化拖拽编排：节点从左侧面板拖进画布，节点之间连线，点击节点在右侧面板配置 Prompt 和参数。Dify、Coze、n8n 都是这个形态。

#### (2) 本篇不做完整形态，原因很直接

可视化拖拽编排是独立的前端工程量，和执行引擎的设计没有关系。本篇的重点是执行引擎跑不跑得通，不是前端够不够好看。感兴趣的话，用 React Flow 或 Vue Flow 库让 Claude Code 帮你实现，思路和前两篇是一样的。

#### (3) 本篇做最简版

最简版：列表页 + 创建页，JSON 直接手写提交。

### 6.2 列表页 + 创建页实现

Hify 前端，工作流管理页面。Vue 3 + Element Plus。

#### (1) 页面一：工作流列表页

路径：`/workflows`

```yaml
- 表格展示：名称、状态（DRAFT/PUBLISHED）、创建时间
- 操作列：删除（二次确认）
- 右上角"新建工作流"按钮，跳转创建页
```

#### (2) 页面二：工作流创建页

路径：`/workflows/create`

```yaml
表单字段：
- 名称（必填）
- 描述（可选）
- 工作流配置（必填，el-input type=textarea，rows=20）

- 预填 22 讲的智能客服分类工作流 JSON 作为示例
- JSON 编辑器下方放"格式化"按钮，点击美化缩进
- 提交前做 JSON 合法性校验，非法 JSON 给错误提示
- 提交成功后跳回列表页
```

#### (3) 调用后端接口

```bash
GET /api/v1/workflows 列表
POST /api/v1/workflows 创建
DELETE /api/v1/workflows/{id} 删除
```

#### (4) 约束

```
遵循 CLAUDE.md 的前端代码规范
```

### 6.3 后端验收：curl 脚本

#### (1) 绑定工作流到 Agent

```bash
curl -X PUT http://localhost:8080/api/v1/agents/1 \
-H "Content-Type: application/json" \
-d '{"workflowId": 1}'
```

#### (2) 发消息触发工作流

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/2/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "我买的耳机坏了，怎么申请保修"}'

curl -N -X POST http://localhost:8080/api/v1/chat/sessions/3/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "你们最新的蓝牙耳机有什么功能"}'

curl -N -X POST http://localhost:8080/api/v1/chat/sessions/4/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "耳机连不上手机蓝牙怎么办"}'
```

#### (3) 查最近一次执行记录

```bash
curl http://localhost:8080/api/v1/workflows/1/runs/latest
```

#### (4) 跑一个不触发工作流的对照

```bash
curl -N -X POST http://localhost:8080/api/v1/chat/sessions/5/messages \
-H "Content-Type: application/json" \
-H "Accept: text/event-stream" \
-d '{"content": "你好"}'
```

### 6.4 异常分支 review：四个必查边界

<img src="imgs/aicent-23-fea-workflow-2/017180dbad9c360c7377fab5e1de08d9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

按方法论三：重点 review 异常分支。Claude Code 在正常路径上基本没问题，但下面这几个边界大概率会漏，要手动检查。

#### (1) 四个边界问题

```text
条件分支所有条件都不匹配，走 defaultTarget 还是直接结束？

targetNodeKey 指向不存在的节点，会不会空指针？

工作流里有环（A→B→A），50 步限制有没有生效？

LLM 节点调用失败，workflow_node_run 是 FAILED，workflow_run 也是 FAILED，SseEmitter 推了错误提示？
```

#### (2) 逐条问，逐条验证

这四个问题逐一问 Claude Code，看处理逻辑是否正确。发现问题立刻修复再往下走。

### 6.5 前端验收：截图核对

<img src="imgs/aicent-23-fea-workflow-2/0fc1303fe6e67f2472392ad5f28be696_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/0fc1303fe6e67f2472392ad5f28be696_MD5.jpg
用途：前端验收第一步——工作流列表页，确认能看到第 22 篇创建的工作流。
内容：浏览器截图。工作流列表页（路径 /workflows），Element Plus 表格，列：名称、状态（DRAFT/PUBLISHED）、创建时间、操作（删除）。表格中能看到第 22 篇创建的工作流记录。右上角有"新建工作流"按钮。
-->

<img src="imgs/aicent-23-fea-workflow-2/333b544b32efa03e6ef0b8ce821064da_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/333b544b32efa03e6ef0b8ce821064da_MD5.jpg
用途：前端验收第二、三步——工作流创建页，确认预填示例 JSON、格式化按钮、JSON 合法性校验生效。
内容：浏览器截图。工作流创建页（路径 /workflows/create），宽幅布局。表单字段：名称、描述、工作流配置（el-input textarea，rows=20，预填第 22 篇智能客服分类工作流 JSON 示例）。配置框下方有"格式化"按钮。修改某节点 Prompt 后点击格式化，JSON 缩进美化正常。
-->

<img src="imgs/aicent-23-fea-workflow-2/b4fb13e13bcc12f49b30a544afc71d2a_MD5.jpg" style="display: block; width: 800px;" alt="默认替换文字">

<!--
图片内容说明
路径：imgs/aicent-23-fea-workflow-2/b4fb13e13bcc12f49b30a544afc71d2a_MD5.jpg
用途：前端验收第四、五步——提交后跳回列表页确认创建成功；输入非法 JSON 时前端拦截不提交。
内容：浏览器截图。提交成功后跳回的工作流列表页，新创建的工作流出现在表格中。另一状态展示输入非法 JSON 时，前端给出错误提示并阻止提交（未发起后端请求）。
-->

前端验收五步：

1. 打开工作流列表页，能看到第 22 篇创建的工作流
2. 点击新建，看到预填的示例 JSON
3. 修改其中一个节点的 Prompt，点格式化，确认 JSON 美化正常
4. 提交，确认创建成功，跳回列表页
5. 输入非法 JSON，确认前端拦截，不提交

## 7. 延伸思考：三个进阶问题

<img src="imgs/aicent-23-fea-workflow-2/e11c006f8ce34fecbf9cf5d17f97a86b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 7.1 中间状态提示：异步化 LLM 节点的反馈

当前工作流中间 LLM 节点是同步执行的。如果分类节点模型响应很慢（5 秒 +），用户要等很久才看到任何反馈。怎么给用户一个"正在分析问题类型…"的中间状态提示？让 Claude Code 帮你设计方案。

### 7.2 重放功能：执行记录需要补存哪些信息

如果要加"重放"功能——管理员看到某次执行分类错了，手动改成"售后"后从条件分支节点重新跑，需要怎么改执行引擎？`workflow_node_run` 要存哪些额外信息才能支持重放？

### 7.3 多模型配置：让不同 LLM 节点用不同模型

当前一个工作流里所有 LLM 节点共用同一套 ModelConfig。如果想让分类节点用既便宜又快的模型（GPT-4o-mini），最终回答节点用好的模型（GPT-4o），当前设计支持吗？需要怎么改？试着配一个这样的工作流测试。
