package pl.szczeliniak.kitchenassistant.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class ShoppingListResponse(
    val shoppingList: ShoppingList
) {
    @Parcelize
    data class ShoppingList(
        val id: Int,
        val name: String,
        val description: String?,
        val date: LocalDate?,
        val items: List<Item>,
    ) : Parcelable {
        @Parcelize
        data class Item(
            val id: Int,
            val name: String,
            val completed: Boolean,
            val quantity: String?,
            val sequence: String,
            val recipe: Recipe?
        ) : Parcelable {
            @Parcelize
            data class Recipe(
                val id: Int,
                val name: String
            ) : Parcelable
        }
    }
}