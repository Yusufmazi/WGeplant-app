package com.wgeplant.model.datasource

import android.net.Uri
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * This class communicates with firebase storage to upload and delete profile pictures.
 * @param storage to communicate with storage
 * @param authentication to authenticate the current user
 */
class FirebaseStorageDataSourceImpl @Inject constructor(
    private val storage: FirebaseStorage,
    private val authentication: FirebaseAuth
) : FirebaseStorageDataSource {

    companion object {
        const val USER_PICTURES_PATH_PREFIX: String = "profile_pictures"
        const val WG_PICTURES_PATH_PREFIX: String = "wg_profile_picture"
        const val PATH_SUFFIX: String = "profile.jpg"
    }

    /**
     * This method gets the Uri of an image and sets a path name, before uploading it on Storage.
     * After successfully uploading it, it returns the downloadUrl of the image.
     * @param userId of the current user
     * @param imageUri of the upload image
     */
    override suspend fun uploadUserImage(
        userId: String,
        imageUri: Uri
    ): Result<String, DomainError> {
        return try {
            val currentUser = authentication.currentUser
            if (currentUser == null || currentUser.uid != userId) {
                return Result.Error(DomainError.UnauthorizedError)
            }

            val storageRef = storage.reference
            val profilePicRef: StorageReference =
                storageRef.child("$USER_PICTURES_PATH_PREFIX/$userId/$PATH_SUFFIX")

            val uploadTask = profilePicRef.putFile(imageUri).await()

            val downloadUrl = uploadTask.storage.downloadUrl.await().toString()
            Result.Success(downloadUrl)
        } catch (e: FirebaseException) {
            Result.Error(DomainError.FirebaseError.UnknownFirebaseError(e.toString()))
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method uploads the image for the wg profile to firebase storage.
     * @param wgId of the wg
     * @param imageUri of the uploaded picture
     */
    override suspend fun uploadWGImage(wgId: String, imageUri: Uri): Result<String, DomainError> {
        return try {
            if (wgId.isBlank()) {
                return Result.Error(DomainError.NotFoundError)
            }
            val storageRef = storage.reference

            val profilePicRef: StorageReference =
                storageRef.child("$WG_PICTURES_PATH_PREFIX/$wgId/$PATH_SUFFIX")
            profilePicRef.putFile(imageUri).await()

            val downloadUrl = profilePicRef.downloadUrl.await().toString()
            Result.Success(downloadUrl)
        } catch (e: FirebaseException) {
            Result.Error(DomainError.FirebaseError.UnknownFirebaseError(e.toString()))
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method deletes the profile picture of the current user.
     */
    override suspend fun deleteUserImage(): Result<Unit, DomainError> {
        return try {
            val user = authentication.currentUser ?: return Result
                .Error(DomainError.UnauthorizedError)

            val userId = user.uid
            val storageRef = storage.reference

            val profilePicRef: StorageReference =
                storageRef.child("$USER_PICTURES_PATH_PREFIX/$userId/$PATH_SUFFIX")

            profilePicRef.delete().await()
            Result.Success(Unit)
        } catch (e: FirebaseException) {
            Result.Error(DomainError.FirebaseError.UnknownFirebaseError(e.toString()))
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method deletes the profile picture of a wg.
     * @param wgId of the wg
     */
    override suspend fun deleteWGImage(wgId: String): Result<Unit, DomainError> {
        return try {
            val user = authentication.currentUser

            if (wgId.isBlank() || user == null) {
                return Result.Error(DomainError.NotFoundError)
            }

            val storageRef = storage.reference

            val profilePicRef: StorageReference =
                storageRef.child("$WG_PICTURES_PATH_PREFIX/$wgId/$PATH_SUFFIX")

            profilePicRef.delete()
            Result.Success(Unit)
        } catch (e: FirebaseException) {
            Result.Error(DomainError.FirebaseError.UnknownFirebaseError(e.toString()))
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }
}
