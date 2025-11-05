package com.wgeplant.viewmodel.wg

import androidx.navigation.NavController
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.domain.User
import com.wgeplant.model.interactor.userManagement.ManageUserProfileInteractor
import com.wgeplant.ui.navigation.Routes
import com.wgeplant.ui.wg.ChooseWGViewModel
import com.wgeplant.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@ExperimentalCoroutinesApi
class ChooseWGViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockManageUserProfileInteractor: ManageUserProfileInteractor

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var viewModel: ChooseWGViewModel

    // loading data tests

    @Test
    fun `ViewModel initializes correctly and loads user data`() = runTest {
        val testUser = User(userId = "1", displayName = "TestName", profilePicture = "http://example.com/image.jpg")
        `when`(mockManageUserProfileInteractor.getUserData()).thenReturn(flowOf(Result.Success(testUser)))

        viewModel = ChooseWGViewModel(mockManageUserProfileInteractor)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("TestName", uiState.userDisplayName)
        assertEquals("http://example.com/image.jpg", uiState.userProfileImageUrl)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.errorMessage.value)

        verify(mockManageUserProfileInteractor).getUserData()
    }

    @Test
    fun `ViewModel handles domain error when loading user data`() = runTest {
        val domainError = DomainError.NetworkError
        `when`(mockManageUserProfileInteractor.getUserData()).thenReturn(flowOf(Result.Error(domainError)))

        viewModel = ChooseWGViewModel(mockManageUserProfileInteractor)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("", uiState.userDisplayName)
        assertNull(uiState.userProfileImageUrl)
        assertFalse(viewModel.isLoading.value)
        assertEquals(domainError.message, viewModel.errorMessage.value)

        verify(mockManageUserProfileInteractor).getUserData()
    }

    @Test
    fun `ViewModel handles unexpected exception in flow when loading user data`() = runTest {
        `when`(mockManageUserProfileInteractor.getUserData()).thenReturn(
            flow { throw RuntimeException("Netzwerkfehler") }
        )

        viewModel = ChooseWGViewModel(mockManageUserProfileInteractor)
        advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("", uiState.userDisplayName)
        assertNull(uiState.userProfileImageUrl)
        assertFalse(viewModel.isLoading.value)
        assertEquals(ChooseWGViewModel.UNEXPECTED_ERROR, viewModel.errorMessage.value)

        verify(mockManageUserProfileInteractor).getUserData()
    }

    // navigation tests

    @Test
    fun `navigateToCreateWG navigates to CreateWG`() = runTest {
        viewModel = ChooseWGViewModel(mockManageUserProfileInteractor)
        viewModel.navigateToCreateWG(mockNavController)

        verify(mockNavController).navigate(Routes.CREATE_WG)
    }

    @Test
    fun `navigateToJoinWG navigates to JoinWG`() = runTest {
        viewModel = ChooseWGViewModel(mockManageUserProfileInteractor)
        viewModel.navigateToJoinWG(mockNavController)

        verify(mockNavController).navigate(Routes.JOIN_WG)
    }

    @Test
    fun `navigateToUserProfile navigates to UserProfile`() = runTest {
        viewModel = ChooseWGViewModel(mockManageUserProfileInteractor)
        viewModel.navigateToUserProfile(mockNavController)

        verify(mockNavController).navigate(Routes.PROFILE_USER)
    }
}
