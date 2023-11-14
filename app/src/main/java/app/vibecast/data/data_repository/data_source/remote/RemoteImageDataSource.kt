package app.vibecast.data.data_repository.data_source.remote


import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.flow.Flow

interface RemoteImageDataSource {

     fun getImages(query : String) : Flow<List<ImageDto>>
}