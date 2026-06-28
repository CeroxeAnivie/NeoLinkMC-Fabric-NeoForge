buildscript {
    repositories {
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.minecraftforge.net/") {
            name = "Forge"
        }
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:1.13.6")
        classpath("net.minecraftforge.gradle:ForgeGradle:6.0.54")
        classpath("net.neoforged:moddev-gradle:2.0.141")
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

sealed interface MinecraftMappings {
    object Official : MinecraftMappings
    object NoIntermediate : MinecraftMappings
}

data class FabricModuleSpec(
    val moduleName: String,
    val minecraftVersion: String,
    val loaderVersion: String,
    val fabricVersion: String,
    val javaVersion: Int,
    val mappings: MinecraftMappings,
    val sourceTemplate: String
) {
    val projectPath: String = ":fabric:$moduleName"
}

data class ForgeModuleSpec(
    val moduleName: String,
    val minecraftVersion: String,
    val forgeVersion: String,
    val javaVersion: Int,
    val sourceTemplate: String = "v1_21_8",
    val loaderTemplate: String = "forge/template",
    val compileRelease: Int = javaVersion
) {
    val projectPath: String = ":forge:$moduleName"
}

data class NeoForgeModuleSpec(
    val moduleName: String,
    val minecraftVersion: String,
    val neoForgeVersion: String,
    val javaVersion: Int,
    val sourceTemplate: String = "v1_21_8"
) {
    val projectPath: String = ":neoforge:$moduleName"
}

val fabricModules = listOf(
    FabricModuleSpec("v1_20", "1.20", "0.16.14", "0.83.0+1.20", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_20_1", "1.20.1", "0.16.14", "0.92.9+1.20.1", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_20_2", "1.20.2", "0.16.14", "0.91.6+1.20.2", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_20_3", "1.20.3", "0.16.14", "0.91.1+1.20.3", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_20_4", "1.20.4", "0.16.14", "0.97.3+1.20.4", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_20_5", "1.20.5", "0.16.14", "0.97.8+1.20.5", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_20_6", "1.20.6", "0.16.14", "0.100.8+1.20.6", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21", "1.21", "0.16.14", "0.100.8+1.21", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_1", "1.21.1", "0.16.14", "0.116.12+1.21.1", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_2", "1.21.2", "0.16.14", "0.106.1+1.21.2", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_3", "1.21.3", "0.16.14", "0.114.1+1.21.3", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_4", "1.21.4", "0.16.14", "0.119.4+1.21.4", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_5", "1.21.5", "0.16.14", "0.128.2+1.21.5", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_6", "1.21.6", "0.16.14", "0.128.2+1.21.6", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_7", "1.21.7", "0.16.14", "0.129.0+1.21.7", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_8", "1.21.8", "0.16.14", "0.134.0+1.21.8", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_9", "1.21.9", "0.16.14", "0.134.1+1.21.9", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_10", "1.21.10", "0.16.14", "0.138.4+1.21.10", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v1_21_11", "1.21.11", "0.16.14", "0.141.4+1.21.11", 21, MinecraftMappings.Official, "v1_21_8"),
    FabricModuleSpec("v26_1", "26.1", "0.18.5", "0.144.0+26.1", 25, MinecraftMappings.NoIntermediate, "v26_1")
)

val forgeModules = listOf(
    ForgeModuleSpec("v1_20", "1.20", "1.20-46.0.14", 21, loaderTemplate = "forge/template_legacy", compileRelease = 17),
    ForgeModuleSpec("v1_20_1", "1.20.1", "1.20.1-47.4.20", 21, loaderTemplate = "forge/template_legacy", compileRelease = 17),
    ForgeModuleSpec("v1_20_2", "1.20.2", "1.20.2-48.1.0", 21, loaderTemplate = "forge/template_legacy", compileRelease = 17),
    ForgeModuleSpec("v1_20_3", "1.20.3", "1.20.3-49.0.2", 21, loaderTemplate = "forge/template_legacy", compileRelease = 17),
    ForgeModuleSpec("v1_20_4", "1.20.4", "1.20.4-49.2.7", 21, loaderTemplate = "forge/template_legacy", compileRelease = 17),
    ForgeModuleSpec("v1_20_6", "1.20.6", "1.20.6-50.2.8", 21, loaderTemplate = "forge/template_legacy"),
    ForgeModuleSpec("v1_21", "1.21", "1.21-51.0.33", 21, loaderTemplate = "forge/template_legacy"),
    ForgeModuleSpec("v1_21_1", "1.21.1", "1.21.1-52.1.14", 21, loaderTemplate = "forge/template_legacy"),
    ForgeModuleSpec("v1_21_3", "1.21.3", "1.21.3-53.1.10", 21, loaderTemplate = "forge/template_legacy"),
    ForgeModuleSpec("v1_21_4", "1.21.4", "1.21.4-54.1.16", 21, loaderTemplate = "forge/template_legacy"),
    ForgeModuleSpec("v1_21_5", "1.21.5", "1.21.5-55.1.10", 21, loaderTemplate = "forge/template_legacy"),
    ForgeModuleSpec("v1_21_6", "1.21.6", "1.21.6-56.0.9", 21),
    ForgeModuleSpec("v1_21_7", "1.21.7", "1.21.7-57.0.3", 21),
    ForgeModuleSpec("v1_21_8", "1.21.8", "1.21.8-58.1.18", 21),
    ForgeModuleSpec("v1_21_9", "1.21.9", "1.21.9-59.0.5", 21),
    ForgeModuleSpec("v1_21_10", "1.21.10", "1.21.10-60.1.9", 21)
)

val neoForgeModules = listOf(
    NeoForgeModuleSpec("v1_20_4", "1.20.4", "20.4.251", 21),
    NeoForgeModuleSpec("v1_20_6", "1.20.6", "20.6.139", 21),
    NeoForgeModuleSpec("v1_21", "1.21", "21.0.167", 21),
    NeoForgeModuleSpec("v1_21_1", "1.21.1", "21.1.234", 21),
    NeoForgeModuleSpec("v1_21_2", "1.21.2", "21.2.1-beta", 21),
    NeoForgeModuleSpec("v1_21_3", "1.21.3", "21.3.96", 21),
    NeoForgeModuleSpec("v1_21_4", "1.21.4", "21.4.157", 21),
    NeoForgeModuleSpec("v1_21_5", "1.21.5", "21.5.97", 21),
    NeoForgeModuleSpec("v1_21_6", "1.21.6", "21.6.20-beta", 21),
    NeoForgeModuleSpec("v1_21_8", "1.21.8", "21.8.53", 21),
    NeoForgeModuleSpec("v1_21_9", "1.21.9", "21.9.16-beta", 21),
    NeoForgeModuleSpec("v1_21_10", "1.21.10", "21.10.64", 21),
    NeoForgeModuleSpec("v1_21_11", "1.21.11", "21.11.42", 21),
    NeoForgeModuleSpec("v26_1", "26.1", "26.1.2.76", 25, "v26_1")
)

fun shouldConfigureLoaderModule(projectPath: String, rootPath: String): Boolean {
    if (requestedTasks.isEmpty()) {
        return true
    }

    if (requestedTasks.none { it.startsWith("$rootPath:") }) {
        return false
    }

    val requestedProjectPaths = requestedTasks
        .filter { it.startsWith("$rootPath:") }
        .map { taskName -> taskName.split(":").take(3).joinToString(":") }
        .toSet()

    return requestedProjectPaths.isEmpty() || projectPath in requestedProjectPaths
}

fun Project.configureJavaCompilerRelease(javaVersion: Int) {
    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        }
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }

    val toolchains = extensions.getByType<JavaToolchainService>()
    tasks.withType<JavaCompile>().configureEach {
        javaCompiler.set(toolchains.compilerFor {
            languageVersion.set(JavaLanguageVersion.of(javaVersion))
        })
        options.encoding = "UTF-8"
        options.release.set(javaVersion)
    }
}

fun Project.officialMojangMappingsDependency(): Any {
    val loomExtension = extensions.findByName("loom")
        ?: throw GradleException("Fabric Loom extension is not registered for $path")
    val mappingsMethod = loomExtension.javaClass.methods
        .firstOrNull { method -> method.name == "officialMojangMappings" && method.parameterCount == 0 }
        ?: throw GradleException("Fabric Loom extension in $path does not expose officialMojangMappings()")
    return mappingsMethod.invoke(loomExtension)
}

subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    configureJavaCompilerRelease(21)

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
    }

    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = "UTF-8"
    }
}

project(":common") {
    apply(plugin = "java-library")

    dependencies {
        "implementation"("top.ceroxe.api:neolinkapi-desktop:$neoLinkApiVersion") {
            exclude(group = "org.slf4j", module = "slf4j-nop")
        }
        "compileOnly"("org.slf4j:slf4j-api:2.0.16")
        "compileOnly"("org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    }
}

project(":fabric") {
    tasks.named<Jar>("jar") {
        enabled = false
    }
}

listOf(":forge", ":neoforge").forEach { loaderRoot ->
    project(loaderRoot) {
        tasks.named<Jar>("jar") {
            enabled = false
        }
    }
}

fun Project.addSharedMinecraftTemplate(sourceTemplate: String, loaderTemplate: String) {
    val minecraftTemplateDir = rootProject.layout.projectDirectory.dir("fabric/$sourceTemplate").asFile
    val loaderTemplateDir = rootProject.layout.projectDirectory.dir(loaderTemplate).asFile
    extensions.configure<SourceSetContainer> {
        named("main") {
            java.srcDir(minecraftTemplateDir.resolve("src/main/java"))
            java.exclude("neoproxy/neolinkmc/NeoLinkMC.java")
            resources.srcDir(minecraftTemplateDir.resolve("src/main/resources"))
            resources.srcDir(loaderTemplateDir.resolve("src/main/resources"))
        }
    }

    tasks.withType<JavaCompile>().configureEach {
        source(loaderTemplateDir.resolve("src/main/java"))
    }

    tasks.withType<ProcessResources>().configureEach {
        exclude("fabric.mod.json")
    }
}

fun Project.addBundledNeoLinkDependencies(bundleConfigurationName: String) {
    val bundle = configurations.create(bundleConfigurationName) {
        isCanBeConsumed = false
        isCanBeResolved = true
        isTransitive = true
    }

    dependencies {
        add("implementation", project(":common"))
        add(bundle.name, project(":common"))
        add(bundle.name, "top.ceroxe.api:neolinkapi-desktop:$neoLinkApiVersion") {
            exclude(group = "org.slf4j", module = "slf4j-nop")
        }
        add(bundle.name, "top.ceroxe.api:neolinkapi-shared:$neoLinkApiVersion")
        add(bundle.name, "top.ceroxe.api:ceroxe-core:2.0.0")
        add(bundle.name, "top.ceroxe.api:ceroxe-detector:2.0.0")
    }

    tasks.named<Jar>("jar") {
        dependsOn(bundle)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(bundle.map { dependency ->
            if (dependency.isDirectory) dependency else zipTree(dependency)
        })
    }
}

fun Project.disableEmptyLoaderTests() {
    tasks.withType<Test>().configureEach {
        enabled = false
    }
    tasks.withType<JavaCompile>().configureEach {
        if (name == "compileTestJava") {
            enabled = false
        }
    }
}

fun Project.configureFabricModule(spec: FabricModuleSpec) {
    apply(plugin = "fabric-loom")
    apply(plugin = "maven-publish")

    version = rootProject.version

    configureJavaCompilerRelease(spec.javaVersion)

    val templateDir = rootProject.layout.projectDirectory.dir("fabric/${spec.sourceTemplate}").asFile
    if (!layout.projectDirectory.dir("src").asFile.exists()) {
        extensions.configure<SourceSetContainer> {
            named("main") {
                java.srcDir(templateDir.resolve("src/main/java"))
                resources.srcDir(templateDir.resolve("src/main/resources"))
            }
        }
    }

    val archivesBaseName = rootProject.property("archives_base_name").toString()
    base {
        archivesName.set("$archivesBaseName-Fabric-${spec.minecraftVersion}")
    }

    if (spec.mappings == MinecraftMappings.NoIntermediate) {
        extensions.findByName("loom")
            ?.javaClass
            ?.methods
            ?.firstOrNull { it.name == "getUseIntermediateMappings" }
            ?.invoke(extensions.findByName("loom"))
            ?.javaClass
            ?.methods
            ?.firstOrNull { it.name == "set" }
            ?.invoke(
                extensions.findByName("loom")
                    ?.javaClass
                    ?.methods
                    ?.firstOrNull { it.name == "getUseIntermediateMappings" }
                    ?.invoke(extensions.findByName("loom")),
                false
            )
    }

    dependencies {
        add("minecraft", "com.mojang:minecraft:${spec.minecraftVersion}")
        when (spec.mappings) {
            MinecraftMappings.Official -> add("mappings", officialMojangMappingsDependency())
            MinecraftMappings.NoIntermediate -> add("mappings", files(rootProject.file("gradle/mappings/minecraft-26.1-empty-named.jar")))
        }
        add("modImplementation", "net.fabricmc:fabric-loader:${spec.loaderVersion}")
        add("modImplementation", "net.fabricmc.fabric-api:fabric-api:${spec.fabricVersion}")
        add("compileOnly", "org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
        add("implementation", project(":common"))
        add("include", project(":common"))

        add("include", "top.ceroxe.api:neolinkapi-desktop:$neoLinkApiVersion")
        add("include", "top.ceroxe.api:neolinkapi-shared:$neoLinkApiVersion")
        add("include", "top.ceroxe.api:ceroxe-core:2.0.0")
        add("include", "top.ceroxe.api:ceroxe-detector:2.0.0")
    }

    val resourceProperties = mapOf(
        "version" to version.toString(),
        "minecraft_version" to spec.minecraftVersion,
        "loader_version" to spec.loaderVersion,
        "fabric_version" to spec.fabricVersion,
        "java_version" to spec.javaVersion.toString()
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
                artifactId = "$archivesBaseName-fabric-${spec.minecraftVersion}"
                from(components["java"])
            }
        }
    }
}

fabricModules.forEach { spec ->
    project(spec.projectPath) {
        if (shouldConfigureLoaderModule(path, ":fabric")) {
            configureFabricModule(spec)
        }
    }
}

fun Project.configureForgeModule(spec: ForgeModuleSpec) {
    apply(plugin = "net.minecraftforge.gradle")
    apply(plugin = "maven-publish")

    version = rootProject.version

    configureJavaCompilerRelease(spec.javaVersion)
    tasks.withType<JavaCompile>().configureEach {
        options.release.set(spec.compileRelease)
    }
    configurations.configureEach {
        if (isCanBeResolved) {
            attributes.attribute(
                org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,
                spec.javaVersion
            )
        }
    }

    addSharedMinecraftTemplate(spec.sourceTemplate, spec.loaderTemplate)
    addBundledNeoLinkDependencies("forgeBundle")
    disableEmptyLoaderTests()

    val archivesBaseName = rootProject.property("archives_base_name").toString()
    base {
        archivesName.set("$archivesBaseName-Forge-${spec.minecraftVersion}")
    }

    extensions.configure<net.minecraftforge.gradle.userdev.UserDevExtension>("minecraft") {
        mappings("official", spec.minecraftVersion)
    }

    dependencies {
        add("minecraft", "net.minecraftforge:forge:${spec.forgeVersion}")
        add("compileOnly", "org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    }

    val resourceProperties = mapOf(
        "version" to version.toString(),
        "minecraft_version" to spec.minecraftVersion,
        "minecraft_version_range" to "[${spec.minecraftVersion},${spec.minecraftVersion}]",
        "loader_version_range" to "[${spec.forgeVersion.substringAfter('-').substringBefore('.')},)",
        "forge_version_range" to "[${spec.forgeVersion.substringAfter('-')},)"
    )

    tasks.named<ProcessResources>("processResources") {
        inputs.properties(resourceProperties)
        filesMatching("META-INF/mods.toml") {
            expand(resourceProperties)
        }
    }

    tasks.named<Jar>("jar") {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_$archivesBaseName" }
        }
        manifest.attributes(
            "MixinConfigs" to "neolinkmc.mixins.json"
        )
    }
}

forgeModules.forEach { spec ->
    project(spec.projectPath) {
        if (shouldConfigureLoaderModule(path, ":forge")) {
            configureForgeModule(spec)
        }
    }
}

fun Project.configureNeoForgeModule(spec: NeoForgeModuleSpec) {
    apply(plugin = "net.neoforged.moddev")
    apply(plugin = "maven-publish")

    version = rootProject.version

    configureJavaCompilerRelease(spec.javaVersion)

    addSharedMinecraftTemplate(spec.sourceTemplate, "neoforge/template")
    addBundledNeoLinkDependencies("neoForgeBundle")
    disableEmptyLoaderTests()

    val archivesBaseName = rootProject.property("archives_base_name").toString()
    base {
        archivesName.set("$archivesBaseName-NeoForge-${spec.minecraftVersion}")
    }

    extensions.configure<net.neoforged.moddevgradle.dsl.NeoForgeExtension>("neoForge") {
        setVersion(spec.neoForgeVersion)
    }

    dependencies {
        add("compileOnly", "org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
    }

    val resourceProperties = mapOf(
        "version" to version.toString(),
        "minecraft_version" to spec.minecraftVersion,
        "minecraft_version_range" to "[${spec.minecraftVersion},${spec.minecraftVersion}]",
        "loader_version_range" to "[1,)",
        "neoforge_version_range" to "[${spec.neoForgeVersion},)"
    )

    tasks.named<ProcessResources>("processResources") {
        inputs.properties(resourceProperties)
        filesMatching("META-INF/neoforge.mods.toml") {
            expand(resourceProperties)
        }
    }

    tasks.named<Jar>("jar") {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_$archivesBaseName" }
        }
        manifest.attributes(
            "MixinConfigs" to "neolinkmc.mixins.json"
        )
    }
}

neoForgeModules.forEach { spec ->
    project(spec.projectPath) {
        if (shouldConfigureLoaderModule(path, ":neoforge")) {
            configureNeoForgeModule(spec)
        }
    }
}
