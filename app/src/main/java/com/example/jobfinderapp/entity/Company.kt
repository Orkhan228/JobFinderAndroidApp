package com.example.jobfinderapp.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Company(
    val average_salary: Float,
    val canonical_name: String,
    val count: Int,
    val display_name: String
) : Parcelable