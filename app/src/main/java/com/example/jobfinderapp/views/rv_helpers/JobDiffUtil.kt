package com.example.jobfinderapp.views.rv_helpers

import androidx.recyclerview.widget.DiffUtil
import com.example.jobfinderapp.data.entity.JobWithSaved

class JobDiffUtil : DiffUtil.ItemCallback<JobWithSaved>() {

    override fun areItemsTheSame(
        oldItem: JobWithSaved,
        newItem: JobWithSaved,
    ): Boolean {
        return oldItem.job.id == newItem.job.id
    }

    override fun areContentsTheSame(
        oldItem: JobWithSaved,
        newItem: JobWithSaved,
    ): Boolean {
        return oldItem == newItem
    }
}