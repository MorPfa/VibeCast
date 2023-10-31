package app.vibecast.data.local.db.weather

import androidx.room.TypeConverter
import app.vibecast.domain.entity.Weather
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WeatherTypeConverter {

    private val gson = Gson()

    @TypeConverter
    fun fromWeather(weather: Weather?): String? {
        return weather?.let {
            gson.toJson(weather)
        }
    }

    @TypeConverter
    fun toWeather(weatherJson: String?): Weather? {
        return weatherJson?.let {
            gson.fromJson(weatherJson, object : TypeToken<Weather>() {}.type)
        }
    }
}