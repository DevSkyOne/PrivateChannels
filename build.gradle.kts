import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
}

group = "one.devsky"
version = "1.0-SNAPSHOT"
var moltenVersion = "1.0-PRE-13.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))

    // Kotlin Base Dependencies
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")

    // Discord API
    implementation("net.dv8tion:JDA:5.0.0-alpha.12")

    // Reflection Dependencies for automatic registration of commands and listeners
    implementation("net.oneandone.reflections8:reflections8:0.11.7")

    // Molten Kotlin Framework (https://github.com/TheFruxz/MoltenKT)
    implementation("com.github.TheFruxz.MoltenKT:moltenkt-core:$moltenVersion")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}