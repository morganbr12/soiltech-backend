package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.CustomerWallet
import com.soiltech.backend.domain.enum.WalletStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "customer_wallets",
    indexes = [
        Index(name = "idx_wallets_customer_id", columnList = "customerId", unique = true),
        Index(name = "idx_wallets_status", columnList = "status")
    ]
)
class CustomerWalletJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val customerId: UUID,

    @Column(nullable = false, length = 20)
    var customerCode: String,

    @Column(nullable = false, length = 255)
    var customerName: String,

    @Column(nullable = false, precision = 16, scale = 2)
    var balance: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 16, scale = 2)
    var pendingAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 16, scale = 2)
    var totalDeposited: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 16, scale = 2)
    var totalWithdrawn: BigDecimal = BigDecimal.ZERO,

    @Column(length = 500)
    var lastTransaction: String? = null,

    var lastTransactionDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var status: WalletStatus = WalletStatus.ACTIVE,

    @Column(length = 100)
    var region: String? = null

) : BaseJpaEntity() {

    fun toDomain(): CustomerWallet = CustomerWallet(
        id = id!!,
        customerId = customerId,
        customerCode = customerCode,
        customerName = customerName,
        balance = balance,
        pendingAmount = pendingAmount,
        totalDeposited = totalDeposited,
        totalWithdrawn = totalWithdrawn,
        lastTransaction = lastTransaction,
        lastTransactionDate = lastTransactionDate,
        status = status,
        region = region,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(w: CustomerWallet): CustomerWalletJpaEntity = CustomerWalletJpaEntity(
            id = w.id,
            customerId = w.customerId,
            customerCode = w.customerCode,
            customerName = w.customerName,
            balance = w.balance,
            pendingAmount = w.pendingAmount,
            totalDeposited = w.totalDeposited,
            totalWithdrawn = w.totalWithdrawn,
            lastTransaction = w.lastTransaction,
            lastTransactionDate = w.lastTransactionDate,
            status = w.status,
            region = w.region
        )
    }
}
