package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.WalletTransactionJpaEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface WalletTransactionJpaRepository : JpaRepository<WalletTransactionJpaEntity, UUID> {
    fun findByWalletIdOrderByCreatedAtDesc(walletId: UUID, pageable: Pageable): Page<WalletTransactionJpaEntity>
}
