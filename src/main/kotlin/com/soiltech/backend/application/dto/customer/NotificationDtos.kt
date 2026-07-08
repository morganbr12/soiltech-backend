package com.soiltech.backend.application.dto.customer

import com.soiltech.backend.domain.enum.NotificationCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime
import java.util.UUID

data class CustomerNotificationResponse(
    val id: UUID,
    val title: String,
    val target: String,
    val sentCount: Int,
    val deliveredCount: Int,
    val openedCount: Int,
    val sentAt: LocalDateTime
)

data class SendNotificationRequest(
    @field:NotBlank(message = "Title is required")
    val title: String,

    @field:NotBlank(message = "Body is required")
    val body: String,

    @field:NotBlank(message = "Target is required")
    val target: String,

    @field:NotNull(message = "Category is required")
    val category: NotificationCategory
)

data class SendNotificationResult(
    val sent: Int,
    val failed: Int
)
