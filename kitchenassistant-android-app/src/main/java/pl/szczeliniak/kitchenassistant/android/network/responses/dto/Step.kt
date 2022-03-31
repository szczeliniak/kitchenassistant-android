package pl.szczeliniak.kitchenassistant.android.network.responses.dto

data class Step(
    val id: Int,
    val title: String,
    val description: String?,
    val sequence: Int?,
)