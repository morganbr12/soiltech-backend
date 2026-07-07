package com.soiltech.backend.application.usecase.farmer

import com.soiltech.backend.application.dto.farmer.CreateFarmerRequest
import com.soiltech.backend.application.dto.farmer.FarmerDto
import com.soiltech.backend.application.dto.farmer.UpdateFarmerRequest
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.Farmer
import com.soiltech.backend.domain.enum.FarmerStatus
import com.soiltech.backend.domain.enum.SyncStatus
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.FarmerRepository
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
class CreateFarmerUseCase(
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    @Transactional
    fun execute(request: CreateFarmerRequest, userId: UUID): FarmerDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")

        val now = LocalDateTime.now()
        val farmer = farmerRepository.save(
            Farmer(
                id = UUID.randomUUID(),
                agentId = agent.id,
                fullName = request.fullName,
                phone = request.phone,
                nationalId = request.nationalId,
                location = request.location,
                status = FarmerStatus.ACTIVE,
                syncStatus = SyncStatus.SYNCED,
                createdAt = now,
                updatedAt = now
            )
        )
        return farmer.toDto()
    }
}

@Service
class GetFarmerUseCase(
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    fun execute(farmerId: UUID, userId: UUID): FarmerDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val farmer = farmerRepository.findById(farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agent.id) throw ForbiddenException("Access denied")
        return farmer.toDto()
    }
}

@Service
class ListFarmersUseCase(
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    fun execute(
        userId: UUID,
        status: FarmerStatus?,
        query: String?,
        page: Int,
        perPage: Int
    ): Pair<List<FarmerDto>, PaginationMeta> {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = farmerRepository.findAll(agent.id, status, query, pageable)
        val meta = PaginationMeta.from(result, page, perPage)
        return result.content.map { it.toDto() } to meta
    }
}

@Service
class UpdateFarmerUseCase(
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    @Transactional
    fun execute(farmerId: UUID, request: UpdateFarmerRequest, userId: UUID): FarmerDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val farmer = farmerRepository.findById(farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agent.id) throw ForbiddenException("Access denied")

        val updated = farmer.copy(
            fullName = request.fullName ?: farmer.fullName,
            phone = request.phone ?: farmer.phone,
            nationalId = request.nationalId ?: farmer.nationalId,
            location = request.location ?: farmer.location,
            status = request.status ?: farmer.status,
            updatedAt = LocalDateTime.now()
        )
        return farmerRepository.update(updated).toDto()
    }
}

@Service
class DeleteFarmerUseCase(
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    @Transactional
    fun execute(farmerId: UUID, userId: UUID) {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val farmer = farmerRepository.findById(farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agent.id) throw ForbiddenException("Access denied")
        farmerRepository.delete(farmerId)
    }
}
