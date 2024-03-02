package app.vibecast.data


import app.vibecast.BuildConfig
import app.vibecast.domain.repository.weather.Unit
import app.vibecast.data.remote_data.network.weather.model.CoordinateApiModel
import app.vibecast.data.remote_data.network.weather.model.CurrentWeatherRemote
import app.vibecast.data.remote_data.network.weather.model.HourlyWeatherRemote
import app.vibecast.data.remote_data.network.weather.model.WeatherApiModel
import app.vibecast.data.remote_data.network.weather.model.WeatherConditionRemote
import app.vibecast.data.remote_data.network.weather.api.WeatherService
import app.vibecast.data.remote_data.data_source.weather.RemoteWeatherDataSourceImpl
import app.vibecast.domain.model.CurrentWeather
import app.vibecast.domain.model.HourlyWeather
import app.vibecast.domain.model.UseCaseException
import app.vibecast.domain.model.WeatherCondition
import app.vibecast.domain.model.WeatherDto
import app.vibecast.domain.repository.weather.UnitPreferenceRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever


class RemoteWeatherDataSourceImplTest {

    private val weatherService = mock<WeatherService>()
    private val dataStore = mock<UnitPreferenceRepository>()
    private val weatherDataSource = RemoteWeatherDataSourceImpl(weatherService, dataStore)
    private lateinit var remoteWeather: WeatherApiModel
    private lateinit var  expectedWeather : WeatherDto
    private val cityName = "Seattle"


    @Before
    fun setUp() {
         remoteWeather = WeatherApiModel(
             cityName = "Seattle",
             latitude = 51.5074,
             longitude = -0.1278,
             timezone = "US",
             currentWeatherRemote = CurrentWeatherRemote(
                 timestamp = 1637094000,
                 temperature = 15.0,
                 feelsLike = 14.0,
                 humidity = 70,
                 uvi = 5.2,
                 cloudCover = 40,
                 visibility = 10,
                 windSpeed = 12.0,
                 weatherConditionRemotes = listOf(
                     WeatherConditionRemote(
                         mainDescription = "Clear",
                         icon = "01d"
                     )
                 )
             ),
             hourlyWeather = List(24) {
                 HourlyWeatherRemote(
                     timestamp = (1637094000 + it * 3600).toLong(),
                     temperature = 14.0,
                     feelsLike = 13.0,
                     humidity = 65,
                     uvi = 5.5,
                     windSpeed = 11.0,
                     weatherConditionRemotes = listOf(
                         WeatherConditionRemote(
                             mainDescription = "Clear",
                             icon = "01d"
                         )
                     ),
                     chanceOfRain = 10.0
                 )
             }
         )

        expectedWeather =  WeatherDto(
            cityName = cityName,
            country = "US",
            latitude = 51.5074,
            longitude = -0.1278,
            dataTimestamp = 10000,
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

    }



    @ExperimentalCoroutinesApi
    @Test
    fun testGetCityCoordinates() = runTest {
        val cityName = "London"
        val remoteCoordinates = listOf(CoordinateApiModel("Seattle",51.5073219,-0.1276474, "US" ))
        whenever(weatherService.getCiyCoordinates(cityName,1,BuildConfig.OWM_KEY)).thenReturn(remoteCoordinates)
        val result = weatherDataSource.getCoordinates(cityName).single()
        assertEquals(remoteCoordinates[0], result)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetWeather() = runTest {
        val remoteCoordinates = listOf(CoordinateApiModel("Seattle",51.5073219,-0.1276474, "US" ))
        whenever(weatherService.getCiyCoordinates(
            cityName,
            1,
            BuildConfig.OWM_KEY
        )).thenReturn(remoteCoordinates)

        whenever(weatherService.getWeather(
            remoteCoordinates[0].latitude,
            remoteCoordinates[0].longitude,
            "minutely,daily",
            "Imperial",
            BuildConfig.OWM_KEY
        )).thenReturn(remoteWeather)

        val result = weatherDataSource.getWeather(cityName).single()


        assertEquals(expectedWeather.cityName, result.weather.cityName)
        assertEquals(expectedWeather.longitude, result.weather.longitude,1.0)
        assertEquals(expectedWeather.latitude, result.weather.latitude,1.0)
        assertEquals(expectedWeather.currentWeather?.timestamp, result.weather.currentWeather?.timestamp)
    }


    @ExperimentalCoroutinesApi
    @Test
    fun testGetWeatherWithCoordinates() = runTest {
        whenever(weatherService.getWeather(
            expectedWeather.latitude,
            expectedWeather.longitude,
            "minutely,daily",
            "Imperial",
            BuildConfig.OWM_KEY
        )).thenReturn(remoteWeather)
        val result = weatherDataSource.getWeather(expectedWeather.latitude, expectedWeather.longitude).single()
        assertEquals(expectedWeather.longitude, result.weather.longitude,1.0)
        assertEquals(expectedWeather.latitude, result.weather.latitude,1.0)
        assertEquals(expectedWeather.currentWeather?.timestamp, result.weather.currentWeather?.timestamp)
    }


    @ExperimentalCoroutinesApi
    @Test
    fun testGetWeatherThrowsError() = runTest {
        val cityName = "London"
        val remoteCoordinates = CoordinateApiModel("Seattle",51.5073219,-0.1276474, "US" )
        whenever(weatherService.getWeather(
            remoteCoordinates.latitude,
            remoteCoordinates.longitude,
            "minutely,daily",
            "Imperial",
            BuildConfig.OWM_KEY
        )).thenThrow(RuntimeException())
        weatherDataSource.getWeather(cityName).catch {
            assertTrue(it is UseCaseException.WeatherException)
        }.collect()
    }


    private fun WeatherApiModel.toWeather(): WeatherDto {
        return WeatherDto(
            cityName = cityName,
            country = "",
            latitude = latitude,
            longitude = longitude,
            dataTimestamp = 1000,
            timezone = timezone,
            unit = Unit.IMPERIAL,
            currentWeather = currentWeatherRemote.toCurrentWeather(),
            hourlyWeather = hourlyWeather.map { it.toHourlyWeather() }
        )
    }

    private fun CurrentWeatherRemote.toCurrentWeather(): CurrentWeather {
        return CurrentWeather(
            timestamp = timestamp,
            temperature = temperature,
            feelsLike = feelsLike,
            humidity = humidity,
            uvi = uvi,
            cloudCover = cloudCover,
            visibility = visibility,
            windSpeed = windSpeed,
            weatherConditions = weatherConditionRemotes.map { it.toWeatherCondition() }
        )
    }

    private fun HourlyWeatherRemote.toHourlyWeather(): HourlyWeather {
        return HourlyWeather(
            timestamp = timestamp,
            temperature = temperature,
            feelsLike = feelsLike,
            humidity = humidity,
            uvi = uvi,
            windSpeed = windSpeed,
            weatherConditions = weatherConditionRemotes.map { it.toWeatherCondition() },
            chanceOfRain = chanceOfRain
        )
    }

    private fun WeatherConditionRemote.toWeatherCondition(): WeatherCondition {
        return WeatherCondition(
            mainDescription = mainDescription,
            icon = icon
        )
    }
}

