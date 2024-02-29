package app.vibecast.data.local_data.data_source.image


import app.vibecast.domain.model.ImageDto
import kotlinx.coroutines.flow.Flow

interface LocalImageDataSource {

    fun getImages() : Flow<List<ImageDto>>

    suspend fun addImage(image : ImageDto)

    suspend fun deleteImage(image: ImageDto)
}