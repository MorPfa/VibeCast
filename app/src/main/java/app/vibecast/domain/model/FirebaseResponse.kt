package app.vibecast.domain.model

data class FirebaseResponse<T>(
    var data: List<T>? = null,
    var exception: Exception? = null
)