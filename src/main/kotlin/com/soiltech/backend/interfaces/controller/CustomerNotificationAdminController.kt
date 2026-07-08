package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.customer.*
import com.soiltech.backend.application.usecase.customer.*
import com.soiltech.backend.interfaces.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/customers/notifications")
class CustomerNotificationAdminController(
    private val sendNotificationUseCase: SendNotificationUseCase,
    private val notificationHistoryUseCase: NotificationHistoryUseCase
) {

    @PostMapping("/send")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:notifications')")
    fun send(
        @Valid @RequestBody request: SendNotificationRequest
    ): ResponseEntity<ApiResponse<SendNotificationResult>> {
        val result = sendNotificationUseCase.execute(request)
        return ResponseEntity.ok(ApiResponse.success(result, "Notification sent"))
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('customers:notifications')")
    fun history(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<ApiResponse<List<CustomerNotificationResponse>>> {
        val (items, meta) = notificationHistoryUseCase.execute(page, limit)
        return ResponseEntity.ok(ApiResponse.success(items, meta = meta))
    }
}
