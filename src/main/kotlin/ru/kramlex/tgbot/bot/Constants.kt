/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object Constants {
    const val DATABASE_NAME = "database/bot_database.db"
    const val BOT_API_KEY = "<bot-api-key>"
    // example: https://docs.google.com/feeds/download/spreadsheets/Export?key=1UpBEgyN7rEkr_5OFqta2mPAFLs6K2Ae_8ltPaaFu6gk&exportFormat=csv
    const val UPLOAD_STRINGS_PATH: String = "<strings-csv-path>"
    // example: https://docs.google.com/feeds/download/spreadsheets/Export?key=1UpBEgyN7rEkr_5OFqta2mPAFLs6K2Ae_8ltPaaFu6gk&exportFormat=csv&gid=199428785
    const val UPLOAD_URLS_PATH: String = "<files-csv-path>"
    val updateDuration: Duration = 5.minutes
}
