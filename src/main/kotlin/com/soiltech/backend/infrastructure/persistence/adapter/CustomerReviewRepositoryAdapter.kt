package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.CustomerReview
import com.soiltech.backend.domain.enum.ReviewStatus
import com.soiltech.backend.domain.enum.ReviewTargetType
import com.soiltech.backend.domain.repository.CustomerReviewRepository
import com.soiltech.backend.infrastructure.persistence.entity.CustomerReviewJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerReviewJpaRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CustomerReviewRepositoryAdapter(
    private val jpa: CustomerReviewJpaRepository
) : CustomerReviewRepository {

    override fun findById(id: UUID): CustomerReview? =
        jpa.findById(id).orElse(null)?.toDomain()

    override fun findAll(
        status: ReviewStatus?,
        targetType: ReviewTargetType?,
        region: String?,
        search: String?,
        pageable: Pageable
    ): Page<CustomerReview> {
        val spec = Specification<CustomerReviewJpaEntity> { root, _, cb ->
            val predicates = mutableListOf<Predicate>()
            status?.let { predicates.add(cb.equal(root.get<ReviewStatus>("status"), it)) }
            targetType?.let { predicates.add(cb.equal(root.get<ReviewTargetType>("targetType"), it)) }
            region?.let { predicates.add(cb.equal(cb.lower(root.get("region")), it.lowercase())) }
            search?.let { q ->
                val like = "%${q.lowercase()}%"
                predicates.add(cb.or(
                    cb.like(cb.lower(root.get("customerName")), like),
                    cb.like(cb.lower(root.get("targetName")), like),
                    cb.like(cb.lower(root.get("comment")), like)
                ))
            }
            cb.and(*predicates.toTypedArray())
        }
        return jpa.findAll(spec, pageable).map { it.toDomain() }
    }

    override fun save(review: CustomerReview): CustomerReview {
        val existing = jpa.findById(review.id).orElse(null)
        return if (existing != null) {
            existing.apply {
                status = review.status
                flagReason = review.flagReason
            }
            jpa.save(existing).toDomain()
        } else {
            jpa.save(CustomerReviewJpaEntity.fromDomain(review)).toDomain()
        }
    }

    override fun delete(id: UUID) = jpa.deleteById(id)

    override fun countByStatus(): Map<ReviewStatus, Long> {
        return jpa.countGroupByStatus().associate { row ->
            val status = when (val v = row[0]) {
                is ReviewStatus -> v
                is String -> ReviewStatus.entries.firstOrNull { it.name == v } ?: return@associate null to 0L
                else -> return@associate null to 0L
            }
            status to (row[1] as Number).toLong()
        }.filterKeys { it != null } as Map<ReviewStatus, Long>
    }

    override fun avgRating(): Double = jpa.avgRating()
}
