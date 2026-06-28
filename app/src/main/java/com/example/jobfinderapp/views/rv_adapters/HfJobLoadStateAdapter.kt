package com.example.jobfinderapp.views.rv_adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.jobfinderapp.databinding.JobLoadStateItemBinding

class HfJobLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<HfJobLoadStateAdapter.JobLoadStateAdapterViewHolder>() {
    override fun onBindViewHolder(
        holder: JobLoadStateAdapterViewHolder,
        loadState: LoadState,
    ) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState,
    ): JobLoadStateAdapterViewHolder =
        JobLoadStateAdapterViewHolder(
            JobLoadStateItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

    inner class JobLoadStateAdapterViewHolder(private val binding: JobLoadStateItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.lsRetryBtn.setOnClickListener {
                retry.invoke()
            }
        }

        fun bind(loadState: LoadState) {
            val isLoading = loadState is LoadState.Loading
            val isError = loadState is LoadState.Error
            val errorState = loadState as? LoadState.Error

            if (isLoading) {
                binding.lottieDots.isVisible = true
                binding.lottieDots.playAnimation()
            } else {
                binding.lottieDots.cancelAnimation()
                binding.lottieDots.isVisible = false
            }

            binding.lsRetryBtn.isVisible = isError
            binding.lsErrorText.isVisible = isError

            if (isError) {
                binding.lsErrorText.text = errorState?.error?.localizedMessage
                    ?: "Failed to load data"
            }
        }
    }
}