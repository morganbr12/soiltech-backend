package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.VehicleLocation
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "vehicle_locations",
    indexes = [Index(name = "idx_vehicle_locations_vehicle_id", columnList = "vehicle_id")]
)
class VehicleLocationJpaEntity(
    @Id
    val id: UUID,

    @Column(name = "vehicle_id", unique = true, nullable = false)
    val vehicleId: UUID,

    @Column(nullable = false)
    var lat: Double,

    @Column(nullable = false)
    var lng: Double,

    @Column(nullable = false)
    var speed: Double = 0.0,

    @Column(nullable = false)
    var heading: Double = 0.0,

    @Column(name = "last_updated", nullable = false)
    var lastUpdated: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain() = VehicleLocation(
        id = id,
        vehicleId = vehicleId,
        lat = lat,
        lng = lng,
        speed = speed,
        heading = heading,
        lastUpdated = lastUpdated
    )

    companion object {
        fun fromDomain(v: VehicleLocation) = VehicleLocationJpaEntity(
            id = v.id,
            vehicleId = v.vehicleId,
            lat = v.lat,
            lng = v.lng,
            speed = v.speed,
            heading = v.heading,
            lastUpdated = v.lastUpdated
        )
    }
}
