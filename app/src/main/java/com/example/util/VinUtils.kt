package com.example.util

/**
 * ابزارهای کمکی مربوط به شماره شاسی (VIN) مطابق استاندارد ISO 3779 / 3780
 */
object VinUtils {

    /** مقادیر ترجمه‌شده کاراکترهای VIN برای محاسبه رقم کنترل (Check Digit) */
    private val TRANSLITERATION: Map<Char, Int> = buildMap {
        val letters = "ABCDEFGHJKLMNPRSTUVWXYZ" // بدون I، O و Q
        val values = intArrayOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5, 6, 7, 8, 9)
        letters.forEachIndexed { index, c -> put(c, values[index]) }
        ('0'..'9').forEach { put(it, it - '0') }
    }

    /** وزن هر موقعیت از ۱۷ رقم VIN برای رقم کنترل */
    private val WEIGHTS = intArrayOf(8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2)

    /**
     * اعتبارسنجی ساختاری VIN: دقیقاً ۱۷ کاراکتر، فقط حروف/اعداد مجاز و تطابق رقم کنترل.
     * در صورت نامعتبر بودن، پیام خطای فارسی مناسب برمی‌گرداند؛ در غیر این صورت null.
     *
     * @param strictCheckDigit اگر true باشد، عدم تطابق رقم کنترل هم خطا محسوب می‌شود.
     *                       برخی سازندگان (مانند Ferrari یا خودروهای قدیمی) رقم کنترل درستی ندارند؛
     *                       پیش‌فرض روی هشدار غیرفعال است تا ورودهای معتبر رد نشوند.
     */
    fun validate(vin: String, strictCheckDigit: Boolean = false): String? {
        if (vin.length != 17) {
            return "شماره VIN باید دقیقاً ۱۷ کاراکتر باشد (تعداد فعلی: ${vin.length})."
        }
        if (!vin.all { it in TRANSLITERATION }) {
            val bad = vin.filterNot { it in TRANSLITERATION }.toSet().joinToString("، ")
            return "کاراکترهای «$bad» در شماره استاندارد VIN مجاز نیستند (فقط ارقام و حروف انگلیسی به‌جز I، O و Q)."
        }
        if (strictCheckDigit && !isCheckDigitValid(vin)) {
            return "رقم کنترل VIN (کاراکتر نهم) با سایر کاراکترها همخوانی ندارد؛ احتمالاً شماره اشتباه تایپ شده است."
        }
        return null
    }

    /** بررسی صحت رقم کنترل (موقعیت نهم) طبق الگوریتم NHTSA/ISO 3779 */
    fun isCheckDigitValid(vin: String): Boolean {
        if (vin.length != 17 || !vin.all { it in TRANSLITERATION }) return false
        val sum = vin.map { TRANSLITERATION.getValue(it) }
            .mapIndexed { index, value -> value * WEIGHTS[index] }
            .sum()
        val remainder = sum % 11
        val expected = if (remainder == 10) 'X' else Char('0'.code + remainder)
        return vin[8] == expected
    }

    /** نرمال‌سازی ورودی کاربر: تبدیل به بزرگ، حذف کاراکترهای نامجاز و محدودکردن به ۱۷ کاراکتر */
    fun normalize(input: String): String =
        input.uppercase().filter { it in TRANSLITERATION }.take(17)
}
