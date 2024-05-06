package pl.szczeliniak.cookbook.android.network.requests

import java.time.LocalDate

data class AddRecipeToDayPlanRequest(
    val date: LocalDate,
    val recipeId: Int
)