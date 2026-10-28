package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileUtils {

    fun createTempImageUri(context: Context): Pair<Uri, File> {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = File(context.cacheDir, "images").apply { mkdirs() }
        val imageFile = File.createTempFile("METER_${timeStamp}_", ".jpg", storageDir)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
        return Pair(uri, imageFile)
    }

    fun copyUriToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val imagesDir = File(context.filesDir, "meter_photos").apply { mkdirs() }
            val destFile = File(imagesDir, "meter_${timeStamp}.jpg")

            context.contentResolver.openInputStream(sourceUri)?.use { input: InputStream ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadBitmapFromPathOrUri(context: Context, pathOrUri: String): Bitmap? {
        return try {
            if (pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://")) {
                val uri = Uri.parse(pathOrUri)
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            } else {
                BitmapFactory.decodeFile(pathOrUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun savePdfToDownloads(context: Context, sourcePdfFile: File, fileName: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/KSEB_Estimates")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        sourcePdfFile.inputStream().use { input ->
                            input.copyTo(out)
                        }
                    }
                    uri
                } else null
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val targetDir = File(downloadsDir, "KSEB_Estimates").apply { mkdirs() }
                val targetFile = File(targetDir, fileName)
                sourcePdfFile.copyTo(targetFile, overwrite = true)
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", targetFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: return file provider URI from the source file
            try {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", sourcePdfFile)
            } catch (ex: Exception) {
                null
            }
        }
    }

    fun sharePdf(context: Context, pdfFile: File, shareToDrive: Boolean = false) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "KSEB Field Verification & Electrical Estimate: ${pdfFile.name}")
                putExtra(Intent.EXTRA_TEXT, "Attached is the field verification report and electrical estimate for KSEB.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                if (shareToDrive) {
                    // Try targeting Google Drive package if installed
                    setPackage("com.google.android.apps.docs")
                }
            }

            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                // If Google Drive package specific intent fails, show general chooser
                val chooser = Intent.createChooser(intent.apply { `package` = null }, "Share PDF or Upload to Drive")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
