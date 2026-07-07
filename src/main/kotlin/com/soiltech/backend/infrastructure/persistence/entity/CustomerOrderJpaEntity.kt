package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.CustomerOrder
import com.soiltech.backend.domain.enum.OrderStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    name = "customer_orders",
    indexes = [
        Index(name = "idx_orders_customer_id", columnList = "customerId"),
        Index(name = "idx_orders_status", columnList = "status")
    ]
)
class CustomerOrderJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val customerId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: OrderStatus = OrderStatus.PENDING,

    @Column(nullable = false, precision = 14, scale = 2)
    var totalAmount: BigDecimal,

    @Column(nullable = false, length = 1000)
    var deliveryAddress: String,

    @Column(length = 1000)
    var notes: String? = null
) : BaseJpaEntity() {


    fun toDomain(): CustomerOrder = CustomerOrder(
        id = id!!,
        customerId = customerId,
        status = status,
        totalAmount = totalAmount,
        deliveryAddress = deliveryAddress,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(order: CustomerOrder): CustomerOrderJpaEntity = CustomerOrderJpaEntity(
            id = order.id,
            customerId = order.customerId,
            status = order.status,
            totalAmount = order.totalAmount,
            deliveryAddress = order.deliveryAddress,
            notes = order.notes
        )
    }
}
