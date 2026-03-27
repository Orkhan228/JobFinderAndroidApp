package com.example.jobfinderapp.views.rv_helpers

import androidx.recyclerview.widget.DiffUtil
import com.example.jobfinderapp.data.entity.JobUIModel

class JobDiffUtil : DiffUtil.ItemCallback<JobUIModel>() {

    override fun areItemsTheSame(
        oldItem: JobUIModel,
        newItem: JobUIModel,
    ): Boolean {
        return oldItem.job.id == newItem.job.id
    }

    override fun areContentsTheSame(
        oldItem: JobUIModel,
        newItem: JobUIModel,
    ): Boolean {
        return oldItem == newItem
    }
}