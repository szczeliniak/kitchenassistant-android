package pl.szczeliniak.kitchenassistant.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class DayPlanResponse(
    val dayPlan: DayPlan
) {
    @Parcelize
    data class DayPlan(
        val date: LocalDate,
        val recipes: List<Recipe>,
    ) : Parcelable {
        @Parcelize
        data class Recipe(
            val id: Int,
            val name: String,
            val author: String?,
            val category: String?,
            val ingredientGroups: List<IngredientGroup>,
        ) : Parcelable {
            @Parcelize
            data class IngredientGroup(
                val name: String,
                val ingredients: List<Ingredient>
            ) : Parcelable {
                @Parcelize
                data class Ingredient(
                    val name: String,
                    val quantity: String?,
                ) : Parcelable
            }
        }
    }
}