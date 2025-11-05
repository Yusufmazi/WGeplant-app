package com.wgeplant.model.datasource.remote.api

import com.wgeplant.common.dto.requests.AbsenceRequestDTO
import com.wgeplant.common.dto.requests.AppointmentRequestDTO
import com.wgeplant.common.dto.requests.TaskRequestDTO
import com.wgeplant.common.dto.requests.UserRequestDTO
import com.wgeplant.common.dto.requests.WGRequestDTO
import com.wgeplant.common.dto.response.AbsenceResponseDTO
import com.wgeplant.common.dto.response.AppointmentResponseDTO
import com.wgeplant.common.dto.response.InitialResponseDTO
import com.wgeplant.common.dto.response.TaskResponseDTO
import com.wgeplant.common.dto.response.UserResponseDTO
import com.wgeplant.common.dto.response.WGResponseDTO
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * This interface defines all possible paths, methods and responses retrofit can
 * expect when communicating with the server.
 */
interface ApiService {
    /**
     * This method sends the server the request to create a new user.
     * @param user the new user
     */
    @POST("user")
    suspend fun createUserRemote(@Body user: UserRequestDTO): Response<UserResponseDTO>

    /**
     * This method sends the server the request to get the user of the given id.
     * @param userId the wanted user
     */
    @GET("user/{userId}")
    suspend fun getUserById(@Path("userId") userId: String): Response<UserResponseDTO>

    /**
     * This method sends the server a request to delete all data related to the current user.
     */
    @DELETE("user")
    suspend fun deleteUserData(): Response<Unit>

    /**
     * This method sends the server the request to join a wg.
     * @param code of the wg the user wants to join
     */
    @PATCH("wg/{code}")
    suspend fun joinWG(@Path("code") code: String): Response<Unit>

    /**
     * This method sends the server the request to remove the current user from the wg.
     */
    @PATCH("user")
    suspend fun leaveWG(): Response<Unit>

    /**
     * This method sends the server the request to update the data of the user with the dto.
     * @param user updated data of the current user
     */
    @PUT("user")
    suspend fun updateUser(@Body user: UserRequestDTO): Response<UserResponseDTO>

    /**
     * This method sends the server the request to create a new wg.
     * @param wg the new wg
     */
    @POST("wg")
    suspend fun createWGRemote(@Body wg: WGRequestDTO): Response<WGResponseDTO>

    /**
     * This method sends the server the request to get the wg of the id.
     * @param wgId of the wanted wg
     */
    @GET("wg/{wgId}")
    suspend fun getWGById(@Path("wgId") wgId: String): Response<WGResponseDTO>

    /**
     * This method sends the server the request to update the data of the given wg.
     * @param wg the updated data of the wg
     */
    @PATCH("wg")
    suspend fun updateWG(@Body wg: WGRequestDTO): Response<WGResponseDTO>

    /**
     * This method creates a new appointment on the server.
     * @param appointment the new appointment
     */
    @POST("appointment")
    suspend fun createAppointment(@Body appointment: AppointmentRequestDTO):
        Response<AppointmentResponseDTO>

    /**
     * This method updates the data of an existing appointment on the server.
     * @param appointment the updated appointment
     */
    @PATCH("appointment")
    suspend fun updateAppointment(@Body appointment: AppointmentRequestDTO):
        Response<AppointmentResponseDTO>

    /**
     * This method gets the appointment of the id from the server.
     * @param appointmentId of the appointment
     */
    @GET("appointment/{appointmentId}")
    suspend fun getAppointmentById(@Path("appointmentId") appointmentId: String):
        Response<AppointmentResponseDTO>

    /**
     * This method deletes an appointment from the server.
     * @param appointmentId of the appointment
     */
    @DELETE("appointment/{appointmentId}")
    suspend fun deleteAppointment(@Path("appointmentId") appointmentId: String):
        Response<Unit>

    /**
     * This method creates a new task on the server.
     * @param task the new task
     */
    @POST("task")
    suspend fun createTask(@Body task: TaskRequestDTO): Response<TaskResponseDTO>

    /**
     * This method updates an existing task on the server.
     * @param task the updated data
     */
    @PATCH("task")
    suspend fun updateTask(@Body task: TaskRequestDTO): Response<TaskResponseDTO>

    /**
     * This method gets a specific task from the server.
     * @param taskId of the task
     */
    @GET("task/{taskId}")
    suspend fun getTaskById(@Path("taskId") taskId: String): Response<TaskResponseDTO>

    /**
     * This method deletes a task on the server.
     * @param taskId of the task
     */
    @DELETE("task/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: String): Response<Unit>

    /**
     * This method creates a new absence on the server.
     * @param absence the new absence
     */
    @POST("absence")
    suspend fun createAbsence(@Body absence: AbsenceRequestDTO): Response<AbsenceResponseDTO>

    /**
     * This method updates an existing absence on the server.
     * @param absence updated absence
     */
    @PATCH("absence")
    suspend fun updateAbsence(@Body absence: AbsenceRequestDTO): Response<AbsenceResponseDTO>

    /**
     * This method fetches a specific absence from the server.
     * @param absenceId of the absence
     */
    @GET("absence/{absenceId}")
    suspend fun getAbsenceById(@Path("absenceId") absenceId: String): Response<AbsenceResponseDTO>

    /**
     * This method deletes an absence from the server.
     * @param absenceId of the absence
     */
    @DELETE("absence/{absenceId}")
    suspend fun deleteAbsence(@Path("absenceId") absenceId: String): Response<Unit>

    /**
     * This method sends a request to the server, to add the given device token of the current user.
     * @param deviceToken of the current user
     */
    @POST("device/{deviceToken}")
    suspend fun addDeviceToken(@Path("deviceToken") deviceToken: String): Response<Unit>

    /**
     * This method sends the request to the server, to delete the device token of the current user.
     */
    @DELETE("device")
    suspend fun deleteDeviceToken(): Response<Unit>

    /**
     * This method sends the request to the server to get all the initial data of the current user.
     */
    @GET("initial")
    suspend fun getInitialData(): Response<InitialResponseDTO>

    /**
     * This method sends the request to the server to remove the given user from the wg.
     * @param userId of the removed user
     */
    @GET("initial/{userId}")
    suspend fun removeUserFromWG(@Path("userId") userId: String): Response<InitialResponseDTO>
}
