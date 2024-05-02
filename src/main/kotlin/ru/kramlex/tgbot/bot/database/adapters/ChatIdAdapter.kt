/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.database.adapters

import app.cash.sqldelight.ColumnAdapter
import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.RawChatId

class ChatIdAdapter : ColumnAdapter<IdChatIdentifier, Long> {
    override fun decode(databaseValue: Long): ChatId =
        ChatId(chatId = RawChatId(databaseValue))

    override fun encode(value: IdChatIdentifier): Long =
        value.chatId.long

    companion object {
        val sharedAdapter by lazy { ChatIdAdapter() }
    }
}
