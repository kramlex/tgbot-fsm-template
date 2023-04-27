/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

plugins {
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
}

dependencies {
    api(libs.kotlinSerialization)
    api(libs.tgBotApi)
    api(libs.kotlinxDatetime)
    implementation(libs.slf4j)
}
