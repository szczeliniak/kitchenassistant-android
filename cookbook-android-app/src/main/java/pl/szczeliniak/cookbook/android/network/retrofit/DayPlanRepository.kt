package pl.szczeliniak.cookbook.android.network.retrofit

import pl.szczeliniak.cookbook.android.network.requests.AddRecipeToDayPlanRequest
import pl.szczeliniak.cookbook.android.network.responses.DayPlanResponse
import pl.szczeliniak.cookbook.android.network.responses.DayPlansResponse
import pl.szczeliniak.cookbook.android.network.responses.SuccessResponse
import retrofit2.http.*
import java.time.LocalDate

interface DayPlanRepository {

    @GET("/dayplans")
    suspend fun findAll(
        @Query("page") page: Long? = null,
        @Query("limit") limit: Int? = null,
        @Query("since") since: LocalDate? = null,
        @Query("to") to: LocalDate? = null,
        @Query("sort") sort: Sort? = null
    ): DayPlansResponse

    @GET("/dayplans/{date}")
    suspend fun findByDate(@Path("date") date: LocalDate): DayPlanResponse

    @DELETE("/dayplans/{date}/recipes/{recipeId}")
    suspend fun deleteRecipe(@Path("date") date: LocalDate, @Path("recipeId") recipeId: Int): SuccessResponse

    @POST("/dayplans")
    suspend fun addRecipe(@Body request: AddRecipeToDayPlanRequest): SuccessResponse

    @PUT("/dayplans/{date}/recipes/{recipeId}/ingredientGroups/{ingredientGroupId}/ingredients/{ingredientId}/{isChecked}")
    suspend fun changeIngredientState(
        @Path("date") date: LocalDate,
        @Path("recipeId") recipeId: Int,
        @Path("ingredientGroupId") ingredientGroupId: Int,
        @Path("ingredientId") ingredientId: Int,
        @Path("isChecked") isChecked: Boolean
    ): SuccessResponse

    enum class Sort {
        ASC, DESC
    }

}