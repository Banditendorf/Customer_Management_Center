package com.cmc.customer.screen.user

import android.content.Context
import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmc.customer.model.User
import com.cmc.customer.permission.PermissionManager
import com.cmc.customer.ui.components.UserFormDialog
import com.cmc.customer.ui.theme.RedPrimary
import com.cmc.customer.ui.theme.White
import com.cmc.customer.ui.ui.RedTopBar
import com.cmc.customer.util.UserStats
import com.cmc.customer.util.exportUserStatsToPDF
import com.cmc.customer.viewmodel.UserViewModel
import com.cmc.customer.viewmodel.MaintenanceViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun UsersScreen(
    userViewModel: UserViewModel = viewModel(),
    maintenanceViewModel: MaintenanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val pm = PermissionManager.getInstance(context)

    var editingUser by remember { mutableStateOf<User?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val allUsers by userViewModel.allUsers.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = { RedTopBar(title = "KullanÄ±cÄ±lar") },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF212121))
        ) {
            // Ãœst butonlar
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { editingUser = null; showFormDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = White)
                    Spacer(Modifier.width(8.dp))
                    Text("Yeni", color = White)
                }
                Button(
                    onClick = { showReportDialog = true },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, tint = White)
                    Spacer(Modifier.width(8.dp))
                    Text("Rapor OluÅŸtur", color = White)
                }
            }

            // Arama
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Ara...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RedPrimary)
            )
            Spacer(Modifier.height(8.dp))

            // KullanÄ±cÄ± listesi
            val filtered = allUsers.filter {
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                        it.email.contains(searchQuery, ignoreCase = true)
            }
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { user ->
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (pm.canManageUsers()) {
                                    editingUser = user; showFormDialog = true
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Yetkiniz yok.")
                                    }
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = Color.DarkGray)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(user.fullName, color = White, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("Mail: ${user.email}", color = White)
                            Spacer(Modifier.height(4.dp))
                            Text("GÃ¶rev: ${user.role}", color = White)
                            Spacer(Modifier.height(4.dp))
                            Text("Ä°ÅŸ Tel: ${user.workPhone}", color = White)
                            Spacer(Modifier.height(4.dp))
                            Text("KiÅŸ. Tel: ${user.personalPhone}", color = White)
                            Spacer(Modifier.height(4.dp))
                            Text("Aktif: ${if (user.isActive) "Evet" else "HayÄ±r"}", color = White)
                        }
                    }
                }
            }
        }
    }

    // KullanÄ±cÄ± ekleme/dÃ¼zenleme
    if (showFormDialog) {
        UserFormDialog(
            user = editingUser,
            onConfirm = { user, password ->
                val validation = when {
                    !user.email.endsWith("@CMCendÃ¼stri.com") -> "E-posta @CMCendÃ¼stri.com ile bitmeli"
                    password != null && password.length < 6 -> "Åifre en az 6 karakter olmalÄ±"
                    else -> null
                }
                if (validation != null) errorMessage = validation
                else userViewModel.saveUser(user, password,
                    onError = { errorMessage = it },
                    onSuccess = { showFormDialog = false; errorMessage = null }
                )
            },
            onDismiss = { showFormDialog = false; errorMessage = null }
        )
        errorMessage?.let { msg ->
            LaunchedEffect(msg) { coroutineScope.launch { snackbarHostState.showSnackbar(msg) } }
        }
    }

    // PDF Raporu (sadece iki tarih arasÄ±)
    if (showReportDialog) {
        PdfReportDialog(
            allUsers = allUsers,
            showDialog = showReportDialog,
            onDismiss = { showReportDialog = false },
            onGenerate = { user, startDate, endDate ->
                maintenanceViewModel.getUserProcessCount(user, startDate, endDate) { count ->
                    val stats = listOf(UserStats(user.fullName, count))
                    exportUserStatsToPDF(context, stats)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("${user.fullName} iÃ§in PDF oluÅŸturuldu")
                    }
                }
                showReportDialog = false
            }
        )
    }
}

@Composable
fun PdfReportDialog(
    allUsers: List<User>,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (user: User, startDate: LocalDate, endDate: LocalDate) -> Unit
) {
    if (!showDialog) return

    var selectedUser by remember { mutableStateOf<User?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(LocalDate.now().withDayOfMonth(1)) }
    var endDate by remember { mutableStateOf(LocalDate.now()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("PDF Raporu OluÅŸtur") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("KullanÄ±cÄ± SeÃ§", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = { expanded = true }) {
                        Text(selectedUser?.fullName ?: "KullanÄ±cÄ± SeÃ§iniz")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        allUsers.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user.fullName) },
                                onClick = {
                                    selectedUser = user; expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("BaÅŸlangÄ±Ã§ Tarihi", fontWeight = FontWeight.SemiBold)
                DatePickerButton(date = startDate) { startDate = it }
                Spacer(Modifier.height(8.dp))
                Text("BitiÅŸ Tarihi", fontWeight = FontWeight.SemiBold)
                DatePickerButton(date = endDate) { endDate = it }
            }
        },
        confirmButton = {
            Button(
                enabled = selectedUser != null && !endDate.isBefore(startDate),
                onClick = {
                    selectedUser?.let { onGenerate(it, startDate, endDate) }
                    onDismiss()
                }
            ) { Text("OluÅŸtur") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Ä°ptal") }
        }
    )
}

@Composable
fun DatePickerButton(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val context = LocalContext.current
    val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    OutlinedButton(
        onClick = {
            DatePickerDialog(
                context,
                { _, year, month, day -> onDateSelected(LocalDate.of(year, month + 1, day)) },
                date.year,
                date.monthValue - 1,
                date.dayOfMonth
            ).show()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(date.format(fmt))
    }
}
