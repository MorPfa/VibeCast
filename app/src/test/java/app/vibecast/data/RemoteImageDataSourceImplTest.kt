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


    @Before
    fun setUp() {

        imageApiModel = ImageApiModel(
            id = "test",
            description = "test",
            altDescription = "test",
            urls = ImageApiModel.PhotoUrls(
                full = "https://dummyurl.com/full",
                regular = "https://dummyurl.com/regular",
                small = "https://dummyurl.com/small",
                thumb = "https://dummyurl.com/thumb"
            ), links = ImageApiModel.PhotoLinks(
                user = "test",
                downloadLink = "https://dummyurl.com/raw"
            ), user = ImageApiModel.UnsplashUser(
                id = "test",
                userName = "test",
                name = "test",
                portfolioUrl = "https://dummyurl.com/raw"
            ))

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
            )
        )
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testGetImages() {
        runTest {
            val query = "Seattle rainy"
            val orientation = "portrait"
            val remoteImages = listOf(imageApiModel)
            val expectedImages = listOf(imageApiModel.toImageDto())
            whenever(imageService.getImages(query, orientation, 1)).thenReturn(remoteImages)
            val result = remoteImageDataSource.getImages(query).first()
            assertEquals(expectedImages[0], result)
        }
    }


    private fun ImageApiModel.toImageDto(): ImageDto {
        return ImageDto(
            id = this.id,
            description = this.description,
            altDescription = this.altDescription,
            urls = ImageDto.PhotoUrls(
                full = this.urls.full,
                regular = this.urls.regular,
                small = this.urls.small,
                thumb = this.urls.thumb
            ),
            user = ImageDto.UnsplashUser(
                id = this.user.id,
                name = this.user.name,
                userName = this.user.userName,
                portfolioUrl = this.user.portfolioUrl
            ),
            links = ImageDto.PhotoLinks(
                user = this.links.user,
                downloadLink = this.links.downloadLink
            )
        )
    }
}