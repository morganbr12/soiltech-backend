package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.Vehicle
import com.soiltech.backend.domain.enum.VehicleStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "vehicles",
    indexes = [
        Index(name = "idx_vehicles_plate", columnList = "carPlateNumber", unique = true),
        Index(name = "idx_vehicles_status", columnList = "status"),
        Index(name = "idx_vehicles_region", columnList = "region")
    ]
)
class VehicleJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 20)
    var carPlateNumber: String,

    @Column(nullable = false, length = 50)
    var vehicleType: String,

    @Column(nullable = false, length = 100)
    var make: String,

    @Column(nullable = false, length = 100)
    var model: String,

    @Column(nullable = false)
    var year: Int,

    @Column(nullable = false, precision = 10, scale = 2)
    var capacity: BigDecimal,

    @Column(nullable = false)
    var fuelLevel: Double,

    @Column(nullable = false, length = 100)
    var region: String,

    @Column(nullable = false, length = 100)
    var driverName: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var status: VehicleStatus = VehicleStatus.AVAILABLE

) : BaseJpaEntity() {

    fun toDomain(): Vehicle = Vehicle(
        id = id!!,
        carPlateNumber = carPlateNumber,
        vehicleType = vehicleType,
        make = make,
        model = model,
        year = year,
        capacity = capacity,
        fuelLevel = fuelLevel,
        region = region,
        driverName = driverName,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBy = createdBy,
        updatedBy = updatedBy
    )

    companion object {
        fun fromDomain(vehicle: Vehicle): VehicleJpaEntity = VehicleJpaEntity(
            id = vehicle.id,
            carPlateNumber = vehicle.carPlateNumber,
            vehicleType = vehicle.vehicleType,
            make = vehicle.make,
            model = vehicle.model,
            year = vehicle.year,
            capacity = vehicle.capacity,
            fuelLevel = vehicle.fuelLevel,
            region = vehicle.region,
            driverName = vehicle.driverName,
            status = vehicle.status
        )
    }
}
