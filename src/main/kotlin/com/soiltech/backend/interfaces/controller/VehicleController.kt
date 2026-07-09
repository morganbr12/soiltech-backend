package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.logistics.CreateVehicleRequest
import com.soiltech.backend.application.dto.logistics.UpdateVehicleRequest
import com.soiltech.backend.application.dto.logistics.VehicleDto
import com.soiltech.backend.application.dto.logistics.VehicleKpisDto
import com.soiltech.backend.application.usecase.logistics.*
import com.soiltech.backend.domain.enum.VehicleStatus
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/vehicles")
@PreAuthorize("hasRole('ADMIN')")
class VehicleController(
    private val createVehicleUseCase: CreateVehicleUseCase,
    private val listVehiclesUseCase: ListVehiclesUseCase,
    private val getVehicleUseCase: GetVehicleUseCase,
    private val updateVehicleUseCase: UpdateVehicleUseCase,
    private val deleteVehicleUseCase: DeleteVehicleUseCase,
    private val getVehicleKpisUseCase: GetVehicleKpisUseCase
) {

    @GetMapping("/kpis")
    fun kpis(): ResponseEntity<ApiResponse<VehicleKpisDto>> =
        ResponseEntity.ok(ApiResponse.success(getVehicleKpisUseCase.execute()))

    @PostMapping
    fun create(
        @Valid @RequestBody request: CreateVehicleRequest
    ): ResponseEntity<ApiResponse<VehicleDto>> {
        val data = createVehicleUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Vehicle added successfully"))
    }

    @GetMapping
    fun list(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) vehicleType: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ApiResponse<List<VehicleDto>>> {
        val statusEnum = status?.takeIf { it.isNotBlank() }
            ?.let { runCatching { VehicleStatus.valueOf(it.uppercase()) }.getOrNull() }
        val (data, meta) = listVehiclesUseCase.execute(statusEnum, region, vehicleType, search, page, limit)
        return ResponseEntity.ok(ApiResponse.success(data, meta = meta))
    }

    @GetMapping("/{id}")
    fun getById(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<VehicleDto>> =
        ResponseEntity.ok(ApiResponse.success(getVehicleUseCase.execute(id)))

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateVehicleRequest
    ): ResponseEntity<ApiResponse<VehicleDto>> {
        val data = updateVehicleUseCase.execute(id, request)
        return ResponseEntity.ok(ApiResponse.success(data, "Vehicle updated successfully"))
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<Unit?>> {
        deleteVehicleUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(null, "Vehicle deleted successfully"))
    }
}
