package com.wgeplant.model.repository

import android.net.Uri
import com.wgeplant.model.datasource.FirebaseStorageDataSource
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import javax.inject.Inject

/**
 * This class manages the upload and deletion of the profile pictures from firebase storage.
 * @param storageDataSource to communicate with firebase storage
 */
class StorageRepoImpl @Inject constructor(
    private val storageDataSource: FirebaseStorageDataSource
) : StorageRepo {
    /**
     * This method uploads the profile picture of a user on firebase storage.
     * It returns the url of the image.
     * @param userId of the current user
     * @param imageUri of the uploading image
     */
    override suspend fun uploadUserImage(userId: String, imageUri: Uri):
        Result<String, DomainError> {
        return storageDataSource.uploadUserImage(userId, imageUri)
    }

    /**
     * This method uploads the profile picture of the wg on firebase storage.
     * It returns the url of the image.
     * @param wgId of the wg
     * @param imageUri of the image
     */
    override suspend fun uploadWGImage(wgId: String, imageUri: Uri): Result<String, DomainError> {
        return storageDataSource.uploadWGImage(wgId, imageUri)
    }

    /**
     * This method deletes the profile picture of a user.
     */
    override suspend fun deleteUserImage(): Result<Unit, DomainError> {
        return storageDataSource.deleteUserImage()
    }

    /**
     * This method deletes the profile picture of a wg.
     */
    override suspend fun deleteWGImage(wgId: String): Result<Unit, DomainError> {
        return storageDataSource.deleteWGImage(wgId)
    }
}
