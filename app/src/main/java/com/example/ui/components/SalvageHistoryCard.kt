package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuctionDamageRecord

@Composable
fun SalvageHistoryCard(
    auctionRecord: AuctionDamageRecord,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("salvage_history_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CarCrash,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "پرونده مزایده خسارت کلی (${auctionRecord.auctionHouse})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = "لات: ${auctionRecord.lotNumber}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val recordFields = listOf(
                "پایگاه مزایده‌گذار" to auctionRecord.auctionHouse,
                "شماره لات مزایده (Lot #)" to auctionRecord.lotNumber,
                "تاریخ برگزاری حراج" to auctionRecord.saleDate,
                "آسیب اولیه (Primary Damage)" to auctionRecord.primaryDamage,
                "آسیب ثانویه (Secondary Damage)" to (auctionRecord.secondaryDamage ?: "گزارش نشده"),
                "علت ثبت خسارت (Loss Type)" to auctionRecord.lossType,
                "نوع سند صادره (Title)" to auctionRecord.titleType,
                "کیلومترشمار ثبت‌شده" to "${auctionRecord.odometer} (${auctionRecord.odometerStatus})",
                "سوییچ خودرو (Keys)" to auctionRecord.keysStatus,
                "وضعیت موتور و حرکت" to auctionRecord.runAndDrive,
                "ارزش تخمینی پیش از تصادف" to auctionRecord.estimatedRetailValue,
                "برآورد هزینه تعمیر بیمه" to auctionRecord.estimatedRepairCost,
                "آخرین پیشنهاد قیمت (Bid)" to (auctionRecord.finalBid ?: "در دست بررسی"),
                "شرکت بیمه واگذارکننده" to (auctionRecord.seller ?: "کنسرسیوم بیمه‌ای"),
                "محل نگهداری و انبار حراجی" to auctionRecord.auctionLocation
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                recordFields.forEachIndexed { index, (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = value,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (label.contains("آسیب") || label.contains("سند")) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (index < recordFields.size - 1) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }

            if (!auctionRecord.auctionUrl.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(auctionRecord.auctionUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "مشاهده زنده این خودرو در وب‌سایت ${auctionRecord.auctionHouse}",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
