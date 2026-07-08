package com.soiltech.backend.application.dto.customer

import com.soiltech.backend.domain.enum.ChatSenderType
import com.soiltech.backend.domain.enum.ChatStatus
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

data class ChatResponse(
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
    val region: String?
)

data class ChatSummaryResponse(
    val open: Long,
    val pending: Long,
    val resolved: Long,
    val escalated: Long
)

data class ChatMessageResponse(
    val id: UUID,
    val chatId: UUID,
    val senderType: ChatSenderType,
    val senderId: UUID,
    val senderName: String,
    val message: String,
    val sentAt: LocalDateTime
)

data class SendMessageRequest(
    @field:NotBlank(message = "Message is required")
    val message: String
)

data class EscalateChatRequest(val reason: String? = null)

data class AssignChatRequest(
    @field:NotNull(message = "Agent ID is required")
    val agentId: UUID
)
