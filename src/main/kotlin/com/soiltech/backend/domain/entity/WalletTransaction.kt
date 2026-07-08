package com.soiltech.backend.domain.entity

import com.soiltech.backend.domain.enum.WalletTransactionType
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class WalletTransaction(
    val id: UUID,
    val walletId: UUID,
    val type: WalletTransactionType,
    val amount: BigDecimal,
    val balanceBefore: BigDecimal,
    val balanceAfter: BigDecimal,
    val description: String?,
    val reference: String?,
    val createdAt: LocalDateTime
)
