/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.handlers

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onText
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import ru.kramlex.tgbot.bot.data.ApplicationData
import ru.kramlex.tgbot.bot.manager.BotManager
import ru.kramlex.tgbot.core.utils.chatId
import ru.kramlex.tgbot.core.utils.isCommand
import ru.kramlex.tgbot.core.utils.text
import ru.kramlex.tgbot.core.actions.MessageAction
import ru.kramlex.tgbot.core.actions.executeActionWithMessage
import ru.kramlex.tgbot.core.actions.startActionsQueue
import ru.kramlex.tgbot.core.states.EnterState
import ru.kramlex.tgbot.core.states.MenuState

suspend fun BehaviourContext.handleTextWithoutCommands(
    botManager: BotManager
) {
    onText { message ->
        if (message.isCommand) return@onText
        println("[TEXT] message: $message")
        processTextInput(
            botManager = botManager,
            message = message,
        )
    }
}

suspend fun BehaviourContext.processTextInput(
    botManager: BotManager,
    message: CommonMessage<TextContent>
) {

    val userId = message.chatId
    val userState = botManager.getStateInfo(userId = userId)

    val messageText = message.text

    when (userState) {
        is MenuState -> {
            val menuData = userState.menuData

            val element = menuData.elements.firstOrNull {
                ApplicationData.getNullableValue(it.textKey) == messageText
            }

            if (element != null) {
                startActionsQueue(
                    chatId = userId,
                    actions = element.actions,
                    botDataProvider = botManager
                )
            } else {
                MessageAction(messageKey = menuData.errorMessageKey)
                    .execute(
                        botDataProvider = botManager,
                        context = this,
                        chatId = userId
                    )
            }
        }
        is EnterState -> {
            val validation = userState.enterData.validation


            for (element in userState.keyboard ?: emptyList()) {
                if (ApplicationData.getValue(element.textKey) == messageText) {
                    startActionsQueue(
                        chatId = userId,
                        actions = element.actions,
                        botDataProvider = botManager
                    )
                    return
                }
            }

            if (validation != null) {
                if (!validation.validate(messageText)) {
                    MessageAction(messageKey = validation.errorTextKey)
                        .execute(
                            botDataProvider = botManager,
                            context = this,
                            chatId = userId
                        )
                    return
                }
            }

            executeActionWithMessage(
                chatId = userId,
                action = userState.enterData.enterAction,
                botDataProvider = botManager,
                message = message
            )

            startActionsQueue(
                chatId = userId,
                actions = userState.enterData.afterActions,
                botDataProvider = botManager
            )
        }

        else -> { /* nothing */ }
    }
}
