package com.wgeplant.model.interactor.calendarManagement

import androidx.compose.ui.graphics.Color
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.repository.AppointmentRepo
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import kotlin.test.assertEquals

class ManageAppointmentInteractorImplTest {
    companion object {
        private const val TEST_APPOINTMENT_ID = "testId"
        private const val TEST_TITLE = "Test Title"
        private const val TEST_DESCRIPTION = "Test Description"
        private val TEST_AFFECTED_USERS = listOf("userId1", "userId2")
        private val TEST_COLOR = mock<Color>()
        private val TEST_START_DATE = LocalDateTime.now()
        private val TEST_END_DATE = LocalDateTime.now().plusDays(1)
    }
    private lateinit var mockAppointmentRepo: AppointmentRepo
    private lateinit var manageAppointmentInteractor: ManageAppointmentInteractorImpl

    @Before
    fun setUp() {
        // initialize mocks
        mockAppointmentRepo = mock()

        // initialize the classes with mocks
        manageAppointmentInteractor = ManageAppointmentInteractorImpl(
            appointmentRepo = mockAppointmentRepo
        )
    }

    @Test
    fun `executeCreation success`() = runTest {
        val input = CreateAppointmentInput(
            title = TEST_TITLE,
            startDate = TEST_START_DATE,
            endDate = TEST_END_DATE,
            affectedUsers = TEST_AFFECTED_USERS,
            color = TEST_COLOR,
            description = TEST_DESCRIPTION
        )
        val appointment = manageAppointmentInteractor.createAppointmentObject(input, null)
        val expectedResult = Result.Success(Unit)
        whenever(mockAppointmentRepo.createAppointment(appointment)).thenReturn(expectedResult)
        val result = manageAppointmentInteractor.executeCreation(input)
        assertEquals(expectedResult, result)
        verify(mockAppointmentRepo).createAppointment(appointment)
    }

    @Test
    fun `executeCreation failure`() = runTest {
        val input = CreateAppointmentInput(
            title = TEST_TITLE,
            startDate = TEST_START_DATE,
            endDate = TEST_END_DATE,
            affectedUsers = TEST_AFFECTED_USERS,
            color = TEST_COLOR,
            description = TEST_DESCRIPTION
        )
        val appointment = manageAppointmentInteractor.createAppointmentObject(input, null)
        val expectedError = Result.Error(DomainError.Unknown(Exception()))
        whenever(mockAppointmentRepo.createAppointment(appointment)).thenReturn(expectedError)
        val result = manageAppointmentInteractor.executeCreation(input)
        assertEquals(expectedError, result)
        verify(mockAppointmentRepo).createAppointment(appointment)
    }

    @Test
    fun `executeEditing success`() = runTest {
        val appointmentId = TEST_APPOINTMENT_ID
        val edit = CreateAppointmentInput(
            title = TEST_TITLE,
            startDate = TEST_START_DATE,
            endDate = TEST_END_DATE,
            affectedUsers = TEST_AFFECTED_USERS,
            color = TEST_COLOR,
            description = TEST_DESCRIPTION
        )
        val appointment = Appointment(
            appointmentId = TEST_APPOINTMENT_ID,
            title = TEST_TITLE,
            startDate = TEST_START_DATE,
            endDate = TEST_END_DATE,
            affectedUsers = TEST_AFFECTED_USERS,
            color = TEST_COLOR,
            description = TEST_DESCRIPTION
        )
        val expectedResult = Result.Success(Unit)
        whenever(mockAppointmentRepo.updateAppointment(appointment)).thenReturn(expectedResult)
        val result = manageAppointmentInteractor.executeEditing(appointmentId, edit)
        assertEquals(expectedResult, result)
        verify(mockAppointmentRepo).updateAppointment(appointment)
    }

    @Test
    fun `executeDeletion success`() = runTest {
        val appointmentId = TEST_APPOINTMENT_ID
        val expectedResult = Result.Success(Unit)
        whenever(mockAppointmentRepo.deleteAppointment(appointmentId)).thenReturn(expectedResult)
        val result = manageAppointmentInteractor.executeDeletion(appointmentId)
        assertEquals(expectedResult, result)
        verify(mockAppointmentRepo).deleteAppointment(appointmentId)
    }
}
