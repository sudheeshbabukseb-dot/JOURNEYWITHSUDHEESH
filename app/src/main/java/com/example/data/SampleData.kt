package com.example.data

object SampleData {
    fun getInitialSample(): VerificationRecord {
        return VerificationRecord(
            id = 0,
            applicantName = "Manoj Kumar P.",
            applicationNo = "KL-2024-EST-8842",
            applicantContactNo = "9847123456",
            completionCertificate = true,
            ownershipCertificate = true,
            wiring = "Good",
            earthResistance = "Good",
            rccbCompanyName = "Legrand 30mA 40A",
            requirementPower = "Single Phase",
            nearestConsumerNo = "1155420087421",
            purposeOfConnection = "Domestic",
            tariff = "LT-1A",
            poleNo = "KP-14/08",
            weatherproofWireLength = "28",
            otherConsentRequired = false,
            gpsCoordinates = "9.9816° N, 76.2999° E",
            gpsAccuracy = "± 3.2 m",
            transformerName = "Kadavanthra South 250kVA",
            photoUri = null,
            dateTime = "2024-10-28 10:30 AM",
            googleDriveLink = "https://drive.google.com",
            estimatePreparedBy = "Sudheesh Babu (Sub Engineer)",
            remarks = "Premises verified physically. Connected load within permissible limits. RCCB tested and working satisfactorily with test button. Earth pipe electrode installed at 3 meters depth with salt and charcoal packing. Service line can be drawn directly from pole KP-14/08 with 28 meters weatherproof wire without crossing neighboring property."
        )
    }
}
