package pl.szczeliniak.kitchenassistant.android.ui.utils

import kotlinx.coroutines.*

class DebounceExecutor(
    private val debounceMillis: Long
) {

    private var searchQueryJob: Job? = null

    fun execute(runnable: Runnable) {
        searchQueryJob?.cancel()
        searchQueryJob = CoroutineScope(Dispatchers.Main).launch {
            delay(debounceMillis)
            runnable.run()
        }
    }

    fun interface Runnable {
        fun run()
    }

}