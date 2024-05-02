/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.handlers

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.behaviour_builder.triggers_handling.onCommand
import dev.inmo.tgbotapi.extensions.utils.asFromUser
import dev.inmo.tgbotapi.utils.PreviewFeature
import org.slf4j.LoggerFactory
import ru.kramlex.tgbot.bot.manager.BotManager
import ru.kramlex.tgbot.bot.model.RepositoryWrapper
import ru.kramlex.tgbot.core.utils.chatId

private const val START_COMMAND = "start"
private const val DROP_COMMAND = "drop"

@OptIn(PreviewFeature::class)
suspend fun BehaviourContext.handleCommand(
    botManager: BotManager,
    repositoryWrapper: RepositoryWrapper
) {
    with(repositoryWrapper) {

        onCommand(START_COMMAND) { message ->
            LoggerFactory.getLogger(BehaviourContext::class.java)
                .info("\"[START] message: $message\"")

            val chatId = message.chatId
            val existUser = userRepository.getUser(chatId)

            if (existUser == null) {
                val user = message.asFromUser()?.user
                    ?: throw IllegalStateException("failed to get user information")

                val startState = botManager.startState
                if (startState != null) {
                    userRepository.saveUser(user, startState = startState.name)
                }
            } else {
                val afterStartState = botManager.states.value.firstOrNull {
                    it.afterCommand == START_COMMAND
                }
                if (afterStartState != null) {
                    userRepository.saveData(chatId) {
                        updateUserState(it, afterStartState.name)
                    }
                } else {
                    val defaultState = botManager.defaultState
                    if (defaultState != null) {
                        userRepository.saveData(chatId) {
                            updateUserState(it, defaultState.name)
                        }
                    }
                }
            }
        }

        val commandsWithoutStart = botManager.commands.value
            .filter { it.command != START_COMMAND }

        commandsWithoutStart.forEach { command ->
            onCommand(command.command) { message ->
                val chatId = message.chatId
                val afterCommandState = botManager.states.value.firstOrNull {
                    it.afterCommand == command.command
                }

                if (afterCommandState != null) {
                    userRepository.saveData(chatId) {
                        updateUserState(it, afterCommandState.name)
                    }
                }
            }
        }

        onCommand(DROP_COMMAND) { message ->
            println("[DROP] message: $message")
            dropData()
        }
    }
}
