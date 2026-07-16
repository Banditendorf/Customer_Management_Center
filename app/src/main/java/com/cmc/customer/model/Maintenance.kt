package com.cmc.customer.model

data class Maintenance(
    // -- Genel Kimlik Bilgileri --
    val id: String = "",
    val machineId: String = "",
    val machineName: String = "",
    val serialNumber: String = "",
    val companyId: String = "",
    val companyName: String = "",

    // -- Planlama Bilgileri --
    val plannedDate: String = "",
    val plannedTime: String = "",
    val workOrderNumber: String = "",
    val status: String = "planlandÄ±", // planlandÄ±, hazÄ±rlandÄ±, tamamlandÄ±, iptal

    // -- AÃ§Ä±klama ve Notlar --
    val description: String = "",
    val note: String = "",
    val preMaintenanceNote: String = "",
    val postMaintenanceNote: String = "",

    // -- Zaman Bilgileri --
    val startTime: String = "",
    val endTime: String = "",

    // -- ParÃ§a ve BakÄ±m Bilgileri --
    val parts: List<SparePart> = emptyList(),
    val extraParts: List<SparePart> = emptyList(),
    val changedParts: List<String> = emptyList(),

    // -- Sorumlu Personeller --
    val preparedBy: String = "",
    val checkedBy: String = "",
    val responsibles: List<String> = emptyList(),

    // -- YaÄŸ Bilgisi --
    val oilChanged: Boolean = false,
    val oilCode: String = "",
    val oilLiter: Double = 0.0,

    // -- Ã–lÃ§Ã¼m ve Teknik Veriler --
    val voltageL1: Float? = null,
    val currentL1: Float? = null,
    val pressure: Float? = null,

    // -- FotoÄŸraf & Dosya Bilgisi --
    val photoFolderName: String = "",

    // -- MCMCne Saat Bilgisi --
    val workingHourAtMaintenance: Int? = null,  // â† Yeni alan: bakÄ±m yapÄ±lan andCMC mCMCne saati

    // -- SonrCMC BakÄ±m Bilgisi --
    val nextMaintenanceTime: String = "",

    // -- KayÄ±t ZamanÄ± --
    val timestamp: Long = 0L
)
