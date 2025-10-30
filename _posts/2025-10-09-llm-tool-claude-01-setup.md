---
title: 大模型办公01：稳定灵活的Claude Code环境                                        
author: fangkun119
date: 2025-10-09 12:00:00 +0800
categories: [大模型办公]
tags: [大模型办公, Claude Code, 效率工具]
pin: false
math: true
mermaid: true
image:
  path: /imgs/cover/claude_code.png
  lqip: data:image/webp;base64,UklGRpoAAABXRUJQVlA4WAoAAAAQAAAADwAABwAAQUxQSDIAAAARL0AmbZurmr57yyIiqE8oiG0bejIYEQTgqiDA9vqnsUSI6H+oAERp2HZ65qP/VIAWAFZQOCBCAAAA8AEAnQEqEAAIAAVAfCWkAALp8sF8rgRgAP7o9FDvMCkMde9PK7euH5M1m6VWoDXf2FkP3BqV0ZYbO6NA/VFIAAAA
  alt: Claude Code
---

{: .no_toc }

## 1 介绍

本文通过与 Anthropic 有合作的第三方平台 GAC 来使用国外大模型并优化费用，同时采用脚本（可选自动、手动）的方式切换到国内大模型 Kimi K2 和 GLM 4.6 。支持 Cursor 、IntelliJ IDEA 和 VS Code。

### 1.1 使用 Claude Code 的理由

理由 1：功能完备

1. Planning，上下文和记忆管理、MCP、内置和自定义命令、Sub Agent、后台程序和 Todo List 管理、状态栏、为自定义事件添加 Hook、Check Point 回退等…… 一应俱全，为开发和办公提供便利。
2. Claude Code CLI 也可有办法使用 Codex 的命令，方便采用多种工具双持甚至三持，各种工具取长补短。

理由 2：社区支持

1. Visual Studio 原生界面，Cursor，IntelliJ IDEA，第三方界面，…… 都可以使用。

理由 3：大模型支持

1. 可以借助 GAC 平台等优化国外大模型的付费模式，也可以对接 Kimi K2、GLM 4.6 等国内大模型
2. 能够在大模型特长和费用之间合理权衡

### 1.2 目标

搭建一套 Claude Code 环境，满足如下需求。

1. 尽量保证稳定性，避免封号之类的风险。
2. 能够在国外、国内大模型之间切换。
3. 不仅用于开发，也可以辅助文档写作等日常工作，使我们专注于任务核心，节省时间，提升效率。

## 2 搭建方案

### 2.1 使用国外大模型

使用与 Anthropic 有合作的第三方平台 GAC ，而非官方 CLI 。

理由如下：

* GAC 网络连接好，不需要翻墙，封号风险低。
* 更新迅速，几乎可以同步享受到最新版本的功能。
* 价格合理，相对更容易承受。通过第三方渠道购买订阅码的形式充值，更加灵活。
* 提供包日/包周/包月的付费方式，每日刷新额度为日常开发提供保证，适合经常使用的用户。对于突增的开发任务，也可以提 ticket 快速恢复额度。

下面是用 AI 总结出的 GAC 版 和 官方版 CLI 的区别，感兴趣的可自行搜素总结。

| 版本             | 安装要求 | 账号支付       | 稳定性  | 封号风险 | 价格灵活性 | 支持套餐     |
| :--------------- | :------- | :------------- | :------ | :------- | :--------- | :----------- |
| 官方 Claude Code | 需翻墙   | 国际信用卡     | 低-极低 | 高       | 少         | 官方订阅制   |
| GAC Claude Code  | 直连GAC  | 支持支付宝微信 | 极高    | 低       | 高         | 天/周/月卡制 |

### 2.2 使用国内大模型

在 GAC 平台 CLI 基础上，提供切换到 GLM 4.6，Kimi K2 的方法（可通过修改配置使用其它国内大模型）。

选择这两个模型有如下原因

* 是厂商目前主推的大模型，支持主流大模型编程工具，性能不弱，对标 Sonnet。
* 价格更加友好，国内用户可以承受！
* 灵活的付费方式，包括预充值、按量扣费，与 GAC 形成互补。

通过这样的一套组合，就可以在国外、国内大模型之间灵活切换，并且避免封号风险保持环境稳定。

## 3 注册 GAC 平台账户

官网界面直接注册即可：[https://gaccode.com/signup?ref=1O20A96L](https://gaccode.com/signup?ref=1O20A96L)

收不到验证码时，换一个邮箱提供商。

## 4 安装 GAC 版本的 Claude Code CLI 

### 4.1 安装

根据 [https://gaccode.com/install-claude-code](https://gaccode.com/install-claude-code ) 的步骤安装，注意几点

* 对本地的 Node.js 版本有要求，参考 Node JS 的官网进行安装。
* 需要为 homebrew 和 npm 设置国内源来访问仓库（Mac）。
* 如果先前已经安装了 Anthropic 版的 Claude Code 需要卸载（[参考文档](https://aicoding.juejin.cn/post/7520763152037969929)）。
* 文档中“-g”代表全局安装，需要 admin 权限，用 npm get prefix 可查看全局安装目录（Mac）。

安装成功后

* 执行`claude --pick-relay` 命令更新和查看 GAC 的中继服务器列表。
* 再执行 `claude --version` 查看 Claude 版本和新的中继服务器。

### 4.2 升级

版本升级：同样参考链接 [https://gaccode.com/install-claude-code](https://gaccode.com/install-claude-code )，有用于升级的命令。

### 4.3 状态栏插件

用于在 CLI 命令输入窗口下方展示状态信息，例如与费用有关的用了多少 GAC credit 等（官方状态栏则是预估费用...）

[https://gaccode.com/install-claude-code](https://gaccode.com/install-claude-code) 安装的是 GAC 版本的状态栏，而不是官方状态栏，根据自己喜好选择用哪个

## 5 配置 API Key 密钥

### 5.1 API Key 设置脚本

先理解 Claude Code CLI 的 API Key 设置，本质是`base_url`和`api_key`两个配置项

* base_url 是大模型的 API 端点，不同模型提供商提供不同的端点
* api_key 代表某个大模型账号的身份

有两种设置方法，环境变量和~/.claude.json 配置文件，其中环境变量拥有最高优先级，可以覆盖后者。

方法1：通过环境变量设置。优点是灵活，缺点是每次在新的 shell 中都要先执行下面两条命令才能让设置生效。

```bash
export ANTHROPIC_BASE_URL=https://gaccode.com/claudecode # todo：换成自己的大模型 endpoint
export ANTHROPIC_API_KEY=sk-ant-oat01-xxxxxxx # todo: 换成自己的API Key
```

方法2：把`base_url`和`api_key`写入到`~/.claude.json`配置文件中，每次claude启动都能够自动加载。

```bash

# set env vars
export ANTHROPIC_BASE_URL=https://gaccode.com/claudecode  # todo：换成自己的大模型 endpoint
export ANTHROPIC_API_KEY=sk-ant-oat01-xxxxxxx # todo: 换成自己的API Key

# backup ~/.calude.json
cp ~/.claude.json ~/claude_backup_$(date +'%Y%m%d_%H%M%S').json

# programmatically approve this API Key
(cat ~/.claude.json 2>/dev/null || echo 'null') | jq --arg key "${ANTHROPIC_API_KEY: -20}" '(. // {}) | .customApiKeyResponses.approved |= ([.[]?, $key] | unique)' > ~/.claude.json.tmp && mv ~/.claude.json.tmp ~/.claude.json
```

把上述命令写到一个bash脚本中执行，执行前后结果对比如下

执行前：

```bash
__________________________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/
$ cat ~/.claude.json
{
  "installMethod": "unknown",
  "autoUpdates": true,
  "firstStartTime": "2025-10-11T08:00:49.015Z",
  "sonnet45MigrationComplete": true
}
```

执行后：可以看到，多了一个执行码

```bash
__________________________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/
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

### 5.2 创建 API Key

#### (1) GAC平台的API Key

获取API Key的地址是 https://gaccode.com/api-keys ，base url 是`https://gaccode.com/claudecode`

GAC只起到Proxy的作用，它依然使用了Anthropic的模型，因此不需要覆盖model相关的配置

```bash
export ANTHROPIC_BASE_URL=https://gaccode.com/claudecode
export ANTHROPIC_API_KEY=sk-ant-oat01-... # (1) todo: 换成自己的API Key; (2) ANTHROPIC_API_KEY 环境变量比较旧，不建议和 ANTHROPIC_AUTH_TOKEN 同时使用；(3) 具体使用哪个以 GAC 官方最新文档为准
```

#### (2) Kimi K2的API Key

官方文档：[https://platform.moonshot.cn/docs/guide/agent-support](https://platform.moonshot.cn/docs/guide/agent-support) (内有一小节介绍了如何让Claude Code使用Kimi）

API Key：[https://platform.moonshot.cn/console/api-keys](https://platform.moonshot.cn/console/api-keys)

费用：[计费规则](https://platform.moonshot.cn/docs/pricing/chat)，[充值](https://platform.moonshot.cn/console/account)，[促销](https://platform.moonshot.cn/docs/promotion)，[等级限速](https://platform.moonshot.cn/docs/pricing/limits)除了替换base url和auth token，还覆盖了model，从而让claude code不再使用Anthropic的模型

```bash
# Linux/macOS 启动高速版 kimi-k2-turbo-preview 模型
# 下面是2025.10配置，如有变化以上面的官方文档为准
export ANTHROPIC_BASE_URL=https://api.moonshot.cn/anthropic
export ANTHROPIC_AUTH_TOKEN=${YOUR_MOONSHOT_API_KEY} # todo:换成自己的 API Key
export ANTHROPIC_MODEL=kimi-k2-turbo-preview # 如有变化以上面“模型版本及价格”为准
export ANTHROPIC_SMALL_FAST_MODEL=kimi-k2-turbo-preview # 如有变化以上面“模型版本及价格”为准
claude
```

把上述命令放在一个脚本中，例如`~/claude_kimi.sh`，然后在terminal执行下面命令，就能切换到kimi

```bash
source ~/claude_kimi.sh
claude
```

#### (3) GLM 4.6的API Key

文档：[https://docs.bigmodel.cn/cn/coding-plan/tool/claude](https://docs.bigmodel.cn/cn/coding-plan/tool/claude)

充值促销：[https://bigmodel.cn/claude-code](https://bigmodel.cn/claude-code) 

API Key：[https://bigmodel.cn/usercenter/proj-mgmt/apikeys](https://bigmodel.cn/usercenter/proj-mgmt/apikeys)

GLM 4.6所需要的环境变量

```bash
# 下面是2025.10配置，如有变化以上面的官方文档为准
export ANTHROPIC_BASE_URL=https://open.bigmodel.cn/api/anthropic
export ANTHROPIC_AUTH_TOKEN=${YOUR_ZHIPU_API_KEY} # todo:换成自己的 API Key
export API_TIMEOUT_MS=3000000
export ANTHROPIC_DEFAULT_HAIKU_MODEL=glm-4.5-air # 会因 GLM 的升级而发生变化
export ANTHROPIC_DEFAULT_SONNET_MODEL=glm-4.6 # 会因 GLM 的升级而发生变化
export ANTHROPIC_DEFAULT_OPUS_MODEL=glm-4.6 # 会因 GLM 的升级而发生变化
```

把上述命令放在一个脚本中，例如`~/claude_glm.sh`，然后在terminal执行下面命令，就能切换到GLM

```bash
source ~/claude_glm.sh
claude
```

### 5.3 确认配置更改生效

在代码目录执行`claude`命令进入claude code，然后执行`\status`命令查看状态

然后检查 Anthropic base URL 和 Model 这两项

## 6 在 IDE 中使用 Claude Code CLI

Claude Code CLI 可理解为命令行工具，在 IDE 自带的 Terminal 中执行

### 6.1 Cursor 和 VS Code

下载 Cursor 最新版，安装并查看功能列表：[https://cursor.com/cn/download](https://cursor.com/cn/download) （写本文时是1.7）。

打开 Cursor，注册或登录。

用菜单中 File 中的 Open Folder 打开项目所在的目录。

用菜单 Terminal 中的 New Terminal 打开一个命令行终端。

可选切换成 Kimi K2 或 GLM 4.6：

* 手动切换：打开 Terminal，手动调用第 5 小节准备的脚本。
* 自动切换：如果在 bash shell 自加载文件（ ~/.bash_profile）中添加自动切换命令（例如 source ~/claude_kimi.sh ），然后在 Cursor Terminal 右侧“+”下拉菜单 Launch Profile 中选择 bash。

在 terminal 中执行 `claude` 命令，就启动了 Claude Code CLI。

在对话框输入 `/status` 命令检查一下 base url 和 model 是否正确，按 ESC 直接退出。

和大模型对话检查连通性

```bash
> /status 
  ⎿  Status dialog dismissed
> tell me the model and version you are using 
⏺ I am using the kimi-k2-turbo-preview model.
```

### 6.2 IntelliJ IDEA

在 IntelliJ IDEA 菜单 View -> Tool Windows 中检查并开启 Terminial 。

可选切换成 Kimi K2 或 GLM 4.6：

* 手动切换：打开 Terminal，手动调用第 5 小节准备的脚本。
* 自动切换：如果在 bash shell 自加载文件（ ~/.bash_profile）中添加自动切换命令（例如 source ~/claude_kimi.sh ），然后在 IDEA Setting 的 Terminal 一项中把 Shell Path 设置成 `/bin/bash`。

在 terminal 中执行 claude 命令，就启动了 Claude Code CLI。

执行 `/status` 命令检查一下 base url 和 model 是否正确，按 Ctrl + ESC 退出 （按提示 ESC 只会切到代码文本区）。

和大模型对话确认模型可以连通。

```bash
> /status 
  ⎿  Status dialog dismissed
> what is your model and version 
⏺ I am powered by the model kimi-k2-turbo-preview.
```

## 7 在IDE中使用 Claude Code 原生界面

原生界面可以理解为 IDE 插件，它与 IDE UI 深度集成。

* 优点是对输出内容进行了优化，操作更便捷，能够更有效的找到自己需要的内容。
* 缺点是更新会略微滞后于 CLI，例如 Claude Code 2.0 的 check point 功能在本文编写时，还未被原生界面支持，需要与 CLI 相互切换。

目前 Anthropic 提供了 VS Code 插件，可以用在 VS Code 和 Cursor 上。

### 7.1 Cursor 和 VS Code

首先需要修改 Claude Code 的配置，向`~/.calude/config.json` 文件添加如下配置，它将会开启 Claude Code 的 API 登录模式，否则接下来 VS Code插件会直接访问 Anthropic 而不是 GAC 平台。

```bash
__________________________________________________________________
$ /KendeMacBook-Air/ ken@KendeMacBook-Air.local:~/
$ cat ~/.claude/config.json
{
  "primaryApiKey":"api"
}
```

然后在 VS Code 的 Extension Marketplace 中搜索 Claude Code for VS Code （作者是 Anthropic）并安装。

接下来配置大模型，默认使用的是 GAC 的 base url 和 model，因为我没有为 GAC 充值，直接对话会提示我没有额度。

现在我要切换成 Kimi K2 或者 GLM 4.6，在 Setting -> Extensions -> Claude Code 中找到 Edit in Setting.json，向"claude-code.environmentVariables"添加环境变量，变量 name 和 value 参考 5.2 小节。

以 Kimi V2 为例，添加了如下内容：

```json
{
    "claude-code.environmentVariables": [
        {
            "name": "ANTHROPIC_BASE_URL",
            "value": "https://api.moonshot.cn/anthropic"
        },
        {
            "name": "ANTHROPIC_AUTH_TOKEN",
            "value": "sk-0...Ghyg"
        },
        {
            "name": "ANTHROPIC_MODEL",
            "value": "kimi-k2-turbo-preview"
        },
        {
            "name": "ANTHROPIC_SMALL_FAST_MODEL",
            "value": "kimi-k2-turbo-preview"
        }
    ]
}
```

打开某个代码文件，点击右上角的“*”图标弹出 Claude Code 面板。  输入"/"以弹出命令菜单，选择"Select Model"，可以看到现在多了“kimi-k2-turbo-preview"这个选项，选择它。 与大模型对话，看是否能连接到模型

```text
tell me your model and version
I'm running on the kimi-k2-turbo-preview model.
```

## 8 使用 Claude Code 提高工作效率

接下来会写一些使用 Claude Code 提高工作效率的例子和操作步骤，链接到这里

