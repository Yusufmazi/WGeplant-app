package com.wgeplant.common.dto.requests

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * This file holds all request DTOs and the serializer methods to serialize special data types.
 */

@Serializable
data class UserRequestDTO(
    val userId: String,
    val displayName: String,
    val profilePicture: String?
)

@Serializable
data class WGRequestDTO(
    val wgId: String?,
    val displayName: String,
    val invitationCode: String?,
    val profilePicture: String?
)

@Serializable
data class AbsenceRequestDTO(
    val absenceId: String?,
    val userId: String,
    @Serializable(with = DateSerializer::class)
    val startDate: LocalDate,
    @Serializable(with = DateSerializer::class)
    val endDate: LocalDate
)

@Serializable
data class AppointmentRequestDTO(
    val appointmentId: String?,
    val title: String,
    @Serializable(with = DateTimeSerializer::class)
    val startDate: LocalDateTime,
    @Serializable(with = DateTimeSerializer::class)
    val endDate: LocalDateTime,
    val affectedUsers: List<String>,
    val description: String?,
    @Serializable(with = ColorToIntSerializer::class)
    val color: Color
)

@Serializable
data class TaskRequestDTO(
    val taskId: String?,
    val title: String,
    @Serializable(with = DateSerializer::class)
    val date: LocalDate?,
    val affectedUsers: List<String>,
    val description: String?,
    @Serializable(with = ColorToIntSerializer::class)
    val color: Color,
    val stateOfTask: Boolean
)

private const val LOCAL_DATE_DESCRIPTOR = "LocalDate"

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object DateSerializer : KSerializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(LOCAL_DATE_DESCRIPTOR, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        return LocalDate.parse(decoder.decodeString(), formatter)
    }
}

private const val LOCAL_DATE_TIME_DESCRIPTOR = "LocalDateTime"

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object DateTimeSerializer : KSerializer<LocalDateTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(LOCAL_DATE_TIME_DESCRIPTOR, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalDateTime {
        return LocalDateTime.parse(decoder.decodeString(), formatter)
    }
}

private const val COLOR_DESCRIPTOR = "Color"

@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object ColorToIntSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor(COLOR_DESCRIPTOR, PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeInt(value.toArgb())
    }

    override fun deserialize(decoder: Decoder): Color {
        return Color(decoder.decodeInt())
    }
}
