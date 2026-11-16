package com.garment.hisabpro

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

object ReportManager {

    var restored: JSONObject = JSONObject()
        private set

    fun writeBackup(
        context: Context,
        uri: Uri,
        pieces: JSONArray,
        entries: JSONArray,
        expenses: JSONArray,
        payments: JSONArray
    ) {
        val json = JSONObject()
            .put("format", "GARMENT-HISAB-PRO-BACKUP")
            .put("version", 1)
            .put(
                "createdAt",
                SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    Locale.getDefault()
                ).format(Date())
            )
            .put("pieces", pieces)
            .put("entries", entries)
            .put("expenses", expenses)
            .put("payments", payments)

        val output = context.contentResolver.openOutputStream(uri, "w")
            ?: throw IllegalStateException("बैकअप फाइल नहीं खुली")

        output.use {
            OutputStreamWriter(it, Charsets.UTF_8).use { writer ->
                writer.write(json.toString(2))
            }
        }
    }

    fun readBackup(context: Context, uri: Uri): Boolean {
        return try {
            val text = context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(
                    InputStreamReader(input, Charsets.UTF_8)
                ).readText()
            } ?: return false

            val json = JSONObject(text)

            if (json.optString("format") != "GARMENT-HISAB-PRO-BACKUP") {
                return false
            }

            if (
                json.optJSONArray("pieces") == null ||
                json.optJSONArray("entries") == null ||
                json.optJSONArray("expenses") == null ||
                json.optJSONArray("payments") == null
            ) {
                return false
            }

            restored = json
            true

        } catch (_: Exception) {
            false
        }
    }

    private fun decodeSampledBitmap(path: String, maxWidth: Int, maxHeight: Int): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }

            BitmapFactory.decodeFile(path, bounds)

            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null
            }

            var sampleSize = 1

            while (
                bounds.outWidth / sampleSize > maxWidth * 2 ||
                bounds.outHeight / sampleSize > maxHeight * 2
            ) {
                sampleSize *= 2
            }

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
                inSampleSize = max(1, sampleSize)
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            BitmapFactory.decodeFile(path, options)

        } catch (_: Exception) {
            null
        }
    }

    fun writePdf(
        context: Context,
        uri: Uri,
        pieces: JSONArray,
        entries: JSONArray,
        expenses: JSONArray,
        payments: JSONArray
    ) {
        val document = PdfDocument()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 11f
            typeface = Typeface.create(
                Typeface.DEFAULT,
                Typeface.NORMAL
            )
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 18f
            typeface = Typeface.create(
                Typeface.DEFAULT,
                Typeface.BOLD
            )
        }

        val pageWidth = 595
        val pageHeight = 842

        var pageNumber = 1

        var page = document.startPage(
            PdfDocument.PageInfo.Builder(
                pageWidth,
                pageHeight,
                pageNumber
            ).create()
        )

        var canvas = page.canvas
        var y = 42f

        fun newPage() {
            document.finishPage(page)

            pageNumber++

            page = document.startPage(
                PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight,
                    pageNumber
                ).create()
            )

            canvas = page.canvas
            y = 42f
        }

        fun line(text: String, bold: Boolean = false) {
            if (y > pageHeight - 48) {
                newPage()
            }

            canvas.drawText(
                text.take(95),
                36f,
                y,
                if (bold) titlePaint else paint
            )

            y += if (bold) 28f else 18f
        }

        fun heading(text: String) {
            y += 8f
            line(text, true)
        }

        fun money(value: Double): String {
            return "Rs. " + String.format(Locale.US, "%.2f", value)
        }

        try {
            line("GARMENT HISAB PRO - REPORT", true)

            line(
                "Report date: " +
                    SimpleDateFormat(
                        "dd-MM-yyyy hh:mm a",
                        Locale.getDefault()
                    ).format(Date())
            )

            heading("PIECE / RATE MASTER")

            for (i in 0 until pieces.length()) {
                val p = pieces.optJSONObject(i) ?: continue

                line(
                    "${p.optString("name")} | Rate: ${money(p.optDouble("rate"))}"
                )
            }

            heading("PRODUCTION ENTRIES")

            for (i in 0 until entries.length()) {
                val e = entries.optJSONObject(i) ?: continue

                line(
                    "${e.optString("piece")} | Qty ${e.optDouble("qty")} x " +
                        "${money(e.optDouble("rate"))} = ${money(e.optDouble("amount"))}"
                )

                line(
                    "${e.optString("date")} ${e.optString("note")}"
                )

                val photos = e.optJSONArray("photos") ?: JSONArray()

                for (j in 0 until photos.length()) {
                    val path = photos.optString(j)

                    try {
                        val bitmap = decodeSampledBitmap(
                            path,
                            440,
                            260
                        )

                        if (bitmap != null) {
                            if (y > pageHeight - 180) {
                                newPage()
                            }

                            val maxW = 220f
                            val maxH = 130f

                            val scale = minOf(
                                maxW / bitmap.width,
                                maxH / bitmap.height,
                                1f
                            )

                            val width = (bitmap.width * scale).toInt().coerceAtLeast(1)
                            val height = (bitmap.height * scale).toInt().coerceAtLeast(1)

                            val scaled = Bitmap.createScaledBitmap(
                                bitmap,
                                width,
                                height,
                                true
                            )

                            canvas.drawBitmap(
                                scaled,
                                42f,
                                y,
                                null
                            )

                            y += height + 12f

                            if (scaled !== bitmap) {
                                scaled.recycle()
                            }

                            bitmap.recycle()
                        } else {
                            line("[Photo unavailable]")
                        }

                    } catch (_: Exception) {
                        line("[Photo unavailable]")
                    }
                }
            }

            heading("EXPENSES")

            for (i in 0 until expenses.length()) {
                val e = expenses.optJSONObject(i) ?: continue

                line(
                    "${e.optString("label")} | " +
                        "${money(e.optDouble("amount"))} | ${e.optString("date")}"
                )
            }

            heading("RECEIVED / ADVANCE")

            for (i in 0 until payments.length()) {
                val p = payments.optJSONObject(i) ?: continue

                line(
                    "${p.optString("label")} | " +
                        "${money(p.optDouble("amount"))} | ${p.optString("date")}"
                )
            }

            val earned = sum(entries, "amount")
            val spent = sum(expenses, "amount")
            val received = sum(payments, "amount")

            heading("SUMMARY")

            line("Total earnings: ${money(earned)}")
            line("Total expenses: ${money(spent)}")
            line("Received / advance: ${money(received)}")

            line(
                "Balance (earnings - expenses - received): " +
                    money(earned - spent - received)
            )

            document.finishPage(page)

            val output = context.contentResolver.openOutputStream(uri, "w")
                ?: throw IllegalStateException("PDF फाइल नहीं खुली")

            output.use {
                document.writeTo(it)
            }

        } finally {
            document.close()
        }
    }

    private fun sum(array: JSONArray, key: String): Double {
        var total = 0.0

        for (i in 0 until array.length()) {
            total += array.optJSONObject(i)?.optDouble(key, 0.0) ?: 0.0
        }

        return total
    }
}
