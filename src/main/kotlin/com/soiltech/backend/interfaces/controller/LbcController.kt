package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.lbc.*
import com.soiltech.backend.application.usecase.lbc.*
import com.soiltech.backend.domain.enum.LbcStatus
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/lbc")
class LbcController(
    private val listLbcsUseCase: ListLbcsUseCase,
    private val getLbcUseCase: GetLbcUseCase,
    private val createLbcUseCase: CreateLbcUseCase,
    private val updateLbcUseCase: UpdateLbcUseCase,
    private val suspendLbcUseCase: SuspendLbcUseCase,
    private val deleteLbcUseCase: DeleteLbcUseCase,
    private val exportLbcsUseCase: ExportLbcsUseCase,
    private val bulkSuspendLbcsUseCase: BulkSuspendLbcsUseCase
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('lbc:view')")
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(name = "sort_by", defaultValue = "createdAt") sortBy: String,
        @RequestParam(name = "sort_order", defaultValue = "desc") sortOrder: String
    ): ResponseEntity<ApiResponse<List<LbcResponse>>> {
        val lbcStatus = status?.let { LbcStatus.fromValue(it) }
        val (items, summary, meta) = listLbcsUseCase.execute(lbcStatus, region, search, page, perPage, sortBy, sortOrder)
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta, summary = summary))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('lbc:view')")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<LbcResponse>> {
        val data = getLbcUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('lbc:create')")
    fun create(
        @Valid @RequestBody request: CreateLbcRequest
    ): ResponseEntity<ApiResponse<LbcResponse>> {
        val data = createLbcUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "LBC registered successfully"))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('lbc:edit')")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateLbcRequest
    ): ResponseEntity<ApiResponse<LbcResponse>> {
        val data = updateLbcUseCase.execute(id, request)
        return ResponseEntity.ok(ApiResponse.success(data, "LBC updated successfully"))
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('lbc:suspend')")
    fun suspend(@PathVariable id: UUID): ResponseEntity<ApiResponse<LbcResponse>> {
        val data = suspendLbcUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(data, "LBC suspended successfully"))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('lbc:delete')")
    fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit?>> {
        deleteLbcUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(null, "LBC deleted successfully"))
    }

    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('lbc:view')")
    fun export(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(name = "ids", required = false) ids: List<UUID>?
    ): ResponseEntity<ByteArray> {
        val csvBytes = exportLbcsUseCase.execute(status?.let { LbcStatus.fromValue(it) }, region, ids)
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"lbcs-export.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csvBytes)
    }

    @PatchMapping("/bulk-suspend")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('lbc:suspend')")
    fun bulkSuspend(
        @Valid @RequestBody request: BulkSuspendRequest
    ): ResponseEntity<ApiResponse<BulkSuspendResponse>> {
        val data = bulkSuspendLbcsUseCase.execute(request)
        return ResponseEntity.ok(ApiResponse.success(data, "Bulk suspend completed"))
    }
}
