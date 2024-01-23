package app.vibecast.presentation

import android.icu.text.SimpleDateFormat
import app.vibecast.data.data_repository.repository.Unit
import app.vibecast.data.remote.LocationGetter
import app.vibecast.domain.entity.CurrentWeather
import app.vibecast.domain.entity.HourlyWeather
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.entity.LocationDto
import app.vibecast.domain.entity.LocationWithWeatherDataDto
import app.vibecast.domain.entity.WeatherCondition
import app.vibecast.domain.entity.WeatherDto
import app.vibecast.domain.repository.DataStoreRepository
import app.vibecast.domain.repository.LocationRepository
import app.vibecast.domain.repository.WeatherRepository
import app.vibecast.presentation.mainscreen.MainScreenViewModel
import app.vibecast.presentation.weather.WeatherModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.Locale

class CurrentLocationViewModelTest{

    private val weatherRepository = mock<WeatherRepository>()
    private val locationRepository = mock<LocationRepository>()
    private val locationGetter = mock<LocationGetter>()
    private val dataStoreRepository = mock<DataStoreRepository>()
    private val viewModel = MainScreenViewModel(
                            locationGetter,
                            weatherRepository,
                            locationRepository,
                            dataStoreRepository
                        )
    private lateinit var imageDto: ImageDto
    private lateinit var  weatherDto : WeatherDto
    private val dateFormat = mock<SimpleDateFormat>()
    private lateinit var  expectedLocation : LocationDto
    private lateinit var  expectedLocationWithWeather : LocationWithWeatherDataDto




    @Before
    fun setUp() {
        imageDto =  ImageDto(
            id = "dummyId",
            description = "This is a dummy image",
            altDescription = "test",
            urls = ImageDto.PhotoUrls(
                full = "https://dummyurl.com/full",
                regular = "https://dummyurl.com/regular",
                small = "https://dummyurl.com/small",
                thumb = "https://dummyurl.com/thumb"
            ),
            user = ImageDto.UnsplashUser(
                id = "dummyUserId",
                name = "Dummy User",
                userName = "dummy_user",
                portfolioUrl = "https://dummyurl.com/portfolio"
            ),
            links = ImageDto.PhotoLinks(
                user = "test",
                downloadLink = "https://dummyurl.com/raw"
            ),
            timestamp = 1000
        )
        weatherDto =  WeatherDto(
            cityName = "Seattle",
            country = "US",
            latitude = 51.5074,
            longitude = -0.1278,
            dataTimestamp = 1000,
            timezone = "US",
            unit = Unit.IMPERIAL,
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
                        mainDescription = "Clear",
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
                    windSpeed = 11.0,
                    weatherConditions = listOf(
                        WeatherCondition(
                            mainDescription = "Clear",
                            icon = "01d"
                        )
                    ),
                    chanceOfRain = 10.0
                )
            }
        )

        expectedLocation = LocationDto("Seattle", "US")
        expectedLocationWithWeather = LocationWithWeatherDataDto(expectedLocation, weatherDto)
    }


    @Test
    fun testLoadWeather() {
        runTest {
            val city = "Seattle"
            val expectedWeather = convertWeatherDtoToWeatherModel(weatherDto)
            whenever(weatherRepository.getWeather(city)).thenReturn(flowOf(expectedLocationWithWeather))
            val result = viewModel.loadCurrentLocationWeather()
            assertEquals(expectedWeather, result)
        }
    }


    private fun convertWeatherDtoToWeatherModel(weatherDto: WeatherDto): WeatherModel {
        return WeatherModel(
            cityName = weatherDto.cityName,
            latitude = weatherDto.latitude,
            longitude = weatherDto.longitude,
            timezone = "US",
            currentWeather = weatherDto.currentWeather?.let { convertCurrentWeather(it) },
            hourlyWeather = weatherDto.hourlyWeather?.map { convertHourlyWeather(it) }
        )
    }

    private fun convertCurrentWeather(dto: CurrentWeather): WeatherModel.CurrentWeather {
        return WeatherModel.CurrentWeather(
            timestamp = convertUnixTimestampToAmPm(dto.timestamp),
            temperature = dto.temperature.toInt(),
            feelsLike = dto.feelsLike.toInt(),
            humidity = dto.humidity,
            uvi = dto.uvi,
            cloudCover = dto.cloudCover,
            visibility = formatVisibility(dto.visibility) ,
            windSpeed = dto.windSpeed.toString(),
            weatherConditions = convertWeatherConditions(dto.weatherConditions)
        )
    }

    private fun convertHourlyWeather(dto: HourlyWeather): WeatherModel.HourlyWeather {
        return WeatherModel.HourlyWeather(
            timestamp = convertUnixTimestampToAmPm(dto.timestamp),
            temperature = dto.temperature.toInt(),
            feelsLike = dto.feelsLike,
            humidity = dto.humidity,
            uvi = dto.uvi,
            windSpeed = dto.windSpeed,
            weatherConditions = convertWeatherConditions(dto.weatherConditions),
            chanceOfRain = dto.chanceOfRain.toInt()
        )
    }

    private fun convertWeatherConditions(dtoList: List<WeatherCondition>): List<WeatherModel.WeatherCondition> {
        return dtoList.map { convertWeatherCondition(it) }
    }

    private fun convertWeatherCondition(dto: WeatherCondition): WeatherModel.WeatherCondition {
        return WeatherModel.WeatherCondition(
            mainDescription = dto.mainDescription,
            icon = dto.icon
        )
    }

    private fun convertUnixTimestampToAmPm(unixTimestamp: Long): String {
        val date = Date(unixTimestamp * 1000L)
        whenever(dateFormat.format(date)).thenReturn("4:00:00 PM ")
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }


    private fun formatVisibility(visibility: Int): String {
        return when {
            visibility >= 1000 -> {
                val kilometers = visibility / 1000.0
                String.format("%.1f km", kilometers)
            }
            else -> {
                "$visibility meters"
            }
        }
    }


}