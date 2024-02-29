package app.vibecast.data.remote_data.data_source.image


import app.vibecast.domain.model.ImageDto
import kotlinx.coroutines.flow.Flow

interface RemoteImageDataSource {



     fun getImageForDownload(query: String) : Flow<String>

     fun getImages(query : String) : Flow<ImageDto>
}