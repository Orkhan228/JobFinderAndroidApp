package com.example.jobfinderapp.utils

import androidx.lifecycle.LiveData

interface AppPrefs {
    fun setDarkTheme(enabled: Boolean)
    fun isDarkTheme(): Boolean
    fun setCountry(countryCode: String)
    fun getSelectedCountry(): String
    fun observeSelectedCountry(): LiveData<String>
}