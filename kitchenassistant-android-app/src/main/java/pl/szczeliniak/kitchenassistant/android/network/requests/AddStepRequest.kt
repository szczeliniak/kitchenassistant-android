package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddStepRequest(
    val title: String,
    val description: String?,
    val sequence: Int?
)