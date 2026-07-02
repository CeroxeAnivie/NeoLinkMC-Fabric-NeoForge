buildscript {
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
        if (providers.gradleProperty("useMavenLocal").map(String::toBoolean).orElse(false).get()) {
            mavenLocal {
                content {
                    includeGroup("top.ceroxe.api")
                    includeGroup("top.ceroxe.neolinkmc")
                }
            }
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForge"
        }
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:1.13.6")
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
val neoLinkMcCommonVersion = providers.gradleProperty("neolinkmc_common_version")
    .orElse(providers.gradleProperty("mod_version"))
    .get()
val neoLinkMcCommonCoordinate = "top.ceroxe.neolinkmc:neolinkmc-common:$neoLinkMcCommonVersion"
val jetbrainsAnnotationsVersion = "26.0.2"
val requestedTasks = gradle.startParameter.taskNames
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
val isDiagnosticOnlyRequest = requestedTasks.isNotEmpty() &&
    requestedTasks.all { taskName -> ":" !in taskName && taskName in diagnosticRootTasks }

val representativeFabricModule = "v26_1_2"
val representativeNeoForgeModule = "v26_1_2"
val lightweightRootTasks = setOf(
    "verifyRepresentativeFabric",
    "verifyRepresentativeNeoForge",
    "verifyRepresentativeLoaders"
)
val representativeProjectPaths = setOf(
    ":fabric:$representativeFabricModule",
    ":neoforge:$representativeNeoForgeModule"
)
val isRepresentativeOnlyRequest = requestedTasks.isNotEmpty() &&
    requestedTasks.all { taskName -> ":" !in taskName && taskName in lightweightRootTasks }

data class FabricModuleSpec(
    val moduleName: String,
    val minecraftVersion: String,
    val loaderVersion: String,
    val fabricVersion: String,
    val javaVersion: Int,
    val sourceTemplate: String,
    val loaderVersionRange: String = ">=0.15.0",
    val mappingsArtifact: String? = null,
    val requiresFabricApi: Boolean = true
) {
    val projectPath: String = ":fabric:$moduleName"
}

data class NeoForgeModuleSpec(
    val moduleName: String,
    val minecraftVersion: String,
    val neoForgeVersion: String,
    val javaVersion: Int,
    val sourceTemplate: String = "v1_21_8",
    val neoForgeVersionRange: String = neoForgeMajorVersionRange(neoForgeVersion)
) {
    val projectPath: String = ":neoforge:$moduleName"
}

val fabricModules = listOf(
    FabricModuleSpec("v1_20", "1.20", "0.16.14", "0.83.0+1.20", 17, "v1_21_8"),
    FabricModuleSpec("v1_20_1", "1.20.1", "0.16.14", "0.92.9+1.20.1", 17, "v1_21_8"),
    FabricModuleSpec("v1_20_2", "1.20.2", "0.16.14", "0.91.6+1.20.2", 17, "v1_21_8"),
    FabricModuleSpec("v1_20_3", "1.20.3", "0.16.14", "0.91.1+1.20.3", 17, "v1_21_8"),
    FabricModuleSpec("v1_20_4", "1.20.4", "0.16.14", "0.97.3+1.20.4", 17, "v1_21_8"),
    FabricModuleSpec("v1_20_5", "1.20.5", "0.16.14", "0.97.8+1.20.5", 21, "v1_21_8"),
    FabricModuleSpec("v1_20_6", "1.20.6", "0.16.14", "0.100.8+1.20.6", 21, "v1_21_8"),
    FabricModuleSpec("v1_21", "1.21", "0.16.14", "0.100.8+1.21", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_1", "1.21.1", "0.16.14", "0.116.12+1.21.1", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_2", "1.21.2", "0.16.14", "0.106.1+1.21.2", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_3", "1.21.3", "0.16.14", "0.114.1+1.21.3", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_4", "1.21.4", "0.16.14", "0.119.4+1.21.4", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_5", "1.21.5", "0.16.14", "0.128.2+1.21.5", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_6", "1.21.6", "0.16.14", "0.128.2+1.21.6", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_7", "1.21.7", "0.16.14", "0.129.0+1.21.7", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_8", "1.21.8", "0.16.14", "0.134.0+1.21.8", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_9", "1.21.9", "0.16.14", "0.134.1+1.21.9", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_10", "1.21.10", "0.16.14", "0.138.4+1.21.10", 21, "v1_21_8"),
    FabricModuleSpec("v1_21_11", "1.21.11", "0.16.14", "0.141.4+1.21.11", 21, "v1_21_8"),
    FabricModuleSpec("v26_1", "26.1", "0.19.3", "0.145.1+26.1", 25, "v26_1", mappingsArtifact = "gradle/mappings/minecraft-26.1-empty-named.jar", requiresFabricApi = false),
    FabricModuleSpec("v26_1_1", "26.1.1", "0.19.3", "0.145.4+26.1.1", 25, "v26_1", mappingsArtifact = "gradle/mappings/minecraft-26.1-empty-named.jar", requiresFabricApi = false),
    FabricModuleSpec("v26_1_2", "26.1.2", "0.19.3", "0.153.0+26.1.2", 25, "v26_1", mappingsArtifact = "gradle/mappings/minecraft-26.1-empty-named.jar", requiresFabricApi = false),
    FabricModuleSpec("v26_2", "26.2", "0.19.3", "0.153.0+26.2", 25, "v26_1", mappingsArtifact = "gradle/mappings/minecraft-26.1-empty-named.jar", requiresFabricApi = false)
)

val neoForgeModules = listOf(
    NeoForgeModuleSpec("v1_20_4", "1.20.4", "20.4.251", 17),
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
    NeoForgeModuleSpec("v26_1", "26.1", "26.1.0.19-beta", 25, sourceTemplate = "v26_1"),
    NeoForgeModuleSpec("v26_1_1", "26.1.1", "26.1.1.15-beta", 25, sourceTemplate = "v26_1"),
    NeoForgeModuleSpec("v26_1_2", "26.1.2", "26.1.2.76", 25, sourceTemplate = "v26_1"),
    NeoForgeModuleSpec("v26_2", "26.2", "26.2.0.7-beta", 25, sourceTemplate = "v26_1")
)

fun shouldConfigureLoaderModule(projectPath: String, rootPath: String): Boolean {
    if (isDiagnosticOnlyRequest) {
        return false
    }

    if (requestedTasks.isEmpty()) {
        return true
    }

    if (isRepresentativeOnlyRequest) {
        return projectPath in representativeProjectPaths
    }

    val rootLevelTaskRequested = requestedTasks.any { taskName -> ":" !in taskName }
    if (rootLevelTaskRequested) {
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
        sourceCompatibility = JavaVersion.toVersion(javaVersion)
        targetCompatibility = JavaVersion.toVersion(javaVersion)
    }
    tasks.withType<JavaCompile>().configureEach {
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

fun Project.disableFabricIntermediateMappings() {
    extensions.configure<net.fabricmc.loom.api.LoomGradleExtensionAPI>("loom") {
        noIntermediateMappings()
        enableTransitiveAccessWideners.set(false)
    }
}

fun exactMinecraftVersionRange(version: String): String {
    val parts = version.split(".")
    require(parts.size >= 2) { "Unsupported Minecraft version: $version" }

    val upperBound = if (parts.size >= 3) {
        parts.toMutableList().also { segments ->
            segments[2] = (segments[2].toInt() + 1).toString()
        }.joinToString(".")
    } else {
        "${parts[0]}.${parts[1].toInt() + 1}"
    }

    return "[$version,$upperBound)"
}

fun neoForgeMajorVersionRange(neoForgeVersion: String): String {
    val majorVersion = neoForgeVersion.substringBefore('.')
    require(majorVersion.toIntOrNull() != null) { "Unsupported NeoForge version: $neoForgeVersion" }
    return "[$majorVersion,)"
}

fun mixinCompatibilityLevel(javaVersion: Int): String {
    return "JAVA_$javaVersion"
}

subprojects {
    apply(plugin = "java")

    group = rootProject.group
    version = rootProject.version

    repositories {
        val commonRepoPath = rootProject.providers.gradleProperty("neolinkmc_common_repo").orNull
        if (!commonRepoPath.isNullOrBlank()) {
            maven {
                url = uri(commonRepoPath)
                content {
                    includeGroup("top.ceroxe.neolinkmc")
                }
            }
        }
        if (providers.gradleProperty("useMavenLocal").map(String::toBoolean).orElse(false).get()) {
            mavenLocal {
                content {
                    includeGroup("top.ceroxe.api")
                    includeGroup("top.ceroxe.neolinkmc")
                }
            }
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

    configureJavaCompilerRelease(21)

    extensions.configure<JavaPluginExtension> {
        withSourcesJar()
    }

    tasks.withType<ProcessResources>().configureEach {
        filteringCharset = "UTF-8"
    }
}

findProject(":fabric")?.tasks?.named<Jar>("jar") {
    enabled = false
}

findProject(":neoforge")?.tasks?.named<Jar>("jar") {
    enabled = false
}

tasks.register("verifyRepresentativeFabric") {
    group = "verification"
    description = "Builds the default Fabric representative module used for cold-start validation."
    dependsOn(":fabric:$representativeFabricModule:remapJar")
}

tasks.register("verifyRepresentativeNeoForge") {
    group = "verification"
    description = "Builds the default NeoForge representative module used for cold-start validation."
    dependsOn(":neoforge:$representativeNeoForgeModule:jar")
}

tasks.register("verifyRepresentativeLoaders") {
    group = "verification"
    description = "Builds the default Fabric and NeoForge representative modules used for cold-start validation."
    dependsOn("verifyRepresentativeFabric", "verifyRepresentativeNeoForge")
}

fun Project.addSharedMinecraftTemplate(sourceTemplate: String, loaderTemplate: String) {
    val minecraftTemplateDir = rootProject.layout.projectDirectory.dir("minecraftCompat/$sourceTemplate").asFile
    val loaderTemplateDir = rootProject.layout.projectDirectory.dir(loaderTemplate).asFile
    extensions.configure<SourceSetContainer> {
        named("main") {
            java.srcDir(minecraftTemplateDir.resolve("src/main/java"))
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
        exclude(group = "com.github.oshi", module = "oshi-core")
        exclude(group = "net.java.dev.jna", module = "jna")
        exclude(group = "net.java.dev.jna", module = "jna-platform")
        exclude(group = "com.google.code.gson", module = "gson")
        exclude(group = "com.google.guava", module = "guava")
        exclude(group = "com.google.guava", module = "failureaccess")
        exclude(group = "org.jline")
        exclude(group = "org.slf4j")
    }

    dependencies {
        add("implementation", neoLinkMcCommonCoordinate)
        add(bundle.name, neoLinkMcCommonCoordinate)
        add(bundle.name, "top.ceroxe.api:neolinkapi-desktop:$neoLinkApiVersion") {
            isTransitive = false
            exclude(group = "org.slf4j", module = "slf4j-nop")
            exclude(group = "com.google.code.gson", module = "gson")
        }
    }

    tasks.named<Jar>("jar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        exclude(
            "module-info.class",
            "oshi/**",
            "com/sun/jna/**",
            "com/google/gson/**",
            "com/google/common/**",
            "com/google/errorprone/**",
            "com/google/j2objc/**",
            "org/checkerframework/**",
            "org/jline/**",
            "org/slf4j/**",
            "META-INF/versions/**",
            "META-INF/services/**",
            "META-INF/native-image/**",
            "META-INF/maven/**"
        )
        from({
            bundle.map { dependency ->
                if (dependency.isDirectory) dependency else zipTree(dependency)
            }
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
    if (spec.mappingsArtifact != null) {
        disableFabricIntermediateMappings()
    }

    val loaderEntryDir = rootProject.layout.projectDirectory.dir("fabric/${spec.sourceTemplate}").asFile
    val minecraftTemplateDir = rootProject.layout.projectDirectory.dir("minecraftCompat/${spec.sourceTemplate}").asFile
    extensions.configure<SourceSetContainer> {
        named("main") {
            java.srcDir(minecraftTemplateDir.resolve("src/main/java"))
            resources.srcDir(minecraftTemplateDir.resolve("src/main/resources"))
            if (!layout.projectDirectory.dir("src").asFile.exists()) {
                java.srcDir(loaderEntryDir.resolve("src/main/java"))
                resources.srcDir(loaderEntryDir.resolve("src/main/resources"))
            }
        }
    }

    val archivesBaseName = rootProject.property("archives_base_name").toString()
    base {
        archivesName.set("$archivesBaseName-Fabric-${spec.minecraftVersion}")
    }

    dependencies {
        add("minecraft", "com.mojang:minecraft:${spec.minecraftVersion}")
        add("mappings", spec.mappingsArtifact?.let { files(rootProject.file(it)) } ?: officialMojangMappingsDependency())
        add("modImplementation", "net.fabricmc:fabric-loader:${spec.loaderVersion}")
        if (spec.requiresFabricApi) {
            add("modImplementation", "net.fabricmc.fabric-api:fabric-api:${spec.fabricVersion}")
        }
        add("compileOnly", "org.jetbrains:annotations:$jetbrainsAnnotationsVersion")
        add("implementation", neoLinkMcCommonCoordinate)
        add("include", neoLinkMcCommonCoordinate)
        add("implementation", "top.ceroxe.api:neolinkapi-desktop:$neoLinkApiVersion") {
            exclude(group = "org.slf4j", module = "slf4j-nop")
            exclude(group = "com.google.code.gson", module = "gson")
        }
        add("include", "top.ceroxe.api:neolinkapi-desktop:$neoLinkApiVersion") {
            isTransitive = false
        }
    }

    val resourceProperties = mapOf(
        "version" to version.toString(),
        "minecraft_version" to spec.minecraftVersion,
        "loader_version" to spec.loaderVersion,
        "loader_version_range" to spec.loaderVersionRange,
        "fabric_version" to spec.fabricVersion,
        "java_version" to spec.javaVersion.toString(),
        "mixin_compatibility_level" to mixinCompatibilityLevel(spec.javaVersion)
    )

    tasks.named<ProcessResources>("processResources") {
        inputs.properties(resourceProperties)
        filesMatching(listOf("fabric.mod.json", "neolinkmc.mixins.json")) {
            expand(resourceProperties)
        }
    }

    tasks.named<Jar>("jar") {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_$archivesBaseName" }
        }
    }

    tasks.named("assemble") {
        dependsOn("remapJar")
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
    findProject(spec.projectPath)?.run {
        if (shouldConfigureLoaderModule(path, ":fabric")) {
            configureFabricModule(spec)
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
        "minecraft_version_range" to exactMinecraftVersionRange(spec.minecraftVersion),
        "loader_version_range" to "[1,)",
        "neoforge_version_range" to spec.neoForgeVersionRange,
        "mixin_compatibility_level" to mixinCompatibilityLevel(spec.javaVersion)
    )

    tasks.named<ProcessResources>("processResources") {
        inputs.properties(resourceProperties)
        filesMatching(listOf("META-INF/neoforge.mods.toml", "neolinkmc.mixins.json")) {
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
    findProject(spec.projectPath)?.run {
        if (shouldConfigureLoaderModule(path, ":neoforge")) {
            configureNeoForgeModule(spec)
        }
    }
}
