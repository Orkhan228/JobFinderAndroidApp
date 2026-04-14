package com.example.jobfinderapp.views.rv_helpers

import androidx.recyclerview.widget.DiffUtil
import com.example.jobfinderapp.data.entity.ReminderEntity

class ReminderDiffUtil : DiffUtil.ItemCallback<ReminderEntity>() {
    override fun areItemsTheSame(
        oldItem: ReminderEntity,
        newItem: ReminderEntity,
    ): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(
        oldItem: ReminderEntity,
        newItem: ReminderEntity,
    ): Boolean {
        return oldItem == newItem
    }
}