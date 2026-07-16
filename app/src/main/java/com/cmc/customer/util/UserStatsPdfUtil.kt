package com.cmc.customer.util

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class UserStats(
    val userName: String,
    val processCount: Int
)

// --- PDF export fonksiyonu ---
fun exportUserStatsToPDF(context: Context, stats: List<UserStats>) {
    // 1. PDF dokÃ¼manÄ±nÄ± oluÅŸtur
    val pdf = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint().apply { textSize = 12f }

    // 2. BaÅŸlÄ±k ve sÃ¼tun baÅŸlÄ±ÄŸÄ±
    canvas.drawText("Personel Ä°ÅŸlem Raporu", 40f, 50f, paint)
    var y = 80f
    canvas.drawText("Ad | Toplam Ä°ÅŸlem", 40f, y, paint)
    y += 25f

    // 3. Her bir kullanÄ±cÄ± iÃ§in satÄ±r
    stats.forEach {
        val line = "${it.userName} | ${it.processCount}"
        canvas.drawText(line, 40f, y, paint)
        y += 20f
    }

    // 4. SayfayÄ± bitir
    pdf.finishPage(page)

    // 5. Download dizinine yaz
    val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
    // 6. Dosya adÄ±nÄ± kullanÄ±cÄ± adÄ± ve tarih formatÄ±nda oluÅŸtur
    val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
    // BoÅŸluk karakterlerini alt Ã§izgiye Ã§evir (Ã¶rn: "Abdullah Kanat" -> "Abdullah_Kanat")
    val safeName = stats.first().userName.replace(" ", "_")
    val fileName = "${safeName}_$dateStr.pdf"
    val outFile = File(downloads, fileName)

    // 7. PDFâ€™i kaydet ve kapat
    FileOutputStream(outFile).use { stream ->
        pdf.writeTo(stream)
    }
    pdf.close()
}
