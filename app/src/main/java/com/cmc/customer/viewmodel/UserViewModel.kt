package com.cmc.customer.viewmodel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cmc.customer.model.NotificationPreferences
import com.cmc.customer.model.User
import com.cmc.customer.permission.PermissionManager
import com.cmc.customer.util.LogHelper
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.cmc.customer.util.UserStats
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val pm = PermissionManager.getInstance(application.applicationContext)

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers

    init {
        fetchCurrentUser()
        listenAllUsers()
    }

    /**
     * GiriÅŸ yapan kullanÄ±cÄ±nÄ±n bilgilerini dinler ve gÃ¼nceller
     */
    fun fetchCurrentUser() {
        auth.currentUser?.uid?.let { uid ->
            db.collection("users").document(uid)
                .addSnapshotListener { doc, error ->
                    if (error == null && doc != null && doc.exists()) {
                        val user = doc.toObject(User::class.java)
                        if (user != null) {
                            _currentUser.value = user
                            pm.saveUser(user)
                            Log.d("UserViewModel", "KullanÄ±cÄ± yÃ¼klendi: ${user.email}")
                        } else {
                            Log.w("UserViewModel", "Firebase'den gelen kullanÄ±cÄ± verisi null.")
                        }
                    } else {
                        Log.e("UserViewModel", "KullanÄ±cÄ± verisi alÄ±namadÄ±: ${error?.message}")
                    }
                }
        } ?: Log.w("UserViewModel", "GiriÅŸ yapan kullanÄ±cÄ± UID alÄ±namadÄ±.")
    }

    /**
     * TÃ¼m kullanÄ±cÄ±larÄ± dinler
     */
    private fun listenAllUsers() {
        db.collection("users")
            .addSnapshotListener { snaps, error ->
                if (error == null && snaps != null) {
                    _allUsers.value = snaps.documents.mapNotNull { it.toObject(User::class.java) }
                } else {
                    Log.e("UserViewModel", "TÃ¼m kullanÄ±cÄ±lar alÄ±namadÄ±: ${error?.message}")
                }
            }
    }

    /**
     * Yeni kullanÄ±cÄ± oluÅŸturur veya mevcut kullanÄ±cÄ±yÄ± gÃ¼nceller
     */
    fun saveUser(
        user: User,
        password: String? = null,
        onError: (String) -> Unit = {},
        onSuccess: () -> Unit = {}
    ) {
        if (!pm.canManageUsers()) {
            Toast.makeText(getApplication(), "Yetkiniz yok", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != null && user.uid.isEmpty()) {
            auth.createUserWithEmailAndPassword(user.email, password)
                .addOnSuccessListener { authRes ->
                    val newUid = authRes.user?.uid ?: return@addOnSuccessListener
                    val newUser = user.copy(uid = newUid)
                    db.collection("users").document(newUid)
                        .set(newUser)
                        .addOnSuccessListener {
                            logUserAction("KullanÄ±cÄ± Eklendi", newUid)
                            onSuccess()
                            fetchCurrentUser() // kendi kaydÄ±nÄ± gÃ¼ncelle
                        }
                        .addOnFailureListener { e -> onError(e.localizedMessage ?: "VeritabanÄ± hatasÄ±") }
                }
                .addOnFailureListener { e -> onError(e.localizedMessage ?: "Authentication hatasÄ±") }
        } else {
            viewModelScope.launch {
                db.collection("users").document(user.uid)
                    .set(user, SetOptions.merge())
                    .addOnSuccessListener {
                        logUserAction("KullanÄ±cÄ± GÃ¼ncellendi", user.uid)
                        onSuccess()
                        fetchCurrentUser()
                    }
                    .addOnFailureListener { e -> onError(e.localizedMessage ?: "GÃ¼ncelleme hatasÄ±") }
            }
        }
    }
    fun updateNotificationPreferences(userId: String, prefs: NotificationPreferences) {
        if (!pm.canManageUsers()) {
            Toast.makeText(getApplication(), "Yetkiniz yok", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("users").document(userId)
            .update("notificationPreferences", prefs)
            .addOnSuccessListener {
                Log.d("UserViewModel", "Bildirim tercihleri gÃ¼ncellendi.")
            }
            .addOnFailureListener { e ->
                Log.e("UserViewModel", "Tercihler gÃ¼ncellenemedi: ${e.localizedMessage}")
            }
    }
    fun getUserProcessCount(
        user: User,
        start: LocalDate,
        end: LocalDate,
        onResult: (count: Int) -> Unit
    ) {
        val fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        db.collection("plannedMaintenances")
            .whereGreaterThanOrEqualTo("plannedDate", start.format(fmt))
            .whereLessThanOrEqualTo("plannedDate", end.format(fmt))
            .get()
            .addOnSuccessListener { snapshot ->
                val count = snapshot.documents.count { doc ->
                    val responsibles = doc.get("responsibles") as? List<String> ?: emptyList()
                    responsibles.any { it.equals(user.fullName, ignoreCase = true) }
                }
                onResult(count)
            }
            .addOnFailureListener {
                onResult(0)
            }
    }

    // TÃ¼m kullanÄ±cÄ±lar iÃ§in haftalÄ±k/aylÄ±k/yÄ±llÄ±k istatistik:


    fun getMachineNotificationPreference(machineId: String, onResult: (Boolean) -> Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore
            .collection("users")
            .document(uid)
            .collection("machinePreferences")
            .document(machineId)
            .get()
            .addOnSuccessListener { doc ->
                val enabled = doc.getBoolean("receiveNotifications") ?: true
                onResult(enabled)
            }
            .addOnFailureListener {
                onResult(true)
            }
    }


    fun updateMachineNotificationPreference(machineId: String, enabled: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        Firebase.firestore
            .collection("users")
            .document(uid)
            .collection("machinePreferences")
            .document(machineId)
            .set(mapOf("receiveNotifications" to enabled))
    }

    fun deleteUser(uid: String, onComplete: () -> Unit = {}) {
        if (!pm.canManageUsers()) {
            Toast.makeText(getApplication(), "Yetkiniz yok", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("users").document(uid)
            .delete()
            .addOnSuccessListener {
                logUserAction("KullanÄ±cÄ± Silindi", uid)
                onComplete()
            }
    }

    fun toggleUserActive(uid: String, newState: Boolean) {
        if (!pm.canManageUsers()) {
            Toast.makeText(getApplication(), "Yetkiniz yok", Toast.LENGTH_SHORT).show()
            return
        }
        db.collection("users").document(uid)
            .update("isActive", newState)
            .addOnSuccessListener {
                logUserAction("Aktiflik Durumu GÃ¼ncellendi", uid, "newState" to newState)
            }
    }

    /**
     * Kendi e-posta adresini dÃ¶ner
     */
    private fun currentUserEmail(): String = _currentUser.value?.email.orEmpty()

    /**
     * Firebase loglama iÅŸlemi
     */
    private fun logUserAction(action: String, uid: String, vararg details: Pair<String, Any?>) {
        val logMap = buildMap<String, Any> {
            put("uid", uid)
            details.forEach { (k, v) ->
                if (v != null) put(k, v) // null deÄŸerleri dahil etme
            }
        }

        LogHelper.logFirebaseAction(
            userEmail = currentUserEmail(),
            action = action,
            details = logMap
        )
    }


    /**
     * UI katmanÄ±na direkt yetki verebilecek yardÄ±mcÄ± fonksiyonlar
     */
    fun canManageUsers(): Boolean = _currentUser.value?.permissions?.manageUser ?: false
    fun canViewCompanies(): Boolean = _currentUser.value?.permissions?.viewCompanies ?: false
    // DiÄŸer izinler de buraya eklenebilir...
}

