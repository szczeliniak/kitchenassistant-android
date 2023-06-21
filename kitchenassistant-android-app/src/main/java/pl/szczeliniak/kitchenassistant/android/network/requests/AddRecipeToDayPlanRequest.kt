package pl.szczeliniak.kitchenassistant.android.network.requests

import java.time.LocalDate

data class AddRecipeToDayPlanRequest(
    val userId: Int,
    val date: LocalDate,
    val recipeId: Int
)