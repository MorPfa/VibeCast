package app.vibecast.presentation.screens.main_screen.image

import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.repository.ImageRepository
import app.vibecast.domain.util.TAGS
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class ImageViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
    private val imageLoader: ImageLoader,
    private val imagePicker: ImagePicker,
    ): ViewModel() {

    val galleryImages: LiveData<List<ImageDto>> = imageRepository.getLocalImages().asLiveData()

    fun deleteImage(imageDto: ImageDto) {
        viewModelScope.launch {
            try {
                imageRepository.deleteImage(imageDto)
            } catch (e: Exception) {
                Log.e(TAGS.IMAGE_ERROR,e.toString())
                throw e
            }
        }
    }

    /**
     * Queries repository for download URL of an image
     */

    fun getImageForDownload(query: String) : Flow<String> = flow {
        imageRepository.getImageForDownload(query).collect{
            emit(it)
        }

    }

    fun addImage(imageDto: ImageDto) {
        viewModelScope.launch {
            try {
                imageRepository.addImage(imageDto)
            } catch (e: Exception) {
                Log.e(TAGS.IMAGE_ERROR,e.toString())
                throw e
            }
        }
    }

    private val _image = MutableLiveData<ImageDto>()
    val image: LiveData<ImageDto> get() = _image

    private val _imageCount = MutableLiveData(0)

    val imageCount = _imageCount


    /**
     * Keeps track of the number of saved images
     */
    fun setImageCountLiveData(){
        viewModelScope.launch {
            imageRepository.getLocalImages().collect{
                withContext(Dispatchers.Main){
                    _imageCount.value = it.size
                }
            }
        }

    }

    /**
     * Picks image saved on users phone by default in case none can be fetched from the remote datasource
     */
    fun pickDefaultImage(weatherCondition: String) : Int = imagePicker.pickDefaultImage(weatherCondition)


    fun loadImageIntoImageView(url: String, imageView: ImageView) {
        imageLoader.loadUrlIntoImageView(url, imageView)
    }

    fun loadImage(query: String, weatherCondition: String): Flow<ImageDto?> = flow {
        emitAll(imagePicker.pickImage(query, weatherCondition).flowOn(Dispatchers.IO))
    }.catch { e ->
        Log.e(TAGS.IMAGE_ERROR, "Error loading image: $e in ViewModel")
        throw e
    }

    fun setImageLiveData(image: ImageDto) {
        _image.value = image
    }
}