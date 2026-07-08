package com.soiltech.backend.application.usecase.farmer

import com.soiltech.backend.application.dto.farmer.*
import com.soiltech.backend.domain.entity.Farmer
import com.soiltech.backend.domain.entity.FarmerMetrics
import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.repository.AgentRepository
import com.soiltech.backend.domain.repository.FarmerRepository
import com.soiltech.backend.domain.repository.LbcRepository
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.ConflictException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

// ── List ──────────────────────────────────────────────────────────────────────

@Service
class ListFarmersUseCase(private val farmerRepository: FarmerRepository) {

    fun execute(
        status: FarmerStatus?,
        region: String?,
        lbcId: UUID?,
        agentId: UUID?,
        kycVerified: Boolean?,
        search: String?,
        page: Int,
        perPage: Int,
        sortBy: String,
        sortOrder: String
    ): Triple<List<FarmerResponse>, FarmerSummaryResponse, PaginationMeta> {

        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val sortField = SORTABLE_FIELDS.getOrDefault(sortBy, "createdAt")
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), perPage.coerceIn(1, 100), Sort.by(direction, sortField))

        val resultPage = farmerRepository.findAll(status, region, lbcId, agentId, kycVerified, search, pageable)
        val counts = farmerRepository.countByStatus()

        val ids = resultPage.content.map { it.id }
        val metricsMap = farmerRepository.findMetricsByFarmerIds(ids)

        val items = resultPage.content.map { it.toResponse(metricsMap[it.id] ?: FarmerMetrics()) }

        val approvedCount = counts.getOrDefault(FarmerStatus.APPROVED, 0L).toInt()
        val pendingCount = counts.getOrDefault(FarmerStatus.PENDING, 0L).toInt()
        val rejectedCount = counts.getOrDefault(FarmerStatus.REJECTED, 0L).toInt()
        val summary = FarmerSummaryResponse(
            total = approvedCount + pendingCount + rejectedCount,
            approved = approvedCount,
            pending = pendingCount,
            rejected = rejectedCount
        )
        val meta = PaginationMeta.from(resultPage, page, perPage)

        return Triple(items, summary, meta)
    }

    companion object {
        val SORTABLE_FIELDS = mapOf(
            "firstName" to "firstName",
            "lastName" to "lastName",
            "farmerCode" to "farmerCode",
            "phone" to "phone",
            "email" to "email",
            "region" to "region",
            "district" to "district",
            "status" to "status",
            "kycVerified" to "kycVerified",
            "joinedDate" to "joinedDate",
            "createdAt" to "createdAt",
            "updatedAt" to "updatedAt"
        )
    }
}

// ── Get single ────────────────────────────────────────────────────────────────

@Service
class GetFarmerUseCase(private val farmerRepository: FarmerRepository) {

    fun execute(id: UUID): FarmerResponse {
        val farmer = farmerRepository.findById(id)
            ?: throw NotFoundException("Farmer not found with id: $id")
        val metrics = farmerRepository.findMetricsByFarmerIds(listOf(id))[id] ?: FarmerMetrics()
        return farmer.toResponse(metrics)
    }
}

// ── Register ──────────────────────────────────────────────────────────────────

@Service
class RegisterFarmerUseCase(
    private val farmerRepository: FarmerRepository,
    private val agentRepository: AgentRepository,
    private val lbcRepository: LbcRepository
) {

    @Transactional
    fun execute(request: RegisterFarmerRequest): FarmerResponse {
        if (farmerRepository.existsByPhone(request.phone))
            throw ConflictException("A farmer with phone '${request.phone}' already exists")
        request.nationalId?.let {
            if (farmerRepository.existsByNationalId(it))
                throw ConflictException("A farmer with national ID '$it' already exists")
        }

        val agent = agentRepository.findById(request.agentId)
            ?: throw NotFoundException("Agent not found with id: ${request.agentId}")
        val lbc = lbcRepository.findById(request.lbcId)
            ?: throw NotFoundException("LBC not found with id: ${request.lbcId}")

        if (agent.lbcId != lbc.id)
            throw BadRequestException(
                "Agent '${agent.agentCode}' belongs to LBC '${agent.lbcName}' not '${lbc.name}'. " +
                "Please select an agent that belongs to the selected LBC."
            )

        val now = LocalDateTime.now()
        val farmerCode = generateUniqueCode(farmerRepository)

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
                latitude = null,
                longitude = null,
                cropTypes = request.cropTypes,
                rejectionReason = null,
                joinedDate = now,
                createdAt = now,
                updatedAt = now,
                createdBy = null,
                updatedBy = null
            )
        )
        return farmer.toResponse(FarmerMetrics(cropTypes = request.cropTypes))
    }

    private fun generateUniqueCode(repo: FarmerRepository): String {
        val lastCode = repo.countAll()
        var num = (lastCode + 1).toInt()
        var code: String
        do {
            code = "FMR-${String.format("%05d", num++)}"
        } while (repo.existsByFarmerCode(code))
        return code
    }
}

// ── Update ────────────────────────────────────────────────────────────────────

@Service
class UpdateFarmerUseCase(
    private val farmerRepository: FarmerRepository,
    private val agentRepository: AgentRepository,
    private val lbcRepository: LbcRepository
) {

    @Transactional
    fun execute(id: UUID, request: UpdateFarmerRequest): FarmerResponse {
        val existing = farmerRepository.findById(id)
            ?: throw NotFoundException("Farmer not found with id: $id")

        request.phone?.let { newPhone ->
            if (farmerRepository.existsByPhoneAndIdNot(newPhone, id))
                throw ConflictException("A farmer with phone '$newPhone' already exists")
        }
        request.nationalId?.let { newId ->
            if (farmerRepository.existsByNationalIdAndIdNot(newId, id))
                throw ConflictException("A farmer with national ID '$newId' already exists")
        }

        val newAgentId = request.agentId ?: existing.agentId
        val newLbcId = request.lbcId ?: existing.lbcId

        val agent = if (request.agentId != null || request.lbcId != null) {
            val a = agentRepository.findById(newAgentId)
                ?: throw NotFoundException("Agent not found with id: $newAgentId")
            val l = lbcRepository.findById(newLbcId)
                ?: throw NotFoundException("LBC not found with id: $newLbcId")
            if (a.lbcId != l.id)
                throw BadRequestException(
                    "Agent '${a.agentCode}' belongs to LBC '${a.lbcName}' not '${l.name}'. " +
                    "Please select an agent that belongs to the selected LBC."
                )
            a
        } else null

        val agentName = agent?.let { "${it.firstName} ${it.lastName}" } ?: existing.agentName
        val lbcName = if (request.lbcId != null) {
            lbcRepository.findById(newLbcId)?.name ?: existing.lbcName
        } else existing.lbcName

        val updated = existing.copy(
            firstName = request.firstName ?: existing.firstName,
            lastName = request.lastName ?: existing.lastName,
            phone = request.phone ?: existing.phone,
            email = request.email ?: existing.email,
            nationalId = request.nationalId ?: existing.nationalId,
            agentId = newAgentId,
            agentName = agentName,
            lbcId = newLbcId,
            lbcName = lbcName,
            region = request.region ?: existing.region,
            district = request.district ?: existing.district,
            cropTypes = request.cropTypes ?: existing.cropTypes,
            updatedAt = LocalDateTime.now()
        )
        val saved = farmerRepository.save(updated)
        val metrics = farmerRepository.findMetricsByFarmerIds(listOf(id))[id] ?: FarmerMetrics()
        return saved.toResponse(metrics)
    }
}

// ── Approve ───────────────────────────────────────────────────────────────────

@Service
class ApproveFarmerUseCase(private val farmerRepository: FarmerRepository) {

    @Transactional
    fun execute(id: UUID): FarmerResponse {
        val farmer = farmerRepository.findById(id)
            ?: throw NotFoundException("Farmer not found with id: $id")

        if (farmer.status != FarmerStatus.PENDING)
            throw BadRequestException("Only PENDING farmers can be approved. Current status: ${farmer.status.value}")

        val approved = farmer.copy(
            status = FarmerStatus.APPROVED,
            kycVerified = true,
            rejectionReason = null,
            updatedAt = LocalDateTime.now()
        )
        val saved = farmerRepository.save(approved)
        val metrics = farmerRepository.findMetricsByFarmerIds(listOf(id))[id] ?: FarmerMetrics()
        return saved.toResponse(metrics)
    }
}

// ── Reject ────────────────────────────────────────────────────────────────────

@Service
class RejectFarmerUseCase(private val farmerRepository: FarmerRepository) {

    @Transactional
    fun execute(id: UUID, request: RejectFarmerRequest): FarmerResponse {
        val farmer = farmerRepository.findById(id)
            ?: throw NotFoundException("Farmer not found with id: $id")

        if (farmer.status != FarmerStatus.PENDING)
            throw BadRequestException("Only PENDING farmers can be rejected. Current status: ${farmer.status.value}")

        val rejected = farmer.copy(
            status = FarmerStatus.REJECTED,
            rejectionReason = request.reason,
            updatedAt = LocalDateTime.now()
        )
        val saved = farmerRepository.save(rejected)
        val metrics = farmerRepository.findMetricsByFarmerIds(listOf(id))[id] ?: FarmerMetrics()
        return saved.toResponse(metrics)
    }
}

// ── Delete ────────────────────────────────────────────────────────────────────

@Service
class DeleteFarmerUseCase(private val farmerRepository: FarmerRepository) {

    @Transactional
    fun execute(id: UUID) {
        farmerRepository.findById(id)
            ?: throw NotFoundException("Farmer not found with id: $id")
        farmerRepository.delete(id)
    }
}

// ── Mapper ────────────────────────────────────────────────────────────────────

private fun Farmer.toResponse(metrics: FarmerMetrics) = FarmerResponse(
    id = id,
    farmerCode = farmerCode,
    firstName = firstName,
    lastName = lastName,
    fullName = "$firstName $lastName",
    phone = phone,
    email = email,
    nationalId = nationalId,
    agentId = agentId,
    agentName = agentName,
    lbcId = lbcId,
    lbcName = lbcName,
    region = region,
    district = district,
    farmsCount = metrics.farmsCount,
    totalFarmSize = metrics.totalFarmSize,
    cropTypes = metrics.cropTypes.ifEmpty { cropTypes },
    walletBalance = metrics.walletBalance,
    totalEarnings = metrics.totalEarnings,
    kycVerified = kycVerified,
    status = status,
    rejectionReason = rejectionReason,
    lat = latitude,
    lng = longitude,
    joinedDate = joinedDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)
