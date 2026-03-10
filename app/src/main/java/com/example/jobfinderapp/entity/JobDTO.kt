package com.example.jobfinderapp.entity

data class JobDTO(
    val count: Int,
    val mean: Float,
    val results: List<Result>
)