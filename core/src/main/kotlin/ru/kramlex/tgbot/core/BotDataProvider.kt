/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core

import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import ru.kramlex.tgbot.core.other.ValueType

interface BotDataProvider {
    fun getStringValue(key: String): String
    fun getStringValueOrNull(key: String): String?
    fun getLocalizedFileName(documentKey: String): String
    fun getRussianFileNameOrNull(documentKey: String): String?
    fun getFileNameOrNull(documentKey: String): String?

    suspend fun saveOrUpdate(
        chatId: IdChatIdentifier,
        infoType: String,
        key: String,
        valueType: ValueType,
        value: String?
    )

    suspend fun route(
        chatId: IdChatIdentifier,
        nextState: String
    )

    suspend fun getCalculatedStrings(
        chatId: IdChatIdentifier,
        infoType: String,
        scriptName: String
    ) : List<TextSourcesList>
}
