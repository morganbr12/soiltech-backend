package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.agent.*
import com.soiltech.backend.application.usecase.agent.*
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
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
    private val activitiesUseCase: AgentActivitiesUseCase
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
