package app.vibecast.data.remote.network.image

import com.google.gson.annotations.SerializedName

data class ImageApiModel(
   @SerializedName("id") val id : String,
   @SerializedName("description") val description : String?,
   @SerializedName("alt_description") val altDescription : String?,
   @SerializedName("urls") val urls : PhotoUrls,
   @SerializedName("user") val user : UnsplashUser,
   @SerializedName("links") val links : PhotoLinks
) {
   data class PhotoUrls(
      @SerializedName("full") val full: String,
      @SerializedName("regular") val regular: String,
      @SerializedName("small") val small: String,
      @SerializedName("thumb") val thumb: String
   )

   data class PhotoLinks(
      @SerializedName("self") val user: String,
      @SerializedName("download_location") val downloadLink: String
   )

   data class UnsplashUser(
      @SerializedName("id") val id: String,
      @SerializedName("username") val userName: String,
      @SerializedName("name") val name: String,
      @SerializedName("portfolio_url") val portfolioUrl: String?,
   ) {
      val attributionUrl get() = "https://unsplash.com/$userName?utm_source=VibeCast&utm_medium=referral"
   }
}
