package pl.szczeliniak.kitchenassistant.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class RecipesResponse(
    val recipes: Page<Recipe>,
) {
    @Parcelize
    data class Recipe(
        val id: Int,
        val name: String,
        val author: String?,
        val favorite: Boolean,
        val category: Category?
    ) : Parcelable {
        @Parcelize
        data class Category(
            val id: Int,
            val name: String
        ) : Parcelable
    }
}