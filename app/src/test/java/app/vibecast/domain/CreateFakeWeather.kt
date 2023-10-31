package app.vibecast.domain

import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.Weather
import app.vibecast.domain.entity.WeatherCondition
import kotlin.random.Random


class CreateFakeWeather {

    fun createFakeWeather(): Weather {
        val random = Random

        // Generate random latitude and longitude values
        val cityName = " London"
        val latitude = random.nextDouble(-90.0, 90.0)
        val longitude = random.nextDouble(-180.0, 180.0)

        // Create a fake CurrentWeather object
        val currentWeather = CurrentWeather(
            timestamp = System.currentTimeMillis(),
            temperature = random.nextDouble(-30.0, 40.0),
            feelsLike = random.nextDouble(-30.0, 40.0),
            humidity = random.nextInt(0, 100),
            uvi = random.nextDouble(0.0, 15.0),
            cloudCover = random.nextInt(0, 100),
            visibility = random.nextInt(0, 20),
            windSpeed = random.nextDouble(0.0, 30.0),
            weatherConditions = listOf(
                WeatherCondition(
                    conditionId = random.nextInt(200, 800),
                    mainDescription = "Clear",
                    detailedDescription = "Clear sky",
                    icon = "01d"
                )
            )
        )

        // Create a list of fake HourlyWeather objects
        val hourlyWeather = List(24) {
            HourlyWeather(
                timestamp = System.currentTimeMillis() + it * 3600000, // One hour difference
                temperature = random.nextDouble(-30.0, 40.0),
                feelsLike = random.nextDouble(-30.0, 40.0),
                humidity = random.nextInt(0, 100),
                uvi = random.nextDouble(0.0, 15.0),
                cloudCover = random.nextInt(0, 100),
                windSpeed = random.nextDouble(0.0, 30.0),
                weatherConditions = listOf(
                    WeatherCondition(
                        conditionId = random.nextInt(200, 800),
                        mainDescription = "Clear",
                        detailedDescription = "Clear sky",
                        icon = "01d"
                    )
                ),
                chanceOfRain = random.nextDouble(0.0, 100.0)
            )
        }

        return Weather(cityName, latitude, longitude, currentWeather, hourlyWeather)
    }

}