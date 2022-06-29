package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddShoppingListItemRequest(
    val name: String,
    val quantity: String?,
    val sequence: Int?,
    val receiptId: Int?
)