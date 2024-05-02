/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.utils

import dev.inmo.tgbotapi.types.ChatId
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.RawChatId
import dev.inmo.tgbotapi.types.chat.User
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.*

val User.fullname: String
    get() = "$firstName $lastName"

val CommonMessage<*>.chatId: IdChatIdentifier get() = this.chat.id

val CommonMessage<TextContent>.text: String get() = this.content.text

val CommonMessage<*>.isCommand: Boolean
    get() {
        val textContent: String = when (content) {
            is PhotoContent -> (content as PhotoContent).text
            is AudioContent -> (content as AudioContent).text
            is DocumentContent -> (content as DocumentContent).text
            is VideoContent -> (content as VideoContent).text
            is AnimationContent -> (content as AnimationContent).text
            is VoiceContent -> (content as VoiceContent).text
            is TextContent -> (content as TextContent).text
            else -> null
        } ?: return false

        val splittedString = textContent.split(" ")
        return splittedString.size == 1 && splittedString.first().startsWith("/")
    }

val Long.chatId: ChatId get() = ChatId(RawChatId(this))
