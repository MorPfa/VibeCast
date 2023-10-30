package app.vibecast.domain

import app.vibecast.domain.entity.Result
import app.vibecast.domain.entity.UseCaseException
import app.vibecast.domain.usecase.UseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock

class UseCaseTest {

    @ExperimentalCoroutinesApi
    private val configuration = UseCase.Configuration(UnconfinedTestDispatcher())
    private val request = mock<UseCase.Request>()
    private val response = mock<UseCase.Response>()

    @ExperimentalCoroutinesApi
    private lateinit var useCase: UseCase<UseCase.Request, UseCase.Response>


    @ExperimentalCoroutinesApi
    @Before

    fun setUp(){
        useCase = object : UseCase<UseCase.Request, UseCase.Response>(configuration){
            override fun process(request: Request): Flow<Response> {
               assertEquals(this@UseCaseTest.request, request)
                return  flowOf(response)
            }
        }

    }

    @ExperimentalCoroutinesApi
    @Test
    fun testExecuteSuccess() = runTest {
        val result = useCase.execute(request).first()
        assertEquals(Result.Success(response),result)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun testExecuteWeatherException(){
        useCase = object : UseCase<UseCase.Request, UseCase.Response>(configuration) {
            override fun process(request: Request): Flow<Response> {
                assertEquals(this@UseCaseTest.request, request)
                return flow {
                    throw UseCaseException.WeatherException(Throwable())
                }
            }

        }

        runTest {
            val result = useCase.execute(request).first()
            assertTrue((result as Result.Error).exception is UseCaseException.WeatherException)
        }

    }


    @ExperimentalCoroutinesApi
    @Test
    fun testExecuteUnknownException() {
        useCase = object : UseCase<UseCase.Request, UseCase.Response>(configuration) {
            override fun process(request: Request): Flow<Response> {
                assertEquals(this@UseCaseTest.request, request)
                return flow {
                    throw Throwable()
                }
            }

        }
        runTest {
            val result = useCase.execute(request).first()
            assertTrue((result as Result.Error).exception is UseCaseException.UnknownException)
        }
    }


}