package com.cmc.customer.ui

import android.content.Context
import android.view.MenuItem
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cmc.customer.R
import com.cmc.customer.model.User
import com.cmc.customer.permission.PermissionManager
import com.cmc.customer.ui.theme.BackgroundDark
import com.cmc.customer.ui.theme.DarkGray
import com.cmc.customer.ui.theme.LightGray
import com.cmc.customer.ui.theme.RedPrimary
import com.cmc.customer.ui.theme.White
import com.cmc.customer.ui.ui.RedTopBar
import com.cmc.customer.viewmodel.MaintenanceViewModel
import com.cmc.customer.viewmodel.MaterialViewModel
import com.cmc.customer.viewmodel.NotificationsViewModel
import kotlinx.coroutines.launch

private data class MenuItem(
    val title: String,
    val route: String,
    val icon: ImageVector,
    val canAccess: (PermissionManager) -> Boolean
)

@Composable
fun MainMenuScreen(
    currentUser: User,
    onMenuItemClick: (String) -> Unit,
    onNotificationClick: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val pm = PermissionManager.getInstance(context)

    val maintenanceViewModel: MaintenanceViewModel   = viewModel()
    val notificationsViewModel: NotificationsViewModel = viewModel()

    // 1) Maintenance ve Material bildirimlerini ayrÄ± al
    val maintenanceNotifications by maintenanceViewModel.notificationList.collectAsState(initial = emptyList())
    val materialNotifications  by notificationsViewModel.notifications.collectAsState(initial = emptyList())

// 2) Ä°kisini birleÅŸtirip tek bir listeye dÃ¶nÃ¼ÅŸtÃ¼r
    val allNotifications = remember(maintenanceNotifications, materialNotifications) {
        (maintenanceNotifications + materialNotifications)
            .distinctBy { it.id }
            .sortedByDescending { it.timestamp }
    }



    // MenÃ¼ tanÄ±mÄ± + log ile kontrol
    val menuItems = listOf(
        MenuItem("Åirketler", "companies", Icons.Default.Business) {
            val access = try { it.canViewCompanies() } catch (e: Exception) { false }
            access
        },
        MenuItem("Planlanan BakÄ±mlar", "bCMCmlar", Icons.Default.CalendarToday) {
            val access = try { it.canViewMaintenancePlans() } catch (e: Exception) { false }
            access
        },
        MenuItem("HazÄ±rlanacak Listeler", "hazirliklar", Icons.Default.ListAlt) {
            val access = try { it.canViewPreparationLists() } catch (e: Exception) { false }
            access
        },
        MenuItem("Malzemeler", "malzemeler", Icons.Default.Inventory2) {
            val access = try { it.canViewMaterialsList() } catch (e: Exception) { false }
            access
        },
        MenuItem("KullanÄ±cÄ±lar", "kullanicilar", Icons.Default.People) {
            val access = try { it.canViewUsers() } catch (e: Exception) { false }
            access
        },
        MenuItem("Ayarlar", "ayarlar", Icons.Default.Settings) { _ -> true } // HERKES ERÄ°ÅEBÄ°LÄ°R
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        RedTopBar(
            title = currentUser.fullName,
            showMenu = false,
            trailingImage = painterResource(id = R.drawable.turk_bayragi),
            trailingImageDescription = "TÃ¼rk BayraÄŸÄ±",
            trailingImageSize = 70.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(0.75f)
                .padding(horizontal = 12.dp)
        ) {
            items(menuItems) { item ->
                Card(
                    onClick = {
                        val hasPermission = try { item.canAccess(pm) } catch (e: Exception) { false }
                        if (hasPermission) {
                            onMenuItemClick(item.route)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Bu iÅŸlem iÃ§in yetkiniz yok.")
                            }
                        }
                    },
                    colors = CardDefaults.cardColors(containerColor = RedPrimary),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            color = White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(DarkGray)
                .clickable { onNotificationClick() }
                .padding(16.dp)
        ) {
            Divider(
                color = BackgroundDark.copy(alpha = 0.4f),
                thickness = 1.dp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ğŸ“© Bildirimler",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val previewText = if (allNotifications.isEmpty()) {
                "Yeni bildiriminiz yok."
            } else {
                allNotifications.take(3).joinToString("\n\n") { "â€¢ ${it.message}" }
            }

            Text(
                text = previewText,
                fontSize = 13.sp,
                color = LightGray,
                lineHeight = 18.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }


        SnackbarHost(hostState = snackbarHostState)
    }
}
