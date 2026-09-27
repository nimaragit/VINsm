package com.example

import com.example.util.VinUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * تست‌های واحد منطق اعتبارسنجی VIN (شامل الگوریتم رقم کنترل ISO 3779)
 */
class VinUtilsTest {

    @Test
    fun `valid VINs pass check digit validation`() {
        // VINهای نمونه معتبر با رقم کنترل صحیح (تأییدشده با الگوریتم ISO 3779)
        val validVins = listOf(
            "1G1YY22G765106253", // Chevrolet Corvette
            "WAU4FAFL2CN017788", // Audi A4 (نمونه سیل‌زده دمو در برنامه)
            "1FA6P8CFXH5528921", // Ford Mustang (نمونه تصادفی دمو در برنامه)
            "4T1B11HK3MU129384", // Toyota Camry (نمونه سند سالم در برنامه)
            "WBA5R1C57K4824412"  // BMW 330i (نمونه IAAI دمو در برنامه)
        )
        validVins.forEach { vin ->
            assertTrue("VIN should be valid: $vin", VinUtils.isCheckDigitValid(vin))
            assertNull(VinUtils.validate(vin, strictCheckDigit = true))
        }
    }

    @Test
    fun `tampered VIN fails check digit validation`() {
        // تغییر یک کاراکتر باید رقم کنترل را بی‌اعتبار کند
        val tampered = "1G1YY22G765106254"
        assertFalse(VinUtils.isCheckDigitValid(tampered))
        assertNotNull(VinUtils.validate(tampered, strictCheckDigit = true))
    }

    @Test
    fun `wrong length is rejected`() {
        assertNotNull(VinUtils.validate("ABC123"))
        assertNotNull(VinUtils.validate(""))
    }

    @Test
    fun `forbidden characters IOQ are rejected`() {
        // «O» در موقعیت ۱ => غیرمجاز
        val withO = "2OUGF41A123456789"
        val error = VinUtils.validate(withO)
        assertNotNull(error)
        assertTrue(error!!.contains("O"))
    }

    @Test
    fun `normalize strips invalid chars and uppercases`() {
        val input = " 1fa6p-8cfxh5528921xyz "
        assertEquals("1FA6P8CFXH5528921", VinUtils.normalize(input))
    }

    @Test
    fun `non strict mode tolerates missing check digit validity`() {
        // برخی سازندگان اروپایی رقم کنترل استاندارد ندارند؛ در حالت غیرسخت‌گیرانه رد نمی‌شوند
        val noCheckDigitVin = "ZFA20000002040284" // fiat-style، رقم کنترل نامعتبر ولی ساختار مجاز
        assertFalse(VinUtils.isCheckDigitValid(noCheckDigitVin))
        assertNull(VinUtils.validate(noCheckDigitVin, strictCheckDigit = false))
    }
}
