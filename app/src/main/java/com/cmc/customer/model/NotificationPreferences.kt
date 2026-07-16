package com.cmc.customer.model

data class NotificationPreferences(
    var stockCritical: Boolean = true,
    var maintenanceUpcoming: Boolean = true,
    var maintenanceOverdue: Boolean = true,
    var maintenanceDone: Boolean = true,
    var breakAlerts: Boolean = true,
    var taskAssigned: Boolean = true // ğŸ‘ˆ BU SATIR GEREKLÄ°
)
