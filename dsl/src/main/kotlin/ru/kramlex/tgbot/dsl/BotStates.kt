/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.dsl

import ru.kramlex.tgbot.core.data.BotData
import ru.kramlex.tgbot.core.other.ValueType
import ru.kramlex.tgbot.dsl.data.BotDslMarker
import ru.kramlex.tgbot.dsl.data.DslAction
import ru.kramlex.tgbot.dsl.data.MenuElementsBuilder
import ru.kramlex.tgbot.dsl.data.botData

@BotDslMarker
private fun MenuElementsBuilder.backToMain(backStateName: String) =
    with(this) {
        "Вернуться в главное меню".addElementWithActions("Common.backToMain") {
            backStateName.route()
        }
    }

@BotDslMarker
private fun MenuElementsBuilder.back(backStateName: String) =
    with(this) {
        "Назад".addElementWithActions("Common.back") {
            backStateName.route()
        }
    }

fun generateBotData(): BotData = botData(
    startState = "HelloMessage",
    defaultState = "MainMenu"
) {

    addCommands {
        "start".description("he command will return you to the main menu")
    }

    addStates {

        "HelloMessage".actionsState {
            actions {
                "HelloMessage.message".sendMessage {
                    delayAfter = 1000
                }
                "Main Menu".route()
            }
        }

        "MainMenu".menuState {
            afterCommand = "start"
            menuData(
                messageKey = "MainMenu.enterText",
                errorMessageKey = "MainMenu.errorText"
            ) {
                addElements {
                    "Say joke".addElementWithActions("MainMenu.sayJoke") {
                        "SayJoke".route()
                    }

                    "Calculate weeks between date of birth and current date".addElementWithActions("MainMenu.weekBetween") {
                        "WeekBetweenBirthdayAndTodayEnter".route()
                    }

                    "Send Lorem file".addElementWithActions("MainMenu.sendLorem") {
                        "LoremFile".route()
                    }
                }
            }
        }
    }
}

