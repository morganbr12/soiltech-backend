package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.produce.CreateProduceRecordRequest
import com.soiltech.backend.application.dto.produce.ProduceRecordDto
import com.soiltech.backend.application.dto.produce.UpdateProduceRecordRequest
import com.soiltech.backend.application.usecase.produce.*
import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.ProduceListingStatus
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
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/produce-records")
@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
class ProduceController(
    private val createProduceRecordUseCase: CreateProduceRecordUseCase,
    private val listProduceRecordsUseCase: ListProduceRecordsUseCase,
    private val getProduceRecordUseCase: GetProduceRecordUseCase,
    private val updateProduceRecordUseCase: UpdateProduceRecordUseCase,
    private val cloudinaryService: CloudinaryService
) {

    @PostMapping
    fun create(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam farmerId: UUID,
        @RequestParam(required = false) farmId: UUID?,
        @RequestParam cropType: String,
        @RequestParam(required = false) cropVariety: String?,
        @RequestParam(required = false) grade: String?,
        @RequestParam quantityKg: BigDecimal,
        @RequestParam pricePerKg: BigDecimal,
        @RequestParam(required = false) notes: String?,
        @RequestParam(required = false) collectedAt: LocalDateTime?,
        @RequestParam("photos", required = false) photos: List<MultipartFile>?
    ): ResponseEntity<ApiResponse<ProduceRecordDto>> {
        val photoUrls = photos?.filter { !it.isEmpty }
            ?.map { cloudinaryService.uploadImage(it, "soiltech/produce") } ?: emptyList()
        val request = CreateProduceRecordRequest(
            farmerId = farmerId, farmId = farmId, cropType = cropType,
            cropVariety = cropVariety, grade = grade, quantityKg = quantityKg,
            pricePerKg = pricePerKg, notes = notes, collectedAt = collectedAt
        )
        val data = createProduceRecordUseCase.execute(request, principal.id, photoUrls)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Produce record created"))
    }

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(name = "farmer_id", required = false) farmerId: UUID?,
        @RequestParam(required = false) status: String?,
        @RequestParam(name = "listing_status", required = false) listingStatusParam: String?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<ProduceRecordDto>>> {
        val collectionStatus = status?.let {
            runCatching { CollectionStatus.valueOf(it.uppercase()) }.getOrNull()
        }
        // Flutter sends status=APPROVED meaning the listing was approved (AVAILABLE)
        val listingStatus = listingStatusParam?.let {
            runCatching { ProduceListingStatus.valueOf(it.uppercase()) }.getOrNull()
        } ?: if (status?.uppercase() == "APPROVED") ProduceListingStatus.AVAILABLE else null

        val (records, meta) = listProduceRecordsUseCase.execute(principal.id, farmerId, collectionStatus, listingStatus, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(records, meta = meta))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ProduceRecordDto>> {
        val data = getProduceRecordUseCase.execute(id, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateProduceRecordRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ProduceRecordDto>> {
        val data = updateProduceRecordUseCase.execute(id, request, principal.id)
        return ResponseEntity.ok(ApiResponse.success(data, "Produce record updated"))
    }
}
