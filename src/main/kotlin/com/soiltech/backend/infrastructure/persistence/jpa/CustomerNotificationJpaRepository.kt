package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.CustomerNotificationJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerNotificationJpaRepository : JpaRepository<CustomerNotificationJpaEntity, UUID>
