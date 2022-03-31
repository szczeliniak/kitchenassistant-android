package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.szczeliniak.kitchenassistant.android.network.retrofit.LoginRepository
import pl.szczeliniak.kitchenassistant.android.network.retrofit.ReceiptRepository

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    @Provides
    fun localStorageService(@ApplicationContext context: Context): LocalStorageService = LocalStorageService(context)

    @Provides
    fun loginService(loginRepository: LoginRepository, localStorageService: LocalStorageService): LoginService =
        LoginService(loginRepository, localStorageService)

    @Provides
    fun receiptService(receiptRepository: ReceiptRepository): ReceiptService = ReceiptService(receiptRepository)

}