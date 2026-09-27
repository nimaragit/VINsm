package com.example.data.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.TitleStatusCategory
import com.example.data.model.VehicleHistoryReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    suspend fun generatePdfReport(
        context: Context,
        report: VehicleHistoryReport
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
            val outputFile = File(reportsDir, "VIN_Report_${report.vin}.pdf")

            val document = PdfDocument()

            // Page 1: Overview, Technical Specs & Auction Damage
            val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page1 = document.startPage(pageInfo1)
            drawPage1(page1.canvas, report)
            document.finishPage(page1)

            // Page 2: NMVTIS Title Brands, Recalls & External Registry Sources
            val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
            val page2 = document.startPage(pageInfo2)
            drawPage2(page2.canvas, report)
            document.finishPage(page2)

            FileOutputStream(outputFile).use { out ->
                document.writeTo(out)
            }
            document.close()

            Result.success(outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun drawPage1(canvas: Canvas, report: VehicleHistoryReport) {
        val width = 595f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Header Banner
        paint.color = Color.rgb(15, 23, 42) // #0F172A
        canvas.drawRect(0f, 0f, width, 90f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("VIN CHECK PRO - VEHICLE HISTORY REPORT", 30f, 40f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(148, 163, 184)
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(report.generatedAt))
        canvas.drawText("Generated: $dateStr | Official Source: NHTSA + Copart/IAAI/BidFax", 30f, 65f, paint)

        // 2. Vehicle Overview Card
        var y = 115f
        paint.color = Color.rgb(241, 245, 249)
        val cardRect = RectF(25f, y, width - 25f, y + 100f)
        canvas.drawRoundRect(cardRect, 8f, 8f, paint)

        // Title Status Pill
        val statusColor = when (report.titleStatusCategory) {
            TitleStatusCategory.CLEAN -> Color.rgb(16, 185, 129) // Green
            TitleStatusCategory.SALVAGE -> Color.rgb(239, 68, 68) // Red
            TitleStatusCategory.WATER_FLOOD -> Color.rgb(14, 165, 233) // Blue
            TitleStatusCategory.REBUILT -> Color.rgb(139, 92, 246) // Purple
            TitleStatusCategory.JUNK_SCRAP -> Color.rgb(220, 38, 38)
        }
        paint.color = statusColor
        val badgeRect = RectF(width - 200f, y + 15f, width - 40f, y + 42f)
        canvas.drawRoundRect(badgeRect, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.isFakeBoldText = true
        val statusText = when (report.titleStatusCategory) {
            TitleStatusCategory.CLEAN -> "CLEAN TITLE"
            TitleStatusCategory.SALVAGE -> "SALVAGE RECORD"
            TitleStatusCategory.WATER_FLOOD -> "FLOOD / WATER"
            TitleStatusCategory.REBUILT -> "REBUILT TITLE"
            TitleStatusCategory.JUNK_SCRAP -> "JUNK / SCRAP"
        }
        canvas.drawText(statusText, width - 180f, y + 33f, paint)

        // Vehicle Name
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 17f
        paint.isFakeBoldText = true
        canvas.drawText("${report.year} ${report.make} ${report.model}", 45f, y + 35f, paint)

        paint.textSize = 11f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(51, 65, 85)
        canvas.drawText("VIN: ${report.vin}", 45f, y + 60f, paint)
        canvas.drawText("Body: ${report.bodyClass} | Origin: ${report.countryOfOrigin}", 45f, y + 80f, paint)

        // 3. Section: Technical Specifications
        y += 125f
        drawSectionHeader(canvas, "1. TECHNICAL SPECIFICATIONS (NHTSA OFFICIAL)", y)

        y += 20f
        val specs = listOf(
            "Make / Manufacturer" to "${report.make} (${report.manufacturer})",
            "Model & Year" to "${report.model} - ${report.year}",
            "Vehicle Type / Class" to report.vehicleType,
            "Engine Displacement" to "${report.displacementL}L (${report.cylinders} Cylinders)",
            "Engine Description" to report.engine,
            "Fuel Type" to report.fuelType,
            "Transmission" to report.transmission,
            "Drive Type" to report.driveType,
            "Doors / Body Style" to "${report.doors} Doors (${report.bodyClass})",
            "Assembly Plant" to "${report.assemblyPlant}, ${report.countryOfOrigin}",
            "GVWR Rating" to report.gvwr
        )

        y = drawKeyValueTable(canvas, specs, y)

        // 4. Section: Auction & Salvage Damage Records (Copart / IAAI / Bidfax)
        y += 25f
        drawSectionHeader(canvas, "2. SALVAGE & AUCTION RECORDS (COPART / IAAI / BIDFAX)", y)
        y += 20f

        if (report.auctionRecords.isNotEmpty()) {
            val record = report.auctionRecords.first()
            val auctionData = listOf(
                "Auction House" to record.auctionHouse,
                "Lot Number" to record.lotNumber,
                "Sale Date" to record.saleDate,
                "Primary Damage" to record.primaryDamage,
                "Secondary Damage" to (record.secondaryDamage ?: "None Reported"),
                "Loss Cause" to record.lossType,
                "Title Document" to record.titleType,
                "Odometer Reading" to record.odometer,
                "Keys Present" to record.keysStatus,
                "Run & Drive Condition" to record.runAndDrive,
                "Estimated Retail Value" to record.estimatedRetailValue,
                "Estimated Repair Cost" to record.estimatedRepairCost,
                "Selling Insurer / Party" to (record.seller ?: "Insurance Pool"),
                "Auction Yard Location" to record.auctionLocation
            )
            drawKeyValueTable(canvas, auctionData, y)
        } else {
            paint.color = Color.rgb(240, 253, 244)
            canvas.drawRoundRect(RectF(25f, y, width - 25f, y + 55f), 6f, 6f, paint)
            paint.color = Color.rgb(22, 101, 52)
            paint.textSize = 12f
            paint.isFakeBoldText = true
            canvas.drawText("NO SALVAGE OR TOTAL LOSS AUCTION RECORDS FOUND", 40f, y + 26f, paint)
            paint.textSize = 10f
            paint.isFakeBoldText = false
            canvas.drawText("This vehicle has no record of total loss auctions in Copart, IAAI, BidFax or stat.vin.", 40f, y + 43f, paint)
        }

        // Page 1 Footer
        drawPageFooter(canvas, 1, 2)
    }

    private fun drawPage2(canvas: Canvas, report: VehicleHistoryReport) {
        val width = 595f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Top mini-header
        paint.color = Color.rgb(15, 23, 42)
        canvas.drawRect(0f, 0f, width, 40f, paint)
        paint.color = Color.WHITE
        paint.textSize = 11f
        paint.isFakeBoldText = true
        canvas.drawText("VIN CHECK REPORT: ${report.vin} (${report.year} ${report.make} ${report.model})", 30f, 25f, paint)

        var y = 65f

        // 1. Section: NMVTIS Title Brands Check
        drawSectionHeader(canvas, "3. TITLE BRANDS & TOTAL LOSS CHECK (NMVTIS AUDIT)", y)
        y += 20f

        report.titleBrands.forEach { brand ->
            val isDanger = brand.severity == "DANGER"
            val isWarning = brand.severity == "WARNING"

            paint.color = when {
                isDanger -> Color.rgb(254, 242, 242)
                isWarning -> Color.rgb(255, 251, 235)
                else -> Color.rgb(240, 253, 244)
            }
            val rowRect = RectF(25f, y, width - 25f, y + 36f)
            canvas.drawRoundRect(rowRect, 4f, 4f, paint)

            paint.color = when {
                isDanger -> Color.rgb(220, 38, 38)
                isWarning -> Color.rgb(217, 119, 6)
                else -> Color.rgb(22, 101, 52)
            }
            paint.textSize = 11f
            paint.isFakeBoldText = true
            val statusTag = if (brand.isFlagged) "[FLAGGED / RECORDED]" else "[CLEAN / NO RECORD]"
            canvas.drawText("${brand.brandName} - $statusTag", 35f, y + 16f, paint)

            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 9f
            paint.isFakeBoldText = false
            canvas.drawText(brand.description, 35f, y + 30f, paint)

            y += 42f
        }

        // 2. Section: NHTSA Active Safety Recalls
        y += 15f
        drawSectionHeader(canvas, "4. SAFETY RECALLS (NHTSA CAMPAIGNS)", y)
        y += 20f

        if (report.recalls.isNotEmpty()) {
            report.recalls.take(2).forEach { recall ->
                paint.color = Color.rgb(254, 242, 242)
                val recallRect = RectF(25f, y, width - 25f, y + 68f)
                canvas.drawRoundRect(recallRect, 6f, 6f, paint)

                paint.color = Color.rgb(185, 28, 28)
                paint.textSize = 11f
                paint.isFakeBoldText = true
                canvas.drawText("CAMPAIGN #${recall.campaignNumber ?: "N/A"}: ${recall.component ?: "SAFETY"}", 35f, y + 18f, paint)

                paint.color = Color.rgb(51, 65, 85)
                paint.textSize = 9f
                paint.isFakeBoldText = false
                val summary = (recall.summary ?: "").take(100) + if ((recall.summary?.length ?: 0) > 100) "..." else ""
                canvas.drawText("Summary: $summary", 35f, y + 34f, paint)

                val remedy = (recall.remedy ?: "").take(100) + if ((recall.remedy?.length ?: 0) > 100) "..." else ""
                canvas.drawText("Remedy: $remedy", 35f, y + 50f, paint)

                y += 76f
            }
        } else {
            paint.color = Color.rgb(240, 253, 244)
            canvas.drawRoundRect(RectF(25f, y, width - 25f, y + 42f), 4f, 4f, paint)
            paint.color = Color.rgb(22, 101, 52)
            paint.textSize = 11f
            paint.isFakeBoldText = true
            canvas.drawText("NO UNREPAIRED SAFETY RECALLS REPORTED", 35f, y + 25f, paint)
            y += 48f
        }

        // 3. Section: External Verification Repositories
        y += 15f
        drawSectionHeader(canvas, "5. MULTI-SOURCE CROSS CHECK DIRECTORY", y)
        y += 20f

        report.externalSources.take(6).forEach { source ->
            paint.color = Color.rgb(248, 250, 252)
            canvas.drawRoundRect(RectF(25f, y, width - 25f, y + 26f), 3f, 3f, paint)

            paint.color = Color.rgb(30, 41, 59)
            paint.textSize = 10f
            paint.isFakeBoldText = true
            canvas.drawText(source.name, 35f, y + 17f, paint)

            paint.color = Color.rgb(100, 116, 139)
            paint.textSize = 8f
            paint.isFakeBoldText = false
            canvas.drawText(source.url.take(65), 230f, y + 17f, paint)

            y += 30f
        }

        // Page 2 Footer
        drawPageFooter(canvas, 2, 2)
    }

    private fun drawSectionHeader(canvas: Canvas, title: String, y: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(15, 23, 42)
            textSize = 12f
            isFakeBoldText = true
        }
        canvas.drawText(title, 25f, y, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.strokeWidth = 1f
        canvas.drawLine(25f, y + 5f, 570f, y + 5f, paint)
    }

    private fun drawKeyValueTable(canvas: Canvas, items: List<Pair<String, String>>, startY: Float): Float {
        var currentY = startY
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        items.forEachIndexed { index, (key, value) ->
            if (index % 2 == 0) {
                paint.color = Color.rgb(248, 250, 252)
                canvas.drawRect(25f, currentY - 12f, 570f, currentY + 6f, paint)
            }

            paint.color = Color.rgb(71, 85, 105)
            paint.textSize = 9.5f
            paint.isFakeBoldText = true
            canvas.drawText(key, 35f, currentY, paint)

            paint.color = Color.rgb(15, 23, 42)
            paint.textSize = 9.5f
            paint.isFakeBoldText = false
            canvas.drawText(value.take(60), 200f, currentY, paint)

            currentY += 18f
        }
        return currentY
    }

    private fun drawPageFooter(canvas: Canvas, pageNumber: Int, totalPages: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 8.5f
            isFakeBoldText = false
        }
        canvas.drawText("VIN Check Pro | Official Vehicle History & Salvage Report | Page $pageNumber of $totalPages", 25f, 825f, paint)
        canvas.drawText("CONFIDENTIAL REPORT", 470f, 825f, paint)
    }

    fun openPdf(context: Context, file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "مشاهده گزارش PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "برنامه‌ای جهت نمایش فایل PDF یافت نشد.", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdf(context: Context, file: File, vin: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "گزارش استعلام خودرو VIN: $vin")
                putExtra(Intent.EXTRA_TEXT, "گزارش جامع سوابق فنی، اصالت سند و تصادفات خودرو با شماره شاسی $vin")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "اشتراک‌گذاری گزارش PDF"))
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در اشتراک‌گذاری فایل PDF", Toast.LENGTH_SHORT).show()
        }
    }

    fun printPdf(context: Context, file: File, jobName: String = "VIN_Report") {
        try {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager == null) {
                Toast.makeText(context, "سرویس چاپ در دسترس نیست.", Toast.LENGTH_SHORT).show()
                return
            }

            val printAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback?,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback?.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder(jobName)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(2)
                        .build()
                    callback?.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor?,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback?
                ) {
                    try {
                        FileInputStream(file).use { input ->
                            FileOutputStream(destination?.fileDescriptor).use { output ->
                                val buffer = ByteArray(1024)
                                var bytesRead: Int
                                while (input.read(buffer).also { bytesRead = it } >= 0) {
                                    if (cancellationSignal?.isCanceled == true) {
                                        callback?.onWriteCancelled()
                                        return
                                    }
                                    output.write(buffer, 0, bytesRead)
                                }
                            }
                        }
                        callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback?.onWriteFailed(e.message)
                    }
                }
            }

            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        } catch (e: Exception) {
            Toast.makeText(context, "خطا در اتصال به سرویس چاپ", Toast.LENGTH_SHORT).show()
        }
    }
}
