---
title: 传统项目迁AI 16：构建护栏 - 流程回顾
author: fangkun119
date: 2026-07-04 16:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-16-safeguard-04-process-recap/cover.jpg
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
aicmigr-16-safeguard-04-process-recap
传统项目迁AI 16：构建护栏 - 流程回顾
-->

## 1. 

<img src="imgs/aicmigr-16-safeguard-04-process-recap/45819c50d197d0c722b3b5d8c6f6b32c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本篇是"构建护栏"部分的收尾，把环境搭建、测试摸底、补测试、CI 集成的所有动作在 Spring AI Alibaba Admin 上串起来跑一遍。改写后分为两部分：第一部分是方法论提炼（速查手册 + Check List），第二部分是实战演示（含全部提示词与产出物）。

<img src="imgs/aicmigr-16-safeguard-04-process-recap/731dcb8140fa4073e1cb02355be73924_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    START["改造前护栏建立流程"] --\> PART1["第一部分 方法论提炼<br>（速查 / Check List）"]
    START --\> PART2["第二部分 实战演示<br>（含提示词与产出物）"]

    PART1 --\> M1["1. 流程总览与产出物对照"]
    PART1 --\> M2["2. 自主修复原则与 3 次兜底"]
    PART1 --\> M3["3. 测试断言凭实际不凭应该"]
    PART1 --\> M4["4. 关键约束清单"]
    PART1 --\> M5["5. 改造前护栏 Check List"]

    PART2 --\> S0["6. 实战准备"]
    PART2 --\> S1["7. 场景一：让 AI 当环境工程师"]
    PART2 --\> S2["8. 场景二：摸清测试现状"]
    PART2 --\> S3["9. 场景三：补出兜底测试"]
    PART2 --\> S4["10. 场景四：让 CI 当兜底护栏"]
    PART2 --\> S5["11. 一键自主执行模式"]
    PART2 --\> S6["12. 跑完之后的目录与资产"]

    M5 -. 项目阶段查阅 .-> S0
    S4 --\> END["护栏到位"]
    END --\> SUMMARY["13. 小结与思考"]
-->

读者路径建议：

| 读者类型 | 推荐路径 |
|---------|---------|
| 初学 AI 编程工程师 | 顺读全文，重点看第二部分各场景的提示词与 review 重点 |
| 熟练 AI 编程工程师 | 先读第一部分速查 + Check List，按需跳到第二部分查阅具体提示词 |

## 2. 第一部分 方法论提炼

### 2.1 流程总览与四场景产出物对照

<img src="imgs/aicmigr-16-safeguard-04-process-recap/c9a1c6b531849ea2aadb6e9426b3930b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

构建改造前护栏是一个串行流程：环境活了 → 测试摸清 → 兜底测试补上 → CI 护栏到位。每个场景对应一个独立目标，缺一不可。

#### (1) 四场景流程关系

<img src="imgs/aicmigr-16-safeguard-04-process-recap/6c657fd54b009879e38abbd56abaea87_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart LR
    A["场景一<br>环境工程师"] --\>|项目活了| B["场景二<br>测试摸底"]
    B --\>|知道补什么| C["场景三<br>补兜底测试"]
    C --\>|P0 全绿| D["场景四<br>CI 护栏"]
    D --\>|push 自动跑| E["护栏到位"]
-->

#### (2) 四场景产出物对照表

| 场景 | 对应章节 | 核心目标 | 关键产出文件 |
|------|---------|---------|------------|
| 场景一 环境工程师 | 第 13 篇 | 项目活了 + 接口冒烟通过 | docs/env-checklist.md、scripts/install-deps.sh、scripts/deps-start.sh / deps-stop.sh / deps-status.sh、docker-compose.dev.yml、docs/startup-log.md、docs/smoke-test-result.md |
| 场景二 测试摸底 | 第 14 篇 | 摸清核心链路、现有覆盖、缺口清单 | docs/critical-paths.md、docs/test-status.md、docs/test-gaps.md |
| 场景三 补兜底测试 | 第 15 篇前半 | P0 缺口全部补上并跑通 | docs/test-plan.md、src/test/ 下新增 P0 测试 |
| 场景四 CI 护栏 | 第 15 篇后半 | push 触发自动跑测试、失败 block merge | .github/workflows/test.yml（或对应平台配置） |

#### (3) 跑完护栏后的目录结构

```
spring-ai-alibaba-admin/
├── CLAUDE.md
├── .claude/skills/
│   ├── docs-auto-sync/
│   │   └── SKILL.md
│   └── env-bootstrap/  ← 场景一挖到的新 skill
│       └── SKILL.md
├── .github/workflows/
│   └── test.yml  ← CI 护栏
├── scripts/
│   ├── install-deps.sh
│   ├── install-log.md
│   ├── deps-start.sh
│   ├── deps-stop.sh
│   └── deps-status.sh
├── docker-compose.dev.yml  ← Docker 备选
├── docs/
│   ├── architecture.svg
│   ├── module-deps.svg
│   ├── external-deps.svg
│   ├── api-list.md
│   ├── data-model.md
│   ├── data-model-er.svg
│   ├── env-checklist.md
│   ├── startup-log.md
│   ├── smoke-test-result.md
│   ├── setup-guide.md
│   ├── critical-paths.md
│   ├── test-status.md
│   ├── test-gaps.md
│   └── test-plan.md
└── src/test/  ← P0 测试已补
```

这就是一个老项目的完整 AI 协作基础设施 + 改造前护栏。每次 push 触发 CI，每天上班 deps-start，docs/ 里每份资产都对应一类共识，CLAUDE.md 是 AI 的常识门面，两个 SKILL 守着重复流程的自动化。

### 2.2 自主修复原则与 3 次兜底

<img src="imgs/aicmigr-16-safeguard-04-process-recap/429a40f212bc8df88650e87b0415c507_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

让 AI 自主跑安装、启动、CI 这类容易踩坑的步骤时，必须把"自主修复"的边界明确写死，否则 AI 要么每步都问、要么陷入死循环。

#### (1) 核心原则

| 原则 | 含义 |
|------|------|
| 先看报错自己判断 | 任何一步失败，AI 先读报错、自己分析原因、自己修、修完重试 |
| 不每个错误都问 | 不要把每个错误都抛给用户 |
| 3 次兜底停 | 同一个错误连续修 3 次还不行，停下来汇报具体卡在哪 |

#### (2) 适用范围

自主修复原则适用于整个改造前护栏建立流程中的所有执行性步骤：

- 安装脚本（scripts/install-deps.sh）的执行
- 应用启动（mvn clean package + 启动）
- CI 第一次跑通（push 后 debug）

#### (3) 反模式

| 反模式 | 后果 | 正确做法 |
|--------|------|---------|
| AI 遇到错误立刻停下问用户 | 用户沦为人工错误处理器 | AI 自己 debug，3 次同错才停 |
| AI 不设兜底无限重试 | 烧 token、卡住整个流程 | 连续 3 次同一错误必须停 |
| 用户没把"3 次"写进提示词 | AI 默认行为不可控 | 一键流程提示词里硬编码"连续 3 次同一错误" |

### 2.3 测试断言凭"实际行为"不凭"应该"

<img src="imgs/aicmigr-16-safeguard-04-process-recap/dfdb804bd8f27cda9b9f198a8817677d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这是补测试环节最值钱的洞察，AI 一不留神就会踩坑导致测试无效。

#### (1) 核心定义

| 类型 | 断言来源 | 适用场景 |
|------|---------|---------|
| Characterization Test（特征化测试） | 凭"现在实际做什么"写断言 | 改造前的存量代码、行为还不确定 |
| 普通测试 | 凭"应该做什么"写断言 | 全新需求、行为已明确 |

#### (2) 为什么必须凭实际

改造前的老代码，业务直觉和实际行为经常对不上。如果按"应该是什么"补断言：

- AI 容易按业务文档或常识补断言
- 实际行为可能与业务文档不一致
- 测试看起来绿了，实际没在保护任何东西
- 改造时一旦触动，测试反而成为阻力

#### (3) Characterization Test 的正确写法

| 步骤 | 动作 |
|------|------|
| Step 1 | 先跑一次现有代码，记录实际行为 |
| Step 2 | 把实际行为转成断言 |
| Step 3 | 不凭"应该是什么"写断言，凭"实际是什么"写 |

#### (4) 一键流程中的硬性约束

在让 Claude Code 自主跑完整流程的提示词中，"测试断言凭实际不凭应该"这句话必须单独列出来。AI 默认会按"业务直觉"补断言导致测试无效，必须用硬约束压住。

### 2.4 关键约束清单

<img src="imgs/aicmigr-16-safeguard-04-process-recap/568e870df204645e79d2830bb9f054ae_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

下表汇总了贯穿四个场景的硬性约束。这些约束在第 13-15 篇反复强调，一键流程提示词中必须全部硬编码进去，否则 AI 默认会"贪快"违反。

#### (1) 数量与分级约束

| 约束 | 数值 | 出现场景 |
|------|------|---------|
| 核心链路总数 | 不超过 8 条，宁少勿多 | 场景二 摸核心链路 |
| 测试缺口总数 | 不超过 20 项 | 场景二 算缺口清单 |
| P0 缺口数量 | 5-10 个 | 场景二 算缺口清单 |
| P1 缺口数量 | 不超过 10 个 | 场景二 算缺口清单 |
| 每批补测试 | 严格 1-3 个，最好 1 个 | 场景三 补测试计划 |
| 冒烟接口数 | 5 个最核心接口 | 场景一 接口冒烟 |

#### (2) 原则性约束

| 约束 | 反模式 |
|------|--------|
| 只列在核心链路上的缺口 | 把不在主链路上的也列进来凑数 |
| 不追求覆盖率指标 | 追求 JaCoCo 那种百分比 |
| 追求"关键路径有兜底" | 追求"覆盖率数字好看" |
| 简单 CRUD 不进补测试计划 | 把 CRUD 也按 1-3 个一批补 |
| 测试健康度算上跳过的 | 只算失败的 |
| 失败分类要靠谱（代码 bug / 测试本身坏 / 环境） | 笼统说"测试失败" |

#### (3) 顺序约束

补测试的批次顺序按价值优先级排：

```mermaid
flowchart LR
    A["改造路径上的<br>Characterization Test"] --\> B["核心链路<br>集成测试"]
    B --\> C["复杂逻辑<br>单元测试"]
```

简单 CRUD 永远不进计划。

### 2.5 改造前护栏 Check List

<img src="imgs/aicmigr-16-safeguard-04-process-recap/040fbcb0a59771f34088d134b5438ebb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-16-safeguard-04-process-recap/5e107b18cb4056837a2e5e4d63bce4d2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

本节是一份可裁剪的 Check List，供项目阶段快速查阅。每个 Check 项都对应第二部分的具体提示词编号。

#### (1) 场景一 环境工程师 Check List

##### ① 依赖盘点
- [ ] docs/env-checklist.md 已生成
- [ ] 每个依赖列出：名字、版本要求、默认端口、连接信息、初始化要求
- [ ] 与 docs/external-deps.svg 对得上、版本号有依据

##### ② 本地安装方案
- [ ] scripts/install-deps.sh 已生成
- [ ] 每个 middleware 包含"装 + 初始化 + 验证"三步
- [ ] 3 次失败兜底已写入脚本
- [ ] scripts/install-log.md 记录每个 middleware 实际命令与修复过程

##### ③ 依赖启停脚本
- [ ] scripts/deps-start.sh / deps-stop.sh / deps-status.sh 已生成
- [ ] 启动顺序对（Nacos 在 OTel Collector 前、MySQL 在应用前）
- [ ] 启动后等服务就绪再返回
- [ ] status 输出清晰

##### ④ Docker 备选方案
- [ ] docker-compose.dev.yml 已生成（备选，偏好 Docker 的同学用）

##### ⑤ 编译启动
- [ ] mvn clean package 跑通
- [ ] 应用监听端口、管理界面地址已知
- [ ] docs/startup-log.md 已记录
- [ ] 日志没报 ERROR、端口监听正常、管理界面能打开

##### ⑥ 接口冒烟
- [ ] docs/smoke-test-result.md 已生成
- [ ] 5 个核心接口覆盖登录、Prompt、Dataset、Evaluator、Trace 几大模块
- [ ] 返回 200 算通过、错误诚实列出
- [ ] 返回结构与 api-list.md 一致

#### (2) 场景二 测试摸底 Check List

##### ① 摸核心链路
- [ ] docs/critical-paths.md 已生成
- [ ] 总数不超过 8 条
- [ ] 每条写：链路名、起点接口、关键节点、终点状态
- [ ] 是真的核心（登录 / Prompt CRUD / Dataset CRUD / Evaluator 跑批 / 实验执行 / Trace 写入），不是账号详情查询这类

##### ② 摸现有测试
- [ ] docs/test-status.md 已生成
- [ ] 单元 / 集成 / E2E 各多少个文件已统计
- [ ] 哪些 Controller / Service 有测试已标出
- [ ] 不给覆盖率百分比（JaCoCo 干的事）
- [ ] 不列每个测试方法，只关注"哪些核心链路被覆盖"
- [ ] 对照 docs/critical-paths.md 标覆盖度（有 / 部分 / 没有）

##### ③ 跑一遍看实际状态
- [ ] mvn test 已跑完
- [ ] 通过 / 失败 / 跳过各多少已统计
- [ ] 失败分类靠谱（代码 bug / 测试本身坏 / 环境）
- [ ] 跑总耗时已记录
- [ ] 测试健康度已判断（绿 ≥90% / 黄 60-90% / 红 <60%）
- [ ] 不试图修复失败的测试，只汇报状态

##### ④ 算出缺口清单
- [ ] docs/test-gaps.md 已生成
- [ ] 总数不超过 20 项
- [ ] 只列在核心链路上的缺口
- [ ] 每项标 P0 / P1
- [ ] 每项写：场景描述、为什么必须、建议测试类型
- [ ] P0 数量控制在 5-10 个

#### (3) 场景三 补兜底测试 Check List

##### ① 补测试计划
- [ ] docs/test-plan.md 已生成
- [ ] 每批严格 1-3 个，最好 1 个
- [ ] 批次顺序：改造路径 Characterization > 核心链路集成 > 复杂逻辑单元
- [ ] 简单 CRUD 没进计划
- [ ] 每批写：批次号、测试类型、覆盖核心链路、预期工作量

##### ② 一批一批补
- [ ] Characterization Test 凭"实际行为"写断言
- [ ] 集成测试用 SpringBootTest 起完整 context + 真实数据库
- [ ] 每批补完跑 mvn test 确保通过
- [ ] review 通过后才进下一批
- [ ] 所有 P0 批次跑完、mvn test 全绿

#### (4) 场景四 CI 护栏 Check List

##### ① CI 状态分析
- [ ] 已扫 .github/workflows/、.gitlab-ci.yml、Jenkinsfile
- [ ] 现有 CI 跑了什么、什么时候触发、有没有跑测试 已知
- [ ] 没有 CI 时已建议用哪种

##### ② 写完整 CI workflow
- [ ] .github/workflows/test.yml（或对应平台）已生成
- [ ] 触发条件：push 到任何分支 + 提 PR 时
- [ ] JDK 版本对齐 pom.xml 里 java.version
- [ ] 中间件配置完整（参考 docker-compose.dev.yml）
- [ ] 跑 mvn clean test，失败 block merge
- [ ] 测试报告输出到 CI artifact 区
- [ ] Maven 依赖 cache 已加

##### ③ 跑通 CI
- [ ] push 一次触发 CI
- [ ] 失败自己 debug 自己修（3 次同错才停）
- [ ] CI 第一次绿色构建
- [ ] 测试报告能下载
- [ ] 跑的时长合理（10 分钟内）

## 3. 第二部分 实战演示

### 3.1 实战准备

<img src="imgs/aicmigr-16-safeguard-04-process-recap/02ba8661b27f92356c5300d6a12f66cd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第 13 篇里读者已经做过准备工作。如果环境还在，跳过本节。如果是新机器或者重置过：

```bash
cd spring-ai-alibaba/spring-ai-alibaba-admin
```

确认 docs/ 里第 12 篇跑出的资产都在（architecture.svg、api-list.md、data-model.md 等）。本篇所有动作都依赖这些资产。

在项目根目录启动 Claude Code，后面所有提示词都在这里跑。

### 3.2 场景一：让 AI 当你的环境工程师

<img src="imgs/aicmigr-16-safeguard-04-process-recap/b499e2b40bdd71579bd705491bde695a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应第 13 篇。产出依赖清单 + 安装脚本 + 启停脚本 + 应用跑起来 + 接口冒烟。

#### (1) 提示词 1：依赖盘点

```
综合看 docs/external-deps.svg、application*.yml、pom.xml、README，
给我列一份这个项目运行需要的完整外部依赖清单。
每个依赖列出：名字、版本要求（精确到主版本）、默认端口、
连接信息、初始化要求（建库、配 Nacos 命名空间等）。
输出用表格总结。保存到 docs/env-checklist.md。
```

产出：docs/env-checklist.md

review 重点：和 docs/external-deps.svg 对得上、版本号有依据、初始化要求要细。

#### (2) 提示词 2A：本地安装方案

```
读 docs/env-checklist.md，给我生成一份本地安装脚本，
保存到 scripts/install-deps.sh。
- 用 brew（macOS）或 apt（Linux）装中间件
- 包含每个中间件的初始化（建库 SQL、Nacos 配置等）

生成完直接执行这个脚本。执行过程遵循自主修复原则：
任何一步失败先看报错、自己判断原因、自己修、修完重试。
不要每个错误都问我。同一个错误连续修 3 次还不行，停下来汇报具体卡在哪。

最终输出 scripts/install-log.md，
记录每个中间件最终用了什么命令装上、过程中遇到什么问题、怎么修的。
```

产出：scripts/install-deps.sh + scripts/install-log.md

review 重点：脚本对每个中间件包含"装 + 初始化 + 验证"三步、3 次失败兜底有效防止死循环。

#### (3) 提示词 2B：依赖启停脚本

```
基于 Step 2A 装好的中间件，生成三个脚本到 scripts/ 下：
- deps-start.sh：一键启动所有依赖中间件
- deps-stop.sh：一键停止所有依赖中间件
- deps-status.sh：查看每个中间件的运行状态

考虑混合场景：有的用 brew services 管，有的是手动 jar，
有的是 systemd。脚本要能处理这几种。
启动后等服务就绪再返回，不要"启动了但还没 ready"。
```

产出：scripts/deps-start.sh / deps-stop.sh / deps-status.sh

review 重点：启动顺序对（Nacos 在 OTel Collector 前、MySQL 在应用前）、status 输出清晰。

#### (4) 提示词 2C：Docker 备选方案

```
顺手给一份 docker-compose.dev.yml，把所有依赖打包成 docker。
偏好 Docker 的同学可以用这个替代 Step 2A 和 2B。
版本号、端口、初始化脚本要齐全。保存到项目根目录。
```

产出：docker-compose.dev.yml（备选）

#### (5) 提示词 3：编译启动

```
中间件已经起来了（用 ./scripts/deps-status.sh 确认）。
现在帮我跑 mvn clean package + 启动应用。

启动过程遵循自主修复原则（参照 install 脚本的兜底机制：
连续 3 次同一错误才停下来汇报）。

启动成功后告诉我应用监听的端口、管理界面地址。
失败和修复的过程记到 docs/startup-log.md。
```

产出：项目跑起来 + docs/startup-log.md

review 重点：日志没报 ERROR、端口监听正常、管理界面能打开。

#### (6) 提示词 4：接口冒烟

```
读 docs/api-list.md，挑 5 个最核心的接口（覆盖登录、Prompt、
Dataset、Evaluator、Trace 几大模块），用 curl 跑一遍。
返回 200 算通过，返回错误的列出来。
输出用表格总结，保存到 docs/smoke-test-result.md。
```

产出：docs/smoke-test-result.md

review 重点：选的接口真的核心、返回结构和 api-list.md 一致、错误的诚实列出来。

跑完场景一，项目活了。

### 3.3 场景二：摸清测试现状

<img src="imgs/aicmigr-16-safeguard-04-process-recap/8b48efb4f339f724ab3ae95252ef9b6c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应第 14 篇。产出核心链路、现有测试状态、缺口清单。

#### (1) 提示词 5：摸核心链路

```
基于 docs/api-list.md、docs/data-model.md、CLAUDE.md，给我列出
这个项目最值得测的核心链路。要求：
- 总数不超过 8 条，宁少勿多
- 必须是"改造时容易出问题"的链路，不是所有链路
- 每条写：链路名、起点（哪个接口）、关键节点、终点（什么状态）

输出用表格总结。保存到 docs/critical-paths.md。
```

产出：docs/critical-paths.md

review 重点：是不是真的核心（登录 / Prompt CRUD / Dataset CRUD / Evaluator 跑批 / 实验执行 / Trace 写入这种是；账号详情查询不是）。

#### (2) 提示词 6：摸现有测试

```
扫一下项目里所有的测试目录（src/test、tests/、e2e/ 等），
统计现有测试情况。要求：
- 单元测试 / 集成测试 / E2E 各多少个文件
- 哪些 Controller 有对应的测试，哪些没有
- 哪些核心 Service 有测试，哪些没有
- 不要给覆盖率百分比，那是 JaCoCo 干的事
- 不要列出每个测试方法，只关注"哪些核心链路被覆盖"

对照 docs/critical-paths.md，标出每条核心链路当前的测试覆盖
情况（有 / 部分 / 没有）。输出用表格总结。
保存到 docs/test-status.md。
```

产出：docs/test-status.md

review 重点：按链路验证，不是按文件验证，文件存在不代表链路被覆盖。

#### (3) 提示词 7：跑一遍看实际状态

```
跑一遍 mvn test（或项目的标准测试命令），统计真实结果：
- 通过 / 失败 / 跳过 各多少
- 失败的分类：代码 bug / 测试本身坏了 / 环境问题
- 跑总耗时多少
- 不要试图修复失败的测试，只汇报状态

最后给一个"测试健康度"的判断：绿（90% 通过）/ 黄（60-90%）/红（< 60%）。
输出用表格总结。
追加到 docs/test-status.md 的"实际运行结果"小节。
```

产出：追加到 docs/test-status.md

review 重点：失败分类要靠谱、跳过的也要算进健康度。

#### (4) 提示词 8：算出缺口清单

```
对照 docs/critical-paths.md（应该测什么）和 docs/test-status.md
（现在测了什么），算出测试缺口。

严格遵守以下原则：
- 总数不超过 20 项，宁少勿多
- 只列在核心链路上的缺口，不在主链路上的不要列
- 每项标 P0（改造前必须有）/ P1（有了更好）
- 不要追求覆盖率指标，追求"关键路径有兜底"
- 每项写：场景描述、为什么必须、建议测试类型（集成 / 单元 / Characterization Test）

输出用表格总结。保存到 docs/test-gaps.md。
```

产出：docs/test-gaps.md

review 重点：P0 数量控制在 5-10 个。每个 P0 都对应明确的核心链路，P1 不超过 10 个。

跑完场景二，测试现状摸清，知道改造前必须补哪些测试了。

### 3.4 场景三：补出兜底测试

<img src="imgs/aicmigr-16-safeguard-04-process-recap/360e958b6bb4d4ce420eff31acca0fc4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应第 15 篇。产出补测试计划 + 一批一批补 + 跑通的测试。

#### (1) 提示词 9：让 AI 补出测试计划

```
基于 docs/test-gaps.md，把 P0 缺口拆成多批，每批 1-3 个
（最好 1 个），给我一份补测试计划。每批写：批次号、测试类型
（Characterization Test / 集成测试 / 单元测试）、覆盖的核心
链路、预期工作量。

按"改造路径上的 Characterization > 核心链路集成 > 复杂逻辑
单元"的顺序排批次。简单 CRUD 不进计划。

输出用表格总结。保存到 docs/test-plan.md。
```

产出：docs/test-plan.md

review 重点：每批严格 1-3 个，不能更多、最好 1 个。批次顺序按价值优先级。简单 CRUD 真的没进计划。

#### (2) 提示词 10：让 AI 一批一批补

```
按 docs/test-plan.md 的第 1 批，给项目补出对应的测试。

对 Characterization Test 类型：先跑一次现有代码记录实际行为，
再把行为转成断言。不要凭"应该是什么"写断言，凭"实际是什么"写。

对集成测试类型：需要真实启动应用 + 数据库。
用 SpringBootTest 的方式起完整 context 跑。

补完跑一遍 mvn test 确保都通过。
输出用表格总结每个测试覆盖的场景、预期结果、实际跑出来的状态。
```

产出：第 1 批 1-3 个测试 + 跑通的结果

review 重点（最关键）：

- 测试是不是测了"现在实际做什么"，不是"AI 觉得应该做什么"
- 测试覆盖的场景对不对，有没有忽略 edge case
- 测试都能跑通

review 通过后开第 2 批：

```
按 docs/test-plan.md 的第 2 批补测试，
参考第 1 批已经跑通的测试风格，保持一致。
其他要求同前。
```

按这个节奏直到所有 P0 批次跑完。

跑完场景三，P0 测试缺口全部补上。

### 3.5 场景四：让 CI 当你的兜底护栏

<img src="imgs/aicmigr-16-safeguard-04-process-recap/73fcd51f86de3bf17d5d48c88e70cb73_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

对应第 15 篇后半。产出 CI 配置 + 跑通自动化护栏。

#### (1) 提示词 11：让 AI 分析项目当前的 CI 状态

```
扫一下项目里有没有现成的 CI 配置（看 .github/workflows/、.gitlab-ci.yml、Jenkinsfile 之类）。
如果有，告诉我现在跑了什么、什么时候触发、有没有跑测试。
如果没有，告诉我项目代码托管在哪个平台，建议用哪种 CI。
输出用表格总结。
```

产出：CI 现状分析

#### (2) 提示词 12：让 AI 写完整的 CI workflow

```
基于上一步的分析，给我写一份完整的 CI workflow。要求：
- 触发条件：push 到任何分支 + 提 PR 时
- 运行环境：用项目对应的 JDK 版本（看 pom.xml 里 java.version）
- 启动需要的中间件（参考 docker-compose.dev.yml）
- 跑 mvn clean test，失败就 block merge
- 输出测试报告到 CI artifact 区方便 review
- 加合理的 cache（Maven 依赖缓存）让跑得快一点

输出完整的 .github/workflows/test.yml（或对应平台的配置文件），
我直接 commit 进仓库就能跑。
```

产出：.github/workflows/test.yml 或 .gitlab-ci.yml

review 重点：触发条件对，中间件配置完整，JDK 版本对齐 pom.xml。

#### (3) 提示词 13：跑通 CI

```
push 一次代码触发 CI，看能不能跑通。失败就自己 debug 自己修，
跟 install 脚本一样的自主修复原则（连续 3 次同错才停下来汇报）。
最终跑通后告诉我 CI 跑一次需要多久。
```

产出：CI 第一次绿色构建

review 重点：CI 绿色 + 测试报告能下载 + 跑的时长合理（10 分钟内）。

跑完场景四，改造前的所有护栏都到位了。

### 3.6 一键自主执行模式

<img src="imgs/aicmigr-16-safeguard-04-process-recap/b0f993b149675589f06ac8a86c50100b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面四个场景一个个跑，是为了让读者看清每一步的产出和 review 点。真正上手之后你会希望一次粘贴、Claude Code 自主跑完所有步骤、遇到问题自己修、跑完自己验收。

下面这段提示词就是干这个的。整段粘贴到 Claude Code，去吃个午饭，回来就齐了。

#### (1) 一键流程提示词

```
我刚跑完第二部分，docs/ 里有架构图、模块图、依赖图、接口清单、数据模型五份资产，
根目录有 CLAUDE.md，.claude/skills/ 下有 docs-auto-sync skill。

现在帮我完整跑通改造前的护栏建立流程，
全程自主推进，遇到问题自己修、自己 review、自己决定下一步，
不要每一步都问我。

请按以下顺序执行：

第一步：环境搭建
- 基于 docs/external-deps.svg + application*.yml + pom.xml 生成 docs/env-checklist.md
- 生成本地安装脚本 scripts/install-deps.sh 并执行（遵循自主修复原则：连续 3 次同错才停）
- 生成依赖启停脚本 deps-start.sh / deps-stop.sh / deps-status.sh
- 顺手给一份 docker-compose.dev.yml 备选
- 跑 mvn package + 启动应用，记录 docs/startup-log.md
- 用 curl 跑 5 个核心接口冒烟，记录 docs/smoke-test-result.md

第二步：测试摸底
- 基于已有资产列 8 条核心链路，保存到 docs/critical-paths.md
- 扫现有测试状态，对照核心链路标覆盖度，保存到 docs/test-status.md
- 跑一遍 mvn test 看真实结果，追加到 test-status.md
- 算出测试缺口清单 docs/test-gaps.md，P0 不超过 10 个，P1 不超过 10 个，每项标场景描述和建议类型

第三步：补 P0 测试
- 拆补测试计划 docs/test-plan.md，每批 1-3 个最好 1 个
- 按计划一批一批补，每批跑通了才进下一批
- Characterization Test 必须凭"实际行为"写断言，不凭"应该"
- 所有 P0 批次跑完确认 mvn test 全绿

第四步：CI 集成
- 分析项目当前 CI 状态
- 写一份完整 .github/workflows/test.yml（或对应平台）
- push 触发一次 CI 跑通

自主原则：
- 每一步跑完自己 review 输出质量，不合格自己重跑
- 遇到失败自己 debug 自己修（除非连续 3 次同一错误）
- 测试别贪多，每批严格 1-3 个最好 1 个
- 测试断言凭实际不凭应该
- 所有步骤跑完后，生成一份 summary.md，列出每个产出文件、
  每份资产的主要内容概括、你认为还需要人工确认的地方
  （特别是补的测试是否都凭"实际行为"写的）

不要打断来问我。有判断不清的地方先做一个合理选择，
在 summary 里标记。跑完再汇报。
```

粘贴完等 Claude Code 自己跑。时间大概 1-2 小时（环境搭建快、补测试慢，主要时间花在补测试和等 mvn 编译）。

#### (2) 一键提示词的设计动机

| 设计点 | 动机 |
|--------|------|
| 所有 1-3 个、自主修复、3 次兜底等关键约束都明确写进去 | 这些约束在第 13-15 篇反复强调，跑一键流程时 AI 默认会"贪快"批量补一堆测试，必须把约束写得很硬 |
| "测试断言凭实际不凭应该"单独列出来 | 这是第 15 篇最值钱的洞察，AI 一不留神就会按"业务直觉"补断言导致测试无效 |
| 人工确认点在 summary 里集中暴露 | 让 AI 把"我不确定的地方"都攒到最后，特别是测试断言这种容易踩坑的，标出来让用户重点 review |

## 4. 小结与思考

<img src="imgs/aicmigr-16-safeguard-04-process-recap/4abc283d3be0ac283731d0b89e174a84_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 4.1 本篇小结

构建护栏部分到这里结束。从第 13 篇让 AI 当环境工程师、到第 14 篇摸清测试、到第 15 篇补出兜底测试和 CI 护栏，整个"改造前准备"的方法论全部跑完。

了解项目部分加构建护栏部分加起来一句话：理解了项目（脑图）、跑通了项目（环境）、护住了项目（测试 + CI）。这三件事做完，才有资格谈改造。

下一篇开始第四部分，终于要动手做真实需求改造了。会从一个模糊的业务需求出发，让 AI 帮你拆出一份能直接指导开发的技术文档。

### 4.2 思考

#### (1) 流程反思

跑完整套流程大约花了多少时间？最卡你的是哪一步：环境搭建、补测试、还是 CI 跑通？

#### (2) 资产价值排序

本篇产出的资产里（docs/ 14 份 + scripts/ 5 份 + .github/workflows/ 1 份 + .claude/skills/ 2 份），哪一份对团队价值最大？为什么？
