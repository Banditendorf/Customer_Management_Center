package com.cmc.customer.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import com.cmc.customer.model.Machine
import com.cmc.customer.util.NotificationHelper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MachineViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _machines = MutableStateFlow<List<Machine>>(emptyList())
    val machines: StateFlow<List<Machine>> = _machines

    // Åirkete ait mCMCneleri yÃ¼kle
    fun loadMachines(companyId: String) {
        db.collection("machines")
            .whereEqualTo("companyId", companyId)
            .get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    doc.toObject(Machine::class.java)?.copy(id = doc.id)
                }
                _machines.value = list
            }
            .addOnFailureListener {
                _machines.value = emptyList()
            }
    }

    // Yeni mCMCne ekle
    fun addMachine(machine: Machine, onComplete: () -> Unit) {
        db.collection("machines")
            .add(machine)
            .addOnCompleteListener { onComplete() }
    }

    // Mevcut mCMCneyi gÃ¼ncelle
    fun updateMachine(machine: Machine, onComplete: () -> Unit) {
        if (machine.id.isBlank()) return
        db.collection("machines").document(machine.id)
            .set(machine)
            .addOnCompleteListener { onComplete() }
    }

    // MCMCne sil
    fun deleteMachine(machineId: String, onComplete: () -> Unit) {
        db.collection("machines").document(machineId)
            .delete()
            .addOnCompleteListener { onComplete() }
    }

    // BakÄ±m zamanÄ± yaklaÅŸan mCMCneleri getir (varsayÄ±lan: 48 saat iÃ§inde)
    fun checkUpcomingMaintenances(threshold: Int = 48, onResult: (List<Machine>) -> Unit) {
        db.collection("machines").get()
            .addOnSuccessListener { result ->
                val list = result.documents.mapNotNull { doc ->
                    val machine = doc.toObject(Machine::class.java)
                    if (machine != null && (machine.nextMaintenanceHour - machine.estimatedHours) <= threshold) {
                        machine.copy(id = doc.id)
                    } else null
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
    fun checkAndNotifyUpcomingMaintenances(
        threshold: Int = 48,
        notify: (Machine) -> Unit
    ) {
        db.collection("machines").get()
            .addOnSuccessListener { result ->
                result.documents.forEach { doc ->
                    val machine = doc.toObject(Machine::class.java)
                    if (machine != null && machine.nextMaintenanceHour - machine.estimatedHours <= threshold) {
                        notify(machine.copy(id = doc.id))
                    }
                }
            }
    }

}
