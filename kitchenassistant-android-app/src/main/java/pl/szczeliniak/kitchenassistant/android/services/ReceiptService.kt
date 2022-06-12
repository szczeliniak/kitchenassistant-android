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
import pl.szczeliniak.kitchenassistant.android.network.requests.*
import pl.szczeliniak.kitchenassistant.android.network.responses.ReceiptsResponse
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Category
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Photo
import pl.szczeliniak.kitchenassistant.android.network.responses.dto.Receipt
import pl.szczeliniak.kitchenassistant.android.network.retrofit.ReceiptRepository
import java.io.File
import java.io.InputStream

class ReceiptService constructor(
    private val receiptRepository: ReceiptRepository,
    private val localStorageService: LocalStorageService,
    private val context: Context,
) {

    suspend fun findAll(
        categoryId: Int?,
        receiptName: String?,
        tag: String?,
        page: Int?,
        limit: Int?
    ): Flow<LoadingState<ReceiptsResponse>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        receiptRepository.findAll(
                            localStorageService.getId(),
                            categoryId,
                            receiptName,
                            tag, page, limit
                        )
                    )
                )
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findById(receiptId: Int): Flow<LoadingState<Receipt>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.findById(receiptId).receipt))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun delete(id: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.delete(id).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun deleteIngredient(receiptId: Int, ingredientGroupId: Int, ingredientId: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        receiptRepository.deleteIngredient(
                            receiptId,
                            ingredientGroupId,
                            ingredientId
                        ).id
                    )
                )
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun deleteStep(receiptId: Int, stepId: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.deleteStep(receiptId, stepId).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun add(request: AddReceiptRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.add(request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun update(receiptId: Int, request: UpdateReceiptRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.update(receiptId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun addIngredient(
        receiptId: Int,
        ingredientGroupId: Int,
        request: AddIngredientRequest
    ): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.addIngredient(receiptId, ingredientGroupId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun updateIngredient(
        receiptId: Int,
        ingredientGroupId: Int,
        ingredientId: Int,
        request: UpdateIngredientRequest
    ): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(
                    LoadingState.Success(
                        receiptRepository.updateIngredient(
                            receiptId,
                            ingredientGroupId,
                            ingredientId,
                            request
                        ).id
                    )
                )
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun addStep(receiptId: Int, request: AddStepRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.addStep(receiptId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun updateStep(receiptId: Int, stepId: Int, request: UpdateStepRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.updateStep(receiptId, stepId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findAllCategories(): Flow<LoadingState<List<Category>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.findAllCategories(localStorageService.getId()).categories))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findAllTags(): Flow<LoadingState<List<String>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.findAllTags(localStorageService.getId()).tags))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun findAllAuthors(): Flow<LoadingState<List<String>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.findAllAuthors(localStorageService.getId()).authors))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun deleteCategory(id: Int): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.deleteCategory(id).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun addCategory(request: AddCategoryRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.addCategory(request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun updateCategory(categoryId: Int, request: UpdateCategoryRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.updateCategory(categoryId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    fun setFavorite(id: Int, isFavorite: Boolean): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.setFavorite(id, isFavorite).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun downloadPhoto(photo: Photo): Flow<LoadingState<DownloadedPhoto>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                val cachedPhoto = getFromCache(photo.name)
                if (cachedPhoto != null) {
                    emit(LoadingState.Success(DownloadedPhoto(photo.id, cachedPhoto)))
                } else {
                    val inputStream = receiptRepository.downloadPhoto(photo.id).body()?.byteStream()
                    if (inputStream == null) {
                        emit(LoadingState.Exception(KitchenAssistantException("Cannot load photo.")))
                    } else {
                        emit(LoadingState.Success(DownloadedPhoto(photo.id, saveInCache(inputStream, photo.name))))
                    }
                }
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    suspend fun uploadPhoto(files: List<File>): Flow<LoadingState<List<Int>>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                val ids = files.map {
                    val filePart = MultipartBody.Part.createFormData(
                        "file",
                        it.name,
                        it.asRequestBody("image/*".toMediaTypeOrNull())
                    )
                    receiptRepository.uploadPhoto(localStorageService.getId(), filePart).id
                }

                emit(LoadingState.Success(ids))
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

    fun addIngredientGroup(receiptId: Int, request: AddIngredientGroupRequest): Flow<LoadingState<Int>> {
        return flow {
            emit(LoadingState.InProgress)
            try {
                emit(LoadingState.Success(receiptRepository.addIngredientGroup(receiptId, request).id))
            } catch (e: KitchenAssistantNetworkException) {
                emit(LoadingState.NoInternetException)
            } catch (e: Exception) {
                emit(LoadingState.Exception(e))
            }
        }
    }

    data class DownloadedPhoto(val fileId: Int, val file: File)

}