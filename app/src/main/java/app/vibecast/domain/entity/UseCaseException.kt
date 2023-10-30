package app.vibecast.domain.entity

sealed class UseCaseException(cause : Throwable) : Throwable(cause){

    class UnknownException(cause: Throwable) : UseCaseException(cause)

    class WeatherException(cause: Throwable) : UseCaseException(cause)

    companion object {
        fun createFromThrowable(throwable: Throwable) : UseCaseException {
            return if (throwable is UseCaseException) throwable else UnknownException(throwable)
        }
    }
}
