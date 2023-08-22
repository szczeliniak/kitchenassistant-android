package pl.szczeliniak.kitchenassistant.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class IngredientGroupResponse(
    val ingredientGroup: IngredientGroup
) {
    @Parcelize
    data class IngredientGroup(
        val id: Int,
        val name: String,
        val ingredients: List<Ingredient>,
    ) : Parcelable {
        @Parcelize
        data class Ingredient(
            val id: Int,
            val name: String,
            val quantity: String?
        ) : Parcelable
    }
}