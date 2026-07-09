package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.ProduceListing
import com.soiltech.backend.domain.enum.ProduceListingStatus
import com.soiltech.backend.domain.repository.ProduceListingRepository
import com.soiltech.backend.infrastructure.persistence.entity.ProduceListingJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.ProduceListingJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.util.UUID

@Component
class ProduceListingRepositoryAdapter(
    private val jpaRepository: ProduceListingJpaRepository
) : ProduceListingRepository {

    override fun save(listing: ProduceListing): ProduceListing =
        jpaRepository.save(ProduceListingJpaEntity.fromDomain(listing)).toDomain()

    override fun findById(id: UUID): ProduceListing? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByProduceRecordId(recordId: UUID): ProduceListing? =
        jpaRepository.findByProduceRecordId(recordId)?.toDomain()

    override fun findAllAvailable(
        cropType: String?,
        region: String?,
        district: String?,
        lbcId: UUID?,
        grade: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        minQuantity: BigDecimal?,
        pageable: Pageable
    ): Page<ProduceListing> = jpaRepository.findAllFiltered(
        ProduceListingStatus.AVAILABLE,
        cropType, region, district, lbcId, grade,
        minPrice, maxPrice, minQuantity, pageable
    ).map { it.toDomain() }

    override fun updateAvailableQuantity(
        id: UUID,
        availableQuantityKg: BigDecimal,
        status: ProduceListingStatus
    ): ProduceListing {
        val entity = jpaRepository.findById(id).orElseThrow()
        entity.availableQuantityKg = availableQuantityKg
        entity.status = status
        return jpaRepository.save(entity).toDomain()
    }

    override fun findAllAdmin(
        status: ProduceListingStatus?,
        cropType: String?,
        region: String?,
        search: String?,
        pageable: Pageable
    ): Page<ProduceListing> =
        jpaRepository.findAllAdmin(status, cropType, region, search, pageable).map { it.toDomain() }

    override fun findRecent(limit: Int): List<ProduceListing> =
        jpaRepository.findTop7ByOrderByCreatedAtDesc().take(limit).map { it.toDomain() }

    override fun computeFillRate(): Double =
        jpaRepository.computeFillRatePercent() ?: 0.0
}
