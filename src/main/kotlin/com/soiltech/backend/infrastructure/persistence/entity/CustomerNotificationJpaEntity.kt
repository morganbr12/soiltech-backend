package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.CustomerNotification
import com.soiltech.backend.domain.enum.NotificationCategory
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "customer_notifications")
class CustomerNotificationJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false, length = 500)
    val title: String,

    @Column(nullable = false, columnDefinition = "text")
    val body: String,

    @Column(nullable = false, length = 255)
    val target: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    val category: NotificationCategory,

    @Column(nullable = false)
    var sentCount: Int = 0,

    @Column(nullable = false)
    var deliveredCount: Int = 0,

    @Column(nullable = false)
    var openedCount: Int = 0,

    @Column(nullable = false, updatable = false)
    val sentAt: LocalDateTime = LocalDateTime.now()

) {
    fun toDomain(): CustomerNotification = CustomerNotification(
        id = id!!,
        title = title,
        body = body,
        target = target,
        category = category,
        sentCount = sentCount,
        deliveredCount = deliveredCount,
        openedCount = openedCount,
        sentAt = sentAt
    )

    companion object {
        fun fromDomain(n: CustomerNotification): CustomerNotificationJpaEntity = CustomerNotificationJpaEntity(
            id = n.id,
            title = n.title,
            body = n.body,
            target = n.target,
            category = n.category,
            sentCount = n.sentCount,
            deliveredCount = n.deliveredCount,
            openedCount = n.openedCount,
            sentAt = n.sentAt
        )
    }
}
