package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.UserRequestDTO
import com.wgeplant.common.dto.response.UserResponseDTO
import com.wgeplant.model.datasource.remote.api.ApiService
import com.wgeplant.model.datasource.remote.api.ResponseHandler
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import okio.IOException
import javax.inject.Inject

/**
 * This class is responsible for communicating with the server about the user data.
 * @param apiService to send a request to the server
 * @param responseHandler to process the server response
 */
class RemoteUserDataSourceImpl @Inject constructor(
    private val apiService: ApiService,
    private val responseHandler: ResponseHandler
) : RemoteUserDataSource {

    /**
     * This method sends the server the request to create a new user.
     * @param user the new user
     */
    override suspend fun createUserRemote(
        user: UserRequestDTO
    ): Result<UserResponseDTO, DomainError> {
        return try {
            val response = apiService.createUserRemote(user)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends the server the request to get the user of the given id.
     * @param userId the wanted user
     */
    override suspend fun getUserById(userId: String): Result<UserResponseDTO, DomainError> {
        return try {
            val response = apiService.getUserById(userId)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends the server a request to delete all data related to the current user.
     */
    override suspend fun deleteUserData(): Result<Unit, DomainError> {
        return try {
            val response = apiService.deleteUserData()
            return responseHandler.handleUnitResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends the server the request to join a wg.
     * @param code of the wg the user wants to join
     */
    override suspend fun joinWG(code: String): Result<Unit, DomainError> {
        return try {
            val response = apiService.joinWG(code)
            return responseHandler.handleUnitResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends the server the request to remove the current user from the wg.
     */
    override suspend fun leaveWG(): Result<Unit, DomainError> {
        return try {
            val response = apiService.leaveWG()
            return responseHandler.handleUnitResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method sends the server the request to update the data of the user with the dto.
     * @param user updated data of the current user
     */
    override suspend fun updateUser(user: UserRequestDTO): Result<UserResponseDTO, DomainError> {
        return try {
            val response = apiService.updateUser(user)
            return responseHandler.handleResponse(response)
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }
}
