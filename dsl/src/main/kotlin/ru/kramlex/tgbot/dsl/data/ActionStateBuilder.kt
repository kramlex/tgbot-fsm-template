/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.dsl.data

import ru.kramlex.tgbot.core.actions.Action
import ru.kramlex.tgbot.core.states.ActionsState
import ru.kramlex.tgbot.core.states.StateType

@BotDslMarker
class ActionStateBuilder(private val name: String) : StateBuilder {
    override val type: StateType = StateType.ACTIONS

    @BotDslMarker
    override var afterCommand: String? = null

    private var actions: MutableList<ru.kramlex.tgbot.core.actions.Action> = mutableListOf()

    @BotDslMarker
    var description: String? = null

    @BotDslMarker
    fun actions(lambda: ActionsBuilder.() -> Unit): Unit =
        actions.addAll(ActionsBuilder().apply(lambda).build()).let { }

    internal fun build(): ActionsState =
        ActionsState(
            name = name,
            type = type,
            actions = actions,
            afterCommand = afterCommand,
        )
}
