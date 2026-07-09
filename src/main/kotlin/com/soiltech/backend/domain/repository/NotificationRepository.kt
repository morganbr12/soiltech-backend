package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.Notification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface NotificationRepository {
    fun save(notification: Notification): Notification
    fun findById(id: UUID): Notification?
    fun findByUserId(userId: UUID, pageable: Pageable): Page<Notification>
    fun countUnreadByUserId(userId: UUID): Long
    fun markAsRead(id: UUID, userId: UUID): Boolean
    fun markAllAsRead(userId: UUID): Int
}
