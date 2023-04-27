/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.states

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.actions.Action
import ru.kramlex.tgbot.core.states.EnterData
import ru.kramlex.tgbot.core.states.MenuElement
import ru.kramlex.tgbot.core.states.StateType
import ru.kramlex.tgbot.core.states.StateWithAction

@Serializable
data class EnterState(
    override val name: String,
    override val type: StateType,
    override val actions: List<Action> = emptyList(),

    @SerialName("enter")
    val enterData: EnterData,
    val keyboard: List<MenuElement>? = null,
    override val afterCommand: String? = null
) : StateWithAction
