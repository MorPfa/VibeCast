package app.vibecast.data.local_data.db.image.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import app.vibecast.data.local_data.db.image.model.ImageEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Query("SELECT * FROM images ORDER BY timestamp DESC")
    fun getAllImages() : Flow<List<ImageEntity>>

    @Upsert
    suspend fun addImage(imageEntity: ImageEntity)

    @Delete
    suspend fun deleteImage(imageEntity: ImageEntity)

}