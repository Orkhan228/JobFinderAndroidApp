package com.example.jobfinderapp.utils

import kotlinx.coroutines.flow.StateFlow

interface AppPrefs {
    fun setDarkTheme(enabled: Boolean)
    fun isDarkTheme(): Boolean
    fun setCountry(countryCode: String)
    fun getSelectedCountry(): String
    fun observeSelectedCountryFlow(): StateFlow<String>
}