package app.vibecast.domain.repository.image

import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.util.Resource
import kotlinx.coroutines.flow.Flow


interface ImageRepository {

    suspend fun getImageForDownload(query: String) : Resource<String>

    suspend fun getRemoteImages(query:  String, collections : String) : Resource<ImageDto>

    fun getLocalImages() : Flow<List<ImageDto>>

    fun addImage(imageDto: ImageDto)

    fun deleteImage(imageDto: ImageDto)
    fun deleteAllImages()


}