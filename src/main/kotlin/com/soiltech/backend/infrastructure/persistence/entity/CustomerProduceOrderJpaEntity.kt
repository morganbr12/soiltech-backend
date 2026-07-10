package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.CustomerProduceOrder
import com.soiltech.backend.domain.enum.ProduceOrderStatus
import com.soiltech.backend.domain.enum.ProducePaymentStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

@Entity
@Table(
    name = "customer_produce_orders",
    indexes = [
        Index(name = "idx_produce_orders_customer_id", columnList = "customerId"),
        Index(name = "idx_produce_orders_status", columnList = "status"),
        Index(name = "idx_produce_orders_payment_status", columnList = "paymentStatus")
    ]
)
class CustomerProduceOrderJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false, unique = true, length = 20)
    var orderCode: String,

    @Column(nullable = false)
    val customerId: UUID,

    @Column(nullable = false, length = 20)
    var customerCode: String,

    @Column(nullable = false, length = 255)
    var customerName: String,

    @Column
    var farmerId: UUID? = null,

    @Column
    var agentId: UUID? = null,

    @Column(nullable = false, length = 100)
    var produce: String,

    @Column(nullable = false)
    var quantityKg: Double,

    @Column(nullable = false, precision = 12, scale = 2)
    var pricePerKg: BigDecimal,

    @Column(nullable = false, precision = 16, scale = 2)
    var totalAmount: BigDecimal,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var status: ProduceOrderStatus = ProduceOrderStatus.PENDING,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var paymentStatus: ProducePaymentStatus = ProducePaymentStatus.UNPAID,

    @Column(length = 50)
    var assignedAgent: String? = null,

    @Column(length = 50)
    var assignedDriver: String? = null,

    @Column(nullable = false, length = 100)
    var region: String,

    @Column(length = 1000)
    var cancellationReason: String? = null,

    @Column(nullable = false)
    var orderDate: LocalDate,

    var deliveryDate: LocalDate? = null,

    @Column(length = 200)
    var farmerName: String? = null,

    @Column(length = 30)
    var farmerPhone: String? = null,

    @Column(length = 30)
    var agentPhone: String? = null

) : BaseJpaEntity() {

    fun toDomain(): CustomerProduceOrder = CustomerProduceOrder(
        id = id!!,
        orderCode = orderCode,
        customerId = customerId,
        customerCode = customerCode,
        customerName = customerName,
        farmerId = farmerId,
        agentId = agentId,
        produce = produce,
        quantityKg = quantityKg,
        pricePerKg = pricePerKg,
        totalAmount = totalAmount,
        status = status,
        paymentStatus = paymentStatus,
        assignedAgent = assignedAgent,
        assignedDriver = assignedDriver,
        region = region,
        cancellationReason = cancellationReason,
        orderDate = orderDate,
        deliveryDate = deliveryDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        farmerName = farmerName,
        farmerPhone = farmerPhone,
        agentPhone = agentPhone
    )

    companion object {
        fun fromDomain(o: CustomerProduceOrder): CustomerProduceOrderJpaEntity = CustomerProduceOrderJpaEntity(
            id = o.id,
            orderCode = o.orderCode,
            customerId = o.customerId,
            customerCode = o.customerCode,
            customerName = o.customerName,
            farmerId = o.farmerId,
            agentId = o.agentId,
            produce = o.produce,
            quantityKg = o.quantityKg,
            pricePerKg = o.pricePerKg,
            totalAmount = o.totalAmount,
            status = o.status,
            paymentStatus = o.paymentStatus,
            assignedAgent = o.assignedAgent,
            assignedDriver = o.assignedDriver,
            region = o.region,
            cancellationReason = o.cancellationReason,
            orderDate = o.orderDate,
            deliveryDate = o.deliveryDate,
            farmerName = o.farmerName,
            farmerPhone = o.farmerPhone,
            agentPhone = o.agentPhone
        )
    }
}
