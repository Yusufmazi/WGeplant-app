package com.wgeplant.model.domain

sealed class Result<out T, out E> {
    /**
     * Represents a successful completion of the operation.
     * @param data The concrete data given back, if the operation was successful.
     * The error type is nothing since the operation was successful.
     */
    data class Success<out T>(val data: T) : Result<T, Nothing>()

    /**
     * Represents an Error that occurred during the unsuccessfully operation.
     * @param error THe specific error that occurred.
     * The data type is nothing since the operation was not successful.
     */
    data class Error<out E>(val error: E) : Result<Nothing, E>()
}
