package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.CustomerChat
import com.soiltech.backend.domain.enum.ChatStatus
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "customer_chats",
    indexes = [
        Index(name = "idx_chats_customer_id", columnList = "customerId"),
        Index(name = "idx_chats_status", columnList = "status")
    ]
)
class CustomerChatJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val customerId: UUID,

    @Column(nullable = false, length = 255)
    var customerName: String,

    var agentId: UUID? = null,

    @Column(length = 255)
    var agentName: String? = null,

    @Column(length = 1000)
    var lastMessage: String? = null,

    var lastMessageAt: LocalDateTime? = null,

    @Column(nullable = false)
    var unreadCount: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var status: ChatStatus = ChatStatus.OPEN,

    @Column(length = 255)
    var topic: String? = null,

    @Column(length = 100)
    var region: String? = null,

    @Column(length = 1000)
    var escalationReason: String? = null

) : BaseJpaEntity() {

    fun toDomain(): CustomerChat = CustomerChat(
        id = id!!,
        customerId = customerId,
        customerName = customerName,
        agentId = agentId,
        agentName = agentName,
        lastMessage = lastMessage,
        lastMessageAt = lastMessageAt,
        unreadCount = unreadCount,
        status = status,
        topic = topic,
        region = region,
        escalationReason = escalationReason,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(c: CustomerChat): CustomerChatJpaEntity = CustomerChatJpaEntity(
            id = c.id,
            customerId = c.customerId,
            customerName = c.customerName,
            agentId = c.agentId,
            agentName = c.agentName,
            lastMessage = c.lastMessage,
            lastMessageAt = c.lastMessageAt,
            unreadCount = c.unreadCount,
            status = c.status,
            topic = c.topic,
            region = c.region,
            escalationReason = c.escalationReason
        )
    }
}
