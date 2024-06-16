package app.vibecast.domain.repository.image

import app.vibecast.data.local_data.data_source.image.LocalImageDataSource
import app.vibecast.data.remote_data.data_source.image.RemoteImageDataSource
import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.util.Resource
import app.vibecast.domain.util.TAGS.COROUTINE_ERROR
import app.vibecast.domain.util.TAGS.IMAGE_ERROR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException


class ImageRepositoryImpl @Inject constructor(
    private val remoteImageDataSource: RemoteImageDataSource,
    private val localImageDataSource: LocalImageDataSource,
) : ImageRepository {




    /**
     *  Gets an image from the remote datasource
     */
    override suspend fun getRemoteImages(query: String, collections: String): Resource<ImageDto> {
        return try {
            when (val data = remoteImageDataSource.getImages(query, collections)) {
                is Resource.Success -> {
                    data.data?.let {
                        Resource.Success(it)
                    } ?: Resource.Error("Data is null")
                }
                is Resource.Error -> {
                    Resource.Error(data.message)
                }
            }
        } catch (e: Exception) {
            Timber.tag(IMAGE_ERROR).e(e.localizedMessage)
            Resource.Error(e.localizedMessage)
        }
    }

    /**
     *  Gets the download URL for an image from the remote datasource
     */
    override suspend fun getImageForDownload(query: String): Resource<String>  {
        return when (val response = remoteImageDataSource.getImageForDownload(query)){
            is Resource.Success -> {
                Resource.Success(data = response.data!!)
            }
            is Resource.Error -> {
                Resource.Error(response.message)
            }
        }
    }

    /**
     *  Queries database for all saved images
     */
    override fun getLocalImages(): Flow<List<ImageDto>> = flow {
        try {
            emitAll(localImageDataSource.getImages())
        } catch (e: CancellationException) {
            Timber.tag(COROUTINE_ERROR).e("Coroutine cancelled $e")
        } catch (e: Exception) {
            Timber.tag(IMAGE_ERROR).e("Error fetching local images: $e")
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
                Timber.tag(COROUTINE_ERROR).e(e.localizedMessage)
            } catch (e: Exception) {
                Timber.tag(IMAGE_ERROR).e(e.localizedMessage)
            }
        }
    }

    override fun deleteAllImages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                localImageDataSource.deleteAllImages()
            } catch (e: CancellationException) {
                Timber.tag(COROUTINE_ERROR).e(e.localizedMessage)
            } catch (e: Exception) {
                Timber.tag(IMAGE_ERROR).e(e.localizedMessage)
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
                Timber.tag(COROUTINE_ERROR).e(e.localizedMessage)
            } catch (e: Exception) {
                Timber.tag(IMAGE_ERROR).e(e.localizedMessage)
            }
        }
    }
}