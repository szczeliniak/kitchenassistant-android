package pl.szczeliniak.kitchenassistant.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class ShoppingListsResponse(
    val shoppingLists: Page<ShoppingList>,
) {
    @Parcelize
    data class ShoppingList(
        val id: Int,
        val name: String,
        val description: String?,
        val date: LocalDate?
    ) : Parcelable
}