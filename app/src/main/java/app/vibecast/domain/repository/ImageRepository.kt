package app.vibecast.domain.repository

import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.flow.Flow


interface ImageRepository {

    fun getImageForDownload(query: String) : Flow<String>

    fun getRemoteImages(query:  String) : Flow<ImageDto>

    fun getLocalImages() : Flow<List<ImageDto>>

    fun addImage(imageDto: ImageDto)

    fun deleteImage(imageDto: ImageDto)
}