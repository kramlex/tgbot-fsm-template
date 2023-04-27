/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.actions

import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.ReplyKeyboardRowBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.buttons.KeyboardButton
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.utils.row
import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.BotDataProvider
import ru.kramlex.tgbot.core.states.MenuElement

@Serializable
data class MessageAction(
    override val type: ActionType = ActionType.SEND_MESSAGE,
    override val delayAfter: Long? = null,
    override val removeKeyboard: Boolean = false,
    val messageKey: String,
) : ExecutableWithProviderAction, EnableKeyboard {

    override suspend fun execute(
        context: BehaviourContext,
        chatId: IdChatIdentifier,
        botDataProvider: BotDataProvider,
        keyboardElements: List<MenuElement>?,
        removeKeyBoard: Boolean,
    ): Unit = with(context) {
        try {
            sendMessage(
                chatId = chatId,
                text = botDataProvider.getStringValue(messageKey),
                replyMarkup = keyboardElements?.let { list ->
                    replyKeyboard {
                        list.forEach {
                            row<KeyboardButton>(fun ReplyKeyboardRowBuilder.() {
                                simpleButton(botDataProvider.getStringValue(it.textKey))
                            })
                        }
                    }
                } ?: if (removeKeyBoard) ReplyKeyboardRemove() else null
            )
        } catch (error: Throwable) {
            println("${error.message} in ${MessageAction::class.simpleName}, messageKey = $messageKey")
        }
    }
}
