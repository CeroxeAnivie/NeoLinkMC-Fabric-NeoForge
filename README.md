# NeoLinkMC

**Minecraft Fabric 内网穿透 Mod | 一键开服，轻松联机**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8-green?style=flat-square&logo=minecraft)](https://minecraft.net)
[![Java](https://img.shields.io/badge/Java-21%2B-orange?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net)
[![Fabric](https://img.shields.io/badge/Fabric-0.16.14-blue?style=flat-square)](https://fabricmc.net)
[![ModernUI](https://img.shields.io/badge/ModernUI-3.12.0-purple?style=flat-square)](https://modrinth.com/mod/modern-ui)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](https://opensource.org/licenses/MIT)

---

## 📖 简介

**NeoLinkMC** 是一款专为 Minecraft 设计的内网穿透 Fabric Mod，深度集成「对局域网开放」界面，让你无需复杂配置即可将本地 LAN 世界暴露到公网，与好友轻松联机。

### ✨ 核心特性

| 特性 | 说明 |
|------|------|
| 🎮 **游戏内集成** | 劫持原版「对局域网开放」界面，一键配置启动 |
| 🔧 **游戏设置同步** | 游戏模式、在线模式、PVP、作弊、最大玩家数一键设置 |
| 🔑 **正版/离线兼容** | 三种在线模式：正版验证 / 离线+UUID修复 / 纯离线模式 |
| 🌐 **TCP 协议支持** | 稳定 TCP 连接，完美兼容 Minecraft 联机需求 |
| 🔄 **智能重连** | 自动检测连接状态，断线后自动重连 |
| 💓 **心跳保活** | 定时心跳包防止 NAT 超时断开 |
| 🗄️ **多节点支持** | 支持配置多个服务端节点，灵活切换 |
| 🎨 **现代化 UI** | 基于 Modern UI 框架，美观易用的图形界面 |

---

## 🚀 快速开始

### 环境要求

| 依赖 | 版本要求 |
|------|----------|
| Minecraft | 1.21.8 |
| Java | 21+ |
| Fabric Loader | 0.16.14+ |
| Fabric API | 0.130.0+ |
| Modern UI | 3.12.0+ |

### 安装步骤

1. **安装 Fabric Loader 0.16.14+**
   - 使用 [Fabric 官方安装器](https://fabricmc.net/use/)

2. **下载前置 Mod**
   - [Modern UI 3.12.0+](https://modrinth.com/mod/modern-ui) - **必需前置**
   - [Fabric API](https://modrinth.com/mod/fabric-api) - **必需前置**

3. **安装 NeoLinkMC**
   - 从 [Releases](../../releases) 下载最新版 `NeoLinkMC-xxx.jar`
   - 将 jar 文件放入 `.minecraft/mods/` 文件夹

4. **启动游戏**
   - 使用 Fabric 配置启动 Minecraft
   - 进入单人游戏 → 选择世界 → ESC → 「对局域网开放」

### 使用方法

```
1. 进入单人游戏世界
2. 按 Esc → 点击「对局域网开放」
3. 在 NeoLinkMC 界面配置：
   ├─ 隧道设置：密钥、服务器地址、本地端口
   └─ 游戏设置：游戏模式、在线模式、PVP、作弊、最大玩家数
4. 点击「开启内网穿透」，等待连接成功
5. 将生成的公网地址分享给好友！
```

---

## ⚙️ 配置说明

### 配置文件位置

```
.minecraft/config/neolinkmc/
└── config.json     # 主配置文件（JSON格式）
```

### config.json 详解

```json
{
  "remote_domain": "p.ceroxe.fun",
  "host_hook_port": "44801",
  "host_connect_port": "44802",
  "local_port": "25565",
  "local_domain": "localhost",
  "gamemode": "SURVIVAL",
  "onlinemode": "OFFLINE_TRY_ONLINE_UUID_FIRST",
  "pvp_allowed": true,
  "allow_cheats": true,
  "max_players": 8
}
```

#### 配置项说明

| 配置项 | 类型 | 默认值 | 说明 |
|--------|------|--------|------|
| `remote_domain` | string | `p.ceroxe.fun` | 远程服务器域名 |
| `host_hook_port` | string | `44801` | 服务端指令端口（用于建立控制连接） |
| `host_connect_port` | string | `44802` | 服务端数据传输端口（用于 TCP 连接转发） |
| `local_port` | string | `25565` | 本地 Minecraft 服务器端口 |
| `local_domain` | string | `localhost` | 本地服务地址 |
| `gamemode` | string | `SURVIVAL` | 游戏模式：SURVIVAL/CREATIVE/ADVENTURE/SPECTATOR |
| `onlinemode` | string | `OFFLINE_TRY_ONLINE_UUID_FIRST` | 在线模式，见下方说明 |
| `pvp_allowed` | boolean | `true` | 是否允许 PVP |
| `allow_cheats` | boolean | `true` | 是否允许作弊 |
| `max_players` | int | `8` | 最大玩家数 |

**注意**：密钥(key)不会保存到配置文件中，每次启动时需要手动输入或使用默认密钥 "Free"。

#### 在线模式 (onlinemode) 说明

| 模式值 | 说明 |
|--------|------|
| `ONLINE_ONLINE_UUID_ONLY` | 正版验证开启，仅正版玩家可加入 |
| `OFFLINE_TRY_ONLINE_UUID_FIRST` | 正版验证关闭，优先使用正版 UUID（推荐） |
| `OFFLINE_OFFLINE_UUID_ONLY` | 纯离线模式，使用离线 UUID |

---

## 🏗️ 开发构建

### 克隆项目

```bash
git clone https://github.com/NeoProxy/NeoLinkMC.git
cd NeoLinkMC
```

### 开发命令

```bash
# 运行开发客户端
./gradlew runClient

# 生成 IDEA 项目文件
./gradlew genSources idea

# 构建 Mod
./gradlew build

# 输出位置: build/libs/NeoLinkMC-xxx.jar
```

---

## 📁 项目结构

```
NeoLinkMC/
├── src/main/java/neoproxy/neolinkmc/
│   ├── NeoLinkMC.java                    # Mod 主入口
│   ├── config/
│   │   ├── ConfigManager.java            # JSON 配置管理
│   │   └── LanguageData.java             # 多语言支持
│   ├── gui/
│   │   ├── core/                         # UI 核心组件
│   │   ├── components/                   # 可复用组件
│   │   ├── screens/                      # 界面页面
│   │   │   ├── NeoLinkMainScreen.java    # 主界面
│   │   │   ├── TunnelSettingsFragment.java  # 隧道设置
│   │   │   └── GameSettingsFragment.java    # 游戏设置
│   │   ├── OnlineMode.java               # 在线模式枚举
│   │   └── ConfigContainer.java
│   ├── mixin/
│   │   └── MixinOpenToLanScreen.java     # 注入原版 LAN 界面
│   ├── network/
│   │   └── InternetOperator.java
│   ├── service/
│   │   ├── ConnectionService.java        # 连接服务核心
│   │   ├── KeyValidator.java
│   │   └── thread/                       # 后台线程
│   │       ├── CheckAliveTask.java       # 心跳检测
│   │       └── TCPTransformer.java       # TCP 数据转发
│   └── util/
│       ├── UUIDFixer.java                # UUID 修复工具
│       └── VersionInfo.java
└── src/main/resources/
    ├── assets/neolinkmc/
    │   ├── lang/zh_cn.json               # 中文语言文件
    │   └── icon.png
    ├── fabric.mod.json                   # Mod 元数据
    └── neolinkmc.mixins.json             # Mixin 配置
```

---

## 🔧 技术栈

- **Java 21** - 现代 Java 语言特性
- **Kotlin 2.1.0** - 部分构建脚本使用
- **Fabric API** - Minecraft Mod 开发框架
- **Modern UI 3.12.0** - 现代化 UI 渲染框架（**必需前置**）
- **Mixin** - 运行时字节码注入
- **Gradle + Loom** - 构建工具

---

## 🤔 常见问题

### Q: 安装后点击「对局域网开放」游戏崩溃？
**A:** 请确保已安装 **Modern UI** 3.12.0+ 前置 Mod，这是必需依赖。

### Q: 提示「密钥验证不通过」？
**A:** 
- 检查访问密钥是否正确输入
- 确认配置文件中的服务器地址和端口正确
- 检查网络连接是否正常

### Q: 好友无法连接？
**A:**
- 确认内网穿透服务已成功启动（看聊天栏提示）
- 检查好友使用的是正确的公网地址和端口
- 确认防火墙未拦截连接

### Q: 正版玩家显示为离线 UUID？
**A:** 在界面中选择「离线+UUID修复」模式，或修改配置 `onlinemode` 为 `OFFLINE_TRY_ONLINE_UUID_FIRST`。

### Q: 如何获取访问密钥？
**A:** 请联系 NeoProxy 服务提供商获取访问密钥。

---

## 📞 联系我们

- **QQ 群**: 304509047 💬
- **作者 QQ**: 1591117599 📧
- **GitHub Issues**: [提交问题](../../issues)

---

## 📜 许可证

本项目基于 [MIT License](LICENSE) 开源发布。

### 第三方依赖

| 依赖 | 许可证 |
|------|--------|
| [Fabric](https://fabricmc.net) | [Apache-2.0](https://github.com/FabricMC/fabric/blob/HEAD/LICENSE) |
| [Modern UI](https://github.com/BloCamLimb/ModernUI) | [LGPL-3.0](https://github.com/BloCamLimb/ModernUI/blob/HEAD/LICENSE) |

---

## ⚠️ 最终用户许可协议 (EULA)

使用本软件即表示您同意遵守以下条款：

1. 本软件仅供学习和个人娱乐使用
2. 禁止用于任何违法或商业用途
3. 作者对软件使用造成的任何损失不承担责任
4. 最终解释权归作者所有

详细内容请参阅 [eula.txt](eula.txt)

---

<div align="center">

**Made with ❤️ by M.T.S. Studio & Ceroxe**

</div>
