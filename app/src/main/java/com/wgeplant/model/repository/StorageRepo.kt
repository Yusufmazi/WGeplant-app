package com.wgeplant.model.repository

import android.net.Uri
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for managing the profile pictures.
 */
interface StorageRepo {
    /**
     * This method uploads the profile picture of a user on firebase storage.
     * It returns the url of the image.
     * @param userId of the current user
     * @param imageUri of the uploading image
     */
    suspend fun uploadUserImage(userId: String, imageUri: Uri): Result<String, DomainError>

    /**
     * This method uploads the profile picture of the wg on firebase storage.
     * It returns the url of the image.
     * @param wgId of the wg
     * @param imageUri of the image
     */
    suspend fun uploadWGImage(wgId: String, imageUri: Uri): Result<String, DomainError>

    /**
     * This method deletes the profile picture of a user.
     */
    suspend fun deleteUserImage(): Result<Unit, DomainError>

    /**
     * This method deletes the profile picture of a wg.
     */
    suspend fun deleteWGImage(wgId: String): Result<Unit, DomainError>
}
