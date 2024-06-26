package pl.szczeliniak.cookbook.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class CategoriesResponse(
    val categories: List<Category>
) {
    @Parcelize
    data class Category(
        val id: Int,
        val name: String
    ) : Parcelable
}