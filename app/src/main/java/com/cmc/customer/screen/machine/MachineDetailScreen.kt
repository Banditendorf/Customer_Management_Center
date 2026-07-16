package com.cmc.customer.screen.machine

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cmc.customer.model.*
import com.cmc.customer.ui.theme.*
import com.cmc.customer.ui.ui.RedTopBar
import androidx.compose.ui.platform.LocalContext
import com.cmc.customer.permission.PermissionManager
import com.cmc.customer.viewmodel.MaintenanceViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import com.cmc.customer.screen.machine.CompressorDialog
import com.cmc.customer.screen.machine.DryerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import com.google.gson.Gson
import com.cmc.customer.viewmodel.UserViewModel
@Composable
fun MachineDetailScreen(
    machine: Machine,
    navController: NavController,
    maintenanceViewModel: MaintenanceViewModel = viewModel(),
    userViewModel: UserViewModel = viewModel()
) {
    val isDryer = machine.type == "kurutucu"
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pm = PermissionManager.getInstance(context)
    val snackbarHostState = remember { SnackbarHostState() }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    var maintenanceList by remember { mutableStateOf(listOf<Maintenance>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditMachineDialog by remember { mutableStateOf(false) }
    var receiveNotifications by remember { mutableStateOf(true) }

    // Bildirim ayarÄ± Firestore'dan Ã§ekilir
    LaunchedEffect(machine.id) {
        userViewModel.getMachineNotificationPreference(machine.id) {
            receiveNotifications = it
        }
    }

    // PlanlanmÄ±ÅŸ bakÄ±mlar Firestore'dan Ã§ekilir
    LaunchedEffect(machine.id) {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
        FirebaseFirestore.getInstance()
            .collection("plannedMaintenances")
            .whereEqualTo("machineId", machine.id)
            .get()
            .addOnSuccessListener { result ->
                maintenanceList = result.documents
                    .mapNotNull { it.toObject(Maintenance::class.java) }
                    .sortedByDescending {
                        try {
                            LocalDate.parse(it.plannedDate, formatter)
                        } catch (e: Exception) {
                            LocalDate.MIN
                        }
                    }
            }
    }

    // Snackbar gÃ¶stermek istersen:
    snackbarMessage?.let { message ->
        LaunchedEffect(message) {
            snackbarHostState.showSnackbar(message)
            snackbarMessage = null
        }
    }


    Column(Modifier.fillMaxSize().background(BackgroundDark)) {
        RedTopBar(title = machine.name, showMenu = true) {
            DropdownMenuItem(
                text = { Text("MCMCneyi GÃ¼ncelle") },
                onClick = {
                    if (pm.canManageMachines()) {
                        showEditMachineDialog = true
                    } else {
                        scope.launch { snackbarMessage = "MCMCne gÃ¼ncelleme yetkiniz yok." }
                    }
                }
            )

            DropdownMenuItem(
                text = { Text("MCMCneyi Sil") },
                onClick = {
                    if (pm.canManageMachines()) {
                        showDeleteConfirm = true
                    } else {
                        scope.launch { snackbarMessage = "MCMCne silme yetkiniz yok." }
                    }
                }
            )

            Divider()

            DropdownMenuItem(
                text = {
                    Text(if (receiveNotifications) "ğŸ”• Bu mCMCna iÃ§in bildirimleri kapat" else "ğŸ”” Bu mCMCna iÃ§in bildirimleri aÃ§")
                },
                onClick = {
                    receiveNotifications = !receiveNotifications
                    userViewModel.updateMachineNotificationPreference(machine.id, receiveNotifications)
                    scope.launch {
                        snackbarMessage = if (receiveNotifications)
                            "Bildirimler aÃ§Ä±ldÄ±."
                        else
                            "Bildirimler kapatÄ±ldÄ±."
                    }
                }
            )
        }


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Seri No: ${machine.serialNumber}", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                if (isDryer) {
                    if (machine.dryerFilterCount > 0) {
                        InfoRow("Kurutucu Filtresi:", machine.dryerFilterCode, "${machine.dryerFilterCount} adet")
                    }
                    if (machine.oilCode == "Aktif AlÃ¼mina") {
                        InfoRow("Aktif AlÃ¼mina:", machine.oilCode, "${machine.oilLiter} kg")
                    }
                } else {
                    InfoRow("YaÄŸ Filtresi:", machine.oilFilterCode, machine.oilFilterCount.toString())
                    InfoRow("SeparatÃ¶r:", machine.separatorCode, machine.separatorCount.toString())
                    InfoRow("Hava Filtresi:", machine.airFilterCode, machine.airFilterCount.toString())
                    InfoRow("YaÄŸ:", machine.oilCode, "${machine.oilLiter} L")
                    InfoRow("Panel Filtresi:", machine.panelFilterSize, "")
                }

                InfoRow("Tahmini Ã‡alÄ±ÅŸma Saati:", formatHoursToTime(machine.estimatedHours), "")
                InfoRow("SÄ±radCMC BakÄ±m Saati:", "${machine.nextMaintenanceHour} saat", "")

                machine.note.takeIf { it.isNotBlank() }?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Not: $it", color = Color.Black)
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // GEÃ‡MÄ°Å BAÅLIÄI DAHÄ°L BAÅLANGIÃ‡
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "GeÃ§miÅŸ",
                    color = White,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 0.dp) // padding(0) ile baÅŸlasÄ±n, istersen ayarla
                )
            }

            // BakÄ±m KartlarÄ±
            items(maintenanceList) { maintenance ->
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
                val today = LocalDate.now()
                val maintenanceDate = try {
                    LocalDate.parse(maintenance.plannedDate, formatter)
                } catch (e: Exception) {
                    null
                }

                val responsibles = maintenance.responsibles.joinToString(", ").ifBlank { "-" }

                var menuExpanded by remember { mutableStateOf(false) }
                var showDeleteDialog by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                when (maintenance.status.trim().lowercase()) {
                                    "planlandÄ±" -> {
                                        val json = Uri.encode(Gson().toJson(maintenance))
                                        navController.navigate("preparationDetail/$json")
                                    }
                                    "hazÄ±rlandÄ±" -> {
                                        val json = Uri.encode(Gson().toJson(maintenance))
                                        navController.navigate("completionDetail/$json")
                                    }
                                    "tamamlandÄ±" -> {
                                        val json = Uri.encode(Gson().toJson(maintenance))
                                        navController.navigate("maintenanceDetail/$json")
                                    }
                                }
                            },
                            onLongClick = { menuExpanded = true }
                        ),
                    colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = maintenance.description.ifBlank { "AÃ§Ä±klama yok" },
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tarih: ${maintenance.plannedDate}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sorumlular: $responsibles",
                            color = LightGray,
                            style = MaterialTheme.typography.titleMedium
                        )

                        // MenÃ¼
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("BakÄ±mÄ± Sil") },
                                onClick = {
                                    menuExpanded = false
                                    if (pm.canManageMaintenance()) {
                                        showDeleteDialog = true
                                    } else {
                                        scope.launch { snackbarMessage = "BakÄ±m silme yetkiniz yok." }
                                    }
                                }
                            )
                            if (maintenance.status.trim().lowercase() == "tamamlandÄ±") {
                                DropdownMenuItem(
                                    text = { Text("BakÄ±mÄ± DÃ¼zenle") },
                                    onClick = {
                                        menuExpanded = false
                                        if (pm.canManageMaintenance()) {
                                            val json = Uri.encode(Gson().toJson(maintenance))
                                            navController.navigate("completionDetail/$json")
                                        } else {
                                            scope.launch { snackbarMessage = "BakÄ±m dÃ¼zenleme yetkiniz yok." }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Silme Onay Dialogu
                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("BakÄ±m Silinsin mi?") },
                        text = { Text("Bu bakÄ±mÄ± silmek istediÄŸinizden emin misiniz?") },
                        confirmButton = {
                            TextButton(onClick = {
                                showDeleteDialog = false
                                if (pm.canManageMaintenance()) {
                                    FirebaseFirestore.getInstance()
                                        .collection("plannedMaintenances")
                                        .document(maintenance.id)
                                        .delete()
                                        .addOnSuccessListener {
                                            // State listesinden sil ve kullanÄ±cÄ±yÄ± bilgilendir
                                            maintenanceList = maintenanceList.filter { it.id != maintenance.id }
                                            snackbarMessage = "BakÄ±m kaydÄ± silindi."
                                        }
                                        .addOnFailureListener {
                                            snackbarMessage = "Silme iÅŸlemi baÅŸarÄ±sÄ±z."
                                        }
                                } else {
                                    snackbarMessage = "BakÄ±m silme yetkiniz yok."
                                }
                            }) {
                                Text("Evet")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Ä°ptal")
                            }
                        }
                    )
                }
            }
        }
    }

    // 1) â€œMCMCneyi Silâ€ Onay DiyaloÄŸu bloÄŸunu izin kontrolÃ¼ ile gÃ¼ncelledik
    if (showDeleteConfirm) {
        ConfirmDeleteDialog(
            text = "Bu mCMCneyi silmek istediÄŸinize emin misiniz?",
            onConfirm = {
                showDeleteConfirm = false
                if (pm.canManageMachines()) {
                    FirebaseFirestore.getInstance()
                        .collection("machines")
                        .document(machine.id)
                        .delete()
                        .addOnSuccessListener {
                            snackbarMessage = "MCMCne baÅŸarÄ±yla silindi!"
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            snackbarMessage = "MCMCne silinirken hata oluÅŸtu."
                        }
                } else {
                    snackbarMessage = "MCMCne silme yetkiniz yok."
                }
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }

// 2) â€œMCMCneyi GÃ¼ncelleâ€ DiyaloÄŸu bloÄŸunu izin kontrolÃ¼ ile gÃ¼ncelledik
    if (showEditMachineDialog) {
        if (pm.canManageMachines()) {
            when (machine.type) {
                "kompresÃ¶r" -> CompressorDialog(
                    companyId   = machine.companyId,
                    companyName = machine.companyName,
                    machine     = machine,
                    onDismiss   = { showEditMachineDialog = false },
                    onCompleted = {
                        snackbarMessage      = "MCMCne baÅŸarÄ±yla gÃ¼ncellendi!"
                        showEditMachineDialog = false
                    }
                )
                "kurutucu"   -> DryerDialog(
                    companyId   = machine.companyId,
                    companyName = machine.companyName,
                    machine     = machine,
                    onDismiss   = { showEditMachineDialog = false },
                    onCompleted = {
                        snackbarMessage      = "MCMCne baÅŸarÄ±yla gÃ¼ncellendi!"
                        showEditMachineDialog = false
                    }
                )
                else -> showEditMachineDialog = false
            }
        } else {
            snackbarMessage      = "MCMCne gÃ¼ncelleme yetkiniz yok."
            showEditMachineDialog = false
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            snackbarMessage = null
        }
    }
}

@Composable
fun InfoRow(label: String, center: String?, right: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = Color.Black, modifier = Modifier.weight(1f))
        Text(center ?: "-", color = Color.DarkGray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        Text(right ?: "", color = Color.DarkGray, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
}

@Composable
fun ConfirmDeleteDialog(
    title: String = "Silme OnayÄ±",
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Evet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ä°ptal")
            }
        },
        title = { Text(title) },
        text = { Text(text) }
    )
}

fun formatHoursToTime(hours: Int): String {
    val totalSeconds = hours * 3600
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return String.format("%02d:%02d:%02d", h, m, s)
}
