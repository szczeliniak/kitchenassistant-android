package pl.szczeliniak.cookbook.android.services

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.szczeliniak.cookbook.android.network.retrofit.DayPlanRepository
import pl.szczeliniak.cookbook.android.network.retrofit.RecipeRepository
import pl.szczeliniak.cookbook.android.network.retrofit.UserRepository

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

}