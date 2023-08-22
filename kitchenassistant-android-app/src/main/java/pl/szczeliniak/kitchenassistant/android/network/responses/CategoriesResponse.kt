package pl.szczeliniak.kitchenassistant.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CategoriesResponse(
    val categories: List<Category>
) {
    @Parcelize
    data class Category(
        val id: Int,
        val name: String,
        val sequence: Int?
    ) : Parcelable
}