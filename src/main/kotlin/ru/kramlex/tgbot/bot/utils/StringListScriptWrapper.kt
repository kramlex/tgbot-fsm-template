/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.utils

import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import kotlinx.serialization.json.JsonObject

data class StringListScriptWrapper(
    val calculation: (JsonObject) -> List<TextSourcesList>,
)
