package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.agent.*
import com.soiltech.backend.application.usecase.agent.*
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/agent")
@PreAuthorize("hasRole('AGENT')")
class AgentDashboardController(
    private val dashboardUseCase: AgentDashboardUseCase,
    private val profileUseCase: AgentMobileProfileUseCase,
    private val activitiesUseCase: AgentActivitiesUseCase,
    private val getFarmersUseCase: GetAgentFarmersUseCase
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

@RestController
@RequestMapping("/notifications")
@PreAuthorize("hasRole('AGENT')")
class AgentNotificationsController(
    private val unreadCountUseCase: AgentNotificationUnreadCountUseCase
) {

    @GetMapping("/unread-count")
    fun getUnreadCount(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<NotificationCountResponse>> =
        ResponseEntity.ok(ApiResponse.success(unreadCountUseCase.execute(principal.id)))
}
