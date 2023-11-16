package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.local.LocalImageDataSource
import app.vibecast.data.data_repository.data_source.remote.RemoteImageDataSource
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import javax.inject.Inject


class ImageRepositoryImpl @Inject constructor(
    private val remoteImageDataSource: RemoteImageDataSource,
    private val localImageDataSource: LocalImageDataSource) : ImageRepository {
    override fun getRemoteImages(query: String): Flow<List<ImageDto>> = remoteImageDataSource.getImages(query)

    override fun getLocalImages(): Flow<List<ImageDto>> = localImageDataSource.getImages()



    @OptIn(ExperimentalCoroutinesApi::class)
    override fun pickRandomImage(query : String): Flow<ImageDto?> = getRemoteImages(query).flatMapLatest { list ->
        if (list.isNotEmpty()) {
            flowOf(list.random())
        } else {
            flowOf(null)
        }
    }

    override fun addImage(imageDto: ImageDto) {
        CoroutineScope(Dispatchers.Main).launch {
            localImageDataSource.addImage(imageDto)
        }
    }


    override fun deleteImage(imageDto: ImageDto) {
        CoroutineScope(Dispatchers.Main).launch {
            localImageDataSource.deleteImage(imageDto)
        }
    }





}