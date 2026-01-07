package com.example.iurankomplek.utils

import com.example.iurankomplek.data.dto.Receipt
import com.example.iurankomplek.data.entity.Transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class ReceiptGenerator {
    fun generateReceipt(transaction: Transaction): Receipt {
        val receiptNumber = generateReceiptNumber()
        val qrCode = generateQRCode(transaction.id)

        return Receipt(
            id = UUID.randomUUID().toString(),
            transactionId = transaction.id,
            userId = transaction.userId,
            amount = transaction.amount,
            description = transaction.description,
            paymentMethod = transaction.paymentMethod.name,
            transactionDate = transaction.createdAt,
            receiptNumber = receiptNumber,
            qrCode = qrCode
        )
    }

    private fun generateReceiptNumber(): String {
        val date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        val random = kotlin.random.Random.nextInt(9999 - 1000 + 1) + 1000
        return "RCPT-$date-$random"
    }

    private fun generateQRCode(transactionId: String): String {
        return "QR:$transactionId"
    }
}
