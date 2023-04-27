/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.other

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDate
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ValueType {
    @SerialName("int") INT,
    @SerialName("double") DOUBLE,
    @SerialName("date") DATE,
    @SerialName("string") STRING,
    @SerialName("bool") BOOL
}

fun String.toLocalDateOrNull(): LocalDate? {
    val firstString = this.trimIndent()
    val secondString = this.trimIndent()
        .replace(
            oldChar = '.',
            newChar = '-'
        )
    val thirdString = this.trimIndent()
        .replace(
            oldChar = '/',
            newChar = '-'
        )

    val firstDate: LocalDate? = firstString.toLocalDateAfterFormatting()
    val secondDate: LocalDate? = secondString.toLocalDateAfterFormatting()
    val thirdDate: LocalDate? = thirdString.toLocalDateAfterFormatting()

    return firstDate ?: secondDate ?: thirdDate
}

private fun String.toLocalDateAfterFormatting(): LocalDate? = try {
    this.split("-")
        .reversed()
        .joinToString("-")
        .toLocalDate()
} catch (e: Throwable) {
    null
}
