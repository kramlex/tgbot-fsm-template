/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.7.10"
    kotlin("plugin.serialization") version "1.7.10"
    id("app.cash.sqldelight") version "2.0.0-alpha02"
}

application {
    mainClass.set("ru.kramlex.tgbot.bot.ApplicationKt")
}

group = "ru.kramlex"
version = "1.0.0"

dependencies {
    implementation(kotlin("script-runtime"))
    implementation(kotlin("script-util"))
    implementation(kotlin("compiler-embeddable"))
    implementation(kotlin("scripting-compiler-embeddable"))
    implementation(kotlin("script-util"))

    implementation(libs.slf4j)
    implementation(libs.coroutines)
    implementation(libs.tgBotApi)
    implementation(libs.sqlDelightCoroutines)
    implementation(libs.sqlDelightDriver)
    implementation(libs.kotlinxDatetime)

    implementation(projects.core)
    implementation(libs.ktorClientCore)
    implementation(libs.ktorClientCio)
    implementation(libs.kotlinCsv)

    testImplementation(libs.kotlinCsv)
    testImplementation(libs.kotlinJunit)
}

sqldelight {
    database("BotDatabase") {
        packageName = "ru.kramlex.db.generated"
        sourceFolders = listOf("sqldelight")
        version = 1
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

tasks["build"].dependsOn("clean")

tasks.register("stage").configure {
    dependsOn(listOf("build", "clean"))
    tasks["build"].mustRunAfter("clean")
}