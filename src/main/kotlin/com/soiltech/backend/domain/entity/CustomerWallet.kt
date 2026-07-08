package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.WalletStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class CustomerWallet(
    val id: UUID,
    val customerId: UUID,
    val customerCode: String,
    val customerName: String,
    val balance: BigDecimal,
    val pendingAmount: BigDecimal,
    val totalDeposited: BigDecimal,
    val totalWithdrawn: BigDecimal,
    val lastTransaction: String?,
    val lastTransactionDate: LocalDateTime?,
    val status: WalletStatus,
    val region: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
