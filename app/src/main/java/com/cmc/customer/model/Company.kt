package com.cmc.customer.model

data class Company(
    val id: String = "",
    val name: String = "",
    val contactPerson: String = "",
    val contactNumber: String = "",
    val role: String = "",
    val location: String? = null,
    val note: String? = null,
    val latitude: Double? = null, // ğŸ”¥ Yeni
    val longitude: Double? = null // ğŸ”¥ Yeni
)
