package ru.kramlex.tgbot.core.states

import kotlinx.serialization.Serializable

@Serializable
data class CallbackButton(
    val textKey: String,
    var data: String,
)