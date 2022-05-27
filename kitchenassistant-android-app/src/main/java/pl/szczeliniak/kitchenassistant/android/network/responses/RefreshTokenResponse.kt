package pl.szczeliniak.kitchenassistant.android.network.responses

import java.time.ZonedDateTime

data class RefreshTokenResponse(
    val token: String,
    val validTo: ZonedDateTime
)