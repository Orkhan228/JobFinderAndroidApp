package com.example.jobfinderapp.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Company(
    val average_salary: Float? = null,
    val canonical_name: String? = null,
    val count: Int? = null,
    val display_name: String? = null
) : Parcelable