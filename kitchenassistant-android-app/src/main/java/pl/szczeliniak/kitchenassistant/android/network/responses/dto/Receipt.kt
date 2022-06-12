package pl.szczeliniak.kitchenassistant.android.network.responses.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Receipt(
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
    val photos: MutableList<Photo>
) : Parcelable