/*
 * Copyright (c) 2023 Mark Dubkov. All rights reserved.
 */

package ru.kramlex.tgbot.core.other

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.lang.IllegalStateException

@Serializable
enum class ValidationType {
    @SerialName("regex")
    REGEX,
    @SerialName("isInt")
    IS_INT,
    @SerialName("isDouble")
    IS_DOUBLE,
}

@Serializable
data class ValidationData(
    val errorTextKey: String,
    val validations: List<Validation>
) {
    fun validate(text: String): Boolean =
        validations.any { it.validate(text) }
}

@Serializable
data class ValidationSurrogate(
    val type: ValidationType,
    val regex: String? = null
) {
    fun toOriginal(): Validation = when {
        type == ValidationType.REGEX && !regex.isNullOrBlank() -> RegexValidation(
            regex = regex,
            type = type
        )
        type == ValidationType.IS_INT -> IsIntValidation(
            type = type
        )
        type == ValidationType.IS_DOUBLE -> IsDoubleValidation(
            type = type
        )
        else -> throw IllegalStateException("ValidationSurrogat create impossible")
    }
    companion object {
        fun from(value: Validation) = when (value) {
            is RegexValidation ->
                ValidationSurrogate(
                    type = value.type,
                    regex = value.regex,
                )

            is IsDoubleValidation ->
                ValidationSurrogate(
                    type = value.type,
                )
            is IsIntValidation ->
                ValidationSurrogate(
                    type = value.type,
                )
        }
    }
}

@Serializable(with = ValidationSerializer::class)
sealed interface Validation {
    val type: ValidationType

    fun validate(text: String): Boolean
}

@Serializable
data class RegexValidation(
    override val type: ValidationType = ValidationType.REGEX,
    val regex: String
) : Validation {
    override fun validate(text: String): Boolean =
        regex.toRegex().containsMatchIn(text)
}

@Serializable
data class IsIntValidation(
    override val type: ValidationType = ValidationType.IS_INT
) : Validation {
    override fun validate(text: String): Boolean {
        return text.toIntOrNull() != null
    }
}

@Serializable
data class IsDoubleValidation(
    override val type: ValidationType = ValidationType.IS_DOUBLE
) : Validation {
    override fun validate(text: String): Boolean {
        return text.toDoubleOrNull() != null
    }
}

class ValidationSerializer: KSerializer<Validation> {
    private val strategy = ValidationSurrogate.serializer()

    override val descriptor: SerialDescriptor
        get() = strategy.descriptor
    override fun serialize(encoder: Encoder, value: Validation) =
        encoder.encodeSerializableValue(strategy, ValidationSurrogate.from(value))

    override fun deserialize(decoder: Decoder): Validation =
        decoder.decodeSerializableValue(strategy).toOriginal()
}
