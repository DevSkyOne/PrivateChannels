import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    application
}

group = "one.devsky"
version = "1.0-SNAPSHOT"

val jdaVersion: String by project
val ktorVersion: String by project
val exposedVersion: String by project

repositories {
    maven("https://nexus.flawcra.cc/repository/maven-mirrors/")
}

val implementDependencies = listOf(
    // Logging
    "ch.qos.logback:logback-classic:1.5.3",

    "net.dv8tion:JDA:$jdaVersion",

    "dev.fruxz:ascend:2024.1.1",
    "io.github.cdimascio:dotenv-kotlin:6.4.1",
    "com.google.code.gson:gson:2.10.1",

    // Jackson (JSON)
    "com.fasterxml.jackson.core:jackson-core:2.15.3",
    "com.fasterxml.jackson.core:jackson-databind:2.15.3",
    "com.fasterxml.jackson.core:jackson-annotations:2.15.3",
    "com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3",

    // Utils
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0",
    "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2",
    "net.oneandone.reflections8:reflections8:0.11.7",

    // Database
    "org.jetbrains.exposed:exposed-core:$exposedVersion",
    "org.jetbrains.exposed:exposed-dao:$exposedVersion",
    "org.jetbrains.exposed:exposed-jdbc:$exposedVersion",
    "org.jetbrains.exposed:exposed-java-time:$exposedVersion",
    "com.mysql:mysql-connector-j:8.3.0",
    "org.mariadb.jdbc:mariadb-java-client:3.3.3",
    "com.zaxxer:HikariCP:5.1.0",
)

dependencies {
    testImplementation(kotlin("test"))

    implementDependencies.forEach { dependency ->
        implementation(dependency)
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        freeCompilerArgs = listOf("-Xopt-in=kotlin.RequiresOptIn")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

application {
    mainClass = "one.devsky.StartKt"
}