package app.vibecast.data.data_repository.data_source.remote

import app.vibecast.data.remote.network.picture.PictureApiModel
import app.vibecast.data.remote.network.weather.CoordinateApiModel
import kotlinx.coroutines.flow.Flow

interface RemotePictureDataSource {

    fun getPictures(name : String) : Flow<PictureApiModel>
}