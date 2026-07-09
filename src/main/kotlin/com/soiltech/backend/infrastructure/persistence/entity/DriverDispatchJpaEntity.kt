package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.DriverDispatch
import com.soiltech.backend.domain.enum.DispatchStatus
import jakarta.persistence.*
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
    name = "driver_dispatches",
    indexes = [
        Index(name = "idx_dispatch_order_id", columnList = "orderId"),
        Index(name = "idx_dispatch_vehicle_id", columnList = "vehicleId"),
        Index(name = "idx_dispatch_status", columnList = "status")
    ]
)
class DriverDispatchJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val orderId: UUID,

    @Column(length = 255)
    val customerName: String? = null,

    @Column(nullable = false)
    val vehicleId: UUID,

    @Column(nullable = false, length = 100)
    var driverName: String,

    @Column(nullable = false, length = 20)
    var plateNumber: String,

    @Column(nullable = false, length = 50)
    var vehicleType: String,

    @Column(nullable = false)
    var scheduledDate: LocalDate,

    @Column(length = 500)
    var pickupLocation: String? = null,

    @Column(length = 1000)
    var notes: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var status: DispatchStatus = DispatchStatus.PENDING

) : BaseJpaEntity() {

    fun toDomain(): DriverDispatch = DriverDispatch(
        id = id!!,
        orderId = orderId,
        customerName = customerName,
        vehicleId = vehicleId,
        driverName = driverName,
        plateNumber = plateNumber,
        vehicleType = vehicleType,
        scheduledDate = scheduledDate,
        pickupLocation = pickupLocation,
        notes = notes,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBy = createdBy,
        updatedBy = updatedBy
    )

    companion object {
        fun fromDomain(dispatch: DriverDispatch): DriverDispatchJpaEntity = DriverDispatchJpaEntity(
            id = dispatch.id,
            orderId = dispatch.orderId,
            customerName = dispatch.customerName,
            vehicleId = dispatch.vehicleId,
            driverName = dispatch.driverName,
            plateNumber = dispatch.plateNumber,
            vehicleType = dispatch.vehicleType,
            scheduledDate = dispatch.scheduledDate,
            pickupLocation = dispatch.pickupLocation,
            notes = dispatch.notes,
            status = dispatch.status
        )
    }
}
