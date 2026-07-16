package com.cmc.customer.ui.dialogs

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.cmc.customer.model.Material

@Composable
fun ConfirmDeleteDialog(
    material: Material,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Silme OnayÄ±") },
        text = { Text("â€œ${material.code}â€ kodlu malzemeyi silmek istediÄŸinize emin misiniz?") },
        confirmButton = {
            TextButton(onClick = {
                onConfirm() // ğŸ”´ Silme burada tetiklenecek
            }) {
                Text("Sil")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ä°ptal")
            }
        }
    )
}
