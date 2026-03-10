package com.example.jobfinderapp.views.fragments

import android.os.Bundle
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobWithSaved
import com.example.jobfinderapp.databinding.FragmentDetailsBinding
import com.example.jobfinderapp.utils.JobCountries
import com.example.jobfinderapp.viewModels.DetailsFragmentViewModel

class DetailsFragment : Fragment() {

    private val args: DetailsFragmentArgs by navArgs()
    private val viewModel: DetailsFragmentViewModel by activityViewModels()
    private lateinit var binding: FragmentDetailsBinding

    private var saveMenuItem: MenuItem? = null
    private var isSaved = false // текущее состояние избранного

    private lateinit var currentJob: JobWithSaved

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentJob = args.jobWithSaved

        setupToolbar()
        observeJob()
    }

    private fun setupToolbar() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.det_toolbar_menu, menu)
                saveMenuItem = menu.findItem(R.id.det_save_btn)
                updateSaveIcon() // сразу ставим правильную иконку
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.det_save_btn -> {
                        viewModel.toggleSaved(currentJob.job)
                        true
                    }
                    R.id.det_share_btn -> {
                        true
                    }
                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun updateSaveIcon() {
        val drawableRes = if (isSaved) R.drawable.baseline_bookmark_24
        else R.drawable.baseline_bookmark_border_24

        saveMenuItem?.icon = ContextCompat.getDrawable(requireContext(), drawableRes)
    }

    private fun observeJob() {
        val job = currentJob.job

        binding.detJobNameTv.text = job.title
        binding.detCompanyNameTv.text = job.company.display_name
        binding.detCalendarTextView.text = job.createdTime
        binding.detContractTimeTv.text = job.contract_time

        if (job.category.label != "Not specified") {
            binding.detCategoryTv.visibility = View.VISIBLE
            binding.detCategoryTv.text = job.category.label
        } else binding.detCategoryTv.visibility = View.GONE

        binding.detContractTypeTv.text = job.contract_type
        binding.detSalaryRangeTv.text =
            requireContext().getString(R.string.job_salary_single, job.salary_max / 1000)
        binding.detLocationDisplayNameTv.text = job.location.display_name
        binding.detLocationCountryNameTv.text =
            JobCountries.countriesLocationCodeToNorm[job.location.area.getOrNull(0) ?: ""]
        binding.detInfoAboutRoleTv.text = job.description

        // обновляем состояние избранного
        isSaved = currentJob.isSaved
        updateSaveIcon()

    }
}