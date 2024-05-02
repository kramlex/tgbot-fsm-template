import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    application
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(projects.core)
    implementation(libs.slf4j)
}

application {
    mainClass.set("MainKt")
}
