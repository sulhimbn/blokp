package com.example.iurankomplek.payment

import com.example.iurankomplek.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID

class RealPaymentGateway(
    private val apiService: ApiService
) : PaymentGateway {
    
    override suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.initiatePayment(
                    amount = request.amount.toString(),
                    description = request.description,
                    customerId = request.customerId,
                    paymentMethod = request.paymentMethod.name
                )
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null) {
                        Result.success(
                            PaymentResponse(
                                transactionId = apiResponse.transactionId,
                                status = convertApiStatus(apiResponse.status),
                                paymentMethod = convertApiPaymentMethod(apiResponse.paymentMethod),
                                amount = if (apiResponse.amount.isNotEmpty()) BigDecimal(apiResponse.amount) else request.amount,
                                currency = apiResponse.currency,
                                transactionTime = apiResponse.transactionTime,
                                referenceNumber = apiResponse.referenceNumber,
                                metadata = request.metadata
                            )
                        )
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Result.failure(Exception("API request failed: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun refundPayment(transactionId: String): Result<RefundResponse> {
        // For now, keeping this as a mock implementation since refund API endpoint wasn't specified
        // In a real implementation, this would call an API endpoint
        // SECURITY: Never use hardcoded refund amounts - must fetch from original transaction
        return try {
            // In a real implementation:
            // 1. Fetch original transaction to get amount
            // 2. Validate refund request against business rules
            // 3. Call payment gateway API for refund
            
            // Mock response - in production, refund amount MUST come from original transaction
            // or be explicitly provided and validated
            val response = RefundResponse(
                refundId = UUID.randomUUID().toString(),
                transactionId = transactionId,
                amount = BigDecimal.ZERO, // Placeholder - must be populated from transaction
                status = RefundStatus.COMPLETED,
                refundTime = System.currentTimeMillis(),
                reason = "Refund processed (mock implementation)"
            )
            
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getPaymentStatus(transactionId: String): Result<PaymentStatus> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getPaymentStatus(transactionId)
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null) {
                        Result.success(convertApiStatus(apiResponse.status))
                    } else {
                        Result.failure(Exception("Empty response body"))
                    }
                } else {
                    Result.failure(Exception("API request failed: ${response.code()} - ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun convertApiStatus(apiStatus: String): PaymentStatus {
        return when (apiStatus.uppercase()) {
            "PENDING" -> PaymentStatus.PENDING
            "PROCESSING" -> PaymentStatus.PROCESSING
            "COMPLETED", "SUCCESS" -> PaymentStatus.COMPLETED
            "FAILED", "ERROR" -> PaymentStatus.FAILED
            "REFUNDED" -> PaymentStatus.REFUNDED
            "CANCELLED" -> PaymentStatus.CANCELLED
            else -> PaymentStatus.PENDING
        }
    }
    
    private fun convertApiPaymentMethod(apiMethod: String): PaymentMethod {
        return when (apiMethod.uppercase()) {
            "CREDIT_CARD" -> PaymentMethod.CREDIT_CARD
            "BANK_TRANSFER" -> PaymentMethod.BANK_TRANSFER
            "E_WALLET" -> PaymentMethod.E_WALLET
            "VIRTUAL_ACCOUNT" -> PaymentMethod.VIRTUAL_ACCOUNT
            else -> PaymentMethod.CREDIT_CARD
        }
    }
}