package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.szczeliniak.kitchenassistant.android.network.retrofit.*

@Module
@InstallIn(SingletonComponent::class)
class ServiceModule {

    @Provides
    fun localStorageService(@ApplicationContext context: Context): LocalStorageService = LocalStorageService(context)

    @Provides
    fun userService(userRepository: UserRepository): UserService = UserService(userRepository)

    @Provides
    fun recipeService(
        recipeRepository: RecipeRepository
    ): RecipeService = RecipeService(recipeRepository)

    @Provides
    fun dayPlansService(
        dayPlanRepository: DayPlanRepository
    ): DayPlanService = DayPlanService(dayPlanRepository)

    @Provides
    fun photoService(
        photoRepository: PhotoRepository,
        @ApplicationContext context: Context
    ): PhotoService = PhotoService(photoRepository, context)

}