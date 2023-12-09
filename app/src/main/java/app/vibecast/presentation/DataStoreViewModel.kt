package app.vibecast.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.data.data_repository.repository.Unit
import app.vibecast.domain.repository.DataStoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataStoreViewModel
@Inject constructor(
    private val dataStoreRepository: DataStoreRepository) : ViewModel() {


    fun storeUnit(unit: Unit) = viewModelScope.launch {
        dataStoreRepository.putUnit(unit)
    }
    fun getUnit(): Flow<Unit?> = flow {
        val unit = dataStoreRepository.getUnit()
        emit(unit)
    }


    fun clearUnit() {
        viewModelScope.launch {
            dataStoreRepository.clearUnit()
        }
    }

}