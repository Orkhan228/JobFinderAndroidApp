package com.example.jobfinderapp.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Serializable
@Parcelize
data class Category(
    val label: String? = null,
    val tag: String? = null
) : Parcelable