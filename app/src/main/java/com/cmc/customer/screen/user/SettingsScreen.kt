package com.cmc.customer.screen.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmc.customer.permission.PermissionManager
import com.cmc.customer.ui.theme.*
import com.cmc.customer.ui.ui.RedTopBar
import com.cmc.customer.util.NotificationHelper
import com.cmc.customer.viewmodel.AuthViewModel
import com.cmc.customer.viewmodel.CompanyViewModel
import com.cmc.customer.viewmodel.MaterialViewModel
import com.cmc.customer.viewmodel.UserViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color.Companion.Green
import com.cmc.customer.model.NotificationPreferences
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request

@Composable
fun SettingsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLogout: () -> Unit,
    onViewLogsClick: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        NotificationHelper.init(context)
    }

    val userViewModel: UserViewModel = viewModel()
    val user by userViewModel.currentUser.collectAsState()
    val companyViewModel: CompanyViewModel = viewModel()
    val materialViewModel: MaterialViewModel = viewModel()

    var feedback by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var passwordChangeMessage by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pm = PermissionManager.getInstance(context)

    // 1) Coroutine scope ve scaffoldState tanÄ±mlamalarÄ±
    val scaffoldState = rememberScaffoldState()



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RedTopBar(title = "Ayarlar")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Geri Bildirim
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Geri Bildirim", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = feedback,
                        onValueChange = { feedback = it },
                        label = { Text("GÃ¶rÃ¼ÅŸ veya Ã¶nerinizi yazÄ±n") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
                            val formattedDate = sdf.format(Date())
                            val data = hashMapOf(
                                "message" to feedback,
                                "timestamp" to formattedDate,
                                "userEmail" to (user?.email ?: "bilinmiyor")
                            )
                            FirebaseFirestore.getInstance()
                                .collection("feedback")
                                .add(data)
                                .addOnSuccessListener {
                                    sent = true
                                    feedback = ""
                                }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) { Text("GÃ¶nder") }
                    if (sent) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("TeÅŸekkÃ¼rler, mesajÄ±nÄ±z iletildi.", color = Green)
                    }
                }
            }

//            // â€”â€” BURAYA EKLENECEK TEST BUTONU â€”â€”
//            Button(
//                onClick = {
//                    scope.launch(Dispatchers.IO) {
//                        try {
//                            val client = OkHttpClient()
//                            val request = Request.Builder()
//                                .url("https://us-central1-CMC-portal-f3900.cloudfunctions.net/testPushNotification")
//                                .get()
//                                .build()
//                            val response = client.newCall(request).execute()
//                            val message = if (response.isSuccessful) {
//                                "Ä°stek baÅŸarÄ±yla gÃ¶nderildi!"
//                            } else {
//                                "Hata: ${response.code}"
//                            }
//                            // Snackbar UI threadâ€™inde gÃ¶stermek iÃ§in
//                            scope.launch {
//                                scaffoldState.snackbarHostState.showSnackbar(message)
//                            }
//                        } catch (e: Exception) {
//                            scope.launch {
//                                scaffoldState.snackbarHostState.showSnackbar("Ä°stek atÄ±lamadÄ±: ${e.localizedMessage}")
//                            }
//                        }
//                    }
//                },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp)
//            ) {
//                Text("Ali CMC Ultisi")
//            }
//            // â€”â€” EKLEME SONU â€”â€”

//            Spacer(modifier = Modifier.height(32.dp))

            // Ã‡Ä±kÄ±ÅŸ yap
            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                modifier = Modifier.fillMaxWidth()
            ) { Text("Ã‡Ä±kÄ±ÅŸ Yap", color = White) }

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Uygulama SÃ¼rÃ¼mÃ¼: 1.2.0",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "Â© 2025 AKÄ° EndÃ¼stri. TÃ¼m haklarÄ± saklÄ±dÄ±r.",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        SnackbarHost(hostState = snackbarHostState)
    }
}

@Composable
fun NotificationSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
