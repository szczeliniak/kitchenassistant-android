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
    val category: Category?,
    val ingredients: List<Ingredient>,
    val steps: List<Step>,
    val tags: List<String>
) : Parcelable