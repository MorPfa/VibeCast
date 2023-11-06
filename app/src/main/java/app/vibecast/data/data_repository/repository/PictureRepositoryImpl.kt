package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.remote.RemotePictureDataSource
import app.vibecast.domain.entity.Picture
import app.vibecast.domain.repository.PictureRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

//TODO add local data source
class PictureRepositoryImpl @Inject constructor(
    private val remotePictureDataSource: RemotePictureDataSource) : PictureRepository {
    override fun getPictures(cityName: String): Flow<Picture> {
        TODO("Not yet implemented")
    }
}