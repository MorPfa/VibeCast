package app.vibecast.domain.entity



data class ImageDto(
    val id : String,
    val description : String?,
    val altDescription : String?,
    val urls : PhotoUrls,
    val user : UnsplashUser,
    val links : PhotoLinks
){
    data class PhotoUrls(
        val raw : String,
        val full : String,
        val regular : String,
        val small : String,
        val thumb : String
    )

    data class PhotoLinks(
        val user : String,
        val downloadLink : String
    )

    data class UnsplashUser(
        val id : String,
        val name : String,
        val userName : String,
        val portfolioUrl : String?,
    ) {
        val attributionUrl get() = "https://unsplash.com/$userName?utm_source=VibeCast&utm_medium=referral"
    }

}