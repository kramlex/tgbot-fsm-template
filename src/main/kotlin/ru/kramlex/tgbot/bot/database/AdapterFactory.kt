/*
 * Copyright (c) 2022-2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.database

import kotlinx.serialization.json.Json
import ru.kramlex.tgbot.bot.database.adapters.ChatIdAdapter
import ru.kramlex.tgbot.bot.database.adapters.JsonObjectAdapter
import ru.kramlex.db.generated.*

internal class AdapterFactory(
    private val json: Json
) {
    val userAdapter: UserRow.Adapter
        get() = UserRow.Adapter(
            idAdapter = ChatIdAdapter.sharedAdapter
        )

    val allInfoRowAdapter: AllInfoRow.Adapter
        get() = AllInfoRow.Adapter(
            userIdAdapter = ChatIdAdapter.sharedAdapter,
            value_Adapter = JsonObjectAdapter(json)
        )
}
