/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import ru.kramlex.tgbot.bot.database.tables.AllInfoDao
import ru.kramlex.tgbot.bot.database.tables.UserDao
import ru.kramlex.db.generated.BotDatabase

internal class BotDao(
    private val botDatabase: BotDatabase
) {
    private val coroutineScope: CoroutineScope =
        CoroutineScope(Dispatchers.IO)

    val userDao: UserDao by lazy {
        UserDao(
            userRowQueries = botDatabase.userRowQueries,
            parentScope = coroutineScope
        )
    }

    val infoDao: AllInfoDao by lazy {
        AllInfoDao(
            allInfoRowQueries = botDatabase.infoRowQueries,
            database = botDatabase
        )
    }
}
