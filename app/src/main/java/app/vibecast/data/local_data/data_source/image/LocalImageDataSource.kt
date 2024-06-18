package app.vibecast.data.local_data.data_source.image


import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow

interface LocalImageDataSource {

    fun getImages() : Flow<List<ImageDto>>

    suspend fun  getImagesForSync() : Resource<List<ImageDto>>

    suspend fun addImage(image : ImageDto)

    suspend fun deleteImage(image: ImageDto)
    suspend fun deleteAllImages()


}