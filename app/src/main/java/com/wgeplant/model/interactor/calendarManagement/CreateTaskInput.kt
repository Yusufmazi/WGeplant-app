package com.wgeplant.model.interactor.calendarManagement

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

/**
 * This data class is used to create a new input for a task.
 * @param title: The title of the task.
 * @param date: The date of the task. It's optional.
 * @param affectedUsers: The users affected by the task.
 * @param color: The color of the task.
 * @param description: The description of the task. It's optional.
 */
data class CreateTaskInput(
    val title: String,
    val date: LocalDate? = null,
    val affectedUsers: List<String>,
    val color: Color,
    val description: String = ""
)
