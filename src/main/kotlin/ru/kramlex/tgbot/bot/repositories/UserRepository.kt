/*
 * Copyright (c) 2022-2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.repositories

import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.chat.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import ru.kramlex.tgbot.bot.database.tables.UserDao
import ru.kramlex.tgbot.bot.utils.fullname
import ru.kramlex.db.generated.UserRow

class UserRepository(
    private val userDao: UserDao
) {
    private val coroutineScope =
        CoroutineScope(Dispatchers.Default)

    private val _stateUsers: MutableStateFlow<List<UserRow>> =
        MutableStateFlow(emptyList())

    val changes: Channel<UserRow> = Channel(CHANNEL_SIZE)

    fun getUser(chatId: IdChatIdentifier): UserRow? =
        userDao.getUser(chatId)

    init {
        _stateUsers.value = userDao.getAllUsers()
        coroutineScope.launch {
            userDao.getAllUsersFlow().collect { allUsers ->
                val oldUsers = _stateUsers.value

                val usersWithChanges = allUsers.filter { user ->
                    val existUser = oldUsers.firstOrNull { it.id == user.id }
                        ?: return@filter true
                    existUser.state != user.state
                }
                usersWithChanges.forEach {
                    changes.trySend(it)
                }
                _stateUsers.value = allUsers
            }
        }
    }

    fun saveUser(
        user: User,
        startState: String
    ) {
        userDao.upsertUser(
            id = user.id,
            fullName = user.fullname,
            state = startState
        )
    }

    fun dropData() = userDao.dropTable()

    suspend fun saveData(id: IdChatIdentifier, block: suspend UserDao.(IdChatIdentifier) -> Unit) =
        userDao.block(id)

    companion object {
        const val CHANNEL_SIZE = 128
    }
}
