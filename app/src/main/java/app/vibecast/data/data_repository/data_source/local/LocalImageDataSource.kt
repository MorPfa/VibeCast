package app.vibecast.data.data_repository.data_source.local


import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.flow.Flow

interface LocalImageDataSource {

    fun getImages() : Flow<List<ImageDto>>

    suspend fun addImage(image : ImageDto)

    suspend fun deleteImage(image: ImageDto)
}