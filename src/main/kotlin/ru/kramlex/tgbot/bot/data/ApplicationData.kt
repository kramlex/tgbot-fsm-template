/*
 * Copyright (c) 2022-2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.data

import com.github.doyaaaaaken.kotlincsv.client.CsvFileReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.kotlin.org.jline.utils.Log
import java.io.File

object ApplicationData {

    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)

    private val uploader = DataUploader()
    private val file: File get() = DataUploader.file
    private val urlsFile: File get() = DataUploader.urlsFile

    private val keyValueList: MutableStateFlow<List<CsvKeyValue>> =
        MutableStateFlow(emptyList())
    private val keyValueUrlList: MutableStateFlow<List<CsvFileValue>> =
        MutableStateFlow(emptyList())

    private var isStartedValue: Boolean = false
    private val isStartedSharedFlow: MutableSharedFlow<Boolean> = MutableSharedFlow()

    init { updateDataFromFile() }

    fun start() {
        uploader.startUpdating()

        uploader.endLoading.onEach {
            if (isStartedValue.not()) {
                isStartedSharedFlow.emit(true)
            }
            updateDataFromFile()
        }.launchIn(coroutineScope)

        keyValueUrlList.onEach { fileValues ->
            println("[UPDATE DOCUMENTS START]")
            val files = fileValues.filter { !it.localizedFileName.contains(".kts") }
            val scripts = fileValues.filter { it.localizedFileName.contains(".kts") }
            files.forEach { fileValue ->
                val file = getFileByFileName(fileValue.localizedFileName)
                uploader.uploadFile(file, fileValue.fileUrl)
            }
            println(files)
            println(scripts)
            scripts.forEach { fileValue ->
                val file = getScriptFile(fileValue.localizedFileName)
                coroutineScope.launch { uploader.uploadFile(file = file, url = fileValue.fileUrl) }
            }
            println("[UPDATE DOCUMENTS END]")
        }.launchIn(coroutineScope)
    }

    fun getValue(key: String): String =
        keyValueList.value.firstOrNull { keyValue ->
            keyValue.key == key
        }?.value ?: ""

    fun getNullableValue(key: String): String? =
        keyValueList.value.firstOrNull { keyValue ->
            keyValue.key == key
        }?.value

    fun getUrl(key: String): String =
        keyValueUrlList.value.firstOrNull { keyValue ->
            keyValue.key == key
        }?.fileUrl ?: ""

    fun getLocalizedFileName(key: String): String =
        keyValueUrlList.value.firstOrNull { keyValue ->
            keyValue.key == key
        }?.localizedFileName ?: ""

    private fun getScriptFile(fileName: String): File =
        File("src/main/kotlin/ru/kramlex/tgbot/bot/scripts", fileName)

    private fun getFileByFileName(fileName: String): File =
        File("src/main/resources/files", fileName)
    private fun getFileByLocalizedFileName(localizedFileName: String): File =
        File("src/main/resources/files", localizedFileName)

    private fun updateDataFromFile() {
        if (file.exists()) {
            csvReader().open(file) {
                val listKeyValue: Sequence<CsvKeyValue> = readKeyValueSequence()
                keyValueList.update { listKeyValue.toList() }
            }
        }
        if (urlsFile.exists()) {
            csvReader().open(urlsFile) {
                val listKeyValue: Sequence<CsvFileValue> = readFileValueSequence()
                keyValueUrlList.update { listKeyValue.toList() }
            }
        }
    }

    private fun CsvFileReader.readKeyValueSequence(): Sequence<CsvKeyValue> {
        return readAllAsSequence().mapNotNull { row ->
            val filteredRow = row.filter { !it.startsWith("//") }
            if (filteredRow.size != 2) return@mapNotNull null
            CsvKeyValue(key = filteredRow.first(), value = filteredRow.last())
        }
    }

    private fun CsvFileReader.readFileValueSequence(): Sequence<CsvFileValue> {
        return readAllAsSequence().mapNotNull { row ->
            val filteredRow = row.filter { !it.startsWith("//") }
            if (filteredRow.size != 3) return@mapNotNull null
            CsvFileValue(
                key = filteredRow.first(),
                localizedFileName = filteredRow.elementAt(1),
                fileUrl = filteredRow.elementAt(2)
            )
        }
    }
}
