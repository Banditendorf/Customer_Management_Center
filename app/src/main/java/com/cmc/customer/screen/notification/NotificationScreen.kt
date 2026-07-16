package com.cmc.customer.screen.notification

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.cmc.customer.model.*
import com.cmc.customer.ui.theme.*
import com.cmc.customer.ui.ui.RedTopBar
import com.cmc.customer.viewmodel.*
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationScreen(
    navController: NavHostController,
    notificationViewModel: NotificationsViewModel = viewModel(),
    materialViewModel: MaterialViewModel = viewModel(),
    machineViewModel: MachineViewModel = viewModel(),
    maintenanceViewModel: MaintenanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

    LaunchedEffect(Unit) {
        materialViewModel.loadMaterials(context, uid)
        machineViewModel.loadMachines(uid)
        delay(1000)
        notificationViewModel.markAllAsRead()
    }

    val allNotifications by notificationViewModel.notifications.collectAsState()
    val materials by materialViewModel.materials.collectAsState()
    val machines by machineViewModel.machines.collectAsState()
    val maintenances by maintenanceViewModel.maintenances.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<NotificationType?>(null) }

    val filtered = allNotifications.filter {
        (selectedType == null || it.type == selectedType?.name) &&
                it.message.contains(searchQuery, ignoreCase = true)
    }

    val bottomPinned = filtered.filter { it.isPersistent && it.pinnedAtBottom == true }
    val persistentTop = filtered.filter { it.isPersistent && it.pinnedAtBottom != true }
    val normal = filtered.filter { !it.isPersistent }




    Column(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        RedTopBar(title = "Bildirimler") {
            notificationViewModel.clearAll()
        }

        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Ara...", color = White) },
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            DropdownMenuFilter(selectedType) { selectedType = it }
        }

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (persistentTop.isNotEmpty()) {
                stickyHeader {
                    Text("ğŸ“Œ Sabitlenenler", color = LightGray, modifier = Modifier.fillMaxWidth().background(BackgroundDark).padding(4.dp))
                }
                items(persistentTop, key = { it.id }) { item ->
                    SwipeableNotificationCard(
                        item = item,
                        onDismiss = { notificationViewModel.remove(it) },
                        onMoveToBottom = { notificationViewModel.snoozeToBottom(it) },
                        onUpdatePersistence = { notificationViewModel.updatePersistence(it) },
                        onClick = {
                            navigateByNotification(item, materials, machines, maintenances, navController)
                        }
                    )
                }
            }

            if (normal.isNotEmpty()) {
                stickyHeader {
                    Text("ğŸ—ƒï¸ Bildirimler", color = LightGray, modifier = Modifier.fillMaxWidth().background(BackgroundDark).padding(4.dp))
                }
                items(normal, key = { it.id }) { item ->
                    SwipeableNotificationCard(
                        item = item,
                        onDismiss = { notificationViewModel.remove(it) },
                        onMoveToBottom = { notificationViewModel.snoozeToBottom(it) },
                        onUpdatePersistence = { notificationViewModel.updatePersistence(it) },
                        onClick = {
                            navigateByNotification(item, materials, machines, maintenances, navController)
                        }
                    )
                }
            }

            if (bottomPinned.isNotEmpty()) {
                stickyHeader {
                    Text("ğŸ“¥ AÅŸaÄŸÄ± Sabitlenenler", color = LightGray, modifier = Modifier.fillMaxWidth().background(BackgroundDark).padding(4.dp))
                }
                items(bottomPinned, key = { it.id }) { item ->
                    SwipeableNotificationCard(
                        item = item,
                        onDismiss = { notificationViewModel.remove(it) },
                        onMoveToBottom = { notificationViewModel.snoozeToBottom(it) },
                        onUpdatePersistence = { notificationViewModel.updatePersistence(it) },
                        onClick = {
                            navigateByNotification(item, materials, machines, maintenances, navController)
                        }
                    )
                }
            }

            if (persistentTop.isEmpty() && normal.isEmpty() && bottomPinned.isEmpty()) {
                item {
                    Text(
                        "ğŸ“­ Bildirim bulunamadÄ±.",
                        color = LightGray,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun DropdownMenuFilter(selected: NotificationType?, onSelected: (NotificationType?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    // Filtrelenecek tÃ¼rler
    val excludedTypes = setOf(
        NotificationType.tea_break_start_1,
        NotificationType.tea_break_end_1,
        NotificationType.tea_break_start_2,
        NotificationType.tea_break_end_2,
        NotificationType.lunch_start,
        NotificationType.lunch_end,
        NotificationType.daily,
        NotificationType.info,
        NotificationType.unknown
    )

    val filteredTypes = NotificationType.values()
        .filterNot { it in excludedTypes }

    val options = listOf<NotificationType?>(null) + filteredTypes

    OutlinedButton(onClick = { expanded = true }) {
        Text(selected?.label() ?: "TÃ¼mÃ¼")
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { type ->
            DropdownMenuItem(
                text = { Text(type?.label() ?: "TÃ¼mÃ¼") },
                onClick = {
                    onSelected(type)
                    expanded = false
                }
            )
        }
    }
}



@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun SwipeableNotificationCard(
    item: NotificationItem,
    onDismiss: (NotificationItem) -> Unit,
    onMoveToBottom: (NotificationItem) -> Unit,
    onClick: () -> Unit,
    onUpdatePersistence: (NotificationItem) -> Unit
) {
    val dismissState = rememberDismissState()
    val isPersistent = item.isPersistent
    val isBottomPinned = item.pinnedAtBottom == true

    val directionSet = setOf(DismissDirection.StartToEnd, DismissDirection.EndToStart)

    val rotation by animateFloatAsState(
        targetValue = when {
            dismissState.targetValue == DismissValue.DismissedToEnd && !isPersistent && !isBottomPinned -> -90f // Ã¼st sabitle
            dismissState.targetValue == DismissValue.DismissedToEnd && !isPersistent && isBottomPinned -> 90f // alt sabitle
            else -> 0f
        },
        animationSpec = tween(durationMillis = 300),
        label = "iconRotation"
    )

    LaunchedEffect(dismissState.currentValue) {
        when {
            dismissState.isDismissed(DismissDirection.StartToEnd) && !isPersistent && !isBottomPinned -> {
                val updated = item.copy(isPersistent = true, pinnedAtBottom = false)
                onUpdatePersistence(updated)
            }
            dismissState.isDismissed(DismissDirection.StartToEnd) && !isPersistent && isBottomPinned -> {
                val updated = item.copy(isPersistent = true, pinnedAtBottom = true)
                onUpdatePersistence(updated)
            }
            dismissState.isDismissed(DismissDirection.EndToStart) && !isPersistent -> {
                onDismiss(item)
            }
            dismissState.isDismissed(DismissDirection.StartToEnd) && isPersistent -> {
                val updated = item.copy(isPersistent = false, pinnedAtBottom = false)
                onUpdatePersistence(updated)
            }
            dismissState.isDismissed(DismissDirection.EndToStart) && isPersistent -> {
                val updated = item.copy(isPersistent = false, pinnedAtBottom = false)
                onUpdatePersistence(updated)
            }
        }
    }

    SwipeToDismiss(
        state = dismissState,
        directions = directionSet,
        background = {
            val direction = dismissState.dismissDirection
            val (color, icon) = when {
                direction == DismissDirection.StartToEnd && !isPersistent && !isBottomPinned -> Color(0xFFBBDEFB) to Icons.Default.KeyboardArrowUp
                direction == DismissDirection.StartToEnd && !isPersistent && isBottomPinned -> Color(0xFFFFF9C4) to Icons.Default.KeyboardArrowDown
                direction == DismissDirection.EndToStart && !isPersistent -> Color(0xFFFFCDD2) to Icons.Default.Delete
                direction == DismissDirection.StartToEnd && isPersistent -> Color(0xFFE0E0E0) to Icons.Default.KeyboardArrowLeft
                direction == DismissDirection.EndToStart && isPersistent -> Color(0xFFE0E0E0) to Icons.Default.KeyboardArrowRight
                else -> Color.Transparent to null
            }

            val alignment = when (direction) {
                DismissDirection.StartToEnd -> Alignment.CenterStart
                DismissDirection.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(color),
                contentAlignment = alignment
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier
                            .size(36.dp)
                            .graphicsLayer {
                                rotationZ = rotation
                            }
                            .padding(4.dp)
                    )
                }
            }
        },
        dismissContent = {
            NotificationCard(item = item, onClick = onClick)
        }
    )
}

@Composable
fun NotificationCard(
    item: NotificationItem,
    onClick: () -> Unit
) {
    val typeEnum = NotificationType.values().firstOrNull { it.name == item.type }
    val borderColor = typeEnum?.color() ?: Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1C1C1C), // nÃ¶tr koyu zemin
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Emoji doÄŸrudan yazÄ± olarak
            Text(
                text = typeEnum?.emoji() ?: "â„¹ï¸",
                fontSize = 24.sp,
                modifier = Modifier.padding(end = 12.dp),
                color = Color.White
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )

                typeEnum?.let {
                    Text(
                        text = it.label(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}





private fun navigateByNotification(
    item: NotificationItem,
    materials: List<Material>,
    machines: List<Machine>,
    maintenances: List<Maintenance>,
    navController: NavHostController
) {
    val type = item.type
    val data = item.data

    when (type) {
        "stock_critical" -> {
            val code = data["code"] ?: return
            materials.firstOrNull { it.code == code }
                ?.let { navController.navigate("materialDetail/${Uri.encode(Gson().toJson(it))}") }
        }
        "maintenance_planned", "maintenance_done", "maintenance_upcoming" -> {
            val maintenanceId = data["maintenanceId"] ?: return
            maintenances.firstOrNull { it.id == maintenanceId }
                ?.let { navController.navigate("maintenanceDetail/${Uri.encode(Gson().toJson(it))}") }
        }
        "maintenance_overdue" -> {
            val machineId = data["machineId"] ?: return
            machines.firstOrNull { it.id == machineId }
                ?.let { navController.navigate("machineDetail/${Uri.encode(Gson().toJson(it))}") }
        }
        "daily" -> navController.navigate("dailyMaintenance")
    }
}