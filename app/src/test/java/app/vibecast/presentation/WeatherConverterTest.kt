package app.vibecast.presentation

import android.content.Context
import app.vibecast.domain.usecase.GetCurrentWeatherUseCase
import app.vibecast.presentation.weather.WeatherConverter
import org.junit.Test
import org.mockito.kotlin.mock
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.presentation.weather.WeatherModel
import app.vibecast.R
import org.junit.Assert.assertEquals
import org.junit.Before
import org.mockito.kotlin.whenever


class WeatherConverterTest {

    private val testContext = mock<Context>()
    private val converter = WeatherConverter(testContext)

    private lateinit var weatherModel: WeatherModel
    private lateinit var  expectedWeather : WeatherDto
    private val cityName = "London"
    private val timestamp = "1637094000"
    private val temperature = "15.0"
    private val feelsLike = "14.0"
    private val humidity = "70"
    private val uvi = "5.2"
    private val visibility = "10"
    private val windSpeed = "12.0"
    private val weatherDescription = "Clear"
    private val firstHourTimeStamp = "1637094000"
    private val firstHourTemp = "14.0"
    private val firstHourWeatherCondition = "Partly Cloudy"
    private val secondHourTimestamp = "1637094000"
    private val secondHourTemperature = "14.0"
    private val secondHourWeatherCondition = "Partly Cloudy"
    private val chanceOfRain = "8"

    @Before
    fun setUp() {
        weatherModel =  WeatherModel(
            cityName = cityName,
            currentWeather = WeatherModel.CurrentWeatherModel(
                timestamp = timestamp,
                temperature = temperature,
                feelsLike = feelsLike,
                humidity = humidity,
                uvi = uvi,
                visibility = visibility,
                windSpeed = windSpeed,
                weatherConditions = listOf(
                    WeatherModel.WeatherConditionModel(mainDescription = "Clear")
                )
            ),
            hourlyWeather = List(3) {
                WeatherModel.HourlyWeatherModel(
                    firstHourTimeStamp = firstHourTimeStamp,
                    firstHourTemp = firstHourTemp,
                    firstHourWeatherCondition = firstHourWeatherCondition,
                    secondHourTimestamp = secondHourTimestamp,
                    secondHourTemperature = secondHourTemperature,
                    secondHourWeatherCondition = secondHourWeatherCondition,

                )
            }
        )

        expectedWeather =  WeatherDto(
            cityName = cityName,
            latitude = 51.5074,
            longitude = -0.1278,
            currentWeather = CurrentWeather(
                timestamp = 1637094000,
                temperature = 15.0,
                feelsLike = 14.0,
                humidity = 70,
                uvi = 5.2,
                cloudCover = 40,
                visibility = 10,
                windSpeed = 12.0,
                weatherConditions = listOf(
                    WeatherCondition(
                        conditionId = 800,
                        mainDescription = "Clear",
                        detailedDescription = "Clear sky",
                        icon = "01d"
                    )
                )
            ),
            hourlyWeather = List(24) {
                HourlyWeather(
                    timestamp = 1637094000,
                    temperature = 14.0,
                    feelsLike = 13.0,
                    humidity = 65,
                    uvi = 5.5,
                    cloudCover = 45,
                    windSpeed = 11.0,
                    weatherConditions = listOf(
                        WeatherCondition(
                            conditionId = 800,
                            mainDescription = "Clear",
                            detailedDescription = "Clear sky",
                            icon = "01d"
                        )
                    ),
                    chanceOfRain = 10.0
                )
            }
        )

    }


    @Test
    fun testConvertSuccess() {
        val response = GetCurrentWeatherUseCase.Response(expectedWeather)
        whenever(testContext.getString(R.string.left_time, "left_time")).thenReturn(timestamp)
        whenever(testContext.getString(R.string.center_temperature_text, "center_temperature_text")).thenReturn(temperature)
        whenever(testContext.getString(R.string.left_temp, "left_temp")).thenReturn(temperature)
        whenever(testContext.getString(R.string.feels_like, "feels_like")).thenReturn(feelsLike)
        whenever(testContext.getString(R.string.humidity, "humidity")).thenReturn(humidity)
        whenever(testContext.getString(R.string.uv_index_value, "uv_index_value")).thenReturn(uvi)
        whenever(testContext.getString(R.string.visibility_value, "visibility_value")).thenReturn(visibility)
        whenever(testContext.getString(R.string.wind_speed_value, "wind_speed_value")).thenReturn(windSpeed)
        whenever(testContext.getString(R.string.left_weather_condition, "left_weather_condition")).thenReturn(weatherDescription)
        whenever(testContext.getString(R.string.center_time, "center_time")).thenReturn(firstHourTimeStamp)
        whenever(testContext.getString(R.string.center_temp, "center_temp")).thenReturn(firstHourTemp)
        whenever(testContext.getString(R.string.center_weather_condition, "center_weather_condition")).thenReturn(firstHourWeatherCondition)
        whenever(testContext.getString(R.string.right_time, "right_time")).thenReturn(secondHourTimestamp)
        whenever(testContext.getString(R.string.right_temp,"right_temp")).thenReturn(secondHourTemperature)
        whenever(testContext.getString(R.string.right_weather_condition, "right_weather_condition")).thenReturn(secondHourWeatherCondition)
        whenever(testContext.getString(R.string.chance_of_rain_value, "chance_of_rain_value")).thenReturn(chanceOfRain)
        val result = converter.convertSuccess(response)
        assertEquals(weatherModel, result)





    }
}