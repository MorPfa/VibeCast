package app.vibecast.presentation.state

import app.vibecast.domain.model.ImageDto

data class ImageState(
    val image : ImageDto? = null,
    val query : String? = null,
    val error : String? = null
)
