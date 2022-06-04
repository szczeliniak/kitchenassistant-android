package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.szczeliniak.kitchenassistant.android.network.retrofit.ReceiptRepository
import pl.szczeliniak.kitchenassistant.android.network.retrofit.ShoppingListRepository
import pl.szczeliniak.kitchenassistant.android.network.retrofit.UserRepository

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    @Provides
    fun localStorageService(@ApplicationContext context: Context): LocalStorageService = LocalStorageService(context)

    @Provides
    fun userService(userRepository: UserRepository): UserService = UserService(userRepository)

    @Provides
    fun receiptService(
        receiptRepository: ReceiptRepository,
        localStorageService: LocalStorageService,
        @ApplicationContext context: Context,
    ): ReceiptService = ReceiptService(receiptRepository, localStorageService, context)

    @Provides
    fun shoppingListService(
        shoppingListRepository: ShoppingListRepository,
        localStorageService: LocalStorageService
    ): ShoppingListService = ShoppingListService(shoppingListRepository, localStorageService)

}