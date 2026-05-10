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
import com.example.jobfinderapp.utils.JobCategories
import com.example.jobfinderapp.utils.JobCountries
import com.example.jobfinderapp.R
import com.example.jobfinderapp.databinding.FragmentFilterModalBotBinding
import com.example.jobfinderapp.entity.JobSortType
import com.example.jobfinderapp.viewModels.HomeFragViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FilterBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentFilterModalBotBinding

    private val viewModel: HomeFragViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentFilterModalBotBinding.inflate(layoutInflater, container, false)
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

        viewModel.filter.observe(viewLifecycleOwner) { currFilter ->

            binding.fullTimeCb.isChecked = currFilter.onlyFullTime
            binding.partTimeCb.isChecked = currFilter.onlyPartTime
            binding.contractTimeCb.isChecked = currFilter.onlyContractJobs
            binding.permanentTimeCb.isChecked = currFilter.onlyPermanentJobs
            binding.categoryDropdown.setText(currFilter.category?.label, false)
            binding.countryDropdown.setText(currFilter.country.name, false)
            binding.sortSalaryCb.isChecked = currFilter.sortBy != JobSortType.DEFAULT
            binding.sortSalaryAscRb.isChecked = currFilter.sortBy == JobSortType.SALARY_ASC
            binding.sortSalaryDescRb.isChecked = currFilter.sortBy == JobSortType.SALARY_DESC

            val countryLocCode = countryLocationCodes[currFilter.country.code] ?: "UK"

            val locationString = currFilter.locations
                ?.filter { it != countryLocCode }
                ?.joinToString(", ") ?: ""

            if (binding.textInputEt.text.toString() != locationString) {
                binding.textInputEt.setText(locationString)
                binding.textInputEt.setSelection(locationString.length)
            }
        }

        binding.filterCloseBtn.setOnClickListener {
            dismiss()
        }

        binding.fullTimeCb.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onFullTimeChecked(isChecked)
        }

        binding.partTimeCb.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onPartTimeChecked(isChecked)
        }

        binding.contractTimeCb.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onContractChecked(isChecked)
        }

        binding.permanentTimeCb.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onPermanentChecked(isChecked)
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
                viewModel.onSortByChecked(JobSortType.DEFAULT)
            } else {
                viewModel.onSortByChecked(JobSortType.SALARY_DESC)
                binding.sortSalaryDescRb.isChecked = true
            }
        }

        binding.sortSalaryAscRb.setOnClickListener {
            viewModel.onSortByChecked(JobSortType.SALARY_ASC)
        }

        binding.sortSalaryDescRb.setOnClickListener {
            viewModel.onSortByChecked(JobSortType.SALARY_DESC)
        }

        viewModel.filterState.observe(viewLifecycleOwner) { isShown ->
            animateClearBtn(isShown)
        }

        viewModel.hasPendingFilterChanges.observe(viewLifecycleOwner) { hasChanges ->
            binding.filterSearchBtn.isEnabled = hasChanges
        }
    }

    private fun animateClearBtn(isShown: Boolean) {
        val clearLay = binding.filterClearLay

        clearLay.animate().cancel()

        if (isShown) {
            if (clearLay.visibility == View.VISIBLE && clearLay.alpha == 1f) return

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

}