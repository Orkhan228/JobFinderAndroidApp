package com.example.jobfinderapp.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jobfinderapp.domain.InterActor
import com.example.jobfinderapp.utils.AppPrefs
import com.example.jobfinderapp.utils.JobCountries
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SettingsFragmentViewModel @Inject constructor(private val interActor: InterActor, private val appPrefs: AppPrefs): ViewModel() {

    val remindersCount = interActor.getFromRemindersAll()
        .map { remindersList ->
            remindersList.size
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

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