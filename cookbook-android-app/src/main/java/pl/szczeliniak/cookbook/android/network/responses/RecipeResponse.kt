package pl.szczeliniak.cookbook.android.network.responses

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
        val stepGroups: List<StepGroup>,
    ) : Parcelable {
        @Parcelize
        data class Category(
            val id: Int,
            val name: String
        ) : Parcelable

        @Parcelize
        data class IngredientGroup(
            val id: Int,
            val name: String?,
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
        data class StepGroup(
            val id: Int,
            val name: String?,
            val steps: List<Step>,
        ) : Parcelable {
            @Parcelize
            data class Step(
                val id: Int,
                val description: String,
            ) : Parcelable
        }
    }
}