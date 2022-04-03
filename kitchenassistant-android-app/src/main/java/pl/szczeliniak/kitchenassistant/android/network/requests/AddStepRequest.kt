package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddStepRequest(
    val name: String,
    val description: String?,
    val sequence: Int?
)