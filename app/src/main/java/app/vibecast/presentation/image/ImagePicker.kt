package app.vibecast.presentation.image

import android.util.Log
import app.vibecast.data.TAGS.IMAGE_ERROR
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ImagePicker @Inject constructor(
    private val imageRepository: ImageRepository) {

    fun pickImage(cityName : String, weatherCondition : String) : Flow<ImageDto> {
        val weather = when (weatherCondition) {
            "Clear" -> "clear"
            "Clouds" -> "cloudy"
            "Drizzle", "Rain" -> "rainy"
            "Thunderstorm" -> "thunderstorm"
            "Mist", "Haze", "Ash" -> "foggy"
            "Snow" -> "snow"
            "Fog" -> "foggy"
            "Smoke" -> "smokey"
            "Dust", "Sand" -> "sand"
            "Squall" -> "squall"
            "Tornado" -> "tornado"
            else -> weatherCondition
        }

        val searchQuery = "$weather $cityName"

         return try {
             imageRepository.getRemoteImages(searchQuery)
         }
         catch(e : Exception) {
             Log.e(IMAGE_ERROR, "Error fetching remote images: $e")
             throw e
         }.flowOn(Dispatchers.IO)
         }
    }
