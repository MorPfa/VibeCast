package app.vibecast.data

import app.vibecast.data.local.db.image.ImageDao
import app.vibecast.data.local.db.image.ImageEntity
import app.vibecast.data.local.source.LocalImageDataSourceImpl
import app.vibecast.domain.entity.ImageDto
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

class LocalImageDataSourceImplTest {

    private val imageDao = mock<ImageDao>()
    private val imageDataSource = LocalImageDataSourceImpl(imageDao)
    private lateinit var imageEntity : ImageEntity
    private lateinit var imageDto: ImageDto

    @Before
    fun setUp() {

        imageEntity = ImageEntity(
            id = "123456",
            description = "Beautiful landscape",
            altDescription = "test",
            regularUrl = "https://example.com/image.jpg",
            name = "John Doe",
            userId = "789",
            userName = "john_doe",
            portfolioUrl = "https://example.com/john_doe",
            userLink = "test",
            downloadLink = "https://dummyurl.com/raw"
        )

        imageDto = ImageDto(
            id = "789012",
            description = "Amazing sunset",
            altDescription = "test",
            urls = ImageDto.PhotoUrls(
                raw = "https://example.com/raw_image.jpg",
                full = "https://example.com/full_image.jpg",
                regular = "https://example.com/regular_image.jpg",
                small = "https://example.com/small_image.jpg",
                thumb = "https://example.com/thumb_image.jpg"
            ),
            user = ImageDto.UnsplashUser(
                id = "456",
                name = "Jane Doe",
                userName = "jane_doe",
                portfolioUrl = "https://example.com/jane_doe"
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
            val retriedImageDto = imageEntity.toImageDto()
            val localImages = listOf(imageEntity)
            val expectedImages = listOf(retriedImageDto)
            whenever(imageDao.getAllImages()).thenReturn(flowOf(localImages))
            val result = imageDataSource.getImages().first()
            assertEquals(expectedImages, result)
        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testAddImage() {
        runTest {
            imageDataSource.addImage(imageDto)
            val imageEntity =  ImageEntity(
                imageDto.id,
                imageDto.description,
                imageDto.altDescription,
                imageDto.urls.regular,
                imageDto.user.name,
                imageDto.user.id,
                imageDto.user.userName,
                imageDto.user.portfolioUrl,
                imageDto.links.user,
                imageDto.links.downloadLink
            )
            verify(imageDao).addImage(imageEntity)

        }
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testDeleteImage() {
        runTest {
            imageDataSource.deleteImage(imageDto)
            val imageEntity =  ImageEntity(
                imageDto.id,
                imageDto.description,
                imageDto.altDescription,
                imageDto.urls.regular,
                imageDto.user.name,
                imageDto.user.id,
                imageDto.user.userName,
                imageDto.user.portfolioUrl,
                imageDto.links.user,
                imageDto.links.downloadLink
            )
            verify(imageDao).deleteImage(imageEntity)

        }
    }

    private fun ImageEntity.toImageDto(): ImageDto {
        return ImageDto(
            id = this.id,
            description = this.description,
            altDescription = this.altDescription,
            urls = ImageDto.PhotoUrls(
                raw = "",
                full = "",
                regular = this.regularUrl,
                small = "",
                thumb = ""
            ),
            user = ImageDto.UnsplashUser(
                id = this.userId,
                name = this.name,
                userName = this.userName,
                portfolioUrl = this.portfolioUrl
            ),
            links = ImageDto.PhotoLinks(
                user = this.userLink,
                downloadLink = this.downloadLink
            )
        )
    }

}