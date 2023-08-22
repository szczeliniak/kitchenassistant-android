package pl.szczeliniak.kitchenassistant.android.network.requests

data class AddIngredientGroupRequest(
    val name: String,
    val ingredients: List<AddIngredientDto>
) {
    data class AddIngredientDto(
        val name: String,
        val quantity: String?
    )
}