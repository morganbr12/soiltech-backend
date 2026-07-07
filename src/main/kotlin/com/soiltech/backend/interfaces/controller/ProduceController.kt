package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.produce.CreateProduceRecordRequest
import com.soiltech.backend.application.dto.produce.ProduceRecordDto
import com.soiltech.backend.application.dto.produce.UpdateProduceRecordRequest
import com.soiltech.backend.application.usecase.produce.*
import com.soiltech.backend.domain.enum.CollectionStatus
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
@RequestMapping("/produce-records")
@PreAuthorize("hasRole('AGENT') or hasRole('ADMIN')")
class ProduceController(
    private val createProduceRecordUseCase: CreateProduceRecordUseCase,
    private val listProduceRecordsUseCase: ListProduceRecordsUseCase,
    private val getProduceRecordUseCase: GetProduceRecordUseCase,
    private val updateProduceRecordUseCase: UpdateProduceRecordUseCase
) {

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateProduceRecordRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<ProduceRecordDto>> {
        val data = createProduceRecordUseCase.execute(request, principal.id)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Produce record created"))
    }

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(name = "farmer_id", required = false) farmerId: UUID?,
        @RequestParam(required = false) status: CollectionStatus?,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<ProduceRecordDto>>> {
        val (records, meta) = listProduceRecordsUseCase.execute(principal.id, farmerId, status, page, perPage)
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
