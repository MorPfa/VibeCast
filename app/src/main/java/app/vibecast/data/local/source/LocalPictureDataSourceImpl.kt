package app.vibecast.data.local.source

import app.vibecast.data.data_repository.data_source.local.LocalPictureDataSource
import app.vibecast.data.local.db.picture.PictureDao
import app.vibecast.data.local.db.picture.PictureEntity
import app.vibecast.data.local.db.weather.WeatherDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalPictureDataSourceImpl @Inject constructor(private val pictureDao: PictureDao) : LocalPictureDataSource {
    override fun getPictures(cityName: String): Flow<PictureEntity> {
        TODO("Not yet implemented")
    }
}