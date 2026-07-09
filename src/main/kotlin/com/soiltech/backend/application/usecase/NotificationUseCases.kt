package com.soiltech.backend.application.usecase

import com.soiltech.backend.application.dto.NotificationDto
import com.soiltech.backend.application.dto.UnreadCountDto
import com.soiltech.backend.domain.repository.NotificationRepository
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.UUID

fun com.soiltech.backend.domain.entity.Notification.toDto() = NotificationDto(
    id = id, title = title, body = body, type = type,
    referenceId = referenceId, referenceType = referenceType,
    isRead = isRead, createdAt = createdAt
)

@Service
class ListNotificationsUseCase(private val notificationRepository: NotificationRepository) {
    fun execute(userId: UUID, page: Int, limit: Int): Pair<List<NotificationDto>, PaginationMeta> {
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100), Sort.by(Sort.Direction.DESC, "createdAt"))
        val result = notificationRepository.findByUserId(userId, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, limit)
    }
}

@Service
class GetUnreadCountUseCase(private val notificationRepository: NotificationRepository) {
    fun execute(userId: UUID): UnreadCountDto =
        UnreadCountDto(notificationRepository.countUnreadByUserId(userId))
}

@Service
class MarkNotificationReadUseCase(private val notificationRepository: NotificationRepository) {
    fun execute(id: UUID, userId: UUID) {
        notificationRepository.markAsRead(id, userId)
    }
}

@Service
class MarkAllNotificationsReadUseCase(private val notificationRepository: NotificationRepository) {
    fun execute(userId: UUID) {
        notificationRepository.markAllAsRead(userId)
    }
}
