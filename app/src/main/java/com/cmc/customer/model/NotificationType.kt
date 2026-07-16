package com.cmc.customer.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.cmc.customer.ui.theme.BluePrimary
import com.cmc.customer.ui.theme.GreenPrimary
import com.cmc.customer.ui.theme.RedPrimary

enum class NotificationType {
    stock_critical,
    maintenance_overdue,
    maintenance_upcoming,
    maintenance_done,
    maintenance_planned,
    task_assigned,
    tea_break_start_1,
    tea_break_end_1,
    tea_break_start_2,
    tea_break_end_2,
    lunch_start,
    lunch_end,
    daily,
    info,
    unknown;

    fun icon(): ImageVector = when (this) {
        stock_critical -> Icons.Default.Inventory
        maintenance_overdue -> Icons.Default.Warning
        maintenance_upcoming -> Icons.Default.Notifications
        maintenance_done -> Icons.Default.CheckCircle
        maintenance_planned -> Icons.Default.Event
        task_assigned -> Icons.AutoMirrored.Filled.List
        tea_break_start_1, tea_break_start_2 -> Icons.Default.LocalCafe
        tea_break_end_1, tea_break_end_2 -> Icons.Default.NotificationsOff
        lunch_start -> Icons.Default.Restaurant
        lunch_end -> Icons.Default.Done
        daily -> Icons.Default.List
        else -> Icons.Default.Info
    }

    fun emoji(): String = when (this) {
        stock_critical -> "ğŸ“¦"
        maintenance_overdue -> "âš ï¸"
        maintenance_upcoming -> "ğŸ””"
        maintenance_done -> "âœ…"
        maintenance_planned -> "ğŸ—“ï¸"
        task_assigned -> "ğŸ“"
        tea_break_start_1, tea_break_start_2 -> "â˜•"
        tea_break_end_1, tea_break_end_2 -> "ğŸ””"
        lunch_start -> "ğŸ½ï¸"
        lunch_end -> "ğŸ””"
        daily -> "ğŸ“"
        else -> "â„¹ï¸"
    }

    fun label(): String = when (this) {
        stock_critical -> "Kritik Stok"
        maintenance_overdue -> "Geciken BakÄ±m"
        maintenance_upcoming -> "YaklaÅŸan BakÄ±m"
        maintenance_done -> "Tamamlanan BakÄ±m"
        maintenance_planned -> "Planlanan BakÄ±m"
        task_assigned -> "GÃ¶rev AtamasÄ±"
        tea_break_start_1, tea_break_start_2 -> "Ã‡ay MolasÄ± BaÅŸladÄ±"
        tea_break_end_1, tea_break_end_2 -> "Ã‡ay MolasÄ± Bitti"
        lunch_start -> "Yemek MolasÄ±"
        lunch_end -> "Yemek Bitti"
        daily -> "GÃ¼nlÃ¼k BakÄ±m"
        else -> "Bildirim"
    }

    fun color(): Color = when (this) {
        stock_critical -> Color(0xBFB71C1C)       // koyu kÄ±rmÄ±zÄ±
        maintenance_overdue -> Color(0xBFE65100)  // koyu turuncu
        maintenance_upcoming -> Color(0xBF1E88E5) // doygun mavi
        maintenance_done -> Color(0xBF2E7D32)     // koyu yeÅŸil
        maintenance_planned -> Color(0xBF6A1B9A)  // koyu mor
        task_assigned -> Color(0xFF1565C0) // koyu mavi ton
        tea_break_start_1, tea_break_start_2 -> Color(0xBF5D4037) // koyu kahverengi
        tea_break_end_1, tea_break_end_2 -> Color(0xFF757575)      // koyu gri
        lunch_start, lunch_end -> Color(0xFF4E342E) // derin kahve
        daily -> Color(0xFF0277BD)              // koyu turkuaz
        else -> Color(0xFF424242)               // nÃ¶tr koyu gri
    }

}
