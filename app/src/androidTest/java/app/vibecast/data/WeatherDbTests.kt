package app.vibecast.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.vibecast.domain.repository.implementation.Unit
import app.vibecast.data.local_data.db.AppDatabase
import app.vibecast.data.local_data.db.location.dao.LocationDao
import app.vibecast.data.local_data.db.location.model.LocationEntity
import app.vibecast.data.local_data.db.location.model.LocationWithWeatherData
import app.vibecast.data.local_data.db.weather.dao.WeatherDao
import app.vibecast.data.local_data.db.weather.model.WeatherEntity
import app.vibecast.domain.model.CurrentWeather
import app.vibecast.domain.model.HourlyWeather
import app.vibecast.domain.model.WeatherCondition
import app.vibecast.domain.model.WeatherDto
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WeatherDbTests {

    private lateinit var db: AppDatabase
    private lateinit var weatherDao: WeatherDao
    private lateinit var locationDao: LocationDao
    private lateinit var weatherData: WeatherEntity
    private lateinit var locationData: LocationEntity
    private lateinit var locationWithWeatherData: LocationWithWeatherData

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        weatherDao = db.weatherDao()
        locationDao = db.locationDao()

        weatherData = WeatherEntity(
            cityName = "Seattle",
            countryName = "US",
            dataTimestamp = 1000,
            unit = Unit.IMPERIAL,
            weatherData = WeatherDto(
                cityName = "Seattle",
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
                        timestamp = (1637094000 + it * 3600).toLong(), // Incrementing timestamp for hourly forecast
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
        )
        locationData = LocationEntity(
            cityName = "Seattle",
            country = "US"
        )

        locationWithWeatherData = LocationWithWeatherData(locationData, weatherData)
    }


    @After
    fun closeDb() {
        db.close()
    }


    @Test
    fun testAddWeather() {
        runBlocking {
            weatherDao.addWeather(weatherData)
            val retrievedData = weatherDao.getWeather("Seattle").firstOrNull()
            assertNotNull(retrievedData)
            assertEquals(weatherData, retrievedData)
        }
    }

    @Test
    fun testRemoveWeather() {
        runBlocking {
            weatherDao.addWeather(weatherData)
            weatherDao.deleteWeather(weatherData)
            val dataAfterDeletion = weatherDao.getWeather("Seattle").firstOrNull()
            assertNull(dataAfterDeletion)

        }

    }

    @Test
    fun testGetWeatherByCity() {
        runBlocking {
            weatherDao.addWeather(weatherData)
            val retrievedData = weatherDao.getWeather("Seattle").firstOrNull()
            assertNotNull(retrievedData)
            assertEquals(weatherData, retrievedData)
        }
    }

    @Test
    fun testGetWeatherWithLocation() = runBlocking {
        locationDao.addLocationWithWeather(locationData, weatherData)
        val retrievedData = locationDao.getLocationsWithWeather().firstOrNull()
        assertNotNull(retrievedData)
        assertEquals(locationWithWeatherData, retrievedData?.get(0))
    }

    @Test
    fun testAddLocationWithWeather() = runBlocking {
        locationDao.addLocationWithWeather(locationData, weatherData)
        val retrievedData = locationDao.getLocationsWithWeather().firstOrNull()
        assertNotNull(retrievedData)
        assertEquals(locationWithWeatherData, retrievedData?.get(0))
    }

    @Test
    fun testRemoveLocationWithWeather() = runBlocking {
        locationDao.addLocationWithWeather(locationData, weatherData)
        locationDao.deleteLocation(locationData)
        val retrievedData = locationDao.getLocationsWithWeather().firstOrNull()
        assertNotNull(retrievedData)
        assertTrue( retrievedData!!.isEmpty())
    }
}
