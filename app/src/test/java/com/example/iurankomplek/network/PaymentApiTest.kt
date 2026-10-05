package com.example.iurankomplek.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import kotlin.coroutines.Continuation

class PaymentApiTest {

    private fun method(name: String): Method? =
        ApiService::class.java.methods.firstOrNull { it.name == name }

    private fun Method.isSuspend(): Boolean =
        parameterTypes.lastOrNull() == Continuation::class.java

    private fun Method.declaredArgCount(): Int = parameterTypes.size - 1

    private fun Method.httpVerb(): String? = annotations
        .map { it.annotationClass.simpleName }
        .firstOrNull { it in setOf("GET", "POST", "PUT", "DELETE", "PATCH") }

    @Test
    fun `api service should be constructible`() {
        assertNotNull(ApiConfig.getApiService())
    }

    @Test
    fun `payment endpoints should exist as suspend functions`() {
        val expectedArgCounts = mapOf(
            "initiatePayment" to 4,
            "getPaymentStatus" to 1,
            "confirmPayment" to 1
        )

        expectedArgCounts.forEach { (name, argCount) ->
            val m = method(name)
            assertNotNull("ApiService is missing endpoint $name", m)
            assertTrue("$name must be a suspend function", m!!.isSuspend())
            assertEquals(
                "$name must take $argCount declared parameters plus a Continuation",
                argCount,
                m.declaredArgCount()
            )
        }
    }

    @Test
    fun `payment endpoints should carry the expected http verbs`() {
        val expected = mapOf(
            "initiatePayment" to "POST",
            "getPaymentStatus" to "GET",
            "confirmPayment" to "POST"
        )

        expected.forEach { (name, verb) ->
            val m = method(name)
            assertNotNull("ApiService is missing endpoint $name", m)
            assertEquals("$name must be annotated @$verb", verb, m!!.httpVerb())
        }
    }

    @Test
    fun `api service must remain a retrofit interface`() {
        assertTrue(
            "ApiService must remain a Retrofit interface",
            Modifier.isInterface(ApiService::class.java.modifiers)
        )
    }
}