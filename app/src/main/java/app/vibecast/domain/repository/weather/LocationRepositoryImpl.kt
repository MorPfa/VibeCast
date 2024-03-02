package app.vibecast.domain.repository.weather

import app.vibecast.data.local_data.data_source.weather.LocalLocationDataSource
import app.vibecast.data.remote_data.data_source.weather.RemoteWeatherDataSource
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.LocationWithWeatherDataDto
import app.vibecast.domain.model.WeatherDto
import app.vibecast.presentation.screens.main_screen.weather.LocationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val localLocationDataSource: LocalLocationDataSource,
    private val remoteWeatherDataSource: RemoteWeatherDataSource
) : LocationRepository {
    private val backgroundScope = CoroutineScope(Dispatchers.IO)

    /**
     *  Updates weather data for specified location
     */
    override fun refreshLocationWeather() : Flow<List<LocationWithWeatherDataDto>> {
        backgroundScope.launch {
            localLocationDataSource.getLocationWithWeather().map { locationWithWeatherDataList ->
                    locationWithWeatherDataList.map { locationWithWeatherData ->
                        val cityName = locationWithWeatherData.location.cityName
                        val newWeatherData = remoteWeatherDataSource.getWeather(cityName).firstOrNull()
                        if (newWeatherData != null) {
                            locationWithWeatherData.weather = newWeatherData.weather
                        }

                        localLocationDataSource.addLocationWithWeather(locationWithWeatherData )

                    }

                }.single()
        }
        return localLocationDataSource.getLocationWithWeather()
    }

    override fun addLocationWeather(location: LocationWithWeatherDataDto) {
        CoroutineScope(Dispatchers.Main).launch {
        localLocationDataSource.addLocationWithWeather(location)
        }
    }
    override fun getLocationWeather(index: Int): Flow<WeatherDto> {
        return localLocationDataSource.getLocationWithWeather().map { locationWithWeatherDataList ->
            if (index in locationWithWeatherDataList.indices) {
                val selectedLocationWithWeather = locationWithWeatherDataList[index]
                selectedLocationWithWeather.weather
            } else {
                WeatherDto(
                    cityName = "",
                    country = "",
                    latitude = 0.0,
                    longitude = 0.0,
                    dataTimestamp = System.currentTimeMillis(),
                    timezone = "",
                    unit = null,
                    currentWeather = null,
                    hourlyWeather = null
                )
            }
        }
    }


    override fun getLocations(): Flow<List<LocationModel>> = flow {
        localLocationDataSource.getLocations().collect{
            emit(it.toLocationModels())
        }
    }


    private fun List<LocationDto>.toLocationModels(): List<LocationModel> {
        return map { locationDto ->
            LocationModel(
                cityName = locationDto.cityName,
                country = locationDto.country
            )
        }
    }

    override fun getLocation(cityName: String): Flow<LocationDto> = localLocationDataSource.getLocation(cityName)

    override fun addLocation(location: LocationDto) =
        CoroutineScope(Dispatchers.Main).launch {
        localLocationDataSource.addLocation(location)
    }

    override fun deleteLocation(location: LocationDto) =
        CoroutineScope(Dispatchers.IO).launch {
            localLocationDataSource.deleteLocation(location)
        }

}