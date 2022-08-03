package pl.szczeliniak.kitchenassistant.android.network.responses

import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Pagination
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Recipe

data class RecipesResponse(
    val recipes: List<Recipe>,
    val pagination: Pagination
)