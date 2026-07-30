---
title: 传统项目迁AI 15：构建护栏 - 补充兜底测试
author: fangkun119
date: 2026-07-04 15:00:00 +0800
categories: [AI编程, 传统项目AI编程]
tags: [AI编程, 传统项目AI编程]
pin: false
math: true
mermaid: true
image:
  path: imgs/aicmigr-15-safeguard-03-fallback-tests/cover.jpg
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
aicmigr-15-safeguard-03-fallback-tests
传统项目迁AI 15：构建护栏 - 补充兜底测试
-->

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/95cb607bf3ebdc747bd6bda410ed0f95_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

老项目的 `src/test/` 目录大概率空空荡荡——你知道应该补测试，但这件事过去性价比太低，永远排不到迭代里。AI 把写测试的成本从半天压到分钟级之后，老项目补测试第一次有了可执行的解法。本篇承接测试缺口清单（`docs/test-gaps.md`），回答三个问题：<span style="color: red; font-weight: bold; background-color: yellow;">该不该补、要补哪些、怎么补</span>。

如果项目原本就有测试，按缺口清单一项一项交给 AI 补即可。但如果项目几乎没测试、或现有测试根本不能跑，"补"的工作量大到让人退缩——本篇正是为这种情况写的：<span style="color: red; font-weight: bold;">把"补测试 + 配 CI"这件过去性价比极低的事，拆成一套 AI 时代可控、可批量、可守护的标准流程。</span>

## 1. 为什么老项目没测试，AI 时代为什么必须补

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/503bd61cc082292835ef89cf51cd0374_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 1.1 老项目的 src/test/ 为什么总是空的

打开一个跑了五年的老项目，`src/test/` 目录大概率空空荡荡。PR review 时常把"建议加个测试"打在评论里，转头就忘。

你可能会问：开发者不懂 TDD 吗？不懂测试的重要性吗？

都懂。真正的原因不是态度，是账算不过来。一个 controller 写一组测试要半天，整个项目补完一个月都不够；收益要等到下一次改造才能兑现，老板不催，自己也不想干。"该补测试但没补"是行业常态，不是个别团队的惰性。

要解决老项目补测试这件事，道德文章再多写一万遍也没用。真正改变局面的，是 AI 把这件事的成本结构彻底改写——这件事第一次有了可执行的解法。

### 1.2 四条根因：为什么过去补不动

把"补不动"这件事拆开，每个维度都被卡住了，一共四条根因：

| 根因 | 具体表现 | 为什么难破 |
|------|---------|-----------|
| 业务价值感弱 | 测试不直接产出业务功能 | 老板不催，自己也排不到迭代里 |
| 单点成本高 | 一个 controller 写一组测试要半天 | 全项目补完一个月都不够 |
| 反馈周期长 | 写完测试要等下一次改造才能验证价值 | 远期收益难以即时变现 |
| 风险偏好错位 | 不补测试短期看不出问题，补测试却要立即付出成本 | 人天然倾向拖延 |

四条加在一起，最终状态就是"知道应该有但永远没有"。

这是结构性问题，不是态度问题。传统软件工程里有个现成的类比——**技术债**。补测试就是一笔典型的"高息、远期、债主不催"的债：

- **利息高**：今天欠下的每一行没补的测试，未来改造时都要连本带息还——改坏了发现不了，回头排查的时间是当初写测试的几倍。
- **期限远**：还款日不在本迭代，而在下一次改造、下一次线上事故——远到人很难现在就感到痛。
- **债主不催**：老板盯的是业务功能交付，测试不交付没人追责；不像线上 bug 那样会立刻有人来敲你的门。

利息高、期限远、债主不催，这三条加起来，人自然越欠越多。给团队讲一万遍"测试很重要"没用，因为成本账算不过来。

### 1.3 AI 重写了这张成本账

AI 出现之后，这张账被彻底改写。<span style="color: red; font-weight: bold;">"写一组测试"从半天压缩到分钟级</span>，AI 还能基于现有代码反推预期行为、自动跑测试看失败、自动调整失败用例。

| 维度         | AI 出现前     | AI 出现后          |
| ---------- | ---------- | --------------- |
| 单组测试编写时间   | 半天起步       | 分钟级             |
| **反推预期行为** | 靠人读代码 + 文档 | AI 基于代码反推，能跑测验证 |
| 调整失败用例     | 人工 debug   | AI 自动迭代调整       |
| 全项目补完可行性   | 一个月都不够     | 几天内可完成 P0 缺口    |

这张表里最关键的一行不是"分钟级"，而是最后一行——**全项目补完可行性**。

为什么这一行最关键？因为过去老项目补测试之所以做不下去，根因不是单组测试慢，而是补到一半就烂尾：人力跟不上，优先级一调整就被砍掉。单组测试再快，只要整件事还是"无限工程"，就永远启动不了。

"无限工程"和"有限工程"的差别是质变，不是量变。过去补测试是西西弗斯推石头 —— 推到一半滚下来，下次从头再来；现在它是可拆解、可排期、可交付的工程项目。四条根因里"单点成本高"和"反馈周期长"被 AI 直接抹平，"风险偏好错位"也跟着松动——成本降到接近零，拖延的收益就消失了。

<span style="color: red; font-weight: bold;">AI 把整件事从"无限工程"压成"几天能干完的有限工程"，这才是范式转变。</span>

回头看四条根因——单点成本高、反馈周期长、业务价值感弱、风险偏好错位——叠加起来让人倾向拖延。AI 把写一组测试的成本从半天压到分钟级，成本账这才第一次算得过来。<span style="color: red; font-weight: bold;">AI 写测试 = 不再有借口拖延。</span>以前不补是因为成本太高，现在 AI 把成本降到零。从今天起，改造前没护栏，不能再怪工作量——只能怪自己的判断。

但 AI 也有它的问题：默认会"大而全"地补一堆不必要的测试——简单 CRUD、getter/setter 全都给你补上，看着热闹，实际是噪音。所以<span style="color: red; font-weight: bold;">光把 AI 放出来还不够，还要控制它补的范围和节奏</span>。怎么控制，是接下来几章要讲的核心。

### 1.4 三个问题：接下来三章分别回答

把"老项目补测试"这件事拆开，无非三个问题。接下来三章分别回答它们：

| 问题 | 简答 |
|---|---|
| **该不该补？** | 不按覆盖率指标判断，<span style="color: red; font-weight: bold;">按改造路径判断</span>。<span style="color: red; font-weight: bold;">即将改的</span>、<span style="color: red; font-weight: bold;">改完没法验证的</span>、<span style="color: red; font-weight: bold;">涉及核心业务的</span>，必须补；改个 log 输出这种小改动可以不补。 |
| **补哪些？** | 按价值排四档优先级。<span style="color: red; font-weight: bold;">改造路径上的 Characterization Test 最值钱</span>，简单 CRUD 性价比最差，可以不补。 |
| **怎么补？** | <span style="color: red; font-weight: bold;">两步走</span>加一道 <span style="color: red; font-weight: bold;">CI 护栏</span>。让 AI 出补测试计划，再一批一批补（每批 1-3 个），最后配 CI 让测试持续自动跑。 |

这三个问题答完，老项目补测试就从"知道应该做但永远做不完"，变成"几天能做完，且做完就有可信的护栏"。

## 2. 该不该补：决策矩阵与够用就停

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/eb289874e8586de8f84c2340896e6f84_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 2.1 决策矩阵：不是所有项目都要立刻补

动手补测试之前，先把"该不该补"这个问题回答清楚。这一步错了，后面补再多也是浪费——补完发现项目下个月下线，前面的人力全打水漂。

把项目里遇到的改造任务摆出来，按下面这张表对号入座：

| 情况                         | 判断依据                | 是否必须补   |
| -------------------------- | ------------------- | ------- |
| <span style="color: red; font-weight: bold;">即将动手改造的接口或链路目前没测试</span>      | <span style="color: red; font-weight: bold;">改造完之后没办法验证有没有改坏</span> | <span style="color: red; font-weight: bold;">必须补</span> |
| <span style="color: red; font-weight: bold;">改造涉及核心业务逻辑</span>（计费、权限、数据写入） | <span style="color: red; font-weight: bold;">出错代价大，不补就是裸奔</span>    | <span style="color: red; font-weight: bold;">必须补</span> |
| AI 改了什么、改没改坏全凭运气           | <span style="color: red; font-weight: bold;">缺少客观验证手段</span>        | <span style="color: red; font-weight: bold;">必须补</span> |
| 改造范围非常小（改一个 log 输出、改一个文案）  | 出错影响有限              | 可暂时不补   |
| 改造涉及的代码已被高质量集成测试覆盖         | 已有兜底，无需重复           | 可暂时不补   |
| 项目即将下线或重写，改造只是临时维持         | 投入换不回长期价值           | 可暂时不补   |

三条"必须补"指向同一件事：<span style="color: red; font-weight: bold;">改造的后果必须可验证</span>。三条"可暂时不补"指向另一件事：<span style="color: red; font-weight: bold;">投入产出比明显失衡</span>。记住这两个收敛点，决策就不会跑偏。

### 2.2 决策流程图：判断顺序比结论更重要

把上面的判断收成一张流程图，照着走就行：

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/ec1cebbff97f3985ccd532a9942f9dd1_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
flowchart TD
    A[改造任务] --\> B{是否即将改<br/>且无测试?}
    B -- 否 --\> Z[可暂时不补]
    B -- 是 --\> C{是否核心业务逻辑?<br/>计费/权限/数据写入}
    C -- 是 --\> Y[必须补]
    C -- 否 --\> D{改造范围是否极小?<br/>log/文案}
    D -- 是 --\> Z
    D -- 否 --\> E{代码是否已被<br/>高质量集成测试覆盖?}
    E -- 是 --\> Z
    E -- 否 --\> Y
-->

这张图的关键不是结论"必须补 / 可暂时不补"，而是**判断顺序**：<span style="color: red; font-weight: bold;">先看是否即将改且无测试，再看是否核心业务，最后看是否已有兜底——顺序错了一样会做错决策。</span>

你可能会问：为什么不直接看覆盖率指标？因为<span style="color: red; font-weight: bold;">覆盖率不是第一性问题</span>。有人一上来就盯"项目覆盖率够不够 80%"，这是把判断顺序搞反了——一个 80% 覆盖率的项目，如果即将改造的接口恰好在那 20% 没被覆盖的地方，照样是裸奔。决策应该从"这次改造会动到什么"出发，而不是从全局指标出发。

### 2.3 够用就停：覆盖率不是自助餐

补测试这件事最容易掉的坑，是把它当成自助餐——看到什么补什么，恨不得吃到扶墙。但<span style="color: red; font-weight: bold;">老项目改造不是 80% 覆盖率自助餐，是按需点菜</span>：当前这次改造会动到的关键节点有兜底就行，其他先放着。

这就是「够用就停」心法落到补测试上的具体含义。为了把它讲透，先把两种目标分清楚：

| 目标类型 | 描述 | 工程性质 |
|---------|------|---------|
| 错误目标 | 追求 80% 测试覆盖率 | 无限工程——盯着这个指标永远补不完，永远启动不了改造 |
| 正确目标 | 让改造路径上的关键节点都有兜底 | 有限投入——节点数有限，补完就停 |

<span style="color: red; font-weight: bold;">覆盖率是无限工程，只要盯着它就永远补不完；关键节点兜底是有限清单，补完就停。</span>两者差别巨大：一个是把人拖死的无底洞，一个是几天能干完的清单。

回到 `docs/test-gaps.md`：<span style="color: red; font-weight: bold;">里面的 P0 项就是流程图判出来的"必须补"，P1 项对应"可暂时不补"。</span>本章的决策到此为止，下一步是按 P0 清单排顺序——这正是第 3 章要回答的"补哪些"。

## 3. 要补哪些：价值优先级四档

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/3377fd44978a712ddaa8de65422fdbb9_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

### 3.1 四档优先级总览

「该补」判断完，下一个问题是「先补哪些」。<span style="color: red; font-weight: bold;">同样的工作量投到不同类型的测试上，价值能差几倍</span> —— 核心链路的兜底测试救一次线上事故就回本，给 getter/setter 补的单元测试可能永远跑不到分支。

把测试按「改造场景下的价值密度」分成四档：

| 优先级 | 测试类型                                                                                                                    | 典型场景                    | 性价比                                                    |
| --- | ----------------------------------------------------------------------------------------------------------------------- | ----------------------- | ------------------------------------------------------ |
| 第一档 | <span style="color: red; font-weight: bold;">改造路径上的 Characterization Test</span>                                        | 即将改的接口 / 链路             | <span style="color: red; font-weight: bold;">极高</span> |
| 第二档 | <span style="color: red; font-weight: bold;">覆盖核心数据写入操作</span>的<span style="color: red; font-weight: bold;">集成测试</span> | 登录、创建、写入等数据进库链路         | <span style="color: red; font-weight: bold;">高</span>  |
| 第三档 | **业务逻辑复杂**的单元测试                                                                                                         | 算分、状态流转、权限校验            | **中**                                                  |
| 第四档 | 简单 CRUD 的单元测试                                                                                                           | getter/setter、简单 SELECT | 低（可不补）                                                 |

记住一句口诀，排序就锁死了：

> 改造路径上的 Characterization > 核心链路集成 > 复杂逻辑单元 > 简单 CRUD（可不补）

<span style="color: red; font-weight: bold;">第一档最反直觉、也最容易被漏掉</span>，单独讲透；后三档合并说清差异。

### 3.2 第一档：改造路径上的 Characterization Test

#### (1) 用「拍 X 光片」理解它

假设接下来要改 `PromptService.create()`。最危险的做法是直接动手改——改完不知道哪些行为变了、哪些没变。安全做法是先给它拍一张「X 光片」：<span style="color: red; font-weight: bold;">让代码在当前状态下跑一遍，把实际的输入输出原样记录下来作为改造前的基线。改造完成后再拍一次，两张片子一对比，行为偏移立刻现形。</span>

这就是 Characterization Test（特征化测试）。它和普通测试最大的区别在于**断言的来源**：

| 测试类型                      | 断言来源           | 典型表述                    |
| ------------------------- | -------------- | ----------------------- |
| 普通测试                      | 需求文档或业务直觉      | 「我觉得这里应该返回 100」         |
| <span style="color: red; font-weight: bold;">Characterization</span> Test | <span style="color: red; font-weight: bold;">代码实际跑出来的结果</span> | 「代码现在确实返回 100，我先锁住这个事实」 |

在老项目里，这个区别是救命的。老代码的「应该做什么」经常没人说得清——文档丢了、原作者走了、需求改了十版。但「现在实际做什么」是确定的、可观测的。<span style="color: red; font-weight: bold;">锁住「现在」，改造后只要行为不变就放心。</span>

#### (2) 它的出处

这个方法不是新发明。Michael Feathers 在《Working Effectively with Legacy Code》里系统讲过这套思路——面对无法测试的遗留代码，先用 Characterization Test 把现有行为钉死，再在其保护下做改造。05 篇业界综述专门讨论过这本书，这里只取它最核心的一个思想。

#### (3) 三步做法

整套流程压成三步：

1. **先跑一遍记录实际行为**：让现有代码在真实输入下运行，把实际输入输出原样记下来，不掺任何主观判断。
2. **把实际行为转成断言**：把上一步记录的结果直接写成测试断言——<span style="color: red; font-weight: bold;">代码现在返回什么，断言就写什么。</span>
3. **改造前后各跑一次做对比**：改造前的断言构成基线，改造后再跑同一份测试，任何行为偏移都会以测试失败的形式暴露出来。

最关键的是第 2 步：断言只能来自「代码实际跑出来的结果」，不能来自「AI 觉得应该是多少」。AI 写测试有一个隐性偏差——它读了代码会猜业务意图，然后按「应该」补断言。但老代码经常和「应该」不一致，那些不一致往往就是历史包袱、对接方依赖、改造禁区。<span style="color: red; font-weight: bold;">AI 按「应该」写出来的测试一跑就失败，反而让人误以为代码有 bug。</span>这条铁律在最后一章还会展开。

### 3.3 后三档：集成、单元与简单 CRUD

第一档之外，剩下三档差异主要在「测的粒度」和「价值密度」上。

#### (1) 第二档：核心数据写入的集成测试

登录、Prompt 创建、Dataset 写入这种「数据进库」的核心链路，要的是集成测试，不是单元测试。集成测试覆盖一条完整链路（HTTP → Service → DB），<span style="color: red; font-weight: bold;">改造时一条集成测试比十个单元测试更能兜底</span>。

原因很现实：<span style="color: red; font-weight: bold;">AI 改了 Service 层但 DAO 层挂了，集成测试一跑就发现；单元测试可能各自都过，合起来才暴露问题——单看单元测试会得到「全绿」的假象。</span>

#### (2) 第三档：业务逻辑复杂的单元测试

算分、状态流转、权限校验这种纯逻辑代码值得加单元测试。<span style="color: red; font-weight: bold;">逻辑分支多，集成测试难以覆盖每一条分支</span>，<span style="color: red; font-weight: bold;">单元测试能精准命中</span>边界条件。

价值不如前两档直接，<span style="color: red; font-weight: bold;">但面对「改一个 if 把所有状态都改错」这类事故，单元测试是最后一道防线</span>。

#### (3) 第四档：简单 CRUD 的单元测试，可不补

getter/setter、简单 SELECT 这类代码放最后，甚至不补。理由有二：

- **AI 改这种代码出错概率本来就低**
- **加测试的维护成本可能比代码本身还高**

把第四档剔除，是为了把有限注意力集中到前三档——这是「够用就停」心法在优先级排序上的落地。

## 4. 怎么补：两步走与节奏护栏

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/7ec7ffbf37344d7162039543fe94cf09_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

该补的、补哪些都讲完了，最后一步是「怎么补」。问题在于：让 AI 别一次性补一二十个测试，大而全必翻车。<span style="color: red; font-weight: bold;">让人能逐批 review、确认每一批都是可信资产</span>（不一口气看十个）才是正确的方法。

答案浓缩成一句话：**两步走 + 一道节奏护栏**——先让 AI 出一份可执行的分批计划，再按「一批 1-3 个、跑通一批 review 一批」的节奏推进。

### 4.1 整体流程

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/65a5216fa65f9049819f9bf8640b568c_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!---
flowchart LR
    A[14篇<br/>test-gaps.md] --\> B[Step 1<br/>AI 出补测试计划]
    B --\> C[docs/test-plan.md]
    C --\> D[Step 2<br/>第 1 批 1-3 个]
    D --\> E{跑通 + review}
    E -- 通过 --\> F[第 2 批<br/>参考前一批风格]
    E -- 失败 --\> G[修代码/修测试/标记]
    G --\> D
    F --\> E
    E -- 全部 P0 完成 --\> H[配 CI 护栏<br/>进入第 5 章]
-->

整条流程的关键在中间那个 `E{跑通 + review}` 判断节点——它是节奏护栏的本体。每补完一批必须经过这一关，通过才能进下一批；失败就回到 Step 2 重补，不带着遗留问题往下走。流程图的起点是 14 篇产出的 `docs/test-gaps.md`，终点是 CI 护栏（下一章会讲）。两端之间，整个补测试过程被切成可控的小循环。

### 4.2 Step 1：让 AI 出补测试计划

这一步把 14 篇的 `test-gaps.md`（缺口清单）拆成可执行的批次清单 `docs/test-plan.md`。本质上是<span style="color: red; font-weight: bold;">把「要补什么」翻译成「按什么顺序、分几批、每批几个」</span>——给 AI 一份明确的施工图，而不是让它自由发挥。

提示词：

```
基于 docs/test-gaps.md，把 P0 缺口拆成多批，每批 1-3 个（最好 1 个），
给我一份补测试计划。每批写：批次号、测试类型（Characterization
Test / 集成测试 / 单元测试）、覆盖的核心链路、预期工作量。

按"改造路径上的 Characterization > 核心链路集成 > 复杂逻辑单元"的顺序排批次。
简单 CRUD 不进计划。

输出用表格总结。保存到 docs/test-plan.md。
```

在 Spring AI Alibaba Admin 上跑出来的实际产出（即 `docs/test-plan.md`）长这样：

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/f4c8dd7d3a6e172f75c67ef668cbe185_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-15-safeguard-03-fallback-tests/f4c8dd7d3a6e172f75c67ef668cbe185_MD5.jpg
用途：展示 AI 基于 test-gaps.md 拆出的补测试计划（docs/test-plan.md）的实际输出效果
内容：补测试计划表格截图，每行包含批次号、测试类型（Characterization Test / 集成测试 / 单元测试）、覆盖的核心链路、预期工作量等字段，按"改造路径上的 Characterization > 核心链路集成 > 复杂逻辑单元"顺序排列
-->

**实战解读**：从表格的第二列（测试类型）就能读出很多信息——哪些批次锁的是「现在行为」（Characterization Test）、哪些是端到端集成（集成测试）、哪些是纯逻辑分支（单元测试）。这一列是后续 review 时最重要的判断依据。对照着它，开发者能一眼看清整份计划的「成分构成」是不是按价值优先级排的。

拿到 `docs/test-plan.md` 后，重点核对三条：

- <span style="color: red; font-weight: bold;">每批严格 1-3 个，最好就 1 个</span>。批次越小越可控，超出范围的让 AI 重新拆。
- <span style="color: red; font-weight: bold;">批次顺序按价值优先级</span>。Characterization Test 应该排在前面，符合第 3 章四档顺序。
- <span style="color: red; font-weight: bold;">简单 CRUD 真的没进计划</span>。AI 容易「贴心」地把 CRUD 也补上，重点扫一遍核心链路列，确认没有 `GET /xxx` 这类纯查询接口混进来。

这三条本质都在反同一件事：**AI 默认会「大而全」地补一堆不必要的测试**。<span style="color: red; font-weight: bold;">计划阶段就把多余项剔除，比执行阶段再返工省事十倍。</span>

### 4.3 Step 2：让 AI 一批一批补

这一步的核心铁律只有一句：**不要让 AI 一口气把所有批次补完**。<span style="color: red; font-weight: bold;">一批补完跑通、人 review 通过，再开下一批。</span>

第 1 批的提示词：

```
按 docs/test-plan.md 的第 1 批，给项目补出对应的测试。

对 Characterization Test 类型：先跑一次现有代码记录实际行为，
再把行为转成断言。不要凭"应该是什么"写断言，凭"实际是什么"写。

对集成测试类型：需要真实启动应用 + 数据库。
用 SpringBootTest 的方式起完整 context 跑。

补完跑一遍 mvn test 确保都通过。

输出用表格总结每个测试覆盖的场景、预期结果、实际跑出来的状态。
```

<span style="color: red; font-weight: bold;">拿到 AI 补出的测试后，逐条 review</span>。这一步是整个补测试流程里最关键、也最容易翻车的环节，重点盯三件事——这是全文最重要的 review 清单。

第一，<span style="color: red; font-weight: bold;">测试是不是测了「现在实际做什么」</span>。这是 Characterization Test 的灵魂。如果看到 AI 写的断言是「amount 应该等于 100」，<span style="color: red; font-weight: bold;">必须追问「100 是从哪来的？是跑代码跑出来的，还是猜的？」</span>。AI 经常贴心地用业务直觉补断言，这反而最危险——AI 按「应该」写出来的测试一跑代码就失败，让人以为代码有 bug，实际上是测试错了。老代码的「应该做什么」经常没人说得清，但「现在实际做什么」是确定的、可观测的；锁住「现在」，改造后只要行为不变就放心。

第二，<span style="color: red; font-weight: bold;">测试覆盖的场景对不对</span>。AI 容易补一堆 happy path、忽略 edge case。如果 14 篇的 `test-gaps.md` 里某条说「测试空入参的处理」，要确认这条真的有对应测试，不是被 AI 默认 skip 掉了。

第三，<span style="color: red; font-weight: bold;">测试都能跑通吗</span>。失败的测试不能进下一批。要么修代码、要么修测试、要么承认这条暂时跑不了先标记，三选一。

review 通过后再开第 2 批。提示词稍微调整：<span style="color: red; font-weight: bold;">让 AI 参考第 1 批的风格继续写——整套测试一致性强，后续维护成本低</span>：

```
按 docs/test-plan.md 的第 2 批补测试，
参考第 1 批已经跑通的测试风格，保持一致。
其他要求同前。
```

第 2 批起，除 review 三件事之外，还要盯住三条一致性：测试类与方法的<span style="color: red; font-weight: bold;">命名风格</span>统一；Characterization Test 的<span style="color: red; font-weight: bold;">断言流程</span>仍走「先跑代码 → 记录实际 → 再转断言」，不退回「应该」；第 1 批用到的测试辅助类与 Setup/TearDown <span style="color: red; font-weight: bold;">优先复用</span>，避免重复造轮子。

这套流程在 Spring AI Alibaba Admin 项目上实际跑过一遍：它具备完整的 Spring Boot + Spring AI 链路（Prompt、Dataset、Service、DAO），但几乎没测试、没 CI，是典型的老项目样本。把第 1 批提示词交给 AI，拿到产出后按 review 三件事逐条核对；通过后再开第 2 批，盯三条一致性。两批跑下来，节奏就锁定了——剩下的批次照同一节奏推进即可。实际跑出的测试结果汇总，建议读者自行执行提示词验证，本篇不再一一贴出。

### 4.4 实战心得：方法论比一句话指令更重要

在 AI 与开发者不断来回的过程中，项目的信息越来越完整，AI 持续持有的项目上下文也越来越完整——这就是从 60 分到 80 分的过程。

这一章要传达的不是「一句话让 AI 搞定」的幻想，而是一套固定的方法论与可复用的步骤。重点在于让开发者能「抄作业」：拿着上面几段提示词，配合 `test-gaps.md` 与 `test-plan.md`，就能把 P0 缺口一步步补完。比起一句「帮我补测试」然后祈祷 AI 一次到位，这种结构化方法可重复、可 review、可兜底。

## 5. 让 CI 当兜底护栏

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/723f8634a609d50a16fe0030f6bfb46e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

测试补完了，但还差最关键的一步：让这套测试真正持续跑起来。这一步不做，前面几章的工作近乎白费。

### 5.1 测试不持续跑，等于白补

先看一个悲剧场景：某个团队花两周辛苦补了一套测试，跑通了，commit 进仓库，大家击掌庆贺，结束。三个月后某次改造把代码改坏了，测试明明能发现这个 bug，但没人主动跑测试，bug 就这么一路溜进了生产环境。

这就像给家里装了一套高级烟雾报警器，装完电池没装、也没接电源，摆在天花板上当装饰。真着火的时候，它和没装没有任何区别。

**测试不持续跑，等于白补。**

让测试持续跑的标准做法是 CI（Continuous Integration，持续集成）：每次 push 代码、每次提 PR，CI 系统自动跑全部测试，失败就拦着不让 merge。CI 就是那个不睡觉的代码门卫，7×24 小时盯着仓库。

### 5.2 AI 时代 CI 特别值得做

传统时代配 CI 是个脏活累活——查文档、对版本、调环境，开发者本能地拖延。AI 时代这件事的性价比结构彻底变了：

#### (1) CI 配置高度标准化

无论 GitHub Actions 还是 GitLab CI，配置文件就那么几样东西：

- 触发条件（哪些分支 push 触发）
- 运行环境（什么镜像、什么 JDK 版本）
- 跑什么命令（mvn test）
- 产物存哪（测试报告、覆盖率）

AI 写这种标准化配置文件特别准，因为它见过无数个类似的项目。开发者自己写要查文档查半天，AI 30 秒搞定。

#### (2) 是改造的长期复利：

今天花 30 分钟让 AI 写好 CI 配置，未来每一次 push、每一次同事 PR、每一次自己 merge，都自动跑一遍测试。一年下来这件事运行了上千次。<span style="color: red; font-weight: bold;">前期一次性投入 30 分钟，换长期上千次自动检查，性价比之王。</span>

#### (3) CI 让测试从自觉变强制

<span style="color: red; font-weight: bold;">靠自觉跑测试在团队里是不可持续的——deadline 紧的时候第一个被砍的就是测试。但 CI 失败 block merge，没人能跳过。强制比自觉可靠十倍。</span>

### 5.3 CI 落地三步法

CI 的落地不复杂，就三步：

| 步骤             | 目标                | 关键产物                                            |
| -------------- | ----------------- | ----------------------------------------------- |
| <span style="color: red; font-weight: bold;">分析现状</span>       | 扫描项目当前的 CI 配置（如有） | 现状分析表                                           |
| <span style="color: red; font-weight: bold;">写 workflow</span> | 基于现状生成完整 CI 配置    | `.github/workflows/test.yml` 或 `.gitlab-ci.yml` |
| <span style="color: red; font-weight: bold;">跑通</span> CI      | push 触发，定位并修复环境问题 | CI 通过的绿色徽章                                      |

动手之前先判断项目处于哪种状态，对症下药：

| 现状 | 处理方式 |
|------|---------|
| 项目有 CI 但没跑测试 | 补 test job 即可 |
| 项目完全没 CI | 从零建一份 |
| 项目有 CI 也跑了测试，但配置过期 | 升级配置 |

下面以 Spring AI Alibaba Admin 为样本走一遍三步法。这个项目走的是第二种情况——完全没 CI，从零建一份，最能体现完整流程。

### 5.4 实战：扫描 CI 现状

先把现状摸清楚。把下面这段提示词交给 AI：

```
扫一下项目里有没有现成的 CI 配置
（看 .github/workflows/、.gitlab-ci.yml、Jenkinsfile、circle.yml 之类）。

如果有，告诉我现在跑了什么、什么时候触发、有没有跑测试。
如果没有，告诉我项目代码托管在哪个平台，建议用哪种 CI。

输出用表格总结。
```

AI 扫完给出的结论是：这个项目根本没有任何 CI 配置。

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/fcfeac377c99861b30379f54e33fba8d_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-15-safeguard-03-fallback-tests/fcfeac377c99861b30379f54e33fba8d_MD5.jpg
用途：展示 AI 扫描项目 CI 状态后的分析结论，证明该项目尚未配置 CI
内容：AI 输出的扫描结果表格或文字说明，列出 .github/workflows/、.gitlab-ci.yml、Jenkinsfile、circle.yml 等位置均未发现 CI 配置，并给出推荐方案（建议使用 GitHub Actions 等）
-->

这个发现本身不意外，但值得停下来想一想：一个能跑、有人在用、还集成了 Spring AI 链路的项目，居然从来没配过 CI——这正是不少公司内部老项目的常态。<span style="color: red; font-weight: bold;">项目能跑不等于项目健康，「能跑」和「有护栏」之间差着整整一个工程化的距离。</span>

### 5.5 实战：写完整 CI workflow

现状摸清后，让 AI 直接生成一份能 commit 进仓库就跑的 workflow：

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

AI 产出的 `.github/workflows/test.yml` 分两段：

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/1431b27d9bfee26a85a293fc1b9ddadc_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-15-safeguard-03-fallback-tests/1431b27d9bfee26a85a293fc1b9ddadc_MD5.jpg
用途：展示 AI 生成的 GitHub Actions CI workflow 配置文件的前半部分（触发条件、运行环境、JDK 版本设置）
内容：.github/workflows/test.yml 配置文件截图，包含 on.push/pull_request 触发条件、jobs 定义、runs-on 运行环境、actions/setup-java 步骤、JDK 版本配置等 YAML 内容
-->

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/a3a91dd631c8a123411aaa60dc4b40bb_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

<!--
图片内容说明
路径：imgs/aicmigr-15-safeguard-03-fallback-tests/a3a91dd631c8a123411aaa60dc4b40bb_MD5.jpg
用途：展示 AI 生成的 GitHub Actions CI workflow 配置文件的后半部分（services 中间件、Maven 缓存、mvn test 执行、产物上传）
内容：.github/workflows/test.yml 配置文件续图，包含 services 中间件配置（如 MySQL 等）、actions/cache 缓存 Maven 依赖、运行 mvn clean test 命令、上传测试报告 artifact 等内容
-->

GitHub Actions Workflow 是项目工程化的「神器」，配置一次长期受益，值得深入学习。

把 workflow commit 进仓库前，有三个 review 重点：

- <span style="color: red; font-weight: bold;">触发条件对不对</span>：push 任何分支 + PR 是基础配置。如果团队规定「只有 develop 和 master 分支跑 CI 节省额度」，让 AI 调整触发分支列表。
- <span style="color: red; font-weight: bold;">中间件配置完整</span>：CI 跑集成测试需要 MySQL、Nacos 这些中间件起来。AI 应该用 `services` 字段把这些中间件配进去（GitHub Actions 原生支持），而不是默认期待中间件已经存在。
- <span style="color: red; font-weight: bold;">JDK 版本对齐</span>：项目要 17 别给写成 21。AI 容易自作主张用最新版本，要让它读 `pom.xml` 里 `<java.version>` 严格对齐。

### 5.6 跑通 CI 与常见失败排查

workflow commit 完，push 一次代码触发 CI，看是不是真的跑过了。第一次跑往往不会一次过，常见失败与处理方式：

| 常见失败 | 可能原因 | 处理方式 |
|---------|---------|---------|
| 中间件起不来 | CI 环境网络问题、版本不匹配 | 让 AI 核对 services 字段镜像版本 |
| Maven 镜像源访问不到 | 国内项目访问 maven central 慢 | 在 workflow 里配置国内镜像源 |
| 本地通过但 CI 上失败 | 环境差异（本地有缓存、CI 没有） | 让 AI 比对本地与 CI 的环境差异，必要时补 cache |

每个失败让 AI 自己 debug、自己改。<span style="color: red; font-weight: bold;">CI 跑通的那一刻，这套测试从「个人资产」变成了「团队级护栏」</span>——后续谁不小心改坏了什么，CI 立刻报错、PR 被 block。这是改造前能给自己最值钱的东西。

跑通之后，`test-gaps.md` 列出的 P0 缺口与本篇补出的测试，全部进入「自动守护」状态。

## 6. 最易翻车的铁律与速查清单

<img src="imgs/aicmigr-15-safeguard-03-fallback-tests/723f8634a609d50a16fe0030f6bfb46e_MD5.jpg" style="display: block; width: 800px;" alt="替换文字">

前面几章把方法论讲完了，最后这一章做两件事：把最容易翻车的三条铁律集中讲透，再把全文散落的速查项汇总成一份随时回来翻的清单。

### 6.1 铁律一：不大而全

<span style="color: red; font-weight: bold;">老项目补测试最大的风险之一，是让 AI 一次性补一二十个测试。</span>这件事违反前面反复强调的「不大而全」，落到执行上有两条硬约束：

- <span style="color: red; font-weight: bold;">数量上限是硬约束</span>：每批 1-3 个，最好 1 个。批次越小越可控——一批出问题影响面小，review 时也能逐个看清。
- <span style="color: red; font-weight: bold;">范围由 test-plan.md 限制</span>：计划阶段就把简单 CRUD 剔除，执行阶段不再放宽。AI 容易在执行中「贴心」地把没进计划的 CRUD 顺手补上，这一步要盯死。

### 6.2 铁律二：不一口气

让 AI 一口气补完一批，跑下来一半失败一半通过，问题就来了：开发者没法判断哪些测试是对的、哪些是 AI 瞎写的。<span style="color: red; font-weight: bold;">整批不可信，等于白做。</span>这条铁律的核心也是两条：

- **慢，但每一批都是可信资产**：跑通一批 review 一批，review 通过再开下一批。宁可慢，不要糊。
- **一次看一两个能仔细看**：<span style="color: red; font-weight: bold;">一次看一两个测试，能逐行追到代码、追到 `test-gaps.md`、追到提示词；一次看十个，人就只能扫一眼放过。</span>Review 质量与一次看的数量成反比。

### 6.3 Characterization Test 的灵魂：测"实际是什么"

Review 时要重点看一件事：**测试是不是测了「<span style="color: red; font-weight: bold;">现在实际做什么</span>」，而<span style="color: red; font-weight: bold;">不是</span>测了「AI 觉得应该做什么」**。

为什么这条单独拎出来讲？因为 AI 写测试有一个**隐性偏差**：它会用业务直觉补断言。

打个比方——让一个刚读了代码的工程师猜业务意图，他会按「这行代码应该做什么」来推断；AI 读了代码也会这么干。它读到 `orderService.create(order)`，会猜这肯定要做金额校验、库存扣减，然后按「应该」写断言：`amount 应该等于 100`。

但老项目的代码经常和「应该」不一致。那些不一致的地方，往往是**历史包袱、对接方依赖、禁区**——具体形态就是那些不能动、不能改、看起来怪但有人依赖的代码（10 篇 CLAUDE.md 的灵魂两节反复强调过这件事）。这些地方，「实际是什么」和「应该是什么」差着十万八千里。

AI 按「应该」写出来的测试，一跑代码就失败。开发者会以为代码有 bug，开始让 AI 改代码去迁就测试，结果改坏了不该动的东西——这正是前面所有护栏要防的灾难。

防止这个偏差的唯一办法：<span style="color: red; font-weight: bold;">强制 AI 先跑代码记录实际行为，再把实际行为转成断言</span>。提示词里那句——

> 不要凭「应该是什么」写断言，凭「实际是什么」写。

是最关键的一句。它把「猜业务意图」这条路堵死，强制走「先跑 → 记录 → 转断言」的流程。

### 6.4 失败率高时的排查路径

按上面三条铁律执行，偶尔还是会遇到：一批测试跑出来失败率高，让 AI 改了好几轮还是失败。这时按三步走排查：

1. **先停下来**——别让 AI 继续改代码。在「改代码 → 跑失败 → 再改」的循环里每多一轮，都在加重对源码的破坏。
2. **查断言来源**——检查 <span style="color: red; font-weight: bold;">AI 写的断言是基于「应该」还是「实际」</span>？多数时候问题就在这里。
3. **重走「先跑 → 记录 → 转断言」流程**——重新提示 AI 先跑一遍现有代码，把真实的输入输出打印出来，再把这些真实输出写回断言。

这个动作看似简单，却能解决一半以上的「测试反复失败」问题。它对应的判断是：**测试失败不一定是代码错了，更可能是断言错了**。在 Characterization Test 这种锁「现状」的场景里，<span style="color: red; font-weight: bold;">断言错误的概率往往比代码错误高——代码是过去真实运行过的，断言是 AI 现在猜的。</span>

### 6.5 速查清单：补测试与 CI 落地

以下清单可裁剪，供项目阶段快速查阅。项目较小时可合并「决策」与「优先级」两组；时间紧张时可先完成前三组与「CI 配置」，剩余项分批补齐。

#### (1) 决策与优先级

| 维度 | 清单项 |
|------|--------|
| 决策 | - [ ] 列出本次改造涉及的所有接口与链路<br/>- [ ] 标注哪些链路目前没有任何测试覆盖<br/>- [ ] 确认改造是否触及核心业务（计费/权限/数据写入）<br/>- [ ] 改造范围极小（log/文案）的，明确允许不补<br/>- [ ] 项目即将下线的，整体允许不补 |
| 优先级 | - [ ] 即将改的接口先补 Characterization Test<br/>- [ ] 核心数据写入链路补集成测试（HTTP → Service → DB）<br/>- [ ] 复杂业务逻辑（算分/状态流转/权限）补单元测试<br/>- [ ] 简单 CRUD 默认不进计划<br/>- [ ] 按 `test-gaps.md` 的 P0 优先级排序 |

#### (2) 怎么补（节奏）

- [ ] 让 AI 基于 `test-gaps.md` 出 `test-plan.md`，每批 1-3 个
- [ ] 计划阶段剔除简单 CRUD 与「贴心」的多余项
- [ ] 一批一批补，跑通一批 review 一批
- [ ] review 时强制追问：断言来自「实际跑出来」还是「AI 猜的」
- [ ] review 时核对 edge case 是否真的被覆盖
- [ ] 失败的测试不进下一批（修代码/修测试/标记三选一）
- [ ] 第 2 批起让 AI 参考前一批风格，保持一致性

#### (3) CI 护栏：配置生成与跑通守护

| 阶段 | 清单项 |
|------|--------|
| 配置生成 | - [ ] 扫描项目当前 CI 现状（有/无/过期）<br/>- [ ] 让 AI 生成 `.github/workflows/test.yml` 或对应平台配置<br/>- [ ] 触发条件：push 任何分支 + PR<br/>- [ ] 中间件用 services 字段配齐（MySQL/Nacos 等）<br/>- [ ] JDK 版本严格对齐 `pom.xml` 的 `<java.version>`<br/>- [ ] 配 Maven 依赖 cache 加速 |
| 跑通守护 | - [ ] push 触发一次 CI，定位并修复环境问题<br/>- [ ] CI 跑通后，确认 PR 失败时 block merge<br/>- [ ] 失败常见原因：中间件起不来 / Maven 镜像源慢 / 本地与 CI 环境差异<br/>- [ ] 让 AI 自行 debug 并迭代修复，跑通即收尾 |

### 6.6 三个问题的回答 + 范式转变

整篇只回答了三个问题，回答如下：

| 问题 | 答案 |
|------|------|
| 该不该补？ | 按改造路径判断。即将改的、改完没法验证的、涉及核心业务逻辑的，必须补；改 log 输出这种小改动可以不补，够用就停。 |
| 要补哪些？ | 按价值排优先级：改造路径上的 Characterization Test > 核心链路集成测试 > 复杂逻辑单元测试 > 简单 CRUD（可不补）。 |
| 怎么补？ | 两步走 + 一道护栏：AI 出计划 → 一批一批补（每批 1-3 个，跑通 review 通过再下一批）→ 配 CI 让测试持续自动跑。 |

三条答案能跑通，靠的不是 AI 多强，而是把 AI 拴在两条约束里——「不大而全」靠数量上限，「不一口气」靠分批 review。两条约束加起来，老项目补测试第一次从「知道应该做但永远做不完」变成「几天能做完，且做完就有可信的护栏」（根因与成本账见第 1 章，此处不展开）。**AI 写测试 = 不再有借口拖延。**

### 6.7 两个思考

**思考一**：回看手上项目最近一次改造，有没有出现过「AI 改了什么悄悄改坏」的事？如果当时有 Characterization Test 锁住改造路径，会不会发现得更早？这个问题不是给过去定罪，而是用来判断下一个要改的接口该不该现在就补一个锁现状的测试。

**思考二**：「AI 写测试 = 不再有借口拖延」这句话是否认同？如果认同，打算从哪个项目、哪条核心链路开始补？如果不认同，还有什么因素让团队拖着不补测试——是管理层认知、是 deadline 压力、还是测试基础设施本身缺失？想清楚这个答案，比补测试本身更重要——它决定补出来的测试能不能活下来。
