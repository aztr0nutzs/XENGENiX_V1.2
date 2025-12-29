package com.xenogenics.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.xenogenics.app.engine.GameSettings
import com.xenogenics.app.engine.JackpotMeters
import com.xenogenics.app.engine.Jackpots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("xenogenics_settings")

class GameDataStore(private val context: Context) {
    private object Keys {
        val soundEnabled = booleanPreferencesKey("sound_enabled")
        val hapticsEnabled = booleanPreferencesKey("haptics_enabled")
        val fastSpin = booleanPreferencesKey("fast_spin")
        val reducedMotion = booleanPreferencesKey("reduced_motion")
        val devToolsEnabled = booleanPreferencesKey("dev_tools_enabled")

        val jackpotMini = intPreferencesKey("jackpot_mini")
        val jackpotMinor = intPreferencesKey("jackpot_minor")
        val jackpotMajor = intPreferencesKey("jackpot_major")
        val jackpotGrand = intPreferencesKey("jackpot_grand")

        val lastSpinHadOutcome = booleanPreferencesKey("last_spin_had_outcome")
        val sessionWagered = longPreferencesKey("session_wagered")
        val sessionReturned = longPreferencesKey("session_returned")
    }

    val settingsFlow: Flow<GameSettings> = context.dataStore.data.map { prefs ->
        GameSettings(
            soundEnabled = prefs[Keys.soundEnabled] ?: true,
            hapticsEnabled = prefs[Keys.hapticsEnabled] ?: true,
            fastSpin = prefs[Keys.fastSpin] ?: false,
            reducedMotion = prefs[Keys.reducedMotion] ?: false,
            devToolsEnabled = prefs[Keys.devToolsEnabled] ?: false
        )
    }

    val metersFlow: Flow<JackpotMeters> = context.dataStore.data.map { prefs ->
        JackpotMeters(
            mini = prefs[Keys.jackpotMini] ?: Jackpots.baseMeters.mini,
            minor = prefs[Keys.jackpotMinor] ?: Jackpots.baseMeters.minor,
            major = prefs[Keys.jackpotMajor] ?: Jackpots.baseMeters.major,
            grand = prefs[Keys.jackpotGrand] ?: Jackpots.baseMeters.grand
        )
    }

    val lastSpinOutcomeFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.lastSpinHadOutcome] ?: false
    }

    val sessionWageredFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.sessionWagered] ?: 0L
    }

    val sessionReturnedFlow: Flow<Long> = context.dataStore.data.map { prefs ->
        prefs[Keys.sessionReturned] ?: 0L
    }

    suspend fun updateSettings(settings: GameSettings) {
        context.dataStore.edit { prefs ->
            prefs[Keys.soundEnabled] = settings.soundEnabled
            prefs[Keys.hapticsEnabled] = settings.hapticsEnabled
            prefs[Keys.fastSpin] = settings.fastSpin
            prefs[Keys.reducedMotion] = settings.reducedMotion
            prefs[Keys.devToolsEnabled] = settings.devToolsEnabled
        }
    }

    suspend fun updateMeters(meters: JackpotMeters) {
        context.dataStore.edit { prefs ->
            prefs[Keys.jackpotMini] = meters.mini
            prefs[Keys.jackpotMinor] = meters.minor
            prefs[Keys.jackpotMajor] = meters.major
            prefs[Keys.jackpotGrand] = meters.grand
        }
    }

    suspend fun setLastSpinOutcome(hasOutcome: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.lastSpinHadOutcome] = hasOutcome
        }
    }

    suspend fun updateSessionStats(wagered: Long, returned: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.sessionWagered] = wagered
            prefs[Keys.sessionReturned] = returned
        }
    }
}
