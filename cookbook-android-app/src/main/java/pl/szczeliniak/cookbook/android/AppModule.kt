package pl.szczeliniak.cookbook.android

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.greenrobot.eventbus.EventBus

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    fun eventBus(): EventBus {
        return EventBus.getDefault()
    }

}