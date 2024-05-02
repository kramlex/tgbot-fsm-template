/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.actions

import dev.inmo.micro_utils.common.MPPFile
import dev.inmo.tgbotapi.extensions.api.send.media.sendDocument
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.types.buttons.ReplyKeyboardRowBuilder
import dev.inmo.tgbotapi.extensions.utils.types.buttons.replyKeyboard
import dev.inmo.tgbotapi.extensions.utils.types.buttons.simpleButton
import dev.inmo.tgbotapi.requests.abstracts.InputFile
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.buttons.KeyboardButton
import dev.inmo.tgbotapi.types.buttons.ReplyKeyboardRemove
import dev.inmo.tgbotapi.utils.row
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.states.MenuElement
import java.io.File
import java.net.URLDecoder

@Serializable
data class SendDocumentAction(
    override val type: ActionType = ActionType.SEND_DOCUMENT,
    override val delayAfter: Long?,
    override val removeKeyboard: Boolean = false,
    val messageKey: String?,
    val documentKey: String
) : ExecutableAction, EnableKeyboard {
    override suspend fun execute(
        context: BehaviourContext,
        chatId: IdChatIdentifier,
        botDataProvider: ru.kramlex.tgbot.core.BotDataProvider,
        keyboardElements: List<MenuElement>?,
        removeKeyBoard: Boolean,
    ): Unit = with(context) {
        val inputFile: InputFile?

        val fileName = botDataProvider.getLocalizedFileName(documentKey)
        val message = messageKey?.let { botDataProvider.getStringValueOrNull(it) }
        val path = getFilePath(fileName)
        try {
            val url = this.javaClass.classLoader.getResource(path)?.path
            val newUrl = withContext(Dispatchers.IO) {
                URLDecoder.decode(url, "utf-8")
            }
            val file: MPPFile = File(newUrl)

            inputFile = InputFile.fromFile(file)

            sendDocument(
                chatId = chatId,
                document = inputFile,
                text = message,
                replyMarkup = keyboardElements?.let { list ->
                    replyKeyboard {
                        list.forEach {
                            row<KeyboardButton>(fun ReplyKeyboardRowBuilder.() {
                                simpleButton(botDataProvider.getStringValue(it.textKey))
                            })
                        }
                    }
                } ?: if (removeKeyBoard) ReplyKeyboardRemove() else null
            )
        } catch (error: Throwable) {
            println("${error.message} in ${SendDocumentAction::class.simpleName}, messageKey = $messageKey")
        }
    }

    companion object {
        fun getFilePath(fileName: String) =
            "files/$fileName"
    }
}

private val File.extension: String
    get() = name.substringAfterLast('.', "")
