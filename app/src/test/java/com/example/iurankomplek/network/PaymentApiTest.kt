package com.example.iurankomplek.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Asserts the payment endpoints of ApiService keep their HTTP contract.
 *
 * The declarations are inspected reflectively rather than invoked: the endpoints are
 * suspend functions, so calling them would issue a real request to the configured base URL.
 */
class PaymentApiTest {

    private fun method(name: String) =
        ApiService::class.java.declaredMethods.firstOrNull { it.name == name }
            ?: error("ApiService has no method named $name")

    @Test
    fun `initiatePayment posts to the payments initiate path`() {
        val declared = method("initiatePayment")

        assertNotNull(declared)
        val post = declared.getAnnotation(POST::class.java)
        assertNotNull("initiatePayment must be annotated @POST", post)
        assertEquals("payments/initiate", post.value)

        assertEquals(Any::class.java, declared.returnType)
        assertEquals(5, declared.parameterCount)
    }

    @Test
    fun `getPaymentStatus gets from the payments status path`() {
        val declared = method("getPaymentStatus")

        assertNotNull(declared)
        val get = declared.getAnnotation(GET::class.java)
        assertNotNull("getPaymentStatus must be annotated @GET", get)
        assertEquals("payments/{id}/status", get.value)
        assertEquals(Any::class.java, declared.returnType)
    }

    @Test
    fun `confirmPayment posts to the payments confirm path`() {
        val declared = method("confirmPayment")

        assertNotNull(declared)
        val post = declared.getAnnotation(POST::class.java)
        assertNotNull("confirmPayment must be annotated @POST", post)
        assertEquals("payments/{id}/confirm", post.value)
        assertEquals(Any::class.java, declared.returnType)
    }

    @Test
    fun `every payment endpoint is exposed as a suspend function`() {
        val paymentEndpoints = listOf("initiatePayment", "getPaymentStatus", "confirmPayment")

        paymentEndpoints.forEach { name ->
            val declared = method(name)
            val isSuspend = declared.parameterTypes.last().name ==
                "kotlin.coroutines.Continuation"
            assertTrue("$name should be a suspend function", isSuspend)
        }
    }
}