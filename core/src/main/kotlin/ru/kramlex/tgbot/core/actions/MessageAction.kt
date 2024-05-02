/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.actions

import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.ReplyKeyboardRowBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.inlineKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.buttons.InlineKeyboardButtons.CallbackDataInlineKeyboardButton
import dev.inmo.tgbotapi.types.buttons.KeyboardButton
import dev.inmo.tgbotapi.types.buttons.KeyboardMarkup
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.utils.row
import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.BotDataProvider
import ru.kramlex.tgbot.core.states.CallbackButton
import ru.kramlex.tgbot.core.states.MenuElement

@Serializable
data class MessageAction(
    override val type: ActionType = ActionType.SEND_MESSAGE,
    override val delayAfter: Long? = null,
    override val removeKeyboard: Boolean = false,
    val messageKey: String,
    val callbacks: List<CallbackButton> = emptyList()
) : ExecutableAction, EnableKeyboard {

    override suspend fun execute(
        context: BehaviourContext,
        chatId: IdChatIdentifier,
        botDataProvider: BotDataProvider,
        keyboardElements: List<MenuElement>?,
        removeKeyBoard: Boolean,
    ): Unit = context.runCatching {
        sendMessage(
            chatId = chatId,
            text = botDataProvider.getStringValue(messageKey),
            replyMarkup = createKeyboard(
                provider = botDataProvider,
                callbacks = callbacks,
                keyboardElements = keyboardElements,
                removeKeyBoard = removeKeyboard
            )
        )
    }.onFailure { error ->
        println("${error.message} in ${MessageAction::class.simpleName}, messageKey = $messageKey")
    }.let { }

    private fun createKeyboard(
        provider: BotDataProvider,
        callbacks: List<CallbackButton>,
        keyboardElements: List<MenuElement>?,
        removeKeyBoard: Boolean,
    ): KeyboardMarkup? = callbacks.takeIf { it.isNotEmpty() }?.let { callbacksNotNull ->
        inlineKeyboard {
            callbacksNotNull.map { button ->
                CallbackDataInlineKeyboardButton(
                    text = provider.getStringValue(button.textKey),
                    callbackData = button.data
                )
            }.also { add(it) }
        }
    } ?: keyboardElements?.let { list ->
        replyKeyboard {
            list.forEach {
                row<KeyboardButton>(fun ReplyKeyboardRowBuilder.() {
                    simpleButton(provider.getStringValue(it.textKey))
                })
            }
        }
    } ?: if (removeKeyBoard) ReplyKeyboardRemove() else null
}
