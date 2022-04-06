package pl.szczeliniak.kitchenassistant.android.network

import android.content.Context
import pl.szczeliniak.kitchenassistant.android.R
import pl.szczeliniak.kitchenassistant.android.ui.utils.toast
import retrofit2.HttpException
import timber.log.Timber

class LoadingStateHandler<T>(
    private val context: Context,
    private val onStateChanged: OnStateChanged<T>? = null,
) {

    fun handle(state: LoadingState<T>) {
        when (state) {
            is LoadingState.InProgress -> {
                onStateChanged?.onInProgress()
            }
            is LoadingState.Success -> {
                onStateChanged?.onFinish()
                onStateChanged?.onSuccess(state.data)
            }
            is LoadingState.Exception -> {
                context.toast(R.string.toast_cannot_execute_operation)
                handleError(state.exception)
            }
            is LoadingState.NoInternetException -> {
                context.toast(R.string.toast_no_internet_connection)
                onStateChanged?.onFinish()
                onStateChanged?.onNoInternetException()
            }
            is LoadingState.HttpException -> {
                onStateChanged?.onHttpException(state.exception)
                handleError(state.exception)
            }
        }
    }

    private fun handleError(exception: Exception) {
        Timber.e(exception)
        onStateChanged?.onFinish()
        onStateChanged?.onException(exception)
    }

    interface OnStateChanged<T> {

        fun onInProgress() {}

        fun onSuccess(data: T) {}

        fun onException(th: Throwable) {}

        fun onNoInternetException() {}

        fun onFinish() {}

        fun onHttpException(exception: HttpException) {
            onException(exception)
        }

    }

}