package com.wgeplant.viewmodel.wg

import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import com.wgeplant.model.interactor.wgManagement.ManageWGInteractor
import com.wgeplant.ui.navigation.Routes
import com.wgeplant.ui.wg.CreateWGViewModel
import com.wgeplant.util.MainCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.verifyNoInteractions
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class CreateWGViewModelTest {

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockManageWGInteractor: ManageWGInteractor

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var viewModel: CreateWGViewModel

    @Before
    fun setUp() {
        viewModel = CreateWGViewModel(mockManageWGInteractor)
    }

    // input changes tests

    @Test
    fun `onWGNameChanged updates wgName and clears error`() = runTest {
        viewModel.onWGNameChanged("")
        viewModel.createWG(mockNavController)
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.wgNameError)

        viewModel.onWGNameChanged("WGName")
        assertEquals("WGName", viewModel.uiState.value.wgName)
        assertNull(viewModel.uiState.value.wgNameError)
    }

    // input validation tests

    @Test
    fun `error for invalid wgName when createWG is called`() = runTest {
        viewModel.onWGNameChanged("")

        viewModel.createWG(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState
        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(CreateWGViewModel.FIELD_EMPTY, state.value.wgNameError)

        viewModel.onWGNameChanged("ThisWGNameIsWayToLongForThisField")

        viewModel.createWG(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(CreateWGViewModel.DISPLAY_NAME_TOO_LONG, state.value.wgNameError)

        viewModel.onWGNameChanged("My#WG")

        viewModel.createWG(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(CreateWGViewModel.DISPLAY_NAME_INVALID_CHARS, state.value.wgNameError)

        viewModel.onWGNameChanged("123")

        viewModel.createWG(mockNavController)
        advanceUntilIdle()

        assertFalse(state.value.isValid)
        assertFalse(viewModel.isLoading.value)
        assertEquals(CreateWGViewModel.DISPLAY_NAME_NO_LETTER, state.value.wgNameError)
    }

    // createWG tests

    @Test
    fun `createWG shows error and does not call interactor if validation fails`() = runTest {
        viewModel.onWGNameChanged("")

        viewModel.createWG(mockNavController)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isValid)
        assertEquals(CreateWGViewModel.FIX_INPUTS, viewModel.errorMessage.value)

        verifyNoInteractions(mockManageWGInteractor)
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `createWG calls interactor and navigates on success`() = runTest {
        viewModel.onWGNameChanged("WGName")

        `when`(mockManageWGInteractor.executeCreation(anyString())).thenReturn(Result.Success(Unit))

        viewModel.createWG(mockNavController)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isValid)
        assertNull(viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verify(mockManageWGInteractor).executeCreation("WGName")
        verify(mockNavController).navigate(eq(Routes.CALENDAR_GRAPH), any<NavOptionsBuilder.() -> Unit>())
    }

    @Test
    fun `createWG handles interactor error and shows error message`() = runTest {
        viewModel.onWGNameChanged("WGName")

        val domainError = DomainError.NetworkError
        `when`(mockManageWGInteractor.executeCreation(anyString())).thenReturn(Result.Error(domainError))

        viewModel.createWG(mockNavController)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isValid)
        assertEquals(domainError.message, viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verify(mockManageWGInteractor).executeCreation("WGName")
        verifyNoInteractions(mockNavController)
    }

    @Test
    fun `createWG handles unexpected exception during creation`() = runTest {
        viewModel.onWGNameChanged("WGName")

        `when`(mockManageWGInteractor.executeCreation(anyString())).thenThrow(RuntimeException("Netzwerkfehler"))

        viewModel.createWG(mockNavController)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isValid)
        assertEquals(CreateWGViewModel.UNEXPECTED_ERROR, viewModel.errorMessage.value)
        assertFalse(viewModel.isLoading.value)

        verify(mockManageWGInteractor).executeCreation("WGName")
        verifyNoInteractions(mockNavController)
    }

    // navigation tests

    @Test
    fun `navigateBack calls popBackStack on navController`() = runTest {
        viewModel.navigateBack(mockNavController)

        verify(mockNavController).popBackStack()
    }
}
