package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.payment.CreatePaymentRecordRequest
import com.soiltech.backend.application.dto.payment.PaymentRecordDto
import com.soiltech.backend.application.usecase.payment.*
import com.soiltech.backend.domain.enum.PaymentStatus
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/payments")
@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
class PaymentController(
    private val createPaymentRecordUseCase: CreatePaymentRecordUseCase,
    private val listPaymentRecordsUseCase: ListPaymentRecordsUseCase,
    private val getPaymentRecordUseCase: GetPaymentRecordUseCase
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreatePaymentRecordRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<PaymentRecordDto>> {
        val data = createPaymentRecordUseCase.execute(request, principal.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Payment record created"))
    }

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(name = "farmer_id", required = false) farmerId: UUID?,
        @RequestParam(required = false) status: PaymentStatus?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<PaymentRecordDto>>> {
        val (payments, meta) = listPaymentRecordsUseCase.execute(principal.id, farmerId, status, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(payments, meta = meta))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<PaymentRecordDto>> {
        val data = getPaymentRecordUseCase.execute(id, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }
}
