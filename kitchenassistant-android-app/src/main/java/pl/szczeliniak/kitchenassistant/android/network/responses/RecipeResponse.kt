package pl.szczeliniak.kitchenassistant.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class RecipeResponse(
    val recipe: Recipe
) {
    @Parcelize
    data class Recipe(
        val id: Int,
        val name: String,
        val author: String?,
        val description: String?,
        val source: String?,
        val favorite: Boolean,
        val category: Category?,
        val ingredientGroups: List<IngredientGroup>,
        val steps: List<Step>,
        val tags: List<String>,
        val photoName: String?
    ) : Parcelable {
        @Parcelize
        data class Category(
            val id: Int,
            val name: String,
            val sequence: Int?
        ) : Parcelable

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

        @Parcelize
        data class Step(
            val id: Int,
            val description: String,
            val sequence: Int?,
            val photoName: String?
        ) : Parcelable
    }
}