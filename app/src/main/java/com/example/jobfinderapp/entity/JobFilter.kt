package com.example.jobfinderapp.entity

data class JobFilter(
    val country: Country,
    val searchKeyWords: String? = null,
    val category: Category? = null,
    val sortDirection: String? = null,
    val sortBy: String? = null,
    val onlyFullTime: Boolean = false,
    val onlyPartTime: Boolean = false,
    val onlyContractJobs: Boolean = false,
    val onlyPermanentJobs: Boolean = false,
    val locations: List<String>? = null
)