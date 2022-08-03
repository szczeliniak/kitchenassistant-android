package pl.szczeliniak.kitchenassistant.android.network.responses

import pl.szczeliniak.kitchenassistant.android.network.responses.dto.DayPlanRecipe

data class DayPlanRecipesResponse(
    val recipes: List<DayPlanRecipe>
)