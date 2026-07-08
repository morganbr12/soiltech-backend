package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.CustomerNotification
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomerNotificationRepository {
    fun save(notification: CustomerNotification): CustomerNotification
    fun findAll(pageable: Pageable): Page<CustomerNotification>
}
