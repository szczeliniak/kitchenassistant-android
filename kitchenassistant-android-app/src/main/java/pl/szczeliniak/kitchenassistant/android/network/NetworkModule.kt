package pl.szczeliniak.kitchenassistant.android.network

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.network.converters.DateTimeConverter
import pl.szczeliniak.kitchenassistant.android.network.interceptors.NetworkCheckInterceptor
import pl.szczeliniak.kitchenassistant.android.network.interceptors.NetworkConnectionChecker
import pl.szczeliniak.kitchenassistant.android.network.interceptors.TokenInterceptor
import pl.szczeliniak.kitchenassistant.android.network.retrofit.LoginRepository
import pl.szczeliniak.kitchenassistant.android.network.retrofit.ReceiptRepository
import pl.szczeliniak.kitchenassistant.android.services.LocalStorageService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    fun dateTimeConverter(): DateTimeConverter = DateTimeConverter()

    @Provides
    @Singleton
    fun gson(dateTimeConverter: DateTimeConverter): Gson {
        return GsonBuilder()
            .registerTypeAdapter(DateTime::class.java, dateTimeConverter)
            .create()
    }

    @Provides
    fun httpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return httpLoggingInterceptor
    }

    @Provides
    fun tokenInterceptor(localStorageService: LocalStorageService): TokenInterceptor =
        TokenInterceptor(localStorageService)

    @Provides
    fun httpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        tokenInterceptor: TokenInterceptor,
        networkCheckInterceptor: NetworkCheckInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(tokenInterceptor)
            .addInterceptor(networkCheckInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun retrofitBuilder(gson: Gson, client: OkHttpClient, @ApplicationContext context: Context): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl(context.getString(R.string.app_api_host))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
    }

    @Provides
    fun networkConnectionChecker(@ApplicationContext context: Context): NetworkConnectionChecker {
        return NetworkConnectionChecker(context)
    }

    @Provides
    fun NetworkCheckInterceptor(checker: Lazy<NetworkConnectionChecker>): NetworkCheckInterceptor {
        return NetworkCheckInterceptor(checker.get())
    }

    @Provides
    fun loginRepository(retrofitBuilder: Retrofit.Builder): LoginRepository {
        return retrofitBuilder.build().create(LoginRepository::class.java)
    }

    @Provides
    fun receiptRepository(retrofitBuilder: Retrofit.Builder): ReceiptRepository {
        return retrofitBuilder.build().create(ReceiptRepository::class.java)
    }

}