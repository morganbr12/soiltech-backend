package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.CustomerChatJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerChatJpaRepository :
    JpaRepository<CustomerChatJpaEntity, UUID>,
    JpaSpecificationExecutor<CustomerChatJpaEntity> {

    @Query("SELECT c.status, COUNT(c) FROM CustomerChatJpaEntity c GROUP BY c.status")
    fun countGroupByStatus(): List<Array<Any>>

    fun findByCustomerIdOrderByUpdatedAtDesc(customerId: UUID): List<CustomerChatJpaEntity>

    fun findByCustomerIdAndLbcId(customerId: UUID, lbcId: UUID): CustomerChatJpaEntity?
}
