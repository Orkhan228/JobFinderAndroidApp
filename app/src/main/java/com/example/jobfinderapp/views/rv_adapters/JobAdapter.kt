package com.example.jobfinderapp.views.rv_adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.example.jobfinderapp.views.rv_helpers.JobDiffUtil
import com.example.jobfinderapp.R
import com.example.jobfinderapp.databinding.JobItemBinding
import com.example.jobfinderapp.data.entity.JobUIModel

class JobAdapter(private val onClick: (JobUIModel, View) -> Unit, private val onFavClick: (JobUIModel) -> Unit) : ListAdapter<JobUIModel, JobAdapter.JobViewHolder>(JobDiffUtil()) {

    inner class JobViewHolder(private val binding: JobItemBinding) : RecyclerView.ViewHolder(binding.root) {

        //В contractTimeTv и contractTypeTv, я сделал так. Если они не указаны, то берется значение по умолчанию.
        //Еще с salaryTv, сделал через ресурсы, указав целочисленные значения.

        fun bind(jobItem: JobUIModel) {
            val context = binding.root.context

            binding.jobNameTv.text = jobItem.job.title
            binding.jobCompanyNameTv.text = jobItem.job.company.display_name
            binding.locationAddressTv.text = jobItem.job.location.display_name
            binding.contractTimeTv.text = jobItem.job.contract_time

            binding.salaryTv.text = context.getString(R.string.job_salary_single, jobItem.job.salary_max / 1000)
            binding.contractTypeTv.text = jobItem.job.contract_type
            binding.jobDescriptionTv.text = jobItem.job.description

            binding.jobSavedIv.setOnClickListener { v ->
                v.animate().cancel()

                v.animate()
                    .scaleX(0.82f)
                    .scaleY(0.82f)
                    .rotation(-8f)
                    .setDuration(70)
                    .withEndAction {
                        v.animate()
                            .scaleX(1.15f)
                            .scaleY(1.15f)
                            .rotation(8f)
                            .setDuration(110)
                            .withEndAction {
                                onFavClick(jobItem)
                                v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .rotation(0f)
                                    .setDuration(90)
                                    .withEndAction {

                                    }
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }

            binding.jobSavedIv.isSelected = jobItem.isSaved

            binding.root.setOnClickListener {
                onClick(jobItem, binding.jobItemRoot)
            }

            ViewCompat.setTransitionName(binding.jobItemRoot, "job_title${jobItem.job.id}")
        }
    }
  
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): JobViewHolder {
        val binding = JobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JobViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: JobViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

}