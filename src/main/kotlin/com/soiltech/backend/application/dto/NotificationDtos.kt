package com.soiltech.backend.application.dto

import com.soiltech.backend.domain.enum.NotificationType
import java.time.LocalDateTime
import java.util.UUID

data class NotificationDto(
    val id: UUID,
    val title: String,
    val body: String,
    val type: NotificationType,
    val referenceId: UUID?,
    val referenceType: String?,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)

data class UnreadCountDto(val unreadCount: Long)
