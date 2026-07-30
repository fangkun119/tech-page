---
title: 传统项目迁AI 13：构建护栏 - 用AI构建环境
author: fangkun119
date: 2026-07-04 13:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
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

## 1. 为什么老项目搭环境是最大的坑

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/4e677b90ec1184ce64dcda33ac449574_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

接手过老项目的人都懂这套折磨：`git clone` 完，`mvn install` 一跑，先报缺 Nacos、再报 MySQL 版本不对、再报某个端口冲突、再报对接的内部服务连不上。<span style="color: red; font-weight: bold;">每一项都得 google 半天、装一遍、配一遍、试一遍，一天就这么过去了。</span>

<span style="color: red; font-weight: bold;">环境搭建之所以是最大的坑，不是某一步难，而是源于三个结构性痛点。</span>

### 1.1 三个结构性痛点

| 痛点              | 描述                                                                                                           |
| --------------- | ------------------------------------------------------------------------------------------------------------ |
| 装环境报错链式触发       | 缺 Nacos 报一次、MySQL 版本不对报一次、端口冲突再报一次……，每一项都要单独 google、装、配、试，一天耗在 debug 里                                       |
| README 中间件清单不完整 | Nacos 写在 README 上、OTel Collector 藏在 `application-prod.yml` 里、Redis 是某个 starter 间接拉的 ……，照着 README 跑能跑通的概率不到一半 |
| 中间件日常运维负担重      | 电脑一关所有服务都停。Nacos / OTel Collector 这种自己下 jar 跑的服务断电就没，每次手动 `sh startup.sh` 一个个拉起来                             |

三个痛点串成一条因果链：**<span style="color: red; font-weight: bold;">装的时候链式报错</span> → <span style="color: red; font-weight: bold;">装的过程中清单不全（缺一补一）</span> → <span style="color: red; font-weight: bold;">装完之后还要天天手动管</span>**。任何一个环节出问题，半天到一天就搭进去了。

### 1.2 破局思路：让 AI 当环境工程师

把这套折磨自动化掉的思路是一句话：**让 AI 当环境工程师，四步走完成全部工作**。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/37e2c29fe35368ba0cccec464c469a72_MD5.jpg" style="display: block; width: 600px;" alt="四步法流程图">

<!--
flowchart LR
    A["Step 1<br>依赖盘点"] --\> B["Step 2<br>本地安装 + 启停管理"]
    B --\> C["Step 3<br>编译启动"]
    C --\> D["Step 4<br>接口冒烟"]
    B -.备选.-> E["Step 2C<br>Docker 方案"]
-->

类比传统装修 vs 智能家居：传统做法像手工装修——水管漏了自己修、电路坏了自己查、每次出门一个个关电源；AI 环境工程师像整套智能家居系统——水电气异常自动报警并修复、统一面板一键启停。区别不在工具本身，而在**是否把"装 + 修 + 管"整套环节交给一个能自主执行的执行器**。

<span style="color: red; font-weight: bold;">跑完四步，电脑上会有一个真正"活着"的项目。中途 AI 自己 debug、自己修复，工程师不需要每个错误都介入。</span>这就是释放生产力——让人不再天天纠结这些细节。

## 2. 整体方法论：四步法骨架

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/567f8e34b3f6d201ca8b8bf881e450f4_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">四步法只是骨架，真正的价值在贯穿全程的三个机制</span>（下一章讲透）。本节先把骨架立起来。

### 2.1 四步法完整骨架

四步法的完整骨架如下：

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/2b08045a52c882e98cf5ca06bfa4b384_MD5.jpg" style="display: block; width: 800px;" alt="四步法完整骨架">

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

图里有两条主线和两条暗线：

- 主线是 Step 1→2→3→4 的执行流；
- 暗线一是"3 次失败兜底"挂在 Step 2、3 上
- 暗线二是"踩坑日志沉淀"挂在 Step 2、3、4 上。

**主线决定跑得快不快，暗线决定跑得稳不稳、能不能复用**。

### 2.2 四步各自的角色

四个步骤，产出各自的文档和脚本，以可复用的方式实现环境搭建自动化

| 步骤                 | 角色                | 产物                                      | 说明                                              |
| ------------------ | ----------------- | --------------------------------------- | ----------------------------------------------- |
| Step 1：依赖盘点        | 地基                | `docs/env-checklist.md`                 | 把项目运行需要的所有外部依赖列清楚。后面三步全靠它，缺一项就要反复回头补             |
| Step 2：装中间件 + 启停管理 | 主推本地安装（备选 Docker） | `scripts/install-deps.sh` + 启停脚本      | 本地装轻、断电好处理、性能优于 Docker（尤其 Mac M 系列 ARM 兼容问题）          |
| Step 3：编译启动        | 让应用本身跑起来          | 编译产物 + 启动命令                             | 同样走自主修复                                        |
| Step 4：接口冒烟        | 验证项目真的活了          | `docs/smoke-test-result.md`              | 5 个核心接口 `curl` 验证返回 200                          |

<span style="color: red; font-weight: bold;">Step 1 是地基，错一步后面全错；Step 2 是主战场，环境怪癖最多；Step 3、4 是验收，确认前面真的跑通了。</span>

## 3. 贯穿全程的三个机制

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/69d1e47fd294aa82031be93097332665_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">四步法能压到半小时跑完的关键，不在四步本身，而在贯穿全程的三个机制：</span>

| 机制      | 含义                                                                          | 为什么必要                                      |
| ------- | --------------------------------------------------------------------------- | ------------------------------------------ |
| 自主修复    | 任何一步失败，AI 先看报错、自己判断原因、自己修、修完重试                                              | 不带这条，工程师每个错误都要介入，又回到手动模式                   |
| <span style="color: red; font-weight: bold;">3 次失败兜底</span> | 同一错误连续修 3 次还不行，AI 停下来汇报卡在哪                                                  | 不带这条，AI 会陷入"改一个配置、报新错、再改、再报新错"的死循环，几小时停不下来 |
| 资产沉淀    | 每一步的<span style="color: red; font-weight: bold;">踩坑过程沉淀成日志</span>（`install-log.md` / `startup-log.md` / `smoke-test-result.md`） | 下次同项目重装能照着跑，<span style="color: red; font-weight: bold;">新人看日志知道项目</span>的"环境怪癖"               |

三个机制各自解决不同问题，下面逐个讲透。

### 3.1 自主修复：AI 的自动恒温器

先讲个生活类比：**自动恒温器**。你设定 22 度，空调发现现在 18 度，自己判断"要升温"，启动制热，温度到了自动停。整个过程不需要你每次都去看温度计、手动开关空调。

<span style="color: red; font-weight: bold;">自主修复机制就是给 AI 装了个"自动恒温器"</span>：任何一步失败时，AI 自己走"看报错 → 判断原因（版本不对、源问题、权限问题、依赖缺失）→ 自己修（换源、换版本、加 sudo、装前置依赖）→ 修完重试"的闭环，跑通为止。

为什么这条必须写进提示词？因为**不带这条，AI 默认会频繁确认**——这是探索阶段的合理行为，但在环境搭建里会打断节奏。每个错误都问一遍，工程师实际介入次数比手动装还多。类比传统协作：**就像授权下属独立干完一个任务，而不是事事请示**——授权不清，下属每个细节都来问，效率比你自己干还低。

### 3.2 三次失败兜底：防止死循环的硬性闸门

再讲个类比：**银行 App 三次输错密码就锁账号**。为什么不是两次、不是十次？两次太敏感（手滑也会锁），十次太宽松（密码被人爆破就完了）。三次是平衡点——既允许合理重试，又能在异常情况下及时止损。

<span style="color: red; font-weight: bold;">3 次失败兜底是同一个设计：同一错误连续修 3 次还不行，AI 停下来汇报卡在哪。</span>为什么是 3 次？**两次太少**——有些问题第一次修完会暴露第二个问题，看起来像"同一个错误"，其实是连锁反应，需要再给一次机会；**四次太多**——AI 陷入"改一个配置、报新错、再改、再报新错"的死循环时，再给它一次机会只会再耗 10 分钟。

<span style="color: red; font-weight: bold;">这是从实战跑出来的硬性经验。3 次同样的错误说明问题已经超出 AI 的判断能力，必须人介入。</span>**这条提示词的价值远高于脚本本身**——脚本换个项目要重写，这条机制换了项目照样能用。

### 3.3 资产沉淀：装修师傅的笔记本

最后一个类比：**装修老师傅的笔记本**。每接一户人家，师傅会在本子上记下这户的怪癖——"这户的水管是老式镀锌管，接头要特别处理"、"这户的电路暗盒位置不规则，开槽时注意"。下次再来这户或者接类似户型，翻开本子就知道坑在哪。

<span style="color: red; font-weight: bold;">资产沉淀机制就是这个笔记本</span>：每一步的踩坑过程都写进日志（Step 2 写 `install-log.md`、Step 3 写 `startup-log.md`、Step 4 写 `smoke-test-result.md`）。下次同项目重装，照着日志跑就能避开上次的坑；新人接手，看日志就知道这个项目的"环境怪癖"。

举个具体场景：某天 brew 默认装的 MySQL 是 9.x，但项目要 8.x。如果没记日志，下次重装又会撞同一个坑；日志里一句"用 `brew install mysql@8` 而不是 `brew install mysql`"，下次直接照做。**这份日志比脚本本身值钱**——脚本能重写，踩坑经验丢了就要重新踩一遍。

## 4. Step 1：依赖盘点（地基）

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/38d12d7ae882c229d074eb898383a1bd_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<span style="color: red; font-weight: bold;">四步法的第一步不是动手装东西，而是先把依赖列清楚。</span>这一步产出一份 `docs/env-checklist.md`，是后面三步的基础——清单错一项，后面三步会反复回头补。

### 4.1 原理：依赖清单是部署契约

类比传统软件工程：接口定义是调用方和被调方的契约。**环境搭建**里的**依赖清单**扮演的是同一种角色，只是它约束的是"部署侧"和"运行侧"——<span style="color: red; font-weight: bold;">依赖清单就是部署契约</span>。

<span style="color: red; font-weight: bold;">契约错，后面三步全炸：</span>

- 清单漏了 OTel Collector，Step 2 装中间件就漏装一项，Step 3 启动时应用连不上链路追踪，报错看半天才发现
- 清单写错 MySQL 版本（写 9 实际要 8），Step 2 按错的版本装上，Step 3 启动时连接池校验版本不通过
- 清单没写初始化要求（Nacos 建命名空间、MySQL 跑初始化 SQL），中间件装上但用不起来

所以 Step 1 是地基。**花十分钟把清单列清楚，省下后面三步反复回头补的几个小时**。

### 4.2 提示词

```
综合看 docs/external-deps.svg、application\*.yml、pom.xml、README，

给我列一份这个项目运行需要的完整外部依赖清单。

每个依赖列出：名字、版本要求（精确到主版本）、默认端口、

连接信息、初始化要求（建库、配 Nacos 命名空间等）。

保存到 docs/env-checklist.md。
```

这条提示词刻意点名四个输入源：`external-deps.svg`（外部依赖图）、`application*.yml`（运行时配置）、`pom.xml`（构建期依赖）、README（作者声明）。**四个源一起看，才能拼出完整真相**——任何单一源都会漏。画外部依赖图时已经踩过这个坑：README 写了 Nacos，OTel Collector 藏在 `application-prod.yml` 里，Redis 是某个 starter 间接拉的。提示词把这四个源点齐，AI 就不会只盯 README 抄一份不完整的清单。

### 4.3 产出

`docs/env-checklist.md`，列出 MySQL 8.x、Nacos 2.x、OTel Collector、外加几个模型 API 的环境变量配置。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/a56bb744616321cb62984ab45ffa60ba_MD5.jpg" style="display: block; width: 800px;" alt="env-checklist.md 内容">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/a56bb744616321cb62984ab45ffa60ba_MD5.jpg
用途：展示 AI 生成的 env-checklist.md 完整依赖清单
内容：编辑器截图，env-checklist.md 中列出 MySQL 8.x、Nacos 2.x、OTel Collector、模型 API 环境变量等多个依赖，每个都标注了版本、端口、初始化要求
-->

这个项目需要依赖很多组件。这里能看到提示词的价值——**如果手动整理这么多依赖，要耗费大量时间**，还要交叉验证四个源；AI 几分钟跑完，还顺带把版本号、端口、初始化要求都列齐。

### 4.4 review 要点

清单出来不能直接用，要按下面三点 review 一遍：

| review 要点                     | 通过标准                                                                                         |
| ----------------------------- | -------------------------------------------------------------------------------------------- |
| 与 `docs/external-deps.svg` 对齐 | AI 列的清单<span style="color: red; font-weight: bold;">和外部依赖图对得上</span>。<br>有出入要让 AI 解释清楚 —— 要么图错了，要么清单错了，不能放过去                                 |
| 版本号有依据                        | 每个版本号后面要<span style="color: red; font-weight: bold;">标注来源</span>（`pom.xml` 里连接池要求的最低版本，或 README 里作者明确测过的版本）。<br>不能出现"MySQL 用 8.0 就行"这种没出处的写法 |
| <span style="color: red; font-weight: bold;">初始化要求</span>细化                   | Nacos 要建命名空间、推几个配置项；<br>MySQL 要跑初始化 SQL——这些动作清单里都要有。<br>装上就能用是不可能的，初始化动作漏一项，Step 3 启动时就报错    |

## 5. Step 2：装中间件 + 启停管理

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/9ca8e0c7cb01002a1c24a99d36b74b89_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

Step 1 的清单过了 review，进入四步法最大的一步。这一步分三个子步：Step 2A 本地安装（主推）、Step 2B 启停管理脚本、Step 2C Docker 备选方案。

### 5.1 为什么主推本地安装

你可能会问：Docker 一条 `docker-compose up` 全搞定，为什么还要本地装？三个原因：

| 原因                  | 说明                                                                                                                                                                        |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 本地装的中间件用起来轻         | 不套一层容器，启动快、占用小，调试时直接看本机进程和端口                                                                                                                                              |
| 断电不跑等问题好处理          | 本地装的中间件挂了就是挂了，看日志、改配置直接上手；<br>Docker 容器挂了还要先 docker logs 拉日志，多一层间接                                                                                                        |
| 性能比 Docker 在 Mac 上好 | <span style="color: red; font-weight: bold;">Mac M 系列芯片下 Docker 有 ARM 兼容问题</span> —— 部分镜像没出 arm64 版本，跑起来要么走 emulation 慢一截，要么直接报错。<br>本地用 `brew install` 装的是原生 ARM 包，没有这层坑 |

Docker 方案在 5.4 节给一份备选 —— 偏好 Docker 的同学可以跳过 5.2 和 5.3，直接走 5.4。

### 5.2 Step 2A：本地安装方案

#### (1) 提示词

```
读 docs/env-checklist.md，给我生成一份本地安装脚本，

保存到 scripts/install-deps.sh。
- 用 brew（macOS）或 apt（Linux）装中间件
- 包含每个中间件的初始化（建库 SQL、Nacos 配置等）
- 不会的依赖（比如某个 jar 包要下）写清楚下载链接和放哪

生成完直接执行这个脚本。执行过程遵循自主修复原则:
- 任何一步失败，先看报错信息
- 自己判断原因（版本不对、源问题、权限问题、依赖缺失）
- 自己修（换源、换版本、加 sudo、装前置依赖）
- 修完重试，跑通为止
- 不要每个错误都问我

如果同一个错误连续修 3 次还不行，停下来汇报具体卡在哪。
其他情况一律自己解决。

最终输出一份 scripts/install-log.md，记录每个中间件
最终用了什么命令装上、过程中遇到什么问题、怎么修的。
```

这条提示词里藏着两个关键设计：<span style="color: red; font-weight: bold;">自主修复 + 3 次失败兜底</span>、<span style="color: red; font-weight: bold;">强制输出 `install-log.md`</span>。下面分别说为什么不可省。

#### (2) 产出

`scripts/install-deps.sh + scripts/install-log.md`

AI 按提示词执行后，逐个装好 MySQL、Nacos、OTel Collector 等中间件，过程中遇到错误自主修复，最终生成 install-log.md 踩坑日志。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/dc6816952841111ab90fc86e383bafd2_MD5.jpg" style="display: block; width: 800px;" alt="install-deps.sh 执行 + install-log.md">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/dc6816952841111ab90fc86e383bafd2_MD5.jpg
用途：展示 install-deps.sh 执行结果与 install-log.md 自动生成
内容：终端截图，AI 自主执行 install-deps.sh，逐个装好 MySQL/Nacos/OTel Collector 等中间件，过程中遇到错误自主修复，最终生成 install-log.md 踩坑日志
-->

#### (3) 两个关键设计

##### ① 关键设计 1：3 次失败兜底不可省

"连续 3 次同一错误才停下来汇报"这条提示词不可省。原理第 3 章已经讲透——**不带这条，AI 会陷入"改一个配置、报新错、再改、再报新错"的死循环，几小时停不下来**。

实战价值在两处：

- **AI 不会闷头死磕**：3 次失败的硬阈值让 AI 在超出自身判断能力时主动停下来，工程师介入即可，不会浪费一整个下午
- <span style="color: red; font-weight: bold;">工程师能拿到干净的卡点报告</span>：AI 停下时汇报"卡在哪、试过什么、为什么不 work"，比工程师自己从一堆日志里扒问题快得多

这条是从实际跑出来的硬性经验，提示词里这一句**一行都不能省**。

##### ② 关键设计 2：`install-log.md` 比脚本值钱

脚本本身只是把中间件装上，但`install-log.md` 是**项目环境怪癖的说明书**，价值远高于脚本。

类比传统软件工程：这就像项目里那个<span style="color: red; font-weight: bold;">只在本机踩过坑才知道的"怪癖笔记"</span> 

- 某个老项目只能在 JDK 17.0.8 上跑
- 某个中间件必须用 8.0.28 不能用 8.0.30
- 某个 brew formula 默认装错版本要换 tap 源。

这些信息散在每个老工程师脑子里，新人来了要重踩一遍。

`install-log.md` 把这些怪癖写下来：

- 下次同项目重装，<span style="color: red; font-weight: bold;">照着日志跑就能避开当年踩过的坑</span>
- 团队新人看日志，十分钟知道这个项目的环境有哪些脾气

举个真实的例子：<span style="color: red; font-weight: bold;">某天 brew 默认装 MySQL 9 和项目要求的 8 冲突时，日志里会记下"用 `brew install mysql@8` 而不是 `brew install mysql`"这种关键决策。</span>**这一条记录，下次重装直接省两小时的排查**。

#### (4) review 要点

脚本和日志出来后，按下面三点 review：

| review 要点 | 通过标准 |
|------------|---------|
| 三步完整 | 脚本对每个中间件都包含"装 + 初始化 + 验证装上"三步。只装不验证、或只装不初始化，都算不合格 |
| 日志真实 | `install-log.md` 真实反映了过程——有没有记录失败、有没有记录决策、有没有记下版本切换的原因。一份全是"成功"的日志基本是 AI 编的 |
| 兜底机制有效 | 3 次失败的兜底有没有触发过、AI 有没有死循环。如果一份日志里某个错误连续修了 5 次还在试，说明兜底没生效，要回去检查提示词 |

### 5.3 Step 2B：依赖启停管理脚本

中间件装好了，还有一个日常痛点：电脑一关再开，所有服务都停了，每天上班要手动起一遍。

#### (1) 提示词

```
基于 Step 2A 装好的中间件，生成三个脚本到 scripts/ 下：
- deps-start.sh：一键启动所有依赖中间件
- deps-stop.sh：一键停止所有依赖中间件
- deps-status.sh：查看每个中间件的运行状态

考虑混合场景：有的用 brew services 管，有的是手动 jar，
有的是 systemd。脚本要能处理这几种。
启动后等服务就绪再返回，不要"启动了但还没 ready"。
status 脚本要打印每个中间件的运行状态和端口监听情况。
```

这条提示词的关键在第三段："混合场景" —— **一个项目里中间件的管理方式天生是混合的**。MySQL 用 brew services 管、Nacos 是手动下载 jar 跑的、Linux 上某些服务又是 systemd 单元。脚本不能假设全是同一种管理方式，必须三种都兼容。

另一句关键："启动后等服务就绪再返回"——<span style="color: red; font-weight: bold;">启动进程和端口监听之间有延迟，脚本要轮询确认端口 ready 了再返回，否则下游应用启动时连不上中间件。</span>

#### (2) 产出

`scripts/deps-start.sh / deps-stop.sh / deps-status.sh` 三个脚本。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/d261bb6ba75eb644c0f07b0fcea8a592_MD5.jpg" style="display: block; width: 800px;" alt="deps-start/stop/status 三脚本">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/d261bb6ba75eb644c0f07b0fcea8a592_MD5.jpg
用途：展示 deps-start/stop/status 三个脚本的生成结果
内容：编辑器截图，scripts 目录下生成了 deps-start.sh、deps-stop.sh、deps-status.sh 三个脚本，AI 同时输出了运行示例和状态表格
-->

每天上班 `./scripts/deps-start.sh`，下班 `./scripts/deps-stop.sh`，比 Docker 方案还方便——Docker 还要 `docker-compose up`。这才是本地开发的应有姿态。

#### (3) review 要点

| review 要点                                                  | 通过标准                                                                                           |
| ---------------------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| <span style="color: red; font-weight: bold;">启动顺序正确</span> | Nacos 要在 OTel Collector 之前启动（Collector 可能用 Nacos 做服务发现），MySQL 要在应用之前启动。AI 默认可能并行启动，要让它按依赖顺序串行起 |
| status 脚本输出清晰                                              | 一眼看出哪个起来了、哪个挂了，比 `ps aux \| grep` 一个个查方便十倍。输出要包含端口监听状态和进程状态，不能只看进程在不在                          |

### 5.4 Step 2C：Docker 方案（备选）

主推本地安装，但有些同学偏好 Docker——容器化部署更接近生产环境、隔离性好、跨机器迁移方便。

#### (1) 提示词

```
顺手给一份 docker-compose.dev.yml，把所有依赖打包成 docker。
偏好 Docker 的同事可以用这个替代 Step 2A 和 2B。
版本号、端口、初始化脚本要齐全。
保存到项目根目录。不用运行
```

最后一句"不用运行"是有意的 —— Docker 方案只是**备选**，主推的本地安装已经在 5.2 跑过了。 这里只生成文件不执行，避免和已经装好的本地中间件打架（端口冲突、资源占用）。

#### (2) 产出

`docker-compose.dev.yml`，保存在项目根目录。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/4869f5d1cd7542429b2ad6d995952ebd_MD5.jpg" style="display: block; width: 800px;" alt="docker-compose.dev.yml">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/4869f5d1cd7542429b2ad6d995952ebd_MD5.jpg
用途：展示 docker-compose.dev.yml 的生成结果
内容：编辑器截图，AI 生成的 docker-compose 文件内容，包含 MySQL、Nacos、OTel Collector 等服务的版本、端口映射和初始化脚本配置
-->

偏好 Docker 的同学直接 `docker-compose -f docker-compose.dev.yml up -d` 就好，本地装那套 install + start/stop 全部跳过。 

这是给启动环境的第二条路。

## 6. Step 3：编译启动

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/a6bcfd9f3cd45ae6f26d0987b39ed63c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

中间件起来了（`./scripts/deps-status.sh` 确认过），进入第三步：让应用本身跑起来。这一步同样让 AI 自主修复，提示词几乎不用换思路——延续 Step 2A 那套"看报错→自己修→修完重试→3 次兜底"的机制。

### 6.1 提示词

```
中间件已经起来了（用 ./scripts/deps-status.sh 确认）。
现在帮我跑 mvn clean package + 启动应用。
启动过程同样遵循自主修复原则
（参照 Step 2A 的兜底机制：连续 3 次同一错误才停下来汇报）。

启动成功后告诉我应用监听的端口、管理界面地址。
失败和修复的过程记到 docs/startup-log.md。
```

这条提示词复用 Step 2A 的两个关键设计：**自主修复 + 3 次失败兜底**（机制原理第 3 章已经讲透，这里不重复），**踩坑日志沉淀到 `docs/startup-log.md`**。原理一致，只是战场从"装中间件"换到"编译启动应用"。

### 6.2 产出

项目跑起来了 + `docs/startup-log.md`。这一步的常见错误比依赖安装还多，AI 大概率会撞上下面五类：

```
- Java 版本不对（项目要 17，本地是 21 或 11）
- Maven 仓库连不上，或者拉的依赖版本和 lockfile 对不上
- 端口被占用（8080、8848 这些常用端口被别的服务占了）
- 配置文件缺失（application-dev.yml 不在仓库里，需要从 application.yml 拷一份改）
- Nacos 配置没推（应用启动时连 Nacos 拉不到配置）
```

每一类 AI 都有处理经验。给它自主修复授权，多数能自己搞定。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/c6b23752dfbf0d8174c1ed19e7830a14_MD5.jpg" style="display: block; width: 800px;" alt="mvn 编译启动成功">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/c6b23752dfbf0d8174c1ed19e7830a14_MD5.jpg
用途：展示 mvn 编译 + 应用启动成功后的终端输出
内容：终端截图，Maven build success 后应用监听端口、管理界面地址打印出来，启动日志没有 ERROR
-->

### 6.3 额外一步：启动前端

后端跑通了，前端也别落下——继续追加一句提示词，让 AI 顺手把前端也启动：

```
有前端页面吗？也启动一下，给我访问地址
```

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/eeb3c2f42e12707b7217b06633b3e10a_MD5.jpg" style="display: block; width: 800px;" alt="前端启动命令">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/eeb3c2f42e12707b7217b06633b3e10a_MD5.jpg
用途：展示"额外请求启动前端"的响应结果
内容：终端截图，AI 返回前端的启动命令、监听端口和访问地址（如 http://localhost:xxxx），运行流畅无报错
-->

页面也出来了：

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/d6c4e367d627051b846fcc9382b22ec5_MD5.jpg" style="display: block; width: 800px;" alt="前端页面浏览器截图">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/d6c4e367d627051b846fcc9382b22ec5_MD5.jpg
用途：展示前端页面在浏览器中正常加载的效果
内容：浏览器截图，Spring AI Alibaba Admin 的管理界面（登录后的管理控制台，能看到 Prompt/Dataset/Evaluator/Trace 等功能模块）
-->

这一步最能看出这套提示词的好处：一句话追加，AI 自己找到前端目录、装依赖、起服务、给地址，工程师全程没动手。

### 6.4 review 要点

应用跑起来不算完，要按下面几点确认真的活着：

| review 要点             | 通过标准                                                                   |
| --------------------- | ---------------------------------------------------------------------- |
| 日志没报 **ERROR**        | 启动日志扫一遍，没有 ERROR 级别输出。WARN 可以接受，ERROR 必须处理掉                            |
| **端口监听**正常            | `lsof -i` 或 `deps-status.sh` 确认应用监听的端口（如 8080）真的在监听，不是进程起了但端口没绑上       |
| **管理界面**能打开           | `http://localhost:8080` 浏览器能打开。页面可能要登录，能看到登录页就算活着——说明应用起来、路由通、前端资源也加载了 |
| **Java 版本**一致         | 项目要 17 就是 17，不能是 21 或 11。版本不一致时某些字节码操作库（如 ASM、CGLIB）会直接报错              |
| **Maven 依赖**能拉到、版本对得上 | 没有"cannot resolve"之类的依赖缺失，拉的版本和 `pom.xml` 里 lockfile 一致                |
| **端口**未被占用            | 8080、8848 这些常用端口启动前没被别的服务占。被占了要么换端口、要么先停掉占用的服务                         |
| **配置文件**齐全            | `application-dev.yml` 等环境配置文件存在。缺了要从 `application.yml` 拷一份改，不能裸跑       |
| **Nacos 配置**已推送       | 应用启动时能从 Nacos 拉到配置。命名空间、配置 group、data id 都要推上去，拉不到配置应用会启动失败            |

前两条是产出标准，后六条是 AI 自主修复过程中可能踩的坑——对应 6.2 那五类常见错误。日志里看到对应错误时按这套排查。

## 7. Step 4：接口冒烟

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/3cad1717f6f63e2de978576460524480_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

应用跑起来、前端也通了，最后一步：用 `curl` 跑几个核心接口，确认项目**真的活了**——不是进程活着，是业务能跑。

### 7.1 提示词

```
读 docs/api-list.md，挑 5 个最核心的接口（覆盖登录、Prompt、Dataset、Evaluator、Trace 几大模块），用 curl 跑一遍。
返回 200 算通过，返回错误的列出来。
最后输出一份 docs/smoke-test-result.md。
```

这条提示词的关键在第一句：<span style="color: red; font-weight: bold;">点名 `docs/api-list.md` 作为接口清单来源</span>——这份清单是<span style="color: red; font-weight: bold;">此前梳理好的接口清单</span>，里面是这个项目对外暴露的所有接口。AI 从这份清单里挑，不会凭空臆造、也不会漏掉核心模块。

第二句关键："<span style="color: red; font-weight: bold;">覆盖登录、Prompt、Dataset、Evaluator、Trace 几大模块</span>" —— <span style="color: red; font-weight: bold;">强制覆盖核心业务路径</span>，而不是挑几个最简单的 GET 凑数。这一点 7.3 的 review 会再强调。

### 7.2 产出

`docs/smoke-test-result.md`。跑完这一步，项目算"真的活了"——<span style="color: red; font-weight: bold;">不是 mvn 启动成功就算活，是核心接口能正常响应业务请求才算活。</span>

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/a58722fcff4914619ef0814872dca5de_MD5.jpg" style="display: block; width: 800px;" alt="5 接口冒烟结果">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/a58722fcff4914619ef0814872dca5de_MD5.jpg
用途：展示 5 个核心接口的 curl 冒烟结果
内容：终端输出截图，登录、Prompt、Dataset、Evaluator、Trace 五个核心接口的 HTTP 响应都返回 200，结果汇总到 docs/smoke-test-result.md
-->

然后看一眼数据库，表都建好了 —— 应用第一次启动时按 entity/model 定义自动建的表，说明 ORM 这条链路也通了。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/1f73cf547156296f1f98d475db6d96e6_MD5.jpg" style="display: block; width: 800px;" alt="数据库表自动建好">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/1f73cf547156296f1f98d475db6d96e6_MD5.jpg
用途：验证接口冒烟后数据库表已自动创建
内容：数据库客户端截图，显示 MySQL 中的表已按 entity/model 定义建好（如 prompt、dataset、evaluator、trace 相关表）
-->

### 7.3 review 要点

`smoke-test-result.md` 出来不能直接信"全绿"，要按下面三点 review：

| review 要点                       | 通过标准                                                                                                                       |
| ------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| **选的接口真的核心**                    | 不是挑了几个最简单的 GET 接口凑数（比如 `/health`、`/version` 这种）。要覆盖登录、Prompt、Dataset、Evaluator、Trace 五大模块各一个，每个模块挑最能代表业务的一个接口              |
| **返回结构**和 `docs/api-list.md` 一致 | 接口返回的字段要和此前梳理好的接口清单对得上——字段名、字段类型、嵌套结构都不能跑偏。AI 不应该改写返回结构来"凑通过"                                                              |
| **错误接口**要标出来                    | 如果有接口返回错误，AI 是不是标出来了——不要为了"全绿"故意挑能过的。<span style="color: red; font-weight: bold;">真实项目里冒烟就全绿的概率很低，看到全绿反而要警惕是不是挑得太保守</span> |

## 8. 沉淀为长期资产与小结

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/2029df22c36aeb41063588ef231f4a54_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

四步跑完，环境起来了。但这一次跑的过程不只是为了一个能跑的项目，更要把过程沉淀成长期资产——下次不重复踩坑，下个新项目一键复用。

### 8.1 最终产物清单

先看四步跑完后，手里多出的一份资产清单：

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

这份清单里九个产物分两个去向：

| 去向 | 产物 | 目的 |
|------|------|------|
| 给团队 | `install-log.md` + `startup-log.md` 整理成 `docs/setup-guide.md` | 下个新人来照着跑，十分钟搭起环境 |
| 沉淀成 SKILL | 整套流程做成 `.claude/skills/env-bootstrap/SKILL.md` | 下次任何新项目（或重置环境）一键复跑 |

下面分别看这两个产出怎么做。

### 8.2 给团队的 setup-guide.md

把 Step 2A 的 `install-log.md` 和 Step 3 的 `startup-log.md` 整理成一份给新人看的 `docs/setup-guide.md`。这件事让 AI 做：

```
基于 scripts/install-log.md 和 docs/startup-log.md，
整理一份给新人看的 setup-guide.md，
包含：前置条件、装中间件步骤、启动命令、常见踩坑、验证清单。
保存到 docs/setup-guide.md。
```

为什么要让 AI 做这件事？因为两份日志是"过程记录" —— 按时间顺序记下装了什么、踩了什么坑、怎么修的。但新人需要的是"操作手册" —— 按步骤告诉我先做什么、再做什么、卡在哪该怎么办。**过程记录喂给 AI，产出一份操作手册**，<span style="color: red; font-weight: bold;">正好把"踩坑经验"转化为"可复用资产"。</span>

类比传统协作：这就像资深工程师把自己电脑里乱七八糟的 `~/notes` 整理成一份新人 onboarding 文档——自己做要花两小时，让 AI 做只要两分钟。

### 8.3 挖成 env-bootstrap SKILL

给团队的 `setup-guide.md` 解决的是"本团队新人能上手"。但还有一类场景没覆盖：**下个新项目来了怎么办**？总不能每次都把这套提示词拷一遍重跑。

解法是把整套"依赖盘点 → 装中间件 → 启动 → 冒烟"流程做成一个 SKILL，下次任何新项目一键复跑：

```
基于这次环境搭建的全流程，给我生成一个 env-bootstrap 的 SKILL，
保存到 .claude/skills/env-bootstrap/SKILL.md。
触发场景：新接手项目、重置环境、定期验证环境健康。
步骤：依赖盘点 → 装中间件 → 启停脚本 → 编译启动 → 接口冒烟。
allowed-tools 限制到 Read, Bash, Write。
```

最后一句 `allowed-tools 限制到 Read, Bash, Write` 不可省——SKILL 是一个"能自己执行操作"的封装，**不给它写文件和执行命令之外的工具**，避免 SKILL 越权干别的事。这是 Claude Code SKILL 设计的标准约束。

跑完这一步，挖到的 SKILL 数量从 1 个变成 2 个：

| SKILL            | 来源                       | 解决的问题                          |
| ---------------- | ------------------------ | ------------------------------ |
| `docs-auto-sync` | docs-auto-sync SKILL 那一篇 | **文档自动同步** —— 代码改了文档跟不上，反复做没沉淀 |
| `env-bootstrap`  | 本篇                       | **环境一键拉起** —— 新项目搭环境半天起，反复做没沉淀 |

两个都是"**反复做但没沉淀**"**的高价值流程**——这正是判断一个动作该不该挖成 SKILL 的核心标准：<span style="color: red; font-weight: bold;">如果一个流程每周都要重复跑一遍，且每次都要重新想提示词，就值得封装成 SKILL</span>。

### 8.4 小结

本篇核心一句话：**让 AI 当环境工程师，把环境搭建从"半天到一天的折磨"压到"半小时跑完"**。

实现路径是四步法：

```
依赖盘点 → 本地安装 + 启停管理（主线，Docker 备选）→ 编译启动 → 接口冒烟
```

四步能跑顺，靠的是贯穿全程的三个机制：

| 机制 | 作用 |
|------|------|
| 自主修复 | AI 看报错、判断原因、自己修、重试——工程师每个错误都不用介入 |
| 3 次失败兜底 | 同一错误连续修 3 次还不行，AI 停下汇报——防止死循环消耗几小时 |
| 资产沉淀 | 踩坑过程写进 `install-log.md` / `startup-log.md` / `smoke-test-result.md`，最终汇成 `setup-guide.md` 和 `env-bootstrap` SKILL，成为团队和自己的长期资产 |

**实战提醒**：四步法不是无脑跑通。安装依赖这一步（Step 2A）要重点关注——<span style="color: red; font-weight: bold;">本地环境的差异性会导致安装可能失败，需要给 AI 提供帮助</span>（比如给 sudo 权限、提供 brew tap 源、手动确认某些破坏性命令）。<span style="color: red; font-weight: bold;">AI 自主修复能搞定 80% 的问题，剩下 20% 涉及系统权限和机器特性，需要工程师介入授权。</span>这不是提示词的问题，是环境本身的特性。

最后放一张四步法的整体流程图，作为全文收尾的视觉锚点——下次想用这套方法时，看这一张图就能想起整个流程。

<img src="imgs/aicmigr-13-safeguard-01-build-guardrails-env/e23a1e8a67a1c73890489d5a14b92e3e_MD5.jpg" style="display: block; width: 800px;" alt="四步法整体流程图">

<!--
图片内容说明
路径：imgs/aicmigr-13-safeguard-01-build-guardrails-env/e23a1e8a67a1c73890489d5a14b92e3e_MD5.jpg
用途：展示四步法（依赖盘点 → 安装启停 → 编译启动 → 接口冒烟）整体流程图
内容：Mermaid 风格的流程示意图，把环境搭建四步法串成一条主线，标出每一步的产出物（env-checklist.md / install-deps.sh / deps-*.sh / startup-log.md / smoke-test-result.md）和兜底机制（3 次失败兜底）
-->



