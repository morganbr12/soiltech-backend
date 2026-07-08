package com.soiltech.backend.application.dto.agent

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class AgentDashboardResponse(
    val todayCollections: Long,
    val todayFarmers: Long,
    val pendingUploads: Long,
    val offlineRecords: Long,
    val todayWeight: Double,
    val weeklyWeight: Double,
    val monthlyRevenue: Double,
    val activePickups: Long,
    val weeklyBreakdown: List<WeeklyBreakdownDto>
)

data class WeeklyBreakdownDto(
    val day: String,
    val kg: Double
)

data class AgentMobileProfileResponse(
    val id: UUID,
    val name: String,
    val phone: String,
    val email: String,
    val agentCode: String,
    val region: String,
    val district: String,
    val avatarUrl: String?,
    val totalFarmersRegistered: Long,
    val totalProduceCollected: Double,
    val totalCollections: Long,
    val performanceScore: Double,
    val joinDate: LocalDate
)

data class AgentActivityResponse(
    val action: String,
    val detail: String,
    val timestamp: LocalDateTime,
    val type: String
)

data class NotificationCountResponse(val unreadCount: Long)

data class AgentFarmerSummaryResponse(
    val id: UUID,
    val farmerCode: String,
    val fullName: String,
    val phone: String,
    val region: String,
    val district: String,
    val cropTypes: List<String>,
    val status: String,
    val kycVerified: Boolean
)
