package app.vibecast.domain.repository

import app.vibecast.domain.entity.Picture
import kotlinx.coroutines.flow.Flow


interface PictureRepository {

    fun getPictures(cityName : String) : Flow<Picture>
}