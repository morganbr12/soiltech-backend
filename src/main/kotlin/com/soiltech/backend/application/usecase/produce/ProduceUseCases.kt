package com.soiltech.backend.application.usecase.produce

import com.soiltech.backend.application.dto.produce.CreateProduceRecordRequest
import com.soiltech.backend.application.dto.produce.ProduceRecordDto
import com.soiltech.backend.application.dto.produce.UpdateProduceRecordRequest
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.entity.Product
import com.soiltech.backend.domain.entity.ProductCategory
import com.soiltech.backend.domain.entity.ProduceListing
import com.soiltech.backend.domain.entity.ProduceRecord
import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.NotificationType
import com.soiltech.backend.domain.enum.ProduceListingStatus
import com.soiltech.backend.domain.enum.SyncStatus
import com.soiltech.backend.infrastructure.service.NotificationService
import com.soiltech.backend.domain.repository.AgentProfileRepository
import com.soiltech.backend.domain.repository.AgentRepository
import com.soiltech.backend.domain.repository.FarmerRepository
import com.soiltech.backend.domain.repository.ProduceListingRepository
import com.soiltech.backend.domain.repository.ProduceRecordRepository
import com.soiltech.backend.domain.repository.ProductCategoryRepository
import com.soiltech.backend.domain.repository.ProductRepository
import com.soiltech.backend.interfaces.exception.ForbiddenException
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class CreateProduceRecordUseCase(
    private val produceRecordRepository: ProduceRecordRepository,
    private val farmerRepository: FarmerRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository,
    private val produceListingRepository: ProduceListingRepository,
    private val productRepository: ProductRepository,
    private val productCategoryRepository: ProductCategoryRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val notificationService: NotificationService
) {
    @Transactional
    fun execute(request: CreateProduceRecordRequest, userId: UUID, photoUrls: List<String> = emptyList()): ProduceRecordDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
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
                photos = photoUrls,
                syncStatus = SyncStatus.SYNCED,
                createdAt = now,
                updatedAt = now
            )
        )

        val listing = produceListingRepository.save(
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
                status = ProduceListingStatus.PENDING_APPROVAL,
                region = farmer.region,
                district = farmer.district,
                agentName = "${agent.firstName} ${agent.lastName}",
                farmerName = "${farmer.firstName} ${farmer.lastName}",
                lbcName = farmer.lbcName,
                photos = photoUrls,
                collectedAt = record.collectedAt,
                createdAt = now,
                updatedAt = now
            )
        )

        autoCreateProduct(listing, now)

        notificationService.pushToAdmins(
            title = "New Produce Listing Pending Approval",
            body = "${agent.firstName} ${agent.lastName} submitted ${record.quantityKg}kg of ${record.cropType} for approval.",
            type = NotificationType.PRODUCE_SUBMITTED,
            referenceId = listing.id,
            referenceType = "PRODUCE_LISTING"
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

    private fun autoCreateProduct(listing: ProduceListing, now: LocalDateTime) {
        val categoryName = listing.cropType.trim()
            .replaceFirstChar { it.uppercase() }
        val category = productCategoryRepository.findByName(categoryName)
            ?: productCategoryRepository.save(
                ProductCategory(
                    id = UUID.randomUUID(),
                    name = categoryName,
                    description = "Fresh $categoryName sourced from local farmers",
                    createdAt = now,
                    updatedAt = now
                )
            )

        val productName = if (listing.cropVariety != null)
            "${listing.cropType} – ${listing.cropVariety}"
        else listing.cropType

        val location = listOfNotNull(listing.district, listing.region)
            .joinToString(", ")

        val description = buildString {
            append("Fresh ${listing.cropType}")
            if (!listing.farmerName.isNullOrBlank()) append(" from ${listing.farmerName}")
            if (location.isNotBlank()) append(". $location")
            if (listing.grade != null) append(". Grade: ${listing.grade}")
        }

        productRepository.save(
            Product(
                id = UUID.randomUUID(),
                categoryId = category.id,
                produceListingId = listing.id,
                name = productName,
                description = description,
                pricePerUnit = listing.pricePerKg,
                unit = "kg",
                stockQuantity = listing.availableQuantityKg.toInt(),
                isAvailable = listing.status == ProduceListingStatus.AVAILABLE,
                imageUrl = listing.photos.firstOrNull(),
                isOnDeal = false,
                isFeatured = false,
                originalPrice = null,
                farmerName = listing.farmerName,
                location = location.ifBlank { null },
                freshnessLabel = if (listing.grade != null) "Grade ${listing.grade}" else "Fresh",
                averageRating = BigDecimal.ZERO,
                reviewCount = 0,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}

@Service
class ListProduceRecordsUseCase(
    private val produceRecordRepository: ProduceRecordRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    fun execute(
        userId: UUID,
        farmerId: UUID?,
        status: CollectionStatus?,
        page: Int,
        perPage: Int
    ): Pair<List<ProduceRecordDto>, PaginationMeta> {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = produceRecordRepository.findAll(agent.id, farmerId, status, pageable)
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class GetProduceRecordUseCase(
    private val produceRecordRepository: ProduceRecordRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    fun execute(recordId: UUID, userId: UUID): ProduceRecordDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
        val record = produceRecordRepository.findById(recordId)
            ?: throw NotFoundException("Produce record not found")
        if (record.agentId != agent.id) throw ForbiddenException("Access denied")
        return record.toDto()
    }
}

@Service
class UpdateProduceRecordUseCase(
    private val produceRecordRepository: ProduceRecordRepository,
    private val agentProfileRepository: AgentProfileRepository,
    private val agentRepository: AgentRepository
) {
    @Transactional
    fun execute(recordId: UUID, request: UpdateProduceRecordRequest, userId: UUID): ProduceRecordDto {
        val profile = agentProfileRepository.findByUserId(userId)
            ?: throw NotFoundException("Agent profile not found")
        val agent = agentRepository.findByAgentCode(profile.agentCode)
            ?: throw NotFoundException("Agent record not found")
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
