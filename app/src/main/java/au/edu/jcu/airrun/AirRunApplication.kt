package au.edu.jcu.airrun

import android.app.Application
import au.edu.jcu.airrun.di.AppContainer
import au.edu.jcu.airrun.di.DefaultAppContainer

class AirRunApplication : Application() {

    /** App-wide dependencies, read by ViewModel factories. */
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
