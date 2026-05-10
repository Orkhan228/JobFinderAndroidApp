package com.example.jobfinderapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.jobfinderapp.domain.InterActor
import com.example.jobfinderapp.utils.AppPrefs
import com.example.jobfinderapp.utils.JobCountries
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(private val interActor: InterActor, private val appPrefs: AppPrefs): ViewModel() {

    val remindersCount: LiveData<Int> = interActor.getFromRemindersAll().map { remindersList ->
        remindersList.size
    }

    fun returnSelectedCountryName(): String  {
        val countryCode = appPrefs.getSelectedCountry()
        return JobCountries.countriesMapNorm[countryCode] ?: "Great Britain"
    }

    fun updateSelectedCountryCode(countryCode: String) {
        appPrefs.setCountry(countryCode)
    }

    fun getCountryCode(): String =
        appPrefs.getSelectedCountry()

    fun updateDarkMode(enabled: Boolean) {
        appPrefs.setDarkTheme(enabled)
    }

    fun getIsDarkTheme(): Boolean {
        return appPrefs.isDarkTheme()
    }

    fun getCountryNameByCode(countryCode: String): String =
        JobCountries.countriesMapNorm[countryCode] ?: "Great Britain"

}