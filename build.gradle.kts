/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

plugins {
    application
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("app.cash.sqldelight") version "2.0.2"
}

application {
    mainClass.set("ru.kramlex.tgbot.bot.ApplicationKt")
}

group = "ru.kramlex"
version = "1.0.0"

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(kotlin("script-runtime", version = "1.8.22"))
    implementation(kotlin("script-util", version = "1.8.22"))
    implementation(kotlin("compiler-embeddable", version = "1.8.22"))
    implementation(kotlin("scripting-compiler-embeddable", version = "1.8.22"))
    implementation(kotlin("script-util", version = "1.8.22"))

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
    databases {
        create("BotDatabase") {
            packageName.set("ru.kramlex.db.generated")
            srcDirs("src/main/sqldelight")
            version = 1
        }
    }
}

tasks["build"].dependsOn("clean")

tasks.register("stage").configure {
    dependsOn(listOf("build", "clean"))
    tasks["build"].mustRunAfter("clean")
}