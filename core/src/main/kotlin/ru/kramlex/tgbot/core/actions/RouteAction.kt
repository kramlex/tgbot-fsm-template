/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.actions

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.IdChatIdentifier
import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.BotDataProvider
import ru.kramlex.tgbot.core.states.MenuElement

@Serializable
data class RouteAction(
    override val type: ActionType = ActionType.ROUTE,
    override val delayAfter: Long?,
    val nextState: String,
) : ExecutableWithProviderAction {
    override suspend fun execute(
        context: BehaviourContext,
        chatId: IdChatIdentifier,
        botDataProvider: BotDataProvider,
        keyboardElements: List<MenuElement>?,
        removeKeyBoard: Boolean
    ): Unit = try {
        botDataProvider.route(chatId = chatId, nextState = nextState)
    } catch (error: Throwable) {
        println("${error.message} in ${RouteAction::class.simpleName}, nextState = $nextState")
    }
}
