package pl.szczeliniak.kitchenassistant.android.network.requests

data class UpdateReceiptRequest(
    val name: String,
    val author: String?,
    val source: String?,
    val description: String?,
    val categoryId: Int?,
    val tags: List<String>,
    val photos: List<Int>
)