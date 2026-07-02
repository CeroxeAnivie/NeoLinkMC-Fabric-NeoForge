# NeoLinkMC-Fabric-NeoForge

NeoLinkMC-Fabric-NeoForge 是 NeoLinkMC 的 Fabric / NeoForge 独立仓库，负责两条 loader 入口、Minecraft 版本矩阵、资源模板和最终模组产物构建。

## 仓库职责

- `fabric/`：Fabric loader 入口与模块矩阵
- `neoforge/`：NeoForge loader 入口与模块矩阵
- `minecraftCompat/`：按 Minecraft 代际拆分的共享模板代码

公共 JVM 内核已经独立到 `NeoLinkMC-Common`，当前仓库通过 Maven 制品依赖它，而不是继续内嵌 `common/` 源码。

## 依赖仓库

- `NeoLinkMC-Common`
- `top.ceroxe.api:neolinkapi-desktop`

## 本地构建

首次验证前，先在 `NeoLinkMC-Common` 仓库执行本地发布：

```cmd
gradlew.bat publishMavenJavaPublicationToLocalDevelopmentRepository
```

然后在本仓库执行：

```cmd
gradlew.bat -Pneolinkmc_common_repo=D:/Engineering/code/NeoLinkMC-Common/build/repos/local-development :fabric:v1_21_11:remapJar
gradlew.bat -Pneolinkmc_common_repo=D:/Engineering/code/NeoLinkMC-Common/build/repos/local-development :neoforge:v26_1_2:jar
```

## 当前验证结论

- 热缓存 Fabric 单模块：约 `1.18s`
- 热缓存 NeoForge 单模块：约 `1.15s`
- 构建 JVM 上限：`4G`

首次冷构建会触发 Loom / ModDev 工件生成，耗时显著高于热缓存单模块，这是工具链初始化成本，不是硬件瓶颈。
