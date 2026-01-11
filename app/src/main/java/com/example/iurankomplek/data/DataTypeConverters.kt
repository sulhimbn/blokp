package com.example.iurankomplek.data

import androidx.room.TypeConverter
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal
import java.util.Date

class DataTypeConverters {
    private val gson = Gson()
    private val BD_HUNDRED = BigDecimal("100")

    @TypeConverter
    fun fromPaymentMethod(method: PaymentMethod): String {
        return method.name
    }

    @TypeConverter
    fun toPaymentMethod(value: String): PaymentMethod {
        return PaymentMethod.valueOf(value)
    }

    @TypeConverter
    fun fromPaymentStatus(status: PaymentStatus): String {
        return status.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String): PaymentStatus {
        return PaymentStatus.valueOf(value)
    }

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): Long {
        return value?.multiply(BD_HUNDRED)
            ?.setScale(0, java.math.RoundingMode.HALF_UP)
            ?.toLong() ?: 0L
    }

    @TypeConverter
    fun toBigDecimal(value: Long?): BigDecimal {
        return if (value != null && value > 0L) {
            BigDecimal(value).divide(BD_HUNDRED, 2, java.math.RoundingMode.HALF_UP)
        } else {
            BigDecimal.ZERO
        }
    }

    @TypeConverter
    fun fromDate(value: Date?): Long {
        return value?.time ?: 0L
    }

    @TypeConverter
    fun toDate(value: Long?): Date {
        return if (value != null && value > 0L) Date(value) else Date()
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String {
        return gson.toJson(value ?: emptyMap<String, String>())
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return if (value != null) gson.fromJson(value, type) else emptyMap()
    }
}
