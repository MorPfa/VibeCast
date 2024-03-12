package app.vibecast.presentation.util

class GenreFormatter {

    fun formatQuery(genre: String): String {
        val formattedQuery = when (genre) {
            "R&B" -> "rnb"
            "Hip-Hop" -> "hiphop"
            "Jazz" -> "jazz"
            "Rock" -> "rock"
            "Classical" -> "classical"
            "Blues" -> "blues"
            "Country" -> "country"
            "Metal" -> "metal"
            "Lo-fi" -> "chill"
            "Ambient" -> "ambient"
            "Electronic" -> "techno"
            "Punk" -> "punk"
            "Funk" -> "funk"
            "House" -> "house"
            "Soul" -> "soul"
            else -> genre
        }
        return formattedQuery
    }
}