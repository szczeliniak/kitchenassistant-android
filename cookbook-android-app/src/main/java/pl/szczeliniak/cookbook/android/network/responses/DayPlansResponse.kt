package pl.szczeliniak.cookbook.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class DayPlansResponse(
    val dayPlans: Page<DayPlan>
) {
    @Parcelize
    data class DayPlan(
        val date: LocalDate,
        val recipes: List<Recipe>
    ) : Parcelable {
        @Parcelize
        data class Recipe(val id: Int, val name: String) : Parcelable
    }
}