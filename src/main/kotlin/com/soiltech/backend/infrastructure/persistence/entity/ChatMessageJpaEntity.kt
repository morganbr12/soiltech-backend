package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.ChatMessage
import com.soiltech.backend.domain.enum.ChatSenderType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "chat_messages",
    indexes = [Index(name = "idx_chat_messages_chat_id", columnList = "chatId")]
)
class ChatMessageJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val chatId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    val senderType: ChatSenderType,

    @Column(nullable = false)
    val senderId: UUID,

    @Column(nullable = false, length = 255)
    val senderName: String,

    @Column(nullable = false, columnDefinition = "text")
    val message: String,

    @Column(nullable = false, updatable = false)
    val sentAt: LocalDateTime = LocalDateTime.now()

) {
    fun toDomain(): ChatMessage = ChatMessage(
        id = id!!,
        chatId = chatId,
        senderType = senderType,
        senderId = senderId,
        senderName = senderName,
        message = message,
        sentAt = sentAt
    )

    companion object {
        fun fromDomain(m: ChatMessage): ChatMessageJpaEntity = ChatMessageJpaEntity(
            id = m.id,
            chatId = m.chatId,
            senderType = m.senderType,
            senderId = m.senderId,
            senderName = m.senderName,
            message = m.message,
            sentAt = m.sentAt
        )
    }
}
