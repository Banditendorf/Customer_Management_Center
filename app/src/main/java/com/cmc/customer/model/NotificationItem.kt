package com.cmc.customer.model

data class NotificationItem(
    val id: String,
    val message: String,
    val type: String,                            // FCM'den gelen "type"
    val timestamp: Long,
    val data: Map<String, String> = emptyMap(),  // FCM'den gelen data payload'Ä±
    val isPersistent: Boolean = false,
    val pinnedAtBottom: Boolean = false,         // ğŸ”½ AÅŸaÄŸÄ±ya sabitlenmiÅŸ mi?
    val isRead: Boolean = false,                 // ğŸ‘ï¸ Okundu bilgisi
    val category: String = "system"              // ğŸ—‚ï¸ maintenance / stock / system
)
