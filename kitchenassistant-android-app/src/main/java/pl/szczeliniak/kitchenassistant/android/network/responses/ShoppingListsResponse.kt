package pl.szczeliniak.kitchenassistant.android.network.responses

import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Pagination
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.ShoppingList

data class ShoppingListsResponse(
    val shoppingLists: List<ShoppingList>,
    val pagination: Pagination
)