package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.logistics.DriverDispatchDto
import com.soiltech.backend.application.dto.logistics.UpdateDispatchStatusRequest
import com.soiltech.backend.application.usecase.logistics.ListDriverDispatchesUseCase
import com.soiltech.backend.application.usecase.logistics.UpdateDispatchStatusUseCase
import com.soiltech.backend.domain.enum.DispatchStatus
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/dispatches")
@PreAuthorize("hasRole('ADMIN')")
class DriverDispatchController(
    private val listDriverDispatchesUseCase: ListDriverDispatchesUseCase,
    private val updateDispatchStatusUseCase: UpdateDispatchStatusUseCase
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ApiResponse<List<DriverDispatchDto>>> {
        val statusEnum = status?.takeIf { it.isNotBlank() }
            ?.let { runCatching { DispatchStatus.fromValue(it) }.getOrNull() }
        val (data, meta) = listDriverDispatchesUseCase.execute(statusEnum, page, limit)
        return ResponseEntity.ok(ApiResponse.success(data, meta = meta))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateDispatchStatusRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<DriverDispatchDto>> {
        val data = updateDispatchStatusUseCase.execute(id, request, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Dispatch status updated"))
    }
}
