package app.vibecast.data.local_data.db.weather.util

import androidx.room.TypeConverter
import app.vibecast.domain.model.WeatherDto
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromWeather(weather: WeatherDto?): String? {
        return weather?.let {
            gson.toJson(weather)
        }
    }

    @TypeConverter
    fun toWeather(weatherJson: String?): WeatherDto? {
        return weatherJson?.let {
            gson.fromJson(weatherJson, object : TypeToken<WeatherDto>() {}.type)
        }
    }
}