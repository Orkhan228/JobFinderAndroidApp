package com.example.jobfinderapp.views.fragments

import android.icu.util.Calendar
import android.os.Bundle
import android.transition.AutoTransition
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.databinding.FragmentNotificationsBinding
import com.example.jobfinderapp.viewModels.NotificationsFragmentViewModel
import com.example.jobfinderapp.views.rv_adapters.NotificationAdapter
import com.example.jobfinderapp.views.rv_helpers.ItemDecReminderRv


class NotificationsFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private val viewModel: NotificationsFragmentViewModel by viewModels()

    private lateinit var notificationAdapter: NotificationAdapter

    private var didRunEnterAnimationForRv = false
    private var currentJobId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = AutoTransition()
        returnTransition = AutoTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val extraRvPadding = requireContext().resources.getDimension(R.dimen.dimenForRVItemsSide).toInt()
        ViewCompat.setOnApplyWindowInsetsListener(binding.notificationsRecyclerView) { v, windowInsets ->

            v.updatePadding(bottom = extraRvPadding)
            windowInsets
        }

        animateBtn(binding.noNotificationsBtn)
        setUpRecycler()
        observeAllReminders()

        binding.noNotificationsBtn.setOnClickListener {
            val action = NotificationsFragmentDirections.actionNotificationsFragmentToHomeFragment()

            findNavController().navigate(action)
        }


        setFragmentResultListener(ReminderDialogFragment.REQUEST_KEY) { _, bundle ->

            val reminderID = bundle.getLong(ReminderDialogFragment.BUNDLE_ID)

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

            val jobId = currentJobId
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

            viewModel.updateReminder(reminder)
        }

    }

    private fun observeAllReminders() {
        viewModel.getAllReminders().observe(viewLifecycleOwner) { remindersList ->

            if (remindersList.isNullOrEmpty()) {
                binding.notificationsRecyclerView.visibility = View.GONE
                showEmptyState(binding.noNotificationsLay, binding.noNotificationsIv, binding.noNotificationsTv, binding.noNotificationsTv1, binding.noNotificationsBtn)
            } else {
                binding.noNotificationsLay.visibility = View.GONE
                binding.notificationsRecyclerView.visibility = View.VISIBLE
            }

            notificationAdapter.submitList(remindersList) {
                if (!didRunEnterAnimationForRv) {
                    binding.notificationsRecyclerView.scheduleLayoutAnimation()
                    didRunEnterAnimationForRv = true
                }
            }
        }
    }

    private fun setUpRecycler() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        notificationAdapter = NotificationAdapter(
            onRootClick = { reminder ->
                val action = NotificationsFragmentDirections.actionNotificationsFragmentToDetailsFragment(
                    jobUIModel = null,
                    jobId = reminder.jobID
                )

                findNavController().navigate(action)
            },
            onEditClick = { reminder ->
                val action = NotificationsFragmentDirections.actionNotificationsFragmentToReminderDialogFragment(
                    isEdit = true,
                    reminderId = reminder.id,
                    reminderTitle = reminder.title,
                    reminderMessage = reminder.message,
                    reminderTriggerTime = reminder.triggerAtMillis
                )

                findNavController().navigate(action)
                currentJobId = reminder.jobID
            },
            onDeleteClick = { reminder ->
                viewModel.deleteReminder(reminder)
            }
        )
        val pT = resources.getDimension(R.dimen.dimenForRVItems).toInt()
        val pS = resources.getDimension(R.dimen.dimenForRVItemsSide).toInt()
        val itemDec = ItemDecReminderRv(pT, pT, pS)

        binding.notificationsRecyclerView.layoutManager = linearLayoutManager
        binding.notificationsRecyclerView.adapter = notificationAdapter
        binding.notificationsRecyclerView.addItemDecoration(itemDec)
    }

    private fun animateBtn(view: View) {

        view.setOnTouchListener { v, event ->
            when (event.action) {
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

                else -> false
            }

        }
    }

    private fun showEmptyState(
        emptyLay: View,
        imageView: ImageView,
        textView: View,
        textView1: View,
        browseBtn: View
    ) {

        if (emptyLay.isVisible && emptyLay.alpha == 1f) return

        emptyLay.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        imageView.apply {
            alpha = 0f
            scaleX = 0.3f
            scaleY = 0.3f
        }

        textView.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        textView1.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        browseBtn.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
        }

        emptyLay.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(280)
            .start()

        imageView.animate()
            .alpha(1f)
            .setDuration(180)
            .start()

        SpringAnimation(imageView, SpringAnimation.SCALE_X, 1f).apply {
            spring = SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            }
            start()
        }

        SpringAnimation(imageView, SpringAnimation.SCALE_Y, 1f).apply {
            spring = SpringForce(1f).apply {
                stiffness = SpringForce.STIFFNESS_LOW
                dampingRatio = SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
            }
            start()
        }

        textView.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)

        textView1.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)

        browseBtn.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(300)

    }

}