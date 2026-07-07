package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.PaymentRecord
import com.soiltech.backend.domain.enum.PaymentStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface PaymentRecordRepository {
    fun findById(id: UUID): PaymentRecord?
    fun findAll(agentId: UUID, farmerId: UUID?, status: PaymentStatus?, pageable: Pageable): Page<PaymentRecord>
    fun save(record: PaymentRecord): PaymentRecord
}
