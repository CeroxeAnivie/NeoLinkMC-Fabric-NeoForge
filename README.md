# NeoLinkMC

Minecraft Fabric 内网穿透 Mod，用于在单人世界的“对局域网开放”流程中启动 NeoLink 隧道，让本地 LAN 服务可以通过公网节点访问。

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.8%20%7C%2026.1-green?style=flat-square&logo=minecraft)](https://minecraft.net)
[![Java](https://img.shields.io/badge/Java-21%2B%20%7C%2025%2B-orange?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net)
[![Fabric](https://img.shields.io/badge/Fabric-Loom%201.17.12-blue?style=flat-square)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](https://opensource.org/licenses/MIT)

## 支持版本

| 模块 | Minecraft | Java | Fabric Loader | Fabric API |
|------|-----------|------|---------------|------------|
| `fabric:v1_21_8` | `>=1.21 <=1.21.8` | 21+ | `0.16.14` | `0.134.0+1.21.8` |
| `fabric:v26_1` | `26.1` | 25+ | `0.18.5` | `0.144.0+26.1` |

## 功能

| 功能 | 说明 |
|------|------|
| 游戏内启动 | 接管“对局域网开放”入口，配置完成后直接启动内网穿透 |
| LAN 设置同步 | 支持游戏模式、作弊、PVP、最大玩家数和在线模式配置 |
| 在线模式 | 支持正版 UUID、离线 UUID 以及优先正版 UUID 的兼容模式 |
| NeoLink 隧道 | 基于 NeoLinkAPI Maven 依赖启动 TCP 转发 |
| 配置文件 | 自动读取和保存 `.minecraft/config/neolinkmc/config.json` |

## 安装

1. 安装对应 Minecraft 版本的 Fabric Loader。
2. 安装对应版本的 Fabric API。
3. 从 Releases 下载对应版本的 NeoLinkMC Fabric jar。
4. 将 jar 放入 `.minecraft/mods/`。
5. 进入单人世界，按 Esc，点击“对局域网开放”。

## 配置

配置文件位置：

```text
.minecraft/config/neolinkmc/config.json
```

默认配置示例：

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

密钥不会写入配置文件。界面中留空时会使用默认密钥 `Free`。

## 构建

使用项目内 Gradle Wrapper 构建：

```cmd
gradlew.bat :fabric:v1_21_8:build
gradlew.bat :fabric:v26_1:build
```

输出位置：

```text
fabric/v1_21_8/build/libs/
fabric/v26_1/build/libs/
```

## 许可

本项目基于 [MIT License](LICENSE) 发布。
