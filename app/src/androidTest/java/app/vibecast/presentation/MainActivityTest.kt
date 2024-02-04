package app.vibecast.presentation

import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import app.vibecast.R
import app.vibecast.data.local.db.AppDatabase
import app.vibecast.data.local.db.image.ImageDao
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var db: AppDatabase
    private lateinit var imageDao: ImageDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        imageDao = db.imageDao()
    }


    @Test
    fun testSaveImageAndNavigateToGalleryFragment() {
        val scenario = launchActivity<MainActivity>()
        scenario.moveToState(Lifecycle.State.RESUMED)
        onView(withId(R.id.action_save_image)).perform(click())
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.nav_pictures)).perform(click())
        Thread.sleep(1000)
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()))


    }

}