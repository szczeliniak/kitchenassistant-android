package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListItemRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddShoppingListRequest
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.SuccessResponse
import retrofit2.http.*

interface ShoppingListRepository {

    @GET("/shoppinglists")
    suspend fun findAll(
        @Query("userId") userId: Int? = null,
        @Query("archived") archived: Boolean? = null
    ): ShoppingListsResponse

    @GET("/shoppinglists/{id}")
    suspend fun findById(@Path("id") id: Int): ShoppingListResponse

    @DELETE("/shoppinglists/{id}")
    suspend fun delete(@Path("id") id: Int): SuccessResponse

    @DELETE("/shoppinglists/{id}/items/{itemId}")
    suspend fun deleteShoppingListItem(
        @Path("id") shoppingListId: Int,
        @Path("itemId") shoppingListItemId: Int
    ): SuccessResponse

    @POST("/shoppinglists")
    suspend fun add(@Body request: AddShoppingListRequest): SuccessResponse

    @POST("/shoppinglists/{id}/archived/{isArchived}")
    suspend fun archive(@Path("id") id: Int, @Path("isArchived") isArchived: Boolean): SuccessResponse

    @POST("/shoppinglists/{id}/items")
    suspend fun addShoppingListItem(@Path("id") id: Int, @Body request: AddShoppingListItemRequest): SuccessResponse

}