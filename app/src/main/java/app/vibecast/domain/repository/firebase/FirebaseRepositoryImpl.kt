package app.vibecast.domain.repository.firebase


import app.vibecast.data.local_data.data_source.image.LocalImageDataSource
import app.vibecast.data.local_data.data_source.music.LocalMusicDataSource
import app.vibecast.data.local_data.data_source.weather.LocalLocationDataSource
import app.vibecast.domain.model.FirebaseImage
import app.vibecast.domain.model.FirebaseLocation
import app.vibecast.domain.model.FirebaseResponse
import app.vibecast.domain.model.FirebaseSong
import app.vibecast.domain.model.ImageDto
import app.vibecast.domain.model.LocationDto
import app.vibecast.domain.model.SongDto
import app.vibecast.domain.repository.firebase.util.ImageUriParser
import app.vibecast.domain.util.Constants.COUNTER_REF
import app.vibecast.domain.util.Constants.IMAGES_REF
import app.vibecast.domain.util.Constants.LOCATIONS_REF
import app.vibecast.domain.util.Constants.MUSIC_REF
import app.vibecast.domain.util.Constants.USERS_REF
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.spotify.protocol.types.ImageUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Implementation of [FirebaseRepository]
 *
 * Methods:
 * - [syncImageData] Checks for missing data in local database and firebase database and fills in missing items
 * - [syncLocationData] Checks for missing data in local database and firebase database and fills in missing items
 * - [deleteUserData] Wipes all user data from firebase when user deletes account
 */
class FirebaseRepositoryImpl @Inject constructor(
    private val locationDataSource: LocalLocationDataSource,
    private val imageDataSource: LocalImageDataSource,
    private val musicDataSource: LocalMusicDataSource,
) : FirebaseRepository {

    private val databaseRoot: DatabaseReference = Firebase.database.reference
    private val usersRef: DatabaseReference = databaseRoot.child(USERS_REF)
    private val auth: FirebaseAuth = Firebase.auth
    private val currentUser = auth.currentUser


    override suspend fun syncData() {
        coroutineScope {
            val locationDeferred = async { syncLocationData() }
            val imageDeferred = async { syncImageData() }
            val musicDeferred = async { syncMusicData() }

            locationDeferred.await()
            imageDeferred.await()
            musicDeferred.await()
        }
    }


    private suspend fun syncMusicData() {
        musicDataSource.getAllSavedSongs().combine(getAllSongs()) { localData, firebaseResponse ->
            val localDataCount = localData.size
            firebaseResponse.data?.let { firebaseData ->
                val firebaseCount = firebaseData.size
                if (localDataCount > firebaseCount) {
                    localData.forEach { song ->
                        val containsLocation = firebaseData.any { firebaseSong ->
                            firebaseSong.name == song.name
                        }
                        if (!containsLocation) {
                            addSong(
                                FirebaseSong(
                                    name = song.name,
                                    artist = song.artist,
                                    album = song.album,
                                    albumUri = song.albumUri,
                                    imageUri = song.imageUri.raw ?: ImageUriParser.stripImageUri(
                                        song.imageUri
                                    ),
                                    trackUri = song.trackUri,
                                    artistUri = song.artistUri,
                                    url = song.url,
                                    previewUrl = song.previewUrl
                                )
                            )
                        }
                    }
                }
                if (localDataCount < firebaseCount) {
                    firebaseData.forEach { song ->
                        val containsLocation = localData.any { localSong ->
                            localSong.name == song.name
                        }
                        if (!containsLocation) {
                            musicDataSource.saveSong(
                                SongDto(
                                    name = song.name,
                                    artist = song.artist,
                                    album = song.album,
                                    albumUri = song.albumUri,
                                    imageUri = ImageUri(song.imageUri),
                                    trackUri = song.trackUri,
                                    artistUri = song.artistUri,
                                    url = song.url,
                                    previewUrl = song.previewUrl
                                )
                            )
                        }
                    }
                }
            }
            if (firebaseResponse.exception != null) {
                Timber.tag("firebaseDB")
                    .e("Error fetching Firebase songs: ${firebaseResponse.exception}")
            }
        }.collect {}
    }


    override fun getAllSongs(): Flow<FirebaseResponse<FirebaseSong>> = flow {
        currentUser?.let { user ->
            val musicRef = usersRef.child(user.uid).child(MUSIC_REF)
            try {
                val dataSnapshot = suspendCoroutine<DataSnapshot> { continuation ->
                    musicRef.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(task.result)
                        } else {
                            continuation.resumeWithException(
                                task.exception ?: Exception("Unknown error")
                            )
                        }
                    }
                }
                val songList = dataSnapshot.children.mapNotNull { songSnapshot ->
                    songSnapshot.getValue(FirebaseSong::class.java)
                }
                emit(FirebaseResponse(data = songList))
                Timber.tag("firebaseDB").d("Retrieved songs successfully")
            } catch (e: Exception) {
                emit(FirebaseResponse(exception = e))
                Timber.tag("firebaseDB").e("Error getting songs: $e")
            }
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun addSong(song: FirebaseSong) {
        currentUser?.let { user ->
            val userMusicRef = usersRef.child(user.uid).child(MUSIC_REF)
            userMusicRef.child(song.trackUri).setValue(song)
                .addOnSuccessListener {
                    Timber.tag("firebaseDB").d("Added song successfully")

                }
                .addOnFailureListener { exception ->
                    Timber.tag("firebaseDB").d("Couldn't add song $exception")
                }
        }
    }


    override suspend fun deleteSong(song: FirebaseSong) {
        currentUser?.let { user ->
            val userMusicRef = usersRef.child(user.uid).child(MUSIC_REF)
            val musicItemRef = userMusicRef.child(song.trackUri)
            musicItemRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Timber.tag("firebaseDB").d("Song deleted successfully")
                } else {
                    Timber.tag("firebaseDB").d("Failed to song image: ${task.exception}")
                }
            }
        }
    }

    private suspend fun syncImageData() {
        imageDataSource.getImages()
            .combine(getAllImages()) { localData, firebaseResponse ->
                val localDataCount = localData.size
                firebaseResponse.data?.let { firebaseData ->
                    val firebaseCount = firebaseData.size
                    if (localDataCount > firebaseCount) {
                        localData.forEach { image ->
                            val containsLocation = firebaseData.any { firebaseImage ->
                                firebaseImage.imageId == image.id
                            }
                            if (!containsLocation) {
                                addImage(
                                    FirebaseImage(
                                        imageId = image.id,
                                        imageUrl = image.urls.regular,
                                        timestamp = image.timestamp,
                                        userLink = image.links.user,
                                        downloadUrl = image.links.downloadLink,
                                        userName = image.user.userName,
                                        userRealName = image.user.name,
                                        portfolioUrl = image.user.portfolioUrl ?: ""
                                    )
                                )
                            }
                        }
                    }

                    if (localDataCount < firebaseCount) {
                        firebaseData.forEach { image ->
                            val containsLocation = localData.any { localImage ->
                                localImage.id == image.imageId
                            }
                            if (!containsLocation) {
                                imageDataSource.addImage(
                                    ImageDto(
                                        id = image.imageId,
                                        description = null,
                                        altDescription = null,
                                        urls = ImageDto.PhotoUrls(
                                            full = "",
                                            regular = image.imageUrl,
                                            small = "",
                                            thumb = ""
                                        ),
                                        user = ImageDto.UnsplashUser(
                                            id = "",
                                            userName = image.userName,
                                            name = image.userRealName,
                                            portfolioUrl = image.portfolioUrl
                                        ),
                                        links = ImageDto.PhotoLinks(
                                            user = image.userLink,
                                            downloadLink = image.downloadUrl
                                        ),
                                        timestamp = image.timestamp
                                    )
                                )

                            }
                        }
                    }
                }

                if (firebaseResponse.exception != null) {
                    Timber.tag("firebaseDB")
                        .e("Error fetching Firebase locations: ${firebaseResponse.exception}")
                }
            }.collect {}
    }

    private suspend fun syncLocationData() {

        locationDataSource.getLocations()
            .combine(getAllLocations()) { localData, firebaseResponse ->
                val localDataCount = localData.size
                firebaseResponse.data?.let { firebaseData ->
                    val firebaseCount = firebaseData.size
                    Timber.tag("firebaseDB").d("Firebase data: $firebaseData")

                    if (localDataCount > firebaseCount) {
                        localData.forEach { location ->
                            val containsLocation = firebaseData.any { firebaseLocation ->
                                firebaseLocation.city == location.city
                            }
                            if (!containsLocation) {
                                addLocation(
                                    FirebaseLocation(
                                        id = location.city,
                                        city = location.city,
                                        country = location.country
                                    )
                                )
                            }
                        }
                    }

                    if (localDataCount < firebaseCount) {
                        firebaseData.forEach { location ->
                            val containsLocation = localData.any { localLocation ->
                                localLocation.city == location.city
                            }
                            if (!containsLocation) {
                                locationDataSource.addLocation(
                                    LocationDto(city = location.city, country = location.country)
                                )
                            }
                        }
                    }
                }

                if (firebaseResponse.exception != null) {
                    Timber.tag("firebaseDB")
                        .e("Error fetching Firebase locations: ${firebaseResponse.exception}")
                }
            }.collect {}
    }


    override fun updateImageCount(action: Action) {
        when (action) {
            Action.INSERT -> {
                currentUser?.let { user ->
                    usersRef.child(user.uid).child(COUNTER_REF).child("image_count")
                        .runTransaction(object : Transaction.Handler {
                            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                val currentValue = mutableData.getValue(Int::class.java) ?: 0
                                mutableData.value = currentValue + 1
                                return Transaction.success(mutableData)
                            }

                            override fun onComplete(
                                databaseError: DatabaseError?,
                                committed: Boolean,
                                dataSnapshot: DataSnapshot?,
                            ) {
                                if (databaseError != null) {
                                    Timber.tag("firebaseDb").d("Counter transaction failed.")
                                } else {
                                    Timber.tag("firebaseDb").d("Counter transaction completed.")
                                }
                            }
                        })
                }

            }

            Action.DELETE -> {
                currentUser?.let { user ->
                    usersRef.child(user.uid).child(COUNTER_REF).child("image_count")
                        .runTransaction(object : Transaction.Handler {
                            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                val currentValue = mutableData.getValue(Int::class.java) ?: 0
                                mutableData.value = currentValue - 1
                                return Transaction.success(mutableData)
                            }

                            override fun onComplete(
                                databaseError: DatabaseError?,
                                committed: Boolean,
                                dataSnapshot: DataSnapshot?,
                            ) {
                                if (databaseError != null) {
                                    Timber.tag("firebaseDb").d("Counter transaction failed.")
                                } else {
                                    Timber.tag("firebaseDb").d("Counter transaction completed.")
                                }
                            }
                        })
                }

            }
        }
    }

    override fun updateLocationCount(action: Action) {
        when (action) {
            Action.INSERT -> {
                currentUser?.let { user ->
                    usersRef.child(user.uid).child(COUNTER_REF).child("location_count")
                        .runTransaction(object : Transaction.Handler {
                            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                val currentValue = mutableData.getValue(Int::class.java) ?: 0
                                mutableData.value = currentValue + 1
                                return Transaction.success(mutableData)
                            }

                            override fun onComplete(
                                databaseError: DatabaseError?,
                                committed: Boolean,
                                dataSnapshot: DataSnapshot?,
                            ) {
                                if (databaseError != null) {
                                    Timber.tag("firebaseDb").d("Counter transaction failed.")
                                } else {
                                    Timber.tag("firebaseDb").d("Counter transaction completed.")
                                }
                            }
                        })
                }

            }

            Action.DELETE -> {
                currentUser?.let { user ->
                    usersRef.child(user.uid).child(COUNTER_REF).child("location_count")
                        .runTransaction(object : Transaction.Handler {
                            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                                val currentValue = mutableData.getValue(Int::class.java) ?: 0
                                mutableData.value = currentValue - 1
                                return Transaction.success(mutableData)
                            }

                            override fun onComplete(
                                databaseError: DatabaseError?,
                                committed: Boolean,
                                dataSnapshot: DataSnapshot?,
                            ) {
                                if (databaseError != null) {
                                    Timber.tag("firebaseDb").d("Counter transaction failed.")
                                } else {
                                    Timber.tag("firebaseDb").d("Counter transaction completed.")
                                }
                            }
                        })
                }

            }
        }
    }


    override suspend fun deleteUserData() {
        currentUser?.let { user ->
            val userRef = usersRef.child(user.uid)
            userRef.removeValue().addOnCompleteListener { databaseTask ->
                if (databaseTask.isSuccessful) {
                    Timber.tag("firebaseDB").d("Deleted user data successfully")

                } else {
                    Timber.tag("firebaseDB").d("Failed user data deletion successfully")
                }
            }
        }
    }


    override suspend fun deleteImage(image: FirebaseImage) {
        currentUser?.let { user ->
            val userImageRef = usersRef.child(user.uid).child(IMAGES_REF)
            val imageItemRef = userImageRef.child(image.imageId)
            imageItemRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Timber.tag("firebaseDB").d("Image deleted successfully")
                } else {
                    Timber.tag("firebaseDB").d("Failed to delete image: ${task.exception}")
                }
            }
        }
    }

    override suspend fun deleteLocation(location: FirebaseLocation) {
        currentUser?.let { user ->
            val userLocationsRef = usersRef.child(user.uid).child(LOCATIONS_REF)
            val locationItemRef = userLocationsRef.child(location.id)
            locationItemRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    Timber.tag("firebaseDB").d("Location deleted successfully")
                } else {
                    Timber.tag("firebaseDB").d("Failed to delete location: ${task.exception}")
                }
            }
        }
    }


    override suspend fun addImage(image: FirebaseImage) {
        currentUser?.let { user ->
            val userImagesRef = usersRef.child(user.uid).child(IMAGES_REF)
            userImagesRef.child(image.imageId).setValue(image)
                .addOnSuccessListener {
                    Timber.tag("firebaseDB").d("Added image successfully")

                }
                .addOnFailureListener { exception ->
                    Timber.tag("firebaseDB").d("Couldn't add image $exception")
                }
        }
    }

    override suspend fun addLocation(location: FirebaseLocation) {
        currentUser?.let { user ->
            val userLocationsRef = usersRef.child(user.uid).child(LOCATIONS_REF)
            userLocationsRef.child(location.id).setValue(location)
                .addOnSuccessListener {

                    Timber.tag("firebaseDB").d("Added location successfully")
                }
                .addOnFailureListener { exception ->
                    Timber.tag("firebaseDB").d("Couldn't add location $exception")
                }
        }
    }

    override suspend fun getImage(): FirebaseResponse<FirebaseImage> {
        val response = FirebaseResponse<FirebaseImage>()
        try {
            response.data =
                currentUser?.let { user ->
                    usersRef.child(user.uid).child(IMAGES_REF).get()
                        .await().children.map { snapShot ->
                            snapShot.getValue(FirebaseImage::class.java)!!
                        }
                }
        } catch (exception: Exception) {
            response.exception = exception
        }
        return response
    }


    override suspend fun getLocation(): FirebaseResponse<FirebaseLocation> {
        val response = FirebaseResponse<FirebaseLocation>()
        try {
            response.data =
                currentUser?.let { user ->
                    usersRef.child(user.uid).child(LOCATIONS_REF).get()
                        .await().children.map { snapShot ->
                            snapShot.getValue(FirebaseLocation::class.java)!!
                        }
                }
        } catch (exception: Exception) {
            response.exception = exception
        }
        return response
    }


    override fun getAllImages(): Flow<FirebaseResponse<FirebaseImage>> = flow {
        currentUser?.let { user ->
            val imagesRef = usersRef.child(user.uid).child(IMAGES_REF)
            try {
                val dataSnapshot = suspendCoroutine<DataSnapshot> { continuation ->
                    imagesRef.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(task.result)
                        } else {
                            continuation.resumeWithException(
                                task.exception ?: Exception("Unknown error")
                            )
                        }
                    }
                }
                val imagesList = dataSnapshot.children.mapNotNull { imageSnapShot ->
                    imageSnapShot.getValue(FirebaseImage::class.java)
                }
                emit(FirebaseResponse(data = imagesList))
                Timber.tag("firebaseDB").d("Retrieved images successfully")
            } catch (e: Exception) {
                emit(FirebaseResponse(exception = e))
                Timber.tag("firebaseDB").e("Error getting images: $e")
            }
        }
    }.flowOn(Dispatchers.IO)


    override fun getAllLocations(): Flow<FirebaseResponse<FirebaseLocation>> = flow {
        currentUser?.let { user ->
            val locationsRef = usersRef.child(user.uid).child(LOCATIONS_REF)
            try {
                val dataSnapshot = suspendCoroutine<DataSnapshot> { continuation ->
                    locationsRef.get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            continuation.resume(task.result)
                        } else {
                            continuation.resumeWithException(
                                task.exception ?: Exception("Unknown error")
                            )
                        }
                    }
                }
                val locationList = dataSnapshot.children.mapNotNull { locationSnapshot ->
                    locationSnapshot.getValue(FirebaseLocation::class.java)
                }
                emit(FirebaseResponse(data = locationList))
                Timber.tag("firebaseDB").d("Retrieved locations successfully")
            } catch (e: Exception) {
                emit(FirebaseResponse(exception = e))
                Timber.tag("firebaseDB").e("Error getting locations: $e")
            }
        }
    }.flowOn(Dispatchers.IO)

}




