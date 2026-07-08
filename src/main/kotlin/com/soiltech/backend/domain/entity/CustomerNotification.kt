package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.NotificationCategory
import java.time.LocalDateTime
import java.util.UUID

data class CustomerNotification(
    val id: UUID,
    val title: String,
    val body: String,
    val target: String,
    val category: NotificationCategory,
    val sentCount: Int,
    val deliveredCount: Int,
    val openedCount: Int,
    val sentAt: LocalDateTime
)
