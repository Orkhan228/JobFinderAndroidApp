package com.example.jobfinderapp.utils

import androidx.room.TypeConverter

class LocationTypeConverter {

    @TypeConverter
    fun areaLocToStringLoc(areaLoc: List<String>?): String {
        val res = areaLoc?.joinToString(", ") ?: ""
        return res
    }

    @TypeConverter
    fun stringLocToAreaLoc(stringLoc: String?): List<String> {
        val res = stringLoc?.split(",", ".", ";") ?: listOf("")
        return res
    }

}