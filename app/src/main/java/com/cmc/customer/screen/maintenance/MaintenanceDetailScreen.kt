package com.cmc.customer.screen.maintenance

import android.content.Context
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cmc.customer.model.Maintenance
import com.cmc.customer.model.Machine
import com.cmc.customer.screen.machine.formatHoursToTime
import com.cmc.customer.ui.theme.*
import com.cmc.customer.ui.ui.RedTopBar
import com.cmc.customer.util.PhotoStorageHelper
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File

@Composable
fun MaintenanceDetailScreen(
    maintenance: Maintenance
) {
    val context = LocalContext.current
    val db = Firebase.firestore

    var machine by remember { mutableStateOf<Machine?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var photoFiles by remember { mutableStateOf<List<File>>(emptyList()) }

    // 1. MCMCneyi ve fotoÄŸraflarÄ± Ã§ek
    LaunchedEffect(maintenance.machineId) {
        db.collection("machines").document(maintenance.machineId).get()
            .addOnSuccessListener {
                machine = it.toObject(Machine::class.java)
                isLoading = false
            }
            .addOnFailureListener {
                error = "MCMCne bilgisi yÃ¼klenemedi"
                isLoading = false
            }
        // FOTOÄRAFLARI ARTIK DOÄRU KLASÃ–R YOLUNDAN OKUYORUZ
        photoFiles = PhotoStorageHelper.listPhotosByFolderName(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.getExternalFilesDir(null)?.absolutePath + "/" + maintenance.photoFolderName
            } else {
                Environment.getExternalStorageDirectory().absolutePath + "/" + maintenance.photoFolderName
            }
        )
    }


    Column(Modifier.fillMaxSize()) {
        RedTopBar(title = "BakÄ±m DetayÄ±", showBackButton = false)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .padding(16.dp)
        ) {
            when {
                isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error != null -> Text("Hata: $error", color = MaterialTheme.colorScheme.error)
                else -> DetailContent(maintenance, machine, photoFiles)
            }
        }
    }
}

@Composable
fun DetailContent(
    maintenance: Maintenance,
    machine: Machine?,
    photoFiles: List<File>
) {
    var selectedFile by remember { mutableStateOf<File?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.horizontalGradient(listOf(Color.Black, Color.Red)))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "Genel Bilgiler",
                    color = White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Åirket", maintenance.companyName)
                InfoRow("MCMCne", maintenance.machineName)
                InfoRow("Seri No", maintenance.serialNumber)
                InfoRow("BakÄ±m Tarihi", "${maintenance.plannedDate} ${maintenance.plannedTime}")
                InfoRow("Ä°ÅŸ Emri No", maintenance.workOrderNumber)
                machine?.estimatedHours?.let {
                    InfoRow("Tahmini MCMCna Saati", formatHoursToTime(it))
                }
            }
        }

        InfoCard("AÃ§Ä±klama") {
            Text(maintenance.description, color = LightGray)
        }

        InfoCard("BakÄ±m NotlarÄ±") {
            Text("BakÄ±m Ã–nÃ¼: ${maintenance.preMaintenanceNote}", color = LightGray)
            Spacer(modifier = Modifier.height(4.dp))
            Text("BakÄ±m Sonu: ${maintenance.postMaintenanceNote}", color = LightGray)
        }

        InfoCard("Zaman Bilgisi") {
            InfoRow("BaÅŸlangÄ±Ã§", maintenance.startTime)
            InfoRow("BitiÅŸ", maintenance.endTime)
        }

        InfoCard("Sorumlular") {
            InfoRow("Ä°ÅŸlem SorumlularÄ±", maintenance.responsibles.joinToString())
            InfoRow("HazÄ±rlayan", maintenance.preparedBy)
        }

        if (maintenance.oilChanged) {
            InfoCard("YaÄŸ Bilgisi") {
                InfoRow("YaÄŸ Kodu", maintenance.oilCode)
                InfoRow("Miktar", "${maintenance.oilLiter} L")
            }
        }

        if (maintenance.voltageL1 != null || maintenance.currentL1 != null || maintenance.pressure != null) {
            InfoCard("Ã–lÃ§Ã¼mler") {
                maintenance.voltageL1?.let { InfoRow("Voltaj L1", "$it V") }
                maintenance.currentL1?.let { InfoRow("AkÄ±m L1", "$it A") }
                maintenance.pressure?.let { InfoRow("BasÄ±nÃ§", "$it bar") }
            }
        }

        if (maintenance.changedParts.isNotEmpty()) {
            InfoCard("DeÄŸiÅŸtirilen ParÃ§alar") {
                val changedCodes = maintenance.changedParts.toSet()
                val allParts = maintenance.parts + maintenance.extraParts
                val changedParts = allParts.filter { it.code in changedCodes }

                if (changedParts.isEmpty()) {
                    Text("ParÃ§a bilgisi mevcut deÄŸil.", color = LightGray)
                } else {
                    changedParts.forEach {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text("â€¢ ${it.name}", color = White, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Kod: ${it.code} | Adet: ${it.quantity}",
                                color = SoftBlue,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        }
                    }
                }
            }
        }

        // FotoÄŸraf klasÃ¶rÃ¼ adÄ± gÃ¶sterimi
        if (maintenance.photoFolderName.isNotBlank()) {
            InfoCard("FotoÄŸraf KlasÃ¶rÃ¼") {
                Text(maintenance.photoFolderName, color = SoftBlue)
            }
        }

        // FOTOÄRAFLAR KARTI
        if (photoFiles.isNotEmpty()) {
            InfoCard("BakÄ±m FotoÄŸraflarÄ±") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(photoFiles) { file ->
                        Card(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedFile = file },
                            colors = CardDefaults.cardColors(containerColor = CardDark)
                        ) {
                            AsyncImage(
                                model = file,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // BÃœYÃœK FOTO DÄ°ALOG
        if (selectedFile != null) {
            AlertDialog(
                onDismissRequest = { selectedFile = null },
                confirmButton = {
                    TextButton(onClick = { selectedFile = null }) {
                        Text("Kapat", color = RedPrimary)
                    }
                },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AsyncImage(
                            model = selectedFile,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = selectedFile?.name ?: "",
                            color = White,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                containerColor = CardDark
            )
        }
    }
}

@Composable
fun InfoCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                color = White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = LightGray)
        Text(value, color = White, fontWeight = FontWeight.SemiBold)
    }
}
