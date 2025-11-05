package com.wgeplant.model.domain

import androidx.compose.ui.graphics.Color
import com.wgeplant.common.dto.requests.AppointmentRequestDTO
import java.time.LocalDateTime

/**
 * This data class encapsulates all necessary appointment data.
 * @property appointmentId The unique ID of an appointment.
 * @property title The title under which the appointment was saved.
 * @property startDate The date the appointment starts.
 * @property endDate The date the appointment ends.
 * @property affectedUsers The IDs of the users who are part of the appointment.
 * @property color The color the appointment was marked with.
 * @property description The description under which the appointment was saved. It's optional.
 */
data class Appointment(
    val appointmentId: String?,
    val title: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val affectedUsers: List<String>,
    val color: Color,
    val description: String?
)

fun Appointment.toRequestDto(): AppointmentRequestDTO {
    return AppointmentRequestDTO(
        appointmentId = this.appointmentId,
        title = this.title,
        startDate = this.startDate,
        endDate = this.endDate,
        affectedUsers = this.affectedUsers,
        color = this.color,
        description = this.description
    )
}
