package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.szczeliniak.kitchenassistant.android.network.retrofit.LoginRepository
import pl.szczeliniak.kitchenassistant.android.network.retrofit.ReceiptRepository
import pl.szczeliniak.kitchenassistant.android.network.retrofit.RegisterRepository
import pl.szczeliniak.kitchenassistant.android.network.retrofit.ShoppingListRepository

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    @Provides
    fun localStorageService(@ApplicationContext context: Context): LocalStorageService = LocalStorageService(context)

    @Provides
    fun loginService(loginRepository: LoginRepository): LoginService = LoginService(loginRepository)

    @Provides
    fun registerService(registerRepository: RegisterRepository): RegisterService = RegisterService(registerRepository)

    @Provides
    fun receiptService(receiptRepository: ReceiptRepository, localStorageService: LocalStorageService): ReceiptService =
        ReceiptService(receiptRepository, localStorageService)

    @Provides
    fun shoppingListService(
        shoppingListRepository: ShoppingListRepository,
        localStorageService: LocalStorageService
    ): ShoppingListService =
        ShoppingListService(shoppingListRepository, localStorageService)

}