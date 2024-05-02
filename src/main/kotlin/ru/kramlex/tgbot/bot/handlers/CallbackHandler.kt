package ru.kramlex.tgbot.bot.handlers

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.asFromUser
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.data
import dev.inmo.tgbotapi.types.update.CallbackQueryUpdate
import dev.inmo.tgbotapi.utils.RiskFeature
import ru.kramlex.tgbot.bot.manager.BotManager
import ru.kramlex.tgbot.bot.model.RepositoryWrapper
import ru.kramlex.tgbot.core.actions.startActionsQueue

@OptIn(RiskFeature::class)
suspend fun BehaviourContext.handleCallback(
    callback: CallbackQueryUpdate,
    botManager: BotManager,
    repositoryWrapper: RepositoryWrapper
) {
    callback.data.data?.let {
        botManager.getCallback(it)
    }?.let { callbackData ->
        val user = repositoryWrapper.userRepository.getUser(callback.data.from.id)
        callbackData to user
    }?.also { (data, user) ->
        if (user != null) {
            startActionsQueue(
                botDataProvider = botManager,
                chatId = user.id,
                actions = data.actions,
                keyboard = null
            )
        }
    }
}