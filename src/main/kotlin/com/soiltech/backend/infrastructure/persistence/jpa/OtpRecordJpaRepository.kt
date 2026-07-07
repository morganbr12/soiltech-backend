package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.OtpRecordJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OtpRecordJpaRepository : JpaRepository<OtpRecordJpaEntity, UUID> {
    fun findTopByEmailOrderByCreatedAtDesc(email: String): OtpRecordJpaEntity?

    @Modifying
    @Query("DELETE FROM OtpRecordJpaEntity o WHERE o.email = :email")
    fun deleteByEmail(email: String)
}
