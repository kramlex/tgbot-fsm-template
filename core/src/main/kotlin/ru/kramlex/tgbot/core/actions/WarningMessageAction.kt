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
import dev.inmo.tgbotapi.utils.buildEntities
import dev.inmo.tgbotapi.utils.row
import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.BotDataProvider
import ru.kramlex.tgbot.core.states.MenuElement

@Serializable
data class WarningMessageAction(
    override val type: ActionType = ActionType.SEND_WARNING,
    override val delayAfter: Long? = null,
    override val removeKeyboard: Boolean = false,
    val messageKey: String
) : ExecutableAction, EnableKeyboard {

    @Suppress("DEPRECATION")
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
                entities = buildEntities {
                    + botDataProvider.getStringValue("Common.warningPrefix")
                    + " "
                    + botDataProvider.getStringValue(messageKey)
                },
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
            println("${error.message} in ${WarningMessageAction::class.simpleName} execute, messageKey = $messageKey")
        }
    }
}
