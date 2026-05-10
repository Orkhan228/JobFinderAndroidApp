package com.example.jobfinderapp.utils

import android.content.SharedPreferences
import javax.inject.Inject
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class AppPrefsImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : AppPrefs {

    companion object {
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_COUNTRY_SELECT = "country_selection"
    }

    private val selectedCountryLiveData = MutableLiveData<String>(getSelectedCountry())

    override fun setCountry(countryCode: String) {
        sharedPreferences.edit { putString(KEY_COUNTRY_SELECT, countryCode) }

        selectedCountryLiveData.postValue(countryCode)
    }

    override fun getSelectedCountry(): String {
        return sharedPreferences.getString(KEY_COUNTRY_SELECT, "gb") ?: "gb"
    }

    override fun observeSelectedCountry(): LiveData<String> = selectedCountryLiveData


    override fun setDarkTheme(enabled: Boolean) {
        sharedPreferences.edit { putBoolean(KEY_DARK_THEME, enabled) }
    }

    override fun isDarkTheme(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_THEME, false)
    }
}