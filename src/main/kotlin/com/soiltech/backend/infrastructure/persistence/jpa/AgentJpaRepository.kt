package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.AgentJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AgentJpaRepository : JpaRepository<AgentJpaEntity, UUID>, JpaSpecificationExecutor<AgentJpaEntity> {

    fun existsByEmail(email: String): Boolean
    fun existsByPhone(phone: String): Boolean
    fun existsByEmailAndIdNot(email: String, id: UUID): Boolean
    fun existsByPhoneAndIdNot(phone: String, id: UUID): Boolean
    fun existsByAgentCode(code: String): Boolean

    @Query("SELECT a.status, COUNT(a) FROM AgentJpaEntity a GROUP BY a.status")
    fun countGroupByStatus(): List<Array<Any>>

    @Query("SELECT f.agentId, COUNT(f) FROM FarmerJpaEntity f WHERE f.agentId IN :ids GROUP BY f.agentId")
    fun countFarmersByAgentIds(@Param("ids") ids: List<UUID>): List<Array<Any>>

    @Query("""
        SELECT f.agentId, COUNT(fm)
        FROM FarmerJpaEntity f, FarmJpaEntity fm
        WHERE fm.farmerId = f.id AND f.agentId IN :ids
        GROUP BY f.agentId
    """)
    fun countFarmsByAgentIds(@Param("ids") ids: List<UUID>): List<Array<Any>>

    @Query("SELECT pr.agentId, COALESCE(SUM(pr.quantityKg), 0) FROM ProduceRecordJpaEntity pr WHERE pr.agentId IN :ids GROUP BY pr.agentId")
    fun sumProduceByAgentIds(@Param("ids") ids: List<UUID>): List<Array<Any>>

    fun findByAgentCode(agentCode: String): AgentJpaEntity?
}
