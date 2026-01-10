package com.example.iurankomplek.domain.usecase

import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.utils.Constants
import com.example.iurankomplek.utils.OperationResult
import java.math.BigDecimal

data class PaymentValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)

sealed class PaymentError {
    data object EmptyAmount : PaymentError()
    data object InvalidFormat : PaymentError()
    data object InvalidValue : PaymentError()
    data object AmountTooSmall : PaymentError()
    data object AmountTooLarge : PaymentError()
    data object TooManyDecimalPlaces : PaymentError()
}

data class ValidatedPayment(
    val amount: BigDecimal,
    val paymentMethod: PaymentMethod
)

class ValidatePaymentUseCase {

    operator fun invoke(
        amountText: String,
        spinnerPosition: Int
    ): OperationResult<ValidatedPayment> {
        val validationResult = validateAmountText(amountText)
        if (!validationResult.isValid) {
            return OperationResult.Error(
                IllegalArgumentException(validationResult.errorMessage ?: "Invalid amount"),
                validationResult.errorMessage ?: "Invalid amount"
            )
        }

        val amount = parseAmount(amountText)
        val amountValidation = validateAmount(amount)
        if (!amountValidation.isValid) {
            return OperationResult.Error(
                IllegalArgumentException(amountValidation.errorMessage ?: "Invalid amount"),
                amountValidation.errorMessage ?: "Invalid amount"
            )
        }

        val paymentMethod = mapSpinnerPositionToPaymentMethod(spinnerPosition)

        return OperationResult.Success(ValidatedPayment(amount, paymentMethod))
    }

    private fun validateAmountText(amountText: String): PaymentValidationResult {
        if (amountText.isEmpty()) {
            return PaymentValidationResult(
                isValid = false,
                errorMessage = "Amount cannot be empty"
            )
        }
        return PaymentValidationResult(isValid = true)
    }

    private fun parseAmount(amountText: String): BigDecimal {
        return BigDecimal(amountText)
    }

    private fun validateAmount(amount: BigDecimal): PaymentValidationResult {
        if (amount <= BigDecimal.ZERO) {
            return PaymentValidationResult(
                isValid = false,
                errorMessage = "Amount must be greater than zero"
            )
        }

        val maxPaymentAmount = BigDecimal.valueOf(Constants.Payment.MAX_PAYMENT_AMOUNT)
        if (amount > maxPaymentAmount) {
            return PaymentValidationResult(
                isValid = false,
                errorMessage = "Amount exceeds maximum limit"
            )
        }

        if (amount.scale() > 2) {
            return PaymentValidationResult(
                isValid = false,
                errorMessage = "Maximum 2 decimal places allowed"
            )
        }

        return PaymentValidationResult(isValid = true)
    }

    private fun mapSpinnerPositionToPaymentMethod(position: Int): PaymentMethod {
        return when (position) {
            0 -> PaymentMethod.CREDIT_CARD
            1 -> PaymentMethod.BANK_TRANSFER
            2 -> PaymentMethod.E_WALLET
            3 -> PaymentMethod.VIRTUAL_ACCOUNT
            else -> PaymentMethod.CREDIT_CARD
        }
    }
}
