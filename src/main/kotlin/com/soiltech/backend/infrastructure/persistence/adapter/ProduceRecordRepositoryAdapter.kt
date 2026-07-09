package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.ProduceRecord
import com.soiltech.backend.domain.enum.CollectionStatus
import com.soiltech.backend.domain.enum.SyncStatus
import com.soiltech.backend.domain.repository.ProduceRecordRepository
import com.soiltech.backend.infrastructure.persistence.entity.ProduceRecordJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.ProduceRecordJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Component
class ProduceRecordRepositoryAdapter(
    private val jpaRepository: ProduceRecordJpaRepository
) : ProduceRecordRepository {

    override fun findById(id: UUID): ProduceRecord? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findAll(agentId: UUID, farmerId: UUID?, status: CollectionStatus?, pageable: Pageable): Page<ProduceRecord> =
        jpaRepository.findAllFiltered(agentId, farmerId, status, pageable).map { it.toDomain() }

    override fun save(record: ProduceRecord): ProduceRecord =
        jpaRepository.save(ProduceRecordJpaEntity.fromDomain(record)).toDomain()

    override fun update(record: ProduceRecord): ProduceRecord {
        val entity = jpaRepository.findById(record.id).orElseThrow()
        entity.apply {
            status = record.status
            collectedAt = record.collectedAt
            notes = record.notes
            syncStatus = record.syncStatus
            quantityKg = record.quantityKg
            pricePerKg = record.pricePerKg
            totalAmount = record.totalAmount
            cropVariety = record.cropVariety
            grade = record.grade
        }
        return jpaRepository.save(entity).toDomain()
    }

    override fun countTodayByAgent(agentId: UUID, dayStart: LocalDateTime, dayEnd: LocalDateTime): Long =
        jpaRepository.countTodayByAgent(agentId, dayStart, dayEnd)

    override fun countDistinctFarmersTodayByAgent(agentId: UUID, dayStart: LocalDateTime, dayEnd: LocalDateTime): Long =
        jpaRepository.countDistinctFarmersTodayByAgent(agentId, dayStart, dayEnd)

    override fun countPendingUploadsByAgent(agentId: UUID): Long =
        jpaRepository.countBySyncStatus(agentId, SyncStatus.PENDING)

    override fun countOfflineByAgent(agentId: UUID): Long =
        jpaRepository.countBySyncStatus(agentId, SyncStatus.CONFLICT)

    override fun sumWeightTodayByAgent(agentId: UUID, dayStart: LocalDateTime, dayEnd: LocalDateTime): BigDecimal =
        jpaRepository.sumWeightTodayByAgent(agentId, dayStart, dayEnd)

    override fun sumWeightWeekByAgent(agentId: UUID, weekStart: LocalDateTime, weekEnd: LocalDateTime): BigDecimal =
        jpaRepository.sumWeightWeekByAgent(agentId, weekStart, weekEnd)

    override fun sumWeightByDayOfWeekForWeek(agentId: UUID, weekStart: LocalDateTime, weekEnd: LocalDateTime): List<Array<Any>> =
        jpaRepository.sumWeightByDayOfWeekForWeek(agentId, weekStart, weekEnd)

    override fun countAllByAgent(agentId: UUID): Long =
        jpaRepository.countAllByAgent(agentId)

    override fun countCollectedByAgent(agentId: UUID): Long =
        jpaRepository.countByAgentAndStatus(agentId, CollectionStatus.COLLECTED)

    override fun sumTotalWeightByAgent(agentId: UUID): BigDecimal =
        jpaRepository.sumTotalWeightByAgent(agentId)

    override fun findRecentByAgent(agentId: UUID, limit: Int): List<ProduceRecord> =
        jpaRepository.findRecentByAgent(agentId, PageRequest.of(0, limit.coerceIn(1, 50)))
            .map { it.toDomain() }
}
