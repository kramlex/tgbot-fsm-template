/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.dsl.data

import kotlinx.serialization.json.JsonObject
import ru.kramlex.tgbot.core.data.BotData
import ru.kramlex.tgbot.core.data.CallbackData
import ru.kramlex.tgbot.core.data.Command
import ru.kramlex.tgbot.core.states.State
import ru.kramlex.tgbot.core.states.jsonObject

@DslMarker
annotation class BotDslMarker()

@BotDslMarker
fun botData(
    startState: String,
    defaultState: String,
    lambda: BotDataBuilder.() -> Unit
): BotData = BotDataBuilder(startState, defaultState).apply(lambda).build()

@BotDslMarker
class BotDataBuilder(
    private val startState: String,
    private val defaultState: String
) {
    private val callbacks: MutableList<CallbackData> = mutableListOf()
    private val commands: MutableList<Command> = mutableListOf()
    private val states: MutableList<State> = mutableListOf()
    private val serializedStates: List<JsonObject>
        get() = states.map { it.jsonObject }

    fun addCallbacks(lambda: CallbacksBuilder.() -> Unit): Unit =
        CallbacksBuilder().apply(lambda).build().let { callbacks.addAll(it) }

    @BotDslMarker
    fun addStates(lambda: StatesBuilder.() -> Unit): Unit =
        StatesBuilder().apply(lambda).build().let { states.addAll(it) }


    @BotDslMarker
    fun addCommands(lambda: CommandsBuilder.() -> Unit): Unit =
        CommandsBuilder().apply(lambda).build().let {
            commands.addAll(it)
        }

    internal fun build(): BotData =
        BotData(startState, defaultState, callbacks, serializedStates, commands)
}

class CommandsBuilder {
    private val commands: MutableList<Command> = mutableListOf()

    @BotDslMarker
    fun addCommand(command: String, description: String): Unit =
        Command(command, description).let { commands.add(it) }

    @BotDslMarker
    fun String.description(description: String) =
        addCommand(this, description)

    internal fun build(): List<Command> = commands.toList()
}

class CallbacksBuilder {
    private val callbacks: MutableList<CallbackData> = mutableListOf()

    @BotDslMarker
    fun callback(
        data: String,
        removeMessage: Boolean = false,
        lambda: ActionsBuilder.() -> Unit
    ): Unit = CallbackData(
        data = data,
        removeMessage = removeMessage,
        actions = ActionsBuilder().apply(lambda).build()
    ).let { callbacks.add(it) }

    @BotDslMarker
    operator fun String.invoke(
        removeMessage: Boolean = false,
        lambda: ActionsBuilder.() -> Unit
    ) = callback(this, removeMessage, lambda)

    internal fun build(): List<CallbackData> = callbacks.toList()
}

class StatesBuilder {
    private val states: MutableList<State> = mutableListOf()

    @BotDslMarker
    fun actionsState(
        name: String,
        lambda: ActionStateBuilder.() -> Unit
    ): Unit = ActionStateBuilder(name).apply(lambda).build().let { states.add(it) }

    @BotDslMarker
    fun enterState(
        name: String,
        lambda: EnterStateBuilder.() -> Unit
    ): Unit = EnterStateBuilder(name).apply(lambda).build().let { states.add(it) }

    @BotDslMarker
    fun menuState(
        name: String,
        lambda: MenuStateBuilder.() -> Unit
    ): Unit = MenuStateBuilder(name).apply(lambda).build().let { states.add(it) }


    @JvmName("actionsStateFromString")
    fun String.actionsState(lambda: ActionStateBuilder.() -> Unit) =
        actionsState(this, lambda)

    @JvmName("enterStateFromString")
    fun String.enterState(lambda: EnterStateBuilder.() -> Unit) =
        enterState(this, lambda)

    @JvmName("menuStateFromString")
    fun String.menuState(lambda: MenuStateBuilder.() -> Unit) =
        menuState(this, lambda)

    internal fun build(): List<State> = states.toList()
}
