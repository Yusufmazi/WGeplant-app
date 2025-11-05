package com.wgeplant.model.domain

const val NETWORK_ERROR = "Keine Internetverbindung. Bitte überprüfe deine Verbindung."
const val UNAUTHORIZED_ERROR = "Authentifizierung fehlgeschlagen. Bitte melde dich nochmal an."
const val NOT_FOUND_ERROR = "Die Daten konnten nicht aufgerufen werden."
const val PERSISTENCE_ERROR = "Versuche es nochmal, oder öffne die App erneut."
const val BAD_REQUEST_ERROR = "Deine Eingabe war nicht ganz richtig."
const val INTERNAL_SERVER_ERROR = "Ein unerwarteter Serverfehler ist aufgetreten."
const val UNKNOWN_SERVER_ERROR = "Ein unbekannter Serverfehler ist aufgetreten."
const val USER_NOT_FOUND_ERROR = "Deine Authentifizierung war fehlerhaft."
const val EMAIL_ALREADY_IN_USE_ERROR = "Die E-Mail-Adresse ist bereits vergeben. Denk nochmal drüber nach ;)"
const val INVALID_INPUT_DATA_ERROR = "Die E-Mail oder das Passwort sind falsch. Bitte überprüfe deine Eingaben."
const val WEAK_PASSWORD_ERROR = "Das Passwort ist zu schwach. Mach es stärker!"
const val WRONG_PASSWORD_ERROR = "Das Passwort ist falsch. Streng dich an und denk!"
const val INVALID_EMAIL_FORMAT_ERROR = "Die E-Mail-Adresse ist nicht im richtigen Format."
const val TO_MANY_REQUESTS_ERROR = "Das Konto wurde temporär deaktiviert. Das waren wohl zu viele Anfragen."
const val FCM_TOKEN_FETCH_FAILED_ERROR = "Es gab einen Fehler bei einem externen Dienst. Versuche es nochmal."
const val UNKNOWN_FIREBASE_ERROR = "Ein unbekannter Fehler von einem externen Dienst ist aufgetreten."
const val UNKNOWN_ERROR = "Ups, da ist wohl was schief gelaufen."

/**
 * This class encapsulates all possible errors that can occur.
 */
sealed class DomainError(val message: String? = null, val cause: Throwable? = null) {

    // general errors, that can originate from everywhere
    object NetworkError : DomainError(NETWORK_ERROR)
    object UnauthorizedError : DomainError(UNAUTHORIZED_ERROR)
    object NotFoundError : DomainError(NOT_FOUND_ERROR)
    object PersistenceError : DomainError(PERSISTENCE_ERROR)

    // specific errors that originate from the server
    sealed class ServerError(message: String? = null, cause: Throwable? = null) : DomainError(message, cause) {
        data class BadRequest(val details: String = BAD_REQUEST_ERROR) : ServerError(details)
        data class InternalServerError(val originalMessage: String = INTERNAL_SERVER_ERROR) : ServerError(
            originalMessage
        )
        data class Unauthorized(val originalMessage: String = UNAUTHORIZED_ERROR) : ServerError(originalMessage)
        data class UnknownServerError(val originalMessage: String = UNKNOWN_SERVER_ERROR) : ServerError(originalMessage)
    }

    // specific errors that originate from Firebase
    sealed class FirebaseError(message: String? = null, cause: Throwable? = null) : DomainError(message, cause) {
        // Firebase Authentication Errors
        object InvalidCredentials : FirebaseError(INVALID_INPUT_DATA_ERROR)
        object UserNotFound : FirebaseError(USER_NOT_FOUND_ERROR)
        object EmailAlreadyInUse : FirebaseError(EMAIL_ALREADY_IN_USE_ERROR)
        object WeakPassword : FirebaseError(WEAK_PASSWORD_ERROR)
        object WrongPassword : FirebaseError(WRONG_PASSWORD_ERROR)
        object InvalidEmailFormat : FirebaseError(INVALID_EMAIL_FORMAT_ERROR)
        object TooManyRequests : FirebaseError(TO_MANY_REQUESTS_ERROR)

        // Firebase Cloud Messaging (FCM)
        object FcmTokenFetchFailed : FirebaseError(FCM_TOKEN_FETCH_FAILED_ERROR)

        // generic Firebase error, if it doesn't fit anything else
        data class UnknownFirebaseError(val originalCode: String? = null, val originalMessage: String? = null) :
            FirebaseError(originalMessage ?: UNKNOWN_FIREBASE_ERROR, null)
    }

    // generic fallback for unknown errors
    data class Unknown(val originalThrowable: Throwable? = null) : DomainError(
        originalThrowable?.localizedMessage ?: UNKNOWN_ERROR,
        originalThrowable
    )
}
