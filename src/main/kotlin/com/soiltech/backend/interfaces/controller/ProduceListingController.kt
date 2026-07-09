package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.produce.ProduceListingDto
import com.soiltech.backend.application.usecase.produce.ApproveProduceListingUseCase
import com.soiltech.backend.application.usecase.produce.GetProduceListingUseCase
import com.soiltech.backend.application.usecase.produce.GetProduceListingsUseCase
import com.soiltech.backend.application.usecase.produce.ListProduceListingsAdminUseCase
import com.soiltech.backend.application.usecase.produce.RejectProduceListingUseCase
import com.soiltech.backend.domain.enum.ProduceListingStatus
import com.soiltech.backend.interfaces.response.ApiResponse
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.UUID

@RestController
@RequestMapping("/produce-listings")
@PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN', 'LBC')")
class ProduceListingController(
    private val getListingsUseCase: GetProduceListingsUseCase,
    private val getListingUseCase: GetProduceListingUseCase,
    private val listAdminUseCase: ListProduceListingsAdminUseCase,
    private val approveListingUseCase: ApproveProduceListingUseCase,
    private val rejectListingUseCase: RejectProduceListingUseCase
) {

    @GetMapping
    fun getListings(
        @RequestParam(required = false) cropType: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) district: String?,
        @RequestParam(required = false) lbcId: UUID?,
        @RequestParam(required = false) grade: String?,
        @RequestParam(required = false) minPrice: BigDecimal?,
        @RequestParam(required = false) maxPrice: BigDecimal?,
        @RequestParam(required = false) minQuantity: BigDecimal?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ApiResponse<List<ProduceListingDto>>> {
        val (data, meta) = getListingsUseCase.execute(
            cropType, region, district, lbcId, grade,
            minPrice, maxPrice, minQuantity, page, limit
        )
        return ResponseEntity.ok(ApiResponse.success(data = data, meta = meta))
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    fun listAdmin(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) cropType: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ApiResponse<List<ProduceListingDto>>> {
        val statusEnum = status?.takeIf { it.isNotBlank() }
            ?.let { runCatching { ProduceListingStatus.valueOf(it.uppercase()) }.getOrNull() }
        val (data, meta) = listAdminUseCase.execute(statusEnum, cropType, region, search, page, limit)
        return ResponseEntity.ok(ApiResponse.success(data = data, meta = meta))
    }

    @GetMapping("/{id}")
    fun getListing(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ProduceListingDto>> =
        ResponseEntity.ok(ApiResponse.success(getListingUseCase.execute(id)))

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    fun approve(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ProduceListingDto>> =
        ResponseEntity.ok(ApiResponse.success(approveListingUseCase.execute(id), "Produce listing approved"))

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    fun reject(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<ProduceListingDto>> =
        ResponseEntity.ok(ApiResponse.success(rejectListingUseCase.execute(id), "Produce listing rejected"))
}
