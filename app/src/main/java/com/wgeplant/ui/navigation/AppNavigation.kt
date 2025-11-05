package com.wgeplant.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wgeplant.ui.auth.LoginScreen
import com.wgeplant.ui.auth.LoginViewModel
import com.wgeplant.ui.auth.RegisterScreen
import com.wgeplant.ui.auth.RegisterViewModel
import com.wgeplant.ui.auth.StartScreen
import com.wgeplant.ui.auth.StartViewModel
import com.wgeplant.ui.calendar.CalendarViewModel
import com.wgeplant.ui.calendar.DailyCalendarScreen
import com.wgeplant.ui.calendar.MonthlyCalendarScreen
import com.wgeplant.ui.calendar.entry.AppointmentScreen
import com.wgeplant.ui.calendar.entry.AppointmentViewModel
import com.wgeplant.ui.calendar.entry.CreateAppointmentScreen
import com.wgeplant.ui.calendar.entry.CreateTaskScreen
import com.wgeplant.ui.calendar.entry.TaskScreen
import com.wgeplant.ui.calendar.entry.TaskViewModel
import com.wgeplant.ui.toDo.ToDoScreen
import com.wgeplant.ui.toDo.ToDoViewModel
import com.wgeplant.ui.user.UserProfileScreen
import com.wgeplant.ui.user.UserProfileViewModel
import com.wgeplant.ui.wg.ChooseWGScreen
import com.wgeplant.ui.wg.ChooseWGViewModel
import com.wgeplant.ui.wg.CreateWGScreen
import com.wgeplant.ui.wg.CreateWGViewModel
import com.wgeplant.ui.wg.JoinWGScreen
import com.wgeplant.ui.wg.JoinWGViewModel
import com.wgeplant.ui.wg.WGProfileScreen
import com.wgeplant.ui.wg.WGProfileViewModel

/**
 * Object holding all route constants used for navigation within the app.
 *
 * Routes define unique string identifiers for each screen or navigation destination,
 * including paths with arguments for dynamic routes.
 */
object Routes {
    const val AUTH_START = "auth_start_view"
    const val LOGIN = "login_view"
    const val REGISTER = "register_view"
    const val CHOOSE_WG = "choose_wg_view"
    const val JOIN_WG = "join_wg_view"
    const val CREATE_WG = "create_wg_view"
    const val CALENDAR_MONTH = "calendar_month_view"
    const val CALENDAR_DAY = "calendar_day_view"
    const val CALENDAR_GRAPH = "calendar_graph"
    const val TODO = "todo_view"
    const val CREATE_APPOINTMENT = "create_appointment_view"
    const val CREATE_TASK = "create_task_view"
    const val APPOINTMENT_ID_ARG = "appointmentId"
    const val APPOINTMENT = "appointment_view/{$APPOINTMENT_ID_ARG}"
    const val TASK_ID_ARG = "taskId"
    const val TASK = "task_view/{$TASK_ID_ARG}"
    const val EDIT_TASK = "edit_task_view/{$TASK_ID_ARG}"
    const val PROFILE_USER = "user_profile_view"
    const val PROFILE_WG = "wg_profile_view"
    const val NETWORK_ERROR = "network_error_view/{previousRoute}"
    const val PREVIOUS_ROUTE = "previousRoute"
    const val APP_START_ROUTE = "app_start_view"

    /**
     * Returns a concrete route string for navigating to a specific appointment by ID.
     *
     * @param appointmentId The ID of the appointment to navigate to.
     * @return The formatted route string for the appointment detail screen.
     */
    fun getAppointmentRoute(appointmentId: String): String {
        return "appointment_view/$appointmentId"
    }

    /**
     * Returns a concrete route string for navigating to a specific task by ID.
     *
     * @param taskId The ID of the task to navigate to.
     * @return The formatted route string for the task detail screen.
     */
    fun getTaskRoute(taskId: String): String {
        return "task_view/$taskId"
    }
}

/**
 * Composable function that sets up the navigation graph for the app.
 *
 * It manages navigation between authentication screens, WG setup,
 * calendar views, to-do lists, appointments, tasks, and profile screens based on
 * the user's login and membership state.
 *
 * Navigation destinations are registered with their corresponding ViewModels via Hilt.
 * Also includes a nested navigation graph for calendar-related screens.
 *
 * @param initialLoginState Nullable Boolean indicating if the user is initially logged in.
 * @param initialMemberState Nullable Boolean indicating if the user is initially a member of a WG.
 * @param loginState Nullable Boolean indicating if the user is initially logged in.
 *                          `true` means logged in, `false` or `null` means not logged in.
 * @param memberState Nullable Boolean indicating if the user is a member of a WG.
 *                    `true` means member, `false` means not a member, `null` means unknown.
 * @param modifier Optional Compose Modifier to apply to the NavHost container.
 * @param navController NavHostController responsible for navigation actions.
 */
@Composable
fun AppNavigation(
    initialLoginState: Boolean?,
    initialMemberState: Boolean?,
    loginState: Boolean?,
    memberState: Boolean?,
    networkState: Boolean?,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    LaunchedEffect(loginState, memberState, networkState) {
        val currentRoute = navController.currentDestination?.route.orEmpty()

        if (currentRoute == Routes.NETWORK_ERROR) {
            return@LaunchedEffect
        }

        if (networkState == false) {
            navController.navigate("network_error_view/$currentRoute") {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else if (loginState != true) {
            navController.navigate(Routes.AUTH_START) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else if (memberState == true) {
            navController.navigate(Routes.CALENDAR_GRAPH) {
                popUpTo(navController.graph.findStartDestination().id) {
                    inclusive = true
                }
                launchSingleTop = true
            }
        } else if (memberState == false) {
            if (currentRoute != Routes.LOGIN) {
                navController.navigate(Routes.CHOOSE_WG) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.APP_START_ROUTE,
        modifier = modifier
    ) {
        composable(Routes.AUTH_START) {
            val startViewModel: StartViewModel = hiltViewModel()
            StartScreen(navController, startViewModel)
        }

        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            LoginScreen(navController, loginViewModel)
        }

        composable(Routes.REGISTER) {
            val registerViewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(navController, registerViewModel)
        }

        composable(Routes.CHOOSE_WG) {
            val chooseWGViewModel: ChooseWGViewModel = hiltViewModel()
            ChooseWGScreen(navController, chooseWGViewModel)
        }

        composable(Routes.JOIN_WG) {
            val joinWGViewModel: JoinWGViewModel = hiltViewModel()
            JoinWGScreen(navController, joinWGViewModel)
        }

        composable(Routes.CREATE_WG) {
            val createWGViewModel: CreateWGViewModel = hiltViewModel()
            CreateWGScreen(navController, createWGViewModel)
        }

        composable(Routes.TODO) {
            val todoViewModel: ToDoViewModel = hiltViewModel()
            ToDoScreen(navController, todoViewModel)
        }

        composable(Routes.CREATE_APPOINTMENT) {
            val createAppointmentViewModel: AppointmentViewModel = hiltViewModel()
            CreateAppointmentScreen(navController, createAppointmentViewModel)
        }

        composable(
            route = Routes.APPOINTMENT,
            arguments = listOf(navArgument(Routes.APPOINTMENT_ID_ARG) { type = NavType.StringType })
        ) {
            val appointmentViewModel: AppointmentViewModel = hiltViewModel()
            AppointmentScreen(navController, appointmentViewModel)
        }

        composable(Routes.CREATE_TASK) {
            val taskViewModel: TaskViewModel = hiltViewModel()
            CreateTaskScreen(navController, taskViewModel)
        }

        composable(
            route = Routes.TASK,
            arguments = listOf(navArgument(Routes.TASK_ID_ARG) { type = NavType.StringType })
        ) {
            val taskViewModel: TaskViewModel = hiltViewModel()
            TaskScreen(navController, taskViewModel)
        }

        composable(
            route = Routes.EDIT_TASK,
            arguments = listOf(navArgument(Routes.TASK_ID_ARG) { type = NavType.StringType })
        ) {
            val taskViewModel: TaskViewModel = hiltViewModel()
            CreateTaskScreen(navController, taskViewModel)
        }

        composable(Routes.PROFILE_USER) {
            val userProfileViewModel: UserProfileViewModel = hiltViewModel()
            UserProfileScreen(navController, userProfileViewModel)
        }

        composable(Routes.PROFILE_WG) {
            val wgProfileViewModel: WGProfileViewModel = hiltViewModel()
            WGProfileScreen(navController, wgProfileViewModel)
        }

        composable(
            route = Routes.NETWORK_ERROR,
            arguments = listOf(
                navArgument(Routes.PREVIOUS_ROUTE) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val previousRoute = backStackEntry.arguments?.getString(Routes.PREVIOUS_ROUTE)
            val networkErrorViewModel: NetworkErrorViewModel = hiltViewModel()
            NetworkErrorScreen(navController, networkErrorViewModel, networkState, previousRoute)
        }

        composable(Routes.APP_START_ROUTE) {
            AppStartScreen(navController, initialLoginState, initialMemberState)
        }

        calendarGraph(navController)
    }
}

/**
 * Adds the nested calendar navigation graph to the provided NavGraphBuilder.
 *
 * This graph contains the monthly and daily calendar screens, sharing the same
 * CalendarViewModel instance scoped to the calendar graph's back stack entry.
 *
 * @param navController NavHostController used for navigation within the calendar graph.
 */
fun NavGraphBuilder.calendarGraph(navController: NavHostController) {
    navigation(
        startDestination = Routes.CALENDAR_MONTH,
        route = Routes.CALENDAR_GRAPH
    ) {
        composable(Routes.CALENDAR_MONTH) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.CALENDAR_GRAPH)
            }
            val calendarViewModel: CalendarViewModel = hiltViewModel(parentEntry)
            MonthlyCalendarScreen(navController, calendarViewModel)
        }
        composable(Routes.CALENDAR_DAY) { backStackEntry ->
            val parentEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Routes.CALENDAR_GRAPH)
            }
            val calendarViewModel: CalendarViewModel = hiltViewModel(parentEntry)
            DailyCalendarScreen(navController, calendarViewModel)
        }
    }
}
