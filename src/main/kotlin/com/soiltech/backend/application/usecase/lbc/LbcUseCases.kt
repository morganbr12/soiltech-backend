package com.soiltech.backend.application.usecase.lbc

import com.soiltech.backend.application.dto.lbc.*
import com.soiltech.backend.domain.entity.Lbc
import com.soiltech.backend.domain.entity.User
import com.soiltech.backend.domain.enum.LbcStatus
import com.soiltech.backend.domain.enum.UserRole
import com.soiltech.backend.domain.repository.LbcRepository
import com.soiltech.backend.domain.repository.UserRepository
import com.soiltech.backend.interfaces.exception.BadRequestException
import com.soiltech.backend.interfaces.exception.ConflictException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

// ── List ──────────────────────────────────────────────────────────────────────

@Service
class ListLbcsUseCase(private val lbcRepository: LbcRepository) {

    fun execute(
        status: LbcStatus?,
        region: String?,
        search: String?,
        page: Int,
        perPage: Int,
        sortBy: String,
        sortOrder: String
    ): Triple<List<LbcResponse>, LbcSummaryResponse, PaginationMeta> {

        val direction = if (sortOrder.equals("asc", ignoreCase = true)) Sort.Direction.ASC else Sort.Direction.DESC
        val sortField = SORTABLE_FIELDS.getOrDefault(sortBy, "createdAt")
        val pageable = PageRequest.of((page - 1).coerceAtLeast(0), perPage.coerceIn(1, 100), Sort.by(direction, sortField))

        val resultPage = lbcRepository.findAll(status, region, search, pageable)
        val counts = lbcRepository.countByStatus()

        val items = resultPage.content.map { it.toResponse() }
        val activeCount = counts.getOrDefault(LbcStatus.ACTIVE, 0L).toInt()
        val pendingCount = counts.getOrDefault(LbcStatus.PENDING, 0L).toInt()
        val suspendedCount = counts.getOrDefault(LbcStatus.SUSPENDED, 0L).toInt()
        val inactiveCount = counts.getOrDefault(LbcStatus.INACTIVE, 0L).toInt()
        val summary = LbcSummaryResponse(
            total = activeCount + pendingCount + suspendedCount + inactiveCount,
            active = activeCount,
            pending = pendingCount,
            suspended = suspendedCount,
            inactive = inactiveCount
        )
        val meta = PaginationMeta.from(resultPage, page, perPage)

        return Triple(items, summary, meta)
    }

    companion object {
        val SORTABLE_FIELDS = mapOf(
            "name" to "name",
            "code" to "code",
            "region" to "region",
            "district" to "district",
            "manager" to "manager",
            "agents" to "agents",
            "farmers" to "farmers",
            "produceTonnes" to "produceTonnes",
            "revenue" to "revenue",
            "compliance" to "compliance",
            "status" to "status",
            "joinedDate" to "joinedDate",
            "createdAt" to "createdAt",
            "updatedAt" to "updatedAt"
        )
    }
}

// ── Get single ────────────────────────────────────────────────────────────────

@Service
class GetLbcUseCase(private val lbcRepository: LbcRepository) {

    fun execute(id: UUID): LbcResponse =
        lbcRepository.findById(id)?.toResponse()
            ?: throw NotFoundException("LBC not found with id: $id")
}

// ── Create ────────────────────────────────────────────────────────────────────

@Service
class CreateLbcUseCase(
    private val lbcRepository: LbcRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun execute(request: CreateLbcRequest): LbcResponse {
        if (lbcRepository.existsByCode(request.code))
            throw ConflictException("LBC with code '${request.code}' already exists")
        if (lbcRepository.existsByEmail(request.email))
            throw ConflictException("LBC with email '${request.email}' already exists")
        if (userRepository.existsByEmail(request.email))
            throw ConflictException("A user account with email '${request.email}' already exists")
        if (userRepository.existsByPhone(request.phone))
            throw ConflictException("A user account with phone '${request.phone}' already exists")

        val now = LocalDateTime.now()

        val user = userRepository.save(
            User(
                id = UUID.randomUUID(),
                email = request.email,
                phone = request.phone,
                passwordHash = passwordEncoder.encode(request.password),
                role = UserRole.LBC,
                isActive = true,
                createdAt = now,
                updatedAt = now
            )
        )

        val lbc = lbcRepository.save(
            Lbc(
                id = UUID.randomUUID(),
                userId = user.id,
                name = request.name,
                code = request.code,
                region = request.region,
                district = request.district,
                manager = request.manager,
                phone = request.phone,
                email = request.email,
                agents = 0,
                farmers = 0,
                produceTonnes = BigDecimal.ZERO,
                revenue = BigDecimal.ZERO,
                compliance = 0,
                status = LbcStatus.ACTIVE,
                joinedDate = now,
                createdAt = now,
                updatedAt = now,
                createdBy = null,
                updatedBy = null
            )
        )
        return lbc.toResponse()
    }
}

// ── Update ────────────────────────────────────────────────────────────────────

@Service
class UpdateLbcUseCase(private val lbcRepository: LbcRepository) {

    @Transactional
    fun execute(id: UUID, request: UpdateLbcRequest): LbcResponse {
        val existing = lbcRepository.findById(id)
            ?: throw NotFoundException("LBC not found with id: $id")

        request.code?.let { newCode ->
            if (lbcRepository.existsByCodeAndIdNot(newCode, id))
                throw ConflictException("LBC with code '$newCode' already exists")
        }
        request.email?.let { newEmail ->
            if (lbcRepository.existsByEmailAndIdNot(newEmail, id))
                throw ConflictException("LBC with email '$newEmail' already exists")
        }

        val updated = existing.copy(
            name = request.name ?: existing.name,
            code = request.code ?: existing.code,
            region = request.region ?: existing.region,
            district = request.district ?: existing.district,
            manager = request.manager ?: existing.manager,
            phone = request.phone ?: existing.phone,
            email = request.email ?: existing.email,
            agents = request.agents ?: existing.agents,
            farmers = request.farmers ?: existing.farmers,
            produceTonnes = request.produceTonnes ?: existing.produceTonnes,
            revenue = request.revenue ?: existing.revenue,
            compliance = request.compliance ?: existing.compliance,
            status = request.status ?: existing.status,
            joinedDate = request.joinedDate ?: existing.joinedDate,
            updatedAt = LocalDateTime.now()
        )
        return lbcRepository.save(updated).toResponse()
    }
}

// ── Suspend single ────────────────────────────────────────────────────────────

@Service
class SuspendLbcUseCase(private val lbcRepository: LbcRepository) {

    @Transactional
    fun execute(id: UUID): LbcResponse {
        val lbc = lbcRepository.findById(id)
            ?: throw NotFoundException("LBC not found with id: $id")

        if (lbc.status != LbcStatus.ACTIVE)
            throw BadRequestException("Only ACTIVE LBCs can be suspended. Current status: ${lbc.status.value}")

        val suspended = lbc.copy(status = LbcStatus.SUSPENDED, updatedAt = LocalDateTime.now())
        return lbcRepository.save(suspended).toResponse()
    }
}

// ── Delete ────────────────────────────────────────────────────────────────────

@Service
class DeleteLbcUseCase(private val lbcRepository: LbcRepository) {

    @Transactional
    fun execute(id: UUID) {
        val lbc = lbcRepository.findById(id)
            ?: throw NotFoundException("LBC not found with id: $id")

        if (lbc.agents > 0 || lbc.farmers > 0)
            throw BadRequestException(
                "Cannot delete LBC '${lbc.name}': it has ${lbc.agents} agent(s) and ${lbc.farmers} farmer(s) linked to it"
            )

        lbcRepository.delete(id)
    }
}

// ── Export ────────────────────────────────────────────────────────────────────

@Service
class ExportLbcsUseCase(private val lbcRepository: LbcRepository) {

    fun execute(status: LbcStatus?, region: String?, ids: List<UUID>?): ByteArray {
        val lbcs = lbcRepository.findAllForExport(status, region, ids)
        return buildCsv(lbcs)
    }

    private fun buildCsv(lbcs: List<com.soiltech.backend.domain.entity.Lbc>): ByteArray {
        val sb = StringBuilder()
        sb.appendLine("ID,Name,Code,Region,District,Manager,Phone,Email,Agents,Farmers,Produce Tonnes,Revenue,Compliance (%),Status,Joined Date,Created At")
        lbcs.forEach { lbc ->
            sb.appendLine(
                "${lbc.id},${csv(lbc.name)},${csv(lbc.code)},${csv(lbc.region)},${csv(lbc.district)}," +
                "${csv(lbc.manager)},${csv(lbc.phone)},${csv(lbc.email)},${lbc.agents},${lbc.farmers}," +
                "${lbc.produceTonnes},${lbc.revenue},${lbc.compliance},${lbc.status.value}," +
                "${lbc.joinedDate},${lbc.createdAt}"
            )
        }
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun csv(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n'))
            "\"${value.replace("\"", "\"\"")}\""
        else value
}

// ── Bulk suspend ──────────────────────────────────────────────────────────────

@Service
class BulkSuspendLbcsUseCase(private val lbcRepository: LbcRepository) {

    @Transactional
    fun execute(request: BulkSuspendRequest): BulkSuspendResponse {
        val succeeded = mutableListOf<String>()
        val skipped = mutableListOf<String>()
        val failures = mutableListOf<BulkSuspendFailure>()

        request.ids.forEach { id ->
            val idStr = id.toString()
            val lbc = lbcRepository.findById(id)
            when {
                lbc == null -> failures.add(BulkSuspendFailure(idStr, "LBC not found"))
                lbc.status != LbcStatus.ACTIVE -> skipped.add(idStr)
                else -> {
                    lbcRepository.save(lbc.copy(status = LbcStatus.SUSPENDED, updatedAt = LocalDateTime.now()))
                    succeeded.add(idStr)
                }
            }
        }

        return BulkSuspendResponse(succeeded = succeeded, skipped = skipped, failures = failures)
    }
}

// ── Mapper ────────────────────────────────────────────────────────────────────

private fun Lbc.toResponse() = LbcResponse(
    id = id.toString(),
    name = name,
    code = code,
    region = region,
    district = district,
    manager = manager,
    phone = phone,
    email = email,
    agents = agents,
    farmers = farmers,
    produceTonnes = produceTonnes,
    revenue = revenue,
    compliance = compliance,
    status = status,
    joinedDate = joinedDate,
    createdAt = createdAt,
    updatedAt = updatedAt
)
