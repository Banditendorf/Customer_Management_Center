package com.cmc.customer.screen.preparation

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmc.customer.model.*
import com.cmc.customer.ui.theme.*
import com.cmc.customer.ui.ui.RedTopBar
import com.cmc.customer.viewmodel.MaintenanceViewModel
import com.cmc.customer.viewmodel.MaterialViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreparationDetailScreen(
    maintenance: Maintenance,
    onBack: () -> Unit,
    currentUser: User,
    maintenanceViewModel: MaintenanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("prepared_prefs", Context.MODE_PRIVATE)

    val materialViewModel: MaterialViewModel = viewModel()

    var materialMap by remember { mutableStateOf<Map<String, Material>>(emptyMap()) }
    var machine by remember { mutableStateOf<Machine?>(null) }
    var company by remember { mutableStateOf<Company?>(null) }
    val allSpareParts = remember { mutableStateListOf<SparePart>() }
    val preparedMap = remember { mutableStateMapOf<String, Boolean>() }
    val allPrepared by derivedStateOf { preparedMap.values.all { it } }
    var isOilPrepared by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val isAlreadyPrepared = maintenance.status == "hazÄ±rlandÄ±"
    val hasAnyUnchecked by derivedStateOf { preparedMap.values.any { !it } || (maintenance.oilLiter > 0 && !isOilPrepared) }
    val allReady = allPrepared && (maintenance.oilLiter <= 0 || isOilPrepared)

    val buttonText = when {
        allReady && !isAlreadyPrepared -> "HazÄ±rlÄ±k TamamlandÄ±"
        allReady && isAlreadyPrepared -> "Liste HazÄ±r"
        hasAnyUnchecked -> "Listeyi GÃ¼ncelle"
        else -> "HazÄ±rlÄ±k Durumu"
    }

    LaunchedEffect(Unit) {
        val firestore = FirebaseFirestore.getInstance()

        val materialsSnap = firestore.collection("materials").get().await()
        val loaded: List<Material> = materialsSnap.documents.flatMap { doc ->
            val category = doc.id
            val content = doc.get("icerik") as? Map<String, Any> ?: emptyMap()

            content.mapNotNull { entry ->
                val data = entry.value as? Map<*, *> ?: return@mapNotNull null
                val code = data["code"] as? String ?: return@mapNotNull null

                Material(
                    code = code,
                    shelf = data["shelf"] as? String ?: "",
                    category = category,
                    stock = (data["stock"] as? Long)?.toInt() ?: 0,
                    kritikStok = (data["kritikStok"] as? Long)?.toInt() ?: 0,
                    description = data["description"] as? String ?: ""
                )
            }
        }

        materialMap = loaded.associateBy { it.code }

        machine = firestore.collection("machines").document(maintenance.machineId).get().await().toObject(Machine::class.java)
        company = machine?.companyId?.let {
            firestore.collection("companies").document(it).get().await().toObject(Company::class.java)
        }

        allSpareParts.clear()
        allSpareParts.addAll(maintenance.parts + maintenance.extraParts)

        allSpareParts.forEach { part ->
            val saved = prefs.getBoolean("${maintenance.id}_${part.code}", part.prepared ?: false)
            preparedMap[part.code] = saved
        }

        isOilPrepared = prefs.getBoolean("${maintenance.id}_oil", false)
    }

    Column(
        modifier = Modifier.fillMaxSize().background(BackgroundDark)
    ) {
        RedTopBar(
            title = "HazÄ±rlÄ±k DetayÄ±",
            showMenu = true,
            menuContent = {
                DropdownMenuItem(
                    text = { Text("Listeyi Sil") },
                    onClick = {
                        expanded = false
                        showDeleteConfirm = true
                    }
                )
            }
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Åirket: ${company?.name ?: maintenance.companyName}", color = White)
                Text("MCMCne: ${machine?.name ?: maintenance.machineName}", color = White)
                Text("Seri No: ${machine?.serialNumber ?: maintenance.serialNumber}", color = LightGray)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allSpareParts) { part ->
                val material = materialMap[part.code]
                val isPrepared = preparedMap[part.code] == true
                val cardColor = if (preparedMap[part.code] == true) Green.copy(alpha = 0.3f) else CardDark

                Card(
                    modifier = Modifier.fillMaxWidth().combinedClickable(
                        onClick = {},
                        onLongClick = {
                            preparedMap[part.code] = !isPrepared
                            prefs.edit().putBoolean("${maintenance.id}_${part.code}", !isPrepared).apply()
                        }
                    ),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Kategori: ${material?.category ?: part.category}", color = White)
                            Text("Kod: ${part.code}", color = White)
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                            Text("Raf: ${material?.shelf ?: part.shelf}", color = LightGray)
                            Text("Adet: ${part.quantity}", color = LightGray)
                        }
                        if (isPrepared) {
                            Spacer(Modifier.height(4.dp))
                            Text("HazÄ±r", color = White, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (maintenance.oilLiter > 0) {
                val total = maintenance.oilLiter.toInt()
                val full20 = total / 20
                val remaining = total % 20

                val bottle20: Int
                val bottle5: Int

                if (remaining >= 16) {
                    bottle20 = full20 + 1
                    bottle5 = 0
                } else {
                    bottle20 = full20
                    bottle5 = remaining / 5
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = {
                                        isOilPrepared = !isOilPrepared
                                        prefs.edit().putBoolean("${maintenance.id}_oil", isOilPrepared).apply()
                                    }
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isOilPrepared) Green.copy(alpha = 0.3f) else CardDark
                        )
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text("YaÄŸ", color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Kod: ${maintenance.oilCode}", color = White)
                            Text("Toplam: $total L", color = LightGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("20L ÅiÅŸe: $bottle20 adet", color = LightGray)
                            Text("5L ÅiÅŸe : $bottle5 adet", color = LightGray)
                            if (isOilPrepared) {
                                Spacer(Modifier.height(4.dp))
                                Text("HazÄ±r", color = Green, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                val updatedParts = allSpareParts.map {
                    it.copy(prepared = preparedMap[it.code])
                }

                val updatedStatus = when {
                    allReady -> "hazÄ±rlandÄ±"
                    isAlreadyPrepared && hasAnyUnchecked -> "planlandÄ±"
                    else -> maintenance.status
                }

                val updated = maintenance.copy(
                    parts = updatedParts.filter { it.code !in maintenance.extraParts.map { it.code } },
                    extraParts = updatedParts.filter { it.code in maintenance.extraParts.map { it.code } },
                    status = updatedStatus,
                    preparedBy = if (updatedStatus == "hazÄ±rlandÄ±") currentUser.fullName else maintenance.preparedBy
                )

                // *** Buradan Ã§Ä±karÄ±lÄ±yor! ***
//        updatedParts.forEach { part ->
//            if (part.prepared == true) {
//                materialViewModel.decreaseStock(part.code, part.quantity)
//            }
//        }

                maintenanceViewModel.updateMaintenance(updated) {
                    onBack()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = allReady || isAlreadyPrepared || (isAlreadyPrepared && hasAnyUnchecked),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (allReady || (isAlreadyPrepared && hasAnyUnchecked)) Green else Gray
            )
        ) {
            Text(buttonText, color = White)
        }


        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                confirmButton = {
                    TextButton(onClick = {
                        FirebaseFirestore.getInstance().collection("plannedMaintenances")
                            .document(maintenance.id)
                            .delete()
                        showDeleteConfirm = false
                        onBack()
                    }) {
                        Text("Sil")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Ä°ptal")
                    }
                },
                title = { Text("Listeyi Sil") },
                text = { Text("Bu bakÄ±m planÄ±nÄ± silmek istediÄŸinize emin misiniz?") }
            )
        }
    }
}
