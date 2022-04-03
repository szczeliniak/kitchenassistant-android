package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.SuccessResponse
import retrofit2.http.*

interface ShoppingListRepository {

    @GET("/shoppinglists")
    suspend fun findAll(): ShoppingListsResponse

    @DELETE("/shoppinglists/{id}")
    suspend fun delete(@Path("id") id: Int): SuccessResponse

    @POST("/shoppinglists")
    suspend fun add(@Body request: AddShoppingListRequest): SuccessResponse

}