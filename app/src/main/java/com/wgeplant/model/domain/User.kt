package com.wgeplant.model.domain

import com.wgeplant.common.dto.requests.UserRequestDTO

/**
 * This data class encapsulates all necessary user data.
 * @property userId The user's unique ID.
 * @property displayName The name displayed to the user.
 * @property profilePicture The user's profile picture url. It's optional.
 */
data class User(
    val userId: String,
    val displayName: String,
    val profilePicture: String? = null
)

fun User.toRequestDto(): UserRequestDTO {
    return UserRequestDTO(
        userId = this.userId,
        displayName = this.displayName,
        profilePicture = this.profilePicture
    )
}
