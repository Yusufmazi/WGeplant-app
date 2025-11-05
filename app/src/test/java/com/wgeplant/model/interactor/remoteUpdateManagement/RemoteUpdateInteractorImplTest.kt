package com.wgeplant.model.interactor.remoteUpdateManagement

import com.wgeplant.model.datasource.remote.api.HeaderConfiguration
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.deviceManagement.ManageDeviceInteractor
import com.wgeplant.model.repository.AbsenceRepo
import com.wgeplant.model.repository.AppointmentRepo
import com.wgeplant.model.repository.AuthRepo
import com.wgeplant.model.repository.DeleteLocalDataRepo
import com.wgeplant.model.repository.InitialDataRepo
import com.wgeplant.model.repository.TaskRepo
import com.wgeplant.model.repository.UserRepo
import com.wgeplant.model.repository.WGRepo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class RemoteUpdateInteractorImplTest {

    companion object {
        private const val TEST_OBJECT_ID = "testObjectId"
        private const val TEST_USER_ID = "testUserId"
        private const val EMPTY_OBJECT_ID = ""
        private const val ADD_USER_OPERATION = "addUser"
        private const val UPDATE_USER_OPERATION = "updateUser"
        private const val UPDATE_WG_OPERATION = "updateWG"
        private const val ADD_APPOINTMENT_OPERATION = "addAppointment"
        private const val UPDATE_APPOINTMENT_OPERATION = "updateAppointment"
        private const val DELETE_APPOINTMENT_OPERATION = "deleteAppointment"
        private const val ADD_TASK_OPERATION = "addTask"
        private const val UPDATE_TASK_OPERATION = "updateTask"
        private const val DELETE_TASK_OPERATION = "deleteTask"
        private const val ADD_ABSENCE_OPERATION = "addAbsence"
        private const val UPDATE_ABSENCE_OPERATION = "updateAbsence"
        private const val DELETE_ABSENCE_OPERATION = "deleteAbsence"
        private const val INITIAL_DATA_OPERATION = "initialData"
    }

    // mocks for dependencies
    private lateinit var mockUserRepo: UserRepo
    private lateinit var mockWGRepo: WGRepo
    private lateinit var mockTaskRepo: TaskRepo
    private lateinit var mockAppointmentRepo: AppointmentRepo
    private lateinit var mockAbsenceRepo: AbsenceRepo
    private lateinit var mockInitialDataRepo: InitialDataRepo
    private lateinit var mockDeleteLocalDataRepo: DeleteLocalDataRepo
    private lateinit var mockManageDeviceInteractor: ManageDeviceInteractor
    private lateinit var mockHeaderConfiguration: HeaderConfiguration
    private lateinit var mockAuthRepo: AuthRepo

    // class that gets tested
    private lateinit var remoteUpdateInteractor: RemoteUpdateInteractorImpl

    @Before
    fun setUp() {
        mockUserRepo = mock()
        mockWGRepo = mock()
        mockTaskRepo = mock()
        mockAppointmentRepo = mock()
        mockAbsenceRepo = mock()
        mockInitialDataRepo = mock()
        mockDeleteLocalDataRepo = mock()
        mockManageDeviceInteractor = mock()
        mockHeaderConfiguration = mock()
        mockAuthRepo = mock()

        remoteUpdateInteractor = RemoteUpdateInteractorImpl(
            userRepo = mockUserRepo,
            wgRepo = mockWGRepo,
            taskRepo = mockTaskRepo,
            appointmentRepo = mockAppointmentRepo,
            absenceRepo = mockAbsenceRepo,
            initialDataRepo = mockInitialDataRepo,
            deleteLocalDataRepo = mockDeleteLocalDataRepo,
            manageDeviceInteractor = mockManageDeviceInteractor,
            headerConfiguration = mockHeaderConfiguration,
            authRepo = mockAuthRepo
        )
    }

    @After
    fun tearDown() {
        remoteUpdateInteractor.cancelAllPendingOperations()
    }

    @Test
    fun `updateModel addUser success on first try`() = runTest {
        whenever(mockUserRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(ADD_USER_OPERATION, TEST_OBJECT_ID)
        verify(mockUserRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel addUser error on first try`() = runTest {
        val expectedError = DomainError.Unknown(Exception())
        whenever(mockUserRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Error(expectedError))
        remoteUpdateInteractor.updateModel(ADD_USER_OPERATION, TEST_OBJECT_ID)
        verify(mockUserRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel updateUser success on first try`() = runTest {
        whenever(mockUserRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(UPDATE_USER_OPERATION, TEST_OBJECT_ID)
        verify(mockUserRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel updateWG success on first try`() = runTest {
        whenever(mockWGRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(UPDATE_WG_OPERATION, TEST_OBJECT_ID)
        verify(mockWGRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel addAppointment success on first try`() = runTest {
        whenever(mockAppointmentRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(ADD_APPOINTMENT_OPERATION, TEST_OBJECT_ID)
        verify(mockAppointmentRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel updateAppointment success on first try`() = runTest {
        whenever(mockAppointmentRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(UPDATE_APPOINTMENT_OPERATION, TEST_OBJECT_ID)
        verify(mockAppointmentRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel deleteAppointment success on first try`() = runTest {
        whenever(mockAppointmentRepo.deleteLocalAppointment(TEST_OBJECT_ID)).thenReturn(
            Result.Success(Unit)
        )
        remoteUpdateInteractor.updateModel(DELETE_APPOINTMENT_OPERATION, TEST_OBJECT_ID)
        verify(mockAppointmentRepo).deleteLocalAppointment(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel addTask success on first try`() = runTest {
        whenever(mockTaskRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(ADD_TASK_OPERATION, TEST_OBJECT_ID)
        verify(mockTaskRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel updateTask success on first try`() = runTest {
        whenever(mockTaskRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(UPDATE_TASK_OPERATION, TEST_OBJECT_ID)
        verify(mockTaskRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel deleteTask success on first try`() = runTest {
        whenever(mockTaskRepo.deleteLocalTask(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(DELETE_TASK_OPERATION, TEST_OBJECT_ID)
        verify(mockTaskRepo).deleteLocalTask(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel addAbsence success on first try`() = runTest {
        whenever(mockAbsenceRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(ADD_ABSENCE_OPERATION, TEST_OBJECT_ID)
        verify(mockAbsenceRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel updateAbsence success on first try`() = runTest {
        whenever(mockAbsenceRepo.fetchAndSafe(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(UPDATE_ABSENCE_OPERATION, TEST_OBJECT_ID)
        verify(mockAbsenceRepo).fetchAndSafe(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel deleteAbsence success on first try`() = runTest {
        whenever(mockAbsenceRepo.deleteLocalAbsence(TEST_OBJECT_ID)).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(DELETE_ABSENCE_OPERATION, TEST_OBJECT_ID)
        verify(mockAbsenceRepo).deleteLocalAbsence(TEST_OBJECT_ID)
    }

    @Test
    fun `updateModel initialData success on first try without removal of local user`() = runTest {
        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockInitialDataRepo.getInitialData()).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(INITIAL_DATA_OPERATION, EMPTY_OBJECT_ID)
        verify(mockUserRepo).getLocalUserId()
        verify(mockInitialDataRepo).getInitialData()
    }

    @Test
    fun `updateModel initialData error on first try without removal of local user`() = runTest {
        val expectedError = DomainError.Unknown(Exception())
        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_USER_ID))
        whenever(mockInitialDataRepo.getInitialData()).thenReturn(Result.Error(expectedError))
        remoteUpdateInteractor.updateModel(INITIAL_DATA_OPERATION, EMPTY_OBJECT_ID)
        verify(mockUserRepo).getLocalUserId()
    }

    @Test
    fun `updateModel initialData success on first try with removal of local user`() = runTest {
        whenever(mockUserRepo.getLocalUserId()).thenReturn(Result.Success(TEST_OBJECT_ID))
        whenever(mockDeleteLocalDataRepo.deleteAllWGRelatedData()).thenReturn(Result.Success(Unit))
        remoteUpdateInteractor.updateModel(INITIAL_DATA_OPERATION, TEST_OBJECT_ID)
        verify(mockUserRepo).getLocalUserId()
    }
}
