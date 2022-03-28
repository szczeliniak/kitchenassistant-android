package pl.szczeliniak.kitchenassistant.android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class KitchenAssistantApplication : Application() {

    init {
        Timber.plant(Timber.DebugTree())
    }

}