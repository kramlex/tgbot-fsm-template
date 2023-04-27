/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.data

import kotlinx.serialization.Serializable

@Serializable
data class Command(
    val command: String,
    val description: String
)
