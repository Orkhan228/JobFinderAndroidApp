package com.example.jobfinderapp.views.rv_adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinderapp.databinding.CountrySelectorItemBinding
import com.example.jobfinderapp.entity.Country
import com.example.jobfinderapp.views.rv_helpers.CountryDiffUtil

class CountrySelectorAdapter(private val onRootClick: (Country) -> Unit, private val selectedCountryCode: String) : ListAdapter<Country, CountrySelectorAdapter.CsViewHolder>(
    CountryDiffUtil()
) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): CsViewHolder {
        val binding = CountrySelectorItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CsViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CsViewHolder,
        position: Int,
    ) {
        holder.bind(getItem(position))
    }

    inner class CsViewHolder(private val binding: CountrySelectorItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(country: Country) {
            with(binding) {

                val isSelected =
                    country.code.trim().lowercase() ==
                            selectedCountryCode.trim().lowercase()

                root.isSelected = isSelected
                csItemCheckIv.visibility = if (isSelected) View.VISIBLE else View.GONE

                csItemCountryCodeTv.text = country.code
                csItemCountryNameTv.text = country.name

                root.setOnClickListener {
                    onRootClick.invoke(country)
                }
            }
        }

    }
}
