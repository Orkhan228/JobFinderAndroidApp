package com.example.jobfinderapp.entity

import com.example.jobfinderapp.data.entity.JobUIModel

data class HomeUiState(
    val jobs: List<JobUIModel> = emptyList(),
    val isConnected: Boolean = true,
    val showNoResults: Boolean = false,
    val showNoInternet: Boolean = false
)