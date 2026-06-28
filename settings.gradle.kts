pluginManagement {
    repositories {
        maven("https://maven.minecraftforge.net/") {
            name = "Forge"
        }
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForge"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        mavenCentral()
        maven("https://maven.minecraftforge.net/") {
            name = "Forge"
        }
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

rootProject.name = "NeoLinkMC"

include("common")
include("fabric")
include("forge")
include("neoforge")

val minecraftModules = listOf(
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
    "v26_1"
)

minecraftModules.forEach { module ->
    include("fabric:$module")
}

listOf(
    "v1_20",
    "v1_20_1",
    "v1_20_2",
    "v1_20_3",
    "v1_20_4",
    "v1_20_6",
    "v1_21",
    "v1_21_1",
    "v1_21_3",
    "v1_21_4",
    "v1_21_5",
    "v1_21_6",
    "v1_21_7",
    "v1_21_8",
    "v1_21_9",
    "v1_21_10"
).forEach { module ->
    include("forge:$module")
}

listOf(
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
    "v26_1"
).forEach { module ->
    include("neoforge:$module")
}
