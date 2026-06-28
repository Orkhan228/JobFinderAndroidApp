package com.example.jobfinderapp.utils

import com.example.jobfinderapp.data.entity.Job
import com.example.jobfinderapp.entity.Category
import com.example.jobfinderapp.entity.Company
import com.example.jobfinderapp.entity.Location
import com.example.jobfinderapp.entity.Result

fun Result.releaseNullable(): Job {
    val id = this.id
    val title = this.title
    val description = this.description

    // 2. Глубокая проверка Location (даже если сам объект пришел, поля внутри могут быть null)
    val location = Location(
        area = this.location?.area ?: listOf(""),
        display_name = this.location?.display_name ?: "Not specified"
    )

    // 3. Глубокая проверка Category
    val category = Category(
        label = this.category?.label ?: "Not specified",
        tag = this.category?.tag ?: ""
    )

    // 4. Глубокая проверка Company
    val company = Company(
        average_salary = this.company?.average_salary ?: 0f,
        canonical_name = this.company?.canonical_name ?: "",
        count = this.company?.count ?: 0,
        display_name = this.company?.display_name ?: "Not specified"
    )

    val salaryMax = this.salary_max ?: 0f
    val salaryMin = this.salary_min ?: 0f

    val contractTime = when(this.contract_time) {
        "full_time", null -> "Full-time"
        "part_time" -> "Part-time"
        else -> "Full-time"
    }

    val contractType = when(this.contract_type) {
        "permanent", null -> "Permanent"
        "contract" -> "Contract"
        else -> "Permanent"
    }

    val date = this.created.split("T", "Z")
    val a = date[1].split(":")
    val b = "${a[0]}:${a[1]}"

    val createdTime = "Posted ${date[0]} $b"

    return Job(
        id = id,
        title = title,
        description = description,
        createdTime = createdTime,
        location = location,
        category = category,
        company = company,
        salary_max = salaryMax,
        salary_min = salaryMin,
        contract_time = contractTime,
        contract_type = contractType
    )
}
