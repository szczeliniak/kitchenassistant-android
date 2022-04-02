package pl.szczeliniak.kitchenassistant.android.network.requests.dto

data class NewStep(
    val title: String,
    val description: String?,
    val sequence: Int?
)