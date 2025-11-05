package com.wgeplant.model.datasource

import android.net.Uri
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for communicating with firebase storage.
 */
interface FirebaseStorageDataSource {
    /**
     * This method gets the Uri of an image and sets a path name, before uploading it on Storage.
     * After successfully uploading it, it returns the downloadUrl of the image.
     * @param userId of the current user
     * @param imageUri of the upload image
     */
    suspend fun uploadUserImage(userId: String, imageUri: Uri): Result<String, DomainError>

    /**
     * This method uploads the image for the wg profile to firebase storage.
     * @param wgId of the wg
     * @param imageUri of the uploaded picture
     */
    suspend fun uploadWGImage(wgId: String, imageUri: Uri): Result<String, DomainError>

    /**
     * This method deletes the profile picture of the current user.
     */
    suspend fun deleteUserImage(): Result<Unit, DomainError>

    /**
     * This method deletes the profile picture of a wg.
     * @param wgId of the wg
     */
    suspend fun deleteWGImage(wgId: String): Result<Unit, DomainError>
}
