pluginManagement {
    val commonRepoPath = providers.gradleProperty("neolinkmc_common_repo").orNull
    repositories {
        if (!commonRepoPath.isNullOrBlank()) {
            maven {
                url = uri(commonRepoPath)
                content {
                    includeGroup("top.ceroxe.neolinkmc")
                }
            }
        }
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForge"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        if (providers.gradleProperty("useMavenLocal").map(String::toBoolean).orElse(false).get()) {
            mavenLocal()
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        val commonRepoPath = providers.gradleProperty("neolinkmc_common_repo").orNull
        if (!commonRepoPath.isNullOrBlank()) {
            maven {
                url = uri(commonRepoPath)
                content {
                    includeGroup("top.ceroxe.neolinkmc")
                }
            }
        }
        if (providers.gradleProperty("useMavenLocal").map(String::toBoolean).orElse(false).get()) {
            mavenLocal()
        }
        mavenCentral()
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForge"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://libraries.minecraft.net/") {
            name = "Mojang"
        }
    }
}

rootProject.name = "NeoLinkMC-Fabric-NeoForge"

val requestedTasks = gradle.startParameter.taskNames
val defaultSyncModules = mapOf(
    "fabric" to listOf(
        "v1_20", "v1_20_1", "v1_20_2", "v1_20_3", "v1_20_4", "v1_20_5", "v1_20_6",
        "v1_21", "v1_21_1", "v1_21_2", "v1_21_3", "v1_21_4", "v1_21_5", "v1_21_6",
        "v1_21_7", "v1_21_8", "v1_21_9", "v1_21_10", "v1_21_11",
        "v26_1", "v26_1_1", "v26_1_2", "v26_2"
    ),
    "neoforge" to listOf(
        "v1_20_4", "v1_20_6", "v1_21", "v1_21_1", "v1_21_2", "v1_21_3",
        "v1_21_4", "v1_21_5", "v1_21_6", "v1_21_8", "v1_21_9", "v1_21_10",
        "v1_21_11", "v26_1", "v26_1_1", "v26_1_2", "v26_2"
    )
)
val diagnosticRootTasks = setOf(
    "help",
    "tasks",
    "projects",
    "properties",
    "dependencies",
    "dependencyInsight",
    "components",
    "model",
    "wrapper"
)
val isDefaultSyncRequest = requestedTasks.isEmpty() ||
    requestedTasks.all { taskName -> ":" !in taskName && taskName in diagnosticRootTasks }
val shouldIncludeAllModules = providers.gradleProperty("neolinkmc.fullMatrix").orNull == "true" ||
    requestedTasks.any { taskName -> ":" !in taskName && taskName !in diagnosticRootTasks }

fun requestedModuleNames(loader: String, allModules: List<String>, defaultModules: List<String>): List<String> {
    if (shouldIncludeAllModules) {
        return allModules
    }

    if (isDefaultSyncRequest) {
        return allModules.filter { it in defaultModules }
    }

    val normalizedLoader = ":$loader"
    val requestedForLoader = requestedTasks.filter { taskName ->
        taskName == loader ||
            taskName == normalizedLoader ||
            taskName.startsWith("$loader:") ||
            taskName.startsWith("$normalizedLoader:")
    }

    if (requestedForLoader.isEmpty()) {
        return emptyList()
    }

    val requestedModules = requestedForLoader.mapNotNull { taskName ->
        val segments = taskName.trimStart(':').split(':')
        segments.getOrNull(1)?.takeIf { it.isNotBlank() }
    }.toSet()

    return if (requestedModules.isEmpty()) {
        allModules
    } else {
        allModules.filter { it in requestedModules }
    }
}

val fabricModules = listOf(
    "v1_20",
    "v1_20_1",
    "v1_20_2",
    "v1_20_3",
    "v1_20_4",
    "v1_20_5",
    "v1_20_6",
    "v1_21",
    "v1_21_1",
    "v1_21_2",
    "v1_21_3",
    "v1_21_4",
    "v1_21_5",
    "v1_21_6",
    "v1_21_7",
    "v1_21_8",
    "v1_21_9",
    "v1_21_10",
    "v1_21_11",
    "v26_1",
    "v26_1_1",
    "v26_1_2",
    "v26_2"
)

val neoForgeModules = listOf(
    "v1_20_4",
    "v1_20_6",
    "v1_21",
    "v1_21_1",
    "v1_21_2",
    "v1_21_3",
    "v1_21_4",
    "v1_21_5",
    "v1_21_6",
    "v1_21_8",
    "v1_21_9",
    "v1_21_10",
    "v1_21_11",
    "v26_1",
    "v26_1_1",
    "v26_1_2",
    "v26_2"
)

requestedModuleNames("fabric", fabricModules, defaultSyncModules.getValue("fabric")).takeIf { it.isNotEmpty() }?.let { modules ->
    include("fabric")
    modules.forEach { module ->
        include("fabric:$module")
    }
}

requestedModuleNames("neoforge", neoForgeModules, defaultSyncModules.getValue("neoforge")).takeIf { it.isNotEmpty() }?.let { modules ->
    include("neoforge")
    modules.forEach { module ->
        include("neoforge:$module")
    }
}
