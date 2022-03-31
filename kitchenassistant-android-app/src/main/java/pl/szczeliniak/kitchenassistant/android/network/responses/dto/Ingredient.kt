package pl.szczeliniak.kitchenassistant.android.network.responses.dto

data class Ingredient(
    val id: Int,
    val name: String,
    val quantity: String,
    val unit: IngredientUnit
) {

    enum class IngredientUnit {
        GRAMS, KILOGRAMS, CUPS, TEE_SPOON, TABLE_SPOON, PINCH_OF
    }

}