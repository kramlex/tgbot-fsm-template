/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.states

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import ru.kramlex.tgbot.core.actions.Action
import ru.kramlex.tgbot.core.json.jsonForParsing

@Serializable
enum class StateType {
    @SerialName("actions") ACTIONS,
    @SerialName("enter")  ENTER,
    @SerialName("menu")  MENU
}

sealed interface State {
    val name: String
    val type: StateType
    val afterCommand: String?
}

val State.jsonObject: JsonObject get() =
    when(this) {
        is MenuState -> jsonForParsing.encodeToJsonElement(
            serializer = MenuState.serializer(),
            value = this
        )
        is StateInfo -> jsonForParsing.encodeToJsonElement(
            serializer = StateInfo.serializer(),
            value = this
        )
        is ActionsState -> jsonForParsing.encodeToJsonElement(
            serializer = ActionsState.serializer(),
            value = this
        )
        is EnterState -> jsonForParsing.encodeToJsonElement(
            serializer = EnterState.serializer(),
            value = this
        )
        else -> error("")
    }.jsonObject

sealed interface StateWithAction : State {
    val actions: List<Action>
}