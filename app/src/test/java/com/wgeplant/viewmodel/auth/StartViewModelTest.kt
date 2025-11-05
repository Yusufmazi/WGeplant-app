package com.wgeplant.viewmodel.auth

import androidx.navigation.NavController
import com.wgeplant.ui.auth.StartViewModel
import com.wgeplant.ui.navigation.Routes
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule

class StartViewModelTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var mockNavController: NavController

    private lateinit var viewModel: StartViewModel

    @Before
    fun setUp() {
        viewModel = StartViewModel()
    }

    @Test
    fun `navigateToLogin navigates to login`() {
        viewModel.navigateToLogin(mockNavController)

        verify(mockNavController).navigate(Routes.LOGIN)
    }

    @Test
    fun `navigateToRegister navigates to register`() {
        viewModel.navigateToRegister(mockNavController)

        verify(mockNavController).navigate(Routes.REGISTER)
    }
}
