package com.soiltech.backend.application.usecase.agent

import com.soiltech.backend.application.dto.agent.*
import com.soiltech.backend.domain.entity.Agent
import com.soiltech.backend.domain.entity.AgentMetrics
import com.soiltech.backend.domain.entity.AgentProfile
import com.soiltech.backend.domain.entity.User
import com.soiltech.backend.domain.enum.AgentStatus
import com.soiltech.backend.domain.enum.UserRole
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.AgentRepository
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.interfaces.exception.ConflictException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

// ── Get profile (mobile-facing) ───────────────────────────────────────────────

@Service
class GetAgentProfileUseCase(
    private val agentProfileRepository: AgentProfileRepository
) {
    fun execute(userId: UUID): AgentProfileDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        return AgentProfileDto(
            id = profile.id,
            userId = profile.userId,
            fullName = profile.fullName,
            firstName = profile.fullName.substringBefore(" ").ifBlank { profile.fullName },
            lastName = profile.fullName.substringAfter(" ", ""),
            agentCode = profile.agentCode,
            region = profile.region,
            createdAt = profile.createdAt,
            updatedAt = profile.updatedAt
        )
    }
}

// ── List (admin) ──────────────────────────────────────────────────────────────

@Service
class ListAgentsUseCase(private val agentRepository: AgentRepository) {

    fun execute(
        status: AgentStatus?,
        region: String?,
        search: String?,
        page: Int,
        perPage: Int,
        sortBy: String,
        sortOrder: String
    ): Triple<List<AgentResponse>, AgentSummaryResponse, PaginationMeta> {

        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val sortField = SORTABLE_FIELDS.getOrDefault(sortBy, "createdAt")
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), perPage.coerceIn(1, 100), Sort.by(direction, sortField))

        val resultPage = agentRepository.findAll(status, region, search, pageable)
        val counts = agentRepository.countByStatus()

        val ids = resultPage.content.map { it.id }
        val metricsMap = agentRepository.findMetricsByAgentIds(ids)

        val items = resultPage.content.map { it.toResponse(metricsMap[it.id] ?: AgentMetrics()) }

        val activeCount = counts.getOrDefault(AgentStatus.ACTIVE, 0L).toInt()
        val inactiveCount = counts.getOrDefault(AgentStatus.INACTIVE, 0L).toInt()
        val suspendedCount = counts.getOrDefault(AgentStatus.SUSPENDED, 0L).toInt()
        val summary = AgentSummaryResponse(
            total = activeCount + inactiveCount + suspendedCount,
            active = activeCount,
            inactive = inactiveCount,
            suspended = suspendedCount
        )
        val meta = PaginationMeta.from(resultPage, page, perPage)

        return Triple(items, summary, meta)
    }

    companion object {
        val SORTABLE_FIELDS = mapOf(
            "firstName" to "firstName",
            "lastName" to "lastName",
            "email" to "email",
            "phone" to "phone",
            "region" to "region",
            "district" to "district",
            "status" to "status",
            "joinedDate" to "joinedDate",
            "createdAt" to "createdAt",
            "updatedAt" to "updatedAt"
        )
    }
}

// ── Get single (admin) ────────────────────────────────────────────────────────

@Service
class GetAgentUseCase(private val agentRepository: AgentRepository) {

    fun execute(id: UUID): AgentResponse {
        val agent = agentRepository.findById(id)
            ?: throw NotFoundException("Agent not found with id: $id")
        val metrics = agentRepository.findMetricsByAgentIds(listOf(id))[id] ?: AgentMetrics()
        return agent.toResponse(metrics)
    }
}

// ── Register (admin) ──────────────────────────────────────────────────────────

@Service
class RegisterAgentUseCase(
    private val agentRepository: AgentRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun execute(request: RegisterAgentRequest): AgentResponse {
        if (agentRepository.existsByEmail(request.email))
            throw ConflictException("Agent with email '${request.email}' already exists")
        if (agentRepository.existsByPhone(request.phone))
            throw ConflictException("Agent with phone '${request.phone}' already exists")
        if (userRepository.existsByEmail(request.email))
            throw ConflictException("A user account with email '${request.email}' already exists")
        if (userRepository.existsByPhone(request.phone))
            throw ConflictException("A user account with phone '${request.phone}' already exists")

        val now = LocalDateTime.now()
        val agentCode = generateUniqueCode(agentRepository)

        val user = userRepository.save(
            User(
                id = UUID.randomUUID(),
                email = request.email,
                phone = request.phone,
                passwordHash = passwordEncoder.encode(request.password),
                role = UserRole.AGENT,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )

        agentProfileRepository.save(
            AgentProfile(
                id = UUID.randomUUID(),
                userId = user.id,
                fullName = "${request.firstName} ${request.lastName}",
                agentCode = agentCode,
                region = request.region,
                createdAt = now,
                updatedAt = now
            )
        )

        val agent = agentRepository.save(
            Agent(
                id = UUID.randomUUID(),
                firstName = request.firstName,
                lastName = request.lastName,
                phone = request.phone,
                email = request.email,
                agentCode = agentCode,
                lbcId = request.lbcId,
                lbcName = "",
                region = request.region,
                district = request.district,
                status = AgentStatus.ACTIVE,
                latitude = null,
                longitude = null,
                lastSeen = null,
                joinedDate = now,
                createdAt = now,
                updatedAt = now,
                createdBy = null,
                updatedBy = null
            )
        )
        return agent.toResponse(AgentMetrics())
    }

    private fun generateUniqueCode(repo: AgentRepository): String {
        var code: String
        do { code = "AGT-${(100000..999999).random()}" } while (repo.existsByAgentCode(code))
        return code
    }
}

// ── Update (admin) ────────────────────────────────────────────────────────────

@Service
class UpdateAgentUseCase(private val agentRepository: AgentRepository) {

    @Transactional
    fun execute(id: UUID, request: UpdateAgentRequest): AgentResponse {
        val existing = agentRepository.findById(id)
            ?: throw NotFoundException("Agent not found with id: $id")

        request.email?.let { newEmail ->
            if (agentRepository.existsByEmailAndIdNot(newEmail, id))
                throw ConflictException("Agent with email '$newEmail' already exists")
        }
        request.phone?.let { newPhone ->
            if (agentRepository.existsByPhoneAndIdNot(newPhone, id))
                throw ConflictException("Agent with phone '$newPhone' already exists")
        }

        val updated = existing.copy(
            firstName = request.firstName ?: existing.firstName,
            lastName = request.lastName ?: existing.lastName,
            phone = request.phone ?: existing.phone,
            email = request.email ?: existing.email,
            lbcId = request.lbcId ?: existing.lbcId,
            region = request.region ?: existing.region,
            district = request.district ?: existing.district,
            status = request.status ?: existing.status,
            updatedAt = LocalDateTime.now()
        )
        val saved = agentRepository.save(updated)
        val metrics = agentRepository.findMetricsByAgentIds(listOf(id))[id] ?: AgentMetrics()
        return saved.toResponse(metrics)
    }
}

// ── Delete (admin) ────────────────────────────────────────────────────────────

@Service
class DeleteAgentUseCase(private val agentRepository: AgentRepository) {

    @Transactional
    fun execute(id: UUID) {
        agentRepository.findById(id)
            ?: throw NotFoundException("Agent not found with id: $id")
        agentRepository.delete(id)
    }
}

// ── Mapper ────────────────────────────────────────────────────────────────────

private fun Agent.toResponse(metrics: AgentMetrics) = AgentResponse(
    id = id,
    firstName = firstName,
    lastName = lastName,
    fullName = "$firstName $lastName",
    phone = phone,
    email = email,
    agentCode = agentCode,
    lbcId = lbcId,
    lbcName = lbcName,
    region = region,
    district = district,
    farmersCount = metrics.farmersCount,
    farmsCount = metrics.farmsCount,
    produceCollected = metrics.produceCollected,
    status = status,
    lat = latitude,
    lng = longitude,
    lastSeen = lastSeen,
    joinedDate = joinedDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)
