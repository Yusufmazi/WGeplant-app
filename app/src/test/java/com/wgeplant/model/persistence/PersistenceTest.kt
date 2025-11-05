package com.wgeplant.model.persistence

import androidx.compose.ui.graphics.Color
import com.wgeplant.model.domain.Appointment
import com.wgeplant.model.domain.User
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.argThat
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import java.time.LocalDateTime
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class PersistenceTest {
    private lateinit var spiedPersistence: Persistence

    private val user1 = User(userId = "user2", displayName = "ichBin3", profilePicture = null)
    private val user2 = User(userId = "user3", displayName = "ichBin1", profilePicture = null)
    private val user3 = User(userId = "user1", displayName = "ichBin2", profilePicture = null)
    private val updatedUser = User(userId = "user2", displayName = "dochNicht", profilePicture = null)
    private val initialUsers = listOf(user1, user2, user3)
    private val today = LocalDateTime.now()
    private val app1 = Appointment(
        "1",
        "Putzen",
        today,
        today,
        listOf(user1.userId),
        Color.Yellow,
        null
    )
    private val app2 = Appointment(
        "2",
        "Aufräumen",
        today,
        today,
        listOf(user1.userId, user2.userId),
        Color.Gray,
        null
    )
    private val updatedApp1 = Appointment(
        "1",
        "Putzen",
        today,
        today,
        listOf(user1.userId),
        Color.Blue,
        null
    )
    private val newApp = Appointment(
        "3",
        "Huhu",
        today,
        today,
        listOf(user1.userId, user2.userId),
        Color.Black,
        null
    )
    private val initialAppointments = listOf(app1, app2)
    private val month = YearMonth.now()

    @Before
    fun setUp() {
        val persistence = Persistence
        spiedPersistence = spy(persistence)
    }

    @Test
    fun `updateUserInWG updates existing user and leaves others unchanged`() = runTest {
        spiedPersistence.saveUsersInWG(initialUsers)

        spiedPersistence.updateUserInWG(updatedUser)

        val finalUsers = spiedPersistence.usersInWG.first()
        assertEquals(3, finalUsers.size)
        assertTrue(finalUsers.contains(updatedUser))
        assertFalse(finalUsers.contains(user1))
        assertTrue(finalUsers.contains(user2))
        assertTrue(finalUsers.contains(user3))
        verify(spiedPersistence).saveUsersInWG(
            argThat { users ->
                users.size == 3 &&
                    users.contains(updatedUser) &&
                    users.contains(user2) &&
                    users.contains(user3) &&
                    !users.contains(user1)
            }
        )
    }

    @Test
    fun `saveAppointment updates an existing appointment and leaves others`() = runTest {
        spiedPersistence.saveUserAppointments(initialAppointments)

        spiedPersistence.saveAppointment(updatedApp1)

        val finalAppointments = spiedPersistence.userAppointments.first()
        assertEquals(2, finalAppointments.size)
        assertTrue(finalAppointments.contains(updatedApp1))
        assertFalse(finalAppointments.contains(app1))
        assertTrue(finalAppointments.contains(app2))
    }

    @Test
    fun `saveAppointment saves a new appointment and leaves others`() = runTest {
        spiedPersistence.saveUserAppointments(initialAppointments)

        spiedPersistence.saveAppointment(newApp)

        val finalAppointments = spiedPersistence.userAppointments.first()
        assertEquals(3, finalAppointments.size)
        assertTrue(finalAppointments.contains(newApp))
        assertTrue(finalAppointments.contains(app1))
        assertTrue(finalAppointments.contains(app2))
    }

    @Test
    fun `getMonthlyAppointments returns the appointments of the given month`() = runTest {
        spiedPersistence.saveUserAppointments(initialAppointments)

        val collectedAppointments = mutableListOf<List<Appointment>>()

        val result = launch {
            spiedPersistence.getMonthlyAppointments(month).collect {
                collectedAppointments.add(it)
            }
        }
        advanceUntilIdle()

        assertEquals(1, collectedAppointments.size)
        val foundList = collectedAppointments.first()
        assertEquals(2, foundList.size)
        assertTrue(foundList.contains(app1))
        assertTrue(foundList.contains(app2))

        result.cancel()
    }
}
