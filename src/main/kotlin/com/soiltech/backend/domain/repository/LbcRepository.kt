package com.soiltech.backend.domain.repository

import com.soiltech.backend.domain.entity.Lbc
import com.soiltech.backend.domain.enum.LbcStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime
import java.util.UUID

interface LbcRepository {
    fun findById(id: UUID): Lbc?
    fun findByCode(code: String): Lbc?
    fun findByEmail(email: String): Lbc?
    fun findAll(status: LbcStatus?, region: String?, search: String?, pageable: Pageable): Page<Lbc>
    fun countByStatus(): Map<LbcStatus, Long>
    fun findAllForExport(status: LbcStatus?, region: String?, ids: List<UUID>?): List<Lbc>
    fun save(lbc: Lbc): Lbc
    fun delete(id: UUID)
    fun existsByCode(code: String): Boolean
    fun existsByEmail(email: String): Boolean
    fun existsByCodeAndIdNot(code: String, id: UUID): Boolean
    fun existsByEmailAndIdNot(email: String, id: UUID): Boolean
    fun countCreatedBetween(from: LocalDateTime, to: LocalDateTime): Long
}
