package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "field_verifications")
data class VerificationRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val applicantName: String = "",
    val applicationNo: String = "",
    val applicantContactNo: String = "",
    val completionCertificate: Boolean = true, // Yes / No
    val ownershipCertificate: Boolean = true, // Yes / No
    val wiring: String = "Good", // Good / Bad
    val earthResistance: String = "Good", // Good / Bad
    val rccbCompanyName: String = "",
    val requirementPower: String = "Single Phase", // Single Phase / Three Phase
    val nearestConsumerNo: String = "", // Mandatory
    val purposeOfConnection: String = "Domestic",
    val tariff: String = "LT-1A",
    val poleNo: String = "",
    val weatherproofWireLength: String = "",
    val otherConsentRequired: Boolean = false, // Yes / No
    val gpsCoordinates: String = "",
    val gpsAccuracy: String = "",
    val transformerName: String = "",
    val photoUri: String? = null,
    val dateTime: String = "",
    val googleDriveLink: String = "",
    val estimatePreparedBy: String = "",
    val remarks: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
