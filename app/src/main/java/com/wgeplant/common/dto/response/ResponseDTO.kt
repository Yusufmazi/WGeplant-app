package com.wgeplant.common.dto.response

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.Task
import com.wgeplant.model.domain.User
import com.wgeplant.model.domain.WG
import com.wgeplant.model.persistence.Persistence
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
 * This file holds all response DTOs and their methods that map them to a domain object.
 * Additionally the serializer methods are also in this file.
 */

@Serializable
data class UserResponseDTO(
    val userId: String,
    val displayName: String,
    val profilePicture: String?
)

fun UserResponseDTO.toDomain(): User {
    return User(
        userId = this.userId,
        displayName = this.displayName,
        profilePicture = this.profilePicture
    )
}

@Serializable
data class WGResponseDTO(
    val wgId: String,
    val displayName: String,
    val invitationCode: String,
    val profilePicture: String?
)

fun WGResponseDTO.toDomain(): WG {
    return WG(
        wgId = this.wgId,
        displayName = this.displayName,
        invitationCode = this.invitationCode,
        profilePicture = this.profilePicture
    )
}

@Serializable
data class AbsenceResponseDTO(
    val absenceId: String,
    val userId: String,
    @Serializable(with = DateSerializer::class)
    val startDate: LocalDate,
    @Serializable(with = DateSerializer::class)
    val endDate: LocalDate
)

fun AbsenceResponseDTO.toDomain(): Absence {
    return Absence(
        absenceId = this.absenceId,
        userId = this.userId,
        startDate = this.startDate,
        endDate = this.endDate
    )
}

@Serializable
data class AppointmentResponseDTO(
    val appointmentId: String,
    val title: String,
    @Serializable(with = DateTimeSerializer::class)
    val startDate: LocalDateTime,
    @Serializable(with = DateTimeSerializer::class)
    val endDate: LocalDateTime,
    val affectedUsers: List<String>,
    @Serializable(with = ColorToIntSerializer::class)
    val color: Color,
    val description: String?
)

fun AppointmentResponseDTO.toDomain(): Appointment {
    return Appointment(
        appointmentId = this.appointmentId,
        title = this.title,
        startDate = this.startDate,
        endDate = this.endDate,
        affectedUsers = this.affectedUsers,
        color = this.color,
        description = this.description
    )
}

@Serializable
data class TaskResponseDTO(
    val taskId: String,
    val title: String,
    @Serializable(with = DateSerializer::class)
    val date: LocalDate?,
    val affectedUsers: List<String>,
    val description: String?,
    @Serializable(with = ColorToIntSerializer::class)
    val color: Color,
    val stateOfTask: Boolean
)

fun TaskResponseDTO.toDomain(): Task {
    return Task(
        taskId = this.taskId,
        title = this.title,
        date = this.date,
        affectedUsers = this.affectedUsers,
        color = this.color,
        description = this.description,
        stateOfTask = this.stateOfTask
    )
}

@Serializable
data class InitialResponseDTO(
    val userResponseDTOs: List<UserResponseDTO>,
    val wgResponseDTO: WGResponseDTO?,
    val appointmentResponseDTOs: List<AppointmentResponseDTO>,
    val taskResponseDTOs: List<TaskResponseDTO>,
    val absenceResponseDTOs: List<AbsenceResponseDTO>
)

fun InitialResponseDTO.toPersistence(): Result<Unit, DomainError> {
    return try {
        val newMembers = this.userResponseDTOs.map {
                member ->
            member.toDomain()
        }
        val newWG = this.wgResponseDTO?.toDomain()
        val newAppointments = this.appointmentResponseDTOs.map {
                appointment ->
            appointment.toDomain()
        }
        val newTasks = this.taskResponseDTOs.map {
                task ->
            task.toDomain()
        }
        val newAbsences = this.absenceResponseDTOs.map {
                absence ->
            absence.toDomain()
        }

        Persistence.saveUsersInWG(newMembers)
        Persistence.saveWGOfLocalUser(newWG)
        Persistence.saveUserAppointments(newAppointments)
        Persistence.saveUserTasks(newTasks)
        Persistence.saveUserAbsences(newAbsences)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(DomainError.PersistenceError)
    }
}

@Serializable
data class ErrorResponseDTO(
    val message: String
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
