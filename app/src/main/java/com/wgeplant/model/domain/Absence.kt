package com.wgeplant.model.domain

import com.wgeplant.common.dto.requests.AbsenceRequestDTO
import java.time.LocalDate

/**
 * This data class encapsulates all necessary absence data.
 * @property absenceId The unique ID of an absence entry.
 * @property userId The user ID of the user who created the attendance entry.
 * @property startDate The start date of the user's absence.
 * @property endDate The end date of the user's absence.
 */
data class Absence(
    var absenceId: String? = null,
    val userId: String,
    val startDate: LocalDate,
    val endDate: LocalDate
)

fun Absence.toRequestDto(): AbsenceRequestDTO {
    return AbsenceRequestDTO(
        absenceId = this.absenceId,
        userId = this.userId,
        startDate = this.startDate,
        endDate = this.endDate
    )
}
