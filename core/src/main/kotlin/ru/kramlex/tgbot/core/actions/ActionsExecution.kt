/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.actions

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.createSubContext
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import kotlinx.coroutines.delay
import ru.kramlex.tgbot.core.BotDataProvider
import ru.kramlex.tgbot.core.states.MenuElement

suspend fun BehaviourContext.startActionsQueueWithMessage(
    botDataProvider: BotDataProvider,
    message: CommonMessage<TextContent>,
    chatId: ChatId,
    actions: List<Action>,
    keyboard: List<MenuElement>? = null
) {
    val newContext = createSubContext()
    actions.let {
        if (it.isNotEmpty()) with(newContext) {
            executeActionsWithMessage(
                botDataProvider = botDataProvider,
                chatId = chatId,
                actions = it,
                keyboard = keyboard,
                message = message
            )
        }
    }
}

suspend fun BehaviourContext.startActionsQueue(
    botDataProvider: BotDataProvider,
    chatId: IdChatIdentifier,
    actions: List<Action>,
    keyboard: List<MenuElement>? = null
) {
    val newContenxt = createSubContext()
    actions.let {
        if (it.isNotEmpty()) with(newContenxt) {
            executeActions(
                botDataProvider = botDataProvider,
                chatId = chatId,
                actions = it, keyboard
                = keyboard
            )
        }
    }
}

suspend fun BehaviourContext.executeActionWithMessage(
    botDataProvider: BotDataProvider,
    message: CommonMessage<TextContent>,
    chatId: IdChatIdentifier,
    action: Action,
    keyboard: List<MenuElement>? = null
) {
    when (action) {
        is ExecutableAction -> action.execute(context = this, chatId = chatId, keyboardElements = keyboard)
        is ExecutableWithProviderAction -> action.execute(
            context = this,
            chatId = chatId,
            botDataProvider = botDataProvider,
            keyboardElements = keyboard
        )
        is ExecutableCustomAction -> action.execute(
            context = this,
            chatId = chatId,
            botDataProvider = botDataProvider,
            message = message,
            keyboardElements = null,
            removeKeyBoard = false
        )
        else -> { /* nothing */ }
    }.apply { action.delayAfter?.let { delay(it) } }
}

suspend fun BehaviourContext.executeAction(
    botDataProvider: BotDataProvider,
    chatId: IdChatIdentifier,
    action: Action,
    keyboard: List<MenuElement>? = null
) {
    when (action) {
        is ExecutableAction -> action.execute(context = this, chatId = chatId, keyboardElements = keyboard)
        is ExecutableWithProviderAction -> action.execute(
            context = this,
            chatId = chatId,
            botDataProvider = botDataProvider,
            keyboardElements = keyboard
        )
        is ExecutableCustomAction -> action.execute(
            context = this,
            chatId = chatId,
            botDataProvider = botDataProvider,
            message = null,
            keyboardElements = null,
            removeKeyBoard= false
        )
        else -> { /* nothing */ }
    }.apply { action.delayAfter?.let { delay(it) } }
}

private suspend fun BehaviourContext.executeActions(
    botDataProvider: BotDataProvider,
    chatId: IdChatIdentifier,
    actions: List<Action>,
    keyboard: List<MenuElement>? = null
) {

    val lastActionWithKeyboardIndex = actions.indexOfLast { (it as? EnableKeyboard) != null}
    actions.forEachIndexed { index, action ->
        val keyboardElements: List<MenuElement>? =
            if (lastActionWithKeyboardIndex == index) keyboard else null
        executeAction(
            botDataProvider = botDataProvider,
            chatId = chatId,
            action = action,
            keyboard = keyboardElements,
        )
    }
}

private suspend fun BehaviourContext.executeActionsWithMessage(
    botDataProvider: BotDataProvider,
    message: CommonMessage<TextContent>,
    chatId: ChatId,
    actions: List<Action>,
    keyboard: List<MenuElement>? = null
) {

    val lastActionWithKeyboardIndex = actions.indexOfLast { (it as? EnableKeyboard) != null}
    actions.forEachIndexed { index, action ->
        val keyboardElements: List<MenuElement>? =
            if (lastActionWithKeyboardIndex == index) keyboard else null
        executeActionWithMessage(
            botDataProvider = botDataProvider,
            chatId = chatId,
            action = action,
            keyboard = keyboardElements,
            message = message
        )
    }
}
