/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.states

import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.actions.Action

@Serializable
data class MenuElement(
    val description: String,
    val textKey: String,
    var actions: List<Action> = emptyList(),
)
