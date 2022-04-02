package pl.szczeliniak.kitchenassistant.android.network.requests

data class UpdateReceiptRequest(
    val name: String,
    val author: String?,
    val url: String?,
    val description: String?,
    val userId: Int
)