package com.example.jobfinderapp.entity

import kotlinx.serialization.Serializable

@Serializable
data class JobDTO(
    val count: Int? = null,
    val mean: Float? = null,
    val results: List<Result>? = null
)