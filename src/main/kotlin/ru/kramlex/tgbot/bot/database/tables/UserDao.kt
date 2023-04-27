/*
 * Copyright (c) 2022-2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.database.tables

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.kramlex.db.generated.UserRow
import ru.kramlex.db.generated.UserRowQueries
import kotlin.coroutines.coroutineContext

class UserDao(
    private val userRowQueries: UserRowQueries,
    private val parentScope: CoroutineScope
) {

    fun transaction(block: UserDao.() -> Unit) {
        userRowQueries.transaction {
            block()
        }
    }

    fun upsertUser(
        id: ChatId,
        fullName: String,
        state: String
    ) = userRowQueries.upsert(
        id = id,
        fullName = fullName,
        state = state
    )

    fun updateUserState(
        id: IdChatIdentifier,
        newState: String
    ) = userRowQueries.updateUserState(
        state = newState,
        id = id
    )

    fun getAllUsersFlow(): Flow<List<UserRow>> =
        userRowQueries.getAllUsers()
            .asFlow()
            .mapToList(context = parentScope.coroutineContext)

    fun getAllUsers(): List<UserRow> =
        userRowQueries.getAllUsers()
            .executeAsList()

    fun getUser(id: IdChatIdentifier): UserRow? =
        userRowQueries.getUserById(id = id)
            .executeAsOneOrNull()

    fun dropTable() =
        userRowQueries.dropTable()
}
