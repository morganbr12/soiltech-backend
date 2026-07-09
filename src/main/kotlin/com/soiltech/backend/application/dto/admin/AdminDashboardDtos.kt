package com.soiltech.backend.application.dto.admin

import java.math.BigDecimal

data class AdminDashboardResponse(
    val kpis: AdminDashboardKpis,
    val monthlyCollection: MonthlyCollectionData,
    val deliveryStatus: DeliveryStatusData,
    val monthlyRevenue: MonthlyRevenueData,
    val regionalOverview: List<RegionalOverviewItem>,
    val recentActivity: List<ActivityItem>,
    val systemAlerts: List<SystemAlert>
)

data class AdminDashboardKpis(
    val totalLbcs: Long,
    val totalLbcsTrend: Double,
    val activeAgents: Long,
    val activeAgentsTrend: Double,
    val registeredFarmers: Long,
    val registeredFarmersTrend: Double,
    val registeredFarms: Long,
    val registeredFarmsTrend: Double,
    val todaysCollection: Double,
    val todaysCollectionTrend: Double,
    val vehiclesOnRoute: Long,
    val vehiclesOnRouteTrend: Double,
    val driversOnline: Long,
    val driversOnlineTrend: Double,
    val deliveriesToday: Long,
    val deliveriesTodayTrend: Double,
    val warehouseCapacity: Double,
    val warehouseCapacityTrend: Double,
    val todaysRevenue: BigDecimal,
    val todaysRevenueTrend: Double,
    val paymentsPending: BigDecimal,
    val paymentsPendingTrend: Double,
    val failedDeliveries: Long,
    val failedDeliveriesTrend: Double
)

data class MonthlyCollectionData(
    val cocoa: List<Double>,
    val coffee: List<Double>,
    val cashew: List<Double>
)

data class DeliveryStatusData(
    val delivered: Long,
    val inTransit: Long,
    val scheduled: Long,
    val failed: Long,
    val returned: Long
)

data class MonthlyRevenueData(
    val months: List<String>,
    val revenue: List<BigDecimal>,
    val target: List<BigDecimal>
)

data class RegionalOverviewItem(
    val region: String,
    val farmers: Long,
    val produce: Double,
    val revenue: BigDecimal
)

data class ActivityItem(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    val time: String,
    val icon: String,
    val iconColor: String,
    val user: String?
)

data class SystemAlert(
    val id: String,
    val severity: String,
    val title: String,
    val message: String,
    val time: String
)
