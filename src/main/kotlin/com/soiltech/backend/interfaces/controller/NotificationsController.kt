package com.soiltech.backend.interfaces.controller

import com.soiltech.backend.application.dto.NotificationDto
import com.soiltech.backend.application.dto.UnreadCountDto
import com.soiltech.backend.application.usecase.*
import com.soiltech.backend.infrastructure.security.UserPrincipal
import com.soiltech.backend.interfaces.response.ApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/notifications")
@PreAuthorize("hasAnyRole('CUSTOMER', 'AGENT', 'ADMIN')")
class NotificationsController(
    private val listNotificationsUseCase: ListNotificationsUseCase,
    private val getUnreadCountUseCase: GetUnreadCountUseCase,
    private val markReadUseCase: MarkNotificationReadUseCase,
    private val markAllReadUseCase: MarkAllNotificationsReadUseCase
) {

    @GetMapping
    fun list(
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "20") limit: Int,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<List<NotificationDto>>> {
        val (data, meta) = listNotificationsUseCase.execute(principal.id, page, limit)
        return ResponseEntity.ok(ApiResponse.success(data, meta = meta))
    }

    @GetMapping("/unread-count")
    fun unreadCount(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<UnreadCountDto>> =
        ResponseEntity.ok(ApiResponse.success(getUnreadCountUseCase.execute(principal.id)))

    @PatchMapping("/{id}/read")
    fun markRead(
        @PathVariable id: UUID,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<Unit?>> {
        markReadUseCase.execute(id, principal.id)
        return ResponseEntity.ok(ApiResponse.success(null, "Marked as read"))
    }

    @PatchMapping("/read-all")
    fun markAllRead(
        @AuthenticationPrincipal principal: UserPrincipal
    ): ResponseEntity<ApiResponse<Unit?>> {
        markAllReadUseCase.execute(principal.id)
        return ResponseEntity.ok(ApiResponse.success(null, "All notifications marked as read"))
    }
}
