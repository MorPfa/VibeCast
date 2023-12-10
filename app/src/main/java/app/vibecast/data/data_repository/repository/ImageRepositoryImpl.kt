package app.vibecast.data.data_repository.repository

import android.util.Log
import app.vibecast.data.TAGS.COROUTINE_ERROR
import app.vibecast.data.TAGS.IMAGE_ERROR
import app.vibecast.data.data_repository.data_source.local.LocalImageDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteImageDataSource
import app.vibecast.data.remote.source.RemoteImageDataSourceImpl
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
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
    private val localImageDataSource: LocalImageDataSource) : ImageRepository {
    override fun getRemoteImages(query: String): Flow<ImageDto> = flow {
        try {
            emitAll(remoteImageDataSource.getImages(query))
        } catch (e: Exception) {
            Log.e(IMAGE_ERROR, e.toString())
            throw RemoteImageDataSourceImpl.ImageFetchException("Error fetching remote images", e)
        }
    }.flowOn(Dispatchers.IO)


    override fun getLocalImages(): Flow<List<ImageDto>> = flow {
        try {
            emitAll(localImageDataSource.getImages())
        } catch (e: CancellationException) {
            Log.e(COROUTINE_ERROR, "Coroutine cancelled: $e")
        } catch (e: Exception) {
            Log.e(IMAGE_ERROR, "Error fetching local images: $e")
        }
    }.flowOn(Dispatchers.IO)


    override fun addImage(imageDto: ImageDto) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localImageDataSource.addImage(imageDto)
            } catch (e: CancellationException) {
                Log.e(COROUTINE_ERROR, e.toString())
            } catch (e: Exception) {
                Log.e(IMAGE_ERROR, e.toString())
            }
        }
    }


    override fun deleteImage(imageDto: ImageDto) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localImageDataSource.deleteImage(imageDto)
            } catch (e: CancellationException) {
                Log.e(COROUTINE_ERROR, e.toString())
            } catch (e: Exception) {
                Log.e(IMAGE_ERROR, e.toString())
            }
        }
    }
}