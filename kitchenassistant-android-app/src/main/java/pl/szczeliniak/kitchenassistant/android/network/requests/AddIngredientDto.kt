package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddIngredientDto(
    val name: String,
    val quantity: String?
)