package app.vibecast

import android.app.Application
import app.vibecast.domain.util.CrashlyticsTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import timber.log.Timber.*

@HiltAndroidApp
class VibeCastApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initTimber()
    }


    private fun initTimber() = when {


        BuildConfig.DEBUG -> {
            Timber.plant(object : DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String {
                    return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
                }
            })
        }

        else -> {
            Timber.plant(CrashlyticsTree())
        }
    }
}