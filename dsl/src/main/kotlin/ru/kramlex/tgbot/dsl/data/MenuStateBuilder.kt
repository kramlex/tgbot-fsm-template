/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.dsl.data

import ru.kramlex.tgbot.core.actions.Action
import ru.kramlex.tgbot.core.states.MenuData
import ru.kramlex.tgbot.core.states.MenuElement
import ru.kramlex.tgbot.core.states.MenuState
import ru.kramlex.tgbot.core.states.StateType

class MenuStateBuilder(
    private val name: String,
) : StateBuilder {
    override val type: StateType = StateType.MENU
    private var menuData: MenuData? = null

    @BotDslMarker
    override var afterCommand: String? = null

    @BotDslMarker
    fun menuData(
        messageKey: String,
        errorMessageKey: String,
        lambda: MenuDataBuilder.() -> Unit
    ): Unit = MenuDataBuilder(messageKey, errorMessageKey).apply(lambda)
        .build()
        .let { menuData = it }

    internal fun build(): MenuState =
        MenuState(name, type, menuData ?: error("builder not contain menuData"), afterCommand)
}

class MenuDataBuilder(
    private val messageKey: String,
    private val errorMessageKey: String
) {
    private val elements: MutableList<MenuElement> = mutableListOf()

    @BotDslMarker
    fun addElements(lambda: MenuElementsBuilder.() -> Unit): Unit =
        elements.apply { addAll(MenuElementsBuilder().apply(lambda).build()) }.let {  }

    @BotDslMarker
    fun addElement(
        textKey: String,
        description: String,
        lambda: MenuElementBuilder.() -> Unit
    ): Unit = elements.add(MenuElementBuilder(textKey, description).apply(lambda).build()).let {  }

    internal fun build(): MenuData = MenuData(messageKey, errorMessageKey, elements)
}

class MenuElementsBuilder {
    private val elements: MutableList<MenuElement> = mutableListOf()

    @BotDslMarker
    fun addElement(
        textKey: String,
        description: String,
        lambda: MenuElementBuilder.() -> Unit
    ): Unit = elements.add(MenuElementBuilder(textKey, description).apply(lambda).build()).let {  }

    @BotDslMarker
    fun addElementWithActions(
        textKey: String,
        description: String,
        lambda: ActionsBuilder.() -> Unit
    ): Unit = elements.add(MenuElement(description, textKey, ActionsBuilder().apply(lambda).build().toList())).let {  }

    @BotDslMarker
    @JvmName("addElementWithActionsFromStringWithDescriptionAndActionsBuilder")
    fun String.addElementWithActions(
        textKey: String,
        lambda: ActionsBuilder.() -> Unit
    ): Unit = addElementWithActions(textKey, this, lambda)

    @BotDslMarker
    @JvmName("addElementFromStringWithDescription")
    fun String.addElement(
        textKey: String,
        lambda: MenuElementBuilder.() -> Unit
    ): Unit = addElement(textKey, this, lambda)

    internal fun build(): List<MenuElement> = elements.toList()
}

class MenuElementBuilder(
    private val textKey: String,
    private var description: String
) {
    private val actions: MutableList<ru.kramlex.tgbot.core.actions.Action> = mutableListOf()

    fun addActions(lambda: ActionsBuilder.() -> Unit): Unit =
        actions.apply { ActionsBuilder().apply(lambda).build() }.let {  }

    internal fun build(): MenuElement = MenuElement(description, textKey, actions.toList())
}
