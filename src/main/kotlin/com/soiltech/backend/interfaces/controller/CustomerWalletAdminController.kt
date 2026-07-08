package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.usecase.customer.*
import com.soiltech.backend.domain.enum.WalletStatus
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/customers/wallets")
class CustomerWalletAdminController(
    private val listWalletsUseCase: ListWalletsUseCase,
    private val listWalletTransactionsUseCase: ListWalletTransactionsUseCase,
    private val topUpWalletUseCase: TopUpWalletUseCase,
    private val freezeWalletUseCase: FreezeWalletUseCase,
    private val unfreezeWalletUseCase: UnfreezeWalletUseCase
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:wallet')")
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(name = "sortBy", defaultValue = "createdAt") sortBy: String,
        @RequestParam(name = "sortOrder", defaultValue = "desc") sortOrder: String
    ): ResponseEntity<ApiResponse<List<WalletResponse>>> {
        val walletStatus = status?.let { WalletStatus.fromValue(it) }
        val (items, summary, meta) = listWalletsUseCase.execute(walletStatus, region, search, page, limit, sortBy, sortOrder)
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta, summary = summary))
    }

    @GetMapping("/{walletId}/transactions")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:wallet')")
    fun transactions(
        @PathVariable walletId: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ApiResponse<List<WalletTransactionResponse>>> {
        val (items, meta) = listWalletTransactionsUseCase.execute(walletId, page, limit)
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta))
    }

    @PostMapping("/{walletId}/topup")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:wallet')")
    fun topUp(
        @PathVariable walletId: UUID,
        @Valid @RequestBody request: TopUpWalletRequest
    ): ResponseEntity<ApiResponse<WalletResponse>> {
        val data = topUpWalletUseCase.execute(walletId, request)
        return ResponseEntity.ok(ApiResponse.success(data, "Wallet topped up successfully"))
    }

    @PatchMapping("/{walletId}/freeze")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:wallet')")
    fun freeze(@PathVariable walletId: UUID): ResponseEntity<ApiResponse<WalletResponse>> {
        val data = freezeWalletUseCase.execute(walletId)
        return ResponseEntity.ok(ApiResponse.success(data, "Wallet frozen"))
    }

    @PatchMapping("/{walletId}/unfreeze")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:wallet')")
    fun unfreeze(@PathVariable walletId: UUID): ResponseEntity<ApiResponse<WalletResponse>> {
        val data = unfreezeWalletUseCase.execute(walletId)
        return ResponseEntity.ok(ApiResponse.success(data, "Wallet unfrozen"))
    }
}
