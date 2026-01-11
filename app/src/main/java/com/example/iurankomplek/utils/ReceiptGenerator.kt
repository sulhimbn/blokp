package com.example.iurankomplek.utils

import com.example.iurankomplek.data.dto.Receipt
import com.example.iurankomplek.data.entity.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReceiptGenerator {
    fun generateReceipt(transaction: Transaction): Receipt {
        val receiptNumber = generateReceiptNumber()
        val qrCode = generateQRCode(transaction.id)
        val amountInCurrency = java.math.BigDecimal(transaction.amount).divide(BD_HUNDRED, 2, java.math.RoundingMode.HALF_UP)

        return Receipt(
            id = UUID.randomUUID().toString(),
            transactionId = transaction.id,
            userId = transaction.userId.toString(),
            amount = amountInCurrency,
            description = transaction.description,
            paymentMethod = transaction.paymentMethod.name,
            transactionDate = transaction.createdAt,
            receiptNumber = receiptNumber,
            qrCode = qrCode
        )
    }

    private fun generateReceiptNumber(): String {
        val date = getDateFormat().format(Date())
        val random = kotlin.random.Random.nextInt(Constants.Receipt.RANDOM_MAX - Constants.Receipt.RANDOM_MIN + 1) + Constants.Receipt.RANDOM_MIN
        return "RCPT-$date-$random"
    }

    companion object {
        @Volatile
        private var DATE_FORMAT: SimpleDateFormat? = null

        private fun getDateFormat(): SimpleDateFormat {
            return DATE_FORMAT ?: synchronized(this) {
                DATE_FORMAT ?: SimpleDateFormat("yyyyMMdd", Locale.US).also { DATE_FORMAT = it }
            }
        }

        private val BD_HUNDRED = java.math.BigDecimal("100")
    }

    private fun generateQRCode(transactionId: String): String {
        return "QR:$transactionId"
    }
}
