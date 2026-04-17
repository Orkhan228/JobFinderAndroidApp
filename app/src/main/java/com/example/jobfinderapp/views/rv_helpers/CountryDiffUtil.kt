package com.example.jobfinderapp.views.rv_helpers

import androidx.recyclerview.widget.DiffUtil
import com.example.jobfinderapp.entity.Country

class CountryDiffUtil : DiffUtil.ItemCallback<Country>() {
    override fun areItemsTheSame(
        oldItem: Country,
        newItem: Country,
    ): Boolean {
        return oldItem.code == newItem.code
    }

    override fun areContentsTheSame(
        oldItem: Country,
        newItem: Country,
    ): Boolean {
        return oldItem == newItem
    }
}