/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.data

data class CsvKeyValue(
    val key: String,
    val value: String
)

data class CsvFileValue(
    val key: String,
    val localizedFileName: String,
    val fileUrl: String
)
