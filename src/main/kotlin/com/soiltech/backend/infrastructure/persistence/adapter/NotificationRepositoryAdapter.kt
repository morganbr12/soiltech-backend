package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.Notification
import com.soiltech.backend.domain.repository.NotificationRepository
import com.soiltech.backend.infrastructure.persistence.entity.NotificationJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.NotificationJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Component
class NotificationRepositoryAdapter(
    private val jpaRepository: NotificationJpaRepository
) : NotificationRepository {

    override fun save(notification: Notification): Notification =
        jpaRepository.save(NotificationJpaEntity.fromDomain(notification)).toDomain()

    override fun findById(id: UUID): Notification? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByUserId(userId: UUID, pageable: Pageable): Page<Notification> =
        jpaRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable).map { it.toDomain() }

    override fun countUnreadByUserId(userId: UUID): Long =
        jpaRepository.countByUserIdAndIsReadFalse(userId)

    @Transactional
    override fun markAsRead(id: UUID, userId: UUID): Boolean =
        jpaRepository.markAsRead(id, userId) > 0

    @Transactional
    override fun markAllAsRead(userId: UUID): Int =
        jpaRepository.markAllAsRead(userId)
}
