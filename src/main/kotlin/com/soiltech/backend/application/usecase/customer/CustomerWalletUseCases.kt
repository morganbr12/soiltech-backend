package com.soiltech.backend.application.usecase.customer

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.domain.entity.WalletTransaction
import com.soiltech.backend.domain.enum.WalletStatus
import com.soiltech.backend.domain.enum.WalletTransactionType
import com.soiltech.backend.domain.repository.CustomerWalletRepository
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class ListWalletsUseCase(private val walletRepository: CustomerWalletRepository) {
    fun execute(
        status: WalletStatus?,
        region: String?,
        search: String?,
        page: Int,
        limit: Int,
        sortBy: String,
        sortOrder: String
    ): Triple<List<WalletResponse>, WalletSummaryResponse, PaginationMeta> {
        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val sortField = when (sortBy) { "balance" -> "balance"; "totalDeposited" -> "totalDeposited"; else -> "createdAt" }
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100), Sort.by(direction, sortField))

        val resultPage = walletRepository.findAll(status, region, search, pageable)
        val summary = WalletSummaryResponse(
            totalWallets = resultPage.totalElements,
            totalBalance = walletRepository.totalBalance(),
            totalDeposited = walletRepository.totalDeposited(),
            totalWithdrawn = walletRepository.totalWithdrawn(),
            frozen = walletRepository.countByStatus(WalletStatus.FROZEN)
        )
        return Triple(resultPage.content.map { it.toResponse() }, summary, PaginationMeta.from(resultPage, page, limit))
    }
}

@Service
class ListWalletTransactionsUseCase(private val walletRepository: CustomerWalletRepository) {
    fun execute(walletId: UUID, page: Int, limit: Int): Pair<List<WalletTransactionResponse>, PaginationMeta> {
        walletRepository.findById(walletId) ?: throw NotFoundException("Wallet not found with id: $walletId")
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), limit.coerceIn(1, 100))
        val resultPage = walletRepository.findTransactions(walletId, pageable)
        return Pair(resultPage.content.map { it.toResponse() }, PaginationMeta.from(resultPage, page, limit))
    }
}

@Service
class TopUpWalletUseCase(private val walletRepository: CustomerWalletRepository) {
    @Transactional
    fun execute(walletId: UUID, request: TopUpWalletRequest): WalletResponse {
        val wallet = walletRepository.findById(walletId)
            ?: throw NotFoundException("Wallet not found with id: $walletId")
        if (wallet.status == WalletStatus.FROZEN)
            throw BadRequestException("Cannot top up a frozen wallet")

        val balanceBefore = wallet.balance
        val balanceAfter = balanceBefore.add(request.amount)
        val now = LocalDateTime.now()

        walletRepository.saveTransaction(
            WalletTransaction(
                id = UUID.randomUUID(),
                walletId = walletId,
                type = WalletTransactionType.DEPOSIT,
                amount = request.amount,
                balanceBefore = balanceBefore,
                balanceAfter = balanceAfter,
                description = request.description ?: "Admin top-up",
                reference = request.reference,
                createdAt = now
            )
        )

        val updated = wallet.copy(
            balance = balanceAfter,
            totalDeposited = wallet.totalDeposited.add(request.amount),
            lastTransaction = "Deposit",
            lastTransactionDate = now,
            updatedAt = now
        )
        return walletRepository.save(updated).toResponse()
    }
}

@Service
class FreezeWalletUseCase(private val walletRepository: CustomerWalletRepository) {
    @Transactional
    fun execute(walletId: UUID): WalletResponse {
        val wallet = walletRepository.findById(walletId)
            ?: throw NotFoundException("Wallet not found with id: $walletId")
        if (wallet.status == WalletStatus.FROZEN)
            throw BadRequestException("Wallet is already frozen")
        return walletRepository.save(wallet.copy(status = WalletStatus.FROZEN, updatedAt = LocalDateTime.now())).toResponse()
    }
}

@Service
class UnfreezeWalletUseCase(private val walletRepository: CustomerWalletRepository) {
    @Transactional
    fun execute(walletId: UUID): WalletResponse {
        val wallet = walletRepository.findById(walletId)
            ?: throw NotFoundException("Wallet not found with id: $walletId")
        if (wallet.status != WalletStatus.FROZEN)
            throw BadRequestException("Wallet is not frozen")
        return walletRepository.save(wallet.copy(status = WalletStatus.ACTIVE, updatedAt = LocalDateTime.now())).toResponse()
    }
}

// ── Mappers ────────────────────────────────────────────────────────────────────

private fun com.soiltech.backend.domain.entity.CustomerWallet.toResponse() = WalletResponse(
    id = id, customerId = customerId, customerName = customerName, customerCode = customerCode,
    balance = balance, pendingAmount = pendingAmount, totalDeposited = totalDeposited,
    totalWithdrawn = totalWithdrawn, lastTransaction = lastTransaction,
    lastTransactionDate = lastTransactionDate, status = status, region = region
)

private fun com.soiltech.backend.domain.entity.WalletTransaction.toResponse() = WalletTransactionResponse(
    id = id, walletId = walletId, type = type, amount = amount,
    balanceBefore = balanceBefore, balanceAfter = balanceAfter,
    description = description, reference = reference, createdAt = createdAt
)
