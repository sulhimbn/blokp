package com.example.iurankomplek.transaction

import androidx.room.TypeConverter
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus

class Converters {
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
}