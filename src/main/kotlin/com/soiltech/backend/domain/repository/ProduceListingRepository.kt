package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.ProduceListing
import com.soiltech.backend.domain.enum.ProduceListingStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.util.UUID

interface ProduceListingRepository {
    fun save(listing: ProduceListing): ProduceListing
    fun findById(id: UUID): ProduceListing?
    fun findByProduceRecordId(recordId: UUID): ProduceListing?
    fun findAllAvailable(
        cropType: String?,
        region: String?,
        district: String?,
        lbcId: UUID?,
        grade: String?,
        minPrice: BigDecimal?,
        maxPrice: BigDecimal?,
        minQuantity: BigDecimal?,
        pageable: Pageable
    ): Page<ProduceListing>
    fun updateAvailableQuantity(id: UUID, availableQuantityKg: BigDecimal, status: ProduceListingStatus): ProduceListing

    // Admin
    fun findAllAdmin(
        status: ProduceListingStatus?,
        cropType: String?,
        region: String?,
        search: String?,
        pageable: org.springframework.data.domain.Pageable
    ): org.springframework.data.domain.Page<ProduceListing>
    fun findRecent(limit: Int): List<ProduceListing>
    fun computeFillRate(): Double
}
