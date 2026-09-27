# گزارش بازبینی و بهینه‌سازی VIN Check Pro

> موتور جستجوی سوابق خودرو بر اساس VIN (اندروید / Jetpack Compose / Room / Retrofit)

## ۱. ضعف‌های بحرانی منطق برنامه (Logic)

| # | مشکل | فایل | توضیح |
|---|------|------|-------|
| L1 | **جعل داده (Hallucination)** | `VinHistoryRepository.kt` | وقتی پاسخ NHTSA برای یک VIN واقعی ناموفق بود، برنامه مشخصات **ساختگی** (`Mustang GT`, `Camry XLE`, `2.0L Turbo`…) تولید می‌کرد و کاربر آن را «گزارش رسمی» می‌پنداشت. خطرناک‌ترین ضعف منطق برنامه. |
| L2 | **اعتبارسنجی ناقص VIN** | `VinHistoryRepository.kt` | فقط طول ۱۷ و حروف I/O/Q بررسی می‌شد؛ **رقم کنترل (Check Digit، موقعیت ۹)** مطابق ISO 3779 اصلاً بررسی نمی‌شد، بنابراین VIN غلط‌تایپ‌شده هم «استعلام موفق» برمی‌گرداند. |
| L3 | **جدول سال مدل اشتباه** | `determineYearFromVin` | بدون چرخه ۳۰ ساله، جا افتادن کاراکتر `U`، و بازگرداندن بی‌صدای `2021/2022` برای کاراکتر ناشناخته. |
| L4 | **عدم تفکیک منبع داده** | کل گزارش | هیچ فیلدی ثبت نمی‌کرد کدام داده از API زنده است و کدام حدس محلی — داده جعلی کش‌شده برای همیشه «رسمی» به‌نظر می‌رسید. |
| L5 | **شرط‌های دمو (Preset) بیش‌ازحد گسترده** | `resolveAuctionAndSalvageData` | `vin.contains("1FA")` باعث می‌شد **هر** خودروی فورد با هر VIN دیگری رکورد تصادف Copart بسازد! |
| L6 | **بدون لغو درخواست قبلی (Race Condition)** | `VinViewModel.kt` | با زدن پشت‌سرهم دکمه استعلام، چند coroutine همزمان اجرا و وضعیت نهایی غیرقابل‌پیش‌بینی می‌شد. |
| L7 | **کش نامحدود و کهنه** | `AppDatabase` / Repository | گزارش‌ها تا ابد کش می‌ماندند (فراخوان/مزایده متغیر است) و سقف تعداد رکورد نبود. |
| L8 | **سربار شبکه در Release** | `NhtsaApiService.kt` | لاگ HTTP در نسخه Release فعال بود و دو Retrofit مستقل (کلاینت/Dispatcher تکراری) ساخته می‌شد. |

## ۲. ضعف‌های عملکردی (Performance)

| # | مشکل | راه‌حل اعمال‌شده |
|---|------|-----------------|
| P1 | استعلام‌های decode و recalls **سری** اجرا می‌شدند (حداقل ۲× تأخیر شبکه) | اجرای موازی با `coroutineScope { async(...) }` → زمان استعلام تقریباً نصف شد |
| P2 | ساخت مجدد OkHttp/Moshi برای هر سرویس | شیء مشترک `HttpClientProvider` (کلاینت تک‌نمونه با Dispatcher کوئول‌دار + Pool اتصالات مشترک) |
| P3 | لاگ شبکه در نسخه Release | `level = NONE` در Release از طریق `BuildConfig.DEBUG` + افزودن `callTimeout` و `retryOnConnectionFailure` |
| P4 | کش Room بدون TTL و بدون سقف رکورد | TTL ۷ روزه + حذف خودکار قدیمی‌تر از ۲۰۰ رکورد (`trimOldSearches`) |
| P5 | ارسال درخواست شبکه حتی برای VIN ناقص/نامعتبر | اعتبارسنجی ساختاری **قبل** از هر کاری (شبکه/کش) |
| P6 | ناسازگاری بایت‌کد: `compileOptions` روی Java 11 در حالی که Kotlin 2.2 پیش‌فرض JVM 17 تولید می‌کند | یکنواخت‌سازی روی Java 17 |
| P7 | `isMinifyEnabled=false` در Release → APK حجیم | R8 + `shrinkResources` با قوانین ProGuard لازم برای Moshi/Retrofit/Room |

## ۳. تغییرات اعمال‌شده

### جدید
- **`util/VinUtils.kt`**: اعتبارسنجی کامل استاندارد ISO 3779 شامل الگوریتم رقم کنترل، نرمال‌سازی ورودی و پیام‌های خطای فارسی دقیق.
- **`test/java/com/example/VinUtilsTest.kt`**: تست‌های واحد الگوریتم VIN.
- **`remote/HttpClientProvider.kt`**: اشتراک کلاینت شبکه و Moshi بین هر دو سرویس NHTSA.

### اصلاح‌شده
- **`VinHistoryRepository.kt`**
  - حذف کامل داده‌سازی جعلی: اگر VPIC پاسخ ندهد یا VIN را نشناسد، `Result.failure` با پیام صریح («این VIN در پایگاه NHTSA یافت نشد») برگردانده می‌شود؛ هیچ مشخصه ساختگی جایگزین نمی‌شود.
  - موازی‌سازی decode + recalls با `async` (کاهش ~۵۰٪ زمان استعلام).
  - تفکیک منابع: فیلدهای `dataSourceSummary` و `recallsSource` در گزارش ثبت می‌شوند (Live API vs Demo).
  - اعتبارسنجی VIN قبل از شبکه/کش با `VinUtils.validate`.
  - TTL کش + پاک‌سازی خودکار تاریخچه.
  - شرط‌های Preset دمو محدود به VINهای نمونه دقیق شدند و برچسب «Demo» خوردند.
- **`VinViewModel.kt`**: لغو خودکار استعلام قبلی هنگام شروع استعلام جدید (رفع Race).
- **`VinInputCard.kt`**: نمایش زنده وضعیت رقم کنترل زیر فیلد ورودی با `remember(vin)` (بدون محاسبه مجدد در هر recomposition).
- **`NhtsaApiService.kt`**: کلاینت مشترک، لاگ فقط در Debug، `callTimeout` و retry.
- **`AppDatabase.kt` / `VinSearchEntity.kt`**: حذف `fallbackToDestructiveMigration` خام، افزودن ایندکس روی `searchedAt`، افزودن `trimOldSearches` به DAO.
- **`app/build.gradle.kts` + `proguard-rules.pro`**: Java 17، R8 فعال، قوانین keep برای Moshi reflection.

## ۴. توصیه‌های بعدی (اعمال‌نشده)
1. اتصال واقعی به سرویس‌های NMVTIS/Copart API (کلید تجاری) — فعلاً داده مزایده صرفاً دمو است و باید در UI با برچسب «نمونه آزمایشی» متمایز بماند.
2. مهاجرت به Hilt برای DI به‌جای ساخت دستی در MainActivity.
3. WorkManager برای به‌روزرسانی دوره‌ای فراخوان‌های خودروهای نشان‌شده (Favorites).
4. پشتیبانی RTL کامل (LocalLayoutDirection) چون متن‌ها فارسی ولی چیدمان پیش‌فرض LTR است.
