package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.WalletTransaction
import com.soiltech.backend.domain.enum.WalletTransactionType
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "wallet_transactions",
    indexes = [Index(name = "idx_wallet_txn_wallet_id", columnList = "walletId")]
)
class WalletTransactionJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val walletId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    val type: WalletTransactionType,

    @Column(nullable = false, precision = 16, scale = 2)
    val amount: BigDecimal,

    @Column(nullable = false, precision = 16, scale = 2)
    val balanceBefore: BigDecimal,

    @Column(nullable = false, precision = 16, scale = 2)
    val balanceAfter: BigDecimal,

    @Column(length = 500)
    val description: String? = null,

    @Column(length = 255)
    val reference: String? = null,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()

) {
    fun toDomain(): WalletTransaction = WalletTransaction(
        id = id!!,
        walletId = walletId,
        type = type,
        amount = amount,
        balanceBefore = balanceBefore,
        balanceAfter = balanceAfter,
        description = description,
        reference = reference,
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(tx: WalletTransaction): WalletTransactionJpaEntity = WalletTransactionJpaEntity(
            id = tx.id,
            walletId = tx.walletId,
            type = tx.type,
            amount = tx.amount,
            balanceBefore = tx.balanceBefore,
            balanceAfter = tx.balanceAfter,
            description = tx.description,
            reference = tx.reference,
            createdAt = tx.createdAt
        )
    }
}
