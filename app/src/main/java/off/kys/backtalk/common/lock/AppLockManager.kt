package off.kys.backtalk.common.lock

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Tracks unlocked states by key and enforces timeouts using the global app lifecycle.
 */
class AppLockManager {
    // Maps lock keys to their expiration timestamps in milliseconds
    private val _unlockedKeys = MutableStateFlow<Map<String, Long>>(emptyMap())
    val unlockedKeys: StateFlow<Map<String, Long>> = _unlockedKeys.asStateFlow()

    init {
        // Watch the global app lifecycle. When the app comes to the foreground, purge expired locks.
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    validateTimeouts()
                }
            }
        )
    }

    /**
     * Unlocks a specific key for a given duration.
     */
    fun setUnlocked(key: String, timeoutMillis: Long) {
        val expirationTime = System.currentTimeMillis() + timeoutMillis
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