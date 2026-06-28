package com.example.jobfinderapp.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsDataStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val Context.settingsDataStore by preferencesDataStore("settings_data_store_preferences")
    private val dataStore = context.settingsDataStore

    private val longKey = longPreferencesKey("last_update_time")

    suspend fun saveCurrentTimeToDataStore() {
        dataStore.edit { preferences ->
            preferences[longKey] = System.currentTimeMillis()
        }
    }

    suspend fun getLastUpdateTimeFromDataStore(): Long =
        dataStore.data.first()[longKey] ?: 0L

}