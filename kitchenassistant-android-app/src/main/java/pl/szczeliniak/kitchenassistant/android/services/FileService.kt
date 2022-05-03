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
import pl.szczeliniak.kitchenassistant.android.network.retrofit.FileRepository
import java.io.File
import java.io.InputStream

class FileService constructor(
    private val fileRepository: FileRepository,
    private val context: Context,
    private val localStorageService: LocalStorageService
) {

    suspend fun download(id: Int): Flow<LoadingState<File>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                val file = getFromCache(id)
                if (file != null) {
                    emit(LoadingState.Success(file))
                } else {
                    val inputStream = fileRepository.download(id).body()?.byteStream()
                    if (inputStream == null) {
                        emit(LoadingState.Exception(KitchenAssistantException("Cannot load photo.")))
                    } else {
                        emit(LoadingState.Success(saveInCache(inputStream, id)))
                    }
                }
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun upload(files: List<File>): Flow<LoadingState<List<Int>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                val ids = files.map {
                    val filePart = MultipartBody.Part.createFormData(
                        "file",
                        it.name,
                        it.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                    fileRepository.upload(localStorageService.getId(), filePart).id
                }

                emit(LoadingState.Success(ids))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    private fun getFromCache(id: Int): File? {
        val file = getFile(id)
        return if (file.exists()) file else null
    }

    private fun getFile(id: Int): File {
        return File(context.cacheDir, "file-${id}")
    }

    private fun saveInCache(inputStream: InputStream, id: Int): File {
        val file = getFile(id)
        file.outputStream().use { inputStream.copyTo(it) }
        return file
    }

}