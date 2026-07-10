package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.farm.AdminFarmListDto
import com.soiltech.backend.application.dto.farm.CreateFarmRequest
import com.soiltech.backend.application.dto.farm.FarmDto
import com.soiltech.backend.application.dto.farm.UpdateFarmRequest
import com.soiltech.backend.application.usecase.farm.*
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.infrastructure.service.CloudinaryService
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/farmers/{farmerId}/farms")
@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
class FarmController(
    private val createFarmUseCase: CreateFarmUseCase,
    private val listFarmsUseCase: ListFarmsUseCase,
    private val updateFarmUseCase: UpdateFarmUseCase,
    private val cloudinaryService: CloudinaryService
) {

    @PostMapping(consumes = ["application/json"])
    fun create(
        @PathVariable farmerId: UUID,
        @Valid @RequestBody request: CreateFarmRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<FarmDto>> {
        val data = createFarmUseCase.execute(farmerId, request, principal.id, emptyList())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Farm created successfully"))
    }

    @PostMapping(consumes = ["multipart/form-data"])
    fun createWithPhotos(
        @PathVariable farmerId: UUID,
        @RequestParam name: String,
        @RequestParam(required = false) sizeHectares: Double?,
        @RequestParam(required = false) cropType: String?,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?,
        @RequestParam(required = false) estimatedYieldKg: Double?,
        @RequestParam(required = false) lastHarvestDate: LocalDate?,
        @RequestParam("photos", required = false) photos: List<MultipartFile>?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<FarmDto>> {
        val photoUrls = photos?.filter { !it.isEmpty }
            ?.map { cloudinaryService.uploadImage(it, "soiltech/farms") } ?: emptyList()
        val request = CreateFarmRequest(
            name = name,
            sizeHectares = sizeHectares,
            cropType = cropType,
            location = location,
            latitude = latitude,
            longitude = longitude,
            estimatedYieldKg = estimatedYieldKg,
            lastHarvestDate = lastHarvestDate
        )
        val data = createFarmUseCase.execute(farmerId, request, principal.id, photoUrls)
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
