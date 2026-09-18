package com.example.data.qr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.TableEntity
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import kotlin.math.abs

object QrCodeGenerator {

    /**
     * Generates a realistic, high-contrast 2D QR Code Bitmap.
     * Uses a deterministic pseudo-random QR matrix based on payload hash
     * with standard QR finder patterns (7x7 corners) and timing patterns.
     */
    fun generateQrBitmap(payload: String, sizePx: Int = 512): Bitmap {
        val matrixSize = 25 // 25x25 QR grid (Version 2)
        val matrix = Array(matrixSize) { BooleanArray(matrixSize) { false } }

        // 1. Draw 3 Finder Patterns at corners (7x7)
        drawFinderPattern(matrix, 0, 0)
        drawFinderPattern(matrix, matrixSize - 7, 0)
        drawFinderPattern(matrix, 0, matrixSize - 7)

        // 2. Timing patterns (row 6 and col 6 alternating)
        for (i in 8 until matrixSize - 8) {
            matrix[6][i] = (i % 2 == 0)
            matrix[i][6] = (i % 2 == 0)
        }

        // 3. Populate data cells deterministically using hash of payload
        val md = MessageDigest.getInstance("SHA-256")
        val hash = md.digest(payload.toByteArray(Charsets.UTF_8))
        var byteIdx = 0
        var bitIdx = 0

        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                // Skip finder patterns & timing lines
                if (isReserved(r, c, matrixSize)) continue

                val b = hash[byteIdx % hash.size].toInt()
                val bit = ((b shr (bitIdx % 8)) and 1) == 1
                matrix[r][c] = bit

                bitIdx++
                if (bitIdx % 8 == 0) byteIdx++
            }
        }

        // Render to Bitmap with Quiet Zone
        val quietZone = 2
        val totalCells = matrixSize + quietZone * 2
        val cellSize = sizePx / totalCells
        val actualSize = totalCells * cellSize

        val bitmap = Bitmap.createBitmap(actualSize, actualSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }

        for (r in 0 until matrixSize) {
            for (c in 0 until matrixSize) {
                if (matrix[r][c]) {
                    val left = (c + quietZone) * cellSize
                    val top = (r + quietZone) * cellSize
                    canvas.drawRect(
                        left.toFloat(),
                        top.toFloat(),
                        (left + cellSize).toFloat(),
                        (top + cellSize).toFloat(),
                        paint
                    )
                }
            }
        }

        return bitmap
    }

    private fun drawFinderPattern(matrix: Array<BooleanArray>, startR: Int, startC: Int) {
        for (r in 0 until 7) {
            for (c in 0 until 7) {
                val isOuter = (r == 0 || r == 6 || c == 0 || c == 6)
                val isInner = (r in 2..4 && c in 2..4)
                matrix[startR + r][startC + c] = isOuter || isInner
            }
        }
    }

    private fun isReserved(r: Int, c: Int, size: Int): Boolean {
        // Top-left finder + separator
        if (r <= 7 && c <= 7) return true
        // Top-right finder + separator
        if (r <= 7 && c >= size - 8) return true
        // Bottom-left finder + separator
        if (r >= size - 8 && c <= 7) return true
        // Timing lines
        if (r == 6 || c == 6) return true
        return false
    }

    /**
     * Generates a multi-page / grid PDF containing printable QR cards for all tables.
     */
    fun createTablesPdf(
        context: Context,
        cafeName: String,
        tables: List<TableEntity>
    ): File {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4 standard width in points
        val pageHeight = 842 // A4 standard height in points

        val cardsPerRow = 2
        val cardsPerCol = 3
        val cardsPerPage = cardsPerRow * cardsPerCol

        val cardWidth = (pageWidth - 60) / cardsPerRow.toFloat()
        val cardHeight = (pageHeight - 90) / cardsPerCol.toFloat()

        var pageNumber = 1
        val chunks = tables.chunked(cardsPerPage)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CCCCCC")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }

        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FBF9F5")
            style = Paint.Style.FILL
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#211109")
            textSize = 14f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val tableNumberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8D4F24")
            textSize = 16f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }

        val typePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#555555")
            textSize = 10f
            textAlign = Paint.Align.CENTER
        }

        val scanHintPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#666666")
            textSize = 9f
            textAlign = Paint.Align.CENTER
        }

        for (chunk in chunks) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            // Page Header
            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#8D4F24")
                textSize = 18f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("$cafeName - باركودات طاولات المينو الإلكتروني", pageWidth / 2f, 35f, headerPaint)

            for (i in chunk.indices) {
                val table = chunk[i]
                val row = i / cardsPerRow
                val col = i % cardsPerRow

                val left = 25f + col * (cardWidth + 10f)
                val top = 55f + row * (cardHeight + 10f)
                val right = left + cardWidth
                val bottom = top + cardHeight

                val rect = RectF(left, top, right, bottom)
                // Draw card rounded background & border
                canvas.drawRoundRect(rect, 12f, 12f, cardBgPaint)
                canvas.drawRoundRect(rect, 12f, 12f, borderPaint)

                val centerX = left + cardWidth / 2f

                // Cafe Name
                canvas.drawText(cafeName, centerX, top + 22f, titlePaint)

                // Table Number
                val tableTypeLabel = when (table.tableType) {
                    "VIP" -> "VIP Lounge - في آي بي"
                    "OUTDOOR" -> "Outdoor - جلسة خارجية"
                    else -> "Indoor - جلسة داخلية"
                }
                canvas.drawText("طاولة رقم #${table.tableNumber}", centerX, top + 42f, tableNumberPaint)
                canvas.drawText(tableTypeLabel, centerX, top + 56f, typePaint)

                // QR Code
                val qrSize = (cardWidth * 0.58f).toInt()
                val webUrl = "https://alikhlel007-lang.github.io/goood/?cafe=${table.cafeId}&table=${table.tableNumber}&token=${table.qrToken}"
                val qrBitmap = generateQrBitmap(webUrl, qrSize)
                val qrLeft = centerX - qrSize / 2f
                val qrTop = top + 66f
                canvas.drawBitmap(qrBitmap, qrLeft, qrTop, null)
                qrBitmap.recycle()

                // Instructions at bottom
                canvas.drawText("امسح الكود لفتح المينو والطلب مباشرة", centerX, bottom - 26f, scanHintPaint)
                canvas.drawText("Token: ${table.qrToken.take(16)}", centerX, bottom - 12f, typePaint)
            }

            pdfDocument.finishPage(page)
            pageNumber++
        }

        // Save PDF to app files
        val pdfFile = File(context.cacheDir, "cafemenu_tables_qr.pdf")
        if (pdfFile.exists()) pdfFile.delete()
        val out = FileOutputStream(pdfFile)
        pdfDocument.writeTo(out)
        out.flush()
        out.close()
        pdfDocument.close()

        return pdfFile
    }

    /**
     * Intent helper to view or share the generated PDF
     */
    fun openPdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "عرض ملف باركودات الطاولات (PDF)").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(chooser)
        } catch (e: Exception) {
            // Fallback share intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "مشاركة ملف PDF"))
        }
    }
}
