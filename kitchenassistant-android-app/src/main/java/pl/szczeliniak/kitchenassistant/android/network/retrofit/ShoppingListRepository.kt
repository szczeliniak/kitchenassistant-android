package pl.szczeliniak.kitchenassistant.android.network.retrofit

import pl.szczeliniak.kitchenassistant.android.network.responses.ShoppingListsResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.SuccessResponse
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Path

interface ShoppingListRepository {

    @GET("/shoppinglists")
    suspend fun findAll(): ShoppingListsResponse

    @DELETE("/shoppinglists/{id}")
    suspend fun delete(@Path("id") id: Int): SuccessResponse

}