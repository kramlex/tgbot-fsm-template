/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.dsl.data

import ru.kramlex.tgbot.core.states.StateType

sealed interface StateBuilder {
    val type: StateType
    var afterCommand: String?
}
