---
title: AI编程方法 12：工程搭建 - 流程回顾
author: fangkun119
date: 2026-07-01 21:00:00 +0800
categories: [AI编程, AI编程方法]
tags: [AI编程, AI编程方法]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicent-12-eng-setup-recap/cover.jpg
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: ai programming
---

<!--
aicent-12-eng-setup-recap
AI编程方法 12：工程搭建 - 流程回顾
-->

本文是系列工程搭建部分的收尾篇，复盘 Hify（AI Agent 开发平台）工程从零到能跑的全过程——后端 Maven 骨架、公共基础设施、前端 Vue 工程、UI 设计、基础组件。全文按两部分组织：第一部分把实战中反复验证的方法论提炼成可速查的手册；第二部分按真实搭建时序复现五个场景，每步给出指令、验收命令与要点。

## 全文导读地图

下图把两部分、十三个章节的阅读路径可视化。第一部分（方法论 1-7 章）用浅色节点，第二部分（实战 8-13 章）用深色节点，箭头标注方法论章与实战章的对应关系。

<img src="imgs/aicent-12-eng-setup-recap/b297a96f92159e834eaeacb25b63d541_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

**如何使用本文**：

| 阅读路径 | 适用读者 | 建议章节 |
|---------|---------|---------|
| 速查路径 | 只想快速对齐项目阶段 | 第 1-7 章（方法论 + Check List） |
| 通读路径 | 系统掌握方法论与架构思维 | 第 1-13 章顺序阅读 |
| 复现路径 | 要把 Hify 工程搭起来 | 直接跳第 8-12 章照做 |

**第一部分　工程搭建方法论提炼**

本部分把工程搭建过程中反复验证的有效动作提炼成六条方法论，外加一份可裁剪的 Check List。每条方法论都做到具体可操作，读者可据此指导团队或直接落地到自己的项目。

## 1. 认知对齐先于执行：让 AI 理解约束

<img src="imgs/aicent-12-eng-setup-recap/78b3b6bc3bbe45e7a1c31dd053d02dee_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">工程搭建的起点不是写第一行代码，而是让 AI 先读一遍 `CLAUDE.md`，确认它理解了项目结构与模块划分。</span>这一步不产出任何代码，只做"认知对齐"，但能显著降低后续执行阶段的返工概率。

### 1.1 为什么"对齐认知"比"开始动手"更重要

后端工程师手工搭过多次 Spring Boot 多模块工程，往往会直接进入"生成 pom、建包、写配置"的执行状态。但把同样的直觉带进 AI 协作流程，问题就出现了：AI 不清楚项目的约束边界，只能依据通用模板生成，结果常常"看起来合理、其实不符合团队意图"。

认知对齐的价值在于三点：第一，让 AI 在生成前就掌握模块清单、依赖关系、命名规范；第二，AI 的反馈能反向暴露 `CLAUDE.md` 里模糊或遗漏的表述，等于让 AI 帮团队审规范；第三，对齐发生在执行之前，成本远低于执行后返工。

### 1.2 操作要点：只读 CLAUDE.md，不做代码

认知对齐阶段的操作很轻量，关键是"只读、不写"。对应的指令直接复用原文场景一第一步的两条原文。

```text
查看当前 CLAUDE.md，确认项目结构和模块划分部分是否清晰

如果有遗漏或不清晰的地方告诉我。
```

这两条指令的设计意图明确：第一条让 AI 通读约束文档，第二条要求 AI 主动反馈模糊点，而不是默默按理解执行。AI 在这一步不做任何工程动作，只输出对 `CLAUDE.md` 的理解与疑问。

**① 反例：跳过对齐直接生成**

工程师跳过认知对齐，直接下达"按 Maven 多模块搭建工程"的指令。由于 AI 不知道团队约定的模块命名（`hify-common` / `hify-app` / `hify-provider` 等）、依赖方向（业务模块依赖 `hify-common`）和版本统一管理方式，它会按通用 Spring Boot 多模块模板生成。结果模块名对不上、`dependencyManagement` 缺失、目录结构与 `CLAUDE.md` 定义不一致，工程师只能在 review 阶段逐条返工。

**② 正例：先对齐再执行**

工程师先让 AI 读 `CLAUDE.md` 并反馈遗漏，AI 指出"模块划分清晰，但未声明各业务模块对 `hify-common` 的依赖方向"。工程师补全约束后再进入生成阶段，AI 产出的父 pom、`<modules>` 声明、目录结构一次就与规范对齐，执行阶段几乎不需要返工。

## 2. 任务拆解到"一次能验证"的颗粒度

<img src="imgs/aicent-12-eng-setup-recap/98fcc9d214f4588034b714d96117d949_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

工程搭建任务交给 AI 时，"拆到多细"是高频疑问。本节给出两个判断标准，并落到一个可操作的颗粒度定义——"一次能验证"，最后给出 Hify 工程的验收锚点表。

### 2.1 两个判断标准

判断拆分是否合理，有两条客观标准：

**生成量是否超出一次 review 的范围**：单个子任务让 AI 生成的内容，工程师是否还能在有限时间内逐文件核对。超出则意味着问题会混在大段输出里难以定位。
**步骤间是否有依赖关系**：如果后一步必须依赖前一步的产物（例如业务模块依赖 `hify-common` 的统一响应类），就必须拆开串行执行，不能并到一个任务里让 AI 自行编排。

### 2.2 "一次能验证"是什么

把上述两条标准收敛成一个实操颗粒度：<span style="color: red; font-weight: bold;">**每个子任务做完，工程师能跑一条命令或访问一个接口，确认这个子任务是对的**</span>。

<span style="color: red; font-weight: bold;">这种颗粒度的好处是每个验收点都成为一个"锚"——确认到这里是对的，往后出问题就能锁定在锚点之后的范围，而不必回溯整条链路。</span>

三种典型拆分情况的对比如下。

| 拆分情况 | 特征 | 后果 |
|---------|------|------|
| 拆太粗 | 一个任务包含骨架+基础组件+业务空壳 | 生成量大、review 困难、问题难定位 |
| 拆太细 | 每个类、每个配置文件单独成任务 | 频繁切换上下文、效率反而下降 |
| 合适 | 以"一次能验证"为单位 | 每步可独立验收、问题可快速定位 |

### 2.3 Hify 工程的验收锚点表

Hify 工程搭建按"一次能验证"颗粒度拆分后，得到四个串行锚点。每个锚点都对应一条可执行的验证命令。

| 验收锚点 | 验证命令 |
|---------|---------|
| Maven 骨架搭完 | `mvn compile` 能通过 |
| `hify-common` 搭完 | `mvn compile` 没有编译错误 |
| 业务空壳搭完 | Spring Boot 能启动 |
| 健康检查接口加完 | `curl localhost:8080/api/v1/health` 返回 200 |

锚点串行推进，每通过一个就锁定一个正确状态。如果 `mvn compile` 通过但 Spring Boot 启动失败，问题必然在"业务空壳"这一段，而无需怀疑 Maven 骨架阶段。

## 3. 执行控制：模板化指令与边界约束

<img src="imgs/aicent-12-eng-setup-recap/f8b8209d28fca05b034b55970efa5cf9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

进入执行阶段后，工程师对 AI 的控制手段可以分成两类：一类是把模板类工作整体交给 AI，另一类是用"边界约束"语句限制 AI 的生成范围。两类手段配合使用，既能提升效率，又能避免 AI 自行发挥产生多余内容。

### 3.1 模板类工作交给 AI

工程搭建中存在大量"工程师脑子里有、但每次都要查参数查语法"的模板性工作。这类工作不需要思考，只需要正确生成，正是 AI 最擅长的领域。Hify 工程中典型的模板类配置包括：

- Maven 多模块的 `pom` 结构（父 pom 声明子模块、`dependencyManagement` 统一版本）
- MyBatis-Plus 的分页插件配置
- `logback-spring.xml` 的多环境日志区分
- 线程池的 `ThreadPoolExecutor` 写法

对这类工作，工程师只需描述清楚"要什么"，AI 生成后由工程师做 review 确认，即可快速完成。需要注意的是模板类指令也要把意图讲透——例如线程池配置若不强调"线程名前缀要可识别"，AI 给出的默认 `ThreadFactory` 会让日志里全是 `pool-1-thread-3`，无法区分业务来源。问题不在 AI 的能力，而在指令是否覆盖了关键意图。

### 3.2 用"边界约束"防止生成多余内容

模板类指令放开了 AI 的生成范围，边界约束则相反——它用来限定"只生成什么、不生成什么"。不写边界约束，AI 会倾向于按"完整方案"生成，附带大量当前阶段不需要的代码。

Hify 工程场景一第二步的边界约束是典型示例。

```text
只创建 pom 文件和目录结构，不需要任何 Java 代码。
```

这条约束出现在 Maven 骨架阶段的指令里。如果不写，AI 很可能在生成 pom 的同时附带生成启动类、Controller、配置类等 Java 代码，而这些内容属于后续阶段的任务，提前生成只会污染当前锚点的验收结果。

边界约束的写法可归纳为一个句式：<span style="color: red; font-weight: bold;">**明确"只做什么" + 明确"不做什么"**</span>。在 Maven 骨架阶段是"只创建 pom 和目录结构 + 不需要 Java 代码"；在业务空壳阶段可以是"每个包只放占位类 + 不需要任何业务代码"。工程师在每个子任务前花一行写清边界，比事后删多余代码的成本低得多。

## 4. 异常处理：给完整上下文而非最后一行报错

<img src="imgs/aicent-12-eng-setup-recap/0ea1aa6d2fd3d406805c1a2457e469ae_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

工程搭建阶段最高频的摩擦点不是「写不出代码」，而是「启动报错后修不动」。工程师把报错丢给 AI，AI 给一个看起来合理的修复，结果问题没解决反而冒出新的报错，来回两三轮才收尾。这一章拆开「为什么慢」和「怎么修最快」。

### 4.1 为什么 AI 修复有时要两三轮

AI 修复报错的机制是：从错误信息倒推原因。它拿到的信息越少，推断空间越大，越容易命中「看起来对、其实不对」的方案。

典型的失真场景有两类：

| 失真类型 | 表现 | 根因 |
|---------|------|------|
| 表面修复 | 报错消失，但换了个位置又报 | 只解决了报错的那一行，没解决根本原因 |
| 方向跑偏 | 修改方案和项目实际架构无关 | 缺少项目结构、模块依赖、版本约束等上下文 |

工程搭建阶段最常踩的是第二类：Maven 多模块、Spring Boot 启动、MyBatis-Plus 注入这类问题，背后往往牵涉模块依赖关系、Bean 扫描路径、依赖版本冲突。只贴最后一行报错，AI 根本看不到这些约束，只能盲猜。

### 4.2 高效处理：完整上下文 + 相关代码 + 明确问"根本原因"

把完整的错误上下文喂给 AI，而不只是最后一行。下面是两种处理方式的对比。

#### (1) 差的方式：信息量不足

```text
报错了，帮我修复
```

这种 prompt 等于让 AI 凭空猜，准确率几乎为零。

#### (2) 好的方式：三要素齐全

<span style="color: red; font-weight: bold;">一个能拉高修复准确率的 prompt 至少包含三部分：完整错误日志、相关代码与项目结构、明确诉求。</span>

```text
mvn clean install 报错了，完整错误如下：

[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.11.0:compile
(default-compile) on project hify-app: Compilation failure
...（完整错误日志，包含完整的 stack trace 与模块路径）

项目结构是 Maven 多模块，hify-common 被 hify-app 依赖，相关 pom 如下：

<dependency>
  <groupId>com.hify</groupId>
  <artifactId>hify-common</artifactId>
  <version>${project.version}</version>
</dependency>
...（相关 pom 内容）

帮我分析根本原因并修复。
```

三要素的作用对照：

| 要素 | 作用 |
|------|------|
| 完整错误日志 | 让 AI 看到真正的异常链，而不是被截断的最后一行 |
| 相关代码 + 项目结构 | 给 AI 提供约束（模块依赖、版本、扫描路径），缩小推断空间 |
| 明确问"根本原因" | 引导 AI 往根因走，而不是只抹掉报错那一行 |

**① 两三轮还没解决，停下来自己看一眼错误信息**

如果来回两三轮问题还没收敛，继续贴报错给 AI 通常是在原地打转。这时候工程师自己看一眼错误信息，往往问题比想象中简单——可能只是一个 Bean 注入失败、一个 `@MapperScan` 扫描路径漏写、或者一个依赖版本冲突。<span style="color: red; font-weight: bold;">AI 修复慢，绝大多数时候是上下文喂得不够；自己看一眼能立刻定位是「喂少了」还是「方向错了」，再决定是补上下文还是换思路。</span>

## 5. 规范迭代：SDD 闭环是日常

<img src="imgs/aicent-12-eng-setup-recap/71a00a56eb3d2be310b264d601d9a0a5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">工程搭建过程中，规范不是一次性写完的，而是通过 SDD（Spec-Driven Development）闭环一点点补出来的。</span>本系列前几篇强调的 CLAUDE.md，在日常开发里就是这样被「喂胖」的：每次 AI 跑偏，就是一次补规范的机会。

### 5.1 案例背景：GlobalExceptionHandler 硬编码错误码

搭建 hify-common 公共基础设施时，子任务三是创建全局异常处理器 `GlobalExceptionHandler`（用 `@RestControllerAdvice` 注解）。要求很明确：所有响应必须使用 `Result.fail()` + `ErrorCode` 枚举，禁止硬编码错误码。

但 AI 的第一版输出在兜底 catch 里偷了懒，硬编码了错误码：

```json
{
  "code": 500,
  "message": "系统繁忙"
}
```

这段硬编码违反了规范——错误码和提示文案散落在 catch 块里，后续维护时谁也说不清它和 `ErrorCode.INTERNAL_ERROR` 是不是一回事，迟早会和枚举里的定义对不上。

### 5.2 SDD 闭环四步

发现这类偏差后，正确的处理不是「改完就完」，而是走一遍 SDD 闭环，把一次性事故变成可复用的规范。

**① 发现偏差**

Review AI 输出时识别出硬编码错误码，判断它违反了「异常处理必须使用 ErrorCode 枚举」的约束。

**② 改正代码**

把兜底 catch 里的硬编码替换成枚举调用：

```text
Result.fail(ErrorCode.INTERNAL_ERROR)
```

**③ 同步 CLAUDE.md**

在 CLAUDE.md 补一条规范，把这次的约束沉淀下来：

```text
异常处理必须使用 ErrorCode 枚举，禁止硬编码错误码
```

**④ 形成可复用规范**

这条规范从此成为 AI 在本项目里的默认约束，后续生成任何异常处理代码都会自动遵守，同类问题不会再重复出现。

### 5.3 落地动作

SDD 闭环的价值不在某一次具体修复，而在「规范库」的持续增长。工程师要养成的动作链是：

| 动作 | 触发时机 | 产出 |
|------|---------|------|
| Review AI 输出 | 每次生成代码后 | 发现偏差（硬编码、偷懒、跑偏） |
| 改正 + 同步 CLAUDE.md | 发现偏差立即做 | 规范条目 +1 |
| 下次复用 | AI 读取更新后的 CLAUDE.md | 同类问题不再出现 |

<span style="color: red; font-weight: bold;">这不是什么宏大流程，就是每次 AI 跑偏时随手堵住同类问题的口子。工程搭建跑完一遍，CLAUDE.md 里自然就攒出一套贴合本项目实际、可复用的工程规范。</span>

## 6. UI 协作：用"位置 + 问题 + 期望"句式驱动 AI

<img src="imgs/aicent-12-eng-setup-recap/8e08313296ec47560e2ff37fd18076d3_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前端 UI 打磨是后端工程师最常卡壳的环节：能看出哪里不对，但说不清楚该怎么改。这一章给出一个通用句式和一套示例库，让「看出来」直接转化成「说清楚」，AI 负责把描述翻译成代码。

### 6.1 通用句式

驱动 AI 改 UI 的核心是一个固定句式，三个槽位填满即可：

```text
[位置/组件] + [哪里不对] + [应该怎样]
```

- `[位置/组件]`：问题出现在哪个元素上（表格、按钮、侧边栏菜单项、弹窗等）
- `[哪里不对]`：当前状态有什么问题（间距太大、颜色不对、选中态不明显、没有反馈）
- `[应该怎样]`：期望的最终效果（改成 16px、用红色文字、加 3px 蓝色竖线、加转圈动画）

<span style="color: red; font-weight: bold;">这个句式的好处是：不需要任何 CSS 知识，也不需要知道 Element Plus 的组件 API，只要工程师能给出「判断」，AI 就能落地。</span>

### 6.2 示例库

下面是本系列前几篇实战中用过的 4 条 UI 调整指令，每条都严格遵循「位置 + 问题 + 期望」结构，直接可用。

**示例 1　表格间距**

```text
表格和顶部标题区之间间距太大，改成 16px
```

- 位置：表格与顶部标题区之间
- 问题：间距太大
- 期望：改成 16px

**示例 2　删除按钮样式**

```text
操作列的删除按钮颜色用红色，但不要 danger 类型的背景，用 text 类型红色文字
```

- 位置：操作列的删除按钮
- 问题：当前是 danger 类型带背景
- 期望：改成 text 类型红色文字（去掉背景）

**示例 3　侧边栏选中态**

```text
侧边栏菜单项选中态太不明显，选中的菜单项左侧加一条 3px 的蓝色竖线
```

- 位置：侧边栏菜单项选中态
- 问题：选中态不明显
- 期望：选中项左侧加 3px 蓝色竖线

**示例 4　弹窗 loading 反馈**

```text
弹窗提交按钮在 loading 时要有转圈动画，不能让用户以为没反应
```

- 位置：弹窗提交按钮
- 问题：loading 时无反馈，用户以为没反应
- 期望：加转圈动画

### 6.3 为什么不需要懂 CSS

很多后端工程师觉得自己「不懂前端」就不敢碰 UI 打磨，这个认知是错的。用「位置 + 问题 + 期望」句式驱动 AI 时，工程师只需要提供两样东西，CSS 和组件 API 全部由 AI 负责。

**① 工程师只提供判断，不提供实现**

工程师不需要知道 `margin` 还是 `padding`、不需要知道 `el-button` 的 `type` 属性有哪些取值、不需要知道选中态用伪元素还是边框实现。只需要描述清楚三件事：

- 在哪里（表格、按钮、菜单项、弹窗）
- 什么问题（间距大、颜色不对、不明显、没反馈）
- 想要什么效果（具体数值或视觉描述）

AI 负责把这些描述翻译成对应的 CSS 属性、Element Plus 组件 props、甚至是 Vue 组件结构。<span style="color: red; font-weight: bold;">工程师有判断力，AI 有执行力，二者分工明确，UI 打磨就能持续迭代出有品牌感的界面。</span>

## 7. 工程搭建阶段 Check List（可裁剪）

<img src="imgs/aicent-12-eng-setup-recap/70a50df26d369a311a1bb6fdeddf04d4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节把前 6 章的方法论与 5 个实战场景的验收锚点压缩成一份可勾选清单，按"认知层 / 执行层 / 验收层 / 协作层"四象限分层。复制下方 code block，按阶段勾选即可；每一条对应一个方法论点或一个验收锚点，方便对照本系列前几篇追溯"为什么"。

### 7.1 认知层（开工前对齐）

认知层是工程搭建的起点：先让 AI 理解约束，再动手。任何一条没勾上，后面执行层都会多走弯路。

```markdown
- [ ] 让 AI 读一遍 CLAUDE.md，并主动反馈遗漏与模糊表述
- [ ] AI 复述的模块清单与团队约定一致（模块划分、包路径、命名规范）
- [ ] AI 复述的依赖方向正确（hify-app 依赖 hify-common，公共模块不反向依赖业务模块）
- [ ] CLAUDE.md 中模块划分、接口规范、异常处理规范三节齐全且无歧义
- [ ] 明确"对齐阶段只读不做代码"，确认通过后才进入执行层
```

### 7.2 执行层（拆分与生成）

执行层是产出代码的主战场：颗粒度对、模板类工作交给 AI、每条指令带边界约束。

```markdown
- [ ] 任务按"一次能验证"颗粒度拆分（每步对应一条可跑命令或可访问接口）
- [ ] 模板类配置（父 pom / 子 pom、MyBatis-Plus 分页、logback、线程池）交给 AI 生成后 review
- [ ] 每条指令带边界约束：明确"只做什么" + "不做什么"
      例：只创建 pom 和目录结构，不需要任何 Java 代码
- [ ] 线程池等需讲清关键意图（线程名前缀可识别，避免日志里全是 pool-1-thread-3）
- [ ] 生成后逐项 review：模块声明与目录对应、依赖关系正确、版本管理统一
- [ ] 业务空壳只建 package 结构和占位类，不写任何业务代码
```

### 7.3 验收层（锚点串行）

验收层按工程搭建时序串成一条"锚链"，每过一个锚点就确认这一段是对的，往后出问题能锁定在锚点之后。

```markdown
- [ ] 锚点 1 Maven 骨架：mvn compile 出现 BUILD SUCCESS
- [ ] 锚点 2 hify-common：mvn compile 无编译错误
- [ ] 锚点 3 业务空壳：mvn spring-boot:run -pl hify-app 启动成功
- [ ] 锚点 4 健康检查：curl http://localhost:8080/api/v1/health 返回 200
- [ ] 锚点 4 响应体：返回统一 Result 结构（{"code":200,"message":"success","data":"Hify is running"}）
- [ ] 锚点 5 前端联通：调健康检查接口，页面出现绿色"后端已连接"
- [ ] 锚点 5 前端基础组件：HifyTable / HifyFormDialog / useConfirm / useRequest / notify 五件套生成可用
- [ ] 启动类注解 @MapperScan("com.hify.**.mapper") 已配置（否则 Mapper 注入失败）
- [ ] Vite 代理 /api → localhost:8080 已配置（开发阶段不需要 Nginx）
```

### 7.4 协作层（异常与规范迭代）

协作层贯穿全流程，决定 AI 是越用越顺还是越用越拧：报错给完整上下文、SDD 闭环堵口子、UI 打磨敢说。

```markdown
- [ ] 报错处理给 AI 完整上下文：完整错误日志 + 相关代码 + 项目结构 + 明确问"根本原因"
- [ ] 两三轮未解决则停下来人工看错误信息（可能是 Bean 注入失败或依赖版本冲突）
- [ ] SDD 闭环四步走通：发现偏差 → 改代码 → 同步 CLAUDE.md → 形成可复用规范
- [ ] 异常处理统一走 Result.fail() + ErrorCode 枚举，禁止硬编码 code/message
- [ ] 前后端规范对齐：axios 响应拦截器自动解包 code === 200 的 data 字段
- [ ] UI 打磨用"[位置/组件] + [哪里不对] + [应该怎样]"句式（无需知道 CSS 属性名）
- [ ] 设计系统指令结构完整：产品定位 → 用户群体 → 风格方向 → 具体参考
```

完成情况速判：四象限全绿即可进入下一篇「业务功能开发 · 模型提供商管理」；任何一象限有红，回到对应章节定位偏差。

**第二部分　Hify 工程搭建实战复现**

本部分按真实搭建时序复现五个场景，每个场景都给出目标、指令、验收命令与要点。读者可按章节顺序照做，复现 Hify 工程从空目录到前后端联通的全过程。所有指令、命令、JSON 输出、配置片段均放在 code block 中，便于直接复制使用。

## 8. 场景一：Maven 多模块骨架搭建

<img src="imgs/aicent-12-eng-setup-recap/a38f3361026431c745985842c01d34f6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 8.1 目标与验收锚点

本场景的目标是从一个空目录起步，完成 Hify 的 Maven 多模块工程骨架，让 `mvn compile` 顺利通过。

验收锚点只有一条：终端出现 `BUILD SUCCESS`。这是一个"零业务逻辑"的纯结构性任务，重点不在于写代码，而在于让 AI 严格按 CLAUDE.md 中定义的项目结构落目录和 pom，不偏不漏。

整个过程拆成三步：先做认知对齐，再生成结构，最后验收。

### 8.2 第一步：确认 CLAUDE.md 已就绪

工程初始化前，先让 AI 读一遍 CLAUDE.md，确认它理解了项目结构和模块划分的约束。这一步不产出任何代码或配置，只做"认知对齐"。

```text
查看当前 CLAUDE.md，确认项目结构和模块划分部分是否清晰。

如果有遗漏或不清晰的地方告诉我。
```

操作要点：

**① 让 AI 先读后做**

不要直接让 AI 开始生成 pom。先让它把 CLAUDE.md 中的模块列表、依赖关系、版本管理策略读一遍，并用它自己的话复述。复述的过程就是暴露理解偏差的过程。

**② 关注 AI 的反馈**

AI 在读完 CLAUDE.md 后给出的反馈往往能帮团队发现文档里模糊的表述。例如"模块之间的依赖方向没有写清楚""未说明是否需要统一 Spring Boot 版本"。这些反馈本身就是 CLAUDE.md 迭代的输入，应当顺手补全，再进入下一步。

**③ 这一步零产出是正常的**

认知对齐阶段不出代码、不出 pom、不出目录。看似浪费一轮对话，实际是为后面的执行阶段减少返工。

### 8.3 第二步：生成父 pom 和子模块结构

认知对齐完成后，下达生成指令。指令中必须带上边界约束，限制 AI 只产出结构，不生成任何 Java 代码。

```text
按照 CLAUDE.md 中定义的项目结构，创建 Hify 的 Maven 多模块工程骨架。

父 pom 声明所有子模块，dependencyManagement 统一管理版本。

只创建 pom 文件和目录结构，不需要任何 Java 代码。
```

操作要点：

**① "不需要 Java 代码"是关键约束**

如果不显式写明这条边界，AI 会顺手生成一批"看起来合理"的占位类、Controller、配置类。这些多余的代码会让后续的 review 变重，也容易和真正的业务代码产生冲突。把生成范围钉死在 pom 和目录结构上，是这一步的核心动作。

**② 拿到输出后检查三件事**

拿到 AI 生成的父 pom 和子模块 pom 后，对照检查：

| 检查项 | 验证方式 |
|--------|----------|
| 模块声明和目录是否对应 | 父 pom 的 `<modules>` 列表与磁盘上的子模块目录一一核对 |
| 依赖关系是否正确 | 子模块之间的 `<dependency>` 声明与 CLAUDE.md 的依赖图一致 |
| 版本管理是否统一 | 公共依赖的版本都放在父 pom 的 `dependencyManagement` 中 |

一个典型的父 pom 模块声明片段如下：

```xml
<modules>
    <module>hify-common</module>
    <module>hify-provider</module>
    <module>hify-agent</module>
    <module>hify-chat</module>
    <module>hify-mcp</module>
    <module>hify-knowledge</module>
    <module>hify-workflow</module>
    <module>hify-app</module>
</modules>
```

**③ 对照 CLAUDE.md 逐一核对 `<modules>`**

review 的具体动作是：打开 CLAUDE.md 的模块列表，逐条对照父 pom 中的 `<module>` 声明。发现漏一个模块、多一个模块、或者模块名拼写不一致，立刻让 AI 修正。这一步的细致核对，能避免后面所有业务模块都因为骨架错位而报错。

### 8.4 第三步：验收

骨架就绪后，用一条命令验收：

```bash
mvn compile
```

看到 `BUILD SUCCESS`，Maven 多模块骨架完成。

如果出现编译报错，不要自己猜测原因，直接把完整的错误日志贴给 AI，并带上相关 pom 的内容。完整的上下文能让 AI 的修复准确率显著提升。报错处理的详细方法见本系列前几篇的"异常处理"章节。

## 9. 场景二：hify-common 公共基础设施

<img src="imgs/aicent-12-eng-setup-recap/fe79333fbd7b1fc7f79236e20f5d2768_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 9.1 目标与五个子任务

本场景的目标是为 Hify 后端搭好公共基础设施 hify-common 模块。五个子任务串行执行，每完成一个就做一次编译验证，确认无误后再进入下一个。

| 顺序 | 子任务 | 包路径 | 验收方式 |
|------|--------|--------|----------|
| 一 | Result 和 PageResult | `com.hify.common.web` | `mvn compile` 通过 |
| 二 | ErrorCode 和 BizException | `com.hify.common.exception` | `mvn compile` 通过 |
| 三 | GlobalExceptionHandler | `com.hify.common.exception` | `mvn compile` 通过 + review 兜底逻辑 |
| 四 | MyBatis-Plus 配置 | `com.hify.common.config` | `mvn compile` 通过 |
| 五 | Redis 配置 | `com.hify.common.config` | `mvn compile` 通过 |

串行的好处是问题定位简单：每个子任务都是一个验收锚点，哪里出错就在哪里修，不会和后面的任务互相干扰。

### 9.2 子任务一：Result 和 PageResult

这是五个子任务里最简单的一个，AI 基本不会出错。指令如下：

```text
在 hify-common 中创建统一响应类 Result<T> 和分页响应类 PageResult<T>。

Result 包含 code、message、data，提供 ok() 和 fail() 静态方法。

PageResult 继承 Result，额外包含 total、page、size。

放在 com.hify.common.web 包下。
```

操作要点：

**① review 字段名与 CLAUDE.md 接口规范一致**

拿到 AI 生成的代码后，重点核对字段名。CLAUDE.md 中定义的接口规范字段是 `code`、`message`、`data`，分页响应额外有 `total`、`page`、`size`。如果 AI 用了 `resultCode`、`msg`、`list` 这类常见但不一致的命名，立刻让它统一改掉。字段命名是前后端协议的根基，必须在此处钉死。

**② 确认静态方法签名**

确认 `ok()` 和 `fail()` 的方法签名符合团队习惯。例如 `Result.ok(data)` 返回成功响应，`Result.fail(ErrorCode)` 或 `Result.fail(code, message)` 返回失败响应。这一步的统一会直接影响后续 GlobalExceptionHandler 的写法。

### 9.3 子任务二：ErrorCode 和 BizException

在统一响应类之后，紧接着搭错误码体系和业务异常类。指令如下：

```text
在 hify-common 中创建错误码枚举 ErrorCode 和业务异常类 BizException。

ErrorCode 包含 PARAM_ERROR、UNAUTHORIZED、NOT_FOUND、INTERNAL_ERROR 等常用错误码。

BizException 持有 ErrorCode，支持自定义 message 覆盖。

放在 com.hify.common.exception 包下。
```

操作要点：

**① 错误码携带 code 和 message**

枚举的每个值都应当带 HTTP 风格的 code 和人类可读的 message，例如 `PARAM_ERROR(400, "参数错误")`、`INTERNAL_ERROR(500, "系统繁忙")`。这样 GlobalExceptionHandler 直接拿枚举就能拼出 `Result.fail()`。

**② BizException 支持自定义 message 覆盖**

有些场景需要把具体的校验信息塞进异常，例如"用户名不能为空"。让 `BizException` 的构造方法同时支持"只传 ErrorCode"和"传 ErrorCode + 自定义 message"两种形式，后者覆盖枚举里的默认 message。这个设计直接关系到子任务三里 `MethodArgumentNotValidException` 的处理能否把字段级校验信息透传给前端。

**③ 编译验证**

执行 `mvn compile` 确认无误后进入子任务三。

### 9.4 子任务三：GlobalExceptionHandler（SDD 闭环）

这是 hify-common 里最容易出问题、也最能体现"规范迭代"价值的一个子任务。指令如下：

```text
在 hify-common 中创建全局异常处理器 GlobalExceptionHandler（@RestControllerAdvice）。

捕获 BizException 返回对应 ErrorCode，

捕获 MethodArgumentNotValidException 返回 PARAM_ERROR 和具体校验信息，

兜底捕获 Exception 返回 INTERNAL_ERROR。

所有响应必须使用 Result.fail() + ErrorCode 枚举，禁止硬编码错误码。
```

操作要点：

**① 第一版常见偏差：兜底 catch 硬编码错误码**

即便指令里已经写了"禁止硬编码错误码"，AI 在生成兜底的 `catch (Exception e)` 分支时，仍然可能给出这样的写法：

```json
{"code": 500, "message": "系统繁忙"}
```

问题在于这个 JSON 是手写的，绕过了 `ErrorCode` 枚举和 `Result.fail()` 通道。一旦后续枚举里 `INTERNAL_ERROR` 的 code 或 message 改了，这个兜底分支不会跟着更新，错误码体系就出现了裂缝。

**② 改成走枚举通道**

让 AI 把兜底分支改成：

```text
Result.fail(ErrorCode.INTERNAL_ERROR)
```

这样所有错误响应都统一走 `Result.fail()` + `ErrorCode`，错误码的修改只需要在一个地方发生。

**③ 同步在 CLAUDE.md 补一条规范**

发现这个偏差后，不能只改代码，还要把它沉淀为规范，防止同类问题在别处复现。在 CLAUDE.md 里补一条：

```text
异常处理必须使用 ErrorCode 枚举，禁止硬编码错误码。
```

**④ SDD 闭环的完整动作**

这一组动作就是 SDD（Spec-Driven Development）闭环在日常开发里的样子，不是一次性写完的：

| 步骤 | 动作 |
|------|------|
| 发现偏差 | review 时发现兜底 catch 硬编码了 code/message |
| 改正代码 | 改为 `Result.fail(ErrorCode.INTERNAL_ERROR)` |
| 同步 CLAUDE.md | 补一条"禁止硬编码错误码"的规范 |
| 形成可复用规范 | 后续所有异常处理都受这条规范约束 |

每次 AI 跑偏时随手堵住同类问题的口子，CLAUDE.md 就这样在一次次的实战中长厚。这是本系列反复强调的方法论之一：规范迭代是日常，而不是项目启动时一次性写死的文档。

**⑤ 编译验证**

执行 `mvn compile` 确认 GlobalExceptionHandler 编译通过，进入子任务四。

### 9.5 子任务四和五：MyBatis-Plus + Redis 配置

最后两个子任务是两份高频模板配置：MyBatis-Plus 的自动填充处理器，以及 Redis 的 Jackson 序列化配置。两者都属于"写过一次就能复用一辈子"的模板代码，AI 的生成质量很高，基本不需要改动。

**① MyBatis-Plus 自动填充处理器**

创建 MetaObjectHandler 的实现，处理 `createTime`、`updateTime` 等字段的自动填充。这类配置的关键是确认填充字段名和实体类注解 `@TableField(fill = ...)` 一致。

**② Redis 的 Jackson 序列化配置**

创建 RedisTemplate 的配置类，把默认的 JDK 序列化换成 Jackson 序列化，避免存进 Redis 的 value 在可读性、跨语言兼容性上出问题。这类配置 AI 几乎是"开箱即用"，验收时确认 `RedisSerializer`、`ObjectMapper` 的可见性设置（例如 `ObjectMapper.DefaultTyping`）符合团队约定即可。

**③ 确认 @MapperScan 与包路径**

两个配置都和 MyBatis-Plus、Redis 强相关，注意在启动类上保留 `@MapperScan` 并把扫描路径写对：

```text
@MapperScan("com.hify.**.mapper")
```

扫描路径写错或不写，会导致所有 Mapper 都注入不了，是 MyBatis-Plus 工程里最高频的启动报错之一。这一步的细节会在下一篇"业务模块空壳与启动验证"里专门展开。

**④ 验收**

执行 `mvn compile`，两份配置类都编译通过后，hify-common 的五个子任务全部就绪。公共基础设施层完成，后续业务模块可以直接复用 `Result`、`ErrorCode`、`GlobalExceptionHandler` 以及这两份配置。

## 10. 场景三：业务模块空壳与启动验证

<img src="imgs/aicent-12-eng-setup-recap/0b2a7d7d603459538a299ccf11e433bb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 10.1 目标

本场景的工程目标分三段：

- 搭完所有业务模块的 package 空壳（`hify-provider` / `hify-agent` / `hify-chat` / `hify-mcp` / `hify-knowledge` / `hify-workflow` 六个模块）。
- Spring Boot 能在 `hify-app` 中正常启动，端口 `8080` 监听成功。
- 健康检查接口 `GET /api/v1/health` 返回 `200`，说明上下文加载、Bean 注入、Web MVC 三条链路全通。

本场景是"第二部分实战"的关键中段，承担"后端骨架可运行"的判定职责。验收锚点如下：

| 阶段 | 验证动作 | 期望结果 |
|------|---------|---------|
| package 结构生成 | `mvn compile` | `BUILD SUCCESS` |
| 启动类 + 配置文件就绪 | `mvn spring-boot:run -pl hify-app` | Tomcat 启动、端口 8080 监听 |
| 健康检查接口 | `curl http://localhost:8080/api/v1/health` | HTTP 200 + 统一响应体 |

### 10.2 第一步：业务模块 package 结构

将下列命令式文本作为 prompt 提交给 AI：

```text
为 hify-provider、hify-agent、hify-chat、hify-mcp、hify-knowledge、hify-workflow
创建标准的 package 结构。

按 CLAUDE.md 代码组织规范，每个模块包含 web / api / domain / infra 四个包，
每个包里创建一个空的占位类（在类上加注释说明这个包的职责）。
不需要任何业务代码。
```

模块与基础包路径对照：

| 模块名 | 根包路径 | 四个子包 |
|--------|---------|---------|
| `hify-provider` | `com.hify.provider` | `web` / `api` / `domain` / `infra` |
| `hify-agent` | `com.hify.agent` | `web` / `api` / `domain` / `infra` |
| `hify-chat` | `com.hify.chat` | `web` / `api` / `domain` / `infra` |
| `hify-mcp` | `com.hify.mcp` | `web` / `api` / `domain` / `infra` |
| `hify-knowledge` | `com.hify.knowledge` | `web` / `api` / `domain` / `infra` |
| `hify-workflow` | `com.hify.workflow` | `web` / `api` / `domain` / `infra` |

关键约束与 review 要点：

**① 边界约束钉死**

"不需要任何业务代码"是关键边界约束——不说会生成一堆 Controller / Service / Mapper 骨架，污染目录、增加 review 成本。

**② 占位类承担自解释**

每个占位类上的注释承担"模块自解释"作用，让后续阅读者一眼看出 `web`（HTTP 入口）、`api`（对外 API 契约 / DTO）、`domain`（领域模型 / 聚合）、`infra`（基础设施 / 持久化 / 外部对接）四层职责。

**③ 锚点对齐方法论**

该步对应方法论章节"任务拆解到一次能验证"——package 结构生成完毕，`mvn compile` 通过即锁定此锚点。

### 10.3 第二步：Spring Boot 启动类和配置文件

```text
在 hify-app 中创建 Spring Boot 启动类 HifyApplication。
@SpringBootApplication + @MapperScan("com.hify.**.mapper")。
创建 application.yml，配置数据库、Redis、MyBatis-Plus、端口 8080。
数据库连接信息用 localhost 本地开发配置。
```

启动类核心注解说明：

**① `@SpringBootApplication` 的扫描范围**

`@SpringBootApplication` 等价于 `@SpringBootConfiguration` + `@EnableAutoConfiguration` + `@ComponentScan`，默认扫描启动类所在包及子包。把 `HifyApplication` 放在 `com.hify` 根包下，可覆盖所有业务模块。

**② `@MapperScan` 必须显式声明**

`@MapperScan("com.hify.**.mapper")` 必须显式声明——MyBatis-Plus 不会自动扫描跨模块的 Mapper 接口。一旦漏配，启动期不会报错，但运行期所有 Mapper Bean 注入失败，抛 `NoSuchBeanDefinitionException`。扫描路径使用 Ant 风格通配 `**`，确保 `com.hify.provider.mapper`、`com.hify.agent.mapper` 等都被纳入。

`application.yml` 关键项（本地开发）：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/hify?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  redis:
    host: localhost
    port: 6379
    timeout: 3000

mybatis-plus:
  mapper-locations: classpath*:mapper/**/*.xml
  configuration:
    map-underscore-to-camel-case: true
```

启动命令的常见坑：

**③ 错误命令（在聚合父工程根目录直接执行）**

```bash
mvn spring-boot:run
```

执行后会报"Unable to find a suitable main class"或"No plugin found for prefix 'spring-boot'"——因为 `spring-boot-maven-plugin` 只绑定在 `hify-app` 子模块上，父工程没有可执行入口。

**④ 正确命令（指定模块）**

```bash
mvn spring-boot:run -pl hify-app
```

`-pl hify-app`（projects list）限定 Maven 只在该子模块上执行 `spring-boot:run` 目标，触发启动类的 `main` 方法。

### 10.4 第三步：健康检查接口 + 验收

```text
在 hify-app 中创建 HealthController，
路径 GET /api/v1/health，返回 Result.ok("Hify is running")。
```

验收命令：

**① 启动应用**

```bash
mvn spring-boot:run -pl hify-app
```

**② 触发健康检查**

```bash
curl http://localhost:8080/api/v1/health
```

期望输出：

```json
{"code": 200, "message": "success", "data": "Hify is running"}
```

验收意义：

**③ 极简接口验证完整闭环**

`code: 200` 说明后端统一响应规范（`Result<T>`）生效，与方法论章节"规范迭代 / SDD 闭环"产出的契约一致；`data` 字段携带字符串说明 Web MVC 路由、JSON 序列化、统一响应包装三条链路全通。该接口本身极简（一行 `Result.ok`），却验证了"启动 → Bean 注入 → 路由 → 响应"完整闭环，是工程搭建阶段最高性价比的验收锚点。后续前端联通（场景四）将直接复用此接口。

## 11. 场景四：前端工程搭建与前后端联通

<img src="imgs/aicent-12-eng-setup-recap/480b20a7b6a19a734fbe46386bfcd37b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 11.1 目标

本场景的工程目标分三段：

- Vue 工程搭好，开发服务器可启动。
- axios 统一请求层封装完成，内置响应拦截与错误提示。
- 调用健康检查接口，页面展示绿色"后端已连接"提示，前后端联通闭环成立。

验收锚点如下：

| 阶段 | 验证动作 | 期望结果 |
|------|---------|---------|
| Vue 骨架就绪 | `npm run dev` | Vite dev server 启动，浏览器可访问 |
| axios 封装完成 | 在任意页面调用 `get('/v1/health')` | 返回解包后的 `"Hify is running"` |
| 前后端联通 | 打开 `/providers` 页面 | 绿色"后端已连接"提示出现 |

### 11.2 第一步：Vue 工程骨架

```text
初始化 Hify 前端项目 hify-web。
技术栈：Vue 3 + TypeScript + Vite + Element Plus。
Vite 代理：/api 请求转发到 localhost:8080。
目录结构按 CLAUDE.md 中定义的前端结构来。
```

技术栈清单：

| 技术 | 角色 | 选型理由 |
|------|------|---------|
| Vue 3 | 视图框架 | Composition API，类型友好 |
| TypeScript | 类型系统 | 与后端 DTO 对齐，编译期防错 |
| Vite | 构建 / 开发服务器 | 启动毫秒级，HMR 即时生效 |
| Element Plus | UI 组件库 | 后台管理场景生态成熟 |

Vite 代理配置（`vite.config.ts`）：

```typescript
export default defineConfig({
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
});
```

为什么开发阶段不需要 Nginx：

**① 跨域问题的来源**

浏览器同源策略下，前端 `localhost:5173` 直接请求后端 `localhost:8080` 会触发 CORS；引入 Nginx 做反向代理是生产做法，本地开发额外维护一份配置成本高。

**② Vite 代理在 dev server 层转发**

Vite 内置 `server.proxy` 在 dev server 层做请求转发：浏览器看到的所有请求都打到 `localhost:5173/api/...`，由 Vite 转发到 `localhost:8080`，从根本上规避跨域。

**③ 一份配置覆盖所有请求**

这样一份代理配置同时覆盖开发期所有 `/api` 请求，无需每个接口单独处理，也不需要在后端配置 CORS。

目录结构 review：

**④ 逐一核对路径是否齐全**

生成后逐一核对以下路径是否齐全：`src/api`、`src/views`、`src/components`、`src/utils`、`src/composables`。职责分层如下：

| 路径 | 职责 |
|------|------|
| `src/api` | 按业务模块组织接口调用，调用 `utils/request.ts` |
| `src/views` | 页面级组件，与路由一一对应 |
| `src/components` | 跨页面复用的展示型 / 容器型组件 |
| `src/utils` | 工具函数与基础设施（如 axios 封装） |
| `src/composables` | Composition API 钩子（请求状态、确认弹窗等） |

### 11.3 第二步：axios 统一请求层

```text
在 hify-web/src/utils/ 下创建 request.ts，封装 axios 实例。
baseURL 设为 /api。
响应拦截器：code === 200 直接返回 data 字段（自动解包），
非 200 用 ElMessage.error 提示 message 并 reject。
导出 get、post、put、del 四个方法。
```

`request.ts` 核心结构：

```typescript
import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';
import { ElMessage } from 'element-plus';

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

service.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data;
    if (code === 200) {
      return data;            // 自动解包，调用方拿到的就是业务数据
    }
    ElMessage.error(message);
    return Promise.reject(new Error(message));
  },
  (error) => {
    ElMessage.error(error.message || '网络异常');
    return Promise.reject(error);
  },
);

export const get = <T>(url: string, config?: AxiosRequestConfig) =>
  service.get<unknown, T>(url, config);
export const post = <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
  service.post<unknown, T>(url, data, config);
export const put = <T>(url: string, data?: unknown, config?: AxiosRequestConfig) =>
  service.put<unknown, T>(url, data, config);
export const del = <T>(url: string, config?: AxiosRequestConfig) =>
  service.delete<unknown, T>(url, config);
```

自动解包的价值：

**① 业务代码不再每次写 `.data.data`**

拦截器在 `code === 200` 分支直接 `return data`，调用方拿到的就是业务载荷。对应关系清晰：

| 后端 `Result<T>` 字段 | 前端处理 |
|----------------------|---------|
| `code === 200` | 拦截器 `return data`，业务层正常 `await` |
| `code !== 200` | `ElMessage.error(message)` + `reject`，业务层走 catch |
| HTTP 层异常 | 兜底 `ElMessage.error` + `reject` |

**② 前后端规范对齐的直接体现**

一份 `CLAUDE.md` 同时约束后端 `Result<T>` 结构与前端拦截器解包逻辑，两端共同遵守同一契约，避免后端改字段、前端忘记同步带来的隐性 bug。

### 11.4 第三步：路由、页面空壳、前后端联通

```text
配置 Vue Router，创建三个路由和对应空壳页面：
模型管理（/providers）、Agent 管理（/agents）、对话（/chat）。
App.vue：左侧 Element Plus 菜单栏，右侧 router-view。
然后改造 ProviderList.vue 调健康检查接口，展示联通效果。
```

路由表：

| 路径 | 页面组件 | 业务含义 |
|------|---------|---------|
| `/providers` | `ProviderList.vue` | 模型提供商管理 |
| `/agents` | `AgentList.vue` | Agent 管理列表 |
| `/chat` | `ChatView.vue` | 对话页 |

联通演示（`ProviderList.vue` 调健康检查）：

**① 在 `src/api/health.ts` 中调用封装方法**

```typescript
import { get } from '@/utils/request';
export const checkHealth = () => get<string>('/v1/health');
```

`ProviderList.vue` 在 `onMounted` 中调用并通过 `ElMessage` 展示结果——成功时绿色"后端已连接"，失败时红色提示。借助 `request.ts` 的自动解包，组件代码只需 `const msg = await checkHealth();`，无需处理 `.data`。

验收闭环：

**② 串起前后端联通验证**

启动后端：`mvn spring-boot:run -pl hify-app`，确认 `localhost:8080` 监听；启动前端：`npm run dev`，打开 `localhost:5173/providers`；页面挂载后自动调用 `/api/v1/health`，经 Vite 代理转发到后端，返回 `"Hify is running"`；页面右上角出现绿色"后端已连接"提示，标志前后端联通闭环成立。该闭环同时验证了：Vite 代理配置正确、axios 拦截器解包逻辑正确、后端统一响应规范（`Result<T>`）与前端口径一致。

## 12. 场景五：UI 设计与基础组件封装

<img src="imgs/aicent-12-eng-setup-recap/ed7fd588cc710b25759324fe6756a150_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 12.1 目标

经过场景四，前端工程骨架已经能跑通健康检查接口，但页面还是 Element Plus 默认样式——灰底、蓝按钮、千篇一律。这一章完成两件事，让 Hify 前端从「能跑」升级到「能用、好看」：

**① 用一条设计系统指令改造外观**

把 Element Plus 的默认外观改造成有品牌感的科技风界面（深色侧边栏 + 蓝紫主色 + 渐变品牌名）。

**② 用一条指令批量生成前端基础组件**

生成 `HifyTable` / `HifyFormDialog` / `useConfirm` / `useRequest` / `notify`，让后续业务页面不用在每个页面重复写分页、弹窗、确认、请求、通知逻辑。

技术栈固定为 Vue 3 + TypeScript + Vite + Element Plus，全程使用 Composition API 和 CSS 变量驱动样式。

### 12.2 第一步：设计系统生成

UI 设计的关键不是「懂配色」，而是「能把约束说清楚」。这一步用一条结构化指令，让 Claude Code 产出整套 CSS 变量设计系统。

指令示例（产品定位 → 用户群体 → 风格方向 → 具体参考，四段递进）：

```text
Hify 是一个 AI Agent 开发平台，面向技术团队内部使用，用户是开发者。

管理后台为主——大量表格、表单、配置页，加一个对话页。

风格：浅底 + 科技感点缀。主色蓝紫系，辅色青色，
侧边栏深色底，按钮和关键元素用亮色。

参考 Linear、Supabase 的视觉风格——干净但不无聊。

帮我设计一套 CSS 变量设计系统：主色/背景色阶/文字色阶/圆角/阴影/过渡动效。
```

指令结构拆解：

| 段落 | 作用 | 示例内容 |
|------|------|---------|
| 产品定位 | 锁定界面类型（管理后台 vs C 端营销页） | AI Agent 开发平台 |
| 用户群体 | 决定信息密度和交互复杂度 | 开发者，容忍高密度表格 |
| 风格方向 | 给出主色、辅色、明暗分布 | 蓝紫主色 + 青色辅色 + 浅底深侧边栏 |
| 具体参考 | 让 AI 对齐已知产品的视觉调性 | Linear、Supabase |
| 产出物清单 | 明确要 CSS 变量而非成品样式表 | 主色/色阶/圆角/阴影/动效 |

拿到 Claude Code 返回的 CSS 变量后，快速 review 三个维度：颜色搭配是否协调（主色 + 辅色 + 中性色三组）、对比度是否达标（文字色与背景色对比度满足 WCAG AA）、整体调性是否贴合「干净但不无聊」。review 通过后，把变量写入工程文件：

```text
src/styles/variables.css
```

Claude Code 会把变量按主色（`--hify-primary` 及色阶）、背景色阶（`--hify-bg-base` / `--hify-bg-elevated`）、文字色阶（`--hify-text-primary` / `--hify-text-secondary`）、圆角（`--hify-radius-sm/md/lg`）、阴影（`--hify-shadow-card`）、过渡动效（`--hify-transition-fast`）分组定义，后续所有组件都引用这套变量，改一处即全局生效。

### 12.3 第二步：侧边栏改造演示

把设计系统变量应用到 App.vue 的侧边栏，前后对比如下：

| 维度 | 改造前（Element Plus 默认） | 改造后（应用设计系统） |
|------|--------------------------|---------------------|
| 侧边栏底色 | 白底浅灰边框 | 深色底（`--hify-sidebar-bg`） |
| 内容区底色 | 白色 | 浅底（`--hify-bg-base`） |
| 选中态 | 蓝色文字 | 左侧 3px 蓝紫竖线 + 浅色高亮 |
| 品牌名 | 普通蓝色文字 | 蓝紫渐变文字效果 |

改造后三个核心设计亮点：

| 设计亮点 | 实现要点 | 视觉效果 |
|---------|---------|---------|
| 深浅层次感 | 侧边栏深色 + 内容区浅色，形成明暗分区 | 后台主次分明，信息层级清晰 |
| 选中态竖线 | 菜单项选中时左侧加 3px 蓝紫竖线（`--hify-primary` 渐变） | 选中状态一目了然，不靠强背景色干扰 |
| 品牌名渐变 | Hify 标题用 background-clip + 渐变色实现文字渐变 | 强化品牌识别度，提升科技感 |

侧边栏改造的关键约束（写入 code block 供 Claude Code 执行）：

```text
侧边栏背景用 --hify-sidebar-bg 深色，菜单项文字默认浅灰，hover 时用 --hify-primary。
选中态的菜单项左侧加一条 3px 的 --hify-primary 渐变竖线，背景轻微高亮。
Hify 品牌名用蓝紫渐变文字效果，参考 Linear 的 logo 风格。
```

### 12.4 第三步：前端基础组件一条指令生成

业务页面高频重复的逻辑有三类：列表分页、表单弹窗、删除确认 + 请求管理 + 通知提示。这一步用一条指令把五个公共组件全部生成出来。

指令示例（完整五组件清单 + 每个组件的 API 契约）：

```text
在 hify-web 中创建以下前端公共组件（src/components/）：

1. HifyTable.vue：通用列表表格，props 接收 columns 配置和 api 方法，
内部管理 loading 和分页，暴露 refresh() 方法。

2. HifyFormDialog.vue：通用表单弹窗，v-model 控制显示，
open(data?) 方法区分新增/编辑模式，提交触发 submit 事件。

3. useConfirm.ts：删除确认 composable，接收确认文案和 API 方法，
一行代码完成确认 → 调接口 → 成功提示。

4. useRequest.ts：请求状态管理，返回 { data, loading, error, execute }。

5. notify.ts：统一通知封装，notifySuccess/notifyError/notifyWarning。

所有组件 Vue 3 Composition API + TypeScript，泛型支持不同数据类型。
```

生成后的五个组件及其职责：

| 组件 | 类型 | 职责 | 关键 API |
|------|------|------|---------|
| `HifyTable.vue` | 组件 | 通用列表表格，封装分页和 loading | `props: { columns, api }`，暴露 `refresh()` |
| `HifyFormDialog.vue` | 组件 | 通用表单弹窗，区分新增/编辑 | `v-model`，`open(data?)`，emit `submit` |
| `useConfirm.ts` | Composable | 删除确认 → 调接口 → 提示 | `useConfirm(text, apiMethod)` |
| `useRequest.ts` | Composable | 请求状态管理 | 返回 `{ data, loading, error, execute }` |
| `notify.ts` | 工具函数 | 统一通知封装 | `notifySuccess/notifyError/notifyWarning` |

重点看 `HifyTable` 的 API 设计。传统写法每个列表页面都要重复写 loading 状态、分页参数、分页器事件、接口调用、错误处理；`HifyTable` 把这些全部内化，业务页面只需要声明 columns 配置和传入 api 方法：

```text
<HifyTable :columns="columns" :api="fetchProviders" />
```

组件内部自动管理 loading、当前页、每页条数，并暴露 `refresh()` 方法供外部刷新（如删除成功后调用）。`useConfirm` 把删除三连（确认框 → 调接口 → 成功提示）压成一行：

```text
useConfirm('确定删除该提供商吗？', deleteProvider)(row.id)
```

为什么一条指令生成五个组件，而不是逐个生成：

**① 后端工程师的关注点不在前端组件细节**

AI 做初稿、工程师只负责验收「好不好用」这种宏观判断。一次性生成完整的组件体系，工程师可以在统一上下文里 review 五个组件的协作关系（`HifyTable` 触发删除 → `useConfirm` 确认 → `notify` 提示 → `refresh` 刷新），比分五次生成更高效，也更能看出整体设计的一致性。

### 12.5 第四步：UI 打磨三轮调校

基础组件生成后，用 ProviderList mock 页面验证整体效果，然后做三轮细节打磨。三轮都遵循「位置/组件 + 哪里不对 + 应该怎样」的句式，不需要知道 CSS 属性名。

第一轮（间距与行高）：

```text
ProviderList 的表格行高太高，改成 52px。

操作列的编辑和删除按钮间距太小，加 8px margin-left。
```

第二轮（颜色语义纠错——禁用是正常状态，不是错误）：

```text
状态列的禁用标签用灰色，不要用红色。

禁用是正常状态，不是错误，不应该用 danger 色。
```

这一轮的关键是「颜色语义」：<span style="color: red; font-weight: bold;">Element Plus 默认会把禁用态渲染成 danger 红色，但在后台管理场景下，「禁用」是正常业务状态（如某个提供商暂时下线），用红色会让用户误以为是异常</span>。把语义判断说清楚，Claude Code 就能把禁用态改成中性灰色（`--hify-text-secondary`）。

第三轮（整体收尾，一次提多条）：

```text
整体看一下 ProviderList：
1. 分页器居右对齐，上方加细分割线
2. 新增按钮加 Plus 图标
3. 操作列改成 text 类型按钮：编辑蓝色、删除红色
```

三轮之后的页面变化：

| 维度 | 第一轮前 | 三轮后 |
|------|---------|--------|
| 表格行高 | 默认偏高（约 60px+） | 52px，信息密度提升 |
| 操作列按钮 | 默认按钮样式，挤在一起 | text 类型，间距 8px，编辑蓝/删除红 |
| 禁用标签 | danger 红色 | 中性灰色 |
| 分页器 | 默认居中或居左 | 居右对齐 + 上方分割线 |
| 新增按钮 | 纯文字 | 带 Plus 图标 |

三轮打磨体现的核心方法论：工程师不需要会写 CSS，但需要具备判断力——能看出来哪里不对，能说清楚应该怎样。Claude Code 负责把判断翻译成代码。

## 13. 回顾与产出清单

<img src="imgs/aicent-12-eng-setup-recap/6dff88b9d7c3a1f53eb1f9df7de65164_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 13.1 本篇产出物

到本章结束，Hify 工程搭建篇的全部产出已就绪。产出物清单如下：

| 圈号 | 产出物 | 状态 | 说明 |
|------|--------|------|------|
| ① | 前后端都能跑的工程骨架 | 就绪 | 后端 Spring Boot 多模块 + 前端 Vue 工程，健康检查联通 |
| ② | 三档就绪的后端基础组件 | 就绪 | `Result`/`PageResult`、`ErrorCode`/`BizException`、`GlobalExceptionHandler` + MyBatis-Plus/Redis 配置 |
| ③ | 有品牌感的前端界面 | 就绪 | CSS 变量设计系统 + 深色侧边栏 + 渐变品牌名 |
| ④ | 一套可复用的前端公共组件 | 就绪 | `HifyTable` / `HifyFormDialog` / `useConfirm` / `useRequest` / `notify` |

每个产出物都对应一个可验证的验收锚点：后端 `mvn compile` 通过 + `curl /api/v1/health` 返回 200；前端页面能看到深色侧边栏、渐变品牌名、调通健康检查接口；公共组件在 ProviderList mock 页面跑通「列表 → 弹窗 → 删除 → 刷新」全链路。

### 13.2 工程搭建系列回顾

本系列（工程搭建篇）从零到能跑，走完了完整链路：

**① 后端 Maven 骨架**

父 pom + 子模块 + `dependencyManagement` 版本统一，`mvn compile` 通过。

**② 公共基础设施（hify-common）**

统一响应、错误码、全局异常处理、MyBatis-Plus + Redis 配置——后端基础组件三档全部就绪。

**③ 前端 Vue 工程**

Vue 3 + TypeScript + Vite + Element Plus，axios 统一请求层自动解包，前后端通过健康检查联通。

**④ UI 设计与基础组件**

CSS 变量设计系统 + 深色侧边栏 + 五个前端公共组件一条指令生成，三轮打磨完成视觉调校。

本系列前几篇（场景一至场景五）展示的核心方法论——认知对齐、任务拆解到「一次能验证」、模板化指令与边界约束、给完整上下文修报错、SDD 闭环迭代、UI 协作句式——在实战中反复被验证有效。到这里，工程搭建篇完整结束。下一阶段正式进入业务功能开发。

### 13.3 下一篇预告

下一篇开始进入业务功能开发系列，第一篇是模型提供商管理——包括提供商的增删改查、密钥管理、连通性测试。届时将复用本章生成的前端公共组件（`HifyTable` + `HifyFormDialog` + `useConfirm`），并演示如何把后端 `Result`/`PageResult` 接口规范与前端 axios 自动解包串联起来，跑通一个完整的业务闭环。
