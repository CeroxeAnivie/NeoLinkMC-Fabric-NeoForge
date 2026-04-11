plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
    id("maven-publish")
    java
    kotlin("jvm") version "2.1.0"
}

// 获取属性文件中的版本信息
val minecraft_version: String by project
val yarn_mappings: String by project
val loader_version: String by project
val fabric_version: String by project
val mod_version: String by project
val maven_group: String by project
val archives_base_name: String by project

// 构建完整的版本号格式: 0.0.3+fabric.1.21-1.21.8
val fullVersion = "${mod_version}+fabric.1.21-1.21.8"

version = fullVersion
group = maven_group

base {
    // 设置 JAR 基础名称，最终格式: NeoLinkMC-5.11.2+fabric.1.21.jar
    archivesName.set(archives_base_name)
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/") {
        name = "Fabric"
    }
    maven("https://jitpack.io") {
        name = "JitPack"
    }
}

dependencies {
    // Minecraft 和 Fabric 核心依赖
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:$loader_version")

    // Fabric API
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    // 原有项目依赖 - 使用include打包到mod中
    implementation("fun.ceroxe.api:ceroxe-core:0.2.7")
    include("fun.ceroxe.api:ceroxe-core:0.2.7")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
    }
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", minecraft_version)
    inputs.property("loader_version", loader_version)
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            mapOf(
                "version" to project.version,
                "minecraft_version" to minecraft_version,
                "loader_version" to loader_version,
                "fabric_version" to fabric_version
            )
        )
    }
}

tasks.withType<Jar> {
    from("LICENSE") {
        rename { "${it}_${archives_base_name}" }
    }
}

// 配置 Maven 发布
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = archives_base_name
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/NeoProxy/NeoLinkMC")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}