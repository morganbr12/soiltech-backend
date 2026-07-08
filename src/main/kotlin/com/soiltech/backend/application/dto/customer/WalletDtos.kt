package com.soiltech.backend.application.dto.customer

import com.soiltech.backend.domain.enum.WalletStatus
import com.soiltech.backend.domain.enum.WalletTransactionType
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class WalletResponse(
    val id: UUID,
    val customerId: UUID,
    val customerName: String,
    val customerCode: String,
    val balance: BigDecimal,
    val pendingAmount: BigDecimal,
    val totalDeposited: BigDecimal,
    val totalWithdrawn: BigDecimal,
    val lastTransaction: String?,
    val lastTransactionDate: LocalDateTime?,
    val status: WalletStatus,
    val region: String?
)

data class WalletSummaryResponse(
    val totalWallets: Long,
    val totalBalance: BigDecimal,
    val totalDeposited: BigDecimal,
    val totalWithdrawn: BigDecimal,
    val frozen: Long
)

data class WalletTransactionResponse(
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

data class TopUpWalletRequest(
    @field:NotNull
    @field:DecimalMin("0.01", message = "Amount must be greater than zero")
    val amount: BigDecimal,
    val description: String? = null,
    val reference: String? = null
)
