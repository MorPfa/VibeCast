package app.vibecast.presentation.screens.main_screen.music.util

import app.vibecast.BuildConfig

object Constants {
    const val CLIENT_ID = BuildConfig.SPOTIFY_KEY
    const val REDIRECT_URI = "vibecast://callback"
    const val REQUEST_CODE = 1337
}