package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.Notification
import com.soiltech.backend.domain.enum.NotificationType
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "notifications",
    indexes = [
        Index(name = "idx_notifications_user_id", columnList = "userId"),
        Index(name = "idx_notifications_is_read", columnList = "isRead"),
        Index(name = "idx_notifications_created_at", columnList = "createdAt")
    ]
)
class NotificationJpaEntity(
    @Id
    val id: UUID,

    @Column(nullable = false)
    val userId: UUID,

    @Column(nullable = false, length = 255)
    val title: String,

    @Column(nullable = false, columnDefinition = "text")
    val body: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    val type: NotificationType,

    @Column
    val referenceId: UUID? = null,

    @Column(length = 50)
    val referenceType: String? = null,

    @Column(nullable = false)
    var isRead: Boolean = false,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain() = Notification(
        id = id,
        userId = userId,
        title = title,
        body = body,
        type = type,
        referenceId = referenceId,
        referenceType = referenceType,
        isRead = isRead,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(n: Notification) = NotificationJpaEntity(
            id = n.id,
            userId = n.userId,
            title = n.title,
            body = n.body,
            type = n.type,
            referenceId = n.referenceId,
            referenceType = n.referenceType,
            isRead = n.isRead,
            createdAt = n.createdAt
        )
    }
}
