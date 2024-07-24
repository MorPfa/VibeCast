package app.vibecast.presentation.util

enum class SpotifyScopes(val scope: List<String>) {
    STREAMING(listOf("streaming")),
    APP_REMOTE(listOf("app-remote-control")),
    READ_CUR_PLAYLIST(listOf("user-read-currently-playing")),
    READ_PRIVATE(
        listOf(
            "user-read-private",
            "playlist-read-private",
            "playlist-read-collaborative"
        )
    ),
    READ_PUBLIC(
        listOf(
            "playlist-modify-private",
            "playlist-modify-public"
        )
    );

    companion object {
        fun getScopes(): Array<String> {
            return entries.flatMap { it.scope }.toTypedArray()
        }
    }

}