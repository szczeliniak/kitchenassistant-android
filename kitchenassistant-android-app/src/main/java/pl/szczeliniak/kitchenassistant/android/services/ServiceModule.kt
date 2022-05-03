package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.szczeliniak.kitchenassistant.android.network.retrofit.FileRepository
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
    fun fileService(
        fileRepository: FileRepository,
        @ApplicationContext context: Context,
        localStorageService: LocalStorageService
    ): FileService = FileService(fileRepository, context, localStorageService)

    @Provides
    fun receiptService(
        receiptRepository: ReceiptRepository,
        localStorageService: LocalStorageService
    ): ReceiptService = ReceiptService(receiptRepository, localStorageService)

    @Provides
    fun shoppingListService(
        shoppingListRepository: ShoppingListRepository,
        localStorageService: LocalStorageService
    ): ShoppingListService = ShoppingListService(shoppingListRepository, localStorageService)

}