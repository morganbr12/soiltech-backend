package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.PaymentRecord
import com.soiltech.backend.domain.enum.PaymentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

interface PaymentRecordRepository {
    fun findById(id: UUID): PaymentRecord?
    fun findAll(agentId: UUID, farmerId: UUID?, status: PaymentStatus?, pageable: Pageable): Page<PaymentRecord>
    fun save(record: PaymentRecord): PaymentRecord

    // Dashboard aggregates
    fun sumMonthlyRevenueByAgent(agentId: UUID, monthStart: LocalDateTime, monthEnd: LocalDateTime): BigDecimal
    fun findRecentByAgent(agentId: UUID, limit: Int): List<PaymentRecord>
    fun countRecentByAgent(agentId: UUID, since: LocalDateTime): Long

    // Admin dashboard
    fun sumAmountByStatus(status: PaymentStatus): BigDecimal
}
