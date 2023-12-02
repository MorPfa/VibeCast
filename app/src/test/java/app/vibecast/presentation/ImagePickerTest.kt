package app.vibecast.presentation

import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.presentation.image.ImagePicker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

class ImagePickerTest {

    private val imageRepository = mock<ImageRepository>()
    private val imagePicker = ImagePicker(imageRepository)
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
            )
        )
    }

    @Test
    fun testImagePicker() = runTest {
        val cityName = "London"
        val weatherCondition = "Clear"
        val expectedWeather = "clear"
        val expectedSearchQuery = "$cityName $expectedWeather"

        val expectedImage = imageDto
        whenever(imageRepository.getRemoteImages(expectedSearchQuery)).thenReturn(flowOf(expectedImage))

        val result = imagePicker.pickImage(cityName, weatherCondition).first()
        assertEquals(expectedImage, result)
    }
}