package pl.szczeliniak.kitchenassistant.android.network.responses

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

data class DayPlansResponse(
    val dayPlans: Page<DayPlan>
) {
    @Parcelize
    data class DayPlan(
        val date: LocalDate,
    ) : Parcelable
}