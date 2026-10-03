package com.example.iurankomplek.network

import com.example.iurankomplek.data.api.models.PaymentConfirmationResponse
import com.example.iurankomplek.data.api.models.PaymentResponse
import com.example.iurankomplek.data.api.models.PaymentStatusResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import retrofit2.http.GET
import retrofit2.http.POST
import java.lang.reflect.Method

class PaymentApiTest {

    private fun method(name: String, vararg params: Class<*>): Method {
        val candidate = ApiService::class.java.methods.firstOrNull { it.name == name }
        assertNotNull("ApiService should declare $name", candidate)
        val method = candidate!!
        val declared = method.parameterTypes.filterNot {
            it == kotlin.coroutines.Continuation::class.java
        }
        assertEquals(
            "parameter types of $name changed",
            params.toList(),
            declared
        )
        return method
    }

    private fun responseTypeOf(method: Method): Class<*> {
        val parameterized = method.genericReturnType as java.lang.reflect.ParameterizedType
        return parameterized.actualTypeArguments[0] as Class<*>
    }

    @Test
    fun `initiatePayment should be a POST with query parameters`() {
        val method = method(
            "initiatePayment",
            String::class.java, String::class.java, String::class.java, String::class.java
        )
        assertEquals("payments/initiate", method.getAnnotation(POST::class.java)?.value)
        assertEquals(PaymentResponse::class.java, responseTypeOf(method))
    }

    @Test
    fun `getPaymentStatus should be a GET on payments id status`() {
        val method = method("getPaymentStatus", String::class.java)
        assertEquals(
            "payments/{id}/status",
            method.getAnnotation(GET::class.java)?.value
        )
        assertEquals(PaymentStatusResponse::class.java, responseTypeOf(method))
    }

    @Test
    fun `confirmPayment should be a POST on payments id confirm`() {
        val method = method("confirmPayment", String::class.java)
        assertEquals(
            "payments/{id}/confirm",
            method.getAnnotation(POST::class.java)?.value
        )
        assertEquals(PaymentConfirmationResponse::class.java, responseTypeOf(method))
    }
}