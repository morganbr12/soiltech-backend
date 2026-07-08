package com.soiltech.backend.infrastructure.persistence.adapter

import com.soiltech.backend.domain.entity.CustomerMetrics
import com.soiltech.backend.domain.entity.CustomerProfile
import com.soiltech.backend.domain.enum.CustomerStatus
import com.soiltech.backend.domain.enum.CustomerTier
import com.soiltech.backend.domain.repository.CustomerProfileRepository
import com.soiltech.backend.infrastructure.persistence.entity.CustomerProfileJpaEntity
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerProduceOrderJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerProfileJpaRepository
import com.soiltech.backend.infrastructure.persistence.jpa.CustomerWalletJpaRepository
import jakarta.persistence.criteria.Predicate
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Component
class CustomerProfileRepositoryAdapter(
    private val jpaRepository: CustomerProfileJpaRepository,
    private val walletJpaRepository: CustomerWalletJpaRepository,
    private val orderJpaRepository: CustomerProduceOrderJpaRepository
) : CustomerProfileRepository {

    override fun findById(id: UUID): CustomerProfile? =
        jpaRepository.findById(id).orElse(null)?.toDomain()

    override fun findByUserId(userId: UUID): CustomerProfile? =
        jpaRepository.findByUserId(userId)?.toDomain()

    override fun save(profile: CustomerProfile): CustomerProfile =
        jpaRepository.save(CustomerProfileJpaEntity.fromDomain(profile)).toDomain()

    override fun update(profile: CustomerProfile): CustomerProfile {
        val entity = jpaRepository.findById(profile.id).orElseThrow()
        entity.apply {
            firstName = profile.firstName
            lastName = profile.lastName
            fullName = profile.fullName
            email = profile.email
            phone = profile.phone
            region = profile.region
            district = profile.district
            address = profile.address
            profileImageUrl = profile.profileImageUrl
            accountType = profile.accountType
            location = profile.location
            status = profile.status
            tier = profile.tier
            businessName = profile.businessName
            businessType = profile.businessType
            nationalId = profile.nationalId
            isVerified = profile.isVerified
            verifiedDate = profile.verifiedDate
            rejectionReason = profile.rejectionReason
            rating = profile.rating
            lat = profile.lat
            lng = profile.lng
        }
        return jpaRepository.save(entity).toDomain()
    }

    override fun delete(id: UUID) = jpaRepository.deleteById(id)

    override fun findAll(
        status: CustomerStatus?,
        tier: CustomerTier?,
        region: String?,
        search: String?,
        pageable: Pageable
    ): Page<CustomerProfile> {
        val spec = buildSpec(status, tier, region, search)
        return jpaRepository.findAll(spec, pageable).map { it.toDomain() }
    }

    override fun countByStatus(): Map<CustomerStatus, Long> {
        val raw = jpaRepository.countGroupByStatus()
        return raw.associate { row ->
            val status = when (val v = row[0]) {
                is CustomerStatus -> v
                is String -> CustomerStatus.entries.firstOrNull { it.name == v } ?: return@associate null to 0L
                else -> return@associate null to 0L
            }
            status to (row[1] as Number).toLong()
        }.filterKeys { it != null } as Map<CustomerStatus, Long>
    }

    override fun countByTier(): Map<CustomerTier, Long> {
        val raw = jpaRepository.countGroupByTier()
        return raw.associate { row ->
            val tier = when (val v = row[0]) {
                is CustomerTier -> v
                is String -> CustomerTier.entries.firstOrNull { it.name == v } ?: return@associate null to 0L
                else -> return@associate null to 0L
            }
            tier to (row[1] as Number).toLong()
        }.filterKeys { it != null } as Map<CustomerTier, Long>
    }

    override fun countAll(): Long = jpaRepository.countAll()

    override fun findMetricsByCustomerIds(ids: List<UUID>): Map<UUID, CustomerMetrics> {
        if (ids.isEmpty()) return emptyMap()

        val spentMap = orderJpaRepository.sumTotalSpentByCustomerIds(ids)
            .associate { row -> (row[0] as UUID) to (row[1] as BigDecimal) }
        val countMap = orderJpaRepository.countByCustomerIds(ids)
            .associate { row -> (row[0] as UUID) to (row[1] as Number).toLong() }
        val lastOrderMap = orderJpaRepository.findLastOrderDatesByCustomerIds(ids)
            .associate { row -> (row[0] as UUID) to (row[1] as LocalDateTime) }
        val balanceMap = walletJpaRepository.findBalancesByCustomerIds(ids)
            .associate { row -> (row[0] as UUID) to (row[1] as BigDecimal) }

        return ids.associateWith { id ->
            CustomerMetrics(
                totalOrders = countMap[id] ?: 0L,
                totalSpent = spentMap[id] ?: BigDecimal.ZERO,
                walletBalance = balanceMap[id] ?: BigDecimal.ZERO,
                lastOrderDate = lastOrderMap[id]
            )
        }
    }

    override fun findTopCustomers(limit: Int): List<CustomerProfile> =
        jpaRepository.findTopByOrderByCreatedAtDesc(PageRequest.of(0, limit)).map { it.toDomain() }

    override fun countMonthlyNewCustomers(year: Int): List<Long> {
        val raw = jpaRepository.countMonthlyByYear(year)
        val monthMap = raw.associate { row -> (row[0] as Number).toInt() to (row[1] as Number).toLong() }
        return (1..12).map { m -> monthMap[m] ?: 0L }
    }

    override fun avgRating(): Double = jpaRepository.avgRating()

    override fun existsByCustomerCode(code: String): Boolean = jpaRepository.existsByCustomerCode(code)
    override fun existsByNationalId(nationalId: String): Boolean = jpaRepository.existsByNationalId(nationalId)
    override fun existsByNationalIdAndIdNot(nationalId: String, id: UUID): Boolean = jpaRepository.existsByNationalIdAndIdNot(nationalId, id)
    override fun existsByPhone(phone: String): Boolean = jpaRepository.existsByPhone(phone)
    override fun existsByPhoneAndIdNot(phone: String, id: UUID): Boolean = jpaRepository.existsByPhoneAndIdNot(phone, id)
    override fun existsByEmail(email: String): Boolean = jpaRepository.existsByEmail(email)
    override fun existsByEmailAndIdNot(email: String, id: UUID): Boolean = jpaRepository.existsByEmailAndIdNot(email, id)

    private fun buildSpec(
        status: CustomerStatus?,
        tier: CustomerTier?,
        region: String?,
        search: String?
    ) = org.springframework.data.jpa.domain.Specification<CustomerProfileJpaEntity> { root, _, cb ->
        val predicates = mutableListOf<Predicate>()
        status?.let { predicates.add(cb.equal(root.get<CustomerStatus>("status"), it)) }
        tier?.let { predicates.add(cb.equal(root.get<CustomerTier>("tier"), it)) }
        region?.let { predicates.add(cb.equal(cb.lower(root.get("region")), it.lowercase())) }
        search?.let { q ->
            val like = "%${q.lowercase()}%"
            predicates.add(
                cb.or(
                    cb.like(cb.lower(root.get("fullName")), like),
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("phone")), like),
                    cb.like(cb.lower(root.get("customerCode")), like),
                    cb.like(cb.lower(root.get("nationalId")), like)
                )
            )
        }
        cb.and(*predicates.toTypedArray())
    }
}
