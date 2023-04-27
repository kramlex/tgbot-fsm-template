/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.states

import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.actions.Action

@Serializable
data class ActionsState(
    override val name: String,
    override val type: StateType,
    override val actions: List<ru.kramlex.tgbot.core.actions.Action> = emptyList(),
    override val afterCommand: String? = null
) : StateWithAction
