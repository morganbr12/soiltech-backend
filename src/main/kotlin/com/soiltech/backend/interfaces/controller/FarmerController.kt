package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.farmer.*
import com.soiltech.backend.application.usecase.farmer.*
import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/farmers")
class FarmerController(
    private val listFarmersUseCase: ListFarmersUseCase,
    private val getFarmerUseCase: GetFarmerUseCase,
    private val registerFarmerUseCase: RegisterFarmerUseCase,
    private val updateFarmerUseCase: UpdateFarmerUseCase,
    private val approveFarmerUseCase: ApproveFarmerUseCase,
    private val rejectFarmerUseCase: RejectFarmerUseCase,
    private val deleteFarmerUseCase: DeleteFarmerUseCase
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('farmers:view')")
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) lbcId: UUID?,
        @RequestParam(required = false) agentId: UUID?,
        @RequestParam(required = false) kycVerified: Boolean?,
        @RequestParam(required = false) search: String?,
        @RequestParam(name = "sort_by", defaultValue = "createdAt") sortBy: String,
        @RequestParam(name = "sort_order", defaultValue = "desc") sortOrder: String
    ): ResponseEntity<ApiResponse<List<FarmerResponse>>> {
        val farmerStatus = status?.let { FarmerStatus.fromValue(it) }
        val (items, summary, meta) = listFarmersUseCase.execute(
            farmerStatus, region, lbcId, agentId, kycVerified, search, page, perPage, sortBy, sortOrder
        )
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta, summary = summary))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('farmers:view')")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<FarmerResponse>> {
        val data = getFarmerUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('farmers:create')")
    fun register(
        @Valid @RequestBody request: RegisterFarmerRequest
    ): ResponseEntity<ApiResponse<FarmerResponse>> {
        val data = registerFarmerUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Farmer registered successfully"))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('farmers:edit')")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateFarmerRequest
    ): ResponseEntity<ApiResponse<FarmerResponse>> {
        val data = updateFarmerUseCase.execute(id, request)
        return ResponseEntity.ok(ApiResponse.success(data, "Farmer updated successfully"))
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('farmers:approve')")
    fun approve(@PathVariable id: UUID): ResponseEntity<ApiResponse<FarmerResponse>> {
        val data = approveFarmerUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(data, "Farmer approved successfully"))
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('farmers:approve')")
    fun reject(
        @PathVariable id: UUID,
        @RequestBody(required = false) request: RejectFarmerRequest?
    ): ResponseEntity<ApiResponse<FarmerResponse>> {
        val data = rejectFarmerUseCase.execute(id, request ?: RejectFarmerRequest())
        return ResponseEntity.ok(ApiResponse.success(data, "Farmer rejected"))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('farmers:delete')")
    fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit?>> {
        deleteFarmerUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(null, "Farmer deleted successfully"))
    }
}
