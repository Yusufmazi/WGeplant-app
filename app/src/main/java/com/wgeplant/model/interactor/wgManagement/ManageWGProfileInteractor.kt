package com.wgeplant.model.interactor.wgManagement

import android.net.Uri
import com.wgeplant.model.domain.Absence
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import com.wgeplant.model.domain.WG
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * This interface is responsible for starting the WG profile related use cases.
 */
interface ManageWGProfileInteractor {
    /**
     * This method gets the wg of the local user and sends all changes of the saved wg to the UI.
     */
    fun getWGData(): Flow<Result<WG?, DomainError>>

    /**
     * This method gets all members the wg of the local user and sends all changes of the saved list to the UI.
     */
    fun getWGMembers(): Flow<Result<List<User>, DomainError>>

    /**
     * This method gets the invitation code of the wg of the local user.
     */
    suspend fun executeInvitationCodeInquiry(): Result<String, DomainError>

    /**
     * This method changes the display name of the wg of the local user for everyone in the wg
     * @param name: The new display name
     */
    suspend fun executeDisplayNameChange(name: String): Result<Unit, DomainError>

    /**
     * This method changes the profile picture of the wg of the local user for everyone in the wg
     * @param uri: The new profile picture
     */
    suspend fun executeProfilePictureChange(uri: Uri): Result<Unit, DomainError>

    /**
     * This method edits an existing absence entry of the local user.
     * @param absenceId: The ID of the absence to be edited.
     * @param startDate: The new start date of the absence.
     * @param endDate: The new end date of the absence.
     */
    suspend fun executeAbsenceEditing(
        absenceId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<Unit, DomainError>

    /**
     * This method deletes an existing absence entry of the local user.
     * @param absenceId: The ID of the absence to be deleted.
     */
    suspend fun executeAbsenceDeletion(absenceId: String): Result<Unit, DomainError>

    /**
     * This method gets all absences of the local user and sends all changes of the saved list to the UI.
     * @param userId: The ID of the user for which the absences should be retrieved.
     */
    fun getAbsence(userId: String): Flow<Result<List<Absence>, DomainError>>
}
