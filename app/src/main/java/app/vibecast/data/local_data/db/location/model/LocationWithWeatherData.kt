package app.vibecast.data.local_data.db.location.model

import androidx.room.Embedded
import androidx.room.Relation
import app.vibecast.data.local_data.db.weather.model.WeatherEntity


data class LocationWithWeatherData(
    @Embedded val location: LocationEntity,
    @Relation(
        parentColumn = "cityName",
        entityColumn = "cityName"
    )
    var weather : WeatherEntity
)
