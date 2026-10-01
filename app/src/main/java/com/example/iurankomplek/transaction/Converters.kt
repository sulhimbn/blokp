package com.example.iurankomplek.transaction

import androidx.room.TypeConverter
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.math.BigDecimal
import java.util.Date

class Converters {

    private val gson = Gson()

    private val mapType = object : TypeToken<Map<String, String>>() {}.type

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
    fun fromDate(value: Date?): Long? = value?.time

    @TypeConverter
    fun toDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String? =
        value?.let { gson.toJson(it) }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String>? {
        if (value.isNullOrBlank()) return null
        return gson.fromJson(value, mapType)
    }
}
