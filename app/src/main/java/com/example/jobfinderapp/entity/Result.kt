package com.example.jobfinderapp.entity

data class Result(
    val adref: String?,
    val category: Category?,
    val company: Company?,
    val contract_time: String?,
    val contract_type: String?,
    val created: String,
    val description: String,
    val id: String,
    val latitude: Float?,
    val location: Location?,
    val longitude: Float?,
    val redirect_url: String?,
    val salary_is_predicted: String?,
    val salary_max: Float?,
    val salary_min: Float?,
    val title: String
)