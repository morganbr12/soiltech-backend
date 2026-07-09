package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.admin.AdminDashboardResponse
import com.soiltech.backend.application.usecase.admin.GetAdminDashboardUseCase
import com.soiltech.backend.interfaces.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/dashboard")
class DashboardController(
    private val getAdminDashboardUseCase: GetAdminDashboardUseCase
) {
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getDashboard(): ResponseEntity<ApiResponse<AdminDashboardResponse>> {
        val data = getAdminDashboardUseCase.execute()
        return ResponseEntity.ok(ApiResponse.success(data))
    }
}
