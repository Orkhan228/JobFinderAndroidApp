package com.example.jobfinderapp.data.entity

import android.os.Parcelable
import androidx.room.Embedded
import kotlinx.parcelize.Parcelize

@Parcelize
data class JobUIModel(
    @Embedded val job: Job,
    val isSaved: Boolean,
    val isApplied: Boolean,
    val appliedTime: Long?
) : Parcelable