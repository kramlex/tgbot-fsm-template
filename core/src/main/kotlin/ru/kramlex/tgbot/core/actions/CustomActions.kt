/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.actions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class CustomActions(val value: String) {
    @SerialName("saveOrUpdate")
    SAVE_OR_UPDATE("saveOrUpdate"),
    @SerialName("sendCalculatedText")
    SEND_CALCULATED_TEXT("sendCalculatedText");

    companion object {
        fun from(value: String): CustomActions? {
            return CustomActions.values().firstOrNull { it.value == value }
        }
    }
}
