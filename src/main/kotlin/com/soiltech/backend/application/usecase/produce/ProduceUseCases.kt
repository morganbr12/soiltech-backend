package com.soiltech.backend.application.usecase.produce

import com.soiltech.backend.application.dto.produce.CreateProduceRecordRequest
import com.soiltech.backend.application.dto.produce.ProduceRecordDto
import com.soiltech.backend.application.dto.produce.UpdateProduceRecordRequest
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.ProduceListing
import com.soiltech.backend.domain.entity.ProduceRecord
import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.ProduceListingStatus
import com.soiltech.backend.domain.enum.SyncStatus
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.FarmerRepository
import com.soiltech.backend.domain.repository.ProduceListingRepository
import com.soiltech.backend.domain.repository.ProduceRecordRepository
import com.soiltech.backend.interfaces.exception.ForbiddenException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class CreateProduceRecordUseCase(
    private val produceRecordRepository: ProduceRecordRepository,
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val produceListingRepository: ProduceListingRepository,
    private val eventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun execute(request: CreateProduceRecordRequest, userId: UUID): ProduceRecordDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val farmer = farmerRepository.findById(request.farmerId)
            ?: throw NotFoundException("Farmer not found")
        if (farmer.agentId != agent.id) throw ForbiddenException("Access denied")

        val now = LocalDateTime.now()
        val totalAmount = request.quantityKg.multiply(request.pricePerKg)
        val record = produceRecordRepository.save(
            ProduceRecord(
                id = UUID.randomUUID(),
                farmerId = request.farmerId,
                farmId = request.farmId,
                agentId = agent.id,
                cropType = request.cropType,
                cropVariety = request.cropVariety,
                grade = request.grade,
                quantityKg = request.quantityKg,
                pricePerKg = request.pricePerKg,
                totalAmount = totalAmount,
                status = CollectionStatus.PENDING,
                collectedAt = request.collectedAt,
                notes = request.notes,
                syncStatus = SyncStatus.SYNCED,
                createdAt = now,
                updatedAt = now
            )
        )

        produceListingRepository.save(
            ProduceListing(
                id = UUID.randomUUID(),
                produceRecordId = record.id,
                farmerId = record.farmerId,
                farmId = record.farmId,
                agentId = record.agentId,
                lbcId = farmer.lbcId,
                cropType = record.cropType,
                cropVariety = record.cropVariety,
                grade = record.grade,
                totalQuantityKg = record.quantityKg,
                availableQuantityKg = record.quantityKg,
                pricePerKg = record.pricePerKg,
                status = ProduceListingStatus.AVAILABLE,
                region = farmer.region,
                district = farmer.district,
                agentName = agent.fullName,
                farmerName = "${farmer.firstName} ${farmer.lastName}",
                lbcName = farmer.lbcName,
                collectedAt = record.collectedAt,
                createdAt = now,
                updatedAt = now
            )
        )

        if (record.collectedAt != null) {
            eventPublisher.publishEvent(
                com.soiltech.backend.domain.event.ProduceCollectedEvent(
                    produceRecordId = record.id,
                    farmerId = record.farmerId,
                    agentId = record.agentId,
                    quantityKg = record.quantityKg,
                    totalAmount = record.totalAmount
                )
            )
        }

        return record.toDto()
    }
}

@Service
class ListProduceRecordsUseCase(
    private val produceRecordRepository: ProduceRecordRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    fun execute(
        userId: UUID,
        farmerId: UUID?,
        status: CollectionStatus?,
        page: Int,
        perPage: Int
    ): Pair<List<ProduceRecordDto>, PaginationMeta> {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = produceRecordRepository.findAll(agent.id, farmerId, status, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class GetProduceRecordUseCase(
    private val produceRecordRepository: ProduceRecordRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    fun execute(recordId: UUID, userId: UUID): ProduceRecordDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val record = produceRecordRepository.findById(recordId)
            ?: throw NotFoundException("Produce record not found")
        if (record.agentId != agent.id) throw ForbiddenException("Access denied")
        return record.toDto()
    }
}

@Service
class UpdateProduceRecordUseCase(
    private val produceRecordRepository: ProduceRecordRepository,
    private val agentProfileRepository: AgentProfileRepository
) {
    @Transactional
    fun execute(recordId: UUID, request: UpdateProduceRecordRequest, userId: UUID): ProduceRecordDto {
        val agent = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val record = produceRecordRepository.findById(recordId)
            ?: throw NotFoundException("Produce record not found")
        if (record.agentId != agent.id) throw ForbiddenException("Access denied")

        val newQty = request.quantityKg ?: record.quantityKg
        val newPrice = request.pricePerKg ?: record.pricePerKg
        val updated = record.copy(
            status = request.status ?: record.status,
            quantityKg = newQty,
            pricePerKg = newPrice,
            totalAmount = newQty.multiply(newPrice),
            notes = request.notes ?: record.notes,
            collectedAt = request.collectedAt ?: record.collectedAt,
            syncStatus = request.syncStatus ?: record.syncStatus,
            updatedAt = LocalDateTime.now()
        )
        return produceRecordRepository.update(updated).toDto()
    }
}
