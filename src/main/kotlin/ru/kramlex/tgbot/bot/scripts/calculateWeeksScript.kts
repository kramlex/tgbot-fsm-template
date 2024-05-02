/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

/** you can edit [NEEDED KOTLIN DEPENDENCIES]*/
import kotlin.math.absoluteValue
import kotlin.math.pow
// ...

/** CUSTOM TUPES (cannot be edited) [PROJECT DEPENDENCIES] */
import kotlinx.datetime.*
import kotlinx.serialization.json.*
import dev.inmo.tgbotapi.extensions.utils.formatting.*
import dev.inmo.tgbotapi.types.message.textsources.*
import ru.kramlex.tgbot.core.utils.StringListScriptWrapper
import kotlin.jvm.Throws

/** EDIT ZONE */

internal data class IllegalDateException(override val message: String): IllegalStateException(message)

@Throws(IllegalDateException::class)
fun weekBetween(dateOfBirth: LocalDate): Int {
    val currentDate = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date
    if (dateOfBirth >= currentDate) throw IllegalDateException("date of birth cannot be later than the current day")
    return dateOfBirth.daysUntil(currentDate) / 7
}

fun calculateWeeksFromJson(
    jsonObject: JsonObject,
): List<TextSourcesList> {
    return listOf(
        buildEntities {
            try {
                val date: LocalDate = jsonObject["date"]
                    ?.jsonPrimitive?.contentOrNull
                    ?.toLocalDate()
                    ?: throw IllegalDateException("incorrect date in json")
                println(date)
                val weekCount = weekBetween(date)
                + "you lived " + bold("$weekCount") + " weeks"
            } catch (error: IllegalDateException) {
                + error.message
            }
        }
    )
}

/** END EDIT ZONE */

StringListScriptWrapper {
    calculateWeeksFromJson(it)
}
