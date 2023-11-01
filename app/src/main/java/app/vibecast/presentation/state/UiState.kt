package app.vibecast.presentation.state

sealed class UiState<T : Any> {

    data object Loading : UiState<Nothing>()

    data class Error<T : Any>(val errorMessage: String) : UiState<T>()

    data class Success<T : Any>(val data: T) : UiState<T>()

}