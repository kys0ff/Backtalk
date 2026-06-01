package off.kys.backtalk.common.lock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import off.kys.backtalk.common.pref.BacktalkPreferences

/**
 * Tracks unlocked states by key and enforces timeouts using the global app lifecycle.
 */
class AppLockManager(
    private val context: Context,
    private val preferences: BacktalkPreferences
) {
    // Maps lock keys to their expiration timestamps in milliseconds
    private val _unlockedKeys = MutableStateFlow<Map<String, Long>>(emptyMap())
    val unlockedKeys: StateFlow<Map<String, Long>> = _unlockedKeys.asStateFlow()

    private var backgroundTimestamp: Long = 0L

    object Keys {
        const val MAIN = "main_app_lock"
        const val SENSITIVE = "sensitive_action_lock"
    }

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_SCREEN_OFF && preferences.lockOnScreenOff) {
                lock(Keys.MAIN)
            }
        }
    }

    init {
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        ContextCompat.registerReceiver(
            context,
            screenOffReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // Watch the global app lifecycle. When the app comes to the foreground, purge expired locks.
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    if (backgroundTimestamp != 0L) {
                        val backgroundDuration = System.currentTimeMillis() - backgroundTimestamp
                        if (backgroundDuration > preferences.lockTimeoutMillis) {
                            lock(Keys.MAIN)
                        }
                    }
                    backgroundTimestamp = 0L
                    validateTimeouts()
                }

                override fun onStop(owner: LifecycleOwner) {
                    backgroundTimestamp = System.currentTimeMillis()
                }
            }
        )
    }

    /**
     * Unlocks a specific key for a given duration.
     */
    fun setUnlocked(key: String, durationMillis: Long = Long.MAX_VALUE) {
        val expirationTime = if (durationMillis == Long.MAX_VALUE) {
            Long.MAX_VALUE
        } else {
            System.currentTimeMillis() + durationMillis
        }
        _unlockedKeys.update { current ->
            current + (key to expirationTime)
        }
    }

    /**
     * Checks if a key is currently unlocked. Re-locks if the timeout has passed.
     */
    fun isUnlocked(key: String): Boolean {
        val expiration = _unlockedKeys.value[key] ?: return false
        if (System.currentTimeMillis() > expiration) {
            lock(key)
            return false
        }
        return true
    }

    /**
     * Manually locks a key.
     */
    fun lock(key: String) {
        _unlockedKeys.update { current -> current - key }
    }

    private fun validateTimeouts() {
        val now = System.currentTimeMillis()
        _unlockedKeys.update { current ->
            current.filterValues { expiration -> expiration > now }
        }
    }
}