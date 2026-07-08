package com.soiltech.backend.infrastructure.persistence.entity

import com.soiltech.backend.domain.entity.Lbc
import com.soiltech.backend.domain.enum.LbcStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(
    name = "lbcs",
    indexes = [
        Index(name = "idx_lbcs_code", columnList = "code", unique = true),
        Index(name = "idx_lbcs_email", columnList = "email", unique = true),
        Index(name = "idx_lbcs_status", columnList = "status"),
        Index(name = "idx_lbcs_region", columnList = "region")
    ]
)
class LbcJpaEntity(
    @Id
    val id: UUID? = null,

    @Column(unique = true)
    var userId: UUID? = null,

    @Column(nullable = false, length = 255)
    var name: String,

    @Column(nullable = false, unique = true, length = 50)
    var code: String,

    @Column(nullable = false, length = 100)
    var region: String,

    @Column(nullable = false, length = 100)
    var district: String,

    @Column(nullable = false, length = 255)
    var manager: String,

    @Column(nullable = false, length = 20)
    var phone: String,

    @Column(nullable = false, length = 255)
    var email: String,

    @Column(nullable = false)
    var agents: Int = 0,

    @Column(nullable = false)
    var farmers: Int = 0,

    @Column(nullable = false, precision = 15, scale = 2)
    var produceTonnes: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false, precision = 20, scale = 2)
    var revenue: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var compliance: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)")
    var status: LbcStatus = LbcStatus.ACTIVE,

    @Column(nullable = false)
    var joinedDate: LocalDateTime = LocalDateTime.now()
) : BaseJpaEntity() {

    fun toDomain(): Lbc = Lbc(
        id = id!!,
        userId = userId,
        name = name,
        code = code,
        region = region,
        district = district,
        manager = manager,
        phone = phone,
        email = email,
        agents = agents,
        farmers = farmers,
        produceTonnes = produceTonnes,
        revenue = revenue,
        compliance = compliance,
        status = status,
        joinedDate = joinedDate,
        createdAt = createdAt,
        updatedAt = updatedAt,
        createdBy = createdBy,
        updatedBy = updatedBy
    )

    companion object {
        fun fromDomain(lbc: Lbc): LbcJpaEntity = LbcJpaEntity(
            id = lbc.id,
            userId = lbc.userId,
            name = lbc.name,
            code = lbc.code,
            region = lbc.region,
            district = lbc.district,
            manager = lbc.manager,
            phone = lbc.phone,
            email = lbc.email,
            agents = lbc.agents,
            farmers = lbc.farmers,
            produceTonnes = lbc.produceTonnes,
            revenue = lbc.revenue,
            compliance = lbc.compliance,
            status = lbc.status,
            joinedDate = lbc.joinedDate
        )
    }
}
