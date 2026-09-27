package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vin_searches")
data class VinSearchEntity(
    @PrimaryKey val vin: String,
    val make: String,
    val model: String,
    val year: String,
    val titleStatus: String, // CLEAN, SALVAGE, REBUILT, WATER_FLOOD, JUNK_SCRAP
    val thumbnailUrl: String?,
    val primaryDamage: String?,
    val lotNumber: String?,
    val hasAuctionRecords: Boolean,
    val recallsCount: Int,
    val searchedAt: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val cachedReportJson: String // Full serialized report for 100% offline access
)
