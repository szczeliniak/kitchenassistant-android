package pl.szczeliniak.kitchenassistant.android.network.responses

import java.time.ZonedDateTime

data class LoginResponse(
    val id: Int,
    val token: String,
    val email: String?,
    val validTo: ZonedDateTime
)