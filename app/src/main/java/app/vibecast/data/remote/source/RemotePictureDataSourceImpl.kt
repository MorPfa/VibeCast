package app.vibecast.data.remote.source

import app.vibecast.data.data_repository.data_source.remote.RemotePictureDataSource
import app.vibecast.data.remote.network.picture.PictureApiModel
import app.vibecast.data.remote.network.picture.PictureService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RemotePictureDataSourceImpl @Inject constructor(
    private val pictureService: PictureService) : RemotePictureDataSource {

    override fun getPictures(name: String): Flow<PictureApiModel> {
        TODO("Not yet implemented")
    }
}