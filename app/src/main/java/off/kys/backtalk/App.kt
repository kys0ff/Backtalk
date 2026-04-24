package off.kys.backtalk

import android.app.Application
import off.kys.backtalk.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Main application class for Backtalk.
 *
 * This class is responsible for initializing global application state,
 * including the dependency injection framework (Koin).
 */
class App: Application() {

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     *
     * Initializes Koin dependency injection with the application context
     * and the [appModule].
     */
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(appModule)
        }
    }

}