package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.domain.enum.LbcStatus
import com.soiltech.backend.infrastructure.persistence.entity.LbcJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface LbcJpaRepository : JpaRepository<LbcJpaEntity, UUID>, JpaSpecificationExecutor<LbcJpaEntity> {

    fun findByCode(code: String): LbcJpaEntity?

    fun findByEmail(email: String): LbcJpaEntity?

    fun existsByCode(code: String): Boolean

    fun existsByEmail(email: String): Boolean

    fun existsByCodeAndIdNot(code: String, id: UUID): Boolean

    fun existsByEmailAndIdNot(email: String, id: UUID): Boolean

    @Query("SELECT l.status, COUNT(l) FROM LbcJpaEntity l GROUP BY l.status")
    fun countGroupByStatus(): List<Array<Any>>
}
