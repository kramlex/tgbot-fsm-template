/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.database.tables

import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.UserId
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import ru.kramlex.db.generated.AllInfoRow
import ru.kramlex.db.generated.BotDatabase
import ru.kramlex.db.generated.InfoRowQueries
import ru.kramlex.tgbot.bot.database.withTransaction

class AllInfoDao(
    private val database: BotDatabase,
    private val allInfoRowQueries: InfoRowQueries
) {

    suspend fun getUserInfo(type: String): AllInfoRow? =
        allInfoRowQueries.withTransaction {
            getAllInfoFromType(type = type).executeAsOneOrNull()
        }


    suspend fun getUserInfo(userId: UserId): AllInfoRow? =
        allInfoRowQueries.withTransaction {
            getAllInfoFromUserId(userId).executeAsOneOrNull()
        }

    suspend fun updateUserInfo(
        userId: IdChatIdentifier,
        infoType: String,
        key: String,
        value: JsonElement
    ) = allInfoRowQueries.withTransaction {
        val oldInfo = getAllInfo(
            type = infoType,
            userId = userId
        ).executeAsOneOrNull()


        if (oldInfo != null) {
            val jsonObject = JsonObject(oldInfo.value_.toMutableMap().apply {
                this[key] = value
            })
            updateState(
                id = oldInfo.id,
                newValue = jsonObject
            )
        } else {
            val jsonObject = JsonObject(mapOf(key to value))
            insert(
                userId = userId,
                type = infoType,
                value = jsonObject
            )
        }
    }

    suspend fun getInfoFromType(userId: IdChatIdentifier, type: String): JsonObject? =
        allInfoRowQueries.withTransaction {
            getAllInfo(userId = userId, type = type)
                .executeAsOneOrNull()
                ?.value_
        }

    fun dropData() {
        allInfoRowQueries.dropTable()
    }
}
