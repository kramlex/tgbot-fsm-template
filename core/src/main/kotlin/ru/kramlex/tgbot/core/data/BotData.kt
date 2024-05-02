/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import ru.kramlex.tgbot.core.actions.CustomActions

@Serializable
data class BotData(
    val startState: String,
    val defaultState: String,
    val callbacks: List<CallbackData>,
    val states: List<JsonElement>,
    val commands: List<Command>
)
