package com.example.jobfinderapp.views.rv_adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinderapp.views.rv_helpers.JobDiffUtil
import com.example.jobfinderapp.R
import com.example.jobfinderapp.databinding.JobItemBinding
import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.data.entity.JobWithSaved

class JobAdapter(private val onClick: (JobWithSaved) -> Unit, private val onFavClick: (JobWithSaved) -> Unit) : ListAdapter<JobWithSaved, JobAdapter.JobViewHolder>(JobDiffUtil()) {

    inner class JobViewHolder(private val binding: JobItemBinding) : RecyclerView.ViewHolder(binding.root) {

        //В contractTimeTv и contractTypeTv, я сделал так. Если они не указаны, то берется значение по умолчанию.
        //Еще с salaryTv, сделал через ресурсы, указав целочисленные значения.

        fun bind(jobItem: JobWithSaved) {
            val context = binding.root.context

            binding.jobNameTv.text = jobItem.job.title
            binding.jobCompanyNameTv.text = jobItem.job.company.display_name
            binding.locationAddressTv.text = jobItem.job.location.display_name
            binding.contractTimeTv.text = jobItem.job.contract_time

            binding.salaryTv.text = context.getString(R.string.job_salary_single, jobItem.job.salary_max / 1000)
            binding.contractTypeTv.text = jobItem.job.contract_type
            binding.jobDescriptionTv.text = jobItem.job.description

            binding.jobSavedIv.setOnClickListener {
                onFavClick(jobItem)
            }

            binding.jobSavedIv.setImageResource(
                if (jobItem.isSaved) R.drawable.baseline_bookmark_24
                else R.drawable.baseline_bookmark_border_24
            )

            binding.root.setOnClickListener {
                onClick(jobItem)
            }
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