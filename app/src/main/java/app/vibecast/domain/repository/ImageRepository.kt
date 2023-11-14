package app.vibecast.domain.repository

import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.flow.Flow


interface ImageRepository {

    fun getImages(query:  String) : Flow<List<ImageDto>>

    fun pickRandomImage(query : String): Flow<ImageDto?>
}