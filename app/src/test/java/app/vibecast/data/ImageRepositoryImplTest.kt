package app.vibecast.data

import app.vibecast.data.data_repository.repository.ImageRepositoryImpl
import app.vibecast.data.remote.network.image.Image
import app.vibecast.data.remote.network.image.ImageApiModel
import app.vibecast.data.remote.source.RemoteImageDataSourceImpl
import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

import org.mockito.kotlin.whenever

class ImageRepositoryImplTest {

    //TODO add local data source tests

    private val remoteImageDataSource = mock<RemoteImageDataSourceImpl>()
    private val imageRepository = ImageRepositoryImpl(remoteImageDataSource)
    private lateinit var imageApiModel: ImageApiModel
    private lateinit var imageDto: ImageDto
    private lateinit var image : Image


    @Before
    fun setUp() {
        image = Image(
            id = "dummyId",
            description = "This is a dummy image",
            urls = Image.PhotoUrls(
                raw = "https://dummyurl.com/raw",
                full = "https://dummyurl.com/full",
                regular = "https://dummyurl.com/regular",
                small = "https://dummyurl.com/small",
                thumb = "https://dummyurl.com/thumb"
            ),
            user = Image.UnsplashUser(
                id = "dummyUserId",
                name = "Dummy User",
                userName = "dummy_user",
                portfolioUrl = "https://dummyurl.com/portfolio"
            )
        )

        imageApiModel = ImageApiModel(listOf(image))

        imageDto = ImageDto(
            id = "dummyId",
            description = "This is a dummy image",
            urls = ImageDto.PhotoUrls(
                raw = "https://dummyurl.com/raw",
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
            )
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetImages() {
        runTest {
            val query = "Seattle rainy"
            val expectedImages = listOf(imageDto)
            whenever(remoteImageDataSource.getImages(query)).thenReturn(flowOf(expectedImages))
            val result = imageRepository.getImages(query).first()
            assertEquals(expectedImages,result)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testPickRandomImage() {
        runTest {
            val query = "Seattle rainy"
            val expectedImage = imageDto
            val listOfImageDtos = listOf(imageDto)
            whenever(imageRepository.getImages(query)).thenReturn(flowOf(listOfImageDtos))
            val result = imageRepository.pickRandomImage(query).first()
            assertEquals(expectedImage, result)
        }
    }
}