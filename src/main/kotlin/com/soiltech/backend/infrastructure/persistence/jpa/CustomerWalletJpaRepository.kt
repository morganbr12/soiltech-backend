package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.WalletStatus
import com.soiltech.backend.infrastructure.persistence.entity.CustomerWalletJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
interface CustomerWalletJpaRepository :
    JpaRepository<CustomerWalletJpaEntity, UUID>,
    JpaSpecificationExecutor<CustomerWalletJpaEntity> {

    fun findByCustomerId(customerId: UUID): CustomerWalletJpaEntity?

    fun countByStatus(status: WalletStatus): Long

    @Query("SELECT COALESCE(SUM(w.balance), 0) FROM CustomerWalletJpaEntity w")
    fun sumTotalBalance(): BigDecimal

    @Query("SELECT COALESCE(SUM(w.totalDeposited), 0) FROM CustomerWalletJpaEntity w")
    fun sumTotalDeposited(): BigDecimal

    @Query("SELECT COALESCE(SUM(w.totalWithdrawn), 0) FROM CustomerWalletJpaEntity w")
    fun sumTotalWithdrawn(): BigDecimal

    @Query("SELECT w.customerId, w.balance FROM CustomerWalletJpaEntity w WHERE w.customerId IN :ids")
    fun findBalancesByCustomerIds(ids: List<UUID>): List<Array<Any>>
}
