package com.cmc.customer.model

data class SparePart(
    val id: String = "",         // Firestore ID (gerekirse)
    val code: String = "",        // ParÃ§a kodu
    val name: String = "",        // ParÃ§a adÄ±
    val category: String = "",    // Kategori (Ã¶rn: YaÄŸ Filtresi, Hava Filtresi)
    val shelf: String = "",       // Raf kodu (Ã¶rn: A3, B2)
    val brand: String = "",       // Marka (Ã¶rn: Mann Filter)
    val quantity: Int = 1,        // Adet
    val prepared: Boolean? = null
)
