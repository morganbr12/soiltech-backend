package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.OrderItem
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.UUID

@Entity
@Table(
    name = "order_items",
    indexes = [Index(name = "idx_order_items_order_id", columnList = "orderId")]
)
class OrderItemJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(nullable = false)
    val orderId: UUID,

    @Column(nullable = false)
    val productId: UUID,

    @Column(nullable = false)
    val quantity: Int,

    @Column(nullable = false, precision = 12, scale = 2)
    val unitPrice: BigDecimal,

    @Column(nullable = false, precision = 14, scale = 2)
    val subtotal: BigDecimal
) {
    fun toDomain(): OrderItem = OrderItem(
        id = id!!,
        orderId = orderId,
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice,
        subtotal = subtotal
    )

    companion object {
        fun fromDomain(item: OrderItem): OrderItemJpaEntity = OrderItemJpaEntity(
            id = item.id,
            orderId = item.orderId,
            productId = item.productId,
            quantity = item.quantity,
            unitPrice = item.unitPrice,
            subtotal = item.subtotal
        )
    }
}
