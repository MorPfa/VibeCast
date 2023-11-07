package app.vibecast.data.local.db.location

import androidx.room.Embedded
import androidx.room.Relation
import app.vibecast.data.local.db.weather.WeatherEntity

data class LocationWithWeatherData(
    @Embedded val location: LocationEntity,
    @Relation(
        parentColumn = "cityName",
        entityColumn = "cityName"
    )
    val weather : WeatherEntity
)
