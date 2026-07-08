package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.agent.AgentResponse
import com.soiltech.backend.application.dto.agent.RegisterAgentRequest
import com.soiltech.backend.application.dto.agent.UpdateAgentRequest
import com.soiltech.backend.application.usecase.agent.*
import com.soiltech.backend.domain.enum.AgentStatus
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/agents")
class AgentManagementController(
    private val listAgentsUseCase: ListAgentsUseCase,
    private val getAgentUseCase: GetAgentUseCase,
    private val registerAgentUseCase: RegisterAgentUseCase,
    private val updateAgentUseCase: UpdateAgentUseCase,
    private val deleteAgentUseCase: DeleteAgentUseCase
) {

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('agents:view')")
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(name = "per_page", defaultValue = "20") perPage: Int,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) region: String?,
        @RequestParam(required = false) search: String?,
        @RequestParam(name = "sort_by", defaultValue = "createdAt") sortBy: String,
        @RequestParam(name = "sort_order", defaultValue = "desc") sortOrder: String
    ): ResponseEntity<ApiResponse<List<AgentResponse>>> {
        val agentStatus = status?.let { AgentStatus.fromValue(it) }
        val (items, summary, meta) = listAgentsUseCase.execute(agentStatus, region, search, page, perPage, sortBy, sortOrder)
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta, summary = summary))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('agents:view')")
    fun getById(@PathVariable id: UUID): ResponseEntity<ApiResponse<AgentResponse>> {
        val data = getAgentUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(data))
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('agents:create')")
    fun register(
        @Valid @RequestBody request: RegisterAgentRequest
    ): ResponseEntity<ApiResponse<AgentResponse>> {
        val data = registerAgentUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(data, "Agent registered successfully"))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('agents:edit')")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateAgentRequest
    ): ResponseEntity<ApiResponse<AgentResponse>> {
        val data = updateAgentUseCase.execute(id, request)
        return ResponseEntity.ok(ApiResponse.success(data, "Agent updated successfully"))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('agents:delete')")
    fun delete(@PathVariable id: UUID): ResponseEntity<ApiResponse<Unit?>> {
        deleteAgentUseCase.execute(id)
        return ResponseEntity.ok(ApiResponse.success(null, "Agent deleted successfully"))
    }
}
