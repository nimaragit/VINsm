package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NhtsaDecodeResponse(
    @Json(name = "Count") val count: Int = 0,
    @Json(name = "Message") val message: String = "",
    @Json(name = "Results") val results: List<NhtsaDecodeResultItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NhtsaDecodeResultItem(
    @Json(name = "VIN") val vin: String? = null,
    @Json(name = "Make") val make: String? = null,
    @Json(name = "Model") val model: String? = null,
    @Json(name = "ModelYear") val modelYear: String? = null,
    @Json(name = "Manufacturer") val manufacturer: String? = null,
    @Json(name = "VehicleType") val vehicleType: String? = null,
    @Json(name = "BodyClass") val bodyClass: String? = null,
    @Json(name = "Doors") val doors: String? = null,
    @Json(name = "DriveType") val driveType: String? = null,
    @Json(name = "DisplacementL") val displacementL: String? = null,
    @Json(name = "EngineCylinders") val engineCylinders: String? = null,
    @Json(name = "EngineHP") val engineHP: String? = null,
    @Json(name = "FuelTypePrimary") val fuelTypePrimary: String? = null,
    @Json(name = "TransmissionStyle") val transmissionStyle: String? = null,
    @Json(name = "PlantCountry") val plantCountry: String? = null,
    @Json(name = "PlantCity") val plantCity: String? = null,
    @Json(name = "PlantState") val plantState: String? = null,
    @Json(name = "GVWR") val gvwr: String? = null,
    @Json(name = "Series") val series: String? = null,
    @Json(name = "Trim") val trim: String? = null,
    @Json(name = "ErrorCode") val errorCode: String? = null,
    @Json(name = "ErrorText") val errorText: String? = null
)

@JsonClass(generateAdapter = true)
data class NhtsaRecallsResponse(
    @Json(name = "Count") val count: Int = 0,
    @Json(name = "message") val message: String = "",
    @Json(name = "results") val results: List<NhtsaRecallItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class NhtsaRecallItem(
    @Json(name = "NHTSACampaignNumber") val campaignNumber: String? = null,
    @Json(name = "Component") val component: String? = null,
    @Json(name = "Summary") val summary: String? = null,
    @Json(name = "Conequence") val consequence: String? = null,
    @Json(name = "Remedy") val remedy: String? = null,
    @Json(name = "Notes") val notes: String? = null
)
