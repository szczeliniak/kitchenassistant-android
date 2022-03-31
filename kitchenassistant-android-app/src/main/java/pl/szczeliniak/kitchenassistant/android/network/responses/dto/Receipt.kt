package pl.szczeliniak.kitchenassistant.android.network.responses.dto

data class Receipt(
    val id: Int,
    val name: String,
    val author: String?,
    val description: String?,
    val source: String?,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
)