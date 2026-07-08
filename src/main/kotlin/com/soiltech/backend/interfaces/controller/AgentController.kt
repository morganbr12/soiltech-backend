package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.agent.AgentProfileDto
import com.soiltech.backend.application.usecase.agent.GetAgentProfileUseCase
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/agent")
@PreAuthorize("hasRole('AGENT')")
class AgentController(
    private val getAgentProfileUseCase: GetAgentProfileUseCase
) {

    @GetMapping("/me")
    fun getMe(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<AgentProfileDto>> =
        ResponseEntity.ok(ApiResponse.success(getAgentProfileUseCase.execute(principal.id)))
}
