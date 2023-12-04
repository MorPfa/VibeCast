package app.vibecast.presentation

sealed class LocationPermissionState {
    data object Granted : LocationPermissionState()
    data object Denied : LocationPermissionState()
    data object RequestPermission : LocationPermissionState()
}
