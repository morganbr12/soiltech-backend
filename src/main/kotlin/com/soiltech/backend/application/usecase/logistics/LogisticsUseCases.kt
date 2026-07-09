package com.soiltech.backend.application.usecase.logistics

import com.soiltech.backend.application.dto.logistics.CreatePickupRequestRequest
import com.soiltech.backend.application.dto.logistics.PickupRequestDto
import com.soiltech.backend.application.dto.logistics.UpdatePickupRequestRequest
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.PickupRequest
import com.soiltech.backend.domain.enum.LogisticsStatus
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.AgentRepository
import com.soiltech.backend.domain.repository.FarmerRepository
import com.soiltech.backend.domain.repository.PickupRequestRepository
import com.soiltech.backend.interfaces.exception.ForbiddenException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class CreatePickupRequestUseCase(
    private val pickupRequestRepository: PickupRequestRepository,
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    @Transactional
    fun execute(request: CreatePickupRequestRequest, userId: UUID): PickupRequestDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
        val farmer = farmerRepository.findById(request.farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agent.id) throw ForbiddenException("Access denied")

        val now = LocalDateTime.now()
        val pickup = pickupRequestRepository.save(
            PickupRequest(
                id = UUID.randomUUID(),
                farmerId = request.farmerId,
                agentId = agent.id,
                produceRecordId = request.produceRecordId,
                scheduledDate = request.scheduledDate,
                status = LogisticsStatus.PENDING,
                notes = request.notes,
                createdAt = now,
                updatedAt = now
            )
        )
        return pickup.toDto()
    }
}

@Service
class ListPickupRequestsUseCase(
    private val pickupRequestRepository: PickupRequestRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    fun execute(
        userId: UUID,
        farmerId: UUID?,
        status: LogisticsStatus?,
        page: Int,
        perPage: Int
    ): Pair<List<PickupRequestDto>, PaginationMeta> {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("scheduledDate").descending())
        val result = pickupRequestRepository.findAll(agent.id, farmerId, status, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class GetPickupRequestUseCase(
    private val pickupRequestRepository: PickupRequestRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    fun execute(requestId: UUID, userId: UUID): PickupRequestDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
        val request = pickupRequestRepository.findById(requestId)
            ?: throw NotFoundException("Pickup request not found")
        if (request.agentId != agent.id) throw ForbiddenException("Access denied")
        return request.toDto()
    }
}

@Service
class UpdatePickupRequestUseCase(
    private val pickupRequestRepository: PickupRequestRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    @Transactional
    fun execute(requestId: UUID, request: UpdatePickupRequestRequest, userId: UUID): PickupRequestDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
        val pickup = pickupRequestRepository.findById(requestId)
            ?: throw NotFoundException("Pickup request not found")
        if (pickup.agentId != agent.id) throw ForbiddenException("Access denied")

        val updated = pickup.copy(
            scheduledDate = request.scheduledDate ?: pickup.scheduledDate,
            status = request.status ?: pickup.status,
            notes = request.notes ?: pickup.notes,
            updatedAt = LocalDateTime.now()
        )
        return pickupRequestRepository.update(updated).toDto()
    }
}
