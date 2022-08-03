package pl.szczeliniak.kitchenassistant.android.network.requests

data class UpdateShoppingListItemRequest(
    val name: String,
    val quantity: String?,
    val sequence: Int?,
    val recipeId: Int?
)