package com.example.iurankomplek.receipt

import com.example.iurankomplek.transaction.Transaction
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Random
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
        val random = Random().nextInt(1000, 9999)
        return "RCPT-$date-$random"
    }
    
    private fun generateQRCode(transactionId: String): String {
        // This would be a real QR code generator in production
        // For now, we return a string representation
        return "QR:$transactionId"
    }
}