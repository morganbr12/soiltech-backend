package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.Farm
import jakarta.persistence.*
import java.util.UUID

@Entity
@Table(
    name = "farms",
    indexes = [Index(name = "idx_farms_farmer_id", columnList = "farmerId")]
)
class FarmJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val farmerId: UUID,

    @Column(nullable = false, length = 255)
    var name: String,

    var sizeHectares: Double? = null,

    @Column(length = 100)
    var cropType: String? = null,

    @Column(length = 500)
    var location: String? = null,

    var latitude: Double? = null,
    var longitude: Double? = null,

    @Column(name = "photos", columnDefinition = "text")
    var photosRaw: String? = null
) : BaseJpaEntity() {


    fun toDomain(): Farm = Farm(
        id = id!!,
        farmerId = farmerId,
        name = name,
        sizeHectares = sizeHectares,
        cropType = cropType,
        location = location,
        latitude = latitude,
        longitude = longitude,
        photos = photosRaw?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(farm: Farm): FarmJpaEntity = FarmJpaEntity(
            id = farm.id,
            farmerId = farm.farmerId,
            name = farm.name,
            sizeHectares = farm.sizeHectares,
            cropType = farm.cropType,
            location = farm.location,
            latitude = farm.latitude,
            longitude = farm.longitude,
            photosRaw = farm.photos.joinToString(",").takeIf { it.isNotEmpty() }
        )
    }
}
