package com.example.jobfinderapp.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.jobfinderapp.entity.Category
import com.example.jobfinderapp.entity.Company
import com.example.jobfinderapp.entity.Location
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "job_table")
data class Job(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val createdTime: String,
    @Embedded(prefix = "location_") val location: Location,
    @Embedded(prefix = "category_") val category: Category,
    @Embedded(prefix = "company_") val company: Company,
    @ColumnInfo(name = "required_salary") val salary_max: Float,
    @ColumnInfo(name = "unnecessary_salary") val salary_min: Float,
    @ColumnInfo("contract_time") val contract_time: String,
    @ColumnInfo("contract_type") val contract_type: String,
) : Parcelable