package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddReceiptRequest(
    val name: String,
    val author: String?,
    val source: String?,
    val description: String?,
    val userId: Int,
    val categoryId: Int?,
    val tags: List<String>
)