package app.vibecast.presentation

import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ImagePicker @Inject constructor(
    private val imageRepository: ImageRepository, ) {

    fun pickImage(cityName : String, weatherCondition : String) : Flow<List<ImageDto>> {
        val searchTerm = when (weatherCondition) {
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

        val searchQuery = "$cityName $searchTerm"

         return imageRepository.getRemoteImages(searchQuery)
    }


}