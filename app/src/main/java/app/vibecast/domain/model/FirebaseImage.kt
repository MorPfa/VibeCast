package app.vibecast.domain.model



data class FirebaseImage (
    val imageId : String ="",
    val imageUrl : String ="",
    val timestamp: Long? = null,
    val userLink : String = "",
    val downloadUrl: String = "",
    val userName: String = "",
    val userRealName: String = "",
    val portfolioUrl: String = "",

)