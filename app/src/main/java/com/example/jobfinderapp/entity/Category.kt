package com.example.jobfinderapp.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    val label: String,
    val tag: String
) : Parcelable