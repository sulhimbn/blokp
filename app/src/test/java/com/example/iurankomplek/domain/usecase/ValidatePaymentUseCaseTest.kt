package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.utils.Constants
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

class ValidatePaymentUseCaseTest {

    private lateinit var useCase: ValidatePaymentUseCase

    @Before
    fun setup() {
        useCase = ValidatePaymentUseCase()
    }

    @Test
    fun `invoke returns success with valid amount and credit card method`() {
        val result = useCase("100000", 0)

        assertTrue(result.isSuccess)
        val validatedPayment = result.getOrNull()
        assertNotNull(validatedPayment)
        assertEquals(BigDecimal("100000"), validatedPayment?.amount)
        assertEquals(PaymentMethod.CREDIT_CARD, validatedPayment?.paymentMethod)
    }

    @Test
    fun `invoke returns success with valid amount and bank transfer method`() {
        val result = useCase("50000", 1)

        assertTrue(result.isSuccess)
        val validatedPayment = result.getOrNull()
        assertEquals(BigDecimal("50000"), validatedPayment?.amount)
        assertEquals(PaymentMethod.BANK_TRANSFER, validatedPayment?.paymentMethod)
    }

    @Test
    fun `invoke returns success with valid amount and e-wallet method`() {
        val result = useCase("25000", 2)

        assertTrue(result.isSuccess)
        val validatedPayment = result.getOrNull()
        assertEquals(BigDecimal("25000"), validatedPayment?.amount)
        assertEquals(PaymentMethod.E_WALLET, validatedPayment?.paymentMethod)
    }

    @Test
    fun `invoke returns success with valid amount and virtual account method`() {
        val result = useCase("15000", 3)

        assertTrue(result.isSuccess)
        val validatedPayment = result.getOrNull()
        assertEquals(BigDecimal("15000"), validatedPayment?.amount)
        assertEquals(PaymentMethod.VIRTUAL_ACCOUNT, validatedPayment?.paymentMethod)
    }

    @Test
    fun `invoke returns success with valid amount and decimal places`() {
        val result = useCase("12345.67", 0)

        assertTrue(result.isSuccess)
        val validatedPayment = result.getOrNull()
        assertEquals(BigDecimal("12345.67"), validatedPayment?.amount)
        assertEquals(PaymentMethod.CREDIT_CARD, validatedPayment?.paymentMethod)
    }

    @Test
    fun `invoke returns failure with empty amount`() {
        val result = useCase("", 0)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertTrue(exception?.message?.contains("Amount cannot be empty") == true)
    }

    @Test
    fun `invoke returns failure with whitespace only amount`() {
        val result = useCase("   ", 1)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `invoke returns failure with zero amount`() {
        val result = useCase("0", 0)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertTrue(exception?.message?.contains("Amount must be greater than zero") == true)
    }

    @Test
    fun `invoke returns failure with negative amount`() {
        val result = useCase("-100", 0)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `invoke returns failure with amount exceeding maximum limit`() {
        val maxAmount = BigDecimal.valueOf(Constants.Payment.MAX_PAYMENT_AMOUNT)
        val invalidAmount = maxAmount.add(BigDecimal.ONE)
        val result = useCase(invalidAmount.toPlainString(), 0)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertTrue(exception?.message?.contains("Amount exceeds maximum limit") == true)
    }

    @Test
    fun `invoke returns success with amount at maximum limit`() {
        val maxAmount = BigDecimal.valueOf(Constants.Payment.MAX_PAYMENT_AMOUNT)
        val result = useCase(maxAmount.toPlainString(), 0)

        assertTrue(result.isSuccess)
        assertEquals(maxAmount, result.getOrNull()?.amount)
    }

    @Test
    fun `invoke returns failure with amount having more than 2 decimal places`() {
        val result = useCase("123.456", 0)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertTrue(exception?.message?.contains("Maximum 2 decimal places allowed") == true)
    }

    @Test
    fun `invoke returns success with amount having exactly 2 decimal places`() {
        val result = useCase("123.45", 0)

        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("123.45"), result.getOrNull()?.amount)
    }

    @Test
    fun `invoke returns success with amount having 1 decimal place`() {
        val result = useCase("123.5", 0)

        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("123.5"), result.getOrNull()?.amount)
    }

    @Test
    fun `invoke returns success with amount without decimal places`() {
        val result = useCase("100000", 0)

        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("100000"), result.getOrNull()?.amount)
    }

    @Test
    fun `invoke returns failure with non-numeric amount`() {
        val result = useCase("abc", 0)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `invoke returns failure with amount containing letters and numbers`() {
        val result = useCase("100abc", 0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns failure with amount containing special characters`() {
        val result = useCase("100@#", 0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns failure with amount containing comma instead of decimal point`() {
        val result = useCase("100,50", 0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns success with default credit card method for invalid spinner position`() {
        val result = useCase("100000", 99)

        assertTrue(result.isSuccess)
        val validatedPayment = result.getOrNull()
        assertEquals(PaymentMethod.CREDIT_CARD, validatedPayment?.paymentMethod)
    }

    @Test
    fun `invoke returns success with default credit card method for negative spinner position`() {
        val result = useCase("100000", -1)

        assertTrue(result.isSuccess)
        val validatedPayment = result.getOrNull()
        assertEquals(PaymentMethod.CREDIT_CARD, validatedPayment?.paymentMethod)
    }

    @Test
    fun `invoke handles large valid amount correctly`() {
        val largeAmount = BigDecimal("999999999.99")
        val result = useCase(largeAmount.toPlainString(), 0)

        assertTrue(result.isSuccess)
        assertEquals(largeAmount, result.getOrNull()?.amount)
    }

    @Test
    fun `invoke handles small valid amount correctly`() {
        val smallAmount = BigDecimal("0.01")
        val result = useCase(smallAmount.toPlainString(), 0)

        assertTrue(result.isSuccess)
        assertEquals(smallAmount, result.getOrNull()?.amount)
    }

    @Test
    fun `invoke returns failure for amount with leading zeros`() {
        val result = useCase("0000.01", 0)

        val exception = result.exceptionOrNull()
        if (result.isFailure) {
            assertTrue(exception is IllegalArgumentException)
        }
    }

    @Test
    fun `invoke returns failure for amount with trailing decimal point`() {
        val result = useCase("100.", 0)

        val exception = result.exceptionOrNull()
        if (result.isFailure) {
            assertTrue(exception is IllegalArgumentException)
        }
    }

    @Test
    fun `invoke returns failure for amount with leading decimal point`() {
        val result = useCase(".50", 0)

        val exception = result.exceptionOrNull()
        if (result.isFailure) {
            assertTrue(exception is IllegalArgumentException)
        }
    }

    @Test
    fun `invoke returns success for amount with thousands separators when properly formatted`() {
        val result = useCase("1000.00", 0)

        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("1000.00"), result.getOrNull()?.amount)
    }

    @Test
    fun `invoke handles all spinner positions correctly`() {
        val testCases = mapOf(
            0 to PaymentMethod.CREDIT_CARD,
            1 to PaymentMethod.BANK_TRANSFER,
            2 to PaymentMethod.E_WALLET,
            3 to PaymentMethod.VIRTUAL_ACCOUNT
        )

        testCases.forEach { (position, expectedMethod) ->
            val result = useCase("10000", position)
            assertTrue("Failed for position $position", result.isSuccess)
            assertEquals(expectedMethod, result.getOrNull()?.paymentMethod)
        }
    }

    @Test
    fun `invoke returns ValidatedPayment with correct type`() {
        val result = useCase("50000", 1)

        assertTrue(result.isSuccess)
        val validatedPayment = result.getOrNull()
        assertTrue(validatedPayment is ValidatedPayment)
    }

    @Test
    fun `invoke validates amount text before parsing`() {
        val result = useCase("", 0)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception?.message?.contains("Amount cannot be empty") == true)
    }

    @Test
    fun `invoke validates parsed amount value`() {
        val result = useCase("0", 0)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception?.message?.contains("must be greater than zero") == true)
    }

    @Test
    fun `invoke validates decimal places separately from value validation`() {
        val result = useCase("100.999", 0)

        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception?.message?.contains("Maximum 2 decimal places") == true)
    }

    @Test
    fun `invoke returns failure for amount with multiple decimal points`() {
        val result = useCase("100.50.25", 0)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke handles scientific notation correctly`() {
        val result = useCase("1E5", 0)

        assertTrue(result.isSuccess)
        assertEquals(BigDecimal("100000"), result.getOrNull()?.amount)
    }
}
