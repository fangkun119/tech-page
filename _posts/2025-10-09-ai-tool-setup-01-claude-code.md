---
title: 工具配置 01：Claude Code 配置
author: fangkun119
date: 2025-10-09 12:00:00 +0800
categories: [大模型办公, 工具配置]
tags: [大模型办公, 工具配置，Claude Code]
pin: false
math: true
mermaid: true
image:
  path: /imgs/cover/claude_code.png
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: Claude Code
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

## 1. 文档概要

本文将介绍 **Claude Code 环境搭建与使用**，主要聚焦以下三个知识模块：

| 知识模块 | 说明 |
| ---- | ---- |
| 使用背景 | Claude Code 的**使用痛点**、**产品特性**与**环境搭建目标** |
| 模型配置 | **国内外大模型**接入：GAC 平台（Claude）、Kimi K2、GLM 4.6 |
| 平台使用 | **Cursor**、**IntelliJ IDEA**、**VSCode**、**Obsidian** 的配置与 CLI 使用 |

基于这些模块，本文将从**使用背景分析**、**模型配置方法**、**安装配置步骤**、**平台使用指南**四个维度展开，帮助读者快速建立 Claude Code 的**使用认知**。

## 2. 背景与目标

### 2.1 痛点与解决方法

#### (1) 使用痛点

在实际使用 Claude Code 的过程中，普遍面临以下两个主要问题：

| 痛点类型     | 具体问题                                  | 主要影响        |
| -------- | ------------------------------------- | ----------- |
| **账号风险** | 使用 Anthropic 官方大模型存在封号风险              | 开发环境稳定性难以保障 |
| **功能依赖** | 使用国内大模型时，部分新功能依赖 Anthropic 模型特性，兼容性受限 | 功能使用体验受影响   |
#### (2) 解决方法

针对上述痛点，本文提出以下解决思路：

| 解决方法 | 具体说明 |
| --- | --- |
| **① 降低账号风险** | 为避免直接使用官方 CLI 带来的封号风险，可通过与 Anthropic 有合作的第三方平台 **GAC** 访问 Claude 模型，在保证功能完整的同时提升账号安全性。 |
| **② 灵活切换模型** | 在解决账号风险问题后，为了更好地适应不同场景的需求，可采用**脚本切换**（支持自动或手动模式）或**工具切换**（如 CC Switch）的方式，在国内外大模型之间灵活切换，根据任务需求选择合适的模型。 |
| **③ 多平台配置支持** | 为了让更多开发者能够方便地使用，本文还将提供 **Cursor**、**IntelliJ IDEA**、**VSCode** 三种主流开发环境的配置方法，覆盖不同用户的使用习惯。 |

### 2.2 Claude Code 的优势

在选择 AI 辅助工具时，**功能完备性**、**界面灵活性**和**模型兼容性**是三个重要考量因素。Claude Code 在这三个方面都具有明显特点，使其成为合适的选择。

#### (1) 功能完整性

Claude Code 提供了**完整的功能集合**，覆盖开发和办公的常见需求：

| 功能类别 | 主要特性 |
| --- | --- |
| **任务管理** | Planning、Todo List、Sub Agent、后台程序管理 |
| **上下文管理** | 记忆管理、Check Point 回退 |
| **扩展能力** | MCP 协议、内置和自定义命令、自定义事件 Hook |
| **界面支持** | 状态栏、原生插件界面 |

除了这些内置功能外，Claude Code CLI 还**兼容 Codex 命令**，便于根据任务特点**灵活选择工具组合**，发挥不同工具的优势。

#### (2) 多平台支持

在界面灵活性方面，Claude Code 支持**多种开发环境**，用户可根据工作习惯选择最合适的使用方式：

| 界面类型 | 适用场景 |
| --- | --- |
| **VSCode 原生插件** | 与编辑器深度集成，使用便捷 |
| **Cursor** | AI 原生编辑器，体验流畅 |
| **IntelliJ IDEA** | Java 开发场景 |
| **第三方界面** | 特定需求定制 |

这种多平台支持使用户能够在熟悉的开发环境中高效使用 Claude Code。

#### (3) 模型兼容性

最后，在模型兼容性方面，Claude Code 支持**国内外多种大模型**，可根据任务需求和预算灵活选择：

| 模型类型 | 代表模型 | 选择依据 |
| --- | --- | --- |
| **国外模型** | Claude（通过 GAC 平台） | 功能完整、更新及时 |
| **国内模型** | Kimi K2、GLM 4.6 | 成本较低、访问稳定 |

这种**多模型支持**使得用户能够在**性能和成本**之间找到最佳平衡点，根据具体任务特点选择最合适的模型，既保证使用效果又控制使用成本。

### 2.3 环境搭建目标

针对前述痛点和需求，本文将帮助读者搭建一套 **Claude Code 环境**，从**稳定性**、**灵活性**和**实用性**三个维度满足实际使用需求。

**① 稳定性保障**

首先关注环境的稳定性。通过使用与 Anthropic 有合作的**第三方平台 GAC**，避免直接使用官方 CLI，降低**封号风险**，提升开发环境的**连续性和可用性**。

**② 灵活切换能力**

在稳定性基础上，进一步提升使用灵活性。可在国外大模型（如 Claude）和国内大模型（如 GLM 4.6、Kimi K2）之间灵活切换，根据任务特点选择合适的模型，在性能和成本之间取得平衡。

**③ 实用价值拓展**

除了技术层面的优化，还注重实用价值的拓展。除代码开发外，还可辅助文档写作、问题分析等日常工作，帮助用户聚焦重要任务，减少重复性工作，提升整体效率。

## 3. 国内外大模型使用

### 3.1 国外模型配置

#### (1) GAC 平台

对于国外大模型的使用，推荐采用与 Anthropic 有合作的第三方平台 GAC，而非官方 CLI。

##### **① 选择理由**

| 优势维度   | 具体说明                                                                    |
| ---------- | ----------------------------------------------------------------------- |
| **网络稳定性** | 直连 GAC 服务器，无需翻墙，网络连接稳定，**封号风险低**                                      |
| **功能同步**  | 功能更新迅速，与官方版本**几乎同步发布**，使用户能及时使用最新功能                                    |
| **支付便捷**  | 支持支付宝、微信等国内支付方式，可通过订阅码充值，购买渠道灵活，价格**相对合理**                                 |
| **套餐灵活**  | 提供**包日/包周/包月**多种付费方式，每日刷新额度为日常开发提供保证。对于突增的开发任务，可提 ticket 快速恢复额度 |

为了更直观地了解 GAC 版本与官方版本的区别，接下来将进行更详细对比。

##### **② 版本对比**

下表总结了 GAC 版与官方版 CLI 的主要区别（内容由 AI 生成，供参考）：

| 版本             | 安装要求 | 账号支付       | 稳定性  | 封号风险 | 价格灵活性 | 支持套餐     |
| :--------------- | :------- | :------------- | :------ | :------- | :--------- | :----------- |
| 官方 Claude Code | 需翻墙   | 国际信用卡     | 低-极低 | 高       | 少         | 官方订阅制   |
| GAC Claude Code  | 直连GAC  | 支持支付宝微信 | 极高    | 低       | 高         | 天/周/月卡制 |

### 3.2 国内模型配置

除了使用 GAC 访问国外模型外，我们还可以配置国内大模型作为补充选择。下面介绍如何选择合适的国内模型，以及它与 GAC 的组合优势。

#### (1) 模型选择

在已安装 GAC 平台 CLI 的基础上，可通过脚本或工具切换到 GLM 4.6、Kimi K2 等国内大模型（支持扩展至其他国产模型）。

##### **① 选择理由**

选择 **GLM 4.6** 和 **Kimi K2** 作为补充模型，主要基于以下考虑：

| 优势维度 | GLM 4.6 | Kimi K2 |
| --- | --- | --- |
| **性能表现** | 智谱AI主推模型，支持主流编程工具，性能接近 Sonnet | 月之暗力最新模型，长文本处理能力强 |
| **价格优势** | 预充值模式，按量扣费 | 提供多种套餐，费用相对较低 |
| **支付便利** | 支持国内支付方式 | 支持国内支付方式 |

从上表可以看出，两款模型均采用**预充值+按量扣费**模式，这与 GAC 的订阅制形成互补，能够满足不同使用频率的需求。

##### **② 组合优势**

将国内模型与 GAC 结合使用，可以发挥更大的价值。通过 **GAC + 国内大模型** 的组合使用，可以：

- **灵活切换**：根据任务需求在**国外模型**（Claude）和**国内模型**（GLM/Kimi）之间切换
- **降低风险**：避免过度依赖单一平台，减少封号风险
- **稳定环境**：保持开发环境的连续性和稳定性

## 4. 安装步骤

### 4.1 注册 GAC 账户

在开始安装前，首先需要完成 **GAC 平台账户注册**。流程非常简单，访问官网后按照界面提示操作即可：

[https://gaccode.com/signup?ref=1O20A96L](https://gaccode.com/signup?ref=1O20A96L)

> **温馨提示**：注册过程中如果收不到验证码，可以尝试更换其他邮箱提供商（例如从 QQ 邮箱切换到 Gmail）。

### 4.2 安装 Claude Code CLI

完成 GAC 账户注册后，接下来需要安装 Claude Code CLI。本节将介绍完整的安装流程、验证方法以及相关的插件配置。

#### (1) 安装

##### **① 安装步骤**

访问 [GAC 官方安装文档](https://gaccode.com/install-claude-code) 按步骤操作即可。在开始安装之前，请先确认以下前提条件是否满足：

| 前提条件 | 说明 |
|---------|------|
| **Node.js 版本** | 本地 Node.js 版本需符合要求，可参考 [Node.js 官网](https://nodejs.org/) 安装或升级 |
| **国内源配置**（Mac） | 为 homebrew 和 npm 设置国内镜像源，以提升访问速度 |
| **旧版本卸载** | 如已安装 Anthropic 版 Claude Code，需先卸载（[参考文档](https://aicoding.juejin.cn/post/7520763152037969929)） |
| **全局安装权限** | 安装命令中的 `-g` 表示全局安装，需要管理员权限（Mac 下可用 `npm get prefix` 查看全局安装目录） |

##### **② 验证安装**

安装完成后，建议通过以下命令验证安装是否成功：

* 执行 `claude --pick-relay` 更新并查看 **GAC 中继服务器列表**
* 执行 `claude --version` 查看 **Claude 版本信息**和**当前中继服务器**

#### (2) 升级

未来如需升级 Claude Code，只需参考 [GAC 官方安装文档](https://gaccode.com/install-claude-code) 中的升级命令执行即可。

#### (3) 状态栏插件

除了主要 CLI 功能外，GAC 版本还提供了状态栏插件，用于在 CLI 命令输入窗口下方实时展示**使用状态信息**，例如 **GAC credit 消费量**等费用相关数据（相比之下，官方状态栏显示的是**预估费用**）。

需要注意的是，通过 [GAC 官方安装文档](https://gaccode.com/install-claude-code) 安装的是 **GAC 版本状态栏**，而非官方版本。用户可根据个人偏好选择使用哪一个。

### 4.3 配置 API Key

安装完成后，接下来需要配置 API Key 才能正常使用 Claude Code。本节将介绍配置原理、具体方法以及不同平台的配置步骤。

#### (1) API Key 配置方法

本节介绍 Claude Code CLI 的 API Key 配置方法,帮助读者在不同使用场景中选择合适的配置方式。

##### **① 配置原理**

在介绍具体配置方法前，先了解 Claude Code CLI 的连接机制。CLI 通过两个配置项连接大模型服务:

| 配置项 | 作用 |
| --- | --- |
| **base_url** | 大模型的 API 端点地址 |
| **api_key** | 大模型账号的身份凭证 |

配置方式有两种：**环境变量**(优先级高)和 `~/.claude.json` **配置文件**(自动加载)。环境变量会覆盖配置文件中的设置。下面分别介绍这两种方法的使用场景和操作步骤。

##### **② 方法1:环境变量**

通过环境变量配置,适合需要频繁切换模型的场景。

```bash
export ANTHROPIC_BASE_URL=https://gaccode.com/claudecode # todo：换成自己的大模型 endpoint
export ANTHROPIC_API_KEY=sk-ant-oat01-xxxxxxx # todo: 换成自己的API Key
```

**注意**: 每次打开新的 shell 窗口都需要重新执行上述命令。

如果希望避免重复设置，可以使用配置文件方式。

##### **③ 方法2:配置文件**

将配置写入 `~/.claude.json`,Claude 启动时自动加载,适合长期使用固定模型的场景。

执行以下脚本完成配置:

```bash

# set env vars
export ANTHROPIC_BASE_URL=https://gaccode.com/claudecode  # todo：换成自己的大模型 endpoint
export ANTHROPIC_API_KEY=sk-ant-oat01-xxxxxxx # todo: 换成自己的API Key

# backup ~/.calude.json
cp ~/.claude.json ~/claude_backup_$(date +'%Y%m%d_%H%M%S').json

# programmatically approve this API Key
(cat ~/.claude.json 2>/dev/null || echo 'null') | jq --arg key "${ANTHROPIC_API_KEY: -20}" '(. // {}) | .customApiKeyResponses.approved |= ([.[]?, $key] | unique)' > ~/.claude.json.tmp && mv ~/.claude.json.tmp ~/.claude.json
```

##### **④ 配置效果验证**

执行上述脚本后,`~/.claude.json` 会新增 API Key 的授权记录。下面通过执行前后的对比,说明配置的变化。

**执行前:**

```bash
_______________________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/
$ cat ~/.claude.json
{
  "installMethod": "unknown",
  "autoUpdates": true,
  "firstStartTime": "2025-10-11T08:00:49.015Z",
  "sonnet45MigrationComplete": true
}
```

**执行后:** 新增了 `customApiKeyResponses` 字段,包含 API Key 后20个字符的摘要

```bash
_______________________________________________________________
$ /KendeMacBook-Air  ken@KendeMacBook-Air.local:~/
$ cat ~/.claude.json
{
  "installMethod": "unknown",
  "autoUpdates": true,
  "firstStartTime": "2025-10-11T08:00:49.015Z",
  "sonnet45MigrationComplete": true,
  "customApiKeyResponses": {
    "approved": [
      "e8***39" # 是API Key 的后20个字符
    ]
  }
}
```

#### (2) 获取 API Key

了解了配置原理和方法后,接下来介绍如何在不同平台获取 API Key 并完成配置。本节涵盖 GAC、Kimi K2 和 GLM 4.6 三种常见配置,帮助读者根据需求选择合适的模型服务。

##### **① GAC平台**

首先介绍 GAC 平台的配置方法。GAC 是一个代理服务，直接转发请求到 Anthropic 官方模型，因此**无需修改模型配置**。

| 配置项 | 地址/说明 |
| --- | --- |
| **API Key 获取** | https://gaccode.com/api-keys |
| **Base URL** | `https://gaccode.com/claudecode` |

配置环境变量：

```bash
export ANTHROPIC_BASE_URL=https://gaccode.com/claudecode
export ANTHROPIC_API_KEY=sk-ant-oat01-... # (1) todo: 换成自己的API Key; (2) ANTHROPIC_API_KEY 环境变量比较旧，不建议和 ANTHROPIC_AUTH_TOKEN 同时使用；(3) 具体使用哪个以 GAC 官方最新文档为准
```

##### **② Kimi K2**

接下来介绍 Kimi K2 的配置。与 GAC 不同，Kimi 使用**自研模型**，需要同时修改 base URL、认证 token 和模型名称。

| 资源类型 | 地址 |
| --- | --- |
| **官方文档** | https://platform.moonshot.cn/docs/guide/agent-support |
| **API Key** | https://platform.moonshot.cn/console/api-keys |
| **计费规则** | [计费](https://platform.moonshot.cn/docs/pricing/chat) · [充值](https://platform.moonshot.cn/console/account) · [促销](https://platform.moonshot.cn/docs/promotion) |

配置环境变量：

```bash
# Linux/macOS 启动高速版 kimi-k2-turbo-preview 模型
# 下面是2025.10配置，如有变化以上面的官方文档为准
export ANTHROPIC_BASE_URL=https://api.moonshot.cn/anthropic
export ANTHROPIC_AUTH_TOKEN=${YOUR_MOONSHOT_API_KEY} # todo:换成自己的 API Key
export ANTHROPIC_MODEL=kimi-k2-turbo-preview # 如有变化以上面”模型版本及价格”为准
export ANTHROPIC_SMALL_FAST_MODEL=kimi-k2-turbo-preview # 如有变化以上面”模型版本及价格”为准
claude
```

为方便在不同模型间切换，可将上述命令保存为脚本（如 `~/claude_kimi.sh`），之后执行 `source ~/claude_kimi.sh && claude` 即可。

##### **③ GLM 4.6**

最后介绍 GLM 4.6 的配置。GLM 同样使用**自研模型**，需要配置完整的模型映射关系。

| 资源类型 | 地址 |
| --- | --- |
| **官方文档** | https://docs.bigmodel.cn/cn/coding-plan/tool/claude |
| **API Key** | https://bigmodel.cn/usercenter/proj-mgmt/apikeys |
| **充值促销** | https://bigmodel.cn/claude-code |

配置环境变量：

```bash
# 下面是2025.10配置，如有变化以上面的官方文档为准
export ANTHROPIC_BASE_URL=https://open.bigmodel.cn/api/anthropic
export ANTHROPIC_AUTH_TOKEN=${YOUR_ZHIPU_API_KEY} # todo:换成自己的 API Key
export API_TIMEOUT_MS=3000000
export ANTHROPIC_DEFAULT_HAIKU_MODEL=glm-4.5-air # 会因 GLM 的升级而发生变化
export ANTHROPIC_DEFAULT_SONNET_MODEL=glm-4.6 # 会因 GLM 的升级而发生变化
export ANTHROPIC_DEFAULT_OPUS_MODEL=glm-4.6 # 会因 GLM 的升级而发生变化
```

同样，为方便切换可将上述命令保存为脚本（如 `~/claude_glm.sh`），之后执行 `source ~/claude_glm.sh && claude` 即可。

##### **④ 验证配置生效**

完成上述配置后，建议验证设置是否正确生效。可以通过执行 `/status` 命令查看当前配置信息，重点检查 Anthropic base URL 和 Model 这两项是否与预期一致。

## 5. 使用

### 5.1 原生插件使用

#### (1) 原生界面特点

本节介绍 Claude Code **原生界面**的基本特点，帮助读者了解其使用场景和局限性。

**原生界面**是与 IDE UI 深度集成的**插件形式**，目前在 **VSCode** 和 **Cursor** 上均可使用。

为了帮助读者根据需求选择合适的使用方式，下面总结了原生界面的主要优势与局限性：

| 维度 | 说明 |
| --- | --- |
| **使用体验** | 输出内容经过优化，操作更便捷，可快速定位所需信息 |
| **功能更新** | 更新节奏**略微滞后于 CLI**，新功能可能需要等待 |
| **灵活切换** | 某些 CLI 新功能（如 **Check Point**）在插件中暂未支持时，需与 CLI **配合使用** |

#### (2) Cursor 和 VSCode

本节介绍如何在 **Cursor** 和 **VSCode** 中配置 Claude Code，包括**API 登录模式配置**、**插件安装**和**大模型切换**三个步骤。

##### **① 配置 API 登录模式**

修改 Claude Code 配置文件 `~/.claude/config.json`，添加以下内容开启 **API 登录模式**。此配置可使 VSCode 插件通过 GAC 平台访问，而非直接连接 Anthropic。

```bash
__________________________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/
$ cat ~/.claude/config.json
{
  “primaryApiKey”:”api”
}
```

##### **② 安装和配置插件**

在 VSCode 的 **Extension Marketplace** 中搜索 **Claude Code for VSCode**（作者 Anthropic）并安装。

插件安装完成后，接下来需要配置大模型。默认情况下使用 GAC 的 base url 和 model，如需切换至其他模型（如 Kimi K2 或 GLM 4.6），需在 **Settings -> Extensions -> Claude Code** 中找到 **Edit in settings.json**，向 `claude-code.environmentVariables` 添加环境变量（变量配置参考 5.2 小节）。

以 **Kimi V2** 为例，配置如下：

```json
{
    “claude-code.environmentVariables”: [
        {
            “name”: “ANTHROPIC_BASE_URL”,
            “value”: “https://api.moonshot.cn/anthropic”
        },
        {
            “name”: “ANTHROPIC_AUTH_TOKEN”,
            “value”: “sk-0...Ghyg”
        },
        {
            “name”: “ANTHROPIC_MODEL”,
            “value”: “kimi-k2-turbo-preview”
        },
        {
            “name”: “ANTHROPIC_SMALL_FAST_MODEL”,
            “value”: “kimi-k2-turbo-preview”
        }
    ]
}
```

##### **③ 切换大模型**

完成配置后，即可在 VSCode 中切换使用新添加的模型。打开任意代码文件，点击右上角的 `*` 图标弹出 Claude Code 面板。输入 `/` 弹出命令菜单，选择 **Select Model**，在列表中选择新添加的 `kimi-k2-turbo-preview`。

为确保模型切换成功，建议通过以下对话验证连接：

```text
tell me your model and version
I'm running on the kimi-k2-turbo-preview model.
```

#### (3) Obsidian

本节介绍如何在 Obsidian 中通过 **Claudian 插件**集成 Claude Code，实现对文档的 AI 辅助编辑。

##### **① 安装 Claudian 插件**

在 Obsidian 插件市场搜索 `claudian` 即可自动安装。若插件已下架，则需根据 [Claudian GitHub](https://github.com/YishenTu/claudian) 说明手动安装。

安装完成后，在插件配置面板找到 `claudian` 并启用。

<img src="/imgs/ai-tool-setup-01-claude-code/2a32df349a113adf9aa2c0c7fa955f7a_MD5.jpg" style="display: block; width: 100%;" alt="2a32df349a113adf9aa2c0c7fa955f7a MD5">

##### **② 配置环境变量**

点击插件设置齿轮图标后，可以看到配置界面。这里需要重点配置 **环境** 和 **Claude CLI 路径** 两项必填内容。

<img src="/imgs/ai-tool-setup-01-claude-code/40d5a3a9920a33ef5be51671bbd228e7_MD5.jpg" style="display: block; width: 100%;" alt="40d5a3a9920a33ef5be51671bbd228e7 MD5">

需要注意的是，由于 Claudian 无法读取系统环境变量，因此需要在配置界面中手动填入以下内容：

```shell
# 模型端点：
# 注意是OpenAI Schema Endpoint，而不是Anthropic Schema Endpoint
# 在模型提供方官方查找或咨询客服
ANTHROPIC_BASE_URL=https://open.bigmodel.cn/api/coding/paas/v4

# 模型提供方主页上生成的 Auth Token
ANTHROPIC_AUTH_TOKEN={你的Auth Token}

# 超时时间：选填，配置值参考模型提供方的推荐
API_TIMEOUT_MS=3000000

# 模型型号配置
ANTHROPIC_DEFAULT_HAIKU_MODEL=glm-4.7-flash
ANTHROPIC_DEFAULT_SONNET_MODEL=glm-4.7
ANTHROPIC_DEFAULT_OPUS_MODEL=glm-5.1
ANTHROPIC_MODEL=glm-4.7
ANTHROPIC_REASONING_MODEL=glm-4.7

# 跳过GAC Code启动Claude Code时的用于确认环节，以便导致Claude Code启动时被卡住
CLAUDE_LAUNCHER_QUIET=1

# 选填，如果使用过程中遇到”XXX Not Found”的问题，可以在这里把PATH路径也配上
# 配置值可以通过在Shell中执行”echo $PATH”来获得
PATH=/bin:/opt/homebrew/bin:/opt/homebrew/opt/ruby/bin:/opt/homebrew/sbin
```

##### **③ 配置 Claude CLI 路径**

环境变量配置完成后，接下来需要指定 Claude Code CLI 的安装路径。执行以下命令获取路径：

```bash
which claude
```

将返回的路径填入配置界面的"Claude CLI 路径"字段中。此外，可根据实际需要配置 **Slash Command 导入**、**Claude Code Plugin** 等自定义选项。

<img src="/imgs/ai-tool-setup-01-claude-code/e91c46768802bcf56954e871b1661369_MD5.jpg" style="display: block; width: 100%;" alt="e91c46768802bcf56954e871b1661369 MD5">

##### **④ 使用 Claudian**

完成以上所有配置步骤后，Claudian 插件即可正常使用。点击左侧边栏的 🤖 图标打开 Claudian 面板，即可开始对文档进行 AI 辅助编辑。

<img src="/imgs/ai-tool-setup-01-claude-code/05f5603626d1572ab19e7bb67564976c_MD5.jpg" style="display: block; width: 100%;" alt="05f5603626d1572ab19e7bb67564976c MD5">

### 5.2 CLI 使用

除了原生插件外，本节介绍如何在 **Cursor**、**VSCode** 和 **IntelliJ IDEA** 中直接使用 **Claude Code CLI**，以便使用最新的功能特性。

#### (1) Cursor 和 VSCode

在 Cursor（或 VSCode）的终端中启动 Claude Code CLI，可以使用 CLI 的最新功能，同时保持与代码编辑的紧密集成。下面以 Cursor 为例介绍具体步骤。

##### **① 启动 Claude Code CLI**

**准备环境**

首先，确保已下载并安装 Cursor 最新版：[https://cursor.com/cn/download](https://cursor.com/cn/download)（写本文时是 1.7 版本）。

**打开项目**

启动 Cursor 后，通过以下步骤打开项目目录：

1. 在菜单栏选择 **File → Open Folder**
2. 选择项目所在目录并打开

**启动 CLI**

环境准备完成后，在菜单栏选择 **Terminal → New Terminal** 打开命令行终端，执行以下命令启动 Claude Code CLI：

```bash
claude
```

**切换模型（可选）**

如需使用 Kimi K2 或 GLM 4.6，可选择以下方式：

- 手动切换：在终端手动调用第 5 小节准备的脚本
- 自动切换：在 `~/.bash_profile` 中添加自动切换命令（如 `source ~/claude_kimi.sh`），然后在 Cursor Terminal 右侧 “+” 下拉菜单的 **Launch Profile** 中选择 bash

模型配置完成后，接下来需要验证配置是否正确。

##### **② 验证配置**

启动 CLI 后，建议通过以下两个步骤验证配置是否正确。

**检查配置信息**

在 CLI 对话框中输入 `/status` 命令，查看 base url 和 model 是否正确。按 ESC 可直接退出状态对话框。

**测试连通性**

```bash
> /status
  ⎿  Status dialog dismissed
> tell me the model and version you are using
⏺ I am using the kimi-k2-turbo-preview model.
```

#### (2) IntelliJ IDEA

对于使用 IntelliJ IDEA 的用户，同样可以在内置终端中使用 Claude Code CLI。操作步骤与 Cursor 类似，但有一些细节差异。

##### **① 启动 Claude Code CLI**

**打开 Terminal**

在 IDEA 菜单栏选择 View -> Tool Windows -> Terminal。

**切换模型（可选）**

如需切换到 Kimi K2 或 GLM 4.6，可选择以下方式：

| 切换方式 | 操作说明 |
| --- | --- |
| **手动切换** | 在 Terminal 中执行第 5 小节准备的切换脚本 |
| **自动切换** | 在 ~/.bash_profile 中添加切换命令，并在 Settings -> Terminal -> Shell Path 中设置为 `/bin/bash` |

**启动 CLI**

完成上述配置后，在 Terminal 中执行 `claude` 命令即可启动。

##### **② 验证配置**

**检查配置信息**

执行 `/status` 命令确认 base url 和 model 配置正确，按 Ctrl + ESC 退出（注意：单独按 ESC 会切回代码编辑区）。

**测试模型连接**

与大模型进行简单对话，确认模型可正常响应。

```bash
> /status 
  ⎿  Status dialog dismissed
> what is your model and version 
⏺ I am powered by the model kimi-k2-turbo-preview.
```

#### (3) Obsidian

虽然前文介绍的 Claudian 插件提供了良好的文档编辑体验，但某些高级功能（如 Sub Agent、Check Point 等）可能仍需通过 CLI 使用。为突破这一限制，本节介绍通过 Obsidian 的 `Terminal` 插件调用 **Claude Code CLI**，从而在文档编写时也能使用这些高级功能。

##### **① 安装 Terminal 插件**

`Terminal` 插件在 Obsidian 的**第三方插件市场**中即可找到。

<img src="/imgs/ai-tool-setup-01-claude-code/c3d6fa964348fd84447ac48613ec1336_MD5.jpg" style="display: block; width: 100%;" alt="c3d6fa964348fd84447ac48613ec1336 MD5">

安装完成后，进入 **”设置 → 第三方插件”**，找到 `Terminal` 插件并**启用**。

<img src="/imgs/ai-tool-setup-01-claude-code/53d6f31634abced61d34460757c71f1c_MD5.jpg" style="display: block; width: 100%;" alt="53d6f31634abced61d34460757c71f1c MD5">

启用后，点击左侧边栏的 **`>_`** 图标即可打开 Terminal 面板。

<img src="/imgs/ai-tool-setup-01-claude-code/427091c2193744345e666b43e43e9644_MD5.jpg" style="display: block; width: 100%;" alt="427091c2193744345e666b43e43e9644 MD5">

##### **② 配置环境变量**

Terminal 插件**无法自动加载**系统的环境变量，这会导致找不到 `claude` 命令。因此需要在 Shell 配置文件中**手动添加 PATH**。

以 Bash 为例，在 `~/.bash_profile` 中添加以下内容（即执行 `echo ${PATH}` 的输出）：

<img src="/imgs/ai-tool-setup-01-claude-code/64ed5d677bdec4c43823a13fa53d20b8_MD5.jpg" style="display: block; width: 100%;" alt="64ed5d677bdec4c43823a13fa53d20b8 MD5">

## 6. 总结

通过上述**使用背景分析**、**模型配置方法**、**安装配置步骤**和**平台使用指南**，本文帮助读者快速完成 Claude Code 的**环境搭建与使用**。具体收获如下：

| 学习层次       | 主要收获                                                                                  |
| ---------- | ------------------------------------------------------------------------------------- |
| **识别使用问题** | 了解使用 Claude Code 的**两个主要问题**：账号风险、功能依赖                                                |
| **理解产品特性** | 熟悉 Claude Code 的**三个特点**：功能完整、多平台支持、模型兼容                                              |
| **掌握模型配置** | 学会配置**三种模型**：GAC（Claude）、Kimi K2、GLM 4.6，了解它们的优势与组合使用方式                               |
| **完成环境搭建** | 掌握注册 GAC 账户、安装 Claude Code CLI、配置 API Key 的完整流程                                       |
| **学会平台使用** | 能够在 **Cursor**、**IntelliJ IDEA**、**VSCode**、**Obsidian** 中配置和使用 Claude Code CLI 及原生插件 |
