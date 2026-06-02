package off.kys.backtalk

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.lifecycle.ProcessLifecycleOwner
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import off.kys.backtalk.di.appModule
import off.kys.backtalk.presentation.activity.MainActivity
import off.kys.backtalk.util.UsageTracker
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import kotlin.system.exitProcess

/**
 * Main application class for Backtalk.
 *
 * This class is responsible for initializing global application state,
 * including the dependency injection framework (Koin).
 */
class App: Application(), ImageLoaderFactory, KoinComponent {

    private val usageTracker: UsageTracker by inject()

    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     *
     * Initializes Koin dependency injection with the application context
     * and the [appModule].
     */
    override fun onCreate() {
        super.onCreate()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleCrash(thread, throwable)
        }

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(usageTracker)
    }

    private fun handleCrash(thread: Thread, throwable: Throwable) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("EXTRA_CRASH_NAME", throwable.javaClass.simpleName)
            putExtra("EXTRA_CRASH_MESSAGE", throwable.localizedMessage)
            putExtra("EXTRA_CRASH_STACKTRACE", Log.getStackTraceString(throwable))
            putExtra("EXTRA_CRASH_THREAD", thread.name)
        }
        startActivity(intent)
        exitProcess(1)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .build()
    }

}