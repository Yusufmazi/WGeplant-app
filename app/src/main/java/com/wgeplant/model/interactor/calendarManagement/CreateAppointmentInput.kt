package com.wgeplant.model.interactor.calendarManagement

import androidx.compose.ui.graphics.Color
import java.time.LocalDateTime

/**
 * This data class is used to create a new input for an appointment.
 * @param title: The title of the appointment.
 * @param startDate: The start date and time of the appointment.
 * @param endDate: The end date and time of the appointment.
 * @param affectedUsers: The users affected by the appointment.
 * @param color: The color of the appointment.
 * @param description: The description of the appointment. It's optional.
 */
data class CreateAppointmentInput(
    val title: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val affectedUsers: List<String>,
    val color: Color,
    val description: String = ""
)
