/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.states

import kotlinx.serialization.Serializable
import ru.kramlex.tgbot.core.states.MenuElement

@Serializable
data class MenuData(
    val messageKey: String,
    val errorMessageKey: String,
    var elements: List<MenuElement> = emptyList(),
)
