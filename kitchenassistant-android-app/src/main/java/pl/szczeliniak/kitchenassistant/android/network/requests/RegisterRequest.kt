package pl.szczeliniak.kitchenassistant.android.network.requests

data class RegisterRequest(
    val email: String,
    val name: String,
    val password: String,
    val passwordRepeated: String
)