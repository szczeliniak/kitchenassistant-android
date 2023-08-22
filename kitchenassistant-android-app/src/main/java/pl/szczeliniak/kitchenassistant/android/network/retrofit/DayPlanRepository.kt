package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.AddRecipeToDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateDayPlanRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlanResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.DayPlansResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.SuccessResponse
import retrofit2.http.*
import java.time.LocalDate

interface DayPlanRepository {

    @GET("/dayplans")
    suspend fun findAll(
        @Query("userId") userId: Int? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("since") since: LocalDate? = null,
        @Query("to") to: LocalDate? = null
    ): DayPlansResponse

    @GET("/dayplans/{date}")
    suspend fun findById(@Path("date") date: LocalDate): DayPlanResponse

    @DELETE("/dayplans/{date}")
    suspend fun delete(@Path("date") date: LocalDate): SuccessResponse

    @DELETE("/dayplans/{date}/recipes/{recipeId}")
    suspend fun deassignRecipe(@Path("date") date: LocalDate, @Path("recipeId") recipeId: Int): SuccessResponse

    @POST("/dayplans")
    suspend fun assignRecipe(@Body request: AddRecipeToDayPlanRequest): SuccessResponse

    @PUT("/dayplans/{date}")
    suspend fun update(@Path("date") date: LocalDate, @Body request: UpdateDayPlanRequest): SuccessResponse

}