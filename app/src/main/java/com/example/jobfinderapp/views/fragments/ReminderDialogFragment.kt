package com.example.jobfinderapp.views.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.example.jobfinderapp.R
import com.example.jobfinderapp.databinding.FragmentReminderDialogBinding
import com.google.android.material.transition.MaterialElevationScale


class ReminderDialogFragment : DialogFragment() {

    private var _binding: FragmentReminderDialogBinding? = null
    private val binding get() = _binding!!

    private var selectedYear: Int? = null
    private var selectedMonth: Int? = null
    private var selectedDay: Int? = null

    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null

    private var selectedTimeInMillis: Long? = null

    private var isEditMode: Boolean = false
    private var reminderId: Long? = null

    private val args: ReminderDialogFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialElevationScale(true).apply {
            duration = 320
        }

        returnTransition = MaterialElevationScale(false).apply {
            duration = 150
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)

        _binding = FragmentReminderDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(), // 95% ширины экрана
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        animateBtn(binding.remDiCancelBtn)
        animateBtn(binding.remDiCreateBtn)
        animateBtn(binding.textInputEtDate)
        animateBtn(binding.textInputEtTime)

        readArgs()

        binding.textInputEtDate.setOnClickListener {
            showDatePicker()
        }

        binding.textInputEtTime.setOnClickListener {
            showTimePicker()
        }

        binding.remDiCancelBtn.setOnClickListener {
            dismiss()
        }

        binding.remDiCreateBtn.setOnClickListener {
            sendResult()
        }

        setupTextWatchers()
        updateCreateButtonState()
    }

    private fun readArgs() {
        isEditMode = args.isEdit
        if (!isEditMode) return

        reminderId = args.reminderId
        val title = args.reminderTitle
        val message = args.reminderMessage
        val triggerAt = args.reminderTriggerTime

        selectedTimeInMillis = triggerAt

        val calendar = Calendar.getInstance().apply {
            timeInMillis = triggerAt
        }

        selectedYear = calendar.get(Calendar.YEAR)
        selectedMonth = calendar.get(Calendar.MONTH)
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY)
        selectedMinute = calendar.get(Calendar.MINUTE)

        binding.textInputEt.setText(title)
        binding.textInputEtMessage.setText(message)

        binding.textInputEtDate.setText(
            String.format("%02d.%02d.%04d", selectedDay, selectedMonth!! + 1, selectedYear)
        )

        binding.textInputEtTime.setText(
            String.format("%02d:%02d", selectedHour, selectedMinute)
        )

        binding.remDiCreateBtn.text = getString(R.string.update)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val year = selectedYear ?: calendar.get(Calendar.YEAR)
        val month = selectedMonth ?: calendar.get(Calendar.MONTH)
        val day = selectedDay ?: calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(),{_, y, m, d ->
            selectedYear = y
            selectedMonth = m
            selectedDay = d

            binding.textInputEtDate.setText(
                String.format("%02d.%02d.%04d", d, m + 1, y)
            )

            updateCreateButtonState()
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        val hour = selectedHour ?: calendar.get(Calendar.HOUR_OF_DAY)
        val minute = selectedMinute ?: calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), {_, h, min ->
            selectedHour = h
            selectedMinute = min

            binding.textInputEtTime.setText(
                String.format("%02d:%02d", h, min)
            )

            updateCreateButtonState()
        }, hour, minute, true).show()
    }

    private fun sendResult() {
        val title = binding.textInputEt.text.toString().trim()
        val message= binding.textInputEtMessage.text.toString().trim()

        val year = selectedYear ?: return
        val month = selectedMonth ?: return
        val day = selectedDay ?: return
        val hour = selectedHour ?: return
        val minute = selectedMinute ?: return

        setFragmentResult(REQUEST_KEY, bundleOf(
            BUNDLE_IS_EDIT to isEditMode,
            BUNDLE_ID to reminderId,
            BUNDLE_TITLE to title,
            BUNDLE_MESSAGE to message,
            BUNDLE_YEAR to year,
            BUNDLE_MONTH to month,
            BUNDLE_DAY to day,
            BUNDLE_HOUR to hour,
            BUNDLE_MINUTE to minute
        ))

        dismiss()
    }

    private fun setupTextWatchers() {
        binding.textInputEt.doAfterTextChanged {
            updateCreateButtonState()
        }
    }

    private fun updateCreateButtonState() {
        val titleFilled = !binding.textInputEt.text.isNullOrBlank()
        val dateSelected = selectedYear != null && selectedMonth != null && selectedDay != null
        val timeSelected = selectedHour != null && selectedMinute != null

        binding.remDiCreateBtn.isEnabled = titleFilled && dateSelected && timeSelected
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

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    companion object {
        const val REQUEST_KEY = "reminder_request_key"

        const val BUNDLE_IS_EDIT = "bundle_is_edit"
        const val BUNDLE_ID = "bundle_id"
        const val BUNDLE_TITLE = "bundle_title"
        const val BUNDLE_MESSAGE = "bundle_message"
        const val BUNDLE_YEAR = "bundle_year"
        const val BUNDLE_MONTH = "bundle_month"
        const val BUNDLE_DAY = "bundle_day"
        const val BUNDLE_HOUR = "bundle_hour"
        const val BUNDLE_MINUTE = "bundle_minute"
    }
}