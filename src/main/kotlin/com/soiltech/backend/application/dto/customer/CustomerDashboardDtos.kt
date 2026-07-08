package com.soiltech.backend.application.dto.customer

import java.math.BigDecimal
import java.util.UUID

data class CustomerDashboardResponse(
    val kpis: CustomerKpiDto,
    val statusBreakdown: Map<String, Long>,
    val tierBreakdown: Map<String, Long>,
    val topCustomers: List<TopCustomerDto>,
    val monthlyGrowth: List<Long>,
    val monthlyRevenue: List<BigDecimal>,
    val recentOrders: List<RecentOrderDto>
)

data class CustomerKpiDto(
    val totalCustomers: Long,
    val activeCustomers: Long,
    val pendingVerification: Long,
    val totalRevenue: BigDecimal,
    val avgRating: Double,
    val totalOrders: Long
)

data class TopCustomerDto(
    val id: UUID,
    val customerCode: String,
    val fullName: String,
    val region: String?,
    val totalOrders: Long,
    val totalSpent: BigDecimal
)

data class RecentOrderDto(
    val id: UUID,
    val orderCode: String,
    val customerId: UUID,
    val customerName: String,
    val produce: String,
    val totalAmount: BigDecimal,
    val status: String
)
