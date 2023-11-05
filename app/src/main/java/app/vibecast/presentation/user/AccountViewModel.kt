package app.vibecast.presentation.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.usecase.GetUserUseCase
import app.vibecast.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val userConverter: UserConverter,
    private val userUseCase: GetUserUseCase
) : ViewModel(){

    private val _userFlow = MutableStateFlow<UiState<UserModel>>(UiState.Loading)
    val userFlow: StateFlow<UiState<UserModel>> = _userFlow

    fun loadUser(userId: Long) {
        viewModelScope.launch {
            userUseCase.execute(GetUserUseCase.Request(userId))
                .map {
                    userConverter.convert(it)
                }
                .collect {
                    _userFlow.value = it
                }
        }
    }
}