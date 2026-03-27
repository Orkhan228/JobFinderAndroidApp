package com.example.jobfinderapp.views.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.navArgs
import androidx.transition.AutoTransition
import androidx.transition.ChangeBounds
import androidx.transition.ChangeClipBounds
import androidx.transition.ChangeTransform
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.databinding.FragmentDetailsBinding
import com.example.jobfinderapp.utils.JobCountries
import com.example.jobfinderapp.viewModels.DetailsFragmentViewModel

class DetailsFragment : Fragment() {

    private val args: DetailsFragmentArgs by navArgs()

    private val viewModel: DetailsFragmentViewModel by viewModels()
    private lateinit var binding: FragmentDetailsBinding

    private var saveMenuItem: MenuItem? = null
    private var isSaved = false // текущее состояние избранного

    private var currentJob: JobUIModel? = null
    private var jobIdFromDeepLink: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeClipBounds())

        sharedElementReturnTransition = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeClipBounds())

        enterTransition = Slide(Gravity.BOTTOM).apply {
            addTarget(R.id.det_location_info_lay)
            addTarget(R.id.det_inf_about_role_lay)
            addTarget(R.id.det_applied_job_lay)
        }

        returnTransition = Fade(Fade.OUT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animateApplyBtn()

        postponeEnterTransition()

        ViewCompat.setOnApplyWindowInsetsListener(binding.detAppliedJobLay) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Устанавливаем padding только если фрагмент активен
            if (isAdded && !isRemoving) {
                view.setPadding(0, 0, 0, insets.bottom)
            }

            // Возвращаем CONSUMED, чтобы инсеты не "дергали" FragmentContainerView
            WindowInsetsCompat.CONSUMED
        }

        currentJob = args.jobUIModel
        jobIdFromDeepLink = args.jobId.toString()

        currentJob?.let {
            viewModel.insertToJobsContainer(it)
        }

        if (currentJob == null && jobIdFromDeepLink.isNotEmpty()) {
            viewModel.getSharedJobById(jobIdFromDeepLink).observe(viewLifecycleOwner) {
                viewModel.insertToJobsContainer(it)
            }
        }

        binding.detAppliedJobBtn.setOnClickListener {
            viewModel.toggleApplied(System.currentTimeMillis())
        }

        viewModel.applyState.observe(viewLifecycleOwner) { state ->
            if (state) {
                animateAppliedBtnInf()
            }
        }

        viewModel.jobsContainer.observe(viewLifecycleOwner) {
            currentJob = it
            val job = it.job

            ViewCompat.setTransitionName(binding.detMainCardLay, "job_title${job.title}")

            startPostponedEnterTransition()

            binding.detJobNameTv.text = job.title
            binding.detCompanyNameTv.text = job.company.display_name

            val spannablePostedText = SpannableString(job.createdTime)
            val spl = job.createdTime.split(" ")
            spannablePostedText.setSpan(StyleSpan(Typeface.BOLD), spl[0].length + 1, job.createdTime.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE )
            binding.detCalendarTextView.text = spannablePostedText

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

            updateApplyBtn(it.isApplied)

            // обновляем состояние избранного
            isSaved = it.isSaved
            updateSaveIcon()
        }

        setupToolbar()
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
                        currentJob?.let {
                            viewModel.toggleSaved(it.job)
                        }
                        true
                    }
                    R.id.det_share_btn -> {
                        currentJob?.let {
                            shareJob(it)
                        }
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

    private fun updateApplyBtn(isApplied: Boolean) {
        if (isApplied) {
            binding.detAppliedJobBtn.text = "Applied"
            binding.detAppliedJobBtn.isSelected = true
            binding.detAppliedJobBtn.isClickable = false
            binding.detAppliedJobBtn.isEnabled = false

        } else {
            binding.detAppliedJobBtn.text = "Apply Now"
            binding.detAppliedJobBtn.isSelected = false
            binding.detAppliedJobBtn.isClickable = true
            binding.detAppliedJobBtn.isEnabled = true
        }
    }

    private fun shareJob(currentJobSh: JobUIModel) {
        viewModel.insertSharedJob()

        val job = currentJobSh.job

        val shareText = buildString {
            appendLine(job.title)
            appendLine(job.company.display_name)
            appendLine(job.location.display_name)
            appendLine("Contract type: ${job.contract_type}")
            appendLine("Salary: ${job.salary_max / 1000}K")

            appendLine()
            appendLine("Open in JobFinderApp:")
            appendLine("jobfinder://job/${job.id}")
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, job.title)
            putExtra(Intent.EXTRA_TEXT, shareText)
        }


        try {
            startActivity(Intent.createChooser(intent, "Share job via"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(requireContext(), "Sorry, no such app", Toast.LENGTH_SHORT).show()
        }

    }

    private fun animateAppliedBtnInf() {
        val infoBtn = binding.successApplyBtnInf

        infoBtn.clearAnimation()
        infoBtn.visibility = View.INVISIBLE

        infoBtn.post {
            infoBtn.translationY = -infoBtn.height.toFloat()
            infoBtn.alpha = 0f
            infoBtn.translationZ = 10f

            infoBtn.visibility = View.VISIBLE
            infoBtn.animate()
                .setInterpolator(AccelerateInterpolator())
                .translationY(0f)
                .alpha(1f)
                .setDuration(250)
                .withEndAction { infoBtn.postDelayed({
                    infoBtn.animate()
                        .setInterpolator(DecelerateInterpolator())
                        .translationY(-infoBtn.height.toFloat())
                        .alpha(0f)
                        .setDuration(250)
                        .withEndAction { infoBtn.visibility = View.GONE }
                        .start()
                }, 1800) }
                .start()
        }

    }

    private fun animateApplyBtn() {
        val applyBtn = binding.detAppliedJobBtn

        applyBtn.setOnTouchListener { v, event ->
            if (!v.isEnabled) return@setOnTouchListener false

            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.isPressed = true

                    v.animate()
                        .scaleX(0.92f)
                        .scaleY(0.92f)
                        .setDuration(100)
                        .start()

                    true
                }

                MotionEvent.ACTION_UP -> {
                    v.isPressed = false

                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    v.performClick()
                    true
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.isPressed = false

                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start()

                    true
                }
            }
            false
        }
    }
}