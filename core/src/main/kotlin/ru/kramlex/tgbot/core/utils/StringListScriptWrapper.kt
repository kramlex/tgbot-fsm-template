/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.utils

import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import kotlinx.serialization.json.JsonObject

fun interface StringListScriptWrapper {
    fun calculate(jsonObject: JsonObject): List<TextSourcesList>
}
