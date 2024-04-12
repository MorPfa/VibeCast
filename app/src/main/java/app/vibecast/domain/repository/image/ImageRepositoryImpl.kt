package app.vibecast.domain.repository.image

import android.util.Log
import app.vibecast.domain.util.TAGS.COROUTINE_ERROR
import app.vibecast.domain.util.TAGS.IMAGE_ERROR
import app.vibecast.data.local_data.data_source.image.LocalImageDataSource
import app.vibecast.data.remote_data.data_source.image.RemoteImageDataSource
import app.vibecast.data.remote_data.data_source.image.RemoteImageDataSourceImpl
import app.vibecast.domain.model.ImageDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


class ImageRepositoryImpl @Inject constructor(
    private val remoteImageDataSource: RemoteImageDataSource,
    private val localImageDataSource: LocalImageDataSource
) : ImageRepository {




    /**
     *  Gets an image from the remote datasource
     */
    override fun getRemoteImages(query: String): Flow<ImageDto> = flow {
        try {
            emitAll(remoteImageDataSource.getImages(query))
        } catch (e: Exception) {
            Log.e(IMAGE_ERROR, e.toString())
            throw RemoteImageDataSourceImpl.ImageFetchException("Error fetching remote images", e)
        }
    }.flowOn(Dispatchers.IO)

    /**
     *  Gets the download URL for an image from the remote datasource
     */
    override fun getImageForDownload(query: String): Flow<String> = flow {
       remoteImageDataSource.getImageForDownload(query).collect{
            emit(it)
        }

    }

    /**
     *  Queries database for all saved images
     */
    override fun getLocalImages(): Flow<List<ImageDto>> = flow {
        try {
            emitAll(localImageDataSource.getImages())
        } catch (e: CancellationException) {
            Log.e(COROUTINE_ERROR, "Coroutine cancelled 1: $e")
        } catch (e: Exception) {
            Log.e(IMAGE_ERROR, "Error fetching local images: $e")
        }
    }.flowOn(Dispatchers.IO)

    /**
     *  Adds image to database
     */
    override fun addImage(imageDto: ImageDto) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localImageDataSource.addImage(imageDto)
            } catch (e: CancellationException) {
                Log.e(COROUTINE_ERROR, e.toString().plus("test"))
            } catch (e: Exception) {
                Log.e(IMAGE_ERROR, e.toString())
            }
        }
    }

    /**
     *  Deletes image from database
     */
    override fun deleteImage(imageDto: ImageDto) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localImageDataSource.deleteImage(imageDto)
            } catch (e: CancellationException) {
                Log.e(COROUTINE_ERROR, e.toString().plus("test"))
            } catch (e: Exception) {
                Log.e(IMAGE_ERROR, e.toString())
            }
        }
    }
}