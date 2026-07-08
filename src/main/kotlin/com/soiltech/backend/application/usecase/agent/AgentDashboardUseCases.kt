package com.soiltech.backend.application.usecase.agent

import com.soiltech.backend.application.dto.agent.*
import com.soiltech.backend.application.dto.farm.FarmDto
import com.soiltech.backend.application.dto.farmer.FarmerResponse
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.Farm
import com.soiltech.backend.domain.entity.Farmer
import com.soiltech.backend.domain.entity.FarmerMetrics
import com.soiltech.backend.domain.repository.FarmRepository
import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.repository.*
import com.soiltech.backend.infrastructure.persistence.jpa.FarmJpaRepository
import com.soiltech.backend.interfaces.exception.ConflictException
import com.soiltech.backend.interfaces.exception.NotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

// ── Resolve agent.id from userId ─────────────────────────────────────────────

private fun resolveAgentId(
    userId: UUID,
    agentProfileRepository: AgentProfileRepository,
    agentRepository: AgentRepository
): UUID {
    val profile = agentProfileRepository.findByUserId(userId)
        ?: throw NotFoundException("Agent profile not found")
    val agent = agentRepository.findByAgentCode(profile.agentCode)
        ?: throw NotFoundException("Agent record not found for code: ${profile.agentCode}")
    return agent.id
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

@Service
class AgentDashboardUseCase(
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository,
    private val produceRecordRepository: ProduceRecordRepository,
    private val paymentRecordRepository: PaymentRecordRepository,
    private val pickupRequestRepository: PickupRequestRepository
) {
    fun execute(userId: UUID): AgentDashboardResponse {
        val agentId = resolveAgentId(userId, agentProfileRepository, agentRepository)

        val today = LocalDate.now()
        val dayStart = today.atStartOfDay()
        val dayEnd = today.atTime(LocalTime.MAX)

        val weekStart = today.with(DayOfWeek.MONDAY).atStartOfDay()
        val weekEnd = today.with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX)

        val monthStart = today.withDayOfMonth(1).atStartOfDay()
        val monthEnd = today.plusMonths(1).withDayOfMonth(1).atStartOfDay()

        val todayCollections = produceRecordRepository.countTodayByAgent(agentId, dayStart, dayEnd)
        val todayFarmers = produceRecordRepository.countDistinctFarmersTodayByAgent(agentId, dayStart, dayEnd)
        val pendingUploads = produceRecordRepository.countPendingUploadsByAgent(agentId)
        val offlineRecords = produceRecordRepository.countOfflineByAgent(agentId)
        val todayWeight = produceRecordRepository.sumWeightTodayByAgent(agentId, dayStart, dayEnd)
        val weeklyWeight = produceRecordRepository.sumWeightWeekByAgent(agentId, weekStart, weekEnd)
        val monthlyRevenue = paymentRecordRepository.sumMonthlyRevenueByAgent(agentId, monthStart, monthEnd)
        val activePickups = pickupRequestRepository.countActiveByAgent(agentId)

        val weeklyBreakdown = buildWeeklyBreakdown(
            produceRecordRepository.sumWeightByDayOfWeekForWeek(agentId, weekStart, weekEnd)
        )

        return AgentDashboardResponse(
            todayCollections = todayCollections,
            todayFarmers = todayFarmers,
            pendingUploads = pendingUploads,
            offlineRecords = offlineRecords,
            todayWeight = todayWeight.toDouble(),
            weeklyWeight = weeklyWeight.toDouble(),
            monthlyRevenue = monthlyRevenue.toDouble(),
            activePickups = activePickups,
            weeklyBreakdown = weeklyBreakdown
        )
    }

    private fun buildWeeklyBreakdown(rawRows: List<Array<Any>>): List<WeeklyBreakdownDto> {
        // Hibernate EXTRACT(DAY_OF_WEEK) maps to PostgreSQL DOW: 0=Sun, 1=Mon..6=Sat
        // Convert to ISO: 1=Mon..7=Sun
        val dayKgMap = mutableMapOf<Int, Double>()
        rawRows.forEach { row ->
            val dow = (row[0] as Number).toInt()
            val iso = if (dow == 0) 7 else dow
            val kg = when (val v = row[1]) {
                is BigDecimal -> v.toDouble()
                is Number -> v.toDouble()
                else -> 0.0
            }
            dayKgMap[iso] = (dayKgMap[iso] ?: 0.0) + kg
        }
        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        return (1..7).map { iso ->
            WeeklyBreakdownDto(day = labels[iso - 1], kg = dayKgMap[iso] ?: 0.0)
        }
    }
}

// ── Profile ───────────────────────────────────────────────────────────────────

@Service
class AgentMobileProfileUseCase(
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository,
    private val farmerRepository: FarmerRepository,
    private val produceRecordRepository: ProduceRecordRepository
) {
    fun execute(userId: UUID): AgentMobileProfileResponse {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found for code: ${profile.agentCode}")

        val totalFarmers = farmerRepository.countByAgentId(agent.id)
        val totalCollections = produceRecordRepository.countAllByAgent(agent.id)
        val totalWeight = produceRecordRepository.sumTotalWeightByAgent(agent.id)
        val performanceScore = computePerformanceScore(agent.id, totalCollections)

        return AgentMobileProfileResponse(
            id = agent.id,
            name = "${agent.firstName} ${agent.lastName}",
            phone = agent.phone,
            email = agent.email,
            agentCode = agent.agentCode,
            region = agent.region,
            district = agent.district,
            avatarUrl = null,
            totalFarmersRegistered = totalFarmers,
            totalProduceCollected = totalWeight.toDouble(),
            totalCollections = totalCollections,
            performanceScore = performanceScore,
            joinDate = agent.joinedDate.toLocalDate()
        )
    }

    private fun computePerformanceScore(agentId: UUID, totalCollections: Long): Double {
        if (totalCollections == 0L) return 0.0
        val collectedCount = produceRecordRepository.countCollectedByAgent(agentId)
        val approvedFarmers = farmerRepository.countApprovedByAgentId(agentId)
        val totalFarmers = farmerRepository.countByAgentId(agentId)

        val collectionRate = collectedCount.toDouble() / totalCollections.toDouble()
        val approvalRate = if (totalFarmers > 0) approvedFarmers.toDouble() / totalFarmers.toDouble() else 0.5

        val raw = (collectionRate * 70.0) + (approvalRate * 30.0)
        return BigDecimal(raw * 100.0).setScale(1, RoundingMode.HALF_UP).toDouble()
            .coerceIn(0.0, 100.0)
    }
}

// ── Activities ────────────────────────────────────────────────────────────────

@Service
class AgentActivitiesUseCase(
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository,
    private val produceRecordRepository: ProduceRecordRepository,
    private val farmerRepository: FarmerRepository,
    private val pickupRequestRepository: PickupRequestRepository,
    private val paymentRecordRepository: PaymentRecordRepository,
    private val farmJpaRepository: FarmJpaRepository
) {
    fun execute(userId: UUID, limit: Int): List<AgentActivityResponse> {
        val agentId = resolveAgentId(userId, agentProfileRepository, agentRepository)
        val fetch = (limit * 2).coerceIn(2, 100)

        val activities = mutableListOf<AgentActivityResponse>()

        // Collections
        val recentProduceRecords = produceRecordRepository.findRecentByAgent(agentId, fetch)
        val farmerIds = recentProduceRecords.map { it.farmerId }.distinct()
        val farmerMap = if (farmerIds.isNotEmpty())
            farmerRepository.findByIds(farmerIds).associateBy { it.id }
        else emptyMap()

        recentProduceRecords.forEach { record ->
            val farmerName = farmerMap[record.farmerId]
                ?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Farmer"
            activities += AgentActivityResponse(
                action = "Collection recorded",
                detail = "$farmerName – ${record.quantityKg} kg ${record.cropType}",
                timestamp = record.createdAt,
                type = "collection"
            )
        }

        // Farmer registrations
        farmerRepository.findRecentByAgent(agentId, fetch).forEach { farmer ->
            activities += AgentActivityResponse(
                action = "Farmer registered",
                detail = "${farmer.firstName} ${farmer.lastName}, ${farmer.region}",
                timestamp = farmer.createdAt,
                type = "farmer_registered"
            )
        }

        // Pickups
        pickupRequestRepository.findRecentByAgent(agentId, fetch).forEach { pickup ->
            activities += AgentActivityResponse(
                action = "Pickup assigned",
                detail = "Pickup #${pickup.id.toString().takeLast(8).uppercase()}, ${pickup.status.value}",
                timestamp = pickup.createdAt,
                type = "pickup"
            )
        }

        // Payments
        val recentPayments = paymentRecordRepository.findRecentByAgent(agentId, fetch)
        val paymentFarmerIds = recentPayments.map { it.farmerId }.distinct()
        val paymentFarmerMap = if (paymentFarmerIds.isNotEmpty())
            farmerRepository.findByIds(paymentFarmerIds).associateBy { it.id }
        else emptyMap()

        recentPayments.forEach { payment ->
            val farmerName = paymentFarmerMap[payment.farmerId]
                ?.let { "${it.firstName} ${it.lastName}" } ?: "Unknown Farmer"
            activities += AgentActivityResponse(
                action = "Payment processed",
                detail = "${payment.currency} ${payment.amount} paid to $farmerName",
                timestamp = payment.createdAt,
                type = "payment"
            )
        }

        // Farm registrations (via farmers of this agent)
        farmJpaRepository.findRecentByAgentId(agentId, PageRequest.of(0, fetch)).forEach { farm ->
            activities += AgentActivityResponse(
                action = "Farm registered",
                detail = buildString {
                    append(farm.name)
                    farm.sizeHectares?.let { append(", ${it} ha") }
                    farm.cropType?.let { append(" – $it") }
                },
                timestamp = farm.createdAt,
                type = "farm_registered"
            )
        }

        return activities
            .sortedByDescending { it.timestamp }
            .take(limit.coerceIn(1, 50))
    }
}

// ── Agent's farmer list (for produce selection) ───────────────────────────────

@Service
class GetAgentFarmersUseCase(
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository,
    private val farmerRepository: FarmerRepository
) {
    fun execute(
        userId: UUID,
        search: String?,
        status: String?,
        page: Int,
        limit: Int
    ): Page<AgentFarmerSummaryResponse> {
        val agentId = resolveAgentId(userId, agentProfileRepository, agentRepository)

        val farmerStatus = status?.uppercase()?.let {
            runCatching { FarmerStatus.valueOf(it) }.getOrNull()
        }

        val pageable = PageRequest.of(
            (page - 1).coerceAtLeast(0),
            limit.coerceIn(1, 100),
            Sort.by(Sort.Direction.ASC, "firstName")
        )

        return farmerRepository.findAll(
            status = farmerStatus,
            region = null,
            lbcId = null,
            agentId = agentId,
            kycVerified = null,
            search = search?.takeIf { it.isNotBlank() },
            pageable = pageable
        ).map { farmer ->
            AgentFarmerSummaryResponse(
                id = farmer.id,
                farmerCode = farmer.farmerCode,
                fullName = "${farmer.firstName} ${farmer.lastName}",
                phone = farmer.phone,
                region = farmer.region,
                district = farmer.district,
                cropTypes = farmer.cropTypes,
                status = farmer.status.value,
                kycVerified = farmer.kycVerified
            )
        }
    }
}

// ── Agent registers a farm for a farmer ──────────────────────────────────────

@Service
class RegisterFarmByAgentUseCase(
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository,
    private val farmerRepository: FarmerRepository,
    private val farmRepository: FarmRepository
) {
    @Transactional
    fun execute(userId: UUID, request: RegisterFarmByAgentRequest, photoUrls: List<String> = emptyList()): FarmDto {
        val agentId = resolveAgentId(userId, agentProfileRepository, agentRepository)

        val farmer = farmerRepository.findById(request.farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agentId)
            throw NotFoundException("Farmer not found")

        val now = LocalDateTime.now()
        val farm = farmRepository.save(
            Farm(
                id = UUID.randomUUID(),
                farmerId = farmer.id,
                name = request.name,
                sizeHectares = request.sizeHectares,
                cropType = request.cropType,
                location = request.location,
                latitude = request.latitude,
                longitude = request.longitude,
                photos = photoUrls,
                createdAt = now,
                updatedAt = now
            )
        )
        return farm.toDto()
    }
}

// ── Agent fetches a single farmer ─────────────────────────────────────────────

@Service
class GetAgentFarmerUseCase(
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository,
    private val farmerRepository: FarmerRepository
) {
    fun execute(userId: UUID, farmerId: UUID): FarmerResponse {
        val agentId = resolveAgentId(userId, agentProfileRepository, agentRepository)
        val farmer = farmerRepository.findById(farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agentId)
            throw NotFoundException("Farmer not found")

        val metrics = farmerRepository.findMetricsByFarmerIds(listOf(farmerId))[farmerId]
            ?: com.soiltech.backend.domain.entity.FarmerMetrics()

        return FarmerResponse(
            id = farmer.id,
            farmerCode = farmer.farmerCode,
            firstName = farmer.firstName,
            lastName = farmer.lastName,
            fullName = "${farmer.firstName} ${farmer.lastName}",
            phone = farmer.phone,
            email = farmer.email,
            nationalId = farmer.nationalId,
            agentId = farmer.agentId,
            agentName = farmer.agentName,
            lbcId = farmer.lbcId,
            lbcName = farmer.lbcName,
            region = farmer.region,
            district = farmer.district,
            farmsCount = metrics.farmsCount,
            totalFarmSize = metrics.totalFarmSize,
            cropTypes = metrics.cropTypes.ifEmpty { farmer.cropTypes },
            walletBalance = metrics.walletBalance,
            totalEarnings = metrics.totalEarnings,
            kycVerified = farmer.kycVerified,
            status = farmer.status,
            rejectionReason = farmer.rejectionReason,
            lat = farmer.latitude,
            lng = farmer.longitude,
            joinedDate = farmer.joinedDate,
            createdAt = farmer.createdAt,
            updatedAt = farmer.updatedAt
        )
    }
}

// ── Agent registers a farmer ──────────────────────────────────────────────────

@Service
class RegisterFarmerByAgentUseCase(
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository,
    private val farmerRepository: FarmerRepository,
    private val lbcRepository: LbcRepository
) {
    @Transactional
    fun execute(userId: UUID, request: RegisterFarmerByAgentRequest): FarmerResponse {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found for code: ${profile.agentCode}")
        val lbc = lbcRepository.findById(agent.lbcId)
            ?: throw NotFoundException("LBC not found for agent")

        if (farmerRepository.existsByPhone(request.phone))
            throw ConflictException("A farmer with phone '${request.phone}' already exists")
        request.nationalId?.let {
            if (farmerRepository.existsByNationalId(it))
                throw ConflictException("A farmer with national ID '$it' already exists")
        }

        val farmerCode = generateUniqueCode(farmerRepository)
        val now = LocalDateTime.now()

        val farmer = farmerRepository.save(
            Farmer(
                id = UUID.randomUUID(),
                farmerCode = farmerCode,
                firstName = request.firstName,
                lastName = request.lastName,
                phone = request.phone,
                email = request.email,
                nationalId = request.nationalId,
                agentId = agent.id,
                agentName = "${agent.firstName} ${agent.lastName}",
                lbcId = lbc.id,
                lbcName = lbc.name,
                region = request.region,
                district = request.district,
                status = FarmerStatus.PENDING,
                kycVerified = false,
                latitude = request.latitude,
                longitude = request.longitude,
                cropTypes = request.cropTypes,
                rejectionReason = null,
                joinedDate = now,
                createdAt = now,
                updatedAt = now,
                createdBy = null,
                updatedBy = null
            )
        )

        return FarmerResponse(
            id = farmer.id,
            farmerCode = farmer.farmerCode,
            firstName = farmer.firstName,
            lastName = farmer.lastName,
            fullName = "${farmer.firstName} ${farmer.lastName}",
            phone = farmer.phone,
            email = farmer.email,
            nationalId = farmer.nationalId,
            agentId = farmer.agentId,
            agentName = farmer.agentName,
            lbcId = farmer.lbcId,
            lbcName = farmer.lbcName,
            region = farmer.region,
            district = farmer.district,
            farmsCount = 0L,
            totalFarmSize = 0.0,
            cropTypes = farmer.cropTypes,
            walletBalance = BigDecimal.ZERO,
            totalEarnings = BigDecimal.ZERO,
            kycVerified = false,
            status = farmer.status,
            rejectionReason = null,
            lat = farmer.latitude,
            lng = farmer.longitude,
            joinedDate = farmer.joinedDate,
            createdAt = farmer.createdAt,
            updatedAt = farmer.updatedAt
        )
    }

    private fun generateUniqueCode(repo: FarmerRepository): String {
        var num = (repo.countAll() + 1).toInt()
        var code: String
        do {
            code = "FMR-${String.format("%05d", num++)}"
        } while (repo.existsByFarmerCode(code))
        return code
    }
}

// ── Notifications unread count ─────────────────────────────────────────────────

@Service
class AgentNotificationUnreadCountUseCase(
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository,
    private val paymentRecordRepository: PaymentRecordRepository
) {
    fun execute(userId: UUID): NotificationCountResponse {
        val agentId = resolveAgentId(userId, agentProfileRepository, agentRepository)
        val since = LocalDateTime.now().minusHours(24)
        val count = paymentRecordRepository.countRecentByAgent(agentId, since)
        return NotificationCountResponse(unreadCount = count)
    }
}
