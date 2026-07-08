package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.CustomerWallet
import com.soiltech.backend.domain.entity.WalletTransaction
import com.soiltech.backend.domain.enum.WalletStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.util.UUID

interface CustomerWalletRepository {
    fun findById(id: UUID): CustomerWallet?
    fun findByCustomerId(customerId: UUID): CustomerWallet?
    fun findAll(status: WalletStatus?, region: String?, search: String?, pageable: Pageable): Page<CustomerWallet>
    fun save(wallet: CustomerWallet): CustomerWallet

    fun totalBalance(): BigDecimal
    fun totalDeposited(): BigDecimal
    fun totalWithdrawn(): BigDecimal
    fun countByStatus(status: WalletStatus): Long

    fun findTransactions(walletId: UUID, pageable: Pageable): Page<WalletTransaction>
    fun saveTransaction(tx: WalletTransaction): WalletTransaction
}
