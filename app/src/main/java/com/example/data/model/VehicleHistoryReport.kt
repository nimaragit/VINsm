package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VehicleHistoryReport(
    val vin: String,
    val make: String,
    val model: String,
    val year: String,
    val manufacturer: String,
    val vehicleType: String,
    val bodyClass: String,
    val doors: String,
    val driveType: String,
    val engine: String,
    val cylinders: String,
    val displacementL: String,
    val fuelType: String,
    val transmission: String,
    val assemblyPlant: String,
    val countryOfOrigin: String,
    val gvwr: String,
    val titleStatusCategory: TitleStatusCategory,
    val titleSummary: String,
    val nmvtisClean: Boolean,
    val titleBrands: List<TitleBrandCheck> = emptyList(),
    val auctionRecords: List<AuctionDamageRecord> = emptyList(),
    val damagePhotos: List<DamagePhoto> = emptyList(),
    val recalls: List<NhtsaRecallItem> = emptyList(),
    val milestones: List<HistoryMilestone> = emptyList(),
    val externalSources: List<ExternalSourceLink> = emptyList(),
    val generatedAt: Long = System.currentTimeMillis(),
    val sourceSummary: String = "NHTSA + Copart / IAAI / NMVTIS / BidFax"
) {
    val titleDisplayBadge: String
        get() = when (titleStatusCategory) {
            TitleStatusCategory.CLEAN -> "سند سالم (Clean Title)"
            TitleStatusCategory.SALVAGE -> "تصادفی / سالویج (Salvage)"
            TitleStatusCategory.REBUILT -> "بازسازی شده (Rebuilt)"
            TitleStatusCategory.WATER_FLOOD -> "آسیب سیل (Flood / Water)"
            TitleStatusCategory.JUNK_SCRAP -> "اسقاطی (Junk / Scrap)"
        }

    val primaryPhotoUrl: String?
        get() = damagePhotos.firstOrNull()?.url
}
