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
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.slf4j.LoggerFactory
import ru.kramlex.tgbot.core.BotDataProvider
import ru.kramlex.tgbot.core.states.MenuElement

@Serializable
data class SendCalculatedMessage(
    override val type: ActionType = ActionType.CUSTOM,
    override val delayAfter: Long? = null,
    val name: CustomActions,

    val infoType: String,
    val scriptName: String,
    val removeKeyboard: Boolean = false,
) : ExecutableCustomAction {
    override suspend fun execute(
        context: BehaviourContext,
        chatId: IdChatIdentifier,
        message: CommonMessage<TextContent>?,
        botDataProvider: BotDataProvider,
        keyboardElements: List<MenuElement>?,
        removeKeyBoard: Boolean
    ) {
        try {
            val stringsArrays = botDataProvider.getCalculatedStrings(
                chatId = chatId,
                infoType = infoType,
                scriptName = scriptName
            )
            with(context) {
                try {
                    stringsArrays.forEach { strings ->
                        sendMessage(
                            chatId = chatId,
                            entities = strings,
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
                        delay(400L)
                    }
                } catch (error: Throwable) {

                    println("${error.message} in ${MessageAction::execute.name}")
                }
            }
        } catch (error: Throwable) {
            LoggerFactory.getLogger(SendCalculatedMessage::class.java)
                .info("${error.message} in ${SendCalculatedMessage::execute.name}")
        }
    }
}