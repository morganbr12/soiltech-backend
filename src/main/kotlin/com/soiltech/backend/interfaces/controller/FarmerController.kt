package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.farmer.CreateFarmerRequest
import com.soiltech.backend.application.dto.farmer.FarmerDto
import com.soiltech.backend.application.dto.farmer.UpdateFarmerRequest
import com.soiltech.backend.application.usecase.farmer.*
import com.soiltech.backend.domain.enum.FarmerStatus
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
@RequestMapping("/farmers")
@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
class FarmerController(
    private val createFarmerUseCase: CreateFarmerUseCase,
    private val getFarmerUseCase: GetFarmerUseCase,
    private val listFarmersUseCase: ListFarmersUseCase,
    private val updateFarmerUseCase: UpdateFarmerUseCase,
    private val deleteFarmerUseCase: DeleteFarmerUseCase
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateFarmerRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<FarmerDto>> {
        val data = createFarmerUseCase.execute(request, principal.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Farmer created successfully"))
    }

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(required = false) status: FarmerStatus?,
        @RequestParam(required = false) query: String?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<FarmerDto>>> {
        val (farmers, meta) = listFarmersUseCase.execute(principal.id, status, query, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(farmers, meta = meta))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<FarmerDto>> {
        val data = getFarmerUseCase.execute(id, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateFarmerRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<FarmerDto>> {
        val data = updateFarmerUseCase.execute(id, request, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Farmer updated"))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<Unit?>> {
        deleteFarmerUseCase.execute(id, principal.id)
        return ResponseEntity.ok(ApiResponse.success(null, "Farmer deleted"))
    }
}
