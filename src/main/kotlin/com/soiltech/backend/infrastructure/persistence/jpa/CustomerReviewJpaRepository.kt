package com.soiltech.backend.infrastructure.persistence.jpa

import com.soiltech.backend.infrastructure.persistence.entity.CustomerReviewJpaEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CustomerReviewJpaRepository :
    JpaRepository<CustomerReviewJpaEntity, UUID>,
    JpaSpecificationExecutor<CustomerReviewJpaEntity> {

    @Query("SELECT r.status, COUNT(r) FROM CustomerReviewJpaEntity r GROUP BY r.status")
    fun countGroupByStatus(): List<Array<Any>>

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM CustomerReviewJpaEntity r")
    fun avgRating(): Double
}
