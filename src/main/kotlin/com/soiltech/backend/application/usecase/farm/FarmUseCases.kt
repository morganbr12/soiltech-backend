package com.soiltech.backend.application.usecase.farm

import com.soiltech.backend.application.dto.farm.CreateFarmRequest
import com.soiltech.backend.application.dto.farm.FarmDto
import com.soiltech.backend.application.dto.farm.UpdateFarmRequest
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.Farm
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.FarmRepository
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
class CreateFarmUseCase(
    private val farmRepository: FarmRepository,
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    @Transactional
    fun execute(farmerId: UUID, request: CreateFarmRequest, userId: UUID): FarmDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val farmer = farmerRepository.findById(farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agent.id) throw ForbiddenException("Access denied")

        val now = LocalDateTime.now()
        val farm = farmRepository.save(
            Farm(
                id = UUID.randomUUID(),
                farmerId = farmerId,
                name = request.name,
                sizeHectares = request.sizeHectares,
                cropType = request.cropType,
                location = request.location,
                latitude = request.latitude,
                longitude = request.longitude,
                createdAt = now,
                updatedAt = now
            )
        )
        return farm.toDto()
    }
}

@Service
class ListFarmsUseCase(
    private val farmRepository: FarmRepository,
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    fun execute(farmerId: UUID, userId: UUID, page: Int, perPage: Int): Pair<List<FarmDto>, PaginationMeta> {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val farmer = farmerRepository.findById(farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agent.id) throw ForbiddenException("Access denied")

        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = farmRepository.findByFarmerId(farmerId, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class UpdateFarmUseCase(
    private val farmRepository: FarmRepository,
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    @Transactional
    fun execute(farmerId: UUID, farmId: UUID, request: UpdateFarmRequest, userId: UUID): FarmDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val farmer = farmerRepository.findById(farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agent.id) throw ForbiddenException("Access denied")

        val farm = farmRepository.findById(farmId)
            ?: throw NotFoundException("Farm not found")
        if (farm.farmerId != farmerId) throw ForbiddenException("Farm does not belong to farmer")

        val updated = farm.copy(
            name = request.name ?: farm.name,
            sizeHectares = request.sizeHectares ?: farm.sizeHectares,
            cropType = request.cropType ?: farm.cropType,
            location = request.location ?: farm.location,
            latitude = request.latitude ?: farm.latitude,
            longitude = request.longitude ?: farm.longitude,
            updatedAt = LocalDateTime.now()
        )
        return farmRepository.update(updated).toDto()
    }
}
