package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.ProduceRecord
import com.soiltech.backend.domain.enum.CollectionStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

interface ProduceRecordRepository {
    fun findById(id: UUID): ProduceRecord?
    fun findAll(agentId: UUID, farmerId: UUID?, status: CollectionStatus?, pageable: Pageable): Page<ProduceRecord>
    fun save(record: ProduceRecord): ProduceRecord
    fun update(record: ProduceRecord): ProduceRecord

    // Dashboard aggregates
    fun countTodayByAgent(agentId: UUID, dayStart: LocalDateTime, dayEnd: LocalDateTime): Long
    fun countDistinctFarmersTodayByAgent(agentId: UUID, dayStart: LocalDateTime, dayEnd: LocalDateTime): Long
    fun countPendingUploadsByAgent(agentId: UUID): Long
    fun countOfflineByAgent(agentId: UUID): Long
    fun sumWeightTodayByAgent(agentId: UUID, dayStart: LocalDateTime, dayEnd: LocalDateTime): BigDecimal
    fun sumWeightWeekByAgent(agentId: UUID, weekStart: LocalDateTime, weekEnd: LocalDateTime): BigDecimal
    fun sumWeightByDayOfWeekForWeek(agentId: UUID, weekStart: LocalDateTime, weekEnd: LocalDateTime): List<Array<Any>>
    fun countAllByAgent(agentId: UUID): Long
    fun countCollectedByAgent(agentId: UUID): Long
    fun sumTotalWeightByAgent(agentId: UUID): BigDecimal
    fun findRecentByAgent(agentId: UUID, limit: Int): List<ProduceRecord>
}
