package pl.szczeliniak.kitchenassistant.android.network.requests

data class EditIngredientGroupRequest(
    val name: String,
    val ingredients: List<EditIngredientDto>
) {
    data class EditIngredientDto(
        val id: Int?,
        val name: String,
        val quantity: String?
    )
}