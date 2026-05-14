package com.example.jobfinderapp.views.fragments

import android.os.Bundle
import android.transition.AutoTransition
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.jobfinderapp.R
import com.example.jobfinderapp.databinding.FragmentSettingsBinding
import com.example.jobfinderapp.viewModels.SettingsFragmentViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding
    private val viewModel: SettingsFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = AutoTransition()
        returnTransition = AutoTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observeRemindersCount()
        observeCountry()
        observeDarkMode()

        setFragmentResultListener(CountrySelectorDialogFragment.REQUEST_KEY) { _, bundle ->
            val selectedCountryCode = bundle.getString(CountrySelectorDialogFragment.BUNDLE_KEY_COUNTRY, "gb")
            viewModel.updateSelectedCountryCode(selectedCountryCode)
            binding.spTbCountryPicker.text = viewModel.getCountryNameByCode(selectedCountryCode)
        }
    }

    private fun observeRemindersCount() {
        binding.settingsRemindersLay.setOnClickListener {
            val action = SettingsFragmentDirections.actionSettingsFragmentToNotificationsFragment()
            findNavController().navigate(action)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.remindersCount.collect { count ->
                    if (count == 0) {
                        binding.spTbReminderCount.text = getString(R.string.no_reminders)
                    } else if (count == 1) {
                        binding.spTbReminderCount.text = StringBuilder(count.toString()).append(" ").append("reminder")
                    } else {
                        binding.spTbReminderCount.text = StringBuilder(count.toString()).append(" ").append("reminders")
                    }
                }
            }
        }
    }

    private fun observeCountry() {
        binding.spTbCountryPicker.text = viewModel.returnSelectedCountryName()

        binding.settingsCountryLay.setOnClickListener {
            val currentSelectedCountryCode = viewModel.getCountryCode()

            val action =
                SettingsFragmentDirections.actionSettingsFragmentToCountrySelectorDialogFragment(
                    selectedCountryCode = currentSelectedCountryCode
                )

            if (findNavController().currentDestination?.id == R.id.settingsFragment) {
                findNavController().navigate(action)
            }
        }
    }

    private fun observeDarkMode() {
        if (viewModel.getIsDarkTheme()) {
            binding.spDarkModeSwitch.isChecked = true
            binding.spTbModePicker.text = getString(R.string.enabled)
        } else {
            binding.spDarkModeSwitch.isChecked = false
            binding.spTbModePicker.text = getString(R.string.disabled)
        }

        binding.spDarkModeSwitch.setOnCheckedChangeListener { _, checked ->
            viewModel.updateDarkMode(checked)

            if (checked) {
                binding.spTbModePicker.text = getString(R.string.enabled)
            } else {
                binding.spTbModePicker.text = getString(R.string.disabled)
            }

            if (checked) {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
            }

        }
    }
}