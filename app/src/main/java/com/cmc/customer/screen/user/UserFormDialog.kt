package com.cmc.customer.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.cmc.customer.model.NotificationPreferences
import com.cmc.customer.model.User
import com.cmc.customer.model.UserPermissions
import com.cmc.customer.ui.theme.BorderGray
import com.cmc.customer.ui.theme.RedPrimary
import com.cmc.customer.ui.theme.SurfaceDark
import com.cmc.customer.ui.theme.White
import java.util.*

@Composable
fun UserFormDialog(
    user: User? = null,
    onConfirm: (User, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val isEdit = user != null
    var fullName by remember { mutableStateOf(user?.fullName.orEmpty()) }
    var email by remember { mutableStateOf(user?.email.orEmpty()) }
    var role by remember { mutableStateOf(user?.role.orEmpty()) }
    var workPhone by remember { mutableStateOf(user?.workPhone.orEmpty()) }
    var personalPhone by remember { mutableStateOf(user?.personalPhone.orEmpty()) }
    var isActive by remember { mutableStateOf(user?.isActive ?: true) }
    var permissions by remember { mutableStateOf(user?.permissions ?: UserPermissions()) }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnClickOutside = false,
            dismissOnBackPress = false
        ),
        containerColor = SurfaceDark,
        tonalElevation = 8.dp,
        title = {
            Text(
                text = if (isEdit) "KullanÄ±cÄ±yÄ± GÃ¼ncelle" else "Yeni KullanÄ±cÄ± Ekle",
                style = MaterialTheme.typography.headlineSmall,
                color = White
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                val fieldColors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RedPrimary,
                    unfocusedBorderColor = BorderGray,
                    cursorColor = RedPrimary,
                    focusedLabelColor = RedPrimary,
                    unfocusedLabelColor = White,
                    disabledLabelColor = White
                )

                // Temel kullanÄ±cÄ± alanlarÄ±
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Ad Soyad") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isEdit,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = fieldColors
                )
                Spacer(Modifier.height(8.dp))
                if (!isEdit) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Åifre") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(icon, contentDescription = null, tint = RedPrimary)
                            }
                        },
                        colors = fieldColors
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Rol") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = fieldColors
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = workPhone,
                    onValueChange = { workPhone = it },
                    label = { Text("Ä°ÅŸ Telefonu") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = fieldColors
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = personalPhone,
                    onValueChange = { personalPhone = it },
                    label = { Text("KiÅŸisel Telefon") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = fieldColors
                )
                Spacer(Modifier.height(8.dp))

                // Aktiflik durumu
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = RedPrimary,
                            uncheckedColor = White
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Aktif",
                        color = White,
                        modifier = Modifier.clickable { isActive = !isActive }
                    )
                }
                Spacer(Modifier.height(12.dp))

                // Yetkiler baÅŸlÄ±ÄŸÄ± ve TÃ¼m Yetkiler butonu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Yetkiler",
                        style = MaterialTheme.typography.titleMedium,
                        color = White
                    )
                    TextButton(onClick = {
                        permissions = UserPermissions(
                            manageUser = true,
                            manageMachine = true,
                            manageCompany = true,
                            manageMaintenance = true,
                            manageCategory = true,
                            manageMaterial = true,
                            callCustomer = true,
                            viewCompanies = true,
                            viewMaintenancePlans = true,
                            viewPreparationLists = true,
                            viewMaterialsList = true,
                            viewUsers = true,
                            viewNotifications = true
                        )
                    }) {
                        Text("TÃ¼m Yetkiler", color = RedPrimary)
                    }
                }
                Spacer(Modifier.height(4.dp))

                // Ä°zin checkboxlarÄ±
                PermissionCheckbox("KullanÄ±cÄ± YÃ¶net", permissions.manageUser) { permissions = permissions.copy(manageUser = it) }
                PermissionCheckbox("MCMCne YÃ¶net", permissions.manageMachine) { permissions = permissions.copy(manageMachine = it) }
                PermissionCheckbox("Åirket YÃ¶net", permissions.manageCompany) { permissions = permissions.copy(manageCompany = it) }
                PermissionCheckbox("BakÄ±m YÃ¶net", permissions.manageMaintenance) { permissions = permissions.copy(manageMaintenance = it) }
                PermissionCheckbox("Kategori YÃ¶net", permissions.manageCategory) { permissions = permissions.copy(manageCategory = it) }
                PermissionCheckbox("Malzeme YÃ¶net", permissions.manageMaterial) { permissions = permissions.copy(manageMaterial = it) }
                PermissionCheckbox("MÃ¼ÅŸteri Arama", permissions.callCustomer) { permissions = permissions.copy(callCustomer = it) }
                PermissionCheckbox("Åirketleri GÃ¶rÃ¼ntÃ¼le", permissions.viewCompanies) { permissions = permissions.copy(viewCompanies = it) }
                PermissionCheckbox("Planlanan BakÄ±mlarÄ± GÃ¶rÃ¼ntÃ¼le", permissions.viewMaintenancePlans) { permissions = permissions.copy(viewMaintenancePlans = it) }
                PermissionCheckbox("HazÄ±rlanacak Listeleri GÃ¶rÃ¼ntÃ¼le", permissions.viewPreparationLists) { permissions = permissions.copy(viewPreparationLists = it) }
                PermissionCheckbox("Malzemeleri GÃ¶rÃ¼ntÃ¼le", permissions.viewMaterialsList) { permissions = permissions.copy(viewMaterialsList = it) }
                PermissionCheckbox("KullanÄ±cÄ±larÄ± GÃ¶rÃ¼ntÃ¼le", permissions.viewUsers) { permissions = permissions.copy(viewUsers = it) }
                PermissionCheckbox("Bildirimleri GÃ¶rÃ¼ntÃ¼le", permissions.viewNotifications) { permissions = permissions.copy(viewNotifications = it) }


                Spacer(Modifier.height(16.dp))

                Divider(color = BorderGray, thickness = 1.dp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Bildirim Tercihleri",
                    style = MaterialTheme.typography.titleMedium,
                    color = White
                )
                Spacer(Modifier.height(4.dp))

                // Bildirim tercihlerini dÃ¼zenle
                var prefs by remember { mutableStateOf(user?.notificationPreferences ?: NotificationPreferences()) }

                PermissionCheckbox("Kritik Stok UyarÄ±larÄ±", prefs.stockCritical) {
                    prefs = prefs.copy(stockCritical = it)
                }
                PermissionCheckbox("BakÄ±m YaklaÅŸma UyarÄ±larÄ±", prefs.maintenanceUpcoming) {
                    prefs = prefs.copy(maintenanceUpcoming = it)
                }
                PermissionCheckbox("BakÄ±m Gecikme UyarÄ±larÄ±", prefs.maintenanceOverdue) {
                    prefs = prefs.copy(maintenanceOverdue = it)
                }
                PermissionCheckbox("BakÄ±m TamamlandÄ± UyarÄ±larÄ±", prefs.maintenanceDone) {
                    prefs = prefs.copy(maintenanceDone = it)
                }
                PermissionCheckbox("GÃ¶rev AtandÄ±ÄŸÄ±nda UyarÄ±", prefs.taskAssigned) {
                    prefs = prefs.copy(taskAssigned = it)
                }
                PermissionCheckbox("Ã‡ay / Yemek MolasÄ± UyarÄ±larÄ±", prefs.breakAlerts) {
                    prefs = prefs.copy(breakAlerts = it)
                }

            }
        },
        confirmButton = {
            TextButton(onClick = {
                val resultUser = user?.copy(
                    fullName = fullName,
                    role = role,
                    workPhone = workPhone,
                    personalPhone = personalPhone,
                    isActive = isActive,
                    permissions = permissions
                ) ?: User(
                    uid = UUID.randomUUID().toString(),
                    email = email,
                    fullName = fullName,
                    role = role,
                    workPhone = workPhone,
                    personalPhone = personalPhone,
                    isActive = isActive,
                    permissions = permissions
                )
                onConfirm(resultUser, if (isEdit) null else password)
            }) {
                Text(
                    text = if (isEdit) "GÃ¼ncelle" else "Ekle",
                    color = RedPrimary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ä°ptal", color = White)
            }
        }
    )
}

@Composable
private fun PermissionCheckbox(
    label: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onChange,
            colors = CheckboxDefaults.colors(
                checkedColor = RedPrimary,
                uncheckedColor = White
            )
        )
        Spacer(Modifier.width(8.dp))
        Text(label, color = White)
    }
}
