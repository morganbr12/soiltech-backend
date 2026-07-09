package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.NotificationJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface NotificationJpaRepository : JpaRepository<NotificationJpaEntity, UUID> {

    fun findByUserIdOrderByCreatedAtDesc(userId: UUID, pageable: Pageable): Page<NotificationJpaEntity>

    fun countByUserIdAndIsReadFalse(userId: UUID): Long

    @Modifying
    @Query("UPDATE NotificationJpaEntity n SET n.isRead = true WHERE n.id = :id AND n.userId = :userId AND n.isRead = false")
    fun markAsRead(@Param("id") id: UUID, @Param("userId") userId: UUID): Int

    @Modifying
    @Query("UPDATE NotificationJpaEntity n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    fun markAllAsRead(@Param("userId") userId: UUID): Int
}
