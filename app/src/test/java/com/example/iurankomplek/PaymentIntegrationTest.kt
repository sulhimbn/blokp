package com.example.iurankomplek

import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.payment.PaymentStatus
import com.example.iurankomplek.transaction.Transaction
import com.example.iurankomplek.utils.FinancialCalculator
import org.junit.Test
import org.junit.Assert.*
import java.math.BigDecimal
import java.util.Date

/**
 * Unit tests for payment integration with financial reporting
 */
class PaymentIntegrationTest {

    @Test
    fun testPaymentTransactionIntegration_withFinancialCalculations() {
        // Test that payment transactions can be properly integrated with financial calculations
        
        // Sample payment transaction
        val paymentTransaction = Transaction(
            id = "test_transaction_1",
            userId = "user_1",
            amount = BigDecimal("500000"),
            currency = "IDR",
            status = PaymentStatus.COMPLETED,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            description = "HOA Monthly Payment",
            createdAt = Date(),
            updatedAt = Date()
        )
        
        // Simulate API data
        val apiDataItems = listOf(
            com.example.iurankomplek.model.DataItem(
                first_name = "John",
                last_name = "Doe",
                email = "john@example.com",
                alamat = "Jl. Test 1",
                iuran_perwarga = 100000,
                total_iuran_rekap = 1200000,
                jumlah_iuran_bulanan = 100000,
                total_iuran_individu = 100000,
                pengeluaran_iuran_warga = 25000,
                pemanfaatan_iuran = "Test utilization",
                avatar = ""
            )
        )
        
        // Calculate financial totals from API data
        val totalIuranBulanan = FinancialCalculator.calculateTotalIuranBulanan(apiDataItems)
        val totalPengeluaran = FinancialCalculator.calculateTotalPengeluaran(apiDataItems)
        val totalIuranIndividu = FinancialCalculator.calculateTotalIuranIndividu(apiDataItems)
        val rekapIuran = FinancialCalculator.calculateRekapIuran(apiDataItems)
        
        // Integrate payment transaction
        val paymentTotal = paymentTransaction.amount.toInt()
        val updatedTotalIuranBulanan = totalIuranBulanan + paymentTotal
        val updatedRekapIuran = updatedTotalIuranBulanan - totalPengeluaran
        
        // Verify calculations
        assertEquals(100000, totalIuranBulanan)  // From API data
        assertEquals(25000, totalPengeluaran)    // From API data
        assertTrue(paymentTotal > 0)             // Payment amount should be positive
        assertEquals(500000, paymentTotal)       // Payment amount as expected
        assertEquals(600000, updatedTotalIuranBulanan) // API data + payment
        assertEquals(575000, updatedRekapIuran)  // Updated rekap calculation
    }
    
    @Test
    fun testMultiplePaymentTransactionsIntegration() {
        // Test integration with multiple payment transactions
        val paymentTransactions = listOf(
            Transaction(
                id = "test_transaction_1",
                userId = "user_1",
                amount = BigDecimal("500000"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                description = "HOA Payment Jan",
                createdAt = Date(),
                updatedAt = Date()
            ),
            Transaction(
                id = "test_transaction_2",
                userId = "user_1",
                amount = BigDecimal("450000"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.E_WALLET,
                description = "HOA Payment Feb",
                createdAt = Date(),
                updatedAt = Date()
            ),
            Transaction(
                id = "test_transaction_3",
                userId = "user_2",
                amount = BigDecimal("600000"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                description = "HOA Payment Mar",
                createdAt = Date(),
                updatedAt = Date()
            )
        )
        
        // Calculate total from all completed transactions
        var totalPaymentAmount = 0
        paymentTransactions.forEach { transaction ->
            totalPaymentAmount += transaction.amount.toInt()
        }
        
        assertEquals(1550000, totalPaymentAmount)  // 500000 + 450000 + 600000
    }
    
    @Test
    fun testPaymentStatusFiltering() {
        // Test that only completed payments are included in financial calculations
        val allPaymentTransactions = listOf(
            Transaction(
                id = "completed_transaction",
                userId = "user_1",
                amount = BigDecimal("500000"),
                currency = "IDR",
                status = PaymentStatus.COMPLETED,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                description = "Completed payment",
                createdAt = Date(),
                updatedAt = Date()
            ),
            Transaction(
                id = "pending_transaction",
                userId = "user_2",
                amount = BigDecimal("300000"),
                currency = "IDR",
                status = PaymentStatus.PENDING,
                paymentMethod = PaymentMethod.E_WALLET,
                description = "Pending payment",
                createdAt = Date(),
                updatedAt = Date()
            ),
            Transaction(
                id = "failed_transaction",
                userId = "user_3",
                amount = BigDecimal("250000"),
                currency = "IDR",
                status = PaymentStatus.FAILED,
                paymentMethod = PaymentMethod.CREDIT_CARD,
                description = "Failed payment",
                createdAt = Date(),
                updatedAt = Date()
            )
        )
        
        // In a real implementation, we would filter by status using the repository
        // For this test, simulate filtering for completed transactions only
        val completedTransactions = allPaymentTransactions.filter { 
            it.status == PaymentStatus.COMPLETED 
        }
        
        var completedPaymentTotal = 0
        completedTransactions.forEach { transaction ->
            completedPaymentTotal += transaction.amount.toInt()
        }
        
        val allPaymentTotal = allPaymentTransactions.sumOf { it.amount.toInt() }
        
        assertEquals(1, completedTransactions.size)  // Only 1 completed transaction
        assertEquals(500000, completedPaymentTotal)  // Only the completed payment amount
        assertEquals(1050000, allPaymentTotal)       // All payments including pending/failed
    }
    
    @Test
    fun testBigDecimalToIntConversion() {
        // Test that BigDecimal to Int conversion works correctly for payment amounts
        val testAmounts = listOf(
            BigDecimal("100000"),
            BigDecimal("500000.00"),
            BigDecimal("1234567"),
            BigDecimal("999999.99")
        )
        
        val intAmounts = testAmounts.map { it.toInt() }
        
        assertEquals(100000, intAmounts[0])
        assertEquals(500000, intAmounts[1])
        assertEquals(1234567, intAmounts[2])
        assertEquals(999999, intAmounts[3])  // Truncates decimal part
    }
}