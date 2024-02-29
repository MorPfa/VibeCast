package app.vibecast.presentation

import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.vibecast.presentation.screens.main_screen.MainScreenFragment
import org.junit.After
import org.junit.Before

import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainScreenFragmentTest {


    private lateinit var scenario: FragmentScenario<MainScreenFragment>


    @Before
    fun setup() {
        scenario = launchFragmentInContainer()
        scenario.moveToState(Lifecycle.State.STARTED)

    }

    @After
    fun tearDown(){

    }





}