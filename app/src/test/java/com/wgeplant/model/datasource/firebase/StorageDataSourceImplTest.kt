package com.wgeplant.model.datasource.firebase

import android.net.Uri
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.UploadTask.TaskSnapshot
import com.wgeplant.model.datasource.FirebaseStorageDataSourceImpl
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StorageDataSourceImplTest {
    @Mock
    private lateinit var mockFirebaseAuth: FirebaseAuth

    @Mock
    private lateinit var mockFirebaseUser: FirebaseUser

    @Mock
    private lateinit var mockFirebaseStorage: FirebaseStorage

    @Mock
    private lateinit var mockStorageReference: StorageReference

    @Mock
    private lateinit var mockChildStorageReference: StorageReference

    @Mock
    private lateinit var mockUploadTask: UploadTask

    @Mock
    private lateinit var mockTaskSnapshot: TaskSnapshot

    @Mock
    private lateinit var mockDownloadedUrlTask: Task<Uri>

    @Mock
    private lateinit var mockDeleteTask: Task<Void>

    @Mock
    private lateinit var mockUri: Uri

    private lateinit var dataSource: FirebaseStorageDataSourceImpl

    private val PATH_SUFFIX = "profile.jpg"
    private val USER_PATH_PREFIX = "profile_pictures"
    private val userId = "user123"
    private val downloadUrl = "https://fake.url/download/url/$PATH_SUFFIX"

    @Before
    fun setUp() {
        mockFirebaseAuth = mock()
        mockFirebaseUser = mock()
        mockFirebaseStorage = mock()
        mockStorageReference = mock()
        mockChildStorageReference = mock()
        mockUploadTask = mock()
        mockTaskSnapshot = mock()
        mockDownloadedUrlTask = mock()
        mockDeleteTask = mock()
        mockUri = mock()

        dataSource = FirebaseStorageDataSourceImpl(mockFirebaseStorage, mockFirebaseAuth)

        whenever(mockFirebaseStorage.reference).thenReturn(mockStorageReference)
        whenever(mockStorageReference.child(anyString())).thenReturn(mockChildStorageReference)

        whenever(mockChildStorageReference.putFile(mockUri)).thenReturn(mockUploadTask)
        whenever(mockUploadTask.isSuccessful).thenReturn(true)
        whenever(mockUploadTask.isComplete).thenReturn(true)
        whenever(mockUploadTask.exception).thenReturn(null)
        whenever(mockUploadTask.addOnFailureListener(any())).thenReturn(mockUploadTask)

        whenever(mockDownloadedUrlTask.isComplete).thenReturn(true)
        whenever(mockDownloadedUrlTask.isSuccessful).thenReturn(true)
        whenever(mockDownloadedUrlTask.exception).thenReturn(null)
        whenever(mockDownloadedUrlTask.result).thenReturn(mockUri)
        whenever(mockDownloadedUrlTask.getResult(any<Class<Exception>>())).thenReturn(mockUri)

        whenever(mockChildStorageReference.delete()).thenReturn(mockDeleteTask)
        whenever(mockDeleteTask.isComplete).thenReturn(true)
        whenever(mockDeleteTask.isSuccessful).thenReturn(true)
        whenever(mockDeleteTask.exception).thenReturn(null)
    }

    @Test
    fun `uploadUserImage with valid user should return download URL Success(String)`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(userId)
        whenever(mockFirebaseStorage.reference).thenReturn(mockStorageReference)
        val expectedPath = "$USER_PATH_PREFIX/$userId/$PATH_SUFFIX"
        whenever(mockStorageReference.child(expectedPath)).thenReturn(mockChildStorageReference)
        whenever(mockChildStorageReference.putFile(mockUri)).thenReturn(mockUploadTask)
        whenever(mockUploadTask.isSuccessful).thenReturn(true)
        whenever(mockUploadTask.isComplete).thenReturn(true)
        whenever(mockUploadTask.isCanceled).thenReturn(false)
        whenever(mockUploadTask.exception).thenReturn(null)
        whenever(mockUploadTask.result).thenReturn(mockTaskSnapshot)

        whenever(mockTaskSnapshot.storage).thenReturn(mockChildStorageReference)

        whenever(mockChildStorageReference.downloadUrl).thenReturn(mockDownloadedUrlTask)
        whenever(mockDownloadedUrlTask.isSuccessful).thenReturn(true)
        whenever(mockDownloadedUrlTask.isComplete).thenReturn(true)
        whenever(mockDownloadedUrlTask.exception).thenReturn(null)
        whenever(mockDownloadedUrlTask.result).thenReturn(mockUri)
        whenever(mockUri.toString()).thenReturn(downloadUrl)

        val result = dataSource.uploadUserImage(userId, mockUri)

        assertTrue(result is Result.Success)
        assertEquals(downloadUrl, result.data)

        verify(mockFirebaseAuth).currentUser
        verify(mockFirebaseStorage).reference
        verify(mockStorageReference).child(expectedPath)
        verify(mockChildStorageReference).putFile(mockUri)
        verify(mockChildStorageReference).downloadUrl
    }

    @Test
    fun `uploadUserImage handles RuntimeException during putFile`() = runTest {
        val simulatedRuntimeException = RuntimeException("Simulated error")
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(userId)
        whenever(mockFirebaseStorage.reference).thenReturn(mockStorageReference)
        val expectedPath = "$USER_PATH_PREFIX/$userId/$PATH_SUFFIX"
        whenever(mockStorageReference.child(expectedPath)).thenReturn(mockChildStorageReference)
        whenever(mockChildStorageReference.putFile(mockUri)).thenReturn(mockUploadTask)
        whenever(mockUploadTask.isSuccessful).thenReturn(false)
        whenever(mockUploadTask.isComplete).thenReturn(true)
        whenever(mockUploadTask.exception).thenReturn(simulatedRuntimeException)

        val expectedError = DomainError.Unknown(simulatedRuntimeException)

        val result = dataSource.uploadUserImage(userId, mockUri)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)
        verify(mockChildStorageReference, never()).downloadUrl
    }

    @Test
    fun `deleteUserImage with valid user should return Success(Unit)`() = runTest {
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(userId)
        whenever(mockFirebaseStorage.reference).thenReturn(mockStorageReference)
        val expectedPath = "$USER_PATH_PREFIX/$userId/$PATH_SUFFIX"
        whenever(mockStorageReference.child(expectedPath)).thenReturn(mockChildStorageReference)
        whenever(mockChildStorageReference.delete()).thenReturn(mockDeleteTask)
        whenever(mockDeleteTask.isSuccessful).thenReturn(true)
        whenever(mockDeleteTask.isComplete).thenReturn(true)
        whenever(mockDeleteTask.exception).thenReturn(null)
        whenever(mockDeleteTask.result).thenReturn(null)

        val result = dataSource.deleteUserImage()

        assertTrue(result is Result.Success)
        assertEquals(Unit, (result as Result.Success).data)
        verify(mockFirebaseAuth).currentUser
        verify(mockFirebaseStorage).reference
        verify(mockStorageReference).child(expectedPath)
        verify(mockChildStorageReference).delete()
    }

    @Test
    fun `deleteUserImage handles FirebaseException during storage delete`() = runTest {
        val simulatedRuntimeException = RuntimeException("Simulated error")
        whenever(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
        whenever(mockFirebaseUser.uid).thenReturn(userId)
        whenever(mockFirebaseStorage.reference).thenReturn(mockStorageReference)
        val expectedPath = "$USER_PATH_PREFIX/$userId/$PATH_SUFFIX"
        whenever(mockStorageReference.child(expectedPath)).thenReturn(mockChildStorageReference)
        whenever(mockChildStorageReference.delete()).thenReturn(mockDeleteTask)

        whenever(mockDeleteTask.isSuccessful).thenReturn(false)
        whenever(mockDeleteTask.isComplete).thenReturn(true)
        whenever(mockDeleteTask.exception).thenReturn(simulatedRuntimeException)

        val result = dataSource.deleteUserImage()

        val expectedError = DomainError.Unknown(simulatedRuntimeException)

        assertTrue(result is Result.Error)
        assertEquals(expectedError, result.error)

        verify(mockFirebaseAuth).currentUser
        verify(mockFirebaseStorage).reference
        verify(mockStorageReference).child(expectedPath)
        verify(mockChildStorageReference).delete()
    }
}
