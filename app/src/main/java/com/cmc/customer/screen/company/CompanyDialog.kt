package com.cmc.customer.screen.company

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.cmc.customer.model.Company
import com.cmc.customer.ui.theme.RedPrimary
import com.cmc.customer.ui.theme.White
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyDialog(
    navController: NavHostController, // Åimdilik kullanÄ±lmÄ±yor; gerekirse kaldÄ±r
    company: Company?,
    onDismiss: () -> Unit,
    onSave: (Company) -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf(company?.name ?: "") }
    var contactPerson by remember { mutableStateOf(company?.contactPerson ?: "") }
    var contactNumber by remember { mutableStateOf(company?.contactNumber ?: "") }
    var role by remember { mutableStateOf(company?.role ?: "") }
    var note by remember { mutableStateOf(company?.note ?: "") }
    var location by remember { mutableStateOf(company?.location ?: "") }
    var latitude by remember { mutableStateOf(company?.latitude) }
    var longitude by remember { mutableStateOf(company?.longitude) }
    var openLocationPicker by remember { mutableStateOf(false) }

    Box {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isBlank() || contactPerson.isBlank() || contactNumber.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("LÃ¼tfen tÃ¼m zorunlu alanlarÄ± doldurun.")
                            }
                        } else {
                            val id = company?.id ?: FirebaseFirestore.getInstance().collection("companies").document().id
                            val newCompany = Company(
                                id = id,
                                name = name,
                                contactPerson = contactPerson,
                                contactNumber = contactNumber,
                                role = role,
                                location = location,
                                note = note,
                                latitude = latitude,
                                longitude = longitude
                            )
                            onSave(newCompany)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Text(text = if (company == null) "Åirketi Ekle" else "Bilgileri GÃ¼ncelle", color = White)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("Ä°ptal") }
            },
            title = { Text(if (company == null) "Yeni Åirket Ekle" else "Åirket Bilgilerini DÃ¼zenle") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Åirket AdÄ± *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactPerson,
                        onValueChange = { contactPerson = it },
                        label = { Text("Yetkili KiÅŸi *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = contactNumber,
                        onValueChange = { contactNumber = it },
                        label = { Text("Telefon *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("GÃ¶revi") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(RedPrimary, shape = MaterialTheme.shapes.medium)
                            .clickable { openLocationPicker = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Konum SeÃ§", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (location.isNotBlank()) location else "Konum SeÃ§",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Notlar") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        )

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    if (openLocationPicker) {
        AlertDialog(
            onDismissRequest = { openLocationPicker = false },
            confirmButton = {},
            text = {
                LocationPickerContent { selectedAddress: String, selectedLatLng: LatLng ->
                    location = selectedAddress
                    latitude = selectedLatLng.latitude
                    longitude = selectedLatLng.longitude
                    openLocationPicker = false
                }
            }
        )
    }
}

/**
 * Tek bir projede *bir kez* tanÄ±mlÄ± olmalÄ±. BaÅŸka dosyalardCMC kopyalarÄ± silin.
 */
@Composable
fun LocationPickerContent(onLocationSelected: (String, LatLng) -> Unit) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(37.80978910918121, 29.062830256289654),
            16f
        )
    }

    var selectedPosition by remember { mutableStateOf<LatLng?>(null) }
    var selectedAddress by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val markerState = rememberMarkerState()

    // Adres Ã§Ã¶zÃ¼mleme
    LaunchedEffect(selectedPosition) {
        selectedPosition?.let { latLng ->
            isLoading = true
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = runCatching { geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) }.getOrNull()
            selectedAddress = addresses?.firstOrNull()?.getAddressLine(0) ?: "Adres bulunamadÄ±"
            isLoading = false
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            cameraPositionState = cameraPositionState,
            onMapLongClick = { latLng ->
                selectedPosition = latLng
                markerState.position = latLng
            },
            properties = MapProperties(
                isMyLocationEnabled = false,
                mapType = MapType.HYBRID
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                mapToolbarEnabled = false,
                compassEnabled = true
            )
        ) {
            selectedPosition?.let {
                Marker(
                    state = markerState,
                    title = "SeÃ§ilen Konum",
                    snippet = selectedAddress
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                selectedPosition?.let { pos ->
                    onLocationSelected(selectedAddress, pos)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
            enabled = selectedPosition != null && !isLoading
        ) {
            Text(text = "Konumu SeÃ§", color = White)
        }
    }
}