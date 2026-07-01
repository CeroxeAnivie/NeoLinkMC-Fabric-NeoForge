# NeoLinkMC

Minecraft 内网穿透 Mod，用于在单人世界的“开启内网穿透”流程中启动 NeoLink 隧道，让本地 LAN 服务可以通过公网节点访问。

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20--1.21.11-green?style=flat-square&logo=minecraft)](https://minecraft.net)
[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-orange?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net)
[![Fabric](https://img.shields.io/badge/Fabric-supported-blue?style=flat-square)](https://fabricmc.net)
[![Forge](https://img.shields.io/badge/Forge-supported-orange?style=flat-square)](https://files.minecraftforge.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-supported-red?style=flat-square)](https://neoforged.net)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](https://opensource.org/licenses/MIT)

## 支持版本

| Loader | Minecraft | Java | 说明 |
|--------|-----------|------|------|
| Fabric | `1.20` - `1.21.11` | 17 / 21 | `1.20` - `1.20.4` 以 Java 17 release 编译，其余模块使用 Java 21 |
| Forge | `1.20` - `1.20.4`, `1.20.6`, `1.21`, `1.21.1`, `1.21.3` - `1.21.10` | 17 / 21 | `1.20` - `1.20.4` 以 Java 17 release 编译，其余模块使用 Java 21 |
| NeoForge | `1.20.4`, `1.20.6`, `1.21` - `1.21.6`, `1.21.8` - `1.21.11` | 17 / 21 | `1.20.4` 以 Java 17 release 编译，其余模块使用 Java 21 |

已跳过的不可用或异常版本：

| Loader | Minecraft | 原因 |
|--------|-----------|------|
| Forge | `1.20.5`, `1.21.2` | Forge Maven 未发布对应 `net.minecraftforge:forge` 坐标 |
| Forge | `1.21.11`, `26.1` | ForgeGradle 当前无法生成或解析对应 `_mapped_official` artifact |
| Fabric / NeoForge | `26.1` | 当前 Gradle 8.14.3 Kotlin DSL 无法在本机 Java 25.0.1 上解析运行时版本，且 Java 21 daemon 无法配置 Minecraft 26.1 |
| NeoForge | `1.20.2`, `1.20.3`, `1.20.5` | Maven artifact 缺少当前 ModDevGradle 需要的 `.module` capability |
| NeoForge | `1.21.7` | `createMinecraftArtifacts` 在当前 NeoForge beta 坐标下长时间无产物 |

## 功能

| 功能 | 说明 |
|------|------|
| 游戏内启动 | 接管“开启内网穿透”入口，配置完成后直接启动内网穿透 |
| LAN 设置同步 | 支持游戏模式、作弊、PVP、最大玩家数和在线模式配置 |
| 在线模式 | 支持正版 UUID、离线 UUID 以及优先正版 UUID 的兼容模式 |
| NeoLink 隧道 | 基于 NeoLinkAPI Maven 依赖启动 TCP 转发 |
| 配置文件 | 自动读取和保存 `.minecraft/config/neolinkmc/config.json` |

## 安装

1. 安装目标 Minecraft 版本对应的 Fabric、Forge 或 NeoForge。
2. 从 Releases 下载对应 Loader 和 Minecraft 版本的 NeoLinkMC jar。
3. 将 jar 放入 `.minecraft/mods/`。
4. 进入单人世界，按 Esc，点击“开启内网穿透”。

Fabric 版本还需要安装对应 Minecraft 版本的 Fabric API。

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

使用项目内 Gradle Wrapper 构建。目标模块任务会按需加载对应子项目，避免单版本构建扫描完整版本矩阵。

```cmd
gradlew.bat :fabric:v1_21_8:jar
gradlew.bat :forge:v1_21_8:jar
gradlew.bat :neoforge:v1_21_8:jar
```

构建配置默认开启 Gradle daemon、build cache、parallel、configure-on-demand。IDE 同步和空任务导入默认只加载 `common` 与 `fabric:v1_21_8`，避免同步阶段生成全部 Minecraft artifact；需要完整矩阵时显式执行根构建：

```cmd
gradlew.bat build -Pneolinkmc.fullMatrix=true
```

需要代理时请使用本机临时参数或用户级 Gradle 配置，不要写入仓库配置。

首次构建 Forge/NeoForge 版本时，Gradle 插件会下载 Minecraft client/server jar，并生成 mappings、sources 和 userdev artifacts；这个阶段可能需要数分钟。缓存命中后，同一模块通常会回到秒级构建。

输出位置：

```text
fabric/<module>/build/libs/
forge/<module>/build/libs/
neoforge/<module>/build/libs/
```

## 许可

本项目基于 [MIT License](LICENSE) 发布。
