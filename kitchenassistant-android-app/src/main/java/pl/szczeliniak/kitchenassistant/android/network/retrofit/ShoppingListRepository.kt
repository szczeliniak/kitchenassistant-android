package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.SuccessResponse
import retrofit2.http.*

interface ShoppingListRepository {

    @GET("/shoppinglists")
    suspend fun findAll(): ShoppingListsResponse

    @GET("/shoppinglists/{id}")
    suspend fun findById(@Path("id") id: Int): ShoppingListResponse

    @DELETE("/shoppinglists/{id}")
    suspend fun delete(@Path("id") id: Int): SuccessResponse

    @POST("/shoppinglists")
    suspend fun add(@Body request: AddShoppingListRequest): SuccessResponse

}