package com.example.iurankomplek.network

import kotlin.coroutines.Continuation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Method

class PaymentApiTest {

    private fun method(name: String, vararg params: Class<*>): Method =
        ApiService::class.java.getDeclaredMethod(name, *params).also {
            assertTrue(
                "$name must be a suspend function",
                it.parameterTypes.lastOrNull() == Continuation::class.java
            )
        }

    @Test
    fun `payment endpoints are declared as suspend functions`() {
        val initiate = method(
            "initiatePayment",
            String::class.java, String::class.java, String::class.java, String::class.java,
            Continuation::class.java
        )
        assertEquals("java.lang.Object", initiate.returnType.name)

        val status = method("getPaymentStatus", String::class.java, Continuation::class.java)
        assertEquals("java.lang.Object", status.returnType.name)

        val confirm = method("confirmPayment", String::class.java, Continuation::class.java)
        assertEquals("java.lang.Object", confirm.returnType.name)
    }

    @Test
    fun `payment endpoints are annotated with the documented HTTP verbs and paths`() {
        assertEquals("payments/initiate", method(
            "initiatePayment",
            String::class.java, String::class.java, String::class.java, String::class.java,
            Continuation::class.java
        ).getAnnotation(retrofit2.http.POST::class.java).value)

        assertEquals("payments/{id}/status", method("getPaymentStatus", String::class.java, Continuation::class.java)
            .getAnnotation(retrofit2.http.GET::class.java).value)

        assertEquals("payments/{id}/confirm", method("confirmPayment", String::class.java, Continuation::class.java)
            .getAnnotation(retrofit2.http.POST::class.java).value)
    }

    @Test
    fun `ApiConfig exposes a usable service`() {
        assertEquals(true, ApiConfig.getApiService() != null)
    }
}