package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.NotificationType
import java.time.LocalDateTime
import java.util.UUID

data class Notification(
    val id: UUID,
    val userId: UUID,
    val title: String,
    val body: String,
    val type: NotificationType,
    val referenceId: UUID?,
    val referenceType: String?,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)
