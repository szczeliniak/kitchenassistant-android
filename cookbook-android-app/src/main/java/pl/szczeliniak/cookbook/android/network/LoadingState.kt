package pl.szczeliniak.cookbook.android.network

sealed class LoadingState<out R> {

    data class Success<out T>(val data: T) : LoadingState<T>()

    data class Exception(val exception: java.lang.Exception) : LoadingState<Nothing>()

    data class HttpException(val exception: retrofit2.HttpException) : LoadingState<Nothing>()

    object InProgress : LoadingState<Nothing>()

    object NoInternetException : LoadingState<Nothing>()

}
