package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Contract test for the payment endpoints on [ApiService].
 *
 * The payment API moved from `Call<T>` to `suspend fun ...: Response<T>`, so this
 * exercises the suspend shape against a real HTTP server instead of keeping a
 * `Call` reference that can no longer exist.
 */
class PaymentApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    private fun enqueue(body: String) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body)
        )
    }

    @Test
    fun `initiatePayment maps the initiate response`() = runTest {
        enqueue(
            """{"transactionId":"txn-1","status":"COMPLETED","paymentMethod":"E_WALLET",
               "amount":"25000","currency":"IDR","transactionTime":1700000000,
               "referenceNumber":"REF-9"}"""
        )

        val response = apiService.initiatePayment(
            amount = "25000",
            description = "Iuran Februari",
            customerId = "user1",
            paymentMethod = "E_WALLET"
        )

        assertTrue(response.isSuccessful)
        val body = requireNotNull(response.body())
        assertEquals("txn-1", body.transactionId)
        assertEquals("25000", body.amount)
        assertEquals("/payments/initiate", mockWebServer.takeRequest().requestUrl?.encodedPath)
    }

    @Test
    fun `getPaymentStatus maps the status response`() = runTest {
        enqueue(
            """{"transactionId":"txn-1","status":"PENDING","amount":"25000",
               "currency":"IDR","updatedAt":1700000001}"""
        )

        val response = apiService.getPaymentStatus("txn-1")

        assertTrue(response.isSuccessful)
        assertEquals("PENDING", requireNotNull(response.body()).status)
        assertEquals("/payments/txn-1/status", mockWebServer.takeRequest().path)
    }

    @Test
    fun `confirmPayment maps the confirmation response`() = runTest {
        enqueue(
            """{"transactionId":"txn-1","status":"CONFIRMED","confirmationTime":1700000002}"""
        )

        val response = apiService.confirmPayment("txn-1")

        assertTrue(response.isSuccessful)
        assertEquals("CONFIRMED", requireNotNull(response.body()).status)
        assertEquals("/payments/txn-1/confirm", mockWebServer.takeRequest().path)
    }

    @Test
    fun `payment endpoints keep their HTTP verbs`() {
        val methods = ApiService::class.java.declaredMethods

        val initiate = methods.first { it.name == "initiatePayment" }
        val status = methods.first { it.name == "getPaymentStatus" }
        val confirm = methods.first { it.name == "confirmPayment" }

        assertEquals(1, initiate.annotations.count { it is POST })
        assertEquals(1, status.annotations.count { it is GET })
        assertEquals(1, confirm.annotations.count { it is POST })
    }

    @Test
    fun `payment response models keep the fields the gateway reads`() {
        val payment = PaymentResponse(
            transactionId = "txn-1",
            status = "COMPLETED",
            paymentMethod = "E_WALLET",
            amount = "25000",
            currency = "IDR",
            transactionTime = 1700000000L,
            referenceNumber = "REF-9"
        )
        val status = PaymentStatusResponse("txn-1", "PENDING", "25000", "IDR", 1700000001L)
        val confirmation = PaymentConfirmationResponse("txn-1", "CONFIRMED", 1700000002L)

        assertTrue(payment.status.isNotBlank())
        assertEquals("25000", payment.amount)
        assertEquals("PENDING", status.status)
        assertEquals("CONFIRMED", confirmation.status)
    }
}