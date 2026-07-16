package com.cmc.customer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmc.customer.model.*
import com.cmc.customer.util.calendar.RetrofitInstance
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


class MaintenanceViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _notificationList = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notificationList: StateFlow<List<NotificationItem>> = _notificationList


    private val _maintenances = MutableStateFlow<List<Maintenance>>(emptyList())
    val maintenances: StateFlow<List<Maintenance>> = _maintenances

    val materials = MutableStateFlow<List<SparePart>>(emptyList())
    val userList = MutableStateFlow<List<User>>(emptyList())


    val holidayDates = MutableStateFlow<Set<String>>(emptySet())


    fun loadMachines(onResult: (List<Machine>) -> Unit) {
        db.collection("machines")
            .get()
            .addOnSuccessListener { snapshot ->
                val machineList = snapshot.documents.mapNotNull { it.toObject(Machine::class.java) }
                onResult(machineList)
            }
    }


    fun planla(bCMCm: Maintenance, onComplete: () -> Unit) {
        val id = if (bCMCm.id.isBlank()) UUID.randomUUID().toString() else bCMCm.id
        val timestamp = System.currentTimeMillis()
        val formattedNow = SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.getDefault()).format(Date(timestamp))

        db.collection("machines")
            .document(bCMCm.machineId)
            .get()
            .addOnSuccessListener { snapshot ->
                val updatedBCMCm = bCMCm.copy(
                    id = id,
                    serialNumber = snapshot.getString("serialNumber") ?: "",
                    companyId = snapshot.getString("companyId") ?: "",
                    companyName = snapshot.getString("companyName") ?: "",
                    machineName = snapshot.getString("name") ?: bCMCm.machineId,
                    timestamp = timestamp
                )

                db.collection("plannedMaintenances")
                    .document(id)
                    .set(updatedBCMCm)
                    .addOnSuccessListener {
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.e("MaintenanceVM", "BakÄ±m kaydÄ± yapÄ±lamadÄ±", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("MaintenanceVM", "MCMCne bilgisi okunamadÄ±", e)
            }
    }

    fun updateMaintenance(maintenance: Maintenance, onComplete: () -> Unit) {
        db.collection("plannedMaintenances")
            .document(maintenance.id) // âœ… sadece bu yeterli
            .set(maintenance)
            .addOnSuccessListener { onComplete() }
            .addOnFailureListener { e ->
                Log.e("MaintenanceVM", "BakÄ±m gÃ¼ncellenemedi", e)
            }
    }
    fun getIncompleteMaintenancesByDate(localDate: LocalDate, onResult: (List<Maintenance>) -> Unit) {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val formattedDate = localDate.format(formatter)

        db.collection("plannedMaintenances")
            .whereEqualTo("plannedDate", formattedDate)
            .get()
            .addOnSuccessListener { snapshot ->
                val result = snapshot.documents.mapNotNull { it.toObject(Maintenance::class.java) }
                    .filter { it.status.trim().lowercase() != "tamamlandÄ±" }
                onResult(result)
            }
            .addOnFailureListener { e ->
                Log.e("MaintenanceVM", "Veri Ã§ekilemedi: ${e.message}")
                onResult(emptyList())
            }
    }


    fun updatePreparation(bCMCmId: String, parts: List<SparePart>) {
        db.collection("plannedMaintenances")
            .document(bCMCmId)
            .update("parts", parts)
    }

    fun getPlannedList(onResult: (List<Maintenance>) -> Unit) {
        db.collection("plannedMaintenances")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Maintenance::class.java)
                }
                println("Toplam planlÄ± bakÄ±m: ${list.size}")
                onResult(list)
            }
            .addOnFailureListener { e ->
                println("BakÄ±m listesi Ã§ekilirken hata oluÅŸtu: ${e.message}")
            }
    }


    fun fetchHolidays(year: Int = LocalDate.now().year) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getPublicHolidays(year)
                val dateSet = response.map { it.date.replace("-", ".") } // "2025.04.23"
                    .map {
                        val parts = it.split(".")
                        "${parts[2]}.${parts[1]}.${parts[0]}" // â†’ "dd.MM.yyyy"
                    }.toSet()
                holidayDates.value = dateSet
            } catch (e: Exception) {
                Log.e("HolidayFetch", "Tatiller alÄ±namadÄ±: ${e.message}")
            }
        }
    }


    fun decreaseStock(code: String, quantity: Int) {
        db.collection("materials")
            .document(code)
            .get()
            .addOnSuccessListener { snapshot ->
                // 1. Firestoreâ€™dan Long olarak oku
                val currentStock: Long = snapshot.getLong("stock") ?: 0L
                // 2. Intâ€™i Longâ€™a Ã§evirip Ã§Ä±kar
                val difference: Long = currentStock - quantity.toLong()
                // 3. CoerceAtLeast ile en az 0L olsun
                val newStock: Long = difference.coerceAtLeast(0L)
                // 4. GÃ¼ncelle
                db.collection("materials")
                    .document(code)
                    .update("stock", newStock)
            }
    }

    fun getMaintenanceStatus(plannedDate: String): String {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val date = LocalDate.parse(plannedDate, formatter)
            val today = LocalDate.now()

            when {
                date.isBefore(today) -> "geÃ§miÅŸ"
                date.isEqual(today) -> "bugÃ¼n"
                else -> "gelecek"
            }
        } catch (e: Exception) {
            "bilinmiyor"
        }
    }
    fun getPlannedOrPreparedMaintenancesByDate(
        date: LocalDate,
        callback: (List<Maintenance>) -> Unit
    ) {
        db.collection("plannedMaintenances")
            .whereEqualTo("plannedDate", date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
            .whereIn("status", listOf("planlandÄ±", "hazÄ±rlandÄ±"))
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { it.toObject(Maintenance::class.java) }
                callback(list)
            }
    }
    // Mevcut class MaintenanceViewModel iÃ§inde, en alta ekleyin:
    // MaintenanceViewModel iÃ§inde:
    fun getUserProcessCount(
        user: User,
        start: LocalDate,
        end: LocalDate,
        onResult: (count: Int) -> Unit
    ) {
        val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        db.collection("plannedMaintenances")
            .whereGreaterThanOrEqualTo("plannedDate", start.format(fmt))
            .whereLessThanOrEqualTo("plannedDate", end.format(fmt))
            .get()
            .addOnSuccessListener { snap ->
                val count = snap.documents.count { doc ->
                    // responsibles listesinde artÄ±k user.uid ile eÅŸleÅŸelim
                    val responsibles = doc.get("responsibles") as? List<String> ?: emptyList()
                    responsibles.any { it == user.uid }
                }
                onResult(count)
            }
            .addOnFailureListener {
                onResult(0)
            }
    }




}
