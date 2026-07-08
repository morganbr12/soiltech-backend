package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.CustomerNotification
import com.soiltech.backend.domain.repository.CustomerNotificationRepository
import com.soiltech.backend.infrastructure.persistence.entity.CustomerNotificationJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerNotificationJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class CustomerNotificationRepositoryAdapter(
    private val jpa: CustomerNotificationJpaRepository
) : CustomerNotificationRepository {

    override fun save(notification: CustomerNotification): CustomerNotification =
        jpa.save(CustomerNotificationJpaEntity.fromDomain(notification)).toDomain()

    override fun findAll(pageable: Pageable): Page<CustomerNotification> =
        jpa.findAll(pageable).map { it.toDomain() }
}
