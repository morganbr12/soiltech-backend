package com.soiltech.backend.domain.enum

enum class NotificationType {
    // Produce
    PRODUCE_SUBMITTED,
    PRODUCE_APPROVED,
    PRODUCE_REJECTED,
    // Orders
    ORDER_PLACED,
    ORDER_CONFIRMED,
    ORDER_AGENT_CONFIRMED,
    ORDER_DRIVER_DISPATCHED,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    ORDER_CANCELLED,
    // Chat
    NEW_CHAT_MESSAGE,
    // Payments
    PAYMENT_RECEIVED,
    // System
    SYSTEM
}
