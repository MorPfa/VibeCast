package app.vibecast.data.remote_data.data_source.image


import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface RemoteImageDataSource {

    suspend fun getImageForDownload(query: String): Resource<String>

    suspend fun getImages(query: String, collections : String): Resource<ImageDto>
}