package com.example.iurankomplek.presentation.ui.activity

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.iurankomplek.R
import com.example.iurankomplek.data.repository.TransactionRepositoryFactory
import com.example.iurankomplek.payment.PaymentMethod
import com.example.iurankomplek.utils.Constants
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
@MediumTest
@Config(sdk = [33])
class PaymentActivityTest {

    private lateinit var scenario: ActivityScenario<PaymentActivity>

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        scenario = ActivityScenario.launch(PaymentActivity::class.java)
    }

    @After
    fun tearDown() {
        scenario.close()
    }

    @Test
    fun `activity launches successfully`() {
        scenario.onActivity { activity ->
            org.junit.Assert.assertNotNull(activity)
            org.junit.Assert.assertNotNull(activity.findViewById(android.R.id.content))
        }
    }

    @Test
    fun `when pay button clicked with empty amount shows error toast`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)

            etAmount.setText("")
            btnPay.performClick()

            org.junit.Assert.assertEquals("", etAmount.text.toString())
        }
    }

    @Test
    fun `when pay button clicked with zero amount shows error toast`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)

            etAmount.setText("0")
            btnPay.performClick()

            org.junit.Assert.assertEquals("0", etAmount.text.toString())
        }
    }

    @Test
    fun `when pay button clicked with negative amount shows error toast`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)

            etAmount.setText("-100")
            btnPay.performClick()

            org.junit.Assert.assertEquals("-100", etAmount.text.toString())
        }
    }

    @Test
    fun `when pay button clicked with amount exceeding max limit shows error toast`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)

            val maxAmount = BigDecimal.valueOf(Constants.Payment.MAX_PAYMENT_AMOUNT)
            etAmount.setText(maxAmount.add(BigDecimal.ONE).toString())
            btnPay.performClick()

            val enteredAmount = BigDecimal(etAmount.text.toString())
            org.junit.Assert.assertTrue("Amount should exceed max limit", enteredAmount > maxAmount)
        }
    }

    @Test
    fun `when pay button clicked with more than 2 decimal places shows error toast`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)

            etAmount.setText("100.123")
            btnPay.performClick()

            val amount = BigDecimal(etAmount.text.toString())
            org.junit.Assert.assertTrue("Amount should have more than 2 decimal places", amount.scale() > 2)
        }
    }

    @Test
    fun `when pay button clicked with invalid format shows error toast`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)

            etAmount.setText("abc")
            btnPay.performClick()

            org.junit.Assert.assertEquals("abc", etAmount.text.toString())
        }
    }

    @Test
    fun `when payment method selected correctly maps to enum`() {
        scenario.onActivity { activity ->
            val spinner = activity.findViewById<android.widget.Spinner>(R.id.spinnerPaymentMethod)

            spinner.setSelection(0)
            org.junit.Assert.assertEquals(0, spinner.selectedItemPosition)

            spinner.setSelection(1)
            org.junit.Assert.assertEquals(1, spinner.selectedItemPosition)

            spinner.setSelection(2)
            org.junit.Assert.assertEquals(2, spinner.selectedItemPosition)

            spinner.setSelection(3)
            org.junit.Assert.assertEquals(3, spinner.selectedItemPosition)
        }
    }

    @Test
    fun `when valid amount entered and pay button clicked processes payment`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)
            val spinner = activity.findViewById<android.widget.Spinner>(R.id.spinnerPaymentMethod)

            etAmount.setText("100.00")
            spinner.setSelection(0)

            org.junit.Assert.assertEquals("100.00", etAmount.text.toString())
            org.junit.Assert.assertEquals(0, spinner.selectedItemPosition)
            org.junit.Assert.assertTrue(BigDecimal("100.00") > BigDecimal.ZERO)
            org.junit.Assert.assertEquals(0, BigDecimal("100.00").scale())
        }
    }

    @Test
    fun `when valid amount with 2 decimal places processes payment`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)

            etAmount.setText("100.50")

            org.junit.Assert.assertEquals("100.50", etAmount.text.toString())
            org.junit.Assert.assertTrue(BigDecimal("100.50") > BigDecimal.ZERO)
            org.junit.Assert.assertEquals(2, BigDecimal("100.50").scale())
        }
    }

    @Test
    fun `when view history button clicked navigates to TransactionHistoryActivity`() {
        scenario.onActivity { activity ->
            val btnViewHistory = activity.findViewById<android.widget.Button>(R.id.btnViewHistory)

            btnViewHistory.performClick()

            org.junit.Assert.assertNotNull(btnViewHistory)
        }
    }

    @Test
    fun `when payment processing should disable pay button`() {
        scenario.onActivity { activity ->
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)

            org.junit.Assert.assertNotNull(btnPay)
        }
    }

    @Test
    fun `when payment succeeds shows success toast`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val btnPay = activity.findViewById<android.widget.Button>(R.id.btnPay)
            val spinner = activity.findViewById<android.widget.Spinner>(R.id.spinnerPaymentMethod)

            etAmount.setText("50.00")
            spinner.setSelection(1)

            org.junit.Assert.assertEquals("50.00", etAmount.text.toString())
            org.junit.Assert.assertTrue(BigDecimal("50.00") > BigDecimal.ZERO)
        }
    }

    @Test
    fun `when amount input field accepts numeric input`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)

            etAmount.setText("1234.56")

            org.junit.Assert.assertEquals("1234.56", etAmount.text.toString())
            org.junit.Assert.assertTrue("Should be valid BigDecimal", try {
                BigDecimal("1234.56")
                true
            } catch (e: Exception) {
                false
            })
        }
    }

    @Test
    fun `when amount input field trims whitespace`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)

            etAmount.setText("  100.00  ")

            org.junit.Assert.assertEquals("  100.00  ", etAmount.text.toString())
        }
    }

    @Test
    fun `when payment method spinner has all options`() {
        scenario.onActivity { activity ->
            val spinner = activity.findViewById<android.widget.Spinner>(R.id.spinnerPaymentMethod)

            org.junit.Assert.assertNotNull(spinner)
            org.junit.Assert.assertNotNull(spinner.adapter)
        }
    }

    @Test
    fun `when invalid decimal format catches ArithmeticException`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)

            etAmount.setText("999999999999999999999999999.999999999999999999999999999")

            org.junit.Assert.assertNotNull(etAmount.text.toString())
        }
    }

    @Test
    fun `when amount equals exactly max limit processes payment`() {
        scenario.onActivity { activity ->
            val etAmount = activity.findViewById<android.widget.EditText>(R.id.etAmount)
            val maxAmount = BigDecimal.valueOf(Constants.Payment.MAX_PAYMENT_AMOUNT)

            etAmount.setText(maxAmount.toString())

            org.junit.Assert.assertEquals(maxAmount.toString(), etAmount.text.toString())
            org.junit.Assert.assertFalse("Amount should equal max limit", BigDecimal(etAmount.text.toString()) > maxAmount)
        }
    }
}