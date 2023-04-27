/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
    }
}

rootProject.name = "tgbot-fsm-template"

include(":core")
include(":dsl")