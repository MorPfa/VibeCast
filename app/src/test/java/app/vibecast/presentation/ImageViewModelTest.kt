package app.vibecast.presentation


import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.repository.image.ImageRepository
import app.vibecast.presentation.screens.main_screen.image.ImageLoader
import app.vibecast.presentation.screens.main_screen.image.ImagePicker
import app.vibecast.presentation.screens.main_screen.image.ImageViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.test.*
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class ImageViewModelTest {

    private val imageRepository = mock<ImageRepository>()
    private val imageLoader= mock<ImageLoader>()
    private val imagePicker = mock<ImagePicker>()
    private val imageViewModel = ImageViewModel(imageRepository, imageLoader, imagePicker )
    private lateinit var imageDto : ImageDto
    private lateinit var downLoadUrl : String

    @Before
    fun setup() {
        imageDto =  ImageDto(
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
        downLoadUrl = "https://dummyurl.com/download"
    }

    @Test
    fun testDeleteImage() = runTest {
        imageViewModel.deleteImage(imageDto)
        verify(imageRepository).deleteImage(imageDto)
    }

    @Test
    fun testAddImage() = runTest {
        imageViewModel.addImage(imageDto)
        verify(imageRepository).addImage(imageDto)
    }

    @Test
    fun testGetImageForDownload() = runTest {
        val query = "Seattle rainy"
        whenever(imageRepository.getImageForDownload(query)).thenReturn(flowOf(downLoadUrl))
        val result = imageViewModel.getImageForDownload(query).single()
        verify(imageRepository).getImageForDownload(query)
        assertEquals(result, downLoadUrl)


    }

}
