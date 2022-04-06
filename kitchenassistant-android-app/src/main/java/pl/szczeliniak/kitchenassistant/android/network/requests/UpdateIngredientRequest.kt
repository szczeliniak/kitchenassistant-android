package pl.szczeliniak.kitchenassistant.android.network.requests

data class UpdateIngredientRequest(
    val name: String,
    val quantity: String
)