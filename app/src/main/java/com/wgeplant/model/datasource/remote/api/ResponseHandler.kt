package com.wgeplant.model.datasource.remote.api

import com.wgeplant.common.dto.response.ErrorResponseDTO
import com.wgeplant.model.domain.DomainError
import com.wgeplant.model.domain.Result
import kotlinx.serialization.json.Json
import okio.IOException
import retrofit2.Response
import javax.inject.Inject

/**
 * This class defines generic methods to handle the responses of the server.
 * @param json for deserializing the json strings of the responses
 */
class ResponseHandler @Inject constructor(
    private val json: Json
) {

    companion object {
        private const val NO_ERROR_DTO_MESSAGE = "Es ist ein Fehler aufgetreten."
        private const val NO_DATA_FOUND_MESSAGE = "Es gibt keine aktuellen Daten."
    }

    /**
     * This method handles a general response where a response dto is expected as a result.
     * @param response the received response from the server
     */
    fun <T> handleResponse(response: Response<T>): Result<T, DomainError> {
        return try {
            if (response.isSuccessful) {
                response.body()?.let { dto ->
                    Result.Success(dto)
                } ?: Result.Error(DomainError.ServerError.UnknownServerError(NO_DATA_FOUND_MESSAGE))
            } else {
                val statusCode = response.code()
                val errorBodyString = response.errorBody()?.string()
                val parsedErrorDto = parseErrorBodyToErrorResponseDTO(errorBodyString)
                if (parsedErrorDto == null && !errorBodyString.isNullOrBlank()) {
                    Result.Error(DomainError.ServerError.UnknownServerError(NO_ERROR_DTO_MESSAGE))
                }
                val domainError: DomainError = mapToDomainError(statusCode, parsedErrorDto)
                Result.Error(domainError)
            }
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method handles a response where a unit is expected as a result for the client.
     * @param response of the server
     */
    fun handleUnitResponse(response: Response<Unit>): Result<Unit, DomainError> {
        return try {
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                val statusCode = response.code()
                val errorBodyString = response.errorBody()?.string()
                val parsedErrorDto = parseErrorBodyToErrorResponseDTO(errorBodyString)
                if (parsedErrorDto == null && !errorBodyString.isNullOrBlank()) {
                    Result.Error(DomainError.ServerError.UnknownServerError(NO_ERROR_DTO_MESSAGE))
                }
                val domainError: DomainError = mapToDomainError(statusCode, parsedErrorDto)
                Result.Error(domainError)
            }
        } catch (e: IOException) {
            Result.Error(DomainError.NetworkError)
        } catch (e: Exception) {
            Result.Error(DomainError.Unknown(e))
        }
    }

    /**
     * This method maps the error codes of the server response to domain errors.
     * @param statusCode of the server response
     * @param parsedErrorDto in case the server sends more than just the statusCode
     */
    private fun mapToDomainError(
        statusCode: Int,
        parsedErrorDto: ErrorResponseDTO?
    ) = when (statusCode) {
        400 -> DomainError.ServerError.BadRequest(parsedErrorDto?.message.toString())
        401 -> DomainError.ServerError.Unauthorized(parsedErrorDto?.message.toString())
        500 ->
            DomainError.ServerError
                .InternalServerError(parsedErrorDto?.message.toString())
        else -> {
            DomainError.ServerError
                .UnknownServerError(parsedErrorDto?.message.toString())
        }
    }

    /**
     * This method parses the ErrorBody of the server response to an ErrorResponseDTO.
     * @param errorJsonString the error body of the response
     */
    private fun parseErrorBodyToErrorResponseDTO(errorJsonString: String?): ErrorResponseDTO? {
        return try {
            errorJsonString?.let { jsonString ->
                if (jsonString.isNotBlank()) {
                    json.decodeFromString<ErrorResponseDTO>(jsonString)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}
