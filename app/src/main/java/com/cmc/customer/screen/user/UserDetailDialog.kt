package com.cmc.customer.screen.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.cmc.customer.model.User
import com.cmc.customer.ui.components.UserFormDialog
import com.cmc.customer.ui.theme.RedPrimary

@Composable
fun UserDetailDialog(
    user: User,
    onDismiss: () -> Unit,
    onDelete: (String) -> Unit,
    onToggleActive: (String, Boolean) -> Unit,
    onSave: (User) -> Unit
) {
    var showEditForm by remember { mutableStateOf(false) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    // Detay dialogu
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("KullanÄ±cÄ± DetaylarÄ±", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Ad Soyad: ${user.fullName}")
                Text("E-posta: ${user.email}")
                Text("GÃ¶revi: ${user.role}")
                Text("Ä°ÅŸ Telefonu: ${user.workPhone}")
                Text("KiÅŸisel Telefon: ${user.personalPhone}")

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Aktif")
                    Switch(
                        checked = user.isActive,
                        onCheckedChange = { onToggleActive(user.uid, it) }
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // DÃ¼zenle butonu
                TextButton(onClick = { showEditForm = true }) {
                    Icon(Icons.Default.Edit, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("DÃ¼zenle")
                }
                // Silme butonu
                TextButton(
                    onClick = { showConfirmDelete = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = RedPrimary
                    )
                ) {
                    Text("Sil")
                }
                // Kapat butonu
                TextButton(onClick = onDismiss) {
                    Text("Kapat")
                }
            }
        }
    )

    // Silme onayÄ±
    if (showConfirmDelete) {
        AlertDialog(
            onDismissRequest = { showConfirmDelete = false },
            title = { Text("Silme OnayÄ±") },
            text = { Text("Bu kullanÄ±cÄ±yÄ± silmek istediÄŸinizden emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(user.uid)
                    showConfirmDelete = false
                    onDismiss()
                }) {
                    Text("Evet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDelete = false }) {
                    Text("Ä°ptal")
                }
            }
        )
    }

    // Tek form dialog: hem alanlar hem permissions
    if (showEditForm) {
        UserFormDialog(
            user = user,
            onConfirm = { updatedUser, _ ->
                onSave(updatedUser)
                showEditForm = false
                onDismiss()
            },
            onDismiss = { showEditForm = false }
        )
    }
}
