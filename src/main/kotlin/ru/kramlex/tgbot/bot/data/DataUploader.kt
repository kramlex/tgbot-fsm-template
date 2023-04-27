/*
 * Copyright (c) 2022-2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import org.jetbrains.kotlin.org.jline.utils.Log
import ru.kramlex.tgbot.bot.Constants
import java.io.File
import java.util.logging.Logger
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class DataUploader {

    private val httpClient = HttpClient(CIO)
    private val coroutineScope = CoroutineScope(Dispatchers.Unconfined)
    private val _endLoading = Channel<Unit>(Channel.RENDEZVOUS)
    private var lastUpdate: Instant? = null

    val endLoading: Flow<Unit> = _endLoading.receiveAsFlow()

    fun startUpdating() {
        coroutineScope.launch {
            while (true) {
                if (lastUpdate == null) {
                    update()
                    lastUpdate = currentTime()
                }
                val lastUpdate = lastUpdate ?: continue
                if (lastUpdate.plus(Constants.updateDuration) < currentTime()) {
                    update()
                    this@DataUploader.lastUpdate = currentTime()
                }
            }
        }
    }

    suspend fun uploadFile(file: File, url: String, additionalFile: File? = null) {
        try {
            val response = httpClient.get(url)
            val bytes = response.readBytes()
            if (file.exists()) file.delete()
            withContext(Dispatchers.IO) {
                file.createNewFile()
                file.writeBytes(bytes)
            }
            if (additionalFile != null) {
                if (additionalFile.exists()) additionalFile.delete()
                withContext(Dispatchers.IO) {
                    additionalFile.createNewFile()
                    additionalFile.writeBytes(bytes)
                }
            }
        } catch (error: Throwable) {
            println("error with download file from url = $url")
        }
    }

    private suspend fun update() {
        update(FileType.Strings)
        update(FileType.Urls)
        _endLoading.trySend(Unit)
    }

    private suspend fun update(fileType: FileType) {
        try {
            println(fileType.startUpdateString)
            val response = httpClient.get(fileType.path)
            val bytes = response.readBytes()
            if (fileType.file.exists()) fileType.file.delete()
            withContext(Dispatchers.IO) {
                fileType.file.createNewFile()
                fileType.file.writeBytes(bytes)
            }
            println(fileType.endUpdateString)
        } catch (error:Throwable) {
            println("error on update file with fileType = $fileType")
        }
    }

    private enum class FileType {
        Strings, Urls;

        val path: String get() = when (this) {
            Strings -> Constants.uploadStringsPath
            Urls -> Constants.uploadUrlsPath
        }

        val file: File get() = when (this) {
            Strings -> DataUploader.file
            Urls -> urlsFile
        }

        val startUpdateString: String
            get() = when (this) {
                Strings -> "[START UPDATING STRINGS]"
                Urls -> "[START UPDATING URLS]"
            }

        val endUpdateString: String
            get() = when (this) {
                Strings -> "[END UPDATING STRINGS]"
                Urls -> "[END UPDATING URLS]"
            }
    }

    companion object {
        fun resourceFile(fileNameWithExt: String): File = File("src/main/resources/files", fileNameWithExt)

        val file get() = File(SAVE_DIRECTORY, FILENAME)

        val urlsFile get() = File(SAVE_DIRECTORY, URL_FILENAME)

        private const val FILENAME = "contents.csv"
        private const val URL_FILENAME = "urls.csv"
        private const val SAVE_DIRECTORY = "uploaded"
        fun currentTime(): Instant =
            Instant.fromEpochMilliseconds(System.currentTimeMillis())
    }
}
