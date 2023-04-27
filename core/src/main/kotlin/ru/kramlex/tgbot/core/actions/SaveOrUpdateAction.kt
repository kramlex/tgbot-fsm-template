/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.actions

import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.text
import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.abstracts.CommonMessage
import dev.inmo.tgbotapi.types.message.content.TextContent
import dev.inmo.tgbotapi.utils.RiskFeature
import ru.kramlex.tgbot.core.other.ValueType
import ru.kramlex.tgbot.core.states.MenuElement
import ru.kramlex.tgbot.core.BotDataProvider

data class SaveOrUpdateAction(
    override val type: ActionType = ActionType.CUSTOM,
    override val delayAfter: Long? = null,
    val name: CustomActions,

    val infoType: String,
    val key: String,
    val valueType: ValueType,
    val value: String? = null,
) : ExecutableCustomAction {
    @OptIn(RiskFeature::class)
    override suspend fun execute(
        context: BehaviourContext,
        chatId: IdChatIdentifier,
        message: CommonMessage<TextContent>?,
        botDataProvider: BotDataProvider,
        keyboardElements: List<MenuElement>?,
        removeKeyBoard: Boolean
    ): Unit = botDataProvider.saveOrUpdate(
        chatId = chatId,
        infoType = infoType,
        key = key,
        value = message?.text ?: value,
        valueType = valueType
    )
}