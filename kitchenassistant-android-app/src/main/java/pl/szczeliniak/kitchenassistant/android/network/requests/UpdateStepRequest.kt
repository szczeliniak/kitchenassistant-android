package pl.szczeliniak.kitchenassistant.android.network.requests

data class UpdateStepRequest(
    val description: String,
    val photoName: String?,
    val sequence: Int?
)