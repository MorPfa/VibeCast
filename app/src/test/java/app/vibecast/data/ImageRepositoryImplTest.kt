package app.vibecast.data

import app.vibecast.domain.repository.image.ImageRepositoryImpl
import app.vibecast.data.local_data.data_source.image.LocalImageDataSourceImpl
import app.vibecast.data.remote_data.data_source.image.RemoteImageDataSourceImpl
import app.vibecast.domain.model.ImageDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify

import org.mockito.kotlin.whenever

class ImageRepositoryImplTest {

    private val remoteImageDataSource = mock<RemoteImageDataSourceImpl>()
    private val localImageDataSource = mock<LocalImageDataSourceImpl>()
    private val imageRepository = ImageRepositoryImpl(remoteImageDataSource, localImageDataSource)
    private lateinit var imageDto: ImageDto


    @Before
    fun setUp() {
        imageDto = ImageDto(
            id = "dummyId",
            description = "This is a dummy image",
            altDescription = "test",
            urls = ImageDto.PhotoUrls(
                full = "https://dummyurl.com/full",
                regular = "https://dummyurl.com/regular",
                small = "https://dummyurl.com/small",
                thumb = "https://dummyurl.com/thumb"
            ),
            user = ImageDto.UnsplashUser(
                id = "dummyUserId",
                name = "Dummy User",
                userName = "dummy_user",
                portfolioUrl = "https://dummyurl.com/portfolio"
            ),
            links = ImageDto.PhotoLinks(
                user = "test",
                downloadLink = "https://dummyurl.com/raw"
            ),
            timestamp = 1000
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetRemoteImages() {
        runTest {
            val query = "Seattle rainy"
            val collections = "HD wallpapers"
            val expectedImages = imageDto
            whenever(remoteImageDataSource.getImages(query, collections)).thenReturn(flowOf(expectedImages))
            val result = imageRepository.getRemoteImages(query, collections).first()
            assertEquals(expectedImages,result)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetLocalImages() {
        runTest {
            val expectedImages = listOf(imageDto)
            whenever(localImageDataSource.getImages()).thenReturn(flowOf(expectedImages))
            val result = imageRepository.getLocalImages().first()
            assertEquals(expectedImages, result)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testAddImage(){
        runTest {
            imageRepository.addImage(imageDto)
            verify(localImageDataSource).addImage(imageDto)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testDeleteImages(){
        runTest {
            imageRepository.deleteImage(imageDto)
            verify(localImageDataSource).deleteImage(imageDto)

        }
    }
}