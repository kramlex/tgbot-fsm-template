/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.states

import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.actions.Action
import ru.kramlex.tgbot.core.other.ValidationData

@Serializable
data class EnterData(
    val validation: ValidationData? = null,
    val enterAction: Action,
    var afterActions: List<Action> = emptyList()
)
