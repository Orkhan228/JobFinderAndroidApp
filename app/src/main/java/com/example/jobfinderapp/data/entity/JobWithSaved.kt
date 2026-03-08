package com.example.jobfinderapp.data.entity

import android.os.Parcelable
import androidx.room.Embedded
import kotlinx.parcelize.Parcelize

@Parcelize
data class JobWithSaved(
    @Embedded val job: Job,
    val isSaved: Boolean,
) : Parcelable