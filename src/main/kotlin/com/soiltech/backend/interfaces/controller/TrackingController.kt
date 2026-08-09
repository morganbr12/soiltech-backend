package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.tracking.TrackingAgentDto
import com.soiltech.backend.application.dto.tracking.TrackingVehicleDto
import com.soiltech.backend.application.dto.tracking.TrackingVehiclesResponse
import com.soiltech.backend.application.dto.tracking.UpdateVehiclePositionRequest
import com.soiltech.backend.application.dto.tracking.VehicleLocationHistoryDto
import com.soiltech.backend.application.usecase.tracking.GetTrackingAgentsUseCase
import com.soiltech.backend.application.usecase.tracking.GetTrackingVehiclesUseCase
import com.soiltech.backend.application.usecase.tracking.GetVehicleLocationHistoryUseCase
import com.soiltech.backend.application.usecase.tracking.UpdateVehiclePositionUseCase
import com.soiltech.backend.infrastructure.service.TrackingService
import com.soiltech.backend.interfaces.response.ApiResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.time.LocalDateTime
import java.util.UUID

@RestController
@RequestMapping("/tracking")
@PreAuthorize("hasRole('ADMIN')")
class TrackingController(
    private val getTrackingVehiclesUseCase: GetTrackingVehiclesUseCase,
    private val updateVehiclePositionUseCase: UpdateVehiclePositionUseCase,
    private val getVehicleLocationHistoryUseCase: GetVehicleLocationHistoryUseCase,
    private val getTrackingAgentsUseCase: GetTrackingAgentsUseCase,
    private val trackingService: TrackingService
) {

    @GetMapping("/vehicles")
    fun getVehicles(): ResponseEntity<ApiResponse<TrackingVehiclesResponse>> {
        val data = getTrackingVehiclesUseCase.execute()
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @GetMapping("/events", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun streamEvents(): SseEmitter = trackingService.subscribe()

    @GetMapping("/vehicles/{id}/history")
    fun getVehicleHistory(
        @PathVariable id: UUID,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: LocalDateTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: LocalDateTime
    ): ResponseEntity<ApiResponse<List<VehicleLocationHistoryDto>>> {
        val data = getVehicleLocationHistoryUseCase.execute(id, from, to)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @GetMapping("/agents")
    fun getAgents(): ResponseEntity<ApiResponse<List<TrackingAgentDto>>> {
        val data = getTrackingAgentsUseCase.execute()
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PutMapping("/vehicles/{id}/position")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    fun updatePosition(
        @PathVariable id: UUID,
        @RequestBody request: UpdateVehiclePositionRequest
    ): ResponseEntity<ApiResponse<TrackingVehicleDto>> {
        val data = updateVehiclePositionUseCase.execute(id, request)
        return ResponseEntity.ok(ApiResponse.success(data, "Position updated"))
    }
}
