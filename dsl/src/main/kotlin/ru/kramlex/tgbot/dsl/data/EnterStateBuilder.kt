/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.dsl.data

import ru.kramlex.tgbot.core.actions.Action
import ru.kramlex.tgbot.core.other.IsDoubleValidation
import ru.kramlex.tgbot.core.other.IsIntValidation
import ru.kramlex.tgbot.core.other.RegexValidation
import ru.kramlex.tgbot.core.other.Validation
import ru.kramlex.tgbot.core.other.ValidationData
import ru.kramlex.tgbot.core.states.EnterData
import ru.kramlex.tgbot.core.states.EnterState
import ru.kramlex.tgbot.core.states.MenuElement
import ru.kramlex.tgbot.core.states.StateType

class EnterStateBuilder(
    private val name: String,
) : StateBuilder {
    override val type: StateType = StateType.ENTER

    private var actions: MutableList<ru.kramlex.tgbot.core.actions.Action> = mutableListOf()
    private var keyboard: MutableList<MenuElement>? = null
    private var enterData: EnterData? = null

    @BotDslMarker
    override var afterCommand: String? = null

    @BotDslMarker
    fun enterData(lambda: EnterDataBuilder.() -> Unit): Unit =
        EnterDataBuilder().apply(lambda).build().let { enterData = it }

    @BotDslMarker
    fun addActions(lambda: ActionsBuilder.() -> Unit): Unit =
        ActionsBuilder().apply(lambda).build().let {
            actions.addAll(it)
        }

    @BotDslMarker
    fun keyboard(lambda: MenuElementsBuilder.() -> Unit): Unit =
        MenuElementsBuilder().apply(lambda).build().let { elements ->
            keyboard = keyboard?.addAll(elements)?.let { keyboard } ?: elements.toMutableList()
        }

    @BotDslMarker
    fun addElement(
        textKey: String,
        description: String,
        lambda: MenuElementBuilder.() -> Unit,
    ): Unit = MenuElementBuilder(textKey, description).apply(lambda).build().let { element ->
        keyboard = keyboard?.add(element)?.let { keyboard } ?: mutableListOf(element)
    }

    internal fun build(): EnterState =
        EnterState(name, type, actions.toList(), enterData ?: error("builder not contain enter data"))
}

class EnterDataBuilder {
    private val afterActions: MutableList<ru.kramlex.tgbot.core.actions.Action> = mutableListOf()
    private val enterAction: ru.kramlex.tgbot.core.actions.Action? get() = afterEnterAction?.build()

    private var validation: ValidationData? = null

    @BotDslMarker
    var afterEnterAction: DslAction? = null

    @BotDslMarker
    fun afterActions(lambda: ActionsBuilder.() -> Unit): Unit =
        afterActions.addAll(ActionsBuilder().apply(lambda).build()).let { }

    @BotDslMarker
    fun validationData(
        errorTextKey: String,
        lambda: ValidationDataBuilder.() -> Unit,
    ): Unit = ValidationDataBuilder(errorTextKey).apply(lambda).build().let { data ->
        validation = data
    }

    @BotDslMarker
    @JvmName("validationDataFromString")
    fun String.validationData(lambda: ValidationDataBuilder.() -> Unit) =
        validationData(this, lambda)

    internal fun build(): EnterData =
        EnterData(validation, enterAction ?: error("enterAction not contains in builder"), afterActions)
}

class ValidationDataBuilder(
    private val errorTextKey: String,
) {
    private val validations: MutableList<Validation> = mutableListOf()

    @BotDslMarker
    fun addValidations(lambda: ValidationsBuilder.() -> Unit): Unit =
        validations.apply {
            validations.addAll(ValidationsBuilder().apply(lambda).build())
        }.let { }

    internal fun build(): ValidationData =
        ValidationData(errorTextKey, validations)
}

class ValidationsBuilder {
    private val validations: MutableList<Validation> = mutableListOf()

    @BotDslMarker
    fun regex(regex: String): Unit = validations.apply { add(RegexValidation(regex = regex)) }.let { }

    @BotDslMarker
    fun isInt(): Unit = validations.apply { add(IsIntValidation()) }.let { }

    @BotDslMarker
    fun isDouble(): Unit = validations.apply { add(IsDoubleValidation()) }.let { }

    internal fun build(): List<Validation> = validations.toList()
}
