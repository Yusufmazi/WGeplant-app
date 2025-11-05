package com.wgeplant.ui.toDo

import androidx.compose.runtime.Immutable
import com.wgeplant.model.domain.Task

/**
 * UI state for the To-Do screen.
 *
 * It is marked as @Immutable to ensure predictable behavior in Compose.
 *
 */
@Immutable
data class ToDoUiState(
    val tasks: List<Task> = emptyList(),
    val wgProfileImageUrl: String? = null
)
