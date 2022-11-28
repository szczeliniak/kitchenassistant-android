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
        @Query("archived") archived: Boolean? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
        @Query("since") since: LocalDate? = null,
        @Query("to") to: LocalDate? = null
    ): DayPlansResponse

    @GET("/dayplans/{id}")
    suspend fun findById(@Path("id") id: Int): DayPlanResponse

    @DELETE("/dayplans/{id}")
    suspend fun delete(@Path("id") id: Int): SuccessResponse

    @PUT("/dayplans/{id}/archive/{archive}")
    suspend fun archive(@Path("id") id: Int, @Path("archive") archive: Boolean): SuccessResponse

    @DELETE("/dayplans/{id}/recipes/{recipeId}")
    suspend fun deassignRecipe(@Path("id") dayPlanId: Int, @Path("recipeId") recipeId: Int): SuccessResponse

    @POST("/dayplans/recipes/{recipeId}")
    suspend fun assignRecipe(@Path("recipeId") recipeId: Int, @Body request: AddRecipeToDayPlanRequest): SuccessResponse

    @PUT("/dayplans/{id}")
    suspend fun update(@Path("id") id: Int, @Body request: UpdateDayPlanRequest): SuccessResponse

}