package com.soiltech.backend.infrastructure.service

import com.soiltech.backend.domain.entity.Notification
import com.soiltech.backend.domain.enum.NotificationType
import com.soiltech.backend.domain.repository.AdminProfileRepository
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.AgentRepository
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.domain.repository.NotificationRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val adminProfileRepository: AdminProfileRepository,
    private val agentRepository: AgentRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val customerProfileRepository: CustomerProfileRepository
) {
    fun push(
        userId: UUID,
        title: String,
        body: String,
        type: NotificationType,
        referenceId: UUID? = null,
        referenceType: String? = null
    ) {
        notificationRepository.save(
            Notification(
                id = UUID.randomUUID(),
                userId = userId,
                title = title,
                body = body,
                type = type,
                referenceId = referenceId,
                referenceType = referenceType,
                isRead = false,
                createdAt = LocalDateTime.now()
            )
        )
    }

    fun pushToAdmins(
        title: String,
        body: String,
        type: NotificationType,
        referenceId: UUID? = null,
        referenceType: String? = null
    ) {
        adminProfileRepository.findAll().filter { it.isActive }.forEach { admin ->
            push(admin.userId, title, body, type, referenceId, referenceType)
        }
    }

    fun pushToAgent(
        agentId: UUID,
        title: String,
        body: String,
        type: NotificationType,
        referenceId: UUID? = null,
        referenceType: String? = null
    ) {
        val agent = agentRepository.findById(agentId) ?: return
        val profile = agentProfileRepository.findByAgentCode(agent.agentCode) ?: return
        push(profile.userId, title, body, type, referenceId, referenceType)
    }

    fun pushToCustomer(
        customerProfileId: UUID,
        title: String,
        body: String,
        type: NotificationType,
        referenceId: UUID? = null,
        referenceType: String? = null
    ) {
        val profile = customerProfileRepository.findById(customerProfileId) ?: return
        push(profile.userId, title, body, type, referenceId, referenceType)
    }
}
