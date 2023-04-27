/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.handlers

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import ru.kramlex.tgbot.bot.manager.BotManager
import ru.kramlex.tgbot.core.actions.startActionsQueue
import ru.kramlex.tgbot.core.states.EnterState
import ru.kramlex.tgbot.core.states.MenuState
import ru.kramlex.tgbot.core.states.StateWithAction
import ru.kramlex.db.generated.UserRow

suspend fun BehaviourContext.handleState(
    botManager: BotManager,
    user: UserRow
) {
    val stateInfo = botManager.getStateInfo(user.state) ?: return

    when (stateInfo) {
        is EnterState -> startActionsQueue(
            botDataProvider = botManager,
            chatId = user.id,
            actions = stateInfo.actions,
            keyboard = stateInfo.keyboard
        )
        is StateWithAction -> startActionsQueue(
            botDataProvider = botManager,
            chatId = user.id,
            actions = stateInfo.actions
        )
        is MenuState -> {
            stateInfo.sendMenu(
                context = this,
                chatId = user.id,
                botDataProvider = botManager
            )
        }
        else -> { /* nothing */ }
    }
}
