package com.soiltech.backend.application.usecase.admin

import com.soiltech.backend.application.dto.admin.*
import com.soiltech.backend.domain.enum.AgentStatus
import com.soiltech.backend.domain.enum.LogisticsStatus
import com.soiltech.backend.domain.enum.PaymentStatus
import com.soiltech.backend.domain.repository.*
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class GetAdminDashboardUseCase(
    private val lbcRepository: LbcRepository,
    private val agentRepository: AgentRepository,
    private val farmerRepository: FarmerRepository,
    private val farmRepository: FarmRepository,
    private val produceRecordRepository: ProduceRecordRepository,
    private val pickupRequestRepository: PickupRequestRepository,
    private val paymentRecordRepository: PaymentRecordRepository,
    private val customerOrderRepository: CustomerOrderRepository,
    private val produceListingRepository: ProduceListingRepository
) {
    fun execute(): AdminDashboardResponse {
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val todayStart = today.atStartOfDay()
        val todayEnd = todayStart.plusDays(1)
        val yesterdayStart = todayStart.minusDays(1)
        val thisMonthStart = today.withDayOfMonth(1).atStartOfDay()
        val lastMonthStart = thisMonthStart.minusMonths(1)
        val year = today.year

        // ─── LBCs ────────────────────────────────────────────────────────────
        val lbcStatusCounts = lbcRepository.countByStatus()
        val totalLbcs = lbcStatusCounts.values.sum()
        val lbcsThisMonth = lbcRepository.countCreatedBetween(thisMonthStart, todayEnd)
        val lbcsLastMonth = lbcRepository.countCreatedBetween(lastMonthStart, thisMonthStart)
        val totalLbcsTrend = pctChange(lbcsThisMonth, lbcsLastMonth)

        // ─── Agents ──────────────────────────────────────────────────────────
        val agentStatusCounts = agentRepository.countByStatus()
        val activeAgents = agentStatusCounts[AgentStatus.ACTIVE] ?: 0L
        val agentsThisMonth = agentRepository.countCreatedBetween(thisMonthStart, todayEnd)
        val agentsLastMonth = agentRepository.countCreatedBetween(lastMonthStart, thisMonthStart)
        val activeAgentsTrend = pctChange(agentsThisMonth, agentsLastMonth)

        // ─── Farmers ─────────────────────────────────────────────────────────
        val registeredFarmers = farmerRepository.countAll()
        val farmersThisMonth = farmerRepository.countCreatedBetween(thisMonthStart, todayEnd)
        val farmersLastMonth = farmerRepository.countCreatedBetween(lastMonthStart, thisMonthStart)
        val registeredFarmersTrend = pctChange(farmersThisMonth, farmersLastMonth)

        // ─── Farms ───────────────────────────────────────────────────────────
        val registeredFarms = farmRepository.countAll()
        val farmsThisMonth = farmRepository.countCreatedBetween(thisMonthStart, todayEnd)
        val farmsLastMonth = farmRepository.countCreatedBetween(lastMonthStart, thisMonthStart)
        val registeredFarmsTrend = pctChange(farmsThisMonth, farmsLastMonth)

        // ─── Collection ──────────────────────────────────────────────────────
        val todaysCollectionKg = produceRecordRepository.sumQuantityKgBetween(todayStart, todayEnd)
        val yesterdayCollectionKg = produceRecordRepository.sumQuantityKgBetween(yesterdayStart, todayStart)
        val todaysCollectionTonnes = todaysCollectionKg.toDouble() / 1000.0
        val yesterdayCollectionTonnes = yesterdayCollectionKg.toDouble() / 1000.0
        val todaysCollectionTrend = pctChangeDouble(todaysCollectionTonnes, yesterdayCollectionTonnes)

        // ─── Logistics ───────────────────────────────────────────────────────
        val deliveryStatusCounts = pickupRequestRepository.countByStatusAll()
        val vehiclesOnRoute = deliveryStatusCounts[LogisticsStatus.IN_TRANSIT] ?: 0L
        val driversOnline = pickupRequestRepository.countDistinctAgentsByStatus(LogisticsStatus.IN_TRANSIT)
        val deliveriesToday = pickupRequestRepository.countByStatusSince(LogisticsStatus.DELIVERED, todayStart)
        val deliveriesYesterday = pickupRequestRepository.countByStatusSince(LogisticsStatus.DELIVERED, yesterdayStart) - deliveriesToday
        val failedDeliveries = pickupRequestRepository.countByStatusSince(LogisticsStatus.CANCELLED, todayStart)
        val failedYesterday = pickupRequestRepository.countByStatusSince(LogisticsStatus.CANCELLED, yesterdayStart) - failedDeliveries

        // ─── Warehouse ───────────────────────────────────────────────────────
        val warehouseCapacity = produceListingRepository.computeFillRate()

        // ─── Revenue ─────────────────────────────────────────────────────────
        val todaysRevenue = customerOrderRepository.sumAmountBetween(todayStart, todayEnd)
        val yesterdayRevenue = customerOrderRepository.sumAmountBetween(yesterdayStart, todayStart)
        val todaysRevenueTrend = pctChange(todaysRevenue, yesterdayRevenue)

        // ─── Payments ────────────────────────────────────────────────────────
        val paymentsPending = paymentRecordRepository.sumAmountByStatus(PaymentStatus.PENDING)

        // ─── Monthly collection chart ─────────────────────────────────────────
        val monthlyCollection = MonthlyCollectionData(
            cocoa  = buildMonthlyList(produceRecordRepository.findMonthlyCropCollection(year, "%cocoa%")),
            coffee = buildMonthlyList(produceRecordRepository.findMonthlyCropCollection(year, "%coffee%")),
            cashew = buildMonthlyList(produceRecordRepository.findMonthlyCropCollection(year, "%cashew%"))
        )

        // ─── Delivery status donut ────────────────────────────────────────────
        val deliveryStatus = DeliveryStatusData(
            delivered = deliveryStatusCounts[LogisticsStatus.DELIVERED] ?: 0L,
            inTransit = deliveryStatusCounts[LogisticsStatus.IN_TRANSIT] ?: 0L,
            scheduled = deliveryStatusCounts[LogisticsStatus.PENDING] ?: 0L,
            failed    = deliveryStatusCounts[LogisticsStatus.CANCELLED] ?: 0L,
            returned  = 0L
        )

        // ─── Monthly revenue chart ────────────────────────────────────────────
        val monthlyRev = customerOrderRepository.sumMonthlyRevenue(year)
        val currentMonth = today.monthValue
        val monthLabels = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
        val revenueSlice = monthlyRev.take(currentMonth)
        val target = computeTargets(revenueSlice)
        val monthlyRevenue = MonthlyRevenueData(
            months  = monthLabels.take(currentMonth),
            revenue = revenueSlice,
            target  = target
        )

        // ─── Regional overview ────────────────────────────────────────────────
        val regionalOverview = farmerRepository.findRegionalOverview().map {
            RegionalOverviewItem(it.region, it.farmers, it.produce, it.revenue)
        }

        // ─── Recent activity ─────────────────────────────────────────────────
        val recentActivity = buildActivity(produceListingRepository, farmerRepository, customerOrderRepository, now)

        // ─── System alerts ────────────────────────────────────────────────────
        val systemAlerts = buildAlerts(failedDeliveries, paymentsPending, now)

        return AdminDashboardResponse(
            kpis = AdminDashboardKpis(
                totalLbcs               = totalLbcs,
                totalLbcsTrend          = totalLbcsTrend,
                activeAgents            = activeAgents,
                activeAgentsTrend       = activeAgentsTrend,
                registeredFarmers       = registeredFarmers,
                registeredFarmersTrend  = registeredFarmersTrend,
                registeredFarms         = registeredFarms,
                registeredFarmsTrend    = registeredFarmsTrend,
                todaysCollection        = todaysCollectionTonnes.round1(),
                todaysCollectionTrend   = todaysCollectionTrend,
                vehiclesOnRoute         = vehiclesOnRoute,
                vehiclesOnRouteTrend    = 0.0,
                driversOnline           = driversOnline,
                driversOnlineTrend      = 0.0,
                deliveriesToday         = deliveriesToday,
                deliveriesTodayTrend    = pctChange(deliveriesToday, deliveriesYesterday),
                warehouseCapacity       = warehouseCapacity.round1(),
                warehouseCapacityTrend  = 0.0,
                todaysRevenue           = todaysRevenue,
                todaysRevenueTrend      = todaysRevenueTrend,
                paymentsPending         = paymentsPending,
                paymentsPendingTrend    = 0.0,
                failedDeliveries        = failedDeliveries,
                failedDeliveriesTrend   = pctChange(failedDeliveries, failedYesterday)
            ),
            monthlyCollection = monthlyCollection,
            deliveryStatus    = deliveryStatus,
            monthlyRevenue    = monthlyRevenue,
            regionalOverview  = regionalOverview,
            recentActivity    = recentActivity,
            systemAlerts      = systemAlerts
        )
    }

    private fun pctChange(current: Long, previous: Long): Double {
        if (previous == 0L) return if (current > 0) 100.0 else 0.0
        return ((current - previous).toDouble() / previous * 100).round1()
    }

    private fun pctChange(current: BigDecimal, previous: BigDecimal): Double {
        if (previous.compareTo(BigDecimal.ZERO) == 0) return if (current > BigDecimal.ZERO) 100.0 else 0.0
        return ((current - previous).toDouble() / previous.toDouble() * 100).round1()
    }

    private fun pctChangeDouble(current: Double, previous: Double): Double {
        if (previous == 0.0) return if (current > 0) 100.0 else 0.0
        return ((current - previous) / previous * 100).round1()
    }

    private fun buildMonthlyList(pairs: List<Pair<Int, BigDecimal>>): List<Double> {
        val map = pairs.associate { (month, kg) -> month to kg.toDouble() / 1000.0 }
        return (1..12).map { m -> (map[m] ?: 0.0).round1() }
    }

    private fun computeTargets(revenue: List<BigDecimal>): List<BigDecimal> {
        if (revenue.isEmpty()) return emptyList()
        val avg = revenue.fold(BigDecimal.ZERO) { a, v -> a + v }
            .divide(BigDecimal(revenue.size.coerceAtLeast(1)), 2, RoundingMode.HALF_UP)
        val target = avg.multiply(BigDecimal("1.20")).setScale(2, RoundingMode.HALF_UP)
        return revenue.map { target }
    }

    private fun buildActivity(
        produceListingRepo: ProduceListingRepository,
        farmerRepo: FarmerRepository,
        orderRepo: CustomerOrderRepository,
        now: LocalDateTime
    ): List<ActivityItem> {
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val items = mutableListOf<Pair<LocalDateTime, ActivityItem>>()

        produceListingRepo.findRecent(4).forEach { listing ->
            val qty = listing.totalQuantityKg.setScale(1, RoundingMode.HALF_UP)
            items += listing.createdAt to ActivityItem(
                id          = listing.id.toString(),
                title       = "Produce Listed",
                description = "${listing.agentName ?: "An agent"} listed ${qty}kg of ${listing.cropType}" +
                              listing.farmerName?.let { " from $it" }.orEmpty(),
                type        = "produce",
                time        = listing.createdAt.format(fmt),
                icon        = "agriculture",
                iconColor   = "#16a34a",
                user        = listing.agentName
            )
        }

        farmerRepo.findRecentGlobal(3).forEach { farmer ->
            items += farmer.createdAt to ActivityItem(
                id          = farmer.id.toString(),
                title       = "New Farmer Registered",
                description = "${farmer.firstName} ${farmer.lastName} registered" + farmer.region?.let { " in $it" }.orEmpty(),
                type        = "farmer",
                time        = farmer.createdAt.format(fmt),
                icon        = "person_add",
                iconColor   = "#0284c7",
                user        = null
            )
        }

        orderRepo.findRecent(3).forEach { order ->
            items += order.createdAt to ActivityItem(
                id          = order.id.toString(),
                title       = "New Customer Order",
                description = "${order.customerName ?: "A customer"} placed an order for GHS ${order.totalAmount.setScale(2, RoundingMode.HALF_UP)}",
                type        = "delivery",
                time        = order.createdAt.format(fmt),
                icon        = "shopping_cart",
                iconColor   = "#d97706",
                user        = order.customerName
            )
        }

        return items.sortedByDescending { it.first }.take(7).map { it.second }
    }

    private fun buildAlerts(
        failedDeliveries: Long,
        paymentsPending: BigDecimal,
        now: LocalDateTime
    ): List<SystemAlert> {
        val alerts = mutableListOf<SystemAlert>()
        val fmt = DateTimeFormatter.ISO_LOCAL_DATE_TIME

        if (failedDeliveries > 5) {
            alerts += SystemAlert(
                id       = "failed-deliveries",
                severity = "critical",
                title    = "High Delivery Failures",
                message  = "$failedDeliveries deliveries failed today — review logistics operations",
                time     = now.format(fmt)
            )
        }
        if (paymentsPending > BigDecimal("50000")) {
            alerts += SystemAlert(
                id       = "pending-payments",
                severity = "warning",
                title    = "Large Pending Payments",
                message  = "GHS ${paymentsPending.setScale(2, RoundingMode.HALF_UP)} awaiting disbursement",
                time     = now.format(fmt)
            )
        }
        alerts += SystemAlert(
            id       = "system-ok",
            severity = "info",
            title    = "System Operational",
            message  = "All services are running normally",
            time     = now.format(fmt)
        )
        return alerts
    }
}

private fun Double.round1(): Double =
    BigDecimal(this).setScale(1, RoundingMode.HALF_UP).toDouble()
