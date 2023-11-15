package app.vibecast.data.remote.network.image

import com.google.gson.annotations.SerializedName

data class Image(
    @SerializedName("id") val id : String,
    @SerializedName("description") val description : String?,
    @SerializedName("urls") val urls : PhotoUrls,
    @SerializedName("user") val user : UnsplashUser
){
    data class PhotoUrls(
        @SerializedName("raw") val raw : String,
        @SerializedName("full") val full : String,
        @SerializedName("regular") val regular : String,
        @SerializedName("small") val small : String,
        @SerializedName("thumb") val thumb : String
    )

    data class UnsplashUser(
        @SerializedName("id") val id : String,
        @SerializedName("name") val name : String,
        @SerializedName("userName") val userName : String,
        @SerializedName("portfolio_url") val portfolioUrl : String,
    ) {
        val attributionUrl get() = "https://unsplash.com/$userName?utm_source=VibeCast&utm_medium=referral"
    }
//TODO include download url
}