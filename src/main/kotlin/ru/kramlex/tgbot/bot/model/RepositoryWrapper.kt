/*
 * Copyright (c) 2022 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.bot.model

import ru.kramlex.tgbot.bot.repositories.InfoRepository
import ru.kramlex.tgbot.bot.repositories.UserRepository

data class RepositoryWrapper(
    val userRepository: UserRepository,
    val infoRepository: InfoRepository
) {
    fun dropData() {
        userRepository.dropData()
        infoRepository.dropData()
    }
}
