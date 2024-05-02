/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.dsl.data

import ru.kramlex.tgbot.core.actions.Action
import ru.kramlex.tgbot.core.actions.ActionType
import ru.kramlex.tgbot.core.actions.CustomActions
import ru.kramlex.tgbot.core.actions.MessageAction
import ru.kramlex.tgbot.core.actions.RouteAction
import ru.kramlex.tgbot.core.actions.SaveOrUpdateAction
import ru.kramlex.tgbot.core.actions.SendCalculatedMessage
import ru.kramlex.tgbot.core.actions.SendDocumentAction
import ru.kramlex.tgbot.core.actions.WarningMessageAction
import ru.kramlex.tgbot.core.other.ValueType
import ru.kramlex.tgbot.core.states.CallbackButton
import ru.kramlex.tgbot.core.utils.emptyLambda

@BotDslMarker
sealed interface DslAction {
    fun build(): Action

    @BotDslMarker
    data class SendMessage(
        private val messageKey: String,
        private val lambda: ActionsBuilder.MessageActionBuilder.() -> Unit = emptyLambda(),
    ) : DslAction {
        override fun build(): Action = ActionsBuilder.MessageActionBuilder(messageKey).apply(lambda).build()
    }

    @BotDslMarker
    data class WarningMessage(
        private val messageKey: String,
        private val lambda: ActionsBuilder.WarningMessageActionBuilder.() -> Unit  = emptyLambda(),
    ) : DslAction {
        override fun build(): Action = ActionsBuilder.WarningMessageActionBuilder(messageKey).apply(lambda).build()
    }

    data class SendDocument(
        private val documentKey: String,
        private val lambda: ActionsBuilder.SendDocumentActionBuilder.() -> Unit  = emptyLambda(),
    ) : DslAction {
        override fun build(): Action = ActionsBuilder.SendDocumentActionBuilder(documentKey).apply(lambda).build()
    }

    @BotDslMarker
    data class Route(
        private val nextState: String,
        private val lambda: ActionsBuilder.RouteActionBuilder.() -> Unit  = emptyLambda(),
    ) : DslAction {
        override fun build(): Action = ActionsBuilder.RouteActionBuilder(nextState).apply(lambda).build()
    }

    @BotDslMarker
    data class SaveOrUpdate @BotDslMarker constructor(
        private val infoType: String,
        private val key: String,
        private val valueType: ValueType,
        private val lambda: ActionsBuilder.SaveOrUpdateActionBuilder.() -> Unit  = emptyLambda(),
    ) : DslAction {
        override fun build(): Action = ActionsBuilder.SaveOrUpdateActionBuilder(infoType, key, valueType)
            .apply(lambda).build()
    }

    @BotDslMarker
    data class SendCalculatedMessage(
        private val infoType: String,
        private val scriptName: String,
        private val lambda: ActionsBuilder.SendCalculatedMessageActionBuilder.() -> Unit  = emptyLambda(),
    ) : DslAction {
        override fun build(): Action = ActionsBuilder.SendCalculatedMessageActionBuilder(infoType, scriptName)
            .apply(lambda).build()
    }
}

@BotDslMarker
fun String.warningMessageAction(lambda: ActionsBuilder.WarningMessageActionBuilder.() -> Unit = emptyLambda()) =
    DslAction.WarningMessage(this, lambda)

@BotDslMarker
fun String.sendDocumentAction(lambda: ActionsBuilder.SendDocumentActionBuilder.() -> Unit = emptyLambda()) =
    DslAction.SendDocument(this, lambda)

@BotDslMarker
fun String.sendMessageAction(lambda: ActionsBuilder.MessageActionBuilder.() -> Unit = emptyLambda()) =
    DslAction.SendMessage(this, lambda)

@BotDslMarker
fun String.routeAction(lambda: ActionsBuilder.RouteActionBuilder.() -> Unit = emptyLambda()) =
    DslAction.Route(this, lambda)

@BotDslMarker
class ActionsBuilder {
    private var actions: MutableList<Action> = mutableListOf()

    @BotDslMarker
    fun sendMessage(messageKey: String, lambda: MessageActionBuilder.() -> Unit = emptyLambda()) =
        actions.apply {
            add(DslAction.SendMessage(messageKey, lambda).build())
        }

    @BotDslMarker
    @JvmName("sendMessageFromString")
    fun String.sendMessage(lambda: MessageActionBuilder.() -> Unit = emptyLambda()) = sendMessage(this, lambda)

    @BotDslMarker
    fun warningMessage(messageKey: String, lambda: WarningMessageActionBuilder.() -> Unit = emptyLambda()) =
        actions.apply {
            add(DslAction.WarningMessage(messageKey, lambda).build())
        }

    @BotDslMarker
    @JvmName("warningMessageFromString")
    fun String.warningMessage(lambda: WarningMessageActionBuilder.() -> Unit = emptyLambda()) = warningMessage(this, lambda)

    fun sendDocument(documentKey: String, lambda: SendDocumentActionBuilder.() -> Unit = emptyLambda()) =
        actions.apply {
            add(DslAction.SendDocument(documentKey, lambda).build())
        }

    @BotDslMarker
    fun route(nextState: String, lambda: RouteActionBuilder.() -> Unit = emptyLambda()) = actions.apply {
        add(DslAction.Route(nextState, lambda).build())
    }

    @BotDslMarker
    @JvmName("routeFromString")
    fun String.route(lambda: RouteActionBuilder.() -> Unit = emptyLambda()) =
        route(this, lambda)

    @BotDslMarker
    fun saveOrUpdate(
        infoType: String,
        key: String,
        valueType: ValueType,
        lambda: SaveOrUpdateActionBuilder.() -> Unit = emptyLambda(),
    ) = actions.apply {
        add(SaveOrUpdateActionBuilder(infoType, key, valueType).apply(lambda).build())
    }

    @BotDslMarker
    fun sendCalculatedMessage(
        infoType: String,
        scriptName: String,
        lambda: SendCalculatedMessageActionBuilder.() -> Unit = emptyLambda(),
    ) = actions.apply {
        add(DslAction.SendCalculatedMessage(infoType, scriptName, lambda).build())
    }

    internal fun build(): List<Action> = actions.toList()

    private sealed interface ActionBuilder {
        val type: ActionType

        @BotDslMarker
        var delayAfter: Long?

        @BotDslMarker
        var removeKeyboard: Boolean

    }

    class MessageActionBuilder(
        private val messageKey: String,
    ) : ActionBuilder {

        private var callbackButtons: MutableList<CallbackButton> = mutableListOf()

        override val type: ActionType = ActionType.SEND_MESSAGE

        @BotDslMarker
        override var delayAfter: Long? = null

        @BotDslMarker
        override var removeKeyboard: Boolean = false

        fun addCallbackButton(key: String, data: String): Unit =
            CallbackButton(textKey = key, data = data).let { callbackButtons.add(it) }

        internal fun build(): MessageAction =
            MessageAction(type, delayAfter, removeKeyboard, messageKey, callbacks = callbackButtons)
    }

    class WarningMessageActionBuilder(
        private val messageKey: String,
    ) : ActionBuilder {
        override val type: ActionType = ActionType.SEND_WARNING

        @BotDslMarker
        override var delayAfter: Long? = null

        @BotDslMarker
        override var removeKeyboard: Boolean = false

        internal fun build(): WarningMessageAction =
            WarningMessageAction(type, delayAfter, removeKeyboard, messageKey)
    }

    class SendDocumentActionBuilder(
        private val documentKey: String,
    ) : ActionBuilder {
        override val type: ActionType = ActionType.SEND_DOCUMENT

        @BotDslMarker
        override var delayAfter: Long? = null

        @BotDslMarker
        override var removeKeyboard: Boolean = false

        @BotDslMarker
        var messageKey: String? = null

        internal fun build(): SendDocumentAction =
            SendDocumentAction(type, delayAfter, removeKeyboard, messageKey, documentKey)
    }

    class RouteActionBuilder(
        private val nextState: String,
    ) : ActionBuilder {
        override val type: ActionType = ActionType.ROUTE

        @BotDslMarker
        override var delayAfter: Long? = null

        @BotDslMarker
        override var removeKeyboard: Boolean = false

        internal fun build(): RouteAction =
            RouteAction(type, delayAfter, nextState)
    }
    class SaveOrUpdateActionBuilder(
        private val infoType: String,
        private val key: String,
        private val valueType: ValueType,
    ) : ActionBuilder {
        override val type: ActionType = ActionType.CUSTOM
        private val name: CustomActions = CustomActions.SAVE_OR_UPDATE

        @BotDslMarker
        override var delayAfter: Long? = null

        @BotDslMarker
        override var removeKeyboard: Boolean = false

        @BotDslMarker
        var value: String? = null

        internal fun build(): SaveOrUpdateAction =
            SaveOrUpdateAction(type, delayAfter, name, infoType, key, valueType, value)
    }

    class SendCalculatedMessageActionBuilder(
        private val infoType: String,
        private val scriptName: String,
    ) : ActionBuilder {
        override val type: ActionType = ActionType.CUSTOM
        private val name: CustomActions = CustomActions.SEND_CALCULATED_TEXT

        @BotDslMarker
        override var delayAfter: Long? = null

        @BotDslMarker
        override var removeKeyboard: Boolean = false

        internal fun build(): SendCalculatedMessage =
            SendCalculatedMessage(type, delayAfter, name, infoType, scriptName, removeKeyboard)
    }
}
