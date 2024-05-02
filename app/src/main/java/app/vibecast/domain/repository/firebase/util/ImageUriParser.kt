package app.vibecast.domain.repository.firebase.util


import com.spotify.protocol.types.ImageUri
import timber.log.Timber

object ImageUriParser {

    fun stripImageUri(uri: ImageUri): String {
        val uriString = uri.toString()
        val startIndex = uriString.indexOf("spotify:image:") + "spotify:image:".length
        val endIndex = uriString.indexOf("'", startIndex)
        return if (startIndex >= 0 && endIndex >= 0) {
            Timber.tag("imageParser").d(uriString.substring(startIndex, endIndex))
            uriString.substring(startIndex, endIndex)
        } else {
            ""
        }
    }


}