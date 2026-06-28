buildscript {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        mavenCentral()
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:1.17.12")
    }
}

plugins {
    base
    id("maven-publish")
}

group = property("maven_group").toString()
version = property("mod_version").toString()

val neoLinkApiVersion = property("neolinkapi_version").toString()
val jetbrainsAnnotationsVersion = "26.0.2"
val requestedTasks = gradle.startParameter.taskNames

fun shouldConfigureFabricModule(projectPath: String): Boolean {
    if (requestedTasks.isEmpty()) {
        return true
    }

    val requestedProjectPaths = requestedTasks
        .filter { it.startsWith(":fabric:") }
        .map { taskName ->
            taskName.split(":")
                .take(3)
                .joinToString(":")
        }
        .toSet()

    return requestedProjectPaths.isEmpty() || projectPath in requestedProjectPaths
}

subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
        withSourcesJar()
    }

    tasks.withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = "UTF-8"
    }
}

project(":common") {
    apply(plugin = "java-library")

    dependencies {
        "api"("top.ceroxe.api:neolinkapi-desktop:$neoLinkApiVersion")
        "compileOnly"("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    }
}

project(":fabric") {
    tasks.named<Jar>("jar") {
        enabled = false
    }
}

fun Project.configureFabricModule(
    minecraftVersion: String,
    loaderVersion: String,
    fabricVersion: String,
    javaVersion: Int,
    versionSuffix: String,
    mappings: MinecraftMappings
) {
    apply(plugin = "fabric-loom")
    apply(plugin = "maven-publish")

    version = "${rootProject.version}+$versionSuffix"

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        options.release.set(javaVersion)
    }

    val archivesBaseName = rootProject.property("archives_base_name").toString()

    base {
        archivesName.set("$archivesBaseName-Fabric-$minecraftVersion")
    }

    val loom = extensions.getByType<net.fabricmc.loom.api.LoomGradleExtensionAPI>()
    if (mappings == MinecraftMappings.NO_INTERMEDIATE) {
        loom.useIntermediateMappings.set(false)
    }

    dependencies {
        add("minecraft", "com.mojang:minecraft:$minecraftVersion")
        when (mappings) {
            is MinecraftMappings.Yarn -> add("mappings", "net.fabricmc:yarn:${mappings.version}:v2")
            MinecraftMappings.Official -> add("mappings", loom.officialMojangMappings())
            MinecraftMappings.NO_INTERMEDIATE -> add("mappings", files(rootProject.file("gradle/mappings/minecraft-26.1-empty-named.jar")))
        }
        add("modImplementation", "net.fabricmc:fabric-loader:$loaderVersion")
        add("modImplementation", "net.fabricmc.fabric-api:fabric-api:$fabricVersion")
        add("compileOnly", "org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
        add("implementation", project(":common"))
        add("include", project(":common"))

        // NeoLinkAPI is the canonical tunnel implementation. Keep it as Maven
        // coordinates so a fresh GitHub clone can build without local paths.
        add("implementation", "top.ceroxe.api:neolinkapi-desktop:$neoLinkApiVersion")
        add("include", "top.ceroxe.api:neolinkapi-desktop:$neoLinkApiVersion")
        add("include", "top.ceroxe.api:neolinkapi-shared:$neoLinkApiVersion")
        add("include", "top.ceroxe.api:ceroxe-core:2.0.0")
        add("include", "top.ceroxe.api:ceroxe-detector:2.0.0")
    }

    val resourceProperties = mapOf(
        "version" to version.toString(),
        "minecraft_version" to minecraftVersion,
        "loader_version" to loaderVersion,
        "fabric_version" to fabricVersion
    )

    tasks.named<ProcessResources>("processResources") {
        inputs.properties(resourceProperties)
        filesMatching("fabric.mod.json") {
            expand(resourceProperties)
        }
    }

    tasks.named<Jar>("jar") {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_$archivesBaseName" }
        }
    }

    extensions.configure<PublishingExtension>("publishing") {
        publications {
            create<MavenPublication>("mavenJava") {
                artifactId = "$archivesBaseName-fabric-$minecraftVersion"
                from(components["java"])
            }
        }
    }
}

sealed interface MinecraftMappings {
    data class Yarn(val version: String) : MinecraftMappings
    data object Official : MinecraftMappings
    data object NO_INTERMEDIATE : MinecraftMappings
}

project(":fabric:v1_21_8") {
    if (shouldConfigureFabricModule(path)) {
        configureFabricModule(
            minecraftVersion = "1.21.8",
            loaderVersion = "0.16.14",
            fabricVersion = "0.134.0+1.21.8",
            javaVersion = 21,
            versionSuffix = "fabric.1.21-1.21.8",
            mappings = MinecraftMappings.Official
        )
    }
}

project(":fabric:v26_1") {
    if (shouldConfigureFabricModule(path)) {
        configureFabricModule(
            minecraftVersion = "26.1",
            loaderVersion = "0.18.5",
            fabricVersion = "0.144.0+26.1",
            javaVersion = 25,
            versionSuffix = "fabric.26.1",
            mappings = MinecraftMappings.NO_INTERMEDIATE
        )
    }
}
