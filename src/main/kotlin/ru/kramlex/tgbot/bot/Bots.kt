/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot

import app.cash.sqldelight.db.SqlDriver
import dev.inmo.tgbotapi.bot.ktor.telegramBot
import dev.inmo.tgbotapi.extensions.api.bot.getMe
import dev.inmo.tgbotapi.extensions.api.bot.setMyCommands
import dev.inmo.tgbotapi.extensions.behaviour_builder.buildBehaviourWithLongPolling
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.message
import dev.inmo.tgbotapi.types.BotCommand
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.joinAll
import kotlinx.serialization.json.Json
import ru.kramlex.tgbot.bot.data.ApplicationData
import ru.kramlex.tgbot.bot.database.AdapterFactory
import ru.kramlex.tgbot.bot.database.BotDao
import ru.kramlex.tgbot.bot.database.JvmSqliteDriver
import ru.kramlex.tgbot.bot.handlers.handleCommand
import ru.kramlex.tgbot.bot.handlers.handleState
import ru.kramlex.tgbot.bot.handlers.handleTextWithoutCommands
import ru.kramlex.tgbot.bot.manager.BotManager
import ru.kramlex.tgbot.bot.model.RepositoryWrapper
import ru.kramlex.tgbot.bot.repositories.InfoRepository
import ru.kramlex.tgbot.bot.repositories.UserRepository
import ru.kramlex.db.generated.BotDatabase
import ru.kramlex.tgbot.bot.handlers.handleCallback

internal class Bots {

    private val bot = telegramBot(Constants.BOT_API_KEY)
    // Json
    private val json by lazy { Json { ignoreUnknownKeys = false } }

    // Database
    private val adapterFactory by lazy { AdapterFactory(json) }
    private val botDao by lazy {
        val driver: SqlDriver = JvmSqliteDriver(BotDatabase.Schema, Constants.DATABASE_NAME)
        val database = with(adapterFactory) { BotDatabase(driver, allInfoRowAdapter, userAdapter) }
        BotDao(database)
    }

    // Repositories & Manager

    private val userRepository by lazy { UserRepository(botDao.userDao) }
    private val infoRepository by lazy { InfoRepository(botDao.infoDao) }
    private val repositoryWrapper by lazy { RepositoryWrapper(userRepository, infoRepository) }

    private val botManager by lazy { BotManager(repositoryWrapper) }

    //start

    suspend fun start() {
        ApplicationData.start()
        startBot()
    }

    private suspend fun startBot() {

        val firstBot = bot.buildBehaviourWithLongPolling {

            println(getMe())

            userRepository.changes.receiveAsFlow()
                .onEach { handleState(botManager, it) }
                .launchIn(scope)

            handleCommand(
                repositoryWrapper = repositoryWrapper,
                botManager = botManager
            )

            handleTextWithoutCommands(botManager)

            botManager.botCommands
                .onEach { setMyCommands(it) }
                .launchIn(scope)

            callbackQueriesFlow
                .onEach { handleCallback(it, botManager, repositoryWrapper) }
                .launchIn(scope)
        }

        listOf(firstBot /* other bots*/).joinAll()
    }
}
