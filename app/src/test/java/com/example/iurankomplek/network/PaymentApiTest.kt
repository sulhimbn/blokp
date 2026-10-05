package com.example.iurankomplek.network

import java.lang.reflect.Method
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Contract checks on the payment portion of [ApiService].
 *
 * Reflection is used instead of calling the methods: the endpoint implementations are suspend
 * functions that would perform a real HTTP request, and ApiConfig.getApiService() resolves to a
 * Docker-internal host in a debug build. A Kotlin `suspend fun` compiles to a JVM method whose last
 * parameter is a Continuation, which is what these assertions check.
 */
class PaymentApiTest {

    private val continuationType = Class.forName("kotlin.coroutines.Continuation")

    private fun declared(name: String, vararg params: Class<*>): Method =
        ApiService::class.java.getDeclaredMethod(name, *params)

    private fun assertSuspendPost(name: String, vararg params: Class<*>) {
        val method = declared(name, *params, continuationType)
        val post = method.getAnnotation(POST::class.java)
        assertTrue("$name should be annotated @POST", post != null)
        assertEquals("payments", post.value.substringBefore("/"))
    }

    private fun assertSuspendGet(name: String, vararg params: Class<*>) {
        val method = declared(name, *params, continuationType)
        val get = method.getAnnotation(GET::class.java)
        assertTrue("$name should be annotated @GET", get != null)
        assertTrue("$name path should be under payments", get.value.startsWith("payments/"))
    }

    @Test
    fun `initiatePayment should be a suspend POST with four query params`() {
        assertSuspendPost("initiatePayment", String::class.java, String::class.java,
            String::class.java, String::class.java)
    }

    @Test
    fun `getPaymentStatus should be a suspend GET with a path param`() {
        assertSuspendGet("getPaymentStatus", String::class.java)
    }

    @Test
    fun `confirmPayment should be a suspend POST with a path param`() {
        assertSuspendPost("confirmPayment", String::class.java)
    }

    @Test
    fun `payment endpoints should return Response wrappers`() {
        val returnTypes = listOf("initiatePayment", "getPaymentStatus", "confirmPayment")
            .associateWith { name ->
                ApiService::class.java.declaredMethods
                    .first { it.name == name }
                    .genericParameterTypes
                    .last()
                    .typeName
            }

        returnTypes.forEach { (name, typeName) ->
            assertTrue(
                "$name continuation should expose a Response payload but was $typeName",
                typeName.contains("Response")
            )
        }
    }
}