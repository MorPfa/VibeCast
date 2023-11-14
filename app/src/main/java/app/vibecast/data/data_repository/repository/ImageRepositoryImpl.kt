package app.vibecast.data.data_repository.repository

import app.vibecast.data.data_repository.data_source.remote.RemoteImageDataSource
import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject


class ImageRepositoryImpl @Inject constructor(
    private val remoteImageDataSource: RemoteImageDataSource) : ImageRepository {
    override fun getImages(query: String): Flow<List<ImageDto>> = remoteImageDataSource.getImages(query)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun pickRandomImage(query : String): Flow<ImageDto?> = getImages(query).flatMapLatest { list ->
        if (list.isNotEmpty()) {
            flowOf(list.random())
        } else {
            flowOf(null)
        }
    }

    //TODO add local data source

}