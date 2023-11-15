package app.vibecast.domain.repository

import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.flow.Flow


interface ImageRepository {

    fun getRemoteImages(query:  String) : Flow<List<ImageDto>>

    fun getLocalImages() : Flow<List<ImageDto>>

    fun pickRandomImage(query : String): Flow<ImageDto?>

    fun addImage(imageDto: ImageDto)

    fun deleteImage(imageDto: ImageDto)
}