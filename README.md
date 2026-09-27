# 🚗 VIN Check Pro

موتور جستجو و استعلام سوابق خودرو بر اساس **شماره شاسی (VIN)** برای اندروید — با استفاده از داده‌های رسمی **NHTSA** (VPIC Decoding + Recalls API)، نمایش مشخصات فنی، سوابق فراخوان، تاریخچه Title/ Salvage، رکوردهای مزایده (Copart/IAAI/BidFax به‌صورت دمو) و خروجی **گزارش چاپی PDF**.

- **نسخه:** 1.0 (versionCode 1)
- **Application ID:** `com.aistudio.vincheck.kxvrep`
- **حداقل اندروید:** 24 (Android 7.0) — **هدف:** SDK 36

---

## ✨ امکانات

| قابلیت | توضیح |
|--------|-------|
| 🔍 استعلام VIN | اعتبارسنجی کامل استاندارد **ISO 3779** شامل الگوریتم **رقم کنترل (Check Digit)** قبل از ارسال درخواست شبکه |
| 📋 مشخصات فنی رسمی | Decode زنده از NHTSA VPIC (سازنده، مدل، سال، موتور، کارخانه و…) |
| ⚠️ سوابق فراخوان (Recalls) | دریافت موازی فراخوان‌های ایمنی از NHTSA Recalls API |
| 🏷️ تفکیک منبع داده | هر گزارش ثبت می‌کند کدام بخش **Live API** است و کدام **Demo** — بدون جعل داده |
| 🖼️ گالری تصاویر مزایده | نمایش رکوردها و تصاویر Copart/IAAI/BidFax (داده نمونه با برچسب Demo) |
| 💾 کش آفلاین (Room) | ذخیره گزارش‌ها با **TTL ۷ روزه** و سقف ۲۰۰ رکورد + تاریخچه جستجو |
| 📄 خروجی PDF | تولید گزارش چاپی قابل اشتراک‌گذاری (FileProvider) |
| ⚡ لغو خودکار استعلام قبلی | رفع Race Condition هنگام جستجوهای پشت‌سرهم |

## 🧱 پشته فناوری (Tech Stack)

- **UI:** Jetpack Compose + Material 3
- **معماری:** MVVM (`ViewModel` + `StateFlow`)
- **شبکه:** Retrofit 2 + OkHttp 4 (کلاینت تک‌نمونه مشترک `HttpClientProvider`) + Moshi (KSP codegen)
- **پایگاه داده محلی:** Room (با ایندکس و پاک‌سازی خودکار)
- **تصاویر:** Coil
- **سرور/هوش مصنوعی:** Firebase AI (Gemini) + App Check — *اختیاری*
- **زنجیره ساخت:** Gradle (Kotlin DSL) + Version Catalog (`gradle/libs.versions.toml`) + KSP + Secrets Gradle Plugin
- **تست:** JUnit4، Robolectric، Coroutines Test، Compose UI Test

## 📂 ساختار پروژه

```
app/src/main/java/com/example/
├── MainActivity.kt              # نقطه ورود و چیدمان اصلی (Compose)
├── data/
│   ├── local/                   # Room: AppDatabase, VinSearchDao, VinSearchEntity
│   ├── model/                   # مدل‌ها: VehicleHistoryReport, NhtsaModels, AuctionDamageRecord
│   ├── pdf/PdfReportGenerator.kt
│   └── remote/                  # NhtsaApiService, HttpClientProvider, VinHistoryRepository
├── ui/
│   ├── VinViewModel.kt          # مدیریت حالت استعلام، لغو درخواست قبلی
│   ├── components/              # VinInputCard, RecallsCard, VehicleSpecsCard, …
│   ├── history/                 # SearchHistorySheet (تاریخچه جستجو)
│   └── theme/                   # Color, Type, Theme
└── util/VinUtils.kt             # اعتبارسنجی VIN طبق ISO 3779 + رقم کنترل
```

## 🚀 راه‌اندازی و اجرا

### پیش‌نیازها
- **Android Studio** (Ladybug یا جدیدتر) با **JDK 17+**
- دستگاه/امولاتور با Android 7.0 (API 24) یا بالاتر

### مراحل
1. کلون کنید:
   ```bash
   git clone <repo-url> && cd vin-check-pro
   ```
2. فایل `.env.example` را به `.env` کپی کنید و در صورت استفاده از Gemini، کلید را وارد کنید:
   ```bash
   cp .env.example .env
   # GEMINI_API_KEY=YOUR_KEY   ← خط مربوطه را از حالت کامنت خارج کنید
   ```
3. پروژه را در Android Studio باز کرده و **Run** را بزنید، یا از خط فرمان:
   ```bash
   ./gradlew :app:assembleDebug
   ```

> 🔑 کلید API لازم نیست: سرویس‌های NHTSA VPIC و Recalls عمومی و بدون کلید هستند.

### امضای نسخه Release
متغیرهای محیطی زیر (یا فایل‌های keystore پیش‌فرض در ریشه پروژه) مورد نیاز است:

| متغیر | کاربرد |
|-------|--------|
| `KEYSTORE_PATH` | مسیر فایل `.jks` (پیش‌فرض: `my-upload-key.jks`) |
| `STORE_PASSWORD` | رمز Keystore |
| `KEY_PASSWORD` | رمز Key (alias: `upload`) |

```bash
./gradlew :app:assembleRelease   # R8 + shrinkResources فعال
```

## 🧪 تست

```bash
./gradlew test                    # تست‌های واحد (VinUtilsTest شامل الگوریتم رقم کنترل)
./gradlew connectedAndroidTest    # تست‌های UI (Compose) روی دستگاه
```

## ⚙️ نکات معماری و عملکرد

- **استعلام موازی:** Decode و Recalls همزمان با `coroutineScope { async }` اجرا می‌شوند (~۵۰٪ کاهش تأخیر).
- **کش هوشمند:** گزارش‌ها ۷ روز معتبرند؛ قدیمی‌ترین رکوردهای تاریخچه پس از ۲۰۰ ردیف حذف می‌شوند.
- **لاگ شبکه فقط در Debug:** در Release سطح لاگ OkHttp روی `NONE` تنظیم می‌شود + `callTimeout` و retry خودکار.
- **بدون داده جعلی:** اگر VIN در پایگاه NHTSA یافت نشود، پیام خطای صریح برگردانده می‌شود؛ هیچ مشخصه ساختگی جایگزین نمی‌شود.

جزئیات کامل بازبینی کد، ضعف‌های شناسایی‌شده و بهینه‌سازی‌های اعمال‌شده:
📎 **[docs/CODE_REVIEW.md](docs/CODE_REVIEW.md)**

## ⚠️ محدودیت‌ها و برنامه‌های آینده

- داده‌های مزایده (Copart/IAAI/BidFax) در حال حاضر **نمونه آزمایشی (Demo)** هستند؛ اتصال واقعی مستلزم کلید تجاری آن سرویس‌هاست.
- برنامه‌های بعدی: Hilt برای DI، WorkManager برای به‌روزرسانی دوره‌ای فراخوان‌ها، پشتیبانی کامل RTL.

## 📄 مجوزها و حریم خصوصی

این برنامه تنها با شماره VIN کار می‌کند و اطلاعات شخصی کاربر را جمع‌آوری نمی‌کند. داده‌ها محلی (Room) نگهداری می‌شوند و استعلام‌ها به سرویس‌های عمومی دولت آمریکا (NHTSA) ارسال می‌گردند.
