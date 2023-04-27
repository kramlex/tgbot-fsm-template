/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    application
}

dependencies {
    implementation(projects.core)
    implementation(libs.slf4j)
}

application {
    mainClass.set("MainKt")
}
