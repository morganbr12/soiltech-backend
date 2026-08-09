package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.VehicleLocationHistory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "vehicle_location_history",
    indexes = [
        Index(name = "idx_vlh_vehicle_id", columnList = "vehicle_id"),
        Index(name = "idx_vlh_recorded_at", columnList = "recorded_at")
    ]
)
class VehicleLocationHistoryJpaEntity(
    @Id
    val id: UUID,

    @Column(name = "vehicle_id", nullable = false)
    val vehicleId: UUID,

    @Column(nullable = false)
    val lat: Double,

    @Column(nullable = false)
    val lng: Double,

    @Column(nullable = false)
    val speed: Double = 0.0,

    @Column(nullable = false)
    val heading: Double = 0.0,

    @Column(name = "recorded_at", nullable = false)
    val recordedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain() = VehicleLocationHistory(
        id = id,
        vehicleId = vehicleId,
        lat = lat,
        lng = lng,
        speed = speed,
        heading = heading,
        recordedAt = recordedAt
    )

    companion object {
        fun fromDomain(v: VehicleLocationHistory) = VehicleLocationHistoryJpaEntity(
            id = v.id,
            vehicleId = v.vehicleId,
            lat = v.lat,
            lng = v.lng,
            speed = v.speed,
            heading = v.heading,
            recordedAt = v.recordedAt
        )
    }
}
