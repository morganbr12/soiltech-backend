package com.soiltech.backend.application.usecase.customer

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.domain.entity.CustomerNotification
import com.soiltech.backend.domain.repository.CustomerNotificationRepository
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.enum.CustomerStatus
import com.soiltech.backend.domain.enum.CustomerTier
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class SendNotificationUseCase(
    private val notificationRepository: CustomerNotificationRepository,
    private val customerProfileRepository: CustomerProfileRepository
) {
    @Transactional
    fun execute(request: SendNotificationRequest): SendNotificationResult {
        val recipientCount = resolveTargetCount(request.target)
        val now = LocalDateTime.now()
        notificationRepository.save(
            CustomerNotification(
                id = UUID.randomUUID(),
                title = request.title,
                body = request.body,
                target = request.target,
                category = request.category,
                sentCount = recipientCount,
                deliveredCount = recipientCount,
                openedCount = 0,
                sentAt = now
            )
        )
        return SendNotificationResult(sent = recipientCount, failed = 0)
    }

    private fun resolveTargetCount(target: String): Int {
        return when {
            target == "all" -> customerProfileRepository.countAll().toInt()
            target == "active" -> customerProfileRepository.countByStatus()
                .let { (it[CustomerStatus.ACTIVE] ?: 0L) + (it[CustomerStatus.VERIFIED] ?: 0L) }.toInt()
            target == "pending" -> (customerProfileRepository.countByStatus()[CustomerStatus.PENDING] ?: 0L).toInt()
            target.startsWith("tier:") -> {
                val tierStr = target.removePrefix("tier:")
                val tier = CustomerTier.entries.firstOrNull { it.value.equals(tierStr, ignoreCase = true) }
                    ?: return 0
                (customerProfileRepository.countByTier()[tier] ?: 0L).toInt()
            }
            target.startsWith("region:") -> {
                val region = target.removePrefix("region:")
                customerProfileRepository.findAll(null, null, region, null,
                    PageRequest.of(0, 1)).totalElements.toInt()
            }
            else -> 0
        }
    }
}

@Service
class NotificationHistoryUseCase(private val notificationRepository: CustomerNotificationRepository) {
    fun execute(page: Int, limit: Int): Pair<List<CustomerNotificationResponse>, PaginationMeta> {
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100), Sort.by(Sort.Direction.DESC, "sentAt"))
        val resultPage = notificationRepository.findAll(pageable)
        return Pair(resultPage.content.map { it.toResponse() }, PaginationMeta.from(resultPage, page, limit))
    }
}

private fun CustomerNotification.toResponse() = CustomerNotificationResponse(
    id = id, title = title, target = target, sentCount = sentCount,
    deliveredCount = deliveredCount, openedCount = openedCount, sentAt = sentAt
)
