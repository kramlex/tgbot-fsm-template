/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.dsl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import ru.kramlex.tgbot.core.data.BotData
import java.io.File

@OptIn(ExperimentalSerializationApi::class)
fun main() {
    val json = Json { prettyPrint = true }
    val botData = generateBotData()
    val outputDir = File("dsl/output")
    val outputFile = File(outputDir, "data.json")
    println(outputFile.absoluteFile)
    val stream = outputFile.outputStream()
    json.encodeToStream(BotData.serializer(), botData, stream)
}
