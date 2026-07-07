package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.logistics.CreatePickupRequestRequest
import com.soiltech.backend.application.dto.logistics.PickupRequestDto
import com.soiltech.backend.application.dto.logistics.UpdatePickupRequestRequest
import com.soiltech.backend.application.usecase.logistics.*
import com.soiltech.backend.domain.enum.LogisticsStatus
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
@RequestMapping("/pickup-requests")
@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
class LogisticsController(
    private val createPickupRequestUseCase: CreatePickupRequestUseCase,
    private val listPickupRequestsUseCase: ListPickupRequestsUseCase,
    private val getPickupRequestUseCase: GetPickupRequestUseCase,
    private val updatePickupRequestUseCase: UpdatePickupRequestUseCase
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreatePickupRequestRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<PickupRequestDto>> {
        val data = createPickupRequestUseCase.execute(request, principal.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Pickup request created"))
    }

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(name = "farmer_id", required = false) farmerId: UUID?,
        @RequestParam(required = false) status: LogisticsStatus?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<PickupRequestDto>>> {
        val (requests, meta) = listPickupRequestsUseCase.execute(principal.id, farmerId, status, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(requests, meta = meta))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<PickupRequestDto>> {
        val data = getPickupRequestUseCase.execute(id, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdatePickupRequestRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<PickupRequestDto>> {
        val data = updatePickupRequestUseCase.execute(id, request, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Pickup request updated"))
    }
}
