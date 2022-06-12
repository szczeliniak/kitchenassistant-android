package pl.szczeliniak.kitchenassistant.android.ui.dialogs.addeditingredient

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import pl.szczeliniak.kitchenassistant.android.network.LoadingState
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientGroupRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.AddIngredientRequest
import pl.szczeliniak.kitchenassistant.android.network.requests.UpdateIngredientRequest
import pl.szczeliniak.kitchenassistant.android.services.ReceiptService
import javax.inject.Inject

@HiltViewModel
class AddEditIngredientDialogViewModel @Inject constructor(
    private val receiptService: ReceiptService,
) : ViewModel() {

    fun addIngredient(
        receiptId: Int,
        ingredientGroupId: Int,
        request: AddIngredientRequest
    ): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            receiptService.addIngredient(receiptId, ingredientGroupId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun updateIngredient(
        receiptId: Int,
        ingredientGroupId: Int,
        ingredientId: Int,
        request: UpdateIngredientRequest
    ): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            receiptService.updateIngredient(receiptId, ingredientGroupId, ingredientId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

    fun addIngredientGroup(receiptId: Int, request: AddIngredientGroupRequest): LiveData<LoadingState<Int>> {
        val liveData = MutableLiveData<LoadingState<Int>>()
        viewModelScope.launch {
            receiptService.addIngredientGroup(receiptId, request)
                .onEach { liveData.value = it }
                .launchIn(viewModelScope)
        }
        return liveData
    }

}