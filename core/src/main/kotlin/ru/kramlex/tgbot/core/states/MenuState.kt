/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.states

import dev.inmo.tgbotapi.extensions.api.send.sendMessage
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.ReplyKeyboardRowBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.buttons.KeyboardButton
import dev.inmo.tgbotapi.utils.row
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.BotDataProvider

@Suppress("MemberVisibilityCanBePrivate")
@Serializable
data class MenuState(
    override val name: String,
    override val type: StateType,
    @SerialName("menu") val menuData: MenuData,
    override val afterCommand: String? = null
): State {
    suspend fun sendMenu(
        botDataProvider: BotDataProvider,
        context: BehaviourContext,
        chatId: IdChatIdentifier
    ) {
        val lambda: suspend BehaviourContext.() -> Unit = {
            try {
                sendMessage(
                    chatId = chatId,
                    text = botDataProvider.getStringValue(menuData.messageKey),
                    replyMarkup = replyKeyboard {
                        menuData.elements.forEach {
                            row<KeyboardButton>(fun ReplyKeyboardRowBuilder.() {
                                simpleButton(botDataProvider.getStringValue(it.textKey))
                            })
                        }
                    }
                )
            } catch (error: Throwable) {
                println("${error.message} in ${::sendMenu.name}")
            }
        }
        lambda.invoke(context)
    }
}
