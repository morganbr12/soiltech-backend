package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.ChatStatus
import java.time.LocalDateTime
import java.util.UUID

data class CustomerChat(
    val id: UUID,
    val customerId: UUID,
    val customerName: String,
    val agentId: UUID?,
    val agentName: String?,
    val lastMessage: String?,
    val lastMessageAt: LocalDateTime?,
    val unreadCount: Int,
    val status: ChatStatus,
    val topic: String?,
    val region: String?,
    val escalationReason: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
