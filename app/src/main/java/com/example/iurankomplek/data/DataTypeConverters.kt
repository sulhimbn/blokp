package com.example.iurankomplek.data

import androidx.room.TypeConverter
import java.util.Date

class DataTypeConverters {
    @TypeConverter
    fun fromDate(value: Date?): Long {
        return value?.time ?: 0L
    }

    @TypeConverter
    fun toDate(value: Long?): Date {
        return if (value != null && value > 0L) Date(value) else Date()
    }
}
