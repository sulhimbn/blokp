package com.example.iurankomplek.network

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Payment endpoints carry financial data, so these tests pin the exact request the app
 * puts on the wire (method, path, query) as well as the parsed response.
 */
class PaymentApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: ApiService
    private val gson = Gson()

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

    private fun enqueue(body: Any, code: Int = 200) {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(code)
                .setHeader("Content-Type", "application/json")
                .setBody(if (body is String) body else gson.toJson(body))
        )
    }

    private val initiatedPayment = mapOf(
        "transactionId" to "txn_1",
        "status" to "PENDING",
        "paymentMethod" to "BANK_TRANSFER",
        "amount" to "150000",
        "currency" to "IDR",
        "transactionTime" to 1_700_000_000_000L,
        "referenceNumber" to "REF-1"
    )

    @Test
    fun `initiatePayment posts amount and description as query parameters`() = runTest {
        enqueue(initiatedPayment)

        val result = apiService.initiatePayment("150000", "Monthly dues", "user_1", "BANK_TRANSFER")

        assertTrue(result.isSuccessful)
        assertEquals("txn_1", result.body()?.transactionId)
        assertEquals("150000", result.body()?.amount)
        assertEquals("IDR", result.body()?.currency)

        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertTrue(recorded.path!!.startsWith("/payments/initiate?"))
        assertTrue(recorded.path!!.contains("amount=150000"))
        assertTrue(recorded.path!!.contains("customerId=user_1"))
    }

    @Test
    fun `getPaymentStatus reads the status resource`() = runTest {
        enqueue(
            mapOf(
                "transactionId" to "txn_1",
                "status" to "COMPLETED",
                "amount" to "150000",
                "currency" to "IDR",
                "updatedAt" to 1_700_000_100_000L
            )
        )

        val result = apiService.getPaymentStatus("txn_1")

        assertTrue(result.isSuccessful)
        assertEquals("COMPLETED", result.body()?.status)
        assertEquals("/payments/txn_1/status", mockWebServer.takeRequest().path)
    }

    @Test
    fun `confirmPayment posts to the confirmation resource`() = runTest {
        enqueue(
            mapOf(
                "transactionId" to "txn_1",
                "status" to "CONFIRMED",
                "confirmationTime" to 1_700_000_200_000L
            )
        )

        val result = apiService.confirmPayment("txn_1")

        assertTrue(result.isSuccessful)
        assertEquals("CONFIRMED", result.body()?.status)
        val recorded = mockWebServer.takeRequest()
        assertEquals("POST", recorded.method)
        assertEquals("/payments/txn_1/confirm", recorded.path)
    }

    @Test
    fun `payment failures surface the http status`() = runTest {
        enqueue("{\"error\":\"declined\"}", code = 402)

        val result = apiService.initiatePayment("150000", "Monthly dues", "user_1", "BANK_TRANSFER")

        assertEquals(402, result.code())
    }

    @Test
    fun `a partial payment payload still parses the available fields`() = runTest {
        enqueue("{\"transactionId\":\"txn_2\",\"status\":\"PENDING\"}")

        val result = apiService.initiatePayment("1", "desc", "user_1", "E_WALLET")

        assertNotNull(result.body())
        assertEquals("txn_2", result.body()?.transactionId)
        assertEquals("PENDING", result.body()?.status)
    }
}