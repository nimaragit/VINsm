package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

data class SampleVinItem(
    val vin: String,
    val title: String,
    val category: String
)

val SAMPLE_VINS = listOf(
    SampleVinItem("1FA6P8CF5H5528921", "فورد موستانگ (تصادفی Copart)", "Copart"),
    SampleVinItem("WBA5R1C50K4824412", "بی‌ام‌و 330i (تصادف بغل IAAI)", "IAAI"),
    SampleVinItem("4T1B11HK5MU129384", "تویوتا کمری (سند سالم Clean)", "Clean"),
    SampleVinItem("WAU4FAFL2CN017788", "آئودی A4 (سیل‌زده Flood)", "Flood")
)

@Composable
fun VinInputCard(
    vin: String,
    isLoading: Boolean,
    onVinChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("vin_input_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_vin_hero_banner_1790486666270),
                    contentDescription = "بررسی سوابق خودرو",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC0F172A))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "استعلام اصالت و سوابق بین‌المللی خودرو",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "NHTSA • Copart • IAAI • BidFax • stat.vin • NMVTIS",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // VIN Input Field
                OutlinedTextField(
                    value = vin,
                    onValueChange = onVinChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vin_text_field"),
                    label = { Text("شماره ۱۷ رقمی VIN / شماره شاسی") },
                    placeholder = { Text("مثال: 1FA6P8CF5H5528921") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (vin.isNotEmpty()) {
                                IconButton(
                                    onClick = { onVinChange("") },
                                    modifier = Modifier.testTag("clear_vin_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "پاک کردن"
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    clipboardManager.getText()?.text?.let { text ->
                                        onVinChange(text)
                                    }
                                },
                                modifier = Modifier.testTag("paste_vin_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "چسباندن"
                                )
                            }
                        }
                    },
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (vin.length == 17) "طول استاندارد VIN تایید شد" else "حروف مجاز: اعداد و حروف انگلیسی (به جز I, O, Q)",
                                fontSize = 11.sp,
                                color = if (vin.length == 17) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${vin.length} / 17",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (vin.length == 17) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            onSearch()
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quick test samples chips
                Text(
                    text = "نمونه‌های آماده جهت بررسی سریع:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SAMPLE_VINS.forEach { sample ->
                        SuggestionChip(
                            onClick = {
                                onVinChange(sample.vin)
                                onSearch()
                            },
                            label = {
                                Text(
                                    text = sample.title,
                                    fontSize = 11.sp
                                )
                            },
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        onSearch()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("search_vin_button"),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "در حال دریافت اطلاعات از مراجع بین‌المللی...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "استعلام جامع و بازیابی عکس‌های تصادف",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
