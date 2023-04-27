/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.repositories

import dev.inmo.tgbotapi.types.IdChatIdentifier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import ru.kramlex.tgbot.bot.database.tables.AllInfoDao
import ru.kramlex.tgbot.core.other.ValueType
import ru.kramlex.tgbot.core.other.toLocalDateOrNull

class InfoRepository(
    private val infoDao: AllInfoDao,
) {

    suspend fun saveOrUpdate(
        userId: IdChatIdentifier,
        infoType: String,
        key: String,
        valueType: ValueType,
        value: String?
    ) {

        val jsonElement: JsonElement = when (valueType) {
            ValueType.INT -> value?.let { JsonPrimitive(it.toIntOrNull()) } ?: return
            ValueType.DOUBLE -> value?.let { JsonPrimitive(it.toDoubleOrNull()) } ?: return
            ValueType.DATE -> value?.let { JsonPrimitive(it.toLocalDateOrNull().toString()) } ?: return
            ValueType.STRING -> JsonPrimitive(value)
            ValueType.BOOL -> value?.let { JsonPrimitive(it.toBooleanStrictOrNull()) } ?: return
        }

        infoDao.updateUserInfo(
            userId = userId,
            infoType = infoType,
            key = key,
            value = jsonElement
        )
    }

    suspend fun getInfoFromType(userId: IdChatIdentifier, type: String): JsonObject? =
        infoDao.getInfoFromType(userId, type)

    fun dropData() {
        infoDao.dropData()
    }
}
