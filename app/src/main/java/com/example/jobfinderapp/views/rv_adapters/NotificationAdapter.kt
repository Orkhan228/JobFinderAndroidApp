package com.example.jobfinderapp.views.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.databinding.NotificationJobItemBinding
import com.example.jobfinderapp.views.rv_helpers.ReminderDiffUtil

class NotificationAdapter(private val onRootClick: (ReminderEntity) -> Unit, private val onEditClick: (ReminderEntity) -> Unit, private val onDeleteClick: (ReminderEntity) -> Unit)
    : ListAdapter<ReminderEntity, NotificationAdapter.NotificationViewHolder>(ReminderDiffUtil()) {

    inner class NotificationViewHolder(private val binding: NotificationJobItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: ReminderEntity) {
            with(binding) {
                notJobTvRemTitle.text = reminder.title
                notJobTvRemMessage.text = reminder.message

                if (System.currentTimeMillis() >= reminder.triggerAtMillis) {
                    notJobOverdueBtn.visibility = View.VISIBLE
                } else {
                    notJobOverdueBtn.visibility = View.GONE
                }

                notJobEditBtn.setOnClickListener { v ->
                    v.animateClick { onEditClick(reminder) }
                }

                notJobDeleteBtn.setOnClickListener { v ->
                    v.animateClick { onDeleteClick(reminder) }
                }

                root.setOnClickListener { v ->
                    v.animateClick { onRootClick(reminder) }
                }
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): NotificationViewHolder {
        val binding = NotificationJobItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: NotificationViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    private fun View.animateClick(action: () -> Unit) {
        animate().cancel()

        animate()
            .scaleX(0.92f)
            .scaleY(0.92f)
            .setDuration(100)
            .withEndAction {
                action.invoke()
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start()
            }
            .start()
    }
}