package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.ProduceRecord
import com.soiltech.backend.domain.enum.CollectionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ProduceRecordRepository {
    fun findById(id: UUID): ProduceRecord?
    fun findAll(agentId: UUID, farmerId: UUID?, status: CollectionStatus?, pageable: Pageable): Page<ProduceRecord>
    fun save(record: ProduceRecord): ProduceRecord
    fun update(record: ProduceRecord): ProduceRecord
}
