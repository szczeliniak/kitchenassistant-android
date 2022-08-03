package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.szczeliniak.kitchenassistant.android.network.retrofit.DayPlanRepository
import pl.szczeliniak.kitchenassistant.android.network.retrofit.RecipeRepository
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
    fun recipeService(
        recipeRepository: RecipeRepository,
        localStorageService: LocalStorageService,
        @ApplicationContext context: Context,
    ): RecipeService = RecipeService(recipeRepository, localStorageService, context)

    @Provides
    fun shoppingListService(
        shoppingListRepository: ShoppingListRepository,
        localStorageService: LocalStorageService
    ): ShoppingListService = ShoppingListService(shoppingListRepository, localStorageService)

    @Provides
    fun dayPlansService(
        dayPlanRepository: DayPlanRepository,
        localStorageService: LocalStorageService
    ): DayPlanService = DayPlanService(dayPlanRepository, localStorageService)

}