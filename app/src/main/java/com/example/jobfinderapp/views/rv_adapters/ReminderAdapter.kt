package com.example.jobfinderapp.views.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinderapp.data.entity.ReminderEntity
import com.example.jobfinderapp.databinding.DetReminderItemBinding
import com.example.jobfinderapp.views.rv_helpers.ReminderDiffUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAdapter(private val onDeleteClick: (ReminderEntity) -> Unit, private val onEditClick: (ReminderEntity) -> Unit) : ListAdapter<ReminderEntity, ReminderAdapter.ViewHolder>(
    ReminderDiffUtil()
) {

    inner class ViewHolder(private val binding: DetReminderItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reminder: ReminderEntity) {
            with(binding) {
                detReminderItemRName.text = reminder.title
                detReminderItemRDescription.text = reminder.message
                val formatter = SimpleDateFormat("MMM dd, yyyy, HH:mm", Locale.ENGLISH)
                detReminderItemRCalendar.text = formatter.format(Date(reminder.triggerAtMillis))

                detReminderItemBtnEdit.setOnClickListener { v ->
                    v.animateClick { onEditClick(reminder) }
                }

                detReminderItemBtnDelete.setOnClickListener { v ->
                    v.animateClick { onDeleteClick(reminder) }
                }
            }
        }
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val binding = DetReminderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
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