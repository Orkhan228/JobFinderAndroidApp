package com.example.jobfinderapp.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.jobfinderapp.utils.JobCategories
import com.example.jobfinderapp.utils.JobCountries
import com.example.jobfinderapp.R
import com.example.jobfinderapp.databinding.FragmentFilterModalBotBinding
import com.example.jobfinderapp.viewModels.HomeFragViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FilterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterModalBotBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeFragViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentFilterModalBotBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setAdapters()
    }

    override fun onStart() {
        super.onStart()

        val dialog = dialog as BottomSheetDialog
        val bottomSheet =
            dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)


        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)

            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true

            // Ограничиваем высоту, например 85% экрана
            val height = (resources.displayMetrics.heightPixels * 0.85).toInt()
            it.layoutParams.height = height

        }

    }

    private fun setAdapters() {
        val listCategories = JobCategories.categoriesList
        val categoryListLabel = JobCategories.categoriesList.map { it.label }
        val countryList = JobCountries.countriesList
        val countryNameList = JobCountries.countriesList.map {
            it.name
        }
        val countryLocationCodes = JobCountries.countriesLocationCode

        val adapterCategory = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            categoryListLabel
        )

        val adapterCountry = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            countryNameList
        )

        binding.categoryDropdown.setAdapter(adapterCategory)
        binding.countryDropdown.setAdapter(adapterCountry)

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.filter.collect { currFilter ->
                    binding.fullTimeCb.isChecked = currFilter.onlyFullTime
                    binding.partTimeCb.isChecked = currFilter.onlyPartTime
                    binding.contractTimeCb.isChecked = currFilter.onlyContractJobs
                    binding.permanentTimeCb.isChecked = currFilter.onlyPermanentJobs
                    binding.categoryDropdown.setText(currFilter.category?.label, false)
                    binding.countryDropdown.setText(currFilter.country.name, false)
                    binding.sortSalaryCb.isChecked = currFilter.sortBy != null
                    binding.sortSalaryAscRb.isChecked = currFilter.sortBy != null && currFilter.sortDirection == "up"
                    binding.sortSalaryDescRb.isChecked = currFilter.sortBy != null && currFilter.sortDirection == "down"

                    val countryLocCode = countryLocationCodes[currFilter.country.code] ?: "UK"

                    val locationString = currFilter.locations
                        ?.filter { it != countryLocCode }
                        ?.joinToString(", ") ?: ""

                    if (binding.textInputEt.text.toString() != locationString) {
                        binding.textInputEt.setText(locationString)
                        binding.textInputEt.setSelection(locationString.length)
                    }
                }
            }
        }

        binding.filterCloseBtn.setOnClickListener {
            dismiss()
        }

        binding.fullTimeCb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    binding.partTimeCb.isChecked = false

                    viewModel.onFullTimeChecked(true)
                    viewModel.onPartTimeChecked(false)
                } else {
                    viewModel.onFullTimeChecked(false)
                }
            }
        }

        binding.partTimeCb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    binding.fullTimeCb.isChecked = false

                    viewModel.onPartTimeChecked(true)
                    viewModel.onFullTimeChecked(false)
                } else {
                    viewModel.onPartTimeChecked(false)
                }
            }
        }

        binding.contractTimeCb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    binding.permanentTimeCb.isChecked = false

                    viewModel.onContractChecked(true)
                    viewModel.onPermanentChecked(false)
                } else {
                    viewModel.onContractChecked(false)
                }
            }
        }

        binding.permanentTimeCb.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed) {
                if (isChecked) {
                    binding.contractTimeCb.isChecked = false

                    viewModel.onPermanentChecked(true)
                    viewModel.onContractChecked(false)
                } else {
                    viewModel.onPermanentChecked(false)
                }
            }
        }

        binding.categoryDropdown.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                val selectedCategory = listCategories[position]
                viewModel.onCategoryChecked(selectedCategory)
            }
        }

        binding.textInputEt.doAfterTextChanged { text->

            val locations = (text?.split(",", ".", ";")
                ?.map { it.trim() }
                ?.filter { it.isNotBlank() }
                ?: emptyList()).toMutableList()

            viewModel.onLocationChecked(locations)
        }

        binding.countryDropdown.onItemClickListener = object : AdapterView.OnItemClickListener {
            override fun onItemClick(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long,
            ) {
                val selectedCountry = countryList[position]

                viewModel.onCountryChanged(selectedCountry)
            }
        }

        binding.filterSearchBtn.setOnClickListener {
            if (binding.filterSearchBtn.isEnabled) {
                viewModel.installFilter(true)

                viewModel.applyFilter()

                dismiss()
            }
        }

        binding.filterClearBtn.setOnClickListener {
            viewModel.resetFilter()
            binding.textInputEt.text?.clear()
        }

        binding.sortSalaryCb.setOnCheckedChangeListener { _, isChecked ->
            binding.sortDirectionRg.isVisible = isChecked

            if (!isChecked) {
                binding.sortDirectionRg.clearCheck()
                viewModel.onSortByChanged(false)
                viewModel.onSortDirectionChanged(null)
            } else {
                viewModel.onSortByChanged(true)
                binding.sortSalaryDescRb.isChecked = true
            }
        }

        binding.sortSalaryAscRb.setOnClickListener {
            viewModel.onSortDirectionChanged("up")
        }

        binding.sortSalaryDescRb.setOnClickListener {
            viewModel.onSortDirectionChanged("down")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    viewModel.filterState.collect { isShown ->
                        animateClearBtn(isShown)
                    }
                }

                launch {
                    viewModel.enableSearchButton.collect { hasChanges ->
                        binding.filterSearchBtn.isEnabled = hasChanges
                    }
                }

            }
        }
    }

    private fun animateClearBtn(isShown: Boolean) {
        val clearLay = binding.filterClearLay

        clearLay.animate().cancel()

        if (isShown) {
            if (clearLay.isVisible && clearLay.alpha == 1f) return

            clearLay.visibility = View.VISIBLE
            clearLay.alpha = 0f
            clearLay.translationY = clearLay.height.toFloat()

            clearLay.post {
                clearLay.alpha = 0f
                clearLay.translationY = clearLay.height.toFloat()

                clearLay.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setInterpolator(DecelerateInterpolator())
                    .setDuration(200)
                    .start()
            }

        } else {
            if (clearLay.visibility != View.VISIBLE) return
            clearLay.animate()
                .alpha(0f)
                .translationY(clearLay.height.toFloat())
                .setInterpolator(AccelerateInterpolator())
                .setDuration(200)
                .withEndAction { clearLay.visibility = View.GONE }
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}