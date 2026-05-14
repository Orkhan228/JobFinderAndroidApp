package com.example.jobfinderapp.utils

import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class AppPrefsImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : AppPrefs {

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_COUNTRY_SELECT = "country_selection"
    }

    private val selectedCountryFlow = MutableStateFlow<String>(getSelectedCountry())

    override fun setCountry(countryCode: String) {
        sharedPreferences.edit { putString(KEY_COUNTRY_SELECT, countryCode) }

        selectedCountryFlow.update {
            countryCode
        }
    }

    override fun getSelectedCountry(): String {
        return sharedPreferences.getString(KEY_COUNTRY_SELECT, "gb") ?: "gb"
    }

    override fun observeSelectedCountryFlow(): StateFlow<String> = selectedCountryFlow

    override fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_DARK_THEME, enabled) }
    }

    override fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }
}