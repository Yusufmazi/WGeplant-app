package com.wgeplant.model.domain

import androidx.compose.ui.graphics.Color
import com.wgeplant.common.dto.requests.TaskRequestDTO
import java.time.LocalDate

/**
 * This data class encapsulates all necessary task data.
 * @property taskId The unique ID of a task.
 * @property title The title the task was saved under.
 * @property date The date the task is due. It's optional.
 * @property affectedUsers The IDs of the users assigned to the task.
 * @property color The color the task was marked with.
 * @property description The description under which the appointment was saved. It's optional.
 * @property stateOfTask The task's status is completed when stateOfTask is True,
 * and incomplete when stateOfTask is False.
 */
data class Task(
    val taskId: String?,
    val title: String,
    val date: LocalDate? = null,
    val affectedUsers: List<String>,
    val color: Color,
    val description: String?,
    val stateOfTask: Boolean
)

fun Task.toRequestDto(): TaskRequestDTO {
    return TaskRequestDTO(
        taskId = this.taskId,
        title = this.title,
        date = this.date,
        affectedUsers = this.affectedUsers,
        color = this.color,
        description = this.description,
        stateOfTask = this.stateOfTask
    )
}
