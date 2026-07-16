package com.cmc.customer.model

data class Material(
    val code: String = "",
    val shelf: String = "",
    val category: String = "",
    val stock: Int = 0,
    val kritikStok: Int = 0,
    val description: String = "", // AÃ§Ä±klama isteÄŸe baÄŸlÄ± olarak kalÄ±yor
    val lastUsedTimestamp: Long? = null,
    val updatedAt: Long? = null

)
