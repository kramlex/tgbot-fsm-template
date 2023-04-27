/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.json

import kotlinx.serialization.json.Json

val jsonForParsing: Json by lazy {
    Json { ignoreUnknownKeys = true }
}
