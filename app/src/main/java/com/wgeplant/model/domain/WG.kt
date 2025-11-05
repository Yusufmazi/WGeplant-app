package com.wgeplant.model.domain

import com.wgeplant.common.dto.requests.WGRequestDTO

/**
 * This data class encapsulates all necessary wg data.
 * @property wgId The user's unique wg ID.
 * @property displayName The wg name displayed to the user.
 * @property invitationCode The user's wg invitation code.
 * @property profilePicture The user's wg profile picture url. It's optional.
 */
data class WG(
    val wgId: String,
    val displayName: String,
    val invitationCode: String,
    val profilePicture: String?
)

fun WG.toRequestDTO(): WGRequestDTO {
    return WGRequestDTO(
        wgId = this.wgId,
        displayName = this.displayName,
        invitationCode = this.invitationCode,
        profilePicture = this.profilePicture
    )
}
