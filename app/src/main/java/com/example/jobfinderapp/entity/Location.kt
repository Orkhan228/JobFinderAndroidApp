package com.example.jobfinderapp.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Location(
    val area: List<String>,
    val display_name: String
) : Parcelable