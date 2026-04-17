package com.example.jobfinderapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.jobfinderapp.App
import com.example.jobfinderapp.domain.InterActor
import com.example.jobfinderapp.utils.AppPrefs
import com.example.jobfinderapp.utils.JobCountries
import javax.inject.Inject

class SettingsFragmentViewModel : ViewModel() {

    @Inject
    lateinit var interActor: InterActor

    @Inject
    lateinit var appPrefs: AppPrefs

    init {
        App.instance.appComponent.inject(this)


    }

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