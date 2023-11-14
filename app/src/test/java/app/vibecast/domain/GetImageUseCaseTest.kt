package app.vibecast.domain

import app.vibecast.domain.entity.ImageDto
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.usecase.GetImageUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetImageUseCaseTest {

    private val imageRepository = mock<ImageRepository>()
    private val useCase = GetImageUseCase(mock(), imageRepository)
    private lateinit var imageDto: ImageDto

    @Before
    fun setUp() {
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
    fun testProcess() {
        runTest {
            val query = "Seattle rainy"
            val request = GetImageUseCase.Request(query)
            val image = imageDto
            whenever(imageRepository.pickRandomImage(query)).thenReturn(flowOf(image))
            val result = useCase.process(request).first()
            assertEquals(GetImageUseCase.Response(image), result)
        }
    }


}


