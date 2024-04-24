package app.vibecast.data.remote_data.data_source.image.util


class EmptyApiResponseException(message: String) : Exception(message)
class ImageFetchException(message: String, cause: Throwable? = null) : Exception(message, cause)
