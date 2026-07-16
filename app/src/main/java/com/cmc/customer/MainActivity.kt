package com.cmc.customer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.cmc.customer.model.User
import com.cmc.customer.screen.main.AppNavigation
import com.cmc.customer.ui.theme.CMCTheme
import com.cmc.customer.util.EncryptedSharedPrefHelper
import com.cmc.customer.util.NotificationHelper
import com.cmc.customer.viewmodel.UserViewModel
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {

    // Ã‡oklu izin launcher (bildirim, kamera, depolama vb.)
    private val requestPermissionsLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                perms[Manifest.permission.POST_NOTIFICATIONS] == true
            else true

            if (notifGranted) {
                Log.d("MainActivity", "POST_NOTIFICATIONS izni verildi")
                setupNotificationSystem()
            } else {
                Log.w("MainActivity", "POST_NOTIFICATIONS izni reddedildi")
            }

            if (perms[Manifest.permission.CAMERA] != true) {
                Log.w("MainActivity", "Kamera izni reddedildi")
            }
            if (perms[Manifest.permission.READ_EXTERNAL_STORAGE] != true) {
                Log.w("MainActivity", "Depolama izni reddedildi")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Uygulama aÃ§Ä±lÄ±r aÃ§Ä±lmaz gerekli izinleri iste
        askAllPermissions()

        // â€œBeni HatÄ±rlaâ€ kontrolÃ¼
        val (rememberedEmail, isRemembered) = try {
            Pair(
                EncryptedSharedPrefHelper.getSavedEmail(this),
                EncryptedSharedPrefHelper.isRemembered(this)
            )
        } catch (e: Exception) {
            Log.e("MainActivity", "SecurePrefs okuma hatasÄ±", e)
            Pair(null, false)
        }

        setContent {
            CMCTheme {
                val navController = rememberNavController()
                var isLoggedIn by remember { mutableStateOf(isRemembered) }
                var currentUser by remember { mutableStateOf<User?>(null) }
                val userViewModel: UserViewModel = viewModel()

                // Login durumunu tCMCp et
                LaunchedEffect(isLoggedIn) {
                    if (isLoggedIn) {
                        userViewModel.fetchCurrentUser()
                        userViewModel.currentUser.collect { user ->
                            currentUser = user
                        }
                    }
                }

                if (isLoggedIn && currentUser == null) {
                    // YÃ¼kleniyor ekranÄ±
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else {
                    // GiriÅŸ veya ana ekran
                    AppNavigation(
                        navController = navController,
                        isLoggedIn = isLoggedIn,
                        currentUser = currentUser,
                        onLoginSuccess = { email ->
                            isLoggedIn = true
                            EncryptedSharedPrefHelper.saveRememberMe(this, email)
                        },
                        onLogout = {
                            isLoggedIn = false
                            EncryptedSharedPrefHelper.clearRememberMe(this)
                        }
                    )
                }
            }
        }
    }

    /**
     * Uygulama aÃ§Ä±lÄ±ÅŸÄ±nda gerekli izinleri topluca sorar
     */
    private fun askAllPermissions() {
        val perms = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
            add(Manifest.permission.CAMERA)
            add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        requestPermissionsLauncher.launch(perms.toTypedArray())
    }

    /**
     * Bildirim kanallarÄ±nÄ± oluÅŸturur, topicâ€™e abone olur ve FCM tokenâ€™Ä± kaydeder
     */
    private fun setupNotificationSystem() {
        NotificationHelper.init(applicationContext)

        Firebase.messaging.token.addOnSuccessListener { token ->
            Log.d("FCM", "Token alÄ±ndÄ±: $token")

            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("FCM", "fcmToken Firestore'a yazÄ±ldÄ±")
                    }
                    .addOnFailureListener {
                        Log.e("FCM", "fcmToken Firestore'a yazÄ±lamadÄ±", it)
                    }
            }
        }

        Firebase.messaging
            .subscribeToTopic("maintenance")
            .addOnSuccessListener { Log.d("FCM", "Maintenance topicâ€™e abone olundu") }
            .addOnFailureListener { Log.e("FCM", "Topic abonelik hatasÄ±", it) }
    }
}
