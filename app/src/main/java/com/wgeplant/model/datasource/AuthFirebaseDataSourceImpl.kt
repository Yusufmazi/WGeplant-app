package com.wgeplant.model.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GetTokenResult
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okio.IOException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * This class is communicating with firebase authentication regarding the users authentication.
 * @param firebaseAuth to address firebase authentication
 */
class AuthFirebaseDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthFirebaseDataSource {

    companion object {
        private const val ERROR_ID_TOKEN_NULL: String = "Es gab Probleme bei der Anmeldung."

        private const val ERROR_INVALID_MAIL: String = "ERROR_INVALID_EMAIL"

        private const val ERROR_WRONG_PASSWORD: String = "ERROR_WRONG_PASSWORD"

        private const val ERROR_USER_NOT_FOUND: String = "ERROR_USER_NOT_FOUND"

        private const val ERROR_IN_USE_MAIL: String = "ERROR_EMAIL_ALREADY_IN_USE"

        private const val ERROR_WEAK_PASSWORD: String = "ERROR_WEAK_PASSWORD"

        private const val ERROR_TOO_MANY_REQUESTS: String = "ERROR_TOO_MANY_REQUESTS"

        private const val ERROR_INVALID_CREDENTIAL: String = "ERROR_INVALID_CREDENTIAL"
    }

    /**
     * This method sends firebase authentication a request to sign in the current user.
     * The idToken is returned when the process was successful.
     * @param email of the current user
     * @param password of the current user
     */
    override suspend fun loginWithEmailAndPassword(
        email: String,
        password: String
    ): Result<String, DomainError> {
        return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = firebaseAuth.currentUser

            if (user != null) {
                val idTokenResult: GetTokenResult = user.getIdToken(true).await()
                val idToken: String? = idTokenResult.token

                if (idToken != null) {
                    Result.Success(idToken)
                } else {
                    Result.Error(DomainError.FirebaseError.UnknownFirebaseError(ERROR_ID_TOKEN_NULL))
                }
            } else {
                Result.Error(DomainError.FirebaseError.UserNotFound)
            }
        } catch (e: FirebaseAuthException) {
            Result.Error(mapFirebaseException(e))
        } catch (e: UnknownHostException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends firebase authentication a request to register a new user.
     * It returns the idToken when it was successful.
     * @param email of the new user
     * @param password of the new user
     */
    override suspend fun registerNewUser(
        email: String,
        password: String
    ): Result<String, DomainError> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                val idTokenResult: GetTokenResult = user.getIdToken(true).await()
                val idToken: String? = idTokenResult.token

                if (idToken != null) {
                    Result.Success(idToken)
                } else {
                    Result.Error(DomainError.FirebaseError.UnknownFirebaseError(ERROR_ID_TOKEN_NULL))
                }
            } else {
                Result.Error(DomainError.FirebaseError.UserNotFound)
            }
        } catch (e: FirebaseAuthException) {
            Result.Error(mapFirebaseException(e))
        } catch (e: UnknownHostException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends firebase authentication a request to logout the current user.
     */
    override suspend fun logout(): Result<Unit, DomainError> {
        return try {
            firebaseAuth.signOut()
            Result.Success(Unit)
        } catch (e: FirebaseAuthException) {
            Result.Error(mapFirebaseException(e))
        } catch (e: UnknownHostException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends firebase authentication a request to delete the account of the user.
     */
    override suspend fun deleteAccount(): Result<Unit, DomainError> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                currentUser.delete().await()
                Result.Success(Unit)
            } else {
                Result.Error(DomainError.FirebaseError.UserNotFound)
            }
        } catch (e: FirebaseAuthException) {
            Result.Error(mapFirebaseException(e))
        } catch (e: UnknownHostException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends firebase authentication a request to get the uid of the current user.
     */
    override suspend fun getCurrentUserId(): Result<String, DomainError> {
        return try {
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                Result.Success(uid)
            } else {
                Result.Error(DomainError.FirebaseError.UserNotFound)
            }
        } catch (e: FirebaseAuthException) {
            Result.Error(mapFirebaseException(e))
        } catch (e: UnknownHostException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method returns a unit if the current user is logged in Firebase Authentication.
     */
    override suspend fun isLoggedIn(): Result<String, DomainError> {
        return try {
            val currentUser = firebaseAuth.currentUser
            if (currentUser != null) {
                val idTokenResult: GetTokenResult = currentUser.getIdToken(true).await()
                val idToken: String? = idTokenResult.token

                if (idToken != null) {
                    return Result.Success(idToken)
                } else {
                    return Result.Error(
                        DomainError.FirebaseError
                            .UnknownFirebaseError(ERROR_ID_TOKEN_NULL)
                    )
                }
            } else {
                Result.Error(DomainError.FirebaseError.UserNotFound)
            }
        } catch (e: FirebaseAuthException) {
            Result.Error(mapFirebaseException(e))
        } catch (e: UnknownHostException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method uses the callback Flow from Firebase to get the authentication state of the
     * current user.
     */
    override fun getAuthStateFlow(): Flow<Result<Boolean, DomainError>> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser

            if (firebaseUser != null) {
                trySend(Result.Success(true))
            } else {
                trySend(Result.Success(false))
            }
        }
        try {
            firebaseAuth.addAuthStateListener(authStateListener)
        } catch (e: FirebaseAuthException) {
            trySend(Result.Error(mapFirebaseException(e)))
            close()
            return@callbackFlow
        } catch (e: UnknownHostException) {
            trySend(Result.Error(DomainError.NetworkError))
            close()
            return@callbackFlow
        } catch (e: IOException) {
            trySend(Result.Error(DomainError.NetworkError))
            close()
            return@callbackFlow
        } catch (e: Exception) {
            trySend(Result.Error(DomainError.Unknown(e)))
            close()
            return@callbackFlow
        }
        awaitClose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    /**
     * This method reloads the current users authentication state from firebase authentication.
     */
    override suspend fun reloadCurrentUser(): Result<Unit, DomainError> {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                user.reload().await()
                Result.Success(Unit)
            } else {
                Result.Error(DomainError.FirebaseError.UserNotFound)
            }
        } catch (e: FirebaseAuthException) {
            Result.Error(mapFirebaseException(e))
        } catch (e: UnknownHostException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method maps firebase error codes to domain errors.
     * @param e the firebase exception
     */
    private fun mapFirebaseException(e: FirebaseAuthException): DomainError.FirebaseError {
        return when (e.errorCode) {
            ERROR_INVALID_MAIL -> DomainError.FirebaseError.InvalidEmailFormat
            ERROR_WRONG_PASSWORD -> DomainError.FirebaseError.WrongPassword
            ERROR_USER_NOT_FOUND -> DomainError.FirebaseError.UserNotFound
            ERROR_IN_USE_MAIL -> DomainError.FirebaseError.EmailAlreadyInUse
            ERROR_WEAK_PASSWORD -> DomainError.FirebaseError.WeakPassword
            ERROR_TOO_MANY_REQUESTS -> DomainError.FirebaseError.TooManyRequests
            ERROR_INVALID_CREDENTIAL -> DomainError.FirebaseError.InvalidCredentials
            else -> {
                DomainError.FirebaseError.UnknownFirebaseError(e.errorCode, e.message)
            }
        }
    }
}
