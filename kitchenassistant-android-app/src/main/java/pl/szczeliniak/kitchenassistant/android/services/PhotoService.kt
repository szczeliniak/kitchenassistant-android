package pl.szczeliniak.kitchenassistant.android.services

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import pl.szczeliniak.kitchenassistant.android.exceptions.KitchenAssistantException
import pl.szczeliniak.kitchenassistant.android.exceptions.KitchenAssistantNetworkException
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.retrofit.PhotoRepository
import java.io.File
import java.io.InputStream

class PhotoService(
    private val photoRepository: PhotoRepository,
    private val context: Context,
) {

    suspend fun downloadPhoto(photoName: String): Flow<LoadingState<DownloadedPhoto>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                getFromCache(photoName)?.let {
                    emit(LoadingState.Success(DownloadedPhoto(photoName, it)))
                } ?: run {
                    val inputStream = photoRepository.downloadPhoto(photoName).body()?.byteStream()
                    if (inputStream == null) {
                        emit(LoadingState.Exception(KitchenAssistantException("Cannot load photo.")))
                    } else {
                        emit(LoadingState.Success(DownloadedPhoto(photoName, saveInCache(inputStream, photoName))))
                    }
                }
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun uploadPhoto(file: File): Flow<LoadingState<String>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                val filePart = MultipartBody.Part.createFormData(
                    "file",
                    file.name,
                    file.asRequestBody("image/*".toMediaTypeOrNull())
                )
                emit(LoadingState.Success(photoRepository.uploadPhoto(filePart).name))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    private fun getFromCache(name: String): File? {
        val file = getFile(name)
        return if (file.exists()) file else null
    }

    private fun getFile(name: String): File {
        return File(context.cacheDir, "file-${name}")
    }

    private fun saveInCache(inputStream: InputStream, name: String): File {
        val file = getFile(name)
        file.outputStream().use { inputStream.copyTo(it) }
        return file
    }

    data class DownloadedPhoto(val photoName: String, val file: File)

}