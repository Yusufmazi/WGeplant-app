package com.wgeplant.model.interactor.userManagement

import android.net.Uri
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * This interface is responsible for starting the user profile related use cases.
 */
interface ManageUserProfileInteractor {
    /**
     * This method gets the user data of the local user and sends all changes of the saved user to the UI.
     */
    suspend fun getUserData(): Flow<Result<User?, DomainError>>

    /**
     * This method changes the display name of the local user.
     * @param displayName: The new display name
     */
    suspend fun executeDisplayNameChange(displayName: String): Result<Unit, DomainError>

    /**
     * This method changes the profile picture of the local user.
     * @param uri: The new profile picture
     */
    suspend fun executeProfilePictureChange(uri: Uri): Result<Unit, DomainError>

    /**
     * This method creates an absence entry for the local user.
     * @param startDate: The start date of the absence.
     * @param endDate: The end date of the absence.
     */
    suspend fun executeAbsenceEntry(startDate: LocalDate, endDate: LocalDate): Result<Unit, DomainError>
}
