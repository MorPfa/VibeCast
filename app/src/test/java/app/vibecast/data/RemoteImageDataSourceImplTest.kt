package app.vibecast.data

import app.vibecast.data.remote.network.image.ImageApiModel
import app.vibecast.data.remote.network.image.ImageService
import app.vibecast.data.remote.source.RemoteImageDataSourceImpl
import app.vibecast.domain.entity.ImageDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals

import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class RemoteImageDataSourceImplTest {

    private val imageService = mock<ImageService>()
    private val remoteImageDataSource = RemoteImageDataSourceImpl(imageService)
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
            val orientation = "portrait"
            val expectedImages = listOf(imageDto)
            whenever(imageService.getImages(query, orientation)).thenReturn(imageApiModel)
            val result = remoteImageDataSource.getImages(query).first()
            assertEquals(expectedImages, result)
        }
    }
}