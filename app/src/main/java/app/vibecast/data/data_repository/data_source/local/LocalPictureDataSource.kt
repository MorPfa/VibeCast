package app.vibecast.data.data_repository.data_source.local


import app.vibecast.data.local.db.picture.PictureEntity
import kotlinx.coroutines.flow.Flow

interface LocalPictureDataSource {

    fun getPictures(cityName : String) : Flow<PictureEntity>
}