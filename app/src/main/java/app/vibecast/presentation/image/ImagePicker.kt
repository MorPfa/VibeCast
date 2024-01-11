package app.vibecast.presentation.image

import android.util.Log
import app.vibecast.R
import app.vibecast.data.TAGS.IMAGE_ERROR
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class ImagePicker @Inject constructor(
    private val imageRepository: ImageRepository) {

    fun pickDefaultImage(weatherCondition: String) : Int{
        val randomIndex = (0..2).random()
        val snowList = listOf(
            R.drawable.snow_image_1,
            R.drawable.snow_image_2,
            R.drawable.snow_image_3
        )
        val rainList = listOf(
            R.drawable.rainy_image_1,
            R.drawable.rainy_image_2,
            R.drawable.rainy_image_3
        )
        val sunList = listOf(
            R.drawable.sunny_image_1,
            R.drawable.sunny_image_2,
            R.drawable.sunny_image_3
        )
        val stormList = listOf(
            R.drawable.storm_image_1,
            R.drawable.storm_image_3,
            R.drawable.storm_image_2
        )
        val fogList = listOf(
            R.drawable.fog_image_1,
            R.drawable.fog_image_2,
            R.drawable.fog_image_3
        )
        val image = when(weatherCondition){
            "Clear" -> sunList[randomIndex]
            "Clouds" -> rainList[randomIndex]
            "Drizzle", "Rain" -> rainList[randomIndex]
            "Thunderstorm" -> stormList[randomIndex]
            "Mist", "Haze", "Ash" -> fogList[randomIndex]
            "Snow" -> snowList[randomIndex]
            "Fog" -> fogList[randomIndex]
            "Smoke" -> fogList[randomIndex]
            "Dust", "Sand" -> sunList[randomIndex]
            "Squall" -> stormList[randomIndex]
            "Tornado" -> stormList[randomIndex]
            else -> sunList[0]
        }
        return image
    }

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
