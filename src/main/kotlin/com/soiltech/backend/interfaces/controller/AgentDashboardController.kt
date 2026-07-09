package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.agent.*
import com.soiltech.backend.application.dto.farm.FarmDto
import com.soiltech.backend.application.dto.farmer.FarmerResponse
import com.soiltech.backend.application.dto.produce.CreateProduceRecordRequest
import com.soiltech.backend.application.dto.produce.ProduceRecordDto
import com.soiltech.backend.application.usecase.agent.*
import com.soiltech.backend.application.usecase.produce.CreateProduceRecordUseCase
import com.soiltech.backend.application.usecase.produce.ListProduceRecordsUseCase
import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.infrastructure.service.CloudinaryService
import com.soiltech.backend.interfaces.response.ApiResponse
import com.soiltech.backend.interfaces.response.PaginationMeta
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/agent")
@PreAuthorize("hasRole('AGENT')")
class AgentDashboardController(
    private val dashboardUseCase: AgentDashboardUseCase,
    private val profileUseCase: AgentMobileProfileUseCase,
    private val activitiesUseCase: AgentActivitiesUseCase,
    private val getFarmersUseCase: GetAgentFarmersUseCase,
    private val getFarmerUseCase: GetAgentFarmerUseCase,
    private val registerFarmerUseCase: RegisterFarmerByAgentUseCase,
    private val registerFarmUseCase: RegisterFarmByAgentUseCase,
    private val createProduceRecordUseCase: CreateProduceRecordUseCase,
    private val listProduceRecordsUseCase: ListProduceRecordsUseCase,
    private val cloudinaryService: CloudinaryService
) {

    @GetMapping("/dashboard")
    fun getDashboard(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<AgentDashboardResponse>> =
        ResponseEntity.ok(ApiResponse.success(dashboardUseCase.execute(principal.id)))

    @GetMapping("/profile")
    fun getProfile(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<AgentMobileProfileResponse>> =
        ResponseEntity.ok(ApiResponse.success(profileUseCase.execute(principal.id)))

    @GetMapping("/activities")
    fun getActivities(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<ApiResponse<List<AgentActivityResponse>>> =
        ResponseEntity.ok(ApiResponse.success(activitiesUseCase.execute(principal.id, limit)))

    @PostMapping("/farms")
    fun registerFarm(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam("farmerId") farmerId: UUID,
        @RequestParam("name") name: String,
        @RequestParam(required = false) sizeHectares: Double?,
        @RequestParam(required = false) cropType: String?,
        @RequestParam(required = false) location: String?,
        @RequestParam(required = false) latitude: Double?,
        @RequestParam(required = false) longitude: Double?,
        @RequestParam("photos", required = false) photos: List<MultipartFile>?
    ): ResponseEntity<ApiResponse<FarmDto>> {
        val photoUrls = photos
            ?.filter { !it.isEmpty }
            ?.map { cloudinaryService.uploadImage(it, "soiltech/farms") }
            ?: emptyList()
        val request = RegisterFarmByAgentRequest(
            farmerId = farmerId,
            name = name,
            sizeHectares = sizeHectares,
            cropType = cropType,
            location = location,
            latitude = latitude,
            longitude = longitude
        )
        val data = registerFarmUseCase.execute(principal.id, request, photoUrls)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Farm registered successfully"))
    }

    @PostMapping("/produce-records")
    fun createProduceRecord(
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

    @GetMapping("/produce-records")
    fun listProduceRecords(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(name = "farmer_id", required = false) farmerId: UUID?,
        @RequestParam(required = false) status: CollectionStatus?
    ): ResponseEntity<ApiResponse<List<ProduceRecordDto>>> {
        val (records, meta) = listProduceRecordsUseCase.execute(principal.id, farmerId, status, page, perPage)
        return ResponseEntity.ok(ApiResponse.success(records, meta = meta))
    }

    @PostMapping("/farmers")
    fun registerFarmer(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: RegisterFarmerByAgentRequest
    ): ResponseEntity<ApiResponse<FarmerResponse>> {
        val data = registerFarmerUseCase.execute(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Farmer registered successfully"))
    }

    @GetMapping("/farmers/{id}")
    fun getFarmer(
        @AuthenticationPrincipal principal: UserPrincipal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<FarmerResponse>> =
        ResponseEntity.ok(ApiResponse.success(getFarmerUseCase.execute(principal.id, id)))

    @GetMapping("/farmers")
    fun getFarmers(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam(required = false) search: String?,
        @RequestParam(required = false) status: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ApiResponse<List<AgentFarmerSummaryResponse>>> {
        val result = getFarmersUseCase.execute(principal.id, search, status, page, limit)
        return ResponseEntity.ok(
            ApiResponse.success(
                data = result.content,
                meta = PaginationMeta.from(result, page, limit)
            )
        )
    }
}
