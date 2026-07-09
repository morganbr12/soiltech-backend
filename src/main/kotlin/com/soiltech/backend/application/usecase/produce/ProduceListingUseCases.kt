package com.soiltech.backend.application.usecase.produce

import com.soiltech.backend.application.dto.produce.ProduceListingDto
import com.soiltech.backend.application.mapper.toDto
import com.soiltech.backend.domain.enum.ProduceListingStatus
import com.soiltech.backend.domain.repository.ProduceListingRepository
import com.soiltech.backend.domain.repository.ProductRepository
import com.soiltech.backend.interfaces.exception.NotFoundException
import com.soiltech.backend.interfaces.response.PaginationMeta
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Service
class GetProduceListingsUseCase(
    private val produceListingRepository: ProduceListingRepository
) {
    fun execute(
        cropType: String?,
        region: String?,
        district: String?,
        lbcId: UUID?,
        grade: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        minQuantity: BigDecimal?,
        page: Int,
        perPage: Int
    ): Pair<List<ProduceListingDto>, PaginationMeta> {
        val pageable = PageRequest.of(page - 1, perPage, Sort.by("createdAt").descending())
        val result = produceListingRepository.findAllAvailable(
            cropType, region, district, lbcId, grade, minPrice, maxPrice, minQuantity, pageable
        )
        return result.content.map { it.toDto() } to PaginationMeta.from(result, page, perPage)
    }
}

@Service
class GetProduceListingUseCase(
    private val produceListingRepository: ProduceListingRepository
) {
    fun execute(id: UUID): ProduceListingDto =
        produceListingRepository.findById(id)?.toDto()
            ?: throw NotFoundException("Produce listing not found")
}

@Service
class SyncProduceListingInventoryUseCase(
    private val produceListingRepository: ProduceListingRepository,
    private val productRepository: ProductRepository
) {
    @Transactional
    fun reserve(listingId: UUID, quantityKg: BigDecimal): ProduceListingDto {
        val listing = produceListingRepository.findById(listingId)
            ?: throw NotFoundException("Produce listing not found")
        val newAvailable = listing.availableQuantityKg.subtract(quantityKg)
        val newStatus = when {
            newAvailable <= BigDecimal.ZERO -> ProduceListingStatus.SOLD_OUT
            else -> ProduceListingStatus.AVAILABLE
        }
        val updated = produceListingRepository.updateAvailableQuantity(
            listingId, newAvailable.max(BigDecimal.ZERO), newStatus
        )
        syncProductStock(listingId, updated.availableQuantityKg, newStatus)
        return updated.toDto()
    }

    @Transactional
    fun restore(listingId: UUID, quantityKg: BigDecimal): ProduceListingDto {
        val listing = produceListingRepository.findById(listingId)
            ?: throw NotFoundException("Produce listing not found")
        val newAvailable = listing.availableQuantityKg.add(quantityKg).min(listing.totalQuantityKg)
        val newStatus = if (newAvailable > BigDecimal.ZERO) ProduceListingStatus.AVAILABLE else ProduceListingStatus.SOLD_OUT
        val updated = produceListingRepository.updateAvailableQuantity(listingId, newAvailable, newStatus)
        syncProductStock(listingId, newAvailable, newStatus)
        return updated.toDto()
    }

    private fun syncProductStock(listingId: UUID, availableQty: BigDecimal, status: ProduceListingStatus) {
        productRepository.findByProduceListingId(listingId)?.let { product ->
            productRepository.save(
                product.copy(
                    stockQuantity = availableQty.toInt(),
                    isAvailable = status == ProduceListingStatus.AVAILABLE,
                    updatedAt = LocalDateTime.now()
                )
            )
        }
    }
}
