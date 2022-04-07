package pl.szczeliniak.kitchenassistant.android.network.responses

import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category

data class CategoriesResponse(
    val categories: List<Category>
)