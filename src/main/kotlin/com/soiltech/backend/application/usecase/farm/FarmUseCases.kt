package com.soiltech.backend.application.usecase.farm

import com.soiltech.backend.application.dto.farm.AdminFarmListDto
import com.soiltech.backend.application.dto.farm.CreateFarmRequest
import com.soiltech.backend.application.dto.farm.FarmDto
import com.soiltech.backend.application.dto.farm.UpdateFarmRequest
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.Farm
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.AgentRepository
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
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    @Transactional
    fun execute(farmerId: UUID, request: CreateFarmRequest, userId: UUID): FarmDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
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
                estimatedYieldKg = request.estimatedYieldKg,
                lastHarvestDate = request.lastHarvestDate,
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
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    fun execute(farmerId: UUID, userId: UUID, page: Int, perPage: Int): Pair<List<FarmDto>, PaginationMeta> {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
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
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    @Transactional
    fun execute(farmerId: UUID, farmId: UUID, request: UpdateFarmRequest, userId: UUID): FarmDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
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
            estimatedYieldKg = request.estimatedYieldKg ?: farm.estimatedYieldKg,
            lastHarvestDate = request.lastHarvestDate ?: farm.lastHarvestDate,
            updatedAt = LocalDateTime.now()
        )
        return farmRepository.update(updated).toDto()
    }
}

@Service
class ListFarmsAdminUseCase(
    private val farmRepository: FarmRepository,
    private val farmerRepository: FarmerRepository
) {
    fun execute(
        region: String?,
        cropType: String?,
        search: String?,
        page: Int,
        perPage: Int
    ): Pair<List<AdminFarmListDto>, PaginationMeta> {
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val farmsPage = farmRepository.findAllAdmin(region, cropType, search, pageable)

        val farmerIds = farmsPage.content.map { it.farmerId }.distinct()
        val farmerMap = if (farmerIds.isNotEmpty()) {
            farmerRepository.findByIds(farmerIds).associateBy { it.id }
        } else emptyMap()

        val dtos = farmsPage.content.map { farm ->
            val farmer = farmerMap[farm.farmerId]
            AdminFarmListDto(
                farmId = farm.id,
                farmName = farm.name,
                farmerName = if (farmer != null) "${farmer.firstName} ${farmer.lastName}" else "Unknown",
                region = farmer?.region ?: "",
                district = farmer?.district ?: "",
                cropType = farm.cropType,
                sizeHectares = farm.sizeHectares,
                estimatedYieldKg = farm.estimatedYieldKg,
                lastHarvestDate = farm.lastHarvestDate,
                registeredDate = farm.createdAt
            )
        }
        return dtos to PaginationMeta.from(farmsPage, page, perPage)
    }
}
