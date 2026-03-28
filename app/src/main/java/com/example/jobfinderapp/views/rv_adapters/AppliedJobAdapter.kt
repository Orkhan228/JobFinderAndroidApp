package com.example.jobfinderapp.views.rv_adapters

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinderapp.R
import com.example.jobfinderapp.data.entity.JobUIModel
import com.example.jobfinderapp.databinding.AppliedJobItemBinding
import com.example.jobfinderapp.utils.toDateString
import com.example.jobfinderapp.utils.toTimeAgo
import com.example.jobfinderapp.views.rv_helpers.JobDiffUtil

class AppliedJobAdapter(private val onClick: (JobUIModel, View) -> Unit, private val withDrawClick: (JobUIModel) -> Unit) : ListAdapter<JobUIModel, AppliedJobAdapter.ViewHolder>(JobDiffUtil()) {

    inner class ViewHolder(private val binding: AppliedJobItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(jobUIModel: JobUIModel) {
            val context = binding.root.context

            binding.appliedJobNameTv.text = jobUIModel.job.title
            if (jobUIModel.isApplied) {
                binding.appliedBadgeLay.visibility = View.VISIBLE
            }
            binding.appliedJobCompanyNameTv.text = jobUIModel.job.company.display_name
            binding.appliedLocationAddressTv.text = jobUIModel.job.location.display_name
            binding.appliedContractTypeTv.text = jobUIModel.job.contract_type
            binding.appliedContractTimeTv.text = jobUIModel.job.contract_time
            binding.appliedSalaryTv.text = context.getString(R.string.job_salary_single, jobUIModel.job.salary_max / 1000)

            val spannablePostedText = SpannableString(jobUIModel.job.createdTime)
            val spl = jobUIModel.job.createdTime.split(" ")
            spannablePostedText.setSpan(StyleSpan(Typeface.BOLD), spl[0].length + 1, jobUIModel.job.createdTime.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.appliedPostedTimeTv.text = spannablePostedText

            if (jobUIModel.job.category.label != "Not specified") {
                binding.appliedJobCategoryTv.visibility = View.VISIBLE
                binding.appliedJobCategoryTv.text = jobUIModel.job.category.label
            }
            if (jobUIModel.appliedTime != null) {
                binding.appliedTimeJobLay.visibility = View.VISIBLE
                binding.appliedTimeTv.text = jobUIModel.appliedTime.toTimeAgo()
            }
            binding.appliedWithdrawBtn.setOnClickListener {
                withDrawClick.invoke(jobUIModel)
            }
            binding.root.setOnClickListener {
                onClick.invoke(jobUIModel, binding.appliedRootLay)
            }

            binding.appliedWithdrawBtn.setOnTouchListener { v, event ->
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

            ViewCompat.setTransitionName(binding.appliedRootLay, "job_title${jobUIModel.job.title}")
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding = AppliedJobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

}


