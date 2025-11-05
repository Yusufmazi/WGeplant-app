package com.wgeplant.model.datasource.remote

import com.wgeplant.common.dto.requests.UserRequestDTO
import com.wgeplant.common.dto.response.UserResponseDTO
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result

/**
 * This interface is responsible for communicating about user data with the server.
 */
interface RemoteUserDataSource {
    /**
     * This method sends the server the request to create a new user.
     * @param user the new user
     */
    suspend fun createUserRemote(user: UserRequestDTO): Result<UserResponseDTO, DomainError>

    /**
     * This method sends the server the request to get the user of the given id.
     * @param userId the wanted user
     */
    suspend fun getUserById(userId: String): Result<UserResponseDTO, DomainError>

    /**
     * This method sends the server a request to delete all data related to the current user.
     */
    suspend fun deleteUserData(): Result<Unit, DomainError>

    /**
     * This method sends the server the request to join a wg.
     * @param code of the wg the user wants to join
     */
    suspend fun joinWG(code: String): Result<Unit, DomainError>

    /**
     * This method sends the server the request to remove the current user from the wg.
     */
    suspend fun leaveWG(): Result<Unit, DomainError>

    /**
     * This method sends the server the request to update the data of the user with the dto.
     * @param user updated data of the current user
     */
    suspend fun updateUser(user: UserRequestDTO): Result<UserResponseDTO, DomainError>
}
