package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.ProduceListing
import com.soiltech.backend.domain.enum.ProduceListingStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "produce_listings",
    indexes = [
        Index(name = "idx_listings_status", columnList = "status"),
        Index(name = "idx_listings_crop_type", columnList = "cropType"),
        Index(name = "idx_listings_region", columnList = "region"),
        Index(name = "idx_listings_lbc_id", columnList = "lbcId"),
        Index(name = "idx_listings_produce_record_id", columnList = "produceRecordId", unique = true)
    ]
)
class ProduceListingJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false, unique = true)
    val produceRecordId: UUID,

    @Column(nullable = false)
    val farmerId: UUID,

    val farmId: UUID? = null,

    @Column(nullable = false)
    val agentId: UUID,

    val lbcId: UUID? = null,

    @Column(nullable = false, length = 100)
    var cropType: String,

    @Column(length = 100)
    var cropVariety: String? = null,

    @Column(length = 50)
    var grade: String? = null,

    @Column(nullable = false, precision = 10, scale = 3)
    var totalQuantityKg: BigDecimal,

    @Column(nullable = false, precision = 10, scale = 3)
    var availableQuantityKg: BigDecimal,

    @Column(nullable = false, precision = 12, scale = 2)
    var pricePerKg: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ProduceListingStatus = ProduceListingStatus.AVAILABLE,

    @Column(length = 100)
    var region: String? = null,

    @Column(length = 100)
    var district: String? = null,

    @Column(length = 255)
    var agentName: String? = null,

    @Column(length = 255)
    var farmerName: String? = null,

    @Column(length = 255)
    var lbcName: String? = null,

    @Column(name = "photos", columnDefinition = "text")
    var photosRaw: String? = null,

    var collectedAt: LocalDateTime? = null
) : BaseJpaEntity() {

    fun toDomain(): ProduceListing = ProduceListing(
        id = id!!,
        produceRecordId = produceRecordId,
        farmerId = farmerId,
        farmId = farmId,
        agentId = agentId,
        lbcId = lbcId,
        cropType = cropType,
        cropVariety = cropVariety,
        grade = grade,
        totalQuantityKg = totalQuantityKg,
        availableQuantityKg = availableQuantityKg,
        pricePerKg = pricePerKg,
        status = status,
        region = region,
        district = district,
        agentName = agentName,
        farmerName = farmerName,
        lbcName = lbcName,
        photos = photosRaw?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        collectedAt = collectedAt,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(listing: ProduceListing): ProduceListingJpaEntity = ProduceListingJpaEntity(
            id = listing.id,
            produceRecordId = listing.produceRecordId,
            farmerId = listing.farmerId,
            farmId = listing.farmId,
            agentId = listing.agentId,
            lbcId = listing.lbcId,
            cropType = listing.cropType,
            cropVariety = listing.cropVariety,
            grade = listing.grade,
            totalQuantityKg = listing.totalQuantityKg,
            availableQuantityKg = listing.availableQuantityKg,
            pricePerKg = listing.pricePerKg,
            status = listing.status,
            region = listing.region,
            district = listing.district,
            agentName = listing.agentName,
            farmerName = listing.farmerName,
            lbcName = listing.lbcName,
            photosRaw = listing.photos.joinToString(",").takeIf { it.isNotEmpty() },
            collectedAt = listing.collectedAt
        )
    }
}
