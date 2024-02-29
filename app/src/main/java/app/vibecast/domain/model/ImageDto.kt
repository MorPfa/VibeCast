package app.vibecast.domain.model


import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ImageDto(
    val id: String,
    val description: String?,
    val altDescription: String?,
    val urls: PhotoUrls,
    val user: UnsplashUser,
    val links: PhotoLinks,
    val timestamp : Long?
) : Parcelable {

    @Parcelize
    data class PhotoUrls(
        val full: String,
        val regular: String,
        val small: String,
        val thumb: String
    ) : Parcelable

    @Parcelize
    data class PhotoLinks(
        val user: String,
        val downloadLink: String
    ) : Parcelable

    @Parcelize
    data class UnsplashUser(
        val id: String,
        val name: String,
        val userName: String,
        val portfolioUrl: String?,
    ) : Parcelable {
        val attributionUrl get() = "https://unsplash.com/$userName?utm_source=VibeCast&utm_medium=referral"
    }
}
