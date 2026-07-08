package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.ChatSenderType
import java.time.LocalDateTime
import java.util.UUID

data class ChatMessage(
    val id: UUID,
    val chatId: UUID,
    val senderType: ChatSenderType,
    val senderId: UUID,
    val senderName: String,
    val message: String,
    val sentAt: LocalDateTime
)
