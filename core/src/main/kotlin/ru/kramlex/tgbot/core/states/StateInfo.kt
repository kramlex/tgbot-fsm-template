/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.states

import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.states.State
import ru.kramlex.tgbot.core.states.StateType

@Serializable
data class StateInfo(
    override val name: String,
    override val type: StateType,
    override val afterCommand: String? = null
) : State
