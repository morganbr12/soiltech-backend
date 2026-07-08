package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.CustomerWallet
import com.soiltech.backend.domain.entity.WalletTransaction
import com.soiltech.backend.domain.enum.WalletStatus
import com.soiltech.backend.domain.repository.CustomerWalletRepository
import com.soiltech.backend.infrastructure.persistence.entity.CustomerWalletJpaEntity
import com.soiltech.backend.infrastructure.persistence.entity.WalletTransactionJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerWalletJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.WalletTransactionJpaRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class CustomerWalletRepositoryAdapter(
    private val walletJpa: CustomerWalletJpaRepository,
    private val txJpa: WalletTransactionJpaRepository
) : CustomerWalletRepository {

    override fun findById(id: UUID): CustomerWallet? =
        walletJpa.findById(id).orElse(null)?.toDomain()

    override fun findByCustomerId(customerId: UUID): CustomerWallet? =
        walletJpa.findByCustomerId(customerId)?.toDomain()

    override fun findAll(status: WalletStatus?, region: String?, search: String?, pageable: Pageable): Page<CustomerWallet> {
        val spec = Specification<CustomerWalletJpaEntity> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            status?.let { predicates.add(cb.equal(root.get<WalletStatus>("status"), it)) }
            region?.let { predicates.add(cb.equal(cb.lower(root.get("region")), it.lowercase())) }
            search?.let { q ->
                val like = "%${q.lowercase()}%"
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("customerName")), like),
                    cb.like(cb.lower(root.get("customerCode")), like)
                ))
            }
            cb.and(*predicates.toTypedArray())
        }
        return walletJpa.findAll(spec, pageable).map { it.toDomain() }
    }

    override fun save(wallet: CustomerWallet): CustomerWallet {
        val existing = walletJpa.findById(wallet.id).orElse(null)
        return if (existing != null) {
            existing.apply {
                balance = wallet.balance
                pendingAmount = wallet.pendingAmount
                totalDeposited = wallet.totalDeposited
                totalWithdrawn = wallet.totalWithdrawn
                lastTransaction = wallet.lastTransaction
                lastTransactionDate = wallet.lastTransactionDate
                status = wallet.status
            }
            walletJpa.save(existing).toDomain()
        } else {
            walletJpa.save(CustomerWalletJpaEntity.fromDomain(wallet)).toDomain()
        }
    }

    override fun totalBalance(): BigDecimal = walletJpa.sumTotalBalance()
    override fun totalDeposited(): BigDecimal = walletJpa.sumTotalDeposited()
    override fun totalWithdrawn(): BigDecimal = walletJpa.sumTotalWithdrawn()
    override fun countByStatus(status: WalletStatus): Long = walletJpa.countByStatus(status)

    override fun findTransactions(walletId: UUID, pageable: Pageable): Page<WalletTransaction> =
        txJpa.findByWalletIdOrderByCreatedAtDesc(walletId, pageable).map { it.toDomain() }

    override fun saveTransaction(tx: WalletTransaction): WalletTransaction =
        txJpa.save(WalletTransactionJpaEntity.fromDomain(tx)).toDomain()
}
