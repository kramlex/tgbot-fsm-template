/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.database.adapters

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

class JsonObjectAdapter(private val json: Json) : ColumnAdapter<JsonObject, String> {
    override fun decode(databaseValue: String): JsonObject =
        json.decodeFromString(databaseValue)

    override fun encode(value: JsonObject): String =
        json.encodeToString(value)
}
