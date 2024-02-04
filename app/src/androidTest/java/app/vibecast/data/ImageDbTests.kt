package app.vibecast.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.vibecast.data.local.db.AppDatabase
import app.vibecast.data.local.db.image.ImageDao
import app.vibecast.data.local.db.image.ImageEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ImageDbTests {

    private lateinit var db: AppDatabase
    private lateinit var imageDao: ImageDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        imageDao = db.imageDao()
    }


    @After
    fun closeDb(){
        db.close()
    }


    @Test
    fun testAddImage() = runBlocking {
        val image = ImageEntity(
            id = "123456",
            description = "Beautiful landscape",
            altDescription = "test",
            regularUrl = "https://example.com/image.jpg",
            name = "John Doe",
            userId = "789",
            userName = "john_doe",
            portfolioUrl = "https://example.com/john_doe",
            userLink = "test",
            downloadLink = "https://dummyurl.com/raw",
            timestamp = System.currentTimeMillis()
        )

        imageDao.addImage(image)
        val retrievedImage = imageDao.getAllImages().firstOrNull()
        assertNotNull(retrievedImage)
        assertEquals(image, retrievedImage?.get(0))

    }


    @Test
    fun testRemoveImage() = runBlocking {
        val image = ImageEntity(
            id = "123456",
            description = "Beautiful landscape",
            altDescription = "test",
            regularUrl = "https://example.com/image.jpg",
            name = "John Doe",
            userId = "789",
            userName = "john_doe",
            portfolioUrl = "https://example.com/john_doe",
            userLink = "test",
            downloadLink = "https://dummyurl.com/raw",
            timestamp = System.currentTimeMillis()
        )

        imageDao.addImage(image)
        var retrievedImage = imageDao.getAllImages().firstOrNull()
        assertNotNull(retrievedImage)
        assertEquals(image, retrievedImage?.get(0))
        imageDao.deleteImage(image)
        retrievedImage = imageDao.getAllImages().firstOrNull()
        assertTrue(retrievedImage?.isEmpty() == true)

    }

    @Test
    fun testGetAllImages() = runBlocking {
        val image = ImageEntity(
            id = "123456",
            description = "Beautiful landscape",
            altDescription = "test",
            regularUrl = "https://example.com/image.jpg",
            name = "John Doe",
            userId = "789",
            userName = "john_doe",
            portfolioUrl = "https://example.com/john_doe",
            userLink = "test",
            downloadLink = "https://dummyurl.com/raw",
            timestamp = System.currentTimeMillis()
        )

        imageDao.addImage(image)
        val retrievedImage = imageDao.getAllImages().firstOrNull()
        assertNotNull(retrievedImage)
        assertEquals(image, retrievedImage?.get(0))
    }
}