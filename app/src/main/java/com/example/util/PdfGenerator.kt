package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.VerificationRecord
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    // Standard A4 dimensions at 72 dpi
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun generateVerificationPdf(context: Context, record: VerificationRecord): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        drawPdfContent(context, canvas, record)

        document.finishPage(page)

        val cleanAppNo = if (record.applicationNo.isNotBlank()) {
            record.applicationNo.replace(Regex("[^a-zA-Z0-9]"), "_")
        } else {
            "RECORD_${record.id}"
        }
        val fileName = "KSEB_Verification_${cleanAppNo}.pdf"
        val outputDir = File(context.cacheDir, "pdf_exports").apply { mkdirs() }
        val outputFile = File(outputDir, fileName)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    private fun drawPdfContent(context: Context, canvas: Canvas, record: VerificationRecord) {
        val margin = 32f
        val contentWidth = PAGE_WIDTH - (margin * 2)

        // Background
        val bgPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // Outer Border
        val borderPaint = Paint().apply {
            color = Color.rgb(11, 79, 156) // KSEB Blue
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRect(margin - 8, margin - 8, PAGE_WIDTH - margin + 8, PAGE_HEIGHT - margin + 8, borderPaint)

        // Header Background Banner
        val headerPaint = Paint().apply {
            color = Color.rgb(11, 79, 156)
            style = Paint.Style.FILL
        }
        canvas.drawRect(margin, margin, PAGE_WIDTH - margin, margin + 50f, headerPaint)

        // Header Texts
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "KERALA STATE ELECTRICITY BOARD LIMITED",
            PAGE_WIDTH / 2f,
            margin + 20f,
            titlePaint
        )

        val subTitlePaint = Paint().apply {
            color = Color.rgb(255, 214, 0) // Electric Yellow
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "FIELD VERIFICATION & ELECTRICAL ESTIMATE REPORT",
            PAGE_WIDTH / 2f,
            margin + 34f,
            subTitlePaint
        )

        val secOfficePaint = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "Electrical Section Office • Service Connection Verification",
            PAGE_WIDTH / 2f,
            margin + 44f,
            secOfficePaint
        )

        var curY = margin + 64f

        // Document Info Bar
        val metaPaint = Paint().apply {
            color = Color.rgb(60, 60, 60)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val metaBoldPaint = Paint().apply {
            color = Color.rgb(11, 79, 156)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val formattedDate = record.dateTime.ifBlank {
            SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(Date())
        }

        canvas.drawText("Application No: ", margin, curY, metaPaint)
        val appNoWidth = metaPaint.measureText("Application No: ")
        canvas.drawText(record.applicationNo.ifBlank { "N/A" }, margin + appNoWidth, curY, metaBoldPaint)

        val dateLabel = "Date & Time: "
        val dateWidth = metaPaint.measureText(dateLabel) + metaBoldPaint.measureText(formattedDate)
        canvas.drawText(dateLabel, PAGE_WIDTH - margin - dateWidth, curY, metaPaint)
        canvas.drawText(formattedDate, PAGE_WIDTH - margin - metaBoldPaint.measureText(formattedDate), curY, metaBoldPaint)

        curY += 14f

        // Divider
        val dividerPaint = Paint().apply {
            color = Color.rgb(200, 210, 225)
            strokeWidth = 1f
        }
        canvas.drawLine(margin, curY, PAGE_WIDTH - margin, curY, dividerPaint)
        curY += 12f

        // SECTION 1: APPLICANT & POWER REQUIREMENT
        curY = drawSectionHeader(canvas, "1. APPLICANT & SUPPLY DETAILS", margin, curY, contentWidth)

        val col1X = margin + 4f
        val col2X = margin + (contentWidth / 2f) + 4f

        curY = drawKeyValue(canvas, "Applicant Name:", record.applicantName.ifBlank { "N/A" }, col1X, curY)
        drawKeyValue(canvas, "Applicant Contact No:", record.applicantContactNo.ifBlank { "N/A" }, col2X, curY - 14f)

        curY = drawKeyValue(canvas, "Nearest Consumer No (Mandatory):", record.nearestConsumerNo.ifBlank { "N/A" }, col1X, curY, isHighlight = true)
        drawKeyValue(canvas, "Requirement Power:", record.requirementPower, col2X, curY - 14f, isHighlight = true)

        curY = drawKeyValue(canvas, "Purpose of Connection:", record.purposeOfConnection.ifBlank { "N/A" }, col1X, curY)
        drawKeyValue(canvas, "Tariff Category:", record.tariff.ifBlank { "N/A" }, col2X, curY - 14f)

        curY += 4f

        // SECTION 2: TECHNICAL & NETWORK PARAMETERS
        curY = drawSectionHeader(canvas, "2. DISTRIBUTION NETWORK & TECHNICAL SPECIFICATIONS", margin, curY, contentWidth)

        curY = drawKeyValue(canvas, "Pole Number:", record.poleNo.ifBlank { "N/A" }, col1X, curY)
        drawKeyValue(canvas, "Weatherproof Wire Length:", if (record.weatherproofWireLength.isNotBlank()) "${record.weatherproofWireLength} Meters" else "N/A", col2X, curY - 14f)

        curY = drawKeyValue(canvas, "Transformer Name/Code:", record.transformerName.ifBlank { "N/A" }, col1X, curY)
        drawKeyValue(canvas, "RCCB Company/Specs:", record.rccbCompanyName.ifBlank { "N/A" }, col2X, curY - 14f)

        curY = drawKeyValue(canvas, "Internal Wiring Condition:", record.wiring, col1X, curY, isStatus = true, isPositive = record.wiring.equals("Good", true))
        drawKeyValue(canvas, "Earth Resistance Value:", record.earthResistance, col2X, curY - 14f, isStatus = true, isPositive = record.earthResistance.equals("Good", true))

        curY += 4f

        // SECTION 3: STATUTORY CERTIFICATES & COMPLIANCE
        curY = drawSectionHeader(canvas, "3. STATUTORY COMPLIANCE & CERTIFICATES", margin, curY, contentWidth)

        curY = drawKeyValue(canvas, "Completion Certificate Submitted:", if (record.completionCertificate) "YES" else "NO", col1X, curY, isStatus = true, isPositive = record.completionCertificate)
        drawKeyValue(canvas, "Ownership Certificate Submitted:", if (record.ownershipCertificate) "YES" else "NO", col2X, curY - 14f, isStatus = true, isPositive = record.ownershipCertificate)

        curY = drawKeyValue(canvas, "Any Other Consent Required:", if (record.otherConsentRequired) "YES" else "NO", col1X, curY, isStatus = true, isPositive = !record.otherConsentRequired)
        drawKeyValue(canvas, "GPS Coordinates:", record.gpsCoordinates.ifBlank { "Not Recorded" }, col2X, curY - 14f)

        if (record.gpsAccuracy.isNotBlank()) {
            curY = drawKeyValue(canvas, "GPS Accuracy:", record.gpsAccuracy, col2X, curY)
        }

        curY += 6f

        // SECTION 4: METER BOARD PHOTO & REMARKS
        curY = drawSectionHeader(canvas, "4. FIELD INSPECTION EVIDENCE & REMARKS", margin, curY, contentWidth)

        // Draw side-by-side or stacked: Photo on right, remarks on left
        val photoBoxWidth = 150f
        val photoBoxHeight = 110f
        val photoLeft = PAGE_WIDTH - margin - photoBoxWidth - 4f
        val photoTop = curY + 4f

        // Load photo if available
        val photoBitmap: Bitmap? = if (!record.photoUri.isNullOrBlank()) {
            FileUtils.loadBitmapFromPathOrUri(context, record.photoUri)
        } else null

        if (photoBitmap != null) {
            val photoPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            val dstRect = RectF(photoLeft, photoTop, photoLeft + photoBoxWidth, photoTop + photoBoxHeight)
            canvas.drawBitmap(photoBitmap, null, dstRect, photoPaint)

            val framePaint = Paint().apply {
                color = Color.rgb(11, 79, 156)
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRect(dstRect, framePaint)

            val tagPaint = Paint().apply {
                color = Color.WHITE
                textSize = 7f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val tagBgPaint = Paint().apply {
                color = Color.argb(190, 0, 0, 0)
                style = Paint.Style.FILL
            }
            val tagRect = RectF(photoLeft, photoTop + photoBoxHeight - 16f, photoLeft + photoBoxWidth, photoTop + photoBoxHeight)
            canvas.drawRect(tagRect, tagBgPaint)
            canvas.drawText("Premises / Meter Board Photo", photoLeft + 6f, photoTop + photoBoxHeight - 5f, tagPaint)
        } else {
            // Draw placeholder frame
            val placeholderPaint = Paint().apply {
                color = Color.rgb(240, 244, 250)
                style = Paint.Style.FILL
            }
            val framePaint = Paint().apply {
                color = Color.rgb(180, 195, 215)
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val placeholderRect = RectF(photoLeft, photoTop, photoLeft + photoBoxWidth, photoTop + photoBoxHeight)
            canvas.drawRect(placeholderRect, placeholderPaint)
            canvas.drawRect(placeholderRect, framePaint)

            val textPaint = Paint().apply {
                color = Color.rgb(120, 135, 155)
                textSize = 8f
                textAlign = Paint.Align.CENTER
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            }
            canvas.drawText("Meter Board / Premises Photo", photoLeft + (photoBoxWidth / 2f), photoTop + 50f, textPaint)
            canvas.drawText("[Not Attached]", photoLeft + (photoBoxWidth / 2f), photoTop + 65f, textPaint)
        }

        // Draw Remarks on the left of photo
        val remarksWidth = photoLeft - margin - 12f
        val remarksLabelPaint = Paint().apply {
            color = Color.rgb(11, 79, 156)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText("Field Remarks / Observations:", margin + 4f, curY + 12f, remarksLabelPaint)

        val remarksText = record.remarks.ifBlank { "No special remarks noted. Installation complies with standard service connection specifications." }
        val remarksPaint = Paint().apply {
            color = Color.rgb(40, 40, 40)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val lines = wrapText(remarksText, remarksPaint, remarksWidth)
        var remarkY = curY + 26f
        for (i in 0 until minOf(lines.size, 8)) {
            canvas.drawText(lines[i], margin + 4f, remarkY, remarksPaint)
            remarkY += 12f
        }

        curY = maxOf(remarkY, photoTop + photoBoxHeight) + 12f

        // Google Drive link if available
        if (record.googleDriveLink.isNotBlank()) {
            curY = drawKeyValue(canvas, "Google Drive Reference Link:", record.googleDriveLink, col1X, curY)
            curY += 4f
        }

        // SECTION 5: SIGNATURE & VERIFICATION
        val signTop = PAGE_HEIGHT - margin - 75f
        val signLinePaint = Paint().apply {
            color = Color.rgb(180, 190, 205)
            strokeWidth = 1f
        }
        canvas.drawLine(margin, signTop, PAGE_WIDTH - margin, signTop, signLinePaint)

        val signTextPaint = Paint().apply {
            color = Color.rgb(50, 50, 50)
            textSize = 8.5f
            isAntiAlias = true
        }
        val signBoldPaint = Paint().apply {
            color = Color.rgb(11, 79, 156)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        canvas.drawText("Estimate Prepared By:", margin + 4f, signTop + 18f, signTextPaint)
        canvas.drawText(record.estimatePreparedBy.ifBlank { "Sub Engineer / Overseer" }, margin + 4f, signTop + 34f, signBoldPaint)
        canvas.drawText("KSEB Electrical Section", margin + 4f, signTop + 48f, signTextPaint)

        val officerSignLabel = "Authorized Signature & Stamp"
        val signLabelWidth = signTextPaint.measureText(officerSignLabel)
        canvas.drawText(officerSignLabel, PAGE_WIDTH - margin - signLabelWidth - 4f, signTop + 48f, signTextPaint)
        canvas.drawLine(PAGE_WIDTH - margin - signLabelWidth - 20f, signTop + 38f, PAGE_WIDTH - margin - 4f, signTop + 38f, signLinePaint)

        // Footer
        val footerPaint = Paint().apply {
            color = Color.rgb(120, 135, 150)
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(
            "Kerala State Electricity Board Ltd. • Field Verification Portal • Blog: www.journeywithsudheesh.in",
            PAGE_WIDTH / 2f,
            PAGE_HEIGHT - margin + 2f,
            footerPaint
        )
    }

    private fun drawSectionHeader(canvas: Canvas, title: String, left: Float, top: Float, width: Float): Float {
        val bannerPaint = Paint().apply {
            color = Color.rgb(235, 242, 252)
            style = Paint.Style.FILL
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(180, 205, 235)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val textPaint = Paint().apply {
            color = Color.rgb(11, 79, 156)
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val height = 18f
        val rect = RectF(left, top, left + width, top + height)
        canvas.drawRect(rect, bannerPaint)
        canvas.drawRect(rect, borderPaint)
        canvas.drawText(title, left + 8f, top + 13f, textPaint)

        return top + height + 10f
    }

    private fun drawKeyValue(
        canvas: Canvas,
        key: String,
        value: String,
        x: Float,
        y: Float,
        isHighlight: Boolean = false,
        isStatus: Boolean = false,
        isPositive: Boolean = true
    ): Float {
        val keyPaint = Paint().apply {
            color = Color.rgb(90, 100, 115)
            textSize = 8.5f
            isAntiAlias = true
        }
        val valuePaint = Paint().apply {
            color = if (isHighlight) {
                Color.rgb(11, 79, 156)
            } else if (isStatus) {
                if (isPositive) Color.rgb(46, 125, 50) else Color.rgb(198, 40, 40)
            } else {
                Color.rgb(25, 25, 25)
            }
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, if (isHighlight || isStatus) Typeface.BOLD else Typeface.NORMAL)
            isAntiAlias = true
        }

        canvas.drawText(key, x, y, keyPaint)
        val keyWidth = keyPaint.measureText(key)
        canvas.drawText("  $value", x + keyWidth, y, valuePaint)

        return y + 14f
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}
