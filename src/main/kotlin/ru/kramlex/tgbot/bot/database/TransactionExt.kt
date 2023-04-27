/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.database

import app.cash.sqldelight.Transacter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun <T : Transacter, R> T.withTransaction(action: T.() -> R): R {
    return withContext(Dispatchers.Default) {
        transactionWithResult { action() }
    }
}
