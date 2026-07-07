---
title: 传统项目迁AI 13：构建护栏 - 用AI构建环境
author: fangkun119
date: 2026-07-04 13:00:00 +0800
categories: [AI编程, 传统项目迁AI]
tags: [AI编程, 传统项目迁AI]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-13-safeguard-01-build-guardrails-env/cover.jpg
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
aicmigr-13-safeguard-01-build-guardrails-env
传统项目迁AI 13：构建护栏 - 用AI构建环境
-->

## 1. 全文导读地图

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/6d536339cdfb9e8f426bd9c0c15ebe38_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第 13 篇要解决一个问题：让 AI 当环境工程师，把老项目最折磨人的"环境搭建"从"半天到一天的折磨"压到"半小时跑完"。

这一篇分为两部分，按读者诉求分流：

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/69800a1185cfabde0c9d6d10a429528d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    START["读者进入第 13 篇"] --\> Q{"你的诉求？"}
    Q --\>|"想直接上手 / 查清单"| P1["第一部分：方法论提炼"]
    Q --\>|"想看完整实战 / 复现全过程"| P2["第二部分：实战演示"]

    P1 --\> C1["1 痛点：环境为何是最大坑"]
    P1 --\> C2["2 方法论：四步法骨架"]
    P1 --\> C3["3 Check List：可裁剪速查"]

    P2 --\> C4["4 实战 Step 1：依赖盘点"]
    P2 --\> C5["5 实战 Step 2：装中间件 + 启停脚本"]
    P2 --\> C6["6 实战 Step 3：编译启动"]
    P2 --\> C7["7 实战 Step 4：接口冒烟"]
    P2 --\> C8["8 沉淀为长期资产"]

    C1 --\> D["两类读者共用"]
    C2 --\> D
    C3 --\> D
    C4 --\> D
    C5 --\> D
    C6 --\> D
    C7 --\> D
    C8 --\> D

    D --\> END["结尾：小结 + 思考"]
-->

- 第一部分（第 1-3 章）：方法论提炼。熟练 AI 编程工程师快速回顾方法论、查看 Check List 即可。
- 第二部分（第 4-8 章）：实战演示。初学 AI 编程工程师结合原文使用的 Spring AI Alibaba Admin 技术栈，复现整个环境搭建过程。

第 13 篇处于系列第三部分"构建护栏"的开端。第三部分的工作流几乎对每个老项目都一样：分析依赖、装中间件、编译启动、冒烟接口、摸清测试、必要时补出兜底测试。每一篇的提示词都可以拿来即用，改改项目名就能在自己公司那个跑了五年的订单系统上跑。

## 2. 第一部分：方法论提炼

### 2.1 环境搭建是老项目改造最大的坑

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/65683680588932123cdd5621e4f9c842_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 三个真实痛点

接手过老项目的人都懂这套折磨。环境搭建之所以成为最大的坑，源于三个结构性痛点：

##### ① 装环境报错链式触发

`clone` 下来 `mvn install` 一跑，先报缺 Nacos、再报 MySQL 版本不对、再报某个端口冲突、再报对接的内部服务连不上。每一项都得 google 半天、装一遍、配一遍、试一遍，一天就这么过去了，一脸绝望。

##### ② README 中间件清单不完整

README 不会告诉你完整的中间件清单。Nacos 写在 README 上，OTel Collector 在 `application-prod.yml` 里藏着，Redis 是某个 starter 间接拉的。照着 README 跑能跑通的概率不到一半。

##### ③ 中间件日常运维负担重

中间件装好了还要管。每次电脑重启所有服务都停了，第二天上班得一个个起。Mac 上 `brew services` 还能管一部分，Nacos / OTel Collector 这种自己下载 jar 跑的服务一断电就没了，每次手动 `sh startup.sh` 一个个拉起来。

#### (2) 破局思路：让 AI 当环境工程师

把这套折磨自动化掉的思路是：让 AI 当环境工程师，四步走完成全部工作。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/37e2c29fe35368ba0cccec464c469a72_MD5.jpg" style="display: block; width: 600px;" alt="替换文字">

<!--
flowchart LR
    A["Step 1<br>依赖盘点"] --\> B["Step 2<br>本地安装 + 启停管理"]
    B --\> C["Step 3<br>编译启动"]
    C --\> D["Step 4<br>接口冒烟"]
    B -.备选.-> E["Step 2C<br>Docker 方案"]
-->

跑完后，电脑上会有一个真正"活着"的项目（系列示例里是 Spring AI Alibaba Admin）。中途 AI 自己 debug、自己修复，工程师不需要每个错误都介入。这就是释放生产力，让人不再天天纠结这些细节问题。

### 2.2 四步法方法论骨架

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/25e5503776e863b12531f6b0ce38bba9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

四步法的完整骨架如下：

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/2b08045a52c882e98cf5ca06bfa4b384_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    S1["Step 1：依赖盘点<br>综合 external-deps.svg / yml / pom.xml / README"] --\> S2["Step 2：装中间件 + 启停管理<br>主推本地安装 + 备选 Docker"]
    S2 --\> S3["Step 3：编译启动<br>mvn clean package + 启动应用"]
    S3 --\> S4["Step 4：接口冒烟<br>5 个核心接口 curl 验证"]

    S2 -.兜底.-> GUARD["3 次失败兜底<br>同一错误连续 3 次未解决 → 停下汇报"]
    S3 -.兜底.-> GUARD
    S2 -.资产.-> LOG["踩坑日志沉淀<br>install-log.md / startup-log.md / smoke-test-result.md"]
    S3 -.资产.-> LOG
    S4 -.资产.-> LOG
-->

#### (1) 四步各自的角色

##### ① Step 1：依赖盘点（地基）

让 AI 把项目运行需要的所有外部依赖列清楚，产出 `docs/env-checklist.md`。这一步的产出是后面所有步骤的基础，缺一项后面会反复回头补。

##### ② Step 2：本地安装 + 启停管理（主推） / Docker（备选）

主推本地安装：本地装的中间件用起来轻、断电不跑等问题好处理，性能也比 Docker 在 Mac 上好（特别是 Mac M 系列芯片下 Docker 有 ARM 兼容问题）。同时让 AI 出一组启停管理脚本，把所有中间件的启停拉齐到统一命令。

偏好 Docker 的同学可以走 Step 2C 的备选方案，整套安装脚本和启停脚本全部跳过。

##### ③ Step 3：编译启动

中间件起来后，开始编译启动应用本身（`mvn clean package` + 启动应用）。这一步同样让 AI 自主修复。

##### ④ Step 4：接口冒烟

最后用 `curl` 跑几个核心接口，确认项目"真的活了"。

#### (2) 关键方法论：自主修复 + 3 次兜底 + 资产沉淀

四步法能压到半小时跑完的关键，不在四步本身，而在贯穿全程的三个机制：

| 机制 | 含义 | 为什么必要 |
|------|------|----------|
| 自主修复 | 任何一步失败，AI 先看报错、自己判断原因、自己修、修完重试 | 不带这条，工程师每个错误都要介入，又回到手动模式 |
| 3 次失败兜底 | 同一错误连续修 3 次还不行，AI 停下来汇报卡在哪 | 不带这条，AI 会陷入"改一个配置、报新错、再改、再报新错"的死循环，几小时停不下来 |
| 资产沉淀 | 每一步的踩坑过程沉淀成日志（install-log.md / startup-log.md / smoke-test-result.md） | 下次同项目重装能照着跑，新人看日志知道项目的"环境怪癖" |

`3 次失败兜底`是从实战跑出来的硬性经验。3 次同样的错误说明问题超出 AI 判断能力，必须人介入。这条提示词价值远高于脚本本身。

#### (3) 最终产物清单

四步跑完后，工程师手里会多出这样一份资产：

```
docs/
├── env-checklist.md          ← 依赖清单（Step 1 产出）
├── startup-log.md            ← 启动踩坑日志（Step 3 产出）
└── smoke-test-result.md      ← 冒烟结果（Step 4 产出）

scripts/
├── install-deps.sh           ← 一次性装齐（Step 2A 产出）
├── install-log.md            ← 安装踩坑日志（Step 2A 产出）
├── deps-start.sh             ← 每天用：一键启动所有依赖
├── deps-stop.sh              ← 每天用：一键停止所有依赖
└── deps-status.sh            ← 每天用：查看每个中间件运行状态

docker-compose.dev.yml        ← Docker 备选方案（Step 2C 产出）
```

两个去向：

- 给团队：把 `install-log.md + startup-log.md` 整理成 `docs/setup-guide.md`，下个新人照着跑。
- 沉淀成 SKILL：整套"依赖盘点 → 装中间件 → 启动 → 冒烟"流程做成 env-bootstrap SKILL，下次任何新项目（或重置环境）一键跑。

### 2.3 环境搭建 Check List（可裁剪速查）

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/0071ead05ede7fc1668123647b733d41_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/a8e61ff3bb6c69dbd41e491bcef42e5b_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

这一份 Check List 供项目阶段快速查阅，熟练 AI 编程工程师可以直接从这里开始。

#### (1) 项目准备

##### ① 前置产物检查

- [ ] `docs/external-deps.svg` 是否存在（来自第 8 篇的外部依赖图）
- [ ] `docs/api-list.md` 是否存在（来自第 9 篇的接口清单）
- [ ] README、`application*.yml`、`pom.xml` 可读

#### (2) Step 1 依赖盘点

##### ① 产出物

- [ ] `docs/env-checklist.md` 生成

##### ② review 要点

- [ ] 清单与 `docs/external-deps.svg` 对得上，有出入要让 AI 解释清楚
- [ ] 每个版本号有依据（来自 pom.xml 或 README），AI 在版本号后标注来源
- [ ] 初始化要求细化（Nacos 建命名空间、MySQL 跑初始化 SQL 等）

#### (3) Step 2 装中间件 + 启停脚本

##### ① 本地安装（主推）

- [ ] `scripts/install-deps.sh` 生成并执行成功
- [ ] `scripts/install-log.md` 真实记录踩坑过程
- [ ] 每个中间件都包含"装 + 初始化 + 验证装上"三步
- [ ] 3 次失败兜底是否触发过、AI 有没有死循环

##### ② 启停管理

- [ ] `scripts/deps-start.sh` / `deps-stop.sh` / `deps-status.sh` 三个脚本生成
- [ ] 启动顺序正确（Nacos 在 OTel Collector 之前，MySQL 在应用之前）
- [ ] status 脚本输出清晰（端口监听 + 运行状态）

##### ③ Docker 备选

- [ ] `docker-compose.dev.yml` 生成（版本号、端口、初始化脚本齐全）

#### (4) Step 3 编译启动

##### ① 产出物

- [ ] 项目跑起来（日志无 ERROR、端口监听正常）
- [ ] `docs/startup-log.md` 记录失败和修复过程
- [ ] 管理界面 `http://localhost:8080` 能打开

##### ② review 要点

- [ ] Java 版本一致（如项目要 17）
- [ ] Maven 依赖能拉到、版本对得上
- [ ] 端口未被占用（8080、8848 等）
- [ ] 配置文件齐全（application-dev.yml）
- [ ] Nacos 配置已推送

#### (5) Step 4 接口冒烟

##### ① 产出物

- [ ] `docs/smoke-test-result.md` 生成
- [ ] 5 个核心接口（覆盖登录、Prompt、Dataset、Evaluator、Trace）都返回 200
- [ ] 数据库表已自动建好

##### ② review 要点

- [ ] 选的接口真的核心，不是几个最简单的 GET
- [ ] 返回结构与 `docs/api-list.md` 描述一致
- [ ] 错误接口被 AI 标出，不为"全绿"挑简单的

#### (6) 长期资产沉淀

- [ ] `docs/setup-guide.md` 生成（给新人看）
- [ ] `.claude/skills/env-bootstrap/SKILL.md` 生成（可复用 SKILL）
- [ ] SKILL 的 `allowed-tools` 限制到 `Read, Bash, Write`

## 3. 第二部分：实战演示

第二部分以系列示例项目 Spring AI Alibaba Admin 为例，把四步法完整跑一遍。每一节的提示词都可以直接拷贝到 Claude Code 跑，改改项目名就能用在自己公司项目上。

### 3.1 Step 1：依赖盘点（实战）

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/aa09943e0d687dc5bb39a1b1c0a9d5e2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 目标

让 AI 把这个项目运行需要的所有外部依赖列清楚。这一步的产出是后面所有步骤的基础。

#### (2) 提示词

```
综合看 docs/external-deps.svg、application\*.yml、pom.xml、README，

给我列一份这个项目运行需要的完整外部依赖清单。

每个依赖列出：名字、版本要求（精确到主版本）、默认端口、

连接信息、初始化要求（建库、配 Nacos 命名空间等）。

保存到 docs/env-checklist.md。
```

#### (3) 产出

`docs/env-checklist.md`。这份清单可能列出 MySQL 8.x、Nacos 2.x、OTel Collector、外加几个模型 API 的环境变量配置。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/a56bb744616321cb62984ab45ffa60ba_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/a56bb744616321cb62984ab45ffa60ba_MD5.jpg
用途：展示 AI 生成的 env-checklist.md 完整依赖清单
内容：编辑器截图，env-checklist.md 中列出 MySQL 8.x、Nacos 2.x、OTel Collector、模型 API 环境变量等多个依赖，每个都标注了版本、端口、初始化要求
-->

需要依赖很多组件。这里可以看到提示词的价值——如果手动整理这么多依赖，要耗费大量时间。

#### (4) review 要点

##### ① 与 external-deps.svg 对齐

和 `docs/external-deps.svg` 对得上。第 8 篇画过的外部依赖图在这里复用，AI 列的清单不应该和图有出入。如果有出入，要么图错了，要么清单错了，要让 AI 解释清楚。

##### ② 版本号要有依据

AI 不能瞎写"MySQL 用 8.0 就行"。版本要么来自 `pom.xml`（数据库连接池要求的最低版本）、要么来自 README（项目作者明确说过测过哪个版本）。让 AI 在每个版本号后面标注来源。

##### ③ 初始化要求要细

Nacos 不是装上就能用，要建命名空间、推几个配置项。MySQL 不是建好库就行，要跑初始化 SQL。这些动作清单里都要有。

### 3.2 Step 2：装中间件 + 启停脚本（实战）

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/5400557f23921089a2de8c5c0624af3d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) Step 2A：本地安装方案（主推）

##### ① 为什么主推本地安装

依赖列清楚了，下一步装。主推本地安装。原因：本地装的中间件用起来轻、断电不跑等问题都好处理，性能也比 Docker 在 Mac 上好（特别是 Mac M 系列芯片下 Docker 有 ARM 兼容问题）。Docker 方案在 Step 2C 顺手给一份。

让 AI 不只是给脚本，直接执行装上。

##### ② 提示词

```
读 docs/env-checklist.md，给我生成一份本地安装脚本，

保存到 scripts/install-deps.sh。

\- 用 brew（macOS）或 apt（Linux）装中间件

\- 包含每个中间件的初始化（建库 SQL、Nacos 配置等）

\- 不会的依赖（比如某个 jar 包要下）写清楚下载链接和放哪

生成完直接执行这个脚本。执行过程遵循自主修复原则:

\- 任何一步失败，先看报错信息

\- 自己判断原因（版本不对、源问题、权限问题、依赖缺失）

\- 自己修（换源、换版本、加 sudo、装前置依赖）

\- 修完重试，跑通为止

\- 不要每个错误都问我

如果同一个错误连续修 3 次还不行，停下来汇报具体卡在哪。

其他情况一律自己解决。

最终输出一份 scripts/install-log.md，记录每个中间件

最终用了什么命令装上、过程中遇到什么问题、怎么修的。
```

##### ③ 产出

`scripts/install-deps.sh + scripts/install-log.md`。看运行结果，这个提示词很完美，AI 执行了正确的结果。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/dc6816952841111ab90fc86e383bafd2_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/dc6816952841111ab90fc86e383bafd2_MD5.jpg
用途：展示 install-deps.sh 执行结果与 install-log.md 自动生成
内容：终端截图，AI 自主执行 install-deps.sh，逐个装好 MySQL/Nacos/OTel Collector 等中间件，过程中遇到错误自主修复，最终生成 install-log.md 踩坑日志
-->

##### ④ 这条提示词的两个关键设计

**关键设计 1：** "3 次还不行就停"是必须的兜底。不带这条会出现 AI 改一个配置、报新错、再改、再报新错的死循环，几小时停不下来。3 次同样的错误说明问题超出 AI 判断能力，必须人介入。这是从实际跑出来的经验。

**关键设计 2：** 要求输出 `install-log.md`。这份踩坑日志比脚本本身更值钱。下次同项目重装能照着跑，团队新人看日志知道项目的"环境怪癖"。比如某天 brew 默认装 MySQL 9 和项目要求的 8 冲突时，日志里有"用 `brew install mysql@8` 而不是 `brew install mysql`"这种关键决策。

##### ⑤ review 要点

- 脚本是不是对每个中间件都包含"装 + 初始化 + 验证装上"三步。
- `install-log.md` 是不是真实反映了过程。
- 3 次失败的兜底有没有触发过、AI 有没有死循环。

#### (2) Step 2B：依赖启停管理脚本

##### ① 目标

中间件装好了，但还有一件事：电脑一关再开，所有服务都停了，每次手动起一遍特别烦。让 AI 生成一组启停管理脚本，把所有中间件的启停拉齐到统一命令。

##### ② 提示词

```
基于 Step 2A 装好的中间件，生成三个脚本到 scripts/ 下：

\- deps-start.sh：一键启动所有依赖中间件

\- deps-stop.sh：一键停止所有依赖中间件

\- deps-status.sh：查看每个中间件的运行状态

考虑混合场景：有的用 brew services 管，有的是手动 jar，

有的是 systemd。脚本要能处理这几种。

启动后等服务就绪再返回，不要"启动了但还没 ready"。

status 脚本要打印每个中间件的运行状态和端口监听情况。
```

##### ③ 产出

`scripts/deps-start.sh / deps-stop.sh / deps-status.sh`。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/d261bb6ba75eb644c0f07b0fcea8a592_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/d261bb6ba75eb644c0f07b0fcea8a592_MD5.jpg
用途：展示 deps-start/stop/status 三个脚本的生成结果
内容：编辑器截图，scripts 目录下生成了 deps-start.sh、deps-stop.sh、deps-status.sh 三个脚本，AI 同时输出了运行示例和状态表格
-->

每天上班 `./scripts/deps-start.sh`，下班 `./scripts/deps-stop.sh`，比 Docker 方案还方便（Docker 还要 `docker-compose up`）。这才是本地开发的应有姿态。

##### ④ review 要点

**review 要点 1：** 启动顺序要对。Nacos 要在 OTel Collector 之前启动，因为 Collector 可能用 Nacos 做服务发现。MySQL 要在应用之前启动。AI 默认可能并行启动，要让它按依赖顺序串行起。

**review 要点 2：** status 脚本输出要清晰。一眼看出哪个起来了、哪个挂了，比 `ps aux | grep` 一个个查方便十倍。

#### (3) Step 2C：Docker 方案（备选）

##### ① 适用人群

主推本地安装，但有些同学可能偏好 Docker。给一条提示词让 AI 顺手出一份 Docker 方案。

##### ② 提示词

```
顺手给一份 docker-compose.dev.yml，把所有依赖打包成 docker。

偏好 Docker 的同学可以用这个替代 Step 2A 和 2B。

版本号、端口、初始化脚本要齐全。

保存到项目根目录。不用运行
```

##### ③ 产出

`docker-compose.dev.yml`。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/4869f5d1cd7542429b2ad6d995952ebd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/4869f5d1cd7542429b2ad6d995952ebd_MD5.jpg
用途：展示 docker-compose.dev.yml 的生成结果
内容：编辑器截图，AI 生成的 docker-compose 文件内容，包含 MySQL、Nacos、OTel Collector 等服务的版本、端口映射和初始化脚本配置
-->

偏好 Docker 的同学直接 `docker-compose -f docker-compose.dev.yml up -d` 就好。本地装那套 install + start/stop 全部跳过。这是给大家的第二条路。

### 3.3 Step 3：编译启动（实战）

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/50ac3260b4fb7ad2b74801fdf8130934_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 目标

中间件起来了，开始编译启动应用本身。这一步同样让 AI 自主修复。

#### (2) 提示词

```
中间件已经起来了（用./scripts/deps-status.sh 确认）。

现在帮我跑 mvn clean package + 启动应用。

启动过程同样遵循自主修复原则

（参照 Step 2A 的兜底机制：连续 3 次同一错误才停下来汇报）。

启动成功后告诉我应用监听的端口、管理界面地址。

失败和修复的过程记到 docs/startup-log.md。
```

#### (3) 产出

项目跑起来了 + `docs/startup-log.md`。编译启动的常见错误比依赖安装更多：

```
Java 版本不对（项目要 17，本地是 21 或 11）

Maven 仓库连不上，或者拉的依赖版本和 lockfile 对不上

端口被占用（8080、8848 这些常用端口被别的服务占了）

配置文件缺失（application-dev.yml 不在仓库里，需要从 application.yml 拷一份改）

Nacos 配置没推（应用启动时连 Nacos 拉不到配置）
```

每一类 AI 都有处理经验。给它自主修复授权，多数能自己搞定。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/c6b23752dfbf0d8174c1ed19e7830a14_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/c6b23752dfbf0d8174c1ed19e7830a14_MD5.jpg
用途：展示 mvn 编译 + 应用启动成功后的终端输出
内容：终端截图，Maven build success 后应用监听端口、管理界面地址打印出来，启动日志没有 ERROR
-->

#### (4) review 要点

- 日志没报 ERROR。
- 端口监听正常。
- 管理界面 `http://localhost:8080` 能打开（页面可能要登录，能看到登录页就算活着）。

#### (5) 额外一步：启动前端

继续追加一句提示词，让 AI 顺手把前端也启动：

```
有前端页面吗？也启动一下，给我访问地址
```

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/eeb3c2f42e12707b7217b06633b3e10a_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/eeb3c2f42e12707b7217b06633b3e10a_MD5.jpg
用途：展示"额外请求启动前端"的响应结果
内容：终端截图，AI 返回前端的启动命令、监听端口和访问地址（如 http://localhost:xxxx），运行流畅无报错
-->

看这个输出就很舒服。然后页面也出来了：

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/d6c4e367d627051b846fcc9382b22ec5_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/d6c4e367d627051b846fcc9382b22ec5_MD5.jpg
用途：展示前端页面在浏览器中正常加载的效果
内容：浏览器截图，Spring AI Alibaba Admin 的管理界面（登录后的管理控制台，能看到 Prompt/Dataset/Evaluator/Trace 等功能模块）
-->

到这里，就可以看出来这套提示词和本篇思路的好处了。

### 3.4 Step 4：接口冒烟（实战）

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/e5e75179eda8d39a4ccca3c5805159b6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

#### (1) 目标

最后一步：用 `curl` 跑几个核心接口，确认项目真的活了。

#### (2) 提示词

```
读 docs/api-list.md，挑 5 个最核心的接口（覆盖登录、Prompt、

Dataset、Evaluator、Trace 几大模块），用 curl 跑一遍。

返回 200 算通过，返回错误的列出来。

最后输出一份 docs/smoke-test-result.md。
```

#### (3) 产出

`docs/smoke-test-result.md`。跑完这一步，项目算"真的活了"。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/a58722fcff4914619ef0814872dca5de_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/a58722fcff4914619ef0814872dca5de_MD5.jpg
用途：展示 5 个核心接口的 curl 冒烟结果
内容：终端输出截图，登录、Prompt、Dataset、Evaluator、Trace 五个核心接口的 HTTP 响应都返回 200，结果汇总到 docs/smoke-test-result.md
-->

然后看一眼数据库，都建好了。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/1f73cf547156296f1f98d475db6d96e6_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/1f73cf547156296f1f98d475db6d96e6_MD5.jpg
用途：验证接口冒烟后数据库表已自动创建
内容：数据库客户端截图，显示 MySQL 中的表已按 entity/model 定义建好（如 prompt、dataset、evaluator、trace 相关表）
-->

#### (4) review 要点

##### ① 选的接口是不是真的核心

不是挑了几个最简单的 GET 接口凑数。

##### ② 返回结构和 api-list.md 描述一致

接口返回的字段要和第 9 篇整理的接口清单对得上。

##### ③ 错误接口要标出来

如果有接口返回错误，AI 是不是标出来了（不要为了"全绿"故意挑能过的）。

### 3.5 沉淀为长期资产（实战）

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/3f834aae8115d279d29f616f41e01655_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

四步跑完后，手里多出一份资产清单：

```
docs/
├── env-checklist.md          ← 依赖清单
├── startup-log.md            ← 启动踩坑日志
└── smoke-test-result.md      ← 冒烟结果

scripts/
├── install-deps.sh           ← 一次性装齐
├── install-log.md            ← 安装踩坑日志
├── deps-start.sh             ← 每天用
├── deps-stop.sh              ← 每天用
└── deps-status.sh            ← 每天用

docker-compose.dev.yml        ← Docker 备选
```

#### (1) 产出 1：给团队的 setup-guide.md

把 `install-log.md + startup-log.md` 整理成一份 `docs/setup-guide.md`，下个新人来照着跑。这件事 AI 帮你做：

```
基于 scripts/install-log.md 和 docs/startup-log.md，

整理一份给新人看的 setup-guide.md，

包含：前置条件、装中间件步骤、启动命令、常见踩坑、验证清单。

保存到 docs/setup-guide.md。
```

#### (2) 产出 2：挖成 env-bootstrap SKILL

整套"依赖盘点 → 装中间件 → 启动 → 冒烟"流程做成一个 SKILL，下次任何新项目（或者重置环境）都能一键跑。这就是第 11 篇挖到的第二个 SKILL。

```
基于这次环境搭建的全流程，给我生成一个 env-bootstrap 的 SKILL，

保存到.claude/skills/env-bootstrap/SKILL.md。

触发场景：新接手项目、重置环境、定期验证环境健康。

步骤：依赖盘点 → 装中间件 → 启停脚本 → 编译启动 → 接口冒烟。

allowed-tools 限制到 Read, Bash, Write。
```

跑完第 13 篇，积累的 SKILL 数量从 1 个变成 2 个：`docs-auto-sync` + `env-bootstrap`。两个都是"反复做但没沉淀"的高价值流程，还有一个给读者留着挖。

## 4. 小结

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/ed9ce38b69f7403ce45e511f96029f4c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

第 13 篇讲了一件事：让 AI 当环境工程师，把环境搭建从"半天到一天的折磨"压到"半小时跑完"。

四步法：依赖盘点 → 本地安装 + 启停管理（主线，Docker 备选）→ 编译启动 → 接口冒烟。在这一篇中，安装依赖的部分可能需要关注下，因为本地环境的差异性，安装可能会失败，需要给 AI 提供帮助，比如提供权限等。

每一步都让 AI 自主修复。3 次失败兜底防死循环。所有踩坑过程沉淀成日志，最终汇成 `setup-guide.md` 和 `env-bootstrap` SKILL，成为团队和自己的长期资产。

整个第三部分的提示词都是这个特点：拿来即用，改改项目名就能在自己公司项目跑。第 13 篇是最典型的一篇。

下一篇摸清这个项目的测试现状：能跑通吗？覆盖度怎样？动手改造之前要不要补测试？

## 5. 思考

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/3403ade60dd40936495f3b43422b5d2f_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

你最近一次接手老项目搭环境花了多长时间？这一篇的四步法，哪一步让你眼前一亮？回想一下当时手动做的那些事，哪些是 AI 现在能替你做的？

你电脑上现在跑着多少个开发依赖（MySQL、Redis、Nacos 等）？有 start / stop 统一管理脚本吗？如果没有，每次电脑重启是怎么处理这些服务的？

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/e23a1e8a67a1c73890489d5a14b92e3e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/13_构建护栏_01：用AI构建环境/e23a1e8a67a1c73890489d5a14b92e3e_MD5.jpg
用途：展示四步法（依赖盘点 → 安装启停 → 编译启动 → 接口冒烟）整体流程图
内容：Mermaid 风格的流程示意图，把环境搭建四步法串成一条主线，标出每一步的产出物（env-checklist.md / install-deps.sh / deps-*.sh / startup-log.md / smoke-test-result.md）和兜底机制（3 次失败兜底）
-->
