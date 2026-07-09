package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.farm.AdminFarmListDto
import com.soiltech.backend.application.dto.farm.CreateFarmRequest
import com.soiltech.backend.application.dto.farm.FarmDto
import com.soiltech.backend.application.dto.farm.UpdateFarmRequest
import com.soiltech.backend.application.usecase.farm.*
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
@RequestMapping("/farmers/{farmerId}/farms")
@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
class FarmController(
    private val createFarmUseCase: CreateFarmUseCase,
    private val listFarmsUseCase: ListFarmsUseCase,
    private val updateFarmUseCase: UpdateFarmUseCase
) {

    @PostMapping
    fun create(
        @PathVariable farmerId: UUID,
        @Valid @RequestBody request: CreateFarmRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<FarmDto>> {
        val data = createFarmUseCase.execute(farmerId, request, principal.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Farm created successfully"))
    }

    @GetMapping
    fun list(
        @PathVariable farmerId: UUID,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<FarmDto>>> {
        val (farms, meta) = listFarmsUseCase.execute(farmerId, principal.id, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(farms, meta = meta))
    }

    @PutMapping("/{farmId}")
    fun update(
        @PathVariable farmerId: UUID,
        @PathVariable farmId: UUID,
        @Valid @RequestBody request: UpdateFarmRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<FarmDto>> {
        val data = updateFarmUseCase.execute(farmerId, farmId, request, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Farm updated"))
    }
}

@RestController
@RequestMapping("/admin/farms")
@PreAuthorize("hasRole('ADMIN')")
class AdminFarmController(
    private val listFarmsAdminUseCase: ListFarmsAdminUseCase
) {

    @GetMapping
    fun list(
        @RequestParam(required = false) region: String?,
        @RequestParam(name = "crop_type", required = false) cropType: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int
    ): ResponseEntity<ApiResponse<List<AdminFarmListDto>>> {
        val (farms, meta) = listFarmsAdminUseCase.execute(region, cropType, search, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(farms, meta = meta))
    }
}
