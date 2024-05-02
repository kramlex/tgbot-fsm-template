/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.actions

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import ru.kramlex.tgbot.core.BotDataProvider
import ru.kramlex.tgbot.core.other.ValueType
import ru.kramlex.tgbot.core.states.CallbackButton
import ru.kramlex.tgbot.core.states.MenuElement

@Serializable
enum class ActionType {
    @SerialName("sendWarningMessage")
    SEND_WARNING,

    @SerialName("sendMessage")
    SEND_MESSAGE,

    @SerialName("sendDocument")
    SEND_DOCUMENT,

    @SerialName("route")
    ROUTE,

    @SerialName("custom")
    CUSTOM;
}

@Serializable(with = ActionSerializer::class)
sealed interface Action {
    val type: ActionType
    val delayAfter: Long?
}

interface EnableKeyboard {
    val removeKeyboard: Boolean
}

sealed interface ExecutableAction : Action {
    suspend fun execute(
        context: BehaviourContext,
        chatId: IdChatIdentifier,
        botDataProvider: BotDataProvider,
        keyboardElements: List<MenuElement>? = null,
        removeKeyBoard: Boolean = true,
    )
}

sealed interface ExecutableCustomAction : Action {
    suspend fun execute(
        context: BehaviourContext,
        chatId: IdChatIdentifier,
        message: CommonMessage<TextContent>?,
        botDataProvider: BotDataProvider,
        keyboardElements: List<MenuElement>? = null,
        removeKeyBoard: Boolean = true,
    )
}

@Serializable
data class ActionSurrogate(
    val type: ActionType,
    val delayAfter: Long? = null,
    val nextState: String? = null,
    val messageKey: String? = null,
    val documentKey: String? = null,

    val name: CustomActions? = null,

    val infoType: String? = null,
    val key: String? = null,
    val value: String? = null,
    val valueType: ValueType? = null,
    val removeKeyboard: Boolean = true,
    val callbacks: List<CallbackButton> = emptyList(),

    val scriptName: String? = null,
) {

    fun toOriginal(): Action {
        try {
            return when {
                type == ActionType.ROUTE && !nextState.isNullOrBlank() -> RouteAction(
                    type = type,
                    delayAfter = delayAfter,
                    nextState = nextState
                )

                type == ActionType.SEND_MESSAGE && !messageKey.isNullOrBlank() -> MessageAction(
                    type = type,
                    delayAfter = delayAfter,
                    messageKey = messageKey,
                    callbacks = callbacks
                )

                type == ActionType.SEND_WARNING && !messageKey.isNullOrBlank() -> WarningMessageAction(
                    type = type,
                    delayAfter = delayAfter,
                    messageKey = messageKey
                )

                type == ActionType.CUSTOM &&
                        name == ru.kramlex.tgbot.core.actions.CustomActions.SAVE_OR_UPDATE &&
                        infoType != null && key != null && valueType != null
                -> SaveOrUpdateAction(
                    type = type,
                    delayAfter = delayAfter,
                    name = name,
                    key = key,
                    value = value,
                    valueType = valueType,
                    infoType = infoType
                )

                type == ActionType.CUSTOM &&
                        name == ru.kramlex.tgbot.core.actions.CustomActions.SEND_CALCULATED_TEXT &&
                        scriptName != null && infoType != null
                -> SendCalculatedMessage(
                    type = type,
                    delayAfter = delayAfter,
                    name = name,
                    scriptName = scriptName,
                    removeKeyboard = removeKeyboard,
                    infoType = infoType
                )

                type == ActionType.SEND_DOCUMENT &&
                        documentKey != null
                -> SendDocumentAction(
                    type = type,
                    delayAfter = delayAfter,
                    messageKey = messageKey,
                    documentKey = documentKey
                )

                else -> throw IllegalStateException("ActionSurrogate create impossible")
            }
        } catch (error: Exception) {
            throw IllegalStateException("Error")
        }
    }

    companion object {
        fun from(value: Action) = when (value) {
            is MessageAction ->
                ActionSurrogate(
                    type = value.type,
                    delayAfter = value.delayAfter,
                    messageKey = value.messageKey,
                    callbacks = value.callbacks
                )

            is RouteAction ->
                ActionSurrogate(
                    type = value.type,
                    delayAfter = value.delayAfter,
                    nextState = value.nextState
                )

            is WarningMessageAction ->
                ActionSurrogate(
                    type = value.type,
                    delayAfter = value.delayAfter,
                    messageKey = value.messageKey
                )

            is SaveOrUpdateAction -> ActionSurrogate(
                type = value.type,
                delayAfter = value.delayAfter,
                name = value.name,
                key = value.key,
                value = value.value,
                valueType = value.valueType,
                infoType = value.infoType
            )

            is SendCalculatedMessage -> ActionSurrogate(
                type = value.type,
                delayAfter = value.delayAfter,
                name = value.name,
                removeKeyboard = value.removeKeyboard
            )

            else -> error("invalid")
        }
    }
}

class ActionSerializer : KSerializer<Action> {
    private val strategy = ActionSurrogate.serializer()

    override val descriptor: SerialDescriptor
        get() = strategy.descriptor

    override fun serialize(encoder: Encoder, value: Action) =
        encoder.encodeSerializableValue(strategy, ActionSurrogate.from(value))

    override fun deserialize(decoder: Decoder): Action =
        decoder.decodeSerializableValue(strategy).toOriginal()
}
