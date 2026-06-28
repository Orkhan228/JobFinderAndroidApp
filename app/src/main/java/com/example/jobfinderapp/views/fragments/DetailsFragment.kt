package com.example.jobfinderapp.views.fragments

import android.Manifest
import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.ChangeBounds
import androidx.transition.ChangeClipBounds
import androidx.transition.ChangeTransform
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.TransitionSet
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.databinding.FragmentDetailsBinding
import com.example.jobfinderapp.utils.JobCountries
import com.example.jobfinderapp.viewModels.DetailsFragmentViewModel
import com.example.jobfinderapp.views.rv_adapters.ReminderAdapter
import com.example.jobfinderapp.views.rv_helpers.ItemDecReminderRv
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.jobfinderapp.utils.AppLogger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    private val args: DetailsFragmentArgs by navArgs()

    private val viewModel: DetailsFragmentViewModel by viewModels()

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private var saveMenuItem: MenuItem? = null
    private var isSaved = false // текущее состояние избранного

    private var currentJob: JobUIModel? = null
    private var jobIdFromDeepLink: String = ""

    private var backCallback: OnBackPressedCallback? = null

    private lateinit var reminderAdapter: ReminderAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeClipBounds())
            .excludeTarget(R.id.det_toolbar, true)

        sharedElementReturnTransition = TransitionSet()
            .addTransition(ChangeBounds())
            .addTransition(ChangeTransform())
            .addTransition(ChangeClipBounds())
            .excludeTarget(R.id.det_toolbar, true)

        enterTransition = Slide(Gravity.BOTTOM).apply {
            addTarget(R.id.det_location_info_lay)
            addTarget(R.id.det_inf_about_role_lay)
            addTarget(R.id.det_applied_job_lay)
            excludeTarget(R.id.det_toolbar, true)
        }

        returnTransition = Fade(Fade.MODE_OUT).apply {
            excludeTarget(R.id.det_toolbar, true)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.detAppBarLay.apply {
            visibility = View.VISIBLE
            alpha = 1f
        }

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

        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                binding.detAppBarLay.animate().cancel()
                binding.detAppBarLay.visibility = View.GONE
                binding.detAppBarDivider.visibility = View.GONE
                isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback!!)

        setupToolbar()

        currentJob = args.jobUIModel
        jobIdFromDeepLink = args.jobId.toString()

        currentJob?.let {
            viewModel.insertToJobsContainer(it)
        }

        if (currentJob == null && jobIdFromDeepLink.isNotEmpty()) {
            viewModel.getSharedJobByJobIdOnce(jobIdFromDeepLink)
        }

        setReminderRecycler()

        observeReminders()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.applyState.collect { state ->
                    animateAppliedBtnInf()
                }
            }
        }

        binding.detReminderCreateBtn.setOnClickListener {
            if (ensureExactAlarmPermission()) {
                requestNotificationPermissionIfNeeded()
                val action = DetailsFragmentDirections.actionDetailsFragmentToReminderDialogFragment()

                findNavController().navigate(action)
            }
        }

        setFragmentResultListener(ReminderDialogFragment.REQUEST_KEY) { _, bundle ->

            val reminderID = bundle.getLong(ReminderDialogFragment.BUNDLE_ID)
            val isEdit = bundle.getBoolean(ReminderDialogFragment.BUNDLE_IS_EDIT)

            val title = bundle.getString(ReminderDialogFragment.BUNDLE_TITLE).orEmpty()
            val message = bundle.getString(ReminderDialogFragment.BUNDLE_MESSAGE).orEmpty()

            val year = bundle.getInt(ReminderDialogFragment.BUNDLE_YEAR)
            val month = bundle.getInt(ReminderDialogFragment.BUNDLE_MONTH)
            val day = bundle.getInt(ReminderDialogFragment.BUNDLE_DAY)
            val hour = bundle.getInt(ReminderDialogFragment.BUNDLE_HOUR)
            val minute = bundle.getInt(ReminderDialogFragment.BUNDLE_MINUTE)

            val triggerTime = Calendar.getInstance().apply {
                set(year, month, day, hour, minute, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val jobId = currentJob?.job?.id
            if (jobId == null) {
                Toast.makeText(requireContext(), "Job is not loaded yet", Toast.LENGTH_SHORT).show()
                return@setFragmentResultListener
            }

            val reminder = ReminderEntity(
                id = reminderID,
                jobID = jobId,
                title = title,
                message = message,
                triggerAtMillis = triggerTime,
            )

            if (isEdit) {
                viewModel.updateReminderInTable(reminder)
            } else {
                viewModel.insertToReminders(reminder)
            }
        }

        binding.detAppliedJobBtn.setOnClickListener {
            viewModel.toggleApplied(System.currentTimeMillis())
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.jobsContainer.collect { jobUIModel ->
                    jobUIModel?.let {
                        currentJob = it
                        val job = it.job

                        ViewCompat.setTransitionName(binding.detMainCardLay, "job_title${job.id}")

                        startPostponedEnterTransition()

                        binding.detJobNameTv.text = job.title
                        binding.detCompanyNameTv.text = job.company.display_name

                        val spannablePostedText = SpannableString(job.createdTime)
                        val spl = job.createdTime.split(" ")
                        spannablePostedText.setSpan(
                            StyleSpan(Typeface.BOLD), spl[0].length + 1, job.createdTime.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding.detCalendarTextView.text = spannablePostedText

                        binding.detContractTimeTv.text = job.contract_time

                        if (job.category.label != "Not specified") {
                            binding.detCategoryTv.visibility = View.VISIBLE
                            binding.detCategoryTv.text = job.category.label
                        } else binding.detCategoryTv.visibility = View.GONE

                        binding.detContractTypeTv.text = job.contract_type
                        binding.detSalaryRangeTv.text =
                            requireContext().getString(
                                R.string.job_salary_single,
                                job.salary_max / 1000
                            )
                        binding.detLocationDisplayNameTv.text = job.location.display_name
                        binding.detLocationCountryNameTv.text =
                            JobCountries.countriesLocationCodeToNorm[job.location.area.getOrNull(0)
                                ?: ""]
                        binding.detInfoAboutRoleTv.text = job.description

                        updateApplyBtn(it.isApplied)

                        // обновляем состояние избранного
                        isSaved = it.isSaved
                        updateSaveIcon()

                        viewModel.updateJobId(job.id)

                    } ?: AppLogger.e("Details Fragment", "No job for details")
                }

            }
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            AppLogger.d("DetailsFragment", "POST_NOTIFICATIONS granted = $granted" )
        }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun ensureExactAlarmPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager =
                requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${requireContext().packageName}".toUri()
                }

                startActivity(intent)
                return false
            }
        }
        return true
    }

    private fun observeReminders() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.reminderFlow.collect { remindersList ->
                    if (remindersList.isEmpty()) {
                        binding.detReminderRecyclerView.visibility = View.GONE
                        binding.detReminderNoRemTv.visibility = View.VISIBLE
                    } else {
                        binding.detReminderNoRemTv.visibility = View.GONE
                        binding.detReminderRecyclerView.visibility = View.VISIBLE
                    }

                    reminderAdapter.submitList(remindersList)
                }
            }
        }
    }

    private fun setReminderRecycler() {
        val layoutManager = LinearLayoutManager(requireContext())

        val pT = resources.getDimension(R.dimen.dimenForRVItems).toInt()
        val pS = resources.getDimension(R.dimen.remindersRVPaddingSides).toInt()
        val itemDec = ItemDecReminderRv(
            paddingTop = pT,
            paddingBottom = pT,
            paddingSides = pS
        )

        reminderAdapter = ReminderAdapter(
            onDeleteClick = { reminder ->
                viewModel.deleteFromReminders(reminder)
            } ,
            onEditClick = { reminder ->
                val action = DetailsFragmentDirections.actionDetailsFragmentToReminderDialogFragment(
                    reminderId = reminder.id,
                    reminderTitle = reminder.title,
                    reminderMessage = reminder.message,
                    reminderTriggerTime = reminder.triggerAtMillis,
                    isEdit = true
                )

                findNavController().navigate(action)
            }
        )
        binding.detReminderRecyclerView.layoutManager = layoutManager
        binding.detReminderRecyclerView.adapter = reminderAdapter
        binding.detReminderRecyclerView.addItemDecoration(itemDec)
    }

    private fun setupToolbar() {
        binding.detToolbar.setNavigationOnClickListener { _ ->
            backCallback?.handleOnBackPressed()
        }

        binding.detToolbar.inflateMenu(R.menu.det_toolbar_menu)

        saveMenuItem = binding.detToolbar.menu.findItem(R.id.det_save_btn)
        updateSaveIcon()

        binding.detToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
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
    }

    private fun updateSaveIcon() {
        val drawableRes = if (isSaved) R.drawable.baseline_bookmark_24
        else R.drawable.baseline_bookmark_border_24

        saveMenuItem?.icon = ContextCompat.getDrawable(requireContext(), drawableRes)
    }

    private fun updateApplyBtn(isApplied: Boolean) {
        if (isApplied) {
            binding.detAppliedJobBtn.text = resources.getString(R.string.applied)
            binding.detAppliedJobBtn.isSelected = true
            binding.detAppliedJobBtn.isClickable = false
            binding.detAppliedJobBtn.isEnabled = false

        } else {
            binding.detAppliedJobBtn.text = resources.getString(R.string.apply_now)
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
            Log.e("DetailsFragment", "No app to share with", e)
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

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}