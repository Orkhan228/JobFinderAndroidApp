package com.example.jobfinderapp.entity

import kotlinx.serialization.Serializable

@Serializable
data class Result(
    val adref: String? = null,
    val category: Category? = null,
    val company: Company? = null,
    val contract_time: String? = null,
    val contract_type: String? = null,
    val created: String,
    val description: String,
    val id: String,
    val latitude: Float? = null,
    val location: Location? = null,
    val longitude: Float? = null,
    val redirect_url: String? = null,
    val salary_is_predicted: String? = null,
    val salary_max: Float? = null,
    val salary_min: Float? = null,
    val title: String
)