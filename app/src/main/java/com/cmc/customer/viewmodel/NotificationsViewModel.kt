package com.cmc.customer.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cmc.customer.model.NotificationItem
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class NotificationsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications

    init {
        listenNotifications()
    }

    private fun listenNotifications() {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy - HH:mm", Locale.getDefault())
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users").document(uid).collection("notifications")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    Log.e("NotificationsVM", "Listen error", err)
                    return@addSnapshotListener
                }

                val list = snap.documents.mapNotNull { doc ->
                    val id = doc.id
                    val msg = doc.getString("message") ?: return@mapNotNull null
                    val type = doc.getString("type") ?: "generic"
                    val persist = doc.getBoolean("isPersistent") ?: false
                    val isRead = doc.getBoolean("isRead") ?: false
                    val category = doc.getString("category") ?: "system"
                    val dataMap = (doc.get("data") as? Map<*, *>)?.mapNotNull {
                        val key = it.key as? String
                        val value = it.value as? String
                        if (key != null && value != null) key to value else null
                    }?.toMap() ?: emptyMap()

                    val rawTs = doc.get("timestamp")
                    val ts: Long = when (rawTs) {
                        is Long -> rawTs
                        is Double -> rawTs.toLong()
                        is String -> runCatching {
                            dateFormat.parse(rawTs)?.time ?: 0L
                        }.getOrElse {
                            Log.w("NotificationsVM", "Invalid ts format: $rawTs")
                            0L
                        }
                        else -> 0L
                    }

                    NotificationItem(
                        id = id,
                        message = msg,
                        type = type,
                        timestamp = ts,
                        data = dataMap,
                        isPersistent = persist,
                        isRead = isRead,
                        category = category
                    )
                }

                _notifications.value = list
            }
    }
    fun updatePersistence(item: NotificationItem) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val docRef = Firebase.firestore
            .collection("users")
            .document(uid)
            .collection("notifications")
            .document(item.id)

        val updates = mapOf(
            "isPersistent" to item.isPersistent,
            "pinnedAtBottom" to item.pinnedAtBottom
        )

        docRef.update(updates)
    }


    fun markAllAsRead() {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            _notifications.value.forEach { item ->
                if (!item.isRead) {
                    db.collection("users").document(uid)
                        .collection("notifications")
                        .document(item.id)
                        .update("isRead", true)
                }
            }
        }
    }
    fun updateNotificationPersistence(item: NotificationItem, persistent: Boolean) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = Firebase.firestore
            .collection("users")
            .document(uid)
            .collection("notifications")
            .document(item.id)

        ref.update("isPersistent", persistent)
    }


    fun remove(item: NotificationItem) {
        _notifications.update { it.filterNot { n -> n.id == item.id } }
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            db.collection("users").document(uid)
                .collection("notifications")
                .document(item.id)
                .delete()
        }
    }

    fun snoozeToBottom(item: NotificationItem) {
        _notifications.update { list ->
            list.filterNot { it.id == item.id } + item
        }
    }

    fun clearAll() {
        _notifications.value = emptyList()
    }

    fun markAsRead(item: NotificationItem) {
        viewModelScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            db.collection("users").document(uid)
                .collection("notifications")
                .document(item.id)
                .update("isRead", true)
        }
    }
}
