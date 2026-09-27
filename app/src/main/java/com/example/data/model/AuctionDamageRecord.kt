package com.example.data.model

import com.squareup.moshi.JsonClass

enum class TitleStatusCategory {
    CLEAN,
    SALVAGE,
    REBUILT,
    WATER_FLOOD,
    JUNK_SCRAP
}

@JsonClass(generateAdapter = true)
data class DamagePhoto(
    val url: String,
    val angle: String,
    val description: String = ""
)

@JsonClass(generateAdapter = true)
data class AuctionDamageRecord(
    val auctionHouse: String, // Copart, IAAI, BidFax, stat.vin
    val lotNumber: String,
    val saleDate: String,
    val primaryDamage: String,
    val secondaryDamage: String? = null,
    val lossType: String,
    val titleType: String,
    val titleCategory: TitleStatusCategory,
    val odometer: String,
    val odometerStatus: String = "Actual",
    val keysStatus: String,
    val runAndDrive: String,
    val estimatedRetailValue: String,
    val estimatedRepairCost: String,
    val finalBid: String? = null,
    val seller: String? = null,
    val auctionLocation: String,
    val auctionUrl: String? = null,
    val photos: List<DamagePhoto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TitleBrandCheck(
    val brandName: String,
    val description: String,
    val isFlagged: Boolean,
    val severity: String // "SAFE", "WARNING", "DANGER"
)

@JsonClass(generateAdapter = true)
data class HistoryMilestone(
    val date: String,
    val event: String,
    val location: String,
    val odometer: String? = null,
    val details: String
)

@JsonClass(generateAdapter = true)
data class ExternalSourceLink(
    val name: String,
    val description: String,
    val url: String,
    val isFree: Boolean,
    val category: String // "Decoder", "Auction", "Europe", "US Title"
)
