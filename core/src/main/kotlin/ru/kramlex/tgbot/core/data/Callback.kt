package ru.kramlex.tgbot.core.data

import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.actions.Action

@Serializable
data class CallbackData(
    val data: String,
    val removeMessage: Boolean = false,
    val actions: List<Action> = emptyList(),
)