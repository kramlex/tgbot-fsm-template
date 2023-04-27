/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.manager

import dev.inmo.tgbotapi.types.IdChatIdentifier
import dev.inmo.tgbotapi.types.message.textsources.TextSourcesList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import ru.kramlex.tgbot.bot.data.ApplicationData
import ru.kramlex.tgbot.bot.kts.KtsObjectLoader
import ru.kramlex.tgbot.bot.model.RepositoryWrapper
import ru.kramlex.tgbot.bot.utils.StringListScriptWrapper
import ru.kramlex.tgbot.core.actions.CustomActions
import ru.kramlex.tgbot.core.data.BotData
import ru.kramlex.tgbot.core.data.Command
import ru.kramlex.tgbot.core.json.jsonForParsing
import ru.kramlex.tgbot.core.other.ValueType
import ru.kramlex.tgbot.core.states.ActionsState
import ru.kramlex.tgbot.core.states.EnterState
import ru.kramlex.tgbot.core.states.MenuState
import ru.kramlex.tgbot.core.states.State
import ru.kramlex.tgbot.core.states.StateInfo
import ru.kramlex.tgbot.core.states.StateType
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class BotManager(
    private val repositoryWrapper: RepositoryWrapper,
) : ru.kramlex.tgbot.core.BotDataProvider {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _states: MutableStateFlow<Set<State>> =
        MutableStateFlow(emptySet())
    private val _commands: MutableStateFlow<List<Command>> =
        MutableStateFlow(emptyList())

    private var _startState: State? = null
    private var _defaultState: State? = null

    private val ktsRunner: KtsObjectLoader by lazy { KtsObjectLoader() }

    val states: StateFlow<Set<State>> = _states.asStateFlow()
    val commands: StateFlow<List<Command>> = _commands.asStateFlow()
    val startState: State?
        get() = _startState
    val defaultState: State?
        get() = _defaultState

    init {
        parseData()
        scope.launch {
            while (true) {
                delay(updateDataDuration)
                parseData()
            }
        }
    }

    fun getStateInfo(stateName: String): State? =
        states.value.find { it.name == stateName }

    fun getStateInfo(userId: IdChatIdentifier): State? =
        repositoryWrapper.userRepository.getUser(userId).let { user ->
            if (user != null) {
                getStateInfo(stateName = user.state) ?: defaultState ?: startState
            } else {
                null
            }
        }

    private fun parseData() {
        val text = statesFile.readText()
        try {
            val dataElement = parseJson.parseToJsonElement(text)
            val botData = parseJson.decodeFromJsonElement(
                deserializer = BotData.serializer(),
                element = dataElement
            )

            val newStates = botData.states.map { jsonElement ->
                val stateInfo = parseJson.decodeFromJsonElement(
                    deserializer = StateInfo.serializer(),
                    element = jsonElement
                )

                when (stateInfo.type) {
                    StateType.ACTIONS -> {
                        parseJson.decodeFromJsonElement(
                            deserializer = ActionsState.serializer(),
                            element = jsonElement
                        )
                    }

                    StateType.ENTER -> {
                        parseJson.decodeFromJsonElement(
                            deserializer = EnterState.serializer(),
                            element = jsonElement
                        )
                    }

                    StateType.MENU -> parseJson.decodeFromJsonElement(
                        deserializer = MenuState.serializer(),
                        element = jsonElement
                    )
                }
            }.toSet()

            val newStartState = newStates
                .find { it.name == botData.startState }
                ?: newStates.firstOrNull()

            val newDefaultState = newStates
                .find { it.name == botData.defaultState }
                ?: newStates.firstOrNull()

            _startState = newStartState
            _defaultState = newDefaultState

            _states.update { newStates }
            _commands.update { botData.commands }
        } catch (error: Throwable) {
            println(error)
        }
    }

    // BOT DATA PROVIDER

    override fun getStringValue(key: String): String = ApplicationData.getValue(key)

    override fun getStringValueOrNull(key: String): String? = ApplicationData.getNullableValue(key)

    override fun getLocalizedFileName(documentKey: String): String = ApplicationData.getLocalizedFileName(documentKey)

    override fun getRussianFileNameOrNull(documentKey: String): String? =
        ApplicationData.getLocalizedFileName(documentKey).ifEmpty { null }

    override fun getFileNameOrNull(documentKey: String): String? =
        ApplicationData.getLocalizedFileName(documentKey).ifEmpty { null }

    override suspend fun saveOrUpdate(
        chatId: IdChatIdentifier,
        infoType: String,
        key: String,
        valueType: ValueType,
        value: String?,
    ) = repositoryWrapper.infoRepository.saveOrUpdate(
        userId = chatId,
        infoType = infoType,
        key = key,
        valueType = valueType,
        value = value
    )


    override suspend fun route(chatId: IdChatIdentifier, nextState: String) =
        repositoryWrapper.userRepository.saveData(chatId) {
            updateUserState(it, nextState)
        }

    override suspend fun getCalculatedStrings(
        chatId: IdChatIdentifier,
        infoType: String,
        scriptName: String,
    ): List<TextSourcesList> {
        val jsonInfo = repositoryWrapper.infoRepository.getInfoFromType(chatId, infoType)
            ?: throw IllegalStateException("info with type = $infoType not exist")

        val scriptPath: String = getScriptPath(scriptName)
        val scriptReader = withContext(Dispatchers.IO) {
            Files.newBufferedReader(Paths.get(scriptPath))
        }
        val scriptWrapper: StringListScriptWrapper = ktsRunner.load(scriptReader)
        return scriptWrapper.calculation(jsonInfo)
    }

    private companion object {
        val parseJson: Json = jsonForParsing
        const val updateDataDuration = 60000L
        val statesFile = File("src/states", "data.json")

        fun getScriptPath(scriptName: String): String =
            "src/main/kotlin/ru/kramlex/tgbot/bot/scripts/$scriptName.kts"
    }
}
